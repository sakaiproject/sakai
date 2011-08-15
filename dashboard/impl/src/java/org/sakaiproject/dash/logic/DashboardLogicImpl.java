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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.sf.ehcache.Cache;

import org.apache.log4j.Logger;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityAdvisor.SecurityAdvice;
import org.sakaiproject.dash.dao.DashboardDao;
import org.sakaiproject.dash.listener.EventProcessor;
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
	
	public CalendarItem createCalendarItem(String title, Date calendarTime,
			String entityReference, String entityUrl, Context context,
			SourceType sourceType) {
		
		CalendarItem calendarItem = new CalendarItem(title, calendarTime,
				entityReference, entityUrl, context, sourceType);
		
		dao.addCalendarItem(calendarItem);
		
		return dao.getCalendarItem(entityReference);
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#createCalendarLinks(org.sakaiproject.dash.model.CalendarItem)
	 */
	public void createCalendarLinks(CalendarItem calendarItem) {
		if(logger.isDebugEnabled()) {
			logger.debug("createCalendarLinks(" + calendarItem + ")");
		}
		List<String> sakaiIds = this.sakaiProxy.getUsersWithReadAccess(calendarItem.getEntityReference(), calendarItem.getSourceType().getAccessPermission());
		for(String sakaiId : sakaiIds) {
			Person person = dao.getPersonBySakaiId(sakaiId);
			if(person == null) {
				User userObj = this.sakaiProxy.getUser(sakaiId);
				person = new Person(sakaiId, userObj.getEid());
				dao.addPerson(person);
			}
			
			CalendarLink link = new CalendarLink(person, calendarItem, calendarItem.getContext(), false, false);
			
			dao.addCalendarLink(link);
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
		
		List<String> sakaiIds = this.sakaiProxy.getUsersWithReadAccess(newsItem.getEntityReference(), newsItem.getSourceType().getAccessPermission());
		if(sakaiIds != null && sakaiIds.size() > 0) {
			for(String sakaiId : sakaiIds) {
				
				Person person = dao.getPersonBySakaiId(sakaiId);
				if(person == null) {
					User userObj = this.sakaiProxy.getUser(sakaiId);
					person = new Person(sakaiId, userObj.getEid());
					dao.addPerson(person);
				}
				
				NewsLink link = new NewsLink(person, newsItem, newsItem.getContext(), false, false);
				
				dao.addNewsLink(link);
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

	public SourceType createSourceType(String identifier, String accessPermission) {
		
		SourceType sourceType = new SourceType(identifier, accessPermission); 
		dao.addSourceType(sourceType);
		return dao.getSourceType(identifier);
	}
	
	public CalendarItem getCalendarItem(long id) {
		
		return dao.getCalendarItem(id);
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

	public void registerEventProcessor(EventProcessor eventProcessor) {
		
		if(eventProcessor != null && eventProcessor.getEventIdentifer() != null) {
			this.eventProcessors.put(eventProcessor.getEventIdentifer(), eventProcessor);
		}
		
	}
	
	public void removeCalendarItem(String entityReference) {
		
		if(logger.isDebugEnabled()) {
			logger.debug("removing calendar links and calendar item for " + entityReference);
		}
		CalendarItem item = dao.getCalendarItem(entityReference);
		if(logger.isDebugEnabled()) {
			logger.debug("removing calendar links and calendar item for item: " + item);
		}
		
		if(item != null) {
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
		
		CalendarItem item = dao.getCalendarItem(entityReference);
		if(item != null) {
			dao.deleteCalendarLinks(item.getId());
		}
	}

	public void removeNewsLinks(String entityReference) {
		
		NewsItem item = dao.getNewsItem(entityReference);
		if(item != null) {
			dao.deleteNewsLinks(item.getId());
		}
	}

	public void reviseCalendarItem(String entityReference, String newTitle, Date newTime) {
		
		CalendarItem item = dao.getCalendarItem(entityReference);
		if(item != null) {
			dao.updateCalendarItem(item.getId(), newTitle, newTime);
		}
				
	}
	
	public void reviseCalendarItemTime(String entityReference, Date newTime) {
		
		CalendarItem item = dao.getCalendarItem(entityReference);
		if(item != null) {
			dao.updateCalendarItemTime(item.getId(), newTime);
		}
				
	}

	public void reviseCalendarItemTitle(String entityReference, String newTitle) {
		
		CalendarItem item = dao.getCalendarItem(entityReference);
		if(item != null) {
			dao.updateCalendarItemTitle(item.getId(), newTitle);
		}
		
	}

	public void reviseNewsItemTitle(String entityReference, String newTitle) {
		
		NewsItem item = dao.getNewsItem(entityReference);
		if(item != null) {
			dao.updateNewsItemTitle(item.getId(), newTitle);
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
			this.eventTime = original.getEventTime();
			this.modify = original.getModify();
			this.priority = original.getPriority();
			this.entityReference = original.getResource();
			this.sessionId = original.getSessionId();
			this.userId = original.getUserId();
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
			while(true) {
				if(logger.isDebugEnabled()) {
					logger.debug("Dashboard Event Processing Thread checking event queue: " + eventQueue.size());
				}
				if(eventQueue.isEmpty()) {
					try {
						Thread.sleep(sleepTime * 1000L);
					} catch (InterruptedException e) {
						
						e.printStackTrace();
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
