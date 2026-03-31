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
import java.util.Optional;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.sakaiproject.videotraining.api.model.VideoProviderType;
import org.sakaiproject.videotraining.api.model.VideoTrainingOAuthCredentials;
import org.sakaiproject.videotraining.api.repository.VideoTrainingOAuthCredentialsRepository;

public class VideoTrainingOAuthCredentialsRepositoryImpl extends SpringCrudRepositoryImpl<VideoTrainingOAuthCredentials, String>
        implements VideoTrainingOAuthCredentialsRepository {

    @Override
    public Optional<VideoTrainingOAuthCredentials> findByProviderType(VideoProviderType providerType) {
        if (providerType == null) {
            return Optional.empty();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingOAuthCredentials> query = cb.createQuery(VideoTrainingOAuthCredentials.class);
        Root<VideoTrainingOAuthCredentials> root = query.from(VideoTrainingOAuthCredentials.class);

        query.select(root).where(cb.equal(root.get("providerType"), providerType));

        List<VideoTrainingOAuthCredentials> results = sessionFactory.getCurrentSession().createQuery(query).getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<VideoTrainingOAuthCredentials> findAllByOrderByProviderTypeAsc() {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingOAuthCredentials> query = cb.createQuery(VideoTrainingOAuthCredentials.class);
        Root<VideoTrainingOAuthCredentials> root = query.from(VideoTrainingOAuthCredentials.class);

        query.select(root).orderBy(cb.asc(root.get("providerType")));
        TypedQuery<VideoTrainingOAuthCredentials> typedQuery = sessionFactory.getCurrentSession().createQuery(query);
        List<VideoTrainingOAuthCredentials> results = typedQuery.getResultList();
        return results == null ? Collections.emptyList() : results;
    }
}
