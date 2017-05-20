package org.sakaiproject.assignment.persistence;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.transaction.Transactional;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
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
    public void newAssignment(Assignment assignment) {
        if (!existsAssignment(assignment.getId())) {
            sessionFactory.getCurrentSession().persist(assignment);
        }
    }

    @Override
    @Transactional
    public Assignment saveAssignment(Assignment assignment) {
        if (existsAssignment(assignment.getId())) {
            return (Assignment) sessionFactory.getCurrentSession().merge(assignment);
        }
        return null;
    }

    @Override
    @Transactional
    public boolean existsAssignment(String assignmentId) {
        if (assignmentId != null && exists(assignmentId)) {
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void deleteAssignment(Assignment assignment) {
        if (existsAssignment(assignment.getId())) {
            delete(assignment);
        }
    }

    @Override
    @Transactional
    public void deleteSubmission(String submissionId) {
        Session session = sessionFactory.getCurrentSession();
        AssignmentSubmission submission = (AssignmentSubmission) session.byId(AssignmentSubmission.class).load(submissionId);
        if (submission != null) {
            Assignment assignment = submission.getAssignment();
            // must call refresh here to ensure the collections are initialized before changing, this is due to lazy loaded entities
            session.refresh(assignment);
            assignment.getSubmissions().remove(submission);
            session.merge(assignment);
        }
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
    public AssignmentSubmission saveSubmission(AssignmentSubmission submission) {
        if (existsSubmission(submission.getId())) {
            return (AssignmentSubmission) sessionFactory.getCurrentSession().merge(submission);
        }
        return null;
    }

    @Override
    @Transactional
    public boolean existsSubmission(String submissionId) {
        if (submissionId != null && sessionFactory.getCurrentSession().get(AssignmentSubmission.class, submissionId) != null) {
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void newSubmission(Assignment assignment, AssignmentSubmission submission, Optional<Set<AssignmentSubmissionSubmitter>> submitters, Optional<Set<String>> feedbackAttachments, Optional<Set<String>> submittedAttachments, Optional<Map<String, String>> properties) {
        if (!existsSubmission(submission.getId()) && exists(assignment.getId())) {
            submitters.ifPresent(submission::setSubmitters);
            submitters.ifPresent(s -> s.forEach(submitter -> submitter.setSubmission(submission)));
            feedbackAttachments.ifPresent(submission::setFeedbackAttachments);
            submittedAttachments.ifPresent(submission::setSubmittedAttachments);
            properties.ifPresent(submission::setProperties);

            submission.setAssignment(assignment);
            assignment.getSubmissions().add(submission);

            sessionFactory.getCurrentSession().persist(assignment);
            // TODO remove the commented items
        }
    }
}
