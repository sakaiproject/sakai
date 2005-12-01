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
import org.sakaiproject.api.kernel.session.cover.SessionManager;
import org.sakaiproject.tool.messageforums.ui.DiscussionForumBean;
import org.sakaiproject.tool.messageforums.ui.DiscussionMessageBean;
import org.sakaiproject.tool.messageforums.ui.DiscussionTopicBean;

/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 */
public class DiscussionForumTool
{
  private static final Log LOG = LogFactory.getLog(DiscussionForumTool.class);

  /**
   * List individual forum details
   */
  private static final String MAIN = "main";
  private static final String COMPOSE = "dfCompose";
  private static final String FORUM_DETAILS = "dfForumDetail";
  private static final String ALL_MESSAGES = "dfAllMessages";
  private static final String THREADED_VIEW = "dfThreadedView";
  private static final String TEMPLATE_SETTING = "dfTemplateSettings";
  private static final String FORUM_SETTING = "dfForumSettings";
  private static final String FORUM_SETTING_REVISE = "dfReviseForumSettings";
  private static final String FORUM_CONFIRM_DELETE = "dfConfirmForumDelete";
  private static final String TOPIC_SETTING = "dfTopicSettings";
  private static final String TOPIC_SETTING_REVISE = "dfReviseTopicSettings";
  
  private DiscussionForumBean selectedForum;
  private DiscussionTopicBean selectedTopic;
  private List forums = new ArrayList();
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
  public String processActionHome()
  {
    LOG.debug("processActionHome()");
    return MAIN;
  }

  /**
   * @return
   */
  public boolean isInstructor()
  {
    LOG.debug("isInstructor()");
    return forumManager.isInstructor();
  }

  /**
   * @return
   */
  public List getForums()
  {
    LOG.debug("getForums()");
    if (forums == null || forums.size() < 1)
    {
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
        DiscussionForumBean decoForum = getDecoratedForum(forum);
        forums.add(decoForum);
      }
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
  public String processActionOrganize()
  {
    LOG.debug("processActionOrganize()");
    return MAIN;
  }

  /**
   * @return
   */
  public String processActionStatistics()
  {
    LOG.debug("processActionStatistics()");
    return MAIN;
  }

  /**
   * @return
   */
  public String processActionTemplateSettings()
  {
    LOG.debug("processActionTemplateSettings()");
    return TEMPLATE_SETTING;
  }

  /**
   * Display Individual forum
   * 
   * @return
   */
  public String processActionDisplayForum()
  {
    LOG.debug("processDisplayForum()");
    if (decorateSelectedForum() == null)
    {
      // TODO : appropriate error page
      return MAIN;
    }
    return FORUM_DETAILS;
  }

  /**
   * @return
   */
  public String processActionDeleteForumConfirm()
  {
    return MAIN;
  }

  /**
   * @return
   */
  public String processActionDeleteForum()
  {
    return MAIN;
  }

  /**
   * @return
   */
  public String processActionNewForum()
  {
    LOG.debug("processActionNewForum()");
    DiscussionForum forum = forumManager.createForum();
    selectedForum = new DiscussionForumBean(forum);
    return FORUM_SETTING_REVISE;
  }

  /**
   * @return
   */
  public String processActionForumSettings()
  {
    LOG.debug("processForumSettings()");
    String forumId = getExternalParameterByKey("forumId");
    if ((forumId) == null)
    {
      // TODO : appropriate error page
      return MAIN;
    }
    DiscussionForum forum = forumManager.getForumById(forumId);
    selectedForum = new DiscussionForumBean(forum);

    return FORUM_SETTING;
  }

  /**
   * @return
   */
  public String processActionReviseForumSettings()
  {
    LOG.debug("processActionReviseForumSettings()");
   
    if ((selectedForum) == null)
    {
      // TODO : appropriate error page
      return MAIN;
    }
//    DiscussionForum forum = forumManager.getForumById(forumId);
//    selectedForum = new DiscussionForumBean(forum);
    return FORUM_SETTING_REVISE; //
  }
  
  /**
   * @return
   */
  public String processActionSaveForumAndAddTopic()
  {
    LOG.debug("processActionSaveForumAndAddTopic()");
    //TODO : save forum
    prepareForReviseTopicSettings();
    return TOPIC_SETTING_REVISE;  
  }
  
  /**
   * @return
   */
  public String processActionSaveForumSettings()
  {
    LOG.debug("processActionSaveForumAsDraft()");
    return MAIN;
  }
  
  /**
   * @return
   */
  public String processActionSaveForumAsDraft()
  {
    LOG.debug("processActionSaveForumAsDraft()");
    return MAIN;
  }
  

  /**
   * @return
   */
  public String processActionNewTopic()
  {
    LOG.debug("processActionNewTopic()");
    prepareForReviseTopicSettings();
    return TOPIC_SETTING_REVISE;  
  }
  
  /**
   * @return
   */
  public String processActionReviseTopicSettings()
  {
    LOG.debug("processActionReviseTopicSettings()");
    DiscussionTopic topic = forumManager.getTopicById(getExternalParameterByKey("topicId"));
    if(topic==null)
    {
      return MAIN;
    }
    selectedTopic = new DiscussionTopicBean(topic);
    selectedTopic.setParentForumId(getExternalParameterByKey("forumId"));
    return TOPIC_SETTING_REVISE;  
  }
  
  
  /**
   * 
   */
  private void prepareForReviseTopicSettings()
  {
    DiscussionTopic topic = forumManager.createTopic();
    selectedTopic = new DiscussionTopicBean(topic);
    selectedTopic.setParentForumId(getExternalParameterByKey("forumId"));
  }
  /**
   * @return
   */
  public String processActionSaveTopicAndAddTopic()
  {
    LOG.debug("processActionSaveTopicAndAddTopic()");
    //TODO : save topic
    prepareForReviseTopicSettings();
    return TOPIC_SETTING_REVISE;     
  }
  
  /**
   * @return
   */
  public String processActionSaveTopicSettings()
  {
    LOG.debug("processActionSaveTopicSettings()");
    return MAIN;
  }
  
  /**
   * @return
   */
  public String processActionSaveTopicAsDraft()
  {
    LOG.debug("processActionSaveTopicAsDraft()");
    return MAIN;
  }
  
  /**
   * @return
   */
  public String processActionDeleteTopicConfirm()
  {
    return MAIN;
  }

  /**
   * @return
   */
  public String processActionDeleteTopic()
  {
    return MAIN;
  }

  
  /**
   * @return Returns the selectedTopic.
   */
  public DiscussionTopicBean getSelectedTopic()
  {
    return selectedTopic;
  }

  /**
   * @return
   */
  public String processActionTopicSettings()
  {
    LOG.debug("processActionTopicSettings()");
    DiscussionTopic topic = forumManager.getTopicById(getExternalParameterByKey("topicId"));
    if(topic==null)
    {
      return MAIN;
    }
    selectedTopic = new DiscussionTopicBean(topic);
    selectedTopic.setParentForumId(getExternalParameterByKey("forumId"));
    return TOPIC_SETTING;
  }

  /**
   * @return
   */
  public String processActionToggleDisplayExtendedDescription()
  {
    LOG.debug("processActionToggleDisplayExtendedDescription()");
    List tmpForums = getForums();
    if (tmpForums != null)
    {
      Iterator iter = tmpForums.iterator();
      while (iter.hasNext())
      {
        DiscussionForumBean decoForumBean = (DiscussionForumBean) iter.next();
        if (decoForumBean != null)
        {
          List tmpTopics = decoForumBean.getTopics();
          Iterator iter2 = tmpTopics.iterator();
          while (iter2.hasNext())
          {
            DiscussionTopicBean decoTopicBean = (DiscussionTopicBean) iter2
                .next();
            if (decoTopicBean != null)
            {
              // if this topic is selected to display full desciption
              if (getExternalParameterByKey("topicId_displayExtended") != null
                  && decoTopicBean.getTopic().getUuid().equals(
                      getExternalParameterByKey("topicId_displayExtended")))
              {
                decoTopicBean.setReadFullDesciption(true);
              }
              // if this topic is selected to display hide extended desciption
              if (getExternalParameterByKey("topicId_hideExtended") != null
                  && decoTopicBean.getTopic().getUuid().equals(
                      getExternalParameterByKey("topicId_hideExtended")))
              {
                decoTopicBean.setReadFullDesciption(false);
              }
            }
          }
        }
      }

    }
    return MAIN;
  }

  /**
   * @return
   */
  public String processActionDisplayTopic()
  {
    LOG.debug("processActionDisplayTopic()");

    return processActionDisplayTopicById("topicId");
  }

  /**
   * @return
   */
  public String processActionDisplayNextTopic()
  {
    LOG.debug("processActionDisplayNextTopic()");
    return processActionDisplayTopicById("nextTopicId");
  }

  /**
   * @return
   */
  public String processActionDisplayPreviousTopic()
  {
    LOG.debug("processActionDisplayNextTopic()");
    return processActionDisplayTopicById("previousTopicId");
  }

  /**
   * @return
   */
  public String processActionDisplayMessage()
  {
    LOG.debug("processActionDisplayMessage()");
    return MAIN;
  }

  // **************************************** helper methods**********************************

  /**
   * @return
   */
  private String getExternalParameterByKey(String parameterId)
  {
    String parameterValue = null;
    ExternalContext context = FacesContext.getCurrentInstance()
        .getExternalContext();
    Map paramMap = context.getRequestParameterMap();
    Iterator itr = paramMap.keySet().iterator();
    while (itr.hasNext())
    {
      String key = (String) itr.next();
      if (key != null && key.equals(parameterId))
      {
        parameterValue = (String) paramMap.get(key);
        break;
      }
    }
    return parameterValue;
  }

  /**
   * @param forum
   * @return
   */
  private DiscussionForumBean getDecoratedForum(DiscussionForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getDecoratedForum(DiscussionForum" + forum + ")");
    }

    DiscussionForumBean decoForum = new DiscussionForumBean(forum);
    List temp_topics = forum.getTopics();
    if (temp_topics == null)
    {
      return decoForum;
    }
    Iterator iter = temp_topics.iterator();
    while (iter.hasNext())
    {
      DiscussionTopic topic = (DiscussionTopic) iter.next();
      if (topic != null)
      {
        DiscussionTopicBean decoTopic = new DiscussionTopicBean(topic);
        decoTopic.setTotalNoMessages(forumManager.getTotalNoMessages(topic));
        decoTopic.setUnreadNoMessages(forumManager.getUnreadNoMessages(
            SessionManager.getCurrentSessionUserId(), topic));
        decoForum.addTopic(decoTopic);
      }
    }
    return decoForum;
  }

  /**
   * @return DiscussionForumBean
   */
  private DiscussionForumBean decorateSelectedForum()
  {
    LOG.debug("decorateSelectedForum()");
    String forumId = getExternalParameterByKey("forumId");
    if ((forumId) != null)
    {
      DiscussionForum forum = forumManager.getForumById(forumId);
      selectedForum = getDecoratedForum(forum);
      return selectedForum;
    }
    return null;
  }

  /**
   * @param topic
   * @return
   */
  private DiscussionTopicBean getDecoratedTopic(DiscussionTopic topic)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getDecoratedTopic(DiscussionTopic " + topic + ")");
    }
    DiscussionTopicBean decoTopic = new DiscussionTopicBean(topic);
    // TODO : implement me
    decoTopic.getTopic().setBaseForum(forumManager.getForumById("5"));
    decoTopic.setTotalNoMessages(forumManager.getTotalNoMessages(topic));
    decoTopic.setUnreadNoMessages(forumManager.getUnreadNoMessages(
        SessionManager.getCurrentSessionUserId(), topic));
    decoTopic.setHasNextTopic(forumManager.hasNextTopic(topic));
    decoTopic.setHasPreviousTopic(forumManager.hasPreviousTopic(topic));
    if (forumManager.hasNextTopic(topic))
    {
      decoTopic.setNextTopicId(forumManager.getNextTopic(topic).getUuid());
    }
    if (forumManager.hasPreviousTopic(topic))
    {
      decoTopic.setPreviousTopicId(forumManager.getPreviousTopic(topic)
          .getUuid());
    }
    List temp_messages = topic.getMessages();
    if (temp_messages == null)
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
        decoTopic.setUnreadNoMessages(forumManager.getUnreadNoMessages(
            SessionManager.getCurrentSessionUserId(), topic));
        decoTopic.addMessage(decoMsg);
      }
    }
    return decoTopic;
  }

  /**
   * @param externalTopicId
   * @return
   */
  private String processActionDisplayTopicById(String externalTopicId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("processActionDisplayTopicById(String" + externalTopicId + ")");
    }
    String topicId = null;
    try
    {
      topicId = getExternalParameterByKey(externalTopicId);
      if (topicId != null)
      {
        DiscussionTopic topic = forumManager.getTopicById(topicId);
        selectedTopic = getDecoratedTopic(topic);
      }
      else
      {
        // TODO : appropriate error page
        return MAIN;
      }
    }
    catch (Exception e)
    {
      // TODO appropriate error page
      return "main";
    }
    return ALL_MESSAGES;
  }

}