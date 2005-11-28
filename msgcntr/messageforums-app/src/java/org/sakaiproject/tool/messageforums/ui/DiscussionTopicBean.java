package org.sakaiproject.tool.messageforums.ui;
 
import java.util.List;

import org.sakaiproject.api.app.messageforums.Topic;
public class DiscussionTopicBean
{
  private Topic topic;
  private int totalNoMessages;
  private int unreadNoMessages;
  private List messages;
  
  public DiscussionTopicBean(Topic topic)
  {
   this.topic= topic;    
  }
  
  /**
   * @return
   */
  public Topic getTopic()
  {
    return topic;
  }
  
  /**
   * @return
   */
  public int getTotalNoMessages()
  {
    return totalNoMessages;
  }
  
  /**
   * @param totalMessages
   */
  public void setTotalNoMessages(int totalMessages)
  {
    this.totalNoMessages = totalMessages;
  }
  
  /**
   * @return
   */
  public int getUnreadNoMessages()
  {
    return unreadNoMessages;
  }
  
  /**
   * @param unreadMessages
   */
  public void setUnreadNoMessages(int unreadMessages)
  {
    this.unreadNoMessages = unreadMessages;
  }
  
   
  /**
   * @return Returns the decorated messages.
   */
  public List getMessages()
  {
     return messages ;
  }  
  
   

  public void addMessage(DiscussionMessageBean decoMessage)
  {
    if(!messages.contains(decoMessage))
    {
      messages.add(decoMessage);    
    }
  } 
}
