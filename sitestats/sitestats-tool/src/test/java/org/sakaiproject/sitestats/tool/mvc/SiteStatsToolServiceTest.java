package org.sakaiproject.sitestats.tool.mvc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
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
import org.sakaiproject.sitestats.api.view.SiteStatsApiUrls;
import org.sakaiproject.sitestats.api.view.SiteStatsOverview;
import org.sakaiproject.sitestats.api.view.SiteStatsReportAccessService;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsViewService;
import org.sakaiproject.sitestats.api.view.SiteStatsWidget;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetTab;
import org.sakaiproject.sitestats.tool.mvc.SiteStatsToolService.ActivityDefinitionTool;
import org.sakaiproject.sitestats.tool.mvc.SiteStatsToolService.ActivityEvent;
import org.sakaiproject.sitestats.tool.mvc.SiteStatsToolService.OverviewResult;
import org.sakaiproject.sitestats.tool.mvc.SiteStatsToolService.PreferencesResult;
import org.sakaiproject.sitestats.tool.mvc.SiteStatsToolService.UserActivityForm;
import org.sakaiproject.sitestats.tool.mvc.SiteStatsToolService.UserActivityResult;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.sakaiproject.util.api.LocaleService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
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
    @Autowired private SiteStatsViewService siteStatsViewService;
    @Autowired private SiteService siteService;
    @Autowired private SiteStatsToolEventsService siteStatsToolEventsService;
    @Autowired private StatsManager statsManager;
    @Autowired private ToolManager toolManager;
    @Autowired private UserTimeService userTimeService;
    @Autowired private UserDirectoryService userDirectoryService;

    @Before
    public void setup() {
        reset(detailedEventsManager, eventRegistryService, localeService, reportAccessService, siteStatsViewService,
                siteService, siteStatsToolEventsService, statsManager, toolManager, userDirectoryService,
                userTimeService);
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
    public void preferencesBuildsFormFromPersistedSettings() {
        Placement placement = mock(Placement.class);
        when(toolManager.getCurrentPlacement()).thenReturn(placement);
        when(placement.getContext()).thenReturn("site-1");
        PrefsData preferences = mock(PrefsData.class);
        when(preferences.isListToolEventsOnlyAvailableInSite()).thenReturn(true);
        when(preferences.isShowOwnStatisticsToStudents()).thenReturn(true);
        when(preferences.isUseAllTools()).thenReturn(false);
        when(preferences.isItemLabelsVisible()).thenReturn(true);
        when(preferences.getChartTransparency()).thenReturn(0.7f);
        when(preferences.getToolEventsStringList()).thenReturn(Arrays.asList("event.one", "event.two"));
        when(preferences.getToolEventsDef()).thenReturn(Collections.emptyList());
        when(statsManager.getPreferences("site-1", true)).thenReturn(preferences);

        PreferencesResult result = service.preferences("site-1");

        assertEquals("site-1", result.getSiteId());
        assertTrue(result.getForm().isListToolEventsOnlyAvailableInSite());
        assertTrue(result.getForm().isShowOwnStatisticsToStudents());
        assertFalse(result.getForm().isUseAllTools());
        assertTrue(result.getForm().isItemLabelsVisible());
        assertEquals(0.7f, result.getForm().getChartTransparency(), 0.0f);
        assertEquals(Arrays.asList("event.one", "event.two"), result.getForm().getSelectedEventIds());
        assertEquals(Collections.emptyList(), result.getTools());
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

    @Test
    public void reportUserSearchIsBoundedAndReturnsOnlyActiveSiteMembers() throws Exception {
        Placement placement = mock(Placement.class);
        when(toolManager.getCurrentPlacement()).thenReturn(placement);
        when(placement.getContext()).thenReturn("site-1");
        Site site = mock(Site.class);
        when(siteService.getSite("site-1")).thenReturn(site);
        User memberUser = mock(User.class);
        User otherUser = mock(User.class);
        when(memberUser.getId()).thenReturn("member-1");
        when(otherUser.getId()).thenReturn("other-1");
        when(userDirectoryService.getUserByEid("ali")).thenReturn(memberUser);
        when(userDirectoryService.searchUsers("ali", 1, 50)).thenReturn(Arrays.asList(memberUser, otherUser));
        Member member = mock(Member.class);
        when(member.isActive()).thenReturn(true);
        when(site.getMember("member-1")).thenReturn(member);
        when(statsManager.getUserNameForDisplay(memberUser)).thenReturn("Alice Member");

        List<SiteStatsToolService.NamedOption> result = service.searchReportUsers("site-1", " ali ");

        assertEquals(1, result.size());
        assertEquals("member-1", result.get(0).getId());
        assertEquals("Alice Member", result.get(0).getLabel());
        verify(userDirectoryService).getUserByEid("ali");
        verify(userDirectoryService).searchUsers("ali", 1, 50);
        verify(site, never()).getUsers();
    }

    @Test
    public void overviewBuildsEndpointsForVisibleWidgetTabs() {
        Placement placement = mock(Placement.class);
        when(toolManager.getCurrentPlacement()).thenReturn(placement);
        when(placement.getContext()).thenReturn("site-1");
        SiteStatsWidgetTab visibleTab = new SiteStatsWidgetTab();
        visibleTab.setId("bydate");
        SiteStatsWidget visibleWidget = new SiteStatsWidget();
        visibleWidget.setId("visits");
        visibleWidget.setTabs(Collections.singletonList(visibleTab));
        SiteStatsWidgetTab hiddenTab = new SiteStatsWidgetTab();
        hiddenTab.setId("bytool");
        SiteStatsWidget hiddenWidget = new SiteStatsWidget();
        hiddenWidget.setId("activity");
        hiddenWidget.setVisible(false);
        hiddenWidget.setTabs(Collections.singletonList(hiddenTab));
        SiteStatsOverview overview = new SiteStatsOverview();
        overview.setSiteId("site-1");
        overview.setWidgets(Arrays.asList(visibleWidget, hiddenWidget));
        when(siteStatsViewService.getOverview("site-1")).thenReturn(overview);
        SiteStatsReportRequest reportRequest = new SiteStatsReportRequest();
        reportRequest.setIncludeTable(true);
        reportRequest.setIncludeChart(true);

        OverviewResult result = service.overviewWithEndpoints("site-1");

        assertEquals(overview, result.getOverview());
        assertEquals(1, result.getWidgetEndpoints().size());
        assertEquals(SiteStatsApiUrls.widgetReport("site-1", "visits", "bydate", reportRequest),
                result.getWidgetEndpoints().get("visits:bydate"));
        assertNull(result.getWidgetEndpoints().get("activity:bytool"));
    }

}
