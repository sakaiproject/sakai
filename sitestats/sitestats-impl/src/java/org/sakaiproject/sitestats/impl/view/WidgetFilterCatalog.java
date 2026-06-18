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

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.view.SiteStatsFilter;
import org.sakaiproject.sitestats.api.view.SiteStatsFilterOption;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;

@Slf4j
public class WidgetFilterCatalog {

	private static final List<String> DATE_FILTERS = Arrays.asList(
			ReportManager.WHEN_ALL,
			ReportManager.WHEN_LAST365DAYS,
			ReportManager.WHEN_LAST30DAYS,
			ReportManager.WHEN_LAST7DAYS);

	@Setter private SiteStatsWidgetContext context;

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
			return context.message("report_when_period");
		}
		if (FILTER_ROLE.equals(id)) {
			return context.message("report_who_role");
		}
		if (FILTER_TOOL.equals(id)) {
			return context.message("report_option_tool");
		}
		if (FILTER_RESOURCE_ACTION.equals(id)) {
			return context.message("report_option_resourceaction");
		}
		if (FILTER_LESSON_ACTION.equals(id)) {
			return context.message("th_action");
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
		options.add(option(ReportManager.WHEN_ALL, context.message("overview_filter_date_all")));
		options.add(option(ReportManager.WHEN_LAST365DAYS, context.message("report_when_last365days")));
		options.add(option(ReportManager.WHEN_LAST30DAYS, context.message("report_when_last30days")));
		options.add(option(ReportManager.WHEN_LAST7DAYS, context.message("report_when_last7days")));
		return options;
	}

	private List<SiteStatsFilterOption> roleFilterOptions(String siteId) {
		List<SiteStatsFilterOption> options = new ArrayList<SiteStatsFilterOption>();
		options.add(option(ReportManager.WHO_ALL, context.message("overview_filter_role_all")));
		try {
			Site site = context.getSiteService().getSite(siteId);
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
		options.add(option(ReportManager.WHAT_EVENTS_ALLTOOLS, context.message("overview_filter_tool_all")));
		PrefsData prefsData = context.getStatsManager().getPreferences(siteId, false);
		for (String toolId : context.getSiteStatsToolEventsService().getToolIds(siteId, prefsData)) {
			options.add(option(toolId, toolName(toolId)));
		}
		return options;
	}

	private String toolName(String toolId) {
		if (context.getEventRegistryService() == null) {
			return toolId;
		}
		return StringUtils.defaultIfBlank(context.getEventRegistryService().getToolName(toolId), toolId);
	}

	private List<SiteStatsFilterOption> resourceActionFilterOptions() {
		List<SiteStatsFilterOption> options = new ArrayList<SiteStatsFilterOption>();
		options.add(option("", context.message("overview_filter_resaction_all")));
		options.add(option(ReportManager.WHAT_RESOURCES_ACTION_NEW, context.message("action_new")));
		options.add(option(ReportManager.WHAT_RESOURCES_ACTION_READ, context.message("action_read")));
		options.add(option(ReportManager.WHAT_RESOURCES_ACTION_REVS, context.message("action_revise")));
		options.add(option(ReportManager.WHAT_RESOURCES_ACTION_DEL, context.message("action_delete")));
		options.add(option(ReportManager.WHAT_RESOURCES_ACTION_DOW, context.message("action_zipdownload")));
		return options;
	}

	private List<SiteStatsFilterOption> lessonActionFilterOptions() {
		List<SiteStatsFilterOption> options = new ArrayList<SiteStatsFilterOption>();
		options.add(option("", context.message("overview_filter_resaction_all")));
		options.add(option(ReportManager.WHAT_LESSONS_ACTION_CREATE, context.message("action_create")));
		options.add(option(ReportManager.WHAT_LESSONS_ACTION_READ, context.message("action_read")));
		options.add(option(ReportManager.WHAT_LESSONS_ACTION_DELETE, context.message("action_delete")));
		options.add(option(ReportManager.WHAT_LESSONS_ACTION_UPDATE, context.message("action_update")));
		return options;
	}

	private SiteStatsFilterOption option(String value, String label) {
		return new SiteStatsFilterOption(value, label);
	}
}
