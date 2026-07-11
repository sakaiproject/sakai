package org.sakaiproject.sitestats.tool.mvc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;
import java.util.Collections;

import org.junit.Test;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.tool.mvc.SiteStatsToolService.ReportForm;

public class SiteStatsReportFormValidatorTest {

    private final SiteStatsReportFormValidator validator = new SiteStatsReportFormValidator();

    @Test
    public void requiresATitle() {
        assertEquals("sitestats_report_title_required", validator.validate(new ReportForm()));
    }

    @Test
    public void rejectsAnInvalidActivityTotal() {
        ReportForm form = validForm();
        form.setWhat(ReportManager.WHAT_EVENTS);
        form.setHowTotalsBy(Collections.singletonList(StatsManager.T_RESOURCE));

        assertEquals("report_err_totalsbyevent", validator.validate(form));
    }

    @Test
    public void requiresToolsForAToolEventReport() {
        ReportForm form = validForm();
        form.setWhat(ReportManager.WHAT_EVENTS);
        form.setWhatToolIds(Collections.emptyList());

        assertEquals("report_err_notools", validator.validate(form));
    }

    @Test
    public void rejectsReversedCustomDates() {
        ReportForm form = validForm();
        form.setWhenTo(LocalDate.of(2026, 7, 9));

        assertEquals("report_err_nocustomdates", validator.validate(form));
    }

    @Test
    public void requiresResourceIdentifiersForALimitedResourceReport() {
        ReportForm form = validForm();
        form.setWhat(ReportManager.WHAT_RESOURCES);
        form.setWhatLimitedResourceIds(true);
        form.setWhatResourceIds("  \n");

        assertEquals("report_err_noresources", validator.validate(form));
    }

    @Test
    public void rejectsAnInvalidResourceTotal() {
        ReportForm form = validForm();
        form.setWhat(ReportManager.WHAT_RESOURCES);
        form.setHowTotalsBy(Collections.singletonList(StatsManager.T_EVENT));

        assertEquals("report_err_totalsbyresource", validator.validate(form));
    }

    @Test
    public void acceptsAValidForm() {
        assertNull(validator.validate(validForm()));
    }

    private ReportForm validForm() {
        ReportForm form = new ReportForm();
        form.setTitle("Report");
        form.setWhen(ReportManager.WHEN_CUSTOM);
        form.setWhenFrom(LocalDate.of(2026, 7, 10));
        form.setWhenTo(LocalDate.of(2026, 7, 11));
        return form;
    }
}
