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
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_PRESENCE_AVERAGE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_PRESENCE_LAST_VISIT;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_PRESENCE_NEVER_VISITED;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_PRESENCE_TOTAL_30D;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_PRESENCE_TOTAL_365D;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_PRESENCE_TOTAL_7D;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_USER;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_PRESENCE_ACCESS;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;

public class PresenceAccessWidgetDefinition extends AbstractSiteStatsWidgetDefinition {

	@Override
	public WidgetSpec getSpec() {
		return widgetSpec(WIDGET_PRESENCE_ACCESS, "overview_title_presence_access", "sakai-sitestats", AUDIENCE_ALL,
				() -> true,
				tabs(
						tabSpec(WIDGET_PRESENCE_ACCESS, TAB_BY_DATE, "overview_tab_bydate", this::presenceByDateDefinition,
								FILTER_DATE, FILTER_ROLE),
						tabSpec(WIDGET_PRESENCE_ACCESS, TAB_BY_USER, "overview_tab_byuser", this::lastVisitByUserDefinition,
								FILTER_DATE, FILTER_ROLE)),
				metrics(
						metricSpec(WIDGET_PRESENCE_ACCESS, METRIC_PRESENCE_LAST_VISIT, "overview_title_last_visit", AUDIENCE_ALL,
								this::lastVisitMetricDefinition, this::lastVisitValue),
						metricSpec(WIDGET_PRESENCE_ACCESS, METRIC_PRESENCE_NEVER_VISITED, "overview_title_never_accessed",
								AUDIENCE_ALL, this::neverVisitedMetricDefinition, this::neverVisitedValue),
						metricSpec(WIDGET_PRESENCE_ACCESS, METRIC_PRESENCE_AVERAGE, "overview_title_presence_time_avg", AUDIENCE_ALL,
								this::presencesEnabled, this::averagePresenceMetricDefinition, this::averagePresenceValue),
						metricSpec(WIDGET_PRESENCE_ACCESS, METRIC_PRESENCE_TOTAL_7D, "overview_title_presence_last7days", AUDIENCE_ALL,
								this::presencesEnabled, this::presence7dMetricDefinition, this::presence7dValue),
						metricSpec(WIDGET_PRESENCE_ACCESS, METRIC_PRESENCE_TOTAL_30D, "overview_title_presence_last30days", AUDIENCE_ALL,
								this::presencesEnabled, this::presence30dMetricDefinition, this::presence30dValue),
						metricSpec(WIDGET_PRESENCE_ACCESS, METRIC_PRESENCE_TOTAL_365D, "overview_title_presence_last365days", AUDIENCE_ALL,
								this::presencesEnabled, this::presence365dMetricDefinition, this::presence365dValue)));
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

	private WidgetReportDefinition lastVisitByUserDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef table = lastVisitByUserReport(siteId, request);
		ReportDef chart = reportFactory().presenceBase(siteId, request);
		ReportParams chartParams = chart.getReportParams();
		chartParams.setHowTotalsBy(Arrays.asList(StatsManager.T_USER));
		reportFactory().applyDateGrouping(chartParams, request, false);
		chartParams.setHowSortBy(StatsManager.T_DURATION);
		chartParams.setHowChartType(StatsManager.CHARTTYPE_PIE);
		chartParams.setHowChartSource(StatsManager.T_USER);
		chartParams.setHowPresentationMode(ReportManager.HOW_PRESENTATION_BOTH);
		return new WidgetReportDefinition(message("overview_title_presence_access"),
				message("overview_title_presence_access"), chart, table);
	}

	private ReportDef lastVisitByUserReport(String siteId, SiteStatsReportRequest request) {
		ReportDef reportDef = reportFactory().baseReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_EVENTS);
		params.setWhatEventSelType(ReportManager.WHAT_EVENTS_BYEVENTS);
		params.setWhatEventIds(Arrays.asList(StatsManager.SITEVISIT_EVENTID));
		reportFactory().applyRoleFilter(params, request);
		params.setHowTotalsBy(Arrays.asList(StatsManager.T_USER, StatsManager.T_LASTDATE));
		params.setHowSortBy(StatsManager.T_LASTDATE);
		params.setHowSortAscending(false);
		params.setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
		return reportDef;
	}

	private WidgetReportDefinition lastVisitMetricDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = lastVisitByUserReport(siteId, new SiteStatsReportRequest());
		reportDef.getReportParams().setWhen(ReportManager.WHEN_ALL);
		return new WidgetReportDefinition(message("overview_title_last_visit"), null, reportDef);
	}

	private WidgetReportDefinition neverVisitedMetricDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = reportFactory().baseMetricReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_VISITS);
		params.setWhen(ReportManager.WHEN_ALL);
		params.setWho(ReportManager.WHO_NONE);
		params.setHowTotalsBy(Arrays.asList(StatsManager.T_USER));
		params.setHowSort(false);
		params.setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
		return new WidgetReportDefinition(message("overview_title_never_accessed"), null, reportDef);
	}

	private WidgetReportDefinition averagePresenceMetricDefinition(String siteId, SiteStatsReportRequest request, String userId) {
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

	private WidgetMetricValue lastVisitValue(String siteId, String userId) {
		return metricSupport().lastVisitValue(siteId, null, true);
	}

	private WidgetMetricValue neverVisitedValue(String siteId, String userId) {
		Set<String> siteUsers = siteUsers(siteId);
		Set<String> usersWithVisits = usersWithVisits(siteId);
		int count = 0;
		for (String siteUser : siteUsers) {
			if (!usersWithVisits.contains(siteUser)) {
				count++;
			}
		}
		return WidgetMetricValue.withPercentage(String.valueOf(count), (int) metricSupport().percent(count, siteUsers.size()));
	}

	private WidgetMetricValue averagePresenceValue(String siteId, String userId) {
		return metricSupport().averagePresencePerVisit(siteId);
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

	private boolean presencesEnabled() {
		return metricSupport().presencesEnabled();
	}

	private Set<String> siteUsers(String siteId) {
		Set<String> users = statsManager().getSiteUsers(siteId);
		return users == null ? Collections.<String>emptySet() : users;
	}

	private Set<String> usersWithVisits(String siteId) {
		Set<String> users = statsManager().getUsersWithVisits(siteId);
		return users == null ? new HashSet<String>() : users;
	}
}
