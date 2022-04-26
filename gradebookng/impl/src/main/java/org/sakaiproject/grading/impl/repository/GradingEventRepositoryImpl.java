package org.sakaiproject.grading.impl.repository;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import org.springframework.transaction.annotation.Transactional;

import org.sakaiproject.grading.api.model.GradableObject;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.model.GradebookAssignment;
import org.sakaiproject.grading.api.model.GradingEvent;
import org.sakaiproject.grading.api.repository.GradingEventRepository;
import org.sakaiproject.hibernate.HibernateCriterionUtils;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class GradingEventRepositoryImpl extends SpringCrudRepositoryImpl<GradingEvent, Long>  implements GradingEventRepository {

    @Transactional(readOnly = true)
    public List<GradingEvent> findByGradableObject_Gradebook_Uid(String gradebookUid) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<GradingEvent> query = cb.createQuery(GradingEvent.class);
        Root<GradingEvent> ge = query.from(GradingEvent.class);
        Join<GradingEvent, Gradebook> gb = ge.join("gradableObject").join("gradebook");
        query.where(cb.equal(gb.get("uid"), gradebookUid));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<GradingEvent> findByGradableObject_IdAndStudentIdOrderByDateGraded(Long assignmentId, String studentId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<GradingEvent> query = cb.createQuery(GradingEvent.class);
        Root<GradingEvent> ge = query.from(GradingEvent.class);
        Join<GradingEvent, GradableObject> go = ge.join("gradableObject");
        query.where(cb.and(cb.equal(go.get("id"), assignmentId), cb.equal(ge.get("studentId"), studentId)))
            .orderBy(cb.desc(ge.get("dateGraded")));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<GradingEvent> findByDateGreaterThanEqualAndGradableObject_IdIn(Date since, List<Long> assignmentIds) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<GradingEvent> query = cb.createQuery(GradingEvent.class);
        Root<GradingEvent> ge = query.from(GradingEvent.class);
        Join<GradingEvent, GradableObject> go = ge.join("gradableObject");
        query.where(cb.and(cb.greaterThanOrEqualTo(ge.get("dateGraded"), since), go.get("id").in(assignmentIds)));
        return session.createQuery(query).list();

        /*
        return (List<GradingEvent>) sessionFactory.getCurrentSession()
            .createCriteria(GradingEvent.class)
            .createAlias("gradableObject", "go")
            .add(Restrictions.and(
                Restrictions.ge("dateGraded", since),
                HibernateCriterionUtils.CriterionInRestrictionSplitter("go.id", assignmentIds)))
            .list();
            */
    }

    @Transactional
    public int deleteByGradableObject(GradebookAssignment assignment) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<GradingEvent> delete = cb.createCriteriaDelete(GradingEvent.class);
        Root<GradingEvent> ge = delete.from(GradingEvent.class);
        delete.where(cb.equal(ge.get("gradableObject"), assignment));
        return session.createQuery(delete).executeUpdate();
    }
}
