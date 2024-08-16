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
package org.sakaiproject.assignment.tool;

import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.SubmissionTransferBean;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.user.api.User;
import org.sakaiproject.site.api.Group;

import org.apache.commons.collections4.CollectionUtils;

import lombok.Data;

@Data
public class SubmitterSubmission {

    private AssignmentService assignmentService;
    private Boolean hasVisibleAttachments = false;
    private EntityManager entityManager;
    private Group group;
    private User user;
    private String reference;
    private SubmissionTransferBean submission;

    public SubmitterSubmission(AssignmentService assignmentService, EntityManager entityManager, User user, SubmissionTransferBean submission) {
        this(assignmentService, entityManager, user, null, submission);
    }

    public SubmitterSubmission(AssignmentService assignmentService, EntityManager entityManager, Group group, SubmissionTransferBean submission) {
        this(assignmentService, entityManager, null, group, submission);
    }

    private SubmitterSubmission(AssignmentService assignmentService, EntityManager entityManager, User user, Group group, SubmissionTransferBean submission) {

        this.assignmentService = assignmentService;
        this.entityManager = entityManager;
        this.user = user;
        this.group = group;
        this.submission = submission;
        reference = AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference();
        hasVisibleAttachments = CollectionUtils.emptyIfNull(submission.getAttachments()).stream()
                .map(r -> entityManager.newReference(r)).anyMatch(ref -> isVisibleAttachment(ref));
    }

    public String getGradeForUser(String id) {
        return assignmentService.getGradeForSubmitter(submission.getId(), id);
    }

    public String getTimeSpent() {
        return assignmentService.getTimeSpent(submission);
    }

    private boolean isVisibleAttachment(Reference r) {
        return r.getProperties() != null && !"true".equals(r.getProperties().getProperty(AssignmentConstants.PROP_INLINE_SUBMISSION));
    }
}
