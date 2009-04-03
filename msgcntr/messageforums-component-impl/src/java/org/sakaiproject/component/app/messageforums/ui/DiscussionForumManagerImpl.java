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
 *       http://www.osedu.org/licenses/ECL-2.0
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.ActorPermissions;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaControlPermission;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.DBMembershipItem;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.DummyDataHelperApi;
import org.sakaiproject.api.app.messageforums.ForumControlPermission;
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
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.component.app.messageforums.MembershipItem;
import org.sakaiproject.component.app.messageforums.dao.hibernate.ActorPermissionsImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.DBMembershipItemImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.MessageForumsUserImpl;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 */
public class DiscussionForumManagerImpl extends HibernateDaoSupport implements
    DiscussionForumManager {
  private static final String MC_DEFAULT = "mc.default.";
  private static final Log LOG = LogFactory
      .getLog(DiscussionForumManagerImpl.class);
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
  private Map courseMemberMap = null;
  private boolean usingHelper = false; // just a flag until moved to database from helper
  private ContentHostingService contentHostingService;
  
  public static final int MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST = 1000;

  public void init()
  {
     LOG.info("init()");
    ;
  }
  
  public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}

  public List searchTopicMessages(Long topicId, String searchText)
  {
    return forumManager.searchTopicMessages(topicId, searchText);
  }

  public Topic getTopicByIdWithAttachments(Long topicId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getTopicByIdWithAttachments(Long " + topicId + ")");
    }
    return forumManager.getTopicByIdWithAttachments(topicId);
  }

  public List getTopicsByIdWithMessages(final Long forumId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getTopicsByIdWithMessages(final Long" + forumId + ")");
    }
    return forumManager.getTopicsByIdWithMessages(forumId);
  }

  public List getTopicsByIdWithMessagesAndAttachments(final Long forumId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getTopicsByIdWithMessagesAndAttachments(final Long" + forumId
          + ")");
    }
    return forumManager.getTopicsByIdWithMessagesAndAttachments(forumId);
  }
  
  public List getTopicsByIdWithMessagesMembershipAndAttachments(final Long forumId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getTopicsByIdWithMessagesMembershipAndAttachments(final Long" + forumId
          + ")");
    }
    return forumManager.getTopicsByIdWithMessagesMembershipAndAttachments(forumId);
  }

  public Topic getTopicByIdWithMessages(final Long topicId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getTopicByIdWithMessages(final Long" + topicId + ")");
    }
    return forumManager.getTopicByIdWithMessages(topicId);
  }
  
  public Topic getTopicWithAttachmentsById(final Long topicId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getTopicWithAttachmentsById(final Long" + topicId + ")");
    }
    return forumManager.getTopicWithAttachmentsById(topicId);
  }

  public Topic getTopicByIdWithMessagesAndAttachments(final Long topicId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getTopicByIdWithMessagesAndAttachments(final Long" + topicId
          + ")");
    }
    return forumManager.getTopicByIdWithMessagesAndAttachments(topicId);
  }
  
  public List getModeratedTopicsInSite()
  {
	  if (LOG.isDebugEnabled())
	  {
		  LOG.debug("getModeratedTopicsInSite()");
	  }
	  return forumManager.getModeratedTopicsInSite(ToolManager.getCurrentPlacement().getContext());
  }

  // start injection
  /**
   * @param helper
   */
  public void setHelper(DummyDataHelperApi helper)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("setHelper(DummyDataHelperApi " + helper + ")");
    }
    this.helper = helper;
  }

  /**
   * @param areaManager
   */
  public void setAreaManager(AreaManager areaManager)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("setAreaManager(AreaManager" + areaManager + ")");
    }
    this.areaManager = areaManager;
  }

  /**
   * @param permissionManager
   *          The permissionManager to set.
   */
  public void setPermissionManager(PermissionManager permissionManager)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("setPermissionManager(PermissionManager" + permissionManager
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
    if (LOG.isDebugEnabled())
    {
      LOG.debug("setTypeManager(MessageForumsTypeManager" + typeManager + ")");
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

    LOG.debug("getMessageManager()");

    return messageManager;
  }

  /**
   * @param messageManager
   */
  public void setMessageManager(MessageForumsMessageManager messageManager)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("setMessageManager(MessageForumsMessageManager"
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
    LOG.debug("getDiscussionForumArea");

    if (usingHelper)
    {
      return helper.getDiscussionForumArea();
    }
    return areaManager.getDiscusionArea();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getMessageById(java.lang.Long)
   */
  public Message getMessageById(Long id)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getMessageById( Long" + id + ")");
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
  public void saveMessage(Message message) {
	  saveMessage(message, true);
  }
  
  public void saveMessage(Message message, boolean logEvent)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("saveMessage(Message " + message + ")");
    }
    if (message.getTopic().getBaseForum() == null)
    {
      message.setTopic(getTopicById(message.getTopic().getId()));
    }
    messageManager.saveMessage(message, logEvent);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#deleteMessage(org.sakaiproject.api.app.messageforums.Message)
   */
  public void deleteMessage(Message message)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("deleteMessage(Message" + message + ")");
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
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getTotalNoMessages(Topic" + topic + ")");
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
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getTotalViewableMessagesWhenMod(Topic" + topic + ")");
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
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getUnreadNoMessages(Topic" + topic + ")");
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
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getNumUnreadViewableMessagesWhenMod(Topic" + topic + ")");
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
		  LOG.error("approveAllPendingMessages failed with topicId: " + topicId);
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
  public List getPendingMsgsInSiteByMembership(List membershipList)
  {
	  return messageManager.getPendingMsgsInSiteByMembership(membershipList);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getDiscussionForums()
   */
  public List getDiscussionForums()
  {
    LOG.debug("getDiscussionForums()");
    if (usingHelper)
    {
      return helper.getDiscussionForumArea().getDiscussionForums();
    }
    return forumManager.getForumByTypeAndContext(typeManager
        .getDiscussionForumType());
    // return getDiscussionForumArea().getDiscussionForums();
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getDiscussionForumsByContextId()
   */
  public List getDiscussionForumsByContextId(String contextId)
  {
    LOG.debug("getDiscussionForumsByContextId(String contextId)");
    
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
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getForumById(Long" + forumId + ")");
    }
    if (usingHelper)
    {
      return helper.getForumById(forumId);
    }
    return (DiscussionForum) forumManager.getForumById(true, forumId);
  }

  public DiscussionForum getForumByUuid(String forumId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getForumByUuid(String" + forumId + ")");
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
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getMessagesByTopicId(Long" + topicId + ")");
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
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getTopicById(Long" + topicId + ")");
    }

    return (DiscussionTopic) forumManager.getTopicById(true, topicId);
  }

  public DiscussionForum getForumByIdWithTopics(Long forumId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getForumByIdWithTopics(Long" + forumId + ")");
    }
    return (DiscussionForum) forumManager.getForumByIdWithTopics(forumId);
  }
  
  public DiscussionForum getForumByIdWithTopicsAttachmentsAndMessages(Long forumId) {
	  if (LOG.isDebugEnabled()) { LOG.debug("getForumByIdWithTopicsAttachmentsAndMessages(Long " + forumId + ")"); }
	  return (DiscussionForum) forumManager.getForumByIdWithTopicsAttachmentsAndMessages(forumId);
  }

  public DiscussionTopic getTopicByUuid(String topicId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug(" getTopicByUuid(String" + topicId + ")");
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
    if (LOG.isDebugEnabled())
    {
      LOG.debug("hasNextTopic(DiscussionTopic" + topic + ")");
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
        DiscussionTopic t = (DiscussionTopic) iter.next();
        if (next && getTopicAccess(t))
        {
          return true;
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
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#hasPreviousTopic(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public boolean hasPreviousTopic(DiscussionTopic topic)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("hasPreviousTopic(DiscussionTopic" + topic + ")");
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
        DiscussionTopic t = (DiscussionTopic) iter.next();
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
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getNextTopic(DiscussionTopic" + topic + ")");
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
        DiscussionTopic t = (DiscussionTopic) iter.next();
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
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getPreviousTopic(DiscussionTopic" + topic + ")");
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
        DiscussionTopic t = (DiscussionTopic) iter.next();
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
    LOG.debug("isInstructor()");
    return isInstructor(userDirectoryService.getCurrentUser());
  }

  public boolean isInstructor(String userId, String siteId) {
    LOG.debug("isInstructor(String " + userId + ", " + siteId + ")");
    return isInstructor(userDirectoryService.getCurrentUser(), siteId);
  }

  /**
   * Check if the given user has site.upd access
   * 
   * @param user
   * @return
   */
  private boolean isInstructor(User user)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isInstructor(User " + user + ")");
    }
    if (user != null)
      return isInstructor(user, getContextSiteId());
    else
      return false;
  }

  /**
   * Check if the given user has site.upd access
   * 
   * @param user
   * @param siteId
   * @return
   */
  private boolean isInstructor(User user, String siteId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isInstructor(User " + user + ", " + siteId + ")");
    }
    if (user != null)
      return securityService.unlock(user, "site.upd", siteId);
    else
      return false;
  }

  /**
   * @return siteId
   */
  private String getContextSiteId()
  {
    LOG.debug("getContextSiteId()");
    return "/site/" + getCurrentContext();
  }
  
  /**
   * 
   * @return the current context without the "/site/" prefix
   */
  private String getCurrentContext() {
      return ToolManager.getCurrentPlacement().getContext();
  }

  /**
   * @param forumManager
   */
  public void setForumManager(MessageForumsForumManager forumManager)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("setForumManager(MessageForumsForumManager" + forumManager
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
    LOG.debug("createForum()");
    return forumManager.createDiscussionForum();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#deleteForum(org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public void deleteForum(DiscussionForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("setForumManager(DiscussionForum" + forum + ")");
    }
    forumManager.deleteDiscussionForum(forum);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#createTopic(org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public DiscussionTopic createTopic(DiscussionForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("createTopic(DiscussionForum" + forum + ")");
    }
    if (forum == null)
    {
      LOG.debug("Attempt to create topic with out forum");
      return null;
    }
    return forumManager.createDiscussionForumTopic(forum);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#saveForum(org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public void saveForum(DiscussionForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("saveForum(DiscussionForum" + forum + ")");
    }
    saveForum(forum, false, getCurrentContext());
  }
  
  public void saveForum(String contextId, DiscussionForum forum) {
      if (LOG.isDebugEnabled()) LOG.debug("saveForum(String contextId, DiscussionForum forum)");
      
      if (contextId == null || forum == null) {
          throw new IllegalArgumentException("Null contextId or forum passed to saveForum. contextId:" + contextId);
      }
      
      saveForum(forum, forum.getDraft(), contextId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#saveForumAsDraft(org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public void saveForumAsDraft(DiscussionForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("saveForumAsDraft(DiscussionForum" + forum + ")");
    }
    saveForum(forum, true, getCurrentContext());
  }

  private void saveForum(DiscussionForum forum, boolean draft, String contextId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("saveForum(DiscussionForum" + forum + "boolean " + draft + ")");
    }

    boolean saveArea = forum.getId() == null;
    forum.setDraft(new Boolean(draft));
//    ActorPermissions originalForumActorPermissions = null;
//    if (saveArea)
//    {
//      originalForumActorPermissions = new ActorPermissionsImpl();
//    }
//    else
//    {
//      originalForumActorPermissions = forum.getActorPermissions();
//    }
//    // setcontributors
//    List holdContributors = new ArrayList();
//    holdContributors = Arrays.asList(forum.getActorPermissions()
//        .getContributors().toArray());
//    originalForumActorPermissions.setContributors(new UniqueArrayList());// clearing list at this
//    // point.
//    if (holdContributors != null && holdContributors.size() > 0)
//    {
//      Iterator iter = holdContributors.iterator();
//      while (iter.hasNext())
//      {
//        MessageForumsUser user = (MessageForumsUser) iter.next();
//        forum.getActorPermissions().addContributor(user);
//      }
//    }
//    // setAccessors
//    List holdAccessors = new ArrayList();
//    holdAccessors = Arrays.asList(forum.getActorPermissions().getAccessors()
//        .toArray());
//    originalForumActorPermissions.setAccessors(new UniqueArrayList());// clearing list at this point.
//    if (holdAccessors != null && holdAccessors.size() > 0)
//    {
//      Iterator iter = holdAccessors.iterator();
//      while (iter.hasNext())
//      {
//        MessageForumsUser user = (MessageForumsUser) iter.next();
//        forum.getActorPermissions().addAccesssor(user);
//      }
//    }
    
    
    forumManager.saveDiscussionForum(forum, draft);

    if (saveArea)
    {
      //Area area = getDiscussionForumArea();
      String dfType = typeManager.getDiscussionForumType();
      Area area = areaManager.getAreaByContextIdAndTypeId(contextId, dfType);
      forum.setArea(area);
      forum.setSortIndex(new Integer(0));
      area.addDiscussionForum(forum);
      areaManager.saveArea(area);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#saveTopic(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public void saveTopic(DiscussionTopic topic)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("saveTopic(DiscussionTopic" + topic + ")");
    }
    saveTopic(topic, false);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#saveTopicAsDraft(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public void saveTopicAsDraft(DiscussionTopic topic)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("saveTopicAsDraft(DiscussionTopic" + topic + ")");
    }
    saveTopic(topic, true);
  }

  private void saveTopic(DiscussionTopic topic, boolean draft)
  {
    LOG
        .debug("saveTopic(DiscussionTopic " + topic + ", boolean " + draft
            + ")");

    boolean saveForum = topic.getId() == null;
    topic.setDraft(new Boolean(draft));
    forumManager.saveDiscussionForumTopic(topic);
    if (saveForum)
    {
      DiscussionForum forum = (DiscussionForum) topic.getBaseForum();
      forum.addTopic(topic);
      forumManager.saveDiscussionForum(forum, forum.getDraft().booleanValue());
      //sak-5146 forumManager.saveDiscussionForum(forum);
    }
    
    if (topic.getId() == null) {
        EventTrackingService.post(EventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_TOPIC_ADD, getEventMessage(topic), false));
    } else {
        EventTrackingService.post(EventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_TOPIC_REVISE, getEventMessage(topic), false));
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#deleteTopic(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public void deleteTopic(DiscussionTopic topic)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("deleteTopic(DiscussionTopic " + topic + ")");
    }
    forumManager.deleteDiscussionForumTopic(topic);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getDefaultControlPermissions()
   */
  public List getDefaultControlPermissions()
  {
    LOG.debug("getDefaultControlPermissions()");
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
    LOG.debug("getAreaControlPermissions()");
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
    LOG.debug("getAreaMessagePermissions()");
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
    LOG.debug("getDefaultMessagePermissions()");
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
    if (LOG.isDebugEnabled())
    {
      LOG.debug("saveDefaultMessagePermissions(List " + messagePermissions
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
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getForumControlPermissions(DiscussionForum " + forum + ")");
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
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getForumMessagePermissions(DiscussionForum " + forum + ")");
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
    LOG.debug("getTopicControlPermissions(DiscussionTopic " + topic + ")");

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
    LOG.debug("getTopicMessagePermissions(DiscussionTopic " + topic + ")");

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
    if (LOG.isDebugEnabled())
    {
      LOG.debug("saveAreaControlPermissions(List" + controlpermissions + ")");
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
    if (LOG.isDebugEnabled())
    {
      LOG.debug("saveForumControlPermissions(List " + controlPermissions + ")");
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
    if (LOG.isDebugEnabled())
    {
      LOG.debug("saveForumMessagePermissions(List " + messagePermissions + ")");
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
    if (LOG.isDebugEnabled())
    {
      LOG.debug("saveTopicControlPermissions(List " + controlPermissions + ")");
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
    if (LOG.isDebugEnabled())
    {
      LOG.debug("saveTopicMessagePermissions(List " + messagePermissions + ")");
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
    LOG.debug("getRoles()");
    List roleList = new ArrayList();
    AuthzGroup realm = null;
    try
    {
      realm = AuthzGroupService.getAuthzGroup(getContextSiteId());
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
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    Collections.sort(roleList);
    return roleList.iterator();
  }

  public void markMessageAs(Message message, boolean readStatus)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("markMessageAsRead(Message" + message + ")");
    }
    try
    {
      messageManager.markMessageReadForUser(message.getTopic().getId(), message
          .getId(), readStatus);
    }
    catch (Exception e)
    {
      LOG.error(e.getMessage(), e);
    }

  }
  
  public void markMessageReadStatusForUser(Message message, boolean readStatus, String userId)
  {
	  if (LOG.isDebugEnabled())
	  {
		  LOG.debug("markMessageReadStatusForUser(Message" + message + " readStatus:" + readStatus + " userId: " + userId + ")");
	  }
	  try
	  {
		  messageManager.markMessageReadForUser(message.getTopic().getId(), message
				  .getId(), readStatus, userId);
	  }
	  catch (Exception e)
	  {
		  LOG.error(e.getMessage(), e);
	  }
  }
  
  /**
   * @param forum
   * @return
   */
  public boolean isForumOwner(DiscussionForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isForumOwner(DiscussionForum " + forum + ")");
    }
    if (forum.getCreatedBy().equals(userDirectoryService.getCurrentUser()) && !isRoleSwapView())
    {
      return true;
    }
    return false;
  }
  
  private boolean isRoleSwapView()
  {
  	String roleswap = (String)sessionManager.getCurrentSession().getAttribute("roleswap" + getContextSiteId());
  	if (roleswap!=null)
  		return true;
  	return false;
  }

  /**
   * @param topic
   * @return
   */
  public boolean isTopicOwner(DiscussionTopic topic)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isTopicOwner(DiscussionTopic " + topic + ")");
    }
    if (topic.getCreatedBy().equals(userDirectoryService.getCurrentUser()) && !isRoleSwapView())
    {
      return true;
    }
    return false;
  }

  private boolean getTopicAccess(DiscussionTopic t)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getTopicAccess(DiscussionTopic" + t + ")");
    }
    //SAK-12685 If topic's permission level name is "None", then can't access 
    boolean nonePermission = false;
    User user=userDirectoryService.getCurrentUser();
    String role=AuthzGroupService.getUserRole(user.getId(), getContextSiteId());
    Set membershipItemSet = t.getMembershipItemSet();
    Iterator it = membershipItemSet.iterator();
    while(it.hasNext()) {
    	DBMembershipItem membershipItem =(DBMembershipItem)it.next();
    	String roleName = membershipItem.getName();
    	String permissionName = membershipItem.getPermissionLevelName();
    	if(roleName.equals(role) && permissionName.equals(PermissionLevelManager.PERMISSION_LEVEL_NAME_NONE)){
    		nonePermission = true;
    	}    	
    }   

    if ((t.getDraft().equals(Boolean.FALSE) && !nonePermission)
    		|| isInstructor()
            || securityService.isSuperUser()
            || isTopicOwner(t))
    {
      return true;
    }
    return false;
  }

  /**
   * @param accessorList
   * @return
   */
  private List decodeActorPermissionTypeList(List selectedList)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("decodeActorPermissionTypeList(List" + selectedList + ")");
    }

    List newSelectedMemberList = new ArrayList();

    for (Iterator i = selectedList.iterator(); i.hasNext();)
    {
      String selectedItem = (String) i.next();
      MessageForumsUser user = new MessageForumsUserImpl();
      /** lookup item in map */
      MembershipItem item = (MembershipItem) getAllCourseMembers().get(
          selectedItem);
      if (item == null)
      {
        LOG.warn("decodeActorPermissionTypeList() could not resolve uuid: "
            + selectedItem);
      }
      else
      {
        if (MembershipItem.TYPE_ALL_PARTICIPANTS.equals(item.getType()))
        {
          user.setTypeUuid(typeManager.getAllParticipantType());
          user.setUserId(typeManager.getAllParticipantType());
          newSelectedMemberList.add(user);
        }
        else
          if (MembershipItem.TYPE_NOT_SPECIFIED.equals(item.getType()))
          {
            user.setTypeUuid(typeManager.getNotSpecifiedType());
            user.setUserId(typeManager.getNotSpecifiedType());
            // if not specified is seleted then only this value remains.
            newSelectedMemberList = null;
            newSelectedMemberList = new ArrayList();
            newSelectedMemberList.add(user);
            break;
          }
          else
            if (MembershipItem.TYPE_ROLE.equals(item.getType()))
            {
              user.setTypeUuid(typeManager.getRoleType());
              user.setUserId(item.getRole().getId());
              newSelectedMemberList.add(user);

            }
            else
              if (MembershipItem.TYPE_GROUP.equals(item.getType()))
              {
                user.setTypeUuid(typeManager.getGroupType());
                user.setUserId(item.getGroup().getId());
                newSelectedMemberList.add(user);
              }
              else
                if (MembershipItem.TYPE_USER.equals(item.getType()))
                {
                  user.setTypeUuid(typeManager.getUserType());
                  user.setUserId(item.getUser().getId());
                  newSelectedMemberList.add(user);
                }
                else
                {
                  LOG
                      .warn("getRecipients() could not resolve membership type: "
                          + item.getType());
                }
      }
    }
    return newSelectedMemberList;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#decodeAccessorsList(java.util.List)
   */
  public List decodeAccessorsList(ArrayList accessorList)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("decodeAccessorsList(List" + accessorList + ")");
    }
    if (accessorList == null || accessorList.size() < 1)
    {
      return forumManager.createDefaultActorPermissions().getAccessors();
    }
    return decodeActorPermissionTypeList(accessorList);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#decodeContributorsList(java.util.List)
   */
  public List decodeContributorsList(ArrayList contributorList)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("decodeContributorsList(List" + contributorList + ")");
    }
    if (contributorList == null || contributorList.size() < 1)
    {
      return forumManager.createDefaultActorPermissions().getContributors();
    }
    return decodeActorPermissionTypeList(contributorList);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getContributorsList(org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public List getContributorsList(DiscussionForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug(" getContributorsList(DiscussionForum" + forum + ")");
    }
    List contributorList = null;
    if (forum == null)
    {
      return null;
    }
    if (forum.getActorPermissions() == null
        || forum.getActorPermissions().getContributors() == null)
    {
      forum.setActorPermissions(forumManager.createDefaultActorPermissions());
      contributorList = forumManager.createDefaultActorPermissions()
          .getContributors();
    }
    else
    {
      contributorList = forum.getActorPermissions().getContributors();
    }
    Iterator iterator = contributorList.iterator();

    return getContributorAccessorList(iterator);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getAccessorsList(org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public List getAccessorsList(DiscussionForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getAccessorsList(DiscussionForum" + forum + ")");
    }
    List accessorsList = null;
    if (forum == null)
    {
      return null;
    }
    if (forum.getActorPermissions() == null
        || forum.getActorPermissions().getAccessors() == null)
    {
      forum.setActorPermissions(forumManager.createDefaultActorPermissions());
      accessorsList = forumManager.createDefaultActorPermissions()
          .getAccessors();
    }
    else
    {
      accessorsList = forum.getActorPermissions().getAccessors();
    }

    Iterator iterator = accessorsList.iterator();

    return getContributorAccessorList(iterator);
  }

  /**
   * @param iterator
   * @return
   */
  private List getContributorAccessorList(Iterator iterator)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getContributorAccessorList(Iterator" + iterator + ")");
    }
    List modifiedContributorList = new ArrayList();
    while (iterator.hasNext())
    {
      String selectedId = null;
      MessageForumsUser user = (MessageForumsUser) iterator.next();
      List totalmembers = membershipManager
          .convertMemberMapToList(courseMemberMap);
      Iterator iter = totalmembers.iterator();

      if (user.getTypeUuid().equals(typeManager.getAllParticipantType()))
      {
        while (iter.hasNext())
        {
          MembershipItem member = (MembershipItem) iter.next();
          if (member.getType().equals(MembershipItem.TYPE_ALL_PARTICIPANTS))
          {
            selectedId = member.getId();
          }
        }
      }
      if (user.getTypeUuid().equals(typeManager.getNotSpecifiedType()))
      {
        while (iter.hasNext())
        {
          MembershipItem member = (MembershipItem) iter.next();
          if (member.getType().equals(MembershipItem.TYPE_NOT_SPECIFIED))
          {
            selectedId = member.getId();
          }
        }
      }

      if (user.getTypeUuid().equals(typeManager.getGroupType()))
      {
        while (iter.hasNext())
        {
          MembershipItem member = (MembershipItem) iter.next();
          if (member.getType().equals(MembershipItem.TYPE_GROUP)
              && user.getUserId().equals(member.getGroup().getId()))
          {
            selectedId = member.getId();
          }
        }

      }
      if (user.getTypeUuid().equals(typeManager.getRoleType()))
      {
        while (iter.hasNext())
        {
          MembershipItem member = (MembershipItem) iter.next();
          if (member.getType().equals(MembershipItem.TYPE_ROLE)
              && user.getUserId().equals(member.getRole().getId()))
          {
            selectedId = member.getId();
          }
        }
      }
      if (user.getTypeUuid().equals(typeManager.getUserType()))
      {
        while (iter.hasNext())
        {
          MembershipItem member = (MembershipItem) iter.next();
          if (member.getType().equals(MembershipItem.TYPE_USER)
              && user.getUserId().equals(member.getUser().getId()))
          {
            selectedId = member.getId();
          }
        }

      }

      modifiedContributorList.add(selectedId);
    }
    return modifiedContributorList;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getAllCourseMembers()
   */
  public Map getAllCourseMembers()
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getAllCourseMembers()");
    }
    if (courseMemberMap == null)
    {
      courseMemberMap = membershipManager.getAllCourseMembers(true, false, true);
    }
    return courseMemberMap;
  }

  /**
   * @param courseMemberMap
   *          The courseMemberMap to set.
   */
  public void setCourseMemberMapToNull()
  {
    this.courseMemberMap = null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getContributorsList(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public List getContributorsList(DiscussionTopic topic, DiscussionForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getContributorsList(DiscussionTopic " + topic
          + ", DiscussionForum " + forum + ")");
    }
    List contributorList = null;
    if (topic == null)
    {
      return null;
    }
    if (topic.getActorPermissions() == null
        || topic.getActorPermissions().getContributors() == null)
    {
      // hibernate does not permit this b/c saving forum and topics will
      // throw uniqueobjectexception
      topic.setActorPermissions(getDeepCopyOfParentActorPermissions(forum
          .getActorPermissions()));
      contributorList = topic.getActorPermissions().getContributors();
    }
    else
    {
      contributorList = topic.getActorPermissions().getContributors();
    }
    Iterator iterator = contributorList.iterator();

    return getContributorAccessorList(iterator);
  }

  private ActorPermissions getDeepCopyOfParentActorPermissions(
      ActorPermissions actorPermissions)
  {
    ActorPermissions newAP = new ActorPermissionsImpl();
    List parentAccessors = actorPermissions.getAccessors();
    List parentContributors = actorPermissions.getContributors();
    List newAccessors = new ArrayList();
    List newContributor = new ArrayList();
    Iterator iter = parentAccessors.iterator();
    while (iter.hasNext())
    {
      MessageForumsUser accessParent = (MessageForumsUser) iter.next();
      MessageForumsUser newaccessor = new MessageForumsUserImpl();
      newaccessor.setTypeUuid(accessParent.getTypeUuid());
      newaccessor.setUserId(accessParent.getUserId());
      newaccessor.setUuid(accessParent.getUuid());
      newAccessors.add(newaccessor);
    }
    Iterator iter1 = parentContributors.iterator();
    while (iter1.hasNext())
    {
      MessageForumsUser contribParent = (MessageForumsUser) iter1.next();
      MessageForumsUser newcontributor = new MessageForumsUserImpl();
      newcontributor.setTypeUuid(contribParent.getTypeUuid());
      newcontributor.setUserId(contribParent.getUserId());
      newcontributor.setUuid(contribParent.getUuid());
      newContributor.add(newcontributor);
    }
    newAP.setAccessors(newAccessors);
    newAP.setContributors(newContributor);
    return newAP;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getAccessorsList(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public List getAccessorsList(DiscussionTopic topic, DiscussionForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getAccessorsList(DiscussionTopic " + topic
          + ", DiscussionForum " + forum + ")");
    }
    List accessorsList = null;
    if (topic == null)
    {
      return null;
    }
    if (topic.getActorPermissions() == null
        || topic.getActorPermissions().getAccessors() == null)
    {
      // hibernate does not permit this b/c saving forum and topics will
      // throw uniqueobjectexception
      topic.setActorPermissions(getDeepCopyOfParentActorPermissions(forum
          .getActorPermissions()));
      accessorsList = topic.getActorPermissions().getAccessors();
    }
    else
    {
      accessorsList = topic.getActorPermissions().getAccessors();
    }

    Iterator iterator = accessorsList.iterator();

    return getContributorAccessorList(iterator);
  }

  public DBMembershipItem getAreaDBMember(Set originalSet, String name,
      Integer type)
  {
    DBMembershipItem newItem = getDBMember(originalSet, name, type);
    return newItem;
  }

  public DBMembershipItem getDBMember(Set originalSet, String name,
      Integer type)
  {
      	
    DBMembershipItem membershipItem = null;
    DBMembershipItem membershipItemIter;
    
    if (originalSet != null){
      Iterator iter = originalSet.iterator();
      while (iter.hasNext())
      {
      	membershipItemIter = (DBMembershipItem) iter.next();
        if (membershipItemIter.getType().equals(type)
            && membershipItemIter.getName().equals(name))
        {
        	membershipItem = membershipItemIter;
          break;
        }
      }
    }
    
    if (membershipItem == null || membershipItem.getPermissionLevel() == null){    	
    	PermissionLevel level = null;
    	//for groups awareness
    	if (type.equals(DBMembershipItem.TYPE_ROLE) || type.equals(DBMembershipItem.TYPE_GROUP))
      { 
    		
    		String levelName = null;
    		
    		if (membershipItem != null){
    			/** use level from stored item */
    			levelName = membershipItem.getPermissionLevelName();
    		}
    		else{    	
    			/** get level from config file */
    			levelName = ServerConfigurationService.getString(MC_DEFAULT
              + name);
    			    			
    			
    		}
      	        	
        if (levelName != null && levelName.trim().length() > 0)
        {
          level = permissionLevelManager.getPermissionLevelByName(levelName);
        } 
        else{
        	Collection siteIds = new Vector();
        	siteIds.add(getContextSiteId());        	
        	if (AuthzGroupService.getAllowedFunctions(name, siteIds)
        			.contains(SiteService.SECURE_UPDATE_SITE)){        			        	        	
        		level = permissionLevelManager.getDefaultOwnerPermissionLevel();
        	}
        	else if(type.equals(DBMembershipItem.TYPE_GROUP))
        	{
        	  level = permissionLevelManager.getDefaultNonePermissionLevel();
        	}
        	else{
        		level = permissionLevelManager.getDefaultContributorPermissionLevel();
        	}
        	
        }
      }
    	PermissionLevel noneLevel = permissionLevelManager.getDefaultNonePermissionLevel();
      membershipItem = new DBMembershipItemImpl();
      membershipItem.setName(name);
      membershipItem.setPermissionLevelName((level == null) ? noneLevel.getName() : level.getName() );
      membershipItem.setType(type);
      membershipItem.setPermissionLevel((level == null) ? noneLevel : level);      
    }        
    return membershipItem;
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
      attach.setAttachmentSize((new Integer(cr.getContentLength())).toString());
      attach.setCreatedBy(cr.getProperties().getProperty(
          cr.getProperties().getNamePropCreator()));
      attach.setModifiedBy(cr.getProperties().getProperty(
          cr.getProperties().getNamePropModifiedBy()));
      attach.setAttachmentType(cr.getContentType());
      String tempString = cr.getUrl();
      String newString = new String();
      char[] oneChar = new char[1];
      for (int i = 0; i < tempString.length(); i++)
      {
        if (tempString.charAt(i) != ' ')
        {
          oneChar[0] = tempString.charAt(i);
          String concatString = new String(oneChar);
          newString = newString.concat(concatString);
        }
        else
        {
          newString = newString.concat("%20");
        }
      }
      //tempString.replaceAll(" ", "%20");
      //attach.setAttachmentUrl(newString);
      attach.setAttachmentUrl("/url");

      return attach;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

	public List getDiscussionForumsWithTopics()
	{
    LOG.debug("getDiscussionForumsWithTopics()");
    return forumManager.getForumByTypeAndContextWithTopicsAllAttachments(typeManager
        .getDiscussionForumType());
	}
	
	public List getDiscussionForumsWithTopics(String contextId) {
	    if (LOG.isDebugEnabled()) LOG.debug("getDiscussionForumsWithTopics(String contextId)");
	    if (contextId == null) {
	        throw new IllegalArgumentException("Null contextId passed to getDiscussionForumsWithTopics");
	    }
	    String dfType = typeManager.getDiscussionForumType();
	    return forumManager.getForumByTypeAndContextWithTopicsAllAttachments(dfType, contextId);
	}

	public Map getReadStatusForMessagesWithId(List msgIds, String userId)
	{
		LOG.debug("getDiscussionForumsWithTopics()");
		if (userId == null) {
			LOG.debug("empty map returns b/c no userId passed to getReadStatusForMessagesWithId");
			return new HashMap(); 
		}
		
		Map msgIdStatusMap = new HashMap();
		if (msgIds == null || msgIds.size() == 0) {
			LOG.debug("empty map returns b/c no msgIds passed to getReadStatusForMessagesWithId");
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
				Map statusMap = messageManager.getReadStatusForMessagesWithId(tempMsgIdList, userId);
				msgIdStatusMap.putAll(statusMap);
				begIndex = endIndex;
			}
		}
		
		return msgIdStatusMap;
	}

	public List getDiscussionForumsWithTopicsMembershipNoAttachments(String contextId)
	{
    LOG.debug("getDiscussionForumsWithTopicsMembershipNoAttachments()");
    return forumManager.getForumByTypeAndContextWithTopicsMembership(typeManager
        .getDiscussionForumType(), contextId);
	}
	
	public List getPendingMsgsInTopic(Long topicId)
	{
		return messageManager.getPendingMsgsInTopic(topicId);
	}
	
	public int getNumModTopicsWithModPermission(List membershipList)
	{
		return forumManager.getNumModTopicCurrentUserHasModPermFor(membershipList);
	}

    private String getEventMessage(Object object) {
    	String eventMessagePrefix = "";
    	final String toolId = ToolManager.getCurrentTool().getId();
    	
    		if (toolId.equals(DiscussionForumService.MESSAGE_CENTER_ID))
    			eventMessagePrefix = "/messagesAndForums";
    		else if (toolId.equals(DiscussionForumService.MESSAGES_TOOL_ID))
    			eventMessagePrefix = "/messages";
    		else
    			eventMessagePrefix = "/forums";
    	
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
    
}
