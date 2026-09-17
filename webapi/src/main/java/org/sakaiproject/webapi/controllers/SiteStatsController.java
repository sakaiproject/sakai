/******************************************************************************
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 ******************************************************************************/
package org.sakaiproject.webapi.controllers;

import java.util.Arrays;
import java.util.List;

import lombok.Setter;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.sitestats.api.view.SiteStatsApiUrls;
import org.sakaiproject.sitestats.api.view.SiteStatsChart;
import org.sakaiproject.sitestats.api.view.SiteStatsOverview;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsReportSummary;
import org.sakaiproject.sitestats.api.view.SiteStatsReportView;
import org.sakaiproject.sitestats.api.view.SiteStatsViewService;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetMetric;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class SiteStatsController extends AbstractSakaiApiController {

	@Autowired
	@Setter
	private SiteStatsViewService siteStatsViewService;

	@GetMapping(value = SiteStatsApiUrls.OVERVIEW_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
	public SiteStatsOverview getOverview(@PathVariable("siteId") String siteId) {
		checkSakaiSession();
		checkSite(siteId);
		try {
			return siteStatsViewService.getOverview(siteId);
		} catch (SecurityException e) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
		}
	}

	@GetMapping(value = SiteStatsApiUrls.REPORTS_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<SiteStatsReportSummary> getReports(@PathVariable("siteId") String siteId) {
		checkSakaiSession();
		checkSite(siteId);
		try {
			return siteStatsViewService.getReports(siteId);
		} catch (SecurityException e) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
		}
	}

	@GetMapping(value = SiteStatsApiUrls.REPORT_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
	public SiteStatsReportView getReport(@PathVariable("siteId") String siteId, @PathVariable("reportId") long reportId,
			@RequestParam(value = "include", required = false) String include,
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "pageSize", defaultValue = "50") int pageSize) {
		checkSakaiSession();
		checkSite(siteId);
		try {
			return siteStatsViewService.getReport(siteId, reportId, request(include, page, pageSize));
		} catch (SecurityException e) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}
	}

	@GetMapping(value = SiteStatsApiUrls.REPORT_PREVIEW_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
	public SiteStatsReportView getPreviewReport(@PathVariable("siteId") String siteId, @PathVariable("previewId") String previewId,
			@RequestParam(value = "include", required = false) String include,
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "pageSize", defaultValue = "50") int pageSize) {
		checkSakaiSession();
		checkSite(siteId);
		try {
			return siteStatsViewService.getPreviewReport(siteId, previewId, request(include, page, pageSize));
		} catch (SecurityException e) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}
	}

	@GetMapping(value = SiteStatsApiUrls.WIDGET_REPORT_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
	public SiteStatsReportView getWidgetReport(@PathVariable("siteId") String siteId, @PathVariable("widgetId") String widgetId, @PathVariable("tabId") String tabId,
			@RequestParam(value = "include", required = false) String include,
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "pageSize", defaultValue = "50") int pageSize,
			@RequestParam(value = "date", required = false) String date,
			@RequestParam(value = "role", required = false) String role,
			@RequestParam(value = "tool", required = false) String tool,
			@RequestParam(value = "resourceAction", required = false) String resourceAction,
			@RequestParam(value = "lessonAction", required = false) String lessonAction) {
		checkSakaiSession();
		checkSite(siteId);
		SiteStatsReportRequest request = request(include, page, pageSize);
		request.setDate(date);
		request.setWhenFrom(whenFrom);
		request.setWhenTo(whenTo);
		request.setRole(role);
		request.setTool(tool);
		request.setResourceAction(resourceAction);
		request.setLessonAction(lessonAction);
		request.setItemType(itemType);
		request.setGroup(group);
		request.setItem(item);
		request.setThreshold(threshold);
		try {
			return siteStatsViewService.getWidgetReport(siteId, widgetId, tabId, request);
		} catch (SecurityException e) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}
	}

	@GetMapping(value = SiteStatsApiUrls.WIDGET_METRICS_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<SiteStatsWidgetMetric> getWidgetMetrics(@PathVariable("siteId") String siteId, @PathVariable("widgetId") String widgetId) {
		checkSakaiSession();
		checkSite(siteId);
		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setItemType(itemType);
		try {
			return siteStatsViewService.getWidgetMetrics(siteId, widgetId, request);
		} catch (SecurityException e) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}
	}

	@GetMapping(value = SiteStatsApiUrls.WIDGET_HIGHLIGHTS_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<SiteStatsChart> getWidgetHighlights(@PathVariable String siteId, @PathVariable String widgetId,
			@RequestParam(required = false) String itemType) {
		checkSakaiSession();
		checkSite(siteId);
		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setItemType(itemType);
		try {
			return siteStatsViewService.getWidgetHighlights(siteId, widgetId, request);
		} catch (SecurityException e) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}
	}

	@GetMapping(value = SiteStatsApiUrls.WIDGET_METRIC_REPORT_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
	public SiteStatsReportView getWidgetMetricReport(@PathVariable("siteId") String siteId, @PathVariable("widgetId") String widgetId, @PathVariable("metricId") String metricId,
			@RequestParam(value = "include", required = false) String include,
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "pageSize", defaultValue = "50") int pageSize) {
		checkSakaiSession();
		checkSite(siteId);
		try {
			return siteStatsViewService.getWidgetMetricReport(siteId, widgetId, metricId, request(include, page, pageSize));
		} catch (SecurityException e) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}
	}

	@GetMapping(value = SiteStatsApiUrls.SERVER_WIDE_REPORT_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
	public SiteStatsReportView getServerWideReport(@PathVariable("siteId") String siteId, @PathVariable("reportType") String reportType) {
		checkSakaiSession();
		checkSite(siteId);
		try {
			return siteStatsViewService.getServerWideReport(siteId, reportType);
		} catch (SecurityException e) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}
	}

	private SiteStatsReportRequest request(String include, int page, int pageSize) {
		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setPage(page);
		request.setPageSize(pageSize);

		if (StringUtils.isNotBlank(include)) {
			List<String> includes = Arrays.asList(StringUtils.split(include, ','));
			request.setIncludeTable(includes.contains("table"));
			request.setIncludeChart(includes.contains("chart"));
		}

		return request;
	}
}
