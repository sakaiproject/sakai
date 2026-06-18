/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.AUDIENCE_ALL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_RESOURCES_FILES;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_RESOURCES_MOST_OPENED_FILE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_RESOURCES_OPENED_FILES;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_RESOURCES_USER_OPENED_MORE_FILES;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_RESOURCE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_USER;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_RESOURCE_ACTION;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_ROLE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_RESOURCES;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.sitestats.api.ResourceStat;
import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;

public class ResourcesWidgetDefinition extends AbstractSiteStatsWidgetDefinition {

	@Override
	public WidgetSpec getSpec() {
		return widgetSpec(WIDGET_RESOURCES, "overview_title_resources", "sakai-resources", AUDIENCE_ALL,
				() -> support.getStatsManager().isEnableResourceStats(),
				tabs(
						tabSpec(WIDGET_RESOURCES, TAB_BY_DATE, "overview_tab_bydate", this::resourcesByDateDefinition,
								FILTER_DATE, FILTER_ROLE,
								FILTER_RESOURCE_ACTION),
						tabSpec(WIDGET_RESOURCES, TAB_BY_USER, "overview_tab_byuser", this::resourcesByUserDefinition,
								FILTER_DATE, FILTER_ROLE,
								FILTER_RESOURCE_ACTION),
						tabSpec(WIDGET_RESOURCES, TAB_BY_RESOURCE, "overview_tab_byresource", this::resourcesByResourceDefinition,
								FILTER_DATE, FILTER_ROLE,
								FILTER_RESOURCE_ACTION)),
				metrics(
						metricSpec(WIDGET_RESOURCES, METRIC_RESOURCES_FILES, "overview_title_resources_sum", AUDIENCE_ALL,
								this::resourcesFilesMetricDefinition, this::resourcesFilesValue),
						metricSpec(WIDGET_RESOURCES, METRIC_RESOURCES_OPENED_FILES, "overview_title_openedfiles_sum", AUDIENCE_ALL,
								this::resourcesOpenedFilesMetricDefinition, this::resourcesOpenedFilesValue),
						metricSpec(WIDGET_RESOURCES, METRIC_RESOURCES_MOST_OPENED_FILE, "overview_title_mostopenedfile_sum", AUDIENCE_ALL,
								this::resourcesOpenedFilesMetricDefinition, this::resourcesMostOpenedFileValue),
						metricSpec(WIDGET_RESOURCES, METRIC_RESOURCES_USER_OPENED_MORE_FILES, "overview_title_useropenedmorefile_sum", AUDIENCE_ALL,
								this::resourcesUserOpenedMoreFilesMetricDefinition, this::resourcesUserOpenedMoreFilesValue)));
	}

	private WidgetReportDefinition resourcesByDateDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		return support.resourceLikeByDateDefinition(siteId, request, support.message("overview_title_resources"),
				ReportManager.WHAT_RESOURCES, StatsManager.RESOURCES_DIR + siteId + "/", support.resourceActionFilter(request));
	}

	private WidgetReportDefinition resourcesByUserDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		return support.resourceLikeByUserDefinition(siteId, request, support.message("overview_title_resources"),
				ReportManager.WHAT_RESOURCES, StatsManager.RESOURCES_DIR + siteId + "/", support.resourceActionFilter(request));
	}

	private WidgetReportDefinition resourcesByResourceDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		return support.resourceLikeByItemDefinition(siteId, request, support.message("overview_title_resources"),
				ReportManager.WHAT_RESOURCES, StatsManager.RESOURCES_DIR + siteId + "/", support.resourceActionFilter(request), StatsManager.T_RESOURCE);
	}

	private WidgetReportDefinition resourcesFilesMetricDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = support.resourceMetricBase(siteId, ReportManager.WHAT_RESOURCES_ACTION_NEW, StatsManager.T_RESOURCE);
		reportDef.getReportParams().setHowSortBy(StatsManager.T_RESOURCE);
		reportDef.getReportParams().setHowSortAscending(true);
		return new WidgetReportDefinition(support.message("overview_title_resources_sum"), null, reportDef);
	}

	private WidgetReportDefinition resourcesOpenedFilesMetricDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = support.resourceMetricBase(siteId, ReportManager.WHAT_RESOURCES_ACTION_READ, StatsManager.T_RESOURCE);
		reportDef.getReportParams().setHowSortBy(StatsManager.T_TOTAL);
		reportDef.getReportParams().setHowSortAscending(false);
		return new WidgetReportDefinition(support.message("overview_title_openedfiles_sum"), null, reportDef);
	}

	private WidgetReportDefinition resourcesUserOpenedMoreFilesMetricDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = support.resourceMetricBase(siteId, ReportManager.WHAT_RESOURCES_ACTION_READ, StatsManager.T_USER);
		reportDef.getReportParams().setHowSortBy(StatsManager.T_TOTAL);
		reportDef.getReportParams().setHowSortAscending(false);
		return new WidgetReportDefinition(support.message("overview_title_useropenedmorefile_sum"), null, reportDef);
	}

	private WidgetMetricValue resourcesFilesValue(String siteId, String userId) {
		return WidgetMetricValue.of(Integer.toString(support.getStatsManager().getTotalResources(siteId, true)));
	}

	private WidgetMetricValue resourcesOpenedFilesValue(String siteId, String userId) {
		Report report = support.getReportManager().getReport(resourcesOpenedFilesMetricDefinition(siteId, new SiteStatsReportRequest(), userId).getTableReportDef(), true, null, false);
		int total = support.countExistingResources(report);
		int totalFiles = support.getStatsManager().getTotalResources(siteId, true);
		return WidgetMetricValue.withPercentage(Integer.toString(total), (int) support.percent(total, totalFiles));
	}

	private WidgetMetricValue resourcesMostOpenedFileValue(String siteId, String userId) {
		Report report = support.getReportManager().getReport(resourcesOpenedFilesMetricDefinition(siteId, new SiteStatsReportRequest(), userId).getTableReportDef(), true, null, false);
		String resourceRef = null;
		for (Stat stat : report.getReportData()) {
			resourceRef = ((ResourceStat) stat).getResourceRef();
			break;
		}
		if (StringUtils.isBlank(resourceRef)) {
			return WidgetMetricValue.withDetail("-", null);
		}
		String value = support.getStatsManager().getResourceName(resourceRef, false);
		if ("null".equals(value)) {
			value = support.message("overview_file_unavailable");
		}
		return WidgetMetricValue.withDetail(value, support.getStatsManager().getResourceName(resourceRef, true));
	}

	private WidgetMetricValue resourcesUserOpenedMoreFilesValue(String siteId, String userId) {
		Report report = support.getReportManager().getReport(resourcesUserOpenedMoreFilesMetricDefinition(siteId, new SiteStatsReportRequest(), userId).getTableReportDef(), true, null, false);
		String activeUserId = null;
		for (Stat stat : report.getReportData()) {
			activeUserId = ((ResourceStat) stat).getUserId();
			break;
		}
		String displayId = activeUserId == null ? "-" : support.userDisplayId(activeUserId);
		return WidgetMetricValue.withDetail(displayId, support.userTooltip(activeUserId));
	}
}
