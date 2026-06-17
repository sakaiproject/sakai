/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import lombok.Setter;

import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.view.SiteStatsReportExportService;

public class SiteStatsReportExportServiceImpl implements SiteStatsReportExportService {

	@Setter private StatsManager statsManager;
	@Setter private ReportManager reportManager;
	@Setter private SiteStatsWidgetCatalog siteStatsWidgetCatalog;
	@Setter private SiteStatsReportAccess siteStatsReportAccess;

	@Override
	public boolean canExportPersistedReport(String siteId, long reportId) {
		try {
			siteStatsReportAccess.assertCanViewAll(siteId);
			siteStatsReportAccess.persistedReportDefinition(reportId);
			return true;
		} catch (SecurityException | IllegalArgumentException e) {
			return false;
		}
	}

	@Override
	public boolean canExportPreviewReport(String siteId, String previewId) {
		try {
			siteStatsReportAccess.assertCanViewAll(siteId);
			siteStatsReportAccess.previewReportDefinition(siteId, previewId);
			return true;
		} catch (SecurityException | IllegalArgumentException e) {
			return false;
		}
	}

	@Override
	public boolean canExportWidgetMetricReport(String siteId, String widgetId, String metricId) {
		try {
			siteStatsReportAccess.assertCanViewMetric(siteId, widgetId, metricId);
			siteStatsWidgetCatalog.getWidgetMetricReportDefinition(siteId, widgetId, metricId,
					siteStatsWidgetCatalog.isOwnOnlyMetric(widgetId, metricId) ? siteStatsReportAccess.currentUserId() : null);
			return true;
		} catch (SecurityException | IllegalArgumentException e) {
			return false;
		}
	}

	@Override
	public Report getPersistedReport(String siteId, long reportId) {
		siteStatsReportAccess.assertCanViewAll(siteId);
		ReportDef safeReportDef = new ReportDef(siteStatsReportAccess.persistedReportDefinition(reportId), siteId);
		return getReport(siteId, safeReportDef);
	}

	@Override
	public Report getPreviewReport(String siteId, String previewId) {
		siteStatsReportAccess.assertCanViewAll(siteId);
		ReportDef reportDef = siteStatsReportAccess.previewReportDefinition(siteId, previewId);
		return getReport(siteId, new ReportDef(reportDef, siteId));
	}

	@Override
	public Report getWidgetMetricReport(String siteId, String widgetId, String metricId) {
		siteStatsReportAccess.assertCanViewMetric(siteId, widgetId, metricId);
		String userId = siteStatsWidgetCatalog.isOwnOnlyMetric(widgetId, metricId) ? siteStatsReportAccess.currentUserId() : null;
		WidgetReportDefinition definition = siteStatsWidgetCatalog.getWidgetMetricReportDefinition(siteId, widgetId, metricId, userId);
		ReportDef reportDef = definition.getTableReportDef() != null ? definition.getTableReportDef() : definition.getChartReportDef();
		if (reportDef == null) {
			throw new IllegalArgumentException("Unknown SiteStats widget metric report: " + widgetId + "/" + metricId);
		}
		return getReport(siteId, reportDef);
	}

	private Report getReport(String siteId, ReportDef reportDef) {
		PrefsData prefsData = statsManager.getPreferences(siteId, false);
		return reportManager.getReport(reportDef, prefsData.isListToolEventsOnlyAvailableInSite(), null, true);
	}
}
