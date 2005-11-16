package org.sakaiproject.tool.messageforums;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.tool.messageforums.ui.DiscussionForumBean;
import org.sakaiproject.tool.messageforums.ui.DiscussionTopicBean;

public class DiscussionForumTool
{
  private static final Log LOG = LogFactory.getLog(DiscussionForumTool.class);
  /**
   * Dependency Injected
   */
  private DiscussionForumManager forumManager;
 
  
  /**
   * 
   */
  public DiscussionForumTool()
  {
    ;
  }

  /**
   * @param forumManager
   */
  public void setForumManager(DiscussionForumManager forumManager)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("setForumManager(DiscussionForumManager " + forumManager + ")");
    }
    this.forumManager = forumManager;
  }

  /**
   * @return
   */
  public List getForums()
  {
    List forums = new ArrayList();
    List tempForum = forumManager.getDiscussionForums();         
    if (tempForum == null)
    {
      return null;
    }
    Iterator iterForum = tempForum.iterator();
    while (iterForum.hasNext())
    {
      DiscussionForum forum = (DiscussionForum) iterForum.next();
      if (forum == null)
      {
        return forums;
      }
      List temp_topics = forum.getTopics();
      if (temp_topics == null)
      {
        return forums;
      }
      DiscussionForumBean decoForum= new DiscussionForumBean(forum);
      Iterator iter = temp_topics.iterator();
      while (iter.hasNext())
      {
        Topic topic = (Topic) iter.next();
        if (topic != null)
        {
          DiscussionTopicBean decoTopic = new DiscussionTopicBean(topic);
          decoTopic.setTotalNoMessages(forumManager.getTotalNoMessages(topic));
          decoTopic.setUnreadNoMessages(forumManager.getUnreadNoMessages(topic));
          decoForum.addTopic(decoTopic);
        }
      }
      forums.add(decoForum);
    }
    return forums;
  }

  /**
   * TODO:// complete featute
   * 
   * @return
   */
  public boolean getUnderconstruction()
  {
    return true;
  }

  /**
   * @return
   */
  public String processCreateNewForum()
  {
    return "main";
  }

  /**
   * @return
   */
  public String processOrganize()
  {
    return "main";
  }

  /**
   * @return
   */
  public String processStatistics()
  {
    return "main";
  }

  /**
   * @return
   */
  public String processTemplateSettings()
  {
    return "main";
  }

  /**
   * @return
   */
  public String processForumSettings()
  {
    return "main";
  }

  /**
   * @return
   */
  public String processCreateNewTopic()
  {
    return "main";
  }

  /**
   * @return
   */
  public String processTopicSettings()
  {
    return "main";
  }
  
  /**
   * @return
   */
  public String processDisplayForum()
  {
    return "main";
  }
  
  /**
   * @return
   */
  public String processDisplayTopic()
  {
    return "main";
  }
}