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
import java.util.stream.Collectors;

import org.sakaiproject.assignment.api.model.AssignmentSubmission;

import org.springframework.beans.BeanUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * AssignmentSubmission represents a student submission for an assignment.
 */

@Data
@NoArgsConstructor
@ToString(exclude = {"submitters", "attachments", "feedbackAttachments", "properties"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SubmissionTransferBean {

    @EqualsAndHashCode.Include
    private String id;

    private Set<SubmitterTransferBean> submitters = new HashSet<>();

    private Instant dateSubmitted;

    private Instant dateReturned;

    private Instant dateCreated;

    private Instant dateModified;

    private Set<String> attachments = new HashSet<>();

    private Set<String> feedbackAttachments = new HashSet<>();

    private String submittedText;

    private String feedbackComment;

    private String feedbackText;

    private String grade;

    private Integer factor;

    private Boolean submitted = Boolean.FALSE;

    private Boolean returned = Boolean.FALSE;

    private Boolean graded = Boolean.FALSE;

    private String gradedBy;

    private Boolean gradeReleased = Boolean.FALSE;

    private Boolean honorPledge = Boolean.FALSE;

    private Boolean hiddenDueDate = Boolean.FALSE;

    private Boolean userSubmission = Boolean.FALSE;

    private String groupId;

    private String privateNotes;

    private Map<String, String> properties = new HashMap<>();

    private String assignmentId;

    private String context;

    private AssignmentTransferBean assignment;

    public SubmissionTransferBean(AssignmentSubmission submission) {

        this(submission, false);
    }

    public SubmissionTransferBean(AssignmentSubmission submission, boolean populate) {

        BeanUtils.copyProperties(submission, this, "attachments", "feedbackAttachments", "properties", "submitters");

        this.attachments.addAll(submission.getAttachments());
        this.feedbackAttachments.addAll(submission.getFeedbackAttachments());
        this.properties.putAll(submission.getProperties());
        this.submitters = submission.getSubmitters().stream().map(SubmitterTransferBean::new).collect(Collectors.toSet());

        if (populate) {
            // This is under a flag because it incurs a db read to get the assignment.
            this.assignmentId = submission.getAssignment().getId();
            this.context = submission.getAssignment().getContext();
        }
    }
}
