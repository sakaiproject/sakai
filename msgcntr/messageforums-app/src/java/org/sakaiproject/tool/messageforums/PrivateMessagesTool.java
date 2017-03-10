/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-app/src/java/org/sakaiproject/tool/messageforums/PrivateMessagesTool.java $
 * $Id: PrivateMessagesTool.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.messageforums;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.validator.EmailValidator;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.DBMembershipItem;
import org.sakaiproject.api.app.messageforums.DefaultPermissionsManager;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.api.app.messageforums.HiddenGroup;
import org.sakaiproject.api.app.messageforums.MembershipManager;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.PrivateForum;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.api.app.messageforums.PrivateMessageRecipient;
import org.sakaiproject.api.app.messageforums.PrivateTopic;
import org.sakaiproject.api.app.messageforums.SynopticMsgcntrManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.UserPreferencesManager;
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.PermissionsHelper;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.app.messageforums.MembershipItem;
import org.sakaiproject.component.app.messageforums.dao.hibernate.HiddenGroupImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateMessageImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateTopicImpl;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.LearningResourceStoreService;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Actor;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Object;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Statement;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Verb;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Verb.SAKAI_VERB;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.messageforums.ui.DecoratedAttachment;
import org.sakaiproject.tool.messageforums.ui.PermissionBean;
import org.sakaiproject.tool.messageforums.ui.PrivateForumDecoratedBean;
import org.sakaiproject.tool.messageforums.ui.PrivateMessageDecoratedBean;
import org.sakaiproject.tool.messageforums.ui.PrivateTopicDecoratedBean;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;

public class PrivateMessagesTool
{
  
  private static final Logger LOG = LoggerFactory.getLogger(PrivateMessagesTool.class);

  private static final String MESSAGECENTER_PRIVACY_URL = "messagecenter.privacy.url";
  private static final String MESSAGECENTER_PRIVACY_TEXT = "messagecenter.privacy.text";

  private static final String MESSAGECENTER_BUNDLE = "org.sakaiproject.api.app.messagecenter.bundle.Messages";
  private static final String PERMISSIONS_BUNDLE = "org.sakaiproject.api.app.messagecenter.bundle.permissions";
 
  private static final ResourceLoader rb = new ResourceLoader(MESSAGECENTER_BUNDLE);
  
  /**
   * List individual private messages details
   */
  private static final String REPLY_SUBJECT_PREFIX = "pvt_reply_prefix";
  private static final String FORWARD_SUBJECT_PREFIX = "pvt_forward_prefix";
  //sakai-reply allF
  private static final String ReplyAll_SUBJECT_PREFIX = "pvt_replyall_prefix";
  private static final String ALERT = "pvt_alert";
  private static final String NO_MATCH_FOUND = "pvt_no_match_found";
  private static final String MISSING_BEG_END_DATE = "pvt_missing_date_range";
  private static final String CREATE_DIFF_FOLDER_NAME = "pvt_create_diff_folder_name";
  private static final String FOLDER_NAME_BLANK = "pvt_folder_name_blank";
  private static final String ENTER_FOLDER_NAME = "pvt_enter_new_folder_name";
  private static final String ENTER_SHORTER_NAME = "pvt_enter_shorter_folder_name";
  private static final String CONFIRM_FOLDER_DELETE = "pvt_delete_folder_confirm";
  private static final String CANNOT_DEL_REVISE_FOLDER = "pvt_no_delete_revise_folder";
  private static final String PROVIDE_VALID_EMAIL = "pvt_provide_email_addr";
  private static final String CONFIRM_PERM_MSG_DELETE = "pvt_confirm_perm_msg_delete";
  private static final String SELECT_MSGS_TO_DELETE = "pvt_select_msgs_to_delete";
  private static final String SELECT_RECIPIENT_LIST_FOR_REPLY = "pvt_select_reply_recipients_list";
  private static final String MISSING_SUBJECT = "pvt_missing_subject";
  private static final String MISSING_SUBJECT_DRAFT = "pvt_missing_subject_draft";
  private static final String SELECT_MSG_RECIPIENT = "pvt_select_msg_recipient";
  private static final String MULTIPLE_WINDOWS = "pvt_multiple_windows";
  
  private static final String CONFIRM_MSG_DELETE = "pvt_confirm_msg_delete";
  private static final String ENTER_SEARCH_TEXT = "pvt_enter_search_text";
  private static final String MOVE_MSG_ERROR = "pvt_move_msg_error";
  private static final String NO_MARKED_READ_MESSAGE = "pvt_no_message_mark_read";
  private static final String NO_MARKED_DELETE_MESSAGE = "pvt_no_message_mark_delete";
  private static final String NO_MARKED_MOVE_MESSAGE = "pvt_no_message_mark_move";
  private static final String MULTIDELETE_SUCCESS_MSG = "pvt_deleted_success";
  private static final String PERM_DELETE_SUCCESS_MSG = "pvt_perm_deleted_success";
  
  public static final String RECIPIENTS_UNDISCLOSED = "pvt_bccUndisclosed";
  
  /** Used to determine if this is combined tool or not */
  private static final String MESSAGECENTER_TOOL_ID = "sakai.messagecenter";
  private static final String MESSAGECENTER_HELPER_TOOL_ID = "sakai.messageforums.helper";
  private static final String MESSAGES_TOOL_ID = "sakai.messages";
  private static final String FORUMS_TOOL_ID = "sakai.forums";
  
  /**
   *Dependency Injected 
   */
  private PrivateMessageManager prtMsgManager;
  private MessageForumsMessageManager messageManager;
  private MessageForumsForumManager forumManager;
  private ErrorMessages errorMessages;
  private MembershipManager membershipManager;
  private SynopticMsgcntrManager synopticMsgcntrManager;
  private UserPreferencesManager userPreferencesManager;
  
  /** Dependency Injected   */
  private MessageForumsTypeManager typeManager;
  private ContentHostingService contentHostingService;
 
  /** Navigation for JSP   */
  public static final String MAIN_PG="main";
  public static final String DISPLAY_MESSAGES_PG="pvtMsg";
  public static final String SELECTED_MESSAGE_PG="pvtMsgDetail";
  public static final String COMPOSE_MSG_PG="compose";
  public static final String COMPOSE_FROM_PG="msgForum:mainOrHp";
  public static final String MESSAGE_SETTING_PG="pvtMsgSettings";
  public static final String MESSAGE_FOLDER_SETTING_PG="pvtMsgFolderSettings";
  public static final String SEARCH_RESULT_MESSAGES_PG="pvtMsgEx";
  public static final String DELETE_MESSAGES_PG="pvtMsgDelete";
  public static final String DELETE_FOLDER_PG="pvtMsgFolderDelete";
  public static final String MESSAGE_STATISTICS_PG="pvtMsgStatistics";
  public static final String MESSAGE_HOME_PG="pvtMsgHpView";
  public static final String MESSAGE_REPLY_PG="pvtMsgReply";

  public static final String MESSAGE_FORWARD_PG="pvtMsgForward";
  
  //sakai-huxt pvtMsgReplyAll
  public static final String MESSAGE_ReplyAll_PG="pvtMsgReplyAll";
  
  public static final String DELETE_MESSAGE_PG="pvtMsgDelete";
  public static final String REVISE_FOLDER_PG="pvtMsgFolderRevise";
  public static final String MOVE_MESSAGE_PG="pvtMsgMove";
  public static final String ADD_FOLDER_IN_FOLDER_PG="pvtMsgFolderInFolderAdd";
  public static final String ADD_MESSAGE_FOLDER_PG="pvtMsgFolderAdd";
  public static final String PVTMSG_COMPOSE = "pvtMsgCompose";
  
  
  //need to modified to support internationalization by huxt
  /** portlet configuration parameter values**/
  public static final String PVTMSG_MODE_RECEIVED = "pvt_received";
  public static final String PVTMSG_MODE_SENT = "pvt_sent";
  public static final String PVTMSG_MODE_DELETE = "pvt_deleted";
  public static final String PVTMSG_MODE_DRAFT = "pvt_drafts";
  public static final String PVTMSG_MODE_CASE = "Personal Folders";
  
  public static final String RECIPIANTS_ENTIRE_CLASS= "All Participants";
  public static final String RECIPIANTS_ALL_INSTRUCTORS= "All Instructors";
  
  public static final String SET_AS_YES="yes";
  public static final String SET_AS_NO="no";    
  
  public static final String THREADED_VIEW = "threaded";
  
  //huxt
  public static final String EXTERNAL_TOPIC_ID = "pvtMsgTopicId";
  public static final String EXTERNAL_WHICH_TOPIC = "selectedTopic";
  
  PrivateForumDecoratedBean decoratedForum;
  
  private List aggregateList = new ArrayList();
  
  private Area area;
  private PrivateForum forum;  
  private List<PrivateTopic> pvtTopics=new ArrayList<PrivateTopic>();
  private List decoratedPvtMsgs;
  //huxt
  private String msgNavMode="privateMessages" ;//============
  private PrivateMessageDecoratedBean detailMsg ;
  private boolean viewChanged = false;
  
  private String currentMsgUuid; //this is the message which is being currently edited/displayed/deleted
  private List selectedItems;
  
  private String userName;    //current user
  private Date time ;       //current time
  
  //delete confirmation screen - single delete 
  private boolean deleteConfirm=false ; //used for displaying delete confirmation message in same jsp
  private boolean validEmail=true ;
  
  //Compose Screen-webpage
  private List selectedComposeToList = new ArrayList();
  private List selectedComposeBccList = new ArrayList();
  private String composeSendAsPvtMsg=SET_AS_YES; // currently set as Default as change by user is allowed
  private boolean booleanEmailOut= Boolean.parseBoolean(ServerConfigurationService.getString("mc.messages.ccEmailDefault", "false"));
  private String composeSubject ;
  private String composeBody;
  private String selectedLabel="pvt_priority_normal" ;   //defautl set
  private List totalComposeToList = null;
  private List totalComposeToBccList = null;
  private List totalComposeToListRecipients;
  
  //Delete items - Checkbox display and selection - Multiple delete
  private List selectedDeleteItems;
  private boolean multiDeleteSuccess;
  private String multiDeleteSuccessMsg;
  private List totalDisplayItems=new ArrayList();
  
  // Move to folder - Checkbox display and selection - Multiple move to folder
  private List selectedMoveToFolderItems;
  
  //reply to 
  private String replyToBody;
  private String replyToSubject;
  
  
  
  
  //forwarding
  private String forwardBody;
  private String forwardSubject;
  
  
//reply to all-huxt

  private String replyToAllBody;
  private String replyToAllSubject;
  
  //Setting Screen
  private String activatePvtMsg=SET_AS_NO; 
  private String forwardPvtMsg=SET_AS_NO;
  private String forwardPvtMsgEmail;
  private boolean superUser; 
  private String sendToEmail;
  
  //message header screen
  private String searchText="";
  private String selectView;
  
  //return to previous page after send msg
  private String fromMainOrHp = null;
  
  // for compose, are we coming from main page?
  private boolean fromMain;
  
  // Message which will be marked as replied
  private PrivateMessage replyingMessage;
  
  //////////////////////
  
  //=====================need to be modified to support internationalization - by huxt
  /** The configuration mode, received, sent,delete, case etc ... */
  public static final String STATE_PVTMSG_MODE = "pvtmsg.mode";
  
  private Map courseMemberMap;
  
  
  public static final String SORT_SUBJECT_ASC = "subject_asc";
  public static final String SORT_SUBJECT_DESC = "subject_desc";
  public static final String SORT_AUTHOR_ASC = "author_asc";
  public static final String SORT_AUTHOR_DESC = "author_desc";
  public static final String SORT_DATE_ASC = "date_asc";
  public static final String SORT_DATE_DESC = "date_desc";
  public static final String SORT_LABEL_ASC = "label_asc";
  public static final String SORT_LABEL_DESC = "label_desc";
  public static final String SORT_TO_ASC = "to_asc";
  public static final String SORT_TO_DESC = "to_desc";
  public static final String SORT_ATTACHMENT_ASC = "attachment_asc";
  public static final String SORT_ATTACHMENT_DESC = "attachment_desc";
  
  private boolean selectedComposedlistequalCurrentuser=false;
  
  /** sort member */
  private String sortType = SORT_DATE_DESC;
  
  private int setDetailMsgCount = 0;
  
  private static final String PERMISSIONS_PREFIX = "msg.";
  
  private boolean instructor = false;
  
  private List<SelectItem> nonHiddenGroups = new ArrayList<SelectItem>();
  private List<HiddenGroup> hiddenGroups = new ArrayList();
  private static final String DEFAULT_NON_HIDDEN_GROUP_ID = "-1";
  private String DEFAULT_NON_HIDDEN_GROUP_TITLE = "hiddenGroups_selectGroup";
  private String selectedNonHiddenGroup = DEFAULT_NON_HIDDEN_GROUP_ID;
  private static final String PARAM_GROUP_ID = "groupId";
  private boolean currentSiteHasGroups = false;
  private Boolean displayHiddenGroupsMsg = null;
  
  private boolean showProfileInfoMsg = false;
  private boolean showProfileLink = false;
  
  public PrivateMessagesTool()
  {    
	  showProfileInfoMsg = ServerConfigurationService.getBoolean("msgcntr.messages.showProfileInfo", true);
	  showProfileLink = showProfileInfoMsg && ServerConfigurationService.getBoolean("profile2.profile.link.enabled", true);
  }
  
  /**
   * @return
   */
  public MessageForumsTypeManager getTypeManager()
  { 
    return typeManager;
  }


  /**
   * @param prtMsgManager
   */
  public void setPrtMsgManager(PrivateMessageManager prtMsgManager)
  {
    this.prtMsgManager = prtMsgManager;
  }
  
  /**
   * @param messageManager
   */
  public void setMessageManager(MessageForumsMessageManager messageManager)
  {
    this.messageManager = messageManager;
  }
  
  /**
   * @param membershipManager
   */
  public void setMembershipManager(MembershipManager membershipManager)
  {
    this.membershipManager = membershipManager;
  }

  
  /**
   * @param typeManager
   */
  public void setTypeManager(MessageForumsTypeManager typeManager)
  {
    this.typeManager = typeManager;
  }

  public void initializePrivateMessageArea()
  {           
    /** get area per request */
	area = prtMsgManager.getPrivateMessageArea();
    
    
    if (! area.getEnabled() && isMessages()) {
    	area.setEnabled(true);
    }
    
    // reset these in case the allowed recipients (such as hidden groups) was updated
    totalComposeToList = null;
    totalComposeToBccList = null;
    displayHiddenGroupsMsg = null;
    
    if (getUserId() != null && (getPvtAreaEnabled() || isInstructor() || isEmailPermit())){      
      PrivateForum pf = prtMsgManager.initializePrivateMessageArea(area, aggregateList);
      pf = prtMsgManager.initializationHelper(pf, area);
      pvtTopics = pf.getTopics();
      Collections.sort(pvtTopics, PrivateTopicImpl.TITLE_COMPARATOR);   //changed to date comparator
      forum=pf;
      activatePvtMsg = (Boolean.TRUE.equals(area.getEnabled())) ? SET_AS_YES : SET_AS_NO;
      sendToEmail = area.getSendToEmail() + "";
      forwardPvtMsg = (Boolean.TRUE.equals(pf.getAutoForward())) ? SET_AS_YES : SET_AS_NO;
      forwardPvtMsgEmail = pf.getAutoForwardEmail();
      hiddenGroups = new ArrayList<HiddenGroup>();
      if(area != null && area.getHiddenGroups() != null){
	for(Iterator itor = area.getHiddenGroups().iterator(); itor.hasNext();){
    	  HiddenGroup group = (HiddenGroup) itor.next();
    	  hiddenGroups.add(group);
	}
      }
    } 
  }
  
  /**
   * Property created rather than setErrorMessage for design requirement
   * @return
   */
  public boolean isDispError()
  {
    if (isInstructor() && !getPvtAreaEnabled())
    {
      return true;
    }
    else
    {
      return false;
    }
  }
  
  public boolean getPvtAreaEnabled()
  {  
    if(area == null) {
    	initializePrivateMessageArea();
    }
	  return area.getEnabled().booleanValue();
  } 
  
  
  /**
   * 
   * @return true if a copy of the message is always sent to the recipient email address(es)
   * per the site-wide setting
   */
  public boolean isEmailCopyAlways() {
      if (area == null) {
          initializePrivateMessageArea();
      }
      
      
      return !isEmailCopyDisabled() && area.getSendToEmail() == Area.EMAIL_COPY_ALWAYS;
  }
  
  public boolean isEmailCopyDisabled(){
	  return ServerConfigurationService.getBoolean("mc.messages.ccEmailDisabled", false);
  }
  
  /**
   * 
   * @return true if the sender may choose whether a copy of the message is sent to recipient
   * email address(es)
   */
  public boolean isEmailCopyOptional() {
      if (area == null) {
          initializePrivateMessageArea();
      }
      
      return !isEmailCopyDisabled() && area.getSendToEmail() == Area.EMAIL_COPY_OPTIONAL;
  }
  
  
  public boolean isEmailForwardDisabled(){
	  return ServerConfigurationService.getBoolean("mc.messages.forwardEmailDisabled", false);
  }
  
  public boolean isShowSettingsLink(){
	  if(isInstructor()){
		  //if the site has groups, then show the settings link b/c there
		  //are settings for groups
		  if(getCurrentSiteHasGroups()){
			  return true;
		  }else{
			  //if no groups and all email settings are disabled, there's no
			  //settings to show, so don't show the link
			  return !isEmailForwardDisabled() || !isEmailCopyDisabled();
		  }
	  }else{
		  //students only see forward options, if it's hidden, don't show this link
		  return !isEmailForwardDisabled();
	  }
  }
  
  //Return decorated Forum
  public PrivateForumDecoratedBean getDecoratedForum()
  {      
      PrivateForumDecoratedBean decoratedForum = new PrivateForumDecoratedBean(getForum()) ;
      
      
      /** only load topics/counts if area is enabled */	    	
	    
      int totalUnreadMessages = 0;
    	  
      if (getPvtAreaEnabled() && decoratedForum.getForum() != null){  
    	  
    	int countForFolderNum = 0;// only three folder 
    	Iterator<PrivateTopic> iterator = pvtTopics.iterator(); 
    	//MSGCNTR-472 we need the first three but need to guard against there being < 3 elements
        for (int i = 0;i < 3 && iterator.hasNext(); i++)//only three times
        {
          PrivateTopic topic = (PrivateTopic) iterator.next();
          
          if (topic != null)
          {
          	
          	/** filter topics by context and type*/                                                    
            if (topic.getTypeUuid() != null
            		&& topic.getTypeUuid().equals(typeManager.getUserDefinedPrivateTopicType())
            	  && topic.getContextId() != null && !topic.getContextId().equals(prtMsgManager.getContextId())){
               continue;
            }       
          	
            PrivateTopicDecoratedBean decoTopic= new PrivateTopicDecoratedBean(topic) ;
           
            // folder uuid
            String typeUuid = getPrivateMessageTypeFromContext(topic.getTitle());
             
            countForFolderNum++;
            
            decoTopic.setTotalNoMessages(prtMsgManager.findMessageCount(typeUuid, aggregateList));

            decoTopic.setUnreadNoMessages(prtMsgManager.findUnreadMessageCount(typeUuid, aggregateList));
            totalUnreadMessages += decoTopic.getUnreadNoMessages();
          
            decoratedForum.addTopic(decoTopic);
          }       
        }
        
        while(iterator.hasNext())//add more folders 
        {
               PrivateTopic topic = (PrivateTopic) iterator.next();
               if (topic != null)
               {

               
               /** filter topics by context and type*/                                                    
                 if (topic.getTypeUuid() != null
                 && topic.getTypeUuid().equals(typeManager.getUserDefinedPrivateTopicType())
                   && topic.getContextId() != null && !topic.getContextId().equals(prtMsgManager.getContextId())){
                    continue;
                 }       
               
                 PrivateTopicDecoratedBean decoTopic= new PrivateTopicDecoratedBean(topic) ;
                
                 String typeUuid = getPrivateMessageTypeFromContext(topic.getTitle());          
               
                 decoTopic.setTotalNoMessages(prtMsgManager.findMessageCount(typeUuid, aggregateList));
                 decoTopic.setUnreadNoMessages(prtMsgManager.findUnreadMessageCount(typeUuid,aggregateList));
                 totalUnreadMessages += decoTopic.getUnreadNoMessages();
                 decoratedForum.addTopic(decoTopic);
               }          
        
        }

      }//if  getPvtAreaEnabled()
      
      
      //update syntopic info:
      if(getUserId() != null){
    	  setMessagesSynopticInfoHelper(getUserId(), getSiteId(), totalUnreadMessages, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
      }
      
    return decoratedForum ;
  }

  public void setMessagesSynopticInfoHelper(String userId, String siteId, int unreadMessagesCount, int numOfAttempts) {
	  try {
		  getSynopticMsgcntrManager().setMessagesSynopticInfoHelper(userId, siteId, unreadMessagesCount);
	  } catch (HibernateOptimisticLockingFailureException holfe) {

		  // failed, so wait and try again
		  try {
			  Thread.sleep(SynopticMsgcntrManager.OPT_LOCK_WAIT);
		  } catch (InterruptedException e) {
			  e.printStackTrace();
		  }

		  numOfAttempts--;

		  if (numOfAttempts <= 0) {
			  System.out
			  .println("PrivateMessagesTool: setMessagesSynopticInfoHelper: HibernateOptimisticLockingFailureException no more retries left");
			  holfe.printStackTrace();
		  } else {
			  System.out
			  .println("PrivateMessagesTool: setMessagesSynopticInfoHelper: HibernateOptimisticLockingFailureException: attempts left: "
					  + numOfAttempts);
			  setMessagesSynopticInfoHelper(userId, siteId, 
					  unreadMessagesCount, numOfAttempts);
		  }
	  }

  }
  
  public List getDecoratedPvtMsgs()
  {
  	/** 
  	    avoid apply_request_values and render_response from calling this method on postback
  	    solution -- only call during render_response phase
  	    8/29/07 JLR - if coming from the synoptic tool, we need to process
  	*/
	  
  	if (!FacesContext.getCurrentInstance().getRenderResponse() && !viewChanged &&
  			getExternalParameterByKey(EXTERNAL_WHICH_TOPIC) == null) { 
  		return decoratedPvtMsgs;
  	}
  	
	if(selectView!=null && selectView.equalsIgnoreCase(THREADED_VIEW))
    {
    	this.rearrageTopicMsgsThreaded(false);
    	return decoratedPvtMsgs;
    }
  	
  	// coming from synoptic view, need to change the msgNavMode
	if (msgNavMode == null)
	{//=======Recibidios by huxt
		msgNavMode = (getExternalParameterByKey(EXTERNAL_WHICH_TOPIC) == null) ? //"selectedTopic"
						forumManager.getTopicByUuid(getExternalParameterByKey(EXTERNAL_TOPIC_ID)).getTitle() ://"pvtMsgTopicid"
						getExternalParameterByKey(EXTERNAL_WHICH_TOPIC);
	}
	
  	decoratedPvtMsgs=new ArrayList();
  	String typeUuid;
  	
   	typeUuid = getPrivateMessageTypeFromContext(msgNavMode);//=======Recibidios by huxt

  	/** support for sorting */
  	/* if the view was changed to "All Messages", we want to retain the previous
  	 * sort setting. Otherwise, the user has selected a different sort setting.
  	 * Also retain sort setting if user has hit "Check All"
  	 */
  	if ((!viewChanged || sortType == null) && !selectAll)
  	{
  		String sortColumnParameter = getExternalParameterByKey("sortColumn");

  		if ("subject".equals(sortColumnParameter)){  		  		
  			sortType = (SORT_SUBJECT_ASC.equals(sortType)) ? SORT_SUBJECT_DESC : SORT_SUBJECT_ASC;  			 		
  		}
  		else if ("author".equals(sortColumnParameter)){  		  		
  			sortType = (SORT_AUTHOR_ASC.equals(sortType)) ? SORT_AUTHOR_DESC : SORT_AUTHOR_ASC;  			 		
  		}
  		else if ("date".equals(sortColumnParameter)){  		  		
  			sortType = (SORT_DATE_ASC.equals(sortType)) ? SORT_DATE_DESC : SORT_DATE_ASC;  			 		
  		}
  		else if ("label".equals(sortColumnParameter)){  		  		
  			sortType = (SORT_LABEL_ASC.equals(sortType)) ? SORT_LABEL_DESC : SORT_LABEL_ASC;  			 		
  		}
  		else if ("to".equals(sortColumnParameter)){  		  		
  			sortType = (SORT_TO_ASC.equals(sortType)) ? SORT_TO_DESC : SORT_TO_ASC;  			 		
  		}
  		else if ("attachment".equals(sortColumnParameter)){  		  		
  			sortType = (SORT_ATTACHMENT_ASC.equals(sortType)) ? SORT_ATTACHMENT_DESC : SORT_ATTACHMENT_ASC;  			 		
  		}
  		else{
  			sortType = SORT_DATE_DESC;
  		}
  	}

  	viewChanged = false; 
    
    /** add support for sorting */
    if (SORT_SUBJECT_ASC.equals(sortType)){
    	decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_SUBJECT,
          PrivateMessageManager.SORT_ASC);
    }
    else if (SORT_SUBJECT_DESC.equals(sortType)){
    	decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_SUBJECT,
          PrivateMessageManager.SORT_DESC);
    }
    else if (SORT_AUTHOR_ASC.equals(sortType)){
    	decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_AUTHOR,
          PrivateMessageManager.SORT_ASC);
    }        
    else if (SORT_AUTHOR_DESC.equals(sortType)){
    	decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_AUTHOR,
          PrivateMessageManager.SORT_DESC);
    }
    else if (SORT_DATE_ASC.equals(sortType)){
    	decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_DATE,
          PrivateMessageManager.SORT_ASC);
    }        
    else if (SORT_DATE_DESC.equals(sortType)){
    	decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_DATE,
          PrivateMessageManager.SORT_DESC);
    }
    else if (SORT_LABEL_ASC.equals(sortType)){
    	decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_LABEL,
          PrivateMessageManager.SORT_ASC);
    }        
    else if (SORT_LABEL_DESC.equals(sortType)){
    	decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_LABEL,
          PrivateMessageManager.SORT_DESC);
    }
    else if (SORT_TO_ASC.equals(sortType)){
        // the recipient list is stored as a CLOB in Oracle, so we cannot use the
    	// db query to obtain the sorted list - cannot order by CLOB
    	/*decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_TO,
          PrivateMessageManager.SORT_ASC);*/
    	decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_DATE,
    	          PrivateMessageManager.SORT_ASC);
    	Collections.sort(decoratedPvtMsgs, PrivateMessageImpl.RECIPIENT_LIST_COMPARATOR_ASC);
    }        
    else if (SORT_TO_DESC.equals(sortType)){
    	// the recipient list is stored as a CLOB in Oracle, so we cannot use the
    	// db query to obtain the sorted list - cannot order by CLOB
    	/*decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_TO,
          PrivateMessageManager.SORT_DESC);*/
    	decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_DATE,
    	          PrivateMessageManager.SORT_ASC);
    	Collections.sort(decoratedPvtMsgs, PrivateMessageImpl.RECIPIENT_LIST_COMPARATOR_DESC);
    }        
    else if (SORT_ATTACHMENT_ASC.equals(sortType)){
    	decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_ATTACHMENT,
          PrivateMessageManager.SORT_ASC);
    }        
    else if (SORT_ATTACHMENT_DESC.equals(sortType)){
    	decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_ATTACHMENT,
          PrivateMessageManager.SORT_DESC);
    }
    
    
    decoratedPvtMsgs = createDecoratedDisplay(decoratedPvtMsgs);    

    //pre/next message
    if(decoratedPvtMsgs != null )
    {
   		setMessageBeanPreNextStatus();
    }
    
    return decoratedPvtMsgs ;
  }
  
  private void setMessageBeanPreNextStatus()
  {
	  List tempMsgs = decoratedPvtMsgs;
      for(int i=0; i<tempMsgs.size(); i++)
      {
        PrivateMessageDecoratedBean dmb = (PrivateMessageDecoratedBean)tempMsgs.get(i);
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

  public void setDecoratedPvtMsgs(List displayPvtMsgs)
  {
    this.decoratedPvtMsgs=displayPvtMsgs;
  }
  
  public String getMsgNavMode() 
  {
    return msgNavMode ;
  }
 
  public PrivateMessageDecoratedBean getDetailMsg()
  {
    return detailMsg ;
  }

  public void setDetailMsg(PrivateMessageDecoratedBean detailMsg)
  {    
    this.detailMsg = detailMsg;
  }

  public String getCurrentMsgUuid()
  {
    return currentMsgUuid;
  }
  
  public void setCurrentMsgUuid(String currentMsgUuid)
  {
    this.currentMsgUuid = currentMsgUuid;
  }

  public List getSelectedItems()
  {
    return selectedItems;
  }
  
  public void setSelectedItems(List selectedItems)
  {
    this.selectedItems=selectedItems ;
  }
  
  public boolean isDeleteConfirm()
  {
    return deleteConfirm;
  }

  public void setDeleteConfirm(boolean deleteConfirm)
  {
    this.deleteConfirm = deleteConfirm;
  }

 
  public boolean isValidEmail()
  {
    return validEmail;
  }
  public void setValidEmail(boolean validEmail)
  {
    this.validEmail = validEmail;
  }
  
  //Deleted page - checkbox display and selection
  public List getSelectedDeleteItems()
  {
    return selectedDeleteItems;
  }
  public List getTotalDisplayItems()
  {
    return totalDisplayItems;
  }
  public void setTotalDisplayItems(List totalDisplayItems)
  {
    this.totalDisplayItems = totalDisplayItems;
  }
  public void setSelectedDeleteItems(List selectedDeleteItems)
  {
    this.selectedDeleteItems = selectedDeleteItems;
  }

  //Compose Getter and Setter
  public String getComposeBody()
  {
    return composeBody;
  }
  
  public void setComposeBody(String composeBody)
  {
    this.composeBody = composeBody;
  }

  public String getSelectedLabel()
  {
    return selectedLabel;
  }
  
  public void setSelectedLabel(String selectedLabel)
  {
    this.selectedLabel = selectedLabel;
  }
  
  public boolean getBooleanEmailOut() {
	  return booleanEmailOut;
  }
  
  public void setBooleanEmailOut(boolean booleanEmailOut) {
	  this.booleanEmailOut= booleanEmailOut;
  }
  
  public PrivateMessage getReplyingMessage() {
	  return replyingMessage;
  }
  
  public void setReplyingMessage(PrivateMessage replyingMessage) {
	  this.replyingMessage = replyingMessage;
  }
  
  /**
   * 
   * @return true if the Messages tool setting in combination with the author-defined
   * {@link #getBooleanEmailOut()} setting requires a copy of the message to be sent to 
   * recipient(s) email
   */
  public boolean isSendEmail() {
      boolean sendEmail;
      if (isEmailCopyAlways() ||
              (isEmailCopyOptional() && getBooleanEmailOut())) {
          sendEmail = true;
      } else {
          sendEmail = false;
      }

      return sendEmail;
  }

  public String getComposeSendAsPvtMsg()
  {
    return composeSendAsPvtMsg;
  }

  public void setComposeSendAsPvtMsg(String composeSendAsPvtMsg)
  {
    this.composeSendAsPvtMsg = composeSendAsPvtMsg;
  }

  public String getComposeSubject()
  {
    return composeSubject;
  }

  public void setComposeSubject(String composeSubject)
  {
    this.composeSubject = composeSubject;
  }

  public void setSelectedComposeToList(List selectedComposeToList)
  {
    this.selectedComposeToList = selectedComposeToList;
  }
  
  public void setSelectedComposeBccList(List selectedComposeBccList)
  {
	  this.selectedComposeBccList = selectedComposeBccList;
  }
  
  public void setTotalComposeToList(List totalComposeToList)
  {
    this.totalComposeToList = totalComposeToList;
  }
  
  public List getSelectedComposeToList()
  {
    return selectedComposeToList;
  }
  
  public List getSelectedComposeBccList()
  {
	  return selectedComposeBccList;
  }

  private String getSiteTitle(){	  
	  try {
		return SiteService.getSite(ToolManager.getCurrentPlacement().getContext()).getTitle();
	} catch (IdUnusedException e) {
		e.printStackTrace();
	}
	return "";
  }
  
  private String getSiteId() {
	  return ToolManager.getCurrentPlacement().getContext();
  }
    
  private String getContextSiteId() 
  {
	 return "/site/" + ToolManager.getCurrentPlacement().getContext();
  }
  
  public List getTotalComposeToList()
  { 
      if (totalComposeToList == null) {
          initializeComposeToLists();
      }

      List<SelectItem> selectItemList = new ArrayList<SelectItem>();
      for (Iterator i = totalComposeToList.iterator(); i.hasNext();) {
          MembershipItem item = (MembershipItem) i.next();
          selectItemList.add(new SelectItem(item.getId(), item.getName()));
      }

      return selectItemList;              
  }

  public List getTotalComposeToBccList() {
      if (totalComposeToBccList == null) {
          initializeComposeToLists();
      }

      List<SelectItem> selectItemList = new ArrayList<SelectItem>();
      for (Iterator i = totalComposeToBccList.iterator(); i.hasNext();) {
          MembershipItem item = (MembershipItem) i.next();
          selectItemList.add(new SelectItem(item.getId(), item.getName()));
      }

      return selectItemList;
  }
  
  /**
   * Since the courseMemberMap generates new uuids each time it is called, and
   * these uuids are used to identify the recipients of the message when the user
   * sends the message, we need to do the logic for the "To" and "Bcc" lists together, 
   * utilizing the same courseMemberMap. This will set the values for the
   * totalComposeToList and totalComposeToBccList.
   */
  private void initializeComposeToLists() {
      totalComposeToList = new ArrayList();
      totalComposeToBccList = new ArrayList();
      
      List<String> hiddenGroupIds = getHiddenGroupIds(area.getHiddenGroups());
      courseMemberMap = membershipManager.getFilteredCourseMembers(true, getHiddenGroupIds(area.getHiddenGroups()));
      List members = membershipManager.convertMemberMapToList(courseMemberMap);
      
      List<SelectItem> selectItemList = new ArrayList<SelectItem>();
      // we need to filter out the hidden groups since they will only appear as recipients in the bcc list
      for (Iterator i = members.iterator(); i.hasNext();) {
          MembershipItem item = (MembershipItem) i.next();
          if (hiddenGroupIds != null && item.getGroup() != null && hiddenGroupIds.contains(item.getGroup().getTitle())) {
              // hidden groups only appear in the bcc list
              totalComposeToBccList.add(item);
          } else {
              totalComposeToList.add(item);
              totalComposeToBccList.add(item);
          }
      }  
  }

  public boolean isDisplayHiddenGroupsMsg() {
      if (displayHiddenGroupsMsg == null) {
          displayHiddenGroupsMsg = hiddenGroups != null && !hiddenGroups.isEmpty() && prtMsgManager.isAllowToViewHiddenGroups();
      }
      
      return displayHiddenGroupsMsg;
  }
  
  private List<String> getHiddenGroupIds(Set hiddenGroups){
	  List<String> returnList = new ArrayList<String>();
	  
	  if(hiddenGroups != null){
		  for(Iterator itor = hiddenGroups.iterator(); itor.hasNext();){
	    	  HiddenGroup group = (HiddenGroup) itor.next();
	    	  returnList.add(group.getGroupId());
		  }
	  }
	  
	  return returnList;
  }
  
  /**
   * 
   * @param id
   * @return
   */
  public String getUserSortNameById(String id){    
    try
    {
      User user=UserDirectoryService.getUser(id) ;
      if (ServerConfigurationService.getBoolean("msg.displayEid", true))
      {
    	  userName= user.getSortName() + " (" + user.getDisplayId() + ")";
      }
      else
      {
    	  userName= user.getSortName();
      }
    }
    catch (UserNotDefinedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    return userName;
  }

  public String getUserName() {
   String userId=SessionManager.getCurrentSessionUserId();
   try
   {
     User user=UserDirectoryService.getUser(userId) ;
     if (ServerConfigurationService.getBoolean("msg.displayEid", true))
     {
    	 userName= user.getDisplayName() + " (" + user.getDisplayId() + ")";
     }
     else {
    	 userName= user.getDisplayName();
     }   
   }
   catch (UserNotDefinedException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
   return userName;
  }
  
  public String getUserId()
  {
    return SessionManager.getCurrentSessionUserId();
  }
  
  public TimeZone getUserTimeZone() {
	  return userPreferencesManager.getTimeZone();
  }
  
  //Reply time
  public Date getTime()
  {
    return new Date();
  }
  //Reply to page
  public String getReplyToBody() {
    return replyToBody;
  }
  public void setReplyToBody(String replyToBody) {
    this.replyToBody=replyToBody;
  }
  public String getReplyToSubject()
  {
    return replyToSubject;
  }
  public void setReplyToSubject(String replyToSubject)
  {
    this.replyToSubject = replyToSubject;
  }
  
  // Forward a message
  public String getForwardBody() {
    return forwardBody;
  }
  public void setForwardBody(String forwardBody) {
    this.forwardBody=forwardBody;
  }
  public String getForwardSubject()
  {
    return forwardSubject;
  }
  public void setForwardSubject(String forwardSubject)
  {
    this.forwardSubject = forwardSubject;
  }
  


  //message header Getter 
  public String getSearchText()
  {
    return searchText ;
  }
  public void setSearchText(String searchText)
  {
    this.searchText=searchText;
  }
  public String getSelectView() 
  {
    return selectView ;
  }
  public void setSelectView(String selectView)
  {
    this.selectView=selectView ;
  }
  
  public boolean isMultiDeleteSuccess() 
  {
	return multiDeleteSuccess;
  }

  public void setMultiDeleteSuccess(boolean multiDeleteSuccess) 
  {
	this.multiDeleteSuccess = multiDeleteSuccess;
  }


  public String getMultiDeleteSuccessMsg() 
  {
	return multiDeleteSuccessMsg;
  }

  public void setMultiDeleteSuccessMsg(String multiDeleteSuccessMsg) 
  {
	this.multiDeleteSuccessMsg = multiDeleteSuccessMsg;
  }

public boolean isFromMain() {
	return fromMain;
}

public String getServerUrl() {
    return ServerConfigurationService.getServerUrl();
 }

public boolean getShowProfileInfoMsg() {
    return showProfileInfoMsg;
}

public boolean getShowProfileLink() {
	return showProfileLink;
}

public void processChangeSelectView(ValueChangeEvent eve)
  {
    multiDeleteSuccess = false;
    String currentValue = (String) eve.getNewValue();
  	if (!currentValue.equalsIgnoreCase(THREADED_VIEW) && selectView != null && selectView.equals(THREADED_VIEW))
  	{
  		selectView = "";
  		viewChanged = true;
  		getDecoratedPvtMsgs();
  		return;
    }
  	else if (currentValue.equalsIgnoreCase(THREADED_VIEW))
  	{
  		selectView = THREADED_VIEW;
  		if (searchPvtMsgs != null && !searchPvtMsgs.isEmpty())
  			this.rearrageTopicMsgsThreaded(true);
  		else
  			this.rearrageTopicMsgsThreaded(false);
  		return;
  	}
  }
  
  public void rearrageTopicMsgsThreaded(boolean searcModeOn)
  {  
	  List msgsList = new ArrayList();

	  if(searcModeOn)
	  {
		  for(int i=0; i<searchPvtMsgs.size(); i++)
		  {
			  msgsList.add((PrivateMessageDecoratedBean)searchPvtMsgs.get(i));
		  }
		  searchPvtMsgs.clear();
	  }else
	  {
		  // always start with the decorated pm in ascending date order
		  String typeUuid = getPrivateMessageTypeFromContext(msgNavMode);
		  decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_DATE,
		          PrivateMessageManager.SORT_ASC);
		  decoratedPvtMsgs = createDecoratedDisplay(decoratedPvtMsgs);
		  
		  for(int i=0; i<decoratedPvtMsgs.size(); i++)
		  {
			  msgsList.add((PrivateMessageDecoratedBean)decoratedPvtMsgs.get(i));
		  }   
		  decoratedPvtMsgs.clear();
	  }


	  if(msgsList != null)
	  {
		  List tempMsgsList = new ArrayList();
		  for(int i=0; i<msgsList.size(); i++)
		  {
			  tempMsgsList.add((PrivateMessageDecoratedBean)msgsList.get(i));
		  }
		  Iterator iter = tempMsgsList.iterator();
		  while(iter.hasNext())
		  {
			  List allRelatedMsgs = messageManager.getAllRelatedMsgs(
					  ((PrivateMessageDecoratedBean)iter.next()).getMsg().getId());
			  List currentRelatedMsgs = new ArrayList();
			  if(allRelatedMsgs != null && allRelatedMsgs.size()>0)
			  {
				  Long msgId = ((Message)allRelatedMsgs.get(0)).getId();
				  PrivateMessage pvtMsg= (PrivateMessage) prtMsgManager.getMessageById(msgId);
				  PrivateMessageDecoratedBean pdb = new PrivateMessageDecoratedBean(pvtMsg);
				  pdb.setDepth(-1);
				  boolean firstEleAdded = false;
				  for(int i=0; i<msgsList.size(); i++)
				  {
					  PrivateMessageDecoratedBean tempPMDB = (PrivateMessageDecoratedBean)msgsList.get(i);
					  if (tempPMDB.getMsg().getId().equals(pdb.getMsg().getId()))
					  {
						  tempPMDB.setDepth(0);
						  currentRelatedMsgs.add(tempPMDB);
						  firstEleAdded = true;
						  recursiveGetThreadedMsgsFromList(msgsList, allRelatedMsgs, currentRelatedMsgs, tempPMDB);
						  break;
					  }
				  }
				  if(!firstEleAdded)
					  recursiveGetThreadedMsgsFromList(msgsList, allRelatedMsgs, currentRelatedMsgs, pdb);
			  }
			  for(int i=0; i<currentRelatedMsgs.size(); i++)
			  {
				  if(searcModeOn)
				  {
					  searchPvtMsgs.add((PrivateMessageDecoratedBean)currentRelatedMsgs.get(i));
				  }else
				  {
					  decoratedPvtMsgs.add((PrivateMessageDecoratedBean)currentRelatedMsgs.get(i));
				  }

				  tempMsgsList.remove((PrivateMessageDecoratedBean)currentRelatedMsgs.get(i));
			  }

			  iter = tempMsgsList.iterator();
		  }
	  }

	  setMessageBeanPreNextStatus();

  }
  
  private void recursiveGetThreadedMsgsFromList(List msgsList, 
  		List allRelatedMsgs, List returnList,
      PrivateMessageDecoratedBean currentMsg)
  {
    for (int i = 0; i < allRelatedMsgs.size(); i++)
    {
      Long msgId = ((Message)allRelatedMsgs.get(i)).getId();
	  PrivateMessage pvtMsg= (PrivateMessage) prtMsgManager.getMessageById(msgId);
	  PrivateMessageDecoratedBean thisMsgBean = new PrivateMessageDecoratedBean(pvtMsg);

      Message thisMsg = thisMsgBean.getMsg();
      boolean existedInCurrentUserList = false;
      for(int j=0; j< msgsList.size(); j++)
      {
      	PrivateMessageDecoratedBean currentUserBean = 
      		(PrivateMessageDecoratedBean)msgsList.get(j);
        if (thisMsg.getInReplyTo() != null
            && thisMsg.getInReplyTo().getId().equals(
                currentMsg.getMsg().getId())
						&& currentUserBean.getMsg().getId().equals(thisMsg.getId()))
        {
          currentUserBean.setDepth(currentMsg.getDepth() + 1);
          if(currentMsg.getDepth() > -1)
          {
          	currentUserBean.setUiInReply(((PrivateMessageDecoratedBean)returnList.get(returnList.size()-1)).getMsg());
          }
          returnList.add(currentUserBean);
          existedInCurrentUserList = true;
          recursiveGetThreadedMsgsFromList(msgsList, allRelatedMsgs, returnList, currentUserBean);
          break;
        }
      }
      if(!existedInCurrentUserList)
      {
        if(thisMsg.getInReplyTo() != null
            && thisMsg.getInReplyTo().getId().equals(
                currentMsg.getMsg().getId()))
        {
          thisMsgBean.setDepth(currentMsg.getDepth());
          recursiveGetThreadedMsgsFromList(msgsList, allRelatedMsgs, returnList, thisMsgBean);
        }
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////////////  
  /**
   * called when any topic like Received/Sent/Deleted clicked
   * @return - pvtMsg
   */
  private String selectedTopicTitle="";
  private String selectedTopicId="";
  public String getSelectedTopicTitle()
  {
    return selectedTopicTitle ;
  }
  public void setSelectedTopicTitle(String selectedTopicTitle) 
  {
    this.selectedTopicTitle=selectedTopicTitle;
  }
  public String getSelectedTopicId()
  {
    return selectedTopicId;
  }
  public void setSelectedTopicId(String selectedTopicId)
  {
    this.selectedTopicId=selectedTopicId;    
  }
  
  public String processActionHome()
  {
    LOG.debug("processActionHome()");
    msgNavMode = "privateMessages";
    multiDeleteSuccess = false;
    if (searchPvtMsgs != null)
    	searchPvtMsgs.clear();
    return  MAIN_PG;
  }  
  public String processActionPrivateMessages()
  {
    LOG.debug("processActionPrivateMessages()");                    
    msgNavMode = "privateMessages";            
    multiDeleteSuccess = false;
    if (searchPvtMsgs != null) 
    	searchPvtMsgs.clear();
    return  MESSAGE_HOME_PG;
  }        
  public String processDisplayForum()
  {
    LOG.debug("processDisplayForum()");
    if (searchPvtMsgs != null)
    	searchPvtMsgs.clear();
    return DISPLAY_MESSAGES_PG;
  }

  public String processDisplayMessages()
  {
    LOG.debug("processDisplayMessages()");
    if (searchPvtMsgs != null)
    	searchPvtMsgs.clear();
    return SELECTED_MESSAGE_PG;
  }
  
  public void initializeFromSynoptic()
  {
	  
	  if(getUserId() != null){
	  
	    /** reset sort type */
	    sortType = SORT_DATE_DESC;    
	    
	    setSelectedTopicId(getExternalParameterByKey(EXTERNAL_TOPIC_ID));
	   	selectedTopic = new PrivateTopicDecoratedBean(forumManager.getTopicByUuid(getExternalParameterByKey(EXTERNAL_TOPIC_ID)));
	    selectedTopicTitle = getExternalParameterByKey(EXTERNAL_WHICH_TOPIC);

	    //set prev/next topic details
	    PrivateForum pf = forumManager.getPrivateForumByOwnerAreaNull(getUserId());
	    
	    if (pf == null)
	    {
	    	initializePrivateMessageArea();
	    }
	    else
	    {
	    	pvtTopics = pf.getTopics();
	    	forum = pf;
	    }

	    msgNavMode=getSelectedTopicTitle();
	    
	    //set prev/next topic details
	    setPrevNextTopicDetails(msgNavMode);
	  }
  }
  
  public String processPvtMsgTopic()
  {
    LOG.debug("processPvtMsgTopic()");
    
    /** reset sort type */
    sortType = SORT_DATE_DESC;    
    
    setSelectedTopicId(getExternalParameterByKey(EXTERNAL_TOPIC_ID));
   	selectedTopic = new PrivateTopicDecoratedBean(forumManager.getTopicByUuid(getExternalParameterByKey(EXTERNAL_TOPIC_ID)));
   	selectedTopicTitle = forumManager.getTopicByUuid(getExternalParameterByKey(EXTERNAL_TOPIC_ID)).getTitle();
   	//"selectedTopicTitle"= "Recibidos"	
    msgNavMode=getSelectedTopicTitle();
    
    //set prev/next topic details
    setPrevNextTopicDetails(msgNavMode);// "Recibidos"	
    
    return DISPLAY_MESSAGES_PG;
  }
    
  /**
   * process Cancel from all JSP's
   * @return - pvtMsg
   */  
  public String processPvtMsgCancel() {
    LOG.debug("processPvtMsgCancel()");
    
    // Return to Messages & Forums page or Messages page
    if (isMessagesandForums()) {
    	return MAIN_PG;
    }
    else {
    	return MESSAGE_HOME_PG;
    }
  }
  
  public String processPvtMsgCancelToListView()
  {
  	return DISPLAY_MESSAGES_PG;
  }
  
  private String processPvtMsgDraft(){
	  //set up draft details:
	  PrivateMessage draft = prtMsgManager.initMessageWithAttachmentsAndRecipients(getDetailMsg().getMsg());
	  setDetailMsg(new PrivateMessageDecoratedBean(draft));
	  setComposeSubject(draft.getTitle());

	  setComposeBody(draft.getBody());
	  
	  ArrayList attachments = new ArrayList();
	  for (Attachment attachment : (List<Attachment>) draft.getAttachments()) {
		  attachments.add(new DecoratedAttachment(attachment));
	  }
	  setAttachments(attachments);
	  
	  setSelectedLabel(draft.getLabel());
	  
	  //go to compose page
	  setFromMainOrHp();
	  fromMain = ("".equals(msgNavMode)) || ("privateMessages".equals(msgNavMode));
	  LOG.debug("processPvtMsgDraft()");
	  return PVTMSG_COMPOSE;
  }

  /**
   * called when subject of List of messages to Topic clicked for detail
   * @return - pvtMsgDetail
   */ 
  public String processPvtMsgDetail() {
    LOG.debug("processPvtMsgDetail()");
    multiDeleteSuccess = false;

    String msgId=getExternalParameterByKey("current_msg_detail");
    setCurrentMsgUuid(msgId) ; 
    //retrive the detail for this message with currentMessageId    
    for (Iterator iter = decoratedPvtMsgs.iterator(); iter.hasNext();)
    {
      PrivateMessageDecoratedBean dMsg= (PrivateMessageDecoratedBean) iter.next();
      if (dMsg.getMsg().getId().equals(Long.valueOf(msgId)))
      {
    	  
        this.setDetailMsg(dMsg); 
        if(dMsg.getMsg().getDraft()){
        	return processPvtMsgDraft();
        }
        setDetailMsgCount++;
       
        prtMsgManager.markMessageAsReadForUser(dMsg.getMsg());
               
        PrivateMessage initPrivateMessage = prtMsgManager.initMessageWithAttachmentsAndRecipients(dMsg.getMsg());
        this.setDetailMsg(new PrivateMessageDecoratedBean(initPrivateMessage));
        setDetailMsgCount++;
        
        List recLs= initPrivateMessage.getRecipients();
        for (Iterator iterator = recLs.iterator(); iterator.hasNext();)
        {
          PrivateMessageRecipient element = (PrivateMessageRecipient) iterator.next();
          if (element != null)
          {
            if((element.getRead().booleanValue()) || (element.getUserId().equals(getUserId())) )
            {
             getDetailMsg().setHasRead(true) ;
            }
          }
        }
        if(dMsg.getMsg().getCreatedBy().equals(getUserId())){
        	//need to display all users who received the message if the user create the message
        	this.getDetailMsg().getMsg().setRecipientsAsTextBcc(dMsg.getMsg().getRecipientsAsTextBcc());        	
        }else{
        	//otherwise, hide the BCC information
        	this.getDetailMsg().getMsg().setRecipientsAsTextBcc("");
        }

        this.getDetailMsg().getMsg().setRecipientsAsText(dMsg.getMsg().getRecipientsAsText());
      }
    }
    this.deleteConfirm=false; //reset this as used for multiple action in same JSP
   
    //prev/next message 
    if(decoratedPvtMsgs != null)
    {
      for(int i=0; i<decoratedPvtMsgs.size(); i++)
      {
        PrivateMessageDecoratedBean thisDmb = (PrivateMessageDecoratedBean)decoratedPvtMsgs.get(i);
        if(((PrivateMessageDecoratedBean)decoratedPvtMsgs.get(i)).getMsg().getId().toString().equals(msgId))
        {
          detailMsg.setDepth(thisDmb.getDepth());
          detailMsg.setHasNext(thisDmb.getHasNext());
          detailMsg.setHasPre(thisDmb.getHasPre());
          break;
        }
      }
    }
    //default setting for moveTo
    moveToTopic=selectedTopicId;
    LearningResourceStoreService lrss = (LearningResourceStoreService) ComponentManager
            .get("org.sakaiproject.event.api.LearningResourceStoreService");
    Event event = EventTrackingService.newEvent("msgcntr", "read private message", true);
    if (null != lrss) {
    	try{
    		lrss.registerStatement(getStatementForUserReadPvtMsg(lrss.getEventActor(event), getDetailMsg().getMsg().getTitle()), "msgcntr");
    	}catch(Exception e){
    		LOG.error(e.getMessage(), e);
    	}
    }
    return SELECTED_MESSAGE_PG;
  }

  /**
   * navigate to "reply" a private message
   * @return - pvtMsgReply
   */ 
  public String processPvtMsgReply() {
    LOG.debug("processPvtMsgReply()");
    
    setDetailMsgCount = 0;

    if (getDetailMsg() == null)
    	return null;
    
    PrivateMessage pm = getDetailMsg().getMsg();
    
    // To mark as replied when user send the reply
    this.setReplyingMessage(pm);
    
    String title = pm.getTitle();
	if(title != null && !title.startsWith(getResourceBundleString(REPLY_SUBJECT_PREFIX)))
		replyToSubject = getResourceBundleString(REPLY_SUBJECT_PREFIX) + ' ' + title;
	else
		replyToSubject = title;

	// format the created date according to the setting in the bundle
    SimpleDateFormat formatter_date = new SimpleDateFormat(getResourceBundleString("date_format_date"), new ResourceLoader().getLocale());
	formatter_date.setTimeZone(TimeService.getLocalTimeZone());
	String formattedCreateDate = formatter_date.format(pm.getCreated());
	
	SimpleDateFormat formatter_date_time = new SimpleDateFormat(getResourceBundleString("date_format_time"), new ResourceLoader().getLocale());
	formatter_date_time.setTimeZone(TimeService.getLocalTimeZone());
	String formattedCreateTime = formatter_date_time.format(pm.getCreated());

	StringBuilder replyText = new StringBuilder();
    
    // populate replyToBody with the reply text
	replyText.append("<br /><br />");
	replyText.append("<span style=\"font-weight:bold;font-style:italic;\">");
	replyText.append(getResourceBundleString("pvt_msg_on"));
	replyText.append(" " + formattedCreateDate + " ");
	replyText.append(getResourceBundleString("pvt_msg_at"));
	replyText.append(" " +formattedCreateTime);
	replyText.append(getResourceBundleString("pvt_msg_comma"));
    replyText.append(" " + pm.getAuthor() + " ");
    replyText.append(getResourceBundleString("pvt_msg_wrote")); 
	replyText.append("</span>");
    	
    String origBody = pm.getBody();
    if (origBody != null && origBody.trim().length() > 0) {
    	replyText.append("<br />" + pm.getBody() + "<br />");
    }
    
    List attachList = getDetailMsg().getAttachList();
    if (attachList != null && attachList.size() > 0) {
    	for (Iterator attachIter = attachList.iterator(); attachIter.hasNext();) {
    		DecoratedAttachment decoAttach = (DecoratedAttachment) attachIter.next();
    		if (decoAttach != null) {
    			replyText.append("<span style=\"font-style:italic;\">");
    			replyText.append(getResourceBundleString("pvt_msg_["));
    			replyText.append(decoAttach.getAttachment().getAttachmentName() );
    			replyText.append(getResourceBundleString("pvt_msg_]") + "  ");
    			replyText.append("</span>");
    		}
    	}
    }
    
    this.setReplyToBody(replyText.toString());
    //from message detail screen
    this.setDetailMsg(getDetailMsg()) ;
    
    setDetailMsgCount++;
    
    return MESSAGE_REPLY_PG;
  }
  
  
  /**
   * navigate to "forward" a private message
   * @return - pvtMsgForward
   */ 
  public String processPvtMsgForward() {
	    LOG.debug("processPvtMsgForward()");
	    
	    setDetailMsgCount = 0;
	    
	    if (getDetailMsg() == null)
	    	return null;
	    
	    PrivateMessage pm = getDetailMsg().getMsg();
	    
	    String title = pm.getTitle();
    	if(title != null && !title.startsWith(getResourceBundleString(FORWARD_SUBJECT_PREFIX)))
    		forwardSubject = getResourceBundleString(FORWARD_SUBJECT_PREFIX) + ' ' + title;
    	else
    		forwardSubject = title;

    	// format the created date according to the setting in the bundle
	    SimpleDateFormat formatter = new SimpleDateFormat(getResourceBundleString("date_format"), new ResourceLoader().getLocale());
		formatter.setTimeZone(TimeService.getLocalTimeZone());
		String formattedCreateDate = formatter.format(pm.getCreated());
		
		StringBuilder forwardedText = new StringBuilder();
	    
	    // populate replyToBody with the forwarded text
		forwardedText.append(getResourceBundleString("pvt_msg_fwd_heading") + "<br /><br />" +
	    	getResourceBundleString("pvt_msg_fwd_authby", new Object[] {pm.getAuthor(), formattedCreateDate}) +  "<br />" +
	    	getResourceBundleString("pvt_msg_fwd_to", new Object[] {pm.getRecipientsAsText()}) + "<br />" +
	    	getResourceBundleString("pvt_msg_fwd_subject", new Object[] {pm.getTitle()}) + "<br />" +
	    	getResourceBundleString("pvt_msg_fwd_label", new Object[] {getDetailMsg().getLabel()}) + "<br />");
	    
	    List attachList = getDetailMsg().getAttachList();
	    if (attachList != null && attachList.size() > 0) {
	    	forwardedText.append(getResourceBundleString("pvt_msg_fwd_attachments") + "<br />");
	    	forwardedText.append("<ul style=\"list-style-type:none;margin:0;padding:0;padding-left:0.5em;\">");
	    	for (Iterator attachIter = attachList.iterator(); attachIter.hasNext();) {
	    		DecoratedAttachment decoAttach = (DecoratedAttachment) attachIter.next();
	    		if (decoAttach != null) {
	    			forwardedText.append("<li>");
	    			// It seems like there must be a better way to do the attachment image...
	    			String fileType = decoAttach.getAttachment().getAttachmentType();
	    			String imageUrl = null;
	    			if (fileType.equalsIgnoreCase("application/vnd.ms-excel"))
	    				imageUrl = "/messageforums-tool/images/excel.gif";
	    			else if (fileType.equalsIgnoreCase("text/html"))
	    				imageUrl = "/messageforums-tool/images/html.gif";
	    			else if (fileType.equalsIgnoreCase("application/pdf"))
	    				imageUrl = "/messageforums-tool/images/pdf.gif";
	    			else if (fileType.equalsIgnoreCase("application/vnd.ms-powerpoint"))
	    				imageUrl = "/messageforums-tool/images/ppt.gif";
	    			else if (fileType.equalsIgnoreCase("text/plain"))
	    				imageUrl = "/messageforums-tool/images/text.gif";
	    			else if (fileType.equalsIgnoreCase("application/msword"))
	    				imageUrl = "/messageforums-tool/images/word.gif";
	    			
	    			if (imageUrl != null) {
	    				forwardedText.append("<img alt=\"\" src=\"" + imageUrl + "\" />");
	    			}
	    			
	    			forwardedText.append("<a href=\"" + messageManager.getAttachmentUrl(decoAttach.getAttachment().getAttachmentId()) + "\" target=\"_blank\">" + decoAttach.getAttachment().getAttachmentName() + "</a></li>");
	    		}
	    	}
	    	forwardedText.append("</ul>");
	    }
	    String origBody = pm.getBody();
	    if (origBody != null && origBody.trim().length() > 0) {
	    	forwardedText.append(pm.getBody());
	    }
	    
	    this.setForwardBody(forwardedText.toString());
	    //from message detail screen
	    this.setDetailMsg(getDetailMsg()) ;
	    
	    setDetailMsgCount++;

	    return MESSAGE_FORWARD_PG;
	  }
	
  
//how many letters k in string a  a= "fdh,jlg,jds,lgjd"  k=","
private   int   getNum(char letter,   String   a)
{  
	int   j=0;  
	for(int   i=0;   i<a.length();   i++){  
		if(a.charAt(i)==(letter)){  //s.charAt(j) == 'x'
		j++;  
		}  
	}  
	return   j;  
}   
/////////////modified by hu2@iupui.edu  begin
  //function: add Reply All Tools

  /**
   * navigate to "Reply to all" a private message
   * @return - pvtMsgForward
   */ 
  public String processPvtMsgReplyAll() {
	    LOG.debug("processPvtMsgReplyAll()");
	    
	    setDetailMsgCount = 0;
	    
	    if (getDetailMsg() == null)
	    	return null;
	    
	    PrivateMessage pm = getDetailMsg().getMsg();
	    
	    // To mark as replied when user send the reply
	    this.setReplyingMessage(pm);
	    
	    String title = pm.getTitle();
    	if(title != null && !title.startsWith(getResourceBundleString(ReplyAll_SUBJECT_PREFIX)))
    		forwardSubject = getResourceBundleString(ReplyAll_SUBJECT_PREFIX) + ' ' + title;
    	else
    		forwardSubject = title;//forwardSubject


    	// format the created date according to the setting in the bundle
	    SimpleDateFormat formatter = new SimpleDateFormat(getResourceBundleString("date_format"), new ResourceLoader().getLocale());
		formatter.setTimeZone(TimeService.getLocalTimeZone());
		String formattedCreateDate = formatter.format(pm.getCreated());
		
		SimpleDateFormat formatter_date_time = new SimpleDateFormat(getResourceBundleString("date_format_time"), new ResourceLoader().getLocale());
		formatter_date_time.setTimeZone(TimeService.getLocalTimeZone());
		String formattedCreateTime = formatter_date_time.format(pm.getCreated());

		StringBuilder replyallText = new StringBuilder();
		
	    
	    // populate replyToBody with the reply text
		replyallText.append("<br /><br />");
		replyallText.append("<span style=\"font-weight:bold;font-style:italic;\">");
		replyallText.append(getResourceBundleString("pvt_msg_on"));
		replyallText.append(" " + formattedCreateDate + " ");
		replyallText.append(getResourceBundleString("pvt_msg_at"));
		replyallText.append(" " +formattedCreateTime);
		replyallText.append(getResourceBundleString("pvt_msg_comma"));
		replyallText.append(" " + pm.getAuthor() + " ");
		replyallText.append(getResourceBundleString("pvt_msg_wrote")); 
		replyallText.append("</span>");
	    	
	    String origBody = pm.getBody();
	    if (origBody != null && origBody.trim().length() > 0) {
	    	replyallText.append("<br />" + pm.getBody() + "<br />");
	    }
	    
	    List attachList = getDetailMsg().getAttachList();
	    if (attachList != null && attachList.size() > 0) {
	    	for (Iterator attachIter = attachList.iterator(); attachIter.hasNext();) {
	    		DecoratedAttachment decoAttach = (DecoratedAttachment) attachIter.next();
	    		if (decoAttach != null) {
	    			replyallText.append("<span style=\"font-style:italic;\">");
	    			replyallText.append(getResourceBundleString("pvt_msg_["));
	    			replyallText.append(decoAttach.getAttachment().getAttachmentName() );
	    			replyallText.append(getResourceBundleString("pvt_msg_]") + "  ");
	    			replyallText.append("</span>");
	    		}
	    	}
	    }
	    
	    this.setForwardBody(replyallText.toString());
	   	    
	    String msgautherString=getDetailMsg().getAuthor();
	    String msgCClistString=getDetailMsg().getRecipientsAsText();
	    
	    //remove the auther in Cc string 	    
	    if(msgCClistString.length()>=msgautherString.length())
	    {
	    String msgCClistStringwithoutAuthor = msgCClistString;	   
	    
	    String currentUserasAuther = getUserName();
	    char letter=';';
	    int  n=getNum(letter,msgCClistStringwithoutAuthor);
	    
	    int numberofAuther=0;
	    
	    if(n==0)
	    {numberofAuther=1;}
	    else if(n>=1)	    	
	    { numberofAuther=n+1;}//add the end ";"
	    String[] ccSS = new String[numberofAuther];
	    ccSS=msgCClistStringwithoutAuthor.split(";");
	  
	    StringBuffer tmpCC = new StringBuffer("");
	    
			if((numberofAuther>0)&&(numberofAuther<=msgCClistStringwithoutAuthor.length()))
					      {
					    
						    for(int indexCC =0;indexCC<numberofAuther;indexCC++)	    //last for ";"	
						    {
						    	
						    	
						    	if(!currentUserasAuther.replaceAll(" ", ", ").equals(msgautherString)){
						    		
							    	if(!ccSS[indexCC].trim().equals(currentUserasAuther.replaceAll(" ", ", "))&&(!ccSS[indexCC].trim().equals(msgautherString)))//not equal current auther and not equal old auther
							    	{						    		
							    		tmpCC.append(ccSS[indexCC].trim()).append("; ");
							    		
							    	}
						    	}
						    	
						    	else if(currentUserasAuther.replaceAll(" ", ", ").equals(msgautherString)){
						    		
						    		if(!ccSS[indexCC].trim().equals(currentUserasAuther.replaceAll(" ", ", "))||(!ccSS[indexCC].trim().equals(msgautherString)))//not equal current auther and not equal old auther
							    	{						    		
							    		tmpCC.append(ccSS[indexCC].trim()).append("; ");
							    		
							    	}
						    		
						    	}
						    	
						    	
						    }
						    
						    if(tmpCC.length()>1)
						    {
							    	tmpCC.delete(tmpCC.length()-2, tmpCC.length());//remove the ";"
							}
						 
						    getDetailMsg().setSendToStringDecorated(tmpCC.toString());
						    getDetailMsg().getMsg().setRecipientsAsText(tmpCC.toString());
						 
						  }
						    
	    
	    }
	    
	    //remove the bcc undiclosed place holder:
	    String ccList = getDetailMsg().getMsg().getRecipientsAsText();
	    if(ccList.contains(getResourceBundleString(RECIPIENTS_UNDISCLOSED) + "; ")){
	    	ccList = ccList.replaceAll(getResourceBundleString(RECIPIENTS_UNDISCLOSED) + "; ", "");
	    }else if(ccList.contains(getResourceBundleString(RECIPIENTS_UNDISCLOSED))){
	    	ccList = ccList.replaceAll(getResourceBundleString(RECIPIENTS_UNDISCLOSED), "");
	    }
	    if(ccList.endsWith(" ")){
	    	ccList = ccList.substring(0, ccList.length() -1);
	    }
	    if(ccList.endsWith(";")){
	    	ccList = ccList.substring(0, ccList.length() -1);
	    }
	    getDetailMsg().getMsg().setRecipientsAsText(ccList);
	    
	    this.setDetailMsg(getDetailMsg()) ;
	    
	    setDetailMsgCount++;
	  
	    return MESSAGE_ReplyAll_PG;//MESSAGE_FORWARD_PG;
	  }
	
  
  //////////modified by hu2@iupui.edu end
  /**
   * called from Single delete Page
   * @return - pvtMsgDetail
   */ 
  public String processPvtMsgDeleteConfirm() {
    LOG.debug("processPvtMsgDeleteConfirm()");
    
    this.setDeleteConfirm(true);
    setErrorMessage(getResourceBundleString(CONFIRM_MSG_DELETE));
    /*
     * same action is used for delete..however if user presses some other action after first
     * delete then 'deleteConfirm' boolean is reset
     */
    return SELECTED_MESSAGE_PG ;
  }
  
  /**
   * called from Single delete Page -
   * called when 'delete' button pressed second time
   * @return - pvtMsg
   */ 
  public String processPvtMsgDeleteConfirmYes() {
    LOG.debug("processPvtMsgDeleteConfirmYes()");
    if(getDetailMsg() != null)
    {      
      prtMsgManager.deletePrivateMessage(getDetailMsg().getMsg(), getPrivateMessageTypeFromContext(msgNavMode));      
    }
    return DISPLAY_MESSAGES_PG ;
  }
  
  //RESET form variable - required as the bean is in session and some attributes are used as helper for navigation
  public void resetFormVariable() {
    
    this.msgNavMode="" ;
    this.deleteConfirm=false;
    selectAll=false;
    attachments.clear();
    oldAttachments.clear();
  }
  
  /**
   * process Compose action from different JSP'S
   * @return - pvtMsgCompose
   */ 
  public String processPvtMsgCompose() {
	  //reset incase draft still has left over data
	  setDetailMsg(null);
	  attachments.clear();
	  oldAttachments.clear();
    setFromMainOrHp();
    fromMain = ("".equals(msgNavMode)) || ("privateMessages".equals(msgNavMode));
    LOG.debug("processPvtMsgCompose()");
    return PVTMSG_COMPOSE;
  }
  
  
  public String processPvtMsgComposeCancel()
  {
    LOG.debug("processPvtMsgComposeCancel()");
    resetComposeContents();
    if(("privateMessages").equals(getMsgNavMode()))
    {
    	return processPvtMsgReturnToMainOrHp();
    }
    else
    {
      return DISPLAY_MESSAGES_PG;
    } 
  }
  
  public void resetComposeContents()
  {
    this.setComposeBody("");
    this.setComposeSubject("");
    this.setComposeSendAsPvtMsg(SET_AS_YES); //set as default
    this.setBooleanEmailOut(Boolean.parseBoolean(ServerConfigurationService.getString("mc.messages.ccEmailDefault", "false"))); //set as default
    this.getSelectedComposeToList().clear();
    this.getSelectedComposeBccList().clear();
    this.setReplyToSubject("");
    this.setReplyToBody("");
    this.getAttachments().clear();
    this.getAllAttachments().clear();
    //reset label
    this.setSelectedLabel("pvt_priority_normal");
  }
  
  public String processPvtMsgPreview(){
	  
	  if(!hasValue(getComposeSubject()))
	  {
		  setErrorMessage(getResourceBundleString(MISSING_SUBJECT));
		  return null;
	  }

	  if(getSelectedComposeToList().size()<1 && getSelectedComposeBccList().size() < 1)
	  {
		  setErrorMessage(getResourceBundleString(SELECT_MSG_RECIPIENT));
		  return null ;
	  }
	  PrivateMessage msg;
	  if(getDetailMsg() != null && getDetailMsg().getMsg() != null && getDetailMsg().getMsg().getDraft()){
		  msg = constructMessage(false, getDetailMsg().getMsg());
		  msg.setDraft(Boolean.TRUE);
	  }else{
		  msg = constructMessage(false, null);
	  }
	  PrivateMessageDecoratedBean pmDb = new PrivateMessageDecoratedBean(msg);
	  pmDb.setIsPreview(true);
	  this.setDetailMsg(pmDb);
	  
	  return SELECTED_MESSAGE_PG;
  }
  
  public String processPvtMsgPreviewSend(){
	  return processPvtMsgSend();
  }
  
  public String processPvtMsgPreviewBack(){
	  return PVTMSG_COMPOSE;
  }
  
  /**
   * process from Compose screen
   * @return - pvtMsg
   */ 
  
  //Modified to support internatioalization -by huxt
  public String processPvtMsgSend() {
          
    LOG.debug("processPvtMsgSend()");
    
    if(!hasValue(getComposeSubject()))
    {
      setErrorMessage(getResourceBundleString(MISSING_SUBJECT));
      return null;
    }

    if(getSelectedComposeToList().size()<1)
    {
      setErrorMessage(getResourceBundleString(SELECT_MSG_RECIPIENT));
      return null ;
    }
    
    PrivateMessage pMsg = null;
    if(getDetailMsg() != null && getDetailMsg().getMsg() != null && getDetailMsg().getMsg().getDraft()){
    	pMsg = constructMessage(true, getDetailMsg().getMsg());
    }else{
    	pMsg= constructMessage(true, null) ;
    }
    
    pMsg.setExternalEmail(booleanEmailOut);
    Map<User, Boolean> recipients = getRecipients();
    
    prtMsgManager.sendPrivateMessage(pMsg, recipients, isSendEmail()); 
    // if you are sending a reply 
    Message replying = pMsg.getInReplyTo();
    if (replying!=null) {
    	replying = prtMsgManager.getMessageById(replying.getId());
    	if (replying!=null) {
    		prtMsgManager.markMessageAsRepliedForUser((PrivateMessage)replying);
    	}
    }
    
    //update synopticLite tool information:
    
    incrementSynopticToolInfo(recipients.keySet(), false);

    //reset contents
    resetComposeContents();
    
    LearningResourceStoreService lrss = (LearningResourceStoreService) ComponentManager
            .get("org.sakaiproject.event.api.LearningResourceStoreService");
    Event event = EventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_ADD, getEventMessage(pMsg), false);
    EventTrackingService.post(event);
    if (null != lrss) {
    	try{
    		lrss.registerStatement(getStatementForUserSentPvtMsg(lrss.getEventActor(event), pMsg.getTitle(), SAKAI_VERB.shared), "msgcntr");
    	}catch(Exception e){
    		LOG.error(e.getMessage(), e);
    	}
    }
    
    if(fromMainOrHp != null && !"".equals(fromMainOrHp))
    {
    	String tmpBackPage = fromMainOrHp;
    	fromMainOrHp = "";
    	return tmpBackPage;
    }
    else if(selectedTopic != null)
    {
    	msgNavMode=getSelectedTopicTitle();
    	setPrevNextTopicDetails(msgNavMode);

    	return DISPLAY_MESSAGES_PG;
    }

    // Return to Messages & Forums page or Messages page
    if (isMessagesandForums()) {
    	return MAIN_PG;
    }
    else {
    	return MESSAGE_HOME_PG;
    }
  }
     
  
  
  public void incrementSynopticToolInfo(Set<User> recipients, boolean updateCurrentUser){
  
	  String siteId = getSiteId();
	  String currentUser = getUserId();
	  List<String> userIds = new ArrayList<String>();
	  for (User user : recipients) {
		  if(updateCurrentUser || (!updateCurrentUser && !currentUser.equals(user.getId())))
			  userIds.add(user.getId());
	  }
	  incrementMessagesSynopticToolInfo(userIds, siteId, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
  }
  
  public void incrementMessagesSynopticToolInfo(List<String> userIds, String siteId, int numOfAttempts) {
		try {
			getSynopticMsgcntrManager().incrementMessagesSynopticToolInfo(userIds, siteId);
		} catch (HibernateOptimisticLockingFailureException holfe) {

			// failed, so wait and try again
			try {
				Thread.sleep(SynopticMsgcntrManager.OPT_LOCK_WAIT);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			numOfAttempts--;

			if (numOfAttempts <= 0) {
				System.out
						.println("PrivateMessagesTool: incrementMessagesSynopticToolInfo: HibernateOptimisticLockingFailureException no more retries left");
				holfe.printStackTrace();
			} else {
				System.out
						.println("PrivateMessagesTool: incrementMessagesSynopticToolInfo: HibernateOptimisticLockingFailureException: attempts left: "
								+ numOfAttempts);
				incrementMessagesSynopticToolInfo(userIds, siteId, numOfAttempts);
			}
		}

	}
		  
  
  /**
   * process from Compose screen
   * @return - pvtMsg
   */
  public String processPvtMsgSaveDraft() {
    LOG.debug("processPvtMsgSaveDraft()");
    if(!hasValue(getComposeSubject()))
    {
      setErrorMessage(getResourceBundleString(MISSING_SUBJECT_DRAFT));
      return null ;
    }
//    if(!hasValue(getComposeBody()) )
//    {
//      setErrorMessage("Please enter message body for this compose message.");
//      return null ;
//    }
//    if(getSelectedComposeToList().size()<1)
//    {
//      setErrorMessage(getResourceBundleString(SELECT_MSG_RECIPIENT));
//      return null ;
//    }
    PrivateMessage dMsg = null;
    if(getDetailMsg() != null && getDetailMsg().getMsg() != null && getDetailMsg().getMsg().getDraft()){
    	dMsg =constructMessage(true, getDetailMsg().getMsg()) ;
    }else{
    	dMsg =constructMessage(true, null) ;
    }
    dMsg.setDraft(Boolean.TRUE);
    dMsg.setDeleted(Boolean.FALSE);
    dMsg.setExternalEmail(booleanEmailOut);

    prtMsgManager.sendPrivateMessage(dMsg, getRecipients(), isSendEmail()); 

    //reset contents
    resetComposeContents();
    
    return returnDraftPage();
  }
  
  private String returnDraftPage(){
	  return processPvtMsgComposeCancel();
  }
  // created separate method as to be used with processPvtMsgSend() and processPvtMsgSaveDraft()
  public PrivateMessage constructMessage(boolean clearAttachments, PrivateMessage aMsg)
  {
	  if(aMsg == null){
		  aMsg = messageManager.createPrivateMessage();
	  }else{
		  //set the date for now:
		  aMsg.setCreated(new Date());
	  }
    
    if (aMsg != null)
    {
      StringBuilder alertMsg = new StringBuilder();
      aMsg.setTitle(getComposeSubject());
      aMsg.setBody(FormattedText.processFormattedText(getComposeBody(), alertMsg));
      
      aMsg.setAuthor(getAuthorString());
      aMsg.setDraft(Boolean.FALSE);      
      aMsg.setApproved(Boolean.FALSE);     
      aMsg.setLabel(getSelectedLabel());
      
      // this property added so can delete forum messages
      // since that and PM share same message object and
      // delete is not null....
      aMsg.setDeleted(Boolean.FALSE);
      
      // Add the recipientList as String for display in Sent folder
      // Any hidden users will be tacked on at the end
      StringBuffer sendToString = new StringBuffer("");
      StringBuffer sendToHiddenString = new StringBuffer("");
      
      if (selectedComposeToList.size() == 1) {
          MembershipItem membershipItem = (MembershipItem) courseMemberMap.get(selectedComposeToList.get(0));
          if (membershipItem != null) {
        	  sendToString.append(membershipItem.getName()).append("; ");
          }
      }
      else {
    	  for (int i = 0; i < selectedComposeToList.size(); i++)
    	  {
    		  MembershipItem membershipItem = (MembershipItem) courseMemberMap.get(selectedComposeToList.get(i));  
    		  if(membershipItem != null)
    		  {
    			  if (membershipItem.isViewable()) {
    				  sendToString.append(membershipItem.getName()).append("; ");
    			  }
    			  else {
    				  sendToHiddenString.append(membershipItem.getName()).append("; ");
    			  }
    		  }
    	  }
      }
      
      //add bcc recipients place holder:
      if(selectedComposeBccList.size() > 0 && !sendToString.toString().contains(getResourceBundleString(RECIPIENTS_UNDISCLOSED))){
    	  sendToString.append(getResourceBundleString(RECIPIENTS_UNDISCLOSED)).append("; ");
      }


      //create bcc string to use to display the user's who got BCC'ed
      StringBuffer sendToBccString = new StringBuffer("");
      StringBuffer sendToBccHiddenString = new StringBuffer("");      
      for (int i = 0; i < selectedComposeBccList.size(); i++)
      {
    	  MembershipItem membershipItem = (MembershipItem) courseMemberMap.get(selectedComposeBccList.get(i));  
    	  if(membershipItem != null)
    	  {
    		  if (membershipItem.isViewable()) {
    			  sendToBccString.append(membershipItem.getName()).append("; ");
    		  }
    		  else {
    			  sendToBccHiddenString.append(membershipItem.getName()).append("; ");
    		  }
    	  }
      }

      if (! "".equals(sendToString.toString())) {
    	  sendToString.delete(sendToString.length()-2, sendToString.length()); //remove last comma and space
      }
      
      if ("".equals(sendToHiddenString.toString())) {
    	  aMsg.setRecipientsAsText(sendToString.toString());
      }
      else {
    	  sendToHiddenString.delete(sendToHiddenString.length()-2, sendToHiddenString.length()); //remove last comma and space
    	  aMsg.setRecipientsAsText(sendToString.toString() + " " + PrivateMessage.HIDDEN_RECIPIENTS_START + sendToHiddenString.toString() + PrivateMessage.HIDDEN_RECIPIENTS_END);
      }
      //clean up sendToBccString
      if (! "".equals(sendToBccString.toString())) {
    	  sendToBccString.delete(sendToBccString.length()-2, sendToBccString.length()); //remove last comma and space
      }

      if ("".equals(sendToBccHiddenString.toString())) {
    	  aMsg.setRecipientsAsTextBcc(sendToBccString.toString());
      }
      else {
    	  sendToBccHiddenString.delete(sendToBccHiddenString.length()-2, sendToBccHiddenString.length()); //remove last comma and space
    	  aMsg.setRecipientsAsTextBcc(sendToBccString.toString() + " " + PrivateMessage.HIDDEN_RECIPIENTS_START + sendToBccHiddenString.toString() + PrivateMessage.HIDDEN_RECIPIENTS_END);
      }

    }
    //Add attachments
    for(int i=0; i<attachments.size(); i++)
    {
      prtMsgManager.addAttachToPvtMsg(aMsg, ((DecoratedAttachment)attachments.get(i)).getAttachment());         
    }    
    if(clearAttachments){
    	//clear
    	attachments.clear();
    	oldAttachments.clear();
    }
    
    return aMsg;    
  }
  ///////////////////// Previous/Next topic and message on Detail message page
  /**
   * Set Previous and Next message details with each PrivateMessageDecoratedBean
   */
  public void setPrevNextMessageDetails()
  {
    List tempMsgs = decoratedPvtMsgs;
    for(int i=0; i<tempMsgs.size(); i++)
    {
      PrivateMessageDecoratedBean dmb = (PrivateMessageDecoratedBean)tempMsgs.get(i);
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
  
  /**
   * processDisplayPreviousMsg()
   * Display the previous message from the list of decorated messages
   */
  public String processDisplayPreviousMsg()
  {
    List tempMsgs = getDecoratedPvtMsgs(); // all messages
    int currentMsgPosition = -1;
    if(tempMsgs != null)
    {
      for(int i=0; i<tempMsgs.size(); i++)
      {
        PrivateMessageDecoratedBean thisDmb = (PrivateMessageDecoratedBean)tempMsgs.get(i);
        if(detailMsg.getMsg().getId().equals(thisDmb.getMsg().getId()))
        {
          currentMsgPosition = i;
          break;
        }
      }
    }
    
    if(currentMsgPosition > 0)
    {
      PrivateMessageDecoratedBean thisDmb = (PrivateMessageDecoratedBean)tempMsgs.get(currentMsgPosition-1);
      PrivateMessage message= (PrivateMessage) prtMsgManager.getMessageById(thisDmb.getMsg().getId()); 
      
      detailMsg= new PrivateMessageDecoratedBean(message);
      //get attachments
      prtMsgManager.markMessageAsReadForUser(detailMsg.getMsg());
      
      PrivateMessage initPrivateMessage = prtMsgManager.initMessageWithAttachmentsAndRecipients(detailMsg.getMsg());
      this.setDetailMsg(new PrivateMessageDecoratedBean(initPrivateMessage));
      setDetailMsgCount++;
      
      List recLs= initPrivateMessage.getRecipients();
      for (Iterator iterator = recLs.iterator(); iterator.hasNext();)
      {
        PrivateMessageRecipient element = (PrivateMessageRecipient) iterator.next();
        if (element != null)
        {
          if((element.getRead().booleanValue()) || (element.getUserId().equals(getUserId())) )
          {
           getDetailMsg().setHasRead(true) ;
          }
        }
      }      
      getDetailMsg().setDepth(thisDmb.getDepth()) ;
      getDetailMsg().setHasNext(thisDmb.getHasNext());
      getDetailMsg().setHasPre(thisDmb.getHasPre()) ;

    }    
    LearningResourceStoreService lrss = (LearningResourceStoreService) ComponentManager
            .get("org.sakaiproject.event.api.LearningResourceStoreService");
    Event event = EventTrackingService.newEvent("msgcntr", "read private message", true);
    if (null != lrss) {
    	try{
    		lrss.registerStatement(getStatementForUserReadPvtMsg(lrss.getEventActor(event), getDetailMsg().getMsg().getTitle()), "msgcntr");
    	}catch(Exception e){
    		LOG.error(e.getMessage(), e);
    	}
    }
    return null;
  }

  /**
   * processDisplayNextMsg()
   * Display the Next message from the list of decorated messages
   */    
  public String processDisplayNextMsg()
  {
    List tempMsgs = getDecoratedPvtMsgs();
    int currentMsgPosition = -1;
    if(tempMsgs != null)
    {
      for(int i=0; i<tempMsgs.size(); i++)
      {
        PrivateMessageDecoratedBean thisDmb = (PrivateMessageDecoratedBean)tempMsgs.get(i);
        if(detailMsg.getMsg().getId().equals(thisDmb.getMsg().getId()))
        {
          currentMsgPosition = i;
          break;
        }
      }
    }
    
    if(currentMsgPosition > -2  && currentMsgPosition < (tempMsgs.size()-1))
    {
      PrivateMessageDecoratedBean thisDmb = (PrivateMessageDecoratedBean)tempMsgs.get(currentMsgPosition+1); 
      //get attachments
      prtMsgManager.markMessageAsReadForUser(thisDmb.getMsg());
      
      PrivateMessage initPrivateMessage = prtMsgManager.initMessageWithAttachmentsAndRecipients(thisDmb.getMsg());
      this.setDetailMsg(new PrivateMessageDecoratedBean(initPrivateMessage));
      setDetailMsgCount++;
      
      List recLs= initPrivateMessage.getRecipients();
      for (Iterator iterator = recLs.iterator(); iterator.hasNext();)
      {
        PrivateMessageRecipient element = (PrivateMessageRecipient) iterator.next();
        if (element != null)
        {
          if((element.getRead().booleanValue()) || (element.getUserId().equals(getUserId())) )
          {
           getDetailMsg().setHasRead(true) ;
          }
        }
      }
      
      getDetailMsg().setDepth(thisDmb.getDepth()) ;
      getDetailMsg().setHasNext(thisDmb.getHasNext());
      getDetailMsg().setHasPre(thisDmb.getHasPre()) ;
    }
    LearningResourceStoreService lrss = (LearningResourceStoreService) ComponentManager
            .get("org.sakaiproject.event.api.LearningResourceStoreService");
    Event event = EventTrackingService.newEvent("msgcntr", "read private message", true);
    if (null != lrss) {
    	try{
    		lrss.registerStatement(getStatementForUserReadPvtMsg(lrss.getEventActor(event), getDetailMsg().getMsg().getTitle()), "msgcntr");
    	}catch(Exception e){
    		LOG.error(e.getMessage(), e);
    	}
    }
    return null;
  }
  
  
  /////////////////////////////////////     DISPLAY NEXT/PREVIOUS TOPIC     //////////////////////////////////  
  private PrivateTopicDecoratedBean selectedTopic;
  
  /**
   * @return Returns the selectedTopic.
   */
  public PrivateTopicDecoratedBean getSelectedTopic()
  {
    return selectedTopic;
  }


  /**
   * @param selectedTopic The selectedTopic to set.
   */
  public void setSelectedTopic(PrivateTopicDecoratedBean selectedTopic)
  {
    this.selectedTopic = selectedTopic;
  }


  /**
   * Add prev and next topic UUID value and booleans for display of links
   * 
   */
  public void setPrevNextTopicDetails(String msgNavMode)
  {
    for (int i = 0; i < pvtTopics.size(); i++)
    {
      Topic el = (Topic)pvtTopics.get(i);
      
      
      if(el.getTitle().equals(msgNavMode))
      {
        setSelectedTopic(new PrivateTopicDecoratedBean(el)) ;
        if(i ==0)
        {
          getSelectedTopic().setHasPreviousTopic(false);
          if(i==(pvtTopics.size()-1))
          {
            getSelectedTopic().setHasNextTopic(false) ;
          }
          else
          {
            getSelectedTopic().setHasNextTopic(true) ;
            Topic nt=(Topic)pvtTopics.get(i+1);
            if (nt != null)
            {
              //getSelectedTopic().setNextTopicId(nt.getUuid());
              getSelectedTopic().setNextTopicTitle(nt.getTitle());
            }
          }
        }
        else if(i==(pvtTopics.size()-1))
        {
          getSelectedTopic().setHasPreviousTopic(true);
          getSelectedTopic().setHasNextTopic(false) ;
          
          Topic pt=(Topic)pvtTopics.get(i-1);
          if (pt != null)
          {
            //getSelectedTopic().setPreviousTopicId(pt.getUuid());
            getSelectedTopic().setPreviousTopicTitle(pt.getTitle());
          }          
        }
        else
        {
          getSelectedTopic().setHasNextTopic(true) ;
          getSelectedTopic().setHasPreviousTopic(true);
          
          Topic nt=(Topic)pvtTopics.get(i+1);
          if (nt != null)
          {
            //getSelectedTopic().setNextTopicId(nt.getUuid());
            getSelectedTopic().setNextTopicTitle(nt.getTitle());
          }
          Topic pt=(Topic)pvtTopics.get(i-1);
          if (pt != null)
          {
            //getSelectedTopic().setPreviousTopicId(pt.getUuid());
            getSelectedTopic().setPreviousTopicTitle(pt.getTitle());
          }
        }
        
      }
    }
    //create a selected topic and set the topic id for next/prev topic
  }
  /**
   * processDisplayPreviousFolder()
   */
  public String processDisplayPreviousTopic() {
	multiDeleteSuccess = false;
    String prevTopicTitle = getExternalParameterByKey("previousTopicTitle");
    if(hasValue(prevTopicTitle))
    {
      msgNavMode=prevTopicTitle;
      
      decoratedPvtMsgs=new ArrayList() ;
      
      String typeUuid = getPrivateMessageTypeFromContext(msgNavMode);        
      
      decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_DATE,
          PrivateMessageManager.SORT_DESC);
      
      decoratedPvtMsgs = createDecoratedDisplay(decoratedPvtMsgs);
      
      if(selectView!=null && selectView.equalsIgnoreCase(THREADED_VIEW))
      {
      	this.rearrageTopicMsgsThreaded(false);
      }

      //set prev/next Topic
      setPrevNextTopicDetails(msgNavMode);
      //set prev/next message
      setPrevNextMessageDetails();
      
      if (searchPvtMsgs != null)
      {
    	  searchPvtMsgs.clear();
    	  return DISPLAY_MESSAGES_PG;
      }
    }
    return null;
  }
  
  /**
   * processDisplayNextFolder()
   */
  public String processDisplayNextTopic()
  { 
	multiDeleteSuccess = false;
    String nextTitle = getExternalParameterByKey("nextTopicTitle");
    if(hasValue(nextTitle))
    {
      msgNavMode=nextTitle;
      decoratedPvtMsgs=new ArrayList() ;
      
      String typeUuid = getPrivateMessageTypeFromContext(msgNavMode);        
      
      decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_DATE,
          PrivateMessageManager.SORT_DESC);
      
      decoratedPvtMsgs = createDecoratedDisplay(decoratedPvtMsgs);
      
      if(selectView!=null && selectView.equalsIgnoreCase(THREADED_VIEW))
      {
      	this.rearrageTopicMsgsThreaded(false);
      }

      //set prev/next Topic
      setPrevNextTopicDetails(msgNavMode);
      //set prev/next message
      setPrevNextMessageDetails();
      
      if (searchPvtMsgs != null)
      {
    	  searchPvtMsgs.clear();
    	  return DISPLAY_MESSAGES_PG;
      }
    }

    return null;
  }
/////////////////////////////////////     DISPLAY NEXT/PREVIOUS TOPIC     //////////////////////////////////
    
  
  
  //////////////////////////////////////////////////////////
  /**
   * @param externalTopicId
   * @return
   */
  private String processDisplayMsgById(String externalMsgId)
  {
    LOG.debug("processDisplayMsgById()");
    String msgId=getExternalParameterByKey(externalMsgId);
    if(msgId!=null)
    {
      PrivateMessageDecoratedBean dbean=null;
      PrivateMessage msg = (PrivateMessage) prtMsgManager.getMessageById(Long.valueOf(msgId)) ;
      if(msg != null)
      {
    	  if (dbean == null)
					throw new IllegalStateException(
							"PrivateMessageDecoratedBean dbean == null!");
        dbean.addPvtMessage(new PrivateMessageDecoratedBean(msg)) ;
        detailMsg = dbean;
      }
    }
    else
    {
      LOG.debug("processDisplayMsgById() - Error");
      return DISPLAY_MESSAGES_PG;
    }
    return SELECTED_MESSAGE_PG;
  }
  
  //////////////////////REPLY SEND  /////////////////
  public String processPvtMsgPreviewReply(){
	  PrivateMessage pvtMsg = getPvtMsgReplyMessage(getDetailMsg().getMsg(), false);
	  if(pvtMsg == null){
		  return null;
	  }else{
		  PrivateMessageDecoratedBean pmDb = new PrivateMessageDecoratedBean(pvtMsg);
		  pmDb.setIsPreviewReply(true);
		  pmDb.setPreviewReplyTmpMsg(getDetailMsg());
		  this.setDetailMsg(pmDb);

		  return SELECTED_MESSAGE_PG;
	  }
  }
  public String processPvtMsgPreviewReplyBack(){
	  this.setDetailMsg(getDetailMsg().getPreviewReplyTmpMsg());
	  return MESSAGE_REPLY_PG;
  }
  
  public String processPvtMsgPreviewReplySend(){
	  
	  return processPvtMsgReplySentAction(getDetailMsg().getMsg());
  }
  
 public String processPvtMsgReplySend() {
    LOG.debug("processPvtMsgReplySend()");
    
    return processPvtMsgReplySentAction(getPvtMsgReplyMessage(getDetailMsg().getMsg(), false));
 }
 
 private String processPvtMsgReplySentAction(PrivateMessage rrepMsg){
    if(rrepMsg == null){
    	return null;
    }else{

    	Map<User, Boolean> recipients = getRecipients();

    	prtMsgManager.sendPrivateMessage(rrepMsg, recipients, isSendEmail());
    	
    	if(!rrepMsg.getDraft()){
    		prtMsgManager.markMessageAsRepliedForUser(getReplyingMessage());
    		incrementSynopticToolInfo(recipients.keySet(), false);
    	    LearningResourceStoreService lrss = (LearningResourceStoreService) ComponentManager
    	            .get("org.sakaiproject.event.api.LearningResourceStoreService");
    	    Event event = EventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_RESPONSE, getEventMessage(rrepMsg), false);
    	    EventTrackingService.post(event);
    	    if (null != lrss) {
    	    	try{
    	    		lrss.registerStatement(getStatementForUserSentPvtMsg(lrss.getEventActor(event), getDetailMsg().getMsg().getTitle(), SAKAI_VERB.responded), "msgcntr");
    	    	}catch(Exception e){
    	    		LOG.error(e.getMessage(), e);
    	    	}
    	    }
    	}
    	//reset contents
    	resetComposeContents();
    	
    	return DISPLAY_MESSAGES_PG;
    }  
 }
 
 
 private PrivateMessage getPvtMsgReplyMessage(PrivateMessage currentMessage, boolean isDraft){
    if (setDetailMsgCount != 1) {
    	setErrorMessage(getResourceBundleString(MULTIPLE_WINDOWS , new Object[] {ServerConfigurationService.getString("ui.service","Sakai")}));
    	return null;
    } else {
    
    	//PrivateMessage currentMessage = getDetailMsg().getMsg() ;
    	//by default add user who sent original message    
    	for (Iterator i = totalComposeToList.iterator(); i.hasNext();) {      
    		MembershipItem membershipItem = (MembershipItem) i.next();                

    		if (membershipItem.getUser() != null && membershipItem.getUser().getId().equals(currentMessage.getCreatedBy())) {
    			selectedComposeToList.add(membershipItem.getId());
    		}
    	}

    	
    	if(!hasValue(getReplyToSubject()))
    	{
    		if(isDraft){
    			setErrorMessage(getResourceBundleString(MISSING_SUBJECT_DRAFT));
    		}else{
    			setErrorMessage(getResourceBundleString(MISSING_SUBJECT));
    		}
    		return null ;
    	}
    	if(!isDraft){
    		if(selectedComposeToList.size()<1 && selectedComposeBccList.size() < 1)
    		{
    			setErrorMessage(getResourceBundleString(SELECT_RECIPIENT_LIST_FOR_REPLY));
    			return null ;
    		}
    	}

    	PrivateMessage rrepMsg = messageManager.createPrivateMessage() ;

    	StringBuilder alertMsg = new StringBuilder();
    	rrepMsg.setTitle(getReplyToSubject());
    	rrepMsg.setBody(FormattedText.processFormattedText(getReplyToBody(), alertMsg));
    	rrepMsg.setDraft(Boolean.FALSE);
    	rrepMsg.setDeleted(Boolean.FALSE);

    	rrepMsg.setAuthor(getAuthorString());
    	rrepMsg.setApproved(Boolean.FALSE);

    	rrepMsg.setLabel(getSelectedLabel());

    	rrepMsg.setInReplyTo(currentMessage) ;

    	//Add the recipientList as String for display in Sent folder
    	// Since some users may be hidden, if some of these are recipients
    	// filter them out (already checked if no recipients)
    	// if only 1 recipient no need to check visibility
    	StringBuffer sendToString = new StringBuffer("");
    	StringBuffer sendToHiddenString = new StringBuffer("");

    	if (selectedComposeToList.size() == 1) {
    		MembershipItem membershipItem = (MembershipItem) courseMemberMap.get(selectedComposeToList.get(0));
    		if(membershipItem != null)
    		{
    			sendToString.append(membershipItem.getName()).append("; ");
    		}          
    	}
    	else {
    		for (int i = 0; i < selectedComposeToList.size(); i++)
    		{
    			MembershipItem membershipItem = (MembershipItem) courseMemberMap.get(selectedComposeToList.get(i));
    			if(membershipItem != null)
    			{
    				if (membershipItem.isViewable()) {
    					sendToString.append(membershipItem.getName()).append("; ");
    				}
    				else {
    					sendToHiddenString.append(membershipItem.getName()).append("; ");
    				}
    			}          
    		}
    	}

    	//add bcc recipients place holder:
    	if(selectedComposeBccList.size() > 0 && !sendToString.toString().contains(getResourceBundleString(RECIPIENTS_UNDISCLOSED))){
    		sendToString.append(getResourceBundleString(RECIPIENTS_UNDISCLOSED)).append("; ");
    	}

    	//create sendToBccString
    	StringBuffer sendToBccString = new StringBuffer("");
    	StringBuffer sendToBccHiddenString = new StringBuffer("");
    	for (int i = 0; i < selectedComposeBccList.size(); i++)
    	{
    		MembershipItem membershipItem = (MembershipItem) courseMemberMap.get(selectedComposeBccList.get(i));
    		if(membershipItem != null)
    		{
    			if (membershipItem.isViewable()) {
    				sendToBccString.append(membershipItem.getName()).append("; ");
    			}
    			else {
    				sendToBccHiddenString.append(membershipItem.getName()).append("; ");
    			}
    		}          
    	}

    	//clean sendToString
    	if (! "".equals(sendToString.toString())) {
    		sendToString.delete(sendToString.length()-2, sendToString.length()); //remove last comma and space
    	}

    	if ("".equals(sendToHiddenString.toString())) {
    		rrepMsg.setRecipientsAsText(sendToString.toString());
    	}
    	else {
    		sendToHiddenString.delete(sendToHiddenString.length()-2, sendToHiddenString.length()); //remove last comma and space    
    		rrepMsg.setRecipientsAsText(sendToString.toString() + " " + PrivateMessage.HIDDEN_RECIPIENTS_START + sendToHiddenString.toString() + PrivateMessage.HIDDEN_RECIPIENTS_END);
    	}    

    	//clean sendToBccString
    	//clean sendToString
    	if (! "".equals(sendToBccString.toString())) {
    		sendToBccString.delete(sendToBccString.length()-2, sendToBccString.length()); //remove last comma and space
    	}

    	if ("".equals(sendToBccHiddenString.toString())) {
    		rrepMsg.setRecipientsAsTextBcc(sendToBccString.toString());
    	}
    	else {
    		sendToBccHiddenString.delete(sendToBccHiddenString.length()-2, sendToBccHiddenString.length()); //remove last comma and space    
    		rrepMsg.setRecipientsAsTextBcc(sendToBccString.toString() + " " + PrivateMessage.HIDDEN_RECIPIENTS_START + sendToBccHiddenString.toString() + PrivateMessage.HIDDEN_RECIPIENTS_END);
    	}  
    	
    	//Add attachments
    	for(int i=0; i<allAttachments.size(); i++)
    	{
    		prtMsgManager.addAttachToPvtMsg(rrepMsg, ((DecoratedAttachment)allAttachments.get(i)).getAttachment());         
    	}            

    	return rrepMsg;
    	
    }
  }
  
  // ////////////////////Forward SEND /////////////////
 
 public String processPvtMsgPreviewForward(){
	 PrivateMessage pvtMsg = getPvtMsgForward(getDetailMsg().getMsg(), false);
	 if(pvtMsg == null){
		 return null;
	 }else{
		 PrivateMessageDecoratedBean pmDb = new PrivateMessageDecoratedBean(pvtMsg);
		 pmDb.setIsPreviewForward(true);
		 pmDb.setPreviewReplyTmpMsg(getDetailMsg());
		 this.setDetailMsg(pmDb);

		 return SELECTED_MESSAGE_PG;
	 }
 }
 public String processPvtMsgPreviewForwardBack(){
	 String title = getDetailMsg().getMsg().getTitle();
	 String body = getDetailMsg().getMsg().getBody();
	 this.setDetailMsg(getDetailMsg().getPreviewReplyTmpMsg());
	 String returnString =  processPvtMsgForward();
	 //reset title and body:
	 getDetailMsg().getMsg().setTitle(title);
	 setForwardSubject(title);
	 getDetailMsg().getMsg().setBody(body);
	 setForwardBody(body);
	 
	 return returnString;
 }
 
 public String processPvtMsgPreviewForwardSend(){	  
	  processPvtMsgForwardSendHelper(getDetailMsg().getMsg());
	  return DISPLAY_MESSAGES_PG;
 }
 public String processPvtMsgForwardSend() {
    LOG.debug("processPvtMsgForwardSend()");
    if (setDetailMsgCount != 1) {
    	setErrorMessage(getResourceBundleString(MULTIPLE_WINDOWS , new Object[] {ServerConfigurationService.getString("ui.service","Sakai")}));
    	return null;
    } else {
    	PrivateMessage pvtMsg = getPvtMsgForward(getDetailMsg().getMsg(), false);
    	if(pvtMsg == null){
    		return null;
    	}else{
    		processPvtMsgForwardSendHelper(pvtMsg);
    		return DISPLAY_MESSAGES_PG;
    	}
    }
 }
 
 public String processPvtMsgForwardSaveDraft(){
	 LOG.debug("processPvtMsgForwardSaveDraft()");
	 if (setDetailMsgCount != 1) {
	     setErrorMessage(getResourceBundleString(MULTIPLE_WINDOWS , new Object[] {ServerConfigurationService.getString("ui.service","Sakai")}));
		 return null;
	 } else {
		 PrivateMessage pvtMsg = getPvtMsgForward(getDetailMsg().getMsg(), true);
		 if(pvtMsg == null){
			 return null;
		 }else{
			 pvtMsg.setDraft(Boolean.TRUE);
			 processPvtMsgForwardSendHelper(pvtMsg);
			 return returnDraftPage();
		 }
	 }
 }
 
 private PrivateMessage getPvtMsgForward(PrivateMessage currentMessage, boolean isDraft){

	 if(!isDraft){
		 if(getSelectedComposeToList().size()<1 && getSelectedComposeBccList().size() < 1)
		 {
			 setErrorMessage(getResourceBundleString(SELECT_MSG_RECIPIENT));
			 return null ;
		 }
	 }
	 if(!hasValue(getForwardSubject()))
	 {
		 if(isDraft){
			 setErrorMessage(getResourceBundleString(MISSING_SUBJECT_DRAFT));
		 }else{
			 setErrorMessage(getResourceBundleString(MISSING_SUBJECT));
		 }
		 return null ;
	 }

    	PrivateMessage rrepMsg = messageManager.createPrivateMessage() ;

    	StringBuilder alertMsg = new StringBuilder();
    	rrepMsg.setTitle(getForwardSubject());
    	rrepMsg.setDraft(Boolean.FALSE);
    	rrepMsg.setDeleted(Boolean.FALSE);

    	rrepMsg.setAuthor(getAuthorString());
    	rrepMsg.setApproved(Boolean.FALSE);
    	rrepMsg.setBody(FormattedText.processFormattedText(getForwardBody(), alertMsg));

    	rrepMsg.setLabel(getSelectedLabel());

    	rrepMsg.setInReplyTo(currentMessage) ;

    	//Add the recipientList as String for display in Sent folder
    	// Since some users may be hidden, if some of these are recipients
    	// filter them out (already checked if no recipients)
    	// if only 1 recipient no need to check visibility
    	StringBuffer sendToString = new StringBuffer();
    	StringBuffer sendToHiddenString = new StringBuffer();

    	if (selectedComposeToList.size() == 1) {
    		MembershipItem membershipItem = (MembershipItem) courseMemberMap.get(selectedComposeToList.get(0));
    		if(membershipItem != null)
    		{
    			sendToString.append(membershipItem.getName()).append("; ");
    		}          
    	}
    	else {
    		for (int i = 0; i < selectedComposeToList.size(); i++)
    		{
    			MembershipItem membershipItem = (MembershipItem) courseMemberMap.get(selectedComposeToList.get(i));
    			if(membershipItem != null)
    			{
    				if (membershipItem.isViewable()) {
    					sendToString.append(membershipItem.getName()).append("; ");
    				}
    				else {
    					sendToHiddenString.append(membershipItem.getName()).append("; ");
    				}
    			}          
    		}
    	}

    	//add bcc recipients place holder:
    	if(selectedComposeBccList.size() > 0 && !sendToString.toString().contains(getResourceBundleString(RECIPIENTS_UNDISCLOSED))){
    		sendToString.append(getResourceBundleString(RECIPIENTS_UNDISCLOSED)).append("; ");
    	}

    	StringBuffer sendToBccString = new StringBuffer();
    	StringBuffer sendToBccHiddenString = new StringBuffer();
    	for (int i = 0; i < selectedComposeBccList.size(); i++)
    	{
    		MembershipItem membershipItem = (MembershipItem) courseMemberMap.get(selectedComposeBccList.get(i));
    		if(membershipItem != null)
    		{
    			if (membershipItem.isViewable()) {
    				sendToBccString.append(membershipItem.getName()).append("; ");
    			}
    			else {
    				sendToBccHiddenString.append(membershipItem.getName()).append("; ");
    			}
    		}          
    	}

    	//clean sendToString
    	if (! "".equals(sendToString.toString())) {
    		sendToString.delete(sendToString.length()-2, sendToString.length()); //remove last comma and space
    	}

    	if ("".equals(sendToHiddenString.toString())) {
    		rrepMsg.setRecipientsAsText(sendToString.toString());
    	}
    	else {
    		sendToHiddenString.delete(sendToHiddenString.length()-2, sendToHiddenString.length()); //remove last comma and space    
    		rrepMsg.setRecipientsAsText(sendToString.toString() + " (" + sendToHiddenString.toString() + ")");
    	}       	      
    	
    	//clean sendToBccString
    	if (! "".equals(sendToBccString.toString())) {
    		sendToBccString.delete(sendToBccString.length()-2, sendToBccString.length()); //remove last comma and space
    	}

    	if ("".equals(sendToBccHiddenString.toString())) {
    		rrepMsg.setRecipientsAsTextBcc(sendToBccString.toString());
    	}
    	else {
    		sendToBccHiddenString.delete(sendToBccHiddenString.length()-2, sendToBccHiddenString.length()); //remove last comma and space    
    		rrepMsg.setRecipientsAsTextBcc(sendToBccString.toString() + " (" + sendToBccHiddenString.toString() + ")");
    	} 
    	
    	//Add attachments
    	for(int i=0; i<allAttachments.size(); i++)
    	{
    		prtMsgManager.addAttachToPvtMsg(rrepMsg, ((DecoratedAttachment)allAttachments.get(i)).getAttachment());         
    	}  
    	
    	return rrepMsg;
    }
    
    private void processPvtMsgForwardSendHelper(PrivateMessage rrepMsg){
    	Map<User, Boolean> recipients = getRecipients();
    	
    	prtMsgManager.sendPrivateMessage(rrepMsg, recipients, isSendEmail());

    	if(!rrepMsg.getDraft()){
    		//update Synoptic tool info
    		incrementSynopticToolInfo(recipients.keySet(), false);
            LearningResourceStoreService lrss = (LearningResourceStoreService) ComponentManager
                    .get("org.sakaiproject.event.api.LearningResourceStoreService");
            Event event = EventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_FORWARD, getEventMessage(rrepMsg), false);
            EventTrackingService.post(event);
            if (null != lrss) {
            	try{
            		lrss.registerStatement(getStatementForUserSentPvtMsg(lrss.getEventActor(event), getDetailMsg().getMsg().getTitle(), SAKAI_VERB.responded), "msgcntr");
            	}catch(Exception e){
            		LOG.error(e.getMessage(), e);
            	}
            }
    	}
    	//reset contents
    	resetComposeContents();    	    	
    }

  
  
  //reply all preview:
 public String processPvtMsgPreviewReplyAll(){
	 PrivateMessage pvtMsg = processPvtMsgReplyAllSendHelper(true, Boolean.FALSE);
	 if(pvtMsg == null){
		 return null;
	 }else{
		 PrivateMessageDecoratedBean pmDb = new PrivateMessageDecoratedBean(pvtMsg);
		 pmDb.setIsPreviewReplyAll(true);
		 pmDb.setPreviewReplyTmpMsg(getDetailMsg());
		 this.setDetailMsg(pmDb);

		 return SELECTED_MESSAGE_PG;
	 }
 }
 public String processPvtMsgPreviewReplyAllBack(){
	  this.setDetailMsg(getDetailMsg().getPreviewReplyTmpMsg());
	  return MESSAGE_ReplyAll_PG;
 }
 
 public String processPvtMsgPreviewReplyAllSend(){
	 this.setDetailMsg(getDetailMsg().getPreviewReplyTmpMsg());
	 PrivateMessage pvtMsg = processPvtMsgReplyAllSendHelper(false, Boolean.FALSE);
	 if(pvtMsg == null){
		 return null;
	 }else{
		 return DISPLAY_MESSAGES_PG;
	 }
 }
  
 public String processPvtMsgReplyAllSaveDraft(){
	 LOG.debug("processPvtMsgReply All Send()");
	 if (setDetailMsgCount != 1) {
	     setErrorMessage(getResourceBundleString(MULTIPLE_WINDOWS , new Object[] {ServerConfigurationService.getString("ui.service","Sakai")}));
		 return null;
	 } else {
		 PrivateMessage pvtMsg = processPvtMsgReplyAllSendHelper(false, Boolean.TRUE);
		 if(pvtMsg == null){
			 return null;
		 }else{
			 return returnDraftPage();
		 }
	 }
 }
 
  public String processPvtMsgReplyAllSend() {
    LOG.debug("processPvtMsgReply All Send()");
    if (setDetailMsgCount != 1) {
    	setErrorMessage(getResourceBundleString(MULTIPLE_WINDOWS , new Object[] {ServerConfigurationService.getString("ui.service","Sakai")}));
    	return null;
    } else {
    	PrivateMessage pvtMsg = processPvtMsgReplyAllSendHelper(false, Boolean.FALSE);
    	if(pvtMsg == null){
    		return null;
    	}else{
    		return DISPLAY_MESSAGES_PG;
    	}
    }
  }
  
  private PrivateMessage processPvtMsgReplyAllSendHelper(boolean preview, Boolean isDraft){

	  PrivateMessage currentMessage = getDetailMsg().getMsg() ;

	  String msgauther=currentMessage.getAuthor();//string   "Test"      

	  //Select Forward Recipients
	  
	  if(!hasValue(getForwardSubject()))
	  {
		  if(isDraft){
			  setErrorMessage(getResourceBundleString(MISSING_SUBJECT_DRAFT));
		  }else{
			  setErrorMessage(getResourceBundleString(MISSING_SUBJECT));
		  }
		  return null ;
	  }

	  PrivateMessage rrepMsg = messageManager.createPrivateMessage() ;


	  StringBuilder alertMsg = new StringBuilder();
	  rrepMsg.setTitle(getForwardSubject());
	  rrepMsg.setDraft(isDraft);
	  rrepMsg.setDeleted(Boolean.FALSE);

	  rrepMsg.setAuthor(getAuthorString());
	  rrepMsg.setApproved(Boolean.FALSE);
	  //add some emty space to the msg composite, by huxt
	  String replyAllbody="  ";
	  replyAllbody=getForwardBody();


	  rrepMsg.setBody(FormattedText.processFormattedText(replyAllbody, alertMsg));
	  rrepMsg.setLabel(getSelectedLabel());
	  rrepMsg.setInReplyTo(currentMessage) ;


	  //Add attachments
	  for(int i=0; i<allAttachments.size(); i++)
	  {
		  prtMsgManager.addAttachToPvtMsg(rrepMsg, ((DecoratedAttachment)allAttachments.get(i)).getAttachment());         
	  }            

	  User autheruser=null;
	  try {
		  autheruser = UserDirectoryService.getUser(currentMessage.getCreatedBy());
	  } catch (UserNotDefinedException e) {
		  e.printStackTrace();
	  }

	  
	  List tmpRecipList = currentMessage.getRecipients();

	  Map<User, Boolean> returnSet = new HashMap<User, Boolean>();
	  StringBuffer sendToStringreplyall = new StringBuffer();

	  Iterator iter = tmpRecipList.iterator();
	  while (iter.hasNext())
	  {
		  PrivateMessageRecipient tmpPMR = (PrivateMessageRecipient)iter.next();
		  User replyrecipientaddtmp=null;
		  try {
			  replyrecipientaddtmp = UserDirectoryService.getUser(tmpPMR.getUserId());
		  } catch (UserNotDefinedException e) {
			  // TODO Auto-generated catch block
			  LOG.warn("Unable to find user : " + tmpPMR.getUserId(), e);
		  }

		  if (replyrecipientaddtmp == null){
			  LOG.warn("continuing passed user : "+tmpPMR.getUserId());
			  //throw new IllegalStateException("User replyrecipientaddtmp == null!");
		  }else{
		  	if(!(replyrecipientaddtmp.getDisplayName()).equals(getUserName()) && !tmpPMR.getBcc())//&&(!(replyrecipientaddtmp.getDisplayName()).equals(msgauther)))
		  	{
				  returnSet.put(replyrecipientaddtmp, tmpPMR.getBcc());
		        }
		  }
	  }

	  if(currentMessage.getRecipientsAsText() != null && !"".equals(currentMessage.getRecipientsAsText())){
		  sendToStringreplyall.append(currentMessage.getRecipientsAsText()).append("; ");
	  }
	  if(returnSet.isEmpty()) {
		  returnSet.put(autheruser, false);
		  if(!sendToStringreplyall.toString().contains(msgauther)){
			  //only add it to the reply string if it doesn't exist
			  sendToStringreplyall.append(msgauther).append("; ");
		  }
	  }
	  if(returnSet.containsKey(autheruser) && !sendToStringreplyall.toString().contains(msgauther)){
		  sendToStringreplyall.append(msgauther).append("; ");
	  }

	  //Add the recipientList as String for display in Sent folder
	  // Since some users may be hidden, if some of these are recipients
	  // filter them out (already checked if no recipients)
	  // if only 1 recipient no need to check visibility
	  StringBuffer sendToString = new StringBuffer(sendToStringreplyall);
	  StringBuffer sendToHiddenString = new StringBuffer();

	  if (selectedComposeToList.size() == 1) {
		  MembershipItem membershipItem = (MembershipItem) courseMemberMap.get(selectedComposeToList.get(0));
		  if(membershipItem != null)
		  {
			  sendToString.append(membershipItem.getName()).append("; ");
		  }          
	  }
	  else {
		  for (int i = 0; i < selectedComposeToList.size(); i++)
		  {
			  MembershipItem membershipItem = (MembershipItem) courseMemberMap.get(selectedComposeToList.get(i));
			  if(membershipItem != null)
			  {
				  if(!sendToStringreplyall.toString().contains(membershipItem.getName())){
					  if (membershipItem.isViewable()) {
						  sendToString.append(membershipItem.getName()).append("; ");
					  }
					  else {
						  sendToHiddenString.append(membershipItem.getName()).append("; ");
					  }
				  }
			  }          
		  }
	  }

	  //add bcc recipients place holder:
	  if(selectedComposeBccList.size() > 0 && !sendToString.toString().contains(getResourceBundleString(RECIPIENTS_UNDISCLOSED))){
		  sendToString.append(getResourceBundleString(RECIPIENTS_UNDISCLOSED)).append("; ");
	  }

	  //create sendToBccString
	  StringBuffer sendToBccString = new StringBuffer();
	  StringBuffer sendToBccHiddenString = new StringBuffer();
	  for (int i = 0; i < selectedComposeBccList.size(); i++)
	  {
		  MembershipItem membershipItem = (MembershipItem) courseMemberMap.get(selectedComposeBccList.get(i));
		  if(membershipItem != null)
		  {
			  if (membershipItem.isViewable()) {
				  sendToBccString.append(membershipItem.getName()).append("; ");
			  }
			  else {
				  sendToBccHiddenString.append(membershipItem.getName()).append("; ");
			  }
		  }          
	  }

	  //clean sendToString
	  if (! "".equals(sendToString.toString()) && sendToString.length() >= 2) {
		  sendToString.delete(sendToString.length()-2, sendToString.length()); //remove last comma and space
	  }

	  if ("".equals(sendToHiddenString.toString())) {
		  rrepMsg.setRecipientsAsText(sendToString.toString());
	  }
	  else {
		  sendToHiddenString.delete(sendToHiddenString.length()-2, sendToHiddenString.length()); //remove last comma and space    
		  rrepMsg.setRecipientsAsText(sendToString.toString() + " (" + sendToHiddenString.toString() + ")");
	  }

	  //clean sendToBccString
	  if (! "".equals(sendToBccString.toString()) && sendToBccString.length() >= 2) {
		  sendToBccString.delete(sendToBccString.length()-2, sendToBccString.length()); //remove last comma and space
	  }

	  if ("".equals(sendToBccHiddenString.toString())) {
		  rrepMsg.setRecipientsAsTextBcc(sendToBccString.toString());
	  }
	  else {
		  sendToBccHiddenString.delete(sendToBccHiddenString.length()-2, sendToBccHiddenString.length()); //remove last comma and space    
		  rrepMsg.setRecipientsAsTextBcc(sendToBccString.toString() + " (" + sendToBccHiddenString.toString() + ")");
	  }

	  //Add selected users to reply all list

	  Map<User, Boolean> recipients = getRecipients();
	  for (Iterator<Entry<User, Boolean>> i = recipients.entrySet().iterator(); i.hasNext();){
		  Entry<User, Boolean> entrySet = (Entry<User, Boolean>) i.next();
		  if(!returnSet.containsKey(entrySet.getKey())){
			  returnSet.put(entrySet.getKey(), entrySet.getValue());
		  }
	  }
	  if(!preview){
	          prtMsgManager.sendPrivateMessage(rrepMsg, returnSet, isSendEmail());

		  if(!rrepMsg.getDraft()){
			  prtMsgManager.markMessageAsRepliedForUser(getReplyingMessage());
			  //update Synoptic tool info
			  incrementSynopticToolInfo(returnSet.keySet(), false);
	          LearningResourceStoreService lrss = (LearningResourceStoreService) ComponentManager
	                    .get("org.sakaiproject.event.api.LearningResourceStoreService");
	          Event event = EventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_FORWARD, getEventMessage(rrepMsg), false);
	          EventTrackingService.post(event);
	          if (null != lrss) {
	        	  try{
	        		  lrss.registerStatement(getStatementForUserSentPvtMsg(lrss.getEventActor(event), getDetailMsg().getMsg().getTitle(), SAKAI_VERB.responded), "msgcntr");
	        	  }catch(Exception e){
	          		LOG.error(e.getMessage(), e);
	        	  }
	          }
		  }
		  //reset contents
		  resetComposeContents();
	  }
	  return rrepMsg;
  }


  
  
 private boolean containedInList(User user,List list){
	 
	boolean isContain=false;
	 if (list==null)
	 {
		 return false;
	 }
	 
	 Iterator iter = list.iterator();
	 	 
	   User tmpuser=null;
	   while(iter.hasNext()){

			 PrivateMessageRecipient tmpPMR = (PrivateMessageRecipient)iter.next();
		 	User replyrecipientaddtmp=null;
				try {
					replyrecipientaddtmp = UserDirectoryService.getUser(tmpPMR.getUserId());
				} catch (UserNotDefinedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		   
		 
		   
		   if((replyrecipientaddtmp!=null)&&(replyrecipientaddtmp==user)){
			   //tmplist.add(tmpPMR);
			   isContain=true;
			   
		   }
	   }
	   return isContain;
	  	   
  }
		
  /**
   * process from Compose screen
   * @return - pvtMsg
   */
 public String processPvtMsgReplySaveDraft() {
	 PrivateMessage pvtMsg = getPvtMsgReplyMessage(getDetailMsg().getMsg(), true);
	 if(pvtMsg == null){
		 return null;
	 }else{
		 pvtMsg.setDraft(Boolean.TRUE);
		 setMsgNavMode(PVTMSG_MODE_DRAFT);
		 String returnVal = processPvtMsgReplySentAction(pvtMsg);
		 if(returnVal == null){
			 return null;
		 }else{
			 return returnDraftPage();
		 }
	 }
 }
  
  ////////////////////////////////////////////////////////////////  
  public String processPvtMsgEmptyDelete() {
    LOG.debug("processPvtMsgEmptyDelete()");
    
    List delSelLs=new ArrayList() ;
    //this.setDisplayPvtMsgs(getDisplayPvtMsgs());    
    for (Iterator iter = this.decoratedPvtMsgs.iterator(); iter.hasNext();)
    {
      PrivateMessageDecoratedBean element = (PrivateMessageDecoratedBean) iter.next();
      if(element.getIsSelected())
      {
        delSelLs.add(element);
      }      
    }
    this.setSelectedDeleteItems(delSelLs);
    if(delSelLs.size()<1)
    {
      setErrorMessage(getResourceBundleString(SELECT_MSGS_TO_DELETE));
      return null;  //stay in the same page if nothing is selected for delete
    }else {
      setErrorMessage(getResourceBundleString(CONFIRM_PERM_MSG_DELETE));
      return DELETE_MESSAGE_PG;
    }
  }
  
  //delete private message 
  public String processPvtMsgMultiDelete()
  { 
    LOG.debug("processPvtMsgMultiDelete()");
  
    boolean deleted = false;
    for (Iterator iter = getSelectedDeleteItems().iterator(); iter.hasNext();)
    {
      PrivateMessage element = ((PrivateMessageDecoratedBean) iter.next()).getMsg();
      if (element != null) 
      {
    	deleted = true;
        prtMsgManager.deletePrivateMessage(element, getPrivateMessageTypeFromContext(msgNavMode)) ;        
      }      
      
      if ("pvt_deleted".equals(msgNavMode))
    	  EventTrackingService.post(EventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_REMOVE, getEventMessage((Message) element), false));
      else
    	  EventTrackingService.post(EventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_MOVE_TO_DELETED_FOLDER, getEventMessage((Message) element), false));

    }
    
    if (deleted)
    {
    	if ("pvt_deleted".equals(msgNavMode))
    		multiDeleteSuccessMsg = getResourceBundleString(PERM_DELETE_SUCCESS_MSG);
    	else
    		multiDeleteSuccessMsg = getResourceBundleString(MULTIDELETE_SUCCESS_MSG);
    	
    	multiDeleteSuccess = true;
    }


	return DISPLAY_MESSAGES_PG;
  }

  
  public String processPvtMsgDispOtions() 
  {
    LOG.debug("processPvtMsgDispOptions()");
    
    return "pvtMsgOrganize" ;
  }
  
  
  ///////////////////////////       Process Select All       ///////////////////////////////
  private boolean selectAll = false;  
  private int numberChecked = 0; // to cover case where user selectes check all
  
  public boolean isSelectAll()
  {
    return selectAll;
  }
  public void setSelectAll(boolean selectAll)
  {
    this.selectAll = selectAll;
  }

  public int getNumberChecked() 
  {
	return numberChecked;
  }

  /**
   * process isSelected for all decorated messages
   * @return same page i.e. will be pvtMsg 
   */
  public String processCheckAll()
  {
    LOG.debug("processCheckAll()");
    selectAll= true;
    multiDeleteSuccess = false;

    return null;
  }
  
  //////////////////////////////   ATTACHMENT PROCESSING        //////////////////////////
  private ArrayList attachments = new ArrayList();
  
  private String removeAttachId = null;
  private ArrayList prepareRemoveAttach = new ArrayList();
  private boolean attachCaneled = false;
  private ArrayList oldAttachments = new ArrayList();
  private List allAttachments = new ArrayList();

  
  public ArrayList getAttachments()
  {
    ToolSession session = SessionManager.getCurrentToolSession();
    if (session.getAttribute(FilePickerHelper.FILE_PICKER_CANCEL) == null &&
        session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS) != null) 
    {
      List refs = (List)session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
      if(refs != null && refs.size()>0)
      {
        Reference ref = (Reference)refs.get(0);
        
        for(int i=0; i<refs.size(); i++)
        {
          ref = (Reference) refs.get(i);
          Attachment thisAttach = prtMsgManager.createPvtMsgAttachment(
              ref.getId(), ref.getProperties().getProperty(ref.getProperties().getNamePropDisplayName()));
          
          //TODO - remove this as being set for test only  
          //thisAttach.setPvtMsgAttachId(Long.valueOf(1));
          
          attachments.add(new DecoratedAttachment(thisAttach));
          
        }
      }
    }
    session.removeAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
    session.removeAttribute(FilePickerHelper.FILE_PICKER_CANCEL);
    
    return attachments;
  }
  
  public List getAllAttachments()
  {
    ToolSession session = SessionManager.getCurrentToolSession();
    if (session.getAttribute(FilePickerHelper.FILE_PICKER_CANCEL) == null &&
        session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS) != null) 
    {
      List refs = (List)session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
      if(refs != null && refs.size()>0)
      {
        Reference ref = (Reference)refs.get(0);
        
        for(int i=0; i<refs.size(); i++)
        {
          ref = (Reference) refs.get(i);
          Attachment thisAttach = prtMsgManager.createPvtMsgAttachment(
              ref.getId(), ref.getProperties().getProperty(ref.getProperties().getNamePropDisplayName()));
          
          //TODO - remove this as being set for test only
          //thisAttach.setPvtMsgAttachId(Long.valueOf(1));
          allAttachments.add(new DecoratedAttachment(thisAttach));
        }
      }
    }
    session.removeAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
    session.removeAttribute(FilePickerHelper.FILE_PICKER_CANCEL);
    
//    if( allAttachments == null || (allAttachments.size()<1))
//    {
//      allAttachments.addAll(this.getDetailMsg().getMsg().getAttachments()) ;
//    }
    return allAttachments;
  }
  
  public void setAttachments(ArrayList attachments)
  {
    this.attachments = attachments;
  }
  
  public String getRemoveAttachId()
  {
    return removeAttachId;
  }

  public final void setRemoveAttachId(String removeAttachId)
  {
    this.removeAttachId = removeAttachId;
  }
  
  public ArrayList getPrepareRemoveAttach()
  {
    if((removeAttachId != null) && (!"".equals(removeAttachId)))
    {
      prepareRemoveAttach.add(prtMsgManager.getPvtMsgAttachment(Long.valueOf(removeAttachId)));
    }
    
    return prepareRemoveAttach;
  }

  public final void setPrepareRemoveAttach(ArrayList prepareRemoveAttach)
  {
    this.prepareRemoveAttach = prepareRemoveAttach;
  }
  
  //Redirect to File picker
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
      LOG.debug("processAddAttachmentRedirect() - Exception");
      return null;
    }
  }
  
  //Process remove attachment 
  public String processDeleteAttach()
  {
    LOG.debug("processDeleteAttach()");
    
    ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
    String attachId = null;
    
    Map paramMap = context.getRequestParameterMap();
    Iterator<Entry<Object, String>> itr = paramMap.entrySet().iterator();
    while(itr.hasNext())
    {
      Entry<Object, String> entry = itr.next();
    	Object key = entry.getKey();
      if( key instanceof String)
      {
        String name =  (String)key;
        int pos = name.lastIndexOf("pvmsg_current_attach");
        
        if(pos>=0 && name.length()==pos+"pvmsg_current_attach".length())
        {
          attachId = entry.getValue();
          break;
        }
      }
    }
    
    if ((attachId != null) && (!"".equals(attachId)))
    {
      for (int i = 0; i < attachments.size(); i++)
      {
        if (attachId.equalsIgnoreCase(((DecoratedAttachment) attachments.get(i)).getAttachment()
            .getAttachmentId()))
        {
          attachments.remove(i);
          break;
        }
      }
    }
    
    return null ;
  }
 
  
  //Process remove attachments from reply message  
  public String processDeleteReplyAttach()
  {
    LOG.debug("processDeleteReplyAttach()");
    
    ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
    String attachId = null;
    
    Map paramMap = context.getRequestParameterMap();
    Iterator<Entry<Object, String>> itr = paramMap.entrySet().iterator();
    while(itr.hasNext())
    {
      Entry<Object, String> entry = itr.next();
      Object key = entry.getKey();
      if( key instanceof String)
      {
        String name =  (String)key;
        int pos = name.lastIndexOf("remsg_current_attach");
        
        if(pos>=0 && name.length()==pos+"remsg_current_attach".length())
        {
          attachId = entry.getValue();
          break;
        }
      }
    }
    
    if ((attachId != null) && (!"".equals(attachId)))
    {
      for (int i = 0; i < allAttachments.size(); i++)
      {
        if (attachId.equalsIgnoreCase(((DecoratedAttachment) allAttachments.get(i)).getAttachment()
            .getAttachmentId()))
        {
          allAttachments.remove(i);
          break;
        }
      }
    }
    
    return null ;
  }
  
  //process deleting confirm from separate screen
  public String processRemoveAttach()
  {
    LOG.debug("processRemoveAttach()");
    
    try
    {
      Attachment sa = prtMsgManager.getPvtMsgAttachment(Long.valueOf(removeAttachId));
      String id = sa.getAttachmentId();
      
      for(int i=0; i<attachments.size(); i++)
      {
      	DecoratedAttachment thisAttach = (DecoratedAttachment)attachments.get(i);
        if(((Long)thisAttach.getAttachment().getPvtMsgAttachId()).toString().equals(removeAttachId))
        {
          attachments.remove(i);
          break;
        }
      }
      
      prtMsgManager.removePvtMsgAttachment(sa);
      if(id.toLowerCase().startsWith("/attachment"))
        contentHostingService.removeResource(id);
    }
    catch(Exception e)
    {
      LOG.debug("processRemoveAttach() - Exception");
    }
    
    removeAttachId = null;
    prepareRemoveAttach.clear();
    return COMPOSE_MSG_PG;
    
  }
  
  public String processRemoveAttachCancel()
  {
    LOG.debug("processRemoveAttachCancel()");
    
    removeAttachId = null;
    prepareRemoveAttach.clear();
    return COMPOSE_MSG_PG ;
  }
  

  ////////////  SETTINGS        //////////////////////////////
  //Setting Getter and Setter
  public String getActivatePvtMsg()
  {
    return activatePvtMsg;
  }
  public void setActivatePvtMsg(String activatePvtMsg)
  {
    this.activatePvtMsg = activatePvtMsg;
  }
  public String getForwardPvtMsg()
  {
    return forwardPvtMsg;
  }
  public void setForwardPvtMsg(String forwardPvtMsg)
  {
    this.forwardPvtMsg = forwardPvtMsg;
  }
  public String getForwardPvtMsgEmail()
  {
    return forwardPvtMsgEmail;
  }
  public void setForwardPvtMsgEmail(String forwardPvtMsgEmail)
  {
    this.forwardPvtMsgEmail = forwardPvtMsgEmail;
  }
  
  public String getSendToEmail() {
      return this.sendToEmail;
  }
  public void setSendToEmail(String sendToEmail) {
      this.sendToEmail = sendToEmail;
  }
  
  public boolean getSuperUser()
  {
    superUser=SecurityService.isSuperUser();
    return superUser;
  }
  //is instructor
  public boolean isInstructor()
  {
    return prtMsgManager.isInstructor();
  }
  
  public boolean isEmailPermit() {
	  return prtMsgManager.isEmailPermit();
  }
  
  public void setSuperUser(boolean superUser)
  {
    this.superUser = superUser;
  }
  
  public String processPvtMsgOrganize()
  {
    LOG.debug("processPvtMsgOrganize()");
    return null ;
    //return "pvtMsgOrganize";
  }

  public String processPvtMsgStatistics()
  {
    LOG.debug("processPvtMsgStatistics()");
    
    return null ;
    //return "pvtMsgStatistics";
  }
  
  public String processPvtMsgSettings()
  {
    LOG.debug("processPvtMsgSettings()");    
    return MESSAGE_SETTING_PG;
  }
    
  public void processPvtMsgSettingsRevise(ValueChangeEvent event)
  {
    LOG.debug("processPvtMsgSettingsRevise()");   
    
    /** block executes when changing value to "no" */
    if (SET_AS_YES.equals(forwardPvtMsg)){
      setForwardPvtMsgEmail(null);      
    }       
    if (SET_AS_NO.equals(forwardPvtMsg)){
      setValidEmail(true);
    }
  }
  
  public String processPvtMsgSettingsSave()
  {
    LOG.debug("processPvtMsgSettingsSave()");
    
 
    String email= getForwardPvtMsgEmail();
    String activate=getActivatePvtMsg() ;
    String forward=getForwardPvtMsg() ;
    if (email != null && (!SET_AS_NO.equals(forward)) 
            && !EmailValidator.getInstance().isValid(email) ) {
      setValidEmail(false);
      setErrorMessage(getResourceBundleString(PROVIDE_VALID_EMAIL));
      setActivatePvtMsg(activate);
      return null;
    }
    else
    {
      Area area = prtMsgManager.getPrivateMessageArea();            
      
      Boolean formAreaEnabledValue = (SET_AS_YES.equals(activate)) ? Boolean.TRUE : Boolean.FALSE;
      area.setEnabled(formAreaEnabledValue);
      
      try {
          int formSendToEmail = Integer.parseInt(sendToEmail);
          area.setSendToEmail(formSendToEmail);
      } catch (NumberFormatException nfe) {
          // if this happens, there is likely something wrong in the UI that needs to be fixed
          LOG.warn("Non-numeric option for sending email to recipient email address on Message screen. This may indicate a UI problem.");
          setErrorMessage(getResourceBundleString("pvt_send_to_email_invalid"));
          return null;
      }
      
      
      Boolean formAutoForward = (SET_AS_YES.equals(forward)) ? Boolean.TRUE : Boolean.FALSE;            
      forum.setAutoForward(formAutoForward);
      if (Boolean.TRUE.equals(formAutoForward)){
        forum.setAutoForwardEmail(email);  
      }
      else{
        forum.setAutoForwardEmail(null);  
      }
      
      area.setHiddenGroups(new HashSet(hiddenGroups));
             
      prtMsgManager.saveAreaAndForumSettings(area, forum);

      if (isMessagesandForums()) {
    	  return MAIN_PG;
      }
      else {
    	  return MESSAGE_HOME_PG;
      }
    }
    
  }
  

  ///////////////////   FOLDER SETTINGS         ///////////////////////
  private String addFolder;
  private boolean ismutable;
  private int totalMsgInFolder;
  public String getAddFolder()
  {
    return addFolder ;    
  }
  public void setAddFolder(String addFolder)
  {
    this.addFolder=addFolder;
  }
  
  public boolean getIsmutable()
  {
    return prtMsgManager.isMutableTopicFolder(getSelectedTopicId());
  }
  
  public int getTotalMsgInFolder()
  {
    return totalMsgInFolder;
  }

  public void setTotalMsgInFolder(int totalMsgInFolder)
  {
    this.totalMsgInFolder = totalMsgInFolder;
  }


  //navigated from header pagecome from Header page 
  public String processPvtMsgFolderSettings() {
    LOG.debug("processPvtMsgFolderSettings()");
    //String topicTitle= getExternalParameterByKey("pvtMsgTopicTitle");
    String topicTitle = forumManager.getTopicByUuid(getExternalParameterByKey("pvtMsgTopicId")).getTitle();
    setSelectedTopicTitle(topicTitle) ;
    String topicId=getExternalParameterByKey("pvtMsgTopicId") ;
    setSelectedTopicId(topicId);
    
    setFromMainOrHp();
    
    return MESSAGE_FOLDER_SETTING_PG;
  }

  public String processPvtMsgFolderSettingRevise() {
    LOG.debug("processPvtMsgFolderSettingRevise()");
    
    if(this.ismutable)
    {
      return null;
    }else 
    {
      selectedNewTopicTitle = selectedTopicTitle;
      return REVISE_FOLDER_PG ;
    }    
  }
  
  public String processPvtMsgFolderSettingAdd() {
    LOG.debug("processPvtMsgFolderSettingAdd()");
    
    setFromMainOrHp();
    this.setAddFolder("");  // make sure the input box is empty
    
    return ADD_MESSAGE_FOLDER_PG ;
  }
  public String processPvtMsgFolderSettingDelete() {
    LOG.debug("processPvtMsgFolderSettingDelete()");
    
    setFromMainOrHp();
    
    String typeUuid = getPrivateMessageTypeFromContext(selectedTopicTitle);          
    
    setTotalMsgInFolder(prtMsgManager.findMessageCount(typeUuid, aggregateList));
    
    if(ismutable)
    {
      setErrorMessage(getResourceBundleString(CANNOT_DEL_REVISE_FOLDER));
      return null;
    }else {
      setErrorMessage(getResourceBundleString(CONFIRM_FOLDER_DELETE));
      return DELETE_FOLDER_PG;
    }    
  }
  
  public String processPvtMsgReturnToMainOrHp()
  {
	  LOG.debug("processPvtMsgReturnToMainOrHp()");
	    if(fromMainOrHp != null && (fromMainOrHp.equals(MESSAGE_HOME_PG) || (fromMainOrHp.equals(MAIN_PG))))
	    {
	    	String returnToPage = fromMainOrHp;
			fromMainOrHp = "";
			return returnToPage;
	    }
	    else
	    {
	    	return MAIN_PG ;
	    }
  }
  
  public String processPvtMsgReturnToFolderView() 
  {
	  return MESSAGE_FOLDER_SETTING_PG;
  }
  
  //Create a folder within a forum
  public String processPvtMsgFldCreate() 
  {
    LOG.debug("processPvtMsgFldCreate()");
    
    String createFolder=getAddFolder() ;   
    StringBuilder alertMsg = new StringBuilder();
    createFolder = FormattedText.processFormattedText(createFolder, alertMsg);
    if(createFolder == null || createFolder.trim().length() == 0)
    {
    	setErrorMessage(getResourceBundleString(ENTER_FOLDER_NAME));
      	return null ;
    } else if((PVTMSG_MODE_RECEIVED.toLowerCase()).equals(createFolder.toLowerCase().trim()) || (PVTMSG_MODE_SENT.toLowerCase()).equals(createFolder.toLowerCase().trim())|| 
    		 (PVTMSG_MODE_DELETE.toLowerCase()).equals(createFolder.toLowerCase().trim()) || (PVTMSG_MODE_DRAFT.toLowerCase()).equals(createFolder.toLowerCase().trim()))
    {
    	setErrorMessage(getResourceBundleString(CREATE_DIFF_FOLDER_NAME));
    	return null;
    } else if(createFolder.length() > 100) 
    {
    	setErrorMessage(getResourceBundleString(ENTER_SHORTER_NAME));
    	return null;      
    } else 
    {
        prtMsgManager.createTopicFolderInForum(forum, createFolder);
      //create a typeUUID in commons
       String newTypeUuid= typeManager.getCustomTopicType(createFolder); 
    }
      //since PrivateMessagesTool has a session scope, 
      //reset addFolder to blank for new form
      addFolder = "";
      return processPvtMsgReturnToMainOrHp();
   }  
  
  private String selectedNewTopicTitle=selectedTopicTitle;  //default
  public String getSelectedNewTopicTitle()
  {
    return selectedNewTopicTitle;
  }
  public void setSelectedNewTopicTitle(String selectedNewTopicTitle)
  {
    this.selectedNewTopicTitle = selectedNewTopicTitle;
  }


  /** 
   * revise
   **/
  public String processPvtMsgFldRevise() 
  {
    LOG.debug("processPvtMsgFldRevise()");
    
    String newTopicTitle = this.getSelectedNewTopicTitle(); 
    
    if(!hasValue(newTopicTitle))
    {
      setErrorMessage(getResourceBundleString(FOLDER_NAME_BLANK));
      return REVISE_FOLDER_PG;
    }
    else if((PVTMSG_MODE_RECEIVED.toLowerCase()).equals(newTopicTitle.toLowerCase().trim()) || (PVTMSG_MODE_SENT.toLowerCase()).equals(newTopicTitle.toLowerCase().trim())|| 
   		 (PVTMSG_MODE_DELETE.toLowerCase()).equals(newTopicTitle.toLowerCase().trim()) || (PVTMSG_MODE_DRAFT.toLowerCase()).equals(newTopicTitle.toLowerCase().trim()))
    {
   	  setErrorMessage(getResourceBundleString(CREATE_DIFF_FOLDER_NAME));
   	  return REVISE_FOLDER_PG;
    } 
    else if(newTopicTitle.length() > 100) 
    {
   	  setErrorMessage(getResourceBundleString(ENTER_SHORTER_NAME));
   	  return REVISE_FOLDER_PG;      
    } 
    else {
      List tmpMsgList = prtMsgManager.getMessagesByType(typeManager.getCustomTopicType(prtMsgManager.getTopicByUuid(selectedTopicId).getTitle()), PrivateMessageManager.SORT_COLUMN_DATE,
          PrivateMessageManager.SORT_ASC);
      prtMsgManager.renameTopicFolder(forum, selectedTopicId,  newTopicTitle);
      //rename topic in commons -- as messages are linked through commons type
      //TODO - what if more than one type objects are returned-- We need to switch from title
      String newTypeUuid = typeManager.renameCustomTopicType(selectedTopicTitle, newTopicTitle);
      for(int i=0; i<tmpMsgList.size(); i++)
      {
      	PrivateMessage tmpPM = (PrivateMessage) tmpMsgList.get(i);
      	List tmpRecipList = tmpPM.getRecipients();
      	tmpPM.setTypeUuid(newTypeUuid);
      	String currentUserId = SessionManager.getCurrentSessionUserId();
      	Iterator iter = tmpRecipList.iterator();
      	while(iter.hasNext())
      	{
      		PrivateMessageRecipient tmpPMR = (PrivateMessageRecipient) iter.next();
      		if(tmpPMR != null && tmpPMR.getUserId().equals(currentUserId))
      		{
      			tmpPMR.setTypeUuid(newTypeUuid);
      		}
      	}
      	tmpPM.setRecipients(tmpRecipList);
      	prtMsgManager.savePrivateMessage(tmpPM, false);
      }
    }
    setSelectedTopicTitle(newTopicTitle) ;
    return MESSAGE_FOLDER_SETTING_PG ;
  }
  
  //Delete
  public String processPvtMsgFldDelete() 
  {
    LOG.debug("processPvtMsgFldDelete()");
    
    prtMsgManager.deleteTopicFolder(forum,getSelectedTopicId()) ;
    
    //delete the messages
    String typeUuid = getPrivateMessageTypeFromContext(selectedTopicTitle);
    List allPvtMsgs= prtMsgManager.getMessagesByType(typeUuid,PrivateMessageManager.SORT_COLUMN_DATE,
        PrivateMessageManager.SORT_DESC);
    for (Iterator iter = allPvtMsgs.iterator(); iter.hasNext();)
    {
      PrivateMessage element = (PrivateMessage) iter.next();
      prtMsgManager.deletePrivateMessage(element, typeUuid);
    }
    return processPvtMsgReturnToMainOrHp();
  }
  
  //create folder within folder
  public String processPvtMsgFolderInFolderAdd()
  {
    LOG.debug("processPvtMsgFolderSettingAdd()");  
    
    setFromMainOrHp();
    this.setAddFolder("");
    
    return ADD_FOLDER_IN_FOLDER_PG ;
  }
 
  //create folder within Folder
  //TODO - add parent fodler id for this  
  public String processPvtMsgFldInFldCreate() 
  {
    LOG.debug("processPvtMsgFldCreate()");
    
    PrivateTopic parentTopic=(PrivateTopic) prtMsgManager.getTopicByUuid(selectedTopicId);
    
    String createFolder=getAddFolder() ;
    if(createFolder == null || createFolder.trim().length() == 0)
    {
      setErrorMessage(getResourceBundleString(ENTER_FOLDER_NAME));
      return null ;
    } 
    else if((PVTMSG_MODE_RECEIVED.toLowerCase()).equals(createFolder.toLowerCase().trim()) || (PVTMSG_MODE_SENT.toLowerCase()).equals(createFolder.toLowerCase().trim())|| 
   		 (PVTMSG_MODE_DELETE.toLowerCase()).equals(createFolder.toLowerCase().trim()) || (PVTMSG_MODE_DRAFT.toLowerCase()).equals(createFolder.toLowerCase().trim()))
    {
      setErrorMessage(getResourceBundleString(CREATE_DIFF_FOLDER_NAME));
   	  return null;
    } 
    else if(createFolder.length() > 100) 
    {
   	  setErrorMessage(getResourceBundleString(ENTER_SHORTER_NAME));
   	  return null;      
    }
    else 
    {
        prtMsgManager.createTopicFolderInTopic(forum, parentTopic, createFolder);
      //create a typeUUID in commons
      String newTypeUuid= typeManager.getCustomTopicType(createFolder); 
    }
      
      addFolder = "";
      return processPvtMsgReturnToMainOrHp();
    
  } 
  ///////////////////// MOVE    //////////////////////
  private String moveToTopic="";
  private String moveToNewTopic="";
  
  public String getMoveToTopic()
  {
    if(hasValue(moveToNewTopic))
    {
      moveToTopic=moveToNewTopic;
    }
    return moveToTopic;
  }
  public void setMoveToTopic(String moveToTopic)
  {
    this.moveToTopic = moveToTopic;
  }

  /**
   * called from Single delete Page
   * @return - pvtMsgMove
   */ 
  public String processPvtMsgMove() {
    LOG.debug("processPvtMsgMove()");
    return MOVE_MESSAGE_PG;
  }
  
  public void processPvtMsgParentFolderMove(ValueChangeEvent event)
  {
    LOG.debug("processPvtMsgSettingsRevise()"); 
    if ((String)event.getNewValue() != null)
    {
      moveToNewTopic= (String)event.getNewValue();
    }
  }
  
  public String processPvtMsgMoveMessage()
  {
    LOG.debug("processPvtMsgMoveMessage()");
    String moveTopicTitle=getMoveToTopic(); //this is uuid of new topic
    if( moveTopicTitle == null || moveTopicTitle.trim().length() == 0){
    	setErrorMessage(getResourceBundleString(MOVE_MSG_ERROR));
    	return null;
    }
    
    Topic newTopic= prtMsgManager.getTopicByUuid(moveTopicTitle);
    Topic oldTopic=selectedTopic.getTopic();
    if (newTopic.getUuid().equals(oldTopic.getUuid())) {
    	//error
    	setErrorMessage(getResourceBundleString(MOVE_MSG_ERROR));
    	return null;
    }
    else{
    	if (selectedMoveToFolderItems == null)
    	{
    		prtMsgManager.movePvtMsgTopic(detailMsg.getMsg(), oldTopic, newTopic);
    	}
    	else
    	{
    		for (Iterator movingIter = selectedMoveToFolderItems.iterator(); movingIter.hasNext();)
    		{
    			PrivateMessageDecoratedBean decoMessage = (PrivateMessageDecoratedBean) movingIter.next();
		        final PrivateMessage initPrivateMessage = prtMsgManager.initMessageWithAttachmentsAndRecipients(decoMessage.getMsg());
    			decoMessage = new PrivateMessageDecoratedBean(initPrivateMessage);
    			
    			prtMsgManager.movePvtMsgTopic(decoMessage.getMsg(), oldTopic, newTopic);
    		}
    	}
	    
		//reset 
		moveToTopic="";
		moveToNewTopic="";
		selectedMoveToFolderItems = null;
		
    	// Return to Messages & Forums page or Messages page
   		if (isMessagesandForums()) 
   		{
   			return MAIN_PG;
   		}
   		else
   		{
   			return MESSAGE_HOME_PG;
   		}
    }
  }
  
  /**
   * 
   * @return
   */
  public String processPvtMsgCancelToDetailView()
  {
    LOG.debug("processPvtMsgCancelToDetailView()");
    this.deleteConfirm=false;
    
    // due to adding ability to move multiple messages
    if (selectedMoveToFolderItems != null)
    {
    	selectedMoveToFolderItems = null;
    	return DISPLAY_MESSAGES_PG;
    }
    
    return SELECTED_MESSAGE_PG;
  }
  
  ///////////////   SEARCH      ///////////////////////
  private List searchPvtMsgs;
  public List getSearchPvtMsgs()
  {
    if(selectView!=null && selectView.equalsIgnoreCase(THREADED_VIEW))
    {
        this.rearrageTopicMsgsThreaded(true);
    }
    //  If "check all", update the decorated pmb to show selected
    if (selectAll)
    {
    	Iterator searchIter = searchPvtMsgs.iterator();
    	while (searchIter.hasNext())
    	{
    		PrivateMessageDecoratedBean searchMsg = (PrivateMessageDecoratedBean)searchIter.next();
    		searchMsg.setIsSelected(true);
    	}
    	
    	selectAll = false;
    }
    return searchPvtMsgs;
  }
  public void setSearchPvtMsgs(List searchPvtMsgs)
  {
    this.searchPvtMsgs=searchPvtMsgs ;
  }
  public String processSearch() 
  {
    LOG.debug("processSearch()");
    multiDeleteSuccess = false;

    List newls = new ArrayList() ;
//    for (Iterator iter = getDecoratedPvtMsgs().iterator(); iter.hasNext();)
//    {
//      PrivateMessageDecoratedBean element = (PrivateMessageDecoratedBean) iter.next();
//      
//      String message=element.getMsg().getTitle();
//      String searchText = getSearchText();
//      //if search on subject is set - default is true
//      if(searchOnSubject)
//      {
//        StringTokenizer st = new StringTokenizer(message);
//        while (st.hasMoreTokens())
//        {
//          if(st.nextToken().matches("(?i).*getSearchText().*")) //matches anywhere 
//          //if(st.nextToken().equalsIgnoreCase(getSearchText()))            
//          {
//            newls.add(element) ;
//            break;
//          }
//        }
//      }
//      //is search 
//      
//    }

    /**TODO - 
     * In advance srearch as there can be ANY type of combination like selection of 
     * ANY 1 or 2 or 3 or 4 or 5 options like - subject, Author By, label, body and date
     * so all possible cases are taken care off.
     * This doesn't look nice, but good this is that we are using same method - our backend is cleaner
     * First - checked if date is selected then purmutation of 4 options
     * ELSE - permutations of other 4 options
     */ 
    List tempPvtMsgLs= new ArrayList();
    
    if(searchOnDate && searchFromDate == null && searchToDate==null)
    {
       setErrorMessage(getResourceBundleString(MISSING_BEG_END_DATE));
    }
    
    if(!hasValue(searchText))
    {
       setErrorMessage(getResourceBundleString(ENTER_SEARCH_TEXT));
    }
    
    tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), 
          getSearchText(), getSearchFromDate(), getSearchToDate(),
          searchOnSubject, searchOnAuthor, searchOnBody, searchOnLabel, searchOnDate) ;
    
    newls= createDecoratedDisplay(tempPvtMsgLs);
//    
//    for (Iterator iter = tempPvtMsgLs.iterator(); iter.hasNext();)
//    {
//      PrivateMessage element = (PrivateMessage) iter.next();
//      PrivateMessageDecoratedBean dbean = new PrivateMessageDecoratedBean(element);
//      
//      //getRecipients() is filtered for this perticular user i.e. returned list of only one PrivateMessageRecipient object
//      for (Iterator iterator = element.getRecipients().iterator(); iterator.hasNext();)
//      {
//        PrivateMessageRecipient el = (PrivateMessageRecipient) iterator.next();
//        if (el != null){
//          if(!el.getRead().booleanValue())
//          {
//            dbean.setHasRead(el.getRead().booleanValue());
//            break;
//          }
//        }
//      }
//      //Add decorate 'TO' String for sent message
//      if(PVTMSG_MODE_SENT.equals(msgNavMode))
//      {
//        dbean.setSendToStringDecorated(createDecoratedSentToDisplay(dbean)); 
//      }
//      newls.add(dbean);     
//    }
    //set threaded view as  false in search 
    selectView="";
    
    if(newls.size()>0)
    {
      this.setSearchPvtMsgs(newls) ;
      return SEARCH_RESULT_MESSAGES_PG ;
    }
    else 
      {
        setErrorMessage(getResourceBundleString(NO_MATCH_FOUND));
        return null;
      }    
  }
  
  /**
   * Clear Search text
   */
  public String processClearSearch()
  {
    searchText="";
    if(searchPvtMsgs != null)
    {
      searchPvtMsgs.clear();
      //searchPvtMsgs= decoratedPvtMsgs;   
    }
    
    searchOnBody=false ;
    searchOnSubject=true;
    searchOnLabel= false ;
    searchOnAuthor=false;
    searchOnDate=false;
    searchFromDate=null;
    searchToDate=null;
    
    return DISPLAY_MESSAGES_PG;
  }
  
  public boolean searchOnBody=false ;
  public boolean searchOnSubject=true;  //default is search on Subject
  public boolean searchOnLabel= false ;
  public boolean searchOnAuthor=false;
  public boolean searchOnDate=false;
  public Date searchFromDate;
  public Date searchToDate; 
  
  public boolean isSearchOnAuthor()
  {
    return searchOnAuthor;
  }
  public void setSearchOnAuthor(boolean searchOnAuthor)
  {
    this.searchOnAuthor = searchOnAuthor;
  }
  public boolean isSearchOnBody()
  {
    return searchOnBody;
  }
  public void setSearchOnBody(boolean searchOnBody)
  {
    this.searchOnBody = searchOnBody;
  }
  public boolean isSearchOnLabel()
  {
    return searchOnLabel;
  }
  public void setSearchOnLabel(boolean searchOnLabel)
  {
    this.searchOnLabel = searchOnLabel;
  }
  public boolean isSearchOnSubject()
  {
    return searchOnSubject;
  }
  public void setSearchOnSubject(boolean searchOnSubject)
  {
    this.searchOnSubject = searchOnSubject;
  }
  public boolean isSearchOnDate()
  {
    return searchOnDate;
  }
  public void setSearchOnDate(boolean searchOnDate)
  {
    this.searchOnDate = searchOnDate;
  }
  public Date getSearchFromDate()
  {
    return searchFromDate;
  }
  public void setSearchFromDate(Date searchFromDate)
  {
    this.searchFromDate = searchFromDate;
  }
  public Date getSearchToDate()
  {
    return searchToDate;
  }
  public void setSearchToDate(Date searchToDate)
  {
    this.searchToDate = searchToDate;
  }


  //////////////        HELPER      //////////////////////////////////
  /**
   * @return
   */
  private String getExternalParameterByKey(String parameterId)
  {    
    ExternalContext context = FacesContext.getCurrentInstance()
        .getExternalContext();
    Map paramMap = context.getRequestParameterMap();
    
    return (String) paramMap.get(parameterId);    
  }

  
  /**
   * decorated display - from List of Message
   */
  public List createDecoratedDisplay(List msg)
  {
    List decLs= new ArrayList() ;
    numberChecked = 0;

    for (Iterator iter = msg.iterator(); iter.hasNext();)
    {
      PrivateMessage element = (PrivateMessage) iter.next();                  
      
      PrivateMessageDecoratedBean dbean= new PrivateMessageDecoratedBean(element);
      //if processSelectAll is set, then set isSelected true for all messages,
      if(selectAll)
      {
        dbean.setIsSelected(true);
        numberChecked++;
      }
       
      //getRecipients() is filtered for this particular user i.e. returned list of only one PrivateMessageRecipient object
      for (Iterator iterator = element.getRecipients().iterator(); iterator.hasNext();)
      {
        PrivateMessageRecipient el = (PrivateMessageRecipient) iterator.next();
        if (el != null){
          dbean.setHasRead(el.getRead().booleanValue());
          dbean.setReplied(el.getReplied().booleanValue());
        }
      }
      //Add decorate 'TO' String for sent message
      if(PVTMSG_MODE_SENT.equals(msgNavMode))
      {
        dbean.setSendToStringDecorated(createDecoratedSentToDisplay(dbean)); 
      }

      decLs.add(dbean) ;
    }
    //reset selectAll flag, for future use
    selectAll=false;
    return decLs;
  }
  
  /**
   * create decorated display for Sent To display for Sent folder
   * @param dbean
   * @return
   */
  public String createDecoratedSentToDisplay(PrivateMessageDecoratedBean dbean)
  {
    String deocratedToDisplay="";
    if(dbean.getMsg().getRecipientsAsText() != null)
    {
      if (dbean.getMsg().getRecipientsAsText().length()>25)
      {
        deocratedToDisplay=(dbean.getMsg().getRecipientsAsText()).substring(0, 20)+" (..)";
      } else
      {
        deocratedToDisplay=dbean.getMsg().getRecipientsAsText();
      }
    }
    return deocratedToDisplay;
  }
  
  /**
   * get recipients
   * @return a set of recipients (User objects)
   */
  private Map<User, Boolean> getRecipients()
  {     
	  Map<User, Boolean> returnSet = new HashMap<User, Boolean>();
    
    /** get List of unfiltered course members */
    List allCourseUsers = membershipManager.getAllCourseUsers();    
    
    Map<User, Boolean> composeToSet = getRecipientsHelper(getSelectedComposeToList(), allCourseUsers, false);
    Map<User, Boolean> composeBccSet = getRecipientsHelper(getSelectedComposeBccList(), allCourseUsers, true);

    //first add the BCC list, then remove the duplicates, then add the regular To list
    //Do this to make the regular TO list have precident over the BCC list.  This is done
    //because of the recipientsAsText list that is created.  When replying, it would cause 
    //names to show up that are BCC'ed, and who don't actually get replies
    returnSet.putAll(composeBccSet);
    //remove all duplicates by doing this first:
    for (Iterator iterator = composeToSet.keySet().iterator(); iterator.hasNext();) {
    	User user = (User) iterator.next();
    	if(returnSet.containsKey(user)){
    		returnSet.remove(user);
    	}
    }
    //now add them all back
    returnSet.putAll(composeToSet);
    
    return returnSet;
  }
  //=========HUXT BEGIN
  
  private Map<User, Boolean> getRecipientsHelper(List selectedList, List allCourseUsers, boolean bcc){

	  Map<User, Boolean>  returnSet = new HashMap<User, Boolean>();

	  for (Iterator i = selectedList.iterator(); i.hasNext();){
		  String selectedItem = (String) i.next();

		  /** lookup item in map */
		  MembershipItem item = (MembershipItem) courseMemberMap.get(selectedItem);
		  if (item == null){
			  LOG.warn("getRecipients() could not resolve uuid: " + selectedItem);
		  }
		  else{                              
			  if (MembershipItem.TYPE_ALL_PARTICIPANTS.equals(item.getType())){
				  for (Iterator a = allCourseUsers.iterator(); a.hasNext();){
					  MembershipItem member = (MembershipItem) a.next();            
					  returnSet.put(member.getUser(), bcc);
				  }
				  //if all users have been selected we may as well return and ignore any other entries
				  return returnSet;
			  }
			  else if (MembershipItem.TYPE_ROLE.equals(item.getType())){
				  for (Iterator r = allCourseUsers.iterator(); r.hasNext();){
					  MembershipItem member = (MembershipItem) r.next();
					  if (member.getRole().equals(item.getRole())){
						  returnSet.put(member.getUser(), bcc);
					  }
				  }
			  }
			  else if (MembershipItem.TYPE_GROUP.equals(item.getType()) || MembershipItem.TYPE_MYGROUPS.equals(item.getType())){
				  for (Iterator g = allCourseUsers.iterator(); g.hasNext();){
					  MembershipItem member = (MembershipItem) g.next();            
					  Set groupMemberSet = item.getGroup().getMembers();
					  for (Iterator s = groupMemberSet.iterator(); s.hasNext();){
						  Member m = (Member) s.next();
						  if (m.getUserId() != null && m.getUserId().equals(member.getUser().getId())){
							  returnSet.put(member.getUser(), bcc);
						  }
					  }            
				  }
			  }
			  else if (MembershipItem.TYPE_USER.equals(item.getType()) || MembershipItem.TYPE_MYGROUPMEMBERS.equals(item.getType())){
				  returnSet.put(item.getUser(), bcc);
			  } 
			  else{
				  LOG.warn("getRecipients() could not resolve membership type: " + item.getType());
			  }
		  }             
	  }
	  return returnSet;
  }
  
  //=========huxt ENG
  /**
   * getUserRecipients
   * @param courseMembers
   * @return set of all User objects for course
   */
  private Set getUserRecipients(Set courseMembers){    
    Set returnSet = new HashSet();
    
    for (Iterator i = courseMembers.iterator(); i.hasNext();){
      MembershipItem item = (MembershipItem) i.next();      
        returnSet.add(item.getUser());
    }    
    return returnSet;    
  }
  
  /**
   * getUserRecipientsForRole
   * @param roleName
   * @param courseMembers
   * @return set of all User objects for role
   */
  private Set getUserRecipientsForRole(String roleName, Set courseMembers){    
    Set returnSet = new HashSet();
    
    for (Iterator i = courseMembers.iterator(); i.hasNext();){
      MembershipItem item = (MembershipItem) i.next();
      if (item.getRole().getId().equalsIgnoreCase(roleName)){
        returnSet.add(item.getUser());   
      }
    }    
    return returnSet;    
  }
      
  private String getPrivateMessageTypeFromContext(String navMode){    
    if(PVTMSG_MODE_RECEIVED.equals(navMode))
        return typeManager.getReceivedPrivateMessageType();
   	else if(PVTMSG_MODE_SENT.equals(navMode))
        return typeManager.getSentPrivateMessageType();
   	else if(PVTMSG_MODE_DELETE.equals(navMode))
        return typeManager.getDeletedPrivateMessageType(); 
   	else if (PVTMSG_MODE_DRAFT.equalsIgnoreCase(navMode))
   		return typeManager.getDraftPrivateMessageType();
   	else
   		return typeManager.getCustomTopicType(navMode);    
  }

  //////// GETTER AND SETTER  ///////////////////  
  public String processUpload(ValueChangeEvent event)
  {
    return DISPLAY_MESSAGES_PG ; 
  }
  
  public String processUploadConfirm()
  {
    return DISPLAY_MESSAGES_PG;
  }
  
  public String processUploadCancel()
  {
    return DISPLAY_MESSAGES_PG ;
  }


  public void setForumManager(MessageForumsForumManager forumManager)
  {
    this.forumManager = forumManager;
  }


  public PrivateForum getForum()
  {            
    return forum;
  }


  public void setForum(PrivateForum forum)
  {
    this.forum = forum;
  }
 
// public String processActionAreaSettingEmailFwd()
// {
//   
// }
  
  /**
   * Check String has value, not null
   * @return boolean
   */
  protected boolean hasValue(String eval)
  {
    if (eval != null && !"".equals(eval.trim()))
    {
      return true;
    }
    else
    {
      return false;
    }
  }
  
  /**
   * @param errorMsg
   */
  private void setErrorMessage(String errorMsg)
  {
    LOG.debug("setErrorMessage(String " + errorMsg + ")");
    FacesContext.getCurrentInstance().addMessage(null,
        new FacesMessage(getResourceBundleString(ALERT) + ' ' + errorMsg));
  }

  private void setInformationMessage(String infoMsg)
  {
	    LOG.debug("setInformationMessage(String " + infoMsg + ")");
	    FacesContext.getCurrentInstance().addMessage(null,
	        new FacesMessage(infoMsg));
  }

 
  /**
   * Enable privacy message
   * @return
   */
  public boolean getRenderPrivacyAlert()
  {
   if(ServerConfigurationService.getString(MESSAGECENTER_PRIVACY_TEXT)!=null &&
       ServerConfigurationService.getString(MESSAGECENTER_PRIVACY_TEXT).trim().length()>0 )
   {
     return true;
   }
    return false;
  }
  
  /**
   * Get Privacy message link  from sakai.properties
   * @return
   */
  public String getPrivacyAlertUrl()
  {
    return ServerConfigurationService.getString(MESSAGECENTER_PRIVACY_URL);
  }
  
  /**
   * Get Privacy message from sakai.properties
   * @return
   */
  public String getPrivacyAlert()
  {
    return ServerConfigurationService.getString(MESSAGECENTER_PRIVACY_TEXT);
  }
  
  public boolean isAtMain(){
    HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
    
    String servletPath = req.getServletPath();
    if (servletPath != null){
      if (servletPath.startsWith("/jsp/main")){
        return true;
      }      
    }
    return false;
  }


	public String getSortType() {
		return sortType;
	}


	public void setSortType(String sortType) {
		this.sortType = sortType;
	}
	
    public static String getResourceBundleString(String key) 
    {
        return rb.getString(key);
    }
    
    public static String getResourceBundleString(String key, Object[] args) {
    	return rb.getFormattedMessage(key, args);
    }


    public String getAuthorString() 
    {
       String authorString = getUserId();
       
       try
       {
    	 User user = UserDirectoryService.getUser(getUserId());
    	 if (ServerConfigurationService.getBoolean("msg.displayEid", true))
    	 {
    		 authorString = user.getSortName() + " (" + user.getDisplayId() + ")";
    	 }
    	 else
    	 {
    		 authorString = user.getSortName();
    	 }
       }
       catch(Exception e)
       {
         e.printStackTrace();
       }
       
       return authorString;
    }
    
    public String getPlacementId() 
    {
       return Validator.escapeJavascript("Main" + ToolManager.getCurrentPlacement().getId());
    }

    public boolean isSearchPvtMsgsEmpty()
    {
    	return searchPvtMsgs == null || searchPvtMsgs.isEmpty();
    }

    public void setMsgNavMode(String msgNavMode) {
		this.msgNavMode = msgNavMode;
	}	
	
	/**
	 * @return
	 */
	public String processActionMarkCheckedAsRead()
	{
		return markCheckedMessages(true);
	}
	
	public String processActionMarkCheckedAsUnread() {
		return markCheckedMessages(false);
	}
	
	public String processActionDeleteChecked() {
	    LOG.debug("processActionDeleteChecked()");

		List pvtMsgList = getPvtMsgListToProcess();
		boolean msgSelected = false;
		selectedDeleteItems = new ArrayList();
		
		Iterator pvtMsgListIter = pvtMsgList.iterator(); 
		while (pvtMsgListIter.hasNext())
		{
			PrivateMessageDecoratedBean decoMessage = (PrivateMessageDecoratedBean) pvtMsgListIter.next();
			if(decoMessage.getIsSelected())
			{
				msgSelected = true;
				selectedDeleteItems.add(decoMessage);
			}
		}

		if (!msgSelected)
		{
			setErrorMessage(getResourceBundleString(NO_MARKED_DELETE_MESSAGE));
			return null;
		}
		
		return processPvtMsgMultiDelete();
	}
	
	public String processActionMoveCheckedToFolder() {
	    LOG.debug("processActionMoveCheckedToFolder()");

	    List pvtMsgList = getPvtMsgListToProcess();
	    boolean msgSelected = false;
	    selectedMoveToFolderItems = new ArrayList();
	    
		Iterator pvtMsgListIter = pvtMsgList.iterator(); 
		while (pvtMsgListIter.hasNext())
		{
			PrivateMessageDecoratedBean decoMessage = (PrivateMessageDecoratedBean) pvtMsgListIter.next();
			if(decoMessage.getIsSelected())
			{
				msgSelected = true;
		        selectedMoveToFolderItems.add(decoMessage);
			}
		}

		if (!msgSelected)
		{
			setErrorMessage(getResourceBundleString(NO_MARKED_MOVE_MESSAGE));
			return null;
		}
		
	    moveToTopic = selectedTopicId;
	    return MOVE_MESSAGE_PG;
		
	}
	
	private List getPvtMsgListToProcess() {
		List pvtMsgList = new ArrayList();
		boolean searchMode = false;
		// determine if we are looking at search results or the main listing
		if (searchPvtMsgs != null && !searchPvtMsgs.isEmpty())
		{
			searchMode = true;
			pvtMsgList = searchPvtMsgs;
		}
		else
		{
			pvtMsgList = decoratedPvtMsgs;
		}
		
		return pvtMsgList;
	}
	/**
	 * 
	 * @param readStatus
	 * @return
	 */
	private String markCheckedMessages(boolean readStatus)
	{
		List pvtMsgList = getPvtMsgListToProcess();
		boolean msgSelected = false;
		boolean searchMode = false;
		
		Iterator pvtMsgListIter = pvtMsgList.iterator(); 
		while (pvtMsgListIter.hasNext())
		{
			PrivateMessageDecoratedBean decoMessage = (PrivateMessageDecoratedBean) pvtMsgListIter.next();
			if(decoMessage.getIsSelected())
			{
				msgSelected = true;
				if (readStatus && !decoMessage.isHasRead()) {
					prtMsgManager.markMessageAsReadForUser(decoMessage.getMsg());
				} else if(!readStatus && decoMessage.isHasRead()) {
					prtMsgManager.markMessageAsUnreadForUser(decoMessage.getMsg());
				}

				if (searchMode)
				{
					// Although the change was made in the db, the search
					// view needs to be refreshed (it doesn't return to db)
					decoMessage.setHasRead(true);
					decoMessage.setIsSelected(false);
				}
			}      
		}
		
		if (!msgSelected)
		{
			setErrorMessage(getResourceBundleString(NO_MARKED_READ_MESSAGE));
			return null;
		}
		
		if (searchMode)
		{
			return SEARCH_RESULT_MESSAGES_PG;
		}
		
		return DISPLAY_MESSAGES_PG; 
	}
	
	private void setFromMainOrHp()
	{
		String fromPage = getExternalParameterByKey(COMPOSE_FROM_PG);
	    if(fromPage != null && (fromPage.equals(MESSAGE_HOME_PG) || (fromPage.equals(MAIN_PG))))
	    {
	    	fromMainOrHp = fromPage;
	    }
	}
	
	@SuppressWarnings("unchecked")
	public String processActionPermissions()
	{
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		ToolSession toolSession = SessionManager.getCurrentToolSession();

		try {
			String url = "../sakai.permissions.helper.helper/tool?" +
			"session." + PermissionsHelper.DESCRIPTION + "=" +
			org.sakaiproject.util.Web.escapeUrl(getResourceBundleString("pvt_properties_desc")) +
			"&session." + PermissionsHelper.TARGET_REF + "=" +
			SiteService.getSite(ToolManager.getCurrentPlacement().getContext()).getReference() +
			"&session." + PermissionsHelper.PREFIX + "=" +
			DefaultPermissionsManager.MESSAGE_FUNCTION_PREFIX + DefaultPermissionsManager.MESSAGE_FUNCITON_PREFIX_PERMISSIONS;

			// Set permission descriptions
			if (toolSession != null) {
				ResourceLoader pRb = new ResourceLoader(PERMISSIONS_BUNDLE);
				HashMap<String, String> pRbValues = new HashMap<String, String>();
				for (Iterator<Entry<String, String>> iEntries = pRb.entrySet().iterator();iEntries.hasNext();)
				{
					Entry<String, String> entry = iEntries.next();
					String key = entry.getKey();
					pRbValues.put(key, entry.getValue());
				}

				toolSession.setAttribute("permissionDescriptions", pRbValues); 
				
				// set group awareness
				 String groupAware = ToolManager.getCurrentTool().getRegisteredConfig().getProperty("groupAware");
				 toolSession.setAttribute("groupAware", groupAware != null ? Boolean.valueOf(groupAware) : Boolean.FALSE);
			}

			// Invoke Permissions helper
			context.redirect(url);
		}
		catch (IOException e) {
			throw new RuntimeException("Failed to redirect to helper", e);
		}catch (IdUnusedException e){
			throw new RuntimeException("Failed to redirect to helper", e);
		}
		return null;
	}

	/**
	 * @return TRUE if within Messages & Forums tool, FALSE otherwise
	 */
	public boolean isMessagesandForums() {
		return messageManager.currentToolMatch(MESSAGECENTER_TOOL_ID);
	}
	
	/**
	 * @return TRUE if within Messages tool, FALSE otherwise
	 */
	public boolean isMessages() {
		return messageManager.currentToolMatch(MESSAGES_TOOL_ID);
	}
	
	/**
	 * @return TRUE if Message Forums (Message Center) exists in this site,
	 *         FALSE otherwise
	 */
	public boolean isMessageForumsPageInSite() {
		return isMessageForumsPageInSite(getSiteId());
	}

	/**
	 * @return TRUE if Messages & Forums (Message Center) exists in this site,
	 *         FALSE otherwise
	 */
	private boolean isMessageForumsPageInSite(String siteId) {
		return messageManager.isToolInSite(siteId, MESSAGECENTER_TOOL_ID);
	}
	
	/**
	 * @return TRUE if Messages tool exists in this site,
	 *         FALSE otherwise
	 */
	public boolean isMessagesPageInSite() {
		return isMessagesPageInSite(getSiteId());
	}

	/**
	 * @return TRUE if Messages tool exists in this site,
	 *         FALSE otherwise
	 */
	private boolean isMessagesPageInSite(String siteId) {
		return messageManager.isToolInSite(siteId, MESSAGES_TOOL_ID);
	}
	
	private String getEventMessage(Object object) {
	  	String eventMessagePrefix = "";
	  	final String toolId = ToolManager.getCurrentTool().getId();
		  	
		if (toolId.equals(DiscussionForumService.MESSAGE_CENTER_ID))
			eventMessagePrefix = "/messages&Forums/site/";
		else if (toolId.equals(DiscussionForumService.MESSAGES_TOOL_ID))
			eventMessagePrefix = "/messages/site/";
		else
			eventMessagePrefix = "/forums/site/";
	  	
	  	return eventMessagePrefix + ToolManager.getCurrentPlacement().getContext() + 
	  				"/" + object.toString() + "/" + SessionManager.getCurrentSessionUserId();
	}

	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}

	public SynopticMsgcntrManager getSynopticMsgcntrManager() {
		return synopticMsgcntrManager;
	}

	public void setSynopticMsgcntrManager(
			SynopticMsgcntrManager synopticMsgcntrManager) {
		this.synopticMsgcntrManager = synopticMsgcntrManager;
	}
	
	public void setUserPreferencesManager(UserPreferencesManager userPreferencesManager) {
		this.userPreferencesManager = userPreferencesManager;
	}
	
	public String getMobileSession()
	{
		Session session = SessionManager.getCurrentSession();
		String rv = session.getAttribute("is_wireless_device") != null && ((Boolean) session.getAttribute("is_wireless_device")).booleanValue()?"true":"false"; 
		return rv;
	}
	
	public boolean getCurrentSiteHasGroups(){
		Site currentSite = getCurrentSite();
		if(currentSite != null){
			return currentSite.hasGroups();
		}else{
			return false;
		}
	}
	
	public Site getCurrentSite(){
		try{
			return SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
		} catch (IdUnusedException e) {
			LOG.error(e.getMessage());
		}
		return null;
	}
	
	public List<SelectItem> getNonHiddenGroups(){
		nonHiddenGroups = new ArrayList<SelectItem>();
		nonHiddenGroups.add(new SelectItem(DEFAULT_NON_HIDDEN_GROUP_ID, getResourceBundleString(DEFAULT_NON_HIDDEN_GROUP_TITLE)));
		
		Site currentSite = getCurrentSite();   
		if(currentSite.hasGroups()){
	      
			Collection groups = currentSite.getGroups();

			groups = sortGroups(groups);

			for (Iterator groupIterator = groups.iterator(); groupIterator.hasNext();)
			{
				Group currentGroup = (Group) groupIterator.next();
				if(!isGroupHidden(currentGroup.getTitle())){
					nonHiddenGroups.add(new SelectItem(currentGroup.getTitle(), currentGroup.getTitle()));
				}				
			}		
		}
		
		return nonHiddenGroups;		
	}
	
	private boolean isGroupHidden(String groupName){
		for (HiddenGroup hiddenGroup : getHiddenGroups()) {
			if(hiddenGroup.getGroupId().equals(groupName)){
				return true;
			}
		}
		return false;
	}
	
	public List<HiddenGroup> getHiddenGroups(){
		return hiddenGroups;
	}
	
	public void setHiddenGroups(List<HiddenGroup> hiddenGroups){
		this.hiddenGroups = hiddenGroups;
	}
	
	/**
	   * Takes groups defined and sorts them alphabetically by title
	   * so will be in some order when displayed on permission widget.
	   * 
	   * @param groups
	   * 			Collection of groups to be sorted
	   * 
	   * @return
	   * 		Collection of groups in sorted order
	   */
	  private Collection sortGroups(Collection groups) {
		  List sortGroupsList = new ArrayList();

		  sortGroupsList.addAll(groups);
		  
		  final GroupComparator groupComparator = new GroupComparator("title", true);
		  
		  Collections.sort(sortGroupsList, groupComparator);
		  
		  groups.clear();
		  
		  groups.addAll(sortGroupsList);
		  
		  return groups;
	  }
	  
	  public String getSelectedNonHiddenGroup(){
		  return selectedNonHiddenGroup;
	  }
	  
	  public void setSelectedNonHiddenGroup(String selectedNonHiddenGroup){
		  this.selectedNonHiddenGroup = selectedNonHiddenGroup;
	  }
	  
	  public void processActionAddHiddenGroup(ValueChangeEvent event){
		  String selectedGroup = (String) event.getNewValue();
		  if(!DEFAULT_NON_HIDDEN_GROUP_ID.equals(selectedGroup) && !isGroupHidden(selectedGroup)){
			  getHiddenGroups().add(new HiddenGroupImpl(selectedGroup));
			  selectedNonHiddenGroup = DEFAULT_NON_HIDDEN_GROUP_ID;
		  }
	  }
	  
	  public String processActionRemoveHiddenGroup(){
		  String groupId = getExternalParameterByKey(PARAM_GROUP_ID);
		  if(groupId != null && !"".equals(PARAM_GROUP_ID)){
			  for (HiddenGroup hiddenGroup : getHiddenGroups()) {
				  if(hiddenGroup.getGroupId().equals(groupId)){
					  getHiddenGroups().remove(hiddenGroup);
					  break;
				  }
			  }
		  }
		  
		  return null;
	  }
		public Locale getUserLocale(){
			return new ResourceLoader().getLocale();
		}

    private LRS_Statement getStatementForUserReadPvtMsg(LRS_Actor student, String subject) {
        String url = ServerConfigurationService.getPortalUrl();
        LRS_Verb verb = new LRS_Verb(SAKAI_VERB.interacted);
        LRS_Object lrsObject = new LRS_Object(url + "/privateMessage", "read-private-message");
        HashMap<String, String> nameMap = new HashMap<String, String>();
        nameMap.put("en-US", "User read a private message");
        lrsObject.setActivityName(nameMap);
        HashMap<String, String> descMap = new HashMap<String, String>();
        descMap.put("en-US", "User read a private message with subject: " + subject);
        lrsObject.setDescription(descMap);
        return new LRS_Statement(student, verb, lrsObject);
    }

    private LRS_Statement getStatementForUserSentPvtMsg(LRS_Actor student, String subject, SAKAI_VERB sakaiVerb) {
        String url = ServerConfigurationService.getPortalUrl();
        LRS_Verb verb = new LRS_Verb(sakaiVerb);
        LRS_Object lrsObject = new LRS_Object(url + "/privateMessage", "send-private-message");
        HashMap<String, String> nameMap = new HashMap<String, String>();
        nameMap.put("en-US", "User sent a private message");
        lrsObject.setActivityName(nameMap);
        HashMap<String, String> descMap = new HashMap<String, String>();
        descMap.put("en-US", "User sent a private message with subject: " + subject);
        lrsObject.setDescription(descMap);
        return new LRS_Statement(student, verb, lrsObject);
    }
}
