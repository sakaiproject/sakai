package org.sakaiproject.sitestats.tool.mvc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;

import org.junit.Test;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.tool.mvc.SiteStatsToolService.ReportForm;

public class SiteStatsReportFormValidatorTest {

    private final SiteStatsReportFormValidator validator = new SiteStatsReportFormValidator(
            mock(StatsManager.class), mock(SiteService.class));

    @Test
    public void requiresATitle() {
        assertEquals("sitestats_report_title_required", validator.validateForm(new ReportForm()));
    }

    @Test
    public void rejectsAnInvalidActivityTotal() {
        ReportForm form = validForm();
        form.setWhat(ReportManager.WHAT_EVENTS);
        form.setHowTotalsBy(Collections.singletonList(StatsManager.T_RESOURCE));

        assertEquals("report_err_totalsbyevent", validator.validateForm(form));
    }

    @Test
    public void requiresToolsForAToolEventReport() {
        ReportForm form = validForm();
        form.setWhat(ReportManager.WHAT_EVENTS);
        form.setWhatToolIds(Collections.emptyList());

        assertEquals("report_err_notools", validator.validateForm(form));
    }

    @Test
    public void rejectsReversedCustomDates() {
        ReportForm form = validForm();
        form.setWhenTo(LocalDate.of(2026, 7, 9));

        assertEquals("report_err_nocustomdates", validator.validateForm(form));
    }

    @Test
    public void requiresResourceIdentifiersForALimitedResourceReport() {
        ReportForm form = validForm();
        form.setWhat(ReportManager.WHAT_RESOURCES);
        form.setWhatLimitedResourceIds(true);
        form.setWhatResourceIds("  \n");

        assertEquals("report_err_noresources", validator.validateForm(form));
    }

    @Test
    public void rejectsAnInvalidResourceTotal() {
        ReportForm form = validForm();
        form.setWhat(ReportManager.WHAT_RESOURCES);
        form.setHowTotalsBy(Collections.singletonList(StatsManager.T_EVENT));

        assertEquals("report_err_totalsbyresource", validator.validateForm(form));
    }

    @Test
    public void acceptsAValidForm() {
        assertNull(validator.validateForm(validForm()));
    }

    @Test
    public void rejectsAGroupThatNoLongerExistsInTheSite() throws Exception {
        StatsManager statsManager = mock(StatsManager.class);
        SiteService siteService = mock(SiteService.class);
        Site site = mock(Site.class);
        when(siteService.getSite("site-id")).thenReturn(site);
        when(statsManager.getEnableSiteVisits()).thenReturn(true);
        when(statsManager.getVisitsInfoAvailable()).thenReturn(true);

        ReportForm form = validForm();
        form.setWho(ReportManager.WHO_GROUPS);
        form.setWhoGroupId("removed-group");

        assertEquals("report_err_nogroup",
                new SiteStatsReportFormValidator(statsManager, siteService).validateForSite("site-id", form));
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
