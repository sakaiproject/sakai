package org.sakaiproject.component.app.messageforums.ui;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.DummyDataHelperApi;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.kernel.tool.cover.ToolManager;
import org.sakaiproject.component.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.service.legacy.security.cover.SecurityService;
import org.sakaiproject.service.legacy.user.User;
import org.sakaiproject.service.legacy.user.cover.UserDirectoryService;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 *
 */
public class DiscussionForumManagerImpl extends HibernateDaoSupport implements
    DiscussionForumManager
{
  private static final Log LOG = LogFactory.getLog(DiscussionForumManagerImpl.class);
  private AreaManager areaManager;
  private MessageForumsForumManager forumManager;
  private MessageForumsMessageManager messageManager;
  private DummyDataHelperApi helper;
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
   * @return
   */
  public AreaManager getAreaManager()
  {
    return areaManager;
  }

  /**
   * @param areaManager
   */
  public void setAreaManager(AreaManager areaManager)
  {
    this.areaManager = areaManager;
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
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getMessageById(java.lang.String)
   */
  public Message getMessageById(String id)
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
    return messageManager.findMessageCountByTopicId(topic.getId().toString());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getUnreadNoMessages(org.sakaiproject.api.app.messageforums.Topic)
   */
  public int getUnreadNoMessages(String userId, Topic topic)
  {
    return messageManager.findUnreadMessageCountByTopicId(userId, topic.getId().toString());
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
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getForumById(java.lang.String)
   */
  public DiscussionForum getForumById(String forumId)
  {
    if (usingHelper)
    {
      return helper.getForumById(forumId);
    }
    return (DiscussionForum) forumManager.getForumById(forumId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getMessagesByTopicId(java.lang.String)
   */
  public List getMessagesByTopicId(String topicId)
  {
    if (usingHelper)
    {
      return helper.getMessagesByTopicId(topicId);
    }
    return messageManager.findMessagesByTopicId(topicId);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager#getTopicById(java.lang.String)
   */
  public DiscussionTopic getTopicById(String topicId)
  {
    if (usingHelper)
    {
      return helper.getTopicById(topicId);
    }
    return (DiscussionTopic) forumManager.getTopicById(topicId);
  }

  /* (non-Javadoc)
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
    if (forum != null && forum.getTopics() != null) {
        for (Iterator iter = forum.getTopics().iterator(); iter.hasNext();) {
            Topic t = (Topic) iter.next();
            if (next) {
                return true;
            }
            if (t.getId().equals(topic.getId())) {
                next = true;
            }
        }
    }

    // if we get here, there is no next topic
    return false;
  }

  /* (non-Javadoc)
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
    if (forum != null && forum.getTopics() != null) {
        for (Iterator iter = forum.getTopics().iterator(); iter.hasNext();) {
        Topic t = (Topic) iter.next();
            if (t.getId().equals(topic.getId())) {
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

  /* (non-Javadoc)
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
    if (forum != null && forum.getTopics() != null) {
        for (Iterator iter = forum.getTopics().iterator(); iter.hasNext();) {
            Topic t = (Topic) iter.next();
            if (next) {
                return (DiscussionTopic) t;
            }
            if (t.getId().equals(topic.getId())) {
                next = true;
            }
        }
    }
    
    // if we get here, there is no next topic
    return null;
  }

  /* (non-Javadoc)
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
    if (forum != null && forum.getTopics() != null) {
        for (Iterator iter = forum.getTopics().iterator(); iter.hasNext();) {
        Topic t = (Topic) iter.next();
            if (t.getId().equals(topic.getId())) {
                return prev;
            }
            prev = (DiscussionTopic) t;
        }
    }
        
    // if we get here, there is no previous topic
    return null;
  }

  
  public boolean isInstructor()
  {
    LOG.debug("isInstructor()");
    return isInstructor(UserDirectoryService.getCurrentUser());
  }
  
  /**
   * Check if the given user has site.upd access
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
  
  public MessageForumsForumManager getForumManager() {
    return forumManager;
  }

  public void setForumManager(MessageForumsForumManager forumManager) {
    this.forumManager = forumManager;
  }

  public DiscussionForum createForum()
  {
    LOG.debug("createForum()");
    return forumManager.createDiscussionForum();
  }

  public DiscussionTopic createTopic()
  {
    LOG.debug("createTopic()");
    return forumManager.createDiscussionForumTopic();
  }

  public void saveForum(DiscussionForum forum)
  {
    LOG.debug("saveForum()");
    forumManager.saveDiscussionForum(forum);
  }

  public void saveTopic(DiscussionTopic topic)
  {
    LOG.debug("saveTopic()");
    forumManager.saveDiscussionForumTopic(topic);
  }
  
}
