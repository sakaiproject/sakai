/*
 * Copyright (c) 2003-2025 The Apereo Foundation
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
package org.sakaiproject.webapi.controllers.test;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.sakaiproject.calendar.api.CalendarConstants;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarEventVector;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.calendar.api.RecurrenceRule;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.webapi.controllers.CalendarController;

import static org.mockito.Mockito.*;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { WebApiTestConfiguration.class })
public class CalendarControllerTests extends BaseControllerTests {

    private MockMvc mockMvc;

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Mock
    private ContentHostingService contentHostingService;

    @Mock
    private CalendarService calendarService;

    @Mock
    private EntityManager entityManager;

    @Mock
    private PortalService portalService;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private SiteService siteService;

    @Mock
    private UserDirectoryService userDirectoryService;

    private AutoCloseable mocks;

    @Before
	public void setup() {

        mocks = MockitoAnnotations.openMocks(this);

        reset(calendarService);

        var controller = new CalendarController();

        controller.setUserDirectoryService(userDirectoryService);
        controller.setCalendarService(calendarService);
        controller.setPortalService(portalService);
        controller.setEntityManager(entityManager);

        var session = mock(Session.class);
        when(session.getUserId()).thenReturn("user1");
        when(sessionManager.getCurrentSession()).thenReturn(session);
        controller.setSessionManager(sessionManager);

        controller.setSiteService(siteService);
        controller.setContentHostingService(contentHostingService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .apply(documentationConfiguration(this.restDocumentation))
            .build();
	}

    @After
    public void tearDown() throws Exception {

        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    public void testGetUsersCurrentCalendar() throws Exception {

        when(portalService.getPinnedSites()).thenReturn(List.of("site1", "site2"));

        when(calendarService.calendarReference("site1", "main")).thenReturn("calendar1");
        when(calendarService.calendarReference("site2", "main")).thenReturn("calendar2");

        CalendarEvent event1 = createEvent("site1", "Site 1", "Event 1", "Canapes and cocktails", 1000L, 40000L);
        CalendarEvent event2 = createEvent("site2", "Site 2", "Event 2", "Fruit punch", 3000L, 70000L);
        when(event2.getField(CalendarConstants.NEW_ASSIGNMENT_DUEDATE_CALENDAR_ASSIGNMENT_ID)).thenReturn("assignment1");

        var site1 = mock(Site.class);
        when(site1.getTitle()).thenReturn("Site 1");
        when(siteService.getOptionalSite("site1")).thenReturn(Optional.of(site1));

        var site2 = mock(Site.class);
        when(site2.getTitle()).thenReturn("Site 2");
        when(siteService.getOptionalSite("site2")).thenReturn(Optional.of(site2));

        var calendarEventVector = new CalendarEventVector(List.of(event1, event2).iterator());
        when(calendarService.getEvents(any(), any(), anyBoolean())).thenReturn(calendarEventVector);

        mockMvc.perform(get("/users/current/calendar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.events[0].title", is(event1.getDisplayName())))
            .andExpect(jsonPath("$.events[0].start", is((int) event1.getRange().firstTime().getTime())))
            .andExpect(jsonPath("$.events[0].duration", is((int) event1.getRange().duration())))
            .andExpect(jsonPath("$.events[0].siteId", is(event1.getSiteId())))
            .andExpect(jsonPath("$.events[0].siteTitle", is(event1.getSiteName())))
            .andExpect(jsonPath("$.events[0].viewText", is(event1.getDescription())))
            .andExpect(jsonPath("$.events[1].title", is(event2.getDisplayName())))
            .andExpect(jsonPath("$.events[1].start", is((int) event2.getRange().firstTime().getTime())))
            .andExpect(jsonPath("$.events[1].duration", is((int) event2.getRange().duration())))
            .andExpect(jsonPath("$.events[1].siteId", is(event2.getSiteId())))
            .andExpect(jsonPath("$.events[1].siteTitle", is(event2.getSiteName())))
            .andExpect(jsonPath("$.events[1].viewText", is(event2.getDescription())))
            .andExpect(jsonPath("$.events[1].tool", is("assignments")))
            .andExpect(jsonPath("$.sites[0].siteId", is("site1")))
            .andExpect(jsonPath("$.sites[0].title", is("Site 1")))
            .andExpect(jsonPath("$.sites[1].siteId", is("site2")))
            .andExpect(jsonPath("$.sites[1].title", is("Site 2")))
            .andDo(document("get-user-calendar", preprocessor));
    }

    @Test
    public void testGetSiteCalendar() throws Exception {

        var siteId = "site1";

        CalendarEvent event = createEvent(siteId, "Site 1", "Event 1", "Canapes and cocktails", 1000L, 40000L);

        when(event.getField(CalendarConstants.NEW_ASSIGNMENT_DUEDATE_CALENDAR_ASSIGNMENT_ID)).thenReturn("assignment1");

        when(calendarService.calendarReference(siteId, "main")).thenReturn("calendar1");

        var calendarEventVector = new CalendarEventVector(List.of(event).iterator());
        when(calendarService.getEvents(any(), any(), anyBoolean())).thenReturn(calendarEventVector);
        var attachment1Ref = mock(Reference.class);
        when(attachment1Ref.getId()).thenReturn("attachment1-ref-id");
        when(event.getAttachments()).thenReturn(List.of(attachment1Ref));

        var attachment1Resource = mock(ContentResource.class);
        when(attachment1Resource.getId()).thenReturn("attachment1-id");
        when(attachment1Resource.getContentType()).thenReturn("text/plain");
        when(attachment1Resource.getContentLength()).thenReturn(100L);
        when(attachment1Resource.getUrl()).thenReturn("attachment1-url");
        var props = mock(ResourceProperties.class);
        when(props.get(ResourceProperties.PROP_DISPLAY_NAME)).thenReturn("data");
        when(attachment1Resource.getProperties()).thenReturn(props);

        when(contentHostingService.getResource(any())).thenReturn(attachment1Resource);

        mockMvc.perform(get("/sites/" + siteId + "/calendar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.events[0].title", is(event.getDisplayName())))
            .andExpect(jsonPath("$.events[0].creator", is(event.getCreator())))
            .andExpect(jsonPath("$.events[0].start", is((int) event.getRange().firstTime().getTime())))
            .andExpect(jsonPath("$.events[0].duration", is((int) event.getRange().duration())))
            .andExpect(jsonPath("$.events[0].siteId", is(event.getSiteId())))
            .andExpect(jsonPath("$.events[0].siteTitle", is(event.getSiteName())))
            .andExpect(jsonPath("$.events[0].viewText", is(event.getDescription())))
            .andExpect(jsonPath("$.events[0].type", is(event.getType())))
            .andExpect(jsonPath("$.events[0].attachments[0].mimetype", is(attachment1Resource.getContentType())))
            .andExpect(jsonPath("$.events[0].attachments[0].name", is(attachment1Resource.getProperties().get(ResourceProperties.PROP_DISPLAY_NAME))))
            .andExpect(jsonPath("$.events[0].attachments[0].size", is((int) attachment1Resource.getContentLength())))
            .andExpect(jsonPath("$.events[0].attachments[0].url", is(attachment1Resource.getUrl())))
            .andExpect(jsonPath("$.events[0].recurrence.count", is(event.getRecurrenceRule().getCount())))
            .andExpect(jsonPath("$.events[0].recurrence.interval", is(event.getRecurrenceRule().getInterval())))
            .andDo(document("get-site-calendar", preprocessor));
    }

    private CalendarEvent createEvent(String siteId, String siteTitle, String title, String description, long start, long duration) {

        var time1 = mock(Time.class);
        when(time1.getTime()).thenReturn(start);
        var timeRange1 = mock(TimeRange.class);
        when(timeRange1.firstTime()).thenReturn(time1);
        when(timeRange1.duration()).thenReturn(duration);

        var event = mock(CalendarEvent.class);
        when(event.getId()).thenReturn(UUID.randomUUID().toString());
        when(event.getCreator()).thenReturn(UUID.randomUUID().toString());
        when(event.getDisplayName()).thenReturn(title);
        when(event.getDescription()).thenReturn(description);
        when(event.getSiteId()).thenReturn(siteId);
        when(event.getSiteName()).thenReturn(siteTitle);
        when(event.getRange()).thenReturn(timeRange1);
        when(event.getType()).thenReturn("mock-event");

        var recurrenceRule = mock(RecurrenceRule.class);
        when(recurrenceRule.getCount()).thenReturn(5);
        when(recurrenceRule.getInterval()).thenReturn(17);
        when(recurrenceRule.getFrequency()).thenReturn("DAILY");
        var untilTime = mock(Time.class);
        when(untilTime.getTime()).thenReturn(8000L);
        when(recurrenceRule.getUntil()).thenReturn(untilTime);
        when(event.getRecurrenceRule()).thenReturn(recurrenceRule);

        return event;
    }
}
