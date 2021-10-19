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

import org.sakaiproject.conversations.api.model.Post;
import org.sakaiproject.conversations.api.repository.PostRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class PostRepositoryImpl extends SpringCrudRepositoryImpl<Post, String>  implements PostRepository {

    @Transactional
    public List<Post> findByTopic_Id(String topicId) {

        return (List<Post>) sessionFactory.getCurrentSession().createCriteria(Post.class)
            .add(Restrictions.eq("topic.id", topicId))
            .addOrder(Order.desc("metadata.created"))
            .list();
    }

    @Transactional
    public List<Post> findByParentPost_Id(String parentPostId) {

        return (List<Post>) sessionFactory.getCurrentSession().createCriteria(Post.class)
            .add(Restrictions.eq("parentPost.id", parentPostId))
            .list();
    }

    @Transactional
    public Integer deleteByTopic_Id(String topicId) {

        return sessionFactory.getCurrentSession()
            .createQuery("delete from Post where topic.id = :topicId")
            .setString("topicId", topicId).executeUpdate();
    }

    @Transactional
    public Integer lockByTopic_Id(Boolean locked, String topicId) {

        return sessionFactory.getCurrentSession()
            .createQuery("update Post set locked = :locked where topic.id = :topicId")
            .setString("topicId", topicId).setBoolean("locked", locked).executeUpdate();
    }

    @Transactional
    public Integer lockByParentPost_Id(Boolean locked, String parentPostId) {

        return sessionFactory.getCurrentSession()
            .createQuery("update Post set locked = :locked where parentPost.id = :parentPostId")
            .setString("parentPostId", parentPostId).setBoolean("locked", locked).executeUpdate();
    }

    @Transactional
    public Integer lockBySiteId(String siteId, Boolean locked) {

        return sessionFactory.getCurrentSession()
            .createQuery("update Post set locked = :locked where siteId = :siteId")
            .setString("siteId", siteId).setBoolean("locked", locked).executeUpdate();
    }
}
