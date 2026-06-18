/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import java.util.Date;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.event.SiteStatsToolEventsService;
import org.sakaiproject.sitestats.api.view.SiteStatsFilter;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;

@Getter(AccessLevel.PACKAGE)
@Slf4j
public class SiteStatsWidgetDefinitionSupport {

	@Setter private StatsManager statsManager;
	@Setter private ReportManager reportManager;
	@Setter private SiteStatsToolEventsService siteStatsToolEventsService;
	@Setter private EventRegistryService eventRegistryService;
	@Setter private SiteService siteService;
	@Setter private ContentHostingService contentHostingService;
	@Setter private UserDirectoryService userDirectoryService;

	private final WidgetFilterCatalog filterCatalog = new WidgetFilterCatalog(this);
	private final WidgetReportDefFactory reportFactory = new WidgetReportDefFactory(this);
	private final WidgetMetricSupport metricSupport = new WidgetMetricSupport(this);

	private ResourceLoader messages = new ResourceLoader("Messages");

	ReportDef baseReportDef(String siteId) {
		return reportFactory.baseReportDef(siteId);
	}

	ReportDef baseMetricReportDef(String siteId) {
		return reportFactory.baseMetricReportDef(siteId);
	}

	ReportDef activityBase(String siteId, SiteStatsReportRequest request) {
		return reportFactory.activityBase(siteId, request);
	}

	ReportDef activityMetricBase(String siteId) {
		return reportFactory.activityMetricBase(siteId);
	}

	WidgetReportDefinition resourceLikeByDateDefinition(String siteId, SiteStatsReportRequest request,
			String title, String what, String refRoot, String actionFilter) {
		return reportFactory.resourceLikeByDateDefinition(siteId, request, title, what, refRoot, actionFilter);
	}

	WidgetReportDefinition resourceLikeByUserDefinition(String siteId, SiteStatsReportRequest request,
			String title, String what, String refRoot, String actionFilter, String... extraTableTotals) {
		return reportFactory.resourceLikeByUserDefinition(siteId, request, title, what, refRoot, actionFilter, extraTableTotals);
	}

	WidgetReportDefinition resourceLikeByItemDefinition(String siteId, SiteStatsReportRequest request,
			String title, String what, String refRoot, String actionFilter, String itemColumn) {
		return reportFactory.resourceLikeByItemDefinition(siteId, request, title, what, refRoot, actionFilter, itemColumn);
	}

	ReportDef resourceMetricBase(String siteId, String action, String totalsBy) {
		return reportFactory.resourceMetricBase(siteId, action, totalsBy);
	}

	void applyRoleFilter(org.sakaiproject.sitestats.api.report.ReportParams params, SiteStatsReportRequest request) {
		reportFactory.applyRoleFilter(params, request);
	}

	void applyDateGrouping(org.sakaiproject.sitestats.api.report.ReportParams params, SiteStatsReportRequest request, boolean sortByDate) {
		reportFactory.applyDateGrouping(params, request, sortByDate);
	}

	List<String> dateTotals(SiteStatsReportRequest request, String... extraColumns) {
		return reportFactory.dateTotals(request, extraColumns);
	}

	List<SiteStatsFilter> filters(String siteId, List<String> ids) {
		return filterCatalog.filters(siteId, ids);
	}

	String dateFilter(SiteStatsReportRequest request) {
		return filterCatalog.dateFilter(request);
	}

	String roleFilter(SiteStatsReportRequest request) {
		return filterCatalog.roleFilter(request);
	}

	String toolFilter(SiteStatsReportRequest request) {
		return filterCatalog.toolFilter(request);
	}

	String resourceActionFilter(SiteStatsReportRequest request) {
		return filterCatalog.resourceActionFilter(request);
	}

	String lessonActionFilter(SiteStatsReportRequest request) {
		return filterCatalog.lessonActionFilter(request);
	}

	String message(String key) {
		try {
			return messages.getString(key);
		} catch (Exception e) {
			return key;
		}
	}

	String message(String key, String defaultValue) {
		String value = message(key);
		return key.equals(value) || org.apache.commons.lang3.StringUtils.startsWith(value, "[missing key") ? defaultValue : value;
	}

	double percent(long partial, long total) {
		return metricSupport.percent(partial, total);
	}

	String msToString(long ms) {
		return metricSupport.msToString(ms);
	}

	String userDisplayId(String userId) {
		return metricSupport.userDisplayId(userId);
	}

	String userTooltip(String userId) {
		return metricSupport.userTooltip(userId);
	}

	long sitePresenceDuration(String siteId, List<String> userIds) {
		return metricSupport.sitePresenceDuration(siteId, userIds);
	}

	Date firstPresenceDate(String siteId) {
		return metricSupport.firstPresenceDate(siteId);
	}

	int countExistingResources(Report report) {
		return metricSupport.countExistingResources(report);
	}
}
