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

import java.time.Instant;
import java.util.Collection;
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

    boolean existsSubmissionSubmitter(Long submissionSubmitterId);

    AssignmentSubmission newSubmission(String assignmentId,
                       Optional<String> groupId,
                       Optional<Set<AssignmentSubmissionSubmitter>> submitters,
                       Optional<Set<String>> feedbackAttachments,
                       Optional<Set<String>> submittedAttachments,
                       Optional<Map<String, String>> properties);

    AssignmentSubmission findSubmissionForUser(String assignmentId, String userId);

    List<AssignmentSubmission> findSubmissionForUsers(String assignmentId, List<String> userIds);

    AssignmentSubmission findSubmissionForGroup(String assignmentId, String groupId);

    long countAssignmentsBySite(String siteId);

    /**
     * Count submissions for a given assignment.
     * If any of the parameters are null they are not included in the query.
     * @param assignmentId the assignment id whose submissions should be counted
     * @param graded if not null adds the requirement that the submission's graded field matches this value
     * @param hasSubmissionDate if not null adds the requirement of whether the submitted date can be null or not
     * @param userSubmission if not null adds the requirement that the submission's userSubmission field matches this value
     * @return
     */
    long countAssignmentSubmissions(String assignmentId, Boolean graded, Boolean hasSubmissionDate, Boolean userSubmission, List<String> userIds);

    void resetAssignment(Assignment assignment);

    /**
     * Find assignments that are linked with to a gradebook item
     * @param context the context the assignment is in
     * @param linkId the linked id or name of the gradebook item
     * @return the assignment id or empty if none is found
     */
    List<Assignment> findAssignmentsForGradebookLink(String context, String linkId);

    Collection<String> findGroupsForAssignmentById(String assignmentId);

    /**
     * Finds assignments that are set to auto-submit based on the current time.
     * @param siteId the id of the site
     * @param now the current time to filter closeDate
     */
    List<SimpleAssignmentAutoSubmit> findAutoSubmitAssignmentsBySite(String siteId, java.time.Instant now);

    /**
     * Finds draft submissions for a given assignment.
     * @param assignmentId the id of the assignment
     * @return a list of draft submissions for the assignment
     */
    List<SimpleSubmissionDraft> findDraftSubmissionsForAssignment(String assignmentId);

    /**
     * Finds all draft submissions that are eligible for auto-submit across all sites.
     * OPTIMIZED METHOD: Returns all eligible submissions in one query to improve performance.
     * @return a list of all eligible draft submissions
     */
    List<SimpleSubmissionDraft> findAllEligibleDraftSubmissions();

    class SimpleAssignmentAutoSubmit {
        public String id;
        public String title;
        public Instant dueTime;
        public Instant closeTime;
        public String context;
        public boolean draft;
        public boolean isGroup;
        public Map<String, String> properties;
    }

    class SimpleSubmissionDraft {
        public String id;
        public String gradableId;
        public Set<String> submitterIds;
        public String submittedText;
        public Set<String> attachments;
        public boolean submitted;
        public boolean draft;
        public Map<String, String> properties;
        public Instant dateModified;
    }
}
