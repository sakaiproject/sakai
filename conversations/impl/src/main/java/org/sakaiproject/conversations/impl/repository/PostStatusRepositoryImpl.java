package org.sakaiproject.conversations.impl.repository;

import java.util.List;
import java.util.Optional;

import org.hibernate.criterion.Restrictions;

import org.sakaiproject.conversations.api.model.PostStatus;
import org.sakaiproject.conversations.api.repository.PostStatusRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class PostStatusRepositoryImpl extends SpringCrudRepositoryImpl<PostStatus, Long>  implements PostStatusRepository {

    @Transactional
    public List<PostStatus> findByUserId(String userId) {

        return (List<PostStatus>) sessionFactory.getCurrentSession().createCriteria(PostStatus.class)
            .add(Restrictions.eq("userId", userId)).list();
    }

    @Transactional
    public Optional<PostStatus> findByPostIdAndUserId(String postId, String userId) {

        return Optional.ofNullable((PostStatus) sessionFactory.getCurrentSession().createCriteria(PostStatus.class)
            .add(Restrictions.eq("postId", postId))
            .add(Restrictions.eq("userId", userId))
            .uniqueResult());
    }

    @Transactional
    public List<PostStatus> findByTopicIdAndUserId(String topicId, String userId) {

        return (List<PostStatus>) sessionFactory.getCurrentSession().createCriteria(PostStatus.class)
            .add(Restrictions.eq("topicId", topicId))
            .add(Restrictions.eq("userId", userId))
            .list();
    }

    @Transactional
    public List<PostStatus> findByTopicIdAndUserIdAndViewed(String topicId, String userId, Boolean viewed) {

        return (List<PostStatus>) sessionFactory.getCurrentSession().createCriteria(PostStatus.class)
            .add(Restrictions.eq("topicId", topicId))
            .add(Restrictions.eq("userId", userId))
            .add(Restrictions.eq("viewed", viewed))
            .list();
    }

    @Transactional
    public Integer deleteByPostId(String postId) {

        return sessionFactory.getCurrentSession()
            .createQuery("delete from PostStatus where postId = :postId")
            .setString("postId", postId).executeUpdate();
    }
}
