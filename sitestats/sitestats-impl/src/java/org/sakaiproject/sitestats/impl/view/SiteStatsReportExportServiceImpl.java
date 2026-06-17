/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import javax.persistence.EntityNotFoundException;

import lombok.Setter;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsAuthz;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.view.SiteStatsReportExportService;
import org.sakaiproject.sitestats.api.view.SiteStatsReportPreviewService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

public class SiteStatsReportExportServiceImpl implements SiteStatsReportExportService {

	@Setter private StatsAuthz statsAuthz;
	@Setter private StatsManager statsManager;
	@Setter private ReportManager reportManager;
	@Setter private SiteStatsReportPreviewService siteStatsReportPreviewService;
	@Setter private SessionManager sessionManager;
	@Setter private SiteStatsWidgetCatalog siteStatsWidgetCatalog;

	@Override
	public boolean canExportPersistedReport(String siteId, long reportId) {
		try {
			assertCanViewAll(siteId);
			persistedReportDefinition(reportId);
			return true;
		} catch (RuntimeException e) {
			return false;
		}
	}

	@Override
	public boolean canExportPreviewReport(String siteId, String previewId) {
		try {
			assertCanViewAll(siteId);
			return siteStatsReportPreviewService != null && siteStatsReportPreviewService.get(siteId, currentUserId(), previewId) != null;
		} catch (RuntimeException e) {
			return false;
		}
	}

	@Override
	public boolean canExportWidgetMetricReport(String siteId, String widgetId, String metricId) {
		try {
			assertCanViewMetric(siteId, widgetId, metricId);
			siteStatsWidgetCatalog.getWidgetMetricReportDefinition(siteId, widgetId, metricId,
					siteStatsWidgetCatalog.isOwnOnlyMetric(widgetId, metricId) ? currentUserId() : null);
			return true;
		} catch (RuntimeException e) {
			return false;
		}
	}

	@Override
	public Report getPersistedReport(String siteId, long reportId) {
		assertCanViewAll(siteId);
		ReportDef safeReportDef = new ReportDef(persistedReportDefinition(reportId), siteId);
		return getReport(siteId, safeReportDef);
	}

	@Override
	public Report getPreviewReport(String siteId, String previewId) {
		assertCanViewAll(siteId);
		ReportDef reportDef = siteStatsReportPreviewService == null ? null : siteStatsReportPreviewService.get(siteId, currentUserId(), previewId);
		if (reportDef == null) {
			throw new IllegalArgumentException("Unknown report preview id: " + previewId);
		}
		return getReport(siteId, new ReportDef(reportDef, siteId));
	}

	@Override
	public Report getWidgetMetricReport(String siteId, String widgetId, String metricId) {
		assertCanViewMetric(siteId, widgetId, metricId);
		String userId = siteStatsWidgetCatalog.isOwnOnlyMetric(widgetId, metricId) ? currentUserId() : null;
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

	private ReportDef persistedReportDefinition(long reportId) {
		if (reportId <= 0) {
			throw new IllegalArgumentException("Unknown report id: " + reportId);
		}
		try {
			ReportDef reportDef = reportManager.getReportDefinition(reportId);
			if (reportDef != null) {
				return reportDef;
			}
		} catch (EntityNotFoundException e) {
			throw new IllegalArgumentException("Unknown report id: " + reportId, e);
		}
		throw new IllegalArgumentException("Unknown report id: " + reportId);
	}

	private String currentUserId() {
		if (sessionManager == null) {
			throw new SecurityException("Current Sakai session is required to export SiteStats reports");
		}
		Session session = sessionManager.getCurrentSession();
		if (session == null || StringUtils.isBlank(session.getUserId())) {
			throw new SecurityException("Current Sakai user is required to export SiteStats reports");
		}
		return session.getUserId();
	}

	private void assertCanViewAll(String siteId) {
		if (!statsAuthz.isUserAbleToViewSiteStats(siteId) || !statsAuthz.isUserAbleToViewSiteStatsAll(siteId)) {
			throw new SecurityException("Current user cannot export all SiteStats data for site " + siteId);
		}
	}

	private void assertCanViewMetric(String siteId, String widgetId, String metricId) {
		if (!statsAuthz.isUserAbleToViewSiteStats(siteId)) {
			throw new SecurityException("Current user cannot view SiteStats for site " + siteId);
		}
		if (siteStatsWidgetCatalog.isOwnOnlyMetric(widgetId, metricId)) {
			if (!statsAuthz.isUserAbleToViewSiteStatsOwn(siteId)) {
				throw new SecurityException("Current user cannot export own SiteStats data for site " + siteId);
			}
		} else if (!statsAuthz.isUserAbleToViewSiteStatsAll(siteId)) {
			throw new SecurityException("Current user cannot export all SiteStats data for site " + siteId);
		}
	}
}
