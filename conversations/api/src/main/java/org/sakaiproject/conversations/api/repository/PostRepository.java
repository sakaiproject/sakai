package org.sakaiproject.conversations.api.repository;

import java.util.List;

import org.sakaiproject.conversations.api.model.Post;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface PostRepository extends SpringCrudRepository<Post, String> {

    List<Post> findByTopic_Id(String topicId);
    List<Post> findByParentPost_Id(String parentPostId);
    Integer deleteByTopic_Id(String topicId);
    Integer lockByTopic_Id(Boolean locked, String topicId);
    Integer lockByParentPost_Id(Boolean locked, String parentPostId);
    Integer lockBySiteId(String siteId, Boolean locked);
}
