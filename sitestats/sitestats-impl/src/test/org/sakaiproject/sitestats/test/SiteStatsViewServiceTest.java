/**
 * Copyright (c) 2026 The Sakai Foundation
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.PrefsData;
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
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsReportView;
import org.sakaiproject.sitestats.api.view.SiteStatsWidget;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetTab;
import org.sakaiproject.sitestats.impl.event.SiteStatsToolEventsServiceImpl;
import org.sakaiproject.sitestats.impl.view.SiteStatsReportPreviewServiceImpl;
import org.sakaiproject.sitestats.impl.view.SiteStatsTableMapperImpl;
import org.sakaiproject.sitestats.impl.SiteVisitsImpl;
import org.sakaiproject.sitestats.impl.view.SiteStatsViewServiceImpl;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;

public class SiteStatsViewServiceTest {

	private static final String SITE_ID = "site-a";
	private static final String USER_ID = "user-a";
	private static final String OTHER_USER_ID = "user-b";

	private StatsAuthz statsAuthz;
	private StatsManager statsManager;
	private ReportManager reportManager;
	private EventRegistryService eventRegistryService;
	private SiteService siteService;
	private SiteStatsReportPreviewServiceImpl previewService;
	private SiteStatsViewServiceImpl service;

	@Before
	public void setUp() {
		statsAuthz = mock(StatsAuthz.class);
		statsManager = mock(StatsManager.class);
		reportManager = mock(ReportManager.class);
		eventRegistryService = mock(EventRegistryService.class);
		siteService = mock(SiteService.class);
		UserDirectoryService userDirectoryService = mock(UserDirectoryService.class);
		UserTimeService userTimeService = mock(UserTimeService.class);
		Session session = mock(Session.class);
		when(session.getUserId()).thenReturn(USER_ID);
		SessionManager sessionManager = mock(SessionManager.class);
		when(sessionManager.getCurrentSession()).thenReturn(session);

		when(statsAuthz.isUserAbleToViewSiteStats(SITE_ID)).thenReturn(true);
		when(statsAuthz.isUserAbleToViewSiteStatsAll(SITE_ID)).thenReturn(true);
		when(statsManager.getPreferences(eq(SITE_ID), anyBoolean())).thenReturn(new PrefsData());
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

		SiteStatsTableMapperImpl tableMapper = new SiteStatsTableMapperImpl();
		tableMapper.setStatsManager(statsManager);
		tableMapper.setReportManager(reportManager);
		tableMapper.setEventRegistryService(eventRegistryService);
		tableMapper.setSiteService(siteService);
		tableMapper.setUserDirectoryService(userDirectoryService);
		tableMapper.setUserTimeService(userTimeService);

		SiteStatsToolEventsServiceImpl toolEventsService = new SiteStatsToolEventsServiceImpl();
		toolEventsService.setStatsManager(statsManager);
		toolEventsService.setEventRegistryService(eventRegistryService);

		previewService = new SiteStatsReportPreviewServiceImpl();

		service = new SiteStatsViewServiceImpl();
		service.setStatsAuthz(statsAuthz);
		service.setStatsManager(statsManager);
		service.setReportManager(reportManager);
		service.setSiteStatsToolEventsService(toolEventsService);
		service.setSiteStatsTableMapper(tableMapper);
		service.setSiteStatsReportPreviewService(previewService);
		service.setEventRegistryService(eventRegistryService);
		service.setSiteService(siteService);
		service.setSessionManager(sessionManager);
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
	public void getWidgetReportMapsTableAndChartData() {
		PrefsData prefsData = new PrefsData();
		prefsData.setChartIn3D(true);
		prefsData.setChartTransparency(0.4f);
		prefsData.setItemLabelsVisible(false);
		when(statsManager.getPreferences(eq(SITE_ID), eq(false))).thenReturn(prefsData);

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_LAST7DAYS);

		SiteStatsReportView view = service.getWidgetReport(SITE_ID, "visits", "bydate", request);

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
	public void getWidgetReportRequiresAllPermissionForAllUserWidgets() {
		when(statsAuthz.isUserAbleToViewSiteStatsAll(SITE_ID)).thenReturn(false);

		assertThrows(SecurityException.class, () -> service.getWidgetReport(SITE_ID, "visits", "bydate", new SiteStatsReportRequest()));
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

	private Report report(ReportDef reportDef) {
		SiteVisits stat = new SiteVisitsImpl();
		stat.setSiteId(SITE_ID);
		stat.setDate(Date.valueOf("2026-06-17"));
		stat.setTotalVisits(3);
		stat.setTotalUnique(2);
		stat.setCount(3);

		Report report = new Report();
		report.setReportDefinition(reportDef);
		report.setReportData(Arrays.asList(stat));
		report.setReportGenerationDate(new java.util.Date());
		return report;
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
