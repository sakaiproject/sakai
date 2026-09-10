/*
 * Copyright (c) 2016, The Apereo Foundation
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
 *
 */

package org.sakaiproject.tool.assessment.facade;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.ExtendedTime;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
@Slf4j
@Transactional
public class ExtendedTimeQueries implements ExtendedTimeQueriesAPI {

    @Setter private SessionFactory sessionFactory;

    /**
     * init
     */
    public void init () {
        log.info("init()");
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public List<ExtendedTime> getEntriesForAss(AssessmentBaseIfc ass) {
        log.debug("getEntriesForAss " + ass.getAssessmentBaseId());

        try {
            Session session = sessionFactory.getCurrentSession();
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<ExtendedTime> cq = cb.createQuery(ExtendedTime.class);
            Root<ExtendedTime> root = cq.from(ExtendedTime.class);

            cq.select(root).where(cb.equal(root.get("assessmentId"), ass.getAssessmentBaseId()));

            return session.createQuery(cq).getResultList();
        } catch (DataAccessException e) {
            log.error("Failed to get Extended TimeEntries for Assessment: " + ass.getAssessmentBaseId(), e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public List<ExtendedTime> getEntriesForPub(PublishedAssessmentIfc pub) {
        log.debug("getEntriesForPub " + pub.getPublishedAssessmentId());

        try {
            Session session = sessionFactory.getCurrentSession();
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<ExtendedTime> cq = cb.createQuery(ExtendedTime.class);
            Root<ExtendedTime> root = cq.from(ExtendedTime.class);

            cq.select(root)
              .where(cb.equal(root.get("publishedAssessmentId"), pub.getPublishedAssessmentId()));

            return session.createQuery(cq).getResultList();
        } catch (DataAccessException e) {
            log.error("Failed to get Extended Time Entries for Published Assessment: " + pub.getPublishedAssessmentId(), e);
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ExtendedTime> getEntriesForPublishedAssessments(List<Long> publishedAssessmentIds, String userId, List<String> groupIds) {
        if (publishedAssessmentIds == null || publishedAssessmentIds.isEmpty()) {
            return new ArrayList<>(0);
        }

        try {
            final boolean hasGroups = groupIds != null && !groupIds.isEmpty();

            Session session = sessionFactory.getCurrentSession();
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<ExtendedTime> cq = cb.createQuery(ExtendedTime.class);
            Root<ExtendedTime> root = cq.from(ExtendedTime.class);

            Predicate pubIdPredicate = root.get("pubAssessment").get("publishedAssessmentId").in(publishedAssessmentIds);
            Predicate userOrGroupPredicate = hasGroups
                ? cb.or(cb.equal(root.get("user"), userId), root.get("group").in(groupIds))
                : cb.equal(root.get("user"), userId);

            cq.select(root).where(cb.and(pubIdPredicate, userOrGroupPredicate));

            return session.createQuery(cq).getResultList();
        } catch (DataAccessException e) {
            log.error("Failed to get extended time entries for published assessments: {}", publishedAssessmentIds, e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public ExtendedTime getEntryForPubAndUser(PublishedAssessmentIfc pub, String userId) {
        log.debug("getEntryForPubAndUser, pub: '" + pub.getPublishedAssessmentId() + "' User: " + userId);

        return getPubAndX(QUERY_GET_ENTRY_FOR_PUB_N_USER, pub, USER_ID, userId);
    }

    /**
     * {@inheritDoc}
     */
    public ExtendedTime getEntryForPubAndGroup (PublishedAssessmentIfc pub, String groupId) {
        log.debug("getEntryForPubAndGroup, pub: '" + pub.getPublishedAssessmentId() + "' group: " + groupId);

        return getPubAndX(QUERY_GET_ENTRY_FOR_PUB_N_GROUP, pub, GROUP, groupId);
    }

    /**
     * {@inheritDoc}
     */
    public boolean updateEntry(ExtendedTime e) {
        log.debug("updating entry assessment: '" + e.getAssessmentId() + "' pubId: '" + e.getPubAssessmentId() + "' user: '" + e.getUser() + "' group: " + e.getGroup());

        try {
            sessionFactory.getCurrentSession().merge(e);
            return true;
        } catch (DataAccessException de) {
            log.error("Error updating extended time entry" , de);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void updateEntries(List<ExtendedTime> entries) {
        entries.forEach(this::updateEntry);
    }

    /**
     * {@inheritDoc}
     */
    public boolean deleteEntry(final ExtendedTime e) {
        log.debug("Removing ExtendedTime entry id: " + e.getId());

       try {
    	   sessionFactory.getCurrentSession().remove(sessionFactory.getCurrentSession().merge(e));
           return true;
       } catch (DataAccessException de) {
           log.error("Failed to delete extendedTime entry, id: " + e.getId() + ".", de);
           return false;
       }
    }

    public boolean deleteEntriesForPub(PublishedAssessmentIfc pub) {
        if (pub == null || pub.getPublishedAssessmentId() == null) {
            return true;
        }

        Long publishedAssessmentId = pub.getPublishedAssessmentId();
        log.debug("Removing ExtendedTime entries for published assessment id: {}", publishedAssessmentId);

        try {
            Session session = sessionFactory.getCurrentSession();
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaDelete<ExtendedTime> delete = cb.createCriteriaDelete(ExtendedTime.class);
            Root<ExtendedTime> e = delete.from(ExtendedTime.class);

            delete.where(cb.equal(e.get("pubAssessment").get("publishedAssessmentId"), publishedAssessmentId));

            session.createQuery(delete).executeUpdate();
            return true;
        } catch (DataAccessException de) {
            log.error("Failed to delete extended time entries for published assessment id: {}.", publishedAssessmentId, de);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private ExtendedTime getPubAndX(final String query, final PublishedAssessmentIfc pub, final String secondParam, final String secondParamValue) {
        try{
            Session session = sessionFactory.getCurrentSession();
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<ExtendedTime> cq = cb.createQuery(ExtendedTime.class);
            Root<ExtendedTime> root = cq.from(ExtendedTime.class);

            cq.select(root).where(cb.and(
                cb.equal(root.get("pubAssessment").get("publishedAssessmentId"),
                    pub.getPublishedAssessmentId()),
                cb.equal(root.get(secondParam), secondParamValue)
            ));

            return session.createQuery(cq).uniqueResult();
        } catch (DataAccessException e) {
            log.error("Failed to get extended time for pub: " + pub.getPublishedAssessmentId() + " and user/group: " + secondParamValue, e);
            return null;
        }
    }

    public ExtendedTime getEntry(final String entryId){
        log.debug("getEntry " + entryId);
        try {
             Session session = sessionFactory.getCurrentSession();
             CriteriaBuilder cb = session.getCriteriaBuilder();
             CriteriaQuery<ExtendedTime> cq = cb.createQuery(ExtendedTime.class);
             Root<ExtendedTime> root = cq.from(ExtendedTime.class);

             cq.select(root).where(cb.equal(root.get("entryId"), Long.valueOf(entryId)));

             return session.createQuery(cq).uniqueResult();
        } catch (DataAccessException e) {
            log.error("Failed to get Extended Time Entries for Published Assessment: " + entryId, e);
            return null;
        }
    }
}
