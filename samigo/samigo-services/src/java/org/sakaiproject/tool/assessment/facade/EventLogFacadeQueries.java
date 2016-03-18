package org.sakaiproject.tool.assessment.facade;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.EventLogData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAccessControl;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

public class EventLogFacadeQueries extends HibernateDaoSupport
implements EventLogFacadeQueriesAPI {

	private Log log = LogFactory
	.getLog(EventLogFacadeQueries.class);


	public void saveOrUpdateEventLog(EventLogFacade eventLog){
		EventLogData data = (EventLogData)  eventLog.getData();

		int retryCount = PersistenceService.getInstance().getRetryCount()
		.intValue();
		while (retryCount > 0) {
			try {
				getHibernateTemplate().saveOrUpdate(data);
				retryCount = 0;
			} catch (Exception e) {
				log
				.warn("problem save or update eventLog: "
						+ e.getMessage());
				retryCount = PersistenceService.getInstance().retryDeadlock(e,
						retryCount);
			}
		}
	}
	
	public List<EventLogData> getEventLogData(final Long assessmentGradingId) {
		String query = "select eld from EventLogData as eld"
			+ " where eld.processId = ?" 
			+ " order by eld.id desc";

		final String hql = query;
		final HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
			throws HibernateException, SQLException {
				Query q = session.createQuery(hql);				
				q.setLong(0, assessmentGradingId.longValue());
				
				return q.list();
			};
		};
		List list = getHibernateTemplate().executeFind(hcb);
		ArrayList<EventLogData> eventLogList = new ArrayList<EventLogData>();
		Map<String, User> userMap = new HashMap<String, User>();
		for(int i = 0; i < list.size(); i++) {
			EventLogData e =(EventLogData) list.get(i);
			e.setUserDisplay(getUserDisplay(e.getUserEid(), userMap));
			eventLogList.add(e);
		}		

		return eventLogList;
	}

	public List<EventLogData> getDataBySiteId(final String siteId) {
		String query = "select eld from EventLogData as eld"
			+ " where eld.siteId = ?"
			+ " order by eld.assessmentId asc, eld.userEid asc";

		final String hql = query;
		final HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
			throws HibernateException, SQLException {
				Query q = session.createQuery(hql);
				q.setString(0, siteId);

				return q.list();
			};
		};
		List list = (ArrayList) getHibernateTemplate().executeFind(hcb);
		
		ArrayList<EventLogData> eventLogDataList = new ArrayList<EventLogData>();
		Map<String, User> userMap = new HashMap<String, User>();
		for(int i = 0; i < list.size(); i++) {
			EventLogData e =(EventLogData) list.get(i);
			e.setUserDisplay(getUserDisplay(e.getUserEid(), userMap));
			eventLogDataList.add(e);
		}		

		return eventLogDataList;
	}
	
	public List<EventLogData> getEventLogData(final String siteId, final Long assessmentId, final String userFilter) {
	   String query = "select eld from EventLogData as eld where eld.siteId = ?";
	   
	   if (assessmentId > -1) {
	      query += " and eld.assessmentId = ?";
	   }
	   
	   query += " order by eld.assessmentId asc, eld.userEid asc";

      final String hql = query;
      final HibernateCallback hcb = new HibernateCallback() {
         public Object doInHibernate(Session session)
         throws HibernateException, SQLException {
            Query q = session.createQuery(hql);
            q.setString(0, siteId);
            if (assessmentId > -1) {
               q.setLong(1, assessmentId);
            }

            return q.list();
         };
      };
      List list = (ArrayList) getHibernateTemplate().executeFind(hcb);
      
      List<EventLogData> eventLogDataList = new ArrayList<EventLogData>();
      Map<String, User> userMap = new HashMap<String, User>();
      for(int i = 0; i < list.size(); i++) {
         EventLogData e =(EventLogData) list.get(i);
         e.setUserDisplay(getUserDisplay(e.getUserEid(), userMap));
     
         if (userFilter == null || "".equals(userFilter) || (userFilter != null && !"".equals(userFilter) && e.getUserDisplay().toLowerCase().contains(userFilter.toLowerCase()))) {
            eventLogDataList.add(e);
         }
         
      }     

      return eventLogDataList;
	}

	/**
	 * Get the user's display name (including the eid), looking up in a map first.
	 * @param userEid
	 * @param userMap
	 * @return
	 */
	private String getUserDisplay(String userEid, Map<String, User> userMap) {

	   String display = userEid;
	   User user = userMap.get(userEid);
	   if (user == null) {
	      try {
	         user = UserDirectoryService.getUserByEid(userEid);
	         userMap.put(userEid, user);
	      } catch (UserNotDefinedException e) {
	         log.warn("Unable to get user with eid: " + userEid);
	      }
	   }
	   if (user!=null)
	      display = user.getSortName() + " (" + userEid + ")";
	   
	   return display;
	}
	
   public List<Object[]> getTitlesFromEventLogBySite(final String siteId) {
	   String query = "select distinct eld.assessmentId, eld.title from EventLogData as eld, PublishedAssessmentData pa"
		         + " where eld.assessmentId = pa.publishedAssessmentId and eld.siteId = ? and pa.status <> 2"
		         + " order by lower(eld.title) asc";

      final String hql = query;
      final HibernateCallback hcb = new HibernateCallback() {
         public Object doInHibernate(Session session)
         throws HibernateException, SQLException {
            Query q = session.createQuery(hql);
            q.setString(0, siteId);

            return q.list();
         };
      };
      List<Object[]> list = (ArrayList<Object[]>) getHibernateTemplate().executeFind(hcb);
      return list;
   }
}
