/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityNotFoundException;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.StatsAuthz;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.event.SiteStatsToolEventsService;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.api.view.SiteStatsChart;
import org.sakaiproject.sitestats.api.view.SiteStatsChartDataset;
import org.sakaiproject.sitestats.api.view.SiteStatsChartPoint;
import org.sakaiproject.sitestats.api.view.SiteStatsFilter;
import org.sakaiproject.sitestats.api.view.SiteStatsFilterOption;
import org.sakaiproject.sitestats.api.view.SiteStatsOverview;
import org.sakaiproject.sitestats.api.view.SiteStatsReportPreviewService;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsReportSummary;
import org.sakaiproject.sitestats.api.view.SiteStatsReportView;
import org.sakaiproject.sitestats.api.view.SiteStatsTable;
import org.sakaiproject.sitestats.api.view.SiteStatsTableCell;
import org.sakaiproject.sitestats.api.view.SiteStatsTableColumn;
import org.sakaiproject.sitestats.api.view.SiteStatsTableMapper;
import org.sakaiproject.sitestats.api.view.SiteStatsTableRow;
import org.sakaiproject.sitestats.api.view.SiteStatsViewService;
import org.sakaiproject.sitestats.api.view.SiteStatsWidget;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetTab;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class SiteStatsViewServiceImpl implements SiteStatsViewService {

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

	@Setter private StatsAuthz statsAuthz;
	@Setter private StatsManager statsManager;
	@Setter private ReportManager reportManager;
	@Setter private SiteStatsToolEventsService siteStatsToolEventsService;
	@Setter private SiteStatsTableMapper siteStatsTableMapper;
	@Setter private SiteStatsReportPreviewService siteStatsReportPreviewService;
	@Setter private EventRegistryService eventRegistryService;
	@Setter private SiteService siteService;

	private ResourceLoader messages = new ResourceLoader("Messages");

	@Override
	public SiteStatsOverview getOverview(String siteId) {
		assertCanView(siteId);

		boolean allAllowed = statsAuthz.isUserAbleToViewSiteStatsAll(siteId);
		boolean ownAllowed = statsAuthz.isUserAbleToViewSiteStatsOwn(siteId);
		SiteStatsOverview overview = new SiteStatsOverview();
		overview.setSiteId(siteId);
		overview.setViewAllowed(true);
		overview.setAllAllowed(allAllowed);
		overview.setOwnAllowed(ownAllowed);
		overview.setAdminAllowed(statsAuthz.isUserAbleToViewSiteStatsAdmin(siteId));

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

	@Override
	public List<SiteStatsReportSummary> getReports(String siteId) {
		assertCanViewAll(siteId);

		List<SiteStatsReportSummary> summaries = new ArrayList<SiteStatsReportSummary>();
		List<ReportDef> reportDefs = reportManager.getReportDefinitions(siteId, true, false);
		for (ReportDef reportDef : reportDefs) {
			SiteStatsReportSummary summary = new SiteStatsReportSummary();
			summary.setId(reportDef.getId());
			summary.setSiteId(siteId);
			summary.setTitle(localizedReportTitle(reportDef));
			summary.setDescription(localizedReportDescription(reportDef));
			summary.setHidden(reportDef.isHidden());
			summaries.add(summary);
		}
		return summaries;
	}

	@Override
	public SiteStatsReportView getReport(String siteId, long reportId, SiteStatsReportRequest request) {
		assertCanViewAll(siteId);

		if (reportId <= 0) {
			throw new IllegalArgumentException("Unknown report id: " + reportId);
		}

		ReportDef reportDef;
		try {
			reportDef = reportManager.getReportDefinition(reportId);
		} catch (EntityNotFoundException e) {
			throw new IllegalArgumentException("Unknown report id: " + reportId, e);
		}
		if (reportDef == null) {
			throw new IllegalArgumentException("Unknown report id: " + reportId);
		}

		ReportDef safeReportDef = new ReportDef(reportDef, siteId);
		PrefsData prefsData = statsManager.getPreferences(siteId, false);
		Report report = reportManager.getReport(safeReportDef, prefsData.isListToolEventsOnlyAvailableInSite(), null, true);

		SiteStatsReportView view = mapReportView(siteId, report, request, prefsData);
		view.setReportId(Long.valueOf(reportId));
		view.setTitle(localizedReportTitle(reportDef));
		return view;
	}

	@Override
	public SiteStatsReportView getPreviewReport(String siteId, String previewId, SiteStatsReportRequest request) {
		assertCanViewAll(siteId);

		ReportDef reportDef = siteStatsReportPreviewService == null ? null : siteStatsReportPreviewService.get(siteId, previewId);
		if (reportDef == null) {
			throw new IllegalArgumentException("Unknown report preview id: " + previewId);
		}

		ReportDef safeReportDef = new ReportDef(reportDef, siteId);
		PrefsData prefsData = statsManager.getPreferences(siteId, false);
		Report report = reportManager.getReport(safeReportDef, prefsData.isListToolEventsOnlyAvailableInSite(), null, true);

		SiteStatsReportView view = mapReportView(siteId, report, request, prefsData);
		view.setTitle(localizedReportTitle(safeReportDef));
		return view;
	}

	@Override
	public SiteStatsReportView getWidgetReport(String siteId, String widgetId, String tabId, SiteStatsReportRequest request) {
		assertCanView(siteId);
		if (WIDGET_STUDENT_VISITS.equals(widgetId)) {
			if (!statsAuthz.isUserAbleToViewSiteStatsOwn(siteId)) {
				throw new SecurityException("Current user cannot view own SiteStats data for site " + siteId);
			}
		} else {
			assertCanViewAll(siteId);
		}

		WidgetReportDefinition definition = buildWidgetReportDefinition(siteId, widgetId, tabId, request);
		PrefsData prefsData = statsManager.getPreferences(siteId, false);
		boolean restrictToToolsInSite = prefsData.isListToolEventsOnlyAvailableInSite();

		Report chartReport = null;
		Report tableReport = null;
		if (requestOrDefault(request).isIncludeChart() && definition.getChartReportDef() != null) {
			chartReport = reportManager.getReport(definition.getChartReportDef(), restrictToToolsInSite, null, false);
		}
		if (requestOrDefault(request).isIncludeTable() && definition.getTableReportDef() != null) {
			tableReport = reportManager.getReport(definition.getTableReportDef(), restrictToToolsInSite, null, false);
		}

		Report baseReport = tableReport != null ? tableReport : chartReport;
		if (baseReport == null) {
			throw new IllegalArgumentException("Unknown SiteStats widget report: " + widgetId + "/" + tabId);
		}

		SiteStatsReportView view = mapReportView(siteId, baseReport, request, prefsData);
		view.setWidgetId(widgetId);
		view.setTabId(tabId);
		view.setTitle(definition.getTitle());
		if (chartReport != null) {
			view.setChart(mapChart(chartReport, prefsData));
		}
		if (tableReport != null) {
			view.setTable(mapTable(tableReport, requestOrDefault(request)));
		}
		return view;
	}

	private SiteStatsReportView mapReportView(String siteId, Report report, SiteStatsReportRequest request, PrefsData prefsData) {
		SiteStatsReportRequest safeRequest = requestOrDefault(request);
		SiteStatsReportView view = new SiteStatsReportView();
		view.setSiteId(siteId);
		view.setPresentationMode(report.getReportDefinition().getReportParams().getHowPresentationMode());
		if (report.getReportGenerationDate() != null) {
			view.setGeneratedOn(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, currentLocale()).format(report.getReportGenerationDate()));
		}
		if (safeRequest.isIncludeTable()) {
			view.setTable(mapTable(report, safeRequest));
		}
		if (safeRequest.isIncludeChart()) {
			view.setChart(mapChart(report, prefsData));
		}
		return view;
	}

	private SiteStatsTable mapTable(Report report, SiteStatsReportRequest request) {
		ReportParams params = report.getReportDefinition().getReportParams();
		List<SiteStatsTableColumn> columns = siteStatsTableMapper.getColumns(params, false);
		List<Stat> reportData = report.getReportData() == null ? Collections.<Stat>emptyList() : report.getReportData();

		SiteStatsTable table = new SiteStatsTable();
		table.setCaption(localizedReportTitle(report.getReportDefinition()));
		table.setPage(request.getPage());
		table.setPageSize(request.getPageSize());
		table.setTotalRows(reportData.size());
		table.setColumns(columns);

		int first = (request.getPage() - 1) * request.getPageSize();
		int last = Math.min(first + request.getPageSize(), reportData.size());
		if (first > last) {
			first = last;
		}

		for (Stat stat : reportData.subList(first, last)) {
			SiteStatsTableRow row = new SiteStatsTableRow();
			Map<String, SiteStatsTableCell> cells = new LinkedHashMap<String, SiteStatsTableCell>();
			for (SiteStatsTableColumn column : columns) {
				cells.put(column.getKey(), siteStatsTableMapper.getCell(stat, column.getKey()));
			}
			row.setCells(cells);
			table.getRows().add(row);
		}
		return table;
	}

	private SiteStatsChart mapChart(Report report, PrefsData prefsData) {
		ReportParams params = report.getReportDefinition().getReportParams();
		PrefsData safePrefsData = prefsData == null ? new PrefsData() : prefsData;
		SiteStatsChart chart = new SiteStatsChart();
		chart.setTitle(localizedReportTitle(report.getReportDefinition()));
		chart.setType(params.getHowChartType());
		String chartSource = chartSourceKey(params);
		chart.setXKey(chartSource);
		chart.setYKey(StatsManager.T_TOTAL);
		chart.setEmptyMessage(message("no_data"));
		chart.setThreeDimensional(safePrefsData.isChartIn3D());
		chart.setTransparency(safePrefsData.getChartTransparency());
		chart.setItemLabelsVisible(safePrefsData.isItemLabelsVisible());

		List<Stat> reportData = report.getReportData() == null ? Collections.<Stat>emptyList() : report.getReportData();
		if (reportData.isEmpty()) {
			return chart;
		}

		List<String> valueKeys = chartValueKeys(report);
		for (String valueKey : valueKeys) {
			SiteStatsChartDataset dataset = new SiteStatsChartDataset();
			dataset.setKey(valueKey);
			dataset.setLabel(siteStatsTableMapper.getColumn(valueKey, false).getLabel());
			for (Stat stat : reportData) {
				SiteStatsChartPoint point = new SiteStatsChartPoint();
				SiteStatsTableCell sourceCell = siteStatsTableMapper.getCell(stat, chartSource);
				point.setX(sourceCell.getRaw());
				point.setLabel(sourceCell.getDisplay());
				point.setY(siteStatsTableMapper.getNumericValue(stat, valueKey));
				dataset.getPoints().add(point);
			}
			chart.getDatasets().add(dataset);
		}
		return chart;
	}

	private String chartSourceKey(ReportParams params) {
		if (StatsManager.T_DATE.equals(params.getHowChartSource())) {
			if (params.getHowTotalsBy().contains(StatsManager.T_DATEMONTH)) {
				return StatsManager.T_DATEMONTH;
			}
			if (params.getHowTotalsBy().contains(StatsManager.T_DATEYEAR)) {
				return StatsManager.T_DATEYEAR;
			}
		}
		return params.getHowChartSource();
	}

	private List<String> chartValueKeys(Report report) {
		ReportParams params = report.getReportDefinition().getReportParams();
		if (ReportManager.WHAT_VISITS_TOTALS.equals(params.getWhat())
				|| params.getHowTotalsBy().contains(StatsManager.T_VISITS)
				|| params.getHowTotalsBy().contains(StatsManager.T_UNIQUEVISITS)) {
			List<String> values = new ArrayList<String>();
			values.add(StatsManager.T_VISITS);
			values.add(StatsManager.T_UNIQUEVISITS);
			return values;
		}
		if (ReportManager.WHAT_PRESENCES.equals(params.getWhat()) || params.getHowTotalsBy().contains(StatsManager.T_DURATION)) {
			return Arrays.asList(StatsManager.T_DURATION);
		}
		return Arrays.asList(StatsManager.T_TOTAL);
	}

	private WidgetReportDefinition buildWidgetReportDefinition(String siteId, String widgetId, String tabId, SiteStatsReportRequest request) {
		SiteStatsReportRequest safeRequest = requestOrDefault(request);
		if (WIDGET_VISITS.equals(widgetId)) {
			return visitsDefinition(siteId, tabId, safeRequest);
		}
		if (WIDGET_ACTIVITY.equals(widgetId)) {
			return activityDefinition(siteId, tabId, safeRequest);
		}
		if (WIDGET_RESOURCES.equals(widgetId)) {
			return resourcesDefinition(siteId, tabId, safeRequest);
		}
		if (WIDGET_LESSONS.equals(widgetId)) {
			return lessonsDefinition(siteId, tabId, safeRequest);
		}
		throw new IllegalArgumentException("Unknown SiteStats widget: " + widgetId);
	}

	private WidgetReportDefinition visitsDefinition(String siteId, String tabId, SiteStatsReportRequest request) {
		if (TAB_BY_DATE.equals(tabId)) {
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
		if (TAB_BY_USER.equals(tabId)) {
			ReportDef chart = visitsByUserChart(siteId, request);
			ReportDef table = new ReportDef(chart, siteId);
			table.getReportParams().setHowTotalsBy(Arrays.asList(StatsManager.T_USER));
			table.getReportParams().setHowSortBy(StatsManager.T_TOTAL);
			return new WidgetReportDefinition(message("overview_title_visits"), chart, table);
		}
		throw new IllegalArgumentException("Unknown visits widget tab: " + tabId);
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

	private WidgetReportDefinition activityDefinition(String siteId, String tabId, SiteStatsReportRequest request) {
		if (TAB_BY_DATE.equals(tabId)) {
			ReportDef reportDef = activityBase(siteId, request);
			reportDef.getReportParams().setHowTotalsBy(dateTotals(request));
			applyDateGrouping(reportDef.getReportParams(), request, true);
			reportDef.getReportParams().setHowChartType(StatsManager.CHARTTYPE_TIMESERIESBAR);
			reportDef.getReportParams().setHowChartSource(StatsManager.T_DATE);
			reportDef.getReportParams().setHowChartSeriesSource(StatsManager.T_NONE);
			return new WidgetReportDefinition(message("overview_title_activity"), reportDef, reportDef);
		}
		if (TAB_BY_USER.equals(tabId)) {
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
		if (TAB_BY_TOOL.equals(tabId)) {
			ReportDef chart = activityBase(siteId, request);
			chart.getReportParams().setHowTotalsBy(Arrays.asList(StatsManager.T_TOOL));
			chart.getReportParams().setHowSortBy(StatsManager.T_DATE);
			chart.getReportParams().setHowChartType(StatsManager.CHARTTYPE_PIE);
			chart.getReportParams().setHowChartSource(StatsManager.T_TOOL);
			ReportDef table = new ReportDef(chart, siteId);
			table.getReportParams().setHowSortBy(StatsManager.T_TOTAL);
			return new WidgetReportDefinition(message("overview_title_activity"), chart, table);
		}
		throw new IllegalArgumentException("Unknown activity widget tab: " + tabId);
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

	private WidgetReportDefinition resourcesDefinition(String siteId, String tabId, SiteStatsReportRequest request) {
		return resourceLikeDefinition(siteId, tabId, request, WIDGET_RESOURCES, message("overview_title_resources"),
				ReportManager.WHAT_RESOURCES, StatsManager.RESOURCES_DIR + siteId + "/", resourceActionFilter(request),
				StatsManager.T_RESOURCE, TAB_BY_RESOURCE);
	}

	private WidgetReportDefinition lessonsDefinition(String siteId, String tabId, SiteStatsReportRequest request) {
		return resourceLikeDefinition(siteId, tabId, request, WIDGET_LESSONS, message("overview_title_lessonpages"),
				ReportManager.WHAT_LESSONPAGES, "/page/", lessonActionFilter(request), StatsManager.T_PAGE, TAB_BY_PAGE);
	}

	private WidgetReportDefinition resourceLikeDefinition(String siteId, String tabId, SiteStatsReportRequest request, String widgetId,
			String title, String what, String refRoot, String actionFilter, String itemColumn, String itemTabId) {
		if (TAB_BY_DATE.equals(tabId)) {
			ReportDef reportDef = resourceLikeBase(siteId, request, what, refRoot, actionFilter);
			reportDef.getReportParams().setHowTotalsBy(dateTotals(request));
			applyDateGrouping(reportDef.getReportParams(), request, true);
			reportDef.getReportParams().setHowChartType(StatsManager.CHARTTYPE_TIMESERIESBAR);
			reportDef.getReportParams().setHowChartSource(StatsManager.T_DATE);
			reportDef.getReportParams().setHowChartSeriesSource(StatsManager.T_NONE);
			return new WidgetReportDefinition(title, reportDef, reportDef);
		}
		if (TAB_BY_USER.equals(tabId)) {
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
		if (itemTabId.equals(tabId)) {
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
		throw new IllegalArgumentException("Unknown " + widgetId + " widget tab: " + tabId);
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
		String date = StringUtils.trimToNull(requestOrDefault(request).getDate());
		return DATE_FILTERS.contains(date) ? date : ReportManager.WHEN_LAST7DAYS;
	}

	private String roleFilter(SiteStatsReportRequest request) {
		return StringUtils.defaultIfBlank(requestOrDefault(request).getRole(), ReportManager.WHO_ALL);
	}

	private String toolFilter(SiteStatsReportRequest request) {
		return StringUtils.defaultIfBlank(requestOrDefault(request).getTool(), ReportManager.WHAT_EVENTS_ALLTOOLS);
	}

	private String resourceActionFilter(SiteStatsReportRequest request) {
		return StringUtils.trimToNull(requestOrDefault(request).getResourceAction());
	}

	private String lessonActionFilter(SiteStatsReportRequest request) {
		return StringUtils.trimToNull(requestOrDefault(request).getLessonAction());
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

	private String localizedReportTitle(ReportDef reportDef) {
		if (reportDef.getTitle() == null) {
			return message("reportres_title");
		}
		if (reportDef.isTitleLocalized()) {
			return message(reportDef.getTitleBundleKey());
		}
		return reportDef.getTitle();
	}

	private String localizedReportDescription(ReportDef reportDef) {
		if (reportDef.getDescription() == null) {
			return null;
		}
		if (reportDef.isDescriptionLocalized()) {
			return message(reportDef.getDescriptionBundleKey());
		}
		return reportDef.getDescription();
	}

	private SiteStatsReportRequest requestOrDefault(SiteStatsReportRequest request) {
		SiteStatsReportRequest safeRequest = request == null ? new SiteStatsReportRequest() : request;
		if (safeRequest.getPageSize() > 500) {
			safeRequest.setPageSize(500);
		}
		return safeRequest;
	}

	private Locale currentLocale() {
		return messages.getLocale();
	}

	private String message(String key) {
		try {
			return messages.getString(key);
		} catch (Exception e) {
			return key;
		}
	}

	private void assertCanView(String siteId) {
		if (!statsAuthz.isUserAbleToViewSiteStats(siteId)) {
			throw new SecurityException("Current user cannot view SiteStats for site " + siteId);
		}
	}

	private void assertCanViewAll(String siteId) {
		if (!statsAuthz.isUserAbleToViewSiteStatsAll(siteId)) {
			throw new SecurityException("Current user cannot view all SiteStats data for site " + siteId);
		}
	}

	private static class WidgetReportDefinition {
		private final String title;
		private final ReportDef chartReportDef;
		private final ReportDef tableReportDef;

		private WidgetReportDefinition(String title, ReportDef chartReportDef, ReportDef tableReportDef) {
			this.title = title;
			this.chartReportDef = chartReportDef;
			this.tableReportDef = tableReportDef;
		}

		private String getTitle() {
			return title;
		}

		private ReportDef getChartReportDef() {
			return chartReportDef;
		}

		private ReportDef getTableReportDef() {
			return tableReportDef;
		}
	}
}
