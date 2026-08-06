/**
 * Copyright (c) 2023 The Apereo Foundation
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
package org.sakaiproject.condition.impl.persistence;

import java.util.List;

import org.sakaiproject.condition.api.model.Condition;
import org.sakaiproject.condition.api.model.ConditionType;
import org.sakaiproject.condition.api.persistence.ConditionRepository;
import org.sakaiproject.serialization.BasicSerializableRepository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public class ConditionRepositoryImpl extends BasicSerializableRepository<Condition, String> implements ConditionRepository {


    @Override
    public Condition findConditionForId(String conditionId) {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Condition> query = cb.createQuery(Condition.class);
        Root<Condition> root = query.from(Condition.class);

        query.select(root).where(cb.equal(root.get("id"), conditionId));

        return sessionFactory.getCurrentSession().createQuery(query).uniqueResult();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Condition> findConditionsForSite(String siteId) {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Condition> query = cb.createQuery(Condition.class);

        Root<Condition> root = query.from(Condition.class);
        query.select(root).where(cb.equal(root.get("siteId"), siteId));

        return sessionFactory.getCurrentSession().createQuery(query).getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Condition> findConditionsForItem(String siteId, String toolId, String itemId) {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Condition> query = cb.createQuery(Condition.class);
        Root<Condition> root = query.from(Condition.class);

        query.select(root).where(
            cb.equal(root.get("siteId"), siteId),
            cb.equal(root.get("toolId"), toolId),
            cb.equal(root.get("itemId"), itemId)
        );

        return sessionFactory.getCurrentSession().createQuery(query).getResultList();
    }

    @Override
    public Condition findRootConditionForItem(String siteId, String toolId, String itemId) {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Condition> query = cb.createQuery(Condition.class);
        Root<Condition> root = query.from(Condition.class);

        query.select(root).where(
            cb.equal(root.get("siteId"), siteId),
            cb.equal(root.get("toolId"), toolId),
            cb.equal(root.get("itemId"), itemId),
            cb.equal(root.get("type"), ConditionType.ROOT)
        );

        return sessionFactory.getCurrentSession().createQuery(query).uniqueResult();
    }
}
