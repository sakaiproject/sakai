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
 *       http://www.osedu.org/licenses/ECL-2.0
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

}
