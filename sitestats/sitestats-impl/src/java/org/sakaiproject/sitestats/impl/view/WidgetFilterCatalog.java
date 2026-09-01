/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_ITEM_TYPE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_LESSON_ACTION;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_RESOURCE_ACTION;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_ROLE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_THRESHOLD;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_TOOL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_WHEN_FROM;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_WHEN_TO;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.ITEM_TYPE_ALL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.ITEM_TYPE_ASSIGNMENT;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.ITEM_TYPE_QUIZ;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

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
import org.sakaiproject.time.api.UserTimeService;

@Slf4j
public class WidgetFilterCatalog {

	private static final List<String> DATE_FILTERS = Arrays.asList(
			ReportManager.WHEN_ALL,
			ReportManager.WHEN_LAST365DAYS,
			ReportManager.WHEN_LAST30DAYS,
			ReportManager.WHEN_LAST7DAYS,
			ReportManager.WHEN_CUSTOM);
	private static final List<String> ITEM_TYPES = Arrays.asList(
			ITEM_TYPE_ALL,
			ITEM_TYPE_ASSIGNMENT,
			ITEM_TYPE_QUIZ);
	private static final int LONG_CUSTOM_RANGE_DAYS = 60;

	@Setter private SiteStatsWidgetContext context;

	List<SiteStatsFilter> filters(String siteId, List<String> ids) {
		List<String> filterIds = new ArrayList<String>(ids);
		if (filterIds.contains(FILTER_DATE)) {
			appendFilterId(filterIds, FILTER_WHEN_FROM);
			appendFilterId(filterIds, FILTER_WHEN_TO);
		}
		List<SiteStatsFilter> filters = new ArrayList<SiteStatsFilter>();
		for (String id : filterIds) {
			SiteStatsFilter filter = new SiteStatsFilter();
			filter.setId(id);
			filter.setType(filterType(id));
			filter.setLabel(filterLabel(id));
			filter.setOptions(filterOptions(siteId, id));
			filter.setValue(filterValue(id));
			filters.add(filter);
		}
		return filters;
	}

	String dateFilter(SiteStatsReportRequest request) {
		SiteStatsReportRequest safeRequest = SiteStatsReportRequest.normalized(request);
		String date = StringUtils.trimToNull(safeRequest.getDate());
		if (ReportManager.WHEN_CUSTOM.equals(date)) {
			return ReportManager.WHEN_CUSTOM;
		}
		return DATE_FILTERS.contains(date) ? date : ReportManager.WHEN_LAST7DAYS;
	}

	Date whenFrom(SiteStatsReportRequest request) {
		return parseIsoDate(SiteStatsReportRequest.normalized(request).getWhenFrom(), false);
	}

	Date whenTo(SiteStatsReportRequest request) {
		return parseIsoDate(SiteStatsReportRequest.normalized(request).getWhenTo(), true);
	}

	boolean isLongCustomRange(SiteStatsReportRequest request) {
		if (!ReportManager.WHEN_CUSTOM.equals(dateFilter(request))) {
			return false;
		}
		Date from = whenFrom(request);
		Date to = whenTo(request);
		if (from == null || to == null) {
			return false;
		}
		return ChronoUnit.DAYS.between(from.toInstant(), to.toInstant()) >= LONG_CUSTOM_RANGE_DAYS;
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

	String itemTypeFilter(SiteStatsReportRequest request) {
		String itemType = StringUtils.trimToNull(SiteStatsReportRequest.normalized(request).getItemType());
		return ITEM_TYPES.contains(itemType) ? itemType : ITEM_TYPE_ALL;
	}

	Double thresholdFilter(SiteStatsReportRequest request) {
		return SiteStatsReportRequest.normalized(request).getThreshold();
	}

	boolean isIncompleteCustomRange(SiteStatsReportRequest request) {
		SiteStatsReportRequest safeRequest = SiteStatsReportRequest.normalized(request);
		return ReportManager.WHEN_CUSTOM.equals(StringUtils.trimToNull(safeRequest.getDate()))
				&& !hasValidCustomDates(safeRequest);
	}

	private boolean hasValidCustomDates(SiteStatsReportRequest request) {
		Date from = whenFrom(request);
		Date to = whenTo(request);
		return from != null && to != null && !from.after(to);
	}

	private Date parseIsoDate(String value, boolean endOfDay) {
		String date = StringUtils.trimToNull(value);
		if (date == null) {
			return null;
		}
		try {
			LocalDate localDate = LocalDate.parse(date);
			ZonedDateTime zonedDateTime = endOfDay
					? localDate.atTime(23, 59, 59).atZone(zoneId())
					: localDate.atStartOfDay(zoneId());
			return Date.from(zonedDateTime.toInstant());
		} catch (DateTimeParseException e) {
			return null;
		}
	}

	private ZoneId zoneId() {
		UserTimeService userTimeService = context.getUserTimeService();
		if (userTimeService == null) {
			return ZoneId.systemDefault();
		}
		TimeZone timeZone = userTimeService.getLocalTimeZone();
		return timeZone != null ? timeZone.toZoneId() : ZoneId.systemDefault();
	}

	private String filterValue(String id) {
		if (FILTER_WHEN_FROM.equals(id)) {
			return today().minusDays(7).toString();
		}
		if (FILTER_WHEN_TO.equals(id)) {
			return today().toString();
		}
		return null;
	}

	private LocalDate today() {
		return LocalDate.now(zoneId());
	}

	private String filterType(String id) {
		if (FILTER_THRESHOLD.equals(id)) {
			return SiteStatsFilter.TYPE_NUMBER;
		}
		if (FILTER_WHEN_FROM.equals(id) || FILTER_WHEN_TO.equals(id)) {
			return SiteStatsFilter.TYPE_DATE;
		}
		return SiteStatsFilter.TYPE_SELECT;
	}

	private String filterLabel(String id) {
		if (FILTER_DATE.equals(id)) {
			return context.message("report_when_period");
		}
		if (FILTER_WHEN_FROM.equals(id)) {
			return context.message("report_when_from_date");
		}
		if (FILTER_WHEN_TO.equals(id)) {
			return context.message("report_when_to_date");
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
		if (FILTER_THRESHOLD.equals(id)) {
			return context.message("overview_filter_threshold");
		}
		if (FILTER_ITEM_TYPE.equals(id)) {
			return context.message("overview_filter_item_type");
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
		if (FILTER_ITEM_TYPE.equals(id)) {
			return itemTypeFilterOptions();
		}
		return Collections.emptyList();
	}

	private List<SiteStatsFilterOption> dateFilterOptions() {
		List<SiteStatsFilterOption> options = new ArrayList<SiteStatsFilterOption>();
		options.add(option(ReportManager.WHEN_ALL, context.message("overview_filter_date_all")));
		options.add(option(ReportManager.WHEN_LAST365DAYS, context.message("report_when_last365days")));
		options.add(option(ReportManager.WHEN_LAST30DAYS, context.message("report_when_last30days")));
		options.add(option(ReportManager.WHEN_LAST7DAYS, context.message("report_when_last7days")));
		options.add(option(ReportManager.WHEN_CUSTOM, context.message("report_when_custom")));
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

	private List<SiteStatsFilterOption> itemTypeFilterOptions() {
		List<SiteStatsFilterOption> options = new ArrayList<SiteStatsFilterOption>();
		options.add(option(ITEM_TYPE_ALL, context.message("overview_filter_item_type_all")));
		options.add(option(ITEM_TYPE_ASSIGNMENT, context.message("overview_filter_item_type_assignment")));
		options.add(option(ITEM_TYPE_QUIZ, context.message("overview_filter_item_type_quiz")));
		return options;
	}

	private SiteStatsFilterOption option(String value, String label) {
		return new SiteStatsFilterOption(value, label);
	}

	private void appendFilterId(List<String> filterIds, String id) {
		if (!filterIds.contains(id)) {
			filterIds.add(id);
		}
	}
}
