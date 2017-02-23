package org.sakaiproject.assignment.persistence;

import java.util.List;

import org.sakaiproject.assignment.api.model.Assignment;

/**
 * Created by enietzel on 2/22/17.
 */
public interface AssignmentRepository {

    Assignment findAssignment(String id);
    List<Assignment> findAssignmentsBySite(String siteId);
    void saveAssignment(Assignment assignment);
    void deleteAssignment(Assignment assignment);
    void softDeleteAssignment(Assignment assignment);
}
