/**********************************************************************************
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.sitestats.impl.view;

import org.sakaiproject.assignment.api.AssignmentServiceConstants;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.sitestats.api.view.SiteStatsChart;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsReportView;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.AUDIENCE_OWN;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_ITEM;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.HIGHLIGHT_SUBMISSIONS_STATUS_SHARE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_STUDENT_SUBMISSIONS_AVG_DELAY;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_STUDENT_SUBMISSIONS_LATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_STUDENT_SUBMISSIONS_MISSED;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_STUDENT_SUBMISSIONS_ON_TIME;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_ITEM;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_STUDENT_SUBMISSIONS;

public class StudentSubmissionsWidgetDefinition extends AbstractSiteStatsWidgetDefinition {

	@Override
	public WidgetSpec getSpec() {
		return widgetSpec(WIDGET_STUDENT_SUBMISSIONS, "overview_title_submissions", "sakai-sitestats", AUDIENCE_OWN, () -> true,
				tabs(viewTabSpec(WIDGET_STUDENT_SUBMISSIONS, TAB_BY_ITEM, "overview_tab_byitem", this::byItem,
						FILTER_DATE, FILTER_ITEM)),
				metrics(
						viewMetricSpec(WIDGET_STUDENT_SUBMISSIONS, METRIC_STUDENT_SUBMISSIONS_ON_TIME,
								"overview_title_submissions_on_time_own", AUDIENCE_OWN, this::onTimeReport, this::onTimeValue),
						viewMetricSpec(WIDGET_STUDENT_SUBMISSIONS, METRIC_STUDENT_SUBMISSIONS_LATE,
								"overview_title_submissions_late_own", AUDIENCE_OWN, this::lateReport, this::lateValue),
						viewMetricSpec(WIDGET_STUDENT_SUBMISSIONS, METRIC_STUDENT_SUBMISSIONS_MISSED,
								"overview_title_submissions_missed_own", AUDIENCE_OWN, this::missedReport, this::missedValue),
						viewMetricSpec(WIDGET_STUDENT_SUBMISSIONS, METRIC_STUDENT_SUBMISSIONS_AVG_DELAY,
								"overview_title_submissions_avg_delay_own", AUDIENCE_OWN, this::lateReport, this::avgDelayValue)),
				highlights(highlightSpec(HIGHLIGHT_SUBMISSIONS_STATUS_SHARE, "overview_title_submissions_status_share",
						this::statusShareChart)),
				toolFilterIds(AssignmentServiceConstants.ASSIGNMENT_TOOL_ID, SamigoConstants.TOOL_ID));
	}

	private SiteStatsReportView byItem(String siteId, SiteStatsReportRequest request, String userId) {
		return submissionsAnalytics().byItemReport(siteId, request, userId);
	}

	private SiteStatsReportView onTimeReport(String siteId, SiteStatsReportRequest request, String userId) {
		return submissionsAnalytics().statusDetailReport(siteId, request, userId, SiteStatsSubmissionsAnalytics.SubmissionStatus.ON_TIME);
	}

	private SiteStatsReportView lateReport(String siteId, SiteStatsReportRequest request, String userId) {
		return submissionsAnalytics().statusDetailReport(siteId, request, userId, SiteStatsSubmissionsAnalytics.SubmissionStatus.LATE);
	}

	private SiteStatsReportView missedReport(String siteId, SiteStatsReportRequest request, String userId) {
		return submissionsAnalytics().statusDetailReport(siteId, request, userId, SiteStatsSubmissionsAnalytics.SubmissionStatus.MISSED);
	}

	private WidgetMetricValue onTimeValue(String siteId, String userId, SiteStatsReportRequest request) {
		return submissionsAnalytics().onTimeValue(siteId, userId, request);
	}

	private WidgetMetricValue lateValue(String siteId, String userId, SiteStatsReportRequest request) {
		return submissionsAnalytics().lateValue(siteId, userId, request);
	}

	private WidgetMetricValue missedValue(String siteId, String userId, SiteStatsReportRequest request) {
		return submissionsAnalytics().missedValue(siteId, userId, request);
	}

	private WidgetMetricValue avgDelayValue(String siteId, String userId, SiteStatsReportRequest request) {
		return submissionsAnalytics().avgDelayValue(siteId, userId, request);
	}

	private SiteStatsChart statusShareChart(String siteId, String userId, SiteStatsReportRequest request) {
		return submissionsAnalytics().statusShareChart(siteId, userId, request);
	}
}
