/**
 * Copyright (c) 2015, The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.samigo.util;

import org.sakaiproject.event.api.NotificationService;

/**
 * Class to hold constants for Samigo, defaults, etc.
 *
 * Modeled after {@link org.sakaiproject.profile2.util.ProfileConstants}
 *
 * @author Leonardo Canessa ( lcanessa1 at udayton dot edu )
 */
public final class SamigoConstants {
    /*
     * Email Templating
     */
    public static final     String      EMAIL_TEMPLATE_ASSESSMENT_SUBMITTED                 = "sam.assessmentSubmitted";
    public static final     String      EMAIL_TEMPLATE_ASSESSMENT_SUBMITTED_FILE_NAME       = "template-assessmentSubmission.xml";
    public static final     String      EMAIL_TEMPLATE_ASSESSMENT_AUTO_SUBMITTED            = "sam.assessmentAutoSubmitted";
    public static final     String      EMAIL_TEMPLATE_ASSESSMENT_AUTO_SUBMITTED_FILE_NAME  = "template-assessmentAutoSubmission.xml";
    public static final     String      EMAIL_TEMPLATE_ASSESSMENT_TIMED_SUBMITTED           = "sam.assessmentTimedSubmitted";
    public static final     String      EMAIL_TEMPLATE_ASSESSMENT_TIMED_SUBMITTED_FILE_NAME = "template-assessmentTimedSubmission.xml";
    public static final     String      EMAIL_TEMPLATE_AUTO_SUBMIT_ERRORS                   = "sam.assessmentAutoSubmitErrors";
    public static final     String      EMAIL_TEMPLATE_AUTO_SUBMIT_ERRORS_FILE_NAME         = "template-assessmentAutoSubmitErrors.xml";

    /*
     * Events
     */
    // Submission events
    public static final     String      EVENT_ASSESSMENT_SUBMITTED                          = "sam.assessment.submit";
    public static final     String      EVENT_ASSESSMENT_SUBMITTED_NOTI                     = "sam.assessment.submit.noti";
    public static final     String      EVENT_ASSESSMENT_SUBMITTED_CHECKED                  = "sam.assessment.submit.checked";
    public static final     String      EVENT_ASSESSMENT_SUBMITTED_CLICKSUB                 = "sam.assessment.submit.click_sub";
    public static final     String      EVENT_ASSESSMENT_SUBMITTED_AUTO                     = "sam.assessment.submit.auto";
    public static final     String      EVENT_ASSESSMENT_SUBMITTED_FROM_LASTPAGE            = "sam.assessment.submit.from_last";
    public static final     String      EVENT_ASSESSMENT_SUBMITTED_FROM_TOC                 = "sam.assessment.submit.from_toc";
    public static final     String      EVENT_ASSESSMENT_SUBMITTED_VIA_URL                  = "sam.assessment.submit.via_url";
    public static final     String      EVENT_ASSESSMENT_SUBMITTED_THREAD                   = "sam.assessment.submit.thread";
    public static final     String      EVENT_ASSESSMENT_SUBMITTED_TIMER                    = "sam.assessment.submit.timer";
    public static final     String      EVENT_ASSESSMENT_SUBMITTED_TIMER_VIA_URL            = "sam.assessment.submit.timer.url";
    public static final     String      EVENT_ASSESSMENT_SUBMITTED_TIMER_THREAD             = "sam.assessment.submit.timer.thrd";

    //Assessment scoring events
    public static final     String      EVENT_ASSESSMENT_TOTAL_SCORE_UPDATE                 = "sam.total.score.update";
    public static final     String      EVENT_ASSESSMENT_STUDENT_SCORE_UPDATE               = "sam.student.score.update";
    public static final     String      EVENT_ASSESSMENT_AUTO_GRADED                        = "sam.assessment.graded.auto";
    public static final     String      EVENT_ASSESSMENT_QUESTION_SCORE_UPDATE              = "sam.question.score.update";

    //Assessment manipulation
    public static final     String      EVENT_ASSESSMENT_SAVEITEM                           = "sam.assessment.saveitem";
    public static final     String      EVENT_ASSESSMENT_ITEM_DELETE                        = "sam.assessment.item.delete";
    public static final     String      EVENT_ASSESSMENT_CREATE                             = "sam.assessment.create";
    public static final     String      EVENT_ASSESSMENT_TAKE                               = "sam.assessment.take";
    public static final     String      EVENT_ASSESSMENT_TAKE_VIAURL                        = "sam.assessment.take.via_url";
    public static final     String      EVENT_ASSESSMENT_RESUME                             = "sam.assessment.resume";
    public static final     String      EVENT_ASSESSMENT_REVIEW                             = "sam.assessment.review";
    public static final     String      EVENT_ASSESSMENT_REMOVE                             = "sam.assessment.remove";
    public static final     String      EVENT_ASSESSMENT_REVISE                             = "sam.assessment.revise";
    public static final     String      EVENT_ASSESSMENT_UNINDEXITEM                        = "sam.assessment.unindexitem";
    public static final     String      EVENT_ASSESSMENT_PUBLISH                            = "sam.assessment.publish";

    //Published assessment events
    public static final     String      EVENT_PUBLISHED_ASSESSMENT_REVISE                   = "sam.pubassessment.revise";
    public static final     String      EVENT_PUBLISHED_ASSESSMENT_SAVEITEM                 = "sam.pubassessment.saveitem";
    public static final     String      EVENT_PUBLISHED_ASSESSMENT_REMOVE                   = "sam.pubassessment.remove";
    public static final     String      EVENT_PUBLISHED_ASSESSMENT_CONFIRM_EDIT             = "sam.pubassessment.confirm_edit";
    public static final     String      EVENT_PUBLISHED_ASSESSMENT_REPUBLISH                = "sam.pubassessment.republish";
    public static final     String      EVENT_PUBLISHED_ASSESSMENT_SETTING_EDIT             = "sam.pubsetting.edit";
    public static final     String      EVENT_PUBLISHED_ASSESSMENT_UNINDEXITEM              = "sam.pubassessment.unindexitem";

    //Question pool events
    public static final     String      EVENT_QUESTIONPOOL_QUESTIONMOVED                    = "sam.questionpool.questionmoved";
    public static final     String      EVENT_QUESTIONPOOL_DELETEITEM                       = "sam.questionpool.deleteitem";
    public static final     String      EVENT_QUESTIONPOOL_TRANSFER                         = "sam.questionpool.transfer";
    public static final     String      EVENT_QUESTIONPOOL_DELETE                           = "sam.questionpool.deleteitem";
    public static final     String      EVENT_QUESTIONPOOL_UNSHARE                          = "sam.questionpool.unshare";

    //Other events
    public static final     String      EVENT_EMAIL                                         = "sam.email";
    public static final     String      EVENT_AUTO_SUBMIT_JOB                               = "sam.auto-submit.job";
    public static final     String      EVENT_AUTO_SUBMIT_JOB_ERROR                         = "sam.auto-submit.job.error";
    public static final     String      EVENT_ASSESSMENT_SETTING_EDIT                       = "sam.setting.edit";

    /*
     * Notification Types
     */
    public static final     String      NOTI_PREFS_TYPE_SAMIGO                              = "sakai:samigo";


    /*
     * Notification Defaults
     */
    public static final     int         NOTI_PREF_DEFAULT                                   = NotificationService.PREF_IMMEDIATE;
    public static final     int         NOTI_EVENT_ASSESSMENT_SUBMITTED                     = NotificationService.NOTI_OPTIONAL;
    public static final     int         NOTI_EVENT_ASSESSMENT_TIMED_SUBMITTED               = NotificationService.NOTI_OPTIONAL;
    public static final     int         NOTI_PREF_INSTRUCTOR_EMAIL_DEFAULT                  = NotificationService.PREF_IGNORE;
    
    /*
     * Authorization 
     */
    public static final		String		AUTHZ_TAKE_ASSESSMENT								= "assessment.takeAssessment";
    public static final		String		AUTHZ_SUBMIT_ASSESSMENT								= "assessment.submitAssessmentForGrade";
    public static final		String		AUTHZ_CREATE_ASSESSMENT								= "assessment.createAssessment";
    public static final		String		AUTHZ_EDIT_ASSESSMENT_ANY							= "assessment.editAssessment.any";
    public static final		String		AUTHZ_EDIT_ASSESSMENT_OWN							= "assessment.editAssessment.own";
    public static final		String		AUTHZ_DELETE_ASSESSMENT_ANY							= "assessment.deleteAssessment.any";
    public static final		String		AUTHZ_DELETE_ASSESSMENT_OWN							= "assessment.deleteAssessment.own";
    public static final		String		AUTHZ_PUBLISH_ASSESSMENT_ANY						= "assessment.publishAssessment.any";
    public static final		String		AUTHZ_PUBLISH_ASSESSMENT_OWN						= "assessment.publishAssessment.own";
    public static final		String		AUTHZ_GRADE_ASSESSMENT_ANY							= "assessment.gradeAssessment.any";
    public static final		String		AUTHZ_GRADE_ASSESSMENT_OWN							= "assessment.gradeAssessment.own";
    public static final		String		AUTHZ_QUESTIONPOOL_CREATE							= "assessment.questionpool.create";
    public static final		String		AUTHZ_QUESTIONPOOL_EDIT_OWN							= "assessment.questionpool.edit.own";
    public static final		String		AUTHZ_QUESTIONPOOL_DELETE_OWN						= "assessment.questionpool.delete.own";
    public static final		String		AUTHZ_QUESTIONPOOL_COPY_OWN							= "assessment.questionpool.copy.own";
    public static final		String		AUTHZ_TEMPLATE_CREATE								= "assessment.template.create";
    public static final		String		AUTHZ_TEMPLATE_EDIT_OWN								= "assessment.template.edit.own";
    public static final		String		AUTHZ_TEMPLATE_DELETE_OWN							= "assessment.template.delete.own";

    /*
     * Sakai.properties 
     */
    public static final     String      SAK_PROP_AUTO_SUBMIT_ERROR_NOTIFICATION_ENABLED     = "samigo.email.autoSubmit.errorNotification.enabled";
    public static final     String      SAK_PROP_AUTO_SUBMIT_ERROR_NOTIFICATION_TO_ADDRESS  = "samigo.email.autoSubmit.errorNotification.toAddress";
    public static final     String      SAK_PROP_SUPPORT_EMAIL_ADDRESS                      = "mail.support";

    public static final     String      ALPHABET                                            = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /*
     * Message Bundles
     */
    public static final     String      EVAL_BUNDLE                                         = "org.sakaiproject.tool.assessment.bundle.EvaluationMessages";
    public static final     String      AUTHOR_BUNDLE                                       = "org.sakaiproject.tool.assessment.bundle.AuthorMessages";

    private SamigoConstants() {
    	throw new AssertionError();
    }
}
