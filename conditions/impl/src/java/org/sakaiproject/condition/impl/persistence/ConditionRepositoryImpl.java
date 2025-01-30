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

import org.hibernate.criterion.Restrictions;
import org.sakaiproject.condition.api.model.Condition;
import org.sakaiproject.condition.api.model.ConditionType;
import org.sakaiproject.condition.api.persistence.ConditionRepository;
import org.sakaiproject.serialization.BasicSerializableRepository;

public class ConditionRepositoryImpl extends BasicSerializableRepository<Condition, String> implements ConditionRepository {


    @Override
    public Condition findConditionForId(String conditionId) {
        return (Condition) startCriteriaQuery()
                .add(Restrictions.eq("id", conditionId))
                .uniqueResult();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Condition> findConditionsForSite(String siteId) {
        return startCriteriaQuery()
                .add(Restrictions.eq("siteId", siteId))
                .list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Condition> findConditionsForItem(String siteId, String toolId, String itemId) {
        return startCriteriaQuery()
                .add(Restrictions.eq("siteId", siteId))
                .add(Restrictions.eq("toolId", toolId))
                .add(Restrictions.eq("itemId", itemId))
                .list();
    }

    @Override
    public Condition findRootConditionForItem(String siteId, String toolId, String itemId) {
        return (Condition) startCriteriaQuery()
                .add(Restrictions.eq("siteId", siteId))
                .add(Restrictions.eq("toolId", toolId))
                .add(Restrictions.eq("itemId", itemId))
                .add(Restrictions.eq("type", ConditionType.ROOT))
                .uniqueResult();
    }
}
