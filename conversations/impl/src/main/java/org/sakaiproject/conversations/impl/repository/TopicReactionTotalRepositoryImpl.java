package org.sakaiproject.conversations.impl.repository;

import java.util.List;
import java.util.Optional;

import org.hibernate.criterion.Restrictions;

import org.sakaiproject.conversations.api.Reaction;
import org.sakaiproject.conversations.api.model.TopicReactionTotal;
import org.sakaiproject.conversations.api.repository.TopicReactionTotalRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class TopicReactionTotalRepositoryImpl extends SpringCrudRepositoryImpl<TopicReactionTotal, Long>  implements TopicReactionTotalRepository {

    @Transactional
    public List<TopicReactionTotal> findByTopic_Id(String topicId) {

        return (List<TopicReactionTotal>) sessionFactory.getCurrentSession().createCriteria(TopicReactionTotal.class)
            .add(Restrictions.eq("topic.id", topicId))
            .list();
    }

    @Transactional
    public Optional<TopicReactionTotal> findByTopic_IdAndReaction(String topicId, Reaction reaction) {

        return Optional.ofNullable((TopicReactionTotal) sessionFactory.getCurrentSession().createCriteria(TopicReactionTotal.class)
            .add(Restrictions.eq("topic.id", topicId))
            .add(Restrictions.eq("reaction", reaction))
            .uniqueResult());
    }


    @Transactional
    public Integer deleteByTopic_Id(String topicId) {

        return sessionFactory.getCurrentSession()
            .createQuery("delete from TopicReactionTotal where topic.id = :topicId")
            .setString("topicId", topicId).executeUpdate();
    }
}
