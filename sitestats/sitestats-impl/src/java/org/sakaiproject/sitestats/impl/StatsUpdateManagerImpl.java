/**
/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *			   http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.*;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.parser.EventParserTip;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;


/**
 * @author <a href="mailto:nuno@ufp.pt">Nuno Fernandes</a>
 */
public class StatsUpdateManagerImpl extends HibernateDaoSupport implements Runnable, StatsUpdateManager, Observer, StatsUpdateManagerMXBean {
	private Logger								LOG									= LoggerFactory.getLogger(StatsUpdateManagerImpl.class);
	private final static String				PRESENCE_SUFFIX						= "-presence";
	private final static int				PRESENCE_SUFFIX_LENGTH				= PRESENCE_SUFFIX.length();

	/** Spring bean members */
	private boolean							collectThreadEnabled				= true;
	public long								collectThreadUpdateInterval			= 4000L;
	private boolean							collectAdminEvents					= false;
	private boolean							collectEventsForSiteWithToolOnly	= true;

	/** Sakai services */
	private StatsManager					M_sm;
	private EventRegistryService			M_ers;
	private SiteService						M_ss;
	private AliasService					M_as;
	private EntityManager					M_em;
	private UsageSessionService				M_uss;
	private EventTrackingService			M_ets;

	/** Collect Thread and Semaphore */
	private Thread							collectThread;
	private List<Event>						collectThreadQueue					= new ArrayList<Event>();
	private Object							collectThreadSemaphore				= new Object();
	private boolean							collectThreadRunning				= false;

	/** Collect thread queue maps */
	private Map<String, EventStat>					eventStatMap			= Collections.synchronizedMap(new HashMap<String, EventStat>());
	private Map<String, ResourceStat>				resourceStatMap			= Collections.synchronizedMap(new HashMap<String, ResourceStat>());
	private Map<String, LessonBuilderStat>			lessonBuilderStatMap	= Collections.synchronizedMap(new HashMap<String, LessonBuilderStat>());
	private Map<String, SiteActivity>				activityMap				= Collections.synchronizedMap(new HashMap<String, SiteActivity>());
	private Map<String, SiteVisits>					visitsMap				= Collections.synchronizedMap(new HashMap<String, SiteVisits>());
	private Map<String, SitePresenceConsolidation>	presencesMap			= Collections.synchronizedMap(new HashMap<String, SitePresenceConsolidation>());
	private Map<UniqueVisitsKey, Integer>			uniqueVisitsMap			= Collections.synchronizedMap(new HashMap<UniqueVisitsKey, Integer>());
	private Map<String, ServerStat>					serverStatMap			= Collections.synchronizedMap(new HashMap<String, ServerStat>());
	private Map<String, UserStat>					userStatMap				= Collections.synchronizedMap(new HashMap<String, UserStat>());

	private Map<String, String>				lessonPageCreateEventMap		=  new HashMap<String, String>();

	private boolean							initialized							= false;
	
	private final ReentrantLock				lock								= new ReentrantLock();
	
	/** Metrics */
	private boolean							isIdle								= true;
	private long							totalEventsProcessed				= 0;
	private long							totalTimeInEventProcessing			= 0;
	private long							resetTime					= System.currentTimeMillis();

	
	// ################################################################
	// Spring related methods
	// ################################################################	
	public void setCollectThreadEnabled(boolean enabled) {
		this.collectThreadEnabled = enabled;
		if(initialized) {
			if(enabled && !collectThreadRunning) {
				// start update thread
				startUpdateThread();
				
				// add this as EventInfo observer
				M_ets.addLocalObserver(this);
			}else if(!enabled && collectThreadRunning){
				// remove this as EventInfo observer
				M_ets.deleteObserver(this);	
				
				// stop update thread
				stopUpdateThread();
			}
		}
	}
	
	public boolean isCollectThreadEnabled() {
		return collectThreadEnabled;
	}
	
	public void setCollectThreadUpdateInterval(long dbUpdateInterval){
		this.collectThreadUpdateInterval = dbUpdateInterval;
	}
	
	public long getCollectThreadUpdateInterval(){
		return collectThreadUpdateInterval;
	}	
	
	public void setCollectAdminEvents(boolean value){
		this.collectAdminEvents = value;
	}

	public boolean isCollectAdminEvents(){
		return collectAdminEvents;
	}

	public void setCollectEventsForSiteWithToolOnly(boolean value){
		this.collectEventsForSiteWithToolOnly = value;
	}
	
	public boolean isCollectEventsForSiteWithToolOnly(){
		return collectEventsForSiteWithToolOnly;
	}
	
	public void setStatsManager(StatsManager mng){
		this.M_sm = mng;
	}

	public void setEventRegistryService(EventRegistryService eventRegistryService) {
		this.M_ers = eventRegistryService;
	}
	
	public void setSiteService(SiteService ss){
		this.M_ss = ss;
	}
	
	public void setAliasService(AliasService as) {
		this.M_as = as;
	}
	
	public void setEntityManager(EntityManager em) {
		this.M_em = em;
	}
	
	public void setEventTrackingService(EventTrackingService ets){
		this.M_ets = ets;
	}
	
	public void setUsageSessionService(UsageSessionService uss){
		this.M_uss = uss;
	}

	public void init(){
		StringBuilder buff = new StringBuilder();
		buff.append("init(): collect thread enabled: ");
		buff.append(collectThreadEnabled);
		if(collectThreadEnabled) {
			buff.append(", db update interval: ");
			buff.append(collectThreadUpdateInterval);
			buff.append(" ms");
		}
		buff.append(", collect administrator events: " + collectAdminEvents);
		buff.append(", collect events only for sites with SiteStats: " + collectEventsForSiteWithToolOnly);			
		logger.info(buff.toString());
		
		initialized = true;
		setCollectThreadEnabled(collectThreadEnabled);
	}
	
	public void destroy(){
		if(collectThreadEnabled) {
			// remove this as EventInfo observer
			M_ets.deleteObserver(this);	
			
			// stop update thread
			stopUpdateThread();
		}
	}

	
	// ################################################################
	// Public methods
	// ################################################################
	public Event buildEvent(Date date, String event, String ref, String sessionUser, String sessionId) {
		return new CustomEventImpl(date, event, ref, sessionUser, sessionId);
	}

	public Event buildEvent(Date date, String event, String ref, String context, String sessionUser, String sessionId) {
		return new CustomEventImpl(date, event, ref, context, sessionUser, sessionId);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsUpdateManager#collectEvent(org.sakaiproject.event.api.Event)
	 */
	public boolean collectEvent(Event e) {
		if(e != null) {
			long startTime = System.currentTimeMillis();
			isIdle = false;
			preProcessEvent(e);
			//long endTime = System.currentTimeMillis();
			//LOG.debug("Time spent pre-processing 1 event: " + (endTime-startTime) + " ms");
			boolean success = doUpdateConsolidatedEvents();
			isIdle = true;
			totalTimeInEventProcessing += (System.currentTimeMillis() - startTime);
			return success;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsUpdateManager#collectEvents(java.util.List)
	 */
	public boolean collectEvents(List<Event> events) {
		if(events != null) {
			int eventCount = events.size();
			if(eventCount > 0) {
				return collectEvents(events.toArray(new Event[eventCount]));
			}
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsUpdateManager#collectEvents(org.sakaiproject.event.api.Event[])
	 */
	public boolean collectEvents(Event[] events) {
		if(events != null) {
			int eventCount = events.length;
			if(eventCount > 0) {
				long startTime = System.currentTimeMillis();
				isIdle = false;
				for(int i=0; i<events.length; i++){
					if(events[i] != null) {
						preProcessEvent(events[i]);
					}
				}
				//long endTime = System.currentTimeMillis();
				//LOG.debug("Time spent pre-processing " + eventCount + " event(s): " + (endTime-startTime) + " ms");
				boolean success = doUpdateConsolidatedEvents();
				isIdle = true;
				totalTimeInEventProcessing += (System.currentTimeMillis() - startTime);
				return success;
			}
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsUpdateManager#collectPastSiteEvents(java.lang.String, java.util.Date, java.util.Date)
	 */
	public long collectPastSiteEvents(String siteId, Date initialDate, Date finalDate) {
		StatsAggregateJobImpl statsAggregateJob = (StatsAggregateJobImpl) ComponentManager.get("org.sakaiproject.sitestats.api.StatsAggregateJob");
		return statsAggregateJob.collectPastSiteEvents(siteId, initialDate, finalDate);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsUpdateManager#saveJobRun(org.sakaiproject.sitestats.api.JobRun)
	 */
	public boolean saveJobRun(final JobRun jobRun){
		if(jobRun == null) {
			return false;
		}
		Object r = getHibernateTemplate().execute(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Transaction tx = null;
				try{
					tx = session.beginTransaction();
					session.saveOrUpdate(jobRun);
					tx.commit();
				}catch(Exception e){
					if(tx != null) tx.rollback();
					LOG.warn("Unable to commit transaction: ", e);
					return Boolean.FALSE;
				}
				return Boolean.TRUE;
			}			
		});
		return ((Boolean) r).booleanValue();
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsUpdateManager#getLatestJobRun()
	 */
	public JobRun getLatestJobRun() throws Exception {
		Object r = getHibernateTemplate().execute(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				JobRun jobRun = null;
				Criteria c = session.createCriteria(JobRunImpl.class);
				c.setMaxResults(1);
				c.addOrder(Order.desc("id"));
				List jobs = c.list();
				if(jobs != null && jobs.size() > 0){
					jobRun = (JobRun) jobs.get(0);
				}
				return jobRun;
			}			
		});
		return (JobRun) r;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsUpdateManager#getEventDateFromLatestJobRun()
	 */
	public Date getEventDateFromLatestJobRun() throws Exception {
		Object r = getHibernateTemplate().execute(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Criteria c = session.createCriteria(JobRunImpl.class);
				c.add(Expression.isNotNull("lastEventDate"));
				c.setMaxResults(1);
				c.addOrder(Order.desc("id"));
				List jobs = c.list();
				if(jobs != null && jobs.size() > 0){
					JobRun jobRun = (JobRun) jobs.get(0);
					return jobRun.getLastEventDate();
				}
				return null;
			}			
		});
		return (Date) r;
	}
	
	
	// ################################################################
	// Metrics related methods
	// ################################################################	
	public int getQueueSize() {
		return collectThreadQueue.size();
	}
	
	public boolean isIdle() {
		return this.isIdle && getQueueSize() == 0;
	}
	
	public void resetMetrics() {
		totalEventsProcessed = 0;
		totalTimeInEventProcessing = 0;
		resetTime = System.currentTimeMillis();
	}
	
	@Override
	public long getNumberOfEventsProcessed() {
		return totalEventsProcessed;
	}
	
	@Override
	public long getTotalTimeInEventProcessing() {
		return totalTimeInEventProcessing;
	}
	
	@Override
	public long getResetTime() {
		return resetTime;
	}

	@Override
	public long getTotalTimeElapsedSinceReset() {
		return System.currentTimeMillis() - resetTime;
	}
	
	@Override
	public double getNumberOfEventsProcessedPerSec() {
		if(totalTimeInEventProcessing > 0) {
			return Util.round((double)totalEventsProcessed / ((double)totalTimeInEventProcessing/1000), 3);
		}else{
			return Util.round((double)totalEventsProcessed / 0.001, 3); // => will assume 1ms instead of 0ms
		}
	}
	
	@Override
	public double getNumberOfEventsGeneratedPerSec() {
		double ellapsed = (double) getTotalTimeElapsedSinceReset();
		if(ellapsed > 0) {
			return Util.round((double)totalEventsProcessed / (ellapsed/1000), 3);
		}else{
			return Util.round((double)totalEventsProcessed / 0.001, 3); // => will assume 1ms instead of 0ms
		}
	}
	
	@Override
	public long getAverageTimeInEventProcessingPerEvent() {
		if(totalEventsProcessed > 0) {
			return totalTimeInEventProcessing / totalEventsProcessed;
		}else{
			return 0;
		}
	}
	
	public String getMetricsSummary(boolean compact) {
		StringBuilder sb = new StringBuilder();
		if(!compact) {
			sb.append("SiteStats Event aggregation metrics summary:\n");
			sb.append("\t\tNumber of total events processed: ").append(getNumberOfEventsProcessed()).append("\n");
			sb.append("\t\tReset/init time: ").append(new Date(getResetTime())).append("\n");
			sb.append("\t\tTotal time ellapsed since reset: ").append(getTotalTimeElapsedSinceReset()).append(" ms\n");
			sb.append("\t\tTotal time spent processing events: ").append(getTotalTimeInEventProcessing()).append(" ms\n");
			sb.append("\t\tNumber of events processed per sec: ").append(getNumberOfEventsProcessedPerSec()).append("\n");
			sb.append("\t\tNumber of events genereated in Sakai per sec: ").append(getNumberOfEventsGeneratedPerSec()).append("\n");
			sb.append("\t\tAverage time spent in event processing per event: ").append(getAverageTimeInEventProcessingPerEvent()).append(" ms\n");
			sb.append("\t\tEvent queue size: ").append(getQueueSize()).append("\n");
			sb.append("\t\tIdle: ").append(isIdle());
		}else{
			sb.append("#Events processed: ").append(getNumberOfEventsProcessed()).append(", ");
			sb.append("Time ellapsed since reset: ").append(getTotalTimeElapsedSinceReset()).append(" ms, ");
			sb.append("Time spent processing events: ").append(getTotalTimeInEventProcessing()).append(" ms, ");
			sb.append("#Events processed/sec: ").append(getNumberOfEventsProcessedPerSec()).append(", ");
			sb.append("Avg. Time/event: ").append(getAverageTimeInEventProcessingPerEvent()).append(" ms, ");
			sb.append("Event queue size: ").append(getQueueSize()).append(", ");
			sb.append("Idle: ").append(isIdle());
		}
		return sb.toString();
	}
	

	// ################################################################
	// Update thread related methods
	// ################################################################	
	/** Method called whenever an new event is generated from EventTrackingService: do not call this method! */
	public void update(Observable obs, Object o) {
		// At the moment this isn't threadsafe, but as sakai event handling is single threaded this shoudn't be a problem,
		// but it's not a formal contract.
		if(o instanceof Event){
			Event e = (Event) o;
			Event eventWithPreciseDate = buildEvent(getToday(), e.getEvent(), e.getResource(), e.getContext(), e.getUserId(), e.getSessionId());
			collectThreadQueue.add(eventWithPreciseDate);
		}
	}
	
	/** Update thread: do not call this method! */
	public void run(){
		try{
			LOG.debug("Started statistics update thread");
			while(collectThreadRunning){
				// do update job
				isIdle = false;
				long startTime = System.currentTimeMillis();
				int eventCount = collectThreadQueue.size();
				if(eventCount > 0) {
					//long startTime2 = System.currentTimeMillis();
					while(collectThreadQueue.size() > 0){
						preProcessEvent(collectThreadQueue.remove(0));
					}
					//long endTime2 = System.currentTimeMillis();
					//LOG.debug("Time spent pre-processing " + eventCount + " event(s): " + (endTime2-startTime2) + " ms");
				}
				doUpdateConsolidatedEvents();
				isIdle = true;
				totalTimeInEventProcessing += (System.currentTimeMillis() - startTime);

				// sleep if no work to do
				if(!collectThreadRunning) break;
				try{
					synchronized (collectThreadSemaphore){
						collectThreadSemaphore.wait(collectThreadUpdateInterval);
					}
				}catch(InterruptedException e){
					LOG.warn("Failed to sleep statistics update thread",e);
				}
			}
		}catch(Throwable t){
			LOG.debug("Failed to execute statistics update thread", t);
		}finally{
			if(collectThreadRunning){
				// thread was stopped by an unknown error: restart
				LOG.debug("Statistics update thread was stoped by an unknown error: restarting...");
				startUpdateThread();
			}else
				LOG.debug("Finished statistics update thread");
		}
	}

	/** Start the update thread */
	private void startUpdateThread(){
		collectThreadRunning = true;
		collectThread = null;
		collectThread = new Thread(this, "org.sakaiproject.sitestats.impl.StatsUpdateManagerImpl");
		collectThread.start();
	}
	
	/** Stop the update thread */
	private void stopUpdateThread(){
		collectThreadRunning = false;
		synchronized (collectThreadSemaphore){
			collectThreadSemaphore.notifyAll();
		}
	}
	

	// ################################################################
	// Event process methods
	// ################################################################	
	private void preProcessEvent(Event event) {
		totalEventsProcessed++;
		String userId = event.getUserId();
		Event e = fixMalFormedEvents(event);
		if (e == null) {
			return;
		}
		if(isRegisteredEvent(e.getEvent()) && isValidEvent(e)){

			// site check
			String siteId = parseSiteId(e);
			if(siteId == null || M_ss.isUserSite(siteId) || M_ss.isSpecialSite(siteId)){
				return;
			}
			Site site = getSite(siteId);
			if(site == null) {
				return;
			}
			if(isCollectEventsForSiteWithToolOnly() && site.getToolForCommonId(StatsManager.SITESTATS_TOOLID) == null) {
				return;
			}
			
			// user check
			if(userId == null) {
				UsageSession session = M_uss.getSession(e.getSessionId());
				if(session != null) {
					userId = session.getUserId();
				}
			}
			if(!isCollectAdminEvents() && ("admin").equals(userId)){
				return;
			}if(!M_sm.isShowAnonymousAccessEvents() && ("?").equals(userId)){
				return;
			}
			
			// consolidate event
			Date date = null;
			if(e instanceof CustomEventImpl){
				date = ((CustomEventImpl) e).getDate();
			}else{
				date = getToday();
			}
			String eventId = e.getEvent();
			String resourceRef = e.getResource();
			
			if(userId == null || eventId == null || resourceRef == null)
				return;
			consolidateEvent(date, eventId, resourceRef, userId, siteId);
		} else if(getServerEvents().contains(e.getEvent()) && !isMyWorkspaceEvent(e)){
			
			//it's a server event
			if(LOG.isDebugEnabled()) {
				LOG.debug("Server event: "+e.toString());
			}
			
			String eventId = e.getEvent();
			
			if(eventId == null) {
				return;
			}
			Date date = new Date();
			
			consolidateServerEvent(date, eventId);
		} 
		
		//we do this separately as we want individual login stats as well as totals from the server stats section
		if (isUserLoginEvent(e)) {
			
			//it's a user event
			if(LOG.isDebugEnabled()) {
				LOG.debug("User event: "+e.toString());
			}
			
			// user check
			if(userId == null) {
				UsageSession session = M_uss.getSession(e.getSessionId());
				if(session != null) {
					userId = session.getUserId();
				}
			}
			
			if(userId == null) {
				return;
			}
			
			Date date = new Date();
			consolidateUserEvent(date, userId);
		}
		
		
		
		//else LOG.debug("EventInfo ignored:  '"+e.toString()+"' ("+e.toString()+") USER_ID: "+userId);
	}

	/**
     * This processes events and updates in-memory cache. This doesn't push those changes back to the
	 * DB yet.
	 *
	 * @param dateTime Can this be <code>null</code>?
	 */
	private void consolidateEvent(Date dateTime, String eventId, String resourceRef, String userId, String siteId) {
		if(eventId == null)
			return;

		Date date = getTruncatedDate(dateTime);
		// update		
		if(isRegisteredEvent(eventId) && !StatsManager.SITEVISITEND_EVENTID.equals(eventId)){
			// add to eventStatMap
			String key = userId+siteId+eventId+date;
			synchronized(eventStatMap){
				EventStat e1 = eventStatMap.get(key);
				if(e1 == null){
					e1 = new EventStatImpl();
					e1.setUserId(userId);
					e1.setSiteId(siteId);
					e1.setEventId(eventId);
					e1.setDate(date);
				}
				e1.setCount(e1.getCount() + 1);
				eventStatMap.put(key, e1);
			}
			
			if(!StatsManager.SITEVISIT_EVENTID.equals(eventId)){
				// add to activityMap
				String key2 = siteId+date+eventId;
				synchronized(activityMap){
					SiteActivity e2 = activityMap.get(key2);
					if(e2 == null){
						e2 = new SiteActivityImpl();
						e2.setSiteId(siteId);
						e2.setDate(date);
						e2.setEventId(eventId);
					}
					e2.setCount(e2.getCount() + 1);
					activityMap.put(key2, e2);
				}
			}
		}	

		if(eventId.startsWith(StatsManager.RESOURCE_EVENTID_PREFIX)){
			// add to resourceStatMap
			String resourceAction = null;
			try{
				resourceAction = eventId.split("\\.")[1];
			}catch(ArrayIndexOutOfBoundsException ex){
				resourceAction = eventId;
			}
			String key = userId+siteId+resourceRef+resourceAction+date;
			synchronized(resourceStatMap){
				ResourceStat e1 = resourceStatMap.get(key);
				if(e1 == null){
					e1 = new ResourceStatImpl();
					e1.setUserId(userId);
					e1.setSiteId(siteId);
					e1.setResourceRef(resourceRef);
					e1.setResourceAction(resourceAction);
					e1.setDate(date);
				}
				e1.setCount(e1.getCount() + 1);
				resourceStatMap.put(key, e1);
			}
		} else if (eventId.startsWith(StatsManager.LESSONS_EVENTID_PREFIX)) {
			String[] resourceParts = resourceRef.split("/");
			if (resourceParts.length > 3 && "page".equals(resourceParts[2])) {
				long pageId = Long.parseLong(resourceParts[3]);
				String lessonBuilderAction = null;
				try {
					lessonBuilderAction = eventId.split("\\.")[1];
				} catch (ArrayIndexOutOfBoundsException ex){
					lessonBuilderAction = eventId;
				}

				String key = userId + siteId + lessonBuilderAction + pageId + date;

				if ("create".equals(lessonBuilderAction)) {
					// We cache create events so we can ignore read events from page creators
					lessonPageCreateEventMap.put(resourceRef, userId);
				}

				if ("read".equals(lessonBuilderAction)) {
					// The user reading this is the creator, don't stash the read
					// event. If we did, every page would be read at creation time.

					// See if the create event was cached for this page
					String creatorUserId = lessonPageCreateEventMap.get(resourceRef);

					if (creatorUserId == null) {
						// It wasn't. Look it up in the db.
						final String hql = "select s.userId "
							+ "from LessonBuilderStatImpl as s "
							+ "where s.siteId = :siteid "
							+ "and s.pageAction = :pageAction "
							+ "and s.pageRef = :pageRef ";

						final String finalPageRef = resourceRef;

						// New files
						HibernateCallback hcb1 = new HibernateCallback() {

							@SuppressWarnings("unchecked")
							public Object doInHibernate(Session session) throws HibernateException, SQLException {

								Query q = session.createQuery(hql);
								q.setString("siteid", siteId);
								q.setString("pageAction", "create");
								q.setString("pageRef", finalPageRef);
								return q.list();
							}
						};

						List<String> creatorUserIds = (List<String>) getHibernateTemplate().execute(hcb1);

						if (creatorUserIds.size() > 0) {
							creatorUserId = creatorUserIds.get(0);
							lessonPageCreateEventMap.put(resourceRef, creatorUserId);
							if (creatorUserIds.size() > 1) {
								LOG.warn("Multiple create events for page reference: " + resourceRef);
							}
						}
					}

					if (creatorUserId == null || !creatorUserId.equals(userId)) {
						addToLessonBuilderStatMap(key, userId, siteId, resourceRef, pageId, lessonBuilderAction, date);
					}
				} else {
					addToLessonBuilderStatMap(key, userId, siteId, resourceRef, pageId, lessonBuilderAction, date);
				}
			}
		} else if(StatsManager.SITEVISIT_EVENTID.equals(eventId)){
			// add to visitsMap
			String key = siteId+date;
			lock.lock();
			try{
				SiteVisits e1 = visitsMap.get(key);
				if(e1 == null){
					e1 = new SiteVisitsImpl();
					e1.setSiteId(siteId);
					e1.setDate(date);
				}
				e1.setTotalVisits(e1.getTotalVisits() + 1);
				// unique visits are determined when updating to db:
				//	 --> e1.setTotalUnique(totalUnique);
				visitsMap.put(key, e1);
				// place entry on map so we can update unique visits later
				UniqueVisitsKey keyUniqueVisits = new UniqueVisitsKey(siteId, date);
				uniqueVisitsMap.put(keyUniqueVisits, Integer.valueOf(1));
				
				// site presence started
				if(M_sm.isEnableSitePresences()) {
					String pKey = siteId+userId+date;
					SitePresenceConsolidation spc = presencesMap.get(pKey);
					if(spc == null) {
						SitePresence sp = new SitePresenceImpl();
						sp.setSiteId(siteId);
						sp.setUserId(userId);
						sp.setDate(date);
						spc = new SitePresenceConsolidation(sp);
					}
					spc.sitePresence.setLastVisitStartTime(dateTime);
					presencesMap.put(pKey, spc);					
				}
			}finally{
				lock.unlock();
			}
			
		}else if(StatsManager.SITEVISITEND_EVENTID.equals(eventId)){
			// site presence ended
			if(M_sm.isEnableSitePresences()) {
				String pKey = siteId+userId+date;
				lock.lock();
				try{
					SitePresenceConsolidation spc = presencesMap.get(pKey);
					if(spc == null) {
						SitePresence sp = new SitePresenceImpl();
						sp.setSiteId(siteId);
						sp.setUserId(userId);
						sp.setDate(date);
						sp.setLastVisitStartTime(null);
						spc = new SitePresenceConsolidation(sp, dateTime);
					}
					if(spc.sitePresence.getLastVisitStartTime() != null) {
						long existingDuration = spc.sitePresence.getDuration();
						long start = spc.sitePresence.getLastVisitStartTime().getTime();
						long thisEventTime = dateTime.getTime();
						long additionalDuration = thisEventTime - start;
						if(additionalDuration > 4*60*60*1000) {
							LOG.warn("A site presence is longer than 4h!: duration="+(additionalDuration/1000/60)+" min (SITE:"+siteId+", USER:"+userId+", DATE:"+date+")");
						}
						spc.sitePresence.setDuration(existingDuration + additionalDuration);						
						spc.sitePresence.setLastVisitStartTime(null);	
					}
					presencesMap.put(pKey, spc);
				}finally{
					lock.unlock();
				}
			}
		} 
		
	}

	private void addToLessonBuilderStatMap(String key, String userId, String siteId, String pageRef, long pageId, String action, Date date) {

		synchronized (lessonBuilderStatMap) {
			LessonBuilderStat e1 = lessonBuilderStatMap.get(key);
			if (e1 == null) {
				e1 = new LessonBuilderStatImpl();
				e1.setUserId(userId);
				e1.setSiteId(siteId);
				e1.setPageRef(pageRef);
				e1.setPageId(pageId);
				e1.setPageAction(action);
				e1.setDate(date);
			}
			e1.setCount(e1.getCount() + 1);
			lessonBuilderStatMap.put(key, e1);
		}
	}

	protected boolean isRegisteredEvent(String eventId) {
		return M_ers.isRegisteredEvent(eventId);
	}
	
	//STAT-299 consolidate a server event
	private void consolidateServerEvent(Date dateTime, String eventId) {
		
		Date date = getTruncatedDate(dateTime);
				
		// add to serverStatMap
		String key = eventId+date;
		synchronized(serverStatMap){
			ServerStat s = serverStatMap.get(key);
			if(s == null){
				s = new ServerStatImpl();
				s.setEventId(eventId);
				s.setDate(date);
			}
			s.setCount(s.getCount() + 1);
			serverStatMap.put(key, s);
		}
		
	}
	
	//STAT-299 consolidate a user event
	private void consolidateUserEvent(Date dateTime, String userId) {
		
		Date date = getTruncatedDate(dateTime);
				
		// add to userStatMap
		String key = userId+date;
		synchronized(userStatMap){
			UserStat s = userStatMap.get(key);
			if(s == null){
				s = new UserStatImpl();
				s.setUserId(userId);
				s.setDate(date);
			}
			s.setCount(s.getCount() + 1);
			userStatMap.put(key, s);
		}
		
	}
	

	// ################################################################
	// Db update methods
	// ################################################################	
	@SuppressWarnings("unchecked")
	private synchronized boolean doUpdateConsolidatedEvents() {
		long startTime = System.currentTimeMillis();
		if(eventStatMap.size() > 0 || resourceStatMap.size() > 0
				|| activityMap.size() > 0 || uniqueVisitsMap.size() > 0 
				|| visitsMap.size() > 0 || presencesMap.size() > 0
				|| serverStatMap.size() > 0 || userStatMap.size() > 0) {
			Object r = getHibernateTemplate().execute(new HibernateCallback() {			
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Transaction tx = null;
					try{
						tx = session.beginTransaction();
						// do: EventStat
						if(eventStatMap.size() > 0) {
							Collection<EventStat> tmp1 = null;
							synchronized(eventStatMap){
								tmp1 = eventStatMap.values();
								eventStatMap = Collections.synchronizedMap(new HashMap<String, EventStat>());
							}
							doUpdateEventStatObjects(session, tmp1);
						}

						// do: ResourceStat
						if(resourceStatMap.size() > 0) {
							Collection<ResourceStat> tmp2 = null;
							synchronized(resourceStatMap){
								tmp2 = resourceStatMap.values();
								resourceStatMap = Collections.synchronizedMap(new HashMap<String, ResourceStat>());
							}
							doUpdateResourceStatObjects(session, tmp2);
						}

						// do: Lessons ResourceStat
						if (lessonBuilderStatMap.size() > 0) {
							Collection<LessonBuilderStat> tmp3 = null;
							synchronized (lessonBuilderStatMap) {
								tmp3 = lessonBuilderStatMap.values();
								lessonBuilderStatMap = Collections.synchronizedMap(new HashMap<String, LessonBuilderStat>());
							}
							doUpdateLessonBuilderStatObjects(session, tmp3);
						}
						
						// do: SiteActivity
						if(activityMap.size() > 0) {
							Collection<SiteActivity> tmp3 = null;
							synchronized(activityMap){
								tmp3 = activityMap.values();
								activityMap = Collections.synchronizedMap(new HashMap<String, SiteActivity>());
							}
							doUpdateSiteActivityObjects(session, tmp3);
						}
	
						// do: SiteVisits
						if(uniqueVisitsMap.size() > 0 || visitsMap.size() > 0) {	
							// determine unique visits for event related sites
							Map<UniqueVisitsKey, Integer> tmp4;
							synchronized(uniqueVisitsMap){
								tmp4 = uniqueVisitsMap;
								uniqueVisitsMap = Collections.synchronizedMap(new HashMap<UniqueVisitsKey, Integer>());
							}
							tmp4 = doGetSiteUniqueVisits(session, tmp4);
						
							// do: SiteVisits
							if(visitsMap.size() > 0) {
								Collection<SiteVisits> tmp5 = null;
								synchronized(visitsMap){
									tmp5 = visitsMap.values();
									visitsMap = Collections.synchronizedMap(new HashMap<String, SiteVisits>());
								}
								doUpdateSiteVisitsObjects(session, tmp5, tmp4);
							}
						}
						
						// do: SitePresences
						if(presencesMap.size() > 0) {
							Collection<SitePresenceConsolidation> tmp6 = null;
							synchronized(presencesMap){
								tmp6 = presencesMap.values();
								presencesMap = Collections.synchronizedMap(new HashMap<String, SitePresenceConsolidation>());
							}
							doUpdateSitePresencesObjects(session, tmp6);
						}
						
						// do: ServerStats
						if(serverStatMap.size() > 0) {
							Collection<ServerStat> tmp7 = null;
							synchronized(serverStatMap){
								tmp7 = serverStatMap.values();
								serverStatMap = Collections.synchronizedMap(new HashMap<String, ServerStat>());
							}
							doUpdateServerStatObjects(session, tmp7);
						}
						
						// do: UserStats
						if(userStatMap.size() > 0) {
							Collection<UserStat> tmp8 = null;
							synchronized(userStatMap){
								tmp8 = userStatMap.values();
								userStatMap = Collections.synchronizedMap(new HashMap<String, UserStat>());
							}
							doUpdateUserStatObjects(session, tmp8);
						}
	
						// commit ALL
						tx.commit();
					}catch(Exception e){
						if(tx != null) tx.rollback();
						LOG.warn("Unable to commit transaction: ", e);
						return Boolean.FALSE;
					}
					return Boolean.TRUE;
				}			
			});
			long endTime = System.currentTimeMillis();
			LOG.debug("Time spent in doUpdateConsolidatedEvents(): " + (endTime-startTime) + " ms");
			return ((Boolean) r).booleanValue();
		}else{
			return true;
		}
	}
	
	private void doUpdateEventStatObjects(Session session, Collection<EventStat> o) {
		if(o == null) return;
		List<EventStat> objects = new ArrayList<EventStat>(o);
		Collections.sort(objects);
		Iterator<EventStat> i = objects.iterator();
		
		while(i.hasNext()){
			EventStat eUpdate = i.next();
			String eExistingSiteId = null;
			EventStat eExisting = null;
			try{
				Criteria c = session.createCriteria(EventStatImpl.class);
				c.add(Expression.eq("siteId", eUpdate.getSiteId()));
				c.add(Expression.eq("eventId", eUpdate.getEventId()));
				c.add(Expression.eq("userId", eUpdate.getUserId()));
				c.add(Expression.eq("date", eUpdate.getDate()));
				try{
					eExisting = (EventStat) c.uniqueResult();
				}catch(HibernateException ex){
					try{
						List events = c.list();
						if ((events!=null) && (events.size()>0)){
							LOG.debug("More than 1 result when unique result expected.", ex);
							eExisting = (EventStat) c.list().get(0);
						}else{
							LOG.debug("No result found", ex);
							eExisting = null;
						}
					}catch(Exception ex3){
						eExisting = null;
					}
				}catch(Exception ex2){
					LOG.warn("Probably ddbb error when loading data at java object", ex2);
				}
				if(eExisting == null) 
					eExisting = eUpdate;
				else
					eExisting.setCount(eExisting.getCount() + eUpdate.getCount());
	
				eExistingSiteId = eExisting.getSiteId();
			}catch(Exception ex){
				//If something happens, skip the event processing
				LOG.warn("Failed to event:"+ eUpdate.getEventId(), ex);
			}
			if ((eExistingSiteId!=null) && (eExistingSiteId.trim().length()>0))
					session.saveOrUpdate(eExisting);
		}
	}

	private void doUpdateResourceStatObjects(Session session, Collection<ResourceStat> o) {
		if(o == null) return;
		List<ResourceStat> objects = new ArrayList<ResourceStat>(o);
		Collections.sort(objects);
		Iterator<ResourceStat> i = objects.iterator();
		while(i.hasNext()){
			ResourceStat eUpdate = i.next();
			ResourceStat eExisting = null;
			String eExistingSiteId = null;
			try{
				Criteria c = session.createCriteria(ResourceStatImpl.class);
				c.add(Expression.eq("siteId", eUpdate.getSiteId()));
				c.add(Expression.eq("resourceRef", eUpdate.getResourceRef()));
				c.add(Expression.eq("resourceAction", eUpdate.getResourceAction()));
				c.add(Expression.eq("userId", eUpdate.getUserId()));
				c.add(Expression.eq("date", eUpdate.getDate()));
				try{
					eExisting = (ResourceStat) c.uniqueResult();
				}catch(HibernateException ex){
					try{
						List events = c.list();
						if ((events!=null) && (events.size()>0)){
							LOG.debug("More than 1 result when unique result expected.", ex);
							eExisting = (ResourceStat) c.list().get(0);
						}else{
							LOG.debug("No result found", ex);
							eExisting = null;
						}
					}catch(Exception ex3){
						eExisting = null;
					}
				}catch(Exception ex2){
					LOG.warn("Probably ddbb error when loading data at java object", ex2);
				}
				if(eExisting == null) 
					eExisting = eUpdate;
				else
					eExisting.setCount(eExisting.getCount() + eUpdate.getCount());
				
				eExistingSiteId = eExisting.getSiteId();
			}catch(Exception ex){
				LOG.warn("Failed to event:"+ eUpdate.getId(), ex);
			}
			if ((eExistingSiteId!=null) && (eExistingSiteId.trim().length()>0))
					session.saveOrUpdate(eExisting);
		}
	}

	private void doUpdateLessonBuilderStatObjects(Session session, Collection<LessonBuilderStat> o) {

		if (o == null) return;
		List<LessonBuilderStat> objects = new ArrayList<LessonBuilderStat>(o);
		Collections.sort(objects);
		Iterator<LessonBuilderStat> i = objects.iterator();
		while (i.hasNext()) {
			LessonBuilderStat eUpdate = i.next();
			LessonBuilderStat eExisting = null;
			String eExistingSiteId = null;
			try {
				Criteria c = session.createCriteria(LessonBuilderStatImpl.class);
				c.add(Expression.eq("siteId", eUpdate.getSiteId()));
				c.add(Expression.eq("pageRef", eUpdate.getPageRef()));
				c.add(Expression.eq("pageAction", eUpdate.getPageAction()));
				c.add(Expression.eq("userId", eUpdate.getUserId()));
				c.add(Expression.eq("date", eUpdate.getDate()));
				try {
					eExisting = (LessonBuilderStat) c.uniqueResult();
				} catch (HibernateException ex){
					try {
						List events = c.list();
						if ((events!=null) && (events.size()>0)){
							LOG.debug("More than 1 result when unique result expected.", ex);
							eExisting = (LessonBuilderStat) c.list().get(0);
						} else{
							LOG.debug("No result found", ex);
							eExisting = null;
						}
					} catch (Exception ex3) {
						eExisting = null;
					}
				} catch(Exception ex2) {
					LOG.warn("Probably ddbb error when loading data at java object", ex2);
				}
				if (eExisting == null) {
					eExisting = eUpdate;
				} else {
					eExisting.setCount(eExisting.getCount() + eUpdate.getCount());
				}

				eExistingSiteId = eExisting.getSiteId();
			} catch (Exception ex) {
				LOG.warn("Failed to event:"+ eUpdate.getId(), ex);
			}
			if ((eExistingSiteId!=null) && (eExistingSiteId.trim().length()>0))
				session.saveOrUpdate(eExisting);
		}
	}
	
	private void doUpdateSiteActivityObjects(Session session, Collection<SiteActivity> o) {
		if(o == null) return;
		List<SiteActivity> objects = new ArrayList<SiteActivity>(o);
		Collections.sort(objects);
		Iterator<SiteActivity> i = objects.iterator();
		while(i.hasNext()){
			SiteActivity eUpdate = i.next();
			SiteActivity eExisting = null;
			String eExistingSiteId = null;
			try{
				Criteria c = session.createCriteria(SiteActivityImpl.class);
				c.add(Expression.eq("siteId", eUpdate.getSiteId()));
				c.add(Expression.eq("eventId", eUpdate.getEventId()));
				c.add(Expression.eq("date", eUpdate.getDate()));
				try{
					eExisting = (SiteActivity) c.uniqueResult();
				}catch(HibernateException ex){
					try{
						List events = c.list();
						if ((events!=null) && (events.size()>0)){
							LOG.debug("More than 1 result when unique result expected.", ex);
							eExisting = (SiteActivity) c.list().get(0);
						}else{
							LOG.debug("No result found", ex);
							eExisting = null;
						}
					}catch(Exception ex3){
						eExisting = null;
					}
				}catch(Exception ex2){
					LOG.warn("Probably ddbb error when loading data at java object", ex2);
				}
				if(eExisting == null) 
					eExisting = eUpdate;
				else
					eExisting.setCount(eExisting.getCount() + eUpdate.getCount());
	
				eExistingSiteId = eExisting.getSiteId();
			}catch(Exception ex){
				LOG.warn("Failed to event:"+ eUpdate.getEventId(), ex);
			}
			
			if ((eExistingSiteId!=null) && (eExistingSiteId.trim().length()>0))
					session.saveOrUpdate(eExisting);
		}
	}
	
	private void doUpdateSiteVisitsObjects(Session session, Collection<SiteVisits> o, Map<UniqueVisitsKey, Integer> map) {
		if(o == null) return;
		List<SiteVisits> objects = new ArrayList<SiteVisits>(o);
		Collections.sort(objects);
		Iterator<SiteVisits> i = objects.iterator();
		while(i.hasNext()){
			SiteVisits eUpdate = i.next();
			SiteVisits eExisting = null;
			String eExistingSiteId = null;
			try{
				Criteria c = session.createCriteria(SiteVisitsImpl.class);
				c.add(Expression.eq("siteId", eUpdate.getSiteId()));
				c.add(Expression.eq("date", eUpdate.getDate()));
				try{
					eExisting = (SiteVisits) c.uniqueResult();
				}catch(HibernateException ex){
					try{
						List events = c.list();
						if ((events!=null) && (events.size()>0)){
							LOG.debug("More than 1 result when unique result expected.", ex);
							eExisting = (SiteVisits) c.list().get(0);
						}else{
							LOG.debug("No result found", ex);
							eExisting = null;
						}
					}catch(Exception ex3){
						eExisting = null;
					}
				}catch(Exception ex2){
					LOG.warn("Probably ddbb error when loading data at java object", ex2);
				}
				if(eExisting == null){
					eExisting = eUpdate;
				}else{
					eExisting.setTotalVisits(eExisting.getTotalVisits() + eUpdate.getTotalVisits());
				}
				Integer mapUV = map.get(new UniqueVisitsKey(eExisting.getSiteId(), eExisting.getDate()));
				eExisting.setTotalUnique(mapUV == null? 1 : mapUV.longValue());
	
				eExistingSiteId = eExisting.getSiteId();
			}catch(Exception ex){
				LOG.warn("Failed to event:"+ eUpdate.getId(), ex);
			}
			if ((eExistingSiteId!=null) && (eExistingSiteId.trim().length()>0))
					session.saveOrUpdate(eExisting);
		}
	}

	private void doUpdateServerStatObjects(Session session, Collection<ServerStat> o) {
		if(o == null) return;
		List<ServerStat> objects = new ArrayList<ServerStat>(o);
		Collections.sort(objects);
		Iterator<ServerStat> i = objects.iterator();
		while(i.hasNext()){
			ServerStat eUpdate = i.next();
			ServerStat eExisting = null;
			try{
				Criteria c = session.createCriteria(ServerStatImpl.class);
				c.add(Expression.eq("eventId", eUpdate.getEventId()));
				c.add(Expression.eq("date", eUpdate.getDate()));
				try{
					eExisting = (ServerStat) c.uniqueResult();
				}catch(HibernateException ex){
					try{
						List events = c.list();
						if ((events!=null) && (events.size()>0)){
							LOG.debug("More than 1 result when unique result expected.", ex);
							eExisting = (ServerStat) c.list().get(0);
						}else{
							LOG.debug("No result found", ex);
							eExisting = null;
						}
					}catch(Exception ex3){
						eExisting = null;
					}
				}catch(Exception ex2){
					LOG.warn("Probably ddbb error when loading data at java object", ex2);
				}
				if(eExisting == null) {
					eExisting = eUpdate;
				}else{
					eExisting.setCount(eExisting.getCount() + eUpdate.getCount());
				}
				
			}catch(Exception ex){
				LOG.warn("Failed to event:"+ eUpdate.getEventId(), ex);
			}
			session.saveOrUpdate(eExisting);
		}
	}
	
	private void doUpdateUserStatObjects(Session session, Collection<UserStat> o) {
		if(o == null) return;
		List<UserStat> objects = new ArrayList<UserStat>(o);
		Collections.sort(objects);
		Iterator<UserStat> i = objects.iterator();
		while(i.hasNext()){
			UserStat eUpdate = i.next();
			UserStat eExisting = null;
			String eExistingUserId = null;
			try{
				Criteria c = session.createCriteria(UserStatImpl.class);
				c.add(Expression.eq("userId", eUpdate.getUserId()));
				c.add(Expression.eq("date", eUpdate.getDate()));
				try{
					eExisting = (UserStat) c.uniqueResult();
				}catch(HibernateException ex){
					try{
						List events = c.list();
						if ((events!=null) && (events.size()>0)){
							LOG.debug("More than 1 result when unique result expected.", ex);
							eExisting = (UserStat) c.list().get(0);
						}else{
							LOG.debug("No result found", ex);
							eExisting = null;
						}
					}catch(Exception ex3){
						eExisting = null;
					}
				}catch(Exception ex2){
					LOG.warn("Probably ddbb error when loading data at java object", ex2);
				}
				if(eExisting == null) {
					eExisting = eUpdate;
				}else{
					eExisting.setCount(eExisting.getCount() + eUpdate.getCount());
				}
				
				eExistingUserId = eExisting.getUserId();
				
			}catch(Exception ex){
				LOG.warn("Failed to event:"+ eUpdate.getId(), ex);
			}
			
			if(StringUtils.isNotBlank(eExistingUserId)) {
				session.saveOrUpdate(eExisting);
			}
			
		}
	}
	
	private Map<UniqueVisitsKey, Integer> doGetSiteUniqueVisits(Session session, Map<UniqueVisitsKey, Integer> map) {
		Iterator<UniqueVisitsKey> i = map.keySet().iterator();
		while(i.hasNext()){
			UniqueVisitsKey key = i.next();
			Query q = session.createQuery("select count(distinct s.userId) " + 
					"from EventStatImpl as s " +
					"where s.siteId = :siteid " +
					"and s.eventId = 'pres.begin' " +
					"and s.date = :idate");
			q.setString("siteid", key.siteId);
			q.setDate("idate", key.date);
			Integer uv = 1;
			try{
				uv = (Integer) q.uniqueResult();
			}catch(ClassCastException ex){
				uv = (int) ((Long) q.uniqueResult()).longValue();
			}catch(HibernateException ex){
				try{
					List visits = q.list();
					if ((visits!=null) && (visits.size()>0)){
						LOG.debug("More than 1 result when unique result expected.", ex);
						uv = (Integer) q.list().get(0);
					}else{
						LOG.debug("No result found", ex);
						uv = 1;
					}
				}catch (Exception e3){
					uv = 1;
				}
				
			}catch(Exception ex2){
				LOG.debug("Probably ddbb error when loading data at java object", ex2);
			}
			int uniqueVisits = uv == null? 1 : uv.intValue();
			map.put(key, Integer.valueOf((int)uniqueVisits));			
		}
		return map;
	}
	
	private void doUpdateSitePresencesObjects(Session session, Collection<SitePresenceConsolidation> o) {
		if(o == null) return;
		List<SitePresenceConsolidation> objects = new ArrayList<>(o);
		Collections.sort(objects);
		Iterator<SitePresenceConsolidation> i = objects.iterator();
		while(i.hasNext()){
			try{
				SitePresenceConsolidation spc = i.next();
				SitePresence sp = spc.sitePresence;
				SitePresence spExisting = doGetSitePresence(session, sp.getSiteId(), sp.getUserId(), sp.getDate());
				if(spExisting == null) {
					// Should we be doing this save if this is an end event?
					session.save(sp);
					if (!spc.firstEventIsPresEnd) {
						doUpdateSitePresenceTotal(session, sp);
					} else {
						// TODO Should deal with midnight crossing.
						// Should we at least warn that we've just got a end event without a start.
					}
				}else{
					long previousTotalPresence = spExisting.getDuration();
					long previousPresence = 0;
					long newTotalPresence;
					if(spc.firstEventIsPresEnd) {
						if(spExisting.getLastVisitStartTime() != null)
							previousPresence = spc.firstPresEndDate.getTime() - spExisting.getLastVisitStartTime().getTime();
						else
							throw new Exception("No initial visit start time found - skipping");
					} 
					newTotalPresence = previousTotalPresence + previousPresence + sp.getDuration();
					spExisting.setDuration(newTotalPresence);				
					spExisting.setLastVisitStartTime(sp.getLastVisitStartTime());
					session.update(spExisting);
					if (!spc.firstEventIsPresEnd) {
						doUpdateSitePresenceTotal(session, spExisting);
					}
				}
			}catch(HibernateException e){
				LOG.debug("Probably ddbb error when loading data at java object", e);
			}catch(Exception e){
				LOG.debug("Unknow error while consolidating presence events", e);
			}
		}
	}

	private void doUpdateSitePresenceTotal(Session session, SitePresence sp) throws Exception {
		SitePresenceTotal sptExisting = doGetSitePresenceTotal(session, sp.getSiteId(), sp.getUserId());
		if (sptExisting == null) {
			SitePresenceTotal spt = new SitePresenceTotalImpl(sp);
			session.save(spt);
		} else {
			sptExisting.updateFrom(sp);
			session.update(sptExisting);
		}
	}

	// ################################################################
	// Special site presence methods (visit time tracking)
	// ################################################################
	@SuppressWarnings("unchecked")
	private SitePresence doGetSitePresence(Session session, String siteId, String userId, Date date) {
		SitePresence eDb = null;
		Criteria c = session.createCriteria(SitePresenceImpl.class);
		c.add(Expression.eq("siteId", siteId));
		c.add(Expression.eq("userId", userId));
		c.add(Expression.eq("date", date));
		
		try{
			eDb = (SitePresence) c.uniqueResult();
		}catch(HibernateException ex){
			try{
				List es = c.list();
				if(es != null && es.size() > 0){
					LOG.debug("More than 1 result when unique result expected.", ex);
					eDb = (SitePresence) es.get(0);
				}else{
					eDb = null;
				}
			}catch (Exception e3){
				LOG.debug("Probably ddbb error when loading data at java object", e3);
				eDb = null;
			}
			
		}catch(Exception ex2){
			LOG.debug("Probably ddbb error when loading data at java object", ex2);
		}
		return eDb;
	}

	@SuppressWarnings("unchecked")
	private SitePresenceTotal doGetSitePresenceTotal(Session session, String siteId, String userId) {

		SitePresenceTotal eDb = null;
		Criteria c = session.createCriteria(SitePresenceTotalImpl.class);
		c.add(Expression.eq("siteId", siteId));
		c.add(Expression.eq("userId", userId));

		try {
			eDb = (SitePresenceTotal) c.uniqueResult();
		} catch (HibernateException ex) {
			try {
				List es = c.list();
				if (es != null && es.size() > 0) {
					LOG.debug("More than 1 result when unique result expected.", ex);
					eDb = (SitePresenceTotal) es.get(0);
				} else {
					eDb = null;
				}
			} catch (Exception e3) {
				LOG.debug("Probably ddbb error when loading data at java object", e3);
				eDb = null;
			}
		} catch (Exception ex2) {
			LOG.debug("Probably ddbb error when loading data at java object", ex2);
		}
		return eDb;
	}
	

	// ################################################################
	// Utility methods
	// ################################################################	
	private synchronized boolean isValidEvent(Event e) {
		if(e.getEvent().startsWith(StatsManager.RESOURCE_EVENTID_PREFIX)){
			String ref = e.getResource();	
			if(ref.trim().equals("")) return false;			
			try{
				String parts[] = ref.split("\\/");		
				if(parts[2].equals("user")){
					// workspace (ignore)
					return false;
				}else if(parts[2].equals("attachment") && parts.length < 6){
					// ignore mail attachments (no reference to site)
					return false;
				}else if(parts[2].equals("group")){
					// resources
					if(parts.length <= 4) return false;	
				}else if(parts[2].equals("group-user")){
					// drop-box
					if(parts.length <= 5) return false;
				}else if ((parts.length >= 3) && (parts[2].equals("private"))) {
		  // discard
					LOG.debug("Discarding content event in private area.");
					return false;
		}
	  }catch(Exception ex){
				return false;
			}
		}
		return true;
	}
	
	private Event fixMalFormedEvents(Event e){
		String event = e.getEvent();
		String resource = e.getResource();
		
		// OBSOLETE: fix bad reference (resource) format
		// => Use <eventParserTip> instead
			//if(!resource.startsWith("/"))
			//	resource = '/' + resource;
		
		// MessageCenter (OLD) CASE: Handle old MessageCenter events */
		if (event!=null){
			if(event.startsWith(StatsManager.RESOURCE_EVENTID_PREFIX) && resource.startsWith("MessageCenter")) {
				resource = resource.replaceFirst("MessageCenter::", "/MessageCenter/site/");
				resource = resource.replaceAll("::", "/");
				return M_ets.newEvent(
						event.replaceFirst("content.", "msgcntr."), 
						resource, 
						false);
			}else{ 
				return e;
			}
		}else{
			return M_ets.newEvent("garbage.", resource, false);
		}
	}
	
	private String parseSiteId(Event e){
		String eventId = e.getEvent();
		
		// get contextId (siteId) from new Event.getContext() method, if available
		if(M_sm.isEventContextSupported()) {
			String contextId = null;
			try{
				contextId = (String) e.getClass().getMethod("getContext", null).invoke(e, null);
				// STAT-150 fix:
				String sitePrefix = "/site/";
				if(contextId != null && contextId.startsWith(sitePrefix)) {
					contextId = contextId.substring(sitePrefix.length());
				}
				LOG.debug("Context read from Event.getContext() for event: " + eventId + " - context: " + contextId);
			}catch(Exception ex){
				LOG.warn("Unable to get Event.getContext() for event: " + eventId, ex);
			}
			if(contextId != null)
				return contextId; 
		}
		
		// get contextId (siteId) from event reference
		String eventRef = e.getResource();
		if(eventRef != null){
			try{
				if(StatsManager.SITEVISIT_EVENTID.equals(eventId)
						|| StatsManager.SITEVISITEND_EVENTID.equals(eventId)){
					// presence (site visit) syntax (/presence/SITE_ID-presence)
					String[] parts = eventRef.split("/");
					if(parts.length > 2 && parts[2].endsWith(PRESENCE_SUFFIX)) {
						return parts[2].substring(0, parts[2].length() - PRESENCE_SUFFIX_LENGTH);
					}

				}else{
					// use <eventParserTip>
					ToolInfo toolInfo = getEventIdToolMap().get(eventId);
					EventParserTip parserTip = toolInfo.getEventParserTip();
					if(parserTip != null && parserTip.getFor().equals(StatsManager.PARSERTIP_FOR_CONTEXTID)){
						int index = Integer.parseInt(parserTip.getIndex());
						return eventRef.split(parserTip.getSeparator())[index];
					}else if(M_sm.isEventContextSupported()) {
						LOG.info("Context information unavailable for event: " + eventId + " (ignoring)");
					}else{
						LOG.info("<eventParserTip> is mandatory when Event.getContext() is unsupported! Ignoring event: " + eventId);
						// try with most common syntax (/abc/cde/SITE_ID/...)
						// return eventRef.split("/")[3];
					}
				}
			}catch(Exception ex){
				LOG.warn("Unable to parse contextId from event: " + eventId + " | " + eventRef, ex);
			}
		}
		return null;
	}
	
	private Site getSite(String siteId) {
		Site site = null;
		try{
			// is it a site id?
			site = M_ss.getSite(siteId);
		}catch(IdUnusedException e1){
			// is it an alias?
			try{
				String alias = siteId;
				String target = M_as.getTarget(alias);
				if(target != null) {
					String newSiteId = M_em.newReference(target).getId();
					LOG.debug(alias + " is an alias targetting site id: "+newSiteId);
					site = M_ss.getSite(newSiteId);
				}else{
					throw new IdUnusedException(siteId);
				}
			}catch(IdUnusedException e2){
				// not a valid site
				LOG.debug(siteId + " is not a valid site.", e2);
			}
		}catch(Exception ex) {
			// not a valid site
			LOG.debug(siteId + " is not a valid site.", ex);
		}
		return site;
	}

	/** Get all server events **/
	private Collection<String> getServerEvents() {
		return M_ers.getServerEventIds();
	}
	
	/** Get eventId -> ToolInfo map */
	private Map<String, ToolInfo> getEventIdToolMap() {
		return M_ers.getEventIdToolMap();
	}	
	
	/**
	 * Get the date for today (time of 00:00:00).
	 * This is used when we are grouping event by day.
	 */
	private Date getToday() {
		return new Date();
	}
	
	private boolean isUserLoginEvent(Event e) {
		return StringUtils.equals(StatsManager.LOGIN_EVENTID, e.getEvent()) || StringUtils.equals(StatsManager.CONTAINER_LOGIN_EVENTID, e.getEvent());
	}
	
	private boolean isMyWorkspaceEvent(Event e) {
		return e.getResource() != null && e.getResource().startsWith("/site/~");
	}
	
	private Date getTruncatedDate(Date date) {
		if(date == null) return null;
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		return c.getTime();
	}

	private static class UniqueVisitsKey {
		public String siteId;
		public Date date;
		
		public UniqueVisitsKey(String siteId, Date date){
			this.siteId = siteId;
			this.date = resetToDay(date);
		}

		@Override
		public boolean equals(Object o) {
			if(o instanceof UniqueVisitsKey) {
				UniqueVisitsKey u = (UniqueVisitsKey) o;
				return siteId.equals(u.siteId) && date.equals(u.date);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return siteId.hashCode() + date.hashCode();
		}
		
		private Date resetToDay(Date date){
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			return c.getTime();
		}
	}

	/**
	 * This is used to hold the summary of a set of SitePresence objects in memory before
	 * they get written out to the DB later.
	 */
	private static class SitePresenceConsolidation implements Comparable<SitePresenceConsolidation>{
	    // Used to track if we never saw the presence start.
		public boolean firstEventIsPresEnd;
		public Date firstPresEndDate;
		public SitePresence sitePresence;
		
		public SitePresenceConsolidation(SitePresence sitePresence) {
			this(sitePresence, null);
		}

		/**
		 * @param firstPresEndDate The date of the end of the first site presence and we never saw start. This may happen
		 *                         when we process the start in one batch and end in another batch.
		 */
		public SitePresenceConsolidation(SitePresence sitePresence, Date firstPresEndDate) {
			this.sitePresence = sitePresence;
			if(firstPresEndDate == null) {
				this.firstEventIsPresEnd = false;
				this.firstPresEndDate = null;
			}else{
				this.firstEventIsPresEnd = true;
				this.firstPresEndDate = firstPresEndDate;
			}
		}

		@Override
		public int compareTo(SitePresenceConsolidation other) {
			int val = sitePresence.compareTo(other.sitePresence);
			if (val != 0) return val;
			val = firstPresEndDate.compareTo(other.firstPresEndDate);
			if (val != 0) return val;
			return (firstEventIsPresEnd?1:0) - (other.firstEventIsPresEnd?1:0);
		}

		@Override
		public boolean equals(Object o) {
			if(o instanceof SitePresenceConsolidation) {
				SitePresenceConsolidation u = (SitePresenceConsolidation) o;
				return firstEventIsPresEnd == u.firstEventIsPresEnd
					&& ( (firstPresEndDate == null && u.firstPresEndDate == null) || (firstPresEndDate != null && firstPresEndDate.equals(u.firstPresEndDate)) )
					&& ( (sitePresence == null && u.sitePresence == null) || (sitePresence != null && sitePresence.equals(u.sitePresence)) );
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Boolean.valueOf(firstEventIsPresEnd).hashCode() + firstPresEndDate.hashCode() + sitePresence.hashCode();
		}
		
		@Override
		public String toString() {
			StringBuilder buff = new StringBuilder();
			buff.append("firstPresEndDate: ");
			buff.append(firstPresEndDate);
			buff.append(" | sitePresence => ");
			if(sitePresence != null) buff.append(sitePresence.toString());
			else buff.append("null");
			return buff.toString();
		}

		
	}
	
}
