package org.sakaiproject.grading.impl.repository;

import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import org.sakaiproject.grading.api.model.CourseGradeRecord;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.model.GradableObject;
import org.sakaiproject.grading.api.repository.CourseGradeRecordRepository;
import org.sakaiproject.hibernate.HibernateCriterionUtils;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class CourseGradeRecordRepositoryImpl extends SpringCrudRepositoryImpl<CourseGradeRecord, Long>  implements CourseGradeRecordRepository {

    @Transactional(readOnly = true)
    public List<CourseGradeRecord> findByGradableObject_Id(Long id) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<CourseGradeRecord> query = cb.createQuery(CourseGradeRecord.class);
        Join<CourseGradeRecord, GradableObject> go = query.from(CourseGradeRecord.class).join("gradableObject");
        query.where(cb.equal(go.get("id"), id));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<CourseGradeRecord> findByGradableObject_GradebookAndEnteredGradeNotNull(Gradebook gradebook) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<CourseGradeRecord> query = cb.createQuery(CourseGradeRecord.class);
        Root<CourseGradeRecord> cgr = query.from(CourseGradeRecord.class);
        Join<CourseGradeRecord, GradableObject> go = cgr.join("gradableObject");
        query.where(cb.and(cb.equal(go.get("gradebook"), gradebook), cb.isNotNull(cgr.get("enteredGrade"))));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public Optional<CourseGradeRecord> findByGradableObject_GradebookAndStudentId(Gradebook gradebook, String studentId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<CourseGradeRecord> query = cb.createQuery(CourseGradeRecord.class);
        Root<CourseGradeRecord> cgr = query.from(CourseGradeRecord.class);
        Join<CourseGradeRecord, GradableObject> go = cgr.join("gradableObject");
        query.where(cb.and(cb.equal(cgr.get("studentId"), studentId),
                            cb.equal(go.get("gradebook"), gradebook)));
        return session.createQuery(query).uniqueResultOptional();
    }

    @Transactional(readOnly = true)
    public Long countByGradableObject_Gradebook_IdAndEnteredGradeNotNullAndStudentIdIn(Long gradebookId, Set<String> studentIds) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<CourseGradeRecord> cgr = query.from(CourseGradeRecord.class);
        Join<GradableObject, Gradebook> gb = cgr.join("gradableObject").join("gradebook");
        query.select(cb.count(cgr))
            .where(cb.and(cb.equal(gb.get("id"), gradebookId),
                            cb.isNotNull(cgr.get("enteredGrade")),
                            cgr.get("studentId").in(studentIds)));
        return session.createQuery(query).uniqueResult();

        /*
        return (Long) sessionFactory.getCurrentSession().createCriteria(CourseGradeRecord.class)
            .createAlias("gradableObject", "go")
            .createAlias("go.gradebook", "gb")
            .add(Restrictions.eq("gb.id", gradebookId))
            .add(Restrictions.isNotNull("enteredGrade"))
            .add(HibernateCriterionUtils.CriterionInRestrictionSplitter("studentId", studentIds))
            .setProjection(Projections.rowCount())
            .uniqueResult();
        */
    }

    @Transactional(readOnly = true)
    public List<CourseGradeRecord> findByGradableObject_Gradebook_Uid(String gradebookUid) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<CourseGradeRecord> query = cb.createQuery(CourseGradeRecord.class);
        Root<CourseGradeRecord> cgr = query.from(CourseGradeRecord.class);
        Join<GradableObject, Gradebook> gb = cgr.join("gradableObject").join("gradebook");
        query.where(cb.equal(gb.get("uid"), gradebookUid));
        return session.createQuery(query).list();
    }
}
