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

import java.util.List;

/**
 * Store the constants used by Assignment tool and service
 *
 * @author zqian
 */
public final class AssignmentConstants {

    public static final String TOOL_ID = "sakai.assignment.grades";

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


    /**
     *  Event for delayed assignment
     */
    public static final String EVENT_AVAILABLE_ASSIGNMENT = "asn.available.assignment";

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
     * Event for saving an assignment peer review
     */
    public static final String EVENT_SAVE_PEER_REVIEW = "asn.save.peer";
    /**
     * Event for submitting an assignment peer review
     */
    public static final String EVENT_SUBMIT_PEER_REVIEW = "asn.submit.peer";

    /**
     * Calendar event field for assignment due dates
     */
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
    public static final String ASSN_SUBMISSION_TYPE_EXTERNAL_TOOL = "externaltool";
    public static final String ASSN_SUBMISSION_TYPE_UNKNOWN_PROP = "submission.type.unknown";
    public static final String ASSN_SUBMISSION_TYPE_VIDEO_PROP = "video";

    /**
     * Auto submit related properties
     */
    public static final String ASSIGNMENT_AUTO_SUBMIT_ENABLED = "auto_submit_enabled";
    public static final String EMAIL_TEMPLATE_AUTO_SUBMIT_ERRORS = "asn.assignmentAutoSubmitErrors";
    public static final String EMAIL_TEMPLATE_AUTO_SUBMIT_ERRORS_FILE_NAME = "template-assignmentAutoSubmitErrors.xml";
    public static final String PROP_SUBMISSION_AUTO_SUBMITTED = "auto_submitted";

    /**
     * Ungraded grade type string
     */
    public static final String UNGRADED_GRADE_TYPE_STRING = "Ungraded";

    public static final String UNGRADED_GRADE_STRING = "ungraded";

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
    public static final String ALLOW_EXTENSION_CLOSETIME = "allow_extension_closeTime"; //constant for Extension, very similar to Resubmission
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
    public static final String ZIP_PDF_FILE_TYPE = ".pdf";

    public static final String NEW_ASSIGNMENT_USE_REVIEW_SERVICE = "new_assignment_use_review_service";
    public static final String NEW_ASSIGNMENT_ALLOW_STUDENT_VIEW = "new_assignment_allow_student_view";
    public static final String NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_RADIO = "submit_papers_to";
    public static final String NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_NONE = "0";
    public static final String NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_STANDARD = "1";
    public static final String NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_INSITUTION = "2";
    // When to generate reports
    // although the service allows for a value of "1" --> Generate report immediately but overwrite until due date,
    // this doesn't make sense for assignment2. We limit the UI to 0 - Immediately
    // or 2 - On Due Date
    public static final String NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_RADIO = "report_gen_speed";
    public static final String NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_IMMEDIATELY = "0";
    public static final String NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_IMMEDIATELY_AND_DUE = "1";
    public static final String NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_DUE = "2";
    public static final String NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_TURNITIN = "s_paper_check";
    public static final String NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INTERNET = "internet_check";
    public static final String NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_PUB = "journal_check";
    public static final String NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INSTITUTION = "institution_check";
    public static final String NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_BIBLIOGRAPHIC = "exclude_biblio";
    public static final String NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_QUOTED = "exclude_quoted";
    public static final String NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_SELF_PLAG = "exclude_self_plag";
    public static final String NEW_ASSIGNMENT_REVIEW_SERVICE_STORE_INST_INDEX = "store_inst_index";
    public static final String NEW_ASSIGNMENT_REVIEW_SERVICE_STUDENT_PREVIEW = "student_preview";
    public static final String NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_SMALL_MATCHES = "exclude_smallmatches";
    public static final String NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_TYPE = "exclude_type";
    public static final String NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_VALUE = "exclude_value";
    public static final String SUBMISSION_REVIEW_SERVICE_EULA_AGREEMENT = "review_service_eula_agreement";
    public static final String SUBMISSION_REVIEW_CHECK_SERVICE_EULA_AGREEMENT = "review_check_service_eula_agreement";
    public static final String NEW_ASSIGNMENT_TAG_CREATOR = "tag_creator";
    public static final String NEW_ASSIGNMENT_TAG_GROUPS = "tag_groups";
    public static final String SHOW_TAGS_STUDENT = "show_tags_student";

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

    public enum SubmissionStatus {
        NOT_STARTED,
        HONOR_ACCEPTED,
        IN_PROGRESS,
        SUBMITTED,
        RESUBMITTED,
        LATE,
        NO_SUBMISSION,
        UNGRADED,
        RETURNED,
        COMMENTED,
        GRADED,
        RESUBMIT_ALLOWED
    }

	// IMS Score Publishing Service - states
	// https://www.imsglobal.org/spec/lti-ags/v2p0/#score-publish-service
	// 2.4.6. activityProgress
	// activityProgress MUST be used to indicate to the tool platform the status of the user towards the activity's completion.

	// Please leave the capitalization as is on this as we use Enum.name() to match incoming JSON values
	public enum IMSActivityProgress {
		Initialized,     // the user has not started the activity, or the activity has been reset for that student.
		Started,         // the activity associated with the line item has been started by the user to which the result relates.
		InProgress,      // the activity is being drafted and is available for comment.
		Submitted,       // the activity has been submitted at least once by the user but the user is still able make further submissions.
		Completed        // the user has completed the activity associated with the line item.
	}

	// 2.4.7 gradingProgress
	// gradingProgress MUST be used to indicate to the platform the status of the grading process, including allowing
	// to inform when human intervention is needed.

	// Please leave the capitalization as is on this as we use Enum.name() to match incoming JSON values
	public enum IMSGradingProgress {
		FullyGraded,    // The grading process is completed; the score value, if any, represents the current Final Grade;
		Pending,        // Final Grade is pending, but does not require manual intervention; if a Score value is present,
						// it indicates the current value is partial and may be updated.
		PendingManual,  // Final Grade is pending, and it does require human intervention; if a Score value is present,
						// it indicates the current value is partial and may be updated during the manual grading.
		Failed,         // The grading could not complete.
		NotReady        // There is no grading process occurring; for example, the student has not yet made any submission.
	}

    public static final String SUBMISSION_OPTION_RELEASE = "release";
    public static final String SUBMISSION_OPTION_RETURN = "return";
    public static final String SUBMISSION_OPTION_SAVE = "save";
    public static final String SUBMISSION_OPTION_RETRACT = "retract";

    public static final String STATE_CONTEXT_STRING = "Assignment.context_string";

    public static final String GRADE_SUBMISSION_SUBMISSION_ID = "grade_submission_submission_id";
    public static final String GRADE_SUBMISSION_ASSIGNMENT_ID = "grade_submission_assignment_id";
    public static final String GRADE_SUBMISSION_GRADE = "grade_submission_grade";

    public static final String GRADE_SUBMISSION_FEEDBACK_COMMENT = "grade_submission_feedback_comment";
    public static final String GRADE_SUBMISSION_FEEDBACK_TEXT = "grade_submission_feedback_text";
    public static final String GRADE_SUBMISSION_PRIVATE_NOTES = "grade_submission_private_notes";
    public static final String GRADE_SUBMISSION_FEEDBACK_ATTACHMENT = "grade_submission_feedback_attachment";

    // submission level of resubmit due time
    public static final String ALLOW_RESUBMIT_CLOSE_YEAR = "allow_resubmit_close_year";
    public static final String ALLOW_RESUBMIT_CLOSE_MONTH = "allow_resubmit_close_month";
    public static final String ALLOW_RESUBMIT_CLOSE_DAY = "allow_resubmit_close_day";
    public static final String ALLOW_RESUBMIT_CLOSE_HOUR = "allow_resubmit_close_hour";
    public static final String ALLOW_RESUBMIT_CLOSE_MIN = "allow_resubmit_close_min";
    public static final String ALLOW_RESUBMIT_CLOSE_EPOCH_MILLIS = "allow_resubmit_close_epoch_millis";
    public static final String ALLOW_EXTENSION_CLOSE_MONTH = "allow_extension_close_month";
    public static final String ALLOW_EXTENSION_CLOSE_DAY = "allow_extension_close_day";
    public static final String ALLOW_EXTENSION_CLOSE_YEAR = "allow_extension_close_year";
    public static final String ALLOW_EXTENSION_CLOSE_HOUR = "allow_extension_close_hour";
    public static final String ALLOW_EXTENSION_CLOSE_MIN = "allow_extension_close_min";
    public static final String ALLOW_EXTENSION_CLOSE_EPOCH_MILLIS = "allow_extension_close_epoch_millis";
    public static final String GRADE_SUBMISSION_DONT_CLEAR_CURRENT_ATTACHMENTS = "grade_submission_dont_clear_current_attachments";

    public static final String GRADEBOOK_INTEGRATION_NO = "no";
    public static final String GRADEBOOK_INTEGRATION_ADD = "add";
    public static final String GRADEBOOK_INTEGRATION_ASSOCIATE = "associate";
    public static final String PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT = "prop_new_assignment_add_to_gradebook";

    public static final String NEW_ASSIGNMENT_ADD_TO_GRADEBOOK = "new_assignment_add_to_gradebook";

	/**
	 * Sakai property key to change the default value for the 'Add due date to calendar' checkbox
	 */
	public static final String SAK_PROP_DUE_DATE_TO_CALENDAR_DEFAULT = "asn.due.date.to.calendar.default";

	/**
	 * Sakai.property for enable/disable anonymous grading
	 */
	public static final String SAK_PROP_ENABLE_ANON_GRADING = "assignment.anon.grading.enabled";

	/**
	 * Site property for forcing anonymous grading in a site
	 */
	public static final String SAK_PROP_FORCE_ANON_GRADING = "assignment.anon.grading.forced";

	/*
	 * Sakai property for allowing the ability for an assignment to send grades to an existing gradebook item
	 */
	public static final String SAK_PROP_ALLOW_LINK_TO_EXISTING_GB_ITEM = "assignment.allowLinkToExistingGBItem";
	public static final boolean SAK_PROP_ALLOW_LINK_TO_EXISTING_GB_ITEM_DFLT = true;

    /*
     * Sakai property for enabling/disabling the auto-submit feature
     */
    public static final String SAK_PROP_AUTO_SUBMIT_ENABLED = "assignment.autoSubmit.enabled";
    public static final boolean SAK_PROP_AUTO_SUBMIT_ENABLED_DFLT = false;

    /*
     * Sakai property for enabling/disabling email notifications for auto-submit errors
     */
    public static final String SAK_PROP_AUTO_SUBMIT_ERROR_NOTIFICATION_ENABLED = "assignment.email.autoSubmit.errorNotification.enabled";
    public static final boolean SAK_PROP_AUTO_SUBMIT_ERROR_NOTIFICATION_ENABLED_DFLT = true;

    /*
     * Sakai property for the email address to send auto-submit error notifications
     */
    public static final String SAK_PROP_AUTO_SUBMIT_ERROR_NOTIFICATION_TO_ADDRESS = "assignment.email.autoSubmit.errorNotification.toAddress";

    /*
     * Sakai property for the support email address
     */
    public static final String SAK_PROP_SUPPORT_EMAIL_ADDRESS = "mail.support";

    public static final String SAK_PROP_NON_SUBMITTER_PERMISSIONS = "assignment.submitter.remove.permission";
    public static final List<String> SAK_PROP_NON_SUBMITTER_PERMISSIONS_DEFAULT = List.of(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT);

    public static final String ASSIGNMENT_INPUT_ADD_SUBMISSION_TIME_SPENT = "value_ASSIGNMENT_INPUT_ADD_SUBMISSION_TIME_SPENT";
}
