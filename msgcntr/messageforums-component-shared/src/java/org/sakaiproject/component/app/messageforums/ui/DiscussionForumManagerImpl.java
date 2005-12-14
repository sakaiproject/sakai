package org.sakaiproject.component.app.messageforums.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaControlPermission;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.DummyDataHelperApi;
import org.sakaiproject.api.app.messageforums.ForumControlPermission;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.PermissionManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.TopicControlPermission;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.kernel.tool.cover.ToolManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.service.legacy.authzGroup.AuthzGroup;
import org.sakaiproject.service.legacy.authzGroup.Role;
import org.sakaiproject.service.legacy.authzGroup.cover.AuthzGroupService;
import org.sakaiproject.service.legacy.security.cover.SecurityService;
import org.sakaiproject.service.legacy.user.User;
import org.sakaiproject.service.legacy.user.cover.UserDirectoryService;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 */
public class DiscussionForumManagerImpl extends HibernateDaoSupport implements
    DiscussionForumManager
{
  private static final Log LOG = LogFactory
      .getLog(DiscussionForumManagerImpl.class);
  private AreaManager areaManager;
  private MessageForumsForumManager forumManager;
  private MessageForumsMessageManager messageManager;
  private DummyDataHelperApi helper;
  private PermissionManager permissionManager;
  private MessageForumsTypeManager typeManager;
  private boolean usingHelper = false; // just a flag until moved to database from helper

  public void init()
  {
    ;
  }

  // start injection
  /**
   * @param helper
   */
  public void setHelper(DummyDataHelperApi helper)
  {
    this.helper = helper;
  }

  /**
   * @param areaManager
   */
  public void setAreaManager(AreaManager areaManager)
  {
    this.areaManager = areaManager;
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
   * @return
   */
  public MessageForumsMessageManager getMessageManager()
  {
    return messageManager;
  }

  /**
   * @param messageManager
   */
  public void setMessageManager(MessageForumsMessageManager messageManager)
  {
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
  public void saveMessage(Message message)
  {
    messageManager.saveMessage(message);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#deleteMessage(org.sakaiproject.api.app.messageforums.Message)
   */
  public void deleteMessage(Message message)
  {
    messageManager.deleteMessage(message);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getTotalNoMessages(org.sakaiproject.api.app.messageforums.Topic)
   */
  public int getTotalNoMessages(Topic topic)
  {
    if (usingHelper)
    {
      return 20;
    }
    return messageManager.findMessageCountByTopicId(topic.getId());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getUnreadNoMessages(org.sakaiproject.api.app.messageforums.Topic)
   */
  public int getUnreadNoMessages(String userId, Topic topic)
  {
    if (usingHelper)
    {
      return 10;
    }
    return messageManager
        .findUnreadMessageCountByTopicId(userId, topic.getId());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getDiscussionForums()
   */
  public List getDiscussionForums()
  {
    if (usingHelper)
    {
      return helper.getDiscussionForumArea().getDiscussionForums();
    }
    return getDiscussionForumArea().getDiscussionForums();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getForumById(java.lang.Long)
   */
  public DiscussionForum getForumById(Long forumId)
  {
    if (usingHelper)
    {
      return helper.getForumById(forumId);
    }
    return (DiscussionForum) forumManager.getForumById(true, forumId);
  }

  public DiscussionForum getForumByUuid(String forumId)
  {
    return (DiscussionForum) forumManager.getForumByUuid(forumId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getMessagesByTopicId(java.lang.Long)
   */
  public List getMessagesByTopicId(Long topicId)
  {
    if (usingHelper)
    {
      return helper.getMessagesByTopicId(topicId);
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
    if (usingHelper)
    {
      return helper.getTopicById(topicId);
    }
    return (DiscussionTopic) forumManager.getTopicById(topicId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#hasNextTopic(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public boolean hasNextTopic(DiscussionTopic topic)
  {
    if (usingHelper)
    {
      return helper.hasNextTopic(topic);
    }

    // TODO: Needs optimized
    boolean next = false;
    DiscussionForum forum = (DiscussionForum) topic.getBaseForum();
    if (forum != null && forum.getTopics() != null)
    {
      for (Iterator iter = forum.getTopics().iterator(); iter.hasNext();)
      {
        Topic t = (Topic) iter.next();
        if (next)
        {
          return true;
        }
        if (t.getId().equals(topic.getId()))
        {
          next = true;
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
    if (usingHelper)
    {
      return helper.hasPreviousTopic(topic);
    }

    // TODO: Needs optimized
    DiscussionTopic prev = null;
    DiscussionForum forum = (DiscussionForum) topic.getBaseForum();
    if (forum != null && forum.getTopics() != null)
    {
      for (Iterator iter = forum.getTopics().iterator(); iter.hasNext();)
      {
        Topic t = (Topic) iter.next();
        if (t.getId().equals(topic.getId()))
        {
          // need to check null because we might be on the first topic
          // which means there is no previous one
          return prev != null;
        }
        prev = (DiscussionTopic) t;
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

    // TODO: Needs optimized
    boolean next = false;
    DiscussionForum forum = (DiscussionForum) topic.getBaseForum();
    if (forum != null && forum.getTopics() != null)
    {
      for (Iterator iter = forum.getTopics().iterator(); iter.hasNext();)
      {
        Topic t = (Topic) iter.next();
        if (next)
        {
          return (DiscussionTopic) t;
        }
        if (t.getId().equals(topic.getId()))
        {
          next = true;
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
    DiscussionForum forum = (DiscussionForum) topic.getBaseForum();
    if (forum != null && forum.getTopics() != null)
    {
      for (Iterator iter = forum.getTopics().iterator(); iter.hasNext();)
      {
        Topic t = (Topic) iter.next();
        if (t.getId().equals(topic.getId()))
        {
          return prev;
        }
        prev = (DiscussionTopic) t;
      }
    }

    // if we get here, there is no previous topic
    return null;
  }

  /* (non-Javadoc)
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
      return SecurityService.unlock(user, "site.upd", getContextSiteId());
    else
      return false;
  }

  /**
   * @return siteId
   */
  private String getContextSiteId()
  {
    LOG.debug("getContextSiteId()");
    return ("/site/" + ToolManager.getCurrentPlacement().getContext());
  }
 

  /**
   * @param forumManager
   */
  public void setForumManager(MessageForumsForumManager forumManager)
  {
    this.forumManager = forumManager;
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#createForum()
   */
  public DiscussionForum createForum()
  {
    LOG.debug("createForum()");
    return forumManager.createDiscussionForum();
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#deleteForum(org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public void deleteForum(DiscussionForum forum)
  {
    LOG.debug("deleteForum()");
    forumManager.deleteDiscussionForum(forum);
  }
  
  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#createTopic(org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public DiscussionTopic createTopic(DiscussionForum forum)
  {
    LOG.debug("createTopic()");
    if (forum == null)
    {
      LOG.debug("Attempt to create topic with out forum");
      return null;
    }
    return forumManager.createDiscussionForumTopic(forum);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#saveForum(org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public void saveForum(DiscussionForum forum)
  {
    LOG.debug("saveForum()");

    boolean saveArea = forum.getId() == null;

    forumManager.saveDiscussionForum(forum);
    if (saveArea) {
        Area area = getDiscussionForumArea();
        forum.setArea(area);    
        area.addDiscussionForum(forum);
        areaManager.saveArea(area);
    }
  }

  public void saveTopic(DiscussionTopic topic)
  {
    LOG.debug("saveTopic(DiscussionTopic "+topic+")");
    
    boolean saveForum = topic.getId() == null;
    
    forumManager.saveDiscussionForumTopic(topic);
    if (saveForum) {
        DiscussionForum forum = (DiscussionForum) topic.getBaseForum();
        forum.addTopic(topic);
        forumManager.saveDiscussionForum(forum);
    }
  }
  
  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#deleteTopic(org.sakaiproject.api.app.messageforums.DiscussionTopic)
   */
  public void deleteTopic(DiscussionTopic topic)
  {
    LOG.debug("deleteTopic(DiscussionTopic "+topic+")");
    forumManager.deleteDiscussionForumTopic(topic);
  }
  
  /* (non-Javadoc)
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
      if (controlPermission == null)
      {
        controlPermission = permissionManager
            .createDefaultAreaControlPermissionForRole(roleId);
      }
      defaultControlPermissions.add(controlPermission);
    }
    return defaultControlPermissions;
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getDefaultMessagePermissions()
   */
  public List getDefaultMessagePermissions()
  {
    return null;
  }

  
  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#saveDefaultMessagePermissions(java.util.List)
   */
  public void saveDefaultMessagePermissions(List controlpermissions)
  {
 
    //  TODO Auto-generated method stub
 
  }
  
  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getForumControlPermissions(org.sakaiproject.api.app.messageforums.DiscussionForum)
   */
  public List getForumControlPermissions(DiscussionForum forum)
  {
    
    LOG.debug("getForumControlPermissions(DiscussionForum "+forum+")");
    List forumControlPermissions = new ArrayList();
    Iterator roles = getRoles();
    while (roles.hasNext())
    {
      String roleId = (String) roles.next();
      ForumControlPermission controlPermission = permissionManager.getForumControlPermissionForRole(forum, roleId,typeManager.getDiscussionForumType());
          
      if (controlPermission == null)
      {
        controlPermission = permissionManager
            .createForumControlPermissionForRole(roleId);
      }
      forumControlPermissions.add(controlPermission);
    }
    return forumControlPermissions;
  }
   
  public List getForumMessagePermissions(DiscussionForum forum)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public List getTopicControlPermissions(DiscussionTopic topic)
  {
    List topicControlPermissions = new ArrayList();
    Iterator roles = getRoles();
    while (roles.hasNext())
    {
      String roleId = (String) roles.next();
      TopicControlPermission controlPermission = permissionManager.getTopicControlPermissionForRole(topic,roleId,typeManager.getDiscussionForumType());
          
      if (controlPermission == null)
      {
        controlPermission = permissionManager
            .createTopicControlPermissionForRole(roleId);
      }
      topicControlPermissions.add(controlPermission);
    }
    return topicControlPermissions;
    
  }

  public List getTopicMessagePermissions(DiscussionTopic topic)
  {
    // TODO Auto-generated method stub
    return null;
  }



  public void saveDefaultControlPermissions(List controlpermissions)
  {
    if(controlpermissions !=null && controlpermissions.size()>0)
    {
      Iterator iterator= controlpermissions.iterator();
      while (iterator.hasNext())
      {
        AreaControlPermission controlPermission = (AreaControlPermission) iterator.next();
        permissionManager.saveDefaultAreaControlPermissionForRole(getDiscussionForumArea(),controlPermission, typeManager.getDiscussionForumType());
      }
    }
    
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#saveForumControlPermissions(org.sakaiproject.api.app.messageforums.DiscussionForum, java.util.List)
   */
  public void saveForumControlPermissions(DiscussionForum forum, List controlPermissions)
  {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#saveForumMessagePermissions(org.sakaiproject.api.app.messageforums.DiscussionForum, java.util.List)
   */
  public void saveForumMessagePermissions(DiscussionForum forum, List messagePermissions)
  {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#saveTopicControlPermissions(org.sakaiproject.api.app.messageforums.DiscussionForum, java.util.List)
   */
  public void saveTopicControlPermissions(DiscussionForum forum, List controlPermissions)
  {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#saveTopicMessagePermissions(org.sakaiproject.api.app.messageforums.DiscussionTopic, java.util.List)
   */
  public void saveTopicMessagePermissions(DiscussionTopic topic, List messagePermissions)
  {
    // TODO Auto-generated method stub
    
  }
  
  /**
   * @return Roles for the current site
   */
  private Iterator getRoles()
  {
    LOG.debug("getRoles()");
    List roleList = new ArrayList();
    AuthzGroup realm;
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
    catch (IdUnusedException e)
    {
      LOG.error(e.getMessage(), e);
    }
    Collections.sort(roleList);
    return roleList.iterator();
  }

}
