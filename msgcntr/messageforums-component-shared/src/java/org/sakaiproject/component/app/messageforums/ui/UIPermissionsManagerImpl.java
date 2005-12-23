/**
 * 
 */
package org.sakaiproject.component.app.messageforums.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 */
public class UIPermissionsManagerImpl implements UIPermissionsManager
{
  private static final Log LOG = LogFactory
      .getLog(UIPermissionsManagerImpl.class);

  // dependencies
  private DefaultPermissionsManager defaultPermissionsManager;
  private AuthzGroupService authzGroupService;
  private SessionManager sessionManager;
  private ToolManager toolManager;
  private PermissionManager permissionManager;
  private MessageForumsTypeManager typeManager;

  /**
   * @param authzGroupService
   *          The authzGroupService to set.
   */
  public void setAuthzGroupService(AuthzGroupService authzGroupService)
  {
    this.authzGroupService = authzGroupService;
  }

  /**
   * @param defaultPermissionsManager
   *          The defaultPermissionsManager to set.
   */
  public void setDefaultPermissionsManager(
      DefaultPermissionsManager defaultPermissionsManager)
  {
    this.defaultPermissionsManager = defaultPermissionsManager;
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

  // end dependencies
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

    ForumControlPermission controlPermission = permissionManager
        .getForumControlPermissionForRole(forum, getCurrentUserRole(), typeManager
            .getDiscussionForumType());
    
    if(controlPermission!=null && controlPermission.getNewTopic()!=null)
    {
      return controlPermission.getNewTopic().booleanValue();      
    }
    return  false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isNewResponse(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public boolean isNewResponse(DiscussionTopic topic)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isNewResponse(DiscussionTopic " + topic + ")");
    }
    TopicControlPermission controlPermission = permissionManager
    .getTopicControlPermissionForRole(topic, getCurrentUserRole(), typeManager
        .getDiscussionForumType());
    
    if(controlPermission!=null && controlPermission.getNewResponse()!=null)
    {
      return controlPermission.getNewResponse().booleanValue();      
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isNewResponseToResponse(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public boolean isNewResponseToResponse(DiscussionTopic topic)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isNewResponseToResponse(DiscussionTopic " + topic + ")");
    }
    TopicControlPermission controlPermission = permissionManager
    .getTopicControlPermissionForRole(topic, getCurrentUserRole(), typeManager
        .getDiscussionForumType());
    
    if(controlPermission!=null && controlPermission.getResponseToResponse()!=null)
    {
      return controlPermission.getResponseToResponse().booleanValue();      
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isMovePostings(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public boolean isMovePostings(DiscussionTopic topic)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isMovePostings(DiscussionTopic " + topic + ")");
    }
    TopicControlPermission controlPermission = permissionManager
    .getTopicControlPermissionForRole(topic, getCurrentUserRole(), typeManager
        .getDiscussionForumType());
    
    if(controlPermission!=null && controlPermission.getMovePostings()!=null)
    {
      return controlPermission.getMovePostings().booleanValue();      
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isChangeSettings(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public boolean isChangeSettings(DiscussionTopic topic)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isChangeSettings(DiscussionTopic " + topic + ")");
    }
    TopicControlPermission controlPermission = permissionManager
    .getTopicControlPermissionForRole(topic, getCurrentUserRole(), typeManager
        .getDiscussionForumType());
    
    if(controlPermission!=null && controlPermission.getChangeSettings()!=null)
    {
      return controlPermission.getChangeSettings().booleanValue();      
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isPostToGradebook(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public boolean isPostToGradebook(DiscussionTopic topic)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isPostToGradebook(DiscussionTopic " + topic + ")");
    }
    TopicControlPermission controlPermission = permissionManager
    .getTopicControlPermissionForRole(topic, getCurrentUserRole(), typeManager
        .getDiscussionForumType());
    
    if(controlPermission!=null && controlPermission.getPostToGradebook()!=null)
    {
      return controlPermission.getPostToGradebook().booleanValue();      
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isRead(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public boolean isRead(DiscussionTopic topic)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isRead(DiscussionTopic " + topic + ")");
    }
    MessagePermissions messagePermission = permissionManager
    .getTopicMessagePermissionForRole(topic, getCurrentUserRole(), typeManager
        .getDiscussionForumType());
    
    if(messagePermission!=null && messagePermission.getRead()!=null)
    {
      return messagePermission.getRead().booleanValue();      
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isReviseAny(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public boolean isReviseAny(DiscussionTopic topic)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isReviseAny(DiscussionTopic " + topic + ")");
    }
    MessagePermissions messagePermission = permissionManager
    .getTopicMessagePermissionForRole(topic, getCurrentUserRole(), typeManager
        .getDiscussionForumType());
    
    if(messagePermission!=null && messagePermission.getReviseAny()!=null)
    {
      return messagePermission.getReviseAny().booleanValue();      
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isReviseOwn(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public boolean isReviseOwn(DiscussionTopic topic)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isReviseOwn(DiscussionTopic " + topic + ")");
    }
    MessagePermissions messagePermission = permissionManager
    .getTopicMessagePermissionForRole(topic, getCurrentUserRole(), typeManager
        .getDiscussionForumType());
    
    if(messagePermission!=null && messagePermission.getReviseOwn()!=null)
    {
      return messagePermission.getReviseOwn().booleanValue();      
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isDeleteAny(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public boolean isDeleteAny(DiscussionTopic topic)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isDeleteAny(DiscussionTopic " + topic + ")");
    }
    MessagePermissions messagePermission = permissionManager
    .getTopicMessagePermissionForRole(topic, getCurrentUserRole(), typeManager
        .getDiscussionForumType());
    
    if(messagePermission!=null && messagePermission.getDeleteAny()!=null)
    {
      return messagePermission.getDeleteAny().booleanValue();      
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isDeleteOwn(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public boolean isDeleteOwn(DiscussionTopic topic)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isDeleteOwn(DiscussionTopic " + topic + ")");
    }
    MessagePermissions messagePermission = permissionManager
    .getTopicMessagePermissionForRole(topic, getCurrentUserRole(), typeManager
        .getDiscussionForumType());
    
    if(messagePermission!=null && messagePermission.getDeleteOwn()!=null)
    {
      return messagePermission.getDeleteOwn().booleanValue();      
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#isMarkAsRead(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public boolean isMarkAsRead(DiscussionTopic topic)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isMarkAsRead(DiscussionTopic " + topic + ")");
    }
    MessagePermissions messagePermission = permissionManager
    .getTopicMessagePermissionForRole(topic, getCurrentUserRole(), typeManager
        .getDiscussionForumType());
    
    if(messagePermission!=null && messagePermission.getMarkAsRead()!=null)
    {
      return messagePermission.getMarkAsRead().booleanValue();      
    }
    return false;
  }

  private String getCurrentUser()
  {
    if (TestUtil.isRunningTests())
    {
      return "test-user";
    }
    return sessionManager.getCurrentSessionUserId();
  }

  private String getCurrentUserRole()
  {
    return authzGroupService.getUserRole(getCurrentUser(), "/site/"
        + getContextId());
  }

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

}
