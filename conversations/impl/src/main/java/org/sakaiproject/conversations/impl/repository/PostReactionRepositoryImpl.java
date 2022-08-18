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
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.sakaiproject.conversations.api.model.PostReaction;
import org.sakaiproject.conversations.api.repository.PostReactionRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class PostReactionRepositoryImpl extends SpringCrudRepositoryImpl<PostReaction, Long>  implements PostReactionRepository {

    @Transactional(readOnly = true)
    public List<PostReaction> findByPostIdAndUserId(String postId, String userId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<PostReaction> query = cb.createQuery(PostReaction.class);
        Root<PostReaction> reaction = query.from(PostReaction.class);
        query.where(cb.and(cb.equal(reaction.get("userId"), userId),
                            cb.equal(reaction.get("postId"), postId)));

        return session.createQuery(query).list();
    }

    @Transactional
    public Integer deleteByPostId(String postId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<PostReaction> delete = cb.createCriteriaDelete(PostReaction.class);
        Root<PostReaction> reaction = delete.from(PostReaction.class);
        delete.where(cb.equal(reaction.get("postId"), postId));

        return session.createQuery(delete).executeUpdate();
    }
}
