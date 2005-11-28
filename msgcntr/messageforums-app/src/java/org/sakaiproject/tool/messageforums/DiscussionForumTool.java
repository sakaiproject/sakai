package org.sakaiproject.tool.messageforums;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.tool.messageforums.ui.DiscussionForumBean;
import org.sakaiproject.tool.messageforums.ui.DiscussionMessageBean;
import org.sakaiproject.tool.messageforums.ui.DiscussionTopicBean;

/**
 * @author rshastri
 *
 */
public class DiscussionForumTool
{
  private static final Log LOG = LogFactory.getLog(DiscussionForumTool.class);

  /**
   * List individual forum details 
   */
  private static final String FORUM_DETAILS =  "dfForumDetail";
  private static final String ALL_MESSAGES =  "dfAllMessages";
  private static final String THREADED_VIEW =  "dfThreadedView";
  private static final String COMPOSE =  "dfCompose";
  private static final String MAIN =  "main";
  
  private DiscussionForumBean selectedForum;
  private DiscussionTopicBean selectedTopic;
  /**
   * Dependency Injected
   */
  private DiscussionForumManager forumManager;
 
  
  /**
   * 
   */
  public DiscussionForumTool()
  {
    LOG.debug("DiscussionForumTool()");
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
    LOG.debug("getForums()");
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
      DiscussionForumBean decoForum= getDecoratedForum(forum);
      forums.add(decoForum);
    }
    return forums;
  }

  
  /**
   * @return Returns the selectedForum.
   */
  public DiscussionForumBean getSelectedForum()
  {
    LOG.debug("getSelectedForum()");
    return selectedForum;
  }

  
  /**
   * @return Returns the selectedTopic.
   */
  public DiscussionTopicBean getSelectedTopic()
  {
    return selectedTopic;
  }

  /**
   * TODO:// complete featute
   * 
   * @return
   */
  public boolean getUnderconstruction()
  {
    LOG.debug("getUnderconstruction()");
    return true;
  }

  /**
   * @return
   */
  public String processCreateNewForum()
  {
    LOG.debug("processCreateNewForum()");
    return MAIN;
  }

  /**
   * @return
   */
  public String processOrganize()
  {
    LOG.debug("processOrganize()");
    return MAIN;
  }

  /**
   * @return
   */
  public String processStatistics()
  {
    LOG.debug("processStatistics()");
    return MAIN;
  }

  /**
   * @return
   */
  public String processTemplateSettings()
  {
    LOG.debug("processTemplateSettings()");
    return MAIN;
  }

  /**
   * @return
   */
  public String processForumSettings()
  {
    LOG.debug("processForumSettings()");
    return MAIN;
  }

  /**
   * @return
   */
  public String processCreateNewTopic()
  {
    LOG.debug("processCreateNewTopic()");
    return MAIN;
  }

  /**
   * @return
   */
  public String processTopicSettings()
  {
    LOG.debug("processTopicSettings()");
    return MAIN;
  }
  
  /**
   * Display Individual forum
   * @return
   */
  public String processDisplayForum()
  {    
    LOG.debug("processDisplayForum()");
    String forumId=null;
    try
    {
      ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
      Map paramMap = context.getRequestParameterMap();
      Iterator itr = paramMap.keySet().iterator();
      while (itr.hasNext())
      {
        String key = (String) itr.next();
        if (key != null && key.equals("forumId"))
        {
          forumId = (String) paramMap.get(key);
          break;
        }
      }
      if(forumId!=null)
      {
        DiscussionForum forum = forumManager.getForumById(forumId);
        selectedForum = getDecoratedForum(forum);       
      }
      else
      {
        //TODO :  appropriate error page
        return MAIN;
      }
    }
    catch (Exception e)
    {
      //TODO  appropriate error page
      return "main"; 
    }
    return FORUM_DETAILS;
  }
  
  /**
   * @return
   */
  public String processDisplayTopic()
  {
    LOG.debug("processDisplayTopic()");
    
    return processDisplayTopicById("topicId");
  } 
  
  /**
   * @return
   */
  public String processDisplayNextTopic()
  {
    LOG.debug("processDisplayNextTopic()");
    return processDisplayTopicById("nextTopicId");
  }
  
  /**
   * @return
   */
  public String processDisplayPreviousTopic()
  {
    LOG.debug("processDisplayNextTopic()");
    return processDisplayTopicById("previousTopicId");
  }
  
  /**
   * @return
   */
  public String processDisplayMessage()
  {
    LOG.debug("processDisplayMessage()");
    return  MAIN;
  }
  
  /**
   * @return
   */
  public String processActionHome()
  {
    LOG.debug("processActionHome()");
    return  MAIN;
  }
  
  /**
   * @param externalTopicId
   * @return
   */
  private String processDisplayTopicById(String externalTopicId)
  {
    if(LOG.isDebugEnabled())
    {
      LOG.debug("processDisplayTopicById(String"+ externalTopicId+")");
    }
    String topicId=null;
    try
    {
      ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
      Map paramMap = context.getRequestParameterMap();
      Iterator itr = paramMap.keySet().iterator();
      while (itr.hasNext())
      {
        String key = (String) itr.next();
        if (key != null && key.equals(externalTopicId))
        {
          topicId = (String) paramMap.get(key);
          break;
        }
      }
      if(topicId!=null)
      {
        DiscussionTopic topic = forumManager.getTopicById(topicId);
        selectedTopic = getDecoratedTopic(topic);       
      }
      else
      {
        //TODO :  appropriate error page
        return MAIN;
      }
    }
    catch (Exception e)
    {
      //TODO  appropriate error page
      return "main"; 
    }
    return ALL_MESSAGES;
  }
  
  
  //helper method
  /**
   * @param forum
   * @return
   */
  private DiscussionForumBean getDecoratedForum(DiscussionForum forum)
  {
    if(LOG.isDebugEnabled())
    {
      LOG.debug("getDecoratedForum(DiscussionForum"+ forum+")");
    }
    DiscussionForumBean decoForum = new DiscussionForumBean(forum);
    List temp_topics = forum.getTopics();
    if (temp_topics == null)
    {
     return  decoForum;
    }
    Iterator iter = temp_topics.iterator();
    while (iter.hasNext())
    {
      DiscussionTopic topic = (DiscussionTopic) iter.next();
      if (topic != null)
      {
        DiscussionTopicBean decoTopic = new DiscussionTopicBean(topic);
        decoTopic.setTotalNoMessages(forumManager.getTotalNoMessages(topic));
        decoTopic.setUnreadNoMessages(forumManager.getUnreadNoMessages(topic));
        decoForum.addTopic(decoTopic);
      }
    }
    return decoForum;
  }
  
  /**
   * @param topic
   * @return
   */
  private DiscussionTopicBean getDecoratedTopic(DiscussionTopic topic)
  {
    if(LOG.isDebugEnabled())
    {
      LOG.debug("getDecoratedTopic(DiscussionTopic "+ topic+ ")");
    }
    DiscussionTopicBean decoTopic = new DiscussionTopicBean(topic);
    //TODO : implement me
    decoTopic.getTopic().setBaseForum(forumManager.getForumById("5"));
    decoTopic.setTotalNoMessages(forumManager.getTotalNoMessages(topic));
    decoTopic.setUnreadNoMessages(forumManager.getUnreadNoMessages(topic));
    decoTopic.setHasNextTopic(forumManager.hasNextTopic(topic));
    decoTopic.setHasPreviousTopic(forumManager.hasPreviousTopic(topic));
    if(forumManager.hasNextTopic(topic))
    {
      decoTopic.setNextTopicId(forumManager.getNextTopic(topic).getUuid());
    }
    if(forumManager.hasPreviousTopic(topic))
    {
      decoTopic.setPreviousTopicId(forumManager.getPreviousTopic(topic).getUuid());
    }
    List temp_messages = topic.getMessages();
    if(temp_messages == null)
    {
      return decoTopic;
    }
    Iterator iter = temp_messages.iterator();
    while (iter.hasNext())
    {
      Message message = (Message) iter.next();
      if (topic != null)
      {
        DiscussionMessageBean decoMsg = new DiscussionMessageBean(message);
        decoTopic.setTotalNoMessages(forumManager.getTotalNoMessages(topic));
        decoTopic.setUnreadNoMessages(forumManager.getUnreadNoMessages(topic));
        decoTopic.addMessage(decoMsg);
      }
    }
    return decoTopic;    
  }
}