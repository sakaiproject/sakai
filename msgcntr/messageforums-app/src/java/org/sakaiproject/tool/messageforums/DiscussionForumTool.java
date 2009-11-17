/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-app/src/java/org/sakaiproject/tool/messageforums/DiscussionForumTool.java $
 * $Id: DiscussionForumTool.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.messageforums;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIData;
import javax.faces.component.UIInput;
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
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.EmailNotification;
import org.sakaiproject.api.app.messageforums.EmailNotificationManager;
import org.sakaiproject.api.app.messageforums.MembershipManager;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.OpenForum;
import org.sakaiproject.api.app.messageforums.PermissionLevel;
import org.sakaiproject.api.app.messageforums.PermissionLevelManager;
import org.sakaiproject.api.app.messageforums.PermissionsMask;
import org.sakaiproject.api.app.messageforums.SynopticMsgcntrManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.cover.SynopticMsgcntrManagerCover;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.app.messageforums.MembershipItem;
import org.sakaiproject.component.app.messageforums.dao.hibernate.util.comparator.ForumBySortIndexAscAndCreatedDateDesc;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CommentDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.InvalidGradeException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.messageforums.ui.DecoratedAttachment;
import org.sakaiproject.tool.messageforums.ui.DiscussionAreaBean;
import org.sakaiproject.tool.messageforums.ui.DiscussionForumBean;
import org.sakaiproject.tool.messageforums.ui.DiscussionMessageBean;
import org.sakaiproject.tool.messageforums.ui.DiscussionTopicBean;
import org.sakaiproject.tool.messageforums.ui.EmailNotificationBean;
import org.sakaiproject.tool.messageforums.ui.PermissionBean;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;

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
  private static final String FORUMS_MAIN = "forumsMain";
  private static final String TEMPLATE_SETTING = "dfTemplateSettings";
  private static final String TEMPLATE_ORGANIZE = "dfTemplateOrganize";
  private static final String WATCH_SETTING = "dfWatchSettings";
  private static final String FORUM_DETAILS = "dfForumDetail";
  private static final String FORUM_SETTING = "dfForumSettings";
  private static final String FORUM_SETTING_REVISE = "dfReviseForumSettings";
  private static final String TOPIC_SETTING = "dfTopicSettings";
  private static final String TOPIC_SETTING_REVISE = "dfReviseTopicSettings";
  private static final String MESSAGE_COMPOSE = "dfCompose";
  private static final String MESSAGE_VIEW = "dfViewMessage";
  private static final String THREAD_VIEW = "dfViewThread";
  private static final String ALL_MESSAGES = "dfAllMessages";
  private static final String SUBJECT_ONLY = "dfSubjectOnly";
  private static final String ENTIRE_MSG = "dfEntireMsg";
  private static final String EXPANDED_VIEW = "dfExpandAllView";
  private static final String THREADED_VIEW = "dfThreadedView";
  private static final String FLAT_VIEW = "dfFlatView";
  private static final String UNREAD_VIEW = "dfUnreadView";
  private static final String GRADE_MESSAGE = "dfMsgGrade";
  private static final String FORUM_STATISTICS = "dfStatisticsList";
  private static final String FORUM_STATISTICS_USER = "dfStatisticsUser";
  private static final String ADD_COMMENT = "dfMsgAddComment";
  private static final String PENDING_MSG_QUEUE = "dfPendingMessages";
  
  private static final String PERMISSION_MODE_TEMPLATE = "template";
  private static final String PERMISSION_MODE_FORUM = "forum";
  private static final String PERMISSION_MODE_TOPIC = "topic";  
  private static final String STATE_INCONSISTENT = "cdfm_state_inconsistent";
  
  private static final String MULTIPLE_WINDOWS = "pvt_multiple_windows";

  private DiscussionForumBean selectedForum;
  private DiscussionTopicBean selectedTopic;
  private DiscussionTopicBean searchResults;
  private DiscussionMessageBean selectedMessage;
  private DiscussionAreaBean template;
  private DiscussionMessageBean selectedThreadHead;
  private List selectedThread = new ArrayList();
  private UIData  forumTable;
  private List groupsUsersList;   
  private List totalGroupsUsersList;
  private List selectedGroupsUsersList;
  private Map courseMemberMap;
  private List permissions;
  private List levels;
  private AreaManager areaManager;
  private int numPendingMessages = 0;
  private boolean refreshPendingMsgs = true;
  
  private static final String TOPIC_ID = "topicId";
  private static final String FORUM_ID = "forumId";
  private static final String MESSAGE_ID = "messageId";
  private static final String REDIRECT_PROCESS_ACTION = "redirectToProcessAction";
  private static final String FROMPAGE = "fromPage";

  private static final String MESSAGECENTER_TOOL_ID = "sakai.messagecenter";
  private static final String FORUMS_TOOL_ID = "sakai.forums";

  private static final String MESSAGECENTER_BUNDLE = "org.sakaiproject.api.app.messagecenter.bundle.Messages";

  private static final String INSUFFICIENT_PRIVILEGES_TO_EDIT_TEMPLATE_SETTINGS = "cdfm_insufficient_privileges";
  private static final String INSUFFICIENT_PRIVILEGES_TO_EDIT_TEMPLATE_ORGANIZE = "cdfm_insufficient_privileges";
  private static final String INSUFFICIENT_PRIVILEAGES_TO="cdfm_insufficient_privileages_to";
  private static final String INSUFFICIENT_PRIVILEGES_REVISE_MESSAGE="cdfm_insufficient_privileges_revise_message";
  private static final String INSUFFICIENT_PRIVILEGES_CHAGNE_FORUM="cdfm_insufficient_privileges_change_forum";
  private static final String INSUFFICIENT_PRIVILEGES_NEW_TOPIC = "cdfm_insufficient_privileges_new_topic";
  private static final String INSUFFICIENT_PRIVILEGES_CREATE_TOPIC="cdfm_insufficient_privileges_create_topic";
  private static final String USER_NOT_ALLOWED_CREATE_FORUM="cdfm_user_not_allowed_create_forum";
  private static final String INSUFFICIENT_PRIVILEGES_TO_DELETE_FORUM="cdfm_insufficient_privileges_delete_forum";
  private static final String SHORT_DESC_TOO_LONG = "cdfm_short_desc_too_long";
  private static final String LAST_REVISE_BY = "cdfm_last_revise_msg"; 
  private static final String LAST_REVISE_ON = "cdfm_last_revise_msg_on";
  private static final String VALID_FORUM_TITLE_WARN = "cdfm_valid_forum_title_warn";
  private static final String VALID_TOPIC_TITLE_WARN = "cdfm_valid_topic_title_warn";
  private static final String INVALID_SELECTED_FORUM ="cdfm_invalid_selected_forum";
  private static final String FORUM_NOT_FOUND = "cdfm_forum_not_found";
  private static final String SELECTED_FORUM_NOT_FOUND =  "cdfm_selected_forum_not_found";
  private static final String FAILED_NEW_TOPIC ="cdfm_failed_new_topic";
  private static final String TOPIC_WITH_ID = "cdfm_topic_with_id";
  private static final String MESSAGE_WITH_ID = "cdfm_message_with_id";
  private static final String NOT_FOUND_WITH_QUOTE = "cdfm_not_found_quote";
  private static final String PARENT_FORUM_NOT_FOUND = "cdfm_parent_forum_not_found";
  private static final String NOT_FOUND_REDIRECT_PAGE = "cdfm_not_found_redirect_page";
  private static final String MESSAGE_REFERENCE_NOT_FOUND = "cdfm_message_reference_not_found";
  private static final String TOPC_REFERENCE_NOT_FOUND = "cdfm_topic_reference_not_found";
  private static final String UNABLE_RETRIEVE_TOPIC = "cdfm_unable_retrieve_topic";
  private static final String PARENT_TOPIC_NOT_FOUND = "cdfm_parent_topic_not_found";
  private static final String FAILED_CREATE_TOPIC = "cdfm_failed_create_topic";
  private static final String FAILED_REND_MESSAGE = "cdfm_failed_rend_message";
  private static final String VIEW_UNDER_CONSTRUCT = "cdfm_view_under_construct";
  private static final String LOST_ASSOCIATE = "cdfm_lost_association";
  private static final String NO_MARKED_READ_MESSAGE = "cdfm_no_message_mark_read";
  private static final String GRADE_SUCCESSFUL = "cdfm_grade_successful";
  private static final String GRADE_GREATER_ZERO = "cdfm_grade_greater_than_zero";
  private static final String GRADE_DECIMAL_WARN = "cdfm_grade_decimal_warn";
  private static final String ALERT = "cdfm_alert";
  private static final String SELECT_ASSIGN = "cdfm_select_assign";
  private static final String INVALID_COMMENT = "cdfm_add_comment_invalid";
  private static final String INSUFFICIENT_PRIVILEGES_TO_ADD_COMMENT = "cdfm_insufficient_privileges_add_comment";
  private static final String MOD_COMMENT_TEXT = "cdfm_moderator_comment_text";
  private static final String NO_MSG_SEL_FOR_APPROVAL = "cdfm_no_message_mark_approved";
  private static final String MSGS_APPROVED = "cdfm_approve_msgs_success";
  private static final String MSGS_DENIED = "cdfm_deny_msgs_success";
  private static final String MSG_REPLY_PREFIX = "cdfm_reply_prefix";
  private static final String NO_GRADE_PTS = "cdfm_no_points_for_grade";
  private static final String TOO_LARGE_GRADE = "cdfm_too_large_grade";
  private static final String NO_ASSGN = "cdfm_no_assign_for_grade";
  private static final String CONFIRM_DELETE_MESSAGE="cdfm_delete_msg";
  private static final String INSUFFICIENT_PRIVILEGES_TO_DELETE = "cdfm_insufficient_privileges_delete_msg";
  
  private static final String FROM_PAGE = "msgForum:mainOrForumOrTopic";
  private String fromPage = null; // keep track of originating page for common functions
  
  private List forums = new ArrayList();
  private List pendingMsgs = new ArrayList();
  
  private String userId;
  
  private boolean showForumLinksInNav = true;

  // compose
  private MessageForumsMessageManager messageManager;
  private String composeTitle;
  private String composeBody;
  private String composeLabel;
  private String searchText = "";
  private String selectedMessageView = ALL_MESSAGES;
  private String selectedMessageShow = SUBJECT_ONLY;
  private String selectedMessageOrganize = "thread"; 
  private String threadAnchorMessageId = null;
  private boolean deleteMsg;
  private boolean displayUnreadOnly;
  private boolean errorSynch = false;
  // attachment
  private ArrayList attachments = new ArrayList();
  private ArrayList prepareRemoveAttach = new ArrayList();
  // private boolean attachCaneled = false;
  // private ArrayList oldAttachments = new ArrayList();
  // private List allAttachments = new ArrayList();
  private boolean threaded = true;
  private boolean expandedView = false;
  private String expanded = "false";
  private boolean orderAsc = true;
  private boolean disableLongDesc = false;
  private boolean isDisplaySearchedMessages;
  private List siteMembers = new ArrayList();
  private String selectedRole;
  private String moderatorComments;
  
  private boolean editMode = true;
  private String permissionMode;
  
  //grading 
  private static final String DEFAULT_GB_ITEM = "Default_0";
  private boolean gradeNotify = false; 
  private List<SelectItem> assignments = new ArrayList<SelectItem>(); 
  private String selectedAssign = DEFAULT_GB_ITEM; 
  private String gradePoint; 
  private String gradeComment; 
  private boolean gradebookExist = false;
  private boolean gradebookExistChecked = false;
  private boolean displayDeniedMsg = false;
  private transient boolean selGBItemRestricted;
  private transient boolean allowedToGradeItem;
  private String gbItemPointsPossible;
  /* There is some funkiness related to the ValueChangeListener used to change the selected gb item on
   * the grading page. The change will process its method and then try to "set" the property with the old score
   * in the input box, overriding the value you just set. gbItemScore and gbItemComment will maintain the correct 
   * values and gradePoint and gradeComment will only be used for updating.
   */
  private String gbItemScore;
  private String gbItemComment;


  /**
   * Dependency Injected
   */
  private DiscussionForumManager forumManager;
  private UIPermissionsManager uiPermissionsManager;
  private MessageForumsTypeManager typeManager;
  private MembershipManager membershipManager;
  private PermissionLevelManager permissionLevelManager; 
  private EmailNotificationManager emailNotificationManager;
  private SynopticMsgcntrManager synopticMsgcntrManager;
  
  
  private Boolean instructor = null;
  private Boolean sectionTA = null;
  private Boolean newForum = null;
  private Boolean displayPendingMsgQueue = null;
  private List siteRoles = null;
  private Boolean forumsTool = null;
  private Boolean messagesandForums = null;
  private List postingOptions = null;
  
  private boolean grade_too_large_make_sure = false;
  
  private int forumClickCount = 0;
  private int topicClickCount = 0;
  
  private String selectedMsgId;

  private int selectedMessageCount = 0;
  private int functionClick = 0;

  
  // email notification options
  private EmailNotificationBean watchSettingsBean;
   

   /**
   * 
   */
  public DiscussionForumTool()
  {
    LOG.debug("DiscussionForumTool()");
    if("true".equalsIgnoreCase(ServerConfigurationService.getString("mc.threadedview")))
    {
    	threaded = true;
    	selectedMessageView = THREADED_VIEW;
    }
    if("true".equalsIgnoreCase(ServerConfigurationService.getString("mc.disableLongDesc")))
    {
    	disableLongDesc = true;
    }
    
    showForumLinksInNav = ServerConfigurationService.getBoolean("mc.showForumLinksInNav", true);
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
    return gotoMain();
  }

  /**
   * @return
   */
  public boolean isInstructor()
  {
    LOG.debug("isInstructor()");
    if (instructor == null)
    {
    	instructor = forumManager.isInstructor();
    }
    return instructor.booleanValue();
  }
  
  /**
   * @return
   */
  public boolean isSectionTA()
  {
    LOG.debug("isSectionTA()");
    if (sectionTA == null)
    {
    	sectionTA = forumManager.isSectionTA();
    }
    return sectionTA.booleanValue();
  }

  /**
   * @return List of SelectItem
   */
  public List getForumSelectItems()
  {
     List f = getForums();
     int num = (f == null) ? 0 : f.size();

     List retSort = new ArrayList();
     for(int i = 1; i <= num; i++) {
        Integer index = new Integer(i);
        retSort.add(new SelectItem(index, index.toString()));
     }
     
     return retSort;
  }

  /**
   * @return List of DiscussionForumBean
   */
  public List getForums()
  {
	  LOG.debug("getForums()");
	  
	     if (forums != null && forums.size() > 0) {
	       return forums;
	     }
	 
	     // query the database for all of the forums that are associated with the current site
	     List<DiscussionForum> tempForums = forumManager.getForumsForMainPage();
	     if (tempForums == null || tempForums.size() < 1) {
	       return null;
	     }
	 
	     // establish some values that we will check multiple times to shave a few processing cycles
	     boolean readFullDescription = "true".equalsIgnoreCase(ServerConfigurationService.getString("mc.defaultLongDescription"));
	 
	     // run through the topics once to get their parent forums, create the decorated topics that will be used later, and
	     // possibly set the message count
	     SortedSet<DiscussionForum> tempSortedForums = new TreeSet<DiscussionForum>(new ForumBySortIndexAscAndCreatedDateDesc());
	     Map<Long, DiscussionTopicBean> topicBeans = new HashMap<Long, DiscussionTopicBean>();
	     Set<Long> topicIdsForCounts = new HashSet<Long>();
	     for (DiscussionForum forum: tempForums) {
	       if (!forum.getDraft()
	           || SecurityService.isSuperUser()
	           || isInstructor()
	           || forum.getCreatedBy().equals(getUserId()))
	       { // this is the start of the big forum if
	 
	         tempSortedForums.add(forum);
	 
	         for (Iterator itor = forum.getTopicsSet().iterator(); itor.hasNext(); ) {
	           DiscussionTopic currTopic = (DiscussionTopic)itor.next();
	 
	           if (currTopic.getDraft().equals(Boolean.FALSE)
	               || isInstructor()
	               || SecurityService.isSuperUser()
	               || currTopic.getCreatedBy().equals(getUserId()))
	           { // this is the start of the big topic if
	             DiscussionTopicBean decoTopic = new DiscussionTopicBean(currTopic, (DiscussionForum)currTopic.getOpenForum(), uiPermissionsManager, forumManager);
	             if (readFullDescription) decoTopic.setReadFullDesciption(true);
	 
	             // set the message count for moderated topics, otherwise it will be set later
	             if (currTopic.getModerated() && !uiPermissionsManager.isModeratePostings(currTopic, (DiscussionForum)currTopic.getOpenForum())) {
	               decoTopic.setTotalNoMessages(forumManager.getTotalViewableMessagesWhenMod(currTopic));
	               decoTopic.setUnreadNoMessages(forumManager.getNumUnreadViewableMessagesWhenMod(currTopic));
	             } else {
	               topicIdsForCounts.add(currTopic.getId());
	             }
	 
	             topicBeans.put(currTopic.getId(), decoTopic);
	           } // end the big topic if
	         }
	       } // end the big forum if
	     }
	 
	     // get the total message count of non-moderated topics and add them to the discussion topic bean and
	     // initialize the unread number of messages to all of them.
	     List<Object[]> topicMessageCounts = forumManager.getMessageCountsForMainPage(topicIdsForCounts);
	     for (Object[] counts: topicMessageCounts) {
	       DiscussionTopicBean decoTopic = topicBeans.get(counts[0]);
	       decoTopic.setTotalNoMessages((Integer)counts[1]);
	       decoTopic.setUnreadNoMessages((Integer)counts[1]);
	     }
	 
	     // get the total read message count for the current user of non-moderated and add them to the discussion
	     // topic bean as the number of unread messages.  I could've combined this with the previous query but
	     // stupid Hibernate (3.x) won't let us properly outer join mapped entitys that do not have a direct
	     // association.  BLURG!  Any topic not in the returned list means the user hasn't read any of the messages
	     // in that topic which is why I set the default unread message count to all the messages in the previous
	     // loop.
	     topicMessageCounts = forumManager.getReadMessageCountsForMainPage(topicIdsForCounts);
	     for (Object[] counts: topicMessageCounts) {
	       DiscussionTopicBean decoTopic = topicBeans.get(counts[0]);
	       decoTopic.setUnreadNoMessages(decoTopic.getTotalNoMessages() - (Integer)counts[1]);
	     }
	 
	     // get the assignments for use later
	     try {
	       assignments = new ArrayList<SelectItem>();
	       assignments.add(new SelectItem(DEFAULT_GB_ITEM, getResourceBundleString(SELECT_ASSIGN)));
	 
	       GradebookService gradebookService = (org.sakaiproject.service.gradebook.shared.GradebookService)
	       ComponentManager.get("org.sakaiproject.service.gradebook.GradebookService");
	       if(getGradebookExist()) {
	         List gradeAssignmentsBeforeFilter = gradebookService.getAssignments(ToolManager.getCurrentPlacement().getContext());
	         for(int i=0; i<gradeAssignmentsBeforeFilter.size(); i++) {
	           Assignment thisAssign = (Assignment) gradeAssignmentsBeforeFilter.get(i);
	           if(!thisAssign.isExternallyMaintained()) {
	             try {
	               assignments.add(new SelectItem(Integer.toString(assignments.size()), thisAssign.getName()));
	             } catch(Exception e) {
	               LOG.error("DiscussionForumTool - processDfMsgGrd:" + e);
	               e.printStackTrace();
	             }
	           }
	         }
	       }
	     } catch(SecurityException se) {
	       LOG.debug("SecurityException caught while getting assignments.", se);
	     } catch(Exception e1) {
	       LOG.error("DiscussionForumTool&processDfMsgGrad:" + e1);
	       e1.printStackTrace();
	     }
	 
	     // now loop through the forums that we found earlier and turn them into forums ready to be displayed to the end user
	     forums = new ArrayList<DiscussionForumBean>();
	     int sortIndex = 1;
	     int unreadMessagesCount = 0;
	     userId = getUserId();

	     for (DiscussionForum forum: tempSortedForums) {
	       // manually set the sort index now that the list is sorted
	       forum.setSortIndex(new Integer(sortIndex));
	       sortIndex++;
	 
	       DiscussionForumBean decoForum = new DiscussionForumBean(forum, uiPermissionsManager, forumManager);
	       if (readFullDescription) decoForum.setReadFullDesciption(true);
	 
	       if (forum.getTopics() != null) {
	         for (Iterator itor = forum.getTopics().iterator(); itor.hasNext(); ) {
	           DiscussionTopic topic = (DiscussionTopic) itor.next();
	           DiscussionTopicBean decoTopic = topicBeans.get(topic.getId());
	           if (decoTopic != null) decoForum.addTopic(decoTopic);
	         }   	
	         
	         //itterate over all topics in the decoratedForum to add the unread message
	         //counts to update the sypnoptic tool

	         for (Iterator iterator = decoForum.getTopics().iterator(); iterator.hasNext();) {
	        	 DiscussionTopicBean dTopicBean = (DiscussionTopicBean) iterator.next();
	        	 //if user can read this forum topic, count the messages as well
	        	 if(uiPermissionsManager.isRead(dTopicBean.getTopic(), decoForum.getForum(), userId)){        			
	        		 unreadMessagesCount += dTopicBean.getUnreadNoMessages();
	        	 }
	         }

	       }
	 
	       decoForum.setGradeAssign(DEFAULT_GB_ITEM);
	       for(int i=0; i<assignments.size(); i++) {
	         if (assignments.get(i).getLabel().equals(forum.getDefaultAssignName())) {
	           decoForum.setGradeAssign(new Integer(i).toString());
	           break;
	         }
	       }
	       forums.add(decoForum);
	     }
	 
	     //update synotpic info for forums only:
		 setForumSynopticInfoHelper(userId, getSiteId(), unreadMessagesCount, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
		   
	     return forums;
  }
  
  public void setForumSynopticInfoHelper(String userId, String siteId,
		  int unreadMessagesCount, int numOfAttempts) {
	  try {
		  // update synotpic info for forums only:
		  getSynopticMsgcntrManager().setForumSynopticInfoHelper(userId, siteId, unreadMessagesCount);
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
			  .println("DiscussionForumTool: setForumSynopticInfoHelper: HibernateOptimisticLockingFailureException no more retries left");
			  holfe.printStackTrace();
		  } else {
			  System.out
			  .println("DiscussionForumTool: setForumSynopticInfoHelper: HibernateOptimisticLockingFailureException: attempts left: "
					  + numOfAttempts);
			  setForumSynopticInfoHelper(userId, siteId, 
					  unreadMessagesCount, numOfAttempts);
		  }
	  }

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
    return FORUM_STATISTICS;
  }
  
  /**
   * @return
   */
  public String processActionTemplateSettings()
  {
    LOG.debug("processActionTemplateSettings()");
    
    setEditMode(true);
    setPermissionMode(PERMISSION_MODE_TEMPLATE);
    template = new DiscussionAreaBean(areaManager.getDiscusionArea());
               
    if(!isInstructor())
    {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_TO_EDIT_TEMPLATE_SETTINGS));
      return gotoMain();
    }
    return TEMPLATE_SETTING;
  }

  /**
   * @return
   */
  public String processActionTemplateOrganize()
  {
    LOG.debug("processActionTemplateOrganize()");
    
    setEditMode(false);
    setPermissionMode(PERMISSION_MODE_TEMPLATE);
               
    if(!isInstructor())
    {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_TO_EDIT_TEMPLATE_ORGANIZE));
      return gotoMain();
    }
    return TEMPLATE_ORGANIZE;
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
  
  /*/**
   * @return
   */
  /*public String processActionReviseTemplateSettings()
  {
  	if (LOG.isDebugEnabled()){
      LOG.debug("processActionReviseTemplateSettings()");
  	}
    
  	setEditMode(true); 
  	setPermissionMode(PERMISSION_MODE_TEMPLATE);
    return TEMPLATE_SETTING;
  }*/

  /**
   * @return
   */
  public String processActionSaveTemplateSettings()
  {
    LOG.debug("processActionSaveTemplateSettings()");
    if(!isInstructor())
    {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_TO_EDIT_TEMPLATE_SETTINGS));
      return gotoMain();
    }    
    
    setObjectPermissions(template.getArea());
    areaManager.saveArea(template.getArea());
    
    return gotoMain();
  }


  public String processActionCancelTemplateSettings()
  {
    LOG.debug("processActionTemplateSettings()");
    // SAK-14073 -- Cleanout values after cancelling.
    FacesContext context = FacesContext.getCurrentInstance();
    UIInput component = (UIInput) context.getViewRoot().findComponent("revise:moderated");
    if (component != null) {
      component.setSubmittedValue(null);
    }
  return gotoMain();
  }

  /**
   * @return
   */
  public String processActionSaveTemplateOrganization()
  {
    LOG.debug("processActionSaveTemplateOrganization()");
    if(!isInstructor())
    {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_TO_EDIT_TEMPLATE_ORGANIZE));
      return gotoMain();
    }
    
    for(Iterator i = forums.iterator(); i.hasNext(); ) {
       DiscussionForumBean forum = (DiscussionForumBean)i.next();
       
       // because there is no straight up save forum function we need to retain the draft status
       if(forum.getForum().getDraft().booleanValue())
          forumManager.saveForumAsDraft(forum.getForum());
       else
          forumManager.saveForum(forum.getForum());
    }
    
    //reload the forums so they change position in the list
    forums = null;
    
	return gotoMain();
  }

  /**
   * @return
   */
  public String processActionRestoreDefaultTemplate()
  {
    LOG.debug("processActionRestoreDefaultTemplate()");
    if(!isInstructor())
    {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_TO_EDIT_TEMPLATE_SETTINGS));
      return gotoMain();
    }
    
    Area area = null;
    if ((area = areaManager.getDiscusionArea()) != null){
    	area.setMembershipItemSet(new HashSet());
    	area.setModerated(Boolean.FALSE);
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
    if (newForum == null){
    	newForum = uiPermissionsManager.isNewForum();
    }
    return newForum.booleanValue();
  }

  /**
   * Display Individual forum
   * 
   * @return
   */
  public String processActionDisplayForum()
  {
    LOG.debug("processDisplayForum()");
    forumClickCount++;
    if (getDecoratedForum() == null)
    {
      LOG.error("Forum not found");
      return gotoMain();
    }
    return FORUM_DETAILS;
  }

  
  /**
   * Action for the delete option present the main forums page
   * @return
   */
  
  public String processActionDeleteForumMainConfirm()
  {

	  LOG.debug("processForumMainConfirm()");

	  String forumId = getExternalParameterByKey(FORUM_ID);
	  DiscussionForum forum = forumManager.getForumById(new Long(forumId));
	  selectedForum = new DiscussionForumBean(forum, uiPermissionsManager, forumManager);

	  selectedForum.setMarkForDeletion(true);
	  return FORUM_SETTING;
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
      return gotoMain();
    }
//  TODO:
    if(!uiPermissionsManager.isChangeSettings(selectedForum.getForum()))
    {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_TO_DELETE_FORUM));
      return gotoMain();
    }
    selectedForum.setMarkForDeletion(true);
    return FORUM_SETTING;
  }

  /**
   * @return
   */
  public String processActionDeleteForum()
  {
    if (uiPermissionsManager == null)
    {
      throw new IllegalStateException("uiPermissionsManager == null");
    }
    if (selectedForum == null)
    {
      throw new IllegalStateException("selectedForum == null");
    }
    if(!uiPermissionsManager.isChangeSettings(selectedForum.getForum()))
    {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEAGES_TO));
      return gotoMain();
   }
    HashMap<String, Integer> beforeChangeHM = null;    
    Long forumId = selectedForum.getForum().getId();
    beforeChangeHM = SynopticMsgcntrManagerCover.getUserToNewMessagesForForumMap(getSiteId(), forumId, null);
    
   
	  forumManager.deleteForum(selectedForum.getForum());

	  if(beforeChangeHM != null)
		  updateSynopticMessagesForForumComparingOldMessagesCount(getSiteId(), forumId, null, beforeChangeHM, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);

	  reset();
	  return gotoMain();
  }
  
  public void updateSynopticMessagesForForumComparingOldMessagesCount(String siteId, Long forumId, Long topicId, HashMap<String, Integer> beforeChangeHM, int numOfAttempts) {
	  try {
		  // update synotpic info for forums only:
		  SynopticMsgcntrManagerCover
		  .updateSynopticMessagesForForumComparingOldMessagesCount(
				  siteId, forumId, topicId, beforeChangeHM);
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
			  .println("DiscussionForumTool: updateSynopticMessagesForForumComparingOldMessagesCount: HibernateOptimisticLockingFailureException no more retries left");
			  holfe.printStackTrace();
		  } else {
			  System.out
			  .println("DiscussionForumTool: updateSynopticMessagesForForumComparingOldMessagesCount: HibernateOptimisticLockingFailureException: attempts left: "
					  + numOfAttempts);
			  updateSynopticMessagesForForumComparingOldMessagesCount(siteId,
					  forumId, topicId, beforeChangeHM, numOfAttempts);
		  }
	  }
  }

  /**
   * @return
   */
  public String processActionNewForum()
  {
    LOG.debug("processActionNewForum()");
    forumClickCount = 0;
    topicClickCount = 0;
    
    setEditMode(true);
    setPermissionMode(PERMISSION_MODE_FORUM);
        
    if (getNewForum())
    {
      DiscussionForum forum = forumManager.createForum();
      forum.setModerated(areaManager.getDiscusionArea().getModerated()); // default to template setting
      forum.setAutoMarkThreadsRead(areaManager.getDiscusionArea().getAutoMarkThreadsRead()); // default to template setting
      selectedForum = null;
      selectedForum = new DiscussionForumBean(forum, uiPermissionsManager, forumManager);
      if("true".equalsIgnoreCase(ServerConfigurationService.getString("mc.defaultLongDescription")))
      {
      	selectedForum.setReadFullDesciption(true);
      }

      setNewForumBeanAssign();
      
      return FORUM_SETTING_REVISE;
    }
    else
    {
      setErrorMessage(getResourceBundleString(USER_NOT_ALLOWED_CREATE_FORUM));
      return gotoMain();
    }
  }

  /**
   * @return
   */
  public String processActionForumSettings()
  {
    LOG.debug("processForumSettings()");
    forumClickCount = 0;
    topicClickCount = 0;
    setEditMode(true);
    setPermissionMode(PERMISSION_MODE_FORUM);
    
    String forumId = getExternalParameterByKey(FORUM_ID);
    if ((forumId) == null)
    {
      setErrorMessage(getResourceBundleString(INVALID_SELECTED_FORUM));
      return gotoMain();
    }
    DiscussionForum forum = forumManager.getForumById(Long.valueOf(forumId));
    if (forum == null)
    {
      setErrorMessage(getResourceBundleString(FORUM_NOT_FOUND));
      return gotoMain();
    }
    
    if(!uiPermissionsManager.isChangeSettings(forum))
    {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_CHAGNE_FORUM));
      return gotoMain();
    }
    
    List attachList = forum.getAttachments();
    if (attachList != null)
    {
      for (int i = 0; i < attachList.size(); i++)
      {
        attachments.add(new DecoratedAttachment((Attachment)attachList.get(i)));
      }
    }
    
    selectedForum = new DiscussionForumBean(forum, uiPermissionsManager, forumManager);
    if("true".equalsIgnoreCase(ServerConfigurationService.getString("mc.defaultLongDescription")))
    {
    	selectedForum.setReadFullDesciption(true);
    }

    setForumBeanAssign();
    setFromMainOrForumOrTopic();
    
    return FORUM_SETTING_REVISE;

  }

  /**
   * @return
   */
  /*public String processActionReviseForumSettings()
  {
    LOG.debug("processActionReviseForumSettings()");    
    setEditMode(true);
    setPermissionMode(PERMISSION_MODE_FORUM);
    if ((selectedForum) == null)
    {
      setErrorMessage(getResourceBundleString(FORUM_NOT_FOUND));
      return gotoMain();
    }
    if(!uiPermissionsManager.isChangeSettings(selectedForum.getForum()))
    {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_CHAGNE_FORUM));
      return gotoMain();
    }
    List attachList = selectedForum.getForum().getAttachments();
    if (attachList != null)
    {
      for (int i = 0; i < attachList.size(); i++)
      {
        attachments.add(new DecoratedAttachment((Attachment)attachList.get(i)));
      }
    }
    
    setFromMainOrForumOrTopic();

    return FORUM_SETTING_REVISE; //
  }*/

  /**
   * @return
   */
  public String processActionSaveForumAndAddTopic()
  {
    LOG.debug("processActionSaveForumAndAddTopic()");
    if(forumClickCount != 0 || topicClickCount != 0) {
    	setErrorMessage(getResourceBundleString(MULTIPLE_WINDOWS , new Object[] {ServerConfigurationService.getString("ui.service")}));
    	return FORUM_SETTING_REVISE;
    }
    
    if(selectedForum !=null && selectedForum.getForum()!=null &&
    		(selectedForum.getForum().getShortDescription()!=null) && 
    		(selectedForum.getForum().getShortDescription().length() > 255))
    {
    	setErrorMessage(getResourceBundleString(SHORT_DESC_TOO_LONG));
    	return null;
    }

    if(selectedForum!=null && selectedForum.getForum()!=null && 
        (selectedForum.getForum().getTitle()==null 
          ||selectedForum.getForum().getTitle().trim().length()<1  ))
    {
      setErrorMessage(getResourceBundleString(VALID_FORUM_TITLE_WARN));
      return FORUM_SETTING_REVISE;
    }
    if (selectedForum == null)
			throw new IllegalStateException("selectedForum == null");
    if(!uiPermissionsManager.isChangeSettings(selectedForum.getForum()))
    {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_CHAGNE_FORUM));
      return gotoMain();
    }   
    
    DiscussionForum forum = saveForumSettings(false);    
    if(!uiPermissionsManager.isNewTopic(selectedForum.getForum()))
    {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_CREATE_TOPIC));
      return gotoMain();
    }    
    selectedTopic = createTopic(forum.getId());
    if (selectedTopic == null)
    {
      setErrorMessage(getResourceBundleString(FAILED_NEW_TOPIC));
      attachments.clear();
      prepareRemoveAttach.clear();
      return gotoMain();
    }
    setPermissionMode(PERMISSION_MODE_TOPIC);
    attachments.clear();
    prepareRemoveAttach.clear();
    
    setPermissionMode(PERMISSION_MODE_TOPIC);
    
    return TOPIC_SETTING_REVISE;
  }

  /**
   * @return
   */
  public String processActionSaveForumSettings()
  {
    LOG.debug("processActionSaveForumSettings()");
    if(forumClickCount != 0 || topicClickCount != 0) {
    	setErrorMessage(getResourceBundleString(MULTIPLE_WINDOWS , new Object[] {ServerConfigurationService.getString("ui.service")}));
    	return FORUM_SETTING_REVISE;
    }
    if(selectedForum !=null && selectedForum.getForum()!=null &&
    		(selectedForum.getForum().getShortDescription()!=null) && 
    		(selectedForum.getForum().getShortDescription().length() > 255))
    {
    	setErrorMessage(getResourceBundleString(SHORT_DESC_TOO_LONG));
    	return null;
    }
    
    if (selectedForum == null)
		throw new IllegalStateException("selectedForum == null");
    if(!uiPermissionsManager.isChangeSettings(selectedForum.getForum()))
    {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_CHAGNE_FORUM));
      return gotoMain();
    }
    if(selectedForum!=null && selectedForum.getForum()!=null && 
        (selectedForum.getForum().getTitle()==null 
          ||selectedForum.getForum().getTitle().trim().length()<1  ))
    {
      setErrorMessage(getResourceBundleString(VALID_FORUM_TITLE_WARN));
      return FORUM_SETTING_REVISE;
    }    
    saveForumSettings(false);
    
    //reset();
    //return MAIN;
    return processReturnToOriginatingPage();
  }

  /**
   * @return
   */
  public String processActionSaveForumAsDraft()
  {
    LOG.debug("processActionSaveForumAsDraft()");
    if(forumClickCount != 0 || topicClickCount != 0) {
    	setErrorMessage(getResourceBundleString(MULTIPLE_WINDOWS , new Object[] {ServerConfigurationService.getString("ui.service")}));
    	return FORUM_SETTING_REVISE;
    }
    
    if(selectedForum !=null && selectedForum.getForum()!=null &&
    		(selectedForum.getForum().getShortDescription()!=null) && 
    		(selectedForum.getForum().getShortDescription().length() > 255))
    {
    	setErrorMessage(getResourceBundleString(SHORT_DESC_TOO_LONG));
    	return null;
    }

    if (selectedForum == null)
		throw new IllegalStateException("selectedForum == null");
    if(!uiPermissionsManager.isChangeSettings(selectedForum.getForum()))
    {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_CHAGNE_FORUM));
      return gotoMain();
    }
    if(selectedForum!=null && selectedForum.getForum()!=null && 
        (selectedForum.getForum().getTitle()==null 
          ||selectedForum.getForum().getTitle().trim().length()<1  ))
    {
      setErrorMessage(getResourceBundleString(VALID_FORUM_TITLE_WARN));
      return FORUM_SETTING_REVISE;
    }    
    saveForumSettings(true);
    
    //reset();
    //return MAIN;
    return processReturnToOriginatingPage();
  }

  private DiscussionForum saveForumSettings(boolean draft)
  {
    LOG.debug("saveForumSettings(boolean " + draft + ")");
    
    if (selectedForum == null)
    {
      setErrorMessage(getResourceBundleString(SELECTED_FORUM_NOT_FOUND));
      return null;
    }
  
    DiscussionForum forum = selectedForum.getForum();
    if (forum == null)
    {
      setErrorMessage(getResourceBundleString(FORUM_NOT_FOUND));
      return null;
    }
    
    boolean isNew = forum.getId() == null;
    HashMap<String, Integer> beforeChangeHM = null;
    if(!isNew){
    	beforeChangeHM = SynopticMsgcntrManagerCover.getUserToNewMessagesForForumMap(getSiteId(), forum.getId(), null);
    }
    
    StringBuilder alertMsg = new StringBuilder();
    forum.setExtendedDescription(FormattedText.processFormattedText(forum.getExtendedDescription(), alertMsg));
    forum.setTitle(FormattedText.processFormattedText(forum.getTitle(), alertMsg));
    forum.setShortDescription(FormattedText.processFormattedText(forum.getShortDescription(), alertMsg));
    
    if (forum.getExtendedDescription().equals("<br/>"))
	{
		forum.setExtendedDescription("");
	}
    
    saveForumSelectedAssignment(forum);
    saveForumAttach(forum);  
    setObjectPermissions(forum);
    if (draft)
      forumManager.saveForumAsDraft(forum);
    else
      forumManager.saveForum(forum);
    //forumManager.saveForumControlPermissions(forum, forumControlPermissions);
    //forumManager.saveForumMessagePermissions(forum, forumMessagePermissions);
    if (isNew)
    {
      String forumUuid = forum.getUuid();
      forum = null;
      forum = forumManager.getForumByUuid(forumUuid);
    }else{   
    	//anytime a forum settings change, we should update synoptic info for forums
    	//since permissions could have changed.
    	if(beforeChangeHM != null)
    		updateSynopticMessagesForForumComparingOldMessagesCount(getSiteId(), forum.getId(), null, beforeChangeHM, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
    }
    
    return forum;
  }

  
  


  /**
   * @return Returns the selectedTopic.
   */
  public DiscussionTopicBean getSelectedTopic()
  {
  	if(selectedTopic == null)
  	{
			LOG.debug("no topic is selected in getSelectedTopic.");
  		return null;
  	}
  	if (!selectedTopic.isSorted()) 
  	{
  		rearrageTopicMsgsThreaded();
  		setMessageBeanPreNextStatus();
  		selectedTopic.setSorted(true);
  	}
  	return selectedTopic;
  }
  
  /**
   * @return Returns the selected Area
   */
  public DiscussionAreaBean getTemplate()
  {	
    return template;
  }

  
  /**
   * @return
   */
  public String processActionNewTopic()
  {   
    LOG.debug("processActionNewTopic()");
    topicClickCount = 0 ;
    forumClickCount = 0;
    setEditMode(true);
    setPermissionMode(PERMISSION_MODE_TOPIC);
         
    selectedTopic = createTopic();
    setNewTopicBeanAssign();
    if (selectedTopic == null)
    {
      setErrorMessage(getResourceBundleString(FAILED_NEW_TOPIC));
      attachments.clear();
      prepareRemoveAttach.clear();
      return gotoMain();
    }
    if(!uiPermissionsManager.isNewTopic(selectedForum.getForum()))
    {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_CREATE_TOPIC));
      return gotoMain();
    }
    attachments.clear();
    prepareRemoveAttach.clear();
    setFromMainOrForumOrTopic();

    return TOPIC_SETTING_REVISE;
  }

  /**
   * @return
   */
  public String processActionReviseTopicSettings()
  {
    LOG.debug("processActionReviseTopicSettings()");
    topicClickCount = 0;
    forumClickCount = 0;
    setPermissionMode(PERMISSION_MODE_TOPIC);
    setEditMode(true);
        
    if(selectedTopic == null)
    {
			LOG.debug("no topic is selected in processActionReviseTopicSettings.");
    	return gotoMain();
    }
    DiscussionTopic topic = selectedTopic.getTopic();

    if (topic == null)
    {
      topic = forumManager.getTopicById(Long.valueOf(
          getExternalParameterByKey(TOPIC_ID)));
    }
    if (topic == null)
    {
      setErrorMessage(getResourceBundleString(TOPIC_WITH_ID) + getExternalParameterByKey(TOPIC_ID)
          + getResourceBundleString(NOT_FOUND_WITH_QUOTE));
      return gotoMain();
    }
  
    setSelectedForumForCurrentTopic(topic);
    selectedTopic = new DiscussionTopicBean(topic, selectedForum.getForum(),
        uiPermissionsManager, forumManager);
    if("true".equalsIgnoreCase(ServerConfigurationService.getString("mc.defaultLongDescription")))
    {
    	selectedTopic.setReadFullDesciption(true);
    }

    setTopicBeanAssign();
    
    if(!uiPermissionsManager.isChangeSettings(selectedTopic.getTopic(),selectedForum.getForum()))
    {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_NEW_TOPIC));
      return gotoMain();
    }
    List attachList = selectedTopic.getTopic().getAttachments();
    if (attachList != null)
    {
      for (int i = 0; i < attachList.size(); i++)
      {
        attachments.add(new DecoratedAttachment((Attachment)attachList.get(i)));
      }
    }  
    
    setFromMainOrForumOrTopic();
    
    return TOPIC_SETTING_REVISE;
  }

  /**
   * @return
   */
  public String processActionSaveTopicAndAddTopic()
  {
    LOG.debug("processActionSaveTopicAndAddTopic()");
    if(topicClickCount != 0 || forumClickCount != 0) {
    	setErrorMessage(getResourceBundleString(MULTIPLE_WINDOWS , new Object[] {ServerConfigurationService.getString("ui.service")}));
    	return TOPIC_SETTING_REVISE;
    }
    if(selectedTopic!=null && selectedTopic.getTopic()!=null &&
    		(selectedTopic.getTopic().getShortDescription()!=null) && 
    		(selectedTopic.getTopic().getShortDescription().length() > 255))
    {
    	setErrorMessage(getResourceBundleString(SHORT_DESC_TOO_LONG));
    	return null;
    }    
    
    setPermissionMode(PERMISSION_MODE_TOPIC);
    if(selectedTopic!=null && selectedTopic.getTopic()!=null && 
        (selectedTopic.getTopic().getTitle()==null 
          ||selectedTopic.getTopic().getTitle().trim().length()<1  ))
    {
      setErrorMessage(getResourceBundleString(VALID_TOPIC_TITLE_WARN));
      return TOPIC_SETTING_REVISE;
    }
    
    // if the topic is not moderated (and already exists), all of the pending messages must be approved
    if (selectedTopic != null && selectedTopic.getTopic() != null &&
    		!selectedTopic.isTopicModerated() && selectedTopic.getTopic().getId() != null)
    {
    	forumManager.approveAllPendingMessages(selectedTopic.getTopic().getId());
    }
    
    saveTopicSettings(false);    
    Long forumId = selectedForum.getForum().getId();
    if (forumId == null)
    {
      setErrorMessage(getResourceBundleString(PARENT_FORUM_NOT_FOUND));
      return gotoMain();
    }
    selectedTopic = null;
    selectedTopic = createTopic(forumId);
    if (selectedTopic == null)
    {
      setErrorMessage(getResourceBundleString(FAILED_NEW_TOPIC));
      attachments.clear();
      prepareRemoveAttach.clear();

      return gotoMain();
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
    if(topicClickCount != 0 || forumClickCount != 0) {
    	setErrorMessage(getResourceBundleString(MULTIPLE_WINDOWS , new Object[] {ServerConfigurationService.getString("ui.service")}));
    	return TOPIC_SETTING_REVISE;
    }
    
    if(selectedTopic!=null && selectedTopic.getTopic()!=null &&
    		(selectedTopic.getTopic().getShortDescription()!=null) && 
    		(selectedTopic.getTopic().getShortDescription().length() > 255))
    {
    	setErrorMessage(getResourceBundleString(SHORT_DESC_TOO_LONG));
    	return null;
    }
    
    setPermissionMode(PERMISSION_MODE_TOPIC);
    if(selectedTopic!=null && selectedTopic.getTopic()!=null && 
        (selectedTopic.getTopic().getTitle()==null 
          ||selectedTopic.getTopic().getTitle().trim().length()<1  ))
    {
      setErrorMessage(getResourceBundleString(VALID_TOPIC_TITLE_WARN));
      return TOPIC_SETTING_REVISE;
    }
	  
    // if the topic is not moderated, all of the messages must be approved
    if (selectedTopic != null && selectedTopic.getTopic().getId() != null &&
    		!selectedTopic.isTopicModerated())
    {
    	forumManager.approveAllPendingMessages(selectedTopic.getTopic().getId());
    }
    
    boolean updateSynopticCounts = false;
    if(selectedForum != null && selectedTopic != null &&
    		selectedForum.getForum().getDraft()){
    	//due to the logic in M/F, when a topic is saved and not a draft, and the
    	//forum is a draft, then the topic turns the forum as not a draft
    	updateSynopticCounts = true;
    }
    
    HashMap<String, Integer> beforeChangeHM = null;
    if(updateSynopticCounts){    	    
    	Long forumId = selectedForum.getForum().getId();
    	beforeChangeHM = SynopticMsgcntrManagerCover.getUserToNewMessagesForForumMap(getSiteId(), forumId, null); 	
    }

    saveTopicSettings(false);  

    if(updateSynopticCounts){
    	if(beforeChangeHM != null){
    		Long forumId = selectedForum.getForum().getId();
    		updateSynopticMessagesForForumComparingOldMessagesCount(getSiteId(), forumId, null, beforeChangeHM, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
    	}
    }
    
    return processReturnToOriginatingPage();
    //reset();
    //return MAIN;
  }

  /**
   * @return
   */
  public String processActionSaveTopicAsDraft()
  {
    LOG.debug("processActionSaveTopicAsDraft()");
    if(topicClickCount != 0 || forumClickCount != 0) {
    	setErrorMessage(getResourceBundleString(MULTIPLE_WINDOWS , new Object[] {ServerConfigurationService.getString("ui.service")}));
    	return TOPIC_SETTING_REVISE;
    }
    if(selectedTopic!=null && selectedTopic.getTopic()!=null &&
    		(selectedTopic.getTopic().getShortDescription()!=null) && 
    		(selectedTopic.getTopic().getShortDescription().length() > 255))
    {
    	setErrorMessage(getResourceBundleString(SHORT_DESC_TOO_LONG));
    	return null;
    }
    
    setPermissionMode(PERMISSION_MODE_TOPIC);
    if(selectedTopic!=null && selectedTopic.getTopic()!=null && 
        (selectedTopic.getTopic().getTitle()==null 
          ||selectedTopic.getTopic().getTitle().trim().length()<1  ))
    {
      setErrorMessage(getResourceBundleString(VALID_TOPIC_TITLE_WARN));
      return TOPIC_SETTING_REVISE;
    }
    if (selectedTopic == null)
		throw new IllegalStateException("selectedTopic == null");
    if (selectedForum == null)
		throw new IllegalStateException("selectedForum == null");
    if(!uiPermissionsManager.isChangeSettings(selectedTopic.getTopic(),selectedForum.getForum()))
    {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_NEW_TOPIC));
      return gotoMain();
    }
    saveTopicSettings(true);    
    //reset();
    //return MAIN;
    
    return processReturnToOriginatingPage();
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
	boolean isNew = topic.getId() == null;
        HashMap<String, Integer> beforeChangeHM = null;
        if(!isNew){
		beforeChangeHM = SynopticMsgcntrManagerCover.getUserToNewMessagesForForumMap(getSiteId(), topic.getBaseForum().getId(), topic.getId());
        }

    	StringBuilder alertMsg = new StringBuilder();
    	topic.setTitle(FormattedText.processFormattedText(topic.getTitle(), alertMsg));
    	topic.setShortDescription(FormattedText.processFormattedText(topic.getShortDescription(), alertMsg));
    	topic.setExtendedDescription(FormattedText.processFormattedText(topic.getExtendedDescription(), alertMsg));
    	
    	if (topic.getExtendedDescription().equals("<br/>"))
    	{
    		topic.setExtendedDescription("");
    	}
    	
        topic.setBaseForum(selectedForum.getForum());
        if(topic.getCreatedBy()==null&&this.forumManager.getAnonRole()==true){
        	topic.setCreatedBy(".anon");
        }
        if(topic.getModifiedBy()==null&&this.forumManager.getAnonRole()==true){
        	topic.setModifiedBy(".anon");
        }
        saveTopicSelectedAssignment(topic);
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

	//anytime a forum settings change, we should update synoptic info for forums
        //since permissions could have changed.
        if(!isNew){
        	if(beforeChangeHM != null)
			updateSynopticMessagesForForumComparingOldMessagesCount(getSiteId(), topic.getBaseForum().getId(), topic.getId(), beforeChangeHM, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
        }        
        //forumManager
        //    .saveTopicControlPermissions(topic, topicControlPermissions);
        //forumManager
        //    .saveTopicMessagePermissions(topic, topicMessagePermissions);
      }
    }
    return gotoMain();
  }

   
  /**
   * @return
   */
  public String processActionDeleteTopicMainConfirm()
  {
	  {
		  LOG.debug("processActionTopicSettings()");

		  DiscussionTopic topic = null;
		  if(getExternalParameterByKey(TOPIC_ID) != "" && getExternalParameterByKey(TOPIC_ID) != null){
			  topic = (DiscussionTopic) forumManager.getTopicByIdWithAttachments(new Long(getExternalParameterByKey(TOPIC_ID)));
		  } else if(selectedTopic != null) {
			  topic = selectedTopic.getTopic();
		  }
		  if (topic == null)
		  {
			  return gotoMain();
		  }
		  setSelectedForumForCurrentTopic(topic);
		  if(!uiPermissionsManager.isChangeSettings(topic,selectedForum.getForum()))
		  {
			  setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_NEW_TOPIC));
			  return gotoMain();
		  }
		  selectedTopic = new DiscussionTopicBean(topic, selectedForum.getForum(),uiPermissionsManager, forumManager);
		
		  selectedTopic.setMarkForDeletion(true);
		    return TOPIC_SETTING;
	  }
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
      return gotoMain();
    }
    if(!uiPermissionsManager.isChangeSettings(selectedTopic.getTopic(),selectedForum.getForum()))
    {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_NEW_TOPIC));
      return gotoMain();
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
      return gotoMain();
    }
    if(!uiPermissionsManager.isChangeSettings(selectedTopic.getTopic(),selectedForum.getForum()))
    {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_NEW_TOPIC));
      return gotoMain();
    }
  
    HashMap<String, Integer> beforeChangeHM = null;    
    Long forumId = selectedTopic.getTopic().getBaseForum().getId();
    Long topicId = selectedTopic.getTopic().getId();
    beforeChangeHM = SynopticMsgcntrManagerCover.getUserToNewMessagesForForumMap(getSiteId(), forumId, topicId);
    
    forumManager.deleteTopic(selectedTopic.getTopic());
    
    if(beforeChangeHM != null)
    	updateSynopticMessagesForForumComparingOldMessagesCount(getSiteId(), forumId, topicId, beforeChangeHM, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
    
    
    reset();
    return gotoMain();
  }

  /**
   * @return
   */
  public String processActionTopicSettings()
  {
    LOG.debug("processActionTopicSettings()");
    
    topicClickCount = 0;
    forumClickCount = 0;
    
    setEditMode(true);
    setPermissionMode(PERMISSION_MODE_TOPIC);
    DiscussionTopic topic = null;
    if(getExternalParameterByKey(TOPIC_ID) != null && !"".equals(getExternalParameterByKey(TOPIC_ID))){
	    topic = (DiscussionTopic) forumManager
	        .getTopicByIdWithAttachments(Long.valueOf(
	            getExternalParameterByKey(TOPIC_ID)));
    } else if(selectedTopic != null) {
    	topic = selectedTopic.getTopic();
    }
    if (topic == null)
    {
      return gotoMain();
    }
    setSelectedForumForCurrentTopic(topic);
    if(!uiPermissionsManager.isChangeSettings(topic,selectedForum.getForum()))
    {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_NEW_TOPIC));
      return gotoMain();
    }
    selectedTopic = new DiscussionTopicBean(topic, selectedForum.getForum(),
        uiPermissionsManager, forumManager);
    if("true".equalsIgnoreCase(ServerConfigurationService.getString("mc.defaultLongDescription")))
    {
    	selectedTopic.setReadFullDesciption(true);
    }
    
    List attachList = selectedTopic.getTopic().getAttachments();
    if (attachList != null)
    {
      for (int i = 0; i < attachList.size(); i++)
      {
        attachments.add(new DecoratedAttachment((Attachment)attachList.get(i)));
      }
    }  
    
    
    setTopicBeanAssign();
    setFromMainOrForumOrTopic();
    
    //return TOPIC_SETTING;
    return TOPIC_SETTING_REVISE;
  }

  public String processActionToggleDisplayForumExtendedDescription()
  {
    LOG.debug("processActionToggleDisplayForumExtendedDescription()");
    String redirectTo = getExternalParameterByKey(REDIRECT_PROCESS_ACTION);
    if (redirectTo == null)
    {
      setErrorMessage(getResourceBundleString(NOT_FOUND_REDIRECT_PAGE));
      return gotoMain();
    }
  
    if ("displayHome".equals(redirectTo))
    {
      displayHomeWithExtendedForumDescription();
      return gotoMain();
    }
    if ("processActionDisplayForum".equals(redirectTo))
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
    return gotoMain();
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
      setErrorMessage(getResourceBundleString(NOT_FOUND_REDIRECT_PAGE));
      return gotoMain();
    }
    if ("displayHome".equals(redirectTo))
    {
      return displayHomeWithExtendedTopicDescription();
    }
    if ("processActionDisplayTopic".equals(redirectTo))
    {
    	if(selectedTopic == null)
    	{
 				LOG.debug("no topic is selected in processActionToggleDisplayExtendedDescription.");
    		return gotoMain();
    	}
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
    if ("processActionDisplayMessage".equals(redirectTo))
    {
    	if(selectedTopic == null)
    	{
 				LOG.debug("no topic is selected in processActionToggleDisplayExtendedDescription.");
    		return gotoMain();
    	}
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
    if ("processActionGradeMessage".equals(redirectTo))
    {
    	if(selectedTopic == null)
    	{
 				LOG.debug("no topic is selected in processActionToggleDisplayExtendedDescription.");
    		return gotoMain();
    	}
      if (selectedTopic.isReadFullDesciption())
      {
        selectedTopic.setReadFullDesciption(false);
      }
      else
      {
        selectedTopic.setReadFullDesciption(true);
      }
      return GRADE_MESSAGE;
    }

    return gotoMain();

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

  public  String formatStringByRemoveLastEmptyLine(String inputStr)
	{		
		final String pattern1 = "<br/>";
		final String pattern2 = "<br>";
		if (inputStr==null || "".equals(inputStr))
			return null;
		String tmpStr=inputStr.trim();
		while(tmpStr.endsWith(pattern1)||tmpStr.endsWith(pattern2))
		{
			if(tmpStr.endsWith(pattern1))
			{
				tmpStr= tmpStr.substring(0, tmpStr.length()-pattern1.length());
				tmpStr=tmpStr.trim();
			}
			if(tmpStr.endsWith(pattern2))
			{
				tmpStr= tmpStr.substring(0, tmpStr.length()-pattern2.length());
				tmpStr=tmpStr.trim();
			}
		}
		return tmpStr;
		
	}
  /**
   * @return Returns the selectedMessage.
   */
  public DiscussionMessageBean getSelectedMessage()
  {
	  if((selectedMessage!=null)&&(!"".equals(selectedMessage.getMessage().getBody())))
	  {
		 String messageBody= selectedMessage.getMessage().getBody();
		 String messageBodyWithoutLastEmptyLine=formatStringByRemoveLastEmptyLine(messageBody);
		 selectedMessage.getMessage().setBody(messageBodyWithoutLastEmptyLine); 		 
	  }
    return selectedMessage;
  }
  
  /**
   * @return Returns the selectedThread.
   */
  public DiscussionMessageBean getSelectedThreadHead()
  {
	  return selectedThreadHead;
  }
  
  public List getPFSelectedThread() 
  {
	List results = new ArrayList();
	List messages = getSelectedThread();
	
	for (Iterator iter = messages.iterator(); iter.hasNext();)
	{
		DiscussionMessageBean message = (DiscussionMessageBean) iter.next();
		
		if (! message.getDeleted())
		{
			results.add(message);
		}
	}
	
	return results;
  }
  /**
   * @return Returns an array of Messages for the current selected thread
   */
  public List getSelectedThread()
  {
	  List returnArray = new ArrayList();
	  returnArray = selectedThread;
	  if(displayUnreadOnly){
		  ArrayList tempmes = new ArrayList();
		  for(int i = returnArray.size()-1; i >= 0; i--){
			  if(!((DiscussionMessageBean)returnArray.get(i)).isRead()){
				  tempmes.add(returnArray.get(i));
			  }
		  }
		  returnArray = tempmes;
	  }
	  if (!orderAsc){
		  ArrayList tempmes = new ArrayList();
		  for(int i = returnArray.size()-1; i >= 0; i--){
			  tempmes.add(returnArray.get(i));
		  }
		  return tempmes;
	  } else
		  return returnArray;
  }
  
  /**
   * @return
   */
  public String processActionDisplayFlatView()
  {
	  return FLAT_VIEW;
  }
  
  /**
   * @return
   */
  public String processActionDisplayThreadedView()
  {
	  return ALL_MESSAGES;
  }
  
  public String processActionGetDisplayThread()
  {
  		if(selectedTopic == null)
  		{
  			LOG.debug("no topic is selected in processActionGetDisplayThread.");
  			return gotoMain();
  		}
	  	selectedTopic = getDecoratedTopic(selectedTopic.getTopic());
	  	
	  	setTopicBeanAssign();
	  	getSelectedTopic();
	    
	    List msgsList = selectedTopic.getMessages();
	    
	    if (msgsList != null && !msgsList.isEmpty())
	    	msgsList = filterModeratedMessages(msgsList, selectedTopic.getTopic(), (DiscussionForum) selectedTopic.getTopic().getBaseForum());
	    
	    List orderedList = new ArrayList();
	    selectedThread = new ArrayList();
	    
	    Boolean foundHead = false;
	    Boolean foundAfterHead = false;
	    
	    //determine to make sure that selectedThreadHead does exist!
	    if(selectedThreadHead == null){
	    	return MAIN;
	    }
	    
	    for(int i=0; i<msgsList.size(); i++){
	    	if(((DiscussionMessageBean)msgsList.get(i)).getMessage().getId().equals(selectedThreadHead.getMessage().getId())){
	    		((DiscussionMessageBean) msgsList.get(i)).setDepth(0);
	    		selectedThread.add((DiscussionMessageBean)msgsList.get(i));
	    		foundHead = true;
	    	}
	    	else if(((DiscussionMessageBean)msgsList.get(i)).getMessage().getInReplyTo() == null && foundHead && !foundAfterHead) {
	    		selectedThreadHead.setHasNextThread(true);
	    		selectedThreadHead.setNextThreadId(((DiscussionMessageBean)msgsList.get(i)).getMessage().getId());
	    		foundAfterHead = true;
	    	} 
	    	else if (((DiscussionMessageBean)msgsList.get(i)).getMessage().getInReplyTo() == null && !foundHead) {
	    		selectedThreadHead.setHasPreThread(true);
	    		selectedThreadHead.setPreThreadId(((DiscussionMessageBean)msgsList.get(i)).getMessage().getId());
	    	}
	    }
	    formatMessagesByRemovelastEmptyLines(msgsList);
	    recursiveGetThreadedMsgsFromList(msgsList, orderedList, selectedThreadHead);
	    selectedThread.addAll(orderedList);
	    
            // now process the complete list of messages in the selected thread to possibly flag as read
            // if this topic is flagged to autoMarkThreadsRead, mark each message in the thread as read
            if (selectedTopic.getTopic().getAutoMarkThreadsRead()) {
                for (int i = 0; i < selectedThread.size(); i++) {
                    messageManager.markMessageReadForUser(selectedTopic.getTopic().getId(), ((DiscussionMessageBean)selectedThread.get(i)).getMessage().getId(), true);
                    ((DiscussionMessageBean)selectedThread.get(i)).setRead(Boolean.TRUE);
                }
            }


	    return THREAD_VIEW;
  }
/**
 *  remove last empty lines of every massage in thread view
 */ 
 public void formatMessagesByRemovelastEmptyLines(List messages)
 {
		if(messages==null) return;		
		Iterator it=messages.iterator();
		while(it.hasNext())
		{
			 DiscussionMessageBean messageBean=	(DiscussionMessageBean) it.next();		
			 if((messageBean!=null)&&(!"".equals(messageBean.getMessage().getBody())))
			  {
				 String messageBody= messageBean.getMessage().getBody();
				 String messageBodyWithoutLastEmptyLine=formatStringByRemoveLastEmptyLine(messageBody);
				 messageBean.getMessage().setBody(messageBodyWithoutLastEmptyLine); 		 
			  }
		}
		 return ;
 }
  /**
   * @return
   */
  public String processActionDisplayThread()
  {
	    LOG.debug("processActionDisplayThread()");
	    
	    selectedMessageCount ++;

	    threadAnchorMessageId = null;
	    String threadId = getExternalParameterByKey(MESSAGE_ID);
	    String topicId = getExternalParameterByKey(TOPIC_ID);
	    if ("".equals(threadId))
	    {
	      setErrorMessage(getResourceBundleString(MESSAGE_REFERENCE_NOT_FOUND));
	      return gotoMain();
	    }
	    if ("".equals(topicId))
	    {
	      setErrorMessage(getResourceBundleString(TOPC_REFERENCE_NOT_FOUND));
	      return gotoMain();
	    }
	    // Message message=forumManager.getMessageById(Long.valueOf(messageId));
	    Message threadMessage = messageManager.getMessageByIdWithAttachments(Long.valueOf(
	        threadId));
	    if (threadMessage == null)
	    {
	      setErrorMessage(getResourceBundleString(MESSAGE_WITH_ID) + threadId + getResourceBundleString(NOT_FOUND_WITH_QUOTE));
	      return gotoMain();
	    }
	    //threadMessage = messageManager.getMessageByIdWithAttachments(threadMessage.getId());
	    selectedThreadHead = new DiscussionMessageBean(threadMessage, messageManager);
	    //make sure we have the thread head of depth 0
	    while(selectedThreadHead.getMessage().getInReplyTo() != null){
	    	threadMessage = messageManager.getMessageByIdWithAttachments(selectedThreadHead.getMessage().getInReplyTo().getId());
	    	selectedThreadHead = new DiscussionMessageBean(
	    			threadMessage, messageManager);
	    }
	    DiscussionTopic topic=forumManager.getTopicById(Long.valueOf(topicId));
	    selectedMessage = selectedThreadHead;
	    setSelectedForumForCurrentTopic(topic);
	    selectedTopic = new DiscussionTopicBean(topic, selectedForum.getForum(),
	        uiPermissionsManager, forumManager);
	    if(topic == null || selectedTopic == null)
	    {
	    	LOG.debug("topic or selectedTopic is null in processActionDisplayThread.");
	    	return gotoMain();
	    }
	    if("true".equalsIgnoreCase(ServerConfigurationService.getString("mc.defaultLongDescription")))
	    {
	    	selectedTopic.setReadFullDesciption(true);
	    }
	    setTopicBeanAssign();
	    String currentForumId = getExternalParameterByKey(FORUM_ID);
	    if (currentForumId != null && (!"".equals(currentForumId.trim()))
	        && (!"null".equals(currentForumId.trim())))
	    {
	      DiscussionForum forum = forumManager
	          .getForumById(Long.valueOf(currentForumId));
	      selectedForum = new DiscussionForumBean(forum, uiPermissionsManager, forumManager);
	      setForumBeanAssign();
	      selectedTopic.getTopic().setBaseForum(forum);
	    }
	    // don't need this here b/c done in processActionGetDisplayThread();
	    // selectedTopic = getDecoratedTopic(topic);
	    
	    return processActionGetDisplayThread();	  
  }
  
  /**
   * @return
   */
  public String processActionDisplayThreadAnchor()
  {
	  String returnString = processActionDisplayThread();
	  threadAnchorMessageId = getExternalParameterByKey(MESSAGE_ID);
	  return returnString;
  }

  /**
   * @return
   */
  public String processActionDisplayMessage()
  {
    LOG.debug("processActionDisplayMessage()");

   selectedMessageCount ++;

    String messageId = getExternalParameterByKey(MESSAGE_ID);
    String topicId = getExternalParameterByKey(TOPIC_ID);
    if (messageId == null)
    {
      setErrorMessage(getResourceBundleString(MESSAGE_REFERENCE_NOT_FOUND));
      return gotoMain();
    }
    if (topicId == null)
    {
      setErrorMessage(getResourceBundleString(TOPC_REFERENCE_NOT_FOUND));
      return gotoMain();
    }
    // Message message=forumManager.getMessageById(Long.valueOf(messageId));
    messageManager.markMessageReadForUser(Long.valueOf(topicId),
            Long.valueOf(messageId), true);
    Message message = messageManager.getMessageByIdWithAttachments(Long.valueOf(
        messageId));

    if (message == null)
    {
      setErrorMessage(getResourceBundleString(MESSAGE_WITH_ID) + messageId + getResourceBundleString(NOT_FOUND_WITH_QUOTE));
      return gotoMain();
    }

    selectedMessage = new DiscussionMessageBean(message, messageManager);
    DiscussionTopic topic=forumManager.getTopicById(Long.valueOf(topicId));
    setSelectedForumForCurrentTopic(topic);
    selectedTopic = new DiscussionTopicBean(topic, selectedForum.getForum(),
        uiPermissionsManager, forumManager);
    if(topic == null || selectedTopic == null)
    {
    	LOG.debug("topic or selectedTopic is null in processActionDisplayMessage.");
    	return gotoMain();
    }
    if("true".equalsIgnoreCase(ServerConfigurationService.getString("mc.defaultLongDescription")))
    {
    	selectedTopic.setReadFullDesciption(true);
    }
    setTopicBeanAssign();
    String currentForumId = getExternalParameterByKey(FORUM_ID);
    if (currentForumId != null && (!"".equals(currentForumId.trim()))
        && (!"null".equals(currentForumId.trim())))
    {
      DiscussionForum forum = forumManager
          .getForumById(Long.valueOf(currentForumId));
      selectedForum = new DiscussionForumBean(forum, uiPermissionsManager, forumManager);
      setForumBeanAssign();
      selectedTopic.getTopic().setBaseForum(forum);
    }
    selectedTopic = getDecoratedTopic(topic);
    setTopicBeanAssign();
    getSelectedTopic();
    //get thread from message
    getThreadFromMessage();
    refreshSelectedMessageSettings(message);
    // selectedTopic= new DiscussionTopicBean(message.getTopic());
    return MESSAGE_VIEW;
  }
  
  
  public void getThreadFromMessage()
  {
	    Message mes = selectedMessage.getMessage();
	    String messageId = mes.getId().toString();
	    while( mes.getInReplyTo() != null) {
	    	mes = messageManager.getMessageById(mes.getInReplyTo().getId());
	    }
	    selectedThreadHead = new DiscussionMessageBean(mes, messageManager);
	    
	    if(selectedTopic == null)
	    {
	    	LOG.debug("selectedTopic is null in getThreadFromMessage.");
	    	return;
	    }
	    
	    List tempMsgs = selectedTopic.getMessages();
	    Boolean foundHead = false;
	    Boolean foundAfterHead = false;
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
	    			foundHead = true;
	    		}
	    		else if(((DiscussionMessageBean)tempMsgs.get(i)).getMessage().getInReplyTo() == null && foundHead && !foundAfterHead) {
	        		selectedThreadHead.setHasNextThread(true);
	        		selectedThreadHead.setNextThreadId(((DiscussionMessageBean)tempMsgs.get(i)).getMessage().getId());
	        		foundAfterHead = true;
	        	} 
	        	else if (((DiscussionMessageBean)tempMsgs.get(i)).getMessage().getInReplyTo() == null && !foundHead) {
	        		selectedThreadHead.setHasPreThread(true);
	        		selectedThreadHead.setPreThreadId(((DiscussionMessageBean)tempMsgs.get(i)).getMessage().getId());
	        	}
	    	}
	    }
	    refreshSelectedMessageSettings(selectedMessage.getMessage());
  }

  
  public String processDisplayPreviousMsg()
  {
    if(selectedTopic == null)
    {
    	LOG.debug("selectedTopic is null in processDisplayPreviousMsg.");
    	return null;
    }
  	
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
	    
	    refreshSelectedMessageSettings(message);  
    }
    
    return null;
  }

  public String processDfDisplayNextMsg()
  {
    if(selectedTopic == null)
    {
    	LOG.debug("selectedTopic is null in processDfDisplayNextMsg.");
    	return null;
    }
  	
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
	    
	    refreshSelectedMessageSettings(message);  
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
   * @return List of DiscussionTopicBean
   */
  private DiscussionForumBean getDecoratedForum(DiscussionForum forum)
  {
	  if (LOG.isDebugEnabled())
	  {
		  LOG.debug("getDecoratedForum(DiscussionForum" + forum + ")");
	  }
	  forum = forumManager.getForumByIdWithTopicsAttachmentsAndMessages(forum.getId());
	  DiscussionForumBean decoForum = new DiscussionForumBean(forum,
			  uiPermissionsManager, forumManager);
	  if("true".equalsIgnoreCase(ServerConfigurationService.getString("mc.defaultLongDescription")))
	  {
		  decoForum.setReadFullDesciption(true);
	  }
	  List temp_topics = forum.getTopics();
	  if (temp_topics == null)
	  {
		  return decoForum;
	  }

	  // to store all of the messages associated with the topics
	  List msgIds = new ArrayList();
	  for (Iterator topicIter = temp_topics.iterator(); topicIter.hasNext();) {
		  DiscussionTopic topic = (DiscussionTopic) topicIter.next();
		  if(topic != null)
		  {
			  List msgList = topic.getMessages();
			  if(msgList != null)
			  {
				  for(int j=0; j<msgList.size(); j++)
				  {
					  Message tempMsg = (Message)msgList.get(j);
					  if(tempMsg != null && !tempMsg.getDraft().booleanValue() && !tempMsg.getDeleted())
					  {
						  msgIds.add(tempMsg.getId());
					  }
				  }
			  }
		  }
	  }

	  Map msgIdReadStatusMap = forumManager.getReadStatusForMessagesWithId(msgIds, getUserId());

	  Iterator iter = temp_topics.iterator();
	  while (iter.hasNext())
	  {
		  DiscussionTopic topic = (DiscussionTopic) iter.next();
		  if (topic == null)
				continue;
//		  TODO: put this logic in database layer
		  if (topic != null && topic.getDraft().equals(Boolean.FALSE)
				  ||isInstructor()
				  ||SecurityService.isSuperUser()
				  ||forumManager.isTopicOwner(topic))
		  { 

			  DiscussionTopicBean decoTopic = new DiscussionTopicBean(topic, forum,
					  uiPermissionsManager, forumManager);
			  if("true".equalsIgnoreCase(ServerConfigurationService.getString("mc.defaultLongDescription")))
			  {
				  decoTopic.setReadFullDesciption(true);
			  }

			  List topicMsgs = topic.getMessages();
			  if (topicMsgs == null || topicMsgs.size() == 0) {
				  decoTopic.setTotalNoMessages(0);
				  decoTopic.setUnreadNoMessages(0);
			  } else if (!topic.getModerated().booleanValue() || uiPermissionsManager.isModeratePostings(topic, forum)) {
				  int totalMsgs = 0;
				  int totalUnread = 0;
				  for (Iterator msgIter = topicMsgs.iterator(); msgIter.hasNext();) {
					  Message message = (Message) msgIter.next();
					  Boolean readStatus = (Boolean)msgIdReadStatusMap.get(message.getId());
					  if (readStatus != null) {
						  totalMsgs++;
						  if (!readStatus.booleanValue()) {
							  totalUnread++;
						  }
					  }
				  }

				  decoTopic.setTotalNoMessages(totalMsgs);
				  decoTopic.setUnreadNoMessages(totalUnread);

			  } else {  // topic is moderated
				  decoTopic.setTotalNoMessages(forumManager.getTotalViewableMessagesWhenMod(topic));
				  decoTopic.setUnreadNoMessages(forumManager.getNumUnreadViewableMessagesWhenMod(topic));
			  }

			  decoForum.addTopic(decoTopic);
		  }

	  }
	  return decoForum;
  }

  private DiscussionForumBean getDecoratedForumWithPersistentForumAndTopics(DiscussionForum forum, Map msgIdReadStatusMap)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getDecoratedForum(DiscussionForum" + forum + ")");
    }
    DiscussionForumBean decoForum = new DiscussionForumBean(forum,
        uiPermissionsManager, forumManager);
    if("true".equalsIgnoreCase(ServerConfigurationService.getString("mc.defaultLongDescription")))
    {
    	decoForum.setReadFullDesciption(true);
    }
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
      if (topic.getDraft().equals(Boolean.FALSE)
              ||isInstructor()
              ||SecurityService.isSuperUser()
              ||forumManager.isTopicOwner(topic))
      { 
        if (topic != null)
        {
          DiscussionTopicBean decoTopic = new DiscussionTopicBean(topic, forum,
              uiPermissionsManager, forumManager);
          if("true".equalsIgnoreCase(ServerConfigurationService.getString("mc.defaultLongDescription")))
          {
          	decoTopic.setReadFullDesciption(true);
          }
          
          List topicMsgs = topic.getMessages();
          if (topicMsgs == null || topicMsgs.size() == 0) {
        	  decoTopic.setTotalNoMessages(0);
        	  decoTopic.setUnreadNoMessages(0);
          } else if (!topic.getModerated().booleanValue() || uiPermissionsManager.isModeratePostings(topic, forum)) {
        	  int totalMsgs = 0;
        	  int totalUnread = 0;
        	  for (Iterator msgIter = topicMsgs.iterator(); msgIter.hasNext();) {
        		  Message message = (Message) msgIter.next();
        		  Boolean readStatus = (Boolean)msgIdReadStatusMap.get(message.getId());
        		  if (readStatus != null) {
        			  totalMsgs++;
        			  if (!readStatus.booleanValue()) {
        				  totalUnread++;
        			  }
        		  }
        	  }
        	  
        	  decoTopic.setTotalNoMessages(totalMsgs);
        	  decoTopic.setUnreadNoMessages(totalUnread);
          } else {
        	  decoTopic.setTotalNoMessages(forumManager.getTotalViewableMessagesWhenMod(topic));
          	  decoTopic.setUnreadNoMessages(forumManager.getNumUnreadViewableMessagesWhenMod(topic));
          }
          
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
      DiscussionForum forum = forumManager.getForumById(Long.valueOf(forumId));
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
                          Long.valueOf(
                              getExternalParameterByKey("forumId_displayExtended"))))
              {
                decoForumBean.setReadFullDesciption(true);
              }
              // if this topic is selected to display hide extended desciption
              if (getExternalParameterByKey("forumId_hideExtended") != null
                  && getExternalParameterByKey("forumId_hideExtended").trim()
                      .length() > 0
                  && decoForumBean.getForum().getId().equals(
                      Long.valueOf(
                          getExternalParameterByKey("forumId_hideExtended"))))
              {
                decoForumBean.setReadFullDesciption(false);
              }
             
          
        }
      }

    }
    return gotoMain();
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
                          Long.valueOf(
                              getExternalParameterByKey("topicId_displayExtended"))))
              {
                decoTopicBean.setReadFullDesciption(true);
              }
              // if this topic is selected to display hide extended desciption
              if (getExternalParameterByKey("topicId_hideExtended") != null
                  && getExternalParameterByKey("topicId_hideExtended").trim()
                      .length() > 0
                  && decoTopicBean.getTopic().getId().equals(
                      Long.valueOf(
                          getExternalParameterByKey("topicId_hideExtended"))))
              {
                decoTopicBean.setReadFullDesciption(false);
              }
            }
          }
        }
      }

    }
    return gotoMain();
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
    if("true".equalsIgnoreCase(ServerConfigurationService.getString("mc.defaultLongDescription")))
    {
    	decoTopic.setReadFullDesciption(true);
    }

    boolean hasNextTopic = forumManager.hasNextTopic(topic);
    boolean hasPreviousTopic = forumManager.hasPreviousTopic(topic);
    decoTopic.setHasNextTopic(hasNextTopic);
    decoTopic.setHasPreviousTopic(hasPreviousTopic);
    if (hasNextTopic)
    {
      DiscussionTopic nextTopic= forumManager.getNextTopic(topic);
      decoTopic.setNextTopicId(nextTopic.getId());
    }
    if (hasPreviousTopic)
    {    
        decoTopic.setPreviousTopicId(forumManager.getPreviousTopic(topic).getId());
    }

    List temp_messages = forumManager.getTopicByIdWithMessagesAndAttachments(topic.getId())
        .getMessages();
    if (temp_messages == null || temp_messages.size() < 1)
    {
      decoTopic.setTotalNoMessages(0);
      decoTopic.setUnreadNoMessages(0);
      return decoTopic;
    }
    
    List msgIdList = new ArrayList();
    for (Iterator msgIter = temp_messages.iterator(); msgIter.hasNext();) {
    	Message msg = (Message) msgIter.next();
    	if(msg != null && !msg.getDraft().booleanValue() && !msg.getDeleted()) {
    		msgIdList.add(msg.getId());
    	}
    }
    
    // retrieve read status for all of the messages in this topic
    Map messageReadStatusMap=null;
    if(getUserId()!=null){
     messageReadStatusMap = forumManager.getReadStatusForMessagesWithId(msgIdList, getUserId());
    }else if(getUserId()==null&&this.forumManager.getAnonRole()==true){
    	messageReadStatusMap = forumManager.getReadStatusForMessagesWithId(msgIdList, ".anon");
    }
    
    // set # read/unread msgs on topic level
    if (!topic.getModerated().booleanValue() || uiPermissionsManager.isModeratePostings(topic, selectedForum.getForum())) {
    	int totalMsgs = 0;
    	int totalUnread = 0;
    	for (Iterator msgIter = msgIdList.iterator(); msgIter.hasNext();) {
    		Long msgId = (Long) msgIter.next();
    		Boolean readStatus = (Boolean)messageReadStatusMap.get(msgId);
    		if (readStatus != null) {
    			totalMsgs++;
    			if (!readStatus.booleanValue()) {
    				totalUnread++;
    			}
    		}
    	}

    	decoTopic.setTotalNoMessages(totalMsgs);
    	decoTopic.setUnreadNoMessages(totalUnread);

    } else {  // topic is moderated
    	decoTopic.setTotalNoMessages(forumManager.getTotalViewableMessagesWhenMod(topic));
    	decoTopic.setUnreadNoMessages(forumManager.getNumUnreadViewableMessagesWhenMod(topic));
    }

    Iterator iter = temp_messages.iterator();
    
    final boolean isRead = decoTopic.getIsRead();
    final boolean isNewResponse = decoTopic.getIsNewResponse();
    
    boolean decoTopicGetIsDeleteAny = decoTopic.getIsDeleteAny();
    boolean decoTopicGetIsDeleteOwn = decoTopic.getIsDeleteOwn();
    boolean decoTopicGetIsReviseAny = decoTopic.getIsReviseAny();
    boolean decoTopicGetIsReviseOwn = decoTopic.getIsReviseOwn();
    while (iter.hasNext())
    {
      Message message = (Message) iter.next();
      if (topic != null)
      {
        if (message != null)
        {
          DiscussionMessageBean decoMsg = new DiscussionMessageBean(message,
              messageManager);
          if(isRead || (isNewResponse && decoMsg.getIsOwn()))
          {
        	Boolean readStatus = (Boolean) messageReadStatusMap.get(message.getId());
        	if (readStatus != null) {
        		decoMsg.setRead(readStatus.booleanValue());
        	} else {
        		decoMsg.setRead(messageManager.isMessageReadForUser(topic.getId(),
        				message.getId()));
        	}
        	boolean isOwn=false;
        	if(getUserId()!=null){
          	 isOwn = decoMsg.getMessage().getCreatedBy().equals(getUserId());
        	}
        	else if(getUserId()==null&&this.forumManager.getAnonRole()==true){
        		 isOwn = decoMsg.getMessage().getCreatedBy().equals(".anon");
        	}
          	decoMsg.setRevise(decoTopicGetIsReviseAny 
          			|| (decoTopicGetIsReviseOwn && isOwn));
          	decoMsg.setUserCanDelete(decoTopicGetIsDeleteAny || (isOwn && decoTopicGetIsDeleteOwn));
      		LOG.debug("decoMsg.setUserCanEmail()");
      		LOG.debug("isSectionTA()" + isSectionTA());
          	decoMsg.setUserCanEmail(isInstructor() || isSectionTA());
          	decoTopic.addMessage(decoMsg);
          }
        }

      }
    }
    return decoTopic;
  }

  private Boolean resetTopicById(String externalTopicId)
  {
	  String topicId = null;
	    //threaded = true;
	    selectedTopic = null;
	    try
	    {
	      topicId = getExternalParameterByKey(externalTopicId);

	      if (topicId != null && topicId.trim().length() > 0)
	      {
	        DiscussionTopic topic = null;
	        try
	        {
	          Long.parseLong(topicId);
	          topic = forumManager.getTopicById(Long.valueOf(topicId));
	        }
	        catch (NumberFormatException e)
	        {
	          LOG.error(e.getMessage(), e);
	          setErrorMessage(getResourceBundleString(UNABLE_RETRIEVE_TOPIC));
	          return false;
	        }

	        setSelectedForumForCurrentTopic(topic);
	        selectedTopic = getDecoratedTopic(topic);
	      }
	      else
	      {
	        LOG.error("Topic with id '" + externalTopicId + "' not found");
	        setErrorMessage(getResourceBundleString(TOPIC_WITH_ID) + externalTopicId + getResourceBundleString(NOT_FOUND_WITH_QUOTE));
	        return false;
	      }
	    }
	    catch (Exception e)
	    {
	      LOG.error(e.getMessage(), e);
	      setErrorMessage(e.getMessage());
	      return false;
	    }
	    return true;
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
    topicClickCount++;
    if(resetTopicById(externalTopicId)){
    	return ALL_MESSAGES;
    } else {
    	return gotoMain();
    }
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
    assignments.clear();
    refreshPendingMsgs = true;
  
  }

  /**
   * @return newly created topic
   */
  private DiscussionTopicBean createTopic()
  {
    String forumId = getExternalParameterByKey(FORUM_ID);
    if (forumId == null)
    {
      setErrorMessage(getResourceBundleString(PARENT_TOPIC_NOT_FOUND));
      return null;
    }
    return createTopic(Long.valueOf(forumId));
  }

  /**
   * @param forumID
   * @return
   */
  private DiscussionTopicBean createTopic(Long forumId)
  {
    if (forumId == null)
    {
    	setErrorMessage(getResourceBundleString(PARENT_TOPIC_NOT_FOUND));
      return null;
    }
    DiscussionForum forum = forumManager.getForumById(forumId);
    if (forum == null)
    {
    	setErrorMessage(getResourceBundleString(PARENT_TOPIC_NOT_FOUND));
      return null;
    }
    selectedForum = new DiscussionForumBean(forum, uiPermissionsManager, forumManager);
    if("true".equalsIgnoreCase(ServerConfigurationService.getString("mc.defaultLongDescription")))
    {
    	selectedForum.setReadFullDesciption(true);
    }

    setForumBeanAssign();
    
    DiscussionTopic topic = forumManager.createTopic(forum);
    if (topic == null)
    {
      setErrorMessage(getResourceBundleString(FAILED_CREATE_TOPIC));
      return null;
    }
    selectedTopic = new DiscussionTopicBean(topic, forum, uiPermissionsManager, forumManager);
    if("true".equalsIgnoreCase(ServerConfigurationService.getString("mc.defaultLongDescription")))
    {
    	selectedTopic.setReadFullDesciption(true);
    }
    
    selectedTopic.setModerated(selectedForum.getModerated()); // default to parent forum's setting
    selectedTopic.setAutoMarkThreadsRead(selectedForum.getAutoMarkThreadsRead()); // default to parent forum's setting

    setNewTopicBeanAssign();
    
    DiscussionTopicBean thisDTB = new DiscussionTopicBean(topic, forum, uiPermissionsManager, forumManager);
    if("true".equalsIgnoreCase(ServerConfigurationService.getString("mc.defaultLongDescription")))
    {
    	thisDTB.setReadFullDesciption(true);
    }

    setNewTopicBeanAssign(selectedForum, thisDTB);
    return thisDTB;
    //return new DiscussionTopicBean(topic, forum, uiPermissionsManager, forumManager);
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
      		//thisAttach.setAttachmentUrl(ref.getUrl());
      		thisAttach.setAttachmentUrl("/url");
      		
      		attachments.add(new DecoratedAttachment(thisAttach));
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

    if ((attachId != null) && (!"".equals(attachId)) && attachments != null)
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
	LOG.debug("processDfMsgPost()");
    Message dMsg = constructMessage();

    if(selectedTopic == null)
    {
    	LOG.debug("selectedTopic is null in processDfMsgPost()");
    	return gotoMain();
    }
    forumManager.saveMessage(dMsg);
    DiscussionTopic dSelectedTopic = (DiscussionTopic) forumManager.getTopicWithAttachmentsById(selectedTopic.getTopic().getId());
    setSelectedForumForCurrentTopic(dSelectedTopic);
    selectedTopic.setTopic(dSelectedTopic);
    selectedTopic.getTopic().setBaseForum(selectedForum.getForum());
    //selectedTopic.addMessage(new DiscussionMessageBean(dMsg, messageManager));
    selectedTopic.insertMessage(new DiscussionMessageBean(dMsg, messageManager));

    selectedTopic.getTopic().addMessage(dMsg);
    
    /** mark message creator as having read the message */
    //update synopticLite tool information:
    incrementSynopticToolInfo(dMsg, true);
    messageManager.markMessageReadForUser(selectedTopic.getTopic().getId(), dMsg.getId(), true);        

    this.composeBody = null;
    this.composeLabel = null;
    this.composeTitle = null;

    this.attachments.clear();

    // refresh page with unread status     
    selectedTopic = getDecoratedTopic(selectedTopic.getTopic());
    sendEmailNotification(dMsg,new DiscussionMessageBean(dMsg, messageManager));
    return ALL_MESSAGES;
  }
  
  /**
   * if updateCurrentUser is set to true, then update the current user
   * even if he is not in the recipients list
   * 
   * @param newMessage
   * @param updateCurrentUser
   */

  public void incrementSynopticToolInfo(Message newMessage, boolean updateCurrentUser){
	  Set<String> recipients = getRecipients(newMessage);

	  String siteId = getSiteId();
	  String currentUser = getUserId();
	  
	  //if updateCurrentUser is set to true, then update the current user
	  //even if he is not in the recipients list, this is done b/c the current
	  //user has a new message (their own) and it is quickly marked as read 
	  //(so the current users count is decremented)
	  if(updateCurrentUser && !recipients.contains(currentUser)){
		  recipients.add(currentUser);
	  }

	  for (String user : recipients) {
		  if(updateCurrentUser || (!updateCurrentUser && !currentUser.equals(user))){
			  incrementForumSynopticToolInfo(user, siteId, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
		  }
	  }
  }
  
  public void incrementForumSynopticToolInfo(String userId, String siteId, int numOfAttempts){
	  try {
		  getSynopticMsgcntrManager().incrementForumSynopticToolInfo(userId, siteId);
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
						.println("DiscussionForumTool: incrementForumSynopticToolInfo: HibernateOptimisticLockingFailureException no more retries left");
				holfe.printStackTrace();
			} else {
				System.out
						.println("DiscussionForumTool: incrementForumSynopticToolInfo: HibernateOptimisticLockingFailureException: attempts left: "
								+ numOfAttempts);
				incrementForumSynopticToolInfo(userId, siteId, numOfAttempts);
			}
		}
  }
  
  public Set<String> getRecipients(Message newMessage){
	    Set<String> usersAbleToReadMessage;
		String msgPostedBy;
		try {
			// get current user's name
			msgPostedBy = UserDirectoryService.getUser(newMessage.getCreatedBy()).getDisplayName();
		} catch (UserNotDefinedException unde) {
			// user not found, so use author
			msgPostedBy = newMessage.getAuthor();
		}
		
		// if this message has not been approved, we may only let moderators view it
		if (newMessage.getApproved() == null || !newMessage.getApproved()) {
			usersAbleToReadMessage = forumManager.getUsersAllowedForTopic(newMessage.getTopic().getId(), true, true);	
		} else {
			usersAbleToReadMessage = forumManager.getUsersAllowedForTopic(newMessage.getTopic().getId(), true, false);	
		}
		
		return usersAbleToReadMessage;
  }

  public String processDfMsgSaveDraft()
  {
    Message dMsg = constructMessage();
    dMsg.setDraft(Boolean.TRUE);

    if(selectedTopic == null)
    {
    	LOG.debug("selectedTopic is null in processDfMsgSaveDraft()");
    	return gotoMain();
    }

    forumManager.saveMessage(dMsg);
    setSelectedForumForCurrentTopic((DiscussionTopic) forumManager
        .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));
    selectedTopic.setTopic((DiscussionTopic) forumManager
        .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));
    selectedTopic.getTopic().setBaseForum(selectedForum.getForum());
    //selectedTopic.addMessage(new DiscussionMessageBean(dMsg, messageManager));
    selectedTopic.insertMessage(new DiscussionMessageBean(dMsg, messageManager));
    selectedTopic.getTopic().addMessage(dMsg);

    this.composeBody = null;
    this.composeLabel = null;
    this.composeTitle = null;

    this.attachments.clear();

    return ALL_MESSAGES;
  }

  public Message constructMessage()
  {
    	LOG.debug("....in constructMessage()");
    Message aMsg;

    aMsg = messageManager.createDiscussionMessage();
    
    if (aMsg != null)
    {
      StringBuilder alertMsg = new StringBuilder();
      aMsg.setTitle(FormattedText.processFormattedText(getComposeTitle(), alertMsg));
      aMsg.setBody(FormattedText.processFormattedText(getComposeBody(), alertMsg));
      
      if(getUserNameOrEid()!=null){
      aMsg.setAuthor(getUserNameOrEid());
      }
      else if(getUserNameOrEid()==null&&this.forumManager.getAnonRole()==true){
    	  aMsg.setAuthor(".anon");
      }
      
      
      aMsg.setDraft(Boolean.FALSE);
      aMsg.setDeleted(Boolean.FALSE);

      // if the topic is moderated, we want to leave approval null.
	  // if the topic is not moderated, all msgs are approved
      // if the author has moderator perm, the msg is automatically approved\
      
    if(selectedTopic == null)
    {
    	LOG.debug("selectedTopic is null in constructMessage()");
    	return null;
    }
	  if (!selectedTopic.isTopicModerated() || selectedTopic.getIsModeratedAndHasPerm())
	  {
		  aMsg.setApproved(Boolean.TRUE);
	  }
      aMsg.setTopic(selectedTopic.getTopic());
    }
    for (int i = 0; i < attachments.size(); i++)
    {
      aMsg.addAttachment(((DecoratedAttachment) attachments.get(i)).getAttachment());
    }
    attachments.clear();
   
    // oldAttachments.clear();

    return aMsg;
  }
  
  /**
   * Prevents users from trying to delete the topic they are currently creating
   * @return
   */
  public boolean isDisplayTopicDeleteOption()
  {
    if(selectedTopic == null)
    {
    	LOG.debug("selectedTopic is null in isDisplayTopicDeleteOption()");
    	return false;
    }
	  Topic topic = selectedTopic.getTopic();
	  if (topic == null || topic.getId() == null)
		  return false;
	  
	  Topic topicInDb = forumManager.getTopicById(topic.getId());
	  
	  return topicInDb != null;
  }
  
  private void setupForum() {
	  if(selectedForum == null)  {
		  DiscussionForum forum = forumManager.createForum();
	      forum.setModerated(areaManager.getDiscusionArea().getModerated()); // default to template setting
	      selectedForum = new DiscussionForumBean(forum, uiPermissionsManager, forumManager);
	      if("true".equalsIgnoreCase(ServerConfigurationService.getString("mc.defaultLongDescription")))
	      {
	      	selectedForum.setReadFullDesciption(true);
	      }
	      setNewForumBeanAssign();
	  }
  }
  
  /**
   * Prevents users from trying to delete the forum they are currently creating
   * @return
   */
  public boolean isDisplayForumDeleteOption()
  {
	  //If you have more than two tab/windows open, when you create one forum in one tab/window, go back to another one, "selectedForum" will be null. See SAK-13780 for detail.
	  if(selectedForum == null)  {
		  setupForum();
		  return false;
	  }

	  OpenForum forum = selectedForum.getForum();
	  if (forum == null || forum.getId() == null)
		  return false;
	  
	  OpenForum forumInDb = forumManager.getForumById(forum.getId());
	  
	  return forumInDb != null;
  }

  public String processDfComposeToggle()
  {
    String redirectTo = getExternalParameterByKey(REDIRECT_PROCESS_ACTION);
    String expand = getExternalParameterByKey("composeExpand");

    if (redirectTo == null || selectedTopic == null)
    {
    	LOG.debug("redirectTo or selectedTopic is null in isDisplayForumDeleteOption");
      return gotoMain();
    }
    if ("dfCompose".equals(redirectTo))
    {
      if ((expand != null) && ("true".equalsIgnoreCase(expand)))
      {
        selectedTopic.setReadFullDesciption(true);
      }
      else
      {
        selectedTopic.setReadFullDesciption(false);
      }
      return MESSAGE_COMPOSE;
    }
    if (MESSAGE_VIEW.equals(redirectTo))
    {
      if ((expand != null) && ("true".equalsIgnoreCase(expand)))
      {
        selectedTopic.setReadFullDesciption(true);
      }
      else
      {
        selectedTopic.setReadFullDesciption(false);
      }
      return MESSAGE_VIEW;
    }
    if ("dfTopicReply".equals(redirectTo))
    {
      if ((expand != null) && ("true".equalsIgnoreCase(expand)))
      {
        selectedTopic.setReadFullDesciption(true);
      }
      else
      {
        selectedTopic.setReadFullDesciption(false);
      }
      return "dfTopicReply";
    }

    return gotoMain();
  }

  public String getUserId()
  {
	  if (userId == null)
    	userId = SessionManager.getCurrentSessionUserId();
	  
	  return userId;
  }

  public boolean getFullAccess()
  {
    return forumManager.isInstructor();
  }

  /**
   * @return
   */
  public String processDfMsgMarkMsgAsRead()
  {
	    String messageId = getExternalParameterByKey(MESSAGE_ID);
	    String topicId = getExternalParameterByKey(TOPIC_ID);
	    if (messageId == null)
	    {
	      setErrorMessage(getResourceBundleString(MESSAGE_REFERENCE_NOT_FOUND));
	      return gotoMain();
	    }
	    if (topicId == null)
	    {
	      setErrorMessage(getResourceBundleString(TOPC_REFERENCE_NOT_FOUND));
	      return gotoMain();
	    }
	    // Message message=forumManager.getMessageById(Long.valueOf(messageId));
	    Message message = messageManager.getMessageByIdWithAttachments(Long.valueOf(
	        messageId));
	    messageManager.markMessageReadForUser(Long.valueOf(topicId),
	        Long.valueOf(messageId), true);
	    if (message == null)
	    {
	      setErrorMessage(getResourceBundleString(MESSAGE_WITH_ID) + messageId + getResourceBundleString(NOT_FOUND_WITH_QUOTE));
	      return gotoMain();
	    }
	    if(resetTopicById(TOPIC_ID)){ // reconstruct topic again;
	    	return null;
	    } else {
	    	return gotoMain();
	    }
  }
  
  /**
   * @return
   */
  public String processDfMsgMarkMsgAsReadFromThread()
  {
	    String messageId = getExternalParameterByKey(MESSAGE_ID);
	    String topicId = getExternalParameterByKey(TOPIC_ID);
	    if (messageId == null)
	    {
	      setErrorMessage(getResourceBundleString(MESSAGE_REFERENCE_NOT_FOUND));
	      return gotoMain();
	    }
	    if (topicId == null)
	    {
	      setErrorMessage(getResourceBundleString(TOPC_REFERENCE_NOT_FOUND));
	      return gotoMain();
	    }
	    // Message message=forumManager.getMessageById(Long.valueOf(messageId));
	    Message message = messageManager.getMessageByIdWithAttachments(Long.valueOf(
	        messageId));
	    messageManager.markMessageReadForUser(Long.valueOf(topicId),
	        Long.valueOf(messageId), true);
	    if (message == null)
	    {
	      setErrorMessage(getResourceBundleString(MESSAGE_WITH_ID) + messageId + getResourceBundleString(NOT_FOUND_WITH_QUOTE));
	      return gotoMain();
	    }
	    return processActionGetDisplayThread(); // reconstruct thread again;
  }
  
  public String processDfMsgReplyMsgFromEntire()
  {
	  	String messageId = getExternalParameterByKey(MESSAGE_ID);
	    String topicId = getExternalParameterByKey(TOPIC_ID);
	    if (messageId == null)
	    {
	      setErrorMessage(getResourceBundleString(MESSAGE_REFERENCE_NOT_FOUND));
	      return gotoMain();
	    }
	    if (topicId == null)
	    {
	      setErrorMessage(getResourceBundleString(TOPC_REFERENCE_NOT_FOUND));
	      return gotoMain();
	    }
	    // Message message=forumManager.getMessageById(Long.valueOf(messageId));
	    messageManager.markMessageReadForUser(Long.valueOf(topicId),
	        Long.valueOf(messageId), true);
	    Message message = messageManager.getMessageByIdWithAttachments(Long.valueOf(
		        messageId));
	    if (message == null)
	    {
	      setErrorMessage(getResourceBundleString(MESSAGE_WITH_ID) + messageId + getResourceBundleString(NOT_FOUND_WITH_QUOTE));
	      return gotoMain();
	    }

	    selectedMessage = new DiscussionMessageBean(message, messageManager);
	    
	    return processDfMsgReplyMsg();
  }
  
  public String processDfMsgReplyMsg()
  {
	  selectedMessageCount  = 0;
	  functionClick ++;
    if(selectedMessage.getMessage().getTitle() != null && !selectedMessage.getMessage().getTitle().startsWith(getResourceBundleString(MSG_REPLY_PREFIX)))
	  this.composeTitle = getResourceBundleString(MSG_REPLY_PREFIX) + " " + selectedMessage.getMessage().getTitle() + " ";
    else
      this.composeTitle = selectedMessage.getMessage().getTitle();
  	
    return "dfMessageReply";
  }

  public String processDfMsgReplyThread()
  {
	  selectedMessageCount  = 0;
	  functionClick ++;
  	if(selectedTopic == null)
  	{
  		LOG.debug("selectedTopic is null in processDfMsgReplyThread");
  		return gotoMain();
  	}
	  //we have to get the depth 0 message that this is in response to
	  DiscussionMessageBean cur = selectedMessage;
	  int depth = 0;
	  long messageId = 0;
	  while(cur.getDepth() > 0){
		  messageId = cur.getMessage().getInReplyTo().getId();
		  depth = cur.getDepth();
		  cur = new DiscussionMessageBean(messageManager.getMessageByIdWithAttachments(cur.getMessage().getInReplyTo().getId()), messageManager);
		  cur.setDepth(--depth);
	  }
	  selectedMessage = cur;
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
	  composeTitle = getResourceBundleString(MSG_REPLY_PREFIX) + " " + selectedMessage.getMessage().getTitle();
	  
	  
	  return "dfMessageReplyThread";
  }
  
  public String processDfMsgReplyTp()
  {
    return "dfTopicReply";
  }
  
  public String processDfMsgGrdFromThread()
  {
	  String messageId = getExternalParameterByKey(MESSAGE_ID);
	    String topicId = getExternalParameterByKey(TOPIC_ID);
	    if (messageId == null)
	    {
	      setErrorMessage(getResourceBundleString(MESSAGE_REFERENCE_NOT_FOUND));
	      return gotoMain();
	    }
	    if (topicId == null)
	    {
	      setErrorMessage(getResourceBundleString(TOPC_REFERENCE_NOT_FOUND));
	      return gotoMain();
	    }
	    // Message message=forumManager.getMessageById(Long.valueOf(messageId));
	    Message message = messageManager.getMessageByIdWithAttachments(Long.valueOf(
	        messageId));
	    if (message == null)
	    {
	      setErrorMessage(getResourceBundleString(MESSAGE_WITH_ID) + messageId + getResourceBundleString(NOT_FOUND_WITH_QUOTE));
	      return gotoMain();
	    }

	    selectedMessage = new DiscussionMessageBean(message, messageManager);
	  return processDfMsgGrd();
  }
  
  public String processDfMsgGrd()
  {
	  selectedMessageCount = 0;
	  functionClick ++;

	  if(selectedTopic == null)
  	{
  		LOG.debug("selectedTopic is null in processDfMsgGrd");
  		return gotoMain();
  	}
  	
  	grade_too_large_make_sure = false;
  	
	  selectedAssign = DEFAULT_GB_ITEM; 
	  resetGradeInfo();

	  try
	  {
		  String createdById = UserDirectoryService.getUser(selectedMessage.getMessage().getCreatedBy()).getId();
		  String gradebookUid = ToolManager.getCurrentPlacement().getContext();
		  String msgAssignmentName = selectedMessage.getMessage().getGradeAssignmentName();
		  String topicDefaultAssignment = selectedTopic.getTopic().getDefaultAssignName();
		  String forumDefaultAssignment = selectedForum.getForum().getDefaultAssignName();
		  
		  String selAssignmentName = null;
		  if (msgAssignmentName !=null && msgAssignmentName.trim().length()>0) {
			  selAssignmentName = msgAssignmentName;
		  } else if (topicDefaultAssignment != null && topicDefaultAssignment.trim().length() > 0) {
			  selAssignmentName = topicDefaultAssignment;
		  } else if (forumDefaultAssignment != null && forumDefaultAssignment.trim().length() > 0) {
			  selAssignmentName = forumDefaultAssignment;
		  }
		  
		  if (selAssignmentName != null) {
			  setUpGradeInformation(gradebookUid, selAssignmentName, createdById);  
		  } else {
			  // this is the "Select a gradebook item" selection
			  allowedToGradeItem = false;
			  selGBItemRestricted = true;
		  }
		  
		  // mark this message as read
		  messageManager.markMessageReadForUser(selectedTopic.getTopic().getId(), selectedMessage.getMessage().getId(), true);
	  }
	  catch(Exception e) 
	  { 
		  LOG.error("processDfMsgGrd in DiscussionFOrumTool - " + e); 
		  e.printStackTrace(); 
		  return null; 
	  } 

	  return GRADE_MESSAGE; 
  }
  
  private void setUpGradeInformation(String gradebookUid, String selAssignmentName, String studentId) {
	  GradebookService gradebookService = (org.sakaiproject.service.gradebook.shared.GradebookService) 
	  ComponentManager.get("org.sakaiproject.service.gradebook.GradebookService"); 
	  // first, check to see if user is authorized to view or grade this item in the gradebook
	  String function = gradebookService.getGradeViewFunctionForUserForStudentForItem(gradebookUid, selAssignmentName, studentId);
	  if (function == null) {
		  allowedToGradeItem = false;
		  selGBItemRestricted = true;
	  } else if (function.equalsIgnoreCase(GradebookService.gradePermission)) {
		  allowedToGradeItem = true;
		  selGBItemRestricted = false;
	  } else {
		  allowedToGradeItem = false;
		  selGBItemRestricted = false;
	  }

	  if (!selGBItemRestricted) {
		  Assignment assign = gradebookService.getAssignment(gradebookUid, selAssignmentName);
		  if (assign != null) {
			  gbItemPointsPossible = assign.getPoints().toString();
		  }

		  Double assignScore = gradebookService.getAssignmentScore(gradebookUid,  
				  selAssignmentName, studentId);
		  CommentDefinition assgnComment = gradebookService.getAssignmentScoreComment(gradebookUid, selAssignmentName, studentId);

		  if (assignScore != null) {
			  gbItemScore = assignScore.toString();
			  setSelectedAssignForMessage(selAssignmentName);
		  }
		  if (assgnComment != null) {
			  gbItemComment = assgnComment.getCommentText();
		  }
		  setSelectedAssignForMessage(selAssignmentName);
	  } else {
		  resetGradeInfo();
		  setSelectedAssignForMessage(selAssignmentName);
	  }
  }
  
  public String processDfMsgRvsFromThread()
  {
	  String messageId = getExternalParameterByKey(MESSAGE_ID);
	    String topicId = getExternalParameterByKey(TOPIC_ID);
	    if (messageId == null)
	    {
	      setErrorMessage(getResourceBundleString(MESSAGE_REFERENCE_NOT_FOUND));
	      return gotoMain();
	    }
	    if (topicId == null)
	    {
	      setErrorMessage(getResourceBundleString(TOPC_REFERENCE_NOT_FOUND));
	      return gotoMain();
	    }
	    // Message message=forumManager.getMessageById(Long.valueOf(messageId));
	    Message message = messageManager.getMessageByIdWithAttachments(Long.valueOf(
	        messageId));
	    if (message == null)
	    {
	      setErrorMessage(getResourceBundleString(MESSAGE_WITH_ID) + messageId + getResourceBundleString(NOT_FOUND_WITH_QUOTE));
	      return gotoMain();
	    }
	    message = messageManager.getMessageByIdWithAttachments(message.getId());
	    selectedMessage = new DiscussionMessageBean(message, messageManager);
	  return processDfMsgRvs();
  }

  public String processDfMsgRvs()
  {
	selectedMessageCount = 0;
	functionClick ++;
	
    attachments.clear();

    composeBody = selectedMessage.getMessage().getBody();
    composeLabel = selectedMessage.getMessage().getLabel();
    composeTitle = selectedMessage.getMessage().getTitle();
    List attachList = selectedMessage.getMessage().getAttachments();
    if (attachList != null)
    {
      for (int i = 0; i < attachList.size(); i++)
      {
        attachments.add(new DecoratedAttachment((Attachment) attachList.get(i)));
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
  
  /**
   * If deleting, the parameter determines where to navigate back to
   */
  public void setFromPage(String fromPage) {
	  this.fromPage = fromPage;
  }
  
  /**
   * Since delete message can be called from 2 places, this
   * parameter determines where to navigate back to
   */
  public String getFromPage() 
  {
	  return (fromPage != null) ? fromPage : "";
  }

  /**
   * Set detail screen for Delete confirmation view
   */
  public String processDfMsgDeleteConfirm()
  {
	selectedMessageCount = 0;
	functionClick ++;
	  // if coming from thread view, need to set message info
  	fromPage = getExternalParameterByKey(FROMPAGE);
    if (fromPage != null) {
    	processActionDisplayMessage();
    }

    deleteMsg = true;
    setErrorMessage(getResourceBundleString(CONFIRM_DELETE_MESSAGE));
    return MESSAGE_VIEW;
  }

  public String processDfReplyMsgPost()
  {
	  if(selectedMessageCount != 0 || functionClick != 1) {
		  setErrorMessage(getResourceBundleString(STATE_INCONSISTENT));
		  return null;
	  }
	  functionClick = 0;
		
  	if(selectedTopic == null)
  	{
  		LOG.debug("selectedTopic is null in processDfReplyMsgPost");
  		return gotoMain();
  	}
  	
  	DiscussionTopic topicWithMsgs = (DiscussionTopic) forumManager.getTopicByIdWithMessages(selectedTopic.getTopic().getId());
    List tempList = topicWithMsgs.getMessages();
    if(tempList != null)
    {
    	boolean existed = false;
    	Long selMsgId = selectedMessage.getMessage().getId();
    	for(int i=0; i<tempList.size(); i++)
    	{
    		Message tempMsg = (Message)tempList.get(i);
    		if(tempMsg.getId().equals(selMsgId))
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
    
    //update synoptic tool info
    incrementSynopticToolInfo(dMsg, true);
    
    setSelectedForumForCurrentTopic(topicWithMsgs);
    selectedTopic.setTopic(topicWithMsgs);
    selectedTopic.getTopic().setBaseForum(selectedForum.getForum());
    //selectedTopic.addMessage(new DiscussionMessageBean(dMsg, messageManager));
    selectedTopic.insertMessage(new DiscussionMessageBean(dMsg, messageManager));
    selectedTopic.getTopic().addMessage(dMsg);
    messageManager.markMessageReadForUser(selectedTopic.getTopic().getId(), dMsg.getId(), true);

    this.composeBody = null;
    this.composeLabel = null;
    this.composeTitle = null;

    this.attachments.clear();

    //return ALL_MESSAGES;
    //check selectedThreadHead exists
    if(selectedThreadHead == null){
    	selectedThreadHead = new DiscussionMessageBean(selectedMessage.getMessage(), messageManager);
	    //make sure we have the thread head of depth 0
	    while(selectedThreadHead.getMessage().getInReplyTo() != null){
	    	selectedThreadHead = new DiscussionMessageBean(
	    			messageManager.getMessageByIdWithAttachments(selectedThreadHead.getMessage().getInReplyTo().getId()), 
	    			messageManager);
	    }
    }
    
    sendEmailNotification(dMsg,selectedThreadHead);
    return processActionGetDisplayThread();
  }

  public String processDfReplyMsgSaveDraft()
  {
  	if(selectedTopic == null)
  	{
  		LOG.debug("selectedTopic is null in processDfReplyMsgSaveDraft");
  		return gotoMain();
  	}
  	
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

    this.composeBody = null;
    this.composeLabel = null;
    this.composeTitle = null;

    this.attachments.clear();

    return processActionGetDisplayThread();
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

    if ((attachId != null) && (!"".equals(attachId)))
    {
      for (int i = 0; i < attachments.size(); i++)
      {
        if (attachId.equalsIgnoreCase(((DecoratedAttachment) attachments.get(i)).getAttachment()
            .getAttachmentId()))
        {
          prepareRemoveAttach.add((DecoratedAttachment) attachments.get(i));
          attachments.remove(i);
          break;
        }
      }
    }

    return null;
  }

  public String processDfMsgRevisedCancel()
  {
	  selectedMessageCount = 0;
	  functionClick = 0;
	  getThreadFromMessage();
	  
	  this.composeBody = null;
	  this.composeLabel = null;
	  this.composeTitle = null;

	  this.attachments.clear();
	  
	  return MESSAGE_VIEW;
  }
  
  
  public String processDfMsgRevisedPost()
  {
	if(selectedMessageCount != 0 || functionClick != 1) {
		setErrorMessage(getResourceBundleString(STATE_INCONSISTENT));
		return null;
	}
	functionClick = 0;
  	if(selectedTopic == null)
  	{
  		LOG.debug("selectedTopic is null in processDfMsgRevisedPost");
  		return gotoMain();
  	}
  	
	DiscussionTopic dfTopic = selectedTopic.getTopic();
	DiscussionForum dfForum = selectedForum.getForum();
  	
    Message dMsg = selectedMessage.getMessage();
    
    messageManager.markMessageReadForUser(dfTopic.getId(), dMsg.getId(), true);

    if(!uiPermissionsManager.isReviseAny(dfTopic, dfForum) && !(selectedMessage.getIsOwn() && uiPermissionsManager.isReviseOwn(dfTopic, dfForum)))
	{
		setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_REVISE_MESSAGE));
		return null;
	}
    
    for (int i = 0; i < prepareRemoveAttach.size(); i++)
    {
      DecoratedAttachment removeAttach = (DecoratedAttachment) prepareRemoveAttach.get(i);
      dMsg.removeAttachment(removeAttach.getAttachment());
    }

    List oldList = dMsg.getAttachments();
    for (int i = 0; i < attachments.size(); i++)
    {
    	DecoratedAttachment thisAttach = (DecoratedAttachment) attachments.get(i);
      boolean existed = false;
      for (int j = 0; j < oldList.size(); j++)
      {
        Attachment existedAttach = (Attachment) oldList.get(j);
        if (existedAttach.getAttachmentId()
            .equals(thisAttach.getAttachment().getAttachmentId()))
        {
          existed = true;
          break;
        }
      }
      if (!existed)
      {
        dMsg.addAttachment(thisAttach.getAttachment());
      }
    }
    String currentBody = getComposeBody();
    String revisedInfo = "<p class=\"lastRevise textPanelFooter\">" + getResourceBundleString(LAST_REVISE_BY);
    
    revisedInfo += getUserNameOrEid();
    
    revisedInfo  += " " + getResourceBundleString(LAST_REVISE_ON);
    Date now = new Date();
    revisedInfo += now.toString() + " </p> ";
    
/*    if(currentBody != null && currentBody.length()>0 && currentBody.startsWith("Last Revised By "))
    {
    	if(currentBody.lastIndexOf(" <br/> ") > 0)
    	{
    		currentBody = currentBody.substring(currentBody.lastIndexOf(" <br/> ") + 7);
    	}
    }*/
    
    revisedInfo = revisedInfo.concat(currentBody);

    StringBuilder alertMsg = new StringBuilder();
    dMsg.setTitle(FormattedText.processFormattedText(getComposeTitle(), alertMsg));
    dMsg.setBody(FormattedText.processFormattedText(revisedInfo, alertMsg));
    dMsg.setDraft(Boolean.FALSE);
    dMsg.setModified(new Date());
    
    dMsg.setModifiedBy(getUserNameOrEid());
    if (!selectedTopic.isTopicModerated() || !selectedTopic.getIsModeratedAndHasPerm())
    {
    	dMsg.setApproved(Boolean.TRUE);
    }
    else
    {
    	dMsg.setApproved(null);
    }

    setSelectedForumForCurrentTopic((DiscussionTopic) forumManager
        .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));
    selectedTopic.setTopic((DiscussionTopic) forumManager
        .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));
    dMsg.setTopic((DiscussionTopic) forumManager
            .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));
//    selectedTopic.getTopic().setBaseForum(selectedForum.getForum());
//    Topic currentTopic = forumManager.getTopicByIdWithMessagesAndAttachments(dMsg.getTopic().getId());
//    dMsg.getTopic().setBaseForum(currentTopic.getBaseForum());
    //dMsg.getTopic().setBaseForum(selectedTopic.getTopic().getBaseForum());
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

    getSelectedTopic();
    getThreadFromMessage();
    return MESSAGE_VIEW;
  }

  public String processDfMsgSaveRevisedDraft()
  {
  	if(selectedTopic == null)
  	{
  		LOG.debug("selectedTopic is null in processDfMsgSaveRevisedDraft");
  		return gotoMain();
  	}
  		
    Message dMsg = selectedMessage.getMessage();

    for (int i = 0; i < prepareRemoveAttach.size(); i++)
    {
      DecoratedAttachment removeAttach = (DecoratedAttachment) prepareRemoveAttach.get(i);
      dMsg.removeAttachment(removeAttach.getAttachment());
    }

    List oldList = dMsg.getAttachments();
    for (int i = 0; i < attachments.size(); i++)
    {
    	DecoratedAttachment thisAttach = (DecoratedAttachment) attachments.get(i);
      boolean existed = false;
      for (int j = 0; j < oldList.size(); j++)
      {
        Attachment existedAttach = (Attachment) oldList.get(j);
        if (existedAttach.getAttachmentId()
            .equals(thisAttach.getAttachment().getAttachmentId()))
        {
          existed = true;
          break;
        }
      }
      if (!existed)
      {
        dMsg.addAttachment(thisAttach.getAttachment());
      }
    }
    String currentBody = getComposeBody();
    String revisedInfo = getResourceBundleString(LAST_REVISE_BY);

    revisedInfo += getUserNameOrEid();
    
    revisedInfo += " " + getResourceBundleString(LAST_REVISE_ON);
    Date now = new Date();
    revisedInfo += now.toString() + " <br/> ";    
    revisedInfo = revisedInfo.concat(currentBody);

    dMsg.setTitle(getComposeTitle());
    dMsg.setBody(revisedInfo);
    dMsg.setDraft(Boolean.TRUE);
    dMsg.setModified(new Date());
    
    dMsg.setModifiedBy(getUserNameOrEid());
    
    //  if the topic is moderated, we want to leave approval null.
	// if the topic is not moderated, all msgs are approved
	if (!selectedTopic.isTopicModerated())
	{
		dMsg.setApproved(Boolean.TRUE);
	}
    
//    setSelectedForumForCurrentTopic((DiscussionTopic) forumManager
//        .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));
//    selectedTopic.setTopic((DiscussionTopic) forumManager
//        .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));
//    selectedTopic.getTopic().setBaseForum(selectedForum.getForum());    
    setSelectedForumForCurrentTopic((DiscussionTopic) forumManager
        .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));
    selectedTopic.setTopic((DiscussionTopic) forumManager
        .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));
    dMsg.setTopic((DiscussionTopic) forumManager
        .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));
    //dMsg.getTopic().setBaseForum(selectedTopic.getTopic().getBaseForum());
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
	  selectedMessageCount = 0;
	  functionClick = 0;
	  
	this.errorSynch = false;
    this.composeBody = null;
    this.composeLabel = null;
    this.composeTitle = null;

    this.attachments.clear();
    
    getThreadFromMessage();
    return MESSAGE_VIEW;
  }
  
  public String processDfReplyThreadCancel()
  {
	  selectedMessageCount = 0;
	  functionClick = 0;
	  this.errorSynch = false;
	    this.composeBody = null;
	    this.composeLabel = null;
	    this.composeTitle = null;

	    this.attachments.clear();

	    return processActionGetDisplayThread();
  }

  public String processDfReplyTopicPost()
  {
  	if(selectedTopic == null)
  	{ 
  		LOG.debug("selectedTopic is null in processDfReplyTopicPost");
  		return gotoMain();
  	}
  	
    Message dMsg = constructMessage();

    forumManager.saveMessage(dMsg);
    
    //update synoptic tool info
    incrementSynopticToolInfo(dMsg, false);
    
    setSelectedForumForCurrentTopic((DiscussionTopic) forumManager
        .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));
    selectedTopic.setTopic((DiscussionTopic) forumManager
        .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));
    selectedTopic.getTopic().setBaseForum(selectedForum.getForum());
    //selectedTopic.addMessage(new DiscussionMessageBean(dMsg, messageManager));
    selectedTopic.insertMessage(new DiscussionMessageBean(dMsg, messageManager));
    selectedTopic.getTopic().addMessage(dMsg);

    //notify watchers
    sendEmailNotification(dMsg,selectedThreadHead);
    this.composeBody = null;
    this.composeLabel = null;
    this.composeTitle = null;

    this.attachments.clear();

    return ALL_MESSAGES;
  }

  public String processDfReplyTopicSaveDraft()
  {
  	if(selectedTopic == null)
  	{ 
  		LOG.debug("selectedTopic is null in processDfReplyTopicSaveDraft");
  		return gotoMain();
  	}
  	
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

  /**
   * Is the detail view normal or delete screen?
   */
  public boolean getDeleteMsg()
  {
    return deleteMsg;
  }

  /**
   * Construct the proper String reference for an Event
   * 
   * @param message
   * @return
   */
  private String getEventReference(Message message) 
  {
	  String eventMessagePrefix = "";
	  final String toolId = ToolManager.getCurrentTool().getId();
  	
	  if (toolId.equals(DiscussionForumService.MESSAGE_CENTER_ID))
		  eventMessagePrefix = "/messagesAndForums";
  	  else if (toolId.equals(DiscussionForumService.MESSAGES_TOOL_ID))
  		  eventMessagePrefix = "/messages";
  	  else
  		  eventMessagePrefix = "/forums";
  	
	  return eventMessagePrefix + getContextSiteId() + "/" + message.toString() + "/" + SessionManager.getCurrentSessionUserId();
  }
  
  /**
   * Deletes the message by setting boolean deleted switch to TRUE.
   */
  public String processDfMsgDeleteConfirmYes()
  {
	  //selectedMessageCount can be 0 or 1 for single tab open. There are 2 places for deleting message. 
	  if(selectedMessageCount > 1 || functionClick != 1) {
			setErrorMessage(getResourceBundleString(STATE_INCONSISTENT));
			return null;
		}
	
	  functionClick = 0;
  	if(selectedTopic == null)
  	{ 
  		LOG.debug("selectedTopic is null in processDfMsgDeleteConfirmYes");
  		return gotoMain();
  	}
  	
	  DiscussionTopic topic = selectedTopic.getTopic();
	  DiscussionForum forum = selectedForum.getForum();
	  
	  //Synoptic Message/Forums tool
	  HashMap<String, Integer> beforeChangeHM = null;    
	    Long forumId = selectedTopic.getTopic().getBaseForum().getId();
	    Long topicId = selectedTopic.getTopic().getId();
	    beforeChangeHM = SynopticMsgcntrManagerCover.getUserToNewMessagesForForumMap(getSiteId(), forumId, topicId);
	    
	    
	  if(!uiPermissionsManager.isDeleteAny(topic, forum) && !(selectedMessage.getIsOwn() && uiPermissionsManager.isDeleteOwn(topic, forum)))
	  {
		  setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_TO_DELETE));
		  this.deleteMsg = false;
		  return null;
	  }
	  
	  Message message = selectedMessage.getMessage();

	  // 'delete' this message
	  message.setDeleted(Boolean.TRUE);

	  // reload topic for this message so we can save it
	  message.setTopic((DiscussionTopic) forumManager
			  .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));

	  // does the actual save to 'delete' this message
	  forumManager.saveMessage(message, false);

	  // reload the topic, forum and reset the topic's base forum
	  selectedTopic = getDecoratedTopic(selectedTopic.getTopic());
	  setSelectedForumForCurrentTopic((DiscussionTopic) forumManager
			  .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));   
	  selectedTopic.getTopic().setBaseForum(selectedForum.getForum());

	  this.deleteMsg = false;

	  //Synoptic Message/Forums tool
	  //Compare previous new message counts to current new message counts after
	  //message was deleted for all users:
	  if(beforeChangeHM != null)
	    	updateSynopticMessagesForForumComparingOldMessagesCount(getSiteId(), forumId, topicId, beforeChangeHM, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
	  
	  // TODO: document it was done for tracking purposes
	  EventTrackingService.post(EventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_REMOVE, getEventReference(message), true));
	  LOG.info("Forum message " + message.getId() + " has been deleted by " + getUserId());

	  // go to thread view or all messages depending on
	  // where come from
	  if (!"".equals(fromPage)) {
		  final String where = fromPage;
		  fromPage = null;
		  processActionGetDisplayThread();
		  return where;
	  }
	  else {
		  return ALL_MESSAGES;
	  }
  }

  public String processDfMsgDeleteCancel()
  {
	  selectedMessageCount = 0;
	  functionClick = 0;
	this.deleteMsg = false;
    this.errorSynch = false;
    
    if (!"".equals(fromPage)) {
    	final String where = fromPage;
    	fromPage = null;
    	return where;
    }
    else {
    	return null;
    }
  }
  
  /**
   * A moderator view of all msgs pending approval
   * @return
   */
  public String processPendingMsgQueue()
  {
	  return PENDING_MSG_QUEUE;
  }
  
  /**
   * "Pending Messages" link will be displayed if current user
   * has moderate perm for at least one moderated topic in site.
   * Also sets number of pending msgs
   * @return
   */
  public boolean isDisplayPendingMsgQueue()
  {
	  if (displayPendingMsgQueue == null){
		  List membershipList = uiPermissionsManager.getCurrentUserMemberships();
		  int numModTopicWithPerm = forumManager.getNumModTopicsWithModPermission(membershipList);
		  
		  if (numModTopicWithPerm < 1)
		  {
			  displayPendingMsgQueue = false;
		  }
		  else
		  {		  
			  displayPendingMsgQueue = true;
		  }
	  }
	  
	  if (refreshPendingMsgs && displayPendingMsgQueue.booleanValue()) {
		  refreshPendingMessages();
	  }
	  return displayPendingMsgQueue.booleanValue();
  }
  
  public int getNumPendingMessages()
  {
	  return numPendingMessages;
  }
  
  public void setNumPendingMessages(int numPendingMessages)
  {
	  this.numPendingMessages = numPendingMessages;
  }
  
  /**
   * Retrieve pending msgs from db and make DiscussionMessageBeans
   *
   */
  private void refreshPendingMessages()
  {
	  pendingMsgs = new ArrayList();
	  numPendingMessages = 0;
	  List messages = forumManager.getPendingMsgsInSiteByMembership(uiPermissionsManager.getCurrentUserMemberships());
	  
	  if (messages != null && !messages.isEmpty())
	  {
		  messages = messageManager.sortMessageByDate(messages, true);
	  
		  Iterator msgIter = messages.iterator();
		  while (msgIter.hasNext())
		  {
			  Message msg = (Message) msgIter.next();
			  DiscussionMessageBean decoMsg = new DiscussionMessageBean(msg, messageManager);
			  pendingMsgs.add(decoMsg);
			  numPendingMessages++;
		  }
	  }
	  
	  refreshPendingMsgs = false;
  }
  
  /**
   * returns all messages in the site that are pending and curr user has
   * moderate perm to view
   * @return
   */
  public List getPendingMessages()
  {
	  if (refreshPendingMsgs)
	  {
	  	refreshPendingMessages();
	  }
	  
	  return pendingMsgs;
  }
  
  /**
   * Will approve all "selected" messags
   * @return
   */
  public String markCheckedAsApproved()
  {
	  approveOrDenySelectedMsgs(true);
	  
	  if (numPendingMessages > 0)
		  return PENDING_MSG_QUEUE;
	  
	  return processActionHome();
  }
  
  /**
   * Will deny all "selected" messages
   * @return
   */
  public String markCheckedAsDenied()
  {
	  approveOrDenySelectedMsgs(false);
	  
	  if (numPendingMessages > 0)
		  return PENDING_MSG_QUEUE;
	  
	  return processActionHome();
  }
  
  /**
   * Mark selected msgs as denied or approved
   * @param approved
   */
  private void approveOrDenySelectedMsgs(boolean approved)
  {
	  if (pendingMsgs == null || pendingMsgs.isEmpty())
	  {
		  return;
	  }
	  
	  int numSelected = 0;
	  
	  Iterator iter = pendingMsgs.iterator();
	  while (iter.hasNext())
	  {
		  DiscussionMessageBean decoMessage = (DiscussionMessageBean) iter.next();
		  if (decoMessage.isSelected())
		  {
			  Message msg = decoMessage.getMessage();
			  if (selectedTopic != null) {
				  // the topic is not initialized on the selectedMessage
				  Topic topic = selectedTopic.getTopic();
				  msg.setTopic(topic);			  			 	  
			  }
			  
			  HashMap<String, Integer> beforeChangeHM = null;	    	  
			  beforeChangeHM = SynopticMsgcntrManagerCover.getUserToNewMessagesForForumMap(getSiteId(), msg.getTopic().getBaseForum().getId(), msg.getTopic().getId());
			  
			  messageManager.markMessageApproval(msg.getId(), approved);			  
			  
			  messageManager.markMessageReadForUser(msg.getTopic().getId(), msg.getId(), true);
			  numSelected++;
			  numPendingMessages--;

			  if(beforeChangeHM != null)
	        		updateSynopticMessagesForForumComparingOldMessagesCount(getSiteId(), msg.getTopic().getBaseForum().getId(), msg.getTopic().getId(), beforeChangeHM, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);

		  }
	  }
	  
	  if (numSelected < 1)
		  setErrorMessage(getResourceBundleString(NO_MSG_SEL_FOR_APPROVAL));
	  else
	  {
		  if (approved)
			  setSuccessMessage(getResourceBundleString(MSGS_APPROVED));
		  else
			  setSuccessMessage(getResourceBundleString(MSGS_DENIED));
	  }
	  
	  refreshPendingMsgs = true;
  }

  /**
   * Deny a message
   * @return
   */
  public String processDfMsgDeny()
  {
	  Long msgId = selectedMessage.getMessage().getId();
	  if (msgId != null)
	  {
		  //grab the recipient list before denied status is saved, so we can
		  //update their synoptic info
		  Message msg = messageManager.getMessageById(msgId);
		  if (selectedTopic != null) {
			  // the topic is not initialized on the selectedMessage
			  Topic topic = selectedTopic.getTopic();
			  msg.setTopic(topic);			  			 	  
		  }

		  HashMap<String, Integer> beforeChangeHM = null;	    	  
		  beforeChangeHM = SynopticMsgcntrManagerCover.getUserToNewMessagesForForumMap(getSiteId(), msg.getTopic().getBaseForum().getId(), msg.getTopic().getId());

		  messageManager.markMessageApproval(msgId, false);
		  selectedMessage = new DiscussionMessageBean(messageManager.getMessageByIdWithAttachments(msgId), messageManager);
		  refreshSelectedMessageSettings(selectedMessage.getMessage());
		  setSuccessMessage(getResourceBundleString("cdfm_denied_alert"));
		  getThreadFromMessage();
		  
		  if(beforeChangeHM != null)
      		updateSynopticMessagesForForumComparingOldMessagesCount(getSiteId(), msg.getTopic().getBaseForum().getId(), msg.getTopic().getId(), beforeChangeHM, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
	  
	  }
	  
	  refreshPendingMsgs = true;
	  
	  return MESSAGE_VIEW;
  }
  
  /**
   * Deny a message and return to comment page
   * @return
   */
  public String processDfMsgDenyAndComment()
  {
	  Long msgId = selectedMessage.getMessage().getId();
	  if (msgId != null)
	  {
		  //grab the recipient list before denied status is saved, so we can
		  //update their synoptic info
		  Message msg = messageManager.getMessageById(msgId);
		  if (selectedTopic != null) {
			  // the topic is not initialized on the selectedMessage
			  Topic topic = selectedTopic.getTopic();
			  msg.setTopic(topic);			  			 	  
		  }
		 
		  HashMap<String, Integer> beforeChangeHM = null;	    	  
		  beforeChangeHM = SynopticMsgcntrManagerCover.getUserToNewMessagesForForumMap(getSiteId(), msg.getTopic().getBaseForum().getId(), msg.getTopic().getId());
		  
		  messageManager.markMessageApproval(msgId, false);
		  selectedMessage = new DiscussionMessageBean(messageManager.getMessageByIdWithAttachments(msgId), messageManager);
		  displayDeniedMsg = true;
		  
		  if(beforeChangeHM != null)
	      		updateSynopticMessagesForForumComparingOldMessagesCount(getSiteId(), msg.getTopic().getBaseForum().getId(), msg.getTopic().getId(), beforeChangeHM, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
		  
	  }
	  
	  refreshPendingMsgs = true;
	  
	  return ADD_COMMENT;
  }
  
  /**
   * Approve a message
   * @return
   */
  public String processDfMsgApprove()
  {
	  Long msgId = selectedMessage.getMessage().getId();
	  if (msgId != null)
	  {
		  Message msg = messageManager.getMessageById(msgId);
		  if (selectedTopic != null) {
			  // the topic is not initialized on the selectedMessage
			  Topic topic = selectedTopic.getTopic();
			  msg.setTopic(topic);			  			 	  
		  }
		  
		  HashMap<String, Integer> beforeChangeHM = null;	    	  
		  beforeChangeHM = SynopticMsgcntrManagerCover.getUserToNewMessagesForForumMap(getSiteId(), msg.getTopic().getBaseForum().getId(), msg.getTopic().getId());
		  
		  messageManager.markMessageApproval(msgId, true);
		  
		    
		  
		  
		  selectedMessage = new DiscussionMessageBean(messageManager.getMessageByIdWithAttachments(msgId), messageManager);
		  refreshSelectedMessageSettings(selectedMessage.getMessage());
		  setSuccessMessage(getResourceBundleString("cdfm_approved_alert"));
		  getThreadFromMessage();
		  
		  if(beforeChangeHM != null)
	      		updateSynopticMessagesForForumComparingOldMessagesCount(getSiteId(), msg.getTopic().getBaseForum().getId(), msg.getTopic().getId(), beforeChangeHM, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
	  }
	  
	  refreshPendingMsgs = true;
	  
	  return MESSAGE_VIEW;
  }
  
  /**
   * @return
   */
  public String processDfMsgAddComment()
  {
	  moderatorComments = "";
	  return ADD_COMMENT;
  }
  
  /**
   * 
   * @return
   */
  public String processCancelAddComment()
  {
	  if (displayDeniedMsg) // only displayed if from Deny & Comment path
	  {
		  setSuccessMessage(getResourceBundleString("cdfm_denied_alert"));
		  displayDeniedMsg = false;
	  }
	  
	  return MESSAGE_VIEW;
  }
  
  /**
   * Moderators may add a comment that is prepended to the text
   * of the denied msg
   * @return
   */
  public String processAddCommentToDeniedMsg()
  {
  	if(selectedTopic == null)
  	{ 
  		LOG.debug("selectedTopic is null in processAddCommentToDeniedMsg");
  		return gotoMain();
  	}
  	
	  if (!selectedTopic.getIsModeratedAndHasPerm())
	  {
		  setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_TO_ADD_COMMENT));
		  return ADD_COMMENT;
	  }
	  
	  if (moderatorComments == null || moderatorComments.trim().length() < 1)
	  {
		 setErrorMessage(getResourceBundleString(INVALID_COMMENT)); 
		 return ADD_COMMENT;
	  }
	  
	  Message currMessage = selectedMessage.getMessage();
	  
	  StringBuilder sb = new StringBuilder();
	  sb.append("<div class=\"messageCommentWrap\">");
	  sb.append("<div class=\"messageCommentMD\">");
	  sb.append(getResourceBundleString(MOD_COMMENT_TEXT) + " ");
	  sb.append(UserDirectoryService.getCurrentUser().getDisplayName());
	  sb.append("</div>");
	  sb.append("<div class=\"messageCommentBody\">");
	  sb.append(moderatorComments);
	  sb.append("</div>");
	  sb.append("</div>");
	  
	  
	  String originalText = currMessage.getBody();
	  currMessage.setBody(sb.toString() + originalText);
	  
	  currMessage.setTopic((DiscussionTopic) forumManager
              .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));
      forumManager.saveMessage(currMessage);
	  
	  if (displayDeniedMsg) // only displayed if from Deny & Comment path
	  {
		  setSuccessMessage(getResourceBundleString("cdfm_denied_alert"));
		  displayDeniedMsg = false;
	  }
	  
	  // we also must mark this message as unread for the author to let them
	  // know there is a comment
	  forumManager.markMessageReadStatusForUser(currMessage, false, currMessage.getCreatedBy());
	  
	  return MESSAGE_VIEW;
  }
  
  /**
   * Approve option is displayed if:
   * 1) topic is moderated
   * 2) user has moderate perm
   * 3) message has not been approved
   * @return
   */
  public boolean isAllowedToApproveMsg()
  {
  	if(selectedTopic == null)
  	{ 
  		LOG.debug("selectedTopic is null in isAllowedToApproveMsg");
  		return false;
  	}
  	
	  return selectedTopic.getIsModeratedAndHasPerm() && !selectedMessage.isMsgApproved();
  }
  
  /**
   * Deny option is displayed if:
   * 1) topic is moderated
   * 2) user has moderate perm
   * 3) message has not been denied
   * 4) message has no responses
   * @return
   */
  public boolean isAllowedToDenyMsg()
  {
  	if(selectedTopic == null)
  	{ 
  		LOG.debug("selectedTopic is null in isAllowedToDenyMsg");
  		return false;
  	}
  	
	  return selectedTopic.getIsModeratedAndHasPerm() && !selectedMessage.isMsgDenied() && !selectedMessage.getHasChild();
  }

  public void setNewForumBeanAssign()
  {
    selectedForum.setGradeAssign(DEFAULT_GB_ITEM);
  }
  
  public void setNewTopicBeanAssign()
  {
  	if(selectedTopic == null)
  	{ 
  		LOG.debug("selectedTopic is null in setNewTopicBeanAssign");
  		return;
  	}
  	
    if(selectedForum !=null && selectedForum.getGradeAssign() != null && selectedForum.getForum() != null)
    {
      selectedTopic.setGradeAssign(selectedForum.getGradeAssign());
      selectedTopic.getTopic().setDefaultAssignName(selectedForum.getForum().getDefaultAssignName());
    }
  }

  public void setNewTopicBeanAssign(DiscussionForumBean dfb, DiscussionTopicBean dtb)
  {
    if(dfb !=null && dfb.getGradeAssign() != null && dfb.getForum() != null)
    {
      dtb.setGradeAssign(dfb.getGradeAssign());
      dtb.getTopic().setDefaultAssignName(dfb.getForum().getDefaultAssignName());
    }
  }

  public void setForumBeanAssign()
  {
	if(assignments != null)
	{
      for(int i=0; i<assignments.size(); i++)
      {
        if(((SelectItem)assignments.get(i)).getLabel().equals(selectedForum.getForum().getDefaultAssignName()))
        {
          selectedForum.setGradeAssign((String)((SelectItem)assignments.get(i)).getValue());
          break;
        }
      }
	}
  }
  
  public void setTopicBeanAssign()
  {
  	if(selectedTopic == null)
  	{ 
  		LOG.debug("selectedTopic is null in setTopicBeanAssign");
  		return;
  	}
  	
  	if(assignments != null)
  	{
  		for(int i=0; i<assignments.size(); i++)
  		{
  			if(((SelectItem)assignments.get(i)).getLabel().equals(selectedTopic.getTopic().getDefaultAssignName()))
  			{
  				selectedTopic.setGradeAssign((String)((SelectItem)assignments.get(i)).getValue());
  				break;
  			}
  		}
  	}
  }
  
  public void setSelectedAssignForMessage(String assignName)
  {
    if(assignments != null)
    {
	  for(int i=0; i<assignments.size(); i++)
      {
        if(((SelectItem)assignments.get(i)).getLabel().equals(assignName))
        {
          this.selectedAssign = (String)((SelectItem)assignments.get(i)).getValue();
          break;
        }
      }
    }
  }

  public void saveForumSelectedAssignment(DiscussionForum forum)
  {
    if(selectedForum.getGradeAssign() != null && !DEFAULT_GB_ITEM.equals(selectedForum.getGradeAssign()))
    {
      forum.setDefaultAssignName( ((SelectItem)assignments.get( new Integer(selectedForum.getGradeAssign()).intValue())).getLabel());
    }
  }
  
  public void saveForumAttach(DiscussionForum forum)
  {
    for (int i = 0; i < prepareRemoveAttach.size(); i++)
    {
    	DecoratedAttachment removeAttach = (DecoratedAttachment) prepareRemoveAttach.get(i);
      List oldList = forum.getAttachments();
      for (int j = 0; j < oldList.size(); j++)
      {
        Attachment existedAttach = (Attachment) oldList.get(j);
        if (existedAttach.getAttachmentId().equals(
            removeAttach.getAttachment().getAttachmentId()))
        {
          forum.removeAttachment(removeAttach.getAttachment());
          break;
        }
      }
    }

    List oldList = forum.getAttachments();
    if (oldList != null && attachments != null)
    {
      for (int i = 0; i < attachments.size(); i++)
      {
      	DecoratedAttachment thisAttach = (DecoratedAttachment) attachments.get(i);
        boolean existed = false;
        for (int j = 0; j < oldList.size(); j++)
        {
          Attachment existedAttach = (Attachment) oldList.get(j);
          if (existedAttach.getAttachmentId().equals(
              thisAttach.getAttachment().getAttachmentId()))
          {
            existed = true;
            break;
          }
        }
        if (!existed)
        {
          forum.addAttachment(thisAttach.getAttachment());
        }
      }
    }

    prepareRemoveAttach.clear();
    attachments.clear();
  }

  public void saveTopicSelectedAssignment(DiscussionTopic topic)
  {
  	if(selectedTopic == null)
  	{ 
  		LOG.debug("selectedTopic is null in saveTopicSelectedAssignment");
  		return;
  	}
  	
    if(selectedTopic.getGradeAssign() != null && !DEFAULT_GB_ITEM.equals(selectedTopic.getGradeAssign()))
    {
      topic.setDefaultAssignName( ((SelectItem)assignments.get( new Integer(selectedTopic.getGradeAssign()).intValue())).getLabel());
    }
  }
  
  public void saveTopicAttach(DiscussionTopic topic)
  {
    for (int i = 0; i < prepareRemoveAttach.size(); i++)
    {
    	DecoratedAttachment removeAttach = (DecoratedAttachment) prepareRemoveAttach.get(i);
      List oldList = topic.getAttachments();
      for (int j = 0; j < oldList.size(); j++)
      {
        Attachment existedAttach = (Attachment) oldList.get(j);
        if (existedAttach.getAttachmentId().equals(
            removeAttach.getAttachment().getAttachmentId()))
        {
          topic.removeAttachment(removeAttach.getAttachment());
          break;
        }
      }
    }

    List oldList = topic.getAttachments();
    if (oldList != null && attachments != null)
    {
      for (int i = 0; i < attachments.size(); i++)
      {
      	DecoratedAttachment thisAttach = (DecoratedAttachment) attachments.get(i);
        boolean existed = false;
        for (int j = 0; j < oldList.size(); j++)
        {
          Attachment existedAttach = (Attachment) oldList.get(j);
          if (existedAttach.getAttachmentId().equals(
              thisAttach.getAttachment().getAttachmentId()))
          {
            existed = true;
            break;
          }
        }
        if (!existed)
        {
          topic.addAttachment(thisAttach.getAttachment());
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

    if ((attachId != null) && (!"".equals(attachId)))
    {
      for (int i = 0; i < attachments.size(); i++)
      {
        if (attachId.equalsIgnoreCase(((DecoratedAttachment) attachments.get(i)).getAttachment()
            .getAttachmentId()))
        {
          prepareRemoveAttach.add((DecoratedAttachment) attachments.get(i));
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
  
  public boolean getExpandedView()
  {
	  return expandedView;
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
    return gbItemScore; 
  } 
  
  public String getGbItemPointsPossible() 
  {
	  return gbItemPointsPossible;
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
    return gbItemComment; 
  } 
  
  public void rearrageTopicMsgsThreaded()
  {
	  if (selectedTopic != null)
	  {





		  List msgsList = selectedTopic.getMessages();
		  Collections.reverse(msgsList);
		  if (msgsList != null && !msgsList.isEmpty())
			  msgsList = filterModeratedMessages(msgsList, selectedTopic.getTopic(), (DiscussionForum)selectedTopic.getTopic().getBaseForum());

		  List orderedList = new ArrayList();
		  List threadList = new ArrayList();

		  if (!ServerConfigurationService.getBoolean("msgcntr.sort.thread.update", false)) {

			  if(msgsList != null)
			  {
				  for(int i=0; i<msgsList.size(); i++)
				  {
					  DiscussionMessageBean dmb = (DiscussionMessageBean)msgsList.get(i);
					  if(dmb.getMessage().getInReplyTo() == null)
					  {
						  threadList.add(dmb);
						  dmb.setDepth(0);
						  orderedList.add(dmb);
						  //for performance speed - operate with existing selectedTopic msgs instead of getting from manager through DB again 
						  //use arrays so as to pass by reference during recursion
						  recursiveGetThreadedMsgsFromListWithCounts(msgsList, orderedList, dmb, new int[1], new int[1]);
					  }
				  }
			  }



		  } else {
			  if(msgsList != null)
			  {
				  for(int i=0; i<msgsList.size(); i++)
				  {
					  DiscussionMessageBean dmb = (DiscussionMessageBean)msgsList.get(i);
					  if(dmb.getMessage().getInReplyTo() == null)
					  {
						  threadList.add(dmb);
					  }
				  }

				  sortThreadListByUpdate(threadList);
				  for(int i=0; i<threadList.size(); i++)
				  {
					  DiscussionMessageBean dmb = (DiscussionMessageBean)threadList.get(i);
					  dmb.setDepth(0);
					  	orderedList.add(dmb);
						  //for performance speed - operate with existing selectedTopic msgs instead of getting from manager through DB again 
						  //use arrays so as to pass by reference during recursion
						  recursiveGetThreadedMsgsFromListWithCounts(msgsList, orderedList, dmb, new int[1], new int[1]);
					  
				  }
			  }
			  
		  }
		  selectedTopic.setMessages(orderedList);
	  }
  }
  
  private List sortThreadListByUpdate(List threadList) {
	  
	  
	  Collections.sort(threadList, new ThreadUpdateSorter());
	  
	  
	  return threadList;
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

  private void recursiveGetThreadedMsgsFromListWithCounts(List msgsList, List returnList,
      DiscussionMessageBean currentMsg, int[] childCount, int[] childUnread)
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
    	if (!thisMsgBean.getDeleted())
    	{
    		childCount[0]++;
    	}
    	
    	if(!thisMsgBean.isRead() && !thisMsgBean.getDeleted())
    	{
    		childUnread[0]++;
    	}
        thisMsgBean.setDepth(currentMsg.getDepth() + 1);
        returnList.add(thisMsgBean);
        this
            .recursiveGetThreadedMsgsFromListWithCounts(msgsList, returnList, thisMsgBean, childCount, childUnread);
      }
    }
    currentMsg.setChildCount(childCount[0]);
    currentMsg.setChildUnread(childUnread[0]);
  }
  
  private void resetGradeInfo() {
	  gradePoint = null; 
	  gbItemScore = null;
	  gbItemComment = null;
	  gradeComment = null; 
	  gbItemPointsPossible = null;
  }
  
  public String processDfGradeCancel() 
  { 
	  selectedMessageCount = 0;
	  functionClick = 0;
    gradeNotify = false; 
    selectedAssign = DEFAULT_GB_ITEM; 
    resetGradeInfo();
    
    getThreadFromMessage();
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
			  resetGradeInfo();

			  if(!DEFAULT_GB_ITEM.equalsIgnoreCase(selectedAssign)) {
				  String gradebookUid = ToolManager.getCurrentPlacement().getContext();
				  String selAssignName = ((SelectItem)assignments.get((new Integer(selectedAssign)).intValue())).getLabel();		  
				  String studentId = UserDirectoryService.getUser(selectedMessage.getMessage().getCreatedBy()).getId();
				  
				  setUpGradeInformation(gradebookUid, selAssignName, studentId);
			  } else {
				  // this is the "Select a gradebook item" option
				  allowedToGradeItem = false;
				  selGBItemRestricted = true;
			  }

			  return GRADE_MESSAGE; 
		  } 
		  catch(Exception e) 
		  { 
			  LOG.error("processGradeAssignChange in DiscussionFOrumTool - " + e); 
			  e.printStackTrace(); 
			  return null; 
		  } 
	  } 
  } 
 
   public boolean isNumber(String validateString) 
   {
     try  
     {
       double d = Double.valueOf(validateString).doubleValue();
       if(d >= 0)
         return true;
       else
         return false;
     }
     catch (NumberFormatException e) 
     {
       //e.printStackTrace();
       return false;
     }
   }
   
   public boolean isFewerDigit(String validateString)
   {
     String stringValue = new Double(validateString).toString();
     if(stringValue.lastIndexOf(".") >= 0)
     {
       String subString = stringValue.substring(stringValue.lastIndexOf("."));
       if(subString != null && subString.length() > 3)
         return false;
     }
     
     return true;
   }
  
  private boolean validateGradeInput()
  {
    if(!isNumber(gradePoint))
    {
      FacesContext currentContext = FacesContext.getCurrentInstance();
      String uiComponentId = "msgForum:dfMsgGradeGradePoint";
      FacesMessage validateMessage = new FacesMessage(getResourceBundleString(GRADE_GREATER_ZERO));
      validateMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
      currentContext.addMessage(uiComponentId, validateMessage);
      
      return false;
    }
    else if(!isFewerDigit(gradePoint))
    {
      FacesContext currentContext = FacesContext.getCurrentInstance();
      String uiComponentId = "msgForum:dfMsgGradeGradePoint";
      FacesMessage validateMessage = new FacesMessage(getResourceBundleString(GRADE_DECIMAL_WARN));
      validateMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
      currentContext.addMessage(uiComponentId, validateMessage); 
      
      return false;
    }
    
    return true;
  }
  
  public String processDfGradeSubmit() 
  { 
	  if(selectedMessageCount != 0 ) {
			setErrorMessage(getResourceBundleString(STATE_INCONSISTENT));
			return null;
		}
	  functionClick = 0;
	  selectedMessageCount = 0;

  	if(selectedTopic == null)
  	{ 
  		LOG.debug("selectedTopic is null in processDfGradeSubmit");
  		return gotoMain();
  	}
  	
	  gbItemScore = gradePoint;
	  gbItemComment = gradeComment;
	  if(selectedAssign == null || selectedAssign.trim().length()==0 || DEFAULT_GB_ITEM.equalsIgnoreCase(selectedAssign)) 
	    { 
			setErrorMessage(getResourceBundleString(NO_ASSGN)); 
		    return null; 
	    }     
	  
	  if(gradePoint == null || gradePoint.trim().length()==0) 
	 { 
	      setErrorMessage(getResourceBundleString(NO_GRADE_PTS)); 
	      return null; 
	 } 
	  
	  try {
		  if(Double.parseDouble(gradePoint) > Double.parseDouble(gbItemPointsPossible) && !grade_too_large_make_sure) {
			  setErrorMessage(getResourceBundleString(TOO_LARGE_GRADE));
			  grade_too_large_make_sure = true;
			  return null;
		  } else {
			  LOG.info("the user confirms he wants to give student higher grade");
		  }		  
	  } catch(NumberFormatException e) {
		  LOG.info("number format problem.");
	  }	  

    
    if(!validateGradeInput())
      return null;
    
    try 
    {   
        GradebookService gradebookService = (org.sakaiproject.service.gradebook.shared.GradebookService) 
        ComponentManager.get("org.sakaiproject.service.gradebook.GradebookService"); 
        String selectedAssignName = ((SelectItem)assignments.get((new Integer(selectedAssign)).intValue())).getLabel();
        String gradebookUuid = ToolManager.getCurrentPlacement().getContext();
        String studentUid = UserDirectoryService.getUser(selectedMessage.getMessage().getCreatedBy()).getId();

        gradebookService.setAssignmentScore(gradebookUuid,  
        		  selectedAssignName, studentUid, new Double(gradePoint), "");
        if (gradeComment != null && gradeComment.trim().length() > 0)
        {
        	gradebookService.setAssignmentScoreComment(gradebookUuid,  
      		  selectedAssignName, studentUid, gradeComment);
        }
        
        Message msg = selectedMessage.getMessage();
        msg.setGradeAssignmentName(selectedAssignName);
        msg.setTopic((DiscussionTopic) forumManager
                .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));
        forumManager.saveMessage(msg, false);
        
        setSuccessMessage(getResourceBundleString(GRADE_SUCCESSFUL));
    } 
    catch(SecurityException se) {
    	LOG.error("Security Exception - processDfGradeSubmit:" + se);
    	setErrorMessage(getResourceBundleString("cdfm_no_gb_perm"));
    }
    catch(Exception e) 
    { 
      LOG.error("DiscussionForumTool - processDfGradeSubmit:" + e); 
      e.printStackTrace(); 
    } 
        
    EventTrackingService.post(EventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_GRADE, getEventReference(selectedMessage.getMessage()), true));
    
    gradeNotify = false; 
    selectedAssign = DEFAULT_GB_ITEM; 
    resetGradeInfo();  
    getThreadFromMessage();
    return MESSAGE_VIEW; 
  } 
 
  public String processCheckAll()
  {
  	if(selectedTopic == null)
  	{ 
  		LOG.debug("selectedTopic is null in processCheckAll");
  		return null;
  	}
  	
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
   * @return Returns the selectedMessageShow.
   */
  public String getSelectedMessageShow()
  {
    return selectedMessageShow;
  }

  /**
   * @param selectedMessageShow
   *          The selectedMessageShow to set.
   */
  public void setSelectedMessageShow(String selectedMessageShow)
  {
    this.selectedMessageShow = selectedMessageShow;
  }
  
  /**
   * @return Returns the selectedMessagOrganize.
   */
  public String getSelectedMessageOrganize()
  {
    return selectedMessageOrganize;
  }

  /**
   * @param selectedMessageOrganize
   *          The selectedMessageOrganize to set.
   */
  public void setSelectedMessageOrganize(String selectedMessageOrganize)
  {
    this.selectedMessageOrganize = selectedMessageOrganize;
  }
  
  public String getThreadAnchorMessageId()
  {
	  return threadAnchorMessageId;
  }
  
  public void setThreadAnchorMessageId(String newValue)
  {
	  threadAnchorMessageId = newValue;
  }
  

  /**
   * @return Returns the displayUnreadOnly.
   */
  public boolean getDisplayUnreadOnly()
  {
    return displayUnreadOnly;
  }
  
  public void processActionToggleExpanded()
  {
	  if("true".equals(expanded)){
		  expanded = "false";
	  } else {
		  expanded = "true";
	  }
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
    //expandedView = false;
    if (changeView == null)
    {
      //threaded = false;
      setErrorMessage(getResourceBundleString(FAILED_REND_MESSAGE));
      return;
    }
    if (ALL_MESSAGES.equals(changeView))
    {
    	if(selectedTopic == null)
    	{ 
    		LOG.debug("selectedTopic is null in processValueChangeForMessageView");
    		return;
    	}
      //threaded = false;
      setSelectedMessageView(ALL_MESSAGES);
      
      DiscussionTopic topic = null;
      topic = forumManager.getTopicById(selectedTopic.getTopic().getId());
      setSelectedForumForCurrentTopic(topic);
      selectedTopic = getDecoratedTopic(topic);

      return;
    }
    else
      if (UNREAD_VIEW.equals(changeView))
      {
      	//threaded = false;
        this.displayUnreadOnly = true;
        return;
      }
    /*
      else
    	if (changeView.equals(EXPANDED_VIEW))
    	{
    		threaded = false;
    		expandedView = true;
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
              */
              else
              {
                //threaded = false;
                setErrorMessage(getResourceBundleString(VIEW_UNDER_CONSTRUCT));
                return;
              }
  }
  
  public void processValueChangedForMessageShow(ValueChangeEvent vce){
	  if (LOG.isDebugEnabled())
	      LOG.debug("processValueChangeForMessageView(ValueChangeEvent " + vce
	          + ")");
	  isDisplaySearchedMessages=false;
	  searchText="";
	  String changeShow = (String) vce.getNewValue();
	  if (changeShow == null){
		  //threaded = false;
	      setErrorMessage(getResourceBundleString(FAILED_REND_MESSAGE));
	      return;
	  }
	  if (ENTIRE_MSG.equals(changeShow)){
		  //threaded = false;
		  selectedMessageShow = ENTIRE_MSG;
		  expandedView = true;
	      return;
	  }
	  else {
		  selectedMessageShow = SUBJECT_ONLY;
		  expandedView = false;
		  return;
	  }
  }
  
  public void processValueChangedForMessageOrganize(ValueChangeEvent vce){
  	if(selectedTopic == null)
  	{ 
  		LOG.debug("selectedTopic is null in processValueChangedForMessageOrganize");
  		return;
  	}

  	if (LOG.isDebugEnabled())
	      LOG.debug("processValueChangeForMessageView(ValueChangeEvent " + vce
	          + ")");
	  isDisplaySearchedMessages=false;
	  searchText="";
	  //expanded="false";
	  String changeOrganize = (String) vce.getNewValue();
	  
	  threadAnchorMessageId = null;
	  DiscussionTopic topic = null;
      topic = forumManager.getTopicById(selectedTopic.getTopic().getId());
      setSelectedForumForCurrentTopic(topic);
      selectedTopic = getDecoratedTopic(topic);
	  
	  if (changeOrganize == null){
		  //threaded = false;
	      setErrorMessage(getResourceBundleString(FAILED_REND_MESSAGE));
	      return;
	  }
	  if("thread".equals(changeOrganize)){
		  threaded = true;
		  orderAsc = true;
		  displayUnreadOnly = false;
	  } else if("date_desc".equals(changeOrganize)){
		  threaded = false;
		  orderAsc = false;
		  displayUnreadOnly = false;
	  } else if("date".equals(changeOrganize)){
		  orderAsc = true;
		  threaded = false;
		  displayUnreadOnly = false;
	  } else if ("unread".equals(changeOrganize)){
		  orderAsc = true;
		  threaded = false;
		  displayUnreadOnly = true;
	  }
		  
	  return;
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
  public String processActionMarkAllAsRead()
  {
	  return markAllMessages(true);
  }
  
  /**
   * @return
   */
  public String processActionMarkAllThreadAsRead()
  {
	  return markAllThreadAsRead(true);
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
      setErrorMessage(getResourceBundleString(LOST_ASSOCIATE));
      return ALL_MESSAGES;
    }
    List messages = selectedTopic.getMessages();
    if (messages == null || messages.size() < 1)
    {
      setErrorMessage(getResourceBundleString(NO_MARKED_READ_MESSAGE));
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

  private String markAllMessages(boolean readStatus)
  {
	  if (selectedTopic == null)
	    {
	      setErrorMessage(getResourceBundleString(LOST_ASSOCIATE));
	      return ALL_MESSAGES;
	    }
	    List messages = selectedTopic.getMessages();
	    if (messages == null || messages.size() < 1)
	    {
	      setErrorMessage(getResourceBundleString(NO_MARKED_READ_MESSAGE));
	      return ALL_MESSAGES;
	    }
	    Iterator iter = messages.iterator();
	    while (iter.hasNext())
	    {
	      DiscussionMessageBean decoMessage = (DiscussionMessageBean) iter.next();
	      forumManager.markMessageAs(decoMessage.getMessage(), readStatus);

	    }
	    //return displayTopicById(TOPIC_ID); // reconstruct topic again;
	    setSelectedForumForCurrentTopic(selectedTopic.getTopic());
        selectedTopic = getDecoratedTopic(selectedTopic.getTopic());
	    return processActionDisplayFlatView();
  }
  
  private String markAllThreadAsRead(boolean readStatus)
  {
	  if(selectedThreadHead == null){
		  setErrorMessage(getResourceBundleString(LOST_ASSOCIATE));
	      return ALL_MESSAGES;
	  }
	  if(selectedThread == null || selectedThread.size() < 1){
		  setErrorMessage(getResourceBundleString(NO_MARKED_READ_MESSAGE));
	      return ALL_MESSAGES;
	  }
	  Iterator iter = selectedThread.iterator();
	  while (iter.hasNext()){
		  DiscussionMessageBean decoMessage = (DiscussionMessageBean) iter.next();
		  forumManager.markMessageAs(decoMessage.getMessage(), readStatus);
	  }
	  return processActionGetDisplayThread();
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
	if (siteRoles == null || siteMembers == null){
		siteRoles = new ArrayList();
	    //for group awareness
	    //return getSiteMembers(false);
	  	siteRoles.addAll(getSiteMembers(true));
	}
	return siteRoles;
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
    	//membershipItems = forumManager.getDiscussionForumArea().getMembershipItemSet();
    	membershipItems = uiPermissionsManager.getAreaItemsSet(forumManager.getDiscussionForumArea());
    }
    else if (PERMISSION_MODE_FORUM.equals(getPermissionMode())){    	
    	if (selectedForum != null && selectedForum.getForum() != null)
    	{
    		membershipItems = uiPermissionsManager.getForumItemsSet(selectedForum.getForum());
        	if (membershipItems == null || membershipItems.size() == 0)
        	{
        		membershipItems = uiPermissionsManager.getAreaItemsSet(forumManager.getDiscussionForumArea());
        	}
    	}
    	else
    	{
    		membershipItems = uiPermissionsManager.getAreaItemsSet(forumManager.getDiscussionForumArea());
    	}
    }
    else if (PERMISSION_MODE_TOPIC.equals(getPermissionMode())){    	
    	if (selectedTopic != null && selectedTopic.getTopic() != null)
    	{
    		membershipItems = uiPermissionsManager.getTopicItemsSet(selectedTopic.getTopic());
    	}
    	if ((membershipItems == null || membershipItems.size() == 0)
					&& (selectedForum != null && selectedForum.getForum() != null)
					&& uiPermissionsManager != null)
			{
    			membershipItems = uiPermissionsManager.getForumItemsSet(selectedForum.getForum());
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
    	List rolesList = sortRoles(roles1);
    	
        Iterator roleIter = rolesList.iterator();
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
            siteMembers.add(new SelectItem(role.getId(), role.getId() + " ("+item.getPermissionLevelName()+")"));
            permissions.add(new PermissionBean(item, permissionLevelManager));
          }
        }
      }  
        
      if(includeGroup)
      {
    	  currentSite = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());   
      
    	  Collection groups = currentSite.getGroups();

    	  groups = sortGroups(groups);
    	  
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
    } catch (GroupNotDefinedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}   

    return siteMembers;
  }


  /**
   * Takes roles defined and sorts them alphabetically by id
   * so when displayed will be in order.
   * 
   * @param roles
   * 			Set of defined roles
   * 
   * @return
   * 			Set of defined roles sorted
   */
  private List sortRoles(Set roles) {
	  final List rolesList = new ArrayList();
	  
	  rolesList.addAll(roles);
	  
	  final AuthzGroupComparator authzGroupComparator = new AuthzGroupComparator("id", true);
	  
	  Collections.sort(rolesList, authzGroupComparator);
	  
	  return rolesList;
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
      forum = forumManager.getForumById(Long.valueOf(forumId));
      if (forum == null)
      {
        selectedForum = null;
        return;
      }
    }
    selectedForum = new DiscussionForumBean(forum, uiPermissionsManager, forumManager);
    if("true".equalsIgnoreCase(ServerConfigurationService.getString("mc.defaultLongDescription")))
    {
    	selectedForum.setReadFullDesciption(true);
    }

    setForumBeanAssign();
  }

  /**
   * @param errorMsg
   */
  private void setErrorMessage(String errorMsg)
  {
    LOG.debug("setErrorMessage(String " + errorMsg + ")");
    FacesContext.getCurrentInstance().addMessage(null,
        new FacesMessage(FacesMessage.SEVERITY_ERROR, getResourceBundleString(ALERT) + errorMsg, null));
  }
  
  private void setSuccessMessage(String successMsg)
  {
	  LOG.debug("setSuccessMessage(String " + successMsg + ")");
	  FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, successMsg, null));
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
  	  	
  	StringBuilder sBuffer = new StringBuilder();  	
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
  	Set oldMembershipItemSet = null;
    
  	DiscussionForum forum = null;
  	Area area = null;
  	//Topic topic = null;
  	DiscussionTopic topic = null;
  	
    /** get membership item set */    
    if (target instanceof DiscussionForum){
    	forum = ((DiscussionForum) target);
    	//membershipItemSet = forum.getMembershipItemSet();
    	//membershipItemSet = uiPermissionsManager.getForumItemsSet(forum);
    	oldMembershipItemSet = uiPermissionsManager.getForumItemsSet(forum);
    }
    else if (target instanceof Area){
    	area = ((Area) target);
    	//membershipItemSet = area.getMembershipItemSet();
    	//membershipItemSet = uiPermissionsManager.getAreaItemsSet();
    	oldMembershipItemSet = uiPermissionsManager.getAreaItemsSet(area);
    }
    else if (target instanceof Topic){
    	//topic = ((Topic) target);
    	//membershipItemSet = topic.getMembershipItemSet();
    	topic = ((DiscussionTopic) target);
    	//membershipItemSet = uiPermissionsManager.getTopicItemsSet(topic);
    	oldMembershipItemSet = uiPermissionsManager.getTopicItemsSet(topic);
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
        //for group awareness
        //DBMembershipItem membershipItem = permissionLevelManager.createDBMembershipItem(permBean.getItem().getName(), permBean.getSelectedLevel(), DBMembershipItem.TYPE_ROLE);
        DBMembershipItem membershipItem = permissionLevelManager.createDBMembershipItem(permBean.getItem().getName(), permBean.getSelectedLevel(), permBean.getItem().getType());
                if (PermissionLevelManager.PERMISSION_LEVEL_NAME_OWNER.equals(membershipItem.getPermissionLevelName())){
        	            PermissionsMask mask = new PermissionsMask();                
        	            mask.put(PermissionLevel.NEW_FORUM, Boolean.valueOf(permBean.getNewForum())); 
        	            mask.put(PermissionLevel.NEW_TOPIC, Boolean.valueOf(permBean.getNewTopic()));
        	            mask.put(PermissionLevel.NEW_RESPONSE, Boolean.valueOf(permBean.getNewResponse()));
        	            mask.put(PermissionLevel.NEW_RESPONSE_TO_RESPONSE, Boolean.valueOf(permBean.getResponseToResponse()));
        	            mask.put(PermissionLevel.MOVE_POSTING, Boolean.valueOf(permBean.getMovePosting()));
        	            mask.put(PermissionLevel.CHANGE_SETTINGS,Boolean.valueOf(permBean.getChangeSettings()));
        	            mask.put(PermissionLevel.POST_TO_GRADEBOOK, Boolean.valueOf(permBean.getPostToGradebook()));
        	            mask.put(PermissionLevel.READ, Boolean.valueOf(permBean.getRead()));
        	            mask.put(PermissionLevel.MARK_AS_READ,Boolean.valueOf(permBean.getMarkAsRead()));
        	            mask.put(PermissionLevel.MODERATE_POSTINGS, Boolean.valueOf(permBean.getModeratePostings()));
        	            mask.put(PermissionLevel.DELETE_OWN, Boolean.valueOf(permBean.getDeleteOwn()));
        	            mask.put(PermissionLevel.DELETE_ANY, Boolean.valueOf(permBean.getDeleteAny()));
        	            mask.put(PermissionLevel.REVISE_OWN, Boolean.valueOf(permBean.getReviseOwn()));
        	            mask.put(PermissionLevel.REVISE_ANY, Boolean.valueOf(permBean.getReviseAny()));
        	            
        	            PermissionLevel level = permissionLevelManager.createPermissionLevel(permBean.getSelectedLevel(), typeManager.getCustomLevelType(), mask);
        	            membershipItem.setPermissionLevel(level);
        	          }
        	        if (PermissionLevelManager.PERMISSION_LEVEL_NAME_AUTHOR.equals(membershipItem.getPermissionLevelName())){
        	            PermissionsMask mask = new PermissionsMask();                
        	            mask.put(PermissionLevel.NEW_FORUM, Boolean.valueOf(permBean.getNewForum())); 
        	            mask.put(PermissionLevel.NEW_TOPIC, Boolean.valueOf(permBean.getNewTopic()));
        	            mask.put(PermissionLevel.NEW_RESPONSE, Boolean.valueOf(permBean.getNewResponse()));
        	            mask.put(PermissionLevel.NEW_RESPONSE_TO_RESPONSE, Boolean.valueOf(permBean.getResponseToResponse()));
        	            mask.put(PermissionLevel.MOVE_POSTING, Boolean.valueOf(permBean.getMovePosting()));
        	            mask.put(PermissionLevel.CHANGE_SETTINGS,Boolean.valueOf(permBean.getChangeSettings()));
        	            mask.put(PermissionLevel.POST_TO_GRADEBOOK, Boolean.valueOf(permBean.getPostToGradebook()));
        	            mask.put(PermissionLevel.READ, Boolean.valueOf(permBean.getRead()));
        	            mask.put(PermissionLevel.MARK_AS_READ,Boolean.valueOf(permBean.getMarkAsRead()));
        	            mask.put(PermissionLevel.MODERATE_POSTINGS, Boolean.valueOf(permBean.getModeratePostings()));
        	            mask.put(PermissionLevel.DELETE_OWN, Boolean.valueOf(permBean.getDeleteOwn()));
        	            mask.put(PermissionLevel.DELETE_ANY, Boolean.valueOf(permBean.getDeleteAny()));
        	            mask.put(PermissionLevel.REVISE_OWN, Boolean.valueOf(permBean.getReviseOwn()));
        	            mask.put(PermissionLevel.REVISE_ANY, Boolean.valueOf(permBean.getReviseAny()));
        	            
        	            PermissionLevel level = permissionLevelManager.createPermissionLevel(permBean.getSelectedLevel(), typeManager.getCustomLevelType(), mask);
        	            membershipItem.setPermissionLevel(level);
        	          }
        	        if (PermissionLevelManager.PERMISSION_LEVEL_NAME_CONTRIBUTOR.equals(membershipItem.getPermissionLevelName())){
        	            PermissionsMask mask = new PermissionsMask();                
        	            mask.put(PermissionLevel.NEW_FORUM, Boolean.valueOf(permBean.getNewForum())); 
        	           mask.put(PermissionLevel.NEW_TOPIC, Boolean.valueOf(permBean.getNewTopic()));
        	           mask.put(PermissionLevel.NEW_RESPONSE, Boolean.valueOf(permBean.getNewResponse()));
        	            mask.put(PermissionLevel.NEW_RESPONSE_TO_RESPONSE, Boolean.valueOf(permBean.getResponseToResponse()));
        	            mask.put(PermissionLevel.MOVE_POSTING, Boolean.valueOf(permBean.getMovePosting()));
        	            mask.put(PermissionLevel.CHANGE_SETTINGS,Boolean.valueOf(permBean.getChangeSettings()));
        	            mask.put(PermissionLevel.POST_TO_GRADEBOOK, Boolean.valueOf(permBean.getPostToGradebook()));
        	            mask.put(PermissionLevel.READ, Boolean.valueOf(permBean.getRead()));
        	            mask.put(PermissionLevel.MARK_AS_READ,Boolean.valueOf(permBean.getMarkAsRead()));
        	            mask.put(PermissionLevel.MODERATE_POSTINGS, Boolean.valueOf(permBean.getModeratePostings()));
        	            mask.put(PermissionLevel.DELETE_OWN, Boolean.valueOf(permBean.getDeleteOwn()));
        	            mask.put(PermissionLevel.DELETE_ANY, Boolean.valueOf(permBean.getDeleteAny()));
        	            mask.put(PermissionLevel.REVISE_OWN, Boolean.valueOf(permBean.getReviseOwn()));
        	            mask.put(PermissionLevel.REVISE_ANY, Boolean.valueOf(permBean.getReviseAny()));
        	            
        	            PermissionLevel level = permissionLevelManager.createPermissionLevel(permBean.getSelectedLevel(), typeManager.getCustomLevelType(), mask);
        	            membershipItem.setPermissionLevel(level);
        	          }
        	        if (PermissionLevelManager.PERMISSION_LEVEL_NAME_REVIEWER.equals(membershipItem.getPermissionLevelName())){
        	            PermissionsMask mask = new PermissionsMask();                
        	            mask.put(PermissionLevel.NEW_FORUM, Boolean.valueOf(permBean.getNewForum())); 
        	            mask.put(PermissionLevel.NEW_TOPIC, Boolean.valueOf(permBean.getNewTopic()));
        	            mask.put(PermissionLevel.NEW_RESPONSE, Boolean.valueOf(permBean.getNewResponse()));
        	            mask.put(PermissionLevel.NEW_RESPONSE_TO_RESPONSE, Boolean.valueOf(permBean.getResponseToResponse()));
        	            mask.put(PermissionLevel.MOVE_POSTING, Boolean.valueOf(permBean.getMovePosting()));
        	            mask.put(PermissionLevel.CHANGE_SETTINGS,Boolean.valueOf(permBean.getChangeSettings()));
        	            mask.put(PermissionLevel.POST_TO_GRADEBOOK, Boolean.valueOf(permBean.getPostToGradebook()));
        	            mask.put(PermissionLevel.READ, Boolean.valueOf(permBean.getRead()));
        	            mask.put(PermissionLevel.MARK_AS_READ,Boolean.valueOf(permBean.getMarkAsRead()));
        	            mask.put(PermissionLevel.MODERATE_POSTINGS, Boolean.valueOf(permBean.getModeratePostings()));
        	            mask.put(PermissionLevel.DELETE_OWN, Boolean.valueOf(permBean.getDeleteOwn()));
        	            mask.put(PermissionLevel.DELETE_ANY, Boolean.valueOf(permBean.getDeleteAny()));
        	            mask.put(PermissionLevel.REVISE_OWN, Boolean.valueOf(permBean.getReviseOwn()));
        	            mask.put(PermissionLevel.REVISE_ANY, Boolean.valueOf(permBean.getReviseAny()));
        	            
        	            PermissionLevel level = permissionLevelManager.createPermissionLevel(permBean.getSelectedLevel(), typeManager.getCustomLevelType(), mask);
        	            membershipItem.setPermissionLevel(level);
        	          }
        	        if (PermissionLevelManager.PERMISSION_LEVEL_NAME_NONEDITING_AUTHOR.equals(membershipItem.getPermissionLevelName())){
        	            PermissionsMask mask = new PermissionsMask();                
        	            mask.put(PermissionLevel.NEW_FORUM, Boolean.valueOf(permBean.getNewForum())); 
        	            mask.put(PermissionLevel.NEW_TOPIC, Boolean.valueOf(permBean.getNewTopic()));
        	            mask.put(PermissionLevel.NEW_RESPONSE, Boolean.valueOf(permBean.getNewResponse()));
        	            mask.put(PermissionLevel.NEW_RESPONSE_TO_RESPONSE, Boolean.valueOf(permBean.getResponseToResponse()));
        	            mask.put(PermissionLevel.MOVE_POSTING, Boolean.valueOf(permBean.getMovePosting()));
        	            mask.put(PermissionLevel.CHANGE_SETTINGS,Boolean.valueOf(permBean.getChangeSettings()));
        	            mask.put(PermissionLevel.POST_TO_GRADEBOOK, Boolean.valueOf(permBean.getPostToGradebook()));
        	            mask.put(PermissionLevel.READ, Boolean.valueOf(permBean.getRead()));
        	            mask.put(PermissionLevel.MARK_AS_READ,Boolean.valueOf(permBean.getMarkAsRead()));
        	            mask.put(PermissionLevel.MODERATE_POSTINGS, Boolean.valueOf(permBean.getModeratePostings()));
        	            mask.put(PermissionLevel.DELETE_OWN, Boolean.valueOf(permBean.getDeleteOwn()));
        	            mask.put(PermissionLevel.DELETE_ANY, Boolean.valueOf(permBean.getDeleteAny()));
        	            mask.put(PermissionLevel.REVISE_OWN, Boolean.valueOf(permBean.getReviseOwn()));
        	            mask.put(PermissionLevel.REVISE_ANY, Boolean.valueOf(permBean.getReviseAny()));
        	            
        	            PermissionLevel level = permissionLevelManager.createPermissionLevel(permBean.getSelectedLevel(), typeManager.getCustomLevelType(), mask);
        	            membershipItem.setPermissionLevel(level);
        	          }
        	        if (PermissionLevelManager.PERMISSION_LEVEL_NAME_NONE.equals(membershipItem.getPermissionLevelName())){
        	            PermissionsMask mask = new PermissionsMask();                
        	            mask.put(PermissionLevel.NEW_FORUM, Boolean.valueOf(permBean.getNewForum())); 
        	            mask.put(PermissionLevel.NEW_TOPIC, Boolean.valueOf(permBean.getNewTopic()));
        	            mask.put(PermissionLevel.NEW_RESPONSE, Boolean.valueOf(permBean.getNewResponse()));
        	            mask.put(PermissionLevel.NEW_RESPONSE_TO_RESPONSE, Boolean.valueOf(permBean.getResponseToResponse()));
        	            mask.put(PermissionLevel.MOVE_POSTING, Boolean.valueOf(permBean.getMovePosting()));
        	            mask.put(PermissionLevel.CHANGE_SETTINGS,Boolean.valueOf(permBean.getChangeSettings()));
        	            mask.put(PermissionLevel.POST_TO_GRADEBOOK, Boolean.valueOf(permBean.getPostToGradebook()));
        	            mask.put(PermissionLevel.READ, Boolean.valueOf(permBean.getRead()));
        	            mask.put(PermissionLevel.MARK_AS_READ,Boolean.valueOf(permBean.getMarkAsRead()));
        	            mask.put(PermissionLevel.MODERATE_POSTINGS, Boolean.valueOf(permBean.getModeratePostings()));
        	            mask.put(PermissionLevel.DELETE_OWN, Boolean.valueOf(permBean.getDeleteOwn()));
        	            mask.put(PermissionLevel.DELETE_ANY, Boolean.valueOf(permBean.getDeleteAny()));
        	            mask.put(PermissionLevel.REVISE_OWN, Boolean.valueOf(permBean.getReviseOwn()));
        	            mask.put(PermissionLevel.REVISE_ANY, Boolean.valueOf(permBean.getReviseAny()));
        	            
        	            PermissionLevel level = permissionLevelManager.createPermissionLevel(permBean.getSelectedLevel(), typeManager.getCustomLevelType(), mask);
        	            membershipItem.setPermissionLevel(level);
        	          }
        
        if (PermissionLevelManager.PERMISSION_LEVEL_NAME_CUSTOM.equals(membershipItem.getPermissionLevelName())){
          PermissionsMask mask = new PermissionsMask();                
          mask.put(PermissionLevel.NEW_FORUM, Boolean.valueOf(permBean.getNewForum())); 
          mask.put(PermissionLevel.NEW_TOPIC, Boolean.valueOf(permBean.getNewTopic()));
          mask.put(PermissionLevel.NEW_RESPONSE, Boolean.valueOf(permBean.getNewResponse()));
          mask.put(PermissionLevel.NEW_RESPONSE_TO_RESPONSE, Boolean.valueOf(permBean.getResponseToResponse()));
          mask.put(PermissionLevel.MOVE_POSTING, Boolean.valueOf(permBean.getMovePosting()));
          mask.put(PermissionLevel.CHANGE_SETTINGS,Boolean.valueOf(permBean.getChangeSettings()));
          mask.put(PermissionLevel.POST_TO_GRADEBOOK, Boolean.valueOf(permBean.getPostToGradebook()));
          mask.put(PermissionLevel.READ, Boolean.valueOf(permBean.getRead()));
          mask.put(PermissionLevel.MARK_AS_READ,Boolean.valueOf(permBean.getMarkAsRead()));
          mask.put(PermissionLevel.MODERATE_POSTINGS, Boolean.valueOf(permBean.getModeratePostings()));
          mask.put(PermissionLevel.DELETE_OWN, Boolean.valueOf(permBean.getDeleteOwn()));
          mask.put(PermissionLevel.DELETE_ANY, Boolean.valueOf(permBean.getDeleteAny()));
          mask.put(PermissionLevel.REVISE_OWN, Boolean.valueOf(permBean.getReviseOwn()));
          mask.put(PermissionLevel.REVISE_ANY, Boolean.valueOf(permBean.getReviseAny()));
          
          PermissionLevel level = permissionLevelManager.createPermissionLevel(permBean.getSelectedLevel(), typeManager.getCustomLevelType(), mask);
          membershipItem.setPermissionLevel(level);
        }
        
                
        // save DBMembershiptItem here to get an id so we can add to the set
        permissionLevelManager.saveDBMembershipItem(membershipItem);          
        
        
        membershipItemSet.add(membershipItem);
      }
      
      if( ((area != null && area.getId() != null) || 
      		(forum != null && forum.getId() != null) || 
      		(topic != null && topic.getId() != null)) 
      		&& oldMembershipItemSet != null)
      	permissionLevelManager.deleteMembershipItems(oldMembershipItemSet);
      
      if (target instanceof DiscussionForum){
      	forum.setMembershipItemSet(membershipItemSet);
      	
      //	if (ThreadLocalManager.get("message_center_permission_set") == null || !((Boolean)ThreadLocalManager.get("message_center_permission_set")).booleanValue())
		//{
			//this.uiPermissionsManager.initMembershipForSite();
	//	}

		//Set forumItemsInThread = (Set) ThreadLocalManager.get("message_center_membership_forum");
     //ThreadLocalManager.set("message_center_membership_forum",membershipItemSet);
		//Set thisForumItemSet = new HashSet();
		//Iterator iter = forumItemsInThread.iterator();
		//thisForumItemSet.add((DBMembershipItem)thisItem);
		
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
        /** store caller navigation string in session (used to return from add groups/users) */
        session.setAttribute("MC_ADD_GROUPS_USERS_CALLER", attr);
    }
                  
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
        if (postingOptions == null){
	        postingOptions = new ArrayList();
	        postingOptions.add(new SelectItem(PermissionBean.getResourceBundleString(PermissionBean.NONE),
	        									PermissionBean.getResourceBundleString(PermissionBean.NONE)));
	        postingOptions.add(new SelectItem(PermissionBean.getResourceBundleString(PermissionBean.OWN),
	        									PermissionBean.getResourceBundleString(PermissionBean.OWN)));
	        postingOptions.add(new SelectItem(PermissionBean.getResourceBundleString(PermissionBean.ALL),
	        									PermissionBean.getResourceBundleString(PermissionBean.ALL)));
        }
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
             if("Custom".equals(level))
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

		/**
		 * Pulls messages from bundle
		 * 
		 * @param key
		 * 			Key of message to get
		 * 
		 * @return
		 * 			String for key passed in or [missing: key] if not found
		 */
	    public static String getResourceBundleString(String key) 
	    {
	        final ResourceLoader rb = new ResourceLoader(MESSAGECENTER_BUNDLE);
	        return rb.getString(key);
	    }

	    public static String getResourceBundleString(String key, Object[] args) {
	    	final ResourceLoader rb = new ResourceLoader(MESSAGECENTER_BUNDLE);
	    	return rb.getFormattedMessage(key, args);
	    }

		public boolean getGradebookExist() 
		{
			if (!gradebookExistChecked)
			{
		 	    try 
			    { 
		 	    	GradebookService gradebookService = (org.sakaiproject.service.gradebook.shared.GradebookService) 
		 	    	ComponentManager.get("org.sakaiproject.service.gradebook.GradebookService");
		 	    	gradebookExist = gradebookService.isGradebookDefined(ToolManager.getCurrentPlacement().getContext());
		 	    	gradebookExistChecked = true;
		 	    	return gradebookExist;
			    }
		 	    catch(Exception e)
		 	    {
		 	    	gradebookExist = false;
		 	    	gradebookExistChecked = true;
		 	    	return gradebookExist;
		 	    }
			}
		 	else
		 	{
		 		return gradebookExist;
		 	}
		}

		public void setGradebookExist(boolean gradebookExist) 
		{
			this.gradebookExist = gradebookExist;
		}

	public String getUserNameOrEid()
	{
	  try
	  {
		String currentUserId = getUserId();
  	    
  	    
		  String userString = "";
		  userString = UserDirectoryService.getUser(currentUserId).getDisplayName();
		  String userEidString = "";
		  userEidString = UserDirectoryService.getUser(currentUserId).getDisplayId();
		  
		  if((userString != null && userString.length() > 0) && ServerConfigurationService.getBoolean("msg.displayEid", true))
		  {
			  return userString + " (" + userEidString + ")";
		  }
		  else if ((userString != null && userString.length() > 0) && !ServerConfigurationService.getBoolean("msg.displayEid", true))
		  {
			  return userString;
		  }
		  else
		  {
			return userEidString;
		  }
		
	  }
  	  catch(Exception e)
  	  {
  		e.printStackTrace();
  	  }
  	  
  	  return getUserId();
	}
	
 	


	public boolean isDisableLongDesc()
	{
		return disableLongDesc;
	}
	
	/**
	 * Determines current level (template, forum, or topic) and
	 * returns boolean indicating whether moderating is enabled or not.
	 * @return
	 */
	public boolean isDisableModeratePerm()
	{
		if (permissionMode == null)
			return true;
		else if (PERMISSION_MODE_TEMPLATE.equals(permissionMode) && template != null)
			return !template.isAreaModerated();
		else if (PERMISSION_MODE_FORUM.equals(permissionMode) && selectedForum != null)
			return !selectedForum.isForumModerated();
		else if (PERMISSION_MODE_TOPIC.equals(permissionMode) && selectedTopic != null)
			return !selectedTopic.isTopicModerated();
		else
			return true;
	}
	
	/**
	 * With ability to delete messages, need to filter out
	 * messages that are from print friendly view so we don't
	 * need to do it within the UI rendering.
	 */
	public List getpFMessages() 
	{
		List results = new ArrayList();
		List messages = getMessages();
		
		for (Iterator iter = messages.iterator(); iter.hasNext();)
		{
			DiscussionMessageBean message = (DiscussionMessageBean) iter.next();
			
			if (!message.getDeleted()) {
				results.add(message);
			}
		}
		
		return results;
	}

	/**
	 * Returns list of DecoratedMessageBean objects, ie, the messages
	 */
	public List getMessages() 
	{
		List messages = new ArrayList();
		
		//if(displayUnreadOnly) 
		//	messages = selectedTopic.getUnreadMessages();	
		//else
		
  	if(selectedTopic == null)
  	{ 
  		LOG.debug("selectedTopic is null in getMessages");
  		return messages;
  	}
  	
		messages = selectedTopic.getMessages();
		
		if (messages != null && !messages.isEmpty())
			messages = filterModeratedMessages(messages, selectedTopic.getTopic(), selectedForum.getForum());

		return messages;
	}

	/**
	 * Given a list of messages, will return all messages that meet at
	 * least one of the following criteria:
	 * 1) message is approved
	 * 2) message was written by current user
	 * 3) current user has moderator perm
	 */
	private List filterModeratedMessages(List messages, DiscussionTopic topic, DiscussionForum forum)
	{
		List viewableMsgs = new ArrayList();
		if (messages != null && messages.size() > 0)
		{
			if (forum == null) 
			{
				forum = selectedForum.getForum();
			}

			boolean hasModeratePerm = uiPermissionsManager.isModeratePostings(topic, forum);
			
			if (hasModeratePerm)
				return messages;
			
			Iterator msgIter = messages.iterator();
			while (msgIter.hasNext())
			{
				DiscussionMessageBean msg = (DiscussionMessageBean) msgIter.next();
				if (msg.isMsgApproved() || msg.getIsOwn())
					viewableMsgs.add(msg);
			}
		}
		
		return viewableMsgs;
	}
	
	public String getModeratorComments()
	{
		return moderatorComments;
	}

	public void setModeratorComments(String moderatorComments)
	{
		this.moderatorComments = moderatorComments;
	}
	
   public UIData getForumTable(){
      return forumTable;
   }

   public void setForumTable(UIData forumTable){
      this.forumTable=forumTable;
   }
   
   public String processReturnToOriginatingPage()
   {
	   LOG.debug("processReturnToOriginatingPage()");
	   if(fromPage != null)
	   {
		   String returnToPage = fromPage;
		   fromPage = "";
		   if(ALL_MESSAGES.equals(returnToPage) && selectedTopic != null)
		   {
			   selectedTopic = getDecoratedTopic(selectedTopic.getTopic());
			   return ALL_MESSAGES;
		   }
		   if(FORUM_DETAILS.equals(returnToPage) && selectedForum != null)
		   {
			   selectedForum = getDecoratedForum(selectedForum.getForum());
			   return FORUM_DETAILS;
		   }
	   }

	   return processActionHome();
   }

   private void setFromMainOrForumOrTopic()
   {
	   String originatingPage = getExternalParameterByKey(FROM_PAGE);
	   if(originatingPage != null && (MAIN.equals(originatingPage) || ALL_MESSAGES.equals(originatingPage) || FORUM_DETAILS.equals(originatingPage)
			   	|| THREAD_VIEW.equals(originatingPage) || FLAT_VIEW.equals(originatingPage)))
	   {
		   fromPage = originatingPage;
	   }
   }
   
	/**
	 * @return TRUE if within Messages & Forums tool, FALSE otherwise
	 */
	public boolean isMessagesandForums() {	
		if (messagesandForums == null){
			messagesandForums = messageManager.currentToolMatch(MESSAGECENTER_TOOL_ID);
		}
		return messagesandForums.booleanValue();
	}
	
	/**
	 * @return TRUE if within Forums tool, FALSE otherwise
	 */
	public boolean isForumsTool() {
		if (forumsTool == null){
			forumsTool = messageManager.currentToolMatch(FORUMS_TOOL_ID);
		}
		return forumsTool;
	}
	
   private String gotoMain() {
	    if (isForumsTool()) {
	    	return FORUMS_MAIN;
	    }
	    else {
	    	return MAIN;
	    }
   }
   
	/**
	 * @return TRUE if Messages & Forums (Message Center) exists in this site,
	 *         FALSE otherwise
	 */
	private boolean isMessageForumsPageInSite(String siteId) {
		return messageManager.isToolInSite(siteId, MESSAGECENTER_TOOL_ID);
	}
	
	/**
	 * @return TRUE if Messages & Forums (Message Center) exists in this site,
	 *         FALSE otherwise
	 */
	private boolean isForumsPageInSite(String siteId) {
		return messageManager.isToolInSite(siteId, FORUMS_TOOL_ID);
	}
	
	 public String getPrintFriendlyUrl()
	  {
		  return ServerConfigurationService.getToolUrl() + Entity.SEPARATOR
						+ ToolManager.getCurrentPlacement().getId() + Entity.SEPARATOR + "discussionForum" 
						+ Entity.SEPARATOR + "message" + Entity.SEPARATOR 
						+ "printFriendly";
	  }
	 
	 public String getPrintFriendlyUrlThread()
	  {
		  return ServerConfigurationService.getToolUrl() + Entity.SEPARATOR
						+ ToolManager.getCurrentPlacement().getId() + Entity.SEPARATOR + "discussionForum" 
						+ Entity.SEPARATOR + "message" + Entity.SEPARATOR 
						+ "printFriendlyThread";
	  }
	 
	 public String getPrintFriendlyAllAuthoredMsg()
	  {
		  return ServerConfigurationService.getToolUrl() + Entity.SEPARATOR
						+ ToolManager.getCurrentPlacement().getId() + Entity.SEPARATOR + "discussionForum" 
						+ Entity.SEPARATOR + "statistics" + Entity.SEPARATOR 
						+ "printFriendlyAllAuthoredMsg";
	  }
	 
	 public String getPrintFriendlyFullTextForOne()
	  {
		  return ServerConfigurationService.getToolUrl() + Entity.SEPARATOR
						+ ToolManager.getCurrentPlacement().getId() + Entity.SEPARATOR + "discussionForum" 
						+ Entity.SEPARATOR + "statistics" + Entity.SEPARATOR 
						+ "printFriendlyFullTextForOne";
	  }
	 
	 public String getPrintFriendlyDisplayInThread()
	  {
		  return ServerConfigurationService.getToolUrl() + Entity.SEPARATOR
						+ ToolManager.getCurrentPlacement().getId() + Entity.SEPARATOR + "discussionForum" 
						+ Entity.SEPARATOR + "statistics" + Entity.SEPARATOR 
						+ "printFriendlyDisplayInThread";
	  }
	 
	 public Boolean isMessageReadForUser(Long topicId, Long messageId)
	 {
		 return messageManager.isMessageReadForUser(topicId, messageId);
	 }
	 
	 public void markMessageReadForUser(Long topicId, Long messageId, Boolean read)
	 {
		 messageManager.markMessageReadForUser(topicId, messageId, read);
		 if(selectedThreadHead != null){
			 //reset the thread to show unread
			 processActionGetDisplayThread();
		 }
		 //also go ahead and reset the the topic
		 DiscussionTopic topic = forumManager.getTopicById(Long.valueOf(topicId));
		 setSelectedForumForCurrentTopic(topic);
		 selectedTopic = getDecoratedTopic(topic);
		 setTopicBeanAssign();
		 getSelectedTopic();
	 }
	 
	 /**
	  * Used to refresh any settings (such as revise) that need to be refreshed
	  * after various actions that re-set the selectedMessage (navigating to prev/next msg, moderating, etc) 
	  * @param message
	  */
	 private void refreshSelectedMessageSettings(Message message) {
		 if(selectedTopic == null)
		 { 
			 LOG.debug("selectedTopic is null in refreshSelectedMessageSettings");
			 return;
		 }
		 boolean isOwn = message.getCreatedBy().equals(getUserId());
		 selectedMessage.setRevise(selectedTopic.getIsReviseAny() 
					|| (selectedTopic.getIsReviseOwn() && isOwn));  
		 selectedMessage.setUserCanDelete(selectedTopic.getIsDeleteAny() || (isOwn && selectedTopic.getIsDeleteOwn()));
		 selectedMessage.setUserCanEmail(isInstructor() || isSectionTA());
	 }
	 
	 public boolean isAllowedToGradeItem() {
		 return allowedToGradeItem;
	 }
	 public boolean isSelGBItemRestricted() {
		 return selGBItemRestricted;
	 }
	 public boolean isNoItemSelected() {
		 return selectedAssign == null || DEFAULT_GB_ITEM.equalsIgnoreCase(selectedAssign);
	 }
	 
	 public boolean getShowForumLinksInNav() {
		 return showForumLinksInNav;
	 }


	 
	 public String processActionShowFullTextForAll() {
		 return "dfStatisticsAllAuthoredMessageForOneUser";
	 }
	 
	 public String processActionDisplayInThread() {

		 String forumId = getExternalParameterByKey("forumId");
		 String topicId = getExternalParameterByKey("topicId");
		 selectedMsgId = getExternalParameterByKey("msgId");
		 DiscussionForum forum = forumManager.getForumById(Long.valueOf(forumId));
		 DiscussionTopic topic = forumManager.getTopicById(Long.valueOf(topicId));
		 setSelectedForumForCurrentTopic(topic);		
		 selectedTopic = getDecoratedTopic(topic);

		 if (uiPermissionsManager.isRead((DiscussionTopic)topic, forum)) {
			 List messageList = messageManager.findMessagesByTopicId(topic.getId());
			 Iterator messageIter = messageList.iterator();
			 while(messageIter.hasNext()){
				 Message mes = (Message) messageIter.next();					
				 messageManager.markMessageReadForUser(topic.getId(), mes.getId(), true, getUserId());
			 }
		 }

		 return "dfStatisticsDisplayInThread";
	 }

	 public String processActionDisplayFullText() {
		 return "dfStatisticsFullTextForOne";
	 }

	 public String getSelectedMsgId() {
		 return selectedMsgId;
	 }

	 public void setSelectedMsgId(String selectedMsgId) {
		 this.selectedMsgId = selectedMsgId;
	 }
	 
	 
	 
	 
	 // STANFORD: added for email notification feature 
	public void setEmailNotificationManager(
				EmailNotificationManager emailnotifyManager) {
			this.emailNotificationManager = emailnotifyManager;
	}
		
	public EmailNotificationBean getWatchSettingsBean() {
		return watchSettingsBean;
	}
		
	public void setWatchSettingsBean(EmailNotificationBean watchSettingsBean) {
		this.watchSettingsBean = watchSettingsBean;
	}

	public String processActionWatch() {
		LOG.debug("processActionWatch()");
		User curruser = UserDirectoryService.getCurrentUser();
		LOG.debug("got user: " + curruser.getDisplayId());
		EmailNotification userwatchoption = emailNotificationManager.getEmailNotification(curruser.getId());
		LOG.debug("userwatchoption = " + userwatchoption.getNotificationLevel());
		if (watchSettingsBean == null){
			LOG.debug("watchsettingbean = null");
			watchSettingsBean = new EmailNotificationBean(userwatchoption);
		}
		watchSettingsBean.setEmailNotification(userwatchoption);
		LOG.debug("watchsettingbean's user = " + watchSettingsBean.getEmailNotification().getUserId() + "  ,emailoption= " + 
				watchSettingsBean.getEmailNotification().getNotificationLevel());
		return WATCH_SETTING;
	}
	
	
	public String processActionSaveEmailNotificationOption() {
		LOG.debug("ForumTool.processActionSaveEmailNotificationOption()");
		if ((watchSettingsBean !=null) && (watchSettingsBean.getEmailNotification()!=null)){
			LOG.debug("watchSettingsBean !=null) && (watchSettingsBean.getEmailNotification()!=null");
			EmailNotification newoption = watchSettingsBean.getEmailNotification();
			emailNotificationManager.saveEmailNotification(newoption);
		}
		else {
			LOG.debug("ForumTool.processActionSaveEmailNotificationOption(): Can not save because watchSettingsBean is null");
			// should come here
		}
		
		return gotoMain();
	}
	
	public List<String> getUserEmailsToBeNotifiedByLevel(List userlist) {
		List<String> emaillist = new ArrayList<String>();
		
		
		List<User> userMailList  = UserDirectoryService.getUsers(userlist);
		for (int i = 0; i < userMailList.size(); i++) {
			User user = userMailList.get(i); 


			String useremail = user.getEmail();
			if (useremail != null && !"".equalsIgnoreCase(useremail)) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Username = " + user.getDisplayId()
							+ " , useremail : " + useremail);
				}
				emaillist.add(useremail);
			}


		}
		
		return emaillist;
		
	}
	
	public void  sendEmailNotification(Message reply, DiscussionMessageBean currthread){
		LOG.debug("ForumTool.sendEmailNotification()");
		
		// get all users with notification level = 2
		List<String> userlist = emailNotificationManager.getUsersToBeNotifiedByLevel( EmailNotification.EMAIL_REPLY_TO_ANY_MESSAGE);
		
		if (LOG.isDebugEnabled()){
			LOG.debug("total count of Level 2 users = " + userlist.size());
			Iterator iter1 = userlist.iterator();
			while (iter1.hasNext()){
				LOG.debug("level 2 users notify all msg:  sendEmailNotification: sending to  " + (String) iter1.next());
			}
		}
		
		// need to get a list of authors for all messages on the current thread, and then check their notification level. 
		//selectedThread  is a list of DiscussionMessageBean in the current thread.
		
		Iterator iter = selectedThread.iterator();
		while (iter.hasNext()){
			DiscussionMessageBean decoMessage = (DiscussionMessageBean) iter.next();
			String threadauthor = decoMessage.getMessage().getCreatedBy();
			EmailNotification authorNotificationLevel = emailNotificationManager.getEmailNotification(threadauthor);
			// only add level 1 users , since we've already got level2 users. 
			if (EmailNotification.EMAIL_REPLY_TO_MY_MESSAGE.equalsIgnoreCase(authorNotificationLevel.getNotificationLevel())){
				if (LOG.isDebugEnabled()){
					LOG.debug("The author: " + threadauthor + " wants to be notified");
				}
				userlist.add(threadauthor);
			}
			
		}
		// now printing out all users = # of messages in the thread - level 2 users
		
		if (LOG.isDebugEnabled()){
			LOG.debug("now printing out all users, including duplicates count = " + userlist.size());
			Iterator iter1 = userlist.iterator();
			while (iter1.hasNext()){
				LOG.debug("sendEmailNotification: should include both level 1 and level 2 sending to  " + (String) iter1.next());
			}
		}
		
		// now we need to remove duplicates:
		Set<String> set = new HashSet<String>();
		set.addAll(userlist);
		
//		avoid overhead :D
			LOG.debug("set size " + set.size());
			LOG.debug("userlist size " + userlist.size());
		if(set.size() < userlist.size()) {
			userlist.clear();
			userlist.addAll(set);
		}
		
		// now printing out all users again after removing duplicate
		
		if (LOG.isDebugEnabled()){
			LOG.debug("now printing out all users again after removing duplicate count = " + userlist.size());
			Iterator iter1 = userlist.iterator();
			while (iter1.hasNext()){
				LOG.debug("" + (String) iter1.next());
			}
		}
		
		//now we need to filer the list\
		if (LOG.isDebugEnabled())
			LOG.debug("About to filter list");
		List<String> finalList = emailNotificationManager.filterUsers(userlist, currthread.getMessage().getTopic());
		
		List<String> useremaillist =  getUserEmailsToBeNotifiedByLevel(finalList);
		
		
		
		if (LOG.isDebugEnabled()){
			LOG.debug("now printint unique emails , count = " + useremaillist.size());
			Iterator useremaillistiter = useremaillist.iterator();
			while (useremaillistiter.hasNext()){
				LOG.debug("sendEmailNotification: sending to  " + (String) useremaillistiter.next());
			}
		}
		
		if (userlist.isEmpty()) {
			LOG.debug("No users need to notified.");
			return;
		}
		
		ForumsEmailService emailService = new ForumsEmailService(useremaillist, reply, currthread);
		emailService.send();
		
		
	}
	
	DeveloperHelperService developerHelperService;
	
	private DeveloperHelperService getDevelperHelperService() {
		if (developerHelperService == null) {
			developerHelperService = (DeveloperHelperService) ComponentManager.get("org.sakaiproject.entitybroker.DeveloperHelperService");
		}
		return developerHelperService;
	}
	
	
	public String getMessageURL() {
		
		String path = "/discussionForum/message/dfViewMessageDirect";
		
		Map<String, String> params = new HashMap<String, String>();
		String msgId = getSelectedMessage().getMessage().getId().toString();
		String topicId = getSelectedTopic().getTopic().getId().toString();
		String forumId = getSelectedTopic().getTopic().getOpenForum().getId().toString();
		params.put("messageId", msgId);
		params.put("topicId", topicId);
		params.put("forumId", forumId);
		LOG.info("message: " + msgId + " topic: " + topicId + " forum: " + forumId);
		
		String context = SiteService.siteReference(ToolManager.getCurrentPlacement().getContext());
		LOG.info("context: " + context);
		
		developerHelperService = getDevelperHelperService();
		String url = developerHelperService.getToolViewURL("sakai.forums", path, params, context);
		LOG.info("url: " + url);
		return url;
	}
	
	
	private class ThreadUpdateSorter implements Comparator<DiscussionMessageBean>{

	    public int compare(DiscussionMessageBean dmb1, DiscussionMessageBean dmb2) {
	        return dmb2.getMessage().getDateThreadlastUpdated().compareTo(dmb1.getMessage().getDateThreadlastUpdated());
	    }
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

	public SynopticMsgcntrManager getSynopticMsgcntrManager() {
		return synopticMsgcntrManager;
	}

	public void setSynopticMsgcntrManager(
			SynopticMsgcntrManager synopticMsgcntrManager) {
		this.synopticMsgcntrManager = synopticMsgcntrManager;
	}
}

