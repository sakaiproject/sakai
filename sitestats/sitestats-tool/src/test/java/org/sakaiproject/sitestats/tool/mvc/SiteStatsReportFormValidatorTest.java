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

        assertEquals("report_err_totalsbyevent", SiteStatsReportFormValidator.validateForm(form));
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

        assertEquals("report_err_totalsbyresource", SiteStatsReportFormValidator.validateForm(form));
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
