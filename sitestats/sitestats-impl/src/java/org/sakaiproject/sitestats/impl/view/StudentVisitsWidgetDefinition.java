/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.AUDIENCE_OWN;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.HIGHLIGHT_VISITS_LAST_30_DAYS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_STUDENT_VISITS_TOTAL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_STUDENT_VISITS_TRAFFIC_TREND;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_STUDENT_VISITS;

import java.util.Arrays;

import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.api.view.SiteStatsChart;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;

public class StudentVisitsWidgetDefinition extends AbstractSiteStatsWidgetDefinition {

	@Override
	public WidgetSpec getSpec() {
		return widgetSpec(WIDGET_STUDENT_VISITS, "overview_title_visits", "sakai-singleuser", AUDIENCE_OWN, () -> true,
				tabs(tabSpec(WIDGET_STUDENT_VISITS, TAB_BY_DATE, "overview_tab_bydate", this::studentVisitsByDateDefinition,
						FILTER_DATE)),
				metrics(
						metricSpec(WIDGET_STUDENT_VISITS, METRIC_STUDENT_VISITS_TOTAL, "overview_title_visits_sum", AUDIENCE_OWN,
								this::studentVisitsByDateDefinition, this::studentVisitsTotalValue),
						metricSpec(WIDGET_STUDENT_VISITS, METRIC_STUDENT_VISITS_TRAFFIC_TREND, "overview_title_traffic_trend",
								AUDIENCE_OWN, this::studentVisitsByDateDefinition, this::studentTrafficTrendValue)),
				highlights(highlightSpec(HIGHLIGHT_VISITS_LAST_30_DAYS, "overview_title_visits_last30days",
						this::studentVisitsLast30DaysChart)));
	}

	private WidgetReportDefinition studentVisitsByDateDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = reportFactory().baseReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_EVENTS);
		params.setWhatEventSelType(ReportManager.WHAT_EVENTS_BYEVENTS);
		params.setWhatEventIds(Arrays.asList(StatsManager.SITEVISIT_EVENTID));
		params.setWho(ReportManager.WHO_CUSTOM);
		params.setWhoUserIds(Arrays.asList(userId));
		reportFactory().applyDateGrouping(params, request, true);
		params.setHowTotalsBy(reportFactory().dateTotals(request));
		params.setHowChartType(StatsManager.CHARTTYPE_TIMESERIESBAR);
		params.setHowChartSource(StatsManager.T_DATE);
		params.setHowChartSeriesSource(StatsManager.T_NONE);
		return new WidgetReportDefinition(message("overview_title_visits"), reportDef, reportDef);
	}

	private WidgetMetricValue studentVisitsTotalValue(String siteId, String userId) {
		return WidgetMetricValue.of(Long.toString(statsManager().getTotalSiteVisitsForUser(siteId, userId)));
	}

	private WidgetMetricValue studentTrafficTrendValue(String siteId, String userId) {
		return metricSupport().trafficTrendValue(siteId, userId);
	}

	private SiteStatsChart studentVisitsLast30DaysChart(String siteId, String userId) {
		return metricSupport().last30DaysVisitsChart(siteId, userId);
	}
}
