/**
 * Copyright (c) 2024 The Apereo Foundation
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
package org.sakaiproject.meetings.impl.persistence;

import org.sakaiproject.meetings.api.persistence.MeetingRepository;
import org.sakaiproject.serialization.BasicSerializableRepository;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.meetings.api.model.AttendeeType;
import org.sakaiproject.meetings.api.model.Meeting;
import org.sakaiproject.meetings.api.model.MeetingAttendee;

public class MeetingRepositoryImpl extends BasicSerializableRepository<Meeting, String> implements MeetingRepository {

    public Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }
    
    public Optional<Meeting> findById(String id) {
        Meeting meeting = (Meeting) startCriteriaQuery().add(Restrictions.eq("id", id)).uniqueResult();
        return Optional.ofNullable(meeting);
    }
    
    public Meeting findMeetingById(String id) {
        return (Meeting) startCriteriaQuery().add(Restrictions.eq("id", id)).uniqueResult();
    }

    @Override
    public void deleteById(String id) {
        getCurrentSession().createQuery("delete from Meeting where id = :id").setParameter("id", id).executeUpdate();
    }
    
    @Override
    public List<Meeting> getSiteMeetings(String siteId) {
        CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<Meeting> query = criteriaBuilder.createQuery(Meeting.class);
        Root<Meeting> root = query.from(Meeting.class);
        Predicate siteRestriction = criteriaBuilder.equal(root.get("siteId"), siteId);
        query.select(root).where(siteRestriction).distinct(true);
        return getCurrentSession().createQuery(query).getResultList();
    }
    
    @Override
    public List<Meeting> getMeetings(String userId, String siteId, List<String> groupIds) {
        CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<Meeting> query = criteriaBuilder.createQuery(Meeting.class);
        Root<Meeting> root = query.from(Meeting.class);
        Join<Meeting, MeetingAttendee> joinAttendees = root.join("attendees");
        Predicate orClause = criteriaBuilder.disjunction();
        if (userId != null) {
            Predicate userRestriction = criteriaBuilder.and(
                    criteriaBuilder.equal(joinAttendees.get("type"), AttendeeType.USER),
                    criteriaBuilder.equal(joinAttendees.get("objectId"), userId));
            orClause.getExpressions().add(userRestriction);
        }
        if (siteId != null) {
            Predicate siteRestriction = criteriaBuilder.and(
                    criteriaBuilder.equal(joinAttendees.get("type"), AttendeeType.SITE),
                    criteriaBuilder.equal(joinAttendees.get("objectId"), siteId));
            orClause.getExpressions().add(siteRestriction);
        }
        if (!CollectionUtils.isEmpty(groupIds)) {
            Predicate groupRestriction = criteriaBuilder.and(
                    criteriaBuilder.equal(joinAttendees.get("type"), AttendeeType.GROUP),
                    joinAttendees.get("objectId").in(groupIds));
            orClause.getExpressions().add(groupRestriction);
        }
        Predicate where = criteriaBuilder.and(criteriaBuilder.equal(root.get("siteId"), siteId), orClause);
        query.select(root).where(where);

        List<Meeting> ret = getCurrentSession().createQuery(query).getResultList();
        return ret.stream().distinct().collect(Collectors.toList());
    }
    
}
