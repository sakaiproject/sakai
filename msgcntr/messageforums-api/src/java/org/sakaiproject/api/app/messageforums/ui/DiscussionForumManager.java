package org.sakaiproject.api.app.messageforums.ui;

import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.Topic;

public interface DiscussionForumManager
{
  /**
   * Retrieve discussion forum area
   * 
   * @return
   */
  Area getDiscussionForumArea();  
  
  /**
   * @param message
   */
  void saveMessage(Message message);

  /**
   * @param message
   */
  void deleteMessage(Message message);

  /**
   * @param id
   * @return
   */
  Message getMessageById(String id);
  
  /**
   * @param topic
   * @return
   */
  int getTotalNoMessages(Topic topic);
  
  /**
   * @param topic
   * @return
   */
  int getUnreadNoMessages(Topic topic);

}
