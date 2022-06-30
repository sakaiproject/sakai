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
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.sakaiproject.conversations.api.model.ConversationsComment;
import org.sakaiproject.conversations.api.repository.ConversationsCommentRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class ConversationsCommentRepositoryImpl extends SpringCrudRepositoryImpl<ConversationsComment, String>  implements ConversationsCommentRepository {

    @Transactional(readOnly = true)
    public List<ConversationsComment> findByPostId(String postId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<ConversationsComment> query = cb.createQuery(ConversationsComment.class);
        Root<ConversationsComment> comment = query.from(ConversationsComment.class);
        query.where(cb.equal(comment.get("postId"), postId))
            .orderBy(cb.desc(comment.get("metadata").get("created")));

        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<ConversationsComment> findBySiteId(String siteId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<ConversationsComment> query = cb.createQuery(ConversationsComment.class);
        Root<ConversationsComment> comment = query.from(ConversationsComment.class);
        query.where(cb.equal(comment.get("siteId"), siteId));

        return session.createQuery(query).list();
    }

    @Transactional
    public Integer deleteByPostId(String postId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<ConversationsComment> delete = cb.createCriteriaDelete(ConversationsComment.class);
        Root<ConversationsComment> comment = delete.from(ConversationsComment.class);
        delete.where(cb.equal(comment.get("postId"), postId));

        return session.createQuery(delete).executeUpdate();
    }

    @Transactional
    public Integer lockByPostId(String postId, Boolean locked) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaUpdate<ConversationsComment> update = cb.createCriteriaUpdate(ConversationsComment.class);
        Root<ConversationsComment> comment = update.from(ConversationsComment.class);
        update.set("locked", locked).where(cb.equal(comment.get("postId"), postId));
        return session.createQuery(update).executeUpdate();
    }

    @Transactional
    public Integer lockBySiteId(String siteId, Boolean locked) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaUpdate<ConversationsComment> update = cb.createCriteriaUpdate(ConversationsComment.class);
        Root<ConversationsComment> comment = update.from(ConversationsComment.class);
        update.set("locked", locked).where(cb.equal(comment.get("siteId"), siteId));
        return session.createQuery(update).executeUpdate();
    }

    @Transactional
    public Integer deleteByTopicId(String topicId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<ConversationsComment> delete = cb.createCriteriaDelete(ConversationsComment.class);
        Root<ConversationsComment> comment = delete.from(ConversationsComment.class);
        delete.where(cb.equal(comment.get("topicId"), topicId));

        return session.createQuery(delete).executeUpdate();
    }
}
