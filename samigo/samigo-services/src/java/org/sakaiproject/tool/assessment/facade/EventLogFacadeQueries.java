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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.EventLogData;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
public class EventLogFacadeQueries implements EventLogFacadeQueriesAPI {

	@Setter private SessionFactory sessionFactory;

	public void saveOrUpdateEventLog(EventLogFacade eventLog){
		EventLogData data = (EventLogData)  eventLog.getData();

		int retryCount = PersistenceService.getInstance().getRetryCount();
		while (retryCount > 0) {
			try {
				sessionFactory.getCurrentSession().merge(data);
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

		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<EventLogData> cq = cb.createQuery(EventLogData.class);
		Root<EventLogData> eld = cq.from(EventLogData.class);

		cq.select(eld)
			.where(cb.equal(eld.get("processId"), assessmentGradingId))
			.orderBy(cb.desc(eld.get("id")));

		List<EventLogData> list = session.createQuery(cq).getResultList();

		ArrayList<EventLogData> eventLogList = new ArrayList<EventLogData>();
		Map<String, User> userMap = new HashMap<String, User>();
		for(EventLogData e : list) {
			e.setUserDisplay(getUserDisplay(e.getUserEid(), userMap));
			eventLogList.add(e);
		}

		return eventLogList;
	}

	public List<EventLogData> getDataBySiteId(final String siteId) {

		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<EventLogData> cq = cb.createQuery(EventLogData.class);
		Root<EventLogData> eld = cq.from(EventLogData.class);

		cq.select(eld)
			.where(cb.equal(eld.get("siteId"), siteId))
			.orderBy(cb.asc(eld.get("assessmentId")), cb.asc(eld.get("userEid")));

		List<EventLogData> list = session.createQuery(cq).getResultList();

		ArrayList<EventLogData> eventLogDataList = new ArrayList<EventLogData>();
		Map<String, User> userMap = new HashMap<String, User>();
		for(EventLogData e : list) {
			e.setUserDisplay(getUserDisplay(e.getUserEid(), userMap));
			eventLogDataList.add(e);
		}

		return eventLogDataList;
	}

	public List<EventLogData> getEventLogData(final String siteId, final Long assessmentId) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<EventLogData> cq = cb.createQuery(EventLogData.class);
		Root<EventLogData> eld = cq.from(EventLogData.class);

		Predicate where = cb.equal(eld.get("siteId"), siteId);

		if (assessmentId > -1) {
			where = cb.and(where, cb.equal(eld.get("assessmentId"), assessmentId));
		}

		cq.select(eld)
			.where(where)
			.orderBy(cb.asc(eld.get("assessmentId")), cb.asc(eld.get("userEid")));

		List<EventLogData> list = session.createQuery(cq).getResultList();

		List<EventLogData> eventLogDataList = new ArrayList<EventLogData>();
		Map<String, User> userMap = new HashMap<String, User>();
		for(EventLogData e : list) {
			e.setUserDisplay(getUserDisplay(e.getUserEid(), userMap));
			eventLogDataList.add(e);
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

		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> cq = cb.createTupleQuery();
		Root<EventLogData> eld = cq.from(EventLogData.class);

		cq.multiselect(cb.array(eld.get("assessmentId"), eld.get("title")))
		.distinct(true)
		.where(cb.equal(eld.get("siteId"), siteId))
		.orderBy(cb.asc(cb.lower(eld.get("title"))));

		List<Tuple> tuples = session.createQuery(cq).getResultList();

		List<Object[]> result = new ArrayList<>(tuples.size());
		for (Tuple t : tuples) {
			result.add(t.toArray());
		}
		return result;
	}
}
