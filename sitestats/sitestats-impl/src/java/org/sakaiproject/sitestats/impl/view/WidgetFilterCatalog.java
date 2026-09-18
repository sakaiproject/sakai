/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_GROUP;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_ITEM;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_ITEM_TYPE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_LESSON_ACTION;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_RESOURCE_ACTION;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_ROLE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_THRESHOLD;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_TOOL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_WHEN_FROM;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_WHEN_TO;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.GROUP_ALL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.ITEM_ALL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.ITEM_TYPE_ALL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_GRADES;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_STUDENT_GRADES;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_STUDENT_SUBMISSIONS;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.assignment.api.AssignmentServiceConstants;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.grading.api.GradingAuthz;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.site.api.Group;
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
	private static final String STUDENT_FUNCTION = "section.role.student";
	private static final int LONG_CUSTOM_RANGE_DAYS = 60;

	@Setter private SiteStatsWidgetContext context;
	@Setter private SiteStatsSubmissionsAnalytics submissionsAnalytics;
	@Setter private SiteStatsGradesAnalytics gradesAnalytics;
	@Setter private ServerConfigurationService serverConfigurationService;

	List<SiteStatsFilter> filters(String siteId, List<String> ids) {
		return filters(siteId, null, ids);
	}

	List<SiteStatsFilter> filters(String siteId, String widgetId, List<String> ids) {
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
			filter.setOptions(filterOptions(siteId, widgetId, id));
			filter.setValue(filterValue(siteId, id));
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

	Date periodFrom(SiteStatsReportRequest request) {
		String date = dateFilter(request);
		if (ReportManager.WHEN_ALL.equals(date)) {
			return null;
		}
		if (ReportManager.WHEN_CUSTOM.equals(date)) {
			return whenFrom(request);
		}
		return Date.from(today().minusDays(presetDays(date)).atStartOfDay(zoneId()).toInstant());
	}

	Date periodTo(SiteStatsReportRequest request) {
		String date = dateFilter(request);
		if (ReportManager.WHEN_ALL.equals(date)) {
			return null;
		}
		if (ReportManager.WHEN_CUSTOM.equals(date)) {
			return whenTo(request);
		}
		return endOfLocalDay(today());
	}

	boolean isDueInRange(java.time.Instant due, SiteStatsReportRequest request) {
		Date from = periodFrom(request);
		Date to = periodTo(request);
		if (from == null && to == null) {
			return true;
		}
		if (due == null) {
			return false;
		}
		Date dueDate = Date.from(due);
		if (from != null && dueDate.before(from)) {
			return false;
		}
		return to == null || !dueDate.after(to);
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
		Set<String> itemTypes = itemTypesFilter(request);
		if (itemTypes.contains(ITEM_TYPE_ALL) || itemTypes.size() != 1) {
			return ITEM_TYPE_ALL;
		}
		return itemTypes.iterator().next();
	}

	Set<String> itemTypesFilter(SiteStatsReportRequest request) {
		String raw = StringUtils.trimToNull(SiteStatsReportRequest.normalized(request).getItemType());
		if (raw == null || ITEM_TYPE_ALL.equals(raw)) {
			return Collections.singleton(ITEM_TYPE_ALL);
		}
		Set<String> selected = new LinkedHashSet<String>();
		for (String part : StringUtils.split(raw, ',')) {
			String itemType = StringUtils.trimToNull(part);
			if (ITEM_TYPE_ALL.equals(itemType)) {
				return Collections.singleton(ITEM_TYPE_ALL);
			}
			if (itemType != null) {
				selected.add(itemType);
			}
		}
		return selected.isEmpty() ? Collections.singleton(ITEM_TYPE_ALL) : selected;
	}

	boolean includesItemType(SiteStatsReportRequest request, String itemType) {
		Set<String> selected = itemTypesFilter(request);
		return selected.contains(ITEM_TYPE_ALL) || selected.contains(itemType);
	}

	List<SiteStatsFilterOption> toolFilters(String siteId, String widgetId, List<String> ids) {
		if (isGradesWidget(widgetId) && gradesAnalytics != null) {
			return gradesToolFilters(siteId, WIDGET_STUDENT_GRADES.equals(widgetId));
		}
		return toolFilters(ids);
	}

	List<SiteStatsFilterOption> toolFilters(List<String> ids) {
		if (ids == null || ids.isEmpty()) {
			return Collections.emptyList();
		}
		List<SiteStatsFilterOption> options = new ArrayList<SiteStatsFilterOption>();
		for (String id : ids) {
			if (StringUtils.isNotBlank(id)) {
				options.add(option(id, toolName(id), "si-" + id.replace('.', '-')));
			}
		}
		options.sort(Comparator.comparing(SiteStatsFilterOption::getLabel, String.CASE_INSENSITIVE_ORDER)
				.thenComparing(SiteStatsFilterOption::getValue, String.CASE_INSENSITIVE_ORDER));
		return options;
	}

	private List<SiteStatsFilterOption> gradesToolFilters(String siteId, boolean studentView) {
		Set<String> present = gradesAnalytics.presentItemTypes(siteId, studentView);
		if (present.size() < 2) {
			return Collections.emptyList();
		}
		return toolFilters(new ArrayList<String>(present));
	}

	String groupFilter(SiteStatsReportRequest request) {
		return StringUtils.defaultIfBlank(SiteStatsReportRequest.normalized(request).getGroup(), GROUP_ALL);
	}

	String itemFilter(SiteStatsReportRequest request) {
		return StringUtils.defaultIfBlank(SiteStatsReportRequest.normalized(request).getItem(), ITEM_ALL);
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
			return endOfDay ? endOfLocalDay(localDate)
					: Date.from(localDate.atStartOfDay(zoneId()).toInstant());
		} catch (DateTimeParseException e) {
			return null;
		}
	}

	private Date endOfLocalDay(LocalDate localDate) {
		return Date.from(localDate.plusDays(1).atStartOfDay(zoneId()).minusNanos(1).toInstant());
	}

	private ZoneId zoneId() {
		UserTimeService userTimeService = context.getUserTimeService();
		if (userTimeService == null) {
			return ZoneId.systemDefault();
		}
		TimeZone timeZone = userTimeService.getLocalTimeZone();
		return timeZone != null ? timeZone.toZoneId() : ZoneId.systemDefault();
	}

	private String filterValue(String siteId, String id) {
		if (FILTER_WHEN_FROM.equals(id)) {
			return today().minusDays(7).toString();
		}
		if (FILTER_WHEN_TO.equals(id)) {
			return today().toString();
		}
		if (FILTER_THRESHOLD.equals(id)) {
			return formatThreshold(context.getStatsManager().getGradesThreshold(siteId));
		}
		return null;
	}

	private String formatThreshold(double value) {
		if (value == Math.rint(value)) {
			return String.valueOf((long) value);
		}
		return String.valueOf(value);
	}

	private LocalDate today() {
		return LocalDate.now(zoneId());
	}

	private int presetDays(String date) {
		if (ReportManager.WHEN_LAST30DAYS.equals(date)) {
			return 30;
		}
		if (ReportManager.WHEN_LAST365DAYS.equals(date)) {
			return 365;
		}
		return 7;
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
		if (FILTER_GROUP.equals(id)) {
			return context.message("overview_filter_group");
		}
		if (FILTER_ITEM.equals(id)) {
			return context.message("overview_filter_item");
		}
		return id;
	}

	private List<SiteStatsFilterOption> filterOptions(String siteId, String widgetId, String id) {
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
		if (FILTER_GROUP.equals(id)) {
			return groupFilterOptions(siteId);
		}
		if (FILTER_ITEM.equals(id)) {
			return itemFilterOptions(siteId, widgetId);
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
		return context.toolName(toolId);
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
		return options;
	}

	private List<SiteStatsFilterOption> groupFilterOptions(String siteId) {
		List<SiteStatsFilterOption> options = new ArrayList<SiteStatsFilterOption>();
		options.add(option(GROUP_ALL, context.message("overview_filter_group_all")));
		try {
			Site site = context.getSiteService().getSite(siteId);
			Collection<Group> groups = site.getGroups();
			if (groups == null || groups.isEmpty()) {
				return options;
			}
			List<Group> sorted = new ArrayList<Group>(groups);
			sorted.sort(Comparator.comparing(
					(Group group) -> group == null ? "" : StringUtils.defaultString(group.getTitle()),
					String.CASE_INSENSITIVE_ORDER));
			for (Group group : sorted) {
				if (group != null && StringUtils.isNotBlank(group.getId())) {
					options.add(option(group.getId(), StringUtils.defaultIfBlank(group.getTitle(), group.getId())));
				}
			}
		} catch (IdUnusedException e) {
			log.warn("Site does not exist: {}", siteId);
		}
		return options;
	}

	private List<SiteStatsFilterOption> itemFilterOptions(String siteId, String widgetId) {
		List<SiteStatsFilterOption> options = new ArrayList<SiteStatsFilterOption>();
		options.add(option(ITEM_ALL, context.message("overview_filter_item_all")));
		if (isGradesWidget(widgetId)) {
			if (gradesAnalytics != null) {
				options.addAll(gradesAnalytics.itemFilterOptions(siteId, WIDGET_STUDENT_GRADES.equals(widgetId)));
			}
			return options;
		}
		if (submissionsAnalytics != null) {
			options.addAll(submissionsAnalytics.itemFilterOptions(siteId, WIDGET_STUDENT_SUBMISSIONS.equals(widgetId)));
		}
		return options;
	}

	Set<String> assignmentSubmitters(String siteId) {
		Set<String> submitters = usersAllowed(siteId, AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT_SUBMISSION);
		String[] permissions = serverConfigurationService == null
				? null
				: serverConfigurationService.getStrings("assignment.submitter.remove.permission");
		if (permissions != null) {
			for (String permission : permissions) {
				submitters.removeAll(usersAllowed(siteId, permission));
			}
		} else {
			submitters.removeAll(usersAllowed(siteId, AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT));
		}
		return submitters;
	}

	Set<String> quizTakers(String siteId) {
		return usersAllowed(siteId, SamigoConstants.AUTHZ_TAKE_ASSESSMENT);
	}

	Set<String> gradeableUsers(String siteId) {
		Set<String> students = usersAllowed(siteId, STUDENT_FUNCTION);
		if (!students.isEmpty()) {
			return students;
		}
		return usersAllowed(siteId, GradingAuthz.PERMISSION_VIEW_OWN_GRADES);
	}

	private Set<String> usersAllowed(String siteId, String function) {
		try {
			Site site = context.getSiteService().getSite(siteId);
			Set<String> users = site.getUsersIsAllowed(function);
			return users == null ? Collections.<String>emptySet() : new HashSet<String>(users);
		} catch (IdUnusedException e) {
			log.warn("Site does not exist: {}", siteId);
			return Collections.emptySet();
		}
	}

	private boolean isGradesWidget(String widgetId) {
		return WIDGET_GRADES.equals(widgetId) || WIDGET_STUDENT_GRADES.equals(widgetId);
	}

	private SiteStatsFilterOption option(String value, String label) {
		return option(value, label, null);
	}

	private SiteStatsFilterOption option(String value, String label, String icon) {
		return new SiteStatsFilterOption(value, label, icon);
	}

	private void appendFilterId(List<String> filterIds, String id) {
		if (!filterIds.contains(id)) {
			filterIds.add(id);
		}
	}
}
