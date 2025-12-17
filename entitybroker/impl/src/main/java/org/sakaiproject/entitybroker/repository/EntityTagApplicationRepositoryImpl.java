/**
 * Copyright (c) 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.entitybroker.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.sakaiproject.entitybroker.model.EntityTagApplication;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA repository implementation for EntityTagApplication
 */
@Repository
@Transactional
public class EntityTagApplicationRepositoryImpl extends SpringCrudRepositoryImpl<EntityTagApplication, Long>
    implements EntityTagApplicationRepository {

    @Override
    @Transactional(readOnly = true)
    public List<EntityTagApplication> findByEntityRef(String entityRef) {
        if (entityRef == null) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<EntityTagApplication> query = cb.createQuery(EntityTagApplication.class);
        Root<EntityTagApplication> root = query.from(EntityTagApplication.class);

        query.select(root)
             .where(cb.equal(root.get("entityRef"), entityRef));

        return sessionFactory.getCurrentSession()
                             .createQuery(query)
                             .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntityTagApplication> findByEntityRefAndTags(String entityRef, String[] tags) {
        if (entityRef == null || tags == null || tags.length == 0) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<EntityTagApplication> query = cb.createQuery(EntityTagApplication.class);
        Root<EntityTagApplication> root = query.from(EntityTagApplication.class);

        query.select(root)
             .where(cb.and(
                 cb.equal(root.get("entityRef"), entityRef),
                 root.get("tag").in(Arrays.asList(tags))
             ));

        return sessionFactory.getCurrentSession()
                             .createQuery(query)
                             .getResultList();
    }

    @Override
    @Transactional
    public int deleteByEntityRef(String entityRef) {
        if (entityRef == null) {
            return 0;
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaDelete<EntityTagApplication> delete = cb.createCriteriaDelete(EntityTagApplication.class);
        Root<EntityTagApplication> root = delete.from(EntityTagApplication.class);

        delete.where(cb.equal(root.get("entityRef"), entityRef));

        return sessionFactory.getCurrentSession()
                             .createQuery(delete)
                             .executeUpdate();
    }

    @Override
    @Transactional
    public int deleteByEntityRefAndTags(String entityRef, String[] tags) {
        if (entityRef == null || tags == null || tags.length == 0) {
            return 0;
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaDelete<EntityTagApplication> delete = cb.createCriteriaDelete(EntityTagApplication.class);
        Root<EntityTagApplication> root = delete.from(EntityTagApplication.class);

        delete.where(cb.and(
            cb.equal(root.get("entityRef"), entityRef),
            root.get("tag").in(Arrays.asList(tags))
        ));

        return sessionFactory.getCurrentSession()
                             .createQuery(delete)
                             .executeUpdate();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntityTagApplication> findByTagsAndPrefixes(String[] tags, String[] prefixes, int limit, int start) {
        if (tags == null || tags.length == 0) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<EntityTagApplication> query = cb.createQuery(EntityTagApplication.class);
        Root<EntityTagApplication> root = query.from(EntityTagApplication.class);

        // Build predicates
        List<javax.persistence.criteria.Predicate> predicates = new ArrayList<>();
        predicates.add(root.get("tag").in(Arrays.asList(tags)));

        if (prefixes != null && prefixes.length > 0) {
            predicates.add(root.get("entityPrefix").in(Arrays.asList(prefixes)));
        }

        query.select(root)
             .where(predicates.toArray(new javax.persistence.criteria.Predicate[0]))
             .orderBy(cb.asc(root.get("entityRef")));

        javax.persistence.Query jpaQuery = sessionFactory.getCurrentSession().createQuery(query);

        if (start > 0) {
            jpaQuery.setFirstResult(start);
        }
        if (limit > 0) {
            jpaQuery.setMaxResults(limit);
        }

        return jpaQuery.getResultList();
    }
}
