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
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_ROLE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.HIGHLIGHT_PRESENCE_LAST_30_DAYS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_PRESENCE_AVERAGE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_PRESENCE_BOUNCE_RATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_PRESENCE_TOTAL_30D;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_PRESENCE_TOTAL_365D;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_PRESENCE_TOTAL_7D;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_USER;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_PRESENCE_ACCESS;

import java.util.Arrays;

import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.api.view.SiteStatsChart;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;

public class PresenceAccessWidgetDefinition extends AbstractSiteStatsWidgetDefinition {

	@Override
	public WidgetSpec getSpec() {
		return widgetSpec(WIDGET_PRESENCE_ACCESS, "overview_title_presence_access", "sakai-sitestats", AUDIENCE_ALL,
				() -> true,
				tabs(
						tabSpec(WIDGET_PRESENCE_ACCESS, TAB_BY_DATE, "overview_tab_bydate", this::presenceByDateDefinition,
								FILTER_DATE, FILTER_ROLE),
						tabSpec(WIDGET_PRESENCE_ACCESS, TAB_BY_USER, "overview_tab_byuser", this::presenceByUserDefinition,
								FILTER_DATE, FILTER_ROLE)),
				metrics(
						metricSpec(WIDGET_PRESENCE_ACCESS, METRIC_PRESENCE_AVERAGE, "overview_title_presence_time_avg", AUDIENCE_ALL,
								this::presencesEnabled, this::medianPresenceMetricDefinition, this::medianPresenceValue),
						metricSpec(WIDGET_PRESENCE_ACCESS, METRIC_PRESENCE_BOUNCE_RATE, "overview_title_bounce_rate", AUDIENCE_ALL,
								this::presencesEnabled, this::bounceRateMetricDefinition, this::bounceRateValue),
						metricSpec(WIDGET_PRESENCE_ACCESS, METRIC_PRESENCE_TOTAL_7D, "overview_title_presence_last7days", AUDIENCE_ALL,
								this::presencesEnabled, this::presence7dMetricDefinition, this::presence7dValue),
						metricSpec(WIDGET_PRESENCE_ACCESS, METRIC_PRESENCE_TOTAL_30D, "overview_title_presence_last30days", AUDIENCE_ALL,
								this::presencesEnabled, this::presence30dMetricDefinition, this::presence30dValue),
						metricSpec(WIDGET_PRESENCE_ACCESS, METRIC_PRESENCE_TOTAL_365D, "overview_title_presence_last365days", AUDIENCE_ALL,
								this::presencesEnabled, this::presence365dMetricDefinition, this::presence365dValue)),
				highlights(highlightSpec(HIGHLIGHT_PRESENCE_LAST_30_DAYS, "overview_title_presence_last30days",
						this::presencesEnabled, this::presenceLast30DaysChart)));
	}

	private WidgetReportDefinition presenceByDateDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = reportFactory().presenceBase(siteId, request);
		ReportParams params = reportDef.getReportParams();
		params.setHowTotalsBy(reportFactory().dateTotals(request));
		reportFactory().applyDateGrouping(params, request, true);
		params.setHowChartType(StatsManager.CHARTTYPE_TIMESERIESBAR);
		params.setHowChartSource(StatsManager.T_DATE);
		params.setHowChartSeriesSource(StatsManager.T_NONE);
		return new WidgetReportDefinition(message("overview_title_presence_access"), reportDef, reportDef);
	}

	private WidgetReportDefinition presenceByUserDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef chart = reportFactory().presenceBase(siteId, request);
		ReportParams chartParams = chart.getReportParams();
		chartParams.setHowTotalsBy(reportFactory().dateTotals(request, StatsManager.T_USER));
		reportFactory().applyDateGrouping(chartParams, request, false);
		chartParams.setHowSortBy(StatsManager.T_DURATION);
		chartParams.setHowChartType(StatsManager.CHARTTYPE_PIE);
		chartParams.setHowChartSource(StatsManager.T_USER);
		chartParams.setHowPresentationMode(ReportManager.HOW_PRESENTATION_BOTH);
		ReportDef table = new ReportDef(chart, siteId);
		table.getReportParams().setHowTotalsBy(Arrays.asList(StatsManager.T_USER));
		table.getReportParams().setHowSortBy(StatsManager.T_DURATION);
		return new WidgetReportDefinition(message("overview_title_presence_access"),
				message("overview_title_presence_access"), chart, table);
	}

	private WidgetReportDefinition medianPresenceMetricDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = reportFactory().baseMetricReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_PRESENCES);
		params.setWhen(ReportManager.WHEN_ALL);
		params.setWho(ReportManager.WHO_ALL);
		params.setHowTotalsBy(Arrays.asList(StatsManager.T_DATE, StatsManager.T_USER));
		params.setHowSortBy(StatsManager.T_DATE);
		params.setHowSortAscending(false);
		params.setHowPresentationMode(ReportManager.HOW_PRESENTATION_BOTH);
		params.setHowChartType(StatsManager.CHARTTYPE_TIMESERIESBAR);
		params.setHowChartSource(StatsManager.T_DATE);
		params.setHowChartSeriesSource(StatsManager.T_NONE);
		params.setHowChartSeriesPeriod(StatsManager.CHARTTIMESERIES_MONTH);
		return new WidgetReportDefinition(message("overview_title_presence_time_avg"), reportDef, reportDef);
	}

	private WidgetReportDefinition bounceRateMetricDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = reportFactory().baseMetricReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_PRESENCES);
		params.setWhen(ReportManager.WHEN_ALL);
		params.setWho(ReportManager.WHO_ALL);
		params.setHowTotalsBy(Arrays.asList(StatsManager.T_DATE, StatsManager.T_USER));
		params.setHowSortBy(StatsManager.T_DURATION);
		params.setHowSortAscending(true);
		params.setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
		return new WidgetReportDefinition(message("overview_title_bounce_rate"), null, reportDef);
	}

	private WidgetReportDefinition presenceRangeMetricDefinition(String siteId, String when, String titleKey) {
		ReportDef reportDef = reportFactory().baseMetricReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_PRESENCES);
		params.setWhen(when);
		params.setWho(ReportManager.WHO_ALL);
		params.setHowTotalsBy(Arrays.asList(StatsManager.T_DATE, StatsManager.T_USER));
		params.setHowSortBy(StatsManager.T_DATE);
		params.setHowSortAscending(false);
		params.setHowPresentationMode(ReportManager.HOW_PRESENTATION_BOTH);
		params.setHowChartType(StatsManager.CHARTTYPE_TIMESERIESBAR);
		params.setHowChartSource(StatsManager.T_DATE);
		params.setHowChartSeriesSource(StatsManager.T_NONE);
		return new WidgetReportDefinition(message(titleKey), reportDef, reportDef);
	}

	private WidgetReportDefinition presence7dMetricDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		return presenceRangeMetricDefinition(siteId, ReportManager.WHEN_LAST7DAYS, "overview_title_presence_last7days");
	}

	private WidgetReportDefinition presence30dMetricDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		return presenceRangeMetricDefinition(siteId, ReportManager.WHEN_LAST30DAYS, "overview_title_presence_last30days");
	}

	private WidgetReportDefinition presence365dMetricDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		return presenceRangeMetricDefinition(siteId, ReportManager.WHEN_LAST365DAYS, "overview_title_presence_last365days");
	}

	private WidgetMetricValue medianPresenceValue(String siteId, String userId) {
		return metricSupport().medianPresencePerVisit(siteId);
	}

	private WidgetMetricValue bounceRateValue(String siteId, String userId) {
		return metricSupport().bounceRateValue(siteId, null);
	}

	private WidgetMetricValue presence7dValue(String siteId, String userId) {
		return metricSupport().presenceDurationValue(siteId, null, ReportManager.WHEN_LAST7DAYS);
	}

	private WidgetMetricValue presence30dValue(String siteId, String userId) {
		return metricSupport().presenceDurationValue(siteId, null, ReportManager.WHEN_LAST30DAYS);
	}

	private WidgetMetricValue presence365dValue(String siteId, String userId) {
		return metricSupport().presenceDurationValue(siteId, null, ReportManager.WHEN_LAST365DAYS);
	}

	private SiteStatsChart presenceLast30DaysChart(String siteId, String userId) {
		return metricSupport().last30DaysPresenceChart(siteId, null);
	}

	private boolean presencesEnabled() {
		return metricSupport().presencesEnabled();
	}
}
