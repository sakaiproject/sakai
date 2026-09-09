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

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.api.view.SiteStatsChart;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.AUDIENCE_OWN;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.HIGHLIGHT_PRESENCE_LAST_30_DAYS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_STUDENT_PRESENCE_AVERAGE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_STUDENT_PRESENCE_BOUNCE_RATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_STUDENT_PRESENCE_TOTAL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_STUDENT_PRESENCE_TOTAL_30D;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_STUDENT_PRESENCE_TOTAL_365D;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_STUDENT_PRESENCE_TOTAL_7D;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_STUDENT_PRESENCE_ACCESS;

public class StudentPresenceAccessWidgetDefinition extends AbstractSiteStatsWidgetDefinition {

	@Override
	public WidgetSpec getSpec() {
		return widgetSpec(WIDGET_STUDENT_PRESENCE_ACCESS, "overview_title_presence_access", "sakai-sitestats", AUDIENCE_OWN,
				() -> true,
				tabs(tabSpec(WIDGET_STUDENT_PRESENCE_ACCESS, TAB_BY_DATE, "overview_tab_bydate",
						this::studentPresenceByDateDefinition, FILTER_DATE)),
				metrics(
						metricSpec(WIDGET_STUDENT_PRESENCE_ACCESS, METRIC_STUDENT_PRESENCE_AVERAGE, "overview_title_presence_time_avg",
								AUDIENCE_OWN, this::presencesEnabled, null, this::studentMedianPresenceValue),
						metricSpec(WIDGET_STUDENT_PRESENCE_ACCESS, METRIC_STUDENT_PRESENCE_BOUNCE_RATE, "overview_title_bounce_rate",
								AUDIENCE_OWN, this::presencesEnabled, null, this::studentBounceRateValue),
						metricSpec(WIDGET_STUDENT_PRESENCE_ACCESS, METRIC_STUDENT_PRESENCE_TOTAL, "overview_title_presence_time",
								AUDIENCE_OWN, this::presencesEnabled, null, this::studentPresenceTotalValue),
						metricSpec(WIDGET_STUDENT_PRESENCE_ACCESS, METRIC_STUDENT_PRESENCE_TOTAL_7D, "overview_title_presence_last7days",
								AUDIENCE_OWN, this::presencesEnabled, null, this::studentPresence7dValue),
						metricSpec(WIDGET_STUDENT_PRESENCE_ACCESS, METRIC_STUDENT_PRESENCE_TOTAL_30D, "overview_title_presence_last30days",
								AUDIENCE_OWN, this::presencesEnabled, null, this::studentPresence30dValue),
						metricSpec(WIDGET_STUDENT_PRESENCE_ACCESS, METRIC_STUDENT_PRESENCE_TOTAL_365D, "overview_title_presence_last365days",
								AUDIENCE_OWN, this::presencesEnabled, null, this::studentPresence365dValue)),
				highlights(highlightSpec(HIGHLIGHT_PRESENCE_LAST_30_DAYS, "overview_title_presence_last30days",
						this::presencesEnabled, this::studentPresenceLast30DaysChart)));
	}

	private WidgetReportDefinition studentPresenceByDateDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = reportFactory().baseReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_PRESENCES);
		params.setWho(ReportManager.WHO_CUSTOM);
		params.setWhoUserIds(Arrays.asList(userId));
		reportFactory().applyDateGrouping(params, request, true);
		params.setHowTotalsBy(reportFactory().dateTotals(request));
		params.setHowChartType(StatsManager.CHARTTYPE_TIMESERIESBAR);
		params.setHowChartSource(StatsManager.T_DATE);
		params.setHowChartSeriesSource(StatsManager.T_NONE);
		return new WidgetReportDefinition(message("overview_title_presence_access"), reportDef, reportDef);
	}

	private WidgetMetricValue studentMedianPresenceValue(String siteId, String userId) {
		return metricSupport().medianPresencePerVisitForUser(siteId, userId);
	}

	private WidgetMetricValue studentBounceRateValue(String siteId, String userId) {
		if (StringUtils.isBlank(userId)) {
			return WidgetMetricValue.of("-");
		}
		return metricSupport().bounceRateValue(siteId, Arrays.asList(userId));
	}

	private WidgetMetricValue studentPresenceTotalValue(String siteId, String userId) {
		return studentPresenceValue(siteId, userId, ReportManager.WHEN_ALL);
	}

	private WidgetMetricValue studentPresence7dValue(String siteId, String userId) {
		return studentPresenceValue(siteId, userId, ReportManager.WHEN_LAST7DAYS);
	}

	private WidgetMetricValue studentPresence30dValue(String siteId, String userId) {
		return studentPresenceValue(siteId, userId, ReportManager.WHEN_LAST30DAYS);
	}

	private WidgetMetricValue studentPresence365dValue(String siteId, String userId) {
		return studentPresenceValue(siteId, userId, ReportManager.WHEN_LAST365DAYS);
	}

	private WidgetMetricValue studentPresenceValue(String siteId, String userId, String when) {
		if (StringUtils.isBlank(userId)) {
			return WidgetMetricValue.of("-");
		}
		return metricSupport().presenceDurationValue(siteId, Arrays.asList(userId), when);
	}

	private SiteStatsChart studentPresenceLast30DaysChart(String siteId, String userId) {
		if (StringUtils.isBlank(userId)) {
			return metricSupport().last30DaysPresenceChart(siteId, null);
		}
		return metricSupport().last30DaysPresenceChart(siteId, Arrays.asList(userId));
	}

	private boolean presencesEnabled() {
		return metricSupport().presencesEnabled();
	}
}
