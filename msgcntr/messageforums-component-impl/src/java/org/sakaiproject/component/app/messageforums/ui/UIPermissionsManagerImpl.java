/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-component-impl/src/java/org/sakaiproject/component/app/messageforums/ui/UIPermissionsManagerImpl.java $
 * $Id: UIPermissionsManagerImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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
package org.sakaiproject.component.app.messageforums.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.DBMembershipItem;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.PermissionLevelManager;
import org.sakaiproject.api.app.messageforums.PermissionManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.app.messageforums.TestUtil;
import org.sakaiproject.component.app.messageforums.dao.hibernate.DBMembershipItemImpl;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 */
@Slf4j
public class UIPermissionsManagerImpl implements UIPermissionsManager {

  // dependencies
  private AuthzGroupService authzGroupService;
  private SessionManager sessionManager;
  private ToolManager toolManager;
  private PermissionManager permissionManager;
  private PermissionLevelManager permissionLevelManager;
  private MessageForumsTypeManager typeManager;
  private SecurityService securityService;
  private DiscussionForumManager forumManager;
  private AreaManager areaManager;
  private MemoryService memoryService;
  private Cache<String, Collection<?>> userGroupMembershipCache;
  private UserDirectoryService userDirectoryService;
  private SiteService siteService;
  private ThreadLocalManager threadLocalManager;
  
  public void init()
  {
     log.info("init()");
     userGroupMembershipCache = memoryService.getCache("org.sakaiproject.component.app.messageforums.ui.UIPermissionsManagerImpl.userGroupMembershipCache");
  }

  /**
   * @param areaManager
   *          The areaManager to set.
   */
  public void setAreaManager(AreaManager areaManager)
  {
    this.areaManager = areaManager;
  }

  /**
   * @param forumManager
   *          The forumManager to set.
   */
  public void setForumManager(DiscussionForumManager forumManager)
  {
    this.forumManager = forumManager;
  }

  /**
   * @param authzGroupService
   *          The authzGroupService to set.
   */
  public void setAuthzGroupService(AuthzGroupService authzGroupService)
  {
    log.debug("setAuthzGroupService(AuthzGroupService {})", authzGroupService);
    this.authzGroupService = authzGroupService;
  }

  public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
    this.userDirectoryService = userDirectoryService;
  }

  public void setSiteService(SiteService siteService) {
    this.siteService = siteService;
  }

  public void setThreadLocalManager(ThreadLocalManager threadLocalManager) {
    this.threadLocalManager = threadLocalManager;
  }

/**
   * @param sessionManager
   *          The sessionManager to set.
   */
  public void setSessionManager(SessionManager sessionManager)
  {
    log.debug("setSessionManager(SessionManager {})", sessionManager);
    this.sessionManager = sessionManager;
  }

  /**
   * @param toolManager
   *          The toolManager to set.
   */
  public void setToolManager(ToolManager toolManager)
  {
    log.debug("setToolManager(ToolManager {})", toolManager);
    this.toolManager = toolManager;
  }

  /**
   * @param permissionManager
   *          The permissionManager to set.
   */
  public void setPermissionManager(PermissionManager permissionManager)
  {
    log.debug("setPermissionManager(PermissionManager {})", permissionManager);
    this.permissionManager = permissionManager;
  }

  /**
   * @param typeManager
   *          The typeManager to set.
   */
  public void setTypeManager(MessageForumsTypeManager typeManager)
  {
    log.debug("setTypeManager(MessageForumsTypeManager {})", typeManager);
    this.typeManager = typeManager;
  }

  /**
   * @param securityService
   *          The securityService to set.
   */
  public void setSecurityService(SecurityService securityService)
  {
    log.debug("setSecurityService(SecurityService {})", securityService);
    this.securityService = securityService;
  }

  // end dependencies
  /**
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isNewForum()
   */
  public boolean isNewForum()
  {
    log.debug("isNewForum()");
    if (isSuperUser())
    {
      return true;
    }
    
    try
    {
      Iterator iter = getAreaItemsByCurrentUser();
      while (iter.hasNext())
      {
        DBMembershipItem item = (DBMembershipItem) iter.next();
        if (item.getPermissionLevel().getNewForum().booleanValue())
        {
          return true;
        }
      }
    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
      return false;
    }
    return false;
  }

  /**
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isChangeSettings(org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public boolean isChangeSettings(DiscussionForum forum)
  {

    log.debug("isChangeSettings(DiscussionForum {})", forum);
    if (isSuperUser())
    {
      return true;
    }
    if (securityService.unlock(siteService.SECURE_UPDATE_SITE, getContextSiteId())){
    	return true;
    }
    if (forumManager.isForumOwner(forum))
    {
      return true;
    }
    
    try
    {
      Iterator iter = getForumItemsByCurrentUser(forum);
      while (iter.hasNext())
      {
        DBMembershipItem item = (DBMembershipItem) iter.next();
        if (item.getPermissionLevel().getChangeSettings().booleanValue())
        {
          return true;
        }
      }
    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
      return false;
    }
    return false;
  }

  /**   
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isNewTopic(org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public boolean isNewTopic(DiscussionForum forum)
  {
    log.debug("isNewTopic(DiscussionForum {})", forum);
    if (isSuperUser())
    {
      return true;
    }
    try
    {
      Iterator iter = getForumItemsByCurrentUser(forum);
      while (iter.hasNext())
      {
        DBMembershipItem item = (DBMembershipItem) iter.next();
        if (item.getPermissionLevel().getNewTopic().booleanValue())
        {
          return true;
        }
      }
    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
      return false;
    }
    return false;
  }

  /** 
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isNewResponse(org.sakaiproject.api.app.messageforums.DiscussionTopic,
   *      org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public boolean isNewResponse(DiscussionTopic topic, DiscussionForum forum)
  {
	  return isNewResponse(topic, forum, getCurrentUserId(), getContextId());
  }
  
  public boolean isNewResponse(DiscussionTopic topic, DiscussionForum forum, String userId, String contextId){
    log.debug("isNewResponse(DiscussionTopic {}), DiscussionForum {}", topic, forum);

    try
    {
      if (checkBaseConditions(topic, forum, userId, contextId))
      {
        return true;
      }
      Iterator iter = getTopicItemsByUser(topic, userId, contextId);
      while (iter.hasNext())
      {
        DBMembershipItem item = (DBMembershipItem) iter.next();
        if (item.getPermissionLevel().getNewResponse().booleanValue()
        	&& forum != null
            && forum.getDraft().equals(Boolean.FALSE)
            && forum.getLocked().equals(Boolean.FALSE)
            && topic.getDraft().equals(Boolean.FALSE)
            && topic.getLocked().equals(Boolean.FALSE))
        {
          return true;
        }
      }
    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
      return false;
    }
    return false;
  }

  /**
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isNewResponseToResponse(org.sakaiproject.api.app.messageforums.DiscussionTopic,
   *      org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public boolean isNewResponseToResponse(DiscussionTopic topic,
      DiscussionForum forum)
  {
	return isNewResponseToResponse(topic, forum, getCurrentUserId(), getContextId());
  }
  
  public boolean isNewResponseToResponse(DiscussionTopic topic, DiscussionForum forum, String userId, String contextId) {
    log.debug("isNewResponseToResponse(DiscussionTopic {}, DiscussionForum {})", topic, forum);

    try
    {
      if (checkBaseConditions(topic, forum, userId, contextId))
      {
        return true;
      }
      Iterator iter = getTopicItemsByUser(topic, userId, contextId);
      while (iter.hasNext())
      {
        DBMembershipItem item = (DBMembershipItem) iter.next();
        if (item.getPermissionLevel().getNewResponseToResponse().booleanValue()
            && forum.getDraft().equals(Boolean.FALSE)
            && forum.getLocked().equals(Boolean.FALSE)
            && topic.getDraft().equals(Boolean.FALSE)
            && topic.getLocked().equals(Boolean.FALSE))
        {
          return true;
        }
      }

    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
      return false;
    }
    return false;
  }

  /**   
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isMovePostings(org.sakaiproject.api.app.messageforums.DiscussionTopic,
   *      org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public boolean isMovePostings(DiscussionTopic topic, DiscussionForum forum)
  {
    log.debug("isMovePostings(DiscussionTopic {}), DiscussionForum {}", topic, forum);

    try
    {
      if (checkBaseConditions(topic, forum))
      {
        return true;
      }
      Iterator iter = getTopicItemsByCurrentUser(topic);
      while (iter.hasNext())
      {
        DBMembershipItem item = (DBMembershipItem) iter.next();
        if ((item.getPermissionLevel().getMovePosting().booleanValue()
            || item.getPermissionLevel().getReviseAny().booleanValue()
            || item.getPermissionLevel().getReviseOwn().booleanValue())
            && forum.getDraft().equals(Boolean.FALSE)
            && forum.getLocked().equals(Boolean.FALSE)
            && topic.getDraft().equals(Boolean.FALSE)
            && topic.getLocked().equals(Boolean.FALSE))
        {
          return true;
        }
      }

    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
      return false;
    }
    return false;
  }

  /**
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isChangeSettings(org.sakaiproject.api.app.messageforums.DiscussionTopic,
   *      org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public boolean isChangeSettings(DiscussionTopic topic, DiscussionForum forum)
  {
	  return isChangeSettings(topic, forum, getCurrentUserId());
  }
  
  public boolean isChangeSettings(DiscussionTopic topic, DiscussionForum forum, String userId)
  {
    log.debug("isChangeSettings(DiscussionTopic {}), DiscussionForum {}", topic, forum);
    if (isSuperUser(userId))
    {
      return true;
    }
    if (securityService.unlock(userId, siteService.SECURE_UPDATE_SITE, getContextSiteId())){
    	return true;
    }
    try
    {
      // Change Settings on Topic allowed even if the forum is locked
      // if (forum.getLocked() == null || forum.getLocked().equals(Boolean.TRUE))
      // {
      // log.debug("This Forum is Locked");
      // return false;
      // }
      // if owner then allow change of settings on the topic or on forum.
      if (forumManager.isTopicOwner(topic, userId))
      {
        return true;
      }
      Iterator iter = getTopicItemsByUser(topic, userId);
      while (iter.hasNext())
      {
        DBMembershipItem item = (DBMembershipItem) iter.next();
        if (item.getPermissionLevel().getChangeSettings().booleanValue())
           // && forum.getDraft().equals(Boolean.FALSE)  SAK-9230
           // && forum.getLocked().equals(Boolean.FALSE)
           // && topic.getDraft().equals(Boolean.FALSE)
           // && topic.getLocked().equals(Boolean.FALSE))
        {
          return true;
        }
      }

    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
      return false;
    }
    return false;
  }

  /** 
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isPostToGradebook(org.sakaiproject.api.app.messageforums.DiscussionTopic,
   *      org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public boolean isPostToGradebook(DiscussionTopic topic, DiscussionForum forum){
	  return isPostToGradebook(topic, forum, getCurrentUserId());
  }
  
  public boolean isPostToGradebook(DiscussionTopic topic, DiscussionForum forum, String userId)
  {
	  return isPostToGradebook(topic, forum, userId, getContextId());
  }
  
  public boolean isPostToGradebook(DiscussionTopic topic, DiscussionForum forum, String userId, String contextId)
  {
    log.debug("isPostToGradebook(DiscussionTopic {}, DiscussionForum {})", topic, forum);

    try
    {
      if (checkBaseConditions(topic, forum, userId, contextId))
      {
        return true;
      }
      Iterator iter = getTopicItemsByUser(topic, userId, contextId);
      while (iter.hasNext())
      {
        DBMembershipItem item = (DBMembershipItem) iter.next();
        if (item.getPermissionLevel().getPostToGradebook().booleanValue()
            && forum.getDraft().equals(Boolean.FALSE)
            && topic.getDraft().equals(Boolean.FALSE))
        {
          return true;
        }
      }

    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
      return false;
    }
    return false;
  }

  /**   
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isRead(org.sakaiproject.api.app.messageforums.DiscussionTopic,
   *      org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  
  public boolean isRead(DiscussionTopic topic, DiscussionForum forum){
	  return isRead(topic, forum, getCurrentUserId());
  }
  
  public boolean isRead(DiscussionTopic topic, DiscussionForum forum, String userId){
	  String contextId = null;
	  try{
		  //context could be null b/c of external queries... first check
		  //since its faster than a DB lookup
		  contextId = getContextId();
	  }catch (Exception e) {
		  contextId = forumManager.getContextForForumById(forum.getId());
	}
	  return isRead(topic, forum, userId, contextId);
  }
  
  public boolean isRead(DiscussionTopic topic, DiscussionForum forum, String userId, String siteId)
  {
      log.debug("isRead(DiscussionTopic {}, DiscussionForum {})", topic, forum);
	  return isRead(topic.getId(), topic.getDraft(), forum.getDraft(), userId, siteId);
  }
  
  public boolean isRead(Long topicId, Boolean isTopicDraft, Boolean isForumDraft, String userId, String siteId)
  {
    
    try
    {
      if (checkBaseConditions(null, null, userId, "/site/" + siteId))
      {
        return true;
      }
      Iterator iter = getTopicItemsByUser(topicId, userId, siteId);
      while (iter.hasNext())
      {
        DBMembershipItem item = (DBMembershipItem) iter.next();
        if (item.getPermissionLevel().getRead().booleanValue()
            && isForumDraft.equals(Boolean.FALSE)
//            && forum.getLocked().equals(Boolean.FALSE)
            && isTopicDraft.equals(Boolean.FALSE))
//            && topic.getLocked().equals(Boolean.FALSE))
        {
          return true;
        }
      }

    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
      return false;
    }
    return false;
  }

  /**   
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isReviseAny(org.sakaiproject.api.app.messageforums.DiscussionTopic,
   *      org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public boolean isReviseAny(DiscussionTopic topic, DiscussionForum forum)
  {
	  return isReviseAny(topic, forum, getCurrentUserId(), getContextId());
  }
  
  public boolean isReviseAny(DiscussionTopic topic, DiscussionForum forum, String userId, String contextId){
      log.debug("isReviseAny(DiscussionTopic {}, DiscussionForum {})", topic, forum);
    try
    {
      if (checkBaseConditions(topic, forum, userId, contextId))
      {
        return true;
      }
       if (topic.getLocked() == null || topic.getLocked().equals(Boolean.TRUE))
    {
      log.debug("This topic is locked {}", topic);
      return false;
    }
    if (topic.getDraft() == null || topic.getDraft().equals(Boolean.TRUE))
    {
      log.debug("This topic is at draft stage {}", topic);
    }
      Iterator iter = getTopicItemsByUser(topic, userId, contextId);
      while (iter.hasNext())
      {
        DBMembershipItem item = (DBMembershipItem) iter.next();
        if (item.getPermissionLevel().getReviseAny().booleanValue()
            && forum.getDraft().equals(Boolean.FALSE)
            && forum.getLocked().equals(Boolean.FALSE)
            && topic.getDraft().equals(Boolean.FALSE)
            && topic.getLocked().equals(Boolean.FALSE))
        {
          return true;
        }
      }

    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
      return false;
    }
    return false;
  }

  /**   
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isReviseOwn(org.sakaiproject.api.app.messageforums.DiscussionTopic,
   *      org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public boolean isReviseOwn(DiscussionTopic topic, DiscussionForum forum)
  {
	  return isReviseOwn(topic, forum, getCurrentUserId(), getContextId());	  
  }
  
  public boolean isReviseOwn(DiscussionTopic topic, DiscussionForum forum, String userId, String contextId){
    log.debug("isReviseOwn(DiscussionTopic {}, DiscussionForum {})", topic, forum);
    if (checkBaseConditions(topic, forum, userId, contextId))
    {
      return true;
    }
    try
    {
      if (checkBaseConditions(topic, forum,  userId, contextId))
      {
        return true;
      }
      
       if (topic.getLocked() == null || topic.getLocked().equals(Boolean.TRUE))
    {
      log.debug("This topic is locked {}", topic);
      return false;
    }
    if (topic.getDraft() == null || topic.getDraft().equals(Boolean.TRUE))
    {
      log.debug("This topic is at draft stage {}", topic);
    }
      Iterator iter = getTopicItemsByUser(topic, userId, contextId);
      while (iter.hasNext())
      {
        DBMembershipItem item = (DBMembershipItem) iter.next();
        if (item.getPermissionLevel().getReviseOwn().booleanValue()
            && forum.getDraft().equals(Boolean.FALSE)
            && forum.getLocked().equals(Boolean.FALSE)
            && topic.getDraft().equals(Boolean.FALSE)
            && topic.getLocked().equals(Boolean.FALSE))
        {
          return true;
        }
      }

    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
      return false;
    }
    return false;
  }

  /**   
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isDeleteAny(org.sakaiproject.api.app.messageforums.DiscussionTopic,
   *      org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public boolean isDeleteAny(DiscussionTopic topic, DiscussionForum forum)
  {
	return isDeleteAny(topic, forum, getCurrentUserId(), getContextId());
  }
  
  public boolean isDeleteAny(DiscussionTopic topic, DiscussionForum forum, String userId, String contextId){
    log.debug("isDeleteAny(DiscussionTopic {}, DiscussionForum {})", topic, forum);
    if (checkBaseConditions(topic, forum, userId, contextId))
    {
      return true;
    }
    try
    {
      if (checkBaseConditions(topic, forum, userId, contextId))
      {
        return true;
      }
        if (topic.getLocked() == null || topic.getLocked().equals(Boolean.TRUE))
    {
      log.debug("This topic is locked {}", topic);
      return false;
    }
    if (topic.getDraft() == null || topic.getDraft().equals(Boolean.TRUE))
    {
      log.debug("This topic is at draft stage {}", topic);
    }
      Iterator iter = getTopicItemsByUser(topic, userId, contextId);
      while (iter.hasNext())
      {
        DBMembershipItem item = (DBMembershipItem) iter.next();
        if (item.getPermissionLevel().getDeleteAny().booleanValue()
            && forum.getDraft().equals(Boolean.FALSE)
            && forum.getLocked().equals(Boolean.FALSE)
            && topic.getDraft().equals(Boolean.FALSE)
            && topic.getLocked().equals(Boolean.FALSE))
        {
          return true;
        }
      }

    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
      return false;
    }
    return false;
  }

  /**   
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isDeleteOwn(org.sakaiproject.api.app.messageforums.DiscussionTopic,
   *      org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public boolean isDeleteOwn(DiscussionTopic topic, DiscussionForum forum)
  {
	  return isDeleteOwn(topic, forum, getCurrentUserId(), getContextId());
  }
  
  public boolean isDeleteOwn(DiscussionTopic topic, DiscussionForum forum, String userId, String contextId){
    log.debug("isDeleteOwn(DiscussionTopic {}, DiscussionForum {})", topic, forum);
    if (checkBaseConditions(topic, forum, userId, contextId))
    {
      return true;
    }
    try
    {
      if (checkBaseConditions(topic, forum, userId, contextId))
      {
        return true;
      }
        if (topic.getLocked() == null || topic.getLocked().equals(Boolean.TRUE))
    {
      log.debug("This topic is locked {}", topic);
      return false;
    }
    if (topic.getDraft() == null || topic.getDraft().equals(Boolean.TRUE))
    {
      log.debug("This topic is at draft stage {}", topic);
    }
      Iterator iter = getTopicItemsByUser(topic, userId, contextId);
      while (iter.hasNext())
      {
        DBMembershipItem item = (DBMembershipItem) iter.next();
        if (item.getPermissionLevel().getDeleteOwn().booleanValue()
            && forum.getDraft().equals(Boolean.FALSE)
            && forum.getLocked().equals(Boolean.FALSE)
            && topic.getDraft().equals(Boolean.FALSE)
            && topic.getLocked().equals(Boolean.FALSE))
        {
          return true;
        }
      }

    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
      return false;
    }
    return false;
  }

  /**   
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isMarkAsRead(org.sakaiproject.api.app.messageforums.DiscussionTopic,
   *      org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public boolean isMarkAsRead(DiscussionTopic topic, DiscussionForum forum)
  {
      log.debug("isMarkAsRead(DiscussionTopic {}, DiscussionForum {})", topic, forum);
    if (checkBaseConditions(topic, forum))
    {
      return true;
    }

    if (topic.getLocked() == null || topic.getLocked().equals(Boolean.TRUE))
    {
      log.debug("This topic is locked {}", topic);
      return false;
    }
    if (topic.getDraft() == null || topic.getDraft().equals(Boolean.TRUE))
    {
      log.debug("This topic is at draft stage {}", topic);
    }
    try
    {
      if (checkBaseConditions(topic, forum))
      {
        return true;
      }
        if (topic.getLocked() == null || topic.getLocked().equals(Boolean.TRUE))
    {
      log.debug("This topic is locked {}", topic);
      return false;
    }
    if (topic.getDraft() == null || topic.getDraft().equals(Boolean.TRUE))
    {
      log.debug("This topic is at draft stage {}", topic);
    }
      Iterator iter = getTopicItemsByCurrentUser(topic);
      while (iter.hasNext())
      {
        DBMembershipItem item = (DBMembershipItem) iter.next();
        if (item.getPermissionLevel().getMarkAsRead().booleanValue()
            && forum.getDraft().equals(Boolean.FALSE)
            && forum.getLocked().equals(Boolean.FALSE)
            && topic.getDraft().equals(Boolean.FALSE)
            && topic.getLocked().equals(Boolean.FALSE))
        {
          return true;
        }
      }

    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
      return false;
    }
    return false;

  }
  
  /**   
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isModerate(org.sakaiproject.api.app.messageforums.DiscussionTopic,
   *      org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public boolean isModeratePostings(DiscussionTopic topic, DiscussionForum forum){
	  return isModeratePostings(topic, forum, getCurrentUserId());
  }
  
  public boolean isModeratePostings(DiscussionTopic topic, DiscussionForum forum, String userId)
  {
	return isModeratePostings(topic, forum, userId, getContextId());
  }
  
  public boolean isModeratePostings(DiscussionTopic topic, DiscussionForum forum, String userId, String siteId)
  {
	  return isModeratePostings(topic.getId(), forum.getLocked(), forum.getDraft(), topic.getLocked(), topic.getDraft(), userId, siteId);
  }
  
  public boolean isModeratePostings(Long topicId, Boolean isForumLocked, Boolean isForumDraft, Boolean isTopicLocked, Boolean isTopicDraft, String userId, String siteId)
  {
    // NOTE: the forum or topic being locked should not affect a user's ability to moderate,
    // so logic related to the locked status was removed
    if (checkBaseConditions(null, null, userId, "/site/" + siteId))
    {
      return true;
    }
    try
    {
      if (checkBaseConditions(null, null, userId, "/site/" + siteId))
      {
        return true;
      }
      
    if (isTopicDraft == null || isTopicDraft.equals(Boolean.TRUE))
    {
      log.debug("This topic is at draft stage {}", topicId);
    }
      Iterator iter = getTopicItemsByUser(topicId, userId, siteId);
      while (iter.hasNext())
      {
        DBMembershipItem item = (DBMembershipItem) iter.next();
        if (item.getPermissionLevel().getModeratePostings().booleanValue()
            && isForumDraft.equals(Boolean.FALSE)
            && isTopicDraft.equals(Boolean.FALSE))
        {
          return true;
        }
      }

    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
      return false;
    }
    return false;
  }

  public boolean isIdentifyAnonAuthors(Topic topic)
  {
    String currentUserId = getCurrentUserId();
    if (isSuperUser(currentUserId))
    {
      return true;
    }

    try
    {
      Iterator iter = getTopicItemsByUser(topic.getId(), currentUserId, getContextId());
      while (iter.hasNext())
      {
        DBMembershipItem item = (DBMembershipItem) iter.next();
        if (item.getPermissionLevel().getIdentifyAnonAuthors())
        {
          return true;
        }
      }
    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
    }

    return false;
  }
  
  /*
   * (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#getCurrentUserMemberships()
   */
  public List getCurrentUserMemberships() {
	return getCurrentUserMemberships(getContextId());  
  }
  
  public List getCurrentUserMemberships(String siteId)
  {
	  List userMemberships = new ArrayList();
	  // first, add the user's role
	  final String currRole = getCurrentUserRole(siteId);
	  if (currRole != null && !currRole.isEmpty()) {
		  userMemberships.add(currRole);
	  }
	  // now, add any groups the user is a member of
	  try {
		  Site site = siteService.getSite(toolManager.getCurrentPlacement().getContext());
		  Collection groups = getGroupsWithMember(site, getCurrentUserId());
	
		  Iterator groupIter = groups.iterator();
		  while (groupIter.hasNext())
		  {
			  Group currentGroup = (Group) groupIter.next();  
			  if (currentGroup != null) {
				  userMemberships.add(currentGroup.getTitle());
			  }
		  }
	  } catch (IdUnusedException iue) {
		  log.debug("No memberships found");
	  }
	  
	  return userMemberships;
  }

  
  private Iterator getGroupsByCurrentUser()
  {
    List memberof = new ArrayList();
    try
    {
      Site site = siteService.getSite(toolManager.getCurrentPlacement().getContext());
	  Collection groups = getGroupsWithMember(site, getCurrentUserId());
      for (Iterator groupIterator = groups.iterator(); groupIterator.hasNext();)
      {
        Group currentGroup = (Group) groupIterator.next();
        memberof.add(currentGroup.getId());
      }
    }
    catch (IdUnusedException e)
    {
      log.debug("Group not found");
    }
    return memberof.iterator();
  }
  
  /**
   * Returns a list of names of the groups/sections
   * the current user is a member of
   * @return
   */
  private Iterator getGroupNamesByCurrentUser(String siteId)
  {
    List memberof = new ArrayList();
    try
    {
    	Site site = siteService.getSite(siteId);
  	  Collection groups = getGroupsWithMember(site, getCurrentUserId());
      
      for (Iterator groupIterator = groups.iterator(); groupIterator.hasNext();)
      {
        Group currentGroup = (Group) groupIterator.next();

        memberof.add(currentGroup.getTitle());
      }
    }
    catch (IdUnusedException e)
    {
      log.debug("Group not found");
    }
    return memberof.iterator();
  }

  private DBMembershipItem getAreaItemByUserRole()
  { 
  	if (log.isDebugEnabled())
    {
      log.debug("getAreaItemByUserRole()");
    }	 
    Set membershipItems = forumManager.getDiscussionForumArea()
      .getMembershipItemSet();
    return forumManager.getDBMember(membershipItems, getCurrentUserRole(),
      DBMembershipItem.TYPE_ROLE);
  }
  
  private Iterator getAreaItemsByCurrentUser()
  { 
    log.debug("getAreaItemsByCurrentUser()");

  	List areaItems = new ArrayList();
  	
		if (threadLocalManager.get("message_center_permission_set") == null || !((Boolean)threadLocalManager.get("message_center_permission_set")).booleanValue())
		{
			initMembershipForSite();
		}

//		Set membershipItems = forumManager.getDiscussionForumArea()
//      .getMembershipItemSet();
		Set areaItemsInThread = (Set) threadLocalManager.get("message_center_membership_area");
    DBMembershipItem item = forumManager.getDBMember(areaItemsInThread, getCurrentUserRole(),
      DBMembershipItem.TYPE_ROLE);
    
    if (item != null){
        areaItems.add(item);
    }
    
    //  for group awareness
    try
    {
      	Collection groups = null;
      	try
      	{
      		Site currentSite = siteService.getSite(getContextId());
       	    groups = getGroupsWithMember(currentSite, getCurrentUserId());
      	}
        catch(IdUnusedException iue)
        {
        	log.error(iue.getMessage(), iue);
        }
    	if(groups != null)
    	{
    		for (Iterator groupIterator = groups.iterator(); groupIterator.hasNext();)
    		{
    			Group currentGroup = (Group) groupIterator.next();  

				DBMembershipItem groupItem = forumManager.getDBMember(areaItemsInThread, currentGroup.getTitle(),
						DBMembershipItem.TYPE_GROUP);
				if (groupItem != null){
					areaItems.add(groupItem);
				}

    		}
    	}
    }
    catch(Exception iue)
    {
    	log.error(iue.getMessage(), iue);
    }
    
    return areaItems.iterator();
  }

  public Set getAreaItemsSet(Area area)
  {
		if (threadLocalManager.get("message_center_permission_set") == null || !((Boolean)threadLocalManager.get("message_center_permission_set")).booleanValue())
		{
			initMembershipForSite();
		}
		Set allAreaSet = (Set) threadLocalManager.get("message_center_membership_area");
		Set returnSet = new HashSet();
		if(allAreaSet != null)
		{
			Iterator iter = allAreaSet.iterator();
			while(iter.hasNext())
			{
				DBMembershipItemImpl thisItem = (DBMembershipItemImpl)iter.next();
				if(thisItem.getArea() != null && area.getId() != null && area.getId().equals(thisItem.getArea().getId()))
				{
					returnSet.add((DBMembershipItem)thisItem);
				}
			}
		}

		return returnSet;
  }
  
  private Iterator getForumItemsByCurrentUser(DiscussionForum forum)
  {
    List forumItems = new ArrayList();
    //Set membershipItems = forum.getMembershipItemSet();
    

		if (threadLocalManager.get("message_center_permission_set") == null || !((Boolean)threadLocalManager.get("message_center_permission_set")).booleanValue())
		{
			initMembershipForSite();
		}

		Set forumItemsInThread = (Set) threadLocalManager.get("message_center_membership_forum");
		Set thisForumItemSet = new HashSet();
		Iterator iter = forumItemsInThread.iterator();
		while(iter.hasNext())
		{
			DBMembershipItemImpl thisItem = (DBMembershipItemImpl)iter.next();
			if(thisItem.getForum() != null && forum.getId()!=null&&forum.getId().equals(thisItem.getForum().getId()))
			{
				thisForumItemSet.add((DBMembershipItem)thisItem);
			}
		}
		if(thisForumItemSet.size()==0&&getAnonRole()==true&&".anon".equals(forum.getCreatedBy())&&forum.getTopicsSet()==null){
			Set newForumMembershipset=forum.getMembershipItemSet();
	        Iterator iterNewForum = newForumMembershipset.iterator();
	        while (iterNewForum.hasNext())
	        {
	          DBMembershipItem item = (DBMembershipItem)iterNewForum.next();
	          if (".anon".equals(item.getName()))
	          {
	        	  thisForumItemSet.add(item);
	          }       
	        }			
		}
    
//    DBMembershipItem item = forumManager.getDBMember(membershipItems, getCurrentUserRole(),
//        DBMembershipItem.TYPE_ROLE);
		DBMembershipItem item = forumManager.getDBMember(thisForumItemSet, getCurrentUserRole(),
			DBMembershipItem.TYPE_ROLE);
    
    if (item != null){
      forumItems.add(item);
    }
    
	//  for group awareness
    try
    {
      	Collection groups = null;
      	try
      	{
      		Site currentSite = siteService.getSite(getContextId());
      		groups = getGroupsWithMember(currentSite, getCurrentUserId());
      	}
        catch(IdUnusedException iue)
        {
        	log.error(iue.getMessage(), iue);
        }
    	if(groups != null)
    	{
    		for (Iterator groupIterator = groups.iterator(); groupIterator.hasNext();)
    		{
    			Group currentGroup = (Group) groupIterator.next();  

				DBMembershipItem groupItem = forumManager.getDBMember(thisForumItemSet, currentGroup.getTitle(),
						DBMembershipItem.TYPE_GROUP);
				if (groupItem != null){
					forumItems.add(groupItem);
				}
    		}
    	}
    }
    catch(Exception iue)
    {
    	log.error(iue.getMessage(), iue);
    }

//    Iterator iter = membershipItems.iterator();
//    while (iter.hasNext())
//    {
//      DBMembershipItem membershipItem = (DBMembershipItem) iter.next();
//      if (membershipItem.getType().equals(DBMembershipItem.TYPE_ROLE)
//          && membershipItem.getName().equals(getCurrentUserRole()))
//      {
//        forumItems.add(membershipItem);
//      }
//      if (membershipItem.getType().equals(DBMembershipItem.TYPE_GROUP)
//          && isGroupMember(membershipItem.getName()))
//      {
//        forumItems.add(membershipItem);
//      }
//    }
    return forumItems.iterator();
  }

  public Set getForumItemsSet(DiscussionForum forum)
  {
		if (threadLocalManager.get("message_center_permission_set") == null || !((Boolean)threadLocalManager.get("message_center_permission_set")).booleanValue())
		{
			initMembershipForSite();
		}

		Set allForumSet = (Set) threadLocalManager.get("message_center_membership_forum");
		Set returnSet = new HashSet();
		Iterator iter = allForumSet.iterator();
		while(iter.hasNext())
		{
			DBMembershipItemImpl thisItem = (DBMembershipItemImpl)iter.next();
			if(thisItem.getForum() != null && forum.getId() != null && forum.getId().equals(thisItem.getForum().getId()))
			{
				returnSet.add((DBMembershipItem)thisItem);
			}
		}

		return returnSet;
  }
  
  private Iterator getTopicItemsByCurrentUser(DiscussionTopic topic){
	  return getTopicItemsByUser(topic, getCurrentUserId());
  }
  
  private Iterator getTopicItemsByUser(DiscussionTopic topic, String userId){
	  return getTopicItemsByUser(topic, userId, getContextId());
  }
  
  private Iterator getTopicItemsByUser(DiscussionTopic topic, String userId, String siteId)
  {
	  return getTopicItemsByUser(topic.getId(), userId, siteId);
  }
  
  private Iterator getTopicItemsByUser(Long topicId, String userId, String siteId)
  {
	  List topicItems = new ArrayList();
    
		if (threadLocalManager.get("message_center_permission_set") == null || !((Boolean)threadLocalManager.get("message_center_permission_set")).booleanValue())
		{
			initMembershipForSite(siteId, userId);
		}

		Set topicItemsInThread = (Set) threadLocalManager.get("message_center_membership_topic");
		Set thisTopicItemSet = new HashSet();
		Iterator iter = topicItemsInThread.iterator();
		while(iter.hasNext())
		{
			DBMembershipItemImpl thisItem = (DBMembershipItemImpl)iter.next();
			if(thisItem.getTopic() != null && topicId.equals(thisItem.getTopic().getId()))
			{
				thisTopicItemSet.add((DBMembershipItem)thisItem);
			}
		}
    
//    Set membershipItems = topic.getMembershipItemSet();
    DBMembershipItem item = forumManager.getDBMember(thisTopicItemSet, getUserRole(siteId, userId),
        DBMembershipItem.TYPE_ROLE, "/site/" + siteId);

    if (item != null){
      topicItems.add(item);
    }

    //for group awareness
    try
    {
      	Collection groups = null;
      	try
      	{
      		Site currentSite = siteService.getSite(siteId);
      		groups = getGroupsWithMember(currentSite, userId);
      	}
        catch(IdUnusedException iue)
        {
        	log.error(iue.getMessage(), iue);
        }
    	if(groups != null)
    	{
    		for (Iterator groupIterator = groups.iterator(); groupIterator.hasNext();)
    		{
    			Group currentGroup = (Group) groupIterator.next();  

				DBMembershipItem groupItem = forumManager.getDBMember(thisTopicItemSet, currentGroup.getTitle(),
						DBMembershipItem.TYPE_GROUP, "/site/" + siteId);
				if (groupItem != null){
					topicItems.add(groupItem);
    			}
    		}
    	}
    }
    catch(Exception iue)
    {
    	log.error(iue.getMessage(), iue);
    }
    
//    Iterator iter = membershipItems.iterator();
//    while (iter.hasNext())
//    {
//      DBMembershipItem membershipItem = (DBMembershipItem) iter.next();
//      if (membershipItem.getType().equals(DBMembershipItem.TYPE_ROLE)
//          && membershipItem.getName().equals(getCurrentUserRole()))
//      {
//        topicItems.add(membershipItem);
//      }
//      if (membershipItem.getType().equals(DBMembershipItem.TYPE_GROUP)
//          && isGroupMember(membershipItem.getName()))
//      {
//        topicItems.add(membershipItem);
//      }
//    }
    return topicItems.iterator();
  }
  
  public Set getTopicItemsSet(DiscussionTopic topic)
  {
		if (threadLocalManager.get("message_center_permission_set") == null || !((Boolean)threadLocalManager.get("message_center_permission_set")).booleanValue())
		{
			initMembershipForSite();
		}

		Set allTopicSet = (Set) threadLocalManager.get("message_center_membership_topic");
		Set returnSet = new HashSet();
		Iterator iter = allTopicSet.iterator();
		while(iter.hasNext())
		{
			DBMembershipItemImpl thisItem = (DBMembershipItemImpl)iter.next();
			if(thisItem.getTopic() != null && topic.getId() != null && topic.getId().equals(thisItem.getTopic().getId()))
			{
				returnSet.add((DBMembershipItem)thisItem);
			}
		}
		
		return returnSet;
  }
  
  /**
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#isInstructor()
   */
  public boolean isInstructor()
  {
    log.debug("isInstructor()");
    return isInstructor(userDirectoryService.getCurrentUser());
  }

  /**
   * Check if the given user has site.upd access
   * 
   * @param user
   * @return
   */
  private boolean isInstructor(User user)
  {
    log.debug("isInstructor(User {})", user);
    if (user != null)
      return securityService.unlock(user, "site.upd", getContextSiteId());
    else
      return false;
  }

  /**
   * @return siteId
   */
  private String getContextSiteId()
  {
    log.debug("getContextSiteId()");
    return ("/site/" + toolManager.getCurrentPlacement().getContext());
  }

  public void setPermissionLevelManager(
      PermissionLevelManager permissionLevelManager)
  {
    this.permissionLevelManager = permissionLevelManager;
  }
  
  /**
   * @return
   */
  private String getCurrentUserId()
  {
    log.debug("getCurrentUserId()");
    if (TestUtil.isRunningTests())
    {
      return "test-user";
    }
    if(sessionManager.getCurrentSessionUserId()==null&&getAnonRole()==true){
    	return ".anon";
    }    	

    return sessionManager.getCurrentSessionUserId();
  }

  /**
   * @return
   */
  private String getCurrentUserRole() {
	  return getCurrentUserRole(getContextId());
  }
  
  private String getCurrentUserRole(String siteId)
  {
	  log.debug("getCurrentUserRole()");
	  if(authzGroupService.getUserRole(getCurrentUserId(), "/site/" + siteId)==null&&sessionManager.getCurrentSessionUserId()==null&&getAnonRole(siteId)==true){
		  return ".anon";
	  }
	  return authzGroupService.getUserRole(getCurrentUserId(), "/site/" + siteId);
  }

  private String getUserRole(String siteId, String userId)
  {
    log.debug("getCurrentUserRole()");
    Map roleMap = (Map) threadLocalManager.get("message_center_user_role_map");
    if(roleMap == null){
    	roleMap = new HashMap();
    }
    String userRole = (String) roleMap.get(siteId + "-" + userId);
    if(userRole == null){
    	userRole = authzGroupService.getUserRole(userId, "/site/" + siteId);
    	roleMap.put(siteId + "-" + userId, userRole);
    	threadLocalManager.set("message_center_user_role_map", roleMap);
    }
    
    // if user role is still null at this point, check for .anon
    if(userRole == null && userId == null && getAnonRole("/site/" + siteId) == true){
        return ".anon";
    }
    
    return userRole;
  }
   
   public boolean  getAnonRole()
    {
	 return  forumManager.getAnonRole();	   
    }
   
   public boolean  getAnonRole(String contextSiteId)
   {
	 return  forumManager.getAnonRole(contextSiteId);	   
   }
  /**
   * @return
   */
  private String getContextId()
  {
    log.debug("getContextId()");
    if (TestUtil.isRunningTests())
    {
      return "test-context";
    }
    Placement placement = toolManager.getCurrentPlacement();
    String presentSiteId = placement.getContext();
    return presentSiteId;
  }

  /**
   * @return
   */
  
  
  private boolean isSuperUser(){
	  return isSuperUser(getCurrentUserId());
  }
  
  
  private boolean isSuperUser(String userId)
  {
    log.debug(" isSuperUser()");
    return securityService.isSuperUser(userId);
  }

  
  /**
   * @param topic
   * @param forum
   * @return
   */
  private boolean checkBaseConditions(DiscussionTopic topic,
	      DiscussionForum forum){
	  return checkBaseConditions(topic, forum, getCurrentUserId(), getContextId());
  }
  

  
  private boolean checkBaseConditions(DiscussionTopic topic,
		  DiscussionForum forum, String userId, String contextSiteId)
  {
    log.debug("checkBaseConditions(DiscussionTopic {}, DiscussionForum {})", topic, forum);
    if (isSuperUser(userId))
    {
      return true;
    }
    return false;
  }
  
  private boolean isRoleMember(String roleId)
  {
    log.debug("isRoleMember(String {})", roleId);
    if (getCurrentUserRole().equals(roleId))
    {
      return true;
    }
    return false;
  }

  private boolean isGroupMember(String groupId)
  {
    log.debug("setAuthzGroupService(AuthzGroupService {})", authzGroupService);
    try
    {
      Site site = siteService.getSite(toolManager.getCurrentPlacement().getContext());
      Collection groups = getGroupsWithMember(site, getCurrentUserId());
      for (Iterator groupIterator = groups.iterator(); groupIterator.hasNext();)
      {
        Group currentGroup = (Group) groupIterator.next();
        if (currentGroup.getId().equals(groupId))
        {
            return true;
        }
      }
    }
    catch (IdUnusedException e)
    {
      log.debug("Group with id {} not found", groupId);
      return false;
    }

    return false;
  }
  
  private void initMembershipForSite(){
	  initMembershipForSite(getContextId());
  }
  
  
  private void initMembershipForSite(String contextSiteId){
	  initMembershipForSite(contextSiteId, getCurrentUserId());
  }
  
  private void initMembershipForSite(String siteId, String userId)
  {
		if (threadLocalManager.get("message_center_permission_set") != null && ((Boolean)threadLocalManager.get("message_center_permission_set")).booleanValue())
		{
			return;
		}
		Area dfa = forumManager.getDiscussionForumArea(siteId);
    Set areaItems = dfa.getMembershipItemSet();
  	List forumItemsList = permissionLevelManager.getAllMembershipItemsForForumsForSite(dfa.getId());
  	List topicItemsList = permissionLevelManager.getAllMembershipItemsForTopicsForSite(dfa.getId());

  	Set forumItems = new HashSet();
  	for(Iterator i = forumItemsList.iterator(); i.hasNext();) {
  		DBMembershipItem forumItem = (DBMembershipItemImpl)i.next();
  		forumItems.add(forumItem);
  	}
  	
  	Set topicItems = new HashSet();
  	for(Iterator i = topicItemsList.iterator(); i.hasNext();) {
  		DBMembershipItem topicItem = (DBMembershipItemImpl)i.next();
  		topicItems.add(topicItem);
  	}
  
  	Collection groups = null;
  	try
  	{
  		Site currentSite = siteService.getSite(siteId);
  		groups = getGroupsWithMember(currentSite, userId);
  	}
    catch(IdUnusedException iue)
    {
    	log.error(iue.getMessage(), iue);
    }

   	threadLocalManager.set("message_center_current_member_groups", groups);
  	threadLocalManager.set("message_center_membership_area", areaItems);
  	threadLocalManager.set("message_center_membership_forum", forumItems);
  	threadLocalManager.set("message_center_membership_topic", topicItems);
	threadLocalManager.set("message_center_permission_set", Boolean.valueOf(true));
  }
  
  public Collection getGroupsWithMember(Site site, String userId){
	  String id = site.getReference() + "/" + userId;
	  Object el = userGroupMembershipCache.get(id);
	  if(el == null){
		  Collection groups = site.getGroupsWithMember(userId);
		  userGroupMembershipCache.put(id, groups);
		  return groups;
	  }else{
		  return (Collection) el;
	  }
	  
	}
	
	public MemoryService getMemoryService() {
		return memoryService;
	}
	
	public void setMemoryService(MemoryService memoryService) {
		this.memoryService = memoryService;
	}
}
