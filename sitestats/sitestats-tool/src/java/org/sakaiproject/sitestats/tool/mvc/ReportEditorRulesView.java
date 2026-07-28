/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.tool.mvc;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.ReportConfigurationRules;

/**
 * Read-only projection of report configuration rules for form option metadata.
 */
public final class ReportEditorRulesView {

    private static final Map<String, String> TOTAL_LABELS = Map.of(
            StatsManager.T_USER, "report_option_user",
            StatsManager.T_TOOL, "report_option_tool",
            StatsManager.T_EVENT, "report_option_event",
            StatsManager.T_RESOURCE, "report_option_resource",
            StatsManager.T_RESOURCE_ACTION, "report_option_resourceaction",
            StatsManager.T_DATE, "report_option_date",
            StatsManager.T_LASTDATE, "report_option_lastdate");
    private static final List<TotalOption> TOTAL_OPTIONS = ReportConfigurationRules.supportedTotals().stream()
            .map(value -> new TotalOption(value, TOTAL_LABELS.get(value),
                    csv(ReportConfigurationRules.allowedReportTypesForTotal(value))))
            .collect(Collectors.toUnmodifiableList());

    public List<TotalOption> getTotalOptions() {
        return TOTAL_OPTIONS;
    }

    public boolean sortRequiresSelectedTotal(String value) {
        return ReportConfigurationRules.sortSourceRequiresSelectedTotal(value);
    }

    public String chartTypesForSource(String value) {
        return csv(ReportConfigurationRules.allowedChartTypesForSource(value));
    }

    public String chartTypesForCategory(String value) {
        return csv(ReportConfigurationRules.allowedChartTypesForCategory(value));
    }

    public String chartTypesForSeries(String value) {
        return csv(ReportConfigurationRules.allowedChartTypesForSeries(value));
    }

    public boolean chartSourceRequiresSelectedTotal(String value) {
        return ReportConfigurationRules.chartSourceRequiresSelectedTotal(value);
    }

    public boolean chartCategoryRequiresSelectedTotal(String value) {
        return ReportConfigurationRules.chartCategoryRequiresSelectedTotal(value);
    }

    public boolean chartSeriesRequiresSelectedTotal(String value) {
        return ReportConfigurationRules.chartSeriesRequiresSelectedTotal(value);
    }

    public String requiredTotalForChartType(String value) {
        return ReportConfigurationRules.requiredTotalForChartType(value);
    }

    public boolean chartTypeUsesCategory(String value) {
        return ReportConfigurationRules.chartTypeUsesCategory(value);
    }

    public boolean chartTypeUsesSeries(String value) {
        return ReportConfigurationRules.chartTypeUsesSeries(value);
    }

    private static String csv(Set<String> values) {
        return String.join(",", values);
    }

    @Getter
    @RequiredArgsConstructor
    public static final class TotalOption {
        private final String value;
        private final String labelKey;
        private final String allowedReportTypes;
    }
}
