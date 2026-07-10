package org.sakaiproject.sitestats.tool.mvc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;
import org.sakaiproject.sitestats.tool.mvc.SiteStatsToolService.ReportForm;

public class SiteStatsToolServiceTest {

    private SiteStatsToolService service;

    @Before
    public void setup() {
        service = new SiteStatsToolService(mock(SakaiFacade.class));
    }

    @Test
    public void reportValidationRequiresTitle() {
        assertEquals("sitestats_report_title_required", service.validateReport(new ReportForm()));
    }

    @Test
    public void reportValidationRequiresToolsForToolEventReport() {
        ReportForm form = validForm();
        form.setWhat(ReportManager.WHAT_EVENTS);
        form.setWhatToolIds(Collections.emptyList());
        assertEquals("report_err_notools", service.validateReport(form));
    }

    @Test
    public void reportValidationRejectsReversedCustomDates() {
        ReportForm form = validForm();
        form.setWhen(ReportManager.WHEN_CUSTOM);
        form.setWhenFrom(LocalDate.of(2026, 7, 11));
        form.setWhenTo(LocalDate.of(2026, 7, 10));
        assertEquals("report_err_nocustomdates", service.validateReport(form));
    }

    @Test
    public void reportValidationAcceptsDefaultReport() {
        assertNull(service.validateReport(validForm()));
    }

    private ReportForm validForm() {
        ReportForm form = new ReportForm();
        form.setTitle("Report");
        return form;
    }
}
