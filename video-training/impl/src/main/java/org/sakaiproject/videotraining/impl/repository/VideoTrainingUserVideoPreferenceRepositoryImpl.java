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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.sakaiproject.videotraining.api.model.VideoTrainingUserVideoPreference;
import org.sakaiproject.videotraining.api.repository.VideoTrainingUserVideoPreferenceRepository;

public class VideoTrainingUserVideoPreferenceRepositoryImpl
        extends SpringCrudRepositoryImpl<VideoTrainingUserVideoPreference, String>
        implements VideoTrainingUserVideoPreferenceRepository {

    @Override
    public Optional<VideoTrainingUserVideoPreference> findBySiteIdAndUserIdAndVideoId(String siteId, String userId, String videoId) {

        if (siteId == null || userId == null || videoId == null) {
            return Optional.empty();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingUserVideoPreference> query = cb.createQuery(VideoTrainingUserVideoPreference.class);
        Root<VideoTrainingUserVideoPreference> root = query.from(VideoTrainingUserVideoPreference.class);

        query.select(root).where(cb.and(
                cb.equal(root.get("siteId"), siteId),
                cb.equal(root.get("userId"), userId),
                cb.equal(root.get("videoId"), videoId)));

        List<VideoTrainingUserVideoPreference> results = sessionFactory.getCurrentSession().createQuery(query)
                .setMaxResults(1)
                .getResultList();
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results.get(0));
    }

    @Override
    public List<VideoTrainingUserVideoPreference> findBySiteIdAndUserIdAndVideoIds(String siteId, String userId, List<String> videoIds) {

        if (siteId == null || userId == null || videoIds == null || videoIds.isEmpty()) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingUserVideoPreference> query = cb.createQuery(VideoTrainingUserVideoPreference.class);
        Root<VideoTrainingUserVideoPreference> root = query.from(VideoTrainingUserVideoPreference.class);

        query.select(root).where(cb.and(
                cb.equal(root.get("siteId"), siteId),
                cb.equal(root.get("userId"), userId),
                root.get("videoId").in(videoIds)));

        return sessionFactory.getCurrentSession().createQuery(query).getResultList();
    }

    @Override
    public List<VideoTrainingUserVideoPreference> findBySiteIdAndUserIdAndFavoriteTrueOrderByModifiedOnDesc(String siteId, String userId) {

        if (siteId == null || userId == null) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingUserVideoPreference> query = cb.createQuery(VideoTrainingUserVideoPreference.class);
        Root<VideoTrainingUserVideoPreference> root = query.from(VideoTrainingUserVideoPreference.class);

        query.select(root)
                .where(cb.and(
                        cb.equal(root.get("siteId"), siteId),
                        cb.equal(root.get("userId"), userId),
                        cb.isTrue(root.get("favorite"))))
                .orderBy(cb.desc(root.get("modifiedOn")));

        return sessionFactory.getCurrentSession().createQuery(query).getResultList();
    }

    @Override
    public List<VideoTrainingUserVideoPreference> findBySiteIdAndUserIdAndWatchLaterTrueOrderByModifiedOnDesc(String siteId, String userId) {

        if (siteId == null || userId == null) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingUserVideoPreference> query = cb.createQuery(VideoTrainingUserVideoPreference.class);
        Root<VideoTrainingUserVideoPreference> root = query.from(VideoTrainingUserVideoPreference.class);

        query.select(root)
                .where(cb.and(
                        cb.equal(root.get("siteId"), siteId),
                        cb.equal(root.get("userId"), userId),
                        cb.isTrue(root.get("watchLater"))))
                .orderBy(cb.desc(root.get("modifiedOn")));

        return sessionFactory.getCurrentSession().createQuery(query).getResultList();
    }

    @Override
    public void deleteByVideoId(String videoId) {
        if (videoId == null) {
            return;
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaDelete<VideoTrainingUserVideoPreference> delete = cb
                .createCriteriaDelete(VideoTrainingUserVideoPreference.class);
        Root<VideoTrainingUserVideoPreference> root = delete.from(VideoTrainingUserVideoPreference.class);
        delete.where(cb.equal(root.get("videoId"), videoId));

        sessionFactory.getCurrentSession().createQuery(delete).executeUpdate();
    }
}
