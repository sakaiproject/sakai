/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
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

package org.sakaiproject.assignment.tool;

import au.com.bytecode.opencsv.CSVReader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.Collator;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.sakaiproject.announcement.api.AnnouncementChannel;
import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementMessageEdit;
import org.sakaiproject.announcement.api.AnnouncementMessageHeaderEdit;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.assignment.api.Assignment.AssignmentAccess;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentContent;
import org.sakaiproject.assignment.api.AssignmentContentEdit;
import org.sakaiproject.assignment.api.AssignmentEdit;
import org.sakaiproject.assignment.api.AssignmentPeerAssessmentService;
import org.sakaiproject.assignment.api.AssignmentSubmission;
import org.sakaiproject.assignment.api.AssignmentSubmissionEdit;
import org.sakaiproject.assignment.api.model.AssignmentAllPurposeItem;
import org.sakaiproject.assignment.api.model.AssignmentAllPurposeItemAccess;
import org.sakaiproject.assignment.api.model.AssignmentModelAnswerItem;
import org.sakaiproject.assignment.api.model.AssignmentNoteItem;
import org.sakaiproject.assignment.api.model.AssignmentSupplementItemAttachment;
import org.sakaiproject.assignment.api.model.AssignmentSupplementItemService;
import org.sakaiproject.assignment.api.model.AssignmentSupplementItemWithAttachment;
import org.sakaiproject.assignment.api.model.PeerAssessmentItem;
import org.sakaiproject.assignment.api.model.PeerAssessmentAttachment;
import org.sakaiproject.assignment.cover.AssignmentService;
import org.sakaiproject.assignment.taggable.api.AssignmentActivityProducer;
import org.sakaiproject.assignment.taggable.tool.DecoratedTaggingProvider;
import org.sakaiproject.assignment.taggable.tool.DecoratedTaggingProvider.Pager;
import org.sakaiproject.assignment.taggable.tool.DecoratedTaggingProvider.Sort;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.PermissionsHelper;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.PagedResourceActionII;
import org.sakaiproject.cheftool.PortletConfig;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.ContentTypeImageService;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.contentreview.service.ContentReviewService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.LearningResourceStoreService;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Actor;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Object;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Statement;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Verb;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Verb.SAKAI_VERB;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.message.api.MessageHeader;
import org.sakaiproject.scoringservice.api.ScoringAgent;
import org.sakaiproject.scoringservice.api.ScoringComponent;
import org.sakaiproject.scoringservice.api.ScoringService;
import org.sakaiproject.service.gradebook.shared.AssignmentHasIllegalPointsException;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingExternalIdException;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.taggable.api.TaggingHelperInfo;
import org.sakaiproject.taggable.api.TaggingManager;
import org.sakaiproject.taggable.api.TaggingProvider;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeBreakdown;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.FileItem;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.SortedIterator;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;

/**
 * <p>
 * AssignmentAction is the action class for the assignment tool.
 * </p>
 */
public class AssignmentAction extends PagedResourceActionII
{
	private static ResourceLoader rb = new ResourceLoader("assignment");
	
	/** Our logger. */
	private static Logger M_log = LoggerFactory.getLogger(AssignmentAction.class);

	private static final String ASSIGNMENT_TOOL_ID = "sakai.assignment.grades";
	
	private static final Boolean allowReviewService = ServerConfigurationService.getBoolean("assignment.useContentReview", false);
	private static final Boolean allowPeerAssessment = ServerConfigurationService.getBoolean("assignment.usePeerAssessment", true);
	
	/** Is the review service available? */
	//Peer Assessment
	private static final String NEW_ASSIGNMENT_USE_PEER_ASSESSMENT= "new_assignment_use_peer_assessment";
	private static final String NEW_ASSIGNMENT_ADDITIONAL_OPTIONS= "new_assignment_additional_options";
	private static final String NEW_ASSIGNMENT_PEERPERIODMONTH = "new_assignment_peerperiodmonth";
	private static final String NEW_ASSIGNMENT_PEERPERIODDAY = "new_assignment_peerperiodday";
	private static final String NEW_ASSIGNMENT_PEERPERIODYEAR = "new_assignment_peerperiodyear";
	private static final String NEW_ASSIGNMENT_PEERPERIODHOUR = "new_assignment_peerperiodhour";
	private static final String NEW_ASSIGNMENT_PEERPERIODMIN = "new_assignment_peerperiodmin";
	private static final String NEW_ASSIGNMENT_PEER_ASSESSMENT_ANON_EVAL= "new_assignment_peer_assessment_anon_eval";
	private static final String NEW_ASSIGNMENT_PEER_ASSESSMENT_STUDENT_VIEW_REVIEWS= "new_assignment_peer_assessment_student_view_review";
	private static final String NEW_ASSIGNMENT_PEER_ASSESSMENT_NUM_REVIEWS= "new_assignment_peer_assessment_num_reviews";
	private static final String NEW_ASSIGNMENT_PEER_ASSESSMENT_INSTRUCTIONS = "new_assignment_peer_assessment_instructions";
	
	private static final String NEW_ASSIGNMENT_USE_REVIEW_SERVICE = "new_assignment_use_review_service";
	
	private static final String NEW_ASSIGNMENT_ALLOW_STUDENT_VIEW = "new_assignment_allow_student_view";
	private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_RADIO = "submit_papers_to";
	private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_NONE = "0";
	private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_STANDARD = "1";
	private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_INSITUTION = "2";
	// When to generate reports
    // although the service allows for a value of "1" --> Generate report immediately but overwrite until due date,
    // this doesn't make sense for assignment2. We limit the UI to 0 - Immediately
    // or 2 - On Due Date
	private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_RADIO = "report_gen_speed";
	private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_IMMEDIATELY = "0";
	private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_DUE = "2";
	private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_TURNITIN = "s_paper_check";
	private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INTERNET = "internet_check";
	private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_PUB = "journal_check";
	private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INSTITUTION = "institution_check";
	private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_BIBLIOGRAPHIC = "exclude_biblio";
	private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_QUOTED = "exclude_quoted";
	private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_SMALL_MATCHES = "exclude_smallmatches";
	private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_TYPE = "exclude_type";
	private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_VALUE = "exclude_value";

	/** Peer Review Attachments **/
	private static final String PEER_ATTACHMENTS = "peer_attachments";
	private static final String PEER_ASSESSMENT = "peer_assessment";
	
	/** The attachments */
	private static final String ATTACHMENTS = "Assignment.attachments";
	private static final String ATTACHMENTS_FOR = "Assignment.attachments_for";
	
        /** The property name associated with Groups that are Sections **/
        private static final String GROUP_SECTION_PROPERTY = "sections_category";

	/** The content type image lookup service in the State. */
	private static final String STATE_CONTENT_TYPE_IMAGE_SERVICE = "Assignment.content_type_image_service";

	/** The calendar service in the State. */
	private static final String STATE_CALENDAR_SERVICE = "Assignment.calendar_service";

	/** The announcement service in the State. */
	private static final String STATE_ANNOUNCEMENT_SERVICE = "Assignment.announcement_service";

	/** The calendar object */
	private static final String CALENDAR = "calendar";
	
	/** Additional calendar service */
	private static final String ADDITIONAL_CALENDAR = "additonal_calendar";
	
	/** The calendar tool */
	private static final String CALENDAR_TOOL_EXIST = "calendar_tool_exisit";

	/** Additional calendar tool */
	private static final String ADDITIONAL_CALENDAR_TOOL_READY = "additional_calendar_tool_ready";
	
	/** The announcement tool */
	private static final String ANNOUNCEMENT_TOOL_EXIST = "announcement_tool_exist";
	
	/** The announcement channel */
	private static final String ANNOUNCEMENT_CHANNEL = "announcement_channel";

	/** The state mode */
	private static final String STATE_MODE = "Assignment.mode";

	/** The context string */
	private static final String STATE_CONTEXT_STRING = "Assignment.context_string";

	/** The user */
	private static final String STATE_USER = "Assignment.user";

	/** The submitter */
	private static final String STATE_SUBMITTER = "Assignment.submitter";

	// SECTION MOD
	/** Used to keep track of the section info not currently being used. */
	private static final String STATE_SECTION_STRING = "Assignment.section_string";

	/** **************************** sort assignment ********************** */
	/** state sort * */
	private static final String SORTED_BY = "Assignment.sorted_by";

	/** state sort ascendingly * */
	private static final String SORTED_ASC = "Assignment.sorted_asc";
	
	/** default sorting */
	private static final String SORTED_BY_DEFAULT = "default";

	/** sort by assignment title */
	private static final String SORTED_BY_TITLE = "title";

	/** sort by assignment section */
	private static final String SORTED_BY_SECTION = "section";

	/** sort by assignment due date */
	private static final String SORTED_BY_DUEDATE = "duedate";

	/** sort by assignment open date */
	private static final String SORTED_BY_OPENDATE = "opendate";

	/** sort by assignment status */
	private static final String SORTED_BY_ASSIGNMENT_STATUS = "assignment_status";

	/** sort by assignment submission status */
	private static final String SORTED_BY_SUBMISSION_STATUS = "submission_status";

	/** sort by assignment number of submissions */
	private static final String SORTED_BY_NUM_SUBMISSIONS = "num_submissions";

	/** sort by assignment number of ungraded submissions */
	private static final String SORTED_BY_NUM_UNGRADED = "num_ungraded";

	/** sort by assignment submission grade */
	private static final String SORTED_BY_GRADE = "grade";

	/** sort by assignment maximun grade available */
	private static final String SORTED_BY_MAX_GRADE = "max_grade";

	/** sort by assignment range */
	private static final String SORTED_BY_FOR = "for";

	/** sort by group title */
	private static final String SORTED_BY_GROUP_TITLE = "group_title";

	/** sort by group description */
	private static final String SORTED_BY_GROUP_DESCRIPTION = "group_description";

	/** *************************** sort submission in instructor grade view *********************** */
	/** state sort submission* */
	private static final String SORTED_GRADE_SUBMISSION_BY = "Assignment.grade_submission_sorted_by";

	/** state sort submission ascendingly * */
	private static final String SORTED_GRADE_SUBMISSION_ASC = "Assignment.grade_submission_sorted_asc";

	/** state sort submission by submitters last name * */
	private static final String SORTED_GRADE_SUBMISSION_BY_LASTNAME = "sorted_grade_submission_by_lastname";

	/** state sort submission by submit time * */
	private static final String SORTED_GRADE_SUBMISSION_BY_SUBMIT_TIME = "sorted_grade_submission_by_submit_time";

	/** state sort submission by submission status * */
	private static final String SORTED_GRADE_SUBMISSION_BY_STATUS = "sorted_grade_submission_by_status";

	/** state sort submission by submission grade * */
	private static final String SORTED_GRADE_SUBMISSION_BY_GRADE = "sorted_grade_submission_by_grade";

	/** state sort submission by submission released * */
	private static final String SORTED_GRADE_SUBMISSION_BY_RELEASED = "sorted_grade_submission_by_released";
	
	/** state sort submissuib by content review score **/
	private static final String SORTED_GRADE_SUBMISSION_CONTENTREVIEW = "sorted_grade_submission_by_contentreview";

	/** *************************** sort submission *********************** */
	/** state sort submission* */
	private static final String SORTED_SUBMISSION_BY = "Assignment.submission_sorted_by";

	/** state sort submission ascendingly * */
	private static final String SORTED_SUBMISSION_ASC = "Assignment.submission_sorted_asc";

	/** state sort submission by submitters last name * */
	private static final String SORTED_SUBMISSION_BY_LASTNAME = "sorted_submission_by_lastname";

	/** state sort submission by submit time * */
	private static final String SORTED_SUBMISSION_BY_SUBMIT_TIME = "sorted_submission_by_submit_time";

	/** state sort submission by submission grade * */
	private static final String SORTED_SUBMISSION_BY_GRADE = "sorted_submission_by_grade";

	/** state sort submission by submission status * */
	private static final String SORTED_SUBMISSION_BY_STATUS = "sorted_submission_by_status";

	/** state sort submission by submission released * */
	private static final String SORTED_SUBMISSION_BY_RELEASED = "sorted_submission_by_released";

	/** state sort submission by assignment title */
	private static final String SORTED_SUBMISSION_BY_ASSIGNMENT = "sorted_submission_by_assignment";

	/** state sort submission by max grade */
	private static final String SORTED_SUBMISSION_BY_MAX_GRADE = "sorted_submission_by_max_grade";
	
	/*********************** Sort by user sort name *****************************************/
	private static final String SORTED_USER_BY_SORTNAME = "sorted_user_by_sortname";

	/** ******************** student's view assignment submission ****************************** */
	/** the assignment object been viewing * */
	private static final String VIEW_SUBMISSION_ASSIGNMENT_REFERENCE = "Assignment.view_submission_assignment_reference";

	/** the submission text to the assignment * */
	private static final String VIEW_SUBMISSION_TEXT = "Assignment.view_submission_text";

	/** the submission answer to Honor Pledge * */
	private static final String VIEW_SUBMISSION_HONOR_PLEDGE_YES = "Assignment.view_submission_honor_pledge_yes";

	/** ***************** student's preview of submission *************************** */
	/** the assignment id * */
	private static final String PREVIEW_SUBMISSION_ASSIGNMENT_REFERENCE = "preview_submission_assignment_reference";

	/** the submission text * */
	private static final String PREVIEW_SUBMISSION_TEXT = "preview_submission_text";

	/** the submission honor pledge answer * */
	private static final String PREVIEW_SUBMISSION_HONOR_PLEDGE_YES = "preview_submission_honor_pledge_yes";

	/** the submission attachments * */
	private static final String PREVIEW_SUBMISSION_ATTACHMENTS = "preview_attachments";

	/** the flag indicate whether the to show the student view or not */
	private static final String PREVIEW_ASSIGNMENT_STUDENT_VIEW_HIDE_FLAG = "preview_assignment_student_view_hide_flag";

	/** the flag indicate whether the to show the assignment info or not */
	private static final String PREVIEW_ASSIGNMENT_ASSIGNMENT_HIDE_FLAG = "preview_assignment_assignment_hide_flag";

	/** the assignment id */
	private static final String PREVIEW_ASSIGNMENT_ASSIGNMENT_ID = "preview_assignment_assignment_id";

	/** the assignment content id */
	private static final String PREVIEW_ASSIGNMENT_ASSIGNMENTCONTENT_ID = "preview_assignment_assignmentcontent_id";

	/** ************** view assignment ***************************************** */
	/** the hide assignment flag in the view assignment page * */
	private static final String VIEW_ASSIGNMENT_HIDE_ASSIGNMENT_FLAG = "view_assignment_hide_assignment_flag";

	/** the hide student view flag in the view assignment page * */
	private static final String VIEW_ASSIGNMENT_HIDE_STUDENT_VIEW_FLAG = "view_assignment_hide_student_view_flag";

	/** ******************* instructor's view assignment ***************************** */
	private static final String VIEW_ASSIGNMENT_ID = "view_assignment_id";

	/** ******************* instructor's edit assignment ***************************** */
	private static final String EDIT_ASSIGNMENT_ID = "edit_assignment_id";

	/** ******************* instructor's delete assignment ids ***************************** */
	private static final String DELETE_ASSIGNMENT_IDS = "delete_assignment_ids";

	/** ******************* flags controls the grade assignment page layout ******************* */
	private static final String GRADE_ASSIGNMENT_EXPAND_FLAG = "grade_assignment_expand_flag";

	private static final String GRADE_SUBMISSION_EXPAND_FLAG = "grade_submission_expand_flag";
	
	private static final String GRADE_NO_SUBMISSION_DEFAULT_GRADE = "grade_no_submission_default_grade";

	/** ******************* instructor's grade submission ***************************** */
	private static final String GRADE_SUBMISSION_ASSIGNMENT_ID = "grade_submission_assignment_id";

	private static final String GRADE_SUBMISSION_SUBMISSION_ID = "grade_submission_submission_id";

	private static final String GRADE_SUBMISSION_FEEDBACK_COMMENT = "grade_submission_feedback_comment";

	private static final String GRADE_SUBMISSION_FEEDBACK_TEXT = "grade_submission_feedback_text";

	private static final String GRADE_SUBMISSION_FEEDBACK_ATTACHMENT = "grade_submission_feedback_attachment";

	private static final String GRADE_SUBMISSION_GRADE = "grade_submission_grade";

	private static final String GRADE_SUBMISSION_ASSIGNMENT_EXPAND_FLAG = "grade_submission_assignment_expand_flag";

	private static final String GRADE_SUBMISSION_ALLOW_RESUBMIT = "grade_submission_allow_resubmit";
	
	private static final String GRADE_SUBMISSION_DONE = "grade_submission_done";
	private static final String GRADE_SUBMISSION_SUBMIT = "grade_submission_submit";
	
	/** ******************* instructor's export assignment ***************************** */
	private static final String EXPORT_ASSIGNMENT_REF = "export_assignment_ref";

	/**
	 * Is review service enabled? 
	 */
	private static final String ENABLE_REVIEW_SERVICE = "enable_review_service";

	private static final String EXPORT_ASSIGNMENT_ID = "export_assignment_id";

	/** ****************** instructor's new assignment ****************************** */
	private static final String NEW_ASSIGNMENT_TITLE = "new_assignment_title";
	
	// assignment order for default view
	private static final String NEW_ASSIGNMENT_ORDER = "new_assignment_order";

        private static final String NEW_ASSIGNMENT_GROUP_SUBMIT = "new_assignment_group_submit";
        
	// open date
	private static final String NEW_ASSIGNMENT_OPENMONTH = "new_assignment_openmonth";

	private static final String NEW_ASSIGNMENT_OPENDAY = "new_assignment_openday";

	private static final String NEW_ASSIGNMENT_OPENYEAR = "new_assignment_openyear";

	private static final String NEW_ASSIGNMENT_OPENHOUR = "new_assignment_openhour";

	private static final String NEW_ASSIGNMENT_OPENMIN = "new_assignment_openmin";

	// visible date
        private static final String NEW_ASSIGNMENT_VISIBLEMONTH = "new_assignment_visiblemonth";

        private static final String NEW_ASSIGNMENT_VISIBLEDAY = "new_assignment_visibleday";

        private static final String NEW_ASSIGNMENT_VISIBLEYEAR = "new_assignment_visibleyear";

        private static final String NEW_ASSIGNMENT_VISIBLEHOUR = "new_assignment_visiblehour";

        private static final String NEW_ASSIGNMENT_VISIBLEMIN = "new_assignment_visiblemin";

	private static final String NEW_ASSIGNMENT_VISIBLETOGGLE = "new_assignment_visibletoggle";
	
	// due date
	private static final String NEW_ASSIGNMENT_DUEMONTH = "new_assignment_duemonth";

	private static final String NEW_ASSIGNMENT_DUEDAY = "new_assignment_dueday";

	private static final String NEW_ASSIGNMENT_DUEYEAR = "new_assignment_dueyear";

	private static final String NEW_ASSIGNMENT_DUEHOUR = "new_assignment_duehour";

	private static final String NEW_ASSIGNMENT_DUEMIN = "new_assignment_duemin";

	private static final String NEW_ASSIGNMENT_PAST_DUE_DATE = "new_assignment_past_due_date";
	
	// close date
	private static final String NEW_ASSIGNMENT_ENABLECLOSEDATE = "new_assignment_enableclosedate";

	private static final String NEW_ASSIGNMENT_CLOSEMONTH = "new_assignment_closemonth";

	private static final String NEW_ASSIGNMENT_CLOSEDAY = "new_assignment_closeday";

	private static final String NEW_ASSIGNMENT_CLOSEYEAR = "new_assignment_closeyear";

	private static final String NEW_ASSIGNMENT_CLOSEHOUR = "new_assignment_closehour";

	private static final String NEW_ASSIGNMENT_CLOSEMIN = "new_assignment_closemin";

	
	private static final String NEW_ASSIGNMENT_ATTACHMENT = "new_assignment_attachment";

	private static final String NEW_ASSIGNMENT_SECTION = "new_assignment_section";

	private static final String NEW_ASSIGNMENT_SUBMISSION_TYPE = "new_assignment_submission_type";
	
	private static final String NEW_ASSIGNMENT_CATEGORY = "new_assignment_category";

	private static final String NEW_ASSIGNMENT_GRADE_TYPE = "new_assignment_grade_type";

	private static final String NEW_ASSIGNMENT_GRADE_POINTS = "new_assignment_grade_points";

	private static final String NEW_ASSIGNMENT_DESCRIPTION = "new_assignment_instructions";

	private static final String NEW_ASSIGNMENT_DUE_DATE_SCHEDULED = "new_assignment_due_date_scheduled";

	private static final String NEW_ASSIGNMENT_OPEN_DATE_ANNOUNCED = "new_assignment_open_date_announced";

	private static final String NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE = "new_assignment_check_add_honor_pledge";

	private static final String NEW_ASSIGNMENT_CHECK_HIDE_DUE_DATE = "new_assignment_check_hide_due_date";

	private static final String NEW_ASSIGNMENT_FOCUS = "new_assignment_focus";

	private static final String NEW_ASSIGNMENT_DESCRIPTION_EMPTY = "new_assignment_description_empty";

	private static final String NEW_ASSIGNMENT_ADD_TO_GRADEBOOK = "new_assignment_add_to_gradebook";

	private static final String NEW_ASSIGNMENT_RANGE = "new_assignment_range";

	private static final String NEW_ASSIGNMENT_GROUPS = "new_assignment_groups";
	
        private static final String VIEW_SUBMISSION_GROUP = "view_submission_group";
        private static final String VIEW_SUBMISSION_ORIGINAL_GROUP = "view_submission_original_group";
	
	private static final String NEW_ASSIGNMENT_PAST_CLOSE_DATE = "new_assignment_past_close_date";
	
	/*************************** assignment model answer attributes *************************/
	private static final String NEW_ASSIGNMENT_MODEL_ANSWER = "new_assignment_model_answer";
	private static final String NEW_ASSIGNMENT_MODEL_ANSWER_TEXT = "new_assignment_model_answer_text";
	private static final String NEW_ASSIGNMENT_MODEL_SHOW_TO_STUDENT = "new_assignment_model_answer_show_to_student";
	private static final String NEW_ASSIGNMENT_MODEL_ANSWER_ATTACHMENT = "new_assignment_model_answer_attachment";
	
	/**************************** assignment year range *************************/
	private static final String NEW_ASSIGNMENT_YEAR_RANGE_FROM = "new_assignment_year_range_from";
	private static final String NEW_ASSIGNMENT_YEAR_RANGE_TO = "new_assignment_year_range_to";
	
	// submission level of resubmit due time
	private static final String ALLOW_RESUBMIT_CLOSEMONTH = "allow_resubmit_closeMonth";
	private static final String ALLOW_RESUBMIT_CLOSEDAY = "allow_resubmit_closeDay";
	private static final String ALLOW_RESUBMIT_CLOSEYEAR = "allow_resubmit_closeYear";
	private static final String ALLOW_RESUBMIT_CLOSEHOUR = "allow_resubmit_closeHour";
	private static final String ALLOW_RESUBMIT_CLOSEMIN = "allow_resubmit_closeMin";
	
	private static final String ATTACHMENTS_MODIFIED = "attachments_modified";

	/** **************************** instructor's view student submission ***************** */
	// the show/hide table based on member id
	private static final String STUDENT_LIST_SHOW_TABLE = "STUDENT_LIST_SHOW_TABLE";

	/** **************************** student view grade submission id *********** */
	private static final String VIEW_GRADE_SUBMISSION_ID = "view_grade_submission_id";
	
	// alert for grade exceeds max grade setting
	private static final String GRADE_GREATER_THAN_MAX_ALERT = "grade_greater_than_max_alert";

	/** **************************** modes *************************** */
	/** The list view of assignments */
   private static final String MODE_LIST_ASSIGNMENTS = "lisofass1"; // set in velocity template

	/** The student view of an assignment submission */
	private static final String MODE_STUDENT_VIEW_SUBMISSION = "Assignment.mode_view_submission";
	
	/** The student view of an assignment submission confirmation */
	private static final String MODE_STUDENT_VIEW_SUBMISSION_CONFIRMATION = "Assignment.mode_view_submission_confirmation";

	/** The student preview of an assignment submission */
	private static final String MODE_STUDENT_PREVIEW_SUBMISSION = "Assignment.mode_student_preview_submission";

	/** The student view of graded submission */
	private static final String MODE_STUDENT_VIEW_GRADE = "Assignment.mode_student_view_grade";
	
	/** The student view of graded submission */
	private static final String MODE_STUDENT_VIEW_GRADE_PRIVATE = "Assignment.mode_student_view_grade_private";

       /** The student view of a group submission error (user is in multiple groups */
       private static final String MODE_STUDENT_VIEW_GROUP_ERROR = "Assignment.mode_student_view_group_error";

	/** The student view of assignments */
	private static final String MODE_STUDENT_VIEW_ASSIGNMENT = "Assignment.mode_student_view_assignment";

	/** The instructor view of creating a new assignment or editing an existing one */
	private static final String MODE_INSTRUCTOR_NEW_EDIT_ASSIGNMENT = "Assignment.mode_instructor_new_edit_assignment";
	
	/** The instructor view to reorder assignments */
	private static final String MODE_INSTRUCTOR_REORDER_ASSIGNMENT = "reorder";

	/** The instructor view to delete an assignment */
	private static final String MODE_INSTRUCTOR_DELETE_ASSIGNMENT = "Assignment.mode_instructor_delete_assignment";

	/** The instructor view to grade an assignment */
	private static final String MODE_INSTRUCTOR_GRADE_ASSIGNMENT = "Assignment.mode_instructor_grade_assignment";

	/** The instructor view to grade a submission */
	private static final String MODE_INSTRUCTOR_GRADE_SUBMISSION = "Assignment.mode_instructor_grade_submission";

	/** The instructor view of preview grading a submission */
	private static final String MODE_INSTRUCTOR_PREVIEW_GRADE_SUBMISSION = "Assignment.mode_instructor_preview_grade_submission";

	/** The instructor preview of one assignment */
	private static final String MODE_INSTRUCTOR_PREVIEW_ASSIGNMENT = "Assignment.mode_instructor_preview_assignments";

	/** The instructor view of one assignment */
	private static final String MODE_INSTRUCTOR_VIEW_ASSIGNMENT = "Assignment.mode_instructor_view_assignments";

	/** The instructor view to list students of an assignment */
	private static final String MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT = "lisofass2"; // set in velocity template

	/** The instructor view of assignment submission report */
	private static final String MODE_INSTRUCTOR_REPORT_SUBMISSIONS = "grarep"; // set in velocity template

	/** The instructor view of download all file */
	private static final String MODE_INSTRUCTOR_DOWNLOAD_ALL = "downloadAll"; 
	
	/** The instructor view of uploading all from archive file */
	private static final String MODE_INSTRUCTOR_UPLOAD_ALL = "uploadAll"; 

	/** The student view of assignment submission report */
	private static final String MODE_STUDENT_VIEW = "stuvie"; // set in velocity template

	/** The option view */
	private static final String MODE_OPTIONS= "options"; // set in velocity template
	
	/** Review Edit page for students */
	private static final String MODE_STUDENT_REVIEW_EDIT= "Assignment.mode_student_review_edit"; // set in velocity template

	/** ************************* vm names ************************** */
	/** The list view of assignments */
	private static final String TEMPLATE_LIST_ASSIGNMENTS = "_list_assignments";

	/** The student view of assignment */
	private static final String TEMPLATE_STUDENT_VIEW_ASSIGNMENT = "_student_view_assignment";

	/** The student view of showing an assignment submission */
	private static final String TEMPLATE_STUDENT_VIEW_SUBMISSION = "_student_view_submission";

        /** The student view of showing a group assignment grouping error */
       private static final String TEMPLATE_STUDENT_VIEW_GROUP_ERROR = "_student_view_group_error";
	
	/** The student view of an assignment submission confirmation */
	private static final String TEMPLATE_STUDENT_VIEW_SUBMISSION_CONFIRMATION = "_student_view_submission_confirmation";

	/** The student preview an assignment submission */
	private static final String TEMPLATE_STUDENT_PREVIEW_SUBMISSION = "_student_preview_submission";

	/** The student view of graded submission */
	private static final String TEMPLATE_STUDENT_VIEW_GRADE = "_student_view_grade";

	/** The instructor view to create a new assignment or edit an existing one */
	private static final String TEMPLATE_INSTRUCTOR_NEW_EDIT_ASSIGNMENT = "_instructor_new_edit_assignment";
	
	/** The instructor view to reorder the default assignments */
	private static final String TEMPLATE_INSTRUCTOR_REORDER_ASSIGNMENT = "_instructor_reorder_assignment";

	/** The instructor view to edit assignment */
	private static final String TEMPLATE_INSTRUCTOR_DELETE_ASSIGNMENT = "_instructor_delete_assignment";

	/** The instructor view to edit assignment */
	private static final String TEMPLATE_INSTRUCTOR_GRADE_SUBMISSION = "_instructor_grading_submission";

	/** The instructor preview to edit assignment */
	private static final String TEMPLATE_INSTRUCTOR_PREVIEW_GRADE_SUBMISSION = "_instructor_preview_grading_submission";

	/** The instructor view to grade the assignment */
	private static final String TEMPLATE_INSTRUCTOR_GRADE_ASSIGNMENT = "_instructor_list_submissions";

	/** The instructor preview of assignment */
	private static final String TEMPLATE_INSTRUCTOR_PREVIEW_ASSIGNMENT = "_instructor_preview_assignment";

	/** The instructor view of assignment */
	private static final String TEMPLATE_INSTRUCTOR_VIEW_ASSIGNMENT = "_instructor_view_assignment";

	/** The instructor view to edit assignment */
	private static final String TEMPLATE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT = "_instructor_student_list_submissions";

	/** The instructor view to assignment submission report */
	private static final String TEMPLATE_INSTRUCTOR_REPORT_SUBMISSIONS = "_instructor_report_submissions";

	/** The instructor view to upload all information from archive file */
	private static final String TEMPLATE_INSTRUCTOR_UPLOAD_ALL = "_instructor_uploadAll";
	/** The student view to edit reviews **/
	private static final String TEMPLATE_STUDENT_REVIEW_EDIT = "_student_review_edit";

	/** The options page */
	private static final String TEMPLATE_OPTIONS = "_options";

	/** The opening mark comment */
	private static final String COMMENT_OPEN = "{{";

	/** The closing mark for comment */
	private static final String COMMENT_CLOSE = "}}";

	/** The selected view */
	private static final String STATE_SELECTED_VIEW = "state_selected_view";

	/** The configuration choice of with grading option or not */
	private static final String WITH_GRADES = "with_grades";
	
	/** The configuration choice of showing or hiding the number of submissions column  */
	private static final String SHOW_NUMBER_SUBMISSION_COLUMN = "showNumSubmissionColumn";

	/** The alert flag when doing global navigation from improper mode */
	private static final String ALERT_GLOBAL_NAVIGATION = "alert_global_navigation";

	/** The total list item before paging */
	private static final String STATE_PAGEING_TOTAL_ITEMS = "state_paging_total_items";

	/** is current user allowed to grade assignment? */
	private static final String STATE_ALLOW_GRADE_SUBMISSION = "state_allow_grade_submission";

	/** property for previous feedback attachments **/
	private static final String PROP_SUBMISSION_PREVIOUS_FEEDBACK_ATTACHMENTS = "prop_submission_previous_feedback_attachments";
	
	/** the user and submission list for list of submissions page */
	private static final String USER_SUBMISSIONS = "user_submissions";
	
	/** the items for storing the comments and grades for peer assessment **/
	private static final String PEER_ASSESSMENT_ITEMS = "peer_assessment_items";
	
	private static final String PEER_ASSESSMENT_ASSESSOR_ID = "peer_assessment_assesor_id";
	
	private static final String PEER_ASSESSMENT_REMOVED_STATUS = "peer_assessment_removed_status";
	
	/** ************************* Taggable constants ************************** */
	/** identifier of tagging provider that will provide the appropriate helper */
	private static final String PROVIDER_ID = "providerId";

	/** Reference to an activity */
	private static final String ACTIVITY_REF = "activityRef";
	
	/** Reference to an item */
	private static final String ITEM_REF = "itemRef";
	
	/** session attribute for list of decorated tagging providers */
	private static final String PROVIDER_LIST = "providerList";
	
	// whether the choice of emails instructor submission notification is available in the installation
	private static final String ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS = "assignment.instructor.notifications";
	
	// default for whether or how the instructor receive submission notification emails, none(default)|each|digest
	private static final String ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_DEFAULT = "assignment.instructor.notifications.default";
	
	// name for release grade notification
	private static final String ASSIGNMENT_RELEASEGRADE_NOTIFICATION = "assignment.releasegrade.notification";
	private static final String ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION = "assignment.releasereturn.notification";
	
	/****************************** Upload all screen ***************************/
	private static final String UPLOAD_ALL_HAS_SUBMISSION_TEXT = "upload_all_has_submission_text";
	private static final String UPLOAD_ALL_HAS_SUBMISSION_ATTACHMENT = "upload_all_has_submission_attachment";
	private static final String UPLOAD_ALL_HAS_GRADEFILE = "upload_all_has_gradefile";
	private static final String UPLOAD_ALL_GRADEFILE_FORMAT = "upload_all_gradefile_format";
	private static final String UPLOAD_ALL_HAS_COMMENTS= "upload_all_has_comments";
	private static final String UPLOAD_ALL_HAS_FEEDBACK_TEXT= "upload_all_has_feedback_text";
	private static final String UPLOAD_ALL_HAS_FEEDBACK_ATTACHMENT = "upload_all_has_feedback_attachment";
	private static final String UPLOAD_ALL_WITHOUT_FOLDERS = "upload_all_without_folders";
	private static final String UPLOAD_ALL_RELEASE_GRADES = "upload_all_release_grades";
	
	// this is to track whether the site has multiple assignment, hence if true, show the reorder link
	private static final String HAS_MULTIPLE_ASSIGNMENTS = "has_multiple_assignments";
	
	// view all or grouped submission list
	private static final String VIEW_SUBMISSION_LIST_OPTION = "view_submission_list_option";
	
	/************************* SAK-17606 - Upload all grades.csv columns ********************/
	private static final int IDX_GRADES_CSV_EID = 1;
	private static final int IDX_GRADES_CSV_GRADE = 5;
	
	// search string for submission list
	private static final String VIEW_SUBMISSION_SEARCH = "view_submission_search";
	
	private ContentHostingService m_contentHostingService = null;

	private org.sakaiproject.entity.api.EntityManager m_entityManager = null;

	private EventTrackingService m_eventTrackingService = null;
	
	private NotificationService m_notificationService = null;
	
	private SecurityService m_securityService = null;

	private AuthzGroupService authzGroupService = null;
	
	/********************** Supplement item ************************/
	private AssignmentSupplementItemService m_assignmentSupplementItemService = null;
	/******** Model Answer ************/
	private static final String MODELANSWER = "modelAnswer";
	private static final String MODELANSWER_TEXT = "modelAnswer.text";
	private static final String MODELANSWER_SHOWTO = "modelAnswer.showTo";
	private static final String MODELANSWER_ATTACHMENTS = "modelanswer_attachments";
	private static final String MODELANSWER_TO_DELETE = "modelanswer.toDelete";
	/******** Note ***********/
	private static final String NOTE = "note";
	private static final String NOTE_TEXT = "note.text";
	private static final String NOTE_SHAREWITH = "note.shareWith";
	private static final String NOTE_TO_DELETE = "note.toDelete";
	
	/******** AllPurpose *******/
	private static final String ALLPURPOSE = "allPurpose";
	private static final String ALLPURPOSE_TITLE = "allPurpose.title";
	private static final String ALLPURPOSE_TEXT = "allPurpose.text";
	private static final String ALLPURPOSE_HIDE = "allPurpose.hide";
	private static final String ALLPURPOSE_SHOW_FROM = "allPurpose.show.from";
	private static final String ALLPURPOSE_SHOW_TO = "allPurpose.show.to";
	private static final String ALLPURPOSE_RELEASE_DATE = "allPurpose.releaseDate";
	private static final String ALLPURPOSE_RETRACT_DATE= "allPurpose.retractDate";
	private static final String ALLPURPOSE_ACCESS = "allPurpose.access";
	private static final String ALLPURPOSE_ATTACHMENTS = "allPurpose_attachments";
	private static final String ALLPURPOSE_RELEASE_YEAR = "allPurpose_releaseYear";
	private static final String ALLPURPOSE_RELEASE_MONTH = "allPurpose_releaseMonth";
	private static final String ALLPURPOSE_RELEASE_DAY = "allPurpose_releaseDay";
	private static final String ALLPURPOSE_RELEASE_HOUR = "allPurpose_releaseHour";
	private static final String ALLPURPOSE_RELEASE_MIN = "allPurpose_releaseMin";
	private static final String ALLPURPOSE_RETRACT_YEAR = "allPurpose_retractYear";
	private static final String ALLPURPOSE_RETRACT_MONTH = "allPurpose_retractMonth";
	private static final String ALLPURPOSE_RETRACT_DAY = "allPurpose_retractDay";
	private static final String ALLPURPOSE_RETRACT_HOUR = "allPurpose_retractHour";
	private static final String ALLPURPOSE_RETRACT_MIN = "allPurpose_retractMin";
	private static final String ALLPURPOSE_TO_DELETE = "allPurpose.toDelete";
	
	private static final String RETURNED_FEEDBACK = "feedback_returned_to_selected_users";
	
	private static final String OW_FEEDBACK = "feedback_overwritten";
	
	private static final String SAVED_FEEDBACK = "feedback_saved";
	
	private static final int INPUT_BUFFER_SIZE = 102400;
	
	private static final String INVOKE = "invoke_via";
	private static final String INVOKE_BY_LINK = "link";
	private static final String INVOKE_BY_PORTAL = "portal";
	
	
	
	private static final String SUBMISSIONS_SEARCH_ONLY = "submissions_search_only";
	
	/*************** search related *******************/
	private static final String STATE_SEARCH = "state_search";
	private static final String FORM_SEARCH = "form_search";
	
	private static final String STATE_DOWNLOAD_URL = "state_download_url";

	/** To know if grade_submission go from view_students_assignment view or not **/
	private static final String FROM_VIEW = "from_view";
	
	/** SAK-17606 - Property for whether an assignment user anonymous grading (user settable). */
	private static final String NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING = "new_assignment_check_anonymous_grading";

	/** Sakai.property for enable/disable anonymous grading */
	private static final String SAK_PROP_ENABLE_ANON_GRADING = "assignment.anon.grading.enabled";

	// SAK-29314
	private boolean nextUngraded = false;
	private boolean prevUngraded = false;
	private boolean nextWithSubmission = false;
	private boolean prevWithSubmission = false;
	private boolean nextUngradedWithSubmission = false;
	private boolean prevUngradedWithSubmission = false;
	private String nextUngradedRef = "";
	private String prevUngradedRef = "";
	private String nextWithSubmissionRef = "";
	private String prevWithSubmissionRef = "";
	private String nextUngradedWithSubmissionRef = "";
	private String prevUngradedWithSubmissionRef = "";
	private final String NO_SUBMISSION = rb.getString( "listsub.nosub" );
	private static final String FLAG_ON = "on";
	private static final String FLAG_TRUE = "true";
	private static final String FLAG_NEXT = "next";
	private static final String FLAG_PREV = "prev";
	private static final String FLAG_NEXT_UNGRADED = "nextUngraded";
	private static final String FLAG_PREV_UNGRADED = "prevUngraded";
	private static final String FLAG_NEXT_WITH_SUB = "nextWithSubmission";
	private static final String FLAG_PREV_WITH_SUB = "prevWithSubmission";
	private static final String FLAG_NEXT_UNGRADED_WITH_SUB = "nextUngradedWithSubmission";
	private static final String FLAG_PREV_UNGRADED_WITH_SUB = "prevUngradedWithSubmission";
	private static final String STATE_VIEW_SUBS_ONLY = "state_view_submissions_only_selected";
	private static final String CONTEXT_VIEW_SUBS_ONLY = "subsOnlySelected";
	private static final String CONTEXT_NEXT_UNGRADED_SUB_ID = "nextUngradedSubmissionID";
	private static final String CONTEXT_PREV_UNGRADED_SUB_ID = "prevUngradedSubmissionID";
	private static final String CONTEXT_NEXT_WITH_SUB_ID = "nextWithSubmissionID";
	private static final String CONTEXT_PREV_WITH_SUB_ID = "prevWithSubmissionID";
	private static final String CONTEXT_NEXT_UNGRADED_WITH_SUB_ID = "nextUngradedWithSubmissionID";
	private static final String CONTEXT_PREV_UNGRADED_WITH_SUB_ID = "prevUngradedWithSubmissionID";
	private static final String CONTEXT_GO_NEXT_UNGRADED_ENABLED = "goNextUngradedEnabled";
	private static final String CONTEXT_GO_PREV_UNGRADED_ENABLED = "goPrevUngradedEnabled";
	private static final String PARAMS_VIEW_SUBS_ONLY_CHECKBOX = "chkSubsOnly1";
	
	private AssignmentPeerAssessmentService assignmentPeerAssessmentService;
	public void setAssignmentPeerAssessmentService(AssignmentPeerAssessmentService assignmentPeerAssessmentService){
		this.assignmentPeerAssessmentService = assignmentPeerAssessmentService;
	}
	
	
	public String buildLinkedPanelContext(VelocityPortlet portlet, Context context, RunData data, SessionState state){
		state.setAttribute(INVOKE, INVOKE_BY_LINK);
		return buildMainPanelContext(portlet, context, data, state);
	}
	
	
	/**
	 * central place for dispatching the build routines based on the state name
	 */
	public String buildMainPanelContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	{
		String template = null;
		context.put("action", "AssignmentAction");

		context.put("tlang", rb);
		context.put("dateFormat", getDateFormatString());
		context.put("cheffeedbackhelper", this);

		String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);

		// allow add assignment?
		boolean allowAddAssignment = AssignmentService.allowAddAssignment(contextString);
		context.put("allowAddAssignment", Boolean.valueOf(allowAddAssignment));

		Object allowGradeSubmission = state.getAttribute(STATE_ALLOW_GRADE_SUBMISSION);

		// allow update site?
		boolean allowUpdateSite = SiteService.allowUpdateSite((String) state.getAttribute(STATE_CONTEXT_STRING));
		context.put("allowUpdateSite", Boolean.valueOf(allowUpdateSite));
		
		//group related settings
		context.put("siteAccess", Assignment.AssignmentAccess.SITE);
		context.put("groupAccess", Assignment.AssignmentAccess.GROUPED);
		
		// allow all.groups?
		boolean allowAllGroups = AssignmentService.allowAllGroups(contextString);
		context.put("allowAllGroups", Boolean.valueOf(allowAllGroups));
		
		//Is the review service allowed?
		Site s = null;
		try {
		 s = SiteService.getSite((String) state.getAttribute(STATE_CONTEXT_STRING));
		}
		catch (IdUnusedException iue) {
			M_log.warn(this + ":buildMainPanelContext: Site not found!" + iue.getMessage());
		}
		
		// Check whether content review service is enabled, present and enabled for this site
		getContentReviewService();
		context.put("allowReviewService", allowReviewService && contentReviewService != null && contentReviewService.isSiteAcceptable(s));

		if (allowReviewService && contentReviewService != null && contentReviewService.isSiteAcceptable(s)) {
			//put the review service stings in context
			String reviewServiceName = contentReviewService.getServiceName();
			String reviewServiceTitle = rb.getFormattedMessage("review.title", new Object[]{reviewServiceName});
			String reviewServiceUse = rb.getFormattedMessage("review.use", new Object[]{reviewServiceName});
			String reviewServiceNonElectronic1 = rb.getFormattedMessage("review.switch.ne.1", reviewServiceName);
			String reviewServiceNonElectronic2 = rb.getFormattedMessage("review.switch.ne.2", reviewServiceName);
			context.put("reviewServiceName", reviewServiceName);
			context.put("reviewServiceTitle", reviewServiceTitle);
			context.put("reviewServiceUse", reviewServiceUse);
			context.put("reviewIndicator", rb.getFormattedMessage("review.contentReviewIndicator", new Object[]{reviewServiceName}));
			context.put("reviewSwitchNe1", reviewServiceNonElectronic1);
			context.put("reviewSwitchNe2", reviewServiceNonElectronic2);
		}
		
		//Peer Assessment
		context.put("allowPeerAssessment", allowPeerAssessment);
		
		// grading option
		context.put("withGrade", state.getAttribute(WITH_GRADES));
		
		// the grade type table
		context.put("gradeTypeTable", gradeTypeTable());

        // set the allowSubmitByInstructor option
        context.put("allowSubmitByInstructor", AssignmentService.getAllowSubmitByInstructor());

		// get the system setting for whether to show the Option tool link or not
		context.put("enableViewOption", ServerConfigurationService.getBoolean("assignment.enableViewOption", true));
		
		String mode = (String) state.getAttribute(STATE_MODE);

		if (!MODE_LIST_ASSIGNMENTS.equals(mode))
		{
			// allow grade assignment?
			if (state.getAttribute(STATE_ALLOW_GRADE_SUBMISSION) == null)
			{
				state.setAttribute(STATE_ALLOW_GRADE_SUBMISSION, Boolean.FALSE);
			}
			context.put("allowGradeSubmission", state.getAttribute(STATE_ALLOW_GRADE_SUBMISSION));
		}

		if (MODE_LIST_ASSIGNMENTS.equals(mode))
		{
			// build the context for the student assignment view
			template = build_list_assignments_context(portlet, context, data, state);
		}
		else if (MODE_STUDENT_VIEW_ASSIGNMENT.equals(mode))
		{
			// the student view of assignment
			template = build_student_view_assignment_context(portlet, context, data, state);
		}
                else if (MODE_STUDENT_VIEW_GROUP_ERROR.equals(mode))
                {
                       // disable auto-updates while leaving the list view
                       justDelivered(state);

                       // build the context for showing group submission error
                       template = build_student_view_group_error_context(portlet, context, data, state);
                }
		else if (MODE_STUDENT_VIEW_SUBMISSION.equals(mode))
		{
			// disable auto-updates while leaving the list view
			justDelivered(state);

			// build the context for showing one assignment submission
			template = build_student_view_submission_context(portlet, context, data, state);
		}
		else if (MODE_STUDENT_VIEW_SUBMISSION_CONFIRMATION.equals(mode))
		{
			context.put("site",s);

			// build the context for showing one assignment submission confirmation
			template = build_student_view_submission_confirmation_context(portlet, context, data, state);
		}
		else if (MODE_STUDENT_PREVIEW_SUBMISSION.equals(mode))
		{
			// build the context for showing one assignment submission
			template = build_student_preview_submission_context(portlet, context, data, state);
		}
		else if (MODE_STUDENT_VIEW_GRADE.equals(mode) || MODE_STUDENT_VIEW_GRADE_PRIVATE.equals(mode))
		{
			context.put("site",s);

			// disable auto-updates while leaving the list view
			justDelivered(state);

			if(MODE_STUDENT_VIEW_GRADE_PRIVATE.equals(mode)){
				context.put("privateView", true);
			}
			// build the context for showing one graded submission
			template = build_student_view_grade_context(portlet, context, data, state);
		}
		else if (MODE_INSTRUCTOR_NEW_EDIT_ASSIGNMENT.equals(mode))
		{
			// allow add assignment?
			boolean allowAddSiteAssignment = AssignmentService.allowAddSiteAssignment(contextString);
			context.put("allowAddSiteAssignment", Boolean.valueOf(allowAddSiteAssignment));

			// disable auto-updates while leaving the list view
			justDelivered(state);

			// build the context for the instructor's create new assignment view
			template = build_instructor_new_edit_assignment_context(portlet, context, data, state);
		}
		else if (MODE_INSTRUCTOR_DELETE_ASSIGNMENT.equals(mode))
		{
			if (state.getAttribute(DELETE_ASSIGNMENT_IDS) != null)
			{
				// disable auto-updates while leaving the list view
				justDelivered(state);

				// build the context for the instructor's delete assignment
				template = build_instructor_delete_assignment_context(portlet, context, data, state);
			}
		}
		else if (MODE_INSTRUCTOR_GRADE_ASSIGNMENT.equals(mode))
		{
			if (allowGradeSubmission != null && ((Boolean) allowGradeSubmission).booleanValue())
			{
				// if allowed for grading, build the context for the instructor's grade assignment
				template = build_instructor_grade_assignment_context(portlet, context, data, state);
			}
		}
		else if (MODE_INSTRUCTOR_GRADE_SUBMISSION.equals(mode))
		{
			context.put("site",s);
			if (allowGradeSubmission != null && ((Boolean) allowGradeSubmission).booleanValue())
			{
				// if allowed for grading, disable auto-updates while leaving the list view
				justDelivered(state);

				// build the context for the instructor's grade submission
				template = build_instructor_grade_submission_context(portlet, context, data, state);
			}
		}
		else if (MODE_INSTRUCTOR_PREVIEW_GRADE_SUBMISSION.equals(mode))
		{
			if ( allowGradeSubmission != null && ((Boolean) allowGradeSubmission).booleanValue())
			{
				// if allowed for grading, build the context for the instructor's preview grade submission
				template = build_instructor_preview_grade_submission_context(portlet, context, data, state);
			}
		}
		else if (MODE_INSTRUCTOR_PREVIEW_ASSIGNMENT.equals(mode))
		{
			// build the context for preview one assignment
			template = build_instructor_preview_assignment_context(portlet, context, data, state);
		}
		else if (MODE_INSTRUCTOR_VIEW_ASSIGNMENT.equals(mode))
		{
                        context.put("site",s);
			// disable auto-updates while leaving the list view
			justDelivered(state);

			// build the context for view one assignment
			template = build_instructor_view_assignment_context(portlet, context, data, state);
		}
		else if (MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT.equals(mode))
		{
			if ( allowGradeSubmission != null && ((Boolean) allowGradeSubmission).booleanValue())
			{
				// if allowed for grading, build the context for the instructor's create new assignment view
				template = build_instructor_view_students_assignment_context(portlet, context, data, state);
			}
		}
		else if (MODE_INSTRUCTOR_REPORT_SUBMISSIONS.equals(mode))
		{
			context.put("site",s);
			if ( allowGradeSubmission != null && ((Boolean) allowGradeSubmission).booleanValue())
			{
				// if allowed for grading, build the context for the instructor's view of report submissions
				template = build_instructor_report_submissions(portlet, context, data, state);
			}
		}
		else if (MODE_INSTRUCTOR_DOWNLOAD_ALL.equals(mode))
		{
			if ( allowGradeSubmission != null && ((Boolean) allowGradeSubmission).booleanValue())
			{
				// if allowed for grading, build the context for the instructor's view of uploading all info from archive file
				template = build_instructor_download_upload_all(portlet, context, data, state);
			}
		}
		else if (MODE_INSTRUCTOR_UPLOAD_ALL.equals(mode))
		{
			if ( allowGradeSubmission != null && ((Boolean) allowGradeSubmission).booleanValue())
			{
				// if allowed for grading, build the context for the instructor's view of uploading all info from archive file
				template = build_instructor_download_upload_all(portlet, context, data, state);
			}
		}
		else if (MODE_INSTRUCTOR_REORDER_ASSIGNMENT.equals(mode))
		{
                        context.put("site",s);
                        
			// disable auto-updates while leaving the list view
			justDelivered(state);

			// build the context for the instructor's create new assignment view
			template = build_instructor_reorder_assignment_context(portlet, context, data, state);
		}
		else if (mode.equals(MODE_OPTIONS))
		{
			if (allowUpdateSite)
			{
				// build the options page
				template = build_options_context(portlet, context, data, state);
			}
		}
		else if (mode.equals(MODE_STUDENT_REVIEW_EDIT))
		{
			template = build_student_review_edit_context(portlet, context, data, state);
		}

		if (template == null)
		{
			// default to student list view
			state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
			template = build_list_assignments_context(portlet, context, data, state);
		}

		// this is a check for seeing if there are any assignments.  The check is used to see if we display a Reorder link in the vm files
		if (state.getAttribute(HAS_MULTIPLE_ASSIGNMENTS) != null)
		{
			context.put("assignmentscheck", state.getAttribute(HAS_MULTIPLE_ASSIGNMENTS));
		}
		
		return template;

	} // buildNormalContext

	/**
	 * local function for getting assignment object
	 * @param assignmentId
	 * @param callingFunctionName
	 * @param state
	 * @return
	 */
	private Assignment getAssignment(String assignmentId, String callingFunctionName, SessionState state)
	{
		Assignment rv = null;
		try
		{
			Session session = SessionManager.getCurrentSession();
			SecurityAdvisor secAdv = pushSecurityAdvisor(session, "assignment.security.advisor", false);
			SecurityAdvisor contentAdvisor = pushSecurityAdvisor(session, "assignment.content.security.advisor", false);
			
			rv = AssignmentService.getAssignment(assignmentId);
			
			m_securityService.popAdvisor(contentAdvisor);
			m_securityService.popAdvisor(secAdv);
		}
		catch (IdUnusedException e)
		{
			M_log.warn(this + ":" + callingFunctionName + " " + e.getMessage() + " " + assignmentId);
			addAlert(state, rb.getFormattedMessage("cannotfin_assignment", new Object[]{assignmentId}));
		}
		catch (PermissionException e)
		{
			M_log.warn(this + ":" + callingFunctionName + " " + e.getMessage() + " " + assignmentId);
			addAlert(state, rb.getFormattedMessage("youarenot_viewAssignment", new Object[]{assignmentId}));
		}
		
		return rv;
	}
	
	/**
	 * local function for getting assignment submission object
	 * @param submissionId
	 * @param callingFunctionName
	 * @param state
	 * @return
	 */
	private AssignmentSubmission getSubmission(String submissionId, String callingFunctionName, SessionState state)
	{
		AssignmentSubmission rv = null;
		try
		{
			Session session = SessionManager.getCurrentSession();
			SecurityAdvisor secAdv = pushSecurityAdvisor(session, "assignment.grade.security.advisor", false);
			rv = AssignmentService.getSubmission(submissionId);
			m_securityService.popAdvisor(secAdv);
		}
		catch (IdUnusedException e)
		{
			M_log.warn(this + ":" + callingFunctionName + " " + e.getMessage() + " " + submissionId);
			addAlert(state, rb.getFormattedMessage("cannotfin_submission", new Object[]{submissionId}));
		}
		catch (PermissionException e)
		{
			M_log.warn(this + ":" + callingFunctionName + " " + e.getMessage() + " " + submissionId);
			addAlert(state, rb.getFormattedMessage("youarenot_viewSubmission", new Object[]{submissionId}));
		}
		
		return rv;
	}
	
	/**
	 * local function for editing assignment submission object
	 * @param submissionId
	 * @param callingFunctionName
	 * @param state
	 * @return
	 */
	private AssignmentSubmissionEdit editSubmission(String submissionId, String callingFunctionName, SessionState state)
	{
		AssignmentSubmissionEdit rv = null;
		try
		{
			rv = AssignmentService.editSubmission(submissionId);
		}
		catch (IdUnusedException e)
		{
			M_log.warn(this + ":" + callingFunctionName + " " + e.getMessage() + " " + submissionId);
			addAlert(state, rb.getFormattedMessage("cannotfin_submission", new Object[]{submissionId}));
		}
		catch (PermissionException e)
		{
			M_log.warn(this + ":" + callingFunctionName + " " + e.getMessage() + " " + submissionId);
			addAlert(state, rb.getFormattedMessage("youarenot_editSubmission", new Object[]{submissionId}));
		}
		catch (InUseException e)
		{
			M_log.warn(this + ":" + callingFunctionName + " " + e.getMessage() + " " + submissionId);
			addAlert(state, rb.getFormattedMessage("somelsis_submission", new Object[]{submissionId}));
		}
		
		return rv;
	}
	
	/**
	 * local function for editing assignment object
	 * @param assignmentId
	 * @param callingFunctionName
	 * @param state
	 * @param allowToAdd
	 * @return
	 */
	private AssignmentEdit editAssignment(String assignmentId, String callingFunctionName, SessionState state, boolean allowAdd)
	{
		
		AssignmentEdit rv = null;
		if (assignmentId.length() == 0 && allowAdd)
		{
			// create a new assignment
			try
			{
				rv = AssignmentService.addAssignment((String) state.getAttribute(STATE_CONTEXT_STRING));
			}
			catch (PermissionException e)
			{
				addAlert(state, rb.getString("youarenot_addAssignment"));
				M_log.warn(this + ":" + callingFunctionName + " " + e.getMessage());
			}
		}
		else
		{
			try
			{
				rv = AssignmentService.editAssignment(assignmentId);
			}
			catch (IdUnusedException e)
			{
				M_log.warn(this + ":" + callingFunctionName + " " + e.getMessage() + " " + assignmentId);
				addAlert(state, rb.getFormattedMessage("cannotfin_assignment", new Object[]{assignmentId}));
			}
			catch (PermissionException e)
			{
				M_log.warn(this + ":" + callingFunctionName + " " + e.getMessage() + " " + assignmentId);
				addAlert(state, rb.getFormattedMessage("youarenot_editAssignment", new Object[]{assignmentId}));
			}
			catch (InUseException e)
			{
				M_log.warn(this + ":" + callingFunctionName + " " + e.getMessage() + " " + assignmentId);
				addAlert(state, rb.getFormattedMessage("somelsis_assignment", new Object[]{assignmentId}));
			}
		} // if-else
		
		return rv;
	}
	
	/**
	 * local function for getting assignment submission object
	 * @param submissionId
	 * @param callingFunctionName
	 * @param state
	 * @return
	 */
	private AssignmentSubmission getSubmission(String assignmentRef, User user, String callingFunctionName, SessionState state)
	{
		AssignmentSubmission rv = null;
		try
		{
			rv = AssignmentService.getSubmission(assignmentRef, user);
		}
		catch (IdUnusedException e)
		{
			M_log.warn(this + ":build_student_view_submission " + e.getMessage() + " " + assignmentRef + " " + user.getId());
			if (state != null)
				addAlert(state, rb.getFormattedMessage("cannotfin_submission_1", new Object[]{assignmentRef, user.getId()}));

		}
		catch (PermissionException e)
		{
			M_log.warn(this + ":" + callingFunctionName + " " + e.getMessage() + " " + assignmentRef + " " + user.getId());
			if (state != null)
				addAlert(state, rb.getFormattedMessage("youarenot_viewSubmission_1", new Object[]{assignmentRef, user.getId()}));
		}
		
		return rv;
	}
		
	/**
	 * local function for getting assignment submission object for a group id (or is that submitter id instead of group id)
	 */
	private AssignmentSubmission getSubmission(String assignmentRef, String group_id, String callingFunctionName, SessionState state)
	{
		AssignmentSubmission rv = null;
		try
		{
                    
			rv = AssignmentService.getSubmission(assignmentRef, group_id);
		}
		catch (IdUnusedException e)
		{
			M_log.warn(this + ":build_student_view_submission " + e.getMessage() + " " + assignmentRef + " " + group_id);
			if (state != null)
				addAlert(state, rb.getFormattedMessage("cannotfin_submission_1", new Object[]{assignmentRef, group_id}));

		}
		catch (PermissionException e)
		{
			M_log.warn(this + ":" + callingFunctionName + " " + e.getMessage() + " " + assignmentRef + " " + group_id);
			if (state != null)
				addAlert(state, rb.getFormattedMessage("youarenot_viewSubmission_1", new Object[]{assignmentRef, group_id}));
		}
		
		return rv;
	}
        
	/**
	 * build the student view of showing an assignment submission
	 */
	protected String build_student_view_submission_context(VelocityPortlet portlet, Context context, RunData data,
			SessionState state)
	{
		String invokedByStatus = (String) state.getAttribute(INVOKE);
		if(invokedByStatus!=null){
			if(invokedByStatus.equalsIgnoreCase(INVOKE_BY_LINK)){
				context.put("linkInvoked", Boolean.valueOf(true));	
			}else{
				context.put("linkInvoked", Boolean.valueOf(false));
			}
		}else{
			context.put("linkInvoked", Boolean.valueOf(false));
		}
		String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
		context.put("context", contextString);

		User user = (User) state.getAttribute(STATE_USER);
		M_log.debug(this + " BUILD SUBMISSION FORM WITH USER " + user.getId() + " NAME " + user.getDisplayName());
		String currentAssignmentReference = (String) state.getAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE);
		Assignment assignment = getAssignment(currentAssignmentReference, "build_student_view_submission_context", state);
		AssignmentSubmission s = null;
		boolean newAttachments = false;
		
		if (assignment != null)
		{
			context.put("assignment", assignment);
			context.put("canSubmit", Boolean.valueOf(AssignmentService.canSubmit(contextString, assignment)));
			// SAK-26322
			if (assignment.getContent().getAllowReviewService())
			{
				context.put("plagiarismNote", rb.getFormattedMessage("gen.yoursubwill", contentReviewService.getServiceName()));
				if (!contentReviewService.allowAllContent() && assignmentSubmissionTypeTakesAttachments(assignment))
				{
					context.put("plagiarismFileTypes", rb.getFormattedMessage("gen.onlythefoll", getContentReviewAcceptedFileTypesMessage()));
					context.put("content_review_acceptedMimeTypes", getContentReviewAcceptedMimeTypes());
				}
			}
			if (assignment.getContent().getTypeOfSubmission() == Assignment.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION)
			{
				context.put("nonElectronicType", Boolean.TRUE);
			}

            User submitter = (User)state.getAttribute("student");
            if (submitter == null) {
                submitter = user;
            }
            s = getSubmission(assignment.getReference(), submitter, "build_student_view_submission_context", state);
			List currentAttachments = (List) state.getAttribute(ATTACHMENTS);

			if (s != null)
			{
				M_log.debug(this + " BUILD SUBMISSION FORM HAS SUBMISSION FOR USER " + s.getSubmitterId() + " NAME " + user.getDisplayName());
				context.put("submission", s);
                                if (assignment.isGroup()) {
                                    context.put("selectedGroup", s.getSubmitterId());
                                    context.put("originalGroup", s.getSubmitterId());
                                }
                                
                setScoringAgentProperties(context, assignment, s, false);
                                
				ResourceProperties p = s.getProperties();
				if (p.getProperty(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT) != null)
				{
					context.put("prevFeedbackText", p.getProperty(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT));
				}

				if (p.getProperty(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT) != null)
				{
					context.put("prevFeedbackComment", p.getProperty(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT));
				}
				
				if (p.getProperty(PROP_SUBMISSION_PREVIOUS_FEEDBACK_ATTACHMENTS) != null)
				{
					context.put("prevFeedbackAttachments", getPrevFeedbackAttachments(p));
				}
				
				if (assignment.isGroup()) {
				    context.put("submitterId", s.getSubmitterId() );
				    String grade_override= (StringUtils.trimToNull(assignment.getProperties().getProperty(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT))!=null) && (assignment.getContent().getTypeOfGrade() == Assignment.SCORE_GRADE_TYPE)?
			                s.getGradeForUserInGradeBook(UserDirectoryService.getCurrentUser().getId())!=null?s.getGradeForUserInGradeBook(UserDirectoryService.getCurrentUser().getId()):s.getGradeForUser(UserDirectoryService.getCurrentUser().getId()):s.getGradeForUser(UserDirectoryService.getCurrentUser().getId());
				    if (grade_override != null) {
				            context.put("override", grade_override);
				    }
				}

				// figure out if attachments have been modified

				// the attachments from the previous submission
				List submittedAttachments = s.getSubmittedAttachments();
				newAttachments = areAttachmentsModified(submittedAttachments, currentAttachments);
			}
			else
			{
				// There is no previous submission, attachments are modified if anything has been uploaded
				newAttachments = CollectionUtils.isNotEmpty(currentAttachments);
			}
			
			// put the resubmit information into context
			assignment_resubmission_option_into_context(context, state);

			if (assignment.isGroup()) {
			    context.put("assignmentService", AssignmentService.getInstance());
			    // get current site
			    Collection<Group> groups = null;
			    Site st = null;
			    try {
			        st = SiteService.getSite((String) state.getAttribute(STATE_CONTEXT_STRING));
			        context.put("site", st);
			        groups = getGroupsWithUser(user.getId(), assignment, st);
			        checkForGroupsInMultipleGroups(assignment, groups, state, rb.getString("group.user.multiple.warning"));
			        context.put("group_size", String.valueOf(groups.size()));
			        context.put("groups", new SortedIterator(groups.iterator(), new AssignmentComparator(state, SORTED_BY_GROUP_TITLE, Boolean.TRUE.toString() )));
			        if (state.getAttribute(VIEW_SUBMISSION_GROUP) != null) {
			            context.put("selectedGroup", (String)state.getAttribute(VIEW_SUBMISSION_GROUP));
			            if (M_log.isDebugEnabled()) M_log.debug(this + ":buildStudentViewSubmissionContext: VIEW_SUBMISSION_GROUP " + (String)state.getAttribute(VIEW_SUBMISSION_GROUP)); 
			        }
			        if (state.getAttribute(VIEW_SUBMISSION_ORIGINAL_GROUP) != null) {
			            context.put("originalGroup", (String)state.getAttribute(VIEW_SUBMISSION_ORIGINAL_GROUP));
			            if (M_log.isDebugEnabled()) M_log.debug(this + ":buildStudentViewSubmissionContext: VIEW_SUBMISSION_ORIGINAL_GROUP " + (String)state.getAttribute(VIEW_SUBMISSION_ORIGINAL_GROUP));
			        }
			    }
			    catch (IdUnusedException iue) {
			        M_log.warn(this + ":buildStudentViewSubmissionContext: Site not found!" + iue.getMessage());
			    }
			} 

			// can the student view model answer or not
			canViewAssignmentIntoContext(context, assignment, s);
		}

		TaggingManager taggingManager = (TaggingManager) ComponentManager
				.get("org.sakaiproject.taggable.api.TaggingManager");
		if (taggingManager.isTaggable() && assignment != null)
		{
			addProviders(context, state);
			addActivity(context, assignment);
			context.put("taggable", Boolean.valueOf(true));
		}

		// name value pairs for the vm
		context.put("name_submission_text", VIEW_SUBMISSION_TEXT);
		context.put("value_submission_text", state.getAttribute(VIEW_SUBMISSION_TEXT));
		context.put("name_submission_honor_pledge_yes", VIEW_SUBMISSION_HONOR_PLEDGE_YES);
		context.put("value_submission_honor_pledge_yes", state.getAttribute(VIEW_SUBMISSION_HONOR_PLEDGE_YES));
		context.put("honor_pledge_text", ServerConfigurationService.getString("assignment.honor.pledge", rb.getString("gen.honple2")));
		context.put("attachments", stripInvisibleAttachments(state.getAttribute(ATTACHMENTS)));
		context.put("new_attachments", newAttachments);
    		context.put("userDirectoryService", UserDirectoryService.getInstance());
		
		context.put("contentTypeImageService", state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE));
		context.put("currentTime", TimeService.newTime());

		// SAK-21525 - Groups were not being queried for authz
		boolean allowSubmit = AssignmentService.allowAddSubmissionCheckGroups((String) state.getAttribute(STATE_CONTEXT_STRING),assignment);
		if (!allowSubmit)
		{
			addAlert(state, rb.getString("not_allowed_to_submit"));
		}
		context.put("allowSubmit", Boolean.valueOf(allowSubmit));
		
		// put supplement item into context
		supplementItemIntoContext(state, context, assignment, s);

		initViewSubmissionListOption(state);
		String allOrOneGroup = (String) state.getAttribute(VIEW_SUBMISSION_LIST_OPTION);
		String search = (String) state.getAttribute(VIEW_SUBMISSION_SEARCH);
        Boolean searchFilterOnly = (state.getAttribute(SUBMISSIONS_SEARCH_ONLY) != null && ((Boolean) state.getAttribute(SUBMISSIONS_SEARCH_ONLY)) ? Boolean.TRUE:Boolean.FALSE);

        // if the instructor is allowed to submit assignment on behalf of student, add the student list to the page
		User student = (User) state.getAttribute("student") ;
		if (AssignmentService.getAllowSubmitByInstructor() && student != null) {
			List<String> submitterIds = AssignmentService.getSubmitterIdList(searchFilterOnly.toString(), allOrOneGroup, search, currentAssignmentReference, contextString);
			if (submitterIds != null && !submitterIds.isEmpty() && submitterIds.contains(student.getId())) {
				// we want to come back to the instructor view page
				state.setAttribute(FROM_VIEW, MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT);
				context.put("student",student);
			}
		}
		String template = (String) getContext(data).get("template");
		return template + TEMPLATE_STUDENT_VIEW_SUBMISSION;

	} // build_student_view_submission_context

	/**
	 * Determines if the attachments have been modified
	 * @return true if currentAttachments isn't equal to oldAttachments
	 */
	private boolean areAttachmentsModified(List oldAttachments, List currentAttachments)
	{
		boolean hasCurrent = CollectionUtils.isNotEmpty(currentAttachments);
		boolean hasOld = CollectionUtils.isNotEmpty(oldAttachments);

		if (!hasCurrent)
		{
			//there are no current attachments
			return hasOld;
		}
		if (!hasOld)
		{
			//there are no old attachments (and there are new ones)
			return true;
		}

		Set<String> ids1 = getIdsFromReferences(oldAttachments);
		Set<String> ids2 = getIdsFromReferences(currentAttachments);

		//.equals on Sets of Strings will compare .equals on the contained Strings
		return !ids1.equals(ids2);
	}

	/**
	 * Gets ids from a list of Reference objects. If the List contains any non-reference objects, they are skipped
	 */
	private Set<String> getIdsFromReferences(List references)
	{
		Set<String> ids = new HashSet<String>();
		for (Object reference : references)
		{
			if (reference instanceof Reference)
			{
				Reference casted = (Reference) reference;
				ids.add(casted.getId());
			}
		}

		return ids;
	}

	/**
	 * Returns a clone of the passed in List of attachments minus any attachments that should not be displayed in the UI
	 */
	private List stripInvisibleAttachments(Object attachments)
	{
		List stripped = new ArrayList();
		if (attachments == null || !(attachments instanceof List))
		{
			return stripped;
		}
		Iterator itAttachments = ((List) attachments).iterator();
		while (itAttachments.hasNext())
		{
			Object next = itAttachments.next();
			if (next instanceof Reference)
			{
				Reference attachment = (Reference) next;
				// inline submissions should not show up in the UI's lists of attachments
				if (!"true".equals(attachment.getProperties().getProperty(AssignmentSubmission.PROP_INLINE_SUBMISSION)))
				{
					stripped.add(attachment);
				}
			}
		}
		return stripped;
	}

	/**
	 * Get a list of accepted mime types suitable for an 'accept' attribute in an html file picker
	 * @throws illegal argument exception if the assignment accepts all attachments
	 */
	private String getContentReviewAcceptedMimeTypes()
	{
		if (contentReviewService.allowAllContent())
		{
			throw new IllegalArgumentException("getContentReviewAcceptedMimeTypes invoked, but the content review service accepts all attachments");
		}

		StringBuilder mimeTypes = new StringBuilder();
		Collection<SortedSet<String>> mimeTypesCollection = contentReviewService.getAcceptableExtensionsToMimeTypes().values();
		String delimiter = "";
		for (SortedSet<String> mimeTypesList : mimeTypesCollection)
		{
			for (String mimeType : mimeTypesList)
			{
				mimeTypes.append(delimiter).append(mimeType);
				delimiter = ",";
			}
		}
		return mimeTypes.toString();
	}

	/**
	 * return true if the assignment's submission type takes attachments.
	 * @throws IllegalArgumentException if assignment is null
	 */
	private boolean assignmentSubmissionTypeTakesAttachments(Assignment assignment)
	{
		if (assignment == null)
		{
			throw new IllegalArgumentException("assignmentSubmissionTypeTakesAttachments invoked with assignment = null");
		}
		
		int submissionType = assignment.getContent().getTypeOfSubmission();

		if (submissionType == Assignment.ATTACHMENT_ONLY_ASSIGNMENT_SUBMISSION)
		{
			return true;
		}
		if (submissionType == Assignment.TEXT_AND_ATTACHMENT_ASSIGNMENT_SUBMISSION)
		{
			return true;
		}
		if (submissionType == Assignment.SINGLE_ATTACHMENT_SUBMISSION)
		{
			return true;
		}

		return false;
	}
	

	/**
	 * build the student view of showing a group assignment error with eligible groups
	 * a user can only be in one eligible group
	 * 
	 * @param portlet
	 * @param context
	 * @param data
	 * @param state
	 * @return the student error message for this context
	 */
	protected String build_student_view_group_error_context(VelocityPortlet portlet, Context context, RunData data,
	        SessionState state)
	{
	    String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
	    context.put("context", contextString);

	    User user = (User) state.getAttribute(STATE_USER);
	    if (M_log.isDebugEnabled()) M_log.debug(this + " BUILD SUBMISSION GROUP ERROR WITH USER " + user.getId() + " NAME " + user.getDisplayName());
	    String currentAssignmentReference = (String) state.getAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE);
	    Assignment assignment = getAssignment(currentAssignmentReference, "build_student_view_submission_context", state);

	    if (assignment != null) {
	        context.put("assignment", assignment);

	        if (assignment.isGroup()) {
	            context.put("assignmentService", AssignmentService.getInstance());
	            Collection<Group> groups = null;
	            Site st = null;
	            try {
	                st = SiteService.getSite((String) state.getAttribute(STATE_CONTEXT_STRING));
	                context.put("site", st);
	                groups = getGroupsWithUser(user.getId(), assignment, st);
	                //checkForGroupsInMultipleGroups(assignment, groups, state, rb.getString("group.user.multiple.warning"));
	                context.put("group_size", String.valueOf(groups.size()));
	                context.put("groups", new SortedIterator(groups.iterator(), new AssignmentComparator(state, SORTED_BY_GROUP_TITLE, Boolean.TRUE.toString() )));
	            } catch (IdUnusedException iue) {
	                M_log.warn(this + ":buildStudentViewSubmissionContext: Site not found!" + iue.getMessage());
	            }
	        }
	    }

	    TaggingManager taggingManager = (TaggingManager) ComponentManager.get("org.sakaiproject.taggable.api.TaggingManager");
	    if (taggingManager.isTaggable() && assignment != null) {
	        addProviders(context, state);
	        addActivity(context, assignment);
	        context.put("taggable", Boolean.valueOf(true));
	    }
	    context.put("userDirectoryService", UserDirectoryService.getInstance());
	    context.put("currentTime", TimeService.newTime());

	    String template = (String) getContext(data).get("template");
	    return template + TEMPLATE_STUDENT_VIEW_GROUP_ERROR;
	} // build_student_view_group_error_context


	/**
	 * Get groups containing a user for this assignment (remove SECTION groups)
	 * @param member
	 * @param assignment
	 * @param site
	 * @return collection of groups with the given member
	 */
	private Collection<Group> getGroupsWithUser(String member, Assignment assignment, Site site) {
	    Collection<Group> groups = new ArrayList<Group>();
	    if (assignment.getAccess().equals(Assignment.AssignmentAccess.SITE))
	    {
	        Iterator<Group> _groups = site.getGroupsWithMember(member).iterator();
	        while (_groups.hasNext()) {
	            Group _g = _groups.next();
	            if (_g.getMember(member) != null)// && _g.getProperties().get(GROUP_SECTION_PROPERTY) == null)
	                groups.add(_g);
	        }
	    } else {
	        Iterator<String> _it = assignment.getGroups().iterator();
	        while (_it.hasNext()) {
	            String _gRef = _it.next();
	            Group _g = site.getGroup(_gRef);
	            if (_g != null && _g.getMember(member) != null)// && _g.getProperties().get(GROUP_SECTION_PROPERTY) == null)
	                groups.add(_g);
	        }
	    }
	    return groups;
	}

	/**
	 * build the student view of showing an assignment submission confirmation
	 */
	protected String build_student_view_submission_confirmation_context(VelocityPortlet portlet, Context context, RunData data,
			SessionState state)
	{
		String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
		context.put("context", contextString);
		
		String invokedByStatus = (String) state.getAttribute(INVOKE);
		if(invokedByStatus!=null){
			if(invokedByStatus.equalsIgnoreCase(INVOKE_BY_LINK)){
				context.put("linkInvoked", Boolean.valueOf(true));
				state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
			}else{
				context.put("linkInvoked", Boolean.valueOf(false));
			}
		}else{
			context.put("linkInvoked", Boolean.valueOf(false));
		}
		
		
        context.put("view", MODE_LIST_ASSIGNMENTS);
		// get user information
		User user = (User) state.getAttribute(STATE_USER);
        String submitterId = (String) state.getAttribute(STATE_SUBMITTER);
        User submitter = user;
        if (submitterId != null) {
            try {
                submitter = UserDirectoryService.getUser(submitterId);
            } catch (UserNotDefinedException ex) {
                M_log.warn(this + ":build_student_view_submission cannot find user with id " + submitterId + " " + ex.getMessage());
            }
        }
        context.put("user_name", submitter.getDisplayName());
        context.put("user_id", submitter.getDisplayId());
		if (StringUtils.trimToNull(user.getEmail()) != null)
			context.put("user_email", user.getEmail());
		
		// get site information
		try
		{
			// get current site
			Site site = SiteService.getSite(contextString);
			context.put("site_title", site.getTitle());
		}
		catch (Exception ignore)
		{
			M_log.warn(this + ":buildStudentViewSubmission " + ignore.getMessage() + " siteId= " + contextString);
		}
		
		// get assignment and submission information
		String currentAssignmentReference = (String) state.getAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE);
		Assignment currentAssignment = getAssignment(currentAssignmentReference, "build_student_view_submission_confirmation_context", state);
		if (currentAssignment != null)
		{
                        context.put("assignment", currentAssignment);
			context.put("assignment_title", currentAssignment.getTitle());
			
			// differenciate submission type
			int submissionType = currentAssignment.getContent().getTypeOfSubmission();
			if (submissionType == Assignment.ATTACHMENT_ONLY_ASSIGNMENT_SUBMISSION || submissionType == Assignment.SINGLE_ATTACHMENT_SUBMISSION)
			{
				context.put("attachmentSubmissionOnly", Boolean.TRUE);
			}
			else
			{
				context.put("attachmentSubmissionOnly", Boolean.FALSE);
			}
			if (submissionType == Assignment.TEXT_ONLY_ASSIGNMENT_SUBMISSION)
			{
				context.put("textSubmissionOnly", Boolean.TRUE);
			}
			else
			{
				context.put("textSubmissionOnly", Boolean.FALSE);
			}
			
			context.put("submissionType", submissionType);
			
			AssignmentSubmission s = getSubmission(currentAssignmentReference, submitter, "build_student_view_submission_confirmation_context",state);
			if (s != null)
			{
			    context.put("submission", s); 
				context.put("submitted", Boolean.valueOf(s.getSubmitted()));
				context.put("submission_id", s.getId());
				if (s.getTimeSubmitted() != null)
				{
					context.put("submit_time", s.getTimeSubmitted().toStringLocalFull());
				}
				List attachments = s.getSubmittedAttachments();
				if (attachments != null && attachments.size()>0)
				{
					context.put("submit_attachments", s.getVisibleSubmittedAttachments());
				}
				context.put("submit_text", StringUtils.trimToNull(s.getSubmittedText()));
				context.put("email_confirmation", Boolean.valueOf(ServerConfigurationService.getBoolean("assignment.submission.confirmation.email", true)));
			}
		}	

		state.removeAttribute(STATE_SUBMITTER);
		String template = (String) getContext(data).get("template");
		return template + TEMPLATE_STUDENT_VIEW_SUBMISSION_CONFIRMATION;

	} // build_student_view_submission_confirmation_context
	
	/**
	 * build the student view of assignment
	 * 
	 * @param portlet
	 * @param context
	 * @param data
	 * @param state
	 * @return
	 */
	protected String build_student_view_assignment_context(VelocityPortlet portlet, Context context, RunData data,
			SessionState state)
	{
		context.put("context", state.getAttribute(STATE_CONTEXT_STRING));

		String aReference = (String) state.getAttribute(VIEW_ASSIGNMENT_ID);
		User user = (User) state.getAttribute(STATE_USER);

		AssignmentSubmission submission = null;
		
		Assignment assignment = getAssignment(aReference, "build_student_view_assignment_context", state);
		if (assignment != null)
		{
			context.put("assignment", assignment);

			// put creator information into context
			putCreatorIntoContext(context, assignment);
			
			submission = getSubmission(aReference, user, "build_student_view_assignment_context", state);
			context.put("submission", submission);

			if (assignment.isGroup()) {
			    Collection<Group> groups = null;
			    Site st = null;
			    try {
			        st = SiteService.getSite((String) state.getAttribute(STATE_CONTEXT_STRING));
			        context.put("site", st);
			        groups = getGroupsWithUser(user.getId(), assignment, st);
			        context.put("group_size", String.valueOf(groups.size()));
			        context.put("groups", new SortedIterator(groups.iterator(), new AssignmentComparator(state, SORTED_BY_GROUP_TITLE, Boolean.TRUE.toString() )));
			        checkForGroupsInMultipleGroups(assignment, groups, state, rb.getString("group.user.multiple.warning"));
			    }
			    catch (IdUnusedException iue) {
			        M_log.warn(this + ":buildStudentViewAssignmentContext: Site not found!" + iue.getMessage());
			    }
			}

			// can the student view model answer or not
			canViewAssignmentIntoContext(context, assignment, submission);
			
			// put resubmit information into context
			assignment_resubmission_option_into_context(context, state);
		}

		TaggingManager taggingManager = (TaggingManager) ComponentManager
				.get("org.sakaiproject.taggable.api.TaggingManager");
		if (taggingManager.isTaggable() && assignment != null)
		{
			addProviders(context, state);
			addActivity(context, assignment);
			context.put("taggable", Boolean.valueOf(true));
		}

		context.put("contentTypeImageService", state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE));
		context.put("userDirectoryService", UserDirectoryService.getInstance());

		// put supplement item into context
		supplementItemIntoContext(state, context, assignment, submission);
		
		String template = (String) getContext(data).get("template");
		return template + TEMPLATE_STUDENT_VIEW_ASSIGNMENT;

	} // build_student_view_submission_context

	/**
	 * build the student preview of showing an assignment submission
	 * 
	 * @param portlet
	 * @param context
	 * @param data
	 * @param state
	 * @return
	 */
	protected String build_student_preview_submission_context(VelocityPortlet portlet, Context context, RunData data,
			SessionState state)
	{
		User user = (User) state.getAttribute(STATE_USER);
		String aReference = (String) state.getAttribute(PREVIEW_SUBMISSION_ASSIGNMENT_REFERENCE);

		Assignment assignment = getAssignment(aReference, "build_student_preview_submission_context", state);
		if (assignment != null) {
			context.put("assignment", assignment);
			
			AssignmentSubmission submission = getSubmission(aReference, user, "build_student_preview_submission_context", state);
			context.put("submission", submission);
			
			context.put("canSubmit", Boolean.valueOf(AssignmentService.canSubmit((String) state.getAttribute(STATE_CONTEXT_STRING), assignment)));
			
			setScoringAgentProperties(context, assignment, submission, false);

			// can the student view model answer or not
			canViewAssignmentIntoContext(context, assignment, submission);
			
			// put the resubmit information into context
			assignment_resubmission_option_into_context(context, state);
			
			if (state.getAttribute(SAVED_FEEDBACK) != null)
			{
				context.put("savedFeedback", Boolean.TRUE);
				state.removeAttribute(SAVED_FEEDBACK);
			}
			if (state.getAttribute(OW_FEEDBACK) != null)
			{
				context.put("overwriteFeedback", Boolean.TRUE);
				state.removeAttribute(OW_FEEDBACK);
			}
			if (state.getAttribute(RETURNED_FEEDBACK) != null)
			{
				context.put("returnedFeedback", Boolean.TRUE);
				state.removeAttribute(RETURNED_FEEDBACK);
			}

		}

		context.put("text", state.getAttribute(PREVIEW_SUBMISSION_TEXT));
		context.put("honor_pledge_yes", state.getAttribute(PREVIEW_SUBMISSION_HONOR_PLEDGE_YES));
		context.put("honor_pledge_text", ServerConfigurationService.getString("assignment.honor.pledge", rb.getString("gen.honple2")));
		context.put("attachments", stripInvisibleAttachments(state.getAttribute(PREVIEW_SUBMISSION_ATTACHMENTS)));
		context.put("contentTypeImageService", state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE));

		String template = (String) getContext(data).get("template");
		return template + TEMPLATE_STUDENT_PREVIEW_SUBMISSION;
	} // build_student_preview_submission_context


	private void canViewAssignmentIntoContext(Context context,
			Assignment assignment, AssignmentSubmission submission) {
		boolean canViewModelAnswer = m_assignmentSupplementItemService.canViewModelAnswer(assignment, submission);
		context.put("allowViewModelAnswer", Boolean.valueOf(canViewModelAnswer));
		if (canViewModelAnswer)
		{
			context.put("modelAnswer", m_assignmentSupplementItemService.getModelAnswer(assignment.getId()));
		}
	}
	
	/**
	 * Look up a security advisor from the session with the given key, and then push it on the security service stack.
	 * @param session
	 * @param sessionKey String key used to look up a SecurityAdvisor stored in the session object
	 * @param removeFromSession boolean flag indicating if the value should be removed from the session once retrieved
	 * @return
	 */
	private SecurityAdvisor pushSecurityAdvisor(Session session, String sessionKey, boolean removeFromSession) {
		SecurityAdvisor asgnAdvisor = (SecurityAdvisor)session.getAttribute(sessionKey);
		if (asgnAdvisor != null) {
			m_securityService.pushAdvisor(asgnAdvisor);
			if (removeFromSession)
				session.removeAttribute(sessionKey);
		}
		return asgnAdvisor;
	}
	
	/**
	 * If necessary, put a "decoratedUrlMap" into the context
	 * @param session
	 * @param context Context object that will have a "decoratedUrlMap" object put into it
	 * @param removeFromSession boolean flag indicating if the value should be removed from the session once retrieved
	 */
	private void addDecoUrlMapToContext(Session session, Context context, boolean removeFromSession) {
		SecurityAdvisor contentAdvisor = (SecurityAdvisor)session.getAttribute("assignment.content.security.advisor");
		
		String decoratedContentWrapper = (String)session.getAttribute("assignment.content.decoration.wrapper");
		String[] contentRefs = (String[])session.getAttribute("assignment.content.decoration.wrapper.refs");
		
		if (removeFromSession) {
			session.removeAttribute("assignment.content.decoration.wrapper");
			session.removeAttribute("assignment.content.decoration.wrapper.refs");
		}		

		if (contentAdvisor != null && contentRefs != null) {
			m_securityService.pushAdvisor(contentAdvisor);
			
			Map<String, String> urlMap = new HashMap<String, String>();
			for (String refStr:contentRefs) {
				Reference ref = EntityManager.newReference(refStr);
				String url = ref.getUrl();
				urlMap.put(url, url.replaceFirst("access/content", "access/" + decoratedContentWrapper + "/content"));					
			}
			context.put("decoratedUrlMap", urlMap);
			m_securityService.popAdvisor(contentAdvisor); 
		}
	}

	/**
	 * build the student view of showing a graded submission
	 */
	protected String build_student_view_grade_context(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	{
		context.put("contentTypeImageService", state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE));

		Session session = SessionManager.getCurrentSession();
		addDecoUrlMapToContext(session, context, false);
		SecurityAdvisor asgnAdvisor = pushSecurityAdvisor(session, "assignment.security.advisor", false);
		
		AssignmentSubmission submission = null;
		Assignment assignment = null;
		String submissionId = (String) state.getAttribute(VIEW_GRADE_SUBMISSION_ID);
		submission = getSubmission(submissionId, "build_student_view_grade_context", state);
		if (submission != null)
		{
			assignment = submission.getAssignment();
			context.put("assignment", assignment);
			if (assignment.getContent().getTypeOfSubmission() == Assignment.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION)
			{
				context.put("nonElectronicType", Boolean.TRUE);
			}
			context.put("submission", submission);
			
            if (assignment.isGroup()) {
                String grade_override= (StringUtils.trimToNull(assignment.getProperties().getProperty(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT))!=null) && (assignment.getContent().getTypeOfGrade() == Assignment.SCORE_GRADE_TYPE)?
                		submission.getGradeForUserInGradeBook(UserDirectoryService.getCurrentUser().getId())!=null?submission.getGradeForUserInGradeBook(UserDirectoryService.getCurrentUser().getId()):submission.getGradeForUser(UserDirectoryService.getCurrentUser().getId()):submission.getGradeForUser(UserDirectoryService.getCurrentUser().getId());
                if (grade_override != null) {
                        context.put("override", grade_override);
                }
            }
			// can the student view model answer or not
			canViewAssignmentIntoContext(context, assignment, submission);
			
			// scoring agent integration
			setScoringAgentProperties(context, assignment, submission, false);
			
			//peer review
			if(assignment.getAllowPeerAssessment() 
					&& assignment.getPeerAssessmentStudentViewReviews()
					&& assignment.isPeerAssessmentClosed()){
				List<PeerAssessmentItem> reviews = assignmentPeerAssessmentService.getPeerAssessmentItems(submission.getId(), assignment.getContent().getFactor());
				if(reviews != null){
					List<PeerAssessmentItem> completedReviews = new ArrayList<PeerAssessmentItem>();
					for(PeerAssessmentItem review : reviews){
						if(!review.isRemoved() && (review.getScore() != null || (review.getComment() != null && !"".equals(review.getComment().trim())))){
							//only show peer reviews that have either a score or a comment saved
							if(assignment.getPeerAssessmentAnonEval()){
								//annonymous eval
								review.setAssessorDisplayName(rb.getFormattedMessage("gen.reviewer.countReview", completedReviews.size() + 1));
							}else{
								//need to set the assessor's display name
								try {
									review.setAssessorDisplayName(UserDirectoryService.getUser(review.getAssessorUserId()).getDisplayName());
								} catch (UserNotDefinedException e) {
									//reviewer doesn't exist or userId is wrong
									M_log.error(e.getMessage(), e);
									//set a default one:
									review.setAssessorDisplayName(rb.getFormattedMessage("gen.reviewer.countReview", completedReviews.size() + 1));
								}
							}
							// get attachments for peer review item
							List<PeerAssessmentAttachment> attachments = assignmentPeerAssessmentService.getPeerAssessmentAttachments(review.getSubmissionId(),review.getAssessorUserId());
							if(attachments != null && !attachments.isEmpty()) {
								List<Reference> attachmentRefList = new ArrayList<>();
								for(PeerAssessmentAttachment attachment : attachments) {
									try {
										Reference ref = m_entityManager.newReference(m_contentHostingService.getReference(attachment.getResourceId()));
										attachmentRefList.add(ref);
									} catch(Exception e) {
										M_log.warn(e.getMessage(), e);
									}
								}
								if(attachmentRefList != null && !attachmentRefList.isEmpty())
									review.setAttachmentRefList(attachmentRefList);
							}
							completedReviews.add(review);
							
						}
					}
					if(completedReviews.size() > 0){
						context.put("peerReviews", completedReviews);
					}
				}
			}
		}

		TaggingManager taggingManager = (TaggingManager) ComponentManager
				.get("org.sakaiproject.taggable.api.TaggingManager");
		if (taggingManager.isTaggable() && submission != null)
		{
			AssignmentActivityProducer assignmentActivityProducer = (AssignmentActivityProducer) ComponentManager
					.get("org.sakaiproject.assignment.taggable.api.AssignmentActivityProducer");
			List<DecoratedTaggingProvider> providers = addProviders(context, state);
			List<TaggingHelperInfo> itemHelpers = new ArrayList<TaggingHelperInfo>();
			for (DecoratedTaggingProvider provider : providers)
			{
				TaggingHelperInfo helper = provider.getProvider()
						.getItemHelperInfo(
								assignmentActivityProducer.getItem(
										submission,
										UserDirectoryService.getCurrentUser()
												.getId()).getReference());
				if (helper != null)
				{
					itemHelpers.add(helper);
				}
			}
			addItem(context, submission, UserDirectoryService.getCurrentUser().getId());
			addActivity(context, submission.getAssignment());
			context.put("itemHelpers", itemHelpers);
			context.put("taggable", Boolean.valueOf(true));
		}

		// put supplement item into context
		supplementItemIntoContext(state, context, assignment, submission);
		
		if (asgnAdvisor != null) {
			m_securityService.popAdvisor(asgnAdvisor);
		}
		
		String template = (String) getContext(data).get("template");
		return template + TEMPLATE_STUDENT_VIEW_GRADE;

	} // build_student_view_grade_context

	/**
	 * build the view of assignments list
	 */
	protected String build_list_assignments_context(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	{
		TaggingManager taggingManager = (TaggingManager) ComponentManager
				.get("org.sakaiproject.taggable.api.TaggingManager");
		if (taggingManager.isTaggable())
		{
			context.put("producer", ComponentManager
					.get("org.sakaiproject.assignment.taggable.api.AssignmentActivityProducer"));
			context.put("providers", taggingManager.getProviders());
			context.put("taggable", Boolean.valueOf(true));
		}
		
		String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
		context.put("contextString", contextString);
		context.put("user", state.getAttribute(STATE_USER));
		context.put("service", AssignmentService.getInstance());
		context.put("AuthzGroupService", authzGroupService);
		context.put("TimeService", TimeService.getInstance());
		context.put("LongObject", Long.valueOf(TimeService.newTime().getTime()));
		context.put("currentTime", TimeService.newTime());
		String sortedBy = (String) state.getAttribute(SORTED_BY);
		String sortedAsc = (String) state.getAttribute(SORTED_ASC);
		// clean sort criteria
		if (SORTED_BY_GROUP_TITLE.equals(sortedBy) || SORTED_BY_GROUP_DESCRIPTION.equals(sortedBy))
		{
			sortedBy = SORTED_BY_DUEDATE;
			sortedAsc = Boolean.TRUE.toString();
			state.setAttribute(SORTED_BY, sortedBy);
			state.setAttribute(SORTED_ASC, sortedAsc);
		}
		context.put("sortedBy", sortedBy);
		context.put("sortedAsc", sortedAsc);
		
		if (state.getAttribute(STATE_SELECTED_VIEW) != null &&
				// this is not very elegant, but the view cannot be 'lisofass2' here.
				!state.getAttribute(STATE_SELECTED_VIEW).equals(MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT))
		{
			context.put("view", state.getAttribute(STATE_SELECTED_VIEW));
		}

		List assignments = prepPage(state);
		context.put("assignments", assignments.iterator());

		// allow add assignment?
		Map<String, List<PeerAssessmentItem>> peerAssessmentItemsMap = new HashMap<String, List<PeerAssessmentItem>>();
		boolean allowAddAssignment = AssignmentService.allowAddAssignment(contextString);
		if(!allowAddAssignment){
			//this is the same requirement for displaying the assignment link for students
			//now lets create a map for peer reviews for each eligible assignment
			for(Assignment assignment : (List<Assignment>) assignments){
				if(assignment.getAllowPeerAssessment() && (assignment.isPeerAssessmentOpen() || assignment.isPeerAssessmentClosed())){
					peerAssessmentItemsMap.put(assignment.getId(), assignmentPeerAssessmentService.getPeerAssessmentItems(assignment.getId(), UserDirectoryService.getCurrentUser().getId(), assignment.getContent().getFactor()));
				}
			}
		}
		context.put("peerAssessmentItemsMap", peerAssessmentItemsMap);
		
		// allow get assignment
		context.put("allowGetAssignment", Boolean.valueOf(AssignmentService.allowGetAssignment(contextString)));
		
		// test whether user user can grade at least one assignment
		// and update the state variable.
		boolean allowGradeSubmission = false;
		for (Iterator aIterator=assignments.iterator(); !allowGradeSubmission && aIterator.hasNext(); )
		{
			if (AssignmentService.allowGradeSubmission(((Assignment) aIterator.next()).getReference()))
			{
				allowGradeSubmission = true;
			}
		}
		state.setAttribute(STATE_ALLOW_GRADE_SUBMISSION, Boolean.valueOf(allowGradeSubmission));
		context.put("allowGradeSubmission", state.getAttribute(STATE_ALLOW_GRADE_SUBMISSION));

		// allow remove assignment?
		boolean allowRemoveAssignment = false;
		for (Iterator aIterator=assignments.iterator(); !allowRemoveAssignment && aIterator.hasNext(); )
		{
			if (AssignmentService.allowRemoveAssignment(((Assignment) aIterator.next()).getReference()))
			{
				allowRemoveAssignment = true;
			}
		}
		context.put("allowRemoveAssignment", Boolean.valueOf(allowRemoveAssignment));

		add2ndToolbarFields(data, context);

		// inform the observing courier that we just updated the page...
		// if there are pending requests to do so they can be cleared
		justDelivered(state);

		pagingInfoToContext(state, context);

		// put site object into context
		try
		{
			// get current site
			Site site = SiteService.getSite(contextString);
			context.put("site", site);
			// any group in the site?
			Collection groups = getAllGroupsInSite(contextString);
			
			context.put("groups", (groups != null && groups.size()>0)?Boolean.TRUE:Boolean.FALSE);

			// add active user list
			AuthzGroup realm = authzGroupService.getAuthzGroup(SiteService.siteReference(contextString));
			if (realm != null)
			{
				context.put("activeUserIds", realm.getUsers());
			}
		}
		catch (Exception ignore)
		{
			M_log.warn(this + ":build_list_assignments_context " + ignore.getMessage());
			M_log.warn(this + ignore.getMessage() + " siteId= " + contextString);
		}

		boolean allowSubmit = AssignmentService.allowAddSubmission(contextString);
		context.put("allowSubmit", Boolean.valueOf(allowSubmit));
		
		// related to resubmit settings
		context.put("allowResubmitNumberProp", AssignmentSubmission.ALLOW_RESUBMIT_NUMBER);
		context.put("allowResubmitCloseTimeProp", AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME);
		
		// the type int for non-electronic submission
		context.put("typeNonElectronic", Integer.valueOf(Assignment.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION));

		// show or hide the number of submission column
		context.put(SHOW_NUMBER_SUBMISSION_COLUMN, state.getAttribute(SHOW_NUMBER_SUBMISSION_COLUMN));

		// clear out peer_attachment list just in case
		state.setAttribute(PEER_ATTACHMENTS, m_entityManager.newReferenceList());
		context.put(PEER_ATTACHMENTS, m_entityManager.newReferenceList());

		String template = (String) getContext(data).get("template");
		return template + TEMPLATE_LIST_ASSIGNMENTS;

	} // build_list_assignments_context
	
	private HashSet<String> getSubmittersIdSet(List submissions)
	{
		HashSet<String> rv = new HashSet<String>();
		for (Iterator iSubmissions=submissions.iterator(); iSubmissions.hasNext();)
		{
                    rv.add(((AssignmentSubmission) iSubmissions.next()).getSubmitterId());
		}
		return rv;
	}
	
	private HashSet<String> getAllowAddSubmissionUsersIdSet(List users)
	{
		HashSet<String> rv = new HashSet<String>();
		for (Iterator iUsers=users.iterator(); iUsers.hasNext();)
		{
			rv.add(((User) iUsers.next()).getId());
		}
		return rv;
	}

	/**
	 * build the instructor view of creating a new assignment or editing an existing one
	 */
	protected String build_instructor_new_edit_assignment_context(VelocityPortlet portlet, Context context, RunData data,
			SessionState state)
	{
		// If the user adds the schedule or alternate calendar tool after using the assignment tool,
		// we need to remove these state attributes so they are re-initialized with the updated
		// availability of the tools.
		state.removeAttribute(CALENDAR_TOOL_EXIST);
		state.removeAttribute(ADDITIONAL_CALENDAR_TOOL_READY);
		initState(state, portlet, (JetspeedRunData)data);

		// Anon grading enabled/disabled
		context.put( "enableAnonGrading", ServerConfigurationService.getBoolean( SAK_PROP_ENABLE_ANON_GRADING, false ) );
		
		// is the assignment an new assignment
		String assignmentId = (String) state.getAttribute(EDIT_ASSIGNMENT_ID);
		if (assignmentId != null)
		{
			Assignment a = getAssignment(assignmentId, "build_instructor_new_edit_assignment_context", state);
			if (a != null)
			{
				context.put("assignment", a);
				if (a.isGroup()) {
                                    Collection<String> _dupUsers = usersInMultipleGroups(a);
                                    if (_dupUsers.size() > 0) context.put("multipleGroupUsers", _dupUsers);
			}
		}
		}

		// set up context variables
		setAssignmentFormContext(state, context);

		context.put("fField", state.getAttribute(NEW_ASSIGNMENT_FOCUS));

                context.put("group_submissions_enabled", Boolean.valueOf(ServerConfigurationService.getBoolean("assignment.group.submission.enabled", true)));
		context.put("visible_date_enabled", Boolean.valueOf(ServerConfigurationService.getBoolean("assignment.visible.date.enabled", false)));
			
		String sortedBy = (String) state.getAttribute(SORTED_BY);
		String sortedAsc = (String) state.getAttribute(SORTED_ASC);
		context.put("sortedBy", sortedBy);
		context.put("sortedAsc", sortedAsc);

		String template = (String) getContext(data).get("template");
		return template + TEMPLATE_INSTRUCTOR_NEW_EDIT_ASSIGNMENT;

	} // build_instructor_new_assignment_context

	protected void setAssignmentFormContext(SessionState state, Context context)
	{
		// put the names and values into vm file
		
		context.put("name_UsePeerAssessment", NEW_ASSIGNMENT_USE_PEER_ASSESSMENT);
		context.put("name_additionalOptions", NEW_ASSIGNMENT_ADDITIONAL_OPTIONS);
		context.put("name_PeerAssessmentAnonEval", NEW_ASSIGNMENT_PEER_ASSESSMENT_ANON_EVAL);
		context.put("name_PeerAssessmentStudentViewReviews", NEW_ASSIGNMENT_PEER_ASSESSMENT_STUDENT_VIEW_REVIEWS);
		context.put("name_PeerAssessmentNumReviews", NEW_ASSIGNMENT_PEER_ASSESSMENT_NUM_REVIEWS);
		context.put("name_PeerAssessmentInstructions", NEW_ASSIGNMENT_PEER_ASSESSMENT_INSTRUCTIONS);
		context.put("name_UseReviewService", NEW_ASSIGNMENT_USE_REVIEW_SERVICE);
		context.put("name_AllowStudentView", NEW_ASSIGNMENT_ALLOW_STUDENT_VIEW);
		context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_RADIO", NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_RADIO);
		context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_NONE", NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_NONE);
		context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_STANDARD", NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_STANDARD);
		context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_INSITUTION", NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_INSITUTION);
		context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_RADIO", NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_RADIO);
		context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_IMMEDIATELY", NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_IMMEDIATELY);
		context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_DUE", NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_DUE);
		context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_TURNITIN", NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_TURNITIN);
		context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INTERNET", NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INTERNET);
		context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_PUB", NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_PUB);
		context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INSTITUTION", NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INSTITUTION);
		context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_BIBLIOGRAPHIC", NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_BIBLIOGRAPHIC);
		context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_QUOTED", NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_QUOTED);
		context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_SMALL_MATCHES", NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_SMALL_MATCHES);
		context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_TYPE", NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_TYPE);
		context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_VALUE", NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_VALUE);
		
		
		context.put("name_title", NEW_ASSIGNMENT_TITLE);
		context.put("name_order", NEW_ASSIGNMENT_ORDER);

		// set open time context variables
		putTimePropertiesInContext(context, state, "Open", NEW_ASSIGNMENT_OPENMONTH, NEW_ASSIGNMENT_OPENDAY, NEW_ASSIGNMENT_OPENYEAR, NEW_ASSIGNMENT_OPENHOUR, NEW_ASSIGNMENT_OPENMIN);
		
		// set visible time context variables
                if (Boolean.valueOf(ServerConfigurationService.getBoolean("assignment.visible.date.enabled", false))) {
                    putTimePropertiesInContext(context, state, "Visible", NEW_ASSIGNMENT_VISIBLEMONTH, NEW_ASSIGNMENT_VISIBLEDAY, NEW_ASSIGNMENT_VISIBLEYEAR, NEW_ASSIGNMENT_VISIBLEHOUR, NEW_ASSIGNMENT_VISIBLEMIN);
                    context.put(NEW_ASSIGNMENT_VISIBLETOGGLE, ((Boolean) state.getAttribute(NEW_ASSIGNMENT_VISIBLETOGGLE)));
                }
                
		// set due time context variables
		putTimePropertiesInContext(context, state, "Due", NEW_ASSIGNMENT_DUEMONTH, NEW_ASSIGNMENT_DUEDAY, NEW_ASSIGNMENT_DUEYEAR, NEW_ASSIGNMENT_DUEHOUR, NEW_ASSIGNMENT_DUEMIN);

		context.put("name_EnableCloseDate", NEW_ASSIGNMENT_ENABLECLOSEDATE);
		// set close time context variables
		putTimePropertiesInContext(context, state, "Close", NEW_ASSIGNMENT_CLOSEMONTH, NEW_ASSIGNMENT_CLOSEDAY, NEW_ASSIGNMENT_CLOSEYEAR, NEW_ASSIGNMENT_CLOSEHOUR, NEW_ASSIGNMENT_CLOSEMIN);

		context.put("name_Section", NEW_ASSIGNMENT_SECTION);
		context.put("name_SubmissionType", NEW_ASSIGNMENT_SUBMISSION_TYPE);
		context.put("name_Category", NEW_ASSIGNMENT_CATEGORY);
		context.put("name_GradeType", NEW_ASSIGNMENT_GRADE_TYPE);
		context.put("name_GradePoints", NEW_ASSIGNMENT_GRADE_POINTS);
		context.put("name_Description", NEW_ASSIGNMENT_DESCRIPTION);
		// do not show the choice when there is no Schedule tool yet
		if (state.getAttribute(CALENDAR) != null || state.getAttribute(ADDITIONAL_CALENDAR) != null)
			context.put("name_CheckAddDueDate", ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE);

		context.put("name_CheckHideDueDate", NEW_ASSIGNMENT_CHECK_HIDE_DUE_DATE);
		//don't show the choice when there is no Announcement tool yet
		if (state.getAttribute(ANNOUNCEMENT_CHANNEL) != null) {
			context.put("name_CheckAutoAnnounce", ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE);
			context.put("name_OpenDateNotification", Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION);
		}
		context.put("name_CheckAddHonorPledge", NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE);

		// SAK-17606
		context.put("name_CheckAnonymousGrading", NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING);

		context.put("name_CheckIsGroupSubmission", NEW_ASSIGNMENT_GROUP_SUBMIT);
		//Default value of additional options for now. It's a radio so it can only have one option
		String contextAdditionalOptions = "none";

		String gs = (String) state.getAttribute(NEW_ASSIGNMENT_GROUP_SUBMIT);
		if (gs != null && "1".equals(gs)) {
			contextAdditionalOptions = "group";
		}

		// set the values
		Assignment a = null;
		String assignmentRef = (String) state.getAttribute(EDIT_ASSIGNMENT_ID);
		if (assignmentRef != null)
		{
			a = getAssignment(assignmentRef, "setAssignmentFormContext", state);

		}
		
		// put the re-submission info into context
		putTimePropertiesInContext(context, state, "Resubmit", ALLOW_RESUBMIT_CLOSEMONTH, ALLOW_RESUBMIT_CLOSEDAY, ALLOW_RESUBMIT_CLOSEYEAR, ALLOW_RESUBMIT_CLOSEHOUR, ALLOW_RESUBMIT_CLOSEMIN);
		
		context.put("value_year_from", state.getAttribute(NEW_ASSIGNMENT_YEAR_RANGE_FROM));
		context.put("value_year_to", state.getAttribute(NEW_ASSIGNMENT_YEAR_RANGE_TO));
		context.put("value_title", state.getAttribute(NEW_ASSIGNMENT_TITLE));
		context.put("value_position_order", state.getAttribute(NEW_ASSIGNMENT_ORDER));

		context.put("value_EnableCloseDate", state.getAttribute(NEW_ASSIGNMENT_ENABLECLOSEDATE));

		context.put("value_Sections", state.getAttribute(NEW_ASSIGNMENT_SECTION));
		context.put("value_SubmissionType", state.getAttribute(NEW_ASSIGNMENT_SUBMISSION_TYPE));
		
		// information related to gradebook categories
		putGradebookCategoryInfoIntoContext(state, context);
		
		context.put("value_totalSubmissionTypes", Assignment.SUBMISSION_TYPES.length);
		context.put("value_GradeType", state.getAttribute(NEW_ASSIGNMENT_GRADE_TYPE));
		// format to show one decimal place
		String maxGrade = (String) state.getAttribute(NEW_ASSIGNMENT_GRADE_POINTS);
		context.put("value_GradePoints", displayGrade(state, maxGrade, a != null ? a.getContent().getFactor() : AssignmentService.getScaleFactor()));
		context.put("value_Description", state.getAttribute(NEW_ASSIGNMENT_DESCRIPTION));
		
		// SAK-17606
		context.put("value_CheckAnonymousGrading", state.getAttribute(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING));
		
		//Peer Assessment
		String peer = (String) state.getAttribute(NEW_ASSIGNMENT_USE_PEER_ASSESSMENT);
		if (peer != null && "true".equals(peer)) {
			contextAdditionalOptions = "peerreview";
		}
		context.put("value_PeerAssessmentAnonEval", state.getAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_ANON_EVAL));
		context.put("value_PeerAssessmentStudentViewReviews", state.getAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_STUDENT_VIEW_REVIEWS));
		context.put("value_PeerAssessmentNumReviews", state.getAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_NUM_REVIEWS));
		context.put("value_PeerAssessmentInstructions", state.getAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_INSTRUCTIONS));
		putTimePropertiesInContext(context, state, "PeerPeriod", NEW_ASSIGNMENT_PEERPERIODMONTH, NEW_ASSIGNMENT_PEERPERIODDAY, NEW_ASSIGNMENT_PEERPERIODYEAR, NEW_ASSIGNMENT_PEERPERIODHOUR, NEW_ASSIGNMENT_PEERPERIODMIN);
		
		// Keep the use review service setting
		context.put("value_UseReviewService", state.getAttribute(NEW_ASSIGNMENT_USE_REVIEW_SERVICE));
		if (!contentReviewService.allowAllContent())
		{
			String fileTypesMessage = getContentReviewAcceptedFileTypesMessage();
			String contentReviewNote = rb.getFormattedMessage("content_review.note", new Object[]{fileTypesMessage});
			context.put("content_review_note", contentReviewNote);
		}
		context.put("turnitin_forceSingleAttachment", ServerConfigurationService.getBoolean("turnitin.forceSingleAttachment", false));
		//Rely on the deprecated "turnitin.allowStudentView.default" setting if set, otherwise use "contentreview.allowStudentView.default"
		boolean defaultAllowStudentView = ServerConfigurationService.getBoolean("turnitin.allowStudentView.default", ServerConfigurationService.getBoolean("contentreview.allowStudentView.default", Boolean.FALSE));
		context.put("value_AllowStudentView", state.getAttribute(NEW_ASSIGNMENT_ALLOW_STUDENT_VIEW) == null ? Boolean.toString(defaultAllowStudentView) : state.getAttribute(NEW_ASSIGNMENT_ALLOW_STUDENT_VIEW));
		
		List<String> subOptions = getSubmissionRepositoryOptions();
		String submitRadio = ServerConfigurationService.getString("turnitin.repository.setting.value",null) == null ? NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_NONE : ServerConfigurationService.getString("turnitin.repository.setting.value");
		if(state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_RADIO) != null && subOptions.contains(state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_RADIO)))
			submitRadio = state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_RADIO).toString();		
		context.put("value_NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_RADIO", submitRadio);
		context.put("show_NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT", subOptions);
		
		List<String> reportGenOptions = getReportGenOptions();
		String reportRadio = ServerConfigurationService.getString("turnitin.report_gen_speed.setting.value", null) == null ? NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_IMMEDIATELY : ServerConfigurationService.getString("turnitin.report_gen_speed.setting.value");
		if(state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_RADIO) != null && reportGenOptions.contains(state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_RADIO)))
			reportRadio = state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_RADIO).toString();	
		context.put("value_NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_RADIO", reportRadio);
		context.put("show_NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT", reportGenOptions);

		context.put("show_NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_TURNITIN", ServerConfigurationService.getBoolean("turnitin.option.s_paper_check", true));
		context.put("show_NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INTERNET", ServerConfigurationService.getBoolean("turnitin.option.internet_check", true));
		context.put("show_NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_PUB", ServerConfigurationService.getBoolean("turnitin.option.journal_check", true));
		context.put("show_NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INSTITUTION", ServerConfigurationService.getBoolean("turnitin.option.institution_check", false));
		
		context.put("value_NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_TURNITIN", (state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_TURNITIN) == null) ? Boolean.toString(ServerConfigurationService.getBoolean("turnitin.option.s_paper_check.default", ServerConfigurationService.getBoolean("turnitin.option.s_paper_check", true) ? true : false)) : state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_TURNITIN));
		context.put("value_NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INTERNET", state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INTERNET) == null ? Boolean.toString(ServerConfigurationService.getBoolean("turnitin.option.internet_check.default", ServerConfigurationService.getBoolean("turnitin.option.internet_check", true) ? true : false)) : state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INTERNET));
		context.put("value_NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_PUB", state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_PUB) == null ? Boolean.toString(ServerConfigurationService.getBoolean("turnitin.option.journal_check.default", ServerConfigurationService.getBoolean("turnitin.option.journal_check", true) ? true : false)) : state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_PUB));
		context.put("value_NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INSTITUTION", state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INSTITUTION) == null ? Boolean.toString(ServerConfigurationService.getBoolean("turnitin.option.institution_check.default", ServerConfigurationService.getBoolean("turnitin.option.institution_check", true) ? true : false)) : state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INSTITUTION));

		//exclude bibliographic materials
		context.put("show_NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_BIBLIOGRAPHIC", ServerConfigurationService.getBoolean("turnitin.option.exclude_bibliographic", true));
		context.put("value_NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_BIBLIOGRAPHIC", (state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_BIBLIOGRAPHIC) == null) ? Boolean.toString(ServerConfigurationService.getBoolean("turnitin.option.exclude_bibliographic.default", ServerConfigurationService.getBoolean("turnitin.option.exclude_bibliographic", true) ? true : false)) : state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_BIBLIOGRAPHIC));
		
		//exclude quoted materials
		//Rely on the deprecated "turnitin.option.exclude_quoted" setting if set, otherwise use "contentreview.option.exclude_quoted"
		boolean showExcludeQuoted = ServerConfigurationService.getBoolean("turnitin.option.exclude_quoted", ServerConfigurationService.getBoolean("contentreview.option.exclude_quoted", Boolean.TRUE));
		context.put("show_NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_QUOTED", showExcludeQuoted);
		//Rely on the deprecated "turnitin.option.exclude_quoted.default" setting if set, otherwise use "contentreview.option.exclude_quoted.default"
		boolean defaultExcludeQuoted = ServerConfigurationService.getBoolean("turnitin.option.exclude_quoted.default", ServerConfigurationService.getBoolean("contentreview.option.exclude_quoted.default", showExcludeQuoted));
		context.put("value_NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_QUOTED", (state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_QUOTED) == null) ? Boolean.toString(defaultExcludeQuoted) : state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_QUOTED));
		
		//exclude quoted materials
		boolean displayExcludeType = ServerConfigurationService.getBoolean("turnitin.option.exclude_smallmatches", true);
		context.put("show_NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_SMALL_MATCHES", displayExcludeType);
		if(displayExcludeType){
			context.put("value_NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_TYPE", (state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_TYPE) == null) ? Integer.toString(ServerConfigurationService.getInt("turnitin.option.exclude_type.default", 0)) : state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_TYPE));
			context.put("value_NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_VALUE", (state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_VALUE) == null) ? Integer.toString(ServerConfigurationService.getInt("turnitin.option.exclude_value.default", 1)) : state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_VALUE));
		}
		
		// don't show the choice when there is no Schedule tool yet
		if (state.getAttribute(CALENDAR) != null || state.getAttribute(ADDITIONAL_CALENDAR) != null)
			context.put("value_CheckAddDueDate", state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE));
		
		context.put("value_CheckHideDueDate", state.getAttribute(NEW_ASSIGNMENT_CHECK_HIDE_DUE_DATE));
		
		// don't show the choice when there is no Announcement tool yet
		if (state.getAttribute(ANNOUNCEMENT_CHANNEL) != null) {
			context.put("value_CheckAutoAnnounce", state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE));
			context.put("value_OpenDateNotification", state.getAttribute(Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION));
			// the option values
			context.put("value_opendate_notification_none", Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION_NONE);
			context.put("value_opendate_notification_low", Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION_LOW);
			context.put("value_opendate_notification_high", Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION_HIGH);
		}
		
		String s = (String) state.getAttribute(NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE);
		if (s == null) s = "1";
		context.put("value_CheckAddHonorPledge", s);
		
		// put resubmission option into context
		assignment_resubmission_option_into_context(context, state);

		// get all available assignments from Gradebook tool except for those created fromcategoryTable
		boolean gradebookExists = isGradebookDefined();
		if (gradebookExists)
		{	
			GradebookService g = (GradebookService)  ComponentManager.get("org.sakaiproject.service.gradebook.GradebookService");
			String gradebookUid = ToolManager.getInstance().getCurrentPlacement().getContext();

			try
			{
				// how many gradebook assignment have been integrated with Assignment tool already
				currentAssignmentGradebookIntegrationIntoContext(context, state, g, gradebookUid, a != null ? a.getTitle() : null);
			
				if (StringUtils.trimToNull((String) state.getAttribute(AssignmentService.NEW_ASSIGNMENT_ADD_TO_GRADEBOOK)) == null)
				{
					state.setAttribute(AssignmentService.NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, AssignmentService.GRADEBOOK_INTEGRATION_NO);
				}
				
				context.put("withGradebook", Boolean.TRUE);
				
				// offer the gradebook integration choice only in the Assignments with Grading tool
				boolean withGrade = ((Boolean) state.getAttribute(WITH_GRADES)).booleanValue();
				if (withGrade)
				{
					context.put("name_Addtogradebook", AssignmentService.NEW_ASSIGNMENT_ADD_TO_GRADEBOOK);
					context.put("name_AssociateGradebookAssignment", AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
				}
				
				context.put("gradebookChoice", state.getAttribute(AssignmentService.NEW_ASSIGNMENT_ADD_TO_GRADEBOOK));
				context.put("gradebookChoice_no", AssignmentService.GRADEBOOK_INTEGRATION_NO);
				context.put("gradebookChoice_add", AssignmentService.GRADEBOOK_INTEGRATION_ADD);
				context.put("gradebookChoice_associate", AssignmentService.GRADEBOOK_INTEGRATION_ASSOCIATE);
				String associateGradebookAssignment = (String) state.getAttribute(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
				if (associateGradebookAssignment != null)
				{
					context.put("associateGradebookAssignment", associateGradebookAssignment);
					if (a != null)
					{
						context.put("noAddToGradebookChoice", 
								Boolean.valueOf(associateGradebookAssignment.equals(a.getReference()) || g.isAssignmentDefined(gradebookUid, a.getTitle())));
					}
				}
			}
			catch (Exception e)
			{
				// not able to link to Gradebook
				M_log.warn(this + "setAssignmentFormContext " + e.getMessage());
			}
			
			if (StringUtils.trimToNull((String) state.getAttribute(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK)) == null)
			{
				state.setAttribute(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, AssignmentService.GRADEBOOK_INTEGRATION_NO);
			}
		}

		context.put("monthTable", monthTable());
		context.put("submissionTypeTable", submissionTypeTable());
		context.put("attachments", state.getAttribute(ATTACHMENTS));
		context.put("contentTypeImageService", state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE));

		String range = StringUtils.trimToNull((String) state.getAttribute(NEW_ASSIGNMENT_RANGE));
		context.put("range", range != null?range:"site");
		
		String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
		// put site object into context
		try
		{
			// get current site
			Site site = SiteService.getSite((String) state.getAttribute(STATE_CONTEXT_STRING));
			context.put("site", site);
		}
		catch (Exception ignore)
		{
			M_log.warn(this + ":setAssignmentFormContext " + ignore.getMessage());
		}

		if (AssignmentService.getAllowGroupAssignments())
		{
			Collection groupsAllowAddAssignment = AssignmentService.getGroupsAllowAddAssignment(contextString);
			if (a != null && a.isGroup()) {
                            List _valid_groups = new ArrayList();
                            Iterator<Group> _it = groupsAllowAddAssignment.iterator();
                            while (_it.hasNext()) {
                                Group _group = _it.next();
                                //if (_group.getProperties().get(GROUP_SECTION_PROPERTY) == null) {
                                    _valid_groups.add(_group);
                                //}
                            }
                            groupsAllowAddAssignment = _valid_groups;
                        }
			
			if (range == null)
			{
				if (AssignmentService.allowAddSiteAssignment(contextString))
				{
					// default to make site selection
					context.put("range", "site");
				}
				else if (groupsAllowAddAssignment.size() > 0)
				{
					// to group otherwise
					context.put("range", "groups");
				}
			}
			
			// group list which user can add message to
			if (groupsAllowAddAssignment.size() > 0)
			{
				String sort = (String) state.getAttribute(SORTED_BY);
				String asc = (String) state.getAttribute(SORTED_ASC);
				if (sort == null || (!sort.equals(SORTED_BY_GROUP_TITLE) && !sort.equals(SORTED_BY_GROUP_DESCRIPTION)))
				{
					sort = SORTED_BY_GROUP_TITLE;
					asc = Boolean.TRUE.toString();
					state.setAttribute(SORTED_BY, sort);
					state.setAttribute(SORTED_ASC, asc);
				}
				
                                
				// SAK-26349 - need to add the collection; the iterator added below is only usable once in the velocity template
				AssignmentComparator comp = new AssignmentComparator(state, sort, asc);
				Collections.sort((List<Group>) groupsAllowAddAssignment, comp);
				context.put("groupsList", groupsAllowAddAssignment);
                                
				context.put("groups", new SortedIterator(groupsAllowAddAssignment.iterator(), comp));
				context.put("assignmentGroups", state.getAttribute(NEW_ASSIGNMENT_GROUPS));
			}
		}

		context.put("allowGroupAssignmentsInGradebook", Boolean.valueOf(AssignmentService.getAllowGroupAssignmentsInGradebook()));

		// the notification email choices
		// whether the choice of emails instructor submission notification is available in the installation
		// system installation allowed assignment submission notification
		boolean allowNotification = ServerConfigurationService.getBoolean(ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS, true);
		if (allowNotification)
		{
			// whether current user can receive notification. If not, don't show the notification choices in the create/edit assignment page
			allowNotification = AssignmentService.allowReceiveSubmissionNotification(contextString);
		}
		if (allowNotification)
		{
			context.put("name_assignment_instructor_notifications", ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS);
			if (state.getAttribute(Assignment.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_VALUE) == null)
			{
				// set the notification value using site default
				// whether or how the instructor receive submission notification emails, none(default)|each|digest
				state.setAttribute(Assignment.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_VALUE, ServerConfigurationService.getString(ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_DEFAULT, Assignment.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_NONE));
			}
			context.put("value_assignment_instructor_notifications", state.getAttribute(Assignment.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_VALUE));
			// the option values
			context.put("value_assignment_instructor_notifications_none", Assignment.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_NONE);
			context.put("value_assignment_instructor_notifications_each", Assignment.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_EACH);
			context.put("value_assignment_instructor_notifications_digest", Assignment.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_DIGEST);
		}
		
		// release grade notification option
		putReleaseGradeNotificationOptionIntoContext(state, context);

		// release grade notification option
		putReleaseResubmissionNotificationOptionIntoContext(state, context, a);		

		// the supplement information
		// model answers		
		context.put("modelanswer", state.getAttribute(MODELANSWER) != null?Boolean.TRUE:Boolean.FALSE);
		context.put("modelanswer_text", state.getAttribute(MODELANSWER_TEXT));
		context.put("modelanswer_showto", state.getAttribute(MODELANSWER_SHOWTO));
		// get attachment for model answer object
		putSupplementItemAttachmentStateIntoContext(state, context, MODELANSWER_ATTACHMENTS);
		// private notes
		context.put("allowReadAssignmentNoteItem", m_assignmentSupplementItemService.canReadNoteItem(a, contextString));
		context.put("allowEditAssignmentNoteItem", m_assignmentSupplementItemService.canEditNoteItem(a));
		context.put("note", state.getAttribute(NOTE) != null?Boolean.TRUE:Boolean.FALSE);
		context.put("note_text", state.getAttribute(NOTE_TEXT));
		context.put("note_to", state.getAttribute(NOTE_SHAREWITH) != null?state.getAttribute(NOTE_SHAREWITH):String.valueOf(0));
		// all purpose item
		context.put("allPurpose", state.getAttribute(ALLPURPOSE) != null?Boolean.TRUE:Boolean.FALSE);
		context.put("value_allPurposeTitle", state.getAttribute(ALLPURPOSE_TITLE));
		context.put("value_allPurposeText", state.getAttribute(ALLPURPOSE_TEXT));
		context.put("value_allPurposeHide", state.getAttribute(ALLPURPOSE_HIDE) != null?state.getAttribute(ALLPURPOSE_HIDE):Boolean.FALSE);
		context.put("value_allPurposeShowFrom", state.getAttribute(ALLPURPOSE_SHOW_FROM) != null?state.getAttribute(ALLPURPOSE_SHOW_FROM):Boolean.FALSE);
		context.put("value_allPurposeShowTo", state.getAttribute(ALLPURPOSE_SHOW_TO) != null?state.getAttribute(ALLPURPOSE_SHOW_TO):Boolean.FALSE);
		context.put("value_allPurposeAccessList", state.getAttribute(ALLPURPOSE_ACCESS));
		putTimePropertiesInContext(context, state, "allPurposeRelease", ALLPURPOSE_RELEASE_MONTH, ALLPURPOSE_RELEASE_DAY, ALLPURPOSE_RELEASE_YEAR, ALLPURPOSE_RELEASE_HOUR, ALLPURPOSE_RELEASE_MIN);
		putTimePropertiesInContext(context, state, "allPurposeRetract", ALLPURPOSE_RETRACT_MONTH, ALLPURPOSE_RETRACT_DAY, ALLPURPOSE_RETRACT_YEAR, ALLPURPOSE_RETRACT_HOUR, ALLPURPOSE_RETRACT_MIN);
		// get attachment for all purpose object
		putSupplementItemAttachmentStateIntoContext(state, context, ALLPURPOSE_ATTACHMENTS);
		
		// put role information into context
		HashMap<String, List> roleUsers = new HashMap<String, List>();
		try
		{
			AuthzGroup realm = authzGroupService.getAuthzGroup(SiteService.siteReference(contextString));
			Set<Role> roles = realm.getRoles();
			for(Iterator iRoles = roles.iterator(); iRoles.hasNext();)
			{
				Role r = (Role) iRoles.next();
				Set<String> users = realm.getUsersHasRole(r.getId());
				if (users!=null && users.size() > 0)
				{
					List<User> usersList = new ArrayList();
					for (Iterator<String> iUsers = users.iterator(); iUsers.hasNext();)
					{
						String userId = iUsers.next();
						try
						{
							User u = UserDirectoryService.getUser(userId);
							usersList.add(u);
						}
						catch (Exception e)
						{
							M_log.warn(this + ":setAssignmentFormContext cannot get user " +  e.getMessage() + " user id=" + userId);
						}
					}
					roleUsers.put(r.getId(), usersList);
				}
			}
			context.put("roleUsers", roleUsers);
		}
		catch (Exception e)
		{
			M_log.warn(this + ":setAssignmentFormContext role cast problem " +  e.getMessage() + " site =" + contextString);
		}
		//Add the additional options in
		context.put("value_additionalOptions", contextAdditionalOptions);
		
	} // setAssignmentFormContext

	/**
	 * Get a user facing String message represeting the list of file types that are accepted by the content review service
	 * They appear in this form: PowerPoint (.pps, .ppt, .ppsx, .pptx), plain text (.txt), ...
	 */
	private String getContentReviewAcceptedFileTypesMessage()
	{
		StringBuilder sb = new StringBuilder();
		Map<String, SortedSet<String>> fileTypesToExtensions = contentReviewService.getAcceptableFileTypesToExtensions();
		// The delimiter is a comma. Commas still need to be internationalized (the arabic comma is not the english comma)
		String i18nDelimiter = rb.getString("content_review.accepted.types.delimiter") + " ";
		String i18nLParen = " " + rb.getString("content_review.accepted.types.lparen");
		String i18nRParen = rb.getString("content_review.accepted.types.rparen");
		String fDelimiter = "";
		// don't worry about conjunctions; just separate with commas
		for (Map.Entry<String, SortedSet<String>> entry : fileTypesToExtensions.entrySet())
		{
			String fileType = entry.getKey();
			SortedSet<String> extensions = entry.getValue();
			sb.append(fDelimiter).append(fileType).append(i18nLParen);
			String eDelimiter = "";
			for (String extension : extensions)
			{
				sb.append(eDelimiter).append(extension);
				// optimized by java compiler
				eDelimiter = i18nDelimiter;
			}
			sb.append(i18nRParen);
			
			// optimized by java compiler
			fDelimiter = i18nDelimiter;
		}

		return sb.toString();
	}

	/**
	 * how many gradebook items has been assoicated with assignment
	 * @param context
	 * @param state
	 */
	private void currentAssignmentGradebookIntegrationIntoContext(Context context, SessionState state, GradebookService g, String gradebookUid, String aTitle)
	{
		String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
		// get all assignment
		Iterator iAssignments = AssignmentService.getAssignmentsForContext(contextString);
		HashMap<String, String> gAssignmentIdTitles = new HashMap<String, String>();

		HashMap<String, String> gradebookAssignmentsSelectedDisabled = new HashMap<String, String>();
		HashMap<String, String> gradebookAssignmentsLabel = new HashMap<String, String>();
		
		while (iAssignments.hasNext())
		{
			Assignment a = (Assignment) iAssignments.next();
			String gradebookItem = StringUtils.trimToNull(a.getProperties().getProperty(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT));
			if (gradebookItem != null)
			{
				String associatedAssignmentTitles="";
				if (gAssignmentIdTitles.containsKey(gradebookItem))
				{
					// get the current associated assignment titles first
					associatedAssignmentTitles=gAssignmentIdTitles.get(gradebookItem) + ", ";
				}
				
				// append the current assignment title
				associatedAssignmentTitles += a.getTitle();
				
				// put the current associated assignment titles back
				gAssignmentIdTitles.put(gradebookItem, associatedAssignmentTitles);
			}
		}
		
		// get all assignments in Gradebook
		try
		{
			List gradebookAssignments = g.getAssignments(gradebookUid);
			List gradebookAssignmentsExceptSamigo = new ArrayList();
	
			// filtering out those from Samigo
			for (Iterator i=gradebookAssignments.iterator(); i.hasNext();)
			{
				org.sakaiproject.service.gradebook.shared.Assignment gAssignment = (org.sakaiproject.service.gradebook.shared.Assignment) i.next();
				if (!gAssignment.isExternallyMaintained() || gAssignment.isExternallyMaintained() && gAssignment.getExternalAppName().equals(getToolTitle()))
				{
					gradebookAssignmentsExceptSamigo.add(gAssignment);
				
					// gradebook item has been associated or not
					String gaId = gAssignment.isExternallyMaintained() ? gAssignment.getExternalId() : gAssignment.getName();
					String status = "";
					if (gAssignmentIdTitles.containsKey(gaId))
					{
						String assignmentTitle = gAssignmentIdTitles.get(gaId);
						if (aTitle != null && aTitle.equals(assignmentTitle))
						{
							// this gradebook item is associated with current assignment, make it selected
							status = "selected";
						}
					}
					
					// check with the state variable
					if ( state.getAttribute(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT) != null)
					{
						String associatedAssignment = ((String) state.getAttribute(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT));
						if (associatedAssignment.equals(gaId))
						{
							status ="selected";
						}
					}
					
					gradebookAssignmentsSelectedDisabled.put(Validator.escapeHtml(gaId), status);
					
					
					// gradebook assignment label
					String label = gAssignment.getName();
					if (gAssignmentIdTitles.containsKey(gaId))
					{
						label += " ( " + rb.getFormattedMessage("usedGradebookAssignment", new Object[]{gAssignmentIdTitles.get(gaId)}) + " )";
					}
					gradebookAssignmentsLabel.put(Validator.escapeHtml(gaId), label);
				}
			}
		}
		catch (GradebookNotFoundException e)
		{
			// exception
			M_log.debug(this + ":currentAssignmentGradebookIntegrationIntoContext " + rb.getFormattedMessage("addtogradebook.alertMessage", new Object[]{e.getMessage()}));
		}
		context.put("gradebookAssignmentsSelectedDisabled", gradebookAssignmentsSelectedDisabled);
		
		context.put("gradebookAssignmentsLabel", gradebookAssignmentsLabel);
	}
	
	private void putGradebookCategoryInfoIntoContext(SessionState state,
			Context context) {
		HashMap<Long, String> categoryTable = categoryTable();
		if (categoryTable != null)
		{
			context.put("value_totalCategories", Integer.valueOf(categoryTable.size()));

			// selected category
			context.put("value_Category", state.getAttribute(NEW_ASSIGNMENT_CATEGORY));
			
			List<Long> categoryList = new ArrayList<Long>();
			for (Map.Entry<Long, String> entry : categoryTable.entrySet())
			{
				categoryList.add(entry.getKey());
			}
			Collections.sort(categoryList);
			context.put("categoryKeys", categoryList);
			context.put("categoryTable", categoryTable);
		}
		else
		{
			context.put("value_totalCategories", Integer.valueOf(0));
		}
	}


	/**
	 * put the release grade notification options into context
	 * @param state
	 * @param context
	 */
	private void putReleaseGradeNotificationOptionIntoContext(SessionState state, Context context) {
		if (state.getAttribute(Assignment.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_VALUE) == null)
		{
			// set the notification value using site default to be none: no email will be sent to student when the grade is released
			state.setAttribute(Assignment.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_VALUE, Assignment.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_NONE);
		}
		// input fields
		context.put("name_assignment_releasegrade_notification", ASSIGNMENT_RELEASEGRADE_NOTIFICATION);
		context.put("value_assignment_releasegrade_notification", state.getAttribute(Assignment.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_VALUE));
		// the option values
		context.put("value_assignment_releasegrade_notification_none", Assignment.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_NONE);
		context.put("value_assignment_releasegrade_notification_each", Assignment.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_EACH);
	}
	/**
	* put the release resubmission grade notification options into context
	* @param state
	* @param context
	*/
	private void putReleaseResubmissionNotificationOptionIntoContext(SessionState state, Context context, Assignment a) {
		if (state.getAttribute(Assignment.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_VALUE) == null && a != null){
			// get the assignment property for notification setting first
			state.setAttribute(Assignment.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_VALUE, a.getProperties().getProperty(Assignment.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_VALUE));
		}
		if (state.getAttribute(Assignment.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_VALUE) == null){
			// set the notification value using site default to be none: no email will be sent to student when the grade is released
			state.setAttribute(Assignment.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_VALUE, Assignment.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_NONE);
		}
		// input fields
		context.put("name_assignment_releasereturn_notification", ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION);
		context.put("value_assignment_releasereturn_notification", state.getAttribute(Assignment.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_VALUE));
		// the option values
		context.put("value_assignment_releasereturn_notification_none", Assignment.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_NONE);
		context.put("value_assignment_releasereturn_notification_each", Assignment.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_EACH);
	}	
	/**
	 * build the instructor view of create a new assignment
	 */
	protected String build_instructor_preview_assignment_context(VelocityPortlet portlet, Context context, RunData data,
			SessionState state)
	{
		context.put("time", TimeService.newTime());

		context.put("user", UserDirectoryService.getCurrentUser());

		context.put("value_Title", (String) state.getAttribute(NEW_ASSIGNMENT_TITLE));
		context.put("name_order", NEW_ASSIGNMENT_ORDER);
		context.put("value_position_order", (String) state.getAttribute(NEW_ASSIGNMENT_ORDER));

		Time openTime = getTimeFromState(state, NEW_ASSIGNMENT_OPENMONTH, NEW_ASSIGNMENT_OPENDAY, NEW_ASSIGNMENT_OPENYEAR, NEW_ASSIGNMENT_OPENHOUR, NEW_ASSIGNMENT_OPENMIN);
		context.put("value_OpenDate", openTime);

                if (Boolean.valueOf(ServerConfigurationService.getBoolean("assignment.visible.date.enabled", false))) {
                    Time visibleTime = getTimeFromState(state, NEW_ASSIGNMENT_VISIBLEMONTH, NEW_ASSIGNMENT_VISIBLEDAY, NEW_ASSIGNMENT_VISIBLEYEAR, NEW_ASSIGNMENT_VISIBLEHOUR, NEW_ASSIGNMENT_VISIBLEMIN);
                    context.put("value_VisibleDate", visibleTime);
                    context.put(NEW_ASSIGNMENT_VISIBLETOGGLE, visibleTime!=null);
                }

		// due time
		Time dueTime = getTimeFromState(state, NEW_ASSIGNMENT_DUEMONTH, NEW_ASSIGNMENT_DUEDAY, NEW_ASSIGNMENT_DUEYEAR, NEW_ASSIGNMENT_DUEHOUR, NEW_ASSIGNMENT_DUEMIN);
		context.put("value_DueDate", dueTime);

		// close time
		Time closeTime = TimeService.newTime();
		Boolean enableCloseDate = (Boolean) state.getAttribute(NEW_ASSIGNMENT_ENABLECLOSEDATE);
		context.put("value_EnableCloseDate", enableCloseDate);
		if ((enableCloseDate).booleanValue())
		{
			closeTime = getTimeFromState(state, NEW_ASSIGNMENT_CLOSEMONTH, NEW_ASSIGNMENT_CLOSEDAY, NEW_ASSIGNMENT_CLOSEYEAR, NEW_ASSIGNMENT_CLOSEHOUR, NEW_ASSIGNMENT_CLOSEMIN);
			context.put("value_CloseDate", closeTime);
		}

		context.put("value_Sections", state.getAttribute(NEW_ASSIGNMENT_SECTION));
		context.put("value_SubmissionType", state.getAttribute(NEW_ASSIGNMENT_SUBMISSION_TYPE));
		context.put("value_GradeType", state.getAttribute(NEW_ASSIGNMENT_GRADE_TYPE));
		String maxGrade = (String) state.getAttribute(NEW_ASSIGNMENT_GRADE_POINTS);
		context.put("value_GradePoints", displayGrade(state, maxGrade, AssignmentService.getScaleFactor()));
		context.put("value_Description", state.getAttribute(NEW_ASSIGNMENT_DESCRIPTION));
		context.put("value_CheckAddDueDate", state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE));
		context.put("value_CheckHideDueDate", state.getAttribute(NEW_ASSIGNMENT_CHECK_HIDE_DUE_DATE));
		context.put("value_CheckAutoAnnounce", state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE));
		context.put("Value_OpenDateNotification", state.getAttribute(Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION));
		// the option values
		context.put("value_opendate_notification_none", Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION_NONE);
		context.put("value_opendate_notification_low", Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION_LOW);
		context.put("value_opendate_notification_high", Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION_HIGH);
		context.put("value_CheckAddHonorPledge", state.getAttribute(NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE));
		context.put("honor_pledge_text", ServerConfigurationService.getString("assignment.honor.pledge", rb.getString("gen.honple2")));

		// SAK-17606
		context.put("value_CheckAnonymousGrading", state.getAttribute(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING));

		// get all available assignments from Gradebook tool except for those created from
		if (isGradebookDefined())
		{
			context.put("gradebookChoice", state.getAttribute(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK));
			context.put("associateGradebookAssignment", state.getAttribute(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT));
		
			// information related to gradebook categories
			putGradebookCategoryInfoIntoContext(state, context);
		}

		context.put("monthTable", monthTable());
		context.put("submissionTypeTable", submissionTypeTable());
		context.put("attachments", state.getAttribute(ATTACHMENTS));

		context.put("contentTypeImageService", state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE));

		context.put("preview_assignment_assignment_hide_flag", state.getAttribute(PREVIEW_ASSIGNMENT_ASSIGNMENT_HIDE_FLAG));
		context.put("preview_assignment_student_view_hide_flag", state.getAttribute(PREVIEW_ASSIGNMENT_STUDENT_VIEW_HIDE_FLAG));
		String assignmentId = StringUtils.trimToNull((String) state.getAttribute(PREVIEW_ASSIGNMENT_ASSIGNMENT_ID));
		if (assignmentId != null)
		{
			// editing existing assignment
			context.put("value_assignment_id", assignmentId);
			Assignment a = getAssignment(assignmentId, "build_instructor_preview_assignment_context", state);
			if (a != null)
			{
				context.put("isDraft", Boolean.valueOf(a.getDraft()));
				context.put("value_GradePoints", displayGrade(state, maxGrade, a.getContent().getFactor()));
			}
		}
		else
		{
			// new assignment
			context.put("isDraft", Boolean.TRUE);
		}
			
		context.put("value_assignmentcontent_id", state.getAttribute(PREVIEW_ASSIGNMENT_ASSIGNMENTCONTENT_ID));

		context.put("currentTime", TimeService.newTime());

		String template = (String) getContext(data).get("template");
		return template + TEMPLATE_INSTRUCTOR_PREVIEW_ASSIGNMENT;

	} // build_instructor_preview_assignment_context

	/**
	 * build the instructor view to delete an assignment
	 */
	protected String build_instructor_delete_assignment_context(VelocityPortlet portlet, Context context, RunData data,
			SessionState state)
	{
		List assignments = new ArrayList();
		List assignmentIds = (List) state.getAttribute(DELETE_ASSIGNMENT_IDS);
		HashMap<String, Integer> submissionCountTable = new HashMap<String, Integer>();
		for (int i = 0; i < assignmentIds.size(); i++)
		{
			String assignmentId = (String) assignmentIds.get(i);
			Assignment a = getAssignment(assignmentId, "build_instructor_delete_assignment_context", state);
			if (a != null)
			{
				Iterator submissions = AssignmentService.getSubmissions(a).iterator();
				int submittedCount = 0;
				while (submissions.hasNext())
				{
					AssignmentSubmission s = (AssignmentSubmission) submissions.next();
					if (s.getSubmitted() && s.getTimeSubmitted() != null)
					{
						submittedCount++;
					}
				}
				if (submittedCount > 0)
				{
					// if there is submission to the assignment, show the alert
					addAlert(state, rb.getFormattedMessage("areyousur_withSubmission", new Object[]{a.getTitle()}));
				}
				assignments.add(a);
				submissionCountTable.put(a.getReference(), Integer.valueOf(submittedCount));
				
			}
		}
		context.put("assignments", assignments);
		
		context.put("confirmMessage", assignments.size() > 1 ? rb.getString("areyousur_multiple"):rb.getString("areyousur_single"));
		context.put("currentTime", TimeService.newTime());
		context.put("submissionCountTable", submissionCountTable);

		String template = (String) getContext(data).get("template");
		return template + TEMPLATE_INSTRUCTOR_DELETE_ASSIGNMENT;

	} // build_instructor_delete_assignment_context

	/**
	 * build the instructor view to grade an submission
	 */
	protected String build_instructor_grade_submission_context(VelocityPortlet portlet, Context context, RunData data,
			SessionState state)
	{
		String submissionId="";
		int gradeType = -1;

		// need to show the alert for grading drafts?
		boolean addGradeDraftAlert = false;
		
		// assignment
		String assignmentId = (String) state.getAttribute(GRADE_SUBMISSION_ASSIGNMENT_ID); 
		Assignment a = getAssignment(assignmentId, "build_instructor_grade_submission_context", state);
		if (a != null)
		{
			context.put("assignment", a);
			if (a.getContent() != null)
			{
				gradeType = a.getContent().getTypeOfGrade();
			}

			// SAK-17606
			state.setAttribute(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING, a.getProperties().getProperty(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING));

			boolean allowToGrade=true;
			String associateGradebookAssignment = StringUtils.trimToNull(a.getProperties().getProperty(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT));
			if (associateGradebookAssignment != null)
			{
				GradebookService g = (GradebookService) ComponentManager.get("org.sakaiproject.service.gradebook.GradebookService");
				String gradebookUid = ToolManager.getInstance().getCurrentPlacement().getContext();
				if (g != null && g.isGradebookDefined(gradebookUid))
				{
					if (!g.currentUserHasGradingPerm(gradebookUid))
					{
						context.put("notAllowedToGradeWarning", rb.getString("not_allowed_to_grade_in_gradebook"));
						allowToGrade=false;
					}
				}
			}
			context.put("allowToGrade", Boolean.valueOf(allowToGrade));
		}

		// assignment submission
		AssignmentSubmission s = getSubmission((String) state.getAttribute(GRADE_SUBMISSION_SUBMISSION_ID), "build_instructor_grade_submission_context", state);
		if (s != null)
		{
			submissionId = s.getId();
			context.put("submission", s);
			
			if(a != null) {
				setScoringAgentProperties(context, a, s, true);
			}

			// show alert if student is working on a draft
			if (!s.getSubmitted() // not submitted
				&& ((s.getSubmittedText() != null && s.getSubmittedText().length()> 0) // has some text
					|| (s.getSubmittedAttachments() != null && s.getSubmittedAttachments().size() > 0))) // has some attachment
			{
				if (s.getCloseTime().after(TimeService.newTime()))	 
				{
					// not pass the close date yet
					addGradeDraftAlert = true;
				}
				else
				{
					// passed the close date already
					addGradeDraftAlert = false;
				}
			}
		
			ResourceProperties p = s.getProperties();
			if (p.getProperty(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT) != null)
			{
				context.put("prevFeedbackText", p.getProperty(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT));
			}

			if (p.getProperty(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT) != null)
			{
				context.put("prevFeedbackComment", p.getProperty(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT));
			}
			
			if (p.getProperty(PROP_SUBMISSION_PREVIOUS_FEEDBACK_ATTACHMENTS) != null)
			{
				context.put("prevFeedbackAttachments", getPrevFeedbackAttachments(p));
			}
			

			// put the re-submission info into context
			putTimePropertiesInContext(context, state, "Resubmit", ALLOW_RESUBMIT_CLOSEMONTH, ALLOW_RESUBMIT_CLOSEDAY, ALLOW_RESUBMIT_CLOSEYEAR, ALLOW_RESUBMIT_CLOSEHOUR, ALLOW_RESUBMIT_CLOSEMIN);
			assignment_resubmission_option_into_context(context, state);
		}

		context.put("user", state.getAttribute(STATE_USER));
		context.put("submissionTypeTable", submissionTypeTable());
		context.put("instructorAttachments", state.getAttribute(ATTACHMENTS));
		context.put("contentTypeImageService", state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE));
		context.put("service", AssignmentService.getInstance());

		// names
		context.put("name_grade_assignment_id", GRADE_SUBMISSION_ASSIGNMENT_ID);
		context.put("name_feedback_comment", GRADE_SUBMISSION_FEEDBACK_COMMENT);
		context.put("name_feedback_text", GRADE_SUBMISSION_FEEDBACK_TEXT);
		context.put("name_feedback_attachment", GRADE_SUBMISSION_FEEDBACK_ATTACHMENT);
		context.put("name_grade", GRADE_SUBMISSION_GRADE);
		context.put("name_allowResubmitNumber", AssignmentSubmission.ALLOW_RESUBMIT_NUMBER);

		// values
		context.put("value_year_from", state.getAttribute(NEW_ASSIGNMENT_YEAR_RANGE_FROM));
		context.put("value_year_to", state.getAttribute(NEW_ASSIGNMENT_YEAR_RANGE_TO));
		context.put("value_grade_assignment_id", state.getAttribute(GRADE_SUBMISSION_ASSIGNMENT_ID));
		context.put("value_feedback_comment", state.getAttribute(GRADE_SUBMISSION_FEEDBACK_COMMENT));
		context.put("value_feedback_text", state.getAttribute(GRADE_SUBMISSION_FEEDBACK_TEXT));
		context.put("value_feedback_attachment", state.getAttribute(ATTACHMENTS));

		// SAK-17606
		context.put("value_CheckAnonymousGrading", state.getAttribute(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING));

		// format to show one decimal place in grade
		context.put("value_grade", (gradeType == 3) ? displayGrade(state, (String) state.getAttribute(GRADE_SUBMISSION_GRADE), a.getContent().getFactor())
				: state.getAttribute(GRADE_SUBMISSION_GRADE));

        // try to put in grade overrides
        if (a.isGroup()) {
            Map<String,Object> _ugrades = new HashMap();
            User[] _users = s.getSubmitters();
            for (int i=0; _users != null && i < _users.length; i ++) {
                if (state.getAttribute(GRADE_SUBMISSION_GRADE + "_" + _users[i].getId()) != null) {
                    _ugrades.put(
                            _users[i].getId(),
                            gradeType == 3 ?
                                displayGrade(state, (String) state.getAttribute(GRADE_SUBMISSION_GRADE + "_" + _users[i].getId()), a.getContent().getFactor()):
                                state.getAttribute(GRADE_SUBMISSION_GRADE + "_" + _users[i].getId())
                    );
                }
            }
            context.put("value_grades", _ugrades);
        }

		context.put("assignment_expand_flag", state.getAttribute(GRADE_SUBMISSION_ASSIGNMENT_EXPAND_FLAG));

		// is this a non-electronic submission type of assignment
		context.put("nonElectronic", (a!=null && a.getContent().getTypeOfSubmission() == Assignment.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION)?Boolean.TRUE:Boolean.FALSE);
		
		if (addGradeDraftAlert)
		{
			addAlert(state, rb.getString("grading.alert.draft.beforeclosedate"));
		}
		context.put("alertGradeDraft", Boolean.valueOf(addGradeDraftAlert));

                if (a != null && a.isGroup()) {
                    checkForUsersInMultipleGroups(a, s.getSubmitterIds(), state, rb.getString("group.user.multiple.warning"));
                }
		
		// SAK-29314
		// Since USER_SUBMISSIONS is restricted to the page size, it is not very useful here
		// Therefore, we will prefer to use STATE_PAGEING_TOTAL_ITEMS. However, sometimes this contains
		// Assignment objects instead of SubmitterSubmission objects, so we have to fall back to USER_SUBMISSIONS
		// if this occurs, although in practise this seems to never happen
		List<SubmitterSubmission> userSubmissions = Collections.EMPTY_LIST;
		List totalItems = (List) state.getAttribute(STATE_PAGEING_TOTAL_ITEMS);
		if (!CollectionUtils.isEmpty(totalItems))
		{
			if (totalItems.get(0) instanceof SubmitterSubmission)
			{
				userSubmissions = (List<SubmitterSubmission>) totalItems;
			}
		}

		if (userSubmissions.isEmpty())
		{
			userSubmissions = (List<SubmitterSubmission>)state.getAttribute(USER_SUBMISSIONS);
		}

		// SAK-29314
		resetNavOptions();
		if (userSubmissions != null)
		{
			for (int index = 0; index < userSubmissions.size(); index++)
			{
				if( ((SubmitterSubmission) userSubmissions.get( index )).getSubmission().getId().equals( submissionId ) )
				{
					// Determine next/previous
					boolean goPT = false;
					boolean goNT = false;
					if( index > 0 )
					{
						goPT = true;
					}
					if( index < userSubmissions.size() - 1 )
					{
						goNT = true;
					}
					
					// Determine next ungraded, next with submission, next ungraded with submission
					for( int i = index + 1; i < userSubmissions.size(); i++ )
					{
						if( !nextUngraded )
						{
							processIfUngraded( userSubmissions.get( i ).getSubmission(), true );
						}

						if( !nextWithSubmission )
						{
							processIfHasSubmission( userSubmissions.get( i ).getSubmission(), true );
						}

						if( !nextUngradedWithSubmission )
						{
							processIfHasUngradedSubmission( userSubmissions.get( i ).getSubmission(), true );
						}

						if( nextUngraded && nextWithSubmission && nextUngradedWithSubmission )
						{
							break;
						}
					}
	
					// Determine previous ungraded, previous with submission, previous ungraded with submission
					for( int i = index - 1; i >= 0; i-- )
					{
						if( !prevUngraded )
						{
							processIfUngraded( userSubmissions.get( i ).getSubmission(), false );
						}

						if( !prevWithSubmission )
						{
							processIfHasSubmission( userSubmissions.get( i ).getSubmission(), false );
						}

						if( !prevUngradedWithSubmission )
						{
							processIfHasUngradedSubmission( userSubmissions.get( i ).getSubmission(), false );
						}

						if( prevUngraded && prevWithSubmission && prevUngradedWithSubmission )
						{
							break;
						}
					}
					
					// Determine if subs only was previously selected
					boolean subsOnlySelected = false;
					if( state.getAttribute( STATE_VIEW_SUBS_ONLY ) != null )
					{
						subsOnlySelected = (Boolean) state.getAttribute( STATE_VIEW_SUBS_ONLY );
						context.put( CONTEXT_VIEW_SUBS_ONLY, subsOnlySelected );
					}
					
					// Get the previous/next ids as necessary
					if( goPT )
					{
						context.put( "prevSubmissionId", ((SubmitterSubmission) userSubmissions.get( index - 1)).getSubmission().getReference() );
					}
					if( goNT )
					{
						context.put( "nextSubmissionId", ((SubmitterSubmission) userSubmissions.get( index + 1 )).getSubmission().getReference() );
					}
					if( nextUngraded )
					{
						context.put( CONTEXT_NEXT_UNGRADED_SUB_ID, nextUngradedRef );
					}
					if( prevUngraded )
					{
						context.put( CONTEXT_PREV_UNGRADED_SUB_ID, prevUngradedRef );
					}
					if( nextWithSubmission )
					{
						context.put( CONTEXT_NEXT_WITH_SUB_ID, nextWithSubmissionRef );
					}
					if( prevWithSubmission )
					{
						context.put( CONTEXT_PREV_WITH_SUB_ID, prevWithSubmissionRef );
					}
					if( nextUngradedWithSubmission )
					{
						context.put( CONTEXT_NEXT_UNGRADED_WITH_SUB_ID, nextUngradedWithSubmissionRef );
					}
					if( prevUngradedWithSubmission )
					{
						context.put( CONTEXT_PREV_UNGRADED_WITH_SUB_ID, prevUngradedWithSubmissionRef );
					}

					// Alter any enabled/disabled states if view subs only is selected
					if( subsOnlySelected )
					{
						if( !nextWithSubmission )
						{
							goNT = false;
						}
						if( !prevWithSubmission )
						{
							goPT = false;
						}
						if( !nextUngradedWithSubmission )
						{
							nextUngraded = false;
						}
						if( !prevUngradedWithSubmission )
						{
							prevUngraded = false;
						}
					}

					// Put the button enable/disable flags into the context
					context.put( "goPTButton", goPT );
					context.put( "goNTButton", goNT );
					context.put( CONTEXT_GO_NEXT_UNGRADED_ENABLED, nextUngraded );
					context.put( CONTEXT_GO_PREV_UNGRADED_ENABLED, prevUngraded );
				}
			}
		}

		// put supplement item into context
		supplementItemIntoContext(state, context, a, null);

		// put the grade confirmation message if applicable
		if (state.getAttribute(GRADE_SUBMISSION_DONE) != null)
		{
			context.put("gradingDone", Boolean.TRUE);
			state.removeAttribute(GRADE_SUBMISSION_DONE);
		}
		
		// put the grade confirmation message if applicable
		if (state.getAttribute(GRADE_SUBMISSION_SUBMIT) != null)
		{
			context.put("gradingSubmit", Boolean.TRUE);
			state.removeAttribute(GRADE_SUBMISSION_SUBMIT);
		}
		
		// letter grading
		letterGradeOptionsIntoContext(context);
		
		String template = (String) getContext(data).get("template");
		return template + TEMPLATE_INSTRUCTOR_GRADE_SUBMISSION;

	} // build_instructor_grade_submission_context

	/**
	 * SAK-29314 - Resets all navigation options
	 */
	private void resetNavOptions()
	{
		nextUngraded = false;
		prevUngraded = false;
		nextWithSubmission = false;
		prevWithSubmission = false;
		nextUngradedWithSubmission = false;
		prevUngradedWithSubmission = false;
		nextUngradedRef = "";
		prevUngradedRef = "";
		nextWithSubmissionRef = "";
		prevWithSubmissionRef = "";
		nextUngradedWithSubmissionRef = "";
		prevUngradedWithSubmissionRef = "";
	}

	/**
	 * SAK-29314 - Reset the appropriate navigation options
	 * 
	 * @param flag denotes which navigation options to reset
	 */
	private void resetNavOptions( String flag )
	{
		if( null != flag )
		{
			switch( flag )
			{
				case FLAG_NEXT_UNGRADED:
					nextUngraded = false;
					nextUngradedRef = "";
					break;
				case FLAG_PREV_UNGRADED:
					prevUngraded = false;
					prevUngradedRef = "";
					break;
				case FLAG_NEXT_WITH_SUB:
					nextWithSubmission = false;
					nextWithSubmissionRef = "";
					break;
				case FLAG_PREV_WITH_SUB:
					prevWithSubmission = false;
					prevWithSubmissionRef = "";
					break;
				case FLAG_NEXT_UNGRADED_WITH_SUB:
					nextUngradedWithSubmission = false;
					nextUngradedWithSubmissionRef = "";
					break;
				case FLAG_PREV_UNGRADED_WITH_SUB:
					prevUngradedWithSubmission = false;
					prevUngradedWithSubmissionRef = "";
					break;
			}
		}
	}

	/**
	 * SAK-29314 - Apply the appropriate navigation options
	 * 
	 * @param flag denotes the navigation options to apply
	 * @param submission the submission object in question
	 */
	private void applyNavOption( String flag, AssignmentSubmission submission )
	{
		if( null != flag )
		{
			switch( flag )
			{
				case FLAG_NEXT_UNGRADED:
					nextUngraded = true;
					nextUngradedRef = submission.getReference();
					break;
				case FLAG_PREV_UNGRADED:
					prevUngraded = true;
					prevUngradedRef = submission.getReference();
					break;
				case FLAG_NEXT_WITH_SUB:
					nextWithSubmission = true;
					nextWithSubmissionRef = submission.getReference();
					break;
				case FLAG_PREV_WITH_SUB:
					prevWithSubmission = true;
					prevWithSubmissionRef = submission.getReference();
					break;
				case FLAG_NEXT_UNGRADED_WITH_SUB:
					nextUngradedWithSubmission = true;
					nextUngradedWithSubmissionRef = submission.getReference();
					break;
				case FLAG_PREV_UNGRADED_WITH_SUB:
					prevUngradedWithSubmission = true;
					prevUngradedWithSubmissionRef = submission.getReference();
					break;
			}
		}
	}

	/**
	 * SAK-29314 - Determine if the given assignment submission is graded or not, whether
	 * it has an actual 'submission' or not.
	 * 
	 * @param submission - the submission to be checked
	 * @param isNext - true/false; is for next submission (true), or previous (false)
	 */
	private void processIfUngraded( AssignmentSubmission submission, boolean isNext )
	{
		String flag = isNext ? FLAG_NEXT_UNGRADED : FLAG_PREV_UNGRADED;
		resetNavOptions( flag );

		// If the submission is ungraded, set the appropriate flag and reference; return true
		if( !submission.getGraded() )
		{
			applyNavOption( flag, submission );
		}
	}

	/**
	 * SAK-29314 - Determine if the given assignment submission actually has a user submission
	 * 
	 * @param submission - the submission to be checked
	 * @param isNext - true/false; is for the next submission (true), or previous (false)
	 */
	private void processIfHasSubmission( AssignmentSubmission submission, boolean isNext )
	{
		String flag = isNext ? FLAG_NEXT_WITH_SUB : FLAG_PREV_WITH_SUB;
		resetNavOptions( flag );

		// If the submission is actually a submission, set the appropriate flag and reference; return true
		if( !NO_SUBMISSION.equals( submission.getStatus() ) && submission.isUserSubmission() )
		{
			applyNavOption( flag, submission );
		}
	}

	/**
	 * SAK-29314 - Determine if the given assignment submission actually has a submission
	 * and is ungraded.
	 * 
	 * @param submission - the submission to be checked
	 * @param isNext - true/false; is for the next submission (true), or previous (false)
	 */
	private void processIfHasUngradedSubmission( AssignmentSubmission submission, boolean isNext )
	{
		String flag = isNext ? FLAG_NEXT_UNGRADED_WITH_SUB : FLAG_PREV_UNGRADED_WITH_SUB;
		resetNavOptions( flag );

		// If the submisison is actually a submission and is ungraded, set the appropriate flag and reference; return true
		if( !submission.getGraded() && !NO_SUBMISSION.equals( submission.getStatus() ) && submission.isUserSubmission() )
		{
			applyNavOption( flag, submission );
		}
	}

	/**
	 * Checks whether the time is already past. 
	 * If yes, return the time of three days from current time; 
	 * Otherwise, return the original time
	 * @param originalTime
	 * @return
	 */
	private Time getProperFutureTime(Time originalTime) {
		// check whether the time is past already. 
		// If yes, add three days to the current time
		Time time = originalTime;
		if (TimeService.newTime().after(time))
		{
			time = TimeService.newTime(TimeService.newTime().getTime() + 3*24*60*60*1000/*add three days*/);
		}
		
		return time;
	}
	
	public void doPrev_back_next_submission_review(RunData rundata, String option, boolean submit)
	{
		if (!"POST".equals(rundata.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData) rundata).getPortletSessionState(((JetspeedRunData) rundata).getJs_peid());
		// save the instructor input
		boolean hasChange = saveReviewGradeForm(rundata, state, submit ? "submit" : "save");
		
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			ParameterParser params = rundata.getParameters();
			List<String> submissionIds = new ArrayList<String>();
			if(state.getAttribute(USER_SUBMISSIONS) != null){
				submissionIds = (List<String>) state.getAttribute(USER_SUBMISSIONS);
			}
			
			String submissionId = null;
			String assessorId = null;
			if ("next".equals(option))
			{
				submissionId = params.get("nextSubmissionId");
				assessorId = params.get("nextAssessorId");
			}
			else if ("prev".equals(option))
			{
				submissionId = params.get("prevSubmissionId");
				assessorId = params.get("prevAssessorId");
			}
			else if ("back".equals(option))
			{
				String assignmentId = (String) state.getAttribute(VIEW_ASSIGNMENT_ID);
				List userSubmissionsState = state.getAttribute(STATE_PAGEING_TOTAL_ITEMS) != null ? (List) state.getAttribute(STATE_PAGEING_TOTAL_ITEMS):null;
				if(userSubmissionsState != null && userSubmissionsState.size() > 0 && userSubmissionsState.get(0) instanceof SubmitterSubmission
						&& AssignmentService.allowGradeSubmission(assignmentId)){
					//coming from instructor view submissions page
					state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_GRADE_ASSIGNMENT);
					state.setAttribute(PEER_ATTACHMENTS, m_entityManager.newReferenceList());
				}else{
					state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
					state.setAttribute(PEER_ATTACHMENTS, m_entityManager.newReferenceList());
				}
			}
			if(submissionId != null && submissionIds.contains(submissionId)){
				state.setAttribute(GRADE_SUBMISSION_SUBMISSION_ID, submissionId);
			}
			if(assessorId != null){
				state.setAttribute(PEER_ASSESSMENT_ASSESSOR_ID, assessorId);
			}
		}
	}
	
	/**
	 * Responding to the request of submission navigation
	 * @param rundata
	 * @param option
	 */
	public void doPrev_back_next_submission(RunData rundata, String option)
	{
		if (!"POST".equals(rundata.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData) rundata).getPortletSessionState(((JetspeedRunData) rundata).getJs_peid());
		// save the instructor input
		boolean hasChange = readGradeForm(rundata, state, "save");
		if (state.getAttribute(STATE_MESSAGE) == null && hasChange)
		{
			grade_submission_option(rundata, "save");
		}
		
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			if ("back".equals(option))
			{
				// SAK-29314 - calculate our position relative to the list so we can return to the correct page
				state.setAttribute(STATE_GOTO_PAGE, calcPageFromSubmission(state));
				state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_GRADE_ASSIGNMENT);
			}
			else if ("backListStudent".equals(option))
			{
				state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT);
			}
			else if( option.startsWith( FLAG_NEXT ) || option.startsWith( FLAG_PREV ) )
			{
				// SAK-29314
				ParameterParser params = rundata.getParameters();
				String submissionsOnlySelected = (String) params.getString( PARAMS_VIEW_SUBS_ONLY_CHECKBOX );
				if( FLAG_ON.equals( submissionsOnlySelected ) )
				{
					switch( option )
					{
						case FLAG_NEXT:
							navigateToSubmission( rundata, CONTEXT_NEXT_WITH_SUB_ID );
							break;
						case FLAG_PREV:
							navigateToSubmission( rundata, CONTEXT_PREV_WITH_SUB_ID );
							break;
						case FLAG_NEXT_UNGRADED:
							navigateToSubmission( rundata, CONTEXT_NEXT_UNGRADED_WITH_SUB_ID );
							break;
						case FLAG_PREV_UNGRADED:
							navigateToSubmission( rundata, CONTEXT_PREV_UNGRADED_WITH_SUB_ID );
							break;
					}
				}
				else
				{
					switch( option )
					{
						case FLAG_NEXT:
							navigateToSubmission( rundata, "nextSubmissionId" );
							break;
						case FLAG_PREV:
							navigateToSubmission( rundata, "prevSubmissionId" );
							break;
						case FLAG_NEXT_UNGRADED:
							navigateToSubmission( rundata, CONTEXT_NEXT_UNGRADED_SUB_ID );
							break;
						case FLAG_PREV_UNGRADED:
							navigateToSubmission( rundata, CONTEXT_PREV_UNGRADED_SUB_ID );
							break;
					}
				}
			}
		}

	} // doPrev_back_next_submission

	/**
	 * SAK-29314 - Calculate the page of the submission list that the current submission belongs to
	 * 
	 * @param state
	 * @return 
	 */
	private Integer calcPageFromSubmission(SessionState state)
	{
		int pageSize = 1;
		try
		{
			pageSize = Integer.parseInt(state.getAttribute(STATE_PAGESIZE).toString());
		}
		catch( NumberFormatException ex )
		{
			M_log.debug(ex.getMessage());
		}
		
		if( pageSize <= 1 )
		{
			return 1;
		}
		
		String submissionId = state.getAttribute(GRADE_SUBMISSION_SUBMISSION_ID).toString();
		List<SubmitterSubmission> subs = (List<SubmitterSubmission>) state.getAttribute(STATE_PAGEING_TOTAL_ITEMS);
		int subIndex = 0;

		for (int i = 0; i < subs.size(); ++i)
		{
			SubmitterSubmission sub = subs.get(i);
			String ref = sub.getSubmission().getReference();
			if (ref.equals(submissionId))
			{
				subIndex = i;
				break;
			}
		}

		int page = subIndex / pageSize + 1;
		return page;
	}

	private void navigateToSubmission(RunData rundata, String paramString) {
		ParameterParser params = rundata.getParameters();
		SessionState state = ((JetspeedRunData) rundata).getPortletSessionState(((JetspeedRunData) rundata).getJs_peid());
		String assignmentId = (String) state.getAttribute(GRADE_SUBMISSION_ASSIGNMENT_ID);
		String submissionId = StringUtils.trimToNull(params.getString(paramString));
		// SAK-29314 - put submission information into state
		boolean viewSubsOnlySelected = stringToBool((String) params.getString(PARAMS_VIEW_SUBS_ONLY_CHECKBOX));
		if (submissionId != null)
		{
			// put submission information into state
			putSubmissionInfoIntoState(state, assignmentId, submissionId, viewSubsOnlySelected);
		}
	}
	
	/**
	 * SAK-29314 - Convert the given string into a boolean value
	 *
	 * @param boolString - the string to be parsed to boolean (may be 'on'/'off' or 'true'/'false')
	 * @return the boolean value representing the string given
	 */
	private boolean stringToBool(String boolString)
	{
		return FLAG_ON.equals(boolString) || FLAG_TRUE.equals(boolString);
	}

	/**
	 * Parse time value and put corresponding values into state
	 * @param context
	 * @param state
	 * @param a
	 * @param timeValue
	 * @param timeName
	 * @param month
	 * @param day
	 * @param year
	 * @param hour
	 * @param min
	 */
	private void putTimePropertiesInState(SessionState state, Time timeValue,
											String month, String day, String year, String hour, String min) {
		TimeBreakdown bTime = null;
		try {
			bTime = timeValue.breakdownLocal();
		} catch (NullPointerException _npe) { 
			bTime = TimeService.newTime().breakdownLocal();
			bTime.setHour(12); bTime.setMin(0);
		}
		state.setAttribute(month, Integer.valueOf(bTime.getMonth()));
		state.setAttribute(day, Integer.valueOf(bTime.getDay()));
		state.setAttribute(year, Integer.valueOf(bTime.getYear()));
		state.setAttribute(hour, Integer.valueOf(bTime.getHour()));
		state.setAttribute(min, Integer.valueOf(bTime.getMin()));
	}

	/**
	 * put related time information into context variable
	 * @param context
	 * @param state
	 * @param timeName
	 * @param month
	 * @param day
	 * @param year
	 * @param hour
	 * @param min
	 */
	private void putTimePropertiesInContext(Context context, SessionState state, String timeName,
													String month, String day, String year, String hour, String min) {
		// get the submission level of close date setting
		context.put("name_" + timeName + "Month", month);
		context.put("name_" + timeName + "Day", day);
		context.put("name_" + timeName + "Year", year);
		context.put("name_" + timeName + "Hour", hour);
		context.put("name_" + timeName + "Min", min);
		context.put("value_" + timeName + "Month", (Integer) state.getAttribute(month));
		context.put("value_" + timeName + "Day", (Integer) state.getAttribute(day));
		context.put("value_" + timeName + "Year", (Integer) state.getAttribute(year));
		context.put("value_" + timeName + "Hour", (Integer) state.getAttribute(hour));
		context.put("value_" + timeName + "Min", (Integer) state.getAttribute(min));
	}

	private List getPrevFeedbackAttachments(ResourceProperties p) {
		String attachmentsString = p.getProperty(PROP_SUBMISSION_PREVIOUS_FEEDBACK_ATTACHMENTS);
		String[] attachmentsReferences = attachmentsString.split(",");
		List prevFeedbackAttachments = EntityManager.newReferenceList();
		for (int k =0; k < attachmentsReferences.length; k++)
		{
			prevFeedbackAttachments.add(EntityManager.newReference(attachmentsReferences[k]));
		}
		return prevFeedbackAttachments;
	}

	/**
	 * build the instructor preview of grading submission
	 */
	protected String build_instructor_preview_grade_submission_context(VelocityPortlet portlet, Context context, RunData data,
			SessionState state)
	{

		// assignment
		int gradeType = -1;
		String assignmentId = (String) state.getAttribute(GRADE_SUBMISSION_ASSIGNMENT_ID);
		Assignment a = getAssignment(assignmentId, "build_instructor_preview_grade_submission_context", state);
		if (a != null)
		{
			context.put("assignment", a);
			gradeType = a.getContent().getTypeOfGrade();
		}

		// submission		
		AssignmentSubmission submission = getSubmission((String) state.getAttribute(GRADE_SUBMISSION_SUBMISSION_ID), "build_instructor_preview_grade_submission_context", state);
		context.put("submission", submission);
				
		if(a != null) {
			setScoringAgentProperties(context, a, submission, false);
		}

		User user = (User) state.getAttribute(STATE_USER);
		context.put("user", user);
		context.put("submissionTypeTable", submissionTypeTable());
		context.put("contentTypeImageService", state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE));
		context.put("service", AssignmentService.getInstance());

		// filter the feedback text for the instructor comment and mark it as red
		String feedbackText = (String) state.getAttribute(GRADE_SUBMISSION_FEEDBACK_TEXT);
		context.put("feedback_comment", state.getAttribute(GRADE_SUBMISSION_FEEDBACK_COMMENT));
		context.put("feedback_text", feedbackText);
		context.put("feedback_attachment", state.getAttribute(GRADE_SUBMISSION_FEEDBACK_ATTACHMENT));

		// SAK-17606
		context.put("value_CheckAnonymousGrading", state.getAttribute(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING));

		// format to show "factor" decimal places
		String grade = (String) state.getAttribute(GRADE_SUBMISSION_GRADE);
		if (gradeType == 3)
		{
			grade = displayGrade(state, grade, submission.getAssignment().getContent().getFactor());
		}
		context.put("grade", grade);

		context.put("comment_open", COMMENT_OPEN);
		context.put("comment_close", COMMENT_CLOSE);

		context.put("allowResubmitNumber", state.getAttribute(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER));
		String closeTimeString =(String) state.getAttribute(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME);
		if (closeTimeString != null)
		{
			// close time for resubmit
			Time time = TimeService.newTime(Long.parseLong(closeTimeString));
			context.put("allowResubmitCloseTime", time.toStringLocalFull());
		}

		String template = (String) getContext(data).get("template");
		return template + TEMPLATE_INSTRUCTOR_PREVIEW_GRADE_SUBMISSION;

	} // build_instructor_preview_grade_submission_context

	/**
	 * build the instructor view to grade an assignment
	 */
	protected String build_instructor_grade_assignment_context(VelocityPortlet portlet, Context context, RunData data,
			SessionState state)
	{
		context.put("user", state.getAttribute(STATE_USER));

		// sorting related fields
		context.put("sortedBy", state.getAttribute(SORTED_GRADE_SUBMISSION_BY));
		context.put("sortedAsc", state.getAttribute(SORTED_GRADE_SUBMISSION_ASC));
		context.put("sort_lastName", SORTED_GRADE_SUBMISSION_BY_LASTNAME);
		context.put("sort_submitTime", SORTED_GRADE_SUBMISSION_BY_SUBMIT_TIME);
		context.put("sort_submitStatus", SORTED_GRADE_SUBMISSION_BY_STATUS);
		context.put("sort_submitGrade", SORTED_GRADE_SUBMISSION_BY_GRADE);
		context.put("sort_submitReleased", SORTED_GRADE_SUBMISSION_BY_RELEASED);
		context.put("sort_submitReview", SORTED_GRADE_SUBMISSION_CONTENTREVIEW);
		context.put("userDirectoryService", UserDirectoryService.getInstance());

		String assignmentRef = (String) state.getAttribute(EXPORT_ASSIGNMENT_REF);
		Assignment assignment = getAssignment(assignmentRef, "build_instructor_grade_assignment_context", state);
		
		// getContent() early and store it, this call is expensive, always making a db call due to lack of caching in this tool
		AssignmentContent assignmentContent = assignment == null ? null : assignment.getContent();
		
		if (assignment != null)
		{
			context.put("assignment", assignment);
			state.setAttribute(EXPORT_ASSIGNMENT_ID, assignment.getId());
			if (assignmentContent != null) 
			{
				context.put("assignmentContent", assignmentContent);
				context.put("value_SubmissionType", Integer.valueOf(assignmentContent.getTypeOfSubmission()));
				context.put("typeOfGrade", assignmentContent.getTypeOfGrade());
			}
			
			// put creator information into context
			putCreatorIntoContext(context, assignment);
			
			String defaultGrade = assignment.getProperties().getProperty(GRADE_NO_SUBMISSION_DEFAULT_GRADE);
			if (defaultGrade != null)
			{
				context.put("defaultGrade", defaultGrade);
			}
			
			initViewSubmissionListOption(state);
			
			String view = (String)state.getAttribute(VIEW_SUBMISSION_LIST_OPTION);
			context.put("view", view);
			
			context.put("searchString", state.getAttribute(VIEW_SUBMISSION_SEARCH) != null ? state.getAttribute(VIEW_SUBMISSION_SEARCH) : "");

			
			// access point url for zip file download
			String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
			String accessPointUrl = ServerConfigurationService.getAccessUrl().concat(AssignmentService.submissionsZipReference(
					contextString, (String) state.getAttribute(EXPORT_ASSIGNMENT_REF)));
			if (view != null && !AssignmentConstants.ALL.equals(view))
			{
				// append the group info to the end
				accessPointUrl = accessPointUrl.concat(view);
			}
			context.put("accessPointUrl", accessPointUrl);

			// SAK-17606
			state.setAttribute(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING, assignment.getProperties().getProperty(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING));
				
			if (AssignmentService.getAllowGroupAssignments())
			{
				Collection groupsAllowGradeAssignment = AssignmentService.getGroupsAllowGradeAssignment((String) state.getAttribute(STATE_CONTEXT_STRING), assignment.getReference());
				
				// group list which user can add message to
				if (groupsAllowGradeAssignment.size() > 0)
				{
					String sort = (String) state.getAttribute(SORTED_BY);
					String asc = (String) state.getAttribute(SORTED_ASC);
					if (sort == null || (!sort.equals(SORTED_BY_GROUP_TITLE) && !sort.equals(SORTED_BY_GROUP_DESCRIPTION)))
					{
						sort = SORTED_BY_GROUP_TITLE;
						asc = Boolean.TRUE.toString();
						state.setAttribute(SORTED_BY, sort);
						state.setAttribute(SORTED_ASC, asc);
					}
					context.put("groups", new SortedIterator(groupsAllowGradeAssignment.iterator(), new AssignmentComparator(state, sort, asc)));
				}
			}

			// SAK-17606
			context.put("value_CheckAnonymousGrading", state.getAttribute(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING));
			
			List<SubmitterSubmission> userSubmissions = prepPage(state);
			
			// attach the assignment to these submissions now to avoid costly lookup for each submission later in the velocity template
			for (SubmitterSubmission s : userSubmissions)
			{
				s.getSubmission().setAssignment(assignment);
			}
			
			state.setAttribute(USER_SUBMISSIONS, userSubmissions);
			context.put("userSubmissions", state.getAttribute(USER_SUBMISSIONS));

			//find peer assessment grades if exist
			if(assignment.getAllowPeerAssessment()){
				List<String> submissionIds = new ArrayList<String>();
				//get list of submission ids to look up reviews in db
				for(SubmitterSubmission s : userSubmissions){
					submissionIds.add(s.getSubmission().getId());
				}
				//look up reviews for these submissions
				List<PeerAssessmentItem> items = assignmentPeerAssessmentService.getPeerAssessmentItems(submissionIds, assignment.getContent().getFactor());
				//create a map for velocity to use in displaying the submission reviews
				Map<String, List<PeerAssessmentItem>> itemsMap = new HashMap<String, List<PeerAssessmentItem>>();
				Map<String, User> reviewersMap = new HashMap<String, User>();
				if(items != null){
					for(PeerAssessmentItem item : items){
						//update items map
						List<PeerAssessmentItem> sItems = itemsMap.get(item.getSubmissionId());
						if(sItems == null){
							sItems = new ArrayList<PeerAssessmentItem>();
						}
						sItems.add(item);
						itemsMap.put(item.getSubmissionId(), sItems);
						//update users map:
						User u = reviewersMap.get(item.getAssessorUserId());
						if(u == null){
							try {
								u = UserDirectoryService.getUser(item.getAssessorUserId());
								reviewersMap.put(item.getAssessorUserId(), u);
							} catch (UserNotDefinedException e) {
								M_log.error(e.getMessage(), e);
							}
						}
					}
				}
				//go through all the submissions and make sure there aren't any nulls
				for(String id : submissionIds){
					List<PeerAssessmentItem> sItems = itemsMap.get(id);
					if(sItems == null){
						sItems = new ArrayList<PeerAssessmentItem>();
						itemsMap.put(id, sItems);
					}
				}
				context.put("peerAssessmentItems", itemsMap);
				context.put("reviewersMap", reviewersMap);
			}
			
			// try to put in grade overrides
			if (assignment.isGroup()) {
			    Map<String,Object> _ugrades = new HashMap<String,Object>();
			    Iterator<SubmitterSubmission> _ssubmits = userSubmissions.iterator();
			    while (_ssubmits.hasNext()) {
			        SubmitterSubmission _ss = _ssubmits.next();
				if (_ss != null && _ss.getSubmission() != null) {
			        	User[] _users = _ss.getSubmission().getSubmitters();
			        	for (int i=0; _users != null && i < _users.length; i ++) {
			            		String agrade = (StringUtils.trimToNull(assignment.getProperties().getProperty(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT))!=null) && (assignment.getContent().getTypeOfGrade() == Assignment.SCORE_GRADE_TYPE)?
			            				_ss.getSubmission().getGradeForUserInGradeBook(_users[i].getId())!=null?_ss.getSubmission().getGradeForUserInGradeBook(_users[i].getId()):_ss.getSubmission().getGradeForUser(_users[i].getId()):_ss.getSubmission().getGradeForUser(_users[i].getId());
			            		if (agrade != null) {
			                		_ugrades.put(
			                        	_users[i].getId(),
			                        	agrade);
			            		}	
			        	}
				}
			    }

			    context.put("value_grades", _ugrades);
			    Collection<String> _dups = checkForUsersInMultipleGroups(assignment, null, state, rb.getString("group.user.multiple.warning"));
			    if (_dups.size() > 0) {
			        context.put("usersinmultiplegroups", _dups);
			    }
			}

			// put the re-submission info into context
			assignment_resubmission_option_into_state(assignment, null, state);
			putTimePropertiesInContext(context, state, "Resubmit", ALLOW_RESUBMIT_CLOSEMONTH, ALLOW_RESUBMIT_CLOSEDAY, ALLOW_RESUBMIT_CLOSEYEAR, ALLOW_RESUBMIT_CLOSEHOUR, ALLOW_RESUBMIT_CLOSEMIN);
			assignment_resubmission_option_into_context(context, state);
		}

		TaggingManager taggingManager = (TaggingManager) ComponentManager
				.get("org.sakaiproject.taggable.api.TaggingManager");
		if (taggingManager.isTaggable() && assignment != null)
		{
			context.put("producer", ComponentManager
					.get("org.sakaiproject.assignment.taggable.api.AssignmentActivityProducer"));
			addProviders(context, state);
			addActivity(context, assignment);
			context.put("taggable", Boolean.valueOf(true));
		}

		context.put("submissionTypeTable", submissionTypeTable());
		context.put("attachments", state.getAttribute(ATTACHMENTS));
		
		
		context.put("contentTypeImageService", state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE));
		context.put("service", AssignmentService.getInstance());

		context.put("assignment_expand_flag", state.getAttribute(GRADE_ASSIGNMENT_EXPAND_FLAG));
		context.put("submission_expand_flag", state.getAttribute(GRADE_SUBMISSION_EXPAND_FLAG));

		add2ndToolbarFields(data, context);

		pagingInfoToContext(state, context);
		
		// put supplement item into context
		supplementItemIntoContext(state, context, assignment, null);
		
		// search context
		String searchString = (String) state.getAttribute(STATE_SEARCH) != null ? (String) state.getAttribute(STATE_SEARCH) : "";
		context.put("searchString", searchString);
		
		context.put("form_search", FORM_SEARCH);
		context.put("showSubmissionByFilterSearchOnly", state.getAttribute(SUBMISSIONS_SEARCH_ONLY) != null && ((Boolean) state.getAttribute(SUBMISSIONS_SEARCH_ONLY)) ? Boolean.TRUE:Boolean.FALSE);
		
		// letter grading
		letterGradeOptionsIntoContext(context);
		
		// ever set the default grade for no-submissions
		if (assignment != null && assignmentContent != null && assignmentContent.getTypeOfSubmission() == Assignment.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION)
		{
			// non-electronic submissions
			context.put("form_action", "eventSubmit_doSet_defaultNotGradedNonElectronicScore");
			context.put("form_label", rb.getFormattedMessage("not.graded.non.electronic.submission.grade", new Object[]{state.getAttribute(STATE_NUM_MESSAGES)}));
		}
		else
		{
			// other types of submissions
			context.put("form_action", "eventSubmit_doSet_defaultNoSubmissionScore");
			context.put("form_label", rb.getFormattedMessage("non.submission.grade", new Object[]{state.getAttribute(STATE_NUM_MESSAGES)}));
		}
		
		// show the reminder for download all url
		String downloadUrl = (String) state.getAttribute(STATE_DOWNLOAD_URL);
		if (downloadUrl != null)
		{
			context.put("download_url_reminder", rb.getString("download_url_reminder"));
			context.put("download_url_link", downloadUrl);
			context.put("download_url_link_label", rb.getString("download_url_link_label"));
			state.removeAttribute(STATE_DOWNLOAD_URL);
		}
		
		String template = (String) getContext(data).get("template");
		
		return template + TEMPLATE_INSTRUCTOR_GRADE_ASSIGNMENT;

	} // build_instructor_grade_assignment_context

	/**
	 * make sure the state variable VIEW_SUBMISSION_LIST_OPTION is not null
	 * @param state
	 */
	private void initViewSubmissionListOption(SessionState state) {
		if (state.getAttribute(VIEW_SUBMISSION_LIST_OPTION) == null
				&& (state.getAttribute(SUBMISSIONS_SEARCH_ONLY) == null
				|| !((Boolean) state.getAttribute(SUBMISSIONS_SEARCH_ONLY)).booleanValue()))
		{
			state.setAttribute(VIEW_SUBMISSION_LIST_OPTION, AssignmentConstants.ALL);
		}
	}

	/**
	 * put the supplement item information into context
	 * @param state
	 * @param context
	 * @param assignment
	 * @param s
	 */
	private void supplementItemIntoContext(SessionState state, Context context, Assignment assignment, AssignmentSubmission s) {

		String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
		
		// for model answer
		boolean allowViewModelAnswer = m_assignmentSupplementItemService.canViewModelAnswer(assignment, s);
		context.put("allowViewModelAnswer", allowViewModelAnswer);
		if (allowViewModelAnswer)
		{
			context.put("assignmentModelAnswerItem", m_assignmentSupplementItemService.getModelAnswer(assignment.getId()));
		}
	
		// for note item
		boolean allowReadAssignmentNoteItem = m_assignmentSupplementItemService.canReadNoteItem(assignment, contextString);
		context.put("allowReadAssignmentNoteItem", allowReadAssignmentNoteItem);
		if (allowReadAssignmentNoteItem)
		{
			context.put("assignmentNoteItem", m_assignmentSupplementItemService.getNoteItem(assignment.getId()));
		}
		// for all purpose item
		boolean allowViewAllPurposeItem = m_assignmentSupplementItemService.canViewAllPurposeItem(assignment);
		context.put("allowViewAllPurposeItem", allowViewAllPurposeItem);
		if (allowViewAllPurposeItem)
		{
			context.put("assignmentAllPurposeItem", m_assignmentSupplementItemService.getAllPurposeItem(assignment.getId()));
		}
	}
	
	/**
	 * build the instructor view of an assignment
	 */
	protected String build_instructor_view_assignment_context(VelocityPortlet portlet, Context context, RunData data,
			SessionState state)
	{
		context.put("tlang", rb);
		
		String assignmentId = (String) state.getAttribute(VIEW_ASSIGNMENT_ID);
		Assignment assignment = getAssignment(assignmentId, "build_instructor_view_assignment_context", state);
		if (assignment != null)
		{
			context.put("assignment", assignment);
			
			// put the resubmit information into context
			assignment_resubmission_option_into_context(context, state);
			
			// put creator information into context
			putCreatorIntoContext(context, assignment);
		}

		TaggingManager taggingManager = (TaggingManager) ComponentManager
				.get("org.sakaiproject.taggable.api.TaggingManager");
		if (taggingManager.isTaggable() && assignment != null)
		{
			Session session = SessionManager.getCurrentSession();
			List<DecoratedTaggingProvider> providers = addProviders(context, state);
			List<TaggingHelperInfo> activityHelpers = new ArrayList<TaggingHelperInfo>();
			AssignmentActivityProducer assignmentActivityProducer = (AssignmentActivityProducer) ComponentManager
					.get("org.sakaiproject.assignment.taggable.api.AssignmentActivityProducer");
			for (DecoratedTaggingProvider provider : providers)
			{
				TaggingHelperInfo helper = provider.getProvider()
						.getActivityHelperInfo(
								assignmentActivityProducer.getActivity(
										assignment).getReference());
				if (helper != null)
				{
					activityHelpers.add(helper);
				}
			}
			addActivity(context, assignment);
			context.put("activityHelpers", activityHelpers);
			context.put("taggable", Boolean.valueOf(true));
			
			addDecoUrlMapToContext(session, context, false);
		}

		context.put("currentTime", TimeService.newTime());
		context.put("submissionTypeTable", submissionTypeTable());
		context.put("hideAssignmentFlag", state.getAttribute(VIEW_ASSIGNMENT_HIDE_ASSIGNMENT_FLAG));
		context.put("hideStudentViewFlag", state.getAttribute(VIEW_ASSIGNMENT_HIDE_STUDENT_VIEW_FLAG));
		context.put("contentTypeImageService", state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE));
		context.put("honor_pledge_text", ServerConfigurationService.getString("assignment.honor.pledge", rb.getString("gen.honple2")));
		
		String template = (String) getContext(data).get("template");
		return template + TEMPLATE_INSTRUCTOR_VIEW_ASSIGNMENT;

	} // build_instructor_view_assignment_context


	private void putCreatorIntoContext(Context context, Assignment assignment) {
		// the creator 
		String creatorId = assignment.getCreator();
		try
		{
			User creator = UserDirectoryService.getUser(creatorId);
			context.put("creator", creator.getDisplayName());
		}
		catch (Exception ee)
		{
			context.put("creator", creatorId);
			M_log.warn(this + ":build_instructor_view_assignment_context " + ee.getMessage());
		}
	}

	/**
	 * build the instructor view of reordering assignments
	 */
	protected String build_instructor_reorder_assignment_context(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	{
		context.put("context", state.getAttribute(STATE_CONTEXT_STRING));
		
		List assignments = prepPage(state);
		
		context.put("assignments", assignments.iterator());
		context.put("assignmentsize", assignments.size());
		
		String sortedBy = (String) state.getAttribute(SORTED_BY);
		String sortedAsc = (String) state.getAttribute(SORTED_ASC);
		context.put("sortedBy", sortedBy);
		context.put("sortedAsc", sortedAsc);
	
		context.put("contentTypeImageService", state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE));
		context.put("userDirectoryService", UserDirectoryService.getInstance());
	
		String template = (String) getContext(data).get("template");
		return template + TEMPLATE_INSTRUCTOR_REORDER_ASSIGNMENT;
	
	} // build_instructor_reorder_assignment_context

	protected String build_student_review_edit_context(VelocityPortlet portlet, Context context, RunData data, SessionState state){
		int gradeType = -1;
		context.put("context", state.getAttribute(STATE_CONTEXT_STRING));
		List<PeerAssessmentItem> peerAssessmentItems = (List<PeerAssessmentItem>) state.getAttribute(PEER_ASSESSMENT_ITEMS);
		String assignmentId = (String) state.getAttribute(VIEW_ASSIGNMENT_ID);
		User sessionUser = (User) state.getAttribute(STATE_USER);
		String assessorId = sessionUser.getId();
		if(state.getAttribute(PEER_ASSESSMENT_ASSESSOR_ID) != null){
			assessorId = (String) state.getAttribute(PEER_ASSESSMENT_ASSESSOR_ID);
		}
		int factor = AssignmentService.getScaleFactor();
		int dec = (int)Math.log10(factor);
		Assignment assignment = getAssignment(assignmentId, "build_student_review_edit_context", state);
		if (assignment != null){
			context.put("assignment", assignment);
			if (assignment.getContent() != null)
			{
				gradeType = assignment.getContent().getTypeOfGrade();
				factor = assignment.getContent().getFactor();
				dec = (int)Math.log10(factor);
			}
			context.put("peerAssessmentInstructions", assignment.getPeerAssessmentInstructions() == null ? "" : assignment.getPeerAssessmentInstructions());
		}
		String submissionId = "";
		SecurityAdvisor secAdv = new SecurityAdvisor(){
			@Override
			public SecurityAdvice isAllowed(String userId, String function,
					String reference) {
				if("asn.submit".equals(function) || "asn.submit".equals(function) || "asn.grade".equals(function)){
					return SecurityAdvice.ALLOWED;
				}
				return null;
			}
		};
		AssignmentSubmission s = null;
		try{
			//surround with a try/catch/finally for the security advisor
			m_securityService.pushAdvisor(secAdv);
			s = getSubmission((String) state.getAttribute(GRADE_SUBMISSION_SUBMISSION_ID), "build_student_review_edit_context", state);
			m_securityService.popAdvisor(secAdv);
		}catch(Exception e){
			M_log.error(e.getMessage(), e);
		}finally{
			if(secAdv != null){
				m_securityService.popAdvisor(secAdv);
			}
		}
		if (s != null)
		{
			submissionId = s.getId();
			context.put("submission", s);

			ResourceProperties p = s.getProperties();
			if (p.getProperty(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT) != null)
			{
				context.put("prevFeedbackText", p.getProperty(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT));
			}

			if (p.getProperty(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT) != null)
			{
				context.put("prevFeedbackComment", p.getProperty(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT));
			}

			if (p.getProperty(PROP_SUBMISSION_PREVIOUS_FEEDBACK_ATTACHMENTS) != null)
			{
				context.put("prevFeedbackAttachments", getPrevFeedbackAttachments(p));
			}
			if ((s.getFeedbackText() == null) || (s.getFeedbackText().length() == 0))
			{
				context.put("value_feedback_text", s.getSubmittedText());
			}
			else
			{
				context.put("value_feedback_text", s.getFeedbackFormattedText());
			}

			context.put("value_feedback_text", s.getSubmittedText());
			List v = EntityManager.newReferenceList();
			Iterator attachments = s.getFeedbackAttachments().iterator();
			while (attachments.hasNext())
			{
				v.add(attachments.next());
			}
			context.put("value_feedback_attachment", v);
			state.setAttribute(ATTACHMENTS, v);
		}
		if(peerAssessmentItems != null && submissionId != null){
			//find the peerAssessmentItem for this submission:
			PeerAssessmentItem peerAssessmentItem = null;
			for(PeerAssessmentItem item : peerAssessmentItems){
				if(submissionId.equals(item.getSubmissionId())
						&& assessorId.equals(item.getAssessorUserId())){
					peerAssessmentItem = item;
					break;
				}
			}
			if(peerAssessmentItem != null){
				//check if current user is the peer assessor, if not, only display data (no editing)
				if(!sessionUser.getId().equals(peerAssessmentItem.getAssessorUserId())){
					context.put("view_only", true);
					try {
						User reviewer = UserDirectoryService.getUser(peerAssessmentItem.getAssessorUserId());
						context.put("reviewer", reviewer);
					} catch (UserNotDefinedException e) {
						M_log.error(e.getMessage(), e);
					}
				}else{
					context.put("view_only", false);
				}

				// get attachments for peer review item
				List<PeerAssessmentAttachment> attachments = assignmentPeerAssessmentService.getPeerAssessmentAttachments(peerAssessmentItem.getSubmissionId(),peerAssessmentItem.getAssessorUserId());
				List<Reference> attachmentRefList = new ArrayList<Reference>();
				if(attachments != null && !attachments.isEmpty()) {
					for(PeerAssessmentAttachment attachment : attachments) {
						try {
							Reference ref = m_entityManager.newReference(m_contentHostingService.getReference(attachment.getResourceId()));
							attachmentRefList.add(ref);
						} catch(Exception e) {
							M_log.warn(e.getMessage(), e);
						}
					}
					if(attachmentRefList != null && !attachmentRefList.isEmpty()) {
						context.put("peer_attachments", attachmentRefList);
						state.setAttribute(PEER_ATTACHMENTS, attachmentRefList);
					}
				} else {
					context.put("peer_attachments", attachmentRefList);
					state.setAttribute(PEER_ATTACHMENTS, attachmentRefList);
				}

				//scores are saved as whole values
				//so a score of 1.3 would be stored as 13
				//so a DB score of 13 needs to be 1.3:
				String decSeparator = FormattedText.getDecimalSeparator();
				if(peerAssessmentItem.getScore() != null){
					double score = peerAssessmentItem.getScore()/(double)factor;
					try {
							String rv = StringUtils.replace(Double.toString(score), (",".equals(decSeparator)?".":","), decSeparator);
							NumberFormat nbFormat = FormattedText.getNumberFormat(dec,dec,false);
							DecimalFormat dcformat = (DecimalFormat) nbFormat;
							Double dblGrade = dcformat.parse(rv).doubleValue();
							rv = nbFormat.format(dblGrade);
							context.put("value_grade", rv);
							context.put("display_grade", rv);
					}
					catch(Exception e){
						M_log.warn(this + ":build_student_review_edit_context: Parse Error in display_Grade peerAssesmentItem" + e.getMessage());
				}
				}else{
					context.put("value_grade", null);
					context.put("display_grade", "");
				}
				context.put("item_removed", peerAssessmentItem.isRemoved());
				context.put("value_feedback_comment", peerAssessmentItem.getComment());
				
				//set previous/next values
				List userSubmissionsState = state.getAttribute(STATE_PAGEING_TOTAL_ITEMS) != null ? (List) state.getAttribute(STATE_PAGEING_TOTAL_ITEMS):null;
				List<String> userSubmissions = new ArrayList<String>();
				boolean instructorView = false;
				if(userSubmissionsState != null && userSubmissionsState.size() > 0 && userSubmissionsState.get(0) instanceof SubmitterSubmission){
					//from instructor view
					for(SubmitterSubmission userSubmission : (List<SubmitterSubmission>) userSubmissionsState){
						if(!userSubmissions.contains(userSubmission.getSubmission().getId())
								&& userSubmission.getSubmission().getSubmitted()){
							userSubmissions.add(userSubmission.getSubmission().getId());
						}
					}
				}else{
					//student view
					for(PeerAssessmentItem item : peerAssessmentItems){
						if(!userSubmissions.contains(item.getSubmissionId()) && !item.isSubmitted()){
							userSubmissions.add(item.getSubmissionId());
						}
					}
				}
				if(userSubmissions != null){
					context.put("totalReviews", userSubmissions.size());
					//first setup map to make the navigation logic easier:
					Map<String, List<PeerAssessmentItem>> itemMap = new HashMap<String, List<PeerAssessmentItem>>();
					for(String userSubmissionId : userSubmissions){
						for (PeerAssessmentItem item : peerAssessmentItems){
							if(userSubmissionId.equals(item.getSubmissionId())){
								List<PeerAssessmentItem> items = itemMap.get(userSubmissionId);
								if(items == null){
									items = new ArrayList<PeerAssessmentItem>();
								}
								items.add(item);
								itemMap.put(item.getSubmissionId(), items);
							}
						}
					}
					for(int i = 0; i < userSubmissions.size(); i++){
						String userSubmissionId = userSubmissions.get(i);
						if(userSubmissionId.equals(submissionId)){
							//we found the right submission, now find the items
							context.put("reviewNumber", (i + 1));
							List<PeerAssessmentItem> submissionItems = itemMap.get(submissionId);
							if(submissionItems != null){
								for (int j = 0; j < submissionItems.size(); j++){
									PeerAssessmentItem item = submissionItems.get(j);
									if(item.getAssessorUserId().equals(assessorId)){
										context.put("anonNumber", i + 1);
										boolean goPT = false;
										boolean goNT = false;
										if ((i - 1) >= 0 || (j - 1) >= 0)
										{
											goPT = true;
										}
										if ((i + 1) < userSubmissions.size() || (j + 1) < submissionItems.size())
										{
											goNT = true;
										}
										context.put("goPTButton", Boolean.valueOf(goPT));
										context.put("goNTButton", Boolean.valueOf(goNT));

										if (j>0)
										{
											// retrieve the previous submission id
											context.put("prevSubmissionId", (submissionItems.get(j-1).getSubmissionId()));
											context.put("prevAssessorId", (submissionItems.get(j-1).getAssessorUserId()));
										}else if(i > 0){
											//go to previous submission and grab the last item in that list
											int k = i - 1;
											while(k >= 0 && !itemMap.containsKey(userSubmissions.get(k))){
												k--;
											}
											if(k >= 0 && itemMap.get(userSubmissions.get(k)).size() > 0){
												List<PeerAssessmentItem> pItems = itemMap.get(userSubmissions.get(k));
												PeerAssessmentItem pItem = pItems.get(pItems.size() - 1);
												context.put("prevSubmissionId", (pItem.getSubmissionId()));
												context.put("prevAssessorId", (pItem.getAssessorUserId()));
											}else{
												//no previous option, set to false
												context.put("goPTButton", Boolean.valueOf(false));
											}
										}

										if (j < submissionItems.size() - 1)
										{
											// retrieve the next submission id
											context.put("nextSubmissionId", (submissionItems.get(j+1).getSubmissionId()));
											context.put("nextAssessorId", (submissionItems.get(j+1).getAssessorUserId()));
										}else if (i < userSubmissions.size() - 1){
											//go to previous submission and grab the last item in that list
											int k = i + 1;
											while(k < userSubmissions.size() && !itemMap.containsKey(userSubmissions.get(k))){
												k++;
											}
											if(k < userSubmissions.size() && itemMap.get(userSubmissions.get(k)).size() > 0){
												List<PeerAssessmentItem> pItems = itemMap.get(userSubmissions.get(k));
												PeerAssessmentItem pItem = pItems.get(0);
												context.put("nextSubmissionId", (pItem.getSubmissionId()));
												context.put("nextAssessorId", (pItem.getAssessorUserId()));
											}else{
												//no next option, set to false
												context.put("goNTButton", Boolean.valueOf(false));
											}
										}
									}
								}
							}
						}
					}
				}
			}
			
		}
		
		context.put("assignment_expand_flag", state.getAttribute(GRADE_SUBMISSION_ASSIGNMENT_EXPAND_FLAG));
		context.put("user", sessionUser);
		context.put("submissionTypeTable", submissionTypeTable());
		context.put("instructorAttachments", state.getAttribute(ATTACHMENTS));
		context.put("contentTypeImageService", state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE));
		context.put("service", AssignmentService.getInstance());
		// names
		context.put("name_grade_assignment_id", GRADE_SUBMISSION_ASSIGNMENT_ID);
		context.put("name_feedback_comment", GRADE_SUBMISSION_FEEDBACK_COMMENT);
		context.put("name_feedback_text", GRADE_SUBMISSION_FEEDBACK_TEXT);
		context.put("name_feedback_attachment", GRADE_SUBMISSION_FEEDBACK_ATTACHMENT);
		context.put("name_grade", GRADE_SUBMISSION_GRADE);
		context.put("name_allowResubmitNumber", AssignmentSubmission.ALLOW_RESUBMIT_NUMBER);

		// put supplement item into context
		try{
			//surround with a try/catch/finally for the security advisor
			m_securityService.pushAdvisor(secAdv);
			supplementItemIntoContext(state, context, assignment, null);
		}catch(Exception e){
			M_log.error(e.getMessage(), e);
		}finally{
			if(secAdv != null){
				m_securityService.popAdvisor(secAdv);
			}
		}
		// put the grade confirmation message if applicable
		if (state.getAttribute(GRADE_SUBMISSION_DONE) != null)
		{
			context.put("gradingDone", Boolean.TRUE);
			state.removeAttribute(GRADE_SUBMISSION_DONE);
			if(state.getAttribute(PEER_ASSESSMENT_REMOVED_STATUS) != null){
				context.put("itemRemoved", state.getAttribute(PEER_ASSESSMENT_REMOVED_STATUS));
				state.removeAttribute(PEER_ASSESSMENT_REMOVED_STATUS);
			}
		}
		// put the grade confirmation message if applicable
		if (state.getAttribute(GRADE_SUBMISSION_SUBMIT) != null)
		{
			context.put("gradingSubmit", Boolean.TRUE);
			state.removeAttribute(GRADE_SUBMISSION_SUBMIT);
		}

		String template = (String) getContext(data).get("template");
		return template + TEMPLATE_STUDENT_REVIEW_EDIT;
	}

	/**
	 * build the instructor view to view the list of students for an assignment
	 */
	protected String build_instructor_view_students_assignment_context(VelocityPortlet portlet, Context context, RunData data,
			SessionState state)
	{
		// cleaning from view attribute
		state.removeAttribute(FROM_VIEW);

		String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);

		initViewSubmissionListOption(state);
		String allOrOneGroup = (String) state.getAttribute(VIEW_SUBMISSION_LIST_OPTION);
		String search = (String) state.getAttribute(VIEW_SUBMISSION_SEARCH);
		Boolean searchFilterOnly = (state.getAttribute(SUBMISSIONS_SEARCH_ONLY) != null && ((Boolean) state.getAttribute(SUBMISSIONS_SEARCH_ONLY)) ? Boolean.TRUE:Boolean.FALSE);

		// get the realm and its member
		List studentMembers = new ArrayList();
		List assignments = AssignmentService.getListAssignmentsForContext(contextString);

		boolean hasAtLeastOneAnonAssigment = false;
		for( Object obj : assignments)
		{
			Assignment assignment = (Assignment) obj;
			if( AssignmentService.getInstance().assignmentUsesAnonymousGrading( assignment ) )
			{
				hasAtLeastOneAnonAssigment = true;
				break;
			}
		}
		context.put( "hasAtLeastOneAnonAssignment", hasAtLeastOneAnonAssigment );

		//No duplicates
		Set allowSubmitMembers = new HashSet();
		for( Object obj : assignments)
		{
			Assignment a = (Assignment) obj;
			List<String> submitterIds = AssignmentService.getSubmitterIdList(searchFilterOnly.toString(), allOrOneGroup, search, a.getReference(), contextString);
			allowSubmitMembers.addAll(submitterIds);
		}
		for (Iterator allowSubmitMembersIterator=allowSubmitMembers.iterator(); allowSubmitMembersIterator.hasNext();)
		{
			// get user
			try
			{
				String userId = (String) allowSubmitMembersIterator.next();
				User user = UserDirectoryService.getUser(userId);
				studentMembers.add(user);
			}
			catch (Exception ee)
			{
				M_log.warn(this + ":build_instructor_view_student_assignment_context " + ee.getMessage());
			}
		}
		
		context.put("studentMembers", new SortedIterator(studentMembers.iterator(), new AssignmentComparator(state, SORTED_USER_BY_SORTNAME, Boolean.TRUE.toString())));
		context.put("assignmentService", AssignmentService.getInstance());
		context.put("userService", UserDirectoryService.getInstance());
		context.put("viewGroup", state.getAttribute(VIEW_SUBMISSION_LIST_OPTION));

		context.put("searchString", state.getAttribute(VIEW_SUBMISSION_SEARCH) != null ? state.getAttribute(VIEW_SUBMISSION_SEARCH) : "");
		context.put("showSubmissionByFilterSearchOnly", state.getAttribute(SUBMISSIONS_SEARCH_ONLY) != null && ((Boolean) state.getAttribute(SUBMISSIONS_SEARCH_ONLY)) ? Boolean.TRUE:Boolean.FALSE);
		
		if (AssignmentService.getAllowGroupAssignments()) {
			Collection groups = getAllGroupsInSite(contextString);
			context.put("groups", new SortedIterator(groups.iterator(), new AssignmentComparator(state, SORTED_BY_GROUP_TITLE, Boolean.TRUE.toString() )));
		}
		
		HashMap showStudentAssignments = new HashMap();
		if (state.getAttribute(STUDENT_LIST_SHOW_TABLE) != null)
		{
			Set showStudentListSet = (Set) state.getAttribute(STUDENT_LIST_SHOW_TABLE);
			context.put("studentListShowSet", showStudentListSet);
			for (Iterator showStudentListSetIterator=showStudentListSet.iterator(); showStudentListSetIterator.hasNext();)
			{
				// get user
				try
				{
					String userId = (String) showStudentListSetIterator.next();
					User user = UserDirectoryService.getUser(userId);
					
					// sort the assignments into the default order before adding
					Iterator assignmentSorter = AssignmentService.getAssignmentsForContext(contextString, userId);
					// filter to obtain only grade-able assignments
					List rv = new ArrayList();
					while (assignmentSorter.hasNext())
					{
						Assignment a = (Assignment) assignmentSorter.next();
						if (AssignmentService.allowGradeSubmission(a.getReference()))
						{
							rv.add(a);
						}
					}
					Iterator assignmentSortFinal = new SortedIterator(rv.iterator(), new AssignmentComparator(state, SORTED_BY_DEFAULT, Boolean.TRUE.toString()));

					showStudentAssignments.put(user, assignmentSortFinal);
				}
				catch (Exception ee)
				{
					M_log.warn(this + ":build_instructor_view_student_assignment_context " + ee.getMessage());
				}
			}
			
		}

		context.put("studentAssignmentsTable", showStudentAssignments);

		add2ndToolbarFields(data, context);

		String template = (String) getContext(data).get("template");
		return template + TEMPLATE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT;

	} // build_instructor_view_students_assignment_context

	/**
	 * build the instructor view to report the submissions
	 */
	protected String build_instructor_report_submissions(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	{
		List submissions = prepPage( state );
		context.put("submissions", submissions);

		List<SubmitterSubmission> allSubmissions = (List) state.getAttribute(STATE_PAGEING_TOTAL_ITEMS);
		boolean hasAtLeastOneAnonAssigment = false;
		for( SubmitterSubmission submission : allSubmissions )
		{
			Assignment assignment = submission.getSubmission().getAssignment();
			if( AssignmentService.getInstance().assignmentUsesAnonymousGrading( assignment ) )
			{
				hasAtLeastOneAnonAssigment = true;
				break;
			}
		}
		context.put( "hasAtLeastOneAnonAssignment", hasAtLeastOneAnonAssigment );

		context.put("sortedBy", (String) state.getAttribute(SORTED_SUBMISSION_BY));
		context.put("sortedAsc", (String) state.getAttribute(SORTED_SUBMISSION_ASC));

		context.put("sortedBy_lastName", SORTED_GRADE_SUBMISSION_BY_LASTNAME);
		context.put("sortedBy_submitTime", SORTED_GRADE_SUBMISSION_BY_SUBMIT_TIME);
		context.put("sortedBy_grade", SORTED_GRADE_SUBMISSION_BY_GRADE);
		context.put("sortedBy_status", SORTED_GRADE_SUBMISSION_BY_STATUS);
		context.put("sortedBy_released", SORTED_GRADE_SUBMISSION_BY_RELEASED);
		//context.put("sortedBy_assignment", SORTED_GRADE_SUBMISSION_BY_ASSIGNMENT);
		//context.put("sortedBy_maxGrade", SORTED_GRADE_SUBMISSION_BY_MAX_GRADE);
		
		// get current site
		String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
		context.put("searchString", state.getAttribute(VIEW_SUBMISSION_SEARCH) != null ? state.getAttribute(VIEW_SUBMISSION_SEARCH) : "");

		context.put("view", MODE_INSTRUCTOR_REPORT_SUBMISSIONS);
		context.put("viewString", state.getAttribute(VIEW_SUBMISSION_LIST_OPTION)!=null?state.getAttribute(VIEW_SUBMISSION_LIST_OPTION):"");
		context.put("searchString", state.getAttribute(VIEW_SUBMISSION_SEARCH)!=null?state.getAttribute(VIEW_SUBMISSION_SEARCH):"");

		context.put("showSubmissionByFilterSearchOnly", state.getAttribute(SUBMISSIONS_SEARCH_ONLY) != null && ((Boolean) state.getAttribute(SUBMISSIONS_SEARCH_ONLY)) ? Boolean.TRUE:Boolean.FALSE);

		if (AssignmentService.getAllowGroupAssignments()) {
			Collection groups = getAllGroupsInSite(contextString);
			context.put("groups", new SortedIterator(groups.iterator(), new AssignmentComparator(state, SORTED_BY_GROUP_TITLE, Boolean.TRUE.toString() )));
		}
		
		 
		add2ndToolbarFields(data, context);

		String view = (String)state.getAttribute(VIEW_SUBMISSION_LIST_OPTION);
		if (view != null && !AssignmentConstants.ALL.equals(view)) {
		context.put("accessPointUrl", ServerConfigurationService.getAccessUrl()
				+ AssignmentService.gradesSpreadsheetReference(view.substring(view.indexOf(Entity.SEPARATOR)+1), null));
		} else {
		context.put("accessPointUrl", ServerConfigurationService.getAccessUrl()
				+ AssignmentService.gradesSpreadsheetReference(contextString, null));
		}

		pagingInfoToContext(state, context);

		context.put("assignmentService", AssignmentService.getInstance());

		String template = (String) getContext(data).get("template");
		return template + TEMPLATE_INSTRUCTOR_REPORT_SUBMISSIONS;

	} // build_instructor_report_submissions
	
	// Is Gradebook defined for the site?
	protected boolean isGradebookDefined()
	{
		boolean rv = false;
		try
		{
			GradebookService g = (GradebookService) ComponentManager.get("org.sakaiproject.service.gradebook.GradebookService");
			String gradebookUid = ToolManager.getCurrentPlacement().getContext();
			if (g.isGradebookDefined(gradebookUid) && (g.currentUserHasEditPerm(gradebookUid) || g.currentUserHasGradingPerm(gradebookUid)))
			{
				rv = true;
			}
		}
		catch (Exception e)
		{
			M_log.debug(this + "isGradebookDefined " + rb.getFormattedMessage("addtogradebook.alertMessage", new Object[]{e.getMessage()}));
		}

		return rv;

	} // isGradebookDefined()
	
	/**
	 * build the instructor view to download/upload information from archive file
	 */
	protected String build_instructor_download_upload_all(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	{
		String view = (String) state.getAttribute(VIEW_SUBMISSION_LIST_OPTION);
		boolean download = (((String) state.getAttribute(STATE_MODE)).equals(MODE_INSTRUCTOR_DOWNLOAD_ALL));
		
		context.put("download", Boolean.valueOf(download));
		context.put("hasSubmissionText", state.getAttribute(UPLOAD_ALL_HAS_SUBMISSION_TEXT));
		context.put("hasSubmissionAttachment", state.getAttribute(UPLOAD_ALL_HAS_SUBMISSION_ATTACHMENT));
		context.put("hasGradeFile", state.getAttribute(UPLOAD_ALL_HAS_GRADEFILE));
		String gradeFileFormat = (String) state.getAttribute(UPLOAD_ALL_GRADEFILE_FORMAT);
		if (gradeFileFormat==null) gradeFileFormat="csv";
		context.put("gradeFileFormat", gradeFileFormat);		
		context.put("hasComments", state.getAttribute(UPLOAD_ALL_HAS_COMMENTS));
		context.put("hasFeedbackText", state.getAttribute(UPLOAD_ALL_HAS_FEEDBACK_TEXT));
		context.put("hasFeedbackAttachment", state.getAttribute(UPLOAD_ALL_HAS_FEEDBACK_ATTACHMENT));
		context.put("releaseGrades", state.getAttribute(UPLOAD_ALL_RELEASE_GRADES));
		// SAK-19147
		context.put("withoutFolders", state.getAttribute(UPLOAD_ALL_WITHOUT_FOLDERS));
		context.put("enableFlatDownload", ServerConfigurationService.getBoolean("assignment.download.flat", false));
		String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
		context.put("contextString", contextString);
		context.put("accessPointUrl", (ServerConfigurationService.getAccessUrl()).concat(AssignmentService.submissionsZipReference(
				contextString, (String) state.getAttribute(EXPORT_ASSIGNMENT_REF))));

		String assignmentRef = (String) state.getAttribute(EXPORT_ASSIGNMENT_REF);
		Assignment a = getAssignment(assignmentRef, "build_instructor_download_upload_all", state);
		if (a != null)
		{
			
			String accessPointUrl = ServerConfigurationService.getAccessUrl().concat(AssignmentService.submissionsZipReference(
					contextString, assignmentRef));
			context.put("accessPointUrl", accessPointUrl);
			
			int submissionType = a.getContent().getTypeOfSubmission();
			// if the assignment is of text-only or allow both text and attachment, include option for uploading student submit text
			context.put("includeSubmissionText", Boolean.valueOf(Assignment.TEXT_ONLY_ASSIGNMENT_SUBMISSION == submissionType || Assignment.TEXT_AND_ATTACHMENT_ASSIGNMENT_SUBMISSION == submissionType));
			
			// if the assignment is of attachment-only or allow both text and attachment, include option for uploading student attachment
			context.put("includeSubmissionAttachment", Boolean.valueOf(Assignment.ATTACHMENT_ONLY_ASSIGNMENT_SUBMISSION == submissionType || Assignment.TEXT_AND_ATTACHMENT_ASSIGNMENT_SUBMISSION == submissionType || Assignment.SINGLE_ATTACHMENT_SUBMISSION == submissionType));
		
			context.put("viewString", state.getAttribute(VIEW_SUBMISSION_LIST_OPTION)!=null?state.getAttribute(VIEW_SUBMISSION_LIST_OPTION):"");
			
			context.put("searchString", state.getAttribute(VIEW_SUBMISSION_SEARCH)!=null?state.getAttribute(VIEW_SUBMISSION_SEARCH):"");
			
			context.put("showSubmissionByFilterSearchOnly", state.getAttribute(SUBMISSIONS_SEARCH_ONLY) != null && ((Boolean) state.getAttribute(SUBMISSIONS_SEARCH_ONLY)) ? Boolean.TRUE:Boolean.FALSE);
		}

		String template = (String) getContext(data).get("template");
		return template + TEMPLATE_INSTRUCTOR_UPLOAD_ALL;

	} // build_instructor_upload_all
	
   /**
    ** Retrieve tool title from Tool configuration file or use default
    ** (This should return i18n version of tool title if available)
    **/
   private String getToolTitle()
   {
      Tool tool = ToolManager.getTool(ASSIGNMENT_TOOL_ID);
      String toolTitle = null;

      if (tool == null)
        toolTitle = "Assignments";
      else
        toolTitle = tool.getTitle();

      return toolTitle;
   }

	/**
	 * integration with gradebook
	 *
	 * @param state
	 * @param assignmentRef Assignment reference
	 * @param associateGradebookAssignment The title for the associated GB assignment
	 * @param addUpdateRemoveAssignment "add" for adding the assignment; "update" for updating the assignment; "remove" for remove assignment
	 * @param oldAssignment_title The original assignment title
	 * @param newAssignment_title The updated assignment title
	 * @param newAssignment_maxPoints The maximum point of the assignment
	 * @param newAssignment_dueTime The due time of the assignment
	 * @param submissionRef Any submission grade need to be updated? Do bulk update if null
	 * @param updateRemoveSubmission "update" for update submission;"remove" for remove submission
	 */
   protected void integrateGradebook (SessionState state, String assignmentRef, String associateGradebookAssignment, String addUpdateRemoveAssignment, String oldAssignment_title, String newAssignment_title, int newAssignment_maxPoints, Time newAssignment_dueTime, String submissionRef, String updateRemoveSubmission, long category)
   {   
		associateGradebookAssignment = StringUtils.trimToNull(associateGradebookAssignment);

		// add or remove external grades to gradebook
		// a. if Gradebook does not exists, do nothing, 'cos setting should have been hidden
		// b. if Gradebook exists, just call addExternal and removeExternal and swallow any exception. The
		// exception are indication that the assessment is already in the Gradebook or there is nothing
		// to remove.
		String assignmentToolTitle = getToolTitle();

		GradebookService g = (GradebookService) ComponentManager.get("org.sakaiproject.service.gradebook.GradebookService");
		GradebookExternalAssessmentService gExternal = (GradebookExternalAssessmentService) ComponentManager.get("org.sakaiproject.service.gradebook.GradebookExternalAssessmentService");
		
		String gradebookUid = ToolManager.getCurrentPlacement().getContext();
		if (g.isGradebookDefined(gradebookUid) && g.currentUserHasGradingPerm(gradebookUid))
		{
			boolean isExternalAssignmentDefined=gExternal.isExternalAssignmentDefined(gradebookUid, assignmentRef);
			boolean isExternalAssociateAssignmentDefined = gExternal.isExternalAssignmentDefined(gradebookUid, associateGradebookAssignment);
			boolean isAssignmentDefined = g.isAssignmentDefined(gradebookUid, associateGradebookAssignment);

			if (addUpdateRemoveAssignment != null)
			{
				Assignment a = getAssignment(assignmentRef, "integrateGradebook", state);
				// add an entry into Gradebook for newly created assignment or modified assignment, and there wasn't a correspond record in gradebook yet
				if ((addUpdateRemoveAssignment.equals(AssignmentService.GRADEBOOK_INTEGRATION_ADD) || ("update".equals(addUpdateRemoveAssignment) && !isExternalAssignmentDefined)) && associateGradebookAssignment == null)
				{
					// add assignment into gradebook
					try
					{
						// add assignment to gradebook
						gExternal.addExternalAssessment(gradebookUid, assignmentRef, null, newAssignment_title, newAssignment_maxPoints/(double)a.getContent().getFactor(), new Date(newAssignment_dueTime.getTime()), assignmentToolTitle, false, category != -1?Long.valueOf(category):null);
					}
					catch (AssignmentHasIllegalPointsException e)
					{
						addAlert(state, rb.getString("addtogradebook.illegalPoints"));
						M_log.warn(this + ":integrateGradebook " + e.getMessage());
					}
					catch (ConflictingAssignmentNameException e)
					{
						// add alert prompting for change assignment title
						addAlert(state, rb.getFormattedMessage("addtogradebook.nonUniqueTitle", new Object[]{"\"" + newAssignment_title + "\""}));
						M_log.warn(this + ":integrateGradebook " + e.getMessage());
					}
					catch (Exception e)
					{
						M_log.warn(this + ":integrateGradebook " + e.getMessage());
					}
				}
				else if ("update".equals(addUpdateRemoveAssignment))
				{
					if (associateGradebookAssignment != null && isExternalAssociateAssignmentDefined)
					{
						// if there is an external entry created in Gradebook based on this assignment, update it
						try
						{
						    // update attributes if the GB assignment was created for the assignment
						    gExternal.updateExternalAssessment(gradebookUid, associateGradebookAssignment, null, newAssignment_title, newAssignment_maxPoints/(double)a.getContent().getFactor(), new Date(newAssignment_dueTime.getTime()), false);
						}
					    catch(Exception e)
				        {
					    	addAlert(state, rb.getFormattedMessage("cannotfin_assignment", new Object[]{assignmentRef}));
					    	M_log.warn(this + ":integrateGradebook " + rb.getFormattedMessage("cannotfin_assignment", new Object[]{assignmentRef}));
				        }
					}
				}	// addUpdateRemove != null
				else if ("remove".equals(addUpdateRemoveAssignment))
				{
					// remove assignment and all submission grades
					removeNonAssociatedExternalGradebookEntry((String) state.getAttribute(STATE_CONTEXT_STRING), assignmentRef, associateGradebookAssignment, gExternal, gradebookUid);
				}
			}

			if (updateRemoveSubmission != null)
			{
				Assignment a = getAssignment(assignmentRef, "integrateGradebook", state);
				
				if (a != null)
				{
					String propAddToGradebook = a.getProperties().getProperty(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK);
					if ("update".equals(updateRemoveSubmission)
					        && (StringUtils.equals( propAddToGradebook, AssignmentService.GRADEBOOK_INTEGRATION_ADD) 
							        || StringUtils.equals( propAddToGradebook, AssignmentService.GRADEBOOK_INTEGRATION_ASSOCIATE))
							&& a.getContent().getTypeOfGrade() == Assignment.SCORE_GRADE_TYPE)
					{
						if (submissionRef == null)
						{
							// bulk add all grades for assignment into gradebook
							Iterator submissions = AssignmentService.getSubmissions(a).iterator();

							//Assignment scores map
							Map<String, String> sm = new HashMap<String, String>();
							//Assignment comments map, though doesn't look like there's any way to update comments in bulk in the UI yet
							Map<String, String> cm = new HashMap<String, String>();

							// any score to copy over? get all the assessmentGradingData and copy over
							while (submissions.hasNext())
							{
								AssignmentSubmission aSubmission = (AssignmentSubmission) submissions.next();
								if (aSubmission.getGradeReleased())
								{
									User[] submitters = aSubmission.getSubmitters();
									String gradeString = StringUtils.trimToNull(aSubmission.getGrade(false));
									String commentString = FormattedText.convertFormattedTextToPlaintext(aSubmission.getFeedbackComment());

									String grade = gradeString != null ? displayGrade(state,gradeString, a.getContent().getFactor()) : null;
									for (int i=0; submitters != null && i < submitters.length; i++) {
 										String submitterId = submitters[i].getId();
										String gradeStringToUse = (a.isGroup() && aSubmission.getGradeForUser(submitterId) != null)
										        ? aSubmission.getGradeForUser(submitterId): grade;
										sm.put(submitterId, gradeStringToUse);
										cm.put(submitterId, commentString);
									}
								}
							}

							// need to update only when there is at least one submission
							if (!sm.isEmpty())
							{
								if (associateGradebookAssignment != null)
								{
									if (isExternalAssociateAssignmentDefined)
									{
										// the associated assignment is externally maintained
										gExternal.updateExternalAssessmentScoresString(gradebookUid, associateGradebookAssignment, sm);
										gExternal.updateExternalAssessmentComments(gradebookUid, associateGradebookAssignment, cm);
									}
									else if (isAssignmentDefined)
									{
									    Long associateGradebookAssignmentId = g.getAssignment(gradebookUid, associateGradebookAssignment).getId();
										// the associated assignment is internal one, update records one by one
										for (Map.Entry<String, String> entry : sm.entrySet())
										{
											String submitterId = (String) entry.getKey();
											String grade = StringUtils.trimToNull(displayGrade(state, (String) sm.get(submitterId), a.getContent().getFactor()));
											if (grade != null)
											{
												g.setAssignmentScoreString(gradebookUid, associateGradebookAssignmentId, submitterId, grade, "");
												String comment = StringUtils.isNotEmpty(cm.get(submitterId)) ? cm.get(submitterId) : "";
												g.setAssignmentScoreComment(gradebookUid, associateGradebookAssignmentId, submitterId, comment);
											}
										}
									}
								}
								else if (isExternalAssignmentDefined)
								{
									gExternal.updateExternalAssessmentScoresString(gradebookUid, assignmentRef, sm);
									gExternal.updateExternalAssessmentComments(gradebookUid, associateGradebookAssignment, cm);
								}
							}
						}
						else
						{
							// only update one submission
							AssignmentSubmission aSubmission = getSubmission(submissionRef, "integrateGradebook", state);
							if (aSubmission != null)
							{
								int factor = aSubmission.getAssignment().getContent().getFactor();
								User[] submitters = aSubmission.getSubmitters();
								String gradeString = displayGrade(state, StringUtils.trimToNull(aSubmission.getGrade(false)), factor);
								for (int i=0; submitters != null && i < submitters.length; i++) {
								    String gradeStringToUse = (a.isGroup() && aSubmission.getGradeForUser(submitters[i].getId()) != null) 
								            ? aSubmission.getGradeForUser(submitters[i].getId()): gradeString;
								    //Gradebook only supports plaintext strings
								    String commentString = FormattedText.convertFormattedTextToPlaintext(aSubmission.getFeedbackComment());
									if (associateGradebookAssignment != null)
									{
										if (gExternal.isExternalAssignmentDefined(gradebookUid, associateGradebookAssignment))
										{
											// the associated assignment is externally maintained
											gExternal.updateExternalAssessmentScore(gradebookUid, associateGradebookAssignment, submitters[i].getId(),
													(gradeStringToUse != null && aSubmission.getGradeReleased()) ? gradeStringToUse : "");
											gExternal.updateExternalAssessmentComment(gradebookUid, associateGradebookAssignment, submitters[i].getId(),
													(commentString != null && aSubmission.getGradeReleased()) ? commentString : "");
										}
										else if (g.isAssignmentDefined(gradebookUid, associateGradebookAssignment))
										{
										    Long associateGradebookAssignmentId = g.getAssignment(gradebookUid, associateGradebookAssignment).getId();
											// the associated assignment is internal one, update records
											g.setAssignmentScoreString(gradebookUid, associateGradebookAssignmentId, submitters[i].getId(),
													(gradeStringToUse != null && aSubmission.getGradeReleased()) ? gradeStringToUse : "", "");
											g.setAssignmentScoreComment(gradebookUid, associateGradebookAssignmentId, submitters[i].getId(), 
											        (commentString != null && aSubmission.getGradeReleased()) ? commentString : "");
										}
									}
									else
									{
										gExternal.updateExternalAssessmentScore(gradebookUid, assignmentRef, submitters[i].getId(),
												(gradeStringToUse != null && aSubmission.getGradeReleased()) ? gradeStringToUse : "");
										gExternal.updateExternalAssessmentComment(gradebookUid, assignmentRef, submitters[i].getId(),
										        (commentString != null && aSubmission.getGradeReleased()) ? commentString : "");
									}
								}
							}
						}

					}
					else if ("remove".equals(updateRemoveSubmission))
					{
						if (submissionRef == null)
						{
							// remove all submission grades (when changing the associated entry in Gradebook)
							Iterator submissions = AssignmentService.getSubmissions(a).iterator();

							// any score to copy over? get all the assessmentGradingData and copy over
							while (submissions.hasNext())
							{
								AssignmentSubmission aSubmission = (AssignmentSubmission) submissions.next();
								if (aSubmission.getGrade(false) != null) {
									User[] submitters = aSubmission.getSubmitters();
									for (int i=0; submitters != null && i < submitters.length; i++) {
										if (isExternalAssociateAssignmentDefined)
										{
											// if the old associated assignment is an external maintained one
											gExternal.updateExternalAssessmentScore(gradebookUid, associateGradebookAssignment, submitters[i].getId(), null);
										}
										else if (isAssignmentDefined)
										{
											g.setAssignmentScoreString(gradebookUid, associateGradebookAssignment, submitters[i].getId(), "0", assignmentToolTitle);
										}
									}
								}
							}
						}
						else
						{
							// remove only one submission grade
							AssignmentSubmission aSubmission = getSubmission(submissionRef, "integrateGradebook", state);
							if (aSubmission != null)
							{
								User[] submitters = aSubmission.getSubmitters();
								for (int i=0; submitters != null && i < submitters.length; i++) {
 									
									if (isExternalAssociateAssignmentDefined)
									{
										// external assignment
										gExternal.updateExternalAssessmentScore(gradebookUid, assignmentRef, submitters[i].getId(), null);
									}
									else if (isAssignmentDefined)
									{
										// gb assignment
										g.setAssignmentScoreString(gradebookUid, associateGradebookAssignment, submitters[i].getId(), "0", "");
									}
								}
							}
						}
					}
				}
			}
		}
	} // integrateGradebook

	/**
	 * Go to the instructor view
	 */
	public void doView_instructor(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
		state.setAttribute(SORTED_BY, SORTED_BY_DEFAULT);
		state.setAttribute(SORTED_ASC, Boolean.TRUE.toString());

	} // doView_instructor

	/**
	 * Go to the student view
	 */
	public void doView_student(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// to the student list of assignment view
		state.setAttribute(SORTED_BY, SORTED_BY_DEFAULT);
		state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);

	} // doView_student


	public void doView_submission_evap(RunData data){
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(INVOKE, INVOKE_BY_LINK);
		doView_submission(data);
	}

	

	/**
	 * Action is to view the content of one specific assignment submission
	 */
	public void doView_submission(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// reset the submission context
		resetViewSubmission(state);

		ParameterParser params = data.getParameters();
		String assignmentReference = params.getString("assignmentReference");
		state.setAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE, assignmentReference);

		User u = (User) state.getAttribute(STATE_USER);

		// redirect student to doView_grade if they clicked an old link
		String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
		Assignment a = getAssignment(assignmentReference, "doView_submission", state);
		if (a != null && !AssignmentService.canSubmit(contextString, a))
		{
			AssignmentSubmission submission = null;
			try
			{
				submission = AssignmentService.getSubmission(assignmentReference, u);
			}
			catch (Exception e)
			{
				String userId = u == null ? "" : u.getId();
				addAlert(state, rb.getFormattedMessage("cannotfin_submission_1", new String[]{assignmentReference, userId}));
			}
			if (submission != null)
			{
				String submissionReference = submission.getReference();
				prepareStudentViewGrade(state, submissionReference);
				return;
			}
		}

		String submitterId = params.get("submitterId");
		if (submitterId != null && (AssignmentService.allowGradeSubmission(assignmentReference))) {
		    try {
		        u = UserDirectoryService.getUser(submitterId);
		        state.setAttribute("student", u);
		    } catch (UserNotDefinedException ex) {
		        M_log.warn(this + ":doView_submission cannot find user with id " + submitterId + " " + ex.getMessage());
		    }
		}

		if (a != null)
		{
			AssignmentSubmission submission = getSubmission(assignmentReference, u, "doView_submission", state);
			if (submission != null)
			{
				state.setAttribute(VIEW_SUBMISSION_TEXT, submission.getSubmittedText());
				state.setAttribute(VIEW_SUBMISSION_HONOR_PLEDGE_YES, (Boolean.valueOf(submission.getHonorPledgeFlag())).toString());
				List v = EntityManager.newReferenceList();
				Iterator l = submission.getSubmittedAttachments().iterator();
				while (l.hasNext())
				{
					v.add(l.next());
				}
				state.setAttribute(ATTACHMENTS, v);		
			}
			else
			{
				state.setAttribute(VIEW_SUBMISSION_HONOR_PLEDGE_YES, "false");
				state.setAttribute(ATTACHMENTS, EntityManager.newReferenceList());	
			}

			// put resubmission option into state
			assignment_resubmission_option_into_state(a, submission, state);

			// show submission view unless group submission with group error
			String _mode = MODE_STUDENT_VIEW_SUBMISSION;
			if (a.isGroup()) {
			    Collection<Group> groups = null;
			    Site st = null;
			    try {
			        st = SiteService.getSite((String) state.getAttribute(STATE_CONTEXT_STRING));
			        groups = getGroupsWithUser(u.getId(), a, st);
			        Collection<String> _dupUsers = checkForGroupsInMultipleGroups(a, groups, state, rb.getString("group.user.multiple.warning"));
			        if (_dupUsers.size() > 0) {
			            _mode = MODE_STUDENT_VIEW_GROUP_ERROR;
			        }
			    } catch (IdUnusedException iue) {
			        M_log.warn(this + ":doView_submission: Site not found!" + iue.getMessage());
			    }
			}
			state.setAttribute(STATE_MODE, _mode);

			if (submission != null)
			{
                // submission read event
                Event event = m_eventTrackingService.newEvent(AssignmentConstants.EVENT_ACCESS_ASSIGNMENT_SUBMISSION, submission.getId(),
                        false);
                m_eventTrackingService.post(event);
                LearningResourceStoreService lrss = (LearningResourceStoreService) ComponentManager
                        .get("org.sakaiproject.event.api.LearningResourceStoreService");
                if (null != lrss) {
                    lrss.registerStatement(getStatementForViewSubmittedAssignment(lrss.getEventActor(event), event, a.getTitle()), "assignment");
                }
			}
			else
			{
                // otherwise, the student just read assignment description and prepare for submission
                Event event = m_eventTrackingService.newEvent(AssignmentConstants.EVENT_ACCESS_ASSIGNMENT, a.getId(), false);
                m_eventTrackingService.post(event);
                LearningResourceStoreService lrss = (LearningResourceStoreService) ComponentManager
                        .get("org.sakaiproject.event.api.LearningResourceStoreService");
                if (null != lrss) {
                    lrss.registerStatement(getStatementForViewAssignment(lrss.getEventActor(event), event, a.getTitle()), "assignment");
                }
			}
		}

	} // doView_submission
	
	/**
	 * Dispatcher for view submission list options
	 */
	public void doView_submission_list_option(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		
		ParameterParser params = data.getParameters();
		String option = params.getString("option");
		if ("changeView".equals(option))
		{
			doChange_submission_list_option(data);
		}
		else if ("search".equals(option))
		{
			state.setAttribute(VIEW_SUBMISSION_SEARCH, params.getString("search"));
		}
		else if ("clearSearch".equals(option))
		{
			state.removeAttribute(VIEW_SUBMISSION_SEARCH);
		}
		else if ("download".equals(option))
		{
			// go to download all page
			doPrep_download_all(data);
		}
		else if ("upload".equals(option))
		{
			// go to upload all page
			doPrep_upload_all(data);
		}
		else if ("releaseGrades".equals(option))
		{
			// release all grades
			doRelease_grades(data);
		}

	} // doView_submission_list_option

	/**
	 * Action is to view the content of one specific assignment submission
	 */
	public void doChange_submission_list_option(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();
		String view = params.getString("viewgroup");
		//Case where two dropdowns on same page
		if (view == null) {
			view = params.getString("view");
		}
		state.setAttribute(VIEW_SUBMISSION_LIST_OPTION, view);

	} // doView_submission_list_option

	/**
	 * Preview of the submission
	 */
	public void doPreview_submission(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		ParameterParser params = data.getParameters();
		String aReference = (String) state.getAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE);
		state.setAttribute(PREVIEW_SUBMISSION_ASSIGNMENT_REFERENCE, aReference);
		Assignment a = getAssignment(aReference, "doPreview_submission", state);

		saveSubmitInputs(state, params);

		// retrieve the submission text (as formatted text)
		boolean checkForFormattingErrors = true; // the student is submitting something - so check for errors
		String text = processFormattedTextFromBrowser(state, params.getCleanString(VIEW_SUBMISSION_TEXT), checkForFormattingErrors);

		state.setAttribute(PREVIEW_SUBMISSION_TEXT, text);
		state.setAttribute(VIEW_SUBMISSION_TEXT, text);

		// assign the honor pledge attribute
		String honor_pledge_yes = params.getString(VIEW_SUBMISSION_HONOR_PLEDGE_YES);
		if (honor_pledge_yes == null)
		{
			honor_pledge_yes = "false";
		}
		state.setAttribute(PREVIEW_SUBMISSION_HONOR_PLEDGE_YES, honor_pledge_yes);
		state.setAttribute(VIEW_SUBMISSION_HONOR_PLEDGE_YES, honor_pledge_yes);

		// get attachment input and generate alert message according to assignment submission type
		checkSubmissionTextAttachmentInput(data, state, a, text);
		state.setAttribute(PREVIEW_SUBMISSION_ATTACHMENTS, state.getAttribute(ATTACHMENTS));
		
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute(STATE_MODE, MODE_STUDENT_PREVIEW_SUBMISSION);
		}
	} // doPreview_submission

	/**
	 * Preview of the grading of submission
	 */
	public void doPreview_grade_submission(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read user input
		readGradeForm(data, state, "read");

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_PREVIEW_GRADE_SUBMISSION);
		}

	} // doPreview_grade_submission

	/**
	 * Action is to end the preview submission process
	 */
	public void doDone_preview_submission(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// back to the student list view of assignments
		state.setAttribute(STATE_MODE, MODE_STUDENT_VIEW_SUBMISSION);

	} // doDone_preview_submission

	/**
	 * Action is to end the view assignment process
	 */
	public void doDone_view_assignment(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// back to the student list view of assignments
		state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);

	} // doDone_view_assignments

	/**
	 * Action is to end the preview new assignment process
	 */
	public void doDone_preview_new_assignment(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// back to the new assignment page
		state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_NEW_EDIT_ASSIGNMENT);

	} // doDone_preview_new_assignment

	/**
	 * Action is to end the user view assignment process and redirect him to the assignment list view
	 */
	public void doCancel_student_view_assignment(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// reset the view assignment
		state.setAttribute(VIEW_ASSIGNMENT_ID, "");

		// back to the student list view of assignments
		state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);

	} // doCancel_student_view_assignment

	/**
	 * Action is to end the show submission process
	 */
	public void doCancel_show_submission(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// reset the view assignment
		state.setAttribute(VIEW_ASSIGNMENT_ID, "");
		
		String fromView = (String) state.getAttribute(FROM_VIEW);
		if (MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT.equals(fromView)) {
			state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT);
		}
		else {
			// back to the student list view of assignments
			state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
		}
		

	} // doCancel_show_submission

	/**
	 * Action is to cancel the delete assignment process
	 */
	public void doCancel_delete_assignment(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// reset the show assignment object
		state.setAttribute(DELETE_ASSIGNMENT_IDS, new ArrayList());

		// back to the instructor list view of assignments
		state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);

	} // doCancel_delete_assignment

	/**
	 * Action is to end the show submission process
	 */
	public void doCancel_edit_assignment(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// reset the assignment object
		resetAssignment(state);
		
		// back to the student list view of assignments
		state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
		
		// reset sorting
		setDefaultSort(state);

	} // doCancel_edit_assignment

	/**
	 * Action is to end the show submission process
	 */
	public void doCancel_new_assignment(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// reset the assignment object
		resetAssignment(state);

		// back to the student list view of assignments
		state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
		
		// reset sorting
		setDefaultSort(state);

	} // doCancel_new_assignment

	/**
	 * Action is to cancel the grade submission process
	 */
	public void doCancel_grade_submission(RunData data)
	{
		// put submission information into state
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		String sId = (String) state.getAttribute(GRADE_SUBMISSION_SUBMISSION_ID);
		String assignmentId = (String) state.getAttribute(GRADE_SUBMISSION_ASSIGNMENT_ID);
		
		// SAK-29314
		boolean viewSubsOnlySelected = stringToBool((String)data.getParameters().getString(PARAMS_VIEW_SUBS_ONLY_CHECKBOX));
		putSubmissionInfoIntoState(state, assignmentId, sId, viewSubsOnlySelected);

		String fromView = (String) state.getAttribute(FROM_VIEW);
		if (MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT.equals(fromView)) {
			state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT);
		}
		else {
			state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_GRADE_SUBMISSION);
		}
	} // doCancel_grade_submission

	/**
	 * clean the state variables related to grading page
	 * @param state
	 */
	private void resetGradeSubmission(SessionState state) {
		// reset the grade parameters
		state.removeAttribute(GRADE_SUBMISSION_FEEDBACK_COMMENT);
		state.removeAttribute(GRADE_SUBMISSION_FEEDBACK_TEXT);
		state.removeAttribute(GRADE_SUBMISSION_FEEDBACK_ATTACHMENT);
		// remove all GRADE_SUBMISSION_GRADE states including possible grade overrides
		// looking like GRADE_SUBMISSION_GRADE_[id of user]
		Iterator<String> _attribute_names = state.getAttributeNames().iterator();
		while (_attribute_names.hasNext()) {
		    String _attribute_name = _attribute_names.next();
		    if (_attribute_name.startsWith(GRADE_SUBMISSION_GRADE)) {
		        state.removeAttribute(_attribute_name);
		    }
		}
		state.removeAttribute(GRADE_SUBMISSION_SUBMISSION_ID);
		state.removeAttribute(GRADE_GREATER_THAN_MAX_ALERT);
		state.removeAttribute(GRADE_SUBMISSION_DONE);
		state.removeAttribute(GRADE_SUBMISSION_SUBMIT);
		state.removeAttribute(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER);
		
		// SAK-29314
		state.removeAttribute(STATE_VIEW_SUBS_ONLY);
		
		resetAllowResubmitParams(state);
	}

	/**
	 * Action is to cancel the preview grade process
	 */
	public void doCancel_preview_grade_submission(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// back to the instructor view of grading a submission
		state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_GRADE_SUBMISSION);

	} // doCancel_preview_grade_submission
	
	/**
	 * Action is to cancel the reorder process
	 */
	public void doCancel_reorder(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		
		// back to the list view of assignments
		state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);

	} // doCancel_reorder
	
	/**
	 * Action is to cancel the preview grade process
	 */
	public void doCancel_preview_to_list_submission(RunData data)
	{
		doCancel_grade_submission(data);

	} // doCancel_preview_to_list_submission
	
	/**
	 * Action is to return to the view of list assignments
	 */
	public void doList_assignments(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// back to the student list view of assignments
		state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
		state.setAttribute(SORTED_BY, SORTED_BY_DEFAULT);
		state.setAttribute(SORTED_ASC, Boolean.TRUE.toString());

	} // doList_assignments

	/**
	 * Action is to cancel the student view grade process
	 */
	public void doCancel_view_grade(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// reset the view grade submission id
		state.setAttribute(VIEW_GRADE_SUBMISSION_ID, "");

		// back to the student list view of assignments
		state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);

	} // doCancel_view_grade

	/**
	 * Action is to save the grade to submission
	 */
	public void doSave_grade_submission(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		readGradeForm(data, state, "save");
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			grade_submission_option(data, "save");
		}

	} // doSave_grade_submission

	public void doSave_grade_submission_review(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		saveReviewGradeForm(data, state, "save");
	}
	
	public void doSave_toggle_remove_review(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		if(state.getAttribute(PEER_ASSESSMENT_ASSESSOR_ID) != null){
			String peerAssessor = (String) state.getAttribute(PEER_ASSESSMENT_ASSESSOR_ID);
			ParameterParser params = data.getParameters();
			String submissionRef = params.getString("submissionId");
			String submissionId = null;
			if(submissionRef != null){
				int i = submissionRef.lastIndexOf(Entity.SEPARATOR);
				if (i == -1){
					submissionId = submissionRef;
				}else{
					submissionId = submissionRef.substring(i + 1);
				}
			}
			if(submissionId != null){
				//call the DB to make sure this user can edit this assessment, otherwise it wouldn't exist
				PeerAssessmentItem item = assignmentPeerAssessmentService.getPeerAssessmentItem(submissionId, peerAssessor);
				if(item != null){
					item.setRemoved(!item.isRemoved());
					assignmentPeerAssessmentService.savePeerAssessmentItem(item);
					if(item.getScore() != null){
						//item was part of the calculation, re-calculate
						boolean saved = assignmentPeerAssessmentService.updateScore(submissionId);
						if(saved){
							//we need to make sure the GB is updated correctly (or removed)
							String assignmentId = item.getAssignmentId();
							if(assignmentId != null){
								Assignment a = getAssignment(assignmentId, "saveReviewGradeForm", state);
								if(a != null){
									String aReference = a.getReference();
									String associateGradebookAssignment = StringUtils.trimToNull(a.getProperties().getProperty(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT));
									// update grade in gradebook
									integrateGradebook(state, aReference, associateGradebookAssignment, null, null, null, -1, null, submissionId, "update", -1);
								}
							}
						}
					}
					state.setAttribute(GRADE_SUBMISSION_DONE, Boolean.TRUE);
					state.setAttribute(PEER_ASSESSMENT_REMOVED_STATUS, item.isRemoved());
					//update session state:
					List<PeerAssessmentItem> peerAssessmentItems = (List<PeerAssessmentItem>) state.getAttribute(PEER_ASSESSMENT_ITEMS);
					if(peerAssessmentItems != null){
						for(int i = 0; i < peerAssessmentItems.size(); i++) {
							PeerAssessmentItem sItem = peerAssessmentItems.get(i);
							if(sItem.getSubmissionId().equals(item.getSubmissionId())
									&& sItem.getAssessorUserId().equals(item.getAssessorUserId())){
								//found it, just update it
								peerAssessmentItems.set(i, item);
								state.setAttribute(PEER_ASSESSMENT_ITEMS, peerAssessmentItems);
								break;
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Action is to release the grade to submission
	 */
	public void doRelease_grade_submission(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		readGradeForm(data, state, "release");
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			grade_submission_option(data, "release");
		}

	} // doRelease_grade_submission

	/**
	 * Action is to return submission with or without grade
	 */
	public void doReturn_grade_submission(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		readGradeForm(data, state, "return");
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			grade_submission_option(data, "return");
		}

	} // doReturn_grade_submission

	/**
	 * Action is to return submission with or without grade from preview
	 */
	public void doReturn_preview_grade_submission(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		grade_submission_option(data, "return");

	} // doReturn_grade_preview_submission

	/**
	 * Action is to save submission with or without grade from preview
	 */
	public void doSave_preview_grade_submission(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		grade_submission_option(data, "save");

	} // doSave_grade_preview_submission

	
	/**
	 * Common grading routine plus specific operation to differenciate cases when saving, releasing or returning grade.
	 */
	private void grade_submission_option(RunData data, String gradeOption)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		boolean withGrade = state.getAttribute(WITH_GRADES) != null ? ((Boolean) state.getAttribute(WITH_GRADES)).booleanValue(): false;

		String sId = (String) state.getAttribute(GRADE_SUBMISSION_SUBMISSION_ID);
		String assignmentId = (String) state.getAttribute(GRADE_SUBMISSION_ASSIGNMENT_ID);

		// for points grading, one have to enter number as the points
		String grade = (String) state.getAttribute(GRADE_SUBMISSION_GRADE);

		AssignmentSubmissionEdit sEdit = editSubmission(sId, "grade_submission_option", state);
		if (sEdit != null)
		{
			//This logic could be done in one line, but would be harder to read, so break it out to make it easier to follow
			boolean gradeChanged = false;
			if((sEdit.getGrade() == null || "".equals(sEdit.getGrade().trim()))
					&& (grade == null || "".equals(grade.trim()))){
				//both are null, keep grade changed = false
			}else if((sEdit.getGrade() == null || "".equals(sEdit.getGrade().trim())
					|| (grade == null || "".equals(grade.trim())))){
				//one is null the other isn't
				gradeChanged = true;
			}else if(!grade.trim().equals(sEdit.getGrade().trim())){
				gradeChanged = true;
			}
			Assignment a = sEdit.getAssignment();
			int typeOfGrade = a.getContent().getTypeOfGrade();

			if (!withGrade)
			{
				// no grade input needed for the without-grade version of assignment tool
				sEdit.setGraded(true);
				if(gradeChanged){
					sEdit.setGradedBy(UserDirectoryService.getCurrentUser() == null ? null : UserDirectoryService.getCurrentUser().getId());
				}
				if ("return".equals(gradeOption) || "release".equals(gradeOption))
				{
					sEdit.setGradeReleased(true);
				}
			}
			else if (grade == null)
			{
				sEdit.setGrade("");
				sEdit.setGraded(false);
				if(gradeChanged){
					sEdit.setGradedBy(null);
				}
				sEdit.setGradeReleased(false);
			}
			else
			{
				sEdit.setGrade(grade);

				if (grade.length() != 0)
				{
					sEdit.setGraded(true);
					if(gradeChanged){
						sEdit.setGradedBy(UserDirectoryService.getCurrentUser() == null ? null : UserDirectoryService.getCurrentUser().getId());
					}
				}
				else
				{
					sEdit.setGraded(false);
					if(gradeChanged){
						sEdit.setGradedBy(null);
					}
				}
			}

			// iterate through submitters and look for grade overrides...
			if (withGrade && a.isGroup()) {
			    User[] _users = sEdit.getSubmitters();
			    for (int i=0; _users != null && i < _users.length; i++) {
			        String _gr = (String)state.getAttribute(GRADE_SUBMISSION_GRADE + "_" + _users[i].getId());
			        sEdit.addGradeForUser(_users[i].getId(), _gr);
			    }
			}

			if ("release".equals(gradeOption))
			{
				sEdit.setGradeReleased(true);
				sEdit.setGraded(true);
				if(gradeChanged){
					sEdit.setGradedBy(UserDirectoryService.getCurrentUser() == null ? null : UserDirectoryService.getCurrentUser().getId());
				}
				// clear the returned flag
				sEdit.setReturned(false);
				sEdit.setTimeReturned(null);
			}
			else if ("return".equals(gradeOption))
			{
				sEdit.setGradeReleased(true);
				sEdit.setGraded(true);
				if(gradeChanged){
					sEdit.setGradedBy(UserDirectoryService.getCurrentUser() == null ? null : UserDirectoryService.getCurrentUser().getId());
				}
				sEdit.setReturned(true);
				sEdit.setTimeReturned(TimeService.newTime());
				sEdit.setHonorPledgeFlag(Boolean.FALSE.booleanValue());
			}
			else if ("save".equals(gradeOption))
			{
				sEdit.setGradeReleased(false);
				sEdit.setReturned(false);
				sEdit.setTimeReturned(null);
			}

			ResourcePropertiesEdit pEdit = sEdit.getPropertiesEdit();
			if (state.getAttribute(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER) != null)
			{
				// get resubmit number
				pEdit.addProperty(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER, (String) state.getAttribute(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER));
			
				if (state.getAttribute(ALLOW_RESUBMIT_CLOSEYEAR) != null)
				{
					// get resubmit time
					Time closeTime = getTimeFromState(state, ALLOW_RESUBMIT_CLOSEMONTH, ALLOW_RESUBMIT_CLOSEDAY, ALLOW_RESUBMIT_CLOSEYEAR, ALLOW_RESUBMIT_CLOSEHOUR, ALLOW_RESUBMIT_CLOSEMIN);
					pEdit.addProperty(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME, String.valueOf(closeTime.getTime()));
				}
				else
				{
					pEdit.removeProperty(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME);
				}
			}
			else
			{
				// clean resubmission property
				pEdit.removeProperty(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME);
				pEdit.removeProperty(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER);
			}

			// the instructor comment
			String feedbackCommentString = StringUtils
					.trimToNull((String) state.getAttribute(GRADE_SUBMISSION_FEEDBACK_COMMENT));
			if (feedbackCommentString != null)
			{
				sEdit.setFeedbackComment(feedbackCommentString);
			} 
			else 
			{
				sEdit.setFeedbackComment("");
			}

			// the instructor inline feedback
			String feedbackTextString = (String) state.getAttribute(GRADE_SUBMISSION_FEEDBACK_TEXT);
			if (feedbackTextString != null)
			{
				sEdit.setFeedbackText(feedbackTextString);
			}

			List v = (List) state.getAttribute(GRADE_SUBMISSION_FEEDBACK_ATTACHMENT);
			if (v != null)
			{
				
				// clear the old attachments first
				sEdit.clearFeedbackAttachments();

				for (int i = 0; i < v.size(); i++)
				{
					sEdit.addFeedbackAttachment((Reference) v.get(i));
				}
			}

			String sReference = sEdit.getReference();
			
			// save a timestamp for this grading process
			sEdit.getPropertiesEdit().addProperty(AssignmentConstants.PROP_LAST_GRADED_DATE, TimeService.newTime().toStringLocalFull());

			AssignmentService.commitEdit(sEdit);

			// update grades in gradebook
			String aReference = a.getReference();
			String associateGradebookAssignment = StringUtils.trimToNull(a.getProperties().getProperty(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT));

			if (!"remove".equals(gradeOption))
			{
				// update grade in gradebook
				integrateGradebook(state, aReference, associateGradebookAssignment, null, null, null, -1, null, sReference, "update", -1);
			}
			else
			{
				//remove grade from gradebook
				integrateGradebook(state, aReference, associateGradebookAssignment, null, null, null, -1, null, sReference, "remove", -1);
			}
		}

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			// SAK-29314 - put submission information into state
			boolean viewSubsOnlySelected = stringToBool((String)data.getParameters().getString(PARAMS_VIEW_SUBS_ONLY_CHECKBOX));
			putSubmissionInfoIntoState(state, assignmentId, sId, viewSubsOnlySelected);

			state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_GRADE_SUBMISSION);
			state.setAttribute(GRADE_SUBMISSION_DONE, Boolean.TRUE);
		}
		else
		{
			state.removeAttribute(GRADE_SUBMISSION_DONE);
		}

		// SAK-29314 - update the list being iterated over
		sizeResources(state);

	} // grade_submission_option

	/**
	 * Action is to save the submission as a draft
	 */
	public void doSave_submission(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		// save submission
		post_save_submission(data, false);
	} // doSave_submission

	/**
	 * set the resubmission related properties in AssignmentSubmission object
	 * @param a
	 * @param edit
	 */
	private void setResubmissionProperties(Assignment a,
			AssignmentSubmissionEdit edit) {
		// get the assignment setting for resubmitting
		ResourceProperties assignmentProperties = a.getProperties();
		String assignmentAllowResubmitNumber = assignmentProperties.getProperty(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER);
		if (assignmentAllowResubmitNumber != null)
		{
			edit.getPropertiesEdit().addProperty(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER, assignmentAllowResubmitNumber);
			
			String assignmentAllowResubmitCloseDate = assignmentProperties.getProperty(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME);
			// if assignment's setting of resubmit close time is null, use assignment close time as the close time for resubmit
			edit.getPropertiesEdit().addProperty(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME, assignmentAllowResubmitCloseDate != null?assignmentAllowResubmitCloseDate:String.valueOf(a.getCloseTime().getTime()));
		}
	}

	/**
	 * Action is to post the submission
	 */
	public void doPost_submission(RunData data)
	{
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		// post submission
		post_save_submission(data, true);
		
	}	// doPost_submission
	
	/**
	 * Inner method used for post or save submission
	 * @param data
	 * @param post
	 */
	private void post_save_submission(RunData data, boolean post)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
		String aReference = (String) state.getAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE);
		Assignment a  = getAssignment(aReference, "post_save_submission", state);
		
		if (a != null && AssignmentService.canSubmit(contextString, a))
		{
			ParameterParser params = data.getParameters();
			// retrieve the submission text (as formatted text)
			boolean checkForFormattingErrors = true; // check formatting error whether the student is posting or saving
			String text = processFormattedTextFromBrowser(state, params.getCleanString(VIEW_SUBMISSION_TEXT), checkForFormattingErrors);
	
			if (text == null)
			{
				text = (String) state.getAttribute(VIEW_SUBMISSION_TEXT);
			}
			else
			{
				state.setAttribute(VIEW_SUBMISSION_TEXT, text);
			}
	
			String honorPledgeYes = params.getString(VIEW_SUBMISSION_HONOR_PLEDGE_YES);
			/*if (honorPledgeYes == null)
			{
				honorPledgeYes = (String) state.getAttribute(VIEW_SUBMISSION_HONOR_PLEDGE_YES);
			}*/
	
			if (honorPledgeYes == null)
			{
				honorPledgeYes = "false";
			}
	
			User u = (User) state.getAttribute(STATE_USER);
			User submitter = null;
			String studentId = params.get("submit_on_behalf_of");
			if (studentId != null && !studentId.equals("-1")) {
				// SAK-23817: return to the Assignments List by Student
				state.setAttribute(FROM_VIEW, MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT);
				try {
					submitter = u;
					u = UserDirectoryService.getUser(studentId);
				} catch (UserNotDefinedException ex1) {
					M_log.warn("Unable to find user with ID [" + studentId + "]");
					submitter = null;
				}
			}

			String group_id = null;
			String original_group_id = null;

			if (a.isGroup()) {
			    original_group_id = 
			            (params.getString("originalGroup") == null || params.getString("originalGroup").trim().length() == 0) ? null: params.getString("originalGroup"); ;
			            if (original_group_id != null) {
			                state.setAttribute(VIEW_SUBMISSION_ORIGINAL_GROUP, original_group_id);
			            } else {
			                if (state.getAttribute(VIEW_SUBMISSION_ORIGINAL_GROUP) != null)
			                    original_group_id = (String)state.getAttribute(VIEW_SUBMISSION_ORIGINAL_GROUP);
			                else
			                    state.setAttribute(VIEW_SUBMISSION_ORIGINAL_GROUP, null);
			            }

			            String[] groupChoice = params.getStrings("selectedGroups");

			            if (groupChoice != null && groupChoice.length != 0)
			            {
			                if (groupChoice.length > 1) {
			                    state.setAttribute(VIEW_SUBMISSION_GROUP, null);
			                    addAlert(state, rb.getString("java.alert.youchoosegroup"));                        
			                } else {
			                    group_id = groupChoice[0];
			                    state.setAttribute(VIEW_SUBMISSION_GROUP, groupChoice[0]);
			                }
			            }
			            else
			            {
			                // get the submitted group id
			                if (state.getAttribute(VIEW_SUBMISSION_GROUP) != null) {
			                    group_id = (String)state.getAttribute(VIEW_SUBMISSION_GROUP); 
			                } else {
			                    state.setAttribute(VIEW_SUBMISSION_GROUP, null);
			                    addAlert(state, rb.getString("java.alert.youchoosegroup"));
			                }
			            }
			}

			String assignmentId = "";
			if (state.getAttribute(STATE_MESSAGE) == null)
			{
				assignmentId = a.getId();
	
				if (a.getContent().getHonorPledge() != 1)
				{
					if (!Boolean.valueOf(honorPledgeYes).booleanValue())
					{
						addAlert(state, rb.getString("youarenot18"));
					}
					state.setAttribute(VIEW_SUBMISSION_HONOR_PLEDGE_YES, honorPledgeYes);
				}

				// SAK-26322
				List nonInlineAttachments = getNonInlineAttachments(state, a);
				int typeOfSubmission = a.getContent().getTypeOfSubmission();
				if (typeOfSubmission == Assignment.SINGLE_ATTACHMENT_SUBMISSION && nonInlineAttachments.size() >1)
				{
					//Single uploaded file and there are multiple attachments
					adjustAttachmentsToSingleUpload(data, state, a, nonInlineAttachments);
				}

				// clear text if submission type does not allow it
				if (typeOfSubmission == Assignment.SINGLE_ATTACHMENT_SUBMISSION || typeOfSubmission == Assignment.ATTACHMENT_ONLY_ASSIGNMENT_SUBMISSION)
				{
					text = null;
				}
				
				// get attachment input and generate alert message according to assignment submission type
				checkSubmissionTextAttachmentInput(data, state, a, text);
			}
			if ((state.getAttribute(STATE_MESSAGE) == null) && (a != null)) {
			    AssignmentSubmission submission =  null;
			    if (a.isGroup()) {
			        submission = getSubmission(a.getReference(), 
			                (original_group_id == null ? group_id: original_group_id), 
			                "post_save_submission", 
			                state);
			    } else {
			        submission = getSubmission(a.getReference(), u, "post_save_submission", state);
			    }

				if (submission != null)
				{
					// the submission already exists, change the text and honor pledge value, post it
					AssignmentSubmissionEdit sEdit = editSubmission(submission.getReference(), "post_save_submission", state);
					if (sEdit != null)
					{
						ResourcePropertiesEdit sPropertiesEdit = sEdit.getPropertiesEdit();
						
						/**
						 * SAK-22150 We will need to know later if there was a previous submission time. DH
						 */
						boolean isPreviousSubmissionTime = true;
						if (sEdit.getTimeSubmitted() == null || "".equals(sEdit.getTimeSubmitted()) || !AssignmentService.hasBeenSubmitted(sEdit))
						{
							isPreviousSubmissionTime = false;
						}

						if (a.isGroup()) {
						    if (original_group_id != null && !original_group_id.equals(group_id)) {
						        // changing group id so we need to check if a submission has already been made for that group
						        AssignmentSubmission submissioncheck = getSubmission(a.getReference(), group_id, "post_save_submission",state);
						        if (submissioncheck != null) {
						            addAlert(state, rb.getString("group.already.submitted"));
						            M_log.warn(this + ":post_save_submission " + group_id + " has already submitted " + submissioncheck.getId() + "!");
						        }
						    } 
						    sEdit.setSubmitterId(group_id);
						}

						sEdit.setSubmittedText(text);
						sEdit.setHonorPledgeFlag(Boolean.valueOf(honorPledgeYes).booleanValue());
						sEdit.setTimeSubmitted(TimeService.newTime());
						sEdit.setSubmitted(post);
						sEdit.setIsUserSubmission(true);
						
						// decrease the allow_resubmit_number, if this submission has been submitted.
						if (sEdit.getSubmitted() && isPreviousSubmissionTime && sPropertiesEdit.getProperty(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER) != null)
						{
							int number = Integer.parseInt(sPropertiesEdit.getProperty(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER));
							// minus 1 from the submit number, if the number is not -1 (not unlimited)
							if (number>=1)
							{
								sPropertiesEdit.addProperty(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER, String.valueOf(number-1));
							}
						}

						// for resubmissions
						// when resubmit, keep the Returned flag on till the instructor grade again.
						Time now = TimeService.newTime();

						// need this to handle feedback and comments, which we have to do even if ungraded
							// get the previous graded date
							String prevGradedDate = sEdit.getProperties().getProperty(AssignmentConstants.PROP_LAST_GRADED_DATE);
							if (prevGradedDate == null)
							{
								// since this is a newly added property, if no value is set, get the default as the submission last modified date
								prevGradedDate = sEdit.getTimeLastModified().toStringLocalFull();
								sEdit.getProperties().addProperty(AssignmentConstants.PROP_LAST_GRADED_DATE, prevGradedDate);
							}
							
						if (sEdit.getGraded() && sEdit.getReturned() && sEdit.getGradeReleased())
						{

							// add the current grade into previous grade histroy
							String previousGrades = (String) sEdit.getProperties().getProperty(
									ResourceProperties.PROP_SUBMISSION_SCALED_PREVIOUS_GRADES);
							if (previousGrades == null)
							{
								previousGrades = (String) sEdit.getProperties().getProperty(
										ResourceProperties.PROP_SUBMISSION_PREVIOUS_GRADES);
								if (previousGrades != null)
								{
									int typeOfGrade = a.getContent().getTypeOfGrade();
									if (typeOfGrade == 3)
									{
										// point grade assignment type
										// some old unscaled grades, need to scale the number and remove the old property
										String[] grades = StringUtils.split(previousGrades, " ");
										String newGrades = "";
										
										String decSeparator = FormattedText.getDecimalSeparator();
										
										for (int jj = 0; jj < grades.length; jj++)
										{
											String grade = grades[jj];
											if (grade.indexOf(decSeparator) == -1)
											{
												// show the grade with decimal point
												grade = grade.concat(decSeparator).concat("0");
											}
											newGrades = newGrades.concat(grade + " ");
										}
										previousGrades = newGrades;
									}
									sPropertiesEdit.removeProperty(ResourceProperties.PROP_SUBMISSION_PREVIOUS_GRADES);
								}
								else
								{
									previousGrades = "";
								}
							}
							
							if (StringUtils.trimToNull(sEdit.getGradeDisplay()) != null)
							{
								previousGrades =  "<h4>" + prevGradedDate + "</h4>" + "<div style=\"margin:0;padding:0\">" + sEdit.getGradeDisplay() + "</div>" +previousGrades;
								sPropertiesEdit.addProperty(ResourceProperties.PROP_SUBMISSION_SCALED_PREVIOUS_GRADES, previousGrades);
							}

							// clear the current grade and make the submission ungraded
							sEdit.setGraded(false);
							sEdit.setGradedBy(null);
							sEdit.setGrade("");
							sEdit.setGradeReleased(false);
							
						}

						// following involves content, not grading, so always do on resubmit, not just if graded

							// clean the ContentReview attributes
							sEdit.setReviewIconUrl(null);
							sEdit.setReviewScore(-2); // the default is -2 (e.g., for a new submission)
							sEdit.setReviewStatus(null);

							if (StringUtils.trimToNull(sEdit.getFeedbackFormattedText()) != null)
							{
								// keep the history of assignment feed back text
								String feedbackTextHistory = sPropertiesEdit
										.getProperty(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT) != null ? sPropertiesEdit
										.getProperty(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT)
										: "";
								feedbackTextHistory =  "<h4>" + prevGradedDate + "</h4>" + "<div style=\"margin:0;padding:0\">" + sEdit.getFeedbackText() + "</div>" + feedbackTextHistory;
								sPropertiesEdit.addProperty(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT,
										feedbackTextHistory);
							}

							if (StringUtils.trimToNull(sEdit.getFeedbackComment()) != null)
							{
								// keep the history of assignment feed back comment
								String feedbackCommentHistory = sPropertiesEdit
										.getProperty(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT) != null ? sPropertiesEdit
										.getProperty(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT)
										: "";
								feedbackCommentHistory = "<h4>" + prevGradedDate + "</h4>" + "<div style=\"margin:0;padding:0\">" + sEdit.getFeedbackComment() + "</div>" + feedbackCommentHistory;
								sPropertiesEdit.addProperty(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT,
										feedbackCommentHistory);
							}
							
							// keep the history of assignment feed back comment
							String feedbackAttachmentHistory = sPropertiesEdit
									.getProperty(PROP_SUBMISSION_PREVIOUS_FEEDBACK_ATTACHMENTS) != null ? sPropertiesEdit
									.getProperty(PROP_SUBMISSION_PREVIOUS_FEEDBACK_ATTACHMENTS)
									: "";
							List feedbackAttachments = sEdit.getFeedbackAttachments();
							StringBuffer attBuffer = new StringBuffer();
							for (int k = 0; k<feedbackAttachments.size();k++)
							{
								// use comma as separator for attachments
								attBuffer.append(((Reference) feedbackAttachments.get(k)).getReference() + ",");
							}
							feedbackAttachmentHistory = attBuffer.toString() + feedbackAttachmentHistory;
								
							sPropertiesEdit.addProperty(PROP_SUBMISSION_PREVIOUS_FEEDBACK_ATTACHMENTS,
									feedbackAttachmentHistory);

							// reset the previous grading context
							sEdit.setFeedbackText("");
							sEdit.setFeedbackComment("");
							sEdit.clearFeedbackAttachments();
						
						sEdit.setAssignment(a);

						// SAK-26322
						if (a.getContent().getTypeOfSubmission() == 5)
						{
							List nonInlineAttachments = getNonInlineAttachments(state, a);
							if (nonInlineAttachments != null)
							{
								//clear out inline attachments for content-review
								//filter the attachments in the state to exclude inline attachments (nonInlineAttachments, is a subset of what's currently in the state)
								state.setAttribute(ATTACHMENTS, nonInlineAttachments);
							}
						}

						// add attachments
						List attachments = (List) state.getAttribute(ATTACHMENTS);
						if (attachments != null)
						{
							if (a.getContent().getTypeOfSubmission() == Assignment.TEXT_ONLY_ASSIGNMENT_SUBMISSION)
							{
								//inline only doesn't accept attachments
								sEdit.clearSubmittedAttachments();
							}
							else
							{
								//Post the attachments before clearing so that we don't sumbit duplicate attachments
								//Check if we need to post the attachments
								if (a.getContent().getAllowReviewService()) {
									if (!attachments.isEmpty()) { 
										sEdit.postAttachment(attachments);
									}
								}
																 
								// clear the old attachments first
								sEdit.clearSubmittedAttachments();

								// add each new attachment
								if (submitter != null) {
									sPropertiesEdit.addProperty(AssignmentSubmission.SUBMITTER_USER_ID, submitter.getId());
									state.setAttribute(STATE_SUBMITTER, u.getId());
								} else {
									sPropertiesEdit.removeProperty(AssignmentSubmission.SUBMITTER_USER_ID);
								}
								
								Iterator it = attachments.iterator();
								while (it.hasNext())
								{
									sEdit.addSubmittedAttachment((Reference) it.next());
								}
							}
						}

						// SAK-26322 - add inline as an attachment for the content review service
						if (a.getContent().getAllowReviewService() && !isHtmlEmpty(text))
						{
							prepareInlineForContentReview(text, sEdit, state, u);
						}

						if (submitter != null) {
						    sPropertiesEdit.addProperty(AssignmentSubmission.SUBMITTER_USER_ID, submitter.getId());
						    state.setAttribute(STATE_SUBMITTER, u.getId());
						} else {
						    sPropertiesEdit.removeProperty(AssignmentSubmission.SUBMITTER_USER_ID);
						}

						// SAK-17606
						String logEntry = new java.util.Date().toString() + " ";
						boolean anonymousGrading = Boolean.parseBoolean(a.getProperties().getProperty(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING));
						if(!anonymousGrading){
							String subOrDraft = post ? "submitted" : "saved draft";
							if( submitter != null && !submitter.getEid().equals( u.getEid() ) )
							{
								logEntry += submitter.getDisplayName() + " (" + submitter.getEid() + ") " + subOrDraft + " " +
											rb.getString( "listsub.submitted.on.behalf" ) + " " + u.getDisplayName() + " (" +
											u.getEid() + ")";
							}
							else
							{
								logEntry += u.getDisplayName() + " (" + u.getEid() + ") " + subOrDraft;
							}
						}
						sEdit.addSubmissionLogEntry( logEntry );
						AssignmentService.commitEdit(sEdit);
					}
				}
				else
				{
					// new submission
					try
					{
					    // if assignment is a group submission... send group id and not user id
					    M_log.debug(this + " NEW SUBMISSION IS GROUP: " + a.isGroup() + " GROUP:" + group_id);
					    AssignmentSubmissionEdit edit = a.isGroup() ? 
					            AssignmentService.addSubmission(contextString, assignmentId, group_id): 
					                AssignmentService.addSubmission(contextString, assignmentId, SessionManager.getCurrentSessionUserId());
						if (edit != null)
						{
							edit.setSubmittedText(text);
							edit.setHonorPledgeFlag(Boolean.valueOf(honorPledgeYes).booleanValue());
							edit.setTimeSubmitted(TimeService.newTime());
							edit.setSubmitted(post);
							edit.setAssignment(a);
	                        ResourcePropertiesEdit sPropertiesEdit = edit.getPropertiesEdit();
	
							// add attachments
							List attachments = (List) state.getAttribute(ATTACHMENTS);

							// SAK-26322 - add inline as an attachment for the content review service
							if (a.getContent().getAllowReviewService() && !isHtmlEmpty(text))
							{
								prepareInlineForContentReview(text, edit, state, u);
							}
							
							if (attachments != null)
							{
	 							// add each attachment
								if ((!attachments.isEmpty()) && a.getContent().getAllowReviewService()) 
									edit.postAttachment(attachments);								
								
								// add each attachment
								Iterator it = attachments.iterator();
								while (it.hasNext())
								{
									edit.addSubmittedAttachment((Reference) it.next());
								}
							}
							
							// set the resubmission properties
							setResubmissionProperties(a, edit);
							if (submitter != null) {
							    sPropertiesEdit.addProperty(AssignmentSubmission.SUBMITTER_USER_ID, submitter.getId());
							    state.setAttribute(STATE_SUBMITTER, u.getId());
							} else {
							    sPropertiesEdit.removeProperty(AssignmentSubmission.SUBMITTER_USER_ID);
							}

							// SAK-17606
							String logEntry = new java.util.Date().toString() + " ";
							boolean anonymousGrading = Boolean.parseBoolean(a.getProperties().getProperty(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING));
							if(!anonymousGrading){
								String subOrDraft = post ? "submitted" : "saved draft";
								if( submitter != null && !submitter.getEid().equals( u.getEid() ) )
								{
									logEntry += submitter.getDisplayName() + " (" + submitter.getEid() + ") " + subOrDraft + " " +
												rb.getString( "listsub.submitted.on.behalf" ) + " " + u.getDisplayName() + " (" +
												u.getEid() + ")";
								}
								else
								{
									logEntry += u.getDisplayName() + " (" + u.getEid() + ") " + subOrDraft;
								}
							}
							edit.addSubmissionLogEntry( logEntry );
							AssignmentService.commitEdit(edit);
						}
					}
					catch (PermissionException e)
					{
						addAlert(state, rb.getString("youarenot13"));
						M_log.warn(this + ":post_save_submission " + e.getMessage());
					}
				} // if-else
	
			} // if
	
			if (state.getAttribute(STATE_MESSAGE) == null)
			{
				state.setAttribute(STATE_MODE, MODE_STUDENT_VIEW_SUBMISSION_CONFIRMATION);
			}
            LearningResourceStoreService lrss = (LearningResourceStoreService) ComponentManager
                    .get("org.sakaiproject.event.api.LearningResourceStoreService");
            if (null != lrss) {
                Event event = m_eventTrackingService.newEvent(AssignmentConstants.EVENT_SUBMIT_ASSIGNMENT_SUBMISSION, assignmentId, false);
                lrss.registerStatement(
                        getStatementForSubmitAssignment(lrss.getEventActor(event), event, ServerConfigurationService.getAccessUrl(),
                                a.getTitle()), "sakai.assignment");
            }
		}	// if

	} // post_save_submission

	/**
	 * Takes the inline submission, prepares it as an attachment to the submission and queues the attachment with the content review service
	 */
	private void prepareInlineForContentReview(String text, AssignmentSubmissionEdit edit, SessionState state, User student)
	{
		//We will be replacing the inline submission's attachment
		//firstly, disconnect any existing attachments with AssignmentSubmission.PROP_INLINE_SUBMISSION set
		List attachments = edit.getSubmittedAttachments();
		List toRemove = new ArrayList();
		Iterator itAttachments = attachments.iterator();
		while (itAttachments.hasNext())
		{
			Reference attachment = (Reference) itAttachments.next();
			ResourceProperties attachProps = attachment.getProperties();
			if ("true".equals(attachProps.getProperty(AssignmentSubmission.PROP_INLINE_SUBMISSION)))
			{
				toRemove.add(attachment);
			}
		}
		Iterator itToRemove = toRemove.iterator();
		while (itToRemove.hasNext())
		{
			Reference attachment = (Reference) itToRemove.next();
			edit.removeSubmittedAttachment(attachment);
		}
		
		//now prepare the new resource
		//provide lots of info for forensics - filename=InlineSub_assignmentId_userDisplayId_(for_studentDisplayId)_date.html
		User currentUser = UserDirectoryService.getCurrentUser();
		String currentDisplayName = currentUser.getDisplayId();
		String siteId = (String) state.getAttribute(STATE_CONTEXT_STRING);
		SimpleDateFormat dform = ((SimpleDateFormat) DateFormat.getDateInstance());
		//avoid semicolons in filenames, right?
		dform.applyPattern("yyyy-MM-dd_HH-mm-ss");
		StringBuilder sb_resourceId = new StringBuilder("InlineSub_");
		String u = "_";
		sb_resourceId.append(edit.getAssignmentId()).append(u).append(currentDisplayName).append(u);
		boolean isOnBehalfOfStudent = student != null && !student.equals(currentUser);
		if (isOnBehalfOfStudent)
		{
			// We're submitting on behalf of somebody
			sb_resourceId.append("for_").append(student.getDisplayId()).append(u);
		}
		sb_resourceId.append(dform.format(new Date()));

		String fileExtension = ".html";

		/* 
		 * TODO: add and use a method in ContentHostingService to get the length of the ID of an attachment collection
		 * Attachment collections currently look like this:
		 * /attachment/dc126c4a-a48f-42a6-bda0-cf7b9c4c5c16/Assignments/eac7212a-9597-4b7d-b958-89e1c47cdfa7/
		 * See BaseContentService.addAttachmentResource for more information
		 */
		String toolName = "Assignments";
		// TODO: add and use a method in IdManager to get the maxUuidLength
		int maxUuidLength = 36;
		int esl = Entity.SEPARATOR.length();
		int attachmentCollectionLength = ContentHostingService.ATTACHMENTS_COLLECTION.length() + siteId.length() + esl + toolName.length() + esl + maxUuidLength + esl;
		int maxChars = ContentHostingService.MAXIMUM_RESOURCE_ID_LENGTH - attachmentCollectionLength - fileExtension.length() - 1;
		String resourceId = StringUtils.substring(sb_resourceId.toString(), 0, maxChars) + fileExtension;

		ResourcePropertiesEdit inlineProps = m_contentHostingService.newResourceProperties();
		inlineProps.addProperty(ResourceProperties.PROP_DISPLAY_NAME, rb.getString("submission.inline"));
		inlineProps.addProperty(ResourceProperties.PROP_DESCRIPTION, resourceId);
		inlineProps.addProperty(AssignmentSubmission.PROP_INLINE_SUBMISSION, "true");

		//create a byte array input stream
		//text is almost in html format, but it's missing the start and ending tags
		//(Is this always the case? Does the content review service care?)
		String toHtml = "<html><head></head><body>" + text + "</body></html>";
		InputStream contentStream = new ByteArrayInputStream(toHtml.getBytes());

		String contentType = "text/html";

		//duplicating code from doAttachUpload. TODO: Consider refactoring into a method

		SecurityAdvisor sa = new SecurityAdvisor()
		{
			public SecurityAdvice isAllowed(String userId, String function, String reference)
			{
				if(function.equals(m_contentHostingService.AUTH_RESOURCE_ADD)){
					return SecurityAdvice.ALLOWED;
				}else if(function.equals(m_contentHostingService.AUTH_RESOURCE_WRITE_ANY)){
					return SecurityAdvice.ALLOWED;
				}else{
					return SecurityAdvice.PASS;
				}
			}
		};
		try
		{
			m_securityService.pushAdvisor(sa);
			ContentResource attachment = m_contentHostingService.addAttachmentResource(resourceId, siteId, toolName, contentType, contentStream, inlineProps);
			// TODO: need to put this file in some kind of list to improve performance with web service impls of content-review service
			String contentUserId = isOnBehalfOfStudent ? student.getId() : currentUser.getId();
			contentReviewService.queueContent(contentUserId, siteId, edit.getAssignment().getReference(), Arrays.asList(attachment));

			try
			{
				Reference ref = EntityManager.newReference(m_contentHostingService.getReference(attachment.getId()));
				edit.addSubmittedAttachment(ref);
			}
			catch (Exception e)
			{
				M_log.warn(this + "prepareInlineForContentReview() cannot find reference for " + attachment.getId() + e.getMessage());
			}
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getString("notpermis4"));
		}
		catch (RuntimeException e)
		{
			if (m_contentHostingService.ID_LENGTH_EXCEPTION.equals(e.getMessage()))
			{
				addAlert(state, rb.getFormattedMessage("alert.toolong", new String[]{resourceId}));
			}
		}
		catch (ServerOverloadException e)
		{
			M_log.debug(this + ".prepareInlineForContentReview() ***** DISK IO Exception ***** " + e.getMessage());
			addAlert(state, rb.getString("failed.diskio"));
		}
		catch (Exception ignore)
		{
			M_log.debug(this + ".prepareInlineForContentReview() ***** Unknown Exception ***** " + ignore.getMessage());
			addAlert(state, rb.getString("failed"));
		}
		finally
		{
			m_securityService.popAdvisor(sa);
		}
	}

	/**
	 * Used when students are selecting from a list of previous attachments for their single uploaded file
	 */
	private void adjustAttachmentsToSingleUpload(RunData data, SessionState state, Assignment a, List nonInlineAttachments)
	{
		if (a == null || a.getContent() == null || a.getContent().getTypeOfSubmission() != 5)
		{
			throw new IllegalArgumentException("adjustAttachmentsToSingleUpload called, but the assignment type is not Single Uploaded File");
		}
		if (nonInlineAttachments == null)
		{
			throw new IllegalArgumentException("adjustAttachmentsToSingleUpload called, but nonInlineAttachments is null");
		}
		
		String selection = data.getParameters().get("attachmentSelection");
		if ("newAttachment".equals(selection))
		{
			Reference attachment = (Reference) state.getAttribute("newSingleUploadedFile");
			if (attachment == null)
			{
				// Try the newSingleAttachmentList
				List l = (List) state.getAttribute("newSingleAttachmentList");
				if (l != null && !l.isEmpty())
				{
					attachment = (Reference) l.get(0);
				}
			}
			if (attachment != null)
			{
				List attachments = EntityManager.newReferenceList();
				attachments.add(attachment);
				state.setAttribute(ATTACHMENTS, attachments);
				state.removeAttribute("newSingleUploadedFile");
				state.removeAttribute("newSingleAttachmentList");
				state.removeAttribute(VIEW_SUBMISSION_TEXT);
			}
			// ^ if attachment is null, we don't care - checkSubmissionTextAttachmentInput() handles that for us
		}
		else
		{
			//they selected a previous attachment. selection represents an index in the nonInlineAttachments list
			boolean error = false;
			int index = -1;
			try
			{
				//get the selected attachment
				index = Integer.parseInt(selection);
				if (nonInlineAttachments.size() <= index)
				{
					error = true;
				}
			}
			catch (NumberFormatException nfe)
			{
				error = true;
			}

			if (error)
			{
				M_log.warn("adjustAttachmentsToSingleUpload() - couldn't parse the selected index as an integer, or the selected index wasn't in the range of attachment indices");
				//checkSubmissionTextAttachmentInput() handles the alert message for us
			}
			else
			{
				Reference attachment = (Reference) nonInlineAttachments.get(index);
				//remove all the attachments from the state and add the selected one back for resubmission
				List attachments = (List) state.getAttribute(ATTACHMENTS);
				attachments.clear();
				attachments.add(attachment);
			}
		}
	}

	private void checkSubmissionTextAttachmentInput(RunData data,
			SessionState state, Assignment a, String text) {
		// SAK-26329 - determine if the submission has text
		boolean textIsEmpty = isHtmlEmpty(text);
		if (a != null)
		{
			// check the submission inputs based on the submission type
			int submissionType = a.getContent().getTypeOfSubmission();
			if (submissionType == Assignment.TEXT_ONLY_ASSIGNMENT_SUBMISSION)
			{
				// for the inline only submission
				if (textIsEmpty)
				{
					addAlert(state, rb.getString("youmust7"));
				}
			}
			else if (submissionType == Assignment.ATTACHMENT_ONLY_ASSIGNMENT_SUBMISSION)
			{
				// for the attachment only submission
				List v = getNonInlineAttachments(state, a);
				if ((v == null) || (v.size() == 0))
				{
					addAlert(state, rb.getString("youmust1"));
				}
			}
			else if (submissionType == Assignment.SINGLE_ATTACHMENT_SUBMISSION)
			{
				// for the single uploaded file only submission
				List v = getNonInlineAttachments(state, a);
				if ((v == null) || (v.size() != 1))
				{
					addAlert(state, rb.getString("youmust8"));
				}
			}
			else
			{	
				// for the inline and attachment submission / other submission types
				// There must be at least one thing submitted: inline text or at least one attachment
				List v = getNonInlineAttachments(state, a);
				if (textIsEmpty && (v == null || v.size() == 0))
				{
					addAlert(state, rb.getString("youmust2"));
				}
			}
		}
	}

	/**
	 * When using content review, inline text gets turned into an attachment. This method returns all the attachments that do not represent inline text
	 */
	private List getNonInlineAttachments(SessionState state, Assignment a)
	{
		List attachments = (List) state.getAttribute(ATTACHMENTS);
		List nonInlineAttachments = new ArrayList();
		nonInlineAttachments.addAll(attachments);
		if (a.getContent().getAllowReviewService())
		{
			Iterator itAttachments = attachments.iterator();
			while (itAttachments.hasNext())
			{
				Object next = itAttachments.next();
				if (next instanceof Reference)
				{
					Reference attachment = (Reference) next;
					if ("true".equals(attachment.getProperties().getProperty(AssignmentSubmission.PROP_INLINE_SUBMISSION)))
					{
						nonInlineAttachments.remove(attachment);
					}
				}
			}
		}
		return nonInlineAttachments;
	}

	/**
	 * SAK-26329 - Parses html and determines whether it contains printable characters. 
	 */
	private boolean isHtmlEmpty(String html)
	{
		return html == null ? true : FormattedText.stripHtmlFromText(html, false, true).isEmpty();
	}

	/**
	 * Action is to confirm the submission and return to list view
	 */
	public void doConfirm_assignment_submission(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		// SAK-23817 if the instructor submitted on behalf of the student, go back to Assignment List by Student
		String fromView = (String) state.getAttribute(FROM_VIEW);
		if (MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT.equals(fromView)) {
			state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT);
		}
		else {
			state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
		}
		state.setAttribute(ATTACHMENTS, EntityManager.newReferenceList());
	}
	/**
	 * Action is to show the new assignment screen
	 */
	public void doNew_assignment(RunData data, Context context)
	{
		
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		if (!alertGlobalNavigation(state, data))
		{
			if (AssignmentService.allowAddAssignment((String) state.getAttribute(STATE_CONTEXT_STRING)))
			{
				initializeAssignment(state);
				
				state.setAttribute(ATTACHMENTS, EntityManager.newReferenceList());
				state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_NEW_EDIT_ASSIGNMENT);
			}
			else
			{
				addAlert(state, rb.getString("youarenot_addAssignment"));
				state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
			}
			
			// reset the global navigaion alert flag
			if (state.getAttribute(ALERT_GLOBAL_NAVIGATION) != null)
			{
				state.removeAttribute(ALERT_GLOBAL_NAVIGATION);
			}
		}

	} // doNew_Assignment
	
	/**
	 * Action is to show the reorder assignment screen
	 */
	public void doReorder(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		
		// this insures the default order is loaded into the reordering tool
		state.setAttribute(SORTED_BY, SORTED_BY_DEFAULT);
		state.setAttribute(SORTED_ASC, Boolean.TRUE.toString());

		if (!alertGlobalNavigation(state, data))
		{
			if (AssignmentService.allowAllGroups((String) state.getAttribute(STATE_CONTEXT_STRING)))
			{	
				state.setAttribute(ATTACHMENTS, EntityManager.newReferenceList());
				state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_REORDER_ASSIGNMENT);
			}
			else
			{
				addAlert(state, rb.getString("youarenot19"));
				state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
			}
			
			// reset the global navigaion alert flag
			if (state.getAttribute(ALERT_GLOBAL_NAVIGATION) != null)
			{
				state.removeAttribute(ALERT_GLOBAL_NAVIGATION);
			}
		}

	} // doReorder

	/**
	 * Action is to save the input infos for assignment fields
	 *
	 * @param validify
	 *        Need to validify the inputs or not
	 */
	protected void setNewAssignmentParameters(RunData data, boolean validify)
	{
		// read the form inputs
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();
		
		String assignmentRef = params.getString("assignmentId");

		// put the input value into the state attributes
		String title = params.getString(NEW_ASSIGNMENT_TITLE);
		state.setAttribute(NEW_ASSIGNMENT_TITLE, title);
		
		String order = params.getString(NEW_ASSIGNMENT_ORDER);
		state.setAttribute(NEW_ASSIGNMENT_ORDER, order);
		
		String additionalOptions = params.getString(NEW_ASSIGNMENT_ADDITIONAL_OPTIONS);
		
		boolean groupAssignment = false;
		if ("group".equals(additionalOptions)) {
			state.setAttribute(NEW_ASSIGNMENT_GROUP_SUBMIT, "1");
			groupAssignment = true;
		}
		else {
			state.setAttribute(NEW_ASSIGNMENT_GROUP_SUBMIT, "0");
		}

		if (title == null || title.length() == 0)
		{
			// empty assignment title
			addAlert(state, rb.getString("plespethe1"));
		}
		else if (sameAssignmentTitleInContext(assignmentRef, title, (String) state.getAttribute(STATE_CONTEXT_STRING)))
		{
			// assignment title already exist
			addAlert(state, rb.getFormattedMessage("same_assignment_title", new Object[]{title}));
		}
		
		// open time
		Time openTime = putTimeInputInState(params, state, NEW_ASSIGNMENT_OPENMONTH, NEW_ASSIGNMENT_OPENDAY, NEW_ASSIGNMENT_OPENYEAR, NEW_ASSIGNMENT_OPENHOUR, NEW_ASSIGNMENT_OPENMIN, "newassig.opedat");

		// visible time
		if (Boolean.valueOf(ServerConfigurationService.getBoolean("assignment.visible.date.enabled", false))) {
		    if (params.get("allowVisibleDateToggle") == null) {
		        state.setAttribute(NEW_ASSIGNMENT_VISIBLETOGGLE, false);
		    } else {
		        Time visibleTime = putTimeInputInState(params, state, NEW_ASSIGNMENT_VISIBLEMONTH, NEW_ASSIGNMENT_VISIBLEDAY, NEW_ASSIGNMENT_VISIBLEYEAR, NEW_ASSIGNMENT_VISIBLEHOUR, NEW_ASSIGNMENT_VISIBLEMIN, "newassig.visdat");
		        state.setAttribute(NEW_ASSIGNMENT_VISIBLETOGGLE, true);
		    }

		}

		// due time
		Time dueTime = putTimeInputInState(params, state, NEW_ASSIGNMENT_DUEMONTH, NEW_ASSIGNMENT_DUEDAY, NEW_ASSIGNMENT_DUEYEAR, NEW_ASSIGNMENT_DUEHOUR, NEW_ASSIGNMENT_DUEMIN, "gen.duedat");
		// show alert message when due date is in past. Remove it after user confirms the choice.
		if (dueTime != null && dueTime.before(TimeService.newTime()) && state.getAttribute(NEW_ASSIGNMENT_PAST_DUE_DATE) == null)
		{
			state.setAttribute(NEW_ASSIGNMENT_PAST_DUE_DATE, Boolean.TRUE);
		}
		else
		{
			// clean the attribute after user confirm
			state.removeAttribute(NEW_ASSIGNMENT_PAST_DUE_DATE);
		}
		if (state.getAttribute(NEW_ASSIGNMENT_PAST_DUE_DATE) != null && validify)
		{
			addAlert(state, rb.getString("assig4"));
		}
		
		if (openTime != null && dueTime != null && !dueTime.after(openTime))
		{
			addAlert(state, rb.getString("assig3"));
		}

		state.setAttribute(NEW_ASSIGNMENT_ENABLECLOSEDATE, Boolean.valueOf(true));

		// close time
		Time closeTime = putTimeInputInState(params, state, NEW_ASSIGNMENT_CLOSEMONTH, NEW_ASSIGNMENT_CLOSEDAY, NEW_ASSIGNMENT_CLOSEYEAR, NEW_ASSIGNMENT_CLOSEHOUR, NEW_ASSIGNMENT_CLOSEMIN, "date.closedate");
		if (openTime != null && closeTime != null && !closeTime.after(openTime))
		{
			addAlert(state, rb.getString("acesubdea3"));
		}
		if (dueTime != null && closeTime != null && closeTime.before(dueTime))
		{
			addAlert(state, rb.getString("acesubdea2"));
		}

		// SECTION MOD
		String sections_string = "";
		String mode = (String) state.getAttribute(STATE_MODE);
		if (mode == null) mode = "";

		state.setAttribute(NEW_ASSIGNMENT_SECTION, sections_string);
		Integer submissionType = Integer.valueOf(params.getString(NEW_ASSIGNMENT_SUBMISSION_TYPE));
		state.setAttribute(NEW_ASSIGNMENT_SUBMISSION_TYPE, submissionType);

		// Skip category if it was never set.
		Long catInt = Long.valueOf(-1);
		if(params.getString(NEW_ASSIGNMENT_CATEGORY) != null) 
			catInt = Long.valueOf(params.getString(NEW_ASSIGNMENT_CATEGORY));
			state.setAttribute(NEW_ASSIGNMENT_CATEGORY, catInt);
		
		int gradeType = -1;

		// grade type and grade points
		if (state.getAttribute(WITH_GRADES) != null && ((Boolean) state.getAttribute(WITH_GRADES)).booleanValue())
		{
			gradeType = Integer.parseInt(params.getString(NEW_ASSIGNMENT_GRADE_TYPE));
			state.setAttribute(NEW_ASSIGNMENT_GRADE_TYPE, Integer.valueOf(gradeType));
		}

		//Peer Assessment
		boolean peerAssessment = false;
		if ("peerreview".equals(additionalOptions)) {
			state.setAttribute(NEW_ASSIGNMENT_USE_PEER_ASSESSMENT, Boolean.TRUE.toString());
			peerAssessment = true;
		}
		else {
			state.setAttribute(NEW_ASSIGNMENT_USE_PEER_ASSESSMENT, Boolean.FALSE.toString());
		}

		if(peerAssessment){
			//not allowed for group assignments:
			if(groupAssignment){
				addAlert(state, rb.getString("peerassessment.invliadGroupAssignment"));
			}
			//do not allow non-electronic assignments
			if(Assignment.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION == submissionType){
				addAlert(state, rb.getString("peerassessment.invliadSubmissionTypeAssignment"));
			}
			if (gradeType != Assignment.SCORE_GRADE_TYPE){
				addAlert(state, rb.getString("peerassessment.invliadGradeTypeAssignment"));
			}

			Time peerPeriodTime = putTimeInputInState(params, state, NEW_ASSIGNMENT_PEERPERIODMONTH, NEW_ASSIGNMENT_PEERPERIODDAY, NEW_ASSIGNMENT_PEERPERIODYEAR, NEW_ASSIGNMENT_PEERPERIODHOUR, NEW_ASSIGNMENT_PEERPERIODMIN, "newassig.opedat");
			GregorianCalendar peerPeriodMinTimeCal = new GregorianCalendar();
			peerPeriodMinTimeCal.setTimeInMillis(closeTime.getTime());
			peerPeriodMinTimeCal.add(GregorianCalendar.MINUTE, 10);
			GregorianCalendar peerPeriodTimeCal = new GregorianCalendar();
			peerPeriodTimeCal.setTimeInMillis(peerPeriodTime.getTime());
			//peer assessment must complete at a minimum of 10 mins after close time
			if(peerPeriodTimeCal.before(peerPeriodMinTimeCal)){
				addAlert(state, rb.getString("peerassessment.invliadPeriodTime"));
			}
		}
		
		String b,r;
		r = params.getString(NEW_ASSIGNMENT_PEER_ASSESSMENT_ANON_EVAL);
		if (r == null) b = Boolean.FALSE.toString();
		else b = Boolean.TRUE.toString();
		state.setAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_ANON_EVAL, b);
		
		r = params.getString(NEW_ASSIGNMENT_PEER_ASSESSMENT_STUDENT_VIEW_REVIEWS);
		if (r == null) b = Boolean.FALSE.toString();
		else b = Boolean.TRUE.toString();
		state.setAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_STUDENT_VIEW_REVIEWS, b);
		if(peerAssessment){
			if(params.get(NEW_ASSIGNMENT_PEER_ASSESSMENT_NUM_REVIEWS) != null && !"".equals(params.get(NEW_ASSIGNMENT_PEER_ASSESSMENT_NUM_REVIEWS))){
				try{
					int peerAssessmentNumOfReviews = Integer.parseInt(params.getString(NEW_ASSIGNMENT_PEER_ASSESSMENT_NUM_REVIEWS));
					if(peerAssessmentNumOfReviews > 0){
						state.setAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_NUM_REVIEWS, Integer.valueOf(peerAssessmentNumOfReviews));
					}else{
						addAlert(state, rb.getString("peerassessment.invalidNumReview"));
					}
				}catch(Exception e){
					addAlert(state, rb.getString("peerassessment.invalidNumReview"));
				}
			}else{
				addAlert(state, rb.getString("peerassessment.specifyNumReview"));
			}
		}
		
		String peerAssessmentInstructions = processFormattedTextFromBrowser(state, params.getString(NEW_ASSIGNMENT_PEER_ASSESSMENT_INSTRUCTIONS), true);
		state.setAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_INSTRUCTIONS, peerAssessmentInstructions);
		
		//REVIEW SERVICE
		r = params.getString(NEW_ASSIGNMENT_USE_REVIEW_SERVICE);
		// set whether we use the review service or not
		if (r == null) b = Boolean.FALSE.toString();
		else 
		{
			b = Boolean.TRUE.toString();
			if (state.getAttribute(NEW_ASSIGNMENT_SUBMISSION_TYPE).equals(Assignment.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION))
			{
				//can't use content-review with non-electronic submissions
				addAlert(state, rb.getFormattedMessage("review.switch.ne.1", contentReviewService.getServiceName()));
			}
		}
		state.setAttribute(NEW_ASSIGNMENT_USE_REVIEW_SERVICE, b);
		
		//set whether students can view the review service results
		r = params.getString(NEW_ASSIGNMENT_ALLOW_STUDENT_VIEW);
		if (r == null) b = Boolean.FALSE.toString();
		else b = Boolean.TRUE.toString();
		state.setAttribute(NEW_ASSIGNMENT_ALLOW_STUDENT_VIEW, b);
		
		//set submit options
		r = params.getString(NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_RADIO);
		if(r == null || (!NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_STANDARD.equals(r) && !NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_INSITUTION.equals(r)))
			r = NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_NONE;
		state.setAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_RADIO, r);
		//set originality report options
		r = params.getString(NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_RADIO);
		if(r == null || !NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_DUE.equals(r))
			r = NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_IMMEDIATELY;
		state.setAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_RADIO, r);
		//set check repository options:
		r = params.getString(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_TURNITIN);
		if (r == null) b = Boolean.FALSE.toString();
		else b = Boolean.TRUE.toString();
		state.setAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_TURNITIN, b);
		
		r = params.getString(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INTERNET);
		if (r == null) b = Boolean.FALSE.toString();
		else b = Boolean.TRUE.toString();
		state.setAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INTERNET, b);
		
		r = params.getString(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_PUB);
		if (r == null) b = Boolean.FALSE.toString();
		else b = Boolean.TRUE.toString();
		state.setAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_PUB, b);
		
		r = params.getString(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INSTITUTION);
		if (r == null) b = Boolean.FALSE.toString();
		else b = Boolean.TRUE.toString();
		state.setAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INSTITUTION, b);
		
		//exclude bibliographic materials:
		r = params.getString(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_BIBLIOGRAPHIC);
		if (r == null) b = Boolean.FALSE.toString();
		else b = Boolean.TRUE.toString();
		state.setAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_BIBLIOGRAPHIC, b);
		
		//exclude quoted materials:
		r = params.getString(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_QUOTED);
		if (r == null) b = Boolean.FALSE.toString();
		else b = Boolean.TRUE.toString();
		state.setAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_QUOTED, b);

		//exclude small matches
		r = params.getString(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_SMALL_MATCHES);
		if (r == null) b = Boolean.FALSE.toString();
		else b = Boolean.TRUE.toString();
		state.setAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_SMALL_MATCHES, b);
		
		//exclude type:
		//only options are 0=none, 1=words, 2=percentages
		r = params.getString(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_TYPE);
		if(!"0".equals(r) && !"1".equals(r) && !"2".equals(r)){
			//this really shouldn't ever happen (unless someone's messing with the parameters)
			r = "0";
		}
		state.setAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_TYPE, r);
		
		//exclude value
		if(!"0".equals(r)){
			r = params.getString(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_VALUE);
			try{
				int rInt = Integer.parseInt(r);
				if(rInt < 0 || rInt > 100){
					addAlert(state, rb.getString("review.exclude.matches.value_error"));
				}
			}catch (Exception e) {
				addAlert(state, rb.getString("review.exclude.matches.value_error"));
			}
			state.setAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_VALUE, r);
		}else{
			state.setAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_VALUE, "1");
		}
		
		// treat the new assignment description as formatted text
		boolean checkForFormattingErrors = true; // instructor is creating a new assignment - so check for errors
		String description = processFormattedTextFromBrowser(state, params.getCleanString(NEW_ASSIGNMENT_DESCRIPTION),
				checkForFormattingErrors);
		state.setAttribute(NEW_ASSIGNMENT_DESCRIPTION, description);

		if (state.getAttribute(CALENDAR) != null || state.getAttribute(ADDITIONAL_CALENDAR) != null)
		{
			// calendar enabled for the site
			if (params.getString(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE) != null
					&& params.getString(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE).equalsIgnoreCase(Boolean.TRUE.toString()))
			{
				state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE, Boolean.TRUE.toString());
			}
			else
			{
				state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE, Boolean.FALSE.toString());
			}
		}
		else
		{
			// no calendar yet for the site
			state.removeAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE);
		}

		if (params.getString(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE) != null
				&& params.getString(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE)
						.equalsIgnoreCase(Boolean.TRUE.toString()))
		{
			state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE, Boolean.TRUE.toString());
		}
		else
		{
			state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE, Boolean.FALSE.toString());
		}
		
		if (params.getString(Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION) != null) {
			if (params.getString(Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION)
					.equalsIgnoreCase(Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION_NONE)) {
				state.setAttribute(Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION, Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION_NONE);
			}
			else if (params.getString(Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION)
					.equalsIgnoreCase(Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION_LOW)) {
				state.setAttribute(Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION, Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION_LOW);
			}
			else if (params.getString(Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION)
					.equalsIgnoreCase(Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION_HIGH)) {
				state.setAttribute(Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION, Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION_HIGH);
			}
		}		

		if (params.getString(NEW_ASSIGNMENT_CHECK_HIDE_DUE_DATE) != null
				&& params.getString(NEW_ASSIGNMENT_CHECK_HIDE_DUE_DATE)
						.equalsIgnoreCase(Boolean.TRUE.toString()))
		{
			state.setAttribute(NEW_ASSIGNMENT_CHECK_HIDE_DUE_DATE, Boolean.TRUE.toString());
		}
		else
		{
			state.setAttribute(NEW_ASSIGNMENT_CHECK_HIDE_DUE_DATE, Boolean.FALSE.toString());
		}

		String s = params.getString(NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE);

		// set the honor pledge to be "no honor pledge"
		if (s == null) s = "1";
		state.setAttribute(NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE, s);

		String grading = params.getString(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK);
		state.setAttribute(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, grading);

		// SAK-17606
		state.setAttribute(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING, params.getString(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING));

		// only when choose to associate with assignment in Gradebook
		String associateAssignment = params.getString(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);

		if (grading != null)
		{
			if (grading.equals(AssignmentService.GRADEBOOK_INTEGRATION_ASSOCIATE))
			{
				state.setAttribute(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT, associateAssignment);
			}
			else
			{
				state.setAttribute(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT, "");
			}

			if (!grading.equals(AssignmentService.GRADEBOOK_INTEGRATION_NO))
			{
				// gradebook integration only available to point-grade assignment
				if (gradeType != Assignment.SCORE_GRADE_TYPE)
				{
					addAlert(state, rb.getString("addtogradebook.wrongGradeScale"));
				}

				// if chosen as "associate", have to choose one assignment from Gradebook
				if (grading.equals(AssignmentService.GRADEBOOK_INTEGRATION_ASSOCIATE) && StringUtils.trimToNull(associateAssignment) == null)
				{
					addAlert(state, rb.getString("grading.associate.alert"));
				}
			}
		}

		List attachments = (List) state.getAttribute(ATTACHMENTS);
		if (attachments == null || attachments.isEmpty())
		{
			// read from vm file
			String[] attachmentIds = data.getParameters().getStrings("attachments");
			if (attachmentIds != null && attachmentIds.length != 0)
			{
				attachments = new ArrayList();
				for (int i= 0; i<attachmentIds.length;i++)
				{
					attachments.add(EntityManager.newReference(attachmentIds[i]));
				}
			}
		}
		state.setAttribute(NEW_ASSIGNMENT_ATTACHMENT, attachments);

		if (validify)
		{
			if ((description == null) || (description.length() == 0) || ("<br/>".equals(description)) && ((attachments == null || attachments.size() == 0)))
			{
				// if there is no description nor an attachment, show the following alert message.
				// One could ignore the message and still post the assignment
				if (state.getAttribute(NEW_ASSIGNMENT_DESCRIPTION_EMPTY) == null)
				{
					state.setAttribute(NEW_ASSIGNMENT_DESCRIPTION_EMPTY, Boolean.TRUE.toString());
				}
				else
				{
					state.removeAttribute(NEW_ASSIGNMENT_DESCRIPTION_EMPTY);
				}
			}
			else
			{
				state.removeAttribute(NEW_ASSIGNMENT_DESCRIPTION_EMPTY);
			}
		}

		if (validify && state.getAttribute(NEW_ASSIGNMENT_DESCRIPTION_EMPTY) != null)
		{
			addAlert(state, rb.getString("thiasshas"));
		}
		
		// assignment range?
		String range = data.getParameters().getString("range");
		state.setAttribute(NEW_ASSIGNMENT_RANGE, range);
		if ("groups".equals(range))
		{
			String[] groupChoice = data.getParameters().getStrings("selectedGroups");
			if (groupChoice != null && groupChoice.length != 0)
			{
				state.setAttribute(NEW_ASSIGNMENT_GROUPS, new ArrayList(Arrays.asList(groupChoice)));
			}
			else
			{
				state.setAttribute(NEW_ASSIGNMENT_GROUPS, null);
				addAlert(state, rb.getString("java.alert.youchoosegroup"));
			}
		}
		else
		{
			state.removeAttribute(NEW_ASSIGNMENT_GROUPS);
		}
		
                // check groups for duplicate members here
                if (groupAssignment) {
                    Collection<String> _dupUsers = usersInMultipleGroups(state, "groups".equals(range),("groups".equals(range) ? data.getParameters().getStrings("selectedGroups") : null), false, null);
                    if (_dupUsers.size() > 0) {
                        StringBuilder _sb = new StringBuilder(rb.getString("group.user.multiple.warning") + " ");
                        Iterator<String> _it = _dupUsers.iterator();
                        if (_it.hasNext()) _sb.append(_it.next());
                        while (_it.hasNext())
                            _sb.append(", " + _it.next());                        
                        addAlert(state, _sb.toString());
                        M_log.warn(this + ":post_save_assignment at least one user in multiple groups.");
                    }
                }
                
		// allow resubmission numbers
		if (params.getString("allowResToggle") != null && params.getString(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER) != null)
		{
			// read in allowResubmit params 
			Time resubmitCloseTime = readAllowResubmitParams(params, state, null);
			if (resubmitCloseTime != null) {
			    // check the date is valid
			    if (openTime != null && ! resubmitCloseTime.after(openTime)) {
                    addAlert(state, rb.getString("acesubdea6"));
			    }
			}
		}
		else if (!Integer.valueOf(Assignment.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION).equals(state.getAttribute(NEW_ASSIGNMENT_SUBMISSION_TYPE)))
		{
			/* 
			 * SAK-26640: If the instructor switches to non-electronic by mistake, the resubmissions settings should persist so they can be easily retrieved.
			 * So we only reset resubmit params for electronic assignments.
			 */
			resetAllowResubmitParams(state);
		}
		
		// assignment notification option
		String notiOption = params.getString(ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS);
		if (notiOption != null)
		{
			state.setAttribute(Assignment.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_VALUE, notiOption);
		}
		
		// release grade notification option
		String releaseGradeOption = params.getString(ASSIGNMENT_RELEASEGRADE_NOTIFICATION);
		if (releaseGradeOption != null)
		{
			state.setAttribute(Assignment.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_VALUE, releaseGradeOption);
		}
		// release resubmission notification option
		String releaseResubmissionOption = params.getString(ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION);
		if (releaseResubmissionOption != null){
			state.setAttribute(Assignment.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_VALUE, releaseResubmissionOption);
		}
		// read inputs for supplement items
		setNewAssignmentParametersSupplementItems(validify, state, params);
		
		if (state.getAttribute(WITH_GRADES) != null && ((Boolean) state.getAttribute(WITH_GRADES)).booleanValue())
		{
			// the grade point
			String gradePoints = params.getString(NEW_ASSIGNMENT_GRADE_POINTS);
			state.setAttribute(NEW_ASSIGNMENT_GRADE_POINTS, gradePoints);
			if (gradePoints != null)
			{
				if (gradeType == 3)
				{
					if ((gradePoints.length() == 0))
					{
						// in case of point grade assignment, user must specify maximum grade point
						addAlert(state, rb.getString("plespethe3"));
					}
					else
					{
						Integer scaleFactor = AssignmentService.getScaleFactor();
						try {
							if (StringUtils.isNotEmpty(assignmentRef)) {
								Assignment assignment = AssignmentService.getAssignment(assignmentRef);
								if (assignment != null && assignment.getContent() != null) {
									scaleFactor = assignment.getContent().getFactor();
								}
							}
						} catch (IdUnusedException | PermissionException e) {
							M_log.error(e.getMessage());
						}
						
						validPointGrade(state, gradePoints, scaleFactor);
						// when scale is points, grade must be integer and less than maximum value
						if (state.getAttribute(STATE_MESSAGE) == null)
						{
							gradePoints = scalePointGrade(state, gradePoints, scaleFactor);
						}
						if (state.getAttribute(STATE_MESSAGE) == null)
						{
							state.setAttribute(NEW_ASSIGNMENT_GRADE_POINTS, gradePoints);
						}
					}
				}
			}
		}
		
	} // setNewAssignmentParameters

	/**
	 * check to see whether there is already an assignment with the same title in the site
	 * @param assignmentRef
	 * @param title
	 * @param contextString
	 * @return
	 */
	private boolean sameAssignmentTitleInContext(String assignmentRef, String title, String contextString) {
		boolean rv = false;
		// in the student list view of assignments
		Iterator assignments = AssignmentService.getAssignmentsForContext(contextString);
		while (assignments.hasNext())
		{
			Assignment a = (Assignment) assignments.next();
			if (assignmentRef == null || !assignmentRef.equals(a.getReference()))
			{
				// don't do self-compare
				String aTitle = a.getTitle();
				if (aTitle != null && aTitle.length() > 0 && title.equals(aTitle))
				{
					//further check whether the assignment is marked as deleted or not
					String deleted = a.getProperties().getProperty(ResourceProperties.PROP_ASSIGNMENT_DELETED);
					if (deleted == null || deleted != null && !Boolean.TRUE.toString().equalsIgnoreCase(deleted))
					{
						rv = true;
					}
				}
			}
		}
		return rv;
	}

	/**
	 * read inputs for supplement items
	 * @param validify
	 * @param state
	 * @param params
	 */
	private void setNewAssignmentParametersSupplementItems(boolean validify,
			SessionState state, ParameterParser params) {
		/********************* MODEL ANSWER ITEM *********************/
		String modelAnswer_to_delete = StringUtils.trimToNull(params.getString("modelanswer_to_delete"));
		if (modelAnswer_to_delete != null)
		{
			state.setAttribute(MODELANSWER_TO_DELETE, modelAnswer_to_delete);
		}
		String modelAnswer_text = StringUtils.trimToNull(params.getString("modelanswer_text"));
		if (modelAnswer_text != null)
		{
			state.setAttribute(MODELANSWER_TEXT, modelAnswer_text);
		}
		String modelAnswer_showto = StringUtils.trimToNull(params.getString("modelanswer_showto"));
		if (modelAnswer_showto != null)
		{
			state.setAttribute(MODELANSWER_SHOWTO, modelAnswer_showto);
		}
		if (modelAnswer_text != null || !"0".equals(modelAnswer_showto) || state.getAttribute(MODELANSWER_ATTACHMENTS) != null)
		{
			// there is Model Answer input
			state.setAttribute(MODELANSWER, Boolean.TRUE);
			
			if (validify && !"true".equalsIgnoreCase(modelAnswer_to_delete))
			{
				// show alert when there is no model answer input
				if (modelAnswer_text == null)
				{
					addAlert(state, rb.getString("modelAnswer.alert.modelAnswer"));
				}
				// show alert when user didn't select show-to option
				if ("0".equals(modelAnswer_showto))
				{
					addAlert(state, rb.getString("modelAnswer.alert.showto"));
				}
			}
		}
		else
		{
			state.removeAttribute(MODELANSWER);
		}
		
		/**************** NOTE ITEM ********************/
		String note_to_delete = StringUtils.trimToNull(params.getString("note_to_delete"));
		if (note_to_delete != null)
		{
			state.setAttribute(NOTE_TO_DELETE, note_to_delete);
		}
		String note_text = StringUtils.trimToNull(params.getString("note_text"));
		if (note_text != null)
		{
			state.setAttribute(NOTE_TEXT, note_text);
		}
		String note_to = StringUtils.trimToNull(params.getString("note_to"));
		if (note_to != null)
		{
			state.setAttribute(NOTE_SHAREWITH, note_to);
		}
		if (note_text != null || !"0".equals(note_to))
		{
			// there is Note Item input
			state.setAttribute(NOTE, Boolean.TRUE);
			
			if (validify && !"true".equalsIgnoreCase(note_to_delete))
			{
				// show alert when there is no note text
				if (note_text == null)
				{
					addAlert(state, rb.getString("note.alert.text"));
				}
				// show alert when there is no share option
				if ("0".equals(note_to))
				{
					addAlert(state, rb.getString("note.alert.to"));
				}
			}
		}
		else
		{
			state.removeAttribute(NOTE);
		}
		
		
		/****************** ALL PURPOSE ITEM **********************/
		String allPurpose_to_delete = StringUtils.trimToNull(params.getString("allPurpose_to_delete"));
		if ( allPurpose_to_delete != null)
		{
			state.setAttribute(ALLPURPOSE_TO_DELETE, allPurpose_to_delete);
		}
		String allPurposeTitle = StringUtils.trimToNull(params.getString("allPurposeTitle"));
		if (allPurposeTitle != null)
		{
			state.setAttribute(ALLPURPOSE_TITLE, allPurposeTitle);
		}
		String allPurposeText = StringUtils.trimToNull(params.getString("allPurposeText"));
		if (allPurposeText != null)
		{
			state.setAttribute(ALLPURPOSE_TEXT, allPurposeText);
		}
		if (StringUtils.trimToNull(params.getString("allPurposeHide")) != null)
		{
			state.setAttribute(ALLPURPOSE_HIDE, Boolean.valueOf(params.getString("allPurposeHide")));
		}
		if (StringUtils.trimToNull(params.getString("allPurposeShowFrom")) != null)
		{
			state.setAttribute(ALLPURPOSE_SHOW_FROM, Boolean.valueOf(params.getString("allPurposeShowFrom")));
			// allpurpose release time
			putTimeInputInState(params, state, ALLPURPOSE_RELEASE_MONTH, ALLPURPOSE_RELEASE_DAY, ALLPURPOSE_RELEASE_YEAR, ALLPURPOSE_RELEASE_HOUR, ALLPURPOSE_RELEASE_MIN, "date.allpurpose.releasedate");
		}
		else
		{
			state.removeAttribute(ALLPURPOSE_SHOW_FROM);
		}
		if (StringUtils.trimToNull(params.getString("allPurposeShowTo")) != null)
		{
			state.setAttribute(ALLPURPOSE_SHOW_TO, Boolean.valueOf(params.getString("allPurposeShowTo")));
			// allpurpose retract time
			putTimeInputInState(params, state, ALLPURPOSE_RETRACT_MONTH, ALLPURPOSE_RETRACT_DAY, ALLPURPOSE_RETRACT_YEAR, ALLPURPOSE_RETRACT_HOUR, ALLPURPOSE_RETRACT_MIN, "date.allpurpose.retractdate");
		}
		else
		{
			state.removeAttribute(ALLPURPOSE_SHOW_TO);
		}
		
		String siteId = (String)state.getAttribute(STATE_CONTEXT_STRING);
		List<String> accessList = new ArrayList<String>();
		try
		{
			AuthzGroup realm = authzGroupService.getAuthzGroup(SiteService.siteReference(siteId));
			Set<Role> roles = realm.getRoles();
			for(Iterator iRoles = roles.iterator(); iRoles.hasNext();)
			{
				// iterator through roles first
				Role role = (Role) iRoles.next();
				if (params.getString("allPurpose_" + role.getId()) != null)
				{
					accessList.add(role.getId());
				}
				else
				{
					// if the role is not selected, iterate through the users with this role
					Set userIds = realm.getUsersHasRole(role.getId());
					for(Iterator iUserIds = userIds.iterator(); iUserIds.hasNext();)
					{
						String userId = (String) iUserIds.next();
						if (params.getString("allPurpose_" + userId) != null)
						{
							accessList.add(userId);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			M_log.warn(this + ":setNewAssignmentParameters" + e.toString() + "error finding authzGroup for = " + siteId);
		}
		state.setAttribute(ALLPURPOSE_ACCESS, accessList);
	
		if (allPurposeTitle != null || allPurposeText != null || (accessList != null && !accessList.isEmpty()) || state.getAttribute(ALLPURPOSE_ATTACHMENTS) != null)
		{
			// there is allpupose item input
			state.setAttribute(ALLPURPOSE, Boolean.TRUE);
			
			if (validify && !"true".equalsIgnoreCase(allPurpose_to_delete))
			{
				if (allPurposeTitle == null)
				{
					// missing title
					addAlert(state, rb.getString("allPurpose.alert.title"));
				}
				if (allPurposeText == null)
				{
					// missing text
					addAlert(state, rb.getString("allPurpose.alert.text"));
				}
				if (accessList == null || accessList.isEmpty())
				{
					// missing access choice
					addAlert(state, rb.getString("allPurpose.alert.access"));
				}
			}
		}
		else
		{
			state.removeAttribute(ALLPURPOSE);
		}
	}
	
	/**
	 * read time input and assign it to state attributes
	 * @param params
	 * @param state
	 * @param monthString
	 * @param dayString
	 * @param yearString
	 * @param hourString
	 * @param minString
	 * @param invalidBundleMessage
	 * @return
	 */
	Time putTimeInputInState(ParameterParser params, SessionState state, String monthString, String dayString, String yearString, String hourString, String minString, String invalidBundleMessage)
	{
		int month = (Integer.valueOf(params.getString(monthString))).intValue();
		state.setAttribute(monthString, Integer.valueOf(month));
		int day = (Integer.valueOf(params.getString(dayString))).intValue();
		state.setAttribute(dayString, Integer.valueOf(day));
		int year = (Integer.valueOf(params.getString(yearString))).intValue();
		state.setAttribute(yearString, Integer.valueOf(year));
		int hour = (Integer.valueOf(params.getString(hourString))).intValue();
		state.setAttribute(hourString, Integer.valueOf(hour));
		int min = (Integer.valueOf(params.getString(minString))).intValue();
		state.setAttribute(minString, Integer.valueOf(min));
		// validate date
		if (!Validator.checkDate(day, month, year))
		{
			addAlert(state, rb.getFormattedMessage("date.invalid", new Object[]{rb.getString(invalidBundleMessage)}));
		}
		return TimeService.newTimeLocal(year, month, day, hour, min, 0, 0);
	}

	/**
	 * Action is to hide the preview assignment student view
	 */
	public void doHide_submission_assignment_instruction(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(GRADE_SUBMISSION_ASSIGNMENT_EXPAND_FLAG, Boolean.valueOf(false));

		// save user input
		readGradeForm(data, state, "read");

	} // doHide_preview_assignment_student_view

	/**
	 * Action is to hide the preview assignment student view
	 */
	public void doHide_submission_assignment_instruction_review(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(GRADE_SUBMISSION_ASSIGNMENT_EXPAND_FLAG, Boolean.valueOf(false));

		// save user input
		saveReviewGradeForm(data, state, "read");

	}
	
	public void doShow_submission_assignment_instruction_review(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(GRADE_SUBMISSION_ASSIGNMENT_EXPAND_FLAG, Boolean.valueOf(true));

		// save user input
		saveReviewGradeForm(data, state, "read");
	}
	
	/**
	 * Action is to show the preview assignment student view
	 */
	public void doShow_submission_assignment_instruction(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(GRADE_SUBMISSION_ASSIGNMENT_EXPAND_FLAG, Boolean.valueOf(true));

		// save user input
		readGradeForm(data, state, "read");

	} // doShow_submission_assignment_instruction

	/**
	 * Action is to hide the preview assignment student view
	 */
	public void doHide_preview_assignment_student_view(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(PREVIEW_ASSIGNMENT_STUDENT_VIEW_HIDE_FLAG, Boolean.valueOf(true));

	} // doHide_preview_assignment_student_view

	/**
	 * Action is to show the preview assignment student view
	 */
	public void doShow_preview_assignment_student_view(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(PREVIEW_ASSIGNMENT_STUDENT_VIEW_HIDE_FLAG, Boolean.valueOf(false));

	} // doShow_preview_assignment_student_view

	/**
	 * Action is to hide the preview assignment assignment infos
	 */
	public void doHide_preview_assignment_assignment(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(PREVIEW_ASSIGNMENT_ASSIGNMENT_HIDE_FLAG, Boolean.valueOf(true));

	} // doHide_preview_assignment_assignment

	/**
	 * Action is to show the preview assignment assignment info
	 */
	public void doShow_preview_assignment_assignment(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(PREVIEW_ASSIGNMENT_ASSIGNMENT_HIDE_FLAG, Boolean.valueOf(false));

	} // doShow_preview_assignment_assignment

	/**
	 * Action is to hide the assignment content in the view assignment page
	 */
	public void doHide_view_assignment(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(VIEW_ASSIGNMENT_HIDE_ASSIGNMENT_FLAG, Boolean.valueOf(true));

	} // doHide_view_assignment

	/**
	 * Action is to show the assignment content in the view assignment page
	 */
	public void doShow_view_assignment(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(VIEW_ASSIGNMENT_HIDE_ASSIGNMENT_FLAG, Boolean.valueOf(false));

	} // doShow_view_assignment

	/**
	 * Action is to hide the student view in the view assignment page
	 */
	public void doHide_view_student_view(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(VIEW_ASSIGNMENT_HIDE_STUDENT_VIEW_FLAG, Boolean.valueOf(true));

	} // doHide_view_student_view

	/**
	 * Action is to show the student view in the view assignment page
	 */
	public void doShow_view_student_view(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(VIEW_ASSIGNMENT_HIDE_STUDENT_VIEW_FLAG, Boolean.valueOf(false));

	} // doShow_view_student_view

	/**
	 * Action is to post assignment
	 */
	public void doPost_assignment(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		// post assignment
		post_save_assignment(data, "post");

	} // doPost_assignment

	/**
	 * Action is to tag items via an items tagging helper
	 */
	public void doHelp_items(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();

		TaggingManager taggingManager = (TaggingManager) ComponentManager
				.get("org.sakaiproject.taggable.api.TaggingManager");
		TaggingProvider provider = taggingManager.findProviderById(params
				.getString(PROVIDER_ID));

		String activityRef = params.getString(ACTIVITY_REF);

		TaggingHelperInfo helperInfo = provider
				.getItemsHelperInfo(activityRef);

		// get into helper mode with this helper tool
		startHelper(data.getRequest(), helperInfo.getHelperId());

		Map<String, ? extends Object> helperParms = helperInfo
				.getParameterMap();

		for (Map.Entry<String, ? extends Object> entry : helperParms.entrySet()) {
			state.setAttribute(entry.getKey(), entry.getValue());
		}
	} // doHelp_items
	
	/**
	 * Action is to tag an individual item via an item tagging helper
	 */
	public void doHelp_item(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();

		TaggingManager taggingManager = (TaggingManager) ComponentManager
				.get("org.sakaiproject.taggable.api.TaggingManager");
		TaggingProvider provider = taggingManager.findProviderById(params
				.getString(PROVIDER_ID));

		String itemRef = params.getString(ITEM_REF);

		TaggingHelperInfo helperInfo = provider
				.getItemHelperInfo(itemRef);

		// get into helper mode with this helper tool
		startHelper(data.getRequest(), helperInfo.getHelperId());

		Map<String, ? extends Object> helperParms = helperInfo
				.getParameterMap();

		for (Map.Entry<String, ? extends Object> entry : helperParms.entrySet()) {
			state.setAttribute(entry.getKey(), entry.getValue());
		}
	} // doHelp_item
	
	/**
	 * Action is to tag an activity via an activity tagging helper
	 */
	public void doHelp_activity(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();

		TaggingManager taggingManager = (TaggingManager) ComponentManager
				.get("org.sakaiproject.taggable.api.TaggingManager");
		TaggingProvider provider = taggingManager.findProviderById(params
				.getString(PROVIDER_ID));

		String activityRef = params.getString(ACTIVITY_REF);

		TaggingHelperInfo helperInfo = provider
				.getActivityHelperInfo(activityRef);

		// get into helper mode with this helper tool
		startHelper(data.getRequest(), helperInfo.getHelperId());

		Map<String, ? extends Object> helperParms = helperInfo
				.getParameterMap();

		for (Map.Entry<String, ? extends Object> entry : helperParms.entrySet()) {
			state.setAttribute(entry.getKey(), entry.getValue());
		}
	} // doHelp_activity
	
	/**
	 * post or save assignment
	 */
	private void post_save_assignment(RunData data, String postOrSave)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		
		ParameterParser params = data.getParameters();
		
		String siteId = (String) state.getAttribute(STATE_CONTEXT_STRING);
		
		boolean post = (postOrSave != null) && "post".equals(postOrSave);

		// assignment old title
		String aOldTitle = null;
		
		// assignment old access setting
		String aOldAccessString = null;
		
		// assignment old group setting
		Collection aOldGroups = null;
		
		// assignment old open date setting
		Time oldOpenTime = null;
		
		// assignment old due date setting
		Time oldDueTime = null;
		
		// assignment old visible date setting
		Time oldVisibleTime = null;
		
		// assignment old close date setting
		Time oldCloseTime = null;

		// assignment old associated Gradebook entry if any
		String oAssociateGradebookAssignment = null;

		String mode = (String) state.getAttribute(STATE_MODE);
		if (!MODE_INSTRUCTOR_PREVIEW_ASSIGNMENT.equals(mode))
		{
			// read input data if the mode is not preview mode
			setNewAssignmentParameters(data, true);
		}
		
		String assignmentId = params.getString("assignmentId");
		String assignmentContentId = params.getString("assignmentContentId");
		
		// whether this is an editing which changes non-point graded assignment to point graded assignment?
		boolean bool_change_from_non_point = false;
		// whether there is a change in the assignment resubmission choice
		boolean bool_change_resubmit_option = false;

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			// AssignmentContent object
			AssignmentContentEdit ac = editAssignmentContent(assignmentContentId, "post_save_assignment", state, true);
			bool_change_from_non_point = change_from_non_point(state, assignmentId, assignmentContentId, ac);
			
			// Assignment
			AssignmentEdit a = editAssignment(assignmentId, "post_save_assignment", state, true);
			bool_change_resubmit_option = change_resubmit_option(state, a);

			// put the names and values into vm file
			String title = (String) state.getAttribute(NEW_ASSIGNMENT_TITLE);
			String order = (String) state.getAttribute(NEW_ASSIGNMENT_ORDER);

			// open time
			Time openTime = getTimeFromState(state, NEW_ASSIGNMENT_OPENMONTH, NEW_ASSIGNMENT_OPENDAY, NEW_ASSIGNMENT_OPENYEAR, NEW_ASSIGNMENT_OPENHOUR, NEW_ASSIGNMENT_OPENMIN);

			// visible time
			Time visibleTime = null;
                        if (Boolean.valueOf(ServerConfigurationService.getBoolean("assignment.visible.date.enabled", false))) {
                             if (((Boolean) state.getAttribute(NEW_ASSIGNMENT_VISIBLETOGGLE)))
                                 visibleTime = getTimeFromState(state, NEW_ASSIGNMENT_VISIBLEMONTH, NEW_ASSIGNMENT_VISIBLEDAY, NEW_ASSIGNMENT_VISIBLEYEAR, NEW_ASSIGNMENT_VISIBLEHOUR, NEW_ASSIGNMENT_VISIBLEMIN);
                        }

			// due time
			Time dueTime = getTimeFromState(state, NEW_ASSIGNMENT_DUEMONTH, NEW_ASSIGNMENT_DUEDAY, NEW_ASSIGNMENT_DUEYEAR, NEW_ASSIGNMENT_DUEHOUR, NEW_ASSIGNMENT_DUEMIN);

			// close time
			Time closeTime = dueTime;
			boolean enableCloseDate = ((Boolean) state.getAttribute(NEW_ASSIGNMENT_ENABLECLOSEDATE)).booleanValue();
			if (enableCloseDate)
			{
				closeTime = getTimeFromState(state, NEW_ASSIGNMENT_CLOSEMONTH, NEW_ASSIGNMENT_CLOSEDAY, NEW_ASSIGNMENT_CLOSEYEAR, NEW_ASSIGNMENT_CLOSEHOUR, NEW_ASSIGNMENT_CLOSEMIN);
			}

			// sections
			String section = (String) state.getAttribute(NEW_ASSIGNMENT_SECTION);

			int submissionType = ((Integer) state.getAttribute(NEW_ASSIGNMENT_SUBMISSION_TYPE)).intValue();

			int gradeType = ((Integer) state.getAttribute(NEW_ASSIGNMENT_GRADE_TYPE)).intValue();

			boolean isGroupSubmit = "1".equals((String)state.getAttribute(NEW_ASSIGNMENT_GROUP_SUBMIT));

			String gradePoints = (String) state.getAttribute(NEW_ASSIGNMENT_GRADE_POINTS);

			String description = (String) state.getAttribute(NEW_ASSIGNMENT_DESCRIPTION);

			String checkAddDueTime = state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE)!=null?(String) state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE):null;
			boolean hideDueDate = "true".equals((String) state.getAttribute(NEW_ASSIGNMENT_CHECK_HIDE_DUE_DATE));

			String checkAutoAnnounce = (String) state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE);
			
			String valueOpenDateNotification = (String) state.getAttribute(Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION);

			String checkAddHonorPledge = (String) state.getAttribute(NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE);

			String addtoGradebook = state.getAttribute(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK) != null?(String) state.getAttribute(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK):"" ;

			long category = state.getAttribute(NEW_ASSIGNMENT_CATEGORY) != null ? ((Long) state.getAttribute(NEW_ASSIGNMENT_CATEGORY)).longValue() : -1;
			
			String associateGradebookAssignment = (String) state.getAttribute(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
			
			String allowResubmitNumber = state.getAttribute(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER) != null?(String) state.getAttribute(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER):null;

			// SAK-17606
			String checkAnonymousGrading = state.getAttribute(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING) != null? (String) state.getAttribute(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING):"";

			// SAK-26319 - we no longer clear the resubmit number for non electronic submissions; the instructor may switch to another submission type in the future

			//Peer Assessment
			boolean usePeerAssessment = "true".equalsIgnoreCase((String) state.getAttribute(NEW_ASSIGNMENT_USE_PEER_ASSESSMENT));
			Time peerPeriodTime = getTimeFromState(state, NEW_ASSIGNMENT_PEERPERIODMONTH, NEW_ASSIGNMENT_PEERPERIODDAY, NEW_ASSIGNMENT_PEERPERIODYEAR, NEW_ASSIGNMENT_PEERPERIODHOUR, NEW_ASSIGNMENT_PEERPERIODMIN);
			boolean peerAssessmentAnonEval = "true".equalsIgnoreCase((String) state.getAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_ANON_EVAL));
			boolean peerAssessmentStudentViewReviews = "true".equalsIgnoreCase((String) state.getAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_STUDENT_VIEW_REVIEWS));
			int peerAssessmentNumReviews = 0;
			if(state.getAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_NUM_REVIEWS) != null){
				peerAssessmentNumReviews = ((Integer) state.getAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_NUM_REVIEWS)).intValue();
			}
			String peerAssessmentInstructions = (String) state.getAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_INSTRUCTIONS);
			
			//Review Service
			boolean useReviewService = "true".equalsIgnoreCase((String) state.getAttribute(NEW_ASSIGNMENT_USE_REVIEW_SERVICE));
			
			boolean allowStudentViewReport = "true".equalsIgnoreCase((String) state.getAttribute(NEW_ASSIGNMENT_ALLOW_STUDENT_VIEW));

			// If the assignment switched to non-electronic, we need to use some of the assignment's previous content-review settings.
			// This way, students will maintain access to their originality reports when appropriate.
			if (submissionType == Assignment.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION)
			{
				useReviewService = ac.getAllowReviewService();
				allowStudentViewReport = ac.getAllowStudentViewReport();
			}
			
			String submitReviewRepo = (String) state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_RADIO);
			String generateOriginalityReport = (String) state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_RADIO);
			boolean checkTurnitin = "true".equalsIgnoreCase((String) state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_TURNITIN));
			boolean checkInternet = "true".equalsIgnoreCase((String) state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INTERNET));
			boolean checkPublications = "true".equalsIgnoreCase((String) state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_PUB));
			boolean checkInstitution = "true".equalsIgnoreCase((String) state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INSTITUTION));
			//exclude bibliographic materials
			boolean excludeBibliographic = "true".equalsIgnoreCase((String) state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_BIBLIOGRAPHIC));
			//exclude quoted materials
			boolean excludeQuoted = "true".equalsIgnoreCase((String) state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_QUOTED));
			//exclude small matches
			boolean excludeSmallMatches = "true".equalsIgnoreCase((String) state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_SMALL_MATCHES));
			//exclude type 0=none, 1=words, 2=percentages
			int excludeType = 0;
			int excludeValue = 1;
			if(excludeSmallMatches){
				try{
					excludeType = Integer.parseInt((String) state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_TYPE));
					if(excludeType != 0 && excludeType != 1 && excludeType != 2){
						excludeType = 0;
					}
				}catch (Exception e) {
					//Numberformatexception
				}
				//exclude value
				try{
					excludeValue = Integer.parseInt((String) state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_VALUE));
					if(excludeValue < 0 || excludeValue > 100){
						excludeValue = 1;
					}
				}catch (Exception e) {
					//Numberformatexception
				}
			}
			
			
			// the attachments
			List attachments = (List) state.getAttribute(NEW_ASSIGNMENT_ATTACHMENT);
			
			// set group property
			String range = (String) state.getAttribute(NEW_ASSIGNMENT_RANGE);
			
			Collection groups = new ArrayList();
			try
			{
				Site site = SiteService.getSite(siteId);
				Collection groupChoice = (Collection) state.getAttribute(NEW_ASSIGNMENT_GROUPS);
				if (Assignment.AssignmentAccess.GROUPED.toString().equals(range) && (groupChoice == null || groupChoice.size() == 0))
				{
					// show alert if no group is selected for the group access assignment
					addAlert(state, rb.getString("java.alert.youchoosegroup"));
				}
				else if (groupChoice != null)
				{
					for (Iterator iGroups = groupChoice.iterator(); iGroups.hasNext();)
					{
						String groupId = (String) iGroups.next();
						Group _aGroup = site.getGroup(groupId);
						if (_aGroup != null) groups.add(_aGroup);
					}
				}
			}
			catch (Exception e)
			{
				M_log.warn(this + ":post_save_assignment " + e.getMessage());
			}


			if ((state.getAttribute(STATE_MESSAGE) == null) && (ac != null) && (a != null))
			{
				aOldTitle = a.getTitle();
				
				aOldAccessString = a.getAccess().toString();
				
				aOldGroups = a.getGroups();
				
				// old open time
				oldOpenTime = a.getOpenTime();
				// old due time
				oldDueTime = a.getDueTime();
				// old visible time
				oldVisibleTime = a.getVisibleTime();
				// old close time
				oldCloseTime = a.getCloseTime();

				//assume creating the assignment with the content review service will be successful
				state.setAttribute("contentReviewSuccess", Boolean.TRUE);

				// commit the changes to AssignmentContent object
				commitAssignmentContentEdit(state, ac, a.getReference(), title, submissionType,useReviewService,allowStudentViewReport, gradeType, gradePoints, description, checkAddHonorPledge, attachments, submitReviewRepo, generateOriginalityReport, checkTurnitin, checkInternet, checkPublications, checkInstitution, excludeBibliographic, excludeQuoted, excludeType, excludeValue, openTime, dueTime, closeTime, hideDueDate);
				
				// set the Assignment Properties object
				ResourcePropertiesEdit aPropertiesEdit = a.getPropertiesEdit();
				oAssociateGradebookAssignment = aPropertiesEdit.getProperty(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
				Time resubmitCloseTime = getTimeFromState(state, ALLOW_RESUBMIT_CLOSEMONTH, ALLOW_RESUBMIT_CLOSEDAY, ALLOW_RESUBMIT_CLOSEYEAR, ALLOW_RESUBMIT_CLOSEHOUR, ALLOW_RESUBMIT_CLOSEMIN);

				// SAK-17606
				editAssignmentProperties(a, checkAddDueTime, checkAutoAnnounce, addtoGradebook, associateGradebookAssignment, allowResubmitNumber, aPropertiesEdit, post, resubmitCloseTime, checkAnonymousGrading);

				//TODO: ADD_DUE_DATE
				// the notification option
				if (state.getAttribute(Assignment.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_VALUE) != null)
				{
					aPropertiesEdit.addProperty(Assignment.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_VALUE, (String) state.getAttribute(Assignment.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_VALUE));
				}
				
				// the release grade notification option
				if (state.getAttribute(Assignment.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_VALUE) != null)
				{
					aPropertiesEdit.addProperty(Assignment.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_VALUE, (String) state.getAttribute(Assignment.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_VALUE));
				}

				if (state.getAttribute(Assignment.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_VALUE) != null){
					aPropertiesEdit.addProperty(Assignment.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_VALUE, (String) state.getAttribute(Assignment.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_VALUE));
				}
				
				// comment the changes to Assignment object
				commitAssignmentEdit(state, post, ac, a, title, visibleTime, openTime, dueTime, closeTime, enableCloseDate, section, range, groups, isGroupSubmit, 
						usePeerAssessment,peerPeriodTime, peerAssessmentAnonEval, peerAssessmentStudentViewReviews, peerAssessmentNumReviews, peerAssessmentInstructions);

				if (post)
				{
					// we need to update the submission
					if (bool_change_from_non_point || bool_change_resubmit_option)
					{
						List submissions = AssignmentService.getSubmissions(a);
						if (submissions != null && submissions.size() >0)
						{
							// assignment already exist and with submissions
							for (Iterator iSubmissions = submissions.iterator(); iSubmissions.hasNext();)
							{
								AssignmentSubmission s = (AssignmentSubmission) iSubmissions.next();
								AssignmentSubmissionEdit sEdit = editSubmission(s.getReference(), "post_save_assignment", state);
								if (sEdit != null)
								{
									ResourcePropertiesEdit sPropertiesEdit = sEdit.getPropertiesEdit();
									if (bool_change_from_non_point)
									{
										// set the grade to be empty for now
										sEdit.setGrade("");
										sEdit.setGraded(false);
										sEdit.setGradedBy(null);
										sEdit.setGradeReleased(false);
										sEdit.setReturned(false);
									}
									if (bool_change_resubmit_option)
									{
										String aAllowResubmitNumber = a.getProperties().getProperty(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER);
										if (aAllowResubmitNumber == null || aAllowResubmitNumber.length() == 0 || "0".equals(aAllowResubmitNumber))
										{
											sPropertiesEdit.removeProperty(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER);
											sPropertiesEdit.removeProperty(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME);
										}
										else
										{
											sPropertiesEdit.addProperty(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER, a.getProperties().getProperty(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER));
											sPropertiesEdit.addProperty(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME, a.getProperties().getProperty(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME));
										}
									}
									AssignmentService.commitEdit(sEdit);
								}
							}
						}
								
					}
	
				} //if
				
				// save supplement item information
				saveAssignmentSupplementItem(state, params, siteId, a);
				
				// set default sorting
				setDefaultSort(state);
				
				if (state.getAttribute(STATE_MESSAGE) == null)
				{
					// set the state navigation variables
					state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
					state.setAttribute(ATTACHMENTS, EntityManager.newReferenceList());
					resetAssignment(state);
					
					// integrate with other tools only if the assignment is posted
					if (post)
					{
						// add the due date to schedule if the schedule exists
						integrateWithCalendar(state, a, title, dueTime, checkAddDueTime, oldDueTime, aPropertiesEdit);
	
						// the open date been announced
						integrateWithAnnouncement(state, aOldTitle, a, title, openTime, checkAutoAnnounce, valueOpenDateNotification, oldOpenTime);
	
						// integrate with Gradebook
						try
						{
							initIntegrateWithGradebook(state, siteId, aOldTitle, oAssociateGradebookAssignment, a, title, dueTime, gradeType, gradePoints, addtoGradebook, associateGradebookAssignment, range, category);
						}
						catch (AssignmentHasIllegalPointsException e)
						{
							addAlert(state, rb.getString("addtogradebook.illegalPoints"));
							M_log.warn(this + ":post_save_assignment " + e.getMessage());
						}
					
						// log event if there is a title update
						if (!aOldTitle.equals(title))
						{
							// title changed
							m_eventTrackingService.post(m_eventTrackingService.newEvent(AssignmentConstants.EVENT_UPDATE_ASSIGNMENT_TITLE, assignmentId, true));
						}
						
						if (!aOldAccessString.equals(a.getAccess().toString()))
						{
							// site-group access setting changed
							m_eventTrackingService.post(m_eventTrackingService.newEvent(AssignmentConstants.EVENT_UPDATE_ASSIGNMENT_ACCESS, assignmentId, true));
						}
						else
						{
							Collection aGroups = a.getGroups();
							if (!(aOldGroups == null && aGroups == null)
									&& !(aOldGroups != null && aGroups != null && aGroups.containsAll(aOldGroups) && aOldGroups.containsAll(aGroups)))
							{
								//group changed
								m_eventTrackingService.post(m_eventTrackingService.newEvent(AssignmentConstants.EVENT_UPDATE_ASSIGNMENT_ACCESS, assignmentId, true));
							}
						}
						
						if (oldOpenTime != null && !oldOpenTime.equals(a.getOpenTime()))
						{
							// open time change
							m_eventTrackingService.post(m_eventTrackingService.newEvent(AssignmentConstants.EVENT_UPDATE_ASSIGNMENT_OPENDATE, assignmentId, true));
						}
						
						if (oldDueTime != null && !oldDueTime.equals(a.getDueTime()))
						{
							// due time change
							m_eventTrackingService.post(m_eventTrackingService.newEvent(AssignmentConstants.EVENT_UPDATE_ASSIGNMENT_DUEDATE, assignmentId, true));
						}
						
						if (oldCloseTime != null && !oldCloseTime.equals(a.getCloseTime()))
						{
							// due time change
							m_eventTrackingService.post(m_eventTrackingService.newEvent(AssignmentConstants.EVENT_UPDATE_ASSIGNMENT_CLOSEDATE, assignmentId, true));
						}
					}
					
				}

			} // if

		} // if
		
	} // post_save_assignment

	/**
	 * supplement item related information
	 * @param state
	 * @param params
	 * @param siteId
	 * @param a
	 */
	private void saveAssignmentSupplementItem(SessionState state,
			ParameterParser params, String siteId, AssignmentEdit a) {
		// assignment supplement items
		String aId = a.getId();
		//model answer
		if (state.getAttribute(MODELANSWER_TO_DELETE) != null && "true".equals((String) state.getAttribute(MODELANSWER_TO_DELETE)))
		{
			// to delete the model answer
			AssignmentModelAnswerItem mAnswer = m_assignmentSupplementItemService.getModelAnswer(aId);
			if (mAnswer != null)
			{
				m_assignmentSupplementItemService.cleanAttachment(mAnswer);
				m_assignmentSupplementItemService.removeModelAnswer(mAnswer);
			}
		}
		else if (state.getAttribute(MODELANSWER_TEXT) != null)
		{
			// edit/add model answer
			AssignmentModelAnswerItem mAnswer = m_assignmentSupplementItemService.getModelAnswer(aId);
			if (mAnswer == null)
			{
				mAnswer = m_assignmentSupplementItemService.newModelAnswer();
				m_assignmentSupplementItemService.saveModelAnswer(mAnswer);
			}
			mAnswer.setAssignmentId(a.getId());
			mAnswer.setText((String) state.getAttribute(MODELANSWER_TEXT));
			mAnswer.setShowTo(state.getAttribute(MODELANSWER_SHOWTO) != null ? Integer.parseInt((String) state.getAttribute(MODELANSWER_SHOWTO)) : 0);
			mAnswer.setAttachmentSet(getAssignmentSupplementItemAttachment(state, mAnswer, MODELANSWER_ATTACHMENTS));
			m_assignmentSupplementItemService.saveModelAnswer(mAnswer);
		}
		// note
		if (state.getAttribute(NOTE_TO_DELETE) != null &&  "true".equals((String) state.getAttribute(NOTE_TO_DELETE)))
		{
			// to remove note item
			AssignmentNoteItem nNote = m_assignmentSupplementItemService.getNoteItem(aId);
			if (nNote != null)
				m_assignmentSupplementItemService.removeNoteItem(nNote);
		}
		else if (state.getAttribute(NOTE_TEXT) != null)
		{
			// edit/add private note
			AssignmentNoteItem nNote = m_assignmentSupplementItemService.getNoteItem(aId);
			if (nNote == null)
				nNote = m_assignmentSupplementItemService.newNoteItem();
			nNote.setAssignmentId(a.getId());
			nNote.setNote((String) state.getAttribute(NOTE_TEXT));
			nNote.setShareWith(state.getAttribute(NOTE_SHAREWITH) != null ? Integer.parseInt((String) state.getAttribute(NOTE_SHAREWITH)) : 0);
			nNote.setCreatorId(UserDirectoryService.getCurrentUser().getId());
			m_assignmentSupplementItemService.saveNoteItem(nNote);
		}
		// all purpose
		if (state.getAttribute(ALLPURPOSE_TO_DELETE) != null && "true".equals((String) state.getAttribute(ALLPURPOSE_TO_DELETE)))
		{
			// to remove allPurpose item
			AssignmentAllPurposeItem nAllPurpose = m_assignmentSupplementItemService.getAllPurposeItem(aId);
			if (nAllPurpose != null)
			{
				m_assignmentSupplementItemService.cleanAttachment(nAllPurpose);
				m_assignmentSupplementItemService.cleanAllPurposeItemAccess(nAllPurpose);
				m_assignmentSupplementItemService.removeAllPurposeItem(nAllPurpose);
			}
		}
		else if (state.getAttribute(ALLPURPOSE_TITLE) != null)
		{
			// edit/add private note
			AssignmentAllPurposeItem nAllPurpose = m_assignmentSupplementItemService.getAllPurposeItem(aId);
			if (nAllPurpose == null)
			{
				nAllPurpose = m_assignmentSupplementItemService.newAllPurposeItem();
				m_assignmentSupplementItemService.saveAllPurposeItem(nAllPurpose);
			}
			nAllPurpose.setAssignmentId(a.getId());
			nAllPurpose.setTitle((String) state.getAttribute(ALLPURPOSE_TITLE));
			nAllPurpose.setText((String) state.getAttribute(ALLPURPOSE_TEXT));
			
			boolean allPurposeShowFrom = state.getAttribute(ALLPURPOSE_SHOW_FROM) != null ? ((Boolean) state.getAttribute(ALLPURPOSE_SHOW_FROM)).booleanValue() : false;
			boolean allPurposeShowTo = state.getAttribute(ALLPURPOSE_SHOW_TO) != null ? ((Boolean) state.getAttribute(ALLPURPOSE_SHOW_TO)).booleanValue() : false;
			boolean allPurposeHide = state.getAttribute(ALLPURPOSE_HIDE) != null ? ((Boolean) state.getAttribute(ALLPURPOSE_HIDE)).booleanValue() : false;
			nAllPurpose.setHide(allPurposeHide);
			// save the release and retract dates
			if (allPurposeShowFrom && !allPurposeHide)
			{
				// save release date
				Time releaseTime = getTimeFromState(state, ALLPURPOSE_RELEASE_MONTH, ALLPURPOSE_RELEASE_DAY, ALLPURPOSE_RELEASE_YEAR, ALLPURPOSE_RELEASE_HOUR, ALLPURPOSE_RELEASE_MIN);
				GregorianCalendar cal = new GregorianCalendar();
				cal.setTimeInMillis(releaseTime.getTime());
				nAllPurpose.setReleaseDate(cal.getTime());
			}
			else
			{
				nAllPurpose.setReleaseDate(null);
			}
			if (allPurposeShowTo && !allPurposeHide)
			{
				// save retract date
				Time retractTime = getTimeFromState(state, ALLPURPOSE_RETRACT_MONTH, ALLPURPOSE_RETRACT_DAY, ALLPURPOSE_RETRACT_YEAR, ALLPURPOSE_RETRACT_HOUR, ALLPURPOSE_RETRACT_MIN);
				GregorianCalendar cal = new GregorianCalendar();
				cal.setTimeInMillis(retractTime.getTime());
				nAllPurpose.setRetractDate(cal.getTime());
			}
			else
			{
				nAllPurpose.setRetractDate(null);
			}
			nAllPurpose.setAttachmentSet(getAssignmentSupplementItemAttachment(state, nAllPurpose, ALLPURPOSE_ATTACHMENTS));
			
			// clean the access list first
			if (state.getAttribute(ALLPURPOSE_ACCESS) != null)
			{
				// get the access settings
				List<String> accessList = (List<String>) state.getAttribute(ALLPURPOSE_ACCESS);
				
				m_assignmentSupplementItemService.cleanAllPurposeItemAccess(nAllPurpose);
				Set<AssignmentAllPurposeItemAccess> accessSet = new HashSet<AssignmentAllPurposeItemAccess>();
				try
				{
					AuthzGroup realm = authzGroupService.getAuthzGroup(SiteService.siteReference(siteId));
					Set<Role> roles = realm.getRoles();
					for(Iterator iRoles = roles.iterator(); iRoles.hasNext();)
					{
						// iterator through roles first
						Role r = (Role) iRoles.next();
						if (accessList.contains(r.getId()))
						{
							AssignmentAllPurposeItemAccess access = m_assignmentSupplementItemService.newAllPurposeItemAccess();
							access.setAccess(r.getId());
							access.setAssignmentAllPurposeItem(nAllPurpose);
							m_assignmentSupplementItemService.saveAllPurposeItemAccess(access);
							accessSet.add(access);
						}
						else
						{
							// if the role is not selected, iterate through the users with this role
							Set userIds = realm.getUsersHasRole(r.getId());
							for(Iterator iUserIds = userIds.iterator(); iUserIds.hasNext();)
							{
								String userId = (String) iUserIds.next();
								if (accessList.contains(userId))
								{
									AssignmentAllPurposeItemAccess access = m_assignmentSupplementItemService.newAllPurposeItemAccess();
									access.setAccess(userId);
									access.setAssignmentAllPurposeItem(nAllPurpose);
									m_assignmentSupplementItemService.saveAllPurposeItemAccess(access);
									accessSet.add(access);
								}
							}
						}
					}
				}
				catch (Exception e)
				{
					M_log.warn(this + ":post_save_assignment " + e.toString() + "error finding authzGroup for = " + siteId);
				}
				nAllPurpose.setAccessSet(accessSet);
			}
			m_assignmentSupplementItemService.saveAllPurposeItem(nAllPurpose);
		}
	}


	private Set<AssignmentSupplementItemAttachment> getAssignmentSupplementItemAttachment(SessionState state, AssignmentSupplementItemWithAttachment mItem, String attachmentString) {
		Set<AssignmentSupplementItemAttachment> sAttachments = new HashSet<AssignmentSupplementItemAttachment>();
		List<String> attIdList = m_assignmentSupplementItemService.getAttachmentListForSupplementItem(mItem);
		if (state.getAttribute(attachmentString) != null)
		{
			List currentAttachments = (List) state.getAttribute(attachmentString);
			for (Iterator aIterator = currentAttachments.iterator(); aIterator.hasNext();)
			{
				Reference attRef = (Reference) aIterator.next();
				String attRefId = attRef.getReference();
				// if the attachment is not exist, add it into db
				if (!attIdList.contains(attRefId))
				{
					AssignmentSupplementItemAttachment mAttach = m_assignmentSupplementItemService.newAttachment();
					mAttach.setAssignmentSupplementItemWithAttachment(mItem);
					mAttach.setAttachmentId(attRefId);
					m_assignmentSupplementItemService.saveAttachment(mAttach);
					sAttachments.add(mAttach);
				}
			}
		}
		return sAttachments;
	}

	/**
	 * 
	 */
	private boolean change_from_non_point(SessionState state, String assignmentId, String assignmentContentId, AssignmentContentEdit ac) 
	{
		// whether this is an editing which changes non point_grade type to point grade type?
		if (StringUtils.trimToNull(assignmentId) != null && StringUtils.trimToNull(assignmentContentId) != null)
		{
			// editing
			if (ac.getTypeOfGrade() != Assignment.SCORE_GRADE_TYPE
					&& ((Integer) state.getAttribute(NEW_ASSIGNMENT_GRADE_TYPE)).intValue() == Assignment.SCORE_GRADE_TYPE)
			{
				// changing from non-point grade type to point grade type?
				return true;
			}
		}
		return false;
	}
	
	/**
	 * whether the resubmit option has been changed
	 * @param state
	 * @param a
	 * @return
	 */
	private boolean change_resubmit_option(SessionState state, Entity entity) 
	{
		if (entity != null)
		{
			// editing
			return propertyValueChanged(state, entity, AssignmentSubmission.ALLOW_RESUBMIT_NUMBER) || propertyValueChanged(state, entity, AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME);
		}
		return false;
	}

	/**
	 * whether there is a change between state variable and object's property value
	 * @param state
	 * @param entity
	 * @param propertyName
	 * @return
	 */
	private boolean propertyValueChanged(SessionState state, Entity entity, String propertyName) {
		String o_property_value = entity.getProperties().getProperty(propertyName);
		String n_property_value = state.getAttribute(propertyName) != null? (String) state.getAttribute(propertyName):null;
		if (o_property_value == null && n_property_value != null
			|| o_property_value != null && n_property_value == null
			|| o_property_value != null && n_property_value != null && !o_property_value.equals(n_property_value))
		{
			// there is a change
			return true;
		}
		return false;
	}

	/**
	 * default sorting
	 */
	private void setDefaultSort(SessionState state) {
		state.setAttribute(SORTED_BY, SORTED_BY_DEFAULT);
		state.setAttribute(SORTED_ASC, Boolean.TRUE.toString());
	}

	/**
	 * Add submission objects if necessary for non-electronic type of assignment
	 * @param state
	 * @param a
	 */
	private void addRemoveSubmissionsForNonElectronicAssignment(SessionState state, List submissions, HashSet<String> addSubmissionForUsers, HashSet<String> removeSubmissionForUsers, Assignment a) 
	{
		// create submission object for those user who doesn't have one yet
		for (Iterator iUserIds = addSubmissionForUsers.iterator(); iUserIds.hasNext();)
		{
			String userId = (String) iUserIds.next();
			try
			{
				User u = UserDirectoryService.getUser(userId);
				// only include those users that can submit to this assignment
				if (u != null)
				{
					// construct fake submissions for grading purpose
					AssignmentSubmissionEdit submission = AssignmentService.addSubmission(a.getContext(), a.getId(), userId);
					if (submission != null)
					{
						submission.setTimeSubmitted(TimeService.newTime());
						submission.setSubmitted(true);
						submission.setIsUserSubmission(false);
						submission.setAssignment(a);
						AssignmentService.commitEdit(submission);
					}
				}
			}
			catch (Exception e)
			{
				M_log.warn(this + ":addRemoveSubmissionsForNonElectronicAssignment " + e.toString() + "error adding submission for userId = " + userId);
			}
		}
		
		// remove submission object for those who no longer in the site
		for (Iterator iUserIds = removeSubmissionForUsers.iterator(); iUserIds.hasNext();)
		{
			String userId = (String) iUserIds.next();
			String submissionRef = null;
			// TODO: we don't have an efficient way to retrieve specific user's submission now, so until then, we still need to iterate the whole submission list
			for (Iterator iSubmissions=submissions.iterator(); iSubmissions.hasNext() && submissionRef == null;)
			{
				AssignmentSubmission submission = (AssignmentSubmission) iSubmissions.next();
                                if (userId.equals(submission.getSubmitterId())) {
                                    submissionRef = submission.getReference();
                                }
			}
			if (submissionRef != null)
			{
				AssignmentSubmissionEdit submissionEdit = editSubmission(submissionRef, "addRemoveSubmissionsForNonElectronicAssignment", state);
				if (submissionEdit != null)
				{
					try
					{
						AssignmentService.removeSubmission(submissionEdit);
					}
					catch (PermissionException e)
					{
						addAlert(state, rb.getFormattedMessage("youarenot_removeSubmission", new Object[]{submissionEdit.getReference()}));
						M_log.warn(this + ":deleteAssignmentObjects " + e.getMessage() + " " + submissionEdit.getReference());
					}
				}
			}
		}
		
	}
	private void initIntegrateWithGradebook(SessionState state, String siteId, String aOldTitle, String oAssociateGradebookAssignment, AssignmentEdit a, String title, Time dueTime, int gradeType, String gradePoints, String addtoGradebook, String associateGradebookAssignment, String range, long category) {
	
		GradebookExternalAssessmentService gExternal = (GradebookExternalAssessmentService) ComponentManager.get("org.sakaiproject.service.gradebook.GradebookExternalAssessmentService");
		
		String context = (String) state.getAttribute(STATE_CONTEXT_STRING);
		boolean gradebookExists = isGradebookDefined();

		// only if the gradebook is defined
		if (gradebookExists)
		{
			GradebookService g = (GradebookService)  ComponentManager.get("org.sakaiproject.service.gradebook.GradebookService");
			String gradebookUid = ToolManager.getCurrentPlacement().getContext();
			
			String aReference = a.getReference();
			String addUpdateRemoveAssignment = "remove";
			if (!addtoGradebook.equals(AssignmentService.GRADEBOOK_INTEGRATION_NO))
			{
				// if integrate with Gradebook
				if (!AssignmentService.getAllowGroupAssignmentsInGradebook() && ("groups".equals(range)))
				{
					// if grouped assignment is not allowed to add into Gradebook
					addAlert(state, rb.getString("java.alert.noGroupedAssignmentIntoGB"));
					String ref = a.getReference();
					
					AssignmentEdit aEdit = editAssignment(a.getReference(), "initINtegrateWithGradebook", state, false);
					if (aEdit != null)
					{
						aEdit.getPropertiesEdit().removeProperty(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK);
						aEdit.getPropertiesEdit().removeProperty(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
						AssignmentService.commitEdit(aEdit);
					}
					integrateGradebook(state, aReference, associateGradebookAssignment, "remove", null, null, -1, null, null, null, category);
				}
				else
				{
					if (addtoGradebook.equals(AssignmentService.GRADEBOOK_INTEGRATION_ADD))
					{
						addUpdateRemoveAssignment = AssignmentService.GRADEBOOK_INTEGRATION_ADD;
					}
					else if (addtoGradebook.equals(AssignmentService.GRADEBOOK_INTEGRATION_ASSOCIATE))
					{
						addUpdateRemoveAssignment = "update";
					}
	
					if (!"remove".equals(addUpdateRemoveAssignment) && gradeType == 3)
					{
						try
						{
							integrateGradebook(state, aReference, associateGradebookAssignment, addUpdateRemoveAssignment, aOldTitle, title, Integer.parseInt (gradePoints), dueTime, null, null, category);
	
							// add all existing grades, if any, into Gradebook
							integrateGradebook(state, aReference, associateGradebookAssignment, null, null, null, -1, null, null, "update", category);
	
							// if the assignment has been assoicated with a different entry in gradebook before, remove those grades from the entry in Gradebook
							if (StringUtils.trimToNull(oAssociateGradebookAssignment) != null && !oAssociateGradebookAssignment.equals(associateGradebookAssignment))
							{
								// remove all previously associated grades, if any, into Gradebook
								integrateGradebook(state, aReference, oAssociateGradebookAssignment, null, null, null, -1, null, null, "remove", category);
								
								// if the old assoicated assignment entry in GB is an external one, but doesn't have anything assoicated with it in Assignment tool, remove it
								removeNonAssociatedExternalGradebookEntry(context, a.getReference(), oAssociateGradebookAssignment,gExternal, gradebookUid);
							}
						}
						catch (NumberFormatException nE)
						{
							alertInvalidPoint(state, gradePoints, a.getContent().getFactor());
							M_log.warn(this + ":initIntegrateWithGradebook " + nE.getMessage()); 
						}
					}
					else
					{
						integrateGradebook(state, aReference, associateGradebookAssignment, "remove", null, null, -1, null, null, null, category);
					}
				}
			}
			else
			{
				// remove all previously associated grades, if any, into Gradebook
				integrateGradebook(state, aReference, oAssociateGradebookAssignment, null, null, null, -1, null, null, "remove", category);
				
				// need to remove the associated gradebook entry if 1) it is external and 2) no other assignment are associated with it
				removeNonAssociatedExternalGradebookEntry(context, a.getReference(), oAssociateGradebookAssignment,gExternal, gradebookUid);
			}
		}
	}

	private void removeNonAssociatedExternalGradebookEntry(String context, String assignmentReference, String associateGradebookAssignment, GradebookExternalAssessmentService gExternal, String gradebookUid) {
		boolean isExternalAssignmentDefined=gExternal.isExternalAssignmentDefined(gradebookUid, associateGradebookAssignment);
		if (isExternalAssignmentDefined)
		{
			// iterate through all assignments currently in the site, see if any is associated with this GB entry
			Iterator i = AssignmentService.getAssignmentsForContext(context);
			boolean found = false;
			while (!found && i.hasNext())
			{
				Assignment aI = (Assignment) i.next();
				String gbEntry = aI.getProperties().getProperty(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
				if (aI.getProperties().getProperty(ResourceProperties.PROP_ASSIGNMENT_DELETED) == null && gbEntry != null && gbEntry.equals(associateGradebookAssignment) && !aI.getReference().equals(assignmentReference))
				{
					found = true;
				}
			}
			// so if none of the assignment in this site is associated with the entry, remove the entry
			if (!found)
			{
				gExternal.removeExternalAssessment(gradebookUid, associateGradebookAssignment);
			}
		}
	}

	private void integrateWithAnnouncement(SessionState state, String aOldTitle, AssignmentEdit a, String title, Time openTime, String checkAutoAnnounce, String valueOpenDateNotification, Time oldOpenTime) 
	{
		if (checkAutoAnnounce.equalsIgnoreCase(Boolean.TRUE.toString()))
		{
			AnnouncementChannel channel = (AnnouncementChannel) state.getAttribute(ANNOUNCEMENT_CHANNEL);
			if (channel != null)
			{	
				// whether the assignment's title or open date has been updated
				boolean updatedTitle = false;
				boolean updatedOpenDate = false;
				boolean updateAccess = false;
				
				String openDateAnnounced = StringUtils.trimToNull(a.getProperties().getProperty(NEW_ASSIGNMENT_OPEN_DATE_ANNOUNCED));
				String openDateAnnouncementId = StringUtils.trimToNull(a.getPropertiesEdit().getProperty(ResourceProperties.PROP_ASSIGNMENT_OPENDATE_ANNOUNCEMENT_MESSAGE_ID));
				if (openDateAnnounced != null && openDateAnnouncementId != null)
				{
					AnnouncementMessage message = null;
					
					try
					{
						message = channel.getAnnouncementMessage(openDateAnnouncementId);
						if (!message.getAnnouncementHeader().getSubject().contains(title))/*whether title has been changed*/
						{
							updatedTitle = true;
						}
						if (!message.getBody().contains(openTime.toStringLocalFull())) /*whether open date has been changed*/
						{
							updatedOpenDate = true;
						}
						if ((message.getAnnouncementHeader().getAccess().equals(MessageHeader.MessageAccess.CHANNEL) && !a.getAccess().equals(AssignmentAccess.SITE))
							|| (!message.getAnnouncementHeader().getAccess().equals(MessageHeader.MessageAccess.CHANNEL) && a.getAccess().equals(AssignmentAccess.SITE)))
						{
							updateAccess = true;
						}
						else if (a.getAccess() == Assignment.AssignmentAccess.GROUPED)
						{
							Collection<String> assnGroups = a.getGroups();
							Collection<String> anncGroups = message.getAnnouncementHeader().getGroups();
							if (!assnGroups.equals(anncGroups))
							{
								updateAccess = true;
							}
						}
					}
					catch (IdUnusedException e)
					{
						M_log.warn(this + ":integrateWithAnnouncement " + e.getMessage());
					}
					catch (PermissionException e)
					{
						M_log.warn(this + ":integrateWithAnnouncement " + e.getMessage());
					}
					
					if (updateAccess  && message != null)
					{
						try
						{
							// if the access level has changed in assignment, remove the original announcement
							channel.removeAnnouncementMessage(message.getId());
						}
						catch (PermissionException e)
						{
							M_log.warn(this + ":integrateWithAnnouncement PermissionException for remove message id=" + message.getId() + " for assignment id=" + a.getId() + " " + e.getMessage());
						}
					}
				}

				// need to create announcement message if assignment is added or assignment has been updated
				if (openDateAnnounced == null || updatedTitle || updatedOpenDate || updateAccess)
				{
					try
					{
						AnnouncementMessageEdit message = channel.addAnnouncementMessage();
						if (message != null)
						{
							AnnouncementMessageHeaderEdit header = message.getAnnouncementHeaderEdit();
							
							// add assignment id into property, to facilitate assignment lookup in Annoucement tool
							message.getPropertiesEdit().addProperty("assignmentReference", a.getReference());
							
							header.setDraft(/* draft */false);
							header.replaceAttachments(/* attachment */EntityManager.newReferenceList());
		
							if (openDateAnnounced == null)
							{
								// making new announcement
								header.setSubject(/* subject */rb.getFormattedMessage("assig6", new Object[]{title}));
							}
							else
							{
								// updated title
								header.setSubject(/* subject */rb.getFormattedMessage("assig5", new Object[]{title}));
							}
							
							if (updatedOpenDate)
							{
								// revised assignment open date
								message.setBody(/* body */rb.getFormattedMessage("newope", new Object[]{FormattedText.convertPlaintextToFormattedText(title), openTime.toStringLocalFull()}));
							}
							else
							{
								// assignment open date
								message.setBody(/* body */rb.getFormattedMessage("opedat", new Object[]{FormattedText.convertPlaintextToFormattedText(title), openTime.toStringLocalFull()}));
							}
		
							// group information
							if (a.getAccess().equals(Assignment.AssignmentAccess.GROUPED))
							{
								try
								{
									// get the group ids selected
									Collection groupRefs = a.getGroups();
		
									// make a collection of Group objects
									Collection groups = new ArrayList();
		
									//make a collection of Group objects from the collection of group ref strings
									Site site = SiteService.getSite((String) state.getAttribute(STATE_CONTEXT_STRING));
									for (Iterator iGroupRefs = groupRefs.iterator(); iGroupRefs.hasNext();)
									{
										String groupRef = (String) iGroupRefs.next();
										groups.add(site.getGroup(groupRef));
									}
		
									// set access
									header.setGroupAccess(groups);
								}
								catch (Exception exception)
								{
									// log
									M_log.warn(this + ":integrateWithAnnouncement " + exception.getMessage());
								}
							}
							else
							{
								// site announcement
								header.clearGroupAccess();
							}
		
							// save notification level if this is a future notification message
							int notiLevel = NotificationService.NOTI_NONE;
							String notification = "n";
							if (Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION_LOW.equals(valueOpenDateNotification)) {
								notiLevel = NotificationService.NOTI_OPTIONAL;
								notification = "o"; 
							}
							else if (Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION_HIGH.equals(valueOpenDateNotification)) {
								notiLevel = NotificationService.NOTI_REQUIRED;
								notification = "r";
							}
								
							Time now = TimeService.newTime();
							if (openDateAnnounced != null && now.before(oldOpenTime))
							{
								message.getPropertiesEdit().addProperty("notificationLevel", notification);
								message.getPropertiesEdit().addPropertyToList("noti_history", now.toStringLocalFull()+"_"+notiLevel+"_"+openDateAnnounced);
							}
							else {
								message.getPropertiesEdit().addPropertyToList("noti_history", now.toStringLocalFull()+"_"+notiLevel);
							}
							
							channel.commitMessage(message, notiLevel, "org.sakaiproject.announcement.impl.SiteEmailNotificationAnnc");
						}
	
						// commit related properties into Assignment object
						AssignmentEdit aEdit = editAssignment(a.getReference(), "integrateWithAnnouncement", state, false);
						if (aEdit != null)
						{
							aEdit.getPropertiesEdit().addProperty(NEW_ASSIGNMENT_OPEN_DATE_ANNOUNCED, Boolean.TRUE.toString());
							if (message != null)
							{
								aEdit.getPropertiesEdit().addProperty(ResourceProperties.PROP_ASSIGNMENT_OPENDATE_ANNOUNCEMENT_MESSAGE_ID, message.getId());
							}
							AssignmentService.commitEdit(aEdit);
						}
	
					}
					catch (PermissionException ee)
					{
						M_log.warn(this + ":IntegrateWithAnnouncement " + rb.getString("cannotmak"));
					}
				}
			}
		} // if
	}

	private void integrateWithCalendar(SessionState state, AssignmentEdit a, String title, Time dueTime, String checkAddDueTime, Time oldDueTime, ResourcePropertiesEdit aPropertiesEdit) 
	{
		// Integrate with Sakai calendar tool
		Calendar c = (Calendar) state.getAttribute(CALENDAR);
		
		integrateWithCalendarTool(state, a, title, dueTime, checkAddDueTime,
				oldDueTime, aPropertiesEdit, c, ResourceProperties.PROP_ASSIGNMENT_DUEDATE_CALENDAR_EVENT_ID);
		
		// Integrate with additional calendar tool if deployed.
		Calendar additionalCal = (Calendar) state.getAttribute(ADDITIONAL_CALENDAR); 
		
		if (additionalCal != null){
			integrateWithCalendarTool(state, a, title, dueTime, checkAddDueTime,
					oldDueTime, aPropertiesEdit, additionalCal, ResourceProperties.PROP_ASSIGNMENT_DUEDATE_ADDITIONAL_CALENDAR_EVENT_ID);
		}
	}


	// Checks to see if due date event in assignment properties exists on the calendar. 
	// If so, remove it and then add a new due date event to the calendar. Then update assignment property
	// with new event id.
	private void integrateWithCalendarTool(SessionState state, AssignmentEdit a, String title, Time dueTime, String checkAddDueTime, Time oldDueTime, ResourcePropertiesEdit aPropertiesEdit, Calendar c, String dueDateProperty) {
		if (c == null){
			return;
		}
		String dueDateScheduled = a.getProperties().getProperty(NEW_ASSIGNMENT_DUE_DATE_SCHEDULED);
		String oldEventId = aPropertiesEdit.getProperty(dueDateProperty);
		CalendarEvent e = null;

		if (dueDateScheduled != null || oldEventId != null)
		{
			// find the old event
			boolean found = false;
			if (oldEventId != null)
			{
				try
				{
					e = c.getEvent(oldEventId);
					found = true;
				}
				catch (IdUnusedException ee)
				{
					M_log.warn(this + ":integrateWithCalendarTool The old event has been deleted: event id=" + oldEventId + ". " + c.getClass().getName());
				}
				catch (PermissionException ee)
				{
					M_log.warn(this + ":integrateWithCalendarTool You do not have the permission to view the schedule event id= "
							+ oldEventId + ". " + c.getClass().getName());
				}
			}
			else
			{
				TimeBreakdown b = oldDueTime.breakdownLocal();
				// TODO: check- this was new Time(year...), not local! -ggolden
				Time startTime = TimeService.newTimeLocal(b.getYear(), b.getMonth(), b.getDay(), 0, 0, 0, 0);
				Time endTime = TimeService.newTimeLocal(b.getYear(), b.getMonth(), b.getDay(), 23, 59, 59, 999);
				try
				{
					Iterator events = c.getEvents(TimeService.newTimeRange(startTime, endTime), null)
							.iterator();

					while ((!found) && (events.hasNext()))
					{
						e = (CalendarEvent) events.next();
						if (((String) e.getDisplayName()).indexOf(rb.getString("gen.assig") + " " + title) != -1)
						{
							found = true;
						}
					}
				}
				catch (PermissionException ignore)
				{
					// ignore PermissionException
				}
			}
			if (found){
				removeOldEvent(title, c, e);
			}
			
		}

		if (checkAddDueTime.equalsIgnoreCase(Boolean.TRUE.toString()))
		{
			updateAssignmentWithEventId(state, a, title, dueTime, c,
					dueDateProperty);
		}
	}


	/**
	 * Add event to calendar and then persist the event id to the assignment properties 
	 * @param state
	 * @param a AssignmentEdit
	 * @param title Event title
	 * @param dueTime Assignment due date/time
	 * @param c Calendar
	 * @param dueDateProperty Property name specifies the appropriate calendar
	 */
	private void updateAssignmentWithEventId(SessionState state, AssignmentEdit a, String title, Time dueTime, Calendar c,
			String dueDateProperty) {
		CalendarEvent e;
		// commit related properties into Assignment object
		AssignmentEdit aEdit = editAssignment(a.getReference(), "updateAssignmentWithEventId", state, false);
		if (aEdit != null)
		{
			try
			{
				e = null;
				CalendarEvent.EventAccess eAccess = CalendarEvent.EventAccess.SITE;
				Collection eGroups = new ArrayList();

				if (aEdit.getAccess().equals(Assignment.AssignmentAccess.GROUPED))
				{
					eAccess = CalendarEvent.EventAccess.GROUPED;
					Collection groupRefs = aEdit.getGroups();

					// make a collection of Group objects from the collection of group ref strings
					Site site = SiteService.getSite((String) state.getAttribute(STATE_CONTEXT_STRING));
					for (Iterator iGroupRefs = groupRefs.iterator(); iGroupRefs.hasNext();)
					{
						String groupRef = (String) iGroupRefs.next();
		                                                Group _aGroup = site.getGroup(groupRef);
		                                                if (_aGroup != null) eGroups.add(_aGroup);
					}
				}
				e = c.addEvent(/* TimeRange */TimeService.newTimeRange(dueTime.getTime(), /* 0 duration */0 * 60 * 1000),
						/* title */rb.getString("gen.due") + " " + title,
						/* description */rb.getFormattedMessage("assign_due_event_desc", new Object[]{title, dueTime.toStringLocalFull()}),
						/* type */rb.getString("deadl"),
						/* location */"",
						/* access */ eAccess,
						/* groups */ eGroups,
						/* attachments */null /*SAK-27919 do not include assignment attachments.*/);

				aEdit.getProperties().addProperty(NEW_ASSIGNMENT_DUE_DATE_SCHEDULED, Boolean.TRUE.toString());
				if (e != null)
				{
					aEdit.getProperties().addProperty(dueDateProperty, e.getId());

		             // edit the calendar object and add an assignment id field
		            addAssignmentIdToCalendar(a, c, e);
				}
				// TODO do we care if the event is null?
				
			}
			catch (IdUnusedException ee)
			{
				M_log.warn(this + ":updateAssignmentWithEventId " + ee.getMessage());
			}
			catch (PermissionException ee)
			{
				M_log.warn(this + ":updateAssignmentWithEventId " + rb.getString("cannotfin1"));
			}
			catch (Exception ee)
			{
				M_log.warn(this + ":updateAssignmentWithEventId " + ee.getMessage());
			}
			// try-catch


			AssignmentService.commitEdit(aEdit);
		}
	}

	// Persist the assignment id to the calendar
	private void addAssignmentIdToCalendar(AssignmentEdit a, Calendar c, CalendarEvent e) throws IdUnusedException, PermissionException,InUseException {
		
		if (c!= null && e != null && a != null){
			CalendarEventEdit edit = c.getEditEvent(e.getId(), org.sakaiproject.calendar.api.CalendarService.EVENT_ADD_CALENDAR);
	        
			edit.setField(AssignmentConstants.NEW_ASSIGNMENT_DUEDATE_CALENDAR_ASSIGNMENT_ID, a.getId());
			
			c.commitEvent(edit);
		}
	}

	// Remove an existing event from the calendar
	private void removeOldEvent(String title, Calendar c, CalendarEvent e) {
		// remove the found old event
		if (c != null && e != null){
			try
			{
				c.removeEvent(c.getEditEvent(e.getId(), CalendarService.EVENT_REMOVE_CALENDAR));
			}
			catch (PermissionException ee)
			{
				M_log.warn(this + ":removeOldEvent " + rb.getFormattedMessage("cannotrem", new Object[]{title}));
			}
			catch (InUseException ee)
			{
				M_log.warn(this + ":removeOldEvent " + rb.getString("somelsis_calendar"));
			}
			catch (IdUnusedException ee)
			{
				M_log.warn(this + ":removeOldEvent " + rb.getFormattedMessage("cannotfin6", new Object[]{e.getId()}));
			}
		}
	}

	private void commitAssignmentEdit(SessionState state, boolean post, AssignmentContentEdit ac, AssignmentEdit a, String title, Time visibleTime, Time openTime, Time dueTime, Time closeTime, boolean enableCloseDate, String s, String range, Collection groups, boolean isGroupSubmit,
			boolean usePeerAssessment, Time peerPeriodTime, boolean peerAssessmentAnonEval, boolean peerAssessmentStudentViewReviews, int peerAssessmentNumReviews, String peerAssessmentInstructions) 
	{
		a.setTitle(title);
		a.setContent(ac);
		a.setContext((String) state.getAttribute(STATE_CONTEXT_STRING));
		a.setSection(s);
		a.setVisibleTime(visibleTime);
		a.setOpenTime(openTime);
		a.setDueTime(dueTime);
                a.setGroup(isGroupSubmit);
		a.setDropDeadTime(dueTime);
		if (enableCloseDate)
		{
			a.setCloseTime(closeTime);
		}
		else
		{
			// if editing an old assignment with close date
			if (a.getCloseTime() != null)
			{
				a.setCloseTime(null);
			}
		}

		if (Boolean.TRUE.equals(state.getAttribute("contentReviewSuccess")))
		{
			// post the assignment if appropriate
			a.setDraft(!post);
		}
		else
		{
			// setup for content review failed, save as a draft
			a.setDraft(true);
		}

		a.setAllowPeerAssessment(usePeerAssessment);
		a.setPeerAssessmentPeriod(peerPeriodTime);
		a.setPeerAssessmentAnonEval(peerAssessmentAnonEval);
		a.setPeerAssessmentStudentViewReviews(peerAssessmentStudentViewReviews);
		a.setPeerAssessmentNumReviews(peerAssessmentNumReviews);
		a.setPeerAssessmentInstructions(peerAssessmentInstructions);

		try
		{
			// SAK-26349 - clear group selection before changing, otherwise it can result in a PermissionException
			a.clearGroupAccess();

			if ("site".equals(range))
			{
				a.setAccess(Assignment.AssignmentAccess.SITE);
			}
			else if ("groups".equals(range))
			{
				a.setGroupAccess(groups);
			}
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getString("youarenot_addAssignmentContent"));
			M_log.warn(this + ":commitAssignmentEdit " + rb.getString("youarenot_addAssignmentContent") + e.getMessage());
		}

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			// commit assignment first
			AssignmentService.commitEdit(a);
		}
                
                if (a.isGroup()) {
                    Collection<String> _dupUsers = usersInMultipleGroups(a);
                    if (_dupUsers.size() > 0) { 
                        addAlert(state, rb.getString("group.user.multiple.error"));
                        M_log.warn(this + ":post_save_assignment at least one user in multiple groups.");
	}
                }
	}

	private void editAssignmentProperties(AssignmentEdit a, String checkAddDueTime, String checkAutoAnnounce, String addtoGradebook, String associateGradebookAssignment, String allowResubmitNumber, ResourcePropertiesEdit aPropertiesEdit, boolean post, Time closeTime, String checkAnonymousGrading)
	{
		if (aPropertiesEdit.getProperty("newAssignment") != null)
		{
			if (aPropertiesEdit.getProperty("newAssignment").equalsIgnoreCase(Boolean.TRUE.toString()))
			{
				// not a newly created assignment, been added.
				aPropertiesEdit.addProperty("newAssignment", Boolean.FALSE.toString());
			}
		}
		else
		{
			// for newly created assignment
			aPropertiesEdit.addProperty("newAssignment", Boolean.TRUE.toString());
		}
		if (checkAddDueTime != null)
		{
			aPropertiesEdit.addProperty(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE, checkAddDueTime);
		}
		else
		{
			aPropertiesEdit.removeProperty(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE);
		}
		aPropertiesEdit.addProperty(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE, checkAutoAnnounce);
		
		aPropertiesEdit.addProperty(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, addtoGradebook);
		aPropertiesEdit.addProperty(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT, associateGradebookAssignment);

		// SAK-17606
		aPropertiesEdit.addProperty(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING, checkAnonymousGrading);

		if (post && addtoGradebook.equals(AssignmentService.GRADEBOOK_INTEGRATION_ADD))
		{
			// if the choice is to add an entry into Gradebook, let just mark it as associated with such new entry then
			aPropertiesEdit.addProperty(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, AssignmentService.GRADEBOOK_INTEGRATION_ASSOCIATE);
			aPropertiesEdit.addProperty(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT, a.getReference());

		}
		
		// allow resubmit number and default assignment resubmit closeTime (dueTime)
		if (allowResubmitNumber != null && closeTime != null)
		{
			aPropertiesEdit.addProperty(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER, allowResubmitNumber);
			aPropertiesEdit.addProperty(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME, String.valueOf(closeTime.getTime()));
		}
		else if (allowResubmitNumber == null || allowResubmitNumber.length() == 0 || "0".equals(allowResubmitNumber))	
		{
			aPropertiesEdit.removeProperty(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER);
			aPropertiesEdit.removeProperty(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME);
		}
	}

	private void commitAssignmentContentEdit(SessionState state, AssignmentContentEdit ac, String assignmentRef, String title, int submissionType,boolean useReviewService, boolean allowStudentViewReport, int gradeType, String gradePoints, String description, String checkAddHonorPledge, List attachments, String submitReviewRepo, String generateOriginalityReport, boolean checkTurnitin, boolean checkInternet, boolean checkPublications, boolean checkInstitution, boolean excludeBibliographic, boolean excludeQuoted, int excludeType, int excludeValue, Time openTime, Time dueTime, Time closeTime, boolean hideDueDate) 
	{
		ac.setTitle(title);
		ac.setInstructions(description);
		ac.setHonorPledge(Integer.parseInt(checkAddHonorPledge));
		ac.setHideDueDate(hideDueDate);
		ac.setTypeOfSubmission(submissionType);
		ac.setAllowReviewService(useReviewService);
		ac.setAllowStudentViewReport(allowStudentViewReport);
		ac.setSubmitReviewRepo(submitReviewRepo);
		ac.setGenerateOriginalityReport(generateOriginalityReport);
		ac.setCheckInstitution(checkInstitution);
		ac.setCheckInternet(checkInternet);
		ac.setCheckPublications(checkPublications);
		ac.setCheckTurnitin(checkTurnitin);
		ac.setExcludeBibliographic(excludeBibliographic);
		ac.setExcludeQuoted(excludeQuoted);
		ac.setExcludeType(excludeType);
		ac.setExcludeValue(excludeValue);
		ac.setTypeOfGrade(gradeType);
		if (gradeType == 3)
		{
			try
			{
				ac.setMaxGradePoint(Integer.parseInt(gradePoints));
			}
			catch (NumberFormatException e)
			{
				alertInvalidPoint(state, gradePoints, ac.getFactor());
				M_log.warn(this + ":commitAssignmentContentEdit " + e.getMessage());
			}
		}
		ac.setGroupProject(true);
		ac.setIndividuallyGraded(false);

		if (submissionType != 1)
		{
			ac.setAllowAttachments(true);
		}
		else
		{
			ac.setAllowAttachments(false);
		}

		// clear attachments
		ac.clearAttachments();

		if (attachments != null)
		{
			// add each attachment
			Iterator it = EntityManager.newReferenceList(attachments).iterator();
			while (it.hasNext())
			{
				Reference r = (Reference) it.next();
				ac.addAttachment(r);
			}
		}
		state.setAttribute(ATTACHMENTS_MODIFIED, Boolean.valueOf(false));
		
		// commit the changes
		AssignmentService.commitEdit(ac);
		
		if(ac.getAllowReviewService()){
			if (!createTIIAssignment(ac, assignmentRef, openTime, dueTime, closeTime, state))
			{
				state.setAttribute("contentReviewSuccess", Boolean.FALSE);
			}
		}
		
	}
	
	public boolean createTIIAssignment(AssignmentContentEdit assign, String assignmentRef, Time openTime, Time dueTime, Time closeTime, SessionState state) {
        Map opts = new HashMap();
        
        opts.put("submit_papers_to", assign.getSubmitReviewRepo());
        opts.put("report_gen_speed", assign.getGenerateOriginalityReport());
        opts.put("institution_check", assign.isCheckInstitution() ? "1" : "0");
        opts.put("internet_check", assign.isCheckInternet() ? "1" : "0");
        opts.put("journal_check", assign.isCheckPublications() ? "1" : "0");
        opts.put("s_paper_check", assign.isCheckTurnitin() ? "1" : "0");        
        opts.put("s_view_report", assign.getAllowStudentViewReport() ? "1" : "0");        
        if(ServerConfigurationService.getBoolean("turnitin.option.exclude_bibliographic", true)){
			//we don't want to pass parameters if the user didn't get an option to set it
        	opts.put("exclude_biblio", assign.isExcludeBibliographic() ? "1" : "0");
		}
		if(ServerConfigurationService.getBoolean("turnitin.option.exclude_quoted", true)){
			//we don't want to pass parameters if the user didn't get an option to set it
			opts.put("exclude_quoted", assign.isExcludeQuoted() ? "1" : "0");
		}
        
        if((assign.getExcludeType() == 1 || assign.getExcludeType() == 2) 
        		&& assign.getExcludeValue() >= 0 && assign.getExcludeValue() <= 100){
        	opts.put("exclude_type", Integer.toString(assign.getExcludeType()));
        	opts.put("exclude_value", Integer.toString(assign.getExcludeValue()));
        }
        opts.put("late_accept_flag", "1");
        
        SimpleDateFormat dform = ((SimpleDateFormat) DateFormat.getDateInstance());
        dform.applyPattern("yyyy-MM-dd HH:mm:ss");
        opts.put("dtstart", dform.format(openTime.getTime()));
        opts.put("dtdue", dform.format(dueTime.getTime()));
        //opts.put("dtpost", dform.format(closeTime.getTime()));
        opts.put("points", assign.getMaxGradePoint());
        opts.put("title", assign.getTitle());
        opts.put("instructions", assign.getInstructions());
        if(assign.getAttachments() != null && assign.getAttachments().size() > 0){
        	List<String> attachments = new ArrayList<String>();
        	for(Reference ref : assign.getAttachments()){
        		attachments.add(ref.getReference());
        	}
        	opts.put("attachments", attachments);
        }
        try {
            contentReviewService.createAssignment(assign.getContext(), assignmentRef, opts);
			return true;
        } catch (Exception e) {
            M_log.error(e.getMessage());
			String uiService = ServerConfigurationService.getString("ui.service", "Sakai");
			String[] args = new String[]{contentReviewService.getServiceName(), uiService, e.toString()};
            state.setAttribute("alertMessage", rb.getFormattedMessage("content_review.error.createAssignment", args));
        }
		return false;
    }
	

	/**
	 * reorderAssignments
	 */
	private void reorderAssignments(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();
		
		List assignments = prepPage(state);
		
		Iterator it = assignments.iterator();
        
        while (it.hasNext()) // reads and writes the parameter for default ordering
        {
            Assignment a = (Assignment) it.next();
            String assignmentid = a.getId();
            String assignmentposition = params.getString("position_" + assignmentid);
            SecurityAdvisor sa = new SecurityAdvisor() {
    			public SecurityAdvice isAllowed(String userId, String function, String reference)
    			{
    				return function.equals(AssignmentService.SECURE_UPDATE_ASSIGNMENT)?SecurityAdvice.ALLOWED:SecurityAdvice.PASS;
    			}
    		};
            try
            {
        		// put in a security advisor so we can create citationAdmin site without need
        		// of further permissions
            	m_securityService.pushAdvisor(sa);
	            AssignmentEdit ae = editAssignment(assignmentid, "reorderAssignments", state, true);
	            if (ae != null)
	            {
		            ae.setPosition_order(Long.valueOf(assignmentposition).intValue());
		            AssignmentService.commitEdit(ae);
	            }
            }
            catch (Exception e)
            {
            	M_log.warn(this + ":reorderAssignments : not able to edit assignment " + assignmentid + e.toString());
            }
            finally
            {
            	// remove advisor
            	m_securityService.popAdvisor(sa);
            }
        }
		
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
			state.setAttribute(ATTACHMENTS, EntityManager.newReferenceList());
		}
	} // reorderAssignments

	private AssignmentContentEdit editAssignmentContent(String assignmentContentId, String callingFunctionName, SessionState state, boolean allowAdd)
	{
		AssignmentContentEdit ac = null;
		if (assignmentContentId.length() == 0 && allowAdd)
		{
			// new assignment
			try
			{
				ac = AssignmentService.addAssignmentContent((String) state.getAttribute(STATE_CONTEXT_STRING));
			}
			catch (PermissionException e)
			{
				addAlert(state, rb.getString("youarenot_addAssignmentContent"));
				M_log.warn(this + ":" + callingFunctionName + " " + e.getMessage() + (String) state.getAttribute(STATE_CONTEXT_STRING));
			}
		}
		else
		{
			try
			{
				// edit assignment
				ac = AssignmentService.editAssignmentContent(assignmentContentId);
			}
			catch (InUseException e)
			{
				addAlert(state, rb.getFormattedMessage("somelsis_assignmentContent", new Object[]{assignmentContentId}));
				M_log.warn(this + ":" + callingFunctionName + " " + e.getMessage());
			}
			catch (IdUnusedException e)
			{
				addAlert(state, rb.getFormattedMessage("cannotfin_assignmentContent", new Object[]{assignmentContentId}));
				M_log.warn(this + ":" + callingFunctionName + " " + e.getMessage());
			}
			catch (PermissionException e)
			{
				addAlert(state, rb.getFormattedMessage("youarenot_viewAssignmentContent", new Object[]{assignmentContentId}));
				M_log.warn(this + ":" + callingFunctionName + " " + e.getMessage());
			}

		}
		return ac;
	}

	/**
	 * construct time object based on various state variables
	 * @param state
	 * @param monthString
	 * @param dayString
	 * @param yearString
	 * @param hourString
	 * @param minString
	 * @return
	 */
	private Time getTimeFromState(SessionState state, String monthString, String dayString, String yearString, String hourString, String minString)
	{
		if (state.getAttribute(monthString) != null ||
			state.getAttribute(dayString) != null ||
			state.getAttribute(yearString) != null ||
			state.getAttribute(hourString) != null ||
			state.getAttribute(minString) != null)
		{
			int month = ((Integer) state.getAttribute(monthString)).intValue();
			int day = ((Integer) state.getAttribute(dayString)).intValue();
			int year = ((Integer) state.getAttribute(yearString)).intValue();
			int hour = ((Integer) state.getAttribute(hourString)).intValue();
			int min = ((Integer) state.getAttribute(minString)).intValue();
			return TimeService.newTimeLocal(year, month, day, hour, min, 0, 0);
		}
		else
		{
			return null;
		}
	}

	/**
	 * Action is to post new assignment
	 */
	public void doSave_assignment(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		post_save_assignment(data, "save");

	} // doSave_assignment
	
	/**
	 * Action is to reorder assignments
	 */
	public void doReorder_assignment(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		reorderAssignments(data);
	} // doReorder_assignments

	/**
	 * Action is to preview the selected assignment
	 */
	public void doPreview_assignment(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		setNewAssignmentParameters(data, true);

		String assignmentId = data.getParameters().getString("assignmentId");
		state.setAttribute(PREVIEW_ASSIGNMENT_ASSIGNMENT_ID, assignmentId);

		String assignmentContentId = data.getParameters().getString("assignmentContentId");
		state.setAttribute(PREVIEW_ASSIGNMENT_ASSIGNMENTCONTENT_ID, assignmentContentId);

		state.setAttribute(PREVIEW_ASSIGNMENT_ASSIGNMENT_HIDE_FLAG, Boolean.valueOf(false));
		state.setAttribute(PREVIEW_ASSIGNMENT_STUDENT_VIEW_HIDE_FLAG, Boolean.valueOf(true));
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_PREVIEW_ASSIGNMENT);
		}

	} // doPreview_assignment

	/**
	 * Action is to view the selected assignment
	 */
	public void doView_assignment(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();

		// show the assignment portion
		state.setAttribute(VIEW_ASSIGNMENT_HIDE_ASSIGNMENT_FLAG, Boolean.valueOf(false));
		// show the student view portion
		state.setAttribute(VIEW_ASSIGNMENT_HIDE_STUDENT_VIEW_FLAG, Boolean.valueOf(true));

		String assignmentId = params.getString("assignmentId");
		state.setAttribute(VIEW_ASSIGNMENT_ID, assignmentId);

		Assignment a = getAssignment(assignmentId, "doView_assignment", state);
			
		// get resubmission option into state
		assignment_resubmission_option_into_state(a, null, state);
			
		// assignment read event
		m_eventTrackingService.post(m_eventTrackingService.newEvent(AssignmentConstants.EVENT_ACCESS_ASSIGNMENT, assignmentId, false));

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_VIEW_ASSIGNMENT);
		}

	} // doView_Assignment

	/**
	 * Action is for student to view one assignment content
	 */
	public void doView_assignment_as_student(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();

		String assignmentId = params.getString("assignmentId");
		state.setAttribute(VIEW_ASSIGNMENT_ID, assignmentId);

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute(STATE_MODE, MODE_STUDENT_VIEW_ASSIGNMENT);
		}

	} // doView_assignment_as_student

	// TODO: investigate if this method can be removed
	public void doView_submissionReviews(RunData data){
		String submissionId = data.getParameters().getString("submissionId");
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		String assessorId = data.getParameters().getString("assessorId");
		String assignmentId = StringUtils.trimToNull(data.getParameters().getString("assignmentId"));
		Assignment a = getAssignment(assignmentId, "doEdit_assignment", state);
		if (submissionId != null && !"".equals(submissionId) && a != null){
			//set the page to go to
			state.setAttribute(VIEW_ASSIGNMENT_ID, assignmentId);
			List<PeerAssessmentItem> peerAssessmentItems = assignmentPeerAssessmentService.getPeerAssessmentItemsByAssignmentId(a.getId(), a.getContent().getFactor());
			state.setAttribute(PEER_ASSESSMENT_ITEMS, peerAssessmentItems);
			List<String> submissionIds = new ArrayList<String>();
			if(peerAssessmentItems != null){
				for(PeerAssessmentItem item : peerAssessmentItems){
					submissionIds.add(item.getSubmissionId());
				}
			}
			state.setAttribute(USER_SUBMISSIONS, submissionIds);
			state.setAttribute(GRADE_SUBMISSION_SUBMISSION_ID, submissionId);
			state.setAttribute(PEER_ASSESSMENT_ASSESSOR_ID, assessorId);
			state.setAttribute(STATE_MODE, MODE_STUDENT_REVIEW_EDIT);
		}else{
			addAlert(state, rb.getString("peerassessment.notavailable"));
		}
	}
	
	// TODO: investigate if this method can be removed
	public void doEdit_review(RunData data){
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();

		String assignmentId = StringUtils.trimToNull(params.getString("assignmentId"));
		Assignment a = getAssignment(assignmentId, "doEdit_assignment", state);
		if (a != null && a.isPeerAssessmentOpen()){
			//set the page to go to
			state.setAttribute(VIEW_ASSIGNMENT_ID, assignmentId);
			String submissionId = null;
			List<PeerAssessmentItem> peerAssessmentItems = assignmentPeerAssessmentService.getPeerAssessmentItems(a.getId(), UserDirectoryService.getCurrentUser().getId(), a.getContent().getFactor());
			state.setAttribute(PEER_ASSESSMENT_ITEMS, peerAssessmentItems);
			List<String> submissionIds = new ArrayList<String>();
			if(peerAssessmentItems != null){
				for(PeerAssessmentItem item : peerAssessmentItems){
					if(!item.isSubmitted()){
						submissionIds.add(item.getSubmissionId());
					}
				}
			}
			if(params.getString("submissionId") != null && submissionIds.contains(params.getString("submissionId"))){
				submissionId = StringUtils.trimToNull(params.getString("submissionId"));
			}else if(submissionIds.size() > 0){
				//submission Id wasn't passed in, let's find one for this user
				//grab the first one:
				submissionId = submissionIds.get(0);
			}
		
			if(submissionId != null){
				state.setAttribute(USER_SUBMISSIONS, submissionIds);
				state.setAttribute(GRADE_SUBMISSION_SUBMISSION_ID, submissionId);
				state.setAttribute(STATE_MODE, MODE_STUDENT_REVIEW_EDIT);
			}else{
				if(peerAssessmentItems != null && peerAssessmentItems.size() > 0){
					//student has submitted all their peer reviews, nothing left to review
					//(student really shouldn't get to this warning)
					addAlert(state, rb.getString("peerassessment.allSubmitted"));
				}else{
					//wasn't able to find a submission id, throw error
					addAlert(state, rb.getString("peerassessment.notavailable"));
				}
			}
		}else{
			addAlert(state, rb.getString("peerassessment.notavailable"));
		}
	}
	
	/**
	 * Action is to show the edit assignment screen
	 */
	public void doEdit_assignment(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();

		String assignmentId = StringUtils.trimToNull(params.getString("assignmentId"));
		if (AssignmentService.allowUpdateAssignment(assignmentId))
		{
			Assignment a = getAssignment(assignmentId, "doEdit_assignment", state);
			if (a != null)
			{
				// whether the user can modify the assignment
				state.setAttribute(EDIT_ASSIGNMENT_ID, assignmentId);
				
				// for the non_electronice assignment, submissions are auto-generated by the time that assignment is created;
				// don't need to go through the following checkings.
				if (a.getContent().getTypeOfSubmission() != Assignment.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION)
				{
					Iterator submissions = AssignmentService.getSubmissions(a).iterator();
					if (submissions.hasNext())
					{
						// any submitted?
						boolean anySubmitted = false;
						for (;submissions.hasNext() && !anySubmitted;)
						{
							AssignmentSubmission s = (AssignmentSubmission) submissions.next();
							if (s.getSubmitted() && s.getTimeSubmitted() != null)
							{
								anySubmitted = true;
							}
						}
						
						// any draft submission
						boolean anyDraft = false;
						for (;submissions.hasNext() && !anyDraft;)
						{
							AssignmentSubmission s = (AssignmentSubmission) submissions.next();
							if (!s.getSubmitted())
							{
								anyDraft = true;
							}
						}
						if (anySubmitted)
						{
							// if there is any submitted submission to this assignment, show alert
							addAlert(state, rb.getFormattedMessage("hassum", new Object[]{a.getTitle()}));
						}
						
						if (anyDraft)
						{
							// otherwise, show alert about someone has started working on the assignment, not necessarily submitted
							addAlert(state, rb.getString("hasDraftSum"));
						}
					}
				}
	
				// SECTION MOD
				state.setAttribute(STATE_SECTION_STRING, a.getSection());
	
				// put the names and values into vm file
				state.setAttribute(NEW_ASSIGNMENT_TITLE, a.getTitle());
				state.setAttribute(NEW_ASSIGNMENT_ORDER, a.getPosition_order());
				
                                if (Boolean.valueOf(ServerConfigurationService.getBoolean("assignment.visible.date.enabled", false))) {
                                     putTimePropertiesInState(state, a.getVisibleTime(), NEW_ASSIGNMENT_VISIBLEMONTH, NEW_ASSIGNMENT_VISIBLEDAY, NEW_ASSIGNMENT_VISIBLEYEAR, NEW_ASSIGNMENT_VISIBLEHOUR, NEW_ASSIGNMENT_VISIBLEMIN);
                                   	 state.setAttribute(NEW_ASSIGNMENT_VISIBLETOGGLE, a.getVisibleTime()!=null);
				}
                                
				putTimePropertiesInState(state, a.getOpenTime(), NEW_ASSIGNMENT_OPENMONTH, NEW_ASSIGNMENT_OPENDAY, NEW_ASSIGNMENT_OPENYEAR, NEW_ASSIGNMENT_OPENHOUR, NEW_ASSIGNMENT_OPENMIN);
				// generate alert when editing an assignment past open date
				if (a.getOpenTime().before(TimeService.newTime()))
				{
					addAlert(state, rb.getString("youarenot20"));
				}
	
				putTimePropertiesInState(state, a.getDueTime(), NEW_ASSIGNMENT_DUEMONTH, NEW_ASSIGNMENT_DUEDAY, NEW_ASSIGNMENT_DUEYEAR, NEW_ASSIGNMENT_DUEHOUR, NEW_ASSIGNMENT_DUEMIN);
				// generate alert when editing an assignment past due date
				if (a.getDueTime().before(TimeService.newTime()))
				{
					addAlert(state, rb.getString("youarenot17"));
				}
	
				if (a.getCloseTime() != null)
				{
					state.setAttribute(NEW_ASSIGNMENT_ENABLECLOSEDATE, Boolean.valueOf(true));
					putTimePropertiesInState(state, a.getCloseTime(), NEW_ASSIGNMENT_CLOSEMONTH, NEW_ASSIGNMENT_CLOSEDAY, NEW_ASSIGNMENT_CLOSEYEAR, NEW_ASSIGNMENT_CLOSEHOUR, NEW_ASSIGNMENT_CLOSEMIN);
				}
				else
				{
					state.setAttribute(NEW_ASSIGNMENT_ENABLECLOSEDATE, Boolean.valueOf(false));
					state.setAttribute(NEW_ASSIGNMENT_CLOSEMONTH, state.getAttribute(NEW_ASSIGNMENT_DUEMONTH));
					state.setAttribute(NEW_ASSIGNMENT_CLOSEDAY, state.getAttribute(NEW_ASSIGNMENT_DUEDAY));
					state.setAttribute(NEW_ASSIGNMENT_CLOSEYEAR, state.getAttribute(NEW_ASSIGNMENT_DUEYEAR));
					state.setAttribute(NEW_ASSIGNMENT_CLOSEHOUR, state.getAttribute(NEW_ASSIGNMENT_DUEHOUR));
					state.setAttribute(NEW_ASSIGNMENT_CLOSEMIN, state.getAttribute(NEW_ASSIGNMENT_DUEMIN));
				}
				state.setAttribute(NEW_ASSIGNMENT_SECTION, a.getSection());
	
				state.setAttribute(NEW_ASSIGNMENT_SUBMISSION_TYPE, Integer.valueOf(a.getContent().getTypeOfSubmission()));
				state.setAttribute(NEW_ASSIGNMENT_CATEGORY, getAssignmentCategoryAsInt(a));
				int typeOfGrade = a.getContent().getTypeOfGrade();
				state.setAttribute(NEW_ASSIGNMENT_GRADE_TYPE, Integer.valueOf(typeOfGrade));
				if (typeOfGrade == 3)
				{
					state.setAttribute(NEW_ASSIGNMENT_GRADE_POINTS, a.getContent().getMaxGradePointDisplay());
				}
				state.setAttribute(NEW_ASSIGNMENT_DESCRIPTION, a.getContent().getInstructions());
                state.setAttribute(NEW_ASSIGNMENT_CHECK_HIDE_DUE_DATE, Boolean.valueOf(a.getContent().getHideDueDate()).toString());
				ResourceProperties properties = a.getProperties();
				state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE, properties.getProperty(
						ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE));
				
				state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE, properties.getProperty(
						ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE));
				
				String defaultNotification = ServerConfigurationService.getString("announcement.default.notification", "n");
				if (defaultNotification.equalsIgnoreCase("r")) {
					state.setAttribute(Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION, Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION_HIGH);
				}
				else if (defaultNotification.equalsIgnoreCase("o")) {
					state.setAttribute(Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION, Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION_LOW);
				}
				else {
					state.setAttribute(Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION, Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION_NONE);
				}
								
				state.setAttribute(NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE, Integer.toString(a.getContent().getHonorPledge()));
				
				state.setAttribute(AssignmentService.NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, properties.getProperty(AssignmentService.NEW_ASSIGNMENT_ADD_TO_GRADEBOOK));
				state.setAttribute(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT, properties.getProperty(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT));
				
				state.setAttribute(ATTACHMENTS, a.getContent().getAttachments());
				
				// submission notification option
				if (properties.getProperty(Assignment.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_VALUE) != null)
				{
					state.setAttribute(Assignment.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_VALUE, properties.getProperty(Assignment.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_VALUE));
				}
				// release grade notification option
				if (properties.getProperty(Assignment.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_VALUE) != null)
				{
					state.setAttribute(Assignment.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_VALUE, properties.getProperty(Assignment.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_VALUE));
				}
	
				// group setting
				if (a.getAccess().equals(Assignment.AssignmentAccess.SITE))
				{
					state.setAttribute(NEW_ASSIGNMENT_RANGE, "site");
				}
				else
				{
					state.setAttribute(NEW_ASSIGNMENT_RANGE, "groups");
				}
				
				// SAK-17606
				state.setAttribute(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING, properties.getProperty(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING));
				
				// put the resubmission option into state
				assignment_resubmission_option_into_state(a, null, state);
				
				// set whether we use peer assessment or not
				Time peerAssessmentPeriod = a.getPeerAssessmentPeriod();
				//check if peer assessment time exist? if not, this could be an old assignment, so just set it
				//to 10 min after accept until date
				if(peerAssessmentPeriod == null && a.getCloseTime() != null){
					// set the peer period time to be 10 mins after accept until date
					GregorianCalendar c = new GregorianCalendar();
					c.setTimeInMillis(a.getCloseTime().getTime());
					c.add(GregorianCalendar.MINUTE, 10);
					peerAssessmentPeriod = TimeService.newTime(c.getTimeInMillis());
				}
				if(peerAssessmentPeriod != null){
					state.setAttribute(NEW_ASSIGNMENT_USE_PEER_ASSESSMENT, Boolean.valueOf(a.getAllowPeerAssessment()).toString());
					putTimePropertiesInState(state, peerAssessmentPeriod, NEW_ASSIGNMENT_PEERPERIODMONTH, NEW_ASSIGNMENT_PEERPERIODDAY, NEW_ASSIGNMENT_PEERPERIODYEAR, NEW_ASSIGNMENT_PEERPERIODHOUR, NEW_ASSIGNMENT_PEERPERIODMIN);
					state.setAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_ANON_EVAL, Boolean.valueOf(a.getPeerAssessmentAnonEval()).toString());
					state.setAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_STUDENT_VIEW_REVIEWS, Boolean.valueOf(a.getPeerAssessmentStudentViewReviews()).toString());
					state.setAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_NUM_REVIEWS, a.getPeerAssessmentNumReviews());
					state.setAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_INSTRUCTIONS, a.getPeerAssessmentInstructions());
				}
				if(!allowPeerAssessment){
					state.setAttribute(NEW_ASSIGNMENT_USE_PEER_ASSESSMENT, false);
				}
				// set whether we use the review service or not
				state.setAttribute(NEW_ASSIGNMENT_USE_REVIEW_SERVICE, Boolean.valueOf(a.getContent().getAllowReviewService()).toString());
				
				//set whether students can view the review service results
				state.setAttribute(NEW_ASSIGNMENT_ALLOW_STUDENT_VIEW, Boolean.valueOf(a.getContent().getAllowStudentViewReport()).toString());
				
				state.setAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_RADIO, a.getContent().getSubmitReviewRepo());
				state.setAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_RADIO, a.getContent().getGenerateOriginalityReport());
				state.setAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_TURNITIN, Boolean.valueOf(a.getContent().isCheckTurnitin()).toString());
				state.setAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INTERNET, Boolean.valueOf(a.getContent().isCheckInternet()).toString());
				state.setAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_PUB, Boolean.valueOf(a.getContent().isCheckPublications()).toString());
				state.setAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INSTITUTION, Boolean.valueOf(a.getContent().isCheckInstitution()).toString());
				//exclude bibliographic
				state.setAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_BIBLIOGRAPHIC, Boolean.valueOf(a.getContent().isExcludeBibliographic()).toString());
				//exclude quoted
				state.setAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_QUOTED, Boolean.valueOf(a.getContent().isExcludeQuoted()).toString());
				//exclude type
				state.setAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_TYPE, a.getContent().getExcludeType());
				//exclude value
				state.setAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_VALUE,a.getContent().getExcludeValue());
				
				state.setAttribute(NEW_ASSIGNMENT_GROUPS, a.getGroups());
				
				state.setAttribute(NEW_ASSIGNMENT_GROUP_SUBMIT, a.isGroup() ? "1": "0");
                                
				// get all supplement item info into state
				setAssignmentSupplementItemInState(state, a);
				
				state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_NEW_EDIT_ASSIGNMENT);
			}
		}
		else
		{
			addAlert(state, rb.getString("youarenot6"));
		}

	} // doEdit_Assignment

	public List<String> getSubmissionRepositoryOptions() {
        List<String> submissionRepoSettings = new ArrayList<String>();
        String[] propertyValues = ServerConfigurationService.getStrings("turnitin.repository.setting");
        if (propertyValues != null && propertyValues.length > 0) {
            for (int i=0; i < propertyValues.length; i++) {
                String propertyVal = propertyValues[i];
                if (propertyVal.equals(NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_NONE) || 
                        propertyVal.equals(NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_INSITUTION) ||
                        propertyVal.equals(NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_STANDARD)) {
                    submissionRepoSettings.add(propertyVal);
                }
            }
        }

        // if there are still no valid settings in the list at this point, use the default
        if (submissionRepoSettings.isEmpty()) {
            // add all three
            submissionRepoSettings.add(NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_NONE);
            submissionRepoSettings.add(NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_STANDARD);
        }

        return submissionRepoSettings;
    }
	
	public List<String> getReportGenOptions() {
        List<String> reportGenSettings = new ArrayList<String>();
        String[] propertyValues = ServerConfigurationService.getStrings("turnitin.report_gen_speed.setting");
        if (propertyValues != null && propertyValues.length > 0) {
            for (int i=0; i < propertyValues.length; i++) {
                String propertyVal = propertyValues[i];
                if (propertyVal.equals(NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_DUE) || 
                        propertyVal.equals(NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_IMMEDIATELY)) {
                	reportGenSettings.add(propertyVal);
                }
            }
        }

        // if there are still no valid settings in the list at this point, use the default
        if (reportGenSettings.isEmpty()) {
            // add all three
        	reportGenSettings.add(NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_DUE);
            reportGenSettings.add(NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_IMMEDIATELY);
        }

        return reportGenSettings;
    }
	
	/**
	 * put all assignment supplement item info into state
	 * @param state
	 * @param a
	 */
	private void setAssignmentSupplementItemInState(SessionState state, Assignment a) {
		
		String assignmentId = a.getId();
		
		// model answer
		AssignmentModelAnswerItem mAnswer = m_assignmentSupplementItemService.getModelAnswer(assignmentId);
		if (mAnswer != null)
		{
			if (state.getAttribute(MODELANSWER_TEXT) == null)
			{
				state.setAttribute(MODELANSWER_TEXT, mAnswer.getText());
			}
			if (state.getAttribute(MODELANSWER_SHOWTO) == null)
			{
				state.setAttribute(MODELANSWER_SHOWTO, String.valueOf(mAnswer.getShowTo()));
			}
			if (state.getAttribute(MODELANSWER) == null)
			{
				state.setAttribute(MODELANSWER, Boolean.TRUE);
			}
		}

		// get attachments for model answer object
		putSupplementItemAttachmentInfoIntoState(state, mAnswer, MODELANSWER_ATTACHMENTS);
		
		// private notes
		AssignmentNoteItem mNote = m_assignmentSupplementItemService.getNoteItem(assignmentId);
		if (mNote != null)
		{
			if (state.getAttribute(NOTE) == null)
			{
				state.setAttribute(NOTE, Boolean.TRUE);
			}
			if (state.getAttribute(NOTE_TEXT) == null)
			{
				state.setAttribute(NOTE_TEXT, mNote.getNote());
			}
			if (state.getAttribute(NOTE_SHAREWITH) == null)
			{
				state.setAttribute(NOTE_SHAREWITH, String.valueOf(mNote.getShareWith()));
			}
		}
		
		// all purpose item
		AssignmentAllPurposeItem aItem = m_assignmentSupplementItemService.getAllPurposeItem(assignmentId);
		if (aItem != null)
		{
			if (state.getAttribute(ALLPURPOSE) == null)
			{
				state.setAttribute(ALLPURPOSE, Boolean.TRUE);
			}
			if (state.getAttribute(ALLPURPOSE_TITLE) == null)
			{
				state.setAttribute(ALLPURPOSE_TITLE, aItem.getTitle());
			}
			if (state.getAttribute(ALLPURPOSE_TEXT) == null)
			{
				state.setAttribute(ALLPURPOSE_TEXT, aItem.getText());
			}
			if (state.getAttribute(ALLPURPOSE_HIDE) == null)
			{
				state.setAttribute(ALLPURPOSE_HIDE, Boolean.valueOf(aItem.getHide()));
			}
			if (state.getAttribute(ALLPURPOSE_SHOW_FROM) == null)
			{
				state.setAttribute(ALLPURPOSE_SHOW_FROM, aItem.getReleaseDate() != null);
			}
			if (state.getAttribute(ALLPURPOSE_SHOW_TO) == null)
			{
				state.setAttribute(ALLPURPOSE_SHOW_TO, aItem.getRetractDate() != null);
			}
			if (state.getAttribute(ALLPURPOSE_ACCESS) == null)
			{
				Set<AssignmentAllPurposeItemAccess> aSet = aItem.getAccessSet();
				List<String> aList = new ArrayList<String>();
				for(Iterator<AssignmentAllPurposeItemAccess> aIterator = aSet.iterator(); aIterator.hasNext();)
				{
					AssignmentAllPurposeItemAccess access = aIterator.next();
					aList.add(access.getAccess());
				}
				state.setAttribute(ALLPURPOSE_ACCESS, aList);
			}

			// get attachments for model answer object
			putSupplementItemAttachmentInfoIntoState(state, aItem, ALLPURPOSE_ATTACHMENTS);
		}
			
		// get the AllPurposeItem and AllPurposeReleaseTime/AllPurposeRetractTime
		//default to assignment open time
		Time releaseTime = a.getOpenTime();
		// default to assignment close time
		Time retractTime = a.getCloseTime();
		if (aItem != null)
		{
			Date releaseDate = aItem.getReleaseDate();
			if (releaseDate != null)
			{
				// overwrite if there is a release date
				releaseTime = TimeService.newTime(releaseDate.getTime());
			}
			
			Date retractDate = aItem.getRetractDate();
			if (retractDate != null)
			{
				// overwriteif there is a retract date
				retractTime = TimeService.newTime(retractDate.getTime());
			}
		}
		putTimePropertiesInState(state, releaseTime, ALLPURPOSE_RELEASE_MONTH, ALLPURPOSE_RELEASE_DAY, ALLPURPOSE_RELEASE_YEAR, ALLPURPOSE_RELEASE_HOUR, ALLPURPOSE_RELEASE_MIN);
		
		putTimePropertiesInState(state, retractTime, ALLPURPOSE_RETRACT_MONTH, ALLPURPOSE_RETRACT_DAY, ALLPURPOSE_RETRACT_YEAR, ALLPURPOSE_RETRACT_HOUR, ALLPURPOSE_RETRACT_MIN);
	}

	/**
	 * Action is to show the delete assigment confirmation screen
	 */
	public void doDelete_confirm_assignment(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();

		String[] assignmentIds = params.getStrings("selectedAssignments");

		if (assignmentIds != null)
		{
			List ids = new ArrayList();
			for (int i = 0; i < assignmentIds.length; i++)
			{
				String id = (String) assignmentIds[i];
				if (!AssignmentService.allowRemoveAssignment(id))
				{
					addAlert(state, rb.getFormattedMessage("youarenot_removeAssignment", new Object[]{id}));
				}
				ids.add(id);
			}

			if (state.getAttribute(STATE_MESSAGE) == null)
			{
				// can remove all the selected assignments
				state.setAttribute(DELETE_ASSIGNMENT_IDS, ids);
				state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_DELETE_ASSIGNMENT);
			}
		}
		else
		{
			addAlert(state, rb.getString("youmust6"));
		}

	} // doDelete_confirm_Assignment

	/**
	 * Action is to delete the confirmed assignments
	 */
	public void doDelete_assignment(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// get the delete assignment ids
		List ids = (List) state.getAttribute(DELETE_ASSIGNMENT_IDS);
		for (int i = 0; i < ids.size(); i++)
		{

			String assignmentId = (String) ids.get(i);
			AssignmentEdit aEdit = editAssignment(assignmentId, "doDelete_assignment", state, false);
			if (aEdit != null)
			{
				ResourcePropertiesEdit pEdit = aEdit.getPropertiesEdit();

				String associateGradebookAssignment = pEdit.getProperty(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);

				String title = aEdit.getTitle();

				// remove related event if there is one
				removeCalendarEvent(state, aEdit, pEdit, title);
				
				// remove related announcement if there is one
				removeAnnouncement(state, pEdit);

				// remove from Gradebook
				integrateGradebook(state, (String) ids.get (i), associateGradebookAssignment, "remove", null, null, -1, null, null, null, -1);

				// we use to check "assignment.delete.cascade.submission" setting. But the implementation now is always remove submission objects when the assignment is removed.
				// delete assignment and its submissions altogether
				deleteAssignmentObjects(state, aEdit, true);
			}
		} // for

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute(DELETE_ASSIGNMENT_IDS, new ArrayList());

			state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
			
			// reset paging information after the assignment been deleted
			resetPaging(state);
		}

	} // doDelete_Assignment


	/**
	 * private function to remove assignment related announcement
	 * @param state
	 * @param pEdit
	 */
	private void removeAnnouncement(SessionState state,
			ResourcePropertiesEdit pEdit) {
		AnnouncementChannel channel = (AnnouncementChannel) state.getAttribute(ANNOUNCEMENT_CHANNEL);
		if (channel != null)
		{
			String openDateAnnounced = StringUtils.trimToNull(pEdit.getProperty(NEW_ASSIGNMENT_OPEN_DATE_ANNOUNCED));
			String openDateAnnouncementId = StringUtils.trimToNull(pEdit.getProperty(ResourceProperties.PROP_ASSIGNMENT_OPENDATE_ANNOUNCEMENT_MESSAGE_ID));
			if (openDateAnnounced != null && openDateAnnouncementId != null)
			{
				try
				{
					channel.removeMessage(openDateAnnouncementId);
				}
				catch (PermissionException e)
				{
					M_log.warn(this + ":removeAnnouncement " + e.getMessage());
				}
			}
		}
	}

	/**
	 * private method to remove assignment and related objects
	 * @param state
	 * @param aEdit
	 * @param removeSubmissions Whether or not to remove the submission objects
	 */
	private void deleteAssignmentObjects(SessionState state, AssignmentEdit aEdit, boolean removeSubmissions) {
		
		if (removeSubmissions)
		{
			// if this is non-electronic submission, remove all the submissions
			List submissions = AssignmentService.getSubmissions(aEdit);
			if (submissions != null)
			{
				for (Iterator sIterator=submissions.iterator(); sIterator.hasNext();)
				{
					AssignmentSubmission s = (AssignmentSubmission) sIterator.next();
					AssignmentSubmissionEdit sEdit = editSubmission((s.getReference()), "deleteAssignmentObjects", state);
					try
					{
						AssignmentService.removeSubmission(sEdit);
					}
					catch (Exception eee)
					{
                                            // Trapping for InUseException... go ahead and remove them.
                                            if (!(eee instanceof InUseException)) {
						addAlert(state, rb.getFormattedMessage("youarenot_removeSubmission", new Object[]{s.getReference()}));
						M_log.warn(this + ":deleteAssignmentObjects " + eee.getMessage() + " " + s.getReference());
					}
				}
			}
		}
		}
		
		AssignmentContent aContent = aEdit.getContent();
		if (aContent != null)
		{
			try
			{
				
				// remove the assignment content
				AssignmentContentEdit acEdit = editAssignmentContent(aContent.getReference(), "deleteAssignmentObjects", state, false);
				if (acEdit != null)
					AssignmentService.removeAssignmentContent(acEdit);
			}
			catch (Exception ee)
			{
				addAlert(state, rb.getString("youarenot11_c") + " " + aEdit.getContentReference() + ". ");
				M_log.warn(this + ":deleteAssignmentObjects " + ee.getMessage());
			}
		}
		
		try
		{
			TaggingManager taggingManager = (TaggingManager) ComponentManager
					.get("org.sakaiproject.taggable.api.TaggingManager");

			AssignmentActivityProducer assignmentActivityProducer = (AssignmentActivityProducer) ComponentManager
					.get("org.sakaiproject.assignment.taggable.api.AssignmentActivityProducer");

			if (taggingManager.isTaggable()) {
				for (TaggingProvider provider : taggingManager
						.getProviders()) {
					provider.removeTags(assignmentActivityProducer
							.getActivity(aEdit));
				}
			}
			
			AssignmentService.removeAssignment(aEdit);
		}
		catch (PermissionException ee)
		{
			addAlert(state, rb.getString("youarenot11") + " " + aEdit.getTitle() + ". ");
			M_log.warn(this + ":deleteAssignmentObjects " + ee.getMessage());
		}
	}

	private void removeCalendarEvent(SessionState state, AssignmentEdit aEdit, ResourcePropertiesEdit pEdit, String title)
	{
		String isThereEvent = pEdit.getProperty(NEW_ASSIGNMENT_DUE_DATE_SCHEDULED);
		if (isThereEvent != null && isThereEvent.equals(Boolean.TRUE.toString()))
		{
			// remove the associated calendar event
			Calendar c = (Calendar) state.getAttribute(CALENDAR);
			removeCalendarEventFromCalendar(state, aEdit, pEdit, title, c, ResourceProperties.PROP_ASSIGNMENT_DUEDATE_CALENDAR_EVENT_ID);
			
			// remove the associated event from the additional calendar
			Calendar additionalCalendar = (Calendar) state.getAttribute(ADDITIONAL_CALENDAR);
			removeCalendarEventFromCalendar(state, aEdit, pEdit, title, additionalCalendar, ResourceProperties.PROP_ASSIGNMENT_DUEDATE_ADDITIONAL_CALENDAR_EVENT_ID);

		}
	}


	// Retrieves the calendar event associated with the due date and removes it from the calendar.
	private void removeCalendarEventFromCalendar(SessionState state, AssignmentEdit aEdit, ResourcePropertiesEdit pEdit, String title, Calendar c, String dueDateProperty) {
		if (c != null)
		{
			// already has calendar object
			// get the old event
			CalendarEvent e = null;
			boolean found = false;
			String oldEventId = pEdit.getProperty(dueDateProperty);
			if (oldEventId != null)
			{
				try
				{
					e = c.getEvent(oldEventId);
					found = true;
				}
				catch (IdUnusedException ee)
				{
					// no action needed for this condition
					M_log.warn(this + ":removeCalendarEventFromCalendar " + ee.getMessage());
				}
				catch (PermissionException ee)
				{
					M_log.warn(this + ":removeCalendarEventFromCalendar " + ee.getMessage());
				}
			}
			else
			{
				TimeBreakdown b = aEdit.getDueTime().breakdownLocal();
				// TODO: check- this was new Time(year...), not local! -ggolden
				Time startTime = TimeService.newTimeLocal(b.getYear(), b.getMonth(), b.getDay(), 0, 0, 0, 0);
				Time endTime = TimeService.newTimeLocal(b.getYear(), b.getMonth(), b.getDay(), 23, 59, 59, 999);
				try
				{
					Iterator events = c.getEvents(TimeService.newTimeRange(startTime, endTime), null).iterator();
					while ((!found) && (events.hasNext()))
					{
						e = (CalendarEvent) events.next();
						if (((String) e.getDisplayName()).indexOf(rb.getString("gen.assig") + " " + title) != -1)
						{
							found = true;
						}
					}
				}
				catch (PermissionException pException)
				{
					addAlert(state, rb.getFormattedMessage("cannot_getEvents", new Object[]{c.getReference()}));
				}
			}
			// remove the found old event
			if (found)
			{
				// found the old event delete it
				removeOldEvent(title, c, e);
				pEdit.removeProperty(NEW_ASSIGNMENT_DUE_DATE_SCHEDULED);
				pEdit.removeProperty(dueDateProperty);
			}
		}
	}

	/**
	 * Action is to delete the assignment and also the related AssignmentSubmission
	 */
	public void doDeep_delete_assignment(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// get the delete assignment ids
		List ids = (List) state.getAttribute(DELETE_ASSIGNMENT_IDS);
		for (int i = 0; i < ids.size(); i++)
		{
			String currentId = (String) ids.get(i);
			AssignmentEdit a = editAssignment(currentId, "doDeep_delete_assignment", state, false);
			if (a != null)
			{
				try
				{
					TaggingManager taggingManager = (TaggingManager) ComponentManager
							.get("org.sakaiproject.taggable.api.TaggingManager");

					AssignmentActivityProducer assignmentActivityProducer = (AssignmentActivityProducer) ComponentManager
					.get("org.sakaiproject.assignment.taggable.api.AssignmentActivityProducer");

					if (taggingManager.isTaggable()) {
						for (TaggingProvider provider : taggingManager
								.getProviders()) {
							provider.removeTags(assignmentActivityProducer
									.getActivity(a));
						}
					}
			
					AssignmentService.removeAssignment(a);
				}
				catch (PermissionException e)
				{
					addAlert(state, rb.getFormattedMessage("youarenot_editAssignment", new Object[]{a.getTitle()}));
					M_log.warn(this + ":doDeep_delete_assignment " + e.getMessage());
				}
			}
		}
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute(DELETE_ASSIGNMENT_IDS, new ArrayList());
			state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
		}

	} // doDeep_delete_Assignment

	/**
	 * Action is to show the duplicate assignment screen
	 */
	public void doDuplicate_assignment(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// we are changing the view, so start with first page again.
		resetPaging(state);

		String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
		ParameterParser params = data.getParameters();
		String assignmentId = StringUtils.trimToNull(params.getString("assignmentId"));

		if (assignmentId != null)
		{
			try
			{
				AssignmentEdit aEdit = AssignmentService.addDuplicateAssignment(contextString, assignmentId);

				// clean the duplicate's property
				ResourcePropertiesEdit aPropertiesEdit = aEdit.getPropertiesEdit();
				aPropertiesEdit.removeProperty(NEW_ASSIGNMENT_DUE_DATE_SCHEDULED);
				aPropertiesEdit.removeProperty(ResourceProperties.PROP_ASSIGNMENT_DUEDATE_CALENDAR_EVENT_ID);
				aPropertiesEdit.removeProperty(ResourceProperties.PROP_ASSIGNMENT_DUEDATE_ADDITIONAL_CALENDAR_EVENT_ID);
				aPropertiesEdit.removeProperty(NEW_ASSIGNMENT_OPEN_DATE_ANNOUNCED);
				aPropertiesEdit.removeProperty(ResourceProperties.PROP_ASSIGNMENT_OPENDATE_ANNOUNCEMENT_MESSAGE_ID);
				aPropertiesEdit.removeProperty(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK);
				aPropertiesEdit.removeProperty(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);

				AssignmentService.commitEdit(aEdit);
			}
			catch (PermissionException e)
			{
				addAlert(state, rb.getString("youarenot5"));
				M_log.warn(this + ":doDuplicate_assignment " + e.getMessage());
			}
			catch (IdInvalidException e)
			{
				addAlert(state, rb.getFormattedMessage("theassiid_isnotval", new Object[]{assignmentId}));
				M_log.warn(this + ":doDuplicate_assignment " + e.getMessage());
			}
			catch (IdUnusedException e)
			{
				addAlert(state, rb.getFormattedMessage("theassiid_hasnotbee", new Object[]{assignmentId}));
				M_log.warn(this + ":doDuplicate_assignment " + e.getMessage());
			}
			catch (Exception e)
			{
				M_log.warn(this + ":doDuplicate_assignment " + e.getMessage());
			}

		}

	} // doDuplicate_Assignment

	/**
	 * Action is to show the grade submission screen
	 */
	public void doGrade_submission(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// reset the submission context
		resetViewSubmission(state);
		
		ParameterParser params = data.getParameters();
		String assignmentId = params.getString("assignmentId");
		String submissionId = params.getString("submissionId");
		
		// SAK-29314 - put submission information into state
		boolean viewSubsOnlySelected = stringToBool((String)data.getParameters().getString(PARAMS_VIEW_SUBS_ONLY_CHECKBOX));
		putSubmissionInfoIntoState(state, assignmentId, submissionId, viewSubsOnlySelected);

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute(GRADE_SUBMISSION_ASSIGNMENT_EXPAND_FLAG, Boolean.valueOf(false));
			state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_GRADE_SUBMISSION);
			state.setAttribute(FROM_VIEW, (String)params.getString("option"));
			// assignment read event
			m_eventTrackingService.post(m_eventTrackingService.newEvent(AssignmentConstants.EVENT_ACCESS_ASSIGNMENT_SUBMISSION, submissionId, false));
		}
	} // doGrade_submission

	/**
	 * put all the submission information into state variables
	 * @param state
	 * @param assignmentId
	 * @param submissionId
	 * @param viewSubsOnlySelected
	 */
	private void putSubmissionInfoIntoState(SessionState state, String assignmentId, String submissionId, boolean viewSubsOnlySelected)
	{
		// reset grading submission variables
		resetGradeSubmission(state);
		
		// reset the grade assignment id and submission id
		state.setAttribute(GRADE_SUBMISSION_ASSIGNMENT_ID, assignmentId);
		state.setAttribute(GRADE_SUBMISSION_SUBMISSION_ID, submissionId);
		
		// SAK-29314
		state.setAttribute(STATE_VIEW_SUBS_ONLY, viewSubsOnlySelected);
		
		// allow resubmit number
		String allowResubmitNumber = "0";
		Assignment a = getAssignment(assignmentId, "putSubmissionInfoIntoState", state);
		if (a != null)
		{
			AssignmentSubmission s = getSubmission(submissionId, "putSubmissionInfoIntoState", state);
			if (s != null)
			{
				state.setAttribute(GRADE_SUBMISSION_FEEDBACK_TEXT, s.getSubmittedText());
				
				if ((s.getFeedbackText() == null) || (s.getFeedbackText().length() == 0))
				{
					state.setAttribute(GRADE_SUBMISSION_FEEDBACK_TEXT, s.getSubmittedText());
				}
				else
				{
					state.setAttribute(GRADE_SUBMISSION_FEEDBACK_TEXT, s.getFeedbackText());
				}
				
				state.setAttribute(GRADE_SUBMISSION_FEEDBACK_COMMENT, s.getFeedbackComment());

				List v = EntityManager.newReferenceList();
				Iterator attachments = s.getFeedbackAttachments().iterator();
				while (attachments.hasNext())
				{
					v.add(attachments.next());
				}
				state.setAttribute(GRADE_SUBMISSION_FEEDBACK_ATTACHMENT,v);
				state.setAttribute(ATTACHMENTS, v);

				state.setAttribute(GRADE_SUBMISSION_GRADE, s.getGrade());

				// populate grade overrides if they exist
				if (a.isGroup()) {
				    User[] _users = s.getSubmitters();
				    for (int i=0; _users != null && i < _users.length; i++) {
				    	String grade_override= (StringUtils.trimToNull(a.getProperties().getProperty(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT))!=null) && (a.getContent().getTypeOfGrade() == Assignment.SCORE_GRADE_TYPE) ?
				    			(s.getGradeForUserInGradeBook(_users[i].getId())!=null) && !(s.getGradeForUserInGradeBook(_users[i].getId()).equals(displayGrade(state, (String) state.getAttribute(GRADE_SUBMISSION_GRADE), a.getContent().getFactor()))) && state.getAttribute(GRADE_SUBMISSION_GRADE)!=null ? s.getGradeForUserInGradeBook(_users[i].getId()) : s.getGradeForUser(_users[i].getId()) : s.getGradeForUser(_users[i].getId());
				    	if (grade_override != null) 
				    	{
				    			state.setAttribute(GRADE_SUBMISSION_GRADE + "_" + _users[i].getId(), grade_override);
				    	}
				    }
				}

				// put the resubmission info into state
				assignment_resubmission_option_into_state(a, s, state);
			}
		}
	}

	/**
	 * Action is to release all the grades of the submission
	 */
	public void doRelease_grades(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		ParameterParser params = data.getParameters();

		String assignmentId = params.getString("assignmentId");
		
		// get the assignment
		Assignment a = getAssignment(assignmentId, "doRelease_grades", state);

		if (a != null)
		{
			String aReference = a.getReference();

			Iterator submissions = getFilteredSubmitters(state, aReference).iterator();
			while (submissions.hasNext())
			{
				AssignmentSubmission s = (AssignmentSubmission) submissions.next();
				if (s.getGraded() || (s.getGrade()!=null && !"".equals(s.getGrade())))
				{
					String sRef = s.getReference();
					AssignmentSubmissionEdit sEdit = editSubmission(sRef, "doRelease_grades", state);
					if (sEdit != null)
					{
						String grade = s.getGrade();
						
						boolean withGrade = state.getAttribute(WITH_GRADES) != null ? ((Boolean) state.getAttribute(WITH_GRADES))
								.booleanValue() : false;
						if (withGrade)
						{
							// for the assignment tool with grade option, a valide grade is needed
							if (grade != null && !"".equals(grade))
							{
								sEdit.setGradeReleased(true);
								if(!s.getGraded())
								{
									sEdit.setGraded(true);
								}
							}
						}
						else
						{
							// for the assignment tool without grade option, no grade is needed
							sEdit.setGradeReleased(true);
						}
						
						// also set the return status
						sEdit.setReturned(true);
						sEdit.setTimeReturned(TimeService.newTime());
						sEdit.setHonorPledgeFlag(Boolean.FALSE.booleanValue());
						
						AssignmentService.commitEdit(sEdit);
					}
				}

			} // while

			// add grades into Gradebook
			String integrateWithGradebook = a.getProperties().getProperty(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK);
			if (integrateWithGradebook != null && !integrateWithGradebook.equals(AssignmentService.GRADEBOOK_INTEGRATION_NO))
			{
				// integrate with Gradebook
				String associateGradebookAssignment = StringUtils.trimToNull(a.getProperties().getProperty(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT));

				integrateGradebook(state, aReference, associateGradebookAssignment, null, null, null, -1, null, null, "update", -1);
			}
		}

	} // doRelease_grades

	/**
	 * Action is to show the assignment in grading page
	 */
	public void doExpand_grade_assignment(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(GRADE_ASSIGNMENT_EXPAND_FLAG, Boolean.valueOf(true));

	} // doExpand_grade_assignment

	/**
	 * Action is to hide the assignment in grading page
	 */
	public void doCollapse_grade_assignment(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(GRADE_ASSIGNMENT_EXPAND_FLAG, Boolean.valueOf(false));

	} // doCollapse_grade_assignment

	/**
	 * Action is to show the submissions in grading page
	 */
	public void doExpand_grade_submission(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(GRADE_SUBMISSION_EXPAND_FLAG, Boolean.valueOf(true));

	} // doExpand_grade_submission

	/**
	 * Action is to hide the submissions in grading page
	 */
	public void doCollapse_grade_submission(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(GRADE_SUBMISSION_EXPAND_FLAG, Boolean.valueOf(false));

	} // doCollapse_grade_submission

	/**
	 * Action is to show the grade assignment
	 */
	public void doGrade_assignment(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();

		// clean state attribute
		state.removeAttribute(USER_SUBMISSIONS);
		state.removeAttribute(SAVED_FEEDBACK);
		state.removeAttribute(OW_FEEDBACK);
		state.removeAttribute(RETURNED_FEEDBACK);

		String assignmentId = params.getString("assignmentId");
		state.setAttribute(EXPORT_ASSIGNMENT_REF, assignmentId);

		Assignment a = getAssignment(assignmentId, "doGrade_assignment", state);
		if (a != null)
		{
			state.setAttribute(EXPORT_ASSIGNMENT_ID, a.getId());
			state.setAttribute(GRADE_ASSIGNMENT_EXPAND_FLAG, Boolean.valueOf(false));
			state.setAttribute(GRADE_SUBMISSION_EXPAND_FLAG, Boolean.valueOf(true));
			state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_GRADE_ASSIGNMENT);
			
			// initialize the resubmission params
			assignment_resubmission_option_into_state(a, null, state);

			// we are changing the view, so start with first page again.
			resetPaging(state);
		}
	} // doGrade_assignment

	/**
	 * Action is to show the View Students assignment screen
	 */
	public void doView_students_assignment(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT);

	} // doView_students_Assignment

	/**
	 * Action is to show the student submissions
	 */
	public void doShow_student_submission(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		Set t = (Set) state.getAttribute(STUDENT_LIST_SHOW_TABLE);
		ParameterParser params = data.getParameters();

		String id = params.getString("studentId");
		// add the student id into the table
		t.add(id);

		state.setAttribute(STUDENT_LIST_SHOW_TABLE, t);

	} // doShow_student_submission

	/**
	 * Action is to hide the student submissions
	 */
	public void doHide_student_submission(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		Set t = (Set) state.getAttribute(STUDENT_LIST_SHOW_TABLE);
		ParameterParser params = data.getParameters();

		String id = params.getString("studentId");
		// remove the student id from the table
		t.remove(id);

		state.setAttribute(STUDENT_LIST_SHOW_TABLE, t);

	} // doHide_student_submission

	/**
	 * Action is to show the graded assignment submission
	 */
	public void doView_grade(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		ParameterParser params = data.getParameters();

		String submissionReference = params.getString("submissionId");

		prepareStudentViewGrade(state, submissionReference);
	} // doView_grade

	/**
	 * Prepares the state for the student to view their grade
	 */
	private void prepareStudentViewGrade(SessionState state, String submissionReference)
	{
		state.setAttribute(VIEW_GRADE_SUBMISSION_ID, submissionReference);

		String _mode = MODE_STUDENT_VIEW_GRADE;

		AssignmentSubmission _s = getSubmission((String) state.getAttribute(VIEW_GRADE_SUBMISSION_ID), "doView_grade", state);
		// whether the user can access the Submission object
		if (_s != null)
		{
			String status = _s.getStatus();
			if ("Not Started".equals(status))
			{
				addAlert(state, rb.getString("stuviewsubm.theclodat"));
			}

			// show submission view unless group submission with group error
			Assignment a = _s.getAssignment();
			User u = (User) state.getAttribute(STATE_USER);
			if (a.isGroup())
			{
				Collection groups = null;
				Site st = null;
				try
				{
					st = SiteService.getSite((String) state.getAttribute(STATE_CONTEXT_STRING));
					groups = getGroupsWithUser(u.getId(), a, st);
					Collection<String> _dupUsers = checkForGroupsInMultipleGroups(a, groups, state, rb.getString("group.user.multiple.warning"));
					if (_dupUsers.size() > 0)
					{
						_mode = MODE_STUDENT_VIEW_GROUP_ERROR;
						state.setAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE, _s.getAssignmentId());
					}
				}
				catch (IdUnusedException iue)
				{
					M_log.warn(this + ":doView_grade found!" + iue.getMessage());
				}
			}
			state.setAttribute(STATE_MODE, _mode);
		}
	}
	
	/**
	 * Action is to show the graded assignment submission while keeping specific information private
	 */
	public void doView_grade_private(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		ParameterParser params = data.getParameters();

		state.setAttribute(VIEW_GRADE_SUBMISSION_ID, params.getString("submissionId"));
		
		// whether the user can access the Submission object
		if (getSubmission((String) state.getAttribute(VIEW_GRADE_SUBMISSION_ID), "doView_grade_private", state ) != null)
		{
			state.setAttribute(STATE_MODE, MODE_STUDENT_VIEW_GRADE_PRIVATE);
		}

	} // doView_grade_private

	/**
	 * Action is to show the student submissions
	 */
	public void doReport_submissions(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_REPORT_SUBMISSIONS);
		state.setAttribute(SORTED_BY, SORTED_SUBMISSION_BY_LASTNAME);
		state.setAttribute(SORTED_ASC, Boolean.TRUE.toString());

	} // doReport_submissions

	/**
	 *
	 *
	 */
	public void doAssignment_form(RunData data)
	{
		ParameterParser params = data.getParameters();
		//Added by Branden Visser: Grab the submission id from the query string
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		String actualGradeSubmissionId = (String) params.getString("submissionId");

	 	M_log.debug("actualGradeSubmissionId = {}", actualGradeSubmissionId);
		
		String option = (String) params.getString("option");
		String fromView = (String) state.getAttribute(FROM_VIEW);
		if (option != null)
		{
			if ("post".equals(option))
			{
				// post assignment
				doPost_assignment(data);
			}
			else if ("save".equals(option))
			{
				// save assignment
				doSave_assignment(data);
			}
			else if ("reorder".equals(option))
			{
				// reorder assignments
				doReorder_assignment(data);
			}
			else if ("preview".equals(option))
			{
				// preview assignment
				doPreview_assignment(data);
			}
			else if ("cancel".equals(option))
			{
				// cancel creating assignment
				doCancel_new_assignment(data);
			}
			else if ("canceledit".equals(option))
			{
				// cancel editing assignment
				doCancel_edit_assignment(data);
			}
			else if ("attach".equals(option))
			{
				// attachments
				doAttachmentsFrom(data, null);
			}
			else if ("removeAttachment".equals(option))
			{
				// remove selected attachment
				doRemove_attachment(data);
			}
			else if ("removeAttachment_review".equals(option))
			{
				// remove selected attachment
				doRemove_attachment(data);
				doSave_grade_submission_review(data);
			}
			else if ("modelAnswerAttach".equals(option))
			{
				doAttachmentsFrom(data, "modelAnswer");
			}
			else if ("allPurposeAttach".equals(option))
			{
				doAttachmentsFrom(data, "allPurpose");
			}
			else if ("view".equals(option))
			{
				// view
				doView(data);
			}
			else if ("permissions".equals(option))
			{
				// permissions
				doPermissions(data);
			}
			else if ("new".equals(option)) {
			    doNew_assignment(data,null);
			}
			else if ("returngrade".equals(option))
			{
					// return grading
					doReturn_grade_submission(data);
				}
			else if ("savegrade".equals(option))
			{
					// save grading
					doSave_grade_submission(data);
				}
			else if ("savegrade_review".equals(option))
			{
				// save review grading
				doSave_grade_submission_review(data);
			}else if("submitgrade_review".equals(option)){
				//we basically need to submit, save, and move the user to the next review (if available)
				if(data.getParameters().get("nextSubmissionId") != null){
					//go next
					doPrev_back_next_submission_review(data, "next", true);
				}else if(data.getParameters().get("prevSubmissionId") != null){
					//go previous
					doPrev_back_next_submission_review(data, "prev", true);
				}else{
					//go back to the list
					doPrev_back_next_submission_review(data, "back", true);
				}	
			}
			else if ("toggleremove_review".equals(option))
			{
				// save review grading
				doSave_toggle_remove_review(data);
			}
			else if ("previewgrade".equals(option))
			{
					// preview grading
					doPreview_grade_submission(data);
				}
			else if ("cancelgrade".equals(option))
			{
				// cancel grading
				doCancel_grade_submission(data);
			}
			else if ("cancelgrade_review".equals(option))
			{
				// cancel grade review
				// no need to do anything, session will have original values and refresh
			}
			else if ("cancelreorder".equals(option))
			{
				// cancel reordering
				doCancel_reorder(data);
			}
			else if ("sortbygrouptitle".equals(option))
			{
				// read input data
				setNewAssignmentParameters(data, true);

				// sort by group title
				doSortbygrouptitle(data);
			}
			else if ("sortbygroupdescription".equals(option))
			{
				// read input data
				setNewAssignmentParameters(data, true);

				// sort group by description
				doSortbygroupdescription(data);
			}
			else if ("hide_instruction".equals(option))
			{
				// hide the assignment instruction
				doHide_submission_assignment_instruction(data);
			}else if ("hide_instruction_review".equals(option))
			{
				// hide the assignment instruction
				doHide_submission_assignment_instruction_review(data);
			}
			else if ("show_instruction".equals(option))
			{
				// show the assignment instruction
				doShow_submission_assignment_instruction(data);
			}
			else if ("show_instruction_review".equals(option))
			{
				// show the assignment instruction
				doShow_submission_assignment_instruction_review(data);
			}
			else if ("sortbygroupdescription".equals(option))
			{
				// show the assignment instruction
				doShow_submission_assignment_instruction(data);
			}
			else if ("revise".equals(option) || "done".equals(option))
			{
				// back from the preview mode
				doDone_preview_new_assignment(data);
			}
			else if ("prevsubmission".equals(option))
			{
				// save and navigate to previous submission
				doPrev_back_next_submission(data, "prev");
			}
			else if ("nextsubmission".equals(option))
			{
				// save and navigate to previous submission
				doPrev_back_next_submission(data, "next");
			}
			else if (FLAG_NEXT_UNGRADED.equals(option) || FLAG_PREV_UNGRADED.equals(option))
			{
				// SAK-29314
				doPrev_back_next_submission(data, option);
			}
			else if ("prevsubmission_review".equals(option))
			{
				// save and navigate to previous submission
				doPrev_back_next_submission_review(data, "prev", false);
			}
			else if ("nextsubmission_review".equals(option))
			{
				// save and navigate to previous submission
				doPrev_back_next_submission_review(data, "next", false);
			}
			else if ("cancelgradesubmission".equals(option))
			{
				if (MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT.equals(fromView)) {
					doPrev_back_next_submission(data, "backListStudent");
				}
				else {
					// save and navigate to previous submission
					doPrev_back_next_submission(data, "back");
				}
			}
			else if ("cancelgradesubmission_review".equals(option))
			{
				// save and navigate to previous submission
				doPrev_back_next_submission_review(data, "back", false);
			}
			else if ("reorderNavigation".equals(option))
			{
				// save and do reorder
				doReorder(data);
			}
			else if ("options".equals(option))
			{
				// go to the options view
				doOptions(data);
			}

		}
	}
	
	// added by Branden Visser - Check that the state is consistent
	boolean checkSubmissionStateConsistency(SessionState state, String actualGradeSubmissionId) {
		String stateGradeSubmissionId = (String)state.getAttribute(GRADE_SUBMISSION_SUBMISSION_ID);
	 	M_log.debug("stateGradeSubmissionId = {}", stateGradeSubmissionId);
		boolean is_good = stateGradeSubmissionId.equals(actualGradeSubmissionId);
		if (!is_good) {
		    M_log.warn("State is inconsistent! Aborting grade save.");
		    addAlert(state, rb.getString("grading.alert.multiTab"));
		}
		return is_good;
	}

	/**
	 * Action is to use when doAattchmentsadding requested, corresponding to chef_Assignments-new "eventSubmit_doAattchmentsadding" when "add attachments" is clicked
	 */
	public void doAttachmentsFrom(RunData data, String from)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		doAttachments(data);
		
		// use the real attachment list
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			if (from != null && "modelAnswer".equals(from))
			{
				state.setAttribute(ATTACHMENTS_FOR, MODELANSWER_ATTACHMENTS);
				state.setAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS, state.getAttribute(MODELANSWER_ATTACHMENTS));
				state.setAttribute(MODELANSWER, Boolean.TRUE);
			}
			else if (from != null && "allPurpose".equals(from))
			{
				state.setAttribute(ATTACHMENTS_FOR, ALLPURPOSE_ATTACHMENTS);
				state.setAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS, state.getAttribute(ALLPURPOSE_ATTACHMENTS));
				state.setAttribute(ALLPURPOSE, Boolean.TRUE);
			}
			else if (from != null && "peerAttach".equals(from))
			{
				state.setAttribute(ATTACHMENTS_FOR, PEER_ATTACHMENTS);
				state.setAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS, state.getAttribute(PEER_ATTACHMENTS));
				state.setAttribute(PEER_ASSESSMENT, Boolean.TRUE);
			}
		}
	}
	
	/**
	 * put supplement item attachment info into state
	 * @param state
	 * @param item
	 * @param attachmentsKind
	 */
	private void putSupplementItemAttachmentInfoIntoState(SessionState state, AssignmentSupplementItemWithAttachment item, String attachmentsKind)
	{
		List refs = new ArrayList();
		
		if (item != null)
		{
			// get reference list
			Set<AssignmentSupplementItemAttachment> aSet = item.getAttachmentSet();
			if (aSet != null && aSet.size() > 0)
			{
				for(Iterator<AssignmentSupplementItemAttachment> aIterator = aSet.iterator(); aIterator.hasNext();)
				{
					AssignmentSupplementItemAttachment att = aIterator.next();
					// add reference
					refs.add(EntityManager.newReference(att.getAttachmentId()));
				}
				state.setAttribute(attachmentsKind, refs);
			}
		}
	}
	
	/**
	 * put supplement item attachment state attribute value into context
	 * @param state
	 * @param context
	 * @param attachmentsKind
	 */
	private void putSupplementItemAttachmentStateIntoContext(SessionState state, Context context, String attachmentsKind)
	{
		List refs = new ArrayList();
		
		String attachmentsFor = (String) state.getAttribute(ATTACHMENTS_FOR);
		if (attachmentsFor != null && attachmentsFor.equals(attachmentsKind))
		{
			ToolSession session = SessionManager.getCurrentToolSession();
		    if (session.getAttribute(FilePickerHelper.FILE_PICKER_CANCEL) == null &&
		        session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS) != null) 
		    {
		    	refs = (List)session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
		    	// set the correct state variable
		    	state.setAttribute(attachmentsKind, refs);
		    }
		    session.removeAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
		    session.removeAttribute(FilePickerHelper.FILE_PICKER_CANCEL);

		    state.removeAttribute(ATTACHMENTS_FOR);
		}
		
		// show attachments content
		if (state.getAttribute(attachmentsKind) != null)
		{
			context.put(attachmentsKind, state.getAttribute(attachmentsKind));
		}
		
		// this is to keep the proper node div open
		context.put("attachments_for", attachmentsKind);
	}
	
	/**
	 * Action is to use when doAattchmentsadding requested, corresponding to chef_Assignments-new "eventSubmit_doAattchmentsadding" when "add attachments" is clicked
	 */
	public void doAttachments(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();

		// determines if the file picker can only add a single attachment
		boolean singleAttachment = false;

		// when content-review is enabled, the inline text will have an associated attachment. It should be omitted from the file picker
		Assignment assignment = null;
		boolean omitInlineAttachments = false;

		String mode = (String) state.getAttribute(STATE_MODE);
		if (MODE_STUDENT_VIEW_SUBMISSION.equals(mode))
		{
			// save the current input before leaving the page
			saveSubmitInputs(state, params);
			
			// Restrict file picker configuration if using content-review (Turnitin):
			String assignmentRef = (String) state.getAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE);
			try
			{
				assignment = AssignmentService.getAssignment(assignmentRef);
				if (assignment.getContent().getAllowReviewService())
				{
					state.setAttribute(FilePickerHelper.FILE_PICKER_MAX_ATTACHMENTS, FilePickerHelper.CARDINALITY_MULTIPLE);
					state.setAttribute(FilePickerHelper.FILE_PICKER_SHOW_URL, Boolean.FALSE);
				}

				if (assignment.getContent().getTypeOfSubmission() == Assignment.SINGLE_ATTACHMENT_SUBMISSION)
				{
					singleAttachment = true;
				}
			}
			catch ( IdUnusedException e )
			{
				addAlert(state, rb.getFormattedMessage("cannotfin_assignment", new Object[]{assignmentRef}));
			}
			catch ( PermissionException e )
			{
				addAlert(state, rb.getFormattedMessage("youarenot_viewAssignment", new Object[]{assignmentRef}));
			}
			
			// need also to upload local file if any
			doAttachUpload(data, false);
			
			// TODO: file picker to save in dropbox? -ggolden
			// User[] users = { UserDirectoryService.getCurrentUser() };
			// state.setAttribute(ResourcesAction.STATE_SAVE_ATTACHMENT_IN_DROPBOX, users);

			// Always omit inline attachments. Even if content-review is not enabled, 
			// this could be a resubmission to an assignment that was previously content-review enabled, 
			// in which case the file will be present and should be omitted.
			omitInlineAttachments = true;
		}
		else if (MODE_INSTRUCTOR_NEW_EDIT_ASSIGNMENT.equals(mode))
		{
			setNewAssignmentParameters(data, false);
		}
		else if (MODE_INSTRUCTOR_GRADE_SUBMISSION.equals(mode))
		{
			readGradeForm(data, state, "read");
		}
		else if (MODE_STUDENT_REVIEW_EDIT.equals(mode)) {
			saveReviewGradeForm(data, state, "save");
		}

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			// get into helper mode with this helper tool
			startHelper(data.getRequest(), "sakai.filepicker");

			if (singleAttachment)
			{
				// SAK-27595 - added a resources file picker for single uploaded file only assignments; we limit it here to accept a maximum of 1 file
				state.setAttribute(FilePickerHelper.FILE_PICKER_MAX_ATTACHMENTS, Integer.valueOf(1));
				state.setAttribute(FilePickerHelper.FILE_PICKER_TITLE_TEXT, rb.getString("gen.addatttoassig.singular"));
			}
			else
			{
				state.setAttribute(FilePickerHelper.FILE_PICKER_TITLE_TEXT, rb.getString("gen.addatttoassig"));
			}
			state.setAttribute(FilePickerHelper.FILE_PICKER_INSTRUCTION_TEXT, rb.getString("gen.addatttoassiginstr"));

			// process existing attachments
			List attachments = (List) state.getAttribute(ATTACHMENTS);
			if (singleAttachment && attachments != null && attachments.size() > 1)
			{
				// multiple attachments -> Single Uploaded File Only
				List newSingleAttachmentList = EntityManager.newReferenceList();
				state.setAttribute("newSingleAttachmentList", newSingleAttachmentList);
				state.setAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS, newSingleAttachmentList);
			}
			else
			{
				// use the real attachment list
				// but omit the inline submission under conditions determined in the logic above
				if (omitInlineAttachments && assignment != null)
				{
					attachments = getNonInlineAttachments(state, assignment);
					state.setAttribute(ATTACHMENTS, attachments);
				}
				state.setAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS, attachments);
			}
		}
	}

	/**
	 * saves the current input before navigating off to other pages
	 * @param state
	 * @param params
	 */
	private void saveSubmitInputs(SessionState state,
			ParameterParser params) {
		// retrieve the submission text (as formatted text)
		boolean checkForFormattingErrors = true; // the student is submitting something - so check for errors
		String text = processFormattedTextFromBrowser(state, params.getCleanString(VIEW_SUBMISSION_TEXT),
				checkForFormattingErrors);

		state.setAttribute(VIEW_SUBMISSION_TEXT, text);
		if (params.getString(VIEW_SUBMISSION_HONOR_PLEDGE_YES) != null)
		{
			state.setAttribute(VIEW_SUBMISSION_HONOR_PLEDGE_YES, "true");
		}

		String assignmentRef = (String) state.getAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE);
		try {
		    Assignment assignment = AssignmentService.getAssignment(assignmentRef);
		    if (assignment.isGroup()) {
		        String[] groupChoice = params.getStrings("selectedGroups");
		        if (groupChoice != null && groupChoice.length != 0) 
		        {
		            if (groupChoice.length > 1) {
		                state.setAttribute(VIEW_SUBMISSION_GROUP, null);
		                addAlert(state, rb.getString("java.alert.youchoosegroup"));
		            } else {
		                state.setAttribute(VIEW_SUBMISSION_GROUP, groupChoice[0]);
		            }
		        }
		        else
		        {
		            state.setAttribute(VIEW_SUBMISSION_GROUP, null);
		            addAlert(state, rb.getString("java.alert.youchoosegroup"));
		        }
		        String original_group_id = params.getString("originalGroup") == null 
		                || params.getString("originalGroup").trim().length() == 0 ? null: params.getString("originalGroup");

		        if (original_group_id != null) {
		            state.setAttribute(VIEW_SUBMISSION_ORIGINAL_GROUP, original_group_id);
		        } else {
		            state.setAttribute(VIEW_SUBMISSION_ORIGINAL_GROUP, null);
		        }
		    }

		} catch (PermissionException p) {
		    M_log.debug(this + " :saveSubmitInputs permission error getting assignment. ");
		} catch ( IdUnusedException e ) {}
	}



	/**
	 * read review grade information form and see if any grading information has been changed
	 * @param data
	 * @param state
	 * @param gradeOption
	 * @return
	 */
	public boolean saveReviewGradeForm(RunData data, SessionState state, String gradeOption){
		String assessorUserId = UserDirectoryService.getCurrentUser().getId();
		if(state.getAttribute(PEER_ASSESSMENT_ASSESSOR_ID) != null && !assessorUserId.equals(state.getAttribute(PEER_ASSESSMENT_ASSESSOR_ID))){
			//this is only set during the read only view, so just return
			return false;
		}
		ParameterParser params = data.getParameters();
		String submissionRef = params.getString("submissionId");
		String submissionId = null;
		if(submissionRef != null){
			int i = submissionRef.lastIndexOf(Entity.SEPARATOR);
			if (i == -1){
				submissionId = submissionRef;
			}else{
				submissionId = submissionRef.substring(i + 1);
			}
		}
		if(submissionId != null){
			
			//call the DB to make sure this user can edit this assessment, otherwise it wouldn't exist
			PeerAssessmentItem item = assignmentPeerAssessmentService.getPeerAssessmentItem(submissionId, assessorUserId);
			if(item != null){
				//find the original assessment item and compare to see if it has changed
				//if so, save it
				boolean changed = false;

				if(submissionId.equals(item.getSubmissionId())
						&& assessorUserId.equals(item.getAssessorUserId())){
					//Grade
					String g = StringUtils.trimToNull(params.getCleanString(GRADE_SUBMISSION_GRADE));
					Integer score = item.getScore();
					if(g != null && !"".equals(g)){
						try{
							String assignmentId = (String) state.getAttribute(VIEW_ASSIGNMENT_ID);
							if (assignmentId == null) {
								addAlert(state, rb.getString("peerassessment.alert.saveerrorunkown"));
							}
							else {
								Assignment a = getAssignment(assignmentId, "saveReviewGradeForm", state);
								if (a == null) {
									addAlert(state, rb.getString("peerassessment.alert.saveerrorunkown"));
								}
								else {
									int factor = a.getContent().getFactor();
									int dec = (int)Math.log10(factor);
									String decSeparator = FormattedText.getDecimalSeparator();
									g = StringUtils.replace(g, (",".equals(decSeparator)?".":","), decSeparator);
									NumberFormat nbFormat = FormattedText.getNumberFormat(dec,dec,false);
									DecimalFormat dcformat = (DecimalFormat) nbFormat;
									Double dScore = dcformat.parse(g).doubleValue();
									
									if(dScore < 0) {
										addAlert(state, rb.getString("peerassessment.alert.saveinvalidscore"));
									}
									else if(dScore <= a.getContent().getMaxGradePoint()/(double)factor) {
										//scores are saved as whole values
										//so a score of 1.3 would be stored as 13
										score = (int) Math.round(dScore * factor);
									}
									else {
										addAlert(state, rb.getFormattedMessage("plesuse4", new Object[]{g, a.getContent().getMaxGradePoint()/(double)factor}));
									}
								}
							}
						}catch(Exception e){
							addAlert(state, rb.getString("peerassessment.alert.saveinvalidscore"));
						}
					}
					boolean scoreChanged = false;
					if(score != null && item.getScore() == null
							|| score == null && item.getScore() != null
							|| (score != null && item.getScore() != null && !score.equals(item.getScore()))){
						//Score changed
						changed = true;
						scoreChanged = true;
						item.setScore(score);
					}

					//Comment:
					boolean checkForFormattingErrors = true;
					String feedbackComment = processFormattedTextFromBrowser(state, params.getCleanString(GRADE_SUBMISSION_FEEDBACK_COMMENT),
							checkForFormattingErrors);
					if(feedbackComment != null && item.getComment() == null
							|| feedbackComment == null && item.getComment() != null
							|| (feedbackComment != null && item.getComment() != null && !feedbackComment.equals(item.getComment()))){
						//comment changed
						changed = true;
						item.setComment(feedbackComment);
					}

					/** Attachments **/
					//Get attachments already added to this item
					List<PeerAssessmentAttachment> savedAttachments = assignmentPeerAssessmentService.getPeerAssessmentAttachments(submissionId,assessorUserId);

					// get attachments added to the review form
					List submittedAttachmentRefs = null;

					if(state.getAttribute(PEER_ATTACHMENTS) != null && !((List) state.getAttribute(PEER_ATTACHMENTS)).isEmpty()) {
						submittedAttachmentRefs = (List) state.getAttribute(PEER_ATTACHMENTS);
					}

					boolean attachmentsChanged = false;
					// if review was saved/submitted with attachments added
					if (submittedAttachmentRefs != null && !submittedAttachmentRefs.isEmpty())
					{
						// build set of attachment reference ids from review form
						List<PeerAssessmentAttachment> attachmentsFromForm = new ArrayList<>();
						for(Object attachment : submittedAttachmentRefs) {
							// Try to get existing attachment first
							PeerAssessmentAttachment peerAssessmentAttachment = assignmentPeerAssessmentService.getPeerAssessmentAttachment(item.getSubmissionId(), item.getAssessorUserId(), ((Reference) attachment).getId());
							if (peerAssessmentAttachment != null) {
								attachmentsFromForm.add(peerAssessmentAttachment);
							} else {
								// Build a new attachment
								peerAssessmentAttachment = new PeerAssessmentAttachment(item.getSubmissionId(), item.getAssessorUserId(), ((Reference) attachment).getId());
								attachmentsFromForm.add(peerAssessmentAttachment);
							}
						}

						// check if there were previously saved attachments
						if (savedAttachments != null && !savedAttachments.isEmpty()) {
							// Find if any previously saved attachments need to be removed from item's attachments
							List<PeerAssessmentAttachment> attachmentsToDelete = new ArrayList<>(savedAttachments);
							attachmentsToDelete.removeAll(attachmentsFromForm);

							// remove no longer needed attachments from peer review item's attachments
							if (!attachmentsToDelete.isEmpty()) {
								// remove previously saved attachments
								attachmentsToDelete.forEach(assignmentPeerAssessmentService::removePeerAttachment);
							}
						}
						item.setAttachmentList(attachmentsFromForm);
						attachmentsChanged = true;
					} else {
						// no attachments added to the review so we need to clear out previously saved attachments
						if (savedAttachments != null && !savedAttachments.isEmpty()) {
							savedAttachments.forEach(assignmentPeerAssessmentService::removePeerAttachment);
							attachmentsChanged = true;
						}
						item.setAttachmentList(new ArrayList<>());
					}

					//Submitted
					if("submit".equals(gradeOption)){
						if(item.getScore() != null || (item.getComment() != null && !"".equals(item.getComment().trim()))){
							item.setSubmitted(true);
							changed = true;
						}else{
							addAlert(state, rb.getString("peerassessment.alert.savenoscorecomment"));
						}
					}
					if(("submit".equals(gradeOption) || "save".equals(gradeOption)) && state.getAttribute(STATE_MESSAGE) == null){					
						if(changed){
							//save this in the DB
							assignmentPeerAssessmentService.savePeerAssessmentItem(item);
							if(scoreChanged){
								//need to re-calcuate the overall score:
								boolean saved = assignmentPeerAssessmentService.updateScore(submissionId);
								if(saved){
									//we need to make sure the GB is updated correctly (or removed)
									String assignmentId = (String) state.getAttribute(VIEW_ASSIGNMENT_ID);
									if(assignmentId != null){
										Assignment a = getAssignment(assignmentId, "saveReviewGradeForm", state);
										if(a != null){
											String aReference = a.getReference();
											String associateGradebookAssignment = StringUtils.trimToNull(a.getProperties().getProperty(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT));
											// update grade in gradebook
											integrateGradebook(state, aReference, associateGradebookAssignment, null, null, null, -1, null, submissionId, "update", -1);
										}
									}
								}
							}
							state.setAttribute(GRADE_SUBMISSION_DONE, Boolean.TRUE);
							if("submit".equals(gradeOption)){
								state.setAttribute(GRADE_SUBMISSION_SUBMIT, Boolean.TRUE);
							}
						}
						if(attachmentsChanged) {
							//save new attachments to the DB
							assignmentPeerAssessmentService.savePeerAssessmentAttachments(item);
						}
					}
					
					//update session state:
					List<PeerAssessmentItem> peerAssessmentItems = (List<PeerAssessmentItem>) state.getAttribute(PEER_ASSESSMENT_ITEMS);
					if(peerAssessmentItems != null){
						for(int i = 0; i < peerAssessmentItems.size(); i++) {
							PeerAssessmentItem sItem = peerAssessmentItems.get(i);
							if(sItem.getSubmissionId().equals(item.getSubmissionId())
									&& sItem.getAssessorUserId().equals(item.getAssessorUserId())){
								//found it, just update it
								peerAssessmentItems.set(i, item);
								state.setAttribute(PEER_ASSESSMENT_ITEMS, peerAssessmentItems);
								break;
							}
						}
					}
					
				}
				
				return changed;
			}else{
				addAlert(state, rb.getString("peerassessment.alert.saveerrorunkown"));
			}
		}else{
			addAlert(state, rb.getString("peerassessment.alert.saveerrorunkown"));

		}
		return false;
	}
	
	/**
	 * read grade information form and see if any grading information has been changed
	 * @param data
	 * @param state
	 * @param gradeOption
	 * @return
	 */
	public boolean readGradeForm(RunData data, SessionState state, String gradeOption)
	{
		// whether user has changed anything from previous grading information
		boolean hasChange = false;
		
		ParameterParser params = data.getParameters();
		String sId = params.getString("submissionId");
		
		//Added by Branden Visser - Check that the state is consistent
		if (!checkSubmissionStateConsistency(state, sId)) {
			return false;
		}
		
		AssignmentSubmission submission = getSubmission(sId, "readGradeForm", state);
		
		// security check for allowing grading submission or not
		if (AssignmentService.allowGradeSubmission(sId))
		{
			int typeOfGrade = -1;
			boolean withGrade = state.getAttribute(WITH_GRADES) != null ? ((Boolean) state.getAttribute(WITH_GRADES)).booleanValue()
					: false;
			
			boolean checkForFormattingErrors = true; // so that grading isn't held up by formatting errors
			String feedbackComment = processFormattedTextFromBrowser(state, params.getCleanString(GRADE_SUBMISSION_FEEDBACK_COMMENT),
					checkForFormattingErrors);
			// comment value changed?
			hasChange = !hasChange && submission != null ? valueDiffFromStateAttribute(state, feedbackComment, submission.getFeedbackComment()):hasChange;
			if (feedbackComment != null)
			{
				state.setAttribute(GRADE_SUBMISSION_FEEDBACK_COMMENT, feedbackComment);
			}
			

			String feedbackText = processAssignmentFeedbackFromBrowser(state, params.getCleanString(GRADE_SUBMISSION_FEEDBACK_TEXT));
			// feedbackText value changed?
			hasChange = !hasChange && submission != null ? valueDiffFromStateAttribute(state, feedbackText, submission.getFeedbackText()):hasChange;
			if (feedbackText != null)
			{
				state.setAttribute(GRADE_SUBMISSION_FEEDBACK_TEXT, feedbackText);
			}
			
			// any change inside attachment list?
			if (!hasChange && submission != null)
			{
				List stateAttachments = submission.getFeedbackAttachments();
				List inputAttachments = (List) state.getAttribute(ATTACHMENTS);
				
				if (stateAttachments == null && inputAttachments != null
					|| stateAttachments != null && inputAttachments == null	
					|| stateAttachments != null && inputAttachments != null && !(stateAttachments.containsAll(inputAttachments) && inputAttachments.containsAll(stateAttachments)))
				{
					hasChange = true;
				}
			}
			state.setAttribute(GRADE_SUBMISSION_FEEDBACK_ATTACHMENT, state.getAttribute(ATTACHMENTS));

			String g = StringUtils.trimToNull(params.getCleanString(GRADE_SUBMISSION_GRADE));

			if (submission != null)
			{
				Assignment a = submission.getAssignment();
				int factor = a.getContent().getFactor();
				typeOfGrade = a.getContent().getTypeOfGrade();
	
				if (withGrade)
				{
					// any change in grade. Do not check for ungraded assignment type
					if (!hasChange && typeOfGrade != Assignment.UNGRADED_GRADE_TYPE)
					{
						if (typeOfGrade == Assignment.SCORE_GRADE_TYPE)
						{
							String currentGrade = submission.getGrade();
							
							String decSeparator = FormattedText.getDecimalSeparator();
							
							if (currentGrade != null && currentGrade.indexOf(decSeparator) != -1)
							{
								currentGrade =  scalePointGrade(state, submission.getGrade(), factor);
							}
							hasChange = valueDiffFromStateAttribute(state, scalePointGrade(state, g, factor), currentGrade);
						}
						else
						{
							hasChange = valueDiffFromStateAttribute(state, g, submission.getGrade());
						}
					}
					if (g != null)
					{
						state.setAttribute(GRADE_SUBMISSION_GRADE, g);
					}
					else
					{
						state.removeAttribute(GRADE_SUBMISSION_GRADE);
					}
					
					// for points grading, one have to enter number as the points
					String grade = (String) state.getAttribute(GRADE_SUBMISSION_GRADE);
					
					// do grade validation only for Assignment with Grade tool
					if (typeOfGrade == Assignment.SCORE_GRADE_TYPE)
					{
						if ((grade != null))
						{
							// the preview grade process might already scaled up the grade by "factor"
							if (!((String) state.getAttribute(STATE_MODE)).equals(MODE_INSTRUCTOR_PREVIEW_GRADE_SUBMISSION))
							{
								if (state.getAttribute(STATE_MESSAGE) == null)
								{
									validPointGrade(state, grade, factor);
									int maxGrade = a.getContent().getMaxGradePoint();
									try
									{
										if (Integer.parseInt(scalePointGrade(state, grade, factor)) > maxGrade)
										{
											if (state.getAttribute(GRADE_GREATER_THAN_MAX_ALERT) == null)
											{
												// alert user first when he enters grade bigger than max scale
												addAlert(state, rb.getFormattedMessage("grad2", new Object[]{grade, displayGrade(state, String.valueOf(maxGrade), factor)}));
												state.setAttribute(GRADE_GREATER_THAN_MAX_ALERT, Boolean.TRUE);
											}
											else
											{
												// remove the alert once user confirms he wants to give student higher grade
												state.removeAttribute(GRADE_GREATER_THAN_MAX_ALERT);
											}
										}
									}
									catch (NumberFormatException e)
									{
										alertInvalidPoint(state, grade, factor);
										M_log.warn(this + ":readGradeForm " + e.getMessage());
									}
								}
								
								state.setAttribute(GRADE_SUBMISSION_GRADE, grade);
							}
						}
					}
	
					// if ungraded and grade type is not "ungraded" type
					if ((grade == null || "ungraded".equals(grade)) && (typeOfGrade != Assignment.UNGRADED_GRADE_TYPE) && "release".equals(gradeOption))
					{
						addAlert(state, rb.getString("plespethe2"));
					}

					// check for grade overrides
					if (a.isGroup()) {
					    User[] _users = submission.getSubmitters();
					    HashMap<String,String> scaledValues = new HashMap<String,String>();
					    for (int i=0; _users != null && i < _users.length; i++) {
					        String ug = StringUtil.trimToNull(params.getCleanString(GRADE_SUBMISSION_GRADE + "_" + _users[i].getId()));
					        if ("null".equals(ug)) ug = null;
					        if (!hasChange && typeOfGrade != Assignment.UNGRADED_GRADE_TYPE) {
					                hasChange = valueDiffFromStateAttribute(state, ug, submission.getGradeForUser(_users[i].getId()));
					        }
					        if (ug == null) {
					            state.removeAttribute(GRADE_SUBMISSION_GRADE + "_" + _users[i].getId());
					        } else {
					            state.setAttribute(GRADE_SUBMISSION_GRADE + "_" + _users[i].getId(), ug);
					        }
					        // for points grading, one have to enter number as the points
					        String ugrade = (String) state.getAttribute(GRADE_SUBMISSION_GRADE + "_" + _users[i].getId());
					        // do grade validation only for Assignment with Grade tool
					        if (typeOfGrade == Assignment.SCORE_GRADE_TYPE) {
					            if (ugrade != null && !(ugrade.equals("null"))) {
					                // the preview grade process might already scaled up the grade by "factor"
					                if (!((String) state.getAttribute(STATE_MODE)).equals(MODE_INSTRUCTOR_PREVIEW_GRADE_SUBMISSION)) {
					                    validPointGrade(state, ugrade, factor);
					                    if (state.getAttribute(STATE_MESSAGE) == null) {
					                        int maxGrade = a.getContent().getMaxGradePoint();
					                        try {
					                            if (Integer.parseInt(scalePointGrade(state, ugrade, factor)) > maxGrade) {
					                                if (state.getAttribute(GRADE_GREATER_THAN_MAX_ALERT) == null) {
					                                    // alert user first when he enters grade bigger than max scale
					                                    addAlert(state, rb.getFormattedMessage("grad2", new Object[]{ugrade, displayGrade(state, String.valueOf(maxGrade), factor)}));
					                                    state.setAttribute(GRADE_GREATER_THAN_MAX_ALERT, Boolean.TRUE);
					                                } else {
					                                    // remove the alert once user confirms he wants to give student higher grade
					                                    state.removeAttribute(GRADE_GREATER_THAN_MAX_ALERT);
					                                }
					                            }
					                        } catch (NumberFormatException e) {
					                            alertInvalidPoint(state, ugrade, factor);
					                            M_log.warn(this + ":readGradeForm User " + e.getMessage());
					                        }
					                    }
					                    scaledValues.put(GRADE_SUBMISSION_GRADE + "_" + _users[i].getId(), scalePointGrade(state, ugrade, factor));
					                }
					            }
					        }
					    }
					    // SAK-28182 If all grades are right place scaled values in state
					    if (state.getAttribute(STATE_MESSAGE) == null) {
					    	for (Map.Entry<String,String> entry:scaledValues.entrySet()) {
					    		state.setAttribute(entry.getKey(),entry.getValue());
					    	}
					    }
					}

				}
				
				// allow resubmit number and due time
				if (params.getString("allowResToggle") != null && params.getString(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER) != null)
				{
					// read in allowResubmit params 
					readAllowResubmitParams(params, state, submission);
				}
				else 
				{
					state.removeAttribute(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME);
					state.removeAttribute(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER);
					
					if (!"read".equals(gradeOption))
					{
						resetAllowResubmitParams(state);
					}
				}
				// record whether the resubmission options has been changed or not
				hasChange = hasChange || change_resubmit_option(state, submission);

				if (state.getAttribute(STATE_MESSAGE) == null)
				{
					String grade = (String) state.getAttribute(GRADE_SUBMISSION_GRADE);
					grade = (typeOfGrade == Assignment.SCORE_GRADE_TYPE)?scalePointGrade(state, grade, factor):grade;
					state.setAttribute(GRADE_SUBMISSION_GRADE, grade);
				}
			}
		}
		else
		{
			// generate alert
			addAlert(state, rb.getFormattedMessage("not_allowed_to_grade_submission", new Object[]{sId}));
		}
		
		return hasChange;
	}
	
	/**
	 * whether the current input value is different from existing oldValue
	 * @param state
	 * @param value
	 * @param oldValue
	 * @return
	 */
	private boolean valueDiffFromStateAttribute(SessionState state, String value, String oldValue)
	{
		boolean rv = false;
		value = StringUtils.trimToNull(value);
		oldValue = StringUtils.trimToNull(oldValue);
		if (oldValue == null && value != null 
				|| oldValue != null && value == null
				|| oldValue != null && value != null && 
				!normalizeAttributeSpaces(oldValue).equals(normalizeAttributeSpaces(value)))
		{
			rv = true;
		}
		return rv;
	}
	
	/**
	Remove extraneous spaces between tag attributes, to allow a better
	equality test in valueDiffFromStateAttribute.
	@param the input string, to be normalized
	@return the normalized string.
	*/
	String normalizeAttributeSpaces(String s) {
		if (s == null) 
			return s;
		Pattern p = Pattern.compile("(=\".*?\")( +)");
		Matcher m = p.matcher(s);
		String c = m.replaceAll("$1 ");
		return c;
	}
	
	/**
	 * read in the resubmit parameters into state variables
	 * @param params
	 * @param state
	 * @return the time set for the resubmit close OR null if it is not set
	 */
	protected Time readAllowResubmitParams(ParameterParser params, SessionState state, Entity entity)
	{
	    Time resubmitCloseTime = null;
		String allowResubmitNumberString = params.getString(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER);
		state.setAttribute(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER, params.getString(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER));
	
		if (allowResubmitNumberString != null && Integer.parseInt(allowResubmitNumberString) != 0)
		{
			int closeMonth = (Integer.valueOf(params.getString(ALLOW_RESUBMIT_CLOSEMONTH))).intValue();
			state.setAttribute(ALLOW_RESUBMIT_CLOSEMONTH, Integer.valueOf(closeMonth));
			int closeDay = (Integer.valueOf(params.getString(ALLOW_RESUBMIT_CLOSEDAY))).intValue();
			state.setAttribute(ALLOW_RESUBMIT_CLOSEDAY, Integer.valueOf(closeDay));
			int closeYear = (Integer.valueOf(params.getString(ALLOW_RESUBMIT_CLOSEYEAR))).intValue();
			state.setAttribute(ALLOW_RESUBMIT_CLOSEYEAR, Integer.valueOf(closeYear));
			int closeHour = (Integer.valueOf(params.getString(ALLOW_RESUBMIT_CLOSEHOUR))).intValue();
			state.setAttribute(ALLOW_RESUBMIT_CLOSEHOUR, Integer.valueOf(closeHour));
			int closeMin = (Integer.valueOf(params.getString(ALLOW_RESUBMIT_CLOSEMIN))).intValue();
			state.setAttribute(ALLOW_RESUBMIT_CLOSEMIN, Integer.valueOf(closeMin));
			resubmitCloseTime = TimeService.newTimeLocal(closeYear, closeMonth, closeDay, closeHour, closeMin, 0, 0);
			state.setAttribute(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME, String.valueOf(resubmitCloseTime.getTime()));
			// no need to show alert if the resubmission setting has not changed
			if (entity == null || change_resubmit_option(state, entity))
			{
				// validate date
				if (resubmitCloseTime.before(TimeService.newTime()) && state.getAttribute(NEW_ASSIGNMENT_PAST_CLOSE_DATE) == null)
				{
					state.setAttribute(NEW_ASSIGNMENT_PAST_CLOSE_DATE, Boolean.TRUE);
				}
				else
				{
					// clean the attribute after user confirm
					state.removeAttribute(NEW_ASSIGNMENT_PAST_CLOSE_DATE);
				}
				if (state.getAttribute(NEW_ASSIGNMENT_PAST_CLOSE_DATE) != null)
				{
					addAlert(state, rb.getString("acesubdea5"));
				}
				if (!Validator.checkDate(closeDay, closeMonth, closeYear))
				{
					addAlert(state, rb.getFormattedMessage("date.invalid", new Object[]{rb.getString("date.resubmission.closedate")}));
				}
			}
		}
		else
		{
			// reset the state attributes
			resetAllowResubmitParams(state);
		}
		return resubmitCloseTime;
	}
	
	protected void resetAllowResubmitParams(SessionState state)
	{
		state.setAttribute(ALLOW_RESUBMIT_CLOSEMONTH,state.getAttribute(NEW_ASSIGNMENT_DUEMONTH));
		state.setAttribute(ALLOW_RESUBMIT_CLOSEDAY,state.getAttribute(NEW_ASSIGNMENT_DUEDAY));
		state.setAttribute(ALLOW_RESUBMIT_CLOSEYEAR,state.getAttribute(NEW_ASSIGNMENT_DUEYEAR));
		state.setAttribute(ALLOW_RESUBMIT_CLOSEHOUR,state.getAttribute(NEW_ASSIGNMENT_DUEHOUR));
		state.setAttribute(ALLOW_RESUBMIT_CLOSEMIN,state.getAttribute(NEW_ASSIGNMENT_DUEMIN));
		state.removeAttribute(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME);
		state.removeAttribute(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER);
	}

	/**
	 * Populate the state object, if needed - override to do something!
	 */
	protected void initState(SessionState state, VelocityPortlet portlet, JetspeedRunData data)
	{
		super.initState(state, portlet, data);
		
		if (m_contentHostingService == null)
		{
			m_contentHostingService = (ContentHostingService) ComponentManager.get("org.sakaiproject.content.api.ContentHostingService");
		}

		if (m_entityManager == null)
		{
			m_entityManager = (org.sakaiproject.entity.api.EntityManager) ComponentManager.get("org.sakaiproject.entity.api.EntityManager");
		}

		if (m_assignmentSupplementItemService == null)
		{
			m_assignmentSupplementItemService = (AssignmentSupplementItemService) ComponentManager.get("org.sakaiproject.assignment.api.model.AssignmentSupplementItemService");
		}
		
		if (m_eventTrackingService == null)
		{
			m_eventTrackingService = (EventTrackingService) ComponentManager.get("org.sakaiproject.event.api.EventTrackingService");
		}
		
		if (m_notificationService == null)
		{
			m_notificationService = (NotificationService) ComponentManager.get("org.sakaiproject.event.api.NotificationService");
		}
		
		if (m_securityService == null)
		{
			m_securityService = (SecurityService) ComponentManager.get("org.sakaiproject.authz.api.SecurityService");
		}
		if(assignmentPeerAssessmentService == null){
			assignmentPeerAssessmentService = (AssignmentPeerAssessmentService) ComponentManager.get("org.sakaiproject.assignment.api.AssignmentPeerAssessmentService");
		}

		if (authzGroupService == null) {
			authzGroupService = ComponentManager.get(AuthzGroupService.class);
		}
		

		String siteId = ToolManager.getCurrentPlacement().getContext();

		// show the list of assignment view first
		if (state.getAttribute(STATE_SELECTED_VIEW) == null)
		{
			state.setAttribute(STATE_SELECTED_VIEW, MODE_LIST_ASSIGNMENTS);
		}

		if (state.getAttribute(STATE_USER) == null)
		{
			state.setAttribute(STATE_USER, UserDirectoryService.getCurrentUser());
		}

		/** The content type image lookup service in the State. */
		ContentTypeImageService iService = (ContentTypeImageService) state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE);
		if (iService == null)
		{
			iService = org.sakaiproject.content.cover.ContentTypeImageService.getInstance();
			state.setAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE, iService);
		} // if

		if (state.getAttribute(SUBMISSIONS_SEARCH_ONLY) == null)
		{
			String propValue = null;
			// save the option into tool configuration
			try {
				Site site = SiteService.getSite(siteId);
				ToolConfiguration tc=site.getToolForCommonId(ASSIGNMENT_TOOL_ID);
				propValue = tc.getPlacementConfig().getProperty(SUBMISSIONS_SEARCH_ONLY);
			}
			catch (IdUnusedException e)
			{
				M_log.warn(this + ":init()  Cannot find site with id " + siteId);
			}
			state.setAttribute(SUBMISSIONS_SEARCH_ONLY, propValue == null ? Boolean.FALSE:Boolean.valueOf(propValue));
		}

		/** The calendar tool  */
		if (state.getAttribute(CALENDAR_TOOL_EXIST) == null)
		{
			CalendarService cService = org.sakaiproject.calendar.cover.CalendarService.getInstance();
			if (cService != null){
				if (!siteHasTool(siteId, cService.getToolId()))
				{
					state.setAttribute(CALENDAR_TOOL_EXIST, Boolean.FALSE);
					state.removeAttribute(CALENDAR);
				}
				else
				{
					state.setAttribute(CALENDAR_TOOL_EXIST, Boolean.TRUE);
					if (state.getAttribute(CALENDAR) == null )
					{
						state.setAttribute(CALENDAR_TOOL_EXIST, Boolean.TRUE);
						state.setAttribute(STATE_CALENDAR_SERVICE, cService);
		
						String calendarId = ServerConfigurationService.getString("calendar", null);
						if (calendarId == null)
						{
							calendarId = cService.calendarReference(siteId, SiteService.MAIN_CONTAINER);
							try
							{
								state.setAttribute(CALENDAR, cService.getCalendar(calendarId));
							}
							catch (IdUnusedException e)
							{
								state.removeAttribute(CALENDAR);
								M_log.info(this + ":initState No calendar found for site " + siteId  + " " + e.getMessage());
							}
							catch (PermissionException e)
							{
								state.removeAttribute(CALENDAR);
								M_log.info(this + ":initState No permission to get the calender. " + e.getMessage());
							}
							catch (Exception ex)
							{
								state.removeAttribute(CALENDAR);
								M_log.info(this + ":initState Assignment : Action : init state : calendar exception : " + ex.getMessage());
								
							}
						}
					}
				}
			}
		}
			
		/** Additional Calendar tool */ 
		// Setting this attribute to true or false currently makes no difference as it is never checked for true or false.
		// true: means the additional calendar is ready to be used with assignments.
		// false: means the tool may not be deployed at all or may be at the site but not ready to be used.
		if (state.getAttribute(ADDITIONAL_CALENDAR_TOOL_READY) == null)
		{
			// Get a handle to the Google calendar service class from the Component Manager. It will be null if not deployed.
			CalendarService additionalCalendarService = (CalendarService)ComponentManager.get(CalendarService.ADDITIONAL_CALENDAR);
			if (additionalCalendarService != null){
				// If tool is not used/used on this site, we set the appropriate flag in the state.
				if (!siteHasTool(siteId, additionalCalendarService.getToolId()))
				{
					state.setAttribute(ADDITIONAL_CALENDAR_TOOL_READY, Boolean.FALSE);
					state.removeAttribute(ADDITIONAL_CALENDAR);
				}
				else
				{	// Also check that this calendar has been fully created (initialized) in the additional calendar service.
					if (additionalCalendarService.isCalendarToolInitialized(siteId)){
						state.setAttribute(ADDITIONAL_CALENDAR_TOOL_READY, Boolean.TRUE); // Alternate calendar ready for events.
						if (state.getAttribute(ADDITIONAL_CALENDAR) == null )
						{
							try {
								state.setAttribute(ADDITIONAL_CALENDAR, additionalCalendarService.getCalendar(null));
							} catch (IdUnusedException e) {
								M_log.info(this + ":initState No calendar found for site " + siteId  + " " + e.getMessage());
							} catch (PermissionException e) {
								M_log.info(this + ":initState No permission to get the calendar. " + e.getMessage());
							}
						}
					}
					else{
						state.setAttribute(ADDITIONAL_CALENDAR_TOOL_READY, Boolean.FALSE); // Tool on site but alternate calendar not yet created.
					}
					
				}
			}
			else{
				state.setAttribute(ADDITIONAL_CALENDAR_TOOL_READY, Boolean.FALSE); // Tool not deployed on the server.
			}
		}

		/** The Announcement tool  */
		if (state.getAttribute(ANNOUNCEMENT_TOOL_EXIST) == null)
		{
			if (!siteHasTool(siteId, "sakai.announcements"))
			{
				state.setAttribute(ANNOUNCEMENT_TOOL_EXIST, Boolean.FALSE);
				state.removeAttribute(ANNOUNCEMENT_CHANNEL);
			}
			else
			{
				state.setAttribute(ANNOUNCEMENT_TOOL_EXIST, Boolean.TRUE);
				if (state.getAttribute(ANNOUNCEMENT_CHANNEL) == null )
				{
					/** The announcement service in the State. */
					AnnouncementService aService = (AnnouncementService) state.getAttribute(STATE_ANNOUNCEMENT_SERVICE);
					if (aService == null)
					{
						aService = org.sakaiproject.announcement.cover.AnnouncementService.getInstance();
						state.setAttribute(STATE_ANNOUNCEMENT_SERVICE, aService);
			
						String channelId = ServerConfigurationService.getString("channel", null);
						if (channelId == null)
						{
							channelId = aService.channelReference(siteId, SiteService.MAIN_CONTAINER);
							try
							{
								state.setAttribute(ANNOUNCEMENT_CHANNEL, aService.getAnnouncementChannel(channelId));
							}
							catch (IdUnusedException e)
							{
								M_log.warn(this + ":initState No announcement channel found. " + e.getMessage());
								state.removeAttribute(ANNOUNCEMENT_CHANNEL);
							}
							catch (PermissionException e)
							{
								M_log.warn(this + ":initState No permission to annoucement channel. " + e.getMessage());
							}
							catch (Exception ex)
							{
								M_log.warn(this + ":initState Assignment : Action : init state : calendar exception : " + ex.getMessage());
							}
						}
					}
				}
			}
		} // if

		if (state.getAttribute(STATE_CONTEXT_STRING) == null || ((String) state.getAttribute(STATE_CONTEXT_STRING)).length() == 0)
		{
			state.setAttribute(STATE_CONTEXT_STRING, siteId);
		} // if context string is null

		if (state.getAttribute(SORTED_BY) == null)
		{
			setDefaultSort(state);
		}

		if (state.getAttribute(SORTED_GRADE_SUBMISSION_BY) == null)
		{
			state.setAttribute(SORTED_GRADE_SUBMISSION_BY, SORTED_GRADE_SUBMISSION_BY_LASTNAME);
		}

		if (state.getAttribute(SORTED_GRADE_SUBMISSION_ASC) == null)
		{
			state.setAttribute(SORTED_GRADE_SUBMISSION_ASC, Boolean.TRUE.toString());
		}

		if (state.getAttribute(SORTED_SUBMISSION_BY) == null)
		{
			state.setAttribute(SORTED_SUBMISSION_BY, SORTED_SUBMISSION_BY_LASTNAME);
		}

		if (state.getAttribute(SORTED_SUBMISSION_ASC) == null)
		{
			state.setAttribute(SORTED_SUBMISSION_ASC, Boolean.TRUE.toString());
		}

		if (state.getAttribute(STUDENT_LIST_SHOW_TABLE) == null)
		{
			state.setAttribute(STUDENT_LIST_SHOW_TABLE, new ConcurrentSkipListSet());
		}

		if (state.getAttribute(ATTACHMENTS_MODIFIED) == null)
		{
			state.setAttribute(ATTACHMENTS_MODIFIED, Boolean.valueOf(false));
		}

		// SECTION MOD
		if (state.getAttribute(STATE_SECTION_STRING) == null)
		{
			
			state.setAttribute(STATE_SECTION_STRING, "001");
		}

		// // setup the observer to notify the Main panel
		// if (state.getAttribute(STATE_OBSERVER) == null)
		// {
		// // the delivery location for this tool
		// String deliveryId = clientWindowId(state, portlet.getID());
		//
		// // the html element to update on delivery
		// String elementId = mainPanelUpdateId(portlet.getID());
		//
		// // the event resource reference pattern to watch for
		// String pattern = AssignmentService.assignmentReference((String) state.getAttribute (STATE_CONTEXT_STRING), "");
		//
		// state.setAttribute(STATE_OBSERVER, new MultipleEventsObservingCourier(deliveryId, elementId, pattern));
		// }

		if (state.getAttribute(STATE_MODE) == null)
		{
			state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
		}

		if (state.getAttribute(STATE_TOP_PAGE_MESSAGE) == null)
		{
			state.setAttribute(STATE_TOP_PAGE_MESSAGE, Integer.valueOf(0));
		}

		if (state.getAttribute(WITH_GRADES) == null)
		{
			PortletConfig config = portlet.getPortletConfig();
			String withGrades = StringUtils.trimToNull(config.getInitParameter("withGrades"));
			if (withGrades == null)
			{
				withGrades = Boolean.FALSE.toString();
			}
			state.setAttribute(WITH_GRADES, Boolean.valueOf(withGrades));
		}
		
		// whether to display the number of submission/ungraded submission column
		// default to show
		if (state.getAttribute(SHOW_NUMBER_SUBMISSION_COLUMN) == null)
		{
			PortletConfig config = portlet.getPortletConfig();
			String value = StringUtils.trimToNull(config.getInitParameter(SHOW_NUMBER_SUBMISSION_COLUMN));
			if (value == null)
			{
				value = Boolean.TRUE.toString();
			}
			state.setAttribute(SHOW_NUMBER_SUBMISSION_COLUMN, Boolean.valueOf(value));
		}
		
		if (state.getAttribute(NEW_ASSIGNMENT_YEAR_RANGE_FROM) == null)
		{
			state.setAttribute(NEW_ASSIGNMENT_YEAR_RANGE_FROM, Integer.valueOf(GregorianCalendar.getInstance().get(GregorianCalendar.YEAR)-4));
		}
		
		if (state.getAttribute(NEW_ASSIGNMENT_YEAR_RANGE_TO) == null)
		{
			state.setAttribute(NEW_ASSIGNMENT_YEAR_RANGE_TO, Integer.valueOf(GregorianCalendar.getInstance().get(GregorianCalendar.YEAR)+4));		}
	} // initState


	/**
	 * whether the site has the specified tool
	 * @param siteId
	 * @return
	 */
	private boolean siteHasTool(String siteId, String toolId) {
		boolean rv = false;
		try
		{
			Site s = SiteService.getSite(siteId);
			if (s.getToolForCommonId(toolId) != null)
			{
				rv = true;
			}
		}
		catch (Exception e)
		{
			M_log.warn(this + "siteHasTool" + e.getMessage() + siteId);
		}
		return rv;
	}

	/**
	 * reset the attributes for view submission
	 */
	private void resetViewSubmission(SessionState state)
	{
		state.removeAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE);
		state.removeAttribute(VIEW_SUBMISSION_TEXT);
		state.setAttribute(VIEW_SUBMISSION_HONOR_PLEDGE_YES, "false");
		state.removeAttribute(GRADE_GREATER_THAN_MAX_ALERT);

	} // resetViewSubmission

	/**
	 * initialize assignment attributes
	 * @param state
	 */
	private void initializeAssignment(SessionState state)
	{
		// put the input value into the state attributes
		state.setAttribute(NEW_ASSIGNMENT_TITLE, "");

		// get current time
		Time t = TimeService.newTime();
		TimeBreakdown tB = t.breakdownLocal();
		int month = tB.getMonth();
		int day = tB.getDay();
		int year = tB.getYear();

		// set the visible time to be 12:00 PM
                if (Boolean.valueOf(ServerConfigurationService.getBoolean("assignment.visible.date.enabled", false))) {                
                    state.setAttribute(NEW_ASSIGNMENT_VISIBLEMONTH, Integer.valueOf(month));
                    state.setAttribute(NEW_ASSIGNMENT_VISIBLEDAY, Integer.valueOf(day));
                    state.setAttribute(NEW_ASSIGNMENT_VISIBLEYEAR, Integer.valueOf(year));
                    state.setAttribute(NEW_ASSIGNMENT_VISIBLEHOUR, Integer.valueOf(12));
                    state.setAttribute(NEW_ASSIGNMENT_VISIBLEMIN, Integer.valueOf(0));
                    state.setAttribute(NEW_ASSIGNMENT_VISIBLETOGGLE, false);
                }
                
		// set the open time to be 12:00 PM
		state.setAttribute(NEW_ASSIGNMENT_OPENMONTH, Integer.valueOf(month));
		state.setAttribute(NEW_ASSIGNMENT_OPENDAY, Integer.valueOf(day));
		state.setAttribute(NEW_ASSIGNMENT_OPENYEAR, Integer.valueOf(year));
		state.setAttribute(NEW_ASSIGNMENT_OPENHOUR, Integer.valueOf(12));
		state.setAttribute(NEW_ASSIGNMENT_OPENMIN, Integer.valueOf(0));
		
		// set the all purpose item release time
		state.setAttribute(ALLPURPOSE_RELEASE_MONTH, Integer.valueOf(month));
		state.setAttribute(ALLPURPOSE_RELEASE_DAY, Integer.valueOf(day));
		state.setAttribute(ALLPURPOSE_RELEASE_YEAR, Integer.valueOf(year));
		state.setAttribute(ALLPURPOSE_RELEASE_HOUR, Integer.valueOf(12));
		state.setAttribute(ALLPURPOSE_RELEASE_MIN, Integer.valueOf(0));

		// due date is shifted forward by 7 days
		t.setTime(t.getTime() + 7 * 24 * 60 * 60 * 1000);
		tB = t.breakdownLocal();
		month = tB.getMonth();
		day = tB.getDay();
		year = tB.getYear();

		// set the due time to be 5:00pm
		state.setAttribute(NEW_ASSIGNMENT_DUEMONTH, Integer.valueOf(month));
		state.setAttribute(NEW_ASSIGNMENT_DUEDAY, Integer.valueOf(day));
		state.setAttribute(NEW_ASSIGNMENT_DUEYEAR, Integer.valueOf(year));
		state.setAttribute(NEW_ASSIGNMENT_DUEHOUR, Integer.valueOf(17));
		state.setAttribute(NEW_ASSIGNMENT_DUEMIN, Integer.valueOf(0));
		
		// set the resubmit time to be the same as due time
		state.setAttribute(ALLOW_RESUBMIT_CLOSEMONTH, Integer.valueOf(month));
		state.setAttribute(ALLOW_RESUBMIT_CLOSEDAY, Integer.valueOf(day));
		state.setAttribute(ALLOW_RESUBMIT_CLOSEYEAR, Integer.valueOf(year));
		state.setAttribute(ALLOW_RESUBMIT_CLOSEHOUR, Integer.valueOf(17));
		state.setAttribute(ALLOW_RESUBMIT_CLOSEMIN, Integer.valueOf(0));
		state.setAttribute(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER, Integer.valueOf(1));

		// enable the close date by default
		state.setAttribute(NEW_ASSIGNMENT_ENABLECLOSEDATE, Boolean.valueOf(true));
		// set the close time to be 5:00 pm, same as the due time by default
		state.setAttribute(NEW_ASSIGNMENT_CLOSEMONTH, Integer.valueOf(month));
		state.setAttribute(NEW_ASSIGNMENT_CLOSEDAY, Integer.valueOf(day));
		state.setAttribute(NEW_ASSIGNMENT_CLOSEYEAR, Integer.valueOf(year));
		state.setAttribute(NEW_ASSIGNMENT_CLOSEHOUR, Integer.valueOf(17));
		state.setAttribute(NEW_ASSIGNMENT_CLOSEMIN, Integer.valueOf(0));
		
		// set the all purpose retract time
		state.setAttribute(ALLPURPOSE_RETRACT_MONTH, Integer.valueOf(month));
		state.setAttribute(ALLPURPOSE_RETRACT_DAY, Integer.valueOf(day));
		state.setAttribute(ALLPURPOSE_RETRACT_YEAR, Integer.valueOf(year));
		state.setAttribute(ALLPURPOSE_RETRACT_HOUR, Integer.valueOf(17));
		state.setAttribute(ALLPURPOSE_RETRACT_MIN, Integer.valueOf(0));

		// set the peer period time to be 10 mins after accept until date
		state.setAttribute(NEW_ASSIGNMENT_PEERPERIODMONTH, Integer.valueOf(month));
		state.setAttribute(NEW_ASSIGNMENT_PEERPERIODDAY, Integer.valueOf(day));
		state.setAttribute(NEW_ASSIGNMENT_PEERPERIODYEAR, Integer.valueOf(year));
		state.setAttribute(NEW_ASSIGNMENT_PEERPERIODHOUR, Integer.valueOf(17));
		state.setAttribute(NEW_ASSIGNMENT_PEERPERIODMIN, Integer.valueOf(10));
		
		state.setAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_ANON_EVAL, Boolean.TRUE.toString());
		state.setAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_STUDENT_VIEW_REVIEWS, Boolean.TRUE.toString());
		state.setAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_NUM_REVIEWS, 1);

		state.setAttribute(NEW_ASSIGNMENT_SECTION, "001");
		state.setAttribute(NEW_ASSIGNMENT_SUBMISSION_TYPE, Integer.valueOf(Assignment.TEXT_AND_ATTACHMENT_ASSIGNMENT_SUBMISSION));
		state.setAttribute(NEW_ASSIGNMENT_GRADE_TYPE, Integer.valueOf(Assignment.UNGRADED_GRADE_TYPE));
		state.setAttribute(NEW_ASSIGNMENT_GRADE_POINTS, "");
		state.setAttribute(NEW_ASSIGNMENT_DESCRIPTION, "");
		state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE, Boolean.FALSE.toString());
		state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE, Boolean.FALSE.toString());
		
		String defaultNotification = ServerConfigurationService.getString("announcement.default.notification", "n");
		if (defaultNotification.equalsIgnoreCase("r")) {
			state.setAttribute(Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION, Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION_HIGH);
		}
		else if (defaultNotification.equalsIgnoreCase("o")) {
			state.setAttribute(Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION, Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION_LOW);
		}
		else {
			state.setAttribute(Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION, Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION_NONE);
		}
		// make the honor pledge not include as the default
		state.setAttribute(NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE, (Integer.valueOf(Assignment.HONOR_PLEDGE_NONE)).toString());

		state.setAttribute(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, AssignmentService.GRADEBOOK_INTEGRATION_NO);

		state.setAttribute(NEW_ASSIGNMENT_ATTACHMENT, EntityManager.newReferenceList());

		state.setAttribute(NEW_ASSIGNMENT_FOCUS, NEW_ASSIGNMENT_TITLE);

		state.removeAttribute(NEW_ASSIGNMENT_DESCRIPTION_EMPTY);

		// reset the global navigaion alert flag
		if (state.getAttribute(ALERT_GLOBAL_NAVIGATION) != null)
		{
			state.removeAttribute(ALERT_GLOBAL_NAVIGATION);
		}

		state.removeAttribute(NEW_ASSIGNMENT_RANGE);
		state.removeAttribute(NEW_ASSIGNMENT_GROUPS);

		// remove the edit assignment id if any
		state.removeAttribute(EDIT_ASSIGNMENT_ID);
		
		// remove the resubmit number
		state.removeAttribute(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER);
		
		// remove the supplement attributes
		state.removeAttribute(MODELANSWER);
		state.removeAttribute(MODELANSWER_TEXT);
		state.removeAttribute(MODELANSWER_SHOWTO);
		state.removeAttribute(MODELANSWER_ATTACHMENTS);
		state.removeAttribute(NOTE);
		state.removeAttribute(NOTE_TEXT);
		state.removeAttribute(NOTE_SHAREWITH);
		state.removeAttribute(ALLPURPOSE);
		state.removeAttribute(ALLPURPOSE_TITLE);
		state.removeAttribute(ALLPURPOSE_TEXT);
		state.removeAttribute(ALLPURPOSE_HIDE);
		state.removeAttribute(ALLPURPOSE_SHOW_FROM);
		state.removeAttribute(ALLPURPOSE_SHOW_TO);
		state.removeAttribute(ALLPURPOSE_RELEASE_DATE);
		state.removeAttribute(ALLPURPOSE_RETRACT_DATE);
		state.removeAttribute(ALLPURPOSE_ACCESS);
		state.removeAttribute(ALLPURPOSE_ATTACHMENTS);

		// SAK-17606
		state.removeAttribute(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING);

	} // resetNewAssignment
	
	/**
	 * reset the attributes for assignment
	 */
	private void resetAssignment(SessionState state)
	{
		state.removeAttribute(NEW_ASSIGNMENT_TITLE);
		state.removeAttribute(NEW_ASSIGNMENT_OPENMONTH);
		state.removeAttribute(NEW_ASSIGNMENT_OPENDAY);
		state.removeAttribute(NEW_ASSIGNMENT_OPENYEAR);
		state.removeAttribute(NEW_ASSIGNMENT_OPENHOUR);
		state.removeAttribute(NEW_ASSIGNMENT_OPENMIN);
		
		state.removeAttribute(ALLPURPOSE_RELEASE_MONTH);
		state.removeAttribute(ALLPURPOSE_RELEASE_DAY);
		state.removeAttribute(ALLPURPOSE_RELEASE_YEAR);
		state.removeAttribute(ALLPURPOSE_RELEASE_HOUR);
		state.removeAttribute(ALLPURPOSE_RELEASE_MIN);

		state.removeAttribute(NEW_ASSIGNMENT_DUEMONTH);
		state.removeAttribute(NEW_ASSIGNMENT_DUEDAY);
		state.removeAttribute(NEW_ASSIGNMENT_DUEYEAR);
		state.removeAttribute(NEW_ASSIGNMENT_DUEHOUR);
		state.removeAttribute(NEW_ASSIGNMENT_DUEMIN);

		state.removeAttribute(NEW_ASSIGNMENT_VISIBLEMONTH);
		state.removeAttribute(NEW_ASSIGNMENT_VISIBLEDAY);
		state.removeAttribute(NEW_ASSIGNMENT_VISIBLEYEAR);
		state.removeAttribute(NEW_ASSIGNMENT_VISIBLEHOUR);
		state.removeAttribute(NEW_ASSIGNMENT_VISIBLEMIN);
		state.removeAttribute(NEW_ASSIGNMENT_VISIBLETOGGLE);

		state.removeAttribute(NEW_ASSIGNMENT_ENABLECLOSEDATE);
		state.removeAttribute(NEW_ASSIGNMENT_CLOSEMONTH);
		state.removeAttribute(NEW_ASSIGNMENT_CLOSEDAY);
		state.removeAttribute(NEW_ASSIGNMENT_CLOSEYEAR);
		state.removeAttribute(NEW_ASSIGNMENT_CLOSEHOUR);
		state.removeAttribute(NEW_ASSIGNMENT_CLOSEMIN);
		
		// set the all purpose retract time
		state.removeAttribute(ALLPURPOSE_RETRACT_MONTH);
		state.removeAttribute(ALLPURPOSE_RETRACT_DAY);
		state.removeAttribute(ALLPURPOSE_RETRACT_YEAR);
		state.removeAttribute(ALLPURPOSE_RETRACT_HOUR);
		state.removeAttribute(ALLPURPOSE_RETRACT_MIN);

		state.removeAttribute(NEW_ASSIGNMENT_SECTION);
		state.removeAttribute(NEW_ASSIGNMENT_SUBMISSION_TYPE);
		state.removeAttribute(NEW_ASSIGNMENT_GRADE_TYPE);
		state.removeAttribute(NEW_ASSIGNMENT_GRADE_POINTS);
		state.removeAttribute(NEW_ASSIGNMENT_DESCRIPTION);
		state.removeAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE);
		state.removeAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE);
		state.removeAttribute(Assignment.ASSIGNMENT_OPENDATE_NOTIFICATION);
		state.removeAttribute(NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE);
		state.removeAttribute(NEW_ASSIGNMENT_CHECK_HIDE_DUE_DATE);
		state.removeAttribute(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK);
		state.removeAttribute(NEW_ASSIGNMENT_ATTACHMENT);
		state.removeAttribute(NEW_ASSIGNMENT_FOCUS);
		state.removeAttribute(NEW_ASSIGNMENT_DESCRIPTION_EMPTY);
                state.removeAttribute(NEW_ASSIGNMENT_GROUP_SUBMIT);

		// reset the global navigaion alert flag
		if (state.getAttribute(ALERT_GLOBAL_NAVIGATION) != null)
		{
			state.removeAttribute(ALERT_GLOBAL_NAVIGATION);
		}

		state.removeAttribute(NEW_ASSIGNMENT_RANGE);
		state.removeAttribute(NEW_ASSIGNMENT_GROUPS);

		// remove the edit assignment id if any
		state.removeAttribute(EDIT_ASSIGNMENT_ID);
		
		// remove the resubmit number
		state.removeAttribute(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER);
		
		// remove the supplement attributes
		state.removeAttribute(MODELANSWER);
		state.removeAttribute(MODELANSWER_TEXT);
		state.removeAttribute(MODELANSWER_SHOWTO);
		state.removeAttribute(MODELANSWER_ATTACHMENTS);
		state.removeAttribute(NOTE);
		state.removeAttribute(NOTE_TEXT);
		state.removeAttribute(NOTE_SHAREWITH);
		state.removeAttribute(ALLPURPOSE);
		state.removeAttribute(ALLPURPOSE_TITLE);
		state.removeAttribute(ALLPURPOSE_TEXT);
		state.removeAttribute(ALLPURPOSE_HIDE);
		state.removeAttribute(ALLPURPOSE_SHOW_FROM);
		state.removeAttribute(ALLPURPOSE_SHOW_TO);
		state.removeAttribute(ALLPURPOSE_RELEASE_DATE);
		state.removeAttribute(ALLPURPOSE_RETRACT_DATE);
		state.removeAttribute(ALLPURPOSE_ACCESS);
		state.removeAttribute(ALLPURPOSE_ATTACHMENTS);

		//revmoew peer assessment settings
		state.removeAttribute(NEW_ASSIGNMENT_USE_PEER_ASSESSMENT);
		state.removeAttribute(NEW_ASSIGNMENT_PEERPERIODMONTH);
		state.removeAttribute(NEW_ASSIGNMENT_PEERPERIODDAY);
		state.removeAttribute(NEW_ASSIGNMENT_PEERPERIODYEAR);
		state.removeAttribute(NEW_ASSIGNMENT_PEERPERIODHOUR);
		state.removeAttribute(NEW_ASSIGNMENT_PEERPERIODMIN);
		state.removeAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_ANON_EVAL);
		state.removeAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_STUDENT_VIEW_REVIEWS);
		state.removeAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_NUM_REVIEWS);
		state.removeAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_INSTRUCTIONS);
		
		// remove content-review setting
		state.removeAttribute(NEW_ASSIGNMENT_USE_REVIEW_SERVICE);
		
		state.removeAttribute(Assignment.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_VALUE);
		
		state.removeAttribute(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
		

	} // resetNewAssignment

	/**
	 * construct a HashMap using integer as the key and three character string of the month as the value
	 */
	private HashMap monthTable()
	{
		HashMap n = new HashMap();
		n.put(Integer.valueOf(1), rb.getString("jan"));
		n.put(Integer.valueOf(2), rb.getString("feb"));
		n.put(Integer.valueOf(3), rb.getString("mar"));
		n.put(Integer.valueOf(4), rb.getString("apr"));
		n.put(Integer.valueOf(5), rb.getString("may"));
		n.put(Integer.valueOf(6), rb.getString("jun"));
		n.put(Integer.valueOf(7), rb.getString("jul"));
		n.put(Integer.valueOf(8), rb.getString("aug"));
		n.put(Integer.valueOf(9), rb.getString("sep"));
		n.put(Integer.valueOf(10), rb.getString("oct"));
		n.put(Integer.valueOf(11), rb.getString("nov"));
		n.put(Integer.valueOf(12), rb.getString("dec"));
		return n;

	} // monthTable

	/**
	 * construct a HashMap using the integer as the key and grade type String as the value
	 */
	private HashMap gradeTypeTable(){
		
		HashMap gradeTypeTable = new HashMap();
		gradeTypeTable.put(Integer.valueOf(1), rb.getString(AssignmentConstants.ASSN_GRADE_TYPE_NOGRADE_PROP));
		gradeTypeTable.put(Integer.valueOf(2), rb.getString(AssignmentConstants.ASSN_GRADE_TYPE_LETTER_PROP));
		gradeTypeTable.put(Integer.valueOf(3), rb.getString(AssignmentConstants.ASSN_GRADE_TYPE_POINTS_PROP));
		gradeTypeTable.put(Integer.valueOf(4), rb.getString(AssignmentConstants.ASSN_GRADE_TYPE_PASS_FAIL_PROP));
		gradeTypeTable.put(Integer.valueOf(5), rb.getString(AssignmentConstants.ASSN_GRADE_TYPE_CHECK_PROP));
		
		return gradeTypeTable;
	} // gradeTypeTable

	/**
	 * construct a HashMap using the integer as the key and submission type String as the value
	 */
	private HashMap submissionTypeTable(){
		
		HashMap submissionTypeTable = new HashMap();
		submissionTypeTable.put(Integer.valueOf(1), rb.getString(AssignmentConstants.ASSN_SUBMISSION_TYPE_INLINE_PROP));
		submissionTypeTable.put(Integer.valueOf(2), rb.getString(AssignmentConstants.ASSN_SUBMISSION_TYPE_ATTACHMENTS_ONLY_PROP));
		submissionTypeTable.put(Integer.valueOf(3), rb.getString(AssignmentConstants.ASSN_SUBMISSION_TYPE_INLINE_AND_ATTACHMENTS_PROP));
		submissionTypeTable.put(Integer.valueOf(4), rb.getString(AssignmentConstants.ASSN_SUBMISSION_TYPE_NON_ELECTRONIC_PROP));
		submissionTypeTable.put(Integer.valueOf(5), rb.getString(AssignmentConstants.ASSN_SUBMISSION_TYPE_SINGLE_ATTACHMENT_PROP));

		return submissionTypeTable;
	} // submissionTypeTable
	
	/**
	* Add the list of categories from the gradebook tool
	* construct a HashMap using the integer as the key and category String as the value
	* @return
	*/
	private HashMap<Long, String> categoryTable()
	{
		boolean gradebookExists = isGradebookDefined();
		HashMap<Long, String> catTable = new HashMap<Long, String>();
		if (gradebookExists) {
			
			GradebookService g = (GradebookService)  ComponentManager.get("org.sakaiproject.service.gradebook.GradebookService");
			String gradebookUid = ToolManager.getInstance().getCurrentPlacement().getContext();
			
			List<CategoryDefinition> categoryDefinitions = g.getCategoryDefinitions(gradebookUid);
			
			catTable.put(Long.valueOf(-1), rb.getString("grading.unassigned"));
			for (CategoryDefinition category: categoryDefinitions) {
				catTable.put(category.getId(), category.getName());
			}
		}
		return catTable;
		
	} // categoryTable

	/**
	 * Sort based on the given property
	 */
	public void doSort(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// we are changing the sort, so start from the first page again
		resetPaging(state);

		setupSort(data, data.getParameters().getString("criteria"));
	}

	/**
	 * setup sorting parameters
	 *
	 * @param criteria
	 *        String for sortedBy
	 */
	private void setupSort(RunData data, String criteria)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// current sorting sequence
		String asc = "";
		if (!criteria.equals(state.getAttribute(SORTED_BY)))
		{
			state.setAttribute(SORTED_BY, criteria);
			asc = Boolean.TRUE.toString();
			state.setAttribute(SORTED_ASC, asc);
		}
		else
		{
			// current sorting sequence
			asc = (String) state.getAttribute(SORTED_ASC);

			// toggle between the ascending and descending sequence
			if (asc.equals(Boolean.TRUE.toString()))
			{
				asc = Boolean.FALSE.toString();
			}
			else
			{
				asc = Boolean.TRUE.toString();
			}
			state.setAttribute(SORTED_ASC, asc);
		}

	} // doSort

	/**
	 * Do sort by group title
	 */
	public void doSortbygrouptitle(RunData data)
	{
		setupSort(data, SORTED_BY_GROUP_TITLE);

	} // doSortbygrouptitle

	/**
	 * Do sort by group description
	 */
	public void doSortbygroupdescription(RunData data)
	{
		setupSort(data, SORTED_BY_GROUP_DESCRIPTION);

	} // doSortbygroupdescription

	/**
	 * Sort submission based on the given property
	 */
	public void doSort_submission(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// we are changing the sort, so start from the first page again
		resetPaging(state);

		// get the ParameterParser from RunData
		ParameterParser params = data.getParameters();

		String criteria = params.getString("criteria");

		// current sorting sequence
		String asc = "";

		if (!criteria.equals(state.getAttribute(SORTED_SUBMISSION_BY)))
		{
			state.setAttribute(SORTED_SUBMISSION_BY, criteria);
			asc = Boolean.TRUE.toString();
			state.setAttribute(SORTED_SUBMISSION_ASC, asc);
		}
		else
		{
			// current sorting sequence
			state.setAttribute(SORTED_SUBMISSION_BY, criteria);
			asc = (String) state.getAttribute(SORTED_SUBMISSION_ASC);

			// toggle between the ascending and descending sequence
			if (asc.equals(Boolean.TRUE.toString()))
			{
				asc = Boolean.FALSE.toString();
			}
			else
			{
				asc = Boolean.TRUE.toString();
			}
			state.setAttribute(SORTED_SUBMISSION_ASC, asc);
		}
	} // doSort_submission


	
	
	/**
	 * Sort submission based on the given property in instructor grade view
	 */
	public void doSort_grade_submission(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// we are changing the sort, so start from the first page again
		resetPaging(state);

		// get the ParameterParser from RunData
		ParameterParser params = data.getParameters();

		String criteria = params.getString("criteria");

		// current sorting sequence
		String asc = "";

		if (!criteria.equals(state.getAttribute(SORTED_GRADE_SUBMISSION_BY)))
		{
			state.setAttribute(SORTED_GRADE_SUBMISSION_BY, criteria);
			//for content review default is desc
			if (criteria.equals(SORTED_GRADE_SUBMISSION_CONTENTREVIEW))
				asc = Boolean.FALSE.toString();
			else
				asc = Boolean.TRUE.toString();
			
			state.setAttribute(SORTED_GRADE_SUBMISSION_ASC, asc);
		}
		else
		{
			// current sorting sequence
			state.setAttribute(SORTED_GRADE_SUBMISSION_BY, criteria);
			asc = (String) state.getAttribute(SORTED_GRADE_SUBMISSION_ASC);

			// toggle between the ascending and descending sequence
			if (asc.equals(Boolean.TRUE.toString()))
			{
				asc = Boolean.FALSE.toString();
			}
			else
			{
				asc = Boolean.TRUE.toString();
			}
			state.setAttribute(SORTED_GRADE_SUBMISSION_ASC, asc);
		}
	} // doSort_grade_submission

	public void doSort_tags(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		ParameterParser params = data.getParameters();

		String criteria = params.getString("criteria");
		String providerId = params.getString(PROVIDER_ID);
		
		String savedText = params.getString("savedText");		
		state.setAttribute(VIEW_SUBMISSION_TEXT, savedText);

		String mode = (String) state.getAttribute(STATE_MODE);
		
		List<DecoratedTaggingProvider> providers = (List) state
				.getAttribute(mode + PROVIDER_LIST);

		for (DecoratedTaggingProvider dtp : providers) {
			if (dtp.getProvider().getId().equals(providerId)) {
				Sort sort = dtp.getSort();
				if (sort.getSort().equals(criteria)) {
					sort.setAscending(sort.isAscending() ? false : true);
				} else {
					sort.setSort(criteria);
					sort.setAscending(true);
				}
				break;
			}
		}
	}
	
	public void doPage_tags(RunData data) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		ParameterParser params = data.getParameters();

		String page = params.getString("page");
		String pageSize = params.getString("pageSize");
		String providerId = params.getString(PROVIDER_ID);

		String savedText = params.getString("savedText");		
		state.setAttribute(VIEW_SUBMISSION_TEXT, savedText);

		String mode = (String) state.getAttribute(STATE_MODE);
		
		List<DecoratedTaggingProvider> providers = (List) state
				.getAttribute(mode + PROVIDER_LIST);

		for (DecoratedTaggingProvider dtp : providers) {
			if (dtp.getProvider().getId().equals(providerId)) {
				Pager pager = dtp.getPager();
				pager.setPageSize(Integer.valueOf(pageSize));
				if (Pager.FIRST.equals(page)) {
					pager.setFirstItem(0);
				} else if (Pager.PREVIOUS.equals(page)) {
					pager.setFirstItem(pager.getFirstItem()
							- pager.getPageSize());
				} else if (Pager.NEXT.equals(page)) {
					pager.setFirstItem(pager.getFirstItem()
							+ pager.getPageSize());
				} else if (Pager.LAST.equals(page)) {
					pager.setFirstItem((pager.getTotalItems() / pager
							.getPageSize())
							* pager.getPageSize());
				}
				break;			
			}
		}
	}
	
	/**
	 * the SubmitterSubmission clas
	 */
	public class SubmitterSubmission
	{
		/**
		 * the User object
		 */
		User m_user = null;

		/**
		 * is the Submitter in more than one group
		 */
		Boolean m_multi_group = false;

		/**
		 * the Group
		 */
		Group m_group = null;
		
		/**
		 * the AssignmentSubmission object
		 */
		AssignmentSubmission m_submission = null;

		/**
		 * the AssignmentSubmission object
		 */
		User m_submittedBy = null;

		public SubmitterSubmission(User u, AssignmentSubmission s)
		{
			m_user = u;
			m_submission = s;
		}

		public SubmitterSubmission(Group g, AssignmentSubmission s)
		{
		    m_group = g;
		    m_submission = s;
		}

		/**
		 * Returns the AssignmentSubmission object
		 */
		public AssignmentSubmission getSubmission()
		{
			return m_submission;
		}

		/**
		 * Returns the User object
		 */
		public User getUser()
		{
			return m_user;
		}

		public void setSubmittedBy(User submittedBy) {
			m_submittedBy = submittedBy;
		}

		/**
		 * Returns the User object of the submitter,
		 * if null, the user submitted the assignment himself.
		 */
		public User getSubmittedBy() {
			return m_submittedBy;
		}
		public Group getGroup()
		{
			return m_group;
		}
		public void setGroup(Group _group) {
		    m_group = _group;
		}
		public Boolean getIsMultiGroup() {
		    return m_multi_group;    
		}
		public void setMultiGroup(Boolean _multi) {
		    m_multi_group = _multi;
		}

		public String getGradeForUser(String id) {
			AssignmentSubmission s=getSubmission();
			String grade = s == null ? null:(StringUtils.trimToNull(s.getAssignment().getProperties().getProperty(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT))!=null) && (s.getAssignment().getContent().getTypeOfGrade() == Assignment.SCORE_GRADE_TYPE)?
					s.getGradeForUserInGradeBook(id)!=null?s.getGradeForUserInGradeBook(id):s.getGradeForUser(id):s.getGradeForUser(id);
			return grade;
		}
	}

	/**
	 * the AssignmentComparator clas
	 */
	private class AssignmentComparator implements Comparator
	{
		Collator collator = null;
		
		/**
		 * the SessionState object
		 */
		SessionState m_state = null;

		/**
		 * the criteria
		 */
		String m_criteria = null;

		/**
		 * the criteria
		 */
		String m_asc = null;

		/**
		 * the user
		 */
		User m_user = null;
		
		// true if users should be compared by anonymous submitter id rather than other identifiers
		boolean m_anon = false;

		/**
		 * constructor
		 *
		 * @param state
		 *        The state object
		 * @param criteria
		 *        The sort criteria string
		 * @param asc
		 *        The sort order string. TRUE_STRING if ascending; "false" otherwise.
		 */
		public AssignmentComparator(SessionState state, String criteria, String asc)
		{
			this(state, criteria, asc, null);

		} // constructor

		/**
		 * constructor
		 *
		 * @param state
		 *        The state object
		 * @param criteria
		 *        The sort criteria string
		 * @param asc
		 *        The sort order string. TRUE_STRING if ascending; "false" otherwise.
		 * @param user
		 *        The user object
		 */
		public AssignmentComparator(SessionState state, String criteria, String asc, User user)
		{
			m_state = state;
			m_criteria = criteria;
			m_asc = asc;
			m_user = user;
			try
			{
				collator= new RuleBasedCollator(((RuleBasedCollator)Collator.getInstance()).getRules().replaceAll("<'\u005f'", "<' '<'\u005f'"));
			}
			catch (ParseException e)
			{
				// error with init RuleBasedCollator with rules
				// use the default Collator
				collator = Collator.getInstance();
				M_log.warn(this + " AssignmentComparator cannot init RuleBasedCollator. Will use the default Collator instead. " + e);
			}
		} // constructor

		/**
		 * caculate the range string for an assignment
		 */
		private String getAssignmentRange(Assignment a)
		{
			String rv = "";
			if (a.getAccess().equals(Assignment.AssignmentAccess.SITE))
			{
				// site assignment
				rv = rb.getString("range.allgroups");
			}
			else
			{
				try
				{
					// get current site
					Site site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
					for (Iterator k = a.getGroups().iterator(); k.hasNext();)
					{
						// announcement by group
                                                Group _aGroup = site.getGroup((String) k.next());
						if (_aGroup != null) 
                                                    rv = rv.concat(_aGroup.getTitle());
					}
				}
				catch (Exception ignore)
				{
					M_log.warn(this + ":getAssignmentRange" + ignore.getMessage());
				}
			}

			return rv;

		} // getAssignmentRange
		
		public void setAnon(boolean value)
		{
			m_anon = value;
		}

		/**
		 * implementing the compare function
		 *
		 * @param o1
		 *        The first object
		 * @param o2
		 *        The second object
		 * @return The compare result. 1 is o1 < o2; -1 otherwise
		 */
		public int compare(Object o1, Object o2)
		{
			int result = -1;
			
			if (m_criteria == null)
			{
				m_criteria = SORTED_BY_DEFAULT;
			}

			/** *********** for sorting assignments ****************** */
			if (m_criteria.equals(SORTED_BY_DEFAULT))
			{
				int s1 = ((Assignment) o1).getPosition_order();
				int s2 = ((Assignment) o2).getPosition_order();
				
				if ( s1 == s2 ) // we either have 2 assignments with no existing postion_order or a numbering error, so sort by duedate
				{
					// sorted by the assignment due date
					Time t1 = ((Assignment) o1).getDueTime();
					Time t2 = ((Assignment) o2).getDueTime();

					if (t1 == null)
					{
						result = -1;
					}
					else if (t2 == null)
					{
						result = 1;
					}
					else 
					{
						if (t1.equals(t2))
						{
							t1 = ((Assignment) o1).getTimeCreated();
							t2 = ((Assignment) o2).getTimeCreated();
						}
						if (t1.before(t2))
						{
							result = 1;
						}
						else
						{
							result = -1;
						}
					}
				}				
				else if ( s1 == 0 && s2 > 0 ) // order has not been set on this object, so put it at the bottom of the list
				{
					result = 1;
				}
				else if ( s2 == 0 && s1 > 0 ) // making sure assignments with no position_order stay at the bottom
				{
					result = -1;
				}
				else // 2 legitimate postion orders
				{
					result = (s1 < s2) ? -1 : 1;
				}
			}
			if (m_criteria.equals(SORTED_BY_TITLE))
			{
				// sorted by the assignment title
				String s1 = ((Assignment) o1).getTitle();
				String s2 = ((Assignment) o2).getTitle();
				result = compareString(s1, s2);
			}
			else if (m_criteria.equals(SORTED_BY_SECTION))
			{
				// sorted by the assignment section
				String s1 = ((Assignment) o1).getSection();
				String s2 = ((Assignment) o2).getSection();
				result = compareString(s1, s2);
			}
			else if (m_criteria.equals(SORTED_BY_DUEDATE))
			{
				// sorted by the assignment due date
				Time t1 = ((Assignment) o1).getDueTime();
				Time t2 = ((Assignment) o2).getDueTime();

				if (t1 == null)
				{
					result = -1;
				}
				else if (t2 == null)
				{
					result = 1;
				}
				else if (t1.before(t2))
				{
					result = -1;
				}
				else
				{
					result = 1;
				}
			}
			else if (m_criteria.equals(SORTED_BY_OPENDATE))
			{
				// sorted by the assignment open
				Time t1 = ((Assignment) o1).getOpenTime();
				Time t2 = ((Assignment) o2).getOpenTime();

				if (t1 == null)
				{
					result = -1;
				}
				else if (t2 == null)
				{
					result = 1;
				}
				else if (t1.before(t2))
				{
					result = -1;
				}
				else
				{
					result = 1;
				}
			}
			else if (m_criteria.equals(SORTED_BY_ASSIGNMENT_STATUS))
			{
				
				if (AssignmentService.allowAddAssignment(((Assignment) o1).getContext()))
				{
					// comparing assignment status
					result = compareString(((Assignment) o1).getStatus(), ((Assignment) o2).getStatus());
				}
				else
				{
					// comparing submission status
					AssignmentSubmission s1 = findAssignmentSubmission((Assignment) o1);
					AssignmentSubmission s2 = findAssignmentSubmission((Assignment) o2);
					result = s1==null ? 1 : s2==null? -1:compareString(s1.getStatus(), s2.getStatus());
				}
			}
			else if (m_criteria.equals(SORTED_BY_NUM_SUBMISSIONS))
			{
				// sort by numbers of submissions

				// initialize
				int subNum1 = 0;
				int subNum2 = 0;
				Time t1,t2;
				
				Iterator submissions1 = AssignmentService.getSubmissions((Assignment) o1).iterator();
				while (submissions1.hasNext())
				{
					AssignmentSubmission submission1 = (AssignmentSubmission) submissions1.next();
					t1 = submission1.getTimeSubmitted();
					
					if (t1!=null) subNum1++;
				}

				Iterator submissions2 = AssignmentService.getSubmissions((Assignment) o2).iterator();
				while (submissions2.hasNext())
				{
					AssignmentSubmission submission2 = (AssignmentSubmission) submissions2.next();
					t2 = submission2.getTimeSubmitted();
					
					if (t2!=null) subNum2++;
				}

				result = (subNum1 > subNum2) ? 1 : -1;

			}
			else if (m_criteria.equals(SORTED_BY_NUM_UNGRADED))
			{
				// sort by numbers of ungraded submissions

				// initialize
				int ungraded1 = 0;
				int ungraded2 = 0;
				Time t1,t2;
				
				Iterator submissions1 = AssignmentService.getSubmissions((Assignment) o1).iterator();
				while (submissions1.hasNext())
				{
					AssignmentSubmission submission1 = (AssignmentSubmission) submissions1.next();
					t1 = submission1.getTimeSubmitted();
					
					if (t1!=null && !submission1.getGraded()) ungraded1++;
				}

				Iterator submissions2 = AssignmentService.getSubmissions((Assignment) o2).iterator();
				while (submissions2.hasNext())
				{
					AssignmentSubmission submission2 = (AssignmentSubmission) submissions2.next();
					t2 = submission2.getTimeSubmitted();
					
					if (t2!=null && !submission2.getGraded()) ungraded2++;
				}

				result = (ungraded1 > ungraded2) ? 1 : -1;

			}
			else if (m_criteria.equals(SORTED_BY_GRADE) || m_criteria.equals(SORTED_BY_SUBMISSION_STATUS))
			{
				AssignmentSubmission submission1 = getSubmission(((Assignment) o1).getId(), m_user, "compare", null);
				String grade1 = " ";
				if (submission1 != null && submission1.getGraded() && submission1.getGradeReleased())
				{
					grade1 = submission1.getGrade();
				}

				AssignmentSubmission submission2 = getSubmission(((Assignment) o2).getId(), m_user, "compare", null);
				String grade2 = " ";
				if (submission2 != null && submission2.getGraded() && submission2.getGradeReleased())
				{
					grade2 = submission2.getGrade();
				}

				result = compareString(grade1, grade2);
			}
			else if (m_criteria.equals(SORTED_BY_MAX_GRADE))
			{
				String maxGrade1 = maxGrade(((Assignment) o1).getContent().getTypeOfGrade(), (Assignment) o1);
				String maxGrade2 = maxGrade(((Assignment) o2).getContent().getTypeOfGrade(), (Assignment) o2);

				try
				{
					// do integer comparation inside point grade type
					int max1 = Integer.parseInt(maxGrade1);
					int max2 = Integer.parseInt(maxGrade2);
					result = (max1 < max2) ? -1 : 1;
				}
				catch (NumberFormatException e)
				{
					// otherwise do an alpha-compare
					result = compareString(maxGrade1, maxGrade2);
				}
			}
			// group related sorting
			else if (m_criteria.equals(SORTED_BY_FOR))
			{
				// sorted by the public view attribute
				String factor1 = getAssignmentRange((Assignment) o1);
				String factor2 = getAssignmentRange((Assignment) o2);
				result = compareString(factor1, factor2);
			}
			else if (m_criteria.equals(SORTED_BY_GROUP_TITLE))
			{
				// sorted by the group title
				String factor1 = ((Group) o1).getTitle();
				String factor2 = ((Group) o2).getTitle();
				result = compareString(factor1, factor2);
			}
			else if (m_criteria.equals(SORTED_BY_GROUP_DESCRIPTION))
			{
				// sorted by the group description
				String factor1 = ((Group) o1).getDescription();
				String factor2 = ((Group) o2).getDescription();
				if (factor1 == null)
				{
					factor1 = "";
				}
				if (factor2 == null)
				{
					factor2 = "";
				}
				result = compareString(factor1, factor2);
			}
			/** ***************** for sorting submissions in instructor grade assignment view ************* */
			else if(m_criteria.equals(SORTED_GRADE_SUBMISSION_CONTENTREVIEW))
			{
				SubmitterSubmission u1 = (SubmitterSubmission) o1;
				SubmitterSubmission u2 = (SubmitterSubmission) o2;
				if (u1 == null || u2 == null )
				{
					result = 1;
				}
				else
				{	
					AssignmentSubmission s1 = u1.getSubmission();
					AssignmentSubmission s2 = u2.getSubmission();


					if (s1 == null)
					{
						result = -1;
					}
					else if (s2 == null )
					{
						result = 1;
					} 
					else
					{
						int score1 = u1.getSubmission().getReviewScore();
						int score2 = u2.getSubmission().getReviewScore();
						result = (Integer.valueOf(score1)).intValue() > (Integer.valueOf(score2)).intValue() ? 1 : -1;
					}
				}
				
			}
			else if (m_criteria.equals(SORTED_GRADE_SUBMISSION_BY_LASTNAME))
			{
				// sorted by the submitters sort name
				SubmitterSubmission u1 = (SubmitterSubmission) o1;
				SubmitterSubmission u2 = (SubmitterSubmission) o2;

				if (u1 == null || u2 == null || (u1.getUser() == null && u1.getGroup() == null) || (u2.getUser() == null && u2.getGroup() == null) )
				{
					result = 1;
				}
				else if (m_anon)
				{
							String anon1 = u1.getSubmission().getAnonymousSubmissionId();
							String anon2 = u2.getSubmission().getAnonymousSubmissionId();
							result = compareString(anon1, anon2);
				}
				else
				{
					String lName1 = u1.getUser() == null ? u1.getGroup().getTitle(): u1.getUser().getSortName();
					String lName2 = u2.getUser() == null ? u2.getGroup().getTitle(): u2.getUser().getSortName();
					result = compareString(lName1, lName2);
				}
			}
			else if (m_criteria.equals(SORTED_GRADE_SUBMISSION_BY_SUBMIT_TIME))
			{
				// sorted by submission time
				SubmitterSubmission u1 = (SubmitterSubmission) o1;
				SubmitterSubmission u2 = (SubmitterSubmission) o2;

				if (u1 == null || u2 == null)
				{
					result = -1;
				}
				else
				{
					AssignmentSubmission s1 = u1.getSubmission();
					AssignmentSubmission s2 = u2.getSubmission();


					if (s1 == null || s1.getTimeSubmitted() == null)
					{
						result = -1;
					}
					else if (s2 == null || s2.getTimeSubmitted() == null)
					{
						result = 1;
					}
					else if (s1.getTimeSubmitted().before(s2.getTimeSubmitted()))
					{
						result = -1;
					}
					else
					{
						result = 1;
					}
				}
			}
			else if (m_criteria.equals(SORTED_GRADE_SUBMISSION_BY_STATUS))
			{
				// sort by submission status
				SubmitterSubmission u1 = (SubmitterSubmission) o1;
				SubmitterSubmission u2 = (SubmitterSubmission) o2;

				String status1 = "";
				String status2 = "";
				
				if (u1 == null)
				{
					status1 = rb.getString("listsub.nosub");
				}
				else
				{
					AssignmentSubmission s1 = u1.getSubmission();
					if (s1 == null)
					{
						status1 = rb.getString("listsub.nosub");
					}
					else
					{
						status1 = s1.getStatus();
					}
				}
				
				if (u2 == null)
				{
					status2 = rb.getString("listsub.nosub");
				}
				else
				{
					AssignmentSubmission s2 = u2.getSubmission();
					if (s2 == null)
					{
						status2 = rb.getString("listsub.nosub");
					}
					else
					{
						status2 = s2.getStatus();
					}
				}
				
				result = compareString(status1, status2);
			}
			else if (m_criteria.equals(SORTED_GRADE_SUBMISSION_BY_GRADE))
			{
				// sort by submission status
				SubmitterSubmission u1 = (SubmitterSubmission) o1;
				SubmitterSubmission u2 = (SubmitterSubmission) o2;

				if (u1 == null || u2 == null)
				{
					result = -1;
				}
				else
				{
					AssignmentSubmission s1 = u1.getSubmission();
					AssignmentSubmission s2 = u2.getSubmission();

					//sort by submission grade
					if (s1 == null)
					{
						result = -1;
					}
					else if (s2 == null)
					{
						result = 1;
					}
					else
					{
						String grade1 = s1.getGrade();
						String grade2 = s2.getGrade();
						if (grade1 == null)
						{
							grade1 = "";
						}
						if (grade2 == null)
						{
							grade2 = "";
						}

						// if scale is points
						if ((s1.getAssignment().getContent().getTypeOfGrade() == 3)
								&& ((s2.getAssignment().getContent().getTypeOfGrade() == 3)))
						{
							if ("".equals(grade1))
							{
								result = -1;
							}
							else if ("".equals(grade2))
							{
								result = 1;
							}
							else
							{
								result = compareDouble(grade1, grade2);
							}
						}
						else
						{
							result = compareString(grade1, grade2);
						}
					}
				}
			}
			else if (m_criteria.equals(SORTED_GRADE_SUBMISSION_BY_RELEASED))
			{
				// sort by submission status
				SubmitterSubmission u1 = (SubmitterSubmission) o1;
				SubmitterSubmission u2 = (SubmitterSubmission) o2;

				if (u1 == null || u2 == null)
				{
					result = -1;
				}
				else
				{
					AssignmentSubmission s1 = u1.getSubmission();
					AssignmentSubmission s2 = u2.getSubmission();

					if (s1 == null)
					{
						result = -1;
					}
					else if (s2 == null)
					{
						result = 1;
					}
					else
					{
						// sort by submission released
						String released1 = (Boolean.valueOf(s1.getGradeReleased())).toString();
						String released2 = (Boolean.valueOf(s2.getGradeReleased())).toString();

						result = compareString(released1, released2);
					}
				}
			}
			/****** for other sort on submissions **/
			else if (m_criteria.equals(SORTED_SUBMISSION_BY_LASTNAME))
			{
				// sorted by the submitters sort name
				AssignmentSubmission _a1 = (AssignmentSubmission)o1;
				AssignmentSubmission _a2 = (AssignmentSubmission)o2;
				String _s1 = "";
				String _s2 = "";

				if (_a1.getAssignment().isGroup()) {
					try 
                                        {
						Site site = SiteService.getSite(_a1.getAssignment().getContext());
                              			_s1 = site.getGroup(_a1.getSubmitterId()).getTitle();
                                        }
					catch (Throwable _dfef) 
					{
					}
				}
				else
				{
					try
					{
                                            _s1 = UserDirectoryService.getUser(_a1.getSubmitterId()).getSortName();
                                        }
                                        catch (UserNotDefinedException e)
						{
                                             M_log.warn(this + ": cannot find user id=" + _a1.getSubmitterId() + e.getMessage() + "");
                                        }
				}
                                if (_a2.getAssignment().isGroup()) {
                                        try 
					{
                                                Site site = SiteService.getSite(_a2.getAssignment().getContext());
                                                _s2 = site.getGroup(_a2.getSubmitterId()).getTitle();
					}
                                        catch (Throwable _dfef) 
                                        { // TODO empty exception block
					}
				}
                                else
				{
                                        try
					{
                                            _s2 = UserDirectoryService.getUser(_a2.getSubmitterId()).getSortName();
                                        }
                                        catch (UserNotDefinedException e)
					{
                                             M_log.warn(this + ": cannot find user id=" + _a2.getSubmitterId() + e.getMessage() + "");
					}
				}

				result = _s1.compareTo(_s2); //compareString(submitters1, submitters2);
			}
			else if (m_criteria.equals(SORTED_SUBMISSION_BY_SUBMIT_TIME))
			{
				// sorted by submission time
				Time t1 = ((AssignmentSubmission) o1).getTimeSubmitted();
				Time t2 = ((AssignmentSubmission) o2).getTimeSubmitted();

				if (t1 == null)
				{
					result = -1;
				}
				else if (t2 == null)
				{
					result = 1;
				}
				else if (t1.before(t2))
				{
					result = -1;
				}
				else
				{
					result = 1;
				}
			}
			else if (m_criteria.equals(SORTED_SUBMISSION_BY_STATUS))
			{
				// sort by submission status
				result = compareString(((AssignmentSubmission) o1).getStatus(), ((AssignmentSubmission) o2).getStatus());
			}
			else if (m_criteria.equals(SORTED_SUBMISSION_BY_GRADE))
			{
				// sort by submission grade
				String grade1 = ((AssignmentSubmission) o1).getGrade();
				String grade2 = ((AssignmentSubmission) o2).getGrade();
				if (grade1 == null)
				{
					grade1 = "";
				}
				if (grade2 == null)
				{
					grade2 = "";
				}

				// if scale is points
				if ((((AssignmentSubmission) o1).getAssignment().getContent().getTypeOfGrade() == 3)
						&& ((((AssignmentSubmission) o2).getAssignment().getContent().getTypeOfGrade() == 3)))
				{
					if ("".equals(grade1))
					{
						result = -1;
					}
					else if ("".equals(grade2))
					{
						result = 1;
					}
					else
					{
						result = compareDouble(grade1, grade2);
					}
				}
				else
				{
					result = compareString(grade1, grade2);
				}
			}
			else if (m_criteria.equals(SORTED_SUBMISSION_BY_GRADE))
			{
				// sort by submission grade
				String grade1 = ((AssignmentSubmission) o1).getGrade();
				String grade2 = ((AssignmentSubmission) o2).getGrade();
				if (grade1 == null)
				{
					grade1 = "";
				}
				if (grade2 == null)
				{
					grade2 = "";
				}

				// if scale is points
				if ((((AssignmentSubmission) o1).getAssignment().getContent().getTypeOfGrade() == 3)
						&& ((((AssignmentSubmission) o2).getAssignment().getContent().getTypeOfGrade() == 3)))
				{
					if ("".equals(grade1))
					{
						result = -1;
					}
					else if ("".equals(grade2))
					{
						result = 1;
					}
					else
					{
						result = compareDouble(grade1, grade2);
					}
				}
				else
				{
					result = compareString(grade1, grade2);
				}
			}
			else if (m_criteria.equals(SORTED_SUBMISSION_BY_MAX_GRADE))
			{
				Assignment a1 = ((AssignmentSubmission) o1).getAssignment();
				Assignment a2 = ((AssignmentSubmission) o2).getAssignment();
				String maxGrade1 = maxGrade(a1.getContent().getTypeOfGrade(), a1);
				String maxGrade2 = maxGrade(a2.getContent().getTypeOfGrade(), a2);

				try
				{
					// do integer comparation inside point grade type
					int max1 = Integer.parseInt(maxGrade1);
					int max2 = Integer.parseInt(maxGrade2);
					result = (max1 < max2) ? -1 : 1;
				}
				catch (NumberFormatException e)
				{
					M_log.warn(this + ":AssignmentComparator compare" + e.getMessage());
					// otherwise do an alpha-compare
					result = maxGrade1.compareTo(maxGrade2);
				}
			}
			else if (m_criteria.equals(SORTED_SUBMISSION_BY_RELEASED))
			{
				// sort by submission released
				String released1 = (Boolean.valueOf(((AssignmentSubmission) o1).getGradeReleased())).toString();
				String released2 = (Boolean.valueOf(((AssignmentSubmission) o2).getGradeReleased())).toString();

				result = compareString(released1, released2);
			}
			else if (m_criteria.equals(SORTED_SUBMISSION_BY_ASSIGNMENT))
			{
				// sort by submission's assignment
				String title1 = ((AssignmentSubmission) o1).getAssignment().getContent().getTitle();
				String title2 = ((AssignmentSubmission) o2).getAssignment().getContent().getTitle();

				result = compareString(title1, title2);
			}
			/*************** sort user by sort name ***************/
			else if (m_criteria.equals(SORTED_USER_BY_SORTNAME))
			{
				// sort by user's sort name
				String name1 = ((User) o1).getSortName();
				String name2 = ((User) o2).getSortName();

				result = compareString(name1, name2);
			}

			// sort ascending or descending
			if (!Boolean.valueOf(m_asc))
			{
				result = -result;
			}
			return result;
		}

		/**
		 * returns AssignmentSubmission object for given assignment by current user
		 * @param a
		 * @return
		 */
		protected AssignmentSubmission findAssignmentSubmission (Assignment a) {
			AssignmentSubmission rv = null;
			try
			{
				rv = AssignmentService.getSubmission(a.getReference(), UserDirectoryService.getCurrentUser());
			}
			catch (IdUnusedException e)
			{
				M_log.warn(this + "compare: " + rb.getFormattedMessage("cannotfin_assignment", new Object[]{a.getReference()}));
			}
			catch (PermissionException e)
			{
				
			}
			return rv;	
		}
		/**
		 * Compare two strings as double values. Deal with the case when either of the strings cannot be parsed as double value.
		 * @param grade1
		 * @param grade2
		 * @return
		 */
		private int compareDouble(String grade1, String grade2) {
			int result;
			try
			{
				result = (Double.valueOf(grade1)).doubleValue() > (Double.valueOf(grade2)).doubleValue() ? 1 : -1;
			}
			catch (Exception formatException)
			{
				// in case either grade1 or grade2 cannot be parsed as Double
				result = compareString(grade1, grade2);
				M_log.warn(this + ":AssignmentComparator compareDouble " + formatException.getMessage());
			}
			return result;
		} // compareDouble

		private int compareString(String s1, String s2) 
		{
			int result;
			if (s1 == null && s2 == null) {
				result = 0;
			} else if (s2 == null) {
				result = 1;
			} else if (s1 == null) {
				result = -1;
			} else {
				result = collator.compare(s1.toLowerCase(), s2.toLowerCase());
			}
			return result;
		}

		/**
		 * get assignment maximun grade available based on the assignment grade type
		 *
		 * @param gradeType
		 *        The int value of grade type
		 * @param a
		 *        The assignment object
		 * @return The max grade String
		 */
		private String maxGrade(int gradeType, Assignment a)
		{
			String maxGrade = "";

			if (gradeType == -1)
			{
				// Grade type not set
				maxGrade = rb.getString("granotset");
			}
			else if (gradeType == 1)
			{
				// Ungraded grade type
				maxGrade = rb.getString("gen.nograd");
			}
			else if (gradeType == 2)
			{
				// Letter grade type
				maxGrade = "A";
			}
			else if (gradeType == 3)
			{
				// Score based grade type
				maxGrade = Integer.toString(a.getContent().getMaxGradePoint());
			}
			else if (gradeType == 4)
			{
				// Pass/fail grade type
				maxGrade = rb.getString("pass");
			}
			else if (gradeType == 5)
			{
				// Grade type that only requires a check
				maxGrade = rb.getString("check");
			}

			return maxGrade;

		} // maxGrade

	} // DiscussionComparator

	/**
	 * Fire up the permissions editor
	 */
	public void doPermissions(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		if (!alertGlobalNavigation(state, data))
		{
			// we are changing the view, so start with first page again.
			resetPaging(state);

			// clear search form
			doSearch_clear(data, null);

			if (SiteService.allowUpdateSite((String) state.getAttribute(STATE_CONTEXT_STRING)))
			{
				// get into helper mode with this helper tool
				startHelper(data.getRequest(), "sakai.permissions.helper");

				String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
				String siteRef = SiteService.siteReference(contextString);

				// setup for editing the permissions of the site for this tool, using the roles of this site, too
				state.setAttribute(PermissionsHelper.TARGET_REF, siteRef);

				// ... with this description
				state.setAttribute(PermissionsHelper.DESCRIPTION, rb.getString("setperfor") + " "
						+ SiteService.getSiteDisplay(contextString));

				// ... showing only locks that are prpefixed with this
				state.setAttribute(PermissionsHelper.PREFIX, "asn.");
				
				// ... pass the resource loader object
				ResourceLoader pRb = new ResourceLoader("permissions");
				HashMap<String, String> pRbValues = new HashMap<String, String>();
				for (Iterator<Map.Entry<String, Object>> iEntries = pRb.entrySet().iterator();iEntries.hasNext();)
				{
					Map.Entry<String, Object> entry = iEntries.next();
					pRbValues.put(entry.getKey(), (String) entry.getValue());
					
				}
				state.setAttribute("permissionDescriptions",  pRbValues);
				
				String groupAware = ToolManager.getCurrentTool().getRegisteredConfig().getProperty("groupAware");
				state.setAttribute("groupAware", groupAware != null?Boolean.valueOf(groupAware):Boolean.FALSE);

				// disable auto-updates while leaving the list view
				justDelivered(state);
			}

			// reset the global navigaion alert flag
			if (state.getAttribute(ALERT_GLOBAL_NAVIGATION) != null)
			{
				state.removeAttribute(ALERT_GLOBAL_NAVIGATION);
			}

			// switching back to assignment list view
			state.setAttribute(STATE_SELECTED_VIEW, MODE_LIST_ASSIGNMENTS);
			doList_assignments(data);
		}

	} // doPermissions

	/**
	 * transforms the Iterator to List
	 */
	private List iterator_to_list(Iterator l)
	{
		List v = new ArrayList();
		while (l.hasNext())
		{
			v.add(l.next());
		}
		return v;
	} // iterator_to_list

	/**
	 * Implement this to return alist of all the resources that there are to page. Sort them as appropriate.
	 */
	protected List readResourcesPage(SessionState state, int first, int last)
	{

		List returnResources = (List) state.getAttribute(STATE_PAGEING_TOTAL_ITEMS);

		PagingPosition page = new PagingPosition(first, last);
		page.validate(returnResources.size());
		returnResources = returnResources.subList(page.getFirst() - 1, page.getLast());

		return returnResources;

	} // readAllResources

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.cheftool.PagedResourceActionII#sizeResources(org.sakaiproject.service.framework.session.SessionState)
	 */
	protected int sizeResources(SessionState state)
	{
		String mode = (String) state.getAttribute(STATE_MODE);
		String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
		// all the resources for paging
		List returnResources = new ArrayList();

		boolean allowAddAssignment = AssignmentService.allowAddAssignment(contextString);
		if (MODE_LIST_ASSIGNMENTS.equals(mode))
		{
			String view = "";
	            
			if (state.getAttribute(STATE_SELECTED_VIEW) != null)
			{
				view = (String) state.getAttribute(STATE_SELECTED_VIEW);
			}

			if (allowAddAssignment && view.equals(MODE_LIST_ASSIGNMENTS))
			{
				// read all Assignments
				returnResources = AssignmentService.getListAssignmentsForContext((String) state
						.getAttribute(STATE_CONTEXT_STRING));
			}
			else if (allowAddAssignment && view.equals(MODE_STUDENT_VIEW)
					|| (!allowAddAssignment && AssignmentService.allowAddSubmission((String) state
						.getAttribute(STATE_CONTEXT_STRING))))
			{
				// in the student list view of assignments
				Iterator assignments = AssignmentService
						.getAssignmentsForContext(contextString);
				Time currentTime = TimeService.newTime();
				while (assignments.hasNext())
				{
					Assignment a = (Assignment) assignments.next();
					String deleted = a.getProperties().getProperty(ResourceProperties.PROP_ASSIGNMENT_DELETED);
					if (deleted == null || "".equals(deleted))
					{
						// show not deleted assignments
						Time openTime = a.getOpenTime();
						Time visibleTime = a.getVisibleTime();
						if (
								(
										(openTime != null && currentTime.after(openTime))||
										(visibleTime != null && currentTime.after(visibleTime))
										) && !a.getDraft())
						{
							returnResources.add(a);
						}
					}
					else if (deleted.equalsIgnoreCase(Boolean.TRUE.toString()) && (a.getContent().getTypeOfSubmission() != Assignment.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION) 
							&& getSubmission(a.getReference(), (User) state.getAttribute(STATE_USER), "sizeResources", state) != null)
					{
						// and those deleted but not non-electronic assignments but the user has made submissions to them
						returnResources.add(a);
					}
				}
			}
			else
			{
				// read all Assignments
				returnResources = AssignmentService.getListAssignmentsForContext((String) state
						.getAttribute(STATE_CONTEXT_STRING));
			}
			
			state.setAttribute(HAS_MULTIPLE_ASSIGNMENTS, Boolean.valueOf(returnResources.size() > 1));
		}
		else if (MODE_INSTRUCTOR_REORDER_ASSIGNMENT.equals(mode))
		{
			returnResources = AssignmentService.getListAssignmentsForContext((String) state
					.getAttribute(STATE_CONTEXT_STRING));
		}
		else if (MODE_INSTRUCTOR_REPORT_SUBMISSIONS.equals(mode))
		{
			
			initViewSubmissionListOption(state);
			String allOrOneGroup = (String) state.getAttribute(VIEW_SUBMISSION_LIST_OPTION);
			String search = (String) state.getAttribute(VIEW_SUBMISSION_SEARCH);
			Boolean searchFilterOnly = (state.getAttribute(SUBMISSIONS_SEARCH_ONLY) != null && ((Boolean) state.getAttribute(SUBMISSIONS_SEARCH_ONLY)) ? Boolean.TRUE:Boolean.FALSE);

		    Boolean has_multiple_groups_for_user = false;
		    List submissions = new ArrayList();

		    List assignments = iterator_to_list(AssignmentService.getAssignmentsForContext(contextString));
		    if (assignments.size() > 0) {
			try
			{
				// get the site object first
				Site site = SiteService.getSite(contextString);
				for (int j = 0; j < assignments.size(); j++)
				{
					Assignment a = (Assignment) assignments.get(j);
					List<String> submitterIds = AssignmentService.getSubmitterIdList(searchFilterOnly.toString(), allOrOneGroup, search, a.getReference(), contextString);
					Collection<String> _dupUsers = new ArrayList<String>();
					if (a.isGroup()) {
						Collection<Group> submitterGroups = AssignmentService.getSubmitterGroupList(searchFilterOnly.toString(), allOrOneGroup, "", a.getReference(), contextString);
						if (submitterGroups != null && !submitterGroups.isEmpty())
						{
							for (Iterator<Group> iSubmitterGroupsIterator = submitterGroups.iterator(); iSubmitterGroupsIterator.hasNext();)
							{
								Group gId = iSubmitterGroupsIterator.next();
								submitterIds.add(gId.getId());
							}
					    _dupUsers = usersInMultipleGroups(a, true);
						}
					}
					//get the list of users which are allowed to grade this assignment
					List allowGradeAssignmentUsers = AssignmentService.allowGradeAssignmentUsers(a.getReference());
					String deleted = a.getProperties().getProperty(ResourceProperties.PROP_ASSIGNMENT_DELETED);
					if ((deleted == null || "".equals(deleted)) && (!a.getDraft()) && AssignmentService.allowGradeSubmission(a.getReference()))
					{
						try
						{
							List assignmentSubmissions = AssignmentService.getSubmissions(a);
							for (int k = 0; k < assignmentSubmissions.size(); k++)
							{
								AssignmentSubmission s = (AssignmentSubmission) assignmentSubmissions.get(k);
								if (s != null && (s.getSubmitted() 
								        || (s.getReturned() && (s.getTimeLastModified().before(s.getTimeReturned())))))
								{
									//If the group search is null or if it contains the group
									if (submitterIds.contains(s.getSubmitterId())){
										if (a.isGroup()) {
											User[] _users = s.getSubmitters();
											for (int m=0; _users != null && m < _users.length; m++) {
												Member member = site.getMember(_users[m].getId());
												if (member != null && member.isActive()) {
													// only include the active student submission
													// conder TODO create temporary submissions
													SubmitterSubmission _new_sub = new SubmitterSubmission(_users[m], s);
													_new_sub.setGroup(site.getGroup(s.getSubmitterId()));
													if (_dupUsers.size() > 0 && _dupUsers.contains(_users[m].getId())) {
														_new_sub.setMultiGroup(true);
														has_multiple_groups_for_user = true;
													}
													submissions.add(_new_sub);
												}
											}
										} else {
											if (s.getSubmitterId() != null && !allowGradeAssignmentUsers.contains(s.getSubmitterId())) {
												// find whether the submitter is still an active member of the site
												Member member = site.getMember(s.getSubmitterId());
												if(member != null && member.isActive()) {
													// only include the active student submission
													try
													{
														SubmitterSubmission _new_sub = new SubmitterSubmission(UserDirectoryService.getUser(s.getSubmitterId()), s);
														submissions.add(_new_sub);
													}
													catch (UserNotDefinedException e)
													{
														M_log.warn(this + ":sizeResources cannot find user id=" + s.getSubmitterId() + e.getMessage() + "");
													}
												}
											}
										}
									}
								} // if-else
							}
						}
						catch (Exception e)
						{
							M_log.warn(this + ":sizeResources " + e.getMessage());
						}
					}
				}
				if (has_multiple_groups_for_user) {
				    addAlert(state, rb.getString("group.user.multiple.error"));
				}
			}
			catch (IdUnusedException idUnusedException)
			{
				M_log.warn(this + ":sizeResources " + idUnusedException.getMessage() + " site id=" + contextString);
			}
			} // end if
		    returnResources = submissions;
		}
		else if (MODE_INSTRUCTOR_GRADE_ASSIGNMENT.equals(mode) || MODE_INSTRUCTOR_GRADE_SUBMISSION.equals(mode))
		{
			initViewSubmissionListOption(state);
			String allOrOneGroup = (String) state.getAttribute(VIEW_SUBMISSION_LIST_OPTION);
			String search = (String) state.getAttribute(VIEW_SUBMISSION_SEARCH);
			String aRef = (String) state.getAttribute(EXPORT_ASSIGNMENT_REF);
			Boolean searchFilterOnly = (state.getAttribute(SUBMISSIONS_SEARCH_ONLY) != null && ((Boolean) state.getAttribute(SUBMISSIONS_SEARCH_ONLY)) ? Boolean.TRUE:Boolean.FALSE);
			Assignment assignment = null;

			
			try
			{
				assignment = AssignmentService.getAssignment( aRef );
				if( assignment.getProperties().getBooleanProperty( NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING ) == true )
				{
					allOrOneGroup = "all";
				}
			}
			catch( IdUnusedException ex )
			{
				M_log.warn( ":sizeResources cannot find assignment " + ex.getMessage() );
			}
			catch( PermissionException aPerException )
			{
				M_log.warn( ":sizeResources: Not allowed to get assignment " + aRef + " " + aPerException.getMessage() );
			}
			catch( EntityPropertyNotDefinedException ex )
			{
				M_log.debug( ":sizeResources: property not defined for assignment  " + aRef + " " + ex.getMessage() );
			}
			catch( EntityPropertyTypeException ex )
			{
				M_log.debug( ":sizeResources: property type exception for assignment  " + aRef + " " + ex.getMessage() );
			}

			if ( assignment != null && assignment.isGroup()) {

				Collection<Group> submitterGroups = AssignmentService.getSubmitterGroupList("false", allOrOneGroup, "", aRef, contextString);

				// construct the group-submission list
				if (submitterGroups != null && !submitterGroups.isEmpty())
				{
					for (Iterator<Group> iSubmitterGroupsIterator = submitterGroups.iterator(); iSubmitterGroupsIterator.hasNext();)
					{
						Group gId = iSubmitterGroupsIterator.next();
						// Allow sections to be used for group assigments - https://jira.sakaiproject.org/browse/SAK-22425
						//if (gId.getProperties().get(GROUP_SECTION_PROPERTY) == null) {
						try
						{
							AssignmentSubmission sub = AssignmentService.getSubmission(aRef, gId.getId());
							returnResources.add(new SubmitterSubmission(gId, sub));  // UserSubmission accepts either User or Group
						}
						catch (IdUnusedException subIdException)
						{
							M_log.warn(this + ".sizeResources: looking for submission for unused assignment id " + aRef + subIdException.getMessage());
						}
						catch (PermissionException subPerException)
						{
							M_log.warn(this + ".sizeResources: cannot have permission to access submission of assignment " + aRef + " of group " + gId.getId());
						}
						//}
					}
				}

			} else {

				//List<String> submitterIds = AssignmentService.getSubmitterIdList(searchFilterOnly.toString(), allOrOneGroup, search, aRef, contextString);
				Map<User, AssignmentSubmission> submitters = AssignmentService.getSubmitterMap(searchFilterOnly.toString(), allOrOneGroup, search, aRef, contextString);

				// construct the user-submission list
				for (User u : submitters.keySet())
				{
					String uId = u.getId();

					AssignmentSubmission sub = submitters.get(u);
					SubmitterSubmission us = new SubmitterSubmission(u, sub);
					String submittedById = (String)sub.getProperties().get(AssignmentSubmission.SUBMITTER_USER_ID);
					if ( submittedById != null) {
						try {
							us.setSubmittedBy(UserDirectoryService.getUser(submittedById));
						} catch (UserNotDefinedException ex1) {
							M_log.warn(this + ":sizeResources cannot find submitter id=" + uId + ex1.getMessage());
						}
					}
					returnResources.add(us);
				}
			}
		}

		// sort them all
		String ascending = "true";
		String sort = "";
		ascending = (String) state.getAttribute(SORTED_ASC);
		sort = (String) state.getAttribute(SORTED_BY);
		if (MODE_INSTRUCTOR_GRADE_ASSIGNMENT.equals(mode) || MODE_INSTRUCTOR_GRADE_SUBMISSION.equals(mode)
			&& (sort == null || !sort.startsWith("sorted_grade_submission_by")))
		{
			ascending = (String) state.getAttribute(SORTED_GRADE_SUBMISSION_ASC);
			sort = (String) state.getAttribute(SORTED_GRADE_SUBMISSION_BY);
		}
		else if (MODE_INSTRUCTOR_REPORT_SUBMISSIONS.equals(mode) && (sort == null || sort.startsWith("sorted_submission_by")))
		{
			ascending = (String) state.getAttribute(SORTED_SUBMISSION_ASC);
			sort = (String) state.getAttribute(SORTED_SUBMISSION_BY);
		}
		else
		{
			ascending = (String) state.getAttribute(SORTED_ASC);
			sort = (String) state.getAttribute(SORTED_BY);
		}
		
		if ((returnResources.size() > 1) && !MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT.equals(mode))
		{
			AssignmentComparator ac = new AssignmentComparator(state, sort, ascending);
			
			// figure out if we have to sort by anonymous id
			if (SORTED_GRADE_SUBMISSION_BY_LASTNAME.equals(sort) &&
					(MODE_INSTRUCTOR_GRADE_ASSIGNMENT.equals(mode) || MODE_INSTRUCTOR_GRADE_SUBMISSION.equals(mode)))
			{
				String aRef = (String) state.getAttribute(EXPORT_ASSIGNMENT_REF);
			try
			{
					Assignment assignment = AssignmentService.getAssignment(aRef);
					if (assignment != null)
					{
						ResourceProperties props = assignment.getProperties();
						if (props != null)
						{
							ac.setAnon(props.getBooleanProperty(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING));
			}
					}
				}
				catch (IdUnusedException iue)
				{
					// ignore, continue with default sort
				}
				catch (PermissionException pe)
				{
					// ignore, continue with default sort
				}
				catch (EntityPropertyNotDefinedException epnde)
				{
					// ignore, continue with default sort
				}
				catch (EntityPropertyTypeException epte)
				{
					// ignore, continue with default sort
				}
			}
			
			try
			{
				Collections.sort(returnResources, ac);
			}
			catch (Exception e)
			{
				// log exception during sorting for helping debugging
				M_log.warn(this + ":sizeResources mode=" + mode + " sort=" + sort + " ascending=" + ascending + " " + e.getStackTrace());
			}
		}

		// record the total item number
		state.setAttribute(STATE_PAGEING_TOTAL_ITEMS, returnResources);
		
		return returnResources.size();
	}

	

	public void doView(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		if (!alertGlobalNavigation(state, data))
		{
			// we are changing the view, so start with first page again.
			resetPaging(state);

			// clear search form
			doSearch_clear(data, null);

			String viewMode = data.getParameters().getString("view");
			state.setAttribute(STATE_SELECTED_VIEW, viewMode);

			if (MODE_LIST_ASSIGNMENTS.equals(viewMode))
			{
				doList_assignments(data);
			}
			else if (MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT.equals(viewMode))
			{
				doView_students_assignment(data);
			}
			else if (MODE_INSTRUCTOR_REPORT_SUBMISSIONS.equals(viewMode))
			{
				doReport_submissions(data);
			}
			else if (MODE_STUDENT_VIEW.equals(viewMode))
			{
				doView_student(data);
			}

			// reset the global navigaion alert flag
			if (state.getAttribute(ALERT_GLOBAL_NAVIGATION) != null)
			{
				state.removeAttribute(ALERT_GLOBAL_NAVIGATION);
			}
		}

	} // doView

	/**
	 * put those variables related to 2ndToolbar into context
	 */
	private void add2ndToolbarFields(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		context.put("totalPageNumber", Integer.valueOf(totalPageNumber(state)));
		context.put("form_search", FORM_SEARCH);
		context.put("formPageNumber", FORM_PAGE_NUMBER);
		context.put("prev_page_exists", state.getAttribute(STATE_PREV_PAGE_EXISTS));
		context.put("next_page_exists", state.getAttribute(STATE_NEXT_PAGE_EXISTS));
		context.put("current_page", state.getAttribute(STATE_CURRENT_PAGE));
		context.put("selectedView", state.getAttribute(STATE_MODE));

	} // add2ndToolbarFields

	/**
	 * valid grade for point based type
	 * returns a double value in a string from the localized input
	 */
	private String validPointGrade(SessionState state, String grade, int factor)
	{
		if (grade != null && !"".equals(grade))
		{
			if (grade.startsWith("-"))
			{
				// check for negative sign
				addAlert(state, rb.getString("plesuse3"));
			}
			else
			{	
				int dec = (int)Math.log10(factor);
				NumberFormat nbFormat = FormattedText.getNumberFormat();
				DecimalFormat dcFormat = (DecimalFormat) nbFormat;
				String decSeparator = FormattedText.getDecimalSeparator();
				
				// only the right decimal separator is allowed and no other grouping separator
				if ((",".equals(decSeparator) && grade.indexOf(".") != -1) ||
						(".".equals(decSeparator) && grade.indexOf(",") != -1) ||
						grade.indexOf(" ") != -1) {
					addAlert(state, rb.getString("plesuse1"));
					return grade;
				}
				
				// parse grade from localized number format
				int index = grade.indexOf(decSeparator);
				if (index != -1)
				{
					// when there is decimal points inside the grade, scale the number by "factor"
					// but only one decimal place is supported
					// for example, change 100.0 to 1000
					if (!decSeparator.equals(grade))
					{
						if (grade.length() > index + dec + 1)
						{
							// if there are more than "factor" decimal points
							addAlert(state, rb.getFormattedMessage("plesuse2", new Object[]{String.valueOf(dec)}));
						}
						else
						{
							// decimal points is the only allowed character inside grade
							// replace it with '1', and try to parse the new String into int
							String zeros = "";
							for (int i=0; i<dec; i++) {
								zeros = zeros.concat("0");
							}
							String gradeString = grade.endsWith(decSeparator) ? grade.substring(0, index).concat(zeros) : 
								grade.substring(0, index).concat(grade.substring(index + 1));
							try
							{
								nbFormat.parse(gradeString);
								try
								{
									Integer.parseInt(gradeString);
								}
								catch (NumberFormatException e)
								{
									M_log.warn(this + ":validPointGrade " + e.getMessage());
									alertInvalidPoint(state, gradeString, factor);
								}
							}
							catch (ParseException e)
							{
								M_log.warn(this + ":validPointGrade " + e.getMessage());
								addAlert(state, rb.getString("plesuse1"));
							}
						}
					}
					else
					{
						// grade is decSeparator
						addAlert(state, rb.getString("plesuse1"));
					}
				}
				else
				{
					// There is no decimal point; should be int number
					String gradeString = grade;
					for (int i=0; i<dec; i++) {
						gradeString = gradeString.concat("0");
					}
					try
					{
						nbFormat.parse(gradeString);
						try
						{
							Integer.parseInt(gradeString);
						}
						catch (NumberFormatException e)
						{
							M_log.warn(this + ":validPointGrade " + e.getMessage());
							alertInvalidPoint(state, gradeString, factor);
						}
					}
					catch (ParseException e)
					{
						M_log.warn(this + ":validPointGrade " + e.getMessage());
						addAlert(state, rb.getString("plesuse1"));
					}
				}
			}
		}
		return grade;

	}

	/**
	 * valid grade for point based type
	 */
	private void validLetterGrade(SessionState state, String grade)
	{
		String VALID_CHARS_FOR_LETTER_GRADE = " ABCDEFGHIJKLMNOPQRSTUVWXYZ+-";
		boolean invalid = false;
		if (grade != null)
		{
			grade = grade.toUpperCase();
			for (int i = 0; i < grade.length() && !invalid; i++)
			{
				char c = grade.charAt(i);
				if (VALID_CHARS_FOR_LETTER_GRADE.indexOf(c) == -1)
				{
					invalid = true;
				}
			}
			if (invalid)
			{
				// -------- SAK-24199 (SAKU-274) by Shoji Kajita
				addAlert(state, rb.getFormattedMessage("plesuse0", new Object []{grade}));
				// --------
			}
		}
	}

	private void alertInvalidPoint(SessionState state, String grade, int factor)
	{
		String decSeparator = FormattedText.getDecimalSeparator();
		
		String VALID_CHARS_FOR_INT = "-01234567890";

		boolean invalid = false;
		// case 1: contains invalid char for int
		for (int i = 0; i < grade.length() && !invalid; i++)
		{
			char c = grade.charAt(i);
			if (VALID_CHARS_FOR_INT.indexOf(c) == -1)
			{
				invalid = true;
			}
		}
		if (invalid)
		{
			addAlert(state, rb.getString("plesuse1"));
		}
		else
		{
			int dec = (int)Math.log10(factor);
			int maxInt = Integer.MAX_VALUE / factor;
			int maxDec = Integer.MAX_VALUE - maxInt * factor;
			// case 2: Due to our internal scaling, input String is larger than Integer.MAX_VALUE/10
			addAlert(state, rb.getFormattedMessage("plesuse4", new Object[]{grade.substring(0, grade.length()-dec) + decSeparator + grade.substring(grade.length()-dec),  maxInt + decSeparator + maxDec}));
		}
	}

	/**
	 * display grade properly
	 */
	private String displayGrade(SessionState state, String grade, int factor)
	{
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			if (grade != null && (grade.length() >= 1))
			{
				int dec = (int)Math.log10(factor);
				NumberFormat nbFormat = FormattedText.getNumberFormat(dec,dec,false);
				DecimalFormat dcformat = (DecimalFormat) nbFormat;
				String decSeparator = FormattedText.getDecimalSeparator();
				
				if (grade.indexOf(decSeparator) != -1)
				{
					if (grade.startsWith(decSeparator))
					{
						grade = "0".concat(grade);
					}
					else if (grade.endsWith(decSeparator))
					{
						for (int i=0; i<dec; i++) {
							grade = grade.concat("0");
						}
					}
				}
				else
				{
					try
					{
						Integer.parseInt(grade);
						int length = grade.length();
						if (length > dec) {
							grade = grade.substring(0, grade.length() - dec) + decSeparator + grade.substring(grade.length() - dec);
						}
						else {
							String newGrade = "0".concat(decSeparator);
							for (int i = length; i < dec; i++) {
								newGrade = newGrade.concat("0");
							}
							grade = newGrade.concat(grade);
						}
					}
					catch (NumberFormatException e)
					{
						// alert
						alertInvalidPoint(state, grade, factor);
						M_log.warn(this + ":displayGrade cannot parse grade into integer grade = " + grade + e.getMessage());
					}
				}			
				try {
					// show grade in localized number format
					Double dblGrade = dcformat.parse(grade).doubleValue();
					grade = nbFormat.format(dblGrade);
				}
				catch (Exception e) {
					// alert
					alertInvalidPoint(state, grade, factor);
					M_log.warn(this + ":displayGrade cannot parse grade into integer grade = " + grade + e.getMessage());
				}
			}
			else
			{
				grade = "";
			}
		}
		return grade;

	} // displayGrade

	/**
	 * scale the point value by "factor" if there is a valid point grade
	 */
	private String scalePointGrade(SessionState state, String point, int factor)
	{
		String decSeparator = FormattedText.getDecimalSeparator();
		int dec = (int)Math.log10(factor);

		point = validPointGrade(state, point, factor);

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			if (point != null && (point.length() >= 1))
			{
				// when there is decimal points inside the grade, scale the number by "factor"
				// but only one decimal place is supported
				// for example, change 100.0 to 1000
				int index = point.indexOf(decSeparator);
				if (index != -1)
				{
					if (index == 0)
					{
						// if the point is the first char, add a 0 for the integer part
						point = "0".concat(point.substring(1));
					}
					else if (index < point.length() - 1)
					{
						// adjust the number of decimals, adding 0's to the end 
						int length = point.length() - index - 1;
						for (int i=length; i<dec; i++) {
							point = point + "0";
						}
						
						// use scale integer for gradePoint
						point = point.substring(0, index) + point.substring(index + 1);
					}
					else
					{
						// decimal point is the last char
						point = point.substring(0, index);
						for (int i=0; i<dec; i++) {
							point = point + "0"; 
						}
					}
				}
				else
				{
					// if there is no decimal place, scale up the integer by "factor"
					for (int i=0; i<dec; i++) {
						point = point + "0";
					}
				}

				// filter out the "zero grade"
				if ("00".equals(point))
				{
					point = "0";
				}
			}
		}
		
		if (StringUtils.trimToNull(point) != null)
		{
			try
			{
				point = Integer.valueOf(point).toString();
			}
			catch (Exception e)
			{
				M_log.warn(this + " scalePointGrade: cannot parse " + point + " into integer. " + e.getMessage());
			}
		}
		return point;

	} // scalePointGrade

	/**
	 * Processes formatted text that is coming back from the browser (from the formatted text editing widget).
	 *
	 * @param state
	 *        Used to pass in any user-visible alerts or errors when processing the text
	 * @param strFromBrowser
	 *        The string from the browser
	 * @param checkForFormattingErrors
	 *        Whether to check for formatted text errors - if true, look for errors in the formatted text. If false, accept the formatted text without looking for errors.
	 * @return The formatted text
	 */
	private String processFormattedTextFromBrowser(SessionState state, String strFromBrowser, boolean checkForFormattingErrors)
	{
		StringBuilder alertMsg = new StringBuilder();
		boolean replaceWhitespaceTags = true;
		String text = FormattedText.processFormattedText(strFromBrowser, alertMsg, checkForFormattingErrors,
				replaceWhitespaceTags);
		if (alertMsg.length() > 0) addAlert(state, alertMsg.toString());
		return text;
	}

	/**
	 * Processes the given assignmnent feedback text, as returned from the user's browser. Makes sure that the Chef-style markup {{like this}} is properly balanced.
	 */
	private String processAssignmentFeedbackFromBrowser(SessionState state, String strFromBrowser)
	{
		if (strFromBrowser == null || strFromBrowser.length() == 0) return strFromBrowser;

		StringBuilder buf = new StringBuilder(strFromBrowser);
		int pos = -1;
		int numopentags = 0;

		while ((pos = buf.indexOf("{{")) != -1)
		{
			buf.replace(pos, pos + "{{".length(), "<ins>");
			numopentags++;
		}

		while ((pos = buf.indexOf("}}")) != -1)
		{
			buf.replace(pos, pos + "}}".length(), "</ins>");
			numopentags--;
		}

		while (numopentags > 0)
		{
			buf.append("</ins>");
			numopentags--;
		}

		boolean checkForFormattingErrors = true; // so that grading isn't held up by formatting errors
		buf = new StringBuilder(processFormattedTextFromBrowser(state, buf.toString(), checkForFormattingErrors));

		while ((pos = buf.indexOf("<ins>")) != -1)
		{
			buf.replace(pos, pos + "<ins>".length(), "{{");
		}

		while ((pos = buf.indexOf("</ins>")) != -1)
		{
			buf.replace(pos, pos + "</ins>".length(), "}}");
		}

		return buf.toString();
	}

	/**
	 * Called to deal with old Chef-style assignment feedback annotation, {{like this}}.
	 *
	 * @param value
	 *        A formatted text string that may contain {{}} style markup
	 * @return HTML ready to for display on a browser
	 */
	public static String escapeAssignmentFeedback(String value)
	{
		if (value == null || value.length() == 0) return value;

		value = fixAssignmentFeedback(value);

		StringBuilder buf = new StringBuilder(value);
		int pos = -1;

		while ((pos = buf.indexOf("{{")) != -1)
		{
			buf.replace(pos, pos + "{{".length(), "<span class='highlight'>");
		}

		while ((pos = buf.indexOf("}}")) != -1)
		{
			buf.replace(pos, pos + "}}".length(), "</span>");
		}

		return FormattedText.escapeHtmlFormattedText(buf.toString());
	}

	/**
	 * Escapes the given assignment feedback text, to be edited as formatted text (perhaps using the formatted text widget)
	 */
	public static String escapeAssignmentFeedbackTextarea(String value)
	{
		if (value == null || value.length() == 0) return value;

		value = fixAssignmentFeedback(value);

		return FormattedText.escapeHtmlFormattedTextarea(value);
	}

	/**
	 * Apply the fix to pre 1.1.05 assignments submissions feedback.
	 */
	private static String fixAssignmentFeedback(String value)
	{
		if (value == null || value.length() == 0) return value;

		StringBuilder buf = new StringBuilder(value);
		int pos = -1;

		// <br/> -> \n
		while ((pos = buf.indexOf("<br/>")) != -1)
		{
			buf.replace(pos, pos + "<br/>".length(), "\n");
		}

		// <span class='chefAlert'>( -> {{
		while ((pos = buf.indexOf("<span class='chefAlert'>(")) != -1)
		{
			buf.replace(pos, pos + "<span class='chefAlert'>(".length(), "{{");
		}

		// )</span> -> }}
		while ((pos = buf.indexOf(")</span>")) != -1)
		{
			buf.replace(pos, pos + ")</span>".length(), "}}");
		}

		while ((pos = buf.indexOf("<ins>")) != -1)
		{
			buf.replace(pos, pos + "<ins>".length(), "{{");
		}

		while ((pos = buf.indexOf("</ins>")) != -1)
		{
			buf.replace(pos, pos + "</ins>".length(), "}}");
		}

		return buf.toString();

	} // fixAssignmentFeedback

	/**
	 * Apply the fix to pre 1.1.05 assignments submissions feedback.
	 */
	public static String showPrevFeedback(String value)
	{
		if (value == null || value.length() == 0) return value;

		StringBuilder buf = new StringBuilder(value);
		int pos = -1;

		// <br/> -> \n
		while ((pos = buf.indexOf("\n")) != -1)
		{
			buf.replace(pos, pos + "\n".length(), "<br />");
		}

		return buf.toString();

	} // showPrevFeedback

	private boolean alertGlobalNavigation(SessionState state, RunData data)
	{
		String mode = (String) state.getAttribute(STATE_MODE);
		ParameterParser params = data.getParameters();

		if (MODE_STUDENT_VIEW_SUBMISSION.equals(mode) || MODE_STUDENT_PREVIEW_SUBMISSION.equals(mode)
				|| MODE_STUDENT_VIEW_GRADE.equals(mode) || MODE_INSTRUCTOR_NEW_EDIT_ASSIGNMENT.equals(mode)
				|| MODE_INSTRUCTOR_DELETE_ASSIGNMENT.equals(mode) || MODE_INSTRUCTOR_GRADE_SUBMISSION.equals(mode)
				|| MODE_INSTRUCTOR_PREVIEW_GRADE_SUBMISSION.equals(mode)|| MODE_INSTRUCTOR_PREVIEW_ASSIGNMENT.equals(mode)
				|| MODE_INSTRUCTOR_VIEW_ASSIGNMENT.equals(mode) || MODE_INSTRUCTOR_REORDER_ASSIGNMENT.equals(mode))
		{
			if (state.getAttribute(ALERT_GLOBAL_NAVIGATION) == null)
			{
				addAlert(state, rb.getString("alert.globalNavi"));
				state.setAttribute(ALERT_GLOBAL_NAVIGATION, Boolean.TRUE);

				if (MODE_STUDENT_VIEW_SUBMISSION.equals(mode))
				{
					// save submit inputs
					saveSubmitInputs(state, params);
					state.setAttribute(FilePickerHelper.FILE_PICKER_TITLE_TEXT, rb.getString("gen.addatt"));

					// TODO: file picker to save in dropbox? -ggolden
					// User[] users = { UserDirectoryService.getCurrentUser() };
					// state.setAttribute(ResourcesAction.STATE_SAVE_ATTACHMENT_IN_DROPBOX, users);
				}
				else if (MODE_INSTRUCTOR_NEW_EDIT_ASSIGNMENT.equals(mode))
				{
					setNewAssignmentParameters(data, false);
				}
				else if (MODE_INSTRUCTOR_GRADE_SUBMISSION.equals(mode))
				{
					readGradeForm(data, state, "read");
				}

				return true;
			}
		}

		return false;

	} // alertGlobalNavigation

	/**
	 * Dispatch function inside add submission page
	 */
	public void doRead_add_submission_form(RunData data)
	{
		String option = data.getParameters().getString("option");
		if ("cancel".equals(option))
		{
			// cancel
			doCancel_show_submission(data);
		}
		else if ("preview".equals(option))
		{
			// preview
			doPreview_submission(data);
		}
		else if ("save".equals(option))
		{
			// save draft
			doSave_submission(data);
		}
		else if ("post".equals(option))
		{
			// post
			doPost_submission(data);
		}
		else if ("revise".equals(option))
		{
			// done preview
			doDone_preview_submission(data);
		}
		else if ("attach".equals(option))
		{
			// attach
			ToolSession toolSession = SessionManager.getCurrentToolSession();
			String userId = SessionManager.getCurrentSessionUserId();
			String siteId = SiteService.getUserSiteId(userId);
	        String collectionId = m_contentHostingService.getSiteCollection(siteId);
	        toolSession.setAttribute(FilePickerHelper.DEFAULT_COLLECTION_ID, collectionId);
			doAttachments(data);
		}
		else if ("removeAttachment".equals(option))
		{
			// remove selected attachment
			doRemove_attachment(data);
		}
		else if ("removeNewSingleUploadedFile".equals(option))
		{
			doRemove_newSingleUploadedFile(data);
		}
		else if ("upload".equals(option))
		{
			// upload local file
			doAttachUpload(data, true);
		}
		else if ("uploadSingleFile".equals(option))
		{
			// upload single local file
			doAttachUpload(data, false);
		}
	}
	
	public void doRemove_attachment(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ()); 
		ParameterParser params = data.getParameters();
		
		// save submit inputs before refresh the page
		saveSubmitInputs(state, params);

		String mode = (String) state.getAttribute(STATE_MODE);
		
		String removeAttachmentId = params.getString("currentAttachment");
		List attachments = new ArrayList();
		if(MODE_STUDENT_REVIEW_EDIT.equals(mode))
		{
			attachments = state.getAttribute(PEER_ATTACHMENTS) == null?null:((List) state.getAttribute(PEER_ATTACHMENTS)).isEmpty()?null:(List) state.getAttribute(PEER_ATTACHMENTS);
		} else
		{
			attachments = state.getAttribute(ATTACHMENTS) == null?null:((List) state.getAttribute(ATTACHMENTS)).isEmpty()?null:(List) state.getAttribute(ATTACHMENTS);
		}
		if (attachments != null)
		{
			Reference found =  null;
			for(Object attachment : attachments)
			{
				if (((Reference) attachment).getId().equals(removeAttachmentId))
				{
					found = (Reference) attachment;
					break;
				}
			}
			if (found != null)
			{
				attachments.remove(found);
				// refresh state variable
				if(MODE_STUDENT_REVIEW_EDIT.equals(mode)) {
					state.setAttribute(PEER_ATTACHMENTS, attachments);
				}
				else {
					state.setAttribute(ATTACHMENTS, attachments);
				}
			}
		}

	}

	public void doRemove_newSingleUploadedFile(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());
		state.removeAttribute("newSingleUploadedFile");
		state.removeAttribute("newSingleAttachmentList");
	}

	/**
	 * return returns all groups in a site
	 * @param contextString
	 * @param aRef
	 * @return
	 */
	protected Collection getAllGroupsInSite(String contextString) 
	{
		Collection groups = new ArrayList();
		try {
			Site site = SiteService.getSite(contextString);
			// any group in the site?
			groups = site.getGroups();
		} catch (IdUnusedException e) {
			// TODO Auto-generated catch block
			M_log.info("Problem getting groups for site:"+e.getMessage());
		}
		return groups;
	}
	
	/**
	 * return list of submission object based on the group filter/search result
	 * @param state
	 * @param aRef
	 * @return
	 */
	protected List<AssignmentSubmission> getFilteredSubmitters(SessionState state, String aRef)
	{
		List<AssignmentSubmission> rv = new ArrayList<AssignmentSubmission>();

		String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
		String allOrOneGroup = (String) state.getAttribute(VIEW_SUBMISSION_LIST_OPTION);
		String search = (String) state.getAttribute(VIEW_SUBMISSION_SEARCH);
		Boolean searchFilterOnly = (state.getAttribute(SUBMISSIONS_SEARCH_ONLY) != null && ((Boolean) state.getAttribute(SUBMISSIONS_SEARCH_ONLY)) ? Boolean.TRUE:Boolean.FALSE);
		
		//List<String> submitterIds = AssignmentService.getSubmitterIdList(searchFilterOnly.toString(), allOrOneGroup, search, aRef, contextString);
		Map<User, AssignmentSubmission> submitters = AssignmentService.getSubmitterMap(searchFilterOnly.toString(), allOrOneGroup, search, aRef, contextString);
		
		// construct the user-submission list
		for (AssignmentSubmission sub : submitters.values())
		{
			if (!rv.contains(sub)) rv.add(sub);
		}
		
		return rv;
	}
	
	/**
	 * Set default score for all ungraded non electronic submissions
	 * @param data
	 */
	public void doSet_defaultNotGradedNonElectronicScore(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ()); 
		ParameterParser params = data.getParameters();
		
		String grade = StringUtils.trimToNull(params.getString("defaultGrade"));
		if (grade == null)
		{
			addAlert(state, rb.getString("plespethe2"));
		}
		
		String assignmentId = (String) state.getAttribute(EXPORT_ASSIGNMENT_REF);
		// record the default grade setting for no-submission
		AssignmentEdit aEdit = editAssignment(assignmentId, "doSet_defaultNotGradedNonElectronicScore", state, false); 
		if (aEdit != null)
		{
			aEdit.getPropertiesEdit().addProperty(GRADE_NO_SUBMISSION_DEFAULT_GRADE, grade);
			AssignmentService.commitEdit(aEdit);
		}
			
			Assignment a = getAssignment(assignmentId, "doSet_defaultNotGradedNonElectronicScore", state);
			if (a != null && a.getContent().getTypeOfGrade() == Assignment.SCORE_GRADE_TYPE)
			{
				//for point-based grades
				validPointGrade(state, grade, a.getContent().getFactor());

				if (state.getAttribute(STATE_MESSAGE) == null)
				{
					int maxGrade = a.getContent().getMaxGradePoint();
					try
					{
						if (Integer.parseInt(scalePointGrade(state, grade, a.getContent().getFactor())) > maxGrade)
						{
							if (state.getAttribute(GRADE_GREATER_THAN_MAX_ALERT) == null)
							{
								// alert user first when he enters grade bigger than max scale
								addAlert(state, rb.getFormattedMessage("grad2", new Object[]{grade, displayGrade(state, String.valueOf(maxGrade), a.getContent().getFactor())}));
								state.setAttribute(GRADE_GREATER_THAN_MAX_ALERT, Boolean.TRUE);
							}
							else
							{
								// remove the alert once user confirms he wants to give student higher grade
								state.removeAttribute(GRADE_GREATER_THAN_MAX_ALERT);
							}
						}
					}
					catch (NumberFormatException e)
					{
						M_log.warn(this + ":setDefaultNotGradedNonElectronicScore " + e.getMessage());
						alertInvalidPoint(state, grade, a.getContent().getFactor());
					}
				}
				
				if (state.getAttribute(STATE_MESSAGE) == null)
				{
					grade = scalePointGrade(state, grade, a.getContent().getFactor());
				}
			}
			
			
			if (grade != null && state.getAttribute(STATE_MESSAGE) == null)
			{
				// get the user list
				List submissions = getFilteredSubmitters(state, a.getReference());
				
				for (int i = 0; i<submissions.size(); i++)
				{
					// get the submission object
					AssignmentSubmission submission = (AssignmentSubmission) submissions.get(i);
					if (submission.getSubmitted() && !submission.getGraded())
					{
						String sRef = submission.getReference();
						// update the grades for those existing non-submissions
						AssignmentSubmissionEdit sEdit = editSubmission(sRef, "doSet_defaultNotGradedNonElectronicScore", state);
						if (sEdit != null)
						{
							sEdit.setGrade(grade);
							sEdit.setGraded(true);
							sEdit.setGradedBy(UserDirectoryService.getCurrentUser() == null ? null : UserDirectoryService.getCurrentUser().getId());
							AssignmentService.commitEdit(sEdit);
						}
					}
				}
			}
	}
	
	/**
	 * 
	 */
	public void doSet_defaultNoSubmissionScore(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ()); 
		ParameterParser params = data.getParameters();
		
		String grade = StringUtils.trimToNull(params.getString("defaultGrade"));
		if (grade == null)
		{
			addAlert(state, rb.getString("plespethe2"));
		}
		
		String assignmentId = (String) state.getAttribute(EXPORT_ASSIGNMENT_REF);
		// record the default grade setting for no-submission
		AssignmentEdit aEdit = editAssignment(assignmentId, "doSet_defaultNoSubmissionScore", state, false); 
		if (aEdit != null)
		{
			aEdit.getPropertiesEdit().addProperty(GRADE_NO_SUBMISSION_DEFAULT_GRADE, grade);
			AssignmentService.commitEdit(aEdit);
		}
		
			Assignment a = getAssignment(assignmentId, "doSet_defaultNoSubmissionScore", state);
			if (a != null && a.getContent().getTypeOfGrade() == Assignment.SCORE_GRADE_TYPE)
			{
				//for point-based grades
				validPointGrade(state, grade, a.getContent().getFactor());

				if (state.getAttribute(STATE_MESSAGE) == null)
				{
					int maxGrade = a.getContent().getMaxGradePoint();
					try
					{
						if (Integer.parseInt(scalePointGrade(state, grade, a.getContent().getFactor())) > maxGrade)
						{
							if (state.getAttribute(GRADE_GREATER_THAN_MAX_ALERT) == null)
							{
								// alert user first when he enters grade bigger than max scale
								addAlert(state, rb.getFormattedMessage("grad2", new Object[]{grade, displayGrade(state, String.valueOf(maxGrade), a.getContent().getFactor())}));
								state.setAttribute(GRADE_GREATER_THAN_MAX_ALERT, Boolean.TRUE);
							}
							else
							{
								// remove the alert once user confirms he wants to give student higher grade
								state.removeAttribute(GRADE_GREATER_THAN_MAX_ALERT);
							}
						}
					}
					catch (NumberFormatException e)
					{
						alertInvalidPoint(state, grade, a.getContent().getFactor());
						M_log.warn(this + ":setDefaultNoSubmissionScore " + e.getMessage());
					}
				}
				
				if (state.getAttribute(STATE_MESSAGE) == null)
				{
					grade = scalePointGrade(state, grade, a.getContent().getFactor());
				}
			}
			
			
			if (grade != null && state.getAttribute(STATE_MESSAGE) == null)
			{
				// get the submission list
				List submissions = getFilteredSubmitters(state, a.getReference());
				
				for (int i = 0; i<submissions.size(); i++)
				{
					// get the submission object
					AssignmentSubmission submission = (AssignmentSubmission) submissions.get(i);
					
					if (StringUtils.trimToNull(submission.getGrade()) == null)
					{
						// update the grades for those existing non-submissions
						AssignmentSubmissionEdit sEdit = editSubmission(submission.getReference(), "doSet_defaultNoSubmissionScore", state);
						if (sEdit != null)
						{
							sEdit.setGrade(grade);
							sEdit.setSubmitted(true);
							sEdit.setGraded(true);
							sEdit.setGradedBy(UserDirectoryService.getCurrentUser() == null ? null : UserDirectoryService.getCurrentUser().getId());
							AssignmentService.commitEdit(sEdit);
						}
					}
					else if (StringUtils.trimToNull(submission.getGrade()) != null && !submission.getGraded())
					{
						// correct the grade status if there is a grade but the graded is false
						AssignmentSubmissionEdit sEdit = editSubmission(submission.getReference(), "doSet_defaultNoSubmissionScore", state);
						if (sEdit != null)
						{
							sEdit.setGraded(true);
							sEdit.setGradedBy(UserDirectoryService.getCurrentUser() == null ? null : UserDirectoryService.getCurrentUser().getId());
							AssignmentService.commitEdit(sEdit);
						}
					}
				}
			}
	}

	/**
	 * A utility method to determine users listed in multiple groups
	 * eligible to submit an assignment.  This is a bad situation.
	 * Current mechanism is to error out assignments with this situation
	 * to prevent affected groups from submitting and viewing feedback
	 * and prevent instructors from grading or sending feedback to
	 * affected groups until the conflict is resolved (by altering
	 * membership or perhaps by designating a resolution).
	 * 
	 * @param assignmentorstate
	 * @param specify_groups
	 * @param ingroups
	 * @param populate_ids
	 * @param users
	 * @return
	 */
	public Collection<String> usersInMultipleGroups(
	        Object assignmentorstate, // an assignment object or state object to find data
	        boolean specify_groups,   // don't use all site groups
	        String[] ingroups,        // limit to looking at specific groups
	        boolean populate_ids,     // return collection of user ids instead of message strings
	        Collection<String> users  // optional list of users to check instead of ALL site users
	        ) {
	    List retVal = new ArrayList();

	    try {
	        Site s = null;
	        Collection<String> _assignmentGroups = new ArrayList<String>();
	        for (int i=0; ingroups != null && i < ingroups.length; i++) {
	            _assignmentGroups.add(ingroups[i]);
	        }
	        if (assignmentorstate instanceof SessionState) {
	            s = SiteService.getSite((String)((SessionState)assignmentorstate).getAttribute(STATE_CONTEXT_STRING));
	        } else {
	            Assignment _a = (Assignment)assignmentorstate;
	            s = SiteService.getSite(_a.getContext());
	            if (_a.getAccess().equals(Assignment.AssignmentAccess.SITE)) {
	                specify_groups = false;
	            } else {
	                _assignmentGroups = _a.getGroups();
	                specify_groups = true;
	            }
	        }

	        Iterator<String> _it = users == null ? s.getUsers().iterator(): users.iterator();
	        while (_it.hasNext()) {
	            String _userRef = _it.next();
	            Collection<Group> _userGroups = s.getGroupsWithMember(_userRef);
	            int _count = 0;
	            StringBuilder _sb = new StringBuilder();
	            Iterator<Group> _checkGroups = _userGroups.iterator();
	            while (_checkGroups.hasNext()) {
	                Group _checkGroup = _checkGroups.next();
	                // exclude Sections from eligible groups
	                //if (_checkGroup.getProperties().get(GROUP_SECTION_PROPERTY) == null) {
	                if (!specify_groups) {
	                    _count++;
	                    if (_count > 1) {
	                        _sb.append(", ");
	                    }
	                    _sb.append(_checkGroup.getTitle());
	                } else {
	                    if (_assignmentGroups != null) {  
	                        Iterator<String> _assgnRefs = _assignmentGroups.iterator();
	                        while (_assgnRefs.hasNext()) {
	                            String _ref = _assgnRefs.next();
	                            Group _group = s.getGroup(_ref);
	                            if (_group != null && _group.getId().equals(_checkGroup.getId())) {
	                                _count++;
	                                if (_count > 1) {
	                                    _sb.append(", ");
	                                }
	                                _sb.append(_checkGroup.getTitle());
	                            }
	                        }
	                    }
	                }
	                //}
	            }
	            if (_count > 1) {
	                try {
	                    User _the_user = UserDirectoryService.getUser(_userRef);
			   /*          
			    * SAK-23697 Allow user to be in multiple groups if
		            * no SECURE_ADD_ASSIGNMENT_SUBMISSION permission or 
			    * if user has both SECURE_ADD_ASSIGNMENT_SUBMISSION
			    * and SECURE_GRADE_ASSIGNMENT_SUBMISSION permission (TAs and Instructors)
		            */
			    if (m_securityService.unlock(_the_user,AssignmentService.SECURE_ADD_ASSIGNMENT_SUBMISSION,s.getReference()) && !m_securityService.unlock(_the_user,AssignmentService.SECURE_GRADE_ASSIGNMENT_SUBMISSION,s.getReference())) {
	                      retVal.add(populate_ids ? _the_user.getId(): _the_user.getDisplayName() + " (" + _sb.toString() + ")");
			    };
	                } catch (UserNotDefinedException _unde) {
	                    retVal.add("UNKNOWN USER (" + _sb.toString() + ")");
	                }
	            }
	        }
	    } catch (IdUnusedException _te) {
	        throw new IllegalStateException("Could not find the site for assignment/state "+assignmentorstate+": "+_te, _te);
	    }
	    return retVal;
	}

	public Collection<String> usersInMultipleGroups(Assignment _a, boolean populate_ids) {
	    return usersInMultipleGroups(_a,false,null,populate_ids,null);
	}

	public Collection<String> usersInMultipleGroups(Assignment _a) {
	    return usersInMultipleGroups(_a,false,null,false,null);
	}

	public Collection<String> checkForUsersInMultipleGroups(
	        Assignment a,
	        Collection<String> ids,
	        SessionState state,
	        String base_message) {
	    Collection<String> _dupUsers = usersInMultipleGroups(a,false,null,false,ids);
	    if (_dupUsers.size() > 0) {
	        StringBuilder _sb = new StringBuilder(base_message + " ");
	        Iterator<String> _it = _dupUsers.iterator();
	        if (_it.hasNext()) {
	            _sb.append(_it.next());
	        }
	        while (_it.hasNext()) {
	            _sb.append(", " + _it.next());
	        }
	        addAlert(state, _sb.toString());
	    }
	    return _dupUsers;
	}

	public Collection<String> checkForGroupsInMultipleGroups(
	        Assignment a,
	        Collection<Group> groups,
	        SessionState state,
	        String base_message) {
	    Collection<String> retVal = new ArrayList<String>();
	    if (groups != null && groups.size() > 0) {
	        ArrayList<String> check_users = new ArrayList<String>();
	        Iterator<Group> it_groups = groups.iterator();
	        while (it_groups.hasNext()) {
	            Group _group = it_groups.next();
	            Iterator<String> it_strings = _group.getUsers().iterator();
	            while (it_strings.hasNext()) {
	                String _id = it_strings.next();
	                if (!check_users.contains(_id)) {
	                    check_users.add(_id);
	                }
	            }
	        }
	        retVal = checkForUsersInMultipleGroups(a, check_users, state, rb.getString("group.user.multiple.warning"));
	    }
	    return retVal;
	}
	
	public void doDownload_all(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_GRADE_ASSIGNMENT);
		ParameterParser params = data.getParameters();
		String downloadUrl = params.getString("downloadUrl");
		state.setAttribute(STATE_DOWNLOAD_URL, downloadUrl);
	}
	
	public void doUpload_all(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();
		// see if the user uploaded a file
		FileItem fileFromUpload = null;
		String fileName = null;
		fileFromUpload = params.getFileItem("file");
		String max_file_size_mb = ServerConfigurationService.getString("content.upload.max", "1");
		
		if(fileFromUpload == null)
		{
			// "The user submitted a file to upload but it was too big!"
			addAlert(state, rb.getFormattedMessage("size.exceeded", new Object[]{max_file_size_mb}));
		}
		else 
		{	
			String fname = StringUtils.lowerCase(fileFromUpload.getFileName());
			
			if (!StringUtils.endsWithAny(fname, new String[] {".zip", ".sit"}))
			{
				// no file
				addAlert(state, rb.getString("uploadall.alert.zipFile"));
			}
			else
			{
				String contextString = ToolManager.getCurrentPlacement().getContext();
				String toolTitle = ToolManager.getTool(ASSIGNMENT_TOOL_ID).getTitle();
				String aReference = (String) state.getAttribute(EXPORT_ASSIGNMENT_REF);
				String associateGradebookAssignment = null;
				
				List<String> choices = params.getStrings("choices") != null?new ArrayList(Arrays.asList(params.getStrings("choices"))):null;
				
				if (choices == null || choices.size() == 0)
				{
					// has to choose one upload feature
					addAlert(state, rb.getString("uploadall.alert.choose.element"));
					state.removeAttribute(UPLOAD_ALL_HAS_SUBMISSION_TEXT);
					state.removeAttribute(UPLOAD_ALL_HAS_SUBMISSION_ATTACHMENT);
					state.removeAttribute(UPLOAD_ALL_HAS_GRADEFILE);
					state.removeAttribute(UPLOAD_ALL_GRADEFILE_FORMAT);
					state.removeAttribute(UPLOAD_ALL_HAS_COMMENTS);
					state.removeAttribute(UPLOAD_ALL_HAS_FEEDBACK_TEXT);
					state.removeAttribute(UPLOAD_ALL_HAS_FEEDBACK_ATTACHMENT);
					state.removeAttribute(UPLOAD_ALL_WITHOUT_FOLDERS);
					state.removeAttribute(UPLOAD_ALL_RELEASE_GRADES);
				}
				else
				{
					// should contain student submission text information
					boolean hasSubmissionText = uploadAll_readChoice(choices, "studentSubmissionText");
					// should contain student submission attachment information
					boolean hasSubmissionAttachment = uploadAll_readChoice(choices, "studentSubmissionAttachment");
					// should contain grade file
					boolean hasGradeFile = uploadAll_readChoice(choices, "gradeFile");	
					String  gradeFileFormat = params.getString("gradeFileFormat");
					if ("excel".equals(gradeFileFormat)) {gradeFileFormat="excel";} else {gradeFileFormat="csv";}; 
					// inline text
					boolean hasFeedbackText = uploadAll_readChoice(choices, "feedbackTexts");
					// comments.txt should be available
					boolean hasComment = uploadAll_readChoice(choices, "feedbackComments");
					// feedback attachment
					boolean hasFeedbackAttachment = uploadAll_readChoice(choices, "feedbackAttachments");
					// folders
					//boolean withoutFolders = params.getString("withoutFolders") != null ? params.getBoolean("withoutFolders") : false;
					boolean withoutFolders = uploadAll_readChoice(choices, "withoutFolders"); // SAK-19147
					// release
					boolean	releaseGrades = params.getString("release") != null ? params.getBoolean("release") : false;
					
					state.setAttribute(UPLOAD_ALL_HAS_SUBMISSION_TEXT, Boolean.valueOf(hasSubmissionText));
					state.setAttribute(UPLOAD_ALL_HAS_SUBMISSION_ATTACHMENT, Boolean.valueOf(hasSubmissionAttachment));
					state.setAttribute(UPLOAD_ALL_HAS_GRADEFILE, Boolean.valueOf(hasGradeFile));
					state.setAttribute(UPLOAD_ALL_GRADEFILE_FORMAT, gradeFileFormat);
					state.setAttribute(UPLOAD_ALL_HAS_COMMENTS, Boolean.valueOf(hasComment));
					state.setAttribute(UPLOAD_ALL_HAS_FEEDBACK_TEXT, Boolean.valueOf(hasFeedbackText));
					state.setAttribute(UPLOAD_ALL_HAS_FEEDBACK_ATTACHMENT, Boolean.valueOf(hasFeedbackAttachment));
					state.setAttribute(UPLOAD_ALL_WITHOUT_FOLDERS, Boolean.valueOf(withoutFolders));
					state.setAttribute(UPLOAD_ALL_RELEASE_GRADES, Boolean.valueOf(releaseGrades));
					
					// SAK-17606
					HashMap anonymousSubmissionAndEidTable = new HashMap();
					
					// constructor the hashmap for all submission objects
					HashMap submissionTable = new HashMap();
					List submissions = null;
					Assignment assignment = getAssignment(aReference, "doUpload_all", state);
					if (assignment != null)
					{
						associateGradebookAssignment = StringUtils.trimToNull(assignment.getProperties().getProperty(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT));
						submissions =  AssignmentService.getSubmissions(assignment);
						if (submissions != null)
						{
							Iterator sIterator = submissions.iterator();
							while (sIterator.hasNext())
							{
							   try {
								AssignmentSubmission s = (AssignmentSubmission) sIterator.next();
                                                                String _eid = s.getSubmitterId();
								submissionTable.put(_eid, new UploadGradeWrapper(s.getGrade(), s.getSubmittedText(), s.getFeedbackComment(), hasSubmissionAttachment?new ArrayList():s.getSubmittedAttachments(), hasFeedbackAttachment?new ArrayList():s.getFeedbackAttachments(), (s.getSubmitted() && s.getTimeSubmitted() != null)?s.getTimeSubmitted().toString():"", s.getFeedbackText()));	

								// SAK-17606
								anonymousSubmissionAndEidTable.put(s.getAnonymousSubmissionId(), _eid);
							   } catch (Throwable _eidprob) {} 
									}
								}
							}
						
					InputStream fileContentStream = fileFromUpload.getInputStream();
					if(fileContentStream != null)
					{	
						submissionTable = uploadAll_parseZipFile(state,
								hasSubmissionText, hasSubmissionAttachment,
								hasGradeFile, hasFeedbackText, hasComment,
								hasFeedbackAttachment, submissionTable,
								assignment, fileContentStream,gradeFileFormat, anonymousSubmissionAndEidTable);
					}
			
					if (state.getAttribute(STATE_MESSAGE) == null)
					{
						// update related submissions
						uploadAll_updateSubmissions(state, aReference,
								associateGradebookAssignment,
								hasSubmissionText, hasSubmissionAttachment,
								hasGradeFile, hasFeedbackText, hasComment,
								hasFeedbackAttachment, releaseGrades,
								submissionTable, submissions, assignment);
					}
				}
			}
		}
		
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			// go back to the list of submissions view
			cleanUploadAllContext(state);
			state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_GRADE_ASSIGNMENT);
		}
	}

	private boolean uploadAll_readChoice(List<String> choices, String text) {
		return choices != null && text != null && choices.contains(text) ? true:false;
	}

	/**
	 * parse content inside uploaded zip file
	 * @param state
	 * @param hasSubmissionText
	 * @param hasSubmissionAttachment
	 * @param hasGradeFile
	 * @param hasFeedbackText
	 * @param hasComment
	 * @param hasFeedbackAttachment
	 * @param submissionTable
	 * @param assignment
	 * @param fileContentStream
	 * @return
	 */
	private HashMap uploadAll_parseZipFile(SessionState state,
			boolean hasSubmissionText, boolean hasSubmissionAttachment,
			boolean hasGradeFile, boolean hasFeedbackText, boolean hasComment,
			boolean hasFeedbackAttachment, HashMap submissionTable,
			Assignment assignment, InputStream fileContentStream,String gradeFileFormat, HashMap anonymousSubmissionAndEidTable) {
		// a flag value for checking whether the zip file is of proper format: 
		// should have a grades.csv file or grades.xls if there is no user folders
		boolean zipHasGradeFile = false;
		// and if have any folder structures, those folders should be named after at least one site user (zip file could contain user names who is no longer inside the site)
		boolean zipHasFolder = false;
		boolean zipHasFolderValidUserId = false;
		
		FileOutputStream tmpFileOut = null;
		File tempFile = null;

		// as stated from UI, we expected the zip file to have structure as follows
		//       assignment_name/user_eid/files
		// or assignment_name/grades.csv or assignment_name/grades.xls
		boolean validZipFormat = true;
		
		try
		{
			tempFile = File.createTempFile(String.valueOf(System.currentTimeMillis()),"");
			
			tmpFileOut = new FileOutputStream(tempFile);
			writeToStream(fileContentStream, tmpFileOut);
			tmpFileOut.flush();
			tmpFileOut.close();

			ZipFile zipFile = new ZipFile(tempFile, "UTF-8");
			Enumeration<ZipEntry> zipEntries = zipFile.getEntries();
			ZipEntry entry;
			while (zipEntries.hasMoreElements() && validZipFormat)
			{
				entry = zipEntries.nextElement();
				String entryName = entry.getName();
				if (!entry.isDirectory() && entryName.indexOf("/.") == -1)
				{
					// SAK-17606
					String anonTitle = rb.getString("grading.anonymous.title");

					if (entryName.endsWith("grades.csv") || entryName.endsWith("grades.xls"))
					{
						if (hasGradeFile && entryName.endsWith("grades.csv") && "csv".equals(gradeFileFormat))
						{
						// at least the zip file has a grade.csv
						zipHasGradeFile = true;
						
							// read grades.cvs from zip
							CSVReader reader = new CSVReader(new InputStreamReader(zipFile.getInputStream(entry)));

							List <String[]> lines = reader.readAll();

							if (lines != null )
							{
								for (int i = 3; i<lines.size(); i++)
								{
									String[] items = lines.get(i);
									if ((assignment.isGroup() && items.length > 3) || items.length > 4)
									{
										// has grade information
										try
										{
											String _the_eid = items[1];
											if (!assignment.isGroup()) {

												// SAK-17606
												User u = null;
												// check for anonymous grading
												if (!AssignmentService.getInstance().assignmentUsesAnonymousGrading(assignment)) {
													u = UserDirectoryService.getUserByEid(items[IDX_GRADES_CSV_EID]);
												} else { // anonymous so pull the real eid out of our hash table
													String anonId = items[IDX_GRADES_CSV_EID];
													String id = (String) anonymousSubmissionAndEidTable.get(anonId);
													u = UserDirectoryService.getUser(id);
												}

												if (u ==  null) throw new Exception("User not found!");
												_the_eid = u.getId();
											}

											UploadGradeWrapper w = (UploadGradeWrapper) submissionTable.get(_the_eid);
											if (w != null)
											{
												String itemString = assignment.isGroup() ? items[3]: items[4];
												int gradeType = assignment.getContent().getTypeOfGrade();
												if (gradeType == Assignment.SCORE_GRADE_TYPE)
												{
													validPointGrade(state, itemString, assignment.getContent().getFactor());
												} // SAK-24199 - Applied patch provided with a few additional modifications.
												else if (gradeType == Assignment.PASS_FAIL_GRADE_TYPE)
												{
													itemString = validatePassFailGradeValue(state, itemString);
												}
												else
												{
													validLetterGrade(state, itemString);
												}
												if (state.getAttribute(STATE_MESSAGE) == null)
												{
													w.setGrade(gradeType == Assignment.SCORE_GRADE_TYPE?scalePointGrade(state, itemString, assignment.getContent().getFactor()):itemString);
													submissionTable.put(_the_eid, w);
												}

											}

										}
										catch (Exception e )
										{
											M_log.warn(this + ":uploadAll_parseZipFile " + e.getMessage());
										}
									}
								}
							}
						} //end of csv grades import

						//Excel file import
						if (hasGradeFile && entryName.endsWith("grades.xls") && "excel".equals(gradeFileFormat)) {
							// at least the zip file has a grade.csv
							zipHasGradeFile = true;

							// read grades.xls from zip
							POIFSFileSystem fsFileSystem = new POIFSFileSystem(zipFile.getInputStream(entry));
							HSSFWorkbook workBook = new HSSFWorkbook(fsFileSystem);
							HSSFSheet hssfSheet = workBook.getSheetAt(0);
							//Iterate the rows
							Iterator rowIterator = hssfSheet.rowIterator();
							int count = 0;
							while (rowIterator.hasNext()) {
								HSSFRow hssfRow = (HSSFRow) rowIterator.next();
								//We skip first row (= header row)
								if (count > 0) {
									double gradeXls = -1;
									String itemString = null;
									// has grade information
									try {
										String _the_eid = hssfRow.getCell(1).getStringCellValue();
										if (!assignment.isGroup()) {
											if (!AssignmentService.getInstance().assignmentUsesAnonymousGrading(assignment))
											{
												User u = UserDirectoryService.getUserByEid(hssfRow.getCell(1).getStringCellValue()/*user eid*/);
												if (u == null) throw new Exception("User not found!");
												_the_eid = u.getId();
											}
											else
											{
												_the_eid = (String) anonymousSubmissionAndEidTable.get(_the_eid);
											}
										}
										UploadGradeWrapper w = (UploadGradeWrapper) submissionTable.get(_the_eid);
										if (w != null) {
											itemString = assignment.isGroup() ? hssfRow.getCell(3).toString() : hssfRow.getCell(4).toString();
											int gradeType = assignment.getContent().getTypeOfGrade();
											if (gradeType == Assignment.SCORE_GRADE_TYPE) {
												//Parse the string to double using the locale format
												try {
													itemString = assignment.isGroup() ? hssfRow.getCell(3).getStringCellValue() : hssfRow.getCell(4).getStringCellValue();
													if ((itemString != null) && (itemString.trim().length() > 0)) {
														NumberFormat nbFormat = FormattedText.getNumberFormat();
														gradeXls = nbFormat.parse(itemString).doubleValue();
													}
												} catch (Exception e) {
													try {
														gradeXls = assignment.isGroup() ? hssfRow.getCell(3).getNumericCellValue() : hssfRow.getCell(4).getNumericCellValue();
													} catch (Exception e2) {
														gradeXls = -1;
													}
												}
												if (gradeXls != -1) {
													// get localized number format
													NumberFormat nbFormat = FormattedText.getNumberFormat();
														itemString = nbFormat.format(gradeXls);
												} else {
													itemString = "";
												}

												validPointGrade(state, itemString, assignment.getContent().getFactor());
											} else if (gradeType == Assignment.PASS_FAIL_GRADE_TYPE) {
												itemString = validatePassFailGradeValue(state, itemString);
											} else {
												validLetterGrade(state, itemString);
											}
											if (state.getAttribute(STATE_MESSAGE) == null) {
												w.setGrade(gradeType == Assignment.SCORE_GRADE_TYPE ? scalePointGrade(state, itemString, assignment.getContent().getFactor()) : itemString);
												submissionTable.put(_the_eid, w);
											}
										}
									} catch (Exception e) {
										M_log.warn(this + ":uploadAll_parseZipFile " + e.getMessage());
									}
								}
								count++;
							}
						} //end of Excel grades import

					} else {
						String[] pathParts = entryName.split("/");
						if (pathParts.length <=2)
						{
							validZipFormat=false;
						}	
						else 
						{
							// get user eid part
							String userEid = "";
							if (entryName.indexOf("/") != -1)
							{
								// there is folder structure inside zip
								if (!zipHasFolder) zipHasFolder = true;
								
								// remove the part of zip name
								userEid = entryName.substring(entryName.indexOf("/")+1);
								// get out the user name part
								if (userEid.indexOf("/") != -1)
								{
									userEid = userEid.substring(0, userEid.indexOf("/"));
								}
								// SAK-17606 - get the eid part
								if ((userEid.indexOf("(") != -1) && !userEid.contains(anonTitle))
								{
									userEid = userEid.substring(userEid.indexOf("(")+1, userEid.indexOf(")"));
								}
								if (userEid.contains(anonTitle)) { // anonymous grading so we have to figure out the eid
									//get eid out of this slick table we made earlier
									userEid = (String) anonymousSubmissionAndEidTable.get(userEid);
								}

								userEid=StringUtils.trimToNull(userEid);
								if (!assignment.isGroup()) {
									try {
										User u = UserDirectoryService.getUserByEid(userEid/*user eid*/);
										if (u != null) userEid = u.getId();
									} catch (Throwable _t) { }
								}
							}

							if (submissionTable.containsKey(userEid))
							{
								if (!zipHasFolderValidUserId) zipHasFolderValidUserId = true;
								if (hasComment && entryName.indexOf("comments") != -1)
								{
									// read the comments file
									String comment = getBodyTextFromZipHtml(zipFile.getInputStream(entry),true);
									if (comment != null)
									{
										UploadGradeWrapper r = (UploadGradeWrapper) submissionTable.get(userEid);
										r.setComment(comment);
										submissionTable.put(userEid, r);
									}
								}
								if (hasFeedbackText && entryName.indexOf("feedbackText") != -1)
								{
									// upload the feedback text
									String text = getBodyTextFromZipHtml(zipFile.getInputStream(entry),false);
									if (text != null)
									{
										UploadGradeWrapper r = (UploadGradeWrapper) submissionTable.get(userEid);
										r.setFeedbackText(text);
										submissionTable.put(userEid, r);
									}
								}
								if (hasSubmissionText && entryName.indexOf("_submissionText") != -1)
								{
									// upload the student submission text
									String text = getBodyTextFromZipHtml(zipFile.getInputStream(entry),false);
									if (text != null)
									{
										UploadGradeWrapper r = (UploadGradeWrapper) submissionTable.get(userEid);
										r.setText(text);
										submissionTable.put(userEid, r);
									}
								}
								if (hasSubmissionAttachment)
								{
									// upload the submission attachment
									String submissionFolder = "/" + rb.getString("stuviewsubm.submissatt") + "/";
									if ( entryName.indexOf(submissionFolder) != -1)
									{
										// clear the submission attachment first
										UploadGradeWrapper r = (UploadGradeWrapper) submissionTable.get(userEid);
										submissionTable.put(userEid, r);
										submissionTable = uploadZipAttachments(state, submissionTable, zipFile.getInputStream(entry), entry, entryName, userEid, "submission");
									}
								}
								if (hasFeedbackAttachment)
								{
									// upload the feedback attachment
									String submissionFolder = "/" + rb.getString("download.feedback.attachment") + "/";
									if ( entryName.indexOf(submissionFolder) != -1)
									{
										// clear the feedback attachment first
										UploadGradeWrapper r = (UploadGradeWrapper) submissionTable.get(userEid);
										submissionTable.put(userEid, r);
										submissionTable = uploadZipAttachments(state, submissionTable, zipFile.getInputStream(entry), entry, entryName, userEid, "feedback");
									}
								}
								
								// if this is a timestamp file
								if (entryName.indexOf("timestamp") != -1)
								{
									byte[] timeStamp = readIntoBytes(zipFile.getInputStream(entry), entryName, entry.getSize());
									UploadGradeWrapper r = (UploadGradeWrapper) submissionTable.get(userEid);
									r.setSubmissionTimestamp(new String(timeStamp));
									submissionTable.put(userEid, r);
								}
							}
						}
					}
				}
			}
		}
		catch (IOException e) 
		{
			// uploaded file is not a valid archive
			addAlert(state, rb.getString("uploadall.alert.zipFile"));
			M_log.warn(this + ":uploadAll_parseZipFile " + e.getMessage());
		}
		finally
		{
			if (tmpFileOut != null) {
				try {
					tmpFileOut.close();
				} catch (IOException e) {
					M_log.warn(this + ":uploadAll_parseZipFile: Error closing temp file output stream: " + e.toString());
				}
			}
			
			if (fileContentStream != null) {
				try {
					fileContentStream.close();
				} catch (IOException e) {
					M_log.warn(this + ":uploadAll_parseZipFile: Error closing file upload stream: " + e.toString());
				}
			}
			
			//clean up the zip file
			if (tempFile != null && tempFile.exists()) {
				if (!tempFile.delete()) {
					M_log.warn("Failed to clean up temp file");
				}
			}
			
		}
		
		//This is used so that the "Zip Error" message is only printed once

		boolean zipError = false;

		// generate error when there is no grade file and no folder structure
		if (!zipHasGradeFile && !zipHasFolder) {
			addAlert(state, rb.getString("uploadall.alert.incorrectFormat"));
			addAlert(state, rb.getString("uploadall.alert.noGradeFile"));
			zipError = true;
		}
		// generate error when there is folder structure but not matching one user id
		if(zipHasFolder && !zipHasFolderValidUserId) {
			if (zipError == false)
				addAlert(state, rb.getString("uploadall.alert.incorrectFormat"));
			addAlert(state, rb.getString("uploadall.alert.invalidUserId"));
			zipError = true;
		}
		// should have right structure of zip file
		if (!validZipFormat) {
			if (zipError == false)
				addAlert(state, rb.getString("uploadall.alert.incorrectFormat"));

			// alert if the zip is of wrong format
			addAlert(state, rb.getString("uploadall.alert.wrongZipFormat"));
			zipError = true;

		}
		return submissionTable;
	}
	
	/**
	 * Update all submission objects based on uploaded zip file
	 * @param state
	 * @param aReference
	 * @param associateGradebookAssignment
	 * @param hasSubmissionText
	 * @param hasSubmissionAttachment
	 * @param hasGradeFile
	 * @param hasFeedbackText
	 * @param hasComment
	 * @param hasFeedbackAttachment
	 * @param releaseGrades
	 * @param submissionTable
	 * @param submissions
	 * @param assignment
	 */
	private void uploadAll_updateSubmissions(SessionState state,
			String aReference, String associateGradebookAssignment,
			boolean hasSubmissionText, boolean hasSubmissionAttachment,
			boolean hasGradeFile, boolean hasFeedbackText, boolean hasComment,
			boolean hasFeedbackAttachment, boolean releaseGrades,
			HashMap submissionTable, List submissions, Assignment assignment) {
		if (assignment != null && submissions != null)
		{
			Iterator sIterator = submissions.iterator();
			while (sIterator.hasNext())
			{
				AssignmentSubmission s = (AssignmentSubmission) sIterator.next();
					if (submissionTable.containsKey(s.getSubmitterId()))
				{
						// update the AssignmetnSubmission record
						AssignmentSubmissionEdit sEdit = editSubmission(s.getReference(), "doUpload_all", state);
						if (sEdit != null)	
						{
							UploadGradeWrapper w = (UploadGradeWrapper) submissionTable.get(s.getSubmitterId());
							
							// the submission text
							if (hasSubmissionText)
							{
								sEdit.setSubmittedText(w.getText());
							}
							
							// the feedback text
							if (hasFeedbackText)
							{
								sEdit.setFeedbackText(w.getFeedbackText());
							}
							
							// the submission attachment
							if (hasSubmissionAttachment)
							{
								// update the submission attachments with newly added ones from zip file
								List submittedAttachments = sEdit.getSubmittedAttachments();
								for (Iterator attachments = w.getSubmissionAttachments().iterator(); attachments.hasNext();)
								{
									Reference a = (Reference) attachments.next();
									if (!submittedAttachments.contains(a))
									{
										sEdit.addSubmittedAttachment(a);
									}
								}
							}
							
							// the feedback attachment
							if (hasFeedbackAttachment)
							{
								List feedbackAttachments = sEdit.getFeedbackAttachments();
								for (Iterator attachments = w.getFeedbackAttachments().iterator(); attachments.hasNext();)
								{
									// update the feedback attachments with newly added ones from zip file
									Reference a = (Reference) attachments.next();
									if (!feedbackAttachments.contains(a))
									{
										sEdit.addFeedbackAttachment(a);
									}
								}
							}
							
							// the feedback comment
							if (hasComment)
							{
								sEdit.setFeedbackComment(w.getComment());
							}
							
							// the grade file
							if (hasGradeFile)
							{
								// set grade
								String grade = StringUtils.trimToNull(w.getGrade());
								sEdit.setGrade(grade);
								if (grade != null && !grade.equals(rb.getString("gen.nograd")) && !"ungraded".equals(grade)){
									sEdit.setGraded(true);
									sEdit.setGradedBy(UserDirectoryService.getCurrentUser() == null ? null : UserDirectoryService.getCurrentUser().getId());
								}
							}
							
							// release or not - If it's graded or if there were feedback comments provided
							if (sEdit.getGraded() || (sEdit.getFeedbackComment() != null && !"".equals(sEdit.getFeedbackComment())))
							{
								sEdit.setGradeReleased(releaseGrades);
								sEdit.setReturned(releaseGrades);
							}
							else
							{
								sEdit.setGradeReleased(false);
								sEdit.setReturned(false);
							}
							
							if (releaseGrades && sEdit.getGraded())
							{
								sEdit.setTimeReturned(TimeService.newTime());
							}
							
							// if the current submission lacks timestamp while the timestamp exists inside the zip file
							if (StringUtils.trimToNull(w.getSubmissionTimeStamp()) != null && sEdit.getTimeSubmitted() == null)
							{
								sEdit.setTimeSubmitted(TimeService.newTimeGmt(w.getSubmissionTimeStamp()));
								sEdit.setSubmitted(true);
							}
							
							// for further information
							boolean graded = sEdit.getGraded();
							String sReference = sEdit.getReference();
							
							// commit
							AssignmentService.commitEdit(sEdit);
							
							if (releaseGrades && graded)
							{
								// update grade in gradebook
								if (associateGradebookAssignment != null)
								{
									integrateGradebook(state, aReference, associateGradebookAssignment, null, null, null, -1, null, sReference, "update", -1);
								}
							}
							
						}
					}
				}	
			}
		}


	/**
	 * This is to get the submission or feedback attachment from the upload zip file into the submission object
	 * @param state
	 * @param submissionTable
	 * @param zin
	 * @param entry
	 * @param entryName
	 * @param userEid
	 * @param submissionOrFeedback
	 */
	private HashMap uploadZipAttachments(SessionState state, HashMap submissionTable, InputStream zin, ZipEntry entry, String entryName, String userEid, String submissionOrFeedback) {
		// upload all the files as instructor attachments to the submission for grading purpose
		String fName = entryName.substring(entryName.lastIndexOf("/") + 1, entryName.length());
		ContentTypeImageService iService = (ContentTypeImageService) state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE);
		try
		{
				// get file extension for detecting content type
				// ignore those hidden files
				String extension = "";
				if(!fName.contains(".") || (fName.contains(".") && fName.indexOf(".") != 0))
				{
					// add the file as attachment
					ResourceProperties properties = m_contentHostingService.newResourceProperties();
					properties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, fName);
					
					String[] parts = fName.split("\\.");
					if(parts.length > 1)
					{
						extension = parts[parts.length - 1];
					}

					try {
						String contentType = ((ContentTypeImageService) state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE)).getContentType(extension);
						ContentResourceEdit attachment = m_contentHostingService.addAttachmentResource(fName);
						attachment.setContent(zin);
						attachment.setContentType(contentType);
						attachment.getPropertiesEdit().addAll(properties);
						m_contentHostingService.commitResource(attachment);
						
			    		UploadGradeWrapper r = (UploadGradeWrapper) submissionTable.get(userEid);
			    		List attachments = "submission".equals(submissionOrFeedback)?r.getSubmissionAttachments():r.getFeedbackAttachments();
			    		attachments.add(EntityManager.newReference(attachment.getReference()));
			    		if ("submission".equals(submissionOrFeedback))
			    		{
			    			r.setSubmissionAttachments(attachments);
			    		}
			    		else
			    		{
			    			r.setFeedbackAttachments(attachments);
			    		}
			    		submissionTable.put(userEid, r);
					}
					catch (Exception e)
					{
						M_log.warn(this + ":doUploadZipAttachments problem commit resource " + e.getMessage());
					}
				}
		}
		catch (Exception ee)
		{
			M_log.warn(this + ":doUploadZipAttachments " + ee.getMessage());
		}
		
		return submissionTable;
	}

	private String getBodyTextFromZipHtml(InputStream zin, boolean convertNewLines)
	{
		String rv = "";
		try
		{
			rv = StringUtils.trimToNull(readIntoString(zin));
		}
		catch (IOException e)
		{
			M_log.warn(this + ":getBodyTextFromZipHtml " + e.getMessage());
		}
		if (rv != null)
		{
			//SAK-28045 - Pre-process newlines
			if (convertNewLines == true) {
				rv=rv.replaceAll("\\r\\n|\\r|\\n", "<br>");
			}
			//Escape the html from malicious tags.
			rv = FormattedText.processEscapedHtml(rv);
			
			int start = rv.indexOf("<body>");
			int end = rv.indexOf("</body>");
			if (start != -1 && end != -1)
			{
				// get the text in between
				rv = rv.substring(start+6, end);
			}
		}
		return rv;
	}

	private byte[] readIntoBytes(InputStream zin, String fName, long length) throws IOException {
			
			byte[] buffer = new byte[4096];
			
			File f = File.createTempFile("asgnup", "tmp");
			
			FileOutputStream fout = new FileOutputStream(f);
			try {
    			int len;
    			while ((len = zin.read(buffer)) > 0)
    			{
    				fout.write(buffer, 0, len);
    			}
    			zin.close();
			} finally {
				try
				{
					fout.close(); // The file channel needs to be closed before the deletion.
				}
				catch (IOException ioException)
				{
					M_log.warn(this + "readIntoBytes: problem closing FileOutputStream " + ioException.getMessage());
				}
			}
			
			FileInputStream fis = new FileInputStream(f);
			FileChannel fc = fis.getChannel();
			byte[] data = null;
			try {
    			data = new byte[(int)(fc.size())];   // fc.size returns the size of the file which backs the channel
    			ByteBuffer bb = ByteBuffer.wrap(data);
    			fc.read(bb);
			} finally {
				try
				{
					fc.close(); // The file channel needs to be closed before the deletion.
				}
				catch (IOException ioException)
				{
					M_log.warn(this + "readIntoBytes: problem closing FileChannel " + ioException.getMessage());
				}
				
				try
				{
					fis.close(); // The file inputstream needs to be closed before the deletion.
				}
				catch (IOException ioException)
				{
					M_log.warn(this + "readIntoBytes: problem closing FileInputStream " + ioException.getMessage());
				}
			}
			
            //remove the file
			f.delete();
			
			return data;
	}
	
	private String readIntoString(InputStream zin) throws IOException 
	{
		StringBuilder buffer = new StringBuilder();
		int size = 2048;
		byte[] data = new byte[2048];
		while (true)
		{
			try
			{
				size = zin.read(data, 0, data.length);
				if (size > 0)
				{
					buffer.append(new String(data, 0, size));
				}
				else
				{
					break;
				}
			}
			catch (IOException e)
			{
				M_log.warn(this + ":readIntoString " + e.getMessage());
			}
         }
		return buffer.toString();
	}
	/**
	 * 
	 * @return
	 */
	public void doCancel_download_upload_all(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_GRADE_ASSIGNMENT);
		cleanUploadAllContext(state);
	}
	
	/**
	 * clean the state variabled used by upload all process
	 */
	private void cleanUploadAllContext(SessionState state)
	{
		state.removeAttribute(UPLOAD_ALL_HAS_SUBMISSION_TEXT);
		state.removeAttribute(UPLOAD_ALL_HAS_SUBMISSION_ATTACHMENT);
		state.removeAttribute(UPLOAD_ALL_HAS_FEEDBACK_ATTACHMENT);
		state.removeAttribute(UPLOAD_ALL_HAS_FEEDBACK_TEXT);
		state.removeAttribute(UPLOAD_ALL_HAS_GRADEFILE);
		state.removeAttribute(UPLOAD_ALL_GRADEFILE_FORMAT);
		state.removeAttribute(UPLOAD_ALL_HAS_COMMENTS);
		state.removeAttribute(UPLOAD_ALL_WITHOUT_FOLDERS);
		state.removeAttribute(UPLOAD_ALL_RELEASE_GRADES);
		
	}
	
	/**
	 * Action is to preparing to go to the download all file
	 */
	public void doPrep_download_all(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();
		String view = params.getString("view");
		state.setAttribute(VIEW_SUBMISSION_LIST_OPTION, view);
		state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_DOWNLOAD_ALL);

	} // doPrep_download_all
	
	/**
	 * Action is to preparing to go to the upload files
	 */
	public void doPrep_upload_all(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();
		state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_UPLOAD_ALL);

	} // doPrep_upload_all
	
	/**
	 * the UploadGradeWrapper class to be used for the "upload all" feature
	 */
	public class UploadGradeWrapper
	{
		/**
		 * the grade 
		 */
		String m_grade = null;
		
		/**
		 * the text
		 */
		String m_text = null;
		
		/**
		 * the submission attachment list
		 */
		List m_submissionAttachments = EntityManager.newReferenceList();
		
		/**
		 * the comment
		 */
		String m_comment = "";
		
		/**
		 * the timestamp
		 */
		String m_timeStamp="";
		
		/**
		 * the feedback text
		 */
		String m_feedbackText="";
		
		/**
		 * the feedback attachment list
		 */
		List m_feedbackAttachments = EntityManager.newReferenceList();

		public UploadGradeWrapper(String grade, String text, String comment, List submissionAttachments, List feedbackAttachments, String timeStamp, String feedbackText)
		{
			m_grade = grade;
			m_text = text;
			m_comment = comment;
			m_submissionAttachments = submissionAttachments;
			m_feedbackAttachments = feedbackAttachments;
			m_feedbackText = feedbackText;
			m_timeStamp = timeStamp;
		}

		/**
		 * Returns grade string
		 */
		public String getGrade()
		{
			return m_grade;
		}
		
		/**
		 * Returns the text
		 */
		public String getText()
		{
			return m_text;
		}

		/**
		 * Returns the comment string
		 */
		public String getComment()
		{
			return m_comment;
		}
		
		/**
		 * Returns the submission attachment list
		 */
		public List getSubmissionAttachments()
		{
			return m_submissionAttachments;
		}
		
		/**
		 * Returns the feedback attachment list
		 */
		public List getFeedbackAttachments()
		{
			return m_feedbackAttachments;
		}
		
		/**
		 * submission timestamp
		 * @return
		 */
		public String getSubmissionTimeStamp()
		{
			return m_timeStamp;
		}
		
		/**
		 * feedback text/incline comment
		 * @return
		 */
		public String getFeedbackText()
		{
			return m_feedbackText;
		}
		
		/**
		 * set the grade string
		 */
		public void setGrade(String grade)
		{
			m_grade = grade;
		}
		
		/**
		 * set the text
		 */
		public void setText(String text)
		{
			m_text = text;
		}
		
		/**
		 * set the comment string
		 */
		public void setComment(String comment)
		{
			m_comment = comment;
		}
		
		/**
		 * set the submission attachment list
		 */
		public void setSubmissionAttachments(List attachments)
		{
			m_submissionAttachments = attachments;
		}
		
		/**
		 * set the attachment list
		 */
		public void setFeedbackAttachments(List attachments)
		{
			m_feedbackAttachments = attachments;
		}
		
		/**
		 * set the submission timestamp
		 */
		public void setSubmissionTimestamp(String timeStamp)
		{
			m_timeStamp = timeStamp;
		}
		
		/**
		 * set the feedback text
		 */
		public void setFeedbackText(String feedbackText)
		{
			m_feedbackText = feedbackText;
		}
	}
	
	private List<DecoratedTaggingProvider> initDecoratedProviders() {
		TaggingManager taggingManager = (TaggingManager) ComponentManager
				.get("org.sakaiproject.taggable.api.TaggingManager");
		List<DecoratedTaggingProvider> providers = new ArrayList<DecoratedTaggingProvider>();
		for (TaggingProvider provider : taggingManager.getProviders())
		{
			providers.add(new DecoratedTaggingProvider(provider));
		}
		return providers;
	}
	
	private List<DecoratedTaggingProvider> addProviders(Context context, SessionState state)
	{
		String mode = (String) state.getAttribute(STATE_MODE);
		List<DecoratedTaggingProvider> providers = (List) state
				.getAttribute(mode + PROVIDER_LIST);
		if (providers == null)
		{
			providers = initDecoratedProviders();
			state.setAttribute(mode + PROVIDER_LIST, providers);
		}
		context.put("providers", providers);
		return providers;
	}
	
	private void addActivity(Context context, Assignment assignment)
	{
		AssignmentActivityProducer assignmentActivityProducer = (AssignmentActivityProducer) ComponentManager
				.get("org.sakaiproject.assignment.taggable.api.AssignmentActivityProducer");
		context.put("activity", assignmentActivityProducer
				.getActivity(assignment));
		
		String placement = ToolManager.getCurrentPlacement().getId();
		context.put("iframeId", Validator.escapeJavascript("Main" + placement));
	}
	
	private void addItem(Context context, AssignmentSubmission submission, String userId)
	{
		AssignmentActivityProducer assignmentActivityProducer = (AssignmentActivityProducer) ComponentManager
				.get("org.sakaiproject.assignment.taggable.api.AssignmentActivityProducer");
		context.put("item", assignmentActivityProducer
				.getItem(submission, userId));
	}
	
	private ContentReviewService contentReviewService;
	public String getReportURL(Long score) {
		getContentReviewService();
		return contentReviewService.getIconUrlforScore(score);
	}
	
	private void getContentReviewService() {
		if (contentReviewService == null)
		{
			contentReviewService = (ContentReviewService) ComponentManager.get(ContentReviewService.class.getName());
		}
	}
	
	/******************* model answer *********/
	/**
	 * add model answer input into state variables
	 */
	public void doModel_answer(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();
		
		String text = StringUtils.trimToNull(params.get("modelanswer_text"));
		if (text == null)
		{
			// no text entered for model answer
			addAlert(state, rb.getString("modelAnswer.show_to_student.alert.noText"));
		}
		
		int showTo = params.getInt("modelanswer_showto");
		if (showTo == 0)
		{
			// no show to criteria specifided for model answer
			addAlert(state, rb.getString("modelAnswer.show_to_student.alert.noShowTo"));
		}
		
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute(NEW_ASSIGNMENT_MODEL_ANSWER, Boolean.TRUE);
			state.setAttribute(NEW_ASSIGNMENT_MODEL_ANSWER_TEXT, text);
			state.setAttribute(NEW_ASSIGNMENT_MODEL_SHOW_TO_STUDENT, showTo);
			//state.setAttribute(NEW_ASSIGNMENT_MODEL_ANSWER_ATTACHMENT);
		}
	}
	
	private void assignment_resubmission_option_into_context(Context context, SessionState state)
	{
		context.put("name_allowResubmitNumber", AssignmentSubmission.ALLOW_RESUBMIT_NUMBER);

		String allowResubmitNumber = state.getAttribute(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER) != null ? (String) state.getAttribute(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER) : null;
		String allowResubmitTimeString = state.getAttribute(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME) != null ? (String) state.getAttribute(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME) : null;
			
		// the resubmit number
		if (allowResubmitNumber != null && !"0".equals(allowResubmitNumber))
		{
			context.put("value_allowResubmitNumber", Integer.valueOf(allowResubmitNumber));
			context.put("resubmitNumber", "-1".equals(allowResubmitNumber) ? rb.getString("allow.resubmit.number.unlimited"): allowResubmitNumber);
			
			// put allow resubmit time information into context
			putTimePropertiesInContext(context, state, "Resubmit", ALLOW_RESUBMIT_CLOSEMONTH, ALLOW_RESUBMIT_CLOSEDAY, ALLOW_RESUBMIT_CLOSEYEAR, ALLOW_RESUBMIT_CLOSEHOUR, ALLOW_RESUBMIT_CLOSEMIN);
			// resubmit close time
			Time resubmitCloseTime = null;
			if (allowResubmitTimeString != null)
			{
				resubmitCloseTime = TimeService.newTime(Long.parseLong(allowResubmitTimeString));
			}
			// put into context
			if (resubmitCloseTime != null)
			{
				context.put("resubmitCloseTime", resubmitCloseTime.toStringLocalFull());
			}
		}
		
		context.put("value_year_from", state.getAttribute(NEW_ASSIGNMENT_YEAR_RANGE_FROM));
		context.put("value_year_to", state.getAttribute(NEW_ASSIGNMENT_YEAR_RANGE_TO));
		
	}
	
	private void assignment_resubmission_option_into_state(Assignment a, AssignmentSubmission s, SessionState state)
	{

		String allowResubmitNumber = null;
		String allowResubmitTimeString = null;
		
		if (s != null)
		{
			// if submission is present, get the resubmission values from submission object first
			ResourceProperties sProperties = s.getProperties();
			allowResubmitNumber = sProperties.getProperty(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER);
			allowResubmitTimeString = sProperties.getProperty(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME);
		}
		else if (a != null)
		{
			// otherwise, if assignment is present, get the resubmission values from assignment object next
			ResourceProperties aProperties = a.getProperties();
			allowResubmitNumber = aProperties.getProperty(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER);
			allowResubmitTimeString = aProperties.getProperty(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME);
		}
		if (StringUtils.trimToNull(allowResubmitNumber) != null)
		{
			state.setAttribute(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER, allowResubmitNumber);
		}
		else
		{
			state.removeAttribute(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER);
		}
		
		if (allowResubmitTimeString == null)
		{
			// default setting
			allowResubmitTimeString = String.valueOf(a.getCloseTime().getTime());
		}
		
		Time allowResubmitTime = null;
		if (allowResubmitTimeString != null)
		{
			state.setAttribute(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME, allowResubmitTimeString);
			
			// get time object
			allowResubmitTime = TimeService.newTime(Long.parseLong(allowResubmitTimeString));
		}
		else
		{
			state.removeAttribute(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME);
		}
		
		if (allowResubmitTime != null)
		{
			// set up related state variables
			putTimePropertiesInState(state, allowResubmitTime, ALLOW_RESUBMIT_CLOSEMONTH, ALLOW_RESUBMIT_CLOSEDAY, ALLOW_RESUBMIT_CLOSEYEAR, ALLOW_RESUBMIT_CLOSEHOUR, ALLOW_RESUBMIT_CLOSEMIN);
		}
	}
	
	/**
	 * save the resubmit option for selected users
	 * @param data
	 */
	public void doSave_resubmission_option(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();
		
		// read in user input into state variable
		if (StringUtils.trimToNull(params.getString("allowResToggle")) != null)
		{
			if (params.getString(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER) != null)
			{
				// read in allowResubmit params 
				readAllowResubmitParams(params, state, null);
			}
		}
		else
		{
			resetAllowResubmitParams(state);
		}
		
		String[] userIds = params.getStrings("selectedAllowResubmit");
		
		if (userIds == null || userIds.length == 0)
		{
			addAlert(state, rb.getString("allowResubmission.nouser"));
		}
		else
		{
			for (int i = 0; i < userIds.length; i++)
			{
				String userId = userIds[i];
				try
				{
					String assignmentRef = (String) state.getAttribute(EXPORT_ASSIGNMENT_REF);
                                        Assignment _a = getAssignment(assignmentRef, " resubmit update ", state);
                                        AssignmentSubmission submission = null;
					if (_a.isGroup()) {
                                            submission = getSubmission(assignmentRef, userId, "doSave_resubmission_option", state);
                                        } else {
					User u = UserDirectoryService.getUser(userId);
                                            submission = getSubmission(assignmentRef, u, "doSave_resubmission_option", state);			    
                                        }
					if (submission != null)
					{
						AssignmentSubmissionEdit submissionEdit = editSubmission(submission.getReference(), "doSave_resubmission_option", state);
						
						if (submissionEdit != null)
						{
							// get resubmit number
							ResourcePropertiesEdit pEdit = submissionEdit.getPropertiesEdit();
							pEdit.addProperty(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER, (String) state.getAttribute(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER));
					
							if (state.getAttribute(ALLOW_RESUBMIT_CLOSEYEAR) != null)
							{
								// get resubmit time
								Time closeTime = getTimeFromState(state, ALLOW_RESUBMIT_CLOSEMONTH, ALLOW_RESUBMIT_CLOSEDAY, ALLOW_RESUBMIT_CLOSEYEAR, ALLOW_RESUBMIT_CLOSEHOUR, ALLOW_RESUBMIT_CLOSEMIN);
								pEdit.addProperty(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME, String.valueOf(closeTime.getTime()));
							}
							else
							{
								pEdit.removeProperty(AssignmentSubmission.ALLOW_RESUBMIT_CLOSETIME);
							}
							
							// save
							AssignmentService.commitEdit(submissionEdit);
						}
					}
				}
				catch (Exception userException)
				{
					M_log.warn(this + ":doSave_resubmission_option error getting user with id " + userId + " " + userException.getMessage());
				}
			}
		}

	}
	
	public void doSave_send_feedback(RunData data) {
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();

		String[] userIds = params.getStrings("selectedAllowResubmit");
		boolean checkForFormattingErrors = true;
		String comment = processFormattedTextFromBrowser(state, params.getCleanString("commentFeedback"),checkForFormattingErrors);
		String overwrite = params.getString("overWrite");
		String returnToStudents = params.getString("returnToStudents");

		if (userIds == null || userIds.length == 0) {
			addAlert(state, rb.getString("sendFeedback.nouser"));
		} else {
			if (comment.equals("")) {
				addAlert(state, rb.getString("sendFeedback.nocomment"));
			} else {
				int errorUsers = 0;
				for (int i = 0; i < userIds.length; i++) {
					String userId = userIds[i];
					try {
						User u = UserDirectoryService.getUser(userId);
						String assignmentRef = (String) state.getAttribute(EXPORT_ASSIGNMENT_REF);
						AssignmentSubmission submission = AssignmentService.getSubmission(assignmentRef, u);
						if (submission != null) {
							AssignmentSubmissionEdit submissionEdit = AssignmentService.editSubmission(submission.getReference());
							if (submissionEdit != null) {
								String newFeedbackComment = "";
								if (overwrite != null) {
									newFeedbackComment = comment + "<br/>";
									state.setAttribute(OW_FEEDBACK,	Boolean.TRUE);
								} else {
									newFeedbackComment = submissionEdit.getFeedbackComment() + comment + "<br/>";
								}
								submissionEdit.setFeedbackComment(newFeedbackComment);
								if (returnToStudents != null) {
									submissionEdit.setReturned(true);
									submissionEdit.setTimeReturned(TimeService.newTime());
									state.setAttribute(RETURNED_FEEDBACK, Boolean.TRUE);
								}
								AssignmentService.commitEdit(submissionEdit);
								state.setAttribute(SAVED_FEEDBACK, Boolean.TRUE);
							}
						}
					} catch (Exception userException) {
						M_log.warn(this	+ ":doSave_send_feedback error getting user with id " + userId + " " + userException.getMessage());
						errorUsers++;
					}
				}
				if (errorUsers>0) {
					addAlert(state, rb.getFormattedMessage("sendFeedback.error", new Object[]{errorUsers}));
				}
			}
		}

	}

	
	/**
	 * multiple file upload
	 * @param data
	 */
	public void doAttachUpload(RunData data)
	{
		// save the current input before leaving the page
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		saveSubmitInputs(state, data.getParameters ());
		doAttachUpload(data, false);
		if(MODE_STUDENT_REVIEW_EDIT.equals(state.getAttribute(STATE_MODE))) {
			saveReviewGradeForm(data, state, "save");
		}
	}
	
	/**
	 * single file upload
	 * @param data
	 */
	public void doAttachUploadSingle(RunData data)
	{
		doAttachUpload(data, true);
	}
	
	/**
	 * upload local file for attachment
	 * @param data
	 * @param singleFileUpload
	 */
	public void doAttachUpload(RunData data, boolean singleFileUpload)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		ParameterParser params = data.getParameters ();

		String max_file_size_mb = ServerConfigurationService.getString("content.upload.max", "1");

		String mode = (String) state.getAttribute(STATE_MODE);
		List attachments;

		if(MODE_STUDENT_REVIEW_EDIT.equals(mode)) {
			// construct the state variable for peer attachment list
			attachments = state.getAttribute(PEER_ATTACHMENTS) != null? (List) state.getAttribute(PEER_ATTACHMENTS) : EntityManager.newReferenceList();
		} else {

			// construct the state variable for attachment list
			attachments = state.getAttribute(ATTACHMENTS) != null? (List) state.getAttribute(ATTACHMENTS) : EntityManager.newReferenceList();
		}
		
		FileItem fileitem = null;
		try
		{
			fileitem = params.getFileItem("upload");
		}
		catch(Exception e)
		{
			// other exceptions should be caught earlier
			M_log.debug(this + ".doAttachupload ***** Unknown Exception ***** " + e.getMessage());
			addAlert(state, rb.getString("failed.upload"));
		}
		if(fileitem == null)
		{
			// "The user submitted a file to upload but it was too big!"
			addAlert(state, rb.getFormattedMessage("size.exceeded", new Object[]{ max_file_size_mb }));
			//addAlert(state, hrb.getString("size") + " " + max_file_size_mb + "MB " + hrb.getString("exceeded2"));
		}
		else if (singleFileUpload && (fileitem.getFileName() == null || fileitem.getFileName().length() == 0))
		{
			// only if in the single file upload case, need to warn user to upload a local file
			addAlert(state, rb.getString("choosefile7"));
		}
		else if (fileitem.getFileName().length() > 0)
		{
			String filename = Validator.getFileName(fileitem.getFileName());
			InputStream fileContentStream = fileitem.getInputStream();
			String contentType = fileitem.getContentType();

			InputStreamReader reader = new InputStreamReader(fileContentStream);
			
			try{
				
				//check the InputStreamReader to see if the file is 0kb aka empty
				if( reader.ready()==false )
				{
					addAlert(state, rb.getFormattedMessage("attempty", new Object[]{filename} ));
				}
				else if(fileContentStream != null)
				{
					// we just want the file name part - strip off any drive and path stuff
					String name = Validator.getFileName(filename);
					String resourceId = Validator.escapeResourceName(name);
	
					// make a set of properties to add for the new resource
					ResourcePropertiesEdit props = m_contentHostingService.newResourceProperties();
					props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
					props.addProperty(ResourceProperties.PROP_DESCRIPTION, filename);
	
					// make an attachment resource for this URL
					SecurityAdvisor sa = new SecurityAdvisor() {
						public SecurityAdvice isAllowed(String userId, String function, String reference)
						{
							//Needed to be able to add or modify their own
							if (function.equals(m_contentHostingService.AUTH_RESOURCE_ADD) || function.equals(m_contentHostingService.AUTH_RESOURCE_WRITE_OWN)) {
								return SecurityAdvice.ALLOWED;
							}
							return SecurityAdvice.PASS;
						}
					};
					try
					{
						String siteId = ToolManager.getCurrentPlacement().getContext();
						
						// add attachment
						// put in a security advisor so we can create citationAdmin site without need
						// of further permissions
						m_securityService.pushAdvisor(sa);
						ContentResource attachment = m_contentHostingService.addAttachmentResource(resourceId, siteId, "Assignments", contentType, fileContentStream, props);
						
						Site s = null;
						try
						{
							s = SiteService.getSite(siteId);
						}
						catch (IdUnusedException iue)
						{
							M_log.warn(this + ":doAttachUpload: Site not found!" + iue.getMessage());
						}

						// Check if the file is acceptable with the ContentReviewService
						boolean blockedByCRS = false;
						if (allowReviewService && contentReviewService != null && contentReviewService.isSiteAcceptable(s))
						{
							String assignmentReference = (String) state.getAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE);
							Assignment a = getAssignment(assignmentReference, "doAttachUpload", state);
							if (a.getContent().getAllowReviewService())
							{
								if (!contentReviewService.isAcceptableContent(attachment))
								{
									addAlert(state, rb.getFormattedMessage("review.file.not.accepted", new Object[]{contentReviewService.getServiceName(), getContentReviewAcceptedFileTypesMessage()}));
									blockedByCRS = true;
									// TODO: delete the file? Could we have done this check without creating it in the first place?
								}
							}
						}

						if (!blockedByCRS)
						{
							try
							{
								Reference ref = EntityManager.newReference(m_contentHostingService.getReference(attachment.getId()));
								if (singleFileUpload && attachments.size() > 1)
								{
									//SAK-26319	- the assignment type is 'single file upload' and the user has existing attachments, so they must be uploading a 'newSingleUploadedFile'	--bbailla2
									state.setAttribute("newSingleUploadedFile", ref);
								}
								else
								{
									attachments.add(ref);
								}
							}
							catch(Exception ee)
							{
								M_log.warn(this + "doAttachUpload cannot find reference for " + attachment.getId() + ee.getMessage());
							}
						}

						if(MODE_STUDENT_REVIEW_EDIT.equals(mode)) {
							state.setAttribute(PEER_ATTACHMENTS, attachments);
						} else {
							state.setAttribute(ATTACHMENTS, attachments);
						}
					}
					catch (PermissionException e)
					{
						addAlert(state, rb.getString("notpermis4"));
					}
					catch(RuntimeException e)
					{
						if(m_contentHostingService.ID_LENGTH_EXCEPTION.equals(e.getMessage()))
						{
							// couldn't we just truncate the resource-id instead of rejecting the upload?
							addAlert(state, rb.getFormattedMessage("alert.toolong", new String[]{name}));
						}
						else
						{
							M_log.debug(this + ".doAttachupload ***** Runtime Exception ***** " + e.getMessage());
							addAlert(state, rb.getString("failed"));
						}
					}
					catch (ServerOverloadException e)
					{
						// disk full or no writing permission to disk
						M_log.debug(this + ".doAttachupload ***** Disk IO Exception ***** " + e.getMessage());
						addAlert(state, rb.getString("failed.diskio"));
					}
					catch(Exception ignore)
					{
						// other exceptions should be caught earlier
						M_log.debug(this + ".doAttachupload ***** Unknown Exception ***** " + ignore.getMessage());
						addAlert(state, rb.getString("failed"));
					}
					finally
					{
						m_securityService.popAdvisor(sa);
					}
				}
				else
				{
					addAlert(state, rb.getString("choosefile7"));
				}
			}
			catch( IOException e){
				M_log.debug(this + ".doAttachupload ***** IOException ***** " + e.getMessage());
				addAlert(state, rb.getString("failed"));
			}
		}
	}	// doAttachupload
	
	
	/**
	 * Simply take as much as possible out of 'in', and write it to 'out'. Don't
	 * close the streams, just transfer the data.
	 * 
	 * @param in
	 * 		The data provider
	 * @param out
	 * 		The data output
	 * @throws IOException
	 * 		Thrown if there is an IOException transfering the data
	 */
	private void writeToStream(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[INPUT_BUFFER_SIZE];
		
		try {
			while (in.read(buffer) > 0) {
				out.write(buffer);
			}
		} catch (IOException e) {
			throw e;
		}
	}
    
    /**
     * Categories are represented as Integers. Right now this feature only will
     * be active for new assignments, so we'll just always return 0 for the 
     * unassigned category. In the future we may (or not) want to update this 
     * to return categories for existing gradebook items.
     * @param assignment
     * @return
     */
    private int getAssignmentCategoryAsInt(Assignment assignment) {
    	int categoryAsInt;
    	categoryAsInt = 0; // zero for unassigned
    	
    	return categoryAsInt;
    }
    
    /*
	 * (non-Javadoc) 
	 */
	public void doOptions(RunData data, Context context)
	{
		doOptions(data);
	} // doOptions
	
	protected void doOptions(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		String siteId = ToolManager.getCurrentPlacement().getContext();
		try {
			Site site = SiteService.getSite(siteId);
			ToolConfiguration tc=site.getToolForCommonId(ASSIGNMENT_TOOL_ID);
			String optionValue = tc.getPlacementConfig().getProperty(SUBMISSIONS_SEARCH_ONLY);
			state.setAttribute(SUBMISSIONS_SEARCH_ONLY, optionValue == null ? Boolean.FALSE:Boolean.valueOf(optionValue));
}	
		catch (IdUnusedException e)
		{
			M_log.warn(this + ":doOptions  Cannot find site with id " + siteId);
		}
		
		if (!alertGlobalNavigation(state, data))
		{
			if (SiteService.allowUpdateSite((String) state.getAttribute(STATE_CONTEXT_STRING)))
			{
				state.setAttribute(STATE_MODE, MODE_OPTIONS);
			}
			else
			{
				addAlert(state, rb.getString("youarenot_options"));
			}
			
			// reset the global navigaion alert flag
			if (state.getAttribute(ALERT_GLOBAL_NAVIGATION) != null)
			{
				state.removeAttribute(ALERT_GLOBAL_NAVIGATION);
			}
		}
	}
	
	/**
	 * build the options
	 */
	protected String build_options_context(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	{
		context.put("context", state.getAttribute(STATE_CONTEXT_STRING));

		context.put(SUBMISSIONS_SEARCH_ONLY, (Boolean) state.getAttribute(SUBMISSIONS_SEARCH_ONLY));
		
		String template = (String) getContext(data).get("template");
		return template + TEMPLATE_OPTIONS;

	} // build_options_context
	
    /**
     * save the option edits
     * @param data
     * @param context
     */
	public void doUpdate_options(RunData data)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		String siteId = ToolManager.getCurrentPlacement().getContext();
		ParameterParser params = data.getParameters();
		
		// only show those submissions matching search criteria
		boolean submissionsSearchOnly = params.getBoolean(SUBMISSIONS_SEARCH_ONLY);
		state.setAttribute(SUBMISSIONS_SEARCH_ONLY, Boolean.valueOf(submissionsSearchOnly));
		
		// save the option into tool configuration
		try {
			Site site = SiteService.getSite(siteId);
			ToolConfiguration tc=site.getToolForCommonId(ASSIGNMENT_TOOL_ID);
			String currentSetting = tc.getPlacementConfig().getProperty(SUBMISSIONS_SEARCH_ONLY);
			if (currentSetting == null || !currentSetting.equals(Boolean.toString(submissionsSearchOnly)))
			{
				// save the change
				tc.getPlacementConfig().setProperty(SUBMISSIONS_SEARCH_ONLY, Boolean.toString(submissionsSearchOnly));
				SiteService.save(site);
			}
		}
		catch (IdUnusedException e)
		{
			M_log.warn(this + ":doUpdate_options  Cannot find site with id " + siteId);
			addAlert(state, rb.getFormattedMessage("options_cannotFindSite", new Object[]{siteId}));
		}
		catch (PermissionException e)
		{
			M_log.warn(this + ":doUpdate_options Do not have permission to edit site with id " + siteId);
			addAlert(state, rb.getFormattedMessage("options_cannotEditSite", new Object[]{siteId}));
		}
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			// back to list view
			state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
		}
	} // doUpdate_options
	
	
    /**
     * cancel the option edits
     * @param data
     * @param context
     */
	public void doCancel_options(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
	} // doCancel_options
	
	/**
	 * handle submission options
	 */
	public void doSubmission_search_option(RunData data, Context context) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the search form field into the state object
		String searchOption = StringUtils.trimToNull(data.getParameters().getString("option"));

		// set the flag to go to the prev page on the next list
		if (searchOption != null && "submit".equals(searchOption)) {
			doSubmission_search(data, context);
		} else if (searchOption != null && "clear".equals(searchOption)) {
			doSubmission_search_clear(data, context);
		}

	} // doSubmission_search_option
	
	/**
	 * Handle the submission search request.
	 */
	public void doSubmission_search(RunData data, Context context) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the search form field into the state object
		String search = StringUtils.trimToNull(data.getParameters().getString(
				FORM_SEARCH));

		// set the flag to go to the prev page on the next list
		if (search == null) {
			state.removeAttribute(STATE_SEARCH);
		} else {
			state.setAttribute(STATE_SEARCH, search);
		}

	} // doSubmission_search

	/**
	 * Handle a Search Clear request.
	 */
	public void doSubmission_search_clear(RunData data, Context context) {
		SessionState state = ((JetspeedRunData) data)
				.getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// clear the search
		state.removeAttribute(STATE_SEARCH);

	} // doSubmission_search_clear
	
	protected void letterGradeOptionsIntoContext(Context context) {
		String lOptions = ServerConfigurationService.getString("assignment.letterGradeOptions", "A+,A,A-,B+,B,B-,C+,C,C-,D+,D,D-,E,F");
		context.put("letterGradeOptions", StringUtils.split(lOptions, ","));
	}

    private LRS_Statement getStatementForViewSubmittedAssignment(LRS_Actor actor, Event event, String assignmentName) {
        String url = ServerConfigurationService.getPortalUrl();
        LRS_Verb verb = new LRS_Verb(SAKAI_VERB.interacted);
        LRS_Object lrsObject = new LRS_Object(url + event.getResource(), "view-submitted-assignment");
        HashMap<String, String> nameMap = new HashMap<String, String>();
        nameMap.put("en-US", "User reviewed a submitted assignment");
        lrsObject.setActivityName(nameMap);
        // Add description
        HashMap<String, String> descMap = new HashMap<String, String>();
        descMap.put("en-US", "User reviewed a submitted assignment: " + assignmentName);
        lrsObject.setDescription(descMap);
        return new LRS_Statement(actor, verb, lrsObject);
    }

    private LRS_Statement getStatementForViewAssignment(LRS_Actor actor, Event event, String assignmentName) {
        String url = ServerConfigurationService.getPortalUrl();
        LRS_Verb verb = new LRS_Verb(SAKAI_VERB.interacted);
        LRS_Object lrsObject = new LRS_Object(url + event.getResource(), "view-assignment");
        HashMap<String, String> nameMap = new HashMap<String, String>();
        nameMap.put("en-US", "User viewed an assignment");
        lrsObject.setActivityName(nameMap);
        HashMap<String, String> descMap = new HashMap<String, String>();
        descMap.put("en-US", "User viewed assignment: " + assignmentName);
        lrsObject.setDescription(descMap);
        return new LRS_Statement(actor, verb, lrsObject);
    }

    private LRS_Statement getStatementForSubmitAssignment(LRS_Actor actor, Event event, String accessUrl, String assignmentName) {
        LRS_Verb verb = new LRS_Verb(SAKAI_VERB.attempted);
        LRS_Object lrsObject = new LRS_Object(accessUrl + event.getResource(), "submit-assignment");
        HashMap<String, String> nameMap = new HashMap<String, String>();
        nameMap.put("en-US", "User submitted an assignment");
        lrsObject.setActivityName(nameMap);
        // Add description
        HashMap<String, String> descMap = new HashMap<String, String>();
        descMap.put("en-US", "User submitted an assignment: " + assignmentName);
        lrsObject.setDescription(descMap);
        return new LRS_Statement(actor, verb, lrsObject);
    }
    /**
     * Validates the ungraded/pass/fail grade values provided in the upload file are valid.
     * Values must be present in the appropriate language property file.
     * @param state
     * @param itemString
     * @return one of the valid values or the original value entered by the user
     */
    private String validatePassFailGradeValue(SessionState state, String itemString)
    {
	    // -------- SAK-24199 (SAKU-274) by Shoji Kajita
		if (itemString.equalsIgnoreCase(rb.getString("pass"))) 
		{
			itemString = "Pass";
		} 
		else if (itemString.equalsIgnoreCase(rb.getString("fail"))) 
		{
			itemString = "Fail";
		} 
		else if (itemString.equalsIgnoreCase(rb.getString("ungra")) || itemString.isEmpty()) {
			itemString = "Ungraded";
		}
		else { // Not one of the expected values. Display error message.
			addAlert(state, rb.getFormattedMessage("plesuse0", new Object []{itemString}));
		}
		// --------
    	
    	return itemString;
    }
    
	/**
	 * Action to determine which view do present to user.
	 * This method is currently called from calendar events in the alternate calendar tool.
	 */
	public void doCheck_view(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();
		String assignmentId = params.getString("assignmentId");
		
		String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
		
		boolean allowReadAssignment = AssignmentService.allowGetAssignment(contextString); 
		boolean allowSubmitAssignment = AssignmentService.allowAddSubmission(contextString); 
		boolean allowAddAssignment = AssignmentService.allowAddAssignment(contextString);
		
		// Retrieve the status of the assignment
		String assignStatus = getAssignmentStatus(assignmentId, state);
		if (assignStatus != null && !assignStatus.equals(rb.getString("gen.open")) && !allowAddAssignment){
			addAlert(state, rb.getFormattedMessage("gen.notavail", new Object[]{assignStatus}));
		}
		// Check the permission and call the appropriate view method.
		if (allowAddAssignment){
			doView_assignment(data);
		}
		else if (allowSubmitAssignment){
			doView_submission(data);
		}
		else if (allowReadAssignment){
			doView_assignment_as_student(data);
		}
		else{
			addAlert(state, rb.getFormattedMessage("youarenot_viewAssignment", new Object[]{assignmentId}));
		}
	} // doCheck_view
    
	/**
	 * Retrieves the status of a given assignment.
	 * @param assignmentId
	 * @param state
	 * @return
	 */
	private String getAssignmentStatus(String assignmentId, SessionState state)
	{
		String rv = null;
		try
		{
			Session session = SessionManager.getCurrentSession();
			rv = AssignmentService.getAssignmentStatus(assignmentId);
		}
		catch (IdUnusedException e)
		{
			M_log.warn(this +  " " + e.getMessage() + " " + assignmentId);
		}
		catch (PermissionException e)
		{
			M_log.warn(this +  e.getMessage() + " " + assignmentId);
		}
		
		return rv;
	}

	/**
	 * Set properties related to grading via an external scoring service. This service may be enabled for the
	 * associated gradebook item.
	 */
	protected void setScoringAgentProperties(Context context, Assignment assignment, AssignmentSubmission submission, boolean gradeView) {
		String associatedGbItem = StringUtils.trimToNull(assignment.getProperties().getProperty(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT));
		if (submission != null && associatedGbItem != null && assignment.getContent().getTypeOfGrade() == 3) {
			ScoringService scoringService = (ScoringService)  ComponentManager.get("org.sakaiproject.scoringservice.api.ScoringService");
			ScoringAgent scoringAgent = scoringService.getDefaultScoringAgent();
			
			String gradebookUid = ToolManager.getInstance().getCurrentPlacement().getContext();
			boolean scoringAgentEnabled = scoringAgent != null && scoringAgent.isEnabled(gradebookUid, null);			
			String studentId = submission.getSubmitterId();
			
			if (scoringAgentEnabled) {
				String gbItemName;
				if (assignment.getReference().equals(associatedGbItem)) {
					// this gb item is controlled by this tool
					gbItemName = assignment.getTitle();
				} else {
					// this assignment was associated with an existing gb item
					gbItemName = associatedGbItem;
				}
				GradebookService gbService = (GradebookService)  ComponentManager.get("org.sakaiproject.service.gradebook.GradebookService");
				org.sakaiproject.service.gradebook.shared.Assignment gbItem = null;
				try {
					gbItem = gbService.getAssignment(gradebookUid, gbItemName);
				} catch (SecurityException se) {
					// the gradebook method above is overzealous about security when retrieving the gb item by name. It doesn't
					// allow student-role users to access the assignment via this method. So we
					// have to retrieve all viewable gb items and filter to get the one we want, unfortunately, if we hit an exception.
					// If gb item isn't released in the gb, scoring agent info will not be available.
					List<org.sakaiproject.service.gradebook.shared.Assignment> viewableGbItems = gbService.getViewableAssignmentsForCurrentUser(gradebookUid);
					if (viewableGbItems != null && !viewableGbItems.isEmpty()) {
						for (org.sakaiproject.service.gradebook.shared.Assignment viewableGbItem : viewableGbItems) {
							if(gbItemName.equals(viewableGbItem.getName())) {
								gbItem = viewableGbItem;
								break;
							}
						}
					}
				}
				
				if (gbItem != null) {
					String gbItemId = Long.toString(gbItem.getId());
					
					// Determine if a scoring component (like a rubric) has been associated with this gradebook item
					ScoringComponent component = scoringService.getScoringComponent(
							scoringAgent.getAgentId(), gradebookUid, gbItemId);
					boolean scoringComponentEnabled = component != null;
					
					context.put("scoringComponentEnabled", scoringComponentEnabled);
					
					if (scoringComponentEnabled) {
						context.put("scoringAgentImage", scoringAgent.getImageReference());
						context.put("scoringAgentName", scoringAgent.getName());
						
						// retrieve the appropriate url
						if (gradeView) {
							context.put("scoreUrl", scoringAgent.getScoreLaunchUrl(gradebookUid, gbItemId, studentId));
							context.put("refreshScoreUrl", scoringService.getDefaultScoringAgent().getScoreUrl(gradebookUid, gbItemId, studentId) + "&t=gb");
							context.put("scoreText",rb.getFormattedMessage("scoringAgent.grade", new Object[]{scoringAgent.getName()}));
						} else {
							// only retrieve the graded rubric if grade has been released. otherwise, keep it generic
							String scoreStudent = null;
							if (submission.getGradeReleased()) {
								scoreStudent = studentId;
							}
							context.put("scoreUrl", scoringAgent.getViewScoreLaunchUrl(gradebookUid, gbItemId, scoreStudent));
							context.put("scoreText",rb.getFormattedMessage("scoringAgent.view", new Object[]{scoringAgent.getName()}));
						}
					}
				}
			}
		}
	}

}	
