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
import org.sakaiproject.conversations.api.model.PostReactionTotal;
import org.sakaiproject.conversations.api.repository.PostReactionTotalRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class PostReactionTotalRepositoryImpl extends SpringCrudRepositoryImpl<PostReactionTotal, Long>  implements PostReactionTotalRepository {

    @Transactional(readOnly = true)
    public List<PostReactionTotal> findByPostId(String postId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<PostReactionTotal> query = cb.createQuery(PostReactionTotal.class);
        Root<PostReactionTotal> total = query.from(PostReactionTotal.class);
        query.where(cb.equal(total.get("postId"), postId));

        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public Optional<PostReactionTotal> findByPostIdAndReaction(String postId, Reaction reaction) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<PostReactionTotal> query = cb.createQuery(PostReactionTotal.class);
        Root<PostReactionTotal> total = query.from(PostReactionTotal.class);
        query.where(cb.and(cb.equal(total.get("reaction"), reaction), cb.equal(total.get("postId"), postId)));

        return session.createQuery(query).uniqueResultOptional();
    }

    @Transactional
    public Integer deleteByPostId(String postId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<PostReactionTotal> delete = cb.createCriteriaDelete(PostReactionTotal.class);
        Root<PostReactionTotal> total = delete.from(PostReactionTotal.class);
        delete.where(cb.equal(total.get("postId"), postId));

        return session.createQuery(delete).executeUpdate();
    }
}
