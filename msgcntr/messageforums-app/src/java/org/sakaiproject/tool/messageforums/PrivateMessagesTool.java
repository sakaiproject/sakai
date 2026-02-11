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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.Attachment;
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
import org.sakaiproject.api.app.messageforums.scheduler.PrivateMessageSchedulerService;
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.api.app.messageforums.MembershipItem;
import org.sakaiproject.component.app.messageforums.dao.hibernate.HiddenGroupImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateForumImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateTopicImpl;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.LearningResourceStoreService;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Actor;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Object;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Statement;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Verb;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Verb.SAKAI_VERB;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tags.api.Tag;
import org.sakaiproject.tags.api.TagService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.messageforums.DraftRecipientsDelegate.SelectedLists;
import org.sakaiproject.tool.messageforums.ui.DecoratedAttachment;
import org.sakaiproject.tool.messageforums.ui.PrivateForumDecoratedBean;
import org.sakaiproject.tool.messageforums.ui.PrivateMessageDecoratedBean;
import org.sakaiproject.tool.messageforums.ui.PrivateTopicDecoratedBean;
import org.sakaiproject.tool.messageforums.util.PrivateMessagesToolHelper;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.DateFormatterUtil;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.util.comparator.GroupTitleComparator;
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException;

import javax.faces.FactoryFinder;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import java.text.ParseException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Slf4j
@ManagedBean(name="PrivateMessagesTool")
@SessionScoped
public class PrivateMessagesTool {

  private static final String MESSAGECENTER_PRIVACY_URL = "messagecenter.privacy.url";
  private static final String MESSAGECENTER_PRIVACY_TEXT = "messagecenter.privacy.text";

  private static final String MESSAGECENTER_BUNDLE = "org.sakaiproject.api.app.messagecenter.bundle.Messages";
 
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
  private static final String MISSING_BODY = "pvt_missing_body";
  private static final String MISSING_BODY_DRAFT = "pvt_missing_body_draft";
  private static final String MISSING_TITLE = "pvt_missing_title";
  private static final String MISSING_QUESTION = "pvt_missing_question";
  private static final String SELECT_MSG_RECIPIENT = "pvt_select_msg_recipient";
  private static final String MULTIPLE_WINDOWS = "pvt_multiple_windows";
  
  private static final String CONFIRM_MSG_DELETE = "pvt_confirm_msg_delete";
  private static final String ENTER_SEARCH_TEXT = "pvt_enter_search_text";
  private static final String ENTER_SEARCH_TAGS = "pvt_enter_search_tags";
  private static final String MOVE_MSG_ERROR = "pvt_move_msg_error";
  private static final String NO_MARKED_READ_MESSAGE = "pvt_no_message_mark_read";
  private static final String NO_MARKED_DELETE_MESSAGE = "pvt_no_message_mark_delete";
  private static final String NO_MARKED_MOVE_MESSAGE = "pvt_no_message_mark_move";
  private static final String MULTIDELETE_SUCCESS_MSG = "pvt_deleted_success";
  private static final String PERM_DELETE_SUCCESS_MSG = "pvt_perm_deleted_success";
  private static final String SUCCESS_PUBLISH_TO_FAQ = "pvt_publish_to_faq_success";
  
  public static final String RECIPIENTS_UNDISCLOSED = "pvt_bccUndisclosed";
  
  private static final String DATE_PGR_MSG_ERROR = "pvt_date_pgr_msg_error";

  /** Used to determine if this is combined tool or not */
  private static final String MESSAGECENTER_TOOL_ID = "sakai.messagecenter";
  private static final String MESSAGECENTER_HELPER_TOOL_ID = "sakai.messageforums.helper";
  private static final String MESSAGES_TOOL_ID = "sakai.messages";
  private static final String FORUMS_TOOL_ID = "sakai.forums";

  private static final String HIDDEN_SEARCH_FROM_ISO_DATE = "searchFromDateISO8601";
  private static final String HIDDEN_SEARCH_TO_ISO_DATE = "searchToDateISO8601";
  
  private Boolean fromPermissions = false;
  private Boolean fromPreview = false;

  /**
   *Dependency Injected 
   */
  @Setter
  @ManagedProperty(value="#{Components[\"org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager\"]}")
  private PrivateMessageManager prtMsgManager;
  @Setter
  @ManagedProperty(value="#{Components[\"org.sakaiproject.api.app.messageforums.MessageForumsMessageManager\"]}")
  private MessageForumsMessageManager messageManager;
  @Setter
  @ManagedProperty(value="#{Components[\"org.sakaiproject.api.app.messageforums.MessageForumsForumManager\"]}")
  private MessageForumsForumManager forumManager;
  @Setter
  @ManagedProperty(value="#{Components[\"org.sakaiproject.api.app.messageforums.MembershipManager\"]}")
  private MembershipManager membershipManager;
  @Getter @Setter
  @ManagedProperty(value="#{Components[\"org.sakaiproject.api.app.messageforums.SynopticMsgcntrManager\"]}")
  private SynopticMsgcntrManager synopticMsgcntrManager;
  /** Dependency Injected   */
  @Setter
  @ManagedProperty(value="#{Components[\"org.sakaiproject.api.app.messageforums.MessageForumsTypeManager\"]}")
  private MessageForumsTypeManager typeManager;
  @Setter
  @ManagedProperty(value="#{Components[\"org.sakaiproject.content.api.ContentHostingService\"]}")
  private ContentHostingService contentHostingService;
  @Setter
  @ManagedProperty(value="#{Components[\"org.sakaiproject.event.api.LearningResourceStoreService\"]}")
  private LearningResourceStoreService learningResourceStoreService;
  @Setter
  @ManagedProperty(value="#{Components[\"org.sakaiproject.user.api.UserDirectoryService\"]}")
  private UserDirectoryService userDirectoryService;
  @Setter
  @ManagedProperty(value="#{Components[\"org.sakaiproject.authz.api.SecurityService\"]}")
  private SecurityService securityService;
  @Setter
  @ManagedProperty(value="#{Components[\"org.sakaiproject.event.api.EventTrackingService\"]}")
  private EventTrackingService eventTrackingService;
  @Setter
  @ManagedProperty(value="#{Components[\"org.sakaiproject.site.api.SiteService\"]}")
  private SiteService siteService;
  @Setter
  @ManagedProperty(value="#{Components[\"org.sakaiproject.authz.api.AuthzGroupService\"]}")
  private AuthzGroupService authzGroupService;
  @Setter
  @ManagedProperty(value="#{Components[\"org.sakaiproject.tool.api.SessionManager\"]}")
  private SessionManager sessionManager;
  @Setter
  @ManagedProperty(value="#{Components[\"org.sakaiproject.time.api.UserTimeService\"]}")
  private UserTimeService userTimeService;
  @Setter
  @ManagedProperty(value="#{Components[\"org.sakaiproject.tool.api.ToolManager\"]}")
  private ToolManager toolManager;
  @Setter
  @ManagedProperty(value="#{Components[\"org.sakaiproject.util.api.FormattedText\"]}")
  private FormattedText formattedText;
  @Setter
  @ManagedProperty(value="#{Components[\"org.sakaiproject.tags.api.TagService\"]}")
  private TagService tagService;
  
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
  public static final String PERMISSIONS_PG = "/jsp/privateMsg/permissions";

  public static final String MESSAGE_FORWARD_PG="pvtMsgForward";
  
  //sakai-huxt pvtMsgReplyAll
  public static final String MESSAGE_ReplyAll_PG="pvtMsgReplyAll";
  
  public static final String DELETE_MESSAGE_PG="pvtMsgDelete";
  public static final String REVISE_FOLDER_PG="pvtMsgFolderRevise";
  public static final String MOVE_MESSAGE_PG="pvtMsgMove";
  public static final String PUBLISH_TO_FAQ_EDIT = "pvtMsgPublishToFaqEdit";
  public static final String ADD_FOLDER_IN_FOLDER_PG="pvtMsgFolderInFolderAdd";
  public static final String ADD_MESSAGE_FOLDER_PG="pvtMsgFolderAdd";
  public static final String PVTMSG_COMPOSE = "pvtMsgCompose";
  
  
  //need to modified to support internationalization by huxt
  /** portlet configuration parameter values**/
  public static final String PVTMSG_MODE_RECEIVED = "pvt_received";
  public static final String PVTMSG_MODE_SENT = "pvt_sent";
  public static final String PVTMSG_MODE_DELETE = "pvt_deleted";
  public static final String PVTMSG_MODE_DRAFT = "pvt_drafts";
  public static final String PVTMSG_MODE_SCHEDULER = "pvt_scheduler";
  public static final String PVTMSG_MODE_CASE = "Personal Folders";
  
  public static final String RECIPIANTS_ENTIRE_CLASS= "All Participants";
  public static final String RECIPIANTS_ALL_INSTRUCTORS= "All Instructors";
  
  public static final String SET_AS_YES="yes";
  public static final String SET_AS_NO="no";    
  public static final String SET_AS_DEFAULT="default";

  public static final String THREADED_VIEW = "threaded";
  
  //huxt
  public static final String EXTERNAL_TOPIC_ID = "pvtMsgTopicId";
  public static final String EXTERNAL_WHICH_TOPIC = "selectedTopic";

  @Getter private final String BREADCRUMB_SEPARATOR = " / ";

  PrivateForumDecoratedBean decoratedForum;
  
  private List aggregateList = new ArrayList();
  
  private Area area;
  private PrivateForum forum;
  private List<PrivateTopic> pvtTopics=new ArrayList<>();
  private List decoratedPvtMsgs;

  @Getter
  private String msgNavMode="privateMessages" ;
  @Getter
  private PrivateMessageDecoratedBean detailMsg;
  public void setDetailMsg(PrivateMessageDecoratedBean detailMsg) {
    this.detailMsg = detailMsg;
    if (detailMsg == null || (!fromPreview && !detailMsg.getIsPreview() && !detailMsg.getIsPreviewReply() && !detailMsg.getIsPreviewReplyAll() && !detailMsg.getIsPreviewForward())) {
      this.selectedTags = "";
      fromPreview = false;
    } else if (detailMsg.getIsPreview() || detailMsg.getIsPreviewReply() || detailMsg.getIsPreviewReplyAll() || detailMsg.getIsPreviewForward()) {
      fromPreview = true;
    }
  }
  private boolean viewChanged = false;
  
  @Getter @Setter
  private String currentMsgUuid; //this is the message which is being currently edited/displayed/deleted
  @Getter @Setter
  private List selectedItems;
  
  private String userName;    //current user
  
  //delete confirmation screen - single delete 
  @Getter @Setter
  private boolean deleteConfirm=false ; //used for displaying delete confirmation message in same jsp
  @Getter @Setter
  private boolean validEmail=true ;
  
  //Compose Screen-webpage
  @Getter @Setter
  private List<String> selectedComposeToList = new ArrayList<>();
  @Getter @Setter
  private List<String> selectedComposeBccList = new ArrayList<>();
  @Getter @Setter
  private String composeSendAsPvtMsg=SET_AS_YES; // currently set as Default as change by user is allowed
  @Setter
  private boolean booleanEmailOut = ServerConfigurationService.getBoolean("mc.messages.ccEmailDefault", false);
  @Getter @Setter
  private boolean booleanReadReceipt;
  @Getter
  private String composeSubject;
  @Getter
  private String composeBody;
  @Getter @Setter
  private String selectedLabel="pvt_priority_normal" ;   //defautl set
  @Setter
  private List<MembershipItem> totalComposeToList = null;
  @Setter
  private List<MembershipItem> totalComposeToBccList = null;
  @Getter @Setter
  private List totalComposeToListRecipients;
  
  //Delete items - Checkbox display and selection - Multiple delete
  @Getter @Setter
  private List selectedDeleteItems;
  @Getter @Setter
  private boolean multiDeleteSuccess;
  @Getter @Setter
  private String multiDeleteSuccessMsg;
  @Getter @Setter
  private List totalDisplayItems=new ArrayList();
  
  // Move to folder - Checkbox display and selection - Multiple move to folder
  @Getter @Setter
  private List selectedMoveToFolderItems;
  
  //reply to 
  @Getter
  private String replyToBody;
  @Getter
  private String replyToSubject;

  //forwarding
  @Getter
  private String forwardBody;
  @Getter
  private String forwardSubject;

  @Getter
  private String replyToAllBody;
  @Getter
  private String replyToAllSubject;
  
  //Setting Screen
  @Getter @Setter
  private String activatePvtMsg=SET_AS_NO;
  @Getter @Setter
  private String forwardPvtMsg=SET_AS_DEFAULT;
  @Getter @Setter
  private String forwardPvtMsgEmail;
  private boolean superUser;
  @Getter @Setter
  private String sendToEmail;
  
  //message header screen
  @Getter @Setter
  private String searchText="";
  @Getter @Setter
  private String selectView;
  
  //return to previous page after send msg
  @Getter @Setter
  private String fromMainOrHp = null;
  
  // for compose, are we coming from main page?
  @Getter
  @Setter
  private boolean fromMain;
  
  // Message which will be marked as replied
  @Getter @Setter
  private PrivateMessage replyingMessage;

  @Getter @Setter
  private String selectedTags;
  //=====================need to be modified to support internationalization - by huxt
  /** The configuration mode, received, sent,delete, case etc ... */
  public static final String STATE_PVTMSG_MODE = "pvtmsg.mode";
  
  private Map courseMemberMap;

  public static final String SORT_DATE_DESC = "date_desc";

  /** sort member */
  private String sortType = SORT_DATE_DESC;
  
  private int setDetailMsgCount = 0;

  private List<HiddenGroup> hiddenGroups = new ArrayList<>();
  private static final String DEFAULT_NON_HIDDEN_GROUP_ID = "-1";
  private String DEFAULT_NON_HIDDEN_GROUP_TITLE = "hiddenGroups_selectGroup";
  private String selectedNonHiddenGroup = DEFAULT_NON_HIDDEN_GROUP_ID;
  private static final String PARAM_GROUP_ID = "groupId";
  private Boolean displayHiddenGroupsMsg = null;

  @Getter
  private boolean showProfileInfoMsg = false;

  private final DraftRecipientsDelegate drDelegate;
  
  @Getter @Setter
  public String schedulerSendDateString;

  @Getter
  public Date openDate;
  @Getter @Setter
  private boolean booleanSchedulerSend = false;

  private PrivateMessageSchedulerService PrivateMessageSchedulerService;

  public PrivateMessagesTool()
  {    
	  showProfileInfoMsg = ServerConfigurationService.getBoolean("msgcntr.messages.showProfileInfo", true);
	  drDelegate = new DraftRecipientsDelegate();
	  PrivateMessageSchedulerService = ComponentManager.get(PrivateMessageSchedulerService.class);
  }

  public void initializePrivateMessageArea() {
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

      switch (pf.getAutoForward()) {
	      case PrivateForumImpl.AUTO_FOWARD_YES:
	    	  forwardPvtMsg = SET_AS_YES;
	    	  break;
	      case PrivateForumImpl.AUTO_FOWARD_NO:
	    	  forwardPvtMsg = SET_AS_NO;
	    	  break;
	      default:
	    	  forwardPvtMsg = SET_AS_DEFAULT;
	    	  break;
      }

      forwardPvtMsgEmail = pf.getAutoForwardEmail();
      hiddenGroups = new ArrayList<>();
      if(area != null && area.getHiddenGroups() != null){
          hiddenGroups.addAll(area.getHiddenGroups());
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

  public boolean isBooleanEmailOut() {
      return booleanEmailOut || isEmailCopyAlways();
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
    	  
    	Iterator<PrivateTopic> iterator = pvtTopics.iterator(); 
        
        while(iterator.hasNext())
        {
               PrivateTopic topic = iterator.next();
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
			  log.error(e.getMessage(), e);
		  }

		  numOfAttempts--;

		  if (numOfAttempts <= 0) {
			  log.info("PrivateMessagesTool: setMessagesSynopticInfoHelper: HibernateOptimisticLockingFailureException no more retries left");
			  log.error(holfe.getMessage(), holfe);
		  } else {
			  log.info("PrivateMessagesTool: setMessagesSynopticInfoHelper: HibernateOptimisticLockingFailureException: attempts left: "
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
						forumManager.getTopicByUuid(getExternalParameterByKey(EXTERNAL_TOPIC_ID)).getTitle() ://EXTERNAL_TOPIC_ID
						getExternalParameterByKey(EXTERNAL_WHICH_TOPIC);
	}

    this.viewChanged = false;

  	/** support for sorting */
  	/* The sorting is done in the client side using datatables */
    decoratedPvtMsgs=new ArrayList();
    String typeUuid = getPrivateMessageTypeFromContext(msgNavMode);
    decoratedPvtMsgs = prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_DATE, PrivateMessageManager.SORT_DESC);
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

      for(PrivateMessageDecoratedBean dmb : (List<PrivateMessageDecoratedBean>) decoratedPvtMsgs) {

        //The first element of the list
        if(dmb.equals(decoratedPvtMsgs.get(0))) {
              dmb.setHasPre(false);
              //If the list only contains one element
              dmb.setHasNext((decoratedPvtMsgs.size() == 1) ? false : true);
        //The last element of the list
        } else if(dmb.equals(decoratedPvtMsgs.get(decoratedPvtMsgs.size() - 1))) {
              dmb.setHasPre(true);
              dmb.setHasNext(false);
        } else {
              dmb.setHasNext(true);
              dmb.setHasPre(true);
        }
      }
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
              (isEmailCopyOptional() && this.isBooleanEmailOut())) {
          sendEmail = true;
      } else {
          sendEmail = false;
      }

      return sendEmail;
  }

  private String getSiteTitle(){
	  try {
		return siteService.getSite(toolManager.getCurrentPlacement().getContext()).getTitle();
	} catch (IdUnusedException e) {
		log.error(e.getMessage(), e);
	}
	return "";
  }
  
  public String getSiteId() {
	  return toolManager.getCurrentPlacement().getContext();
  }
  
  public String getTagTool() {
	return TagService.TOOL_PRIVATE_MESSAGES;
  }

  private String getContextSiteId() 
  {
	 return "/site/" + toolManager.getCurrentPlacement().getContext();
  }
  
  public List<SelectItem> getTotalComposeToList()
  { 
      if (totalComposeToList == null) {
          initializeComposeToLists();
      }
      return totalComposeToList.stream().map(item->new SelectItem(item.getId(), item.getName())).collect(Collectors.toList());
  }

  public List getTotalComposeToBccList() {
      if (totalComposeToBccList == null) {
          initializeComposeToLists();
      }
      return totalComposeToBccList.stream().map(item->new SelectItem(item.getId(), item.getName())).collect(Collectors.toList());
  }
  
  /**
   * Since the courseMemberMap generates new uuids each time it is called, and
   * these uuids are used to identify the recipients of the message when the user
   * sends the message, we need to do the logic for the "To" and "Bcc" lists together, 
   * utilizing the same courseMemberMap. This will set the values for the
   * totalComposeToList and totalComposeToBccList.
   */
  private void initializeComposeToLists() {
      totalComposeToList = new ArrayList<>();
      totalComposeToBccList = new ArrayList<>();
      
      List<String> hiddenGroupIds = getHiddenGroupIds(area.getHiddenGroups());
      courseMemberMap = membershipManager.getFilteredCourseMembers(true, getHiddenGroupIds(area.getHiddenGroups()));
      List<MembershipItem> members = membershipManager.convertMemberMapToList(courseMemberMap);

      // we need to filter out the hidden groups since they will only appear as recipients in the bcc list
      for (MembershipItem item : members) {
          if (hiddenGroupIds != null && item.getGroup() != null && hiddenGroupIds.contains(item.getGroup().getTitle())) {
              // hidden groups only appear in the bcc list (to users with permission)
              if (isDisplayHiddenGroupsMsg()) {
                totalComposeToBccList.add(item);
              }
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
    return CollectionUtils.emptyIfNull((Set<HiddenGroup>) hiddenGroups).stream().map(HiddenGroup::getGroupId).collect(Collectors.toList());
  }
  
  /**
   * 
   * @param id
   * @return
   */
  public String getUserSortNameById(String id){    
    try
    {
      User user=userDirectoryService.getUser(id) ;
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
		log.error(e.getMessage(), e);
	}
    return userName;
  }

  public String getUserName() {
   String userId=sessionManager.getCurrentSessionUserId();
   try
   {
     User user=userDirectoryService.getUser(userId) ;
     if (ServerConfigurationService.getBoolean("msg.displayEid", true))
     {
    	 userName= user.getDisplayName() + " (" + user.getDisplayId() + ")";
     }
     else {
    	 userName= user.getDisplayName();
     }   
   }
   catch (UserNotDefinedException e) {
       log.error(e.getMessage(), e);
}
   return userName;
  }
  
  public String getUserId()
  {
    return sessionManager.getCurrentSessionUserId();
  }
  
  public TimeZone getUserTimeZone() {
	  return userTimeService.getLocalTimeZone();
  }

    public String getServerUrl() {
    return ServerConfigurationService.getServerUrl();
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
		  msgsList.addAll(searchPvtMsgs);
		  searchPvtMsgs.clear();
	  }else
	  {
		  // always start with the decorated pm in ascending date order
		  String typeUuid = getPrivateMessageTypeFromContext(msgNavMode);
		  decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_DATE, PrivateMessageManager.SORT_ASC);
		  decoratedPvtMsgs = createDecoratedDisplay(decoratedPvtMsgs);
		  msgsList.addAll(decoratedPvtMsgs);
		  decoratedPvtMsgs.clear();
	  }


	  if(msgsList != null)
	  {
		  List tempMsgsList = new ArrayList();
		  // Using this HashSet to ensure that each message is only processed once
		  // preventing a logic loop that can cause a memory leak. 
		  HashSet messageIds = new HashSet<Long>();
		  for(PrivateMessageDecoratedBean pmdb : (List<PrivateMessageDecoratedBean>) msgsList) {

			  long msgId = pmdb.getMsg().getId();
			  if (!messageIds.contains(msgId)) {
				  messageIds.add(msgId);
				  tempMsgsList.add(pmdb);
			  }
		  }

		  Iterator iter = tempMsgsList.iterator();
		  while(iter.hasNext())
		  {
			  List allRelatedMsgs = messageManager.getAllRelatedMsgs(
					  ((PrivateMessageDecoratedBean)iter.next()).getMsg().getId());
			  List currentRelatedMsgs = new ArrayList();
			  if(allRelatedMsgs != null && !allRelatedMsgs.isEmpty())
			  {
				  Long msgId = ((Message)allRelatedMsgs.get(0)).getId();
				  PrivateMessage pvtMsg= (PrivateMessage) prtMsgManager.getMessageById(msgId);
				  PrivateMessageDecoratedBean pdb = new PrivateMessageDecoratedBean(pvtMsg);
				  pdb.setDepth(-1);
				  boolean firstEleAdded = false;
				  for(PrivateMessageDecoratedBean tempPMDB : (List<PrivateMessageDecoratedBean>) msgsList) {
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

			  for(PrivateMessageDecoratedBean currentRelatedMsg : (List<PrivateMessageDecoratedBean>) currentRelatedMsgs) {

				  if(searcModeOn)
				  {
					  searchPvtMsgs.add(currentRelatedMsg);
				  }else
				  {
					  decoratedPvtMsgs.add(currentRelatedMsg);
				  }

				  tempMsgsList.remove(currentRelatedMsg);
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
    for (Message msg : (List<Message>) allRelatedMsgs) {
      Long msgId = msg.getId();
	  PrivateMessage pvtMsg= (PrivateMessage) prtMsgManager.getMessageById(msgId);
	  PrivateMessageDecoratedBean thisMsgBean = new PrivateMessageDecoratedBean(pvtMsg);

      Message thisMsg = thisMsgBean.getMsg();
      boolean existedInCurrentUserList = false;
      for(PrivateMessageDecoratedBean currentUserBean : (List<PrivateMessageDecoratedBean>) msgsList){

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
  @Getter @Setter
  private String selectedTopicTitle="";
  @Getter @Setter
  private String selectedTopicId="";

  public String processActionHome()
  {
    log.debug("processActionHome()");
    msgNavMode = "privateMessages";
    multiDeleteSuccess = false;
    if (searchPvtMsgs != null)
    	searchPvtMsgs.clear();
    return  MAIN_PG;
  }  
  public String processActionPrivateMessages()
  {
    log.debug("processActionPrivateMessages()");
    msgNavMode = "privateMessages";
    multiDeleteSuccess = false;
    if (searchPvtMsgs != null) 
    	searchPvtMsgs.clear();
    return  MESSAGE_HOME_PG;
  }        
  public String processDisplayForum()
  {
    log.debug("processDisplayForum()");
    multiDeleteSuccess = false;
    if (searchPvtMsgs != null)
    	searchPvtMsgs.clear();
    this.selectedTags = "";
    this.fromPreview = false;
    return DISPLAY_MESSAGES_PG;
  }

  public String processDisplayMessages()
  {
    log.debug("processDisplayMessages()");
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
  
  public String getReceivedTopicForMessage(String msgId) {
	  if (StringUtils.isNotBlank(msgId) && getPvtAreaEnabled()) {
		  for (Topic topic : pvtTopics) {
			  String typeUuid = getPrivateMessageTypeFromContext(topic.getTitle());
			  List<Message> topicMessages = prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_DATE,PrivateMessageManager.SORT_DESC);
			  for (Message dMsg : topicMessages) {
				  if (dMsg.getId().equals(Long.valueOf(msgId))) {
					  return topic.getUuid();
				  }
			  }
		  }
	  }
	  return null;
  }
  
  public String processPvtMsgTopicAndDetail() {
	  try {
		  processPvtMsgTopic();
		  viewChanged = true;
		  decoratedPvtMsgs = getDecoratedPvtMsgs();
		  return processPvtMsgDetail();
	  } catch (Exception ex) {
		  setErrorMessage(getResourceBundleString("error_direct_access"));
		  return null;
	  }
  }
  
  public String processPvtMsgTopic()
  {
    log.debug("processPvtMsgTopic()");
    
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

  public String calculateColumnClass() {
	String columnClasses = "check,attach,reply,specialLink,date,dateScheduler,created,addressee,priority,taglist hidden-xs";
	if (selectedTopic != null && selectedTopic.getTopic() != null) {
		final String topicTitle = selectedTopic.getTopic().getTitle();
		if (PVTMSG_MODE_RECEIVED.equals(topicTitle)) {
			columnClasses = "check,attach,reply,specialLink,date,created,priority,taglist hidden-xs";
		} else if (PVTMSG_MODE_SENT.equals(topicTitle)) {
			columnClasses = "check,attach,reply,specialLink,date,dateScheduler,addressee,priority,taglist hidden-xs";
		} else if (PVTMSG_MODE_DRAFT.equals(topicTitle) || PVTMSG_MODE_DELETE.equals(topicTitle) || PVTMSG_MODE_SCHEDULER.equals(topicTitle)) {
			columnClasses = "check,attach,reply,specialLink,date,dateScheduler,created,priority,taglist hidden-xs";
		}
	}
	return columnClasses;
  }

  /**
   * process Cancel from all JSP's
   * @return - pvtMsg
   */  
  public String processPvtMsgCancel() {
    log.debug("processPvtMsgCancel()");
    
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

	  // get the draft recipients and populate the selected lists
	  if (totalComposeToList == null || totalComposeToBccList == null) {
		  initializeComposeToLists();
	  }
	  SelectedLists selectedLists = drDelegate.populateDraftRecipients(draft.getId(), messageManager, totalComposeToList, totalComposeToBccList);
	  selectedComposeToList = selectedLists.to;
	  selectedComposeBccList = selectedLists.bcc;

	  if (draft.getExternalEmail() != null) {
		  setBooleanEmailOut(draft.getExternalEmail());
	  }

	  if(draft.getScheduler() != null && draft.getScheduler() && draft.getScheduledDate() != null) {
		  setSchedulerSendDateString(draft.getScheduledDate().toString());
		  setBooleanSchedulerSend(draft.getScheduler());
	  }

	  //go to compose page
	  setFromMainOrHp();
	  fromMain = (StringUtils.isEmpty(msgNavMode)) || ("privateMessages".equals(msgNavMode));
	  log.debug("processPvtMsgDraft()");
	  return PVTMSG_COMPOSE;
  }

  private void setComposeLists(PrivateMessage currentMessage){
	  //by default add user who sent original message
	  for (MembershipItem membershipItem : totalComposeToList) {
		  if (membershipItem.getUser() != null && membershipItem.getUser().getId().equals(currentMessage.getCreatedBy())) {
			  selectedComposeToList.add(membershipItem.getId());
		  }
	  }
	  String recipientsAsTextBcc = currentMessage.getRecipientsAsTextBcc() == null ? "" : currentMessage.getRecipientsAsTextBcc();

	  String[] splitRecipents = currentMessage.getRecipientsAsText().split(";");
	  String[] splitRecipentsBcc = recipientsAsTextBcc.split(";");
	  for (MembershipItem membershipItem : totalComposeToList) {
		  for (String recipient: splitRecipents) {
			  if (membershipItem.getName().equals(recipient) && !selectedComposeToList.stream().anyMatch(s -> s.equals(recipient))) {
				  selectedComposeToList.add(membershipItem.getId());
			  }
		  }
		  for (String recipientBcc: splitRecipentsBcc) {
			  if (membershipItem.getName().equals(recipientBcc) && !selectedComposeBccList.stream().anyMatch(s -> s.equals(recipientBcc))) {
				  selectedComposeBccList.add(membershipItem.getId());
			  }
		  }
	  }
  }

  /**
   * called when subject of List of messages to Topic clicked for detail
   * @return - pvtMsgDetail
   */ 
  public String processPvtMsgDetail() {
    log.debug("processPvtMsgDetail()");
    multiDeleteSuccess = false;

    String msgId=getExternalParameterByKey("current_msg_detail");
    setCurrentMsgUuid(msgId) ; 
    //retrive the detail for this message with currentMessageId    
    for (PrivateMessageDecoratedBean dMsg : (List<PrivateMessageDecoratedBean>) decoratedPvtMsgs){

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
        for (PrivateMessageRecipient element : (List<PrivateMessageRecipient>) recLs) {
          if (element != null && (element.getRead().booleanValue()) || (element.getUserId().equals(getUserId()))) {
              getDetailMsg().setHasRead(true);
          }
        }
        if(dMsg.getMsg().getCreatedBy().equals(getUserId())){
        	//need to display all users who received the message if the user create the message
        	this.getDetailMsg().getMsg().setRecipientsAsTextBcc(dMsg.getMsg().getRecipientsAsTextBcc());
        }else{
        	//otherwise, hide the BCC information
        	this.getDetailMsg().getMsg().setRecipientsAsTextBcc("");
        }

        this.getDetailMsg().getMsg().setRecipientsAsText(PrivateMessagesToolHelper.removeRecipientUndisclosed(
                dMsg.getMsg().getRecipientsAsText(),
                getResourceBundleString(RECIPIENTS_UNDISCLOSED))
        );

      }
    }
    this.deleteConfirm=false; //reset this as used for multiple action in same JSP
   
    //prev/next message 
    if(decoratedPvtMsgs != null)
    {
      for(PrivateMessageDecoratedBean thisDmb : (List<PrivateMessageDecoratedBean>) decoratedPvtMsgs){

        if(msgId.equals(thisDmb.getMsg().getId().toString())) {
          detailMsg.setDepth(thisDmb.getDepth());
          detailMsg.setHasNext(thisDmb.getHasNext());
          detailMsg.setHasPre(thisDmb.getHasPre());
          break;
        }
      }
    }
    //default setting for moveTo
    moveToTopic=selectedTopicId;
    LRS_Statement statement = null;
    if (null != learningResourceStoreService) {
    	try{
    		statement = getStatementForUserReadPvtMsg(getDetailMsg().getMsg().getTitle());
    	}catch(Exception e){
    		log.error(e.getMessage(), e);
    	}
    }
	Event event = eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_READ, getEventMessage(getDetailMsg().getMsg()), null, true, NotificationService.NOTI_OPTIONAL, statement);
    eventTrackingService.post(event);
    return SELECTED_MESSAGE_PG;
  }

  /**
   * navigate to "reply" a private message
   * @return - pvtMsgReply
   */ 
  public String processPvtMsgReply() {
    log.debug("processPvtMsgReply()");
    
    multiDeleteSuccess = false;
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
	formatter_date.setTimeZone(userTimeService.getLocalTimeZone());
	String formattedCreateDate = formatter_date.format(pm.getCreated());
	
	SimpleDateFormat formatter_date_time = new SimpleDateFormat(getResourceBundleString("date_format_time"), new ResourceLoader().getLocale());
	formatter_date_time.setTimeZone(userTimeService.getLocalTimeZone());
	String formattedCreateTime = formatter_date_time.format(pm.getCreated());

	StringBuilder replyText = new StringBuilder();
    
    // populate replyToBody with the reply text
	replyText.append("<p></p><p></p>");
	replyText.append("<span style=\"font-weight:bold;font-style:italic;\">");
	replyText.append(getResourceBundleString("pvt_msg_on"));
	replyText.append(" " + formattedCreateDate + " ");
	replyText.append(getResourceBundleString("pvt_msg_at"));
	replyText.append(" " +formattedCreateTime);
	replyText.append(getResourceBundleString("pvt_msg_comma"));
	replyText.append(" " + formattedText.escapeHtml(pm.getAuthor(), false) + " ");
	replyText.append(getResourceBundleString("pvt_msg_wrote")); 
	replyText.append("</span>");
    	
    String origBody = pm.getBody();
    if (origBody != null && origBody.trim().length() > 0) {
    	replyText.append("<p></p>");
    	replyText.append(pm.getBody());
    }
    
    List attachList = getDetailMsg().getAttachList();
    if (attachList != null && !attachList.isEmpty()) {
    	for (DecoratedAttachment decoAttach : (List<DecoratedAttachment>) attachList) {
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
	    log.debug("processPvtMsgForward()");

	    setDetailMsgCount = 0;
	    multiDeleteSuccess = false;
	    
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
		formatter.setTimeZone(userTimeService.getLocalTimeZone());
		String formattedCreateDate = formatter.format(pm.getCreated());
		
		StringBuilder forwardedText = new StringBuilder();
	    
	    // populate replyToBody with the forwarded text
		forwardedText.append(getResourceBundleString("pvt_msg_fwd_heading") + "<br /><br />");
		forwardedText.append("<strong>");
		forwardedText.append(getResourceBundleString("pvt_msg_fwd_authby"));
		forwardedText.append("</strong>");
		forwardedText.append(" " + formattedText.escapeHtml(pm.getAuthor(), false));
		forwardedText.append(" (" + formattedCreateDate + ")<br />");
		forwardedText.append("<strong>");
		forwardedText.append(getResourceBundleString("pvt_msg_fwd_to"));
		forwardedText.append("</strong>");
		forwardedText.append(" " + pm.getRecipientsAsText() + "<br />");
		forwardedText.append("<strong>");
		forwardedText.append(getResourceBundleString("pvt_msg_fwd_subject"));
		forwardedText.append("</strong>");
		forwardedText.append(" " + pm.getTitle() + "<br />");
		forwardedText.append("<strong>");
		forwardedText.append(getResourceBundleString("pvt_msg_fwd_label"));
		forwardedText.append("</strong>");
		forwardedText.append(" " + getDetailMsg().getLabel());
	    
	    List attachList = getDetailMsg().getAttachList();
	    if (CollectionUtils.isNotEmpty(attachList)) {
	    	forwardedText.append("<div>");
	    	forwardedText.append("<strong>");
	    	forwardedText.append(getResourceBundleString("pvt_msg_fwd_attachments"));
	    	forwardedText.append("</strong>");
	    	forwardedText.append("<ul style=\"list-style-type:none;margin-bottom:1.0em;margin-left:0;margin-right:0;margin-top:0;padding-left:0.5em;padding:0\">");
	    	for (DecoratedAttachment decoAttach : (List<DecoratedAttachment>) attachList ) {
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
	    	forwardedText.append("</div>");
	    }
	    String origBody = pm.getBody();
	    if (origBody != null && origBody.trim().length() > 0) {
	    	forwardedText.append("<p></p>");
	    	forwardedText.append(pm.getBody());
	    }
	    
	    this.setForwardBody(forwardedText.toString());
	    //from message detail screen
	    this.setDetailMsg(getDetailMsg()) ;
	    
	    setDetailMsgCount++;

	    return MESSAGE_FORWARD_PG;
	  }

  //function: add Reply All Tools

  /**
   * navigate to "Reply to all" a private message
   * @return - pvtMsgForward
   */ 
  public String processPvtMsgReplyAll() {
	    log.debug("processPvtMsgReplyAll()");

	    multiDeleteSuccess = false;
	    setDetailMsgCount = 0;
	    
	    if (getDetailMsg() == null)
	    	return null;
	    
	    PrivateMessage pm = getDetailMsg().getMsg();
	    
	    // To mark as replied when user send the reply
	    this.setReplyingMessage(pm);
	    
	    String title = pm.getTitle();
    	if(title != null && !title.startsWith(getResourceBundleString(ReplyAll_SUBJECT_PREFIX))) {
    		replyToAllSubject = getResourceBundleString(ReplyAll_SUBJECT_PREFIX) + ' ' + title;
    	}
    	else {
    		replyToAllSubject = title;
    	}


    	// format the created date according to the setting in the bundle
	    SimpleDateFormat formatter = new SimpleDateFormat(getResourceBundleString("date_format_date"), new ResourceLoader().getLocale());
		formatter.setTimeZone(userTimeService.getLocalTimeZone());
		String formattedCreateDate = formatter.format(pm.getCreated());
		
		SimpleDateFormat formatter_date_time = new SimpleDateFormat(getResourceBundleString("date_format_time"), new ResourceLoader().getLocale());
		formatter_date_time.setTimeZone(userTimeService.getLocalTimeZone());
		String formattedCreateTime = formatter_date_time.format(pm.getCreated());

		StringBuilder replyallText = new StringBuilder();
		
	    
	    // populate replyToBody with the reply text
		replyallText.append("<p></p><p></p>");
		replyallText.append("<span style=\"font-weight:bold;font-style:italic;\">");
		replyallText.append(getResourceBundleString("pvt_msg_on"));
		replyallText.append(" " + formattedCreateDate + " ");
		replyallText.append(getResourceBundleString("pvt_msg_at"));
		replyallText.append(" " +formattedCreateTime);
		replyallText.append(getResourceBundleString("pvt_msg_comma"));
		replyallText.append(" " + formattedText.escapeHtml(pm.getAuthor(), false) + " ");
		replyallText.append(getResourceBundleString("pvt_msg_wrote")); 
		replyallText.append("</span>");
	    	
	    String origBody = pm.getBody();
	    if (origBody != null && origBody.trim().length() > 0) {
	    	replyallText.append("<p></p>");
	    	replyallText.append(pm.getBody());
	    }
	    
	    List attachList = getDetailMsg().getAttachList();
	    if (CollectionUtils.isNotEmpty(attachList)) {
	    	for (DecoratedAttachment decoAttach : (List<DecoratedAttachment>) attachList ) {
	    		if (decoAttach != null) {
	    			replyallText.append("<span style=\"font-style:italic;\">");
	    			replyallText.append(getResourceBundleString("pvt_msg_["));
	    			replyallText.append(decoAttach.getAttachment().getAttachmentName() );
	    			replyallText.append(getResourceBundleString("pvt_msg_]") + "  ");
	    			replyallText.append("</span>");
	    		}
	    	}
	    }
	    
	    setReplyToAllBody(replyallText.toString());
	   	    
	    String msgautherString=getDetailMsg().getAuthor();
	    String msgCClistString=getDetailMsg().getRecipientsAsText();
	    
	    //remove the auther in Cc string
	    if(msgCClistString.length()>=msgautherString.length())
	    {
	    String msgCClistStringwithoutAuthor = msgCClistString;
	    
	    String currentUserasAuther = getUserName();
	    int n = StringUtils.countMatches(msgCClistStringwithoutAuthor, ";");
	    
	    int numberofAuther=0;
	    
	    if(n==0)
	    {numberofAuther=1;}
	    else if(n>=1)
	    { numberofAuther=n+1;}//add the end ";"
	    String[] ccSS = new String[numberofAuther];
	    ccSS=msgCClistStringwithoutAuthor.split(";");
	  
	    StringBuilder tmpCC = new StringBuilder("");
	    
			if((numberofAuther>0)&&(numberofAuther<=msgCClistStringwithoutAuthor.length()))
					      {
					    
						    for(int indexCC =0;indexCC<numberofAuther;indexCC++)	    //last for ";"	
						    {
						    	
						    	
						    	if(!currentUserasAuther.replace(" ", ", ").equals(msgautherString)){
						    		
							    	if(!ccSS[indexCC].trim().equals(currentUserasAuther.replace(" ", ", "))&&(!ccSS[indexCC].trim().equals(msgautherString)))//not equal current auther and not equal old auther
							    	{						    		
							    		tmpCC.append(ccSS[indexCC].trim()).append("; ");
							    		
							    	}
						    	}
						    	
						    	else if(currentUserasAuther.replace(" ", ", ").equals(msgautherString)){
						    		
						    		if(!ccSS[indexCC].trim().equals(currentUserasAuther.replace(" ", ", "))||(!ccSS[indexCC].trim().equals(msgautherString)))//not equal current auther and not equal old auther
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
    log.debug("processPvtMsgDeleteConfirm()");
    
    this.setMultiDeleteSuccess(false);
    this.setDeleteConfirm(true);
    setErrorMessage(getResourceBundleString(CONFIRM_MSG_DELETE));
    /*
     * same action is used for delete..however if user presses some other action after first
     * delete then 'deleteConfirm' boolean is reset
     */
    return SELECTED_MESSAGE_PG ;
  }

  public String processPvtMsgSaveTags() {
    log.debug("processPvtMsgDeleteConfirm() " + currentMsgUuid);
    
    manageTagAssociation(Long.valueOf(currentMsgUuid));
    return SELECTED_MESSAGE_PG;
  }

  /**
   * called from Single delete Page -
   * called when 'delete' button pressed second time
   * @return - pvtMsg
   */ 
  public String processPvtMsgDeleteConfirmYes() {
    log.debug("processPvtMsgDeleteConfirmYes()");
    if(getDetailMsg() != null)
    {      
      prtMsgManager.deletePrivateMessage(getDetailMsg().getMsg(), getPrivateMessageTypeFromContext(msgNavMode));
      PrivateMessageSchedulerService.removeScheduledReminder(getDetailMsg().getMsg().getId());
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
    fromMain = (StringUtils.isEmpty(msgNavMode)) || ("privateMessages".equals(msgNavMode));
    log.debug("processPvtMsgCompose()");
    return PVTMSG_COMPOSE;
  }

  public String processPvtMsgComposeCancel()
  {
    log.debug("processPvtMsgComposeCancel()");
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
    this.setBooleanSchedulerSend(false);
    this.setOpenDate("");
    this.setSchedulerSendDateString("");
    this.setBooleanReadReceipt(false);
    this.setSelectedTags("");
    this.fromPreview = false;
  }
  
  public String processPvtMsgPreview(){
	  
	  if(StringUtils.isEmpty(getComposeSubject()))
	  {
		  setErrorMessage(getResourceBundleString(MISSING_SUBJECT));
		  return null;
	  }
	  if(StringUtils.isEmpty(getComposeBody()))
	  {
		  setErrorMessage(getResourceBundleString(MISSING_BODY));
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
	  this.schedulerSendDateString = (String)FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("openDateISO8601");
	  
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
          
    log.debug("processPvtMsgSend()");
    storeDateISO();

    boolean isSendEmail = isSendEmail();

    if(StringUtils.isEmpty(getComposeSubject()))
    {
      setErrorMessage(getResourceBundleString(MISSING_SUBJECT));
      return null;
    }
    if(StringUtils.isEmpty(getComposeBody()))
    {
      setErrorMessage(getResourceBundleString(MISSING_BODY));
      return null;
    }
    if(getSelectedComposeToList().size()<1)
    {
      setErrorMessage(getResourceBundleString(SELECT_MSG_RECIPIENT));
      return null ;
    }
    if(booleanSchedulerSend) {
	    setOpenDate(schedulerSendDateString);
	    if(openDate == null || !openDate.after(Date.from(Instant.now()))) {
		    setErrorMessage(getResourceBundleString(DATE_PGR_MSG_ERROR));
	        return null;
	    }
    }
    
    PrivateMessage pMsg = null;
    if(getDetailMsg() != null && getDetailMsg().getMsg() != null && getDetailMsg().getMsg().getDraft()){
    	pMsg = constructMessage(true, getDetailMsg().getMsg());
    }else{
    	pMsg= constructMessage(true, null) ;
    }
    
    pMsg.setExternalEmail(booleanEmailOut);
    pMsg.setScheduler(booleanSchedulerSend);
    Map<User, Boolean> recipients = getRecipients();
    
    if(booleanSchedulerSend) {
	    pMsg.setScheduledDate(openDate);
	    schedulerMessage(pMsg, isSendEmail);
	    return processPvtMsgComposeCancel();
    } else {
	    PrivateMessageSchedulerService.removeScheduledReminder(pMsg.getId());
	    Long msgId = prtMsgManager.sendPrivateMessage(pMsg, recipients, isSendEmail, booleanReadReceipt);
	    manageTagAssociation(msgId);
    }
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
    
    LRS_Statement statement = null;
    if (null != learningResourceStoreService) {
    	try{
    		statement = getStatementForUserSentPvtMsg(pMsg.getTitle(), SAKAI_VERB.shared);
    	}catch(Exception e){
    		log.error(e.getMessage(), e);
    	}
    }

    String eventMessage = getEventMessage(pMsg) + "/sendEmail=" + String.valueOf(isSendEmail);
    Event event = eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_ADD, eventMessage, null, true, NotificationService.NOTI_OPTIONAL, statement);
    eventTrackingService.post(event);
    
    if(StringUtils.isNotEmpty(fromMainOrHp)) {
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
		  if(updateCurrentUser || (!currentUser.equals(user.getId())))
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
				log.error(e.getMessage(), e);
			}

			numOfAttempts--;

			if (numOfAttempts <= 0) {
				log.info("PrivateMessagesTool: incrementMessagesSynopticToolInfo: HibernateOptimisticLockingFailureException no more retries left");
				log.error(holfe.getMessage(), holfe);
			} else {
				log.info("PrivateMessagesTool: incrementMessagesSynopticToolInfo: HibernateOptimisticLockingFailureException: attempts left: "
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
    log.debug("processPvtMsgSaveDraft()");
    if(StringUtils.isEmpty(getComposeSubject()))
    {
      setErrorMessage(getResourceBundleString(MISSING_SUBJECT_DRAFT));
      return null;
    }
    if(StringUtils.isEmpty(getComposeBody()))
    {
      setErrorMessage(getResourceBundleString(MISSING_BODY_DRAFT));
      return null;
    }
    if(booleanSchedulerSend) {
		setOpenDate(schedulerSendDateString);
		if(booleanSchedulerSend && (openDate == null || !openDate.after(Date.from(Instant.now())))) {
			setErrorMessage(getResourceBundleString(DATE_PGR_MSG_ERROR));
		    return null;
		}
    }

    PrivateMessage dMsg = null;
    if(getDetailMsg() != null && getDetailMsg().getMsg() != null && getDetailMsg().getMsg().getDraft()){
	    dMsg = constructMessage(true, getDetailMsg().getMsg());
	    PrivateMessageSchedulerService.removeScheduledReminder(dMsg.getId());
    }else{
	    dMsg = constructMessage(true, null);
    }
    dMsg.setDraft(Boolean.TRUE);
    dMsg.setDeleted(Boolean.FALSE);
    dMsg.setExternalEmail(booleanEmailOut);
    dMsg.setScheduler(booleanSchedulerSend);
    dMsg.setScheduledDate(booleanSchedulerSend ? openDate : null);

    List<MembershipItem> draftRecipients = drDelegate.getDraftRecipients(getSelectedComposeToList(), courseMemberMap);
    List<MembershipItem> draftBccRecipients = drDelegate.getDraftRecipients(getSelectedComposeBccList(), courseMemberMap);

    Long msgId = prtMsgManager.sendPrivateMessage(dMsg, getRecipients(), isSendEmail(), draftRecipients, draftBccRecipients, booleanReadReceipt);
    manageTagAssociation(msgId);

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
      aMsg.setTitle(getComposeSubject());
      aMsg.setBody(formattedText.processFormattedText(getComposeBody(), null, null));
      
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
      StringBuilder sendToString = new StringBuilder("");
      StringBuilder sendToHiddenString = new StringBuilder("");
      
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
      if(CollectionUtils.isNotEmpty(selectedComposeBccList) && !sendToString.toString().contains(getResourceBundleString(RECIPIENTS_UNDISCLOSED))){
    	  sendToString.append(getResourceBundleString(RECIPIENTS_UNDISCLOSED)).append("; ");
      }

      //create bcc string to use to display the user's who got BCC'ed
      StringBuilder sendToBccString = new StringBuilder("");
      StringBuilder sendToBccHiddenString = new StringBuilder("");
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

      if (StringUtils.isNotEmpty(sendToString.toString())) {
    	  sendToString.delete(sendToString.length()-2, sendToString.length()); //remove last comma and space
      }
      
      if (StringUtils.isEmpty(sendToHiddenString.toString())) {
    	  aMsg.setRecipientsAsText(sendToString.toString());
      }
      else {
    	  sendToHiddenString.delete(sendToHiddenString.length()-2, sendToHiddenString.length()); //remove last comma and space
    	  aMsg.setRecipientsAsText(sendToString.toString() + " " + PrivateMessage.HIDDEN_RECIPIENTS_START + sendToHiddenString.toString() + PrivateMessage.HIDDEN_RECIPIENTS_END);
      }
      //clean up sendToBccString
      if (StringUtils.isNotEmpty(sendToBccString.toString())) {
    	  sendToBccString.delete(sendToBccString.length()-2, sendToBccString.length()); //remove last comma and space
      }

      if (StringUtils.isEmpty(sendToBccHiddenString.toString())) {
    	  aMsg.setRecipientsAsTextBcc(sendToBccString.toString());
      }
      else {
    	  sendToBccHiddenString.delete(sendToBccHiddenString.length()-2, sendToBccHiddenString.length()); //remove last comma and space
    	  aMsg.setRecipientsAsTextBcc(sendToBccString.toString() + " " + PrivateMessage.HIDDEN_RECIPIENTS_START + sendToBccHiddenString.toString() + PrivateMessage.HIDDEN_RECIPIENTS_END);
      }

    }

    // Remove attachments from the message that are not in the bean
    for (Attachment attachment : (List<Attachment>) aMsg.getAttachments()) {
      if (attachments.stream().map(DecoratedAttachment::getAttachment).noneMatch(a -> a.getAttachmentId().equals(attachment.getAttachmentId()))) {
        prtMsgManager.removePvtMsgAttachment(attachment);
      }
    }

    // Add attachments to the message that are in the bean
    for (DecoratedAttachment decoratedAttachment : attachments) {
      prtMsgManager.addAttachToPvtMsg(aMsg, decoratedAttachment.getAttachment());
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
   * processDisplayPreviousMsg()
   * Display the previous message from the list of decorated messages
   */
  public String processDisplayPreviousMsg()
  {
  multiDeleteSuccess = false;

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
      for (PrivateMessageRecipient element : (List<PrivateMessageRecipient>) recLs) {
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
    LRS_Statement statement = null;
    if (null != learningResourceStoreService) {
    	try{
    		statement = getStatementForUserReadPvtMsg(getDetailMsg().getMsg().getTitle());
    	}catch(Exception e){
    		log.error(e.getMessage(), e);
    	}
    }
    
	Event event = eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_READ, getEventMessage(getDetailMsg().getMsg()), null, true, NotificationService.NOTI_OPTIONAL, statement);
    eventTrackingService.post(event);
    return null;
  }

  /**
   * processDisplayNextMsg()
   * Display the Next message from the list of decorated messages
   */    
  public String processDisplayNextMsg() 
  {
	multiDeleteSuccess = false;
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
      for (PrivateMessageRecipient element : (List<PrivateMessageRecipient>) recLs) {
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
    
    LRS_Statement statement = null;
    if (null != learningResourceStoreService) {
    	try{
    		statement = getStatementForUserReadPvtMsg(getDetailMsg().getMsg().getTitle());
    	}catch(Exception e){
    		log.error(e.getMessage(), e);
    	}
    }
	Event event = eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_READ, getEventMessage(getDetailMsg().getMsg()), null, true, NotificationService.NOTI_OPTIONAL, statement);
    eventTrackingService.post(event);
    return null;
  }

  /////////////////////////////////////     DISPLAY NEXT/PREVIOUS TOPIC     //////////////////////////////////  
  @Getter @Setter
  private PrivateTopicDecoratedBean selectedTopic;

  /**
   * Add prev and next topic UUID value and booleans for display of links
   * 
   */
  public void setPrevNextTopicDetails(String msgNavMode)
  {
    for (int i = 0; i < pvtTopics.size(); i++)
    {
      Topic el = pvtTopics.get(i);
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
            Topic nt=pvtTopics.get(i+1);
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
          
          Topic pt=pvtTopics.get(i-1);
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
          
          Topic nt=pvtTopics.get(i+1);
          if (nt != null)
          {
            //getSelectedTopic().setNextTopicId(nt.getUuid());
            getSelectedTopic().setNextTopicTitle(nt.getTitle());
          }
          Topic pt=pvtTopics.get(i-1);
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
    if(StringUtils.isNotEmpty(prevTopicTitle))
    {
      msgNavMode=prevTopicTitle;
      
      decoratedPvtMsgs=new ArrayList() ;
      
      String typeUuid = getPrivateMessageTypeFromContext(msgNavMode);        
      
      decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_DATE, PrivateMessageManager.SORT_DESC);
      
      decoratedPvtMsgs = createDecoratedDisplay(decoratedPvtMsgs);
      
      if(selectView!=null && selectView.equalsIgnoreCase(THREADED_VIEW))
      {
      	this.rearrageTopicMsgsThreaded(false);
      }

      //set prev/next Topic
      setPrevNextTopicDetails(msgNavMode);
      //set prev/next message
      setMessageBeanPreNextStatus();
      
      if (searchPvtMsgs != null)
      {
          return processClearSearch();
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
    if(StringUtils.isNotEmpty(nextTitle))
    {
      msgNavMode=nextTitle;
      decoratedPvtMsgs=new ArrayList() ;
      
      String typeUuid = getPrivateMessageTypeFromContext(msgNavMode);        
      
      decoratedPvtMsgs= prtMsgManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_DATE, PrivateMessageManager.SORT_DESC);
      
      decoratedPvtMsgs = createDecoratedDisplay(decoratedPvtMsgs);
      
      if(selectView!=null && selectView.equalsIgnoreCase(THREADED_VIEW))
      {
      	this.rearrageTopicMsgsThreaded(false);
      }

      //set prev/next Topic
      setPrevNextTopicDetails(msgNavMode);
      //set prev/next message
      setMessageBeanPreNextStatus();
      
      if (searchPvtMsgs != null)
      {
          return processClearSearch();
      }
    }

    return null;
  }
/////////////////////////////////////     DISPLAY NEXT/PREVIOUS TOPIC     //////////////////////////////////
  /**
   * @param externalTopicId
   * @return
   */
  private String processDisplayMsgById(String externalMsgId)
  {
    log.debug("processDisplayMsgById()");
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
      log.debug("processDisplayMsgById() - Error");
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
		  this.schedulerSendDateString = (String)FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("openDateISO8601");

		  return SELECTED_MESSAGE_PG;
	  }
  }
  public String processPvtMsgPreviewReplyBack(){
	  for (MembershipItem membershipItem : totalComposeToList) {
	    if (membershipItem.getUser() != null && membershipItem.getUser().getId().equals(getDetailMsg().getPreviewReplyTmpMsg().getMsg().getCreatedBy())) {
	      selectedComposeToList.remove(membershipItem.getId());
	    }
	  }
	  this.setDetailMsg(getDetailMsg().getPreviewReplyTmpMsg());
	  return MESSAGE_REPLY_PG;
  }
  
  public String processPvtMsgPreviewReplySend(){
	  storeDateISO();
	  return processPvtMsgReplySentAction(getDetailMsg().getMsg());
  }
  
 public String processPvtMsgReplySend() {
    log.debug("processPvtMsgReplySend()");
    
    return processPvtMsgReplySentAction(getPvtMsgReplyMessage(getDetailMsg().getMsg(), false));
 }
 
 private String processPvtMsgReplySentAction(PrivateMessage rrepMsg){
    if(rrepMsg == null){
    	return null;
    }else{
	    if(booleanSchedulerSend) {
		    setOpenDate(schedulerSendDateString);
		    if(booleanSchedulerSend && (openDate == null || !openDate.after(Date.from(Instant.now())))) {
			    setErrorMessage(getResourceBundleString(DATE_PGR_MSG_ERROR));
			    return null;
		    }
	    }
	    rrepMsg.setExternalEmail(booleanEmailOut);
	    rrepMsg.setScheduler(booleanSchedulerSend);

	    Map<User, Boolean> recipients = getRecipients();

    	Long msgId;
	    if(booleanSchedulerSend && !rrepMsg.getDraft()) {
		    rrepMsg.setScheduledDate(openDate);
		    schedulerMessage(rrepMsg, isSendEmail());
		    resetComposeContents();
		    return DISPLAY_MESSAGES_PG;
	    }else if(rrepMsg.getDraft()) {
		    List<MembershipItem> draftRecipients = drDelegate.getDraftRecipients(getSelectedComposeToList(), courseMemberMap);
		    List<MembershipItem> draftBccRecipients = drDelegate.getDraftRecipients(getSelectedComposeBccList(), courseMemberMap);

		    msgId = prtMsgManager.sendPrivateMessage(rrepMsg, getRecipients(), isSendEmail(), draftRecipients, draftBccRecipients, booleanReadReceipt);
        } else {
            msgId = prtMsgManager.sendPrivateMessage(rrepMsg, recipients, isSendEmail(), booleanReadReceipt);
        }

    	manageTagAssociation(msgId);

    	if(!rrepMsg.getDraft()){
    		prtMsgManager.markMessageAsRepliedForUser(getReplyingMessage());
    		incrementSynopticToolInfo(recipients.keySet(), false);
    		
    		LRS_Statement statement = null;
    	    if (null != learningResourceStoreService) {
    	    	try{
    	    		statement = getStatementForUserSentPvtMsg(getDetailMsg().getMsg().getTitle(), SAKAI_VERB.responded);
    	    	}catch(Exception e){
    	    		log.error(e.getMessage(), e);
    	    	}
    	    }
    	    
    		Event event = eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_RESPONSE, getEventMessage(getDetailMsg().getMsg()), null, true, NotificationService.NOTI_OPTIONAL, statement);
    	    eventTrackingService.post(event);
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
    	storeDateISO();
    	//PrivateMessage currentMessage = getDetailMsg().getMsg() ;
    	//by default add user who sent original message    
    	for (MembershipItem membershipItem : totalComposeToList) {
    		if (membershipItem.getUser() != null && membershipItem.getUser().getId().equals(currentMessage.getCreatedBy())) {
    			selectedComposeToList.add(membershipItem.getId());
    		}
    	}
    	
    	if(StringUtils.isEmpty(getReplyToSubject()))
    	{
    		if(isDraft){
    			setErrorMessage(getResourceBundleString(MISSING_SUBJECT_DRAFT));
    		}else{
    			setErrorMessage(getResourceBundleString(MISSING_SUBJECT));
    		}
    		return null ;
    	}
    	if(StringUtils.isEmpty(getReplyToBody())) {
    		if(isDraft) {
    			setErrorMessage(getResourceBundleString(MISSING_BODY_DRAFT));
    		} else {
    			setErrorMessage(getResourceBundleString(MISSING_BODY));
    		}
    		return null ;
    	}
    	if(!isDraft){
    		if(selectedComposeToList.isEmpty() && selectedComposeBccList.isEmpty())
    		{
    			setErrorMessage(getResourceBundleString(SELECT_RECIPIENT_LIST_FOR_REPLY));
    			return null ;
    		}
    	}

    	PrivateMessage rrepMsg = messageManager.createPrivateMessage() ;

    	rrepMsg.setTitle(getReplyToSubject());
    	rrepMsg.setBody(formattedText.processFormattedText(getReplyToBody(), null, null));
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
    	StringBuilder sendToString = new StringBuilder("");
    	StringBuilder sendToHiddenString = new StringBuilder("");

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
    	StringBuilder sendToBccString = new StringBuilder("");
    	StringBuilder sendToBccHiddenString = new StringBuilder("");
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
    	if (StringUtils.isNotEmpty(sendToString.toString())) {
    		sendToString.delete(sendToString.length()-2, sendToString.length()); //remove last comma and space
    	}

    	if (StringUtils.isEmpty(sendToHiddenString.toString())) {
    		rrepMsg.setRecipientsAsText(sendToString.toString());
    	}
    	else {
    		sendToHiddenString.delete(sendToHiddenString.length()-2, sendToHiddenString.length()); //remove last comma and space    
    		rrepMsg.setRecipientsAsText(sendToString.toString() + " " + PrivateMessage.HIDDEN_RECIPIENTS_START + sendToHiddenString.toString() + PrivateMessage.HIDDEN_RECIPIENTS_END);
    	}    

    	//clean sendToBccString
    	//clean sendToString
    	if (StringUtils.isNotEmpty(sendToBccString.toString())) {
    		sendToBccString.delete(sendToBccString.length()-2, sendToBccString.length()); //remove last comma and space
    	}

    	if (StringUtils.isEmpty(sendToBccHiddenString.toString())) {
    		rrepMsg.setRecipientsAsTextBcc(sendToBccString.toString());
    	}
    	else {
    		sendToBccHiddenString.delete(sendToBccHiddenString.length()-2, sendToBccHiddenString.length()); //remove last comma and space    
    		rrepMsg.setRecipientsAsTextBcc(sendToBccString.toString() + " " + PrivateMessage.HIDDEN_RECIPIENTS_START + sendToBccHiddenString.toString() + PrivateMessage.HIDDEN_RECIPIENTS_END);
    	}  
    	
    	//Add attachments
    	for(DecoratedAttachment attachment : (List<DecoratedAttachment>) allAttachments) {
    		prtMsgManager.addAttachToPvtMsg(rrepMsg, attachment.getAttachment());
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
		 this.schedulerSendDateString = (String)FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("openDateISO8601");

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
	  storeDateISO();
	  processPvtMsgForwardSendHelper(getDetailMsg().getMsg());
	  return DISPLAY_MESSAGES_PG;
 }
 public String processPvtMsgForwardSend() {
    log.debug("processPvtMsgForwardSend()");
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
	 log.debug("processPvtMsgForwardSaveDraft()");
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
	 storeDateISO();
	 if(!isDraft){
		 if(getSelectedComposeToList().size()<1 && getSelectedComposeBccList().size() < 1)
		 {
			 setErrorMessage(getResourceBundleString(SELECT_MSG_RECIPIENT));
			 return null ;
		 }
	 }
	 if(StringUtils.isEmpty(getForwardSubject()))
	 {
		 if(isDraft){
			 setErrorMessage(getResourceBundleString(MISSING_SUBJECT_DRAFT));
		 }else{
			 setErrorMessage(getResourceBundleString(MISSING_SUBJECT));
		 }
		 return null ;
	 }
	 if(StringUtils.isEmpty(getForwardBody()))  {
		 if(isDraft) {
			 setErrorMessage(getResourceBundleString(MISSING_BODY_DRAFT));
		 } else {
			 setErrorMessage(getResourceBundleString(MISSING_BODY));
		 }
		 return null;
	 }

    	PrivateMessage rrepMsg = messageManager.createPrivateMessage() ;

    	rrepMsg.setTitle(getForwardSubject());
    	rrepMsg.setDraft(Boolean.FALSE);
    	rrepMsg.setDeleted(Boolean.FALSE);

    	rrepMsg.setAuthor(getAuthorString());
    	rrepMsg.setApproved(Boolean.FALSE);
    	rrepMsg.setBody(formattedText.processFormattedText(getForwardBody(), null, null));

    	rrepMsg.setLabel(getSelectedLabel());

    	rrepMsg.setInReplyTo(currentMessage) ;

    	//Add the recipientList as String for display in Sent folder
    	// Since some users may be hidden, if some of these are recipients
    	// filter them out (already checked if no recipients)
    	// if only 1 recipient no need to check visibility
    	StringBuilder sendToString = new StringBuilder();
    	StringBuilder sendToHiddenString = new StringBuilder();

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

    	StringBuilder sendToBccString = new StringBuilder();
    	StringBuilder sendToBccHiddenString = new StringBuilder();
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
    	if (StringUtils.isNotEmpty(sendToString.toString())) {
    		sendToString.delete(sendToString.length()-2, sendToString.length()); //remove last comma and space
    	}

    	if (StringUtils.isEmpty(sendToHiddenString.toString())) {
    		rrepMsg.setRecipientsAsText(sendToString.toString());
    	}
    	else {
    		sendToHiddenString.delete(sendToHiddenString.length()-2, sendToHiddenString.length()); //remove last comma and space    
    		rrepMsg.setRecipientsAsText(sendToString.toString() + " (" + sendToHiddenString.toString() + ")");
    	}       	      
    	
    	//clean sendToBccString
    	if (StringUtils.isNotEmpty(sendToBccString.toString())) {
    		sendToBccString.delete(sendToBccString.length()-2, sendToBccString.length()); //remove last comma and space
    	}

    	if (StringUtils.isEmpty(sendToBccHiddenString.toString())) {
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
	    if (booleanSchedulerSend) {
		    setOpenDate(schedulerSendDateString);
		    if (booleanSchedulerSend && (openDate == null || !openDate.after(Date.from(Instant.now())))) {
			    setErrorMessage(getResourceBundleString(DATE_PGR_MSG_ERROR));
			    return;
		    }
	    }
	    rrepMsg.setExternalEmail(booleanEmailOut);
	    rrepMsg.setScheduler(booleanSchedulerSend);

	    Map<User, Boolean> recipients = getRecipients();

	    if (booleanSchedulerSend && !rrepMsg.getDraft()) {
		    rrepMsg.setScheduledDate(openDate);
		    schedulerMessage(rrepMsg, isSendEmail());
		    resetComposeContents();
	    } else if(rrepMsg.getDraft()) {
		    List<MembershipItem> draftRecipients = drDelegate.getDraftRecipients(getSelectedComposeToList(), courseMemberMap);
		    List<MembershipItem> draftBccRecipients = drDelegate.getDraftRecipients(getSelectedComposeBccList(), courseMemberMap);

		    Long msgId = prtMsgManager.sendPrivateMessage(rrepMsg, getRecipients(), isSendEmail(), draftRecipients, draftBccRecipients, booleanReadReceipt);
		    manageTagAssociation(msgId);
	    } else {
	        Long msgId = prtMsgManager.sendPrivateMessage(rrepMsg, recipients, isSendEmail(), booleanReadReceipt);
	        manageTagAssociation(msgId);
        }

    	if(!rrepMsg.getDraft()){
    		//update Synoptic tool info
    		incrementSynopticToolInfo(recipients.keySet(), false);
    		LRS_Statement statement = null;
            if (null != learningResourceStoreService) {
            	try{
            		statement = getStatementForUserSentPvtMsg(getDetailMsg().getMsg().getTitle(), SAKAI_VERB.responded);
            	}catch(Exception e){
            		log.error(e.getMessage(), e);
            	}
            }
    		Event event = eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_FORWARD, getEventMessage(rrepMsg), null, true, NotificationService.NOTI_OPTIONAL, statement);
            eventTrackingService.post(event);
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
		 this.schedulerSendDateString = (String)FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("openDateISO8601");

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
	 log.debug("processPvtMsgReply All Send()");
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
    log.debug("processPvtMsgReply All Send()");
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
	  setComposeLists(currentMessage);
	  String msgauther=currentMessage.getAuthor();//string   "Test"      
	  storeDateISO();
	  //Select Forward Recipients
	  
	  if(StringUtils.isEmpty(getReplyToAllSubject())) {
		  if(isDraft){
			  setErrorMessage(getResourceBundleString(MISSING_SUBJECT_DRAFT));
		  }else{
			  setErrorMessage(getResourceBundleString(MISSING_SUBJECT));
		  }
		  return null ;
	  }
	  if(StringUtils.isEmpty(getReplyToAllBody())) {
		  if(isDraft) {
			  setErrorMessage(getResourceBundleString(MISSING_BODY_DRAFT));
		  } else {
			  setErrorMessage(getResourceBundleString(MISSING_BODY));
		  }
		  return null;
	  }
	  if(!isDraft){
		  if(selectedComposeToList.isEmpty() && selectedComposeBccList.isEmpty()){
			  setErrorMessage(getResourceBundleString(SELECT_RECIPIENT_LIST_FOR_REPLY));
			  return null ;
		  }
	  }

	  PrivateMessage rrepMsg = messageManager.createPrivateMessage() ;


	  rrepMsg.setTitle(getReplyToAllSubject());
	  rrepMsg.setDraft(isDraft);
	  rrepMsg.setDeleted(Boolean.FALSE);

	  rrepMsg.setAuthor(getAuthorString());
	  rrepMsg.setApproved(Boolean.FALSE);
	  String replyAllbody=getReplyToAllBody();


	  rrepMsg.setBody(formattedText.processFormattedText(replyAllbody, null, null));
	  rrepMsg.setLabel(getSelectedLabel());
	  rrepMsg.setInReplyTo(currentMessage) ;


	  //Add attachments
	  for(int i=0; i<allAttachments.size(); i++)
	  {
		  prtMsgManager.addAttachToPvtMsg(rrepMsg, ((DecoratedAttachment)allAttachments.get(i)).getAttachment());         
	  }            

	  User autheruser=null;
	  try {
		  autheruser = userDirectoryService.getUser(currentMessage.getCreatedBy());
	  } catch (UserNotDefinedException e) {
		  log.error(e.getMessage(), e);
	  }

	  
	  List tmpRecipList = currentMessage.getRecipients();

	  Map<User, Boolean> returnSet = new HashMap<>();
	  StringBuilder sendToStringreplyall = new StringBuilder();

	  for (PrivateMessageRecipient tmpPMR : (List<PrivateMessageRecipient>) tmpRecipList) {
		  User replyrecipientaddtmp=null;
		  try {
			  replyrecipientaddtmp = userDirectoryService.getUser(tmpPMR.getUserId());
		  } catch (UserNotDefinedException e) {
			  log.warn("Unable to find user : " + tmpPMR.getUserId(), e);
		  }

		  if (replyrecipientaddtmp == null){
			  log.warn("continuing passed user : "+tmpPMR.getUserId());
			  //throw new IllegalStateException("User replyrecipientaddtmp == null!");
		  }else{
		  	if(!(replyrecipientaddtmp.getDisplayName()).equals(getUserName()) && !tmpPMR.getBcc())//&&(!(replyrecipientaddtmp.getDisplayName()).equals(msgauther)))
		  	{
				  returnSet.put(replyrecipientaddtmp, tmpPMR.getBcc());
		        }
		  }
	  }

	  if(currentMessage.getRecipientsAsText() != null && StringUtils.isNotEmpty(currentMessage.getRecipientsAsText())){
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
	  StringBuilder sendToString = new StringBuilder(sendToStringreplyall);
	  StringBuilder sendToHiddenString = new StringBuilder();

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
	  StringBuilder sendToBccString = new StringBuilder();
	  StringBuilder sendToBccHiddenString = new StringBuilder();
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
	  if (StringUtils.isNotEmpty(sendToString.toString()) && sendToString.length() >= 2) {
		  sendToString.delete(sendToString.length()-2, sendToString.length()); //remove last comma and space
	  }

	  if (StringUtils.isEmpty(sendToHiddenString.toString())) {
		  rrepMsg.setRecipientsAsText(sendToString.toString());
	  }
	  else {
		  sendToHiddenString.delete(sendToHiddenString.length()-2, sendToHiddenString.length()); //remove last comma and space    
		  rrepMsg.setRecipientsAsText(sendToString.toString() + " (" + sendToHiddenString.toString() + ")");
	  }

	  //clean sendToBccString
	  if (StringUtils.isNotEmpty(sendToBccString.toString()) && sendToBccString.length() >= 2) {
		  sendToBccString.delete(sendToBccString.length()-2, sendToBccString.length()); //remove last comma and space
	  }

	  if (StringUtils.isEmpty(sendToBccHiddenString.toString())) {
		  rrepMsg.setRecipientsAsTextBcc(sendToBccString.toString());
	  }
	  else {
		  sendToBccHiddenString.delete(sendToBccHiddenString.length()-2, sendToBccHiddenString.length()); //remove last comma and space    
		  rrepMsg.setRecipientsAsTextBcc(sendToBccString.toString() + " (" + sendToBccHiddenString.toString() + ")");
	  }

	  //Add users that joined the groups to which the message was sent (after it was sent)
	  List<User> usersFromGroupsInCC = getUsersFromGroupsInCC();
	  if (usersFromGroupsInCC != null && !usersFromGroupsInCC.isEmpty()) {
		  for (User user : usersFromGroupsInCC) {
			  //only if it wasn't part already
			  if (!returnSet.containsKey(user)) {
				  returnSet.put(user, false);
				  log.debug("User '{}' added to the reply all list", user.getDisplayName());
			  }
		  }
	  }

	  //Add selected users to reply all list

	  Map<User, Boolean> recipients = getRecipients();
	  for (Entry<User, Boolean> entrySet : recipients.entrySet()){
		  if(!returnSet.containsKey(entrySet.getKey())){
			  returnSet.put(entrySet.getKey(), entrySet.getValue());
		  }
	  }
	  if(!preview){
		  if(booleanSchedulerSend) {
	        setOpenDate(schedulerSendDateString);
	        if(booleanSchedulerSend && (openDate == null || !openDate.after(Date.from(Instant.now())))) {
	            setErrorMessage(getResourceBundleString(DATE_PGR_MSG_ERROR));
	            return null;
	        }
		  }
		    rrepMsg.setExternalEmail(booleanEmailOut);
		    rrepMsg.setScheduler(booleanSchedulerSend);

	        if(booleanSchedulerSend && !rrepMsg.getDraft()) {
	            rrepMsg.setScheduledDate(openDate);
	            schedulerMessage(rrepMsg, isSendEmail());
	            resetComposeContents();
	        }else if(rrepMsg.getDraft()) {
                List<MembershipItem> draftRecipients = drDelegate.getDraftRecipients(getSelectedComposeToList(), courseMemberMap);
                List<MembershipItem> draftBccRecipients = drDelegate.getDraftRecipients(getSelectedComposeBccList(), courseMemberMap);

                Long msgId = prtMsgManager.sendPrivateMessage(rrepMsg, getRecipients(), isSendEmail(), draftRecipients, draftBccRecipients, booleanReadReceipt);
                manageTagAssociation(msgId);
	        } else {
	            Long msgId = prtMsgManager.sendPrivateMessage(rrepMsg, returnSet, isSendEmail(), booleanReadReceipt);
	            manageTagAssociation(msgId);
	        }

		  if(!rrepMsg.getDraft()){
			  prtMsgManager.markMessageAsRepliedForUser(getReplyingMessage());
			  //update Synoptic tool info
			  incrementSynopticToolInfo(returnSet.keySet(), false);
			  
			  LRS_Statement statement = null;
	          if (null != learningResourceStoreService) {
	        	  try{
	        		  statement = getStatementForUserSentPvtMsg(getDetailMsg().getMsg().getTitle(), SAKAI_VERB.responded);
	        	  }catch(Exception e){
	          		log.error(e.getMessage(), e);
	        	  }
	          }
     		  Event event = eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_FORWARD, getEventMessage(rrepMsg), null, true, NotificationService.NOTI_OPTIONAL, statement);
	          eventTrackingService.post(event);
		  }
		  //reset contents
		  resetComposeContents();
	  }
	  return rrepMsg;
  }

    private List<User> getUsersFromGroupsInCC() {
        log.debug("getUsersFromGroupsInCC()");
        //Try to get members of the groups to which the message was sent
        List<User> usersFromGroupsInCC = new ArrayList<>();
        Site currentSite = getCurrentSite();

        if (currentSite != null && currentSite.hasGroups()) {
            List<Group> siteGroups = new ArrayList<>(currentSite.getGroups());
            //this string is all we know about previously selected groups..
            String msgCClistString = getDetailMsg().getRecipientsAsText();
            String[] ccItems = msgCClistString.split(";");

            for (String ccItem : ccItems) {
                //group prefix/suffix
                String groupLabel = rb.getString("participants_group_desc")
                    .replace("{0}", "").trim();
                //we know ccItem is a group if it says so..
                if (ccItem.contains(groupLabel)) {

                    for (Group group : siteGroups) {
                        //get group title by removing the group label from the CC item
                        String groupTitle = ccItem.replace(groupLabel, "").trim();
                        //get group users if title matches
                        if (group.getTitle().equals(groupTitle)) {
                            Set<String> userIds = group.getUsers();

                            for (String userId : userIds) {
                                try {
                                    User user = userDirectoryService.getUser(userId);
                                    usersFromGroupsInCC.add(user);
                                } catch (UserNotDefinedException e) {
                                    log.error(e.getMessage(), e);
                                }
                            }
                        } else {
                            //group title modifications will cause mismatches
                            log.warn("Group title '{}' doesn't match any group in current site", groupTitle);
                        }
                    }
                } else {
                    //language-sensitive comparison, may lead to mismatches
                    log.warn("ccItem '{}' doesn't contain group label '{}'", ccItem, groupLabel);
                }
            }
        } else {
            log.info("No groups found in current site");
        }
        return usersFromGroupsInCC;
    }

  private void manageTagAssociation(Long msgId) {
    log.debug("msgId " + msgId + " - selectedTags " + selectedTags);
    if (msgId != null && ServerConfigurationService.getBoolean("tagservice.enable.integrations", true) && isInstructor() && selectedTags != null) {
      List<String> tagIds = Arrays.asList(selectedTags.split(","));
      tagService.updateTagAssociations(getUserId(), String.valueOf(msgId), tagIds, false);
      selectedTags = String.join(",", tagService.getTagAssociationIds(getUserId(), String.valueOf(msgId)));
    }
  }

 private boolean containedInList(User user,List list){

	boolean isContain=false;
	 if (list==null)
	 {
		 return false;
	 }

	   for(PrivateMessageRecipient tmpPMR : (List<PrivateMessageRecipient>)list) {

		 	User replyrecipientaddtmp=null;
				try {
					replyrecipientaddtmp = userDirectoryService.getUser(tmpPMR.getUserId());
				} catch (UserNotDefinedException e) {
					log.error(e.getMessage(), e);
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
    log.debug("processPvtMsgEmptyDelete()");
    
    List delSelLs=new ArrayList() ;
    for (PrivateMessageDecoratedBean element : (List<PrivateMessageDecoratedBean>) this.decoratedPvtMsgs) {
      if(element.getIsSelected()) {
         delSelLs.add(element);
      }
    }
    this.setSelectedDeleteItems(delSelLs);
    if(delSelLs.isEmpty())
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
    log.debug("processPvtMsgMultiDelete()");
  
    boolean deleted = false;
    for (PrivateMessageDecoratedBean privateMessageDecoratedBean : (List<PrivateMessageDecoratedBean>) getSelectedDeleteItems()) {
      PrivateMessage element = privateMessageDecoratedBean.getMsg();
      if (element != null) 
      {
    	deleted = true;
        prtMsgManager.deletePrivateMessage(element, getPrivateMessageTypeFromContext(msgNavMode)) ;
	    PrivateMessageSchedulerService.removeScheduledReminder(element.getId());
      }      
      
      if (PVTMSG_MODE_DELETE.equals(msgNavMode))
    	  eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_REMOVE, getEventMessage((Message) element), false));
      else
    	  eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_MOVE_TO_DELETED_FOLDER, getEventMessage((Message) element), false));

    }
    
    if (deleted)
    {
    	if (PVTMSG_MODE_DELETE.equals(msgNavMode))
    		multiDeleteSuccessMsg = getResourceBundleString(PERM_DELETE_SUCCESS_MSG);
    	else
    		multiDeleteSuccessMsg = getResourceBundleString(MULTIDELETE_SUCCESS_MSG);
    	
    	multiDeleteSuccess = true;
    }


	return DISPLAY_MESSAGES_PG;
  }

  
  public String processPvtMsgDispOtions() 
  {
    log.debug("processPvtMsgDispOptions()");
    
    return "pvtMsgOrganize" ;
  }
  
  
  ///////////////////////////       Process Select All       ///////////////////////////////
  @Getter @Setter
  private boolean selectAll = false;  
  @Getter @Setter
  private int numberChecked = 0; // to cover case where user selectes check all

  /**
   * process isSelected for all decorated messages
   * @return same page i.e. will be pvtMsg 
   */
  public String processCheckAll()
  {
    log.debug("processCheckAll()");
    selectAll= true;
    multiDeleteSuccess = false;

    return null;
  }
  
  //////////////////////////////   ATTACHMENT PROCESSING        //////////////////////////
  private ArrayList<DecoratedAttachment> attachments = new ArrayList();
  
  private String removeAttachId = null;
  private ArrayList prepareRemoveAttach = new ArrayList();
  private ArrayList oldAttachments = new ArrayList();
  private List allAttachments = new ArrayList();

  
  public ArrayList getAttachments()
  {
    ToolSession session = sessionManager.getCurrentToolSession();
    if (session.getAttribute(FilePickerHelper.FILE_PICKER_CANCEL) == null &&
        session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS) != null) 
    {
      final List<Reference> refs = (List<Reference>) session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
      if(CollectionUtils.isNotEmpty(refs))
      {
        for(Reference ref : refs) {
          Attachment thisAttach = prtMsgManager.createPvtMsgAttachment(
              ref.getId(), ref.getProperties().getProperty(ref.getProperties().getNamePropDisplayName()));
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
    ToolSession session = sessionManager.getCurrentToolSession();
    if (session.getAttribute(FilePickerHelper.FILE_PICKER_CANCEL) == null &&
        session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS) != null) 
    {
      final List<Reference> refs = (List<Reference>) session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
      if(CollectionUtils.isNotEmpty(refs))
      {
        for(Reference ref : refs) {
          Attachment thisAttach = prtMsgManager.createPvtMsgAttachment(
              ref.getId(), ref.getProperties().getProperty(ref.getProperties().getNamePropDisplayName()));
          allAttachments.add(new DecoratedAttachment(thisAttach));
        }
      }
    }
    session.removeAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
    session.removeAttribute(FilePickerHelper.FILE_PICKER_CANCEL);
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
    if(StringUtils.isNotEmpty(removeAttachId)) {
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
    log.debug("processAddAttachmentRedirect()");
    try
    {
      ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
      context.redirect("sakai.filepicker.helper/tool");
      return null;
    }
    catch(Exception e)
    {
      log.debug("processAddAttachmentRedirect() - Exception");
      return null;
    }
  }
  
  //Process remove attachment 
  public String processDeleteAttach() {
    log.debug("processDeleteAttach()");

    ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();

    Map<String, String> paramMap = context.getRequestParameterMap();

    if(paramMap != null) {
        final String attachId = paramMap.entrySet().stream().
                filter(entry -> entry.getKey().contains("pvmsg_current_attach")).
                collect(Collectors.collectingAndThen(Collectors.toList(), list -> list.isEmpty() ? null : list.get(0).getValue()));
        if (StringUtils.isNotEmpty(attachId)) {
            attachments.removeIf(da -> attachId.equalsIgnoreCase(da.getAttachment().getAttachmentId()));
        }
    }
    return null;
  }
 
  
  //Process remove attachments from reply message  
  public String processDeleteReplyAttach()
  {
    log.debug("processDeleteReplyAttach()");
    
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
    
    if (StringUtils.isNotEmpty(attachId)) {
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
    log.debug("processRemoveAttach()");
    
    try
    {
      Attachment sa = prtMsgManager.getPvtMsgAttachment(Long.valueOf(removeAttachId));
      String id = sa.getAttachmentId();
      
      for(int i=0; i<attachments.size(); i++)
      {
        DecoratedAttachment thisAttach = attachments.get(i);
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
      log.debug("processRemoveAttach() - Exception");
    }
    
    removeAttachId = null;
    prepareRemoveAttach.clear();
    return COMPOSE_MSG_PG;
    
  }
  
  public String processRemoveAttachCancel()
  {
    log.debug("processRemoveAttachCancel()");
    
    removeAttachId = null;
    prepareRemoveAttach.clear();
    return COMPOSE_MSG_PG ;
  }

  public boolean getSuperUser()
  {
    superUser=securityService.isSuperUser();
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

  public boolean isCanUseTags() {
    boolean tagServiceEnabled = ServerConfigurationService.getBoolean(TagService.TAGSERVICE_ENABLED_INTEGRATION_PROP, TagService.TAGSERVICE_ENABLED_INTEGRATION_DEFAULT);
    boolean manageTagsAllowed = securityService.unlock(userDirectoryService.getCurrentUser(), TagService.TAGSERVICE_MANAGE_PERMISSION, getContextSiteId());

    log.debug("IsTagServiceEnabled:{}|HasCurrentUserTagPermission:{}", tagServiceEnabled, manageTagsAllowed);
    
    return tagServiceEnabled && manageTagsAllowed;
  }

  public String processPvtMsgOrganize()
  {
    log.debug("processPvtMsgOrganize()");
    return null ;
    //return "pvtMsgOrganize";
  }

  public String processPvtMsgStatistics()
  {
    log.debug("processPvtMsgStatistics()");
    
    return null ;
    //return "pvtMsgStatistics";
  }
  
  public String processPvtMsgSettings()
  {
    log.debug("processPvtMsgSettings()");    
    return MESSAGE_SETTING_PG;
  }
    
  public void processPvtMsgSettingsRevise(ValueChangeEvent event)
  {
    log.debug("processPvtMsgSettingsRevise()");   
    
    /** block executes when changing value to "no" */

    switch (forwardPvtMsg) {
	    case SET_AS_YES:
	    	setForwardPvtMsgEmail(null);
	    	break;
	    case SET_AS_NO:
	    	setValidEmail(true);
	    	break;
	    default:
	    	setValidEmail(true);
	    	break;
    }
  }
  
  public String processPvtMsgSettingsSave()
  {
    log.debug("processPvtMsgSettingsSave()");
    
 
    String email= getForwardPvtMsgEmail();
    String activate=getActivatePvtMsg() ;
    String forward=getForwardPvtMsg() ;
    if (email != null && (SET_AS_YES.equals(forward)) 
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
          log.warn("Non-numeric option for sending email to recipient email address on Message screen. This may indicate a UI problem.");
          setErrorMessage(getResourceBundleString("pvt_send_to_email_invalid"));
          return null;
      }

      switch (forward) {
	      case SET_AS_YES:
	    	  forum.setAutoForward(PrivateForumImpl.AUTO_FOWARD_YES);
	    	  break;
	      case SET_AS_NO:
	    	  forum.setAutoForward(PrivateForumImpl.AUTO_FOWARD_NO);
	    	  break;
	      default:
	    	  forum.setAutoForward(PrivateForumImpl.AUTO_FOWARD_DEFAULT);
	    	  break;
      }

      if (SET_AS_YES.equals(forward)){
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
  @Getter @Setter
  private String addFolder;
  private boolean ismutable;
  @Getter @Setter
  private int totalMsgInFolder;

  public boolean getIsmutable()
  {
    return prtMsgManager.isMutableTopicFolder(getSelectedTopicId());
  }

  //navigated from header pagecome from Header page 
  public String processPvtMsgFolderSettings() {
    log.debug("processPvtMsgFolderSettings()");
    //String topicTitle= getExternalParameterByKey("pvtMsgTopicTitle");
    String topicTitle = forumManager.getTopicByUuid(getExternalParameterByKey(EXTERNAL_TOPIC_ID)).getTitle();
    setSelectedTopicTitle(topicTitle) ;
    String topicId=getExternalParameterByKey(EXTERNAL_TOPIC_ID) ;
    setSelectedTopicId(topicId);
    
    setFromMainOrHp();
    
    return MESSAGE_FOLDER_SETTING_PG;
  }

  public String processPvtMsgFolderSettingRevise() {
    log.debug("processPvtMsgFolderSettingRevise()");
    
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
    log.debug("processPvtMsgFolderSettingAdd()");
    
    setFromMainOrHp();
    this.setAddFolder("");  // make sure the input box is empty
    
    return ADD_MESSAGE_FOLDER_PG ;
  }
  public String processPvtMsgFolderSettingDelete() {
    log.debug("processPvtMsgFolderSettingDelete()");
    
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
	    log.debug("processPvtMsgReturnToMainOrHp()");
	    if(fromMainOrHp != null && (fromMainOrHp.equals(MESSAGE_HOME_PG) || (fromMainOrHp.equals(MAIN_PG))))
	    {
	    	String returnToPage = fromMainOrHp;
			fromMainOrHp = "";
			return returnToPage;
	    }
	    else
	    {
	    	return MESSAGE_HOME_PG ;
	    }
  }
  
  public String processPvtMsgReturnToFolderView() 
  {
	  return MESSAGE_FOLDER_SETTING_PG;
  }
  
  //Create a folder within a forum
  public String processPvtMsgFldCreate() 
  {
    log.debug("processPvtMsgFldCreate()");
    
    String createFolder=getAddFolder() ;   
    createFolder = formattedText.processFormattedText(createFolder, null, null);
    if(StringUtils.isEmpty(createFolder)) {
    	setErrorMessage(getResourceBundleString(ENTER_FOLDER_NAME));
      	return null ;
    } else if((PVTMSG_MODE_RECEIVED.toLowerCase()).equals(createFolder.toLowerCase().trim()) || (PVTMSG_MODE_SENT.toLowerCase()).equals(createFolder.toLowerCase().trim())|| 
            (PVTMSG_MODE_DELETE.toLowerCase()).equals(createFolder.toLowerCase().trim()) || (PVTMSG_MODE_DRAFT.toLowerCase()).equals(createFolder.toLowerCase().trim()) || (PVTMSG_MODE_SCHEDULER.toLowerCase()).equals(createFolder.toLowerCase().trim()))
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
    log.debug("processPvtMsgFldRevise()");
    
    String newTopicTitle = this.getSelectedNewTopicTitle(); 
    
    if(StringUtils.isEmpty(newTopicTitle))
    {
      setErrorMessage(getResourceBundleString(FOLDER_NAME_BLANK));
      return REVISE_FOLDER_PG;
    }
    else if((PVTMSG_MODE_RECEIVED.toLowerCase()).equals(newTopicTitle.toLowerCase().trim()) || (PVTMSG_MODE_SENT.toLowerCase()).equals(newTopicTitle.toLowerCase().trim())|| 
            (PVTMSG_MODE_DELETE.toLowerCase()).equals(newTopicTitle.toLowerCase().trim()) || (PVTMSG_MODE_DRAFT.toLowerCase()).equals(newTopicTitle.toLowerCase().trim()) || (PVTMSG_MODE_SCHEDULER.toLowerCase()).equals(newTopicTitle.toLowerCase().trim()))
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
      for(PrivateMessage tmpPM : (List<PrivateMessage>) tmpMsgList) {
      	List tmpRecipList = tmpPM.getRecipients();
      	tmpPM.setTypeUuid(newTypeUuid);
      	String currentUserId = sessionManager.getCurrentSessionUserId();
      	for(PrivateMessageRecipient tmpPMR : (List<PrivateMessageRecipient>) tmpRecipList) {
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
    log.debug("processPvtMsgFldDelete()");
    
    prtMsgManager.deleteTopicFolder(forum,getSelectedTopicId()) ;
    
    //delete the messages
    String typeUuid = getPrivateMessageTypeFromContext(selectedTopicTitle);
    List allPvtMsgs= prtMsgManager.getMessagesByType(typeUuid,PrivateMessageManager.SORT_COLUMN_DATE, PrivateMessageManager.SORT_DESC);
    for (PrivateMessage element : (List<PrivateMessage>) allPvtMsgs) {
      prtMsgManager.deletePrivateMessage(element, typeUuid);
      PrivateMessageSchedulerService.removeScheduledReminder(element.getId());
    }
    return processPvtMsgReturnToMainOrHp();
  }
  
  //create folder within folder
  public String processPvtMsgFolderInFolderAdd()
  {
    log.debug("processPvtMsgFolderSettingAdd()");  
    
    setFromMainOrHp();
    this.setAddFolder("");
    
    return ADD_FOLDER_IN_FOLDER_PG ;
  }
 
  //create folder within Folder
  //TODO - add parent fodler id for this  
  public String processPvtMsgFldInFldCreate() 
  {
    log.debug("processPvtMsgFldCreate()");
    
    PrivateTopic parentTopic=(PrivateTopic) prtMsgManager.getTopicByUuid(selectedTopicId);
    
    String createFolder=getAddFolder() ;
    if(createFolder == null || createFolder.trim().length() == 0)
    {
      setErrorMessage(getResourceBundleString(ENTER_FOLDER_NAME));
      return null ;
    } 
    else if((PVTMSG_MODE_RECEIVED.toLowerCase()).equals(createFolder.toLowerCase().trim()) || (PVTMSG_MODE_SENT.toLowerCase()).equals(createFolder.toLowerCase().trim())|| 
            (PVTMSG_MODE_DELETE.toLowerCase()).equals(createFolder.toLowerCase().trim()) || (PVTMSG_MODE_DRAFT.toLowerCase()).equals(createFolder.toLowerCase().trim()) || (PVTMSG_MODE_SCHEDULER.toLowerCase()).equals(createFolder.toLowerCase().trim()))
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
    multiDeleteSuccess = false;
    if(StringUtils.isNotEmpty(moveToNewTopic))
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
    log.debug("processPvtMsgMove()");
    return MOVE_MESSAGE_PG;
  }
  
  public String processPvtMsgPublishToFaq() {
    log.debug("processPvtMsgPublishToFaq()");
    MessageForumPublishToFaqBean publishToFaqBean =
        (MessageForumPublishToFaqBean) lookupBean(MessageForumPublishToFaqBean.NAME);

    if (StringUtils.isBlank(publishToFaqBean.getTitle())) {
      setErrorMessage(getResourceBundleString(MISSING_TITLE));
      return null;
    } else if (StringUtils.isBlank(publishToFaqBean.getQuestion())) {
      setErrorMessage(getResourceBundleString(MISSING_QUESTION));
      return null;
    } else {
      publishToFaqBean.publishToFaq();
      multiDeleteSuccessMsg = getResourceBundleString(SUCCESS_PUBLISH_TO_FAQ);
      multiDeleteSuccess = true;
      return SELECTED_MESSAGE_PG;
    }
    
  }

  public String processPvtMsgPublishToFaqEdit() {
    log.debug("processPvtMsgPublishToFaqEdit()");
    multiDeleteSuccess = false;

    MessageForumPublishToFaqBean publishToFaqBean =
        (MessageForumPublishToFaqBean) lookupBean(MessageForumPublishToFaqBean.NAME);

    publishToFaqBean.setMessage(detailMsg.getMsg());

    return PUBLISH_TO_FAQ_EDIT;
  }

  public Object lookupBean(String beanName) {
    ApplicationFactory applicationFactory = (ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);

    return (Serializable) applicationFactory.getApplication().getVariableResolver()
        .resolveVariable(FacesContext.getCurrentInstance(), beanName);
  }

  public void processPvtMsgParentFolderMove(ValueChangeEvent event)
  {
    log.debug("processPvtMsgSettingsRevise()"); 
    if ((String)event.getNewValue() != null)
    {
      moveToNewTopic= (String)event.getNewValue();
    }
  }
  
  public String processPvtMsgMoveMessage()
  {
    log.debug("processPvtMsgMoveMessage()");
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
    		for (PrivateMessageDecoratedBean decoMessage : (List<PrivateMessageDecoratedBean>) selectedMoveToFolderItems ) {
				PrivateMessage message = decoMessage.getMsg();
				final PrivateMessage initPrivateMessage = prtMsgManager.initMessageWithAttachmentsAndRecipients(message);
				PrivateMessageDecoratedBean newDecoMessage = new PrivateMessageDecoratedBean(initPrivateMessage);

				prtMsgManager.movePvtMsgTopic(newDecoMessage.getMsg(), oldTopic, newTopic);

				Long msgId = newDecoMessage.getMsg().getId();
				PrivateMessage pvtMsg= (PrivateMessage) prtMsgManager.getMessageById(msgId);

				if(Boolean.TRUE.equals((pvtMsg.getScheduler())) && (pvtMsg.getScheduledDate()!=null) && (Boolean.TRUE.equals((pvtMsg.getDraft()))) && newTopic.getTitle().equals(PVTMSG_MODE_SCHEDULER)) {
					PrivateMessageSchedulerService.scheduleDueDateReminder(pvtMsg.getId());
				}
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
    log.debug("processPvtMsgCancelToDetailView()");
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
  @Setter
  private List searchPvtMsgs;
  public List getSearchPvtMsgs()
  {
    if(selectView!=null && selectView.equalsIgnoreCase(THREADED_VIEW)) {
        this.rearrageTopicMsgsThreaded(true);
    }
    //  If "check all", update the decorated pmb to show selected
    if (selectAll) {
    	for (PrivateMessageDecoratedBean searchMsg : (List<PrivateMessageDecoratedBean>) searchPvtMsgs) {
    		searchMsg.setIsSelected(true);
    	}

    	selectAll = false;
    }
    return searchPvtMsgs;
  }

  public String processSearch() 
  {
    log.debug("processSearch()");
    multiDeleteSuccess = false;

    List newls = new ArrayList() ;
    List tempPvtMsgLs= new ArrayList();

    // If the hidden values contain valid ISO dates set them
    Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
    String searchFromISODate = params.get(HIDDEN_SEARCH_FROM_ISO_DATE);
    String searchToISODate = params.get(HIDDEN_SEARCH_TO_ISO_DATE);

    if(DateFormatterUtil.isValidISODate(searchFromISODate)){
        setSearchFromDate(DateFormatterUtil.parseISODate(searchFromISODate));
    }

    if(DateFormatterUtil.isValidISODate(searchToISODate)){
        setSearchToDate(DateFormatterUtil.parseISODate(searchToISODate));
    }

    if(searchOnDate && searchFromDate == null && searchToDate==null) {
       setErrorMessage(getResourceBundleString(MISSING_BEG_END_DATE));
    }

    if(searchOnSubject && StringUtils.isEmpty(searchText)) {
       setErrorMessage(getResourceBundleString(ENTER_SEARCH_TEXT));
    }

    if(searchOnTags && StringUtils.isEmpty(selectedTags)) {
       setErrorMessage(getResourceBundleString(ENTER_SEARCH_TAGS));
    }

    if(searchToDate != null){
        searchToDate = Date.from(searchToDate.toInstant().plus(23, ChronoUnit.HOURS).plus(59, ChronoUnit.MINUTES).plusSeconds(59));
    }

    tempPvtMsgLs= prtMsgManager.searchPvtMsgs(getPrivateMessageTypeFromContext(msgNavMode), 
          getSearchText(), getSearchFromDate(), getSearchToDate(), getSelectedSearchLabel(),
          searchOnSubject, searchOnAuthor, searchOnBody, searchOnLabel, searchOnDate) ;
    
    List<String> selectedTagsList = selectedTags != null ? Arrays.asList(selectedTags.split(",")) : new ArrayList<>();
    if(searchOnTags && CollectionUtils.isNotEmpty(selectedTagsList)) {
        tempPvtMsgLs = ((List<PrivateMessage>)tempPvtMsgLs).stream().filter(pm -> {
                List<String> tagIds = tagService.getTagAssociationIds(getUserId(), String.valueOf(pm.getId()));
                return (tagIds.containsAll(selectedTagsList));
        }).collect(Collectors.toList());
    }
    
    newls= createDecoratedDisplay(tempPvtMsgLs);

    //set threaded view as  false in search 
    selectView="";

    if(!newls.isEmpty()) {
        this.setSearchPvtMsgs(newls);
        return SEARCH_RESULT_MESSAGES_PG;
    } else {
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
    searchOnTags=false;
    searchFromDate=null;
    searchToDate=null;
    searchFromDateString=null;
    searchToDateString=null;
    selectedTags = "";
    
    return DISPLAY_MESSAGES_PG;
  }

  @Getter @Setter
  public boolean searchOnBody=false ;
  @Getter @Setter
  public boolean searchOnSubject=true;  //default is search on Subject
  @Getter @Setter
  public boolean searchOnLabel= false ;
  @Getter @Setter
  public boolean searchOnAuthor=false;
  @Getter @Setter
  public boolean searchOnDate=false;
  @Getter @Setter
  public boolean searchOnTags=false;
  @Getter @Setter
  public Date searchFromDate;
  @Getter @Setter
  public Date searchToDate;
  @Getter @Setter
  public String selectedSearchLabel="pvt_priority_normal";
  @Getter @Setter
  public String searchFromDateString;
  @Getter @Setter
  public String searchToDateString; 

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

    for (PrivateMessage element : (List<PrivateMessage>) msg){

      PrivateMessageDecoratedBean dbean= new PrivateMessageDecoratedBean(element);
      //if processSelectAll is set, then set isSelected true for all messages,
      if(selectAll)
      {
        dbean.setIsSelected(true);
        numberChecked++;
      }
       
      //getRecipients() is filtered for this particular user i.e. returned list of only one PrivateMessageRecipient object
      for (PrivateMessageRecipient el : (List<PrivateMessageRecipient>) element.getRecipients()){

        if (el != null){
          dbean.setHasRead(el.getRead().booleanValue());
          dbean.setReplied(el.getReplied().booleanValue());
        }
      }
        dbean.setSendToStringDecorated(createDecoratedSentToDisplay(dbean));

      List<String> tagLabels = tagService.getAssociatedTagsForItem(getUserId(), String.valueOf(element.getId())).stream().map(Tag::getTagLabel).collect(Collectors.toList());
      dbean.setTagList(tagLabels);

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
	  Map<User, Boolean> returnSet = new HashMap<>();
    
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
    for (User user : composeToSet.keySet()) {
    	if(returnSet.containsKey(user)){
    		returnSet.remove(user);
    	}
    }
    //now add them all back
    returnSet.putAll(composeToSet);
    
    return returnSet;
  }

  private Map<User, Boolean> getRecipientsHelper(List selectedList, List allCourseUsers, boolean bcc){

	  Map<User, Boolean>  returnSet = new HashMap<>();

	  for (String selectedItem : (List<String>) selectedList){

		  /** lookup item in map */
		  MembershipItem item = (MembershipItem) courseMemberMap.get(selectedItem);
		  if (item == null){
			  log.warn("getRecipients() could not resolve uuid: " + selectedItem);
		  }
		  else{
              if (MembershipItem.TYPE_ALL_PARTICIPANTS == item.getType()) {
				  for (MembershipItem member : (List<MembershipItem>) allCourseUsers){
					  returnSet.put(member.getUser(), bcc);
				  }
				  //if all users have been selected we may as well return and ignore any other entries
				  return returnSet;
			  } else if (MembershipItem.TYPE_ROLE == item.getType()) {
				  for (MembershipItem member : (List<MembershipItem>) allCourseUsers){
					  if (member.getRole().equals(item.getRole())){
						  returnSet.put(member.getUser(), bcc);
					  }
				  }
			  } else if (MembershipItem.TYPE_GROUP == item.getType() || MembershipItem.TYPE_MYGROUPS == item.getType()) {
				  for (MembershipItem member : (List<MembershipItem>) allCourseUsers){
					  Set groupMemberSet = item.getGroup().getMembers();
					  for (Member m : (Set<Member>) groupMemberSet){
						  if (m.getUserId() != null && m.getUserId().equals(member.getUser().getId())){
							  returnSet.put(member.getUser(), bcc);
						  }
					  }
				  }
			  } else if (MembershipItem.TYPE_USER == item.getType() || MembershipItem.TYPE_MYGROUPMEMBERS == item.getType()) {
				  returnSet.put(item.getUser(), bcc);
			  } else if (MembershipItem.TYPE_MYGROUPROLES == item.getType()) {
				  for (MembershipItem member : (List<MembershipItem>) allCourseUsers){
					  Set groupMemberSet = item.getGroup().getMembers();
					  for (Member m : (Set<Member>) groupMemberSet){
						  if (m.getUserId() != null && m.getUserId().equals(member.getUser().getId()) && member.getRole().equals(item.getRole())){
							  returnSet.put(member.getUser(), bcc);
						  }
					  }
				  }
			  }
			  else{
				  log.warn("getRecipients() could not resolve membership type: " + item.getType());
			  }
		  }
	  }
	  return returnSet;
  }

  /**
   * getUserRecipients
   * @param courseMembers
   * @return set of all User objects for course
   */
  private Set getUserRecipients(Set courseMembers){
    Set returnSet = new HashSet();
    
    for (MembershipItem item : (Set<MembershipItem>) courseMembers) {
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
    
    for (MembershipItem item : (Set<MembershipItem>) courseMembers){

      if (item.getRole().getId().equalsIgnoreCase(roleName)){
        returnSet.add(item.getUser());
      }
    }
    return returnSet;
  }
      
  private String getPrivateMessageTypeFromContext(String navMode){
    switch(navMode){
        case PVTMSG_MODE_RECEIVED:
            return typeManager.getReceivedPrivateMessageType();
        case PVTMSG_MODE_SENT:
            return typeManager.getSentPrivateMessageType();
        case PVTMSG_MODE_DELETE:
            return typeManager.getDeletedPrivateMessageType();
        case PVTMSG_MODE_DRAFT:
            return typeManager.getDraftPrivateMessageType();
        case PVTMSG_MODE_SCHEDULER:
            return typeManager.getSchedulerPrivateMessageType();
        default:
            return typeManager.getCustomTopicType(navMode);
    }
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

  /**
   * @param errorMsg
   */
  private void setErrorMessage(String errorMsg)
  {
    log.debug("setErrorMessage(String " + errorMsg + ")");
    FacesContext.getCurrentInstance().addMessage(null,
        new FacesMessage(FacesMessage.SEVERITY_ERROR,
            getResourceBundleString(ALERT) + " " + errorMsg, null));
  }

  private void setSuccessMessage(String successMsg)
  {
    log.debug("setSuccessMessage(String " + successMsg + ")");
    FacesContext.getCurrentInstance().addMessage(null,
        new FacesMessage(FacesMessage.SEVERITY_INFO, successMsg, null));
  }

  private void setInformationMessage(String infoMsg)
  {
	    log.debug("setInformationMessage(String " + infoMsg + ")");
	    FacesContext.getCurrentInstance().addMessage(null,
	        new FacesMessage(infoMsg));
  }

 
  /**
   * Enable privacy message
   * @return
   */
  public boolean getRenderPrivacyAlert()
  {
    return StringUtils.isNotEmpty(ServerConfigurationService.getString(MESSAGECENTER_PRIVACY_TEXT, null));
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
    if (StringUtils.isNotEmpty(servletPath) && servletPath.startsWith("/jsp/main")){
        return true;
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
    	 User user = userDirectoryService.getUser(getUserId());
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
         log.error(e.getMessage(), e);
       }
       
       return authorString;
    }
    
    public String getPlacementId() 
    {
       return StringEscapeUtils.escapeEcmaScript("Main" + toolManager.getCurrentPlacement().getId());
    }

    public boolean isSearchPvtMsgsEmpty()
    {
    	return searchPvtMsgs == null || searchPvtMsgs.isEmpty();
    }

    public boolean isDetailMessagePublishableToFaq() {
        String siteId = getSiteId();

        if (!StringUtils.equalsAny(getMsgNavMode(),
            PrivateMessagesTool.PVTMSG_MODE_SENT, PrivateMessagesTool.PVTMSG_MODE_RECEIVED)) {
          return false;
        }

        boolean forumsToolPresent = false;
        try {
            forumsToolPresent = siteService.getSite(siteId)
                    .getToolForCommonId(DiscussionForumService.FORUMS_TOOL_ID) != null;
        } catch (IdUnusedException e) {
          log.error("Could not find site with id [{}]: {}", siteId, e.toString());
        }

        return forumsToolPresent;
    }

    public void setMsgNavMode(String msgNavMode) {
		this.msgNavMode = msgNavMode;
		this.selectedTags = "";
		this.fromPreview = false;
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
	    log.debug("processActionDeleteChecked()");

		List pvtMsgList = getPvtMsgListToProcess();
		boolean msgSelected = false;
		selectedDeleteItems = new ArrayList();

		for (PrivateMessageDecoratedBean decoMessage : (List<PrivateMessageDecoratedBean>) pvtMsgList) {

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
	    log.debug("processActionMoveCheckedToFolder()");

	    List pvtMsgList = getPvtMsgListToProcess();
	    boolean msgSelected = false;
	    selectedMoveToFolderItems = new ArrayList();

		for (PrivateMessageDecoratedBean decoMessage : (List<PrivateMessageDecoratedBean>) pvtMsgList) {

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

		for (PrivateMessageDecoratedBean decoMessage : (List<PrivateMessageDecoratedBean>) pvtMsgList) {

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
	    this.selectedTags = "";
	    this.fromPreview = false;
	}
	
	@SuppressWarnings("unchecked")
	public String processActionPermissions()
	{
		if(fromPermissions) {
			fromPermissions = false;
			return null;
		}
		fromPermissions = true;
		return PERMISSIONS_PG;
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
	  	final String toolId = toolManager.getCurrentTool().getId();
		switch(toolId){
			case DiscussionForumService.MESSAGE_CENTER_ID:
				eventMessagePrefix = "/messages&Forums/site/";
				break;
			case DiscussionForumService.MESSAGES_TOOL_ID:
				eventMessagePrefix = "/messages/site/";
				break;
			default:
				eventMessagePrefix = "/forums/site/";
				break;
		}

	  	return eventMessagePrefix + toolManager.getCurrentPlacement().getContext() + 
	  				"/" + object.toString() + "/" + sessionManager.getCurrentSessionUserId();
	}

	public String getMobileSession()
	{
		Session session = sessionManager.getCurrentSession();
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
			return siteService.getSite(toolManager.getCurrentPlacement().getContext());
		} catch (IdUnusedException e) {
			log.error(e.getMessage());
		}
		return null;
	}
	
	public List<SelectItem> getNonHiddenGroups(){
		List<SelectItem> nonHiddenGroups = new ArrayList<>();
		nonHiddenGroups.add(new SelectItem(DEFAULT_NON_HIDDEN_GROUP_ID, getResourceBundleString(DEFAULT_NON_HIDDEN_GROUP_TITLE)));
		
		Site currentSite = getCurrentSite();   
		if(currentSite.hasGroups()){
	      
			Collection<Group> groups = currentSite.getGroups();

			sortGroups(groups);

			for (Group currentGroup : (List<Group>) groups) {
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
	  private Collection<Group> sortGroups(Collection<Group> groups) {
		  List<Group> sortGroupsList = new ArrayList<>();

		  sortGroupsList.addAll(groups);
		  final GroupTitleComparator groupComparator = new GroupTitleComparator();
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
		  if(StringUtils.isNotEmpty(groupId)){
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

    private LRS_Statement getStatementForUserReadPvtMsg(String subject) {
    	LRS_Actor student = learningResourceStoreService.getActor(sessionManager.getCurrentSessionUserId());

        String url = ServerConfigurationService.getPortalUrl();
        LRS_Verb verb = new LRS_Verb(SAKAI_VERB.interacted);
        LRS_Object lrsObject = new LRS_Object(url + "/privateMessage", "read-private-message");
        HashMap<String, String> nameMap = new HashMap<>();
        nameMap.put("en-US", "User read a private message");
        lrsObject.setActivityName(nameMap);
        HashMap<String, String> descMap = new HashMap<>();
        descMap.put("en-US", "User read a private message with subject: " + subject);
        lrsObject.setDescription(descMap);
        return new LRS_Statement(student, verb, lrsObject);
    }

    private LRS_Statement getStatementForUserSentPvtMsg(String subject, SAKAI_VERB sakaiVerb) {
        LRS_Actor student = learningResourceStoreService.getActor(sessionManager.getCurrentSessionUserId());
        String url = ServerConfigurationService.getPortalUrl();
        LRS_Verb verb = new LRS_Verb(sakaiVerb);
        LRS_Object lrsObject = new LRS_Object(url + "/privateMessage", "send-private-message");
        HashMap<String, String> nameMap = new HashMap<>();
        nameMap.put("en-US", "User sent a private message");
        lrsObject.setActivityName(nameMap);
        HashMap<String, String> descMap = new HashMap<>();
        descMap.put("en-US", "User sent a private message with subject: " + subject);
        lrsObject.setDescription(descMap);
        return new LRS_Statement(student, verb, lrsObject);
    }

    public String getAttachmentReadableSize(final String attachmentSize) {
      return FileUtils.byteCountToDisplaySize(Long.parseLong(attachmentSize));
    }

    public String getSenderRoleAndGroups() {
      if (totalComposeToList == null) {
        initializeComposeToLists();
      }
      final String userId = detailMsg.getMsg().getAuthorId();
      final String role = authzGroupService.getUserRole(userId, siteService.siteReference(getCurrentSite().getId()));
      final String groups = totalComposeToList.stream()
                    .filter(item -> (item.getGroup() != null && item.getGroup().getMembers().stream()
                            .anyMatch(member -> member.getUserId().equals(userId))))
		            .filter(item -> (item.getGroup() != null && item.getGroup().getMembers().stream()
                            .anyMatch(member -> member.getUserId().equals(getUserId()))))
                    .map(item->item.getGroup().getDescription())
                    .collect(Collectors.joining(", "));
      return "(" + role + ") " + groups;
    }

    public boolean isDisplayDraftRecipientsNotFoundMsg() {
        return drDelegate.isDisplayDraftRecipientsNotFoundMsg();
    }

	public void setComposeSubject(String value) {
		composeSubject = StringUtils.trimToEmpty(value);
	}

	public void setComposeBody(String value) {
		composeBody = StringUtils.trimToEmpty(value);
	}

	public void setReplyToSubject(String value) {
		replyToSubject = StringUtils.trimToEmpty(value);
	}

	public void setReplyToBody(String value) {
		replyToBody = StringUtils.trimToEmpty(value);
	}

	public void setForwardSubject(String value) {
		forwardSubject = StringUtils.trimToEmpty(value);
	}

	public void setForwardBody(String value) {
		forwardBody = StringUtils.trimToEmpty(value);
	}

	public void setReplyToAllSubject(String value) {
		replyToAllSubject = StringUtils.trimToEmpty(value);
	}

	public void setReplyToAllBody(String value) {
		replyToAllBody = StringUtils.trimToEmpty(value);
	}

	private void schedulerMessage(PrivateMessage pMsg, boolean asEmail) {
	    List<MembershipItem> draftRecipients = drDelegate.getDraftRecipients(getSelectedComposeToList(), courseMemberMap);
	    List<MembershipItem> draftBccRecipients = drDelegate.getDraftRecipients(getSelectedComposeBccList(), courseMemberMap);
		prtMsgManager.sendProgamMessage(pMsg, draftRecipients, draftBccRecipients, asEmail);
		PrivateMessageSchedulerService.scheduleDueDateReminder(pMsg.getId());
		manageTagAssociation(pMsg.getId());
	}

	public void setOpenDate(String openDateStr){
		SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		datetimeFormat.setTimeZone(userTimeService.getLocalTimeZone());
		  if (StringUtils.isNotBlank(openDateStr)) {
			  try {
				  String hiddenOpenDate = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("openDateISO8601") != null ? (String)FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("openDateISO8601") : openDateStr;
				  Date openDate = (Date) datetimeFormat.parse(hiddenOpenDate);
				  this.openDate = openDate;
			}catch (ParseException e) {
				log.error("Couldn't convert open date", e);
				this.openDate = null;
			}
		  }else{
			  this.openDate = null;
		  }
	  }

	private void storeDateISO() {
	    String openDateISO8601 = (String)FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("openDateISO8601");
	    if(booleanSchedulerSend && StringUtils.isNotBlank(openDateISO8601)) {
		    this.schedulerSendDateString = openDateISO8601;
		}
	}
}
