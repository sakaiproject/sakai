/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.assignment.api;

/**
 * Store the constants used by Assignment tool and service
 * @author zqian
 *
 */
public class AssignmentConstants {
	
	public final static String MODEL_ANSWER_SHOW_TO_STUDENT = "show_to_student";
	
	public final static int MODEL_ANSWER_SHOW_TO_STUDENT_BEFORE_STARTS = 1;
	
	public final static int MODEL_ANSWER_SHOW_TO_STUDENT_AFTER_SUBMIT = 2;
	
	public final static int MODEL_ANSWER_SHOW_TO_STUDENT_AFTER_GRADE_RETURN = 3;
	
	public final static int MODEL_ANSWER_SHOW_TO_STUDENT_AFTER_ACCEPT_UTIL = 4;
	
	public final static int NOTE_KEEP_PRIVATE = 1;
	
	public final static int NOTE_READ_BY_OTHER = 2;
	
	public final static int NOTE_READ_AND_WRITE_BY_OTHER = 3;
	
	public final static int DEFAULT_SCALED_FACTOR = 10;
	
	public final static int DEFAULT_DECIMAL_POINT = 2;

	/**********************************************************************************************************************************************************************************************************************************************************
	 * EVENT STRINGS
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** Event for adding an assignment. */
	public static final String EVENT_ADD_ASSIGNMENT = "asn.new.assignment";

	/** Event for adding an assignment. */
	public static final String EVENT_ADD_ASSIGNMENT_CONTENT = "asn.new.assignmentcontent";

	/** Event for adding an assignment submission. */
	public static final String EVENT_ADD_ASSIGNMENT_SUBMISSION = "asn.new.submission";

	/** Event for removing an assignment. */
	public static final String EVENT_REMOVE_ASSIGNMENT = "asn.delete.assignment";

	/** Event for removing an assignment content. */
	public static final String EVENT_REMOVE_ASSIGNMENT_CONTENT = "asn.delete.assignmentcontent";

	/** Event for removing an assignment submission. */
	public static final String EVENT_REMOVE_ASSIGNMENT_SUBMISSION = "asn.delete.submission";

	/** Event for accessing an assignment. */
	public static final String EVENT_ACCESS_ASSIGNMENT = "asn.read.assignment";

	/** Event for accessing an assignment content. */
	public static final String EVENT_ACCESS_ASSIGNMENT_CONTENT = "asn.read.assignmentcontent";

	/** Event for accessing an assignment submission. */
	public static final String EVENT_ACCESS_ASSIGNMENT_SUBMISSION = "asn.read.submission";

	/** Event for updating an assignment. */
	public static final String EVENT_UPDATE_ASSIGNMENT = "asn.revise.assignment";
	
	/** Event for assignment title update */
	public static final String EVENT_UPDATE_ASSIGNMENT_TITLE = "asn.revise.title";
	
	/** Event for assignment open date update */
	public static final String EVENT_UPDATE_ASSIGNMENT_OPENDATE = "asn.revise.opendate";
	
	/** Event for assignment due update */
	public static final String EVENT_UPDATE_ASSIGNMENT_DUEDATE = "asn.revise.duedate";
	
	/** Event for assignment close update */
	public static final String EVENT_UPDATE_ASSIGNMENT_CLOSEDATE = "asn.revise.closedate";
	
	/** Event for assignment access update */
	public static final String EVENT_UPDATE_ASSIGNMENT_ACCESS = "asn.revise.access";

	/** Event for updating an assignment content. */
	public static final String EVENT_UPDATE_ASSIGNMENT_CONTENT = "asn.revise.assignmentcontent";

	/** Event for updating an assignment submission. */
	public static final String EVENT_UPDATE_ASSIGNMENT_SUBMISSION = "asn.revise.submission";

	/** Event for saving an assignment submission. */
	public static final String EVENT_SAVE_ASSIGNMENT_SUBMISSION = "asn.save.submission";

	/** Event for submitting an assignment submission. */
	public static final String EVENT_SUBMIT_ASSIGNMENT_SUBMISSION = "asn.submit.submission";
	
	/** Event for grading an assignment submission. */
	public static final String EVENT_GRADE_ASSIGNMENT_SUBMISSION = "asn.grade.submission";

	/** Calendar event field for assignment due dates */
	public static final String NEW_ASSIGNMENT_DUEDATE_CALENDAR_ASSIGNMENT_ID = "new_assignment_duedate_calendar_assignment_id";

	/** the String for all choice in dropdown menu */
	public static final String ALL = "all";
	
	public static final String PROP_LAST_GRADED_DATE = "last_graded_date";
	
	// Property name for Grade Type (scale) options
	public static final String ASSN_GRADE_TYPE_NOGRADE_PROP = "gen.nograd";
	public static final String ASSN_GRADE_TYPE_LETTER_PROP = "letter";
	public static final String ASSN_GRADE_TYPE_POINTS_PROP = "points";
	public static final String ASSN_GRADE_TYPE_PASS_FAIL_PROP = "passfail";
	public static final String ASSN_GRADE_TYPE_CHECK_PROP = "check";
	public static final String ASSN_GRADE_TYPE_UNKNOWN_PROP = "grade.type.unknown";
	
	// Property name for Submission Type options
	public static final String ASSN_SUBMISSION_TYPE_INLINE_PROP = "inlin";
	public static final String ASSN_SUBMISSION_TYPE_ATTACHMENTS_ONLY_PROP = "attaonly";
	public static final String ASSN_SUBMISSION_TYPE_INLINE_AND_ATTACHMENTS_PROP = "inlinatt";
	public static final String ASSN_SUBMISSION_TYPE_NON_ELECTRONIC_PROP = "nonelec";
	public static final String ASSN_SUBMISSION_TYPE_SINGLE_ATTACHMENT_PROP = "singleatt";
	public static final String ASSN_SUBMISSION_TYPE_UNKNOWN_PROP = "submission.type.unknown";

}
