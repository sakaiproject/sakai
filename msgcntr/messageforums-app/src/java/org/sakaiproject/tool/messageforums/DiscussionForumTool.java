package org.sakaiproject.tool.messageforums;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.api.kernel.session.ToolSession;
import org.sakaiproject.api.kernel.session.cover.SessionManager;
import org.sakaiproject.api.kernel.tool.cover.ToolManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.service.legacy.authzGroup.AuthzGroup;
import org.sakaiproject.service.legacy.authzGroup.Role;
import org.sakaiproject.service.legacy.authzGroup.cover.AuthzGroupService;
import org.sakaiproject.service.legacy.entity.Reference;
import org.sakaiproject.service.legacy.filepicker.FilePickerHelper;
import org.sakaiproject.service.legacy.security.cover.SecurityService;
import org.sakaiproject.tool.messageforums.ui.DiscussionForumBean;
import org.sakaiproject.tool.messageforums.ui.DiscussionMessageBean;
import org.sakaiproject.tool.messageforums.ui.DiscussionTopicBean;

/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 * @author Chen wen
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
  private static final String TOPIC_SETTING = "dfTopicSettings";
  private static final String TOPIC_SETTING_REVISE = "dfReviseTopicSettings";
  private static final String MESSAGE_COMPOSE = "dfCompose";
  private static final String MESSAGE_VIEW = "dfViewMessage";
  private static final String ALL_MESSAGES = "dfAllMessages";
  private static final String THREADED_VIEW = "dfThreadedView";
  private static final String UNREAD_VIEW = "dfUnreadView";

  private DiscussionForumBean selectedForum;
  private DiscussionTopicBean selectedTopic;
  private DiscussionTopicBean searchResults;
  private DiscussionMessageBean selectedMessage;

  private List templateControlPermissions; // control settings
  private List templateMessagePermissions;
  private List forumControlPermissions; // control settings
  private List forumMessagePermissions;
  private List topicControlPermissions; // control settings
  private List topicMessagePermissions;

  private static final String TOPIC_ID = "topicId";
  private static final String FORUM_ID = "forumId";
  private static final String MESSAGE_ID = "messageId";
  private static final String REDIRECT_PROCESS_ACTION = "redirectToProcessAction";

  private static final String INSUFFICIENT_PRIVILEGES_TO_EDIT_TEMPLATE_SETTINGS = "Insufficient privileges to edit Template Settings";

  private List forums = new ArrayList();

  // compose
  private MessageForumsMessageManager messageManager;
  private String composeTitle;
  private String composeBody;
  private String composeLabel;
  private String searchText = "";
  private String selectedMessageView = ALL_MESSAGES;
  private boolean deleteMsg;
  private boolean displayUnreadOnly;
  // attachment
  private ArrayList attachments = new ArrayList();
  private ArrayList prepareRemoveAttach = new ArrayList();
  // private boolean attachCaneled = false;
  // private ArrayList oldAttachments = new ArrayList();
  // private List allAttachments = new ArrayList();
  private boolean threaded = false;
  private String expanded = "true";
  private boolean isDisplaySearchedMessages;
  /**
   * Dependency Injected
   */
  private DiscussionForumManager forumManager;
  private UIPermissionsManager uiPermissionsManager;

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
   * @param uiPermissionsManager
   *          The uiPermissionsManager to set.
   */
  public void setUiPermissionsManager(UIPermissionsManager uiPermissionsManager)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("setUiPermissionsManager(UIPermissionsManager "
          + uiPermissionsManager + ")");
    }
    this.uiPermissionsManager = uiPermissionsManager;
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
      forums = new ArrayList();
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
        // TODO: put this logic in database layer
        if (forum.getDraft().equals(Boolean.FALSE)||(forum.getDraft().equals(Boolean.TRUE)&& forum.getCreatedBy().equals(SessionManager.getCurrentSessionUserId()) 
            )||SecurityService.isSuperUser()
            ||isInstructor()
            ||forum.getCreatedBy().equals(
            SessionManager.getCurrentSessionUserId()))
        { 
          DiscussionForumBean decoForum = getDecoratedForum(forum);
          forums.add(decoForum);
        }        
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
    if(!isInstructor())
    {
      setErrorMessage(INSUFFICIENT_PRIVILEGES_TO_EDIT_TEMPLATE_SETTINGS);
      return MAIN;
    }
    return TEMPLATE_SETTING;
  }

  /**
   * @return
   */
  public List getTemplateControlPermissions()
  {
    if (templateControlPermissions == null)
    {
      templateControlPermissions = forumManager.getAreaControlPermissions();
    }
    return templateControlPermissions;
  }

  /**
   * @return
   */
  public void setTemplateControlPermissions(List templateSettings)
  {
    this.templateControlPermissions = templateSettings;
  }

  /**
   * @return Returns the templateMessagePermissions.
   */
  public List getTemplateMessagePermissions()
  {
    if (templateMessagePermissions == null)
    {
      templateMessagePermissions = forumManager.getAreaMessagePermissions();
    }
    return templateMessagePermissions;
  }

  /**
   * @param templateMessagePermissions
   *          The templateMessagePermissions to set.
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
    if(!isInstructor())
    {
      setErrorMessage(INSUFFICIENT_PRIVILEGES_TO_EDIT_TEMPLATE_SETTINGS);
      return MAIN;
    }
    forumManager.saveAreaControlPermissions(templateControlPermissions);
    forumManager.saveAreaMessagePermissions(templateMessagePermissions);
    return MAIN;
  }

  /**
   * @return
   */
  public String processActionRestoreDefaultTemplate()
  {
    LOG.debug("processActionRestoreDefaultTemplate()");
    if(!isInstructor())
    {
      setErrorMessage(INSUFFICIENT_PRIVILEGES_TO_EDIT_TEMPLATE_SETTINGS);
      return MAIN;
    }
    templateControlPermissions = forumManager.getDefaultControlPermissions();
    templateMessagePermissions = forumManager.getDefaultMessagePermissions();
    return TEMPLATE_SETTING;
  }

  /**
   * @return Returns the forumControlPermissions.
   */
  public List getForumControlPermissions()
  {
    if (selectedForum != null)
    {
      forumControlPermissions = forumManager
          .getForumControlPermissions(selectedForum.getForum());
    }
    return forumControlPermissions;
  }

  /**
   * @param forumControlPermissions
   *          The forumControlPermissions to set.
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
    if (selectedForum != null)
    {
      forumMessagePermissions = forumManager
          .getForumMessagePermissions(selectedForum.getForum());
    }
    return forumMessagePermissions;

  }

  /**
   * @param forumMessagePermissions
   *          The forumMessagePermissions to set.
   */
  public void setForumMessagePermissions(List forumMessagePermissions)
  {
    this.forumMessagePermissions = forumMessagePermissions;
  }

  /**
   * Check out if the user is allowed to create new forum
   * 
   * @return
   */
  public boolean getNewForum()
  {
    LOG.debug("getNewForum()");
    return uiPermissionsManager.isNewForum();
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
      LOG.error("Forum not found");
      return MAIN;
    }
    return FORUM_DETAILS;
  }

  /**
   * Forward to delete forum confirmation screen
   * 
   * @return
   */
  public String processActionDeleteForumConfirm()
  {
    LOG.debug("processActionDeleteForumConfirm()");
    if (selectedForum == null)
    {
      LOG.debug("There is no forum selected for deletion");
      return MAIN;
    }
//  TODO:
    if(!uiPermissionsManager.isChangeSettings(selectedForum.getForum()))
    {
      setErrorMessage("Insufficient privileges to delete this forum");
      return MAIN;
    }
    selectedForum.setMarkForDeletion(true);
    return FORUM_SETTING;
  }

  /**
   * @return
   */
  public String processActionDeleteForum()
  {
    if (selectedForum == null)
    {
      LOG.debug("There is no forum selected for deletion");
    }
    if(!uiPermissionsManager.isChangeSettings(selectedForum.getForum()))
    {
      setErrorMessage("Insufficient privileges to");
      return MAIN;
    }
    forumManager.deleteForum(selectedForum.getForum());
    reset();
    return MAIN;
  }

  /**
   * @return
   */
  public String processActionNewForum()
  {
    LOG.debug("processActionNewForum()");
    if (getNewForum())
    {
      DiscussionForum forum = forumManager.createForum();
      selectedForum = null;
      selectedForum = new DiscussionForumBean(forum, uiPermissionsManager);
      return FORUM_SETTING_REVISE;
    }
    else
    {
      setErrorMessage("User is not allowed to create a new forum");
      return MAIN;
    }
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
      setErrorMessage("Invalid forum selected");
      return MAIN;
    }
    if(!uiPermissionsManager.isChangeSettings(selectedForum.getForum()))
    {
      setErrorMessage("Insufficient privileges to change forum settings");
      return MAIN;
    }
    DiscussionForum forum = forumManager.getForumById(new Long(forumId));
    selectedForum = new DiscussionForumBean(forum, uiPermissionsManager);
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
      setErrorMessage("Forum not found");
      return MAIN;
    }
    if(!uiPermissionsManager.isChangeSettings(selectedForum.getForum()))
    {
      setErrorMessage("Insufficient privileges to change forum settings");
      return MAIN;
    }
    List attachList = selectedForum.getForum().getAttachments();
    if (attachList != null)
    {
      for (int i = 0; i < attachList.size(); i++)
      {
        attachments.add((Attachment) attachList.get(i));
      }
    }

    return FORUM_SETTING_REVISE; //
  }

  /**
   * @return
   */
  public String processActionSaveForumAndAddTopic()
  {
    LOG.debug("processActionSaveForumAndAddTopic()");
    if(selectedForum!=null && selectedForum.getForum()!=null && 
        (selectedForum.getForum().getTitle()==null 
          ||selectedForum.getForum().getTitle().trim().length()<1  ))
    {
      setErrorMessage("Please enter a valid forum title");
      return FORUM_SETTING_REVISE;
    }
    if(!uiPermissionsManager.isChangeSettings(selectedForum.getForum()))
    {
      setErrorMessage("Insufficient privileges to change forum settings");
      return MAIN;
    }
    if(!uiPermissionsManager.isNewTopic(selectedForum.getForum()))
    {
      setErrorMessage("Insufficient privileges to create new topic");
      return MAIN;
    }
    DiscussionForum forum = saveForumSettings(false);
    selectedTopic = createTopic(forum.getId());
    if (selectedTopic == null)
    {
      setErrorMessage("Create New Topic Failed!");
      attachments.clear();
      prepareRemoveAttach.clear();
      return MAIN;
    }
    attachments.clear();
    prepareRemoveAttach.clear();
    return TOPIC_SETTING_REVISE;
  }

  /**
   * @return
   */
  public String processActionSaveForumSettings()
  {
    LOG.debug("processActionSaveForumSettings()");
    if(!uiPermissionsManager.isChangeSettings(selectedForum.getForum()))
    {
      setErrorMessage("Insufficient privileges to change forum settings");
      return MAIN;
    }
    if(selectedForum!=null && selectedForum.getForum()!=null && 
        (selectedForum.getForum().getTitle()==null 
          ||selectedForum.getForum().getTitle().trim().length()<1  ))
    {
      setErrorMessage("Please enter a valid forum title");
      return FORUM_SETTING_REVISE;
    }
    saveForumSettings(false);
    reset();
    return MAIN;
  }

  /**
   * @return
   */
  public String processActionSaveForumAsDraft()
  {
    LOG.debug("processActionSaveForumAsDraft()");
    if(!uiPermissionsManager.isChangeSettings(selectedForum.getForum()))
    {
      setErrorMessage("Insufficient privileges to change forum settings");
      return MAIN;
    }
    if(selectedForum!=null && selectedForum.getForum()!=null && 
        (selectedForum.getForum().getTitle()==null 
          ||selectedForum.getForum().getTitle().trim().length()<1  ))
    {
      setErrorMessage("Please enter a valid forum title");
      return FORUM_SETTING_REVISE;
    }
    saveForumSettings(true);
    reset();
    return MAIN;
  }

  private DiscussionForum saveForumSettings(boolean draft)
  {
    LOG.debug("saveForumSettings(boolean " + draft + ")");
    
    if (selectedForum == null)
    {
      setErrorMessage("Selected Forum not found");
      return null;
    }
  
    DiscussionForum forum = selectedForum.getForum();
    if (forum == null)
    {
      setErrorMessage("Forum not found");
      return null;
    }
    
    saveForumAttach(forum);
    if (draft)
      forumManager.saveForumAsDraft(forum);
    else
      forumManager.saveForum(forum);
    forumManager.saveForumControlPermissions(forum, forumControlPermissions);
    forumManager.saveForumMessagePermissions(forum, forumMessagePermissions);
    if (forum.getId() == null)
    {
      String forumUuid = forum.getUuid();
      forum = null;
      forum = forumManager.getForumByUuid(forumUuid);
    }
    return forum;
  }

  /**
   * @return Returns the selectedTopic.
   */
  public DiscussionTopicBean getSelectedTopic()
  {
 
  	if(threaded)
  	{
  	  rearrageTopicMsgsThreaded();
  	}
  	setMessageBeanPreNextStatus();
  	
 
    return selectedTopic;
  }

  /**
   * @return Returns the topicControlPermissions.
   */
  public List getTopicControlPermissions()
  {
    if (selectedTopic != null)
    {
      topicControlPermissions = forumManager
          .getTopicControlPermissions(selectedTopic.getTopic());
    }

    return topicControlPermissions;

  }

  /**
   * @param topicControlPermissions
   *          The topicControlPermissions to set.
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
    if (selectedTopic != null)
    {
      topicMessagePermissions = forumManager
          .getTopicMessagePermissions(selectedTopic.getTopic());
    }

    return topicMessagePermissions;

  }

  /**
   * @param topicMessagePermissions
   *          The topicMessagePermissions to set.
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
    selectedTopic = createTopic();
    if (selectedTopic == null)
    {
      setErrorMessage("Create New Topic Failed!");
      attachments.clear();
      prepareRemoveAttach.clear();
      return MAIN;
    }
    if(!uiPermissionsManager.isNewTopic(selectedForum.getForum()))
    {
      setErrorMessage("Insufficient privileges to create new topic");
      return MAIN;
    }
    attachments.clear();
    prepareRemoveAttach.clear();
    return TOPIC_SETTING_REVISE;
  }

  /**
   * @return
   */
  public String processActionReviseTopicSettings()
  {
    LOG.debug("processActionReviseTopicSettings()");
    DiscussionTopic topic = selectedTopic.getTopic();

    if (topic == null)
    {
      topic = forumManager.getTopicById(new Long(
          getExternalParameterByKey(TOPIC_ID)));
    }
    if (topic == null)
    {
      setErrorMessage("Topic with id '" + getExternalParameterByKey(TOPIC_ID)
          + "'not found");
      return MAIN;
    }
    if(!uiPermissionsManager.isChangeSettings(selectedTopic.getTopic(),selectedForum.getForum()))
    {
      setErrorMessage("Insufficient privileges to change topic settings");
      return MAIN;
    }
    setSelectedForumForCurrentTopic(topic);
    selectedTopic = new DiscussionTopicBean(topic, selectedForum.getForum(),
        uiPermissionsManager);

    List attachList = selectedTopic.getTopic().getAttachments();
    if (attachList != null)
    {
      for (int i = 0; i < attachList.size(); i++)
      {
        attachments.add((Attachment) attachList.get(i));
      }
    }    
    return TOPIC_SETTING_REVISE;
  }

  /**
   * @return
   */
  public String processActionSaveTopicAndAddTopic()
  {
    LOG.debug("processActionSaveTopicAndAddTopic()");
    if(selectedTopic!=null && selectedTopic.getTopic()!=null && 
        (selectedTopic.getTopic().getTitle()==null 
          ||selectedTopic.getTopic().getTitle().trim().length()<1  ))
    {
      setErrorMessage("Please enter a valid topic title");
      return TOPIC_SETTING_REVISE;
    }
    saveTopicSettings(false);
    Long forumId = selectedForum.getForum().getId();
    if (forumId == null)
    {
      setErrorMessage("Parent Forum not found");
      return MAIN;
    }
    selectedTopic = null;
    selectedTopic = createTopic(forumId);
    if (selectedTopic == null)
    {
      setErrorMessage("Create New Topic Failed!");
      attachments.clear();
      prepareRemoveAttach.clear();
      return MAIN;
    }
    attachments.clear();
    prepareRemoveAttach.clear();
    return TOPIC_SETTING_REVISE;

  }

  /**
   * @return
   */
  public String processActionSaveTopicSettings()
  {
    LOG.debug("processActionSaveTopicSettings()");
    if(selectedTopic!=null && selectedTopic.getTopic()!=null && 
        (selectedTopic.getTopic().getTitle()==null 
          ||selectedTopic.getTopic().getTitle().trim().length()<1  ))
    {
      setErrorMessage("Please enter a valid topic title");
      return TOPIC_SETTING_REVISE;
    }
    saveTopicSettings(false);
    reset();
    return MAIN;
  }

  /**
   * @return
   */
  public String processActionSaveTopicAsDraft()
  {
    LOG.debug("processActionSaveTopicAsDraft()");
    if(selectedTopic!=null && selectedTopic.getTopic()!=null && 
        (selectedTopic.getTopic().getTitle()==null 
          ||selectedTopic.getTopic().getTitle().trim().length()<1  ))
    {
      setErrorMessage("Please enter a valid topic title");
      return TOPIC_SETTING_REVISE;
    }
    if(!uiPermissionsManager.isChangeSettings(selectedTopic.getTopic(),selectedForum.getForum()))
    {
      setErrorMessage("Insufficient privileges to change topic settings");
      return MAIN;
    }
    saveTopicSettings(true);
    reset();
    return MAIN;
  }

  private String saveTopicSettings(boolean draft)
  {
    if (selectedTopic != null)
    {
      DiscussionTopic topic = selectedTopic.getTopic();
      if (selectedForum != null)
      {
        topic.setBaseForum(selectedForum.getForum());
        saveTopicAttach(topic);
        if (draft)
        {
          forumManager.saveTopicAsDraft(topic);
        }
        else
        {
          forumManager.saveTopic(topic);
        }
        forumManager
            .saveTopicControlPermissions(topic, topicControlPermissions);
        forumManager
            .saveTopicMessagePermissions(topic, topicMessagePermissions);
      }
    }
    return MAIN;
  }

  /**
   * @return
   */
  public String processActionDeleteTopicConfirm()
  {
    LOG.debug("processActionDeleteTopicConfirm()");
    
    if (selectedTopic == null)
    {
      LOG.debug("There is no topic selected for deletion");
      return MAIN;
    }
    if(!uiPermissionsManager.isChangeSettings(selectedTopic.getTopic(),selectedForum.getForum()))
    {
      setErrorMessage("Insufficient privileges to change topic settings");
      return MAIN;
    }
    selectedTopic.setMarkForDeletion(true);
    return TOPIC_SETTING;
  }

  /**
   * @return
   */
  public String processActionDeleteTopic()
  {   
    LOG.debug("processActionDeleteTopic()");
    if (selectedTopic == null)
    {
      LOG.debug("There is no topic selected for deletion");
    }
    if(!uiPermissionsManager.isChangeSettings(selectedTopic.getTopic(),selectedForum.getForum()))
    {
      setErrorMessage("Insufficient privileges to change topic settings");
      return MAIN;
    }
    forumManager.deleteTopic(selectedTopic.getTopic());
    reset();
    return MAIN;
  }

  /**
   * @return
   */
  public String processActionTopicSettings()
  {
    LOG.debug("processActionTopicSettings()");
    DiscussionTopic topic = (DiscussionTopic) forumManager
        .getTopicByIdWithAttachments(new Long(
            getExternalParameterByKey(TOPIC_ID)));
    if (topic == null)
    {
      return MAIN;
    }
    setSelectedForumForCurrentTopic(topic);
    if(!uiPermissionsManager.isChangeSettings(selectedTopic.getTopic(),selectedForum.getForum()))
    {
      setErrorMessage("Insufficient privileges to change topic settings");
      return MAIN;
    }
    selectedTopic = new DiscussionTopicBean(topic, selectedForum.getForum(),
        uiPermissionsManager);
    
    return TOPIC_SETTING;
  }

  public String processActionToggleDisplayForumExtendedDescription()
  {
    LOG.debug("processActionToggleDisplayForumExtendedDescription()");
    String redirectTo = getExternalParameterByKey(REDIRECT_PROCESS_ACTION);
    if (redirectTo == null)
    {
      setErrorMessage("Could not find a redirect page : Read / Hide full descriptions");
      return MAIN;
    }
  
    if (redirectTo.equals("displayHome"))
    {
      displayHomeWithExtendedForumDescription();
      return MAIN;
    }
    if (redirectTo.equals("processActionDisplayForum"))
    {
      if (selectedForum.isReadFullDesciption())
      {
        selectedForum.setReadFullDesciption(false);
      }
      else
      {
        selectedForum.setReadFullDesciption(true);
      }  
       return FORUM_DETAILS;
    }
    return MAIN;
  }
  /**
   * @return
   */
  public String processActionToggleDisplayExtendedDescription()
  {
    LOG.debug("processActionToggleDisplayExtendedDescription()");
    String redirectTo = getExternalParameterByKey(REDIRECT_PROCESS_ACTION);
    if (redirectTo == null)
    {
      setErrorMessage("Could not find a redirect page : Read / Hide full descriptions");
      return MAIN;
    }
    if (redirectTo.equals("displayHome"))
    {
      return displayHomeWithExtendedTopicDescription();
    }
    if (redirectTo.equals("processActionDisplayTopic"))
    {
      if (selectedTopic.isReadFullDesciption())
      {
        selectedTopic.setReadFullDesciption(false);
      }
      else
      {
        selectedTopic.setReadFullDesciption(true);
      }
      return ALL_MESSAGES;
    }
    if (redirectTo.equals("processActionDisplayMessage"))
    {
      if (selectedTopic.isReadFullDesciption())
      {
        selectedTopic.setReadFullDesciption(false);
      }
      else
      {
        selectedTopic.setReadFullDesciption(true);
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

    String messageId = getExternalParameterByKey(MESSAGE_ID);
    String topicId = getExternalParameterByKey(TOPIC_ID);
    if (messageId == null)
    {
      setErrorMessage("Message reference not found");
      return MAIN;
    }
    if (topicId == null)
    {
      setErrorMessage("Topic reference not found for the message");
      return MAIN;
    }
    // Message message=forumManager.getMessageById(new Long(messageId));
    Message message = messageManager.getMessageByIdWithAttachments(new Long(
        messageId));
    messageManager.markMessageReadForUser(new Long(topicId),
        new Long(messageId), true);
    if (message == null)
    {
      setErrorMessage("Message with id '" + messageId + "'not found");
      return MAIN;
    }
    message = messageManager.getMessageByIdWithAttachments(message.getId());
    selectedMessage = new DiscussionMessageBean(message, messageManager);
    // TODO:remove this after real data is in there
    DiscussionTopic topic=forumManager.getTopicById(new Long(
        getExternalParameterByKey(TOPIC_ID)));
    setSelectedForumForCurrentTopic(topic);
    selectedTopic = new DiscussionTopicBean(topic, selectedForum.getForum(),
        uiPermissionsManager);
    String currentForumId = getExternalParameterByKey(FORUM_ID);
    if (currentForumId != null && (!currentForumId.trim().equals(""))
        && (!currentForumId.trim().equals("null")))
    {
      DiscussionForum forum = forumManager
          .getForumById(new Long(currentForumId));
      selectedForum = getDecoratedForum(forum);
      selectedTopic.getTopic().setBaseForum(forum);
    }
    selectedTopic = getDecoratedTopic(forumManager.getTopicById(new Long(
        getExternalParameterByKey(TOPIC_ID))));
    getSelectedTopic();
    List tempMsgs = selectedTopic.getMessages();
    if(tempMsgs != null)
    {
    	for(int i=0; i<tempMsgs.size(); i++)
    	{
    		DiscussionMessageBean thisDmb = (DiscussionMessageBean)tempMsgs.get(i);
    		if(((DiscussionMessageBean)tempMsgs.get(i)).getMessage().getId().toString().equals(messageId))
    		{
    			selectedMessage.setDepth(thisDmb.getDepth());
    			selectedMessage.setHasNext(thisDmb.getHasNext());
    			selectedMessage.setHasPre(thisDmb.getHasPre());
    			break;
    		}
    	}
    }
    // selectedTopic= new DiscussionTopicBean(message.getTopic());
    return MESSAGE_VIEW;
  }
  
  public String processDisplayPreviousMsg()
  {
  	List tempMsgs = selectedTopic.getMessages();
  	int currentMsgPosition = -1;
    if(tempMsgs != null)
    {
    	for(int i=0; i<tempMsgs.size(); i++)
    	{
    		DiscussionMessageBean thisDmb = (DiscussionMessageBean)tempMsgs.get(i);
    		if(selectedMessage.getMessage().getId().equals(thisDmb.getMessage().getId()))
    		{
    			currentMsgPosition = i;
    			break;
    		}
    	}
    }
    
    if(currentMsgPosition > 0)
    {
    	DiscussionMessageBean thisDmb = (DiscussionMessageBean)tempMsgs.get(currentMsgPosition-1);
    	Message message = messageManager.getMessageByIdWithAttachments(thisDmb.getMessage().getId());
      selectedMessage = new DiscussionMessageBean(message, messageManager);
			selectedMessage.setDepth(thisDmb.getDepth());
			selectedMessage.setHasNext(thisDmb.getHasNext());
			selectedMessage.setHasPre(thisDmb.getHasPre());
			
	    messageManager.markMessageReadForUser(selectedTopic.getTopic().getId(),
	        selectedMessage.getMessage().getId(), true);
    }
    
    return null;
  }

  public String processDfDisplayNextMsg()
  {
  	List tempMsgs = selectedTopic.getMessages();
  	int currentMsgPosition = -1;
    if(tempMsgs != null)
    {
    	for(int i=0; i<tempMsgs.size(); i++)
    	{
    		DiscussionMessageBean thisDmb = (DiscussionMessageBean)tempMsgs.get(i);
    		if(selectedMessage.getMessage().getId().equals(thisDmb.getMessage().getId()))
    		{
    			currentMsgPosition = i;
    			break;
    		}
    	}
    }
    
    if(currentMsgPosition > -2  && currentMsgPosition < (tempMsgs.size()-1))
    {
    	DiscussionMessageBean thisDmb = (DiscussionMessageBean)tempMsgs.get(currentMsgPosition+1);
    	Message message = messageManager.getMessageByIdWithAttachments(thisDmb.getMessage().getId());
      selectedMessage = new DiscussionMessageBean(message, messageManager);
			selectedMessage.setDepth(thisDmb.getDepth());
			selectedMessage.setHasNext(thisDmb.getHasNext());
			selectedMessage.setHasPre(thisDmb.getHasPre());
			
	    messageManager.markMessageReadForUser(selectedTopic.getTopic().getId(),
	        selectedMessage.getMessage().getId(), true);
    }
    
    return null;
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
    forum = forumManager.getForumByIdWithTopics(forum.getId());
    DiscussionForumBean decoForum = new DiscussionForumBean(forum,
        uiPermissionsManager);
    List temp_topics = forum.getTopics();
    if (temp_topics == null)
    {
      return decoForum;
    }
    Iterator iter = temp_topics.iterator();
    while (iter.hasNext())
    {
      DiscussionTopic topic = (DiscussionTopic) iter.next();
//    TODO: put this logic in database layer
      if (topic.getDraft().equals(Boolean.FALSE)||
          (topic.getDraft().equals(Boolean.TRUE)&&topic.getCreatedBy().equals(SessionManager.getCurrentSessionUserId()))
          ||isInstructor()
          ||SecurityService.isSuperUser()||topic.getCreatedBy().equals(
          SessionManager.getCurrentSessionUserId()))
      { 
        topic = (DiscussionTopic) forumManager.getTopicByIdWithAttachments(topic
            .getId());
        if (topic != null)
        {
          DiscussionTopicBean decoTopic = new DiscussionTopicBean(topic, forum,
              uiPermissionsManager);
          decoTopic.setTotalNoMessages(forumManager.getTotalNoMessages(topic));
          decoTopic.setUnreadNoMessages(forumManager.getUnreadNoMessages(topic));
          decoForum.addTopic(decoTopic);
        }
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
      DiscussionForum forum = forumManager.getForumById(new Long(forumId));
      if (forum == null)
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
  private String displayHomeWithExtendedForumDescription()
  {
    LOG.debug("displayHomeWithExtendedForumDescription()");
    List tmpForums = getForums();
    if (tmpForums != null)
    {
      Iterator iter = tmpForums.iterator();
      while (iter.hasNext())
      {
        DiscussionForumBean decoForumBean = (DiscussionForumBean) iter.next();
        if (decoForumBean != null)
        {
          // if this forum is selected to display full desciption
              if (getExternalParameterByKey("forumId_displayExtended") != null
                  && getExternalParameterByKey("forumId_displayExtended")
                      .trim().length() > 0
                  && decoForumBean
                      .getForum()
                      .getId()
                      .equals(
                          new Long(
                              getExternalParameterByKey("forumId_displayExtended"))))
              {
                decoForumBean.setReadFullDesciption(true);
              }
              // if this topic is selected to display hide extended desciption
              if (getExternalParameterByKey("forumId_hideExtended") != null
                  && getExternalParameterByKey("forumId_hideExtended").trim()
                      .length() > 0
                  && decoForumBean.getForum().getId().equals(
                      new Long(
                          getExternalParameterByKey("forumId_hideExtended"))))
              {
                decoForumBean.setReadFullDesciption(false);
              }
             
          
        }
      }

    }
    return MAIN;
  }
  
  /**
   * @return
   */
  private String displayHomeWithExtendedTopicDescription()
  {
    LOG.debug("displayHomeWithExtendedTopicDescription()");
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
                  && getExternalParameterByKey("topicId_displayExtended")
                      .trim().length() > 0
                  && decoTopicBean
                      .getTopic()
                      .getId()
                      .equals(
                          new Long(
                              getExternalParameterByKey("topicId_displayExtended"))))
              {
                decoTopicBean.setReadFullDesciption(true);
              }
              // if this topic is selected to display hide extended desciption
              if (getExternalParameterByKey("topicId_hideExtended") != null
                  && getExternalParameterByKey("topicId_hideExtended").trim()
                      .length() > 0
                  && decoTopicBean.getTopic().getId().equals(
                      new Long(
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
    DiscussionTopicBean decoTopic = new DiscussionTopicBean(topic,
        selectedForum.getForum(), uiPermissionsManager);
    decoTopic.setTotalNoMessages(forumManager.getTotalNoMessages(topic));
    decoTopic.setUnreadNoMessages(forumManager.getUnreadNoMessages(topic));
    decoTopic.setHasNextTopic(forumManager.hasNextTopic(topic));
    decoTopic.setHasPreviousTopic(forumManager.hasPreviousTopic(topic));
    if (forumManager.hasNextTopic(topic))
    {
      DiscussionTopic nextTopic= forumManager.getNextTopic(topic);
        
              decoTopic.setNextTopicId(nextTopic.getId());
             
    }
    if (forumManager.hasPreviousTopic(topic))
    {
      DiscussionTopic previousTopic= forumManager.getPreviousTopic(topic);
      
        decoTopic
          .setPreviousTopicId(forumManager.getPreviousTopic(topic).getId());
       
    }

    List temp_messages = forumManager.getTopicByIdWithMessages(topic.getId())
        .getMessages();
    if (temp_messages == null || temp_messages.size() < 1)
    {
      return decoTopic;
    }
    Iterator iter = temp_messages.iterator();
    while (iter.hasNext())
    {
      Message message = (Message) iter.next();
      if (topic != null)
      {
        decoTopic.setTotalNoMessages(forumManager.getTotalNoMessages(topic));
        decoTopic.setUnreadNoMessages(forumManager.getUnreadNoMessages(topic));
        if (message != null)
        {
          DiscussionMessageBean decoMsg = new DiscussionMessageBean(message,
              messageManager);
          if(decoTopic.getIsRead())
          {
          	decoMsg.setRead(messageManager.isMessageReadForUser(topic.getId(),
              message.getId()));
          	decoTopic.addMessage(decoMsg);
          }
        }

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

    selectedTopic = null;
    try
    {
      topicId = getExternalParameterByKey(externalTopicId);

      if (topicId != null)
      {
        DiscussionTopic topic = null;
        try
        {
          Long.parseLong(topicId);
          topic = forumManager.getTopicById(new Long(topicId));
        }
        catch (NumberFormatException e)
        {
          LOG.error(e.getMessage(), e);
          setErrorMessage("Unable to retrieve topic");
          return MAIN;
        }

        setSelectedForumForCurrentTopic(topic);
        selectedTopic = getDecoratedTopic(topic);
      }
      else
      {
        LOG.error("Topic with id '" + externalTopicId + "' not found");
        setErrorMessage("Topic with id '" + externalTopicId + "' not found");
        return MAIN;
      }
    }
    catch (Exception e)
    {
      LOG.error(e.getMessage(), e);
      setErrorMessage(e.getMessage());
      return "main";
    }
    return ALL_MESSAGES;
  }

  private void reset()
  {
    this.forums = null;
    this.selectedForum = null;
    this.selectedTopic = null;
    this.selectedMessage = null;
    this.templateControlPermissions = null;
    this.templateMessagePermissions = null;
    attachments.clear();
    prepareRemoveAttach.clear();
  
  }

  /**
   * @return newly created topic
   */
  private DiscussionTopicBean createTopic()
  {
    String forumId = getExternalParameterByKey(FORUM_ID);
    if (forumId == null)
    {
      setErrorMessage("Parent Forum for new topic was not found");
      return null;
    }
    return createTopic(new Long(forumId));
  }

  /**
   * @param forumID
   * @return
   */
  private DiscussionTopicBean createTopic(Long forumId)
  {
    if (forumId == null)
    {
      setErrorMessage("Parent Forum for new topic was not found");
      return null;
    }
    DiscussionForum forum = forumManager.getForumById(forumId);
    if (forum == null)
    {
      setErrorMessage("Parent Forum for new topic was not found");
      return null;
    }
    selectedForum = new DiscussionForumBean(forum, uiPermissionsManager);
    DiscussionTopic topic = forumManager.createTopic(forum);
    if (topic == null)
    {
      setErrorMessage("Failed to create new topic");
      return null;
    }
    //topic.setTitle("");
    //topic.setShortDescription("");
    //topic.setExtendedDescription("");
    selectedTopic = new DiscussionTopicBean(topic, forum, uiPermissionsManager);
    return new DiscussionTopicBean(topic, forum, uiPermissionsManager);
  }

  // compose
  public String processAddMessage()
  {
    return MESSAGE_COMPOSE;
  }

  public String processAddAttachmentRedirect()
  {
    LOG.debug("processAddAttachmentRedirect()");
    try
    {
      ExternalContext context = FacesContext.getCurrentInstance()
          .getExternalContext();
      context.redirect("sakai.filepicker.helper/tool");
      return null;
    }
    catch (Exception e)
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
    if (session.getAttribute(FilePickerHelper.FILE_PICKER_CANCEL) == null
        && session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS) != null)
    {
      List refs = (List) session
          .getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
      if(refs != null && refs.size()>0)
      {
      	Reference ref = (Reference) refs.get(0);
      	
      	for (int i = 0; i < refs.size(); i++)
      	{
      		ref = (Reference) refs.get(i);
      		Attachment thisAttach = messageManager.createAttachment();
      		thisAttach.setAttachmentName(ref.getProperties().getProperty(
      				ref.getProperties().getNamePropDisplayName()));
      		thisAttach.setAttachmentSize(ref.getProperties().getProperty(
      				ref.getProperties().getNamePropContentLength()));
      		thisAttach.setAttachmentType(ref.getProperties().getProperty(
      				ref.getProperties().getNamePropContentType()));
      		thisAttach.setAttachmentId(ref.getId());
      		thisAttach.setAttachmentUrl(ref.getUrl());
      		
      		attachments.add(thisAttach);
      	}
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

    ExternalContext context = FacesContext.getCurrentInstance()
        .getExternalContext();
    String attachId = null;

    Map paramMap = context.getRequestParameterMap();
    Iterator itr = paramMap.keySet().iterator();
    while (itr.hasNext())
    {
      Object key = itr.next();
      if (key instanceof String)
      {
        String name = (String) key;
        int pos = name.lastIndexOf("dfmsg_current_attach");

        if (pos >= 0 && name.length() == pos + "dfmsg_current_attach".length())
        {
          attachId = (String) paramMap.get(key);
          break;
        }
      }
    }

    if ((attachId != null) && (!attachId.equals("")))
    {
      for (int i = 0; i < attachments.size(); i++)
      {
        if (attachId.equalsIgnoreCase(((Attachment) attachments.get(i))
            .getAttachmentId()))
        {
          attachments.remove(i);
          break;
        }
      }
    }

    return null;
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
    Message dMsg = constructMessage();

    forumManager.saveMessage(dMsg);
    setSelectedForumForCurrentTopic((DiscussionTopic) forumManager
        .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));
    selectedTopic.setTopic((DiscussionTopic) forumManager
        .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));
    selectedTopic.getTopic().setBaseForum(selectedForum.getForum());
    //selectedTopic.addMessage(new DiscussionMessageBean(dMsg, messageManager));
    selectedTopic.insertMessage(new DiscussionMessageBean(dMsg, messageManager));

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
    Message dMsg = constructMessage();
    dMsg.setDraft(Boolean.TRUE);

    forumManager.saveMessage(dMsg);
    setSelectedForumForCurrentTopic((DiscussionTopic) forumManager
        .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));
    selectedTopic.setTopic((DiscussionTopic) forumManager
        .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));
    selectedTopic.getTopic().setBaseForum(selectedForum.getForum());
    //selectedTopic.addMessage(new DiscussionMessageBean(dMsg, messageManager));
    selectedTopic.insertMessage(new DiscussionMessageBean(dMsg, messageManager));
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
      aMsg.setTopic(selectedTopic.getTopic());
    }
    for (int i = 0; i < attachments.size(); i++)
    {
      aMsg.addAttachment((Attachment) attachments.get(i));
    }
    attachments.clear();
    // oldAttachments.clear();

    return aMsg;
  }

  public String processDfComposeToggle()
  {
    String redirectTo = getExternalParameterByKey(REDIRECT_PROCESS_ACTION);
    String expand = getExternalParameterByKey("composeExpand");

    if (redirectTo == null)
    {
      return MAIN;
    }
    if (redirectTo.equals("dfCompose"))
    {
      if ((expand != null) && (expand.equalsIgnoreCase("true")))
      {
        selectedTopic.setReadFullDesciption(true);
      }
      else
      {
        selectedTopic.setReadFullDesciption(false);
      }
      return MESSAGE_COMPOSE;
    }
    if (redirectTo.equals("dfViewMessage"))
    {
      if ((expand != null) && (expand.equalsIgnoreCase("true")))
      {
        selectedTopic.setReadFullDesciption(true);
      }
      else
      {
        selectedTopic.setReadFullDesciption(false);
      }
      return MESSAGE_VIEW;
    }
    if (redirectTo.equals("dfTopicReply"))
    {
      if ((expand != null) && (expand.equalsIgnoreCase("true")))
      {
        selectedTopic.setReadFullDesciption(true);
      }
      else
      {
        selectedTopic.setReadFullDesciption(false);
      }
      return "dfTopicReply";
    }

    return MAIN;
  }

  public String getUserId()
  {
    return SessionManager.getCurrentSessionUserId();
  }

  public boolean getFullAccess()
  {
    return forumManager.isInstructor();
  }

  public String processDfMsgReplyMsg()
  {
  	this.composeTitle = "Re: " + selectedMessage.getMessage().getTitle() + " "; 
  	
    return "dfMessageReply";
  }

  public String processDfMsgReplyTp()
  {
    return "dfTopicReply";
  }

  public String processDfMsgGrd()
  {
    return null;
  }

  public String processDfMsgRvs()
  {
    attachments.clear();

    composeBody = selectedMessage.getMessage().getBody();
    composeLabel = selectedMessage.getMessage().getLabel();
    composeTitle = selectedMessage.getMessage().getTitle();
    List attachList = selectedMessage.getMessage().getAttachments();
    if (attachList != null)
    {
      for (int i = 0; i < attachList.size(); i++)
      {
        attachments.add((Attachment) attachList.get(i));
      }
    }

    return "dfMsgRevise";
  }

  public String processDfMsgMove()
  {
    List childMsgs = new ArrayList();
    messageManager
        .getChildMsgs(selectedMessage.getMessage().getId(), childMsgs);
    // selectedMessage.getMessage().setTopic(selectedTopic.getTopic());

    return null;
  }

  public String processDfMsgDeleteConfirm()
  {
    deleteMsg = true;
    return null;
  }

  public String processDfReplyMsgPost()
  {
    Message dMsg = constructMessage();

    dMsg.setInReplyTo(selectedMessage.getMessage());
    forumManager.saveMessage(dMsg);
    setSelectedForumForCurrentTopic((DiscussionTopic) forumManager
        .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));
    selectedTopic.setTopic((DiscussionTopic) forumManager
        .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));
    selectedTopic.getTopic().setBaseForum(selectedForum.getForum());
    //selectedTopic.addMessage(new DiscussionMessageBean(dMsg, messageManager));
    selectedTopic.insertMessage(new DiscussionMessageBean(dMsg, messageManager));
    selectedTopic.getTopic().addMessage(dMsg);
    forumManager.saveTopic(selectedTopic.getTopic());

    this.composeBody = null;
    this.composeLabel = null;
    this.composeTitle = null;

    this.attachments.clear();

    return ALL_MESSAGES;
  }

  public String processDfReplyMsgSaveDraft()
  {
    Message dMsg = constructMessage();
    dMsg.setDraft(Boolean.TRUE);

    dMsg.setInReplyTo(selectedMessage.getMessage());
    forumManager.saveMessage(dMsg);
    setSelectedForumForCurrentTopic((DiscussionTopic) forumManager
        .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));
    selectedTopic.setTopic((DiscussionTopic) forumManager
        .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));
    selectedTopic.getTopic().setBaseForum(selectedForum.getForum());
    //selectedTopic.addMessage(new DiscussionMessageBean(dMsg, messageManager));
    selectedTopic.insertMessage(new DiscussionMessageBean(dMsg, messageManager));
    selectedTopic.getTopic().addMessage(dMsg);
    forumManager.saveTopic(selectedTopic.getTopic());

    this.composeBody = null;
    this.composeLabel = null;
    this.composeTitle = null;

    this.attachments.clear();

    return ALL_MESSAGES;
  }

  public String processDeleteAttachRevise()
  {
    ExternalContext context = FacesContext.getCurrentInstance()
        .getExternalContext();
    String attachId = null;

    Map paramMap = context.getRequestParameterMap();
    Iterator itr = paramMap.keySet().iterator();
    while (itr.hasNext())
    {
      Object key = itr.next();
      if (key instanceof String)
      {
        String name = (String) key;
        int pos = name.lastIndexOf("dfmsg_current_attach");

        if (pos >= 0 && name.length() == pos + "dfmsg_current_attach".length())
        {
          attachId = (String) paramMap.get(key);
          break;
        }
      }
    }

    if ((attachId != null) && (!attachId.equals("")))
    {
      for (int i = 0; i < attachments.size(); i++)
      {
        if (attachId.equalsIgnoreCase(((Attachment) attachments.get(i))
            .getAttachmentId()))
        {
          prepareRemoveAttach.add((Attachment) attachments.get(i));
          attachments.remove(i);
          break;
        }
      }
    }

    return null;
  }

  public String processDfMsgRevisedPost()
  {
    Message dMsg = selectedMessage.getMessage();

    for (int i = 0; i < prepareRemoveAttach.size(); i++)
    {
      Attachment removeAttach = (Attachment) prepareRemoveAttach.get(i);
      dMsg.removeAttachment(removeAttach);
    }

    List oldList = dMsg.getAttachments();
    for (int i = 0; i < attachments.size(); i++)
    {
      Attachment thisAttach = (Attachment) attachments.get(i);
      boolean existed = false;
      for (int j = 0; j < oldList.size(); j++)
      {
        Attachment existedAttach = (Attachment) oldList.get(j);
        if (existedAttach.getAttachmentId()
            .equals(thisAttach.getAttachmentId()))
        {
          existed = true;
          break;
        }
      }
      if (!existed)
      {
        dMsg.addAttachment(thisAttach);
      }
    }
    String currentBody = getComposeBody();
    String revisedInfo = "Last Revised By " + this.getUserId() + " on ";
    Date now = new Date();
    revisedInfo += now.toString() + " <br/> ";
    
/*    if(currentBody != null && currentBody.length()>0 && currentBody.startsWith("Last Revised By "))
    {
    	if(currentBody.lastIndexOf(" <br/> ") > 0)
    	{
    		currentBody = currentBody.substring(currentBody.lastIndexOf(" <br/> ") + 7);
    	}
    }*/
    
    revisedInfo = revisedInfo.concat(currentBody);

    dMsg.setTitle(getComposeTitle());
    dMsg.setBody(revisedInfo);
    dMsg.setDraft(Boolean.FALSE);
    dMsg.setModified(new Date());
    dMsg.setModifiedBy(getUserId());
    // dMsg.setApproved(Boolean.TRUE);

    setSelectedForumForCurrentTopic((DiscussionTopic) forumManager
        .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));
    selectedTopic.setTopic((DiscussionTopic) forumManager
        .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));
    selectedTopic.getTopic().setBaseForum(selectedForum.getForum());
    dMsg.getTopic().setBaseForum(selectedTopic.getTopic().getBaseForum());
    forumManager.saveMessage(dMsg);

    List messageList = selectedTopic.getMessages();
    for (int i = 0; i < messageList.size(); i++)
    {
      DiscussionMessageBean dmb = (DiscussionMessageBean) messageList.get(i);
      if (dmb.getMessage().getId().equals(dMsg.getId()))
      {
        selectedTopic.getMessages().set(i,
            new DiscussionMessageBean(dMsg, messageManager));
      }
    }

    try
    {
      DiscussionTopic topic = null;
      try
      {
        topic = forumManager.getTopicById(selectedTopic.getTopic().getId());
      }
      catch (NumberFormatException e)
      {
        LOG.error(e.getMessage(), e);
      }
      setSelectedForumForCurrentTopic(topic);
      selectedTopic = getDecoratedTopic(topic);
      
    }
    catch (Exception e)
    {
      LOG.error(e.getMessage(), e);
      setErrorMessage(e.getMessage());
      return null;
    }

    prepareRemoveAttach.clear();
    composeBody = null;
    composeLabel = null;
    composeTitle = null;
    attachments.clear();

    return ALL_MESSAGES;
  }

  public String processDfMsgSaveRevisedDraft()
  {
    Message dMsg = selectedMessage.getMessage();

    for (int i = 0; i < prepareRemoveAttach.size(); i++)
    {
      Attachment removeAttach = (Attachment) prepareRemoveAttach.get(i);
      dMsg.removeAttachment(removeAttach);
    }

    List oldList = dMsg.getAttachments();
    for (int i = 0; i < attachments.size(); i++)
    {
      Attachment thisAttach = (Attachment) attachments.get(i);
      boolean existed = false;
      for (int j = 0; j < oldList.size(); j++)
      {
        Attachment existedAttach = (Attachment) oldList.get(j);
        if (existedAttach.getAttachmentId()
            .equals(thisAttach.getAttachmentId()))
        {
          existed = true;
          break;
        }
      }
      if (!existed)
      {
        dMsg.addAttachment(thisAttach);
      }
    }
    String currentBody = getComposeBody();
    String revisedInfo = "Last Revised By " + this.getUserId() + " on ";
    Date now = new Date();
    revisedInfo += now.toString() + " <br/> ";    
    revisedInfo = revisedInfo.concat(currentBody);

    dMsg.setTitle(getComposeTitle());
    dMsg.setBody(revisedInfo);
    dMsg.setDraft(Boolean.TRUE);
    dMsg.setModified(new Date());
    dMsg.setModifiedBy(getUserId());
    // dMsg.setApproved(Boolean.TRUE);
    
    setSelectedForumForCurrentTopic((DiscussionTopic) forumManager
        .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));
    selectedTopic.setTopic((DiscussionTopic) forumManager
        .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));
    selectedTopic.getTopic().setBaseForum(selectedForum.getForum());
    dMsg.getTopic().setBaseForum(selectedTopic.getTopic().getBaseForum());
    forumManager.saveMessage(dMsg);

    List messageList = selectedTopic.getMessages();
    for (int i = 0; i < messageList.size(); i++)
    {
      DiscussionMessageBean dmb = (DiscussionMessageBean) messageList.get(i);
      if (dmb.getMessage().getId().equals(dMsg.getId()))
      {
        selectedTopic.getMessages().set(i,
            new DiscussionMessageBean(dMsg, messageManager));
      }
    }

    try
    {
      DiscussionTopic topic = null;
      try
      {
        topic = forumManager.getTopicById(selectedTopic.getTopic().getId());
      }
      catch (NumberFormatException e)
      {
        LOG.error(e.getMessage(), e);
      }
      setSelectedForumForCurrentTopic(topic);
      selectedTopic = getDecoratedTopic(topic);      
    }
    catch (Exception e)
    {
      LOG.error(e.getMessage(), e);
      setErrorMessage(e.getMessage());
      return null;
    }

    prepareRemoveAttach.clear();
    composeBody = null;
    composeLabel = null;
    composeTitle = null;
    attachments.clear();

    return ALL_MESSAGES;
  }

  public String processDfReplyMsgCancel()
  {
    this.composeBody = null;
    this.composeLabel = null;
    this.composeTitle = null;

    this.attachments.clear();

    return ALL_MESSAGES;
  }

  public String processDfReplyTopicPost()
  {
    Message dMsg = constructMessage();

    forumManager.saveMessage(dMsg);
    setSelectedForumForCurrentTopic((DiscussionTopic) forumManager
        .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));
    selectedTopic.setTopic((DiscussionTopic) forumManager
        .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));
    selectedTopic.getTopic().setBaseForum(selectedForum.getForum());
    //selectedTopic.addMessage(new DiscussionMessageBean(dMsg, messageManager));
    selectedTopic.insertMessage(new DiscussionMessageBean(dMsg, messageManager));
    selectedTopic.getTopic().addMessage(dMsg);
    forumManager.saveTopic(selectedTopic.getTopic());

    this.composeBody = null;
    this.composeLabel = null;
    this.composeTitle = null;

    this.attachments.clear();

    return ALL_MESSAGES;
  }

  public String processDfReplyTopicSaveDraft()
  {
    Message dMsg = constructMessage();
    dMsg.setDraft(Boolean.TRUE);

    forumManager.saveMessage(dMsg);
    setSelectedForumForCurrentTopic((DiscussionTopic) forumManager
        .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));
    selectedTopic.setTopic((DiscussionTopic) forumManager
        .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));
    selectedTopic.getTopic().setBaseForum(selectedForum.getForum());
    //selectedTopic.addMessage(new DiscussionMessageBean(dMsg, messageManager));
    selectedTopic.insertMessage(new DiscussionMessageBean(dMsg, messageManager));
    selectedTopic.getTopic().addMessage(dMsg);
    forumManager.saveTopic(selectedTopic.getTopic());

    this.composeBody = null;
    this.composeLabel = null;
    this.composeTitle = null;

    this.attachments.clear();

    return ALL_MESSAGES;
  }

  public String processDfReplyTopicCancel()
  {
    this.composeBody = null;
    this.composeLabel = null;
    this.composeTitle = null;

    this.attachments.clear();

    return ALL_MESSAGES;
  }

  public boolean getDeleteMsg()
  {
    return deleteMsg;
  }

  public String processDfMsgDeleteConfirmYes()
  {
    List delList = new ArrayList();
    messageManager.getChildMsgs(selectedMessage.getMessage().getId(), delList);

    selectedTopic.removeMessage(selectedMessage);
    Topic tempTopic = forumManager.getTopicByIdWithMessages(selectedTopic
        .getTopic().getId());
    tempTopic.removeMessage(selectedMessage.getMessage());
    // selectedTopic.getTopic().removeMessage(selectedMessage.getMessage());
    for (int i = 0; i < delList.size(); i++)
    {
      selectedTopic.removeMessage(new DiscussionMessageBean((Message) delList
          .get(i), messageManager));
      // tempTopic.removeMessage((Message) delList.get(i));
      // selectedTopic.getTopic().removeMessage((Message) delList.get(i));
    }

    messageManager.deleteMsgWithChild(selectedMessage.getMessage().getId());

    try
    {
      DiscussionTopic topic = null;
      try
      {
        topic = forumManager.getTopicById(selectedTopic.getTopic().getId());
      }
      catch (NumberFormatException e)
      {
        LOG.error(e.getMessage(), e);
      }
      setSelectedForumForCurrentTopic(topic);
      selectedTopic = getDecoratedTopic(topic);
    }
    catch (Exception e)
    {
      LOG.error(e.getMessage(), e);
      setErrorMessage(e.getMessage());
      this.deleteMsg = false;
      return "main";
    }

    this.deleteMsg = false;

    return ALL_MESSAGES;
  }

  public String processDfMsgDeleteCancel()
  {
    this.deleteMsg = false;

    return null;
  }

  public void saveForumAttach(DiscussionForum forum)
  {
    for (int i = 0; i < prepareRemoveAttach.size(); i++)
    {
      Attachment removeAttach = (Attachment) prepareRemoveAttach.get(i);
      List oldList = forum.getAttachments();
      for (int j = 0; j < oldList.size(); j++)
      {
        Attachment existedAttach = (Attachment) oldList.get(j);
        if (existedAttach.getAttachmentId().equals(
            removeAttach.getAttachmentId()))
        {
          forum.removeAttachment(removeAttach);
          break;
        }
      }
    }

    List oldList = forum.getAttachments();
    if (oldList != null)
    {
      for (int i = 0; i < attachments.size(); i++)
      {
        Attachment thisAttach = (Attachment) attachments.get(i);
        boolean existed = false;
        for (int j = 0; j < oldList.size(); j++)
        {
          Attachment existedAttach = (Attachment) oldList.get(j);
          if (existedAttach.getAttachmentId().equals(
              thisAttach.getAttachmentId()))
          {
            existed = true;
            break;
          }
        }
        if (!existed)
        {
          forum.addAttachment(thisAttach);
        }
      }
    }

    prepareRemoveAttach.clear();
    attachments.clear();
  }

  public void saveTopicAttach(DiscussionTopic topic)
  {
    for (int i = 0; i < prepareRemoveAttach.size(); i++)
    {
      Attachment removeAttach = (Attachment) prepareRemoveAttach.get(i);
      List oldList = topic.getAttachments();
      for (int j = 0; j < oldList.size(); j++)
      {
        Attachment existedAttach = (Attachment) oldList.get(j);
        if (existedAttach.getAttachmentId().equals(
            removeAttach.getAttachmentId()))
        {
          topic.removeAttachment(removeAttach);
          break;
        }
      }
    }

    List oldList = topic.getAttachments();
    if (oldList != null)
    {
      for (int i = 0; i < attachments.size(); i++)
      {
        Attachment thisAttach = (Attachment) attachments.get(i);
        boolean existed = false;
        for (int j = 0; j < oldList.size(); j++)
        {
          Attachment existedAttach = (Attachment) oldList.get(j);
          if (existedAttach.getAttachmentId().equals(
              thisAttach.getAttachmentId()))
          {
            existed = true;
            break;
          }
        }
        if (!existed)
        {
          topic.addAttachment(thisAttach);
        }
      }
    }

    prepareRemoveAttach.clear();
    attachments.clear();
  }

  public String processDeleteAttachSetting()
  {
    LOG.debug("processDeleteAttach()");

    ExternalContext context = FacesContext.getCurrentInstance()
        .getExternalContext();
    String attachId = null;

    Map paramMap = context.getRequestParameterMap();
    Iterator itr = paramMap.keySet().iterator();
    while (itr.hasNext())
    {
      Object key = itr.next();
      if (key instanceof String)
      {
        String name = (String) key;
        int pos = name.lastIndexOf("dfmsg_current_attach");

        if (pos >= 0 && name.length() == pos + "dfmsg_current_attach".length())
        {
          attachId = (String) paramMap.get(key);
          break;
        }
      }
    }

    if ((attachId != null) && (!attachId.equals("")))
    {
      for (int i = 0; i < attachments.size(); i++)
      {
        if (attachId.equalsIgnoreCase(((Attachment) attachments.get(i))
            .getAttachmentId()))
        {
          prepareRemoveAttach.add((Attachment) attachments.get(i));
          attachments.remove(i);
          break;
        }
      }
    }

    return null;
  }

  public boolean getThreaded()
  {
    return threaded;
  }

  public void setThreaded(boolean threaded)
  {
    this.threaded = threaded;
  }

  public String getExpanded()
  {
    return expanded;
  }

  public void setExpanded(String expanded)
  {
    this.expanded = expanded;
  }

  public void rearrageTopicMsgsThreaded()
  {
 
 
  	List msgsList = selectedTopic.getMessages();
  	List orderedList = new ArrayList();
  	
  	if(msgsList != null)
  	{
  		for(int i=0; i<msgsList.size(); i++)
  		{
  			DiscussionMessageBean dmb = (DiscussionMessageBean)msgsList.get(i);
  			if(dmb.getMessage().getInReplyTo() == null)
  			{
  				dmb.setDepth(0);
  				orderedList.add(dmb);
  				//for performance speed - operate with existing selectedTopic msgs instead of getting from manager through DB again 
  				//recursiveGetThreadedMsgs(msgsList, orderedList, dmb);
  				recursiveGetThreadedMsgsFromList(msgsList, orderedList, dmb);
  			}
  		}
  	}
  	selectedTopic.setMessages(orderedList);
 
  }

  public void recursiveGetThreadedMsgs(List msgsList, List returnList,
      DiscussionMessageBean currentMsg)
  {
  	List childList = messageManager.getFirstLevelChildMsgs(currentMsg.getMessage().getId());
		
		for(int j=0; j<childList.size(); j++)
		{
			Message currentChild = (Message)childList.get(j);
			for(int k=0; k<msgsList.size(); k++)
			{
				Message existedMsg = ((DiscussionMessageBean)msgsList.get(k)).getMessage();
				if(currentChild.getId().equals(existedMsg.getId()))
				{
					DiscussionMessageBean dmb = new DiscussionMessageBean(currentChild, messageManager);
/*					dmb.setDepth(currentMsg.getDepth() + 1);
					returnList.add(dmb);*/
					((DiscussionMessageBean)msgsList.get(k)).setDepth(currentMsg.getDepth() + 1);
					returnList.add(((DiscussionMessageBean)msgsList.get(k)));
					recursiveGetThreadedMsgs(msgsList, returnList, ((DiscussionMessageBean)msgsList.get(k)));
					break;
				}
			}
		}
 
  }

  private void recursiveGetThreadedMsgsFromList(List msgsList, List returnList,
      DiscussionMessageBean currentMsg)
  {
    for (int i = 0; i < msgsList.size(); i++)
    {
      DiscussionMessageBean thisMsgBean = (DiscussionMessageBean) msgsList
          .get(i);
      Message thisMsg = thisMsgBean.getMessage();
      if (thisMsg.getInReplyTo() != null
          && thisMsg.getInReplyTo().getId().equals(
              currentMsg.getMessage().getId()))
      {
        /*
         * DiscussionMessageBean dmb = new DiscussionMessageBean(thisMsg, messageManager);
         * dmb.setDepth(currentMsg.getDepth() + 1); returnList.add(dmb);
         * this.recursiveGetThreadedMsgsFromList(msgsList, returnList, dmb);
         */
        thisMsgBean.setDepth(currentMsg.getDepth() + 1);
        returnList.add(thisMsgBean);
        this
            .recursiveGetThreadedMsgsFromList(msgsList, returnList, thisMsgBean);
      }
    }
  }
  
  public String processCheckAll()
  {
  	for(int i=0; i<selectedTopic.getMessages().size(); i++)
  	{
  		((DiscussionMessageBean)selectedTopic.getMessages().get(i)).setSelected(true);
  	}
  	return null;
  }
 
  private void setMessageBeanPreNextStatus()
  {
  	if(selectedTopic != null)
  	{
  		if(selectedTopic.getMessages() != null)
  		{
  			List tempMsgs = selectedTopic.getMessages();
  			for(int i=0; i<tempMsgs.size(); i++)
				{
					DiscussionMessageBean dmb = (DiscussionMessageBean)tempMsgs.get(i);
					if(i==0)
					{
						dmb.setHasPre(false);
						if(i==(tempMsgs.size()-1))
						{
							dmb.setHasNext(false);
						}
						else
						{
							dmb.setHasNext(true);
						}
					}
					else if(i==(tempMsgs.size()-1))
					{
						dmb.setHasPre(true);
						dmb.setHasNext(false);
					}
					else
					{
						dmb.setHasNext(true);
						dmb.setHasPre(true);
					}
				}
  		}
  	}
  }
  
 
  /**
   * @return Returns the selectedMessageView.
   */
  public String getSelectedMessageView()
  {
    return selectedMessageView;
  }

  /**
   * @param selectedMessageView
   *          The selectedMessageView to set.
   */
  public void setSelectedMessageView(String selectedMessageView)
  {
    this.selectedMessageView = selectedMessageView;
  }

  /**
   * @return Returns the displayUnreadOnly.
   */
  public boolean getDisplayUnreadOnly()
  {
    return displayUnreadOnly;
  }

  /**
   * @param vce
   */
  public void processValueChangeForMessageView(ValueChangeEvent vce)
  {
    if (LOG.isDebugEnabled())
      LOG.debug("processValueChangeForMessageView(ValueChangeEvent " + vce
          + ")");
    isDisplaySearchedMessages=false;
    searchText="";
    String changeView = (String) vce.getNewValue();
    this.displayUnreadOnly = false;
    if (changeView == null)
    {
      threaded = false;
      setErrorMessage("Failed Rending Messages");
      return;
    }
    if (changeView.equals(ALL_MESSAGES))
    {
      threaded = false;
      setSelectedMessageView(ALL_MESSAGES);
      
      DiscussionTopic topic = null;
      topic = forumManager.getTopicById(selectedTopic.getTopic().getId());
      setSelectedForumForCurrentTopic(topic);
      selectedTopic = getDecoratedTopic(topic);

      return;
    }
    else
      if (changeView.equals(UNREAD_VIEW))
      {
      	threaded = false;
        this.displayUnreadOnly = true;
        return;
      }
      else
        if (changeView.equals(THREADED_VIEW))
        {
          threaded = true;
          expanded = "true";
          return;
        }
        else
          if (changeView.equals("expand"))
          {
            threaded = true;
            expanded = "true";
            return;
          }
          else
            if (changeView.equals("collapse"))
            {
              threaded = true;
              expanded = "false";
              return;
            }
            else
            {
              threaded = false;
              setErrorMessage("This view is under contruction");
              return;
            }
  }

  /**
   * @return
   */
  public String processActionSearch()
  {
    LOG.debug("processActionSearch()");

//    //TODO : should be fetched via a query in db
//    //Subject, Authored By, Date,
//    isDisplaySearchedMessages=true;
//  
//    if(searchText==null || searchText.trim().length()<1)
//    {
//      setErrorMessage("Invalid search criteria");  
//      return ALL_MESSAGES;
//    }
//    if(selectedTopic == null)
//    {
//      setErrorMessage("There is no topic selected for search");     
//      return ALL_MESSAGES;
//    }
//    searchResults=new  DiscussionTopicBean(selectedTopic.getTopic(),selectedForum.getForum() ,uiPermissionsManager);
//   if(selectedTopic.getMessages()!=null)
//    {
//     Iterator iter = selectedTopic.getMessages().iterator();
//     
//     while (iter.hasNext())
//      {
//            DiscussionMessageBean decoMessage = (DiscussionMessageBean) iter.next();
//        if((decoMessage.getMessage()!= null && (decoMessage.getMessage().getTitle().matches(".*"+searchText+".*") ||
//            decoMessage.getMessage().getCreatedBy().matches(".*"+searchText+".*") ||
//            decoMessage.getMessage().getCreated().toString().matches(".*"+searchText+".*") )))
//        {
//          searchResults.addMessage(decoMessage);
//        }
//      }
//    }  
   return ALL_MESSAGES;
  }

  /**
   * @return
   */
  public String processActionMarkCheckedAsRead()
  {
    return markCheckedMessages(true);
  }

  /**
   * @return
   */
  public String processActionMarkCheckedAsUnread()
  {
    return markCheckedMessages(false);
  }

  private String markCheckedMessages(boolean readStatus)
  {
    if (selectedTopic == null)
    {
      setErrorMessage("Lost association with current topic");
      return ALL_MESSAGES;
    }
    List messages = selectedTopic.getMessages();
    if (messages == null || messages.size() < 1)
    {
      setErrorMessage("No message selected to mark as read. Please select a message");
      return ALL_MESSAGES;
    }
    Iterator iter = messages.iterator();
    while (iter.hasNext())
    {
      DiscussionMessageBean decoMessage = (DiscussionMessageBean) iter.next();
      if (decoMessage.isSelected())
      {
        forumManager.markMessageAs(decoMessage.getMessage(), readStatus);
      }
    }
    return displayTopicById(TOPIC_ID); // reconstruct topic again;
  }

  
  /**
   * @return Returns the isDisplaySearchedMessages.
   */
  public boolean getIsDisplaySearchedMessages()
  {
    return isDisplaySearchedMessages;
  }

  /**
   * @return Returns the searchText.
   */
  public String getSearchText()
  {
    return searchText;
  }

  /**
   * @param searchText
   *          The searchText to set.
   */
  public void setSearchText(String searchText)
  {
    this.searchText = searchText;
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
          if (role != null) roleList.add(role.getId());
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

  /**
   * @param topic
   */
  private void setSelectedForumForCurrentTopic(DiscussionTopic topic)
  {
    if (selectedForum != null)
    {
      return;
    }
    DiscussionForum forum = (DiscussionForum) topic.getBaseForum();
    if (forum == null)
    {

      String forumId = getExternalParameterByKey(FORUM_ID);
      if (forumId == null || forumId.trim().length() < 1)
      {
        selectedForum = null;
        return;
      }
      forum = forumManager.getForumById(new Long(forumId));
      if (forum == null)
      {
        selectedForum = null;
        return;
      }
    }
    selectedForum = new DiscussionForumBean(forum, uiPermissionsManager);
  }

  /**
   * @param errorMsg
   */
  private void setErrorMessage(String errorMsg)
  {
    LOG.debug("setErrorMessage(String " + errorMsg + ")");
    FacesContext.getCurrentInstance().addMessage(null,
        new FacesMessage("Alert: " + errorMsg));
  }
  
  public List getAccessList()
  {
    List access = new ArrayList();
    access.add(new SelectItem("allParticipants","All Participants"));
    access.add(new SelectItem("All Instructors"));
    access.add(new SelectItem("A"));
    access.add(new SelectItem("B"));
    access.add(new SelectItem("C"));
    access.add(new SelectItem("D"));
    return access;    
  }

  public List getContributorsList()
  {
    List access = new ArrayList();
    access.add("allParticipants");   
    return access;    
  }
}