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
import org.hibernate.exception.ConstraintViolationException;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.CriteriaQuery;
//import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import org.sakaiproject.conversations.api.model.ConversationsTopic;
import org.sakaiproject.conversations.api.model.TopicStatus;
import org.sakaiproject.conversations.api.repository.TopicStatusRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TopicStatusRepositoryImpl extends SpringCrudRepositoryImpl<TopicStatus, Long>  implements TopicStatusRepository {

    @Transactional
    public Optional<TopicStatus> findByTopicIdAndUserId(String topicId, String userId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<TopicStatus> query = cb.createQuery(TopicStatus.class);
        Root<TopicStatus> status = query.from(TopicStatus.class);
        //Join<TopicStatus, ConversationsTopic> topicJoin = status.join("topic");
        query.where(cb.and(cb.equal(status.get("topic").get("id"), topicId),
                            cb.equal(status.get("userId"), userId)));

        return session.createQuery(query).uniqueResultOptional();
    }
    
    @Override
    @Transactional
    public TopicStatus saveTopicStatus(String topicId, String userId, boolean viewed) {
        Session session = sessionFactory.getCurrentSession();
        
        try {
            // Try to find the existing record first
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<TopicStatus> query = cb.createQuery(TopicStatus.class);
            Root<TopicStatus> root = query.from(TopicStatus.class);
            query.where(
                cb.and(
                    cb.equal(root.get("topic").get("id"), topicId),
                    cb.equal(root.get("userId"), userId)
                )
            );
            
            TopicStatus status = session.createQuery(query).uniqueResultOptional().orElse(null);
            
            if (status != null) {
                // Update existing record
                status.setViewed(viewed);
                return (TopicStatus) session.merge(status);
            } else {
                // Get the topic entity
                CriteriaQuery<ConversationsTopic> topicQuery = cb.createQuery(ConversationsTopic.class);
                Root<ConversationsTopic> topicRoot = topicQuery.from(ConversationsTopic.class);
                topicQuery.where(cb.equal(topicRoot.get("id"), topicId));
                ConversationsTopic topic = session.createQuery(topicQuery).uniqueResult();
                
                if (topic == null) {
                    throw new IllegalArgumentException("No topic found for id: " + topicId);
                }
                
                // Create and save new status
                TopicStatus newStatus = new TopicStatus(topic, userId);
                newStatus.setViewed(viewed);
                try {
                    session.persist(newStatus);
                    return newStatus;
                } catch (ConstraintViolationException | DataIntegrityViolationException e) {
                    // Another thread created the record first, so let's get it and update it
                    log.debug("Caught concurrent creation exception, fetching the existing record: {}", e.getMessage());
                    session.clear(); // Clear session to avoid stale data
                    
                    // Fetch the record that was just created by another thread
                    status = session.createQuery(query).uniqueResultOptional()
                        .orElseThrow(() -> new IllegalStateException("Expected to find TopicStatus after constraint violation"));
                    
                    status.setViewed(viewed);
                    return (TopicStatus) session.merge(status);
                }
            }
        } catch (Exception e) {
            log.warn("Error in saveTopicStatus: {}", e.toString());
            throw e;
        }
    }

    @Transactional
    public Integer deleteByTopicId(String topicId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<TopicStatus> delete = cb.createCriteriaDelete(TopicStatus.class);
        Root<TopicStatus> status = delete.from(TopicStatus.class);
        delete.where(cb.equal(status.get("topic").get("id"), topicId));

        return session.createQuery(delete).executeUpdate();
    }

    @Transactional(readOnly = true)
    public List<Object[]> countBySiteIdAndViewed(String siteId, Boolean viewed) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<TopicStatus> status = query.from(TopicStatus.class);
        //Join<TopicStatus, ConversationsTopic> topicJoin = status.join("topic");
        //query.multiselect(status.get("userId"), cb.count(topicJoin.get("id")))
        query.multiselect(status.get("userId"), cb.count(status.get("topic")))
            //.where(cb.and(cb.equal(topicJoin.get("siteId"), siteId),
            .where(cb.and(cb.equal(status.get("topic").get("siteId"), siteId),
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
        update.set(status.get("viewed"), viewed).where(cb.equal(status.get("topic").get("id"), topicId));

        return session.createQuery(update).executeUpdate();
    }

    @Transactional
    public Integer setViewedByTopicIdAndUserId(String topicId, String userId, Boolean viewed) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaUpdate<TopicStatus> update = cb.createCriteriaUpdate(TopicStatus.class);
        Root<TopicStatus> status = update.from(TopicStatus.class);
        update.set(status.get("viewed"), viewed).where(cb.and(cb.equal(status.get("topic").get("id"), topicId)), cb.equal(status.get("userId"), userId));

        return session.createQuery(update).executeUpdate();
    }
}
