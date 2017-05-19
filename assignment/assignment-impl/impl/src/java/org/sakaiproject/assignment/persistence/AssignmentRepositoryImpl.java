package org.sakaiproject.assignment.persistence;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.transaction.Transactional;

import org.apache.commons.lang.NotImplementedException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
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
    @Transactional
    public void saveAssignment(Assignment assignment) {
        save(assignment);
    }

    @Override
    @Transactional
    public void deleteAssignment(Assignment assignment) {
        delete(assignment);
    }

    @Override
    @Transactional
    public void softDeleteAssignment(Assignment assignment) {
        throw new NotImplementedException("Soft Delete is currently not implemented");
    }

    @Override
    public AssignmentSubmission findSubmission(String submissionId) {
        return (AssignmentSubmission) sessionFactory.getCurrentSession().createCriteria(AssignmentSubmission.class).add(Restrictions.eq("id", submissionId)).uniqueResult();
    }

    @Override
    @Transactional
    public void saveSubmission(Assignment assignment, AssignmentSubmission submission, Optional<Set<AssignmentSubmissionSubmitter>> submitters, Optional<Set<String>> feedbackAttachments, Optional<Set<String>> submittedAttachments, Optional<Map<String, String>> properties) {
        submitters.ifPresent(submission::setSubmitters);
        submitters.ifPresent(s -> s.forEach(submitter -> submitter.setSubmission(submission)));
        feedbackAttachments.ifPresent(submission::setFeedbackAttachments);
        submittedAttachments.ifPresent(submission::setSubmittedAttachments);
        properties.ifPresent(submission::setProperties);

        submission.setAssignment(assignment);
        assignment.getSubmissions().add(submission);

        sessionFactory.getCurrentSession().persist(assignment);
//        sessionFactory.getCurrentSession().save(assignment);
//        session.save(submission);
//        submitters.ifPresent(s -> s.forEach(session::save));
//        session.persist(submission);
//        submitters.ifPresent(s -> s.forEach(session::persist));
        // TODO remove the commented items
    }
}
