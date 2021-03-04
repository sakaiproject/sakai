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
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.persister.collection.CollectionPropertyNames;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.assignment.api.persistence.AssignmentRepository;
import org.sakaiproject.hibernate.HibernateCriterionUtils;
import org.sakaiproject.serialization.BasicSerializableRepository;
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
        AssignmentSubmission submission = session.get(AssignmentSubmission.class, submissionId);
        if (submission != null) {
            log.info("Deleting submission {}", submission);
            Assignment assignment = submission.getAssignment();
            assignment.getSubmissions().remove(submission);
            session.delete(submission);
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
            submission.setDateModified(Instant.now());
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
    @Transactional
    public AssignmentSubmission newSubmission(String assignmentId, Optional<String> groupId, Optional<Set<AssignmentSubmissionSubmitter>> submitters, Optional<Set<String>> feedbackAttachments, Optional<Set<String>> submittedAttachments, Optional<Map<String, String>> properties) {
        Assignment assignment = findAssignment(assignmentId);
        if (assignment != null) {
            Session session = sessionFactory.getCurrentSession();
            // Since this transaction is going to add a submission to the assignment we lock the assignment
            // the lock is freed once transaction is committed or rolled back
            session.buildLockRequest(LockOptions.UPGRADE).setLockMode(LockMode.PESSIMISTIC_WRITE).lock(assignment);

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

            session.persist(assignment);
            return submission;
        }
        return null;
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public AssignmentSubmission findSubmissionForUser(String assignmentId, String userId) {
        List<AssignmentSubmission> submissions = sessionFactory.getCurrentSession().createCriteria(AssignmentSubmission.class)
                .add(Restrictions.eq("assignment.id", assignmentId))
                .createAlias("submitters", "s")
                .add(Restrictions.eq("s.submitter", userId))
                .list();

        switch (submissions.size()) {
            case 0: return null;
            case 1: return submissions.get(0);
            default:
                log.info("Duplicate submissions detected for assignment {} and user {} attempting to clean", assignmentId, userId);
                // when more than 1 was found it is considered a duplicate submission

                // find non user submissions with no text or submitted date
                List<AssignmentSubmission> removable = submissions.stream()
                        .filter(s -> !s.getUserSubmission() && StringUtils.isBlank(s.getSubmittedText()) && s.getDateSubmitted() == null)
                        .collect(Collectors.toList());
                if  (submissions.size() - removable.size() > 1) {
                    log.debug("{} to many submissions, trying more agressively", submissions.size() - removable.size());
                    // still to many lets be a little more aggressive finding those that are not returned and no grade
                    submissions.removeAll(removable);
                    submissions.stream().filter(s -> !s.getReturned() && s.getGrade() == null).forEach(removable::add);
                }
                if ((submissions.size() - removable.size()) > 1) {
                    log.debug("{} to many submissions, take the first submission and remove the rest", submissions.size() - removable.size());
                    // if we get here it's likely there is no easy decision, so lets just take the first created submission
                    submissions.removeAll(removable);
                    submissions.sort(Comparator.comparing(AssignmentSubmission::getDateCreated));
                    removable.addAll(submissions.subList(1, submissions.size()));
                }
                if (submissions.size() > 1 && (submissions.size() - removable.size()) == 0) {
                    // if we have to many submissions for removal select the first
                    submissions.sort(Comparator.comparing(AssignmentSubmission::getDateCreated));
                    removable.remove(submissions.get(0));
                }
                submissions.removeAll(removable);
                removable.forEach(s -> deleteSubmission(s.getId()));
                return submissions.get(0);
        }
    }

    @Override
    @Transactional
    public List<AssignmentSubmission> findSubmissionForUsers(String assignmentId, List<String> userIds) {
        List<AssignmentSubmission> submissions = sessionFactory.getCurrentSession().createCriteria(AssignmentSubmission.class)
                .add(Restrictions.eq("assignment.id", assignmentId))
                .createAlias("submitters", "s")
                .add(HibernateCriterionUtils.CriterionInRestrictionSplitter("s.submitter", userIds))
                .list();
        return submissions;
    }

    @Override
    public AssignmentSubmission findSubmissionForGroup(String assignmentId, String groupId) {
        return (AssignmentSubmission) sessionFactory.getCurrentSession().createCriteria(AssignmentSubmission.class)
                .add(Restrictions.eq("assignment.id", assignmentId))
                .add(Restrictions.eq("groupId", groupId))
                .uniqueResult();
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

    @Override
    public String findAssignmentIdForGradebookLink(String context, String linkId) {
        return String.valueOf(startCriteriaQuery()
                .createAlias("properties", "p")
                .add(Restrictions.eq("context", context))
                .add(Restrictions.eq("p." + CollectionPropertyNames.COLLECTION_INDICES, AssignmentConstants.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT))
                .add(Restrictions.eq("p." + CollectionPropertyNames.COLLECTION_ELEMENTS, linkId))
                .setProjection(Projections.property("id"))
                .uniqueResult());
    }

    @Override
    public Collection<String> findGroupsForAssignmentById(String assignmentId) {
        CriteriaBuilder builder = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = builder.createTupleQuery();
        Root<Assignment> root = query.from(Assignment.class);
        ParameterExpression<String> paramAssignmentId = builder.parameter(String.class);
        query.where(builder.equal(root.get("id"), paramAssignmentId));
        query.select(builder.tuple(root.join("groups")));
        List<Tuple> result = sessionFactory.getCurrentSession()
                .createQuery(query)
                .setParameter(paramAssignmentId, assignmentId)
                .getResultList();
        return result.stream().map(tuple -> (String) tuple.get(0)).collect(Collectors.toList());
    }
}
