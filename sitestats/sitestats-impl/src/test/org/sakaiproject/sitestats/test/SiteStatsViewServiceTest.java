/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.sakaiproject.sitestats.test.SiteStatsTestFixtures.site;
import static org.sakaiproject.sitestats.test.SiteStatsTestFixtures.tool;
import static org.sakaiproject.sitestats.test.SiteStatsTestFixtures.visitReport;
import static org.sakaiproject.sitestats.test.SiteStatsTestFixtures.visitStat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.AUDIENCE_ALL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_LESSONS_PAGES;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_VISITS_TOTAL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_LESSONS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_VISITS;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsAuthz;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.api.view.SiteStatsFilter;
import org.sakaiproject.sitestats.api.view.SiteStatsOverview;
import org.sakaiproject.sitestats.api.view.SiteStatsReportPreviewService;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsReportView;
import org.sakaiproject.sitestats.api.view.SiteStatsViewService;
import org.sakaiproject.sitestats.api.view.SiteStatsWidget;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetMetric;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetTab;
import org.sakaiproject.sitestats.impl.view.SiteStatsTableMapperImpl;
import org.sakaiproject.sitestats.test.data.FakeData;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SiteStatsTestConfiguration.class})
public class SiteStatsViewServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

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
	@Autowired private SiteStatsTableMapperImpl tableMapper;
	@Autowired private SiteStatsViewService service;
	@Autowired private SessionManager sessionManager;

	@Before
	public void setUp() throws Exception {
		db.deleteAll();
		reset(securityService, siteService, sessionManager);

		Session session = mock(Session.class);
		when(session.getUserId()).thenReturn(USER_ID);
		when(sessionManager.getCurrentSession()).thenReturn(session);

		Site site = site(SITE_ID, "Site A", "Instructor");
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
		db.insertObject(visitStat(SITE_ID, Date.valueOf("2026-06-17"), 3, 2));
	}

	@Test
	public void getOverviewPopulatesFilterMetadataFromSpringWiredServices() throws Exception {
		SiteStatsOverview overview = service.getOverview(SITE_ID);

		SiteStatsFilter dateFilter = filter(overview, WIDGET_VISITS, TAB_BY_DATE, "date");
		assertEquals(ReportManager.WHEN_ALL, dateFilter.getOptions().get(0).getValue());

		SiteStatsFilter roleFilter = filter(overview, WIDGET_VISITS, TAB_BY_DATE, "role");
		assertEquals("Instructor", roleFilter.getOptions().get(1).getValue());
	}

	@Test
	public void getWidgetReportBuildsReportViewFromSpringWiredServices() {
		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_ALL);

		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_VISITS, TAB_BY_DATE, request);

		assertNotNull(view.getTable());
		assertNotNull(view.getChart());
		assertEquals(WIDGET_VISITS, view.getWidgetId());
		assertEquals(TAB_BY_DATE, view.getTabId());
	}

	@Test
	public void getReportMapsPersistedVisitDataFromSpringWiredServices() {
		ReportDef reportDef = visitReport(SITE_ID, USER_ID);
		assertTrue(reportManager.saveReportDefinition(reportDef));

		SiteStatsReportView view = service.getReport(SITE_ID, reportDef.getId(), new SiteStatsReportRequest());

		assertEquals(1, view.getTable().getTotalRows());
		assertEquals(1, view.getTable().getRows().size());
		assertEquals(Long.valueOf(3), view.getTable().getRows().get(0).getCells().get(StatsManager.T_VISITS).getRaw());
		assertEquals(Long.valueOf(2), view.getTable().getRows().get(0).getCells().get(StatsManager.T_UNIQUEVISITS).getRaw());
	}

	@Test
	public void getWidgetMetricsReturnsStableMetricMetadata() {
		List<SiteStatsWidgetMetric> metrics = service.getWidgetMetrics(SITE_ID, WIDGET_VISITS);

		assertEquals(METRIC_VISITS_TOTAL, metrics.get(0).getId());
		assertEquals(AUDIENCE_ALL, metrics.get(0).getAudience());
		assertNotNull(metrics.get(0).getWidgetTitle());
		assertFalse(metrics.get(0).getWidgetTitle().isEmpty());
		assertTrue(metrics.get(0).isReportable());
	}

	@Test
	public void lessonMetricsAreExplicitlyNonReportable() {
		List<SiteStatsWidgetMetric> metrics = service.getWidgetMetrics(SITE_ID, WIDGET_LESSONS);

		assertEquals(METRIC_LESSONS_PAGES, metrics.get(0).getId());
		assertFalse(metrics.get(0).isReportable());
	}

	@Test
	public void reportRequestCapsPageSizeAtApiBoundary() {
		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setIncludeChart(false);
		request.setPageSize(1000);

		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_VISITS, TAB_BY_DATE, request);

		assertEquals(SiteStatsReportRequest.MAX_PAGE_SIZE, request.getPageSize());
		assertEquals(SiteStatsReportRequest.MAX_PAGE_SIZE, view.getTable().getPageSize());
	}

	@Test
	public void tableMapperRejectsUnknownColumns() {
		assertThrows(IllegalArgumentException.class, () -> tableMapper.getColumn("missing-column", false));
	}

	@Test
	public void reportDefCopyEnforcesSiteAndDeepCopiesMutableValues() {
		ReportDef source = new ReportDef();
		source.setId(42);
		source.setSiteId("source-site");
		source.setTitle("Source");
		source.setCreatedOn(new java.util.Date(1000L));
		ReportParams params = new ReportParams("source-site");
		params.setWhatToolIds(new ArrayList<String>(Arrays.asList("sakai.assignment")));
		params.setWhenFrom(Date.valueOf("2026-06-17"));
		params.setHowTotalsBy(new ArrayList<String>(Arrays.asList(StatsManager.T_DATE, StatsManager.T_TOTAL)));
		source.setReportParams(params);

		ReportDef copy = new ReportDef(source, SITE_ID);
		params.getWhatToolIds().add("sakai.forums");
		params.getWhenFrom().setTime(Date.valueOf("2026-06-18").getTime());
		source.getCreatedOn().setTime(2000L);

		assertEquals(SITE_ID, copy.getSiteId());
		assertEquals(SITE_ID, copy.getReportParams().getSiteId());
		assertEquals(1, copy.getReportParams().getWhatToolIds().size());
		assertEquals("sakai.assignment", copy.getReportParams().getWhatToolIds().get(0));
		assertEquals(Date.valueOf("2026-06-17"), copy.getReportParams().getWhenFrom());
		assertEquals(1000L, copy.getCreatedOn().getTime());
	}

	@Test
	public void previewServiceStoresAndReturnsDefensiveCopies() {
		ReportDef preview = visitReport(SITE_ID, USER_ID);
		preview.setTitle("Original");
		preview.getReportParams().setWhatToolIds(new ArrayList<String>(Arrays.asList("sakai.assignment")));

		String previewId = previewService.register(SITE_ID, USER_ID, preview);
		preview.setTitle("Changed after register");
		preview.getReportParams().getWhatToolIds().add("sakai.forums");

		ReportDef firstRead = previewService.get(SITE_ID, USER_ID, previewId);
		assertEquals("Original", firstRead.getTitle());
		assertEquals(1, firstRead.getReportParams().getWhatToolIds().size());

		firstRead.setTitle("Changed after read");
		firstRead.getReportParams().getWhatToolIds().add("sakai.gradebookng");

		ReportDef secondRead = previewService.get(SITE_ID, USER_ID, previewId);
		assertEquals("Original", secondRead.getTitle());
		assertEquals(1, secondRead.getReportParams().getWhatToolIds().size());
		assertEquals("sakai.assignment", secondRead.getReportParams().getWhatToolIds().get(0));
	}

	@Test
	public void getWidgetReportRequiresAllPermissionForAllUserWidgets() {
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_ALL, SITE_REF)).thenReturn(false);

		assertThrows(SecurityException.class,
				() -> service.getWidgetReport(SITE_ID, WIDGET_VISITS, TAB_BY_DATE, new SiteStatsReportRequest()));
	}

	@Test
	public void getPreviewReportDoesNotLeakAcrossSites() {
		String previewId = previewService.register("other-site", USER_ID, visitReport("other-site", USER_ID));

		assertThrows(IllegalArgumentException.class,
				() -> service.getPreviewReport(SITE_ID, previewId, new SiteStatsReportRequest()));
	}

	@Test
	public void getPreviewReportDoesNotLeakAcrossUsers() {
		String previewId = previewService.register(SITE_ID, OTHER_USER_ID, visitReport(SITE_ID, OTHER_USER_ID));

		assertThrows(IllegalArgumentException.class,
				() -> service.getPreviewReport(SITE_ID, previewId, new SiteStatsReportRequest()));
	}

	private SiteStatsFilter filter(SiteStatsOverview overview, String widgetId, String tabId, String filterId) {
		for (SiteStatsWidget widget : overview.getWidgets()) {
			if (widgetId.equals(widget.getId())) {
				for (SiteStatsWidgetTab tab : widget.getTabs()) {
					if (tabId.equals(tab.getId())) {
						for (SiteStatsFilter filter : tab.getFilters()) {
							if (filterId.equals(filter.getId())) {
								return filter;
							}
						}
					}
				}
			}
		}
		throw new AssertionError("Missing filter " + widgetId + "/" + tabId + "/" + filterId);
	}

}
