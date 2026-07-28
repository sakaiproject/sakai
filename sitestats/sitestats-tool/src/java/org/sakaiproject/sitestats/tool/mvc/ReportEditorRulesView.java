/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.tool.mvc;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.sakaiproject.sitestats.api.report.ReportConfigurationRules;

/**
 * Read-only projection of report configuration rules for form option metadata.
 */
public final class ReportEditorRulesView {

    private static final List<TotalOption> TOTAL_OPTIONS = ReportConfigurationRules.supportedTotals().stream()
            .map(value -> new TotalOption(value, labelKey(value),
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

    private static String labelKey(String value) {
        return "report_option_" + value.replace("-", "");
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
