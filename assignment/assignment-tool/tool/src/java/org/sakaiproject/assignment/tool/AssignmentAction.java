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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.assignment.tool;

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
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.sakaiproject.announcement.api.AnnouncementChannel;
import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementMessageEdit;
import org.sakaiproject.announcement.api.AnnouncementMessageHeaderEdit;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentContent;
import org.sakaiproject.assignment.api.AssignmentContentEdit;
import org.sakaiproject.assignment.api.AssignmentEdit;
import org.sakaiproject.assignment.api.AssignmentSubmission;
import org.sakaiproject.assignment.api.AssignmentSubmissionEdit;
import org.sakaiproject.assignment.api.model.AssignmentAllPurposeItem;
import org.sakaiproject.assignment.api.model.AssignmentAllPurposeItemAccess;
import org.sakaiproject.assignment.api.model.AssignmentModelAnswerItem;
import org.sakaiproject.assignment.api.model.AssignmentNoteItem;
import org.sakaiproject.assignment.api.model.AssignmentSupplementItemAttachment;
import org.sakaiproject.assignment.api.model.AssignmentSupplementItemService;
import org.sakaiproject.assignment.api.model.AssignmentSupplementItemWithAttachment;
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
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.authz.cover.SecurityService;
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
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.id.cover.IdManager;
import org.sakaiproject.javax.PagingPosition;
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
	private static Log M_log = LogFactory.getLog(AssignmentAction.class);

	private static final String ASSIGNMENT_TOOL_ID = "sakai.assignment.grades";
	
	private static final Boolean allowReviewService = ServerConfigurationService.getBoolean("assignment.useContentReview", false);
	
	/** Is the review service available? */
	private static final String ALLOW_REVIEW_SERVICE = "allow_review_service";
	
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
	
	
	
	
	/** The attachments */
	private static final String ATTACHMENTS = "Assignment.attachments";
	private static final String ATTACHMENTS_FOR = "Assignment.attachments_for";
	

	/** The content type image lookup service in the State. */
	private static final String STATE_CONTENT_TYPE_IMAGE_SERVICE = "Assignment.content_type_image_service";

	/** The calendar service in the State. */
	private static final String STATE_CALENDAR_SERVICE = "Assignment.calendar_service";

	/** The announcement service in the State. */
	private static final String STATE_ANNOUNCEMENT_SERVICE = "Assignment.announcement_service";

	/** The calendar object */
	private static final String CALENDAR = "calendar";
	
	/** The calendar tool */
	private static final String CALENDAR_TOOL_EXIST = "calendar_tool_exisit";

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

	// open date
	private static final String NEW_ASSIGNMENT_OPENMONTH = "new_assignment_openmonth";

	private static final String NEW_ASSIGNMENT_OPENDAY = "new_assignment_openday";

	private static final String NEW_ASSIGNMENT_OPENYEAR = "new_assignment_openyear";

	private static final String NEW_ASSIGNMENT_OPENHOUR = "new_assignment_openhour";

	private static final String NEW_ASSIGNMENT_OPENMIN = "new_assignment_openmin";

	private static final String NEW_ASSIGNMENT_OPENAMPM = "new_assignment_openampm";

	// due date
	private static final String NEW_ASSIGNMENT_DUEMONTH = "new_assignment_duemonth";

	private static final String NEW_ASSIGNMENT_DUEDAY = "new_assignment_dueday";

	private static final String NEW_ASSIGNMENT_DUEYEAR = "new_assignment_dueyear";

	private static final String NEW_ASSIGNMENT_DUEHOUR = "new_assignment_duehour";

	private static final String NEW_ASSIGNMENT_DUEMIN = "new_assignment_duemin";

	private static final String NEW_ASSIGNMENT_DUEAMPM = "new_assignment_dueampm";
	
	private static final String NEW_ASSIGNMENT_PAST_DUE_DATE = "new_assignment_past_due_date";
	
	// close date
	private static final String NEW_ASSIGNMENT_ENABLECLOSEDATE = "new_assignment_enableclosedate";

	private static final String NEW_ASSIGNMENT_CLOSEMONTH = "new_assignment_closemonth";

	private static final String NEW_ASSIGNMENT_CLOSEDAY = "new_assignment_closeday";

	private static final String NEW_ASSIGNMENT_CLOSEYEAR = "new_assignment_closeyear";

	private static final String NEW_ASSIGNMENT_CLOSEHOUR = "new_assignment_closehour";

	private static final String NEW_ASSIGNMENT_CLOSEMIN = "new_assignment_closemin";

	private static final String NEW_ASSIGNMENT_CLOSEAMPM = "new_assignment_closeampm";

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

	private static final String NEW_ASSIGNMENT_FOCUS = "new_assignment_focus";

	private static final String NEW_ASSIGNMENT_DESCRIPTION_EMPTY = "new_assignment_description_empty";

	private static final String NEW_ASSIGNMENT_ADD_TO_GRADEBOOK = "new_assignment_add_to_gradebook";

	private static final String NEW_ASSIGNMENT_RANGE = "new_assignment_range";

	private static final String NEW_ASSIGNMENT_GROUPS = "new_assignment_groups";
	
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
	private static final String ALLOW_RESUBMIT_CLOSEAMPM = "allow_resubmit_closeAMPM";
	
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

	/** ************************* vm names ************************** */
	/** The list view of assignments */
	private static final String TEMPLATE_LIST_ASSIGNMENTS = "_list_assignments";

	/** The student view of assignment */
	private static final String TEMPLATE_STUDENT_VIEW_ASSIGNMENT = "_student_view_assignment";

	/** The student view of showing an assignment submission */
	private static final String TEMPLATE_STUDENT_VIEW_SUBMISSION = "_student_view_submission";
	
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
	
	/****************************** Upload all screen ***************************/
	private static final String UPLOAD_ALL_HAS_SUBMISSION_TEXT = "upload_all_has_submission_text";
	private static final String UPLOAD_ALL_HAS_SUBMISSION_ATTACHMENT = "upload_all_has_submission_attachment";
	private static final String UPLOAD_ALL_HAS_GRADEFILE = "upload_all_has_gradefile";
	private static final String UPLOAD_ALL_HAS_COMMENTS= "upload_all_has_comments";
	private static final String UPLOAD_ALL_HAS_FEEDBACK_TEXT= "upload_all_has_feedback_text";
	private static final String UPLOAD_ALL_HAS_FEEDBACK_ATTACHMENT = "upload_all_has_feedback_attachment";
	private static final String UPLOAD_ALL_RELEASE_GRADES = "upload_all_release_grades";
	
	// this is to track whether the site has multiple assignment, hence if true, show the reorder link
	private static final String HAS_MULTIPLE_ASSIGNMENTS = "has_multiple_assignments";
	
	// view all or grouped submission list
	private static final String VIEW_SUBMISSION_LIST_OPTION = "view_submission_list_option";
	
	// search string for submission list
	private static final String VIEW_SUBMISSION_SEARCH = "view_submission_search";
	
	private ContentHostingService m_contentHostingService = null;
	
	private EventTrackingService m_eventTrackingService = null;
	
	private NotificationService m_notificationService = null;
	
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
	private static final String ALLPURPOSE_RELEASE_AMPM = "allPurpose_releaseAMPM";
	private static final String ALLPURPOSE_RETRACT_YEAR = "allPurpose_retractYear";
	private static final String ALLPURPOSE_RETRACT_MONTH = "allPurpose_retractMonth";
	private static final String ALLPURPOSE_RETRACT_DAY = "allPurpose_retractDay";
	private static final String ALLPURPOSE_RETRACT_HOUR = "allPurpose_retractHour";
	private static final String ALLPURPOSE_RETRACT_MIN = "allPurpose_retractMin";
	private static final String ALLPURPOSE_RETRACT_AMPM = "allPurpose_retractAMPM";
	private static final String ALLPURPOSE_TO_DELETE = "allPurpose.toDelete";
	
	private static final String SHOW_ALLOW_RESUBMISSION = "show_allow_resubmission";
	
	private static final int INPUT_BUFFER_SIZE = 102400;
	
	private static final String SUBMISSIONS_SEARCH_ONLY = "submissions_search_only";
	
	/*************** search related *******************/
	private static final String STATE_SEARCH = "state_search";
	private static final String FORM_SEARCH = "form_search";
	
	private static final String STATE_DOWNLOAD_URL = "state_download_url";

	/** To know if grade_submission go from view_students_assignment view or not **/
	private static final String FROM_VIEW = "from_view";
	
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
			context.put("reviewServiceName", reviewServiceTitle);
			context.put("reviewServiceUse", reviewServiceUse);
		}
		
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
		else if (MODE_STUDENT_VIEW_SUBMISSION.equals(mode))
		{
			// disable auto-updates while leaving the list view
			justDelivered(state);

			// build the context for showing one assignment submission
			template = build_student_view_submission_context(portlet, context, data, state);
		}
		else if (MODE_STUDENT_VIEW_SUBMISSION_CONFIRMATION.equals(mode))
		{
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
			rv = AssignmentService.getAssignment(assignmentId);
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
			rv = AssignmentService.getSubmission(submissionId);
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
	 * build the student view of showing an assignment submission
	 */
	protected String build_student_view_submission_context(VelocityPortlet portlet, Context context, RunData data,
			SessionState state)
	{
		String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
		context.put("context", contextString);

		User user = (User) state.getAttribute(STATE_USER);
		String currentAssignmentReference = (String) state.getAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE);
		AssignmentSubmission s = null;
		
		Assignment assignment = getAssignment(currentAssignmentReference, "build_student_view_submission_context", state);
		
		if (assignment != null)
		{
			context.put("assignment", assignment);
			context.put("canSubmit", Boolean.valueOf(AssignmentService.canSubmit(contextString, assignment)));
			if (assignment.getContent().getTypeOfSubmission() == Assignment.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION)
			{
				context.put("nonElectronicType", Boolean.TRUE);
			}
			s = getSubmission(assignment.getReference(), user, "build_student_view_submission_context", state);
			if (s != null)
			{
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
				
				// put the resubmit information into context
				assignment_resubmission_option_into_context(context, state);
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
		context.put("attachments", state.getAttribute(ATTACHMENTS));
		
		context.put("contentTypeImageService", state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE));
		context.put("currentTime", TimeService.newTime());

		boolean allowSubmit = AssignmentService.allowAddSubmission((String) state.getAttribute(STATE_CONTEXT_STRING));
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
				context.put("student",student);
			}
		}
		String template = (String) getContext(data).get("template");
		return template + TEMPLATE_STUDENT_VIEW_SUBMISSION;

	} // build_student_view_submission_context

	/**
	 * build the student view of showing an assignment submission confirmation
	 */
	protected String build_student_view_submission_confirmation_context(VelocityPortlet portlet, Context context, RunData data,
			SessionState state)
	{
		String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
		context.put("context", contextString);
		
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
			
			
			AssignmentSubmission s = getSubmission(currentAssignmentReference, submitter, "build_student_view_submission_confirmation_context",state);
			if (s != null)
			{
				context.put("submitted", Boolean.valueOf(s.getSubmitted()));
				context.put("submission_id", s.getId());
				if (s.getTimeSubmitted() != null)
				{
					context.put("submit_time", s.getTimeSubmitted().toStringLocalFull());
				}
				List attachments = s.getSubmittedAttachments();
				if (attachments != null && attachments.size()>0)
				{
					context.put("submit_attachments", s.getSubmittedAttachments());
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
	 */
	protected String build_student_preview_submission_context(VelocityPortlet portlet, Context context, RunData data,
			SessionState state)
	{
		User user = (User) state.getAttribute(STATE_USER);
		String aReference = (String) state.getAttribute(PREVIEW_SUBMISSION_ASSIGNMENT_REFERENCE);

		Assignment assignment = getAssignment(aReference, "build_student_preview_submission_context", state);
		if (assignment != null)
		{
			context.put("assignment", assignment);
			
			AssignmentSubmission submission = getSubmission(aReference, user, "build_student_preview_submission_context", state);
			context.put("submission", submission);
			
			context.put("canSubmit", Boolean.valueOf(AssignmentService.canSubmit((String) state.getAttribute(STATE_CONTEXT_STRING), assignment)));
			
			// can the student view model answer or not
			canViewAssignmentIntoContext(context, assignment, submission);
			
			// put the resubmit information into context
			assignment_resubmission_option_into_context(context, state);
		}

		context.put("text", state.getAttribute(PREVIEW_SUBMISSION_TEXT));
		context.put("honor_pledge_yes", state.getAttribute(PREVIEW_SUBMISSION_HONOR_PLEDGE_YES));
		context.put("attachments", state.getAttribute(PREVIEW_SUBMISSION_ATTACHMENTS));
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
	 * build the student view of showing a graded submission
	 */
	protected String build_student_view_grade_context(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	{
		context.put("contentTypeImageService", state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE));

		Session session = SessionManager.getCurrentSession();
		SecurityAdvisor contentAdvisor = (SecurityAdvisor)session.getAttribute("assignment.content.security.advisor");
		
		String decoratedContentWrapper = (String)session.getAttribute("assignment.content.decoration.wrapper");
		session.removeAttribute("assignment.content.decoration.wrapper");
		
		String[] contentRefs = (String[])session.getAttribute("assignment.content.decoration.wrapper.refs");
		session.removeAttribute("assignment.content.decoration.wrapper.refs");
		

		if (contentAdvisor != null && contentRefs != null) {
			SecurityService.pushAdvisor(contentAdvisor);
			
			Map urlMap = new HashMap();
			for (String refStr:contentRefs) {
				Reference ref = EntityManager.newReference(refStr);
				String url = ref.getUrl();
				urlMap.put(url, url.replaceFirst("access/content", "access/" + decoratedContentWrapper + "/content"));					
			}
			context.put("decoratedUrlMap", urlMap);
		}
		SecurityAdvisor asgnAdvisor = (SecurityAdvisor)session.getAttribute("assignment.security.advisor");
		
		if (asgnAdvisor != null) {
			SecurityService.pushAdvisor(asgnAdvisor);

			session.removeAttribute("assignment.security.advisor");
		}
		
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
			
			// can the student view model answer or not
			canViewAssignmentIntoContext(context, assignment, submission);

			SecurityService.popAdvisor(); 
			//should be the asgnAdvisor that gets popped
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
			Collection groups = site.getGroups();
			context.put("groups", (groups != null && groups.size()>0)?Boolean.TRUE:Boolean.FALSE);

			// add active user list
			AuthzGroup realm = AuthzGroupService.getAuthzGroup(SiteService.siteReference(contextString));
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
		
		String template = (String) getContext(data).get("template");
		return template + TEMPLATE_LIST_ASSIGNMENTS;

	} // build_list_assignments_context
	
	private HashSet<String> getSubmittersIdSet(List submissions)
	{
		HashSet<String> rv = new HashSet<String>();
		for (Iterator iSubmissions=submissions.iterator(); iSubmissions.hasNext();)
		{
			List submitterIds = ((AssignmentSubmission) iSubmissions.next()).getSubmitterIds();
			if (submitterIds != null && submitterIds.size() > 0)
			{
				rv.add((String) submitterIds.get(0));
			}
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
		// is the assignment an new assignment
		String assignmentId = (String) state.getAttribute(EDIT_ASSIGNMENT_ID);
		if (assignmentId != null)
		{
			Assignment a = getAssignment(assignmentId, "build_instructor_new_edit_assignment_context", state);
			if (a != null)
			{
				context.put("assignment", a);
			}
		}

		// set up context variables
		setAssignmentFormContext(state, context);

		context.put("fField", state.getAttribute(NEW_ASSIGNMENT_FOCUS));

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
		
		
		context.put("name_title", NEW_ASSIGNMENT_TITLE);
		context.put("name_order", NEW_ASSIGNMENT_ORDER);

		// set open time context variables
		putTimePropertiesInContext(context, state, "Open", NEW_ASSIGNMENT_OPENMONTH, NEW_ASSIGNMENT_OPENDAY, NEW_ASSIGNMENT_OPENYEAR, NEW_ASSIGNMENT_OPENHOUR, NEW_ASSIGNMENT_OPENMIN, NEW_ASSIGNMENT_OPENAMPM);
		
		// set due time context variables
		putTimePropertiesInContext(context, state, "Due", NEW_ASSIGNMENT_DUEMONTH, NEW_ASSIGNMENT_DUEDAY, NEW_ASSIGNMENT_DUEYEAR, NEW_ASSIGNMENT_DUEHOUR, NEW_ASSIGNMENT_DUEMIN, NEW_ASSIGNMENT_DUEAMPM);

		context.put("name_EnableCloseDate", NEW_ASSIGNMENT_ENABLECLOSEDATE);
		// set close time context variables
		putTimePropertiesInContext(context, state, "Close", NEW_ASSIGNMENT_CLOSEMONTH, NEW_ASSIGNMENT_CLOSEDAY, NEW_ASSIGNMENT_CLOSEYEAR, NEW_ASSIGNMENT_CLOSEHOUR, NEW_ASSIGNMENT_CLOSEMIN, NEW_ASSIGNMENT_CLOSEAMPM);

		context.put("name_Section", NEW_ASSIGNMENT_SECTION);
		context.put("name_SubmissionType", NEW_ASSIGNMENT_SUBMISSION_TYPE);
		context.put("name_Category", NEW_ASSIGNMENT_CATEGORY);
		context.put("name_GradeType", NEW_ASSIGNMENT_GRADE_TYPE);
		context.put("name_GradePoints", NEW_ASSIGNMENT_GRADE_POINTS);
		context.put("name_Description", NEW_ASSIGNMENT_DESCRIPTION);
		// do not show the choice when there is no Schedule tool yet
		if (state.getAttribute(CALENDAR) != null)
			context.put("name_CheckAddDueDate", ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE);
		//don't show the choice when there is no Announcement tool yet
		if (state.getAttribute(ANNOUNCEMENT_CHANNEL) != null)
			context.put("name_CheckAutoAnnounce", ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE);
		context.put("name_CheckAddHonorPledge", NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE);

		// set the values
		Assignment a = null;
		String assignmentRef = (String) state.getAttribute(EDIT_ASSIGNMENT_ID);
		if (assignmentRef != null)
		{
			a = getAssignment(assignmentRef, "setAssignmentFormContext", state);
		}
		
		// put the re-submission info into context
		putTimePropertiesInContext(context, state, "Resubmit", ALLOW_RESUBMIT_CLOSEMONTH, ALLOW_RESUBMIT_CLOSEDAY, ALLOW_RESUBMIT_CLOSEYEAR, ALLOW_RESUBMIT_CLOSEHOUR, ALLOW_RESUBMIT_CLOSEMIN, ALLOW_RESUBMIT_CLOSEAMPM);
		
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
		context.put("value_GradePoints", displayGrade(state, maxGrade));
		context.put("value_Description", state.getAttribute(NEW_ASSIGNMENT_DESCRIPTION));
		
		
		// Keep the use review service setting
		context.put("value_UseReviewService", state.getAttribute(NEW_ASSIGNMENT_USE_REVIEW_SERVICE));
		context.put("value_AllowStudentView", state.getAttribute(NEW_ASSIGNMENT_ALLOW_STUDENT_VIEW) == null ? Boolean.toString(ServerConfigurationService.getBoolean("turnitin.allowStudentView.default", false)) : state.getAttribute(NEW_ASSIGNMENT_ALLOW_STUDENT_VIEW));
		
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
		context.put("show_NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INSTITUTION", ServerConfigurationService.getBoolean("turnitin.option.institution_check", true));
		
		context.put("value_NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_TURNITIN", (state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_TURNITIN) == null) ? Boolean.toString(ServerConfigurationService.getBoolean("turnitin.option.s_paper_check.default", ServerConfigurationService.getBoolean("turnitin.option.s_paper_check", true) ? true : false)) : state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_TURNITIN));
		context.put("value_NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INTERNET", state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INTERNET) == null ? Boolean.toString(ServerConfigurationService.getBoolean("turnitin.option.internet_check.default", ServerConfigurationService.getBoolean("turnitin.option.internet_check", true) ? true : false)) : state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INTERNET));
		context.put("value_NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_PUB", state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_PUB) == null ? Boolean.toString(ServerConfigurationService.getBoolean("turnitin.option.journal_check.default", ServerConfigurationService.getBoolean("turnitin.option.journal_check", true) ? true : false)) : state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_PUB));
		context.put("value_NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INSTITUTION", state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INSTITUTION) == null ? Boolean.toString(ServerConfigurationService.getBoolean("turnitin.option.institution_check.default", ServerConfigurationService.getBoolean("turnitin.option.institution_check", true) ? true : false)) : state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INSTITUTION));
		
		
		// don't show the choice when there is no Schedule tool yet
		if (state.getAttribute(CALENDAR) != null)
		context.put("value_CheckAddDueDate", state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE));
		
		// don't show the choice when there is no Announcement tool yet
		if (state.getAttribute(ANNOUNCEMENT_CHANNEL) != null)
				context.put("value_CheckAutoAnnounce", state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE));
		
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
						context.put("noAddToGradebookChoice", Boolean.valueOf(associateGradebookAssignment.equals(a.getReference())));
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
				context.put("groups", new SortedIterator(groupsAllowAddAssignment.iterator(), new AssignmentComparator(state, sort, asc)));
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
		putTimePropertiesInContext(context, state, "allPurposeRelease", ALLPURPOSE_RELEASE_MONTH, ALLPURPOSE_RELEASE_DAY, ALLPURPOSE_RELEASE_YEAR, ALLPURPOSE_RELEASE_HOUR, ALLPURPOSE_RELEASE_MIN, ALLPURPOSE_RELEASE_AMPM);
		putTimePropertiesInContext(context, state, "allPurposeRetract", ALLPURPOSE_RETRACT_MONTH, ALLPURPOSE_RETRACT_DAY, ALLPURPOSE_RETRACT_YEAR, ALLPURPOSE_RETRACT_HOUR, ALLPURPOSE_RETRACT_MIN, ALLPURPOSE_RETRACT_AMPM);
		// get attachment for all purpose object
		putSupplementItemAttachmentStateIntoContext(state, context, ALLPURPOSE_ATTACHMENTS);
		
		// put role information into context
		HashMap<String, List> roleUsers = new HashMap<String, List>();
		try
		{
			AuthzGroup realm = AuthzGroupService.getAuthzGroup(SiteService.siteReference(contextString));
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
		
	} // setAssignmentFormContext

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
				gAssignmentIdTitles.put(gradebookItem, a.getTitle());
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
					String gaId = gAssignment.isExternallyMaintained() ? Validator.escapeHtml(gAssignment.getExternalId()) : Validator.escapeHtml(gAssignment.getName());
					String status = "";
					if (gAssignmentIdTitles.containsKey(gaId))
					{
						String assignmentTitle = gAssignmentIdTitles.get(gaId);
						if (aTitle == null || !aTitle.equals(assignmentTitle))
						{
							// this gradebook item has been associated with other assignment, not selectable
							status = "disabled";
						}
						else if (aTitle != null && aTitle.equals(assignmentTitle))
						{
							// this gradebook item is associated with current assignment, make it selected
							status = "selected";
						}
					}
					gradebookAssignmentsSelectedDisabled.put(gaId, status);
					
					
					// gradebook assignment label
					String label = gAssignment.getName();
					if (gAssignmentIdTitles.containsKey(gaId))
					{
						label += " ( " + rb.getFormattedMessage("usedGradebookAssignment", new Object[]{gAssignmentIdTitles.get(gaId)}) + " )";
					}
					gradebookAssignmentsLabel.put(gaId, label);
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
			context.put("categoryTable", categoryTable());
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

		Time openTime = getTimeFromState(state, NEW_ASSIGNMENT_OPENMONTH, NEW_ASSIGNMENT_OPENDAY, NEW_ASSIGNMENT_OPENYEAR, NEW_ASSIGNMENT_OPENHOUR, NEW_ASSIGNMENT_OPENMIN, NEW_ASSIGNMENT_OPENAMPM);
		context.put("value_OpenDate", openTime);

		// due time
		Time dueTime = getTimeFromState(state, NEW_ASSIGNMENT_DUEMONTH, NEW_ASSIGNMENT_DUEDAY, NEW_ASSIGNMENT_DUEYEAR, NEW_ASSIGNMENT_DUEHOUR, NEW_ASSIGNMENT_DUEMIN, NEW_ASSIGNMENT_DUEAMPM);
		context.put("value_DueDate", dueTime);

		// close time
		Time closeTime = TimeService.newTime();
		Boolean enableCloseDate = (Boolean) state.getAttribute(NEW_ASSIGNMENT_ENABLECLOSEDATE);
		context.put("value_EnableCloseDate", enableCloseDate);
		if ((enableCloseDate).booleanValue())
		{
			closeTime = getTimeFromState(state, NEW_ASSIGNMENT_CLOSEMONTH, NEW_ASSIGNMENT_CLOSEDAY, NEW_ASSIGNMENT_CLOSEYEAR, NEW_ASSIGNMENT_CLOSEHOUR, NEW_ASSIGNMENT_CLOSEMIN, NEW_ASSIGNMENT_CLOSEAMPM);
			context.put("value_CloseDate", closeTime);
		}

		context.put("value_Sections", state.getAttribute(NEW_ASSIGNMENT_SECTION));
		context.put("value_SubmissionType", state.getAttribute(NEW_ASSIGNMENT_SUBMISSION_TYPE));
		context.put("value_GradeType", state.getAttribute(NEW_ASSIGNMENT_GRADE_TYPE));
		String maxGrade = (String) state.getAttribute(NEW_ASSIGNMENT_GRADE_POINTS);
		context.put("value_GradePoints", displayGrade(state, maxGrade));
		context.put("value_Description", state.getAttribute(NEW_ASSIGNMENT_DESCRIPTION));
		context.put("value_CheckAddDueDate", state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE));
		context.put("value_CheckAutoAnnounce", state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE));
		context.put("value_CheckAddHonorPledge", state.getAttribute(NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE));

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
			putTimePropertiesInContext(context, state, "Resubmit", ALLOW_RESUBMIT_CLOSEMONTH, ALLOW_RESUBMIT_CLOSEDAY, ALLOW_RESUBMIT_CLOSEYEAR, ALLOW_RESUBMIT_CLOSEHOUR, ALLOW_RESUBMIT_CLOSEMIN, ALLOW_RESUBMIT_CLOSEAMPM);
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

		// format to show one decimal place in grade
		context.put("value_grade", (gradeType == 3) ? displayGrade(state, (String) state.getAttribute(GRADE_SUBMISSION_GRADE))
				: state.getAttribute(GRADE_SUBMISSION_GRADE));

		context.put("assignment_expand_flag", state.getAttribute(GRADE_SUBMISSION_ASSIGNMENT_EXPAND_FLAG));

		// is this a non-electronic submission type of assignment
		context.put("nonElectronic", (a!=null && a.getContent().getTypeOfSubmission() == Assignment.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION)?Boolean.TRUE:Boolean.FALSE);
		
		if (addGradeDraftAlert)
		{
			addAlert(state, rb.getString("grading.alert.draft.beforeclosedate"));
		}
		context.put("alertGradeDraft", Boolean.valueOf(addGradeDraftAlert));
		
		// for the navigation purpose
		List<UserSubmission> userSubmissions = state.getAttribute(USER_SUBMISSIONS) != null ? (List<UserSubmission>) state.getAttribute(USER_SUBMISSIONS):null;
		if (userSubmissions != null)
		{
			for (int i = 0; i < userSubmissions.size(); i++)
			{
				if (((UserSubmission) userSubmissions.get(i)).getSubmission().getId().equals(submissionId))
				{
					boolean goPT = false;
					boolean goNT = false;
					if ((i - 1) >= 0)
					{
						goPT = true;
					}
					if ((i + 1) < userSubmissions.size())
					{
						goNT = true;
					}
					context.put("goPTButton", Boolean.valueOf(goPT));
					context.put("goNTButton", Boolean.valueOf(goNT));
					
					if (i>0)
					{
						// retrieve the previous submission id
						context.put("prevSubmissionId", ((UserSubmission) userSubmissions.get(i-1)).getSubmission().getReference());
					}
					
					if (i < userSubmissions.size() - 1)
					{
						// retrieve the next submission id
						context.put("nextSubmissionId", ((UserSubmission) userSubmissions.get(i+1)).getSubmission().getReference());
					}
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
		
		// letter grading
		letterGradeOptionsIntoContext(context);
		
		String template = (String) getContext(data).get("template");
		return template + TEMPLATE_INSTRUCTOR_GRADE_SUBMISSION;

	} // build_instructor_grade_submission_context

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
	
	/**
	 * Responding to the request of submission navigation
	 * @param rundata
	 * @param option
	 */
	public void doPrev_back_next_submission(RunData rundata, String option)
	{
		SessionState state = ((JetspeedRunData) rundata).getPortletSessionState(((JetspeedRunData) rundata).getJs_peid());
		// save the instructor input
		boolean hasChange = readGradeForm(rundata, state, "save");
		if (state.getAttribute(STATE_MESSAGE) == null && hasChange)
		{
			grade_submission_option(rundata, "save");
		}
		
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			if ("next".equals(option))
			{
				navigateToSubmission(rundata, "nextSubmissionId");
			}
			else if ("prev".equals(option))
			{
				navigateToSubmission(rundata, "prevSubmissionId");
			}
			else if ("back".equals(option))
			{
				state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_GRADE_ASSIGNMENT);
			}
			else if ("backListStudent".equals(option))
			{
				state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT);
			}
		}

	} // doPrev_back_next_submission


	private void navigateToSubmission(RunData rundata, String paramString) {
		ParameterParser params = rundata.getParameters();
		SessionState state = ((JetspeedRunData) rundata).getPortletSessionState(((JetspeedRunData) rundata).getJs_peid());
		String assignmentId = (String) state.getAttribute(GRADE_SUBMISSION_ASSIGNMENT_ID);
		String submissionId = StringUtils.trimToNull(params.getString(paramString));
		if (submissionId != null)
		{
			// put submission information into state
			putSubmissionInfoIntoState(state, assignmentId, submissionId);
		}
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
	 * @param ampm
	 */
	private void putTimePropertiesInState(SessionState state, Time timeValue,
											String month, String day, String year, String hour, String min, String ampm) {
		TimeBreakdown bTime = timeValue.breakdownLocal();
		state.setAttribute(month, Integer.valueOf(bTime.getMonth()));
		state.setAttribute(day, Integer.valueOf(bTime.getDay()));
		state.setAttribute(year, Integer.valueOf(bTime.getYear()));
		int bHour = bTime.getHour();
		if (bHour >= 12)
		{
			state.setAttribute(ampm, "PM");
		}
		else
		{		
			state.setAttribute(ampm, "AM");
		}
		if (bHour == 0)
		{
			// for midnight point, we mark it as 12AM
			bHour = 12;
		}		
		state.setAttribute(hour, Integer.valueOf((bHour > 12) ? bHour - 12 : bHour));
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
	 * @param ampm
	 */
	private void putTimePropertiesInContext(Context context, SessionState state, String timeName,
													String month, String day, String year, String hour, String min, String ampm) {
		// get the submission level of close date setting
		context.put("name_" + timeName + "Month", month);
		context.put("name_" + timeName + "Day", day);
		context.put("name_" + timeName + "Year", year);
		context.put("name_" + timeName + "Hour", hour);
		context.put("name_" + timeName + "Min", min);
		context.put("name_" + timeName + "AMPM", ampm);
		context.put("value_" + timeName + "Month", (Integer) state.getAttribute(month));
		context.put("value_" + timeName + "Day", (Integer) state.getAttribute(day));
		context.put("value_" + timeName + "Year", (Integer) state.getAttribute(year));
		context.put("value_" + timeName + "AMPM", (String) state.getAttribute(ampm));
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
		context.put("submission", getSubmission((String) state.getAttribute(GRADE_SUBMISSION_SUBMISSION_ID), "build_instructor_preview_grade_submission_context", state));

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

		// format to show one decimal place
		String grade = (String) state.getAttribute(GRADE_SUBMISSION_GRADE);
		if (gradeType == 3)
		{
			grade = displayGrade(state, grade);
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

		String assignmentRef = (String) state.getAttribute(EXPORT_ASSIGNMENT_REF);
		Assignment assignment = getAssignment(assignmentRef, "build_instructor_grade_assignment_context", state);
		if (assignment != null)
		{
			context.put("assignment", assignment);
			state.setAttribute(EXPORT_ASSIGNMENT_ID, assignment.getId());
			if (assignment.getContent() != null) 
			{
				context.put("value_SubmissionType", Integer.valueOf(assignment.getContent().getTypeOfSubmission()));
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
			context.put("searchString", state.getAttribute(VIEW_SUBMISSION_SEARCH));
			
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
			
			List<UserSubmission> userSubmissions = prepPage(state);
			state.setAttribute(USER_SUBMISSIONS, userSubmissions);
			context.put("userSubmissions", state.getAttribute(USER_SUBMISSIONS));
			
			// whether to show the resubmission choice
			if (state.getAttribute(SHOW_ALLOW_RESUBMISSION) != null)
			{
				context.put("showAllowResubmission", Boolean.TRUE);
			}
			// put the re-submission info into context
			putTimePropertiesInContext(context, state, "Resubmit", ALLOW_RESUBMIT_CLOSEMONTH, ALLOW_RESUBMIT_CLOSEDAY, ALLOW_RESUBMIT_CLOSEYEAR, ALLOW_RESUBMIT_CLOSEHOUR, ALLOW_RESUBMIT_CLOSEMIN, ALLOW_RESUBMIT_CLOSEAMPM);
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
		String searchString = (String) state.getAttribute(STATE_SEARCH);
		if (searchString == null)
		{
			searchString = rb.getString("search_student_instruction");
		}
		context.put("searchString", searchString);
		
		context.put("form_search", FORM_SEARCH);
		context.put("showSubmissionByFilterSearchOnly", state.getAttribute(SUBMISSIONS_SEARCH_ONLY) != null && ((Boolean) state.getAttribute(SUBMISSIONS_SEARCH_ONLY)) ? Boolean.TRUE:Boolean.FALSE);
		
		// letter grading
		letterGradeOptionsIntoContext(context);
		
		// ever set the default grade for no-submissions
		if (assignment != null && assignment.getContent().getTypeOfSubmission() == Assignment.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION)
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
		}

		context.put("currentTime", TimeService.newTime());
		context.put("submissionTypeTable", submissionTypeTable());
		context.put("hideAssignmentFlag", state.getAttribute(VIEW_ASSIGNMENT_HIDE_ASSIGNMENT_FLAG));
		context.put("hideStudentViewFlag", state.getAttribute(VIEW_ASSIGNMENT_HIDE_STUDENT_VIEW_FLAG));
		context.put("contentTypeImageService", state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE));
		
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
		
		//		 put site object into context
		try
		{
			// get current site
			Site site = SiteService.getSite((String) state.getAttribute(STATE_CONTEXT_STRING));
			context.put("site", site);
		}
		catch (Exception ignore)
		{
			M_log.warn(this + ":build_instructor_reorder_assignment_context " + ignore.getMessage());
		}
	
		context.put("contentTypeImageService", state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE));
		context.put("userDirectoryService", UserDirectoryService.getInstance());
	
		String template = (String) getContext(data).get("template");
		return template + TEMPLATE_INSTRUCTOR_REORDER_ASSIGNMENT;
	
	} // build_instructor_reorder_assignment_context

	/**
	 * build the instructor view to view the list of students for an assignment
	 */
	protected String build_instructor_view_students_assignment_context(VelocityPortlet portlet, Context context, RunData data,
			SessionState state)
	{
		// cleaning from view attribute
		state.removeAttribute(FROM_VIEW);

		String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);

		// get the realm and its member
		List studentMembers = new ArrayList();
		List allowSubmitMembers = AssignmentService.allowAddAnySubmissionUsers(contextString);
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
		context.put("submissions", prepPage(state));

		context.put("sortedBy", (String) state.getAttribute(SORTED_SUBMISSION_BY));
		context.put("sortedAsc", (String) state.getAttribute(SORTED_SUBMISSION_ASC));
		context.put("sortedBy_lastName", SORTED_SUBMISSION_BY_LASTNAME);
		context.put("sortedBy_submitTime", SORTED_SUBMISSION_BY_SUBMIT_TIME);
		context.put("sortedBy_grade", SORTED_SUBMISSION_BY_GRADE);
		context.put("sortedBy_status", SORTED_SUBMISSION_BY_STATUS);
		context.put("sortedBy_released", SORTED_SUBMISSION_BY_RELEASED);
		context.put("sortedBy_assignment", SORTED_SUBMISSION_BY_ASSIGNMENT);
		context.put("sortedBy_maxGrade", SORTED_SUBMISSION_BY_MAX_GRADE);

		add2ndToolbarFields(data, context);

		String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
		context.put("accessPointUrl", ServerConfigurationService.getAccessUrl()
				+ AssignmentService.gradesSpreadsheetReference(contextString, null));

		pagingInfoToContext(state, context);

		String template = (String) getContext(data).get("template");
		return template + TEMPLATE_INSTRUCTOR_REPORT_SUBMISSIONS;

	} // build_instructor_report_submissions
	
	// Is Gradebook defined for the site?
	protected boolean isGradebookDefined()
	{
		boolean rv = false;
		try
		{
			GradebookService g = (GradebookService)  ComponentManager
					.get("org.sakaiproject.service.gradebook.GradebookService");
			String gradebookUid = ToolManager.getInstance().getCurrentPlacement().getContext();
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
		context.put("hasComments", state.getAttribute(UPLOAD_ALL_HAS_COMMENTS));
		context.put("hasFeedbackText", state.getAttribute(UPLOAD_ALL_HAS_FEEDBACK_TEXT));
		context.put("hasFeedbackAttachment", state.getAttribute(UPLOAD_ALL_HAS_FEEDBACK_ATTACHMENT));
		context.put("releaseGrades", state.getAttribute(UPLOAD_ALL_RELEASE_GRADES));
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
		
		String gradebookUid = ToolManager.getInstance().getCurrentPlacement().getContext();
		if (g.isGradebookDefined(gradebookUid) && g.currentUserHasGradingPerm(gradebookUid))
		{
			boolean isExternalAssignmentDefined=gExternal.isExternalAssignmentDefined(gradebookUid, assignmentRef);
			boolean isExternalAssociateAssignmentDefined = gExternal.isExternalAssignmentDefined(gradebookUid, associateGradebookAssignment);
			boolean isAssignmentDefined = g.isAssignmentDefined(gradebookUid, associateGradebookAssignment);

			if (addUpdateRemoveAssignment != null)
			{
				// add an entry into Gradebook for newly created assignment or modified assignment, and there wasn't a correspond record in gradebook yet
				if ((addUpdateRemoveAssignment.equals(AssignmentService.GRADEBOOK_INTEGRATION_ADD) || ("update".equals(addUpdateRemoveAssignment) && !isExternalAssignmentDefined)) && associateGradebookAssignment == null)
				{
					// add assignment into gradebook
					try
					{
						// add assignment to gradebook
						gExternal.addExternalAssessment(gradebookUid, assignmentRef, null, newAssignment_title, newAssignment_maxPoints/10.0, new Date(newAssignment_dueTime.getTime()), assignmentToolTitle, false, category != -1?Long.valueOf(category):null);
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
					catch (ConflictingExternalIdException e)
					{
						// this shouldn't happen, as we have already checked for assignment reference before. Log the error
						M_log.warn(this + ":integrateGradebook " + e.getMessage());
					}
					catch (GradebookNotFoundException e)
					{
						// this shouldn't happen, as we have checked for gradebook existence before
						M_log.warn(this + ":integrateGradebook " + e.getMessage());
					}
					catch (Exception e)
					{
						// ignore
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
						    gExternal.updateExternalAssessment(gradebookUid, associateGradebookAssignment, null, newAssignment_title, newAssignment_maxPoints/10.0, new Date(newAssignment_dueTime.getTime()), false);
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
					if ("update".equals(updateRemoveSubmission)
							&& a.getProperties().getProperty(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK) != null
							&& !a.getProperties().getProperty(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK).equals(AssignmentService.GRADEBOOK_INTEGRATION_NO)
							&& a.getContent().getTypeOfGrade() == Assignment.SCORE_GRADE_TYPE)
					{
						if (submissionRef == null)
						{
							// bulk add all grades for assignment into gradebook
							Iterator submissions = AssignmentService.getSubmissions(a).iterator();

							Map<String, String> m = new HashMap<String, String>();

							// any score to copy over? get all the assessmentGradingData and copy over
							while (submissions.hasNext())
							{
								AssignmentSubmission aSubmission = (AssignmentSubmission) submissions.next();
								if (aSubmission.getGradeReleased())
								{
									User[] submitters = aSubmission.getSubmitters();
									if (submitters != null && submitters.length > 0) {
										String submitterId = submitters[0].getId();
										String gradeString = StringUtils.trimToNull(aSubmission.getGrade(false));
										String grade = gradeString != null ? displayGrade(state,gradeString) : null;
										m.put(submitterId, grade);
									}
								}
							}

							// need to update only when there is at least one submission
							if (!m.isEmpty())
							{
								if (associateGradebookAssignment != null)
								{
									if (isExternalAssociateAssignmentDefined)
									{
										// the associated assignment is externally maintained
										gExternal.updateExternalAssessmentScoresString(gradebookUid, associateGradebookAssignment, m);
									}
									else if (isAssignmentDefined)
									{
										// the associated assignment is internal one, update records one by one
										for (Map.Entry<String, String> entry : m.entrySet())
										{
											String submitterId = (String) entry.getKey();
											String grade = StringUtils.trimToNull(displayGrade(state, (String) m.get(submitterId)));
											if (grade != null)
											{
												g.setAssignmentScoreString(gradebookUid, associateGradebookAssignment, submitterId, grade, "");
											}
										}
									}
								}
								else if (isExternalAssignmentDefined)
								{
									gExternal.updateExternalAssessmentScoresString(gradebookUid, assignmentRef, m);
								}
							}
						}
						else
						{
							// only update one submission
							AssignmentSubmission aSubmission = getSubmission(submissionRef, "integrateGradebook", state);
							if (aSubmission != null)
							{
								User[] submitters = aSubmission.getSubmitters();
								String gradeString = StringUtils.trimToNull(displayGrade(state, aSubmission.getGrade(false)));
								if (submitters != null && submitters.length > 0)
								{
									if (associateGradebookAssignment != null)
									{
										if (gExternal.isExternalAssignmentDefined(gradebookUid, associateGradebookAssignment))
										{
											// the associated assignment is externally maintained
											gExternal.updateExternalAssessmentScore(gradebookUid, associateGradebookAssignment, submitters[0].getId(),
													(gradeString != null && aSubmission.getGradeReleased()) ? gradeString : null);
										}
										else if (g.isAssignmentDefined(gradebookUid, associateGradebookAssignment))
										{
											// the associated assignment is internal one, update records
											g.setAssignmentScoreString(gradebookUid, associateGradebookAssignment, submitters[0].getId(),
													gradeString != null ? gradeString : "0", "");
										}
									}
									else
									{
										gExternal.updateExternalAssessmentScore(gradebookUid, assignmentRef, submitters[0].getId(),
												(gradeString != null && aSubmission.getGradeReleased()) ? gradeString : null);
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
								User[] submitters = aSubmission.getSubmitters();
								if (submitters != null && submitters.length > 0)
								{
									if (isExternalAssociateAssignmentDefined)
									{
										// if the old associated assignment is an external maintained one
										gExternal.updateExternalAssessmentScore(gradebookUid, associateGradebookAssignment, submitters[0].getId(), null);
									}
									else if (isAssignmentDefined)
									{
										g.setAssignmentScoreString(gradebookUid, associateGradebookAssignment, submitters[0].getId(), "0", assignmentToolTitle);
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
								if (submitters != null && submitters.length > 0)
								{
									if (isExternalAssociateAssignmentDefined)
									{
										// external assignment
										gExternal.updateExternalAssessmentScore(gradebookUid, assignmentRef, submitters[0].getId(), null);
									}
									else if (isAssignmentDefined)
									{
										// gb assignment
										g.setAssignmentScoreString(gradebookUid, associateGradebookAssignment, submitters[0].getId(), "0", "");
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

        String submitterId = params.get("submitterId");
        if (submitterId != null) {
            try {
                u = UserDirectoryService.getUser(submitterId);
                state.setAttribute("student", u);
            } catch (UserNotDefinedException ex) {
                M_log.warn(this + ":doView_submission cannot find user with id " + submitterId + " " + ex.getMessage());
            }
        }

		Assignment a = getAssignment(assignmentReference, "doView_submission", state);
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
			
			state.setAttribute(STATE_MODE, MODE_STUDENT_VIEW_SUBMISSION);
			
			if (submission != null)
			{
				// submission read event
				m_eventTrackingService.post(m_eventTrackingService.newEvent(AssignmentConstants.EVENT_ACCESS_ASSIGNMENT_SUBMISSION, submission.getId(), false));
			}
			else
			{
				// otherwise, the student just read assignment description and prepare for submission
				m_eventTrackingService.post(m_eventTrackingService.newEvent(AssignmentConstants.EVENT_ACCESS_ASSIGNMENT, a.getId(), false));
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
			state.setAttribute(VIEW_SUBMISSION_LIST_OPTION, params.getString("view"));
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
	 * Preview of the submission
	 */
	public void doPreview_submission(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		ParameterParser params = data.getParameters();
		String aReference = (String) state.getAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE);
		state.setAttribute(PREVIEW_SUBMISSION_ASSIGNMENT_REFERENCE, aReference);
		Assignment a = getAssignment(aReference, "doPreview_submission", state);

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

		// back to the student list view of assignments
		state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);

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
		putSubmissionInfoIntoState(state, assignmentId, sId);
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
		state.removeAttribute(GRADE_SUBMISSION_GRADE);
		state.removeAttribute(GRADE_SUBMISSION_SUBMISSION_ID);
		state.removeAttribute(GRADE_GREATER_THAN_MAX_ALERT);
		state.removeAttribute(GRADE_SUBMISSION_DONE);
		state.removeAttribute(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER);
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
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		readGradeForm(data, state, "save");
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			grade_submission_option(data, "save");
		}

	} // doSave_grade_submission

	/**
	 * Action is to release the grade to submission
	 */
	public void doRelease_grade_submission(RunData data)
	{
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
		grade_submission_option(data, "return");

	} // doReturn_grade_preview_submission

	/**
	 * Action is to save submission with or without grade from preview
	 */
	public void doSave_preview_grade_submission(RunData data)
	{
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
			Assignment a = sEdit.getAssignment();
			int typeOfGrade = a.getContent().getTypeOfGrade();

			if (!withGrade)
			{
				// no grade input needed for the without-grade version of assignment tool
				sEdit.setGraded(true);
				if ("return".equals(gradeOption) || "release".equals(gradeOption))
				{
					sEdit.setGradeReleased(true);
				}
			}
			else if (grade == null)
			{
				sEdit.setGrade("");
				sEdit.setGraded(false);
				sEdit.setGradeReleased(false);
			}
			else
			{
				sEdit.setGrade(grade);
				
				if (grade.length() != 0)
				{
					sEdit.setGraded(true);
				}
				else
				{
					sEdit.setGraded(false);
				}
			}

			if ("release".equals(gradeOption))
			{
				sEdit.setGradeReleased(true);
				sEdit.setGraded(true);
				// clear the returned flag
				sEdit.setReturned(false);
				sEdit.setTimeReturned(null);
			}
			else if ("return".equals(gradeOption))
			{
				sEdit.setGradeReleased(true);
				sEdit.setGraded(true);
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
					Time closeTime = getTimeFromState(state, ALLOW_RESUBMIT_CLOSEMONTH, ALLOW_RESUBMIT_CLOSEDAY, ALLOW_RESUBMIT_CLOSEYEAR, ALLOW_RESUBMIT_CLOSEHOUR, ALLOW_RESUBMIT_CLOSEMIN, ALLOW_RESUBMIT_CLOSEAMPM);
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
			// put submission information into state
			putSubmissionInfoIntoState(state, assignmentId, sId);
			state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_GRADE_SUBMISSION);
			state.setAttribute(GRADE_SUBMISSION_DONE, Boolean.TRUE);
		}
		else
		{
			state.removeAttribute(GRADE_SUBMISSION_DONE);
		}

	} // grade_submission_option

	/**
	 * Action is to save the submission as a draft
	 */
	public void doSave_submission(RunData data)
	{
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
			if (honorPledgeYes == null)
			{
				honorPledgeYes = (String) state.getAttribute(VIEW_SUBMISSION_HONOR_PLEDGE_YES);
			}
	
			if (honorPledgeYes == null)
			{
				honorPledgeYes = "false";
			}
	
			User u = (User) state.getAttribute(STATE_USER);
			User submitter = null;
			String studentId = params.get("submit_on_behalf_of");
			if (studentId != null && !studentId.equals("-1")) {
				try {
					submitter = u;
					u = UserDirectoryService.getUser(studentId);
				} catch (UserNotDefinedException ex1) {
					M_log.warn("Unable to find user with ID [" + studentId + "]");
					submitter = null;
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
				
				// get attachment input and generate alert message according to assignment submission type
				checkSubmissionTextAttachmentInput(data, state, a, text);
			}
			if ((state.getAttribute(STATE_MESSAGE) == null) && (a != null))
			{
				AssignmentSubmission submission = getSubmission(a.getReference(), u, "post_save_submission", state);
				if (submission != null)
				{
					// the submission already exists, change the text and honor pledge value, post it
					AssignmentSubmissionEdit sEdit = editSubmission(submission.getReference(), "post_save_submission", state);
					if (sEdit != null)
					{
						ResourcePropertiesEdit sPropertiesEdit = sEdit.getPropertiesEdit();
						
						sEdit.setSubmittedText(text);
						sEdit.setHonorPledgeFlag(Boolean.valueOf(honorPledgeYes).booleanValue());
						sEdit.setTimeSubmitted(TimeService.newTime());
						sEdit.setSubmitted(post);
						
						// decrease the allow_resubmit_number, if this submission has been submitted.
						if (sEdit.getSubmitted() && sEdit.getTimeSubmitted() != null && sPropertiesEdit.getProperty(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER) != null)
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
										
										NumberFormat nbFormat = (DecimalFormat) getNumberFormat();
										DecimalFormat dcFormat = (DecimalFormat) nbFormat;
										String decSeparator = dcFormat.getDecimalFormatSymbols().getDecimalSeparator() + "";
										
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
							previousGrades =  "<h4>" + now.toStringLocalFull() + "</h4>" + "<div style=\"margin:0;padding:0\">" + sEdit.getGradeDisplay() + "</div>" +previousGrades;

							sPropertiesEdit.addProperty(ResourceProperties.PROP_SUBMISSION_SCALED_PREVIOUS_GRADES,
									previousGrades);

							// clear the current grade and make the submission ungraded
							sEdit.setGraded(false);
							sEdit.setGrade("");
							sEdit.setGradeReleased(false);
							
							// clean the ContentReview attributes
							sEdit.setReviewIconUrl(null);
							sEdit.setReviewScore(0); // default to be 0?
							sEdit.setReviewStatus(null);

							// keep the history of assignment feed back text
							String feedbackTextHistory = sPropertiesEdit
									.getProperty(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT) != null ? sPropertiesEdit
									.getProperty(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT)
									: "";
							feedbackTextHistory =  "<h4>" + now.toStringLocalFull() + "</h4>" + "<div style=\"margin:0;padding:0\">" + sEdit.getFeedbackText() + "</div>" + feedbackTextHistory;
							sPropertiesEdit.addProperty(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT,
									feedbackTextHistory);

							// keep the history of assignment feed back comment
							String feedbackCommentHistory = sPropertiesEdit
									.getProperty(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT) != null ? sPropertiesEdit
									.getProperty(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT)
									: "";
							feedbackCommentHistory = "<h4>" + now.toStringLocalFull() + "</h4>" + "<div style=\"margin:0;padding:0\">" + sEdit.getFeedbackComment() + "</div>" + feedbackCommentHistory;
							sPropertiesEdit.addProperty(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT,
									feedbackCommentHistory);
							
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
						}
						
						sEdit.setAssignment(a);

						// add attachments
						List attachments = (List) state.getAttribute(ATTACHMENTS);
						if (attachments != null)
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

						if (submitter != null) {
                            sPropertiesEdit.addProperty(AssignmentSubmission.SUBMITTER_USER_ID, submitter.getId());
                            state.setAttribute(STATE_SUBMITTER, u.getId());
                        } else {
                            sPropertiesEdit.removeProperty(AssignmentSubmission.SUBMITTER_USER_ID);
                        }

						AssignmentService.commitEdit(sEdit);
						
					}
				}
				else
				{
					// new submission
					try
					{
						AssignmentSubmissionEdit edit = AssignmentService.addSubmission(contextString, assignmentId, SessionManager.getCurrentSessionUserId());
						edit.setSubmittedText(text);
						edit.setHonorPledgeFlag(Boolean.valueOf(honorPledgeYes).booleanValue());
						edit.setTimeSubmitted(TimeService.newTime());
						edit.setSubmitted(post);
						edit.setAssignment(a);
                        ResourcePropertiesEdit sPropertiesEdit = edit.getPropertiesEdit();

						// add attachments
						List attachments = (List) state.getAttribute(ATTACHMENTS);
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

						AssignmentService.commitEdit(edit);
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
			
		}	// if

	} // post_save_submission


	private void checkSubmissionTextAttachmentInput(RunData data,
			SessionState state, Assignment a, String text) {
		if (a != null)
		{
			// check the submission inputs based on the submission type
			int submissionType = a.getContent().getTypeOfSubmission();
			if (submissionType == 1)
			{
				// for the inline only submission
				if (text.length() == 0)
				{
					addAlert(state, rb.getString("youmust7"));
				}
			}
			else if (submissionType == 2)
			{
				// for the attachment only submission
				List v = (List) state.getAttribute(ATTACHMENTS);
				if ((v == null) || (v.size() == 0))
				{
					addAlert(state, rb.getString("youmust1"));
				}
			}
			else if (submissionType == 3)
			{	
				// for the inline and attachment submission
				List v = (List) state.getAttribute(ATTACHMENTS);
				if ((text.length() == 0 || "<br/>".equals(text)) && ((v == null) || (v.size() == 0)))
				{
					addAlert(state, rb.getString("youmust2"));
				}
			}
		}
	}
	
	/**
	 * Action is to confirm the submission and return to list view
	 */
	public void doConfirm_assignment_submission(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
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
		Time openTime = putTimeInputInState(params, state, NEW_ASSIGNMENT_OPENMONTH, NEW_ASSIGNMENT_OPENDAY, NEW_ASSIGNMENT_OPENYEAR, NEW_ASSIGNMENT_OPENHOUR, NEW_ASSIGNMENT_OPENMIN, NEW_ASSIGNMENT_OPENAMPM, "newassig.opedat");

		// due time
		Time dueTime = putTimeInputInState(params, state, NEW_ASSIGNMENT_DUEMONTH, NEW_ASSIGNMENT_DUEDAY, NEW_ASSIGNMENT_DUEYEAR, NEW_ASSIGNMENT_DUEHOUR, NEW_ASSIGNMENT_DUEMIN, NEW_ASSIGNMENT_DUEAMPM, "gen.duedat");		
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
		Time closeTime = putTimeInputInState(params, state, NEW_ASSIGNMENT_CLOSEMONTH, NEW_ASSIGNMENT_CLOSEDAY, NEW_ASSIGNMENT_CLOSEYEAR, NEW_ASSIGNMENT_CLOSEHOUR, NEW_ASSIGNMENT_CLOSEMIN, NEW_ASSIGNMENT_CLOSEAMPM, "date.closedate");		
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
		state.setAttribute(NEW_ASSIGNMENT_SUBMISSION_TYPE, Integer.valueOf(params.getString(NEW_ASSIGNMENT_SUBMISSION_TYPE)));

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

		
		String r = params.getString(NEW_ASSIGNMENT_USE_REVIEW_SERVICE);
		String b;
		// set whether we use the review service or not
		if (r == null) b = Boolean.FALSE.toString();
		else b = Boolean.TRUE.toString();
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
		
		// treat the new assignment description as formatted text
		boolean checkForFormattingErrors = true; // instructor is creating a new assignment - so check for errors
		String description = processFormattedTextFromBrowser(state, params.getCleanString(NEW_ASSIGNMENT_DESCRIPTION),
				checkForFormattingErrors);
		state.setAttribute(NEW_ASSIGNMENT_DESCRIPTION, description);

		if (state.getAttribute(CALENDAR) != null)
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

		String s = params.getString(NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE);

		// set the honor pledge to be "no honor pledge"
		if (s == null) s = "1";
		state.setAttribute(NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE, s);

		String grading = params.getString(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK);
		state.setAttribute(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, grading);

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
		else
		{
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
						validPointGrade(state, gradePoints);
						// when scale is points, grade must be integer and less than maximum value
						if (state.getAttribute(STATE_MESSAGE) == null)
						{
							gradePoints = scalePointGrade(state, gradePoints);
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
			putTimeInputInState(params, state, ALLPURPOSE_RELEASE_MONTH, ALLPURPOSE_RELEASE_DAY, ALLPURPOSE_RELEASE_YEAR, ALLPURPOSE_RELEASE_HOUR, ALLPURPOSE_RELEASE_MIN, ALLPURPOSE_RELEASE_AMPM, "date.allpurpose.releasedate");
		}
		else
		{
			state.removeAttribute(ALLPURPOSE_SHOW_FROM);
		}
		if (StringUtils.trimToNull(params.getString("allPurposeShowTo")) != null)
		{
			state.setAttribute(ALLPURPOSE_SHOW_TO, Boolean.valueOf(params.getString("allPurposeShowTo")));
			// allpurpose retract time
			putTimeInputInState(params, state, ALLPURPOSE_RETRACT_MONTH, ALLPURPOSE_RETRACT_DAY, ALLPURPOSE_RETRACT_YEAR, ALLPURPOSE_RETRACT_HOUR, ALLPURPOSE_RETRACT_MIN, ALLPURPOSE_RETRACT_AMPM, "date.allpurpose.retractdate");
		}
		else
		{
			state.removeAttribute(ALLPURPOSE_SHOW_TO);
		}
		
		String siteId = (String)state.getAttribute(STATE_CONTEXT_STRING);
		List<String> accessList = new ArrayList<String>();
		try
		{
			AuthzGroup realm = AuthzGroupService.getAuthzGroup(SiteService.siteReference(siteId));
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
	 * @param ampmString
	 * @param invalidBundleMessage
	 * @return
	 */
	Time putTimeInputInState(ParameterParser params, SessionState state, String monthString, String dayString, String yearString, String hourString, String minString, String ampmString, String invalidBundleMessage)
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
		String ampm = params.getString(ampmString);
		state.setAttribute(ampmString, ampm);
		if (("PM".equals(ampm)) && (hour != 12))
		{
			hour = hour + 12;
		}
		if ((hour == 12) && ("AM".equals(ampm)))
		{
			hour = 0;
		}
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
		
		// whether this is an editing which changes non-electronic assignment to any other type?
		boolean bool_change_from_non_electronic = false;
		// whether this is an editing which changes non-point graded assignment to point graded assignment?
		boolean bool_change_from_non_point = false;
		// whether there is a change in the assignment resubmission choice
		boolean bool_change_resubmit_option = false;

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			// AssignmentContent object
			AssignmentContentEdit ac = editAssignmentContent(assignmentContentId, "post_save_assignment", state, true);
			bool_change_from_non_electronic = change_from_non_electronic(state, assignmentId, assignmentContentId, ac);
			bool_change_from_non_point = change_from_non_point(state, assignmentId, assignmentContentId, ac);
			
			// Assignment
			AssignmentEdit a = editAssignment(assignmentId, "post_save_assignment", state, true);
			bool_change_resubmit_option = change_resubmit_option(state, a);

			// put the names and values into vm file
			String title = (String) state.getAttribute(NEW_ASSIGNMENT_TITLE);
			String order = (String) state.getAttribute(NEW_ASSIGNMENT_ORDER);

			// open time
			Time openTime = getTimeFromState(state, NEW_ASSIGNMENT_OPENMONTH, NEW_ASSIGNMENT_OPENDAY, NEW_ASSIGNMENT_OPENYEAR, NEW_ASSIGNMENT_OPENHOUR, NEW_ASSIGNMENT_OPENMIN, NEW_ASSIGNMENT_OPENAMPM);

			// due time
			Time dueTime = getTimeFromState(state, NEW_ASSIGNMENT_DUEMONTH, NEW_ASSIGNMENT_DUEDAY, NEW_ASSIGNMENT_DUEYEAR, NEW_ASSIGNMENT_DUEHOUR, NEW_ASSIGNMENT_DUEMIN, NEW_ASSIGNMENT_DUEAMPM);

			// close time
			Time closeTime = dueTime;
			boolean enableCloseDate = ((Boolean) state.getAttribute(NEW_ASSIGNMENT_ENABLECLOSEDATE)).booleanValue();
			if (enableCloseDate)
			{
				closeTime = getTimeFromState(state, NEW_ASSIGNMENT_CLOSEMONTH, NEW_ASSIGNMENT_CLOSEDAY, NEW_ASSIGNMENT_CLOSEYEAR, NEW_ASSIGNMENT_CLOSEHOUR, NEW_ASSIGNMENT_CLOSEMIN, NEW_ASSIGNMENT_CLOSEAMPM);
			}

			// sections
			String section = (String) state.getAttribute(NEW_ASSIGNMENT_SECTION);

			int submissionType = ((Integer) state.getAttribute(NEW_ASSIGNMENT_SUBMISSION_TYPE)).intValue();

			int gradeType = ((Integer) state.getAttribute(NEW_ASSIGNMENT_GRADE_TYPE)).intValue();

			String gradePoints = (String) state.getAttribute(NEW_ASSIGNMENT_GRADE_POINTS);

			String description = (String) state.getAttribute(NEW_ASSIGNMENT_DESCRIPTION);

			String checkAddDueTime = state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE)!=null?(String) state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE):null;

			String checkAutoAnnounce = (String) state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE);

			String checkAddHonorPledge = (String) state.getAttribute(NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE);

			String addtoGradebook = state.getAttribute(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK) != null?(String) state.getAttribute(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK):"" ;

			long category = state.getAttribute(NEW_ASSIGNMENT_CATEGORY) != null ? ((Long) state.getAttribute(NEW_ASSIGNMENT_CATEGORY)).longValue() : -1;
			
			String associateGradebookAssignment = (String) state.getAttribute(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
			
			String allowResubmitNumber = state.getAttribute(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER) != null?(String) state.getAttribute(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER):null;
			if (ac != null && ac.getTypeOfSubmission() == Assignment.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION)
			{
				// resubmit option is not allowed for non-electronic type
				allowResubmitNumber = null;
			}
			
			boolean useReviewService = "true".equalsIgnoreCase((String) state.getAttribute(NEW_ASSIGNMENT_USE_REVIEW_SERVICE));
			
			boolean allowStudentViewReport = "true".equalsIgnoreCase((String) state.getAttribute(NEW_ASSIGNMENT_ALLOW_STUDENT_VIEW));
			
			String submitReviewRepo = (String) state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_RADIO);
			String generateOriginalityReport = (String) state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_RADIO);
			boolean checkTurnitin = "true".equalsIgnoreCase((String) state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_TURNITIN));
			boolean checkInternet = "true".equalsIgnoreCase((String) state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INTERNET));
			boolean checkPublications = "true".equalsIgnoreCase((String) state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_PUB));
			boolean checkInstitution = "true".equalsIgnoreCase((String) state.getAttribute(NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INSTITUTION));
			
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
						groups.add(site.getGroup(groupId));
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
				
				// commit the changes to AssignmentContent object
				commitAssignmentContentEdit(state, ac, title, submissionType,useReviewService,allowStudentViewReport, gradeType, gradePoints, description, checkAddHonorPledge, attachments, submitReviewRepo, generateOriginalityReport, checkTurnitin, checkInternet, checkPublications, checkInstitution, openTime, dueTime, closeTime);
				
				// set the Assignment Properties object
				ResourcePropertiesEdit aPropertiesEdit = a.getPropertiesEdit();
				oAssociateGradebookAssignment = aPropertiesEdit.getProperty(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
				Time resubmitCloseTime = getTimeFromState(state, ALLOW_RESUBMIT_CLOSEMONTH, ALLOW_RESUBMIT_CLOSEDAY, ALLOW_RESUBMIT_CLOSEYEAR, ALLOW_RESUBMIT_CLOSEHOUR, ALLOW_RESUBMIT_CLOSEMIN, ALLOW_RESUBMIT_CLOSEAMPM);
				editAssignmentProperties(a, checkAddDueTime, checkAutoAnnounce, addtoGradebook, associateGradebookAssignment, allowResubmitNumber, aPropertiesEdit, post, resubmitCloseTime);
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
				
				// comment the changes to Assignment object
				commitAssignmentEdit(state, post, ac, a, title, openTime, dueTime, closeTime, enableCloseDate, section, range, groups);

				if (post)
				{
					// we need to update the submission
					if (bool_change_from_non_electronic || bool_change_from_non_point || bool_change_resubmit_option)
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
									if (bool_change_from_non_electronic)
									{
										sEdit.setSubmitted(false);
										sEdit.setTimeSubmitted(null);
									}
									else if (bool_change_from_non_point)
									{
										// set the grade to be empty for now
										sEdit.setGrade("");
										sEdit.setGraded(false);
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
						integrateWithAnnouncement(state, aOldTitle, a, title, openTime, checkAutoAnnounce, oldOpenTime);
	
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
				m_assignmentSupplementItemService.removeModelAnswer(mAnswer);
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
				m_assignmentSupplementItemService.removeAllPurposeItem(nAllPurpose);
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
				Time releaseTime = getTimeFromState(state, ALLPURPOSE_RELEASE_MONTH, ALLPURPOSE_RELEASE_DAY, ALLPURPOSE_RELEASE_YEAR, ALLPURPOSE_RELEASE_HOUR, ALLPURPOSE_RELEASE_MIN, ALLPURPOSE_RELEASE_AMPM);
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
				Time retractTime = getTimeFromState(state, ALLPURPOSE_RETRACT_MONTH, ALLPURPOSE_RETRACT_DAY, ALLPURPOSE_RETRACT_YEAR, ALLPURPOSE_RETRACT_HOUR, ALLPURPOSE_RETRACT_MIN, ALLPURPOSE_RETRACT_AMPM);
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
					AuthzGroup realm = AuthzGroupService.getAuthzGroup(SiteService.siteReference(siteId));
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
	private boolean change_from_non_electronic(SessionState state, String assignmentId, String assignmentContentId, AssignmentContentEdit ac) 
	{
		// whether this is an editing which changes non-electronic assignment to any other type?
		if (StringUtils.trimToNull(assignmentId) != null && StringUtils.trimToNull(assignmentContentId) != null)
		{
			// editing
			if (ac.getTypeOfSubmission() == Assignment.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION
					&& ((Integer) state.getAttribute(NEW_ASSIGNMENT_SUBMISSION_TYPE)).intValue() != Assignment.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION)
			{
				// changing from non-electronic type
				return true;
			}
		}
		return false;
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
					submission.setTimeSubmitted(TimeService.newTime());
					submission.setSubmitted(true);
					submission.setAssignment(a);
					AssignmentService.commitEdit(submission);
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
				List submitterIds = submission.getSubmitterIds();
				if (submitterIds != null && submitterIds.size() > 0 && userId.equals((String) submitterIds.get(0)))
				{
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
			String gradebookUid = ToolManager.getInstance().getCurrentPlacement().getContext();
			
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
								// if the old assoicated assignment entry in GB is an external one, but doesn't have anything assoicated with it in Assignment tool, remove it
								removeNonAssociatedExternalGradebookEntry(context, a.getReference(), oAssociateGradebookAssignment,gExternal, gradebookUid);
							}
						}
						catch (NumberFormatException nE)
						{
							alertInvalidPoint(state, gradePoints);
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

	private void integrateWithAnnouncement(SessionState state, String aOldTitle, AssignmentEdit a, String title, Time openTime, String checkAutoAnnounce, Time oldOpenTime) 
	{
		if (checkAutoAnnounce.equalsIgnoreCase(Boolean.TRUE.toString()))
		{
			AnnouncementChannel channel = (AnnouncementChannel) state.getAttribute(ANNOUNCEMENT_CHANNEL);
			if (channel != null)
			{
				// whether the assignment's title or open date has been updated
				boolean updatedTitle = false;
				boolean updatedOpenDate = false;
				
				String openDateAnnounced = StringUtils.trimToNull(a.getProperties().getProperty(NEW_ASSIGNMENT_OPEN_DATE_ANNOUNCED));
				String openDateAnnouncementId = StringUtils.trimToNull(a.getPropertiesEdit().getProperty(ResourceProperties.PROP_ASSIGNMENT_OPENDATE_ANNOUNCEMENT_MESSAGE_ID));
				if (openDateAnnounced != null && openDateAnnouncementId != null)
				{
					try
					{
						AnnouncementMessage message = channel.getAnnouncementMessage(openDateAnnouncementId);
						if (!message.getAnnouncementHeader().getSubject().contains(title))/*whether title has been changed*/
						{
							updatedTitle = true;
						}
						if (!message.getBody().contains(openTime.toStringLocalFull())) /*whether open date has been changed*/
						{
							updatedOpenDate = true;
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
				}

				// need to create announcement message if assignment is added or assignment has been updated
				if (openDateAnnounced == null || updatedTitle || updatedOpenDate)
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
		
		
							channel.commitMessage(message, m_notificationService.NOTI_NONE);
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
		Calendar c = (Calendar) state.getAttribute(CALENDAR);
		if (c != null)
		{
			String dueDateScheduled = a.getProperties().getProperty(NEW_ASSIGNMENT_DUE_DATE_SCHEDULED);
			String oldEventId = aPropertiesEdit.getProperty(ResourceProperties.PROP_ASSIGNMENT_DUEDATE_CALENDAR_EVENT_ID);
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
						M_log.warn(this + ":integrateWithCalender The old event has been deleted: event id=" + oldEventId + ". ");
					}
					catch (PermissionException ee)
					{
						M_log.warn(this + ":integrateWithCalender You do not have the permission to view the schedule event id= "
								+ oldEventId + ".");
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

				if (found)
				{
					// remove the founded old event
					try
					{
						c.removeEvent(c.getEditEvent(e.getId(), CalendarService.EVENT_REMOVE_CALENDAR));
					}
					catch (PermissionException ee)
					{
						M_log.warn(this + ":integrateWithCalender " + rb.getFormattedMessage("cannotrem", new Object[]{title}));
					}
					catch (InUseException ee)
					{
						M_log.warn(this + ":integrateWithCalender " + rb.getString("somelsis_calendar"));
					}
					catch (IdUnusedException ee)
					{
						M_log.warn(this + ":integrateWithCalender " + rb.getFormattedMessage("cannotfin6", new Object[]{e.getId()}));
					}
				}
			}

			if (checkAddDueTime.equalsIgnoreCase(Boolean.TRUE.toString()))
			{
				// commit related properties into Assignment object
				AssignmentEdit aEdit = editAssignment(a.getReference(), "integrateWithCalendar", state, false);
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
								eGroups.add(site.getGroup(groupRef));
							}
						}
						e = c.addEvent(/* TimeRange */TimeService.newTimeRange(dueTime.getTime(), /* 0 duration */0 * 60 * 1000),
								/* title */rb.getString("gen.due") + " " + title,
								/* description */rb.getFormattedMessage("assign_due_event_desc", new Object[]{title, dueTime.toStringLocalFull()}),
								/* type */rb.getString("deadl"),
								/* location */"",
								/* access */ eAccess,
								/* groups */ eGroups,
								/* attachments */EntityManager.newReferenceList());

						aEdit.getProperties().addProperty(NEW_ASSIGNMENT_DUE_DATE_SCHEDULED, Boolean.TRUE.toString());
						if (e != null)
						{
							aEdit.getProperties().addProperty(ResourceProperties.PROP_ASSIGNMENT_DUEDATE_CALENDAR_EVENT_ID, e.getId());

		                     // edit the calendar ojbject and add an assignment id field
	                        CalendarEventEdit edit = c.getEditEvent(e.getId(), org.sakaiproject.calendar.api.CalendarService.EVENT_ADD_CALENDAR);
	                                
	                        edit.setField(AssignmentConstants.NEW_ASSIGNMENT_DUEDATE_CALENDAR_ASSIGNMENT_ID, a.getId());
	                        
	                        c.commitEvent(edit);
						}
						// TODO do we care if the event is null?
						
					}
					catch (IdUnusedException ee)
					{
						M_log.warn(this + ":integrateWithCalender " + ee.getMessage());
					}
					catch (PermissionException ee)
					{
						M_log.warn(this + ":integrateWithCalender " + rb.getString("cannotfin1"));
					}
					catch (Exception ee)
					{
						M_log.warn(this + ":integrateWithCalender " + ee.getMessage());
					}
					// try-catch


					AssignmentService.commitEdit(aEdit);
				}
			} // if
		}
	}

	private void commitAssignmentEdit(SessionState state, boolean post, AssignmentContentEdit ac, AssignmentEdit a, String title, Time openTime, Time dueTime, Time closeTime, boolean enableCloseDate, String s, String range, Collection groups) 
	{
		a.setTitle(title);
		a.setContent(ac);
		a.setContext((String) state.getAttribute(STATE_CONTEXT_STRING));
		a.setSection(s);
		a.setOpenTime(openTime);
		a.setDueTime(dueTime);
		// set the drop dead date as the due date
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

		// post the assignment
		a.setDraft(!post);

		try
		{
			if ("site".equals(range))
			{
				a.setAccess(Assignment.AssignmentAccess.SITE);
				a.clearGroupAccess();
			}
			else if ("groups".equals(range))
			{
				a.setGroupAccess(groups);
			}
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getString("youarenot1"));
			M_log.warn(this + ":commitAssignmentEdit " + rb.getString("youarenot1") + e.getMessage());
		}

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			// commit assignment first
			AssignmentService.commitEdit(a);
		}
	}

	private void editAssignmentProperties(AssignmentEdit a, String checkAddDueTime, String checkAutoAnnounce, String addtoGradebook, String associateGradebookAssignment, String allowResubmitNumber, ResourcePropertiesEdit aPropertiesEdit, boolean post, Time closeTime) 
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

	private void commitAssignmentContentEdit(SessionState state, AssignmentContentEdit ac, String title, int submissionType,boolean useReviewService, boolean allowStudentViewReport, int gradeType, String gradePoints, String description, String checkAddHonorPledge, List attachments, String submitReviewRepo, String generateOriginalityReport, boolean checkTurnitin, boolean checkInternet, boolean checkPublications, boolean checkInstitution, Time openTime, Time dueTime, Time closeTime) 
	{
		ac.setTitle(title);
		ac.setInstructions(description);
		ac.setHonorPledge(Integer.parseInt(checkAddHonorPledge));
		ac.setTypeOfSubmission(submissionType);
		ac.setAllowReviewService(useReviewService);
		ac.setAllowStudentViewReport(allowStudentViewReport);
		ac.setSubmitReviewRepo(submitReviewRepo);
		ac.setGenerateOriginalityReport(generateOriginalityReport);
		ac.setCheckInstitution(checkInstitution);
		ac.setCheckInternet(checkInternet);
		ac.setCheckPublications(checkPublications);
		ac.setCheckTurnitin(checkTurnitin);
		ac.setTypeOfGrade(gradeType);
		if (gradeType == 3)
		{
			try
			{
				ac.setMaxGradePoint(Integer.parseInt(gradePoints));
			}
			catch (NumberFormatException e)
			{
				alertInvalidPoint(state, gradePoints);
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
			createTIIAssignment(ac, openTime, dueTime, closeTime, state);
		}
		
	}
	
	public void createTIIAssignment(AssignmentContentEdit assign, Time openTime, Time dueTime, Time closeTime, SessionState state) {
        Map opts = new HashMap();
        
        opts.put("submit_papers_to", assign.getSubmitReviewRepo());
        opts.put("report_gen_speed", assign.getGenerateOriginalityReport());
        opts.put("institution_check", assign.isCheckInstitution() ? "1" : "0");
        opts.put("internet_check", assign.isCheckInternet() ? "1" : "0");
        opts.put("journal_check", assign.isCheckPublications() ? "1" : "0");
        opts.put("s_paper_check", assign.isCheckTurnitin() ? "1" : "0");        
        opts.put("s_view_report", assign.getAllowStudentViewReport() ? "1" : "0");        
        opts.put("late_accept_flag", "1");
        
        SimpleDateFormat dform = ((SimpleDateFormat) DateFormat.getDateInstance());
        dform.applyPattern("yyyy-MM-dd HH:mm:ss");
        opts.put("dtstart", dform.format(openTime.getTime()));
        opts.put("dtdue", dform.format(dueTime.getTime()));
        //opts.put("dtpost", dform.format(closeTime.getTime()));       
        try {
            contentReviewService.createAssignment(assign.getContext(), assign.getReference(), opts);
        } catch (Exception e) {
            M_log.error(e);
            state.setAttribute("alertMessage", rb.getString("content_review.error.createAssignment"));
        }
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
		
		// temporarily allow the user to read and write from assignments (asn.revise permission)
        enableSecurityAdvisor();
        
        while (it.hasNext()) // reads and writes the parameter for default ordering
        {
            Assignment a = (Assignment) it.next();
            String assignmentid = a.getId();
            String assignmentposition = params.getString("position_" + assignmentid);
            AssignmentEdit ae = editAssignment(assignmentid, "reorderAssignments", state, true);
            if (ae != null)
            {
	            ae.setPosition_order(Long.valueOf(assignmentposition).intValue());
	            AssignmentService.commitEdit(ae);
            }
        }
        
        // clear the permission
        disableSecurityAdvisor();
		
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
	 * @param ampmString
	 * @return
	 */
	private Time getTimeFromState(SessionState state, String monthString, String dayString, String yearString, String hourString, String minString, String ampmString) 
	{
		if (state.getAttribute(monthString) != null ||
			state.getAttribute(dayString) != null ||
			state.getAttribute(yearString) != null ||
			state.getAttribute(hourString) != null ||
			state.getAttribute(minString) != null ||
			state.getAttribute(ampmString) != null)
		{
			int month = ((Integer) state.getAttribute(monthString)).intValue();
			int day = ((Integer) state.getAttribute(dayString)).intValue();
			int year = ((Integer) state.getAttribute(yearString)).intValue();
			int hour = ((Integer) state.getAttribute(hourString)).intValue();
			int min = ((Integer) state.getAttribute(minString)).intValue();
			String ampm = (String) state.getAttribute(ampmString);
			if (("PM".equals(ampm)) && (hour != 12))
			{
				hour = hour + 12;
			}
			if ((hour == 12) && ("AM".equals(ampm)))
			{
				hour = 0;
			}
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
		post_save_assignment(data, "save");

	} // doSave_assignment
	
	/**
	 * Action is to reorder assignments
	 */
	public void doReorder_assignment(RunData data)
	{
		reorderAssignments(data);
	} // doReorder_assignments

	/**
	 * Action is to preview the selected assignment
	 */
	public void doPreview_assignment(RunData data)
	{
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
			// whether the user can modify the assignment
			state.setAttribute(EDIT_ASSIGNMENT_ID, assignmentId);
	
			Assignment a = getAssignment(assignmentId, "doEdit_assignment", state);
			if (a != null)
			{
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
				
				putTimePropertiesInState(state, a.getOpenTime(), NEW_ASSIGNMENT_OPENMONTH, NEW_ASSIGNMENT_OPENDAY, NEW_ASSIGNMENT_OPENYEAR, NEW_ASSIGNMENT_OPENHOUR, NEW_ASSIGNMENT_OPENMIN, NEW_ASSIGNMENT_OPENAMPM);
				// generate alert when editing an assignment past open date
				if (a.getOpenTime().before(TimeService.newTime()))
				{
					addAlert(state, rb.getString("youarenot20"));
				}
	
				putTimePropertiesInState(state, a.getDueTime(), NEW_ASSIGNMENT_DUEMONTH, NEW_ASSIGNMENT_DUEDAY, NEW_ASSIGNMENT_DUEYEAR, NEW_ASSIGNMENT_DUEHOUR, NEW_ASSIGNMENT_DUEMIN, NEW_ASSIGNMENT_DUEAMPM);
				// generate alert when editing an assignment past due date
				if (a.getDueTime().before(TimeService.newTime()))
				{
					addAlert(state, rb.getString("youarenot17"));
				}
	
				if (a.getCloseTime() != null)
				{
					state.setAttribute(NEW_ASSIGNMENT_ENABLECLOSEDATE, Boolean.valueOf(true));
					putTimePropertiesInState(state, a.getCloseTime(), NEW_ASSIGNMENT_CLOSEMONTH, NEW_ASSIGNMENT_CLOSEDAY, NEW_ASSIGNMENT_CLOSEYEAR, NEW_ASSIGNMENT_CLOSEHOUR, NEW_ASSIGNMENT_CLOSEMIN, NEW_ASSIGNMENT_CLOSEAMPM);
				}
				else
				{
					state.setAttribute(NEW_ASSIGNMENT_ENABLECLOSEDATE, Boolean.valueOf(false));
					state.setAttribute(NEW_ASSIGNMENT_CLOSEMONTH, state.getAttribute(NEW_ASSIGNMENT_DUEMONTH));
					state.setAttribute(NEW_ASSIGNMENT_CLOSEDAY, state.getAttribute(NEW_ASSIGNMENT_DUEDAY));
					state.setAttribute(NEW_ASSIGNMENT_CLOSEYEAR, state.getAttribute(NEW_ASSIGNMENT_DUEYEAR));
					state.setAttribute(NEW_ASSIGNMENT_CLOSEHOUR, state.getAttribute(NEW_ASSIGNMENT_DUEHOUR));
					state.setAttribute(NEW_ASSIGNMENT_CLOSEMIN, state.getAttribute(NEW_ASSIGNMENT_DUEMIN));
					state.setAttribute(NEW_ASSIGNMENT_CLOSEAMPM, state.getAttribute(NEW_ASSIGNMENT_DUEAMPM));
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
				
				ResourceProperties properties = a.getProperties();
				state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE, properties.getProperty(
						ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE));
				state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE, properties.getProperty(
						ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE));
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
				
				// put the resubmission option into state
				assignment_resubmission_option_into_state(a, null, state);
				
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
				
				
				
				state.setAttribute(NEW_ASSIGNMENT_GROUPS, a.getGroups());
				
				// get all supplement item info into state
				setAssignmentSupplementItemInState(state, a);
			
			}
	
			state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_NEW_EDIT_ASSIGNMENT);
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
            submissionRepoSettings.add(NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_INSITUTION);
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
		putTimePropertiesInState(state, releaseTime, ALLPURPOSE_RELEASE_MONTH, ALLPURPOSE_RELEASE_DAY, ALLPURPOSE_RELEASE_YEAR, ALLPURPOSE_RELEASE_HOUR, ALLPURPOSE_RELEASE_MIN, ALLPURPOSE_RELEASE_AMPM);
		
		putTimePropertiesInState(state, retractTime, ALLPURPOSE_RETRACT_MONTH, ALLPURPOSE_RETRACT_DAY, ALLPURPOSE_RETRACT_YEAR, ALLPURPOSE_RETRACT_HOUR, ALLPURPOSE_RETRACT_MIN, ALLPURPOSE_RETRACT_AMPM);
	}

	/**
	 * Action is to show the delete assigment confirmation screen
	 */
	public void doDelete_confirm_assignment(RunData data)
	{
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
				
				// we use to check "assignment.delete.cascade.submission" setting. But the implementation now is always remove submission objects when the assignment is removed.
				// delete assignment and its submissions altogether
				deleteAssignmentObjects(state, aEdit, true);

				// remove from Gradebook
				integrateGradebook(state, (String) ids.get (i), associateGradebookAssignment, "remove", null, null, -1, null, null, null, -1);
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
						addAlert(state, rb.getFormattedMessage("youarenot_removeSubmission", new Object[]{s.getReference()}));

						M_log.warn(this + ":deleteAssignmentObjects " + eee.getMessage() + " " + s.getReference());
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
			if (c != null)
			{
				// already has calendar object
				// get the old event
				CalendarEvent e = null;
				boolean found = false;
				String oldEventId = pEdit.getProperty(ResourceProperties.PROP_ASSIGNMENT_DUEDATE_CALENDAR_EVENT_ID);
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
						M_log.warn(this + ":removeCalendarEvent " + ee.getMessage());
					}
					catch (PermissionException ee)
					{
						M_log.warn(this + ":removeCalendarEvent " + ee.getMessage());
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
				// remove the founded old event
				if (found)
				{
					// found the old event delete it
					try
					{
						c.removeEvent(c.getEditEvent(e.getId(), CalendarService.EVENT_REMOVE_CALENDAR));
						pEdit.removeProperty(NEW_ASSIGNMENT_DUE_DATE_SCHEDULED);
						pEdit.removeProperty(ResourceProperties.PROP_ASSIGNMENT_DUEDATE_CALENDAR_EVENT_ID);
					}
					catch (PermissionException ee)
					{
						M_log.warn(this + ":removeCalendarEvent " + rb.getFormattedMessage("cannotrem", new Object[]{title}));
					}
					catch (InUseException ee)
					{
						M_log.warn(this + ":removeCalendarEvent " + rb.getString("somelsis_calendar"));
					}
					catch (IdUnusedException ee)
					{
						M_log.warn(this + ":removeCalendarEvent " + rb.getFormattedMessage("cannotfin6", new Object[]{e.getId()}));
					}
				}
			}
		}
	}

	/**
	 * Action is to delete the assignment and also the related AssignmentSubmission
	 */
	public void doDeep_delete_assignment(RunData data)
	{
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
		// put submission information into state
		putSubmissionInfoIntoState(state, assignmentId, submissionId);

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
	 */
	private void putSubmissionInfoIntoState(SessionState state, String assignmentId, String submissionId)
	{
		// reset grading submission variables
		resetGradeSubmission(state);
		
		// reset the grade assignment id and submission id
		state.setAttribute(GRADE_SUBMISSION_ASSIGNMENT_ID, assignmentId);
		state.setAttribute(GRADE_SUBMISSION_SUBMISSION_ID, submissionId);
		
		// allow resubmit number
		String allowResubmitNumber = "0";
		Assignment a = getAssignment(assignmentId, "putSubmissionInfoIntoState", state);
		if (a != null)
		{
			AssignmentSubmission s = getSubmission(submissionId, "putSubmissionInfoIntoState", state);
			if (s != null)
			{
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
				if (s.getGraded())
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
		state.removeAttribute(SHOW_ALLOW_RESUBMISSION);

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

		state.setAttribute(VIEW_GRADE_SUBMISSION_ID, params.getString("submissionId"));
		
		// whether the user can access the Submission object
		if (getSubmission((String) state.getAttribute(VIEW_GRADE_SUBMISSION_ID), "doView_grade", state ) != null)
		{
			state.setAttribute(STATE_MODE, MODE_STUDENT_VIEW_GRADE);
		}

	} // doView_grade
	
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

		Log.debug("chef", "doAssignment_form(): actualGradeSubmissionId = " + actualGradeSubmissionId);
		
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
			}
			else if ("show_instruction".equals(option))
			{
				// show the assignment instruction
				doShow_submission_assignment_instruction(data);
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
		Log.debug("chef", "checkSubmissionStateConsistency(): stateGradeSubmissionId = " + stateGradeSubmissionId);
		boolean is_good = stateGradeSubmissionId.equals(actualGradeSubmissionId);
		if (!is_good) {
		    Log.warn("chef", "checkSubissionStateConsistency(): State is inconsistent! Aborting grade save.");
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

		String mode = (String) state.getAttribute(STATE_MODE);
		if (MODE_STUDENT_VIEW_SUBMISSION.equals(mode))
		{
			// save the current input before leaving the page
			saveSubmitInputs(state, params);
			
			// Restrict file picker configuration if using content-review (Turnitin):
			String assignmentRef = (String) state.getAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE);
			try
			{
				Assignment assignment = AssignmentService.getAssignment(assignmentRef);
				if (assignment.getContent().getAllowReviewService())
				{
					state.setAttribute(FilePickerHelper.FILE_PICKER_MAX_ATTACHMENTS, FilePickerHelper.CARDINALITY_SINGLE);
					state.setAttribute(FilePickerHelper.FILE_PICKER_SHOW_URL, Boolean.FALSE);
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
		}
		else if (MODE_INSTRUCTOR_NEW_EDIT_ASSIGNMENT.equals(mode))
		{
			setNewAssignmentParameters(data, false);
		}
		else if (MODE_INSTRUCTOR_GRADE_SUBMISSION.equals(mode))
		{
			readGradeForm(data, state, "read");
		}

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			// get into helper mode with this helper tool
			startHelper(data.getRequest(), "sakai.filepicker");

			state.setAttribute(FilePickerHelper.FILE_PICKER_TITLE_TEXT, rb.getString("gen.addatttoassig"));
			state.setAttribute(FilePickerHelper.FILE_PICKER_INSTRUCTION_TEXT, rb.getString("gen.addatttoassiginstr"));

			// use the real attachment list
			state.setAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS, state.getAttribute(ATTACHMENTS));
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
				typeOfGrade = a.getContent().getTypeOfGrade();
	
				if (withGrade)
				{
					// any change in grade. Do not check for ungraded assignment type
					if (!hasChange && typeOfGrade != Assignment.UNGRADED_GRADE_TYPE)
					{
						if (typeOfGrade == Assignment.SCORE_GRADE_TYPE)
						{
							String currentGrade = submission.getGrade();
							
							NumberFormat nbFormat = (DecimalFormat) getNumberFormat();
							DecimalFormat dcFormat = (DecimalFormat) nbFormat;
							String decSeparator = dcFormat.getDecimalFormatSymbols().getDecimalSeparator() + "";
							
							if (currentGrade != null && currentGrade.indexOf(decSeparator) != -1)
							{
								currentGrade =  scalePointGrade(state, submission.getGrade());
							}
							hasChange = valueDiffFromStateAttribute(state, scalePointGrade(state, g), currentGrade);
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
							// the preview grade process might already scaled up the grade by 10
							if (!((String) state.getAttribute(STATE_MODE)).equals(MODE_INSTRUCTOR_PREVIEW_GRADE_SUBMISSION))
							{
								validPointGrade(state, grade);
								
								if (state.getAttribute(STATE_MESSAGE) == null)
								{
									int maxGrade = a.getContent().getMaxGradePoint();
									try
									{
										if (Integer.parseInt(scalePointGrade(state, grade)) > maxGrade)
										{
											if (state.getAttribute(GRADE_GREATER_THAN_MAX_ALERT) == null)
											{
												// alert user first when he enters grade bigger than max scale
												addAlert(state, rb.getFormattedMessage("grad2", new Object[]{grade, displayGrade(state, String.valueOf(maxGrade))}));
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
										alertInvalidPoint(state, grade);
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
			}
		
			if (state.getAttribute(STATE_MESSAGE) == null)
			{
				String grade = (String) state.getAttribute(GRADE_SUBMISSION_GRADE);
				grade = (typeOfGrade == Assignment.SCORE_GRADE_TYPE)?scalePointGrade(state, grade):grade;
				state.setAttribute(GRADE_SUBMISSION_GRADE, grade);
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
				|| oldValue != null && value != null && !oldValue.equals(value))
		{
			rv = true;
		}
		return rv;
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
			String closeAMPM = params.getString(ALLOW_RESUBMIT_CLOSEAMPM);
			state.setAttribute(ALLOW_RESUBMIT_CLOSEAMPM, closeAMPM);
			if (("PM".equals(closeAMPM)) && (closeHour != 12))
			{
				closeHour = closeHour + 12;
			}
			if ((closeHour == 12) && ("AM".equals(closeAMPM)))
			{
				closeHour = 0;
			}
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
		state.setAttribute(ALLOW_RESUBMIT_CLOSEAMPM,state.getAttribute(NEW_ASSIGNMENT_DUEAMPM));
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
			if (!siteHasTool(siteId, "sakai.schedule"))
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

					CalendarService cService = org.sakaiproject.calendar.cover.CalendarService.getInstance();
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
			state.setAttribute(STUDENT_LIST_SHOW_TABLE, new HashSet());
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
			state.setAttribute(NEW_ASSIGNMENT_YEAR_RANGE_FROM, Integer.valueOf(2002));
		}
		
		if (state.getAttribute(NEW_ASSIGNMENT_YEAR_RANGE_TO) == null)
		{
			state.setAttribute(NEW_ASSIGNMENT_YEAR_RANGE_TO, Integer.valueOf(new Integer(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)+5)));
		}
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

		// set the open time to be 12:00 PM
		state.setAttribute(NEW_ASSIGNMENT_OPENMONTH, Integer.valueOf(month));
		state.setAttribute(NEW_ASSIGNMENT_OPENDAY, Integer.valueOf(day));
		state.setAttribute(NEW_ASSIGNMENT_OPENYEAR, Integer.valueOf(year));
		state.setAttribute(NEW_ASSIGNMENT_OPENHOUR, Integer.valueOf(12));
		state.setAttribute(NEW_ASSIGNMENT_OPENMIN, Integer.valueOf(0));
		state.setAttribute(NEW_ASSIGNMENT_OPENAMPM, "PM");
		
		// set the all purpose item release time
		state.setAttribute(ALLPURPOSE_RELEASE_MONTH, Integer.valueOf(month));
		state.setAttribute(ALLPURPOSE_RELEASE_DAY, Integer.valueOf(day));
		state.setAttribute(ALLPURPOSE_RELEASE_YEAR, Integer.valueOf(year));
		state.setAttribute(ALLPURPOSE_RELEASE_HOUR, Integer.valueOf(12));
		state.setAttribute(ALLPURPOSE_RELEASE_MIN, Integer.valueOf(0));
		state.setAttribute(ALLPURPOSE_RELEASE_AMPM, "PM");

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
		state.setAttribute(NEW_ASSIGNMENT_DUEHOUR, Integer.valueOf(5));
		state.setAttribute(NEW_ASSIGNMENT_DUEMIN, Integer.valueOf(0));
		state.setAttribute(NEW_ASSIGNMENT_DUEAMPM, "PM");
		
		// set the resubmit time to be the same as due time
		state.setAttribute(ALLOW_RESUBMIT_CLOSEMONTH, Integer.valueOf(month));
		state.setAttribute(ALLOW_RESUBMIT_CLOSEDAY, Integer.valueOf(day));
		state.setAttribute(ALLOW_RESUBMIT_CLOSEYEAR, Integer.valueOf(year));
		state.setAttribute(ALLOW_RESUBMIT_CLOSEHOUR, Integer.valueOf(5));
		state.setAttribute(ALLOW_RESUBMIT_CLOSEMIN, Integer.valueOf(0));
		state.setAttribute(ALLOW_RESUBMIT_CLOSEAMPM, "PM");
		state.setAttribute(AssignmentSubmission.ALLOW_RESUBMIT_NUMBER, Integer.valueOf(1));

		// enable the close date by default
		state.setAttribute(NEW_ASSIGNMENT_ENABLECLOSEDATE, Boolean.valueOf(true));
		// set the close time to be 5:00 pm, same as the due time by default
		state.setAttribute(NEW_ASSIGNMENT_CLOSEMONTH, Integer.valueOf(month));
		state.setAttribute(NEW_ASSIGNMENT_CLOSEDAY, Integer.valueOf(day));
		state.setAttribute(NEW_ASSIGNMENT_CLOSEYEAR, Integer.valueOf(year));
		state.setAttribute(NEW_ASSIGNMENT_CLOSEHOUR, Integer.valueOf(5));
		state.setAttribute(NEW_ASSIGNMENT_CLOSEMIN, Integer.valueOf(0));
		state.setAttribute(NEW_ASSIGNMENT_CLOSEAMPM, "PM");
		
		// set the all purpose retract time
		state.setAttribute(ALLPURPOSE_RETRACT_MONTH, Integer.valueOf(month));
		state.setAttribute(ALLPURPOSE_RETRACT_DAY, Integer.valueOf(day));
		state.setAttribute(ALLPURPOSE_RETRACT_YEAR, Integer.valueOf(year));
		state.setAttribute(ALLPURPOSE_RETRACT_HOUR, Integer.valueOf(5));
		state.setAttribute(ALLPURPOSE_RETRACT_MIN, Integer.valueOf(0));
		state.setAttribute(ALLPURPOSE_RETRACT_AMPM, "PM");

		state.setAttribute(NEW_ASSIGNMENT_SECTION, "001");
		state.setAttribute(NEW_ASSIGNMENT_SUBMISSION_TYPE, Integer.valueOf(Assignment.TEXT_AND_ATTACHMENT_ASSIGNMENT_SUBMISSION));
		state.setAttribute(NEW_ASSIGNMENT_GRADE_TYPE, Integer.valueOf(Assignment.UNGRADED_GRADE_TYPE));
		state.setAttribute(NEW_ASSIGNMENT_GRADE_POINTS, "");
		state.setAttribute(NEW_ASSIGNMENT_DESCRIPTION, "");
		state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE, Boolean.FALSE.toString());
		state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE, Boolean.FALSE.toString());
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
		state.removeAttribute(NEW_ASSIGNMENT_OPENAMPM);
		
		state.removeAttribute(ALLPURPOSE_RELEASE_MONTH);
		state.removeAttribute(ALLPURPOSE_RELEASE_DAY);
		state.removeAttribute(ALLPURPOSE_RELEASE_YEAR);
		state.removeAttribute(ALLPURPOSE_RELEASE_HOUR);
		state.removeAttribute(ALLPURPOSE_RELEASE_MIN);
		state.removeAttribute(ALLPURPOSE_RELEASE_AMPM);

		state.removeAttribute(NEW_ASSIGNMENT_DUEMONTH);
		state.removeAttribute(NEW_ASSIGNMENT_DUEDAY);
		state.removeAttribute(NEW_ASSIGNMENT_DUEYEAR);
		state.removeAttribute(NEW_ASSIGNMENT_DUEHOUR);
		state.removeAttribute(NEW_ASSIGNMENT_DUEMIN);
		state.removeAttribute(NEW_ASSIGNMENT_DUEAMPM);

		state.removeAttribute(NEW_ASSIGNMENT_ENABLECLOSEDATE);
		state.removeAttribute(NEW_ASSIGNMENT_CLOSEMONTH);
		state.removeAttribute(NEW_ASSIGNMENT_CLOSEDAY);
		state.removeAttribute(NEW_ASSIGNMENT_CLOSEYEAR);
		state.removeAttribute(NEW_ASSIGNMENT_CLOSEHOUR);
		state.removeAttribute(NEW_ASSIGNMENT_CLOSEMIN);
		state.removeAttribute(NEW_ASSIGNMENT_CLOSEAMPM);
		
		// set the all purpose retract time
		state.removeAttribute(ALLPURPOSE_RETRACT_MONTH);
		state.removeAttribute(ALLPURPOSE_RETRACT_DAY);
		state.removeAttribute(ALLPURPOSE_RETRACT_YEAR);
		state.removeAttribute(ALLPURPOSE_RETRACT_HOUR);
		state.removeAttribute(ALLPURPOSE_RETRACT_MIN);
		state.removeAttribute(ALLPURPOSE_RETRACT_AMPM);

		state.removeAttribute(NEW_ASSIGNMENT_SECTION);
		state.removeAttribute(NEW_ASSIGNMENT_SUBMISSION_TYPE);
		state.removeAttribute(NEW_ASSIGNMENT_GRADE_TYPE);
		state.removeAttribute(NEW_ASSIGNMENT_GRADE_POINTS);
		state.removeAttribute(NEW_ASSIGNMENT_DESCRIPTION);
		state.removeAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE);
		state.removeAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE);
		state.removeAttribute(NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE);
		state.removeAttribute(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK);
		state.removeAttribute(NEW_ASSIGNMENT_ATTACHMENT);
		state.removeAttribute(NEW_ASSIGNMENT_FOCUS);
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

		// remove content-review setting
		state.removeAttribute(NEW_ASSIGNMENT_USE_REVIEW_SERVICE);

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
	private HashMap gradeTypeTable()
	{
		HashMap n = new HashMap();
		n.put(Integer.valueOf(2), rb.getString("letter"));
		n.put(Integer.valueOf(3), rb.getString("points"));
		n.put(Integer.valueOf(4), rb.getString("pass"));
		n.put(Integer.valueOf(5), rb.getString("check"));
		n.put(Integer.valueOf(1), rb.getString("ungra"));
		return n;

	} // gradeTypeTable

	/**
	 * construct a HashMap using the integer as the key and submission type String as the value
	 */
	private HashMap submissionTypeTable()
	{
		HashMap n = new HashMap();
		n.put(Integer.valueOf(1), rb.getString("inlin"));
		n.put(Integer.valueOf(2), rb.getString("attaonly"));
		n.put(Integer.valueOf(3), rb.getString("inlinatt"));
		n.put(Integer.valueOf(4), rb.getString("nonelec"));
		n.put(Integer.valueOf(5), rb.getString("singleatt"));
		return n;

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
	 * the UserSubmission clas
	 */
	public class UserSubmission
	{
		/**
		 * the User object
		 */
		User m_user = null;

		/**
		 * the AssignmentSubmission object
		 */
		AssignmentSubmission m_submission = null;

		/**
		 * the AssignmentSubmission object
		 */
		User m_submittedBy = null;

		public UserSubmission(User u, AssignmentSubmission s)
		{
			m_user = u;
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
	}

	/**
	 * the AssignmentComparator clas
	 */
	private class AssignmentComparator implements Comparator
	{
		Collator collator = Collator.getInstance();
		
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
			m_state = state;
			m_criteria = criteria;
			m_asc = asc;

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
						rv = rv.concat(site.getGroup((String) k.next()).getTitle());
					}
				}
				catch (Exception ignore)
				{
					M_log.warn(this + ":getAssignmentRange" + ignore.getMessage());
				}
			}

			return rv;

		} // getAssignmentRange

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
						else if (t1.before(t2))
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
				UserSubmission u1 = (UserSubmission) o1;
				UserSubmission u2 = (UserSubmission) o2;
				if (u1 == null || u2 == null || u1.getUser() == null || u2.getUser() == null )
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
				UserSubmission u1 = (UserSubmission) o1;
				UserSubmission u2 = (UserSubmission) o2;

				if (u1 == null || u2 == null || u1.getUser() == null || u2.getUser() == null )
				{
					result = 1;
				}
				else
				{
					String lName1 = u1.getUser().getSortName();
					String lName2 = u2.getUser().getSortName();
					result = compareString(lName1, lName2);
				}
			}
			else if (m_criteria.equals(SORTED_GRADE_SUBMISSION_BY_SUBMIT_TIME))
			{
				// sorted by submission time
				UserSubmission u1 = (UserSubmission) o1;
				UserSubmission u2 = (UserSubmission) o2;

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
				UserSubmission u1 = (UserSubmission) o1;
				UserSubmission u2 = (UserSubmission) o2;

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
				UserSubmission u1 = (UserSubmission) o1;
				UserSubmission u2 = (UserSubmission) o2;

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
				UserSubmission u1 = (UserSubmission) o1;
				UserSubmission u2 = (UserSubmission) o2;

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
				User[] u1 = ((AssignmentSubmission) o1).getSubmitters();
				User[] u2 = ((AssignmentSubmission) o2).getSubmitters();

				if (u1 == null || u1.length == 0 || u2 == null || u2.length ==0)
				{
					return 1;
				}
				else
				{
					String submitters1 = "";
					String submitters2 = "";

					for (int j = 0; j < u1.length; j++)
					{
						if (u1[j] != null && u1[j].getSortName() != null)
						{
							if (j > 0)
							{
								submitters1 = submitters1.concat("; ");
							}
							submitters1 = submitters1.concat("" + u1[j].getSortName());
						}
					}

					for (int j = 0; j < u2.length; j++)
					{
						if (u2[j] != null && u2[j].getSortName() != null)
						{
							if (j > 0)
							{
								submitters2 = submitters2.concat("; ");
							}
							submitters2 = submitters2.concat(u2[j].getSortName());
						}
					}
					result = compareString(submitters1, submitters2);
				}
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
						if (openTime != null && currentTime.after(openTime) && !a.getDraft())
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
			List submissions = new ArrayList();
			
			List assignments = iterator_to_list(AssignmentService.getAssignmentsForContext(contextString));
			if (assignments.size() > 0)
			{
				// users = AssignmentService.allowAddSubmissionUsers (((Assignment)assignments.get(0)).getReference ());
			}

			try
			{
				// get the site object first
				Site site = SiteService.getSite(contextString);
				for (int j = 0; j < assignments.size(); j++)
				{
					Assignment a = (Assignment) assignments.get(j);
					
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
								if (s != null && (s.getSubmitted() || (s.getReturned() && (s.getTimeLastModified().before(s
													.getTimeReturned())))))
								{
									// has been subitted or has been returned and not work on it yet
									User[] submitters = s.getSubmitters();
									if (submitters != null && submitters.length > 0 && !allowGradeAssignmentUsers.contains(submitters[0]))
									{
										// find whether the submitter is still an active member of the site
										Member member = site.getMember(submitters[0].getId());
										if(member != null && member.isActive()) {
											// only include the active student submission
											submissions.add(s);
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
			}
			catch (IdUnusedException idUnusedException)
			{
				M_log.warn(this + ":sizeResources " + idUnusedException.getMessage() + " site id=" + contextString);
			}

			returnResources = submissions;
		}
		else if (MODE_INSTRUCTOR_GRADE_ASSIGNMENT.equals(mode))
		{
			initViewSubmissionListOption(state);
			String allOrOneGroup = (String) state.getAttribute(VIEW_SUBMISSION_LIST_OPTION);
			String search = (String) state.getAttribute(VIEW_SUBMISSION_SEARCH);
			String aRef = (String) state.getAttribute(EXPORT_ASSIGNMENT_REF);
			Boolean searchFilterOnly = (state.getAttribute(SUBMISSIONS_SEARCH_ONLY) != null && ((Boolean) state.getAttribute(SUBMISSIONS_SEARCH_ONLY)) ? Boolean.TRUE:Boolean.FALSE);
			
			List<String> submitterIds = AssignmentService.getSubmitterIdList(searchFilterOnly.toString(), allOrOneGroup, search, aRef, contextString);

			// construct the user-submission list
			if (submitterIds != null && !submitterIds.isEmpty())
			{
				for (Iterator<String> iSubmitterIdsIterator = submitterIds.iterator(); iSubmitterIdsIterator.hasNext();)
				{
					String uId = iSubmitterIdsIterator.next();
					try
					{
						User u = UserDirectoryService.getUser(uId);
	
						try
						{
							AssignmentSubmission sub = AssignmentService.getSubmission(aRef, u);
							UserSubmission us = new UserSubmission(u, sub);
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
						catch (IdUnusedException subIdException)
						{
							M_log.warn(this + ".sizeResources: looking for submission for unused assignment id " + aRef + subIdException.getMessage());
						}
						catch (PermissionException subPerException)
						{
							M_log.warn(this + ".sizeResources: cannot have permission to access submission of assignment " + aRef + " of user " + u.getId());
						}
					}
					catch (UserNotDefinedException e)
					{
						M_log.warn(this + ":sizeResources cannot find user id=" + uId + e.getMessage() + "");
					}
				}
			}
		}

		// sort them all
		String ascending = "true";
		String sort = "";
		ascending = (String) state.getAttribute(SORTED_ASC);
		sort = (String) state.getAttribute(SORTED_BY);
		if (MODE_INSTRUCTOR_GRADE_ASSIGNMENT.equals(mode) && (sort == null || !sort.startsWith("sorted_grade_submission_by")))
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
			try
			{
				Collections.sort(returnResources, new AssignmentComparator(state, sort, ascending));
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
	private String validPointGrade(SessionState state, String grade)
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
				NumberFormat nbFormat = (DecimalFormat) getNumberFormat();
				DecimalFormat dcFormat = (DecimalFormat) nbFormat;
				String decSeparator = dcFormat.getDecimalFormatSymbols().getDecimalSeparator() + "";
				
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
					// when there is decimal points inside the grade, scale the number by 10
					// but only one decimal place is supported
					// for example, change 100.0 to 1000
					if (!grade.equals(decSeparator))
					{
						if (grade.length() > index + 2)
						{
							// if there are more than one decimal point
							addAlert(state, rb.getString("plesuse2"));
						}
						else
						{
							// decimal points is the only allowed character inside grade
							// replace it with '1', and try to parse the new String into int
							String gradeString = grade.endsWith(decSeparator) ? grade.substring(0, index).concat("0") : grade.substring(0,
									index).concat(grade.substring(index + 1));
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
									alertInvalidPoint(state, gradeString);
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
					String gradeString = grade + "0";
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
							alertInvalidPoint(state, gradeString);
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
	 * get the right number format based on local
	 * @return
	 */
	private NumberFormat getNumberFormat() {
		// get localized number format
		NumberFormat nbFormat = NumberFormat.getInstance();				
		try {
			Locale locale = null;
			ResourceLoader rb = new ResourceLoader();
			locale = rb.getLocale();
			nbFormat = NumberFormat.getNumberInstance(locale);
		}				
		catch (Exception e) {
			M_log.warn("Error while retrieving local number format, using default ", e);
		}
		return nbFormat;
	} // validPointGrade
	
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
				addAlert(state, rb.getString("plesuse0"));
			}
		}
	}

	private void alertInvalidPoint(SessionState state, String grade)
	{
		NumberFormat nbFormat = (DecimalFormat) getNumberFormat();
		DecimalFormat dcFormat = (DecimalFormat) nbFormat;
		String decSeparator = dcFormat.getDecimalFormatSymbols().getDecimalSeparator() + "";
		
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
			int maxInt = Integer.MAX_VALUE / 10;
			int maxDec = Integer.MAX_VALUE - maxInt * 10;
			// case 2: Due to our internal scaling, input String is larger than Integer.MAX_VALUE/10
			addAlert(state, rb.getFormattedMessage("plesuse4", new Object[]{grade.substring(0, grade.length()-1) + decSeparator + grade.substring(grade.length()-1),  maxInt + decSeparator + maxDec}));
		}
	}

	/**
	 * display grade properly
	 */
	private String displayGrade(SessionState state, String grade)
	{
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			if (grade != null && (grade.length() >= 1))
			{
				NumberFormat nbFormat = getNumberFormat();
				nbFormat.setMaximumFractionDigits(1);
				nbFormat.setMinimumFractionDigits(1);
				nbFormat.setGroupingUsed(false);
				
				DecimalFormat dcformat = (DecimalFormat) nbFormat;
				String decSeparator = dcformat.getDecimalFormatSymbols().getDecimalSeparator() + "";
				
				if (grade.indexOf(decSeparator) != -1)
				{
					if (grade.startsWith(decSeparator))
					{
						grade = "0".concat(grade);
					}
					else if (grade.endsWith(decSeparator))
					{
						grade = grade.concat("0");
					}
				}
				else
				{
					try
					{
						Integer.parseInt(grade);
						grade = grade.substring(0, grade.length() - 1) + decSeparator + grade.substring(grade.length() - 1);
					}
					catch (NumberFormatException e)
					{
						// alert
						alertInvalidPoint(state, grade);
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
					alertInvalidPoint(state, grade);
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
	 * scale the point value by 10 if there is a valid point grade
	 */
	private String scalePointGrade(SessionState state, String point)
	{
		NumberFormat nbFormat = (DecimalFormat) getNumberFormat();
		DecimalFormat dcFormat = (DecimalFormat) nbFormat;
		String decSeparator = dcFormat.getDecimalFormatSymbols().getDecimalSeparator() + "";
		
		point = validPointGrade(state, point);
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			if (point != null && (point.length() >= 1))
			{
				// when there is decimal points inside the grade, scale the number by 10
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
						// use scale integer for gradePoint
						point = point.substring(0, index) + point.substring(index + 1);
					}
					else
					{
						// decimal point is the last char
						point = point.substring(0, index) + "0";
					}
				}
				else
				{
					// if there is no decimal place, scale up the integer by 10
					point = point + "0";
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
		
		String removeAttachmentId = params.getString("currentAttachment");
		List attachments = state.getAttribute(ATTACHMENTS) == null?null:((List) state.getAttribute(ATTACHMENTS)).isEmpty()?null:(List) state.getAttribute(ATTACHMENTS);
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
				state.setAttribute(ATTACHMENTS, attachments);	
			}
		}
		
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
		
		List<String> submitterIds = AssignmentService.getSubmitterIdList(searchFilterOnly.toString(), allOrOneGroup, search, aRef, contextString);

		// construct the user-submission list
		if (submitterIds != null && !submitterIds.isEmpty())
		{
			for (Iterator<String> iSubmitterIdsIterator = submitterIds.iterator(); iSubmitterIdsIterator.hasNext();)
			{
				String uId = iSubmitterIdsIterator.next();
				try
				{
					User u = UserDirectoryService.getUser(uId);

					try
					{
						AssignmentSubmission sub = AssignmentService.getSubmission(aRef, u);
						rv.add(sub);
					}
					catch (IdUnusedException subIdException)
					{
						M_log.warn(this + ".sizeResources: looking for submission for unused assignment id " + aRef + subIdException.getMessage());
					}
					catch (PermissionException subPerException)
					{
						M_log.warn(this + ".sizeResources: cannot have permission to access submission of assignment " + aRef + " of user " + u.getId());
					}
				}
				catch (UserNotDefinedException e)
				{
					M_log.warn(this + ":sizeResources cannot find user id=" + uId + e.getMessage() + "");
				}
			}
		}
		
		return rv;
	}
	
	/**
	 * Set default score for all ungraded non electronic submissions
	 * @param data
	 */
	public void doSet_defaultNotGradedNonElectronicScore(RunData data)
	{
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
				validPointGrade(state, grade);
				
				if (state.getAttribute(STATE_MESSAGE) == null)
				{
					int maxGrade = a.getContent().getMaxGradePoint();
					try
					{
						if (Integer.parseInt(scalePointGrade(state, grade)) > maxGrade)
						{
							if (state.getAttribute(GRADE_GREATER_THAN_MAX_ALERT) == null)
							{
								// alert user first when he enters grade bigger than max scale
								addAlert(state, rb.getFormattedMessage("grad2", new Object[]{grade, displayGrade(state, String.valueOf(maxGrade))}));
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
						alertInvalidPoint(state, grade);
					}
				}
				
				if (state.getAttribute(STATE_MESSAGE) == null)
				{
					grade = scalePointGrade(state, grade);
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
				validPointGrade(state, grade);
				
				if (state.getAttribute(STATE_MESSAGE) == null)
				{
					int maxGrade = a.getContent().getMaxGradePoint();
					try
					{
						if (Integer.parseInt(scalePointGrade(state, grade)) > maxGrade)
						{
							if (state.getAttribute(GRADE_GREATER_THAN_MAX_ALERT) == null)
							{
								// alert user first when he enters grade bigger than max scale
								addAlert(state, rb.getFormattedMessage("grad2", new Object[]{grade, displayGrade(state, String.valueOf(maxGrade))}));
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
						alertInvalidPoint(state, grade);
						M_log.warn(this + ":setDefaultNoSubmissionScore " + e.getMessage());
					}
				}
				
				if (state.getAttribute(STATE_MESSAGE) == null)
				{
					grade = scalePointGrade(state, grade);
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
							AssignmentService.commitEdit(sEdit);
						}
					}
				}
			}
	}
	
	/**
	 * 
	 * @return
	 */
	public void doDownload_upload_all(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();
		String flow = params.getString("flow");
		if ("upload".equals(flow))
		{
			// upload
			doUpload_all(data);
		}
		else if ("download".equals(flow))
		{
			// upload
			doDownload_all(data);
		}
		else if ("cancel".equals(flow))
		{
			// cancel
			doCancel_download_upload_all(data);
		}
	}
	
	public void doDownload_all(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		
		state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_GRADE_ASSIGNMENT);
	}
	
	public void doUpload_all(RunData data)
	{ 
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
			addAlert(state, rb.getFormattedMessage("uploadall.size", new Object[]{max_file_size_mb}));
		}
		else 
		{	
			String contentType = fileFromUpload.getContentType();
			
			if (fileFromUpload.getFileName() == null || fileFromUpload.getFileName().length() == 0 
					|| (!"application/zip".equals(contentType) &&  !"application/x-zip-compressed".equals(contentType))) 
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
					state.removeAttribute(UPLOAD_ALL_HAS_COMMENTS);
					state.removeAttribute(UPLOAD_ALL_HAS_FEEDBACK_TEXT);
					state.removeAttribute(UPLOAD_ALL_HAS_FEEDBACK_ATTACHMENT);
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
					// inline text
					boolean hasFeedbackText = uploadAll_readChoice(choices, "feedbackTexts");
					// comments.txt should be available
					boolean hasComment = uploadAll_readChoice(choices, "feedbackComments");
					// feedback attachment
					boolean hasFeedbackAttachment = uploadAll_readChoice(choices, "feedbackAttachments");
					// release
					boolean	releaseGrades = params.getString("release") != null ? params.getBoolean("release") : false;
					
					state.setAttribute(UPLOAD_ALL_HAS_SUBMISSION_TEXT, Boolean.valueOf(hasSubmissionText));
					state.setAttribute(UPLOAD_ALL_HAS_SUBMISSION_ATTACHMENT, Boolean.valueOf(hasSubmissionAttachment));
					state.setAttribute(UPLOAD_ALL_HAS_GRADEFILE, Boolean.valueOf(hasGradeFile));
					state.setAttribute(UPLOAD_ALL_HAS_COMMENTS, Boolean.valueOf(hasComment));
					state.setAttribute(UPLOAD_ALL_HAS_FEEDBACK_TEXT, Boolean.valueOf(hasFeedbackText));
					state.setAttribute(UPLOAD_ALL_HAS_FEEDBACK_ATTACHMENT, Boolean.valueOf(hasFeedbackAttachment));
					state.setAttribute(UPLOAD_ALL_RELEASE_GRADES, Boolean.valueOf(releaseGrades));
					
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
								AssignmentSubmission s = (AssignmentSubmission) sIterator.next();
								User[] users = s.getSubmitters();
								if (users != null && users.length > 0 && users[0] != null)
								{
									submissionTable.put(users[0].getEid(), new UploadGradeWrapper(s.getGrade(), s.getSubmittedText(), s.getFeedbackComment(), hasSubmissionAttachment?new ArrayList():s.getSubmittedAttachments(), hasFeedbackAttachment?new ArrayList():s.getFeedbackAttachments(), (s.getSubmitted() && s.getTimeSubmitted() != null)?s.getTimeSubmitted().toString():"", s.getFeedbackText()));
								}
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
								assignment, fileContentStream);
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
			Assignment assignment, InputStream fileContentStream) {
		// a flag value for checking whether the zip file is of proper format: 
		// should have a grades.csv file if there is no user folders
		boolean zipHasGradeFile = false;
		// and if have any folder structures, those folders should be named after at least one site user (zip file could contain user names who is no longer inside the site)
		boolean zipHasFolder = false;
		boolean zipHasFolderValidUserId = false;
		
		FileOutputStream tmpFileOut = null;
		File tempFile = null;

		// as stated from UI, we expected the zip file to have structure as follows
		//       assignment_name/user_eid/files
		// or assignment_name/grades.csv
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
					if (entryName.endsWith("grades.csv"))
					{
						// at least the zip file has a grade.csv
						zipHasGradeFile = true;
						
						if (hasGradeFile)
						{
							// read grades.cvs from zip
							String result = StringUtils.trimToEmpty(readIntoString(zipFile.getInputStream(entry)));
					        String[] lines=null;
					        if (result.indexOf("\r\n") != -1)
					        	lines = result.split("\r\n");
					        else if (result.indexOf("\r") != -1)
					        		lines = result.split("\r");
					        else if (result.indexOf("\n") != -1)
				        			lines = result.split("\n");
					        if (lines != null )
					        {
						        for (int i = 3; i<lines.length; i++)
						        {
					        		// escape the first three header lines
					        		String[] items = lines[i].split(",");
					        		if (items.length > 4)
					        		{
					        			// has grade information
						        		try
						        		{
						        			User u = UserDirectoryService.getUserByEid(items[1]/*user eid*/);
						        			if (u != null)
						        			{
							        			UploadGradeWrapper w = (UploadGradeWrapper) submissionTable.get(u.getEid());
							        			if (w != null)
							        			{
							        				String itemString = items[4];
							        				int gradeType = assignment.getContent().getTypeOfGrade();
							        				if (gradeType == Assignment.SCORE_GRADE_TYPE)
							        				{
							        					validPointGrade(state, itemString);
							        				}
							        				else
							        				{
							        					validLetterGrade(state, itemString);
							        				}
							        				if (state.getAttribute(STATE_MESSAGE) == null)
							        				{
								        				w.setGrade(gradeType == Assignment.SCORE_GRADE_TYPE?scalePointGrade(state, itemString):itemString);
								        				submissionTable.put(u.getEid(), w);
							        				}
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
						}
					}
					else 
					{
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
								// get the eid part
								if (userEid.indexOf("(") != -1)
								{
									userEid = userEid.substring(userEid.indexOf("(")+1, userEid.indexOf(")"));
								}
								userEid=StringUtils.trimToNull(userEid);
							}
							if (submissionTable.containsKey(userEid))
							{
								if (!zipHasFolderValidUserId) zipHasFolderValidUserId = true;
								
								if (hasComment && entryName.indexOf("comments") != -1)
								{
									// read the comments file
									String comment = getBodyTextFromZipHtml(zipFile.getInputStream(entry));
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
									String text = getBodyTextFromZipHtml(zipFile.getInputStream(entry));
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
									String text = getBodyTextFromZipHtml(zipFile.getInputStream(entry));
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
		
		if ((!zipHasGradeFile && !zipHasFolder)					// generate error when there is no grade file and no folder structure
				|| (zipHasFolder && !zipHasFolderValidUserId) 	// generate error when there is folder structure but not matching one user id
				|| !validZipFormat)								// should have right structure of zip file
		{
			// alert if the zip is of wrong format
			addAlert(state, rb.getString("uploadall.alert.wrongZipFormat"));
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
				User[] users = s.getSubmitters();
				if (users != null && users.length > 0 && users[0] != null)
				{
					String uName = users[0].getEid();
					if (submissionTable.containsKey(uName))
					{
						// update the AssignmetnSubmission record
						AssignmentSubmissionEdit sEdit = editSubmission(s.getReference(), "doUpload_all", state);
						if (sEdit != null)	
						{
							UploadGradeWrapper w = (UploadGradeWrapper) submissionTable.get(uName);
							
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
								if (grade != null && !grade.equals(rb.getString("gen.nograd")) && !"ungraded".equals(grade))
									sEdit.setGraded(true);
							}
							
							// release or not
							if (sEdit.getGraded())
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

	private String getBodyTextFromZipHtml(InputStream zin)
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
		ParameterParser params = data.getParameters();
		String downloadUrl = params.getString("downloadUrl");
		state.setAttribute(STATE_DOWNLOAD_URL, downloadUrl);
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
		state.removeAttribute(UPLOAD_ALL_HAS_COMMENTS);
		state.removeAttribute(UPLOAD_ALL_RELEASE_GRADES);
		
	}
	
	/**
	 * Action is to preparing to go to the download all file
	 */
	public void doPrep_download_all(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();
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
			putTimePropertiesInContext(context, state, "Resubmit", ALLOW_RESUBMIT_CLOSEMONTH, ALLOW_RESUBMIT_CLOSEDAY, ALLOW_RESUBMIT_CLOSEYEAR, ALLOW_RESUBMIT_CLOSEHOUR, ALLOW_RESUBMIT_CLOSEMIN, ALLOW_RESUBMIT_CLOSEAMPM);	
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
			putTimePropertiesInState(state, allowResubmitTime, ALLOW_RESUBMIT_CLOSEMONTH, ALLOW_RESUBMIT_CLOSEDAY, ALLOW_RESUBMIT_CLOSEYEAR, ALLOW_RESUBMIT_CLOSEHOUR, ALLOW_RESUBMIT_CLOSEMIN, ALLOW_RESUBMIT_CLOSEAMPM);
		}
	}
	
	/**
	 * save the resubmit option for selected users
	 * @param data
	 */
	public void doSave_resubmission_option(RunData data)
	{
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
					User u = UserDirectoryService.getUser(userId);
					String assignmentRef = (String) state.getAttribute(EXPORT_ASSIGNMENT_REF);
					AssignmentSubmission submission = getSubmission(assignmentRef, u, "doSave_resubmission_option", state);
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
								Time closeTime = getTimeFromState(state, ALLOW_RESUBMIT_CLOSEMONTH, ALLOW_RESUBMIT_CLOSEDAY, ALLOW_RESUBMIT_CLOSEYEAR, ALLOW_RESUBMIT_CLOSEHOUR, ALLOW_RESUBMIT_CLOSEMIN, ALLOW_RESUBMIT_CLOSEAMPM);
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
		
		// make sure the options are exposed in UI 
		state.setAttribute(SHOW_ALLOW_RESUBMISSION, Boolean.TRUE);
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
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		ParameterParser params = data.getParameters ();

		String max_file_size_mb = ServerConfigurationService.getString("content.upload.max", "1");
		
		// construct the state variable for attachment list
		List attachments = state.getAttribute(ATTACHMENTS) != null? (List) state.getAttribute(ATTACHMENTS) : EntityManager.newReferenceList();
		
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
					try
					{
						String siteId = ToolManager.getCurrentPlacement().getContext();
						
						// add attachment
						enableSecurityAdvisor();
						ContentResource attachment = m_contentHostingService.addAttachmentResource(resourceId, siteId, "Assignments", contentType, fileContentStream, props);
						disableSecurityAdvisor();
						
						try
						{
							Reference ref = EntityManager.newReference(m_contentHostingService.getReference(attachment.getId()));
							attachments.add(ref);
						}
						catch(Exception ee)
						{
							M_log.warn(this + "doAttachUpload cannot find reference for " + attachment.getId() + ee.getMessage());
						}
						state.setAttribute(ATTACHMENTS, attachments);
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
     * remove recent added security advisor
     */
    protected void disableSecurityAdvisor()
    {
    	// remove recent added security advisor
    	SecurityService.popAdvisor();
    }

    /**
     * Establish a security advisor to allow the "embedded" azg work to occur
     * with no need for additional security permissions.
     */
    protected void enableSecurityAdvisor()
    {
      // put in a security advisor so we can create citationAdmin site without need
      // of further permissions
      SecurityService.pushAdvisor(new SecurityAdvisor() {
        public SecurityAdvice isAllowed(String userId, String function, String reference)
        {
          return SecurityAdvice.ALLOWED;
        }
      });
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
			tc.getPlacementConfig().setProperty(SUBMISSIONS_SEARCH_ONLY, Boolean.toString(submissionsSearchOnly));
			SiteService.save(site);
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
		context.put("letterGradeOptions", StringUtil.split(lOptions, ","));
	}
}	
