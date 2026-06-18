/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Setter;

import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;

public class WidgetReportDefFactory {

	@Setter private SiteStatsWidgetContext context;
	@Setter private WidgetFilterCatalog filterCatalog;

	ReportDef baseReportDef(String siteId) {
		ReportDef reportDef = new ReportDef();
		reportDef.setId(0);
		reportDef.setSiteId(siteId);
		ReportParams params = new ReportParams(siteId);
		params.setWhen(ReportManager.WHEN_LAST7DAYS);
		params.setWho(ReportManager.WHO_ALL);
		params.setHowPresentationMode(ReportManager.HOW_PRESENTATION_BOTH);
		params.setHowSort(true);
		params.setHowSortAscending(false);
		reportDef.setReportParams(params);
		return reportDef;
	}

	ReportDef baseMetricReportDef(String siteId) {
		ReportDef reportDef = baseReportDef(siteId);
		reportDef.getReportParams().setWhen(ReportManager.WHEN_ALL);
		return reportDef;
	}

	ReportDef activityBase(String siteId, SiteStatsReportRequest request) {
		ReportDef reportDef = baseReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_EVENTS);
		params.setWhatEventSelType(ReportManager.WHAT_EVENTS_BYEVENTS);
		params.setWhatEventIds(context.getSiteStatsToolEventsService().getEventsForToolFilter(
				filterCatalog.toolFilter(request), siteId, context.getStatsManager().getPreferences(siteId, true), false));
		applyRoleFilter(params, request);
		return reportDef;
	}

	ReportDef activityMetricBase(String siteId) {
		ReportDef reportDef = baseMetricReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_EVENTS);
		params.setWhatEventSelType(ReportManager.WHAT_EVENTS_BYEVENTS);
		params.setWhatEventIds(context.getStatsManager().getPreferences(siteId, true).getToolEventsStringList());
		params.setWhen(ReportManager.WHEN_ALL);
		params.setWho(ReportManager.WHO_ALL);
		params.setHowSort(true);
		return reportDef;
	}

	WidgetReportDefinition resourceLikeByDateDefinition(String siteId, SiteStatsReportRequest request,
			String title, String what, String refRoot, String actionFilter) {
		ReportDef reportDef = resourceLikeBase(siteId, request, what, refRoot, actionFilter);
		reportDef.getReportParams().setHowTotalsBy(dateTotals(request));
		applyDateGrouping(reportDef.getReportParams(), request, true);
		reportDef.getReportParams().setHowChartType(StatsManager.CHARTTYPE_TIMESERIESBAR);
		reportDef.getReportParams().setHowChartSource(StatsManager.T_DATE);
		reportDef.getReportParams().setHowChartSeriesSource(StatsManager.T_NONE);
		return new WidgetReportDefinition(title, reportDef, reportDef);
	}

	WidgetReportDefinition resourceLikeByUserDefinition(String siteId, SiteStatsReportRequest request,
			String title, String what, String refRoot, String actionFilter, String... extraTableTotals) {
		ReportDef chart = resourceLikeBase(siteId, request, what, refRoot, actionFilter);
		chart.getReportParams().setHowTotalsBy(dateTotals(request, StatsManager.T_USER));
		applyDateGrouping(chart.getReportParams(), request, false);
		chart.getReportParams().setHowChartType(StatsManager.CHARTTYPE_PIE);
		chart.getReportParams().setHowChartSource(StatsManager.T_USER);
		ReportDef table = new ReportDef(chart, siteId);
		List<String> tableTotals = new ArrayList<String>();
		tableTotals.add(StatsManager.T_USER);
		tableTotals.addAll(Arrays.asList(extraTableTotals));
		table.getReportParams().setHowTotalsBy(tableTotals);
		table.getReportParams().setHowSortBy(StatsManager.T_TOTAL);
		return new WidgetReportDefinition(title, chart, table);
	}

	WidgetReportDefinition resourceLikeByItemDefinition(String siteId, SiteStatsReportRequest request,
			String title, String what, String refRoot, String actionFilter, String itemColumn) {
		ReportDef chart = resourceLikeBase(siteId, request, what, refRoot, actionFilter);
		chart.getReportParams().setHowTotalsBy(dateTotals(request, itemColumn));
		applyDateGrouping(chart.getReportParams(), request, false);
		chart.getReportParams().setHowChartType(StatsManager.CHARTTYPE_PIE);
		chart.getReportParams().setHowChartSource(itemColumn);
		ReportDef table = new ReportDef(chart, siteId);
		table.getReportParams().setHowTotalsBy(Arrays.asList(itemColumn));
		table.getReportParams().setHowSortBy(StatsManager.T_TOTAL);
		return new WidgetReportDefinition(title, chart, table);
	}

	ReportDef resourceMetricBase(String siteId, String action, String totalsBy) {
		ReportDef reportDef = baseMetricReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_RESOURCES);
		params.setWhatLimitedAction(true);
		params.setWhatResourceAction(action);
		params.setWhatLimitedResourceIds(true);
		params.setWhatResourceIds(Arrays.asList(StatsManager.RESOURCES_DIR + siteId + "/"));
		params.setWhen(ReportManager.WHEN_ALL);
		params.setWho(ReportManager.WHO_ALL);
		params.setHowTotalsBy(Arrays.asList(totalsBy));
		params.setHowSort(true);
		params.setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
		return reportDef;
	}

	void applyRoleFilter(ReportParams params, SiteStatsReportRequest request) {
		params.setWhen(filterCatalog.dateFilter(request));
		String role = filterCatalog.roleFilter(request);
		if (!ReportManager.WHO_ALL.equals(role)) {
			params.setWho(ReportManager.WHO_ROLE);
			params.setWhoRoleId(role);
		}
	}

	void applyDateGrouping(ReportParams params, SiteStatsReportRequest request, boolean sortByDate) {
		String date = filterCatalog.dateFilter(request);
		params.setWhen(date);
		if (date.equals(ReportManager.WHEN_LAST365DAYS) || date.equals(ReportManager.WHEN_ALL)) {
			params.setHowSortBy(sortByDate ? StatsManager.T_DATEMONTH : params.getHowSortBy());
			params.setHowChartSeriesPeriod(StatsManager.CHARTTIMESERIES_MONTH);
		} else if (date.equals(ReportManager.WHEN_LAST30DAYS)) {
			params.setHowSortBy(sortByDate ? StatsManager.T_DATE : params.getHowSortBy());
			params.setHowChartSeriesPeriod(StatsManager.CHARTTIMESERIES_DAY);
		} else {
			params.setHowSortBy(sortByDate ? StatsManager.T_DATE : params.getHowSortBy());
			params.setHowChartSeriesPeriod(StatsManager.CHARTTIMESERIES_WEEKDAY);
		}
	}

	List<String> dateTotals(SiteStatsReportRequest request, String... extraColumns) {
		List<String> totalsBy = new ArrayList<String>();
		String date = filterCatalog.dateFilter(request);
		if (date.equals(ReportManager.WHEN_LAST365DAYS) || date.equals(ReportManager.WHEN_ALL)) {
			totalsBy.add(StatsManager.T_DATEMONTH);
		} else {
			totalsBy.add(StatsManager.T_DATE);
		}
		totalsBy.addAll(Arrays.asList(extraColumns));
		return totalsBy;
	}

	private ReportDef resourceLikeBase(String siteId, SiteStatsReportRequest request, String what, String refRoot, String actionFilter) {
		ReportDef reportDef = baseReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(what);
		params.setWhatLimitedResourceIds(true);
		params.setWhatResourceIds(Arrays.asList(refRoot));
		if (actionFilter != null) {
			params.setWhatLimitedAction(true);
			params.setWhatResourceAction(actionFilter);
		}
		applyRoleFilter(params, request);
		return reportDef;
	}
}
