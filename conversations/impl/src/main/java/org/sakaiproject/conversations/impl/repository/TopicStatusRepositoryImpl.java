package org.sakaiproject.conversations.impl.repository;

import java.util.List;
import java.util.Optional;

import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import org.sakaiproject.conversations.api.model.TopicStatus;
import org.sakaiproject.conversations.api.repository.TopicStatusRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class TopicStatusRepositoryImpl extends SpringCrudRepositoryImpl<TopicStatus, Long>  implements TopicStatusRepository {

    @Transactional
    public Optional<TopicStatus> findByTopicIdAndUserId(String topicId, String userId) {

        return Optional.ofNullable((TopicStatus) sessionFactory.getCurrentSession().createCriteria(TopicStatus.class)
            .add(Restrictions.eq("topicId", topicId))
            .add(Restrictions.eq("userId", userId))
            .uniqueResult());
    }

    @Transactional
    public Integer deleteByTopicId(String topicId) {

        return sessionFactory.getCurrentSession()
            .createQuery("delete from TopicStatus where topicId = :topicId")
            .setString("topicId", topicId).executeUpdate();
    }

    @Transactional(readOnly = true)
    public List<Object[]> countBySiteIdAndViewed(String siteId, Boolean viewed) {

        return (List<Object[]>) sessionFactory.getCurrentSession()
            .createQuery("select userId, count(topicId) as total from TopicStatus as ts where siteId = :siteId and ts.viewed = :viewed group by userId")
            .setString("siteId", siteId).setBoolean("viewed", viewed).list();
    }
}
