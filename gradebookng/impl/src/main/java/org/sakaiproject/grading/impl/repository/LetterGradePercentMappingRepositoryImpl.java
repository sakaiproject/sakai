package org.sakaiproject.grading.impl.repository;

import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import org.sakaiproject.grading.api.model.LetterGradePercentMapping;
import org.sakaiproject.grading.api.repository.LetterGradePercentMappingRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class LetterGradePercentMappingRepositoryImpl extends SpringCrudRepositoryImpl<LetterGradePercentMapping, Long>  implements LetterGradePercentMappingRepository {

    @Transactional(readOnly = true)
    public List<LetterGradePercentMapping> findByMappingType(Integer mappingType) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<LetterGradePercentMapping> query = cb.createQuery(LetterGradePercentMapping.class);
        Root<LetterGradePercentMapping> pm = query.from(LetterGradePercentMapping.class);
        query.where(cb.equal(pm.get("mappingType"), mappingType));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public Optional<LetterGradePercentMapping> findByGradebookIdAndMappingType(Long gradebookId, Integer mappingType) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<LetterGradePercentMapping> query = cb.createQuery(LetterGradePercentMapping.class);
        Root<LetterGradePercentMapping> pm = query.from(LetterGradePercentMapping.class);
        query.where(cb.and(cb.equal(pm.get("gradebookId"), gradebookId), cb.equal(pm.get("mappingType"), mappingType)));
        return session.createQuery(query).uniqueResultOptional();
    }
}
