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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.AUDIENCE_ALL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_ACTIVITY_EVENTS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_ACTIVITY_MOST_ACTIVE_TOOL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_LESSONS_PAGES;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_VISITS_TOTAL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_ACTIVITY;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_LESSONS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_STUDENT_VISITS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_VISITS;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.ServerWideReportManager;
import org.sakaiproject.sitestats.api.ServerWideStatsRecord;
import org.sakaiproject.sitestats.api.SiteVisits;
import org.sakaiproject.sitestats.api.StatsAuthz;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportFormattedParams;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.api.view.SiteStatsFilter;
import org.sakaiproject.sitestats.api.view.SiteStatsOverview;
import org.sakaiproject.sitestats.api.view.SiteStatsReportInfoItem;
import org.sakaiproject.sitestats.api.view.SiteStatsReportPreviewService;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsReportView;
import org.sakaiproject.sitestats.api.view.SiteStatsViewService;
import org.sakaiproject.sitestats.api.view.SiteStatsWidget;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetMetric;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetTab;
import org.sakaiproject.sitestats.impl.view.SiteStatsTableMapperImpl;
import org.sakaiproject.sitestats.impl.ServerWideStatsRecordImpl;
import org.sakaiproject.sitestats.impl.SiteVisitsImpl;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SiteStatsViewBehaviorTestConfiguration.class})
public class SiteStatsViewServiceTest {

	private static final String SITE_ID = "site-a";
	private static final String SITE_REF = "/site/" + SITE_ID;
	private static final String USER_ID = "user-a";
	private static final String OTHER_USER_ID = "user-b";

	@Autowired private SecurityService securityService;
	@Autowired private StatsManager statsManager;
	@Autowired private ReportManager reportManager;
	@Autowired private ServerWideReportManager serverWideReportManager;
	@Autowired private EventRegistryService eventRegistryService;
	@Autowired private SiteService siteService;
	@Autowired private SiteStatsReportPreviewService previewService;
	@Autowired private SiteStatsTableMapperImpl tableMapper;
	@Autowired private SiteStatsViewService service;
	@Autowired private SessionManager sessionManager;
	@Autowired @Qualifier("org.sakaiproject.time.api.UserTimeService") private UserTimeService userTimeService;

	@Before
	public void setUp() {
		Session session = mock(Session.class);
		when(session.getUserId()).thenReturn(USER_ID);
		reset(securityService, statsManager, reportManager, serverWideReportManager, eventRegistryService, siteService, sessionManager, userTimeService);
		when(sessionManager.getCurrentSession()).thenReturn(session);

		when(siteService.siteReference(SITE_ID)).thenReturn(SITE_REF);
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_VIEW, SITE_REF)).thenReturn(true);
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_ALL, SITE_REF)).thenReturn(true);
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_OWN, SITE_REF)).thenReturn(true);
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_ADMIN_VIEW, SITE_REF)).thenReturn(true);
		PrefsData prefsData = new PrefsData();
		prefsData.setShowOwnStatisticsToStudents(true);
		when(statsManager.getPreferences(eq(SITE_ID), anyBoolean())).thenReturn(prefsData);
		when(statsManager.isEventContextSupported()).thenReturn(true);
		when(userTimeService.shortLocalizedDate(any(), any(Locale.class))).thenReturn("6/17/26");
		when(reportManager.isReportColumnAvailable(any(ReportParams.class), any(String.class))).thenAnswer(invocation -> {
			ReportParams params = invocation.getArgument(0);
			String column = invocation.getArgument(1);
			return params.getHowTotalsBy().contains(column);
		});
		when(reportManager.getReport(any(ReportDef.class), anyBoolean(), any(), anyBoolean())).thenAnswer(invocation -> report(invocation.getArgument(0)));
		ReportFormattedParams formattedParams = reportFormattedParams();
		when(reportManager.getReportFormattedParams()).thenReturn(formattedParams);

	}

	@Test
	public void getOverviewPopulatesFilterMetadata() throws Exception {
		Site site = mock(Site.class);
		Role role = mock(Role.class);
		when(role.getId()).thenReturn("Instructor");
		when(site.getRoles()).thenReturn(new LinkedHashSet<Role>(Arrays.asList(role)));
		when(siteService.getSite(SITE_ID)).thenReturn(site);
		when(statsManager.isEnableResourceStats()).thenReturn(true);
		when(statsManager.isEnableLessonsStats()).thenReturn(true);

		ToolInfo toolInfo = new ToolInfo("sakai.assignment");
		toolInfo.setSelected(true);
		PrefsData prefsData = new PrefsData();
		prefsData.setToolEventsDef(Arrays.asList(toolInfo));
		when(statsManager.getPreferences(eq(SITE_ID), eq(false))).thenReturn(prefsData);
		when(eventRegistryService.getToolName("sakai.assignment")).thenReturn("Assignments");

		SiteStatsOverview overview = service.getOverview(SITE_ID);

		SiteStatsFilter dateFilter = filter(overview, "visits", "bydate", "date");
		assertEquals(ReportManager.WHEN_ALL, dateFilter.getOptions().get(0).getValue());
		assertEquals(4, dateFilter.getOptions().size());

		SiteStatsFilter roleFilter = filter(overview, "visits", "bydate", "role");
		assertEquals("Instructor", roleFilter.getOptions().get(1).getValue());

		SiteStatsFilter toolFilter = filter(overview, "activity", "bytool", "tool");
		assertEquals("sakai.assignment", toolFilter.getOptions().get(1).getValue());
		assertEquals("Assignments", toolFilter.getOptions().get(1).getLabel());

		SiteStatsFilter resourceActionFilter = filter(overview, "resources", "byresource", "resourceAction");
		assertEquals(ReportManager.WHAT_RESOURCES_ACTION_DOW, resourceActionFilter.getOptions().get(5).getValue());

		SiteStatsFilter lessonActionFilter = filter(overview, "lessons", "bypage", "lessonAction");
		assertEquals(ReportManager.WHAT_LESSONS_ACTION_UPDATE, lessonActionFilter.getOptions().get(4).getValue());
	}

	@Test
	public void getWidgetTabReturnsFocusedMetadataWithoutFullOverviewLookup() {
		SiteStatsWidgetTab tab = service.getWidgetTab(SITE_ID, WIDGET_STUDENT_VISITS, TAB_BY_DATE);

		assertEquals(TAB_BY_DATE, tab.getId());
		assertNotNull(tab.getTitle());
		assertFalse(tab.getTitle().isEmpty());
		assertNotNull(tab.getWidgetTitle());
		assertFalse(tab.getWidgetTitle().isEmpty());
		assertEquals(1, tab.getFilters().size());
		assertEquals("date", tab.getFilters().get(0).getId());
		assertEquals(ReportManager.WHEN_ALL, tab.getFilters().get(0).getOptions().get(0).getValue());
	}

	@Test
	public void getWidgetReportMapsTableAndChartData() {
		PrefsData prefsData = new PrefsData();
		prefsData.setChartIn3D(true);
		prefsData.setChartTransparency(0.4f);
		prefsData.setItemLabelsVisible(false);
		when(statsManager.getPreferences(eq(SITE_ID), eq(false))).thenReturn(prefsData);

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_LAST7DAYS);

		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_VISITS, TAB_BY_DATE, request);

		assertNotNull(view.getTable());
		assertEquals(1, view.getTable().getTotalRows());
		assertEquals("6/17/26", view.getTable().getRows().get(0).getCells().get(StatsManager.T_DATE).getDisplay());
		assertEquals("3", view.getTable().getRows().get(0).getCells().get(StatsManager.T_VISITS).getDisplay());
		assertNotNull(view.getChart());
		assertTrue(view.getChart().isThreeDimensional());
		assertEquals(0.4f, view.getChart().getTransparency(), 0.000001f);
		assertFalse(view.getChart().isItemLabelsVisible());
		assertEquals(2, view.getChart().getDatasets().size());
		assertEquals(3L, view.getChart().getDatasets().get(0).getPoints().get(0).getY().longValue());
	}

	@Test
	public void getStudentVisitsByDateUsesCurrentUserOnly() {
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_ALL, SITE_REF)).thenReturn(false);
		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setIncludeChart(false);
		request.setDate(ReportManager.WHEN_LAST30DAYS);

		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_STUDENT_VISITS, TAB_BY_DATE, request);

		assertEquals(WIDGET_STUDENT_VISITS, view.getWidgetId());
		assertEquals(TAB_BY_DATE, view.getTabId());
		ArgumentCaptor<ReportDef> captor = ArgumentCaptor.forClass(ReportDef.class);
		verify(reportManager).getReport(captor.capture(), anyBoolean(), any(), eq(false));
		ReportParams params = captor.getValue().getReportParams();
		assertEquals(SITE_ID, params.getSiteId());
		assertEquals(ReportManager.WHAT_EVENTS, params.getWhat());
		assertEquals(Arrays.asList(StatsManager.SITEVISIT_EVENTID), params.getWhatEventIds());
		assertEquals(ReportManager.WHO_CUSTOM, params.getWho());
		assertEquals(Arrays.asList(USER_ID), params.getWhoUserIds());
		assertEquals(ReportManager.WHEN_LAST30DAYS, params.getWhen());
	}

	@Test
	public void getWidgetMetricReportUsesBackendMetricDefinition() {
		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setIncludeChart(false);

		SiteStatsReportView view = service.getWidgetMetricReport(SITE_ID, WIDGET_ACTIVITY, METRIC_ACTIVITY_MOST_ACTIVE_TOOL, request);

		assertEquals(WIDGET_ACTIVITY, view.getWidgetId());
		assertEquals(METRIC_ACTIVITY_MOST_ACTIVE_TOOL, view.getMetricId());
		ArgumentCaptor<ReportDef> captor = ArgumentCaptor.forClass(ReportDef.class);
		verify(reportManager).getReport(captor.capture(), anyBoolean(), any(), eq(false));
		ReportParams params = captor.getValue().getReportParams();
		assertEquals(ReportManager.WHAT_EVENTS, params.getWhat());
		assertEquals(ReportManager.WHO_ALL, params.getWho());
		assertEquals(Arrays.asList(StatsManager.T_TOOL), params.getHowTotalsBy());
		assertEquals(StatsManager.T_TOOL, params.getHowSortBy());
		assertEquals(ReportManager.HOW_PRESENTATION_BOTH, params.getHowPresentationMode());
	}

	@Test
	public void getWidgetMetricReportRequiresAllPermissionForAllUserMetrics() {
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_ALL, SITE_REF)).thenReturn(false);

		assertThrows(SecurityException.class,
				() -> service.getWidgetMetricReport(SITE_ID, WIDGET_ACTIVITY, METRIC_ACTIVITY_EVENTS, new SiteStatsReportRequest()));
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
	public void getWidgetMetricReturnsFocusedMetricMetadata() {
		SiteStatsWidgetMetric metric = service.getWidgetMetric(SITE_ID, WIDGET_VISITS, METRIC_VISITS_TOTAL);

		assertEquals(METRIC_VISITS_TOTAL, metric.getId());
		assertEquals(AUDIENCE_ALL, metric.getAudience());
		assertNotNull(metric.getWidgetTitle());
		assertFalse(metric.getWidgetTitle().isEmpty());
		assertTrue(metric.isReportable());
	}

	@Test
	public void lessonMetricsAreExplicitlyNonReportable() {
		when(statsManager.isEnableLessonsStats()).thenReturn(true);

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
	public void getWidgetReportAggregatesDuplicateChartPoints() {
		when(reportManager.getReport(any(ReportDef.class), anyBoolean(), any(), anyBoolean())).thenAnswer(invocation -> {
			ReportDef reportDef = invocation.getArgument(0);
			Report report = new Report();
			report.setReportDefinition(reportDef);
			report.setReportData(Arrays.asList(
					visitStat(Date.valueOf("2026-06-17"), 3, 2),
					visitStat(Date.valueOf("2026-06-17"), 4, 1)));
			report.setReportGenerationDate(new java.util.Date());
			return report;
		});

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_LAST7DAYS);

		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_VISITS, TAB_BY_DATE, request);

		assertEquals(1, view.getChart().getDatasets().get(0).getPoints().size());
		assertEquals(7L, view.getChart().getDatasets().get(0).getPoints().get(0).getY().longValue());
		assertEquals(3L, view.getChart().getDatasets().get(1).getPoints().get(0).getY().longValue());
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
		ReportDef preview = new ReportDef();
		preview.setSiteId(SITE_ID);
		preview.setTitle("Original");
		ReportParams params = new ReportParams(SITE_ID);
		params.setWhatToolIds(new ArrayList<String>(Arrays.asList("sakai.assignment")));
		preview.setReportParams(params);

		String previewId = previewService.register(SITE_ID, USER_ID, preview);
		preview.setTitle("Changed after register");
		params.getWhatToolIds().add("sakai.forums");

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

		assertThrows(SecurityException.class, () -> service.getWidgetReport(SITE_ID, WIDGET_VISITS, TAB_BY_DATE, new SiteStatsReportRequest()));
	}

	@Test
	public void getReportRequiresBaseViewPermissionBeforeAllStats() {
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_VIEW, SITE_REF)).thenReturn(false);
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_ALL, SITE_REF)).thenReturn(true);

		assertThrows(SecurityException.class, () -> service.getReport(SITE_ID, 42, new SiteStatsReportRequest()));
		verify(reportManager, never()).getReportDefinition(42);
	}

	@Test
	public void getReportRejectsTransientReportId() {
		assertThrows(IllegalArgumentException.class, () -> service.getReport(SITE_ID, 0, new SiteStatsReportRequest()));

		verify(reportManager, never()).getReportDefinition(0);
	}

	@Test
	public void getPreviewReportUsesUrlSiteIdAsAuthoritativeSite() {
		ReportDef preview = new ReportDef();
		preview.setId(0);
		preview.setSiteId("other-site");
		ReportParams params = new ReportParams("other-site");
		params.setWhat(ReportManager.WHAT_VISITS_TOTALS);
		params.setHowTotalsBy(Arrays.asList(StatsManager.T_DATE, StatsManager.T_VISITS));
		preview.setReportParams(params);
		String previewId = previewService.register(SITE_ID, USER_ID, preview);

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setIncludeChart(false);
		SiteStatsReportView view = service.getPreviewReport(SITE_ID, previewId, request);

		assertNotNull(view.getTable());
		ArgumentCaptor<ReportDef> captor = ArgumentCaptor.forClass(ReportDef.class);
		verify(reportManager).getReport(captor.capture(), anyBoolean(), any(), eq(true));
		assertEquals(SITE_ID, captor.getValue().getSiteId());
		assertEquals(SITE_ID, captor.getValue().getReportParams().getSiteId());
	}

	@Test
	public void getPreviewReportDoesNotLeakAcrossSites() {
		ReportDef preview = new ReportDef();
		preview.setReportParams(new ReportParams("other-site"));
		String previewId = previewService.register("other-site", USER_ID, preview);

		assertThrows(IllegalArgumentException.class, () -> service.getPreviewReport(SITE_ID, previewId, new SiteStatsReportRequest()));
	}

	@Test
	public void getPreviewReportDoesNotLeakAcrossUsers() {
		ReportDef preview = new ReportDef();
		preview.setReportParams(new ReportParams(SITE_ID));
		String previewId = previewService.register(SITE_ID, OTHER_USER_ID, preview);

		assertThrows(IllegalArgumentException.class, () -> service.getPreviewReport(SITE_ID, previewId, new SiteStatsReportRequest()));
	}

	@Test
	public void getPreviewReportIncludesFormattedSummaryRows() {
		ReportDef preview = new ReportDef();
		preview.setReportParams(new ReportParams(SITE_ID));
		String previewId = previewService.register(SITE_ID, USER_ID, preview);

		SiteStatsReportView view = service.getPreviewReport(SITE_ID, previewId, new SiteStatsReportRequest());

		SiteStatsReportInfoItem site = summaryItem(view, "site");
		assertNotNull(site.getLabel());
		assertEquals("Site A", site.getValue());
		assertEquals("Generated", summaryItem(view, "generated-on").getValue());
	}

	@Test
	public void getReportUsesUrlSiteIdAsAuthoritativeSite() {
		ReportDef stored = new ReportDef();
		stored.setId(42);
		stored.setSiteId("other-site");
		ReportParams params = new ReportParams("other-site");
		params.setWhat(ReportManager.WHAT_VISITS_TOTALS);
		params.setHowTotalsBy(Arrays.asList(StatsManager.T_DATE, StatsManager.T_VISITS));
		stored.setReportParams(params);
		when(reportManager.getReportDefinition(42)).thenReturn(stored);

		service.getReport(SITE_ID, 42, new SiteStatsReportRequest());

		ArgumentCaptor<ReportDef> captor = ArgumentCaptor.forClass(ReportDef.class);
		verify(reportManager).getReport(captor.capture(), anyBoolean(), any(), eq(true));
		assertEquals(SITE_ID, captor.getValue().getSiteId());
		assertEquals(SITE_ID, captor.getValue().getReportParams().getSiteId());
	}

	@Test
	public void getReportIncludesFormattedSummaryRows() {
		ReportDef stored = new ReportDef();
		stored.setId(42);
		stored.setSiteId(SITE_ID);
		stored.setReportParams(new ReportParams(SITE_ID));
		when(reportManager.getReportDefinition(42)).thenReturn(stored);

		SiteStatsReportView view = service.getReport(SITE_ID, 42, new SiteStatsReportRequest());

		assertEquals("Description", summaryItem(view, "description").getValue());
		assertEquals("Activity type", summaryItem(view, "activity-based-on").getValue());
		assertEquals("User type", summaryItem(view, "user-selection-type").getValue());
	}

	@Test
	public void getReportReturnsExplicitUnsupportedChartState() {
		ReportDef stored = new ReportDef();
		stored.setId(42);
		stored.setSiteId(SITE_ID);
		ReportParams params = new ReportParams(SITE_ID);
		params.setWhat(ReportManager.WHAT_VISITS_TOTALS);
		params.setHowTotalsBy(Arrays.asList(StatsManager.T_DATE, StatsManager.T_VISITS));
		params.setHowChartSource(StatsManager.T_DATE);
		params.setHowChartType("radar");
		stored.setReportParams(params);
		when(reportManager.getReportDefinition(42)).thenReturn(stored);

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setIncludeTable(false);
		SiteStatsReportView view = service.getReport(SITE_ID, 42, request);

		assertTrue(view.getChart().getDatasets().isEmpty());
		assertEquals("This chart configuration is not yet supported by the SiteStats JSON renderer.", view.getChart().getEmptyMessage());
	}

	@Test
	public void getServerWideReportRequiresAdminPermissionAndSetsSiteId() {
		Date laterPeriod = Date.valueOf("2026-06-01");
		Date earlierPeriod = Date.valueOf("2026-05-01");
		when(serverWideReportManager.getMonthlyTotalLogins()).thenReturn(Arrays.asList(
				serverWideRecord(laterPeriod, 10L),
				serverWideRecord(earlierPeriod, 5L)));
		when(serverWideReportManager.getMonthlyUniqueLogins()).thenReturn(Arrays.asList(
				serverWideRecord(laterPeriod, 4L),
				serverWideRecord(earlierPeriod, 2L)));
		when(serverWideReportManager.getSiteCreatedDeletedStats("monthly")).thenReturn(Arrays.asList(
				serverWideRecord(laterPeriod, 2L, 1L),
				serverWideRecord(earlierPeriod, 1L, 0L)));
		when(serverWideReportManager.getNewUserStats("monthly")).thenReturn(Arrays.asList(serverWideRecord(laterPeriod, 3L)));

		SiteStatsReportView view = service.getServerWideReport(SITE_ID, StatsManager.MONTHLY_LOGIN_REPORT);

		assertEquals(SITE_ID, view.getSiteId());
		assertNotNull(view.getChart());
		assertNotNull(view.getTable());
		assertEquals("line", view.getChart().getType());
		assertEquals(5, view.getChart().getDatasets().size());
		assertEquals(2, view.getTable().getTotalRows());
		assertEquals(earlierPeriod, view.getTable().getRows().get(0).getCells().get("label").getRaw());
		assertEquals(5L, view.getTable().getRows().get(0).getCells().get("logins").getRaw());
		assertEquals(2L, view.getTable().getRows().get(0).getCells().get("uniqueLogins").getRaw());
		assertEquals(0L, view.getTable().getRows().get(0).getCells().get("newUsers").getRaw());
		assertEquals(2, view.getChart().getDatasets().get(4).getPoints().size());
		assertEquals(0L, view.getChart().getDatasets().get(4).getPoints().get(0).getY().longValue());
		assertEquals(3L, view.getChart().getDatasets().get(4).getPoints().get(1).getY().longValue());
		verify(serverWideReportManager).getMonthlyTotalLogins();
		verify(serverWideReportManager).getMonthlyUniqueLogins();
		verify(serverWideReportManager).getSiteCreatedDeletedStats("monthly");
		verify(serverWideReportManager).getNewUserStats("monthly");

		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_ADMIN_VIEW, SITE_REF)).thenReturn(false);
		assertThrows(SecurityException.class, () -> service.getServerWideReport(SITE_ID, StatsManager.MONTHLY_LOGIN_REPORT));
	}

	@Test
	public void getServerWideHourlyReportMapsAverageUsageExplicitly() {
		Date dayOne = Date.valueOf("2026-06-01");
		Date dayTwo = Date.valueOf("2026-06-02");
		when(serverWideReportManager.getHourlyUsagePattern()).thenReturn(Arrays.asList(
				serverWideRecord(dayOne, 9, 4L),
				serverWideRecord(dayTwo, 10, 8L)));

		SiteStatsReportView view = service.getServerWideReport(SITE_ID, StatsManager.HOURLY_USAGE_REPORT);

		assertEquals("bar", view.getChart().getType());
		assertEquals(24, view.getTable().getTotalRows());
		assertEquals(2L, view.getTable().getRows().get(9).getCells().get("averageUsers").getRaw());
		assertEquals(4L, view.getTable().getRows().get(10).getCells().get("averageUsers").getRaw());
		assertEquals(2L, view.getChart().getDatasets().get(0).getPoints().get(9).getY().longValue());
		assertEquals(4L, view.getChart().getDatasets().get(0).getPoints().get(10).getY().longValue());
		verify(serverWideReportManager).getHourlyUsagePattern();
	}

	@Test
	public void getServerWideRegularUsersReportSortsWeeksChronologically() {
		Date laterWeek = Date.valueOf("2026-06-08");
		Date earlierWeek = Date.valueOf("2026-06-01");
		when(serverWideReportManager.getWeeklyRegularUsers()).thenReturn(Arrays.asList(
				serverWideRecord(laterWeek, 5L, 4L, 3L, 2L, 1L),
				serverWideRecord(earlierWeek, 10L, 9L, 8L, 7L, 6L)));

		SiteStatsReportView view = service.getServerWideReport(SITE_ID, StatsManager.REGULAR_USERS_REPORT);

		assertEquals("line", view.getChart().getType());
		assertEquals(2, view.getTable().getTotalRows());
		assertEquals(earlierWeek, view.getTable().getRows().get(0).getCells().get("label").getRaw());
		assertEquals(laterWeek, view.getTable().getRows().get(1).getCells().get("label").getRaw());
		assertEquals(10L, view.getChart().getDatasets().get(0).getPoints().get(0).getY().longValue());
		assertEquals(5L, view.getChart().getDatasets().get(0).getPoints().get(1).getY().longValue());
		verify(serverWideReportManager).getWeeklyRegularUsers();
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

	private ServerWideStatsRecord serverWideRecord(Object... values) {
		ServerWideStatsRecordImpl record = new ServerWideStatsRecordImpl();
		for (Object value : values) {
			record.add(value);
		}
		return record;
	}

	private Report report(ReportDef reportDef) {
		Report report = new Report();
		report.setReportDefinition(reportDef);
		report.setReportData(Arrays.asList(visitStat(Date.valueOf("2026-06-17"), 3, 2)));
		report.setReportGenerationDate(new java.util.Date());
		return report;
	}

	private SiteVisits visitStat(Date date, long totalVisits, long totalUnique) {
		SiteVisits stat = new SiteVisitsImpl();
		stat.setSiteId(SITE_ID);
		stat.setDate(date);
		stat.setTotalVisits(totalVisits);
		stat.setTotalUnique(totalUnique);
		stat.setCount(totalVisits);
		return stat;
	}

	private SiteStatsReportInfoItem summaryItem(SiteStatsReportView view, String id) {
		for (SiteStatsReportInfoItem item : view.getSummary()) {
			if (id.equals(item.getId())) {
				return item;
			}
		}
		throw new AssertionError("Missing summary item: " + id);
	}

	private ReportFormattedParams reportFormattedParams() {
		ReportFormattedParams formatter = mock(ReportFormattedParams.class);
		when(formatter.getReportDescription(any(Report.class))).thenReturn("Description");
		when(formatter.getReportSite(any(Report.class))).thenReturn("Site A");
		when(formatter.getReportActivityBasedOn(any(Report.class))).thenReturn("Activity type");
		when(formatter.getReportResourceActionTitle(any(Report.class))).thenReturn(null);
		when(formatter.getReportResourceAction(any(Report.class))).thenReturn(null);
		when(formatter.getReportActivitySelectionTitle(any(Report.class))).thenReturn(null);
		when(formatter.getReportActivitySelection(any(Report.class))).thenReturn(null);
		when(formatter.getReportTimePeriod(any(Report.class))).thenReturn("Time period");
		when(formatter.getReportUserSelectionType(any(Report.class))).thenReturn("User type");
		when(formatter.getReportUserSelectionTitle(any(Report.class))).thenReturn(null);
		when(formatter.getReportUserSelection(any(Report.class))).thenReturn(null);
		when(formatter.getReportGenerationDate(any(Report.class))).thenReturn("Generated");
		return formatter;
	}
}
