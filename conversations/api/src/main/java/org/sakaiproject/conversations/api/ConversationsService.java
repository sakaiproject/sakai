package org.sakaiproject.conversations.api;

import java.util.List;

import org.sakaiproject.conversations.api.model.Topic;

public interface ConversationsService {

    List<Topic> getTopicsAboutRef(String reference);
}
