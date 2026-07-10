package org.sakaiproject.sitestats.tool.mvc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.view.SiteStatsReportAccessService;
import org.sakaiproject.sitestats.tool.mvc.SiteStatsToolService.ReportForm;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SiteStatsToolTestConfiguration.class)
@WebAppConfiguration("src/webapp")
public class SiteStatsToolServiceTest {

    @Autowired private SiteStatsToolService service;
    @Autowired private SiteStatsReportAccessService reportAccessService;
    @Autowired private ToolManager toolManager;

    @Before
    public void setup() {
        reset(reportAccessService, toolManager);
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
    public void reportValidationRequiresResourcesWhenLimited() {
        ReportForm form = validForm();
        form.setWhat(ReportManager.WHAT_RESOURCES);
        form.setWhatLimitedResourceIds(true);
        form.setWhatResourceIds("  \n");
        assertEquals("report_err_noresources", service.validateReport(form));
    }

    @Test
    public void reportValidationRejectsResourceTotalsForActivity() {
        ReportForm form = validForm();
        form.setWhat(ReportManager.WHAT_EVENTS);
        form.setHowTotalsBy(Collections.singletonList(StatsManager.T_RESOURCE));
        assertEquals("report_err_totalsbyevent", service.validateReport(form));
    }

    @Test
    public void reportValidationRejectsEventTotalsForResources() {
        ReportForm form = validForm();
        form.setWhat(ReportManager.WHAT_RESOURCES);
        form.setHowTotalsBy(Collections.singletonList(StatsManager.T_EVENT));
        assertEquals("report_err_totalsbyresource", service.validateReport(form));
    }

    @Test
    public void reportRoutesUseCentralViewAllAuthorization() {
        Placement placement = mock(Placement.class);
        when(toolManager.getCurrentPlacement()).thenReturn(placement);
        when(placement.getContext()).thenReturn("site-1");
        doThrow(new SecurityException("Not authorized"))
                .when(reportAccessService).assertCanViewAll("site-1");

        assertThrows(SecurityException.class, () -> service.reportSite("site-1"));
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
