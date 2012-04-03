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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.sf.ehcache.Cache;

import org.apache.log4j.Logger;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.dash.dao.DashboardDao;
import org.sakaiproject.dash.entity.EntityType;
import org.sakaiproject.dash.entity.RepeatingEventGenerator;
import org.sakaiproject.dash.listener.EventProcessor;
import org.sakaiproject.dash.model.AvailabilityCheck;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.CalendarLink;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.NewsLink;
import org.sakaiproject.dash.model.Person;
import org.sakaiproject.dash.model.RepeatingCalendarItem;
import org.sakaiproject.dash.model.SourceType;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;

/**
 * 
 *
 */
/**
 * @author jimeng
 *
 */
public class DashboardLogicImpl implements DashboardLogic, Observer 
{
	private static Logger logger = Logger.getLogger(DashboardLogicImpl.class);
	
	public static final String DASHBOARD_NEGOTIATE_AVAILABILITY_CHECKS = "dash.negotiate.availCheck";
	public static final String DASHBOARD_NEGOTIATE_REPEAT_EVENTS = "dash.negotiate.repeatEvents";
	public static final String DASHBOARD_NEGOTIATE_EXPIRATION_AND_PURGING = "dash.negotiate.expireAndPurge";
	
	/** time to allow negotaion among servers -- 2 minutes */
	public static final long NEGOTIATING_TIME = 1000L * 60L * 2L;
	
	public static final long TIME_BETWEEN_AVAILABILITY_CHECKS = 1000L * 60L * 1L;  // one minute
	public static final long TIME_BETWEEN_EXPIRING_AND_PURGING = 1000L * 60L * 60L; // one hour

	private static final long ONE_DAY = 1000L * 60L * 60L * 24L;
	private static final long ONE_YEAR = ONE_DAY * 365L;
	
	protected Date nextHorizonUpdate = new Date();
	private Date horizon = new Date();
		
	protected long nextTimeToQueryAvailabilityChecks = System.currentTimeMillis();
	protected long nextTimeToExpireAndPurge = System.currentTimeMillis();
	
	protected Map<String,EntityType> entityTypes = new HashMap<String,EntityType>();
	protected Map<String,EventProcessor> eventProcessors = new HashMap<String,EventProcessor>();
	
	protected DashboardEventProcessingThread eventProcessingThread = new DashboardEventProcessingThread();
	protected Queue<EventCopy> eventQueue = new ConcurrentLinkedQueue<EventCopy>();
	protected Object eventQueueLock = new Object();
	
	protected static long dashboardEventProcessorThreadId = 0L;

	protected String serverId = null;
	protected String serverHandlingAvailabilityChecks = "";
	protected String serverHandlingRepeatEvents = "";
	protected String serverHandlingExpirationAndPurging = "";

	protected boolean handlingAvailabilityChecks = false;
	protected boolean notHandlingAvailabilityChecks = false;
	protected boolean handlingRepeatedEvents = false;
	protected boolean notHandlingRepeatedEvents = false;
	protected boolean handlingExpirationAndPurging = false;
	protected boolean notHandlingExpirationAndPurging = false;
	
	protected Date claimAvailabilityCheckDutyTime = null;
	protected Date claimRepeatEventsDutyTime = null;
	protected Date claimExpirationAndPurgingTime = null;
	
	protected static final Integer DEFAULT_NEWS_ITEM_EXPIRATION = new Integer(26);
	protected static final Integer DEFAULT_CALENDAR_ITEM_EXPIRATION = new Integer(2);

	public static final Set<String> NAVIGATION_EVENTS = new HashSet<String>();
	public static final Set<String> DASH_NAV_EVENTS = new HashSet<String>();
	public static final Set<String> ITEM_DETAIL_EVENTS = new HashSet<String>();
	public static final Set<String> PREFERENCE_EVENTS = new HashSet<String>();

	public static final int LOG_MODE_NONE = 0;
	public static final int LOG_MODE_SAVE = 1;
	public static final int LOG_MODE_POST = 2;
	public static final int LOG_MODE_SAVE_AND_POST = 3;

	static {
		NAVIGATION_EVENTS.add(EVENT_DASH_VISIT);
		NAVIGATION_EVENTS.add(EVENT_DASH_FOLLOW_TOOL_LINK);
		NAVIGATION_EVENTS.add(EVENT_DASH_FOLLOW_SITE_LINK);
		NAVIGATION_EVENTS.add(EVENT_DASH_ACCESS_URL);
		NAVIGATION_EVENTS.add(EVENT_VIEW_ATTACHMENT);
		
		DASH_NAV_EVENTS.add(EVENT_DASH_TABBING);
		DASH_NAV_EVENTS.add(EVENT_DASH_PAGING);		
		
		ITEM_DETAIL_EVENTS.add(EVENT_DASH_ITEM_DETAILS);
		ITEM_DETAIL_EVENTS.add(EVENT_DASH_VIEW_GROUP);
		
		PREFERENCE_EVENTS.add(EVENT_DASH_STAR);
		PREFERENCE_EVENTS.add(EVENT_DASH_UNSTAR);
		PREFERENCE_EVENTS.add(EVENT_DASH_HIDE);
		PREFERENCE_EVENTS.add(EVENT_DASH_SHOW);
		PREFERENCE_EVENTS.add(EVENT_DASH_HIDE_MOTD);
	}	
	
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
	
	protected DashboardConfig dashboardConfig;
	public void setDashboardConfig(DashboardConfig dashboardConfig) {
		this.dashboardConfig = dashboardConfig;
	}

	protected Cache cache;

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
				// TODO: handle error: null parameters?
				logger.warn("TODO: handle error: null parameters?");
			} else {
				EntityType entityType = entityTypes.get(repeatingEvent.getSourceType().getIdentifier());
				if(entityType == null) {
					// TODO: handle error: entityType cannot be null
					logger.warn("TODO: handle error: entityType cannot be null");
				} else if(entityType instanceof RepeatingEventGenerator) {
					
					List<CalendarItem> oldDates = dao.getCalendarItems(repeatingEvent);
					Map<Date, CalendarItem> oldDatesMap = new HashMap<Date, CalendarItem>();
					for(CalendarItem cItem: oldDates) {
						if(cItem.getSequenceNumber() == null || cItem.getCalendarTime() == null) {
							logger.warn("addCalendarItemsForRepeatingCalendarItem() -- Deleting bogus CalendarItem and all links to it: " + cItem);
							dao.deleteCalendarLinks(cItem.getId());
							dao.deleteCalendarItem(cItem.getId());
						} else {
							oldDatesMap.put(cItem.getCalendarTime(), cItem);
						}
					}
					Map<Integer, Date> newDates = ((RepeatingEventGenerator) entityType).generateRepeatingEventDates(repeatingEvent.getEntityReference(), beginDate, endDate);
					if(newDates == null) {
						// ignore: there are no new dates to add at this time
					} else {
						for(Map.Entry<Integer, Date> entry : newDates.entrySet()) {
							try {
								if(oldDatesMap.containsKey(entry.getValue())) {
									verifyCalendarItem(oldDatesMap.get(entry.getValue()), repeatingEvent, entry.getKey(), entry.getValue());
								} else {
									// create an instance
									CalendarItem calendarItem = createCalendarItem(repeatingEvent.getTitle(), entry.getValue(), repeatingEvent.getCalendarTimeLabelKey(), 
											repeatingEvent.getEntityReference(), repeatingEvent.getContext(), repeatingEvent.getSourceType(), repeatingEvent.getSubtype(), repeatingEvent, entry.getKey());
									// dao.addCalendarItem(calendarItem);
									// calendarItem = dao.getCalendarItem(repeatingEvent.getEntityReference(), repeatingEvent.getCalendarTimeLabelKey(), entry.getKey());
									if(calendarItem == null) {
										// this could occur if we are trying to add an instance that has already been added
										StringBuilder buf = new StringBuilder();
										buf.append("Error trying to add calendar item for repeating event (");
										buf.append(repeatingEvent);
										buf.append(") for date (");
										buf.append(entry.getValue());
										buf.append(") and sequence number (");
										buf.append(entry.getKey());
										buf.append("). Calendar item is null for repeating event: ");
										buf.append(repeatingEvent);
										buf.append(" :: Sequence Number: ");
										buf.append(entry.getKey());
										buf.append(" Date: ");
										buf.append(entry.getValue());
										logger.warn(buf);
									} else {
										createCalendarLinks(calendarItem);
									}
								}
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
					logger.warn("TODO: handle error: entityType must be a RepeatingEventGenerator");
				}
			}
		} else {
			// TODO: handle error: invalid parameters? beginDate must be before endDate
			logger.warn("TODO: handle error: invalid parameters? beginDate must be before endDate");
		}
	}
	
	/**
	 * Verify that all properties of a CalendarItem correspond to the values of a RepeatingCalendarItem, 
	 * a sequenceNumber and a calendarTime.  Update any incorrect values in the database.  
	 * @param calendarItem
	 * @param repeatingEvent
	 * @param sequenceNumber
	 * @param calendarTime
	 * @return true if updates have been made, false otherwise.
	 */
	protected boolean verifyCalendarItem(CalendarItem calendarItem,
			RepeatingCalendarItem repeatingEvent, Integer sequenceNumber, Date calendarTime) {
		
		boolean saveChanges = false;
		if(sequenceNumber != null && sequenceNumber.equals(calendarItem.getSequenceNumber())) {
			// do nothing
		} else {
			calendarItem.setSequenceNumber(sequenceNumber);
			saveChanges = true;
		}
		if(repeatingEvent.getEntityReference() != null && repeatingEvent.getEntityReference().equals(calendarItem.getEntityReference())) {
			// do nothing
		} else {
			calendarItem.setEntityReference(repeatingEvent.getEntityReference());
			saveChanges = true;
		}
		if(calendarTime != null && calendarTime.equals(calendarItem.getCalendarTime())) {
			// do nothing
		} else {
			calendarItem.setCalendarTime(calendarTime);
			saveChanges = true;
		}
		if(repeatingEvent.getTitle() != null && repeatingEvent.getTitle().equals(calendarItem.getTitle())) {
			// do nothing
		} else {
			calendarItem.setTitle(repeatingEvent.getTitle());
			saveChanges = true;
		}
		if(repeatingEvent.getCalendarTimeLabelKey() != null && repeatingEvent.getCalendarTimeLabelKey().equals(calendarItem.getCalendarTimeLabelKey())) {
			// do nothing
		} else {
			calendarItem.setCalendarTimeLabelKey(repeatingEvent.getCalendarTimeLabelKey());
			saveChanges = true;
		}
		if(repeatingEvent.getContext() != null && repeatingEvent.getContext().equals(calendarItem.getContext())) {
			// do nothing
		} else {
			calendarItem.setContext(repeatingEvent.getContext());
			saveChanges = true;
		}
		if(repeatingEvent.getSourceType() != null && repeatingEvent.getSourceType().getIdentifier() != null 
				&& calendarItem.getSourceType() != null && calendarItem.getSourceType().getIdentifier() != null && repeatingEvent.getSourceType().getIdentifier().equals(calendarItem.getSourceType().getIdentifier()) ) {
			// do nothing
		} else {
			calendarItem.setSourceType(repeatingEvent.getSourceType());
			saveChanges = true;
		}
		if(repeatingEvent.getSubtype() != null && repeatingEvent.getSubtype().equals(calendarItem.getSubtype())) {
			// do nothing
		} else {
			calendarItem.setSubtype(repeatingEvent.getSubtype());
			saveChanges = true;
		}
		if(saveChanges) {
			dao.updateCalendarItem(calendarItem);
		}
		return saveChanges;
	}

	public void updateTimeOfRepeatingCalendarItem(RepeatingCalendarItem repeatingEvent, Date oldTime, Date newTime) {
		if(repeatingEvent == null) {
			logger.warn("updateTimeOfRepeatingCalendarItem() called with null parameter ");
		} else {
			EntityType entityType = entityTypes.get(repeatingEvent.getSourceType().getIdentifier());
			if(entityType == null) {
				// TODO: handle error: entityType cannot be null
				logger.warn("updateTimeOfRepeatingCalendarItem() handle error: entityType cannot be null");
			} else if(entityType instanceof RepeatingEventGenerator) {
				Date beginDate = repeatingEvent.getFirstTime();
				Date endDate = repeatingEvent.getLastTime();
				Map<Integer, Date> dates = ((RepeatingEventGenerator) entityType).generateRepeatingEventDates(repeatingEvent.getEntityReference(), beginDate, endDate);
				for(Map.Entry<Integer, Date> entry : dates.entrySet()) {
					
				}
				
			} else {
				// TODO: handle error: entityType cannot be null
				logger.warn("updateTimeOfRepeatingCalendarItem() handle error: entityType must be RepeatingEventGenerator");
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#addCalendarLinks(java.lang.String, java.lang.String)
	 */
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
				logger.warn(message.toString());
			} else {
				for(CalendarItem item: items) {
					SourceType sourceType = item.getSourceType();
					EntityType entityType = this.entityTypes.get(sourceType.getIdentifier());
					if(entityType != null && entityType.isUserPermitted(sakaiUserId, item.getEntityReference(), item.getContext().getContextId())) {
						CalendarLink calendarLink = new CalendarLink(person, item, item.getContext(), false, false);
						dao.addCalendarLink(calendarLink);
					}
				}
			}
		}
		
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#addNewsLinks(java.lang.String, java.lang.String)
	 */
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
				logger.warn(message.toString());
			} else {
				for(NewsItem item: items) {
					SourceType sourceType = item.getSourceType();
					EntityType entityType = this.entityTypes.get(sourceType.getIdentifier());
					if(entityType != null && entityType.isUserPermitted(sakaiUserId, item.getEntityReference(), item.getContext().getContextId()) ) {
						NewsLink newsLink = new NewsLink(person, item, item.getContext(), false, false);
						dao.addNewsLink(newsLink);
					}
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#createCalendarItem(java.lang.String, java.util.Date, java.lang.String, java.lang.String, org.sakaiproject.dash.model.Context, org.sakaiproject.dash.model.SourceType, java.lang.String, org.sakaiproject.dash.model.RepeatingCalendarItem, java.lang.Integer)
	 */
	public CalendarItem createCalendarItem(String title, Date calendarTime,
			String calendarTimeLabelKey, String entityReference, Context context,
			SourceType sourceType, String subtype, RepeatingCalendarItem repeatingCalendarItem, Integer sequenceNumber) {
		
		CalendarItem calendarItem = new CalendarItem(title, calendarTime,
				calendarTimeLabelKey, entityReference, context, sourceType, subtype, repeatingCalendarItem, sequenceNumber);
		
		CalendarItem rv = null;
		boolean success = dao.addCalendarItem(calendarItem);
		if(success) {
			rv = dao.getCalendarItem(entityReference, calendarTimeLabelKey, sequenceNumber);
		}
		
		return rv;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#createRepeatingCalendarItem(java.lang.String, java.util.Date, java.util.Date, java.lang.String, java.lang.String, org.sakaiproject.dash.model.Context, org.sakaiproject.dash.model.SourceType, java.lang.String, int)
	 */
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

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#createCalendarLinks(org.sakaiproject.dash.model.CalendarItem)
	 */
	public void createCalendarLinks(CalendarItem calendarItem) {
		if(logger.isDebugEnabled()) {
			logger.debug("createCalendarLinks(" + calendarItem + ")");
		}
		if(calendarItem != null) {
			EntityType entityType = this.entityTypes.get(calendarItem.getSourceType().getIdentifier());
			if(entityType != null) {
				Set<String> usersWithLinks = dao.listUsersWithLinks(calendarItem);
				
				List<String> sakaiIds = entityType.getUsersWithAccess(calendarItem.getEntityReference());
				for(String sakaiId : sakaiIds) {
					if(usersWithLinks.contains(sakaiId)) {
						// do nothing -- link already exists
					} else {
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
		}
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#createContext(java.lang.String)
	 */
	public Context createContext(String contextId) { 
		Context context = null;
		if (contextId.equals(MOTD_CONTEXT))
		{
			// add an exception for MOTD announcement, where the context id is "!site"
			context = new Context(contextId, "MOTD", this.sakaiProxy.getConfigParam("serverUr", "")+ "/access/content/public/MOTD%20files/");
		}
		else
		{
			// get the site id, title and url
			Site site = this.sakaiProxy.getSite(contextId);
			if(site != null) {
				context = new Context(site.getId(), site.getTitle(), site.getUrl());
			}
		}
		if (context != null)
		{
			dao.addContext(context);
		}
		context = dao.getContext(contextId);
		return context;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#createNewsItem(java.lang.String, java.util.Date, java.lang.String, java.lang.String, org.sakaiproject.dash.model.Context, org.sakaiproject.dash.model.SourceType, java.lang.String)
	 */
	public NewsItem createNewsItem(String title, Date newsTime,
			String labelKey, String entityReference, Context context, SourceType sourceType, String subtype) {
		
		NewsItem newsItem = new NewsItem(title, newsTime, 
				labelKey, entityReference, context, sourceType, subtype);
		
		dao.addNewsItem(newsItem);
		
		return dao.getNewsItem(entityReference) ;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#createNewsLinks(org.sakaiproject.dash.model.NewsItem)
	 */
	public void createNewsLinks(NewsItem newsItem) {
		if(logger.isDebugEnabled()) {
			logger.debug("createNewsLinks(" + newsItem + ")");
		}
		if(newsItem != null) {
			EntityType entityType = this.entityTypes.get(newsItem.getSourceType().getIdentifier());
			List<String> sakaiIds = entityType.getUsersWithAccess(newsItem.getEntityReference());
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

	public SourceType createSourceType(String identifier) {
		SourceType sourceType = dao.getSourceType(identifier);
		if(sourceType == null) {
			sourceType = new SourceType(identifier); 
			dao.addSourceType(sourceType);
			sourceType = dao.getSourceType(identifier);
		} 
		return sourceType;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getCalendarItem(long)
	 */
	public CalendarItem getCalendarItem(long id) {
		
		return dao.getCalendarItem(id);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getEntityIconUrl(java.lang.String, java.lang.String)
	 */
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

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getCalendarItem(java.lang.String, java.lang.String, java.lang.Integer)
	 */
	public CalendarItem getCalendarItem(String entityReference, String calendarTimeLabelKey, Integer sequenceNumber) {
		
		return dao.getCalendarItem(entityReference, calendarTimeLabelKey, sequenceNumber);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getCalendarLink(java.lang.Long)
	 */
	public CalendarLink getCalendarLink(Long id) {
		return dao.getCalendarLink(id);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getFutureCalendarLinks(java.lang.String, java.lang.String, boolean)
	 */
	public List<CalendarLink> getFutureCalendarLinks(String sakaiUserId, String contextId, boolean hidden) {
		return dao.getFutureCalendarLinks(sakaiUserId, contextId, hidden);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getPastCalendarLinks(java.lang.String, java.lang.String, boolean)
	 */
	public List<CalendarLink> getPastCalendarLinks(String sakaiUserId, String contextId, boolean hidden) {
		return dao.getPastCalendarLinks(sakaiUserId, contextId, hidden);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getStarredCalendarLinks(java.lang.String, java.lang.String)
	 */
	public List<CalendarLink> getStarredCalendarLinks(String sakaiUserId, String contextId) {
		return dao.getStarredCalendarLinks(sakaiUserId, contextId);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getContext(java.lang.String)
	 */
	public Context getContext(String contextId) {
		Context rv = null;
		try {
			rv = dao.getContext(contextId);
			
		} catch(Exception e) {
			logger.debug("No context retrieved for contextId: " + contextId);
		}
		
		if (rv == null)
		{
			// create context
			rv = createContext(contextId);
		}
		return rv;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getMOTD()
	 */
	public List<NewsItem> getMOTD() {
		return dao.getMOTD(MOTD_CONTEXT);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getNewsItem(long)
	 */
	public NewsItem getNewsItem(long id) {
		
		return dao.getNewsItem(id);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getNewsItem(java.lang.String)
	 */
	public NewsItem getNewsItem(String entityReference) {
		
		return dao.getNewsItem(entityReference);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#countNewsLinksByGroupId(java.lang.String, java.lang.String)
	 */
	public int countNewsLinksByGroupId(String sakaiUserId,
			String groupId) {
		return dao.countNewsLinksByGroupId(sakaiUserId,groupId);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getNewsLinksByGroupId(java.lang.String, java.lang.String, int, int)
	 */
	public List<NewsLink> getNewsLinksByGroupId(String sakaiUserId,
			String groupId, int limit, int offset) {
		return dao.getNewsLinksByGroupId(sakaiUserId, groupId, limit, offset);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getCurrentNewsLinks(java.lang.String, java.lang.String)
	 */
	public List<NewsLink> getCurrentNewsLinks(String sakaiId, String siteId) {
		List<NewsLink> links = dao.getCurrentNewsLinks(sakaiId, siteId);
		
		if(links != null) {
			for(NewsLink link : links) {
				NewsItem item = link.getNewsItem();
				if(item != null && item.getItemCount() > 1) {
					int itemCount = item.getItemCount();
					SourceType sourceType = item.getSourceType();
					if(sourceType != null) {
						EntityType typeObj = this.entityTypes.get(sourceType.getIdentifier());
						if(typeObj == null) {
							ResourceLoader rl = new ResourceLoader("dash_entity");
							Object[] args = new Object[]{itemCount, sourceType.getIdentifier(), item.getContext().getContextTitle()};
							rl.getFormattedMessage("dash.grouped.title", args );
						} else {
							item.setTitle(typeObj.getGroupTitle(itemCount, item.getContext().getContextTitle(), item.getNewsTimeLabelKey()));
						}
					}
				}
			}
		}
		
		return links;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getStarredNewsLinks(java.lang.String, java.lang.String)
	 */
	public List<NewsLink> getStarredNewsLinks(String sakaiId, String siteId) {
		return dao.getStarredNewsLinks(sakaiId, siteId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getHiddenNewsLinks(java.lang.String, java.lang.String)
	 */
	public List<NewsLink> getHiddenNewsLinks(String sakaiId, String siteId) {
		return dao.getHiddenNewsLinks(sakaiId, siteId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getRepeatingCalendarItem(java.lang.String, java.lang.String)
	 */
	public RepeatingCalendarItem getRepeatingCalendarItem(String entityReference, String calendarTimeLabelKey) {
		return dao.getRepeatingCalendarItem(entityReference, calendarTimeLabelKey);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getSourceType(java.lang.String)
	 */
	public SourceType getSourceType(String identifier) {
		SourceType  rv = null;
		try {
			rv = dao.getSourceType(identifier);
		} catch(Exception e) {
			logger.debug("No context retrieved for identifier: " + identifier);
		}
		
		if (rv == null)
		{
			// create SourceType
			rv = createSourceType(identifier);
		}
		
		return rv ;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getEntityMapping(java.lang.String, java.lang.String, java.util.Locale)
	 */
	public Map<String, Object> getEntityMapping(String entityType, String entityReference, Locale locale) {
		Map<String, Object> map = new HashMap<String, Object>();
		if(logger.isDebugEnabled()) {
			logger.debug("getEntityMapping(" + entityType + "," + entityReference + "," + locale + ")");
		}

		EntityType entityTypeDef = this.entityTypes.get(entityType);
		if(entityTypeDef != null) {
			if(logger.isDebugEnabled()) {
				logger.debug("getEntityMapping(" + entityType + "," + entityReference + "," + locale + ") " + entityTypeDef);
			}
			Map<String, Object> values = processFormattedText(entityTypeDef.getValues(entityReference, locale.toString()), 6);
			map.putAll(values);
			map.putAll(entityTypeDef.getProperties(entityReference, locale.toString()));
			map.put(EntityType.VALUES_ORDER, entityTypeDef.getOrder(entityReference, locale.toString()));
		}
		
		return map;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getFutureSequnceNumbers(java.lang.String, java.lang.String, java.lang.Integer)
	 */
	public SortedSet<Integer> getFutureSequnceNumbers(String entityReference,
			String calendarTimeLabelKey, Integer firstSequenceNumber) {
		return dao.getFutureSequenceNumbers(entityReference, calendarTimeLabelKey, firstSequenceNumber);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getRepeatingEventHorizon()
	 */
	public Date getRepeatingEventHorizon() {
		
		return new Date(this.horizon.getTime());
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getString(java.lang.String, java.lang.String, java.lang.String)
	 */
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
				str = entityType.getEventDisplayString(key, dflt);
			}
		}
		return str;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#hideCalendarItem(java.lang.String, long)
	 */
	public boolean hideCalendarItem(String sakaiUserId, long calendarItemId) {
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		CalendarLink link = dao.getCalendarLink(calendarItemId, person.getId().longValue());
		link.setHidden(true);
		return dao.updateCalendarLink(link);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#hideNewsItem(java.lang.String, long)
	 */
	public boolean hideNewsItem(String sakaiUserId, long newsItemId) {
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		NewsLink link = dao.getNewsLink(newsItemId, person.getId().longValue());
		link.setHidden(true);
		return dao.updateNewsLink(link);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#keepCalendarItem(java.lang.String, long)
	 */
	public boolean keepCalendarItem(String sakaiUserId, long calendarItemId) {
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		CalendarLink link = dao.getCalendarLink(calendarItemId, person.getId().longValue());
		link.setSticky(true);
		return dao.updateCalendarLink(link);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#keepNewsItem(java.lang.String, long)
	 */
	public boolean keepNewsItem(String sakaiUserId, long newsItemId) {
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		NewsLink link = dao.getNewsLink(newsItemId, person.getId().longValue());
		link.setSticky(true);
		return dao.updateNewsLink(link);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#unhideCalendarItem(java.lang.String, long)
	 */
	public boolean unhideCalendarItem(String sakaiUserId, long calendarItemId) {
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		CalendarLink link = dao.getCalendarLink(calendarItemId, person.getId().longValue());
		link.setHidden(false);
		return dao.updateCalendarLink(link);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#unhideNewsItem(java.lang.String, long)
	 */
	public boolean unhideNewsItem(String sakaiUserId, long newsItemId) {
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		NewsLink link = dao.getNewsLink(newsItemId, person.getId().longValue());
		link.setHidden(false);
		return dao.updateNewsLink(link);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#unkeepCalendarItem(java.lang.String, long)
	 */
	public boolean unkeepCalendarItem(String sakaiUserId, long calendarItemId) {
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		CalendarLink link = dao.getCalendarLink(calendarItemId, person.getId().longValue());
		link.setSticky(false);
		return dao.updateCalendarLink(link);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#unkeepNewsItem(java.lang.String, long)
	 */
	public boolean unkeepNewsItem(String sakaiUserId, long newsItemId) {
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		NewsLink link = dao.getNewsLink(newsItemId, person.getId().longValue());
		link.setSticky(false);
		return dao.updateNewsLink(link);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#isAvailable(java.lang.String, java.lang.String)
	 */
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

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#recordDashboardActivity(java.lang.String, java.lang.String)
	 */
	public void recordDashboardActivity(String event, String itemRef) {
		if(event == null) {
			// log error and return
			logger.warn("attempting to record dashboard activity with null event. itemRef == " + itemRef);
			return;
		} else if(itemRef == null) {
			// log error and return
			logger.warn("attempting to record dashboard activity with null itemRef. event == " + event);
			return;
		}
		
		int disposition = LOG_MODE_NONE;
		if(NAVIGATION_EVENTS.contains(event)) {
			disposition = this.getLogModeNavigationEvents();
		} else if (DASH_NAV_EVENTS.contains(event)) {
			disposition = this.getLogModeDashboardNavigationEvents();
		} else if (ITEM_DETAIL_EVENTS.contains(event)) {
			disposition = this.getLogModeItemDetailEvents();
		} else if (PREFERENCE_EVENTS.contains(event)) {
			disposition = this.getLogModePreferenceEvents();
		} else {
			// log error and return
			logger.warn("attempting to record dashboard activity with invalid event. event == " + event);
			return;
		}
		
		if(disposition == LOG_MODE_SAVE_AND_POST) {
			sakaiProxy.postEvent(event, itemRef, false);
			this.saveEventLocally(event, itemRef, false);
		} else if (disposition == LOG_MODE_POST) {
			sakaiProxy.postEvent(event, itemRef, false);
		} else if (disposition == LOG_MODE_SAVE) {
			this.saveEventLocally(event, itemRef, false);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#registerEntityType(org.sakaiproject.dash.entity.EntityType)
	 */
	public void registerEntityType(EntityType entityType) {
		if(entityType != null && entityType.getIdentifier() != null) {
			this.entityTypes.put(entityType.getIdentifier(), entityType);
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#registerEventProcessor(org.sakaiproject.dash.listener.EventProcessor)
	 */
	public void registerEventProcessor(EventProcessor eventProcessor) {
		
		if(eventProcessor != null && eventProcessor.getEventIdentifer() != null) {
			this.eventProcessors.put(eventProcessor.getEventIdentifer(), eventProcessor);
		}
		
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#removeCalendarItem(java.lang.String, java.lang.String, java.lang.Integer)
	 */
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

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#removeCalendarItems(java.lang.String)
	 */
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

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#removeCalendarLinks(java.lang.String, java.lang.String, int)
	 */
	public void removeCalendarLinks(String entityReference,
			String calendarTimeLabelKey, int sequenceNumber) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#removeNewsItem(java.lang.String)
	 */
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

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#removeCalendarLinks(java.lang.String)
	 */
	public void removeCalendarLinks(String entityReference) {
		
		List<CalendarItem> items = dao.getCalendarItems(entityReference);
		if(items != null && items.size() > 0) {
			for(CalendarItem item : items) {
			dao.deleteCalendarLinks(item.getId());
		}
	}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#removeCalendarLinks(java.lang.String, java.lang.String)
	 */
	public void removeCalendarLinks(String sakaiUserId, String contextId) {
		if(logger.isDebugEnabled()) {
			logger.debug("removeCalendarLinks(" + sakaiUserId + "," + contextId + ") ");
		}
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		if(person != null) {
			Context context = dao.getContext(contextId);
			if(context != null) {
				dao.deleteCalendarLinks(person.getId(), context.getId());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#removeNewsLinks(java.lang.String)
	 */
	public void removeNewsLinks(String entityReference) {
		
		NewsItem item = dao.getNewsItem(entityReference);
		if(item != null) {
			dao.deleteNewsLinks(item.getId());
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#removeNewsLinks(java.lang.String, java.lang.String)
	 */
	public void removeNewsLinks(String sakaiUserId, String contextId) {
		if(logger.isDebugEnabled()) {
			logger.debug("removeNewsLinks(" + sakaiUserId + "," + contextId + ") ");
		}
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		if(person != null) {
			Context context = dao.getContext(contextId);
			if(context != null) {
				dao.deleteNewsLinks(person.getId(), context.getId());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#removeAllScheduledAvailabilityChecks(java.lang.String)
	 */
	public void removeAllScheduledAvailabilityChecks(String entityReference) {
		boolean removed = dao.deleteAvailabilityChecks(entityReference);
		
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#reviseCalendarItemTime(java.lang.String, java.lang.String, java.lang.Integer, java.util.Date)
	 */
	public void reviseCalendarItemTime(String entityReference,
			String labelKey, Integer sequenceNumber, Date newDate) {
		
		dao.updateCalendarItemTime(entityReference, labelKey, sequenceNumber, newDate);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#reviseCalendarItemsTime(java.lang.String, java.util.Date)
	 */
	public void reviseCalendarItemsTime(String entityReference, Date newTime) {
		
		List<CalendarItem> items = dao.getCalendarItems(entityReference);
		if(items != null) {
			for(CalendarItem item : items) {
				dao.updateCalendarItemTime(item.getId(), newTime);
			}
		}
				
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#reviseCalendarItemsTitle(java.lang.String, java.lang.String)
	 */
	public void reviseCalendarItemsTitle(String entityReference, String newTitle) {
		
		List<CalendarItem> items = dao.getCalendarItems(entityReference);
		if(items != null) {
			for(CalendarItem item : items) {
				dao.updateCalendarItemTitle(item.getId(), newTitle);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#reviseCalendarItemsLabelKey(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void reviseCalendarItemsLabelKey(String entityReference, String oldLabelKey, String newLabelKey) {
		if(entityReference == null || oldLabelKey == null || newLabelKey == null) {
			return;
		}
		
		dao.updateCalendarItemsLabelKey(entityReference, oldLabelKey, newLabelKey);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#reviseNewsItemTime(java.lang.String, java.util.Date, java.lang.String)
	 */
	public void reviseNewsItemTime(String entityReference, Date newTime, String newGroupingIdentifier) {
		NewsItem item = dao.getNewsItem(entityReference);
		item.setNewsTime(newTime);
		if(item == null) {
			logger.warn("Attempting to revise time of non-existent news item: " + entityReference);
		} else {
			dao.updateNewsItemTime(item.getId(), newTime, newGroupingIdentifier);
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#reviseNewsItemTitle(java.lang.String, java.lang.String, java.util.Date, java.lang.String, java.lang.String)
	 */
	public void reviseNewsItemTitle(String entityReference, String newTitle, Date newNewsTime, String newLabelKey, String newGroupingIdentifier) {
		
		NewsItem item = dao.getNewsItem(entityReference);
		if(item == null) {
			logger.warn("Attempting to revise title of non-existent news item: " + entityReference);
		} else {
			dao.updateNewsItemTitle(item.getId(), newTitle, newNewsTime, newLabelKey, newGroupingIdentifier);
		}
		
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#reviseRepeatingCalendarItemFrequency(java.lang.String, java.lang.String)
	 */
	public boolean reviseRepeatingCalendarItemFrequency(String entityReference,
			String frequency) {
		return dao.updateRepeatingCalendarItemFrequency(entityReference, frequency);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#reviseRepeatingCalendarItemsLabelKey(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void reviseRepeatingCalendarItemsLabelKey(String entityReference, String oldLabelKey, String newLabelKey) {
		dao.updateRepeatingCalendarItemsLabelKey(entityReference, oldLabelKey, newLabelKey);
		
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#reviseRepeatingCalendarItemTime(java.lang.String, java.util.Date, java.util.Date)
	 */
	public void reviseRepeatingCalendarItemTime(String entityReference, Date newFirstTime, Date newLastTime) {
		boolean done = dao.updateRepeatingCalendarItemTime(entityReference, newFirstTime, newLastTime);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#reviseRepeatingCalendarItemTitle(java.lang.String, java.lang.String)
	 */
	public void reviseRepeatingCalendarItemTitle(String entityReference, String newTitle) {
		dao.updateRepeatingCalendarItemTitle(entityReference, newTitle);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#scheduleAvailabilityCheck(java.lang.String, java.lang.String, java.util.Date)
	 */
	public void scheduleAvailabilityCheck(String entityReference, String entityTypeId, Date scheduledTime) {
		AvailabilityCheck availabilityCheck = new AvailabilityCheck(entityReference, entityTypeId, scheduledTime);
		boolean added = dao.addAvailabilityCheck(availabilityCheck);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#updateCalendarLinks(java.lang.String)
	 */
	public void updateCalendarLinks(String entityReference) {
		List<CalendarItem> items = dao.getCalendarItems(entityReference);
		if(items != null && items.size() > 0) {
			CalendarItem firstItem = items.get(0);
			EntityType entityType = this.entityTypes.get(firstItem.getSourceType().getIdentifier());
			Set<String> oldUserSet = dao.getSakaIdsForUserWithCalendarLinks(entityReference);
			Set<String> newUserSet = new TreeSet<String>(entityType.getUsersWithAccess(entityReference));
			
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
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#updateNewsLinks(java.lang.String)
	 */
	public void updateNewsLinks(String entityReference) {
		NewsItem item = dao.getNewsItem(entityReference);
		if(item == null) {
			
		} else {
			EntityType entityType = this.entityTypes.get(item.getSourceType().getIdentifier());
			Set<String> oldUserSet = dao.getSakaiIdsForUserWithNewsLinks(entityReference);
			Set<String> newUserSet = new TreeSet<String>(entityType.getUsersWithAccess(entityReference));
			
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
	
	/**
	 * @return
	 */
	protected int getLogModeNavigationEvents() {
		return dashboardConfig.getConfigValue(DashboardConfig.PROP_LOG_MODE_FOR_NAVIGATION_EVENTS, new Integer(2));
	}
	
	/**
	 * @return
	 */
	protected int getLogModeDashboardNavigationEvents() {
		return dashboardConfig.getConfigValue(DashboardConfig.PROP_LOG_MODE_FOR_DASH_NAV_EVENTS, new Integer(2));
	}
	
	/**
	 * @return
	 */
	protected int getLogModeItemDetailEvents() {
		return dashboardConfig.getConfigValue(DashboardConfig.PROP_LOG_MODE_FOR_ITEM_DETAIL_EVENTS, new Integer(2));
	}
	
	/**
	 * @return
	 */
	protected int getLogModePreferenceEvents() {
		return dashboardConfig.getConfigValue(DashboardConfig.PROP_LOG_MODE_FOR_PREFERENCE_EVENTS, new Integer(2));
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
						// verify that users with permissions in alwaysAllowPermission have links and others do not
						
						// need to remove all links, if there are any
						this.removeCalendarLinks(check.getEntityReference());
						this.removeNewsLinks(check.getEntityReference());
					}
				}
				removeAvailabilityChecksBeforeTime(currentTime);
			}
		}
	}
	
	/**
	 * @param sakaiId
	 * @return
	 */
	protected Person getOrCreatePerson(String sakaiId) {
		Person person = dao.getPersonBySakaiId(sakaiId);
		if(person == null) {
			User userObj = this.sakaiProxy.getUser(sakaiId);
			person = new Person(sakaiId, userObj.getEid());
			dao.addPerson(person);
			person = dao.getPersonBySakaiId(sakaiId);
		}
		return person;
	}
	
	/**
	 * @param time
	 * @return
	 */
	protected List<AvailabilityCheck> getAvailabilityChecksBeforeTime(Date time) {
		
		return dao.getAvailabilityChecksBeforeTime(time);
	}

	/**
	 * @param map
	 * @param maxDepth
	 * @return
	 */
	protected Map processFormattedText(Map<String,Object> map, int maxDepth) {
		if(maxDepth <= 0) {
			return null;
		}
		for(Map.Entry<String,Object> entry : map.entrySet()) {
			Object val = entry.getValue();
			if(val instanceof String) {
				StringBuilder errorMessages = new StringBuilder();
				entry.setValue(FormattedText.processFormattedText((String) val, errorMessages , true, false));
				if(errorMessages != null && errorMessages.length() > 0) {
					logger.warn("Error encountered while processing values map:\n" + errorMessages);
				}
			} else if(val instanceof Map) {
				entry.setValue(processFormattedText((Map) val, maxDepth - 1));
			} else if(val instanceof List) {
				entry.setValue(processFormattedText((List) val, maxDepth - 1));
			}
		}
		return map;
	}

	/**
	 * @param list
	 * @param maxDepth
	 * @return
	 */
	protected List processFormattedText(List list, int maxDepth) {
		if(maxDepth <= 0) {
			return null;
		}
		for(int i = 0; i < list.size(); i++) {
			Object item = list.get(i);
			if(item instanceof String) {
				StringBuilder errorMessages = new StringBuilder();
				list.set(i, FormattedText.processFormattedText((String) item, errorMessages , true, false));
				if(errorMessages != null && errorMessages.length() > 0) {
					logger.warn("Error encountered while processing values map:\n" + errorMessages);
				}
			} else if(item instanceof Map) {
				processFormattedText((Map) item, maxDepth - 1);
			} else if(item instanceof List) {
				processFormattedText((List) item, maxDepth - 1);
			}
		}
		return list;
	}

	/**
	 * @param event
	 * @param itemRef
	 * @param b
	 */
	protected void saveEventLocally(String event, String itemRef, boolean b) {
		// event_date timestamp, event varchar (32), itemRef varchar (255), 
		// contextId varchar (255), session_id varchar (163), event_code varchar (1)
		Date eventDate = new Date();
		String contextId = sakaiProxy.getCurrentSiteId();
		String sessionId = sakaiProxy.getCurrentSessionId();
		String eventCode = "X";
		
		boolean success = dao.addEvent(eventDate, event, itemRef, contextId, sessionId, eventCode);
	}

	/**
	 * @param time
	 */
	protected void removeAvailabilityChecksBeforeTime(Date time) {
		
		dao.deleteAvailabilityChecksBeforeTime(time);
		
	}

	protected void initServerId() {
		serverId = sakaiProxy.getServerId();
		
	}
	
	/************************************************************************
	 * init() and destroy()
	 ************************************************************************/

	public void init() {
		logger.info("init()");
		
		if (!sakaiProxy.isEventProcessingThreadDisabled())
		{
			if(this.eventProcessingThread == null) {
				this.eventProcessingThread = new DashboardEventProcessingThread();
			}
			this.eventProcessingThread.start();
			
			this.sakaiProxy.registerFunction(DASHBOARD_NEGOTIATE_AVAILABILITY_CHECKS);
			this.sakaiProxy.registerFunction(DASHBOARD_NEGOTIATE_REPEAT_EVENTS);
			this.sakaiProxy.registerFunction(DASHBOARD_NEGOTIATE_EXPIRATION_AND_PURGING);
			
			this.sakaiProxy.addLocalEventListener(this);
			
			Integer weeksToHorizon = dashboardConfig.getConfigValue(DashboardConfig.PROP_WEEKS_TO_HORIZON, new Integer(4));
			this.horizon = new Date(System.currentTimeMillis() + weeksToHorizon.longValue() * 7L * ONE_DAY);
		}
		
		
	}
	
	public void destroy() {
		logger.info("destroy()");
		
		synchronized(eventQueueLock) {
			if(this.eventQueue != null) {
				// empty the event queue 
				this.eventQueue.clear();
				
				// shut down daemon once it's done processing events
				if(this.eventProcessingThread != null) {
					this.eventProcessingThread.close();
					this.eventProcessingThread = null;
				}
				
				// destroy the event queue 
				this.eventQueue = null;
			}
		}
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
				synchronized(this.eventQueueLock) {
					if(this.eventQueue != null) {
						this.eventQueue.add(new EventCopy(event));	
					}
				}
				if(this.eventProcessingThread == null || ! this.eventProcessingThread.isAlive()) {
					if( eventQueue != null) {
						// the update() method gets called if and only if DashboardLogic is registered as an observer.
						// DashboardLogic is registered as an observer if and only if event processing is enabled.
						// So if the eventProcessingThread is null or disabled in some way, we should restart it, 
						// unless the eventQueue is null, which should happen if and only if we are shutting down.
						this.eventProcessingThread = null;
						this.eventProcessingThread = new DashboardEventProcessingThread();
						this.eventProcessingThread.start();
					}
				}
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
		protected static final String EVENT_PROCESSING_THREAD_SHUT_DOWN_MESSAGE = 
			"\n===================================================\n  Dashboard Event Processing Thread shutting down  \n===================================================";

		private static final long ONE_WEEK_IN_MILLIS = 1000L * 60L * 60L * 24L * 7L;

		protected boolean timeToQuit = false;
		
		protected Date handlingAvailabilityChecksTimer = null;
		protected Date handlingRepeatedEventsTimer = null;
		protected Date handlingExpirationAndPurgingTime = null;
		
		private long sleepTime = 2L;

		public DashboardEventProcessingThread() {
			super("Dashboard Event Processing Thread");
			logger.info("Created Dashboard Event Processing Thread");
			
			this.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler(){

				public void uncaughtException(Thread arg0, Throwable arg1) {
					logger.error(EVENT_PROCESSING_THREAD_SHUT_DOWN_MESSAGE, arg1);
					
				}
				
			});
		}

		public void close() {
			timeToQuit = true;
		}

		public void run() {
			try {
				dashboardEventProcessorThreadId = Thread.currentThread().getId();
				logger.info("Started Dashboard Event Processing Thread: " + dashboardEventProcessorThreadId);
				
				registerEventProcessor(new DashboardNegotiateAvailabilityChecksEventProcessor());
				registerEventProcessor(new DashboardNegotiateRepeatEventsEventProcessor());
				registerEventProcessor(new DashboardNegotiateExpirationAndPurgingProcessor());
				
				boolean timeToHandleAvailabilityChecks = true;
				boolean timeToHandleRepeatedEvents = false;
				boolean timeToHandleExpirationAndPurging = false;
				
				sakaiProxy.startAdminSession();
				while(! timeToQuit) {
					if(logger.isDebugEnabled()) {
						logger.debug("Dashboard Event Processing Thread checking event queue: " + eventQueue.size());
					}
					EventCopy event = null;
					synchronized(eventQueueLock) {
						if(eventQueue != null && ! eventQueue.isEmpty()) {
							event = eventQueue.poll();
						}
					}
					if(event == null) {
						if(serverId == null) {
							initServerId();
						}
												
						if(timeToHandleAvailabilityChecks) {
							if(handlingAvailabilityChecks) {
								SecurityAdvisor advisor = new DashboardLogicSecurityAdvisor();
								sakaiProxy.pushSecurityAdvisor(advisor);
								try {
									handleAvailabilityChecks();
									//timeToHandleAvailabilityChecks = false;
								} catch (Exception e) {
									logger.warn("run: " + event, e);
								} finally {
									sakaiProxy.popSecurityAdvisor(advisor);
									sakaiProxy.clearThreadLocalCache();
								}
							} else if(notHandlingAvailabilityChecks) {
								// do nothing
							} else if ("".equals(serverHandlingAvailabilityChecks)) {
								claimAvailabilityCheckDuty();
							} else {
								if(handlingAvailabilityChecksTimer == null) {
									handlingAvailabilityChecksTimer = new Date(System.currentTimeMillis() + NEGOTIATING_TIME);
								} else if (handlingAvailabilityChecksTimer.before(new Date())) {
									if(serverHandlingAvailabilityChecks.equals(serverId)) {
										handlingAvailabilityChecks = true;
									} else {
										notHandlingAvailabilityChecks = true;
									}
									logger.info("Server handling availability checks is " + serverHandlingAvailabilityChecks);
								}
								
							} 
							timeToHandleRepeatedEvents = true;
							timeToHandleAvailabilityChecks = false;
						} else if(timeToHandleRepeatedEvents) {
							if(handlingRepeatedEvents) {
								SecurityAdvisor advisor = new DashboardLogicSecurityAdvisor();
								sakaiProxy.pushSecurityAdvisor(advisor);
								try {
									updateRepeatingEvents();
									//timeToHandleAvailabilityChecks = true;
								} catch (Exception e) {
									logger.warn("run: " + event, e);
								} finally {
									sakaiProxy.popSecurityAdvisor(advisor);
								}	
							} else if(notHandlingRepeatedEvents) {
								// do nothing
							} else if("".equals(serverHandlingRepeatEvents)) {
								claimRepeatEventsDuty();
							} else {
								if(handlingRepeatedEventsTimer == null) {
									handlingRepeatedEventsTimer = new Date(System.currentTimeMillis() + NEGOTIATING_TIME);
								} else if (handlingRepeatedEventsTimer.before(new Date())) {
									if(serverHandlingRepeatEvents.equals(serverId)) {
										handlingRepeatedEvents = true;
									} else {
										notHandlingRepeatedEvents = true;
									}
									logger.info("Server handling repeated events is " + serverHandlingRepeatEvents);
								}
							}
							timeToHandleExpirationAndPurging = true;
							timeToHandleRepeatedEvents = false;
						} else if(timeToHandleExpirationAndPurging) {
							
							if(handlingExpirationAndPurging) {
								SecurityAdvisor advisor = new DashboardLogicSecurityAdvisor();
								sakaiProxy.pushSecurityAdvisor(advisor);
								try {
									expireAndPurge();
									//timeToHandleAvailabilityChecks = true;
								} catch (Exception e) {
									logger.warn("run: " + event, e);
								} finally {
									sakaiProxy.popSecurityAdvisor(advisor);
								}	
							} else if(notHandlingExpirationAndPurging) {
								// do nothing
							} else if("".equals(serverHandlingExpirationAndPurging)) {
								claimExpirationAndPurging();
							} else {
								if(handlingExpirationAndPurgingTime == null) {
									handlingExpirationAndPurgingTime = new Date(System.currentTimeMillis() + NEGOTIATING_TIME);
								} else if (handlingExpirationAndPurgingTime.before(new Date())) {
									if(serverHandlingExpirationAndPurging.equals(serverId)) {
										handlingExpirationAndPurging = true;
									} else {
										notHandlingExpirationAndPurging = true;
									}
									logger.info("Server handling expiration and purging is " + serverHandlingExpirationAndPurging);
								}
							}
							timeToHandleAvailabilityChecks= true;
							timeToHandleExpirationAndPurging = false;
						}
						
						try {
							Thread.sleep(sleepTime * 1000L);
						} catch (InterruptedException e) {
							logger.warn("InterruptedException in Dashboard Event Processing Thread: " + e);
						}
					} else {
						if(logger.isDebugEnabled()) {
							logger.debug("Dashboard Event Processing Thread is processing event: " + event.getEvent());
						}
						EventProcessor eventProcessor = eventProcessors.get(event.getEvent());
						
						SecurityAdvisor advisor = new DashboardLogicSecurityAdvisor();
						sakaiProxy.pushSecurityAdvisor(advisor);
						try {
							eventProcessor.processEvent(event);
						} catch (Exception e) {
							logger.warn("Error processing event: " + event, e);
						} finally {
							sakaiProxy.popSecurityAdvisor(advisor);
							sakaiProxy.clearThreadLocalCache();
						}
					}
				}
				
				logger.warn(EVENT_PROCESSING_THREAD_SHUT_DOWN_MESSAGE);
				
			} catch(Throwable t) {
				logger.error("Unhandled throwable is stopping Dashboard Event Processing Thread", t);
				throw new RuntimeException(t);
			}
		}

		protected void expireAndPurge() {
			if(System.currentTimeMillis() > nextTimeToExpireAndPurge ) {
				expireAndPurgeCalendarItems();
				expireAndPurgeNewsItems();
				
				nextTimeToQueryAvailabilityChecks = System.currentTimeMillis() + TIME_BETWEEN_EXPIRING_AND_PURGING;
			}
			// TODO Auto-generated method stub
			
		}

		protected void expireAndPurgeNewsItems() {
			Integer weeksToExpireItems = dashboardConfig.getConfigValue(DashboardConfig.PROP_REMOVE_NEWS_ITEMS_AFTER_WEEKS, DEFAULT_NEWS_ITEM_EXPIRATION);
			Integer weeksToExpireStarredItems = dashboardConfig.getConfigValue(DashboardConfig.PROP_REMOVE_STARRED_NEWS_ITEMS_AFTER_WEEKS, DEFAULT_NEWS_ITEM_EXPIRATION);
			Integer weeksToExpireHiddenItems = dashboardConfig.getConfigValue(DashboardConfig.PROP_REMOVE_HIDDEN_NEWS_ITEMS_AFTER_WEEKS, DEFAULT_NEWS_ITEM_EXPIRATION);

			expireNewsLinks(new Date(System.currentTimeMillis() - weeksToExpireItems.intValue() * ONE_WEEK_IN_MILLIS), false, false);
			expireNewsLinks(new Date(System.currentTimeMillis() - weeksToExpireStarredItems.intValue() * ONE_WEEK_IN_MILLIS), false, false);
			expireNewsLinks(new Date(System.currentTimeMillis() - weeksToExpireHiddenItems.intValue() * ONE_WEEK_IN_MILLIS), false, true);
			purgeNewsItems();
			
		}

		private void purgeNewsItems() {
			dao.deleteNewsItemsWithoutLinks();
		}

		protected void expireNewsLinks(Date expireBefore, boolean starred, boolean hidden) {
			dao.deleteNewsLinksBefore(expireBefore,starred,hidden);
			
		}

		protected void expireAndPurgeCalendarItems() {
			Integer weeksToExpireItems = dashboardConfig.getConfigValue(DashboardConfig.PROP_REMOVE_CALENDAR_ITEMS_AFTER_WEEKS, DEFAULT_CALENDAR_ITEM_EXPIRATION);
			Integer weeksToExpireStarredItems = dashboardConfig.getConfigValue(DashboardConfig.PROP_REMOVE_STARRED_CALENDAR_ITEMS_AFTER_WEEKS, DEFAULT_CALENDAR_ITEM_EXPIRATION);
			Integer weeksToExpireHiddenItems = dashboardConfig.getConfigValue(DashboardConfig.PROP_REMOVE_HIDDEN_CALENDAR_ITEMS_AFTER_WEEKS, DEFAULT_CALENDAR_ITEM_EXPIRATION);

			expireCalendarLinks(new Date(System.currentTimeMillis() - weeksToExpireItems.intValue() * ONE_WEEK_IN_MILLIS), false, false);
			expireCalendarLinks(new Date(System.currentTimeMillis() - weeksToExpireStarredItems.intValue() * ONE_WEEK_IN_MILLIS), false, false);
			expireCalendarLinks(new Date(System.currentTimeMillis() - weeksToExpireHiddenItems.intValue() * ONE_WEEK_IN_MILLIS), false, true);
			purgeCalendarItems();
		}

		private void purgeCalendarItems() {
			dao.deleteCalendarItemsWithoutLinks();
			
		}

		protected void expireCalendarLinks(Date expireBefore, boolean starred, boolean hidden) {
			dao.deleteCalendarLinksBefore(expireBefore, starred, hidden);
		}

		/**
		 * 
		 */
		protected void updateRepeatingEvents() {
			
			if(nextHorizonUpdate != null && System.currentTimeMillis() > nextHorizonUpdate.getTime()) {
				// time to update
				Date oldHorizon = horizon;
				Integer weeksToHorizon = dashboardConfig.getConfigValue(DashboardConfig.PROP_WEEKS_TO_HORIZON, new Integer(4));
				horizon = new Date(System.currentTimeMillis() + weeksToHorizon * 7L * ONE_DAY);
				
				if(horizon.after(oldHorizon)) {
					List<RepeatingCalendarItem> repeatingEvents = dao.getRepeatingCalendarItems();
					if(repeatingEvents != null) {
						for(RepeatingCalendarItem repeatingEvent: repeatingEvents) {
							addCalendarItemsForRepeatingCalendarItem(repeatingEvent, oldHorizon, horizon);

						}
					}
				}
				Integer daysBetweenHorizonUpdates = dashboardConfig.getConfigValue(DashboardConfig.PROP_DAYS_BETWEEN_HORIZ0N_UPDATES, new Integer(1));
				nextHorizonUpdate = new Date(nextHorizonUpdate.getTime() + daysBetweenHorizonUpdates.longValue() * ONE_DAY);
			}
		}

		protected void claimAvailabilityCheckDuty() {
			if(claimAvailabilityCheckDutyTime == null) {
				sakaiProxy.postEvent(DASHBOARD_NEGOTIATE_AVAILABILITY_CHECKS, serverId, true);
				
				claimAvailabilityCheckDutyTime = new Date(System.currentTimeMillis() + NEGOTIATING_TIME);
			} 
		}
		
		protected void claimRepeatEventsDuty() {
			if(claimRepeatEventsDutyTime == null) {
				sakaiProxy.postEvent(DASHBOARD_NEGOTIATE_REPEAT_EVENTS, serverId, true);
				
				claimRepeatEventsDutyTime = new Date(System.currentTimeMillis() + NEGOTIATING_TIME);
			} 
		}

		protected void claimExpirationAndPurging() {
			if(claimExpirationAndPurgingTime == null) {
				sakaiProxy.postEvent(DASHBOARD_NEGOTIATE_EXPIRATION_AND_PURGING, serverId, true);
				
				claimExpirationAndPurgingTime = new Date(System.currentTimeMillis() + NEGOTIATING_TIME);
			}
		}
	}
	
	/**
	 * 
	 *
	 */
	public class DashboardLogicSecurityAdvisor implements SecurityAdvisor 
	{
		/**
		 */
		public DashboardLogicSecurityAdvisor() {
			super();
		}

		/*
		 * (non-Javadoc)
		 * @see org.sakaiproject.authz.api.SecurityAdvisor#isAllowed(java.lang.String, java.lang.String, java.lang.String)
		 */
		public SecurityAdvice isAllowed(String userId, String function,
				String reference) {
			
			long threadId = Thread.currentThread().getId();
			
			if(threadId == DashboardLogicImpl.dashboardEventProcessorThreadId) {
				return SecurityAdvice.ALLOWED;
			}
			return SecurityAdvice.PASS;
		}
		
	}

	public class DashboardNegotiateAvailabilityChecksEventProcessor implements EventProcessor {

		public String getEventIdentifer() {
			return DASHBOARD_NEGOTIATE_AVAILABILITY_CHECKS;
		}

		public void processEvent(Event event) {
			if(logger.isDebugEnabled()) {
				logger.debug("\n\n\n=============================================================\n" + event  
						+ "\n=============================================================\n\n\n");
			}
			if(event.getModify()) {
				// this is a message indicating an attempt to claim availability-check processing
				if(handlingAvailabilityChecks) {
					// availability-check processing is already claimed by this server -- report that
					sakaiProxy.postEvent(DASHBOARD_NEGOTIATE_AVAILABILITY_CHECKS, serverHandlingAvailabilityChecks, false);
				} else if (notHandlingAvailabilityChecks) {
					// do nothing
				} else if(event.getEventTime() != null && (claimAvailabilityCheckDutyTime == null || event.getEventTime().before(claimAvailabilityCheckDutyTime))) {
					// negotiate
					synchronized(serverHandlingAvailabilityChecks) {
						claimAvailabilityCheckDutyTime = event.getEventTime();
						serverHandlingAvailabilityChecks = event.getResource();
					}
				}
			} else {
				// this message indicates that availability-check processing has been claimed by another server
				if(! handlingAvailabilityChecks && ! notHandlingAvailabilityChecks) {
					// we're trying to claim availability-check processing and it's already claimed, so stop trying
					synchronized(serverHandlingAvailabilityChecks) {
						claimAvailabilityCheckDutyTime = new Date(System.currentTimeMillis() - NEGOTIATING_TIME);
						serverHandlingAvailabilityChecks = event.getResource();
					}
				}
				
			}			
		}
		
	}
	public class DashboardNegotiateRepeatEventsEventProcessor implements EventProcessor {

		public String getEventIdentifer() {
			return DASHBOARD_NEGOTIATE_REPEAT_EVENTS;
		}

		public void processEvent(Event event) {
			if(logger.isDebugEnabled()) {
				logger.debug("\n\n\n=============================================================\n" + event  
						+ "\n=============================================================\n\n\n");
			}
			if(event.getModify()) {
				// this is a message indicating an attempt to claim repeated events processing
				if(handlingRepeatedEvents) {
					// repeated events processing is already claimed by this server -- report that
					sakaiProxy.postEvent(DASHBOARD_NEGOTIATE_REPEAT_EVENTS, serverHandlingRepeatEvents, false);
				} else if (notHandlingRepeatedEvents) {
					// do nothing
				} else if(event.getEventTime() != null && (claimRepeatEventsDutyTime == null || event.getEventTime().before(claimRepeatEventsDutyTime))) {
					// negotiate
					synchronized(serverHandlingRepeatEvents) {
						claimRepeatEventsDutyTime = event.getEventTime();
						serverHandlingRepeatEvents = event.getResource();
					}
				}
			} else {
				// this message indicates that repeated events processing has been claimed by another server
				if(! handlingRepeatedEvents && ! notHandlingRepeatedEvents) {
					// we're trying to claim repeated events processing and it's already claimed, so stop trying
					synchronized(serverHandlingRepeatEvents) {
						claimRepeatEventsDutyTime = new Date(System.currentTimeMillis() - NEGOTIATING_TIME);
						serverHandlingRepeatEvents = event.getResource();
					}
				}
			}
		}
		
	}
	public class DashboardNegotiateExpirationAndPurgingProcessor implements EventProcessor {

		public String getEventIdentifer() {
			return DASHBOARD_NEGOTIATE_EXPIRATION_AND_PURGING;
		}

		public void processEvent(Event event) {
			if(logger.isDebugEnabled()) {
				logger.debug("\n\n\n=============================================================\n" + event  
						+ "\n=============================================================\n\n\n");
			}
			if(event.getModify()) {
				// this is a message indicating an attempt to claim expiration and purging 
				if(handlingExpirationAndPurging) {
					// expiration and purging is already claimed by this server -- report that
					sakaiProxy.postEvent(DASHBOARD_NEGOTIATE_EXPIRATION_AND_PURGING, serverHandlingExpirationAndPurging, false);
				} else if (notHandlingExpirationAndPurging) {
					// do nothing
				} else if(event.getEventTime() != null && (claimExpirationAndPurgingTime == null || event.getEventTime().before(claimExpirationAndPurgingTime))) {
					// negotiate
					synchronized(serverHandlingExpirationAndPurging) {
						claimExpirationAndPurgingTime = event.getEventTime();
						serverHandlingExpirationAndPurging = event.getResource();
					}
				}
			} else {
				// this message indicates that expiration and purging has been claimed by another server
				if(! handlingExpirationAndPurging && ! notHandlingExpirationAndPurging) {
					// we're trying to claim expiration and purging and it's already claimed, so stop trying
					synchronized(serverHandlingExpirationAndPurging) {
						claimExpirationAndPurgingTime = new Date(System.currentTimeMillis() - NEGOTIATING_TIME);
						serverHandlingExpirationAndPurging = event.getResource();
					}
				}
			}
		}
		
	}
}
