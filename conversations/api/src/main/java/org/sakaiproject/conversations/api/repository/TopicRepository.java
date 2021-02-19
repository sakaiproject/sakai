package org.sakaiproject.conversations.api.repository;

import org.sakaiproject.conversations.api.model.Topic;

import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface TopicRepository extends SpringCrudRepository<Topic, Long> {
}
