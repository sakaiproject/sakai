/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.AUDIENCE_ALL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_ACTIVITY_MOST_ACTIVE_TOOL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_LESSONS_PAGES;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_PRESENCE_AVERAGE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_PRESENCE_BOUNCE_RATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_PRESENCE_LAST_VISIT;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_PRESENCE_TOTAL_7D;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_STUDENT_PRESENCE_LAST_VISIT;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_STUDENT_PRESENCE_TOTAL_7D;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_STUDENT_VISITS_AVERAGE_PRESENCE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_STUDENT_VISITS_PRESENCE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_STUDENT_VISITS_TOTAL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_STUDENT_VISITS_TRAFFIC_TREND;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_VISITS_AVERAGE_PRESENCE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_VISITS_ENROLLED_USERS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_VISITS_TOTAL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_VISITS_TRAFFIC_TREND;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_VISITS_UNIQUE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_VISITS_USERS_WITHOUT_VISITS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_VISITS_USERS_WITH_VISITS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_ROLE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_USER;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_ACTIVITY;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_LESSONS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_PRESENCE_ACCESS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_STUDENT_PRESENCE_ACCESS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_GRADES;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_STUDENT_GRADES;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_STUDENT_SUBMISSIONS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_STUDENT_VISITS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_SUBMISSIONS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_VISITS;
import static org.sakaiproject.sitestats.test.SiteStatsTestFixtures.eventStat;
import static org.sakaiproject.sitestats.test.SiteStatsTestFixtures.presenceStat;
import static org.sakaiproject.sitestats.test.SiteStatsTestFixtures.presenceTotal;
import static org.sakaiproject.sitestats.test.SiteStatsTestFixtures.site;
import static org.sakaiproject.sitestats.test.SiteStatsTestFixtures.tool;
import static org.sakaiproject.sitestats.test.SiteStatsTestFixtures.visitReport;
import static org.sakaiproject.sitestats.test.SiteStatsTestFixtures.visitStat;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsAuthz;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.api.view.SiteStatsChartPoint;
import org.sakaiproject.sitestats.api.view.SiteStatsFilter;
import org.sakaiproject.sitestats.api.view.SiteStatsOverview;
import org.sakaiproject.sitestats.api.view.SiteStatsReportPreviewService;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsReportView;
import org.sakaiproject.sitestats.api.view.SiteStatsViewService;
import org.sakaiproject.sitestats.api.view.SiteStatsWidget;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetMetric;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetMetricSnapshot;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetTab;
import org.sakaiproject.sitestats.impl.view.SiteStatsTableMapperImpl;
import org.sakaiproject.sitestats.impl.view.ViewFactoryFixtureWidgetDefinition;
import org.sakaiproject.sitestats.test.data.FakeData;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
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
	@Autowired private UserDirectoryService userDirectoryService;

	@Before
	public void setUp() throws Exception {
		db.deleteAll();
		reset(securityService, siteService, sessionManager, userDirectoryService);

		Session session = mock(Session.class);
		when(session.getUserId()).thenReturn(USER_ID);
		when(sessionManager.getCurrentSession()).thenReturn(session);
		User user = mock(User.class);
		when(user.getDisplayId()).thenReturn(USER_ID);
		when(user.getDisplayName()).thenReturn("User A");
		when(user.getSortName()).thenReturn("User A");
		when(userDirectoryService.getUser(USER_ID)).thenReturn(user);
		User otherUser = mock(User.class);
		when(otherUser.getDisplayId()).thenReturn(OTHER_USER_ID);
		when(otherUser.getDisplayName()).thenReturn("User B");
		when(otherUser.getSortName()).thenReturn("User B");
		when(userDirectoryService.getUser(OTHER_USER_ID)).thenReturn(otherUser);

		Site site = site(SITE_ID, "Site A", "Instructor");
		when(site.getUsers()).thenReturn(new HashSet<String>(Arrays.asList(USER_ID, OTHER_USER_ID)));
		when(site.getMembers()).thenReturn(new HashSet<Member>(Arrays.asList(mock(Member.class), mock(Member.class))));
		when(site.getUsersIsAllowed(SiteService.SECURE_UPDATE_SITE))
				.thenReturn(new HashSet<String>(Arrays.asList(USER_ID)));
		when(site.getUsersHasRole(anyString())).thenAnswer(invocation -> {
			if ("Instructor".equals(invocation.getArgument(0))) {
				return new HashSet<String>(Arrays.asList(USER_ID));
			}
			return new HashSet<String>();
		});
		when(siteService.siteReference(SITE_ID)).thenReturn(SITE_REF);
		when(siteService.getSite(SITE_ID)).thenReturn(site);
		when(siteService.isUserSite(SITE_ID)).thenReturn(false);
		when(siteService.isSpecialSite(SITE_ID)).thenReturn(false);

		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_VIEW, SITE_REF)).thenReturn(true);
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_ALL, SITE_REF)).thenReturn(true);
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_OWN, SITE_REF)).thenReturn(true);

		PrefsData prefsData = new PrefsData();
		prefsData.setShowOwnStatisticsToStudents(true);
		prefsData.setListToolEventsOnlyAvailableInSite(false);
		prefsData.setToolEventsDef(Arrays.asList(tool(FakeData.TOOL_CHAT, FakeData.EVENT_CHATNEW)));
		assertTrue(statsManager.setPreferences(SITE_ID, prefsData));
		db.insertObject(visitStat(SITE_ID, Date.valueOf("2026-06-17"), 3, 2));
	}

	@Test
	public void getOverviewPopulatesFilterMetadataFromSpringWiredServices() throws Exception {
		SiteStatsOverview overview = service.getOverview(SITE_ID);

		SiteStatsFilter dateFilter = filter(overview, WIDGET_VISITS, TAB_BY_DATE, "date");
		assertEquals(ReportManager.WHEN_ALL, dateFilter.getOptions().get(0).getValue());
		assertEquals(ReportManager.WHEN_CUSTOM, dateFilter.getOptions().get(dateFilter.getOptions().size() - 1).getValue());

		SiteStatsFilter roleFilter = filter(overview, WIDGET_VISITS, TAB_BY_DATE, "role");
		assertEquals("Instructor", roleFilter.getOptions().get(1).getValue());
	}

	@Test
	public void getOverviewPrefersAllWidgetsWhenAllAndOwnPermissionsAreGranted() {
		SiteStatsOverview overview = service.getOverview(SITE_ID);

		assertNull(metric(overview, WIDGET_VISITS, METRIC_VISITS_TOTAL).getSnapshot());
		assertEquals("3", snapshot(WIDGET_VISITS, METRIC_VISITS_TOTAL).getPrimary());
		assertTrue(hasWidget(overview, WIDGET_PRESENCE_ACCESS));
		assertTrue(hasWidget(overview, WIDGET_SUBMISSIONS));
		assertTrue(hasWidget(overview, WIDGET_GRADES));
		assertFalse(hasWidget(overview, WIDGET_STUDENT_VISITS));
		assertFalse(hasWidget(overview, WIDGET_STUDENT_PRESENCE_ACCESS));
		assertFalse(hasWidget(overview, WIDGET_STUDENT_SUBMISSIONS));
		assertFalse(hasWidget(overview, WIDGET_STUDENT_GRADES));
	}

	@Test
	public void getOverviewIncludesOwnWidgetsWhenAllPermissionIsNotGranted() {
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_ALL, SITE_REF)).thenReturn(false);

		SiteStatsOverview overview = service.getOverview(SITE_ID);

		assertFalse(hasWidget(overview, WIDGET_VISITS));
		assertFalse(hasWidget(overview, WIDGET_PRESENCE_ACCESS));
		assertFalse(hasWidget(overview, WIDGET_SUBMISSIONS));
		assertFalse(hasWidget(overview, WIDGET_GRADES));
		assertTrue(hasWidget(overview, WIDGET_STUDENT_SUBMISSIONS));
		assertTrue(hasWidget(overview, WIDGET_STUDENT_GRADES));
		assertNotNull(metric(overview, WIDGET_STUDENT_VISITS, METRIC_STUDENT_VISITS_TOTAL));
		assertNull(metric(overview, WIDGET_STUDENT_VISITS, METRIC_STUDENT_VISITS_TOTAL).getSnapshot());
		assertNotNull(snapshot(WIDGET_STUDENT_VISITS, METRIC_STUDENT_VISITS_TOTAL));
		assertNotNull(snapshot(WIDGET_STUDENT_VISITS, METRIC_STUDENT_PRESENCE_LAST_VISIT));
	}

	@Test
	public void getOverviewDoesNotDuplicatePrimaryMetricValuesAsDetails() {
		db.insertObject(eventStat(SITE_ID, USER_ID, FakeData.TOOL_CHAT, FakeData.EVENT_CHATNEW,
				Date.valueOf("2026-06-17"), 4));

		SiteStatsWidgetMetric metric = metricValue(WIDGET_ACTIVITY, METRIC_ACTIVITY_MOST_ACTIVE_TOOL);

		assertNotNull(metric.getSnapshot().getPercentage());
		assertNull(metric.getSnapshot().getDetail());
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
		assertEquals("overview_title_visits", view.getTitle());
		assertEquals(view.getTitle(), view.getChart().getTitle());
		assertEquals(view.getTitle(), view.getTable().getCaption());
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
	public void pieChartLabelsDescribeTheMeasuredValue() {
		db.insertObject(eventStat(SITE_ID, USER_ID, FakeData.TOOL_CHAT,
				FakeData.EVENT_CHATNEW, Date.valueOf("2026-06-17"), 4));
		SiteStatsReportView view = service.getWidgetMetricReport(SITE_ID, WIDGET_ACTIVITY,
				METRIC_ACTIVITY_MOST_ACTIVE_TOOL, new SiteStatsReportRequest());

		assertFalse(view.getChart().getDatasets().isEmpty());
		assertEquals("Total", view.getChart().getDatasets().get(0).getLabel());
	}

	@Test
	public void visitPieChartLabelsDescribeVisitCounts() {
		ReportDef reportDef = visitReport(SITE_ID, USER_ID);
		ReportParams params = reportDef.getReportParams();
		params.setHowPresentationMode(ReportManager.HOW_PRESENTATION_CHART);
		params.setHowChartType(StatsManager.CHARTTYPE_PIE);
		params.setHowChartSource(StatsManager.T_DATE);
		assertTrue(reportManager.saveReportDefinition(reportDef));

		SiteStatsReportView view = service.getReport(SITE_ID, reportDef.getId(), new SiteStatsReportRequest());

		assertEquals("Visits", view.getChart().getDatasets().get(0).getLabel());
	}

	@Test
	public void activityByUserPieChartUsesTheLocalizedWidgetTitleForCounts() {
		db.insertObject(eventStat(SITE_ID, USER_ID, FakeData.TOOL_CHAT,
				FakeData.EVENT_CHATNEW, Date.valueOf("2026-06-17"), 4));
		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_ALL);
		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_ACTIVITY, TAB_BY_USER, request);

		assertFalse(view.getChart().getDatasets().isEmpty());
		assertEquals("overview_title_activity", view.getTitle());
		assertEquals(view.getTitle(), view.getChart().getDatasets().get(0).getLabel());
	}

	@Test
	public void visitsByUserPieChartUsesTheLocalizedWidgetTitleForCounts() {
		db.insertObject(eventStat(SITE_ID, USER_ID, FakeData.TOOL_CHAT,
				StatsManager.SITEVISIT_EVENTID, Date.valueOf("2026-06-17"), 3));
		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_ALL);

		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_VISITS, TAB_BY_USER, request);

		assertFalse(view.getChart().getDatasets().isEmpty());
		assertEquals("overview_title_visits", view.getTitle());
		assertEquals(view.getTitle(), view.getChart().getDatasets().get(0).getLabel());
	}

	@Test
	public void presenceAccessWidgetReportsEngagementMetrics() {
		Date lastVisit = Date.valueOf("2026-06-17");
		db.insertObject(presenceTotal(SITE_ID, USER_ID, lastVisit, 3));
		db.insertObject(presenceStat(SITE_ID, USER_ID, new java.util.Date(), 120000L));
		db.insertObject(eventStat(SITE_ID, USER_ID, FakeData.TOOL_CHAT, StatsManager.SITEVISIT_EVENTID,
				lastVisit, 3));

		SiteStatsOverview overview = service.getOverview(SITE_ID);

		assertNull(metric(overview, WIDGET_PRESENCE_ACCESS, METRIC_PRESENCE_TOTAL_7D).getSnapshot());
		assertEquals("2 minutes_abbr",
				snapshot(WIDGET_PRESENCE_ACCESS, METRIC_PRESENCE_TOTAL_7D).getPrimary());
		assertEquals("2 minutes_abbr",
				snapshot(WIDGET_PRESENCE_ACCESS, METRIC_PRESENCE_AVERAGE).getPrimary());
		assertEquals("1 / 1", snapshot(WIDGET_PRESENCE_ACCESS, METRIC_PRESENCE_BOUNCE_RATE).getPrimary());
		assertEquals(Integer.valueOf(100),
				snapshot(WIDGET_PRESENCE_ACCESS, METRIC_PRESENCE_BOUNCE_RATE).getPercentage());

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_ALL);
		SiteStatsReportView byDate = service.getWidgetReport(SITE_ID, WIDGET_PRESENCE_ACCESS, TAB_BY_DATE, request);
		assertEquals(WIDGET_PRESENCE_ACCESS, byDate.getWidgetId());
		assertNotNull(byDate.getTable());
		assertNotNull(byDate.getChart());

		SiteStatsReportView byUser = service.getWidgetReport(SITE_ID, WIDGET_PRESENCE_ACCESS, TAB_BY_USER, request);
		assertNotNull(byUser.getTable());
		assertTrue(byUser.getTable().getTotalRows() >= 1);
	}

	@Test
	public void presenceMedianIgnoresOutlierDurations() {
		db.insertObject(presenceStat(SITE_ID, USER_ID, Date.valueOf("2026-06-15"), 60000L));
		db.insertObject(presenceStat(SITE_ID, USER_ID, Date.valueOf("2026-06-16"), 60000L));
		db.insertObject(presenceStat(SITE_ID, USER_ID, Date.valueOf("2026-06-17"), 420000L));

		assertEquals("1 minutes_abbr",
				snapshot(WIDGET_PRESENCE_ACCESS, METRIC_PRESENCE_AVERAGE).getPrimary());
	}

	@Test
	public void presenceDurationIncludesEveryNonzeroRemainingUnit() {
		long durationMs = ((2L * 24L + 1L) * 3600L + 5L * 60L + 3L) * 1000L;
		db.insertObject(presenceStat(SITE_ID, USER_ID, new java.util.Date(), durationMs));

		assertEquals("2 days_abbr 1 hours_abbr 5 minutes_abbr 3 seconds_abbr",
				snapshot(WIDGET_PRESENCE_ACCESS, METRIC_PRESENCE_TOTAL_7D).getPrimary());
	}

	@Test
	public void presenceSparklineSumsRawDurationsBeforeConvertingToMinutes() {
		Date today = Date.valueOf(java.time.LocalDate.now());
		db.insertObject(presenceStat(SITE_ID, USER_ID, today, 90000L));
		db.insertObject(presenceStat(SITE_ID, OTHER_USER_ID, today, 90000L));

		SiteStatsOverview overview = service.getOverview(SITE_ID);

		assertEquals(1, widget(overview, WIDGET_PRESENCE_ACCESS).getHighlights().size());
		List<SiteStatsChartPoint> points = widget(overview, WIDGET_PRESENCE_ACCESS)
				.getHighlights().get(0).getDatasets().get(0).getPoints();
		assertEquals(30, points.size());
		long totalMinutes = 0L;
		for (SiteStatsChartPoint point : points) {
			if (point.getY() != null) {
				totalMinutes += point.getY().longValue();
			}
		}
		assertEquals(3L, totalMinutes);
	}

	@Test
	public void trafficWidgetReportsUniqueVisitorsAndVisitsByRole() {
		Date lastVisit = Date.valueOf("2026-06-17");
		db.insertObject(eventStat(SITE_ID, USER_ID, FakeData.TOOL_CHAT, StatsManager.SITEVISIT_EVENTID,
				lastVisit, 3));

		SiteStatsOverview overview = service.getOverview(SITE_ID);

		assertEquals("1", snapshot(WIDGET_VISITS, METRIC_VISITS_UNIQUE).getPrimary());
		assertEquals("1 / 2", snapshot(WIDGET_VISITS, METRIC_VISITS_USERS_WITH_VISITS).getPrimary());
		assertEquals(Integer.valueOf(50),
				snapshot(WIDGET_VISITS, METRIC_VISITS_USERS_WITH_VISITS).getPercentage());
		assertTrue(widget(overview, WIDGET_VISITS).getHighlights().isEmpty());

		db.insertObject(visitStat(SITE_ID, Date.valueOf(java.time.LocalDate.now()), 2, 1));
		overview = service.getOverview(SITE_ID);
		assertEquals(1, widget(overview, WIDGET_VISITS).getHighlights().size());
		assertEquals(30, widget(overview, WIDGET_VISITS).getHighlights().get(0).getDatasets().get(0).getPoints().size());
		assertTrue(widget(overview, WIDGET_VISITS).getHighlights().get(0).isCompact());

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_ALL);
		SiteStatsReportView byRole = service.getWidgetReport(SITE_ID, WIDGET_VISITS, TAB_BY_ROLE, request);
		assertEquals(WIDGET_VISITS, byRole.getWidgetId());
		assertEquals(TAB_BY_ROLE, byRole.getTabId());
		assertNotNull(byRole.getChart());
		assertFalse(byRole.getChart().getDatasets().isEmpty());
		assertEquals("Instructor", byRole.getChart().getDatasets().get(0).getPoints().get(0).getLabel());
		assertEquals(Long.valueOf(3), byRole.getChart().getDatasets().get(0).getPoints().get(0).getY());
		assertEquals(1, byRole.getTable().getTotalRows());
		assertEquals("Instructor", byRole.getTable().getRows().get(0).getCells().get("role").getDisplay());
	}

	@Test
	public void lastVisitMetricIgnoresUsersWhoCanUpdateTheSite() {
		db.insertObject(eventStat(SITE_ID, USER_ID, FakeData.TOOL_CHAT, StatsManager.SITEVISIT_EVENTID,
				Date.valueOf("2026-06-20"), 2));
		db.insertObject(eventStat(SITE_ID, OTHER_USER_ID, FakeData.TOOL_CHAT, StatsManager.SITEVISIT_EVENTID,
				Date.valueOf("2026-06-17"), 1));

		assertTrue(snapshot(WIDGET_VISITS, METRIC_PRESENCE_LAST_VISIT).getPrimary().contains("2026"));
		assertEquals("User B", snapshot(WIDGET_VISITS, METRIC_PRESENCE_LAST_VISIT).getDetail());

		SiteStatsReportView lastVisitReport = service.getWidgetMetricReport(SITE_ID, WIDGET_VISITS,
				METRIC_PRESENCE_LAST_VISIT, new SiteStatsReportRequest());
		assertNotNull(lastVisitReport.getTable());
		assertEquals(1, lastVisitReport.getTable().getTotalRows());
	}

	@Test
	public void studentPresenceAccessWidgetUsesCurrentUserLastVisit() {
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_ALL, SITE_REF)).thenReturn(false);
		Date lastVisit = Date.valueOf("2026-06-17");
		db.insertObject(presenceTotal(SITE_ID, USER_ID, lastVisit, 1));
		db.insertObject(presenceStat(SITE_ID, USER_ID, new java.util.Date(), 60000L));

		SiteStatsOverview overview = service.getOverview(SITE_ID);
		assertTrue(hasWidget(overview, WIDGET_STUDENT_PRESENCE_ACCESS));

		assertTrue(snapshot(WIDGET_STUDENT_VISITS, METRIC_STUDENT_PRESENCE_LAST_VISIT)
				.getPrimary().contains("2026"));
		assertEquals("1 minutes_abbr",
				snapshot(WIDGET_STUDENT_PRESENCE_ACCESS, METRIC_STUDENT_PRESENCE_TOTAL_7D).getPrimary());

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_ALL);
		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_STUDENT_PRESENCE_ACCESS, TAB_BY_DATE, request);
		assertEquals(WIDGET_STUDENT_PRESENCE_ACCESS, view.getWidgetId());
		assertNotNull(view.getTable());
	}

	@Test
	public void getWidgetMetricsReturnsStableMetricMetadata() {
		List<SiteStatsWidgetMetric> metrics = service.getWidgetMetrics(SITE_ID, WIDGET_VISITS);

		assertEquals(METRIC_VISITS_TOTAL, metrics.get(0).getId());
		assertEquals(5, metrics.size());
		assertEquals(AUDIENCE_ALL, metrics.get(0).getAudience());
		assertNotNull(metrics.get(0).getWidgetTitle());
		assertFalse(metrics.get(0).getWidgetTitle().isEmpty());
		assertTrue(metrics.get(0).isReportable());
		assertEquals(METRIC_PRESENCE_LAST_VISIT, metrics.get(3).getId());
		assertTrue(metrics.get(3).isReportable());
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
	public void getWidgetReportBuildsReportViewWithoutReportDef() {
		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_CUSTOM);
		request.setWhenFrom("2026-06-01");
		request.setWhenTo("2026-06-30");
		request.setItemType("assignment");
		request.setThreshold(Double.valueOf(70));

		SiteStatsReportView view = service.getWidgetReport(SITE_ID, ViewFactoryFixtureWidgetDefinition.WIDGET_ID,
				ViewFactoryFixtureWidgetDefinition.TAB_ID, request);

		assertEquals(ViewFactoryFixtureWidgetDefinition.WIDGET_ID, view.getWidgetId());
		assertEquals(ViewFactoryFixtureWidgetDefinition.TAB_ID, view.getTabId());
		assertEquals("View factory fixture", view.getTitle());
		assertNotNull(view.getTable());
		assertEquals("assignment|70.0|2026-06-01|2026-06-30", view.getTable().getCaption());
	}

	@Test
	public void viewFactoryWidgetExposesThresholdAndItemTypeFilters() {
		SiteStatsWidgetTab tab = service.getWidgetTab(SITE_ID, ViewFactoryFixtureWidgetDefinition.WIDGET_ID,
				ViewFactoryFixtureWidgetDefinition.TAB_ID);

		SiteStatsFilter itemType = null;
		SiteStatsFilter threshold = null;
		SiteStatsFilter date = null;
		for (SiteStatsFilter filter : tab.getFilters()) {
			if ("itemType".equals(filter.getId())) {
				itemType = filter;
			} else if ("threshold".equals(filter.getId())) {
				threshold = filter;
			} else if ("date".equals(filter.getId())) {
				date = filter;
			}
		}
		assertNotNull(itemType);
		assertNotNull(threshold);
		assertNotNull(date);
		assertEquals(SiteStatsFilter.TYPE_SELECT, itemType.getType());
		assertEquals(1, itemType.getOptions().size());
		assertEquals("all", itemType.getOptions().get(0).getValue());
		assertEquals(SiteStatsFilter.TYPE_NUMBER, threshold.getType());
		assertEquals(ReportManager.WHEN_CUSTOM, date.getOptions().get(date.getOptions().size() - 1).getValue());

		SiteStatsFilter whenFrom = null;
		SiteStatsFilter whenTo = null;
		for (SiteStatsFilter filter : tab.getFilters()) {
			if ("whenFrom".equals(filter.getId())) {
				whenFrom = filter;
			} else if ("whenTo".equals(filter.getId())) {
				whenTo = filter;
			}
		}
		assertNotNull(whenFrom);
		assertNotNull(whenTo);
		assertEquals(SiteStatsFilter.TYPE_DATE, whenFrom.getType());
		assertEquals(SiteStatsFilter.TYPE_DATE, whenTo.getType());
		assertFalse(whenFrom.getLabel().isBlank());
		assertFalse(whenTo.getLabel().isBlank());
		assertNotEquals("whenFrom", whenFrom.getLabel());
		assertNotEquals("whenTo", whenTo.getLabel());
		LocalDate to = LocalDate.parse(whenTo.getValue());
		LocalDate from = LocalDate.parse(whenFrom.getValue());
		assertEquals(LocalDate.now(ZoneId.of("America/New_York")), to);
		assertEquals(to.minusDays(7), from);
	}

	@Test
	public void getWidgetMetricsComputesSnapshotsForViewFactoryWidgets() {
		List<SiteStatsWidgetMetric> metrics = service.getWidgetMetrics(SITE_ID, ViewFactoryFixtureWidgetDefinition.WIDGET_ID);

		assertEquals(ViewFactoryFixtureWidgetDefinition.METRIC_ID, metrics.get(0).getId());
		assertEquals("7", metrics.get(0).getSnapshot().getPrimary());
		assertTrue(metrics.get(0).isReportable());
	}

	@Test
	public void getWidgetReportAcceptsCustomDateRangeForReportDefWidgets() {
		db.insertObject(eventStat(SITE_ID, USER_ID, FakeData.TOOL_CHAT, StatsManager.SITEVISIT_EVENTID,
				Date.valueOf("2026-06-17"), 3));

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_CUSTOM);
		request.setWhenFrom("2026-01-01");
		request.setWhenTo("2026-09-01");

		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_VISITS, TAB_BY_DATE, request);

		assertNotNull(view.getTable());
		assertFalse(view.getTable().getRows().isEmpty());
		assertEquals(WIDGET_VISITS, view.getWidgetId());
	}

	@Test
	public void customDateWithoutValidBoundsReturnsEmptyTable() {
		db.insertObject(eventStat(SITE_ID, USER_ID, FakeData.TOOL_CHAT, StatsManager.SITEVISIT_EVENTID,
				Date.valueOf("2026-06-17"), 3));

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_CUSTOM);

		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_VISITS, TAB_BY_DATE, request);

		assertNotNull(view.getTable());
		assertFalse(view.getTable().getColumns().isEmpty());
		assertTrue(view.getTable().getRows().isEmpty());
	}

	@Test
	public void customDateWithInvertedRangeReturnsEmptyTable() {
		db.insertObject(eventStat(SITE_ID, USER_ID, FakeData.TOOL_CHAT, StatsManager.SITEVISIT_EVENTID,
				Date.valueOf("2026-06-17"), 3));

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_CUSTOM);
		request.setWhenFrom("2026-09-01");
		request.setWhenTo("2026-01-01");

		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_VISITS, TAB_BY_DATE, request);

		assertNotNull(view.getTable());
		assertFalse(view.getTable().getColumns().isEmpty());
		assertTrue(view.getTable().getRows().isEmpty());
	}

	@Test
	public void customDateWithoutValidBoundsReturnsEmptyVisitsByRole() {
		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_CUSTOM);

		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_VISITS, TAB_BY_ROLE, request);

		assertEquals(TAB_BY_ROLE, view.getTabId());
		assertNotNull(view.getTable());
		assertTrue(view.getTable().getRows().isEmpty());
	}

	@Test
	public void getWidgetReportAcceptsShortCustomDateRangeFromVisitTotals() {
		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_CUSTOM);
		request.setWhenFrom("2026-06-01");
		request.setWhenTo("2026-06-30");

		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_VISITS, TAB_BY_DATE, request);

		assertNotNull(view.getTable());
		assertFalse(view.getTable().getRows().isEmpty());
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

	private SiteStatsWidget widget(SiteStatsOverview overview, String widgetId) {
		for (SiteStatsWidget widget : overview.getWidgets()) {
			if (widgetId.equals(widget.getId())) {
				return widget;
			}
		}
		throw new AssertionError("Missing widget " + widgetId);
	}

	private SiteStatsWidgetMetric metric(SiteStatsOverview overview, String widgetId, String metricId) {
		for (SiteStatsWidget widget : overview.getWidgets()) {
			if (widgetId.equals(widget.getId())) {
				for (SiteStatsWidgetMetric metric : widget.getMetrics()) {
					if (metricId.equals(metric.getId())) {
						return metric;
					}
				}
			}
		}
		throw new AssertionError("Missing metric " + widgetId + "/" + metricId);
	}

	private SiteStatsWidgetMetric metricValue(String widgetId, String metricId) {
		for (SiteStatsWidgetMetric metric : service.getWidgetMetrics(SITE_ID, widgetId)) {
			if (metricId.equals(metric.getId())) {
				return metric;
			}
		}
		throw new AssertionError("Missing metric value " + widgetId + "/" + metricId);
	}

	private SiteStatsWidgetMetricSnapshot snapshot(String widgetId, String metricId) {
		return metricValue(widgetId, metricId).getSnapshot();
	}

	private boolean hasWidget(SiteStatsOverview overview, String widgetId) {
		return overview.getWidgets().stream().anyMatch(widget -> widgetId.equals(widget.getId()));
	}

	private List<String> metricIds(List<SiteStatsWidgetMetric> metrics) {
		List<String> ids = new ArrayList<String>();
		for (SiteStatsWidgetMetric metric : metrics) {
			ids.add(metric.getId());
		}
		return ids;
	}

}
