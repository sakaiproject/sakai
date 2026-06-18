/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.AUDIENCE_ALL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_ACTIVITY_EVENTS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_ACTIVITY_MOST_ACTIVE_TOOL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_ACTIVITY_MOST_ACTIVE_USER;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_TOOL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_USER;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_ROLE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_TOOL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_ACTIVITY;

import java.util.Arrays;

import org.sakaiproject.sitestats.api.EventStat;
import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;

public class ActivityWidgetDefinition extends AbstractSiteStatsWidgetDefinition {

	@Override
	public WidgetSpec getSpec() {
		return widgetSpec(WIDGET_ACTIVITY, "overview_title_activity", "sakai-poll", AUDIENCE_ALL, () -> true,
				tabs(
						tabSpec(WIDGET_ACTIVITY, TAB_BY_DATE, "overview_tab_bydate", this::activityByDateDefinition,
								FILTER_DATE, FILTER_ROLE,
								FILTER_TOOL),
						tabSpec(WIDGET_ACTIVITY, TAB_BY_USER, "overview_tab_byuser", this::activityByUserDefinition,
								FILTER_DATE, FILTER_ROLE,
								FILTER_TOOL),
						tabSpec(WIDGET_ACTIVITY, TAB_BY_TOOL, "overview_tab_bytool", this::activityByToolDefinition,
								FILTER_DATE, FILTER_ROLE,
								FILTER_TOOL)),
				metrics(
						metricSpec(WIDGET_ACTIVITY, METRIC_ACTIVITY_EVENTS, "overview_title_events_sum", AUDIENCE_ALL,
								this::activityEventsMetricDefinition, this::activityEventsValue),
						metricSpec(WIDGET_ACTIVITY, METRIC_ACTIVITY_MOST_ACTIVE_TOOL, "overview_title_mostactivetool_sum", AUDIENCE_ALL,
								this::activityMostActiveToolMetricDefinition, this::activityMostActiveToolValue),
						metricSpec(WIDGET_ACTIVITY, METRIC_ACTIVITY_MOST_ACTIVE_USER, "overview_title_mostactiveuser_sum", AUDIENCE_ALL,
								this::activityMostActiveUserMetricDefinition, this::activityMostActiveUserValue)));
	}

	private WidgetReportDefinition activityByDateDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = support.activityBase(siteId, request);
		reportDef.getReportParams().setHowTotalsBy(support.dateTotals(request));
		support.applyDateGrouping(reportDef.getReportParams(), request, true);
		reportDef.getReportParams().setHowChartType(StatsManager.CHARTTYPE_TIMESERIESBAR);
		reportDef.getReportParams().setHowChartSource(StatsManager.T_DATE);
		reportDef.getReportParams().setHowChartSeriesSource(StatsManager.T_NONE);
		return new WidgetReportDefinition(support.message("overview_title_activity"), reportDef, reportDef);
	}

	private WidgetReportDefinition activityByUserDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef chart = support.activityBase(siteId, request);
		chart.getReportParams().setHowTotalsBy(support.dateTotals(request, StatsManager.T_USER));
		support.applyDateGrouping(chart.getReportParams(), request, false);
		chart.getReportParams().setHowChartType(StatsManager.CHARTTYPE_PIE);
		chart.getReportParams().setHowChartSource(StatsManager.T_USER);
		ReportDef table = new ReportDef(chart, siteId);
		table.getReportParams().setHowTotalsBy(Arrays.asList(StatsManager.T_USER));
		table.getReportParams().setHowSortBy(StatsManager.T_TOTAL);
		return new WidgetReportDefinition(support.message("overview_title_activity"), chart, table);
	}

	private WidgetReportDefinition activityByToolDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef chart = support.activityBase(siteId, request);
		chart.getReportParams().setHowTotalsBy(Arrays.asList(StatsManager.T_TOOL));
		chart.getReportParams().setHowSortBy(StatsManager.T_DATE);
		chart.getReportParams().setHowChartType(StatsManager.CHARTTYPE_PIE);
		chart.getReportParams().setHowChartSource(StatsManager.T_TOOL);
		ReportDef table = new ReportDef(chart, siteId);
		table.getReportParams().setHowSortBy(StatsManager.T_TOTAL);
		return new WidgetReportDefinition(support.message("overview_title_activity"), chart, table);
	}

	private WidgetReportDefinition activityEventsMetricDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = support.activityMetricBase(siteId);
		reportDef.getReportParams().setHowTotalsBy(Arrays.asList(StatsManager.T_EVENT));
		reportDef.getReportParams().setHowSortBy(StatsManager.T_EVENT);
		reportDef.getReportParams().setHowSortAscending(true);
		reportDef.getReportParams().setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
		return new WidgetReportDefinition(support.message("overview_title_events_sum"), null, reportDef);
	}

	private WidgetReportDefinition activityMostActiveToolMetricDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = support.activityMetricBase(siteId);
		reportDef.getReportParams().setHowTotalsBy(Arrays.asList(StatsManager.T_TOOL));
		reportDef.getReportParams().setHowSortBy(StatsManager.T_TOOL);
		reportDef.getReportParams().setHowPresentationMode(ReportManager.HOW_PRESENTATION_BOTH);
		reportDef.getReportParams().setHowChartType(StatsManager.CHARTTYPE_PIE);
		reportDef.getReportParams().setHowChartSource(StatsManager.T_TOOL);
		return new WidgetReportDefinition(support.message("overview_title_mostactivetool_sum"), reportDef, reportDef);
	}

	private WidgetReportDefinition activityMostActiveUserMetricDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = support.activityMetricBase(siteId);
		reportDef.getReportParams().setHowTotalsBy(Arrays.asList(StatsManager.T_USER));
		reportDef.getReportParams().setHowSortBy(StatsManager.T_TOTAL);
		reportDef.getReportParams().setHowSortAscending(false);
		reportDef.getReportParams().setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
		return new WidgetReportDefinition(support.message("overview_title_mostactiveuser_sum"), null, reportDef);
	}

	private WidgetMetricValue activityEventsValue(String siteId, String userId) {
		return WidgetMetricValue.of(Long.toString(support.getStatsManager().getTotalSiteActivity(siteId,
				support.getStatsManager().getPreferences(siteId, true).getToolEventsStringList())));
	}

	private WidgetMetricValue activityMostActiveToolValue(String siteId, String userId) {
		ReportDef reportDef = support.activityMetricBase(siteId);
		reportDef.getReportParams().setHowTotalsBy(Arrays.asList(StatsManager.T_TOOL));
		reportDef.getReportParams().setHowSortBy(StatsManager.T_TOTAL);
		reportDef.getReportParams().setHowSortAscending(false);
		Report report = support.getReportManager().getReport(reportDef, true, null, false);
		String toolId = null;
		long toolActivity = 0;
		long totalActivity = 0;
		for (Stat stat : report.getReportData()) {
			EventStat eventStat = (EventStat) stat;
			if (toolId == null) {
				toolId = eventStat.getToolId();
				toolActivity = eventStat.getCount();
			}
			totalActivity += eventStat.getCount();
		}
		String toolName = toolId == null ? "-" : support.getEventRegistryService().getToolName(toolId);
		return WidgetMetricValue.withPercentageAndDetail(toolName, (int) support.percent(toolActivity, totalActivity), toolName);
	}

	private WidgetMetricValue activityMostActiveUserValue(String siteId, String userId) {
		ReportDef reportDef = support.activityMetricBase(siteId);
		reportDef.getReportParams().setHowTotalsBy(Arrays.asList(StatsManager.T_USER));
		reportDef.getReportParams().setHowSortBy(StatsManager.T_TOTAL);
		reportDef.getReportParams().setHowSortAscending(false);
		Report report = support.getReportManager().getReport(reportDef, true, null, false);
		String activeUserId = null;
		long userActivity = 0;
		long totalActivity = 0;
		for (Stat stat : report.getReportData()) {
			EventStat eventStat = (EventStat) stat;
			if (activeUserId == null) {
				activeUserId = eventStat.getUserId();
				userActivity = eventStat.getCount();
			}
			totalActivity += eventStat.getCount();
		}
		String displayId = activeUserId == null ? "-" : support.userDisplayId(activeUserId);
		return WidgetMetricValue.withPercentageAndDetail(displayId, (int) support.percent(userActivity, totalActivity), support.userTooltip(activeUserId));
	}
}
