package org.sakaiproject.conversations.api.repository;

import java.util.List;
import java.util.Optional;

import org.sakaiproject.conversations.api.Reaction;
import org.sakaiproject.conversations.api.model.TopicReactionTotal;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface TopicReactionTotalRepository extends SpringCrudRepository<TopicReactionTotal, Long> {

    List<TopicReactionTotal> findByTopic_Id(String topicId);
    Optional<TopicReactionTotal> findByTopic_IdAndReaction(String topicId, Reaction reaction);
    Integer deleteByTopic_Id(String topicId);
}
