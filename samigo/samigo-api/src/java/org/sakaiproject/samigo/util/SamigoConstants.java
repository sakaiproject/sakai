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
public class SamigoConstants {
    /*
     * Email Templating
     */
    public static final     String      EMAIL_TEMPLATE_ASSESSMENT_SUBMITTED                 = "sam.assessmentSubmitted";
    public static final     String      EMAIL_TEMPLATE_ASSESSMENT_SUBMITTED_FILE_NAME       = "template-assessmentSubmission.xml";
    public static final     String      EMAIL_TEMPLATE_ASSESSMENT_AUTO_SUBMITTED            = "sam.assessmentAutoSubmitted";
    public static final     String      EMAIL_TEMPLATE_ASSESSMENT_AUTO_SUBMITTED_FILE_NAME  = "template-assessmentAutoSubmission.xml";
    public static final     String      EMAIL_TEMPLATE_ASSESSMENT_TIMED_SUBMITTED           = "sam.assessmentTimedSubmitted";
    public static final     String      EMAIL_TEMPLATE_ASSESSMENT_TIMED_SUBMITTED_FILE_NAME = "template-assessmentTimedSubmission.xml";

    /*
     * Events
     */
    public static final     String      EVENT_ASSESSMENT_SUBMITTED                          = "sam.assessmentSubmitted";
    public static final     String      EVENT_ASSESSMENT_AUTO_SUBMITTED                     = "sam.assessmentAutoSubmitted";
    public static final     String      EVENT_ASSESSMENT_TIMED_SUBMITTED                    = "sam.assessmentTimedSubmitted";

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
    
}
