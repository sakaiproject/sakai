package org.sakaiproject.conversations.api.repository;

import java.util.List;

import org.sakaiproject.conversations.api.model.Topic;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface TopicRepository extends SpringCrudRepository<Topic, String> {

    List<Topic> findBySiteId(String siteId);
    List<Topic> findByTags_Id(Long tagId);
    Long countBySiteIdAndMetadata_Creator_Id(String siteId, String creatorId);
    Integer lockBySiteId(String siteId, Boolean locked);
}
