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
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.sakaiproject.serialization.MapperFactory;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import java.io.Serializable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Transactional(readOnly = true)
public abstract class SpringCrudRepositoryImpl<T extends PersistableEntity<ID>, ID extends Serializable> implements SpringCrudRepository<T, ID> {

    // currently using a static since all entities should use these mappers
    protected static ObjectMapper jsonMapper = MapperFactory.createDefaultJsonMapper();
    protected static ObjectMapper xmlMapper = MapperFactory.createDefaultXmlMapper();
    protected static ObjectMapper xmlMapperDisableCDataAsText = MapperFactory.xmlBuilder()
            .registerJavaTimeModule()
            .disableDateTimestamps()
            .ignoreUnknownProperties()
            .excludeNulls()
            .disableOutputCDataAsText()
            .disableNamespaceAware()
            .setMaxAttributeSize(32000)
            .enableRepairingNamespaces()
            .enablePrettyPrinting()
            .enableOutputXML11()
            .build();

    public static final int BATCH_SIZE = 100;

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

        boolean isNew = entity.getId() == null;
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
    public Optional<T> findById(ID id) {

        Assert.notNull(id, "The id cannot be null");
        return Optional.ofNullable(sessionFactory.getCurrentSession().get(domainClass, id));
    }

    @Override
    public T getById(ID id) {

        Assert.notNull(id, "The id cannot be null");
        return sessionFactory.getCurrentSession().load(domainClass, id);
    }

    @Override
    public T getReferenceById(ID id) {

        Assert.notNull(id, "The id cannot be null");
        return sessionFactory.getCurrentSession().load(domainClass, id);
    }

    @Override
    public boolean existsById(ID id) {

        Assert.notNull(id, "The id cannot be null");
        return findById(id).isPresent();
    }

    @Override
    public List<T> findAll() {
        return (List<T>) startCriteriaQuery().list();
    }

    @Override
    public Page<T> findAll(Pageable pageable) {

        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(domainClass);
        criteria.setFirstResult((int) pageable.getOffset());
        criteria.setMaxResults(pageable.getPageSize());
        return new PageImpl(criteria.list());
    }

    @Override
    public Iterable<T> findAllById(Iterable<ID> ids) {

        List<T> list = new ArrayList<>();
        if (ids != null) {
            ids.forEach(id -> findById(id).ifPresent(list::add));
        }
        return list;
    }

    @Override
    public List<T> findAllByIds(List<ID> ids) {
        if (ids == null || ids.isEmpty()) return List.of();

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(domainClass);
        Root<T> root = query.from(domainClass);
        EntityType<T> entityType = sessionFactory.getMetamodel().entity(domainClass);
        String idField = entityType.getId(entityType.getIdType().getJavaType()).getName();

        query.select(root)
                .where(root.get(idField).in(ids));

        return sessionFactory.getCurrentSession()
                .createQuery(query)
                .getResultList();
    }

    @Override
    @Transactional
    public void delete(T entity) {
        if (entity != null) {
            deleteById(entity.getId());
        } else {
            log.warn("Can not perform delete on a null entity");
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
        if (id != null) {
            Session session = sessionFactory.getCurrentSession();
            findById(id).ifPresent(session::delete);
        } else {
            log.warn("Can not perform delete with a null id");
        }
    }

    @Override
    public String toJSON(T t) {
        String json = "";

        if (t != null) {
            sessionFactory.getCurrentSession().refresh(t);
            try {
                json = jsonMapper.writeValueAsString(t);
            } catch (JsonProcessingException e) {
                log.warn("Could not serialize to json", e);
            }
        }
        return json;
    }

    @Override
    public T fromJSON(String json) {
        T obj = null;

        if (StringUtils.isNotBlank(json)) {
            try {
                obj = jsonMapper.readValue(json, getDomainClass());
            } catch (IOException e) {
                log.warn("Could not deserialize json", e);
            }
        }
        return obj;
    }

    @Override
    public String toXML(T t) {
        return toXML(t, true);
    }

    @Override
    public String toXML(T t, boolean cdataAsText) {
        String xml = "";

        if (t != null) {
            sessionFactory.getCurrentSession().refresh(t);
            try {
                xml = cdataAsText ? xmlMapper.writeValueAsString(t) : xmlMapperDisableCDataAsText.writeValueAsString(t);
            } catch (JsonProcessingException e) {
                log.warn("Could not serialize to xml", e);
            }
        }
        return xml;
    }

    @Override
    public T fromXML(String xml) {
        T obj = null;

        if (StringUtils.isNotBlank(xml)) {
            try {
                obj = xmlMapper.readValue(xml, getDomainClass());
            } catch (IOException e) {
                log.warn("Could not deserialize xml", e);
            }
        }
        return obj;
    }

    /**
     * Starts a Hibernate Criteria query for the type T in HibernateCrudRespository&lt;T, I&gt;
     * @return a Criteria query
     */
    protected Criteria startCriteriaQuery() {
        return sessionFactory.getCurrentSession().createCriteria(domainClass);
    }
}
