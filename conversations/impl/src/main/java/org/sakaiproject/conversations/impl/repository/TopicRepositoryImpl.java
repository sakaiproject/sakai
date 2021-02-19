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
