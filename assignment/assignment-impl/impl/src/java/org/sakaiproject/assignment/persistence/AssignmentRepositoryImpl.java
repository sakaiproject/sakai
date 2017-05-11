package org.sakaiproject.assignment.persistence;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.serialization.BasicSerializableRepository;

/**
 * Created by enietzel on 2/22/17.
 */
public class AssignmentRepositoryImpl extends BasicSerializableRepository<Assignment, String> implements AssignmentRepository {

    @Override
    public Assignment findAssignment(String id) {
        return findOne(id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Assignment> findAssignmentsBySite(String siteId) {
        return startCriteriaQuery().add(Restrictions.eq("context", siteId)).list();
    }

    @Override
    public void saveAssignment(Assignment assignment) {
        save(assignment);
    }

    @Override
    public void deleteAssignment(Assignment assignment) {
        delete(assignment);
    }

    @Override
    public void softDeleteAssignment(Assignment assignment) {
        throw new NotImplementedException("Soft Delete is currently not implemented");
    }

    @Override
    public AssignmentSubmission findSubmission(String submissionId) {
        return (AssignmentSubmission) sessionFactory.getCurrentSession().createCriteria(AssignmentSubmission.class).add(Restrictions.eq("id", submissionId)).uniqueResult();
    }

    @Override
    public void saveSubmission(AssignmentSubmission submission) {
        Assignment assignment = submission.getAssignment();
        saveAssignment(assignment);
    }
}
