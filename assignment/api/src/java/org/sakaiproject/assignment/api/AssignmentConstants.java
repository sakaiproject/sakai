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
 *
 * @author zqian
 */
public final class AssignmentConstants {

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
    /**
     * Event for adding an assignment.
     */
    public static final String EVENT_ADD_ASSIGNMENT = "asn.new.assignment";

    /**********************************************************************************************************************************************************************************************************************************************************
     * EVENT STRINGS
     *********************************************************************************************************************************************************************************************************************************************************/
    /**
     * Event for adding an assignment.
     */
    public static final String EVENT_ADD_ASSIGNMENT_CONTENT = "asn.new.assignmentcontent";
    /**
     * Event for adding an assignment submission.
     */
    public static final String EVENT_ADD_ASSIGNMENT_SUBMISSION = "asn.new.submission";
    /**
     * Event for removing an assignment.
     */
    public static final String EVENT_REMOVE_ASSIGNMENT = "asn.delete.assignment";
    /**
     * Event for removing an assignment content.
     */
    public static final String EVENT_REMOVE_ASSIGNMENT_CONTENT = "asn.delete.assignmentcontent";
    /**
     * Event for removing an assignment submission.
     */
    public static final String EVENT_REMOVE_ASSIGNMENT_SUBMISSION = "asn.delete.submission";
    /**
     * Event for accessing an assignment.
     */
    public static final String EVENT_ACCESS_ASSIGNMENT = "asn.read.assignment";
    /**
     * Event for accessing an assignment content.
     */
    public static final String EVENT_ACCESS_ASSIGNMENT_CONTENT = "asn.read.assignmentcontent";
    /**
     * Event for accessing an assignment submission.
     */
    public static final String EVENT_ACCESS_ASSIGNMENT_SUBMISSION = "asn.read.submission";
    /**
     * Event for updating an assignment.
     */
    public static final String EVENT_UPDATE_ASSIGNMENT = "asn.revise.assignment";
    /**
     * Event for assignment title update
     */
    public static final String EVENT_UPDATE_ASSIGNMENT_TITLE = "asn.revise.title";
    /**
     * Event for assignment open date update
     */
    public static final String EVENT_UPDATE_ASSIGNMENT_OPENDATE = "asn.revise.opendate";
    /**
     * Event for assignment due update
     */
    public static final String EVENT_UPDATE_ASSIGNMENT_DUEDATE = "asn.revise.duedate";
    /**
     * Event for assignment close update
     */
    public static final String EVENT_UPDATE_ASSIGNMENT_CLOSEDATE = "asn.revise.closedate";
    /**
     * Event for assignment access update
     */
    public static final String EVENT_UPDATE_ASSIGNMENT_ACCESS = "asn.revise.access";
    /**
     * Event for updating an assignment content.
     */
    public static final String EVENT_UPDATE_ASSIGNMENT_CONTENT = "asn.revise.assignmentcontent";
    /**
     * Event for updating an assignment submission.
     */
    public static final String EVENT_UPDATE_ASSIGNMENT_SUBMISSION = "asn.revise.submission";
    /**
     * Event for saving an assignment submission.
     */
    public static final String EVENT_SAVE_ASSIGNMENT_SUBMISSION = "asn.save.submission";
    /**
     * Event for submitting an assignment submission.
     */
    public static final String EVENT_SUBMIT_ASSIGNMENT_SUBMISSION = "asn.submit.submission";
    /**
     * Event for grading an assignment submission.
     */
    public static final String EVENT_GRADE_ASSIGNMENT_SUBMISSION = "asn.grade.submission";
    /**
     * Calendar event field for assignment due dates
     */
    public static final String NEW_ASSIGNMENT_DUEDATE_CALENDAR_ASSIGNMENT_ID = "new_assignment_duedate_calendar_assignment_id";
    public static final String NEW_ASSIGNMENT_DUE_DATE_SCHEDULED = "new_assignment_due_date_scheduled";
    public static final String NEW_ASSIGNMENT_OPEN_DATE_ANNOUNCED = "new_assignment_open_date_announced";
    /**
     * the String for all choice in dropdown menu
     */
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
    /**
     * Ungraded grade type string
     */
    public static final String UNGRADED_GRADE_TYPE_STRING = "Ungraded";

    /** Grade type not set */
    // public static final int GRADE_TYPE_NOT_SET = -1; change to 0
    /**
     * Letter grade type string
     */
    public static final String LETTER_GRADE_TYPE_STRING = "Letter Grade";
    /**
     * Score based grade type string
     */
    public static final String SCORE_GRADE_TYPE_STRING = "Points";
    /**
     * Pass/fail grade type string
     */
    public static final String PASS_FAIL_GRADE_TYPE_STRING = "Pass/Fail";
    /**
     * Grade type that only requires a check string
     */
    public static final String CHECK_GRADE_TYPE_STRING = "Checkmark";
    /**
     * Assignment type not yet set
     */
    // public static final int ASSIGNMENT_SUBMISSION_TYPE_NOT_SET = -1; change to 0

    public static final int HONOR_PLEDGE_NOT_SET = -1;
    public static final int HONOR_PLEDGE_NONE = 1;
    public static final int HONOR_PLEDGE_ENGINEERING = 2;
    // the option of email notification setting per assignment about student submissions
    public static final String ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_VALUE = "assignment_instructor_notifications_value";
    // no email to instructor
    public static final String ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_NONE = "assignment_instructor_notifications_none";
    // send every email to instructor
    public static final String ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_EACH = "assignment_instructor_notifications_each";
    // send email in digest form
    public static final String ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_DIGEST = "assignment_instructor_notifications_digest";
    // the option of student email notification setting per assignment about released grades
    public static final String ASSIGNMENT_RELEASEGRADE_NOTIFICATION_VALUE = "assignment_releasegrade_notification_value";
    // do NOT send student email notification when the grade is released
    public static final String ASSIGNMENT_RELEASEGRADE_NOTIFICATION_NONE = "assignment_releasegrade_notification_none";
    // send student email notification when the grade is released
    public static final String ASSIGNMENT_RELEASEGRADE_NOTIFICATION_EACH = "assignment_releasegrade_notification_each";
    public static final String ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_VALUE = "assignment_releasereturn_notification_value";
    public static final String ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_NONE = "assignment_releasereturn_notification_none";
    public static final String ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_EACH = "assignment_releasereturn_notification_each";
    // the option of open date notification to students
    public static final String ASSIGNMENT_OPENDATE_NOTIFICATION = "assignment_opendate_notification";
    public static final String ASSIGNMENT_OPENDATE_NOTIFICATION_NONE = "assignment_opendate_notification_none";
    public static final String ASSIGNMENT_OPENDATE_NOTIFICATION_LOW = "assignment_opendate_notification_low";
    public static final String ASSIGNMENT_OPENDATE_NOTIFICATION_HIGH = "assignment_opendate_notification_high";
    /**
     * number of times that the submission is allowed to resubmit
     */
    public static final String ALLOW_RESUBMIT_NUMBER = "allow_resubmit_number";
    /**
     * submission level of close time
     */
    public static final String ALLOW_RESUBMIT_CLOSETIME = "allow_resubmit_closeTime";
    /**
     * submission by different user
     */
    public static final String SUBMITTER_USER_ID = "submitted_user_id";
    /**
     * resource property that marks the attachment as being the inline submission (boolean)
     */
    public static final String PROP_INLINE_SUBMISSION = "assignment_submission_attachment_is_inline";
    public static final String ZIP_COMMENT_FILE_TYPE = ".txt";
    public static final String ZIP_SUBMITTED_TEXT_FILE_TYPE = ".html";
    public static final String REVIEW_SCORE = "review_score";
    public static final String REVIEW_REPORT = "review_report";
    public static final String REVIEW_STATUS = "review_status";
    public static final String REVIEW_ICON = "review_icon";
    public static final String REVIEW_ERROR = "review_error";

    private AssignmentConstants() {
        throw new RuntimeException(this.getClass().getCanonicalName() + " is not to be instantiated");
    }

    public enum Status {
        DRAFT,
        NOT_OPEN,
        OPEN,
        CLOSED,
        DUE
    }
    
    public static final String SUBMISSION_OPTION_RELEASE = "release";
    public static final String SUBMISSION_OPTION_RETURN = "return";
    public static final String SUBMISSION_OPTION_SAVE = "save";
    public static final String SUBMISSION_OPTION_RETRACT = "retract";

}
