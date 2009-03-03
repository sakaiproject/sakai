/**********************************************************************************
 *
 * Copyright (c) 2006 Universidade Fernando Pessoa
 *
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 *
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.EventStat;
import org.sakaiproject.sitestats.api.JobRun;
import org.sakaiproject.sitestats.api.ResourceStat;
import org.sakaiproject.sitestats.api.SiteActivity;
import org.sakaiproject.sitestats.api.SiteVisits;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.StatsUpdateManager;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.parser.EventParserTip;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;


/**
 * @author <a href="mailto:nuno@ufp.pt">Nuno Fernandes</a>
 */
public class StatsUpdateManagerImpl extends HibernateDaoSupport implements Runnable, StatsUpdateManager, Observer {
	private Log								LOG									= LogFactory.getLog(StatsUpdateManagerImpl.class);
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
	private Map<String, EventStat>			eventStatMap						= Collections.synchronizedMap(new HashMap<String, EventStat>());
	private Map<String, ResourceStat>		resourceStatMap						= Collections.synchronizedMap(new HashMap<String, ResourceStat>());
	private Map<String, SiteActivity>		activityMap							= Collections.synchronizedMap(new HashMap<String, SiteActivity>());
	private Map<String, SiteVisits>			visitsMap							= Collections.synchronizedMap(new HashMap<String, SiteVisits>());
	private Map<UniqueVisitsKey, Integer>	uniqueVisitsMap						= Collections.synchronizedMap(new HashMap<UniqueVisitsKey, Integer>());

	private List<String>					registeredEvents					= null;
	private Map<String, ToolInfo>			eventIdToolMap						= null;
	private boolean							initialized 						= false;
	
	private final ReentrantLock				lock								= new ReentrantLock();


	
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
		// get all registered events
		registeredEvents = M_ers.getEventIds();
		// add site visit event
		registeredEvents.add(StatsManager.SITEVISIT_EVENTID);
		// get eventId -> ToolInfo map
		eventIdToolMap = M_ers.getEventIdToolMap();
		
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
			preProcessEvent(e);
			long endTime = System.currentTimeMillis();
			LOG.debug("Time spent pre-processing 1 event: " + (endTime-startTime) + " ms");
			return doUpdateConsolidatedEvents();
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
				for(int i=0; i<events.length; i++){
					if(events[i] != null) {
						preProcessEvent(events[i]);
					}
				}
				long endTime = System.currentTimeMillis();
				LOG.debug("Time spent pre-processing " + eventCount + " event(s): " + (endTime-startTime) + " ms");
				return doUpdateConsolidatedEvents();
			}
		}
		return true;
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
	// Update thread related methods
	// ################################################################	
	/** Method called whenever an new event is generated from EventTrackingService: do not call this method! */
	public void update(Observable obs, Object o) {		
		if(o instanceof Event){
			collectThreadQueue.add((Event) o);
		}
	}
	
	/** Update thread: do not call this method! */
	public void run(){
		try{
			LOG.debug("Started statistics update thread");
			while(collectThreadRunning){
				// do update job
				int eventCount = collectThreadQueue.size();
				if(eventCount > 0) {
					long startTime = System.currentTimeMillis();
					while(collectThreadQueue.size() > 0){
						preProcessEvent(collectThreadQueue.remove(0));
					}
					long endTime = System.currentTimeMillis();
					LOG.debug("Time spent pre-processing " + eventCount + " event(s): " + (endTime-startTime) + " ms");
				}
				doUpdateConsolidatedEvents();
				
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
			LOG.debug("Failed to execute statistics update thread",t);
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
	private void preProcessEvent(Event e) {
		String userId = e.getUserId();
		e = fixMalFormedEvents(e);
		if(registeredEvents.contains(e.getEvent()) && isValidEvent(e)){
			
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
		}//else LOG.debug("EventInfo ignored:  '"+e.toString()+"' ("+e.toString()+") USER_ID: "+userId);
	}
	
	private void consolidateEvent(Date date, String eventId, String resourceRef, String userId, String siteId) {
		if(eventId == null)
			return;
		// update		
		if(registeredEvents.contains(eventId)){
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
			
		}else if(StatsManager.SITEVISIT_EVENTID.equals(eventId)){
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
				//   --> e1.setTotalUnique(totalUnique);
				visitsMap.put(key, e1);
				// place entry on map so we can update unique visits later
				UniqueVisitsKey keyUniqueVisits = new UniqueVisitsKey(siteId, date);
				uniqueVisitsMap.put(keyUniqueVisits, Integer.valueOf(1));
			}finally{
				lock.unlock();
			}
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
				|| visitsMap.size() > 0) {
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
	
	private void doUpdateEventStatObjects(Session session, Collection<EventStat> objects) {
		if(objects == null) return;
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
					LOG.debug("Probably ddbb error when loading data at java object", ex2);
					System.out.println("Probably ddbb error when loading data at java object!!!!!!!!");
					
				}
				if(eExisting == null) 
					eExisting = eUpdate;
				else
					eExisting.setCount(eExisting.getCount() + eUpdate.getCount());
	
				eExistingSiteId = eExisting.getSiteId();
			}catch(Exception ex){
				//If something happens, skip the event processing
				ex.printStackTrace();
			}
			if ((eExistingSiteId!=null) && (eExistingSiteId.trim().length()>0))
					session.saveOrUpdate(eExisting);
		}
	}

	private void doUpdateResourceStatObjects(Session session, Collection<ResourceStat> objects) {
		if(objects == null) return;
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
					LOG.debug("Probably ddbb error when loading data at java object", ex2);
					System.out.println("Probably ddbb error when loading data at java object!!!!!!!!");
					
				}
				if(eExisting == null) 
					eExisting = eUpdate;
				else
					eExisting.setCount(eExisting.getCount() + eUpdate.getCount());
				
				eExistingSiteId = eExisting.getSiteId();
			}catch(Exception e){
				e.printStackTrace();
			}
			if ((eExistingSiteId!=null) && (eExistingSiteId.trim().length()>0))
					session.saveOrUpdate(eExisting);
		}
	}
	
	private void doUpdateSiteActivityObjects(Session session, Collection<SiteActivity> objects) {
		if(objects == null) return;
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
					LOG.debug("Probably ddbb error when loading data at java object", ex2);
					System.out.println("Probably ddbb error when loading data at java object!!!!!!!!");
					
				}
				if(eExisting == null) 
					eExisting = eUpdate;
				else
					eExisting.setCount(eExisting.getCount() + eUpdate.getCount());
	
				eExistingSiteId = eExisting.getSiteId();
			}catch(Exception e){
				e.printStackTrace();
			}
			
			if ((eExistingSiteId!=null) && (eExistingSiteId.trim().length()>0))
					session.saveOrUpdate(eExisting);
		}
	}
	
	private void doUpdateSiteVisitsObjects(Session session, Collection<SiteVisits> objects, Map<UniqueVisitsKey, Integer> map) {
		if(objects == null) return;
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
					LOG.debug("Probably ddbb error when loading data at java object", ex2);
					System.out.println("Probably ddbb error when loading data at java object!!!!!!!!");
					
				}
				if(eExisting == null){
					eExisting = eUpdate;
				}else{
					eExisting.setTotalVisits(eExisting.getTotalVisits() + eUpdate.getTotalVisits());
				}
				Integer mapUV = map.get(new UniqueVisitsKey(eExisting.getSiteId(), eExisting.getDate()));
				eExisting.setTotalUnique(mapUV == null? 1 : mapUV.longValue());
	
				eExistingSiteId = eExisting.getSiteId();
			}catch(Exception e){
				e.printStackTrace();
			}
			if ((eExistingSiteId!=null) && (eExistingSiteId.trim().length()>0))
					session.saveOrUpdate(eExisting);
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
				if(StatsManager.SITEVISIT_EVENTID.equals(eventId)){
					// presence (site visit) syntax (/presence/SITE_ID-presence)
					String[] parts = eventRef.split("/");
					if(parts.length > 2 && parts[2].endsWith(PRESENCE_SUFFIX)) {
						return parts[2].substring(0, parts[2].length() - PRESENCE_SUFFIX_LENGTH);
					}

				}else{
					// use <eventParserTip>
					ToolInfo toolInfo = eventIdToolMap.get(eventId);
					EventParserTip parserTip = toolInfo.getEventParserTip();
					if(parserTip != null && parserTip.getFor().equals(StatsManager.PARSERTIP_FOR_CONTEXTID)){
						int index = Integer.parseInt(parserTip.getIndex());
						return eventRef.split(parserTip.getSeparator())[index];
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
	
	private Date getToday() {
		Calendar c = Calendar.getInstance();
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
}
