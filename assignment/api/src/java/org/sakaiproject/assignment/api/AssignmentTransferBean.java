/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.assignment.api;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.Assignment.Access;
import org.sakaiproject.assignment.api.model.Assignment.GradeType;
import org.sakaiproject.assignment.api.model.Assignment.SubmissionType;

import org.springframework.beans.BeanUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AssignmentTransferBean {

    @EqualsAndHashCode.Include
    private String id;

    private String title;
    private String instructions;
    private String context;
    private String section;
    private Instant dateCreated;
    private Instant dateModified;
    private Instant visibleDate;
    private Instant openDate;
    private Instant dueDate;
    private Instant closeDate;
    private Instant dropDeadDate;
    private Instant softRemovedDate;
    private String modifier;
    private String author;
    private Boolean draft = Boolean.FALSE;
    private Boolean deleted = Boolean.FALSE;
    private Boolean hideDueDate = Boolean.FALSE;
    private Boolean isGroup = Boolean.FALSE;
    private Integer position;
    private Map<String, String> properties = new HashMap<>();
    private Set<String> groups = new HashSet<>();
    private Set<String> attachments = new HashSet<>();
    private Access typeOfAccess = Access.SITE;
    private Boolean honorPledge = Boolean.FALSE;
    private SubmissionType typeOfSubmission = SubmissionType.ASSIGNMENT_SUBMISSION_TYPE_NONE;
    private GradeType typeOfGrade = GradeType.GRADE_TYPE_NONE;
    private Integer maxGradePoint;
    private Integer scaleFactor;
    private Boolean individuallyGraded;
    private Boolean releaseGrades = Boolean.FALSE;
    private Boolean allowAttachments;
    private Boolean allowPeerAssessment = Boolean.FALSE;
    private Instant peerAssessmentPeriodDate;
    private Boolean peerAssessmentAnonEval;
    private Boolean peerAssessmentStudentReview = Boolean.FALSE;
    private Integer peerAssessmentNumberReviews;
    private String peerAssessmentInstructions;
    private Boolean contentReview = Boolean.FALSE;
    private Boolean estimateRequired = Boolean.FALSE;
    private String estimate;
    private Integer contentId = null;
    private Boolean contentLaunchNewWindow = Boolean.FALSE;

    // gradebookCategory, displayInGradebook and newGbItemId are transient so we can use Assignment
    // in a transfer bean way, as a carrier of parameters. At some point we need to add an
    // AssignmentTransferBean so we don't use the live hibernate object in all tiers of the code

    private Long gradebookCategory;
    private Boolean displayInGradebook = Boolean.FALSE;
    private Long newGbItemId = 0L;

    public AssignmentTransferBean(Assignment assignment) {

        BeanUtils.copyProperties(assignment, this, "attachments", "groups", "properties");

        attachments.clear();
        attachments.addAll(assignment.getAttachments());

        groups.clear();
        groups.addAll(assignment.getGroups());

        properties.clear();
        properties.putAll(assignment.getProperties());
    }

    public AssignmentTransferBean(AssignmentTransferBean assignment, boolean includeSubmissions) {

        BeanUtils.copyProperties(assignment, this, "attachments", "groups", "properties");

        attachments.clear();
        attachments.addAll(assignment.getAttachments());

        groups.clear();
        groups.addAll(assignment.getGroups());

        properties.clear();
        properties.putAll(assignment.getProperties());
    }

    public Assignment mergeInto(Assignment assignment) {

        assignment.setTitle(title);
        assignment.setInstructions(instructions);
        assignment.setSection(section);
        assignment.setDateModified(dateModified);
        assignment.setModifier(modifier);
        assignment.setVisibleDate(visibleDate);
        assignment.setOpenDate(openDate);
        assignment.setDueDate(dueDate);
        assignment.setCloseDate(closeDate);
        assignment.setDropDeadDate(dropDeadDate);
        assignment.setSoftRemovedDate(softRemovedDate);
        assignment.setDraft(draft);
        assignment.setDeleted(deleted);
        assignment.setHideDueDate(hideDueDate);
        assignment.setIsGroup(isGroup);
        assignment.setPosition(position);
        assignment.getGroups().clear();
        assignment.getGroups().addAll(groups);
        assignment.getAttachments().clear();
        assignment.getAttachments().addAll(attachments);
        assignment.getProperties().clear();
        assignment.getProperties().putAll(properties);
        assignment.setTypeOfAccess(typeOfAccess);
        assignment.setHonorPledge(honorPledge);
        assignment.setTypeOfSubmission(typeOfSubmission);
        assignment.setTypeOfGrade(typeOfGrade);
        assignment.setMaxGradePoint(maxGradePoint);
        assignment.setScaleFactor(scaleFactor);
        assignment.setIndividuallyGraded(individuallyGraded);
        assignment.setReleaseGrades(releaseGrades);
        assignment.setAllowAttachments(allowAttachments);
        assignment.setAllowPeerAssessment(allowPeerAssessment);
        assignment.setPeerAssessmentPeriodDate(peerAssessmentPeriodDate);
        assignment.setPeerAssessmentAnonEval(peerAssessmentAnonEval);
        assignment.setPeerAssessmentStudentReview(peerAssessmentStudentReview);
        assignment.setPeerAssessmentNumberReviews(peerAssessmentNumberReviews);
        assignment.setPeerAssessmentInstructions(peerAssessmentInstructions);
        assignment.setContentReview(contentReview);
        assignment.setEstimateRequired(estimateRequired);
        assignment.setEstimate(estimate);
        assignment.setContentId(contentId);
        assignment.setContentLaunchNewWindow(contentLaunchNewWindow);

        return assignment;
    }
}

