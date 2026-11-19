/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.api.report;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.sitestats.api.StatsManager;

/**
 * Defines the supported combinations for SiteStats report configuration.
 */
public final class ReportConfigurationRules {

    private static final Set<String> ALL_REPORT_TYPES = Set.of(
            ReportManager.WHAT_VISITS, ReportManager.WHAT_EVENTS,
            ReportManager.WHAT_RESOURCES, ReportManager.WHAT_PRESENCES);
    private static final Set<String> BASIC_CHART_TYPES = Set.of(
            StatsManager.CHARTTYPE_BAR, StatsManager.CHARTTYPE_PIE);
    private static final Set<String> TIME_SERIES_CHART_TYPES = Set.of(
            StatsManager.CHARTTYPE_TIMESERIES, StatsManager.CHARTTYPE_TIMESERIESBAR);
    private static final Set<String> ALL_CHART_TYPES = Set.of(
            StatsManager.CHARTTYPE_BAR, StatsManager.CHARTTYPE_PIE,
            StatsManager.CHARTTYPE_TIMESERIES, StatsManager.CHARTTYPE_TIMESERIESBAR);
    private static final List<String> SUPPORTED_TOTALS = List.of(
            StatsManager.T_USER, StatsManager.T_TOOL, StatsManager.T_EVENT,
            StatsManager.T_RESOURCE, StatsManager.T_RESOURCE_ACTION,
            StatsManager.T_DATE, StatsManager.T_LASTDATE);

    private static final Map<String, Set<String>> REPORT_TYPES_BY_TOTAL = Map.of(
            StatsManager.T_USER, ALL_REPORT_TYPES,
            StatsManager.T_TOOL, Set.of(ReportManager.WHAT_EVENTS),
            StatsManager.T_EVENT, Set.of(ReportManager.WHAT_VISITS, ReportManager.WHAT_EVENTS),
            StatsManager.T_RESOURCE, Set.of(ReportManager.WHAT_RESOURCES),
            StatsManager.T_RESOURCE_ACTION, Set.of(ReportManager.WHAT_RESOURCES),
            StatsManager.T_DATE, ALL_REPORT_TYPES,
            StatsManager.T_LASTDATE, ALL_REPORT_TYPES);
    private static final Map<String, Set<String>> CHART_TYPES_BY_SOURCE = Map.of(
            StatsManager.T_USER, BASIC_CHART_TYPES,
            StatsManager.T_TOOL, BASIC_CHART_TYPES,
            StatsManager.T_EVENT, BASIC_CHART_TYPES,
            StatsManager.T_RESOURCE, BASIC_CHART_TYPES,
            StatsManager.T_RESOURCE_ACTION, BASIC_CHART_TYPES,
            StatsManager.T_DATE, ALL_CHART_TYPES);
    private static final Map<String, Set<String>> CHART_TYPES_BY_CATEGORY = Map.of(
            StatsManager.T_NONE, Set.of(StatsManager.CHARTTYPE_BAR),
            StatsManager.T_USER, Set.of(StatsManager.CHARTTYPE_BAR),
            StatsManager.T_TOOL, Set.of(StatsManager.CHARTTYPE_BAR),
            StatsManager.T_EVENT, Set.of(StatsManager.CHARTTYPE_BAR),
            StatsManager.T_RESOURCE, Set.of(StatsManager.CHARTTYPE_BAR),
            StatsManager.T_RESOURCE_ACTION, Set.of(StatsManager.CHARTTYPE_BAR),
            StatsManager.T_DATE, Set.of(StatsManager.CHARTTYPE_BAR));
    private static final Map<String, Set<String>> CHART_TYPES_BY_SERIES = Map.of(
            StatsManager.T_TOTAL, TIME_SERIES_CHART_TYPES,
            StatsManager.T_USER, TIME_SERIES_CHART_TYPES,
            StatsManager.T_TOOL, TIME_SERIES_CHART_TYPES,
            StatsManager.T_EVENT, TIME_SERIES_CHART_TYPES,
            StatsManager.T_RESOURCE, TIME_SERIES_CHART_TYPES,
            StatsManager.T_RESOURCE_ACTION, TIME_SERIES_CHART_TYPES);
    private static final Set<String> EVENT_SELECTION_TYPES = Set.of(
            ReportManager.WHAT_EVENTS_BYTOOL, ReportManager.WHAT_EVENTS_BYEVENTS);
    private static final Set<String> RESOURCE_ACTIONS = Set.of(
            ReportManager.WHAT_RESOURCES_ACTION_NEW, ReportManager.WHAT_RESOURCES_ACTION_READ,
            ReportManager.WHAT_RESOURCES_ACTION_DOW, ReportManager.WHAT_RESOURCES_ACTION_REVS,
            ReportManager.WHAT_RESOURCES_ACTION_DEL);
    private static final Set<String> WHEN_TYPES = Set.of(
            ReportManager.WHEN_ALL, ReportManager.WHEN_LAST7DAYS,
            ReportManager.WHEN_LAST30DAYS, ReportManager.WHEN_LAST365DAYS,
            ReportManager.WHEN_CUSTOM);
    private static final Set<String> WHO_TYPES = Set.of(
            ReportManager.WHO_ALL, ReportManager.WHO_ROLE, ReportManager.WHO_GROUPS,
            ReportManager.WHO_CUSTOM, ReportManager.WHO_NONE);
    private static final Set<String> PRESENTATION_MODES = Set.of(
            ReportManager.HOW_PRESENTATION_TABLE, ReportManager.HOW_PRESENTATION_CHART,
            ReportManager.HOW_PRESENTATION_BOTH);
    private static final Set<String> CHART_SERIES_PERIODS = Set.of(
            StatsManager.CHARTTIMESERIES_DAY, StatsManager.CHARTTIMESERIES_WEEKDAY,
            StatsManager.CHARTTIMESERIES_MONTH, StatsManager.CHARTTIMESERIES_YEAR);

    private ReportConfigurationRules() {
    }

    public static boolean isEventSelectionTypeAllowed(String value) {
        return contains(EVENT_SELECTION_TYPES, value);
    }

    public static boolean isResourceActionAllowed(String value) {
        return contains(RESOURCE_ACTIONS, value);
    }

    public static boolean isWhenTypeAllowed(String value) {
        return contains(WHEN_TYPES, value);
    }

    public static boolean isWhoTypeAllowed(String value) {
        return contains(WHO_TYPES, value);
    }

    public static boolean isPresentationModeAllowed(String value) {
        return contains(PRESENTATION_MODES, value);
    }

    public static boolean isTotalAllowed(String reportType, String total) {
        return reportType != null && allowedReportTypesForTotal(total).contains(reportType);
    }

    public static Set<String> allowedReportTypesForTotal(String total) {
        return immutableValues(REPORT_TYPES_BY_TOTAL, total);
    }

    public static List<String> supportedTotals() {
        return SUPPORTED_TOTALS;
    }

    public static boolean isSortSourceAllowed(List<String> totals, String sortSource) {
        return ReportManager.HOW_SORT_DEFAULT.equals(sortSource)
                || StatsManager.T_TOTAL.equals(sortSource)
                || totals != null && totals.contains(sortSource);
    }

    public static boolean sortSourceRequiresSelectedTotal(String sortSource) {
        return !ReportManager.HOW_SORT_DEFAULT.equals(sortSource)
                && !StatsManager.T_TOTAL.equals(sortSource);
    }

    public static boolean isChartConfigurationAllowed(String chartType, String chartSource,
            String categorySource, String seriesSource, String seriesPeriod, List<String> totals) {
        if (!contains(ALL_CHART_TYPES, chartType)
                || !isChartOptionAllowed(CHART_TYPES_BY_SOURCE, chartType, chartSource, totals, null)) {
            return false;
        }
        if (StatsManager.CHARTTYPE_BAR.equals(chartType)) {
            return isChartOptionAllowed(CHART_TYPES_BY_CATEGORY, chartType, categorySource,
                    totals, StatsManager.T_NONE);
        }
        if (contains(TIME_SERIES_CHART_TYPES, chartType)) {
            return isChartOptionAllowed(CHART_TYPES_BY_SERIES, chartType, seriesSource,
                    totals, StatsManager.T_TOTAL)
                    && contains(CHART_SERIES_PERIODS, seriesPeriod);
        }
        return true;
    }

    public static Set<String> allowedChartTypesForSource(String value) {
        return immutableValues(CHART_TYPES_BY_SOURCE, value);
    }

    public static Set<String> allowedChartTypesForCategory(String value) {
        return immutableValues(CHART_TYPES_BY_CATEGORY, value);
    }

    public static Set<String> allowedChartTypesForSeries(String value) {
        return immutableValues(CHART_TYPES_BY_SERIES, value);
    }

    public static boolean chartSourceRequiresSelectedTotal(String value) {
        return CHART_TYPES_BY_SOURCE.containsKey(value);
    }

    public static boolean chartCategoryRequiresSelectedTotal(String value) {
        return !StatsManager.T_NONE.equals(value);
    }

    public static boolean chartSeriesRequiresSelectedTotal(String value) {
        return !StatsManager.T_TOTAL.equals(value);
    }

    public static String requiredTotalForChartType(String chartType) {
        return contains(TIME_SERIES_CHART_TYPES, chartType) ? StatsManager.T_DATE : "";
    }

    public static boolean chartTypeUsesCategory(String chartType) {
        return StatsManager.CHARTTYPE_BAR.equals(chartType);
    }

    public static boolean chartTypeUsesSeries(String chartType) {
        return contains(TIME_SERIES_CHART_TYPES, chartType);
    }

    private static boolean isChartOptionAllowed(Map<String, Set<String>> chartTypesByOption,
            String chartType, String option, List<String> totals, String optionWithoutTotal) {
        if (!immutableValues(chartTypesByOption, option).contains(chartType)) {
            return false;
        }
        return (optionWithoutTotal != null && optionWithoutTotal.equals(option))
                || (totals != null && totals.contains(option));
    }

    private static Set<String> immutableValues(Map<String, Set<String>> values, String key) {
        if (key == null) {
            return Collections.emptySet();
        }
        return values.getOrDefault(key, Collections.emptySet());
    }

    private static boolean contains(Set<String> values, String value) {
        return value != null && values.contains(value);
    }
}
