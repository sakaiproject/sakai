/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.api.view;

import java.util.List;

public interface SiteStatsViewService {

	public SiteStatsOverview getOverview(String siteId);

	public SiteStatsOverview getInstitutionalOverview(String siteId);

	/**
	 * Widget ids available for the course dashboard picker.
	 * Returns {@code AUDIENCE_ALL} when the user has {@code sitestats.all}, otherwise
	 * {@code AUDIENCE_OWN} when the user has {@code sitestats.own}. Empty when neither is granted.
	 */
	public List<String> getCourseDashboardWidgetIds(String siteId);

	/**
	 * Widget ids available for the home dashboard picker ({@code AUDIENCE_ADMIN} only).
	 * Empty when the current user lacks {@code sitestats.admin.view} for the site.
	 */
	public List<String> getHomeDashboardWidgetIds(String siteId);

	public SiteStatsWidgetTab getWidgetTab(String siteId, String widgetId, String tabId);

	public List<SiteStatsWidgetMetric> getWidgetMetrics(String siteId, String widgetId);

	public List<SiteStatsWidgetMetric> getWidgetMetrics(String siteId, String widgetId, SiteStatsReportRequest request);

	public List<SiteStatsChart> getWidgetHighlights(String siteId, String widgetId, SiteStatsReportRequest request);

	public SiteStatsWidgetMetric getWidgetMetric(String siteId, String widgetId, String metricId);

	public List<SiteStatsReportSummary> getReports(String siteId);

	public SiteStatsReportView getReport(String siteId, long reportId, SiteStatsReportRequest request);

	public SiteStatsReportView getPreviewReport(String siteId, String previewId, SiteStatsReportRequest request);

	public SiteStatsReportView getWidgetReport(String siteId, String widgetId, String tabId, SiteStatsReportRequest request);

	public SiteStatsReportView getWidgetMetricReport(String siteId, String widgetId, String metricId, SiteStatsReportRequest request);

	public SiteStatsReportView getServerWideReport(String siteId, String reportType);
}
