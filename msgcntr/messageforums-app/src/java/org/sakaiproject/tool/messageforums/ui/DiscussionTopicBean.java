/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-app/src/java/org/sakaiproject/tool/messageforums/ui/DiscussionTopicBean.java $
 * $Id: DiscussionTopicBean.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.messageforums.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;

/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 * @author Chen Wen
 */
public class DiscussionTopicBean
{
  private static final Log LOG = LogFactory
  .getLog(DiscussionForumBean.class);
  private DiscussionTopic topic;
  private int totalNoMessages;
  private int unreadNoMessages;
  private boolean hasNextTopic;
  private boolean hasPreviousTopic;
  private Long nextTopicId;
  private Long previousTopicId;
  private boolean readFullDesciption;
  private boolean markForDeletion;
  private UIPermissionsManager uiPermissionsManager;
  private DiscussionForumManager forumManager;
  private ArrayList contributorsList = new ArrayList();
  private ArrayList accessorList = new ArrayList();
  private String gradeAssign;
  

  private List messages = new ArrayList();

  public DiscussionTopicBean(DiscussionTopic topic, DiscussionForum forum,
      UIPermissionsManager uiPermissionsManager, DiscussionForumManager forumManager)
  {
    this.topic = topic;
    this.uiPermissionsManager = uiPermissionsManager;
    this.topic.setBaseForum(forum);
    this.forumManager=forumManager;
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
    if(LOG.isDebugEnabled())
    {
       LOG.debug("setMessages(List"+ messages+")");
    }
    this.messages = messages;
  }

  public void addMessage(DiscussionMessageBean decoMessage)
  {
    if(LOG.isDebugEnabled())
    {
       LOG.debug("addMessage(DiscussionMessageBean"+ decoMessage+")");
    }
    if (!messages.contains(decoMessage))
    {
      messages.add(decoMessage);
    }
  }

  public void insertMessage(DiscussionMessageBean decoMessage)
  {
    if(LOG.isDebugEnabled())
    {
       LOG.debug("insertMessage(DiscussionMessageBean"+ decoMessage+")");
    }
    if (!messages.contains(decoMessage))
    {
    	messages.add(0, decoMessage);
    }
  }

  /**
   * @return Returns the if ExtendedDesciption is available
   */
  public boolean getHasExtendedDesciption()
  {
    LOG.debug("getHasExtendedDesciption()");
    if (topic.getExtendedDescription() != null
        && topic.getExtendedDescription().trim().length() > 0
        && (!readFullDesciption))
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
    LOG.debug("isReadFullDesciption()");
    return readFullDesciption;
  }

  /**
   * @param readFullDesciption
   *          The readFullDesciption to set.
   */
  public void setReadFullDesciption(boolean readFullDesciption)
  {
    if(LOG.isDebugEnabled())
    {
       LOG.debug("setReadFullDesciption(boolean "+ readFullDesciption+")");
    }
    this.readFullDesciption = readFullDesciption;
  }

  /**
   * @return Returns the parentForumId.
   */
  public String getParentForumId()
  {
    LOG.debug("getParentForumId()");
    return topic.getBaseForum().getId().toString();
  }

  /**
   * @return Returns the mustRespondBeforeReading.
   */
  public String getMustRespondBeforeReading()
  {
    LOG.debug("getMustRespondBeforeReading()");
    if (topic == null || topic.getMustRespondBeforeReading() == null
        || topic.getMustRespondBeforeReading().booleanValue() == false)
    {
      return Boolean.FALSE.toString();
    }
    return Boolean.TRUE.toString();
  }

  /**
   * @param mustRespondBeforeReading
   */
  public void setMustRespondBeforeReading(String mustRespondBeforeReading)
  {
    if(LOG.isDebugEnabled())
    {
       LOG.debug("setMustRespondBeforeReading(String"+ mustRespondBeforeReading+")");
    }
    if (mustRespondBeforeReading.equals(Boolean.TRUE.toString()))
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
    LOG.debug("getLocked()");
    if (topic == null || topic.getLocked() == null
        || topic.getLocked().booleanValue() == false)
    {
      return Boolean.FALSE.toString();
    }
    return Boolean.TRUE.toString();
  }

  /**
   * @param locked
   *          The locked to set.
   */
  public void setLocked(String locked)
  {
    if(LOG.isDebugEnabled())
    {
       LOG.debug("setLocked(String "+ locked+")");
    }
    if (locked.equals(Boolean.TRUE.toString()))
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
    if(LOG.isDebugEnabled())
    {
       LOG.debug("removeMessage(DiscussionMessageBean"+ decoMessage+")");
    }
    for (int i = 0; i < messages.size(); i++)
    {
      if (((DiscussionMessageBean) messages.get(i)).getMessage().getId()
          .equals(decoMessage.getMessage().getId()))
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
    LOG.debug("isMarkForDeletion()");
    return markForDeletion;
  }

  /**
   * @param markForDeletion
   *          The markForDeletion to set.
   */
  public void setMarkForDeletion(boolean markForDeletion)
  {
    if(LOG.isDebugEnabled())
    {
       LOG.debug("setMarkForDeletion(boolean "+ markForDeletion+")");
    }
    this.markForDeletion = markForDeletion;
  }

  /**
   * @param topic
   */
  public void setTopic(DiscussionTopic topic)
  {
    if(LOG.isDebugEnabled())
    {
       LOG.debug("setTopic(DiscussionTopic"+ topic+")");
    }
    this.topic = topic;
  }

  /**
   * @return
   */
  public boolean getIsNewResponse()
  {
    LOG.debug("getIsNewResponse()");
    return uiPermissionsManager.isNewResponse(topic, (DiscussionForum) topic
        .getBaseForum());
  }

  /**
   * @return
   */
  public boolean getIsNewResponseToResponse()
  {
    LOG.debug("getIsNewResponseToResponse()");
    return uiPermissionsManager.isNewResponseToResponse(topic,
        (DiscussionForum) topic.getBaseForum());
  }

  /**
   * @return
   */
  public boolean getIsMovePostings()
  {
    LOG.debug("getIsMovePostings()");
    return uiPermissionsManager.isMovePostings(topic, (DiscussionForum) topic
        .getBaseForum());
  }

  /**
   * @return
   */
  public boolean isChangeSettings()
  {
    LOG.debug("isChangeSettings()");
    return uiPermissionsManager.isChangeSettings(topic, (DiscussionForum) topic
        .getBaseForum());
  }

  /**
   * @return
   */
  public boolean isPostToGradebook()
  {
    LOG.debug("isPostToGradebook()");
    return uiPermissionsManager.isPostToGradebook(topic,
        (DiscussionForum) topic.getBaseForum());
  }
  
  public boolean getIsPostToGradebook()
  {
    LOG.debug("getIsPostToGradebook()");
    return isPostToGradebook();
  }

  /**
   * @return
   */
  public boolean getIsRead()
  {
    LOG.debug("getIsRead()");
    return uiPermissionsManager.isRead(topic, (DiscussionForum) topic
        .getBaseForum());
  }

  /**
   * @return
   */
  public boolean getIsReviseAny()
  {
    LOG.debug("getIsReviseAny()");
    return uiPermissionsManager.isReviseAny(topic, (DiscussionForum) topic
        .getBaseForum());
  }

  /**
   * @return
   */
  public boolean getIsReviseOwn()
  {
    LOG.debug("getIsReviseOwn()");
    return uiPermissionsManager.isReviseOwn(topic, (DiscussionForum) topic
        .getBaseForum());
  }

  /**
   * @return
   */
  public boolean getIsDeleteAny()
  {
    LOG.debug("getIsDeleteAny()");
    return uiPermissionsManager.isDeleteAny(topic, (DiscussionForum) topic
        .getBaseForum());
  }

  /**
   * @return
   */
  public boolean getIsDeleteOwn()
  {
    LOG.debug("getIsDeleteOwn()");
    return uiPermissionsManager.isDeleteOwn(topic, (DiscussionForum) topic
        .getBaseForum());
  }

  /**
   * @return
   */
  public boolean getIsMarkAsRead()
  {
    LOG.debug("getIsMarkAsRead()");
    return uiPermissionsManager.isMarkAsRead(topic, (DiscussionForum) topic
        .getBaseForum());
  }

  
  
  /**
   * @return
   */
  public ArrayList getContributorsList()
  {
    LOG.debug("getContributorsList()");  
    Iterator iter= forumManager.getContributorsList(topic, (DiscussionForum)topic.getBaseForum()).iterator();
    while (iter.hasNext())
    { 
      contributorsList.add((String)iter.next());
     }
    return contributorsList; 

  }
  
  /**
   * @return
   */
  public ArrayList getAccessorList()
  {
    LOG.debug("getAccessorList()");
    Iterator iter= forumManager.getAccessorsList(topic, (DiscussionForum)topic.getBaseForum()).iterator();
    while (iter.hasNext())
    { 
      accessorList.add((String)iter.next());
     }
    return accessorList; 
  }

  /**
   * @param accessorList The accessorList to set.
   */
  public void setAccessorList(ArrayList accessorList)
  {    
    if(LOG.isDebugEnabled())
     {
        LOG.debug("setAccessorList(List"+ accessorList+")");
     }    
    topic.getActorPermissions().setAccessors(forumManager.decodeAccessorsList(accessorList));
  }

  /**
   * @param contributorsList The contributorsList to set.
   */
  public void setContributorsList(ArrayList contributorsList)
  {
    if(LOG.isDebugEnabled())
    {
       LOG.debug("setContributorsList(List"+ contributorsList+")");
    }    
    topic.getActorPermissions().setContributors(forumManager.decodeContributorsList(contributorsList));
  }

  public String getGradeAssign()
  {
    return gradeAssign;
  }

  public void setGradeAssign(String gradeAssign)
  {
    this.gradeAssign = gradeAssign;
  }
  
}
