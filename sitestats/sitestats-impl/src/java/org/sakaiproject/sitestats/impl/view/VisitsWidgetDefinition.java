/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.AUDIENCE_ALL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_ROLE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.HIGHLIGHT_VISITS_LAST_30_DAYS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_PRESENCE_LAST_VISIT;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_VISITS_TOTAL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_VISITS_TRAFFIC_TREND;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_VISITS_UNIQUE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_VISITS_USERS_WITH_VISITS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_ROLE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_USER;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_VISITS;

import java.util.Arrays;

import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.api.view.SiteStatsChart;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsReportView;

public class VisitsWidgetDefinition extends AbstractSiteStatsWidgetDefinition {

	@Override
	public WidgetSpec getSpec() {
		return widgetSpec(WIDGET_VISITS, "overview_title_visits", "sakai-singleuser", AUDIENCE_ALL, () -> true,
				tabs(
						tabSpec(WIDGET_VISITS, TAB_BY_DATE, "overview_tab_bydate", this::visitsByDateDefinition,
								FILTER_DATE, FILTER_ROLE),
						tabSpec(WIDGET_VISITS, TAB_BY_USER, "overview_tab_byuser", this::visitsByUserDefinition,
								FILTER_DATE, FILTER_ROLE),
						viewTabSpec(WIDGET_VISITS, TAB_BY_ROLE, "overview_tab_byrole", this::visitsByRoleView,
								FILTER_DATE)),
				metrics(
						metricSpec(WIDGET_VISITS, METRIC_VISITS_TOTAL, "overview_title_visits_sum", AUDIENCE_ALL,
								this::visitsTotalMetricDefinition, this::visitsTotalValue),
						metricSpec(WIDGET_VISITS, METRIC_VISITS_UNIQUE, "overview_title_unique_visits_sum", AUDIENCE_ALL,
								this::visitsUniqueMetricDefinition, this::visitsUniqueValue),
						metricSpec(WIDGET_VISITS, METRIC_VISITS_USERS_WITH_VISITS, "overview_title_enrolled_users_with_visits_sum", AUDIENCE_ALL,
								this::visitsUsersWithVisitsMetricDefinition, this::visitsUsersWithVisitsValue),
						metricSpec(WIDGET_VISITS, METRIC_PRESENCE_LAST_VISIT, "overview_title_last_visit", AUDIENCE_ALL,
								this::lastVisitMetricDefinition, this::lastVisitValue),
						metricSpec(WIDGET_VISITS, METRIC_VISITS_TRAFFIC_TREND, "overview_title_traffic_trend", AUDIENCE_ALL,
								this::visitsTotalMetricDefinition, this::visitsTrafficTrendValue)),
				highlights(highlightSpec(HIGHLIGHT_VISITS_LAST_30_DAYS, "overview_title_visits_last30days",
						this::visitsLast30DaysChart)));
	}

	private WidgetReportDefinition visitsByDateDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = reportFactory().baseReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		if (ReportManager.WHO_ALL.equals(filterCatalog().roleFilter(request))) {
			params.setWhat(ReportManager.WHAT_VISITS_TOTALS);
		} else {
			params.setWhat(ReportManager.WHAT_EVENTS);
			params.setWhatEventSelType(ReportManager.WHAT_EVENTS_BYEVENTS);
			params.setWhatEventIds(Arrays.asList(StatsManager.SITEVISIT_EVENTID));
			params.setWho(ReportManager.WHO_ROLE);
			params.setWhoRoleId(filterCatalog().roleFilter(request));
		}
		reportFactory().applyDateGrouping(params, request, true);
		params.setHowTotalsBy(reportFactory().dateTotals(request, StatsManager.T_VISITS, StatsManager.T_UNIQUEVISITS));
		params.setHowChartType(StatsManager.CHARTTYPE_TIMESERIESBAR);
		params.setHowChartSource(StatsManager.T_DATE);
		params.setHowChartSeriesSource(StatsManager.T_NONE);
		return new WidgetReportDefinition(message("overview_title_visits"), reportDef, reportDef);
	}

	private WidgetReportDefinition visitsByUserDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef chart = visitsByUserChart(siteId, request);
		ReportDef table = new ReportDef(chart, siteId);
		table.getReportParams().setHowTotalsBy(Arrays.asList(StatsManager.T_USER));
		table.getReportParams().setHowSortBy(StatsManager.T_TOTAL);
		String title = message("overview_title_visits");
		return new WidgetReportDefinition(title, title, chart, table);
	}

	private ReportDef visitsByUserChart(String siteId, SiteStatsReportRequest request) {
		ReportDef reportDef = reportFactory().baseReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_EVENTS);
		params.setWhatEventSelType(ReportManager.WHAT_EVENTS_BYEVENTS);
		params.setWhatEventIds(Arrays.asList(StatsManager.SITEVISIT_EVENTID));
		reportFactory().applyRoleFilter(params, request);
		params.setHowTotalsBy(reportFactory().dateTotals(request, StatsManager.T_USER));
		reportFactory().applyDateGrouping(params, request, false);
		params.setHowSortBy(StatsManager.T_TOTAL);
		params.setHowChartType(StatsManager.CHARTTYPE_PIE);
		params.setHowChartSource(StatsManager.T_USER);
		return reportDef;
	}

	private WidgetReportDefinition visitsTotalMetricDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = reportFactory().baseMetricReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_VISITS_TOTALS);
		params.setWhen(ReportManager.WHEN_ALL);
		params.setWho(ReportManager.WHO_ALL);
		params.setHowTotalsBy(Arrays.asList(StatsManager.T_DATE, StatsManager.T_VISITS, StatsManager.T_UNIQUEVISITS));
		params.setHowSortBy(StatsManager.T_DATE);
		params.setHowSortAscending(false);
		params.setHowPresentationMode(ReportManager.HOW_PRESENTATION_BOTH);
		params.setHowChartType(StatsManager.CHARTTYPE_TIMESERIESBAR);
		params.setHowChartSource(StatsManager.T_DATE);
		params.setHowChartSeriesSource(StatsManager.T_NONE);
		params.setHowChartSeriesPeriod(StatsManager.CHARTTIMESERIES_DAY);
		return new WidgetReportDefinition(message("overview_title_visits"), reportDef, reportDef);
	}

	private WidgetReportDefinition visitsUsersWithVisitsMetricDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		return visitsByUserTableDefinition(siteId, "overview_title_enrolled_users_with_visits_sum");
	}

	private WidgetReportDefinition visitsUniqueMetricDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		return visitsByUserTableDefinition(siteId, "overview_title_unique_visits_sum");
	}

	private WidgetReportDefinition visitsByUserTableDefinition(String siteId, String titleKey) {
		ReportDef reportDef = reportFactory().baseMetricReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_VISITS);
		params.setWhen(ReportManager.WHEN_ALL);
		params.setWho(ReportManager.WHO_ALL);
		params.setHowTotalsBy(Arrays.asList(StatsManager.T_USER));
		params.setHowSort(false);
		params.setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
		return new WidgetReportDefinition(message(titleKey), null, reportDef);
	}

	private WidgetReportDefinition lastVisitMetricDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = reportFactory().baseMetricReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_EVENTS);
		params.setWhatEventSelType(ReportManager.WHAT_EVENTS_BYEVENTS);
		params.setWhatEventIds(Arrays.asList(StatsManager.SITEVISIT_EVENTID));
		params.setWhen(ReportManager.WHEN_ALL);
		params.setWho(ReportManager.WHO_ALL);
		params.setHowTotalsBy(Arrays.asList(StatsManager.T_USER, StatsManager.T_LASTDATE));
		params.setHowSort(true);
		params.setHowSortBy(StatsManager.T_LASTDATE);
		params.setHowSortAscending(false);
		params.setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
		return new WidgetReportDefinition(message("overview_title_last_visit"), null, reportDef);
	}

	private SiteStatsReportView visitsByRoleView(String siteId, SiteStatsReportRequest request, String userId) {
		return metricSupport().visitsByRoleView(siteId, request);
	}

	private WidgetMetricValue visitsTotalValue(String siteId, String userId) {
		return WidgetMetricValue.of(Long.toString(statsManager().getTotalSiteVisits(siteId)));
	}

	private WidgetMetricValue visitsUniqueValue(String siteId, String userId) {
		return metricSupport().uniqueVisitsValue(siteId);
	}

	private WidgetMetricValue visitsUsersWithVisitsValue(String siteId, String userId) {
		return metricSupport().membersVisitedValue(siteId);
	}

	private WidgetMetricValue lastVisitValue(String siteId, String userId) {
		return metricSupport().lastVisitValue(siteId, null, true);
	}

	private WidgetMetricValue visitsTrafficTrendValue(String siteId, String userId) {
		return metricSupport().trafficTrendValue(siteId, null);
	}

	private SiteStatsChart visitsLast30DaysChart(String siteId, String userId) {
		return metricSupport().last30DaysVisitsChart(siteId, null);
	}
}
