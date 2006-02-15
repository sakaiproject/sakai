/**
 * 
 */
package org.sakaiproject.component.app.messageforums.ui;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.AreaControlPermission;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.ForumControlPermission;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.MessageForumsUser;
import org.sakaiproject.api.app.messageforums.MessagePermissions;
import org.sakaiproject.api.app.messageforums.PermissionLevelManager;
import org.sakaiproject.api.app.messageforums.PermissionManager;
import org.sakaiproject.api.app.messageforums.TopicControlPermission;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.api.kernel.session.SessionManager;
import org.sakaiproject.api.kernel.tool.Placement;
import org.sakaiproject.api.kernel.tool.ToolManager;
import org.sakaiproject.component.app.messageforums.TestUtil;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.service.framework.portal.cover.PortalService;
import org.sakaiproject.service.legacy.authzGroup.AuthzGroupService;
import org.sakaiproject.service.legacy.authzGroup.Member;
import org.sakaiproject.service.legacy.security.SecurityService;
import org.sakaiproject.service.legacy.site.Group;
import org.sakaiproject.service.legacy.site.cover.SiteService;
import org.sakaiproject.service.legacy.user.User;
import org.sakaiproject.service.legacy.user.cover.UserDirectoryService;

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
  private PermissionLevelManager permissionLevelManager;
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
    if (LOG.isDebugEnabled())
    {
      LOG.debug("setAuthzGroupService(AuthzGroupService " + authzGroupService
          + ")");
    }
    this.authzGroupService = authzGroupService;
  }

  /**
   * @param sessionManager
   *          The sessionManager to set.
   */
  public void setSessionManager(SessionManager sessionManager)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("setSessionManager(SessionManager " + sessionManager + ")");
    }
    this.sessionManager = sessionManager;
  }

  /**
   * @param toolManager
   *          The toolManager to set.
   */
  public void setToolManager(ToolManager toolManager)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("setToolManager(ToolManager " + toolManager + ")");
    }
    this.toolManager = toolManager;
  }

  /**
   * @param permissionManager
   *          The permissionManager to set.
   */
  public void setPermissionManager(PermissionManager permissionManager)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("setPermissionManager(PermissionManager " + permissionManager
          + ")");
    }
    this.permissionManager = permissionManager;
  }

  /**
   * @param typeManager
   *          The typeManager to set.
   */
  public void setTypeManager(MessageForumsTypeManager typeManager)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("setTypeManager(MessageForumsTypeManager " + typeManager + ")");
    }
    this.typeManager = typeManager;
  }

  /**
   * @param securityService
   *          The securityService to set.
   */
  public void setSecurityService(SecurityService securityService)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("setSecurityService(SecurityService" + securityService + ")");
    }
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
      return getAreaControlPermissions().getNewForum().booleanValue();
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
      return getAreaControlPermissions().getChangeSettings().booleanValue();
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
      else
      {
        return true;
      }
    }
    catch (Exception e)
    {
      LOG.error(e.getMessage(), e);
      return false;
    }

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
      LOG.debug("isNewResponse(DiscussionTopic " + topic + "), DiscussionForum"
          + forum + "");
    }

    try
    {
      if (checkBaseConditions(topic, forum))
      {
        return true;
      }
      if (isContributor(topic) != null)
      {
        return isContributor(topic).booleanValue();
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
      LOG.debug("isNewResponseToResponse(DiscussionTopic " + topic
          + " , DiscussionForum" + forum + ") ");
    }

    try
    {
      if (checkBaseConditions(topic, forum))
      {
        return true;
      }
      if (isContributor(topic) != null)
      {
        return isContributor(topic).booleanValue();
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
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isMovePostings(org.sakaiproject.api.app.messageforums.DiscussionTopic,
   *      org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public boolean isMovePostings(DiscussionTopic topic, DiscussionForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isMovePostings(DiscussionTopic " + topic
          + "), DiscussionForum" + forum + "");
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
      LOG.debug("isChangeSettings(DiscussionTopic " + topic
          + "), DiscussionForum" + forum + "");
    }
    if (isSuperUser())
    {
      return true;
    }
    try
    {
      // Change Settings on Topic allowed even if the forum is locked
      // if (forum.getLocked() == null || forum.getLocked().equals(Boolean.TRUE))
      // {
      // LOG.debug("This Forum is Locked");
      // return false;
      // }
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
      LOG.debug("isPostToGradebook(DiscussionTopic " + topic
          + ", DiscussionForum" + forum + ")");
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
      LOG.debug("isRead(DiscussionTopic " + topic + ", DiscussionForum" + forum
          + ")");
    }
    if (checkBaseConditions(topic, forum))
    {
      return true;
    }
    if (isContributor(topic) != null && isContributor(topic).booleanValue())
    {      
      return true;
    }
    if (isReadAccess(topic) != null)
    {
      return isReadAccess(topic).booleanValue();
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
      LOG.debug("isReviseAny(DiscussionTopic " + topic + ", DiscussionForum"
          + forum + ")");
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
    if (topic.getLocked() == null || topic.getLocked().equals(Boolean.TRUE))
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
      LOG.debug("isReviseOwn(DiscussionTopic " + topic + ", DiscussionForum"
          + forum + ")");
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
      LOG.debug("isDeleteAny(DiscussionTopic " + topic + ", DiscussionForum"
          + forum + ")");
    }
    if (checkBaseConditions(topic, forum))
    {
      return true;
    }
    MessagePermissions messagePermission = permissionManager
        .getTopicMessagePermissionForRole(topic, getCurrentUserRole(),
            typeManager.getDiscussionForumType());

    if (messagePermission == null || messagePermission.getDeleteAny() == null
        || messagePermission.getDeleteAny().equals(Boolean.FALSE))
    {
      if (LOG.isDebugEnabled())
      {
        LOG.debug("Role :" + getCurrentUserRole()
            + "is not allowed to delete any messages for given topic " + topic);
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
    if (messagePermission.getDeleteAny().equals(Boolean.TRUE)
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
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isDeleteOwn(org.sakaiproject.api.app.messageforums.DiscussionTopic,
   *      org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public boolean isDeleteOwn(DiscussionTopic topic, DiscussionForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isDeleteOwn(DiscussionTopic " + topic + ", DiscussionForum"
          + forum + ")");
    }
    if (checkBaseConditions(topic, forum))
    {
      return true;
    }
    MessagePermissions messagePermission = permissionManager
        .getTopicMessagePermissionForRole(topic, getCurrentUserRole(),
            typeManager.getDiscussionForumType());

    if (messagePermission == null || messagePermission.getDeleteOwn() == null
        || messagePermission.getDeleteOwn().equals(Boolean.FALSE))
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
    if (topic.getLocked() == null || topic.getLocked().equals(Boolean.TRUE))
    {
      LOG.debug("This topic is locked " + topic);
      return false;
    }
    if (topic.getDraft() == null || topic.getDraft().equals(Boolean.TRUE))
    {
      LOG.debug("This topic is at draft stage " + topic);
    }
    if (messagePermission.getDeleteOwn().equals(Boolean.TRUE)
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
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isMarkAsRead(org.sakaiproject.api.app.messageforums.DiscussionTopic,
   *      org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public boolean isMarkAsRead(DiscussionTopic topic, DiscussionForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isMarkAsRead(DiscussionTopic " + topic + ", DiscussionForum"
          + forum + ")");
    }
    if (checkBaseConditions(topic, forum))
    {
      return true;
    }
    MessagePermissions messagePermission = permissionManager
        .getTopicMessagePermissionForRole(topic, getCurrentUserRole(),
            typeManager.getDiscussionForumType());

    if (messagePermission == null || messagePermission.getMarkAsRead() == null
        || messagePermission.getMarkAsRead().equals(Boolean.FALSE))
    {
      if (LOG.isDebugEnabled())
      {
        LOG.debug("Role :" + getCurrentUserRole()
            + "is not allowed to mark messages as read for given topic "
            + topic);
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
    if (messagePermission.getMarkAsRead().equals(Boolean.TRUE)
        && forum.getDraft().equals(Boolean.FALSE)
        && forum.getLocked().equals(Boolean.FALSE)
        && topic.getDraft().equals(Boolean.FALSE)
        && topic.getLocked().equals(Boolean.FALSE))
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
    LOG.debug("getCurrentUserId()");
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
    LOG.debug("getCurrentUserRole()");
    return authzGroupService.getUserRole(getCurrentUserId(), "/site/"
        + getContextId());
  }

  /**
   * @return
   */
  private String getContextId()
  {
    LOG.debug("getContextId()");
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
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isForumOwner(DiscussionForum " + forum + ")");
    }
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
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isTopicOwner(DiscussionTopic " + topic + ")");
    }
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
    LOG.debug(" isSuperUser()");
    return securityService.isSuperUser();
  }

  /**
   * @return
   */
  private AreaControlPermission getAreaControlPermissions()
  {
    LOG.debug("getAreaControlPermissions()");
    return permissionManager.getAreaControlPermissionForRole(
        getCurrentUserRole(), typeManager.getDiscussionForumType());
  }

  /**
   * @param topic
   * @param forum
   * @return
   */
  private boolean checkBaseConditions(DiscussionTopic topic,
      DiscussionForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("checkBaseConditions(DiscussionTopic " + topic
          + ", DiscussionForum " + forum + ")");
    }
    if (isSuperUser())
    {
      return true;
    }
    if (forum.getLocked() == null || forum.getLocked().equals(Boolean.FALSE))
    {
      LOG.debug("This Forum is Locked");
      return false;
    }
    if (forum.getDraft() == null || forum.getDraft().equals(Boolean.FALSE))
    {
      LOG.debug("This forum is a draft");
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
      return false;
    }
    return false;
  }

  /**
   * @param userId
   * @param topic
   * @return
   */
  private Boolean isContributor(DiscussionTopic topic)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isContributor(DiscussionTopic " + topic + ")");
    }
    if (topic == null)
    {
      return null;
    }
    if (topic.getActorPermissions() == null
        || topic.getActorPermissions().getContributors() == null
        || topic.getActorPermissions().getContributors().size() < 1)
    {
      return null;
    }
    Iterator iter = topic.getActorPermissions().getContributors().iterator();
    while (iter.hasNext())
    {
      MessageForumsUser user = (MessageForumsUser) iter.next();
      if (user != null && user.getUuid() != null
          && user.getUuid().trim().length() > 0 && user.getTypeUuid() != null)
      {
        if (user.getTypeUuid().equals(typeManager.getNotSpecifiedType()))
        {
          return null;
        }
        if (user.getTypeUuid().equals(typeManager.getAllParticipantType()))
        {
          return Boolean.TRUE;
        }
        if (user.getTypeUuid().equals(typeManager.getGroupType())
            && isGroupMember(user.getUserId()))
        {
          return Boolean.TRUE;
        }
        if (user.getTypeUuid().equals(typeManager.getRoleType())
            && isRoleMember(user.getUserId()))
        {
          return Boolean.TRUE;
        }
        if (user.getTypeUuid().equals(typeManager.getUserType())
            && user.getUserId().equals(getCurrentUserId()))
        {
          return Boolean.TRUE;
        }
      }
    }
    return Boolean.FALSE;
  }

  private Boolean isReadAccess(DiscussionTopic topic)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug(" isReadAccess(DiscussionTopic  " + topic + ")");
    }
    if (topic == null)
    {
      return null;
    }
    if (topic.getActorPermissions() == null
        || topic.getActorPermissions().getAccessors() == null
        || topic.getActorPermissions().getAccessors().size() < 1)
    {
      return null;
    }
    Iterator iter = topic.getActorPermissions().getAccessors().iterator();
    while (iter.hasNext())
    {
      MessageForumsUser user = (MessageForumsUser) iter.next();
      if (user != null && user.getUuid() != null
          && user.getUuid().trim().length() > 0 && user.getTypeUuid() != null)
      {
        if (user.getTypeUuid().equals(typeManager.getNotSpecifiedType()))
        {
          return null;
        }
        if (user.getTypeUuid().equals(typeManager.getAllParticipantType()))
        {
          return Boolean.TRUE;
        }
        if (user.getTypeUuid().equals(typeManager.getGroupType())
            && isGroupMember(user.getUserId()))
        {
          return Boolean.TRUE;
        }
        if (user.getTypeUuid().equals(typeManager.getRoleType())
            && isRoleMember(user.getUserId()))
        {
          return Boolean.TRUE;
        }
        if (user.getTypeUuid().equals(typeManager.getUserType())
            && user.getUserId().equals(getCurrentUserId()))
        {
          return Boolean.TRUE;
        }
      }
    }
    return Boolean.FALSE;
  }

  private boolean isRoleMember(String roleId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isRoleMember(String " + roleId + ")");
    }
    if (getCurrentUserRole().equals(roleId))
    {
      return true;
    }
    return false;
  }

  private boolean isGroupMember(String groupId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("setAuthzGroupService(AuthzGroupService " + authzGroupService
          + ")");
    }
    try
    {
      Collection groups = SiteService.getSite(PortalService.getCurrentSiteId())
          .getGroups();
      for (Iterator groupIterator = groups.iterator(); groupIterator.hasNext();)
      {
        Group currentGroup = (Group) groupIterator.next();
        if (currentGroup.getId().equals(groupId))
        {
          Member member = currentGroup.getMember(getCurrentUserId());
          if (member!=null && member.getUserId().equals(getCurrentUserId()))
          {          
            return true;
           
          }
        }
      }
    }
    catch (IdUnusedException e)
    {
      LOG.debug("Group with id " + groupId + " not found");
      return false;
    }

    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#isInstructor()
   */
  public boolean isInstructor()
  {
    LOG.debug("isInstructor()");
    return isInstructor(UserDirectoryService.getCurrentUser());
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
      return securityService.unlock(user, "site.upd", getContextSiteId());
    else
      return false;
  }

  /**
   * @return siteId
   */
  private String getContextSiteId()
  {
    LOG.debug("getContextSiteId()");
    return ("/site/" + toolManager.getCurrentPlacement().getContext());
  }

	public void setPermissionLevelManager(
			PermissionLevelManager permissionLevelManager) {
		this.permissionLevelManager = permissionLevelManager;
	}
}
