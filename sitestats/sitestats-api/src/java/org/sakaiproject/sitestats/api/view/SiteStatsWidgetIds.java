/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.api.view;

public final class SiteStatsWidgetIds {

	private SiteStatsWidgetIds() {
	}

	public static final String AUDIENCE_ALL = "all";
	public static final String AUDIENCE_OWN = "own";
	public static final String AUDIENCE_ADMIN = "admin";

	public static final String WIDGET_VISITS = "visits";
	public static final String WIDGET_PRESENCE_ACCESS = "presence-access";
	public static final String WIDGET_STUDENT_VISITS = "student-visits";
	public static final String WIDGET_STUDENT_PRESENCE_ACCESS = "student-presence-access";
	public static final String WIDGET_SUBMISSIONS = "submissions";
	public static final String WIDGET_STUDENT_SUBMISSIONS = "student-submissions";
	public static final String WIDGET_GRADES = "grades";
	public static final String WIDGET_STUDENT_GRADES = "student-grades";
	public static final String WIDGET_COMMUNICATION = "communication";
	public static final String WIDGET_STUDENT_COMMUNICATION = "student-communication";
	public static final String WIDGET_ACTIVITY = "activity";
	public static final String WIDGET_RESOURCES = "resources";
	public static final String WIDGET_LESSONS = "lessons";
	public static final String WIDGET_PLATFORM_HEALTH = "platform-health";
	public static final String WIDGET_INSTITUTIONAL_PRESENCE = "institutional-presence";
	public static final String WIDGET_INSTITUTIONAL_LEARNER_ACTIVITY = "institutional-learner-activity";
	public static final String WIDGET_INSTITUTIONAL_AUTHORING_ACTIVITY = "institutional-authoring-activity";
	public static final String WIDGET_CLIENT_ENVIRONMENT = "client-environment";

	public static final String TAB_BY_DATE = "bydate";
	public static final String TAB_BY_USER = "byuser";
	public static final String TAB_BY_ROLE = "byrole";
	public static final String TAB_BY_ITEM = "byitem";
	public static final String TAB_BY_TOOL = "bytool";
	public static final String TAB_BY_RESOURCE = "byresource";
	public static final String TAB_BY_PAGE = "bypage";
	public static final String TAB_BY_SITE = "bysite";
	public static final String TAB_BY_DEVICE = "bydevice";
	public static final String TAB_BY_BROWSER = "bybrowser";
	public static final String TAB_BY_EVENT = "byevent";

	public static final String FILTER_DATE = "date";
	public static final String FILTER_WHEN_FROM = "whenFrom";
	public static final String FILTER_WHEN_TO = "whenTo";
	public static final String FILTER_ROLE = "role";
	public static final String FILTER_TOOL = "tool";
	public static final String FILTER_RESOURCE_ACTION = "resourceAction";
	public static final String FILTER_LESSON_ACTION = "lessonAction";
	public static final String FILTER_THRESHOLD = "threshold";
	public static final String FILTER_ITEM_TYPE = "itemType";
	public static final String FILTER_GROUP = "group";
	public static final String FILTER_ITEM = "item";
	public static final String FILTER_SITE_TYPE = "siteType";
	public static final String FILTER_SITE_PREFIX = "sitePrefix";
	public static final String FILTER_PUBLISHED_ONLY = "publishedOnly";

	public static final String ITEM_TYPE_ALL = "all";
	public static final String GROUP_ALL = "all";
	public static final String ITEM_ALL = "all";

	public static final String METRIC_VISITS_TOTAL = "visits-total";
	public static final String METRIC_VISITS_UNIQUE = "visits-unique";
	public static final String METRIC_VISITS_ENROLLED_USERS = "visits-enrolled-users";
	public static final String METRIC_VISITS_USERS_WITH_VISITS = "visits-users-with-visits";
	public static final String METRIC_VISITS_USERS_WITHOUT_VISITS = "visits-users-without-visits";
	public static final String METRIC_VISITS_AVERAGE_PRESENCE = "visits-average-presence";
	public static final String METRIC_VISITS_TRAFFIC_TREND = "visits-traffic-trend";
	public static final String METRIC_STUDENT_VISITS_TOTAL = "student-visits-total";
	public static final String METRIC_STUDENT_VISITS_TRAFFIC_TREND = "student-visits-traffic-trend";
	public static final String METRIC_STUDENT_VISITS_AVERAGE_PRESENCE = "student-visits-average-presence";
	public static final String METRIC_STUDENT_VISITS_PRESENCE = "student-visits-presence";
	public static final String METRIC_PRESENCE_LAST_VISIT = "presence-last-visit";
	public static final String METRIC_PRESENCE_NEVER_VISITED = "presence-never-visited";
	public static final String METRIC_PRESENCE_AVERAGE = "presence-average";
	public static final String METRIC_PRESENCE_BOUNCE_RATE = "presence-bounce-rate";
	public static final String METRIC_PRESENCE_TOTAL_7D = "presence-total-7d";
	public static final String METRIC_PRESENCE_TOTAL_30D = "presence-total-30d";
	public static final String METRIC_PRESENCE_TOTAL_365D = "presence-total-365d";
	public static final String METRIC_STUDENT_PRESENCE_LAST_VISIT = "student-presence-last-visit";
	public static final String METRIC_STUDENT_PRESENCE_AVERAGE = "student-presence-average";
	public static final String METRIC_STUDENT_PRESENCE_BOUNCE_RATE = "student-presence-bounce-rate";
	public static final String METRIC_STUDENT_PRESENCE_TOTAL = "student-presence-total";
	public static final String METRIC_STUDENT_PRESENCE_TOTAL_7D = "student-presence-total-7d";
	public static final String METRIC_STUDENT_PRESENCE_TOTAL_30D = "student-presence-total-30d";
	public static final String METRIC_STUDENT_PRESENCE_TOTAL_365D = "student-presence-total-365d";
	public static final String METRIC_ADOPTION_ACTIVE = "adoption-active";
	public static final String METRIC_SUBMISSIONS_ON_TIME = "submissions-on-time";
	public static final String METRIC_SUBMISSIONS_LATE = "submissions-late";
	public static final String METRIC_SUBMISSIONS_MISSED = "submissions-missed";
	public static final String METRIC_SUBMISSIONS_NEEDS_GRADING = "submissions-needs-grading";
	public static final String METRIC_SUBMISSIONS_AT_RISK = "submissions-at-risk";
	public static final String METRIC_SUBMISSIONS_AVG_DELAY = "submissions-avg-delay";
	public static final String METRIC_STUDENT_SUBMISSIONS_ON_TIME = "student-submissions-on-time";
	public static final String METRIC_STUDENT_SUBMISSIONS_LATE = "student-submissions-late";
	public static final String METRIC_STUDENT_SUBMISSIONS_MISSED = "student-submissions-missed";
	public static final String METRIC_STUDENT_SUBMISSIONS_AVG_DELAY = "student-submissions-avg-delay";
	public static final String METRIC_GRADES_GRADED = "grades-graded";
	public static final String METRIC_GRADES_COMPLETE = "grades-complete";
	public static final String METRIC_GRADES_AVERAGE = "grades-average";
	public static final String METRIC_GRADES_BELOW_THRESHOLD = "grades-below-threshold";
	public static final String METRIC_STUDENT_GRADES_GRADED = "student-grades-graded";
	public static final String METRIC_STUDENT_GRADES_COMPLETE = "student-grades-complete";
	public static final String METRIC_STUDENT_GRADES_BELOW_THRESHOLD = "student-grades-below-threshold";
	public static final String METRIC_COMMUNICATION_AUTHORED = "communication-authored";
	public static final String METRIC_COMMUNICATION_REPLIED = "communication-replied";
	public static final String METRIC_COMMUNICATION_UNANSWERED = "communication-unanswered";
	public static final String METRIC_COMMUNICATION_MOST_ACTIVE = "communication-most-active";
	public static final String METRIC_STUDENT_COMMUNICATION_AUTHORED = "student-communication-authored";
	public static final String METRIC_STUDENT_COMMUNICATION_REPLIED = "student-communication-replied";
	public static final String METRIC_ACTIVITY_EVENTS = "activity-events";
	public static final String METRIC_ACTIVITY_MOST_ACTIVE_TOOL = "activity-most-active-tool";
	public static final String METRIC_ACTIVITY_MOST_ACTIVE_USER = "activity-most-active-user";
	public static final String METRIC_RESOURCES_FILES = "resources-files";
	public static final String METRIC_RESOURCES_OPENED_FILES = "resources-opened-files";
	public static final String METRIC_RESOURCES_MOST_OPENED_FILE = "resources-most-opened-file";
	public static final String METRIC_RESOURCES_USER_OPENED_MORE_FILES = "resources-user-opened-more-files";
	public static final String METRIC_LESSONS_PAGES = "lessons-pages";
	public static final String METRIC_LESSONS_READ_PAGES = "lessons-read-pages";
	public static final String METRIC_LESSONS_MOST_READ_PAGE = "lessons-most-read-page";
	public static final String METRIC_LESSONS_USER_READ_MORE_PAGES = "lessons-user-read-more-pages";
	public static final String METRIC_PLATFORM_LOGINS = "platform-logins";
	public static final String METRIC_PLATFORM_UNIQUE_LOGINS = "platform-unique-logins";
	public static final String METRIC_PLATFORM_SITES_CREATED = "platform-sites-created";
	public static final String METRIC_PLATFORM_SITES_DELETED = "platform-sites-deleted";
	public static final String METRIC_PLATFORM_NEW_USERS = "platform-new-users";
	public static final String METRIC_PLATFORM_TOP_TOOL = "platform-top-tool";
	public static final String METRIC_INST_PRESENCE_ACTIVE_SITES = "inst-presence-active-sites";
	public static final String METRIC_INST_PRESENCE_ACTIVE_30D = "inst-presence-active-30d";
	public static final String METRIC_INST_PRESENCE_LOW_ACTIVITY = "inst-presence-low-activity";
	public static final String METRIC_INST_PRESENCE_NEVER_VISITED = "inst-presence-never-visited";
	public static final String METRIC_INST_LEARNER_EVENTS = "inst-learner-events";
	public static final String METRIC_INST_LEARNER_SITES = "inst-learner-sites";
	public static final String METRIC_INST_LEARNER_TOP_EVENT = "inst-learner-top-event";
	public static final String METRIC_INST_AUTHORING_EVENTS = "inst-authoring-events";
	public static final String METRIC_INST_AUTHORING_SITES = "inst-authoring-sites";
	public static final String METRIC_INST_AUTHORING_TOP_EVENT = "inst-authoring-top-event";
	public static final String METRIC_CLIENT_DESKTOP = "client-desktop";
	public static final String METRIC_CLIENT_MOBILE = "client-mobile";
	public static final String METRIC_CLIENT_TABLET = "client-tablet";
	public static final String METRIC_CLIENT_TOP_BROWSER = "client-top-browser";
	public static final String METRIC_CLIENT_TOP_OS = "client-top-os";

	public static final String HIGHLIGHT_VISITS_LAST_30_DAYS = "visits-last-30-days";
	public static final String HIGHLIGHT_PRESENCE_LAST_30_DAYS = "presence-last-30-days";
	public static final String HIGHLIGHT_SUBMISSIONS_STATUS_SHARE = "submissions-status-share";
	public static final String HIGHLIGHT_GRADES_FUNNEL = "grades-funnel";
	public static final String HIGHLIGHT_COMMUNICATION_SHARE = "communication-share";
	public static final String HIGHLIGHT_PLATFORM_LOGIN_TREND = "platform-login-trend";
	public static final String HIGHLIGHT_CLIENT_DEVICE_SHARE = "client-device-share";
	public static final String HIGHLIGHT_INST_PRESENCE_SHARE = "inst-presence-share";
	public static final String HIGHLIGHT_INST_LEARNER_EVENT_SHARE = "inst-learner-event-share";
	public static final String HIGHLIGHT_INST_AUTHORING_EVENT_SHARE = "inst-authoring-event-share";

	public static final String CHART_COLOR_INFO = "info";
	public static final String CHART_COLOR_SUCCESS = "success";
	public static final String CHART_COLOR_WARNING = "warning";
	public static final String CHART_COLOR_DANGER = "danger";
}
