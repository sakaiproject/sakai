package org.sakaiproject.conversations.api.repository;

import java.util.List;

import org.sakaiproject.conversations.api.model.Comment;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface CommentRepository extends SpringCrudRepository<Comment, String> {

    List<Comment> findByPost_Id(String postId);
    Integer deleteByPost_Id(String postId);
    Integer deleteByPost_Topic_Id(String topicId);
    Integer lockByPost_Id(String postId, Boolean locked);
    Integer lockBySiteId(String siteId, Boolean locked);
}
