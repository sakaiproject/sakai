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
import static org.sakaiproject.sitestats.test.SiteStatsTestFixtures.site;
import static org.sakaiproject.sitestats.test.SiteStatsTestFixtures.tool;
import static org.sakaiproject.sitestats.test.SiteStatsTestFixtures.visitReport;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_ACTIVITY_EVENTS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_LESSONS_PAGES;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_ACTIVITY;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_LESSONS;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsAuthz;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.view.SiteStatsReportExportService;
import org.sakaiproject.sitestats.api.view.SiteStatsReportPreviewService;
import org.sakaiproject.sitestats.test.data.FakeData;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SiteStatsTestConfiguration.class})
public class SiteStatsReportExportServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

	private static final String SITE_ID = FakeData.SITE_A_ID;
	private static final String SITE_REF = FakeData.SITE_A_REF;
	private static final String USER_ID = FakeData.USER_A_ID;
	private static final String OTHER_USER_ID = FakeData.USER_B_ID;

	@Autowired private DB db;
	@Autowired private SecurityService securityService;
	@Autowired private SiteService siteService;
	@Autowired private StatsManager statsManager;
	@Autowired private ReportManager reportManager;
	@Autowired private SiteStatsReportPreviewService previewService;
	@Autowired private SiteStatsReportExportService service;
	@Autowired private SessionManager sessionManager;

	@Before
	public void setUp() throws Exception {
		db.deleteAll();
		reset(securityService, siteService, sessionManager);

		Session session = mock(Session.class);
		when(session.getUserId()).thenReturn(USER_ID);
		when(sessionManager.getCurrentSession()).thenReturn(session);

		Site site = site(SITE_ID, "Site A");
		when(siteService.siteReference(SITE_ID)).thenReturn(SITE_REF);
		when(siteService.getSite(SITE_ID)).thenReturn(site);
		when(siteService.isUserSite(SITE_ID)).thenReturn(false);
		when(siteService.isSpecialSite(SITE_ID)).thenReturn(false);

		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_VIEW, SITE_REF)).thenReturn(true);
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_ALL, SITE_REF)).thenReturn(true);
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_OWN, SITE_REF)).thenReturn(true);

		PrefsData prefsData = new PrefsData();
		prefsData.setShowOwnStatisticsToStudents(true);
		prefsData.setToolEventsDef(Arrays.asList(tool(FakeData.TOOL_CHAT, FakeData.EVENT_CHATNEW)));
		assertTrue(statsManager.setPreferences(SITE_ID, prefsData));
	}

	@Test
	public void canExportPersistedReportRequiresAllStatsAndExistingReport() {
		ReportDef storedReport = visitReport(SITE_ID, USER_ID);
		assertTrue(reportManager.saveReportDefinition(storedReport));

		assertTrue(service.canExportPersistedReport(SITE_ID, storedReport.getId()));
		assertFalse(service.canExportPersistedReport(SITE_ID, 0));

		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_ALL, SITE_REF)).thenReturn(false);
		assertFalse(service.canExportPersistedReport(SITE_ID, storedReport.getId()));
	}

	@Test
	public void getPersistedReportUsesUrlSiteAsAuthoritativeSite() {
		ReportDef storedReport = visitReport("stored-site", USER_ID);
		assertTrue(reportManager.saveReportDefinition(storedReport));

		Report report = service.getPersistedReport(SITE_ID, storedReport.getId());

		assertEquals(SITE_ID, report.getReportDefinition().getSiteId());
		assertEquals(SITE_ID, report.getReportDefinition().getReportParams().getSiteId());
	}

	@Test
	public void previewExportsUseCurrentUserOwnership() {
		String ownedPreview = previewService.register(SITE_ID, USER_ID, visitReport(SITE_ID, USER_ID));
		String otherUserPreview = previewService.register(SITE_ID, OTHER_USER_ID, visitReport(SITE_ID, OTHER_USER_ID));

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
		assertFalse(service.canExportWidgetMetricReport(SITE_ID, WIDGET_LESSONS, METRIC_LESSONS_PAGES));
	}

}
