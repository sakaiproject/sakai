package org.sakaiproject.grading.impl.repository;

import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import org.sakaiproject.grading.api.model.CourseGrade;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.repository.CourseGradeRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class CourseGradeRepositoryImpl extends SpringCrudRepositoryImpl<CourseGrade, Long>  implements CourseGradeRepository {

    @Transactional(readOnly = true)
    public List<CourseGrade> findByGradebook_Id(Long gradebookId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<CourseGrade> query = cb.createQuery(CourseGrade.class);
        Join<CourseGrade, Gradebook> gb = query.from(CourseGrade.class).join("gradebook");
        query.where(cb.equal(gb.get("id"), gradebookId));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<CourseGrade> findByGradebook_Uid(String gradebookUid) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<CourseGrade> query = cb.createQuery(CourseGrade.class);
        Join<CourseGrade, Gradebook> gb = query.from(CourseGrade.class).join("gradebook");
        query.where(cb.equal(gb.get("uid"), gradebookUid));
        return session.createQuery(query).list();
    }
}
