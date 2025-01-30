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
package org.sakaiproject.cardgame.impl.persistence;

import java.util.List;
import java.util.Optional;

import org.hibernate.criterion.Restrictions;
import org.sakaiproject.cardgame.api.model.CardGameStatItem;
import org.sakaiproject.cardgame.api.persistence.StatItemRepository;
import org.sakaiproject.serialization.BasicSerializableRepository;

public class StatItemRepositoryImpl extends BasicSerializableRepository<CardGameStatItem, String> implements StatItemRepository {


    @SuppressWarnings("unchecked")
    public List<CardGameStatItem> findByPlayerId(String playerId) {
        return startCriteriaQuery().add(Restrictions.eq("playerId", playerId)).list();
    }

    public Optional<CardGameStatItem> findByPlayerIdAndUserId(String playerId, String userId) {
        return Optional.ofNullable((CardGameStatItem) startCriteriaQuery()
                .add(Restrictions.eq("playerId", playerId))
                .add(Restrictions.eq("userId", userId))
                .uniqueResult());
    }

    @SuppressWarnings("unchecked")
    public void deleteByPlayerId(String playerId) {
        delete(startCriteriaQuery().add(Restrictions.eq("playerId", playerId)).list());
    }
}
