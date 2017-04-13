package org.sakaiproject.hibernate;

import static org.springframework.core.GenericTypeResolver.resolveTypeArguments;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.springframework.core.GenericTypeResolver;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by enietzel on 2/22/17.
 */
public abstract class HibernateCrudRepository<T, ID extends Serializable> implements CrudRepository<T, ID> {

    @Getter
    private final Class<T> domainClass;

    @Setter
    protected SessionFactory sessionFactory;

    @SuppressWarnings("unchecked")
    public HibernateCrudRepository() {

        Class<?>[] classes = GenericTypeResolver.resolveTypeArguments(this.getClass(), Repository.class);
        domainClass = (classes != null && classes.length == 2) ? (Class<T>) classes[0] : null;
    }

    @Override
    @Transactional
    public <S extends T> S save(S entity) {

        sessionFactory.getCurrentSession().save(entity);
        return entity;
    }

    @Override
    @Transactional
    public <S extends T> Iterable<S> save(Iterable<S> entities) {

        List<S> list = new ArrayList<>();
        if (entities != null) {
            for (S entity : entities) {
                list.add(save(entity));
            }
        }
        return list;
    }

    @Override
    public T findOne(ID id) {
        Assert.notNull(id, "The id cannot be null");

        Object entity = sessionFactory.getCurrentSession().get(domainClass, id);
        return (T) entity;
    }

    @Override
    public boolean exists(ID id) {
        Assert.notNull(id, "The id cannot be null");

        return findOne(id) != null;
    }

    @Override
    public Iterable<T> findAll() {

        List<?> list = startCriteriaQuery().list();
        return (List<T>) list;
    }

    @Override
    public Iterable<T> findAll(Iterable<ID> ids) {

        List<T> list = new ArrayList<>();
        if (ids != null) {
            for (ID id : ids) {
                list.add(findOne(id));
            }
        }
        return list;
    }

    @Override
    public long count() {

        Object count = startCriteriaQuery().setProjection(Projections.rowCount()).uniqueResult();
        return ((Number) count).longValue();
    }

    @Override
    @Transactional
    public void delete(ID id) {

        delete(findOne(id));
    }

    @Override
    @Transactional
    public void delete(T entity) {

        sessionFactory.getCurrentSession().delete(entity);
    }

    @Override
    @Transactional
    public void delete(Iterable<? extends T> entities) {

        if (entities != null) {
            for (T entity : entities) {
                delete(entity);
            }
        }
    }

    @Override
    @Transactional
    public void deleteAll() {

        for (T entity : findAll()) {
            delete(entity);
        }
    }

    /**
     * Starts a Hibernate Criteria query for the type T in HibernateCrudRespository&lt;T, I&gt;
     * @return a Criteria query
     */
    protected Criteria startCriteriaQuery() {
        return sessionFactory.getCurrentSession().createCriteria(domainClass);
    }
}
