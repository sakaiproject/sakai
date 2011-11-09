/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2011 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.dash.logic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.sf.ehcache.Cache;

import org.apache.log4j.Logger;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityAdvisor.SecurityAdvice;
import org.sakaiproject.dash.dao.DashboardDao;
import org.sakaiproject.dash.entity.EntityLinkStrategy;
import org.sakaiproject.dash.entity.EntityType;
import org.sakaiproject.dash.entity.RepeatingEventGenerator;
import org.sakaiproject.dash.listener.EventProcessor;
import org.sakaiproject.dash.model.AvailabilityCheck;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.CalendarLink;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.ItemType;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.NewsLink;
import org.sakaiproject.dash.model.Person;
import org.sakaiproject.dash.model.PersonContext;
import org.sakaiproject.dash.model.PersonContextSourceType;
import org.sakaiproject.dash.model.PersonSourceType;
import org.sakaiproject.dash.model.Realm;
import org.sakaiproject.dash.model.RepeatingCalendarItem;
import org.sakaiproject.dash.model.SourceType;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;

/**
 * 
 *
 */
public class DashboardLogicImpl implements DashboardLogic, Observer 
{
	private static Logger logger = Logger.getLogger(DashboardLogicImpl.class);
	
	public static final long TIME_BETWEEN_AVAILABILITY_CHECKS = 1000L * 60L * 1L;

	private static final long ONE_DAY = 1000L * 60L * 60L * 24L;
	private static final long ONE_YEAR = ONE_DAY * 365L;
	
	public static final String MOTD_CONTEXT = "!site";
	
	// this will be a setting
	protected long daysToHorizon = 30L;
	public void setDaysToHorizon(long daysToHorizon) {
		this.daysToHorizon = daysToHorizon;
	}
	
	protected long daysBetweenHorizonUpdates = 7L;
	public void setDaysBetweenHorizonUpdates(long daysBetweenHorizonUpdates) {
		this.daysBetweenHorizonUpdates = daysBetweenHorizonUpdates;
	}
	
	protected Date nextHorizonUpdate = new Date();
	private Date horizon = new Date();
		
	protected long nextTimeToQueryAvailabilityChecks = System.currentTimeMillis();
	
	protected Map<String,EntityType> entityTypes = new HashMap<String,EntityType>();
	protected Map<String,EventProcessor> eventProcessors = new HashMap<String,EventProcessor>();
	
	protected DashboardEventProcessingThread eventProcessingThread = new DashboardEventProcessingThread();
	protected Queue<EventCopy> eventQueue = new ConcurrentLinkedQueue<EventCopy>();
		
	/************************************************************************
	 * Spring-injected classes
	 ************************************************************************/
	
	protected SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy proxy) {
		this.sakaiProxy = proxy;
	}
	
	protected DashboardDao dao;
	public void setDao(DashboardDao dao) {
		this.dao = dao;
	}
	
	protected Cache cache;

	protected static boolean timeToQuit = false;

	public void setCache(Cache cache) {
		this.cache = cache;
	}
	
	/************************************************************************
	 * Dashboard Logic methods
	 ************************************************************************/

	/**
	 * Add calendar items and links for any instances of a repeating event between two dates.
	 * @param repeatingEvent
	 * @param beginDate
	 * @param endDate 
	 */
	public void addCalendarItemsForRepeatingCalendarItem(RepeatingCalendarItem repeatingEvent, Date beginDate, Date endDate) {
		if(beginDate.before(endDate)) {
			if(repeatingEvent == null || repeatingEvent.getSourceType() == null || repeatingEvent.getSourceType().getIdentifier() == null) {
				// TODO: handle error: invalid parameters?
			} else {
				EntityType entityType = entityTypes.get(repeatingEvent.getSourceType().getIdentifier());
				if(entityType == null) {
					// TODO: handle error: entityType cannot be null
				} else if(entityType instanceof RepeatingEventGenerator) {
					
					Map<Integer, Date> dates = ((RepeatingEventGenerator) entityType).generateRepeatingEventDates(repeatingEvent.getEntityReference(), beginDate, endDate);
					if(dates != null) {
						for(Map.Entry<Integer, Date> entry : dates.entrySet()) {
							try {
								// create an instance
								CalendarItem calendarItem = createCalendarItem(repeatingEvent.getTitle(), entry.getValue(), repeatingEvent.getCalendarTimeLabelKey(), 
										repeatingEvent.getEntityReference(), repeatingEvent.getContext(), repeatingEvent.getSourceType(), repeatingEvent, entry.getKey());
								dao.addCalendarItem(calendarItem);
								calendarItem = dao.getCalendarItem(repeatingEvent.getEntityReference(), repeatingEvent.getCalendarTimeLabelKey(), entry.getKey());
								createCalendarLinks(calendarItem);
							} catch(Exception e) {
								// this could occur if we are trying to add an instance that has already been added
								StringBuilder buf = new StringBuilder();
								buf.append("Error trying to add calendar item for repeating event (");
								buf.append(repeatingEvent);
								buf.append(") for date (");
								buf.append(entry.getValue());
								buf.append(") and sequence number (");
								buf.append(entry.getKey());
								buf.append("). Exception: ");
								buf.append(e);
								buf.append(" Message: ");
								buf.append(e.getMessage());
								logger.warn(buf);
							}
						}
					}
				} else {
					// TODO: handle error: entityType must be a RepeatingEventGenerator
				}
			}
		} else {
			// TODO: handle error: invalid parameters? beginDate must be before endDate
		}
	}

	public void addCalendarLinks(String sakaiUserId, String contextId) {
		if(logger.isDebugEnabled()) {
			logger.debug("addCalendarLinks(" + sakaiUserId + "," + contextId + ") ");
		}
		Person person = this.getOrCreatePerson(sakaiUserId);
		if(person == null) {
			logger.warn("Failed attempt to add calendar links for non-existent user: " + sakaiUserId);
		} else {
			// TODO: deal with expired items
			List<CalendarItem> items = dao.getCalendarItemsByContext(contextId);
			if(items == null || items.isEmpty()) {
				StringBuilder message = new StringBuilder();
				message.append("Failed attempt to retrieve calendar events in context (");
				message.append(contextId);
				message.append(") for new user (");
				message.append(sakaiUserId);
				message.append(")");
				logger.info(message.toString());
			} else {
				for(CalendarItem item: items) {
					SourceType sourceType = item.getSourceType();
					EntityType entityType = this.entityTypes.get(sourceType.getIdentifier());
					if(entityType != null && entityType.isUserPermitted(sakaiUserId, sourceType.getAccessPermission(), item.getEntityReference(), item.getContext().getContextId())) {
						CalendarLink calendarLink = new CalendarLink(person, item, item.getContext(), false, false);
						dao.addCalendarLink(calendarLink);
					}
				}
			}
		}
		
	}

	public void addNewsLinks(String sakaiUserId, String contextId) {
		if(logger.isDebugEnabled()) {
			logger.debug("addNewsLinks(" + sakaiUserId + "," + contextId + ") ");
		}
		Person person = this.getOrCreatePerson(sakaiUserId);
		if(person == null) {
			logger.warn("Attempting to add news links for non-existent user: " + sakaiUserId);
		} else {
			// TODO: deal with expired items
			List<NewsItem> items = dao.getNewsItemsByContext(contextId);
			if(items == null || items.isEmpty()) {
				StringBuilder message = new StringBuilder();
				message.append("Failed attempt to retrieve news events in context (");
				message.append(contextId);
				message.append(") for new user (");
				message.append(sakaiUserId);
				message.append(")");
				logger.info(message.toString());
			} else {
				for(NewsItem item: items) {
					SourceType sourceType = item.getSourceType();
					EntityType entityType = this.entityTypes.get(sourceType.getIdentifier());
					if(entityType != null && entityType.isUserPermitted(sakaiUserId, sourceType.getAccessPermission(), item.getEntityReference() , item.getContext().getContextId()) ) {
						NewsLink newsLink = new NewsLink(person, item, item.getContext(), false, false);
						dao.addNewsLink(newsLink);
					}
				}
			}
		}
	}

	public CalendarItem createCalendarItem(String title, Date calendarTime,
			String calendarTimeLabelKey, String entityReference, Context context,
			SourceType sourceType, RepeatingCalendarItem repeatingCalendarItem, Integer sequenceNumber) {
		
		CalendarItem calendarItem = new CalendarItem(title, calendarTime,
				calendarTimeLabelKey, entityReference, context, sourceType, null, repeatingCalendarItem, sequenceNumber);
		
		dao.addCalendarItem(calendarItem);
		
		return dao.getCalendarItem(entityReference, calendarTimeLabelKey, sequenceNumber);
	}

	public RepeatingCalendarItem createRepeatingCalendarItem(String title, Date firstTime,
			Date lastTime, String calendarTimeLabelKey, String entityReference, Context context, 
			SourceType sourceType, String frequency, int count) {
		
		RepeatingCalendarItem repeatingCalendarItem = new RepeatingCalendarItem(title, firstTime,
				lastTime, calendarTimeLabelKey, entityReference, null, context, sourceType, frequency, count);
		
		dao.addRepeatingCalendarItem(repeatingCalendarItem);
		
		repeatingCalendarItem = dao.getRepeatingCalendarItem(entityReference, calendarTimeLabelKey);
		
		this.addCalendarItemsForRepeatingCalendarItem(repeatingCalendarItem, new Date(), horizon);
		
		return repeatingCalendarItem;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#createCalendarLinks(org.sakaiproject.dash.model.CalendarItem)
	 */
	public void createCalendarLinks(CalendarItem calendarItem) {
		if(logger.isDebugEnabled()) {
			logger.debug("createCalendarLinks(" + calendarItem + ")");
		}
		if(calendarItem != null) {
			List<String> sakaiIds = this.sakaiProxy.getUsersWithReadAccess(calendarItem.getEntityReference(), calendarItem.getSourceType().getAccessPermission());
			for(String sakaiId : sakaiIds) {
				Person person = getOrCreatePerson(sakaiId);
				if(person == null) {
					logger.warn("Error retrieving user " + sakaiId);
				} else {
					CalendarLink link = new CalendarLink(person, calendarItem, calendarItem.getContext(), false, false);
					dao.addCalendarLink(link);
				}
			}
		}
	}
	
	public Context createContext(String contextId) { 
		Context context = null;
		if (contextId.equals(MOTD_CONTEXT))
		{
			// add an exception for MOTD announcement, where the context id is "!site"
			context = new Context("!site", "MOTD", this.sakaiProxy.getConfigParam("serverUr", "")+ "/access/content/public/MOTD%20files/");
		}
		else
		{
			// get the site id, title and url
			Site site = this.sakaiProxy.getSite(contextId);
			context = new Context(site.getId(), site.getTitle(), site.getUrl());
		}
		if (context != null)
		{
			dao.addContext(context);
		}
		return context;
	}

	public NewsItem createNewsItem(String title, Date newsTime,
			String labelKey, String entityReference, Context context, SourceType sourceType, String subtype) {
		
		NewsItem newsItem = new NewsItem(title, newsTime, 
				labelKey, entityReference, context, sourceType, subtype);
		
		dao.addNewsItem(newsItem);
		
		return dao.getNewsItem(entityReference) ;
	}


	public void createNewsLinks(NewsItem newsItem) {
		if(logger.isDebugEnabled()) {
			logger.debug("createNewsLinks(" + newsItem + ")");
		}
		if(newsItem != null) {
			List<String> sakaiIds = this.sakaiProxy.getUsersWithReadAccess(newsItem.getEntityReference(), newsItem.getSourceType().getAccessPermission());
			if(sakaiIds != null && sakaiIds.size() > 0) {
				for(String sakaiId : sakaiIds) {
					try {
						Person person = getOrCreatePerson(sakaiId);
						if(person != null) {
							NewsLink link = new NewsLink(person, newsItem, newsItem.getContext(), false, false);
							dao.addNewsLink(link);
						}
					} catch(Exception e) {
						logger.warn("Error trying to retrieve or add person " + sakaiId + " for news-item " + newsItem, e);
					}
				}
			}
		}
	}


	public Realm createRealm(String entityReference, String contextId) {
		
//		String realmId = null;
//		Collection<String> groups = this.sakaiProxy.getRealmId(entityReference, contextId);
//		if(groups != null && groups.size() > 0) {
//			List<String> authzGroups = new ArrayList<String>(groups );
//			if(authzGroups != null && authzGroups.size() > 0) {
//				realmId = authzGroups.get(0);
//			}
//			if(realmId != null) {
//				Realm realm = dao.getRealm(realmId);
//				if(realm == null) {
//					realm = new Realm(realmId);
//					dao.addRealm(realm);
//				}
//				return realm;
//			}
//		}
		
		return null;
	}

	public SourceType createSourceType(String identifier, String accessPermission, EntityLinkStrategy entityLinkStrategy) {
		
		SourceType sourceType = new SourceType(identifier, accessPermission, entityLinkStrategy); 
		dao.addSourceType(sourceType);
		return dao.getSourceType(identifier);
	}
	
	/**
	 * @param sakaiId
	 * @return
	 */
	public Person getOrCreatePerson(String sakaiId) {
		Person person = dao.getPersonBySakaiId(sakaiId);
		if(person == null) {
			User userObj = this.sakaiProxy.getUser(sakaiId);
			person = new Person(sakaiId, userObj.getEid());
			dao.addPerson(person);
			person = dao.getPersonBySakaiId(sakaiId);
		}
		return person;
	}
	
	public List<AvailabilityCheck> getAvailabilityChecksBeforeTime(Date time) {
		
		return dao.getAvailabilityChecksBeforeTime(time);
	}

	public CalendarItem getCalendarItem(long id) {
		
		return dao.getCalendarItem(id);
	}
	
	public CalendarItem getCalendarItem(String entityReference, String calendarTimeLabelKey, Integer sequenceNumber) {
		
		return dao.getCalendarItem(entityReference, calendarTimeLabelKey, sequenceNumber);
	}

	public List<CalendarItem> getCalendarItems(String sakaiUserId,
			String contextId, boolean showFuture, boolean showPast, boolean saved, boolean hidden) {
		if(showFuture) {
			return dao.getFutureCalendarItems(sakaiUserId, contextId);
		} else if(showPast) {
			return dao.getPastCalendarItems(sakaiUserId, contextId);
		}
		return dao.getCalendarItems(sakaiUserId, contextId, saved, hidden);
	}

	public List<CalendarItem> getCalendarItems(String sakaiUserId, boolean showFuture, boolean showPast, boolean saved, boolean hidden) {
		
		return this.getCalendarItems(sakaiUserId, null, showFuture, showPast, saved, hidden);
	}
	
	public CalendarLink getCalendarLink(Long id) {
		return dao.getCalendarLink(id);
	}
	
	public List<CalendarLink> getFutureCalendarLinks(String sakaiUserId, String contextId, boolean hidden) {
		return dao.getFutureCalendarLinks(sakaiUserId, contextId, hidden);
	}

	public List<CalendarLink> getPastCalendarLinks(String sakaiUserId, String contextId, boolean hidden) {
		return dao.getPastCalendarLinks(sakaiUserId, contextId, hidden);
	}

	public List<CalendarLink> getStarredCalendarLinks(String sakaiUserId, String contextId) {
		return dao.getStarredCalendarLinks(sakaiUserId, contextId);
	}
	


	public Context getContext(String contextId) {
		try {
			return dao.getContext(contextId);
			
		} catch(Exception e) {
			logger.debug("No context retrieved for contextId: " + contextId);
		}
		
		return null;
	}

	public NewsItem getNewsItem(long id) {
		
		return dao.getNewsItem(id);
	}

	public NewsItem getNewsItem(String entityReference) {
		
		return dao.getNewsItem(entityReference);
	}
	
	public List<NewsItem> getNewsItems(String sakaiUserId, String contextId, int collapseCount) {
		List<NewsItem> items = dao.getNewsItems(sakaiUserId, contextId, collapseCount);
		if(items != null) {
			for(NewsItem item : items) {
				if(item != null && item.getItemCount() > 1) {
					int itemCount = item.getItemCount();
					SourceType sourceType = item.getSourceType();
					if(sourceType != null) {
						EntityType typeObj = this.entityTypes.get(sourceType.getIdentifier());
						if(typeObj != null) {
							item.setTitle(typeObj.getGroupTitle(itemCount, item.getContext().getContextTitle()));
						}
					}
				}
			}
		}
		return items;
	}

	public List<NewsItem> getNewsItems(String sakaiUserId, String contextId, boolean saved, boolean hidden) {
		
		return dao.getNewsItems(sakaiUserId, contextId, saved, hidden);
	}

	public List<NewsItem> getNewsItems(String sakaiUserId, boolean saved, boolean hidden) {
		
		return dao.getNewsItems(sakaiUserId, null, saved, hidden);
	}
	
	public List<NewsItem> getNewsItemsByGroupId(String sakaiUserId,
			String groupId, int pageSize, int pageNumber) {
		return dao.getNewsItemsByGroupId(sakaiUserId, groupId, pageSize, pageNumber);
	}
	
	public NewsLink getNewsLink(Long id) {
		return dao.getNewsLink(id);
	}

	public List<NewsLink> getCurrentNewsLinks(String sakaiId, String siteId) {
		return dao.getCurrentNewsLinks(sakaiId, siteId);
	}

	public List<NewsLink> getStarredNewsLinks(String sakaiId, String siteId) {
		return dao.getStarredNewsLinks(sakaiId, siteId);
	}

	public List<NewsLink> getHiddenNewsLinks(String sakaiId, String siteId) {
		return dao.getHiddenNewsLinks(sakaiId, siteId);
	}

	public RepeatingCalendarItem getRepeatingCalendarItem(String entityReference, String calendarTimeLabelKey) {
		return dao.getRepeatingCalendarItem(entityReference, calendarTimeLabelKey);
	}

	public SourceType getSourceType(String identifier) {
		try {
			return dao.getSourceType(identifier);
		} catch(Exception e) {
			logger.debug("No context retrieved for identifier: " + identifier);
		}
		
		return null ;
	}

	public Map<String, Object> getEntityMapping(String entityType, String entityReference, Locale locale) {
		Map<String, Object> map = new HashMap<String, Object>();
		logger.info("getEntityMapping(" + entityType + "," + entityReference + "," + locale + ")");

		EntityType entityTypeDef = this.entityTypes.get(entityType);
		if(entityTypeDef != null) {
			logger.info("getEntityMapping(" + entityType + "," + entityReference + "," + locale + ") " + entityTypeDef);
			map.putAll(entityTypeDef.getValues(entityReference, locale.toString()));
			map.putAll(entityTypeDef.getProperties(entityReference, locale.toString()));
			map.put(EntityType.VALUES_ORDER, entityTypeDef.getOrder(entityReference, locale.toString()));
		}
		
		return map;
	}

//	public Date getReleaseDate(String entityReference, String entityTypeId) {
//		Date date = null;
//		if(entityReference == null || entityTypeId == null) {
//			logger.warn("getReleaseDate() invoked with null parameter: " + entityReference + " :: " + entityTypeId);
//		} else {
//			EntityType entityType = this.entityTypes.get(entityTypeId);
//			if(entityType == null) {
//				logger.warn("getReleaseDate() invalid entityTypeId: " + entityTypeId);
//			} else {
//				date = entityType.getReleaseDate(entityReference);
//			}
//		}
//		return date;
//	}
//	
//	public Date getRetractDate(String entityReference, String entityTypeId) {
//		Date date = null;
//		if(entityReference == null || entityTypeId == null) {
//			logger.warn("getRetractDate() invoked with null parameter: " + entityReference + " :: " + entityTypeId);
//		} else {
//			EntityType entityType = this.entityTypes.get(entityTypeId);
//			if(entityType == null) {
//				logger.warn("getRetractDate() invalid entityTypeId: " + entityTypeId);
//			} else {
//				date = entityType.getRetractDate(entityReference);
//			}
//		}
//		return date;
//	}

	public String getString(String key, String dflt, String entityTypeId) {
		if(dflt == null) {
			dflt = "";
		}
		String str = dflt;
		if(key == null || entityTypeId == null) {
			logger.warn("getString() invoked with null parameter: " + key + " :: " + entityTypeId);
		} else {
			EntityType entityType = this.entityTypes.get(entityTypeId);
			if(entityType == null) {
				logger.warn("getString() invalid entityTypeId: " + entityTypeId);
			} else {
				str = entityType.getString(key, dflt);
			}
		}
		return str;
	}

	public boolean isAvailable(String entityReference, String entityTypeId) {
		// assume entity is unavailable unless entityType callback says otherwise
		boolean isAvailable = false;
		if(entityReference == null || entityTypeId == null) {
			logger.warn("isAvailable() invoked with null parameter: " + entityReference + " :: " + entityTypeId);
		} else {
			EntityType entityType = this.entityTypes.get(entityTypeId);
			if(entityType == null) {
				logger.warn("isAvailable() invalid entityTypeId: " + entityTypeId);
			} else {
				isAvailable = entityType.isAvailable(entityReference);
			}
		}
		return isAvailable;
	}
	
	public void registerEntityType(EntityType entityType) {
		if(entityType != null && entityType.getIdentifier() != null) {
			this.entityTypes.put(entityType.getIdentifier(), entityType);
		}
	}

	public void registerEventProcessor(EventProcessor eventProcessor) {
		
		if(eventProcessor != null && eventProcessor.getEventIdentifer() != null) {
			this.eventProcessors.put(eventProcessor.getEventIdentifer(), eventProcessor);
		}
		
	}
	
	public void removeAvailabilityChecksBeforeTime(Date time) {
		
		dao.deleteAvailabilityChecksBeforeTime(time);
		
	}

	public void removeCalendarItem(String entityReference,
			String calendarTimeLabelKey, Integer sequenceNumber) {
		
		if(logger.isDebugEnabled()) {
			logger.debug("removing calendar links and calendar item for " + entityReference + " " + calendarTimeLabelKey + " " + sequenceNumber);
		}
		CalendarItem item = dao.getCalendarItem(entityReference, calendarTimeLabelKey, sequenceNumber);
		if(item != null ) {
			if(logger.isDebugEnabled()) {
				logger.debug("removing calendar links and calendar item for item: " + item);
			}
			
			if(logger.isDebugEnabled()) {
				logger.debug("removing calendar links for item: " + item);
			}
			dao.deleteCalendarLinks(item.getId());
			if(logger.isDebugEnabled()) {
				logger.debug("removing calendar item: " + item);
			}
			dao.deleteCalendarItem(item.getId());
		}
		
	}

	public void removeCalendarItems(String entityReference) {
		
		if(logger.isDebugEnabled()) {
			logger.debug("removing calendar links and calendar item for " + entityReference);
		}
		List<CalendarItem> items = dao.getCalendarItems(entityReference);
		if(items != null && items.size() > 0) {
			for(CalendarItem item : items) {
			if(logger.isDebugEnabled()) {
				logger.debug("removing calendar links and calendar item for item: " + item);
			}
			
				if(logger.isDebugEnabled()) {
					logger.debug("removing calendar links for item: " + item);
				}
				dao.deleteCalendarLinks(item.getId());
				if(logger.isDebugEnabled()) {
					logger.debug("removing calendar item: " + item);
				}
				dao.deleteCalendarItem(item.getId());
			}
		}
	}

	public void removeNewsItem(String entityReference) {

		if(logger.isDebugEnabled()) {
			logger.debug("removing news links and news item for " + entityReference);
		}
		NewsItem item = dao.getNewsItem(entityReference);
		if(logger.isDebugEnabled()) {
			logger.debug("removing news links and news item for item: " + item);
		}
		if(item != null) {
			if(logger.isDebugEnabled()) {
				logger.debug("removing news links for item: " + item);
			}
			dao.deleteNewsLinks(item.getId());
			if(logger.isDebugEnabled()) {
				logger.debug("removing news item: " + item);
			}
			dao.deleteNewsItem(item.getId());
		}
		
		
	}

	public void removeCalendarLinks(String entityReference) {
		
		List<CalendarItem> items = dao.getCalendarItems(entityReference);
		if(items != null && items.size() > 0) {
			for(CalendarItem item : items) {
			dao.deleteCalendarLinks(item.getId());
		}
	}
	}

	public void removeCalendarLinks(String sakaiUserId, String contextId) {
		if(logger.isDebugEnabled()) {
			logger.info("removeCalendarLinks(" + sakaiUserId + "," + contextId + ") ");
		}
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		if(person != null) {
			Context context = dao.getContext(contextId);
			if(context != null) {
				dao.deleteCalendarLinks(person.getId(), context.getId());
			}
		}
	}

	public void removeNewsLinks(String entityReference) {
		
		NewsItem item = dao.getNewsItem(entityReference);
		if(item != null) {
			dao.deleteNewsLinks(item.getId());
		}
	}

	public void removeNewsLinks(String sakaiUserId, String contextId) {
		if(logger.isDebugEnabled()) {
			logger.info("removeNewsLinks(" + sakaiUserId + "," + contextId + ") ");
		}
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		if(person != null) {
			Context context = dao.getContext(contextId);
			if(context != null) {
				dao.deleteNewsLinks(person.getId(), context.getId());
			}
		}
	}

	public void removeAllScheduledAvailabilityChecks(String entityReference) {
		boolean removed = dao.deleteAvailabilityChecks(entityReference);
		
	}

	public void reviseCalendarItems(String entityReference, String newTitle, Date newTime) {
		
		List<CalendarItem> items = dao.getCalendarItems(entityReference);
		if(items != null) {
			for(CalendarItem item : items) {
				dao.updateCalendarItem(item.getId(), newTitle, newTime);
			}
		}
				
	}
	
	public void reviseCalendarItemsTime(String entityReference, Date newTime) {
		
		List<CalendarItem> items = dao.getCalendarItems(entityReference);
		if(items != null) {
			for(CalendarItem item : items) {
				dao.updateCalendarItemTime(item.getId(), newTime);
			}
		}
				
	}

	public void reviseCalendarItemsTitle(String entityReference, String newTitle) {
		
		List<CalendarItem> items = dao.getCalendarItems(entityReference);
		if(items != null) {
			for(CalendarItem item : items) {
				dao.updateCalendarItemTitle(item.getId(), newTitle);
			}
		}
	}

	public void reviseCalendarItemsSubtype(String entityReference, String labelKey, Integer sequenceNumber, String newSubtype) {
		
		CalendarItem item = dao.getCalendarItem(entityReference, labelKey, sequenceNumber);
		dao.updateCalendarItemSubtype(item.getId(), newSubtype);
	}

	public void reviseCalendarItemsLabelKey(String entityReference, String oldLabelKey, String newLabelKey) {
		if(entityReference == null || oldLabelKey == null || newLabelKey == null) {
			return;
		}
		
		dao.updateCalendarItemsLabelKey(entityReference, oldLabelKey, newLabelKey);
	}
	
	public void reviseContextTitle(String contextId, String newContextTitle) {
		boolean updated = dao.updateContextTitle(contextId, newContextTitle);
		
	}
	
	public void reviseNewsItemTime(String entityReference, Date newTime) {
		NewsItem item = dao.getNewsItem(entityReference);
		dao.updateNewsItemTime(item.getId(), newTime);
	}

	public void reviseNewsItemTitle(String entityReference, String newTitle) {
		
		NewsItem item = dao.getNewsItem(entityReference);
		if(item != null) {
			dao.updateNewsItemTitle(item.getId(), newTitle);
		}
		
	}
	
	public void reviseNewsItemLabelKey(String entityReference, String newLabelKey) {
		
		NewsItem item = dao.getNewsItem(entityReference);
		if(item != null) {
			dao.updateNewsItemLabelKey(item.getId(), newLabelKey);
		}
		
	}
	
	public void reviseNewsItemSubtype(String entityReference, String newSubtype) {
		
		NewsItem item = dao.getNewsItem(entityReference);
		if(item != null) {
			dao.updateNewsItemSubtype(item.getId(), newSubtype);
		}
		
	}
	
	public void reviseRepeatingCalendarItemsLabelKey(String entityReference, String oldLabelKey, String newLabelKey) {
		dao.updateRepeatingCalendarItemsLabelKey(entityReference, oldLabelKey, newLabelKey);
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#reviseRepeatingCalendarItemSubtype(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void reviseRepeatingCalendarItemSubtype(String entityReference, String labelKey, String newSubtype) {
		dao.updateRepeatingCalendarItemsSubtype(entityReference, labelKey, newSubtype);
	}

	public void scheduleAvailabilityCheck(String entityReference, String entityTypeId, Date scheduledTime) {
		AvailabilityCheck availabilityCheck = new AvailabilityCheck(entityReference, entityTypeId, scheduledTime);
		boolean added = dao.addAvailabilityCheck(availabilityCheck);
	}

	public void updateCalendarLinks(String entityReference) {
		List<CalendarItem> items = dao.getCalendarItems(entityReference);
		if(items != null && items.size() > 0) {
			CalendarItem firstItem = items.get(0);
			Set<String> oldUserSet = dao.getSakaIdsForUserWithCalendarLinks(entityReference);
			Set<String> newUserSet = new TreeSet<String>(this.sakaiProxy.getUsersWithReadAccess(entityReference, firstItem.getSourceType().getAccessPermission()));
			
			Set<String> removeSet = new TreeSet(oldUserSet);
			removeSet.removeAll(newUserSet);
			Set<String> addSet = new TreeSet(newUserSet);
			addSet.removeAll(oldUserSet);

			if(logger.isDebugEnabled()) {
				logger.debug("oldUserSet.size == " + oldUserSet.size());
				logger.debug("newUserSet.size == " + newUserSet.size());
				logger.debug("removeSet.size == " + removeSet.size());
				logger.debug("addSet.size == " + addSet.size());
			}
			
			for(String sakaiUserId : removeSet) {
				Person person = dao.getPersonBySakaiId(sakaiUserId);
				if(person != null) {
					for(CalendarItem item : items) {
						dao.deleteCalendarLink(person.getId(), item.getId());
					}
				}
			}
			
			for(String sakaiUserId : addSet) {
				Person person = dao.getPersonBySakaiId(sakaiUserId);
				if(person != null) {
					for(CalendarItem item : items) {
						CalendarLink link = new CalendarLink(person, item, item.getContext(),false, false);
						dao.addCalendarLink(link);
					}
				}
			}
		}
	}
	
	public void updateNewsLinks(String entityReference) {
		NewsItem item = dao.getNewsItem(entityReference);
		if(item == null) {
			
		} else {
			Set<String> oldUserSet = dao.getSakaiIdsForUserWithNewsLinks(entityReference);
			Set<String> newUserSet = new TreeSet<String>(this.sakaiProxy.getUsersWithReadAccess(entityReference, item.getSourceType().getAccessPermission()));
			
			Set<String> removeSet = new TreeSet(oldUserSet);
			removeSet.removeAll(newUserSet);
			Set<String> addSet = new TreeSet(newUserSet);
			addSet.removeAll(oldUserSet);
			
			if(logger.isDebugEnabled()) {
				logger.debug("oldUserSet.size == " + oldUserSet.size());
				logger.debug("newUserSet.size == " + newUserSet.size());
				logger.debug("removeSet.size == " + removeSet.size());
				logger.debug("addSet.size == " + addSet.size());
			}
			
			for(String sakaiUserId : removeSet) {
				Person person = dao.getPersonBySakaiId(sakaiUserId);
				if(person != null) {
					logger.debug("Attempting to remove link for person: " + person);
					dao.deleteNewsLink(person.getId(), item.getId());
				}
			}
			
			for(String sakaiUserId : addSet) {
				Person person = dao.getPersonBySakaiId(sakaiUserId);
				if(person != null) {
					logger.debug("Attempting to add link for person: " + person);
					NewsLink link = new NewsLink(person, item, item.getContext(),false, false);
					dao.addNewsLink(link);
				}
			}
		}
	}
	
	/*
	 * 
	 */
	protected void handleAvailabilityChecks() {
		Date currentTime = new Date();
		if(currentTime.getTime() > nextTimeToQueryAvailabilityChecks ) {
			List<AvailabilityCheck> checks = getAvailabilityChecksBeforeTime(currentTime );
			nextTimeToQueryAvailabilityChecks = currentTime.getTime() + TIME_BETWEEN_AVAILABILITY_CHECKS;
			
			if(checks != null && ! checks.isEmpty()) {
				for(AvailabilityCheck check : checks) {
					EntityType entityType = entityTypes.get(check.getEntityTypeId());
					if(entityType == null) {
						logger.warn("Unable to process AvailabilityCheck because entityType is null " + check.toString());
					} else if(entityType.isAvailable(check.getEntityReference())) {
						// need to add links
						List<CalendarItem> calendarItems = dao.getCalendarItems(check.getEntityReference());
						for(CalendarItem calendarItem : calendarItems) {
							if(calendarItem != null) {
								createCalendarLinks(calendarItem);
							}
						}
						
						NewsItem newsItem = getNewsItem(check.getEntityReference());
						if(newsItem != null) {
							createNewsLinks(newsItem);
						}
					} else {
						// need to remove all links, if there are any
						this.removeCalendarLinks(check.getEntityReference());
						this.removeNewsLinks(check.getEntityReference());
					}
				}
				removeAvailabilityChecksBeforeTime(currentTime);
			}
		}
	}

	/************************************************************************
	 * init() and destroy()
	 ************************************************************************/

	public void init() {
		logger.info("init()");
		if (!sakaiProxy.isEventProcessingThreadDisabled())
		{
			this.eventProcessingThread.start();
			
			this.sakaiProxy.addLocalEventListener(this);
			
			this.horizon = new Date(System.currentTimeMillis() + daysToHorizon * ONE_DAY);
		}
	}
	
	public void destroy() {
		logger.info("destroy()");
		
		// shut down daemon once it's done processing events
		timeToQuit = true;
		
		
	}

	/************************************************************************
	 * Observer method
	 ************************************************************************/

	/**
	 * 
	 */
	public void update(Observable arg0, Object obj) {
		if(obj instanceof Event) {
			Event event = (Event) obj;
			if(this.eventProcessors.containsKey(event.getEvent())) {
				if(logger.isDebugEnabled()) {
					logger.debug("adding event to queue: " + event.getEvent());
				}
				this.eventQueue.add(new EventCopy(event));				
			}
		}
		
	}

	/************************************************************************
	 * Making copies of events
	 ************************************************************************/

	/**
	 * 
	 */
	public class EventCopy implements Event 
	{

		protected String context;
		protected String eventIdentifier;
		protected Date eventTime;
		protected boolean modify;
		protected int priority;
		protected String entityReference;
		protected String sessionId;
		protected String userId;
		
		public EventCopy(Event original) {
			super();
			this.context = original.getContext();
			this.eventIdentifier = original.getEvent();
			
			try {
				// this.eventTime = original.getEventTime();
				// the getEventTime() method did not exist before kernel 1.2
				// so we use reflection
				Method getEventTimeMethod = original.getClass().getMethod("getEventTime", null);
				this.eventTime = (Date) getEventTimeMethod.invoke(original, null);
			} catch (SecurityException e) {
				logger.warn("Exception trying to get event time: " + e);
			} catch (NoSuchMethodException e) {
				logger.warn("Exception trying to get event time: " + e);
			} catch (IllegalArgumentException e) {
				logger.warn("Exception trying to get event time: " + e);
			} catch (IllegalAccessException e) {
				logger.warn("Exception trying to get event time: " + e);
			} catch (InvocationTargetException e) {
				logger.warn("Exception trying to get event time: " + e);
			}
			if(this.eventTime == null) {
				// If we couldn't get eventTime from event, just use NOW.  That's close enough.
				this.eventTime = new Date();
			}
			
			
			this.modify = original.getModify();
			this.priority = original.getPriority();
			this.entityReference = original.getResource();
			this.sessionId = original.getSessionId();
			this.userId = original.getUserId();
			if(userId == null && sessionId != null) {
				userId = sakaiProxy.getCurrentUserId();
			}
		}
		
		public String getContext() {
			return context;
		}

		public String getEvent() {
			return eventIdentifier;
		}

		public Date getEventTime() {
			return eventTime;
		}

		public boolean getModify() {
			return modify;
		}

		public int getPriority() {
			return priority;
		}

		public String getResource() {
			return entityReference;
		}

		public String getSessionId() {
			return sessionId;
		}

		public String getUserId() {
			return userId;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("EventCopy [context=");
			builder.append(context);
			builder.append(", eventIdentifier=");
			builder.append(eventIdentifier);
			builder.append(", eventTime=");
			builder.append(eventTime);
			builder.append(", modify=");
			builder.append(modify);
			builder.append(", priority=");
			builder.append(priority);
			builder.append(", entityReference=");
			builder.append(entityReference);
			builder.append(", sessionId=");
			builder.append(sessionId);
			builder.append(", userId=");
			builder.append(userId);
			builder.append("]");
			return builder.toString();
		}
		
	}
	
	/************************************************************************
	 * Event processing daemon (or thread?)
	 ************************************************************************/
	
	/**
	 * 
	 */
	public class DashboardEventProcessingThread extends Thread
	{
		private long sleepTime = 2L;

		public DashboardEventProcessingThread() {
			super("Dashboard Event Processing Thread");
			logger.info("Created Dashboard Event Processing Thread");
		}

		public void run() {
			logger.info("Started Dashboard Event Processing Thread");
			boolean timeToHandleAvailabilityChecks = true;
			sakaiProxy.startAdminSession();
			while(! timeToQuit) {
				if(logger.isDebugEnabled()) {
					logger.debug("Dashboard Event Processing Thread checking event queue: " + eventQueue.size());
				}
				if(eventQueue.isEmpty()) {
					if(timeToHandleAvailabilityChecks) {
						handleAvailabilityChecks();
						timeToHandleAvailabilityChecks = false;
					} else {
						updateRepeatingEvents();
						timeToHandleAvailabilityChecks = true;
					}
					try {
						Thread.sleep(sleepTime * 1000L);
					} catch (InterruptedException e) {
						logger.warn("InterruptedException in Dashboard Event Processing Thread: " + e);
					}
				} else {
					EventCopy event = eventQueue.poll();
					if(logger.isDebugEnabled()) {
						logger.debug("Dashboard Event Processing Thread is processing event: " + event.getEvent());
					}
					EventProcessor eventProcessor = eventProcessors.get(event.getEvent());
					
					SecurityAdvisor advisor = new DashboardLogicSecurityAdvisor(event.getResource());
					sakaiProxy.pushSecurityAdvisor(advisor );
					try {
						eventProcessor.processEvent(event);
					} catch (Exception e) {
						logger.warn("Error processing event: " + event, e);
					} finally {
						sakaiProxy.popSecurityAdvisor(advisor);
					}
				}
			}
		}

		protected void updateRepeatingEvents() {
			
			if(nextHorizonUpdate != null && System.currentTimeMillis() > nextHorizonUpdate.getTime()) {
				// time to update
				Date oldHorizon = horizon;
				horizon = new Date(System.currentTimeMillis() + daysToHorizon * ONE_DAY);
				
				if(horizon.after(oldHorizon)) {
					List<RepeatingCalendarItem> repeatingEvents = dao.getRepeatingCalendarItems();
					if(repeatingEvents != null) {
						for(RepeatingCalendarItem repeatingEvent: repeatingEvents) {
							addCalendarItemsForRepeatingCalendarItem(repeatingEvent, oldHorizon, horizon);

						}
					}
				}
				nextHorizonUpdate = new Date(nextHorizonUpdate.getTime() + daysBetweenHorizonUpdates * ONE_DAY);
			}
		}

	}
	
	public class DashboardLogicSecurityAdvisor implements SecurityAdvisor 
	{
		protected String entityReference;
		
		/**
		 * @param entityReference
		 */
		public DashboardLogicSecurityAdvisor(String entityReference) {
			super();
			this.entityReference = entityReference;
		}

		/*
		 * (non-Javadoc)
		 * @see org.sakaiproject.authz.api.SecurityAdvisor#isAllowed(java.lang.String, java.lang.String, java.lang.String)
		 */
		public SecurityAdvice isAllowed(String userId, String function,
				String reference) {
			if(reference != null && reference.equalsIgnoreCase(entityReference)) {
				return SecurityAdvice.ALLOWED;
			}
			return SecurityAdvice.NOT_ALLOWED;
		}
		
	}

	public boolean hideCalendarItem(String sakaiUserId, long calendarItemId) {
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		CalendarLink link = dao.getCalendarLink(calendarItemId, person.getId().longValue());
		link.setHidden(true);
		return dao.updateCalendarLink(link);
	}

	public boolean hideNewsItem(String sakaiUserId, long newsItemId) {
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		NewsLink link = dao.getNewsLink(newsItemId, person.getId().longValue());
		link.setHidden(true);
		return dao.updateNewsLink(link);
	}

	public boolean keepCalendarItem(String sakaiUserId, long calendarItemId) {
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		CalendarLink link = dao.getCalendarLink(calendarItemId, person.getId().longValue());
		link.setSticky(true);
		return dao.updateCalendarLink(link);
	}

	public boolean keepNewsItem(String sakaiUserId, long newsItemId) {
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		NewsLink link = dao.getNewsLink(newsItemId, person.getId().longValue());
		link.setSticky(true);
		return dao.updateNewsLink(link);
	}

	public boolean unhideCalendarItem(String sakaiUserId, long calendarItemId) {
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		CalendarLink link = dao.getCalendarLink(calendarItemId, person.getId().longValue());
		link.setHidden(false);
		return dao.updateCalendarLink(link);
	}

	public boolean unhideNewsItem(String sakaiUserId, long newsItemId) {
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		NewsLink link = dao.getNewsLink(newsItemId, person.getId().longValue());
		link.setHidden(false);
		return dao.updateNewsLink(link);
	}

	public boolean unkeepCalendarItem(String sakaiUserId, long calendarItemId) {
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		CalendarLink link = dao.getCalendarLink(calendarItemId, person.getId().longValue());
		link.setSticky(false);
		return dao.updateCalendarLink(link);
	}

	public boolean unkeepNewsItem(String sakaiUserId, long newsItemId) {
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		NewsLink link = dao.getNewsLink(newsItemId, person.getId().longValue());
		link.setSticky(false);
		return dao.updateNewsLink(link);
	}

	public boolean hideCalendarItemsByContext(String sakaiUserId,
			long contextId) {
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		Context context = dao.getContext(contextId);
		PersonContext personContext = new PersonContext(ItemType.CALENDAR_ITEM, person, context);
		return dao.addPersonContext(personContext);
	}

	public boolean hideCalendarItemsByContextSourceType(String sakaiUserId,
			long contextId, long sourceTypeId) {
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		Context context = dao.getContext(contextId);
		SourceType sourceType = dao.getSourceType(sourceTypeId);
		PersonContextSourceType personContextSourceType = new PersonContextSourceType(ItemType.CALENDAR_ITEM, person, context, sourceType);
		return dao.addPersonContextSourceType(personContextSourceType);
	}

	public boolean hideCalendarItemsBySourceType(String sakaiUserId,
			long sourceTypeId) {
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		SourceType sourceType = dao.getSourceType(sourceTypeId);
		PersonSourceType personSourceType = new PersonSourceType(ItemType.CALENDAR_ITEM, person, sourceType);
		return dao.addPersonSourceType(personSourceType);
	}

	public boolean hideNewsItemsByContext(String sakaiUserId,
			long contextId) {
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		Context context = dao.getContext(contextId);
		PersonContext personContext = new PersonContext(ItemType.NEWS_ITEM, person, context);
		return dao.addPersonContext(personContext);
	}

	public boolean hideNewsItemsByContextSourceType(String sakaiUserId,
			long contextId, long sourceTypeId) {
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		Context context = dao.getContext(contextId);
		SourceType sourceType = dao.getSourceType(sourceTypeId);
		PersonContextSourceType personContextSourceType = new PersonContextSourceType(ItemType.NEWS_ITEM, person, context, sourceType);
		return dao.addPersonContextSourceType(personContextSourceType);
	}

	public boolean hideNewsItemsBySourceType(String sakaiUserId,
			long sourceTypeId) {
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		SourceType sourceType = dao.getSourceType(sourceTypeId);
		PersonSourceType personSourceType = new PersonSourceType(ItemType.NEWS_ITEM, person, sourceType);
		return dao.addPersonSourceType(personSourceType);
	}

	public String getEntityIconUrl(String type, String subtype) {
		String url = "#"; 
		if(type != null) {
			EntityType typeObj = this.entityTypes.get(type);
			if(typeObj != null) {
				url = typeObj.getIconUrl(subtype);
			}
		}
		return url;
	}

}
