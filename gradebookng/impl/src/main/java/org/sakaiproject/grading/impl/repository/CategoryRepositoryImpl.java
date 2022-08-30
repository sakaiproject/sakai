package org.sakaiproject.grading.impl.repository;

import java.util.List;

import org.hibernate.Session;

import org.springframework.transaction.annotation.Transactional;

import org.sakaiproject.grading.api.model.Category;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.repository.CategoryRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

public class CategoryRepositoryImpl extends SpringCrudRepositoryImpl<Category, Long>  implements CategoryRepository {

    @Transactional(readOnly = true)
    public List<Category> findByGradebook_IdAndRemoved(Long gradebookId, Boolean removed) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Category> query = cb.createQuery(Category.class);
        Root<Category> cat = query.from(Category.class);
        Join<Category, Gradebook> gb = cat.join("gradebook");
        query.where(cb.and(cb.equal(cat.get("removed"), removed), cb.equal(gb.get("id"), gradebookId)));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public boolean existsByNameAndGradebookAndRemoved(String name, Gradebook gradebook, Boolean removed) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Category> cat = query.from(Category.class);
        query.select(cb.count(cat))
            .where(cb.and(cb.equal(cat.get("name"), name),
                            cb.equal(cat.get("gradebook"), gradebook),
                            cb.equal(cat.get("removed"), removed)));
        return session.createQuery(query).getSingleResult() > 0L;
    }

    @Transactional(readOnly = true)
    public boolean existsByNameAndGradebookAndNotIdAndRemoved(String name, Gradebook gradebook, Long id, Boolean removed) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Category> cat = query.from(Category.class);
        query.select(cb.count(cat))
            .where(cb.and(cb.equal(cat.get("name"), name),
                            cb.equal(cat.get("gradebook"), gradebook),
                            cb.equal(cat.get("removed"), removed),
                            cb.notEqual(cat.get("id"), id)));
        return session.createQuery(query).getSingleResult() > 0L;
    }

    @Transactional(readOnly = true)
    public List<Category> findByGradebook_Uid(String gradebookUid) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Category> query = cb.createQuery(Category.class);
        Join<Category, Gradebook> gb = query.from(Category.class).join("gradebook");
        query.where(cb.equal(gb.get("uid"), gradebookUid));
        return session.createQuery(query).list();
    }
}
