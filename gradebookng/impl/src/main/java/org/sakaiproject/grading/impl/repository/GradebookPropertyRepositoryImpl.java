package org.sakaiproject.grading.impl.repository;

import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import java.util.Optional;

import org.sakaiproject.grading.api.model.GradebookProperty;
import org.sakaiproject.grading.api.repository.GradebookPropertyRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class GradebookPropertyRepositoryImpl extends SpringCrudRepositoryImpl<GradebookProperty, Long>  implements GradebookPropertyRepository {

    public Optional<GradebookProperty> findByName(String name) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<GradebookProperty> query = cb.createQuery(GradebookProperty.class);
        Root<GradebookProperty> prop = query.from(GradebookProperty.class);
        query.where(cb.equal(prop.get("name"), name));
        return session.createQuery(query).uniqueResultOptional();
    }
}
