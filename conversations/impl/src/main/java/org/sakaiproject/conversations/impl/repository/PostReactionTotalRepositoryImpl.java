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
