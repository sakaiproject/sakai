package org.sakaiproject.grading.impl.repository;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.model.GradeMapping;
import org.sakaiproject.grading.api.repository.GradeMappingRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class GradeMappingRepositoryImpl extends SpringCrudRepositoryImpl<GradeMapping, Long>  implements GradeMappingRepository {

    @Transactional(readOnly = true)
    public List<GradeMapping> findByGradebook_Uid(String gradebookUid) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<GradeMapping> query = cb.createQuery(GradeMapping.class);
        Join<GradeMapping, Gradebook> gb = query.from(GradeMapping.class).join("gradebook");
        query.where(cb.equal(gb.get("uid"), gradebookUid));
        return session.createQuery(query).list();
    }

}
