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

import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import org.sakaiproject.conversations.api.model.TopicStatus;
import org.sakaiproject.conversations.api.repository.TopicStatusRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class TopicStatusRepositoryImpl extends SpringCrudRepositoryImpl<TopicStatus, Long>  implements TopicStatusRepository {

    @Transactional
    public Optional<TopicStatus> findByTopicIdAndUserId(String topicId, String userId) {

        return Optional.ofNullable((TopicStatus) sessionFactory.getCurrentSession().createCriteria(TopicStatus.class)
            .add(Restrictions.eq("topicId", topicId))
            .add(Restrictions.eq("userId", userId))
            .uniqueResult());
    }

    @Transactional
    public Integer deleteByTopicId(String topicId) {

        return sessionFactory.getCurrentSession()
            .createQuery("delete from TopicStatus where topicId = :topicId")
            .setString("topicId", topicId).executeUpdate();
    }

    @Transactional(readOnly = true)
    public List<Object[]> countBySiteIdAndViewed(String siteId, Boolean viewed) {

        return (List<Object[]>) sessionFactory.getCurrentSession()
            .createQuery("select userId, count(topicId) as total from TopicStatus as ts where siteId = :siteId and ts.viewed = :viewed group by userId")
            .setString("siteId", siteId).setBoolean("viewed", viewed).list();
    }
}
