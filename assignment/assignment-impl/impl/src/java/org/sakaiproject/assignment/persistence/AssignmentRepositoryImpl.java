package org.sakaiproject.assignment.persistence;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.NotImplementedException;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.hibernate.HibernateCrudRepository;
import org.sakaiproject.serialization.BasicSerializableRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by enietzel on 2/22/17.
 */
@Repository
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
}
