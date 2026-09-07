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
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.AUDIENCE_ALL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_GROUP;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_ITEM;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_ROLE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.HIGHLIGHT_SUBMISSIONS_STATUS_SHARE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_SUBMISSIONS_AT_RISK;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_SUBMISSIONS_AVG_DELAY;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_SUBMISSIONS_LATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_SUBMISSIONS_MISSED;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_SUBMISSIONS_NEEDS_GRADING;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_SUBMISSIONS_ON_TIME;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_ITEM;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_USER;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_SUBMISSIONS;

public class SubmissionsWidgetDefinition extends AbstractSiteStatsWidgetDefinition {

	@Override
	public WidgetSpec getSpec() {
		return widgetSpec(WIDGET_SUBMISSIONS, "overview_title_submissions", "sakai-sitestats", AUDIENCE_ALL, () -> true,
				tabs(
						viewTabSpec(WIDGET_SUBMISSIONS, TAB_BY_USER, "overview_tab_byuser", this::byStudent,
								FILTER_DATE, FILTER_ROLE, FILTER_GROUP, FILTER_ITEM),
						viewTabSpec(WIDGET_SUBMISSIONS, TAB_BY_ITEM, "overview_tab_byitem", this::byItem,
								FILTER_DATE, FILTER_ROLE, FILTER_GROUP, FILTER_ITEM)),
				metrics(
						viewMetricSpec(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_ON_TIME, "overview_title_submissions_on_time",
								AUDIENCE_ALL, this::onTimeReport, this::onTimeValue),
						viewMetricSpec(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_LATE, "overview_title_submissions_late",
								AUDIENCE_ALL, this::lateReport, this::lateValue),
						viewMetricSpec(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_MISSED, "overview_title_submissions_missed",
								AUDIENCE_ALL, this::missedReport, this::missedValue),
						viewMetricSpec(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_NEEDS_GRADING, "overview_title_submissions_needs_grading",
								AUDIENCE_ALL, this::needsGradingReport, this::needsGradingValue),
						viewMetricSpec(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_AT_RISK, "overview_title_submissions_at_risk",
								AUDIENCE_ALL, this::atRiskReport, this::atRiskValue),
						viewMetricSpec(WIDGET_SUBMISSIONS, METRIC_SUBMISSIONS_AVG_DELAY, "overview_title_submissions_avg_delay",
								AUDIENCE_ALL, this::lateReport, this::avgDelayValue)),
				highlights(highlightSpec(HIGHLIGHT_SUBMISSIONS_STATUS_SHARE, "overview_title_submissions_status_share",
						this::statusShareChart)),
				toolFilterIds(AssignmentServiceConstants.ASSIGNMENT_TOOL_ID, SamigoConstants.TOOL_ID));
	}

	private SiteStatsReportView byStudent(String siteId, SiteStatsReportRequest request, String userId) {
		return submissionsAnalytics().byStudentReport(siteId, request, userId);
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

	private SiteStatsReportView needsGradingReport(String siteId, SiteStatsReportRequest request, String userId) {
		return submissionsAnalytics().needsGradingReport(siteId, request, userId);
	}

	private WidgetMetricValue needsGradingValue(String siteId, String userId, SiteStatsReportRequest request) {
		return submissionsAnalytics().needsGradingValue(siteId, userId, request);
	}

	private SiteStatsReportView atRiskReport(String siteId, SiteStatsReportRequest request, String userId) {
		return submissionsAnalytics().atRiskReport(siteId, request, userId);
	}

	private WidgetMetricValue atRiskValue(String siteId, String userId, SiteStatsReportRequest request) {
		return submissionsAnalytics().atRiskValue(siteId, userId, request);
	}

	private WidgetMetricValue avgDelayValue(String siteId, String userId, SiteStatsReportRequest request) {
		return submissionsAnalytics().avgDelayValue(siteId, userId, request);
	}

	private SiteStatsChart statusShareChart(String siteId, String userId, SiteStatsReportRequest request) {
		return submissionsAnalytics().statusShareChart(siteId, null, request);
	}
}
