/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.api.report;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.junit.Test;
import org.sakaiproject.sitestats.api.StatsManager;

public class ReportConfigurationRulesTest {

    @Test
    public void exposesImmutableTotalRules() {
        Set<String> reportTypes = ReportConfigurationRules.allowedReportTypesForTotal(StatsManager.T_TOOL);

        assertTrue(reportTypes.contains(ReportManager.WHAT_EVENTS));
        assertThrows(UnsupportedOperationException.class,
                () -> reportTypes.add(ReportManager.WHAT_VISITS));
        assertThrows(UnsupportedOperationException.class,
                () -> ReportConfigurationRules.supportedTotals().add("unknown"));
    }

    @Test
    public void validatesTotalsFromTheCanonicalMatrix() {
        assertTrue(ReportConfigurationRules.isTotalAllowed(
                ReportManager.WHAT_EVENTS, StatsManager.T_TOOL));
        assertFalse(ReportConfigurationRules.isTotalAllowed(
                ReportManager.WHAT_VISITS, StatsManager.T_TOOL));
    }

    @Test
    public void validatesSortSourcesAgainstSelectedTotals() {
        assertTrue(ReportConfigurationRules.isSortSourceAllowed(
                Collections.singletonList(StatsManager.T_USER), StatsManager.T_TOTAL));
        assertTrue(ReportConfigurationRules.isSortSourceAllowed(
                Collections.singletonList(StatsManager.T_USER), StatsManager.T_USER));
        assertFalse(ReportConfigurationRules.isSortSourceAllowed(
                Collections.singletonList(StatsManager.T_USER), StatsManager.T_TOOL));
    }

    @Test
    public void validatesChartOptionsFromTheSameRulesExposedToTheEditor() {
        assertTrue(ReportConfigurationRules.allowedChartTypesForSource(StatsManager.T_DATE)
                .contains(StatsManager.CHARTTYPE_TIMESERIES));
        assertTrue(ReportConfigurationRules.isChartConfigurationAllowed(
                StatsManager.CHARTTYPE_TIMESERIES, StatsManager.T_DATE,
                StatsManager.T_NONE, StatsManager.T_USER, StatsManager.CHARTTIMESERIES_DAY,
                Arrays.asList(StatsManager.T_DATE, StatsManager.T_USER)));
        assertFalse(ReportConfigurationRules.isChartConfigurationAllowed(
                StatsManager.CHARTTYPE_TIMESERIES, StatsManager.T_USER,
                StatsManager.T_NONE, StatsManager.T_USER, StatsManager.CHARTTIMESERIES_DAY,
                Arrays.asList(StatsManager.T_DATE, StatsManager.T_USER)));
    }
}
