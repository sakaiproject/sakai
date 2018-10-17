/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.assignment.impl.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.assignment.api.persistence.AssignmentRepository;
import org.sakaiproject.hibernate.HibernateCriterionUtils;
import org.sakaiproject.serialization.BasicSerializableRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by enietzel on 2/22/17.
 */
@Slf4j
@Transactional(readOnly = true)
public class AssignmentRepositoryImpl extends BasicSerializableRepository<Assignment, String> implements AssignmentRepository {

    @Override
    public Assignment findAssignment(String id) {
        return findOne(id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Assignment> findAssignmentsBySite(String siteId) {
        return startCriteriaQuery()
                .add(Restrictions.eq("context", siteId))
                .add(Restrictions.eq("deleted", Boolean.FALSE))
                .list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Assignment> findDeletedAssignmentsBySite(String siteId) {
        return startCriteriaQuery()
                .add(Restrictions.eq("context", siteId))
                .add(Restrictions.eq("deleted", Boolean.TRUE))
                .list();
    }

    @Override
    public List<String> findAllAssignmentIds() {
        return startCriteriaQuery()
                .setProjection(Projections.property("id"))
                .list();
    }

    @Override
    @Transactional
    public void newAssignment(Assignment assignment) {
        if (!existsAssignment(assignment.getId())) {
            assignment.setDateCreated(Instant.now());
            sessionFactory.getCurrentSession().persist(assignment);
        }
    }

    @Override
    @Transactional
    public boolean existsAssignment(String assignmentId) {
        if (assignmentId != null && exists(assignmentId)) {
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void deleteAssignment(String assignmentId) {
        Assignment assignment = findOne(assignmentId);
        if (assignment != null) {
            delete(assignment);
        }
    }

    @Override
    @Transactional
    public void deleteSubmission(String submissionId) {
        Session session = sessionFactory.getCurrentSession();
        AssignmentSubmission submission = (AssignmentSubmission) session.get(AssignmentSubmission.class, submissionId);
        if (submission != null) {
            Assignment assignment = submission.getAssignment();
            // must call refresh here to ensure the collections are initialized before changing, this is due to lazy loaded entities
            session.refresh(assignment);
            assignment.getSubmissions().remove(submission);
            session.update(assignment);
            session.flush();
        }
    }

    @Override
    @Transactional
    public void softDeleteAssignment(String assignmentId) {
        Assignment assignment = findOne(assignmentId);
        assignment.setDeleted(Boolean.TRUE);
        update(assignment);
    }

    @Override
    public AssignmentSubmission findSubmission(String submissionId) {
        return (AssignmentSubmission) sessionFactory.getCurrentSession().get(AssignmentSubmission.class, submissionId);
    }

    @Override
    @Transactional
    public void updateSubmission(AssignmentSubmission submission) {
        if (existsSubmission(submission.getId())) {
            sessionFactory.getCurrentSession().merge(submission);
        }
    }

    @Override
    @Transactional
    public boolean existsSubmission(String submissionId) {
        if (submissionId != null && sessionFactory.getCurrentSession().get(AssignmentSubmission.class, submissionId) != null) {
            return true;
        }
        return false;
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRES_NEW)
    public AssignmentSubmission newSubmission(String assignmentId, Optional<String> groupId, Optional<Set<AssignmentSubmissionSubmitter>> submitters, Optional<Set<String>> feedbackAttachments, Optional<Set<String>> submittedAttachments, Optional<Map<String, String>> properties) {
        Assignment assignment = findAssignment(assignmentId);
        if (assignment != null) {
            AssignmentSubmission submission = new AssignmentSubmission();
            submission.setDateCreated(Instant.now());
            submitters.ifPresent(submission::setSubmitters);
            submitters.ifPresent(s -> s.forEach(submitter -> submitter.setSubmission(submission)));
            feedbackAttachments.ifPresent(submission::setFeedbackAttachments);
            submittedAttachments.ifPresent(submission::setAttachments);
            properties.ifPresent(submission::setProperties);
            if (assignment.getIsGroup()) { groupId.ifPresent(submission::setGroupId); }

            submission.setAssignment(assignment);
            assignment.getSubmissions().add(submission);

            sessionFactory.getCurrentSession().persist(assignment);
            return submission;
        }
        return null;
    }

    @Override
    public AssignmentSubmission findSubmissionForUser(String assignmentId, String userId) {
        return (AssignmentSubmission) sessionFactory.getCurrentSession().createCriteria(AssignmentSubmission.class)
                .add(Restrictions.eq("assignment.id", assignmentId))
                .createAlias("submitters", "s")
                .add(Restrictions.eq("s.submitter", userId))
                .uniqueResult();
    }

    @Override
    public AssignmentSubmission findSubmissionForGroup(String assignmentId, String groupId) {
        return (AssignmentSubmission) sessionFactory.getCurrentSession().createCriteria(AssignmentSubmission.class)
                .add(Restrictions.eq("assignment.id", assignmentId))
                .add(Restrictions.eq("groupId", groupId))
                .uniqueResult();
    }

    @Override
    public void initializeAssignment(Assignment assignment) {
        sessionFactory.getCurrentSession().refresh(assignment);
    }

    @Override
    public long countAssignmentSubmissions(String assignmentId, Boolean graded, Boolean hasSubmissionDate, Boolean userSubmission, List<String> userIds) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(AssignmentSubmission.class)
                .setProjection(Projections.countDistinct("id"))
                .add(Restrictions.eq("assignment.id", assignmentId))
                .add(Restrictions.eq("submitted", Boolean.TRUE))
                .createAlias("submitters", "s");

        if (graded != null) {
            criteria.add(Restrictions.eq("graded", graded));
        }
        if (hasSubmissionDate != null) {
            criteria.add(hasSubmissionDate ? Restrictions.isNotNull("dateSubmitted") : Restrictions.isNull("dateSubmitted"));
        }
        if (userSubmission != null) {
            criteria.add(Restrictions.eq("userSubmission", userSubmission));
        }
        if (userIds != null) {
            if (userIds.isEmpty()) {
                return 0; // if we have an empty list then we return always return 0
            } else {
                criteria.add(HibernateCriterionUtils.CriterionInRestrictionSplitter("s.submitter", userIds));
            }
        }
        return ((Number) criteria.uniqueResult()).longValue();
    }

    @Override
    public void resetAssignment(Assignment assignment) {
        if (assignment != null && assignment.getId() != null) {
            sessionFactory.getCache().evictEntity(Assignment.class, assignment.getId());
        }
    }
}
