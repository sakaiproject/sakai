/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.api.view;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public final class SiteStatsApiUrls {

	public static final String API_PREFIX = "/api";
	public static final String BASE_PATH = "/sites/{siteId}/sitestats";
	public static final String OVERVIEW_PATH = BASE_PATH + "/overview";
	public static final String REPORTS_PATH = BASE_PATH + "/reports";
	public static final String REPORT_PATH = REPORTS_PATH + "/{reportId}";
	public static final String REPORT_PREVIEW_PATH = BASE_PATH + "/report-previews/{previewId}";
	public static final String WIDGET_REPORT_PATH = BASE_PATH + "/widgets/{widgetId}/tabs/{tabId}";

	private SiteStatsApiUrls() {
	}

	public static String overview(String siteId) {
		return API_PREFIX + siteBase(siteId) + "/overview";
	}

	public static String reports(String siteId) {
		return API_PREFIX + siteBase(siteId) + "/reports";
	}

	public static String persistedReport(String siteId, long reportId, SiteStatsReportRequest request) {
		StringBuilder endpoint = new StringBuilder();
		endpoint.append(API_PREFIX);
		endpoint.append(siteBase(siteId));
		endpoint.append("/reports/");
		endpoint.append(reportId);
		appendReportParams(endpoint, safeRequest(request), false);
		return endpoint.toString();
	}

	public static String previewReport(String siteId, String previewId, SiteStatsReportRequest request) {
		StringBuilder endpoint = new StringBuilder();
		endpoint.append(API_PREFIX);
		endpoint.append(siteBase(siteId));
		endpoint.append("/report-previews/");
		endpoint.append(encode(previewId));
		appendReportParams(endpoint, safeRequest(request), false);
		return endpoint.toString();
	}

	public static String widgetReport(String siteId, String widgetId, String tabId, SiteStatsReportRequest request) {
		SiteStatsReportRequest safeRequest = safeRequest(request);
		StringBuilder endpoint = new StringBuilder();
		endpoint.append(API_PREFIX);
		endpoint.append(siteBase(siteId));
		endpoint.append("/widgets/");
		endpoint.append(encode(widgetId));
		endpoint.append("/tabs/");
		endpoint.append(encode(tabId));
		appendReportParams(endpoint, safeRequest, true);
		return endpoint.toString();
	}

	private static String siteBase(String siteId) {
		return "/sites/" + encode(siteId) + "/sitestats";
	}

	private static SiteStatsReportRequest safeRequest(SiteStatsReportRequest request) {
		return request == null ? new SiteStatsReportRequest() : request;
	}

	private static void appendReportParams(StringBuilder endpoint, SiteStatsReportRequest request, boolean includeWidgetFilters) {
		endpoint.append("?include=");
		endpoint.append(include(request));
		endpoint.append("&page=");
		endpoint.append(request.getPage());
		endpoint.append("&pageSize=");
		endpoint.append(request.getPageSize());

		if (includeWidgetFilters) {
			appendParam(endpoint, "date", request.getDate());
			appendParam(endpoint, "role", request.getRole());
			appendParam(endpoint, "tool", request.getTool());
			appendParam(endpoint, "resourceAction", request.getResourceAction());
			appendParam(endpoint, "lessonAction", request.getLessonAction());
		}
	}

	private static String include(SiteStatsReportRequest request) {
		List<String> includes = new ArrayList<String>();
		if (request.isIncludeTable()) {
			includes.add("table");
		}
		if (request.isIncludeChart()) {
			includes.add("chart");
		}
		return StringUtils.join(includes, ',');
	}

	private static void appendParam(StringBuilder endpoint, String name, String value) {
		if (StringUtils.isNotBlank(value)) {
			endpoint.append('&');
			endpoint.append(name);
			endpoint.append('=');
			endpoint.append(encode(value));
		}
	}

	private static String encode(String value) {
		return URLEncoder.encode(StringUtils.defaultString(value), StandardCharsets.UTF_8);
	}
}
