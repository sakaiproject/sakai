/**
 * Copyright (c) 2005-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.tool.assessment.facade;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.sakaiproject.tool.assessment.data.dao.assessment.EventLogData;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

@Slf4j
public class EventLogFacadeQueries extends HibernateDaoSupport implements EventLogFacadeQueriesAPI {

	public void saveOrUpdateEventLog(EventLogFacade eventLog){
		EventLogData data = (EventLogData)  eventLog.getData();

		int retryCount = PersistenceService.getInstance().getRetryCount();
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

		final HibernateCallback<List<EventLogData>> hcb = session -> {
			Query q = session.createQuery(
					"select eld from EventLogData as eld"
							+ " where eld.processId = :id"
							+ " order by eld.id desc");
			q.setLong("id", assessmentGradingId);

            return q.list();
        };
		List<EventLogData> list = getHibernateTemplate().execute(hcb);

		ArrayList<EventLogData> eventLogList = new ArrayList<EventLogData>();
		Map<String, User> userMap = new HashMap<String, User>();
		for(EventLogData e : list) {
			e.setUserDisplay(getUserDisplay(e.getUserEid(), userMap));
			eventLogList.add(e);
		}		

		return eventLogList;
	}

	public List<EventLogData> getDataBySiteId(final String siteId) {

		final HibernateCallback<List<EventLogData>> hcb = session -> {
            Query q = session.createQuery(
                    "select eld from EventLogData as eld"
                            + " where eld.siteId = :site"
                            + " order by eld.assessmentId asc, eld.userEid asc"
            );
            q.setString("site", siteId);

            return q.list();
        };
		List<EventLogData> list = getHibernateTemplate().execute(hcb);
		
		ArrayList<EventLogData> eventLogDataList = new ArrayList<EventLogData>();
		Map<String, User> userMap = new HashMap<String, User>();
		for(EventLogData e : list) {
			e.setUserDisplay(getUserDisplay(e.getUserEid(), userMap));
			eventLogDataList.add(e);
		}		

		return eventLogDataList;
	}
	
	public List<EventLogData> getEventLogData(final String siteId, final Long assessmentId, final String userFilter) {
	   String query = "select eld from EventLogData as eld where eld.siteId = :site";

	   if (assessmentId > -1) {
	      query += " and eld.assessmentId = :id";
	   }
	   
	   query += " order by eld.assessmentId asc, eld.userEid asc";

      final String hql = query;
      final HibernateCallback<List<EventLogData>> hcb = session -> {
         Query q = session.createQuery(hql);
         q.setString("site", siteId);
         if (assessmentId > -1) {
            q.setLong("id", assessmentId);
         }

         return q.list();
      };
      List<EventLogData> list = getHibernateTemplate().execute(hcb);
      
      List<EventLogData> eventLogDataList = new ArrayList<EventLogData>();
      Map<String, User> userMap = new HashMap<String, User>();
      for(EventLogData e : list) {
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

      final HibernateCallback<List<Object[]>> hcb = session -> {
         Query q = session.createQuery(
                 "select distinct eld.assessmentId, eld.title from EventLogData as eld"
                         + " where eld.siteId = :site"
                         + " order by lower(eld.title) asc"
         );
         q.setString("site", siteId);

         return q.list();
      };
      List<Object[]> list = getHibernateTemplate().execute(hcb);
      return list;
   }
}
