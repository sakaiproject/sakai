/**
 * Copyright (c) 2026 The Apereo Foundation
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
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_ACTIVITY_EVENTS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_LESSONS_PAGES;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_ACTIVITY;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_LESSONS;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsAuthz;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.api.view.SiteStatsReportExportService;
import org.sakaiproject.sitestats.api.view.SiteStatsReportPreviewService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SiteStatsViewBehaviorTestConfiguration.class})
public class SiteStatsReportExportServiceTest {

	private static final String SITE_ID = "site-a";
	private static final String SITE_REF = "/site/" + SITE_ID;
	private static final String USER_ID = "user-a";
	private static final String OTHER_USER_ID = "user-b";

	@Autowired private SecurityService securityService;
	@Autowired private SiteService siteService;
	@Autowired private StatsManager statsManager;
	@Autowired private ReportManager reportManager;
	@Autowired private SiteStatsReportPreviewService previewService;
	@Autowired private SiteStatsReportExportService service;
	@Autowired private SessionManager sessionManager;

	@Before
	public void setUp() {
		Session session = mock(Session.class);
		when(session.getUserId()).thenReturn(USER_ID);
		reset(securityService, siteService, statsManager, reportManager, sessionManager);
		when(sessionManager.getCurrentSession()).thenReturn(session);

		when(siteService.siteReference(SITE_ID)).thenReturn(SITE_REF);
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_VIEW, SITE_REF)).thenReturn(true);
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_ALL, SITE_REF)).thenReturn(true);
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_OWN, SITE_REF)).thenReturn(true);
		PrefsData prefsData = new PrefsData();
		prefsData.setShowOwnStatisticsToStudents(true);
		when(statsManager.getPreferences(eq(SITE_ID), anyBoolean())).thenReturn(prefsData);
		when(reportManager.getReport(any(ReportDef.class), anyBoolean(), any(), anyBoolean())).thenAnswer(invocation -> {
			Report report = new Report();
			report.setReportDefinition(invocation.getArgument(0));
			return report;
		});

	}

	@Test
	public void canExportPersistedReportRequiresAllStatsAndExistingReport() {
		when(reportManager.getReportDefinition(42)).thenReturn(storedReport("stored-site"));

		assertTrue(service.canExportPersistedReport(SITE_ID, 42));
		assertFalse(service.canExportPersistedReport(SITE_ID, 0));

		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_ALL, SITE_REF)).thenReturn(false);
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

		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_ALL, SITE_REF)).thenReturn(false);
		assertFalse(service.canExportWidgetMetricReport(SITE_ID, WIDGET_ACTIVITY, METRIC_ACTIVITY_EVENTS));
	}

	@Test
	public void canExportWidgetMetricReportRejectsNonReportableLessonMetrics() {
		when(statsManager.isEnableLessonsStats()).thenReturn(true);

		assertFalse(service.canExportWidgetMetricReport(SITE_ID, WIDGET_LESSONS, METRIC_LESSONS_PAGES));
	}

	private ReportDef storedReport(String siteId) {
		ReportDef reportDef = new ReportDef();
		reportDef.setId(42);
		reportDef.setSiteId(siteId);
		reportDef.setReportParams(new ReportParams(siteId));
		return reportDef;
	}
}
