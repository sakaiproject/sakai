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
import org.sakaiproject.conversations.api.model.PostReactionTotal;
import org.sakaiproject.conversations.api.repository.PostReactionTotalRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class PostReactionTotalRepositoryImpl extends SpringCrudRepositoryImpl<PostReactionTotal, Long>  implements PostReactionTotalRepository {

    @Transactional
    public List<PostReactionTotal> findByPost_Id(String postId) {

        return (List<PostReactionTotal>) sessionFactory.getCurrentSession().createCriteria(PostReactionTotal.class)
            .add(Restrictions.eq("post.id", postId))
            .list();
    }

    @Transactional
    public Optional<PostReactionTotal> findByPost_IdAndReaction(String postId, Reaction reaction) {

        return Optional.ofNullable((PostReactionTotal) sessionFactory.getCurrentSession().createCriteria(PostReactionTotal.class)
            .add(Restrictions.eq("post.id", postId))
            .add(Restrictions.eq("reaction", reaction))
            .uniqueResult());
    }


    @Transactional
    public Integer deleteByPost_Id(String postId) {

        return sessionFactory.getCurrentSession()
            .createQuery("delete from PostReactionTotal where post.id = :postId")
            .setString("postId", postId).executeUpdate();
    }
}
