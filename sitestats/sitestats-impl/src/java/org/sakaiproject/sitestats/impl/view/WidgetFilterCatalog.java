/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_LESSON_ACTION;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_RESOURCE_ACTION;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_ROLE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_TOOL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.view.SiteStatsFilter;
import org.sakaiproject.sitestats.api.view.SiteStatsFilterOption;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class WidgetFilterCatalog {

	private static final List<String> DATE_FILTERS = Arrays.asList(
			ReportManager.WHEN_ALL,
			ReportManager.WHEN_LAST365DAYS,
			ReportManager.WHEN_LAST30DAYS,
			ReportManager.WHEN_LAST7DAYS);

	private final SiteStatsWidgetDefinitionSupport support;

	WidgetFilterCatalog(SiteStatsWidgetDefinitionSupport support) {
		this.support = support;
	}

	List<SiteStatsFilter> filters(String siteId, List<String> ids) {
		List<SiteStatsFilter> filters = new ArrayList<SiteStatsFilter>();
		for (String id : ids) {
			SiteStatsFilter filter = new SiteStatsFilter();
			filter.setId(id);
			filter.setType("select");
			filter.setLabel(filterLabel(id));
			filter.setOptions(filterOptions(siteId, id));
			filters.add(filter);
		}
		return filters;
	}

	String dateFilter(SiteStatsReportRequest request) {
		String date = StringUtils.trimToNull(SiteStatsReportRequest.normalized(request).getDate());
		return DATE_FILTERS.contains(date) ? date : ReportManager.WHEN_LAST7DAYS;
	}

	String roleFilter(SiteStatsReportRequest request) {
		return StringUtils.defaultIfBlank(SiteStatsReportRequest.normalized(request).getRole(), ReportManager.WHO_ALL);
	}

	String toolFilter(SiteStatsReportRequest request) {
		return StringUtils.defaultIfBlank(SiteStatsReportRequest.normalized(request).getTool(), ReportManager.WHAT_EVENTS_ALLTOOLS);
	}

	String resourceActionFilter(SiteStatsReportRequest request) {
		return StringUtils.trimToNull(SiteStatsReportRequest.normalized(request).getResourceAction());
	}

	String lessonActionFilter(SiteStatsReportRequest request) {
		return StringUtils.trimToNull(SiteStatsReportRequest.normalized(request).getLessonAction());
	}

	private String filterLabel(String id) {
		if (FILTER_DATE.equals(id)) {
			return support.message("report_when_period");
		}
		if (FILTER_ROLE.equals(id)) {
			return support.message("report_who_role");
		}
		if (FILTER_TOOL.equals(id)) {
			return support.message("report_option_tool");
		}
		if (FILTER_RESOURCE_ACTION.equals(id)) {
			return support.message("report_option_resourceaction");
		}
		if (FILTER_LESSON_ACTION.equals(id)) {
			return support.message("th_action");
		}
		return id;
	}

	private List<SiteStatsFilterOption> filterOptions(String siteId, String id) {
		if (FILTER_DATE.equals(id)) {
			return dateFilterOptions();
		}
		if (FILTER_ROLE.equals(id)) {
			return roleFilterOptions(siteId);
		}
		if (FILTER_TOOL.equals(id)) {
			return toolFilterOptions(siteId);
		}
		if (FILTER_RESOURCE_ACTION.equals(id)) {
			return resourceActionFilterOptions();
		}
		if (FILTER_LESSON_ACTION.equals(id)) {
			return lessonActionFilterOptions();
		}
		return Collections.emptyList();
	}

	private List<SiteStatsFilterOption> dateFilterOptions() {
		List<SiteStatsFilterOption> options = new ArrayList<SiteStatsFilterOption>();
		options.add(option(ReportManager.WHEN_ALL, support.message("overview_filter_date_all")));
		options.add(option(ReportManager.WHEN_LAST365DAYS, support.message("report_when_last365days")));
		options.add(option(ReportManager.WHEN_LAST30DAYS, support.message("report_when_last30days")));
		options.add(option(ReportManager.WHEN_LAST7DAYS, support.message("report_when_last7days")));
		return options;
	}

	private List<SiteStatsFilterOption> roleFilterOptions(String siteId) {
		List<SiteStatsFilterOption> options = new ArrayList<SiteStatsFilterOption>();
		options.add(option(ReportManager.WHO_ALL, support.message("overview_filter_role_all")));
		try {
			Site site = support.getSiteService().getSite(siteId);
			Set<Role> roles = site.getRoles();
			for (Role role : roles) {
				options.add(option(role.getId(), role.getId()));
			}
		} catch (IdUnusedException e) {
			log.warn("Site does not exist: {}", siteId);
		}
		return options;
	}

	private List<SiteStatsFilterOption> toolFilterOptions(String siteId) {
		List<SiteStatsFilterOption> options = new ArrayList<SiteStatsFilterOption>();
		options.add(option(ReportManager.WHAT_EVENTS_ALLTOOLS, support.message("overview_filter_tool_all")));
		PrefsData prefsData = support.getStatsManager().getPreferences(siteId, false);
		for (String toolId : support.getSiteStatsToolEventsService().getToolIds(siteId, prefsData)) {
			options.add(option(toolId, toolName(toolId)));
		}
		return options;
	}

	private String toolName(String toolId) {
		if (support.getEventRegistryService() == null) {
			return toolId;
		}
		return StringUtils.defaultIfBlank(support.getEventRegistryService().getToolName(toolId), toolId);
	}

	private List<SiteStatsFilterOption> resourceActionFilterOptions() {
		List<SiteStatsFilterOption> options = new ArrayList<SiteStatsFilterOption>();
		options.add(option("", support.message("overview_filter_resaction_all")));
		options.add(option(ReportManager.WHAT_RESOURCES_ACTION_NEW, support.message("action_new")));
		options.add(option(ReportManager.WHAT_RESOURCES_ACTION_READ, support.message("action_read")));
		options.add(option(ReportManager.WHAT_RESOURCES_ACTION_REVS, support.message("action_revise")));
		options.add(option(ReportManager.WHAT_RESOURCES_ACTION_DEL, support.message("action_delete")));
		options.add(option(ReportManager.WHAT_RESOURCES_ACTION_DOW, support.message("action_zipdownload")));
		return options;
	}

	private List<SiteStatsFilterOption> lessonActionFilterOptions() {
		List<SiteStatsFilterOption> options = new ArrayList<SiteStatsFilterOption>();
		options.add(option("", support.message("overview_filter_resaction_all")));
		options.add(option(ReportManager.WHAT_LESSONS_ACTION_CREATE, support.message("action_create")));
		options.add(option(ReportManager.WHAT_LESSONS_ACTION_READ, support.message("action_read")));
		options.add(option(ReportManager.WHAT_LESSONS_ACTION_DELETE, support.message("action_delete")));
		options.add(option(ReportManager.WHAT_LESSONS_ACTION_UPDATE, support.message("action_update")));
		return options;
	}

	private SiteStatsFilterOption option(String value, String label) {
		return new SiteStatsFilterOption(value, label);
	}
}
