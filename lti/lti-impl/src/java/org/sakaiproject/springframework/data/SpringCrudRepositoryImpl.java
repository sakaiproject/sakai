package org.sakaiproject.springframework.data;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic implementation of {@link SpringCrudRepository}.
 *
 * @param <T> the entity type
 * @param <ID> the id type
 */
public class SpringCrudRepositoryImpl<T, ID extends Serializable> implements SpringCrudRepository<T, ID> {

    private SessionFactory sessionFactory;
    private Class<T> entityClass;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    @Transactional
    public T save(T entity) {
        getSession().saveOrUpdate(entity);
        return entity;
    }

    @Override
    @Transactional
    public Iterable<T> saveAll(Iterable<T> entities) {
        for (T entity : entities) {
            save(entity);
        }
        return entities;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<T> findById(ID id) {
        T entity = (T) getSession().get(entityClass, id);
        return Optional.ofNullable(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(ID id) {
        return findById(id).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> findAll() {
        return getSession().createQuery("from " + entityClass.getName()).list();
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<T> findAllById(Iterable<ID> ids) {
        StringBuilder queryBuilder = new StringBuilder("from " + entityClass.getName() + " where id in (");
        boolean first = true;
        for (ID id : ids) {
            if (!first) {
                queryBuilder.append(", ");
            }
            queryBuilder.append(id);
            first = false;
        }
        queryBuilder.append(")");
        return getSession().createQuery(queryBuilder.toString()).list();
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return (Long) getSession().createQuery("select count(*) from " + entityClass.getName()).uniqueResult();
    }

    @Override
    @Transactional
    public void deleteById(ID id) {
        findById(id).ifPresent(this::delete);
    }

    @Override
    @Transactional
    public void delete(T entity) {
        getSession().delete(entity);
    }

    @Override
    @Transactional
    public void deleteAll(Iterable<T> entities) {
        for (T entity : entities) {
            delete(entity);
        }
    }

    @Override
    @Transactional
    public void deleteAll() {
        getSession().createQuery("delete from " + entityClass.getName()).executeUpdate();
    }
}