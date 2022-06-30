package org.sakaiproject.grading.impl.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import org.springframework.transaction.annotation.Transactional;

import org.sakaiproject.grading.api.model.Comment;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.model.GradableObject;
import org.sakaiproject.grading.api.model.GradebookAssignment;
import org.sakaiproject.grading.api.repository.CommentRepository;
import org.sakaiproject.hibernate.HibernateCriterionUtils;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

public class CommentRepositoryImpl extends SpringCrudRepositoryImpl<Comment, Long>  implements CommentRepository {

    @Transactional(readOnly = true)
    public Optional<Comment> findByStudentIdAndGradableObject_Gradebook_UidAndGradableObject_IdAndGradableObject_Removed(
            String studentUid, String gradebookUid, Long assignmentId, Boolean removed) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Comment> query = cb.createQuery(Comment.class);
        Root<Comment> comment = query.from(Comment.class);
        Join<Comment, GradableObject> go = comment.join("gradableObject");
        Join<GradableObject, Gradebook> gb = go.join("gradebook");
        query.where(cb.and(cb.equal(comment.get("studentId"), studentUid),
                            cb.equal(gb.get("uid"), gradebookUid),
                            cb.equal(go.get("id"), assignmentId)));
        return session.createQuery(query).uniqueResultOptional();
    }

    @Transactional(readOnly = true)
    public List<Comment> findByGradableObjectAndStudentIdIn(GradebookAssignment assignment, Collection<String> studentIds) {

        return (List<Comment>) sessionFactory.getCurrentSession()
            .createCriteria(Comment.class)
            .add(Restrictions.eq("gradableObject", assignment))
            .add(HibernateCriterionUtils.CriterionInRestrictionSplitter("studentId", studentIds))
            .list();
    }

    @Transactional(readOnly = true)
    public List<Comment> findByGradableObject_Gradebook_Uid(String gradebookUid) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Comment> query = cb.createQuery(Comment.class);
        Root<Comment> comment = query.from(Comment.class);
        Join<GradableObject, Gradebook> gb = comment.join("gradableObject").join("gradebook");
        return session.createQuery(query.where(cb.equal(gb.get("uid"), gradebookUid))).list();
    }

    @Transactional
    public int deleteByGradableObject(GradebookAssignment assignment) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<Comment> delete = cb.createCriteriaDelete(Comment.class);
        Root<Comment> comment = delete.from(Comment.class);
        delete.where(cb.equal(comment.get("gradableObject"), assignment));
        return session.createQuery(delete).executeUpdate();
    }
}
