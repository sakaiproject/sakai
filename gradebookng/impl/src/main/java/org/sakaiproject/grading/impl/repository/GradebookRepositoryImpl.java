package org.sakaiproject.grading.impl.repository;

import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.repository.GradebookRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class GradebookRepositoryImpl extends SpringCrudRepositoryImpl<Gradebook, Long>  implements GradebookRepository {

    @Transactional(readOnly = true)
    public Optional<Gradebook> findByUid(String uid) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Gradebook> query = cb.createQuery(Gradebook.class);
        Root<Gradebook> gradebook = query.from(Gradebook.class);
        query.where(cb.equal(gradebook.get("uid"), uid));
        return session.createQuery(query).uniqueResultOptional();
    }

    @Transactional
    public int deleteByUid(String uid) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<Gradebook> delete = cb.createCriteriaDelete(Gradebook.class);
        Root<Gradebook> gradebook = delete.from(Gradebook.class);
        delete.where(cb.equal(gradebook.get("uid"), uid));
        return session.createQuery(delete).executeUpdate();
    }
}
