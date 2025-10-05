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
import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
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
    @SuppressWarnings("unchecked")
    public List<SignupMeeting> getAllSignupMeetings(String siteId) {
        DetachedCriteria criteria = DetachedCriteria.forClass(SignupMeeting.class)
                .addOrder(Order.asc("startTime"))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .createCriteria("signupSites")
                .add(Restrictions.eq("siteId", siteId));
        return (List<SignupMeeting>) criteria.getExecutableCriteria(currentSession()).list();
    }

	/**
	 * {@inheritDoc}
	 */
    @SuppressWarnings("unchecked")
    public List<SignupMeeting> getSignupMeetings(String siteId, Date searchEndDate) {
        DetachedCriteria criteria = DetachedCriteria.forClass(SignupMeeting.class)
                .add(Restrictions.le("startTime", searchEndDate))
                .addOrder(Order.asc("startTime"))
                .createCriteria("signupSites")
                .add(Restrictions.eq("siteId", siteId));

        return (List<SignupMeeting>) criteria.getExecutableCriteria(currentSession()).list();
    }

	/**
	 * {@inheritDoc}
	 */
    @SuppressWarnings("unchecked")
    public List<SignupMeeting> getSignupMeetings(String siteId, Date startDate, Date endDate) {
        DetachedCriteria criteria = DetachedCriteria.forClass(SignupMeeting.class)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .add(Restrictions.ge("endTime", startDate))
                .add(Restrictions.lt("startTime", endDate))
                .addOrder(Order.asc("startTime"))
                .createCriteria("signupSites")
                .add(Restrictions.eq("siteId", siteId));

        return (List<SignupMeeting>) criteria.getExecutableCriteria(currentSession()).list();
    }
	
    @SuppressWarnings("unchecked")
    public List<SignupMeeting> getSignupMeetingsInSite(String siteId, Date startDate, Date endDate) {
        DetachedCriteria criteria = DetachedCriteria.forClass(SignupMeeting.class)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .add(Restrictions.ge("endTime", startDate))
                .add(Restrictions.lt("startTime", endDate))
                .addOrder(Order.asc("startTime"))
                .createCriteria("signupSites")
                .add(Restrictions.eq("siteId", siteId));

        return (List<SignupMeeting>) criteria.getExecutableCriteria(currentSession()).list();
    }
	
	/**
	 * {@inheritDoc}
	 */
    @SuppressWarnings("unchecked")
    public List<SignupMeeting> getSignupMeetingsInSites(List<String> siteIds, Date startDate, Date endDate) {
        DetachedCriteria criteria = DetachedCriteria.forClass(SignupMeeting.class)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .add(Restrictions.ge("endTime", startDate))
                .add(Restrictions.lt("startTime", endDate))
                .addOrder(Order.asc("startTime"))
                .createCriteria("signupSites")
                .add(Restrictions.in("siteId", siteIds));

        return (List<SignupMeeting>) criteria.getExecutableCriteria(currentSession()).list();
    }

	/**
	 * {@inheritDoc}
	 */
    @SuppressWarnings("unchecked")
    public List<SignupMeeting> getRecurringSignupMeetings(String siteId, Long recurrenceId, Date currentTime) {
        DetachedCriteria criteria = DetachedCriteria.forClass(SignupMeeting.class)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .add(Restrictions.eq("recurrenceId", recurrenceId))
                .add(Restrictions.gt("endTime", currentTime))
                .addOrder(Order.asc("startTime"))
                .createCriteria("signupSites")
                .add(Restrictions.eq("siteId", siteId));

        return (List<SignupMeeting>) criteria.getExecutableCriteria(currentSession()).list();
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
        DetachedCriteria criteria = DetachedCriteria.forClass(SignupMeeting.class)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .add(Restrictions.eq("id", meetingId));
        List<SignupMeeting> ls = (List<SignupMeeting>) criteria.getExecutableCriteria(currentSession()).list();
        if (ls == null || ls.isEmpty()) return null;
        return ls.get(0);
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
	
    @SuppressWarnings("unchecked")
    private SignupTimeslot loadSignupTimeslot(Long timeslotId) {
        DetachedCriteria criteria = DetachedCriteria.forClass(SignupTimeslot.class)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .add(Restrictions.eq("id", timeslotId));
        List<SignupTimeslot> ls = (List<SignupTimeslot>) criteria.getExecutableCriteria(currentSession()).list();
        if (ls == null || ls.isEmpty()) return null;
        return ls.get(0);
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
        Number size = (Number) currentSession().createCriteria(SignupMeeting.class)
                .add(Restrictions.eq("autoReminder", true))
                .add(Restrictions.between("startTime", startDate, endDate))
                .setProjection(Projections.rowCount())
                .uniqueResult();
        return size == null ? 0 : size.intValue();
    }
	
	/**
	 * {@inheritDoc}
	 */
    @SuppressWarnings("unchecked")
    public List<SignupMeeting> getAutoReminderSignupMeetings(Date startDate, Date endDate) {
        DetachedCriteria criteria = DetachedCriteria.forClass(SignupMeeting.class)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .add(Restrictions.eq("autoReminder", true))
                // .add(Restrictions.between("startTime", startDate, endDate))
                .add(Restrictions.le("startTime", endDate))
                .add(Restrictions.ge("endTime", startDate))
                .addOrder(Order.asc("startTime"));

        return (List<SignupMeeting>) criteria.getExecutableCriteria(currentSession()).list();
    }

	@Override
    public List<String> getAllCategories(String siteId) throws DataAccessException {
        DetachedCriteria criteria = DetachedCriteria.forClass(SignupMeeting.class)
                .setProjection(Projections.distinct(Projections.projectionList()
                        .add(Projections.property("category"), "category")))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .addOrder(Order.asc("category"))
                .createCriteria("signupSites")
                .add(Restrictions.eq("siteId", siteId));

        List<String> categories = (List<String>) criteria.getExecutableCriteria(currentSession()).list();
        if (categories != null && !categories.isEmpty()) {
            return categories;
        }
        return null;
    }

	@Override
    public List<String> getAllLocations(String siteId) throws DataAccessException {
        DetachedCriteria criteria = DetachedCriteria.forClass(SignupMeeting.class)
                .setProjection(Projections.distinct(Projections.projectionList()
                        .add(Projections.property("location"), "location")))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .addOrder(Order.asc("location"))
                .createCriteria("signupSites")
                .add(Restrictions.eq("siteId", siteId));

        List<String> locations = (List<String>) criteria.getExecutableCriteria(currentSession()).list();

        if (locations != null && !locations.isEmpty()) {
            return locations;
        }
        return null;
    }

}
