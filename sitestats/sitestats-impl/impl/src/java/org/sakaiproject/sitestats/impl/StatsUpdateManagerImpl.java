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
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Expression;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.sitestats.api.EventStat;
import org.sakaiproject.sitestats.api.ResourceStat;
import org.sakaiproject.sitestats.api.SiteActivity;
import org.sakaiproject.sitestats.api.SiteVisits;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.StatsUpdateManager;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;


/**
 * @author <a href="mailto:nuno@ufp.pt">Nuno Fernandes</a>
 */
public class StatsUpdateManagerImpl extends HibernateDaoSupport implements Runnable, StatsUpdateManager, Observer {
	private Log					LOG					= LogFactory.getLog(StatsUpdateManagerImpl.class);
	
	/** Sakai services */
	private StatsManager		M_sm;
	private UsageSessionService	M_uss;

	/** Update Thread and Semaphore */
	private Thread				updateThread;
	private List				updateQueue			= new ArrayList();
	private Object				semaphore			= new Object();
	private boolean				threadsRunning		= true;
	public long					sleepTime			= 4000L;

	private List				registeredEvents	= null;
	
	public void setStatsManager(StatsManager mng){
		this.M_sm = mng;
	}
	
	public void setUsageSessionService(UsageSessionService uss){
		this.M_uss = uss;
	}

	
	// ################################################################
	// Spring init/destroy methods
	// ################################################################	
	public void init(){
		// registered events
		registeredEvents = M_sm.getRegisteredEventIds();
		
		// start update thread
		startUpdateThread();
		
		// add this as Event observer
		//EventTrackingService.addObserver(this);
		EventTrackingService.addLocalObserver(this);
	}
	
	public void destroy(){
		// remove this as Event observer
		EventTrackingService.deleteObserver(this);	
		
		// stop update thread
		stopUpdateThread();
	}

	
	// ################################################################
	// Process new events in real time
	// ################################################################
	/** Method called whenever an new event is generated from EventTrackingService */
	public void update(Observable obs, Object o) {		
		if(o instanceof Event){
			updateQueue.add(o);
		}
	}
	
	/** Update thread: do not call this method! */
	public void run(){
		try{
			while(threadsRunning){
				// do update job
				while(updateQueue.size() > 0){
					Event e = (Event) updateQueue.remove(0);
					if(registeredEvents.contains(e.getEvent()) && isValidEvent(e)){
						String userId = e.getUserId();
						if(userId == null) userId = M_uss.getSession(e.getSessionId()).getUserId();
						if(!M_sm.isCollectAdminEvents() && userId.equals("admin")) continue;
						String siteId = parseSiteId(e.getResource());
						if(siteId == null || SiteService.isUserSite(siteId) || SiteService.isSpecialSite(siteId)) continue;
						
						doUpdate(e, userId, siteId);
						//LOG.debug("Statistics updated for '"+e.getEvent()+"' ("+e.toString()+")");
					}//else
						//LOG.debug("Event ignored:  '"+e.toString()+"' ("+e.toString()+")");
				}
				
				// sleep if no work to do
				if(!threadsRunning) break;
				try{
					synchronized (semaphore){
						semaphore.wait(sleepTime);
					}
				}catch(InterruptedException e){
					LOG.warn("Failed to sleep statistics update thread",e);
				}
			}
		}catch(Throwable t){
			LOG.debug("Failed to execute statistics update thread",t);
		}finally{
			if(threadsRunning){
				// thread was stoped by an unknown error: restart
				LOG.debug("Statistics update thread was stoped by an unknown error: restarting...");
				startUpdateThread();
			}else
				LOG.info("Finished statistics update thread");
		}
	}

	/** Start the update thread */
	private void startUpdateThread(){
		threadsRunning = true;
		updateThread = null;
		updateThread = new Thread(this, "org.sakaiproject.sitestats.impl.StatsUpdateManagerImpl");
		updateThread.start();
	}
	
	/** Stop the update thread */
	private void stopUpdateThread(){
		threadsRunning = false;
		synchronized (semaphore){
			semaphore.notifyAll();
		}
	}
	

	// ################################################################
	// Update methods
	// ################################################################
	private synchronized void doUpdate(Event e, final String userId, final String siteId){
		// event details
		final Date date = getToday();
		final String event = e.getEvent();
		final String resource = e.getResource();
		if(resource.trim().equals("")) return;
		
		// update		
		if(registeredEvents.contains(event)){			
			doUpdateEventStat(event, resource, userId, siteId, date, registeredEvents);
			if(!event.equals("pres.begin")){
				doUpdateSiteActivity(event, siteId, date, registeredEvents);
			}
		}
		if(event.startsWith("content.")){
			doUpdateResourceStat(event, resource, userId, siteId, date);
		}else if(event.equals("pres.begin")){
			doUpdateSiteVisits(userId, siteId, date);
		}
	}

	private synchronized void doUpdateSiteVisits(final String userId, final String siteId, final Date date) {
		final String hql = "select s.userId " + 
				"from EventStatImpl as s " +
				"where s.siteId = :siteid " +
				"and s.eventId = 'pres.begin' " +
				"and s.date = :idate " +
				//"and s.userId != :userid " +
				"group by s.siteId, s.userId";
		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Transaction tx = null;
				try{
					tx = session.beginTransaction();
					// do the work		
					Criteria c = session.createCriteria(SiteVisitsImpl.class);
					c.add(Expression.eq("siteId", siteId));
					c.add(Expression.eq("date", date));
					SiteVisits entryV = null;
					try{
						entryV = (SiteVisits) c.uniqueResult();
					}catch(Exception ex){
						LOG.debug("More than 1 result when unique result expected.", ex);
						entryV = (SiteVisits) c.list().get(0);
					}
					if(entryV == null) entryV = new SiteVisitsImpl();
					
					Query q = session.createQuery(hql);
					q.setString("siteid", siteId);
					q.setDate("idate", getToday());
					//q.setString("userid", userId);
					long uniqueVisitors = q.list().size();// + 1;
					
					entryV.setSiteId(siteId);
					entryV.setTotalVisits(entryV.getTotalVisits() + 1);
					entryV.setTotalUnique(uniqueVisitors);
					entryV.setDate(date);
					// save & commit
					session.saveOrUpdate(entryV);
					tx.commit();
				}catch(Exception e){
					if(tx != null) tx.rollback();
					LOG.warn("Unable to commit transaction: ", e);
				}
				return null;
			}
		});
	}

	private synchronized void doUpdateResourceStat(final String event, final String ref, final String userId, final String siteId, final Date date) {
		final String fileName = ref;
		
		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Transaction tx = null;
				try{
					tx = session.beginTransaction();
					// do the work
					String resourceAction = null;
					try{
						resourceAction = event.split("\\.")[1];
					}catch(ArrayIndexOutOfBoundsException e){
						resourceAction = event;
					}
					Criteria c = session.createCriteria(ResourceStatImpl.class);
					c.add(Expression.eq("siteId", siteId));
					c.add(Expression.eq("resourceRef", fileName));
					c.add(Expression.eq("resourceAction", resourceAction));
					c.add(Expression.eq("userId", userId));
					c.add(Expression.eq("date", date));
					ResourceStat entryR = null;
					try{
						entryR = (ResourceStat) c.uniqueResult();
					}catch(Exception ex){
						LOG.debug("More than 1 result when unique result expected.", ex);
						entryR = (ResourceStat) c.list().get(0);
					}
					if(entryR == null) entryR = new ResourceStatImpl();
					entryR.setSiteId(siteId);
					entryR.setUserId(userId);
					entryR.setResourceRef(fileName);
					entryR.setResourceAction(resourceAction);
					entryR.setCount(entryR.getCount() + 1);
					entryR.setDate(date);
					// save & commit
					session.saveOrUpdate(entryR);
					tx.commit();
				}catch(Exception e){
					if(tx != null) tx.rollback();
					LOG.warn("Unable to commit transaction: ", e);
				}
				return null;
			}
		});
	}

	private synchronized void doUpdateSiteActivity(final String event, final String siteId, final Date date, List registeredEvents) {
		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Transaction tx = null;
				try{
					tx = session.beginTransaction();
					// do the work
					Criteria c = session.createCriteria(SiteActivityImpl.class);
					c.add(Expression.eq("siteId", siteId));
					c.add(Expression.eq("eventId", event));
					c.add(Expression.eq("date", date));
					SiteActivity entryA = null;
					try{
						entryA = (SiteActivity) c.uniqueResult();
					}catch(Exception ex){
						LOG.debug("More than 1 result when unique result expected.", ex);
						entryA = (SiteActivity) c.list().get(0);
					}
					if(entryA == null) entryA = new SiteActivityImpl();
					entryA.setSiteId(siteId);
					entryA.setEventId(event);
					entryA.setCount(entryA.getCount() + 1);
					entryA.setDate(date);
					// save & commit
					session.saveOrUpdate(entryA);
					tx.commit();
				}catch(Exception e){
					if(tx != null) tx.rollback();
					LOG.warn("Unable to commit transaction: ", e);
				}
				return null;
			}
		});
	}

	private synchronized void doUpdateEventStat(final String event, String resource, final String userId, final String siteId, final Date date, List registeredEvents) {
		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Transaction tx = null;
				try{
					tx = session.beginTransaction();
					// do the work
					Criteria c = session.createCriteria(EventStatImpl.class);
					c.add(Expression.eq("siteId", siteId));
					c.add(Expression.eq("eventId", event));
					c.add(Expression.eq("userId", userId));
					c.add(Expression.eq("date", date));
					EventStat entryE = null;
					try{
						entryE = (EventStat) c.uniqueResult();
					}catch(Exception ex){
						LOG.debug("More than 1 result when unique result expected.", ex);
						entryE = (EventStat) c.list().get(0);
					}
					if(entryE == null) entryE = new EventStatImpl();
					entryE.setSiteId(siteId);
					entryE.setUserId(userId);
					entryE.setEventId(event);
					entryE.setCount(entryE.getCount() + 1);
					entryE.setDate(date);
					// save & commit
					session.saveOrUpdate(entryE);
					tx.commit();
				}catch(Exception e){
					if(tx != null) tx.rollback();
					LOG.warn("Unable to commit transaction: ", e);
				}
				return null;
			}
		});
	}
	

	// ################################################################
	// Utility methods
	// ################################################################	
	private synchronized boolean isValidEvent(Event e) {
		if(e.getEvent().startsWith("content")){
			String ref = e.getResource();			
			try{
				String parts[] = ref.split("\\/");		
				if(parts[2].equals("user")){
					// workspace (ignore)
					return false;
				}else if(parts[2].equals("attachment")){
					if(parts.length <= 4) return false;
					if(parts[4].equals("Choose File")){
						// assignment/annoucement attachment
						if(parts.length <= 6) return false;
					}else{
						// mail attachment
						return false;
						// FIXME Id is not in the reference - must invoke MailArchive...
					}
				}else if(parts[2].equals("group")){
					// resources
					if(parts.length <= 4) return false;	
				}else if(parts[2].equals("group-user")){
					// drop-box
					if(parts.length <= 5) return false;
				}
			}catch(Exception ex){
				return false;
			}
		}
		return true;
	}
	
	private String parseSiteId(String ref){
		try{
			String[] parts = ref.split("/");
			if(parts == null)
				return null;
			if(parts.length == 1){
				// try with MessageCenter syntax (MessageCenter::SITE_ID::...)
				parts = ref.split("::");
				return parts.length > 1 ? parts[1] : null;
			}
			if(parts[0].equals("")){
				if(parts[1].equals("presence"))
					// try with presence syntax (/presence/SITE_ID-presence)
					if(parts[2].endsWith("-presence"))
						return parts[2].substring(0,parts[2].length()-9);
					else
						return null;
				else if(parts[1].equals("syllabus"))
					// try with Syllabus syntax (/syllabus/SITE_ID/...)
					return parts[2];
				else
					// try with most common syntax (/abc/cde/SITE_ID/...)
					return parts[3];
			}
		}catch(Exception e){
			LOG.debug("Unable to parse site ID from "+ref, e);
		}
		return null;
	}
	
	private Date getToday() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		return c.getTime();
	}
}
