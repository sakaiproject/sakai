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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.model.EntityProperty;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA repository implementation for EntityProperty
 */
@Transactional
public class EntityPropertyRepositoryImpl extends SpringCrudRepositoryImpl<EntityProperty, Long> implements EntityPropertyRepository {

    @Override
    @Transactional(readOnly = true)
    public List<String> findDistinctEntityRefs(List<String> properties, List<String> values,
                                               List<Integer> comparisons, List<String> relations) {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<EntityProperty> root = query.from(EntityProperty.class);

        // Build predicates dynamically
        List<Predicate> predicateList = new ArrayList<>();

        for (int i = 0; i < properties.size(); i++) {
            String property = properties.get(i);
            String value = values.get(i);
            int comparison = comparisons.get(i);

            Predicate predicate;
            if (comparison == Restriction.LIKE) {  // LIKE comparison (value = 6)
                predicate = cb.like(root.get(property), "%" + value + "%");
            } else {  // EQUALS comparison (value = 0)
                predicate = cb.equal(root.get(property), value);
            }

            // Handle AND/OR logic
            if (i == 0) {
                predicateList.add(predicate);
            } else {
                String relation = relations.get(i);
                if ("and".equalsIgnoreCase(relation)) {
                    // For AND, we need to group previous predicates and add new one
                    Predicate combined = cb.and(predicateList.toArray(new Predicate[0]));
                    predicateList.clear();
                    predicateList.add(cb.and(combined, predicate));
                } else if ("or".equalsIgnoreCase(relation)) {
                    // For OR, combine with previous predicate
                    predicateList.add(cb.or(predicateList.remove(predicateList.size() - 1), predicate));
                }
            }
        }

        query.select(root.get("entityRef"))
                .distinct(true)
                .where(predicateList.toArray(new Predicate[0]))
                .orderBy(cb.asc(root.get("entityRef")));

        return sessionFactory.getCurrentSession()
                .createQuery(query)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntityProperty> findByEntityRef(String entityRef) {
        if (entityRef == null) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<EntityProperty> query = cb.createQuery(EntityProperty.class);
        Root<EntityProperty> root = query.from(EntityProperty.class);

        query.select(root)
                .where(cb.equal(root.get("entityRef"), entityRef));

        return sessionFactory.getCurrentSession()
                .createQuery(query)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntityProperty> findByEntityRefAndPropertyName(String entityRef, String propertyName) {
        if (entityRef == null || propertyName == null) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<EntityProperty> query = cb.createQuery(EntityProperty.class);
        Root<EntityProperty> root = query.from(EntityProperty.class);

        query.select(root)
                .where(cb.and(
                        cb.equal(root.get("entityRef"), entityRef),
                        cb.equal(root.get("propertyName"), propertyName)
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
        CriteriaDelete<EntityProperty> delete = cb.createCriteriaDelete(EntityProperty.class);
        Root<EntityProperty> root = delete.from(EntityProperty.class);

        delete.where(cb.equal(root.get("entityRef"), entityRef));

        return sessionFactory.getCurrentSession()
                .createQuery(delete)
                .executeUpdate();
    }

    @Override
    @Transactional
    public int deleteByEntityRefAndPropertyName(String entityRef, String propertyName) {
        if (entityRef == null || propertyName == null) {
            return 0;
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaDelete<EntityProperty> delete = cb.createCriteriaDelete(EntityProperty.class);
        Root<EntityProperty> root = delete.from(EntityProperty.class);

        delete.where(cb.and(
                cb.equal(root.get("entityRef"), entityRef),
                cb.equal(root.get("propertyName"), propertyName)
        ));

        return sessionFactory.getCurrentSession()
                .createQuery(delete)
                .executeUpdate();
    }
}
