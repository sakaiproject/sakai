/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.api.view;

import org.sakaiproject.sitestats.api.report.Report;

public interface SiteStatsReportExportService {

	public boolean canExportPersistedReport(String siteId, long reportId);

	public boolean canExportPreviewReport(String siteId, String previewId);

	public boolean canExportWidgetMetricReport(String siteId, String widgetId, String metricId);

	public Report getPersistedReport(String siteId, long reportId);

	public Report getPreviewReport(String siteId, String previewId);

	public Report getWidgetMetricReport(String siteId, String widgetId, String metricId);
}
