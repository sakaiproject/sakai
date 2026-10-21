/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.api.view;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SiteStatsApiUrls {

	public static final String API_PREFIX = "/api";
	public static final String BASE_PATH = "/sites/{siteId}/sitestats";
	public static final String OVERVIEW_PATH = BASE_PATH + "/overview";
	public static final String REPORTS_PATH = BASE_PATH + "/reports";
	public static final String REPORT_PATH = REPORTS_PATH + "/{reportId}";
	public static final String REPORT_PREVIEW_PATH = BASE_PATH + "/report-previews/{previewId}";
	public static final String WIDGET_REPORT_PATH = BASE_PATH + "/widgets/{widgetId}/tabs/{tabId}";
	public static final String WIDGET_METRICS_PATH = BASE_PATH + "/widgets/{widgetId}/metrics";
	public static final String WIDGET_HIGHLIGHTS_PATH = BASE_PATH + "/widgets/{widgetId}/highlights";
	public static final String WIDGET_METRIC_REPORT_PATH = WIDGET_METRICS_PATH + "/{metricId}";
	public static final String SERVER_WIDE_REPORT_PATH = BASE_PATH + "/server-wide/{reportType}";

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
		appendReportParams(endpoint, SiteStatsReportRequest.normalized(request), false);
		return endpoint.toString();
	}

	public static String previewReport(String siteId, String previewId, SiteStatsReportRequest request) {
		StringBuilder endpoint = new StringBuilder();
		endpoint.append(API_PREFIX);
		endpoint.append(siteBase(siteId));
		endpoint.append("/report-previews/");
		endpoint.append(encode(previewId));
		appendReportParams(endpoint, SiteStatsReportRequest.normalized(request), false);
		return endpoint.toString();
	}

	public static String widgetReport(String siteId, String widgetId, String tabId, SiteStatsReportRequest request) {
		SiteStatsReportRequest safeRequest = SiteStatsReportRequest.normalized(request);
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

	public static String widgetMetrics(String siteId, String widgetId) {
		return widgetMetrics(siteId, widgetId, null);
	}

	public static String widgetMetrics(String siteId, String widgetId, SiteStatsReportRequest request) {
		StringBuilder endpoint = new StringBuilder();
		endpoint.append(API_PREFIX);
		endpoint.append(siteBase(siteId));
		endpoint.append("/widgets/");
		endpoint.append(encode(widgetId));
		endpoint.append("/metrics");
		appendWidgetFilters(endpoint, request, true);
		return endpoint.toString();
	}

	public static String widgetHighlights(String siteId, String widgetId) {
		return widgetHighlights(siteId, widgetId, null);
	}

	public static String widgetHighlights(String siteId, String widgetId, SiteStatsReportRequest request) {
		StringBuilder endpoint = new StringBuilder();
		endpoint.append(API_PREFIX);
		endpoint.append(siteBase(siteId));
		endpoint.append("/widgets/");
		endpoint.append(encode(widgetId));
		endpoint.append("/highlights");
		appendWidgetFilters(endpoint, request, true);
		return endpoint.toString();
	}

	public static String widgetMetricReport(String siteId, String widgetId, String metricId, SiteStatsReportRequest request) {
		StringBuilder endpoint = new StringBuilder();
		endpoint.append(API_PREFIX);
		endpoint.append(siteBase(siteId));
		endpoint.append("/widgets/");
		endpoint.append(encode(widgetId));
		endpoint.append("/metrics/");
		endpoint.append(encode(metricId));
		appendReportParams(endpoint, SiteStatsReportRequest.normalized(request), false);
		return endpoint.toString();
	}

	public static String serverWideReport(String siteId, String reportType) {
		StringBuilder endpoint = new StringBuilder();
		endpoint.append(API_PREFIX);
		endpoint.append(siteBase(siteId));
		endpoint.append("/server-wide/");
		endpoint.append(encode(reportType));
		return endpoint.toString();
	}

	private static String siteBase(String siteId) {
		return "/sites/" + encode(siteId) + "/sitestats";
	}

	private static void appendReportParams(StringBuilder endpoint, SiteStatsReportRequest request, boolean includeWidgetFilters) {
		endpoint.append("?include=");
		endpoint.append(include(request));
		endpoint.append("&page=");
		endpoint.append(request.getPage());
		endpoint.append("&pageSize=");
		endpoint.append(request.getPageSize());

		if (includeWidgetFilters) {
			appendWidgetFilterParams(endpoint, request, false);
		}
	}

	private static void appendWidgetFilters(StringBuilder endpoint, SiteStatsReportRequest request, boolean startQuery) {
		if (request == null) {
			return;
		}
		appendWidgetFilterParams(endpoint, SiteStatsReportRequest.normalized(request), startQuery);
	}

	private static void appendWidgetFilterParams(StringBuilder endpoint, SiteStatsReportRequest request,
			boolean startQuery) {
		boolean first = startQuery;
		first = appendParam(endpoint, "date", request.getDate(), first);
		first = appendParam(endpoint, "whenFrom", request.getWhenFrom(), first);
		first = appendParam(endpoint, "whenTo", request.getWhenTo(), first);
		first = appendParam(endpoint, "role", request.getRole(), first);
		first = appendParam(endpoint, "tool", request.getTool(), first);
		first = appendParam(endpoint, "resourceAction", request.getResourceAction(), first);
		first = appendParam(endpoint, "lessonAction", request.getLessonAction(), first);
		first = appendParam(endpoint, "itemType", request.getItemType(), first);
		first = appendParam(endpoint, "group", request.getGroup(), first);
		first = appendParam(endpoint, "item", request.getItem(), first);
		if (request.getThreshold() != null) {
			first = appendParam(endpoint, "threshold", request.getThreshold().toString(), first);
		}
		first = appendParam(endpoint, "siteType", request.getSiteType(), first);
		first = appendParam(endpoint, "sitePrefix", request.getSitePrefix(), first);
		if (request.getPublishedOnly() != null) {
			appendParam(endpoint, "publishedOnly", request.getPublishedOnly().toString(), first);
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
		appendParam(endpoint, name, value, false);
	}

	private static boolean appendParam(StringBuilder endpoint, String name, String value, boolean startQuery) {
		if (StringUtils.isBlank(value)) {
			return startQuery;
		}
		endpoint.append(startQuery ? '?' : '&');
		endpoint.append(name);
		endpoint.append('=');
		endpoint.append(encode(value));
		return false;
	}

	private static String encode(String value) {
		return URLEncoder.encode(StringUtils.defaultString(value), StandardCharsets.UTF_8);
	}
}
