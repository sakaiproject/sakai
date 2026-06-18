/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.AUDIENCE_OWN;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_STUDENT_VISITS_AVERAGE_PRESENCE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_STUDENT_VISITS_PRESENCE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_STUDENT_VISITS_TOTAL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_STUDENT_VISITS;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;

public class StudentVisitsWidgetDefinition extends AbstractSiteStatsWidgetDefinition {

	@Override
	public WidgetSpec getSpec() {
		return widgetSpec(WIDGET_STUDENT_VISITS, "overview_title_visits", "sakai-singleuser", AUDIENCE_OWN, () -> true,
				tabs(tabSpec(WIDGET_STUDENT_VISITS, TAB_BY_DATE, "overview_tab_bydate", this::studentVisitsByDateDefinition,
						FILTER_DATE)),
				metrics(
						metricSpec(WIDGET_STUDENT_VISITS, METRIC_STUDENT_VISITS_TOTAL, "overview_title_visits_sum", AUDIENCE_OWN,
								null, this::studentVisitsTotalValue),
						metricSpec(WIDGET_STUDENT_VISITS, METRIC_STUDENT_VISITS_AVERAGE_PRESENCE, "overview_title_presence_time_avg", AUDIENCE_OWN,
								() -> Boolean.TRUE.equals(support.getStatsManager().getEnableSitePresences()), null, this::studentVisitsAveragePresenceValue),
						metricSpec(WIDGET_STUDENT_VISITS, METRIC_STUDENT_VISITS_PRESENCE, "overview_title_presence_time", AUDIENCE_OWN,
								() -> Boolean.TRUE.equals(support.getStatsManager().getEnableSitePresences()), null, this::studentVisitsPresenceValue)));
	}

	private WidgetReportDefinition studentVisitsByDateDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = support.baseReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_EVENTS);
		params.setWhatEventSelType(ReportManager.WHAT_EVENTS_BYEVENTS);
		params.setWhatEventIds(Arrays.asList(StatsManager.SITEVISIT_EVENTID));
		params.setWho(ReportManager.WHO_CUSTOM);
		params.setWhoUserIds(Arrays.asList(userId));
		support.applyDateGrouping(params, request, true);
		params.setHowTotalsBy(support.dateTotals(request));
		params.setHowChartType(StatsManager.CHARTTYPE_TIMESERIESBAR);
		params.setHowChartSource(StatsManager.T_DATE);
		params.setHowChartSeriesSource(StatsManager.T_NONE);
		return new WidgetReportDefinition(support.message("overview_title_visits"), reportDef, reportDef);
	}

	private WidgetMetricValue studentVisitsTotalValue(String siteId, String userId) {
		return WidgetMetricValue.of(Long.toString(support.getStatsManager().getTotalSiteVisitsForUser(siteId, userId)));
	}

	private WidgetMetricValue studentVisitsAveragePresenceValue(String siteId, String userId) {
		long visits = support.getStatsManager().getTotalSiteVisitsForUser(siteId, userId);
		if (visits == 0 || StringUtils.isBlank(userId)) {
			return WidgetMetricValue.of("0");
		}
		long duration = support.sitePresenceDuration(siteId, Arrays.asList(userId));
		return WidgetMetricValue.of(support.msToString(duration / visits));
	}

	private WidgetMetricValue studentVisitsPresenceValue(String siteId, String userId) {
		long duration = StringUtils.isBlank(userId) ? 0 : support.sitePresenceDuration(siteId, Arrays.asList(userId));
		return WidgetMetricValue.of(support.msToString(duration));
	}
}
