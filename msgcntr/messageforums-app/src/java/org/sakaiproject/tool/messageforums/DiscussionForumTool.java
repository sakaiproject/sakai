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

import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.faces.bean.ManagedProperty;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIData;
import javax.faces.component.UIInput;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.persistence.OptimisticLockException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.api.app.messageforums.AnonymousManager;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.BulkPermission;
import org.sakaiproject.api.app.messageforums.DBMembershipItem;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.EmailNotification;
import org.sakaiproject.api.app.messageforums.EmailNotificationManager;
import org.sakaiproject.api.app.messageforums.MembershipItem;
import org.sakaiproject.api.app.messageforums.MembershipManager;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.MutableEntity;
import org.sakaiproject.api.app.messageforums.OpenForum;
import org.sakaiproject.api.app.messageforums.PermissionLevel;
import org.sakaiproject.api.app.messageforums.PermissionLevelManager;
import org.sakaiproject.api.app.messageforums.PermissionsMask;
import org.sakaiproject.api.app.messageforums.Rank;
import org.sakaiproject.api.app.messageforums.RankImage;
import org.sakaiproject.api.app.messageforums.RankManager;
import org.sakaiproject.api.app.messageforums.SynopticMsgcntrManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.cover.ForumScheduleNotificationCover;
import org.sakaiproject.api.app.messageforums.cover.SynopticMsgcntrManagerCover;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.calendar.api.CalendarConstants;
import org.sakaiproject.component.app.messageforums.dao.hibernate.DBMembershipItemImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.util.comparator.ForumBySortIndexAscAndCreatedDateDesc;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.util.SakaiToolData;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Statement;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Verb.SAKAI_VERB;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.grading.api.AssessmentNotFoundException;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.GradingConstants;
import org.sakaiproject.grading.api.GradeDefinition;
import org.sakaiproject.grading.api.GradeType;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.grading.api.SortType;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.model.GradebookAssignment;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tasks.api.Priorities;
import org.sakaiproject.tasks.api.Task;
import org.sakaiproject.tasks.api.TaskService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.messageforums.ui.DecoratedAttachment;
import org.sakaiproject.tool.messageforums.ui.DiscussionAreaBean;
import org.sakaiproject.tool.messageforums.ui.DiscussionForumBean;
import org.sakaiproject.tool.messageforums.ui.DiscussionMessageBean;
import org.sakaiproject.tool.messageforums.ui.DiscussionTopicBean;
import org.sakaiproject.tool.messageforums.ui.EmailNotificationBean;
import org.sakaiproject.tool.messageforums.ui.ForumRankBean;
import org.sakaiproject.tool.messageforums.ui.PermissionBean;
import org.sakaiproject.tool.messageforums.ui.SiteGroupBean;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.NumberUtil;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.util.comparator.GroupTitleComparator;
import org.sakaiproject.util.comparator.RoleIdComparator;

import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException;
import org.sakaiproject.rubrics.api.RubricsConstants;
import org.sakaiproject.rubrics.api.RubricsService;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import org.sakaiproject.api.app.messageforums.events.ForumsMessageEventParams;
import org.sakaiproject.api.app.messageforums.events.ForumsTopicEventParams;
import static org.sakaiproject.component.app.messageforums.dao.hibernate.MessageImpl.DATE_COMPARATOR;

/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 * @author Chen wen
 */
@Slf4j
@Setter
@Getter
@ManagedBean(name="ForumTool")
@SessionScoped
public class DiscussionForumTool {

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
  private static final String MESSAGE_MOVE_THREADS= "dfMoveThreads";
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
  private String selectedGradedUserId;
  private DiscussionAreaBean template;
  private DiscussionMessageBean selectedThreadHead;
  private List selectedThread = new ArrayList();
  private UIData forumTable;
  private List totalGroupsUsersList;
  private List selectedGroupsUsersList;
  private Map<String, MembershipItem> courseMemberMap;
  private List<PermissionBean> permissions;
  private List levels;
  @ManagedProperty(value="#{Components[\"org.sakaiproject.api.app.messageforums.AreaManager\"]}")
  private AreaManager areaManager;
  private int numPendingMessages = 0;
  
  private static final String TOPIC_ID = "topicId";
  private static final String FORUM_ID = "forumId";
  private static final String USER_ID = "userId";
  private static final String MESSAGE_ID = "messageId";
  private static final String CURRENT_MESSAGE_ID = "currentMessageId";
  private static final String CURRENT_TOPIC_ID = "currentTopicId";
  private static final String CURRENT_FORUM_ID = "currentForumId";
  private static final String REDIRECT_PROCESS_ACTION = "redirectToProcessAction";
  private static final String FROMPAGE = "fromPage";
  private static final String TOPIC_REF = Entity.SEPARATOR + "topic" + Entity.SEPARATOR;
  private static final String SEPARATOR = "/";

  private static final String MESSAGECENTER_TOOL_ID = "sakai.messagecenter";
  private static final String FORUMS_TOOL_ID = "sakai.forums";

  private static final String MESSAGECENTER_BUNDLE = "org.sakaiproject.api.app.messagecenter.bundle.Messages";
  private static final ResourceLoader rb = new ResourceLoader(MESSAGECENTER_BUNDLE);

  private static final String INSUFFICIENT_PRIVILEGES_TO_EDIT_TEMPLATE_SETTINGS = "cdfm_insufficient_privileges";
  private static final String INSUFFICIENT_PRIVILEGES_TO_EDIT_TEMPLATE_ORGANIZE = "cdfm_insufficient_privileges";
  private static final String INSUFFICIENT_PRIVILEAGES_TO="cdfm_insufficient_privileages_to";
  private static final String INSUFFICIENT_PRIVILEAGES_TO_POST_THREAD="cdfm_insufficient_privileges_post_thread";  
  private static final String INSUFFICIENT_PRIVILEGES_REVISE_MESSAGE="cdfm_insufficient_privileges_revise_message";
  private static final String INSUFFICIENT_PRIVILEGES_CHANGE_FORUM="cdfm_insufficient_privileges_change_forum";
  private static final String INSUFFICIENT_PRIVILEGES_NEW_TOPIC = "cdfm_insufficient_privileges_new_topic";
  private static final String INSUFFICIENT_PRIVILEGES_CREATE_TOPIC="cdfm_insufficient_privileges_create_topic";
  private static final String FORUM_LOCKED = "cdfm_forum_locked";
  private static final String TOPIC_LOCKED = "cdfm_topic_locked";
  private static final String ERROR_POSTING_THREAD = "cdfm_error_posting_thread";
  private static final String ERROR_POSTING_THREAD_STALE = "cdfm_error_posting_thread_stale";
  private static final String USER_NOT_ALLOWED_CREATE_FORUM="cdfm_user_not_allowed_create_forum";
  private static final String INSUFFICIENT_PRIVILEGES_TO_DELETE_FORUM="cdfm_insufficient_privileges_delete_forum";
  private static final String INSUFFICIENT_PRIVILEGES_TO_DUPLICATE = "cdfm_insufficient_privileges_duplicate";
  private static final String SHORT_DESC_TOO_LONG = "cdfm_short_desc_too_long";
  private static final String LAST_REVISE_BY = "cdfm_last_revise_msg"; 
  private static final String LAST_REVISE_ON = "cdfm_last_revise_msg_on";
  private static final String LAST_REVISE_ON_ANON = "cdfm_last_revise_msg_on_anon";
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
  private static final String NO_MARKED_NO_READ_MESSAGE = "cdfm_no_message_mark_no_read";
  private static final String GRADE_SUCCESSFUL = "cdfm_grade_successful";
  private static final String GRADE_GREATER_ZERO = "cdfm_grade_greater_than_zero";
  private static final String GRADE_DECIMAL_WARN = "cdfm_grade_decimal_warn";
  private static final String GRADE_INVALID_GENERIC = "cdfm_grade_invalid_warn";
  private static final String ALERT = "cdfm_alert";
  private static final String SELECT_ASSIGN = "cdfm_select_assign";
  private static final String INVALID_COMMENT = "cdfm_add_comment_invalid";
  private static final String INSUFFICIENT_PRIVILEGES_TO_ADD_COMMENT = "cdfm_insufficient_privileges_add_comment";
  private static final String MOD_COMMENT_TEXT = "cdfm_moderator_comment_text";
  private static final String MOD_COMMENT_TEXT_ANON = "cdfm_moderator_comment_text_anon";
  private static final String NO_MSG_SEL_FOR_APPROVAL = "cdfm_no_message_mark_approved";
  private static final String MSGS_APPROVED = "cdfm_approve_msgs_success";
  private static final String MSGS_DENIED = "cdfm_deny_msgs_success";
  private static final String MSG_REPLY_PREFIX = "cdfm_reply_prefix";
  private static final String NO_GRADE_PTS = "cdfm_no_points_for_grade";
  private static final String TOO_LARGE_GRADE = "cdfm_too_large_grade";
  private static final String NO_ASSGN = "cdfm_no_assign_for_grade";
  private static final String CONFIRM_DELETE_MESSAGE="cdfm_delete_msg";
  private static final String INSUFFICIENT_PRIVILEGES_TO_DELETE = "cdfm_insufficient_privileges_delete_msg";
  private static final String END_DATE_BEFORE_OPEN_DATE = "endDateBeforeOpenDate";
  private static final String NO_GROUP_SELECTED ="cdfm_no_group_selected";
  private static final String AUTOCREATE_TOPICS_ROLES_DESCRIPTION = "cdfm_autocreate_topics_desc_roles";
  private static final String AUTOCREATE_TOPICS_GROUPS_DESCRIPTION = "cdfm_autocreate_topics_desc_groups";
  private static final String DUPLICATE_COPY_TITLE = "cdfm_duplicate_copy_title";
  private static final String TASK_NOT_CREATED =  "cdfm_cant_create_task";
  private static final String MSG_PVT_ANSWER_PREFIX = "pvt_answer_title_prefix";
  private static final String MSG_PVT_QUESTION_PREFIX = "pvt_question_title_prefix";
  private static final String MULTI_GRADEBOOK_ITEMS_ERROR = "group_sitegradebook_items_error";
  private static final String MULTI_GRADEBOOK_ITEMS_MUST_SELECT = "group_sitegradebook_items_must_select";
  private static final String MULTI_GRADEBOOK_GROUP_ITEMS_ERROR = "group_sitegradebook_group_items_error";
  private static final String MULTI_GRADEBOOK_GROUP_FORUM_ITEMS_ERROR = "group_sitegradebook_forum_group_item_error";


  private static final String FROM_PAGE = "msgForum:mainOrForumOrTopic";
  /**
   * If deleting, the parameter determines where to navigate back to
   */
  private String fromPage = null; // keep track of originating page for common functions
  
  private List forums = new ArrayList();
  private List pendingMsgs = new ArrayList();
  
  private String userId;
  
  private boolean showForumLinksInNav = true;
  private boolean showShortDescription = true;
  private boolean collapsePermissionPanel = false;
  private boolean showProfileInfo = false;
  private boolean showThreadChanges = true;

  // compose
  @ManagedProperty(value="#{Components[\"org.sakaiproject.api.app.messageforums.MessageForumsMessageManager\"]}")
  private MessageForumsMessageManager messageManager;
  @Setter
  @ManagedProperty(value="#{Components[\"org.sakaiproject.util.api.FormattedText\"]}")
  private FormattedText formattedText;
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
  private List<SelectItem> assignments = new ArrayList<>();
  private String selectedAssign = DEFAULT_GB_ITEM; 
  private String gradePoint; 
  private String gradeComment; 
  private boolean gradebookExist = true;
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
  
  private boolean gradeByPoints;
  private boolean gradeByPercent;
  private boolean gradeByLetter;

  private boolean discussionGeneric = false;
  private String groupId;

  /**
   * Dependency Injected
   */
  @ManagedProperty(value="#{Components[\"org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager\"]}")
  private DiscussionForumManager forumManager;
  @ManagedProperty(value="#{Components[\"org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager\"]}")
  private UIPermissionsManager uiPermissionsManager;
  @ManagedProperty(value="#{Components[\"org.sakaiproject.api.app.messageforums.MessageForumsTypeManager\"]}")
  private MessageForumsTypeManager typeManager;
  @ManagedProperty(value="#{Components[\"org.sakaiproject.api.app.messageforums.MembershipManager\"]}")
  private MembershipManager membershipManager;
  @ManagedProperty(value="#{Components[\"org.sakaiproject.api.app.messageforums.PermissionLevelManager\"]}")
  private PermissionLevelManager permissionLevelManager; 
  @ManagedProperty(value="#{Components[\"org.sakaiproject.api.app.messageforums.EmailNotificationManager\"]}")
  private EmailNotificationManager emailNotificationManager;
  @ManagedProperty(value="#{Components[\"org.sakaiproject.api.app.messageforums.SynopticMsgcntrManager\"]}")
  private SynopticMsgcntrManager synopticMsgcntrManager;
  @ManagedProperty(value="#{Components[\"org.sakaiproject.content.api.ContentHostingService\"]}")
  private ContentHostingService contentHostingService;
  @ManagedProperty(value="#{Components[\"org.sakaiproject.authz.api.AuthzGroupService\"]}")
  private AuthzGroupService authzGroupService;
  @ManagedProperty(value="#{Components[\"org.sakaiproject.event.api.EventTrackingService\"]}")
  private EventTrackingService eventTrackingService;
  @ManagedProperty(value="#{Components[\"org.sakaiproject.user.api.UserDirectoryService\"]}")
  private UserDirectoryService userDirectoryService;
  @ManagedProperty(value="#{Components[\"org.sakaiproject.site.api.SiteService\"]}")
  private SiteService siteService;
  @ManagedProperty(value="#{Components[\"org.sakaiproject.authz.api.SecurityService\"]}")
  private SecurityService securityService;
  @ManagedProperty(value="#{Components[\"org.sakaiproject.tool.api.SessionManager\"]}")
  private SessionManager sessionManager;
  @ManagedProperty(value="#{Components[\"org.sakaiproject.tool.api.ToolManager\"]}")
  private ToolManager toolManager;
  @ManagedProperty(value="#{Components[\"org.sakaiproject.thread_local.api.ThreadLocalManager\"]}")
  private ThreadLocalManager threadLocalManager;
  @ManagedProperty(value="#{Components[\"org.sakaiproject.rubrics.api.RubricsService\"]}")
  private RubricsService rubricsService;
  @ManagedProperty(value = "#{Components[\"org.sakaiproject.time.api.UserTimeService\"]}")
  private UserTimeService userTimeService;
  @ManagedProperty(value = "#{Components[\"org.sakaiproject.tasks.api.TaskService\"]}")
  private TaskService taskService;
  @ManagedProperty(value="#{Components[\"org.sakaiproject.calendar.api.CalendarService\"]}")
  @Getter @Setter
  private CalendarService calendarService;
  @ManagedProperty(value="#{Components[\"org.sakaiproject.time.api.TimeService\"]}")
  @Setter
  private TimeService timeService;

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
  
  private List<SiteGroupBean> siteGroups = new ArrayList<>();

  @Getter @Setter
  private boolean dialogGradeSavedSuccessfully = false;

  // email notification options
  private EmailNotificationBean watchSettingsBean;
  
  private boolean needToPostFirst;
  
  // rank
  @ManagedProperty(value="#{Components[\"org.sakaiproject.api.app.messageforums.RankManager\"]}")
  private RankManager rankManager;
  private ForumRankBean forumRankBean;

  @ManagedProperty(value="#{Components[\"org.sakaiproject.api.app.messageforums.AnonymousManager\"]}")
  private AnonymousManager anonymousManager;

  private String editorRows;
  
  private boolean threadMoved;

   /**
   * 
   */
  public DiscussionForumTool()
  {
    log.debug("DiscussionForumTool()");
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
    showShortDescription = ServerConfigurationService.getBoolean("mc.showShortDescription", true);
    collapsePermissionPanel = ServerConfigurationService.getBoolean("mc.collapsePermissionPanel", false);
    showProfileInfo = ServerConfigurationService.getBoolean("msgcntr.forums.showProfileInfo", true);
  }

  protected GradingService getGradingService() {
    return (GradingService)  ComponentManager.get("org.sakaiproject.grading.api.GradingService");
  }

  /**
   * @return
   */
  public String processActionHome()
  {
    log.debug("processActionHome()");
   	reset();
    return gotoMain();
  }

  /**
   * @return
   */
  public boolean isInstructor() {
    if (instructor == null) {
        instructor = forumManager.isInstructor();
    }
    return instructor.booleanValue();
  }
  
  /**
   * @return
   */
  public boolean isSectionTA() {
    if (sectionTA == null) {
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
        Integer index = Integer.valueOf(i);
        retSort.add(new SelectItem(index, index.toString()));
     }

     return retSort;
  }

  /**
   * @return List of DiscussionForumBean
   */
  public List getForums() {

    log.debug("getForums()");

    if (forums != null && forums.size() > 0) {
      return forums;
    }

    forums = new ArrayList<DiscussionForumBean>();
    int unreadMessagesCount = 0;
    userId = getUserId();

    boolean hasOverridingPermissions = false;
    if (securityService.isSuperUser() || isInstructor()) {
      hasOverridingPermissions = true;
    }
    // MSGCNTR-661 - the template settings are no longer affecting the
    // availability, so we need this to always be true
    boolean isAreaAvailable = true;

    if (isAreaAvailable) {
      // query the database for all of the forums that are associated with the current site
      List<DiscussionForum> tempForums = forumManager.getForumsForMainPage();
      if (tempForums == null || tempForums.size() < 1) {
        if (securityService.isSuperUser() && ServerConfigurationService.getBoolean("forums.setDefault.forum", true)) {
          //initialize area:
          forumManager.getDiscussionForumArea();
          //try again:
          tempForums = forumManager.getForumsForMainPage();
          if (tempForums == null || tempForums.size() < 1) {
            return null;
          }
        } else {
          return null;
        }
      }

      // establish some values that we will check multiple times to shave a few processing cycles
      boolean readFullDescription = "true".equalsIgnoreCase(ServerConfigurationService.getString("mc.defaultLongDescription"));

      // run through the topics once to get their parent forums, create the decorated topics that will be used later, and
      // possibly set the message count
      SortedSet<DiscussionForum> tempSortedForums = new TreeSet<DiscussionForum>(new ForumBySortIndexAscAndCreatedDateDesc());
      Map<Long, DiscussionTopicBean> topicBeans = new HashMap<Long, DiscussionTopicBean>();
      Set<Long> topicIdsForCounts = new HashSet<Long>();
      for (DiscussionForum forum: tempForums) {
        if ((!forum.getDraft() && forum.getAvailability()) || hasOverridingPermissions) {
          // this is the start of the big forum if

          tempSortedForums.add(forum);

          for (DiscussionTopic currTopic : (Set<DiscussionTopic>)forum.getTopicsSet()) {
            if ((currTopic.getDraft().equals(Boolean.FALSE) && currTopic.getAvailability()) || hasOverridingPermissions) {
              // this is the start of the big topic if
              DiscussionTopicBean decoTopic = new DiscussionTopicBean(currTopic, (DiscussionForum)currTopic.getOpenForum(), forumManager, rubricsService, userTimeService);
              loadTopicDataInTopicBean(currTopic, decoTopic);
              if (readFullDescription) decoTopic.setReadFullDesciption(true);

              // set the message count for moderated topics, otherwise it will be set later
              if (decoTopic.getIsRead()) {
                if (currTopic.getModerated() && !decoTopic.getIsModeratePostings()) {
                  decoTopic.setTotalNoMessages(forumManager.getTotalViewableMessagesWhenMod(currTopic));
                  decoTopic.setUnreadNoMessages(forumManager.getNumUnreadViewableMessagesWhenMod(currTopic));
                } else {
                  topicIdsForCounts.add(currTopic.getId());
                }
               } else{
                  decoTopic.setTotalNoMessages(0);
                  decoTopic.setUnreadNoMessages(0);
               }

               topicBeans.put(currTopic.getId(), decoTopic);
            } // end the big topic if
          }
        } // end the big forum if
      }

      // get the total message count of non-moderated topics and add them to the discussion topic bean and
      // initialize the unread number of messages to all of them.
      for (Object[] counts: forumManager.getMessageCountsForMainPage(topicIdsForCounts)) {
        DiscussionTopicBean decoTopic = topicBeans.get(counts[0]);
        decoTopic.setTotalNoMessages(((Long) counts[1]).intValue());
        decoTopic.setUnreadNoMessages(((Long) counts[1]).intValue());
      }

      // get the total read message count for the current user of non-moderated and add them to the discussion
      // topic bean as the number of unread messages.  I could've combined this with the previous query but
      // stupid Hibernate (3.x) won't let us properly outer join mapped entitys that do not have a direct
      // association.  BLURG!  Any topic not in the returned list means the user hasn't read any of the messages
      // in that topic which is why I set the default unread message count to all the messages in the previous
      // loop.
      for (Object[] counts: forumManager.getReadMessageCountsForMainPage(topicIdsForCounts)) {
        DiscussionTopicBean decoTopic = topicBeans.get(counts[0]);
        decoTopic.setUnreadNoMessages(decoTopic.getTotalNoMessages() - ((Long) counts[1]).intValue());
      }

      // get the assignments for use later
      try {
        assignments = new ArrayList<>();
        assignments.add(new SelectItem(DEFAULT_GB_ITEM, getResourceBundleString(SELECT_ASSIGN)));

        //Code to get the gradebook service from ComponentManager
        GradingService gradingService = getGradingService();
        
		for (Assignment thisAssign : gradingService.getAssignments(toolManager.getCurrentPlacement().getContext(), toolManager.getCurrentPlacement().getContext(), SortType.SORT_BY_NONE)) {
			if (!thisAssign.getExternallyMaintained()) {
				try {
					assignments.add(new SelectItem(Long.toString(thisAssign.getId()), thisAssign.getName()));
				} catch (Exception e) {
					log.error("DiscussionForumTool - processDfMsgGrd:" + e);
				}
			}
		}
        
      } catch (SecurityException se) {
          log.debug("SecurityException caught while getting assignments.", se);
      } catch (Exception e1) {
          log.error("DiscussionForumTool&processDfMsgGrad:" + e1);
      }

      // now loop through the forums that we found earlier and turn them into forums ready to be displayed to the end user
      int sortIndex = 1;
      for (DiscussionForum forum: tempSortedForums) {
        // manually set the sort index now that the list is sorted
        forum.setSortIndex(Integer.valueOf(sortIndex));
        sortIndex++;

        DiscussionForumBean decoForum = new DiscussionForumBean(forum, forumManager, userTimeService);
        loadForumDataInForumBean(forum, decoForum);
        if (readFullDescription) decoForum.setReadFullDesciption(true);

        if (forum.getTopics() != null) {
          for (DiscussionTopic topic : (List<DiscussionTopic>) forum.getTopics()) {
            DiscussionTopicBean decoTopic = topicBeans.get(topic.getId());
            if (decoTopic != null) decoForum.addTopic(decoTopic);
          }

          String forumDefaultAssignName = forum.getDefaultAssignName();

          //iterate over all topics in the decoratedForum to add the unread message
          //counts to update the sypnoptic tool
          for (DiscussionTopicBean dTopicBean : decoForum.getTopics()) {
            //if user can read this forum topic, count the messages as well
            if (dTopicBean.getIsRead()) {
                unreadMessagesCount += dTopicBean.getUnreadNoMessages();
            }

            setTopicGradeAssign(dTopicBean, forumDefaultAssignName);
          }
        }

		if (isGradebookGroupEnabled()) {
			decoForum.setGradeAssign(forum.getDefaultAssignName());
		} else {
			decoForum.setGradeAssign(DEFAULT_GB_ITEM);
			for (SelectItem ass : assignments) {
			  if (ass.getLabel().equals(forum.getDefaultAssignName()) ||
				ass.getValue().equals(forum.getDefaultAssignName())) {
				decoForum.setGradeAssign((String) ass.getValue());
				break;
			  }
			}
		}

        forums.add(decoForum);
      }
    }
    //update synoptic info for forums only:
    setForumSynopticInfoHelper(userId, getSiteId(), unreadMessagesCount, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
    return forums;
  }

  public void setTopicGradeAssign(DiscussionTopicBean bean, String defaultGradeAssign) {

    if (StringUtils.isNotEmpty(bean.getTopic().getDefaultAssignName())) {
      bean.setGradeAssign(bean.getTopic().getDefaultAssignName());
    } else {
      if (StringUtils.isNotEmpty(defaultGradeAssign)) {
        bean.setGradeAssign(defaultGradeAssign);
      }
    }
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
			  log.error(e.getMessage(), e);
		  }

		  numOfAttempts--;

		  if (numOfAttempts <= 0) {
			  log.info("DiscussionForumTool: setForumSynopticInfoHelper: HibernateOptimisticLockingFailureException no more retries left");
			  log.error(holfe.getMessage(), holfe);
		  } else {
			  log.info("DiscussionForumTool: setForumSynopticInfoHelper: HibernateOptimisticLockingFailureException: attempts left: "
					  + numOfAttempts);
			  setForumSynopticInfoHelper(userId, siteId, 
					  unreadMessagesCount, numOfAttempts);
		  }
	  }

  }

  /**
   * @return
   */
  public String processActionOrganize()
  {
    log.debug("processActionOrganize()");
    return MAIN;
  }

  /**
   * @return
   */
  public String processActionStatistics()
  {
    log.debug("processActionStatistics()");
    return FORUM_STATISTICS;
  }
  
  /**
   * @return
   */
  public String processActionTemplateSettings()
  {
    log.debug("processActionTemplateSettings()");
    
    setEditMode(true);
    setPermissionMode(PERMISSION_MODE_TEMPLATE);
    template = new DiscussionAreaBean(areaManager.getDiscusionArea(), userTimeService);

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
    log.debug("processActionTemplateOrganize()");
    
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
  	if (log.isDebugEnabled()){
      log.debug("processActionReviseTemplateSettings()");
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
    log.debug("processActionSaveTemplateSettings()");
    if(!isInstructor())
    {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_TO_EDIT_TEMPLATE_SETTINGS));
      return gotoMain();
    }  
    
    if(template.getArea().getOpenDate() != null && template.getArea().getCloseDate() != null
    		&& template.getArea().getAvailabilityRestricted()){
    	//check whether the close date is after the open date or not:
    	if(template.getArea().getOpenDate().after(template.getArea().getCloseDate())){
    		setErrorMessage(getResourceBundleString(END_DATE_BEFORE_OPEN_DATE));
    		return null;
    	}
    }
    
    setObjectPermissions(template.getArea());
    areaManager.saveArea(template.getArea());
    return gotoMain();
  }


  public String processActionCancelTemplateSettings()
  {
    log.debug("processActionTemplateSettings()");
    // SAK-14073 -- Cleanout values after cancelling.
    FacesContext context = FacesContext.getCurrentInstance();
    UIInput component = (UIInput) context.getViewRoot().findComponent("revise:moderated");
    if (component != null) {
      component.setSubmittedValue(null);
    }
    return processActionHome();
  }

  /**
   * @return
   */
  public String processActionSaveTemplateOrganization()
  {
    log.debug("processActionSaveTemplateOrganization()");
    if(!isInstructor())
    {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_TO_EDIT_TEMPLATE_ORGANIZE));
      return gotoMain();
    }
    if(forums != null){
    	for(Iterator i = forums.iterator(); i.hasNext(); ) {
    		DiscussionForumBean forum = (DiscussionForumBean)i.next();

    		// because there is no straight up save forum function we need to retain the draft status
    		if(forum.getForum().getDraft().booleanValue())
    			forumManager.saveForumAsDraft(forum.getForum());
    		else
    			forumManager.saveForum(forum.getForum());
    	}
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
    log.debug("processActionRestoreDefaultTemplate()");
    if(!isInstructor())
    {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_TO_EDIT_TEMPLATE_SETTINGS));
      return gotoMain();
    }
    
    Area area = null;
    if ((area = areaManager.getDiscusionArea()) != null){
    	area.setMembershipItemSet(new HashSet());
    	area.setModerated(Boolean.FALSE);
    	area.setPostFirst(Boolean.FALSE);
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
  public boolean getNewForum() {
    if (newForum == null) {
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
    log.debug("processDisplayForum()");
    forumClickCount++;
    if (getDecoratedForum() == null)
    {
      log.warn("Forum not found for id {}", getExternalParameterByKey(FORUM_ID));
      // Clear cached state so main view rebuilds after import/replace
      reset();
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
	  log.debug("processForumMainConfirm()");

	  String forumId = getExternalParameterByKey(FORUM_ID);
	  DiscussionForum forum = forumManager.getForumById(Long.valueOf(forumId));
	  selectedForum = new DiscussionForumBean(forum, forumManager, userTimeService);
	  loadForumDataInForumBean(forum, selectedForum);

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
    log.debug("processActionDeleteForumConfirm()");
    if (selectedForum == null)
    {
      log.debug("There is no forum selected for deletion");
      return gotoMain();
    }
//  TODO:
    if(!uiPermissionsManager.isChangeSettings(selectedForum.getForum()))
    {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_TO_DELETE_FORUM));
      return gotoMain();
    }
    selectedForum.getForum().setExtendedDescription(formattedText.processFormattedText(selectedForum.getForum().getExtendedDescription(), null, null));
    selectedForum.setMarkForDeletion(true);
    return FORUM_SETTING;
  }

  /**
   * @return
   */
  public String processActionDeleteForum() {
    if (uiPermissionsManager == null) {
      throw new IllegalStateException("uiPermissionsManager == null");
    }
    if (selectedForum == null) {
      throw new IllegalStateException("selectedForum == null");
    }
    if(!uiPermissionsManager.isChangeSettings(selectedForum.getForum())) {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEAGES_TO));
      return gotoMain();
    }
    HashMap<String, Integer> beforeChangeHM = null; 
    DiscussionForum forum = selectedForum.getForum();
    Long forumId = forum.getId();
    List topics = forum.getTopics();
    beforeChangeHM = SynopticMsgcntrManagerCover.getUserToNewMessagesForForumMap(getSiteId(), forumId, null);

    forumManager.deleteForum(selectedForum.getForum());

    if(beforeChangeHM != null){
        updateSynopticMessagesForForumComparingOldMessagesCount(getSiteId(), forumId, null, beforeChangeHM, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
    }
    
    // Delete task (forum)
    String reference = DiscussionForumService.REFERENCE_ROOT + SEPARATOR + getSiteId() + SEPARATOR + forumId;
    taskService.removeTaskByReference(reference);
    // Delete task (topics)
    for (Iterator topicIter = topics.iterator(); topicIter.hasNext();) {
        DiscussionTopic topic = (DiscussionTopic) topicIter.next();
        Long topicId = topic.getId();
        reference = DiscussionForumService.REFERENCE_ROOT + SEPARATOR + getSiteId() + SEPARATOR + forumId + TOPIC_REF + topicId;
        taskService.removeTaskByReference(reference);
    }
    

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
			  log.error(e.getMessage(), e);
		  }

		  numOfAttempts--;

		  if (numOfAttempts <= 0) {
			  log.info("DiscussionForumTool: updateSynopticMessagesForForumComparingOldMessagesCount: HibernateOptimisticLockingFailureException no more retries left");
			  log.error(holfe.getMessage(), holfe);
		  } else {
			  log.info("DiscussionForumTool: updateSynopticMessagesForForumComparingOldMessagesCount: HibernateOptimisticLockingFailureException: attempts left: "
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
    log.debug("processActionNewForum()");
    forumClickCount = 0;
    topicClickCount = 0;
    
    setEditMode(true);
    setPermissionMode(PERMISSION_MODE_FORUM);
        
    if (getNewForum())
    {
      DiscussionForum forum = forumManager.createForum();
      forum.setModerated(areaManager.getDiscusionArea().getModerated()); // default to template setting
      forum.setAutoMarkThreadsRead(areaManager.getDiscusionArea().getAutoMarkThreadsRead()); // default to template setting
      forum.setPostFirst(areaManager.getDiscusionArea().getPostFirst()); // default to template setting
      if (areaManager.getDiscusionArea().getAvailabilityRestricted()) {
          forum.setAvailabilityRestricted(true);
          forum.setOpenDate(areaManager.getDiscusionArea().getOpenDate());
          forum.setCloseDate(areaManager.getDiscusionArea().getCloseDate());
      }
      
      selectedForum = null;
      selectedForum = new DiscussionForumBean(forum, forumManager, userTimeService);
      loadForumDataInForumBean(forum, selectedForum);
      if("true".equalsIgnoreCase(ServerConfigurationService.getString("mc.defaultLongDescription")))
      {
      	selectedForum.setReadFullDesciption(true);
      }

      setNewForumBeanAssign();

	  if (isGradebookGroupEnabled()) {
		selectedForum.setRestrictPermissionsForGroups("true");
		setGroupId(null);
		setDiscussionGeneric(false);
	  }

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
    log.debug("processActionForumSettings()");
    forumClickCount = 0;
    topicClickCount = 0;
    setEditMode(true);
    setPermissionMode(PERMISSION_MODE_FORUM);
    
    String forumId = getExternalParameterByKey(FORUM_ID);
    if (StringUtils.isBlank(forumId) || "null".equals(forumId))
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
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_CHANGE_FORUM));
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
    
    selectedForum = new DiscussionForumBean(forum, forumManager, userTimeService);
    loadForumDataInForumBean(forum, selectedForum);
    if("true".equalsIgnoreCase(ServerConfigurationService.getString("mc.defaultLongDescription")))
    {
    	selectedForum.setReadFullDesciption(true);
    }

	String currentDefaultAssignName = forum.getDefaultAssignName();

	selectedForum.setGradeAssign(currentDefaultAssignName);

	if (isGradebookGroupEnabled()) {
		if (currentDefaultAssignName != null && !StringUtils.isBlank(currentDefaultAssignName)) {
			GradingService gradingService = getGradingService();
			String gbUid = gradingService.getGradebookUidByAssignmentById(toolManager.getCurrentPlacement().getContext(), Long.parseLong(currentDefaultAssignName));
			setGroupId(gbUid);
			setDiscussionGeneric(false);
		} else {
			setDiscussionGeneric(true);
		}
	}

    setForumBeanAssign();
    setFromMainOrForumOrTopic();

    return FORUM_SETTING_REVISE;

  }

  /**
   * @return
   */
  public String processActionSaveForumAndAddTopic()
  {
    log.debug("processActionSaveForumAndAddTopic()");
    if(forumClickCount != 0 || topicClickCount != 0) {
    	setErrorMessage(getResourceBundleString(MULTIPLE_WINDOWS , new Object[] {ServerConfigurationService.getString("ui.service","Sakai")}));
    	return FORUM_SETTING_REVISE;
    }
    
    if (selectedForum == null)
        throw new IllegalStateException("selectedForum == null");
    
    if(selectedForum.getForum() != null
            && selectedForum.getForum().getOpenDate() != null && selectedForum.getForum().getCloseDate() != null
    		&& selectedForum.getForum().getAvailabilityRestricted()){
    	//check whether the close date is after the open date or not:
    	if(selectedForum.getForum().getOpenDate().after(selectedForum.getForum().getCloseDate())){
    		setErrorMessage(getResourceBundleString(END_DATE_BEFORE_OPEN_DATE));
    		return null;
    	}
    }
    
    if(selectedForum.getForum()!=null &&
    		(selectedForum.getForum().getShortDescription()!=null))
    {
    	if(selectedForum.getForum().getShortDescription().length() > 255){
    		setErrorMessage(getResourceBundleString(SHORT_DESC_TOO_LONG));
    		return null;
    	}
    }

    if(selectedForum.getForum()!=null && 
        (selectedForum.getForum().getTitle()==null 
          ||selectedForum.getForum().getTitle().trim().length()<1  ))
    {
      setErrorMessage(getResourceBundleString(VALID_FORUM_TITLE_WARN));
      return FORUM_SETTING_REVISE;
    }
    
    if(!uiPermissionsManager.isChangeSettings(selectedForum.getForum()))
    {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_CHANGE_FORUM));
      return gotoMain();
    }   

    DiscussionForum forum = processForumSettings(false);
    if(!uiPermissionsManager.isNewTopic(forum))
    {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_CREATE_TOPIC));
      reset();
      return gotoMain();
    }    
    selectedTopic = createTopic(forum.getId());
    if (selectedTopic == null)
    {
      setErrorMessage(getResourceBundleString(FAILED_NEW_TOPIC));
      attachments.clear();
      prepareRemoveAttach.clear();
      reset();
      return gotoMain();
    }
    setPermissionMode(PERMISSION_MODE_TOPIC);
    siteGroups.clear();
    selectedForum.getForum().setRestrictPermissionsForGroups(false);
    selectedTopic.getTopic().setRestrictPermissionsForGroups(false);
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
    log.debug("processActionSaveForumSettings()");
    if(forumClickCount != 0 || topicClickCount != 0) {
    	setErrorMessage(getResourceBundleString(MULTIPLE_WINDOWS , new Object[] {ServerConfigurationService.getString("ui.service","Sakai")}));
    	return FORUM_SETTING_REVISE;
    }

    if (selectedForum == null)
        throw new IllegalStateException("selectedForum == null");

    if(selectedForum.getForum() != null 
            && selectedForum.getForum().getOpenDate() != null && selectedForum.getForum().getCloseDate() != null
    		&& selectedForum.getForum().getAvailabilityRestricted()){
    	//check whether the close date is after the open date or not:
    	if(selectedForum.getForum().getOpenDate().after(selectedForum.getForum().getCloseDate())){
    		setErrorMessage(getResourceBundleString(END_DATE_BEFORE_OPEN_DATE));
    		return null;
    	}
    }
    
    if(selectedForum.getForum()!=null &&
    		(selectedForum.getForum().getShortDescription()!=null))
    {
    	if(selectedForum.getForum().getShortDescription().length() > 255){
    		setErrorMessage(getResourceBundleString(SHORT_DESC_TOO_LONG));
    		return null;
    	}
    }    
	
    if(!uiPermissionsManager.isChangeSettings(selectedForum.getForum()))
    {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_CHANGE_FORUM));
      return gotoMain();
    }
    if(selectedForum.getForum()!=null && 
        (selectedForum.getForum().getTitle()==null 
          ||selectedForum.getForum().getTitle().trim().length()<1  ))
    {
      setErrorMessage(getResourceBundleString(VALID_FORUM_TITLE_WARN));
      return FORUM_SETTING_REVISE;
    }    
    if(processForumSettings(false) == null){
        return null;
    }
    return processReturnToOriginatingPage();
  }

	public boolean checkMultiGradebook(boolean isForum) {
		if (isGradebookGroupEnabled()) {
			List<String> selectedGroupList = new ArrayList<>();

			for (SiteGroupBean siteGroup : siteGroups) {
				if (siteGroup.getGroup() != null && (isForum && siteGroup.getCreateForumForGroup()) || (!isForum && siteGroup.getCreateTopicForGroup())) {
					selectedGroupList.add(siteGroup.getGroup().getId());
				}
			}

			String gradeAssign = "";

			if (isForum) {
				gradeAssign = selectedForum.getGradeAssign();
			} else {
				gradeAssign = selectedTopic.getGradeAssign();
			}

			if (!StringUtils.isBlank(gradeAssign) && !gradeAssign.equals(DEFAULT_GB_ITEM)) {
				GradingService gradingService = getGradingService();
				List<String> gbItemList = Arrays.asList(gradeAssign.split(","));

				boolean areItemsInGroups =
					gradingService.checkMultiSelectorList(toolManager.getCurrentPlacement().getContext(),
					selectedGroupList, gbItemList, false);

				if (!areItemsInGroups) {
					setErrorMessage(getResourceBundleString(MULTI_GRADEBOOK_ITEMS_ERROR));
					return false;
				}
			} else {
				setErrorMessage(getResourceBundleString(MULTI_GRADEBOOK_ITEMS_MUST_SELECT));
				return false;
			}
		}

		return true;
  	}

  /**
   * @return
   */
  public String processActionSaveForumAsDraft()
  {
    log.debug("processActionSaveForumAsDraft()");
    if(forumClickCount != 0 || topicClickCount != 0) {
    	setErrorMessage(getResourceBundleString(MULTIPLE_WINDOWS , new Object[] {ServerConfigurationService.getString("ui.service","Sakai")}));
    	return FORUM_SETTING_REVISE;
    }
    
    if (selectedForum == null)
        throw new IllegalStateException("selectedForum == null");
    
    if(selectedForum.getForum() != null && selectedForum.getForum().getOpenDate() != null && selectedForum.getForum().getCloseDate() != null
    		&& selectedForum.getForum().getAvailabilityRestricted()){
    	//check whether the close date is after the open date or not:
    	if(selectedForum.getForum().getOpenDate().after(selectedForum.getForum().getCloseDate())){
    		setErrorMessage(getResourceBundleString(END_DATE_BEFORE_OPEN_DATE));
    		return null;
    	}
    }
    
    if(selectedForum.getForum()!=null &&
    		(selectedForum.getForum().getShortDescription()!=null))
    {
    	if(selectedForum.getForum().getShortDescription().length() > 255){
    		setErrorMessage(getResourceBundleString(SHORT_DESC_TOO_LONG));
    		return null;
    	}
    }

    if(!uiPermissionsManager.isChangeSettings(selectedForum.getForum()))
    {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_CHANGE_FORUM));
      return gotoMain();
    }
    if(selectedForum.getForum()!=null && 
        (selectedForum.getForum().getTitle()==null 
          ||selectedForum.getForum().getTitle().trim().length()<1  ))
    {
      setErrorMessage(getResourceBundleString(VALID_FORUM_TITLE_WARN));
      return FORUM_SETTING_REVISE;
    }    
    if(processForumSettings(true) == null){
        return null;
    }
    return processReturnToOriginatingPage();
  }

  private DiscussionForum processForumSettings(boolean draft) {
	if (isGradebookGroupEnabled()) {
		if (selectedForum.getForum().getId() == null) {
			String newDefaultAssignName = selectedForum.getGradeAssign();

			if (!selectedForum.getForum().getRestrictPermissionsForGroups() &&
				!StringUtils.isBlank(newDefaultAssignName) && !newDefaultAssignName.equals(DEFAULT_GB_ITEM)) {
				setErrorMessage(getResourceBundleString(MULTI_GRADEBOOK_GROUP_ITEMS_ERROR));
				return null;
			}
		}

		String currentDefaultAssignName = selectedForum.getForum().getDefaultAssignName();

		if (selectedForum.getForum().getId() != null &&
			currentDefaultAssignName != null && !StringUtils.isBlank(currentDefaultAssignName)) {
			if (!checkUpdateSettings(true)) {
				return null;
			}

			selectedForum.getForum().setDefaultAssignName(selectedForum.getGradeAssign());
		}
	}

    if (selectedForum.getForum().getRestrictPermissionsForGroups() && selectedForum.getForum().getId() == null) {
        if (!saveForumsForGroups(draft)) {
            return null;
        }
    } else {
        saveForumSettings(draft);
    }
    return selectedForum.getForum();
  }

  private boolean checkUpdateSettings(boolean isForum) {
	String defaultAssignName = null;
	String newDefaultAssignName = null;

	if (isForum) {
		defaultAssignName = selectedForum.getForum().getDefaultAssignName();
		newDefaultAssignName = selectedForum.getGradeAssign();
	} else {
		defaultAssignName = selectedTopic.getTopic().getDefaultAssignName();
		newDefaultAssignName = selectedTopic.getGradeAssign();
	}

	if (StringUtils.isBlank(newDefaultAssignName) || newDefaultAssignName.equals(DEFAULT_GB_ITEM)) {
		setErrorMessage(getResourceBundleString(MULTI_GRADEBOOK_ITEMS_MUST_SELECT));
		return false;
	}

	GradingService gradingService = getGradingService();
	Long itemId = Long.parseLong(defaultAssignName);

	GradebookAssignment gradebookAssignment = gradingService.getGradebookAssigment(
		toolManager.getCurrentPlacement().getContext(), itemId);

	String groupId = gradebookAssignment.getGradebook().getUid();

	String gbUid = gradingService.getGradebookUidByAssignmentById(toolManager.getCurrentPlacement().getContext(), Long.parseLong(newDefaultAssignName));

	if (!gbUid.equals(groupId)) {
		setErrorMessage(getResourceBundleString(MULTI_GRADEBOOK_GROUP_FORUM_ITEMS_ERROR));
		return false;
	}

	return true;
  }

  private DiscussionForum saveForumSettings(boolean draft) {
    log.debug("saveForumSettings(boolean " + draft + ")");
    
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
    boolean updateCounts = false;
    if(!isNew){
    	updateCounts = needToUpdateSynopticOnForumSave(forum, draft);    	
    }   
    //refresh synoptic counts if availability has changed:
    HashMap<String, Integer> beforeChangeHM = null;
    if(updateCounts){
    	beforeChangeHM = SynopticMsgcntrManagerCover.getUserToNewMessagesForForumMap(getSiteId(), forum.getId(), null);
    }
    
    forum.setExtendedDescription(formattedText.processFormattedText(forum.getExtendedDescription(), null, null));
	if(forum.getShortDescription()!=null && forum.getShortDescription().length() > 255){
		forum.setShortDescription(forum.getShortDescription().substring(0, 255));
	}

    if ("<br/>".equals(forum.getExtendedDescription()))
	{
		forum.setExtendedDescription("");
	}

	if (!isGradebookGroupEnabled()) {
		saveForumSelectedAssignment(forum);
	}

	saveForumAttach(forum);
    setObjectPermissions(forum);
    processActionSendToCalendar(forum);
    if (draft)
      forum = forumManager.saveForumAsDraft(forum);
    else
      forum = forumManager.saveForum(forum);

    if (!isNew && beforeChangeHM != null) {
      updateSynopticMessagesForForumComparingOldMessagesCount(getSiteId(), forum.getId(), null, beforeChangeHM, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
    }
    
    selectedForum.getForum().setId(forum.getId());
    
    // Update or create task if needed
    String gradeAssign = selectedForum.getGradeAssign();
	gradeAssign = gradeAssign == null ? selectedForum.getForum().getDefaultAssignName() : gradeAssign;

	if (!isGradebookGroupEnabled()) {
		if (!draft && gradeAssign != null) {
			GradingService gradingService = getGradingService();
			String gradebookUid = toolManager.getCurrentPlacement().getContext();
			Assignment assignment = gradingService.getAssignmentByNameOrId(gradebookUid, gradebookUid, gradeAssign);
			Date dueDate = (assignment != null ? assignment.getDueDate() : null);
			String reference = DiscussionForumService.REFERENCE_ROOT + SEPARATOR + getSiteId() + SEPARATOR + forum.getId();
			Optional<Task> optTask = taskService.getTask(reference);
			if (optTask.isPresent()) {
			  this.updateTask(optTask.get(), this.selectedForum.getForum().getTitle(), dueDate);
			} else if (this.selectedForum.isCreateTask() && StringUtils.isNotBlank(gradeAssign) && !DEFAULT_GB_ITEM.equals(gradeAssign) ) {
			  this.createTask(reference, this.selectedForum.getForum().getTitle(), dueDate);
			}
		}
	}

    return forum;
  }

  private boolean availabilityChanged(Object newTarget, Object oldTarget){
	  if (newTarget instanceof DiscussionForum && oldTarget instanceof DiscussionForum){
		  DiscussionForum forum = ((DiscussionForum) newTarget);
		  DiscussionForum oldForum = ((DiscussionForum) oldTarget);
		  boolean newAvailable = ForumScheduleNotificationCover.makeAvailableHelper(forum.getAvailabilityRestricted(), forum.getOpenDate(), forum.getCloseDate(), forum.getLockedAfterClosed());
		  boolean oldAvailable = ForumScheduleNotificationCover.makeAvailableHelper(oldForum.getAvailabilityRestricted(), oldForum.getOpenDate(), oldForum.getCloseDate(), oldForum.getLockedAfterClosed());
		  return newAvailable != oldAvailable;			
	  }else if (newTarget instanceof Topic && oldTarget instanceof Topic){
		  DiscussionTopic topic = ((DiscussionTopic) newTarget);
		  DiscussionTopic oldTopic = ((DiscussionTopic) oldTarget);
		  boolean newAvailable = ForumScheduleNotificationCover.makeAvailableHelper(topic.getAvailabilityRestricted(), topic.getOpenDate(), topic.getCloseDate(), topic.getLockedAfterClosed());
		  boolean oldAvailable = ForumScheduleNotificationCover.makeAvailableHelper(oldTopic.getAvailabilityRestricted(), oldTopic.getOpenDate(), oldTopic.getCloseDate(), oldTopic.getLockedAfterClosed());
		  return newAvailable != oldAvailable;	
	  }
	  return false;
  }
  
  private boolean needToUpdateSynopticOnForumSave(Object target, boolean isDraft){
	  boolean update = false;
	  
	  boolean isModerated = false;
	  boolean isModeratedOld = false;
	  boolean isDraftOld = false;
	  boolean availabilityChanged = false;

	  Set oldMembershipItemSet = null;
	  
	  if (target instanceof DiscussionForum){
		  DiscussionForum forum = ((DiscussionForum) target);
		  
		  DiscussionForum oldForum = forumManager.getForumById(forum.getId());
		  isDraftOld = oldForum.getDraft();		  

		  availabilityChanged = availabilityChanged(forum, oldForum);
	  }
	  else if (target instanceof Topic){
		  DiscussionTopic topic = ((DiscussionTopic) target);
		  isModerated = topic.getModerated();
		  
		  DiscussionTopic oldTopic = forumManager.getTopicById(topic.getId());
		  isModeratedOld = oldTopic.getModerated();
		  isDraftOld = oldTopic.getDraft();
		  
		  availabilityChanged = availabilityChanged(topic, oldTopic);
	  }
	  
	  
	  if(isModerated != isModeratedOld ||
			  isDraft != isDraftOld || availabilityChanged){
		  update = true;
	  }
	  
	  if(!update && isModerated && permissions != null && target instanceof Topic){
		  //only need to look up permission changes for moderate postings if it is moderated

		  if (target instanceof DiscussionForum){
			  oldMembershipItemSet = uiPermissionsManager.getForumItemsSet((DiscussionForum) target);
		  }else  if (target instanceof DiscussionTopic){
			  oldMembershipItemSet = uiPermissionsManager.getTopicItemsSet((DiscussionTopic) target);
		  }
		  
		  if(oldMembershipItemSet != null){
		      for (PermissionBean permBean : permissions) {
		        if(permBean.getItem().getId() == null){
		        	//this is a new permission set, a group more than likely, so update b/c this rarely happens anyways
		        	//and state isn't positive
		        	update = true;
		        	break;
		        }
		        
		        Iterator iter2 = oldMembershipItemSet.iterator();
				while(iter2.hasNext())
				{
					DBMembershipItem oldItem = (DBMembershipItem) iter2.next();
					if(permBean.getItem().getId().equals(oldItem.getId())){
						if(permBean.getModeratePostings() != oldItem.getPermissionLevel().getModeratePostings()){
							update = true;
							break;
						}
					}
				}
				if(update){
					break;
				}
		      }
		  }
	  }

	  return update;
  }

  /**
   * @return Returns the selectedTopic.
   */
  public DiscussionTopicBean getSelectedTopic()
  {
  	if(selectedTopic == null)
  	{
			log.debug("no topic is selected in getSelectedTopic.");
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
	  if(template == null){
		  template = new DiscussionAreaBean(forumManager.getDiscussionForumArea(), userTimeService);
	  }
	  return template;
  }

  
  /**
   * @return
   */
  public String processActionNewTopic()
  {   
    log.debug("processActionNewTopic()");
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

	if (isGradebookGroupEnabled()) {
		selectedTopic.setRestrictPermissionsForGroups("true");
		setGroupId(null);
		setDiscussionGeneric(false);
	}

    attachments.clear();
    prepareRemoveAttach.clear();
    siteGroups.clear();
    setFromMainOrForumOrTopic();

    return TOPIC_SETTING_REVISE;
  }

  /**
   * @return
   */
  public String processActionReviseTopicSettings()
  {
    log.debug("processActionReviseTopicSettings()");
    topicClickCount = 0;
    forumClickCount = 0;
    setPermissionMode(PERMISSION_MODE_TOPIC);
    setEditMode(true);
        
    if(selectedTopic == null)
    {
			log.debug("no topic is selected in processActionReviseTopicSettings.");
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
    selectedTopic = new DiscussionTopicBean(topic, selectedForum.getForum(), forumManager, rubricsService, userTimeService);
    loadTopicDataInTopicBean(topic, selectedTopic);
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
    siteGroups.clear();
    return TOPIC_SETTING_REVISE;
  }

  /**
   * @return
   */
  public String processActionSaveTopicAndAddTopic()
  {
    log.debug("processActionSaveTopicAndAddTopic()");
    
    if(selectedTopic != null && selectedTopic.getTopic() != null
            && selectedTopic.getTopic().getOpenDate() != null && selectedTopic.getTopic().getCloseDate() != null
    		&& selectedTopic.getTopic().getAvailabilityRestricted()){
    	//check whether the close date is after the open date or not:
    	if(selectedTopic.getTopic().getOpenDate().after(selectedTopic.getTopic().getCloseDate())){
    		setErrorMessage(getResourceBundleString(END_DATE_BEFORE_OPEN_DATE));
    		return null;
    	}
    }
    
    if(topicClickCount != 0 || forumClickCount != 0) {
    	setErrorMessage(getResourceBundleString(MULTIPLE_WINDOWS , new Object[] {ServerConfigurationService.getString("ui.service","Sakai")}));
    	return TOPIC_SETTING_REVISE;
    }
    if(selectedTopic!=null && selectedTopic.getTopic()!=null &&
    		(selectedTopic.getTopic().getShortDescription()!=null))
    {
    	if(selectedTopic.getTopic().getShortDescription().length() > 255){
    		setErrorMessage(getResourceBundleString(SHORT_DESC_TOO_LONG));
    		return null;
    	}
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
    		!selectedTopic.getTopic().getModerated() && selectedTopic.getTopic().getId() != null)
    {
    	forumManager.approveAllPendingMessages(selectedTopic.getTopic().getId());
    }

    if(processTopicSettings(false) == null){
        return null;
    }

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
    siteGroups.clear();
    selectedTopic.getTopic().setRestrictPermissionsForGroups(false);

    return TOPIC_SETTING_REVISE;

  }

  /**
   * @return
   */
  public String processActionSaveTopicSettings()
  {
    log.debug("processActionSaveTopicSettings()");
    
    if(selectedTopic != null && selectedTopic.getTopic() != null 
            && selectedTopic.getTopic().getOpenDate() != null && selectedTopic.getTopic().getCloseDate() != null
    		&& selectedTopic.getTopic().getAvailabilityRestricted()){
    	//check whether the close date is after the open date or not:
    	if(selectedTopic.getTopic().getOpenDate().after(selectedTopic.getTopic().getCloseDate())){
    		setErrorMessage(getResourceBundleString(END_DATE_BEFORE_OPEN_DATE));
    		return null;
    	}
    }
    
    if(topicClickCount != 0 || forumClickCount != 0) {
    	setErrorMessage(getResourceBundleString(MULTIPLE_WINDOWS , new Object[] {ServerConfigurationService.getString("ui.service","Sakai")}));
    	return TOPIC_SETTING_REVISE;
    }
    
    if(selectedTopic!=null && selectedTopic.getTopic()!=null &&
    		(selectedTopic.getTopic().getShortDescription()!=null))
    {
    	if(selectedTopic.getTopic().getShortDescription().length() > 255){
    		setErrorMessage(getResourceBundleString(SHORT_DESC_TOO_LONG));
    		return null;
    	}
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
    		!selectedTopic.getTopic().getModerated())
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

    if(processTopicSettings(false) == null){
        return null;
    }

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
    log.debug("processActionSaveTopicAsDraft()");
    
    if(selectedTopic != null && selectedTopic.getTopic() != null
            && selectedTopic.getTopic().getOpenDate() != null && selectedTopic.getTopic().getCloseDate() != null
    		&& selectedTopic.getTopic().getAvailabilityRestricted()){
    	//check whether the close date is after the open date or not:
    	if(selectedTopic.getTopic().getOpenDate().after(selectedTopic.getTopic().getCloseDate())){
    		setErrorMessage(getResourceBundleString(END_DATE_BEFORE_OPEN_DATE));
    		return null;
    	}
    }
    
    if(topicClickCount != 0 || forumClickCount != 0) {
    	setErrorMessage(getResourceBundleString(MULTIPLE_WINDOWS , new Object[] {ServerConfigurationService.getString("ui.service","Sakai")}));
    	return TOPIC_SETTING_REVISE;
    }
    if(selectedTopic!=null && selectedTopic.getTopic()!=null &&
    		(selectedTopic.getTopic().getShortDescription()!=null))
    {
    	if(selectedTopic.getTopic().getShortDescription().length() > 255){
    		setErrorMessage(getResourceBundleString(SHORT_DESC_TOO_LONG));
    		return null;
    	}
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
    if(processTopicSettings(true) == null){
        return null;
    }
    //reset();
    //return MAIN;
    
    return processReturnToOriginatingPage();
  }

  private String processTopicSettings(boolean draft){
	if (isGradebookGroupEnabled()) {
		if (selectedTopic.getTopic().getId() == null) {
			String newDefaultAssignName = selectedTopic.getGradeAssign();

			if (!selectedTopic.getTopic().getRestrictPermissionsForGroups() &&
				!StringUtils.isBlank(newDefaultAssignName) && !newDefaultAssignName.equals(DEFAULT_GB_ITEM)) {
				setErrorMessage(getResourceBundleString(MULTI_GRADEBOOK_GROUP_ITEMS_ERROR));
				return null;
			}
		}

		String currentDefaultAssignName = selectedTopic.getTopic().getDefaultAssignName();

		if (selectedTopic.getTopic().getId() != null &&
			currentDefaultAssignName != null && !StringUtils.isBlank(currentDefaultAssignName)) {
			if (!checkUpdateSettings(false)) {
				return null;
			}

			selectedTopic.getTopic().setDefaultAssignName(selectedTopic.getGradeAssign());
		}
	}

    if (selectedTopic.getTopic().getRestrictPermissionsForGroups() && selectedTopic.getTopic().getId() == null) {
        if (!saveTopicsForGroups(draft)) {
            return null;
        }
    } else {
		saveTopicSettings(draft);
    }
    return gotoMain();
  }

  private String saveTopicSettings(boolean draft)
  {
    log.debug("saveTopicSettings({})", draft);
    setPermissionMode(PERMISSION_MODE_TOPIC);
    if (selectedTopic != null)
    {
      DiscussionTopic topic = selectedTopic.getTopic();
      if (selectedForum != null)
      {
        boolean isNew = topic.getId() == null;
        boolean permissionsUpdated = false;
        if(!isNew){
          permissionsUpdated = needToUpdateSynopticOnForumSave(topic, draft);
        }
        boolean synopticUpdate = isNew ? false : permissionsUpdated;
        HashMap<String, Integer> beforeChangeHM = null;
        if(!isNew && synopticUpdate){
          beforeChangeHM = SynopticMsgcntrManagerCover.getUserToNewMessagesForForumMap(getSiteId(), topic.getBaseForum().getId(), topic.getId());
        }

        if(topic.getShortDescription()!=null && topic.getShortDescription().length() > 255){
          topic.setShortDescription(topic.getShortDescription().substring(0, 255));
        }

        topic.setExtendedDescription(formattedText.processFormattedText(topic.getExtendedDescription(), null, null));

        if ("<br/>".equals(topic.getExtendedDescription()))
        {
          topic.setExtendedDescription("");
        }

        topic.setBaseForum(selectedForum.getForum());
        if(selectedForum.getForum().getRestrictPermissionsForGroups() && ServerConfigurationService.getBoolean("msgcntr.restricted.group.perms", false)){
          topic.setRestrictPermissionsForGroups(true);
        }

        if (!isNew
            && Boolean.TRUE.equals(topic.getAvailabilityRestricted())
            && Boolean.TRUE.equals(topic.getLockedAfterClosed())
            && Boolean.TRUE.equals(topic.getLocked())) {
          DiscussionTopic persistedTopic = forumManager.getTopicById(topic.getId());
          Date persistedCloseDate = persistedTopic != null ? persistedTopic.getCloseDate() : null;
          Date closeDate = topic.getCloseDate();
          if (persistedCloseDate != null
              && persistedCloseDate.before(new Date())
              && closeDate != null
              && closeDate.after(new Date())) {
            topic.setLocked(false);
            selectedTopic.setTopicLocked(false);
          }
        }
        if(topic.getCreatedBy()==null&&this.forumManager.getAnonRole()==true){
          topic.setCreatedBy(".anon");
        }
        if(topic.getModifiedBy()==null&&this.forumManager.getAnonRole()==true){
          topic.setModifiedBy(".anon");
        }

		if (!isGradebookGroupEnabled()) {
			saveTopicSelectedAssignment(topic);
		}

        saveTopicAttach(topic);
        setObjectPermissions(topic);
        processActionSendToCalendar(topic);
        topic = forumManager.saveTopic(topic, draft);

        //anytime a forum settings change, we should update synoptic info for forums
        if (!isNew && beforeChangeHM != null) {
          updateSynopticMessagesForForumComparingOldMessagesCount(getSiteId(), topic.getBaseForum().getId(), topic.getId(), beforeChangeHM, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
        }

		if (!isGradebookGroupEnabled()) {
			// Update or create task if needed
			String gradeAssign = selectedTopic.getGradeAssign();
			if (!draft) {
				GradingService gradingService = getGradingService();
				String gradebookUid = toolManager.getCurrentPlacement().getContext();
				Assignment assignment = gradingService.getAssignmentByNameOrId(gradebookUid, gradebookUid, gradeAssign);
				Date dueDate = (assignment != null ? assignment.getDueDate() : null);
				String reference = DiscussionForumService.REFERENCE_ROOT + SEPARATOR + getSiteId() + SEPARATOR + topic.getBaseForum().getId() + TOPIC_REF + topic.getId();
				Optional<Task> optTask = taskService.getTask(reference);
				if (optTask.isPresent()) {
					this.updateTask(optTask.get(), topic.getTitle(), dueDate);
				} else if (this.selectedTopic.isCreateTask() && StringUtils.isNotBlank(gradeAssign) && !DEFAULT_GB_ITEM.equals(gradeAssign) ) {
					this.createTask(reference, topic.getTitle(), dueDate);
				}
			}
		}
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
		  log.debug("processActionTopicSettings()");

		  DiscussionTopic topic = null;
		  String topicId = getExternalParameterByKey(TOPIC_ID);

		  if(StringUtils.isNotBlank(topicId) && !"null".equals(topicId) ){
			  topic = (DiscussionTopic) forumManager.getTopicByIdWithAttachments(Long.valueOf(topicId));
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
		  selectedTopic = new DiscussionTopicBean(topic, selectedForum.getForum(), forumManager, rubricsService, userTimeService);
		  loadTopicDataInTopicBean(topic, selectedTopic);
		  selectedTopic.setMarkForDeletion(true);
		    return TOPIC_SETTING;
	  }
  }

  
  /**
   * @return
   */
  public String processActionDeleteTopicConfirm()
  {
    log.debug("processActionDeleteTopicConfirm()");
    
    if (selectedTopic == null)
    {
      log.debug("There is no topic selected for deletion");
      return gotoMain();
    }
    if(!uiPermissionsManager.isChangeSettings(selectedTopic.getTopic(),selectedForum.getForum()))
    {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_NEW_TOPIC));
      return gotoMain();
    }
    //in case XSS was slipped in, make sure we remove it:
    selectedTopic.getTopic().setExtendedDescription(formattedText.processFormattedText(selectedTopic.getTopic().getExtendedDescription(), null, null));
    selectedTopic.setMarkForDeletion(true);
    return TOPIC_SETTING;
  }


  /**
   * @return
   */
  public String processActionDeleteTopic() {   
    log.debug("processActionDeleteTopic()");
    if (selectedTopic == null) {
      log.debug("There is no topic selected for deletion");
      return gotoMain();
    }
    if(!uiPermissionsManager.isChangeSettings(selectedTopic.getTopic(),selectedForum.getForum())) {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_NEW_TOPIC));
      return gotoMain();
    }
  
    HashMap<String, Integer> beforeChangeHM = null;    
    Long forumId = selectedTopic.getTopic().getBaseForum().getId();
    Long topicId = selectedTopic.getTopic().getId();
    beforeChangeHM = SynopticMsgcntrManagerCover.getUserToNewMessagesForForumMap(getSiteId(), forumId, topicId);

    forumManager.deleteTopic(selectedTopic.getTopic());

    if(beforeChangeHM != null){
        updateSynopticMessagesForForumComparingOldMessagesCount(getSiteId(), forumId, topicId, beforeChangeHM, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
    }
    
    // Delete task
    String reference = DiscussionForumService.REFERENCE_ROOT + SEPARATOR + getSiteId() + SEPARATOR + forumId + TOPIC_REF + topicId;
    taskService.removeTaskByReference(reference);

    reset();
    return gotoMain();
  }

  /**
   * @return
   */
  public String processActionTopicSettings()
  {
    log.debug("processActionTopicSettings()");
    
    topicClickCount = 0;
    forumClickCount = 0;
    
    setEditMode(true);
    setPermissionMode(PERMISSION_MODE_TOPIC);
    permissions=null;
    
    DiscussionTopic topic = null;
    String topicId = getExternalParameterByKey(TOPIC_ID);
    if(StringUtils.isNotBlank(topicId) && !"null".equals(topicId)){
	    topic = (DiscussionTopic) forumManager
	        .getTopicByIdWithAttachments(Long.valueOf(
	            topicId));
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
    selectedTopic = new DiscussionTopicBean(topic, selectedForum.getForum(), forumManager, rubricsService, userTimeService);
    loadTopicDataInTopicBean(topic, selectedTopic);
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

	String currentDefaultAssignName = topic.getDefaultAssignName();

	selectedTopic.setGradeAssign(currentDefaultAssignName);

	if (isGradebookGroupEnabled()) {
		if (currentDefaultAssignName != null && !StringUtils.isBlank(currentDefaultAssignName)) {
			GradingService gradingService = getGradingService();
			String gbUid = gradingService.getGradebookUidByAssignmentById(toolManager.getCurrentPlacement().getContext(), Long.parseLong(currentDefaultAssignName));
			setGroupId(gbUid);
			setDiscussionGeneric(false);
		} else {
			setDiscussionGeneric(true);
		}
	}

    siteGroups.clear();
    setTopicBeanAssign();
    setFromMainOrForumOrTopic();
    
    //return TOPIC_SETTING;
    return TOPIC_SETTING_REVISE;
  }

  public String processActionToggleDisplayForumExtendedDescription()
  {
    log.debug("processActionToggleDisplayForumExtendedDescription()");
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
    log.debug("processActionToggleDisplayExtendedDescription()");
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
 				log.debug("no topic is selected in processActionToggleDisplayExtendedDescription.");
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
 				log.debug("no topic is selected in processActionToggleDisplayExtendedDescription.");
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
 				log.debug("no topic is selected in processActionToggleDisplayExtendedDescription.");
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

  /*
  * Send the open and close dates of a Forum or Forum Topic to the Calendar.
  * MutableEntity is a parent class of both DiscussionForums and DiscussionTopics.
  * The empty values declared at the beginning are used throughout this method's creation and maintenance of the Calendar sending.
  */
  private void processActionSendToCalendar(MutableEntity forumItem) {
    String calendarBeginId;
    String calendarEndId;
    Boolean sendOpenCloseToCalendar;
    Boolean availabilityRestrictedNow;
    String title;
    Date openDate;
    Date closeDate;
    String openingTitle;
    String closingTitle;
    Set<DBMembershipItem> membershipItems;
    if (forumItem instanceof DiscussionTopic) {  //this part of the processing determines if the MutableEntity is a Forum or a Topic and fills in all the variables by appropriately casting.
      DiscussionTopic topicNow = ((DiscussionTopic) forumItem);
      calendarBeginId = topicNow.getCalendarBeginId();
      calendarEndId = topicNow.getCalendarEndId();
      sendOpenCloseToCalendar = topicNow.getSendOpenCloseToCalendar();
      openDate = topicNow.getOpenDate();
      closeDate = topicNow.getCloseDate();
      membershipItems = topicNow.getMembershipItemSet();
      availabilityRestrictedNow = topicNow.getAvailabilityRestricted();
      openingTitle = getResourceBundleString("sendOpenCloseToCalendar.title.topic.assembly",new Object[]{topicNow.getTitle(),getResourceBundleString("sendOpenCloseToCalendar.topic.open")});
      closingTitle = getResourceBundleString("sendOpenCloseToCalendar.title.topic.assembly",new Object[]{topicNow.getTitle(),getResourceBundleString("sendOpenCloseToCalendar.topic.close")});
    } else if (forumItem instanceof DiscussionForum) {
      DiscussionForum forumNow = ((DiscussionForum) forumItem);
      calendarBeginId = forumNow.getCalendarBeginId();
      calendarEndId = forumNow.getCalendarEndId();
      sendOpenCloseToCalendar = forumNow.getSendOpenCloseToCalendar();
      openDate = forumNow.getOpenDate();
      closeDate = forumNow.getCloseDate();
      membershipItems = forumNow.getMembershipItemSet();
      availabilityRestrictedNow = forumNow.getAvailabilityRestricted();
      openingTitle = getResourceBundleString("sendOpenCloseToCalendar.title.forum.assembly",new Object[]{forumNow.getTitle(),getResourceBundleString("sendOpenCloseToCalendar.topic.open")});
      closingTitle = getResourceBundleString("sendOpenCloseToCalendar.title.forum.assembly",new Object[]{forumNow.getTitle(),getResourceBundleString("sendOpenCloseToCalendar.topic.close")});
    } else {  //don't do anything with forumItem or the Calendar if it's not a Forum or a Forum Topic.
      return;
    }

    Collection<Group> allowedGroups = getAllowedGroups(membershipItems);
    try {   //now actually start processing the data for Calendar.
      Calendar targetCalendar = this.calendarService.getCalendar(calendarService.calendarReference(getContextSiteId().replace("/site/",""), siteService.MAIN_CONTAINER));
      if(targetCalendar == null){
        return; //no further processing is possible if the calendar has not been found.
      }
      CalendarEventEdit begin = null;
      CalendarEventEdit end = null;
      if (sendOpenCloseToCalendar && availabilityRestrictedNow) {
        if (calendarBeginId != null) { //if there is already a Calendar record for the forum opening
          try {
            begin = targetCalendar.getEditEvent(calendarBeginId, CalendarService.EVENT_MODIFY_CALENDAR);
          } catch (IdUnusedException e) {
            // If we couldn't get Begin from the calendar, it likely means that the event was deleted from Calendar without consulting Forums,
            // so Forums has an ID saved for a Calendar event that won't exist. We can just create it.
            begin = targetCalendar.addEvent();
          }
          if (openDate != null) {
            begin.setDisplayName(openingTitle);
            begin.setDescription(getResourceBundleString("sendOpenCloseToCalendar.description.assembly",new Object[]{openingTitle,openDate.toString()}));
            begin.setType(getResourceBundleString("sendOpenCloseToCalendar.type"));
            begin.setGroupAccess(allowedGroups, false);
            begin.setRange(this.timeService.newTimeRange(openDate.getTime(), 0));
            begin.setField(CalendarConstants.EVENT_OWNED_BY_TOOL_ID, FORUMS_TOOL_ID);
            targetCalendar.commitEvent(begin);
          } else {  //if there is a Calendar event for the current forum item, but the open date is cleared, we interpret this as a removal.
            targetCalendar.removeEvent(begin);
            calendarBeginId = null;
          }
        } else if (calendarBeginId == null && openDate != null) { //if there is not already a Calendar record for this forum item, we create one if there is actually an Open date set.
          begin = targetCalendar.addEvent();
          begin.setDisplayName(openingTitle);
          begin.setDescription(getResourceBundleString("sendOpenCloseToCalendar.description.assembly",new Object[]{openingTitle,openDate.toString()}));
          begin.setType(getResourceBundleString("sendOpenCloseToCalendar.type"));
          begin.setGroupAccess(allowedGroups, false);
          begin.setRange(this.timeService.newTimeRange(openDate.getTime(), 0));
          begin.setField(CalendarConstants.EVENT_OWNED_BY_TOOL_ID, FORUMS_TOOL_ID);
          targetCalendar.commitEvent(begin);
        }
        if (calendarEndId != null) {
          try {
            end = targetCalendar.getEditEvent(calendarEndId, CalendarService.EVENT_MODIFY_CALENDAR);
          } catch (IdUnusedException e) {
            // This might happen if the event was removed from Calendar without this Forum data being changed,
            // so Forums would have a record for a Calendar event that doesn't exist. We can just create it.
            end = targetCalendar.addEvent();
          }
          if (closeDate != null) {
            end.setDisplayName(closingTitle);
            end.setDescription(getResourceBundleString("sendOpenCloseToCalendar.description.assembly",new Object[]{closingTitle,closeDate.toString()}));
            end.setType(getResourceBundleString("sendOpenCloseToCalendar.type"));
            end.setGroupAccess(allowedGroups, false);
            end.setRange(this.timeService.newTimeRange(closeDate.getTime(), 0));
            end.setField(CalendarConstants.EVENT_OWNED_BY_TOOL_ID, FORUMS_TOOL_ID);
            targetCalendar.commitEvent(end);
          } else {  //if there is a Calendar record for the closing, but no close date, we interpret it as a removal.
            targetCalendar.removeEvent(end);
            calendarEndId = null;
          }
        } else if (calendarEndId == null && closeDate != null) {  //when there is not already a Calendar record for this closing date and there is a Close date actually set.
          end = targetCalendar.addEvent();
          end.setDisplayName(closingTitle);
          end.setDescription(getResourceBundleString("sendOpenCloseToCalendar.description.assembly",new Object[]{closingTitle,closeDate.toString()}));
          end.setType(getResourceBundleString("sendOpenCloseToCalendar.type"));
          end.setGroupAccess(allowedGroups, false);
          end.setRange(this.timeService.newTimeRange(closeDate.getTime(), 0));
          end.setField(CalendarConstants.EVENT_OWNED_BY_TOOL_ID, FORUMS_TOOL_ID);
          targetCalendar.commitEvent(end);
        }
        if (begin != null) { //put the Calendar entry for Open in the Forum data.
          if (forumItem instanceof DiscussionTopic) {
            ((DiscussionTopic) forumItem).setCalendarBeginId(begin.getId());
          } else if (forumItem instanceof DiscussionForum) {
            ((DiscussionForum) forumItem).setCalendarBeginId(begin.getId());
          }
        } else {
          if (forumItem instanceof DiscussionTopic) {
            ((DiscussionTopic) forumItem).setCalendarBeginId(null);
          } else if (forumItem instanceof DiscussionForum) {
            ((DiscussionForum) forumItem).setCalendarBeginId(null);
          }
        }
        if (end != null) {  //put the Calendar entry for End in the forum data.
          if (forumItem instanceof DiscussionTopic) {
            ((DiscussionTopic) forumItem).setCalendarEndId(end.getId());
          } else if (forumItem instanceof DiscussionForum) {
            ((DiscussionForum) forumItem).setCalendarEndId(end.getId());
          }
        } else {
          if (forumItem instanceof DiscussionTopic) {
            ((DiscussionTopic) forumItem).setCalendarEndId(null);
          } else if (forumItem instanceof DiscussionForum) {
            ((DiscussionForum) forumItem).setCalendarEndId(null);
          }
        }
      } else {  // when Send To Calendar is not checked
        if (calendarBeginId != null) {  //don't mess with the Calendar unless there have previously been Calendar events created for this topic
          try {
            begin = targetCalendar.getEditEvent(calendarBeginId, CalendarService.EVENT_MODIFY_CALENDAR);
            targetCalendar.removeEvent(begin);
          } catch (IdUnusedException e) { //if we couldn't get Begin from the calendar, it likely means that the event was deleted from Calendar without consulting Forums, so Forums has an ID saved for a Calendar event that won't exist. We can just create it.
            log.info(e.toString());
          }
        }
        if (calendarEndId != null) {
          try {
            end = targetCalendar.getEditEvent(calendarEndId, CalendarService.EVENT_MODIFY_CALENDAR);
            targetCalendar.removeEvent(end);
          } catch (IdUnusedException e) { //this might happen if the event was removed from Calendar without this Forum data being changed, so Forums would have a record for a Calendar event that doesn't exist. We can just create it.
            log.info(e.toString());
          }

        }
        if (forumItem instanceof DiscussionTopic) { //put the null begin/end IDs in the forumItem itself as well, depending on type.
          ((DiscussionTopic) forumItem).setCalendarBeginId(null);
          ((DiscussionTopic) forumItem).setCalendarEndId(null);
        } else if (forumItem instanceof DiscussionForum) {
          ((DiscussionForum) forumItem).setCalendarBeginId(null);
          ((DiscussionForum) forumItem).setCalendarEndId(null);
        }
      }
    } catch (IdUnusedException e) { //for getCalendar, if the Calendar doesn't exist.
      log.error(e.toString());
    } catch (PermissionException e) { //for addEvent, if the user can't edit a calendar.
      log.error(e.toString());
    } catch (InUseException e) {  //for getEditEvent, if it's already being edited.
      log.error(e.toString());
    }
  }

  public Boolean getDoesSiteHaveCalendar() {  //if any Calendar data exists for this site, this method returns True. It's not necessarily based on the literal presence of the Calendar tool in the site, but on Calendar-related data.
    try { //just look for a usable calendar with this site's ID.
      this.calendarService.getCalendar(calendarService.calendarReference(getContextSiteId().replace("/site/",""), siteService.MAIN_CONTAINER));
    } catch (IdUnusedException e) { //this would be the normal Catch case...when the calendar we tried to get just doesn't exist.
      log.debug(" No Calendar data exist for this site.");
      return false;
    } catch (PermissionException e) { //this just means the user can't edit. Probably rare.
      log.debug(" User does not have permission to edit this calendar.");
      return false;
    }
    return true;  // if no exceptions get thrown, return True...either usable Calendar data exists, or the Calendar tool is in the site.
  }

  private Collection<Group> getAllowedGroups(Set<DBMembershipItem> membershipItems) {  //get the groups allowed to see a Forum item's related Calendar events.
    if (membershipItems == null) {
      return Collections.emptyList();
    }
    Collection<Group> output = new ArrayList<>();
    ArrayList<SiteGroupBean> availableGroups = (ArrayList<SiteGroupBean>) getSiteGroups();
    List<String> availableGroupNames = availableGroups.stream().map(ag -> ag.getGroup().getTitle()).collect(Collectors.toList());
    for(DBMembershipItem now: membershipItems) {
      if (availableGroupNames.contains(now.getName()) && !now.getPermissionLevelName().equals("None")) { //we want the groups that are available to the site, and have some kind of permission to see the forum item.
        for (int count = 0; count < availableGroups.size(); count++) {
          if (now.getName().equals(availableGroups.get(count).getGroup().getTitle())) {
            output.add(availableGroups.get(count).getGroup());
          }
        }
      }
    }
    return output;
  }

  /**
   * @return
   */
  public String processActionDisplayTopic()
  {
    log.debug("processActionDisplayTopic()");
    return displayTopicById(TOPIC_ID);
  }

  /**
   * @return
   */
  public String processActionDisplayNextTopic()
  {
    log.debug("processActionDisplayNextTopic()");
    return displayTopicById("nextTopicId");
  }

  /**
   * @return
   */
  public String processActionDisplayPreviousTopic()
  {
    log.debug("processActionDisplayNextTopic()");
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
    
  public boolean getNeedToPostFirst(){
	  String currentUserId = getUserId();
	  List<String> currentUser = new ArrayList<String>();
	  currentUser.add(currentUserId);
	  if (selectedTopic == null) {
	      log.warn("selectedTopic null in getNeedToPostFirst");
	      return true;
	  } else {
	      return getNeedToPostFirst(currentUser, selectedTopic.getTopic(), selectedTopic.getMessages()).contains(currentUserId);
	  }
  }

  /**
   * takes a list of userIds and returns a filtered list of users who need to post first
   * @param userIds
   * @return
   */
  private List<String> getNeedToPostFirst(List<String> userIds, DiscussionTopic topic, List messages){
	  List returnList = new ArrayList<String>();
	  if(topic != null && topic.getPostFirst()){
		  for(String userId : userIds){
			  boolean needToPost = true;
			  //make sure the user has posted before they can view all messages
			  //only need to force this for users who do not have "ChangeSettings" permission
			  for (Object messageObj : messages) {
				  Message message = null;
				  if(messageObj instanceof DiscussionMessageBean){
					  message = ((DiscussionMessageBean) messageObj).getMessage();
				  }else if(messageObj instanceof Message){
					  message = (Message) messageObj;
				  }
				  if(message != null && message.getCreatedBy().equals(userId) && 
						  !message.getDraft() && 
						  ((message.getApproved() != null && message.getApproved()) || !topic.getModerated() ||
								  message.getCreatedBy().equals(userId)) &&
						  !message.getDeleted()){
					  needToPost = false;
					  break;
				  }
			  }
			  if(needToPost && !(uiPermissionsManager.isChangeSettings(topic, (DiscussionForum) topic.getBaseForum(), userId)
					   || uiPermissionsManager.isPostToGradebook(topic, (DiscussionForum) topic.getBaseForum(), userId)
					   || uiPermissionsManager.isModeratePostings(topic, (DiscussionForum) topic.getBaseForum(), userId))){
				  returnList.add(userId);
			  }
		  }
	  }
	  
	  return returnList;
  }
  
  public String processActionGetDisplayThread()
  {
  		if(selectedTopic == null)
  		{
  			log.debug("no topic is selected in processActionGetDisplayThread.");
  			return gotoMain();
  		}
	  	selectedTopic = getDecoratedTopic(selectedTopic.getTopic());
	  	
	  	setTopicBeanAssign();
	  	selectedTopic = getSelectedTopic();
	    
	    List msgsList = selectedTopic.getMessages();
	    
	    if (msgsList != null && !msgsList.isEmpty())
	    	msgsList = filterModeratedMessages(msgsList, selectedTopic.getTopic(), (DiscussionForum) selectedTopic.getTopic().getBaseForum());
	    
	    List orderedList = new ArrayList();
	    selectedThread = new ArrayList();
	    
	    Boolean foundHead = false;
	    Boolean foundAfterHead = false;
        threadMoved = didThreadMove();
	    
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
	    if (!threadMoved) {
	    recursiveGetThreadedMsgsFromList(msgsList, orderedList, selectedThreadHead);
	    selectedThread.addAll(orderedList);
	    }
	    
	    // now process the complete list of messages in the selected thread to possibly flag as read
	    // if this topic is flagged to autoMarkThreadsRead, mark each message in the thread as read
	    // mark all as read
	    if (selectedTopic.getTopic().getAutoMarkThreadsRead()) {
	    	for (int i = 0; i < selectedThread.size(); i++) {
	    		messageManager.markMessageNotReadForUser(selectedTopic.getTopic().getId(), ((DiscussionMessageBean)selectedThread.get(i)).getMessage().getId(), false);
	    		((DiscussionMessageBean)selectedThread.get(i)).setRead(Boolean.TRUE);
	    	}
	    }

	    boolean postFirst = getNeedToPostFirst();	    
	    if(postFirst){
	    	//user can't view this message until they have posted a message:
	    	selectedMessage = null;
	    }

		showThreadChanges = true;
	    return THREAD_VIEW;
  }

	public String processActionGetDisplayThread(boolean readStatus) {
		if (selectedTopic == null) {
			log.debug("no topic is selected in processActionGetDisplayThread.");
			return gotoMain();
		}
		selectedTopic = getDecoratedTopic(selectedTopic.getTopic());

		setTopicBeanAssign();
		selectedTopic = getSelectedTopic();

		List<DiscussionMessageBean> msgsList = selectedTopic.getMessages();

		if (msgsList != null && !msgsList.isEmpty())
			msgsList = filterModeratedMessages(msgsList, selectedTopic.getTopic(),
					(DiscussionForum) selectedTopic.getTopic().getBaseForum());

		List<DiscussionMessageBean> orderedList = new ArrayList<>();
		selectedThread = new ArrayList();

		Boolean foundHead = false;
		Boolean foundAfterHead = false;
		threadMoved = didThreadMove();

		// determine to make sure that selectedThreadHead does exist!
		if (selectedThreadHead == null) {
			return MAIN;
		}

		for (DiscussionMessageBean msg : msgsList) {
			if (msg.getMessage().getId()
					.equals(selectedThreadHead.getMessage().getId())) {
				msg.setDepth(0);
				selectedThread.add(msg);
				foundHead = true;
			} else if (msg.getMessage().getInReplyTo() == null && foundHead
					&& !foundAfterHead) {
				selectedThreadHead.setHasNextThread(true);
				selectedThreadHead.setNextThreadId(msg.getMessage().getId());
				foundAfterHead = true;
			} else if (msg.getMessage().getInReplyTo() == null && !foundHead) {
				selectedThreadHead.setHasPreThread(true);
				selectedThreadHead.setPreThreadId(msg.getMessage().getId());
			}
		}
		formatMessagesByRemovelastEmptyLines(msgsList);
		if (!threadMoved) {
			recursiveGetThreadedMsgsFromList(msgsList, orderedList, selectedThreadHead);
			selectedThread.addAll(orderedList);
		}

		// mark all as not read
		selectedThread.forEach(msg -> {
			messageManager.markMessageNotReadForUser(selectedTopic.getTopic().getId(),
					((DiscussionMessageBean) msg).getMessage().getId(), readStatus); // true
			((DiscussionMessageBean) msg).setRead(Boolean.FALSE);
		});

		boolean postFirst = getNeedToPostFirst();
		if (postFirst) {
			// user can't view this message until they have posted a message:
			selectedMessage = null;
		}

		showThreadChanges = false;
		return THREAD_VIEW;
	}

    private boolean didThreadMove() {
        threadMoved = false;
        String message = selectedThreadHead.getMessage().toString();
        List msgsList = selectedTopic.getMessages();
        boolean listHasMessage = false;
        for (int i = 0; i < msgsList.size(); i++) {
            listHasMessage = message.equals(((DiscussionMessageBean) msgsList.get(i)).getMessage().toString());
            if (listHasMessage) {
                break;
            }
        }
        threadMoved = !listHasMessage;
        return threadMoved;
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
	    log.debug("processActionDisplayThread()");
	    
	    selectedMessageCount ++;

	    threadAnchorMessageId = null;
	    String threadId = getExternalParameterByKey(MESSAGE_ID);
	    String topicId = getExternalParameterByKey(TOPIC_ID);
	    if ("".equals(threadId) || null == threadId || "null".equals(threadId))
	    {
	      setErrorMessage(getResourceBundleString(MESSAGE_REFERENCE_NOT_FOUND));
	      return gotoMain();
	    }
	    if ("".equals(topicId) || null == topicId || "null".equals(topicId))
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
	    selectedThreadHead = getThreadHeadForMessage(threadMessage);
	    threadMessage = selectedThreadHead.getMessage();

	    DiscussionTopic topic=forumManager.getTopicById(Long.valueOf(topicId));
	    selectedMessage = selectedThreadHead;
	    setSelectedForumForCurrentTopic(topic);
	    selectedTopic = new DiscussionTopicBean(topic, selectedForum.getForum(), forumManager, rubricsService, userTimeService);
	    loadTopicDataInTopicBean(topic, selectedTopic);
	    if(topic == null || selectedTopic == null)
	    {
	    	log.debug("topic or selectedTopic is null in processActionDisplayThread.");
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
	      selectedForum = new DiscussionForumBean(forum, forumManager, userTimeService);
	      loadForumDataInForumBean(forum, selectedForum);
	      setForumBeanAssign();
	      selectedTopic.getTopic().setBaseForum(forum);
	    }
	    // don't need this here b/c done in processActionGetDisplayThread();
	    // selectedTopic = getDecoratedTopic(topic);
	    LRS_Statement statement = forumManager.getStatementForUserReadViewed(threadMessage.getTitle(), "thread").orElse(null);
        Event event = eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_READ, getEventReference(threadMessage), null, true, NotificationService.NOTI_OPTIONAL, statement);
        eventTrackingService.post(event);

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
    log.debug("processActionDisplayMessage()");
    
   selectedMessageCount ++;

    String messageId = getExternalParameterByKey(MESSAGE_ID);
    String topicId = getExternalParameterByKey(TOPIC_ID);
    if (messageId == null || "".equals(messageId))
    {
      setErrorMessage(getResourceBundleString(MESSAGE_REFERENCE_NOT_FOUND));
      return gotoMain();
    }
    if (topicId == null || "".equals(topicId))
    {
      setErrorMessage(getResourceBundleString(TOPC_REFERENCE_NOT_FOUND));
      return gotoMain();
    }
    // Message message=forumManager.getMessageById(Long.valueOf(messageId));
    messageManager.markMessageNotReadForUser(Long.valueOf(topicId),
            Long.valueOf(messageId), false);
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
    selectedTopic = new DiscussionTopicBean(topic, selectedForum.getForum(), forumManager, rubricsService, userTimeService);
    loadTopicDataInTopicBean(topic, selectedTopic);
    if(topic == null || selectedTopic == null)
    {
    	log.debug("topic or selectedTopic is null in processActionDisplayMessage.");
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
      selectedForum = new DiscussionForumBean(forum, forumManager, userTimeService);
      loadForumDataInForumBean(forum, selectedForum);
      setForumBeanAssign();
      selectedTopic.getTopic().setBaseForum(forum);
    }
    selectedTopic = getDecoratedTopic(topic);
    setTopicBeanAssign();
    selectedTopic = getSelectedTopic();
    //get thread from message
    getThreadFromMessage();
    refreshSelectedMessageSettings(message);
    // selectedTopic= new DiscussionTopicBean(message.getTopic()); 
    LRS_Statement statement = forumManager.getStatementForUserReadViewed(message.getTitle(), "thread").orElse(null);
	Event event = eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_READ, getEventReference(message), null, true, NotificationService.NOTI_OPTIONAL, statement);
    eventTrackingService.post(event);

    return MESSAGE_VIEW;
  }
  
  
  public void getThreadFromMessage()
  {
	  if(selectedMessage != null){
	    Message mes = selectedMessage.getMessage();
	    String messageId = mes.getId().toString();
	    while( mes.getInReplyTo() != null) {
	    	mes = messageManager.getMessageById(mes.getInReplyTo().getId());
	    }
	    selectedThreadHead = new DiscussionMessageBean(mes, messageManager);
	    
	    if(selectedTopic == null)
	    {
	    	log.debug("selectedTopic is null in getThreadFromMessage.");
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
  }

  
  public String processDisplayPreviousMsg()
  {
    if(selectedTopic == null)
    {
    	log.debug("selectedTopic is null in processDisplayPreviousMsg.");
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
			
	    messageManager.markMessageNotReadForUser(selectedTopic.getTopic().getId(),
	        selectedMessage.getMessage().getId(), false);
	    
	    refreshSelectedMessageSettings(message);  
    }
    LRS_Statement statement = forumManager.getStatementForUserReadViewed(selectedMessage.getMessage().getTitle(), "thread").orElse(null);
    Event event = eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_READ, getEventReference(selectedMessage.getMessage()), null, true, NotificationService.NOTI_OPTIONAL, statement);
    eventTrackingService.post(event);
    
    return null;
  }

  public String processDfDisplayNextMsg()
  {
    if(selectedTopic == null)
    {
    	log.debug("selectedTopic is null in processDfDisplayNextMsg.");
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
			
	    messageManager.markMessageNotReadForUser(selectedTopic.getTopic().getId(),
	        selectedMessage.getMessage().getId(), false);
	    
	    refreshSelectedMessageSettings(message);  
    }
    LRS_Statement statement = forumManager.getStatementForUserReadViewed(selectedMessage.getMessage().getTitle(), "thread").orElse(null);
    Event event = eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_READ, getEventReference(selectedMessage.getMessage()), null, true, NotificationService.NOTI_OPTIONAL, statement);
    eventTrackingService.post(event);

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
	  if (log.isDebugEnabled())
	  {
		  log.debug("getDecoratedForum(DiscussionForum" + forum + ")");
	  }
	  forum = forumManager.getForumByIdWithTopicsAttachmentsAndMessages(forum.getId());
	  DiscussionForumBean decoForum = new DiscussionForumBean(forum, forumManager, userTimeService);
	  loadForumDataInForumBean(forum, decoForum);
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

	  String forumDefaultAssignName = forum.getDefaultAssignName();

	  Iterator iter = temp_topics.iterator();
	  while (iter.hasNext())
	  {
		  DiscussionTopic topic = (DiscussionTopic) iter.next();
		  if (topic == null)
				continue;
//		  TODO: put this logic in database layer
		  if (topic != null && (topic.getDraft().equals(Boolean.FALSE) && topic.getAvailability())
				  ||isInstructor()
				  ||securityService.isSuperUser()
				  ||forumManager.isTopicOwner(topic))
		  { 

			  DiscussionTopicBean decoTopic = new DiscussionTopicBean(topic, forum, forumManager, rubricsService, userTimeService);
			  loadTopicDataInTopicBean(topic, decoTopic);
			  if("true".equalsIgnoreCase(ServerConfigurationService.getString("mc.defaultLongDescription")))
			  {
				  decoTopic.setReadFullDesciption(true);
			  }

			  List topicMsgs = topic.getMessages();
			  if (topicMsgs == null || topicMsgs.size() == 0 || !decoTopic.getIsRead()) {
				  decoTopic.setTotalNoMessages(0);
				  decoTopic.setUnreadNoMessages(0);
			  } else if (!topic.getModerated() || decoTopic.getIsModeratePostings()) {
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

			  setTopicGradeAssign(decoTopic, forumDefaultAssignName);

			  decoForum.addTopic(decoTopic);
		  }

	  }
	  return decoForum;
  }

  private DiscussionForumBean getDecoratedForumWithPersistentForumAndTopics(DiscussionForum forum, Map msgIdReadStatusMap)
  {
    if (log.isDebugEnabled())
    {
      log.debug("getDecoratedForum(DiscussionForum" + forum + ")");
    }
    DiscussionForumBean decoForum = new DiscussionForumBean(forum, forumManager, userTimeService);
    loadForumDataInForumBean(forum, decoForum);
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
      if (topic != null && (topic.getDraft().equals(Boolean.FALSE)
              ||isInstructor()
              ||securityService.isSuperUser()
              ||forumManager.isTopicOwner(topic)))
      { 
          DiscussionTopicBean decoTopic = new DiscussionTopicBean(topic, forum, forumManager, rubricsService, userTimeService);
          loadTopicDataInTopicBean(topic, decoTopic);
          if("true".equalsIgnoreCase(ServerConfigurationService.getString("mc.defaultLongDescription")))
          {
          	decoTopic.setReadFullDesciption(true);
          }
          
          List topicMsgs = topic.getMessages();
          if (topicMsgs == null || topicMsgs.size() == 0) {
        	  decoTopic.setTotalNoMessages(0);
        	  decoTopic.setUnreadNoMessages(0);
          } else if (!topic.getModerated() || decoTopic.getIsModeratePostings()) {
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
    return decoForum;
  }
  /**
   * @return DiscussionForumBean
   */
  private DiscussionForumBean getDecoratedForum()
  {
    log.debug("decorateSelectedForum()");
    String forumId = getExternalParameterByKey(FORUM_ID);
    if (StringUtils.isNotBlank(forumId) && !"null".equals(forumId))
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
    log.debug("displayHomeWithExtendedForumDescription()");
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
    log.debug("displayHomeWithExtendedTopicDescription()");
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
    if (log.isDebugEnabled())
    {
      log.debug("getDecoratedTopic(DiscussionTopic " + topic + ")");
    }
    DiscussionTopicBean decoTopic = null;
    if(topic != null){
    	decoTopic = new DiscussionTopicBean(topic, selectedForum.getForum(), forumManager, rubricsService, userTimeService);
    	loadTopicDataInTopicBean(topic, decoTopic);
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
    	List temp_messages = null;
    	if (decoTopic.getIsRead()) {
    		temp_messages = forumManager.getTopicByIdWithMessagesAndAttachments(topic.getId()).getMessages();
    	}

		  // Now get messages moved from this topic

		  List moved_messages = null;
          if (decoTopic.getIsRead()) {
			  moved_messages = messageManager.findMovedMessagesByTopicId(topic.getId());
		  
			  if (log.isDebugEnabled())
			  {
				  log.debug("getDecoratedTopic(moved_messages size  " + moved_messages.size()  );
				  for (Iterator msgIter = moved_messages.iterator(); msgIter.hasNext();) {
					  Message msg = (Message) msgIter.next();
					  log.debug("moved message ids = " +  msg.getId()  + "  title : " + msg.getTitle()  + " moved to topic : " +  msg.getTopic().getId() );
				  }
			  }
		  }

		// Determine if we should display authors' userIDs or their anonIDs
		boolean useAnonymousId = isUseAnonymousId(topic);
		Map<String, String> userIdAnonIdMap = Collections.emptyMap();

		  List msgIdList = new ArrayList();
    	if (temp_messages == null || temp_messages.size() < 1)
    	{
    		decoTopic.setTotalNoMessages(0);
    		decoTopic.setUnreadNoMessages(0);
    	}
		  else {
    	if (useAnonymousId)
    	{
    		// We're in an anonymous context and there are messages. Get the anonIDs for all the authors in this topic
    		userIdAnonIdMap = getUserIdAnonIdMapForMessages(temp_messages);
    	}
    	for (Iterator msgIter = temp_messages.iterator(); msgIter.hasNext();) {
    		Message msg = (Message) msgIter.next();
    		if(msg != null && !msg.getDraft().booleanValue() && !msg.getDeleted()) {
    			msgIdList.add(msg.getId());
    		}
    	}

    	// retrieve read status for all of the messages in this topic
    	Map messageReadStatusMap=null;
    	if(getUserId()!= null){
				  if (log.isDebugEnabled()) log.debug("getting unread counts for " + getUserId());
    		messageReadStatusMap = forumManager.getReadStatusForMessagesWithId(msgIdList, getUserId());
    	}else if(getUserId() == null && this.forumManager.getAnonRole()==true){
				  if (log.isDebugEnabled()) log.debug("getting unread counts for anon user");
    		messageReadStatusMap = forumManager.getReadStatusForMessagesWithId(msgIdList, ".anon");
    	}

    	// set # read/unread msgs on topic level
    	if (!topic.getModerated() || decoTopic.getIsModeratePostings()) {
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

        setTopicGradeAssign(decoTopic, selectedForum.getForum().getDefaultAssignName());

    	Iterator iter = temp_messages.iterator();

    	final boolean isRead = decoTopic.getIsRead();
    	loadTopicDataInTopicBean(topic, decoTopic);

    	boolean decoTopicGetIsDeleteAny = decoTopic.getIsDeleteAny();
    	boolean decoTopicGetIsDeleteOwn = decoTopic.getIsDeleteOwn();
    	boolean decoTopicGetIsReviseAny = decoTopic.getIsReviseAny();
    	boolean decoTopicGetIsReviseOwn = decoTopic.getIsReviseOwn();
    	while (iter.hasNext())
    	{
    		Message message = (Message) iter.next();
    		if (message != null)
    		{
    			DiscussionMessageBean decoMsg = new DiscussionMessageBean(message,
    					messageManager);
    			// Set anonymous attributes on the bean (reduces queries later; improves performance)
    			decoMsg.setUseAnonymousId(useAnonymousId);
    			if (useAnonymousId)
    			{
    				String userId = message.getAuthorId();
    				decoMsg.setAnonId(userIdAnonIdMap.get(userId));
    			}
    			if(isRead || (decoTopic.getIsNewResponse() && decoMsg.getIsOwn()))
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
    					isOwn = ".anon".equals(decoMsg.getMessage().getCreatedBy());
    				}
    				decoMsg.setRevise(decoTopicGetIsReviseAny 
    						|| (decoTopicGetIsReviseOwn && isOwn));
    				decoMsg.setUserCanDelete(decoTopicGetIsDeleteAny || (isOwn && decoTopicGetIsDeleteOwn));
    				decoMsg.setUserCanEmail(!useAnonymousId && (isInstructor() || isSectionTA()));
    				decoTopic.addMessage(decoMsg);
    			}
				String userEid = decoMsg.getMessage().getCreatedBy();
				Rank thisrank = this.getAuthorRank(userEid);
				decoMsg.setAuthorRank(thisrank);
				decoMsg.setAuthorPostCount(userEid);
    		}
    	}
    }
		  //  now add moved messages to decoTopic
    	if(moved_messages != null){
		  for (Iterator msgIter = moved_messages.iterator(); msgIter.hasNext();) {
			  Message message = (Message) msgIter.next();
			  if (message != null)
			  {
				  // load topic, it was not fully loaded.
				  Topic desttopic = message.getTopic();
				  Topic fulltopic = forumManager.getTopicById(message.getTopic().getId());
				  message.setTopic(fulltopic);
				  if (log.isDebugEnabled()) log.debug("message.getTopic() id " + message.getTopic().getId());
				  if (log.isDebugEnabled()) log.debug("message.getTopic() title" + message.getTopic().getTitle());

				  DiscussionMessageBean decoMsg = new DiscussionMessageBean(message,
						  messageManager);
				  // Set anonymous attributes on the bean (reduces queries later; improves performance)
				  decoMsg.setUseAnonymousId(useAnonymousId);
				  if (useAnonymousId)
				  {
				  	String userId = message.getAuthorId();
				  	decoMsg.setAnonId(userIdAnonIdMap.get(userId));
				  }
				  decoMsg.setMoved(true);
				  decoTopic.addMessage(decoMsg);
			  }
		  }
    	}

	  }
    return decoTopic;
  }

	public boolean isAnonymousEnabled()
	{
		return anonymousManager.isAnonymousEnabled();
	}

	public boolean isPostAnonymousRevisable()
	{
		return anonymousManager.isPostAnonymousRevisable();
	}

	public boolean isRevealIDsToRolesRevisable()
	{
		return anonymousManager.isRevealIDsToRolesRevisable();
	}

	/** Determines whether the postAnonymous checkbox should be enabled / disabled (visibility is not controlled here) */
	public boolean isNewTopicOrPostAnonymousRevisable()
	{
		return !isExistingTopic() || isPostAnonymousRevisable();
	}

	/** Determines whether the revealIDsToRoles checkbox should be enabled / disabled (visibility is not controlled here) */
	public boolean isNewTopicOrRevealIDsToRolesRevisable()
	{
		return !isExistingTopic() || isRevealIDsToRolesRevisable();
	}

	public boolean isExistingTopic()
	{
		// Topic exists if it has an ID
		return selectedTopic != null && selectedTopic.getTopic() != null && selectedTopic.getTopic().getId() != null;
	}

	public boolean isSiteHasAnonymousTopics()
	{
		return forumManager.isSiteHasAnonymousTopics(getSiteId());
	}

	/**
	 * Determines whether the current user should see anonymous IDs in the context of the specified topic.
	 * @param topic
	 * @param true if the topic is anonymous and revealIDsToRoles is disabled or
	 * revealIDsToRoles is enabled, but the user doesn't have the permission to identify users in this topic
	 */
	private boolean isUseAnonymousId(Topic topic)
	{
		if (topic == null)
		{
			throw new IllegalArgumentException("isUseAnonymousId invoked with null topic");
		}

		if (!isAnonymousEnabled())
		{
			return false;
		}

		// if topic.postAnonymous
		//   if topic.revealIDsToRoles
		//     return !uiPermissionsManager.isIdentifyAnonAuthors (anonymous only if they don't have permission to see identities)
		//   return true (anonymous for all)
		//  return false (not anonymous)

		// Condenses to
		return topic.getPostAnonymous() && (!topic.getRevealIDsToRoles() || !uiPermissionsManager.isIdentifyAnonAuthors((DiscussionTopic) topic));
	}

	/**
	 * Gets a userId -> anonymousID map containing entries for every author obtained from the 'messages' list.
	 * Does so in a single query (unless anonymous mappings are missing, in which case they are created)
	 * @param messages list of Messages
	 */
	private Map<String, String> getUserIdAnonIdMapForMessages(List<Message> messages)
	{
		if (messages == null)
		{
			throw new IllegalArgumentException("getUserIdAnonIdMapForMessages: null argument");
		}

		String siteId = toolManager.getCurrentPlacement().getContext();
		// Iterate over messages and construct a list of authors
		List<String> userIds = new ArrayList<>();
		for (Message message : messages)
		{
			userIds.add(message.getAuthorId());
		}

		return anonymousManager.getOrCreateUserIdAnonIdMap(siteId, userIds);
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
	          if (topic == null) {
	            // Topic was not found, likely due to an import/replace or deletion.
	            log.warn("Topic with id '{}' not found", topicId);
	            setErrorMessage(getResourceBundleString(TOPIC_WITH_ID) + topicId + getResourceBundleString(NOT_FOUND_WITH_QUOTE));
	            return false;
	          }
	        }
	        catch (NumberFormatException e)
	        {
	          log.error(e.getMessage(), e);
	          setErrorMessage(getResourceBundleString(UNABLE_RETRIEVE_TOPIC));
	          return false;
	        }

	        setSelectedForumForCurrentTopic(topic);
	        selectedTopic = getDecoratedTopic(topic);
	      }
	      else
	      {
	        log.error("Topic with id '" + externalTopicId + "' not found");
	        setErrorMessage(getResourceBundleString(TOPIC_WITH_ID) + externalTopicId + getResourceBundleString(NOT_FOUND_WITH_QUOTE));
	        return false;
	      }
	    }
	    catch (Exception e)
	    {
	      log.error(e.getMessage(), e);
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
    if (log.isDebugEnabled())
    {
      log.debug("processActionDisplayTopicById(String" + externalTopicId + ")");
    }
    topicClickCount++;
    if(resetTopicById(externalTopicId)){
        LRS_Statement statement = forumManager.getStatementForUserReadViewed(selectedTopic.getTopic().getTitle(), "topic").orElse(null);
        Event event = eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_TOPIC_READ, getEventReference(selectedTopic.getTopic()), null, true, NotificationService.NOTI_OPTIONAL, statement);
        eventTrackingService.post(event);

        return ALL_MESSAGES;
    } else {
        // Clear cached state so main view rebuilds after import/replace
        reset();
        return gotoMain();
    }
  }

  private void reset()
  {
    this.forums = null;
    this.selectedForum = null;
    this.selectedTopic = null;
    this.selectedMessage = null;
    this.selectedThreadHead = null;
    this.selectedThread = new ArrayList();
//    this.templateControlPermissions = null;
//    this.templateMessagePermissions = null;
    this.permissions=null;
    this.errorSynch = false;
    this.siteMembers=null;   
    attachments.clear();
    prepareRemoveAttach.clear();
    assignments.clear();
  }

  /**
   * @return newly created topic
   */
  private DiscussionTopicBean createTopic()
  {
    String forumId = getExternalParameterByKey(FORUM_ID);
    if (StringUtils.isBlank(forumId) || "null".equals(forumId))
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
    selectedForum = new DiscussionForumBean(forum, forumManager, userTimeService);
    loadForumDataInForumBean(forum, selectedForum);
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
    selectedTopic = new DiscussionTopicBean(topic, forum, forumManager, rubricsService, userTimeService);
    // TODO ERN loadTopicDataInTopicBean(topic, selectedTopic);
    if("true".equalsIgnoreCase(ServerConfigurationService.getString("mc.defaultLongDescription")))
    {
    	selectedTopic.setReadFullDesciption(true);
    }
    
    selectedTopic.setModerated(selectedForum.getModerated()); // default to parent forum's setting
    selectedTopic.setPostFirst(selectedForum.getPostFirst()); // default to parent forum's setting
    selectedTopic.setAutoMarkThreadsRead(selectedForum.getAutoMarkThreadsRead()); // default to parent forum's setting

    setNewTopicBeanAssign();
    
    DiscussionTopicBean thisDTB = new DiscussionTopicBean(topic, forum, forumManager, rubricsService, userTimeService);
    // TODO ERN loadTopicDataInTopicBean(topic, thisDTB);
    if("true".equalsIgnoreCase(ServerConfigurationService.getString("mc.defaultLongDescription")))
    {
    	thisDTB.setReadFullDesciption(true);
    }

    setNewTopicBeanAssign(selectedForum, thisDTB);
    return thisDTB;
  }

  // compose
  public String processAddMessage()
  {
    return MESSAGE_COMPOSE;
  }

  public String processAddAttachmentRedirect()
  {
    log.debug("processAddAttachmentRedirect()");
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

  public ArrayList getAttachments()
  {
    ToolSession session = sessionManager.getCurrentToolSession();
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

  public String processDeleteAttach()
  {
    log.debug("processDeleteAttach()");

    ExternalContext context = FacesContext.getCurrentInstance()
        .getExternalContext();
    String attachId = null;

    Map paramMap = context.getRequestParameterMap();
    Iterator<Entry<Object, String>> itr = paramMap.entrySet().iterator();
    while (itr.hasNext())
    {
      Entry<Object, String> entry = itr.next();
      Object key = entry.getKey();
      if (key instanceof String)
      {
        String name = (String) key;
        int pos = name.lastIndexOf("dfmsg_current_attach");

        if (pos >= 0 && name.length() == pos + "dfmsg_current_attach".length())
        {
          attachId = entry.getValue();
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
	log.debug("processDfMsgPost()");
    if(!checkPermissionsForUser("processDfReplyTopicSaveDraft", false, true, false, false)){
    	return gotoMain();
    }
    
    Message newMessage = constructMessage();
    try{
    	LRS_Statement statement = forumManager.getStatementForUserPosted(newMessage.getTitle(), SAKAI_VERB.responded).orElse(null);
    	ForumsMessageEventParams params = new ForumsMessageEventParams(ForumsMessageEventParams.MessageEvent.ADD, statement);
    	Message dMsg = forumManager.saveMessage(newMessage, params);

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
    	messageManager.markMessageNotReadForUser(selectedTopic.getTopic().getId(), dMsg.getId(), false);

    	this.composeBody = null;
    	this.composeLabel = null;
    	this.composeTitle = null;

    	this.attachments.clear();
    	this.selectedThread.clear();

    	// refresh page with unread status     
    	selectedTopic = getDecoratedTopic(selectedTopic.getTopic());
    	sendEmailNotification(dMsg,new DiscussionMessageBean(dMsg, messageManager), dMsg.getTopic().getModerated());
    }catch(Exception e){
    	log.error("DiscussionForumTool: processDfMsgPost", e);
    	setErrorMessage(getResourceBundleString(ERROR_POSTING_THREAD));
    	return gotoMain();
    }

    return ALL_MESSAGES;
  }
  
  private void updateThreadLastUpdatedValue(Message message, int numOfAttempts) throws Exception{
	  try{
		  message.setDateThreadlastUpdated(new Date());
		  if (message.getInReplyTo() != null) {
			  //only top level child messages have thread ids
			  if (message.getInReplyTo().getThreadId() != null) {
				  message.setThreadId(message.getInReplyTo().getThreadId());
			  } else {
				  message.setThreadId(message.getInReplyTo().getId());
			  }
		  }
  		if (message.getInReplyTo() != null) {
  			Message m = null;
  			if (message.getThreadId() != null)
  				m = forumManager.getMessageById(message.getThreadId());
  			else 
  				m = message.getInReplyTo();
  			//otherwise we get an NPE when we try to save this message
  			Topic topic = message.getTopic();
  			BaseForum bf  = topic.getBaseForum();
  			m.setTopic(topic);
  			m.getTopic().setBaseForum(bf);
  			m.setThreadLastPost(message.getId());
  			m.setDateThreadlastUpdated(new Date());
			LRS_Statement statement = forumManager.getStatementForUserPosted(m.getTitle(), SAKAI_VERB.responded).orElse(null);
			Message persistedMessage = forumManager.saveMessage(m, new ForumsMessageEventParams(ForumsMessageEventParams.MessageEvent.RESPONSE, statement));
  		}
  	}catch (HibernateOptimisticLockingFailureException holfe) {
  	// failed, so wait and try again
		try {
			Thread.sleep(SynopticMsgcntrManager.OPT_LOCK_WAIT);
			//grab the message again fresh from the DB:
			if (message.getInReplyTo() != null) {
				message.setInReplyTo(forumManager.getMessageById(message.getInReplyTo().getId()));
			}
			
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}

		numOfAttempts--;

		if (numOfAttempts <= 0) {
			log.error("DiscussionForumTool: updateThreadLastUpdatedValue: HibernateOptimisticLockingFailureException no more retries left", holfe);
			throw new Exception(holfe);
		} else {
			log.info("DiscussionForumTool: updateThreadLastUpdatedValue: HibernateOptimisticLockingFailureException: attempts left: "
							+ numOfAttempts);
			updateThreadLastUpdatedValue(message, numOfAttempts);
		}
  	}

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
	  //make sure current user isn't in the list if they shouldn't be updated
	  if(!updateCurrentUser){
		  recipients.remove(currentUser);
	  }
	  incrementForumSynopticToolInfo(new ArrayList<String>(recipients), siteId, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
  }
  
  public void incrementForumSynopticToolInfo(List<String> userIds, String siteId, int numOfAttempts){
	  try {
		  getSynopticMsgcntrManager().incrementForumSynopticToolInfo(userIds, siteId);
		} catch (HibernateOptimisticLockingFailureException holfe) {

			// failed, so wait and try again
			try {
				Thread.sleep(SynopticMsgcntrManager.OPT_LOCK_WAIT);
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}

			numOfAttempts--;

			if (numOfAttempts <= 0) {
				log.info("DiscussionForumTool: incrementForumSynopticToolInfo: HibernateOptimisticLockingFailureException no more retries left");
				log.error(holfe.getMessage(), holfe);
			} else {
				log.info("DiscussionForumTool: incrementForumSynopticToolInfo: HibernateOptimisticLockingFailureException: attempts left: "
								+ numOfAttempts);
				incrementForumSynopticToolInfo(userIds, siteId, numOfAttempts);
			}
		}
  }
  
  public Set<String> getRecipients(Message newMessage){
	    Set<String> usersAbleToReadMessage;
		
		// if this message has not been approved, we may only let moderators view it
		if (newMessage.getApproved() == null || !newMessage.getApproved()) {
			usersAbleToReadMessage = forumManager.getUsersAllowedForTopic(newMessage.getTopic().getId(), true, true);	
		} else {
			usersAbleToReadMessage = forumManager.getUsersAllowedForTopic(newMessage.getTopic().getId(), true, false);	
		}
		
		return usersAbleToReadMessage;
  }

  public Message constructMessage()
  {
    	log.debug("....in constructMessage()");
    Message aMsg;

    aMsg = messageManager.createDiscussionMessage();
    
    if (aMsg != null)
    {
      aMsg.setTitle(getComposeTitle());
      aMsg.setBody(formattedText.processFormattedText(getComposeBody(), null, null));
      
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
    	log.debug("selectedTopic is null in constructMessage()");
    	return null;
    }
	  if (!selectedTopic.getTopicModerated() || selectedTopic.getIsModeratedAndHasPerm())
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
    	log.debug("selectedTopic is null in isDisplayTopicDeleteOption()");
    	return false;
    }
	  DiscussionTopic topic = selectedTopic.getTopic();
	  if (topic == null || topic.getId() == null)
		  return false;
	  
	  Topic topicInDb = forumManager.getTopicById(topic.getId());
	  
	  return topicInDb != null && selectedForum != null && uiPermissionsManager != null && uiPermissionsManager.isChangeSettings(topic,selectedForum.getForum());
  }
  
  private void setupForum() {
	  if(selectedForum == null)  {
		  DiscussionForum forum = forumManager.createForum();
	      forum.setModerated(areaManager.getDiscusionArea().getModerated()); // default to template setting
	      forum.setPostFirst(areaManager.getDiscusionArea().getPostFirst()); // default to template setting
	      selectedForum = new DiscussionForumBean(forum, forumManager, userTimeService);
	      loadForumDataInForumBean(forum, selectedForum);
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
    	log.debug("redirectTo or selectedTopic is null in isDisplayForumDeleteOption");
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
    	userId = sessionManager.getCurrentSessionUserId();
	  
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
	    messageManager.markMessageNotReadForUser(Long.valueOf(topicId),
	        Long.valueOf(messageId), false);
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
  public String processDfMsgMarkMsgAsNotReadFromThread()
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
	    messageManager.markMessageNotReadForUser(Long.valueOf(topicId),
	        Long.valueOf(messageId), false);
	    if (message == null)
	    {
	      setErrorMessage(getResourceBundleString(MESSAGE_WITH_ID) + messageId + getResourceBundleString(NOT_FOUND_WITH_QUOTE));
	      return gotoMain();
	    }
	    return processActionGetDisplayThread(); // reconstruct thread again;
  }
  
  public String processDfMsgReplyMsgFromEntire()
  {
	  	String messageIdStr = getExternalParameterByKey(MESSAGE_ID);
	    String topicIdStr = getExternalParameterByKey(TOPIC_ID);
	    if (messageIdStr == null || "".equals(messageIdStr))
	    {
	      setErrorMessage(getResourceBundleString(MESSAGE_REFERENCE_NOT_FOUND));
	      return gotoMain();
	    }
	    if (topicIdStr == null || "".equals(topicIdStr))
	    {
	      setErrorMessage(getResourceBundleString(TOPC_REFERENCE_NOT_FOUND));
	      return gotoMain();
	    }
	    long messageId, topicId;
	    try{
	    	messageId = Long.valueOf(messageIdStr);
	    }catch (NumberFormatException e) {
	    	log.error(e.getMessage());
	    	setErrorMessage(getResourceBundleString(MESSAGE_REFERENCE_NOT_FOUND));
	    	return gotoMain();
		}
	    try{
	    	topicId = Long.valueOf(topicIdStr);
	    }catch (NumberFormatException e) {
	    	log.error(e.getMessage());
	    	setErrorMessage(getResourceBundleString(TOPC_REFERENCE_NOT_FOUND));
	    	return gotoMain();
		}
	    
	    // Message message=forumManager.getMessageById(Long.valueOf(messageId));
	    messageManager.markMessageNotReadForUser(topicId, messageId, false);
	    Message message = messageManager.getMessageByIdWithAttachments(messageId);
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
    selectedMessageCount = 0;

    boolean isFaqForum = Boolean.TRUE.equals(selectedTopic.getTopic().getFaqTopic());

    String replyPrefix = getResourceBundleString(MSG_REPLY_PREFIX);
    String answerPrefix = getResourceBundleString(MSG_PVT_ANSWER_PREFIX);
    String questionPrefix = getResourceBundleString(MSG_PVT_QUESTION_PREFIX);
    String title = StringUtils.trim(selectedMessage.getMessage().getTitle());

    if (StringUtils.startsWith(title, replyPrefix)) {
        // Re: title --> Re: title
        this.composeTitle = title;
    } else if (isFaqForum) {
        if (StringUtils.startsWith(title, questionPrefix)) {
            // Q: title -> A: title
            this.composeTitle = StringUtils.replace(title, questionPrefix, answerPrefix);
        } else {
            // title --> Re: title
            // A: title --> Re: A: title
            this.composeTitle = replyPrefix + " " + title;
        }
    } else {
        // title --> Re: title
        // A: title --> Re: A: title
        // Q: title --> Re: Q: title
        this.composeTitle = replyPrefix + " " + title;
    }

    return "dfMessageReply";
  }

  public String processDfMsgReplyThread()
  {
	  selectedMessageCount  = 0;
  	if(selectedTopic == null)
  	{
  		log.debug("selectedTopic is null in processDfMsgReplyThread");
  		return gotoMain();
  	}
	  // we have to get the first message that is not a response
	  selectedMessage = getThreadHeadForMessage(selectedMessage.getMessage());
	  
	  List tempMsgs = selectedTopic.getMessages();
	    if(tempMsgs != null)
	    {
	    	for(int i=0; i<tempMsgs.size(); i++)
	    	{
	    		DiscussionMessageBean thisDmb = (DiscussionMessageBean)tempMsgs.get(i);
	    		if(((DiscussionMessageBean)tempMsgs.get(i)).getMessage().getId().compareTo(selectedMessage.getMessage().getId()) == 0)
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
	    String forumId = getExternalParameterByKey(FORUM_ID);
	    String userId = getExternalParameterByKey(USER_ID);
	    if (topicId == null)
	    {
	      setErrorMessage(getResourceBundleString(TOPC_REFERENCE_NOT_FOUND));
	      return gotoMain();
	    }
	    if((messageId == null || "".equals(messageId)) && (userId == null || "".equals(userId))){
	    	setErrorMessage(getResourceBundleString(TOPC_REFERENCE_NOT_FOUND));
		      return gotoMain();
	    }
	    return processDfMsgGrdFromThread(messageId, topicId, forumId, userId);
  }
  
  public String processDfMsgGrdFromThread(String messageId, String topicId, String forumId, String userId){
	  
	  selectedGradedUserId = userId;
  
	  // Message message=forumManager.getMessageById(Long.valueOf(messageId));
	  if(messageId != null && !"".equals(messageId)){
		  Message message = messageManager.getMessageByIdWithAttachments(Long.valueOf(
				  messageId));
		  if (message == null)
		  {
			  setErrorMessage(getResourceBundleString(MESSAGE_WITH_ID) + messageId + getResourceBundleString(NOT_FOUND_WITH_QUOTE));
			  return gotoMain();
		  }

		  selectedMessage = new DiscussionMessageBean(message, messageManager);

	  }else{
		  selectedMessage = null;
	  }

	  if(selectedForum == null || (forumId != null && !selectedForum.getForum().getId().toString().equals(forumId))){
		  DiscussionForum forum = forumManager.getForumById(Long.parseLong(forumId));
		  selectedForum = new DiscussionForumBean(forum, forumManager, userTimeService);
		  loadForumDataInForumBean(forum, selectedForum);
	  }

	  if(topicId == null || "".equals(topicId)){
		  selectedTopic = null;
	  }else if(selectedTopic == null || !selectedTopic.getTopic().getId().toString().equals(topicId)){
		  DiscussionTopic topic = forumManager.getTopicById(Long.parseLong(topicId));
		  selectedTopic = getDecoratedTopic(topic);
	  }    
	    
	  if(selectedMessage != null){
		  return processDfMsgGrd();
	  }else{
		  return processDfMsgGrdHelper(userId, null);
	  }
  }
  
  public String processDfMsgGrd()
  {
	  try
	  {
		  String createdById = userDirectoryService.getUser(selectedMessage.getMessage().getCreatedBy()).getId();
		  String msgAssignmentName = selectedMessage.getMessage().getGradeAssignmentName();

		  String returnStr = processDfMsgGrdHelper(createdById, msgAssignmentName);

		  // mark this message as read
		  messageManager.markMessageNotReadForUser(selectedTopic.getTopic().getId(), selectedMessage.getMessage().getId(), false);
		  
		  return returnStr;
	  }
	  catch(Exception e) 
	  { 
		  log.error("processDfMsgGrd in DiscussionFOrumTool - " + e); 
		  return null; 
	  } 
  }
  
  private String processDfMsgGrdHelper(String userId, String msgAssignmentName) {

    selectedMessageCount = 0;
	  
    grade_too_large_make_sure = false;
  	
    selectedAssign = DEFAULT_GB_ITEM;
    resetGradeInfo();

    String gradebookUid = toolManager.getCurrentPlacement().getContext();

    String topicDefaultAssignment = null;
    if (selectedTopic != null) {
      topicDefaultAssignment = selectedTopic.getTopic().getDefaultAssignName();
    }
    String forumDefaultAssignment = selectedForum.getForum().getDefaultAssignName();

    String selAssignmentName = null;
    if (msgAssignmentName !=null && msgAssignmentName.trim().length()>0) {
      selAssignmentName = msgAssignmentName;
    } else if (topicDefaultAssignment != null && topicDefaultAssignment.trim().length() > 0) {
      selAssignmentName = topicDefaultAssignment;
      if (selectedTopic != null) {
        selectedTopic.setGradeAssign(topicDefaultAssignment);
      }
    } else if (forumDefaultAssignment != null && forumDefaultAssignment.trim().length() > 0) {
      selAssignmentName = forumDefaultAssignment;
      selectedForum.setGradeAssign(forumDefaultAssignment);
    }



    try {
    	if (selAssignmentName != null) {
    		setUpGradeInformation(gradebookUid, toolManager.getCurrentPlacement().getContext(), selAssignmentName, userId);
    	} else {
    		// this is the "Select a gradebook item" selection
    		allowedToGradeItem = false;
    		selGBItemRestricted = true;
    	}
    	}catch (AssessmentNotFoundException e) {
    		if (msgAssignmentName !=null && msgAssignmentName.trim().length()>0) {
    			Message msg = selectedMessage.getMessage();
    			msg.setGradeAssignmentName(null);
    			msg = forumManager.saveMessage(msg);
    			selectedMessage.setMessage(msg);
    		} else if (topicDefaultAssignment != null && topicDefaultAssignment.trim().length() > 0) {
    			DiscussionTopic dt = selectedTopic.getTopic();
    			dt.setDefaultAssignName(null);
    			dt = forumManager.saveTopic(dt);
    			selectedTopic.setTopic(dt);
    		} else if (forumDefaultAssignment != null && forumDefaultAssignment.trim().length() > 0) {
    			DiscussionForum df = selectedForum.getForum();
    			df.setDefaultAssignName(null);
    			df = forumManager.saveForum(df);
    			selectedForum.setForum(df);
    		}
    		allowedToGradeItem = false;
    		selGBItemRestricted = true;
    	}

    return GRADE_MESSAGE;
  }

  private void setUpGradeInformation(String gradebookUid, String siteId, String selAssignmentName, String studentId) {
	  GradingService gradingService = getGradingService();
	  if (gradingService == null) return;
	  
	  Assignment assignment = gradingService.getAssignmentByNameOrId(gradebookUid, siteId, selAssignmentName);
	  
	  // first, check to see if user is authorized to view or grade this item in the gradebook
	  String function = gradingService.getGradeViewFunctionForUserForStudentForItem(gradebookUid, siteId, assignment.getId(), studentId);
	  if (function == null) {
		  allowedToGradeItem = false;
		  selGBItemRestricted = true;
	  } else if (function.equalsIgnoreCase(GradingConstants.gradePermission)) {
		  allowedToGradeItem = true;
		  selGBItemRestricted = false;
	  } else {
		  allowedToGradeItem = false;
		  selGBItemRestricted = false;
	  }
	  
		// get the grade entry type for the gradebook
		GradeType gradeEntryType = gradingService.getGradeEntryType(gradebookUid);
		switch (gradeEntryType) {
			case LETTER:
				gradeByLetter = true;
				gradeByPoints = false;
				gradeByPercent = false;
				break;
			case PERCENTAGE:
				gradeByPercent = true;
				gradeByPoints = false;
				gradeByLetter = false;
				break;
			default:
				gradeByPoints = true;
				gradeByLetter = false;
				gradeByPercent = false;
		}

	  NumberFormat numberFormat = DecimalFormat.getInstance(new ResourceLoader().getLocale());
	  if (!selGBItemRestricted) {
		  if (assignment != null && assignment.getPoints() != null) {
			  gbItemPointsPossible = ((DecimalFormat) numberFormat).format(assignment.getPoints());
		  }
		  
	  	  GradeDefinition gradeDef = gradingService.getGradeDefinitionForStudentForItem(gradebookUid, siteId, assignment.getId(), studentId);

		  if (gradeDef.getGrade() != null) {
		      String decSeparator = formattedText.getDecimalSeparator();
		      gbItemScore = StringUtils.replace(gradeDef.getGrade(), (",".equals(decSeparator)?".":","), decSeparator);
		  }

		  if (gradeDef.getGradeComment() != null) {
		      gbItemComment = gradeDef.getGradeComment();
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

    return null;
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

	  //checks whether the user can post to the forum & topic based on the passed in message id
	  if(!checkPermissionsForUser("processDfReplyMsgPost", true, true, false, false)){
		  return gotoMain();
	  }
  	try{
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

  		Message newMessage = constructMessage();

  		newMessage.setInReplyTo(selectedMessage.getMessage());
		LRS_Statement statement = forumManager.getStatementForUserPosted(newMessage.getTitle(), SAKAI_VERB.responded).orElse(null);
		Message dMsg = forumManager.saveMessage(newMessage, new ForumsMessageEventParams(ForumsMessageEventParams.MessageEvent.ADD, statement));

  		//update synoptic tool info
  		incrementSynopticToolInfo(dMsg, true);

  		setSelectedForumForCurrentTopic(topicWithMsgs);
  		selectedTopic.setTopic(topicWithMsgs);
  		selectedTopic.getTopic().setBaseForum(selectedForum.getForum());
  		//selectedTopic.addMessage(new DiscussionMessageBean(dMsg, messageManager));
  		selectedTopic.insertMessage(new DiscussionMessageBean(dMsg, messageManager));
  		selectedTopic.getTopic().addMessage(dMsg);
  		messageManager.markMessageNotReadForUser(selectedTopic.getTopic().getId(), dMsg.getId(), false);

  		this.composeBody = null;
  		this.composeLabel = null;
  		this.composeTitle = null;

  		this.attachments.clear();

  		//return ALL_MESSAGES;
  		//check selectedThreadHead exists
  		if(selectedThreadHead == null){
  			selectedThreadHead = getThreadHeadForMessage(selectedMessage.getMessage());
  		}
  		//since replyTo has been set to the selected message, the selected message was update
  		//we need to grab the newest one from the db
  		if(selectedMessage != null && selectedMessage.getMessage() != null){
  			selectedMessage = new DiscussionMessageBean(messageManager.getMessageByIdWithAttachments(selectedMessage.getMessage().getId()), messageManager);
  		}

  		sendEmailNotification(dMsg,selectedThreadHead,dMsg.getTopic().getModerated());
  		
  		//now update the parent thread:
    	updateThreadLastUpdatedValue(dMsg, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
  	}catch(Exception e){
  		log.error("DiscussionForumTool: processDfReplyMsgPost", e);
  		setErrorMessage(getResourceBundleString(ERROR_POSTING_THREAD));
  		return gotoMain();
  	}
    return processActionGetDisplayThread();
  }

  public String processDeleteAttachRevise()
  {
    ExternalContext context = FacesContext.getCurrentInstance()
        .getExternalContext();
    String attachId = null;

    Map paramMap = context.getRequestParameterMap();
    Iterator<Entry<Object, String>> itr = paramMap.entrySet().iterator();
    while (itr.hasNext())
    {
      Entry<Object, String> entry = itr.next();
      Object key = entry.getKey();
      if (key instanceof String)
      {
        String name = (String) key;
        int pos = name.lastIndexOf("dfmsg_current_attach");

        if (pos >= 0 && name.length() == pos + "dfmsg_current_attach".length())
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
	  getThreadFromMessage();
	  
	  this.composeBody = null;
	  this.composeLabel = null;
	  this.composeTitle = null;

	  this.attachments.clear();
	  
	  return MESSAGE_VIEW;
  }
  
  
  public String processDfMsgRevisedPost()
  {
	//checks whether the user can post to the forum & topic based on the passed in message id
	if(!checkPermissionsForUser("processDfMsgRevisedPost", false, false, true, false)){
		return gotoMain();
	}
	try{
		DiscussionTopic dfTopic = selectedTopic.getTopic();
		DiscussionForum dfForum = selectedForum.getForum();

		Message dMsg = selectedMessage.getMessage();
	

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

		// optionally include the revision history. by default, revision history is included
		boolean showRevisionHistory = ServerConfigurationService.getBoolean("msgcntr.forums.showRevisionHistory",true);
		if (showRevisionHistory) {
			String revisedInfo = "<p class=\"lastRevise textPanelFooter\">";
			revisedInfo += createLastReviseByString(dfTopic);
			SimpleDateFormat formatter = new SimpleDateFormat(getResourceBundleString("date_format"), getUserLocale());
			formatter.setTimeZone(getUserTimeZone());
			Date now = new Date();
			revisedInfo += formatter.format(now) + " </p> ";
			currentBody = revisedInfo.concat(currentBody);
		} 

		dMsg.setTitle(getComposeTitle());
		dMsg.setBody(formattedText.processFormattedText(currentBody, null, null));
		dMsg.setDraft(Boolean.FALSE);
		dMsg.setModified(new Date());

		dMsg.setModifiedBy(getUserNameOrEid());
		if (!selectedTopic.getTopicModerated() || selectedTopic.getIsModeratedAndHasPerm())
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
		
		if (dMsg.getInReplyTo() != null) {
			//grab a fresh copy of the message incase it has changes (staleobjectexception)
			dMsg.setInReplyTo(forumManager.getMessageById(dMsg.getInReplyTo().getId()));
		}

		LRS_Statement statement = forumManager.getStatementForUserPosted(dMsg.getTitle(), SAKAI_VERB.responded).orElse(null);
		Message persistedMessage = forumManager.saveMessage(dMsg, new ForumsMessageEventParams(ForumsMessageEventParams.MessageEvent.REVISE, statement));

		messageManager.markMessageNotReadForUser(dfTopic.getId(), persistedMessage.getId(), false);

		List messageList = selectedTopic.getMessages();
		for (int i = 0; i < messageList.size(); i++)
		{
			DiscussionMessageBean dmb = (DiscussionMessageBean) messageList.get(i);
			if (dmb.getMessage().getId().equals(persistedMessage.getId()))
			{
				selectedTopic.getMessages().set(i,
						new DiscussionMessageBean(persistedMessage, messageManager));
			}
		}

		try
		{
			DiscussionTopic topic = null;
			try
			{
				topic = forumManager.getTopicById(selectedTopic.getTopic().getId());
				setSelectedForumForCurrentTopic(topic);
				selectedTopic = getDecoratedTopic(topic);
			}
			catch (NumberFormatException e)
			{
				log.error(e.getMessage(), e);
			}

		}
		catch (Exception e)
		{
			log.error(e.getMessage(), e);
			setErrorMessage(e.getMessage());
			return null;
		}

		prepareRemoveAttach.clear();
		composeBody = null;
		composeLabel = null;
		composeTitle = null;
		attachments.clear();

		selectedTopic = getSelectedTopic();
		getThreadFromMessage();
	} catch(Exception e) {
      log.error("Error while editing a message", e);
      if (e instanceof OptimisticLockException) {
        // javax.persistence.OptimisticLockException: Row was updated or deleted by another transaction (or unsaved-value mapping was incorrect)
        setErrorMessage(getResourceBundleString(ERROR_POSTING_THREAD_STALE));
      } else {
        setErrorMessage(getResourceBundleString(ERROR_POSTING_THREAD));
      }
      return gotoMain();
    }
    return MESSAGE_VIEW;
  }

	/**
	 * For revised posts, creates the 'last revised (by ...) on ' part
	 */
	private String createLastReviseByString(Topic t)
	{
		if (t.getPostAnonymous())
		{
			return getResourceBundleString(LAST_REVISE_ON_ANON) + " ";
		}
		return getResourceBundleString(LAST_REVISE_BY) + getUserNameOrEid() + " " + getResourceBundleString(LAST_REVISE_ON);
	}

  public String processDfReplyMsgCancel()
  {
	  selectedMessageCount = 0;
	  
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
	  this.errorSynch = false;
	    this.composeBody = null;
	    this.composeLabel = null;
	    this.composeTitle = null;

	    this.attachments.clear();

	    return processActionGetDisplayThread();
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
  private String getEventReference(Object obj) 
  {
	  String eventMessagePrefix = "";
	  final String toolId = toolManager.getCurrentTool().getId();
  	
	  if (toolId.equals(DiscussionForumService.MESSAGE_CENTER_ID))
		  eventMessagePrefix = "/messagesAndForums";
  	  else if (toolId.equals(DiscussionForumService.MESSAGES_TOOL_ID))
  		  eventMessagePrefix = "/messages";
  	  else
  		  eventMessagePrefix = "/forums";
  	
	  return eventMessagePrefix + getContextSiteId() + "/" + obj.toString() + "/" + sessionManager.getCurrentSessionUserId();
  }
  
  /**
   * Deletes the message by setting boolean deleted switch to TRUE.
   */
  public String processDfMsgDeleteConfirmYes()
  {
	  //checks whether the user can post to the forum & topic based on the passed in message id
	  if(!checkPermissionsForUser("processDfReplyTopicSaveDraft", false, false, false, true)){
		  return gotoMain();
	  }
	
  	if(selectedTopic == null)
  	{ 
  		log.debug("selectedTopic is null in processDfMsgDeleteConfirmYes");
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
	  
	  Message message = messageManager.getMessageById(selectedMessage.getMessage().getId());

	  // 'delete' this message
	  message.setDeleted(Boolean.TRUE);

	  // reload topic for this message so we can save it
	  message.setTopic((DiscussionTopic) forumManager
			  .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));

	  // does the actual save to 'delete' this message
	  Message persistedMessage = forumManager.saveMessage(message);

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
	  eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_REMOVE, getEventReference(persistedMessage), true));
	  log.info("Forum message {} has been deleted by {}", persistedMessage.getId(), getUserId());

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
  public boolean isDisplayPendingMsgQueue() {
    if (displayPendingMsgQueue == null) {
      List<String> membershipList = new ArrayList<>();
      List<Topic> moderatedTopics = forumManager.getModeratedTopicsInSite();

      // Avoid the expensive queries below if there are no moderated topics
      if (moderatedTopics != null && !moderatedTopics.isEmpty()) {
        membershipList = uiPermissionsManager.getCurrentUserMemberships();
        int numModTopicWithPerm = forumManager.getNumModTopicsWithModPermissionByPermissionLevel(membershipList, moderatedTopics);

        if (numModTopicWithPerm < 1) {
          numModTopicWithPerm = forumManager.getNumModTopicsWithModPermissionByPermissionLevelName(membershipList, moderatedTopics);
        }

        displayPendingMsgQueue = numModTopicWithPerm > 0;
      }
      else {
        displayPendingMsgQueue = false;
      }

      if (displayPendingMsgQueue) {
        refreshPendingMessages(membershipList, moderatedTopics);
      }
    }

    return displayPendingMsgQueue;
  }
  
  /**
   * Retrieve pending msgs from db and make DiscussionMessageBeans
   *
   * @param membershipList
   * @param moderatedTopics
   */
  private void refreshPendingMessages(List<String> membershipList, List<Topic> moderatedTopics)
  {
	  pendingMsgs = new ArrayList();
	  numPendingMessages = 0;
	  List<Message> messages = forumManager.getPendingMsgsInSiteByMembership(membershipList, moderatedTopics);
	  
	  if (messages != null && !messages.isEmpty())
	  {
		  messages = messageManager.sortMessageByDate(messages, true);

		  // For anonymous performance
		  //Maps topics to a boolean representing whether anonymousIDs should be displayed within the topic's context
		  Map<Topic, Boolean> topicUseAnonIdMap = new HashMap<>();
		  // Maps userIds to all the messages that are within an anonymous context
		  Map<String, List<DiscussionMessageBean>> userIdAnonMessagesMap = new HashMap<>();
	  
		  for (Message msg : messages)
		  {
			  // Determine if we should display anonIds in the context of this message's topic, keep track of this in a map to minimize redundant permission checks, etc.
			  Topic topic = msg.getTopic();
			  Boolean useAnonId = topicUseAnonIdMap.get(topic);
			  if (useAnonId == null)
			  {
			  	useAnonId = isUseAnonymousId(topic);
			  	topicUseAnonIdMap.put(topic, useAnonId);
			  }

			  DiscussionMessageBean decoMsg = new DiscussionMessageBean(msg, messageManager);

			  decoMsg.setUseAnonymousId(useAnonId);
			  // If the message's context is anonymous, map the author to this message bean. We'll use this map to set the anonIDs momentarily
			  if (useAnonId)
			  {
			  	String userId = msg.getAuthorId();
			  	List<DiscussionMessageBean> userAnonymousMessages = userIdAnonMessagesMap.get(userId);
			  	if (userAnonymousMessages == null)
			  	{
			  		userAnonymousMessages = new ArrayList<>();
			  		userIdAnonMessagesMap.put(userId, userAnonymousMessages);
			  	}
			  	userAnonymousMessages.add(decoMsg);
			  }

			  pendingMsgs.add(decoMsg);
			  numPendingMessages++;
		  }

		  // For all message beans in anonymous contexts, populate their anonymousIDs now to reduce queries and improve performance
		  // Get the anonId map for all releveant users.
		  String siteId = toolManager.getCurrentPlacement().getContext();
		  // AnonymousManager requires lists (because it needs to sublist into groups of 1000 for Oracle)
		  // Convert to userIdAnonMessagesMap's keySet into a list
		  List<String> userIdList = new ArrayList<>();
		  userIdList.addAll(userIdAnonMessagesMap.keySet());
		  Map<String, String> userIdAnonIdMap = anonymousManager.getOrCreateUserIdAnonIdMap(siteId, userIdList);
		  for (String userId : userIdList)
		  {
		  	// Set the user's anonId on all of their decoMsgs
		  	String anonId = userIdAnonIdMap.get(userId);
		  	List<DiscussionMessageBean> userMessages = userIdAnonMessagesMap.get(userId);
		  	for (DiscussionMessageBean decoMsg : userMessages)
		  	{
		  		decoMsg.setAnonId(anonId);
		  	}
		  }
	  }
  }
  
  /**
   * returns all messages in the site that are pending and curr user has
   * moderate perm to view
   * @return
   */
  public List getPendingMessages()
  {
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
		  return null;
	  
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
		  return null;
	  
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
			  
			  messageManager.markMessageNotReadForUser(msg.getTopic().getId(), msg.getId(), false);
			  numSelected++;
			  numPendingMessages--;

			  if(beforeChangeHM != null)
	        		updateSynopticMessagesForForumComparingOldMessagesCount(getSiteId(), msg.getTopic().getBaseForum().getId(), msg.getTopic().getId(), beforeChangeHM, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);

			  if (approved) {
                  // send out email notification to the watchers
                  Message msgWithAttach = messageManager.getMessageByIdWithAttachments(msg.getId());
                  msgWithAttach.setTopic(msg.getTopic());
                  sendEmailNotification(msgWithAttach, getThreadHeadForMessage(msgWithAttach), false);
              }
		  }
	  }
	  
	  if (numSelected < 1)
		  setErrorMessage(getResourceBundleString(NO_MSG_SEL_FOR_APPROVAL));
	  else
	  {
		  refreshPendingMessages(uiPermissionsManager.getCurrentUserMemberships() , forumManager.getModeratedTopicsInSite());
		  if (approved)
			  setSuccessMessage(getResourceBundleString(MSGS_APPROVED));
		  else
			  setSuccessMessage(getResourceBundleString(MSGS_DENIED));
	  }
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
	  
	  refreshPendingMessages(uiPermissionsManager.getCurrentUserMemberships() , forumManager.getModeratedTopicsInSite());
	  
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
	  
	  refreshPendingMessages(uiPermissionsManager.getCurrentUserMemberships() , forumManager.getModeratedTopicsInSite());
	  
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
		  
          // send out email notifications now that the message is visible
		  if (selectedTopic != null && selectedForum != null) {
		      Message msgWithAttach = messageManager.getMessageByIdWithAttachments(msgId);
		      Topic topic = forumManager.getTopicByIdWithMessages(selectedTopic.getTopic().getId());
		      topic.setBaseForum(selectedForum.getForum());
		      msgWithAttach.setTopic(topic);
		      sendEmailNotification(msgWithAttach, getThreadHeadForMessage(msgWithAttach), false);
		  }
		  
		  if(beforeChangeHM != null)
	      		updateSynopticMessagesForForumComparingOldMessagesCount(getSiteId(), msg.getTopic().getBaseForum().getId(), msg.getTopic().getId(), beforeChangeHM, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
	  }
	  
	  refreshPendingMessages(uiPermissionsManager.getCurrentUserMemberships() , forumManager.getModeratedTopicsInSite());
	  
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
  		log.debug("selectedTopic is null in processAddCommentToDeniedMsg");
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
	  if (selectedTopic.getTopic().getPostAnonymous())
	  {
	  	sb.append(getResourceBundleString(MOD_COMMENT_TEXT_ANON));
	  }
	  else
	  {
	  	sb.append(getResourceBundleString(MOD_COMMENT_TEXT)).append(" ");
	  	sb.append(userDirectoryService.getCurrentUser().getDisplayName());
	  }
	  sb.append("</div>");
	  sb.append("<div class=\"messageCommentBody\">");
	  sb.append(moderatorComments);
	  sb.append("</div>");
	  sb.append("</div>");
	  
	  
	  String originalText = currMessage.getBody();
	  currMessage.setBody(sb.toString() + originalText);
	  
	  currMessage.setTopic((DiscussionTopic) forumManager
              .getTopicByIdWithMessages(selectedTopic.getTopic().getId()));
	  LRS_Statement statement = forumManager.getStatementForUserPosted(currMessage.getTitle(), SAKAI_VERB.responded).orElse(null);
	  Message persistedMessage = forumManager.saveMessage(currMessage, new ForumsMessageEventParams(ForumsMessageEventParams.MessageEvent.REVISE, statement), true);

	  if (displayDeniedMsg) // only displayed if from Deny & Comment path
	  {
		  setSuccessMessage(getResourceBundleString("cdfm_denied_alert"));
		  displayDeniedMsg = false;
	  }
	  
	  // we also must mark this message as unread for the author to let them
	  // know there is a comment
	  forumManager.markMessageNotReadStatusForUser(persistedMessage, true, persistedMessage.getCreatedBy());
	  
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
  		log.debug("selectedTopic is null in isAllowedToApproveMsg");
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
  		log.debug("selectedTopic is null in isAllowedToDenyMsg");
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
  		log.debug("selectedTopic is null in setNewTopicBeanAssign");
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
        if(((SelectItem)assignments.get(i)).getLabel().equals(selectedForum.getForum().getDefaultAssignName()) ||
        	((SelectItem)assignments.get(i)).getValue().equals(selectedForum.getForum().getDefaultAssignName()))
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
  		log.debug("selectedTopic is null in setTopicBeanAssign");
  		return;
  	}
  	
  	if(assignments != null)
  	{
  		for(int i=0; i<assignments.size(); i++)
  		{
  			if(((SelectItem)assignments.get(i)).getLabel().equals(selectedTopic.getTopic().getDefaultAssignName()) ||
  					((SelectItem)assignments.get(i)).getValue().equals(selectedTopic.getTopic().getDefaultAssignName()))
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
        if(((SelectItem)assignments.get(i)).getLabel().equals(assignName) ||
        		((SelectItem)assignments.get(i)).getValue().equals(assignName))
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
      forum.setDefaultAssignName( selectedForum.getGradeAssign() );
    }
    else if(selectedForum.getGradeAssign() != null && DEFAULT_GB_ITEM.equals(selectedForum.getGradeAssign()))
    {
      forum.setDefaultAssignName( null );
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
  		log.debug("selectedTopic is null in saveTopicSelectedAssignment");
  		return;
  	}
  	
    if(selectedTopic.getGradeAssign() != null && !DEFAULT_GB_ITEM.equals(selectedTopic.getGradeAssign()))
    {
      topic.setDefaultAssignName( selectedTopic.getGradeAssign() );
    }
    else if(selectedTopic.getGradeAssign() != null && DEFAULT_GB_ITEM.equals(selectedTopic.getGradeAssign()))
    {
        topic.setDefaultAssignName( null );
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
    log.debug("processDeleteAttach()");

    ExternalContext context = FacesContext.getCurrentInstance()
        .getExternalContext();
    String attachId = null;

    Map paramMap = context.getRequestParameterMap();
    Iterator<Entry<Object, String>> itr = paramMap.entrySet().iterator();
    while (itr.hasNext())
    {
      Entry<Object, String> entry = itr.next();
      Object key = entry.getKey();
      if (key instanceof String)
      {
        String name = (String) key;
        int pos = name.lastIndexOf("dfmsg_current_attach");

        if (pos >= 0 && name.length() == pos + "dfmsg_current_attach".length())
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
          prepareRemoveAttach.add((DecoratedAttachment) attachments.get(i));
          attachments.remove(i);
          break;
        }
      }
    }

    return null;
  }

  public String getGradePoint() 
  { 
    return gbItemScore; 
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
		  //aparently this could be null
		  if (selectedTopic != null ) {
			  selectedTopic.setMessages(orderedList);
		  }
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
	  currentChange = null;
  }
  
  public String processDfGradeCancel() 
  {
    selectedMessageCount = 0;
    gradeNotify = false; 
    selectedAssign = DEFAULT_GB_ITEM; 
    resetGradeInfo();
    
    getThreadFromMessage();
    return MESSAGE_VIEW;
  } 
  
  public String processDfGradeCancelFromDialog() 
  {
    selectedMessageCount = 0;
    gradeNotify = false; 
    selectedAssign = DEFAULT_GB_ITEM; 
    resetGradeInfo();
    
    getThreadFromMessage();
    return null;
  } 

  public String currentChange;
  public void setCurrentChange(String newChange){
	currentChange = newChange;
  }
  public String getCurrentChange(){
	return currentChange;
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
				  String gradebookUid = toolManager.getCurrentPlacement().getContext();
				  String studentId;
				  if (selectedMessage == null && StringUtils.isNotBlank(selectedGradedUserId)) {
					  studentId = selectedGradedUserId;
				  }else{
					  studentId = userDirectoryService.getUser(selectedMessage.getMessage().getCreatedBy()).getId();  
				  }				   
				  
				  setUpGradeInformation(gradebookUid, toolManager.getCurrentPlacement().getContext(), selectedAssign, studentId);
			  } else {
				  // this is the "Select a gradebook item" option
				  allowedToGradeItem = false;
				  selGBItemRestricted = true;
			  }

			  return GRADE_MESSAGE; 
		  } 
		  catch(Exception e) 
		  { 
			log.error("processGradeAssignChange in DiscussionFOrumTool - {} ", e.toString());
			return null;
		  } 
	  } 
  }

  public void processGradeAssignSend() 
  {
	  String changeAssign = currentChange; // Set value
	  if (changeAssign == null || changeAssign.equals("") || changeAssign.split(",").length > 1) 
	  { 
		  setErrorMessage(getResourceBundleString("cdfm_select_assign"));
	  } 
	  else 
	  {

		  try 
		  { 
			  selectedAssign = changeAssign; 
			  resetGradeInfo();

			  if(!DEFAULT_GB_ITEM.equalsIgnoreCase(selectedAssign)) {
				  String gradebookUid = toolManager.getCurrentPlacement().getContext();
				  if (isGradebookGroupEnabled()) {
					boolean exit = false;
					List<Gradebook> gradebookGroupInstances = getGradingService().getGradebookGroupInstances(gradebookUid);
					int i = 0;
					while (!exit && i < gradebookGroupInstances.size()) {
						Gradebook gradebookGroup = gradebookGroupInstances.get(i);
						List<Assignment> groupAssignments = getGradingService().getAssignments(gradebookGroup.getUid().toString(), toolManager.getCurrentPlacement().getContext(), SortType.SORT_BY_NONE);
						int z = 0;
						while (!exit && z < groupAssignments.size()) {
							Assignment assignment = groupAssignments.get(z);
							if (assignment.getId().toString().equals(selectedAssign)) {
								gradebookUid = gradebookGroup.getUid().toString();
								exit = true;
							}
							z++;
						}
						i++;
					}
				  }
				  String studentId;
				  if(selectedMessage == null && selectedGradedUserId != null && !"".equals(selectedGradedUserId)){
					  studentId = selectedGradedUserId;
				  }else{
					  studentId = userDirectoryService.getUser(selectedMessage.getMessage().getCreatedBy()).getId();  
				  }				   
				  setUpGradeInformation(gradebookUid, toolManager.getCurrentPlacement().getContext(), selectedAssign, studentId);
			  } else {
				  // this is the "Select a gradebook item" option
				  allowedToGradeItem = false;
				  selGBItemRestricted = true;
			  }
		  } 
		  catch(Exception e) 
		  { 
			  log.error("processGradeAssignSend in DiscussionForumTool - " + e); 
		  } 
	  }
  }
 
	/**
	 * Returns {@code true} when the supplied value parses to a non-negative, finite number for the current locale.
	 */
	public boolean isNumber(String validateString) 
	{
			Double parsed = NumberUtil.parseLocaleDouble(validateString, rb.getLocale());
			return parsed != null && parsed >= 0 && Double.isFinite(parsed);
	}	 
     public boolean isFewerDigit(String validateString)
     {
         if (validateString == null) {
             return true;
         }
         // Normalize first so separators are consistent with the locale; if parsing fails, defer to other validators
         final String normalized = NumberUtil.normalizeLocaleDouble(validateString, rb.getLocale());
         final DecimalFormatSymbols dfs = ((DecimalFormat) DecimalFormat.getInstance(rb.getLocale()))
                 .getDecimalFormatSymbols();
         int idx = normalized.lastIndexOf(dfs.getDecimalSeparator());
         // Also handle dot-decimal input when the locale uses a comma
         if (idx < 0 && dfs.getDecimalSeparator() != '.') {
             idx = normalized.lastIndexOf('.');
         }
         // true if no decimal point or at most two digits after it
         return idx < 0 || (normalized.length() - idx - 1) <= 2;
     }
	
	 private boolean validateGradeInput()
	 {
			 GradingService gradingService = getGradingService();
			 if (gradingService == null) {
					 return false;
			 }

       String gradebookUid = getSiteId();
       boolean gradeValid = gradingService.isGradeValid(gradebookUid, gradePoint);

       if (!gradeValid) {
           // see if we can figure out why
           String errorMessageRef = GRADE_INVALID_GENERIC;
           if (GradeType.LETTER != gradingService.getGradeEntryType(gradebookUid)) {
               if(!isNumber(gradePoint))
               {
                   errorMessageRef = GRADE_GREATER_ZERO;
               }
               else if(!isFewerDigit(gradePoint))
               {
                   errorMessageRef = GRADE_DECIMAL_WARN; 
               } 
           }

           FacesContext currentContext = FacesContext.getCurrentInstance();
           String uiComponentId = "msgForum:dfMsgGradeGradePoint";
           FacesMessage validateMessage = new FacesMessage(getResourceBundleString(errorMessageRef));
           validateMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
           currentContext.addMessage(uiComponentId, validateMessage);
       }

       return gradeValid;
   }
  
  public String processDfGradeSubmitFromDialog(){
	  String result = processDfGradeSubmit();
	  if(MESSAGE_VIEW.equals(result)){
		  //success
		  dialogGradeSavedSuccessfully = true;
	  }
	  return null;
  }
  
  public String processDfGradeSubmit() 
  {
	GradingService gradingService = getGradingService();
	if (gradingService == null) {
//		Maybe print an error message if it's possible to get into this state
//		setErrorMessage(getResourceBundleString(STATE_INCONSISTENT));
		return null;
	}
	  if(selectedMessageCount != 0 ) {
			setErrorMessage(getResourceBundleString(STATE_INCONSISTENT));
			return null;
		}
	  selectedMessageCount = 0;
  	
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
	  
	  if(!validateGradeInput())
	      return null;
	  
	  NumberFormat nf = DecimalFormat.getInstance(new ResourceLoader().getLocale());
	  Double gradeAsDouble = null;
	  double pointsPossibleAsDouble = 0.0;
	  try {
	      gradeAsDouble = new Double (nf.parse(gradePoint).doubleValue());
	  } catch(ParseException pe) {
	      // we shouldn't get here if the validation above is working properly
	      log.warn("Error converting grade " + gradePoint + " to Double");
	      return null;
	  }

	  if (gradeByPoints) {
	      try {
	          pointsPossibleAsDouble = nf.parse(gbItemPointsPossible).doubleValue();
	          if((gradeAsDouble.doubleValue() > pointsPossibleAsDouble) && !grade_too_large_make_sure) {
	              setErrorMessage(getResourceBundleString(TOO_LARGE_GRADE));
	              grade_too_large_make_sure = true;
	              return null;
	          } else {
	              log.info("the user confirms he wants to give student higher grade");
	          }	  
	      } catch(ParseException e) {
	          log.warn("Unable to parse points possible " + gbItemPointsPossible + 
	                  " to determine if entered grade is greater than points possible");
	      }	  
	  }
    String studentUid = null;
    try 
    {   
        String siteId = toolManager.getCurrentPlacement().getContext();
        String gradebookUuid = toolManager.getCurrentPlacement().getContext();
		if (isGradebookGroupEnabled()) {
			boolean exit = false;
			List<Gradebook> gradebookGroupInstances = getGradingService().getGradebookGroupInstances(gradebookUuid);
			int i = 0;
			while (!exit && i < gradebookGroupInstances.size()) {
				Gradebook gradebookGroup = gradebookGroupInstances.get(i);
				List<Assignment> groupAssignments = getGradingService().getAssignments(gradebookGroup.getUid().toString(), toolManager.getCurrentPlacement().getContext(), SortType.SORT_BY_NONE);
				int z = 0;
				while (!exit && z < groupAssignments.size()) {
					Assignment assignment = groupAssignments.get(z);
					if (assignment.getId().toString().equals(selectedAssign)) {
						gradebookUuid = gradebookGroup.getUid().toString();
						exit = true;
					}
					z++;
				}
				i++;
			}
		}
        if(selectedMessage == null && selectedGradedUserId != null && !"".equals(selectedGradedUserId)){
        	studentUid = selectedGradedUserId;
        }else{
        	studentUid = userDirectoryService.getUser(selectedMessage.getMessage().getCreatedBy()).getId();
        }
        
        Long gbItemId = gradingService.getAssignmentByNameOrId(gradebookUuid, siteId, selectedAssign).getId();
        gradingService.saveGradeAndCommentForStudent(gradebookUuid, siteId, gbItemId, studentUid, gradePoint, gradeComment);
        
        if(selectedMessage != null){

            // Get fresh copy of current data, to prevent hibernate version discrepancy on
            // intermediate update from "Select a Gradebook item" html select input.
            selectedMessage = new DiscussionMessageBean(
                messageManager.getMessageByIdWithAttachments(selectedMessage.getMessage().getId()),
                messageManager);

        	Message msg = selectedMessage.getMessage();
        //SAK-30711
        	msg.setGradeAssignmentName(Long.toString(gbItemId));
        	msg.setTopic((DiscussionTopic) forumManager
        			.getTopicByIdWithMessages(selectedTopic.getTopic().getId()));
        	Message persistedMessage = forumManager.saveMessage(msg);
        }
        
        setSuccessMessage(getResourceBundleString(GRADE_SUCCESSFUL));
    } 
    catch(SecurityException se) {
    	log.error("Security Exception - processDfGradeSubmit:" + se);
    	setErrorMessage(getResourceBundleString("cdfm_no_gb_perm"));
    }
    catch(Exception e) 
    { 
      log.error("DiscussionForumTool - processDfGradeSubmit:" + e); 
    } 
        
    String eventRef = "";
    String evaluatedItemTitle = "";
    if(selectedMessage != null){
        eventRef = getEventReference(selectedMessage.getMessage());
        evaluatedItemTitle = selectedMessage.getMessage().getTitle();
    }else if(selectedTopic != null){
        eventRef = getEventReference(selectedTopic.getTopic());
        evaluatedItemTitle = selectedTopic.getTopic().getTitle();
    }else if(selectedForum != null){
        eventRef = getEventReference(selectedForum.getForum());
        evaluatedItemTitle = selectedForum.getForum().getTitle();
    }

    LRS_Statement statement = forumManager.getStatementForGrade(studentUid, evaluatedItemTitle, gradeAsDouble).orElse(null);
    Event event = eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_GRADE, eventRef, null, true, NotificationService.NOTI_OPTIONAL, statement);
    eventTrackingService.post(event);

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
  		log.debug("selectedTopic is null in processCheckAll");
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
    if (log.isDebugEnabled())
      log.debug("processValueChangeForMessageView(ValueChangeEvent " + vce
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
    		log.debug("selectedTopic is null in processValueChangeForMessageView");
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
            if ("expand".equals(changeView))
            {
              threaded = true;
              expanded = "true";
              return;
            }
            else
              if ("collapse".equals(changeView))
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
	  if (log.isDebugEnabled())
	      log.debug("processValueChangeForMessageView(ValueChangeEvent " + vce
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
  		log.debug("selectedTopic is null in processValueChangedForMessageOrganize");
  		return;
  	}

  	if (log.isDebugEnabled())
	      log.debug("processValueChangeForMessageView(ValueChangeEvent " + vce
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
  
  /**
   * @return
   */
  public String processActionSearch()
  {
    log.debug("processActionSearch()");

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
  public String processActionMarkAllAsNotRead()
  {
	  return markAllMessagesAsNoRead(true);
  }
  
  /**
   * @return
   */
  public String processActionMarkAllThreadAsNotRead()
  {
	  return markAllThreadAsNotRead(true);
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
      setErrorMessage(getResourceBundleString(NO_MARKED_NO_READ_MESSAGE));
      return ALL_MESSAGES;
    }
    Iterator iter = messages.iterator();
    while (iter.hasNext())
    {
      DiscussionMessageBean decoMessage = (DiscussionMessageBean) iter.next();
      if (decoMessage.isSelected())
      {
        forumManager.markMessageAsNoRead(decoMessage.getMessage(), readStatus);
      }
    }
    return displayTopicById(TOPIC_ID); // reconstruct topic again;
  }

  private String markAllMessagesAsNoRead(boolean readStatus)
  {
	  if (selectedTopic == null)
	    {
	      setErrorMessage(getResourceBundleString(LOST_ASSOCIATE));
	      return ALL_MESSAGES;
	    }
	    List messages = selectedTopic.getMessages();
	    if (messages == null || messages.size() < 1)
	    {
	      setErrorMessage(getResourceBundleString(NO_MARKED_NO_READ_MESSAGE));
	      return ALL_MESSAGES;
	    }
	    Iterator iter = messages.iterator();
	    while (iter.hasNext())
	    {
	      DiscussionMessageBean decoMessage = (DiscussionMessageBean) iter.next();
	      forumManager.markMessageAsNoRead(decoMessage.getMessage(), readStatus);

	    }
	    //return displayTopicById(TOPIC_ID); // reconstruct topic again;
	    setSelectedForumForCurrentTopic(selectedTopic.getTopic());
        selectedTopic = getDecoratedTopic(selectedTopic.getTopic());
		showThreadChanges = false;
	    return processActionDisplayFlatView();
  }
  
  private String markAllThreadAsNotRead(boolean readStatus)
  {
	  if(selectedThreadHead == null){
		  setErrorMessage(getResourceBundleString(LOST_ASSOCIATE));
	      return ALL_MESSAGES;
	  }
	  if(selectedThread == null || selectedThread.size() < 1){
		  setErrorMessage(getResourceBundleString(NO_MARKED_NO_READ_MESSAGE));
	      return ALL_MESSAGES;
	  }
	  Iterator iter = selectedThread.iterator();
	  while (iter.hasNext()){
		  DiscussionMessageBean decoMessage = (DiscussionMessageBean) iter.next();
		  forumManager.markMessageAsNoRead(decoMessage.getMessage(), readStatus);
	  }
	  return processActionGetDisplayThread(readStatus);
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
    log.debug("getSiteMembers()");
        
    if(siteMembers!=null && siteMembers.size()>0)
    {
        return siteMembers;
    }
    
    permissions=new ArrayList();
    
    Set membershipItems = null;
    if(uiPermissionsManager != null){
    	if (PERMISSION_MODE_TEMPLATE.equals(getPermissionMode())){
    		membershipItems = uiPermissionsManager.getAreaItemsSet(forumManager.getDiscussionForumArea());
    	}
    	else if (PERMISSION_MODE_FORUM.equals(getPermissionMode())){    	
    		if (selectedForum != null && selectedForum.getForum() != null)
    		{
    			membershipItems = uiPermissionsManager.getForumItemsSet(selectedForum.getForum());
    			
    			// if there are no membershipItems at this point, either an existing forum
    			// doesn't have any permissions associated with it or it is a new forum.
    			// if the forum is new, we retrieve the area's perms.
    			// otherwise, we'll use the ootb defaults that will be picked up 
    			// via getAreaDBMember at the end of this method
    			if ((membershipItems == null || membershipItems.size() == 0) && selectedForum.getForum().getId() == null)
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
    			
    			// if there are no membershipItems at this point, either an existing
    			// topic doesn't have any permissions associated with it or it is a new topic.
    			// if the topic is new, we will use the forum's perms. otherwise,
    			// there are no perms associated with an existing topic so we
    			// use the defaults
    			if ((membershipItems == null || membershipItems.size() == 0)
                        && selectedTopic.getTopic().getId() == null 
                        && (selectedForum != null && selectedForum.getForum() != null)
                        && uiPermissionsManager != null)
                {
                    membershipItems = uiPermissionsManager.getForumItemsSet(selectedForum.getForum());
                }
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
      realm = authzGroupService.getAuthzGroup(getContextSiteId());
      
      Set roles1 = realm.getRoles();

      if (roles1 != null && roles1.size() > 0)
      {
    	List rolesList = sortRoles(roles1);
    	
        Iterator roleIter = rolesList.iterator();
        while (roleIter.hasNext())
        {
          Role role = (Role) roleIter.next();
          if (role != null && authzGroupService.isRoleAssignable(role.getId()))
          {
            if(i==0)
            {
              selectedRole = role.getId();
              i=1;
            }
            DBMembershipItem item = forumManager.getAreaDBMember(membershipItems, role.getId(), MembershipItem.TYPE_ROLE);
            String level = item.getPermissionLevelName();
            siteMembers.add(new SelectItem(role.getId(), role.getId() + " (" + getResourceBundleString("perm_level_" + level.replaceAll(" ", "_").toLowerCase()) + ")"));
            permissions.add(new PermissionBean(item, permissionLevelManager));
          }
        }
      }  
        
      if(includeGroup)
      {
    	  currentSite = siteService.getSite(toolManager.getCurrentPlacement().getContext());   
      
    	  Collection groups = currentSite.getGroups();

    	  groups = sortGroups(groups);
    	  
    	  for (Iterator groupIterator = groups.iterator(); groupIterator.hasNext();)
    	  {
    		  Group currentGroup = (Group) groupIterator.next();  
    		  DBMembershipItem item = forumManager.getAreaDBMember(membershipItems,currentGroup.getTitle(), MembershipItem.TYPE_GROUP);
    		  String level = item.getPermissionLevelName();
    		  siteMembers.add(new SelectItem(currentGroup.getTitle(), currentGroup.getTitle() + " (" + getResourceBundleString("perm_level_" + level.replaceAll(" ", "_").toLowerCase()) + ")"));
    		  permissions.add(new PermissionBean(item, permissionLevelManager));
    	  }
      }
    }
    catch (IdUnusedException e)
    {
      log.error(e.getMessage(), e);
    } catch (GroupNotDefinedException e) {
		log.error(e.getMessage(), e);
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
	  Collections.sort(rolesList, new RoleIdComparator());
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
	  Collections.sort(sortGroupsList, new GroupTitleComparator());
	  groups.clear();
	  groups.addAll(sortGroupsList);
	  return groups;
  }
  /**
   * @return siteId
   */
  private String getContextSiteId()
  {
    log.debug("getContextSiteId()");
    return ("/site/" + toolManager.getCurrentPlacement().getContext());
  }

  /**
   * @param topic
   */
  private void setSelectedForumForCurrentTopic(DiscussionTopic topic)
  {
    DiscussionForumBean oldSelectedForum = selectedForum;
    DiscussionForum forum = (DiscussionForum) topic.getBaseForum();
    if (forum == null)
    {

      String forumId = getExternalParameterByKey(FORUM_ID);
      if (forumId == null || forumId.trim().length() < 1)
      {
        selectedForum = oldSelectedForum;
        return;
      }
      forum = forumManager.getForumById(Long.valueOf(forumId));
      if (forum == null)
      {
        selectedForum = oldSelectedForum;
        return;
      }
    }
    selectedForum = new DiscussionForumBean(forum, forumManager, userTimeService);
    loadForumDataInForumBean(forum, selectedForum);
    if (selectedForum == null) {
    	selectedForum = oldSelectedForum;
    }
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
    log.debug("setErrorMessage(String " + errorMsg + ")");
    FacesContext facesContext = FacesContext.getCurrentInstance();
    facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, getResourceBundleString(ALERT) + errorMsg, null));
    facesContext.getExternalContext().getFlash().setKeepMessages(true);
  }
  
  private void setSuccessMessage(String successMsg)
  {
	  log.debug("setSuccessMessage(String " + successMsg + ")");
	  FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, successMsg, null));
  }
 
  public void processPost(){
  	
  }
  
  // Getter methods for permission level arrays (for JSF access)
  public String getOwnerLevelArray() {
  	return permissionLevelManager.getDefaultOwnerPermissionLevel().toString();
  }

  public String getAuthorLevelArray() {
  	return permissionLevelManager.getDefaultAuthorPermissionLevel().toString();
  }

  public String getNoneditingAuthorLevelArray() {
  	return permissionLevelManager.getDefaultNoneditingAuthorPermissionLevel().toString();
  }

  public String getReviewerLevelArray() {
  	return permissionLevelManager.getDefaultReviewerPermissionLevel().toString();
  }

  public String getNoneLevelArray() {
  	return permissionLevelManager.getDefaultNonePermissionLevel().toString();
  }

  public String getContributorLevelArray() {
  	return permissionLevelManager.getDefaultContributorPermissionLevel().toString();
  }

  public void setObjectPermissions(Object target){
    if (permissions != null) {
      Area area = null;
      DiscussionForum forum = null;
      DiscussionTopic topic = null;

      Set<DBMembershipItem> oldMembershipItemSet = null;
      Set<DBMembershipItem> membershipItemSet = new HashSet<>();

      if (target instanceof DiscussionForum){
          forum = ((DiscussionForum) target);
          oldMembershipItemSet = uiPermissionsManager.getForumItemsSet(forum);
      } else if (target instanceof Area){
          area = ((Area) target);
          oldMembershipItemSet = uiPermissionsManager.getAreaItemsSet(area);
      } else if (target instanceof Topic){
          topic = ((DiscussionTopic) target);
          oldMembershipItemSet = uiPermissionsManager.getTopicItemsSet(topic);
      }

      for (PermissionBean permBean : permissions) {
        //for group awareness
        //DBMembershipItem membershipItem = permissionLevelManager.createDBMembershipItem(permBean.getItem().getName(), permBean.getSelectedLevel(), DBMembershipItem.TYPE_ROLE);
        DBMembershipItem membershipItem = permissionLevelManager.createDBMembershipItem(permBean.getItem().getName(), permBean.getSelectedLevel(), permBean.getItem().getType());
        setupMembershipItemPermission(membershipItem, permBean);

        // save DBMembershiptItem here to get an id so we can add to the set
        membershipItem = permissionLevelManager.saveDBMembershipItem(membershipItem);

        membershipItemSet.add(membershipItem);
      }

      if (forum != null) {
        final DiscussionForum f = forum;
        area = f.getArea();
        forum.setMembershipItemSet(membershipItemSet);
        membershipItemSet.forEach(i -> ((DBMembershipItemImpl) i).setForum(f));
      } else if (area != null) {
        final Area a = area;
        area.setMembershipItemSet(membershipItemSet);
        membershipItemSet.forEach(i -> ((DBMembershipItemImpl) i).setArea(a));
      } else if (topic != null) {
        final Topic t = topic;
        area = t.getBaseForum().getArea();
        topic.setMembershipItemSet(membershipItemSet);
        membershipItemSet.forEach(i -> ((DBMembershipItemImpl) i).setTopic(t));
      }
      permissionLevelManager.deleteMembershipItems(oldMembershipItemSet);
    }
    siteMembers = null;
  }

	/**
	 * Using a PermissionBean, constructs a PermissionMask, then constructs a PermissionLevel, and assigns it to a MembershipItem
	 * @param membershipItem membershipItem the item on which to apply the new permission level
	 * @param permBean the PermissionBean from which the new PermissionLevel should be based
	 */
	private void setupMembershipItemPermission(DBMembershipItem membershipItem, PermissionBean permBean)
	{
		PermissionsMask mask = new PermissionsMask();                
		mask.put(PermissionLevel.NEW_FORUM, Boolean.valueOf(permBean.getNewForum())); 
		mask.put(PermissionLevel.NEW_TOPIC, Boolean.valueOf(permBean.getNewTopic()));
		mask.put(PermissionLevel.NEW_RESPONSE, Boolean.valueOf(permBean.getNewResponse()));
		mask.put(PermissionLevel.NEW_RESPONSE_TO_RESPONSE, Boolean.valueOf(permBean.getResponseToResponse()));
		mask.put(PermissionLevel.MOVE_POSTING, Boolean.valueOf(permBean.getMovePosting()));
		mask.put(PermissionLevel.CHANGE_SETTINGS,Boolean.valueOf(permBean.getChangeSettings()));
		mask.put(PermissionLevel.POST_TO_GRADEBOOK, Boolean.valueOf(permBean.getPostToGradebook()));
		mask.put(PermissionLevel.READ, Boolean.valueOf(permBean.getRead()));
		mask.put(PermissionLevel.MARK_AS_NOT_READ,Boolean.valueOf(permBean.getMarkAsNotRead()));
		mask.put(PermissionLevel.MODERATE_POSTINGS, Boolean.valueOf(permBean.getModeratePostings()));
		mask.put(PermissionLevel.IDENTIFY_ANON_AUTHORS, Boolean.valueOf(permBean.getIdentifyAnonAuthors()));
		mask.put(PermissionLevel.DELETE_OWN, Boolean.valueOf(permBean.getDeleteOwn()));
		mask.put(PermissionLevel.DELETE_ANY, Boolean.valueOf(permBean.getDeleteAny()));
		mask.put(PermissionLevel.REVISE_OWN, Boolean.valueOf(permBean.getReviseOwn()));
		mask.put(PermissionLevel.REVISE_ANY, Boolean.valueOf(permBean.getReviseAny()));
		
		PermissionLevel level = permissionLevelManager.createPermissionLevel(permBean.getSelectedLevel(), typeManager.getCustomLevelType(), mask);
		membershipItem.setPermissionLevel(level);
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
         
    courseMemberMap = membershipManager.getAllCourseMembers(true, false, false, null);
 
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
             levels.add(new SelectItem(level, getResourceBundleString("perm_level_" + level.replaceAll(" ", "_").toLowerCase())));
             if("Custom".equals(level))
                 {
                   hasCustom =true;
                 }
           }
         }
         if(!hasCustom)
         {
           levels.add(new SelectItem("Custom", getResourceBundleString("perm_level_custom")));
         }
       }       
       return levels;
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
	        return rb.getString(key);
	    }

	    public static String getResourceBundleString(String key, Object[] args) {
	    	return rb.getFormattedMessage(key, args);
	    }

	public String getUserNameOrEid()
	{
	  try
	  {
		String currentUserId = getUserId();

		  String userString = "";
		  userString = userDirectoryService.getUser(currentUserId).getDisplayName();
		  String userEidString = "";
		  userEidString = userDirectoryService.getUser(currentUserId).getDisplayId();
		  
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
  		log.error(e.getMessage(), e);
  	  }
  	  
  	  return getUserId();
	}
	
 	public TimeZone getUserTimeZone() {
 		return userTimeService.getLocalTimeZone();
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
			return !selectedForum.getForumModerated();
		else if (PERMISSION_MODE_TOPIC.equals(permissionMode) && selectedTopic != null)
			return !selectedTopic.getTopicModerated();
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
  		log.debug("selectedTopic is null in getMessages");
  		return messages;
  	}
  	
		messages = selectedTopic.getMessages();
		
		if (messages != null && !messages.isEmpty())
			messages = filterModeratedMessages(messages, selectedTopic.getTopic(), selectedForum.getForum());

		showThreadChanges = true;
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

			if(selectedTopic != null){
				boolean postFirst = getNeedToPostFirst();
				if(postFirst){
					//return an empty list, b/c user needs to post before seeing any messages
					return viewableMsgs;
				}
			}
			
			boolean hasModeratePerm = uiPermissionsManager.isModeratePostings(topic, forum);
			boolean excludeDeleted = ServerConfigurationService.getBoolean("msgcntr.forums.exclude.deleted", false);
			boolean excludeDeletedOnlyWithoutChild = ServerConfigurationService.getBoolean("msgcntr.forums.exclude.deleted.onlywithoutdescendant", true);
			if (hasModeratePerm){
				if ((excludeDeleted) || (excludeDeletedOnlyWithoutChild)) {
					Iterator msgIter = messages.iterator();
					List msgs = new ArrayList();
					while (msgIter.hasNext()){
						DiscussionMessageBean msg = (DiscussionMessageBean) msgIter.next();
						if ((!msg.getDeleted()) || ((excludeDeletedOnlyWithoutChild) && (msg.getHasNotDeletedDescendant(null))))
							msgs.add(msg);
					}
					return msgs;
				}else{
					return messages;
				}
			}else{			
				Iterator msgIter = messages.iterator();
				while (msgIter.hasNext()){
					DiscussionMessageBean msg = (DiscussionMessageBean) msgIter.next();
					if ((msg.isMsgApproved() || msg.getIsOwn()) && (!excludeDeleted || (excludeDeleted && !msg.getDeleted()) || (excludeDeletedOnlyWithoutChild && msg.getHasNotDeletedDescendant(null))))
						viewableMsgs.add(msg);
				}
			}
		}
		
		return viewableMsgs;
	}
	
   public String processReturnToOriginatingPage()
   {
	   log.debug("processReturnToOriginatingPage()");
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
     // ern believes the faces-redirect=true is crucial to carry over the error message to new page
     // return (isForumsTool() ? FORUMS_MAIN : MAIN) + "?faces-redirect=true";
     return (isForumsTool() ? FORUMS_MAIN : MAIN);
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
						+ toolManager.getCurrentPlacement().getId() + Entity.SEPARATOR + "discussionForum" 
						+ Entity.SEPARATOR + "message" + Entity.SEPARATOR 
						+ "printFriendly";
	  }
	 
	 public String getPrintFriendlyUrlThread()
	  {
		  return ServerConfigurationService.getToolUrl() + Entity.SEPARATOR
						+ toolManager.getCurrentPlacement().getId() + Entity.SEPARATOR + "discussionForum" 
						+ Entity.SEPARATOR + "message" + Entity.SEPARATOR 
						+ "printFriendlyThread";
	  }
	 
	 public String getPrintFriendlyAllAuthoredMsg()
	  {
		  return ServerConfigurationService.getToolUrl() + Entity.SEPARATOR
						+ toolManager.getCurrentPlacement().getId() + Entity.SEPARATOR + "discussionForum" 
						+ Entity.SEPARATOR + "statistics" + Entity.SEPARATOR 
						+ "printFriendlyAllAuthoredMsg";
	  }
	 
	 public String getPrintFriendlyFullTextForOne()
	  {
		  return ServerConfigurationService.getToolUrl() + Entity.SEPARATOR
						+ toolManager.getCurrentPlacement().getId() + Entity.SEPARATOR + "discussionForum" 
						+ Entity.SEPARATOR + "statistics" + Entity.SEPARATOR 
						+ "printFriendlyFullTextForOne";
	  }
	 
	 public String getPrintFriendlyDisplayInThread()
	  {
		  return ServerConfigurationService.getToolUrl() + Entity.SEPARATOR
						+ toolManager.getCurrentPlacement().getId() + Entity.SEPARATOR + "discussionForum" 
						+ Entity.SEPARATOR + "statistics" + Entity.SEPARATOR 
						+ "printFriendlyDisplayInThread";
	  }

	public Boolean isMessageReadForUser(Long topicId, Long messageId) {
		return messageManager.isMessageReadForUser(topicId, messageId);
	}

	public Boolean isMessageNotReadForUser(Long topicId, Long messageId) {
		return !messageManager.isMessageReadForUser(topicId, messageId);
	}

	 public void markMessageNotReadForUser(Long topicId, Long messageId, Boolean read)
	 {
		 messageManager.markMessageNotReadForUser(topicId, messageId, read);
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
			 log.debug("selectedTopic is null in refreshSelectedMessageSettings");
			 return;
		 }
		 boolean isOwn = message.getCreatedBy().equals(getUserId());
		 selectedMessage.setRevise(selectedTopic.getIsReviseAny() 
					|| (selectedTopic.getIsReviseOwn() && isOwn));  
		 selectedMessage.setUserCanDelete(selectedTopic.getIsDeleteAny() || (isOwn && selectedTopic.getIsDeleteOwn()));
		 boolean useAnonymousId = isUseAnonymousId(selectedTopic.getTopic());
		 selectedMessage.setUserCanEmail(!useAnonymousId && (isInstructor() || isSectionTA()));

		 // Set Rank for selectedMessage.
		 String userEid = message.getCreatedBy();
		 Rank thisrank = this.getAuthorRank(userEid);
		 selectedMessage.setAuthorRank(thisrank);
		 selectedMessage.setAuthorPostCount(userEid);

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
	 
	 //Returns if the property mc.showShortDescription is set to true or false. Default value is true
	 public boolean getShowShortDescription() {
		 return showShortDescription;
	 }
	 
	 //Checks for the showShortDescription property and existence of forum's short description
	 public boolean getShowForumShortDescription() {
		 if (this.selectedForum == null || this.selectedForum.getForum() == null)
			 return false;

		 String shortDescription= this.selectedForum.getForum().getShortDescription();
		 if (shortDescription!=null){
			 if (!showShortDescription && shortDescription.isEmpty()){
				 return false;
			 }
			 else{
				 return true;
			 }
		 }
		 return showShortDescription;
	 }
	 
	 //Checks for the showShortDescription property and existence of topic's short description
	 public boolean getShowTopicShortDescription() {
		 if (this.selectedTopic == null || this.selectedTopic.getTopic() == null)
			 return false;

		 String shortDescription= this.selectedTopic.getTopic().getShortDescription();
		 if (shortDescription!=null){
			 if (!showShortDescription && shortDescription.isEmpty()){
				 return false;
			 }
			 else{
				 return true;
			 }
		 }
		 return showShortDescription;
	 }
	 
	//Returns property value of mc.collapsePermissionPanel, default is false
	 public String getCollapsePermissionPanel() {
		 if(collapsePermissionPanel){
			 return "true";
		 }
		 return "false";
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
		 selectedForum = getDecoratedForum(forum);
		 if (!isInstructor()) {
			 Collections.sort(selectedTopic.getMessages(), DATE_COMPARATOR);
		 }

		 if (uiPermissionsManager.isRead((DiscussionTopic)topic, forum)) {
			 List messageList = messageManager.findMessagesByTopicId(topic.getId());
			 Iterator messageIter = messageList.iterator();
			 while(messageIter.hasNext()){
				 Message mes = (Message) messageIter.next();					
				 messageManager.markMessageNotReadForUser(topic.getId(), mes.getId(), false, getUserId());
			 }
		 }

		 return "dfStatisticsDisplayInThread";
	 }

	 public String processActionDisplayFullText() {
		 return "dfStatisticsFullTextForOne";
	 }

	public String processActionWatch() {
		log.debug("processActionWatch()");
		User curruser = userDirectoryService.getCurrentUser();
		log.debug("got user: " + curruser.getDisplayId());
		EmailNotification userwatchoption = emailNotificationManager.getEmailNotification(curruser.getId());
		log.debug("userwatchoption = " + userwatchoption.getNotificationLevel());
		if (watchSettingsBean == null){
			log.debug("watchsettingbean = null");
			watchSettingsBean = new EmailNotificationBean(userwatchoption);
		}
		watchSettingsBean.setEmailNotification(userwatchoption);
		log.debug("watchsettingbean's user = " + watchSettingsBean.getEmailNotification().getUserId() + "  ,emailoption= " + 
				watchSettingsBean.getEmailNotification().getNotificationLevel());
		return WATCH_SETTING;
	}

	public String processActionSaveEmailNotificationOption() {
		log.debug("ForumTool.processActionSaveEmailNotificationOption()");
		if ((watchSettingsBean !=null) && (watchSettingsBean.getEmailNotification()!=null)){
			log.debug("watchSettingsBean !=null) && (watchSettingsBean.getEmailNotification()!=null");
			EmailNotification newoption = watchSettingsBean.getEmailNotification();
			emailNotificationManager.saveEmailNotification(newoption);
		}
		else {
			log.debug("ForumTool.processActionSaveEmailNotificationOption(): Can not save because watchSettingsBean is null");
			// should come here
		}
		
		return gotoMain();
	}
	
	public List<String> getUserEmailsToBeNotifiedByLevel(List userlist) {
		List<String> emaillist = new ArrayList<String>();

		List<User> userMailList  = userDirectoryService.getUsers(userlist);
		for (int i = 0; i < userMailList.size(); i++) {
			User user = userMailList.get(i); 
			String useremail = user.getEmail();
			if (useremail != null && !"".equalsIgnoreCase(useremail)) {
				if (log.isDebugEnabled()) {
					log.debug("Username = " + user.getDisplayId()
							+ " , useremail : " + useremail);
				}
				emaillist.add(useremail);
			}
		}
		
		return emaillist;
	}
	
	public void sendEmailNotification(Message reply, DiscussionMessageBean currthread){
		log.debug("ForumTool.sendEmailNotification(Message, DiscussionMessageBean)");
		sendEmailNotification(reply, currthread, false);
	}
	
	public void  sendEmailNotification(Message reply, DiscussionMessageBean currthread, boolean needsModeration){

		if (!reply.getTopic().getAllowEmailNotifications()) {
			return;
		}

		// get all users with notification level = 2
		List<String> userlist = emailNotificationManager.getUsersToBeNotifiedByLevel( EmailNotification.EMAIL_REPLY_TO_ANY_MESSAGE);
		
		if (log.isDebugEnabled()){
			log.debug("total count of Level 2 users = " + userlist.size());
			Iterator iter1 = userlist.iterator();
			while (iter1.hasNext()){
				log.debug("level 2 users notify all msg:  sendEmailNotification: sending to  " + (String) iter1.next());
			}
		}
		
		// need to get a list of authors for all messages on the current thread, and then check their notification level. 
		//selectedThread  is a list of DiscussionMessageBean in the current thread.
		
		Iterator iter = selectedThread.iterator();
		while (iter.hasNext()){
			DiscussionMessageBean decoMessage = (DiscussionMessageBean) iter.next();
			String threadauthor = decoMessage.getMessage().getCreatedBy();
			// don't include the reply author
			if (!threadauthor.equals(reply.getCreatedBy())) {
				EmailNotification authorNotificationLevel = emailNotificationManager.getEmailNotification(threadauthor);
				// only add level 1 users , since we've already got level2 users.
				if (EmailNotification.EMAIL_REPLY_TO_MY_MESSAGE.equalsIgnoreCase(authorNotificationLevel.getNotificationLevel())){
					log.debug("The author: {} wants to be notified", threadauthor);
					userlist.add(threadauthor);
				}
			}
		}

		// MSGCNTR-375 if this post needs to be moderated, only send the email notification to those with moderator permission
		// Except if the moderator is the creator of the topic.
		if (needsModeration && !selectedTopic.getIsModeratedAndHasPerm()) {
			DiscussionTopic topic = (DiscussionTopic)reply.getTopic();
			DiscussionForum forum = (DiscussionForum)topic.getBaseForum();

			log.debug("Filtering userlist to only return moderators. Had: " + userlist.size());

			List<String> nonModerators = new ArrayList<String>();
			for(String userId: userlist) {
				if(!uiPermissionsManager.isModeratePostings(topic, forum, userId)) {
					log.debug("userId: " + userId + " is not a moderator");
					nonModerators.add(userId);
				}
			}

			userlist.removeAll(nonModerators);
			log.debug("filtering complete. Now have: " + userlist.size());

		}
		
		// now we need to remove duplicates:
		Set<String> set = new HashSet<String>();
		set.addAll(userlist);
		
//		avoid overhead :D
			log.debug("set size " + set.size());
			log.debug("userlist size " + userlist.size());
		if(set.size() < userlist.size()) {
			userlist.clear();
			userlist.addAll(set);
		}
		
		//MSGCNTR-741 need to filter out post first users
		if (((DiscussionTopic)reply.getTopic()).getPostFirst()) {
		    Topic topicWithMessages = forumManager.getTopicByIdWithMessagesAndAttachments(reply.getTopic().getId());
		    userlist.removeAll(getNeedToPostFirst(userlist, (DiscussionTopic)reply.getTopic(), topicWithMessages.getMessages()));
		}

		// now printing out all users = # of messages in the thread - level 2 users
		if (log.isDebugEnabled()){
			log.debug("now printing out all users, including duplicates count = " + userlist.size());
			Iterator iter1 = userlist.iterator();
			while (iter1.hasNext()){
				log.debug("sendEmailNotification: should include both level 1 and level 2 sending to  " + (String) iter1.next());
			}
		}
		
		// now printing out all users again after removing duplicate
		if (log.isDebugEnabled()){
			log.debug("now printing out all users again after removing duplicate count = " + userlist.size());
			Iterator iter1 = userlist.iterator();
			while (iter1.hasNext()){
				log.debug("" + (String) iter1.next());
			}
		}
		
		//now we need to filer the list\
		if (log.isDebugEnabled())
			log.debug("About to filter list");
		List<String> finalList = emailNotificationManager.filterUsers(userlist, currthread.getMessage().getTopic());
		
		List<String> useremaillist =  getUserEmailsToBeNotifiedByLevel(finalList);

		if (log.isDebugEnabled()){
			log.debug("now printint unique emails , count = " + useremaillist.size());
			Iterator useremaillistiter = useremaillist.iterator();
			while (useremaillistiter.hasNext()){
				log.debug("sendEmailNotification: sending to  " + (String) useremaillistiter.next());
			}
		}
		
		if (userlist.isEmpty()) {
			log.debug("No users need to notified.");
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
		
		if (getSelectedMessage() == null || getSelectedMessage().getMessage() == null) {
			return null;
		}
		
		String msgId = getSelectedMessage().getMessage().getId().toString();
		String topicId = getSelectedTopic().getTopic().getId().toString();
		String forumId = getSelectedTopic().getTopic().getOpenForum().getId().toString();
		log.debug("message: " + msgId + " topic: " + topicId + " forum: " + forumId);
		
		String context = siteService.siteReference(toolManager.getCurrentPlacement().getContext());
		log.debug("context: " + context);
		
		developerHelperService = getDevelperHelperService();
		String url = "";
		try{
			SakaiToolData toolData = developerHelperService.getToolData("sakai.forums", context);
			String toolId = toolData.getPlacementId();
			String pageUrl = toolData.getToolURL();
			String toolUrl = pageUrl.substring(0,pageUrl.indexOf("/page/")); 
			url =  toolUrl + "/tool/" + toolId + path + "?forumId=" + forumId + "&topicId=" + topicId + "&messageId=" + msgId;
			log.debug("url: " + url);
		}catch (Exception e) {
			log.warn(e.getMessage());
		}
		return url;
	}

	private class ThreadUpdateSorter implements Comparator<DiscussionMessageBean>{

	    public int compare(DiscussionMessageBean dmb1, DiscussionMessageBean dmb2) {
	    	//MSGCNTR-446 if one dmb2 is null or 
	    	if (dmb2 == null || dmb2.getMessage() == null || dmb2.getMessage().getDateThreadlastUpdated() == null) {
	    		return 1;
	    	}
	    	
	    	if (dmb1 == null || dmb1.getMessage() == null || dmb1.getMessage().getDateThreadlastUpdated() == null) {
	    		return -1;
	    	}
	    	
	        return dmb2.getMessage().getDateThreadlastUpdated().compareTo(dmb1.getMessage().getDateThreadlastUpdated());
	    }
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

    /**
   * Forward to duplicate forum confirmation screen
   *
   * @return
   */
  public String processActionDuplicateForumConfirm()
  {
    log.debug("processActionDuplicateForumConfirm()");
    if (selectedForum == null)
    {
      log.debug("There is no forum selected for duplication");
      return gotoMain();
    }

    if(!getNewForum())
    {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_TO_DUPLICATE));
      return gotoMain();
    }
    //in case XSS was slipped in, make sure we remove it:
    selectedForum.getForum().setExtendedDescription(formattedText.processFormattedText(selectedForum.getForum().getExtendedDescription(), null, null));
    selectedForum.getForum().setTitle(getResourceBundleString(DUPLICATE_COPY_TITLE, new Object [] {selectedForum.getForum().getTitle()} ));

    selectedForum.setMarkForDuplication(true);
    return FORUM_SETTING;
  }

   /**
   * Action for the duplicate option present the main forums page
   * @return
   */
  public String processActionDuplicateForumMainConfirm()
  {

	  log.debug("processActionDuplicateForumMainConfirm()");

	  String forumId = getExternalParameterByKey(FORUM_ID);
	  DiscussionForum forum = forumManager.getForumById(Long.valueOf(forumId));
	  selectedForum = new DiscussionForumBean(forum, forumManager, userTimeService);
	  loadForumDataInForumBean(forum, selectedForum);
      selectedForum.getForum().setTitle(getResourceBundleString(DUPLICATE_COPY_TITLE, new Object[] {selectedForum.getForum().getTitle()}));
	  selectedForum.setMarkForDuplication(true);
	  return FORUM_SETTING;
  }

  /**
   * @return
   */
  public String processActionDuplicateForum()
  {
    if (uiPermissionsManager == null)
    {
      throw new IllegalStateException("uiPermissionsManager == null");
    }
    if (selectedForum == null)
    {
      throw new IllegalStateException("selectedForum == null");
    }
    if(!getNewForum())
    {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_TO_DUPLICATE));
      return gotoMain();
    }
    if(selectedForum.getForum()!=null && 
        (selectedForum.getForum().getTitle()==null 
          ||selectedForum.getForum().getTitle().trim().length()<1  ))
    {
      setErrorMessage(getResourceBundleString(VALID_FORUM_TITLE_WARN));
      return FORUM_SETTING;
    }
    Long forumId = selectedForum.getForum().getId();

	duplicateForum(forumId);
	  reset();
	  return gotoMain();
  }

   /**
   * @return
   */
  public String processActionDuplicateTopicConfirm()
  {
    log.debug("processActionDuplicateTopicConfirm()");

    if (selectedTopic == null)
    {
      log.debug("There is no topic selected for duplication");
      return gotoMain();
    }
    if(!uiPermissionsManager.isNewTopic(selectedForum.getForum()))
    {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_NEW_TOPIC));
      return gotoMain();
    }
    //in case XSS was slipped in, make sure we remove it:
    selectedTopic.getTopic().setExtendedDescription(formattedText.processFormattedText(selectedTopic.getTopic().getExtendedDescription(), null, null));
    selectedTopic.getTopic().setTitle(getResourceBundleString(DUPLICATE_COPY_TITLE, new Object[] {selectedTopic.getTopic().getTitle()}));
    selectedTopic.setMarkForDuplication(true);
    return TOPIC_SETTING;
  }

    /**
   * @return
   */
  public String processActionDuplicateTopicMainConfirm()
  {
	  {
		  log.debug("processActionDuplicateTopicMainConfirm()");

		  DiscussionTopic topic = null;
		  String topicId = getExternalParameterByKey(TOPIC_ID);
		  if(StringUtils.isNotBlank(topicId) && !"null".equals(topicId)){
			  topic = (DiscussionTopic) forumManager.getTopicByIdWithAttachments(Long.valueOf(topicId));
		  } else if(selectedTopic != null) {
			  topic = selectedTopic.getTopic();
		  }
		  if (topic == null)
		  {
			  return gotoMain();
		  }
		  setSelectedForumForCurrentTopic(topic);
		  if(!uiPermissionsManager.isNewTopic(selectedForum.getForum()))
		  {
			  setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_NEW_TOPIC));
			  return gotoMain();
		  }
		  selectedTopic = new DiscussionTopicBean(topic, selectedForum.getForum(), forumManager, rubricsService, userTimeService);
		  loadTopicDataInTopicBean(topic, selectedTopic);
          selectedTopic.getTopic().setTitle(getResourceBundleString(DUPLICATE_COPY_TITLE, new Object[] {selectedTopic.getTopic().getTitle()}));
		  selectedTopic.setMarkForDuplication(true);
		  return TOPIC_SETTING;
	  }
  }

  public String processActionDuplicateTopic()
  {
    log.debug("processActionDuplicateTopic()");
    if (selectedTopic == null)
    {
      log.debug("There is no topic selected for duplication");
      return gotoMain();
    }
    if(!uiPermissionsManager.isNewTopic(selectedForum.getForum()))
    {
      setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_NEW_TOPIC));
      return gotoMain();
    }
    setPermissionMode(PERMISSION_MODE_TOPIC);
    if(selectedTopic!=null && selectedTopic.getTopic()!=null && 
        (selectedTopic.getTopic().getTitle()==null 
          ||selectedTopic.getTopic().getTitle().trim().length()<1  ))
    {
      setErrorMessage(getResourceBundleString(VALID_TOPIC_TITLE_WARN));
      return TOPIC_SETTING;
    }
    HashMap<String, Integer> beforeChangeHM = null;
	DiscussionForum forum = selectedForum.getForum();
    Long topicId = selectedTopic.getTopic().getId();

	duplicateTopic(topicId, forum, false);

    reset();
    return gotoMain();
  }

  private DiscussionTopicBean duplicateTopic(Long originalTopicId, DiscussionForum forum, boolean forumDuplicate) {
	log.debug("duplicateTopic(" + originalTopicId + ")");

    if("true".equalsIgnoreCase(ServerConfigurationService.getString("mc.defaultLongDescription")))
    {
    	selectedForum.setReadFullDesciption(true);
    }

    DiscussionTopic newTopic = forumManager.createTopic(forum);
    if (newTopic == null)
    {
      setErrorMessage(getResourceBundleString(FAILED_CREATE_TOPIC));
      return null;
    }
	DiscussionTopic fromTopic = (DiscussionTopic) forumManager.getTopicByIdWithAttachments(originalTopicId);
	String newTitle;
	if (forumDuplicate) {
        newTitle = fromTopic.getTitle();
		newTopic.setSortIndex(fromTopic.getSortIndex());
	} else {
        newTitle = selectedTopic.getTopic().getTitle();
	}
	newTopic.setTitle(newTitle);
	log.debug("New Topic Title = " + newTopic.getTitle());
	if (fromTopic.getShortDescription() != null && fromTopic.getShortDescription().length() > 0)
		newTopic.setShortDescription(fromTopic.getShortDescription());
	if (fromTopic.getExtendedDescription() != null && fromTopic.getExtendedDescription().length() > 0)
		newTopic.setExtendedDescription(fromTopic.getExtendedDescription());
	newTopic.setLocked(fromTopic.getLocked());
	newTopic.setModerated(fromTopic.getModerated());
	newTopic.setPostFirst(fromTopic.getPostFirst());
	newTopic.setPostAnonymous(fromTopic.getPostAnonymous());
	newTopic.setRevealIDsToRoles(fromTopic.getRevealIDsToRoles());
	newTopic.setAutoMarkThreadsRead(fromTopic.getAutoMarkThreadsRead());

	// Get/set the topic's permissions
	Set<DBMembershipItem> fromTopicMembershipItems = uiPermissionsManager.getTopicItemsSet(fromTopic);
	if (fromTopicMembershipItems != null) {
      for (DBMembershipItem fromTopicMembershipItem : fromTopicMembershipItems) {
        DBMembershipItem membershipItemCopy = getMembershipItemCopy(fromTopicMembershipItem);
        DBMembershipItem savedMembershipItem = permissionLevelManager.saveDBMembershipItem(membershipItemCopy);
        newTopic.addMembershipItem(savedMembershipItem);
        ((DBMembershipItemImpl) savedMembershipItem).setTopic(newTopic);
      }
	}

	// Add the attachments - create true copies of the files, not just references
	List fromTopicAttach = forumManager.getTopicByIdWithAttachments(originalTopicId).getAttachments();
	if (fromTopicAttach != null && !fromTopicAttach.isEmpty()) {
		for (int topicAttach=0; topicAttach < fromTopicAttach.size(); topicAttach++) {
			Attachment thisAttach = (Attachment)fromTopicAttach.get(topicAttach);
			Attachment thisDFAttach = forumManager.createDuplicateDFAttachment(
					thisAttach.getAttachmentId(),
					thisAttach.getAttachmentName());
			newTopic.addAttachment(thisDFAttach);
		}
	}

  String fromAssignmentTitle = fromTopic.getDefaultAssignName();
  newTopic.setDefaultAssignName(fromAssignmentTitle);
	
	// copy the release/end dates	
	if(fromTopic.getAvailabilityRestricted()){
		newTopic.setAvailabilityRestricted(true);
		newTopic.setOpenDate(fromTopic.getOpenDate());
		newTopic.setCloseDate(fromTopic.getCloseDate());
		newTopic.setLockedAfterClosed(fromTopic.getLockedAfterClosed());
	}

	newTopic.setBaseForum(forum);

	LRS_Statement statement = forumManager.getStatementForUserPosted(newTopic.getTitle(), SAKAI_VERB.interacted).orElse(null);
	ForumsTopicEventParams params = new ForumsTopicEventParams(ForumsTopicEventParams.TopicEvent.ADD, statement);

	newTopic = forumManager.saveTopic(newTopic, fromTopic.getDraft(), params);
	selectedTopic = new DiscussionTopicBean(newTopic, forum, forumManager, rubricsService, userTimeService);
	loadTopicDataInTopicBean(newTopic, selectedTopic);

    if("true".equalsIgnoreCase(ServerConfigurationService.getString("mc.defaultLongDescription")))
    {
    	selectedTopic.setReadFullDesciption(true);
    }

    setNewTopicBeanAssign();

    DiscussionTopicBean thisDTB = new DiscussionTopicBean(newTopic, forum, forumManager, rubricsService, userTimeService);
    loadTopicDataInTopicBean(newTopic, thisDTB);
    if("true".equalsIgnoreCase(ServerConfigurationService.getString("mc.defaultLongDescription")))
    {
    	thisDTB.setReadFullDesciption(true);
    }

    setNewTopicBeanAssign(selectedForum, thisDTB);
    return selectedTopic;
  }

  	private DBMembershipItem getMembershipItemCopy(DBMembershipItem itemToCopy) {
		log.debug("getMembershipItemCopy()");
		DBMembershipItem newItem = permissionLevelManager.createDBMembershipItem(itemToCopy.getName(), itemToCopy.getPermissionLevelName(),
				itemToCopy.getType());
		PermissionLevel oldPermLevel = itemToCopy.getPermissionLevel();
		if (newItem.getPermissionLevelName().equals(PermissionLevelManager.PERMISSION_LEVEL_NAME_CUSTOM)) {
			PermissionsMask mask = new PermissionsMask();
			List customPermList = permissionLevelManager.getCustomPermissions();
			for (int c = 0; c < customPermList.size(); c++) {
				String customPermName = (String) customPermList.get(c);
				Boolean hasPermission = permissionLevelManager.getCustomPermissionByName(customPermName, oldPermLevel);
				mask.put(customPermName, hasPermission);
			}

			PermissionLevel level = permissionLevelManager.createPermissionLevel(newItem.getPermissionLevelName(), typeManager.getCustomLevelType(), mask);
			newItem.setPermissionLevel(level);
		}
		return newItem;
	}

	private DiscussionForumBean duplicateForum(Long originalForumId) {
		log.debug("DuplicateForum() FORUM-ID=" + originalForumId.toString());
	    forumClickCount = 0;
	    topicClickCount = 0;

	    setEditMode(true);
	    setPermissionMode(PERMISSION_MODE_FORUM);

		DiscussionForum oldForum = forumManager.getForumByIdWithTopicsAttachmentsAndMessages(Long.valueOf(originalForumId));

		DiscussionForum forum = forumManager.createForum();
		forum.setModerated(oldForum.getModerated());
		forum.setPostFirst(oldForum.getPostFirst());
		forum.setAutoMarkThreadsRead(oldForum.getAutoMarkThreadsRead()); // default to template setting
        String oldTitle =  selectedForum.getForum().getTitle();
		selectedForum = null;
		selectedForum = new DiscussionForumBean(forum, forumManager, userTimeService);
		loadForumDataInForumBean(forum, selectedForum);
		if("true".equalsIgnoreCase(ServerConfigurationService.getString("mc.defaultLongDescription")))
		{
			selectedForum.setReadFullDesciption(true);
		}
		String oldShortDescription = oldForum.getShortDescription();
		if (oldShortDescription == null) oldShortDescription = " ";
		forum.setShortDescription(oldShortDescription);
		String oldExtendedDescription = oldForum.getExtendedDescription();
		if (oldExtendedDescription == null) oldExtendedDescription = "";
		forum.setExtendedDescription(oldExtendedDescription);
		forum.setTitle(oldTitle);

      Set<DBMembershipItem> fromForumMembershipItems = uiPermissionsManager.getForumItemsSet(oldForum);
      if (fromForumMembershipItems != null) {
        for (DBMembershipItem fromForumMembershipItem : fromForumMembershipItems) {
          DBMembershipItem membershipItemCopy = getMembershipItemCopy(fromForumMembershipItem);
          DBMembershipItem savedMembershipItem = permissionLevelManager.saveDBMembershipItem(membershipItemCopy);
          forum.addMembershipItem(savedMembershipItem);
          ((DBMembershipItemImpl) savedMembershipItem).setForum(forum);
        }
      }

      List fromForumAttach = oldForum.getAttachments();
		if (fromForumAttach != null && !fromForumAttach.isEmpty()) {
			for (int topicAttach=0; topicAttach < fromForumAttach.size(); topicAttach++) {
				Attachment thisAttach = (Attachment)fromForumAttach.get(topicAttach);
				Attachment thisDFAttach = forumManager.createDuplicateDFAttachment(
						thisAttach.getAttachmentId(),
						thisAttach.getAttachmentName());
				forum.addAttachment(thisDFAttach);
			}
		}

    String fromAssignmentTitle = oldForum.getDefaultAssignName();
    forum.setDefaultAssignName(fromAssignmentTitle);

		if(oldForum.getAvailabilityRestricted()) {
			forum.setAvailabilityRestricted(true);
			forum.setOpenDate(oldForum.getOpenDate());
			forum.setCloseDate(oldForum.getCloseDate());
			forum.setLockedAfterClosed(oldForum.getLockedAfterClosed());
		}

		forum = saveForumSettings(oldForum.getDraft());

		forum = forumManager.getForumById(forum.getId());
		List attachList = forum.getAttachments();
		if (attachList != null)
		{
		  for (int i = 0; i < attachList.size(); i++)
		  {
			attachments.add(new DecoratedAttachment((Attachment)attachList.get(i)));
		  }
		}

		selectedForum = new DiscussionForumBean(forum, forumManager, userTimeService);
		loadForumDataInForumBean(forum, selectedForum);
		if("true".equalsIgnoreCase(ServerConfigurationService.getString("mc.defaultLongDescription")))
		{
			selectedForum.setReadFullDesciption(true);
		}

		setForumBeanAssign();
		setFromMainOrForumOrTopic();

		List oldTopics = oldForum.getTopics();
		Iterator itr = oldTopics.iterator();
		while (itr.hasNext()) {
			Topic oldTopic = (Topic) itr.next();
			Long oldTopicId = oldTopic.getId();
			duplicateTopic(oldTopicId, forum, true);
		}
		selectedForum = new DiscussionForumBean(forum, forumManager, userTimeService);
		loadForumDataInForumBean(forum, selectedForum);
		return selectedForum;
	}
	
	/**
	 * 
	 * @param msg
	 * @return the head of the discussion thread for the given msg
	 */
	private DiscussionMessageBean getThreadHeadForMessage(Message msg) {
		Message inReplyTo = msg.getInReplyTo();
		while (inReplyTo != null)
		{
			// Have to use getMessageByIdWithAttachments, or we'll get LazyInitializationExceptions
			msg = messageManager.getMessageByIdWithAttachments(inReplyTo.getId());
			inReplyTo = msg.getInReplyTo();
		}
		return new DiscussionMessageBean(msg, messageManager);
	}

	public String getEditorRows() {
		return ServerConfigurationService.getString("msgcntr.editor.rows", "22");
	}

	public List getSiteGroups() {
        if (siteGroups == null || siteGroups.isEmpty()) {
            siteGroups = new ArrayList();
            try {
                Site currentSite = siteService.getSite(toolManager.getCurrentPlacement().getContext());
                Collection groups = currentSite.getGroups();
                groups = sortGroups(groups);
                for (Iterator groupIterator = groups.iterator(); groupIterator.hasNext();) {
                    Group currentGroup = (Group) groupIterator.next();
                    siteGroups.add(new SiteGroupBean(currentGroup, false, false));
                }
            } catch (IdUnusedException e) {
                log.error(e.getMessage(), e);
            }
        }
        return siteGroups;
    }
	
	private List getSiteRolesNames() {
		ArrayList siteRolesNames = new ArrayList();
		
		AuthzGroup realm;
		try {
			realm = authzGroupService.getAuthzGroup(getContextSiteId());

			Set roles1 = realm.getRoles();

			if (roles1 != null && roles1.size() > 0) {
				List rolesList = sortRoles(roles1);

				Iterator roleIter = rolesList.iterator();
				while (roleIter.hasNext()) {
					Role role = (Role) roleIter.next();
					if (role != null) {
						siteRolesNames.add(role.getId());
					}
				}
			}
		} catch (GroupNotDefinedException e) {
			log.error(e.getMessage(), e);
		}
		return siteRolesNames;
	}

    /**
	 * 
	 * @param draft
	 * @return error status (no groups selected)
	 */
    private boolean saveForumsForGroups(boolean draft) {
        log.debug("saveForumsForGroups()");

		boolean isCorrect = checkMultiGradebook(true);
		String oldGradeAssign = selectedForum.getGradeAssign();

		if (!isCorrect) {
			return false;
		}

        if (siteGroups == null || siteGroups.isEmpty()) {
            setErrorMessage(getResourceBundleString(NO_GROUP_SELECTED, new Object[]{getResourceBundleString("cdfm_discussions")}));
            return false;
        }

        DiscussionForumBean forumTemplate = selectedForum;
        ArrayList attachmentsTemplate = attachments;
        
        // sakai.properties settings
        ArrayList rolesNone = (ArrayList) getAutoRolesNone();
        String groupLevel = getAutoGroupsPermConfig();

        Collections.reverse(siteGroups);

        ArrayList groupsNone = new ArrayList();
        for (SiteGroupBean currentGroup : siteGroups){
            if (currentGroup.getCreateForumForGroup()==true) {
                groupsNone.add(currentGroup.getGroup().getTitle());
            }
        }

        boolean groupSelected = false;
        for (SiteGroupBean currentGroup : siteGroups){
            if (currentGroup.getCreateForumForGroup()==true) {
                groupSelected = true;
                DiscussionForum forum = forumManager.createForum();
                if (ServerConfigurationService.getBoolean("msgcntr.restricted.group.perms", false)) {
                    forum.setRestrictPermissionsForGroups(forumTemplate.getForum().getRestrictPermissionsForGroups());
                }
                forum.setModerated(forumTemplate.getForum().getModerated());
                forum.setAutoMarkThreadsRead(forumTemplate.getForum().getAutoMarkThreadsRead());
                forum.setPostFirst(forumTemplate.getForum().getPostFirst());
                selectedForum = new DiscussionForumBean(forum, forumManager, userTimeService);
                loadForumDataInForumBean(forum, selectedForum);
                setNewForumBeanAssign();
                DiscussionForum thisForum = selectedForum.getForum();
                thisForum.setTitle(forumTemplate.getForum().getTitle() + " - " + currentGroup.getGroup().getTitle());
                thisForum.setShortDescription(forumTemplate.getForum().getShortDescription());
                thisForum.setExtendedDescription(forumTemplate.getForum().getExtendedDescription());
                thisForum.setLocked(forumTemplate.getForum().getLocked());
                thisForum.setAvailabilityRestricted(forumTemplate.getForum().getAvailabilityRestricted());
                thisForum.setOpenDate(forumTemplate.getForum().getOpenDate());
                thisForum.setCloseDate(forumTemplate.getForum().getCloseDate());

				if (isGradebookGroupEnabled()) {
					thisForum.setDefaultAssignName(getNewAssignName(currentGroup, oldGradeAssign));
				}

                // Attachments
                attachments.clear();
                for (Iterator attachmentIterator = attachmentsTemplate.iterator(); attachmentIterator.hasNext();) {
                    DecoratedAttachment currentAttachment = (DecoratedAttachment) attachmentIterator.next();
                    Attachment thisDFAttach = forumManager.createDFAttachment(
                            currentAttachment.getAttachment().getAttachmentId(),
                            currentAttachment.getAttachment().getAttachmentName());
                    attachments.add(new DecoratedAttachment(thisDFAttach));
                }

                // Permissions
                for (PermissionBean permBean : permissions) {
                    if (rolesNone.contains(permBean.getName())) {
                        permBean.setSelectedLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_NONE);
                    }
                    // Permissions will be remembered across forum loops, so we must reset marked groups to none in every loop
                    if (groupsNone.contains(permBean.getName())) {
                        permBean.setSelectedLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_NONE); 
                    }
                    if (permBean.getName().equals(currentGroup.getGroup().getTitle())) {
                        permBean.setSelectedLevel(groupLevel);
                    }
                }
                saveForumSettings(draft);
            }
        }

        if (!groupSelected) {
            setErrorMessage(getResourceBundleString(NO_GROUP_SELECTED, new Object[]{getResourceBundleString("cdfm_discussions")}));
            Collections.reverse(siteGroups);
            return false;
        }
		siteGroups.clear();
		selectedForum.getForum().setRestrictPermissionsForGroups(false);
        return true;

	}

    /**
	 * 
	 * @param draft
	 * @return error status (no groups selected)
	 */
    private boolean saveTopicsForGroups(boolean draft) {
        log.debug("saveTopicsForGroup()");

		boolean isCorrect = checkMultiGradebook(false);
		String oldGradeAssign = selectedTopic.getGradeAssign();

		if (!isCorrect) {
			return false;
		}

        if (siteGroups == null || siteGroups.isEmpty()) {
            setErrorMessage(getResourceBundleString(NO_GROUP_SELECTED, new Object[]{getResourceBundleString("topics")}));
            return false;
        }
        DiscussionTopicBean topicTempate = selectedTopic;
        ArrayList attachmentsTemplate = attachments;
        
        // sakai.properties settings
        ArrayList rolesNone = (ArrayList) getAutoRolesNone();
        String groupLevel = getAutoGroupsPermConfig();
        
		Collections.reverse(siteGroups);
        
        ArrayList groupsNone = new ArrayList();
        for (SiteGroupBean currentGroup : siteGroups){
            if (currentGroup.getCreateTopicForGroup()==true) {
                groupsNone.add(currentGroup.getGroup().getTitle());
            }
        }
        
        boolean groupSelected = false;
        for (SiteGroupBean currentGroup : siteGroups){
            if (currentGroup.getCreateTopicForGroup()==true) {
                groupSelected = true;
                selectedTopic = createTopic(topicTempate.getTopic().getBaseForum().getId());
                selectedTopic.setGradeAssign(topicTempate.getGradeAssign());
                DiscussionTopic thisTopic = selectedTopic.getTopic();
                if (ServerConfigurationService.getBoolean("msgcntr.restricted.group.perms", false)) {
                   thisTopic.setRestrictPermissionsForGroups(topicTempate.getTopic().getRestrictPermissionsForGroups());
                }
                thisTopic.setTitle(topicTempate.getTopic().getTitle() + " - " + currentGroup.getGroup().getTitle());
                thisTopic.setShortDescription(topicTempate.getTopic().getShortDescription());
                thisTopic.setExtendedDescription(topicTempate.getTopic().getExtendedDescription());
                thisTopic.setLocked(topicTempate.getTopic().getLocked());
                thisTopic.setModerated(topicTempate.getTopic().getModerated());
                thisTopic.setPostFirst(topicTempate.getTopic().getPostFirst());
                thisTopic.setPostAnonymous(topicTempate.getTopic().getPostAnonymous());
                thisTopic.setRevealIDsToRoles(topicTempate.getTopic().getRevealIDsToRoles());
                thisTopic.setAvailabilityRestricted(topicTempate.getTopic().getAvailabilityRestricted());
                thisTopic.setOpenDate(topicTempate.getTopic().getOpenDate());
                thisTopic.setCloseDate(topicTempate.getTopic().getCloseDate());
                thisTopic.setAutoMarkThreadsRead(topicTempate.getTopic().getAutoMarkThreadsRead());
                thisTopic.setGradebookAssignment(topicTempate.getTopic().getGradebookAssignment());

				if (isGradebookGroupEnabled()) {
					thisTopic.setDefaultAssignName(getNewAssignName(currentGroup, oldGradeAssign));
				}

                // Attachments
                attachments.clear();
                for (Iterator attachmentIterator = attachmentsTemplate.iterator(); attachmentIterator.hasNext();) {
                    DecoratedAttachment currentAttachment = (DecoratedAttachment) attachmentIterator.next();
                    Attachment thisDFAttach = forumManager.createDFAttachment(
                            currentAttachment.getAttachment().getAttachmentId(),
                            currentAttachment.getAttachment().getAttachmentName());
                    attachments.add(new DecoratedAttachment(thisDFAttach));
                }

                // Permissions
                for (PermissionBean permBean : permissions) {
                    if (rolesNone.contains(permBean.getName())) {
                        permBean.setSelectedLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_NONE);
                    }
                    // Permissions will be remembered across topic loops, so we must reset marked groups to none in every loop
                    if (groupsNone.contains(permBean.getName())) {
                        permBean.setSelectedLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_NONE); 
                    }
                    if (permBean.getName().equals(currentGroup.getGroup().getTitle())) {
                        permBean.setSelectedLevel(groupLevel);
                    }
                }
                saveTopicSettings(draft);
            }
        }
        if (!groupSelected) {
            setErrorMessage(getResourceBundleString(NO_GROUP_SELECTED, new Object[]{getResourceBundleString("topics")}));
            Collections.reverse(siteGroups);
            return false;
        }
        siteGroups.clear();
        selectedTopic.getTopic().setRestrictPermissionsForGroups(false);
        return true;
    }

	private String getNewAssignName(SiteGroupBean currentGroup, String gradeAssign) {
		String topicAssignName = "";

		String groupId = currentGroup.getGroup().getId();
		List<String> gbItemList = Arrays.asList(gradeAssign.split(","));
		GradingService gradingService = getGradingService();

		for (String gbItem : gbItemList) {
			String gbUid = gradingService.getGradebookUidByAssignmentById(toolManager.getCurrentPlacement().getContext(), Long.parseLong(gbItem));

			if (gbUid.equals(groupId)) {
				if (StringUtils.isBlank(topicAssignName)) {
					topicAssignName += gbItem;
				} else {
					topicAssignName += ("," + gbItem);
				}
			}
		}

		return topicAssignName;
	}

	/**
	 * 
	 * @return list of role titles appropriate to this site which should be set to None when autocreating topics
	 */
	private List getAutoRolesNone() {
		ArrayList autoRolesNone = new ArrayList();
		ArrayList siteRolesList = (ArrayList) getSiteRolesNames();
		String[] rolesNone = ServerConfigurationService.getStrings("msgcntr.rolesnone");
		if (rolesNone != null && rolesNone.length > 0) {
			for (String role : rolesNone) {
				if (siteRolesList.contains(role.trim())) autoRolesNone.add(role.trim());
			}
		}
		return autoRolesNone;
	}
	
    public String getAutoRolesNoneDesc() {
        ArrayList autoRolesNone = (ArrayList) getAutoRolesNone();
		if (autoRolesNone.size() > 0) {
			StringBuffer roles = new StringBuffer();
			Iterator iter = autoRolesNone.iterator();
			while (iter.hasNext()) {
				roles.append(iter.next());
				if (iter.hasNext()) roles.append("/");
			}
			return getResourceBundleString(AUTOCREATE_TOPICS_ROLES_DESCRIPTION , new Object[] {roles}) ;
		} else {
			return "";
		}
    }
    
    public String getAutoGroupsPermConfig() {
        String groupLevel = ServerConfigurationService.getString("msgcntr.groupsdefaultlevel", "Contributor");
        return groupLevel;
    }
    
	public String getAutoGroupsDesc() {
		String level = getAutoGroupsPermConfig();
		return getResourceBundleString(AUTOCREATE_TOPICS_GROUPS_DESCRIPTION, new Object[]{getResourceBundleString("perm_level_" + level.replaceAll(" ", "_").toLowerCase())});
	}

    public boolean getHasTopicAccessPrivileges(String topicIdStr){
        String userId = getUserId();
        long topicId = -1;
        try{
            topicId = Long.parseLong(topicIdStr);
        }catch (Exception e) {
        }
        if(topicId == -1 || userId == null){
            return false;
        }
        boolean hasOverridingPermissions = false;
        
        if(securityService.isSuperUser()){

            return true;
        }

        DiscussionTopic topic = forumManager.getTopicById(topicId);
        if(topic == null){
            return false;
        }
        
        DiscussionForum forum = forumManager.getForumById(topic.getBaseForum().getId());
        if(forum == null){
            return false;
        }
        
        Area currentArea = forumManager.getDiscussionForumArea();
        Area forumArea = forum.getArea();
        if(forumArea == null || currentArea == null || !forumArea.getId().equals(currentArea.getId()) ){
            return false;
        }

        return isInstructor()
        		|| userId.equals(topic.getCreatedBy())
        		|| (!topic.getDraft() && !forum.getDraft()
        				&& topic.getAvailability() 
        				&& forum.getAvailability() 
        				&& currentArea.getAvailability());
    }
    
	public String getServerUrl() {
		return ServerConfigurationService.getServerUrl();
	}
	
	public boolean getShowProfileInfo() {
	    return showProfileInfo;
	}

	public boolean getShowThreadChanges() {
		return showThreadChanges;
	}

	public Locale getUserLocale(){
		return new ResourceLoader().getLocale();
	}
	
	public String getDefaultAvailabilityTime(){
		return ServerConfigurationService.getString("msgcntr.forums.defaultAvailabilityTime", "").toLowerCase();
	}
	
	// MSGCNTR-241 move threads
	public String processMoveMessage() {
		return MESSAGE_MOVE_THREADS;
	}

	public String getMoveThreadJSON() {
		List allItemsList = new ArrayList();

		Map<String, List<JSONObject>> topicMap = null;
		Map<String, List<JSONObject>> forumMap = null;
		List allforums = forumManager.getDiscussionForumsWithTopics(this.getSiteId());
		if (allforums != null) {
			Iterator iter = allforums.iterator();
			if (allforums == null || allforums.size() < 1) {
				return null;
			}
			topicMap = new HashMap<String, List<JSONObject>>();
			forumMap = new HashMap<String, List<JSONObject>>();
			topicMap.put("topics", new ArrayList<JSONObject>());
			forumMap.put("forums", new ArrayList<JSONObject>());
			while (iter.hasNext()) {
				DiscussionForum tmpforum = (DiscussionForum) iter.next();
				parseForums(tmpforum, forumMap);
				if (tmpforum != null) {
					for (Iterator itor = tmpforum.getTopicsSet().iterator(); itor.hasNext();) {
						DiscussionTopic topic = (DiscussionTopic) itor.next();
						if (tmpforum.getLocked() == null || tmpforum.getLocked().equals(Boolean.TRUE)) {
							// do nothing. Skip forums that are locked. topics in locked forums should not show in the dialog
						} else if (topic.getLocked() == null || topic.getLocked().equals(Boolean.TRUE)) {
							// do nothing, skip locked topics. do not show them in move thread dialog
						} else {
							parseTopics(topic, topicMap, tmpforum);
						}
					}
				}

			}
			allItemsList.add(topicMap);
			allItemsList.add(forumMap);
		}

		JsonConfig config = new JsonConfig();
		JSON json = JSONSerializer.toJSON(allItemsList);
		if (log.isDebugEnabled())
			log.debug("converted getTotalTopicsJSON to json : " + json.toString(4, 0));
		return json.toString(4, 0);
	}

	private void parseForums(DiscussionForum forum, Map<String, List<JSONObject>> forumMap) {
		Long forumId = forum.getId();
		String forumtitle = forum.getTitle();
		Long forumid = forum.getId();
		List<JSONObject> forumList = forumMap.get("forums");
		if (forumList == null) {
			forumList = new ArrayList<JSONObject>();
		}

		JSONObject forumJSON = new JSONObject();
		forumJSON.element("forumid", forumId).element("forumtitle", forumtitle);
		forumList.add(forumJSON);
	}

	private void parseTopics(DiscussionTopic topic, Map<String, List<JSONObject>> topicMap, DiscussionForum tmpforum) {
		Long topicId = topic.getId();
		String forumtitle = tmpforum.getTitle();
		Long forumid = tmpforum.getId();
		List<JSONObject> topiclist = topicMap.get("topics");
		if (topiclist == null) {
			topiclist = new ArrayList<JSONObject>();
		}
		String title = topic.getTitle();
		JSONObject topicJSON = new JSONObject();
		topicJSON.element("topicid", topic.getId()).element("topictitle", title).element("forumid", forumid)
		.element("forumtitle", forumtitle);
		topiclist.add(topicJSON);
	}

	public List getRequestParamArray(String paramPart) {
		// FacesContext context = FacesContext.getCurrentInstance();
		// Map requestParams = context.getExternalContext().getRequestParameterMap();

		HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		Map requestParams = req.getParameterMap();
		String[] result = (String[]) requestParams.get(paramPart);
		return Arrays.asList(result);
	}

	public String processMoveThread() {
		Long sourceTopicId = this.selectedTopic.getTopic().getId();
		if (log.isDebugEnabled()) log.debug("Calling processMoveThread source topic is " + sourceTopicId);
		List checkedThreads = getRequestParamArray("moveCheckbox");
		List destTopicList = getRequestParamArray("selectedTopicid");

		String desttopicIdstr = null;

		if (destTopicList.size() != 1) {
			// do nothing, there should be one and only one destination.
			return gotoMain();
		} else {
			desttopicIdstr = (String) destTopicList.get(0);
			if ("0".equals(desttopicIdstr)){
				setErrorMessage(getResourceBundleString(NOT_SELECTED_TOPIC));
				return gotoMain();
			}
		}
		if (log.isDebugEnabled()) log.debug("Calling processMoveThread dest topic is " + desttopicIdstr);

		List checkbox_reminder = getRequestParamArray("moveReminder");
		boolean checkReminder = false;
		if (checkbox_reminder.size() != 1) {
			// do nothing, there should be one and only one destination.
			return gotoMain();
		} else {
			checkReminder = Boolean.parseBoolean((String) checkbox_reminder.get(0));
			// reminderVal = Boolean.parseBoolean(checkReminder);
		}

		if (log.isDebugEnabled()) log.debug("Calling processMoveThread checkReminder is " + checkReminder);

		Long desttopicId = Long.parseLong(desttopicIdstr);
		DiscussionTopic desttopic = forumManager.getTopicById(desttopicId);
		// now update topic id in mfr_message_t table, including all childrens (direct and indirect),
		// For each move, also add a row to the mfr_move_history_t table.

		Message mes = null;
		Iterator mesiter = checkedThreads.iterator();
		if (log.isDebugEnabled()) log.debug("processMoveThread checkedThreads size = " + checkedThreads.size());
		while (mesiter.hasNext()) {
			Long messageId = new Long((String) mesiter.next());
			mes = messageManager.getMessageById(messageId);
			if (log.isDebugEnabled()) log.debug("processMoveThread messageId = " + mes.getId());
			if (log.isDebugEnabled()) log.debug("processMoveThread message title = " + mes.getTitle());
			mes.setTopic(desttopic);
			mes = messageManager.saveOrUpdateMessage(mes);

			// mfr_move_history_t stores only records that are used to display reminder links. Not all moves are recorded in this
			// table.
			messageManager.saveMessageMoveHistory(mes.getId(), desttopicId, sourceTopicId, checkReminder);

			String eventmsg = "Moving message " + mes.getId() + " from topic " + sourceTopicId + " to topic " + desttopicId;
			eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_MOVE_THREAD, eventmsg, true));

			List childrenMsg = new ArrayList(); // will store a list of child messages
			messageManager.getChildMsgs(messageId, childrenMsg);
			if (log.isDebugEnabled()) log.debug("processMoveThread childrenMsg for  " + messageId + "   size = " + childrenMsg.size());
			Iterator childiter = childrenMsg.iterator();

			// update topic id for each child msg.
			while (childiter.hasNext()) {
				Message childMsg = (Message) childiter.next();
				if (log.isDebugEnabled()) log.debug("processMoveThread messageId = " + childMsg.getId());
				if (log.isDebugEnabled()) log.debug("processMoveThread message title = " + childMsg.getTitle());
				childMsg.setTopic(desttopic);
				childMsg = messageManager.saveOrUpdateMessage(childMsg);
				messageManager.saveMessageMoveHistory(childMsg.getId(), desttopicId, sourceTopicId, checkReminder);
				eventmsg = "Moving message " + childMsg.getId() + " from topic " + sourceTopicId + " to topic " + desttopicId;
				eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_MOVE_THREAD, eventmsg, true));
			}
		}

		setSelectedForumForCurrentTopic(desttopic);
		selectedTopic = getDecoratedTopic(desttopic);
		return ALL_MESSAGES;
	}

	/**
	 * Determine if we have been passed a parameter that contains a given string, return ArrayList of the corresponding values,
	 * else return empty list.
	 */
	public static ArrayList getRequestParamArrayValueLike(String paramPart) {
		FacesContext context = FacesContext.getCurrentInstance();
		Map requestParams = context.getExternalContext().getRequestParameterMap();
		ArrayList list = new ArrayList();

		for (Iterator it = requestParams.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			String currKey = (String) entry.getKey();
			int location = currKey.indexOf(paramPart);
			if (location > -1) {
				list.add((String) entry.getValue());
			}
		}
		return list;
	}

	public String getTotalAssignToListJSON() {
		if (this.courseMemberMap == null) {
			this.courseMemberMap = membershipManager.getFilteredCourseMembers(true, null);
		}
		List members = membershipManager.convertMemberMapToList(courseMemberMap);
		List jsonList = transformItemList(members);
		JsonConfig config = new JsonConfig();
		JSON json = JSONSerializer.toJSON(jsonList);
		if (log.isDebugEnabled()) log.debug(" finished getTotalAssignToListJSON");
		return json.toString(4, 0);
	}

	private List transformItemList(List members) {
		Map<String, List<JSONObject>> rolesMap = new HashMap<String, List<JSONObject>>(1);
		rolesMap.put("roles", new ArrayList<JSONObject>(1));

		Map<String, List<JSONObject>> groupsMap = new HashMap<String, List<JSONObject>>(1);
		groupsMap.put("groups", new ArrayList<JSONObject>(1));

		Map<String, List<JSONObject>> usersMap = new HashMap<String, List<JSONObject>>(1);
		usersMap.put("users", new ArrayList<JSONObject>());

		for (Iterator iterator = members.iterator(); iterator.hasNext();) {
			MembershipItem item = (MembershipItem) iterator.next();
			if (MembershipItem.TYPE_ROLE == item.getType()) {
				parseRoles(item, rolesMap);
			} else if (MembershipItem.TYPE_GROUP == item.getType()) {
				parseGroups(item, groupsMap);
			} else if (MembershipItem.TYPE_USER == item.getType()) {
				continue;
			} else {
                if (log.isDebugEnabled()) {
                    log.debug("Could not determine type of MembershipItem" + item);
                }
			}
		}
		// now that roles and groups are parsed, walk users, adding them
		// to users map and their ids to the groups and/or roles the belong to
		for (Iterator iterator = members.iterator(); iterator.hasNext();) {
			MembershipItem item = (MembershipItem) iterator.next();
			if (MembershipItem.TYPE_USER == item.getType()) {
				parseUsers(item, groupsMap, rolesMap, usersMap);
                if (log.isDebugEnabled()) {
                    log.debug("parseUsers....TYPE_USER  itemtype =  " + item.getType());
                }
			} else {
                if (log.isDebugEnabled()) {
                    log.debug("parseUsers...Could not determine type of MembershipItem" + item.getType());
                }
			}
		}
		List allItemsList = new ArrayList();
		allItemsList.add(rolesMap);

		// we only need the userIds to setup the individual user data
		// so remove it before delivering to page
		List<JSONObject> groupsList = groupsMap.get("groups");
		for (JSONObject groupJSON : groupsList) {
			groupJSON.remove("userIds");
		}
		allItemsList.add(groupsMap);
		allItemsList.add(usersMap);
		return allItemsList;
	}

	private void parseRoles(MembershipItem item, Map<String, List<JSONObject>> rolesMap) {
		List<JSONObject> rolesList = rolesMap.get("roles");
		if (rolesList == null) {
			rolesList = new ArrayList<JSONObject>();
		}
		Role role = item.getRole();
		List<String> userIds = new ArrayList<String>();
		JSONObject rolesJSON = new JSONObject();
		rolesJSON.element("membershipItemId", item.getId()).element("roleId", role.getId()).element("description", role.getDescription())
				.element("userIds", userIds);
		rolesList.add(rolesJSON);
	}

	private void parseGroups(MembershipItem item, Map<String, List<JSONObject>> groupsMap) {
		Group group = item.getGroup();
		List<JSONObject> groupsList = groupsMap.get("groups");
		if (groupsList == null) {
			groupsList = new ArrayList<JSONObject>();
		}
		Set<Member> groupMembers = (Set<Member>) group.getMembers();
		List<String> userIds = new ArrayList<String>(groupMembers.size());
		for (Member member : groupMembers) {
			userIds.add(member.getUserId());
		}
		JSONObject groupJSON = new JSONObject().element("membershipItemId", item.getId()).element("groupId", group.getId())
				.element("title", group.getTitle()).element("userIds", userIds);
		groupsList.add(groupJSON);
	}

	private void parseUsers(MembershipItem item, Map<String, List<JSONObject>> groupsMap, Map<String, List<JSONObject>> rolesMap,
			Map<String, List<JSONObject>> usersMap) {
		List<JSONObject> usersList = usersMap.get("users");
		if (usersList == null) {
			usersList = new ArrayList<JSONObject>();
		}

		JSONObject jsonMembershipItem = new JSONObject();
		jsonMembershipItem.element("membershipItemId", item.getId()).element("roleId", item.getRole().getId())
				.element("userDisplayName", item.getUser().getDisplayName()).element("eid", item.getUser().getEid());
		usersList.add(jsonMembershipItem);

		JSONArray memberGroupsArray = new JSONArray();
		List<JSONObject> groupsList = groupsMap.get("groups");
		for (JSONObject jsonGroup : groupsList) {
			List<String> userIds = (List<String>) jsonGroup.get("userIds");
			if (userIds.contains(item.getUser().getId())) {
				JSONObject memberGroupJSON = new JSONObject();
				memberGroupJSON.element("groupId", jsonGroup.get("groupId"));
				memberGroupJSON.element("title", jsonGroup.get("title"));
				memberGroupsArray.add(memberGroupJSON);
			}
		}
		jsonMembershipItem.element("groups", memberGroupsArray);
	}

	private List<ForumRankBean> rankBeanList = new ArrayList<ForumRankBean>();

	public void setRankBeanList(List ranklist) {
		List<ForumRankBean> alist = new ArrayList();
		if (ranklist != null) {
			Iterator childiter = ranklist.iterator();
			// update topic id for each child msg.
			while (childiter.hasNext()) {
				Rank thisrank = (Rank) childiter.next();
				ForumRankBean rankBean = new ForumRankBean(thisrank);
				alist.add(rankBean);
			}
		}
		this.rankBeanList.clear();
		this.rankBeanList.addAll(alist);
	}

	public boolean isRanksEnabled()
	{
		return ServerConfigurationService.getBoolean("msgcntr.forums.ranks.enable", false);
	}

	private static final String INSUFFICIENT_PRIVILEGES_TO_EDIT_RANKS = "cdfm_insufficient_privileges_ranks";
	private static final String VIEW_RANK = "dfViewAllRanks";
	private static final String ADD_RANK = "dfAddRank";
	private static final String EDIT_RANK = "dfEditRank";
	private static final String CONFIRM_REMOVE_RANK = "dfConfirmRemoveRanks";
	private static final String NOT_SELECTED_TOPIC = "cdfm_not_selected_topic";

	private boolean just_created = false;
	private boolean imageDeletePending = false;

	public void saveRank(Rank newRank) {
		if ((forumRankBean != null) && (newRank != null)) {
			if (log.isDebugEnabled()) log.debug("saveRank:   forumRankBean !=null) && (newRank!=null");

			String selectedRankType = this.forumRankBean.getType();
			if (log.isDebugEnabled()) log.debug("saveRank: selectedRankType () = " + selectedRankType);

			if (Rank.RANK_TYPE_INDIVIDUAL.equalsIgnoreCase(selectedRankType)) {
				if (log.isDebugEnabled()) log.debug("saveRank:   RANK_TYPE_INDIVIDUAL");

				newRank.setType(Rank.RANK_TYPE_INDIVIDUAL);
				Set<String> assignToIds = constructAssignToIds();
				if (log.isDebugEnabled()) log.debug("user_id = " + assignToIds);
				newRank.setAssignToIds(assignToIds);
				newRank.setMinPosts(0);
				rankManager.saveRank(newRank);
			} else if (Rank.RANK_TYPE_POST_COUNT.equalsIgnoreCase(selectedRankType)) { // by # of post
				if (log.isDebugEnabled()) log.debug("saveRank:  RANK_TYPE_POST_COUNT ");

				newRank.setType(Rank.RANK_TYPE_POST_COUNT);
				rankManager.saveRank(newRank);
			} else {
				log.warn("ForumTool.saveRank(): should not come here.  The type is undefined.");
			}
			this.setSelectedIndividualMemberItemIds(null);
		} else {
			if (log.isDebugEnabled()) log.debug("ForumTool.saveRank(): Can not save because forumRankBean is null");
			// should not come here
		}
	}

	public void saveRankImages(Rank rank) {
		if (just_created) {
			if (attachment != null) {
				rankManager.addImageAttachToRank(rank, attachment);
				just_created = false;
			}
		}
	}

	public String processDeleteRankImage() {
		setImageDeletePending(true);
		if (log.isDebugEnabled()) log.debug("ForumTool.processDeleteRankImage(): ranktype = " + this.forumRankBean.getType());
		return EDIT_RANK;
	}

	public void finishDeleteRankImage() {
		Rank currRank = this.forumRankBean.getRank();
		RankImage imageAttach = currRank.getRankImage();
		if (imageAttach != null) {
			rankManager.removeImageAttachToRank(currRank, imageAttach);
		}

		// refresh the Edit rank page
		Rank newRank = rankManager.getRankById(currRank.getId());
		this.forumRankBean.setRank(newRank);
		setImageDeletePending(false);
	}

	// JSF for checkboxes for deleteting ranks
	private String[] deleteRanks =
		{}; // for ranks to delete
	private List checkedRanks;

	public String processActionViewRanks() {
		if (log.isDebugEnabled()) log.debug("processActionViewRanks()");
		if (!isInstructor()) {
			setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_TO_EDIT_RANKS));
			return gotoMain();
		}
		List<Rank> ranklist = new ArrayList();
		ranklist = rankManager.getRankList(getSiteId());
		constructAssignedToDisplay(ranklist);
		setRankBeanList(ranklist);
		return VIEW_RANK;
	}

	public String processActionAddRank() {
		if (log.isDebugEnabled()) log.debug("processActionAddRank()");
		this.setForumRankBean(new ForumRankBean());
        this.courseMemberMap = membershipManager.getFilteredCourseMembers(true, null);
		return ADD_RANK;
	}

	public static final String ASSIGNEDTO_DELIMITER = ";";

	public String processActionEditRank() {
		if (log.isDebugEnabled()) log.debug("processActionEditRank()");
		String rankId = getExternalParameterByKey("rankId");
		Rank thisrank = rankManager.getRankById(new Long(rankId));
		ForumRankBean rankBean = new ForumRankBean(thisrank);
		this.setForumRankBean(rankBean);

		if (Rank.RANK_TYPE_INDIVIDUAL.equalsIgnoreCase(rankBean.getType())) {
			// get selected individuals for editing
			Set<String> assignToIds = thisrank.getAssignToIds();
			if (assignToIds == null || assignToIds.isEmpty()) {
				return VIEW_RANK; // not going anywhere. AssignTo should have at least 1 user.
			}
			StringBuffer memberitemidlist = new StringBuffer();
            this.courseMemberMap = membershipManager.getFilteredCourseMembers(true, null);
			List members = membershipManager.convertMemberMapToList(courseMemberMap);
			Map<String, MembershipItem> membersKeyOnUserId = new HashMap();

			for (Iterator i = members.iterator(); i.hasNext();) {
				MembershipItem item = (MembershipItem) i.next();
				User itemUser = item.getUser();
				if (itemUser != null) {
					membersKeyOnUserId.put(itemUser.getId(), item);
				} else {
					// okay ,not a User membershipItem, could be Group, or Role...
				}
			}

			for (String userId: assignToIds) {
				if (membersKeyOnUserId.containsKey(userId)) {
					memberitemidlist.append(membersKeyOnUserId.get(userId).getId());
					memberitemidlist.append(AGGREGATE_DELIMITER);
				}
			}

			if (log.isDebugEnabled()) log.debug("processActionEditRank() memberitemidlist.toString = " + memberitemidlist.toString());
			this.setSelectedIndividualMemberItemIds(memberitemidlist.toString());
		}
		return EDIT_RANK;
	}

	public String processActionUpdateRank() {
		if (log.isDebugEnabled()) log.debug("ForumTool.processActionUpdateRank()");
		if (this.isImageDeletePending()) {
			finishDeleteRankImage();
		}

        // if processUpdate sets imageTooLarge, then stop
        if (imageTooLarge) {
            imageTooLarge = false; // reset imageTooLarge for new Edit
			return EDIT_RANK;
		}

		Rank newRank = this.forumRankBean.getRank(); // rankManager.getRankById(this.forumRankBean.getRank().getId());
		newRank.setTitle(forumRankBean.getTitle());
		newRank.setMinPosts(forumRankBean.getMinPosts());
		saveRank(newRank);
		saveRankImages(newRank);
		return processActionViewRanks();
	}

	public String processActionSaveRank() {
		if (log.isDebugEnabled()) log.debug("ForumTool.processActionSaveRank()");

		String filename = getExternalParameterByKey("addRank:add_attach.uploadId");
        // if processUpdate sets imageTooLarge, then stop
		if (imageTooLarge) {
		    imageTooLarge = false;          // reset imageTooLarge for new Add 
			return ADD_RANK;
		}

		Rank newRank = this.forumRankBean.getRank();
		newRank.setTitle(forumRankBean.getTitle());
		newRank.setMinPosts(forumRankBean.getMinPosts());
		saveRank(newRank);
		saveRankImages(newRank);
		return processActionViewRanks();
	}

	public String processActionDeleteRanks() {
		if (log.isDebugEnabled()) log.debug("ForumTool.processActionDeleteRank()");

		List ranklist = this.getCheckedRanks();
		Iterator iter = ranklist.iterator();
		while (iter.hasNext()) {
			Rank rank_to_delete = (Rank) iter.next();
			RankImage imageAttach = rank_to_delete.getRankImage();
			if (imageAttach != null) {
				rankManager.removeImageAttachToRank(rank_to_delete, imageAttach);
			}
			Rank rank2 = rankManager.getRankById(rank_to_delete.getId());
			rankManager.removeRank(rank2);
		}
		return processActionViewRanks();
	}

	public String processActionConfirmDeleteRanks() {
		if (log.isDebugEnabled()) log.debug("ForumTool.processActionConfirmDeleteRanks()");
		Long rankId = null;
		List selectedRanks = getRequestParamArrayValueLike("removeCheckbox");
		List ranklist = new ArrayList();
		Iterator iter = selectedRanks.iterator();
		while (iter.hasNext()) {
			rankId = new Long((String) iter.next());
			Rank rankchecked = rankManager.getRankById(rankId);
			ranklist.add(rankchecked);
		}

		this.setCheckedRanks(ranklist);
		return CONFIRM_REMOVE_RANK;
	}

	public String gotoViewRank() {
		setImageDeletePending(false);
		return VIEW_RANK;
	}

	// Code borrowed from Messages tool. Rank based on roles. Below code is to parse the roles selected from the dialog popup.
	private String aggregatedAssignToItemIds;
	private List selectedComposeToList = new ArrayList();
	public static final String AGGREGATE_DELIMITER = "&";
	private String selectedIndividualMemberItemIds;

	/**
	 * Copied from Messages Tool, new method to handle the new UI submission as we're now using a custom widget, not a select
	 * list, and we need to aggregate id's to parse into a List
	 */
	public void setAggregatedAssignToItemIds(String aggregatedids) {
		this.aggregatedAssignToItemIds = aggregatedids;
		this.selectedComposeToList = parseAggregatedAssignToItemIds();
	}

	private List parseAggregatedAssignToItemIds() {
		List<String> itemIdList = null;
		Set<String> itemIdSet = null;
		if (this.aggregatedAssignToItemIds == null || "".equals(this.aggregatedAssignToItemIds.trim())) {
			// make an empty list so regular error handling will work with new hidden form field data
			// aggregate_compose_to_item_ids
			itemIdList = new ArrayList(0);
			log.error("aggregatedAssignToItemIds is null or empty, check you post data param aggregate_compose_to_item_ids");
		} else if (this.aggregatedAssignToItemIds.contains(AGGREGATE_DELIMITER)) {
			StringTokenizer st = new StringTokenizer(this.aggregatedAssignToItemIds, AGGREGATE_DELIMITER, false);
			itemIdSet = new HashSet(st.countTokens());
			while (st.hasMoreTokens()) {
				itemIdSet.add(st.nextToken());
			}
			itemIdList = new ArrayList<String>(itemIdSet.size());
			itemIdList.addAll(itemIdSet);
		} else {
			itemIdList = new ArrayList(1);
			itemIdList.add(this.aggregatedAssignToItemIds);
		}

		return itemIdList;
	}

	public String getAggregatedAssignToItemIds() {
		return aggregatedAssignToItemIds;
	}

	public List getSelectedComposeToList() {
		return selectedComposeToList;
	}

	private void constructAssignedToDisplay(List<Rank> ranks) {
		courseMemberMap = membershipManager.getFilteredCourseMembers(true, null);
		Map<String, String> memberIdNameMap = new HashMap<String, String>();
		for (Object o: courseMemberMap.values()) {
			MembershipItem item = (MembershipItem) o;
			if (item.getUser() != null) {
				memberIdNameMap.put(item.getUser().getId(), item.getUser().getDisplayName());
			}
		}
		for (Rank rank: ranks) {
			List<String> assignToNames = new ArrayList<String>();
			for (String userId: rank.getAssignToIds()) {
				if (memberIdNameMap.get(userId) != null) {
					assignToNames.add(memberIdNameMap.get(userId));
				}
			}
			rank.setAssignToDisplay(StringUtils.join(assignToNames, ", "));
		}
	}

	private Set<String> constructAssignToIds() {
		Set<String> assignToIds = new HashSet<String>();
		for (String selectedComponseTo: (List<String>)selectedComposeToList) {
			MembershipItem item = (MembershipItem)courseMemberMap.get(selectedComponseTo);
			if (item != null) {
				assignToIds.add(item.getUser().getId());
			}
		}
		return assignToIds;
	}

	private boolean attachCaneled = false;
	private RankImage attachment = null;
    private boolean imageTooLarge = false;

	private boolean validateImageSize(FileItem item) {
		// check size
		long maxsize = new Long(ServerConfigurationService.getString("msgcntr.forum.rankimage.maxsize", "102400"));
		long imagesize = item.getSize();
		if (log.isDebugEnabled()) log.debug("validateImageSize(item)  imagesize = " + imagesize);

		if (imagesize > maxsize) {
			this.getForumRankBean().setImageSizeErr(true);
            imageTooLarge = true;
			return false;
		}
        this.getForumRankBean().setImageSizeErr(false);
		return true;
	}

	public String processUpload(ValueChangeEvent event) {
		if (log.isDebugEnabled()) log.debug("processUpload(ValueChangeEvent event) ");
		if (attachCaneled == false) {
			Object newValue = event.getNewValue();
            if (newValue instanceof String) {
                return "";
            }
            if (newValue == null) {
                return "";
            }
            FileItem item = (FileItem) event.getNewValue();
            if (!validateImageSize(item)) {
              return null;
            }
            try (InputStream inputStream = item.getInputStream()) {
                String fileName = item.getName();
                ResourcePropertiesEdit props = contentHostingService.newResourceProperties();
                String tempS = fileName;

                int lastSlash = tempS.lastIndexOf("/") > tempS.lastIndexOf("\\") ? tempS.lastIndexOf("/") : tempS.lastIndexOf("\\");
                if (lastSlash > 0) {
                    fileName = tempS.substring(lastSlash + 1);
                }
                props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, fileName);
                ContentResourceEdit thisAttach = contentHostingService.addAttachmentResource(fileName);
                thisAttach.setContentType(item.getContentType());
                thisAttach.setContent(inputStream);
                thisAttach.getPropertiesEdit().addAll(props);
                contentHostingService.commitResource(thisAttach);
                RankImage attachObj = rankManager.createRankImageAttachmentObject(thisAttach.getId(), fileName);
                attachment = attachObj;

            } catch (Exception e) {
                log.error(this + ".processUpload() in DiscussionForumTool", e);
            }
            just_created = true;
            return VIEW_RANK;
        }
		return null;
	}

	private Rank authorRank;

	public Rank getAuthorRank(String userEid) {
		// if both types of ranks exist for the same user, use the "Special rank assigned to selected site member(s)" type first.
		Rank currRank = null;
		if (isRanksEnabled())
		{
			currRank = findRankByUser(userEid);
			if (currRank == null) {
				int authorCount = messageManager.findAuthoredMessageCountForStudent(userEid);
				currRank = findRankByMinPost(authorCount);
			}
		}
		return currRank;
	}

	private Rank findRankByMinPost(int authorCount) {
		Rank returnRank = null;
		List sortedranks = rankManager.findRanksByContextIdOrderByMinPostDesc(getSiteId());
		if (sortedranks != null && !sortedranks.isEmpty()) {
			Rank currRank = (Rank) sortedranks.get(sortedranks.size() - 1);
			for (int i = 0; i < sortedranks.size(); i++) {
				currRank = (Rank) sortedranks.get(i);
				if (log.isDebugEnabled()) log.debug("... findRankByMinPost authorCount = " + authorCount);
				if (log.isDebugEnabled()) log.debug("... findRankByMinPost currRank.getMinPosts = " + currRank.getMinPosts());
				if (authorCount >= currRank.getMinPosts()) {
					returnRank = currRank;
					break;
				} else {
					// continue
				}
			}
		}
		return returnRank;
	}

	private Rank findRankByUser(String userEid) {
		Rank returnRank = null;
		List sortedranks = rankManager.findRanksByContextIdUserId(getSiteId(), userEid);
		if (sortedranks != null && !sortedranks.isEmpty()) {
			// if more than one result, pick the first one.
			returnRank = (Rank) sortedranks.get(0);
		}
		return returnRank;
	}

    public String getCurrentToolId(){
    	return toolManager.getCurrentPlacement().getId();
    }
    
    /**
     * This method uses hidden input fields to verify that the selected message, topic and forum is correct.
     * This is used to prevent the multiple tabs issue where these values can be wrong. However, we can't just
     * assume they are correct either. We must verify that the user truly has permission for these current fields.
     * This method will verify that the user has access to the requested action against the hidden inputs fields, and if so,
     * update the selected fields to complete the user's action and make the page load properly. 
     * 
     * @param methodCalled
     * @param formId
     * @param canReply
     * @param canCompose
     * @param canEdit
     * @param canDelete
     * @return
     */
    public boolean checkPermissionsForUser(String methodCalled, boolean canReply, boolean canCompose, boolean canEdit, boolean canDelete){
    	try{
    		boolean checkCurrentMessageId = canReply || canEdit || canDelete;
    		DiscussionMessageBean tmpSelectedMessage = selectedMessage;
    		DiscussionTopicBean tmpSelectedTopic = selectedTopic;
    		DiscussionForumBean tmpSelectedForum = selectedForum;
    		DiscussionMessageBean tmpSelectedThreadHead = selectedThreadHead;
    		String forumContextId = getSiteId();
    		//Check Message input field
    		if(checkCurrentMessageId){
    			try{	
    				String msgIdStr = getExternalParameterByKey(CURRENT_MESSAGE_ID);
    				long msgId = Long.parseLong(msgIdStr);
    				if(tmpSelectedMessage == null || tmpSelectedMessage.getMessage() == null 
    						|| (!tmpSelectedMessage.getMessage().getId().equals(msgId))){
    					Message threadMessage = messageManager.getMessageByIdWithAttachments(msgId);
    					tmpSelectedMessage = new DiscussionMessageBean(threadMessage, messageManager);
    					//selected message has changed, make sure we set the selected thread head
    					tmpSelectedThreadHead = new DiscussionMessageBean(tmpSelectedMessage.getMessage(), messageManager);
    				    //make sure we have the thread head of depth 0
    				    while(tmpSelectedThreadHead.getMessage().getInReplyTo() != null){
    				    	threadMessage = messageManager.getMessageByIdWithAttachments(tmpSelectedThreadHead.getMessage().getInReplyTo().getId());
    				    	tmpSelectedThreadHead = new DiscussionMessageBean(threadMessage, messageManager);
    				    }
    				}
    			}catch(Exception e){
    				log.error(e.getMessage(), e);
    			}
    		}
    		//Check Forum input field
    		try{
    			String forumIdStr = getExternalParameterByKey(CURRENT_FORUM_ID);
    			long forumId = Long.parseLong(forumIdStr);
    			if(tmpSelectedForum == null || tmpSelectedForum.getForum() == null 
    					|| (!tmpSelectedForum.getForum().getId().equals(forumId))){
    				DiscussionForum forum = forumManager.getForumById(forumId);
    				tmpSelectedForum = getDecoratedForum(forum);
    				//forum changed, so make sure you use that forum's site id:
    				forumContextId = forumManager.getContextForForumById(forum.getId());
    			}
    		}catch(Exception e){
    			log.error(e.getMessage(), e);
    		}

    		//Check Topic: input field
    		try{
    			String topicIdStr = getExternalParameterByKey(CURRENT_TOPIC_ID);
    			long topicId = Long.parseLong(topicIdStr);
    			if(tmpSelectedTopic == null || tmpSelectedTopic.getTopic() == null || tmpSelectedTopic.getTopic().getBaseForum() == null
    					|| (!tmpSelectedTopic.getTopic().getId().equals(topicId))){
    				//selected message doesn't match the current message input,
    				//verify user has access to parameter message and use that one

    				DiscussionTopic topicWithMsgs = (DiscussionTopic) forumManager.getTopicByIdWithMessages(topicId);
    				tmpSelectedTopic = getDecoratedTopic(topicWithMsgs);    			
    			}
    		}catch(Exception e){
    			log.error(e.getMessage(), e);
    		}
    		//verify everything is set properly
    		//Obviously this could be done in one huge if statement, but it's not as easy to ready and understand the logic,
    		//so I left it broken out

    		//is message set
    		if(checkCurrentMessageId && (tmpSelectedMessage == null || tmpSelectedMessage.getMessage() == null)){
    			log.info(methodCalled + ": can not check permissions against a null message. user: " + getUserId());
    			return false;
    		}
    		//is forum set
    		if(tmpSelectedForum == null || tmpSelectedForum.getForum() == null){
    			log.info(methodCalled + ": can not check permissions against a null forum. user: " + getUserId());
    			return false;
    		}
    		//is topic set
    		if(tmpSelectedTopic == null || tmpSelectedTopic.getTopic() == null){
    			log.info(methodCalled + ": can not check permissions against a null topic. user: " + getUserId());
    			return false;
    		}
    		//check topic belongs to the forum
    		if(!tmpSelectedForum.getForum().getId().equals(tmpSelectedTopic.getTopic().getBaseForum().getId())){
    			log.info(methodCalled + ": topic: " + tmpSelectedTopic.getTopic().getId() + " does not belong to the forum: " + tmpSelectedForum.getForum().getId() + ". user: " + getUserId());
    			return false;    				
    		}
    		//check message belongs to the topic
    		if(checkCurrentMessageId && !tmpSelectedMessage.getMessage().getTopic().getId().equals(tmpSelectedTopic.getTopic().getId())){
    			log.info(methodCalled + ": message: " + tmpSelectedMessage.getMessage().getId() + " does not belong to the topic: " + tmpSelectedTopic.getTopic().getId() + ".  user: " + getUserId());
    			return false;
    		}
    		//is topic locked?
    		if(tmpSelectedTopic.getTopic().getLocked()){
    			setErrorMessage(getResourceBundleString(TOPIC_LOCKED, new Object[]{tmpSelectedTopic.getTopic().getTitle()}));
    			log.info(methodCalled + ": Topic is locked: " + tmpSelectedTopic.getTopic().getTitle() + ".  user: " + getUserId());
    			return false;
    		}
    		//is forum locked?
    		if(tmpSelectedForum != null && tmpSelectedForum.getForum().getLocked()){
    			setErrorMessage(getResourceBundleString(FORUM_LOCKED, new Object[]{tmpSelectedForum.getForum().getTitle()}));
    			log.info(methodCalled + ": Forum is locked: " + tmpSelectedForum.getForum().getTitle() + ".  user: " + getUserId());
    			return false;
    		}
    		
    		//can the user reply to only existing messages (Check this first)
    		if (tmpSelectedMessage != null && (canReply && !uiPermissionsManager.isNewResponseToResponse(tmpSelectedTopic.getTopic(), tmpSelectedForum.getForum(), getUserId(), forumContextId))) {
    			setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEAGES_TO_POST_THREAD, new Object[]{tmpSelectedTopic.getTopic().getTitle()}));
    			log.info(methodCalled + ": user can not reply with new messages in this topic: " + tmpSelectedTopic.getTopic().getId() + ".  user: " + getUserId());
    			return false;
    		}
    		//can the user compose a new message
    		if(tmpSelectedMessage == null && (canCompose && !uiPermissionsManager.isNewResponse(tmpSelectedTopic.getTopic(), tmpSelectedForum.getForum(), getUserId(), forumContextId))){
    			setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEAGES_TO_POST_THREAD, new Object[]{tmpSelectedTopic.getTopic().getTitle()}));
    			log.info(methodCalled + ": user can not create new messages in this topic: " + tmpSelectedTopic.getTopic().getId() + ".  user: " + getUserId());
    			return false;
    		}

    		boolean messageOwner = tmpSelectedMessage != null && tmpSelectedMessage.getMessage() != null && tmpSelectedMessage.getMessage().getCreatedBy().equals(getUserId());
    		//can the user edit this message
    		if(canEdit && !((messageOwner && uiPermissionsManager.isReviseOwn(tmpSelectedTopic.getTopic(), tmpSelectedForum.getForum(), getUserId(), forumContextId)) 
    						|| uiPermissionsManager.isReviseAny(tmpSelectedTopic.getTopic(), tmpSelectedForum.getForum(), getUserId(), forumContextId))){
    			setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEAGES_TO_POST_THREAD, new Object[]{tmpSelectedTopic.getTopic().getTitle()}));
    			log.info(methodCalled + ": Insufficient privileages for user to revise message in topic: " + tmpSelectedTopic.getTopic().getTitle() + ".  user: " + getUserId());
    			return false;
    		}
    		//can the user delete this message
    		if(canDelete && !((messageOwner && uiPermissionsManager.isDeleteOwn(tmpSelectedTopic.getTopic(), tmpSelectedForum.getForum(), getUserId(), forumContextId)) 
    							|| uiPermissionsManager.isDeleteAny(tmpSelectedTopic.getTopic(), tmpSelectedForum.getForum(), getUserId(), forumContextId))){
    			setErrorMessage(getResourceBundleString(INSUFFICIENT_PRIVILEGES_TO_DELETE, new Object[]{tmpSelectedTopic.getTopic().getTitle()}));
    			log.info(methodCalled + ": Insufficient privileages for user to delete message: " + (tmpSelectedMessage == null ? "" : tmpSelectedMessage.getMessage().getId()) + ".  user: " + getUserId());
    			return false;
    		}

    		//ok Everything matched, so set the current values in case they changed:
    		selectedMessage = tmpSelectedMessage;
    		selectedThreadHead = tmpSelectedThreadHead;
    		selectedTopic = tmpSelectedTopic;
    		selectedForum = tmpSelectedForum;

    		return true;
    	}catch(Exception e){
    		log.error(methodCalled + ": " + e.getMessage(), e);
    	}
    	return false;
    }
	
	public boolean isShowAvailabilityDates(){
		return ServerConfigurationService.getBoolean("msgcntr.display.availability.dates", true);
	}

	//RUBRICS INTEGRATION FUNCTIONS
	public HashMap<String,String> getRubricConfigurationParameters() {
		FacesContext context = FacesContext.getCurrentInstance();
		Map requestParams = context.getExternalContext().getRequestParameterMap();
		HashMap list = new HashMap<String, String>();

		requestParams.forEach((key, value) -> {
			if (key.toString().startsWith("rbcs")) {
				list.put(key, value);
			}
		});
		return list;
	}

	public boolean hasAssociatedRubric(){
		return (allowedToGradeItem && (getRubricAssociationId() != null));
	}

    public String getRubricAssociationId() {
        String gradeAssign = selectedTopic != null ? selectedTopic.getGradeAssign()
                : selectedForum != null ? selectedForum.getGradeAssign()
                : null;

        if (gradeAssign != null && rubricsService.hasAssociatedRubric(RubricsConstants.RBCS_TOOL_GRADEBOOKNG, gradeAssign)) {
            return gradeAssign;
        }
        return null;
    }

	public String getCDNQuery() {
		return PortalUtils.getCDNQuery();
	}

	private void loadForumDataInForumBean(DiscussionForum forum, DiscussionForumBean bean) {
      BulkPermission permissions = uiPermissionsManager.getBulkPermissions(forum);
      bean.setChangeSettings(permissions.isChangeSettings());
      bean.setNewTopic(permissions.isNewTopic());

      // load attachments
      List<DecoratedAttachment> decoAttachList = new ArrayList<>();
      List<Attachment> attachList = forum.getAttachments();
      if (attachList != null) {
        attachList.stream().map(DecoratedAttachment::new).forEach(decoAttachList::add);
      }
      bean.setDecoAttachList(decoAttachList);
    }

	private void loadTopicDataInTopicBean(DiscussionTopic topic, DiscussionTopicBean bean) {
      final DiscussionForum forum = (DiscussionForum) topic.getBaseForum();

      BulkPermission permissions = uiPermissionsManager.getBulkPermissions(topic, forum);
      bean.setChangeSettings(permissions.isChangeSettings());
      bean.setIsDeleteAny(permissions.isDeleteAny());
      bean.setIsDeleteOwn(permissions.isDeleteOwn());
      bean.setIsMarkAsNotRead(permissions.isMarkAsNotRead());
      bean.setIsMovePostings(permissions.isMovePostings());
      bean.setIsModeratePostings(permissions.isModeratePostings());
      bean.setIsModeratedAndHasPerm(topic.getModerated() && permissions.isModeratePostings());
      bean.setIsNewResponse(permissions.isNewResponse());
      bean.setIsNewResponseToResponse(permissions.isNewResponseToResponse());
      bean.setPostToGradebook(permissions.isPostToGradebook());
      bean.setIsRead(permissions.isRead());
      bean.setIsReviseAny(permissions.isReviseAny());
      bean.setIsReviseOwn(permissions.isReviseOwn());

      // load attachments
      List<DecoratedAttachment> decoAttachList = new ArrayList<>();
      List<Attachment> attachList = forumManager.getTopicAttachments(topic.getId());
      if (attachList != null) {
        attachList.stream().map(DecoratedAttachment::new).forEach(decoAttachList::add);
      }
      bean.setAttachList(decoAttachList);
    }
	
    private void updateTask(Task task, String title, Date dueDate) {
      task.setDescription(title);
      task.setDue(dueDate == null ? null : dueDate.toInstant());
      taskService.saveTask(task);
    }
    
    private void createTask(String reference, String title, Date dueDate) {
      try {
        Task task = new Task();
        task.setSiteId(getSiteId());
        task.setReference(reference);
        task.setSystem(true);
        task.setDescription(title);
        task.setDue(dueDate == null ? null : dueDate.toInstant());
        Site site = siteService.getSite(getSiteId());
        Set<String> users = site.getUsersIsAllowed("section.role.student");
        taskService.createTask(task, users, Priorities.HIGH);
      } catch (Exception e) {
        setErrorMessage(getResourceBundleString(TASK_NOT_CREATED));
      }
    }

    public String getAttachmentReadableSize(final String attachmentSize) {
      return FileUtils.byteCountToDisplaySize(Long.parseLong(attachmentSize));
    }

	public boolean isGradebookGroupEnabled() {
		return getGradingService().isGradebookGroupEnabled(toolManager.getCurrentPlacement().getContext());
	}
}
