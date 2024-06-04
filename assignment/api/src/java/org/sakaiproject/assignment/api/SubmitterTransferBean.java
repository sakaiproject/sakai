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
package org.sakaiproject.assignment.api;

import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;

import org.springframework.beans.BeanUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.springframework.beans.BeanUtils;

/**
 * Defines a relation between a submission and the submission's submitters.
 * <br/> - A submitter can have its own grade separate from the grade of the submission,
 * useful in providing user with different grades in group submissions.
 * <br/> - A submitter can have its own feedback separate from the feedback of the submission,
 * useful when different feedback is needed in group submissions
 * <p>
 * <b>Constraints</b>
 * <br/>- submission and submitter are unique,
 * meaning a user can't be a submitter more than once on a submission.
 * Notice that equals and hashcode also reflect this relationship.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SubmitterTransferBean {

    @EqualsAndHashCode.Include
    private Long id;

    private String submitter;
    private Boolean submittee = Boolean.FALSE;
    private String grade;
    private String feedback;
    private String timeSpent;
    private String submissionId;

    public SubmitterTransferBean(AssignmentSubmissionSubmitter submitter) {

        BeanUtils.copyProperties(submitter, this);

        this.submissionId = submitter.getSubmission().getId();
    }
}
