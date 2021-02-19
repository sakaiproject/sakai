package org.sakaiproject.conversations.api.repository;

import java.util.List;
import java.util.Optional;

import org.sakaiproject.conversations.api.model.TopicStatus;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface TopicStatusRepository extends SpringCrudRepository<TopicStatus, Long> {

    Optional<TopicStatus> findByTopicIdAndUserId(String topicId, String userId);
    Integer deleteByTopicId(String topicId);
    List<Object[]> countBySiteIdAndViewed(String siteId, Boolean viewed);
}
