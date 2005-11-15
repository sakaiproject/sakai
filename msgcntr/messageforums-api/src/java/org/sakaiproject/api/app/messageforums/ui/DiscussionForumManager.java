package org.sakaiproject.api.app.messageforums.ui;

import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.Message;

public interface DiscussionForumManager {
    /**
     * Retrieve discussion forum area
     * 
     * @return
     */
    Area getDiscussionForumArea();

    void saveMessage(Message message);

    void deleteMessage(Message message);

    Message getMessageById(String id);

}
