/**
 * Copyright (c) 2007-2016 The Apereo Foundation
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
/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational 
* Community License, Version 2.0 (the "License"); you may not use this file 
* except in compliance with the License. You may obtain a copy of the 
* License at:
*
* http://opensource.org/licenses/ecl2.txt
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.signup.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.dao.DataAccessException;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;

/**
 * <p>
 * SignupMeetingServiceImpl is an implementation of SignupMeetingDao interface,
 * which provides methods to access the database storage for retrieving,
 * creating, updating and removing SignupMeeting objects.
 * 
 * @author Peter Liu
 * 
 * </p>
 */
@Slf4j
public class SignupMeetingDaoImpl implements SignupMeetingDao {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private Session currentSession() {
        return sessionFactory.getCurrentSession();
    }

	public void init() {
		log.debug("init");
	}

	/**
	 * {@inheritDoc}
	 */
    public List<SignupMeeting> getAllSignupMeetings(String siteId) {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<SignupMeeting> cq = cb.createQuery(SignupMeeting.class);
        Root<SignupMeeting> root = cq.from(SignupMeeting.class);
        Join<Object, Object> sites = root.join("signupSites");
        cq.select(root).distinct(true)
          .where(cb.equal(sites.get("siteId"), siteId))
          .orderBy(cb.asc(root.get("startTime")));
        return currentSession().createQuery(cq).getResultList();
    }

	/**
	 * {@inheritDoc}
	 */
    public List<SignupMeeting> getSignupMeetings(String siteId, Date searchEndDate) {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<SignupMeeting> cq = cb.createQuery(SignupMeeting.class);
        Root<SignupMeeting> root = cq.from(SignupMeeting.class);
        Join<Object, Object> sites = root.join("signupSites");
        cq.select(root).distinct(true)
          .where(cb.and(
                  cb.lessThanOrEqualTo(root.<Date>get("startTime"), searchEndDate),
                  cb.equal(sites.get("siteId"), siteId)
          ))
          .orderBy(cb.asc(root.get("startTime")));
        return currentSession().createQuery(cq).getResultList();
    }

	/**
	 * {@inheritDoc}
	 */
    public List<SignupMeeting> getSignupMeetings(String siteId, Date startDate, Date endDate) {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<SignupMeeting> cq = cb.createQuery(SignupMeeting.class);
        Root<SignupMeeting> root = cq.from(SignupMeeting.class);
        Join<Object, Object> sites = root.join("signupSites");
        cq.select(root).distinct(true)
          .where(cb.and(
                  cb.greaterThanOrEqualTo(root.<Date>get("endTime"), startDate),
                  cb.lessThan(root.<Date>get("startTime"), endDate),
                  cb.equal(sites.get("siteId"), siteId)
          ))
          .orderBy(cb.asc(root.get("startTime")));
        return currentSession().createQuery(cq).getResultList();
    }
	
    public List<SignupMeeting> getSignupMeetingsInSite(String siteId, Date startDate, Date endDate) {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<SignupMeeting> cq = cb.createQuery(SignupMeeting.class);
        Root<SignupMeeting> root = cq.from(SignupMeeting.class);
        Join<Object, Object> sites = root.join("signupSites");
        cq.select(root).distinct(true)
          .where(cb.and(
                  cb.greaterThanOrEqualTo(root.<Date>get("endTime"), startDate),
                  cb.lessThan(root.<Date>get("startTime"), endDate),
                  cb.equal(sites.get("siteId"), siteId)
          ))
          .orderBy(cb.asc(root.get("startTime")));
        return currentSession().createQuery(cq).getResultList();
    }
	
	/**
	 * {@inheritDoc}
	 */
    public List<SignupMeeting> getSignupMeetingsInSites(List<String> siteIds, Date startDate, Date endDate) {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<SignupMeeting> cq = cb.createQuery(SignupMeeting.class);
        Root<SignupMeeting> root = cq.from(SignupMeeting.class);
        Join<Object, Object> sites = root.join("signupSites");
        Expression<String> siteIdPath = sites.get("siteId");
        Predicate inSites = siteIdPath.in(siteIds);
        cq.select(root).distinct(true)
          .where(cb.and(
                  cb.greaterThanOrEqualTo(root.<Date>get("endTime"), startDate),
                  cb.lessThan(root.<Date>get("startTime"), endDate),
                  inSites
          ))
          .orderBy(cb.asc(root.get("startTime")));
        return currentSession().createQuery(cq).getResultList();
    }

	/**
	 * {@inheritDoc}
	 */
    public List<SignupMeeting> getRecurringSignupMeetings(String siteId, Long recurrenceId, Date currentTime) {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<SignupMeeting> cq = cb.createQuery(SignupMeeting.class);
        Root<SignupMeeting> root = cq.from(SignupMeeting.class);
        Join<Object, Object> sites = root.join("signupSites");
        cq.select(root).distinct(true)
          .where(cb.and(
                  cb.equal(root.get("recurrenceId"), recurrenceId),
                  cb.greaterThan(root.<Date>get("endTime"), currentTime),
                  cb.equal(sites.get("siteId"), siteId)
          ))
          .orderBy(cb.asc(root.get("startTime")));
        return currentSession().createQuery(cq).getResultList();
    }

	/**
	 * {@inheritDoc}
	 */
    public Long saveMeeting(SignupMeeting signupMeeting) {
        return (Long) currentSession().save(signupMeeting);
    }

	/**
	 * {@inheritDoc}
	 */
    public void saveMeetings(List<SignupMeeting> signupMeetings) {
        if (signupMeetings != null && signupMeetings.size() > 0) {
            SignupMeeting sm = signupMeetings.get(0);
            if (sm.isRecurredMeeting()) {
                Long reRecurId = (Long) currentSession().save(sm);
                // use the first unique meeting id as the recurrenceId for all recurring meetings
                for (SignupMeeting sMeeting : signupMeetings) {
                    sMeeting.setRecurrenceId(reRecurId);
                }
            }
            for (SignupMeeting signupMeeting : signupMeetings) {
                currentSession().saveOrUpdate(signupMeeting);
            }
        }
    }

	/**
	 * {@inheritDoc}
	 */
    public SignupMeeting loadSignupMeeting(Long meetingId) {
        return currentSession().get(SignupMeeting.class, meetingId);
    }

	/**
	 * {@inheritDoc}
	 */
    public void updateMeeting(SignupMeeting meeting) throws DataAccessException {
        currentSession().update(meeting);
    }
	

	/**
	 * {@inheritDoc}
	 */
    public void updateMeetings(List<SignupMeeting> meetings) throws DataAccessException {
        for (SignupMeeting meeting : meetings) {
            currentSession().saveOrUpdate(meeting);
        }
    }
	
	/**
	 * {@inheritDoc}
	 */
    public void updateModifiedMeetings(List<SignupMeeting> meetings, List<SignupTimeslot> removedTimeslots)
            throws DataAccessException {
        for (SignupMeeting meeting : meetings) {
            currentSession().saveOrUpdate(meeting);
        }

        // remove the deleted timeslot and related attendees/wait-list people
        if (removedTimeslots != null && !removedTimeslots.isEmpty()) {
            for (SignupTimeslot ts : removedTimeslots) {
                long tsId = ts.getId();
                SignupTimeslot sTimeslot = loadSignupTimeslot(tsId);
                if (sTimeslot != null) {
                    currentSession().delete(sTimeslot);
                }
            }
        }
    }
	
    private SignupTimeslot loadSignupTimeslot(Long timeslotId) {
        return currentSession().get(SignupTimeslot.class, timeslotId);
    }

	/**
	 * {@inheritDoc}
	 */
    public void removeMeetings(List<SignupMeeting> meetings) {
        for (SignupMeeting meeting : meetings) {
            SignupMeeting attached = (SignupMeeting) currentSession().merge(meeting);
            currentSession().delete(attached);
        }
    }

    private Collection mergeAll(Collection entities) {
        List merged = new ArrayList();
        entities.forEach(ent -> merged.add(currentSession().merge(ent)));
        return merged;
    }

	/**
	 * {@inheritDoc}
	 */
	public boolean isEventExisted(Long eventId) {
		//TODO need test with lazy loading
		SignupMeeting ls = loadSignupMeeting(eventId);
		if (ls ==null)
			return false;
		
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
    public int getAutoReminderTotalEventCounts(Date startDate, Date endDate) {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<SignupMeeting> root = cq.from(SignupMeeting.class);
        cq.select(cb.count(root))
          .where(cb.and(
                  cb.isTrue(root.get("autoReminder")),
                  cb.between(root.<Date>get("startTime"), startDate, endDate)
          ));
        Long count = currentSession().createQuery(cq).getSingleResult();
        return count == null ? 0 : count.intValue();
    }
	
	/**
	 * {@inheritDoc}
	 */
    public List<SignupMeeting> getAutoReminderSignupMeetings(Date startDate, Date endDate) {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<SignupMeeting> cq = cb.createQuery(SignupMeeting.class);
        Root<SignupMeeting> root = cq.from(SignupMeeting.class);
        cq.select(root).distinct(true)
          .where(cb.and(
                  cb.isTrue(root.get("autoReminder")),
                  cb.lessThanOrEqualTo(root.<Date>get("startTime"), endDate),
                  cb.greaterThanOrEqualTo(root.<Date>get("endTime"), startDate)
          ))
          .orderBy(cb.asc(root.get("startTime")));
        return currentSession().createQuery(cq).getResultList();
    }

	@Override
    public List<String> getAllCategories(String siteId) throws DataAccessException {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<SignupMeeting> root = cq.from(SignupMeeting.class);
        Join<Object, Object> sites = root.join("signupSites");
        cq.select(root.get("category")).distinct(true)
          .where(cb.equal(sites.get("siteId"), siteId))
          .orderBy(cb.asc(root.get("category")));
        List<String> results = currentSession().createQuery(cq).getResultList();
        return (results != null && !results.isEmpty()) ? results : null;
    }

	@Override
    public List<String> getAllLocations(String siteId) throws DataAccessException {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<SignupMeeting> root = cq.from(SignupMeeting.class);
        Join<Object, Object> sites = root.join("signupSites");
        cq.select(root.get("location")).distinct(true)
          .where(cb.equal(sites.get("siteId"), siteId))
          .orderBy(cb.asc(root.get("location")));

        List<String> results = currentSession().createQuery(cq).getResultList();
        return (results != null && !results.isEmpty()) ? results : null;
    }

}
