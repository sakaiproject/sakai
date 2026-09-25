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

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.sakaiproject.videotraining.api.model.VideoTrainingCategory;
import org.sakaiproject.videotraining.api.repository.VideoTrainingCategoryRepository;

public class VideoTrainingCategoryRepositoryImpl extends SpringCrudRepositoryImpl<VideoTrainingCategory, String>
        implements VideoTrainingCategoryRepository {

    @Override
    public List<VideoTrainingCategory> findBySiteIdOrderBySortOrderAscNameAsc(String siteId, int offset, int limit) {
        if (siteId == null) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingCategory> query = cb.createQuery(VideoTrainingCategory.class);
        Root<VideoTrainingCategory> root = query.from(VideoTrainingCategory.class);

        query.select(root)
                .where(cb.equal(root.get("siteId"), siteId))
                .orderBy(cb.asc(root.get("sortOrder")), cb.asc(root.get("name")));

        TypedQuery<VideoTrainingCategory> typedQuery =
            sessionFactory.getCurrentSession().createQuery(query);

        if (offset > 0) {
            typedQuery.setFirstResult(offset);
        }

        if (limit > 0) {
            typedQuery.setMaxResults(limit);
        }

        return typedQuery.getResultList();
    }

    @Override
    public List<VideoTrainingCategory> findBySiteIdStartingWithOrderBySortOrderAscNameAsc(String siteIdPrefix, int offset, int limit) {
        if (siteIdPrefix == null) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingCategory> query = cb.createQuery(VideoTrainingCategory.class);
        Root<VideoTrainingCategory> root = query.from(VideoTrainingCategory.class);

        query.select(root)
                .where(cb.like(root.get("siteId"), siteIdPrefix + "%"))
                .orderBy(cb.asc(root.get("sortOrder")), cb.asc(root.get("name")));

        TypedQuery<VideoTrainingCategory> typedQuery = sessionFactory.getCurrentSession().createQuery(query);

        if (offset > 0) {
            typedQuery.setFirstResult(offset);
        }

        if (limit > 0) {
            typedQuery.setMaxResults(limit);
        }

        return typedQuery.getResultList();
    }

    @Override
    public long countBySiteId(String siteId) {
        if (siteId == null) {
            return 0;
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<VideoTrainingCategory> root = query.from(VideoTrainingCategory.class);

        query.select(cb.count(root))
                .where(cb.equal(root.get("siteId"), siteId));

        return sessionFactory.getCurrentSession()
                .createQuery(query)
                .getSingleResult();
    }
}
