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
import org.hibernate.criterion.Projections;

import org.sakaiproject.conversations.api.model.Topic;
import org.sakaiproject.conversations.api.repository.TopicRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class TopicRepositoryImpl extends SpringCrudRepositoryImpl<Topic, String>  implements TopicRepository {

    @Transactional(readOnly = true)
    public List<Topic> findBySiteId(String siteId) {

        return (List<Topic>) sessionFactory.getCurrentSession().createCriteria(Topic.class)
            .add(Restrictions.eq("siteId", siteId))
            .addOrder(Order.desc("metadata.created"))
            .list();
    }

    @Transactional(readOnly = true)
    public List<Topic> findByTags_Id(Long tagId) {

        return (List<Topic>) sessionFactory.getCurrentSession()
            .createQuery("from Topic as t where :tagId in elements(t.tagIds)")
            .setParameter("tagId", tagId)
            .list();
    }

    @Transactional(readOnly = true)
    public Long countBySiteIdAndMetadata_Creator_Id(String siteId, String creatorId) {

        return (Long) sessionFactory.getCurrentSession().createCriteria(Topic.class)
            .add(Restrictions.eq("siteId", siteId))
            .add(Restrictions.eq("metadata.creatorId", creatorId))
            .setProjection(Projections.rowCount()).uniqueResult();


    }

    @Transactional
    public Integer lockBySiteId(String siteId, Boolean locked) {

        return sessionFactory.getCurrentSession().createQuery("update Topic set locked = :locked where siteId = :siteId")
            .setBoolean("locked", locked).setString("siteId", siteId).executeUpdate();
    }
}
