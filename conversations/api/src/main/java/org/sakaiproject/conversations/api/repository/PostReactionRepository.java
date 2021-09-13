package org.sakaiproject.conversations.api.repository;

import java.util.List;

import org.sakaiproject.conversations.api.model.PostReaction;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface PostReactionRepository extends SpringCrudRepository<PostReaction, Long> {

    List<PostReaction> findByPost_IdAndUserId(String postId, String userId);
    Integer deleteByPost_Id(String postId);
}
