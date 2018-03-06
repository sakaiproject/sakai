/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-app/src/java/org/sakaiproject/tool/messageforums/ui/DiscussionTopicBean.java $
 * $Id: DiscussionTopicBean.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.messageforums.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.UserPreferencesManager;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.util.ResourceLoader;

/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 * @author Chen Wen
 */
@Slf4j
public class DiscussionTopicBean
{

  private static UserPreferencesManager userPreferencesManager = ComponentManager.get(UserPreferencesManager.class);
  private DiscussionTopic topic;
  private int totalNoMessages;
  private int unreadNoMessages;
  private boolean hasNextTopic;
  private boolean hasPreviousTopic;
  private Long nextTopicId;
  private Long previousTopicId;
  private boolean readFullDesciption;
  private boolean markForDeletion;
  private boolean markForDuplication;
  private UIPermissionsManager uiPermissionsManager;
  private DiscussionForumManager forumManager;
  private ArrayList contributorsList = new ArrayList();
  private ArrayList accessorList = new ArrayList();
  private String gradeAssign;
  private Boolean nonePermission = null;
  private boolean sorted = false;

  
  private Boolean isRead = null;
  private Boolean isReviseAny = null; 
  private Boolean isReviseOwn = null;
  private Boolean isDeleteAny = null;
  private Boolean isDeleteOwn = null;
  private Boolean isMarkAsRead = null;
  private Boolean isModeratedAndHasPerm = null;
  
  private Boolean changeSettings = null;
  private Boolean isMovePostings = null;
  private Boolean isNewResponse = null;
  private Boolean isNewResponseToResponse = null;
  private Boolean postToGradebook = null;
  private String locked = "";
  private String moderated = "";
  private String postFirst = "";
  private String postAnonymous = "";
  private String revealIDsToRoles = "";
  private String mustRespondBeforeReading = "";
  private String parentForumId = "";
  
  private String openDate = "";
  private String closeDate = "";

  private SimpleDateFormat datetimeFormat = ourDateFormat();
  
  private static final String MESSAGECENTER_BUNDLE = "org.sakaiproject.api.app.messagecenter.bundle.Messages";
  private static final ResourceLoader rb = new ResourceLoader(MESSAGECENTER_BUNDLE);
  
  private List messages = new ArrayList();

  private SimpleDateFormat ourDateFormat() {
      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
      df.setTimeZone(userPreferencesManager.getTimeZone());
      return df;
  }

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
    if(log.isDebugEnabled())
    {
       log.debug("setMessages(List"+ messages+")");
    }
    this.messages = messages;
  }

  public void addMessage(DiscussionMessageBean decoMessage)
  {
    if(log.isDebugEnabled())
    {
       log.debug("addMessage(DiscussionMessageBean"+ decoMessage+")");
    }
    if (!messages.contains(decoMessage))
    {
      messages.add(decoMessage);
    }
  }

  public void insertMessage(DiscussionMessageBean decoMessage)
  {
    if(log.isDebugEnabled())
    {
       log.debug("insertMessage(DiscussionMessageBean"+ decoMessage+")");
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
    log.debug("getHasExtendedDesciption()");
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
    log.debug("isReadFullDesciption()");
    return readFullDesciption;
  }

  /**
   * @param readFullDesciption
   *          The readFullDesciption to set.
   */
  public void setReadFullDesciption(boolean readFullDesciption)
  {
    if(log.isDebugEnabled())
    {
       log.debug("setReadFullDesciption(boolean "+ readFullDesciption+")");
    }
    this.readFullDesciption = readFullDesciption;
  }
 
  /**
   * @return Returns the parentForumId.
   */
  public String getParentForumId()
  {
    log.debug("getParentForumId()");
    if ("".equals(parentForumId)){
    	parentForumId = topic.getBaseForum().getId().toString();
    }
    return parentForumId;
  }

  /**
   * @return Returns the mustRespondBeforeReading.
   */
  public String getMustRespondBeforeReading()
  {
    log.debug("getMustRespondBeforeReading()");
    if ("".equals(mustRespondBeforeReading)){
	    if (topic == null || topic.getMustRespondBeforeReading() == null
	        || topic.getMustRespondBeforeReading().booleanValue() == false)
	    {
	      mustRespondBeforeReading = Boolean.FALSE.toString();
	    }
	    else
	    {
	      mustRespondBeforeReading = Boolean.TRUE.toString();
	    }
    }
    return mustRespondBeforeReading;
  }

  /**
   * @param mustRespondBeforeReading
   */
  public void setMustRespondBeforeReading(String mustRespondBeforeReading)
  {
    if(log.isDebugEnabled())
    {
       log.debug("setMustRespondBeforeReading(String"+ mustRespondBeforeReading+")");
    }
    if (mustRespondBeforeReading.equals(Boolean.TRUE.toString()))
    {
      topic.setMustRespondBeforeReading(Boolean.valueOf(true));
    }
    else
    {
      topic.setMustRespondBeforeReading(Boolean.valueOf(false));
    }
  }

  /**
   * @return Returns the locked.
   */
  public String getLocked()
  {
    log.debug("getLocked()");
    if ("".equals(locked)){
	    if (topic == null || topic.getLocked() == null
	        || topic.getLocked().booleanValue() == false)
	    {
	      locked = Boolean.FALSE.toString();
	    }
	    else
	    {
	      locked =  Boolean.TRUE.toString();
	    }
    }
    return locked;
  }

  /**
   * @param locked
   *          The locked to set.
   */
  public void setLocked(String locked)
  {
    if(log.isDebugEnabled())
    {
       log.debug("setLocked(String "+ locked+")");
    }
    if (locked.equals(Boolean.TRUE.toString()))
    {
      topic.setLocked(Boolean.valueOf(true));
    }
    else
    {
      topic.setLocked(Boolean.valueOf(false));
    }
  }
  
  /**
   * @return Returns the boolean value of locked.
   */
  public Boolean getTopicLocked()
  {
    log.debug("getTopicLocked()");
    if ("".equals(locked)){
	    if (topic == null || topic.getLocked() == null
	        || topic.getLocked().booleanValue() == false)
	    {
	      locked = Boolean.FALSE.toString();
	    }
	    else
	    {
	      locked =  Boolean.TRUE.toString();
	    }
    }
    return Boolean.parseBoolean(locked);
  }

  /**
   * @param Boolean locked
   *          The locked to set.
   */
  public void setTopicLocked(Boolean locked)
  {
    if(log.isDebugEnabled())
    {
       log.debug("setTopicLocked(String "+ locked+")");
    }
    topic.setLocked(locked);
  }
  
  
  /**
   * @return Returns the moderated status.
   */
  public String getModerated()
  {
    log.debug("getModerated()");
    if ("".equals(moderated)){
	    if (topic == null || topic.getModerated() == null
	        || topic.getModerated().booleanValue() == false)
	    {
	      moderated = Boolean.FALSE.toString();
	    }
	    else
	    {
	      moderated = Boolean.TRUE.toString();
	    }
    }
    return moderated;
  }

  /**
   * @param moderated
   * Set the moderated status.
   */
  public void setModerated(String moderated)
  {
    if(log.isDebugEnabled())
    {
       log.debug("setModerated(String "+ moderated+")");
    }
    if (moderated.equals(Boolean.TRUE.toString()))
    {
      topic.setModerated(Boolean.valueOf(true));
    }
    else
    {
      topic.setModerated(Boolean.valueOf(false));
    }
  }
  
  /**
   * @return Returns the boolean value of moderated status.
   */
  public Boolean getTopicModerated()
  {
    log.debug("getTopicModerated()");
    if ("".equals(moderated)){
	    if (topic == null || topic.getModerated() == null
	        || topic.getModerated().booleanValue() == false)
	    {
	      moderated = Boolean.FALSE.toString();
	    }
	    else
	    {
	      moderated = Boolean.TRUE.toString();
	    }
    }
    return Boolean.parseBoolean(moderated);
  }

  /**
   * @param Boolean moderated
   * Set the moderated status.
   */
  public void setTopicModerated(Boolean moderated)
  {
    if(log.isDebugEnabled())
    {
       log.debug("setTopicModerated(String "+ moderated+")");
    }
    topic.setModerated(moderated);    
  }
  
  /**
   * Returns the autoMarkThreadsRead value.
   */
  public String getAutoMarkThreadsRead()
  {
    log.debug("getAutoMarkThreadsRead()");
    if (topic == null || topic.getAutoMarkThreadsRead() == null) {
      return Boolean.FALSE.toString();
    } else {
      return topic.getAutoMarkThreadsRead().toString();
    }
  }
  
  /**
   * Set the autoMarkThreadsRead value for this Discussion Topic.
   */
  public void setAutoMarkThreadsRead(String autoMarkThreadsRead)
  {
    if (log.isDebugEnabled()) 
    {
      log.debug("setAutoMarkThreadsRead(String " + autoMarkThreadsRead + ")");
    }
    
    topic.setAutoMarkThreadsRead(Boolean.parseBoolean(autoMarkThreadsRead));
  }
  
  /**
   * @return Returns the postFirst status.
   */
  public String getPostFirst()
  {
    log.debug("getPostFirst()");
    if ("".equals(postFirst)){
	    if (topic == null || topic.getPostFirst() == null
	        || topic.getPostFirst().booleanValue() == false)
	    {
	    	postFirst = Boolean.FALSE.toString();
	    }
	    else
	    {
	    	postFirst = Boolean.TRUE.toString();
	    }
    }
    return postFirst;
  }
  
  /**
   * @param postFirst
   * Set the postFirst status.
   */
  public void setPostFirst(String postFirst)
  {
    if(log.isDebugEnabled())
    {
       log.debug("setPostFirst(String "+ postFirst+")");
    }
    if (postFirst.equals(Boolean.TRUE.toString()))
    {
      topic.setPostFirst(Boolean.valueOf(true));
    }
    else
    {
      topic.setPostFirst(Boolean.valueOf(false));
    }
  }

  /**
   * @return Returns the postAnonymous status.
   */
  public String getPostAnonymous()
  {
    log.debug("getPostAnonymous()");
    if ("".equals(postAnonymous))
    {
      boolean isAnonymous = topic != null && topic.getPostAnonymous() != null && topic.getPostAnonymous().booleanValue();
      postAnonymous = Boolean.valueOf(isAnonymous).toString();
    }
    return postAnonymous;
  }

  /**
   * @param postAnonymous
   * Set the postAnonymous status
   */
  public void setPostAnonymous(String postAnonymous)
  {
    if (log.isDebugEnabled())
    {
      log.debug("setPostAnonymous(String " + postAnonymous + ")");
    }
    boolean isAnonymous = Boolean.TRUE.toString().equals(postAnonymous);
    topic.setPostAnonymous(Boolean.valueOf(isAnonymous));
  }

  /**
   * @return Returns the revealIDsToRoles status.
   */
  public String getRevealIDsToRoles()
  {
    log.debug("getRevealIDsToRoles()");
    if ("".equals(revealIDsToRoles))
    {
      boolean isRevealIDsToRoles = topic != null && topic.getRevealIDsToRoles() != null && topic.getRevealIDsToRoles().booleanValue();
      revealIDsToRoles = Boolean.valueOf(isRevealIDsToRoles).toString();
    }
    return revealIDsToRoles;
  }

  /**
   * @param revelIDsToRoles
   * Set the revealIDsToRoles status
   */
  public void setRevealIDsToRoles(String revealIDsToRoles)
  {
    if (log.isDebugEnabled())
    {
      log.debug("setRevealIDsToRoles(String " + revealIDsToRoles + ")");
    }
    boolean isRevealIDsToRoles = Boolean.TRUE.toString().equals(revealIDsToRoles);
    topic.setRevealIDsToRoles(Boolean.valueOf(isRevealIDsToRoles));
  }
    

  /**
   * Returns boolean value of autoMarkThreadsRead value.
   */
  public Boolean getTopicAutoMarkThreadsRead()
  {
    log.debug("getTopicAutoMarkThreadsRead()");
    if (topic == null || topic.getAutoMarkThreadsRead() == null) {
      return false;
    } else {
      return topic.getAutoMarkThreadsRead();
    }
  }
  
  /**
   * Set the boolean autoMarkThreadsRead value for this Discussion Topic.
   */
  public void setTopicAutoMarkThreadsRead(Boolean autoMarkThreadsRead)
  {
    if (log.isDebugEnabled()) 
    {
      log.debug("setTopicAutoMarkThreadsRead(String " + autoMarkThreadsRead + ")");
    }    
    topic.setAutoMarkThreadsRead(autoMarkThreadsRead);
  }
  
  /**
   * @return Returns boolean value of postFirst status.
   */
  public Boolean getTopicPostFirst()
  {
    log.debug("getTopicPostFirst()");
    if ("".equals(postFirst)){
	    if (topic == null || topic.getPostFirst() == null
	        || topic.getPostFirst().booleanValue() == false)
	    {
	    	postFirst = Boolean.FALSE.toString();
	    }
	    else
	    {
	    	postFirst = Boolean.TRUE.toString();
	    }
    }
    return Boolean.parseBoolean(postFirst);
  }
  
  /**
   * @param Boolean postFirst
   * Set the postFirst status.
   */
  public void setTopicPostFirst(Boolean postFirst)
  {
    if(log.isDebugEnabled())
    {
       log.debug("setTopicPostFirst(String "+ postFirst+")");
    }
    topic.setPostFirst(postFirst);    
  }

  /**
   * @return Returns boolean value of postAnonymous status.
   */
  public Boolean getTopicPostAnonymous()
  {
    log.debug("getTopicPostAnonymous()");
    if ("".equals(postAnonymous))
    {
      boolean isPostAnonymous = topic != null && topic.getPostAnonymous() != null && topic.getPostAnonymous();
      postAnonymous = Boolean.valueOf(isPostAnonymous).toString();
    }
    return Boolean.parseBoolean(postAnonymous);
  }

  /**
   * @param Boolean postAnonymous
   * Set the postAnonymous staus.
   */
  public void setTopicPostAnonymous(Boolean postAnonymous)
  {
    if (log.isDebugEnabled())
    {
      log.debug("setTopicPostAnonymous(String " + postAnonymous + ")");
    }
    topic.setPostAnonymous(postAnonymous);
  }

  /**
   * @return Returns boolean value of revealIDsToRoles status.
   */
  public Boolean getTopicRevealIDsToRoles()
  {
    log.debug("getTopicRevealIDsToRoles()");
    if ("".equals(revealIDsToRoles))
    {
      boolean isRevealIDsToRoles = topic != null && topic.getRevealIDsToRoles() != null && topic.getRevealIDsToRoles();
      revealIDsToRoles = Boolean.valueOf(isRevealIDsToRoles).toString();
    }
    return Boolean.parseBoolean(revealIDsToRoles);
  }

  /**
   * @param Boolean revealIDsToRoles
   * Set the revealIDsToRoles status.
   */
  public void setTopicRevealIDsToRoles(Boolean revealIDsToRoles)
  {
    if (log.isDebugEnabled())
    {
      log.debug("setTopicRevealIDsToRoles(String " + revealIDsToRoles + ")");
    }
    topic.setRevealIDsToRoles(revealIDsToRoles);
  }
    


  public void removeMessage(DiscussionMessageBean decoMessage)
  {
    if(log.isDebugEnabled())
    {
       log.debug("removeMessage(DiscussionMessageBean"+ decoMessage+")");
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
    log.debug("isMarkForDeletion()");
    return markForDeletion;
  }

  /**
   * @param markForDeletion
   *          The markForDeletion to set.
   */
  public void setMarkForDeletion(boolean markForDeletion)
  {
    if(log.isDebugEnabled())
    {
       log.debug("setMarkForDeletion(boolean "+ markForDeletion+")");
    }
    this.markForDeletion = markForDeletion;
  }

  /**
   * @param topic
   */
  public void setTopic(DiscussionTopic topic)
  {
    if(log.isDebugEnabled())
    {
       log.debug("setTopic(DiscussionTopic"+ topic+")");
    }
    this.topic = topic;
  }

  /**
   * @return Returns the markForDuplication.
   */
  public boolean isMarkForDuplication()
  {
    log.debug("isMarkForDuplication()");
    return markForDuplication;
  }

  /**
   * @param markForDuplication
   *          The markForDuplication to set.
   */
  public void setMarkForDuplication(boolean markForDuplication)
  {
    if(log.isDebugEnabled())
    {
       log.debug("setMarkForDuplication(boolean "+ markForDuplication+")");
    }
    this.markForDuplication = markForDuplication;
  }

  /**
   * @return
   */
  public boolean getIsNewResponse()
  {
    log.debug("getIsNewResponse()");
    if (isNewResponse == null){
    	isNewResponse = uiPermissionsManager.isNewResponse(topic, (DiscussionForum) topic
    			.getBaseForum());
    }
    return isNewResponse.booleanValue();
  }

  /**
   * @return
   */
  public boolean getIsNewResponseToResponse()
  {
    log.debug("getIsNewResponseToResponse()");
    if (isNewResponseToResponse == null){
    	isNewResponseToResponse = uiPermissionsManager.isNewResponseToResponse(topic,
    			(DiscussionForum) topic.getBaseForum());
    }
    return isNewResponseToResponse.booleanValue();
  }

  
  /**
   * @return
   */
  public boolean getIsMovePostings()
  {
    log.debug("getIsMovePostings()");
    if (isMovePostings == null){
    	isMovePostings = uiPermissionsManager.isMovePostings(topic, (DiscussionForum) topic
    			.getBaseForum());
    }
    return isMovePostings.booleanValue();
  }

  /**
   * @return
   */
  public boolean isChangeSettings()
  {
    log.debug("isChangeSettings()");
    if (changeSettings == null){
    	changeSettings = uiPermissionsManager.isChangeSettings(topic, (DiscussionForum) topic
    			.getBaseForum());
    }
    return changeSettings.booleanValue();
  }

  /**
   * @return
   */
  public boolean isPostToGradebook()
  {
    log.debug("isPostToGradebook()");
    if (postToGradebook == null){
    	postToGradebook = uiPermissionsManager.isPostToGradebook(topic,
    			(DiscussionForum) topic.getBaseForum());
    }
    return postToGradebook.booleanValue();
  }
  
  public boolean getIsPostToGradebook()
  {
    log.debug("getIsPostToGradebook()");
    return isPostToGradebook();
  }
  
  /**
   * @return
   */
  public boolean getIsRead()
  {
    log.debug("getIsRead()");
    if (isRead == null){
    	isRead = uiPermissionsManager.isRead(topic, (DiscussionForum) topic
		        .getBaseForum());
    }
    return isRead.booleanValue();
  }

  /**
   * @return
   */
  public boolean getIsReviseAny()
  {
    log.debug("getIsReviseAny()");
    if (isReviseAny == null){
    	isReviseAny = uiPermissionsManager.isReviseAny(topic, (DiscussionForum) topic
		        .getBaseForum());
    }
    return isReviseAny.booleanValue();
  }

  /**
   * @return
   */
  public boolean getIsReviseOwn()
  {
    log.debug("getIsReviseOwn()");
    if (isReviseOwn == null){
    	isReviseOwn = uiPermissionsManager.isReviseOwn(topic, (DiscussionForum) topic
		        .getBaseForum());
    }
    return isReviseOwn.booleanValue();
  }

  /**
   * @return
   */
  public boolean getIsDeleteAny()
  {
    log.debug("getIsDeleteAny()");
    if (isDeleteAny == null){
    	isDeleteAny = uiPermissionsManager.isDeleteAny(topic, (DiscussionForum) topic
		        .getBaseForum());
    }
    return isDeleteAny.booleanValue();
  }

  /**
   * @return
   */
  public boolean getIsDeleteOwn()
  {
    log.debug("getIsDeleteOwn()");
    if (isDeleteOwn == null){
    	isDeleteOwn = uiPermissionsManager.isDeleteOwn(topic, (DiscussionForum) topic
		        .getBaseForum());
    }
    return isDeleteOwn.booleanValue();
  }

  /**
   * @return
   */
  public boolean getIsMarkAsRead()
  {
    log.debug("getIsMarkAsRead()");
    if (isMarkAsRead == null){
    	isMarkAsRead = uiPermissionsManager.isMarkAsRead(topic, (DiscussionForum) topic
		        .getBaseForum());
    }
    return isMarkAsRead.booleanValue();
  }
  
  public boolean getIsModeratedAndHasPerm()
  {
	  log.debug("getIsModeratedAndHasPerm()");
	  if (isModeratedAndHasPerm == null){
	    	isModeratedAndHasPerm = topic.getModerated().booleanValue()
		  	&& uiPermissionsManager.isModeratePostings(topic, (DiscussionForum) topic.getBaseForum());
	  }
	  return isModeratedAndHasPerm.booleanValue();
  }

  /**
   * @return
   */
  public ArrayList getContributorsList()
  {
    log.debug("getContributorsList()");  
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
    log.debug("getAccessorList()");
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
    if(log.isDebugEnabled())
     {
        log.debug("setAccessorList(List"+ accessorList+")");
     }    
    topic.getActorPermissions().setAccessors(forumManager.decodeAccessorsList(accessorList));
  }

  /**
   * @param contributorsList The contributorsList to set.
   */
  public void setContributorsList(ArrayList contributorsList)
  {
    if(log.isDebugEnabled())
    {
       log.debug("setContributorsList(List"+ contributorsList+")");
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

	public boolean getNonePermission()
	{
		if (nonePermission == null){
	/*		if(uiPermissionsManager.isChangeSettings(topic, (DiscussionForum)topic.getBaseForum()) 
					|| uiPermissionsManager.isDeleteAny(topic, (DiscussionForum)topic.getBaseForum())
					|| uiPermissionsManager.isDeleteOwn(topic, (DiscussionForum)topic.getBaseForum())
					|| uiPermissionsManager.isMarkAsRead(topic, (DiscussionForum)topic.getBaseForum())
					|| uiPermissionsManager.isMovePostings(topic, (DiscussionForum)topic.getBaseForum())
					|| uiPermissionsManager.isNewResponse(topic, (DiscussionForum)topic.getBaseForum())
					|| uiPermissionsManager.isNewResponseToResponse(topic, (DiscussionForum)topic.getBaseForum())
					|| uiPermissionsManager.isPostToGradebook(topic, (DiscussionForum)topic.getBaseForum())
					|| uiPermissionsManager.isRead(topic, (DiscussionForum)topic.getBaseForum())
					|| uiPermissionsManager.isReviseAny(topic, (DiscussionForum)topic.getBaseForum())
					|| uiPermissionsManager.isReviseOwn(topic, (DiscussionForum)topic.getBaseForum()))*/
				if(uiPermissionsManager.isChangeSettings(topic, (DiscussionForum)topic.getBaseForum())
						|| uiPermissionsManager.isNewResponse(topic, (DiscussionForum)topic.getBaseForum())
						|| uiPermissionsManager.isRead(topic, (DiscussionForum)topic.getBaseForum()))
				{
					nonePermission = false;
				}
				else
				{
					nonePermission = true;
				}
					
		}
		return nonePermission.booleanValue();
	}

	public void setNonePermission(boolean nonePermission)
	{
		this.nonePermission = nonePermission;
	}
	
	public List getUnreadMessages() {

		ArrayList unreadMessages = new ArrayList();

		for(int i = 0; i < messages.size(); i++) {

			if(!((DiscussionMessageBean) messages.get(i)).isRead()) {

				unreadMessages.add(messages.get(i));

			}
		}

		return unreadMessages;
	}
	
	public List getUnreadMessagesInThreads(){
  		//remove all the threads that have been completely read
  		
  		return recursivelyGetUnreadMessagesInThreads(messages, 0);
	}
	
	public List recursivelyGetUnreadMessagesInThreads(List curList, int depth){
  		List unreadList = new ArrayList();
  		List threadPart = new ArrayList();
  		Boolean foundUnRead = false;
  		DiscussionMessageBean newHead = null;

  		for(int i=0; i<curList.size(); i++){
  			DiscussionMessageBean dmb = (DiscussionMessageBean)curList.get(i);
  			//check either replys to no-one, or replys to current head
  			if(dmb.getDepth() == depth){
  				if(foundUnRead && newHead != null){
  					unreadList.add(newHead);
  					unreadList.addAll(recursivelyGetUnreadMessagesInThreads(threadPart, ++depth));
  				}
  				newHead = dmb;
  				threadPart = new ArrayList();
  				foundUnRead = false;
  			}else
  				threadPart.add(dmb);

  			if(!dmb.isRead()){
  				foundUnRead = true;   
  			}
  		
  		}

  		if(foundUnRead && newHead != null){
  			unreadList.add(newHead);
  			//unreadList.addAll(threadPart);
  			if(threadPart.size() > 0)
  				unreadList.addAll(recursivelyGetUnreadMessagesInThreads(threadPart, ++depth));
  		}
  		return unreadList;

	}

	public List<DecoratedAttachment> getAttachList()
	{
		List<DecoratedAttachment> decoAttachList = new ArrayList<DecoratedAttachment>();
		List<Attachment> attachList = forumManager.getTopicAttachments(topic.getId());  
		if(attachList != null)
		{
			for(int i=0; i<attachList.size(); i++)
			{
				DecoratedAttachment decoAttach = new DecoratedAttachment((Attachment)attachList.get(i));
				decoAttachList.add(decoAttach);
			}
		}
		return decoAttachList;
	}

	public boolean isSorted()
	{
		return sorted;
	}

	public void setSorted(boolean sorted)
	{
		this.sorted = sorted;
	}
	
	public String getAvailabilityRestricted()
	  {
		  log.debug("getAvailabilityRestricted()");
		  if (topic == null || topic.getAvailabilityRestricted() == null || 
				  topic.getAvailabilityRestricted().booleanValue() == false)
		  {
			  return Boolean.FALSE.toString();
		  }

		  return Boolean.TRUE.toString();
	  }
	  
	  /**
	   * Set the "availabilityRestricted" setting for the forum
	   * @param restricted
	   */
	  public void setAvailabilityRestricted(String restricted)
	  {
		  log.debug("setAvailabilityRestricted()");
		  if (restricted.equals(Boolean.TRUE.toString()))
		  {
			  topic.setAvailabilityRestricted(Boolean.valueOf(true));
		  }
		  else
		  {
			  topic.setAvailabilityRestricted(Boolean.valueOf(false));
		  }
	  }
	
	public String getAvailability()
	{
		log.debug("getAvailability()");
		if (topic == null || topic.getAvailability() == null || 
				topic.getAvailability().booleanValue() == false)
		{
			return Boolean.FALSE.toString();
		}

		return Boolean.TRUE.toString();
	}

	/**
	 * Set the "Availability" setting for the forum
	 * @param restricted
	 */
	public void setAvailability(String restricted)
	{
		log.debug("setAvailability()");
		if (restricted.equals(Boolean.TRUE.toString()))
		{
			topic.setAvailability(Boolean.valueOf(true));
		}
		else
		{
			topic.setAvailability(Boolean.valueOf(false));
		}
	}

	public String getOpenDate(){
		if(topic == null || topic.getOpenDate() == null){
			return "";
		}else{
			StringBuilder dateTimeOpenDate = new StringBuilder( datetimeFormat.format( topic.getOpenDate() ) );			
			return dateTimeOpenDate.toString();
		}
	}	  

	public void setOpenDate(String openDateStr){
		if(StringUtils.isNotBlank(openDateStr)) {
			try{
				String hiddenOpenDate = (String)FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("openDateISO8601");
				Date openDate = (Date) datetimeFormat.parse(hiddenOpenDate);
				topic.setOpenDate(openDate);
			}catch (ParseException e) {
				log.error("Couldn't convert open date", e);
			}
		}else{
			topic.setOpenDate(null);
		}
	}

	public String getCloseDate(){
		if(topic == null || topic.getCloseDate() == null){
			return "";
		}else{
			StringBuilder dateTimeCloseDate = new StringBuilder( datetimeFormat.format( topic.getCloseDate() ) );
			return dateTimeCloseDate.toString();
		}
	}	  

	public void setCloseDate(String closeDateStr){
		if(StringUtils.isNotBlank(closeDateStr)) {
			try{
				String hiddenCloseDate = (String)FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("closeDateISO8601");
				Date CloseDate = (Date) datetimeFormat.parse(hiddenCloseDate);
				topic.setCloseDate(CloseDate);
			}catch (ParseException e) {
				log.error("Couldn't convert Close date", e);
			}
		}else{
			topic.setCloseDate(null);
		}
	}
	
	public String getFormattedCloseDate(){
		if(topic == null || topic.getCloseDate() == null){
			return "";
		}else{
			SimpleDateFormat formatter_date = new SimpleDateFormat(rb.getString("date_format"), new ResourceLoader().getLocale());
			formatter_date.setTimeZone(userPreferencesManager.getTimeZone());
			String formattedCloseDate = formatter_date.format(topic.getCloseDate());
			return formattedCloseDate;
		}
	}	

	public String getFormattedOpenDate(){
		if(topic == null || topic.getOpenDate() == null){
			return "";
		}else{
			SimpleDateFormat formatter_date = new SimpleDateFormat(rb.getString("date_format"), new ResourceLoader().getLocale());
			formatter_date.setTimeZone(userPreferencesManager.getTimeZone());
			String formattedOpenDate = formatter_date.format(topic.getOpenDate());
			return formattedOpenDate;
		}
	}
	
}
