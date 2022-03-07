/******************************************************************************
 * Copyright 2022 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.beans;

import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignmentSubmissionRestBean {

    private String id;
    private String user;
    private String title;
    private String grade;
    private String status;
    private String statusText;
    private boolean released;
    private boolean hasAttachment;
    private Long submittedDate;

    public AssignmentSubmissionRestBean(AssignmentSubmission assignmentSubmission) {
        Assignment assignment = assignmentSubmission.getAssignment();
        id = assignmentSubmission.getId();
        title = assignment.getTitle();
        released = assignmentSubmission.getGradeReleased();
        hasAttachment = assignmentSubmission.getAttachments().size() > 0;
        submittedDate = assignmentSubmission.getUserSubmission() ? assignmentSubmission.getDateSubmitted().toEpochMilli() : null;
    }
}
