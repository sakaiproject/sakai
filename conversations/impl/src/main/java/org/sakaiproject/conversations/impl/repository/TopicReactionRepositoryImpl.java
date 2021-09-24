package org.sakaiproject.conversations.impl.repository;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import org.sakaiproject.conversations.api.model.TopicReaction;
import org.sakaiproject.conversations.api.repository.TopicReactionRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class TopicReactionRepositoryImpl extends SpringCrudRepositoryImpl<TopicReaction, Long>  implements TopicReactionRepository {

    @Transactional
    public List<TopicReaction> findByTopic_IdAndUserId(String topicId, String userId) {

        return (List<TopicReaction>) sessionFactory.getCurrentSession().createCriteria(TopicReaction.class)
            .add(Restrictions.eq("topic.id", topicId))
            .add(Restrictions.eq("userId", userId))
            .list();
    }

    @Transactional
    public Integer deleteByTopic_Id(String topicId) {

        return sessionFactory.getCurrentSession()
            .createQuery("delete from TopicReaction where topic.id = :topicId")
            .setString("topicId", topicId).executeUpdate();
    }
}
