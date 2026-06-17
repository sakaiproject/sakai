/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.AUDIENCE_ALL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.AUDIENCE_OWN;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_ACTIVITY_EVENTS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_ACTIVITY_MOST_ACTIVE_TOOL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_ACTIVITY_MOST_ACTIVE_USER;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_LESSONS_MOST_READ_PAGE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_LESSONS_PAGES;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_LESSONS_READ_PAGES;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_LESSONS_USER_READ_MORE_PAGES;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_RESOURCES_FILES;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_RESOURCES_MOST_OPENED_FILE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_RESOURCES_OPENED_FILES;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_RESOURCES_USER_OPENED_MORE_FILES;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_VISITS_AVERAGE_PRESENCE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_VISITS_TOTAL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_VISITS_UNIQUE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_VISITS_USERS_WITHOUT_VISITS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_VISITS_USERS_WITH_VISITS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_PAGE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_RESOURCE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_TOOL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_USER;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_ACTIVITY;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_LESSONS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_RESOURCES;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_STUDENT_VISITS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_VISITS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

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
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetMetric;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetTab;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class SiteStatsWidgetCatalog {

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
	private Map<String, WidgetSpec> widgetSpecs;
	private Map<String, WidgetTabSpec> tabSpecs;
	private Map<String, WidgetMetricSpec> metricSpecs;

	public SiteStatsOverview getOverview(String siteId, boolean allAllowed, boolean ownAllowed, boolean adminAllowed) {
		ensureRegistry();
		SiteStatsOverview overview = new SiteStatsOverview();
		overview.setSiteId(siteId);
		overview.setViewAllowed(true);
		overview.setAllAllowed(allAllowed);
		overview.setOwnAllowed(ownAllowed);
		overview.setAdminAllowed(adminAllowed);

		for (WidgetSpec spec : widgetSpecs.values()) {
			if (!spec.isAvailable() || !isAudienceAllowed(spec.audience, allAllowed, ownAllowed)) {
				continue;
			}
			overview.getWidgets().add(toWidget(siteId, spec));
		}
		return overview;
	}

	public SiteStatsWidgetTab getWidgetTab(String siteId, String widgetId, String tabId) {
		ensureRegistry();
		WidgetTabSpec spec = tabSpecs.get(key(widgetId, tabId));
		if (spec == null || !widgetAvailable(widgetId)) {
			throw new IllegalArgumentException("Unknown SiteStats widget tab: " + widgetId + "/" + tabId);
		}
		return toTab(siteId, spec);
	}

	public List<SiteStatsWidgetMetric> getWidgetMetrics(String siteId, String widgetId) {
		ensureRegistry();
		WidgetSpec spec = widgetSpecs.get(widgetId);
		if (spec == null || !spec.isAvailable()) {
			throw new IllegalArgumentException("Unknown SiteStats widget: " + widgetId);
		}
		return toMetrics(spec);
	}

	public SiteStatsWidgetMetric getWidgetMetric(String siteId, String widgetId, String metricId) {
		ensureRegistry();
		WidgetSpec spec = widgetSpecs.get(widgetId);
		WidgetMetricSpec metric = metricSpecs.get(key(widgetId, metricId));
		if (spec == null || metric == null || !spec.isAvailable()) {
			throw new IllegalArgumentException("Unknown SiteStats widget metric: " + widgetId + "/" + metricId);
		}
		return toMetric(spec, metric);
	}

	public boolean isOwnOnlyWidget(String widgetId) {
		ensureRegistry();
		WidgetSpec spec = widgetSpecs.get(widgetId);
		return spec != null && AUDIENCE_OWN.equals(spec.audience);
	}

	public boolean isOwnOnlyMetric(String widgetId, String metricId) {
		ensureRegistry();
		WidgetMetricSpec spec = metricSpecs.get(key(widgetId, metricId));
		return spec != null && AUDIENCE_OWN.equals(spec.audience);
	}

	public WidgetReportDefinition getWidgetReportDefinition(String siteId, String widgetId, String tabId,
			SiteStatsReportRequest request, String userId) {
		ensureRegistry();
		WidgetTabSpec spec = tabSpecs.get(key(widgetId, tabId));
		if (spec == null || spec.reportFactory == null || !widgetAvailable(widgetId)) {
			throw new IllegalArgumentException("Unknown SiteStats widget report: " + widgetId + "/" + tabId);
		}
		return spec.reportFactory.build(siteId, SiteStatsReportRequests.orDefault(request), userId);
	}

	public WidgetReportDefinition getWidgetMetricReportDefinition(String siteId, String widgetId, String metricId, String userId) {
		ensureRegistry();
		WidgetMetricSpec spec = metricSpecs.get(key(widgetId, metricId));
		if (spec == null || spec.reportFactory == null || !widgetAvailable(widgetId)) {
			throw new IllegalArgumentException("Unknown SiteStats widget metric report: " + widgetId + "/" + metricId);
		}
		return spec.reportFactory.build(siteId, new SiteStatsReportRequest(), userId);
	}

	private synchronized void ensureRegistry() {
		if (widgetSpecs != null) {
			return;
		}

		Map<String, WidgetSpec> widgets = new LinkedHashMap<String, WidgetSpec>();
		addWidget(widgets, widgetSpec(WIDGET_VISITS, "overview_title_visits", "sakai-singleuser", AUDIENCE_ALL, () -> true,
				tabs(
						tabSpec(WIDGET_VISITS, TAB_BY_DATE, "overview_tab_bydate", this::visitsByDateDefinition, FILTER_DATE, FILTER_ROLE),
						tabSpec(WIDGET_VISITS, TAB_BY_USER, "overview_tab_byuser", this::visitsByUserDefinition, FILTER_DATE, FILTER_ROLE)),
				metrics(
						metricSpec(WIDGET_VISITS, METRIC_VISITS_TOTAL, "overview_title_visits_sum", AUDIENCE_ALL, this::visitsTotalMetricDefinition),
						metricSpec(WIDGET_VISITS, METRIC_VISITS_UNIQUE, "overview_title_unique_visits_sum", AUDIENCE_ALL, this::visitsTotalMetricDefinition),
						metricSpec(WIDGET_VISITS, METRIC_VISITS_USERS_WITH_VISITS, "overview_title_enrolled_users_with_visits_sum", AUDIENCE_ALL, this::visitsUsersWithVisitsMetricDefinition),
						metricSpec(WIDGET_VISITS, METRIC_VISITS_USERS_WITHOUT_VISITS, "overview_title_enrolled_users_without_visits_sum", AUDIENCE_ALL, this::visitsUsersWithoutVisitsMetricDefinition),
						metricSpec(WIDGET_VISITS, METRIC_VISITS_AVERAGE_PRESENCE, "overview_title_presence_time_avg", AUDIENCE_ALL, this::visitsAveragePresenceMetricDefinition))));
		addWidget(widgets, widgetSpec(WIDGET_STUDENT_VISITS, "overview_title_visits", "sakai-singleuser", AUDIENCE_OWN, () -> true,
				tabs(tabSpec(WIDGET_STUDENT_VISITS, TAB_BY_DATE, "overview_tab_bydate", this::studentVisitsByDateDefinition, FILTER_DATE)),
				metrics()));
		addWidget(widgets, widgetSpec(WIDGET_ACTIVITY, "overview_title_activity", "sakai-poll", AUDIENCE_ALL, () -> true,
				tabs(
						tabSpec(WIDGET_ACTIVITY, TAB_BY_DATE, "overview_tab_bydate", this::activityByDateDefinition, FILTER_DATE, FILTER_ROLE, FILTER_TOOL),
						tabSpec(WIDGET_ACTIVITY, TAB_BY_USER, "overview_tab_byuser", this::activityByUserDefinition, FILTER_DATE, FILTER_ROLE, FILTER_TOOL),
						tabSpec(WIDGET_ACTIVITY, TAB_BY_TOOL, "overview_tab_bytool", this::activityByToolDefinition, FILTER_DATE, FILTER_ROLE, FILTER_TOOL)),
				metrics(
						metricSpec(WIDGET_ACTIVITY, METRIC_ACTIVITY_EVENTS, "overview_title_events_sum", AUDIENCE_ALL, this::activityEventsMetricDefinition),
						metricSpec(WIDGET_ACTIVITY, METRIC_ACTIVITY_MOST_ACTIVE_TOOL, "overview_title_mostactivetool_sum", AUDIENCE_ALL, this::activityMostActiveToolMetricDefinition),
						metricSpec(WIDGET_ACTIVITY, METRIC_ACTIVITY_MOST_ACTIVE_USER, "overview_title_mostactiveuser_sum", AUDIENCE_ALL, this::activityMostActiveUserMetricDefinition))));
		addWidget(widgets, widgetSpec(WIDGET_RESOURCES, "overview_title_resources", "sakai-resources", AUDIENCE_ALL,
				() -> statsManager.isEnableResourceStats(),
				tabs(
						tabSpec(WIDGET_RESOURCES, TAB_BY_DATE, "overview_tab_bydate", this::resourcesByDateDefinition, FILTER_DATE, FILTER_ROLE, FILTER_RESOURCE_ACTION),
						tabSpec(WIDGET_RESOURCES, TAB_BY_USER, "overview_tab_byuser", this::resourcesByUserDefinition, FILTER_DATE, FILTER_ROLE, FILTER_RESOURCE_ACTION),
						tabSpec(WIDGET_RESOURCES, TAB_BY_RESOURCE, "overview_tab_byresource", this::resourcesByResourceDefinition, FILTER_DATE, FILTER_ROLE, FILTER_RESOURCE_ACTION)),
				metrics(
						metricSpec(WIDGET_RESOURCES, METRIC_RESOURCES_FILES, "overview_title_resources_sum", AUDIENCE_ALL, this::resourcesFilesMetricDefinition),
						metricSpec(WIDGET_RESOURCES, METRIC_RESOURCES_OPENED_FILES, "overview_title_openedfiles_sum", AUDIENCE_ALL, this::resourcesOpenedFilesMetricDefinition),
						metricSpec(WIDGET_RESOURCES, METRIC_RESOURCES_MOST_OPENED_FILE, "overview_title_mostopenedfile_sum", AUDIENCE_ALL, this::resourcesOpenedFilesMetricDefinition),
						metricSpec(WIDGET_RESOURCES, METRIC_RESOURCES_USER_OPENED_MORE_FILES, "overview_title_useropenedmorefile_sum", AUDIENCE_ALL, this::resourcesUserOpenedMoreFilesMetricDefinition))));
		addWidget(widgets, widgetSpec(WIDGET_LESSONS, "overview_title_lessonpages", "sakai-lessonbuildertool", AUDIENCE_ALL,
				() -> statsManager.isEnableLessonsStats(),
				tabs(
						tabSpec(WIDGET_LESSONS, TAB_BY_DATE, "overview_tab_bydate", this::lessonsByDateDefinition, FILTER_DATE, FILTER_ROLE, FILTER_LESSON_ACTION),
						tabSpec(WIDGET_LESSONS, TAB_BY_USER, "overview_tab_byuser", this::lessonsByUserDefinition, FILTER_DATE, FILTER_ROLE, FILTER_LESSON_ACTION),
						tabSpec(WIDGET_LESSONS, TAB_BY_PAGE, "overview_tab_bypage", this::lessonsByPageDefinition, FILTER_DATE, FILTER_ROLE, FILTER_LESSON_ACTION)),
				metrics(
						metricSpec(WIDGET_LESSONS, METRIC_LESSONS_PAGES, "overview_title_pages_sum", AUDIENCE_ALL, null),
						metricSpec(WIDGET_LESSONS, METRIC_LESSONS_READ_PAGES, "overview_title_readpages_sum", AUDIENCE_ALL, null),
						metricSpec(WIDGET_LESSONS, METRIC_LESSONS_MOST_READ_PAGE, "overview_title_mostreadpage_sum", AUDIENCE_ALL, null),
						metricSpec(WIDGET_LESSONS, METRIC_LESSONS_USER_READ_MORE_PAGES, "overview_title_userreadmorepage_sum", AUDIENCE_ALL, null))));

		widgetSpecs = Collections.unmodifiableMap(widgets);
		Map<String, WidgetTabSpec> tabs = new LinkedHashMap<String, WidgetTabSpec>();
		Map<String, WidgetMetricSpec> metrics = new LinkedHashMap<String, WidgetMetricSpec>();
		for (WidgetSpec widget : widgetSpecs.values()) {
			for (WidgetTabSpec tab : widget.tabs) {
				tabs.put(key(tab.widgetId, tab.id), tab);
			}
			for (WidgetMetricSpec metric : widget.metrics) {
				metrics.put(key(metric.widgetId, metric.id), metric);
			}
		}
		tabSpecs = Collections.unmodifiableMap(tabs);
		metricSpecs = Collections.unmodifiableMap(metrics);
	}

	private boolean isAudienceAllowed(String audience, boolean allAllowed, boolean ownAllowed) {
		return (AUDIENCE_ALL.equals(audience) && allAllowed) || (AUDIENCE_OWN.equals(audience) && ownAllowed);
	}

	private boolean widgetAvailable(String widgetId) {
		WidgetSpec spec = widgetSpecs.get(widgetId);
		return spec != null && spec.isAvailable();
	}

	private void addWidget(Map<String, WidgetSpec> widgets, WidgetSpec spec) {
		widgets.put(spec.id, spec);
	}

	private WidgetSpec widgetSpec(String id, String titleKey, String icon, String audience, BooleanSupplier available,
			List<WidgetTabSpec> tabs, List<WidgetMetricSpec> metrics) {
		return new WidgetSpec(id, titleKey, icon, audience, available, tabs, metrics);
	}

	private List<WidgetTabSpec> tabs(WidgetTabSpec... tabs) {
		return Collections.unmodifiableList(Arrays.asList(tabs));
	}

	private List<WidgetMetricSpec> metrics(WidgetMetricSpec... metrics) {
		return Collections.unmodifiableList(Arrays.asList(metrics));
	}

	private WidgetTabSpec tabSpec(String widgetId, String id, String titleKey, WidgetReportFactory reportFactory, String... filterIds) {
		return new WidgetTabSpec(widgetId, id, titleKey, Arrays.asList(filterIds), reportFactory);
	}

	private WidgetMetricSpec metricSpec(String widgetId, String id, String labelKey, String audience, WidgetReportFactory reportFactory) {
		return new WidgetMetricSpec(widgetId, id, labelKey, audience, reportFactory);
	}

	private SiteStatsWidget toWidget(String siteId, WidgetSpec spec) {
		SiteStatsWidget widget = new SiteStatsWidget();
		widget.setId(spec.id);
		widget.setTitle(message(spec.titleKey));
		widget.setIcon(spec.icon);
		widget.setAudience(spec.audience);
		List<SiteStatsWidgetTab> tabs = new ArrayList<SiteStatsWidgetTab>();
		for (WidgetTabSpec tab : spec.tabs) {
			tabs.add(toTab(siteId, tab));
		}
		widget.setTabs(tabs);
		widget.setMetrics(toMetrics(spec));
		return widget;
	}

	private SiteStatsWidgetTab toTab(String siteId, WidgetTabSpec spec) {
		SiteStatsWidgetTab tab = new SiteStatsWidgetTab();
		tab.setId(spec.id);
		tab.setTitle(message(spec.titleKey));
		WidgetSpec widget = widgetSpecs.get(spec.widgetId);
		if (widget != null) {
			tab.setWidgetTitle(message(widget.titleKey));
		}
		tab.setFilters(filters(siteId, spec.filterIds));
		return tab;
	}

	private List<SiteStatsWidgetMetric> toMetrics(WidgetSpec spec) {
		List<SiteStatsWidgetMetric> metrics = new ArrayList<SiteStatsWidgetMetric>();
		for (WidgetMetricSpec metric : spec.metrics) {
			metrics.add(toMetric(spec, metric));
		}
		return metrics;
	}

	private SiteStatsWidgetMetric toMetric(WidgetSpec spec, WidgetMetricSpec metric) {
		SiteStatsWidgetMetric viewMetric = new SiteStatsWidgetMetric(metric.id, message(metric.labelKey), metric.audience, metric.reportFactory != null);
		viewMetric.setWidgetTitle(message(spec.titleKey));
		return viewMetric;
	}

	private String key(String widgetId, String id) {
		return widgetId + "/" + id;
	}

	private WidgetReportDefinition visitsByDateDefinition(String siteId, SiteStatsReportRequest request, String userId) {
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

	private WidgetReportDefinition studentVisitsByDateDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = baseReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_EVENTS);
		params.setWhatEventSelType(ReportManager.WHAT_EVENTS_BYEVENTS);
		params.setWhatEventIds(Arrays.asList(StatsManager.SITEVISIT_EVENTID));
		params.setWho(ReportManager.WHO_CUSTOM);
		params.setWhoUserIds(Arrays.asList(userId));
		applyDateGrouping(params, request, true);
		params.setHowTotalsBy(dateTotals(request));
		params.setHowChartType(StatsManager.CHARTTYPE_TIMESERIESBAR);
		params.setHowChartSource(StatsManager.T_DATE);
		params.setHowChartSeriesSource(StatsManager.T_NONE);
		return new WidgetReportDefinition(message("overview_title_visits"), reportDef, reportDef);
	}

	private WidgetReportDefinition visitsByUserDefinition(String siteId, SiteStatsReportRequest request, String userId) {
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

	private WidgetReportDefinition activityByDateDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = activityBase(siteId, request);
		reportDef.getReportParams().setHowTotalsBy(dateTotals(request));
		applyDateGrouping(reportDef.getReportParams(), request, true);
		reportDef.getReportParams().setHowChartType(StatsManager.CHARTTYPE_TIMESERIESBAR);
		reportDef.getReportParams().setHowChartSource(StatsManager.T_DATE);
		reportDef.getReportParams().setHowChartSeriesSource(StatsManager.T_NONE);
		return new WidgetReportDefinition(message("overview_title_activity"), reportDef, reportDef);
	}

	private WidgetReportDefinition activityByUserDefinition(String siteId, SiteStatsReportRequest request, String userId) {
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

	private WidgetReportDefinition activityByToolDefinition(String siteId, SiteStatsReportRequest request, String userId) {
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

	private WidgetReportDefinition resourcesByDateDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		return resourceLikeByDateDefinition(siteId, request, message("overview_title_resources"),
				ReportManager.WHAT_RESOURCES, StatsManager.RESOURCES_DIR + siteId + "/", resourceActionFilter(request));
	}

	private WidgetReportDefinition resourcesByUserDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		return resourceLikeByUserDefinition(siteId, request, WIDGET_RESOURCES, message("overview_title_resources"),
				ReportManager.WHAT_RESOURCES, StatsManager.RESOURCES_DIR + siteId + "/", resourceActionFilter(request));
	}

	private WidgetReportDefinition resourcesByResourceDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		return resourceLikeByItemDefinition(siteId, request, message("overview_title_resources"),
				ReportManager.WHAT_RESOURCES, StatsManager.RESOURCES_DIR + siteId + "/", resourceActionFilter(request), StatsManager.T_RESOURCE);
	}

	private WidgetReportDefinition lessonsByDateDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		return resourceLikeByDateDefinition(siteId, request, message("overview_title_lessonpages"),
				ReportManager.WHAT_LESSONPAGES, "/page/", lessonActionFilter(request));
	}

	private WidgetReportDefinition lessonsByUserDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		return resourceLikeByUserDefinition(siteId, request, WIDGET_LESSONS, message("overview_title_lessonpages"),
				ReportManager.WHAT_LESSONPAGES, "/page/", lessonActionFilter(request));
	}

	private WidgetReportDefinition lessonsByPageDefinition(String siteId, SiteStatsReportRequest request, String userId) {
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

	private WidgetReportDefinition visitsTotalMetricDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = baseMetricReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_VISITS_TOTALS);
		params.setWhen(ReportManager.WHEN_ALL);
		params.setWho(ReportManager.WHO_ALL);
		params.setHowTotalsBy(Arrays.asList(StatsManager.T_DATE, StatsManager.T_VISITS, StatsManager.T_UNIQUEVISITS));
		params.setHowSortBy(StatsManager.T_DATE);
		params.setHowSortAscending(false);
		params.setHowPresentationMode(ReportManager.HOW_PRESENTATION_BOTH);
		params.setHowChartType(StatsManager.CHARTTYPE_TIMESERIESBAR);
		params.setHowChartSource(StatsManager.T_DATE);
		params.setHowChartSeriesSource(StatsManager.T_NONE);
		params.setHowChartSeriesPeriod(StatsManager.CHARTTIMESERIES_DAY);
		return new WidgetReportDefinition(message("overview_title_visits"), reportDef, reportDef);
	}

	private WidgetReportDefinition visitsUsersWithVisitsMetricDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = baseMetricReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_VISITS);
		params.setWhen(ReportManager.WHEN_ALL);
		params.setWho(ReportManager.WHO_ALL);
		params.setHowTotalsBy(Arrays.asList(StatsManager.T_USER));
		params.setHowSort(false);
		params.setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
		return new WidgetReportDefinition(message("overview_title_enrolled_users_with_visits_sum"), null, reportDef);
	}

	private WidgetReportDefinition visitsUsersWithoutVisitsMetricDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = baseMetricReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_VISITS);
		params.setWhen(ReportManager.WHEN_ALL);
		params.setWho(ReportManager.WHO_NONE);
		params.setHowTotalsBy(Arrays.asList(StatsManager.T_USER));
		params.setHowSort(false);
		params.setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
		return new WidgetReportDefinition(message("overview_title_enrolled_users_without_visits_sum"), null, reportDef);
	}

	private WidgetReportDefinition visitsAveragePresenceMetricDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = baseMetricReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_PRESENCES);
		params.setWhen(ReportManager.WHEN_ALL);
		params.setWho(ReportManager.WHO_ALL);
		params.setHowTotalsBy(Arrays.asList(StatsManager.T_DATE, StatsManager.T_USER));
		params.setHowSortBy(StatsManager.T_DATE);
		params.setHowSortAscending(false);
		params.setHowPresentationMode(ReportManager.HOW_PRESENTATION_BOTH);
		params.setHowChartType(StatsManager.CHARTTYPE_TIMESERIESBAR);
		params.setHowChartSource(StatsManager.T_DATE);
		params.setHowChartSeriesSource(StatsManager.T_NONE);
		params.setHowChartSeriesPeriod(StatsManager.CHARTTIMESERIES_MONTH);
		return new WidgetReportDefinition(message("overview_title_presence_time_avg"), reportDef, reportDef);
	}

	private WidgetReportDefinition activityEventsMetricDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = activityMetricBase(siteId);
		reportDef.getReportParams().setHowTotalsBy(Arrays.asList(StatsManager.T_EVENT));
		reportDef.getReportParams().setHowSortBy(StatsManager.T_EVENT);
		reportDef.getReportParams().setHowSortAscending(true);
		reportDef.getReportParams().setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
		return new WidgetReportDefinition(message("overview_title_events_sum"), null, reportDef);
	}

	private WidgetReportDefinition activityMostActiveToolMetricDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = activityMetricBase(siteId);
		reportDef.getReportParams().setHowTotalsBy(Arrays.asList(StatsManager.T_TOOL));
		reportDef.getReportParams().setHowSortBy(StatsManager.T_TOOL);
		reportDef.getReportParams().setHowPresentationMode(ReportManager.HOW_PRESENTATION_BOTH);
		reportDef.getReportParams().setHowChartType(StatsManager.CHARTTYPE_PIE);
		reportDef.getReportParams().setHowChartSource(StatsManager.T_TOOL);
		return new WidgetReportDefinition(message("overview_title_mostactivetool_sum"), reportDef, reportDef);
	}

	private WidgetReportDefinition activityMostActiveUserMetricDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = activityMetricBase(siteId);
		reportDef.getReportParams().setHowTotalsBy(Arrays.asList(StatsManager.T_USER));
		reportDef.getReportParams().setHowSortBy(StatsManager.T_TOTAL);
		reportDef.getReportParams().setHowSortAscending(false);
		reportDef.getReportParams().setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
		return new WidgetReportDefinition(message("overview_title_mostactiveuser_sum"), null, reportDef);
	}

	private ReportDef activityMetricBase(String siteId) {
		ReportDef reportDef = baseMetricReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_EVENTS);
		params.setWhatEventSelType(ReportManager.WHAT_EVENTS_BYEVENTS);
		params.setWhatEventIds(statsManager.getPreferences(siteId, true).getToolEventsStringList());
		params.setWhen(ReportManager.WHEN_ALL);
		params.setWho(ReportManager.WHO_ALL);
		params.setHowSort(true);
		return reportDef;
	}

	private WidgetReportDefinition resourcesFilesMetricDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = resourceMetricBase(siteId, ReportManager.WHAT_RESOURCES_ACTION_NEW, StatsManager.T_RESOURCE);
		reportDef.getReportParams().setHowSortBy(StatsManager.T_RESOURCE);
		reportDef.getReportParams().setHowSortAscending(true);
		return new WidgetReportDefinition(message("overview_title_resources_sum"), null, reportDef);
	}

	private WidgetReportDefinition resourcesOpenedFilesMetricDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = resourceMetricBase(siteId, ReportManager.WHAT_RESOURCES_ACTION_READ, StatsManager.T_RESOURCE);
		reportDef.getReportParams().setHowSortBy(StatsManager.T_TOTAL);
		reportDef.getReportParams().setHowSortAscending(false);
		return new WidgetReportDefinition(message("overview_title_openedfiles_sum"), null, reportDef);
	}

	private WidgetReportDefinition resourcesUserOpenedMoreFilesMetricDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		ReportDef reportDef = resourceMetricBase(siteId, ReportManager.WHAT_RESOURCES_ACTION_READ, StatsManager.T_USER);
		reportDef.getReportParams().setHowSortBy(StatsManager.T_TOTAL);
		reportDef.getReportParams().setHowSortAscending(false);
		return new WidgetReportDefinition(message("overview_title_useropenedmorefile_sum"), null, reportDef);
	}

	private ReportDef resourceMetricBase(String siteId, String action, String totalsBy) {
		ReportDef reportDef = baseMetricReportDef(siteId);
		ReportParams params = reportDef.getReportParams();
		params.setWhat(ReportManager.WHAT_RESOURCES);
		params.setWhatLimitedAction(true);
		params.setWhatResourceAction(action);
		params.setWhatLimitedResourceIds(true);
		params.setWhatResourceIds(Arrays.asList(StatsManager.RESOURCES_DIR + siteId + "/"));
		params.setWhen(ReportManager.WHEN_ALL);
		params.setWho(ReportManager.WHO_ALL);
		params.setHowTotalsBy(Arrays.asList(totalsBy));
		params.setHowSort(true);
		params.setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
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

	private ReportDef baseMetricReportDef(String siteId) {
		ReportDef reportDef = baseReportDef(siteId);
		reportDef.getReportParams().setWhen(ReportManager.WHEN_ALL);
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
		String date = dateFilter(request);
		params.setWhen(date);
		if (date.equals(ReportManager.WHEN_LAST365DAYS) || date.equals(ReportManager.WHEN_ALL)) {
			params.setHowSortBy(sortByDate ? StatsManager.T_DATEMONTH : params.getHowSortBy());
			params.setHowChartSeriesPeriod(StatsManager.CHARTTIMESERIES_MONTH);
		} else if (date.equals(ReportManager.WHEN_LAST30DAYS)) {
			params.setHowSortBy(sortByDate ? StatsManager.T_DATE : params.getHowSortBy());
			params.setHowChartSeriesPeriod(StatsManager.CHARTTIMESERIES_DAY);
		} else {
			params.setHowSortBy(sortByDate ? StatsManager.T_DATE : params.getHowSortBy());
			params.setHowChartSeriesPeriod(StatsManager.CHARTTIMESERIES_WEEKDAY);
		}
	}

	private List<String> dateTotals(SiteStatsReportRequest request, String... extraColumns) {
		List<String> totalsBy = new ArrayList<String>();
		String date = dateFilter(request);
		if (date.equals(ReportManager.WHEN_LAST365DAYS) || date.equals(ReportManager.WHEN_ALL)) {
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

	private List<SiteStatsFilter> filters(String siteId, List<String> ids) {
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
		WidgetReportDefinition build(String siteId, SiteStatsReportRequest request, String userId);
	}

	private static class WidgetSpec {
		private final String id;
		private final String titleKey;
		private final String icon;
		private final String audience;
		private final BooleanSupplier available;
		private final List<WidgetTabSpec> tabs;
		private final List<WidgetMetricSpec> metrics;

		private WidgetSpec(String id, String titleKey, String icon, String audience, BooleanSupplier available,
				List<WidgetTabSpec> tabs, List<WidgetMetricSpec> metrics) {
			this.id = id;
			this.titleKey = titleKey;
			this.icon = icon;
			this.audience = audience;
			this.available = available;
			this.tabs = tabs;
			this.metrics = metrics;
		}

		private boolean isAvailable() {
			return available.getAsBoolean();
		}
	}

	private static class WidgetTabSpec {
		private final String widgetId;
		private final String id;
		private final String titleKey;
		private final List<String> filterIds;
		private final WidgetReportFactory reportFactory;

		private WidgetTabSpec(String widgetId, String id, String titleKey, List<String> filterIds, WidgetReportFactory reportFactory) {
			this.widgetId = widgetId;
			this.id = id;
			this.titleKey = titleKey;
			this.filterIds = Collections.unmodifiableList(new ArrayList<String>(filterIds));
			this.reportFactory = reportFactory;
		}
	}

	private static class WidgetMetricSpec {
		private final String widgetId;
		private final String id;
		private final String labelKey;
		private final String audience;
		private final WidgetReportFactory reportFactory;

		private WidgetMetricSpec(String widgetId, String id, String labelKey, String audience, WidgetReportFactory reportFactory) {
			this.widgetId = widgetId;
			this.id = id;
			this.labelKey = labelKey;
			this.audience = audience;
			this.reportFactory = reportFactory;
		}
	}
}
