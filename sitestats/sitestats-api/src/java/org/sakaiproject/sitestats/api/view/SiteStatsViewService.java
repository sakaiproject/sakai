/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.api.view;

import java.util.List;

public interface SiteStatsViewService {

	public SiteStatsOverview getOverview(String siteId);

	public SiteStatsWidgetTab getWidgetTab(String siteId, String widgetId, String tabId);

	public List<SiteStatsWidgetMetric> getWidgetMetrics(String siteId, String widgetId);

	public SiteStatsWidgetMetric getWidgetMetric(String siteId, String widgetId, String metricId);

	public List<SiteStatsReportSummary> getReports(String siteId);

	public SiteStatsReportView getReport(String siteId, long reportId, SiteStatsReportRequest request);

	public SiteStatsReportView getPreviewReport(String siteId, String previewId, SiteStatsReportRequest request);

	public SiteStatsReportView getWidgetReport(String siteId, String widgetId, String tabId, SiteStatsReportRequest request);

	public SiteStatsReportView getWidgetMetricReport(String siteId, String widgetId, String metricId, SiteStatsReportRequest request);

	public SiteStatsReportView getServerWideReport(String siteId, String reportType);
}
