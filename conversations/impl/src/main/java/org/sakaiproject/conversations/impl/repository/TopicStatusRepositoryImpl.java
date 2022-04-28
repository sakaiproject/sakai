/*
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.conversations.impl.repository;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.sakaiproject.conversations.api.model.TopicStatus;
import org.sakaiproject.conversations.api.repository.TopicStatusRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class TopicStatusRepositoryImpl extends SpringCrudRepositoryImpl<TopicStatus, Long>  implements TopicStatusRepository {

    @Transactional
    public Optional<TopicStatus> findByTopicIdAndUserId(String topicId, String userId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<TopicStatus> query = cb.createQuery(TopicStatus.class);
        Root<TopicStatus> status = query.from(TopicStatus.class);
        query.where(cb.and(cb.equal(status.get("topicId"), topicId),
                            cb.equal(status.get("userId"), userId)));

        return session.createQuery(query).uniqueResultOptional();
    }

    @Transactional
    public Integer deleteByTopicId(String topicId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<TopicStatus> delete = cb.createCriteriaDelete(TopicStatus.class);
        Root<TopicStatus> status = delete.from(TopicStatus.class);
        delete.where(cb.equal(status.get("topicId"), topicId));

        return session.createQuery(delete).executeUpdate();
    }

    @Transactional(readOnly = true)
    public List<Object[]> countBySiteIdAndViewed(String siteId, Boolean viewed) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<TopicStatus> status = query.from(TopicStatus.class);
        query.multiselect(status.get("userId"), cb.count(status.get("topicId")))
            .where(cb.and(cb.equal(status.get("siteId"), siteId),
                            cb.equal(status.get("viewed"), viewed)));
        query.groupBy(status.get("userId"));

        return session.createQuery(query).list();
    }

    @Transactional
    public Integer setViewedByTopicId(String topicId, Boolean viewed) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaUpdate<TopicStatus> update = cb.createCriteriaUpdate(TopicStatus.class);
        Root<TopicStatus> status = update.from(TopicStatus.class);
        update.set(status.get("viewed"), viewed).where(cb.equal(status.get("topicId"), topicId));

        return session.createQuery(update).executeUpdate();
    }
}
