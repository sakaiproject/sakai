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
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.sakaiproject.conversations.api.Reaction;
import org.sakaiproject.conversations.api.model.TopicReactionTotal;
import org.sakaiproject.conversations.api.repository.TopicReactionTotalRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class TopicReactionTotalRepositoryImpl extends SpringCrudRepositoryImpl<TopicReactionTotal, Long> implements TopicReactionTotalRepository {

    @Transactional
    public List<TopicReactionTotal> findByTopicId(String topicId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<TopicReactionTotal> query = cb.createQuery(TopicReactionTotal.class);
        Root<TopicReactionTotal> total = query.from(TopicReactionTotal.class);
        query.where(cb.equal(total.get("topicId"), topicId));

        return session.createQuery(query).list();
    }

    @Transactional
    public Optional<TopicReactionTotal> findByTopicIdAndReaction(String topicId, Reaction reaction) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<TopicReactionTotal> query = cb.createQuery(TopicReactionTotal.class);
        Root<TopicReactionTotal> total = query.from(TopicReactionTotal.class);
        query.where(cb.and(cb.equal(total.get("topicId"), topicId),
                            cb.equal(total.get("reaction"), reaction)));

        return session.createQuery(query).uniqueResultOptional();
    }


    @Transactional
    public Integer deleteByTopicId(String topicId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<TopicReactionTotal> delete = cb.createCriteriaDelete(TopicReactionTotal.class);
        Root<TopicReactionTotal> total = delete.from(TopicReactionTotal.class);
        delete.where(cb.equal(total.get("topicId"), topicId));

        return session.createQuery(delete).executeUpdate();
    }
}
