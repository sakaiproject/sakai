package org.sakaiproject.sitestats.tool.mvc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.event.SiteStatsToolEventsService;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.event.detailed.DetailedEvent;
import org.sakaiproject.sitestats.api.event.detailed.DetailedEventsManager;
import org.sakaiproject.sitestats.api.event.detailed.PagingParams;
import org.sakaiproject.sitestats.api.event.detailed.SortingParams;
import org.sakaiproject.sitestats.api.event.detailed.TrackingParams;
import org.sakaiproject.sitestats.api.view.SiteStatsReportAccessService;
import org.sakaiproject.sitestats.tool.mvc.SiteStatsToolService.ActivityDefinitionTool;
import org.sakaiproject.sitestats.tool.mvc.SiteStatsToolService.ActivityEvent;
import org.sakaiproject.sitestats.tool.mvc.SiteStatsToolService.ReportForm;
import org.sakaiproject.sitestats.tool.mvc.SiteStatsToolService.UserActivityForm;
import org.sakaiproject.sitestats.tool.mvc.SiteStatsToolService.UserActivityResult;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.sakaiproject.util.api.LocaleService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SiteStatsToolTestConfiguration.class)
@WebAppConfiguration("src/webapp")
public class SiteStatsToolServiceTest {

    @Autowired private SiteStatsToolService service;
    @Autowired private DetailedEventsManager detailedEventsManager;
    @Autowired private EventRegistryService eventRegistryService;
    @Autowired private LocaleService localeService;
    @Autowired private SiteStatsReportAccessService reportAccessService;
    @Autowired private SiteStatsToolEventsService siteStatsToolEventsService;
    @Autowired private StatsManager statsManager;
    @Autowired private ToolManager toolManager;
    @Autowired private UserTimeService userTimeService;

    @Before
    public void setup() {
        reset(detailedEventsManager, eventRegistryService, localeService, reportAccessService,
                siteStatsToolEventsService, statsManager, toolManager, userTimeService);
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

    @Test
    public void activityDefinitionUsesLocalizedToolAndEventNames() {
        ToolInfo tool = mock(ToolInfo.class);
        EventInfo event = mock(EventInfo.class);
        when(tool.getToolId()).thenReturn("sakai.assignments");
        when(tool.getEvents()).thenReturn(Collections.singletonList(event));
        when(event.getEventId()).thenReturn("asn.submit.submission");
        when(eventRegistryService.getToolName("sakai.assignments")).thenReturn("Assignments");
        when(eventRegistryService.getEventName("asn.submit.submission")).thenReturn("Assignment submitted");
        PrefsData preferences = mock(PrefsData.class);
        when(preferences.getToolEventsDef()).thenReturn(Collections.singletonList(tool));

        ActivityDefinitionTool result = service.activityDefinitionTools(preferences).get(0);

        assertEquals("sakai.assignments", result.getId());
        assertEquals("Assignments", result.getLabel());
        assertEquals("asn.submit.submission", result.getEvents().get(0).getId());
        assertEquals("Assignment submitted", result.getEvents().get(0).getLabel());
    }

    @Test
    public void userActivityUsesLocalizedLabelsAndUserTimeServiceTimestamp() {
        Placement placement = mock(Placement.class);
        when(toolManager.getCurrentPlacement()).thenReturn(placement);
        when(placement.getContext()).thenReturn("site-1");
        PrefsData preferences = mock(PrefsData.class);
        when(statsManager.getPreferences("site-1", false)).thenReturn(preferences);
        when(detailedEventsManager.getUsersForTracking("site-1")).thenReturn(Collections.emptyList());
        when(siteStatsToolEventsService.getToolIds("site-1", preferences))
                .thenReturn(Collections.singletonList("sakai.assignments"));
        when(siteStatsToolEventsService.getEventsForToolFilter(
                eq(ReportManager.WHAT_EVENTS_ALLTOOLS), eq("site-1"), eq(preferences), eq(true)))
                .thenReturn(Collections.emptyList());
        when(eventRegistryService.getToolName(ReportManager.WHAT_EVENTS_ALLTOOLS)).thenReturn("All tools");
        when(eventRegistryService.getToolName("sakai.assignments")).thenReturn("Assignments");
        when(eventRegistryService.getEventName("asn.submit.submission")).thenReturn("Assignment submitted");
        DetailedEvent event = mock(DetailedEvent.class);
        Instant timestamp = Instant.parse("2026-07-10T18:30:00Z");
        when(event.getId()).thenReturn(42L);
        when(event.getEventId()).thenReturn("asn.submit.submission");
        when(event.getEventRef()).thenReturn("/assignment/a/123");
        when(event.getEventDate()).thenReturn(Date.from(timestamp));
        when(detailedEventsManager.getDetailedEventsCount(any(TrackingParams.class))).thenReturn(1L);
        when(detailedEventsManager.getDetailedEvents(any(TrackingParams.class), any(PagingParams.class),
                any(SortingParams.class))).thenReturn(Collections.singletonList(event));
        TimeZone timeZone = TimeZone.getTimeZone("America/New_York");
        when(userTimeService.getLocalTimeZone()).thenReturn(timeZone);
        when(localeService.getLocaleForCurrentSiteAndUser()).thenReturn(Locale.US);
        when(userTimeService.shortLocalizedTimestamp(timestamp, timeZone, Locale.US))
                .thenReturn("Jul 10, 2026, 2:30 PM EDT");
        UserActivityForm form = new UserActivityForm();
        form.setUserId("user-1");

        UserActivityResult result = service.userActivity("site-1", form);
        ActivityEvent activityEvent = result.getEvents().get(0);

        assertEquals(Arrays.asList("All tools", "Assignments"), Arrays.asList(
                result.getTools().get(0).getLabel(), result.getTools().get(1).getLabel()));
        assertEquals("Assignment submitted", activityEvent.getLabel());
        assertEquals(timestamp.toString(), activityEvent.getTimestamp());
        assertEquals("Jul 10, 2026, 2:30 PM EDT", activityEvent.getDisplayTimestamp());
    }

    private ReportForm validForm() {
        ReportForm form = new ReportForm();
        form.setTitle("Report");
        return form;
    }
}
