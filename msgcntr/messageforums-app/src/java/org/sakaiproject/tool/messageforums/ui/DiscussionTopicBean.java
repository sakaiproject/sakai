package org.sakaiproject.tool.messageforums.ui;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.Topic;

/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 */
public class DiscussionTopicBean
{
  private DiscussionTopic topic;
  private int totalNoMessages;
  private int unreadNoMessages;
  private boolean hasNextTopic;
  private boolean hasPreviousTopic;
  private Long nextTopicId;
  private Long previousTopicId;
  private boolean readFullDesciption;
  private boolean markForDeletion;
  
  private List messages = new ArrayList();

  public DiscussionTopicBean(DiscussionTopic topic)
  {
    this.topic = topic;
  }

  /**
   * @return
   */
  public DiscussionTopic getTopic()
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
   * @return Returns the hasNextTopic.
   */
  public boolean isHasNextTopic()
  {
    return hasNextTopic;
  }

  /**
   * @param hasNextTopic
   *          The hasNextTopic to set.
   */
  public void setHasNextTopic(boolean hasNextTopic)
  {
    this.hasNextTopic = hasNextTopic;
  }

  /**
   * @return Returns the hasPreviousTopic.
   */
  public boolean isHasPreviousTopic()
  {
    return hasPreviousTopic;
  }

  /**
   * @param hasPreviousTopic
   *          The hasPreviousTopic to set.
   */
  public void setHasPreviousTopic(boolean hasPreviousTopic)
  {
    this.hasPreviousTopic = hasPreviousTopic;
  }

  /**
   * @return Returns the nextTopicId.
   */
  public Long getNextTopicId()
  {
    return nextTopicId;
  }

  /**
   * @param nextTopicId
   *          The nextTopicId to set.
   */
  public void setNextTopicId(Long nextTopicId)
  {
    this.nextTopicId = nextTopicId;
  }

  /**
   * @return Returns the previousTopicId.
   */
  public Long getPreviousTopicId()
  {
    return previousTopicId;
  }

  /**
   * @param previousTopicId
   *          The previousTopicId to set.
   */
  public void setPreviousTopicId(Long previousTopicId)
  {
    this.previousTopicId = previousTopicId;
  }

  /**
   * @return Returns the decorated messages.
   */
  public List getMessages()
  {
    return messages;
  }
  
  public void setMessages(List messages)
  {
  	this.messages = messages;
  }

  public void addMessage(DiscussionMessageBean decoMessage)
  {
    if (!messages.contains(decoMessage))
    {
      messages.add(decoMessage);
    }
  }

  /**
   * @return Returns the if ExtendedDesciption is available
   */
  public boolean getHasExtendedDesciption()
  {
    if (topic.getExtendedDescription() != null
        && topic.getExtendedDescription().trim().length() > 0 && (!readFullDesciption))
    {
      return true;
    }
    return false;
  }
  
  /**
   * @return Returns the readFullDesciption.
   */
  public boolean isReadFullDesciption()
  {
    return readFullDesciption;
  }

  /**
   * @param readFullDesciption The readFullDesciption to set.
   */
  public void setReadFullDesciption(boolean readFullDesciption)
  {
    this.readFullDesciption = readFullDesciption;
  }

  /**
   * @return Returns the parentForumId.
   */
  public String getParentForumId()
  {
    return topic.getBaseForum().getId().toString();
  }

  
  /**
   * @return Returns the mustRespondBeforeReading.
   */
  public String getMustRespondBeforeReading()
  {
   if(topic==null || topic.getMustRespondBeforeReading()==null||topic.getMustRespondBeforeReading().booleanValue()==false)
    {
      return Boolean.FALSE.toString();      
    }
   return  Boolean.TRUE.toString();
  }

  /**
   * @param mustRespondBeforeReading 
   */
  public void setMustRespondBeforeReading(String mustRespondBeforeReading)
  {
    if(mustRespondBeforeReading.equals(Boolean.TRUE.toString()))
    {
      topic.setMustRespondBeforeReading(new Boolean(true));
    }
    else
    {
      topic.setMustRespondBeforeReading(new Boolean(false));
    }
  } 
  

  /**
   * @return Returns the locked.
   */
  public String getLocked()
  {
     if(topic==null || topic.getLocked()==null||topic.getLocked().booleanValue()==false)
    {
      return Boolean.FALSE.toString();      
    }
    return  Boolean.TRUE.toString();
  }

  /**
   * @param locked The locked to set.
   */
  public void setLocked(String locked)
  {
    if(locked.equals(Boolean.TRUE.toString()))
    {
       topic.setLocked(new Boolean(true));
    }
    else
    {
       topic.setLocked(new Boolean(false));
    }
  } 
  
  public void removeMessage(DiscussionMessageBean decoMessage)
  {
  	for(int i=0; i<messages.size(); i++)
  	{
  		if (((DiscussionMessageBean)messages.get(i)).getMessage().getId().equals(decoMessage.getMessage().getId()))
  		{
  			messages.remove(i);
  			break;
  		}
  	}
  }

  /**
   * @return Returns the markForDeletion.
   */
  public boolean isMarkForDeletion()
  {
    return markForDeletion;
  }

  /**
   * @param markForDeletion The markForDeletion to set.
   */
  public void setMarkForDeletion(boolean markForDeletion)
  {
    this.markForDeletion = markForDeletion;
  }

  public void setTopic(DiscussionTopic topic)
  {
    this.topic = topic;
  }  
}
