package org.sakaiproject.conversations.impl.repository;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import org.sakaiproject.conversations.api.model.PostReaction;
import org.sakaiproject.conversations.api.repository.PostReactionRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class PostReactionRepositoryImpl extends SpringCrudRepositoryImpl<PostReaction, Long>  implements PostReactionRepository {

    @Transactional
    public List<PostReaction> findByPost_IdAndUserId(String postId, String userId) {

        return (List<PostReaction>) sessionFactory.getCurrentSession().createCriteria(PostReaction.class)
            .add(Restrictions.eq("post.id", postId))
            .add(Restrictions.eq("userId", userId))
            .list();
    }

    @Transactional
    public Integer deleteByPost_Id(String postId) {

        return sessionFactory.getCurrentSession()
            .createQuery("delete from PostReaction where post.id = :postId")
            .setString("postId", postId).executeUpdate();
    }
}
