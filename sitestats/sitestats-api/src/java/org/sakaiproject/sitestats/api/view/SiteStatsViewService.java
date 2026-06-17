/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.api.view;

import java.util.List;

public interface SiteStatsViewService {

	public SiteStatsOverview getOverview(String siteId);

	public List<SiteStatsReportSummary> getReports(String siteId);

	public SiteStatsReportView getReport(String siteId, long reportId, SiteStatsReportRequest request);

	public SiteStatsReportView getPreviewReport(String siteId, String previewId, SiteStatsReportRequest request);

	public SiteStatsReportView getWidgetReport(String siteId, String widgetId, String tabId, SiteStatsReportRequest request);
}
