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
import org.sakaiproject.dash.listener.EventProcessor;
import org.sakaiproject.dash.model.AvailabilityCheck;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.CalendarLink;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.NewsLink;
import org.sakaiproject.dash.model.Person;
import org.sakaiproject.dash.model.Realm;
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
	public void setCache(Cache cache) {
		this.cache = cache;
	}
	
	/************************************************************************
	 * Dashboard Logic methods
	 ************************************************************************/
	
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
					if( this.sakaiProxy.isUserPermitted(sakaiUserId, item.getSourceType().getAccessPermission(), item.getEntityReference()) ) {
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
					if( this.sakaiProxy.isUserPermitted(sakaiUserId, item.getSourceType().getAccessPermission(), item.getEntityReference()) ) {
						NewsLink newsLink = new NewsLink(person, item, item.getContext(), false, false);
						dao.addNewsLink(newsLink);
					}
				}
			}
		}
	}

	public CalendarItem createCalendarItem(String title, Date calendarTime,
			String calendarTimeLabelKey, String entityReference, String entityUrl,
			Context context, SourceType sourceType) {
		
		CalendarItem calendarItem = new CalendarItem(title, calendarTime,
				calendarTimeLabelKey, entityReference, entityUrl, context, sourceType);
		
		dao.addCalendarItem(calendarItem);
		
		return dao.getCalendarItem(entityReference, calendarTimeLabelKey);
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
				
				CalendarLink link = new CalendarLink(person, calendarItem, calendarItem.getContext(), false, false);
				
				dao.addCalendarLink(link);
			}
		}
	}
	
	public Context createContext(String contextId) {
		
		Site site = this.sakaiProxy.getSite(contextId);
		
		Context context = new Context(site.getId(), site.getTitle(), site.getUrl());
		dao.addContext(context);
		return dao.getContext(contextId);
	}

	public NewsItem createNewsItem(String title, Date newsTime,
			String entityReference, String entityUrl, Context context,
			SourceType sourceType) {
		
		NewsItem newsItem = new NewsItem(title, newsTime, 
				entityReference, entityUrl, context, sourceType);
		
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
					
					Person person = getOrCreatePerson(sakaiId);
					
					NewsLink link = new NewsLink(person, newsItem, newsItem.getContext(), false, false);
					
					dao.addNewsLink(link);
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
	
	public CalendarItem getCalendarItem(String entityReference, String calendarTimeLabelKey) {
		
		return dao.getCalendarItem(entityReference, calendarTimeLabelKey);
	}

	public List<CalendarItem> getCalendarItems(String sakaiUserId,
			String contextId) {
		
		return dao.getCalendarItems(sakaiUserId, contextId);
	}

	public List<CalendarItem> getCalendarItems(String sakaiUserId) {
		
		return dao.getCalendarItems(sakaiUserId, null);
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

	public List<NewsItem> getNewsItems(String sakaiUserId, String contextId) {
		
		return dao.getNewsItems(sakaiUserId, contextId);
	}

	public List<NewsItem> getNewsItems(String sakaiUserId) {
		
		return dao.getNewsItems(sakaiUserId, null);
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

	public void reviseNewsItemTitle(String entityReference, String newTitle) {
		
		NewsItem item = dao.getNewsItem(entityReference);
		if(item != null) {
			dao.updateNewsItemTitle(item.getId(), newTitle);
		}
		
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
		
		this.eventProcessingThread.start();
		
		this.sakaiProxy.addLocalEventListener(this);
	}
	
	public void destroy() {
		logger.info("destroy()");
		
		// need to shut down daemon once it's done processing events??
		//this.daemon.
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
			sakaiProxy.startAdminSession();
			while(true) {
				if(logger.isDebugEnabled()) {
					logger.debug("Dashboard Event Processing Thread checking event queue: " + eventQueue.size());
				}
				if(eventQueue.isEmpty()) {
					handleAvailabilityChecks();
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

}
