/*
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.webapi.controllers.test;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.view.SiteStatsOverview;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsReportSummary;
import org.sakaiproject.sitestats.api.view.SiteStatsReportView;
import org.sakaiproject.sitestats.api.view.SiteStatsViewService;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetMetric;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.webapi.controllers.SiteStatsController;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class SiteStatsControllerTests extends BaseControllerTests {

	private static final String SITE_ID = "site1";

	private MockMvc mockMvc;
	private SiteStatsViewService siteStatsViewService;

	@Before
	public void setup() {
		SiteStatsController controller = new SiteStatsController();

		Session session = mock(Session.class);
		when(session.getUserId()).thenReturn("user1");
		SessionManager sessionManager = mock(SessionManager.class);
		when(sessionManager.getCurrentSession()).thenReturn(session);
		controller.setSessionManager(sessionManager);

		Site site = mock(Site.class);
		SiteService siteService = mock(SiteService.class);
		when(siteService.getOptionalSite(SITE_ID)).thenReturn(Optional.of(site));
		controller.setSiteService(siteService);
		controller.setPortalService(mock(PortalService.class));

		siteStatsViewService = mock(SiteStatsViewService.class);
		ReflectionTestUtils.setField(controller, "siteStatsViewService", siteStatsViewService);

		mockMvc = MockMvcBuilders.standaloneSetup(controller).apply(configurer).build();
	}

	@Test
	public void getOverviewReturnsOverviewJson() throws Exception {
		SiteStatsOverview overview = new SiteStatsOverview();
		overview.setSiteId(SITE_ID);
		overview.setViewAllowed(true);
		when(siteStatsViewService.getOverview(SITE_ID)).thenReturn(overview);

		mockMvc.perform(get("/sites/" + SITE_ID + "/sitestats/overview"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.siteId", is(SITE_ID)))
			.andExpect(jsonPath("$.viewAllowed", is(true)));
	}

	@Test
	public void getReportsReturnsReportSummaryJson() throws Exception {
		SiteStatsReportSummary summary = new SiteStatsReportSummary();
		summary.setId(42L);
		summary.setSiteId(SITE_ID);
		summary.setTitle("Weekly activity");
		summary.setDescription("Activity for the last week");
		summary.setHidden(true);
		when(siteStatsViewService.getReports(SITE_ID)).thenReturn(Collections.singletonList(summary));

		mockMvc.perform(get("/sites/" + SITE_ID + "/sitestats/reports"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].id", is(42)))
			.andExpect(jsonPath("$[0].siteId", is(SITE_ID)))
			.andExpect(jsonPath("$[0].title", is("Weekly activity")))
			.andExpect(jsonPath("$[0].description", is("Activity for the last week")))
			.andExpect(jsonPath("$[0].hidden", is(true)));
	}

	@Test
	public void getWidgetMetricsReturnsMetricJson() throws Exception {
		SiteStatsWidgetMetric metric = new SiteStatsWidgetMetric("activity-events", "Events", "all", true);
		when(siteStatsViewService.getWidgetMetrics(SITE_ID, "activity")).thenReturn(Collections.singletonList(metric));

		mockMvc.perform(get("/sites/" + SITE_ID + "/sitestats/widgets/activity/metrics"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].id", is("activity-events")))
			.andExpect(jsonPath("$[0].label", is("Events")))
			.andExpect(jsonPath("$[0].audience", is("all")))
			.andExpect(jsonPath("$[0].reportable", is(true)));
	}

	@Test
	public void getWidgetMetricsMapsUnknownWidgetToNotFound() throws Exception {
		when(siteStatsViewService.getWidgetMetrics(SITE_ID, "missing")).thenThrow(new IllegalArgumentException("missing"));

		mockMvc.perform(get("/sites/" + SITE_ID + "/sitestats/widgets/missing/metrics"))
			.andExpect(status().isNotFound());
	}

	@Test
	public void getWidgetMetricReportReturnsMetricReportJsonAndPassesRequest() throws Exception {
		SiteStatsReportView view = new SiteStatsReportView();
		view.setSiteId(SITE_ID);
		view.setWidgetId("activity");
		view.setMetricId("activity-events");
		when(siteStatsViewService.getWidgetMetricReport(eq(SITE_ID), eq("activity"), eq("activity-events"), any(SiteStatsReportRequest.class)))
			.thenReturn(view);

		mockMvc.perform(get("/sites/" + SITE_ID + "/sitestats/widgets/activity/metrics/activity-events?include=chart&page=3&pageSize=10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.siteId", is(SITE_ID)))
			.andExpect(jsonPath("$.widgetId", is("activity")))
			.andExpect(jsonPath("$.metricId", is("activity-events")));

		ArgumentCaptor<SiteStatsReportRequest> captor = ArgumentCaptor.forClass(SiteStatsReportRequest.class);
		verify(siteStatsViewService).getWidgetMetricReport(eq(SITE_ID), eq("activity"), eq("activity-events"), captor.capture());
		SiteStatsReportRequest request = captor.getValue();
		org.junit.Assert.assertEquals(3, request.getPage());
		org.junit.Assert.assertEquals(10, request.getPageSize());
		org.junit.Assert.assertEquals(false, request.isIncludeTable());
		org.junit.Assert.assertEquals(true, request.isIncludeChart());
	}

	@Test
	public void getWidgetMetricReportMapsSecurityExceptionToForbidden() throws Exception {
		when(siteStatsViewService.getWidgetMetricReport(eq(SITE_ID), eq("activity"), eq("activity-events"), any(SiteStatsReportRequest.class)))
			.thenThrow(new SecurityException("forbidden"));

		mockMvc.perform(get("/sites/" + SITE_ID + "/sitestats/widgets/activity/metrics/activity-events"))
			.andExpect(status().isForbidden());
	}

	@Test
	public void getWidgetMetricReportMapsUnknownMetricToNotFound() throws Exception {
		when(siteStatsViewService.getWidgetMetricReport(eq(SITE_ID), eq("activity"), eq("missing"), any(SiteStatsReportRequest.class)))
			.thenThrow(new IllegalArgumentException("missing"));

		mockMvc.perform(get("/sites/" + SITE_ID + "/sitestats/widgets/activity/metrics/missing"))
			.andExpect(status().isNotFound());
	}

	@Test
	public void getWidgetReportMapsSecurityExceptionToForbidden() throws Exception {
		when(siteStatsViewService.getWidgetReport(eq(SITE_ID), eq("visits"), eq("bydate"), any(SiteStatsReportRequest.class)))
			.thenThrow(new SecurityException("forbidden"));

		mockMvc.perform(get("/sites/" + SITE_ID + "/sitestats/widgets/visits/tabs/bydate"))
			.andExpect(status().isForbidden());
	}

	@Test
	public void getReportMapsUnknownReportToNotFound() throws Exception {
		when(siteStatsViewService.getReport(eq(SITE_ID), eq(99L), any(SiteStatsReportRequest.class)))
			.thenThrow(new IllegalArgumentException("missing"));

		mockMvc.perform(get("/sites/" + SITE_ID + "/sitestats/reports/99"))
			.andExpect(status().isNotFound());
	}

	@Test
	public void getReportPassesIncludeAndPaging() throws Exception {
		SiteStatsReportView view = new SiteStatsReportView();
		view.setSiteId(SITE_ID);
		when(siteStatsViewService.getReport(eq(SITE_ID), eq(42L), any(SiteStatsReportRequest.class))).thenReturn(view);

		mockMvc.perform(get("/sites/" + SITE_ID + "/sitestats/reports/42?include=chart&page=2&pageSize=25"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.siteId", is(SITE_ID)));

		ArgumentCaptor<SiteStatsReportRequest> captor = ArgumentCaptor.forClass(SiteStatsReportRequest.class);
		verify(siteStatsViewService).getReport(eq(SITE_ID), eq(42L), captor.capture());
		SiteStatsReportRequest request = captor.getValue();
		org.junit.Assert.assertEquals(2, request.getPage());
		org.junit.Assert.assertEquals(25, request.getPageSize());
		org.junit.Assert.assertEquals(false, request.isIncludeTable());
		org.junit.Assert.assertEquals(true, request.isIncludeChart());
	}

	@Test
	public void getReportCapsPageSizeAtApiBoundary() throws Exception {
		SiteStatsReportView view = new SiteStatsReportView();
		view.setSiteId(SITE_ID);
		when(siteStatsViewService.getReport(eq(SITE_ID), eq(42L), any(SiteStatsReportRequest.class))).thenReturn(view);

		mockMvc.perform(get("/sites/" + SITE_ID + "/sitestats/reports/42?pageSize=1000"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.siteId", is(SITE_ID)));

		ArgumentCaptor<SiteStatsReportRequest> captor = ArgumentCaptor.forClass(SiteStatsReportRequest.class);
		verify(siteStatsViewService).getReport(eq(SITE_ID), eq(42L), captor.capture());
		org.junit.Assert.assertEquals(SiteStatsReportRequest.MAX_PAGE_SIZE, captor.getValue().getPageSize());
	}

	@Test
	public void getPreviewReportReturnsPreviewJson() throws Exception {
		SiteStatsReportView view = new SiteStatsReportView();
		view.setSiteId(SITE_ID);
		when(siteStatsViewService.getPreviewReport(eq(SITE_ID), eq("preview-1"), any(SiteStatsReportRequest.class))).thenReturn(view);

		mockMvc.perform(get("/sites/" + SITE_ID + "/sitestats/report-previews/preview-1?include=table&page=2&pageSize=25"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.siteId", is(SITE_ID)));

		ArgumentCaptor<SiteStatsReportRequest> captor = ArgumentCaptor.forClass(SiteStatsReportRequest.class);
		verify(siteStatsViewService).getPreviewReport(eq(SITE_ID), eq("preview-1"), captor.capture());
		SiteStatsReportRequest request = captor.getValue();
		org.junit.Assert.assertEquals(2, request.getPage());
		org.junit.Assert.assertEquals(25, request.getPageSize());
		org.junit.Assert.assertEquals(true, request.isIncludeTable());
		org.junit.Assert.assertEquals(false, request.isIncludeChart());
	}

	@Test
	public void getPreviewReportMapsUnknownPreviewToNotFound() throws Exception {
		when(siteStatsViewService.getPreviewReport(eq(SITE_ID), eq("missing"), any(SiteStatsReportRequest.class)))
			.thenThrow(new IllegalArgumentException("missing"));

		mockMvc.perform(get("/sites/" + SITE_ID + "/sitestats/report-previews/missing"))
			.andExpect(status().isNotFound());
	}
}
