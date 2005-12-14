package org.sakaiproject.tool.messageforums;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.kernel.session.ToolSession;
import org.sakaiproject.api.kernel.session.cover.SessionManager;
import org.sakaiproject.service.legacy.entity.Reference;
import org.sakaiproject.service.legacy.filepicker.FilePickerHelper;
import org.sakaiproject.api.kernel.tool.cover.ToolManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.service.legacy.authzGroup.AuthzGroup;
import org.sakaiproject.service.legacy.authzGroup.Role;
import org.sakaiproject.service.legacy.authzGroup.cover.AuthzGroupService;
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
  private static final String TEMPLATE_SETTING = "dfTemplateSettings";
  private static final String FORUM_DETAILS = "dfForumDetail";
  private static final String FORUM_SETTING = "dfForumSettings";
  private static final String FORUM_SETTING_REVISE = "dfReviseForumSettings";
  private static final String FORUM_CONFIRM_DELETE = "dfConfirmForumDelete";
  private static final String TOPIC_SETTING = "dfTopicSettings";
  private static final String TOPIC_SETTING_REVISE = "dfReviseTopicSettings";
  private static final String MESSAGE_COMPOSE = "dfCompose";
  private static final String MESSAGE_VIEW = "dfViewMessage";
  private static final String ALL_MESSAGES = "dfAllMessages";
  private static final String THREADED_VIEW = "dfThreadedView";
  
  private DiscussionForumBean selectedForum;
  private DiscussionTopicBean selectedTopic;
  private DiscussionMessageBean selectedMessage;
  
  private List templateControlPermissions; //control settings
  private List templateMessagePermissions;
  private List forumControlPermissions; //control settings
  private List forumMessagePermissions;
  private List topicControlPermissions; //control settings
  private List topicMessagePermissions;
  
  private static final String TOPIC_ID = "topicId";
  private static final String FORUM_ID = "forumId";
  private static final String MESSAGE_ID = "messageId";
  private static final String REDIRECT_PROCESS_ACTION = "redirectToProcessAction";
  
  private List forums = new ArrayList();
  
  //compose - cwen
  private MessageForumsMessageManager messageManager;
  private String composeTitle;
  private String composeBody;
  private String composeLabel;   
  //attachment
  private ArrayList attachments = new ArrayList();
  private ArrayList prepareRemoveAttach = new ArrayList();
  //private boolean attachCaneled = false;
  //private ArrayList oldAttachments = new ArrayList();
  //private List allAttachments = new ArrayList();
  
  
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
    reset();
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
   * @return
   */
  public List getTemplateControlPermissions()
  {
    templateControlPermissions=forumManager.getDefaultControlPermissions();
    return templateControlPermissions;
  }
  
  /**
   * @return
   */
  public void setTemplateControlPermissions(List templateSettings)
  {
    this.templateControlPermissions= templateSettings;
  }
  
  
  /**
   * @return Returns the templateMessagePermissions.
   */
  public List getTemplateMessagePermissions()
  {
    return forumManager.getDefaultMessagePermissions();
  }

  /**
   * @param templateMessagePermissions The templateMessagePermissions to set.
   */
  public void setTemplateMessagePermissions(List templateMessagePermissions)
  {
    this.templateMessagePermissions = templateMessagePermissions;
  }

  /**
   * @return
   */
  public String processActionSaveTemplateSettings()
  {
    LOG.debug("processActionSaveForumSettings()");
    forumManager.saveDefaultControlPermissions(templateControlPermissions); 
    //forumManager.saveDefaultMessagePermissions(templateMessagePermissions);
    return MAIN;
  }
  
  /**
   * @return
   */
  public String processActionRestoreDefaultTemplate()
  {
    templateControlPermissions=null;
    return TEMPLATE_SETTING;
  }
  
  
  
  
  /**
   * @return Returns the forumControlPermissions.
   */
  public List getForumControlPermissions()
  {
    if(selectedForum!=null)
    {
      
      forumControlPermissions=forumManager.getForumControlPermissions(selectedForum.getForum());
      
    }
    return forumControlPermissions;
  }

  /**
   * @param forumControlPermissions The forumControlPermissions to set.
   */
  public void setForumControlPermissions(List forumControlPermissions)
  {
    this.forumControlPermissions = forumControlPermissions;
  }

  /**
   * @return Returns the forumMessagePermissions.
   */
  public List getForumMessagePermissions()
  {
    if(selectedForum!=null)
    {
      forumMessagePermissions= forumManager.getForumMessagePermissions(selectedForum.getForum());
    }
      return forumControlPermissions;
        
  }

  /**
   * @param forumMessagePermissions The forumMessagePermissions to set.
   */
  public void setForumMessagePermissions(List forumMessagePermissions)
  {
    this.forumMessagePermissions = forumMessagePermissions;
  }

  /**
   * Display Individual forum
   * 
   * @return
   */
  public String processActionDisplayForum()
  {
    LOG.debug("processDisplayForum()");
    if (getDecoratedForum() == null)
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
    selectedForum=null;
    selectedForum = new DiscussionForumBean(forum);
    return FORUM_SETTING_REVISE;
  }

  /**
   * @return
   */
  public String processActionForumSettings()
  {
    LOG.debug("processForumSettings()");
    String forumId = getExternalParameterByKey(FORUM_ID);
    if ((forumId) == null)
    {
      // TODO : appropriate error page
      return MAIN;
    }
    DiscussionForum forum = forumManager.getForumById(new Long(forumId));
    //TODO:way around lazy initialization
//    List attachment=forum.getAttachments();
    selectedForum = new DiscussionForumBean(forum);
//    selectedForum.getForum().setAttachments(attachment);

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
//     DiscussionForum forum = forumManager.getForumById(forumId);
//     selectedForum = new DiscussionForumBean(forum);
    return FORUM_SETTING_REVISE; //
  }

  /**
   * @return
   */
  public String processActionSaveForumAndAddTopic()
  {
    LOG.debug("processActionSaveForumAndAddTopic()");
    // TODO : save forum
    selectedTopic=createTopic();
    if(selectedTopic==null)
    {
      //TODO : redirect to a error page
      return MAIN;
    }
    return TOPIC_SETTING_REVISE;
  }

  /**
   * @return
   */
  public String processActionSaveForumSettings()
  {
    LOG.debug("processActionSaveForumSettings()");
    if (selectedForum != null) {
        DiscussionForum forum = selectedForum.getForum();
        forumManager.saveForum(forum);
        if(forum!=null)
        {
          forumManager.saveForumControlPermissions(forum,forumControlPermissions);
//          forumManager.saveForumMessagePermissions(forum,forumMessagePermissions);
        }
    }
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
   * @return Returns the selectedTopic.
   */
  public DiscussionTopicBean getSelectedTopic()
  {
    return selectedTopic;
  }
  
  
  /**
   * @return Returns the topicControlPermissions.
   */
  public List getTopicControlPermissions()
  {  if(selectedTopic!=null)
    {
        topicControlPermissions= forumManager.getTopicControlPermissions(selectedTopic.getTopic());
    }
     
      return topicControlPermissions;
        
  }

  /**
   * @param topicControlPermissions The topicControlPermissions to set.
   */
  public void setTopicControlPermissions(List topicControlPermissions)
  {
    this.topicControlPermissions = topicControlPermissions;
  }

  /**
   * @return Returns the topicMessagePermissions.
   */
  public List getTopicMessagePermissions()
  {
    if(selectedTopic!=null)
    {
      topicMessagePermissions= forumManager.getTopicMessagePermissions(selectedTopic.getTopic());
    }
   
      return topicMessagePermissions;
        
  }

  /**
   * @param topicMessagePermissions The topicMessagePermissions to set.
   */
  public void setTopicMessagePermissions(List topicMessagePermissions)
  {
    this.topicMessagePermissions = topicMessagePermissions;
  }

  /**
   * @return
   */
  public String processActionNewTopic()
  {
    LOG.debug("processActionNewTopic()");
    selectedTopic=createTopic();
    if(selectedTopic==null)
    {
      //TODO : redirect to a error page
      return MAIN;
    }
 
    return TOPIC_SETTING_REVISE;
  }

  /**
   * @return
   */
  public String processActionReviseTopicSettings()
  {
    LOG.debug("processActionReviseTopicSettings()");
    DiscussionTopic topic=selectedTopic.getTopic();
    
    if(topic==null)
      {
      topic = forumManager     
        .getTopicById(new Long(getExternalParameterByKey(TOPIC_ID)));
      }
    if (topic == null)
    {
      return MAIN;
    }
    selectedTopic = new DiscussionTopicBean(topic);
    return TOPIC_SETTING_REVISE;
  }

  /**
   * @return
   */
  public String processActionSaveTopicAndAddTopic()
  {
    LOG.debug("processActionSaveTopicAndAddTopic()");
    // TODO : save topic
    forumManager.saveTopic(selectedTopic.getTopic());
    createTopic();
    return TOPIC_SETTING_REVISE;
  }

  /**
   * @return
   */
  public String processActionSaveTopicSettings()
  {
    LOG.debug("processActionSaveTopicSettings()");
    
    if (selectedTopic != null) {
        DiscussionTopic topic = selectedTopic.getTopic();
        if(selectedForum!=null)
        {
          topic.setBaseForum(selectedForum.getForum());
          forumManager.saveTopic(topic);
          forumManager.saveTopicControlPermissions(topic,topicControlPermissions);          
        }
    }    
    reset();
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
   * @return
   */
  public String processActionTopicSettings()
  {
    LOG.debug("processActionTopicSettings()");
    DiscussionForum forum = forumManager
    .getForumById(new Long(getExternalParameterByKey(FORUM_ID)));
    if (forum == null)
    {
      return MAIN;
    }
    DiscussionTopic topic = forumManager
        .getTopicById(new Long(getExternalParameterByKey(TOPIC_ID)));
    if (topic == null)
    {
      return MAIN;
    }
    
    selectedTopic = new DiscussionTopicBean(topic);
    selectedForum= new DiscussionForumBean(forum);
    return TOPIC_SETTING;
  }

  /**
   * @return
   */
  public String processActionToggleDisplayExtendedDescription()
  {
    LOG.debug("processActionToggleDisplayExtendedDescription()");
    String redirectTo = getExternalParameterByKey(REDIRECT_PROCESS_ACTION);
    if(redirectTo==null)
    {
      //TODO: direct me
      return MAIN;
    }
    if(redirectTo.equals("displayHome"))
    {
      return displayHomeWithExtendedTopicDescription();
    }
    if(redirectTo.equals("processActionDisplayTopic"))
    {
      if(selectedTopic.isReadFullDesciption())
      {
        selectedTopic.setReadFullDesciption(true);
      }
      else
      {
        selectedTopic.setReadFullDesciption(false);
      }
      return ALL_MESSAGES;
    }
    if(redirectTo.equals("processActionDisplayMessage"))
    {
      if(selectedTopic.isReadFullDesciption())
      {
        selectedTopic.setReadFullDesciption(true);
      }
      else
      {
        selectedTopic.setReadFullDesciption(false);
      }
      return MESSAGE_VIEW;
    }

    return MAIN;
  
  }

  /**
   * @return
   */
  public String processActionDisplayTopic()
  {
    LOG.debug("processActionDisplayTopic()");

    return displayTopicById(TOPIC_ID);
  }

  /**
   * @return
   */
  public String processActionDisplayNextTopic()
  {
    LOG.debug("processActionDisplayNextTopic()");
    return displayTopicById("nextTopicId");
  }

  /**
   * @return
   */
  public String processActionDisplayPreviousTopic()
  {
    LOG.debug("processActionDisplayNextTopic()");
    return displayTopicById("previousTopicId");
  }
  
  

  /**
   * @return Returns the selectedMessage.
   */
  public DiscussionMessageBean getSelectedMessage()
  {
    return selectedMessage;
  }

  /**
   * @return
   */
  public String processActionDisplayMessage()
  {
    LOG.debug("processActionDisplayMessage()");
    String messageId=getExternalParameterByKey("messageId");
    if(messageId==null)
    {
      //TODO: direct me 
      return MAIN;
    }
    Message message=forumManager.getMessageById(new Long(messageId));
    if(message==null)
    {
//    TODO: direct me 
      return MAIN;
    }
    selectedMessage= new DiscussionMessageBean(message);
//    TODO:remove this after real data is in there
    selectedTopic= new DiscussionTopicBean(forumManager.getTopicById(new Long(getExternalParameterByKey(TOPIC_ID))));
//    selectedTopic= new DiscussionTopicBean(message.getTopic());
    return MESSAGE_VIEW;
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
  private DiscussionForumBean getDecoratedForum()
  {
    LOG.debug("decorateSelectedForum()");
    String forumId = getExternalParameterByKey(FORUM_ID);
    if ((forumId) != null)
    {
      DiscussionForum forum = forumManager.getForumByUuid(forumId);
      if(forum==null)
      {
        return null;
      }
      selectedForum = getDecoratedForum(forum);
      return selectedForum;
    }
    return null;
  }

  
 
  /**
   * @return
   */
  private String  displayHomeWithExtendedTopicDescription()
  {
    LOG.debug("displayHome()");
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
                  && getExternalParameterByKey("topicId_displayExtended").trim().length()>0
                  && decoTopicBean.getTopic().getId().equals(new Long(
                      getExternalParameterByKey("topicId_displayExtended"))))
              {
                decoTopicBean.setReadFullDesciption(true);
              }
              // if this topic is selected to display hide extended desciption
              if (getExternalParameterByKey("topicId_hideExtended") != null
                  && getExternalParameterByKey("topicId_hideExtended").trim().length()>0
                  && decoTopicBean.getTopic().getId().equals(new Long(
                      getExternalParameterByKey("topicId_hideExtended"))))
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
    decoTopic.getTopic().setBaseForum(forumManager.getForumById(new Long(5)));
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
    if (temp_messages == null || temp_messages.size()<1)
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
  private String displayTopicById(String externalTopicId)
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
        DiscussionTopic topic = forumManager.getTopicById(new Long(topicId));
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
  
  private void reset()
  {
    selectedForum=null;
    selectedTopic=null;
    selectedMessage=null;    
  }

  /**
   * @return newly created topic
   * 
   */
  private DiscussionTopicBean createTopic()
  {
    DiscussionForum forum=forumManager.getForumById(new Long(getExternalParameterByKey(FORUM_ID)));
    
    if(forum==null)
    {
      return null;
    }
    selectedForum= new DiscussionForumBean(forum);
    DiscussionTopic topic = forumManager.createTopic(forum);
    if(topic==null)
      {
        return null;
      }
    selectedTopic= new DiscussionTopicBean(topic);
    return new DiscussionTopicBean(topic);
    
  }
  
  //compose - cwen
  public String processAddMessage()
  {
  	return MESSAGE_COMPOSE;
  }
  
  public String processAddAttachmentRedirect()
  {
    LOG.debug("processAddAttachmentRedirect()");
    try
    {
      ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
      context.redirect("sakai.filepicker.helper/tool");
      return null;
    }
    catch(Exception e)
    {
      return null;
    }
  }
  
  public void setMessageManager(MessageForumsMessageManager messageManager)
  {
    this.messageManager = messageManager;
  }
  
  public String getComposeTitle()
  {
  	return composeTitle;
  }
  
  public void setComposeTitle(String composeTitle)
  {
  	this.composeTitle = composeTitle;
  }
  
  public String getComposeBody()
  {
  	return composeBody;
  }
  
  public void setComposeBody(String composeBody)
  {
  	this.composeBody = composeBody;
  }
  
  
  public String getComposeLabel()
  {
  	return composeLabel;
  }
  
  public void setComposeLabel(String composeLabel)
  {
  	this.composeLabel = composeLabel;
  }

  public ArrayList getAttachments()
  {
    ToolSession session = SessionManager.getCurrentToolSession();
    if (session.getAttribute(FilePickerHelper.FILE_PICKER_CANCEL) == null &&
        session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS) != null) 
    {
      List refs = (List)session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
      Reference ref = (Reference)refs.get(0);
      
      for(int i=0; i<refs.size(); i++)
      {
        ref = (Reference) refs.get(i);
        Attachment thisAttach = messageManager.createAttachment();
        thisAttach.setAttachmentName(ref.getProperties().getProperty(ref.getProperties().getNamePropDisplayName()));
        thisAttach.setAttachmentSize(ref.getProperties().getProperty(ref.getProperties().getNamePropContentLength()));
        thisAttach.setAttachmentType(ref.getProperties().getProperty(ref.getProperties().getNamePropContentType()));
        thisAttach.setAttachmentId(ref.getId());
        thisAttach.setAttachmentUrl(ref.getUrl());

        attachments.add(thisAttach);
      }
    }
    session.removeAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
    session.removeAttribute(FilePickerHelper.FILE_PICKER_CANCEL);
    
    return attachments;
  }

  public void setAttachments(ArrayList attachments)
  {
    this.attachments = attachments;
  }

  public String processDeleteAttach()
  {
    LOG.debug("processDeleteAttach()");
    
    ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
    String attachId = null;
    
    Map paramMap = context.getRequestParameterMap();
    Iterator itr = paramMap.keySet().iterator();
    while(itr.hasNext())
    {
      Object key = itr.next();
      if( key instanceof String)
      {
        String name =  (String)key;
        int pos = name.lastIndexOf("dfmsg_current_attach");
        
        if(pos>=0 && name.length()==pos+"dfmsg_current_attach".length())
        {
          attachId = (String)paramMap.get(key);
          break;
        }
      }
    }
    
    if((attachId != null) && (!attachId.equals("")))
    {
      for(int i=0; i<attachments.size(); i++)
      {
      	if(attachId.equalsIgnoreCase(((Attachment)attachments.get(i)).getAttachmentId()))
      	{
      		attachments.remove(i);
      		break;
      	}
      }
    }
        
    return null ;
  }
 
  public String processDfMsgCancel()
  {
  	this.composeBody = null;
  	this.composeLabel = null;
  	this.composeTitle = null;
  	
  	this.attachments.clear();
  	
  	return ALL_MESSAGES;
  }
  
  public String processDfMsgPost()
  {
    Message dMsg=constructMessage();

    forumManager.saveMessage(dMsg);
    selectedTopic.addMessage(new DiscussionMessageBean(dMsg));
    selectedTopic.getTopic().addMessage(dMsg);
    forumManager.saveTopic(selectedTopic.getTopic());
    
    this.composeBody = null;
  	this.composeLabel = null;
  	this.composeTitle = null;
  	
  	this.attachments.clear();
    
  	return ALL_MESSAGES;    
  }
  
  public String processDfMsgSaveDraft() 
  {
    Message dMsg=constructMessage() ;
    dMsg.setDraft(Boolean.TRUE);
    
    forumManager.saveMessage(dMsg);
    selectedTopic.addMessage(new DiscussionMessageBean(dMsg));
    selectedTopic.getTopic().addMessage(dMsg);
    forumManager.saveTopic(selectedTopic.getTopic());
    
  	this.composeBody = null;
  	this.composeLabel = null;
  	this.composeTitle = null;
  	
  	this.attachments.clear();

    return ALL_MESSAGES;    
  }

  public Message constructMessage()
  {
    Message aMsg;
    
    aMsg = messageManager.createDiscussionMessage();

    if (aMsg != null)
    {
      aMsg.setTitle(getComposeTitle());
      aMsg.setBody(getComposeBody());
      aMsg.setAuthor(getUserId());
      aMsg.setDraft(Boolean.FALSE);      
      aMsg.setApproved(Boolean.TRUE);
    }
    for(int i=0; i<attachments.size(); i++)
    {
      aMsg.addAttachment((Attachment)attachments.get(i));
    }    
    attachments.clear();
    //oldAttachments.clear();
    
    return aMsg;    
  }
  
  public String processDfComposeToggle()
  {
    String redirectTo = getExternalParameterByKey(REDIRECT_PROCESS_ACTION);
    String expand = getExternalParameterByKey("composeExpand");
    
    if(redirectTo==null)
    {
      return MAIN;
    }
    if(redirectTo.equals("dfCompose"))
    {
      if((expand != null) && (expand.equalsIgnoreCase("true")))
      {
      	selectedTopic.setReadFullDesciption(true);
      }
      else
      {
        selectedTopic.setReadFullDesciption(false);
      }
      return MESSAGE_COMPOSE;
    }
    
    return MAIN;
  }
  
  public String getUserId()
  {
    return SessionManager.getCurrentSessionUserId();
  }
  
  public List getRoles()
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
          if (role != null ) roleList.add(role.getId());
        }
      }
    }
    catch (IdUnusedException e)
    {
      LOG.error(e.getMessage(), e);
    }
    Collections.sort(roleList);
    return roleList;
  }
  /**
   * @return siteId
   */
  private String getContextSiteId()
  {
    LOG.debug("getContextSiteId()");
    return ("/site/" + ToolManager.getCurrentPlacement().getContext());
  }
}