/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.springframework.data;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.springframework.core.GenericTypeResolver;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import org.sakaiproject.springframework.data.PersistableEntity;
import org.sakaiproject.springframework.data.Repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Transactional(readOnly = true)
public abstract class SpringCrudRepositoryImpl<T extends PersistableEntity<ID>, ID extends Serializable> implements SpringCrudRepository<T, ID> {

    @Getter
    private final Class<T> domainClass;

    @Setter
    protected SessionFactory sessionFactory;

    @SuppressWarnings("unchecked")
    public SpringCrudRepositoryImpl() {

        Class<?>[] classes = GenericTypeResolver.resolveTypeArguments(this.getClass(), Repository.class);
        domainClass = (classes != null && classes.length == 2) ? (Class<T>) classes[0] : null;
    }

    @Override
    public long count() {

        Object count = startCriteriaQuery().setProjection(Projections.rowCount()).uniqueResult();
        return ((Number) count).longValue();
    }

    @Override
    @Transactional
    public <S extends T> S save(S entity) {

        Session session = sessionFactory.getCurrentSession();

        final boolean isNew = entity.getId() == null;
        if (isNew) {
            session.persist(entity);
            return entity;
        } else {
            return (S) session.merge(entity);
        }
    }

    @Override
    @Transactional
    public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {

        List<S> list = new ArrayList<>();
        if (entities != null) {
            entities.forEach(entity -> list.add(save(entity)));
        }
        return list;
    }

    @Override
    public T findById(ID id) {

        Assert.notNull(id, "The id cannot be null");
        return (T) sessionFactory.getCurrentSession().get(domainClass, id);
    }

    @Override
    public boolean existsById(ID id) {

        Assert.notNull(id, "The id cannot be null");
        return findById(id) != null;
    }

    @Override
    public Iterable<T> findAll() {
        return (List<T>) startCriteriaQuery().list();
    }

    @Override
    public Iterable<T> findAllById(Iterable<ID> ids) {

        List<T> list = new ArrayList<>();
        if (ids != null) {
            ids.forEach(id -> list.add(findById(id)));
        }
        return list;
    }

    @Override
    @Transactional
    public void delete(T entity) {

        Session session = sessionFactory.getCurrentSession();

        try {
            session.delete(entity);
        } catch (Exception he) {
            session.delete(session.merge(entity));
        }
    }

    @Override
    @Transactional
    public void deleteAll() {
        findAll().forEach(this::delete);
    }

    @Override
    @Transactional
    public void deleteAll(Iterable<? extends T> entities) {

        if (entities != null) {
            entities.forEach(this::delete);
        }
    }

    @Override
    @Transactional
    public void deleteById(ID id) {
        delete(findById(id));
    }

    /**
     * Starts a Hibernate Criteria query for the type T in HibernateCrudRespository&lt;T, I&gt;
     * @return a Criteria query
     */
    protected Criteria startCriteriaQuery() {
        return sessionFactory.getCurrentSession().createCriteria(domainClass);
    }
}
