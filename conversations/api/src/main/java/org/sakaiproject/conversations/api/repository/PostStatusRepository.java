package org.sakaiproject.conversations.api.repository;

import java.util.List;
import java.util.Optional;

import org.sakaiproject.conversations.api.model.PostStatus;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface PostStatusRepository extends SpringCrudRepository<PostStatus, Long> {

    List<PostStatus> findByUserId(String userId);
    List<PostStatus> findByTopicIdAndUserId(String topicId, String userId);
    List<PostStatus> findByTopicIdAndUserIdAndViewed(String topicId, String userId, Boolean viewed);
    Optional<PostStatus> findByPostIdAndUserId(String postId, String userId);
    Integer deleteByPostId(String postId);
}
