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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.MapJoin;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.ParameterExpression;
import jakarta.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.model.*;
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

    public Session geCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public Assignment findAssignment(String id) {
        return findOne(id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Assignment> findAssignmentsBySite(String siteId) {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Assignment> cq = cb.createQuery(Assignment.class);
        Root<Assignment> root = cq.from(Assignment.class);
        cq.where(
            cb.equal(root.get("context"), siteId),
            cb.equal(root.get("deleted"), Boolean.FALSE)
        );
        return geCurrentSession().createQuery(cq).list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Assignment> findDeletedAssignmentsBySite(String siteId) {
    	CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Assignment> cq = cb.createQuery(Assignment.class);
        Root<Assignment> root = cq.from(Assignment.class);
        cq.where(
            cb.equal(root.get("context"), siteId),
            cb.equal(root.get("deleted"), Boolean.TRUE)
        );
        return geCurrentSession().createQuery(cq).list();
    }

    @Override
    public List<String> findAllAssignmentIds() {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<Assignment> root = cq.from(Assignment.class);
        cq.select(root.get("id"));
        return geCurrentSession().createQuery(cq).list();
    }

    @Override
    @Transactional
    public void newAssignment(Assignment assignment) {
        if (!existsAssignment(assignment.getId())) {
            assignment.setDateCreated(Instant.now());
            geCurrentSession().persist(assignment);
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
            hardDeleteHelper(assignmentId);
        }
    }

    @Override
    @Transactional
    public void deleteSubmission(String submissionId) {
        AssignmentSubmission submission = geCurrentSession().get(AssignmentSubmission.class, submissionId);
        if (submission != null) {
            log.info("Deleting submission {}", submission);
            Assignment assignment = submission.getAssignment();
            assignment.getSubmissions().remove(submission);
            geCurrentSession().delete(submission);
        }
    }

    @Override
    @Transactional
    public void softDeleteAssignment(String assignmentId) {
        Assignment assignment = findOne(assignmentId);
        assignment.setDeleted(Boolean.TRUE);
        assignment.setSoftRemovedDate(Instant.now());
        update(assignment);
    }

    @Override
    public AssignmentSubmission findSubmission(String submissionId) {
        return geCurrentSession().get(AssignmentSubmission.class, submissionId);
    }

    @Override
    @Transactional
    public void updateSubmission(AssignmentSubmission submission) {
        if (existsSubmission(submission.getId())) {
            submission.setDateModified(Instant.now());
            geCurrentSession().merge(submission);
        }
    }

    @Override
    @Transactional
    public boolean existsSubmission(String submissionId) {
        if (submissionId != null && geCurrentSession().get(AssignmentSubmission.class, submissionId) != null) {
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean existsSubmissionSubmitter(Long submissionSubmitterId) {
        if (submissionSubmitterId != null && geCurrentSession().get(AssignmentSubmissionSubmitter.class, submissionSubmitterId) != null) {
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public AssignmentSubmission newSubmission(String assignmentId, Optional<String> groupId, Optional<Set<AssignmentSubmissionSubmitter>> submitters, Optional<Set<String>> feedbackAttachments, Optional<Set<String>> submittedAttachments, Optional<Map<String, String>> properties) {
        Assignment assignment = findAssignment(assignmentId);
        if (assignment != null) {
            // Since this transaction is going to add a submission to the assignment we lock the assignment
            // the lock is freed once transaction is committed or rolled back
            geCurrentSession().buildLockRequest(LockOptions.UPGRADE).setLockMode(LockMode.PESSIMISTIC_WRITE).lock(assignment);

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

            geCurrentSession().persist(assignment);
            return submission;
        }
        return null;
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public AssignmentSubmission findSubmissionForUser(String assignmentId, String userId) {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<AssignmentSubmission> cq = cb.createQuery(AssignmentSubmission.class);
        Root<AssignmentSubmission> root = cq.from(AssignmentSubmission.class);
        Join<AssignmentSubmission, AssignmentSubmissionSubmitter> submitters = root.join("submitters");
        cq.where(
            cb.equal(root.get("assignment").get("id"), assignmentId),
            cb.equal(submitters.get("submitter"), userId)
        );
        List<AssignmentSubmission> submissions = geCurrentSession().createQuery(cq).list();

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
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<AssignmentSubmission> cq = cb.createQuery(AssignmentSubmission.class);
        Root<AssignmentSubmission> root = cq.from(AssignmentSubmission.class);
        Join<AssignmentSubmission, AssignmentSubmissionSubmitter> submitters = root.join("submitters");
        cq.where(
            cb.equal(root.get("assignment").get("id"), assignmentId),
            submitters.get("submitter").in(userIds)
        );
        return geCurrentSession().createQuery(cq).list();
    }

    @Override
    public AssignmentSubmission findSubmissionForGroup(String assignmentId, String groupId) {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<AssignmentSubmission> cq = cb.createQuery(AssignmentSubmission.class);
        Root<AssignmentSubmission> root = cq.from(AssignmentSubmission.class);
        cq.where(
            cb.equal(root.get("assignment").get("id"), assignmentId),
            cb.equal(root.get("groupId"), groupId)
        );
        return geCurrentSession().createQuery(cq).uniqueResult();
    }

    @Override
    public long countAssignmentsBySite(String siteId) {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Assignment> root = cq.from(Assignment.class);
        cq.select(cb.countDistinct(root.get("id")));
        cq.where(cb.equal(root.get("context"), siteId));
        return geCurrentSession().createQuery(cq).uniqueResult();
    }

    @Override
    public long countAssignmentSubmissions(String assignmentId, Boolean graded, Boolean hasSubmissionDate, Boolean userSubmission, List<String> userIds) {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<AssignmentSubmission> root = cq.from(AssignmentSubmission.class);
        Join<AssignmentSubmission, AssignmentSubmissionSubmitter> submitters = root.join("submitters");

        cq.select(cb.countDistinct(root.get("id")));

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("assignment").get("id"), assignmentId));
        predicates.add(cb.equal(root.get("submitted"), Boolean.TRUE));

        if (graded != null) {
            predicates.add(cb.equal(root.get("graded"), graded));
        }
        if (hasSubmissionDate != null) {
            predicates.add(hasSubmissionDate ? cb.isNotNull(root.get("dateSubmitted")) : cb.isNull(root.get("dateSubmitted")));
        }
        if (userSubmission != null) {
            predicates.add(cb.equal(root.get("userSubmission"), userSubmission));
        }
        if (userIds != null) {
            if (userIds.isEmpty()) {
                return 0; // if we have an empty list then we return always return 0
            } else {
                predicates.add(submitters.get("submitter").in(userIds));
            }
        }

        cq.where(predicates.toArray(new Predicate[0]));
        return geCurrentSession().createQuery(cq).uniqueResult();
    }

    @Override
    public void resetAssignment(Assignment assignment) {
        if (assignment != null && assignment.getId() != null) {
            sessionFactory.getCache().evictEntityData(Assignment.class, assignment.getId());
        }
    }

    @Override
    public List<Assignment> findAssignmentsForGradebookLink(String context, String linkId) {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Assignment> cq = cb.createQuery(Assignment.class);
        Root<Assignment> root = cq.from(Assignment.class);

        MapJoin<Assignment, String, String> properties = root.joinMap("properties");

        cq.select(root)
            .distinct(true)
            .where(
                cb.and(
                    cb.equal(root.get("context"), context),
                    cb.equal(properties.key(),
                        AssignmentConstants.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT),
                    cb.equal(properties.value(), linkId)
                )
            );

        return geCurrentSession().createQuery(cq).getResultList();
    }

    @Override
    public Collection<String> findGroupsForAssignmentById(String assignmentId) {
        CriteriaBuilder builder = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = builder.createTupleQuery();
        Root<Assignment> root = query.from(Assignment.class);
        ParameterExpression<String> paramAssignmentId = builder.parameter(String.class);
        query.where(builder.equal(root.get("id"), paramAssignmentId));
        query.select(builder.tuple(root.join("groups")));
        List<Tuple> result = geCurrentSession()
                .createQuery(query)
                .setParameter(paramAssignmentId, assignmentId)
                .getResultList();
        return result.stream().map(tuple -> (String) tuple.get(0)).collect(Collectors.toList());
    }

    private void hardDeleteHelper(String assignmentId){


        try{
            // only one per assignment
            CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<AssignmentAllPurposeItem> cq = cb.createQuery(AssignmentAllPurposeItem.class);
            Root<AssignmentAllPurposeItem> root = cq.from(AssignmentAllPurposeItem.class);

            cq.select(root)
           .where(cb.equal(root.get("assignmentId"), assignmentId));

            AssignmentAllPurposeItem apItem = sessionFactory.getCurrentSession().createQuery(cq).uniqueResult();

            if (apItem != null){
                log.info("delete AssignmentAllPurposeItem for assignment: {}", assignmentId);
                sessionFactory.getCurrentSession().delete(apItem);
            }


            // only one per assignment
            cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<AssignmentModelAnswerItem> cq2 = cb.createQuery(AssignmentModelAnswerItem.class);
            Root<AssignmentModelAnswerItem> root2 = cq2.from(AssignmentModelAnswerItem.class);
            cq2.select(root2)
            .where(cb.equal(root.get("assignmentId"), assignmentId));

            AssignmentModelAnswerItem maItem = sessionFactory.getCurrentSession().createQuery(cq2).uniqueResult();

            if(maItem != null){
                log.info("delete AssignmentModelAnswerItem for assignment: {}", assignmentId);
                sessionFactory.getCurrentSession().delete(maItem);
            }


            // only one per assignment
            cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<AssignmentNoteItem> cq3 = cb.createQuery(AssignmentNoteItem.class);
            Root<AssignmentNoteItem> root3 = cq.from(AssignmentNoteItem.class);

            cq3.select(root3)
            .where(cb.equal(root.get("assignmentId"), assignmentId));

            AssignmentNoteItem noteItem = sessionFactory.getCurrentSession().createQuery(cq3).uniqueResult();

            if (noteItem != null) {
                log.info("delete AssignmentNoteItem for assignment: {}", assignmentId);
                sessionFactory.getCurrentSession().delete(noteItem);
            }

            // multiple possible per assignment
            cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<PeerAssessmentItem> cq4 = cb.createQuery(PeerAssessmentItem.class);
            Root<PeerAssessmentItem> root4 = cq4.from(PeerAssessmentItem.class);

            cq4.select(root4)
            .where(cb.equal(root.get("assignmentId"), assignmentId));

            List<PeerAssessmentItem> peerAssessmentItems = sessionFactory.getCurrentSession().createQuery(cq4).getResultList();

            if (!peerAssessmentItems.isEmpty()){
                for(PeerAssessmentItem item : peerAssessmentItems){
                    //get submissionId and assessor_user_id for deletion of PeerAssessmentAttachment
                    String submissionId = item.getId().getSubmissionId();
                    String assessorUserId = item.getId().getAssessorUserId();
                    sessionFactory.getCurrentSession().delete(item);

                    cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
                    CriteriaQuery<PeerAssessmentAttachment> cq5 = cb.createQuery(PeerAssessmentAttachment.class);
                    Root<PeerAssessmentAttachment> root5 = cq5.from(PeerAssessmentAttachment.class);

                    cq5.select(root5)
                        .where(
                            cb.and(
                                cb.equal(root.get("submissionId"), submissionId),
                                cb.equal(root.get("assessorUserId"), assessorUserId)
                            )
                        );

                    List<PeerAssessmentAttachment> peerAssessmentItemAttach = sessionFactory.getCurrentSession().createQuery(cq5).getResultList();
                    if(peerAssessmentItemAttach.size() !=  0){
                        for(PeerAssessmentAttachment attach: peerAssessmentItemAttach)
                            sessionFactory.getCurrentSession().delete(attach);
                    }
                }
            }

        }catch (HibernateException e){
            log.error("error hardDelete of assignment: {}", assignmentId, e);
        }

    }
}
