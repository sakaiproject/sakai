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
package org.sakaiproject.assignment.api.persistence;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.serialization.SerializableRepository;

/**
 * Created by enietzel on 4/12/17.
 */

public interface AssignmentRepository extends SerializableRepository<Assignment, String> {

    Assignment findAssignment(String id);

    List<Assignment> findAssignmentsBySite(String siteId);

    List<Assignment> findDeletedAssignmentsBySite(String siteId);

    List<String> findAllAssignmentIds();

    void newAssignment(Assignment assignment);

    boolean existsAssignment(String assignmentId);

    void deleteAssignment(String assignmentId);

    void deleteSubmission(String submissionId);

    void softDeleteAssignment(String assignmentId);

    AssignmentSubmission findSubmission(String submissionId);

    void updateSubmission(AssignmentSubmission submission);

    boolean existsSubmission(String submissionId);

    void newSubmission(Assignment assignment,
                       AssignmentSubmission submission,
                       Optional<Set<AssignmentSubmissionSubmitter>> submitters,
                       Optional<Set<String>> feedbackAttachments,
                       Optional<Set<String>> submittedAttachments,
                       Optional<Map<String, String>> properties);

    AssignmentSubmission findSubmissionForUser(String assignmentId, String userId);

    AssignmentSubmission findSubmissionForGroup(String assignmentId, String groupId);

    void initializeAssignment(Assignment assignment);

    /**
     * Count submissions for a given assignment.
     * If any of the parameters are null they are not included in the query.
     * @param assignmentId the assignment id whose submissions should be counted
     * @param graded if not null adds the requirement that the submission's graded field matches this value
     * @param hasSubmissionDate if not null adds the requirement of whether the submitted date can be null or not
     * @param userSubmission if not null adds the requirement that the submission's userSubmission field matches this value
     * @return
     */
    long countAssignmentSubmissions(String assignmentId, Boolean graded, Boolean hasSubmissionDate, Boolean userSubmission);

    void resetAssignment(Assignment assignment);
}
