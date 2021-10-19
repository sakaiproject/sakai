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

import org.hibernate.criterion.Restrictions;

import org.sakaiproject.conversations.api.Reaction;
import org.sakaiproject.conversations.api.model.TopicReactionTotal;
import org.sakaiproject.conversations.api.repository.TopicReactionTotalRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class TopicReactionTotalRepositoryImpl extends SpringCrudRepositoryImpl<TopicReactionTotal, Long> implements TopicReactionTotalRepository {

    @Transactional
    public List<TopicReactionTotal> findByTopic_Id(String topicId) {

        return (List<TopicReactionTotal>) sessionFactory.getCurrentSession().createCriteria(TopicReactionTotal.class)
            .add(Restrictions.eq("topic.id", topicId))
            .list();
    }

    @Transactional
    public Optional<TopicReactionTotal> findByTopic_IdAndReaction(String topicId, Reaction reaction) {

        return Optional.ofNullable((TopicReactionTotal) sessionFactory.getCurrentSession().createCriteria(TopicReactionTotal.class)
            .add(Restrictions.eq("topic.id", topicId))
            .add(Restrictions.eq("reaction", reaction))
            .uniqueResult());
    }


    @Transactional
    public Integer deleteByTopic_Id(String topicId) {

        return sessionFactory.getCurrentSession()
            .createQuery("delete from TopicReactionTotal where topic.id = :topicId")
            .setString("topicId", topicId).executeUpdate();
    }
}
