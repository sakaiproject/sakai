/*
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.tool.mvc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;
import java.util.Collections;

import org.junit.Test;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.StatsManager;

public class SiteStatsReportFormValidatorTest {

    @Test
    public void requiresATitle() {
        assertEquals("sitestats_report_title_required",
                SiteStatsReportFormValidator.validateForm(new SiteStatsReportForm()));
    }

    @Test
    public void rejectsAnInvalidActivityTotal() {
        SiteStatsReportForm form = validForm();
        form.setWhat(ReportManager.WHAT_EVENTS);
        form.setHowTotalsBy(Collections.singletonList(StatsManager.T_RESOURCE));

        assertEquals("sitestats_report_totals_unavailable", SiteStatsReportFormValidator.validateForm(form));
    }

    @Test
    public void requiresToolsForAToolEventReport() {
        SiteStatsReportForm form = validForm();
        form.setWhat(ReportManager.WHAT_EVENTS);
        form.setWhatToolIds(Collections.emptyList());

        assertEquals("report_err_notools", SiteStatsReportFormValidator.validateForm(form));
    }

    @Test
    public void rejectsReversedCustomDates() {
        SiteStatsReportForm form = validForm();
        form.setWhenTo(LocalDate.of(2026, 7, 9));

        assertEquals("report_err_nocustomdates", SiteStatsReportFormValidator.validateForm(form));
    }

    @Test
    public void requiresResourceIdentifiersForALimitedResourceReport() {
        SiteStatsReportForm form = validForm();
        form.setWhat(ReportManager.WHAT_RESOURCES);
        form.setWhatLimitedResourceIds(true);
        form.setWhatResourceIds("  \n");

        assertEquals("report_err_noresources", SiteStatsReportFormValidator.validateForm(form));
    }

    @Test
    public void rejectsAnInvalidResourceTotal() {
        SiteStatsReportForm form = validForm();
        form.setWhat(ReportManager.WHAT_RESOURCES);
        form.setHowTotalsBy(Collections.singletonList(StatsManager.T_EVENT));

        assertEquals("sitestats_report_totals_unavailable", SiteStatsReportFormValidator.validateForm(form));
    }

    @Test
    public void rejectsAnInvalidVisitTotal() {
        SiteStatsReportForm form = validForm();
        form.setWhat(ReportManager.WHAT_VISITS);
        form.setHowTotalsBy(Collections.singletonList(StatsManager.T_RESOURCE));

        assertEquals("sitestats_report_totals_unavailable", SiteStatsReportFormValidator.validateForm(form));
    }

    @Test
    public void rejectsAnInvalidPresenceTotal() {
        SiteStatsReportForm form = validForm();
        form.setWhat(ReportManager.WHAT_PRESENCES);
        form.setHowTotalsBy(Collections.singletonList(StatsManager.T_TOOL));

        assertEquals("sitestats_report_totals_unavailable", SiteStatsReportFormValidator.validateForm(form));
    }

    @Test
    public void rejectsAnUnknownTotal() {
        SiteStatsReportForm form = validForm();
        form.setHowTotalsBy(Collections.singletonList("unknown"));

        assertEquals("sitestats_report_totals_unavailable", SiteStatsReportFormValidator.validateForm(form));
    }

    @Test
    public void rejectsAnUnknownEventSelectionType() {
        SiteStatsReportForm form = validForm();
        form.setWhat(ReportManager.WHAT_EVENTS);
        form.setWhatEventSelType("unknown");

        assertEquals("sitestats_report_configuration_invalid", SiteStatsReportFormValidator.validateForm(form));
    }

    @Test
    public void rejectsAnUnknownDateRangeType() {
        SiteStatsReportForm form = validForm();
        form.setWhen("unknown");

        assertEquals("sitestats_report_configuration_invalid", SiteStatsReportFormValidator.validateForm(form));
    }

    @Test
    public void rejectsAnUnknownAudienceType() {
        SiteStatsReportForm form = validForm();
        form.setWho("unknown");

        assertEquals("sitestats_report_configuration_invalid", SiteStatsReportFormValidator.validateForm(form));
    }

    @Test
    public void rejectsAnUnknownLimitedResourceAction() {
        SiteStatsReportForm form = validForm();
        form.setWhat(ReportManager.WHAT_RESOURCES);
        form.setWhatLimitedAction(true);
        form.setWhatResourceAction("unknown");
        form.setHowTotalsBy(Collections.singletonList(StatsManager.T_RESOURCE));

        assertEquals("sitestats_report_configuration_invalid", SiteStatsReportFormValidator.validateForm(form));
    }

    @Test
    public void rejectsAnUnknownChartType() {
        SiteStatsReportForm form = validForm();
        form.setHowPresentationMode(ReportManager.HOW_PRESENTATION_CHART);
        form.setHowChartType("unknown");

        assertEquals("sitestats_report_configuration_invalid", SiteStatsReportFormValidator.validateForm(form));
    }

    @Test
    public void rejectsAMissingPresentationMode() {
        SiteStatsReportForm form = validForm();
        form.setHowPresentationMode(null);

        assertEquals("sitestats_report_configuration_invalid", SiteStatsReportFormValidator.validateForm(form));
    }

    @Test
    public void rejectsAChartSourceThatIsNotATotal() {
        SiteStatsReportForm form = validForm();
        form.setHowPresentationMode(ReportManager.HOW_PRESENTATION_CHART);
        form.setHowChartSource(StatsManager.T_TOOL);

        assertEquals("sitestats_report_configuration_invalid", SiteStatsReportFormValidator.validateForm(form));
    }

    @Test
    public void rejectsATimeSeriesWithoutADateTotal() {
        SiteStatsReportForm form = validForm();
        form.setHowTotalsBy(Collections.singletonList(StatsManager.T_USER));
        form.setHowPresentationMode(ReportManager.HOW_PRESENTATION_CHART);
        form.setHowChartType(StatsManager.CHARTTYPE_TIMESERIES);
        form.setHowChartSource(StatsManager.T_USER);

        assertEquals("sitestats_report_configuration_invalid", SiteStatsReportFormValidator.validateForm(form));
    }

    @Test
    public void acceptsAValidTimeSeries() {
        SiteStatsReportForm form = validForm();
        form.setHowPresentationMode(ReportManager.HOW_PRESENTATION_CHART);
        form.setHowChartType(StatsManager.CHARTTYPE_TIMESERIES);
        form.setHowChartSource(StatsManager.T_DATE);

        assertNull(SiteStatsReportFormValidator.validateForm(form));
    }

    @Test
    public void acceptsAValidForm() {
        assertNull(SiteStatsReportFormValidator.validateForm(validForm()));
    }

    private SiteStatsReportForm validForm() {
        SiteStatsReportForm form = new SiteStatsReportForm();
        form.setTitle("Report");
        form.setWhen(ReportManager.WHEN_CUSTOM);
        form.setWhenFrom(LocalDate.of(2026, 7, 10));
        form.setWhenTo(LocalDate.of(2026, 7, 11));
        return form;
    }
}
