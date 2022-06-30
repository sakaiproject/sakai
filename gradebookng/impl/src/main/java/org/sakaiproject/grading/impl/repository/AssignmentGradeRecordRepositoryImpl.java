package org.sakaiproject.grading.impl.repository;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import org.sakaiproject.grading.api.model.AssignmentGradeRecord;
import org.sakaiproject.grading.api.model.GradableObject;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.model.GradebookAssignment;
import org.sakaiproject.grading.api.repository.AssignmentGradeRecordRepository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import org.sakaiproject.hibernate.HibernateCriterionUtils;

import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class AssignmentGradeRecordRepositoryImpl extends SpringCrudRepositoryImpl<AssignmentGradeRecord, Long>  implements AssignmentGradeRecordRepository {

    @Transactional(readOnly = true)
    public List<AssignmentGradeRecord> findByGradableObject_Gradebook_IdAndGradableObject_RemovedOrderByPointsEarned(Long gradebookId, Boolean removed) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<AssignmentGradeRecord> query = cb.createQuery(AssignmentGradeRecord.class);
        Root<AssignmentGradeRecord> agr = query.from(AssignmentGradeRecord.class);
        Join<AssignmentGradeRecord, GradableObject> go = agr.join("gradableObject");
        Join<GradableObject, Gradebook> gb = go.join("gradebook");
        query.where(cb.and(cb.equal(gb.get("id"), gradebookId), cb.equal(go.get("removed"), removed)))
            .orderBy(cb.asc(agr.get("pointsEarned")));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<AssignmentGradeRecord> findByGradableObject_IdAndGradableObject_RemovedOrderByPointsEarned(Long gradableObjectId, Boolean removed) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<AssignmentGradeRecord> query = cb.createQuery(AssignmentGradeRecord.class);
        Root<AssignmentGradeRecord> agr = query.from(AssignmentGradeRecord.class);
        Join<AssignmentGradeRecord, GradableObject> go = agr.join("gradableObject");
        query.where(cb.and(cb.equal(go.get("id"), gradableObjectId), cb.equal(go.get("removed"), removed)))
            .orderBy(cb.asc(agr.get("pointsEarned")));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public Optional<AssignmentGradeRecord> findByGradableObject_IdAndStudentId(Long gradableObjectId, String studentId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<AssignmentGradeRecord> query = cb.createQuery(AssignmentGradeRecord.class);
        Root<AssignmentGradeRecord> agr = query.from(AssignmentGradeRecord.class);
        Join<AssignmentGradeRecord, GradableObject> go = agr.join("gradableObject");
        query.where(cb.and(cb.equal(go.get("id"), gradableObjectId), cb.equal(agr.get("studentId"), studentId)));
        return session.createQuery(query).uniqueResultOptional();
    }

    @Transactional(readOnly = true)
    public List<AssignmentGradeRecord> findByGradableObject_Gradebook_Id(Long gradebookId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<AssignmentGradeRecord> query = cb.createQuery(AssignmentGradeRecord.class);
        Root<AssignmentGradeRecord> agr = query.from(AssignmentGradeRecord.class);
        Join<GradableObject, Gradebook> gb = agr.join("gradableObject").join("gradebook");
        query.where(cb.equal(gb.get("id"), gradebookId));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<AssignmentGradeRecord> findByGradableObject_Gradebook_Uid(String gradebookUid) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<AssignmentGradeRecord> query = cb.createQuery(AssignmentGradeRecord.class);
        Root<AssignmentGradeRecord> agr = query.from(AssignmentGradeRecord.class);
        Join<GradableObject, Gradebook> gb = agr.join("gradableObject").join("gradebook");
        query.where(cb.equal(gb.get("uid"), gradebookUid));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<AssignmentGradeRecord> findByGradableObject_RemovedAndGradableObject_IdInAndStudentIdIn(Boolean removed, List<Long> gradableObjectIds, List<String> studentIds) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<AssignmentGradeRecord> query = cb.createQuery(AssignmentGradeRecord.class);
        Root<AssignmentGradeRecord> agr = query.from(AssignmentGradeRecord.class);
        Join<AssignmentGradeRecord, GradableObject> go = agr.join("gradableObject");
        query.where(cb.and(cb.equal(go.get("removed"), removed)),
                                    go.get("id").in(gradableObjectIds),
                                    agr.get("studentId").in(studentIds));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<AssignmentGradeRecord> findByGradableObject_Gradebook_IdAndGradableObject_RemovedAndStudentIdIn(Long gradebookId, Boolean removed, Collection<String> studentIds) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<AssignmentGradeRecord> query = cb.createQuery(AssignmentGradeRecord.class);
        Root<AssignmentGradeRecord> agr = query.from(AssignmentGradeRecord.class);
        Join<AssignmentGradeRecord, GradableObject> go = agr.join("gradableObject");
        Join<GradableObject, Gradebook> gb = go.join("gradebook");
        query.where(cb.and(cb.equal(gb.get("id"), gradebookId), cb.equal(go.get("removed"), false), agr.get("studentId").in(studentIds)));
        return session.createQuery(query).list();

        /*
        return (List<AssignmentGradeRecord>) sessionFactory.getCurrentSession()
            .createCriteria(AssignmentGradeRecord.class)
            .createAlias("gradableObject", "go")
            .createAlias("gradableObject.gradebook", "gb")
            .add(Restrictions.equal("gb.id", gradebookId))
            .add(Restrictions.equal("go.removed", false))
            .add(HibernateCriterionUtils.CriterionInRestrictionSplitter("studentId", studentIds))
            .list();
            */
    }

    @Transactional(readOnly = true)
    public List<AssignmentGradeRecord> findByGradableObjectAndStudentIdIn(GradebookAssignment assignment, Collection<String> studentIds) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<AssignmentGradeRecord> query = cb.createQuery(AssignmentGradeRecord.class);
        Root<AssignmentGradeRecord> agr = query.from(AssignmentGradeRecord.class);
        query.where(cb.and(cb.equal(agr.get("gradableObject"), assignment), agr.get("studendId").in(studentIds)));
        return session.createQuery(query).list();

        /*
        return (List<AssignmentGradeRecord>) sessionFactory.getCurrentSession()
            .createCriteria(AssignmentGradeRecord.class)
            .add(Restrictions.equal("gradableObject", assignment))
            .add(HibernateCriterionUtils.CriterionInRestrictionSplitter("studentId", studentIds))
            .list();
        */
    }


    @Transactional
    public int deleteByGradableObject(GradebookAssignment assignment) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<AssignmentGradeRecord> delete = cb.createCriteriaDelete(AssignmentGradeRecord.class);
        Root<AssignmentGradeRecord> agr = delete.from(AssignmentGradeRecord.class);
        delete.where(cb.equal(agr.get("gradableObject"), assignment));
        return session.createQuery(delete).executeUpdate();
    }
}
