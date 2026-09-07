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

import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.AUDIENCE_ALL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_GROUP;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_ITEM;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_ROLE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.HIGHLIGHT_GRADES_FUNNEL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_GRADES_AVERAGE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_GRADES_BELOW_THRESHOLD;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_GRADES_COMPLETE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_GRADES_GRADED;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_ITEM;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_USER;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_GRADES;

import org.sakaiproject.sitestats.api.view.SiteStatsChart;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsReportView;

public class GradesWidgetDefinition extends AbstractSiteStatsWidgetDefinition {

	@Override
	public WidgetSpec getSpec() {
		return widgetSpec(WIDGET_GRADES, "overview_title_grades", "sakai-sitestats", AUDIENCE_ALL, () -> true,
				tabs(
						viewTabSpec(WIDGET_GRADES, TAB_BY_USER, "overview_tab_byuser", this::byStudent,
								FILTER_DATE, FILTER_ROLE, FILTER_GROUP, FILTER_ITEM),
						viewTabSpec(WIDGET_GRADES, TAB_BY_ITEM, "overview_tab_byitem", this::byItem,
								FILTER_DATE, FILTER_ROLE, FILTER_GROUP, FILTER_ITEM)),
				metrics(
						viewMetricSpec(WIDGET_GRADES, METRIC_GRADES_GRADED, "overview_title_grades_graded",
								AUDIENCE_ALL, this::gradedReport, this::gradedValue),
						viewMetricSpec(WIDGET_GRADES, METRIC_GRADES_COMPLETE, "overview_title_grades_complete",
								AUDIENCE_ALL, this::completeReport, this::completeValue),
						viewMetricSpec(WIDGET_GRADES, METRIC_GRADES_AVERAGE, "overview_title_grades_average",
								AUDIENCE_ALL, this::byStudent, this::averageValue),
						viewMetricSpec(WIDGET_GRADES, METRIC_GRADES_BELOW_THRESHOLD, "overview_title_grades_below_threshold",
								AUDIENCE_ALL, this::belowReport, this::belowValue)),
				highlights(highlightSpec(HIGHLIGHT_GRADES_FUNNEL, "overview_title_grades_funnel",
						this::gradingFunnelChart)));
	}

	private SiteStatsReportView byStudent(String siteId, SiteStatsReportRequest request, String userId) {
		return gradesAnalytics().byStudentReport(siteId, request, userId);
	}

	private SiteStatsReportView byItem(String siteId, SiteStatsReportRequest request, String userId) {
		return gradesAnalytics().byItemReport(siteId, request, userId);
	}

	private SiteStatsReportView belowReport(String siteId, SiteStatsReportRequest request, String userId) {
		return gradesAnalytics().belowThresholdReport(siteId, request, userId);
	}

	private SiteStatsReportView completeReport(String siteId, SiteStatsReportRequest request, String userId) {
		return gradesAnalytics().completeReport(siteId, request, userId);
	}

	private SiteStatsReportView gradedReport(String siteId, SiteStatsReportRequest request, String userId) {
		return gradesAnalytics().gradedReport(siteId, request, userId);
	}

	private WidgetMetricValue belowValue(String siteId, String userId, SiteStatsReportRequest request) {
		return gradesAnalytics().belowThresholdValue(siteId, userId, request);
	}

	private WidgetMetricValue completeValue(String siteId, String userId, SiteStatsReportRequest request) {
		return gradesAnalytics().completeValue(siteId, userId, request);
	}

	private WidgetMetricValue gradedValue(String siteId, String userId, SiteStatsReportRequest request) {
		return gradesAnalytics().gradedValue(siteId, userId, request);
	}

	private WidgetMetricValue averageValue(String siteId, String userId, SiteStatsReportRequest request) {
		return gradesAnalytics().classAverageValue(siteId, userId, request);
	}

	private SiteStatsChart gradingFunnelChart(String siteId, String userId, SiteStatsReportRequest request) {
		return gradesAnalytics().gradingFunnelChart(siteId, null, request);
	}
}
