/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;

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
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

public class SiteStatsViewServiceImpl implements SiteStatsViewService {

	@Setter private StatsAuthz statsAuthz;
	@Setter private StatsManager statsManager;
	@Setter private ReportManager reportManager;
	@Setter private SiteStatsReportPreviewService siteStatsReportPreviewService;
	@Setter private SessionManager sessionManager;
	@Setter private SiteStatsWidgetCatalog siteStatsWidgetCatalog;
	@Setter private SiteStatsReportViewMapper siteStatsReportViewMapper;

	@Override
	public SiteStatsOverview getOverview(String siteId) {
		assertCanView(siteId);

		boolean allAllowed = statsAuthz.isUserAbleToViewSiteStatsAll(siteId);
		boolean ownAllowed = statsAuthz.isUserAbleToViewSiteStatsOwn(siteId);
		boolean adminAllowed = statsAuthz.isUserAbleToViewSiteStatsAdmin(siteId);
		return siteStatsWidgetCatalog.getOverview(siteId, allAllowed, ownAllowed, adminAllowed);
	}

	@Override
	public List<SiteStatsReportSummary> getReports(String siteId) {
		assertCanViewAll(siteId);

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
		assertCanViewAll(siteId);

		ReportDef reportDef = persistedReportDefinition(reportId);
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
		assertCanViewAll(siteId);

		ReportDef reportDef = siteStatsReportPreviewService == null ? null : siteStatsReportPreviewService.get(siteId, currentUserId(), previewId);
		if (reportDef == null) {
			throw new IllegalArgumentException("Unknown report preview id: " + previewId);
		}

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
		assertCanView(siteId);
		if (siteStatsWidgetCatalog.isOwnOnlyWidget(widgetId)) {
			if (!statsAuthz.isUserAbleToViewSiteStatsOwn(siteId)) {
				throw new SecurityException("Current user cannot view own SiteStats data for site " + siteId);
			}
		} else {
			assertCanViewAll(siteId);
		}

		SiteStatsReportRequest safeRequest = SiteStatsReportRequests.orDefault(request);
		WidgetReportDefinition definition = siteStatsWidgetCatalog.getWidgetReportDefinition(siteId, widgetId, tabId, safeRequest);
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
			throw new IllegalArgumentException("Unknown SiteStats widget report: " + widgetId + "/" + tabId);
		}

		SiteStatsReportView view = siteStatsReportViewMapper.mapReportView(siteId, baseReport, safeRequest, prefsData);
		view.setWidgetId(widgetId);
		view.setTabId(tabId);
		view.setTitle(definition.getTitle());
		if (chartReport != null) {
			view.setChart(siteStatsReportViewMapper.mapChart(chartReport, prefsData));
		}
		if (tableReport != null) {
			view.setTable(siteStatsReportViewMapper.mapTable(tableReport, safeRequest));
		}
		return view;
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
			throw new SecurityException("Current Sakai session is required to access SiteStats report previews");
		}
		Session session = sessionManager.getCurrentSession();
		if (session == null || StringUtils.isBlank(session.getUserId())) {
			throw new SecurityException("Current Sakai user is required to access SiteStats report previews");
		}
		return session.getUserId();
	}

	private void assertCanView(String siteId) {
		if (!statsAuthz.isUserAbleToViewSiteStats(siteId)) {
			throw new SecurityException("Current user cannot view SiteStats for site " + siteId);
		}
	}

	private void assertCanViewAll(String siteId) {
		if (!statsAuthz.isUserAbleToViewSiteStatsAll(siteId)) {
			throw new SecurityException("Current user cannot view all SiteStats data for site " + siteId);
		}
	}
}
