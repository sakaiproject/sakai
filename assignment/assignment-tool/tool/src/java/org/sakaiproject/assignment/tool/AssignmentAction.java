/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.assignment.tool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.sakaiproject.announcement.api.AnnouncementChannel;
import org.sakaiproject.announcement.api.AnnouncementMessageEdit;
import org.sakaiproject.announcement.api.AnnouncementMessageHeaderEdit;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.assignment.api.AssignmentContentEdit;
import org.sakaiproject.assignment.api.AssignmentEdit;
import org.sakaiproject.assignment.api.AssignmentSubmission;
import org.sakaiproject.assignment.api.AssignmentSubmissionEdit;
import org.sakaiproject.assignment.cover.AssignmentService;
import org.sakaiproject.authz.api.PermissionsHelper;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.PagedResourceActionII;
import org.sakaiproject.cheftool.PortletConfig;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentTypeImageService;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.service.gradebook.shared.AssignmentHasIllegalPointsException;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingExternalIdException;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeBreakdown;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
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

	/** The attachments */
	private static final String ATTACHMENTS = "Assignment.attachments";

	/** The content type image lookup service in the State. */
	private static final String STATE_CONTENT_TYPE_IMAGE_SERVICE = "Assignment.content_type_image_service";

	/** The calendar service in the State. */
	private static final String STATE_CALENDAR_SERVICE = "Assignment.calendar_service";

	/** The announcement service in the State. */
	private static final String STATE_ANNOUNCEMENT_SERVICE = "Assignment.announcement_service";

	/** The calendar object */
	private static final String CALENDAR = "calendar";

	/** The announcement channel */
	private static final String ANNOUNCEMENT_CHANNEL = "announcement_channel";

	/** The state mode */
	private static final String STATE_MODE = "Assignment.mode";

	/** The context string */
	private static final String STATE_CONTEXT_STRING = "Assignment.context_string";

	/** The user */
	private static final String STATE_USER = "Assignment.user";

	// SECTION MOD
	/** Used to keep track of the section info not currently being used. */
	private static final String STATE_SECTION_STRING = "Assignment.section_string";

	/** **************************** sort assignment ********************** */
	/** state sort * */
	private static final String SORTED_BY = "Assignment.sorted_by";

	/** state sort ascendingly * */
	private static final String SORTED_ASC = "Assignment.sorted_asc";

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

	/** *************************** sort submission *********************** */
	/** state sort submission* */
	private static final String SORTED_SUBMISSION_BY = "Assignment.submission_sorted_by";

	/** state sort submission ascendingly * */
	private static final String SORTED_SUBMISSION_ASC = "Assignment.submission_sorted_asc";

	/** state sort submission by submitters last name * */
	private static final String SORTED_SUBMISSION_BY_LASTNAME = "lastname";

	/** state sort submission by submit time * */
	private static final String SORTED_SUBMISSION_BY_SUBMIT_TIME = "submit_time";

	/** state sort submission by submission grade * */
	private static final String SORTED_SUBMISSION_BY_GRADE = "submission_grade";

	/** state sort submission by submission status * */
	private static final String SORTED_SUBMISSION_BY_STATUS = "status";

	/** state sort submission by submission released * */
	private static final String SORTED_SUBMISSION_BY_RELEASED = "released";

	/** state sort submission by assignment title */
	private static final String SORTED_SUBMISSION_BY_ASSIGNMENT = "assignment";

	/** state sort submission by max grade */
	private static final String SORTED_SUBMISSION_BY_MAX_GRADE = "submission_scale";

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

	/** ******************* instructor's grade submission ***************************** */
	private static final String GRADE_SUBMISSION_ASSIGNMENT_ID = "grade_submission_assignment_id";

	private static final String GRADE_SUBMISSION_SUBMISSION_ID = "grade_submission_submission_id";

	private static final String GRADE_SUBMISSION_FEEDBACK_COMMENT = "grade_submission_feedback_comment";

	private static final String GRADE_SUBMISSION_FEEDBACK_TEXT = "grade_submission_feedback_text";

	private static final String GRADE_SUBMISSION_FEEDBACK_ATTACHMENT = "grade_submission_feedback_attachment";

	private static final String GRADE_SUBMISSION_GRADE = "grade_submission_grade";

	private static final String GRADE_SUBMISSION_ASSIGNMENT_EXPAND_FLAG = "grade_submission_assignment_expand_flag";

	private static final String GRADE_SUBMISSION_ALLOW_RESUBMIT = "grade_submission_allow_resubmit";

	/** ******************* instructor's export assignment ***************************** */
	private static final String EXPORT_ASSIGNMENT_REF = "export_assignment_ref";

	private static final String EXPORT_ASSIGNMENT_ID = "export_assignment_id";

	/** ****************** instructor's new assignment ****************************** */
	private static final String NEW_ASSIGNMENT_TITLE = "new_assignment_title";

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

	private static final String NEW_ASSIGNMENT_GRADE_TYPE = "new_assignment_grade_type";

	private static final String NEW_ASSIGNMENT_GRADE_POINTS = "new_assignment_grade_points";

	private static final String NEW_ASSIGNMENT_DESCRIPTION = "new_assignment_instructions";

	private static final String NEW_ASSIGNMENT_DUE_DATE_SCHEDULED = "new_assignment_due_date_scheduled";

	private static final String NEW_ASSIGNMENT_OPEN_DATE_ANNOUNCED = "new_assignment_open_date_announced";

	private static final String NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE = "new_assignment_check_add_honor_pledge";

	private static final String NEW_ASSIGNMENT_HIDE_OPTION_FLAG = "new_assignment_hide_option_flag";

	private static final String NEW_ASSIGNMENT_FOCUS = "new_assignment_focus";

	private static final String NEW_ASSIGNMENT_DESCRIPTION_EMPTY = "new_assignment_description_empty";

	private static final String NEW_ASSIGNMENT_ADD_TO_GRADEBOOK = "new_assignment_add_to_gradebook";

	private static final String NEW_ASSIGNMENT_RANGE = "new_assignment_range";

	private static final String NEW_ASSIGNMENT_GROUPS = "new_assignment_groups";

	private static final String ATTACHMENTS_MODIFIED = "attachments_modified";

	/** **************************** instructor's view student submission ***************** */
	// the show/hide table based on member id
	private static final String STUDENT_LIST_SHOW_TABLE = "STUDENT_LIST_SHOW_TABLE";

	/** **************************** student view grade submission id *********** */
	private static final String VIEW_GRADE_SUBMISSION_ID = "view_grade_submission_id";

	/** **************************** modes *************************** */
	/** The list view of assignments */
   private static final String MODE_LIST_ASSIGNMENTS = "lisofass1"; // set in velocity template

	/** The student view of an assignment submission */
	private static final String MODE_STUDENT_VIEW_SUBMISSION = "Assignment.mode_view_submission";

	/** The student preview of an assignment submission */
	private static final String MODE_STUDENT_PREVIEW_SUBMISSION = "Assignment.mode_student_preview_submission";

	/** The student view of graded submission */
	private static final String MODE_STUDENT_VIEW_GRADE = "Assignment.mode_student_view_grade";

	/** The student view of assignments */
	private static final String MODE_STUDENT_VIEW_ASSIGNMENT = "Assignment.mode_student_view_assignment";

	/** The instructor view of creating a new assignment or editing an existing one */
	private static final String MODE_INSTRUCTOR_NEW_EDIT_ASSIGNMENT = "Assignment.mode_instructor_new_edit_assignment";

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

	/** The student view of assignment submission report */
   private static final String MODE_STUDENT_VIEW = "stuvie"; // set in velocity template

	/** ************************* vm names ************************** */
	/** The list view of assignments */
	private static final String TEMPLATE_LIST_ASSIGNMENTS = "_list_assignments";

	/** The student view of assignment */
	private static final String TEMPLATE_STUDENT_VIEW_ASSIGNMENT = "_student_view_assignment";

	/** The student view of showing an assignment submission */
	private static final String TEMPLATE_STUDENT_VIEW_SUBMISSION = "_student_view_submission";

	/** The student preview an assignment submission */
	private static final String TEMPLATE_STUDENT_PREVIEW_SUBMISSION = "_student_preview_submission";

	/** The student view of graded submission */
	private static final String TEMPLATE_STUDENT_VIEW_GRADE = "_student_view_grade";

	/** The instructor view to create a new assignment or edit an existing one */
	private static final String TEMPLATE_INSTRUCTOR_NEW_EDIT_ASSIGNMENT = "_instructor_new_edit_assignment";

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

	/** The opening mark comment */
	private static final String COMMENT_OPEN = "{{";

	/** The closing mark for comment */
	private static final String COMMENT_CLOSE = "}}";

	/** The selected view */
	private static final String STATE_SELECTED_VIEW = "state_selected_view";

	/** The configuration choice of with grading option or not */
	private static final String WITH_GRADES = "with_grades";

	/** The alert flag when doing global navigation from improper mode */
	private static final String ALERT_GLOBAL_NAVIGATION = "alert_global_navigation";

	/** The maximum trial number to get an uniq assignment title in gradebook */
	private static final int MAXIMUM_ATTEMPTS_FOR_UNIQUENESS = 100;

	/**
	 * central place for dispatching the build routines based on the state name
	 */
	public String buildMainPanelContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	{
		String template = null;
		
		context.put("tlang", rb);

		context.put("cheffeedbackhelper", this);

		String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);

		// allow add assignment?
		boolean allowAddAssignment = AssignmentService.allowAddAssignment(contextString);
		context.put("allowAddAssignment", Boolean.valueOf(allowAddAssignment));

		// allow grade assignment?
		boolean allowGradeSubmission = AssignmentService.allowGradeSubmission(contextString);
		context.put("allowGradeSubmission", Boolean.valueOf(allowGradeSubmission));

		// allow update site?
		context
				.put("allowUpdateSite", Boolean
						.valueOf(SiteService.allowUpdateSite(ToolManager.getCurrentPlacement().getContext())));

		// grading option
		context.put("withGrade", state.getAttribute(WITH_GRADES));

		String mode = (String) state.getAttribute(STATE_MODE);

		if (mode.equals(MODE_LIST_ASSIGNMENTS))
		{
			// build the context for the student assignment view
			template = build_list_assignments_context(portlet, context, data, state);
		}
		else if (mode.equals(MODE_STUDENT_VIEW_ASSIGNMENT))
		{
			// the student view of assignment
			template = build_student_view_assignment_context(portlet, context, data, state);
		}
		else if (mode.equals(MODE_STUDENT_VIEW_SUBMISSION))
		{
			// disable auto-updates while leaving the list view
			justDelivered(state);

			// build the context for showing one assignment submission
			template = build_student_view_submission_context(portlet, context, data, state);
		}
		else if (mode.equals(MODE_STUDENT_PREVIEW_SUBMISSION))
		{
			// build the context for showing one assignment submission
			template = build_student_preview_submission_context(portlet, context, data, state);
		}
		else if (mode.equals(MODE_STUDENT_VIEW_GRADE))
		{
			// disable auto-updates while leaving the list view
			justDelivered(state);

			// build the context for showing one graded submission
			template = build_student_view_grade_context(portlet, context, data, state);
		}
		else if (mode.equals(MODE_INSTRUCTOR_NEW_EDIT_ASSIGNMENT))
		{
			// allow add assignment?
			boolean allowAddSiteAssignment = AssignmentService.allowAddSiteAssignment(contextString);
			context.put("allowAddSiteAssignment", Boolean.valueOf(allowAddSiteAssignment));

			// disable auto-updates while leaving the list view
			justDelivered(state);

			// build the context for the instructor's create new assignment view
			template = build_instructor_new_edit_assignment_context(portlet, context, data, state);
		}
		else if (mode.equals(MODE_INSTRUCTOR_DELETE_ASSIGNMENT))
		{
			if (state.getAttribute(DELETE_ASSIGNMENT_IDS) != null)
			{
				Vector assignmentIds = (Vector) state.getAttribute(DELETE_ASSIGNMENT_IDS);
				boolean allowRemove = false;
				for (int i = 0; !allowRemove && i < assignmentIds.size(); i++)
				{
					String aReference = AssignmentService.assignmentReference(contextString, (String) assignmentIds.get(i));
					if (AssignmentService.allowRemoveAssignment(aReference))
					{
						allowRemove = true;
					}
				}

				// if user can remove at least one assignment
				if (allowRemove)
				{
					// disable auto-updates while leaving the list view
					justDelivered(state);

					// build the context for the instructor's delete assignment
					template = build_instructor_delete_assignment_context(portlet, context, data, state);
				}
			}
		}
		else if (mode.equals(MODE_INSTRUCTOR_GRADE_ASSIGNMENT) && allowGradeSubmission)
		{
			// build the context for the instructor's grade assignment
			template = build_instructor_grade_assignment_context(portlet, context, data, state);
		}
		else if (mode.equals(MODE_INSTRUCTOR_GRADE_SUBMISSION) && allowGradeSubmission)
		{
			// disable auto-updates while leaving the list view
			justDelivered(state);

			// build the context for the instructor's grade submission
			template = build_instructor_grade_submission_context(portlet, context, data, state);
		}
		else if (mode.equals(MODE_INSTRUCTOR_PREVIEW_GRADE_SUBMISSION) && allowGradeSubmission)
		{
			// build the context for the instructor's preview grade submission
			template = build_instructor_preview_grade_submission_context(portlet, context, data, state);
		}
		else if (mode.equals(MODE_INSTRUCTOR_PREVIEW_ASSIGNMENT) && allowAddAssignment)
		{
			// build the context for preview one assignment
			template = build_instructor_preview_assignment_context(portlet, context, data, state);
		}
		else if (mode.equals(MODE_INSTRUCTOR_VIEW_ASSIGNMENT) && (allowAddAssignment || allowGradeSubmission))
		{
			// disable auto-updates while leaving the list view
			justDelivered(state);

			// build the context for view one assignment
			template = build_instructor_view_assignment_context(portlet, context, data, state);
		}
		else if (mode.equals(MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT) && (allowAddAssignment || allowGradeSubmission))
		{
			// build the context for the instructor's create new assignment view
			template = build_instructor_view_students_assignment_context(portlet, context, data, state);
		}
		else if (mode.equals(MODE_INSTRUCTOR_REPORT_SUBMISSIONS) && (allowAddAssignment || allowGradeSubmission))
		{
			// build the context for the instructor's view of report submissions
			template = build_instructor_report_submissions(portlet, context, data, state);
		}

		if (template == null)
		{
			// default to student list view
			state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
			template = build_list_assignments_context(portlet, context, data, state);
		}

		// if group in assignment is allowed, show all the groups in this channal that user has get message in
		if (AssignmentService.getAllowGroupAssignments())
		{
			Collection groups = AssignmentService.getGroupsAllowAddAssignment(contextString);
			if (groups != null && groups.size() > 0)
			{
				context.put("groups", groups);
			}
		}
		return template;

	} // buildNormalContext

	/**
	 * build the student view of showing an assignment submission
	 */
	protected String build_student_view_submission_context(VelocityPortlet portlet, Context context, RunData data,
			SessionState state)
	{
		context.put("context", (String) state.getAttribute(STATE_CONTEXT_STRING));

		User user = (User) state.getAttribute(STATE_USER);
		String currentAssignmentReference = (String) state.getAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE);
		try
		{
			Assignment currentAssignment = AssignmentService.getAssignment(currentAssignmentReference);
			context.put("assignment", currentAssignment);
			AssignmentSubmission s = AssignmentService.getSubmission(currentAssignment.getReference(), user);
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
			}
		}
		catch (IdUnusedException e)
		{
			addAlert(state, "Cannot find the assignment.");
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getString("youarenot16"));
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
			addAlert(state, "You are not allowed to submit to this assignment. ");
		}
		context.put("allowSubmit", new Boolean(allowSubmit));

		String template = (String) getContext(data).get("template");
		return template + TEMPLATE_STUDENT_VIEW_SUBMISSION;

	} // build_student_view_submission_context

	/**
	 * build the student view of assignment
	 */
	protected String build_student_view_assignment_context(VelocityPortlet portlet, Context context, RunData data,
			SessionState state)
	{
		context.put("context", (String) state.getAttribute(STATE_CONTEXT_STRING));

		String aId = (String) state.getAttribute(VIEW_ASSIGNMENT_ID);
		try
		{
			Assignment currentAssignment = AssignmentService.getAssignment(aId);
			context.put("assignment", currentAssignment);
		}
		catch (IdUnusedException e)
		{
			addAlert(state, "Cannot find the assignment.");
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getString("youarenot14"));
		}

		context.put("contentTypeImageService", state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE));
		context.put("gradeTypeTable", gradeTypeTable());
		context.put("userDirectoryService", UserDirectoryService.getInstance());

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

		try
		{
			context.put("assignment", AssignmentService.getAssignment(aReference));
			context.put("submission", AssignmentService.getSubmission(aReference, user));
		}
		catch (IdUnusedException e)
		{
			addAlert(state, rb.getString("cannotfin3"));
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getString("youarenot16"));
		}

		context.put("text", state.getAttribute(PREVIEW_SUBMISSION_TEXT));
		context.put("honor_pledge_yes", state.getAttribute(PREVIEW_SUBMISSION_HONOR_PLEDGE_YES));
		context.put("attachments", state.getAttribute(PREVIEW_SUBMISSION_ATTACHMENTS));
		context.put("contentTypeImageService", state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE));

		String template = (String) getContext(data).get("template");
		return template + TEMPLATE_STUDENT_PREVIEW_SUBMISSION;

	} // build_student_preview_submission_context

	/**
	 * build the student view of showing a graded submission
	 */
	protected String build_student_view_grade_context(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	{
		context.put("contentTypeImageService", state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE));
		try
		{
			AssignmentSubmission s = AssignmentService.getSubmission((String) state.getAttribute(VIEW_GRADE_SUBMISSION_ID));
			context.put("assignment", s.getAssignment());
			context.put("submission", s);
		}
		catch (IdUnusedException e)
		{
			addAlert(state, rb.getString("cannotfin5"));
		}
		catch (PermissionException e)
		{
			addAlert(state, "You are not allowed to get the submission to this assignment. ");
		}

		String template = (String) getContext(data).get("template");
		return template + TEMPLATE_STUDENT_VIEW_GRADE;

	} // build_student_view_grade_context

	/**
	 * build the view of assignments list
	 */
	protected String build_list_assignments_context(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	{
		context.put("contextString", (String) state.getAttribute(STATE_CONTEXT_STRING));
		context.put("user", (User) state.getAttribute(STATE_USER));
		context.put("service", AssignmentService.getInstance());
		context.put("currentTime", TimeService.newTime());
		String sortedBy = (String) state.getAttribute(SORTED_BY);
		String sortedAsc = (String) state.getAttribute(SORTED_ASC);
		// clean sort criteria
		if (sortedBy.equals(SORTED_BY_GROUP_TITLE) || sortedBy.equals(SORTED_BY_GROUP_DESCRIPTION))
		{
			sortedBy = SORTED_BY_DUEDATE;
			sortedAsc = Boolean.TRUE.toString();
			state.setAttribute(SORTED_BY, sortedBy);
			state.setAttribute(SORTED_ASC, sortedAsc);
		}
		context.put("sortedBy", sortedBy);
		context.put("sortedAsc", sortedAsc);
		if (state.getAttribute(STATE_SELECTED_VIEW) != null)
		{
			context.put("view", state.getAttribute(STATE_SELECTED_VIEW));
		}

		boolean allowAdd = AssignmentService.allowAddAssignment((String) state.getAttribute(STATE_CONTEXT_STRING));
		context.put("allowAdd", new Boolean(allowAdd));

		boolean allowGrade = AssignmentService.allowGradeSubmission((String) state.getAttribute(STATE_CONTEXT_STRING));
		context.put("allowGrade", new Boolean(allowGrade));

		boolean allowSubmit = AssignmentService.allowAddSubmission((String) state.getAttribute(STATE_CONTEXT_STRING));
		context.put("allowSubmit", new Boolean(allowSubmit));

		List assignments = prepPage(state);
		boolean allowRemove = false;
		boolean allowRead = false;
		for (Iterator i = assignments.iterator(); i.hasNext();)
		{
			String assignmentReference = ((Assignment) i.next()).getReference();

			// see if user allowed to remove at least one assignment
			allowRemove = AssignmentService.allowRemoveAssignment(assignmentReference);

			// see if user allowed to read at least one assignment
			allowRead = AssignmentService.allowGetAssignment(assignmentReference);

		}
		context.put("allowRemove", new Boolean(allowRemove));
		context.put("allowRead", new Boolean(allowRead));

		context.put("assignments", assignments.iterator());

		context.put("contextString", state.getAttribute(STATE_CONTEXT_STRING));

		add2ndToolbarFields(data, context);

		// inform the observing courier that we just updated the page...
		// if there are pending requests to do so they can be cleared
		justDelivered(state);

		pagingInfoToContext(state, context);

		// put site object into context
		try
		{
			// get current site
			Site site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
			context.put("site", site);
		}
		catch (Exception ignore)
		{
		}

		String template = (String) getContext(data).get("template");
		return template + TEMPLATE_LIST_ASSIGNMENTS;

	} // build_list_assignments_context

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
			try
			{
				Assignment a = AssignmentService.getAssignment(assignmentId);
				context.put("assignment", a);
			}
			catch (IdUnusedException e)
			{
				addAlert(state, rb.getString("cannotfin3") + ": " + assignmentId);
			}
			catch (PermissionException e)
			{
				addAlert(state, rb.getString("youarenot14") + ": " + assignmentId);
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
		context.put("name_title", NEW_ASSIGNMENT_TITLE);

		context.put("name_OpenMonth", NEW_ASSIGNMENT_OPENMONTH);
		context.put("name_OpenDay", NEW_ASSIGNMENT_OPENDAY);
		context.put("name_OpenYear", NEW_ASSIGNMENT_OPENYEAR);
		context.put("name_OpenHour", NEW_ASSIGNMENT_OPENHOUR);
		context.put("name_OpenMin", NEW_ASSIGNMENT_OPENMIN);
		context.put("name_OpenAMPM", NEW_ASSIGNMENT_OPENAMPM);

		context.put("name_DueMonth", NEW_ASSIGNMENT_DUEMONTH);
		context.put("name_DueDay", NEW_ASSIGNMENT_DUEDAY);
		context.put("name_DueYear", NEW_ASSIGNMENT_DUEYEAR);
		context.put("name_DueHour", NEW_ASSIGNMENT_DUEHOUR);
		context.put("name_DueMin", NEW_ASSIGNMENT_DUEMIN);
		context.put("name_DueAMPM", NEW_ASSIGNMENT_DUEAMPM);

		context.put("name_EnableCloseDate", NEW_ASSIGNMENT_ENABLECLOSEDATE);
		context.put("name_CloseMonth", NEW_ASSIGNMENT_CLOSEMONTH);
		context.put("name_CloseDay", NEW_ASSIGNMENT_CLOSEDAY);
		context.put("name_CloseYear", NEW_ASSIGNMENT_CLOSEYEAR);
		context.put("name_CloseHour", NEW_ASSIGNMENT_CLOSEHOUR);
		context.put("name_CloseMin", NEW_ASSIGNMENT_CLOSEMIN);
		context.put("name_CloseAMPM", NEW_ASSIGNMENT_CLOSEAMPM);

		context.put("name_Section", NEW_ASSIGNMENT_SECTION);
		context.put("name_SubmissionType", NEW_ASSIGNMENT_SUBMISSION_TYPE);
		context.put("name_GradeType", NEW_ASSIGNMENT_GRADE_TYPE);
		context.put("name_GradePoints", NEW_ASSIGNMENT_GRADE_POINTS);
		context.put("name_Description", NEW_ASSIGNMENT_DESCRIPTION);
		context.put("name_CheckAddDueDate", ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE);
		context.put("name_CheckAutoAnnounce", ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE);
		context.put("name_CheckAddHonorPledge", NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE);

		// offer the gradebook integration choice only in the Assignments with Grading tool
		boolean withGrade = ((Boolean) state.getAttribute(WITH_GRADES)).booleanValue();
		if (withGrade)
		{
			context.put("name_Addtogradebook", NEW_ASSIGNMENT_ADD_TO_GRADEBOOK);
		}
		// gradebook integration
		context.put("withGradebook", Boolean.valueOf(isGradebookDefined()));

		// set the values
		context.put("value_title", state.getAttribute(NEW_ASSIGNMENT_TITLE));
		context.put("value_OpenMonth", state.getAttribute(NEW_ASSIGNMENT_OPENMONTH));
		context.put("value_OpenDay", state.getAttribute(NEW_ASSIGNMENT_OPENDAY));
		context.put("value_OpenYear", state.getAttribute(NEW_ASSIGNMENT_OPENYEAR));
		context.put("value_OpenHour", state.getAttribute(NEW_ASSIGNMENT_OPENHOUR));
		context.put("value_OpenMin", state.getAttribute(NEW_ASSIGNMENT_OPENMIN));
		context.put("value_OpenAMPM", state.getAttribute(NEW_ASSIGNMENT_OPENAMPM));

		context.put("value_DueMonth", state.getAttribute(NEW_ASSIGNMENT_DUEMONTH));
		context.put("value_DueDay", state.getAttribute(NEW_ASSIGNMENT_DUEDAY));
		context.put("value_DueYear", state.getAttribute(NEW_ASSIGNMENT_DUEYEAR));
		context.put("value_DueHour", state.getAttribute(NEW_ASSIGNMENT_DUEHOUR));
		context.put("value_DueMin", state.getAttribute(NEW_ASSIGNMENT_DUEMIN));
		context.put("value_DueAMPM", state.getAttribute(NEW_ASSIGNMENT_DUEAMPM));

		context.put("value_EnableCloseDate", state.getAttribute(NEW_ASSIGNMENT_ENABLECLOSEDATE));
		context.put("value_CloseMonth", state.getAttribute(NEW_ASSIGNMENT_CLOSEMONTH));
		context.put("value_CloseDay", state.getAttribute(NEW_ASSIGNMENT_CLOSEDAY));
		context.put("value_CloseYear", state.getAttribute(NEW_ASSIGNMENT_CLOSEYEAR));
		context.put("value_CloseHour", state.getAttribute(NEW_ASSIGNMENT_CLOSEHOUR));
		context.put("value_CloseMin", state.getAttribute(NEW_ASSIGNMENT_CLOSEMIN));
		context.put("value_CloseAMPM", state.getAttribute(NEW_ASSIGNMENT_CLOSEAMPM));

		context.put("value_Sections", state.getAttribute(NEW_ASSIGNMENT_SECTION));
		context.put("value_SubmissionType", state.getAttribute(NEW_ASSIGNMENT_SUBMISSION_TYPE));
		context.put("value_GradeType", state.getAttribute(NEW_ASSIGNMENT_GRADE_TYPE));
		// format to show one decimal place
		String maxGrade = (String) state.getAttribute(NEW_ASSIGNMENT_GRADE_POINTS);
		context.put("value_GradePoints", displayGrade(state, maxGrade));
		context.put("value_Description", state.getAttribute(NEW_ASSIGNMENT_DESCRIPTION));
		context.put("value_CheckAddDueDate", state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE));
		context.put("value_CheckAutoAnnounce", state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE));
		String s = (String) state.getAttribute(NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE);
		if (s == null) s = "1";
		context.put("value_CheckAddHonorPledge", s);
		context.put("value_AddToGradebook", state.getAttribute(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK));

		context.put("monthTable", monthTable());
		context.put("gradeTypeTable", gradeTypeTable());
		context.put("submissionTypeTable", submissionTypeTable());
		context.put("hide_assignment_option_flag", state.getAttribute(NEW_ASSIGNMENT_HIDE_OPTION_FLAG));
		context.put("attachments", state.getAttribute(ATTACHMENTS));
		context.put("contentTypeImageService", state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE));

		String range = (String) state.getAttribute(NEW_ASSIGNMENT_RANGE);
		String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
		
		// put site object into context
		try
		{
			// get current site
			Site site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
			context.put("site", site);
		}
		catch (Exception ignore)
		{
		}
		
		if (AssignmentService.getAllowGroupAssignments())
		{
			Collection groupsAllowAddAssignment = AssignmentService.getGroupsAllowAddAssignment(contextString);
			if (range != null && range.length() != 0)
			{
				context.put("range", range);
			}
			else
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
				context.put("groups", new SortedIterator(groupsAllowAddAssignment.iterator(), new AssignmentComparator(sort, asc)));
				context.put("assignmentGroups", state.getAttribute(NEW_ASSIGNMENT_GROUPS));
			}
		}
		
		context.put("allowGroupAssignmentsInGradebook", new Boolean(AssignmentService.getAllowGroupAssignmentsInGradebook()));

	} // setAssignmentFormContext

	/**
	 * build the instructor view of create a new assignment
	 */
	protected String build_instructor_preview_assignment_context(VelocityPortlet portlet, Context context, RunData data,
			SessionState state)
	{
		context.put("time", TimeService.newTime());

		context.put("user", UserDirectoryService.getCurrentUser());

		context.put("value_Title", (String) state.getAttribute(NEW_ASSIGNMENT_TITLE));

		// open time
		int openMonth = ((Integer) state.getAttribute(NEW_ASSIGNMENT_OPENMONTH)).intValue();
		int openDay = ((Integer) state.getAttribute(NEW_ASSIGNMENT_OPENDAY)).intValue();
		int openYear = ((Integer) state.getAttribute(NEW_ASSIGNMENT_OPENYEAR)).intValue();
		int openHour = ((Integer) state.getAttribute(NEW_ASSIGNMENT_OPENHOUR)).intValue();
		int openMin = ((Integer) state.getAttribute(NEW_ASSIGNMENT_OPENMIN)).intValue();
		String openAMPM = (String) state.getAttribute(NEW_ASSIGNMENT_OPENAMPM);
		if ((openAMPM.equals("PM")) && (openHour != 12))
		{
			openHour = openHour + 12;
		}
		if ((openHour == 12) && (openAMPM.equals("AM")))
		{
			openHour = 0;
		}
		Time openTime = TimeService.newTimeLocal(openYear, openMonth, openDay, openHour, openMin, 0, 0);
		context.put("value_OpenDate", openTime);

		// due time
		int dueMonth = ((Integer) state.getAttribute(NEW_ASSIGNMENT_DUEMONTH)).intValue();
		int dueDay = ((Integer) state.getAttribute(NEW_ASSIGNMENT_DUEDAY)).intValue();
		int dueYear = ((Integer) state.getAttribute(NEW_ASSIGNMENT_DUEYEAR)).intValue();
		int dueHour = ((Integer) state.getAttribute(NEW_ASSIGNMENT_DUEHOUR)).intValue();
		int dueMin = ((Integer) state.getAttribute(NEW_ASSIGNMENT_DUEMIN)).intValue();
		String dueAMPM = (String) state.getAttribute(NEW_ASSIGNMENT_DUEAMPM);
		if ((dueAMPM.equals("PM")) && (dueHour != 12))
		{
			dueHour = dueHour + 12;
		}
		if ((dueHour == 12) && (dueAMPM.equals("AM")))
		{
			dueHour = 0;
		}
		Time dueTime = TimeService.newTimeLocal(dueYear, dueMonth, dueDay, dueHour, dueMin, 0, 0);
		context.put("value_DueDate", dueTime);

		// close time
		Time closeTime = TimeService.newTime();
		Boolean enableCloseDate = (Boolean) state.getAttribute(NEW_ASSIGNMENT_ENABLECLOSEDATE);
		context.put("value_EnableCloseDate", enableCloseDate);
		if ((enableCloseDate).booleanValue())
		{
			int closeMonth = ((Integer) state.getAttribute(NEW_ASSIGNMENT_CLOSEMONTH)).intValue();
			int closeDay = ((Integer) state.getAttribute(NEW_ASSIGNMENT_CLOSEDAY)).intValue();
			int closeYear = ((Integer) state.getAttribute(NEW_ASSIGNMENT_CLOSEYEAR)).intValue();
			int closeHour = ((Integer) state.getAttribute(NEW_ASSIGNMENT_CLOSEHOUR)).intValue();
			int closeMin = ((Integer) state.getAttribute(NEW_ASSIGNMENT_CLOSEMIN)).intValue();
			String closeAMPM = (String) state.getAttribute(NEW_ASSIGNMENT_CLOSEAMPM);
			if ((closeAMPM.equals("PM")) && (closeHour != 12))
			{
				closeHour = closeHour + 12;
			}
			if ((closeHour == 12) && (closeAMPM.equals("AM")))
			{
				closeHour = 0;
			}
			closeTime = TimeService.newTimeLocal(closeYear, closeMonth, closeDay, closeHour, closeMin, 0, 0);
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
		context.put("value_AddToGradebook", state.getAttribute(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK));

		context.put("monthTable", monthTable());
		context.put("gradeTypeTable", gradeTypeTable());
		context.put("submissionTypeTable", submissionTypeTable());
		context.put("hide_assignment_option_flag", state.getAttribute(NEW_ASSIGNMENT_HIDE_OPTION_FLAG));
		context.put("attachments", state.getAttribute(ATTACHMENTS));
		context.put("contentTypeImageService", state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE));

		context.put("preview_assignment_assignment_hide_flag", state.getAttribute(PREVIEW_ASSIGNMENT_ASSIGNMENT_HIDE_FLAG));
		context.put("preview_assignment_student_view_hide_flag", state.getAttribute(PREVIEW_ASSIGNMENT_STUDENT_VIEW_HIDE_FLAG));
		context.put("value_assignment_id", state.getAttribute(PREVIEW_ASSIGNMENT_ASSIGNMENT_ID));
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
		Vector assignments = new Vector();
		Vector assignmentIds = (Vector) state.getAttribute(DELETE_ASSIGNMENT_IDS);
		for (int i = 0; i < assignmentIds.size(); i++)
		{
			try
			{
				Assignment a = AssignmentService.getAssignment((String) assignmentIds.get(i));

				Iterator submissions = AssignmentService.getSubmissions(a);
				if (submissions.hasNext())
				{
					// if there is submission to the assignment, show the alert
					addAlert(state, rb.getString("areyousur") + " \"" + a.getTitle() + "\" " + rb.getString("whihassub") + "\n");
				}
				assignments.add(a);
			}
			catch (IdUnusedException e)
			{
				addAlert(state, rb.getString("cannotfin3"));
			}
			catch (PermissionException e)
			{
				addAlert(state, rb.getString("youarenot14"));
			}
		}
		context.put("assignments", assignments);
		context.put("service", AssignmentService.getInstance());
		context.put("currentTime", TimeService.newTime());

		String template = (String) getContext(data).get("template");
		return template + TEMPLATE_INSTRUCTOR_DELETE_ASSIGNMENT;

	} // build_instructor_delete_assignment_context

	/**
	 * build the instructor view to grade an submission
	 */
	protected String build_instructor_grade_submission_context(VelocityPortlet portlet, Context context, RunData data,
			SessionState state)
	{
		int gradeType = -1;

		// assignment
		try
		{
			Assignment a = AssignmentService.getAssignment((String) state.getAttribute(GRADE_SUBMISSION_ASSIGNMENT_ID));
			context.put("assignment", a);
			gradeType = a.getContent().getTypeOfGrade();
		}
		catch (IdUnusedException e)
		{
			addAlert(state, rb.getString("cannotfin5"));
		}
		catch (PermissionException e)
		{
			addAlert(state, "You are not allowed to view the assignment submission. ");
		}

		// assignment submission
		try
		{
			AssignmentSubmission s = AssignmentService.getSubmission((String) state.getAttribute(GRADE_SUBMISSION_SUBMISSION_ID));
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

				if (p.getProperty(GRADE_SUBMISSION_ALLOW_RESUBMIT) != null)
				{
					context.put("value_allowResubmit", p.getProperty(GRADE_SUBMISSION_ALLOW_RESUBMIT));
				}
			}
		}
		catch (IdUnusedException e)
		{
			addAlert(state, rb.getString("cannotfin5"));
		}
		catch (PermissionException e)
		{
			addAlert(state, "You are not allowed to view the assignment submission. ");
		}

		context.put("user", state.getAttribute(STATE_USER));
		context.put("submissionTypeTable", submissionTypeTable());
		context.put("gradeTypeTable", gradeTypeTable());
		context.put("instructorAttachments", state.getAttribute(ATTACHMENTS));
		context.put("contentTypeImageService", state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE));
		context.put("service", AssignmentService.getInstance());

		// names
		context.put("name_grade_assignment_id", GRADE_SUBMISSION_ASSIGNMENT_ID);
		context.put("name_feedback_comment", GRADE_SUBMISSION_FEEDBACK_COMMENT);
		context.put("name_feedback_text", GRADE_SUBMISSION_FEEDBACK_TEXT);
		context.put("name_feedback_attachment", GRADE_SUBMISSION_FEEDBACK_ATTACHMENT);
		context.put("name_grade", GRADE_SUBMISSION_GRADE);

		// values
		context.put("value_grade_assignment_id", state.getAttribute(GRADE_SUBMISSION_ASSIGNMENT_ID));
		context.put("value_feedback_comment", state.getAttribute(GRADE_SUBMISSION_FEEDBACK_COMMENT));
		context.put("value_feedback_text", state.getAttribute(GRADE_SUBMISSION_FEEDBACK_TEXT));
		context.put("value_feedback_attachment", state.getAttribute(ATTACHMENTS));
		if (state.getAttribute(GRADE_SUBMISSION_ALLOW_RESUBMIT) != null)
		{
			context.put("value_allowResubmit", state.getAttribute(GRADE_SUBMISSION_ALLOW_RESUBMIT));
		}

		// format to show one decimal place in grade
		context.put("value_grade", (gradeType == 3) ? displayGrade(state, (String) state.getAttribute(GRADE_SUBMISSION_GRADE))
				: state.getAttribute(GRADE_SUBMISSION_GRADE));

		context.put("assignment_expand_flag", state.getAttribute(GRADE_SUBMISSION_ASSIGNMENT_EXPAND_FLAG));
		context.put("gradingAttachments", state.getAttribute(ATTACHMENTS));

		String template = (String) getContext(data).get("template");
		return template + TEMPLATE_INSTRUCTOR_GRADE_SUBMISSION;

	} // build_instructor_grade_submission_context

	/**
	 * build the instructor preview of grading submission
	 */
	protected String build_instructor_preview_grade_submission_context(VelocityPortlet portlet, Context context, RunData data,
			SessionState state)
	{

		// assignment
		int gradeType = -1;
		try
		{
			Assignment a = AssignmentService.getAssignment((String) state.getAttribute(GRADE_SUBMISSION_ASSIGNMENT_ID));
			context.put("assignment", a);
			gradeType = a.getContent().getTypeOfGrade();
		}
		catch (IdUnusedException e)
		{
			addAlert(state, rb.getString("cannotfin3"));
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getString("youarenot14"));
		}

		// submission
		try
		{
			context.put("submission", AssignmentService.getSubmission((String) state.getAttribute(GRADE_SUBMISSION_SUBMISSION_ID)));
		}
		catch (IdUnusedException e)
		{
			addAlert(state, rb.getString("cannotfin5"));
		}
		catch (PermissionException e)
		{
			addAlert(state, "You are not allowed to view the assignment submission. ");
		}

		User user = (User) state.getAttribute(STATE_USER);
		context.put("user", user);
		context.put("submissionTypeTable", submissionTypeTable());
		context.put("gradeTypeTable", gradeTypeTable());
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

		context.put("allowResubmit", state.getAttribute(GRADE_SUBMISSION_ALLOW_RESUBMIT));

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

		try
		{
			Assignment a = AssignmentService.getAssignment((String) state.getAttribute(EXPORT_ASSIGNMENT_REF));
			context.put("assignment", a);
			state.setAttribute(EXPORT_ASSIGNMENT_ID, a.getId());

			String sortedSubmissionBy = (String) state.getAttribute(SORTED_SUBMISSION_BY);
			String sortedSubmissionAsc = (String) state.getAttribute(SORTED_SUBMISSION_ASC);
			context.put("sortedBy", sortedSubmissionBy);
			context.put("sortedAsc", sortedSubmissionAsc);
			List submissions = prepPage(state);
			context.put("submissions", submissions);

			boolean noSubmittedSubmission = true;
			if (submissions != null && submissions.size() > 0)
			{
				for (int k = 0; noSubmittedSubmission && k < submissions.size(); k++)
				{
					if (((AssignmentSubmission) submissions.get(k)).getSubmitted())
					{
						noSubmittedSubmission = false;
					}
				}
			}
			context.put("noSubmittedSubmission", new Boolean(noSubmittedSubmission));
		}
		catch (IdUnusedException e)
		{
			addAlert(state, rb.getString("cannotfin3"));
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getString("youarenot14"));
		}

		context.put("submissionTypeTable", submissionTypeTable());
		context.put("gradeTypeTable", gradeTypeTable());
		context.put("attachments", state.getAttribute(ATTACHMENTS));
		context.put("contentTypeImageService", state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE));
		context.put("service", AssignmentService.getInstance());

		context.put("assignment_expand_flag", state.getAttribute(GRADE_ASSIGNMENT_EXPAND_FLAG));
		context.put("submission_expand_flag", state.getAttribute(GRADE_SUBMISSION_EXPAND_FLAG));

		// the user directory service
		context.put("userDirectoryService", UserDirectoryService.getInstance());
		add2ndToolbarFields(data, context);

		pagingInfoToContext(state, context);

		String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
		context.put("accessPointUrl", (ServerConfigurationService.getAccessUrl()).concat(AssignmentService.submissionsZipReference(
				contextString, (String) state.getAttribute(EXPORT_ASSIGNMENT_REF))));
		// gradebook integration
		context.put("withGradebook", Boolean.valueOf(isGradebookDefined()));

		String template = (String) getContext(data).get("template");
		return template + TEMPLATE_INSTRUCTOR_GRADE_ASSIGNMENT;

	} // build_instructor_grade_assignment_context

	/**
	 * build the instructor view of an assignment
	 */
	protected String build_instructor_view_assignment_context(VelocityPortlet portlet, Context context, RunData data,
			SessionState state)
	{
		try
		{
			context.put("assignment", AssignmentService.getAssignment((String) state.getAttribute(VIEW_ASSIGNMENT_ID)));
		}
		catch (IdUnusedException e)
		{
			addAlert(state, rb.getString("cannotfin3"));
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getString("youarenot14"));
		}

		context.put("currentTime", TimeService.newTime());
		context.put("submissionTypeTable", submissionTypeTable());
		context.put("gradeTypeTable", gradeTypeTable());
		context.put("hideAssignmentFlag", state.getAttribute(VIEW_ASSIGNMENT_HIDE_ASSIGNMENT_FLAG));
		context.put("hideStudentViewFlag", state.getAttribute(VIEW_ASSIGNMENT_HIDE_STUDENT_VIEW_FLAG));
		context.put("contentTypeImageService", state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE));

		// the user directory service
		context.put("userDirectoryService", UserDirectoryService.getInstance());

		String template = (String) getContext(data).get("template");
		return template + TEMPLATE_INSTRUCTOR_VIEW_ASSIGNMENT;

	} // build_instructor_view_assignment_context

	/**
	 * build the instructor view to view the list of students for an assignment
	 */
	protected String build_instructor_view_students_assignment_context(VelocityPortlet portlet, Context context, RunData data,
			SessionState state)
	{
		Iterator assignments = AssignmentService.getAssignmentsForContext((String) state.getAttribute(STATE_CONTEXT_STRING));
		Vector assignmentsVector = new Vector();
		while (assignments.hasNext())
		{
			Assignment a = (Assignment) assignments.next();
			String deleted = a.getProperties().getProperty(ResourceProperties.PROP_ASSIGNMENT_DELETED);
			if ((deleted == null || deleted.equals("")) && (!a.getDraft()))
			{
				assignmentsVector.add(a);
			}
		}

		List studentMembers = new Vector();
		if (assignmentsVector.size() != 0)
		{
			List allowAddAssignmentUsers = AssignmentService.allowAddAssignmentUsers((String) state
					.getAttribute(STATE_CONTEXT_STRING));
			List allowAddSubmissionUsers = AssignmentService.allowAddSubmissionUsers(((Assignment) assignmentsVector.get(0))
					.getReference());
			for (int i = 0; i < allowAddSubmissionUsers.size(); i++)
			{
				User allowAddSubmissionUser = (User) allowAddSubmissionUsers.get(i);
				if (!allowAddAssignmentUsers.contains(allowAddSubmissionUser))
				{
					studentMembers.add(allowAddSubmissionUser);
				}
			}
		}
		context.put("studentMembers", studentMembers);
		context.put("assignmentService", AssignmentService.getInstance());
		context.put("assignments", assignmentsVector);
		if (state.getAttribute(STUDENT_LIST_SHOW_TABLE) != null)
		{
			context.put("studentListShowTable", (Hashtable) state.getAttribute(STUDENT_LIST_SHOW_TABLE));
		}

		add2ndToolbarFields(data, context);

		pagingInfoToContext(state, context);

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
			GradebookService g = (GradebookService) (org.sakaiproject.service.gradebook.shared.GradebookService) ComponentManager
					.get("org.sakaiproject.service.gradebook.GradebookService");
			String gradebookUid = ToolManager.getInstance().getCurrentPlacement().getContext();
			if (g.isGradebookDefined(gradebookUid))
			{
				rv = true;
			}
		}
		catch (Exception e)
		{
			Log.debug("chef", this + rb.getString("addtogradebook.alertMessage") + "\n" + e.getMessage());
		}

		return rv;

	} // isGradebookDefined()

	/**
	 * integration with gradebook
	 * 
	 * @param state
	 * @param assignmentRef Assignment reference
	 * @param addUpdateRemoveAssignment "add" for adding the assignment; "update" for updating the assignment; "remove" for remove assignment
	 * @param oldAssignment_title The original assignment title
	 * @param newAssignment_title The updated assignment title
	 * @param newAssignment_maxPoints The maximum point of the assignment
	 * @param newAssignment_dueTime The due time of the assignment
	 * @param submissionRef Any submission grade need to be updated? Do bulk update if null
	 * @param updateRemoveSubmission "update" for update submission;"remove" for remove submission
	 */
	protected void integrateGradebook (SessionState state, String assignmentRef, String addUpdateRemoveAssignment, String oldAssignment_title, String newAssignment_title, int newAssignment_maxPoints, Time newAssignment_dueTime, String submissionRef, String updateRemoveSubmission)
	{
		// add or remove external grades to gradebook
		// a. if Gradebook does not exists, do nothing, 'cos setting should have been hidden
		// b. if Gradebook exists, just call addExternal and removeExternal and swallow any exception. The
		// exception are indication that the assessment is already in the Gradebook or there is nothing
		// to remove.
		GradebookService g = (GradebookService) (org.sakaiproject.service.gradebook.shared.GradebookService) ComponentManager
				.get("org.sakaiproject.service.gradebook.GradebookService");
		String gradebookUid = ToolManager.getInstance().getCurrentPlacement().getContext();
		boolean gradebookExists = isGradebookDefined();

		if (gradebookExists)
		{

			if (addUpdateRemoveAssignment != null)
			{
				// add an entry into Gradebook for newly created assignment or modified assignment but newly integrated
				if (addUpdateRemoveAssignment.equals("add")
					|| ( addUpdateRemoveAssignment.equals("update") && !g.isAssignmentDefined(gradebookUid, newAssignment_title) && !g.isAssignmentDefined(gradebookUid, oldAssignment_title)))
				{
					// add assignment into gradebook
					try
					{
						// add assignment to gradebook
						g.addExternalAssessment(gradebookUid, assignmentRef, null, newAssignment_title,
								newAssignment_maxPoints / 10, new Date(newAssignment_dueTime.getTime()), "Assignment");
					}
					catch (AssignmentHasIllegalPointsException e)
					{
						addAlert(state, rb.getString("addtogradebook.illegalPoints"));
					}
					catch (ConflictingAssignmentNameException e)
					{
						// try to modify assignment title, make sure there is no such assignment in the gradebook, and insert again
						boolean trying = true;
						int attempts = 1;
						String titleBase = newAssignment_title;
						while (trying && attempts < MAXIMUM_ATTEMPTS_FOR_UNIQUENESS) // see end of loop for condition that enforces attempts <= limit)
						{
							String newTitle = titleBase + "-" + attempts;

							if (!g.isAssignmentDefined(gradebookUid, newTitle))
							{
								try
								{
									// add assignment to gradebook
									g.addExternalAssessment(gradebookUid, assignmentRef, null, newTitle,
											newAssignment_maxPoints / 10, new Date(newAssignment_dueTime.getTime()), "Assignment");
									trying = false;
								}
								catch (Exception ee)
								{
									// try again, ignore the exception
								}
							}

							if (trying)
							{
								attempts++;
								if (attempts >= MAXIMUM_ATTEMPTS_FOR_UNIQUENESS)
								{
									// add alert prompting for change assignment title
									addAlert(state, rb.getString("addtogradebook.nonUniqueTitle"));
								}
							}
						}
					}
					catch (ConflictingExternalIdException e)
					{
						// ignore
					}
					catch (GradebookNotFoundException e)
					{
						// ignore
					}
					catch (Exception e)
					{
						// ignore
					}
				}
				else if (addUpdateRemoveAssignment.equals("update"))
				{
					try
					{
					    Assignment a = AssignmentService.getAssignment(assignmentRef);
					    	
					    // update attributes for existing assignment
				    	g.updateExternalAssessment(gradebookUid, assignmentRef, null, a.getTitle(), a.getContent().getMaxGradePoint()/10, new Date(a.getDueTime().getTime()));
					}
				    catch(Exception e)
			        {
			        	Log.debug("chef", "Cannot find assignment " + assignmentRef + ": " + e.getMessage());
			        }
				}	// addUpdateRemove != null
				else if (addUpdateRemoveAssignment.equals("remove"))
				{
					// remove assignment and all submission grades
					try
					{
						g.removeExternalAssessment(gradebookUid, assignmentRef);
					}
					catch (Exception e)
					{
						Log.debug("chef", "Exception when removing assignment " + assignmentRef + " and its submissions:"
								+ e.getMessage());
					}
				}
			}

			if (updateRemoveSubmission != null)
			{
				try
				{
					Assignment a = AssignmentService.getAssignment(assignmentRef);

					if (updateRemoveSubmission.equals("update")
							&& a.getProperties().getProperty(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK) != null
							&& a.getProperties().getBooleanProperty(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK)
							&& a.getContent().getTypeOfGrade() == Assignment.SCORE_GRADE_TYPE)
					{
						if (submissionRef == null)
						{
							// bulk add all grades for assignment into gradebook
							Iterator submissions = AssignmentService.getSubmissions(a);

							Map m = new HashMap();
							
							// any score to copy over? get all the assessmentGradingData and copy over
							while (submissions.hasNext())
							{
								AssignmentSubmission aSubmission = (AssignmentSubmission) submissions.next();
								User[] submitters = aSubmission.getSubmitters();
								String submitterId = submitters[0].getId();
								Double grade = StringUtil.trimToNull(aSubmission.getGrade()) != null ? Double.valueOf(displayGrade(state,aSubmission.getGrade())) : null;
								m.put(submitterId, grade);
							}
							
							// need to update only when there is at least one submission
							if (m.size()>0)
							{
								g.updateExternalAssessmentScores(gradebookUid, assignmentRef, m);
							}
						}
						else
						{
							try
							{
								// only update one submission
								AssignmentSubmission aSubmission = (AssignmentSubmission) AssignmentService
										.getSubmission(submissionRef);
								User[] submitters = aSubmission.getSubmitters();
								g.updateExternalAssessmentScore(gradebookUid, assignmentRef, submitters[0].getId(), StringUtil
										.trimToNull(aSubmission.getGrade()) != null ? Double.valueOf(displayGrade(state,
										aSubmission.getGrade())) : null);
							}
							catch (Exception e)
							{
								Log.debug("chef", "Cannot find submission " + submissionRef + ": " + e.getMessage());
							}
						}

					}
					else if (updateRemoveSubmission.equals("remove"))
					{
						if (submissionRef == null)
						{
							// remove all submission grades (cannot think of a user case within Assignment tool for this)
							Iterator submissions = AssignmentService.getSubmissions(a);

							// any score to copy over? get all the assessmentGradingData and copy over
							while (submissions.hasNext())
							{
								AssignmentSubmission aSubmission = (AssignmentSubmission) submissions.next();
								User[] submitters = aSubmission.getSubmitters();
								g.updateExternalAssessmentScore(gradebookUid, assignmentRef, submitters[0].getId(), null);
							}
						}
						else
						{
							// remove only one submission grade
							try
							{
								AssignmentSubmission aSubmission = (AssignmentSubmission) AssignmentService
										.getSubmission(submissionRef);
								User[] submitters = aSubmission.getSubmitters();
								g.updateExternalAssessmentScore(gradebookUid, assignmentRef, submitters[0].getId(), null);
							}
							catch (Exception e)
							{
								Log.debug("chef", "Cannot find submission " + submissionRef + ": " + e.getMessage());
							}
						}
					}
				}
				catch (Exception e)
				{
					Log.debug("chef", "Cannot find assignment " + assignmentRef + ": " + e.getMessage());
				}
			} // updateRemoveSubmission != null
		} // if gradebook exists
	} // integrateGradebook

	/**
	 * Go to the instructor view
	 */
	public void doView_instructor(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
		state.setAttribute(SORTED_BY, SORTED_BY_DUEDATE);
		state.setAttribute(SORTED_ASC, Boolean.TRUE.toString());

	} // doView_instructor

	/**
	 * Go to the student view
	 */
	public void doView_student(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// to the student list of assignment view
		state.setAttribute(SORTED_BY, SORTED_BY_DUEDATE);
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

		try
		{
			AssignmentSubmission submission = AssignmentService.getSubmission(assignmentReference, u);

			if (submission != null)
			{
				state.setAttribute(VIEW_SUBMISSION_TEXT, submission.getSubmittedText());
				state.setAttribute(VIEW_SUBMISSION_HONOR_PLEDGE_YES, (new Boolean(submission.getHonorPledgeFlag())).toString());
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

			state.setAttribute(STATE_MODE, MODE_STUDENT_VIEW_SUBMISSION);
		}
		catch (IdUnusedException e)
		{
			addAlert(state, rb.getString("cannotfin5"));
		}
		catch (PermissionException e)
		{
			addAlert(state, "You are not allowed to view the assignment submission. ");
		} // try

	} // doView_submission

	/**
	 * Preview of the submission
	 */
	public void doPreview_submission(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		ParameterParser params = data.getParameters();
		// String assignmentId = params.getString(assignmentId);
		state.setAttribute(PREVIEW_SUBMISSION_ASSIGNMENT_REFERENCE, state.getAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE));

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
		ParameterParser params = data.getParameters();

		String feedbackText = processAssignmentFeedbackFromBrowser(state, params.getCleanString(GRADE_SUBMISSION_FEEDBACK_TEXT));
		String grade = params.getString(GRADE_SUBMISSION_GRADE);
		state.setAttribute(GRADE_SUBMISSION_GRADE, grade);
		boolean checkForFormattingErrors = false; // so that grading isn't held up by formatting errors
		String feedbackComment = processFormattedTextFromBrowser(state, params.getCleanString(GRADE_SUBMISSION_FEEDBACK_COMMENT),
				checkForFormattingErrors);
		state.setAttribute(GRADE_SUBMISSION_FEEDBACK_COMMENT, feedbackComment);
		state.setAttribute(GRADE_SUBMISSION_FEEDBACK_TEXT, feedbackText);
		state.setAttribute(GRADE_SUBMISSION_FEEDBACK_ATTACHMENT, state.getAttribute(ATTACHMENTS));

		if (params.getString("allowResubmit") != null)
		{
			state.setAttribute(GRADE_SUBMISSION_ALLOW_RESUBMIT, Boolean.TRUE);
		}
		else
		{
			state.setAttribute(GRADE_SUBMISSION_ALLOW_RESUBMIT, Boolean.FALSE);
		}

		boolean withGrade = ((Boolean) state.getAttribute(WITH_GRADES)).booleanValue();

		if (withGrade)
		{
			if (grade != null && !grade.equals(""))
			{
				state.setAttribute(GRADE_SUBMISSION_GRADE, grade);
			}
			else
			{
				addAlert(state, rb.getString("plespethe2"));
			}
		}

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			String sId = (String) state.getAttribute(GRADE_SUBMISSION_SUBMISSION_ID);
			try
			{
				AssignmentSubmission s = AssignmentService.getSubmission(sId);
				Assignment a = s.getAssignment();
				if (a.getContent().getTypeOfGrade() == 3)
				{
					if ((grade != null) && (grade.length() != 0))
					{
						validPointGrade(state, grade);
						state.setAttribute(GRADE_SUBMISSION_GRADE, grade);
						if (state.getAttribute(STATE_MESSAGE) == null)
						{
							grade = scalePointGrade(state, grade);
						}
						if (state.getAttribute(STATE_MESSAGE) == null)
						{
							try
							{
								int l = Integer.parseInt(grade);
								if (l > a.getContent().getMaxGradePoint())
								{
									addAlert(state, rb.getString("grad2"));
								}
							}
							catch (NumberFormatException e)
							{
								alertInvalidPoint(state, grade);
							}
						}
					}
				}
			}
			catch (IdUnusedException e)
			{
				addAlert(state, rb.getString("cannotfin3"));
			}
			catch (PermissionException e)
			{
				addAlert(state, "You are not allowed to view the assignment submission. ");
			}
		}
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute(GRADE_SUBMISSION_GRADE, grade);
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
	 * Action is to end the preview edit assignment process
	 */
	public void doDone_preview_edit_assignment(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// back to the edit assignment page
		state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_NEW_EDIT_ASSIGNMENT);

	} // doDone_preview_edit_assignment

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
		state.setAttribute(DELETE_ASSIGNMENT_IDS, new Vector());

		// back to the instructor list view of assignments
		state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);

	} // doCancel_delete_assignment

	/**
	 * Action is to end the show submission process
	 */
	public void doCancel_edit_assignment(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// back to the student list view of assignments
		state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);

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

	} // doCancel_new_assignment

	/**
	 * Action is to cancel the grade submission process
	 */
	public void doCancel_grade_submission(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// reset the assignment object
		// resetAssignment (state);

		// back to the student list view of assignments
		state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_GRADE_ASSIGNMENT);

	} // doCancel_grade_submission

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
	 * Action is to return to the view of list assignments
	 */
	public void doList_assignments(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// back to the student list view of assignments
		state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
		state.setAttribute(SORTED_BY, SORTED_BY_DUEDATE);

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
		grade_submission_option(data, "save");

	} // doSave_grade_submission

	/**
	 * Action is to release the grade to submission
	 */
	public void doRelease_grade_submission(RunData data)
	{
		grade_submission_option(data, "release");

	} // doRelease_grade_submission

	/**
	 * Action is to return submission with or without grade
	 */
	public void doReturn_grade_submission(RunData data)
	{
		grade_submission_option(data, "return");

	} // doReturn_grade_submission

	/**
	 * Common grading routine plus specific operation to differenciate cases when saving, releasing or returning grade.
	 */
	private void grade_submission_option(RunData data, String gradeOption)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();

		boolean withGrade = state.getAttribute(WITH_GRADES) != null ? ((Boolean) state.getAttribute(WITH_GRADES)).booleanValue()
				: false;

		if (params.getString("allowResubmit") != null)
		{
			state.setAttribute(GRADE_SUBMISSION_ALLOW_RESUBMIT, Boolean.TRUE);
		}
		else
		{
			state.setAttribute(GRADE_SUBMISSION_ALLOW_RESUBMIT, Boolean.FALSE);
		}

		boolean checkForFormattingErrors = false; // so that grading isn't held up by formatting errors
		String feedbackComment = processFormattedTextFromBrowser(state, params.getCleanString(GRADE_SUBMISSION_FEEDBACK_COMMENT),
				checkForFormattingErrors);
		if (feedbackComment != null)
		{
			state.setAttribute(GRADE_SUBMISSION_FEEDBACK_COMMENT, feedbackComment);
		}

		String feedbackText = processAssignmentFeedbackFromBrowser(state, params.getCleanString(GRADE_SUBMISSION_FEEDBACK_TEXT));
		if (feedbackText != null)
		{
			state.setAttribute(GRADE_SUBMISSION_FEEDBACK_TEXT, feedbackText);
		}

		state.setAttribute(GRADE_SUBMISSION_FEEDBACK_ATTACHMENT, state.getAttribute(ATTACHMENTS));

		String g = params.getCleanString(GRADE_SUBMISSION_GRADE);
		if (g != null)
		{
			state.setAttribute(GRADE_SUBMISSION_GRADE, g);
		}

		String sId = (String) state.getAttribute(GRADE_SUBMISSION_SUBMISSION_ID);

		try
		{
			// for points grading, one have to enter number as the points
			String grade = (String) state.getAttribute(GRADE_SUBMISSION_GRADE);

			Assignment a = AssignmentService.getSubmission(sId).getAssignment();
			int typeOfGrade = a.getContent().getTypeOfGrade();

			if (withGrade)
			{
				// do grade validation only for Assignment with Grade tool
				if (typeOfGrade == 3)
				{
					if ((grade.length() == 0))
					{
						if (gradeOption.equals("release"))
						{
							// in case of releasing grade, user must specify a grade
							addAlert(state, rb.getString("plespethe2"));
						}
					}
					else
					{
						// the preview grade process might already scaled up the grade by 10
						if (!((String) state.getAttribute(STATE_MODE)).equals(MODE_INSTRUCTOR_PREVIEW_GRADE_SUBMISSION))
						{
							validPointGrade(state, grade);
							state.setAttribute(GRADE_SUBMISSION_GRADE, grade);
							if (state.getAttribute(STATE_MESSAGE) == null)
							{
								grade = scalePointGrade(state, grade);
							}
						}

						if (state.getAttribute(STATE_MESSAGE) == null)
						{
							int maxGrade = a.getContent().getMaxGradePoint();
							try
							{
								if (Integer.parseInt(grade) > maxGrade)
								{
									addAlert(state, rb.getString("grad2"));
								}
							}
							catch (NumberFormatException e)
							{
								alertInvalidPoint(state, grade);
							}
						}
					}
				}

				// if ungraded and grade type is not "ungraded" type
				if ((grade == null || grade.equals("ungraded")) && (typeOfGrade != 1) && gradeOption.equals("release"))
				{
					addAlert(state, rb.getString("plespethe2"));
				}
			}

			if (state.getAttribute(STATE_MESSAGE) == null)
			{
				state.setAttribute(GRADE_SUBMISSION_GRADE, grade);
				AssignmentSubmissionEdit sEdit = AssignmentService.editSubmission(sId);

				if (!withGrade)
				{
					// no grade input needed for the without-grade version of assignment tool
					sEdit.setGraded(true);
					if (gradeOption.equals("return") || gradeOption.equals("release"))
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
					if (typeOfGrade == 1)
					{
						sEdit.setGrade("no grade");
						sEdit.setGraded(true);
					}
					else
					{
						if (!grade.equals(""))
						{
							if (typeOfGrade == 3)
							{
								sEdit.setGrade(grade);
							}
							else
							{
								sEdit.setGrade(grade);
							}
							sEdit.setGraded(true);
						}
					}
				}

				if (gradeOption.equals("release"))
				{
					sEdit.setGradeReleased(true);
					sEdit.setGraded(true);
					// clear the returned flag
					sEdit.setReturned(false);
					sEdit.setTimeReturned(null);
				}
				else if (gradeOption.equals("return"))
				{
					if (StringUtil.trimToNull(grade) != null)
					{
						sEdit.setGradeReleased(true);
						sEdit.setGraded(true);
					}
					sEdit.setReturned(true);
					sEdit.setTimeReturned(TimeService.newTime());
					sEdit.setHonorPledgeFlag(Boolean.FALSE.booleanValue());
				}

				if (state.getAttribute(GRADE_SUBMISSION_ALLOW_RESUBMIT) != null && ((Boolean)state.getAttribute(GRADE_SUBMISSION_ALLOW_RESUBMIT)).booleanValue())
				{
					sEdit.getPropertiesEdit().addProperty(GRADE_SUBMISSION_ALLOW_RESUBMIT, Boolean.TRUE.toString());
					state.removeAttribute(GRADE_SUBMISSION_ALLOW_RESUBMIT);
				}
				else
				{
					sEdit.getPropertiesEdit().removeProperty(GRADE_SUBMISSION_ALLOW_RESUBMIT);
				}
				
				// the instructor comment
				String feedbackCommentString = StringUtil
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
				if (gradeOption.equals("release") || gradeOption.equals("return"))
				{
					// update grade in gradebook
					integrateGradebook(state, a.getReference(), null, null, null, -1, null, sReference, "update");
				}
				else
				{
					// remove grade from gradebook
					integrateGradebook(state, a.getReference(), null, null, null, -1, null, sReference, "remove");
				}

			} // if
		}
		catch (IdUnusedException e)
		{
		}
		catch (PermissionException e)
		{
		}
		catch (InUseException e)
		{
			addAlert(state, rb.getString("somelsis") + " " + rb.getString("submiss"));
		} // try

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_GRADE_ASSIGNMENT);
			state.setAttribute(ATTACHMENTS, EntityManager.newReferenceList());
		}

	} // grade_submission_option

	/**
	 * Action is to save the submission as a draft
	 */
	public void doSave_submission(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();

		// retrieve the submission text (as formatted text)
		boolean checkForFormattingErrors = true; // the student is submitting something - so check for errors
		String text = processFormattedTextFromBrowser(state, params.getCleanString(VIEW_SUBMISSION_TEXT), checkForFormattingErrors);

		if (text == null)
		{
			text = (String) state.getAttribute(VIEW_SUBMISSION_TEXT);
		}

		String honorPledgeYes = params.getString(VIEW_SUBMISSION_HONOR_PLEDGE_YES);
		if (honorPledgeYes == null)
		{
			honorPledgeYes = "false";
		}

		String aReference = (String) state.getAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE);
		User u = (User) state.getAttribute(STATE_USER);

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			try
			{
				Assignment a = AssignmentService.getAssignment(aReference);
				String assignmentId = a.getId();

				AssignmentSubmission submission = AssignmentService.getSubmission(aReference, u);
				if (submission != null)
				{
					// the submission already exists, change the text and honor pledge value, save as draft
					try
					{
						AssignmentSubmissionEdit edit = AssignmentService.editSubmission(submission.getReference());
						edit.setSubmittedText(text);
						edit.setHonorPledgeFlag(Boolean.valueOf(honorPledgeYes).booleanValue());
						edit.setSubmitted(false);
						// edit.addSubmitter (u);
						edit.setAssignment(a);

						// add attachments
						List attachments = (List) state.getAttribute(ATTACHMENTS);
						if (attachments != null)
						{
							// clear the old attachments first
							edit.clearSubmittedAttachments();

							// add each new attachment
							Iterator it = attachments.iterator();
							while (it.hasNext())
							{
								edit.addSubmittedAttachment((Reference) it.next());
							}
						}
						AssignmentService.commitEdit(edit);
					}
					catch (IdUnusedException e)
					{
						addAlert(state, rb.getString("cannotfin2") + " " + a.getTitle());
					}
					catch (PermissionException e)
					{
						addAlert(state, rb.getString("youarenot12"));
					}
					catch (InUseException e)
					{
						addAlert(state, rb.getString("somelsis") + " " + rb.getString("submiss"));
					}
				}
				else
				{
					// new submission, save as draft
					try
					{
						AssignmentSubmissionEdit edit = AssignmentService.addSubmission((String) state
								.getAttribute(STATE_CONTEXT_STRING), assignmentId);
						edit.setSubmittedText(text);
						edit.setHonorPledgeFlag(Boolean.valueOf(honorPledgeYes).booleanValue());
						edit.setSubmitted(false);
						// edit.addSubmitter (u);
						edit.setAssignment(a);

						// add attachments
						List attachments = (List) state.getAttribute(ATTACHMENTS);
						if (attachments != null)
						{
							// add each attachment
							Iterator it = attachments.iterator();
							while (it.hasNext())
							{
								edit.addSubmittedAttachment((Reference) it.next());
							}
						}
						AssignmentService.commitEdit(edit);
					}
					catch (PermissionException e)
					{
						addAlert(state, rb.getString("youarenot4"));
					}
				}
			}
			catch (IdUnusedException e)
			{
				addAlert(state, rb.getString("cannotfin5"));
			}
			catch (PermissionException e)
			{
				addAlert(state, "You are not allowed to view the assignment submission. ");
			}
		}

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
			state.setAttribute(ATTACHMENTS, EntityManager.newReferenceList());
		}

	} // doSave_submission

	/**
	 * Action is to post the submission
	 */
	public void doPost_submission(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();

		// retrieve the submission text (as formatted text)
		boolean checkForFormattingErrors = true; // the student is submitting something - so check for errors
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
		String aReference = (String) state.getAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE);
		Assignment a = null;
		String assignmentId = "";
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			try
			{
				a = AssignmentService.getAssignment(aReference);
				assignmentId = a.getId();

				if (a.getContent().getHonorPledge() != 1)
				{
					if (!Boolean.valueOf(honorPledgeYes).booleanValue())
					{
						addAlert(state, rb.getString("youarenot18"));
					}
					state.setAttribute(VIEW_SUBMISSION_HONOR_PLEDGE_YES, honorPledgeYes);
				}

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
					Vector v = (Vector) state.getAttribute(ATTACHMENTS);
					if ((v == null) || (v.size() == 0))
					{
						addAlert(state, rb.getString("youmust1"));
					}
				}
				else if (submissionType == 3)
				{
					// for the inline and attachment submission
					Vector v = (Vector) state.getAttribute(ATTACHMENTS);
					if ((text.length() == 0) && ((v == null) || (v.size() == 0)))
					{
						addAlert(state, rb.getString("youmust2"));
					}
				}
			}
			catch (IdUnusedException e)
			{
				addAlert(state, rb.getString("cannotfin2"));
			}
			catch (PermissionException e)
			{
				addAlert(state, rb.getString("youarenot14"));
			}
		}

		if ((state.getAttribute(STATE_MESSAGE) == null) && (a != null))
		{
			try
			{
				AssignmentSubmission submission = AssignmentService.getSubmission(a.getReference(), u);
				if (submission != null)
				{
					// the submission already exists, change the text and honor pledge value, post it
					try
					{
						AssignmentSubmissionEdit sEdit = AssignmentService.editSubmission(submission.getReference());
						sEdit.setSubmittedText(text);
						sEdit.setHonorPledgeFlag(Boolean.valueOf(honorPledgeYes).booleanValue());
						sEdit.setTimeSubmitted(TimeService.newTime());
						sEdit.setSubmitted(true);

						// for resubmissions
						// when resubmit, keep the Returned flag on till the instructor grade again.
						if (sEdit.getReturned())
						{
							ResourcePropertiesEdit sPropertiesEdit = sEdit.getPropertiesEdit();
							if (sEdit.getGraded())
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
											String[] grades = StringUtil.split(previousGrades, " ");
											String newGrades = "";
											for (int jj = 0; jj < grades.length; jj++)
											{
												String grade = grades[jj];
												if (grade.indexOf(".") == -1)
												{
													// show the grade with decimal point
													grade = grade.concat(".0");
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
								previousGrades = previousGrades.concat(sEdit.getGradeDisplay() + " ");

								sPropertiesEdit.addProperty(ResourceProperties.PROP_SUBMISSION_SCALED_PREVIOUS_GRADES,
										previousGrades);

								// clear the current grade and make the submission ungraded
								sEdit.setGraded(false);
								sEdit.setGrade("");
								sEdit.setGradeReleased(false);
							}

							// keep the history of assignment feed back text
							String feedbackTextHistory = sPropertiesEdit
									.getProperty(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT) != null ? sPropertiesEdit
									.getProperty(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT)
									: "";
							feedbackTextHistory = sEdit.getFeedbackText() + "\n" + feedbackTextHistory;
							sPropertiesEdit.addProperty(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT,
									feedbackTextHistory);

							// keep the history of assignment feed back comment
							String feedbackCommentHistory = sPropertiesEdit
									.getProperty(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT) != null ? sPropertiesEdit
									.getProperty(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT)
									: "";
							feedbackCommentHistory = sEdit.getFeedbackComment() + "\n" + feedbackCommentHistory;
							sPropertiesEdit.addProperty(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT,
									feedbackCommentHistory);

							// reset the previous grading context
							sEdit.setFeedbackText("");
							sEdit.setFeedbackComment("");
							sEdit.clearFeedbackAttachments();

							sPropertiesEdit.removeProperty(GRADE_SUBMISSION_ALLOW_RESUBMIT);
						}
						// sEdit.addSubmitter (u);
						sEdit.setAssignment(a);

						// add attachments
						List attachments = (List) state.getAttribute(ATTACHMENTS);
						if (attachments != null)
						{
							// clear the old attachments first
							sEdit.clearSubmittedAttachments();

							// add each new attachment
							Iterator it = attachments.iterator();
							while (it.hasNext())
							{
								sEdit.addSubmittedAttachment((Reference) it.next());
							}
						}

						AssignmentService.commitEdit(sEdit);
					}
					catch (IdUnusedException e)
					{
						addAlert(state, rb.getString("cannotfin2") + " " + a.getTitle());
					}
					catch (PermissionException e)
					{
						addAlert(state, "You do not have permission to edit the submission. ");
					}
					catch (InUseException e)
					{
						addAlert(state, rb.getString("somelsis") + " " + rb.getString("submiss"));
					}
				}
				else
				{
					// new submission, post it
					try
					{
						AssignmentSubmissionEdit edit = AssignmentService.addSubmission((String) state
								.getAttribute(STATE_CONTEXT_STRING), assignmentId);
						edit.setSubmittedText(text);
						edit.setHonorPledgeFlag(Boolean.valueOf(honorPledgeYes).booleanValue());
						edit.setTimeSubmitted(TimeService.newTime());
						edit.setSubmitted(true);
						// edit.addSubmitter (u);
						edit.setAssignment(a);

						// add attachments
						List attachments = (List) state.getAttribute(ATTACHMENTS);
						if (attachments != null)
						{
							// add each attachment
							Iterator it = attachments.iterator();
							while (it.hasNext())
							{
								edit.addSubmittedAttachment((Reference) it.next());
							}
						}

						AssignmentService.commitEdit(edit);
					}
					catch (PermissionException e)
					{
						addAlert(state, rb.getString("youarenot13"));
					}
				} // if -else
			}
			catch (IdUnusedException e)
			{
				addAlert(state, rb.getString("cannotfin5"));
			}
			catch (PermissionException e)
			{
				addAlert(state, "You are not allowed to view the assignment submission. ");
			}

		} // if

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
			state.setAttribute(ATTACHMENTS, EntityManager.newReferenceList());
		}

	} // doPost_submission

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
				resetAssignment(state);
				state.setAttribute(ATTACHMENTS, EntityManager.newReferenceList());
				state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_NEW_EDIT_ASSIGNMENT);
			}
			else
			{
				addAlert(state, rb.getString("youarenot2"));
				state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
			}
		}

	} // doNew_Assignment

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

		// put the input value into the state attributes
		String title = params.getString(NEW_ASSIGNMENT_TITLE);
		state.setAttribute(NEW_ASSIGNMENT_TITLE, title);

		if (title.length() == 0)
		{
			// empty assignment title
			addAlert(state, rb.getString("plespethe1"));
		}

		// open time
		int openMonth = (new Integer(params.getString(NEW_ASSIGNMENT_OPENMONTH))).intValue();
		state.setAttribute(NEW_ASSIGNMENT_OPENMONTH, new Integer(openMonth));
		int openDay = (new Integer(params.getString(NEW_ASSIGNMENT_OPENDAY))).intValue();
		state.setAttribute(NEW_ASSIGNMENT_OPENDAY, new Integer(openDay));
		int openYear = (new Integer(params.getString(NEW_ASSIGNMENT_OPENYEAR))).intValue();
		state.setAttribute(NEW_ASSIGNMENT_OPENYEAR, new Integer(openYear));
		int openHour = (new Integer(params.getString(NEW_ASSIGNMENT_OPENHOUR))).intValue();
		state.setAttribute(NEW_ASSIGNMENT_OPENHOUR, new Integer(openHour));
		int openMin = (new Integer(params.getString(NEW_ASSIGNMENT_OPENMIN))).intValue();
		state.setAttribute(NEW_ASSIGNMENT_OPENMIN, new Integer(openMin));
		String openAMPM = params.getString(NEW_ASSIGNMENT_OPENAMPM);
		state.setAttribute(NEW_ASSIGNMENT_OPENAMPM, openAMPM);
		if ((openAMPM.equals("PM")) && (openHour != 12))
		{
			openHour = openHour + 12;
		}
		if ((openHour == 12) && (openAMPM.equals("AM")))
		{
			openHour = 0;
		}
		Time openTime = TimeService.newTimeLocal(openYear, openMonth, openDay, openHour, openMin, 0, 0);
		// validate date
		if (!Validator.checkDate(openDay, openMonth, openYear))
		{
			addAlert(state, rb.getString("date.invalid") + rb.getString("date.opendate") + ".");
		}

		// due time
		int dueMonth = (new Integer(params.getString(NEW_ASSIGNMENT_DUEMONTH))).intValue();
		state.setAttribute(NEW_ASSIGNMENT_DUEMONTH, new Integer(dueMonth));
		int dueDay = (new Integer(params.getString(NEW_ASSIGNMENT_DUEDAY))).intValue();
		state.setAttribute(NEW_ASSIGNMENT_DUEDAY, new Integer(dueDay));
		int dueYear = (new Integer(params.getString(NEW_ASSIGNMENT_DUEYEAR))).intValue();
		state.setAttribute(NEW_ASSIGNMENT_DUEYEAR, new Integer(dueYear));
		int dueHour = (new Integer(params.getString(NEW_ASSIGNMENT_DUEHOUR))).intValue();
		state.setAttribute(NEW_ASSIGNMENT_DUEHOUR, new Integer(dueHour));
		int dueMin = (new Integer(params.getString(NEW_ASSIGNMENT_DUEMIN))).intValue();
		state.setAttribute(NEW_ASSIGNMENT_DUEMIN, new Integer(dueMin));
		String dueAMPM = params.getString(NEW_ASSIGNMENT_DUEAMPM);
		state.setAttribute(NEW_ASSIGNMENT_DUEAMPM, dueAMPM);
		if ((dueAMPM.equals("PM")) && (dueHour != 12))
		{
			dueHour = dueHour + 12;
		}
		if ((dueHour == 12) && (dueAMPM.equals("AM")))
		{
			dueHour = 0;
		}
		Time dueTime = TimeService.newTimeLocal(dueYear, dueMonth, dueDay, dueHour, dueMin, 0, 0);
		// validate date
		if (!Validator.checkDate(dueDay, dueMonth, dueYear))
		{
			addAlert(state, rb.getString("date.invalid") + rb.getString("date.duedate") + ".");
		}

		state.setAttribute(NEW_ASSIGNMENT_ENABLECLOSEDATE, new Boolean(true));

		// close time
		int closeMonth = (new Integer(params.getString(NEW_ASSIGNMENT_CLOSEMONTH))).intValue();
		state.setAttribute(NEW_ASSIGNMENT_CLOSEMONTH, new Integer(closeMonth));
		int closeDay = (new Integer(params.getString(NEW_ASSIGNMENT_CLOSEDAY))).intValue();
		state.setAttribute(NEW_ASSIGNMENT_CLOSEDAY, new Integer(closeDay));
		int closeYear = (new Integer(params.getString(NEW_ASSIGNMENT_CLOSEYEAR))).intValue();
		state.setAttribute(NEW_ASSIGNMENT_CLOSEYEAR, new Integer(closeYear));
		int closeHour = (new Integer(params.getString(NEW_ASSIGNMENT_CLOSEHOUR))).intValue();
		state.setAttribute(NEW_ASSIGNMENT_CLOSEHOUR, new Integer(closeHour));
		int closeMin = (new Integer(params.getString(NEW_ASSIGNMENT_CLOSEMIN))).intValue();
		state.setAttribute(NEW_ASSIGNMENT_CLOSEMIN, new Integer(closeMin));
		String closeAMPM = params.getString(NEW_ASSIGNMENT_CLOSEAMPM);
		state.setAttribute(NEW_ASSIGNMENT_CLOSEAMPM, closeAMPM);
		if ((closeAMPM.equals("PM")) && (closeHour != 12))
		{
			closeHour = closeHour + 12;
		}
		if ((closeHour == 12) && (closeAMPM.equals("AM")))
		{
			closeHour = 0;
		}
		// validate date
		if (!Validator.checkDate(closeDay, closeMonth, closeYear))
		{
			addAlert(state, rb.getString("date.invalid") + rb.getString("date.closedate") + ".");
		}

		// SECTION MOD
		String sections_string = "";
		String mode = (String) state.getAttribute(STATE_MODE);
		if (mode == null) mode = "";

		state.setAttribute(NEW_ASSIGNMENT_SECTION, sections_string);
		state.setAttribute(NEW_ASSIGNMENT_SUBMISSION_TYPE, new Integer(params.getString(NEW_ASSIGNMENT_SUBMISSION_TYPE)));

		int gradeType = -1;

		// grade type and grade points
		if (state.getAttribute(WITH_GRADES) != null && ((Boolean) state.getAttribute(WITH_GRADES)).booleanValue())
		{
			gradeType = Integer.parseInt(params.getString(NEW_ASSIGNMENT_GRADE_TYPE));
			state.setAttribute(NEW_ASSIGNMENT_GRADE_TYPE, new Integer(gradeType));
		}

		// treat the new assignment description as formatted text
		boolean checkForFormattingErrors = true; // instructor is creating a new assignment - so check for errors
		String description = processFormattedTextFromBrowser(state, params.getCleanString(NEW_ASSIGNMENT_DESCRIPTION),
				checkForFormattingErrors);
		state.setAttribute(NEW_ASSIGNMENT_DESCRIPTION, description);

		if (params.getString(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE) != null
				&& params.getString(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE).equalsIgnoreCase(Boolean.TRUE.toString()))
		{
			state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE, Boolean.TRUE.toString());
		}
		else
		{
			state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE, Boolean.FALSE.toString());
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

		if (params.getString(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK) != null
				&& params.getString(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK).equalsIgnoreCase(Boolean.TRUE.toString()))
		{
			state.setAttribute(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, Boolean.TRUE.toString());
			if (gradeType != Assignment.SCORE_GRADE_TYPE)
			{
				// gradebook integration only available to point-grade assignment
				addAlert(state, rb.getString("addtogradebook.wrongGradeScale"));
			}
		}
		else
		{
			state.setAttribute(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, Boolean.FALSE.toString());
		}

		List attachments = (List) state.getAttribute(ATTACHMENTS);
		state.setAttribute(NEW_ASSIGNMENT_ATTACHMENT, attachments);

		// correct inputs
		// checks on the times
		if (validify && dueTime.before(openTime))
		{
			addAlert(state, rb.getString("assig3"));
		}

		if (validify)
		{
			if (((description == null) || (description.length() == 0)) && ((attachments == null || attachments.size() == 0)))
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
						// in case of releasing grade, user must specify a grade
						addAlert(state, rb.getString("plespethe2"));
					}
					else
					{
						validPointGrade(state, gradePoints);
						// when scale is points, grade must be integer and less than maximum value
						if (state.getAttribute(STATE_MESSAGE) == null)
						{
							gradePoints = scalePointGrade(state, gradePoints);
						}
						state.setAttribute(NEW_ASSIGNMENT_GRADE_POINTS, gradePoints);
					}
				}
			}
		}

		// assignment range?
		String range = data.getParameters().getString("range");
		state.setAttribute(NEW_ASSIGNMENT_RANGE, range);
		if (range.equals("groups"))
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

	} // setNewAssignmentParameters

	/**
	 * Action is to hide the preview assignment student view
	 */
	public void doHide_submission_assignment_instruction(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(GRADE_SUBMISSION_ASSIGNMENT_EXPAND_FLAG, new Boolean(false));
		
		// save user input
		readGradeForm(data, state);

	} // doHide_preview_assignment_student_view

	/**
	 * Action is to show the preview assignment student view
	 */
	public void doShow_submission_assignment_instruction(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(GRADE_SUBMISSION_ASSIGNMENT_EXPAND_FLAG, new Boolean(true));
		
		// save user input
		readGradeForm(data, state);

	} // doShow_submission_assignment_instruction

	/**
	 * Action is to hide the preview assignment student view
	 */
	public void doHide_preview_assignment_student_view(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(PREVIEW_ASSIGNMENT_STUDENT_VIEW_HIDE_FLAG, new Boolean(true));

	} // doHide_preview_assignment_student_view

	/**
	 * Action is to show the preview assignment student view
	 */
	public void doShow_preview_assignment_student_view(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(PREVIEW_ASSIGNMENT_STUDENT_VIEW_HIDE_FLAG, new Boolean(false));

	} // doShow_preview_assignment_student_view

	/**
	 * Action is to hide the preview assignment assignment infos
	 */
	public void doHide_preview_assignment_assignment(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(PREVIEW_ASSIGNMENT_ASSIGNMENT_HIDE_FLAG, new Boolean(true));

	} // doHide_preview_assignment_assignment

	/**
	 * Action is to show the preview assignment assignment info
	 */
	public void doShow_preview_assignment_assignment(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(PREVIEW_ASSIGNMENT_ASSIGNMENT_HIDE_FLAG, new Boolean(false));

	} // doShow_preview_assignment_assignment

	/**
	 * Action is to hide the assignment option
	 */
	public void doHide_assignment_option(RunData data)
	{
		setNewAssignmentParameters(data, false);
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(NEW_ASSIGNMENT_HIDE_OPTION_FLAG, new Boolean(true));
		state.setAttribute(NEW_ASSIGNMENT_FOCUS, "eventSubmit_doShow_assignment_option");

	} // doHide_assignment_option

	/**
	 * Action is to show the assignment option
	 */
	public void doShow_assignment_option(RunData data)
	{
		setNewAssignmentParameters(data, false);
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(NEW_ASSIGNMENT_HIDE_OPTION_FLAG, new Boolean(false));
		state.setAttribute(NEW_ASSIGNMENT_FOCUS, NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE);

	} // doShow_assignment_option

	/**
	 * Action is to hide the assignment content in the view assignment page
	 */
	public void doHide_view_assignment(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(VIEW_ASSIGNMENT_HIDE_ASSIGNMENT_FLAG, new Boolean(true));

	} // doHide_view_assignment

	/**
	 * Action is to show the assignment content in the view assignment page
	 */
	public void doShow_view_assignment(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(VIEW_ASSIGNMENT_HIDE_ASSIGNMENT_FLAG, new Boolean(false));

	} // doShow_view_assignment

	/**
	 * Action is to hide the student view in the view assignment page
	 */
	public void doHide_view_student_view(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(VIEW_ASSIGNMENT_HIDE_STUDENT_VIEW_FLAG, new Boolean(true));

	} // doHide_view_student_view

	/**
	 * Action is to show the student view in the view assignment page
	 */
	public void doShow_view_student_view(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(VIEW_ASSIGNMENT_HIDE_STUDENT_VIEW_FLAG, new Boolean(false));

	} // doShow_view_student_view

	/**
	 * Action is to post assignment
	 */
	public void doPost_assignment(RunData data)
	{
		// post assignment
		postOrSaveAssignment(data, "post");

	} // doPost_assignment

	/**
	 * post or save assignment
	 */
	private void postOrSaveAssignment(RunData data, String postOrSave)
	{
		boolean post = (postOrSave != null && postOrSave.equals("post")) ? true : false;
		
		// assignment old title
		String aOldTitle = null;
		
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		String mode = (String) state.getAttribute(STATE_MODE);
		if (!mode.equals(MODE_INSTRUCTOR_PREVIEW_ASSIGNMENT))
		{
			// read input data if the mode is not preview mode
			setNewAssignmentParameters(data, true);
		}

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			ParameterParser params = data.getParameters();

			// put the names and values into vm file
			String title = (String) state.getAttribute(NEW_ASSIGNMENT_TITLE);

			// open time
			int openMonth = ((Integer) state.getAttribute(NEW_ASSIGNMENT_OPENMONTH)).intValue();
			int openDay = ((Integer) state.getAttribute(NEW_ASSIGNMENT_OPENDAY)).intValue();
			int openYear = ((Integer) state.getAttribute(NEW_ASSIGNMENT_OPENYEAR)).intValue();
			int openHour = ((Integer) state.getAttribute(NEW_ASSIGNMENT_OPENHOUR)).intValue();
			int openMin = ((Integer) state.getAttribute(NEW_ASSIGNMENT_OPENMIN)).intValue();
			String openAMPM = (String) state.getAttribute(NEW_ASSIGNMENT_OPENAMPM);
			if ((openAMPM.equals("PM")) && (openHour != 12))
			{
				openHour = openHour + 12;
			}
			if ((openHour == 12) && (openAMPM.equals("AM")))
			{
				openHour = 0;
			}
			Time openTime = TimeService.newTimeLocal(openYear, openMonth, openDay, openHour, openMin, 0, 0);

			// due time
			int dueMonth = ((Integer) state.getAttribute(NEW_ASSIGNMENT_DUEMONTH)).intValue();
			int dueDay = ((Integer) state.getAttribute(NEW_ASSIGNMENT_DUEDAY)).intValue();
			int dueYear = ((Integer) state.getAttribute(NEW_ASSIGNMENT_DUEYEAR)).intValue();
			int dueHour = ((Integer) state.getAttribute(NEW_ASSIGNMENT_DUEHOUR)).intValue();
			int dueMin = ((Integer) state.getAttribute(NEW_ASSIGNMENT_DUEMIN)).intValue();
			String dueAMPM = (String) state.getAttribute(NEW_ASSIGNMENT_DUEAMPM);
			if ((dueAMPM.equals("PM")) && (dueHour != 12))
			{
				dueHour = dueHour + 12;
			}
			if ((dueHour == 12) && (dueAMPM.equals("AM")))
			{
				dueHour = 0;
			}
			Time dueTime = TimeService.newTimeLocal(dueYear, dueMonth, dueDay, dueHour, dueMin, 0, 0);

			// checks on the times
			if (dueTime.before(openTime))
			{
				addAlert(state, rb.getString("assig3"));
			}

			// close time
			Time closeTime = TimeService.newTime();
			boolean enableCloseDate = ((Boolean) state.getAttribute(NEW_ASSIGNMENT_ENABLECLOSEDATE)).booleanValue();
			if (enableCloseDate)
			{
				int closeMonth = ((Integer) state.getAttribute(NEW_ASSIGNMENT_CLOSEMONTH)).intValue();
				int closeDay = ((Integer) state.getAttribute(NEW_ASSIGNMENT_CLOSEDAY)).intValue();
				int closeYear = ((Integer) state.getAttribute(NEW_ASSIGNMENT_CLOSEYEAR)).intValue();
				int closeHour = ((Integer) state.getAttribute(NEW_ASSIGNMENT_CLOSEHOUR)).intValue();
				int closeMin = ((Integer) state.getAttribute(NEW_ASSIGNMENT_CLOSEMIN)).intValue();
				String closeAMPM = (String) state.getAttribute(NEW_ASSIGNMENT_CLOSEAMPM);
				if ((closeAMPM.equals("PM")) && (closeHour != 12))
				{
					closeHour = closeHour + 12;
				}
				if ((closeHour == 12) && (closeAMPM.equals("AM")))
				{
					closeHour = 0;
				}
				closeTime = TimeService.newTimeLocal(closeYear, closeMonth, closeDay, closeHour, closeMin, 0, 0);

				// if there is a close time, check it
				if (closeTime.before(openTime))
				{
					addAlert(state, rb.getString("acesubdea3"));
				}
				if (closeTime.before(dueTime))
				{
					addAlert(state, rb.getString("acesubdea2"));
				}
			}

			// sections
			String s = (String) state.getAttribute(NEW_ASSIGNMENT_SECTION);

			int submissionType = ((Integer) state.getAttribute(NEW_ASSIGNMENT_SUBMISSION_TYPE)).intValue();

			int gradeType = ((Integer) state.getAttribute(NEW_ASSIGNMENT_GRADE_TYPE)).intValue();

			String gradePoints = (String) state.getAttribute(NEW_ASSIGNMENT_GRADE_POINTS);

			String description = (String) state.getAttribute(NEW_ASSIGNMENT_DESCRIPTION);

			String checkAddDueTime = (String) state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE);

			String checkAutoAnnounce = (String) state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE);

			String checkAddHonorPledge = (String) state.getAttribute(NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE);

			String addtoGradebook = (String) state.getAttribute(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK);

			// the attachments
			List attachments = (List) state.getAttribute(ATTACHMENTS);
			List attachments1 = EntityManager.newReferenceList(attachments);

			boolean newAssignment = true;

			// correct inputs
			if (state.getAttribute(STATE_MESSAGE) == null)
			{
				String assignmentId = params.getString("assignmentId");
				String assignmentContentId = params.getString("assignmentContentId");

				// AssignmentContent object
				AssignmentContentEdit ac = null;
				if (assignmentContentId.length() == 0)
				{
					// new assignment
					// only show alert when dealing with new assignment
					// allow editing assignment after due date
					if (dueTime.before(TimeService.newTime()))
					{
						addAlert(state, rb.getString("assig4"));
					}
					else
					{
						try
						{
							ac = AssignmentService.addAssignmentContent((String) state.getAttribute(STATE_CONTEXT_STRING));
						}
						catch (PermissionException e)
						{
							addAlert(state, rb.getString("youarenot3"));
						}
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
						addAlert(state, rb.getString("theassicon"));
					}
					catch (IdUnusedException e)
					{
						addAlert(state, rb.getString("cannotfin4"));
					}
					catch (PermissionException e)
					{
						addAlert(state, rb.getString("youarenot15"));
					}

				}

				// Assignment
				AssignmentEdit a = null;
				if (assignmentId.length() == 0)
				{
					// create a new assignment
					try
					{
						a = AssignmentService.addAssignment((String) state.getAttribute(STATE_CONTEXT_STRING));
					}
					catch (PermissionException e)
					{
						addAlert(state, rb.getString("youarenot1"));
					}
				}
				else
				{
					// not a new assignment
					newAssignment = false;

					try
					{
						// edit assignment
						a = AssignmentService.editAssignment(assignmentId);
						aOldTitle = a.getTitle();
					}
					catch (InUseException e)
					{
						addAlert(state, rb.getString("theassicon"));
					}
					catch (IdUnusedException e)
					{
						addAlert(state, rb.getString("cannotfin3"));
					}
					catch (PermissionException e)
					{
						addAlert(state, rb.getString("youarenot14"));
					} // try-catch
				} // if-else

				if ((state.getAttribute(STATE_MESSAGE) == null) && (ac != null) && (a != null))
				{
					ac.setTitle(title);
					ac.setInstructions(description);
					ac.setHonorPledge(Integer.parseInt(checkAddHonorPledge));
					ac.setTypeOfSubmission(submissionType);
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

					// add each attachment
					Iterator it = EntityManager.newReferenceList(attachments1).iterator();
					while (it.hasNext())
					{
						Reference r = (Reference) it.next();
						ac.addAttachment(r);
					}
					state.setAttribute(ATTACHMENTS_MODIFIED, new Boolean(false));

					// commit the changes
					AssignmentService.commitEdit(ac);

				}
				
				try
				{
					a.setTitle(title);
					a.setContent(ac);
					a.setContext((String) state.getAttribute(STATE_CONTEXT_STRING));
					a.setSection(s);
					// old open time
					Time oldOpenTime = a.getOpenTime();
					a.setOpenTime(openTime);
					// old due time
					Time oldDueTime = a.getDueTime();
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

					// set the auto check/auto announce property
					ResourcePropertiesEdit aPropertiesEdit = a.getPropertiesEdit();
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
					aPropertiesEdit.addProperty(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE, checkAddDueTime);
					aPropertiesEdit.addProperty(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE, checkAutoAnnounce);
					aPropertiesEdit.addProperty(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, addtoGradebook);

					// set group property
					Site site = null;
					try
					{
						site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
					}
					catch (Exception e)
					{
						if (Log.getLogger("chef").isDebugEnabled())
							Log.debug("chef", this + "doPost_assignment(): cannot find site with id "
									+ ToolManager.getCurrentPlacement().getContext());
					}
					String range = (String) state.getAttribute(NEW_ASSIGNMENT_RANGE);
					if (range.equals("site"))
					{
						a.setAccess(Assignment.AssignmentAccess.SITE);
						if (site != null)
						{
							for (Iterator aGroups = a.getGroups().iterator(); aGroups.hasNext();)
							{
								try
								{
									a.removeGroup(site.getGroup((String) aGroups.next()));
								}
								catch (PermissionException e)
								{

								}
							}
						}
					}
					else if (range.equals("groups"))
					{
						Collection groupChoice = (Collection) state.getAttribute(NEW_ASSIGNMENT_GROUPS);

						// if group has been dropped, remove it from assignment
						for (Iterator oSIterator = a.getGroups().iterator(); oSIterator.hasNext();)
						{
							Reference oGRef = EntityManager.newReference((String) oSIterator.next());
							boolean selected = false;
							for (Iterator gIterator = groupChoice.iterator(); gIterator.hasNext() && !selected;)
							{
								if (oGRef.getId().equals((String) gIterator.next()))
								{
									selected = true;
								}
							}
							if (!selected && site != null)
							{
								try
								{
									a.removeGroup(site.getGroup(oGRef.getId()));
								}
								catch (Exception ignore)
								{
									if (Log.getLogger("chef").isDebugEnabled())
										Log.debug("chef", this + "doPost_assignment(): cannot remove group " + oGRef.getId());
								}
							}
						}

						// add group to assignment
						if (groupChoice != null)
						{
							a.setAccess(Assignment.AssignmentAccess.GROUPED);
							for (Iterator gIterator2 = groupChoice.iterator(); gIterator2.hasNext();)
							{
								String gString = (String) gIterator2.next();
								try
								{
									a.addGroup(site.getGroup(gString));
								}
								catch (Exception eIgnore)
								{
									if (Log.getLogger("chef").isDebugEnabled())
										Log.debug("chef", this + "doPost_assignment(): cannot add group " + gString);
								}
							}
						}
					}

					if (state.getAttribute(STATE_MESSAGE) == null)
					{
						// commit assignment first
						AssignmentService.commitEdit(a);

						if (post)
						{
							// add due date to schedule and add open date to announcement only if user is posting the assignment

							// add the due date to schedule
							Calendar c = (Calendar) state.getAttribute(CALENDAR);
							String dueDateScheduled = a.getProperties().getProperty(NEW_ASSIGNMENT_DUE_DATE_SCHEDULED);
							boolean dueDateEventModified = false;
							CalendarEvent e = null;

							if (dueDateScheduled != null && dueDateScheduled.equalsIgnoreCase(Boolean.TRUE.toString()))
							{
								// find the old event
								boolean found = false;
								String oldEventId = aPropertiesEdit
										.getProperty(ResourceProperties.PROP_ASSIGNMENT_DUEDATE_CALENDAR_EVENT_ID);
								if (oldEventId != null && c != null)
								{
									try
									{
										e = c.getEvent(oldEventId);
										found = true;
									}
									catch (IdUnusedException ee)
									{
										Log.warn("chef", "The old event has been deleted: event id=" + oldEventId + ". ");
									}
									catch (PermissionException ee)
									{
										Log.warn("chef", "You do not have the permission to view the schedule event id= "
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
											if (((String) e.getDisplayName()).indexOf(rb.getString("assig1") + " " + title) != -1)
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
									if (oldDueTime != null && (!oldDueTime.toStringLocalFull().equals(dueTime.toStringLocalFull())) // due date changed
											|| !aOldTitle.equals(title) // title changed
											|| (!checkAddDueTime.equalsIgnoreCase(Boolean.TRUE.toString()))) // user choose to not schedule due date
									{
										// if the assignment due date or title has been changed, we need to update existing event
										dueDateEventModified = true;
									}

									if (dueDateEventModified && found)
									{
										// remove the founded old event
										try
										{
											c.removeEvent(c.editEvent(e.getId()));
										}
										catch (PermissionException ee)
										{
											Log.warn("chef", rb.getString("cannotrem") + " " + title + ". ");
										}
										catch (InUseException ee)
										{
											Log.warn("chef", rb.getString("somelsis") + " " + rb.getString("calen"));
										}
									}
								}
							}
							if (checkAddDueTime.equalsIgnoreCase(Boolean.TRUE.toString()))
							{
								if (c != null)
								{
									try
									{
										e = null;
										e = c.addEvent(/* TimeRange */TimeService.newTimeRange(dueTime.getTime(), /* 0 duration */
										0 * 60 * 1000),
										/* title */rb.getString("due") + " " + title,
										/* description */rb.getString("assig1") + " " + title + " " + "is due on "
												+ dueTime.toStringLocalFull() + ". ",
										/* type */rb.getString("deadl"),
										/* location */"",
										/* attachments */EntityManager.newReferenceList());
										aPropertiesEdit.addProperty(NEW_ASSIGNMENT_DUE_DATE_SCHEDULED, Boolean.TRUE.toString());
										if (e != null)
										{
											aPropertiesEdit.addProperty(
													ResourceProperties.PROP_ASSIGNMENT_DUEDATE_CALENDAR_EVENT_ID, e.getId());
										}
									}
									catch (PermissionException ee)
									{
										Log.warn("chef", rb.getString("cannotfin1"));
									} // try-catch
								} // if
							} // if

							// the open date been announced
							if (checkAutoAnnounce.equalsIgnoreCase(Boolean.TRUE.toString()))
							{
								AnnouncementChannel channel = (AnnouncementChannel) state.getAttribute(ANNOUNCEMENT_CHANNEL);
								if (channel != null)
								{
									String openDateAnnounced = a.getProperties().getProperty(NEW_ASSIGNMENT_OPEN_DATE_ANNOUNCED);

									// open date has been announced or title has been changed?
									boolean openDateMessageModified = false;
									if (openDateAnnounced != null && openDateAnnounced.equalsIgnoreCase(Boolean.TRUE.toString()))
									{
										if (oldOpenTime != null
												&& (!oldOpenTime.toStringLocalFull().equals(openTime.toStringLocalFull())) // open time changes
												|| !aOldTitle.equals(title)) // assignment title changes
										{
											// need to change message
											openDateMessageModified = true;
										}

									}

									// add the open date to annoucement
									if (openDateAnnounced == null // no announcement yet
											|| (openDateAnnounced != null
													&& openDateAnnounced.equalsIgnoreCase(Boolean.TRUE.toString()) && openDateMessageModified)) // announced, but open date or announcement title changes
									{
										// announcement channel is in place
										try
										{
											AnnouncementMessageEdit message = channel.addAnnouncementMessage();
											AnnouncementMessageHeaderEdit header = message.getAnnouncementHeaderEdit();
											header.setDraft(/* draft */false);
											header.replaceAttachments(/* attachment */EntityManager.newReferenceList());

											if (!openDateMessageModified)
											{
												header.setSubject(/* subject */rb.getString("assig6") + " " + title);
												message.setBody(/* body */rb.getString("opedat") + " "
														+ FormattedText.convertPlaintextToFormattedText(title) + " is "
														+ openTime.toStringLocalFull() + ". ");
											}
											else
											{
												header.setSubject(/* subject */rb.getString("assig5") + " " + title);
												message.setBody(/* body */rb.getString("newope") + " "
														+ FormattedText.convertPlaintextToFormattedText(title) + " is "
														+ openTime.toStringLocalFull() + ". ");
											}
											channel.commitMessage(message, NotificationService.NOTI_NONE);

											aPropertiesEdit
													.addProperty(NEW_ASSIGNMENT_OPEN_DATE_ANNOUNCED, Boolean.TRUE.toString());
											if (message != null)
											{
												aPropertiesEdit.addProperty(
														ResourceProperties.PROP_ASSIGNMENT_OPENDATE_ANNOUNCEMENT_MESSAGE_ID,
														message.getId());
											}
										}
										catch (PermissionException ee)
										{
											Log.warn("chef", rb.getString("cannotmak"));
										}
									}
								}
							} // if

							// integrate with Gradebook
							String aReference = a.getReference();
							String addUpdateRemoveAssignment = "remove";
							if (Boolean.valueOf(addtoGradebook).booleanValue())
							{
								if (!AssignmentService.getAllowGroupAssignmentsInGradebook() && (range.equals("groups")))
								{
									// if grouped assignment is not allowed to add into Gradebook
									addAlert(state, rb.getString("java.alert.noGroupedAssignmentIntoGB"));
									String ref = "";
									try
									{
										ref = a.getReference();
										AssignmentEdit aEdit = AssignmentService.editAssignment(ref);
										aEdit.getPropertiesEdit().removeProperty(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK);
										AssignmentService.commitEdit(aEdit);
									}
									catch (Exception ignore)
									{
										// ignore the exception
										Log.warn("chef", rb.getString("cannotfin2") + ref);
									}
									integrateGradebook(state, aReference, "remove", null, null, -1, null, null, null);
								}
								else
								{
									if (newAssignment)
									{
										addUpdateRemoveAssignment = "add";
									}
									else
									{
										addUpdateRemoveAssignment = "update";
									}
	
									if (!addUpdateRemoveAssignment.equals("remove") && gradeType == 3)
									{
										try
										{
											// no assignment committed yet. Use user input data
											integrateGradebook(state, aReference, addUpdateRemoveAssignment, aOldTitle, title, Integer.parseInt (gradePoints), dueTime, null, null);
											
											// add all existing grades, if any, into Gradebook
											integrateGradebook(state, aReference, null, null, null, -1, null, null, "update");
										}
										catch (NumberFormatException nE)
										{
											alertInvalidPoint(state, gradePoints);
										}
									}
									else
									{
										integrateGradebook(state, aReference, "remove", null, null, -1, null, null, null);
									}
								}
							}

							state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);

							state.setAttribute(ATTACHMENTS, EntityManager.newReferenceList());
							resetAssignment(state);
						}
					} // if
				}
				catch (IdUnusedException e)
				{
					addAlert(state, rb.getString("cannotfin3"));
				} // try-catch

			} // if

		} // if

	} // doPost_assignment

	/**
	 * Action is to post new assignment
	 */
	public void doSave_assignment(RunData data)
	{
		postOrSaveAssignment(data, "save");

	} // doSave_assignment

	/**
	 * Action is to preview the selected assignment
	 */
	public void doPreview_assignment(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		setNewAssignmentParameters(data, false);

		String assignmentId = data.getParameters().getString("assignmentId");
		state.setAttribute(PREVIEW_ASSIGNMENT_ASSIGNMENT_ID, assignmentId);

		String assignmentContentId = data.getParameters().getString("assignmentContentId");
		state.setAttribute(PREVIEW_ASSIGNMENT_ASSIGNMENTCONTENT_ID, assignmentContentId);

		state.setAttribute(PREVIEW_ASSIGNMENT_ASSIGNMENT_HIDE_FLAG, new Boolean(false));
		state.setAttribute(PREVIEW_ASSIGNMENT_STUDENT_VIEW_HIDE_FLAG, new Boolean(true));
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
		state.setAttribute(VIEW_ASSIGNMENT_HIDE_ASSIGNMENT_FLAG, new Boolean(false));
		// show the student view portion
		state.setAttribute(VIEW_ASSIGNMENT_HIDE_STUDENT_VIEW_FLAG, new Boolean(true));

		String assignmentId = params.getString("assignmentId");
		state.setAttribute(VIEW_ASSIGNMENT_ID, assignmentId);

		try
		{
			AssignmentService.getAssignment(assignmentId);
		}
		catch (IdUnusedException e)
		{
			addAlert(state, rb.getString("cannotfin3"));
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getString("youarenot14"));
		}

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

		String assignmentId = StringUtil.trimToNull(params.getString("assignmentId"));
		// whether the user can modify the assignment
		state.setAttribute(EDIT_ASSIGNMENT_ID, assignmentId);

		try
		{
			Assignment a = AssignmentService.getAssignment(assignmentId);
			Iterator submissions = AssignmentService.getSubmissions(a);
			if (submissions.hasNext())
			{
				// if there is submission to the assignment, show the alert
				addAlert(state, rb.getString("assig1") + " " + a.getTitle() + " " + rb.getString("hassum"));
			}

			// SECTION MOD
			state.setAttribute(STATE_SECTION_STRING, a.getSection());

			// put the names and values into vm file
			state.setAttribute(NEW_ASSIGNMENT_TITLE, a.getTitle());
			TimeBreakdown openTime = a.getOpenTime().breakdownLocal();
			state.setAttribute(NEW_ASSIGNMENT_OPENMONTH, new Integer(openTime.getMonth()));
			state.setAttribute(NEW_ASSIGNMENT_OPENDAY, new Integer(openTime.getDay()));
			state.setAttribute(NEW_ASSIGNMENT_OPENYEAR, new Integer(openTime.getYear()));
			int openHour = openTime.getHour();
			if (openHour == 0)
			{
				// for midnight point, we mark it as 12AM
				openHour = 12;
			}
			state.setAttribute(NEW_ASSIGNMENT_OPENHOUR, new Integer((openHour > 12) ? openHour - 12 : openHour));
			state.setAttribute(NEW_ASSIGNMENT_OPENMIN, new Integer(openTime.getMin()));
			if (((String) a.getOpenTime().toStringLocalFull()).indexOf("pm") != -1)
			{
				state.setAttribute(NEW_ASSIGNMENT_OPENAMPM, "PM");
			}
			else
			{
				state.setAttribute(NEW_ASSIGNMENT_OPENAMPM, "AM");
			}

			TimeBreakdown dueTime = a.getDueTime().breakdownLocal();
			state.setAttribute(NEW_ASSIGNMENT_DUEMONTH, new Integer(dueTime.getMonth()));
			state.setAttribute(NEW_ASSIGNMENT_DUEDAY, new Integer(dueTime.getDay()));
			state.setAttribute(NEW_ASSIGNMENT_DUEYEAR, new Integer(dueTime.getYear()));
			int dueHour = dueTime.getHour();
			if (dueHour == 0)
			{
				// for midnight point, we mark it as 12AM
				dueHour = 12;
			}
			state.setAttribute(NEW_ASSIGNMENT_DUEHOUR, new Integer((dueHour > 12) ? dueHour - 12 : dueHour));
			state.setAttribute(NEW_ASSIGNMENT_DUEMIN, new Integer(dueTime.getMin()));
			if (((String) a.getDueTime().toStringLocalFull()).indexOf("pm") != -1)
			{
				state.setAttribute(NEW_ASSIGNMENT_DUEAMPM, "PM");
			}
			else
			{
				state.setAttribute(NEW_ASSIGNMENT_DUEAMPM, "AM");
			}
			// generate alert when editing an assignment past due date
			if (a.getDueTime().before(TimeService.newTime()))
			{
				addAlert(state, rb.getString("youarenot17"));
			}

			if (a.getCloseTime() != null)
			{
				state.setAttribute(NEW_ASSIGNMENT_ENABLECLOSEDATE, new Boolean(true));
				TimeBreakdown closeTime = a.getCloseTime().breakdownLocal();
				state.setAttribute(NEW_ASSIGNMENT_CLOSEMONTH, new Integer(closeTime.getMonth()));
				state.setAttribute(NEW_ASSIGNMENT_CLOSEDAY, new Integer(closeTime.getDay()));
				state.setAttribute(NEW_ASSIGNMENT_CLOSEYEAR, new Integer(closeTime.getYear()));
				int closeHour = closeTime.getHour();
				if (closeHour == 0)
				{
					// for the midnight point, we mark it as 12 AM
					closeHour = 12;
				}
				state.setAttribute(NEW_ASSIGNMENT_CLOSEHOUR, new Integer((closeHour > 12) ? closeHour - 12 : closeHour));
				state.setAttribute(NEW_ASSIGNMENT_CLOSEMIN, new Integer(closeTime.getMin()));
				if (((String) a.getCloseTime().toStringLocalFull()).indexOf("pm") != -1)
				{
					state.setAttribute(NEW_ASSIGNMENT_CLOSEAMPM, "PM");
				}
				else
				{
					state.setAttribute(NEW_ASSIGNMENT_CLOSEAMPM, "AM");
				}
			}
			else
			{
				state.setAttribute(NEW_ASSIGNMENT_ENABLECLOSEDATE, new Boolean(false));
				state.setAttribute(NEW_ASSIGNMENT_CLOSEMONTH, state.getAttribute(NEW_ASSIGNMENT_DUEMONTH));
				state.setAttribute(NEW_ASSIGNMENT_CLOSEDAY, state.getAttribute(NEW_ASSIGNMENT_DUEDAY));
				state.setAttribute(NEW_ASSIGNMENT_CLOSEYEAR, state.getAttribute(NEW_ASSIGNMENT_DUEYEAR));
				state.setAttribute(NEW_ASSIGNMENT_CLOSEHOUR, state.getAttribute(NEW_ASSIGNMENT_DUEHOUR));
				state.setAttribute(NEW_ASSIGNMENT_CLOSEMIN, state.getAttribute(NEW_ASSIGNMENT_DUEMIN));
				state.setAttribute(NEW_ASSIGNMENT_CLOSEAMPM, state.getAttribute(NEW_ASSIGNMENT_DUEAMPM));
			}
			state.setAttribute(NEW_ASSIGNMENT_SECTION, a.getSection());

			state.setAttribute(NEW_ASSIGNMENT_SUBMISSION_TYPE, new Integer(a.getContent().getTypeOfSubmission()));
			int typeOfGrade = a.getContent().getTypeOfGrade();
			state.setAttribute(NEW_ASSIGNMENT_GRADE_TYPE, new Integer(typeOfGrade));
			if (typeOfGrade == 3)
			{
				state.setAttribute(NEW_ASSIGNMENT_GRADE_POINTS, a.getContent().getMaxGradePointDisplay());
			}
			state.setAttribute(NEW_ASSIGNMENT_DESCRIPTION, a.getContent().getInstructions());
			state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE, a.getProperties().getProperty(
					ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE));
			state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE, a.getProperties().getProperty(
					ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE));
			state.setAttribute(NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE, Integer.toString(a.getContent().getHonorPledge()));
			state.setAttribute(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, a.getProperties().getProperty(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK));
			state.setAttribute(ATTACHMENTS, a.getContent().getAttachments());

			// group setting
			if (a.getAccess().equals(Assignment.AssignmentAccess.SITE))
			{
				state.setAttribute(NEW_ASSIGNMENT_RANGE, "site");
			}
			else
			{
				state.setAttribute(NEW_ASSIGNMENT_RANGE, "groups");
			}

			state.setAttribute(NEW_ASSIGNMENT_GROUPS, a.getGroups());
		}
		catch (IdUnusedException e)
		{
			addAlert(state, rb.getString("cannotfin3"));
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getString("youarenot14"));
		}

		state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_NEW_EDIT_ASSIGNMENT);

	} // doEdit_Assignment

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
			Vector ids = new Vector();
			for (int i = 0; i < assignmentIds.length; i++)
			{
				String id = (String) assignmentIds[i];

				ids.add(id);
				try
				{
					Assignment a = AssignmentService.getAssignment(id);

					if (!AssignmentService.allowRemoveAssignment(id))
					{
						addAlert(state, rb.getString("youarenot9") + " " + "\"" + a.getTitle() + "\". ");
					}
				}
				catch (IdUnusedException e)
				{
					addAlert(state, rb.getString("cannotfin3"));
				}
				catch (PermissionException e)
				{
					addAlert(state, rb.getString("youarenot14"));
				}
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
		Vector ids = (Vector) state.getAttribute(DELETE_ASSIGNMENT_IDS);
		for (int i = 0; i < ids.size(); i++)
		{

			String assignmentId = (String) ids.get(i);
			try
			{
				AssignmentEdit aEdit = AssignmentService.editAssignment(assignmentId);

				String assignmentRef = aEdit.getReference();

				ResourcePropertiesEdit pEdit = aEdit.getPropertiesEdit();
				String title = aEdit.getTitle();

				// remove releted event if there is one
				String isThereEvent = pEdit.getProperty(NEW_ASSIGNMENT_DUE_DATE_SCHEDULED);
				if (isThereEvent != null && isThereEvent.equals(Boolean.TRUE.toString()))
				{
					// remove the associated calender event
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
							}
							catch (PermissionException ee)
							{
							}
						}
						else
						{
							TimeBreakdown b = aEdit.getDueTime().breakdownLocal();
							// TODO: check- this was new Time(year...), not local! -ggolden
							Time startTime = TimeService.newTimeLocal(b.getYear(), b.getMonth(), b.getDay(), 0, 0, 0, 0);
							Time endTime = TimeService.newTimeLocal(b.getYear(), b.getMonth(), b.getDay(), 23, 59, 59, 999);
							Iterator events = c.getEvents(TimeService.newTimeRange(startTime, endTime), null).iterator();
							while ((!found) && (events.hasNext()))
							{
								e = (CalendarEvent) events.next();
								if (((String) e.getDisplayName()).indexOf(rb.getString("assig1") + " " + title) != -1)
								{
									found = true;
								}
							}
						}
						// remove the founded old event
						if (found)
						{
							// found the old event delete it
							try
							{
								c.removeEvent(c.editEvent(e.getId()));
								pEdit.removeProperty(NEW_ASSIGNMENT_DUE_DATE_SCHEDULED);
								pEdit.removeProperty(ResourceProperties.PROP_ASSIGNMENT_DUEDATE_CALENDAR_EVENT_ID);
							}
							catch (PermissionException ee)
							{
								// ignore exception
							}
							catch (InUseException ee)
							{
								// ignore exception
							}
						}
					}
				} // if-else

				if (!AssignmentService.getSubmissions(aEdit).hasNext())
				{
					// there is no submission to this assignment yet, delete the assignment record completely
					try
					{
						AssignmentService.removeAssignment(aEdit);
					}
					catch (PermissionException e)
					{
						addAlert(state, rb.getString("youarenot11") + " " + aEdit.getTitle() + ". ");
					}
				}
				else
				{
					// remove the assignment by marking the remove status property true
					pEdit.addProperty(ResourceProperties.PROP_ASSIGNMENT_DELETED, Boolean.TRUE.toString());

					AssignmentService.commitEdit(aEdit);
				}

				// remove from Gradebook
				integrateGradebook(state, (String) ids.get (i), "remove", null, null, -1, null, null, null);
			}
			catch (InUseException e)
			{
				addAlert(state, rb.getString("somelsis") + " " + rb.getString("assig2"));
			}
			catch (IdUnusedException e)
			{
				addAlert(state, rb.getString("cannotfin3"));
			}
			catch (PermissionException e)
			{
				addAlert(state, rb.getString("youarenot6"));
			}
		} // for

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute(DELETE_ASSIGNMENT_IDS, new Vector());

			state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
		}

	} // doDelete_Assignment

	/**
	 * Action is to delete the assignment and also the related AssignmentSubmission
	 */
	public void doDeep_delete_assignment(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// get the delete assignment ids
		Vector ids = (Vector) state.getAttribute(DELETE_ASSIGNMENT_IDS);
		for (int i = 0; i < ids.size(); i++)
		{
			String currentId = (String) ids.get(i);
			try
			{
				AssignmentEdit a = AssignmentService.editAssignment(currentId);
				try
				{
					AssignmentService.removeAssignment(a);
				}
				catch (PermissionException e)
				{
					addAlert(state, rb.getString("youarenot11") + " " + a.getTitle() + ". ");
				}
			}
			catch (IdUnusedException e)
			{
				addAlert(state, rb.getString("cannotfin3"));
			}
			catch (PermissionException e)
			{
				addAlert(state, rb.getString("youarenot14"));
			}
			catch (InUseException e)
			{
				addAlert(state, rb.getString("somelsis") + " " +  rb.getString("assig2"));
			}
		}
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute(DELETE_ASSIGNMENT_IDS, new Vector());
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
		String assignmentId = StringUtil.trimToNull(params.getString("assignmentId"));

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

				AssignmentService.commitEdit(aEdit);
			}
			catch (PermissionException e)
			{
				addAlert(state, rb.getString("youarenot5"));
			}
			catch (IdInvalidException e)
			{
				addAlert(state, rb.getString("theassiid") + " " + assignmentId + " " + rb.getString("isnotval"));
			}
			catch (IdUnusedException e)
			{
				addAlert(state, rb.getString("theassiid") + " " + assignmentId + " " + rb.getString("hasnotbee"));
			}
			catch (Exception e)
			{
			}

		}

	} // doDuplicate_Assignment

	/**
	 * Action is to show the grade submission screen
	 */
	public void doGrade_submission(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		ParameterParser params = data.getParameters();

		// reset the grade assignment id
		state.setAttribute(GRADE_SUBMISSION_ASSIGNMENT_ID, params.getString("assignmentId"));
		state.setAttribute(GRADE_SUBMISSION_SUBMISSION_ID, params.getString("submissionId"));

		try
		{
			AssignmentSubmission s = AssignmentService.getSubmission((String) state.getAttribute(GRADE_SUBMISSION_SUBMISSION_ID));

			if ((s.getFeedbackText() != null) && (s.getFeedbackText().length() == 0))
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
			state.setAttribute(ATTACHMENTS, v);

			state.setAttribute(GRADE_SUBMISSION_GRADE, s.getGrade());
		}
		catch (IdUnusedException e)
		{
			addAlert(state, rb.getString("cannotfin5"));
		}
		catch (PermissionException e)
		{
			addAlert(state, "You are not allowed to view the assignment submission. ");
		}

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute(GRADE_SUBMISSION_ASSIGNMENT_EXPAND_FLAG, new Boolean(false));
			state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_GRADE_SUBMISSION);
		}

	} // doGrade_submission

	/**
	 * Action is to release all the grades of the submission
	 */
	public void doRelease_grades(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		ParameterParser params = data.getParameters();

		try
		{
			// get the assignment
			Assignment a = AssignmentService.getAssignment(params.getString("assignmentId"));

			String aReference = a.getReference();

			Iterator submissions = AssignmentService.getSubmissions(a);
			while (submissions.hasNext())
			{
				AssignmentSubmission s = (AssignmentSubmission) submissions.next();
				AssignmentSubmissionEdit sEdit = AssignmentService.editSubmission(s.getReference());
				String grade = s.getGrade();
				if (s.getGraded())
				{
					boolean withGrade = state.getAttribute(WITH_GRADES) != null ? ((Boolean) state.getAttribute(WITH_GRADES))
							.booleanValue() : false;
					if (withGrade)
					{
						// for the assignment tool with grade option, a valide grade is needed
						if (grade != null && !grade.equals(""))
						{
							sEdit.setGradeReleased(true);
						}
					}
					else
					{
						// for the assignment tool without grade option, no grade is needed
						sEdit.setGradeReleased(true);
					}
				}

				// clear the returned flag
				sEdit.setReturned(false);
				sEdit.setTimeReturned(null);

				AssignmentService.commitEdit(sEdit);
			} // while
			
			// add grades into Gradebook
			integrateGradebook(state, aReference, null, null, null, -1, null, null, "update");
		}
		catch (IdUnusedException e)
		{
			addAlert(state, rb.getString("cannotfin3"));
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getString("youarenot14"));
		}
		catch (InUseException e)
		{
			addAlert(state, rb.getString("somelsis") + " " + rb.getString("submiss"));
		}

	} // doRelease_grades

	/**
	 * Action is to show the assignment in grading page
	 */
	public void doExpand_grade_assignment(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(GRADE_ASSIGNMENT_EXPAND_FLAG, new Boolean(true));

	} // doExpand_grade_assignment

	/**
	 * Action is to hide the assignment in grading page
	 */
	public void doCollapse_grade_assignment(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(GRADE_ASSIGNMENT_EXPAND_FLAG, new Boolean(false));

	} // doCollapse_grade_assignment

	/**
	 * Action is to show the submissions in grading page
	 */
	public void doExpand_grade_submission(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(GRADE_SUBMISSION_EXPAND_FLAG, new Boolean(true));

	} // doExpand_grade_submission

	/**
	 * Action is to hide the submissions in grading page
	 */
	public void doCollapse_grade_submission(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(GRADE_SUBMISSION_EXPAND_FLAG, new Boolean(false));

	} // doCollapse_grade_submission

	/**
	 * Action is to show the grade assignment
	 */
	public void doGrade_assignment(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();

		// reset the export assignment id
		state.setAttribute(EXPORT_ASSIGNMENT_REF, "");

		String assignmentId = StringUtil.trimToNull(params.getString("assignmentId"));
		if (assignmentId != null)
		{
			// get the assignment id
			state.setAttribute(EXPORT_ASSIGNMENT_REF, assignmentId);
			try
			{
				Assignment a = AssignmentService.getAssignment(assignmentId);
				state.setAttribute(EXPORT_ASSIGNMENT_ID, a.getId());
				state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_GRADE_ASSIGNMENT);
				
				// we are changing the view, so start with first page again. 
				resetPaging(state);
			}
			catch (IdUnusedException e)
			{
				addAlert(state, rb.getString("theisno"));
			}
			catch (PermissionException e)
			{
				addAlert(state, rb.getString("youarenot14"));
			}
		}

	} // doGrade_Assignment

	/**
	 * Action is to show the grade assignment
	 */
	public void doGrade_assignment_from(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();

		state.setAttribute(EXPORT_ASSIGNMENT_REF, params.getString("assignmentId"));

		try
		{
			Assignment a = AssignmentService.getAssignment((String) state.getAttribute(EXPORT_ASSIGNMENT_REF));
			state.setAttribute(EXPORT_ASSIGNMENT_ID, a.getId());
			state.setAttribute(GRADE_ASSIGNMENT_EXPAND_FLAG, new Boolean(false));
			state.setAttribute(GRADE_SUBMISSION_EXPAND_FLAG, new Boolean(true));
			state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_GRADE_ASSIGNMENT);

			// we are changing the view, so start with first page again. 
			resetPaging(state);
		}
		catch (IdUnusedException e)
		{
			addAlert(state, rb.getString("cannotfin3"));
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getString("youarenot14"));
		}
	} // doGrade_assignment_from

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
		Hashtable t = (Hashtable) state.getAttribute(STUDENT_LIST_SHOW_TABLE);
		ParameterParser params = data.getParameters();

		String id = params.getString("studentId");
		// add the student id into the table
		t.put(id, "show");

		state.setAttribute(STUDENT_LIST_SHOW_TABLE, t);

	} // doShow_student_submission

	/**
	 * Action is to hide the student submissions
	 */
	public void doHide_student_submission(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		Hashtable t = (Hashtable) state.getAttribute(STUDENT_LIST_SHOW_TABLE);
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

		state.setAttribute(STATE_MODE, MODE_STUDENT_VIEW_GRADE);

	} // doView_grade

	/**
	 * Action is to show the student submissions
	 */
	public void doReport_submissions(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_REPORT_SUBMISSIONS);
		state.setAttribute(SORTED_BY, SORTED_SUBMISSION_BY_LASTNAME);

	} // doReport_submissions

	/**
	 * 
	 * 
	 */
	public void doAssignment_form(RunData data)
	{
		ParameterParser params = data.getParameters();

		String option = (String) params.getString("option");
		if (option != null)
		{
			if (option.equals("post"))
			{
				// post assignment
				doPost_assignment(data);
			}
			else if (option.equals("save"))
			{
				// save assignment
				doSave_assignment(data);
			}
			else if (option.equals("preview"))
			{
				// preview assignment
				doPreview_assignment(data);
			}
			else if (option.equals("cancel"))
			{
				// cancel creating assignment
				doCancel_new_assignment(data);
			}
			else if (option.equals("canceledit"))
			{
				// cancel editing assignment
				doCancel_edit_assignment(data);
			}
			else if (option.equals("attach"))
			{
				// attachments
				doAttachments(data);
			}
			else if (option.equals("view"))
			{
				// view
				doView(data);
			}
			else if (option.equals("permissions"))
			{
				// permissions
				doPermissions(data);
			}
			else if (option.equals("returngrade"))
			{
				// return grading
				doReturn_grade_submission(data);
			}
			else if (option.equals("savegrade"))
			{
				// save grading
				doSave_grade_submission(data);
			}
			else if (option.equals("previewgrade"))
			{
				// preview grading
				doPreview_grade_submission(data);
			}
			else if (option.equals("cancelgrade"))
			{
				// cancel grading
				doCancel_grade_submission(data);
			}
			else if (option.equals("sortbygrouptitle"))
			{
				// read input data
				setNewAssignmentParameters(data, true);

				// sort by group title
				doSortbygrouptitle(data);
			}
			else if (option.equals("sortbygroupdescription"))
			{
				// read input data
				setNewAssignmentParameters(data, true);

				// sort group by description
				doSortbygroupdescription(data);
			}
			else if (option.equals("hide_instruction"))
			{
				// hide the assignment instruction
				doHide_submission_assignment_instruction(data);
			}
			else if (option.equals("show_instruction"))
			{
				// show the assignment instruction
				doShow_submission_assignment_instruction(data);
			}
			else if (option.equals("sortbygroupdescription"))
			{
				// show the assignment instruction
				doShow_submission_assignment_instruction(data);
			}
			

		}
	}

	/**
	 * Action is to use when doAattchmentsadding requested, corresponding to chef_Assignments-new "eventSubmit_doAattchmentsadding" when "add attachments" is clicked
	 */
	public void doAttachments(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		ParameterParser params = data.getParameters();

		String mode = (String) state.getAttribute(STATE_MODE);
		if (mode.equals(MODE_STUDENT_VIEW_SUBMISSION))
		{
			// retrieve the submission text (as formatted text)
			boolean checkForFormattingErrors = true; // the student is submitting something - so check for errors
			String text = processFormattedTextFromBrowser(state, params.getCleanString(VIEW_SUBMISSION_TEXT),
					checkForFormattingErrors);

			state.setAttribute(VIEW_SUBMISSION_TEXT, text);
			if (params.getString(VIEW_SUBMISSION_HONOR_PLEDGE_YES) != null)
			{
				state.setAttribute(VIEW_SUBMISSION_HONOR_PLEDGE_YES, "true");
			}
			state.setAttribute(FilePickerHelper.FILE_PICKER_TITLE_TEXT, rb.getString("thenewass"));

			// TODO: file picker to save in dropbox? -ggolden
			// User[] users = { UserDirectoryService.getCurrentUser() };
			// state.setAttribute(ResourcesAction.STATE_SAVE_ATTACHMENT_IN_DROPBOX, users);
		}
		else if (mode.equals(MODE_INSTRUCTOR_NEW_EDIT_ASSIGNMENT))
		{
			String stateFromText = "";
			String title = "";

			if (state.getAttribute(EDIT_ASSIGNMENT_ID) != null)
			{
				stateFromText = rb.getString("thenewass");
				title = (String) state.getAttribute(NEW_ASSIGNMENT_TITLE);
				if (title != null && title.length() > 0)
				{
					stateFromText = rb.getString("newass") + " " + '"' + title + '"';
				}
			}
			else
			{
				stateFromText = rb.getString("theassi");
				title = (String) state.getAttribute(NEW_ASSIGNMENT_TITLE);
				if (title != null && title.length() > 0)
				{
					stateFromText = rb.getString("assig2") + " " + '"' + title + '"';
				}
			}

			state.setAttribute(FilePickerHelper.FILE_PICKER_TITLE_TEXT, stateFromText);
			setNewAssignmentParameters(data, false);
		}
		else if (mode.equals(MODE_INSTRUCTOR_GRADE_SUBMISSION))
		{
			readGradeForm(data, state);

			try
			{
				AssignmentSubmission s = AssignmentService.getSubmission((String) state
						.getAttribute(GRADE_SUBMISSION_SUBMISSION_ID));
				if (s != null)
				{
					// TODO: file picker to save in dropbox? -ggolden
					// User[] users = s.getSubmitters();
					// state.setAttribute(ResourcesAction.STATE_SAVE_ATTACHMENT_IN_DROPBOX, users);
				}
			}
			catch (Exception ignore)
			{
			}
		}

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			// get into helper mode with this helper tool
			startHelper(data.getRequest(), "sakai.filepicker");

			// use the real attachment list
			state.setAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS, state.getAttribute(ATTACHMENTS));
		}
	}

	/**
	 * readGradeForm
	 */
	public void readGradeForm(RunData data, SessionState state)
	{
		ParameterParser params = data.getParameters();
		boolean checkForFormattingErrors = false; // so that grading isn't held up by formatting errors
		String feedbackComment = processFormattedTextFromBrowser(state, params.getCleanString(GRADE_SUBMISSION_FEEDBACK_COMMENT),
				checkForFormattingErrors);
		state.setAttribute(GRADE_SUBMISSION_FEEDBACK_COMMENT, feedbackComment);
		String feedbackText = processAssignmentFeedbackFromBrowser(state, params.getCleanString(GRADE_SUBMISSION_FEEDBACK_TEXT));
		state.setAttribute(GRADE_SUBMISSION_FEEDBACK_TEXT, feedbackText);
		state.setAttribute(GRADE_SUBMISSION_FEEDBACK_ATTACHMENT, params.getString(GRADE_SUBMISSION_FEEDBACK_ATTACHMENT));

		if (params.getString("allowResubmit") != null)
		{
			state.setAttribute(GRADE_SUBMISSION_ALLOW_RESUBMIT, Boolean.TRUE);
		}
		else
		{
			state.setAttribute(GRADE_SUBMISSION_ALLOW_RESUBMIT, Boolean.FALSE);
		}

		String grade = params.getString(GRADE_SUBMISSION_GRADE);
		state.setAttribute(GRADE_SUBMISSION_GRADE, grade);
		try
		{
			Assignment a = AssignmentService.getAssignment((String) state.getAttribute(GRADE_SUBMISSION_ASSIGNMENT_ID));

			if (a.getContent().getTypeOfGrade() == 3)
			{
				// for point grades, scale the point grade by 10
				state.setAttribute(GRADE_SUBMISSION_GRADE, scalePointGrade(state, grade));
			}
			else
			{
				// for other grade type, do not scale.
				state.setAttribute(GRADE_SUBMISSION_GRADE, grade);
			}
		}
		catch (IdUnusedException e)
		{
			addAlert(state, rb.getString("cannotfin3"));
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getString("youarenot14"));
		}

		try
		{
			AssignmentSubmission s = AssignmentService.getSubmission((String) state.getAttribute(GRADE_SUBMISSION_SUBMISSION_ID));
			if (s != null)
			{
				// TODO: file picker to save in dropbox? -ggolden
				// User[] users = s.getSubmitters();
				// state.setAttribute(ResourcesAction.STATE_SAVE_ATTACHMENT_IN_DROPBOX, users);
			}
		}
		catch (Exception ignore)
		{
		}
	}

	/**
	 * Populate the state object, if needed - override to do something!
	 */
	protected void initState(SessionState state, VelocityPortlet portlet, JetspeedRunData data)
	{
		super.initState(state, portlet, data);

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

		/** The calendar service in the State. */
		CalendarService cService = (CalendarService) state.getAttribute(STATE_CALENDAR_SERVICE);
		if (cService == null)
		{
			cService = org.sakaiproject.calendar.cover.CalendarService.getInstance();
			state.setAttribute(STATE_CALENDAR_SERVICE, cService);

			String calendarId = ServerConfigurationService.getString("calendar", null);
			if (calendarId == null)
			{
				calendarId = cService.calendarReference(ToolManager.getCurrentPlacement().getContext(), SiteService.MAIN_CONTAINER);
				try
				{
					state.setAttribute(CALENDAR, cService.getCalendar(calendarId));
				}
				catch (IdUnusedException e)
				{
					Log.warn("chef", "No calendar found. ");
					try
					{
						cService.addCalendar(calendarId);
					}
					catch (PermissionException e1)
					{
						Log.warn("chef", "Can not create calendar. ");
					}
					catch (IdUsedException e1)
					{
						Log.warn("chef", "The calendar id has already been used. ");
					}
					catch (IdInvalidException e1)
					{
						Log.warn("chef", "This calendar could not be created because the Id is invalid. ");
					}
					catch (Exception ex)
					{
						Log.warn("chef", "Assignment : Action : init state : calendar exception : " + ex);
					}
				}
				catch (PermissionException e)
				{
					Log.warn("chef", "No permission to get the calender. ");
				}
				catch (Exception ex)
				{
					Log.warn("chef", "Assignment : Action : init state : calendar exception : " + ex);
				}
			}
		} // if

		/** The announcement service in the State. */
		AnnouncementService aService = (AnnouncementService) state.getAttribute(STATE_ANNOUNCEMENT_SERVICE);
		if (aService == null)
		{
			aService = org.sakaiproject.announcement.cover.AnnouncementService.getInstance();
			state.setAttribute(STATE_ANNOUNCEMENT_SERVICE, aService);

			String channelId = ServerConfigurationService.getString("channel", null);
			if (channelId == null)
			{
				channelId = aService.channelReference(ToolManager.getCurrentPlacement().getContext(), SiteService.MAIN_CONTAINER);
				try
				{
					state.setAttribute(ANNOUNCEMENT_CHANNEL, aService.getAnnouncementChannel(channelId));
				}
				catch (IdUnusedException e)
				{
					Log.warn("chef", "No announcement channel found. ");
					// the announcement channel is not created yet; go create
					try
					{
						aService.addAnnouncementChannel(channelId);
					}
					catch (PermissionException ee)
					{
						Log.warn("chef", "Can not create announcement channel. ");
					}
					catch (IdUsedException ee)
					{

					}
					catch (IdInvalidException ee)
					{
						Log.warn("chef", "The announcement channel could not be created because the Id is invalid. ");
					}
					catch (Exception ex)
					{
						Log.warn("chef", "Assignment : Action : init state : announcement exception : " + ex);
					}
				}
				catch (PermissionException e)
				{
					Log.warn("chef", "No permission to annoucement channel. ");
				}
				catch (Exception ex)
				{
					Log.warn("chef", "Assignment : Action : init state : calendar exception : " + ex);
				}
			}

		} // if

		if (state.getAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE) == null)
		{
			// reset the view submission attributes
			resetViewSubmission(state);
		}
		if (state.getAttribute(STATE_CONTEXT_STRING) == null)
		{
			state.setAttribute(STATE_CONTEXT_STRING, ToolManager.getCurrentPlacement().getContext());
		} // if context string is null

		if (state.getAttribute(SORTED_BY) == null)
		{
			state.setAttribute(SORTED_BY, SORTED_BY_DUEDATE);
		}

		if (state.getAttribute(SORTED_ASC) == null)
		{
			state.setAttribute(SORTED_ASC, Boolean.TRUE.toString());
		}

		if (state.getAttribute(SORTED_SUBMISSION_BY) == null)
		{
			state.setAttribute(SORTED_SUBMISSION_BY, SORTED_SUBMISSION_BY_LASTNAME);
		}

		if (state.getAttribute(SORTED_SUBMISSION_ASC) == null)
		{
			state.setAttribute(SORTED_SUBMISSION_ASC, Boolean.TRUE.toString());
		}

		if (state.getAttribute(NEW_ASSIGNMENT_HIDE_OPTION_FLAG) == null)
		{
			resetAssignment(state);
		}

		if (state.getAttribute(STUDENT_LIST_SHOW_TABLE) == null)
		{
			state.setAttribute(STUDENT_LIST_SHOW_TABLE, new Hashtable());
		}

		if (state.getAttribute(ATTACHMENTS_MODIFIED) == null)
		{
			state.setAttribute(ATTACHMENTS_MODIFIED, new Boolean(false));
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

		if (state.getAttribute(STATE_SEARCH) == null)
		{
			state.setAttribute(STATE_SEARCH, "");
		}

		if (state.getAttribute(STATE_TOP_PAGE_MESSAGE) == null)
		{
			state.setAttribute(STATE_TOP_PAGE_MESSAGE, new Integer(0));
		}

		if (state.getAttribute(WITH_GRADES) == null)
		{
			PortletConfig config = portlet.getPortletConfig();
			String withGrades = StringUtil.trimToNull(config.getInitParameter("withGrades"));
			if (withGrades == null)
			{
				withGrades = Boolean.FALSE.toString();
			}
			state.setAttribute(WITH_GRADES, new Boolean(withGrades));
		}
	} // initState

	/**
	 * reset the attributes for view submission
	 */
	private void resetViewSubmission(SessionState state)
	{
		state.removeAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE);
		state.removeAttribute(VIEW_SUBMISSION_TEXT);
		state.setAttribute(VIEW_SUBMISSION_HONOR_PLEDGE_YES, "false");

	} // resetViewSubmission

	/**
	 * reset the attributes for view submission
	 */
	private void resetAssignment(SessionState state)
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
		state.setAttribute(NEW_ASSIGNMENT_OPENMONTH, new Integer(month));
		state.setAttribute(NEW_ASSIGNMENT_OPENDAY, new Integer(day));
		state.setAttribute(NEW_ASSIGNMENT_OPENYEAR, new Integer(year));
		state.setAttribute(NEW_ASSIGNMENT_OPENHOUR, new Integer(12));
		state.setAttribute(NEW_ASSIGNMENT_OPENMIN, new Integer(0));
		state.setAttribute(NEW_ASSIGNMENT_OPENAMPM, "PM");

		// due date is shifted forward by 7 days
		t.setTime(t.getTime() + 7 * 24 * 60 * 60 * 1000);
		tB = t.breakdownLocal();
		month = tB.getMonth();
		day = tB.getDay();
		year = tB.getYear();

		// set the due time to be 5:00pm
		state.setAttribute(NEW_ASSIGNMENT_DUEMONTH, new Integer(month));
		state.setAttribute(NEW_ASSIGNMENT_DUEDAY, new Integer(day));
		state.setAttribute(NEW_ASSIGNMENT_DUEYEAR, new Integer(year));
		state.setAttribute(NEW_ASSIGNMENT_DUEHOUR, new Integer(5));
		state.setAttribute(NEW_ASSIGNMENT_DUEMIN, new Integer(0));
		state.setAttribute(NEW_ASSIGNMENT_DUEAMPM, "PM");

		// enable the close date by default
		state.setAttribute(NEW_ASSIGNMENT_ENABLECLOSEDATE, new Boolean(true));
		// set the close time to be 5:00 pm, same as the due time by default
		state.setAttribute(NEW_ASSIGNMENT_CLOSEMONTH, new Integer(month));
		state.setAttribute(NEW_ASSIGNMENT_CLOSEDAY, new Integer(day));
		state.setAttribute(NEW_ASSIGNMENT_CLOSEYEAR, new Integer(year));
		state.setAttribute(NEW_ASSIGNMENT_CLOSEHOUR, new Integer(5));
		state.setAttribute(NEW_ASSIGNMENT_CLOSEMIN, new Integer(0));
		state.setAttribute(NEW_ASSIGNMENT_CLOSEAMPM, "PM");

		state.setAttribute(NEW_ASSIGNMENT_SECTION, "001");
		state.setAttribute(NEW_ASSIGNMENT_SUBMISSION_TYPE, new Integer(Assignment.TEXT_AND_ATTACHMENT_ASSIGNMENT_SUBMISSION));
		state.setAttribute(NEW_ASSIGNMENT_GRADE_TYPE, new Integer(Assignment.UNGRADED_GRADE_TYPE));
		state.setAttribute(NEW_ASSIGNMENT_GRADE_POINTS, "");
		state.setAttribute(NEW_ASSIGNMENT_DESCRIPTION, "");
		state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE, Boolean.FALSE.toString());
		state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE, Boolean.FALSE.toString());
		// make the honor pledge not include as the default
		state.setAttribute(NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE, (new Integer(Assignment.HONOR_PLEDGE_NONE)).toString());

		state.removeAttribute(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK);

		state.setAttribute(NEW_ASSIGNMENT_ATTACHMENT, EntityManager.newReferenceList());

		state.setAttribute(NEW_ASSIGNMENT_HIDE_OPTION_FLAG, new Boolean(false));

		state.setAttribute(NEW_ASSIGNMENT_FOCUS, NEW_ASSIGNMENT_TITLE);

		state.removeAttribute(NEW_ASSIGNMENT_DESCRIPTION_EMPTY);

		// reset the global navigaion alert flag
		if (state.getAttribute(ALERT_GLOBAL_NAVIGATION) != null)
		{
			state.removeAttribute(ALERT_GLOBAL_NAVIGATION);
		}

		state.setAttribute(NEW_ASSIGNMENT_RANGE, "site");
		state.removeAttribute(NEW_ASSIGNMENT_GROUPS);
		
		// remove the edit assignment id if any
		state.removeAttribute(EDIT_ASSIGNMENT_ID);

	} // resetNewAssignment

	/**
	 * construct a Hashtable using integer as the key and three character string of the month as the value
	 */
	private Hashtable monthTable()
	{
		Hashtable n = new Hashtable();
		n.put(new Integer(1), rb.getString("jan"));
		n.put(new Integer(2), rb.getString("feb"));
		n.put(new Integer(3), rb.getString("mar"));
		n.put(new Integer(4), rb.getString("apr"));
		n.put(new Integer(5), rb.getString("may"));
		n.put(new Integer(6), rb.getString("jun"));
		n.put(new Integer(7), rb.getString("jul"));
		n.put(new Integer(8), rb.getString("aug"));
		n.put(new Integer(9), rb.getString("sep"));
		n.put(new Integer(10), rb.getString("oct"));
		n.put(new Integer(11), rb.getString("nov"));
		n.put(new Integer(12), rb.getString("dec"));
		return n;

	} // monthTable

	/**
	 * construct a Hashtable using the integer as the key and grade type String as the value
	 */
	private Hashtable gradeTypeTable()
	{
		Hashtable n = new Hashtable();
		n.put(new Integer(2), rb.getString("letter"));
		n.put(new Integer(3), rb.getString("points"));
		n.put(new Integer(4), rb.getString("pass"));
		n.put(new Integer(5), rb.getString("check"));
		n.put(new Integer(1), rb.getString("ungra"));
		return n;

	} // gradeTypeTable

	/**
	 * construct a Hashtable using the integer as the key and submission type String as the value
	 */
	private Hashtable submissionTypeTable()
	{
		Hashtable n = new Hashtable();
		n.put(new Integer(1), rb.getString("inlin"));
		n.put(new Integer(2), rb.getString("attaonly"));
		n.put(new Integer(3), rb.getString("inlinatt"));
		return n;

	} // submissionTypeTable

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
	 * the AssignmentComparator clas
	 */
	private class AssignmentComparator implements Comparator
	{
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
		 * @param criteria
		 *        The sort criteria string
		 * @param asc
		 *        The sort order string. TRUE_STRING if ascending; "false" otherwise.
		 */
		public AssignmentComparator(String criteria, String asc)
		{
			m_criteria = criteria;
			m_asc = asc;

		} // constructor

		/**
		 * constructor
		 * 
		 * @param criteria
		 *        The sort criteria string
		 * @param asc
		 *        The sort order string. TRUE_STRING if ascending; "false" otherwise.
		 * @param user
		 *        The user object
		 */
		public AssignmentComparator(String criteria, String asc, User user)
		{
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

			/** *********** fo sorting assignments ****************** */
			if (m_criteria.equals(SORTED_BY_TITLE))
			{
				// sorted by the assignment title
				String s1 = ((Assignment) o1).getTitle();
				String s2 = ((Assignment) o2).getTitle();
				result = s1.compareToIgnoreCase(s2);
			}
			else if (m_criteria.equals(SORTED_BY_SECTION))
			{
				// sorted by the assignment section
				String s1 = ((Assignment) o1).getSection();
				String s2 = ((Assignment) o2).getSection();
				result = s1.compareToIgnoreCase(s2);
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
				if (t1.before(t2))
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
				String s1 = getAssignmentStatus((Assignment) o1);
				String s2 = getAssignmentStatus((Assignment) o2);
				result = s1.compareToIgnoreCase(s2);
			}
			else if (m_criteria.equals(SORTED_BY_NUM_SUBMISSIONS))
			{
				// sort by numbers of submissions

				// initialize
				int subNum1 = 0;
				int subNum2 = 0;

				Iterator submissions1 = AssignmentService.getSubmissions((Assignment) o1);
				while (submissions1.hasNext())
				{
					AssignmentSubmission submission1 = (AssignmentSubmission) submissions1.next();
					if (submission1.getSubmitted()) subNum1++;
				}

				Iterator submissions2 = AssignmentService.getSubmissions((Assignment) o2);
				while (submissions2.hasNext())
				{
					AssignmentSubmission submission2 = (AssignmentSubmission) submissions2.next();
					if (submission2.getSubmitted()) subNum2++;
				}

				result = (subNum1 > subNum2) ? 1 : -1;

			}
			else if (m_criteria.equals(SORTED_BY_NUM_UNGRADED))
			{
				// sort by numbers of ungraded submissions

				// initialize
				int ungraded1 = 0;
				int ungraded2 = 0;

				Iterator submissions1 = AssignmentService.getSubmissions((Assignment) o1);
				while (submissions1.hasNext())
				{
					AssignmentSubmission submission1 = (AssignmentSubmission) submissions1.next();
					if (submission1.getSubmitted() && !submission1.getGraded()) ungraded1++;
				}

				Iterator submissions2 = AssignmentService.getSubmissions((Assignment) o2);
				while (submissions2.hasNext())
				{
					AssignmentSubmission submission2 = (AssignmentSubmission) submissions2.next();
					if (submission2.getSubmitted() && !submission2.getGraded()) ungraded2++;
				}

				result = (ungraded1 > ungraded2) ? 1 : -1;

			}
			else if (m_criteria.equals(SORTED_BY_SUBMISSION_STATUS))
			{
				try
				{
					AssignmentSubmission submission1 = AssignmentService.getSubmission(((Assignment) o1).getId(), m_user);
					String status1 = getSubmissionStatus(submission1, (Assignment) o1);

					AssignmentSubmission submission2 = AssignmentService.getSubmission(((Assignment) o2).getId(), m_user);
					String status2 = getSubmissionStatus(submission2, (Assignment) o2);

					result = status1.compareTo(status2);
				}
				catch (IdUnusedException e)
				{
					return 1;
				}
				catch (PermissionException e)
				{
					return 1;
				}
			}
			else if (m_criteria.equals(SORTED_BY_GRADE))
			{
				try
				{
					AssignmentSubmission submission1 = AssignmentService.getSubmission(((Assignment) o1).getId(), m_user);
					String grade1 = " ";
					if (submission1 != null && submission1.getGraded() && submission1.getGradeReleased())
					{
						grade1 = submission1.getGrade();
					}

					AssignmentSubmission submission2 = AssignmentService.getSubmission(((Assignment) o2).getId(), m_user);
					String grade2 = " ";
					if (submission2 != null && submission2.getGraded() && submission2.getGradeReleased())
					{
						grade2 = submission2.getGrade();
					}

					result = grade1.compareTo(grade2);
				}
				catch (IdUnusedException e)
				{
					return 1;
				}
				catch (PermissionException e)
				{
					return 1;
				}
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
					result = maxGrade1.compareTo(maxGrade2);
				}
			}
			// group related sorting
			else if (m_criteria.equals(SORTED_BY_FOR))
			{
				// sorted by the public view attribute
				String factor1 = getAssignmentRange((Assignment) o1);
				String factor2 = getAssignmentRange((Assignment) o2);
				result = factor1.compareToIgnoreCase(factor2);
			}
			else if (m_criteria.equals(SORTED_BY_GROUP_TITLE))
			{
				// sorted by the group title
				String factor1 = ((Group) o1).getTitle();
				String factor2 = ((Group) o2).getTitle();
				result = factor1.compareToIgnoreCase(factor2);
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
				result = factor1.compareToIgnoreCase(factor2);
			}
			/** ***************** for sorting submissions ************* */
			else if (m_criteria.equals(SORTED_SUBMISSION_BY_LASTNAME))
			{
				// sorted by the submitters sort name
				User[] u1 = ((AssignmentSubmission) o1).getSubmitters();
				User[] u2 = ((AssignmentSubmission) o2).getSubmitters();

				if (u1 == null || u2 == null)
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
					result = submitters1.compareTo(submitters2);
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
				String status1 = getSubmissionStatus((AssignmentSubmission) o1);
				String status2 = getSubmissionStatus((AssignmentSubmission) o2);

				result = status1.compareTo(status2);
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
					if (grade1.equals(""))
					{
						result = -1;
					}
					else if (grade2.equals(""))
					{
						result = 1;
					}
					else
					{
						result = (new Integer(grade1)).intValue() > (new Integer(grade2)).intValue() ? 1 : -1;

					}
				}
				else
				{
					result = grade1.compareTo(grade2);
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
					// otherwise do an alpha-compare
					result = maxGrade1.compareTo(maxGrade2);
				}
			}
			else if (m_criteria.equals(SORTED_SUBMISSION_BY_RELEASED))
			{
				// sort by submission released
				String released1 = (new Boolean(((AssignmentSubmission) o1).getGradeReleased())).toString();
				String released2 = (new Boolean(((AssignmentSubmission) o2).getGradeReleased())).toString();

				result = released1.compareTo(released2);
			}
			else if (m_criteria.equals(SORTED_SUBMISSION_BY_ASSIGNMENT))
			{
				// sort by submission's assignment
				String title1 = ((AssignmentSubmission) o1).getAssignment().getContent().getTitle();
				String title2 = ((AssignmentSubmission) o2).getAssignment().getContent().getTitle();

				result = title1.compareTo(title2);
			}
			else if (m_criteria.equals(SORTED_SUBMISSION_BY_MAX_GRADE))
			{
				// sort by submission max grade
				int maxGrade1 = ((AssignmentSubmission) o1).getAssignment().getContent().getMaxGradePoint();
				int maxGrade2 = ((AssignmentSubmission) o2).getAssignment().getContent().getMaxGradePoint();

				result = (maxGrade1 < maxGrade2) ? 1 : -1;
			}

			// sort ascending or descending
			if (m_asc.equals(Boolean.FALSE.toString()))
			{
				result = -result;
			}
			return result;
		} // compare

		/**
		 * get the submissin status
		 */
		private String getSubmissionStatus(AssignmentSubmission s)
		{
			String status = "";
			if (s.getGraded())
			{
				if (s.getGradeReleased())
				{
					status = rb.getString("releas");
				}
				else
				{
					status = rb.getString("grad3");
				}
			}
			else
			{
				status = rb.getString("ungra");
			}
			return status;

		} // getSubmissionStatus

		/**
		 * get the status string of assignment
		 */
		private String getAssignmentStatus(Assignment a)
		{
			String status = "";
			Time currentTime = TimeService.newTime();

			if (a.getDraft())
				status = rb.getString("draft2");
			else if (a.getOpenTime().after(currentTime))
				status = rb.getString("notope");
			else if (a.getDueTime().after(currentTime))
				status = rb.getString("ope");
			else if ((a.getCloseTime() != null) && (a.getCloseTime().before(currentTime)))
				status = rb.getString("clos");
			else
				status = rb.getString("due2");
			return status;
		} // getAssignmentStatus

		/**
		 * get submission status
		 */
		private String getSubmissionStatus(AssignmentSubmission submission, Assignment assignment)
		{
			String status = "";

			if (submission != null)
				if (submission.getSubmitted())
					if (submission.getGraded() && submission.getGradeReleased())
						status = rb.getString("grad3");
					else if (submission.getReturned())
						status = rb.getString("return") + " " + submission.getTimeReturned().toStringLocalFull();
					else
					{
						status = rb.getString("submitt") + submission.getTimeSubmitted().toStringLocalFull();
						if (submission.getTimeSubmitted().after(assignment.getDueTime())) status = status + rb.getString("late");
					}
				else
					status = rb.getString("inpro");
			else
				status = rb.getString("notsta");

			return status;

		} // getSubmissionStatus

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
				maxGrade = rb.getString("nogra");
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
				maxGrade = rb.getString("pass2");
			}
			else if (gradeType == 5)
			{
				// Grade type that only requires a check
				maxGrade = rb.getString("check2");
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

			if (SiteService.allowUpdateSite(ToolManager.getCurrentPlacement().getContext()))
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

				// disable auto-updates while leaving the list view
				justDelivered(state);
			}
			state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);

			// reset the global navigaion alert flag
			if (state.getAttribute(ALERT_GLOBAL_NAVIGATION) != null)
			{
				state.removeAttribute(ALERT_GLOBAL_NAVIGATION);
			}
		}

	} // doPermissions

	/**
	 * transforms the Iterator to Vector
	 */
	private Vector iterator_to_vector(Iterator l)
	{
		Vector v = new Vector();
		while (l.hasNext())
		{
			v.add(l.next());
		}
		return v;
	} // iterator_to_vector

	/**
	 * Implement this to return alist of all the resources that there are to page. Sort them as appropriate.
	 */
	protected List readResourcesPage(SessionState state, int first, int last)
	{
		String mode = (String) state.getAttribute(STATE_MODE);
		String search = (String) state.getAttribute(STATE_SEARCH);

		// all the resources for paging
		List returnResources = new Vector();

		if (mode.equalsIgnoreCase(MODE_LIST_ASSIGNMENTS))
		{
			String view = "";
			if (state.getAttribute(STATE_SELECTED_VIEW) != null)
			{
				view = (String) state.getAttribute(STATE_SELECTED_VIEW);
			}

			if (AssignmentService.allowAddAssignment((String) state.getAttribute(STATE_CONTEXT_STRING))
					&& view.equals(MODE_LIST_ASSIGNMENTS))
			{
				// read all Assignments
				Iterator allAssignmentsIterator = null;

				// get those opened and posted assignments
				allAssignmentsIterator = AssignmentService.getAssignmentsForContext((String) state
						.getAttribute(STATE_CONTEXT_STRING));

				// deal with no assignments
				if (allAssignmentsIterator == null) return new Vector();

				Vector assignments = new Vector();

				while (allAssignmentsIterator.hasNext())
				{
					Assignment a = (Assignment) allAssignmentsIterator.next();

					String deleted = a.getProperties().getProperty(ResourceProperties.PROP_ASSIGNMENT_DELETED);
					if (deleted == null || deleted.equals(""))
					{
						// not deleted, show it
						if (search != null && !search.equals(""))
						{
							// filtering if searching
							if (StringUtil.containsIgnoreCase(a.getTitle(), search)
									|| StringUtil.containsIgnoreCase(a.getContent().getInstructions(), search))
							{
								if (a.getDraft())
								{
									// for draft assignment, only admin users or the creator can see it
									if (SecurityService.isSuperUser()
											|| a.getCreator().equals(UserDirectoryService.getCurrentUser().getId()))
									{
										assignments.add(a);
									}
								}
								else
								{
									assignments.add(a);
								}
							}
						}
						else
						{
							if (a.getDraft())
							{
								// for draft assignment, only admin users or the creator can see it
								if (SecurityService.isSuperUser()
										|| a.getCreator().equals(UserDirectoryService.getCurrentUser().getId()))
								{
									assignments.add(a);
								}
							}
							else
							{
								assignments.add(a);
							}
						}
					}

				}
				returnResources = assignments;
			}
			else if (AssignmentService.allowAddAssignment((String) state.getAttribute(STATE_CONTEXT_STRING))
					&& view.equals(MODE_STUDENT_VIEW)
					|| !AssignmentService.allowAddAssignment((String) state.getAttribute(STATE_CONTEXT_STRING)))
			{
				// in the student list view of assignments
				Iterator assignments = AssignmentService
						.getAssignmentsForContext((String) state.getAttribute(STATE_CONTEXT_STRING));
				Time currentTime = TimeService.newTime();
				while (assignments.hasNext())
				{
					Assignment a = (Assignment) assignments.next();
					try
					{
						AssignmentSubmission submission = AssignmentService.getSubmission(a.getReference(), (User) state
								.getAttribute(STATE_USER));
						String deleted = a.getProperties().getProperty(ResourceProperties.PROP_ASSIGNMENT_DELETED);
						if (deleted == null || deleted.equals("")
								|| (deleted.equalsIgnoreCase(Boolean.TRUE.toString()) && submission != null))
						{
							// show not deleted assignments and those deleted assignments but the user has made submissions to them
							Time openTime = a.getOpenTime();
							if (openTime != null && currentTime.after(openTime) && !a.getDraft())
							{
								if (search != null && !search.equals(""))
								{
									// search based on assignment title and instructions
									if (StringUtil.containsIgnoreCase(a.getTitle(), search)
											|| StringUtil.containsIgnoreCase(a.getContent().getInstructions(), search))
									{
										if (a.getDraft())
										{
											// for draft assignment, only admin users or the creator can see it
											if (SecurityService.isSuperUser()
													|| a.getCreator().equals(UserDirectoryService.getCurrentUser().getId()))
											{
												returnResources.add(a);
											}
										}
										else
										{
											returnResources.add(a);
										}
									}
								}
								else
								{
									if (a.getDraft())
									{
										// for draft assignment, only admin users or the creator can see it
										if (SecurityService.isSuperUser()
												|| a.getCreator().equals(UserDirectoryService.getCurrentUser().getId()))
										{
											returnResources.add(a);
										}
									}
									else
									{
										returnResources.add(a);
									}
								}
							}
						}
					}
					catch (IdUnusedException e)
					{
						addAlert(state, rb.getString("cannotfin3"));
					}
					catch (PermissionException e)
					{
						addAlert(state, rb.getString("youarenot14"));
					}
				}
			}
		}
		/*
		 * else if (mode.equalsIgnoreCase(MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT)) { Iterator assignments = AssignmentService.getAssignmentsForContext ((String) state.getAttribute (STATE_CONTEXT_STRING)); boolean found = false; while
		 * (assignments.hasNext () && !found) { Assignment a = (Assignment) assignments.next(); String deleted = a.getProperties().getProperty(ResourceProperties.PROP_ASSIGNMENT_DELETED); if ((deleted == null || deleted.equals("")) && (!a.getDraft())) { //
		 * not deleted, show it List users = AssignmentService.allowAddSubmissionUsers (a.getReference ()); // not deleted, show it if (search != null && !search.equals("")) { for (int i=0; i < users.size(); i++) { User u = (User) users.get(i); if
		 * (StringUtil.containsIgnoreCase(u.getDisplayName(),search)) { returnResources.add(u); } } } else { returnResources.add(users); } found = true; } } }
		 */
		else if (mode.equalsIgnoreCase(MODE_INSTRUCTOR_REPORT_SUBMISSIONS))
		{
			Vector submissions = new Vector();

			Vector assignments = iterator_to_vector(AssignmentService.getAssignmentsForContext((String) state
					.getAttribute(STATE_CONTEXT_STRING)));
			if (assignments.size() > 0)
			{
				// users = AssignmentService.allowAddSubmissionUsers (((Assignment)assignments.get(0)).getReference ());
			}

			for (int j = 0; j < assignments.size(); j++)
			{
				Assignment a = (Assignment) assignments.get(j);
				String deleted = a.getProperties().getProperty(ResourceProperties.PROP_ASSIGNMENT_DELETED);
				if ((deleted == null || deleted.equals("")) && (!a.getDraft()))
				{
					try
					{
						Vector assignmentSubmissions = iterator_to_vector(AssignmentService.getSubmissions(a));
						for (int k = 0; k < assignmentSubmissions.size(); k++)
						{
							AssignmentSubmission s = (AssignmentSubmission) assignmentSubmissions.get(k);
							if (search != null && !search.equals(""))
							{
								// search result
								User[] submitters = s.getSubmitters();
								String names = "";
								for (int kk = 0; kk < submitters.length; kk++)
								{
									names = names.concat(((User) submitters[kk]).getDisplayName());
								}
								if (StringUtil.containsIgnoreCase(names, search))
								{
									if (s != null
											&& (s.getSubmitted() || (s.getReturned() && (s.getTimeLastModified().before(s
													.getTimeReturned())))))
									{
										// has been subitted or has been returned and not work on it yet
										submissions.add(s);
									}
								}
							}
							else
							{
								if (s != null
										&& (s.getSubmitted() || (s.getReturned() && (s.getTimeLastModified().before(s
												.getTimeReturned())))))
								{
									// has been subitted or has been returned and not work on it yet
									submissions.add(s);
								}
							} // if-else
						}
					}
					catch (Exception e)
					{
					}
				}
			}

			returnResources = submissions;
		}
		else if (mode.equalsIgnoreCase(MODE_INSTRUCTOR_GRADE_ASSIGNMENT))
		{
			try
			{
				Assignment a = AssignmentService.getAssignment((String) state.getAttribute(EXPORT_ASSIGNMENT_REF));
				Vector submissions = iterator_to_vector(AssignmentService.getSubmissions(a));

				// iterator to find only submitted ones
				for (int i = 0; i < submissions.size(); i++)
				{
					AssignmentSubmission submission = (AssignmentSubmission) submissions.get(i);

					if (submission.getSubmitted())
					{
						if (search != null && !search.equals(""))
						{
							User[] users = submission.getSubmitters();
							for (int j = 0; j < users.length; j++)
							{
								User user = users[j];
	
								if (StringUtil.containsIgnoreCase(user.getDisplayName(), search))
								{
									returnResources.add(submission);
								}
							}
						}
						else
						{
							returnResources.add(submission);
						}
					}
				}
			}
			catch (IdUnusedException e)
			{
				addAlert(state, rb.getString("cannotfin3"));
			}
			catch (PermissionException e)
			{
				addAlert(state, rb.getString("youarenot14"));
			}
		}

		// sort them all
		String ascending = "true";
		String sort = "";
		if (mode.equalsIgnoreCase(MODE_INSTRUCTOR_GRADE_ASSIGNMENT) || mode.equalsIgnoreCase(MODE_INSTRUCTOR_REPORT_SUBMISSIONS))
		{
			ascending = (String) state.getAttribute(SORTED_SUBMISSION_ASC);
			sort = (String) state.getAttribute(SORTED_SUBMISSION_BY);
		}
		else
		{
			ascending = (String) state.getAttribute(SORTED_ASC);
			sort = (String) state.getAttribute(SORTED_BY);
		}

		if ((returnResources.size() > 1) && !mode.equalsIgnoreCase(MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT))
		{
			Collections.sort(returnResources, new AssignmentComparator(sort, ascending));
		}

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
		String search = (String) state.getAttribute(STATE_SEARCH);
		String view = "";
		if (state.getAttribute(STATE_SELECTED_VIEW) != null)
		{
			view = (String) state.getAttribute(STATE_SELECTED_VIEW);
		}

		// all the resources for paging
		int size = 0;

		if (mode.equalsIgnoreCase(MODE_LIST_ASSIGNMENTS))
		{
			size = 0;

			if (AssignmentService.allowAddAssignment((String) state.getAttribute(STATE_CONTEXT_STRING))
					&& view.equals(MODE_LIST_ASSIGNMENTS))
			{

				// read all Assignments
				Iterator allAssignmentsIterator = null;

				// get those opened and posted assignments
				allAssignmentsIterator = AssignmentService.getAssignmentsForContext((String) state
						.getAttribute(STATE_CONTEXT_STRING));

				// deal with no assignments
				if (allAssignmentsIterator == null) return 0;

				while (allAssignmentsIterator.hasNext())
				{
					Assignment a = (Assignment) allAssignmentsIterator.next();

					String deleted = a.getProperties().getProperty(ResourceProperties.PROP_ASSIGNMENT_DELETED);
					if (deleted == null || deleted.equals(""))
					{
						// not deleted, show it
						if (search != null && !search.equals(""))
						{
							// filtering if searching
							if (StringUtil.containsIgnoreCase(a.getTitle(), search)
									|| StringUtil.containsIgnoreCase(a.getContent().getInstructions(), search))
							{
								if (a.getDraft())
								{
									// for draft assignment, only admin users or the creator can see it
									if (SecurityService.isSuperUser()
											|| a.getCreator().equals(UserDirectoryService.getCurrentUser().getId()))
									{
										size++;
									}
								}
								else
								{
									size++;
								}
							}
						}
						else
						{
							if (a.getDraft())
							{
								// for draft assignment, only admin users or the creator can see it
								if (SecurityService.isSuperUser()
										|| a.getCreator().equals(UserDirectoryService.getCurrentUser().getId()))
								{
									size++;
								}
							}
							else
							{
								size++;
							}
						}
					}

				}
			}
			else if (AssignmentService.allowAddAssignment((String) state.getAttribute(STATE_CONTEXT_STRING))
					&& view.equals(MODE_STUDENT_VIEW)
					|| !AssignmentService.allowAddAssignment((String) state.getAttribute(STATE_CONTEXT_STRING)))
			{
				// in the student list view of assignments
				Iterator assignments = AssignmentService
						.getAssignmentsForContext((String) state.getAttribute(STATE_CONTEXT_STRING));
				Time currentTime = TimeService.newTime();
				while (assignments.hasNext())
				{
					Assignment a = (Assignment) assignments.next();
					try
					{
						AssignmentSubmission submission = AssignmentService.getSubmission(a.getReference(), (User) state
								.getAttribute(STATE_USER));
						String deleted = a.getProperties().getProperty(ResourceProperties.PROP_ASSIGNMENT_DELETED);
						if (deleted == null || deleted.equals("")
								|| (deleted.equalsIgnoreCase(Boolean.TRUE.toString()) && submission != null))
						{
							// show not deleted assignments and those deleted assignments but the user has made submissions to them
							Time openTime = a.getOpenTime();
							if (openTime != null && currentTime.after(openTime) && !a.getDraft())
							{
								if (search != null && !search.equals(""))
								{
									// search based on assignment title and instructions
									if (StringUtil.containsIgnoreCase(a.getTitle(), search)
											|| StringUtil.containsIgnoreCase(a.getContent().getInstructions(), search))
									{
										size++;
									}
								}
								else
								{
									size++;
								}
							}
						}
					}
					catch (IdUnusedException e)
					{
						addAlert(state, rb.getString("cannotfin3"));
					}
					catch (PermissionException e)
					{
						addAlert(state, rb.getString("youarenot14"));
					}
				}
			}
		}
		else if (mode.equalsIgnoreCase(MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT))
		{
			size = 0;
			Iterator assignments = AssignmentService.getAssignmentsForContext((String) state.getAttribute(STATE_CONTEXT_STRING));
			boolean found = false;
			while (assignments.hasNext() && !found)
			{
				Assignment a = (Assignment) assignments.next();
				String deleted = a.getProperties().getProperty(ResourceProperties.PROP_ASSIGNMENT_DELETED);
				if ((deleted == null || deleted.equals("")) && (!a.getDraft()))
				{
					// not deleted, show it
					List users = AssignmentService.allowAddSubmissionUsers(a.getReference());

					// not deleted, show it
					if (search != null && !search.equals(""))
					{
						for (int i = 0; i < users.size(); i++)
						{
							User u = (User) users.get(i);
							if (StringUtil.containsIgnoreCase(u.getDisplayName(), search))
							{
								size++;
							}
						}
					}
					else
					{
						size++;
					}
					found = true;
				}
			}
		}
		else if (mode.equalsIgnoreCase(MODE_INSTRUCTOR_REPORT_SUBMISSIONS))
		{
			size = 0;

			Vector assignments = iterator_to_vector(AssignmentService.getAssignmentsForContext((String) state
					.getAttribute(STATE_CONTEXT_STRING)));

			for (int j = 0; j < assignments.size(); j++)
			{
				Assignment a = (Assignment) assignments.get(j);
				String deleted = a.getProperties().getProperty(ResourceProperties.PROP_ASSIGNMENT_DELETED);
				if ((deleted == null || deleted.equals("")) && (!a.getDraft()))
				{
					try
					{
						Vector assignmentSubmissions = iterator_to_vector(AssignmentService.getSubmissions(a));
						for (int k = 0; k < assignmentSubmissions.size(); k++)
						{
							AssignmentSubmission s = (AssignmentSubmission) assignmentSubmissions.get(k);
							if (search != null && !search.equals(""))
							{
								// search result
								User[] submitters = s.getSubmitters();
								String names = "";
								for (int kk = 0; kk < submitters.length; kk++)
								{
									names = names.concat(((User) submitters[kk]).getDisplayName());
								}
								if (StringUtil.containsIgnoreCase(names, search))
								{
									if (s != null
											&& (s.getSubmitted() || (s.getReturned() && (s.getTimeLastModified().before(s
													.getTimeReturned())))))
									{
										// has been subitted or has been returned and not work on it yet
										size++;
									}
								}
							}
							else
							{
								if (s != null
										&& (s.getSubmitted() || (s.getReturned() && (s.getTimeLastModified().before(s
												.getTimeReturned())))))
								{
									// has been subitted or has been returned and not work on it yet
									size++;
								}
							} // if-else
						}
					}
					catch (Exception e)
					{
					}
				}
			}
		}
		else if (mode.equalsIgnoreCase(MODE_INSTRUCTOR_GRADE_ASSIGNMENT))
		{
			size = 0;

			try
			{
				Assignment a = AssignmentService.getAssignment((String) state.getAttribute(EXPORT_ASSIGNMENT_REF));
				Vector submissions = iterator_to_vector(AssignmentService.getSubmissions(a));

				// filtering if searching based on submitter's display name and sort name
				for (int i = 0; i < submissions.size(); i++)
				{
					AssignmentSubmission submission = (AssignmentSubmission) submissions.get(i);

					if (submission.getSubmitted())
					{
						if (search != null && !search.equals(""))
						{
							User[] users = submission.getSubmitters();
							for (int j = 0; j < users.length; j++)
							{
								User user = users[j];

								if (StringUtil.containsIgnoreCase(user.getDisplayName(), search))
								{
									size++;
								}
							}
						}
						else
						{
							size++;
						}
					}
				}

			}
			catch (IdUnusedException e)
			{
				addAlert(state, rb.getString("cannotfin3"));
			}
			catch (PermissionException e)
			{
				addAlert(state, rb.getString("youarenot14"));
			}
		}

		return size;
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

			if (viewMode.equals(MODE_LIST_ASSIGNMENTS))
			{
				doList_assignments(data);
			}
			else if (viewMode.equals(MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT))
			{
				doView_students_assignment(data);
			}
			else if (viewMode.equals(MODE_INSTRUCTOR_REPORT_SUBMISSIONS))
			{
				doReport_submissions(data);
			}
			else if (viewMode.equals(MODE_STUDENT_VIEW))
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

		context.put("totalPageNumber", new Integer(totalPageNumber(state)));
		context.put("searchString", state.getAttribute(STATE_SEARCH));
		context.put("form_search", FORM_SEARCH);
		context.put("formPageNumber", FORM_PAGE_NUMBER);
		context.put("prev_page_exists", state.getAttribute(STATE_PREV_PAGE_EXISTS));
		context.put("next_page_exists", state.getAttribute(STATE_NEXT_PAGE_EXISTS));
		context.put("current_page", state.getAttribute(STATE_CURRENT_PAGE));
		context.put("selectedView", state.getAttribute(STATE_MODE));

	} // add2ndToolbarFields

	/**
	 * valid grade?
	 */
	private void validPointGrade(SessionState state, String grade)
	{
		if (grade != null && !grade.equals(""))
		{
			if (grade.startsWith("-"))
			{
				// check for negative sign
				addAlert(state, rb.getString("plesuse3"));
			}
			else
			{
				int index = grade.indexOf(".");
				if (index != -1)
				{
					// when there is decimal points inside the grade, scale the number by 10
					// but only one decimal place is supported
					// for example, change 100.0 to 1000
					if (!grade.equals("."))
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
							String gradeString = (grade.endsWith(".")) ? grade.substring(0, index).concat("0") : grade.substring(0,
									index).concat(grade.substring(index + 1));
							try
							{
								Integer.parseInt(gradeString);
							}
							catch (NumberFormatException e)
							{
								alertInvalidPoint(state, gradeString);
							}
						}
					}
					else
					{
						// grade is "."
						addAlert(state, rb.getString("plesuse1"));
					}
				}
				else
				{
					// There is no decimal point; should be int number
					String gradeString = grade + "0";
					try
					{
						Integer.parseInt(gradeString);
					}
					catch (NumberFormatException e)
					{
						alertInvalidPoint(state, gradeString);
					}
				}
			}
		}

	} // validPointGrade

	private void alertInvalidPoint(SessionState state, String grade)
	{
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
			addAlert(state, rb.getString("plesuse4") + maxInt + "." + maxDec + ".");
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
				if (grade.indexOf(".") != -1)
				{
					if (grade.startsWith("."))
					{
						grade = "0".concat(grade);
					}
					else if (grade.endsWith("."))
					{
						grade = grade.concat("0");
					}
				}
				else
				{
					try
					{
						Integer.parseInt(grade);
						grade = grade.substring(0, grade.length() - 1) + "." + grade.substring(grade.length() - 1);
					}
					catch (NumberFormatException e)
					{
						alertInvalidPoint(state, grade);
					}
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
		validPointGrade(state, point);
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			if (point != null && (point.length() >= 1))
			{
				// when there is decimal points inside the grade, scale the number by 10
				// but only one decimal place is supported
				// for example, change 100.0 to 1000
				int index = point.indexOf(".");
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
				if (point.equals("00"))
				{
					point = "0";
				}
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
		StringBuffer alertMsg = new StringBuffer();
		try
		{
			boolean replaceWhitespaceTags = true;
			String text = FormattedText.processFormattedText(strFromBrowser, alertMsg, checkForFormattingErrors,
					replaceWhitespaceTags);
			if (alertMsg.length() > 0) addAlert(state, alertMsg.toString());
			return text;
		}
		catch (Exception e)
		{
			Log.warn("chef", this + ": ", e);
			return strFromBrowser;
		}
	}

	/**
	 * Processes the given assignmnent feedback text, as returned from the user's browser. Makes sure that the Chef-style markup {{like this}} is properly balanced.
	 */
	private String processAssignmentFeedbackFromBrowser(SessionState state, String strFromBrowser)
	{
		if (strFromBrowser == null || strFromBrowser.length() == 0) return strFromBrowser;

		StringBuffer buf = new StringBuffer(strFromBrowser);
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

		boolean checkForFormattingErrors = false; // so that grading isn't held up by formatting errors
		buf = new StringBuffer(processFormattedTextFromBrowser(state, buf.toString(), checkForFormattingErrors));

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

		StringBuffer buf = new StringBuffer(value);
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

		StringBuffer buf = new StringBuffer(value);
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

		StringBuffer buf = new StringBuffer(value);
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

		if (mode.equals(MODE_STUDENT_VIEW_SUBMISSION) || mode.equals(MODE_STUDENT_PREVIEW_SUBMISSION)
				|| mode.equals(MODE_STUDENT_VIEW_GRADE) || mode.equals(MODE_INSTRUCTOR_NEW_EDIT_ASSIGNMENT)
				|| mode.equals(MODE_INSTRUCTOR_DELETE_ASSIGNMENT) || mode.equals(MODE_INSTRUCTOR_GRADE_SUBMISSION)
				|| mode.equals(MODE_INSTRUCTOR_PREVIEW_GRADE_SUBMISSION) || mode.equals(MODE_INSTRUCTOR_PREVIEW_ASSIGNMENT)
				|| mode.equals(MODE_INSTRUCTOR_VIEW_ASSIGNMENT))
		{
			if (state.getAttribute(ALERT_GLOBAL_NAVIGATION) == null)
			{
				addAlert(state, rb.getString("alert.globalNavi"));
				state.setAttribute(ALERT_GLOBAL_NAVIGATION, Boolean.TRUE);

				if (mode.equals(MODE_STUDENT_VIEW_SUBMISSION))
				{
					// retrieve the submission text (as formatted text)
					boolean checkForFormattingErrors = true; // the student is submitting something - so check for errors
					String text = processFormattedTextFromBrowser(state, params.getCleanString(VIEW_SUBMISSION_TEXT),
							checkForFormattingErrors);

					state.setAttribute(VIEW_SUBMISSION_TEXT, text);
					if (params.getString(VIEW_SUBMISSION_HONOR_PLEDGE_YES) != null)
					{
						state.setAttribute(VIEW_SUBMISSION_HONOR_PLEDGE_YES, "true");
					}
					state.setAttribute(FilePickerHelper.FILE_PICKER_TITLE_TEXT, rb.getString("thenewass"));

					// TODO: file picker to save in dropbox? -ggolden
					// User[] users = { UserDirectoryService.getCurrentUser() };
					// state.setAttribute(ResourcesAction.STATE_SAVE_ATTACHMENT_IN_DROPBOX, users);
				}
				else if (mode.equals(MODE_INSTRUCTOR_NEW_EDIT_ASSIGNMENT))
				{
					setNewAssignmentParameters(data, false);
				}
				else if (mode.equals(MODE_INSTRUCTOR_GRADE_SUBMISSION))
				{
					readGradeForm(data, state);
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
		if (option.equals("cancel"))
		{
			// cancel
			doCancel_show_submission(data);
		}
		else if (option.equals("preview"))
		{
			// preview
			doPreview_submission(data);
		}
		else if (option.equals("save"))
		{
			// save draft
			doSave_submission(data);
		}
		else if (option.equals("post"))
		{
			// post
			doPost_submission(data);
		}
		else if (option.equals("revise"))
		{
			// done preview
			doDone_preview_submission(data);
		}
		else if (option.equals("attach"))
		{
			// attach
			doAttachments(data);
		}
	}
}
