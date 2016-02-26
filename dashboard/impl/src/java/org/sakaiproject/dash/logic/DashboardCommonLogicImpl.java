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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Collection;

import net.sf.ehcache.Cache;

import org.apache.log4j.Logger;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.dash.app.DashboardCommonLogic;
import org.sakaiproject.dash.app.DashboardConfig;
import org.sakaiproject.dash.app.DashboardUserLogic;
import org.sakaiproject.dash.app.SakaiProxy;
import org.sakaiproject.dash.dao.DashboardDao;
import org.sakaiproject.dash.entity.DashboardEntityInfo;
import org.sakaiproject.dash.entity.RepeatingEventGenerator;
import org.sakaiproject.dash.listener.EventProcessor;
import org.sakaiproject.dash.model.AvailabilityCheck;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.CalendarLink;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.NewsLink;
import org.sakaiproject.dash.model.RepeatingCalendarItem;
import org.sakaiproject.dash.model.SourceType;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * 
 *
 */
public class DashboardCommonLogicImpl implements DashboardCommonLogic, Observer {
	private static Logger logger = Logger.getLogger(DashboardCommonLogicImpl.class);
	
	private static final int TASK_LOGGING_INTERVAL = 100;
	
	private static final long ONE_WEEK_IN_MILLIS = 1000L * 60L * 60L * 24L * 7L;
	public static final long TIME_BETWEEN_AVAILABILITY_CHECKS = 1000L * 60L * 1L;  // one minute
	public static final long TIME_BETWEEN_EXPIRING_AND_PURGING = 1000L * 60L * 60L; // one hour

	protected Date nextHorizonUpdate = new Date();
		
	protected long nextTimeToQueryAvailabilityChecks = System.currentTimeMillis();
	protected long nextTimeToExpireAndPurge = System.currentTimeMillis();
	
	protected DashboardEventProcessingThread eventProcessingThread = new DashboardEventProcessingThread();
	protected Queue<EventCopy> eventQueue = new ConcurrentLinkedQueue<EventCopy>();
	protected Object eventQueueLock = new Object();
	
	protected static long dashboardEventProcessorThreadId = 0L;

	protected String serverId = null;
	protected String serverHandlingAvailabilityChecks = "";
	protected String serverHandlingRepeatEvents = "";
	protected String serverHandlingExpirationAndPurging = "";

	protected boolean handlingAvailabilityChecks = false;
	protected boolean handlingRepeatedEvents = false;
	protected boolean handlingExpirationAndPurging = false;
	protected boolean loopTimerEnabled = false;
	
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
	
	protected String propLoopTimerEnabledLocally = null;
	
	// strings to indicate dashboard link type
	private static final String CALENDAR_LINK_TYPE = "calendar_link_type";
	private static final String NEWS_LINK_TYPE = "news_link_type";

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
	
	protected DashboardLogic dashboardLogic;
	public void setDashboardLogic(DashboardLogic dashboardLogic) {
		this.dashboardLogic = dashboardLogic;
	}

	protected DashboardUserLogic dashboardUserLogic;
	public void setDashboardUserLogic(DashboardUserLogic dashboardUserLogic) {
		this.dashboardUserLogic = dashboardUserLogic;
	}

	protected AuthzGroupService authzGroupService;
	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
		this.authzGroupService = authzGroupService;
	}

	protected PlatformTransactionManager transactionManager;
	public void setTransactionManager(PlatformTransactionManager txManager) {
	    transactionManager = txManager;
	}	

	protected Cache cache;

	public void setCache(Cache cache) {
		this.cache = cache;
	}
	
	public void updateTimeOfRepeatingCalendarItem(RepeatingCalendarItem repeatingEvent, Date oldTime, Date newTime) {
		if(repeatingEvent == null) {
			logger.warn("updateTimeOfRepeatingCalendarItem() called with null parameter ");
		} else {
			DashboardEntityInfo dashboardEntityInfo = this.dashboardLogic.getDashboardEntityInfo(repeatingEvent.getSourceType().getIdentifier());
			if(dashboardEntityInfo == null) {
				// TODO: handle error: entityType cannot be null
				logger.warn("updateTimeOfRepeatingCalendarItem() handle error: entityType cannot be null");
			} else if(dashboardEntityInfo instanceof RepeatingEventGenerator) {
				Date beginDate = repeatingEvent.getFirstTime();
				Date endDate = repeatingEvent.getLastTime();
				Map<Integer, Date> dates = ((RepeatingEventGenerator) dashboardEntityInfo).generateRepeatingEventDates(repeatingEvent.getEntityReference(), beginDate, endDate);
				for(Map.Entry<Integer, Date> entry : dates.entrySet()) {
					
				}
				
			} else {
				// TODO: handle error: entityType cannot be null
				logger.warn("updateTimeOfRepeatingCalendarItem() handle error: entityType must be RepeatingEventGenerator");
			}
		}
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardCommonLogic#getEntityIconUrl(java.lang.String, java.lang.String)
	 */
	public String getEntityIconUrl(String type, String subtype) {
		String url = "#"; 
		if(type != null) {
		DashboardEntityInfo typeObj = this.dashboardLogic.getDashboardEntityInfo(type);
			if(typeObj != null) {
				url = typeObj.getIconUrl(subtype);
			}
		}
		return url;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardCommonLogic#getMOTD()
	 */
	public List<NewsItem> getMOTD() {
		return dao.getMOTD(DashboardLogic.MOTD_CONTEXT);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardCommonLogic#getEntityMapping(java.lang.String, java.lang.String, java.util.Locale)
	 */
	public Map<String, Object> getEntityMapping(String entityType, String entityReference, Locale locale) {
		Map<String, Object> map = new HashMap<String, Object>();
		if(logger.isDebugEnabled()) {
			logger.debug("getEntityMapping(" + entityType + "," + entityReference + "," + locale + ")");
		}

		DashboardEntityInfo entityTypeDef = this.dashboardLogic.getDashboardEntityInfo(entityType);
		if(entityTypeDef != null) {
			if(logger.isDebugEnabled()) {
				logger.debug("getEntityMapping(" + entityType + "," + entityReference + "," + locale + ") " + entityTypeDef);
			}
			Map<String, Object> values = processFormattedText(entityTypeDef.getValues(entityReference, locale.toString()), 6);
			map.putAll(values);
			map.putAll(entityTypeDef.getProperties(entityReference, locale.toString()));
			map.put(DashboardEntityInfo.VALUES_ORDER, entityTypeDef.getOrder(entityReference, locale.toString()));
		}
		
		return map;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardCommonLogic#getString(java.lang.String, java.lang.String, java.lang.String)
	 */
	public String getString(String key, String dflt, String entityTypeId) {
		if(dflt == null) {
			dflt = "";
		}
		String str = dflt;
		if(key == null || entityTypeId == null) {
			logger.warn("getString() invoked with null parameter: " + key + " :: " + entityTypeId);
		} else {
			DashboardEntityInfo dashboardEntityInfo = this.dashboardLogic.getDashboardEntityInfo(entityTypeId);
			if(dashboardEntityInfo == null) {
				logger.warn("getString() invalid entityTypeId: " + entityTypeId);
			} else {
				str = dashboardEntityInfo.getEventDisplayString(key, dflt);
			}
		}
		return str;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardCommonLogic#recordDashboardActivity(java.lang.String, java.lang.String)
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
	 * This is to be called from Quartz job
	 */
	public void handleAvailabilityChecks() {
		handleAvailabilityChecks(false);
	}
	
	/*
	 * 
	 */
	public void handleAvailabilityChecks(boolean taskLockApproach) {
		Date currentTime = new Date();
		if((taskLockApproach && currentTime.getTime() > nextTimeToQueryAvailabilityChecks )
			|| !taskLockApproach)
		{
			SecurityAdvisor advisor = getDashboardSecurityAdvisor();
			sakaiProxy.pushSecurityAdvisor(advisor);
			try {
				long startTime = System.currentTimeMillis();
				
				logger.debug("DashboardCommonLogicImpl.handleAvailabilityChecks start " + serverId);
				
				List<AvailabilityCheck> checks = getAvailabilityChecksBeforeTime(currentTime );
				nextTimeToQueryAvailabilityChecks = currentTime.getTime() + TIME_BETWEEN_AVAILABILITY_CHECKS;
				
				if(checks != null) {
					logger.debug("DashboardCommonLogicImpl.handleAvailabilityChecks checks size=" + checks.size());
					int count = 0;
					for(AvailabilityCheck check : checks) {
						DashboardEntityInfo dashboardEntityInfo = this.dashboardLogic.getDashboardEntityInfo(check.getEntityTypeId());
						if(dashboardEntityInfo == null) {
							logger.warn("Unable to process AvailabilityCheck because entityType is null " + check.toString());
						} else if(dashboardEntityInfo.isAvailable(check.getEntityReference())) {
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
						count++;
						if (count % TASK_LOGGING_INTERVAL == 0)
						{
							logger.debug("DashboardCommonLogicImpl.handleAvailabilityChecks processed " + count + " checks.");
						}
					}
					logger.debug("DashboardCommonLogicImpl.handleAvailabilityChecks end of the loop processed " + count + " checks.");
					removeAvailabilityChecksBeforeTime(currentTime);
				}
				
				if (taskLockApproach)
				{
					dashboardLogic.updateTaskLock(TaskLock.CHECK_AVAILABILITY_OF_HIDDEN_ITEMS);
				}
				
				long elapsedTime = System.currentTimeMillis() - startTime;
				StringBuilder buf = new StringBuilder("DashboardCommonLogicImpl.handleAvailabilityChecks done. ");
				buf.append(serverId);
				buf.append(" Elapsed Time (ms): ");
				buf.append(elapsedTime);
				logger.debug(buf.toString());

			} catch (Exception e) {
				logger.warn(this + " handleAvailabilityChecks: ", e);
			} finally {
				sakaiProxy.popSecurityAdvisor(advisor);
				sakaiProxy.clearThreadLocalCache();
			}
		}
		
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
	@Transactional
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
	@Transactional
	protected void removeAvailabilityChecksBeforeTime(Date time) {
		
		dao.deleteAvailabilityChecksBeforeTime(time);
		
	}

	/************************************************************************
	 * init() and destroy()
	 ************************************************************************/

	public void init() {
		logger.info("init()");
		
		if(serverId == null) {
			serverId = sakaiProxy.getServerId();
		}
		
		if (!sakaiProxy.isEventProcessingThreadDisabled())
		{
			if(this.eventProcessingThread == null) {
				this.eventProcessingThread = new DashboardEventProcessingThread();
			}
			this.eventProcessingThread.start();
			
//			this.sakaiProxy.registerFunction(DASHBOARD_NEGOTIATE_AVAILABILITY_CHECKS);
//			this.sakaiProxy.registerFunction(DASHBOARD_NEGOTIATE_REPEAT_EVENTS);
//			this.sakaiProxy.registerFunction(DASHBOARD_NEGOTIATE_EXPIRATION_AND_PURGING);
			
			this.sakaiProxy.addLocalEventListener(this);
			
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
			if(this.dashboardLogic.getEventProcessor(event.getEvent()) != null) {
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
						// the update() method gets called if and only if DashboardCommonLogic is registered as an observer.
						// DashboardCommonLogic is registered as an observer if and only if event processing is enabled.
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
	 * Event processing daemon (or thread?)
	 ************************************************************************/
	
	/**
	 * 
	 */
	public class DashboardEventProcessingThread extends Thread
	{
		protected static final String EVENT_PROCESSING_THREAD_SHUT_DOWN_MESSAGE = 
			"\n===================================================\n  Dashboard Event Processing Thread shutting down  \n===================================================";

		protected boolean timeToQuit = false;
		
		protected Date handlingAvailabilityChecksTimer = null;
		protected Date handlingRepeatedEventsTimer = null;
		protected Date handlingExpirationAndPurgingTime = null;
		
		protected long loopTimer = 0L;
		protected String loopActivity = "";
		
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
			if(handlingRepeatedEvents) {
				removeTaskLocks(TaskLock.UPDATE_REPEATING_EVENTS);
			}
			
			
			timeToQuit = true;
		}

	    // WARNING
	    // The database layer we're using, jdbctemplate, requires explicit commits or putting all
	    // changes in a transaction. We're using a @Transactional annotation for the layer right
	    // above the Dao to put everything in transactions. But that doesn't seem to work for this
	    // background task. Thus I'm putting every interation of the loop in a transaction explicitly

		public void run() {
			// wait till ComponentManager is ready
			ComponentManager.waitTillConfigured();

			TransactionStatus status = null;
			try {
				DefaultTransactionDefinition defaultTransaction = new DefaultTransactionDefinition();
				dashboardEventProcessorThreadId = Thread.currentThread().getId();
				logger.info("Started Dashboard Event Processing Thread: " + dashboardEventProcessorThreadId);
				if(propLoopTimerEnabledLocally == null) {
					propLoopTimerEnabledLocally = DashboardConfig.PROP_LOOP_TIMER_ENABLED + "_" + serverId;
				}
				String dashboardQuartzServer = sakaiProxy.getConfigParam("dashboard_quartzServer", null);
				boolean timeToHandleAvailabilityChecks = true;
				boolean timeToHandleRepeatedEvents = false;
				boolean timeToHandleExpirationAndPurging = false;
				boolean timeToCheckForAdminChanges = false;
								
				sakaiProxy.startAdminSession();
				while(! timeToQuit) {
				        status = transactionManager.getTransaction(defaultTransaction);
					if(loopTimerEnabled) {
						loopTimer = System.currentTimeMillis();
						loopActivity = "nothing";
					}
					if(logger.isDebugEnabled()) {
						logger.debug("Dashboard Event Processing Thread checking event queue: " + eventQueue.size());
					}
					EventCopy event = null;
					synchronized(eventQueueLock) {
						if(eventQueue != null && ! eventQueue.isEmpty()) {
							event = eventQueue.poll();
						}
					}
					
					// always give precedence to handling events from queue
					// so skip other tasks if there's an event to process
					if(event == null) {
						if (dashboardQuartzServer == null)
						{
							if(timeToHandleAvailabilityChecks) {
								if(handlingAvailabilityChecks) {
									if(loopTimerEnabled) {
										loopActivity = "checkingTimeForAvailabilityChecks";
									}
									handleAvailabilityChecks(true);
								} else {
									// TODO: move to checkForAdminUpdates
									if(loopTimerEnabled) {
										loopActivity = "checkingTaskLock_handleAvailabilityChecks";
									}
									handlingAvailabilityChecks = dashboardLogic.checkTaskLock(TaskLock.CHECK_AVAILABILITY_OF_HIDDEN_ITEMS);
								} 
								timeToHandleRepeatedEvents = true;
								timeToHandleAvailabilityChecks = false;
							} else if(timeToHandleRepeatedEvents) {
								if (true){//if(handlingRepeatedEvents) {
									if(loopTimerEnabled) {
										loopActivity = "checkingTimeForRepeatedEvents";
									}
									updateRepeatingEvents(true);
								} else {
									// TODO: move to checkForAdminUpdates
									if(loopTimerEnabled) {
										loopActivity = "checkingTaskLock_handleRepeatedEvents";
									}
									handlingRepeatedEvents = dashboardLogic.checkTaskLock(TaskLock.UPDATE_REPEATING_EVENTS);
								}
								timeToHandleExpirationAndPurging = true;
								timeToHandleRepeatedEvents = false;
							} else if(timeToHandleExpirationAndPurging) {
								if(handlingExpirationAndPurging) {
									if(loopTimerEnabled) {
										loopActivity = "checkingTimeForExpirationAndPurging";
									}
									expireAndPurge(true);
								} else {
									// TODO: move to checkForAdminUpdates
									if(loopTimerEnabled) {
										loopActivity = "checkingTaskLock_handleExpirationAndPurging";
									}
									handlingExpirationAndPurging = dashboardLogic.checkTaskLock(TaskLock.EXPIRE_AND_PURGE_OLD_DASHBOARD_ITEMS);
								}
								timeToCheckForAdminChanges= true;
								timeToHandleExpirationAndPurging = false;
							} else if(timeToCheckForAdminChanges) {
								if(loopTimerEnabled) {
									loopActivity = "checkingForAdminChanges";
								}
								checkForAdminChanges();
								timeToHandleAvailabilityChecks= true;
								timeToCheckForAdminChanges = false;
							}
							if(loopTimerEnabled) {
								long elapsedTime = System.currentTimeMillis() - loopTimer;
								StringBuilder buf = new StringBuilder("DashboardEventProcessingThread.activityTimer\t");
								buf.append(loopTimer);
								buf.append("\t");
								buf.append(elapsedTime);
								buf.append("\t");
								buf.append(loopActivity);
								logger.info(buf.toString());
							}
						}
					} else {
						if(loopTimerEnabled) {
							loopActivity = "processingEvents";
						}
						if(logger.isDebugEnabled()) {
							logger.debug("Dashboard Event Processing Thread is processing event: " + event.getEvent());
						}
						final EventProcessor eventProcessor = dashboardLogic.getEventProcessor(event.getEvent());
						
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
						
						if(loopTimerEnabled) {
							long elapsedTime = System.currentTimeMillis() - loopTimer;
							StringBuilder buf = new StringBuilder("DashboardEventProcessingThread.activityTimer\t");
							buf.append(loopTimer);
							buf.append("\t");
							buf.append(elapsedTime);
							buf.append("\t");
							buf.append(loopActivity);
							logger.info(buf.toString());
						}
					}
					
					if(eventQueue == null || eventQueue.isEmpty()) {
						try {
							Thread.sleep(sleepTime * 1000L);
						} catch (InterruptedException e) {
							logger.warn("InterruptedException in Dashboard Event Processing Thread: " + e);
						}
					}
					transactionManager.commit(status);
					status = null;
				}
				
				logger.warn(EVENT_PROCESSING_THREAD_SHUT_DOWN_MESSAGE);
				
			} catch(Throwable t) {
				logger.error("Unhandled throwable is stopping Dashboard Event Processing Thread", t);
				throw new RuntimeException(t);
			} finally {
			    // abnormal termination, on normal end of loop status is set null
				if (status != null)
				    transactionManager.rollback(status);
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

			if(threadId == DashboardCommonLogicImpl.dashboardEventProcessorThreadId) {
				return SecurityAdvice.ALLOWED;
			}
			return SecurityAdvice.PASS;
		}

	}



	/************************************************************************
	 * DashboardLogic methods
	 ************************************************************************/

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#addCalendarItemsForRepeatingCalendarItem(org.sakaiproject.dash.model.RepeatingCalendarItem, java.util.Date, java.util.Date)
	 */
	@Override
	public void addCalendarItemsForRepeatingCalendarItem(
			RepeatingCalendarItem repeatingEvent, Date oldHorizon,
			Date newHorizon) {
		this.dashboardLogic.addCalendarItemsForRepeatingCalendarItem(repeatingEvent, oldHorizon, newHorizon);
		
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#addCalendarLinks(java.lang.String, java.lang.String)
	 */
	@Override
	public void addCalendarLinks(String sakaiUserId, String contextId) {
		this.dashboardLogic.addCalendarLinks(sakaiUserId, contextId);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#modifyLinksByContext( java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public void modifyLinksByContext(String contextId, String type, boolean addOrRemove) {
		this.dashboardLogic.modifyLinksByContext(contextId, type, addOrRemove);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#addNewsLinks(java.lang.String, java.lang.String)
	 */
	@Override
	public void addNewsLinks(String sakaiUserId, String contextId) {
		
		this.dashboardLogic.addNewsLinks(sakaiUserId, contextId);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#checkTaskLock(java.lang.String)
	 */
	public boolean checkTaskLock(String task) {
		return this.dashboardLogic.checkTaskLock(task);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#createCalendarItem(java.lang.String, java.util.Date, java.lang.String, java.lang.String, org.sakaiproject.dash.model.Context, org.sakaiproject.dash.model.SourceType, java.lang.String, org.sakaiproject.dash.model.RepeatingCalendarItem, java.lang.Integer)
	 */
	@Override
	public CalendarItem createCalendarItem(String title, Date calendarTime,
			String calendarTimeLabelKey, String entityReference,
			Context context, SourceType sourceType, String subtype,
			RepeatingCalendarItem repeatingCalendarItem, Integer sequenceNumber) {
		
		return this.dashboardLogic.createCalendarItem(title, calendarTime, calendarTimeLabelKey, entityReference, 
				context, sourceType, subtype, repeatingCalendarItem, sequenceNumber);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#createCalendarLinks(org.sakaiproject.dash.model.CalendarItem)
	 */
	@Override
	public void createCalendarLinks(CalendarItem calendarItem) {
		
		this.dashboardLogic.createCalendarLinks(calendarItem);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#createContext(java.lang.String)
	 */
	@Override
	public Context createContext(String contextId) {
		
		return this.dashboardLogic.createContext(contextId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#createNewsItem(java.lang.String, java.util.Date, java.lang.String, java.lang.String, org.sakaiproject.dash.model.Context, org.sakaiproject.dash.model.SourceType, java.lang.String)
	 */
	@Override
	public NewsItem createNewsItem(String title, Date newsTime,
			String labelKey, String entityReference, Context context,
			SourceType sourceType, String subtype) {
		
		return this.dashboardLogic.createNewsItem(title, newsTime, labelKey, entityReference, context, sourceType, subtype);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#createNewsLinks(org.sakaiproject.dash.model.NewsItem)
	 */
	@Override
	public void createNewsLinks(NewsItem newsItem) {
		
		this.dashboardLogic.createNewsLinks(newsItem);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#createRepeatingCalendarItem(java.lang.String, java.util.Date, java.util.Date, java.lang.String, java.lang.String, org.sakaiproject.dash.model.Context, org.sakaiproject.dash.model.SourceType, java.lang.String, int)
	 */
	@Override
	public RepeatingCalendarItem createRepeatingCalendarItem(String title,
			Date firstTime, Date lastTime, String calendarTimeLabelKey,
			String entityReference, Context context, SourceType sourceType,
			String frequency, int count) {
		
		return this.dashboardLogic.createRepeatingCalendarItem(title, firstTime, lastTime, calendarTimeLabelKey, 
				entityReference, context, sourceType, frequency, count);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#createSourceType(java.lang.String)
	 */
	@Override
	public SourceType createSourceType(String resourceTypeIdentifier) {
		
		return this.dashboardLogic.createSourceType(resourceTypeIdentifier);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getCalendarItem(long)
	 */
	@Override
	public CalendarItem getCalendarItem(long id) {
		
		return this.dashboardLogic.getCalendarItem(id);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getCalendarItem(java.lang.String, java.lang.String, java.lang.Integer)
	 */
	@Override
	public CalendarItem getCalendarItem(String entityReference,
			String calendarTimeLabelKey, Integer sequenceNumber) {
		
		return this.dashboardLogic.getCalendarItem(entityReference, calendarTimeLabelKey, sequenceNumber);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getCalendarLink(java.lang.Long)
	 */
	@Override
	public CalendarLink getCalendarLink(Long id) {
		
		return this.dashboardLogic.getCalendarLink(id);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getContext(java.lang.String)
	 */
	@Override
	public Context getContext(String contextId) {
		
		return this.dashboardLogic.getContext(contextId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getDashboardEntityInfo(java.lang.String)
	 */
	public DashboardEntityInfo getDashboardEntityInfo(String Identifier) {
		
		return this.dashboardLogic.getDashboardEntityInfo(Identifier);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getEventProcessor(java.lang.String)
	 */
	public EventProcessor getEventProcessor(String eventIdentifier) {
		
		return this.dashboardLogic.getEventProcessor(eventIdentifier);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getFutureSequnceNumbers(java.lang.String, java.lang.String, java.lang.Integer)
	 */
	@Override
	public SortedSet<Integer> getFutureSequnceNumbers(String entityReference,
			String calendarTimeLabelKey, Integer firstSequenceNumber) {
		
		return this.dashboardLogic.getFutureSequnceNumbers(entityReference, calendarTimeLabelKey, firstSequenceNumber);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getNewsItem(long)
	 */
	@Override
	public NewsItem getNewsItem(long id) {
		
		return this.dashboardLogic.getNewsItem(id);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getNewsItem(java.lang.String)
	 */
	@Override
	public NewsItem getNewsItem(String entityReference) {
		
		return this.dashboardLogic.getNewsItem(entityReference);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getRepeatingCalendarItem(java.lang.String, java.lang.String)
	 */
	@Override
	public RepeatingCalendarItem getRepeatingCalendarItem(
			String entityReference, String calendarTimeLabelKey) {
		
		return this.dashboardLogic.getRepeatingCalendarItem(entityReference, calendarTimeLabelKey);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getRepeatingEventHorizon()
	 */
	@Override
	public Date getRepeatingEventHorizon() {
		
		return this.dashboardLogic.getRepeatingEventHorizon();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#getSourceType(java.lang.String)
	 */
	@Override
	public SourceType getSourceType(String identifier) {
		
		return this.dashboardLogic.getSourceType(identifier);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#isAvailable(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean isAvailable(String entityReference, String entityTypeId) {
		
		return this.dashboardLogic.isAvailable(entityReference, entityTypeId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#registerEntityType(org.sakaiproject.dash.entity.DashboardEntityInfo)
	 */
	@Override
	public void registerEntityType(DashboardEntityInfo dashboardEntityInfo) {
		
		this.dashboardLogic.registerEntityType(dashboardEntityInfo);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#registerEventProcessor(org.sakaiproject.dash.listener.EventProcessor)
	 */
	@Override
	public void registerEventProcessor(EventProcessor eventProcessor) {
		
		this.dashboardLogic.registerEventProcessor(eventProcessor);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#removeAllScheduledAvailabilityChecks(java.lang.String)
	 */
	@Override
	public void removeAllScheduledAvailabilityChecks(String entityReference) {
		
		this.dashboardLogic.removeAllScheduledAvailabilityChecks(entityReference);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#removeCalendarItem(java.lang.String, java.lang.String, java.lang.Integer)
	 */
	@Override
	public void removeCalendarItem(String entityReference,
			String calendarTimeLabelKey, Integer sequenceNumber) {
		
		this.dashboardLogic.removeCalendarItem(entityReference, calendarTimeLabelKey, sequenceNumber);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#removeCalendarItems(java.lang.String)
	 */
	@Override
	public void removeCalendarItems(String entityReference) {
		
		this.dashboardLogic.removeCalendarItems(entityReference);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#removeCalendarLinks(java.lang.String)
	 */
	@Override
	public void removeCalendarLinks(String entityReference) {
		
		this.dashboardLogic.removeCalendarLinks(entityReference);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#removeCalendarLinks(java.lang.String, java.lang.String)
	 */
	@Override
	public void removeCalendarLinks(String sakaiUserId, String contextId) {
		
		this.dashboardLogic.removeCalendarLinks(sakaiUserId, contextId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#removeCalendarLinks(java.lang.String, java.lang.String, int)
	 */
	@Override
	public void removeCalendarLinks(String entityReference,
			String calendarTimeLabelKey, int sequenceNumber) {
		
		this.dashboardLogic.removeCalendarLinks(entityReference, calendarTimeLabelKey, sequenceNumber);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#removeNewsItem(java.lang.String)
	 */
	@Override
	public void removeNewsItem(String entityReference) {
		
		this.dashboardLogic.removeNewsItem(entityReference);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#removeNewsLinks(java.lang.String)
	 */
	@Override
	public void removeNewsLinks(String entityReference) {
		
		this.dashboardLogic.removeNewsLinks(entityReference);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#removeNewsLinks(java.lang.String, java.lang.String)
	 */
	@Override
	public void removeNewsLinks(String sakaiUserId, String contextId) {
		
		this.dashboardLogic.removeNewsLinks(sakaiUserId, contextId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#reviseCalendarItemsLabelKey(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void reviseCalendarItemsLabelKey(String entityReference,
			String oldLabelKey, String newLabelKey) {
		
		this.dashboardLogic.reviseCalendarItemsLabelKey(entityReference, oldLabelKey, newLabelKey);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#reviseCalendarItemsTime(java.lang.String, java.util.Date)
	 */
	@Override
	public void reviseCalendarItemsTime(String entityReference, Date newTime) {
		
		this.dashboardLogic.reviseCalendarItemsTime(entityReference, newTime);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#reviseCalendarItemsTitle(java.lang.String, java.lang.String)
	 */
	@Override
	public void reviseCalendarItemsTitle(String entityReference, String newTitle) {
		
		this.dashboardLogic.reviseCalendarItemsTitle(entityReference, newTitle);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#reviseCalendarItemTime(java.lang.String, java.lang.String, java.lang.Integer, java.util.Date)
	 */
	@Override
	public void reviseCalendarItemTime(String entityReference, String labelKey,
			Integer sequenceNumber, Date newDate) {
		
		this.dashboardLogic.reviseCalendarItemTime(entityReference, labelKey, sequenceNumber, newDate);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#reviseNewsItemTime(java.lang.String, java.util.Date, java.lang.String)
	 */
	@Override
	public void reviseNewsItemTime(String entityReference, Date newTime,
			String newGroupingIdentifier) {
		
		this.dashboardLogic.reviseNewsItemTime(entityReference, newTime, newGroupingIdentifier);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#reviseNewsItemTitle(java.lang.String, java.lang.String, java.util.Date, java.lang.String, java.lang.String)
	 */
	@Override
	public void reviseNewsItemTitle(String entityReference, String newTitle,
			Date newNewsTime, String newLabelKey, String newGroupingIdentifier) {
		
		this.dashboardLogic.reviseNewsItemTitle(entityReference, newTitle, newNewsTime, newLabelKey, newGroupingIdentifier);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#reviseRepeatingCalendarItemFrequency(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean reviseRepeatingCalendarItemFrequency(String entityReference,
			String frequency) {
		
		return this.dashboardLogic.reviseRepeatingCalendarItemFrequency(entityReference, frequency);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#reviseRepeatingCalendarItemsLabelKey(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void reviseRepeatingCalendarItemsLabelKey(String entityReference,
			String oldType, String newType) {
		
		this.dashboardLogic.reviseRepeatingCalendarItemsLabelKey(entityReference, oldType, newType);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#reviseRepeatingCalendarItemTime(java.lang.String, java.util.Date, java.util.Date)
	 */
	@Override
	public void reviseRepeatingCalendarItemTime(String entityReference,
			Date newFirstTime, Date newLastTime) {
		
		this.dashboardLogic.reviseRepeatingCalendarItemTime(entityReference, newFirstTime, newLastTime);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#reviseRepeatingCalendarItemTitle(java.lang.String, java.lang.String)
	 */
	@Override
	public void reviseRepeatingCalendarItemTitle(String entityReference,
			String newTitle) {
		
		this.dashboardLogic.reviseRepeatingCalendarItemTitle(entityReference, newTitle);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#scheduleAvailabilityCheck(java.lang.String, java.lang.String, java.util.Date)
	 */
	@Override
	public void scheduleAvailabilityCheck(String entityReference,
			String entityTypeId, Date scheduledTime) {
		
		this.dashboardLogic.scheduleAvailabilityCheck(entityReference, entityTypeId, scheduledTime);
	}
	
	@Override
	public void updateScheduleAvailabilityCheck(String entityReference, String entityTypeId, Date scheduledTime) {
		this.dashboardLogic.updateScheduleAvailabilityCheck(entityReference, entityTypeId, scheduledTime);
	}
	
	@Override
	public boolean isScheduleAvailabilityCheckMade(String entityReference, String entityTypeId, Date scheduledTime) {
		return this.dashboardLogic.isScheduleAvailabilityCheckMade(entityReference, entityTypeId, scheduledTime);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#setRepeatingEventHorizon(java.util.Date)
	 */
	public void setRepeatingEventHorizon(Date newHorizon) {
		this.dashboardLogic.setRepeatingEventHorizon(newHorizon);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#updateCalendarLinks(java.lang.String)
	 */
	@Override
	public void updateCalendarLinks(String entityReference) {
		
		this.dashboardLogic.updateCalendarLinks(entityReference);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#updateNewsLinks(java.lang.String)
	 */
	@Override
	public void updateNewsLinks(String entityReference) {
		
		this.dashboardLogic.updateNewsLinks(entityReference);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#updateTaskLock(java.lang.String)
	 */
	public void updateTaskLock(String task) {
		this.dashboardLogic.updateTaskLock(task);
		
	}


	
	/************************************************************************
	 * DashboardUserLogic methods
	 ************************************************************************/

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardUserLogic#countNewsLinksByGroupId(java.lang.String, java.lang.String)
	 */
	@Override
	public int countNewsLinksByGroupId(String sakaiUserId, String groupId) {
		
		return this.dashboardUserLogic.countNewsLinksByGroupId(sakaiUserId, groupId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardUserLogic#getCurrentNewsLinks(java.lang.String, java.lang.String)
	 */
	@Override
	public List<NewsLink> getCurrentNewsLinks(String sakaiUserId,
			String contextId) {
		
		return this.dashboardUserLogic.getCurrentNewsLinks(sakaiUserId, contextId);
	}
	
	public List<NewsLink> getCurrentNewsLinks(String sakaiUserId,String contextId, boolean includeInfoLinkUrl) {
		
		return this.dashboardUserLogic.getCurrentNewsLinks(sakaiUserId, contextId, includeInfoLinkUrl);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardUserLogic#getFutureCalendarLinks(java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public List<CalendarLink> getFutureCalendarLinks(String sakaiUserId,
			String contextId, boolean hidden) {
		
		return this.dashboardUserLogic.getFutureCalendarLinks(sakaiUserId, contextId, hidden);
	}
	
	public List<CalendarLink> getFutureCalendarLinks(String sakaiUserId,
			String contextId, boolean hidden, boolean includeInfoLinkUrl) {
		
		return this.dashboardUserLogic.getFutureCalendarLinks(sakaiUserId, contextId, hidden,includeInfoLinkUrl);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardUserLogic#getHiddenNewsLinks(java.lang.String, java.lang.String)
	 */
	@Override
	public List<NewsLink> getHiddenNewsLinks(String sakaiUserId, String siteId) {
		
		return this.dashboardUserLogic.getHiddenNewsLinks(sakaiUserId, siteId);
	}
	
	public List<NewsLink> getHiddenNewsLinks(String sakaiUserId, String siteId, boolean includeInfoLinkUrl) {
		
		return this.dashboardUserLogic.getHiddenNewsLinks(sakaiUserId, siteId, includeInfoLinkUrl);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardUserLogic#getNewsLinksByGroupId(java.lang.String, java.lang.String, int, int)
	 */
	@Override
	public List<NewsLink> getNewsLinksByGroupId(String sakaiUserId,
			String groupId, int limit, int offset) {
		
		return this.dashboardUserLogic.getNewsLinksByGroupId(sakaiUserId, groupId, limit, offset);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardUserLogic#getPastCalendarLinks(java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public List<CalendarLink> getPastCalendarLinks(String sakaiUserId,
			String contextId, boolean hidden) {
		
		return this.dashboardUserLogic.getPastCalendarLinks(sakaiUserId, contextId, hidden);
	}
	
	public List<CalendarLink> getPastCalendarLinks(String sakaiUserId,
			String contextId, boolean hidden, boolean includeInfoLinkUrl) {
		
		return this.dashboardUserLogic.getPastCalendarLinks(sakaiUserId, contextId, hidden, includeInfoLinkUrl);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardUserLogic#getStarredCalendarLinks(java.lang.String, java.lang.String)
	 */
	@Override
	public List<CalendarLink> getStarredCalendarLinks(String sakaiUserId,
			String contextId) {
		
		return this.dashboardUserLogic.getStarredCalendarLinks(sakaiUserId, contextId);
	}
	
	public List<CalendarLink> getStarredCalendarLinks(String sakaiUserId,
			String contextId, boolean includeInfoLinkUrl) {
		
		return this.dashboardUserLogic.getStarredCalendarLinks(sakaiUserId, contextId, includeInfoLinkUrl);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardUserLogic#getStarredNewsLinks(java.lang.String, java.lang.String)
	 */
	@Override
	public List<NewsLink> getStarredNewsLinks(String sakaiUserId, String siteId) {
		
		return this.dashboardUserLogic.getStarredNewsLinks(sakaiUserId, siteId);
	}
	
	public List<NewsLink> getStarredNewsLinks(String sakaiUserId, String siteId,boolean includeInfoLinkUrl) {
		
		return this.dashboardUserLogic.getStarredNewsLinks(sakaiUserId, siteId, includeInfoLinkUrl);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardUserLogic#hideCalendarItem(java.lang.String, long)
	 */
	@Override
	public boolean hideCalendarItem(String sakaiUserId, long calendarItemId) {
		
		return this.dashboardUserLogic.hideCalendarItem(sakaiUserId, calendarItemId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardUserLogic#hideNewsItem(java.lang.String, long)
	 */
	@Override
	public boolean hideNewsItem(String sakaiUserId, long newsItemId) {
		
		return this.dashboardUserLogic.hideNewsItem(sakaiUserId, newsItemId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardUserLogic#keepCalendarItem(java.lang.String, long)
	 */
	@Override
	public boolean keepCalendarItem(String sakaiUserId, long calendarItemId) {
		
		return this.dashboardUserLogic.keepCalendarItem(sakaiUserId, calendarItemId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardUserLogic#keepNewsItem(java.lang.String, long)
	 */
	@Override
	public boolean keepNewsItem(String sakaiUserId, long newsItemId) {
		
		return this.dashboardUserLogic.keepNewsItem(sakaiUserId, newsItemId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardUserLogic#unhideCalendarItem(java.lang.String, long)
	 */
	@Override
	public boolean unhideCalendarItem(String sakaiUserId, long calendarItemId) {
		
		return this.dashboardUserLogic.unhideCalendarItem(sakaiUserId, calendarItemId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardUserLogic#unhideNewsItem(java.lang.String, long)
	 */
	@Override
	public boolean unhideNewsItem(String sakaiUserId, long newsItemId) {
		
		return this.dashboardUserLogic.unhideNewsItem(sakaiUserId, newsItemId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardUserLogic#unkeepCalendarItem(java.lang.String, long)
	 */
	@Override
	public boolean unkeepCalendarItem(String sakaiUserId, long calendarItemId) {
		
		return this.dashboardUserLogic.unkeepCalendarItem(sakaiUserId, calendarItemId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardUserLogic#unkeepNewsItem(java.lang.String, long)
	 */
	@Override
	public boolean unkeepNewsItem(String sakaiUserId, long newsItemId) {
		
		return this.dashboardUserLogic.unkeepNewsItem(sakaiUserId, newsItemId);
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.dash.logic.DashboardLogic#removeTaskLocks(java.lang.String)
	 */
	public void removeTaskLocks(String task) {
		this.dashboardLogic.removeTaskLocks(task);
	}

	/**
	 * this is to be called from Quartz job
	 */
	public void expireAndPurge() {
		expireAndPurge(false);
	}
	
	public void expireAndPurge(boolean taskLockApproach) {
		if((taskLockApproach && System.currentTimeMillis() > nextTimeToExpireAndPurge)
			|| !taskLockApproach) {
			SecurityAdvisor advisor = getDashboardSecurityAdvisor();
			sakaiProxy.pushSecurityAdvisor(advisor);
			try {
				long startTime = System.currentTimeMillis();
				logger.debug("DashboardCommonLogicImpl.expireAndPurge start " + serverId);
				expireAndPurgeCalendarItems();
				expireAndPurgeNewsItems();
				
				nextTimeToExpireAndPurge = System.currentTimeMillis() + TIME_BETWEEN_EXPIRING_AND_PURGING;
	
				if (taskLockApproach)
				{
					dashboardLogic.updateTaskLock(TaskLock.EXPIRE_AND_PURGE_OLD_DASHBOARD_ITEMS);
				}
	
				long elapsedTime = System.currentTimeMillis() - startTime;
				StringBuilder buf = new StringBuilder("DashboardCommonLogicImpl.expireAndPurge done. ");
				buf.append(serverId);
				buf.append(" Elapsed Time (ms): ");
				buf.append(elapsedTime);
				logger.debug(buf.toString());
			} catch (Exception e) {
				logger.warn(this + " expireAndPurge: ", e);
			} finally {
				sakaiProxy.popSecurityAdvisor(advisor);
			}
		}
		
	}

	protected void expireAndPurgeNewsItems() {
		Integer weeksToExpireItems = dashboardConfig.getConfigValue(DashboardConfig.PROP_REMOVE_NEWS_ITEMS_AFTER_WEEKS, DEFAULT_NEWS_ITEM_EXPIRATION);
		Integer weeksToExpireStarredItems = dashboardConfig.getConfigValue(DashboardConfig.PROP_REMOVE_STARRED_NEWS_ITEMS_AFTER_WEEKS, DEFAULT_NEWS_ITEM_EXPIRATION);
		Integer weeksToExpireHiddenItems = dashboardConfig.getConfigValue(DashboardConfig.PROP_REMOVE_HIDDEN_NEWS_ITEMS_AFTER_WEEKS, DEFAULT_NEWS_ITEM_EXPIRATION);
		Integer purgeItemsWithoutLinks = dashboardConfig.getConfigValue(DashboardConfig.PROP_REMOVE_NEWS_ITEMS_WITH_NO_LINKS, 0);
		
		if(weeksToExpireItems.intValue() > 0) {
			expireNewsLinks(new Date(System.currentTimeMillis() - weeksToExpireItems.intValue() * ONE_WEEK_IN_MILLIS), false, false);
		}
		if(weeksToExpireStarredItems.intValue() > 0) {
			expireNewsLinks(new Date(System.currentTimeMillis() - weeksToExpireStarredItems.intValue() * ONE_WEEK_IN_MILLIS), false, false);
		}
		if(weeksToExpireHiddenItems.intValue() > 0) {
			expireNewsLinks(new Date(System.currentTimeMillis() - weeksToExpireHiddenItems.intValue() * ONE_WEEK_IN_MILLIS), false, true);
		}
		if(purgeItemsWithoutLinks.intValue() > 0) {
			purgeNewsItems();
		}
	}

	@Transactional
	private void purgeNewsItems() {
		dao.deleteNewsItemsWithoutLinks();
	}

	@Transactional
	protected void expireNewsLinks(Date expireBefore, boolean starred, boolean hidden) {
		dao.deleteNewsLinksBefore(expireBefore,starred,hidden);
		
	}

	protected void expireAndPurgeCalendarItems() {
		Integer weeksToExpireItems = dashboardConfig.getConfigValue(DashboardConfig.PROP_REMOVE_CALENDAR_ITEMS_AFTER_WEEKS, DEFAULT_CALENDAR_ITEM_EXPIRATION);
		Integer weeksToExpireStarredItems = dashboardConfig.getConfigValue(DashboardConfig.PROP_REMOVE_STARRED_CALENDAR_ITEMS_AFTER_WEEKS, DEFAULT_CALENDAR_ITEM_EXPIRATION);
		Integer weeksToExpireHiddenItems = dashboardConfig.getConfigValue(DashboardConfig.PROP_REMOVE_HIDDEN_CALENDAR_ITEMS_AFTER_WEEKS, DEFAULT_CALENDAR_ITEM_EXPIRATION);
		Integer purgeItemsWithoutLinks = dashboardConfig.getConfigValue(DashboardConfig.PROP_REMOVE_CALENDAR_ITEMS_WITH_NO_LINKS, 0);

		if(weeksToExpireItems.intValue() > 0) {
			expireCalendarLinks(new Date(System.currentTimeMillis() - weeksToExpireItems.intValue() * ONE_WEEK_IN_MILLIS), false, false);
		}
		if(weeksToExpireStarredItems.intValue() > 0) {
			expireCalendarLinks(new Date(System.currentTimeMillis() - weeksToExpireStarredItems.intValue() * ONE_WEEK_IN_MILLIS), false, false);
		}
		if(weeksToExpireHiddenItems.intValue() > 0) {
			expireCalendarLinks(new Date(System.currentTimeMillis() - weeksToExpireHiddenItems.intValue() * ONE_WEEK_IN_MILLIS), false, true);
		}
		if(purgeItemsWithoutLinks.intValue() > 0) {
			purgeCalendarItems();
		}
	}

	@Transactional
	private void purgeCalendarItems() {
		dao.deleteCalendarItemsWithoutLinks();
		
	}

	@Transactional
	protected void expireCalendarLinks(Date expireBefore, boolean starred, boolean hidden) {
		dao.deleteCalendarLinksBefore(expireBefore, starred, hidden);
	}

	/**
	 * 
	 */
	public void updateRepeatingEvents(boolean taskLockApproach) {
		if((taskLockApproach && nextHorizonUpdate != null && System.currentTimeMillis() > nextHorizonUpdate.getTime())
			|| !taskLockApproach)
		{
			SecurityAdvisor advisor = getDashboardSecurityAdvisor();
			sakaiProxy.pushSecurityAdvisor(advisor);
			try {
				long startTime = System.currentTimeMillis();
				logger.debug("DashboardCommonLogicImpl.updateRepeatingEvents start " + serverId);
	
				// time to update
				Date oldHorizon = dashboardLogic.getRepeatingEventHorizon();
				Integer weeksToHorizon = dashboardConfig.getConfigValue(DashboardConfig.PROP_WEEKS_TO_HORIZON, new Integer(4));
				Date newHorizon = new Date(System.currentTimeMillis() + weeksToHorizon * 7L * DashboardLogic.ONE_DAY);
				dashboardLogic.setRepeatingEventHorizon(newHorizon);
				
				if(newHorizon.after(oldHorizon)) {
					List<RepeatingCalendarItem> repeatingEvents = dao.getRepeatingCalendarItems();
					if(repeatingEvents != null) {
						logger.debug("DashboardCommonLogicImpl.updateRepeatingEvents repeatingEvent list size=" + repeatingEvents.size() + " new horizon=" + newHorizon + " oldHorizon=" + oldHorizon);
						int count = 0;
						for(RepeatingCalendarItem repeatingEvent: repeatingEvents) {
							addCalendarItemsForRepeatingCalendarItem(repeatingEvent, oldHorizon, newHorizon);
							count++;
							if (count % TASK_LOGGING_INTERVAL == 0)
							{
								// log progress in every TASK_LOGGING_INTERVAL tasks
								logger.debug("DashboardCommonLogicImpl.updateRepeatingEvents processed " + count  + " repeating events. "); 
							}
						}
						logger.debug("DashboardCommonLogicImpl.updateRepeatingEvents end of the loop processed " + count  + " repeating events. ");
					}
				}
				Integer daysBetweenHorizonUpdates = dashboardConfig.getConfigValue(DashboardConfig.PROP_DAYS_BETWEEN_HORIZ0N_UPDATES, new Integer(1));
				nextHorizonUpdate = new Date(nextHorizonUpdate.getTime() + daysBetweenHorizonUpdates.longValue() * DashboardLogic.ONE_DAY);
				
				if (taskLockApproach)
				{
					dashboardLogic.updateTaskLock(TaskLock.UPDATE_REPEATING_EVENTS);
				}
				
				long elapsedTime = System.currentTimeMillis() - startTime;
				StringBuilder buf = new StringBuilder("DashboardCommonLogicImpl.updateRepeatingEvents done. ");
				buf.append(serverId);
				buf.append(" Elapsed Time (ms): ");
				buf.append(elapsedTime);
				logger.debug(buf.toString());
			} catch (Exception e) {
				logger.warn(this + " updateRepeatingEvents: ", e);
			} finally {
				sakaiProxy.popSecurityAdvisor(advisor);
			}
		}
	}
	
	/**
	 * This is to be called from Quartz Job
	 */
	public void updateRepeatingEvents() {
		updateRepeatingEvents(false);
	}
	
	public void checkForAdminChanges() {
		// check for change in loopTimerEnabled
		Integer enabled = dao.getConfigProperty(DashboardConfig.PROP_LOOP_TIMER_ENABLED);
		if(enabled == null || enabled.intValue() == 0) {
			if(loopTimerEnabled) {
				logger.debug("DashboardEventProcessingThread.checkForAdminChanges loopTimerEnabled changed to false");
			}
			loopTimerEnabled = false;
		} else {
			if(! loopTimerEnabled) {
				logger.debug("DashboardEventProcessingThread.checkForAdminChanges loopTimerEnabled changed to true");
			}
			loopTimerEnabled = true;
		}
		
		if(! loopTimerEnabled) {
			enabled = dao.getConfigProperty(propLoopTimerEnabledLocally);
			if(enabled != null && enabled.intValue() > 0) {
				logger.debug("DashboardEventProcessingThread.checkForAdminChanges loopTimerEnabled changed to true for " + serverId);
				loopTimerEnabled = true;
			}
		}
		
		// set the loopTimerEnabledLocal property to false if it's not already set
		Integer enabledLocal = dao.getConfigProperty(propLoopTimerEnabledLocally);
		if(enabledLocal == null) {
			dao.setConfigProperty(propLoopTimerEnabledLocally, 0);
		}

		
		// TODO: move other admin checks here
	}
	
	private SecurityAdvisor getDashboardSecurityAdvisor()
	{
		return new SecurityAdvisor() {
			public SecurityAdvice isAllowed(String userId, String function, String reference) {
				long threadId = Thread.currentThread().getId();

				if(threadId == dashboardEventProcessorThreadId) {
					// calling from the dashboard thread
					return SecurityAdvice.ALLOWED;
				}
				else
				{
					// calling from Quartz Job
					if (sakaiProxy.isOfDashboardRelatedPermissions(function))
					{
						return SecurityAdvice.ALLOWED;
					}
					else
					{
						return SecurityAdvice.PASS;
					}
				}
			}
		};
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void syncDashboardUsersWithSiteUsers()
	{
		logger.info(this + ".syncDashboardUsersWithSiteUsers start " + serverId);
		
		HashMap<String, Set<String>> calendarLinksUserMap = dao.getDashboardCalendarContextUserMap();
		HashMap<String, Set<String>> newsLinksUserMap =  dao.getDashboardNewsContextUserMap();

		// combine the context id from keyset of both maps
		// so that we just need to call AuthzGroupService once to find out site members
		Set<String> combinedContextIdSet = new HashSet<String>();
		combinedContextIdSet.addAll(calendarLinksUserMap.keySet());
		combinedContextIdSet.addAll(newsLinksUserMap.keySet());
		
		logger.info(this + ".syncDashboardUsersWithSiteUsers total site set size " + combinedContextIdSet.size());

		// now that we have a hashmap, we will check the current site member list
		for(String context_id: combinedContextIdSet)
		{
			HashSet<String> siteUserSet = new HashSet<String>();
			Collection<String> siteMembersCollection = getSiteUserIdList(context_id);
			if (siteMembersCollection != null)
			{
				siteUserSet = new HashSet<String>(siteMembersCollection);
			}
			
			// remove or add user DashboardCalendarlinks if needed
			if (calendarLinksUserMap.containsKey(context_id))
			{
				addOrRemoveDashboardLinksBasedOnUsersSetComp(context_id, calendarLinksUserMap.get(context_id), siteUserSet, CALENDAR_LINK_TYPE);
			}
			
			// remove or add user DashboardNewslinks if needed
			if (newsLinksUserMap.containsKey(context_id))
			{
				addOrRemoveDashboardLinksBasedOnUsersSetComp(context_id, newsLinksUserMap.get(context_id), siteUserSet, NEWS_LINK_TYPE);
			}
		}
		logger.info(this + ".syncDashboardUsersWithSiteUsers end " + serverId);
	}
	
	
	/**
	 * Returns the site user id list
	 *   
	 * @param siteId
	 * @return Collection of site user ids
	 */
	private Collection<String> getSiteUserIdList(String siteId)
	{
		HashSet<String> set = new HashSet<String>();
		set.add(sakaiProxy.getSiteReference(siteId));
		return this.authzGroupService.getAuthzUsersInGroups(set);
	}
	
	/**
	 * Compare two user sets (one from dashboard calendar/news links table, and the other from site membership)
	 * add or remove calendar/news links
	 * @context_id the site id
	 * @dashboardUserSet
	 * @siteUserSet
	 * @forCalendarLinks when true, add/remove in DASH_CALENDAR_LINK table; otherwise, add/remove in DASH_NEWS_LINK table
	 */
	private void addOrRemoveDashboardLinksBasedOnUsersSetComp(String context_id, Set<String> dashboardUserSet, Set<String> siteUserSet, String linkType)
	{
		// construct two base set, one for remove user links, one for add user links
		Set<String> removeSet = new HashSet<String>();
		removeSet.addAll(dashboardUserSet);
		Set<String> addSet = new HashSet<String>();
		addSet.addAll(siteUserSet);
		
		// now we have two user sets: 
		// one is from the current dashboard user record
		// the other is from the current site member list
		// need to do the comparison between those two sets: 
		// 1. add dashboard links if the user is added to site; 
		addSet.removeAll(dashboardUserSet);
		if (!addSet.isEmpty())
			logger.info(this + " addOrRemoveDashboardLinksBasedOnUsersSetComp add dash link user set size=" + addSet.size() + " for context " + context_id);
		for(String userId: addSet)
		{	
			if (CALENDAR_LINK_TYPE.equals(linkType))
			{
				addCalendarLinks(userId, context_id);
				logger.debug(this + ".syncDashboardUsersWithSiteUsers ADD calendar links for user= " + userId + " context_id=" + context_id);
			}
			else if (NEWS_LINK_TYPE.equals(linkType))
			{
				addNewsLinks(userId, context_id);
				logger.debug(this + ".syncDashboardUsersWithSiteUsers ADD news links for user= " + userId + " context_id=" + context_id);
			}
		}
		// 2. remove dashboard links if the user is removed from the site
		removeSet.removeAll(siteUserSet);
		if (!removeSet.isEmpty())
			logger.info(this + " addOrRemoveDashboardLinksBasedOnUsersSetComp remove dash link user set size=" + removeSet.size()+ " for context " + context_id);
		for(String userId: removeSet)
		{
			if (CALENDAR_LINK_TYPE.equals(linkType))
			{
				removeCalendarLinks(userId, context_id);
				logger.debug(this + ".syncDashboardUsersWithSiteUsers REMOVE calendar links for user= " + userId + " context_id=" + context_id);
			}
			else if (NEWS_LINK_TYPE.equals(linkType))
			{
				removeNewsLinks(userId, context_id);
				logger.debug(this + ".syncDashboardUsersWithSiteUsers REMOVE news links for user= " + userId + " context_id=" + context_id);
			}
		}
	}
}
