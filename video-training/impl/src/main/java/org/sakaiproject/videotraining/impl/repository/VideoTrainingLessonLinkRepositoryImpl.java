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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.sakaiproject.videotraining.api.model.VideoTrainingLessonLink;
import org.sakaiproject.videotraining.api.repository.VideoTrainingLessonLinkRepository;

public class VideoTrainingLessonLinkRepositoryImpl extends SpringCrudRepositoryImpl<VideoTrainingLessonLink, String>
        implements VideoTrainingLessonLinkRepository {

    @Override
    public List<VideoTrainingLessonLink> findByVideoIdOrderByCreatedOnDesc(String videoId) {
        if (videoId == null) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingLessonLink> query = cb.createQuery(VideoTrainingLessonLink.class);
        Root<VideoTrainingLessonLink> root = query.from(VideoTrainingLessonLink.class);

        query.select(root)
                .where(cb.equal(root.get("videoId"), videoId))
                .orderBy(cb.desc(root.get("createdOn")));

        return sessionFactory.getCurrentSession().createQuery(query).getResultList();
    }

    @Override
    public List<VideoTrainingLessonLink> findBySiteIdAndLessonPageId(String siteId, String lessonPageId) {
        if (siteId == null || lessonPageId == null) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VideoTrainingLessonLink> query = cb.createQuery(VideoTrainingLessonLink.class);
        Root<VideoTrainingLessonLink> root = query.from(VideoTrainingLessonLink.class);

        query.select(root)
                .where(cb.and(cb.equal(root.get("siteId"), siteId), cb.equal(root.get("lessonPageId"), lessonPageId)))
                .orderBy(cb.desc(root.get("createdOn")));

        return sessionFactory.getCurrentSession().createQuery(query).getResultList();
    }

    @Override
    public void deleteByVideoId(String videoId) {
        if (videoId == null) {
            return;
        }

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaDelete<VideoTrainingLessonLink> delete = cb.createCriteriaDelete(VideoTrainingLessonLink.class);
        Root<VideoTrainingLessonLink> root = delete.from(VideoTrainingLessonLink.class);
        delete.where(cb.equal(root.get("videoId"), videoId));

        sessionFactory.getCurrentSession().createQuery(delete).executeUpdate();
    }
}
