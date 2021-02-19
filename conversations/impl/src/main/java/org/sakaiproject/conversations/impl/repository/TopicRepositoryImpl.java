package org.sakaiproject.conversations.impl.repository;

import org.sakaiproject.conversations.api.model.Topic;
import org.sakaiproject.conversations.api.repository.TopicRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class TopicRepositoryImpl extends SpringCrudRepositoryImpl<Topic, Long>  implements TopicRepository {
}
