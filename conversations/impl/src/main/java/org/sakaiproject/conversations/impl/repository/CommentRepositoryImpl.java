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

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import org.sakaiproject.conversations.api.model.Comment;
import org.sakaiproject.conversations.api.repository.CommentRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class CommentRepositoryImpl extends SpringCrudRepositoryImpl<Comment, String>  implements CommentRepository {

    @Transactional
    public List<Comment> findByPost_Id(String postId) {

        return (List<Comment>) sessionFactory.getCurrentSession().createCriteria(Comment.class)
            .add(Restrictions.eq("post.id", postId))
            .addOrder(Order.desc("metadata.created"))
            .list();
    }

    @Transactional
    public Integer deleteByPost_Id(String postId) {

        return sessionFactory.getCurrentSession()
            .createQuery("delete from Comment where post.id = :postId")
            .setString("postId", postId).executeUpdate();
    }

    @Transactional
    public Integer lockByPost_Id(String postId, Boolean locked) {

        return sessionFactory.getCurrentSession()
            .createQuery("update Comment set locked = :locked where post.id = :postId")
            .setBoolean("locked", locked).setString("postId", postId).executeUpdate();
    }

    @Transactional
    public Integer lockBySiteId(String siteId, Boolean locked) {

        return sessionFactory.getCurrentSession()
            .createQuery("update Comment set locked = :locked where siteId = :siteId")
            .setBoolean("locked", locked).setString("siteId", siteId).executeUpdate();
    }

    @Transactional
    public Integer deleteByPost_Topic_Id(String topicId) {

        /*
        return sessionFactory.getCurrentSession()
            .createQuery("delete from Comment where post.topic.id = :topicId")
            .setString("topicId", topicId).executeUpdate();
            */
        return sessionFactory.getCurrentSession()
            .createQuery("delete from Comment c join c.post p where p.topic = :topicId")
            .setString("topicId", topicId).executeUpdate();
    }
}
