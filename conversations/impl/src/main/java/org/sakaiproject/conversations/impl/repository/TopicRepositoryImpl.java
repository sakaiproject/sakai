package org.sakaiproject.conversations.impl.repository;

import java.util.Collections;
import java.util.List;

import org.sakaiproject.conversations.api.model.Topic;
import org.sakaiproject.conversations.api.repository.TopicRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class TopicRepositoryImpl extends SpringCrudRepositoryImpl<Topic, Long>  implements TopicRepository {

    @Transactional
    public List<Topic> findByAbout(String aboutReference) {

        return Collections.emptyList();
    }
}
