/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.event.SiteStatsToolEventsService;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.api.view.SiteStatsFilter;
import org.sakaiproject.sitestats.api.view.SiteStatsFilterOption;
import org.sakaiproject.sitestats.api.view.SiteStatsOverview;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsWidget;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetTab;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class SiteStatsWidgetCatalog {

	private static final String WIDGET_VISITS = "visits";
	private static final String WIDGET_STUDENT_VISITS = "student-visits";
	private static final String WIDGET_ACTIVITY = "activity";
	private static final String WIDGET_RESOURCES = "resources";
	private static final String WIDGET_LESSONS = "lessons";

	private static final String TAB_BY_DATE = "bydate";
	private static final String TAB_BY_USER = "byuser";
	private static final String TAB_BY_TOOL = "bytool";
	private static final String TAB_BY_RESOURCE = "byresource";
	private static final String TAB_BY_PAGE = "bypage";

	private static final String FILTER_DATE = "date";
	private static final String FILTER_ROLE = "role";
	private static final String FILTER_TOOL = "tool";
	private static final String FILTER_RESOURCE_ACTION = "resourceAction";
	private static final String FILTER_LESSON_ACTION = "lessonAction";

	private static final List<String> DATE_FILTERS = Arrays.asList(
			ReportManager.WHEN_ALL,
			ReportManager.WHEN_LAST365DAYS,
			ReportManager.WHEN_LAST30DAYS,
			ReportManager.WHEN_LAST7DAYS);

	@Setter private StatsManager statsManager;
	@Setter private SiteStatsToolEventsService siteStatsToolEventsService;
	@Setter private EventRegistryService eventRegistryService;
	@Setter private SiteService siteService;

	private ResourceLoader messages = new ResourceLoader("Messages");
	private Map<String, WidgetReportFactory> reportRegistry;

	public SiteStatsOverview getOverview(String siteId, boolean allAllowed, boolean ownAllowed, boolean adminAllowed) {
		SiteStatsOverview overview = new SiteStatsOverview();
		overview.setSiteId(siteId);
		overview.setViewAllowed(true);
		overview.setAllAllowed(allAllowed);
		overview.setOwnAllowed(ownAllowed);
		overview.setAdminAllowed(adminAllowed);

		if (allAllowed) {
			overview.getWidgets().add(widget(WIDGET_VISITS, message("overview_title_visits"), "sakai-singleuser", "all",
					tab(TAB_BY_DATE, message("overview_tab_bydate"), filters(siteId, FILTER_DATE, FILTER_ROLE)),
					tab(TAB_BY_USER, message("overview_tab_byuser"), filters(siteId, FILTER_DATE, FILTER_ROLE))));
			overview.getWidgets().add(widget(WIDGET_ACTIVITY, message("overview_title_activity"), "sakai-poll", "all",
					tab(TAB_BY_DATE, message("overview_tab_bydate"), filters(siteId, FILTER_DATE, FILTER_ROLE, FILTER_TOOL)),
					tab(TAB_BY_USER, message("overview_tab_byuser"), filters(siteId, FILTER_DATE, FILTER_ROLE, FILTER_TOOL)),
					tab(TAB_BY_TOOL, message("overview_tab_bytool"), filters(siteId, FILTER_DATE, FILTER_ROLE, FILTER_TOOL))));
			if (statsManager.isEnableResourceStats()) {
				overview.getWidgets().add(widget(WIDGET_RESOURCES, message("overview_title_resources"), "sakai-resources", "all",
						tab(TAB_BY_DATE, message("overview_tab_bydate"), filters(siteId, FILTER_DATE, FILTER_ROLE, FILTER_RESOURCE_ACTION)),
						tab(TAB_BY_USER, message("overview_tab_byuser"), filters(siteId, FILTER_DATE, FILTER_ROLE, FILTER_RESOURCE_ACTION)),
						tab(TAB_BY_RESOURCE, message("overview_tab_byresource"), filters(siteId, FILTER_DATE, FILTER_ROLE, FILTER_RESOURCE_ACTION))));
			}
			if (statsManager.isEnableLessonsStats()) {
				overview.getWidgets().add(widget(WIDGET_LESSONS, message("overview_title_lessonpages"), "sakai-lessonbuildertool", "all",
						tab(TAB_BY_DATE, message("overview_tab_bydate"), filters(siteId, FILTER_DATE, FILTER_ROLE, FILTER_LESSON_ACTION)),
						tab(TAB_BY_USER, message("overview_tab_byuser"), filters(siteId, FILTER_DATE, FILTER_ROLE, FILTER_LESSON_ACTION)),
						tab(TAB_BY_PAGE, message("overview_tab_bypage"), filters(siteId, FILTER_DATE, FILTER_ROLE, FILTER_LESSON_ACTION))));
			}
		} else if (ownAllowed) {
			overview.getWidgets().add(widget(WIDGET_STUDENT_VISITS, message("overview_title_visits"), "sakai-singleuser", "own"));
		}

		return overview;
	}

	public boolean isOwnOnlyWidget(String widgetId) {
		return WIDGET_STUDENT_VISITS.equals(widgetId);
	}

	public WidgetReportDefinition getWidgetReportDefinition(String siteId, String widgetId, String tabId, SiteStatsReportRequest request) {
		WidgetReportFactory factory = reportRegistry().get(key(widgetId, tabId));
		if (factory == null) {
			throw new IllegalArgumentException("Unknown SiteStats widget report: " + widgetId + "/" + tabId);
		}
		return factory.build(siteId, SiteStatsReportRequests.orDefault(request));
	}

	private Map<String, WidgetReportFactory> reportRegistry() {
		if (reportRegistry == null) {
			Map<String, WidgetReportFactory> registry = new LinkedHashMap<String, WidgetReportFactory>();
			register(registry, WIDGET_VISITS, TAB_BY_DATE, this::visitsByDateDefinition);
			register(registry, WIDGET_VISITS, TAB_BY_USER, this::visitsByUserDefinition);
			register(registry, WIDGET_ACTIVITY, TAB_BY_DATE, this::activityByDateDefinition);
			register(registry, WIDGET_ACTIVITY, TAB_BY_USER, this::activityByUserDefinition);
			register(registry, WIDGET_ACTIVITY, TAB_BY_TOOL, this::activityByToolDefinition);
			register(registry, WIDGET_RESOURCES, TAB_BY_DATE, this::resourcesByDateDefinition);
			register(registry, WIDGET_RESOURCES, TAB_BY_USER, this::resourcesByUserDefinition);
			register(registry, WIDGET_RESOURCES, TAB_BY_RESOURCE, this::resourcesByResourceDefinition);
			register(registry, WIDGET_LESSONS, TAB_BY_DATE, this::lessonsByDateDefinition);
			register(registry, WIDGET_LESSONS, TAB_BY_USER, this::lessonsByUserDefinition);
			register(registry, WIDGET_LESSONS, TAB_BY_PAGE, this::lessonsByPageDefinition);
			reportRegistry = Collections.unmodifiableMap(registry);
		}
		return reportRegistry;
	}

	private void register(Map<String, WidgetReportFactory> registry, String widgetId, String tabId, WidgetReportFactory factory) {
		registry.put(key(widgetId, tabId), factory);
	}

	private String key(String widgetId, String tabId) {
		return widgetId + "/" + tabId;
	}

	private WidgetReportDefinition visitsByDateDefinition(String siteId, SiteStatsReportRequest request) {
		ReportDef reportDef = baseReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		if (ReportManager.WHO_ALL.equals(roleFilter(request))) {
			params.setWhat(ReportManager.WHAT_VISITS_TOTALS);
		} else {
			params.setWhat(ReportManager.WHAT_EVENTS);
			params.setWhatEventSelType(ReportManager.WHAT_EVENTS_BYEVENTS);
			params.setWhatEventIds(Arrays.asList(StatsManager.SITEVISIT_EVENTID));
			params.setWho(ReportManager.WHO_ROLE);
			params.setWhoRoleId(roleFilter(request));
		}
		applyDateGrouping(params, request, true);
		params.setHowTotalsBy(dateTotals(request, StatsManager.T_VISITS, StatsManager.T_UNIQUEVISITS));
		params.setHowChartType(StatsManager.CHARTTYPE_TIMESERIESBAR);
		params.setHowChartSource(StatsManager.T_DATE);
		params.setHowChartSeriesSource(StatsManager.T_NONE);
		return new WidgetReportDefinition(message("overview_title_visits"), reportDef, reportDef);
	}

	private WidgetReportDefinition visitsByUserDefinition(String siteId, SiteStatsReportRequest request) {
		ReportDef chart = visitsByUserChart(siteId, request);
		ReportDef table = new ReportDef(chart, siteId);
		table.getReportParams().setHowTotalsBy(Arrays.asList(StatsManager.T_USER));
		table.getReportParams().setHowSortBy(StatsManager.T_TOTAL);
		return new WidgetReportDefinition(message("overview_title_visits"), chart, table);
	}

	private ReportDef visitsByUserChart(String siteId, SiteStatsReportRequest request) {
		ReportDef reportDef = baseReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_EVENTS);
		params.setWhatEventSelType(ReportManager.WHAT_EVENTS_BYEVENTS);
		params.setWhatEventIds(Arrays.asList(StatsManager.SITEVISIT_EVENTID));
		applyRoleFilter(params, request);
		params.setHowTotalsBy(dateTotals(request, StatsManager.T_USER));
		applyDateGrouping(params, request, false);
		params.setHowSortBy(StatsManager.T_TOTAL);
		params.setHowChartType(StatsManager.CHARTTYPE_PIE);
		params.setHowChartSource(StatsManager.T_USER);
		return reportDef;
	}

	private WidgetReportDefinition activityByDateDefinition(String siteId, SiteStatsReportRequest request) {
		ReportDef reportDef = activityBase(siteId, request);
		reportDef.getReportParams().setHowTotalsBy(dateTotals(request));
		applyDateGrouping(reportDef.getReportParams(), request, true);
		reportDef.getReportParams().setHowChartType(StatsManager.CHARTTYPE_TIMESERIESBAR);
		reportDef.getReportParams().setHowChartSource(StatsManager.T_DATE);
		reportDef.getReportParams().setHowChartSeriesSource(StatsManager.T_NONE);
		return new WidgetReportDefinition(message("overview_title_activity"), reportDef, reportDef);
	}

	private WidgetReportDefinition activityByUserDefinition(String siteId, SiteStatsReportRequest request) {
		ReportDef chart = activityBase(siteId, request);
		chart.getReportParams().setHowTotalsBy(dateTotals(request, StatsManager.T_USER));
		applyDateGrouping(chart.getReportParams(), request, false);
		chart.getReportParams().setHowChartType(StatsManager.CHARTTYPE_PIE);
		chart.getReportParams().setHowChartSource(StatsManager.T_USER);
		ReportDef table = new ReportDef(chart, siteId);
		table.getReportParams().setHowTotalsBy(Arrays.asList(StatsManager.T_USER));
		table.getReportParams().setHowSortBy(StatsManager.T_TOTAL);
		return new WidgetReportDefinition(message("overview_title_activity"), chart, table);
	}

	private WidgetReportDefinition activityByToolDefinition(String siteId, SiteStatsReportRequest request) {
		ReportDef chart = activityBase(siteId, request);
		chart.getReportParams().setHowTotalsBy(Arrays.asList(StatsManager.T_TOOL));
		chart.getReportParams().setHowSortBy(StatsManager.T_DATE);
		chart.getReportParams().setHowChartType(StatsManager.CHARTTYPE_PIE);
		chart.getReportParams().setHowChartSource(StatsManager.T_TOOL);
		ReportDef table = new ReportDef(chart, siteId);
		table.getReportParams().setHowSortBy(StatsManager.T_TOTAL);
		return new WidgetReportDefinition(message("overview_title_activity"), chart, table);
	}

	private ReportDef activityBase(String siteId, SiteStatsReportRequest request) {
		ReportDef reportDef = baseReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_EVENTS);
		params.setWhatEventSelType(ReportManager.WHAT_EVENTS_BYEVENTS);
		params.setWhatEventIds(siteStatsToolEventsService.getEventsForToolFilter(toolFilter(request), siteId, statsManager.getPreferences(siteId, true), false));
		applyRoleFilter(params, request);
		return reportDef;
	}

	private WidgetReportDefinition resourcesByDateDefinition(String siteId, SiteStatsReportRequest request) {
		return resourceLikeByDateDefinition(siteId, request, message("overview_title_resources"),
				ReportManager.WHAT_RESOURCES, StatsManager.RESOURCES_DIR + siteId + "/", resourceActionFilter(request));
	}

	private WidgetReportDefinition resourcesByUserDefinition(String siteId, SiteStatsReportRequest request) {
		return resourceLikeByUserDefinition(siteId, request, WIDGET_RESOURCES, message("overview_title_resources"),
				ReportManager.WHAT_RESOURCES, StatsManager.RESOURCES_DIR + siteId + "/", resourceActionFilter(request));
	}

	private WidgetReportDefinition resourcesByResourceDefinition(String siteId, SiteStatsReportRequest request) {
		return resourceLikeByItemDefinition(siteId, request, message("overview_title_resources"),
				ReportManager.WHAT_RESOURCES, StatsManager.RESOURCES_DIR + siteId + "/", resourceActionFilter(request), StatsManager.T_RESOURCE);
	}

	private WidgetReportDefinition lessonsByDateDefinition(String siteId, SiteStatsReportRequest request) {
		return resourceLikeByDateDefinition(siteId, request, message("overview_title_lessonpages"),
				ReportManager.WHAT_LESSONPAGES, "/page/", lessonActionFilter(request));
	}

	private WidgetReportDefinition lessonsByUserDefinition(String siteId, SiteStatsReportRequest request) {
		return resourceLikeByUserDefinition(siteId, request, WIDGET_LESSONS, message("overview_title_lessonpages"),
				ReportManager.WHAT_LESSONPAGES, "/page/", lessonActionFilter(request));
	}

	private WidgetReportDefinition lessonsByPageDefinition(String siteId, SiteStatsReportRequest request) {
		return resourceLikeByItemDefinition(siteId, request, message("overview_title_lessonpages"),
				ReportManager.WHAT_LESSONPAGES, "/page/", lessonActionFilter(request), StatsManager.T_PAGE);
	}

	private WidgetReportDefinition resourceLikeByDateDefinition(String siteId, SiteStatsReportRequest request,
			String title, String what, String refRoot, String actionFilter) {
		ReportDef reportDef = resourceLikeBase(siteId, request, what, refRoot, actionFilter);
		reportDef.getReportParams().setHowTotalsBy(dateTotals(request));
		applyDateGrouping(reportDef.getReportParams(), request, true);
		reportDef.getReportParams().setHowChartType(StatsManager.CHARTTYPE_TIMESERIESBAR);
		reportDef.getReportParams().setHowChartSource(StatsManager.T_DATE);
		reportDef.getReportParams().setHowChartSeriesSource(StatsManager.T_NONE);
		return new WidgetReportDefinition(title, reportDef, reportDef);
	}

	private WidgetReportDefinition resourceLikeByUserDefinition(String siteId, SiteStatsReportRequest request,
			String widgetId, String title, String what, String refRoot, String actionFilter) {
		ReportDef chart = resourceLikeBase(siteId, request, what, refRoot, actionFilter);
		chart.getReportParams().setHowTotalsBy(dateTotals(request, StatsManager.T_USER));
		applyDateGrouping(chart.getReportParams(), request, false);
		chart.getReportParams().setHowChartType(StatsManager.CHARTTYPE_PIE);
		chart.getReportParams().setHowChartSource(StatsManager.T_USER);
		ReportDef table = new ReportDef(chart, siteId);
		List<String> tableTotals = new ArrayList<String>();
		tableTotals.add(StatsManager.T_USER);
		if (WIDGET_LESSONS.equals(widgetId)) {
			tableTotals.add(StatsManager.T_PAGE);
		}
		table.getReportParams().setHowTotalsBy(tableTotals);
		table.getReportParams().setHowSortBy(StatsManager.T_TOTAL);
		return new WidgetReportDefinition(title, chart, table);
	}

	private WidgetReportDefinition resourceLikeByItemDefinition(String siteId, SiteStatsReportRequest request,
			String title, String what, String refRoot, String actionFilter, String itemColumn) {
		ReportDef chart = resourceLikeBase(siteId, request, what, refRoot, actionFilter);
		chart.getReportParams().setHowTotalsBy(dateTotals(request, itemColumn));
		applyDateGrouping(chart.getReportParams(), request, false);
		chart.getReportParams().setHowChartType(StatsManager.CHARTTYPE_PIE);
		chart.getReportParams().setHowChartSource(itemColumn);
		ReportDef table = new ReportDef(chart, siteId);
		table.getReportParams().setHowTotalsBy(Arrays.asList(itemColumn));
		table.getReportParams().setHowSortBy(StatsManager.T_TOTAL);
		return new WidgetReportDefinition(title, chart, table);
	}

	private ReportDef resourceLikeBase(String siteId, SiteStatsReportRequest request, String what, String refRoot, String actionFilter) {
		ReportDef reportDef = baseReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(what);
		params.setWhatLimitedResourceIds(true);
		params.setWhatResourceIds(Arrays.asList(refRoot));
		if (actionFilter != null) {
			params.setWhatLimitedAction(true);
			params.setWhatResourceAction(actionFilter);
		}
		applyRoleFilter(params, request);
		return reportDef;
	}

	private ReportDef baseReportDef(String siteId) {
		ReportDef reportDef = new ReportDef();
		reportDef.setId(0);
		reportDef.setSiteId(siteId);
		ReportParams params = new ReportParams(siteId);
		params.setWhen(ReportManager.WHEN_LAST7DAYS);
		params.setWho(ReportManager.WHO_ALL);
		params.setHowPresentationMode(ReportManager.HOW_PRESENTATION_BOTH);
		params.setHowSort(true);
		params.setHowSortAscending(false);
		reportDef.setReportParams(params);
		return reportDef;
	}

	private void applyRoleFilter(ReportParams params, SiteStatsReportRequest request) {
		params.setWhen(dateFilter(request));
		String role = roleFilter(request);
		if (!ReportManager.WHO_ALL.equals(role)) {
			params.setWho(ReportManager.WHO_ROLE);
			params.setWhoRoleId(role);
		}
	}

	private void applyDateGrouping(ReportParams params, SiteStatsReportRequest request, boolean sortByDate) {
		params.setWhen(dateFilter(request));
		if (dateFilter(request).equals(ReportManager.WHEN_LAST365DAYS) || dateFilter(request).equals(ReportManager.WHEN_ALL)) {
			params.setHowSortBy(sortByDate ? StatsManager.T_DATEMONTH : params.getHowSortBy());
			params.setHowChartSeriesPeriod(StatsManager.CHARTTIMESERIES_MONTH);
		} else if (dateFilter(request).equals(ReportManager.WHEN_LAST30DAYS)) {
			params.setHowSortBy(sortByDate ? StatsManager.T_DATE : params.getHowSortBy());
			params.setHowChartSeriesPeriod(StatsManager.CHARTTIMESERIES_DAY);
		} else {
			params.setHowSortBy(sortByDate ? StatsManager.T_DATE : params.getHowSortBy());
			params.setHowChartSeriesPeriod(StatsManager.CHARTTIMESERIES_WEEKDAY);
		}
	}

	private List<String> dateTotals(SiteStatsReportRequest request, String... extraColumns) {
		List<String> totalsBy = new ArrayList<String>();
		if (dateFilter(request).equals(ReportManager.WHEN_LAST365DAYS) || dateFilter(request).equals(ReportManager.WHEN_ALL)) {
			totalsBy.add(StatsManager.T_DATEMONTH);
		} else {
			totalsBy.add(StatsManager.T_DATE);
		}
		totalsBy.addAll(Arrays.asList(extraColumns));
		return totalsBy;
	}

	private String dateFilter(SiteStatsReportRequest request) {
		String date = StringUtils.trimToNull(SiteStatsReportRequests.orDefault(request).getDate());
		return DATE_FILTERS.contains(date) ? date : ReportManager.WHEN_LAST7DAYS;
	}

	private String roleFilter(SiteStatsReportRequest request) {
		return StringUtils.defaultIfBlank(SiteStatsReportRequests.orDefault(request).getRole(), ReportManager.WHO_ALL);
	}

	private String toolFilter(SiteStatsReportRequest request) {
		return StringUtils.defaultIfBlank(SiteStatsReportRequests.orDefault(request).getTool(), ReportManager.WHAT_EVENTS_ALLTOOLS);
	}

	private String resourceActionFilter(SiteStatsReportRequest request) {
		return StringUtils.trimToNull(SiteStatsReportRequests.orDefault(request).getResourceAction());
	}

	private String lessonActionFilter(SiteStatsReportRequest request) {
		return StringUtils.trimToNull(SiteStatsReportRequests.orDefault(request).getLessonAction());
	}

	private SiteStatsWidget widget(String id, String title, String icon, String audience, SiteStatsWidgetTab... tabs) {
		SiteStatsWidget widget = new SiteStatsWidget();
		widget.setId(id);
		widget.setTitle(title);
		widget.setIcon(icon);
		widget.setAudience(audience);
		widget.setTabs(new ArrayList<SiteStatsWidgetTab>(Arrays.asList(tabs)));
		return widget;
	}

	private SiteStatsWidgetTab tab(String id, String title, List<SiteStatsFilter> filters) {
		SiteStatsWidgetTab tab = new SiteStatsWidgetTab();
		tab.setId(id);
		tab.setTitle(title);
		tab.setFilters(filters);
		return tab;
	}

	private List<SiteStatsFilter> filters(String siteId, String... ids) {
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

	private String filterLabel(String id) {
		if (FILTER_DATE.equals(id)) {
			return message("report_when_period");
		}
		if (FILTER_ROLE.equals(id)) {
			return message("report_who_role");
		}
		if (FILTER_TOOL.equals(id)) {
			return message("report_option_tool");
		}
		if (FILTER_RESOURCE_ACTION.equals(id)) {
			return message("report_option_resourceaction");
		}
		if (FILTER_LESSON_ACTION.equals(id)) {
			return message("th_action");
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
		options.add(option(ReportManager.WHEN_ALL, message("overview_filter_date_all")));
		options.add(option(ReportManager.WHEN_LAST365DAYS, message("report_when_last365days")));
		options.add(option(ReportManager.WHEN_LAST30DAYS, message("report_when_last30days")));
		options.add(option(ReportManager.WHEN_LAST7DAYS, message("report_when_last7days")));
		return options;
	}

	private List<SiteStatsFilterOption> roleFilterOptions(String siteId) {
		List<SiteStatsFilterOption> options = new ArrayList<SiteStatsFilterOption>();
		options.add(option(ReportManager.WHO_ALL, message("overview_filter_role_all")));
		try {
			Site site = siteService.getSite(siteId);
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
		options.add(option(ReportManager.WHAT_EVENTS_ALLTOOLS, message("overview_filter_tool_all")));
		PrefsData prefsData = statsManager.getPreferences(siteId, false);
		for (String toolId : siteStatsToolEventsService.getToolIds(siteId, prefsData)) {
			options.add(option(toolId, toolName(toolId)));
		}
		return options;
	}

	private String toolName(String toolId) {
		if (eventRegistryService == null) {
			return toolId;
		}
		return StringUtils.defaultIfBlank(eventRegistryService.getToolName(toolId), toolId);
	}

	private List<SiteStatsFilterOption> resourceActionFilterOptions() {
		List<SiteStatsFilterOption> options = new ArrayList<SiteStatsFilterOption>();
		options.add(option("", message("overview_filter_resaction_all")));
		options.add(option(ReportManager.WHAT_RESOURCES_ACTION_NEW, message("action_new")));
		options.add(option(ReportManager.WHAT_RESOURCES_ACTION_READ, message("action_read")));
		options.add(option(ReportManager.WHAT_RESOURCES_ACTION_REVS, message("action_revise")));
		options.add(option(ReportManager.WHAT_RESOURCES_ACTION_DEL, message("action_delete")));
		options.add(option(ReportManager.WHAT_RESOURCES_ACTION_DOW, message("action_zipdownload")));
		return options;
	}

	private List<SiteStatsFilterOption> lessonActionFilterOptions() {
		List<SiteStatsFilterOption> options = new ArrayList<SiteStatsFilterOption>();
		options.add(option("", message("overview_filter_resaction_all")));
		options.add(option(ReportManager.WHAT_LESSONS_ACTION_CREATE, message("action_create")));
		options.add(option(ReportManager.WHAT_LESSONS_ACTION_READ, message("action_read")));
		options.add(option(ReportManager.WHAT_LESSONS_ACTION_DELETE, message("action_delete")));
		options.add(option(ReportManager.WHAT_LESSONS_ACTION_UPDATE, message("action_update")));
		return options;
	}

	private SiteStatsFilterOption option(String value, String label) {
		return new SiteStatsFilterOption(value, label);
	}

	private String message(String key) {
		try {
			return messages.getString(key);
		} catch (Exception e) {
			return key;
		}
	}

	private interface WidgetReportFactory {
		WidgetReportDefinition build(String siteId, SiteStatsReportRequest request);
	}
}
