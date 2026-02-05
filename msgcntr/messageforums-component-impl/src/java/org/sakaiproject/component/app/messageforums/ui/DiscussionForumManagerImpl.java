/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-component-impl/src/java/org/sakaiproject/component/app/messageforums/ui/DiscussionForumManagerImpl.java $
 * $Id: DiscussionForumManagerImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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
package org.sakaiproject.component.app.messageforums.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.sakaiproject.api.app.messageforums.ActorPermissions;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaControlPermission;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.DBMembershipItem;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.DummyDataHelperApi;
import org.sakaiproject.api.app.messageforums.ForumControlPermission;
import org.sakaiproject.api.app.messageforums.MembershipItem;
import org.sakaiproject.api.app.messageforums.MembershipManager;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.MessageForumsUser;
import org.sakaiproject.api.app.messageforums.MessagePermissions;
import org.sakaiproject.api.app.messageforums.PermissionLevel;
import org.sakaiproject.api.app.messageforums.PermissionLevelManager;
import org.sakaiproject.api.app.messageforums.PermissionManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.TopicControlPermission;
import org.sakaiproject.api.app.messageforums.events.ForumsMessageEventParams;
import org.sakaiproject.api.app.messageforums.events.ForumsTopicEventParams;
import org.sakaiproject.api.app.messageforums.events.ForumsTopicEventParams.TopicEvent;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.app.messageforums.dao.hibernate.ActorPermissionsImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.DBMembershipItemImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.MessageForumsUserImpl;
import org.sakaiproject.component.app.messageforums.ui.delegates.LRSDelegate;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.LearningResourceStoreService;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Statement;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Verb.SAKAI_VERB;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tasks.api.Task;
import org.sakaiproject.tasks.api.TaskService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 */
@Slf4j
public class DiscussionForumManagerImpl extends HibernateDaoSupport implements
    DiscussionForumManager {
  private static final String MC_DEFAULT = "mc.default.";
  private AreaManager areaManager;
  private MessageForumsForumManager forumManager;
  private MessageForumsMessageManager messageManager;
  private DummyDataHelperApi helper;
  private PermissionManager permissionManager;
  private MessageForumsTypeManager typeManager;
  private SiteService siteService;
  private UserDirectoryService userDirectoryService;
  private MembershipManager membershipManager;
  private SecurityService securityService;
  private SessionManager sessionManager;
  private PermissionLevelManager permissionLevelManager;
  private AuthzGroupService authzGroupService;
  private boolean usingHelper = false; // just a flag until moved to database from helper
  private ContentHostingService contentHostingService;
  private MemoryService memoryService;
  private Cache<String, Set<String>> allowedFunctionsCache;
  private EventTrackingService eventTrackingService;
  private ToolManager toolManager;
  private LearningResourceStoreService learningResourceStoreService;
  @Setter private UIPermissionsManager uiPermissionsManager;
  
  public static final int MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST = 1000;

  public void init()
  {
     log.info("init()");
     allowedFunctionsCache = memoryService.getCache("org.sakaiproject.component.app.messageforums.ui.DiscussionForumManagerImpl.allowedFunctionsCache");
  }

  public void setContentHostingService(ContentHostingService contentHostingService) {
	  this.contentHostingService = contentHostingService;
  }

  public void setAuthzGroupService(AuthzGroupService authzGroupService) {
    this.authzGroupService = authzGroupService;
  }
  
  public void setEventTrackingService(EventTrackingService eventTrackingService) {
	this.eventTrackingService = eventTrackingService;
  }

  public void setLearningResourceStoreService(LearningResourceStoreService service) {
	learningResourceStoreService = service;
  }

  public void setToolManager(ToolManager toolManager) {
	this.toolManager = toolManager;
  }

  public List searchTopicMessages(Long topicId, String searchText)
  {
    return forumManager.searchTopicMessages(topicId, searchText);
  }

  public Topic getTopicByIdWithAttachments(Long topicId)
  {
    if (log.isDebugEnabled())
    {
      log.debug("getTopicByIdWithAttachments(Long " + topicId + ")");
    }
    return forumManager.getTopicByIdWithAttachments(topicId);
  }

  public List getTopicsByIdWithMessages(final Long forumId)
  {
    if (log.isDebugEnabled())
    {
      log.debug("getTopicsByIdWithMessages(final Long" + forumId + ")");
    }
    return forumManager.getTopicsByIdWithMessages(forumId);
  }

  public List getTopicsByIdWithMessagesAndAttachments(final Long forumId)
  {
    if (log.isDebugEnabled())
    {
      log.debug("getTopicsByIdWithMessagesAndAttachments(final Long" + forumId
          + ")");
    }
    return forumManager.getTopicsByIdWithMessagesAndAttachments(forumId);
  }
  
  public List getTopicsByIdWithMessagesMembershipAndAttachments(final Long forumId)
  {
    if (log.isDebugEnabled())
    {
      log.debug("getTopicsByIdWithMessagesMembershipAndAttachments(final Long" + forumId
          + ")");
    }
    return forumManager.getTopicsByIdWithMessagesMembershipAndAttachments(forumId);
  }

  /*
   * (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getForumsForMainPage()
   */
  public List<DiscussionForum> getForumsForMainPage() {
    if (log.isDebugEnabled()) {
      log.debug("getForumsForMainPage()");
    }
    return forumManager.getForumsForMainPage();
  }
  
  /*
   * (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getMessageCountsForMainPage(java.util.List)
   */
  public List<Object[]> getMessageCountsForMainPage(Collection<Long> topicIds) {
    if (log.isDebugEnabled()) {
      log.debug("getMessageCountsForMainPage(" + topicIds + ")");
    }
    return messageManager.findMessageCountsForMainPage(topicIds);
  }

  /*
   * (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getMessageCountsForMainPage(java.util.Collection)
   */
  public List<Object[]> getReadMessageCountsForMainPage(Collection<Long> topicIds) {
    if (log.isDebugEnabled()) {
      log.debug("getReadMessageCountsForMainPage(" + topicIds + ")");
    }
    return messageManager.findReadMessageCountsForMainPage(topicIds);
  }
  
  public Topic getTopicByIdWithMessages(final Long topicId)
  {
    if (log.isDebugEnabled())
    {
      log.debug("getTopicByIdWithMessages(final Long" + topicId + ")");
    }
    return forumManager.getTopicByIdWithMessages(topicId);
  }
  
  public Topic getTopicWithAttachmentsById(final Long topicId)
  {
    if (log.isDebugEnabled())
    {
      log.debug("getTopicWithAttachmentsById(final Long" + topicId + ")");
    }
    return forumManager.getTopicWithAttachmentsById(topicId);
  }

  public Topic getTopicByIdWithMessagesAndAttachments(final Long topicId)
  {
    if (log.isDebugEnabled())
    {
      log.debug("getTopicByIdWithMessagesAndAttachments(final Long" + topicId
          + ")");
    }
    return forumManager.getTopicByIdWithMessagesAndAttachments(topicId);
  }
  
  public List getModeratedTopicsInSite()
  {
	  if (log.isDebugEnabled())
	  {
		  log.debug("getModeratedTopicsInSite()");
	  }
	  return forumManager.getModeratedTopicsInSite(toolManager.getCurrentPlacement().getContext());
  }

  // start injection
  /**
   * @param helper
   */
  public void setHelper(DummyDataHelperApi helper)
  {
    if (log.isDebugEnabled())
    {
      log.debug("setHelper(DummyDataHelperApi " + helper + ")");
    }
    this.helper = helper;
  }

  /**
   * @param areaManager
   */
  public void setAreaManager(AreaManager areaManager)
  {
    if (log.isDebugEnabled())
    {
      log.debug("setAreaManager(AreaManager" + areaManager + ")");
    }
    this.areaManager = areaManager;
  }

  /**
   * @param permissionManager
   *          The permissionManager to set.
   */
  public void setPermissionManager(PermissionManager permissionManager)
  {
    if (log.isDebugEnabled())
    {
      log.debug("setPermissionManager(PermissionManager" + permissionManager
          + ")");
    }
    this.permissionManager = permissionManager;
  }

  /**
   * @param permissionLevelManager
   *          The permissionLevelManager to set.
   */
  public void setPermissionLevelManager(
      PermissionLevelManager permissionLevelManager)
  {
    this.permissionLevelManager = permissionLevelManager;
  }

  /**
   * @param typeManager
   *          The typeManager to set.
   */
  public void setTypeManager(MessageForumsTypeManager typeManager)
  {
    if (log.isDebugEnabled())
    {
      log.debug("setTypeManager(MessageForumsTypeManager" + typeManager + ")");
    }
    this.typeManager = typeManager;
  }

  /**
   * @param siteService
   *          The siteService to set.
   */
  public void setSiteService(SiteService siteService)
  {
    this.siteService = siteService;
  }

  /**
   * @param sessionManager
   *          The sessionManager to set.
   */
  public void setSessionManager(SessionManager sessionManager)
  {
    this.sessionManager = sessionManager;
  }

  /**
   * @param securityService
   *          The securityService to set.
   */
  public void setSecurityService(SecurityService securityService)
  {
    this.securityService = securityService;
  }

  /**
   * @param userDirectoryService
   *          The userDirectoryService to set.
   */
  public void setUserDirectoryService(UserDirectoryService userDirectoryService)
  {
    this.userDirectoryService = userDirectoryService;
  }

  /**
   * @param membershipManager
   *          The membershipManager to set.
   */
  public void setMembershipManager(MembershipManager membershipManager)
  {
    this.membershipManager = membershipManager;
  }

  /**
   * @return
   */
  public MessageForumsMessageManager getMessageManager()
  {

    log.debug("getMessageManager()");

    return messageManager;
  }

  /**
   * @param messageManager
   */
  public void setMessageManager(MessageForumsMessageManager messageManager)
  {
    if (log.isDebugEnabled())
    {
      log.debug("setMessageManager(MessageForumsMessageManager"
          + messageManager + ")");
    }
    this.messageManager = messageManager;
  }

  // end injection

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getDiscussionForumArea()
   */
  public Area getDiscussionForumArea()
  {
	return getDiscussionForumArea(toolManager.getCurrentPlacement().getContext());  
  }
  
  public Area getDiscussionForumArea(String siteId)
  {
    log.debug("getDiscussionForumArea");

    if (usingHelper)
    {
      return helper.getDiscussionForumArea();
    }
    return areaManager.getDiscussionArea(siteId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getMessageById(java.lang.Long)
   */
  public Message getMessageById(Long id)
  {
    if (log.isDebugEnabled())
    {
      log.debug("getMessageById( Long" + id + ")");
    }
    if (usingHelper)
    {
      return helper.getMessageById(id);
    }
    return messageManager.getMessageById(id);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#saveMessage(org.sakaiproject.api.app.messageforums.Message)
   */
  @Override
  public Message saveMessage(Message message) {
      return saveMessage(message, null, false);
  }

  @Override
  public Message saveMessage(Message message, ForumsMessageEventParams params) {
      return saveMessage(message, params, false);
  }

  @Override
  public Message saveMessage(Message message, ForumsMessageEventParams params, boolean ignoreLockedTopicForum) {
    if (log.isDebugEnabled())
    {
      log.debug("saveMessage(Message " + message + ")");
    }
    if (message.getTopic().getBaseForum() == null)
    {
      message.setTopic(getTopicById(message.getTopic().getId()));
    }
    if(this.getAnonRole() && message.getCreatedBy() == null)
    {
    	message.setCreatedBy(".anon");
    }
    if(this.getAnonRole() && message.getModifiedBy() == null)
    {
    	message.setModifiedBy(".anon");
    }

    // save the message first to ensure we have a valid message id
    final Message persistedMessage = messageManager.saveOrUpdateMessage(message, false, ignoreLockedTopicForum);
    if (params != null) {
        Event event = eventTrackingService.newEvent(params.event.type, getEventMessage(persistedMessage), null, params.event.modification,
                NotificationService.NOTI_OPTIONAL, params.lrsStatement);
        eventTrackingService.post(event);
    }

    TaskService taskService = (TaskService) ComponentManager.get("org.sakaiproject.tasks.api.TaskService");
    List<String> userList = new ArrayList<>();
    userList.add(message.getAuthorId());
    // Complete task related to forum 
    String referenceForum = DiscussionForumService.REFERENCE_ROOT + "/" + getCurrentContext() + "/" + message.getTopic().getBaseForum().getId();
    taskService.completeUserTaskByReference(referenceForum, userList);
    // Complete task related to topic 
    String referenceTopic = DiscussionForumService.REFERENCE_ROOT + "/" + getCurrentContext() + "/" + message.getTopic().getBaseForum().getId() + "/topic/" + message.getTopic().getId();
    taskService.completeUserTaskByReference(referenceTopic, userList);

    return persistedMessage;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#deleteMessage(org.sakaiproject.api.app.messageforums.Message)
   */
  public void deleteMessage(Message message)
  {
    if (log.isDebugEnabled())
    {
      log.debug("deleteMessage(Message" + message + ")");
    }
    messageManager.deleteMessage(message);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getTotalNoMessages(org.sakaiproject.api.app.messageforums.Topic)
   */
  public int getTotalNoMessages(Topic topic)
  {
    if (log.isDebugEnabled())
    {
      log.debug("getTotalNoMessages(Topic" + topic + ")");
    }
    if (usingHelper)
    {
      return 20;
    }
    return messageManager.findMessageCountByTopicId(topic.getId());
  }
  
  /*
   * (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getTotalViewableMessagesWhenMod(org.sakaiproject.api.app.messageforums.Topic)
   */
  public int getTotalViewableMessagesWhenMod(Topic topic)
  {
    if (log.isDebugEnabled())
    {
      log.debug("getTotalViewableMessagesWhenMod(Topic" + topic + ")");
    }
    if (usingHelper)
    {
      return 20;
    }
    return messageManager.findViewableMessageCountByTopicId(topic.getId());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getUnreadNoMessages(org.sakaiproject.api.app.messageforums.Topic)
   */
  public int getUnreadNoMessages(Topic topic)
  {
    if (log.isDebugEnabled())
    {
      log.debug("getUnreadNoMessages(Topic" + topic + ")");
    }
    if (usingHelper)
    {
      return 10;
    }
    return messageManager.findUnreadMessageCountByTopicId(topic.getId());
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getUnreadApprovedNoMessages(org.sakaiproject.api.app.messageforums.Topic)
   */
  public int getNumUnreadViewableMessagesWhenMod(Topic topic)
  {
    if (log.isDebugEnabled())
    {
      log.debug("getNumUnreadViewableMessagesWhenMod(Topic" + topic + ")");
    }
    if (usingHelper)
    {
      return 10;
    }
    return messageManager.findUnreadViewableMessageCountByTopicId(topic.getId());
  }
  
  /*
   * (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#approveAllPendingMessages(java.lang.Long)
   */
  public void approveAllPendingMessages(Long topicId)
  {
	  if (topicId == null)
	  {
		  log.error("approveAllPendingMessages failed with topicId: Null" );
          throw new IllegalArgumentException("Null Argument");
	  }
	  List messages = this.getMessagesByTopicId(topicId);
	  if (messages != null && messages.size() > 0)
	  {
		  Iterator msgIter = messages.iterator();
		  while (msgIter.hasNext())
		  {
			  Message msg = (Message) msgIter.next();
			  if (msg.getApproved() == null)
			  {
				  msg.setApproved(Boolean.TRUE);
			  }
		  }
	  }
  }
  
  
  /*
   * (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getTotalNoPendingMessages()
   */
  public List<Message> getPendingMsgsInSiteByMembership(List<String> membershipList, List<Topic> moderatedTopics)
  {
	  return messageManager.getPendingMsgsInSiteByMembership(membershipList, moderatedTopics);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getDiscussionForums()
   */
  public List getDiscussionForums()
  {
    log.debug("getDiscussionForums()");
    if (usingHelper)
    {
      return helper.getDiscussionForumArea().getDiscussionForums();
    }
    return forumManager.getForumByTypeAndContext(typeManager
        .getDiscussionForumType());
    // return getDiscussionForumArea().getDiscussionForums();
  }
  public List getDiscussionForums(String siteId)
  {
    log.debug("getDiscussionForums(siteId)");
    if (usingHelper)
    {
      return helper.getDiscussionForumArea().getDiscussionForums();
    }
    return forumManager.getForumByTypeAndContext(typeManager
        .getDiscussionForumType(), siteId);
    // return getDiscussionForumArea().getDiscussionForums();
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getDiscussionForumsByContextId()
   */
  public List getDiscussionForumsByContextId(String contextId)
  {
    log.debug("getDiscussionForumsByContextId(String contextId)");
    
    return forumManager.getForumByTypeAndContext(typeManager
        .getDiscussionForumType(), contextId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getForumById(java.lang.Long)
   */
  public DiscussionForum getForumById(Long forumId)
  {
    if (log.isDebugEnabled())
    {
      log.debug("getForumById(Long" + forumId + ")");
    }
    if (usingHelper)
    {
      return helper.getForumById(forumId);
    }
    return (DiscussionForum) forumManager.getForumById(true, forumId);
  }

  public DiscussionForum getForumByUuid(String forumId)
  {
    if (log.isDebugEnabled())
    {
      log.debug("getForumByUuid(String" + forumId + ")");
    }
    return (DiscussionForum) forumManager.getForumByUuid(forumId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getMessagesByTopicId(java.lang.Long)
   */
  public List getMessagesByTopicId(Long topicId)
  {
    if (log.isDebugEnabled())
    {
      log.debug("getMessagesByTopicId(Long" + topicId + ")");
    }
    return messageManager.findMessagesByTopicId(topicId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getTopicById(java.lang.Long)
   */
  public DiscussionTopic getTopicById(Long topicId)
  {
    if (log.isDebugEnabled())
    {
      log.debug("getTopicById(Long" + topicId + ")");
    }

    return (DiscussionTopic) forumManager.getTopicById(true, topicId);
  }

  public DiscussionForum getForumByIdWithTopics(Long forumId)
  {
    if (log.isDebugEnabled())
    {
      log.debug("getForumByIdWithTopics(Long" + forumId + ")");
    }
    return (DiscussionForum) forumManager.getForumByIdWithTopics(forumId);
  }
  
  public DiscussionForum getForumByIdWithTopicsAttachmentsAndMessages(Long forumId) {
	  if (log.isDebugEnabled()) { log.debug("getForumByIdWithTopicsAttachmentsAndMessages(Long " + forumId + ")"); }
	  return (DiscussionForum) forumManager.getForumByIdWithTopicsAttachmentsAndMessages(forumId);
  }

  public DiscussionTopic getTopicByUuid(String topicId)
  {
    if (log.isDebugEnabled())
    {
      log.debug(" getTopicByUuid(String" + topicId + ")");
    }
    return (DiscussionTopic) forumManager.getTopicByUuid(topicId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#hasNextTopic(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public boolean hasNextTopic(DiscussionTopic topic)
  {
    if (log.isDebugEnabled())
    {
      log.debug("hasNextTopic(DiscussionTopic" + topic + ")");
    }
    if (usingHelper)
    {
      return helper.hasNextTopic(topic);
    }

    // TODO: Needs optimized
    boolean next = false;
    DiscussionForum forum = getForumById(topic.getBaseForum().getId());
    if (forum != null && forum.getTopics() != null)
    {
      for (Iterator iter = forum.getTopics().iterator(); iter.hasNext();)
      {
        try{
          DiscussionTopic t = (DiscussionTopic) Hibernate.unproxy(iter.next());
          if (next && getTopicAccess(t))
          {
            return true;
          }
          if (t != null && getTopicAccess(t) && t.getId().equals(topic.getId()))
            {
              next = true;
          }
        }catch (Exception e) {
          log.error(e.getMessage());
        }
      }
    }

    // if we get here, there is no next topic
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#hasPreviousTopic(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public boolean hasPreviousTopic(DiscussionTopic topic)
  {
    if (log.isDebugEnabled())
    {
      log.debug("hasPreviousTopic(DiscussionTopic" + topic + ")");
    }
    if (usingHelper)
    {
      return helper.hasPreviousTopic(topic);
    }

    // TODO: Needs optimized
    DiscussionTopic prev = null;
    DiscussionForum forum = getForumById(topic.getBaseForum().getId());
    if (forum != null && forum.getTopics() != null)
    {
      for (Iterator iter = forum.getTopics().iterator(); iter.hasNext();)
      {
        DiscussionTopic t = (DiscussionTopic) Hibernate.unproxy(iter.next());
        if (t != null && getTopicAccess(t))
        {
          if (t.getId().equals(topic.getId()))
          {
            // need to check null because we might be on the first topic
            // which means there is no previous one
            return prev != null;
          }
          prev = (DiscussionTopic) t;
        }
      }
    }

    // if we get here, there is no previous topic
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getNextTopic(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public DiscussionTopic getNextTopic(DiscussionTopic topic)
  {
    if (log.isDebugEnabled())
    {
      log.debug("getNextTopic(DiscussionTopic" + topic + ")");
    }
    if (usingHelper)
    {
      if (hasNextTopic(topic))
      {
        return helper.getNextTopic(topic);
      }
      else
      {
        return null;
      }
    }

    // TODO: Needs optimized and re-written to take advantage of the db... this is really horrible.
    boolean next = false;
    DiscussionForum forum = getForumById(topic.getBaseForum().getId());
    if (forum != null && forum.getTopics() != null)
    {
      for (Iterator iter = forum.getTopics().iterator(); iter.hasNext();)
      {
        DiscussionTopic t = (DiscussionTopic) Hibernate.unproxy(iter.next());
        if (next && getTopicAccess(t))
        {
          if (t == null)
          {
            do
            {
              t = (DiscussionTopic) iter.next();
            } while (t == null);
          }
          return (DiscussionTopic) t;
        }
        if (t != null && getTopicAccess(t))
        {
          if (t.getId().equals(topic.getId()))
          {
            next = true;
          }
        }
      }
    }

    // if we get here, there is no next topic
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getPreviousTopic(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public DiscussionTopic getPreviousTopic(DiscussionTopic topic)
  {
    if (log.isDebugEnabled())
    {
      log.debug("getPreviousTopic(DiscussionTopic" + topic + ")");
    }
    if (usingHelper)
    {
      if (hasPreviousTopic(topic))
      {
        return helper.getPreviousTopic(topic);
      }
      else
      {
        return null;
      }
    }
    // TODO: Needs optimized
    DiscussionTopic prev = null;
    DiscussionForum forum = getForumById(topic.getBaseForum().getId());
    if (forum != null && forum.getTopics() != null)
    {
      for (Iterator iter = forum.getTopics().iterator(); iter.hasNext();)
      {
        DiscussionTopic t = (DiscussionTopic) Hibernate.unproxy(iter.next());
        if (t != null && getTopicAccess(t))
        {
          if (t.getId().equals(topic.getId()))
          {
            return prev;
          }
          if (t != null && getTopicAccess(t))
          {
            prev = (DiscussionTopic) t;
          }
        }
      }
    }

    // if we get here, there is no previous topic
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#isInstructor()
   */
  public boolean isInstructor()
  {
    log.debug("isInstructor()");
    return isInstructor(userDirectoryService.getCurrentUser());
  }
  
  public boolean isInstructor(String userId)
  {
    log.debug("isInstructor()");
    try {
		return isInstructor(userDirectoryService.getUser(userId));
	} catch (UserNotDefinedException e) {
		log.error("DiscussionForumManagerImpl: isInstructor(String userId, String siteId): " + e.getMessage());
		return false;
	}
  }

  public boolean isInstructor(String userId, String siteId) {
    log.debug("isInstructor(userId={}, siteId={})", userId, siteId);
    try {
		return isInstructor(userDirectoryService.getUser(userId), siteId);
	} catch (UserNotDefinedException e) {
		log.debug("DiscussionForumManagerImpl: isInstructor(String userId, String siteId): " + e.getMessage());
		return false;
	}
  }

  /**
   * Check if the given user has site.upd access
   * 
   * @param user
   * @return
   */
  public boolean isInstructor(User user)
  {
    log.debug("isInstructor(User eid: {})", user.getEid());
    if (user != null)
      return isInstructor(user, getContextSiteId());
    else
      return false;
  }
  
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#isInstructor()
   */
  public boolean isSectionTA()
  {
    log.debug("isSectionTA()");
    return isSectionTA(userDirectoryService.getCurrentUser());
  }

  
  /**
   * Check if the given user has site.upd access
   * 
   * @param user
   * @param siteId
   * @return
   */
  public boolean isInstructor(User user, String siteId)
  {
    log.debug("isInstructor(userId={}, siteId={})", user.getId(), siteId);
    if (user != null)
      return securityService.unlock(user, "site.upd", siteId);
    else
      return false;
  }

  /**
   * Check if the given user has section.role.ta access
   * 
   * @param user
   * @return
   */
  private boolean isSectionTA(User user)
  {
    if (log.isDebugEnabled())
    {
      log.debug("isSectionTA(User " + user + ")");
    }
    if (user != null)
      return securityService.unlock(user, "section.role.ta", getContextSiteId());
    else
      return false;
  }

  /**
   * @return siteId
   */
  private String getContextSiteId()
  {
    log.debug("getContextSiteId()");
    return "/site/" + getCurrentContext();
  }
  
  /**
   * 
   * @return the current context without the "/site/" prefix
   */
  private String getCurrentContext() {
      return toolManager.getCurrentPlacement().getContext();
  }
  
  private String getCurrentUser() {
      return sessionManager.getCurrentSessionUserId();
  }

  /**
   * @param forumManager
   */
  public void setForumManager(MessageForumsForumManager forumManager)
  {
    if (log.isDebugEnabled())
    {
      log.debug("setForumManager(MessageForumsForumManager" + forumManager
          + ")");
    }
    this.forumManager = forumManager;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#createForum()
   */
  public DiscussionForum createForum()
  {
    log.debug("createForum()");
    DiscussionForum forum = forumManager.createDiscussionForum();
    flagAreaCacheForClearing(forum);
    return forum;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#deleteForum(org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public void deleteForum(DiscussionForum forum)
  {
    if (log.isDebugEnabled())
    {
      log.debug("setForumManager(DiscussionForum" + forum + ")");
    }
    Long forumId = forum == null ? null : forum.getId();
    List<Long> topicIds = new ArrayList<>();
    if (forumId != null)
    {
      DiscussionForum forumWithTopics = (DiscussionForum) forumManager.getForumByIdWithTopics(forumId);
      if (forumWithTopics != null && forumWithTopics.getTopics() != null)
      {
        for (Iterator iter = forumWithTopics.getTopics().iterator(); iter.hasNext();)
        {
          DiscussionTopic topic = (DiscussionTopic) Hibernate.unproxy(iter.next());
          if (topic != null && topic.getId() != null)
          {
            topicIds.add(topic.getId());
          }
        }
      }
    }
    forumManager.deleteDiscussionForum(forum);
    flagAreaCacheForClearing(forum);
    if (forumId != null)
    {
      TaskService taskService = (TaskService) ComponentManager.get("org.sakaiproject.tasks.api.TaskService");
      String referenceForum = DiscussionForumService.REFERENCE_ROOT + "/" + getCurrentContext() + "/" + forumId;
      taskService.removeTaskByReference(referenceForum);
      for (Long topicId : topicIds)
      {
        String referenceTopic = referenceForum + "/topic/" + topicId;
        taskService.removeTaskByReference(referenceTopic);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#createTopic(org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public DiscussionTopic createTopic(DiscussionForum forum)
  {
    if (log.isDebugEnabled())
    {
      log.debug("createTopic(DiscussionForum" + forum + ")");
    }
    if (forum == null)
    {
      log.debug("Attempt to create topic with out forum");
      return null;
    }
    DiscussionTopic topic = forumManager.createDiscussionForumTopic(forum);
    flagAreaCacheForClearing(forum);
    return topic;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#saveForum(org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public DiscussionForum saveForum(DiscussionForum forum)
  {
    if (log.isDebugEnabled())
    {
      log.debug("saveForum(DiscussionForum" + forum + ")");
    }
    return saveForum(forum, false, getCurrentContext(), true, getCurrentUser());
  }
  
  public DiscussionForum saveForum(String contextId, DiscussionForum forum) {
      if (log.isDebugEnabled()) log.debug("saveForum(String contextId, DiscussionForum forum)");
      
      if (contextId == null || forum == null) {
          throw new IllegalArgumentException("Null contextId or forum passed to saveForum. contextId:" + contextId);
      }
      
      return saveForum(forum, forum.getDraft(), contextId, true, getCurrentUser());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#saveForumAsDraft(org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public DiscussionForum saveForumAsDraft(DiscussionForum forum)
  {
    if (log.isDebugEnabled())
    {
      log.debug("saveForumAsDraft(DiscussionForum" + forum + ")");
    }
    return saveForum(forum, true, getCurrentContext(), true, getCurrentUser());
  }

  public DiscussionForum saveForum(DiscussionForum forum, boolean draft, String contextId, boolean logEvent, String currentUser)
  {
    if (log.isDebugEnabled())
    {
      log.debug("saveForum(DiscussionForum" + forum + "boolean " + draft + ")");
    }

    boolean saveArea = forum.getId() == null;
    forum.setDraft(draft);

    final DiscussionForum forumReturn = forumManager.saveDiscussionForum(forum, draft, logEvent, currentUser);
    if (saveArea)
    {
      String dfType = typeManager.getDiscussionForumType();
      Area area = areaManager.getAreaByContextIdAndTypeId(contextId, dfType);
      forumReturn.setArea(area);
      forumReturn.setSortIndex(0);
      area.addDiscussionForum(forumReturn);
      area = areaManager.saveArea(area, currentUser);
      flagAreaCacheForClearing(area);
    }
    return forumReturn;
  }

  private void flagAreaCacheForClearing(Object object) {
    Area area = null;
    if (object instanceof Topic) {
      Topic topic = (Topic) object;
      if (topic.getBaseForum() != null) area = topic.getBaseForum().getArea();
      if (topic.getOpenForum() != null) area = topic.getOpenForum().getArea();
      if (topic.getPrivateForum() != null) area = topic.getPrivateForum().getArea();
    } else if (object instanceof BaseForum) {
      BaseForum forum = (BaseForum) object;
      area = forum.getArea();
    } else if (object instanceof Area) {
      area = (Area) object;
    }
    uiPermissionsManager.clearMembershipsFromCacheForArea(area);
  }

  @Override
  public void saveTopicAsDraft(DiscussionTopic topic)
  {
    saveTopic(topic, true);
  }

  @Override
  public DiscussionTopic saveTopic(DiscussionTopic topic)
  {
    return saveTopic(topic, false);
  }

  @Override
  public DiscussionTopic saveTopic(DiscussionTopic topic, boolean draft)
  {
    TopicEvent event = topic.getId() == null ? TopicEvent.ADD : TopicEvent.REVISE;
    LRS_Statement statement = getStatementForUserPosted(topic.getTitle(), SAKAI_VERB.interacted).orElse(null);
    return saveTopic(topic, draft, new ForumsTopicEventParams(event, statement));
  }

  @Override
  public DiscussionTopic saveTopic(DiscussionTopic topic, boolean draft, ForumsTopicEventParams params)
  {
    return saveTopic(topic, draft, params, getCurrentUser());
  }

  @Override
  public DiscussionTopic saveTopic(DiscussionTopic topic, boolean draft, ForumsTopicEventParams params, String currentUser)
  {
    log.debug("Save topic {}, as a draft ({})", topic, draft);

    boolean saveForum = topic.getId() == null;
    
    topic.setDraft(draft);
    DiscussionForum forum = (DiscussionForum) topic.getBaseForum();
    topic = forumManager.saveDiscussionForumTopic(topic, forum.getDraft(), currentUser, params != null);
    // refresh the forum for Hibernate
    forum = (DiscussionForum) topic.getBaseForum();

    if (saveForum)
    {
      forum.addTopic(topic);
      forum = forumManager.saveDiscussionForum(forum, forum.getDraft(), false, currentUser); // event already logged by saveDiscussionForumTopic()
      //sak-5146 forumManager.saveDiscussionForum(forum);
    }
    flagAreaCacheForClearing(forum);

    if (params != null)
    {
      Event event = eventTrackingService.newEvent(params.event.type, getEventMessage(topic), null, params.event.modification,
          NotificationService.NOTI_OPTIONAL, params.lrsStatement);
      eventTrackingService.post(event);
    }
    return topic;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#deleteTopic(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public void deleteTopic(DiscussionTopic topic)
  {
    if (log.isDebugEnabled())
    {
      log.debug("deleteTopic(DiscussionTopic " + topic + ")");
    }
    Long forumId = null;
    Long topicId = null;
    if (topic != null)
    {
      topicId = topic.getId();
      if (topic.getBaseForum() != null)
      {
        forumId = topic.getBaseForum().getId();
      }
    }
    forumManager.deleteDiscussionForumTopic(topic);
    flagAreaCacheForClearing(topic);
    if (forumId != null && topicId != null)
    {
      TaskService taskService = (TaskService) ComponentManager.get("org.sakaiproject.tasks.api.TaskService");
      String referenceTopic = DiscussionForumService.REFERENCE_ROOT + "/" + getCurrentContext() + "/" + forumId + "/topic/" + topicId;
      taskService.removeTaskByReference(referenceTopic);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getDefaultControlPermissions()
   */
  public List getDefaultControlPermissions()
  {
    log.debug("getDefaultControlPermissions()");
    List defaultControlPermissions = new ArrayList();
    Iterator roles = getRoles();
    while (roles.hasNext())
    {
      String roleId = (String) roles.next();
      AreaControlPermission controlPermission = permissionManager
          .getDefaultAreaControlPermissionForRole(roleId, typeManager
              .getDiscussionForumType());

      defaultControlPermissions.add(controlPermission);
    }
    return defaultControlPermissions;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getAreaControlPermissions()
   */
  public List getAreaControlPermissions()
  {
    log.debug("getAreaControlPermissions()");
    List areaControlPermissions = new ArrayList();
    Iterator roles = getRoles();
    while (roles.hasNext())
    {
      String roleId = (String) roles.next();
      AreaControlPermission controlPermission = permissionManager
          .getAreaControlPermissionForRole(roleId, typeManager
              .getDiscussionForumType());
      if (controlPermission == null)
      {
        controlPermission = permissionManager
            .createAreaControlPermissionForRole(roleId, typeManager
                .getDiscussionForumType());
      }
      areaControlPermissions.add(controlPermission);
    }
    return areaControlPermissions;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getAreaMessagePermissions()
   */
  public List getAreaMessagePermissions()
  {
    log.debug("getAreaMessagePermissions()");
    List areaMessagePermissions = new ArrayList();
    Iterator roles = getRoles();
    while (roles.hasNext())
    {
      String roleId = (String) roles.next();
      MessagePermissions messagePermission = permissionManager
          .getAreaMessagePermissionForRole(roleId, typeManager
              .getDiscussionForumType());
      if (messagePermission == null)
      {
        messagePermission = permissionManager
            .createAreaMessagePermissionForRole(roleId, typeManager
                .getDiscussionForumType());
      }
      areaMessagePermissions.add(messagePermission);
    }
    return areaMessagePermissions;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getDefaultMessagePermissions()
   */
  public List getDefaultMessagePermissions()
  {
    log.debug("getDefaultMessagePermissions()");
    List defaultMessagePermissions = new ArrayList();
    Iterator roles = getRoles();
    while (roles.hasNext())
    {
      String roleId = (String) roles.next();
      MessagePermissions messagePermission = permissionManager
          .getDefaultAreaMessagePermissionForRole(roleId, typeManager
              .getDiscussionForumType());
      defaultMessagePermissions.add(messagePermission);
    }
    return defaultMessagePermissions;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#saveDefaultMessagePermissions(java.util.List)
   */
  public void saveAreaMessagePermissions(List messagePermissions)
  {
    if (log.isDebugEnabled())
    {
      log.debug("saveDefaultMessagePermissions(List " + messagePermissions
          + ")");
    }
    if (messagePermissions != null && messagePermissions.size() > 0)
    {
      Iterator iterator = messagePermissions.iterator();
      while (iterator.hasNext())
      {
        MessagePermissions msgPermission = (MessagePermissions) iterator.next();
        permissionManager.saveAreaMessagePermissionForRole(
            getDiscussionForumArea(), msgPermission, typeManager
                .getDiscussionForumType());
      }
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getForumControlPermissions(org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public List getForumControlPermissions(DiscussionForum forum)
  {
    if (log.isDebugEnabled())
    {
      log.debug("getForumControlPermissions(DiscussionForum " + forum + ")");
    }
    List forumControlPermissions = new ArrayList();
    Iterator roles = getRoles();
    while (roles.hasNext())
    {
      String roleId = (String) roles.next();
      ForumControlPermission controlPermission = permissionManager
          .getForumControlPermissionForRole(forum, roleId, typeManager
              .getDiscussionForumType());

      if (controlPermission == null)
      {
        controlPermission = permissionManager
            .createForumControlPermissionForRole(roleId, typeManager
                .getDiscussionForumType());
      }
      forumControlPermissions.add(controlPermission);
    }
    return forumControlPermissions;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getForumMessagePermissions(org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public List getForumMessagePermissions(DiscussionForum forum)
  {
    if (log.isDebugEnabled())
    {
      log.debug("getForumMessagePermissions(DiscussionForum " + forum + ")");
    }

    List forumMessagePermissions = new ArrayList();
    Iterator roles = getRoles();
    while (roles.hasNext())
    {
      String roleId = (String) roles.next();
      MessagePermissions messagePermission = permissionManager
          .getForumMessagePermissionForRole(forum, roleId, typeManager
              .getDiscussionForumType());

      if (messagePermission == null)
      {
        messagePermission = permissionManager
            .createForumMessagePermissionForRole(roleId, typeManager
                .getDiscussionForumType());
      }
      forumMessagePermissions.add(messagePermission);
    }
    return forumMessagePermissions;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getTopicControlPermissions(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public List getTopicControlPermissions(DiscussionTopic topic)
  {
    log.debug("getTopicControlPermissions(DiscussionTopic " + topic + ")");

    List topicControlPermissions = new ArrayList();
    Iterator roles = getRoles();
    while (roles.hasNext())
    {
      String roleId = (String) roles.next();
      TopicControlPermission controlPermission = permissionManager
          .getTopicControlPermissionForRole(topic, roleId, typeManager
              .getDiscussionForumType());

      if (controlPermission == null)
      {
        controlPermission = permissionManager
            .createTopicControlPermissionForRole(topic.getBaseForum(), roleId,
                typeManager.getDiscussionForumType());
      }
      topicControlPermissions.add(controlPermission);
    }
    return topicControlPermissions;

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getTopicMessagePermissions(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public List getTopicMessagePermissions(DiscussionTopic topic)
  {
    log.debug("getTopicMessagePermissions(DiscussionTopic " + topic + ")");

    List topicMessagePermissions = new ArrayList();
    Iterator roles = getRoles();
    while (roles.hasNext())
    {
      String roleId = (String) roles.next();
      MessagePermissions messagePermission = permissionManager
          .getTopicMessagePermissionForRole(topic, roleId, typeManager
              .getDiscussionForumType());

      if (messagePermission == null)
      {
        messagePermission = permissionManager
            .createTopicMessagePermissionForRole(topic.getBaseForum(), roleId,
                typeManager.getDiscussionForumType());
      }
      topicMessagePermissions.add(messagePermission);
    }
    return topicMessagePermissions;

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#saveDefaultControlPermissions(java.util.List)
   */
  public void saveAreaControlPermissions(List controlpermissions)
  {
    if (log.isDebugEnabled())
    {
      log.debug("saveAreaControlPermissions(List" + controlpermissions + ")");
    }
    if (controlpermissions != null && controlpermissions.size() > 0)
    {
      Iterator iterator = controlpermissions.iterator();
      while (iterator.hasNext())
      {
        AreaControlPermission controlPermission = (AreaControlPermission) iterator
            .next();
        permissionManager.saveAreaControlPermissionForRole(
            getDiscussionForumArea(), controlPermission, typeManager
                .getDiscussionForumType());
      }
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#saveForumControlPermissions(org.sakaiproject.api.app.messageforums.DiscussionForum,
   *      java.util.List)
   */
  public void saveForumControlPermissions(DiscussionForum forum,
      List controlPermissions)
  {
    if (log.isDebugEnabled())
    {
      log.debug("saveForumControlPermissions(List " + controlPermissions + ")");
    }
    if (forum != null && controlPermissions != null
        && controlPermissions.size() > 0)
    {
      Iterator iterator = controlPermissions.iterator();
      while (iterator.hasNext())
      {
        ForumControlPermission controlPermission = (ForumControlPermission) iterator
            .next();
        permissionManager.saveForumControlPermissionForRole(forum,
            controlPermission);
      }
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#saveForumMessagePermissions(org.sakaiproject.api.app.messageforums.DiscussionForum,
   *      java.util.List)
   */
  public void saveForumMessagePermissions(DiscussionForum forum,
      List messagePermissions)
  {
    if (log.isDebugEnabled())
    {
      log.debug("saveForumMessagePermissions(List " + messagePermissions + ")");
    }
    if (forum != null && messagePermissions != null
        && messagePermissions.size() > 0)
    {
      Iterator iterator = messagePermissions.iterator();
      while (iterator.hasNext())
      {
        MessagePermissions messagePermission = (MessagePermissions) iterator
            .next();
        permissionManager.saveForumMessagePermissionForRole(forum,
            messagePermission);
      }
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#saveTopicControlPermissions(org.sakaiproject.api.app.messageforums.DiscussionForum,
   *      java.util.List)
   */
  public void saveTopicControlPermissions(DiscussionTopic topic,
      List controlPermissions)
  {
    if (log.isDebugEnabled())
    {
      log.debug("saveTopicControlPermissions(List " + controlPermissions + ")");
    }
    if (topic != null && controlPermissions != null
        && controlPermissions.size() > 0)
    {
      Iterator iterator = controlPermissions.iterator();
      while (iterator.hasNext())
      {
        TopicControlPermission controlPermission = (TopicControlPermission) iterator
            .next();
        permissionManager.saveTopicControlPermissionForRole(topic,
            controlPermission);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#saveTopicMessagePermissions(org.sakaiproject.api.app.messageforums.DiscussionTopic,
   *      java.util.List)
   */
  public void saveTopicMessagePermissions(DiscussionTopic topic,
      List messagePermissions)
  {
    if (log.isDebugEnabled())
    {
      log.debug("saveTopicMessagePermissions(List " + messagePermissions + ")");
    }
    if (topic != null && messagePermissions != null
        && messagePermissions.size() > 0)
    {
      Iterator iterator = messagePermissions.iterator();
      while (iterator.hasNext())
      {
        MessagePermissions messagePermission = (MessagePermissions) iterator
            .next();
        permissionManager.saveTopicMessagePermissionForRole(topic,
            messagePermission);
      }
    }

  }

  /**
   * @return Roles for the current site
   */
  private Iterator getRoles()
  {
    log.debug("getRoles()");
    List roleList = new ArrayList();
    AuthzGroup realm = null;
    try
    {
      realm = authzGroupService.getAuthzGroup(getContextSiteId());
      Set roles = realm.getRoles();
      if (roles != null && roles.size() > 0)
      {
        Iterator roleIter = roles.iterator();
        while (roleIter.hasNext())
        {
          Role role = (Role) roleIter.next();
          if (role != null) roleList.add(role.getId());
        }
      }
    }
    catch (GroupNotDefinedException e) {
		log.error(e.getMessage(), e);
	}
    Collections.sort(roleList);
    return roleList.iterator();
  }
  
  public boolean  getAnonRole()
  {
	  return getAnonRole(getContextSiteId());
  }
  
  public boolean  getAnonRole(String contextSiteId)
  {
   log.debug("getAnonRoles()");
   AuthzGroup realm = null;
   try
    {
      realm = authzGroupService.getAuthzGroup(contextSiteId);
      Role anon = realm.getRole(".anon");
     if (sessionManager.getCurrentSessionUserId()==null && anon != null && anon.getAllowedFunctions().contains("site.visit"))
      {
			return true;
      }
    }       

    catch (GroupNotDefinedException e) {
		
		log.error(e.getMessage(), e);
		return false;
	}      
    return false; 
  }

  public void markMessageAs(Message message, boolean readStatus)
  {
    if (log.isDebugEnabled())
    {
      log.debug("markMessageAsRead(Message" + message + ")");
    }
    try
    {
      messageManager.markMessageReadForUser(message.getTopic().getId(), message
          .getId(), readStatus);
    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
    }

  }
  
  public void markMessageReadStatusForUser(Message message, boolean readStatus, String userId)
  {
	  if (log.isDebugEnabled())
	  {
		  log.debug("markMessageReadStatusForUser(Message" + message + " readStatus:" + readStatus + " userId: " + userId + ")");
	  }
	  try
	  {
		  messageManager.markMessageReadForUser(message.getTopic().getId(), message
				  .getId(), readStatus, userId);
	  }
	  catch (Exception e)
	  {
		  log.error(e.getMessage(), e);
	  }
  }
  
  /**
   * @param forum
   * @return
   */
  
  public boolean isForumOwner(DiscussionForum forum){
	  return isForumOwner(forum, userDirectoryService.getCurrentUser().getId());
  }
  
  public boolean isForumOwner(DiscussionForum forum, String userId)
  {
	return isForumOwner(forum, userId, getContextSiteId());
  }
  
  public boolean isForumOwner(DiscussionForum forum, String userId, String siteId)
  {
	  return isForumOwner(forum.getId(), forum.getCreatedBy(), userId, siteId);
  }
  
  public boolean isForumOwner(Long forumId, String forumCreatedBy, String userId, String siteId)
  {
    if (log.isDebugEnabled())
    {
      log.debug("isForumOwner(DiscussionForum " + forumId + ")");
    }
    return forumCreatedBy.equals(userId) && !isRoleSwapView();
  }
  
  private boolean isRoleSwapView()
  {
	return (securityService.getUserEffectiveRole() != null);
  }

  /**
   * @param topic
   * @return
   */
  
  public boolean isTopicOwner(DiscussionTopic topic){
	  return isTopicOwner(topic, userDirectoryService.getCurrentUser().getId());
  }
  
  public boolean isTopicOwner(DiscussionTopic topic, String userId)
  {
	  return isTopicOwner(topic, userId, getContextSiteId());
  }
  
  public boolean isTopicOwner(DiscussionTopic topic, String userId, String siteId)
  {
	  return isTopicOwner(topic.getId(), topic.getCreatedBy(), userId, siteId);
  }
  
  public boolean isTopicOwner(Long topicId, String topicCreatedBy, String userId, String siteId)
  {
    if (log.isDebugEnabled())
    {
      log.debug("isTopicOwner(DiscussionTopic " + topicId + ")");
    }
    return topicCreatedBy.equals(userId) && !isRoleSwapView();
  }

  private boolean getTopicAccess(DiscussionTopic t)
  {
    log.debug("getTopicAccess(DiscussionTopic {} )", t.getId());

    // SAK-27570: Return early instead of looping through lots of database records
    if (isInstructor() || securityService.isSuperUser() || isTopicOwner(t)) {
      return true;
    }
    else if (t.getDraft().equals(Boolean.TRUE) || t.getAvailability() == null || !t.getAvailability()) {
    	return false;
    }

    //SAK-12685 If topic's permission level name is "None", then can't access 
    User user=userDirectoryService.getCurrentUser();
    String role=authzGroupService.getUserRole(user.getId(), getContextSiteId());
    return !forumManager.doesRoleHavePermissionInTopic(t.getId(), role, PermissionLevelManager.PERMISSION_LEVEL_NAME_NONE);
  }

  private List<MessageForumsUser> decodeActorPermissionTypeList(List<String> selectedList) {
    log.debug("decodeActorPermissionTypeList(List{})", selectedList);
    List<MessageForumsUser> newSelectedMemberList = new ArrayList<>();

    /** lookup item in map */
    for (String selectedItem : selectedList) {
      MessageForumsUser user = new MessageForumsUserImpl();
      /** lookup item in map */
      MembershipItem item = getAllCourseMembers().get(selectedItem);
      if (item == null) {
        log.warn("decodeActorPermissionTypeList() could not resolve uuid: {}", selectedItem);
      } else {
        switch (item.getType()) {
          case MembershipItem.TYPE_ALL_PARTICIPANTS:
            user.setTypeUuid(typeManager.getAllParticipantType());
            user.setUserId(typeManager.getAllParticipantType());
            newSelectedMemberList.add(user);
            break;
          case MembershipItem.TYPE_NOT_SPECIFIED:
            user.setTypeUuid(typeManager.getNotSpecifiedType());
            user.setUserId(typeManager.getNotSpecifiedType());
            // if not specified is deleted then only this value remains.
            newSelectedMemberList = new ArrayList<>();
            newSelectedMemberList.add(user);
            break;
          case MembershipItem.TYPE_ROLE:
            user.setTypeUuid(typeManager.getRoleType());
            user.setUserId(item.getRole().getId());
            newSelectedMemberList.add(user);
            break;
          case MembershipItem.TYPE_GROUP:
            user.setTypeUuid(typeManager.getGroupType());
            user.setUserId(item.getGroup().getId());
            newSelectedMemberList.add(user);
            break;
          case MembershipItem.TYPE_USER:
            user.setTypeUuid(typeManager.getUserType());
            user.setUserId(item.getUser().getId());
            newSelectedMemberList.add(user);
            break;
          default:
            log.warn("Could not resolve membership type: {}", item.getType());
            break;
        }
      }
    }
    return newSelectedMemberList;
  }

  public List<MessageForumsUser> decodeAccessorsList(List<String> accessorList) {
    log.debug("decodeAccessorsList(List{})", accessorList);
    if (accessorList == null || accessorList.isEmpty()) {
      return forumManager.createDefaultActorPermissions().getAccessors();
    }
    return decodeActorPermissionTypeList(accessorList);
  }

  public List<MessageForumsUser> decodeContributorsList(List<String> contributorList) {
    log.debug("decodeContributorsList(List{})", contributorList);
    if (contributorList == null || contributorList.isEmpty()) {
      return forumManager.createDefaultActorPermissions().getContributors();
    }
    return decodeActorPermissionTypeList(contributorList);
  }

  public List<String> getContributorsList(DiscussionForum forum) {
    log.debug(" getContributorsList(DiscussionForum{})", forum);
    if (forum == null) return null;
    List<MessageForumsUser> contributorList;
    if (forum.getActorPermissions() == null || forum.getActorPermissions().getContributors() == null) {
      ActorPermissions permissions = forumManager.createDefaultActorPermissions();
      forum.setActorPermissions(permissions);
      contributorList = permissions.getContributors();
    } else {
      contributorList = forum.getActorPermissions().getContributors();
    }

    return getContributorAccessorList(contributorList.iterator());
  }

  public List<String> getAccessorsList(DiscussionForum forum) {
    log.debug("getAccessorsList(DiscussionForum" + forum + ")");
    if (forum == null) return null;
    List<MessageForumsUser> accessorsList;
    if (forum.getActorPermissions() == null || forum.getActorPermissions().getAccessors() == null) {
      forum.setActorPermissions(forumManager.createDefaultActorPermissions());
      accessorsList = forumManager.createDefaultActorPermissions().getAccessors();
    } else {
      accessorsList = forum.getActorPermissions().getAccessors();
    }

    return getContributorAccessorList(accessorsList.iterator());
  }

  private List<String> getContributorAccessorList(Iterator<MessageForumsUser> iterator) {
    log.debug("getContributorAccessorList(Iterator{})", iterator);
    List<String> modifiedContributorList = new ArrayList<>();
    while (iterator.hasNext())
    {
      String selectedId = null;
      MessageForumsUser user = iterator.next();
      List<MembershipItem> totalmembers = membershipManager.convertMemberMapToList(getAllCourseMembers());
      Iterator<MembershipItem> iter = totalmembers.iterator();

      if (user.getTypeUuid().equals(typeManager.getAllParticipantType()))
      {
        while (iter.hasNext())
        {
          MembershipItem member = iter.next();
          if (member.getType() == MembershipItem.TYPE_ALL_PARTICIPANTS)
          {
            selectedId = member.getId();
          }
        }
      }
      if (user.getTypeUuid().equals(typeManager.getNotSpecifiedType()))
      {
        while (iter.hasNext())
        {
          MembershipItem member = iter.next();
          if (member.getType() == MembershipItem.TYPE_NOT_SPECIFIED)
          {
            selectedId = member.getId();
          }
        }
      }

      if (user.getTypeUuid().equals(typeManager.getGroupType()))
      {
        while (iter.hasNext())
        {
          MembershipItem member = iter.next();
          if (member.getType() == MembershipItem.TYPE_GROUP && user.getUserId().equals(member.getGroup().getId()))
          {
            selectedId = member.getId();
          }
        }

      }
      if (user.getTypeUuid().equals(typeManager.getRoleType()))
      {
        while (iter.hasNext())
        {
          MembershipItem member = iter.next();
          if (member.getType() == MembershipItem.TYPE_ROLE && user.getUserId().equals(member.getRole().getId()))
          {
            selectedId = member.getId();
          }
        }
      }
      if (user.getTypeUuid().equals(typeManager.getUserType()))
      {
        while (iter.hasNext())
        {
          MembershipItem member = iter.next();
          if (member.getType() == MembershipItem.TYPE_USER && user.getUserId().equals(member.getUser().getId()))
          {
            selectedId = member.getId();
          }
        }
      }
      modifiedContributorList.add(selectedId);
    }
    return modifiedContributorList;
  }

  @Override
  public Map<String, MembershipItem> getAllCourseMembers() {
    // TODO ERN cache this
    return membershipManager.getAllCourseMembers(true, false, true, null);
  }

  @Override
  public List<String> getContributorsList(DiscussionTopic topic, DiscussionForum forum) {
    log.debug("getContributorsList(DiscussionTopic {}, DiscussionForum {})", topic, forum);
    if (topic == null) return null;
    if (topic.getActorPermissions() == null || topic.getActorPermissions().getContributors() == null) {
      // hibernate does not permit this b/c saving forum and topics will
      // throw uniqueobjectexception
      topic.setActorPermissions(getDeepCopyOfParentActorPermissions(forum.getActorPermissions()));
    }

    return getContributorAccessorList(topic.getActorPermissions().getContributors().iterator());
  }

  private ActorPermissions getDeepCopyOfParentActorPermissions(ActorPermissions actorPermissions) {
    ActorPermissions newAP = new ActorPermissionsImpl();
    List<MessageForumsUser> parentAccessors = actorPermissions.getAccessors();
    List<MessageForumsUser> parentContributors = actorPermissions.getContributors();
    List<MessageForumsUser> newAccessors = new ArrayList<>();
    List<MessageForumsUser> newContributors = new ArrayList<>();

    for (MessageForumsUser accessParent : parentAccessors) {
      MessageForumsUser newAccessor = new MessageForumsUserImpl();
      newAccessor.setTypeUuid(accessParent.getTypeUuid());
      newAccessor.setUserId(accessParent.getUserId());
      newAccessor.setUuid(accessParent.getUuid());
      newAccessors.add(newAccessor);
    }

    for (MessageForumsUser contribParent : parentContributors) {
      MessageForumsUser newContributor = new MessageForumsUserImpl();
      newContributor.setTypeUuid(contribParent.getTypeUuid());
      newContributor.setUserId(contribParent.getUserId());
      newContributor.setUuid(contribParent.getUuid());
      newContributors.add(newContributor);
    }
    newAP.setAccessors(newAccessors);
    newAP.setContributors(newContributors);
    return newAP;
  }

  public List<String> getAccessorsList(DiscussionTopic topic, DiscussionForum forum) {
    log.debug("getAccessorsList(DiscussionTopic {}, DiscussionForum {})", topic, forum);
    if (topic == null) return null;
    if (topic.getActorPermissions() == null || topic.getActorPermissions().getAccessors() == null) {
      // hibernate does not permit this b/c saving forum and topics will
      // throw uniqueobjectexception
      topic.setActorPermissions(getDeepCopyOfParentActorPermissions(forum.getActorPermissions()));
    }
    return getContributorAccessorList(topic.getActorPermissions().getAccessors().iterator());
  }

  @Override
  public DBMembershipItem getAreaDBMember(Set<DBMembershipItem> originalSet, String name, int type) {
    return getDBMember(originalSet, name, type);
  }

  @Override
  public DBMembershipItem getDBMember(Set<DBMembershipItem> originalSet, String name, int type) {
	return getDBMember(originalSet, name, type, getContextSiteId());
  }

  @Override
  public DBMembershipItem getDBMember(Set<DBMembershipItem> originalSet, String name, int type, String contextSiteId) {

    Predicate<DBMembershipItem> ifTypeAndNameAreEqual = item -> item.getType() == type && item.getName().equals(name);
    Optional<DBMembershipItem> membershipItem = Optional.empty();
    if (originalSet != null) membershipItem = originalSet.stream().filter(ifTypeAndNameAreEqual).findAny();

    if (membershipItem.isPresent() && membershipItem.get().getPermissionLevel() != null) return membershipItem.get();

    PermissionLevel level = null;
    //for groups awareness
    if (type == MembershipItem.TYPE_ROLE || type == MembershipItem.TYPE_GROUP) {

      String levelName;
      if (membershipItem.isPresent()) {
        /** use level from stored item */
        levelName = membershipItem.get().getPermissionLevelName();
      } else {
        /** get level from config file */
        levelName = ServerConfigurationService.getString(MC_DEFAULT + name);
      }

      if (StringUtils.isNotBlank(levelName)) {
        level = permissionLevelManager.getPermissionLevelByName(levelName);
      } else if (name == null || ".anon".equals(name)) {
        level = permissionLevelManager.getDefaultNonePermissionLevel();
      } else {
        if (type == MembershipItem.TYPE_GROUP) {
          level = permissionLevelManager.getDefaultNonePermissionLevel();
        } else {
          //check cache first:
          String cacheId = contextSiteId + "/" + name;
          Set<String> allowedFunctions = allowedFunctionsCache.get(cacheId);
          if (allowedFunctions == null) {
            allowedFunctions = authzGroupService.getAllowedFunctions(name, Collections.singletonList(contextSiteId));
            allowedFunctionsCache.put(cacheId, allowedFunctions);
          }
          if (allowedFunctions.contains(SiteService.SECURE_UPDATE_SITE)) {
            level = permissionLevelManager.getDefaultOwnerPermissionLevel();
          } else {
            level = permissionLevelManager.getDefaultContributorPermissionLevel();
          }
        }
      }
    }
    PermissionLevel noneLevel = permissionLevelManager.getDefaultNonePermissionLevel();

    DBMembershipItem item  = new DBMembershipItemImpl();
    item.setName(name);
    item.setPermissionLevelName((level == null) ? noneLevel.getName() : level.getName());
    item.setType(type);
    item.setPermissionLevel((level == null) ? noneLevel : level);
    return item;
  }

  //Attachment
  public Attachment createDFAttachment(String attachId, String name)
  {
    try
    {
      Attachment attach = messageManager.createAttachment();

      attach.setAttachmentId(attachId);

      attach.setAttachmentName(name);

      ContentResource cr = contentHostingService.getResource(attachId);
      attach.setAttachmentSize((Long.valueOf(cr.getContentLength())).toString());
      attach.setCreatedBy(cr.getProperties().getProperty(
          cr.getProperties().getNamePropCreator()));
      attach.setModifiedBy(cr.getProperties().getProperty(
          cr.getProperties().getNamePropModifiedBy()));
      attach.setAttachmentType(cr.getContentType());
      attach.setAttachmentUrl("/url");

      return attach;
    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
      return null;
    }
  }
  
  @Override
  public Attachment createDuplicateDFAttachment(String attachId, String name)
  {
    try
    {
      // Get the current context
      String currentContext = toolManager.getCurrentPlacement().getContext();

      // Copy the attachment using the ContentHostingService's copyAttachment method
      ContentResource attachment = contentHostingService.copyAttachment(
          attachId, 
          currentContext, 
          toolManager.getTool("sakai.forums").getTitle(), 
          null);  // Using null for MergeConfig as we're duplicating within the same site

      // Now create the attachment object pointing to the new file
      Attachment attach = messageManager.createAttachment();
      attach.setAttachmentId(attachment.getId());
      attach.setAttachmentName(name);

      // Set other properties from the new resource
      attach.setAttachmentSize((Long.valueOf(attachment.getContentLength())).toString());
      attach.setCreatedBy(attachment.getProperties().getProperty(
          attachment.getProperties().getNamePropCreator()));
      attach.setModifiedBy(attachment.getProperties().getProperty(
          attachment.getProperties().getNamePropModifiedBy()));
      attach.setAttachmentType(attachment.getContentType());
      attach.setAttachmentUrl("/url");
      
      return attach;
    }
    catch (Exception e)
    {
      log.error("Error creating duplicate attachment: {}", e.toString());
      return null;
    }
  }

	public List getDiscussionForumsWithTopics()
	{
    log.debug("getDiscussionForumsWithTopics()");
    return forumManager.getForumByTypeAndContextWithTopicsAllAttachments(typeManager
        .getDiscussionForumType());
	}
	
	public List getDiscussionForumsWithTopics(String contextId) {
	    if (log.isDebugEnabled()) log.debug("getDiscussionForumsWithTopics(String contextId)");
	    if (contextId == null) {
	        throw new IllegalArgumentException("Null contextId passed to getDiscussionForumsWithTopics");
	    }
	    String dfType = typeManager.getDiscussionForumType();
	    return forumManager.getForumByTypeAndContextWithTopicsAllAttachments(dfType, contextId);
	}

	public Map<Long, Boolean> getReadStatusForMessagesWithId(List<Long> msgIds, String userId)
	{
		log.debug("getDiscussionForumsWithTopics()");

		
		Map<Long, Boolean> msgIdStatusMap = new HashMap<>();
		if (CollectionUtils.isEmpty(msgIds)) {
			log.debug("empty map returns b/c no msgIds passed to getReadStatusForMessagesWithId");
			return msgIdStatusMap;
		}

		if (userId == null) {
			log.debug("empty user assume that all messages are read");
			for (int i =0; i < msgIds.size(); i++) {
				msgIdStatusMap.put(msgIds.get(i), Boolean.TRUE);
			}
			return msgIdStatusMap; 
		}
		
		
		if (msgIds.size() < MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST) {
			return messageManager.getReadStatusForMessagesWithId(msgIds, userId);
		} else {
			// if there are more than MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST msgs, we need to do multiple queries
			int begIndex = 0;
			int endIndex = 0;

			while (begIndex < msgIds.size()) {
				endIndex = begIndex + MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST;
				if (endIndex > msgIds.size()) {
					endIndex = msgIds.size();
				}
				List tempMsgIdList = new ArrayList();
				tempMsgIdList.addAll(msgIds.subList(begIndex, endIndex));
				Map<Long, Boolean> statusMap = messageManager.getReadStatusForMessagesWithId(tempMsgIdList, userId);
				msgIdStatusMap.putAll(statusMap);
				begIndex = endIndex;
			}
		}
		
		return msgIdStatusMap;
	}

	public List<DiscussionForum> getDiscussionForumsWithTopicsMembershipNoAttachments(String contextId)
	{
        log.debug("getDiscussionForumsWithTopicsMembershipNoAttachments()");
        return forumManager.getForumByTypeAndContextWithTopicsMembership(typeManager.getDiscussionForumType(), contextId);
	}
	
	public List getPendingMsgsInTopic(Long topicId)
	{
		return messageManager.getPendingMsgsInTopic(topicId);
	}
	
	public int getNumModTopicsWithModPermissionByPermissionLevel(List<String> membershipList, List<Topic> moderatedTopics)
	{
		return forumManager.getNumModTopicCurrentUserHasModPermForWithPermissionLevel(membershipList, moderatedTopics);
	}
	
	public int getNumModTopicsWithModPermissionByPermissionLevelName(List<String> membershipList, List<Topic> moderatedTopics)
	{
		return forumManager.getNumModTopicCurrentUserHasModPermForWithPermissionLevelName(membershipList, moderatedTopics);
	}

    private String getEventMessage(Object object) {
        String eventMessagePrefix = "/forums";
        Tool tool = toolManager.getCurrentTool();
        if (tool != null) {
            switch (tool.getId()) {
                case DiscussionForumService.MESSAGE_CENTER_ID:
                    eventMessagePrefix = "/messagesAndForums";
                    break;
                case DiscussionForumService.MESSAGES_TOOL_ID:
                    eventMessagePrefix = "/messages";
                    break;
            }
        }
        return eventMessagePrefix + getContextSiteId() + "/" + object.toString() + "/" + sessionManager.getCurrentSessionUserId();
    }

    public String getContextForTopicById(Long topicId) {
      return getTopicById(topicId).getOpenForum().getArea().getContextId();
    }

    public String getContextForForumById(Long forumId) {
      return getForumById(forumId).getArea().getContextId();
    }
    
    public String getContextForMessageById(Long messageId) {
      return getMessageById(messageId).getTopic().getOpenForum().getArea().getContextId();
    }

    public String ForumIdForMessage(Long messageId) {
      return getMessageById(messageId).getTopic().getOpenForum().getId().toString();
    }
    

    public Set<String> getUsersAllowedForTopic(Long topicId, boolean checkReadPermission, boolean checkModeratePermission) {
  	 log.debug("getUsersAllowedForTopic(" + topicId + ", " + checkReadPermission + ", " + checkModeratePermission + ")"); 
  	 
     if (topicId == null) {
  		  throw new IllegalArgumentException("Null topicId passed to getUsersAllowedToReadTopic");
  	  }
  	  
  	  Set<String> usersAllowed = new HashSet<String>();

  	  // we need to get all of the membership items associated with this topic

  	  // first, check to see if it is in the thread
  	  Set<DBMembershipItem> topicItems = new HashSet<DBMembershipItem>();
  	  DiscussionTopic topicWithMemberships = (DiscussionTopic)forumManager.getTopicByIdWithMemberships(topicId);
  	  if (topicWithMemberships != null && topicWithMemberships.getMembershipItemSet() != null) {
  		  topicItems = topicWithMemberships.getMembershipItemSet();
  	  }


  	  Set<Role> rolesInSite = null;
  	  Set<Group> groupsInSite = new HashSet<Group>();

  	  Site currentSite;
  	  String siteId = toolManager.getCurrentPlacement().getContext();
  	  try {
  		  currentSite = siteService.getSite(siteId);

  		  // get all of the roles in this site
  		  rolesInSite = currentSite.getRoles();
  		  Collection<Group> groups = currentSite.getGroups();
  		  if (groups != null) {
  			  groupsInSite = new HashSet<Group>(groups);
  		  } 
  		  
  	  } catch (IdUnusedException iue) {
  		  log.warn("No site found with id: " + siteId + ". No users returned by getUsersAllowedToReadTopic");
  		  return new HashSet<String>();
  	  }
  	  
  	  List<DBMembershipItem> revisedMembershipItemSet = new ArrayList<DBMembershipItem>();
  	  // we need to get the membership items for the roles separately b/c of default permissions
  	  if (rolesInSite != null) {
  		  for (Role role : rolesInSite) {
  			  DBMembershipItem roleItem = getDBMember(topicItems, role.getId(), MembershipItem.TYPE_ROLE);
  			  if (roleItem != null) {
  				  revisedMembershipItemSet.add(roleItem);
  			  }
  		  }
  	  }
  	  // now add in the group perms
  	  for (Group group : groupsInSite) {
  		  DBMembershipItem groupItem = getDBMember(topicItems, group.getTitle(), MembershipItem.TYPE_GROUP);
  		  if (groupItem != null) {
  			  revisedMembershipItemSet.add(groupItem);
  		  }
  	  }
  	  
  	  // now we have the membership items. let's see which ones can read
  	  for (DBMembershipItem membershipItem : revisedMembershipItemSet) {
  		  if ((checkReadPermission && membershipItem.getPermissionLevel().getRead() && !checkModeratePermission) ||
  				  (!checkReadPermission && checkModeratePermission && membershipItem.getPermissionLevel().getModeratePostings()) ||
  				  (checkReadPermission && membershipItem.getPermissionLevel().getRead() && checkModeratePermission && membershipItem.getPermissionLevel().getModeratePostings())) {
  			  if (membershipItem.getType() == MembershipItem.TYPE_ROLE) {
  				  // add the users who are a member of this role
  				  log.debug("Adding users in role: " + membershipItem.getName() + " with read: " + membershipItem.getPermissionLevel().getRead());
  				  Set<String> usersInRole = currentSite.getUsersHasRole(membershipItem.getName());
  				  usersAllowed.addAll(usersInRole);
  			  } else if (membershipItem.getType() == MembershipItem.TYPE_GROUP) {
  				  String groupName = membershipItem.getName();
  				  for (Group group : groupsInSite) {
  					  if (group.getTitle().equals(groupName)) {
  						  Set<Member> groupMembers = group.getMembers();
  						  if (groupMembers != null) {
  							  for (Member member : groupMembers) {
  								  usersAllowed.add(member.getUserId());
  							  }
  						  }
  					  }
  				  }
  			  }
  		  }
  	  }
  		  
  	  return usersAllowed;
    }

	public List getRecentDiscussionForumThreadsByTopicIds(List<Long> topicIds, int numberOfMessages)
	{
		if (log.isDebugEnabled())
		{
			log.debug("getRecentDiscussionForumMessagesByContext( Size of list is " + topicIds.size() + ")");
		}
		return messageManager.getRecentDiscussionForumThreadsByTopicIds(topicIds, numberOfMessages);
	}

	public List<Attachment> getTopicAttachments(Long topicId) {
		return forumManager.getTopicAttachments(topicId);
	}

	public List<Topic> getTopicsInSite(final String contextId)
	{
		return forumManager.getTopicsInSite(contextId);
	}

	public List<Topic> getAnonymousTopicsInSite(final String contextId)
	{
		return forumManager.getAnonymousTopicsInSite(contextId);
	}

	public boolean isSiteHasAnonymousTopics(final String contextId)
	{
		return forumManager.isSiteHasAnonymousTopics(contextId);
	}

	public MemoryService getMemoryService() {
		return memoryService;
	}

	public void setMemoryService(MemoryService memoryService) {
		this.memoryService = memoryService;
	}

	public List<String> getAllowedGroupForRestrictedForum(final Long forumId, final String permissionName) {
		return forumManager.getAllowedGroupForRestrictedForum(forumId, permissionName);
	}

	public List<String> getAllowedGroupForRestrictedTopic(final Long topicId, final String permissionName) {
		return forumManager.getAllowedGroupForRestrictedTopic(topicId, permissionName);
	}

	@Override
	public Optional<LRS_Statement> getStatementForUserPosted(String subject, SAKAI_VERB sakaiVerb) {
		return LRSDelegate.getStatementForUserPosted(learningResourceStoreService, sessionManager.getCurrentSessionUserId(), subject, sakaiVerb);
    }

	@Override
	public Optional<LRS_Statement> getStatementForUserReadViewed(String subject, String target) {
		return LRSDelegate.getStatementForUserReadViewed(learningResourceStoreService, sessionManager.getCurrentSessionUserId(), subject, target);
	}

	@Override
	public Optional<LRS_Statement> getStatementForGrade(String studentUid, String forumTitle, double score) {
		return LRSDelegate.getStatementForGrade(learningResourceStoreService, userDirectoryService, studentUid, forumTitle, score);
	}
}
