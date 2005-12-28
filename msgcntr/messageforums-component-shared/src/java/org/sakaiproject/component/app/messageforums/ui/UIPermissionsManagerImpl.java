/**
 * 
 */
package org.sakaiproject.component.app.messageforums.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.AreaControlPermission;
import org.sakaiproject.api.app.messageforums.DefaultPermissionsManager;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.ForumControlPermission;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.MessagePermissions;
import org.sakaiproject.api.app.messageforums.PermissionManager;
import org.sakaiproject.api.app.messageforums.TopicControlPermission;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.api.kernel.session.SessionManager;
import org.sakaiproject.api.kernel.tool.Placement;
import org.sakaiproject.api.kernel.tool.ToolManager;
import org.sakaiproject.component.app.messageforums.TestUtil;
import org.sakaiproject.service.legacy.authzGroup.AuthzGroupService;
import org.sakaiproject.service.legacy.security.SecurityService;

/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 */
/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 */
public class UIPermissionsManagerImpl implements UIPermissionsManager
{
  private static final Log LOG = LogFactory
      .getLog(UIPermissionsManagerImpl.class);

  // dependencies
  private AuthzGroupService authzGroupService;
  private SessionManager sessionManager;
  private ToolManager toolManager;
  private PermissionManager permissionManager;
  private MessageForumsTypeManager typeManager;
  private SecurityService securityService;

  public void init()
  {
    ;
  }
  /**
   * @param authzGroupService
   *          The authzGroupService to set.
   */
  public void setAuthzGroupService(AuthzGroupService authzGroupService)
  {
    this.authzGroupService = authzGroupService;
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
   * @param toolManager
   *          The toolManager to set.
   */
  public void setToolManager(ToolManager toolManager)
  {
    this.toolManager = toolManager;
  }

  /**
   * @param permissionManager
   *          The permissionManager to set.
   */
  public void setPermissionManager(PermissionManager permissionManager)
  {
    this.permissionManager = permissionManager;
  }

  /**
   * @param typeManager
   *          The typeManager to set.
   */
  public void setTypeManager(MessageForumsTypeManager typeManager)
  {
    this.typeManager = typeManager;
  }

  /**
   * @param securityService
   *          The securityService to set.
   */
  public void setSecurityService(SecurityService securityService)
  {
    this.securityService = securityService;
  }

  // end dependencies
  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isNewForum()
   */
  public boolean isNewForum()
  {
    LOG.debug("isNewForum()");
    if (isSuperUser())
    {
      return true;
    }
    try
    { 
      return geAreaControlPermissions().getNewForum().booleanValue();
    }
    catch (Exception e)
    {
      LOG.warn(e.getMessage(), e);
      return false;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isChangeSettings(org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public boolean isChangeSettings(DiscussionForum forum)
  {

    if (LOG.isDebugEnabled())
    {
      LOG.debug("isChangeSettings(DiscussionForum " + forum + ")");
    }
    if (isSuperUser())
    {
      return true;
    }
    if (isForumOwner(forum))
    {
      return true;
    }
    try
    {
        return geAreaControlPermissions().getChangeSettings().booleanValue();
    }
    catch (Exception e)
    {
      LOG.warn(e.getMessage(), e);
      return false;
    }
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isNewTopic(org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public boolean isNewTopic(DiscussionForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isNewTopic(DiscussionForum " + forum + ")");
    }
    if (isSuperUser())
    {
      return true;
    }
    // TODO: should the forum owner be allowed to create a new topic
    // without checking permissions?
    // if (isForumOwner(forum))
    // {
    // return true;
    // }
    try
    {
      ForumControlPermission controlPermission = permissionManager
          .getForumControlPermissionForRole(forum, getCurrentUserRole(),
              typeManager.getDiscussionForumType());

      if (controlPermission == null || controlPermission.getNewTopic() == null
          || controlPermission.getNewTopic().equals(Boolean.FALSE))
      {
        if (LOG.isDebugEnabled())
        {
          LOG.debug("Role :" + getCurrentUserRole()
              + "is not allowed to create new topic for given forum " + forum);
        }
        return false;
      }
      if (forum.getLocked() == null || forum.getLocked().equals(Boolean.TRUE))
      {
        LOG.debug("This Forum is Locked");
        return false;
      }
      // //TODO: confirm: if a forum is in draft stage, you could still
      // create topics but if you are owner
      if (forum.getDraft() == null || forum.getDraft().equals(Boolean.TRUE))
      {
        LOG.debug("This forum is a draft");
        if (isForumOwner(forum))
        {
          return true;
        }
        return false;
      }
      if (controlPermission.getNewTopic().equals(Boolean.TRUE)
          && forum.getDraft().equals(Boolean.FALSE)
          && forum.getLocked().equals(Boolean.FALSE))
      {
        return true;
      }
    }
    catch (Exception e)
    {
      LOG.error(e.getMessage(), e);
      return false;
    }

    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isNewResponse(org.sakaiproject.api.app.messageforums.DiscussionTopic,
   *      org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public boolean isNewResponse(DiscussionTopic topic, DiscussionForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isNewResponse(DiscussionTopic " + topic + ")");
    }

    try
    {
      if (checkBaseConditions(topic, forum))
      {
        return true;
      }
      TopicControlPermission controlPermission = permissionManager
          .getTopicControlPermissionForRole(topic, getCurrentUserRole(),
              typeManager.getDiscussionForumType());

      if (controlPermission == null
          || controlPermission.getNewResponse() == null
          || controlPermission.getNewResponse().equals(Boolean.FALSE))
      {
        if (LOG.isDebugEnabled())
        {
          LOG.debug("Role :" + getCurrentUserRole()
              + "is not allowed to create new response for given topic "
              + topic);
        }
        return false;
      }

      if (controlPermission.getNewResponse().equals(Boolean.TRUE)
          && forum.getDraft().equals(Boolean.FALSE)
          && forum.getLocked().equals(Boolean.FALSE)
          && topic.getDraft().equals(Boolean.FALSE)
          && topic.getLocked().equals(Boolean.FALSE))
      {
        return true;
      }
    }
    catch (Exception e)
    {
      LOG.error(e.getMessage(), e);
      return false;
    }

    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isNewResponseToResponse(org.sakaiproject.api.app.messageforums.DiscussionTopic,
   *      org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public boolean isNewResponseToResponse(DiscussionTopic topic,
      DiscussionForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isNewResponseToResponse(DiscussionTopic " + topic + ")");
    }

    try
    {
      if (checkBaseConditions(topic, forum))
      {
        return true;
      }
      TopicControlPermission controlPermission = permissionManager
          .getTopicControlPermissionForRole(topic, getCurrentUserRole(),
              typeManager.getDiscussionForumType());

      if (controlPermission == null
          || controlPermission.getResponseToResponse() == null
          || controlPermission.getResponseToResponse().equals(Boolean.FALSE))
      {
        if (LOG.isDebugEnabled())
        {
          LOG
              .debug("Role :"
                  + getCurrentUserRole()
                  + "is not allowed to create response to response for given topic "
                  + topic);
        }
        return false;
      }

      if (controlPermission.getResponseToResponse().equals(Boolean.TRUE)
          && forum.getDraft().equals(Boolean.FALSE)
           && forum.getLocked().equals(Boolean.FALSE)
          && topic.getDraft().equals(Boolean.FALSE)
          && topic.getLocked().equals(Boolean.FALSE)
          )
      {
        return true;
      }
    }
    catch (Exception e)
    {
      LOG.error(e.getMessage(), e);
      return false;
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isMovePostings(org.sakaiproject.api.app.messageforums.DiscussionTopic,
   *      org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public boolean isMovePostings(DiscussionTopic topic, DiscussionForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isMovePostings(DiscussionTopic " + topic + ")");
    }

    try
    {
      if (checkBaseConditions(topic, forum))
      {
        return true;
      }
      TopicControlPermission controlPermission = permissionManager
          .getTopicControlPermissionForRole(topic, getCurrentUserRole(),
              typeManager.getDiscussionForumType());

      if (controlPermission == null
          || controlPermission.getMovePostings() == null
          || controlPermission.getMovePostings().equals(Boolean.FALSE))
      {
        if (LOG.isDebugEnabled())
        {
          LOG.debug("Role :" + getCurrentUserRole()
              + "is not allowed to move postings for given topic " + topic);
        }
        return false;
      }
      if (controlPermission.getMovePostings().equals(Boolean.TRUE)
          && forum.getDraft().equals(Boolean.FALSE)
          && forum.getLocked().equals(Boolean.FALSE)
          && topic.getDraft().equals(Boolean.FALSE)
          && topic.getLocked().equals(Boolean.FALSE))
      {
        return true;
      }
    }
    catch (Exception e)
    {
      LOG.error(e.getMessage(), e);
      return false;
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isChangeSettings(org.sakaiproject.api.app.messageforums.DiscussionTopic,
   *      org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public boolean isChangeSettings(DiscussionTopic topic, DiscussionForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isChangeSettings(DiscussionTopic " + topic + ")");
    }
    if (isSuperUser())
    {
      return true;
    }
    try
    {
      if (forum.getLocked() == null || forum.getLocked().equals(Boolean.TRUE))
      {
        LOG.debug("This Forum is Locked");
        return false;
      }
      if (isTopicOwner(topic))
      {
        return true;
      }
      TopicControlPermission controlPermission = permissionManager
          .getTopicControlPermissionForRole(topic, getCurrentUserRole(),
              typeManager.getDiscussionForumType());
      // if owner then allow change of settings on the topic or on forum.
      if (topic.getCreatedBy().equals(getCurrentUserId()))
      {
        return true;
      }
      if (controlPermission == null
          || controlPermission.getChangeSettings() == null
          || controlPermission.getChangeSettings().equals(Boolean.FALSE))
      {
        if (LOG.isDebugEnabled())
        {
          LOG.debug("Role :" + getCurrentUserRole()
              + "is not allowed to change settings for given topic " + topic);
        }
        return false;
      }
      if (controlPermission.getChangeSettings().equals(Boolean.TRUE))
      {
        return true;
      }
    }
    catch (Exception e)
    {
      LOG.error(e.getMessage(), e);
      return false;
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isPostToGradebook(org.sakaiproject.api.app.messageforums.DiscussionTopic,
   *      org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public boolean isPostToGradebook(DiscussionTopic topic, DiscussionForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isPostToGradebook(DiscussionTopic " + topic + ")");
    }

    try
    {
      if (checkBaseConditions(topic, forum))
      {
        return true;
      }
      TopicControlPermission controlPermission = permissionManager
          .getTopicControlPermissionForRole(topic, getCurrentUserRole(),
              typeManager.getDiscussionForumType());

      if (controlPermission == null
          || controlPermission.getPostToGradebook() == null
          || controlPermission.getPostToGradebook().equals(Boolean.FALSE))
      {
        if (LOG.isDebugEnabled())
        {
          LOG.debug("Role :" + getCurrentUserRole()
              + "is not allowed to post to gradebook for given topic " + topic);
        }
        return false;
      }
      if (controlPermission.getPostToGradebook().equals(Boolean.TRUE)
          && forum.getDraft().equals(Boolean.FALSE)
          && forum.getLocked().equals(Boolean.FALSE)
          && topic.getDraft().equals(Boolean.FALSE)
          && topic.getLocked().equals(Boolean.FALSE))
      {
        return true;
      }
    }
    catch (Exception e)
    {
      LOG.error(e.getMessage(), e);
      return false;
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isRead(org.sakaiproject.api.app.messageforums.DiscussionTopic,
   *      org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public boolean isRead(DiscussionTopic topic, DiscussionForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isRead(DiscussionTopic " + topic + ")");
    }
    if (checkBaseConditions(topic, forum))
    {
      return true;
    }
    MessagePermissions messagePermission = permissionManager
        .getTopicMessagePermissionForRole(topic, getCurrentUserRole(),
            typeManager.getDiscussionForumType());

    if (messagePermission == null || messagePermission.getRead() == null
        || messagePermission.getRead().equals(Boolean.FALSE))
    {
      if (LOG.isDebugEnabled())
      {
        LOG.debug("Role :" + getCurrentUserRole()
            + "is not allowed to read messages for given topic " + topic);
      }
      return false;
    }
    if (messagePermission.getRead().equals(Boolean.TRUE)
        && forum.getDraft().equals(Boolean.FALSE)
        && topic.getDraft().equals(Boolean.FALSE))
    {
      return true;
    }

    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isReviseAny(org.sakaiproject.api.app.messageforums.DiscussionTopic,
   *      org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public boolean isReviseAny(DiscussionTopic topic, DiscussionForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isReviseAny(DiscussionTopic " + topic + ")");
    }
    if (checkBaseConditions(topic, forum))
    {
      return true;
    }
    MessagePermissions messagePermission = permissionManager
        .getTopicMessagePermissionForRole(topic, getCurrentUserRole(),
            typeManager.getDiscussionForumType());

    if (messagePermission == null || messagePermission.getReviseAny() == null
        || messagePermission.getReviseAny().equals(Boolean.FALSE))
    {
      if (LOG.isDebugEnabled())
      {
        LOG.debug("Role :" + getCurrentUserRole()
            + "is not allowed to revise any messages for given topic " + topic);
      }
      return false;
    }
    if (topic.getLocked() == null || topic.getLocked() .equals(Boolean.TRUE))
    {
      LOG.debug("This topic is locked " + topic);
      return false;
    }
    if (topic.getDraft() == null || topic.getDraft().equals(Boolean.TRUE))
    {
      LOG.debug("This topic is at draft stage " + topic);
    }
    if (messagePermission.getReviseAny().equals(Boolean.TRUE)
        && forum.getDraft().equals(Boolean.FALSE)
        && forum.getLocked().equals(Boolean.FALSE)
        && topic.getDraft().equals(Boolean.FALSE)
        && topic.getLocked().equals(Boolean.FALSE))
    {
      return true;
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isReviseOwn(org.sakaiproject.api.app.messageforums.DiscussionTopic,
   *      org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public boolean isReviseOwn(DiscussionTopic topic, DiscussionForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isReviseOwn(DiscussionTopic " + topic + ")");
    }
    if (checkBaseConditions(topic, forum))
    {
      return true;
    }
    MessagePermissions messagePermission = permissionManager
        .getTopicMessagePermissionForRole(topic, getCurrentUserRole(),
            typeManager.getDiscussionForumType());

    if (messagePermission == null || messagePermission.getReviseOwn() == null
        || messagePermission.getReviseOwn().equals(Boolean.FALSE))
    {
      if (LOG.isDebugEnabled())
      {
        LOG.debug("Role :" + getCurrentUserRole()
            + "is not allowed to revise own messages for given topic " + topic);
      }
      return false;
    }
    if (topic.getLocked() == null || topic.getLocked().equals(Boolean.TRUE))
    {
      LOG.debug("This topic is locked " + topic);
      return false;
    }
    if (topic.getDraft() == null || topic.getDraft().equals(Boolean.TRUE))
    {
      LOG.debug("This topic is at draft stage " + topic);
    }
    if (messagePermission.getReviseOwn().equals(Boolean.TRUE)
        && forum.getDraft().equals(Boolean.FALSE)
        && forum.getLocked().equals(Boolean.FALSE)
        && topic.getDraft().equals(Boolean.FALSE)
        && topic.getLocked().equals(Boolean.FALSE))
    {
      return true;
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isDeleteAny(org.sakaiproject.api.app.messageforums.DiscussionTopic,
   *      org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public boolean isDeleteAny(DiscussionTopic topic, DiscussionForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isDeleteAny(DiscussionTopic " + topic + ")");
    }
    if (checkBaseConditions(topic, forum))
    {
      return true;
    }
    MessagePermissions messagePermission = permissionManager
        .getTopicMessagePermissionForRole(topic, getCurrentUserRole(),
            typeManager.getDiscussionForumType());

    if (messagePermission == null || messagePermission.getDeleteAny() == null
        || messagePermission.getDeleteAny() == Boolean.FALSE)
    {
      if (LOG.isDebugEnabled())
      {
        LOG.debug("Role :" + getCurrentUserRole()
            + "is not allowed to delete any messages for given topic " + topic);
      }
      return false;
    }
    if (topic.getLocked() == null || topic.getLocked() == Boolean.TRUE)
    {
      LOG.debug("This topic is locked " + topic);
      return false;
    }
    if (topic.getDraft() == null || topic.getDraft() == Boolean.TRUE)
    {
      LOG.debug("This topic is at draft stage " + topic);
    }
    if (messagePermission.getDeleteAny() == Boolean.TRUE
        && forum.getDraft().equals(Boolean.FALSE)
        && forum.getLocked().equals(Boolean.FALSE)
        && topic.getDraft() == Boolean.FALSE
        && topic.getLocked() == Boolean.FALSE)
    {
      return true;
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isDeleteOwn(org.sakaiproject.api.app.messageforums.DiscussionTopic,
   *      org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public boolean isDeleteOwn(DiscussionTopic topic, DiscussionForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isDeleteOwn(DiscussionTopic " + topic + ")");
    }
    if (checkBaseConditions(topic, forum))
    {
      return true;
    }
    MessagePermissions messagePermission = permissionManager
        .getTopicMessagePermissionForRole(topic, getCurrentUserRole(),
            typeManager.getDiscussionForumType());

    if (messagePermission == null || messagePermission.getDeleteOwn() == null
        || messagePermission.getDeleteOwn() == Boolean.FALSE)
    {
      if (LOG.isDebugEnabled())
      {
        LOG
            .debug("Role :" + getCurrentUserRole()
                + "is not allowed to delete own  messages for given topic "
                + topic);
      }
      return false;
    }
    if (topic.getLocked() == null || topic.getLocked() == Boolean.TRUE)
    {
      LOG.debug("This topic is locked " + topic);
      return false;
    }
    if (topic.getDraft() == null || topic.getDraft() == Boolean.TRUE)
    {
      LOG.debug("This topic is at draft stage " + topic);
    }
    if (messagePermission.getDeleteOwn() == Boolean.TRUE
        && forum.getDraft().equals(Boolean.FALSE)
        && forum.getLocked().equals(Boolean.FALSE)
        && topic.getDraft() == Boolean.FALSE
        && topic.getLocked() == Boolean.FALSE)
    {
      return true;
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isMarkAsRead(org.sakaiproject.api.app.messageforums.DiscussionTopic,
   *      org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public boolean isMarkAsRead(DiscussionTopic topic, DiscussionForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isMarkAsRead(DiscussionTopic " + topic + ")");
    }
    if (checkBaseConditions(topic, forum))
    {
      return true;
    }
    MessagePermissions messagePermission = permissionManager
        .getTopicMessagePermissionForRole(topic, getCurrentUserRole(),
            typeManager.getDiscussionForumType());

    if (messagePermission == null || messagePermission.getMarkAsRead() == null
        || messagePermission.getMarkAsRead() == Boolean.FALSE)
    {
      if (LOG.isDebugEnabled())
      {
        LOG.debug("Role :" + getCurrentUserRole()
            + "is not allowed to mark messages as read for given topic "
            + topic);
      }
      return false;
    }
    if (topic.getLocked() == null || topic.getLocked() == Boolean.TRUE)
    {
      LOG.debug("This topic is locked " + topic);
      return false;
    }
    if (topic.getDraft() == null || topic.getDraft() == Boolean.TRUE)
    {
      LOG.debug("This topic is at draft stage " + topic);
    }
    if (messagePermission.getMarkAsRead() == Boolean.TRUE
        && forum.getDraft().equals(Boolean.FALSE)
        && forum.getLocked().equals(Boolean.FALSE)
        && topic.getDraft() == Boolean.FALSE
        && topic.getLocked() == Boolean.FALSE)
    {
      return true;
    }
    return false;
  }

  /**
   * @return
   */
  private String getCurrentUserId()
  {
    if (TestUtil.isRunningTests())
    {
      return "test-user";
    }

    return sessionManager.getCurrentSessionUserId();
  }

  /**
   * @return
   */
  private String getCurrentUserRole()
  {
    return authzGroupService.getUserRole(getCurrentUserId(), "/site/"
        + getContextId());
  }

  /**
   * @return
   */
  private String getContextId()
  {
    if (TestUtil.isRunningTests())
    {
      return "test-context";
    }
    Placement placement = toolManager.getCurrentPlacement();
    String presentSiteId = placement.getContext();
    return presentSiteId;
  }

  /**
   * @param forum
   * @return
   */
  private boolean isForumOwner(DiscussionForum forum)
  {
    if (forum.getCreatedBy().equals(getCurrentUserId()))
    {
      return true;
    }
    return false;
  }

  /**
   * @param topic
   * @return
   */
  private boolean isTopicOwner(DiscussionTopic topic)
  {
    if (topic.getCreatedBy().equals(getCurrentUserId()))
    {
      return true;
    }
    return false;
  }

  /**
   * @return
   */
  private boolean isSuperUser()
  {
    return securityService.isSuperUser();
  }

  /**
   * @return
   */
  private AreaControlPermission geAreaControlPermissions()
  {
    return permissionManager
    .getAreaControlPermissionForRole(getCurrentUserRole(),
        typeManager.getDiscussionForumType());    
  }
  
  /**
   * @param topic
   * @param forum
   * @return
   */
  private boolean checkBaseConditions(DiscussionTopic topic,
      DiscussionForum forum)
  {
    if (isSuperUser())
    {
      return true;
    }
    if (forum.getLocked() == null || forum.getLocked() == Boolean.TRUE)
    {
      LOG.debug("This Forum is Locked");
      return false;
    }
    if (forum.getDraft() == null || forum.getDraft() == Boolean.TRUE)
    {
      LOG.debug("This forum is a draft");
      return false;
    }
    if (topic.getLocked() == null || topic.getLocked() == Boolean.TRUE)
    {
      LOG.debug("This topic is locked " + topic);
      return false;
    }
    if (topic.getDraft() == null || topic.getDraft() == Boolean.TRUE)
    {
      LOG.debug("This topic is at draft stage " + topic);
      return false;
    }
    return false;
  }
}
