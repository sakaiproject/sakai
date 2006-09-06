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

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Expression;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.content.cover.ContentTypeImageService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.CommonStatGrpByDate;
import org.sakaiproject.sitestats.api.Prefs;
import org.sakaiproject.sitestats.api.SiteActivity;
import org.sakaiproject.sitestats.api.SiteVisits;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.Validator;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;


/**
 * @author Nuno Fernandes
 *
 */
public class StatsManagerImpl extends HibernateDaoSupport implements StatsManager {
	private Log						LOG								= LogFactory.getLog(StatsManagerImpl.class);
	protected ResourceBundle		msgs							= ResourceBundle.getBundle("org.sakaiproject.sitestats.impl.bundle.Messages");
	
	/** Spring bean members */
	private boolean					collectAdminEvents				= false;

	/** Controller fields */
	private List					registeredEvents				= new ArrayList();
	private Map						eventNameMap;

	/** Sakai services */ 
	private SqlService 				M_sql;
    private boolean 				autoDdl;
	private UserDirectoryService 	M_uds;
	private SiteService			 	M_ss;
	

	// ################################################################
	// Spring bean methods
	// ################################################################
	public void setEventIds(String eventIds) {
		String[] e = eventIds.replace('\n', ' ').split(",");
		for(int i=0; i<e.length; i++)
			registeredEvents.add(e[i].trim());
	}
	
	public void setAddEventIds(String eventIds) {
		String[] e = eventIds.replace('\n', ' ').split(",");
		for(int i=0; i<e.length; i++)
			registeredEvents.add(e[i].trim());
	}
	
	public void setRemoveEventIds(String eventIds) {
		String[] e = eventIds.replace('\n', ' ').split(",");
		for(int i=0; i<e.length; i++)
			registeredEvents.remove(e[i].trim());
	}

	public void setCollectAdminEvents(boolean value){
		this.collectAdminEvents = value;
	}

	public boolean isCollectAdminEvents(){
		return collectAdminEvents;
	}

	public void setAutoDdl(boolean autoDdl) {
		this.autoDdl = autoDdl;
	}

	public void setSqlService(SqlService sqlService) {
		this.M_sql = sqlService;
	}

	public void setUserService(UserDirectoryService userService) {
		this.M_uds = userService;
	}

	public void setSiteService(SiteService siteService) {
		this.M_ss = siteService;
	}

	
	// ################################################################
	// Spring init/destroy methods
	// ################################################################	
	public void init(){
//		String sqlRenameUpdateScriptName = "sakai_sitestats_post_schemaupdate_ren";
//		String sqlUpdateScriptName = "sakai_sitestats_post_schemaupdate";
		if (autoDdl && M_sql != null/* && M_sql.getVendor().equals("mysql")*/) {
//			if (LOG.isInfoEnabled()) LOG.info("About to call sqlService.ddl with " + sqlRenameUpdateScriptName);
//			M_sql.ddl(this.getClass().getClassLoader(), sqlRenameUpdateScriptName);
//			if (LOG.isInfoEnabled()) LOG.info("About to call sqlService.ddl with " + sqlUpdateScriptName);
//			M_sql.ddl(this.getClass().getClassLoader(), sqlUpdateScriptName);
			DBHelper dbHelper = new DBHelper(M_sql);
			dbHelper.updateIndexes();
		}
	}


	// ################################################################
	// Registered/configured events
	// ################################################################
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getRegisteredEventIds()
	 */
	public List getRegisteredEventIds() {
		return registeredEvents;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getDefaultEventIdsForActivity()
	 */
	public List getDefaultEventIdsForActivity(){
		List registered = getRegisteredEventIds();
		List defaultForActivity = new ArrayList();
		Iterator i = registered.iterator();
		while(i.hasNext()){
			String eventId = (String) i.next();
			if(!eventId.equals("pres.begin"))
				defaultForActivity.add(eventId);
		}
		return defaultForActivity;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getSiteConfiguredEventIds(java.lang.String, int)
	 */
	public List getSiteConfiguredEventIds(final String siteId, final int page) {
		if (siteId == null){
	      throw new IllegalArgumentException("Null siteId");
	    }else{
	    	HibernateCallback hcb = new HibernateCallback(){                
	    		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    			Criteria c = session.createCriteria(PrefsImpl.class)
	    				.add(Expression.eq("siteId", siteId))
	    				.add(Expression.eq("page", new Integer(page)));
	    			List prefs = c.list();
	    			List eventIds = new ArrayList();
	    			Iterator i = prefs.iterator();
	    			while (i.hasNext()){
	    				Prefs p = (Prefs) i.next();
	    				eventIds.add(p.getEventId());
	    			}
	    			return eventIds;
	    		}
	    	};
	    	List dbList = (List) getHibernateTemplate().execute(hcb);
	    	if(dbList != null && dbList.size() > 0)
	    		return dbList;
	    	else{
	    		if(page == PREFS_EVENTS_PAGE) return getRegisteredEventIds();
	    		if(page == PREFS_OVERVIEW_PAGE) return getDefaultEventIdsForActivity();
	    	}
	    	return null;
	    }
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#setSiteConfiguredEventIds(java.lang.String, java.util.List, int)
	 */
	public void setSiteConfiguredEventIds(final String siteId, final List eventIds, final int page) {
		if (siteId == null || eventIds == null){
			throw new IllegalArgumentException("Null siteId or eventIds");
		}else{
			getHibernateTemplate().execute(new HibernateCallback(){                
	    		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    			try{
						// 1. List actual list
						List currPrefs = session.createCriteria(PrefsImpl.class)
							.add(Expression.eq("siteId", siteId))
							.add(Expression.eq("page", new Integer(page)))
							.list();
						
						// 2. Find records to remove
						List toRemoveFromNewList = new ArrayList();
						List toRemoveFromExistingList = new ArrayList();
						Iterator c = currPrefs.iterator();
						while (c.hasNext()){
							Prefs p = (Prefs) c.next();
							boolean toRemove = true;
							Iterator n = eventIds.iterator();
							while (n.hasNext()){
								String s1 = (String) n.next();
								if(p.getEventId().equals(s1) && p.getPage() == page){
									toRemove = false;
									toRemoveFromNewList.add(s1);
								}
							}
							if(toRemove){
								// remove de-selected event from existing (saved) list
								toRemoveFromExistingList.add(p);
							}
						}
						
						// 3. Remove de-selected event from existing (saved) list
						Iterator rn = toRemoveFromExistingList.iterator();
						while (rn.hasNext())
							session.delete(rn.next());

						// 4. Remove already selected events from new list
						Iterator r = toRemoveFromNewList.iterator();
						while (r.hasNext())
							eventIds.remove(r.next());

						// 5. Add new records
						Iterator i = eventIds.iterator();
						while (i.hasNext()){
							Prefs n = new PrefsImpl();
							n.setSiteId(siteId);
							n.setEventId((String) i.next());
							n.setPage(page);
							session.saveOrUpdate(n);
						}
					}catch(Exception e){
						LOG.warn("Unable to save preferences for site id: "+siteId, e);
					}
					return null;
				}
			});
		}
	}
	
	  public void setSiteConfiguredEventIds_old(final String siteId, List eventIds, final int page) {
		if(siteId == null || eventIds == null){
			throw new IllegalArgumentException("Null siteId or eventIds");
		}else{
			// 1. List actual list
			HibernateCallback hcb1 = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					List currPrefs = session.createCriteria(PrefsImpl.class).add(Expression.eq("siteId", siteId)).add(Expression.eq("page", new Integer(page))).list();
					return currPrefs;
				}
			};
			List currPrefs = (List) getHibernateTemplate().execute(hcb1);
			// 2. Find records to remove 
			List toRemoveFromExistingList = new ArrayList();
			List toRemoveFromNewList = new ArrayList();
			Iterator c = currPrefs.iterator();
			while (c.hasNext()){
				Prefs p = (Prefs) c.next();
				boolean toRemove = true;
				Iterator n = eventIds.iterator();
				while (n.hasNext()){
					String s1 = (String) n.next();
					if(p.getEventId().equals(s1) && p.getPage() == page){
						toRemove = false;
						toRemoveFromNewList.add(s1);
					}
				}
				if(toRemove) toRemoveFromExistingList.add(p);
			}
			// 3. Remove existing not necessary records
			try{
				getHibernateTemplate().deleteAll(toRemoveFromExistingList);
			}catch(DataAccessException e){
				logger.error("Unable to remove prefs: " + toRemoveFromExistingList.toString(), e);
			}
			// 4. Remove records from new list
			Iterator r = toRemoveFromNewList.iterator();
			while (r.hasNext())
				eventIds.remove(r.next());
			// 3. Add new records
			List newPrefs = new ArrayList();
			Iterator i = eventIds.iterator();
			while (i.hasNext()){
				Prefs n = new PrefsImpl();
				n.setSiteId(siteId);
				n.setEventId((String) i.next());
				n.setPage(page);
				newPrefs.add(n);
			}
			try{
				getHibernateTemplate().saveOrUpdateAll(newPrefs);
			}catch(DataAccessException e){
				logger.error("Unable to save/update pref: " + newPrefs.toString(), e);
			}
		}
	}
	 
	
	
	// ################################################################
	// Maps
	// ################################################################		
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getEventName(java.lang.String)
	 */
	public String getEventName(String eventId) {
		//return eventsBean.getProperty(eventId, eventId);
		return msgs.getString(eventId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getEventNameMap()
	 */
	public Map getEventNameMap() {
		if(eventNameMap == null){
			eventNameMap = new HashMap();
			if(registeredEvents == null) getRegisteredEventIds();
			Iterator i = registeredEvents.iterator();
			while (i.hasNext()){
				String eId = (String) i.next();
				eventNameMap.put(eId, getEventName(eId));
			}

		}
		return eventNameMap;
	}
		
	
	public String getResourceName(String ref){
		Reference r = EntityManager.newReference(ref);
		ResourceProperties rp = r.getProperties();
		if(rp == null){
			return getResourceName_ManualParse(ref);
		}
		String name = rp.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
		StringBuffer _fileName = new StringBuffer("");
		
		String parts[] = ref.split("\\/");		
		if(parts[2].equals("user")){
			_fileName.append("[workspace]");
			_fileName.append(SEPARATOR);
		}else if(parts[2].equals("attachment")){
			if(parts.length > 6 && parts[4].equals("Choose File")){
				// assignment/annoucement attachment
				_fileName.append("[attachment]");
				_fileName.append(SEPARATOR);
			}else if(parts.length > 4){
				// mail attachment
				_fileName.append("[attachment]");
				_fileName.append(SEPARATOR);
			}
		}else if(parts.length > 4  && parts[2].equals("group")){
			// resource (standard)
		}else if(parts.length > 5 && parts[2].equals("group-user")){
			// mail attachment
			_fileName.append("[dropbox");
			_fileName.append(SEPARATOR);
			String userEid = null;
			try{
				userEid = M_uds.getUserEid(parts[4]);
			}catch(UserNotDefinedException e){
				userEid = parts[4];
			}
			_fileName.append(userEid);
			_fileName.append("]");
			_fileName.append(SEPARATOR);
		}
		
		_fileName.append(name);
		return _fileName.toString();		
	}
	
	public String getResourceName_ManualParse(String ref) {
		String parts[] = ref.split("\\/");
		StringBuffer _fileName = new StringBuffer("");
		// filename
		if(parts[2].equals("user")){ return null; }
		if(parts[2].equals("attachment")){
			if(parts.length <= 4) return null;
			if(parts[4].equals("Choose File")){
				// assignment/annoucement attachment
				if(parts.length <= 6) return null;
				_fileName.append("attachment");
				_fileName.append(SEPARATOR);
				for(int i = 6; i < parts.length - 1; i++)
					_fileName.append(parts[i] + SEPARATOR);
				_fileName.append(parts[parts.length - 1]);
			}else{
				// mail attachment
				return null;

			}
			// append filename
		}else if(parts[2].equals("group")){
			if(parts.length <= 4) return null;
			for(int i = 4; i < parts.length - 1; i++)
				_fileName.append(parts[i] + SEPARATOR);
			_fileName.append(parts[parts.length - 1]);
		}else if(parts[2].equals("group-user")){
			if(parts.length <= 5) return null;
			// append user eid
			String userEid = null;
			try{
				userEid = M_uds.getUserEid(parts[4]);
			}catch(UserNotDefinedException e){
				userEid = parts[4];
			}
			_fileName.append(userEid);
			_fileName.append(SEPARATOR);
			// append filename
			for(int i = 5; i < parts.length - 1; i++)
				_fileName.append(parts[i] + SEPARATOR);
			_fileName.append(parts[parts.length - 1]);
		}
		String fileName = _fileName.toString();
		if(fileName.trim().equals("")) return null;
		return fileName;
	}
	
	public String getResourceImage(String ref){
		String href = "../../../library/image/";
		Reference r = EntityManager.newReference(ref);
		ResourceProperties rp = r.getProperties();
		
		boolean isCollection;
		if(rp != null){
			try{
				isCollection = rp.getBooleanProperty(rp.getNamePropIsCollection());
			}catch(EntityPropertyNotDefinedException e){
				isCollection = false;
			}catch(EntityPropertyTypeException e){
				isCollection = false;
			}
		}else
			isCollection = false;
		
		String imgLink = "";
		if(isCollection)
			imgLink = href + ContentTypeImageService.getContentTypeImage("folder");			
		else if(rp != null)
			imgLink = href + ContentTypeImageService.getContentTypeImage(rp.getProperty(rp.getNamePropContentType()));
		else
			imgLink = href + "sakai/generic.gif";
		return imgLink;
	}	
	
	public String getResourceURL(String ref){
		Reference r = EntityManager.newReference(ref);
		return Validator.escapeHtml(r.getUrl());
	}
	
	// ################################################################
	// Event stats
	// ################################################################
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getEventStats(java.lang.String, java.util.List)
	 */
	public List getEventStats(String siteId, List events) {
		return getEventStats(siteId, events, null, getInitialActivityDate(siteId), null);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getEventStats(java.lang.String, java.util.List, java.lang.String, java.util.Date, java.util.Date, boolean)
	 */
	public List getEventStats(final String siteId, final List events, 
			final String searchKey, final Date iDate, final Date fDate) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			final List userIdList = searchUsers(searchKey);
			
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Criteria c = session.createCriteria(EventStatImpl.class)
							.add(Expression.eq("siteId", siteId))
							.add(Expression.in("eventId", events));
					if(userIdList != null && userIdList.size() > 0)
						c.add(Expression.in("userId", userIdList));
					if(iDate != null)
						c.add(Expression.ge("date", iDate));
					if(fDate != null){
						// adjust final date
						Calendar ca = Calendar.getInstance();
						ca.setTime(fDate);
						ca.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = ca.getTime();
						c.add(Expression.lt("date", fDate2));
					}
					return c.list();
				}
			};
			return (List) getHibernateTemplate().execute(hcb);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getEventStatsGrpByDate(String, List, String, Date, Date, PagingPosition)
	 */
	public List getEventStatsGrpByDate(final String siteId, final List events, 
			final String searchKey, final Date iDate, final Date fDate, final PagingPosition page) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			final List userIdList = searchUsers(searchKey);

			String usersStr = "";
			String iDateStr = "";
			String fDateStr = "";
			if(userIdList != null && userIdList.size() > 0)
				usersStr = "and s.userId in (:users) ";
			if(iDate != null)
				iDateStr = "and s.date >= :idate ";
			if(fDate != null)
				fDateStr = "and s.date < :fdate ";
			final String hql = "select s.siteId, s.userId, s.eventId, sum(s.count), max(s.date) " + 
					"from EventStatImpl as s " +
					"where s.siteId = :siteid " +
					"and s.eventId in (:events) " +
					usersStr + iDateStr + fDateStr +
					"group by s.siteId, s.userId, s.eventId";
			
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = session.createQuery(hql);
					q.setString("siteid", siteId);
					q.setParameterList("events", events);
					if(userIdList != null && userIdList.size() > 0)
						q.setParameterList("users", userIdList);
					if(iDate != null)
						q.setDate("idate", iDate);
					if(fDate != null){
						// adjust final date
						Calendar c = Calendar.getInstance();
						c.setTime(fDate);
						c.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = c.getTime();
						q.setDate("fdate", fDate2);
					}
					if(page != null){
						q.setFirstResult(page.getFirst() - 1);
						q.setMaxResults(page.getLast() - page.getFirst());
					}
					List records = q.list();
					List results = new ArrayList();
					if(records.size() > 0){
						for(Iterator iter = records.iterator(); iter.hasNext();) {
							Object[] s = (Object[]) iter.next();
							CommonStatGrpByDate c = new CommonStatGrpByDateImpl();
							c.setSiteId((String)s[0]);
							c.setUserId((String)s[1]);
							c.setRef((String)s[2]);
							c.setCount(((Long)s[3]).longValue());
							c.setDate((Date)s[4]);
							results.add(c);
						}
						return results;
					}
					else return results;	
				}
			};
			return (List) getHibernateTemplate().execute(hcb);
		}
	}
	
	public int countEventStatsGrpByDate(final String siteId, final List events, 
			final String searchKey, final Date iDate, final Date fDate) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			final List userIdList = searchUsers(searchKey);

			String usersStr = "";
			String iDateStr = "";
			String fDateStr = "";
			if(userIdList != null && userIdList.size() > 0)
				usersStr = "and s.userId in (:users) ";
			if(iDate != null)
				iDateStr = "and s.date >= :idate ";
			if(fDate != null)
				fDateStr = "and s.date < :fdate ";
			final String hql = "select count(*) " + 
					"from EventStatImpl as s " +
					"where s.siteId = :siteid " +
					"and s.eventId in (:events) " +
					usersStr + iDateStr + fDateStr +
					"group by s.siteId, s.userId, s.eventId";
			
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = session.createQuery(hql);
					q.setString("siteid", siteId);
					q.setParameterList("events", events);
					if(userIdList != null && userIdList.size() > 0)
						q.setParameterList("users", userIdList);
					if(iDate != null)
						q.setDate("idate", iDate);
					if(fDate != null){
						// adjust final date
						Calendar c = Calendar.getInstance();
						c.setTime(fDate);
						c.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = c.getTime();
						q.setDate("fdate", fDate2);
					}
					return new Integer(q.list().size());	
				}
			};
			return ((Integer) getHibernateTemplate().execute(hcb)).intValue();
		}
	}

	
	// ################################################################
	// Resource stats
	// ################################################################
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getResourceStats(java.lang.String)
	 */
	public List getResourceStats(String siteId) {
		return getResourceStats(siteId, null, getInitialActivityDate(siteId), null);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getResourceStats(java.lang.String, java.lang.String, java.util.Date, java.util.Date, boolean)
	 */
	public List getResourceStats(final String siteId, final String searchKey, 
			final Date iDate, final Date fDate) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			final List userIdList = searchUsers(searchKey);			
			
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Criteria c = session.createCriteria(ResourceStatImpl.class)
							.add(Expression.eq("siteId", siteId));
					if(userIdList != null && userIdList.size() > 0)
						c.add(Expression.in("userId", userIdList));
					if(iDate != null)
						c.add(Expression.ge("date", iDate));
					if(fDate != null){
						// adjust final date
						Calendar ca = Calendar.getInstance();
						ca.setTime(fDate);
						ca.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = ca.getTime();
						c.add(Expression.lt("date", fDate2));
					}
					return c.list();
				}
			};
			return (List) getHibernateTemplate().execute(hcb);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getResourceStatsGrpByDateAndAction(java.lang.String, java.lang.String, java.util.Date, java.util.Date, org.sakaiproject.javax.PagingPosition)
	 */
	public List getResourceStatsGrpByDateAndAction(final String siteId, final String searchKey, 
			final Date iDate, final Date fDate, final PagingPosition page) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			final List userIdList = searchUsers(searchKey);			
			
			String usersStr = "";
			String iDateStr = "";
			String fDateStr = "";
			if(userIdList != null && userIdList.size() > 0)
				usersStr = "and s.userId in (:users) ";
			if(iDate != null)
				iDateStr = "and s.date >= :idate ";
			if(fDate != null)
				fDateStr = "and s.date < :fdate ";
			final String hql = "select s.siteId, s.userId, s.resourceRef, s.resourceAction, sum(s.count), max(s.date) " + 
					"from ResourceStatImpl as s " +
					"where s.siteId = :siteid " +
					usersStr + iDateStr + fDateStr +
					"group by s.siteId, s.userId, s.resourceRef, s.resourceAction";
			
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = session.createQuery(hql);
					q.setString("siteid", siteId);
					if(userIdList != null && userIdList.size() > 0)
						q.setParameterList("users", userIdList);
					if(iDate != null)
						q.setDate("idate", iDate);
					if(fDate != null){
						// adjust final date
						Calendar c = Calendar.getInstance();
						c.setTime(fDate);
						c.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = c.getTime();
						q.setDate("fdate", fDate2);
					}
					if(page != null){
						q.setFirstResult(page.getFirst() - 1);
						q.setMaxResults(page.getLast() - page.getFirst());
					}
					List records = q.list();
					List results = new ArrayList();
					if(records.size() > 0){
						for(Iterator iter = records.iterator(); iter.hasNext();) {
							Object[] s = (Object[]) iter.next();
							CommonStatGrpByDate c = new CommonStatGrpByDateImpl();
							c.setSiteId((String)s[0]);
							c.setUserId((String)s[1]);
							c.setRef((String)s[2]);
							c.setRefImg(getResourceImage((String)s[2]));
							c.setRefUrl(getResourceURL((String)s[2]));
							c.setRefAction((String)s[3]);
							c.setCount(((Long)s[4]).longValue());
							c.setDate((Date)s[5]);
							results.add(c);
						}
						return results;
					}
					else return results;	
				}
			};
			return (List) getHibernateTemplate().execute(hcb);
		}
	}
	
	public int countResourceStatsGrpByDateAndAction(final String siteId, final String searchKey,
			final Date iDate, final Date fDate){
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			final List userIdList = searchUsers(searchKey);			
			
			String usersStr = "";
			String iDateStr = "";
			String fDateStr = "";
			if(userIdList != null && userIdList.size() > 0)
				usersStr = "and s.userId in (:users) ";
			if(iDate != null)
				iDateStr = "and s.date >= :idate ";
			if(fDate != null)
				fDateStr = "and s.date < :fdate ";
			final String hql = "select count(*) " + 
					"from ResourceStatImpl as s " +
					"where s.siteId = :siteid " +
					usersStr + iDateStr + fDateStr +
					"group by s.siteId, s.userId, s.resourceRef, s.resourceAction";
			
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = session.createQuery(hql);
					q.setString("siteid", siteId);
					if(userIdList != null && userIdList.size() > 0)
						q.setParameterList("users", userIdList);
					if(iDate != null)
						q.setDate("idate", iDate);
					if(fDate != null){
						// adjust final date
						Calendar c = Calendar.getInstance();
						c.setTime(fDate);
						c.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = c.getTime();
						q.setDate("fdate", fDate2);
					}
					return new Integer(q.list().size());						
				}
			};
			return ((Integer) getHibernateTemplate().execute(hcb)).intValue();
		}
	}

	
	// ################################################################
	// Site stats
	// ################################################################
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getSiteVisits(java.lang.String)
	 */
	public List getSiteVisits(String siteId) {
		return getSiteVisits(siteId, getInitialActivityDate(siteId), null);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getSiteVisits(java.lang.String, java.util.Date, java.util.Date, boolean)
	 */
	public List getSiteVisits(final String siteId, final Date iDate, final Date fDate) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Criteria c = session.createCriteria(SiteVisitsImpl.class)
							.add(Expression.eq("siteId", siteId));
					if(iDate != null)
						c.add(Expression.ge("date", iDate));
					if(fDate != null){
						// adjust final date
						Calendar ca = Calendar.getInstance();
						ca.setTime(fDate);
						ca.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = ca.getTime();
						c.add(Expression.lt("date", fDate2));
					}
					return c.list();
				}
			};
			return (List) getHibernateTemplate().execute(hcb);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getSiteVisitsByMonth(java.lang.String, java.util.Date, java.util.Date, boolean)
	 */
	public List getSiteVisitsByMonth(final String siteId, final Date iDate, final Date fDate) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			String iDateStr = "";
			String fDateStr = "";
			if(M_sql.getVendor().equals("oracle")){
				if(iDate != null)
					iDateStr = "and s.VISITS_DATE >= :idate ";
				if(fDate != null)
					fDateStr = "and s.VISITS_DATE < :fdate ";
			}else{
				if(iDate != null)
					iDateStr = "and s.date >= :idate ";
				if(fDate != null)
					fDateStr = "and s.date < :fdate ";
			}
			final String hql = "select s.siteId, sum(s.totalVisits), sum(s.totalUnique), year(s.date), month(s.date) " + 
					"from SiteVisitsImpl as s " +
					"where s.siteId = :siteid " +
					iDateStr + fDateStr +
					"group by s.siteId, year(s.date), month(s.date)";
			final String oracleSql = "select s.SITE_ID as actSiteId, sum(s.TOTAL_VISITS) as actVisits, sum(s.TOTAL_UNIQUE) as actUnique, to_char(s.VISITS_DATE,'YYYY') as actYear, to_char(s.VISITS_DATE,'MM') as actMonth " + 
				"from SST_SITEVISITS s " +
				"where s.SITE_ID = :siteid " +
				iDateStr + fDateStr +
				"group by s.SITE_ID, to_char(s.VISITS_DATE,'YYYY'), to_char(s.VISITS_DATE,'MM')";
			
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = null;
					if(M_sql.getVendor().equals("oracle")){
						q = session.createSQLQuery(oracleSql)
							.addScalar("actSiteId")
							.addScalar("actVisits")
							.addScalar("actUnique")
							.addScalar("actYear")
							.addScalar("actMonth");
						
					}else{
						q = session.createQuery(hql);
					}
					q.setString("siteid", siteId);
					if(iDate != null)
						q.setDate("idate", iDate);
					if(fDate != null){
						// adjust final date
						Calendar c = Calendar.getInstance();
						c.setTime(fDate);
						c.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = c.getTime();
						q.setDate("fdate", fDate2);
					}
					List records = q.list();
					List results = new ArrayList();
					Calendar cal = Calendar.getInstance();
					if(records.size() > 0){
						for(Iterator iter = records.iterator(); iter.hasNext();) {
							Object[] s = (Object[]) iter.next();
							SiteVisits c = new SiteVisitsImpl();
							if(M_sql.getVendor().equals("oracle")){
								c.setSiteId((String)s[0]);
								c.setTotalVisits(((BigDecimal)s[1]).longValue());
								c.setTotalUnique(((BigDecimal)s[2]).longValue());
								cal.set(Calendar.YEAR, Integer.parseInt((String)s[3]));
								cal.set(Calendar.MONTH, Integer.parseInt((String)s[4]) - 1);							
							}else{
								c.setSiteId((String)s[0]);
								c.setTotalVisits(((Long)s[1]).longValue());
								c.setTotalUnique(((Long)s[2]).longValue());
								cal.set(Calendar.YEAR, ((Integer)s[3]).intValue());
								cal.set(Calendar.MONTH, ((Integer)s[4]).intValue() - 1);
							}							
							c.setDate(cal.getTime());
							results.add(c);
						}
						return results;
					}
					else return results;	
				}
			};
			return (List) getHibernateTemplate().execute(hcb);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getSiteActivity(java.lang.String, boolean)
	 */
	public List getSiteActivity(String siteId, List events) {
		return getSiteActivity(siteId, events, getInitialActivityDate(siteId), null);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getSiteActivity(java.lang.String, java.util.Date, java.util.Date, boolean)
	 */
	public List getSiteActivity(final String siteId, final List events, final Date iDate, final Date fDate) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Criteria c = session.createCriteria(SiteActivityImpl.class)
							.add(Expression.eq("siteId", siteId))
							.add(Expression.in("eventId", events));
					if(iDate != null)
						c.add(Expression.ge("date", iDate));
					if(fDate != null){
						// adjust final date
						Calendar ca = Calendar.getInstance();
						ca.setTime(fDate);
						ca.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = ca.getTime();
						c.add(Expression.lt("date", fDate2));
					}
					return c.list();
				}
			};
			return (List) getHibernateTemplate().execute(hcb);
		}
	}
	
	public List getSiteActivityByDay(final String siteId, final List events, final Date iDate, final Date fDate) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			String iDateStr = "";
			String fDateStr = "";
			if(M_sql.getVendor().equals("oracle")){
				if(iDate != null)
					iDateStr = "and s.ACTIVITY_DATE >= :idate ";
				if(fDate != null)
					fDateStr = "and s.ACTIVITY_DATE < :fdate ";
			}else{
				if(iDate != null)
					iDateStr = "and s.date >= :idate ";
				if(fDate != null)
					fDateStr = "and s.date < :fdate ";
			}
			final String hql = "select s.siteId, sum(s.count), year(s.date), month(s.date), day(s.date) " + 
					"from SiteActivityImpl as s " +
					"where s.siteId = :siteid " +
					"and s.eventId in (:eventlist) " +
					iDateStr + fDateStr +
					"group by s.siteId, year(s.date), month(s.date), day(s.date)";
			// to_char(to_date(sa.ACTIVITY_DATE,'DD-Mon-YY'),'YYYY') as actYear
			final String oracleSql = 
				"select s.SITE_ID as actSiteId, sum(s.ACTIVITY_COUNT) as actCount, to_char(s.ACTIVITY_DATE,'YYYY') as actYear, to_char(s.ACTIVITY_DATE,'MM') as actMonth, to_char(s.ACTIVITY_DATE,'DD') as actDay " + 
				"from SST_SITEACTIVITY s " +
				"where s.SITE_ID = :siteid " +
				"and s.EVENT_ID in (:eventlist) " +
				iDateStr + fDateStr +
				"group by s.SITE_ID, to_char(s.ACTIVITY_DATE,'YYYY'), to_char(s.ACTIVITY_DATE,'MM'), to_char(s.ACTIVITY_DATE,'DD')";
			
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = null;
					if(M_sql.getVendor().equals("oracle")){
						q = session.createSQLQuery(oracleSql)
							.addScalar("actSiteId")
							.addScalar("actCount")
							.addScalar("actYear")
							.addScalar("actMonth")
							.addScalar("actDay");
						
					}else{
						q = session.createQuery(hql);
					}
					q.setString("siteid", siteId);
					if(events != null && events.size() > 0)
						q.setParameterList("eventlist", events);
					else
						q.setParameterList("eventlist", getDefaultEventIdsForActivity());
					if(iDate != null)
						q.setDate("idate", iDate);
					if(fDate != null){
						// adjust final date
						Calendar c = Calendar.getInstance();
						c.setTime(fDate);
						c.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = c.getTime();
						q.setDate("fdate", fDate2);
					}
					List records = q.list();
					List results = new ArrayList();
					Calendar cal = Calendar.getInstance();
					if(records.size() > 0){
						for(Iterator iter = records.iterator(); iter.hasNext();) {
							Object[] s = (Object[]) iter.next();
							SiteActivity c = new SiteActivityImpl();
							if(M_sql.getVendor().equals("oracle")){
								c.setSiteId((String)s[0]);
								c.setCount(((BigDecimal)s[1]).longValue());
								cal.set(Calendar.YEAR, Integer.parseInt((String)s[2]));
								cal.set(Calendar.MONTH, Integer.parseInt((String)s[3]) - 1);
								cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt((String)s[4]));								
							}else{
								c.setSiteId((String)s[0]);
								c.setCount(((Long)s[1]).longValue());
								cal.set(Calendar.YEAR, ((Integer)s[2]).intValue());
								cal.set(Calendar.MONTH, ((Integer)s[3]).intValue() - 1);
								cal.set(Calendar.DAY_OF_MONTH, ((Integer)s[4]).intValue());
							}
							c.setDate(cal.getTime());
							c.setEventId(null);
							results.add(c);
						}
						return results;
					}
					else return results;	
				}
			};
			return (List) getHibernateTemplate().execute(hcb);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getSiteActivityByMonth(java.lang.String, java.util.List, java.util.Date, java.util.Date, boolean)
	 */
	public List getSiteActivityByMonth(final String siteId, final List events, final Date iDate, final Date fDate) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			String iDateStr = "";
			String fDateStr = "";
			if(M_sql.getVendor().equals("oracle")){
				if(iDate != null)
					iDateStr = "and s.ACTIVITY_DATE >= :idate ";
				if(fDate != null)
					fDateStr = "and s.ACTIVITY_DATE < :fdate ";
			}else{
				if(iDate != null)
					iDateStr = "and s.date >= :idate ";
				if(fDate != null)
					fDateStr = "and s.date < :fdate ";
			}
			final String hql = "select s.siteId, sum(s.count), year(s.date), month(s.date) " + 
					"from SiteActivityImpl as s " +
					"where s.siteId = :siteid " +
					"and s.eventId in (:eventlist) " +
					iDateStr + fDateStr +
					"group by s.siteId, year(s.date), month(s.date)";
			final String oracleSql = "select s.SITE_ID as actSiteId, sum(s.ACTIVITY_COUNT) as actCount, to_char(s.ACTIVITY_DATE,'YYYY') as actYear, to_char(s.ACTIVITY_DATE,'MM') as actMonth " + 
			"from SST_SITEACTIVITY s " +
			"where s.SITE_ID = :siteid " +
			"and s.EVENT_ID in (:eventlist) " +
			iDateStr + fDateStr +
			"group by s.SITE_ID, to_char(s.ACTIVITY_DATE,'YYYY'), to_char(s.ACTIVITY_DATE,'MM')";
			
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = null;
					if(M_sql.getVendor().equals("oracle")){
						q = session.createSQLQuery(oracleSql)
							.addScalar("actSiteId")
							.addScalar("actCount")
							.addScalar("actYear")
							.addScalar("actMonth");
						
					}else{
						q = session.createQuery(hql);
					}
					q.setString("siteid", siteId);
					if(events != null && events.size() > 0)
						q.setParameterList("eventlist", events);
					else
						q.setParameterList("eventlist", getDefaultEventIdsForActivity());
					if(iDate != null)
						q.setDate("idate", iDate);
					if(fDate != null){
						// adjust final date
						Calendar c = Calendar.getInstance();
						c.setTime(fDate);
						c.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = c.getTime();
						q.setDate("fdate", fDate2);
					}
					List records = q.list();
					List results = new ArrayList();
					Calendar cal = Calendar.getInstance();
					if(records.size() > 0){
						for(Iterator iter = records.iterator(); iter.hasNext();) {
							Object[] s = (Object[]) iter.next();
							SiteActivity c = new SiteActivityImpl();
							if(M_sql.getVendor().equals("oracle")){
								c.setSiteId((String)s[0]);
								c.setCount(((BigDecimal)s[1]).longValue());
								cal.set(Calendar.YEAR, Integer.parseInt((String)s[2]));
								cal.set(Calendar.MONTH, Integer.parseInt((String)s[3]) - 1);								
							}else{
								c.setSiteId((String)s[0]);
								c.setCount(((Long)s[1]).longValue());
								cal.set(Calendar.YEAR, ((Integer)s[2]).intValue());
								cal.set(Calendar.MONTH, ((Integer)s[3]).intValue() - 1);
							}							
							c.setDate(cal.getTime());
							c.setEventId(null);
							results.add(c);
						}
						return results;
					}
					else return results;	
				}
			};
			return (List) getHibernateTemplate().execute(hcb);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getSiteActivityGrpByDate(java.lang.String, java.util.List, java.util.Date, java.util.Date, boolean)
	 */
	public List getSiteActivityGrpByDate(final String siteId, final List events, final Date iDate, final Date fDate) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			String iDateStr = "";
			String fDateStr = "";
			if(iDate != null)
				iDateStr = "and s.date >= :idate ";
			if(fDate != null)
				fDateStr = "and s.date < :fdate ";
			final String hql = "select s.siteId, sum(s.count),s.date " + 
					"from SiteActivityImpl as s " +
					"where s.siteId = :siteid " +
					"and s.eventId in (:eventlist) " +
					iDateStr + fDateStr +
					"group by s.siteId, s.date";
			
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = session.createQuery(hql);
					q.setString("siteid", siteId);
					if(events != null && events.size() > 0)
						q.setParameterList("eventlist", events);
					else
						q.setParameterList("eventlist", getDefaultEventIdsForActivity());
					if(iDate != null)
						q.setDate("idate", iDate);
					if(fDate != null){
						// adjust final date
						Calendar c = Calendar.getInstance();
						c.setTime(fDate);
						c.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = c.getTime();
						q.setDate("fdate", fDate2);
					}
					List records = q.list();
					List results = new ArrayList();
					if(records.size() > 0){
						for(Iterator iter = records.iterator(); iter.hasNext();) {
							Object[] s = (Object[]) iter.next();
							SiteActivity c = new SiteActivityImpl();
							c.setSiteId((String)s[0]);
							c.setCount(((Long)s[1]).longValue());
							Date recDate = (Date)s[2];
							c.setDate(recDate);
							c.setEventId(null);
							results.add(c);
						}
						return results;
					}
					else return results;	
				}
			};
			return (List) getHibernateTemplate().execute(hcb);
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getTotalSiteVisits(java.lang.String)
	 */
	public long getTotalSiteVisits(String siteId) {
		return getTotalSiteVisits(siteId, getInitialActivityDate(siteId), null);
	}

	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getTotalSiteVisits(java.lang.String, java.util.Date, java.util.Date, boolean)
	 */
	public long getTotalSiteVisits(final String siteId, final Date iDate, final Date fDate) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			String iDateStr = "";
			String fDateStr = "";
			if(iDate != null)
				iDateStr = "and ss.date >= :idate ";
			if(fDate != null)
				fDateStr = "and ss.date < :fdate ";
			final String hql = "select sum(ss.totalVisits) " +
					"from SiteVisitsImpl as ss " +
					"where ss.siteId = :siteid " +
					iDateStr + fDateStr +
					"group by ss.siteId";
			
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = session.createQuery(hql);
					q.setString("siteid", siteId);
					if(iDate != null)
						q.setDate("idate", iDate);
					if(fDate != null){
						// adjust final date
						Calendar c = Calendar.getInstance();
						c.setTime(fDate);
						c.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = c.getTime();
						q.setDate("fdate", fDate2);
					}
					List res = q.list();
					if(res.size() > 0) return res.get(0);
					else return new Long(0);	
				}
			};
			return ((Long) getHibernateTemplate().execute(hcb)).longValue();
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getTotalSiteUniqueVisits(java.lang.String, boolean)
	 */
	public long getTotalSiteUniqueVisits(String siteId) {
		return getTotalSiteUniqueVisits(siteId, getInitialActivityDate(siteId), null);
	}

	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getTotalSiteUniqueVisits(java.lang.String, java.util.Date, java.util.Date, boolean)
	 */
	public long getTotalSiteUniqueVisits(final String siteId, final Date iDate, final Date fDate) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			String iDateStr = "";
			String fDateStr = "";
			if(iDate != null)
				iDateStr = "and ss.date >= :idate ";
			if(fDate != null)
				fDateStr = "and ss.date < :fdate ";
			final String hql = "select sum(ss.totalUnique) " +
					"from SiteVisitsImpl as ss " +
					"where ss.siteId = :siteid " +
					iDateStr + fDateStr +
					"group by ss.siteId";
			
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = session.createQuery(hql);
					q.setString("siteid", siteId);
					if(iDate != null)
						q.setDate("idate", iDate);
					if(fDate != null){
						// adjust final date
						Calendar c = Calendar.getInstance();
						c.setTime(fDate);
						c.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = c.getTime();
						q.setDate("fdate", fDate2);
					}
					List res = q.list();
					if(res.size() > 0) return res.get(0);
					else return new Long(0);	
				}
			};
			return ((Long) getHibernateTemplate().execute(hcb)).longValue();
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getTotalSiteActivity(java.lang.String, boolean)
	 */
	public long getTotalSiteActivity(String siteId, List events) {
		return getTotalSiteActivity(siteId, events, getInitialActivityDate(siteId), null);
	}

	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getTotalSiteActivity(java.lang.String, java.util.Date, java.util.Date, boolean)
	 */
	public long getTotalSiteActivity(final String siteId, final List events, final Date iDate, final Date fDate) {
		if(siteId == null){
			throw new IllegalArgumentException("Null siteId");
		}else{
			String iDateStr = "";
			String fDateStr = "";
			if(iDate != null)
				iDateStr = "and ss.date >= :idate ";
			if(fDate != null)
				fDateStr = "and ss.date < :fdate ";
			final String hql = "select sum(ss.count) " +
					"from SiteActivityImpl as ss " +
					"where ss.eventId in (:eventlist) " +
					"and ss.siteId = :siteid " +
					iDateStr + fDateStr +
					"group by ss.siteId";
			
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = session.createQuery(hql);
					q.setString("siteid", siteId);
					if(events != null && events.size() > 0)
						q.setParameterList("eventlist", events);
					else
						q.setParameterList("eventlist", getDefaultEventIdsForActivity());
					if(iDate != null)
						q.setDate("idate", iDate);
					if(fDate != null){
						// adjust final date
						Calendar c = Calendar.getInstance();
						c.setTime(fDate);
						c.add(Calendar.DAY_OF_YEAR, 1);
						Date fDate2 = c.getTime();
						q.setDate("fdate", fDate2);
					}
					List res = q.list();
					if(res.size() > 0) return res.get(0);
					else return new Long(0);	
				}
			};
			return ((Long) getHibernateTemplate().execute(hcb)).longValue();
		}
	}

	
	// ################################################################
	// Utility methods
	// ################################################################
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#getInitialActivityDate(java.lang.String)
	 */
	public Date getInitialActivityDate(String siteId) {
		Date date = null;
//		try{
//			Connection c = M_sql.borrowConnection();
//			String sql = "select CREATEDON from SAKAI_SITE where SITE_ID=?;";
//			PreparedStatement pst = c.prepareStatement(sql);
//			pst.setString(1, siteId);
//			ResultSet rs = pst.executeQuery();
//			while (rs.next()){
//				date = rs.getDate("CREATEDON");
//				if(date == null) date = Calendar.getInstance().getTime();
//			}
//			rs.close();
//			pst.close();
//			M_sql.returnConnection(c);
//		}catch(SQLException e){
//			LOG.error("SQL error occurred while retrieving getInitialActivityDate(): " + e.getMessage());
//		}
		try{
			date = new Date(M_ss.getSite(siteId).getCreatedTime().getTime());
		}catch(Exception e){
			return new Date(0); //Calendar.getInstance().getTime();
		}
		return date;
	}
	
	private List searchUsers(String searchKey){
		if(searchKey == null || searchKey.trim().equals(""))
			return null;
		List userList = M_uds.searchUsers(searchKey, 0, Integer.MAX_VALUE);
		List userIdList = new ArrayList();
		Iterator i = userList.iterator();
		while(i.hasNext())
			userIdList.add(((User)i.next()).getId());
		return userIdList;
	}
	

	// ################################################################
	// Tool access 
	// ################################################################
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsManager#isUserAllowed(java.lang.String, org.sakaiproject.service.legacy.site.Site, org.sakaiproject.api.kernel.tool.Tool)
	 */
	public boolean isUserAllowed(String userId, Site site, Tool tool) {
		if(userId == null) return false;
		if(SecurityService.isSuperUser()) return true;
		Role r = site.getUserRole(userId);
		if(r != null) return isToolAllowedForRole(tool, r.getId());
		else return false;
	}

	/**
	 * Checks if a given user has permissions access a tool in a site. This is
	 * checked agains the following tags in tool xml file:<br>
	 * <configuration name="roles.allow" value="maintain,Instructor" /><br>
	 * <configuration name="roles.deny" value="access,guest" /> <br>
	 * Both, one or none of this configuration tags can be specified. By
	 * default, an user has permissions to see the tool in site.<br>
	 * Permissions are checked in the order: Allow, Deny.
	 * @param tool Tool to check permissions on.
	 * @param roleId Current user's role.
	 * @return Whether user has permissions to this tool in this site.
	 */
	private boolean isToolAllowedForRole(Tool tool, String roleId) {
		if(tool == null) return false;
		String TOOL_CFG_ROLES_ALLOW = "roles.allow";
		String TOOL_CFG_ROLES_DENY = "roles.deny";
		Properties roleConfig = tool.getRegisteredConfig();
		String toolTitle = tool.getTitle();
		boolean allowRuleSpecified = roleConfig.containsKey(TOOL_CFG_ROLES_ALLOW);
		boolean denyRuleSpecified = roleConfig.containsKey(TOOL_CFG_ROLES_DENY);
		
		// allow by default, when no config keys are present
		if(!allowRuleSpecified && !allowRuleSpecified) return true;
		
		boolean allowed = true;
		if(allowRuleSpecified){
			allowed = false;
			boolean found = false;
			String[] result = roleConfig.getProperty(TOOL_CFG_ROLES_ALLOW).split("\\,");
		    for (int x=0; x<result.length; x++){
		    	if(result[x].trim().equals(roleId)){
		        	 found = true;
		        	 break;
		         }
		    }
			if(found){
				LOG.debug("Tool config '"+TOOL_CFG_ROLES_ALLOW+"' allowed access to '"+roleId+"' in "+toolTitle);
				allowed = true;
			}
		}
		if(denyRuleSpecified){
			if(!allowRuleSpecified)
				allowed = true;
			boolean found = false;
			String[] result = roleConfig.getProperty(TOOL_CFG_ROLES_DENY).split("\\,");
		    for (int x=0; x<result.length; x++){
		    	if(result[x].trim().equals(roleId)){
		        	 found = true;
		        	 break;
		         }
		    }
			if(found){
				LOG.debug("Tool config '"+TOOL_CFG_ROLES_DENY+"' denied access to '"+roleId+"' in "+toolTitle);
				allowed = false;
			}
		}else if(!allowRuleSpecified)
			allowed = true;
		LOG.debug("Allowed access to '"+roleId+"' in "+toolTitle+"? "+allowed);
		return allowed;
	}
	
}
