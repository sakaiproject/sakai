/*
 * Copyright (c) 2003-2023 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.facade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAnswer;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StatisticsFacadeQueries extends HibernateDaoSupport implements StatisticsFacadeQueriesAPI {


    private static int IN_QUERY_BATCH_SIZE = 50;


    public Map<Long, Set<PublishedAnswer>> getPublishedAnswerMap(@NonNull Collection<Long> publishedItemIds) {
        if (publishedItemIds.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            HibernateCallback<Map<Long, Set<PublishedAnswer>>> hibernateCallback = session -> {
                CriteriaBuilder cb = session.getCriteriaBuilder();
                Stream<PublishedAnswer> resultStream = Stream.empty();

                for (List<Long> batchItemIds : batches(publishedItemIds, IN_QUERY_BATCH_SIZE)) {
                    CriteriaQuery<PublishedAnswer> criteriaQuery = cb.createQuery(PublishedAnswer.class);
                    Root<PublishedAnswer> root = criteriaQuery.from(PublishedAnswer.class);

                    // Filter by itemId of published item
                    Predicate itemIdPredicate = root.<PublishedItemData>get("item").get("itemId").in(batchItemIds);
                    criteriaQuery.where(itemIdPredicate);

                    resultStream = Stream.concat(resultStream, session.createQuery(criteriaQuery).getResultStream());
                }

                Function<PublishedAnswer, Long> keyMapper = publishedAnswer -> publishedAnswer.getItem().getItemId();

                return resultStream.collect(Collectors.groupingBy(keyMapper, Collectors.toSet()));
            };

            return getHibernateTemplate().execute(hibernateCallback);
        } catch (Exception e) {
            log.warn("Could not get publishedAnswerMap due to {}: {}", e.toString(), ExceptionUtils.getStackTrace(e));
            return Collections.emptyMap();
        }
    }

    public Map<Long, Set<ItemGradingData>> getGradingDataMap(Collection<Long> publishedItemIds) {
        if (publishedItemIds.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            HibernateCallback<Map<Long, Set<ItemGradingData>>> hibernateCallback = session -> {
                CriteriaBuilder cb = session.getCriteriaBuilder();
                Stream<ItemGradingData> resultStream = Stream.empty();

                for (List<Long> batchItemIds : batches(publishedItemIds, IN_QUERY_BATCH_SIZE)) {
                    CriteriaQuery<ItemGradingData> criteriaQuery = cb.createQuery(ItemGradingData.class);
                    Root<ItemGradingData> root = criteriaQuery.from(ItemGradingData.class);

                    // Filter by publishedItemId
                    Predicate itemIdPredicate = root.get("publishedItemId").in(batchItemIds);
                    criteriaQuery.where(itemIdPredicate);

                    resultStream = Stream.concat(resultStream, session.createQuery(criteriaQuery).getResultStream());
                }

                return resultStream.collect(Collectors.groupingBy(ItemGradingData::getPublishedItemId, Collectors.toSet()));
            };

            return getHibernateTemplate().execute(hibernateCallback);
        } catch (Exception e) {
            log.warn("Could not get gradingDataMap due to {}: {}", e.toString(), ExceptionUtils.getStackTrace(e));
            return Collections.emptyMap();
        }
    }

    public List<PublishedItemData> getItemDataByHashes(@NonNull Collection<String> hashes) {
        if (hashes.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            HibernateCallback<List<PublishedItemData>> hibernateCallback = session -> {
                CriteriaBuilder cb = session.getCriteriaBuilder();
                List<PublishedItemData> resultList = new ArrayList<>();

                for (List<String> batchHashes : batches(hashes, IN_QUERY_BATCH_SIZE)) {
                    CriteriaQuery<PublishedItemData> criteriaQuery = cb.createQuery(PublishedItemData.class);
                    Root<PublishedItemData> root = criteriaQuery.from(PublishedItemData.class);

                    // Filter by hash
                    Predicate hashPredicate = root.<PublishedItemData>get("hash").in(batchHashes);
                    criteriaQuery.where(hashPredicate);

                    resultList.addAll(session.createQuery(criteriaQuery).getResultList());
                }

                return resultList;
            };

            return getHibernateTemplate().execute(hibernateCallback);
        } catch (Exception e) {
            log.warn("Could not get items due to {}: {}", e.toString(), ExceptionUtils.getStackTrace(e));
            return Collections.emptyList();
        }
    }

    private <T> List<List<T>> batches(Collection<T> items, int batchSize) {
        List<T> itemList;
        if (items instanceof List) {
            itemList = (List<T>) items;
        } else {
            itemList = new ArrayList<T>(items);
        }

        return ListUtils.partition(itemList, batchSize);
    }
}
