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
package org.sakaiproject.assignment.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.assignment.api.AssignmentPeerAssessmentService;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentServiceConstants;
import org.sakaiproject.assignment.api.model.AssessorSubmissionId;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.assignment.api.model.PeerAssessmentAttachment;
import org.sakaiproject.assignment.api.model.PeerAssessmentItem;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AssignmentPeerAssessmentServiceImpl extends HibernateDaoSupport implements AssignmentPeerAssessmentService {

    private ScheduledInvocationManager scheduledInvocationManager;
    protected AssignmentService assignmentService;
    private SecurityService securityService = null;
    private SessionManager sessionManager;

    public void schedulePeerReview(String assignmentId) {
        //first remove any previously scheduled reviews:
        removeScheduledPeerReview(assignmentId);
        //now schedule a time for the review to be setup
        Assignment assignment;
        try {
            assignment = assignmentService.getAssignment(assignmentId);
            if (!assignment.getDraft() && assignment.getAllowPeerAssessment()) {
                Instant assignmentCloseTime = assignment.getCloseDate();
                Instant openTime = null;
                if (assignmentCloseTime != null) {
                    openTime = assignmentCloseTime;
                }
                // Schedule the new notification
                if (openTime != null) {
                    scheduledInvocationManager.createDelayedInvocation(openTime, "org.sakaiproject.assignment.api.AssignmentPeerAssessmentService", assignmentId);
                }
            }
        } catch (Exception e) {
            log.error("Error scheduling peer review", e);
        }
    }

    public void removeScheduledPeerReview(String assignmentId) {
        // Remove any existing notifications for this area
        scheduledInvocationManager.deleteDelayedInvocation("org.sakaiproject.assignment.api.AssignmentPeerAssessmentService", assignmentId);
    }

    /**
     * Method called by the scheduledInvocationManager
     */
    public void execute(String opaqueContext) {
        try {
            //for group assignments, we need to have a user ID, otherwise, an exception is thrown:
            sessionManager.getCurrentSession().setUserEid("admin");
            sessionManager.getCurrentSession().setUserId("admin");
            Assignment assignment = assignmentService.getAssignment(opaqueContext);
            if (assignment.getAllowPeerAssessment() && !assignment.getDraft()) {
                int numOfReviews = assignment.getPeerAssessmentNumberReviews();
                Set<AssignmentSubmission> submissions = assignmentService.getSubmissions(assignment);
                //keep a map of submission ids to look up possible existing peer assessments
                Map<String, AssignmentSubmission> submissionIdMap = new HashMap<String, AssignmentSubmission>();
                //keep track of who has been assigned an assessment
                Map<String, Map<String, PeerAssessmentItem>> assignedAssessmentsMap = new HashMap<String, Map<String, PeerAssessmentItem>>();
                //keep track of how many assessor's each student has
                Map<String, Integer> studentAssessorsMap = new HashMap<String, Integer>();
                List<User> submitterUsersList = assignmentService.allowAddSubmissionUsers(assignmentService.createAssignmentEntity(assignment.getId()).getReference());
                List<String> submitterIdsList = new ArrayList<String>();
                if (submitterUsersList != null) {
                    for (User u : submitterUsersList) {
                        submitterIdsList.add(u.getId());
                    }
                }
                //loop through the assignment submissions and setup the maps and lists
                for (AssignmentSubmission s : submissions) {
                    Optional<AssignmentSubmissionSubmitter> ass = assignmentService.getSubmissionSubmittee(s);
                    List<String> submitteeIds = s.getSubmitters().stream().map(AssignmentSubmissionSubmitter::getSubmitter).collect(Collectors.toList());
                    //check if the submission is submitted, if not, see if there is any submission data to review (i.e. draft was auto submitted)
                    if (s.getDateSubmitted() != null && (s.getSubmitted() || (StringUtils.isNotBlank(s.getSubmittedText() ) || (CollectionUtils.isNotEmpty(s.getAttachments() ))))
                            && CollectionUtils.containsAny(submitterIdsList,submitteeIds)
                            && !s.getSubmitters().contains("admin")) {
                        //only deal with users in the submitter's list
                        submissionIdMap.put(s.getId(), s);
                        if (ass.isPresent()) {
                            assignedAssessmentsMap.put(ass.get().getSubmitter(), new HashMap<>());
                            studentAssessorsMap.put(ass.get().getSubmitter(), 0);
                        }
                    }
                }
                //this could be an update to an existing assessment... just make sure to grab any existing review items first
                List<PeerAssessmentItem> existingItems = getPeerAssessmentItems(submissionIdMap.keySet(), assignment.getScaleFactor());
                List<PeerAssessmentItem> removeItems = new ArrayList<PeerAssessmentItem>();
                //remove all empty items to start from scratch:
                for (Iterator iterator = existingItems.iterator(); iterator.hasNext(); ) {
                    PeerAssessmentItem peerAssessmentItem = (PeerAssessmentItem) iterator.next();
                    if (peerAssessmentItem.getScore() == null && (peerAssessmentItem.getComment() == null || "".equals(peerAssessmentItem.getComment().trim()))) {
                        removeItems.add(peerAssessmentItem);
                        iterator.remove();
                    }
                }
                if (removeItems.size() > 0) {
                    getHibernateTemplate().deleteAll(removeItems);
                    getHibernateTemplate().flush();
                }
                //loop through the items and update the map values:
                for (PeerAssessmentItem p : existingItems) {
                    if (submissionIdMap.containsKey(p.getId().getSubmissionId())) {
                        //first, add this assessment to the AssignedAssessmentsMap
                        AssignmentSubmission s = submissionIdMap.get(p.getId().getSubmissionId());
                        Optional<AssignmentSubmissionSubmitter> ass = assignmentService.getSubmissionSubmittee(s);//Next, increment the count for studentAssessorsMap
                        Integer count = ass.isPresent() ?studentAssessorsMap.get(ass.get().getSubmitter()) : 0;

                        //check if the count is less than num of reviews before added another one,
                        //otherwise, we need to delete this one (if it's empty)
                        if (count < numOfReviews || p.getScore() != null || p.getComment() != null) {
                            count++;
                            studentAssessorsMap.put(ass.get().getSubmitter(), count);
                            Map<String, PeerAssessmentItem> peerAssessments = assignedAssessmentsMap.get(p.getId().getAssessorUserId());
                            if (peerAssessments == null) {
                                //probably not possible, but just check
                                peerAssessments = new HashMap<String, PeerAssessmentItem>();
                            }
                            peerAssessments.put(p.getId().getSubmissionId(), p);
                            assignedAssessmentsMap.put(p.getId().getAssessorUserId(), peerAssessments);
                        } else {
                            //this shoudln't happen since the code above removes all empty assessments, but just in case:
                            getHibernateTemplate().delete(p);
                        }
                    } else {
                        //this isn't realy possible since we looked up the peer assessments by submission id
                        log.error("AssignmentPeerAssessmentServiceImpl: found a peer assessment with an invalid session id: " + p.getId().getSubmissionId());
                    }
                }

                //ok now that we have any existing assigned reviews accounted for, let's make sure that the number of reviews is setup properly,
                //if not, add some
                //let's get a random order of submission IDs so we can have a random assigning algorithm
                List<String> randomSubmissionIds = new ArrayList<String>(submissionIdMap.keySet());
                Collections.shuffle(randomSubmissionIds);
                List<PeerAssessmentItem> newItems = new ArrayList<PeerAssessmentItem>();
                int i = 0;
                for (String submissionId : randomSubmissionIds) {
                    AssignmentSubmission s = submissionIdMap.get(submissionId);
                    Optional<AssignmentSubmissionSubmitter> ass = assignmentService.getSubmissionSubmittee(s);//first find out how many existing items exist for this user:
                    Integer assignedCount = ass.isPresent() ? studentAssessorsMap.get(ass.get().getSubmitter()) : 0;
                    //by creating a tailing list (snake style), we eliminate the issue where you can be stuck with
                    //a submission and the same submission user left, making for uneven distributions of submission reviews
                    List<String> snakeSubmissionList = new ArrayList<String>(randomSubmissionIds.subList(i, randomSubmissionIds.size()));
                    if (i > 0) {
                        snakeSubmissionList.addAll(new ArrayList<String>(randomSubmissionIds.subList(0, i)));
                    }
                    while (assignedCount < numOfReviews) {
                        //we need to add more reviewers for this user's submission
                        String lowestAssignedAssessor = findLowestAssignedAssessor(assignedAssessmentsMap, ass.get().getSubmitter(), submissionId, snakeSubmissionList, submissionIdMap);
                        if (lowestAssignedAssessor != null) {
                            Map<String, PeerAssessmentItem> assessorsAssessmentMap = assignedAssessmentsMap.get(lowestAssignedAssessor);
                            if (assessorsAssessmentMap == null) {
                                assessorsAssessmentMap = new HashMap<String, PeerAssessmentItem>();
                            }
                            PeerAssessmentItem newItem = new PeerAssessmentItem();
                            newItem.setId(new AssessorSubmissionId(submissionId, lowestAssignedAssessor));
                            newItem.setAssignmentId(assignment.getId());
                            newItems.add(newItem);
                            assessorsAssessmentMap.put(submissionId, newItem);
                            assignedAssessmentsMap.put(lowestAssignedAssessor, assessorsAssessmentMap);
                            //update this submission user's count:
                            assignedCount++;
                            studentAssessorsMap.put(submissionId, assignedCount);
                        } else {
                            break;
                        }
                    }
                    i++;
                }
                if (newItems.size() > 0) {
                    for (PeerAssessmentItem item : newItems) {
                        getHibernateTemplate().saveOrUpdate(item);
                    }
                }
            }
        } catch (IdUnusedException | PermissionException e) {
            log.error(e.getMessage(), e);
        } finally {
            sessionManager.getCurrentSession().setUserEid(null);
            sessionManager.getCurrentSession().setUserId(null);
        }
    }

    private String findLowestAssignedAssessor(Map<String, Map<String, PeerAssessmentItem>> peerAssessments, String assesseeId, String assesseeSubmissionId, List<String> snakeSubmissionList,
                                              Map<String, AssignmentSubmission> submissionIdMap) {//find the lowest count of assigned submissions
        String lowestAssignedAssessor = null;
        Integer lowestAssignedAssessorCount = null;
        for (String sId : snakeSubmissionList) {
            AssignmentSubmission s = submissionIdMap.get(sId);
            Optional<AssignmentSubmissionSubmitter> ass = assignmentService.getSubmissionSubmittee(s);//do not include assesseeId (aka the user being assessed)
            if (!assesseeId.equals(ass.get().getSubmitter()) &&
                    (lowestAssignedAssessorCount == null || peerAssessments.get(ass.get().getSubmitter()).keySet().size() < lowestAssignedAssessorCount)) {
                //check if this user already has a peer assessment for this assessee
                boolean found = false;
                for (PeerAssessmentItem p : peerAssessments.get(ass.get().getSubmitter()).values()) {
                    if (p.getId().getSubmissionId().equals(assesseeSubmissionId)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    lowestAssignedAssessorCount = peerAssessments.get(ass.get().getSubmitter()).keySet().size();
                    lowestAssignedAssessor = ass.get().getSubmitter();
                }
            }
        }
        return lowestAssignedAssessor;
    }

    public List<PeerAssessmentItem> getPeerAssessmentItems(final Collection<String> submissionsIds, Integer scaledFactor) {
        List<PeerAssessmentItem> listPeerAssessmentItem = new ArrayList<>();
        if (submissionsIds == null || submissionsIds.size() == 0) {
            //return an empty list
            return listPeerAssessmentItem;
        }
        HibernateCallback<List<PeerAssessmentItem>> hcb = session -> {
            Query q = session.getNamedQuery("findPeerAssessmentItemsBySubmissions");
            q.setParameterList("submissionIds", submissionsIds);
            return q.list();
        };

        listPeerAssessmentItem = getHibernateTemplate().execute(hcb);

        for (PeerAssessmentItem item : listPeerAssessmentItem) {
            item.setScaledFactor(scaledFactor);
        }

        return listPeerAssessmentItem;
    }

    public List<PeerAssessmentItem> getPeerAssessmentItems(final String assignmentId, final String assessorUserId, Integer scaledFactor) {
        List<PeerAssessmentItem> listPeerAssessmentItem = new ArrayList<>();
        if (assignmentId == null || assessorUserId == null) {
            //return an empty list
            return listPeerAssessmentItem;
        }
        HibernateCallback<List<PeerAssessmentItem>> hcb = session -> {
            Query q = session.getNamedQuery("findPeerAssessmentItemsByUserAndAssignment");
            q.setParameter("assignmentId", assignmentId);
            q.setParameter("assessorUserId", assessorUserId);
            return q.list();
        };

        listPeerAssessmentItem = getHibernateTemplate().execute(hcb);

        for (PeerAssessmentItem item : listPeerAssessmentItem) {
            item.setScaledFactor(scaledFactor);
        }

        return listPeerAssessmentItem;
    }

    public List<PeerAssessmentItem> getPeerAssessmentItems(final String submissionId, Integer scaledFactor) {
        List<PeerAssessmentItem> listPeerAssessmentItem = new ArrayList<>();
        if (submissionId == null || "".equals(submissionId)) {
            //return an empty list
            return listPeerAssessmentItem;
        }
        HibernateCallback<List<PeerAssessmentItem>> hcb = session -> {
            Query q = session.getNamedQuery("findPeerAssessmentItemsBySubmissionId");
            q.setParameter("submissionId", submissionId);
            return q.list();
        };

        listPeerAssessmentItem = getHibernateTemplate().execute(hcb);

        for (PeerAssessmentItem item : listPeerAssessmentItem) {
            item.setScaledFactor(scaledFactor);
        }

        return listPeerAssessmentItem;
    }

    public List<PeerAssessmentItem> getPeerAssessmentItemsByAssignmentId(final String assignmentId, Integer scaledFactor) {
        List<PeerAssessmentItem> listPeerAssessmentItem = new ArrayList<>();
        if (assignmentId == null || "".equals(assignmentId)) {
            //return an empty list
            return listPeerAssessmentItem;
        }
        HibernateCallback<List<PeerAssessmentItem>> hcb = session -> {
            Query q = session.getNamedQuery("findPeerAssessmentItemsByAssignmentId");
            q.setParameter("assignmentId", assignmentId);
            return q.list();
        };

        listPeerAssessmentItem = getHibernateTemplate().execute(hcb);

        for (PeerAssessmentItem item : listPeerAssessmentItem) {
            item.setScaledFactor(scaledFactor);
        }

        return listPeerAssessmentItem;
    }

    public PeerAssessmentItem getPeerAssessmentItem(final String submissionId, final String assessorUserId) {
        if (submissionId == null || assessorUserId == null) {
            //return an empty list
            return null;
        }
        HibernateCallback<List<PeerAssessmentItem>> hcb = session -> {
            Query q = session.getNamedQuery("findPeerAssessmentItemsByUserAndSubmission");
            q.setParameter("submissionId", submissionId);
            q.setParameter("assessorUserId", assessorUserId);
            return q.list();
        };

        List<PeerAssessmentItem> results = getHibernateTemplate().execute(hcb);
        if (results != null && results.size() == 1) {
            return results.get(0);
        } else {
            return null;
        }
    }

    public List<PeerAssessmentAttachment> getPeerAssessmentAttachments(final String submissionId, final String assessorUserId) {
        if (submissionId == null || "".equals(submissionId) || assessorUserId == null || "".equals(assessorUserId)) {
            //return an empty list
            return new ArrayList<>();
        }
        HibernateCallback<List<PeerAssessmentAttachment>> hcb = session -> {
            Query q = session.getNamedQuery("findPeerAssessmentAttachmentsByUserAndSubmission");
            q.setParameter("submissionId", submissionId);
            q.setParameter("assessorUserId", assessorUserId);
            return q.list();
        };

        return getHibernateTemplate().execute(hcb);
    }

    public PeerAssessmentAttachment getPeerAssessmentAttachment(final String submissionId, final String assessorUserId, final String resourceId) {
        DetachedCriteria d = DetachedCriteria.forClass(PeerAssessmentAttachment.class)
                .add(Restrictions.eq("submissionId", submissionId))
                .add(Restrictions.eq("assessorUserId", assessorUserId))
                .add(Restrictions.eq("resourceId", resourceId));
        List attachments = getHibernateTemplate().findByCriteria(d);
        if (attachments == null || attachments.isEmpty()) {
            return null;
        } else {
            return (PeerAssessmentAttachment) attachments.get(0);
        }
    }

    public void savePeerAssessmentItem(PeerAssessmentItem item) {
        if (item != null && item.getId().getAssessorUserId() != null && item.getId().getSubmissionId() != null) {
            getHibernateTemplate().saveOrUpdate(item);
            getHibernateTemplate().flush();
        }
    }

    public void savePeerAssessmentAttachments(PeerAssessmentItem item) {
        if (item != null && item.getAttachmentList() != null) {
            for (PeerAssessmentAttachment element : item.getAttachmentList()) {
                getHibernateTemplate().saveOrUpdate(element);
            }
            getHibernateTemplate().flush();
        }
    }

    public void removePeerAttachment(PeerAssessmentAttachment peerAssessmentAttachment) {
        getHibernateTemplate().delete(peerAssessmentAttachment);
        getHibernateTemplate().flush();
    }

    public boolean updateScore(String submissionId, String assessorId) {
        boolean saved = false;
        SecurityAdvisor sa = new SecurityAdvisor() {
            public SecurityAdvice isAllowed(String userId, String function, String reference) {
                if (AssignmentServiceConstants.SECURE_GRADE_ASSIGNMENT_SUBMISSION.equals(function)
                        || AssignmentServiceConstants.SECURE_UPDATE_ASSIGNMENT.equals(function)
                        || AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT.equals(function)
                        || AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT_SUBMISSION.equals(function)
                        || AssignmentServiceConstants.GRADEBOOK_PERMISSION_GRADE_ALL.equals(function)
                        || AssignmentServiceConstants.GRADEBOOK_PERMISSION_EDIT_ASSIGNMENTS.equals(function)) {
                    return SecurityAdvice.ALLOWED;
                } else {
                    return SecurityAdvice.PASS;
                }
            }
        };
        try {
            securityService.pushAdvisor(sa);
            //first check that submission exists and that it can be graded/override score
            AssignmentSubmission submission = assignmentService.getSubmission(submissionId);
            //only override grades that have never been graded or was last graded by this service
            //this prevents this service from overriding instructor set grades, which take precedent.
            if (submission != null && (!submission.getGraded() || StringUtils.isBlank(submission.getGradedBy()))) {
                List<PeerAssessmentItem> items = getPeerAssessmentItems(submissionId, submission.getAssignment().getScaleFactor());
                if (items != null) {
                    //scores are stored w/o decimal points, so a score of 3.4 is stored as 34 in the DB
                    //add all the scores together and divide it by the number of scores added.  Then round.
                    Integer totalScore = 0;
                    int denominator = 0;
                    for (PeerAssessmentItem item : items) {
                        if (!item.getRemoved() && item.getScore() != null) {
                            totalScore += item.getScore();
                            denominator++;
                        }
                    }
                    if (denominator > 0) {
                        totalScore = Math.round(totalScore.floatValue() / denominator);
                    } else {
                        totalScore = null;
                    }
                    String totleScoreStr = null;
                    if (totalScore != null) {
                        totleScoreStr = totalScore.toString();
                    }
                    boolean changed = false;
                    if ((totleScoreStr == null || "".equals(totleScoreStr)) && (submission.getGrade() == null || "".equals(submission.getGrade()))) {
                        //scores are both null, nothing changed
                    } else if ((totleScoreStr != null && !"".equals(totleScoreStr)) && (submission.getGrade() == null || "".equals(submission.getGrade()))) {
                        //one score changed, update
                        changed = true;
                    } else if ((totleScoreStr == null || "".equals(totleScoreStr)) && (submission.getGrade() != null && !"".equals(submission.getGrade()))) {
                        //one score changed, update
                        changed = true;
                    } else if (!totleScoreStr.equals(submission.getGrade())) {
                        changed = true;
                    }
                    if (changed) {
                        // don't set gradedBy since grading is done by peers
                        submission.setGrade(totleScoreStr);
                        submission.setGraded(true);
                        submission.setGradeReleased(false);
                        assignmentService.updateSubmission(submission);
                        saved = true;
                    }
                }
            }
        } catch (IdUnusedException |  PermissionException e) {
            log.error(e.getMessage(), e);
        } finally {
            // remove advisor
            securityService.popAdvisor(sa);
        }
        return saved;
    }

    public void setScheduledInvocationManager(
            ScheduledInvocationManager scheduledInvocationManager) {
        this.scheduledInvocationManager = scheduledInvocationManager;
    }

    public void setAssignmentService(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

}
