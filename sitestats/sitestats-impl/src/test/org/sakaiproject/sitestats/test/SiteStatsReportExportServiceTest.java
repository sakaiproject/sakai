/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_ACTIVITY_EVENTS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_ACTIVITY;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsAuthz;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.event.SiteStatsToolEventsService;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.impl.view.SiteStatsReportExportServiceImpl;
import org.sakaiproject.sitestats.impl.view.SiteStatsReportPreviewServiceImpl;
import org.sakaiproject.sitestats.impl.view.SiteStatsWidgetCatalog;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

public class SiteStatsReportExportServiceTest {

	private static final String SITE_ID = "site-a";
	private static final String USER_ID = "user-a";
	private static final String OTHER_USER_ID = "user-b";

	private StatsAuthz statsAuthz;
	private ReportManager reportManager;
	private SiteStatsReportPreviewServiceImpl previewService;
	private SiteStatsReportExportServiceImpl service;

	@Before
	public void setUp() {
		statsAuthz = mock(StatsAuthz.class);
		StatsManager statsManager = mock(StatsManager.class);
		reportManager = mock(ReportManager.class);
		SiteStatsToolEventsService siteStatsToolEventsService = mock(SiteStatsToolEventsService.class);
		EventRegistryService eventRegistryService = mock(EventRegistryService.class);
		SiteService siteService = mock(SiteService.class);

		Session session = mock(Session.class);
		when(session.getUserId()).thenReturn(USER_ID);
		SessionManager sessionManager = mock(SessionManager.class);
		when(sessionManager.getCurrentSession()).thenReturn(session);

		when(statsAuthz.isUserAbleToViewSiteStats(SITE_ID)).thenReturn(true);
		when(statsAuthz.isUserAbleToViewSiteStatsAll(SITE_ID)).thenReturn(true);
		when(statsAuthz.isUserAbleToViewSiteStatsOwn(SITE_ID)).thenReturn(true);
		when(statsManager.getPreferences(eq(SITE_ID), anyBoolean())).thenReturn(new PrefsData());
		when(reportManager.getReport(any(ReportDef.class), anyBoolean(), any(), anyBoolean())).thenAnswer(invocation -> {
			Report report = new Report();
			report.setReportDefinition(invocation.getArgument(0));
			return report;
		});

		SiteStatsWidgetCatalog widgetCatalog = new SiteStatsWidgetCatalog();
		widgetCatalog.setStatsManager(statsManager);
		widgetCatalog.setSiteStatsToolEventsService(siteStatsToolEventsService);
		widgetCatalog.setEventRegistryService(eventRegistryService);
		widgetCatalog.setSiteService(siteService);

		previewService = new SiteStatsReportPreviewServiceImpl();

		service = new SiteStatsReportExportServiceImpl();
		service.setStatsAuthz(statsAuthz);
		service.setStatsManager(statsManager);
		service.setReportManager(reportManager);
		service.setSiteStatsReportPreviewService(previewService);
		service.setSessionManager(sessionManager);
		service.setSiteStatsWidgetCatalog(widgetCatalog);
	}

	@Test
	public void canExportPersistedReportRequiresAllStatsAndExistingReport() {
		when(reportManager.getReportDefinition(42)).thenReturn(storedReport("stored-site"));

		assertTrue(service.canExportPersistedReport(SITE_ID, 42));
		assertFalse(service.canExportPersistedReport(SITE_ID, 0));

		when(statsAuthz.isUserAbleToViewSiteStatsAll(SITE_ID)).thenReturn(false);
		assertFalse(service.canExportPersistedReport(SITE_ID, 42));
	}

	@Test
	public void getPersistedReportUsesUrlSiteAsAuthoritativeSite() {
		when(reportManager.getReportDefinition(42)).thenReturn(storedReport("stored-site"));

		service.getPersistedReport(SITE_ID, 42);

		ArgumentCaptor<ReportDef> captor = ArgumentCaptor.forClass(ReportDef.class);
		verify(reportManager).getReport(captor.capture(), anyBoolean(), any(), eq(true));
		assertEquals(SITE_ID, captor.getValue().getSiteId());
		assertEquals(SITE_ID, captor.getValue().getReportParams().getSiteId());
	}

	@Test
	public void previewExportsUseCurrentUserOwnership() {
		String ownedPreview = previewService.register(SITE_ID, USER_ID, storedReport(SITE_ID));
		String otherUserPreview = previewService.register(SITE_ID, OTHER_USER_ID, storedReport(SITE_ID));

		assertTrue(service.canExportPreviewReport(SITE_ID, ownedPreview));
		assertFalse(service.canExportPreviewReport(SITE_ID, otherUserPreview));
		assertThrows(IllegalArgumentException.class, () -> service.getPreviewReport(SITE_ID, otherUserPreview));
	}

	@Test
	public void canExportWidgetMetricReportRequiresAllStatsForAllUserMetrics() {
		assertTrue(service.canExportWidgetMetricReport(SITE_ID, WIDGET_ACTIVITY, METRIC_ACTIVITY_EVENTS));

		when(statsAuthz.isUserAbleToViewSiteStatsAll(SITE_ID)).thenReturn(false);
		assertFalse(service.canExportWidgetMetricReport(SITE_ID, WIDGET_ACTIVITY, METRIC_ACTIVITY_EVENTS));
	}

	private ReportDef storedReport(String siteId) {
		ReportDef reportDef = new ReportDef();
		reportDef.setId(42);
		reportDef.setSiteId(siteId);
		reportDef.setReportParams(new ReportParams(siteId));
		return reportDef;
	}
}
