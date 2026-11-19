/**********************************************************************************
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.videotraining.impl.repository;

import java.util.Collections;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.hibernate.SessionFactory;

import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.sakaiproject.videotraining.api.model.VideoTrainingAnalyticsEvent;
import org.sakaiproject.videotraining.api.repository.VideoTrainingAnalyticsEventRepository;

public class VideoTrainingAnalyticsEventRepositoryImpl extends SpringCrudRepositoryImpl<VideoTrainingAnalyticsEvent, Long>
        implements VideoTrainingAnalyticsEventRepository {

    @Override
    public List<VideoTrainingAnalyticsEvent> findByVideoIdOrderByEventTimeDesc(String videoId) {
        if (videoId == null) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingAnalyticsEvent> query = cb.createQuery(VideoTrainingAnalyticsEvent.class);
        Root<VideoTrainingAnalyticsEvent> root = query.from(VideoTrainingAnalyticsEvent.class);

        query.select(root)
                .where(cb.equal(root.get("videoId"), videoId))
                .orderBy(cb.desc(root.get("eventTime")));

        return sessionFactory.getCurrentSession().createQuery(query).getResultList();
    }

    @Override
    public List<VideoTrainingAnalyticsEvent> findBySiteIdAndEventType(String siteId, String eventType) {
        if (siteId == null || eventType == null) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingAnalyticsEvent> query = cb.createQuery(VideoTrainingAnalyticsEvent.class);
        Root<VideoTrainingAnalyticsEvent> root = query.from(VideoTrainingAnalyticsEvent.class);

        Predicate sitePredicate = cb.equal(root.get("siteId"), siteId);
        Predicate eventTypePredicate = cb.equal(root.get("eventType"), eventType);

        query.select(root)
                .where(cb.and(sitePredicate, eventTypePredicate))
                .orderBy(cb.desc(root.get("eventTime")));

        return sessionFactory.getCurrentSession().createQuery(query).getResultList();
    }
}
