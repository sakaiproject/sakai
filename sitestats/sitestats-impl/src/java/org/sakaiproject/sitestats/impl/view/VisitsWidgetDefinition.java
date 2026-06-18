/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.AUDIENCE_ALL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_VISITS_AVERAGE_PRESENCE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_VISITS_ENROLLED_USERS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_VISITS_TOTAL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_VISITS_UNIQUE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_VISITS_USERS_WITHOUT_VISITS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_VISITS_USERS_WITH_VISITS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_USER;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_ROLE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_VISITS;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.Util;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;

public class VisitsWidgetDefinition extends AbstractSiteStatsWidgetDefinition {

	@Override
	public WidgetSpec getSpec() {
		return widgetSpec(WIDGET_VISITS, "overview_title_visits", "sakai-singleuser", AUDIENCE_ALL, () -> true,
				tabs(
						tabSpec(WIDGET_VISITS, TAB_BY_DATE, "overview_tab_bydate", this::visitsByDateDefinition,
								FILTER_DATE, FILTER_ROLE),
						tabSpec(WIDGET_VISITS, TAB_BY_USER, "overview_tab_byuser", this::visitsByUserDefinition,
								FILTER_DATE, FILTER_ROLE)),
				metrics(
						metricSpec(WIDGET_VISITS, METRIC_VISITS_TOTAL, "overview_title_visits_sum", AUDIENCE_ALL,
								this::visitsTotalMetricDefinition, this::visitsTotalValue),
						metricSpec(WIDGET_VISITS, METRIC_VISITS_UNIQUE, "overview_title_unique_visits_sum", AUDIENCE_ALL,
								this::visitsTotalMetricDefinition, this::visitsUniqueValue),
						metricSpec(WIDGET_VISITS, METRIC_VISITS_ENROLLED_USERS, "overview_title_enrolled_users_sum", AUDIENCE_ALL,
								null, this::visitsEnrolledUsersValue),
						metricSpec(WIDGET_VISITS, METRIC_VISITS_USERS_WITH_VISITS, "overview_title_enrolled_users_with_visits_sum", AUDIENCE_ALL,
								this::visitsUsersWithVisitsMetricDefinition, this::visitsUsersWithVisitsValue),
						metricSpec(WIDGET_VISITS, METRIC_VISITS_USERS_WITHOUT_VISITS, "overview_title_enrolled_users_without_visits_sum", AUDIENCE_ALL,
								this::visitsUsersWithoutVisitsMetricDefinition, this::visitsUsersWithoutVisitsValue),
						metricSpec(WIDGET_VISITS, METRIC_VISITS_AVERAGE_PRESENCE, "overview_title_presence_time_avg", AUDIENCE_ALL,
								() -> Boolean.TRUE.equals(support.getStatsManager().getEnableSitePresences()), this::visitsAveragePresenceMetricDefinition,
								this::visitsAveragePresenceValue)));
	}

	private WidgetReportDefinition visitsByDateDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = support.baseReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		if (ReportManager.WHO_ALL.equals(support.roleFilter(request))) {
			params.setWhat(ReportManager.WHAT_VISITS_TOTALS);
		} else {
			params.setWhat(ReportManager.WHAT_EVENTS);
			params.setWhatEventSelType(ReportManager.WHAT_EVENTS_BYEVENTS);
			params.setWhatEventIds(Arrays.asList(StatsManager.SITEVISIT_EVENTID));
			params.setWho(ReportManager.WHO_ROLE);
			params.setWhoRoleId(support.roleFilter(request));
		}
		support.applyDateGrouping(params, request, true);
		params.setHowTotalsBy(support.dateTotals(request, StatsManager.T_VISITS, StatsManager.T_UNIQUEVISITS));
		params.setHowChartType(StatsManager.CHARTTYPE_TIMESERIESBAR);
		params.setHowChartSource(StatsManager.T_DATE);
		params.setHowChartSeriesSource(StatsManager.T_NONE);
		return new WidgetReportDefinition(support.message("overview_title_visits"), reportDef, reportDef);
	}

	private WidgetReportDefinition visitsByUserDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef chart = visitsByUserChart(siteId, request);
		ReportDef table = new ReportDef(chart, siteId);
		table.getReportParams().setHowTotalsBy(Arrays.asList(StatsManager.T_USER));
		table.getReportParams().setHowSortBy(StatsManager.T_TOTAL);
		return new WidgetReportDefinition(support.message("overview_title_visits"), chart, table);
	}

	private ReportDef visitsByUserChart(String siteId, SiteStatsReportRequest request) {
		ReportDef reportDef = support.baseReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_EVENTS);
		params.setWhatEventSelType(ReportManager.WHAT_EVENTS_BYEVENTS);
		params.setWhatEventIds(Arrays.asList(StatsManager.SITEVISIT_EVENTID));
		support.applyRoleFilter(params, request);
		params.setHowTotalsBy(support.dateTotals(request, StatsManager.T_USER));
		support.applyDateGrouping(params, request, false);
		params.setHowSortBy(StatsManager.T_TOTAL);
		params.setHowChartType(StatsManager.CHARTTYPE_PIE);
		params.setHowChartSource(StatsManager.T_USER);
		return reportDef;
	}

	private WidgetReportDefinition visitsTotalMetricDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = support.baseMetricReportDef(siteId);
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
		return new WidgetReportDefinition(support.message("overview_title_visits"), reportDef, reportDef);
	}

	private WidgetReportDefinition visitsUsersWithVisitsMetricDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = support.baseMetricReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_VISITS);
		params.setWhen(ReportManager.WHEN_ALL);
		params.setWho(ReportManager.WHO_ALL);
		params.setHowTotalsBy(Arrays.asList(StatsManager.T_USER));
		params.setHowSort(false);
		params.setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
		return new WidgetReportDefinition(support.message("overview_title_enrolled_users_with_visits_sum"), null, reportDef);
	}

	private WidgetReportDefinition visitsUsersWithoutVisitsMetricDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = support.baseMetricReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_VISITS);
		params.setWhen(ReportManager.WHEN_ALL);
		params.setWho(ReportManager.WHO_NONE);
		params.setHowTotalsBy(Arrays.asList(StatsManager.T_USER));
		params.setHowSort(false);
		params.setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
		return new WidgetReportDefinition(support.message("overview_title_enrolled_users_without_visits_sum"), null, reportDef);
	}

	private WidgetReportDefinition visitsAveragePresenceMetricDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = support.baseMetricReportDef(siteId);
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
		return new WidgetReportDefinition(support.message("overview_title_presence_time_avg"), reportDef, reportDef);
	}

	private WidgetMetricValue visitsTotalValue(String siteId, String userId) {
		return WidgetMetricValue.of(Long.toString(support.getStatsManager().getTotalSiteVisits(siteId)));
	}

	private WidgetMetricValue visitsUniqueValue(String siteId, String userId) {
		return WidgetMetricValue.of(Long.toString(support.getStatsManager().getTotalSiteUniqueVisits(siteId)));
	}

	private WidgetMetricValue visitsEnrolledUsersValue(String siteId, String userId) {
		return WidgetMetricValue.of(Long.toString(support.getStatsManager().getTotalSiteUsers(siteId)));
	}

	private WidgetMetricValue visitsUsersWithVisitsValue(String siteId, String userId) {
		return enrolledUsersVisitMetric(siteId, true);
	}

	private WidgetMetricValue visitsUsersWithoutVisitsValue(String siteId, String userId) {
		return enrolledUsersVisitMetric(siteId, false);
	}

	private WidgetMetricValue enrolledUsersVisitMetric(String siteId, boolean withVisits) {
		Set<String> siteUsers = siteUsers(siteId);
		Set<String> usersWithVisits = usersWithVisits(siteId);
		int count = 0;
		for (String siteUser : siteUsers) {
			if (usersWithVisits.contains(siteUser) == withVisits) {
				count++;
			}
		}
		return WidgetMetricValue.withPercentage(String.valueOf(count), (int) support.percent(count, siteUsers.size()));
	}

	private WidgetMetricValue visitsAveragePresenceValue(String siteId, String userId) {
		long durationInMs = support.sitePresenceDuration(siteId, null);
		Date firstPresenceDate = support.firstPresenceDate(siteId);
		long totalVisits = support.getStatsManager().getTotalSiteVisits(siteId, firstPresenceDate, null);
		double durationInMin = durationInMs == 0 || totalVisits == 0 ? 0 : Util.round((durationInMs / (double) totalVisits) / 1000 / 60, 1);
		return WidgetMetricValue.of(durationInMin + " " + support.message("minutes_abbr"));
	}

	private Set<String> siteUsers(String siteId) {
		Set<String> users = support.getStatsManager().getSiteUsers(siteId);
		return users == null ? Collections.<String>emptySet() : users;
	}

	private Set<String> usersWithVisits(String siteId) {
		Set<String> users = support.getStatsManager().getUsersWithVisits(siteId);
		return users == null ? new HashSet<String>() : users;
	}
}
