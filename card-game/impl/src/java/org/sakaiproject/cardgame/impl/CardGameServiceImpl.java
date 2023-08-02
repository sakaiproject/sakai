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
package org.sakaiproject.cardgame.impl;

import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.cardgame.api.CardGameService;
import org.sakaiproject.cardgame.api.model.CardGameStatItem;
import org.sakaiproject.cardgame.api.persistence.StatItemRepository;
import org.springframework.transaction.annotation.Transactional;

import lombok.Setter;

@Slf4j
public class CardGameServiceImpl implements CardGameService {


    @Setter
    private StatItemRepository statItemRepository;


    public void init() {
        log.info("Initializing Card Game Service");
    }

    public List<CardGameStatItem> findStatItemByPlayerId(String playerId) {
        return statItemRepository.findByPlayerId(playerId);
    }

    @Transactional
    public void addHit(String playerId, String userId) {
        Optional<CardGameStatItem> optStatItem = statItemRepository.findByPlayerIdAndUserId(playerId, userId);

        if (optStatItem.isPresent()) {
            CardGameStatItem statItem = optStatItem.get();
            statItem.setHits(statItem.getHits() != null
                    ? statItem.getHits() + 1
                    : CardGameStatItem.HITS_DEFAULT + 1);
        } else {
            statItemRepository.save(CardGameStatItem.builderWithDefaults()
                .playerId(playerId)
                .userId(userId)
                .hits(CardGameStatItem.HITS_DEFAULT + 1)
                .build());
        }
    }

    @Transactional
    public void addMiss(String playerId, String userId) {
        Optional<CardGameStatItem> optStatItem = statItemRepository.findByPlayerIdAndUserId(playerId, userId);

        if (optStatItem.isPresent()) {
            CardGameStatItem statItem = optStatItem.get();
            statItem.setMisses(statItem.getMisses() != null
                    ? statItem.getMisses() + 1
                    : CardGameStatItem.MISSES_DEFAULT + 1);
        } else {
            statItemRepository.save(CardGameStatItem.builderWithDefaults()
                .playerId(playerId)
                .userId(userId)
                .misses(CardGameStatItem.MISSES_DEFAULT + 1)
                .build());
        }
    }

    @Transactional
    public void resetGameForPlayer(String playerId) {
        statItemRepository.deleteByPlayerId(playerId);
    }

    @Transactional
    public void markUserAsLearnedForPlayer(String playerId, String userId) {
        Optional<CardGameStatItem> optStatItem = statItemRepository.findByPlayerIdAndUserId(playerId, userId);

        if (optStatItem.isPresent()) {
            optStatItem.get().setMarkedAsLearned(true);
        } else {
            statItemRepository.save(CardGameStatItem.builderWithDefaults()
                .playerId(playerId)
                .userId(userId)
                .markedAsLearned(true)
                .build());
        }
    }

}
