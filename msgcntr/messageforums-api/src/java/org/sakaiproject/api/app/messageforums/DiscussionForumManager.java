package org.sakaiproject.api.app.messageforums;

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
