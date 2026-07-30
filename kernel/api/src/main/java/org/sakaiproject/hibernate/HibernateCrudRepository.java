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
package org.sakaiproject.hibernate;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.SessionFactory;
import org.springframework.core.GenericTypeResolver;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import org.sakaiproject.springframework.data.Repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by enietzel on 2/22/17.
 */
@Transactional(readOnly = true)
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

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(domainClass);
        Root<T> root = query.from(domainClass);
        query.select(root);

        return sessionFactory.getCurrentSession().createQuery(query).getResultList();
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

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(domainClass);
        query.select(cb.count(root));

        return sessionFactory.getCurrentSession().createQuery(query).getSingleResult();
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

    @Override
    public void refresh(T entity) {
        sessionFactory.getCurrentSession().refresh(entity);
    }

    @Override
    @Transactional
    public T merge(T entity) {
        return (T) sessionFactory.getCurrentSession().merge(entity);
    }

    @Override
    @Transactional
    public void persist(T entity) {
        sessionFactory.getCurrentSession().persist(entity);
    }

    @Override
    @Transactional
    public void update(T entity) {
        sessionFactory.getCurrentSession().update(entity);
    }
}