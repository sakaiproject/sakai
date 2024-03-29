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

import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.sakaiproject.conversations.api.model.TopicReaction;
import org.sakaiproject.conversations.api.repository.TopicReactionRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class TopicReactionRepositoryImpl extends SpringCrudRepositoryImpl<TopicReaction, Long>  implements TopicReactionRepository {

    @Transactional
    public List<TopicReaction> findByTopicIdAndUserId(String topicId, String userId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<TopicReaction> query = cb.createQuery(TopicReaction.class);
        Root<TopicReaction> reaction = query.from(TopicReaction.class);
        query.where(cb.and(cb.equal(reaction.get("userId"), userId),
                            cb.equal(reaction.get("topic").get("id"), topicId)));

        return session.createQuery(query).list();
    }

    @Transactional
    public Integer deleteByTopicId(String topicId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<TopicReaction> delete = cb.createCriteriaDelete(TopicReaction.class);
        Root<TopicReaction> reaction = delete.from(TopicReaction.class);
        delete.where(cb.equal(reaction.get("topic").get("id"), topicId));

        return session.createQuery(delete).executeUpdate();
    }
}
