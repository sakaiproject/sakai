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
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.persister.collection.CollectionPropertyNames;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentAllPurposeItem;
import org.sakaiproject.assignment.api.model.AssignmentModelAnswerItem;
import org.sakaiproject.assignment.api.model.AssignmentNoteItem;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.assignment.api.model.PeerAssessmentAttachment;
import org.sakaiproject.assignment.api.model.PeerAssessmentItem;
import org.sakaiproject.assignment.api.persistence.AssignmentRepository;
import org.sakaiproject.assignment.api.persistence.AssignmentRepository.SimpleAssignmentAutoSubmit;
import org.sakaiproject.assignment.api.persistence.AssignmentRepository.SimpleSubmissionDraft;
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
        List<AssignmentSubmission> submissions = geCurrentSession().createCriteria(AssignmentSubmission.class)
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
        List<AssignmentSubmission> submissions = geCurrentSession().createCriteria(AssignmentSubmission.class)
                .add(Restrictions.eq("assignment.id", assignmentId))
                .createAlias("submitters", "s")
                .add(HibernateCriterionUtils.CriterionInRestrictionSplitter("s.submitter", userIds))
                .list();
        return submissions;
    }

    @Override
    public AssignmentSubmission findSubmissionForGroup(String assignmentId, String groupId) {
        return (AssignmentSubmission) geCurrentSession().createCriteria(AssignmentSubmission.class)
                .add(Restrictions.eq("assignment.id", assignmentId))
                .add(Restrictions.eq("groupId", groupId))
                .uniqueResult();
    }

    @Override
    public long countAssignmentsBySite(String siteId) {
        Criteria criteria = geCurrentSession().createCriteria(Assignment.class)
                .setProjection(Projections.countDistinct("id"))
                .add(Restrictions.eq("context", siteId));

        return ((Number) criteria.uniqueResult()).longValue();
    }

    @Override
    public long countAssignmentSubmissions(String assignmentId, Boolean graded, Boolean hasSubmissionDate, Boolean userSubmission, List<String> userIds) {
        Criteria criteria = geCurrentSession().createCriteria(AssignmentSubmission.class)
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
    public List<Assignment> findAssignmentsForGradebookLink(String context, String linkId) {
        List<Assignment> result = startCriteriaQuery()
                .createAlias("properties", "p")
                .add(Restrictions.eq("context", context))
                .add(Restrictions.eq("p." + CollectionPropertyNames.COLLECTION_INDICES, AssignmentConstants.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT))
                .add(Restrictions.eq("p." + CollectionPropertyNames.COLLECTION_ELEMENTS, linkId))
                .list();
        return result;
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
            AssignmentAllPurposeItem apItem = (AssignmentAllPurposeItem) sessionFactory.getCurrentSession().createCriteria(AssignmentAllPurposeItem.class).add(Restrictions.eq("assignmentId", assignmentId)).uniqueResult();
            if (apItem != null){
                log.info("delete AssignmentAllPurposeItem for assignment: {}", assignmentId);
                sessionFactory.getCurrentSession().delete(apItem);
            }


            // only one per assignment
            AssignmentModelAnswerItem maItem = (AssignmentModelAnswerItem) sessionFactory.getCurrentSession().createCriteria(AssignmentModelAnswerItem.class).add(Restrictions.eq("assignmentId", assignmentId)).uniqueResult();
            if(maItem != null){
                log.info("delete AssignmentModelAnswerItem for assignment: {}", assignmentId);
                sessionFactory.getCurrentSession().delete(maItem);
            }


            // only one per assignment
            AssignmentNoteItem noteItem = (AssignmentNoteItem) sessionFactory.getCurrentSession().createCriteria(AssignmentNoteItem.class).add(Restrictions.eq("assignmentId", assignmentId)).uniqueResult();
            if (noteItem != null) {
                log.info("delete AssignmentNoteItem for assignment: {}", assignmentId);
                sessionFactory.getCurrentSession().delete(noteItem);
            }

            // multiple possible per assignment
            List<PeerAssessmentItem> peerAssessmentItems = (List<PeerAssessmentItem>) sessionFactory.getCurrentSession().createCriteria(PeerAssessmentItem.class).add(Restrictions.eq("assignmentId", assignmentId)).list();
            if (!peerAssessmentItems.isEmpty()){
                for(PeerAssessmentItem item : peerAssessmentItems){
                    //get submissionId and assessor_user_id for deletion of PeerAssessmentAttachment
                    String submissionId = item.getId().getSubmissionId();
                    String assessorUserId = item.getId().getAssessorUserId();
                    sessionFactory.getCurrentSession().delete(item);
                    List<PeerAssessmentAttachment> peerAssessmentItemAttach = (List) sessionFactory.getCurrentSession().createCriteria(PeerAssessmentAttachment.class).add(Restrictions.eq("submissionId", submissionId)).add(Restrictions.eq("assessorUserId", assessorUserId)).list();
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

    @Override
    public List<SimpleAssignmentAutoSubmit> findAutoSubmitAssignmentsBySite(String siteId, java.time.Instant now) {
        List<Assignment> assignments = startCriteriaQuery()
                .add(Restrictions.eq("context", siteId))
                .add(Restrictions.eq("deleted", Boolean.FALSE))
                .add(Restrictions.eq("draft", Boolean.FALSE))
                .add(Restrictions.le("closeDate", now))
                .list();
        return assignments.stream()
                .filter(a -> {
                    String autoSubmit = a.getProperties().get(AssignmentConstants.ASSIGNMENT_AUTO_SUBMIT_ENABLED);
                    return "true".equals(autoSubmit);
                })
                .map(a -> {
                    SimpleAssignmentAutoSubmit dto = new SimpleAssignmentAutoSubmit();
                    dto.id = a.getId();
                    dto.title = a.getTitle();
                    dto.dueTime = a.getDueDate();
                    dto.closeTime = a.getCloseDate();
                    dto.context = a.getContext();
                    dto.draft = a.getDraft();
                    dto.isGroup = Boolean.TRUE.equals(a.getIsGroup());
                    dto.properties = a.getProperties();
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<SimpleSubmissionDraft> findDraftSubmissionsForAssignment(String assignmentId) {
        List<AssignmentSubmission> submissions = geCurrentSession().createCriteria(AssignmentSubmission.class)
                .add(Restrictions.eq("assignment.id", assignmentId))
                .list();
        return submissions.stream()
                .filter(s -> !Boolean.TRUE.equals(s.getSubmitted()) &&
                        ((s.getSubmittedText() != null && !s.getSubmittedText().isEmpty()) ||
                         (s.getAttachments() != null && !s.getAttachments().isEmpty())))
                .map(s -> {
                    SimpleSubmissionDraft dto = new SimpleSubmissionDraft();
                    dto.id = s.getId();
                    dto.gradableId = s.getAssignment().getId();
                    dto.submitted = Boolean.TRUE.equals(s.getSubmitted());
                    dto.draft = !Boolean.TRUE.equals(s.getSubmitted());
                    dto.submittedText = s.getSubmittedText();
                    dto.attachments = s.getAttachments();
                    dto.properties = s.getProperties();
                    dto.submitterIds = s.getSubmitters() != null ?
                        s.getSubmitters().stream().map(sub -> sub.getSubmitter()).collect(Collectors.toSet()) : null;
                    dto.dateModified = s.getDateModified();
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SimpleSubmissionDraft> findAllEligibleDraftSubmissions() {
        // OPTIMIZED QUERY: Get all draft submissions from assignments with auto-submit enabled and past close date
        java.time.Instant now = java.time.Instant.now();
        
        List<AssignmentSubmission> submissions = geCurrentSession().createCriteria(AssignmentSubmission.class)
                .createAlias("assignment", "a")
                .add(Restrictions.eq("submitted", Boolean.FALSE)) // Draft submissions only  
                .add(Restrictions.eq("a.draft", Boolean.FALSE)) // Published assignments only
                .add(Restrictions.eq("a.deleted", Boolean.FALSE)) // Non-deleted assignments only
                .add(Restrictions.le("a.closeDate", now)) // Past close date
                .list();
                
        return submissions.stream()
                .filter(s -> {
                    // Filter for assignments with auto-submit enabled
                    String autoSubmit = s.getAssignment().getProperties().get(AssignmentConstants.ASSIGNMENT_AUTO_SUBMIT_ENABLED);
                    if (!"true".equals(autoSubmit)) {
                        return false;
                    }
                    
                    // Filter for submissions with content (text or attachments)
                    boolean hasText = s.getSubmittedText() != null && !s.getSubmittedText().trim().isEmpty();
                    boolean hasAttachments = s.getAttachments() != null && !s.getAttachments().isEmpty();
                    return hasText || hasAttachments;
                })
                .map(s -> {
                    SimpleSubmissionDraft dto = new SimpleSubmissionDraft();
                    dto.id = s.getId();
                    dto.gradableId = s.getAssignment().getId();
                    dto.submitted = Boolean.TRUE.equals(s.getSubmitted());
                    dto.draft = !Boolean.TRUE.equals(s.getSubmitted());
                    dto.submittedText = s.getSubmittedText();
                    dto.attachments = s.getAttachments();
                    dto.properties = s.getProperties();
                    dto.submitterIds = s.getSubmitters() != null ?
                        s.getSubmitters().stream().map(sub -> sub.getSubmitter()).collect(Collectors.toSet()) : null;
                    dto.dateModified = s.getDateModified();
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
