/**
 * Copyright (c) 2007 The Apereo Foundation
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
package org.sakaiproject.scorm.api;

public class ScormConstants
{
	public static String IS_CONTENT_PACKAGE_PROPERTY = "scorm:is_content_package";

	public static String CONTENT_PACKAGE_TITLE_PROPERTY = "CONTENT_PACKAGE_TITLE";

	public static final int CONTENT_PACKAGE_STATUS_UNKNOWN = 0;

	public static final int CONTENT_PACKAGE_STATUS_OPEN = 1;

	public static final int CONTENT_PACKAGE_STATUS_OVERDUE = 2;

	public static final int CONTENT_PACKAGE_STATUS_CLOSED = 3;

	public static final int CONTENT_PACKAGE_STATUS_NOTYETOPEN = 4;

	public static final int NOT_ACCESSED = 0;

	public static final int INCOMPLETE = 1;

	public static final int COMPLETED = 2;

	public static final int GRADED = 3;

	public final static String CMI_OBJECTIVES_ROOT = "cmi.objectives.";

    public final static String CMI_OBJECTIVES_COUNT = CMI_OBJECTIVES_ROOT + "_count";

    public final static String CMI_INTERACTIONS_ROOT = "cmi.interactions.";

    public final static String CMI_INTERACTIONS_COUNT = CMI_INTERACTIONS_ROOT + "_count";

	public final static String CMI_COMPLETION_STATUS = "cmi.completion_status";

    public final static String CMI_COMPLETION_THRESHOLD = "cmi.completion_threshold";

	public final static String CMI_SUCCESS_STATUS = "cmi.success_status";

    public final static String CMI_SUSPEND_DATA = "cmi.suspend_data";

	public final static String CMI_ENTRY = "cmi.entry";

    public final static String CMI_CREDIT = "cmi.credit";

    public final static String CMI_EXIT = "cmi.exit";

    public final static String CMI_LAUNCH_DATA = "cmi.launch_data";

    public final static String CMI_LEARNER_ID = "cmi.learner_id";

    public final static String CMI_LEARNER_NAME = "cmi.learner_name";

    public final static String CMI_LOCATION = "cmi.location";

    public final static String CMI_MAX_TIME_ALLOWED = "cmi.max_time_allowed";

    public final static String CMI_TIME_LIMIT_ACTION = "cmi.time_limit_action";

    public final static String CMI_TOTAL_TIME = "cmi.total_time";

    public final static String CMI_TIMESTAMP = "cmi.timestamp";

    public final static String CMI_COMMENTS_FROM_LEARNER = "cmi.comments_from_learner";

    public final static String CMI_MODE = "cmi.mode";

    public final static String CMI_PROGRESS_MEASURE = "cmi.progress_measure";

    public final static String CMI_SCALED_PASSING_SCORE = "cmi.scaled_passing_score";

	public final static String CMI_SCORE_SCALED = "cmi.score.scaled";

    public final static String CMI_SCORE_RAW = "cmi.score.raw";

    public final static String CMI_SCORE_MIN = "cmi.score.min";

    public final static String CMI_SCORE_MAX = "cmi.score.max";

	public final static String CMI_SESSION_TIME = "cmi.session_time";

	public static final String DEFAULT_USER_AUDIO_LEVEL = "1";

	public static final String DEFAULT_USER_AUDIO_CAPTIONING = "0";

	public static final String DEFAULT_USER_DELIVERY_SPEED = "1";

	public static final String DEFAULT_USER_LANGUAGE = "";

	public static final String PREF_USER_AUDIO_LEVEL = "1";

	public static final String PREF_USER_AUDIO_CAPTIONING = "0";

	public static final String PREF_USER_DELIVERY_SPEED = "1";

	public static final String PREF_USER_LANGUAGE = "English";

	public static final int VALIDATION_SUCCESS = 0;

	public static final int VALIDATION_NOFILE = 1;

	public static final int VALIDATION_NOMANIFEST = 2;

	public static final int VALIDATION_NOTWELLFORMED = 3;

	public static final int VALIDATION_NOTVALIDROOT = 4;

	public static final int VALIDATION_NOTVALIDSCHEMA = 5;

	public static final int VALIDATION_NOTVALIDPROFILE = 6;

	public static final int VALIDATION_MISSINGREQUIREDFILES = 7;

	public static final int VALIDATION_CONVERTFAILED = -1;

	public static final int VALIDATION_WRONGMIMETYPE = 8;

	/** SCORM Player permission: configure */
	public static final String PERM_CONFIG = "scorm.configure";

	/** SCORM Player permission: delete */
	public static final String PERM_DELETE = "scorm.delete";

	/** SCORM Player permission: grade */
	public static final String PERM_GRADE = "scorm.grade";

	/** SCORM Player permission: launch */
	public static final String PERM_LAUNCH = "scorm.launch";

	/** SCORM Player permission: upload */
	public static final String PERM_UPLOAD = "scorm.upload";

	/** SCORM Player permission: validate */
	public static final String PERM_VALIDATE = "scorm.validate";

	/** SCORM Player permission: view results */
	public static final String PERM_VIEW_RESULTS = "scorm.view.results";

	/** SCORM Player tool registration ID */
	public static final String SCORM_TOOL_ID = "sakai.scorm.tool";

	/** SCORM Player default tool name */
	public static final String SCORM_DFLT_TOOL_NAME = "SCORM Player";

	/** SCORM Player root directory in Resources */
	public static final String ROOT_DIRECTORY = "/private/scorm/";

	/** Sakai.property to control default setting for show/hide table of contents */
	public static final String SAK_PROP_CONFIG_SHOW_TOC = "scorm.config.showTOC.default";
	public static final boolean SAK_PROP_CONFIG_SHOW_TOC_DFLT = false;

	/** Sakai.property to control default setting for show/hide nav bar controls */
	public static final String SAK_PROP_CONFIG_SHOW_NAV_BAR = "scorm.config.showNavBar.default";
	public static final boolean SAK_PROP_CONFIG_SHOW_NAV_BAR_DFLT = false;
}
