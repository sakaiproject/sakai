package org.sakaiproject.assignment.persistence;

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

    @SuppressWarnings("unchecked")
    List<Assignment> findAssignmentsBySite(String siteId);

    void saveAssignment(Assignment assignment);

    void deleteAssignment(Assignment assignment);

    void softDeleteAssignment(Assignment assignment);

    AssignmentSubmission findSubmission(String submissionId);

    void saveSubmission(Assignment assignment, AssignmentSubmission submission, Optional<Set<AssignmentSubmissionSubmitter>> submitters, Optional<Set<String>> feedbackAttachments, Optional<Set<String>> submittedAttachments, Optional<Map<String, String>> properties);
}
