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
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;

import javax.persistence.criteria.CriteriaBuilder;

import org.sakaiproject.conversations.api.model.ConversationsPost;
import org.sakaiproject.conversations.api.repository.ConversationsPostRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class ConversationsPostRepositoryImpl extends SpringCrudRepositoryImpl<ConversationsPost, String>  implements ConversationsPostRepository {

    @Transactional(readOnly = true)
    public List<ConversationsPost> findByTopicId(String topicId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<ConversationsPost> query = cb.createQuery(ConversationsPost.class);
        query.where(cb.equal(query.from(ConversationsPost.class).get("topicId"), topicId));

        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<ConversationsPost> findByTopicIdAndParentPostIdIsNull(String topicId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<ConversationsPost> query = cb.createQuery(ConversationsPost.class);
        Root<ConversationsPost> post = query.from(ConversationsPost.class);
        query.where(cb.and(cb.equal(post.get("topicId"), topicId),
                            cb.isNull(post.get("parentPostId"))))
                                .orderBy(cb.asc(post.get("metadata").get("created")));

        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<ConversationsPost> findByTopicIdAndMetadata_Creator(String topicId, String creatorId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<ConversationsPost> query = cb.createQuery(ConversationsPost.class);
        Root<ConversationsPost> post = query.from(ConversationsPost.class);
        query.where(cb.and(cb.equal(post.get("topicId"), topicId),
                            cb.equal(post.get("metadata").get("creator"), creatorId)));

        return session.createQuery(query).list();
    }


    @Transactional(readOnly = true)
    public List<ConversationsPost> findByParentPostId(String parentPostId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<ConversationsPost> query = cb.createQuery(ConversationsPost.class);
        Root<ConversationsPost> post = query.from(ConversationsPost.class);
        query.where(cb.equal(post.get("parentPostId"), parentPostId));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<ConversationsPost> findBySiteId(String siteId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<ConversationsPost> query = cb.createQuery(ConversationsPost.class);
        Root<ConversationsPost> post = query.from(ConversationsPost.class);
        query.where(cb.equal(post.get("siteId"), siteId));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public Long countByParentPostId(String parentConversationsPostId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ConversationsPost> post = query.from(ConversationsPost.class);
        query.select(cb.count(post)).where(cb.equal(post.get("parentPostId"), parentConversationsPostId));
        return session.createQuery(query).uniqueResult();
    }

    @Transactional(readOnly = true)
    public List<ConversationsPost> findByParentThreadId(String parentThreadId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<ConversationsPost> query = cb.createQuery(ConversationsPost.class);
        Root<ConversationsPost> post = query.from(ConversationsPost.class);
        query.where(cb.equal(post.get("parentThreadId"), parentThreadId));
        return session.createQuery(query).list();
    }

    @Transactional
    public Integer deleteByTopicId(String topicId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<ConversationsPost> delete = cb.createCriteriaDelete(ConversationsPost.class);
        delete.where(cb.equal(delete.from(ConversationsPost.class).get("topicId"), topicId));

        return session.createQuery(delete).executeUpdate();
    }

    @Transactional
    public Integer lockByTopicId(Boolean locked, String topicId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaUpdate<ConversationsPost> update = cb.createCriteriaUpdate(ConversationsPost.class);
        Root<ConversationsPost> post = update.from(ConversationsPost.class);
        update.set("locked", locked).where(cb.equal(post.get("topicId"), topicId));

        return session.createQuery(update).executeUpdate();
    }

    @Transactional
    public Integer lockByParentPostId(Boolean locked, String parentConversationsPostId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaUpdate<ConversationsPost> update = cb.createCriteriaUpdate(ConversationsPost.class);
        Root<ConversationsPost> post = update.from(ConversationsPost.class);
        update.set("locked", locked).where(cb.equal(post.get("parentPostId"), parentConversationsPostId));

        return session.createQuery(update).executeUpdate();
    }

    @Transactional
    public Integer lockBySiteId(String siteId, Boolean locked) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaUpdate<ConversationsPost> update = cb.createCriteriaUpdate(ConversationsPost.class);
        Root<ConversationsPost> post = update.from(ConversationsPost.class);
        update.set("locked", locked).where(cb.equal(post.get("siteId"), siteId));

        return session.createQuery(update).executeUpdate();
    }
}
