/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsAuthz;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.view.SiteStatsOverview;
import org.sakaiproject.sitestats.api.view.SiteStatsReportPreviewService;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsReportSummary;
import org.sakaiproject.sitestats.api.view.SiteStatsReportView;
import org.sakaiproject.sitestats.api.view.SiteStatsViewService;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetMetric;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetTab;
import org.sakaiproject.tool.api.SessionManager;

public class SiteStatsViewServiceImpl implements SiteStatsViewService {

	@Setter private StatsManager statsManager;
	@Setter private SiteStatsReportViewMapper siteStatsReportViewMapper;
	private ReportManager reportManager;
	private SiteStatsWidgetCatalog siteStatsWidgetCatalog;
	private SiteStatsReportAccess siteStatsReportAccess = new SiteStatsReportAccess();

	public void setStatsAuthz(StatsAuthz statsAuthz) {
		siteStatsReportAccess.setStatsAuthz(statsAuthz);
	}

	public void setReportManager(ReportManager reportManager) {
		this.reportManager = reportManager;
		siteStatsReportAccess.setReportManager(reportManager);
	}

	public void setSiteStatsReportPreviewService(SiteStatsReportPreviewService siteStatsReportPreviewService) {
		siteStatsReportAccess.setSiteStatsReportPreviewService(siteStatsReportPreviewService);
	}

	public void setSessionManager(SessionManager sessionManager) {
		siteStatsReportAccess.setSessionManager(sessionManager);
	}

	public void setSiteStatsWidgetCatalog(SiteStatsWidgetCatalog siteStatsWidgetCatalog) {
		this.siteStatsWidgetCatalog = siteStatsWidgetCatalog;
		siteStatsReportAccess.setSiteStatsWidgetCatalog(siteStatsWidgetCatalog);
	}

	@Override
	public SiteStatsOverview getOverview(String siteId) {
		siteStatsReportAccess.assertCanView(siteId);

		boolean allAllowed = siteStatsReportAccess.isViewAllAllowed(siteId);
		boolean ownAllowed = siteStatsReportAccess.isViewOwnAllowed(siteId);
		boolean adminAllowed = siteStatsReportAccess.isViewAdminAllowed(siteId);
		return siteStatsWidgetCatalog.getOverview(siteId, allAllowed, ownAllowed, adminAllowed);
	}

	@Override
	public SiteStatsWidgetTab getWidgetTab(String siteId, String widgetId, String tabId) {
		siteStatsReportAccess.assertCanViewWidget(siteId, widgetId);
		return siteStatsWidgetCatalog.getWidgetTab(siteId, widgetId, tabId);
	}

	@Override
	public List<SiteStatsWidgetMetric> getWidgetMetrics(String siteId, String widgetId) {
		siteStatsReportAccess.assertCanViewWidget(siteId, widgetId);
		return siteStatsWidgetCatalog.getWidgetMetrics(siteId, widgetId);
	}

	@Override
	public SiteStatsWidgetMetric getWidgetMetric(String siteId, String widgetId, String metricId) {
		siteStatsReportAccess.assertCanViewMetric(siteId, widgetId, metricId);
		return siteStatsWidgetCatalog.getWidgetMetric(siteId, widgetId, metricId);
	}

	@Override
	public List<SiteStatsReportSummary> getReports(String siteId) {
		siteStatsReportAccess.assertCanViewAll(siteId);

		List<SiteStatsReportSummary> summaries = new ArrayList<SiteStatsReportSummary>();
		List<ReportDef> reportDefs = reportManager.getReportDefinitions(siteId, true, false);
		for (ReportDef reportDef : reportDefs) {
			SiteStatsReportSummary summary = new SiteStatsReportSummary();
			summary.setId(reportDef.getId());
			summary.setSiteId(siteId);
			summary.setTitle(siteStatsReportViewMapper.localizedReportTitle(reportDef));
			summary.setDescription(siteStatsReportViewMapper.localizedReportDescription(reportDef));
			summary.setHidden(reportDef.isHidden());
			summaries.add(summary);
		}
		return summaries;
	}

	@Override
	public SiteStatsReportView getReport(String siteId, long reportId, SiteStatsReportRequest request) {
		siteStatsReportAccess.assertCanViewAll(siteId);

		ReportDef reportDef = siteStatsReportAccess.persistedReportDefinition(reportId);
		ReportDef safeReportDef = new ReportDef(reportDef, siteId);
		PrefsData prefsData = statsManager.getPreferences(siteId, false);
		Report report = reportManager.getReport(safeReportDef, prefsData.isListToolEventsOnlyAvailableInSite(), null, true);

		SiteStatsReportView view = siteStatsReportViewMapper.mapReportView(siteId, report, request, prefsData);
		view.setReportId(Long.valueOf(reportId));
		view.setTitle(siteStatsReportViewMapper.localizedReportTitle(reportDef));
		view.setSummary(siteStatsReportViewMapper.mapSummary(report));
		return view;
	}

	@Override
	public SiteStatsReportView getPreviewReport(String siteId, String previewId, SiteStatsReportRequest request) {
		siteStatsReportAccess.assertCanViewAll(siteId);

		ReportDef reportDef = siteStatsReportAccess.previewReportDefinition(siteId, previewId);
		ReportDef safeReportDef = new ReportDef(reportDef, siteId);
		PrefsData prefsData = statsManager.getPreferences(siteId, false);
		Report report = reportManager.getReport(safeReportDef, prefsData.isListToolEventsOnlyAvailableInSite(), null, true);

		SiteStatsReportView view = siteStatsReportViewMapper.mapReportView(siteId, report, request, prefsData);
		view.setTitle(siteStatsReportViewMapper.localizedReportTitle(safeReportDef));
		view.setSummary(siteStatsReportViewMapper.mapSummary(report));
		return view;
	}

	@Override
	public SiteStatsReportView getWidgetReport(String siteId, String widgetId, String tabId, SiteStatsReportRequest request) {
		siteStatsReportAccess.assertCanViewWidget(siteId, widgetId);

		SiteStatsReportRequest safeRequest = SiteStatsReportRequests.orDefault(request);
		String userId = siteStatsWidgetCatalog.isOwnOnlyWidget(widgetId) ? siteStatsReportAccess.currentUserId() : null;
		WidgetReportDefinition definition = siteStatsWidgetCatalog.getWidgetReportDefinition(siteId, widgetId, tabId, safeRequest, userId);
		return buildWidgetReportView(siteId, definition, safeRequest, widgetId, tabId, null,
				"Unknown SiteStats widget report: " + widgetId + "/" + tabId);
	}

	@Override
	public SiteStatsReportView getWidgetMetricReport(String siteId, String widgetId, String metricId, SiteStatsReportRequest request) {
		siteStatsReportAccess.assertCanViewMetric(siteId, widgetId, metricId);

		SiteStatsReportRequest safeRequest = SiteStatsReportRequests.orDefault(request);
		String userId = siteStatsWidgetCatalog.isOwnOnlyMetric(widgetId, metricId) ? siteStatsReportAccess.currentUserId() : null;
		WidgetReportDefinition definition = siteStatsWidgetCatalog.getWidgetMetricReportDefinition(siteId, widgetId, metricId, userId);
		return buildWidgetReportView(siteId, definition, safeRequest, widgetId, null, metricId,
				"Unknown SiteStats widget metric report: " + widgetId + "/" + metricId);
	}

	private SiteStatsReportView buildWidgetReportView(String siteId, WidgetReportDefinition definition, SiteStatsReportRequest safeRequest,
			String widgetId, String tabId, String metricId, String unknownReportMessage) {
		PrefsData prefsData = statsManager.getPreferences(siteId, false);
		boolean restrictToToolsInSite = prefsData.isListToolEventsOnlyAvailableInSite();

		Report chartReport = null;
		Report tableReport = null;
		if (safeRequest.isIncludeChart() && definition.getChartReportDef() != null) {
			chartReport = reportManager.getReport(definition.getChartReportDef(), restrictToToolsInSite, null, false);
		}
		if (safeRequest.isIncludeTable() && definition.getTableReportDef() != null) {
			tableReport = reportManager.getReport(definition.getTableReportDef(), restrictToToolsInSite, null, false);
		}

		Report baseReport = tableReport != null ? tableReport : chartReport;
		if (baseReport == null) {
			throw new IllegalArgumentException(unknownReportMessage);
		}

		SiteStatsReportView view = siteStatsReportViewMapper.mapReportShell(siteId, baseReport);
		view.setWidgetId(widgetId);
		if (StringUtils.isNotBlank(tabId)) {
			view.setTabId(tabId);
		}
		if (StringUtils.isNotBlank(metricId)) {
			view.setMetricId(metricId);
		}
		view.setTitle(definition.getTitle());
		if (chartReport != null) {
			view.setChart(siteStatsReportViewMapper.mapChart(chartReport, prefsData));
		}
		if (tableReport != null) {
			view.setTable(siteStatsReportViewMapper.mapTable(tableReport, safeRequest));
		}
		return view;
	}

}
