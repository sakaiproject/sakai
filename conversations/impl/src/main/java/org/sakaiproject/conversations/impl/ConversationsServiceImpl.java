package org.sakaiproject.conversations.impl;

import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.sakaiproject.conversations.api.ConversationsService;
import org.sakaiproject.conversations.api.model.Topic;
import org.sakaiproject.conversations.api.repository.TopicRepository;

public class ConversationsServiceImpl implements ConversationsService {

    @Resource
    private TopicRepository topicRepository;

    public List<Topic> getTopicsAboutRef(String reference) {
        return topicRepository.findByAbout(reference);
    }
}
