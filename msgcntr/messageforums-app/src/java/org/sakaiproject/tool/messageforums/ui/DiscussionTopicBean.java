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
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import javax.faces.context.FacesContext;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.rubrics.api.RubricsConstants;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.util.ResourceLoader;

/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 * @author Chen Wen
 */
@Slf4j
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
  private boolean markForDuplication;
  private List contributorsList = null;
  private List accessorList = null;
  private List<DecoratedAttachment> attachList = new ArrayList<>();
  private String gradeAssign;
  private Boolean nonePermission = null;
  private boolean sorted = false;
  @Getter @Setter private boolean createTask = false;
  @Getter @Setter private boolean selected = false;

  
  private Boolean isRead = null;
  private Boolean isReviseAny = null; 
  private Boolean isReviseOwn = null;
  private Boolean isDeleteAny = null;
  private Boolean isDeleteOwn = null;
  private Boolean isMarkAsNotRead = null;
  private Boolean isModeratedAndHasPerm = null;
  private Boolean isModeratePostings = null;

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
  private Boolean allowEmailNotifications = null;
  private Boolean includeContentsInEmails = null;
  private String mustRespondBeforeReading = "";
  private String parentForumId = "";
  
  private String openDate = "";
  private String closeDate = "";

  private SimpleDateFormat datetimeFormat;
  
  private static final String MESSAGECENTER_BUNDLE = "org.sakaiproject.api.app.messagecenter.bundle.Messages";
  private static final ResourceLoader rb = new ResourceLoader(MESSAGECENTER_BUNDLE);

  private DiscussionForumManager forumManager;
  private RubricsService rubricsService;
  private UserTimeService userTimeService;

  private List<DiscussionMessageBean> messages = new ArrayList<>();

  public DiscussionTopicBean(DiscussionTopic topic, DiscussionForum forum, DiscussionForumManager forumManager, RubricsService rubricsService, UserTimeService userTimeService)
  {
    this.topic = topic;
    this.topic.setBaseForum(forum);
    this.forumManager = forumManager;
    this.rubricsService = rubricsService;
    this.userTimeService = userTimeService;
    datetimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    datetimeFormat.setTimeZone(userTimeService.getLocalTimeZone());
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
    log.debug("setMessages(List{})", messages);
    this.messages = messages;
  }

  public void addMessage(DiscussionMessageBean decoMessage)
  {
    log.debug("addMessage(DiscussionMessageBean{})", decoMessage);
    if (!messages.contains(decoMessage))
    {
      messages.add(decoMessage);
    }
  }

  public void insertMessage(DiscussionMessageBean decoMessage)
  {
    log.debug("insertMessage(DiscussionMessageBean{})", decoMessage);
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
    return topic.getExtendedDescription() != null
            && topic.getExtendedDescription().trim().length() > 0
            && (!readFullDesciption);
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
    log.debug("setReadFullDesciption(boolean {})", readFullDesciption);
    this.readFullDesciption = readFullDesciption;
  }
 
  /**
   * @return Returns the parentForumId.
   */
  public String getParentForumId()
  {
    log.debug("getParentForumId()");
    if (StringUtils.isBlank(parentForumId)) {
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
    if (StringUtils.isBlank(mustRespondBeforeReading)) {
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
    log.debug("setMustRespondBeforeReading(String{})", mustRespondBeforeReading);
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
    if (StringUtils.isBlank(locked)){
	    if (topic == null || topic.getLocked() == null || !topic.getLocked())
	    {
	      locked = Boolean.FALSE.toString();
	    }
	    else
	    {
	      locked =  Boolean.TRUE.toString();
	    }
    }

    handleLockedAfterClosedCondition();

    return locked;
  }

  /**
   * @param locked
   *          The locked to set.
   */
  public void setLocked(String locked)
  {
    log.debug("setLocked(String {})", locked);
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
    return topic != null && topic.getLocked() != null && topic.getLocked();
  }

  /**
   * @param locked
   */
  public void setTopicLocked(Boolean locked)
  {
    log.debug("setTopicLocked(Boolean {})", locked);
    topic.setLocked(locked);
    this.locked = String.valueOf(Boolean.TRUE.equals(locked));
  }

  private void handleLockedAfterClosedCondition(){
    Boolean availabilityRestricted = getTopic().getAvailabilityRestricted();

    if (Boolean.TRUE.equals(availabilityRestricted) && Boolean.FALSE.toString().equals(locked)) {
      Date closeDate = getTopic().getCloseDate();
      if (closeDate != null && Boolean.TRUE.equals(getTopic().getLockedAfterClosed()) && closeDate.before(new Date())) {
        locked = Boolean.TRUE.toString();
      }
    }
  }
  
  
  /**
   * @return Returns the moderated status.
   */
  public String getModerated()
  {
    log.debug("getModerated()");
    if (StringUtils.isBlank(moderated)){
	    if (topic == null || topic.getModerated() == null || !topic.getModerated())
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
    log.debug("setModerated(String {})", moderated);
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
    if (StringUtils.isBlank(moderated)){
	    if (topic == null || topic.getModerated() == null || !topic.getModerated())
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
   * @param moderated
   * Set the moderated status.
   */
  public void setTopicModerated(Boolean moderated)
  {
    log.debug("setTopicModerated(String {})", moderated);
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
    log.debug("setAutoMarkThreadsRead(String {})", autoMarkThreadsRead);
    topic.setAutoMarkThreadsRead(Boolean.parseBoolean(autoMarkThreadsRead));
  }
  
  /**
   * @return Returns the postFirst status.
   */
  public String getPostFirst()
  {
    log.debug("getPostFirst()");
    if (StringUtils.isBlank(postFirst)){
	    if (topic == null || topic.getPostFirst() == null || !topic.getPostFirst())
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
    log.debug("setPostFirst(String {})", postFirst);
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
    if (StringUtils.isBlank(postAnonymous))
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
    log.debug("setPostAnonymous(String {})", postAnonymous);
    boolean isAnonymous = Boolean.TRUE.toString().equals(postAnonymous);
    topic.setPostAnonymous(Boolean.valueOf(isAnonymous));
  }

  /**
   * @return Returns the revealIDsToRoles status.
   */
  public String getRevealIDsToRoles()
  {
    log.debug("getRevealIDsToRoles()");
    if (StringUtils.isBlank(revealIDsToRoles))
    {
      boolean isRevealIDsToRoles = topic != null && topic.getRevealIDsToRoles() != null && topic.getRevealIDsToRoles();
      revealIDsToRoles = Boolean.valueOf(isRevealIDsToRoles).toString();
    }
    return revealIDsToRoles;
  }

  /**
   * @param revealIDsToRoles
   * Set the revealIDsToRoles status
   */
  public void setRevealIDsToRoles(String revealIDsToRoles)
  {
    log.debug("setRevealIDsToRoles(String {})", revealIDsToRoles);
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
    log.debug("setTopicAutoMarkThreadsRead(String {})", autoMarkThreadsRead);
    topic.setAutoMarkThreadsRead(autoMarkThreadsRead);
  }
  
  /**
   * @return Returns boolean value of postFirst status.
   */
  public Boolean getTopicPostFirst()
  {
    log.debug("getTopicPostFirst()");
    if (StringUtils.isBlank(postFirst)){
	    if (topic == null || topic.getPostFirst() == null || !topic.getPostFirst())
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
   * @param postFirst
   * Set the postFirst status.
   */
  public void setTopicPostFirst(Boolean postFirst)
  {
    log.debug("setTopicPostFirst(String {})", postFirst);
    topic.setPostFirst(postFirst);
  }

  /**
   * @return Returns boolean value of postAnonymous status.
   */
  public Boolean getTopicPostAnonymous()
  {
    log.debug("getTopicPostAnonymous()");
    if (StringUtils.isBlank(postAnonymous))
    {
      boolean isPostAnonymous = topic != null && topic.getPostAnonymous() != null && topic.getPostAnonymous();
      postAnonymous = Boolean.valueOf(isPostAnonymous).toString();
    }
    return Boolean.parseBoolean(postAnonymous);
  }

  /**
   * @param postAnonymous
   * Set the postAnonymous staus.
   */
  public void setTopicPostAnonymous(Boolean postAnonymous)
  {
    log.debug("setTopicPostAnonymous(String {})", postAnonymous);
    topic.setPostAnonymous(postAnonymous);
  }

  /**
   * @return Returns boolean value of revealIDsToRoles status.
   */
  public Boolean getTopicRevealIDsToRoles()
  {
    log.debug("getTopicRevealIDsToRoles()");
    if (StringUtils.isBlank(revealIDsToRoles))
    {
      boolean isRevealIDsToRoles = topic != null && topic.getRevealIDsToRoles() != null && topic.getRevealIDsToRoles();
      revealIDsToRoles = Boolean.valueOf(isRevealIDsToRoles).toString();
    }
    return Boolean.parseBoolean(revealIDsToRoles);
  }

  /**
   * @param revealIDsToRoles
   * Set the revealIDsToRoles status.
   */
  public void setTopicRevealIDsToRoles(Boolean revealIDsToRoles)
  {
    log.debug("setTopicRevealIDsToRoles(String {})", revealIDsToRoles);
    topic.setRevealIDsToRoles(revealIDsToRoles);
  }
    


  public void removeMessage(DiscussionMessageBean decoMessage)
  {
    log.debug("removeMessage(DiscussionMessageBean{})", decoMessage);

    final Long id = decoMessage.getMessage().getId();
    Predicate<DiscussionMessageBean> isIdEqual = m -> m.getMessage().getId().equals(id);
    messages.removeIf(isIdEqual);
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
    log.debug("setMarkForDeletion(boolean {})", markForDeletion);
    this.markForDeletion = markForDeletion;
  }

  /**
   * @param topic
   */
  public void setTopic(DiscussionTopic topic)
  {
    log.debug("setTopic(DiscussionTopic{})", topic);
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
    log.debug("setMarkForDuplication(boolean {})", markForDuplication);
    this.markForDuplication = markForDuplication;
  }

  /**
   * @return
   */
  public boolean getIsNewResponse()
  {
    log.debug("getIsNewResponse()");
    return isNewResponse != null ? isNewResponse : false;
  }

  public void setIsNewResponse(Boolean isNewResponse) {
    log.debug("setIsNewResponse({})", isNewResponse);
    this.isNewResponse = isNewResponse;
  }
  /**
   * @return
   */
  public boolean getIsNewResponseToResponse()
  {
    log.debug("getIsNewResponseToResponse()");
    return isNewResponseToResponse != null ? isNewResponseToResponse : false;
  }

  public void setIsNewResponseToResponse(Boolean isNewResponseToResponse) {
    log.debug("setIsNewResponseToResponse({})", isNewResponseToResponse);
    this.isNewResponseToResponse = isNewResponseToResponse;
  }

  /**
   * @return
   */
  public boolean getIsMovePostings()
  {
    log.debug("getIsMovePostings()");
    return isMovePostings != null ? isMovePostings : false;
  }

  public void setIsMovePostings(Boolean isMovePostings) {
    log.debug("setMovePostings({})", isMovePostings);
    this.isMovePostings = isMovePostings;
  }
  /**
   * @return
   */
  public boolean isChangeSettings()
  {
    log.debug("isChangeSettings()");
    return changeSettings != null ? changeSettings : false;
  }

  public void setChangeSettings(Boolean changeSettings) {
    log.debug("setChangeSettings({})", changeSettings);
    this.changeSettings = changeSettings;
  }

  /**
   * @return
   */
  public boolean isPostToGradebook()
  {
    log.debug("isPostToGradebook()");
    return postToGradebook != null ? postToGradebook : false;
  }

  public void setPostToGradebook(Boolean postToGradebook) {
    log.debug("setPostToGradebook({})", postToGradebook);
    this.postToGradebook = postToGradebook;
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
    return isRead != null ? isRead : false;
  }

  public void setIsRead(Boolean isRead) {
    log.debug("setIsRead({})", isRead);
    this.isRead = isRead;
  }

  public boolean getIsReviseAny()
  {
    log.debug("getIsReviseAny()");
    return isReviseAny != null ? isReviseAny : false;
  }

  public void setIsReviseAny(Boolean isReviseAny) {
    log.debug("setIsReviseAny({})", isReviseAny);
    this.isReviseAny = isReviseAny;
  }

  public boolean getIsReviseOwn()
  {
    log.debug("getIsReviseOwn()");
    return isReviseOwn != null ? isReviseOwn : false;
  }

  public void setIsReviseOwn(Boolean isReviseOwn) {
    log.debug("setIsReviseOwn({})", isReviseOwn);
    this.isReviseOwn = isReviseOwn;
  }

  public boolean getIsDeleteAny()
  {
    log.debug("getIsDeleteAny()");
    return isDeleteAny != null ? isDeleteAny : false;
  }

  public void setIsDeleteAny(Boolean isDeleteAny) {
    log.debug("setIsDeleteAny({})", isDeleteAny);
    this.isDeleteAny = isDeleteAny;
  }

  public boolean getIsDeleteOwn()
  {
    log.debug("getIsDeleteOwn()");
    return isDeleteOwn != null ? isDeleteOwn : false;
  }

  public void setIsDeleteOwn(Boolean isDeleteOwn) {
    log.debug("setIsDeleteOwn({})", isDeleteOwn);
    this.isDeleteOwn = isDeleteOwn;
  }

  public boolean getIsMarkAsNotRead()
  {
    log.debug("getIsMarkAsNotRead()");
    return isMarkAsNotRead != null ? isMarkAsNotRead : false;
  }

  public void setIsMarkAsNotRead(Boolean isMarkAsNotRead) {
    log.debug("setIsMarkAsNotRead({})", isMarkAsNotRead);
    this.isMarkAsNotRead = isMarkAsNotRead;
  }

  public boolean getIsModeratedAndHasPerm()
  {
	  log.debug("getIsModeratedAndHasPerm()");
	  return isModeratedAndHasPerm != null ? isModeratedAndHasPerm : false;
  }

  public void setIsModeratedAndHasPerm(Boolean isModeratedAndHasPerm) {
    log.debug("setIsModeratedAndHasPerm({})", isModeratedAndHasPerm);
    this.isModeratedAndHasPerm = isModeratedAndHasPerm;
  }

  public boolean getIsModeratePostings()
  {
	  log.debug("getIsModerated()");
	  return isModeratePostings != null ? isModeratePostings : false;
  }

  public void setIsModeratePostings(Boolean isModeratePostings) {
    log.debug("setIsModerated({})", isModeratePostings);
    this.isModeratePostings = isModeratePostings;
  }

  public List<String> getContributorsList() {
    log.debug("getContributorsList()");
    if (contributorsList == null) {
      contributorsList = forumManager.getContributorsList(topic, (DiscussionForum) topic.getBaseForum());
    }
    return contributorsList;

  }

  public void setContributorsList(List<String> contributorsList) {
    log.debug("setContributorsList(List{})", contributorsList);
    if (!contributorsList.equals(this.contributorsList)) {
      this.contributorsList = contributorsList;
      topic.getActorPermissions().setContributors(forumManager.decodeContributorsList(contributorsList));
    }
  }

  public List<String> getAccessorList()
  {
    log.debug("getAccessorList()");
    if (accessorList == null) {
      accessorList = forumManager.getAccessorsList(topic, (DiscussionForum) topic.getBaseForum());
     }
    return accessorList; 
  }

  /**
   * @param accessorList The accessorList to set.
   */
  public void setAccessorList(List accessorList)
  {    
    if(log.isDebugEnabled())
     {
        log.debug("setAccessorList(List"+ accessorList+")");
     }    
    topic.getActorPermissions().setAccessors(forumManager.decodeAccessorsList(accessorList));
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
					|| uiPermissionsManager.isMarkAsNotRead(topic, (DiscussionForum)topic.getBaseForum())
					|| uiPermissionsManager.isMovePostings(topic, (DiscussionForum)topic.getBaseForum())
					|| uiPermissionsManager.isNewResponse(topic, (DiscussionForum)topic.getBaseForum())
					|| uiPermissionsManager.isNewResponseToResponse(topic, (DiscussionForum)topic.getBaseForum())
					|| uiPermissionsManager.isPostToGradebook(topic, (DiscussionForum)topic.getBaseForum())
					|| uiPermissionsManager.isRead(topic, (DiscussionForum)topic.getBaseForum())
					|| uiPermissionsManager.isReviseAny(topic, (DiscussionForum)topic.getBaseForum())
					|| uiPermissionsManager.isReviseOwn(topic, (DiscussionForum)topic.getBaseForum()))*/
				if(changeSettings || isNewResponse || isRead)
				{
					nonePermission = false;
				}
				else
				{
					nonePermission = true;
				}
					
		}
		return nonePermission;
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

    public List<DecoratedAttachment> getAttachList() {
      return attachList;
    }

    // TODO ERN this needs to be verified that it is getting the right info
	public void setAttachList(List<DecoratedAttachment> attachList) {
      this.attachList = attachList;
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
				// Get the ISO8601 value directly from the request
				String hiddenOpenDate = (String)FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("openDateISO8601");
				if (StringUtils.isNotBlank(hiddenOpenDate)) {
					Date openDate = (Date) datetimeFormat.parse(hiddenOpenDate);
					topic.setOpenDate(openDate);
				}
			} catch (ParseException e) {
				log.error("Couldn't convert open date", e);
			}
		} else {
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
				// Get the ISO8601 value directly from the request
				String hiddenCloseDate = (String)FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("closeDateISO8601");
				if (StringUtils.isNotBlank(hiddenCloseDate)) {
					Date closeDate = (Date) datetimeFormat.parse(hiddenCloseDate);
					topic.setCloseDate(closeDate);
				}
			} catch (ParseException e) {
				log.error("Couldn't convert close date", e);
			}
		} else {
			topic.setCloseDate(null);
		}
	}
	
	public String getFormattedCloseDate(){
		if(topic == null || topic.getCloseDate() == null){
			return "";
		}else{
			SimpleDateFormat formatter_date = new SimpleDateFormat(rb.getString("date_format"), new ResourceLoader().getLocale());
			formatter_date.setTimeZone(userTimeService.getLocalTimeZone());
			String formattedCloseDate = formatter_date.format(topic.getCloseDate());
			return formattedCloseDate;
		}
	}	

	public String getFormattedOpenDate(){
		if(topic == null || topic.getOpenDate() == null){
			return "";
		}else{
			SimpleDateFormat formatter_date = new SimpleDateFormat(rb.getString("date_format"), new ResourceLoader().getLocale());
			formatter_date.setTimeZone(userTimeService.getLocalTimeZone());
			String formattedOpenDate = formatter_date.format(topic.getOpenDate());
			return formattedOpenDate;
		}
	}

	/**
	 * @return Returns boolean value of allowEmailNotifications
	 */
	public Boolean getTopicAllowEmailNotifications() {
		log.debug("getTopicAllowEmailNotifications()");
		if (allowEmailNotifications == null) {
			if (topic == null || topic.getAllowEmailNotifications() == null) {
				allowEmailNotifications = Boolean.FALSE;
			} else {
				allowEmailNotifications = topic.getAllowEmailNotifications();
			}
		}

		return allowEmailNotifications;
	}

	/**
	 * @param allowEmailNotifications
	 * Set the allowEmailNotifications status.
	 */
	public void setTopicAllowEmailNotifications(Boolean allowEmailNotifications)
	{
		log.debug("setTopicAllowEmailNotifications(Boolean {}", allowEmailNotifications);
		topic.setAllowEmailNotifications(allowEmailNotifications);
	}

	/**
	 * @return Returns boolean value of includeContentsInEmails status.
	 */
	public Boolean getTopicIncludeContentsInEmails() {
		log.debug("getTopicIncludeContentsInEmails()");
		if (includeContentsInEmails == null) {
			if (topic == null || topic.getIncludeContentsInEmails() == null) {
				includeContentsInEmails = Boolean.FALSE;
			} else {
				includeContentsInEmails = topic.getIncludeContentsInEmails();
			}
		}

		return includeContentsInEmails;
	}

	/**
	 * @param includeContentsInEmails
	 * Set the includeContentsInEmails status.
	 */
	public void setTopicIncludeContentsInEmails(Boolean includeContentsInEmails) {
		log.debug("setTopicIncludeContentsInEmails(Boolean {})", includeContentsInEmails);
		topic.setIncludeContentsInEmails(includeContentsInEmails);
	}

	/**
	 * Return whether or not the topic will use specific group permissions.
	 */
	public String getRestrictPermissionsForGroups() {
		log.debug("getRestrictPermissionsForGroups()");
		return Boolean.toString(topic.getRestrictPermissionsForGroups());
	}

	/**
	 * Set the restrictPermissionsForGroups setting for the topic.
	 */
	public void setRestrictPermissionsForGroups(String restrictPermissionsForGroups) {
		log.debug("setRestrictPermissionsForGroups()");
		topic.setRestrictPermissionsForGroups(Boolean.parseBoolean(restrictPermissionsForGroups));
	}
	public String getHasRubric(){
		return rubricsService.hasAssociatedRubric(RubricsConstants.RBCS_TOOL_GRADEBOOKNG, topic.getDefaultAssignName()) ? Boolean.TRUE.toString() : Boolean.FALSE.toString();
	}
}
