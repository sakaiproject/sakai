package org.sakaiproject.tool.messageforums;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.DBMembershipItem;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.MembershipManager;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.PermissionLevel;
import org.sakaiproject.api.app.messageforums.PermissionLevelManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;

//grading
import org.sakaiproject.api.app.messageforums.PrivateMessage; 
import org.sakaiproject.service.gradebook.shared.GradebookService; 
import org.sakaiproject.service.gradebook.shared.Assignment; 
import org.sakaiproject.api.kernel.component.cover.ComponentManager; 
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager; 
import org.sakaiproject.service.legacy.user.User; 
import org.sakaiproject.service.legacy.user.cover.UserDirectoryService; 

import org.sakaiproject.api.common.authorization.PermissionsMask;
import org.sakaiproject.api.kernel.session.ToolSession;
import org.sakaiproject.api.kernel.session.cover.SessionManager;
import org.sakaiproject.api.kernel.tool.cover.ToolManager;
import org.sakaiproject.component.app.messageforums.MembershipItem;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.service.legacy.authzGroup.AuthzGroup;
import org.sakaiproject.service.legacy.authzGroup.Role;
import org.sakaiproject.service.legacy.authzGroup.cover.AuthzGroupService;
import org.sakaiproject.service.legacy.entity.Reference;
import org.sakaiproject.service.legacy.filepicker.FilePickerHelper;
import org.sakaiproject.service.legacy.security.cover.SecurityService;
import org.sakaiproject.service.legacy.site.Group;
import org.sakaiproject.service.legacy.site.Site;
import org.sakaiproject.service.legacy.site.cover.SiteService;
import org.sakaiproject.tool.messageforums.ui.DiscussionForumBean;
import org.sakaiproject.tool.messageforums.ui.DiscussionMessageBean;
import org.sakaiproject.tool.messageforums.ui.DiscussionTopicBean;
import org.sakaiproject.tool.messageforums.ui.PermissionBean;

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
  
  private static final String PERMISSION_MODE_TEMPLATE = "template";
  private static final String PERMISSION_MODE_FORUM = "forum";
  private static final String PERMISSION_MODE_TOPIC = "topic";

  private DiscussionForumBean selectedForum;
  private DiscussionTopicBean selectedTopic;
  private DiscussionTopicBean searchResults;
  private DiscussionMessageBean selectedMessage;
  private List groupsUsersList;   
  private List totalGroupsUsersList;
  private List selectedGroupsUsersList;
  private Map courseMemberMap;
  private List permissions;
  private List levels;
  private AreaManager areaManager;
  
  private static final String TOPIC_ID = "topicId";
  private static final String FORUM_ID = "forumId";
  private static final String MESSAGE_ID = "messageId";
  private static final String REDIRECT_PROCESS_ACTION = "redirectToProcessAction";

  private static final String INSUFFICIENT_PRIVILEGES_TO_EDIT_TEMPLATE_SETTINGS = "Insufficient privileges to edit Template Settings";
  private static final String SHORT_DESC_TOO_LONG = "Short description can not be longer than 255 characters.";

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
  private boolean errorSynch = false;
  // attachment
  private ArrayList attachments = new ArrayList();
  private ArrayList prepareRemoveAttach = new ArrayList();
  // private boolean attachCaneled = false;
  // private ArrayList oldAttachments = new ArrayList();
  // private List allAttachments = new ArrayList();
  private boolean threaded = false;
  private String expanded = "true";
  private boolean isDisplaySearchedMessages;
  private List siteMembers = new ArrayList();
  private String selectedRole;
  
  private boolean editMode = false;
  private String permissionMode;
  
  //grading 
  private boolean gradeNotify = false; 
  private List assignments; 
  private String selectedAssign = "Default_0"; 
  private String gradePoint = ""; 
  private String gradebookScore = ""; 
  private String gradeComment; 
  private boolean noGradeWarn = false; 
  private boolean noAssignWarn = false; 

  /**
   * Dependency Injected
   */
  private DiscussionForumManager forumManager;
  private UIPermissionsManager uiPermissionsManager;
  private MessageForumsTypeManager typeManager;
  private MembershipManager membershipManager;
  private PermissionLevelManager permissionLevelManager;
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
   * @param typeManager The typeManager to set.
   */
  public void setTypeManager(MessageForumsTypeManager typeManager)
  {
    this.typeManager = typeManager;
  }
  
  

  /**
   * @param membershipManager The membershipManager to set.
   */
  public void setMembershipManager(MembershipManager membershipManager)
  {
    this.membershipManager = membershipManager;
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
    
    setEditMode(false);
    setPermissionMode(PERMISSION_MODE_TEMPLATE);
    	       	
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
  public List getPermissions()
  {
  	  	  	
    if (permissions == null)
    {
      siteMembers=null;
      getSiteRoles();
    }
    return permissions;
  }

  /**
   * @return
   */
  public void setPermissions(List permissions)
  {
    this.permissions = permissions;
  }

//  /**
//   * @return Returns the templateMessagePermissions.
//   */
//  public List getTemplateMessagePermissions()
//  {
//    if (templateMessagePermissions == null)
//    {
//      templateMessagePermissions = forumManager.getAreaMessagePermissions();
//    }
//    return templateMessagePermissions;
//  }
//
//  /**
//   * @param templateMessagePermissions
//   *          The templateMessagePermissions to set.
//   */
//  public void setTemplateMessagePermissions(List templateMessagePermissions)
//  {
//    this.templateMessagePermissions = templateMessagePermissions;
//  }
  
  /**
   * @return
   */
  public String processActionReviseTemplateSettings()
  {
  	if (LOG.isDebugEnabled()){
      LOG.debug("processActionReviseTemplateSettings()");
  	}
    
  	setEditMode(true); 
  	setPermissionMode(PERMISSION_MODE_TEMPLATE);
    return TEMPLATE_SETTING;
  }

  /**
   * @return
   */
  public String processActionSaveTemplateSettings()
  {
    LOG.debug("processActionSaveTemplateSettings()");
    if(!isInstructor())
    {
      setErrorMessage(INSUFFICIENT_PRIVILEGES_TO_EDIT_TEMPLATE_SETTINGS);
      return MAIN;
    }    
    
    Area area = areaManager.getDiscusionArea();
    setObjectPermissions(area);
    areaManager.saveArea(area);
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
    
    Area area = null;
    if ((area = areaManager.getDiscusionArea()) != null){
    	area.setMembershipItemSet(new HashSet());
    	areaManager.saveArea(area);
    	permissions = null;
    }
    else{
    	throw new IllegalStateException("Could not obtain area for site: " + getContextSiteId());
    }
    
    return TEMPLATE_SETTING;      
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
    
    setEditMode(true);
    setPermissionMode(PERMISSION_MODE_FORUM);
        
    if (getNewForum())
    {
      DiscussionForum forum = forumManager.createForum();
      selectedForum = null;
      selectedForum = new DiscussionForumBean(forum, uiPermissionsManager, forumManager);
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
    setEditMode(false);
    setPermissionMode(PERMISSION_MODE_FORUM);
    
    String forumId = getExternalParameterByKey(FORUM_ID);
    if ((forumId) == null)
    {
      setErrorMessage("Invalid forum selected");
      return MAIN;
    }
    DiscussionForum forum = forumManager.getForumById(new Long(forumId));
    if(!uiPermissionsManager.isChangeSettings(forum))
    {
      setErrorMessage("Insufficient privileges to change forum settings");
      return MAIN;
    }
    selectedForum = new DiscussionForumBean(forum, uiPermissionsManager, forumManager);    
    return FORUM_SETTING;

  }

  /**
   * @return
   */
  public String processActionReviseForumSettings()
  {
    LOG.debug("processActionReviseForumSettings()");    
    setEditMode(true);
    setPermissionMode(PERMISSION_MODE_FORUM);
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

    if(selectedForum !=null && selectedForum.getForum()!=null &&
    		(selectedForum.getForum().getShortDescription()!=null) && 
    		(selectedForum.getForum().getShortDescription().length() > 255))
    {
    	setErrorMessage(SHORT_DESC_TOO_LONG);
    	return null;
    }

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
    
    DiscussionForum forum = saveForumSettings(false);    
    if(!uiPermissionsManager.isNewTopic(selectedForum.getForum()))
    {
      setErrorMessage("Insufficient privileges to create new topic");
      return MAIN;
    }    
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
    
    if(selectedForum !=null && selectedForum.getForum()!=null &&
    		(selectedForum.getForum().getShortDescription()!=null) && 
    		(selectedForum.getForum().getShortDescription().length() > 255))
    {
    	setErrorMessage(SHORT_DESC_TOO_LONG);
    	return null;
    }
    
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

    if(selectedForum !=null && selectedForum.getForum()!=null &&
    		(selectedForum.getForum().getShortDescription()!=null) && 
    		(selectedForum.getForum().getShortDescription().length() > 255))
    {
    	setErrorMessage(SHORT_DESC_TOO_LONG);
    	return null;
    }

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
    setObjectPermissions(forum);
    if (draft)
      forumManager.saveForumAsDraft(forum);
    else
      forumManager.saveForum(forum);
    //forumManager.saveForumControlPermissions(forum, forumControlPermissions);
    //forumManager.saveForumMessagePermissions(forum, forumMessagePermissions);
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
   * @return
   */
  public String processActionNewTopic()
  {   
    LOG.debug("processActionNewTopic()");
    
    setEditMode(true);
    setPermissionMode(PERMISSION_MODE_TOPIC);
         
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
    
    setPermissionMode(PERMISSION_MODE_TOPIC);
    setEditMode(true);
        
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
  
    setSelectedForumForCurrentTopic(topic);
    selectedTopic = new DiscussionTopicBean(topic, selectedForum.getForum(),
        uiPermissionsManager, forumManager);
    if(!uiPermissionsManager.isChangeSettings(selectedTopic.getTopic(),selectedForum.getForum()))
    {
      setErrorMessage("Insufficient privileges to change topic settings");
      return MAIN;
    }
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
    		(selectedTopic.getTopic().getShortDescription()!=null) && 
    		(selectedTopic.getTopic().getShortDescription().length() > 255))
    {
    	setErrorMessage(SHORT_DESC_TOO_LONG);
    	return null;
    }    
    
    setPermissionMode(PERMISSION_MODE_TOPIC);
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
    		(selectedTopic.getTopic().getShortDescription()!=null) && 
    		(selectedTopic.getTopic().getShortDescription().length() > 255))
    {
    	setErrorMessage(SHORT_DESC_TOO_LONG);
    	return null;
    }
    
    setPermissionMode(PERMISSION_MODE_TOPIC);
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
    		(selectedTopic.getTopic().getShortDescription()!=null) && 
    		(selectedTopic.getTopic().getShortDescription().length() > 255))
    {
    	setErrorMessage(SHORT_DESC_TOO_LONG);
    	return null;
    }
    
    setPermissionMode(PERMISSION_MODE_TOPIC);
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
  	LOG.debug("saveTopicSettings(" + draft + ")");
  	setPermissionMode(PERMISSION_MODE_TOPIC);
    if (selectedTopic != null)
    {
      DiscussionTopic topic = selectedTopic.getTopic();
      if (selectedForum != null)
      {
        topic.setBaseForum(selectedForum.getForum());
        saveTopicAttach(topic);
        setObjectPermissions(topic);
        if (draft)
        {        	
          forumManager.saveTopicAsDraft(topic);          
        }
        else
        {        	
          forumManager.saveTopic(topic);
        }        
        //forumManager
        //    .saveTopicControlPermissions(topic, topicControlPermissions);
        //forumManager
        //    .saveTopicMessagePermissions(topic, topicMessagePermissions);
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
    
    setEditMode(false);
    setPermissionMode(PERMISSION_MODE_TOPIC);
    DiscussionTopic topic = (DiscussionTopic) forumManager
        .getTopicByIdWithAttachments(new Long(
            getExternalParameterByKey(TOPIC_ID)));
    if (topic == null)
    {
      return MAIN;
    }
    setSelectedForumForCurrentTopic(topic);
    if(!uiPermissionsManager.isChangeSettings(topic,selectedForum.getForum()))
    {
      setErrorMessage("Insufficient privileges to change topic settings");
      return MAIN;
    }
    selectedTopic = new DiscussionTopicBean(topic, selectedForum.getForum(),
        uiPermissionsManager, forumManager);
    
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
    DiscussionTopic topic=forumManager.getTopicById(new Long(
        getExternalParameterByKey(TOPIC_ID)));
    setSelectedForumForCurrentTopic(topic);
    selectedTopic = new DiscussionTopicBean(topic, selectedForum.getForum(),
        uiPermissionsManager, forumManager);
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

  private String getExternalParameterByKey(String parameterId)
  {    
    ExternalContext context = FacesContext.getCurrentInstance()
        .getExternalContext();
    Map paramMap = context.getRequestParameterMap();
    
    return (String) paramMap.get(parameterId);    
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
        uiPermissionsManager, forumManager);
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
              uiPermissionsManager, forumManager);
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
        selectedForum.getForum(), uiPermissionsManager, forumManager);
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
          if(decoTopic.getIsRead() || (decoTopic.getIsNewResponse()&& decoMsg.getIsOwn()))
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
//    this.templateControlPermissions = null;
//    this.templateMessagePermissions = null;
    this.permissions=null;
    this.errorSynch = false;
    this.siteMembers=null;   
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
    selectedForum = new DiscussionForumBean(forum, uiPermissionsManager, forumManager);
    DiscussionTopic topic = forumManager.createTopic(forum);
    if (topic == null)
    {
      setErrorMessage("Failed to create new topic");
      return null;
    }
    selectedTopic = new DiscussionTopicBean(topic, forum, uiPermissionsManager, forumManager);
    return new DiscussionTopicBean(topic, forum, uiPermissionsManager, forumManager);
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
    try 
    { 
      GradebookService gradebookService = (org.sakaiproject.service.gradebook.shared.GradebookService) 
      ComponentManager.get("org.sakaiproject.service.gradebook.GradebookService"); 
      List gradeAssignmentsBeforeFilter = gradebookService.getAssignments(ToolManager.getCurrentPlacement().getContext());
      List gradeAssignments = new ArrayList();
      for(int i=0; i<gradeAssignmentsBeforeFilter.size(); i++)
      {
        Assignment thisAssign = (Assignment) gradeAssignmentsBeforeFilter.get(i);
        if(!thisAssign.isExternallyMaintained())
        {
          gradeAssignments.add(thisAssign);
        }
      }
      assignments = new ArrayList(); 
      SelectItem item = new SelectItem("Default_0", "Select an assignment"); 
      assignments.add(item); 
      for(int i=0; i<gradeAssignments.size(); i++) 
      { 
        try 
        { 
          Assignment thisAssign = (Assignment) gradeAssignments.get(i); 
          
          String assignName = thisAssign.getName(); 
          
          item = new SelectItem((new Integer(i+1)).toString(), assignName); 
          assignments.add(item); 
        } 
        catch(Exception e) 
        { 
          LOG.error("DiscussionForumTool - processDfMsgGrd:" + e); 
          e.printStackTrace(); 
        } 
      } 
    } 
    catch(Exception e1) 
    { 
      LOG.error("DiscussionForumTool&processDfMsgGrad:" + e1); 
      e1.printStackTrace(); 
    } 
    return "dfMsgGrade"; 
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
    List tempList = forumManager.getMessagesByTopicId(selectedTopic.getTopic().getId());
    if(tempList != null)
    {
    	boolean existed = false;
    	for(int i=0; i<tempList.size(); i++)
    	{
    		Message tempMsg = (Message)tempList.get(i);
    		if(tempMsg.getId().equals(selectedMessage.getMessage().getId()))
    		{
    			existed = true;
    			break;
    		}
    	}
    	if(!existed)
    	{
      	this.errorSynch = true;
        return null;
    	}
    }
    else
    {
    	this.errorSynch = true;
      return null;
    }
    
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
    List tempList = forumManager.getMessagesByTopicId(selectedTopic.getTopic().getId());
    if(tempList != null)
    {
    	boolean existed = false;
    	for(int i=0; i<tempList.size(); i++)
    	{
    		Message tempMsg = (Message)tempList.get(i);
    		if(tempMsg.getId().equals(selectedMessage.getMessage().getId()))
    		{
    			existed = true;
    			break;
    		}
    	}
    	if(!existed)
    	{
      	this.errorSynch = true;
        return null;
    	}
    }
    else
    {
    	this.errorSynch = true;
      return null;
    }


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
  	this.errorSynch = false;
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
    
    if(delList.size() > 0)
    {
    	if(!selectedTopic.getIsDeleteAny())
    	{
    		errorSynch = true;
    		return null;
    	}
    }

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
    this.errorSynch = false;

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

  public void setGradeNotify(boolean gradeNotify) 
  { 
    this.gradeNotify = gradeNotify; 
  } 
   
  public boolean getGradeNotify() 
  { 
    return gradeNotify; 
  } 
   
  public String getSelectedAssign() 
  { 
    return selectedAssign; 
  } 
   
  public void setSelectedAssign(String selectedAssign) 
  { 
    this.selectedAssign = selectedAssign; 
  } 
   
  public void setGradePoint(String gradePoint) 
  { 
    this.gradePoint = gradePoint; 
  } 
   
  public String getGradePoint() 
  { 
    return gradePoint; 
  } 
   
  public String getGradebookScore() 
  { 
    return gradebookScore; 
  } 
   
  public void setGradebookScore(String gradebookScore) 
  { 
    this.gradebookScore = gradebookScore; 
  } 
   
  public List getAssignments() 
  { 
    return assignments; 
  } 
   
  public void setAssignments(List assignments) 
  { 
    this.assignments = assignments; 
  } 
   
  public void setGradeComment(String gradeComment) 
  { 
    this.gradeComment = gradeComment; 
  } 
   
  public String getGradeComment() 
  { 
    return gradeComment; 
  } 
   
  public boolean getNoGradeWarn() 
  { 
    return noGradeWarn; 
  } 

  public void setNoGradeWarn(boolean noGradeWarn) 
  { 
    this.noGradeWarn = noGradeWarn; 
  } 
   
  public boolean getNoAssignWarn() 
  { 
    return noAssignWarn; 
  } 
 
  public void setNoAssignWarn(boolean noAssignWarn) 
  { 
    this.noAssignWarn = noAssignWarn; 
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
  
  public String processDfGradeCancel() 
  { 
     
    gradeNotify = false; 
    assignments.clear(); 
    selectedAssign = "Default_0"; 
    gradePoint = ""; 
    gradebookScore = ""; 
    gradeComment = ""; 
    noGradeWarn = false; 
    noAssignWarn = false; 
    return MESSAGE_VIEW; 
  } 
   
  public String processGradeAssignChange(ValueChangeEvent vce) 
  { 
    String changeAssign = (String) vce.getNewValue(); 
    if (changeAssign == null) 
    { 
      return null; 
    } 
    else 
    { 
      try 
      { 
        selectedAssign = changeAssign; 
         
        GradebookService gradebookService = (org.sakaiproject.service.gradebook.shared.GradebookService) 
        ComponentManager.get("org.sakaiproject.service.gradebook.GradebookService"); 
         
        if((!selectedAssign.equalsIgnoreCase("Default_0")) 
          && ((gradebookService.getAssignmentScore(ToolManager.getCurrentPlacement().getContext(),  
              ((SelectItem)assignments.get((new Integer(selectedAssign)).intValue())).getLabel(),  
              UserDirectoryService.getUser(selectedMessage.getMessage().getAuthor()).getId())) != null)) 
        { 
          gradebookScore = (gradebookService.getAssignmentScore(ToolManager.getCurrentPlacement().getContext(),  
              ((SelectItem)assignments.get((new Integer(selectedAssign)).intValue())).getLabel(),  
              UserDirectoryService.getUser(selectedMessage.getMessage().getAuthor()).getId())).toString();  
        } 
        else 
        { 
          gradebookScore = ""; 
        } 
 
        return "dfMsgGrade"; 
      } 
      catch(Exception e) 
      { 
        LOG.error("processGradeAssignChange in DiscussionFOrumTool - " + e); 
        e.printStackTrace(); 
        return null; 
      } 
    } 
   } 
 
  public String processDfGradeSubmit() 
  { 
    if(selectedAssign == null || selectedAssign.trim().length()==0 || selectedAssign.equalsIgnoreCase("Default_0")) 
    { 
      noAssignWarn = true; 
      return null; 
    } 
    else 
      noAssignWarn = false; 
    if(gradePoint == null || gradePoint.trim().length()==0) 
    { 
      noGradeWarn = true; 
      return null; 
    } 
    else 
      noGradeWarn = false; 
     
    try 
    { 
      if(selectedAssign != null && selectedAssign.trim().length()>0) 
      { 
        if(!selectedAssign.equalsIgnoreCase("Default_0")) 
        { 
          if(gradePoint != null && gradePoint.trim().length()>0) 
          { 
            GradebookService gradebookService = (org.sakaiproject.service.gradebook.shared.GradebookService) 
            ComponentManager.get("org.sakaiproject.service.gradebook.GradebookService"); 
            String selectedAssignName = ((SelectItem)assignments.get((new Integer(selectedAssign)).intValue())).getLabel(); 
            String tempName = UserDirectoryService.getUser(selectedMessage.getMessage().getAuthor()).getId(); 
            gradebookService.setAssignmentScore(ToolManager.getCurrentPlacement().getContext(),  
                selectedAssignName,  
                UserDirectoryService.getUser(selectedMessage.getMessage().getAuthor()).getId(),  
                new Double(gradePoint),  
                ""); 
             
            if(gradeNotify) 
            { 
              PrivateMessageManager pvtMsgManager = (PrivateMessageManager) ComponentManager.get( 
              "org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager"); 
              User sendTo = UserDirectoryService.getUser(selectedMessage.getMessage().getAuthor()); 
              Set sendToSet = new HashSet(); 
              sendToSet.add(sendTo); 
               
              PrivateMessage pvtMsg = messageManager.createPrivateMessage() ; 
 
              pvtMsg.setTitle(selectedMessage.getMessage().getTitle()); 
              String msgBody = ""; 
              for(int i=0; i<assignments.size(); i++) 
              { 
                if(selectedAssign.equalsIgnoreCase((String)((SelectItem)assignments.get(i)).getValue())) 
                { 
                  msgBody = msgBody.concat("ASSIGNMENT: " + ((SelectItem)assignments.get(i)).getLabel() + " <br/> "); 
                  break; 
                } 
              } 
              msgBody = msgBody.concat("GRADE: " + gradePoint + " <br/> "); 
              msgBody = msgBody.concat("COMMENTS: " + gradeComment + " <br/> <br/> "); 
              msgBody = msgBody.concat(selectedMessage.getMessage().getBody() + " <br/> "); 
              pvtMsg.setBody(msgBody); 
              pvtMsg.setAuthor(UserDirectoryService.getCurrentUser().getId()); 
              pvtMsg.setApproved(Boolean.TRUE); 
              pvtMsg.setModified(new Date()); 
              pvtMsg.setModifiedBy(UserDirectoryService.getCurrentUser().getId()); 
               
              pvtMsgManager.sendPrivateMessage(pvtMsg, sendToSet, false); 
            } 
          } 
        } 
      } 
    } 
    catch(Exception e) 
    { 
      LOG.error("DiscussionForumTool - processDfGradeSubmit:" + e); 
      e.printStackTrace(); 
    } 
     
    gradeNotify = false; 
    assignments.clear(); 
    selectedAssign = "Default_0"; 
    gradePoint = ""; 
    gradebookScore = ""; 
    gradeComment = ""; 
    noAssignWarn = false; 
    noGradeWarn = false; 
    return MESSAGE_VIEW; 
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
  
  public boolean getErrorSynch()
  {
  	return errorSynch;
  }
  
  public void setErrorSynch(boolean errorSynch)
  {
  	this.errorSynch = errorSynch;
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

  public List getSiteMembers()
  {
    return getSiteMembers(true);
  }
  public List getSiteRoles()
  {
    return getSiteMembers(false);
  }

  public List getSiteMembers(boolean includeGroup)
  {
    LOG.debug("getSiteMembers()");
        
    if(siteMembers!=null && siteMembers.size()>0)
    {
      return siteMembers;
    }
    
    permissions=new ArrayList();
    
    Set membershipItems = null;
    
    if (PERMISSION_MODE_TEMPLATE.equals(getPermissionMode())){
    	membershipItems = forumManager.getDiscussionForumArea().getMembershipItemSet();
    }
    else if (PERMISSION_MODE_FORUM.equals(getPermissionMode())){    	
    	membershipItems = selectedForum.getForum().getMembershipItemSet();
    	
    	if (membershipItems == null || membershipItems.size() == 0){
    		membershipItems = forumManager.getDiscussionForumArea().getMembershipItemSet();
    	}
    }
    else if (PERMISSION_MODE_TOPIC.equals(getPermissionMode())){    	
    	membershipItems = selectedTopic.getTopic().getMembershipItemSet();
    	
    	if (membershipItems == null || membershipItems.size() == 0){
    		//membershipItems = forumManager.getDiscussionForumArea().getMembershipItemSet();
    		if (selectedForum != null && selectedForum.getForum() != null){
    		  membershipItems = selectedForum.getForum().getMembershipItemSet();
    		}
    	}
    } 
    	            
    siteMembers=new ArrayList(); 
    // get Roles     
    AuthzGroup realm;
    Site currentSite = null;
    int i=0;
    try
    {      
      realm = AuthzGroupService.getAuthzGroup(getContextSiteId());
      Set roles1 = realm.getRoles();
      if (roles1 != null && roles1.size() > 0)
      {
        Iterator roleIter = roles1.iterator();
        while (roleIter.hasNext())
        {
          Role role = (Role) roleIter.next();
          if (role != null) 
          {
            if(i==0)
            {
              selectedRole = role.getId();
              i=1;
            }
            DBMembershipItem item = forumManager.getAreaDBMember(membershipItems, role.getId(), DBMembershipItem.TYPE_ROLE);
            siteMembers.add(new SelectItem(role.getId(), role.getId() + "("+item.getPermissionLevelName()+")"));
            permissions.add(new PermissionBean(item, permissionLevelManager));
          }
        }
      }  
        
      if(includeGroup)
      {
     currentSite = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());   
      
     Collection groups = currentSite.getGroups();    
      for (Iterator groupIterator = groups.iterator(); groupIterator.hasNext();)
      {
        Group currentGroup = (Group) groupIterator.next();  
        DBMembershipItem item = forumManager.getAreaDBMember(membershipItems,currentGroup.getTitle(), DBMembershipItem.TYPE_GROUP);
        siteMembers.add(new SelectItem(currentGroup.getTitle(), currentGroup.getTitle() + " ("+item.getPermissionLevel().getName()+")"));
        permissions.add(new PermissionBean(item, permissionLevelManager));
      }
      }
    }
    catch (IdUnusedException e)
    {
      LOG.error(e.getMessage(), e);
    }   

    return siteMembers;
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
    selectedForum = new DiscussionForumBean(forum, uiPermissionsManager, forumManager);
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
  
 
  public void processPost(){
  	
  }
  
  public String generatePermissionScript(){
  	  	    	
  	PermissionLevel ownerLevel = permissionLevelManager.getDefaultOwnerPermissionLevel();
  	PermissionLevel authorLevel = permissionLevelManager.getDefaultAuthorPermissionLevel();
  	PermissionLevel noneditingAuthorLevel = permissionLevelManager.getDefaultNoneditingAuthorPermissionLevel();
  	PermissionLevel reviewerLevel = permissionLevelManager.getDefaultReviewerPermissionLevel();
  	PermissionLevel noneLevel = permissionLevelManager.getDefaultNonePermissionLevel();
  	PermissionLevel contributorLevel = permissionLevelManager.getDefaultContributorPermissionLevel();
  	  	
  	StringBuffer sBuffer = new StringBuffer();  	
  	sBuffer.append("<script type=\"text/javascript\">\n");   	  	
  	sBuffer.append("var ownerLevelArray = " + ownerLevel + ";\n");
  	sBuffer.append("var authorLevelArray = " + authorLevel + ";\n");
  	sBuffer.append("var noneditingAuthorLevelArray = " + noneditingAuthorLevel + ";\n");
  	sBuffer.append("var reviewerLevelArray = " + reviewerLevel + ";\n");
  	sBuffer.append("var noneLevelArray = " + noneLevel + ";\n");
  	sBuffer.append("var contributorLevelArray = " + contributorLevel + ";\n");
  	sBuffer.append("var owner = 'Owner';\n");
  	sBuffer.append("var author = 'Author';\n");
  	sBuffer.append("var nonEditingAuthor = 'Nonediting Author';\n");
  	sBuffer.append("var reviewer = 'Reviewer';\n");
  	sBuffer.append("var none = 'None';\n");
  	sBuffer.append("var contributor = 'Contributor';\n");  	
  	sBuffer.append("var custom = 'Custom';\n");
  	sBuffer.append("var all = 'All';\n");
  	sBuffer.append("var own = 'Own';\n");  	  	
  	
  	sBuffer.append("function checkLevel(selectedLevel){\n" +  			           
  			           "  var ownerVal = true;\n" +
  			           "  var authorVal = true;\n" +
  			           "  var noneditingAuthorVal = true;\n" +
  			           "  var reviewerVal = true;\n" +
  			           "  var noneVal = true;\n" +
  			           "  var contributorVal = true;\n\n" +  			           
  			           "  for (var i = 0; i < selectedLevel.length; i++){\n" +
  			           "    if (ownerVal && ownerLevelArray[i] != selectedLevel[i])\n" +
  	               "      ownerVal = false;\n" +
  			           "    if (authorVal && authorLevelArray[i] != selectedLevel[i])\n" +
  	               "      authorVal = false;\n" +
  	               "    if (noneditingAuthorVal && noneditingAuthorLevelArray[i] != selectedLevel[i])\n" +
  	               "      noneditingAuthorVal = false;\n" +
  	               "    if (reviewerVal && reviewerLevelArray[i] != selectedLevel[i])\n" +
  	               "      reviewerVal = false;\n" +
  	               "    if (noneVal && noneLevelArray[i] != selectedLevel[i])\n" +
  	               "      noneVal = false;\n" +
  	               "    if (contributorVal && contributorLevelArray[i] != selectedLevel[i])\n" +
  	               "      contributorVal = false;\n" +
  	               "  }\n\n" +  	  	    
  	               "  if (ownerVal)\n" +  	               
  	               "    return 'Owner';\n" +  	               
  	               "  else if (authorVal)\n" +  	               
  	               "    return 'Author';\n" +
  	               "  else if (noneditingAuthorVal)\n" +  	               
  	               "    return 'Nonediting Author';\n" + 
  	               "  else if (reviewerVal)\n" +
  	               "    return 'Reviewer';\n" +
  	               "  else if (noneVal)\n" +
  	               "    return 'None';\n" +
  	               "  else if (contributorVal)\n" +
  	               "    return 'Contributor';\n" +
  	               "  else return 'Custom';\n" +
  	               "}\n"
  	);
  			              	
  	sBuffer.append("</script>");  	
  	return sBuffer.toString();
  }
  
  public void setObjectPermissions(Object target){
  	Set membershipItemSet = null;
    
  	DiscussionForum forum = null;
  	Area area = null;
  	Topic topic = null;
  	
    /** get membership item set */    
    if (target instanceof DiscussionForum){
    	forum = ((DiscussionForum) target);
    	membershipItemSet = forum.getMembershipItemSet();
    }
    else if (target instanceof Area){
    	area = ((Area) target);
    	membershipItemSet = area.getMembershipItemSet();
    }
    else if (target instanceof Topic){
    	topic = ((Topic) target);
    	membershipItemSet = topic.getMembershipItemSet();
    }
     
    if (membershipItemSet != null){
      membershipItemSet.clear();
    }
    else{
    	membershipItemSet = new HashSet();
    }
        
    if(permissions!=null ){
      Iterator iter = permissions.iterator();
      while (iter.hasNext())
      {
        PermissionBean permBean = (PermissionBean) iter.next();
        DBMembershipItem membershipItem = permissionLevelManager.createDBMembershipItem(permBean.getItem().getName(), permBean.getSelectedLevel(), DBMembershipItem.TYPE_ROLE);
        
        
        if (PermissionLevelManager.PERMISSION_LEVEL_NAME_CUSTOM.equals(membershipItem.getPermissionLevelName())){
          PermissionsMask mask = new PermissionsMask();                
          mask.put(PermissionLevel.NEW_FORUM, new Boolean(permBean.getNewForum())); 
          mask.put(PermissionLevel.NEW_TOPIC, new Boolean(permBean.getNewTopic()));
          mask.put(PermissionLevel.NEW_RESPONSE, new Boolean(permBean.getNewResponse()));
          mask.put(PermissionLevel.NEW_RESPONSE_TO_RESPONSE, new Boolean(permBean.getResponseToResponse()));
          mask.put(PermissionLevel.MOVE_POSTING, new Boolean(permBean.getMovePosting()));
          mask.put(PermissionLevel.CHANGE_SETTINGS,new Boolean(permBean.getChangeSettings()));
          mask.put(PermissionLevel.POST_TO_GRADEBOOK, new Boolean(permBean.getPostToGradebook()));
          mask.put(PermissionLevel.READ, new Boolean(permBean.getRead()));
          mask.put(PermissionLevel.MARK_AS_READ,new Boolean(permBean.getMarkAsRead()));
          mask.put(PermissionLevel.MODERATE_POSTINGS, new Boolean(permBean.getModeratePostings()));
          mask.put(PermissionLevel.DELETE_OWN, new Boolean(permBean.getDeleteOwn()));
          mask.put(PermissionLevel.DELETE_ANY, new Boolean(permBean.getDeleteAny()));
          mask.put(PermissionLevel.REVISE_OWN, new Boolean(permBean.getReviseOwn()));
          mask.put(PermissionLevel.REVISE_ANY, new Boolean(permBean.getReviseAny()));
          
          PermissionLevel level = permissionLevelManager.createPermissionLevel(permBean.getSelectedLevel(), typeManager.getCustomLevelType(), mask);
          membershipItem.setPermissionLevel(level);
        }
                
        // save DBMembershiptItem here to get an id so we can add to the set
        permissionLevelManager.saveDBMembershipItem(membershipItem);
        membershipItemSet.add(membershipItem);
      }
      
      if (target instanceof DiscussionForum){
      	forum.setMembershipItemSet(membershipItemSet);
      	//forumManager.saveForum(forum);
      }
      else if (area != null){
      	area.setMembershipItemSet(membershipItemSet);
      	//areaManager.saveArea(area);
      }
      else if (topic != null){
      	topic.setMembershipItemSet(membershipItemSet);
      	//forumManager.saveTopic((DiscussionTopic) topic);
      }
    }
    siteMembers = null;
  }
  
  /**
   * processActionAddGroupsUsers
   * @return navigation String
   */
  public String processActionAddGroupsUsers(){
  	
  	totalGroupsUsersList = null;
  	
  	ExternalContext exContext = FacesContext.getCurrentInstance().getExternalContext();
    HttpSession session = (HttpSession) exContext.getSession(false);
    		
    String attr = null;

    if (session != null){
    	/** get navigation string of previous navigation (set by navigation handler) */
    	attr = (String) session.getAttribute("MC_PREVIOUS_NAV");	
    }
		    
    /** store caller navigation string in session (used to return from add groups/users) */
    session.setAttribute("MC_ADD_GROUPS_USERS_CALLER", attr);
                  
  	return "addGroupsUsers";
  }
  
  /**
   * processAddGroupsUsersSubmit
   * @return navigation String
   */
  public String processAddGroupsUsersSubmit(){
  	
  	
  	ExternalContext exContext = FacesContext.getCurrentInstance().getExternalContext();
    HttpSession session = (HttpSession) exContext.getSession(false);
    	
    /** get navigation string of previous navigation (set by navigation handler) */
    return (String) session.getAttribute("MC_ADD_GROUPS_USERS_CALLER");    
  }
  
  /**
   * processAddGroupsUsersCancel
   * @return navigation String
   */
  public String processAddGroupsUsersCancel(){
  	
  	ExternalContext exContext = FacesContext.getCurrentInstance().getExternalContext();
    HttpSession session = (HttpSession) exContext.getSession(false);
    	
    /** get navigation string of previous navigation (set by navigation handler) */
    return (String) session.getAttribute("MC_ADD_GROUPS_USERS_CALLER");
  }
  
  public List getTotalGroupsUsersList()
  { 
    
    /** protect from jsf calling multiple times */
    if (totalGroupsUsersList != null){
      return totalGroupsUsersList;
    }
         
    courseMemberMap = membershipManager.getAllCourseMembers(true, false, false);
 
    List members = membershipManager.convertMemberMapToList(courseMemberMap);
    totalGroupsUsersList = new ArrayList();
    
    /** create a list of SelectItem elements */
    for (Iterator i = members.iterator(); i.hasNext();){
      
      MembershipItem item = (MembershipItem) i.next();     
      totalGroupsUsersList.add(
        new SelectItem(item.getId(), item.getName()));
    }
    
    return totalGroupsUsersList;       
  }
 
	public void setPermissionLevelManager(
			PermissionLevelManager permissionLevelManager) {
		this.permissionLevelManager = permissionLevelManager;
	}
    
     public List getPostingOptions()
      {
        List postingOptions = new ArrayList();
        postingOptions.add(new SelectItem(PermissionBean.NONE,PermissionBean.NONE));
        postingOptions.add(new SelectItem(PermissionBean.OWN,PermissionBean.OWN));
        postingOptions.add(new SelectItem(PermissionBean.ALL,PermissionBean.ALL));    
        
        return postingOptions;
      }
     
     /**
      * @return Returns the levels.
      */
     public List getLevels()
     {
       boolean hasCustom = false;
       if (levels == null || levels.size() == 0)
       {
         levels = new ArrayList();
         List origLevels = permissionLevelManager.getOrderedPermissionLevelNames();
         if (origLevels != null)
         {
           Iterator iter = origLevels.iterator();

           while (iter.hasNext())
           {
             String level = (String) iter.next();
             levels.add(new SelectItem(level));
             if(level.equals("Custom"))
                 {
                   hasCustom =true;
                 }
           }
         }
         if(!hasCustom)
         {
           levels.add(new SelectItem("Custom"));
         }
       }       
       return levels;
     }

    /**
     * @param areaManager The areaManager to set.
     */
    public void setAreaManager(AreaManager areaManager)
    {
      this.areaManager = areaManager;
    }

    /**
     * @return Returns the selectedRole.
     */
    public String getSelectedRole()
    {
      return selectedRole;
    }

    /**
     * @param selectedRole The selectedRole to set.
     */
    public void setSelectedRole(String selectedRole)
    {
      this.selectedRole = selectedRole;
    }

		public boolean getEditMode() {
			return editMode;
		}

		public void setEditMode(boolean editMode) {
			this.editMode = editMode;
		}

		public String getPermissionMode() {
			return permissionMode;
		}

		public void setPermissionMode(String permissionMode) {
			this.permissionMode = permissionMode;
		}

		public List getSelectedGroupsUsersList() {
			return selectedGroupsUsersList;
		}

		public void setSelectedGroupsUsersList(List selectedGroupsUsersList) {
			this.selectedGroupsUsersList = selectedGroupsUsersList;
		}		

		public void setTotalGroupsUsersList(List totalGroupsUsersList) {
			this.totalGroupsUsersList = totalGroupsUsersList;
		}              
}