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
	public void widgetReportBuildsStableApiUrlWithCustomFilters() {
		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_CUSTOM);
		request.setWhenFrom("2026-06-01");
		request.setWhenTo("2026-06-30");
		request.setItemType("sakai.assignment.grades");
		request.setGroup("group-1");
		request.setItem("sakai.assignment.grades:asn-homework-1");
		request.setThreshold(Double.valueOf(70));

		String url = SiteStatsApiUrls.widgetReport("site1", "grades", "bystudent", request);

		assertEquals("/api/sites/site1/sitestats/widgets/grades/tabs/bystudent?include=table,chart&page=1&pageSize=50"
				+ "&date=when-custom&whenFrom=2026-06-01&whenTo=2026-06-30&role=who-all&tool=all"
				+ "&itemType=sakai.assignment.grades&group=group-1&item=sakai.assignment.grades%3Aasn-homework-1&threshold=70.0", url);
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
	public void widgetMetricsBuildsStableApiUrlWithItemType() {
		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setItemType("sakai.assignment.grades,sakai.samigo");

		String url = SiteStatsApiUrls.widgetMetrics("site1", "submissions", request);

		assertEquals("/api/sites/site1/sitestats/widgets/submissions/metrics?itemType=sakai.assignment.grades%2Csakai.samigo", url);
	}

	@Test
	public void widgetHighlightsBuildsStableApiUrl() {
		String url = SiteStatsApiUrls.widgetHighlights("site1", "submissions");

		assertEquals("/api/sites/site1/sitestats/widgets/submissions/highlights", url);
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
