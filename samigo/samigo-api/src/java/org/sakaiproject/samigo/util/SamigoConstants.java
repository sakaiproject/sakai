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
}
