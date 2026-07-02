/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.view.SiteStatsApiUrls;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;

public class SiteStatsApiUrlsTest {

	@Test
	public void persistedReportBuildsStableApiUrl() {
		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setPage(2);
		request.setPageSize(25);

		String url = SiteStatsApiUrls.persistedReport("site 1", 42, request);

		assertEquals("/api/sites/site+1/sitestats/reports/42?include=table,chart&page=2&pageSize=25", url);
	}

	@Test
	public void previewReportBuildsStableApiUrl() {
		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setIncludeChart(false);

		String url = SiteStatsApiUrls.previewReport("site/1", "preview 1", request);

		assertEquals("/api/sites/site%2F1/sitestats/report-previews/preview+1?include=table&page=1&pageSize=50", url);
	}

	@Test
	public void widgetReportBuildsStableApiUrlWithFilters() {
		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setPageSize(5);
		request.setDate(ReportManager.WHEN_LAST30DAYS);
		request.setRole("Instructor & Mentor");
		request.setTool("sakai.assignment");
		request.setLessonAction(ReportManager.WHAT_LESSONS_ACTION_READ);

		String url = SiteStatsApiUrls.widgetReport("site/1", "activity", "bytool", request);

		assertEquals("/api/sites/site%2F1/sitestats/widgets/activity/tabs/bytool?include=table,chart&page=1&pageSize=5"
				+ "&date=when-last30days&role=Instructor+%26+Mentor&tool=sakai.assignment&lessonAction=read", url);
	}

	@Test
	public void widgetReportCanRequestOnlyTable() {
		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setIncludeChart(false);

		String url = SiteStatsApiUrls.widgetReport("site1", "visits", "bydate", request);

		assertEquals("/api/sites/site1/sitestats/widgets/visits/tabs/bydate?include=table&page=1&pageSize=50"
				+ "&date=when-last7days&role=who-all&tool=all", url);
	}

	@Test
	public void widgetMetricsBuildsStableApiUrl() {
		String url = SiteStatsApiUrls.widgetMetrics("site/1", "student-visits");

		assertEquals("/api/sites/site%2F1/sitestats/widgets/student-visits/metrics", url);
	}

	@Test
	public void widgetMetricReportBuildsStableApiUrl() {
		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setIncludeTable(false);
		request.setPage(3);
		request.setPageSize(10);

		String url = SiteStatsApiUrls.widgetMetricReport("site 1", "activity", "activity-most-active-tool", request);

		assertEquals("/api/sites/site+1/sitestats/widgets/activity/metrics/activity-most-active-tool"
				+ "?include=chart&page=3&pageSize=10", url);
	}

	@Test
	public void reportUrlsUseApiPageSizeCap() {
		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setPageSize(1000);

		String url = SiteStatsApiUrls.persistedReport("site1", 42, request);

		assertEquals("/api/sites/site1/sitestats/reports/42?include=table,chart&page=1&pageSize=500", url);
	}
}
