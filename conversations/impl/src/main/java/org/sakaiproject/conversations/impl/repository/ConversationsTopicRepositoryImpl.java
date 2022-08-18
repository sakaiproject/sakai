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
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import org.sakaiproject.conversations.api.model.Metadata;
import org.sakaiproject.conversations.api.model.ConversationsTopic;
import org.sakaiproject.conversations.api.repository.ConversationsTopicRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class ConversationsTopicRepositoryImpl extends SpringCrudRepositoryImpl<ConversationsTopic, String>  implements ConversationsTopicRepository {

    @Transactional(readOnly = true)
    public List<ConversationsTopic> findBySiteId(String siteId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<ConversationsTopic> query = cb.createQuery(ConversationsTopic.class);
        Root<ConversationsTopic> topic = query.from(ConversationsTopic.class);
        Join<ConversationsTopic, Metadata> metadata = topic.join("metadata");
        query.where(cb.equal(topic.get("siteId"), siteId)).orderBy(cb.desc(metadata.get("created")));

        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<ConversationsTopic> findByTags_Id(Long tagId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<ConversationsTopic> query = cb.createQuery(ConversationsTopic.class);
        Root<ConversationsTopic> topic = query.from(ConversationsTopic.class);
        query.where(cb.isMember(tagId, topic.get("tagIds")));

        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public Long countBySiteIdAndMetadata_Creator_Id(String siteId, String creatorId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ConversationsTopic> topic = query.from(ConversationsTopic.class);
        query.select(cb.count(topic)).where(cb.and(cb.equal(topic.get("siteId"), siteId)),
                                                    cb.equal(topic.get("metadata").get("creatorId"), creatorId));

        return session.createQuery(query).getSingleResult();
    }

    @Transactional
    public Integer lockBySiteId(String siteId, Boolean locked) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaUpdate<ConversationsTopic> update = cb.createCriteriaUpdate(ConversationsTopic.class);
        Root<ConversationsTopic> topic = update.from(ConversationsTopic.class);
        update.set("locked", locked).where(cb.equal(topic.get("siteId"), siteId));
        return session.createQuery(update).executeUpdate();
    }
}
