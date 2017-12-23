package org.sakaiproject.assignment.api.conversion;

import java.util.List;

public interface AssignmentDataProvider {
    List<String> fetchAssignmentsToConvert();

    String fetchAssignment(String assignmentId);

    String fetchAssignmentContent(String contentId);

    List<String> fetchAssignmentSubmissions(String assignmentId);
}
