package org.sakaiproject.tool.messageforums.ui;
import java.util.Date;
import java.util.List;

import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.Topic;
public class PrivateTopicBean
{
  private Topic topic;
  private int totalMessages;
  private int unreadMessages;
  public PrivateTopicBean(Topic topic)
  {
   this.topic= topic;    
  }
  public Topic getTopic()
  {
    return topic;
  }
  
  /**
   * @return
   */
  public int getTotalMessages()
  {
    return totalMessages;
  }
  
  /**
   * @param totalMessages
   */
  public void setTotalMessages(int totalMessages)
  {
    this.totalMessages = totalMessages;
  }
  
  /**
   * @return
   */
  public int getUnreadMessages()
  {
    return unreadMessages;
  }
  
  /**
   * @param unreadMessages
   */
  public void setUnreadMessages(int unreadMessages)
  {
    this.unreadMessages = unreadMessages;
  }
  
  /**
   * @param topic
   */
  public void setTopic(Topic topic)
  {
    this.topic = topic;
  }
  
}
