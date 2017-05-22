package org.sakaiproject.assignment.persistence;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.transaction.Transactional;

import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.hibernate.Repository;
import org.sakaiproject.serialization.SerializableRepository;

/**
 * Created by enietzel on 4/12/17.
 */
public interface AssignmentRepository extends Repository<Assignment, String>, SerializableRepository<Assignment, String> {
    Assignment findAssignment(String id);

    List<Assignment> findAssignmentsBySite(String siteId);

    void newAssignment(Assignment assignment);

    void updateAssignment(Assignment assignment);

    boolean existsAssignment(String assignmentId);

    void deleteAssignment(String assignmentId);

    void deleteSubmission(String submissionId);

    void softDeleteAssignment(Assignment assignment);

    AssignmentSubmission findSubmission(String submissionId);

    void updateSubmission(AssignmentSubmission submission);

    boolean existsSubmission(String submissionId);

    void newSubmission(Assignment assignment, AssignmentSubmission submission, Optional<Set<AssignmentSubmissionSubmitter>> submitters, Optional<Set<String>> feedbackAttachments, Optional<Set<String>> submittedAttachments, Optional<Map<String, String>> properties);
}
