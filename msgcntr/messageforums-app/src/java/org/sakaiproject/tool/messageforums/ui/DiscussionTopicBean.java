package org.sakaiproject.tool.messageforums.ui;
 
import java.util.Iterator;
import java.util.List;

import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.service.legacy.discussion.DiscussionMessage;
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
    List tmpMessages = topic.getMessages();
    if (tmpMessages !=null)
    {
      Iterator iter = tmpMessages.iterator();
      while (iter.hasNext())
      {
        DiscussionMessage message = (DiscussionMessage) iter.next();
        messages.add(new DiscussionMessageBean(message));
      }
    }
    return messages ;
  }  
}
