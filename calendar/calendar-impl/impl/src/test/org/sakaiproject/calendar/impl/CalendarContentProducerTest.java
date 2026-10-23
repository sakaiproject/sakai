/**********************************************************************************
 * Copyright (c) 2026 The Apereo Foundation
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
 *
 **********************************************************************************/

package org.sakaiproject.calendar.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.search.model.SearchBuilderItem;

public class CalendarContentProducerTest {

    private static final String CALENDAR_REF = "/calendar/calendar/site1/main";
    private static final String EVENT_REF = "/calendar/event/site1/main/event1";

    private final CalendarService calendarService = mock(CalendarService.class);
    private final Calendar calendar = mock(Calendar.class);
    private final CalendarEvent event = mock(CalendarEvent.class);
    private CalendarContentProducer producer;

    @Before
    public void setup() throws Exception {
        when(calendarService.calendarReference("site1", "main")).thenReturn(CALENDAR_REF);
        when(calendarService.getCalendar(CALENDAR_REF)).thenReturn(calendar);
        when(calendar.getId()).thenReturn("main");
        when(calendar.getEvent("event1")).thenReturn(event);
        when(event.getDisplayName()).thenReturn("Midterm review");
        when(event.getDescription()).thenReturn("Bring photosynthesis notes");
        when(event.getLocation()).thenReturn("Room 101");

        producer = new CalendarContentProducer();
        producer.setCalendarService(calendarService);
    }

    @Test
    public void eventReferencesIdentifySiteAndEvent() {
        assertTrue(producer.matches(EVENT_REF));
        assertEquals("site1", producer.getSiteId(EVENT_REF));
        assertEquals("event1", producer.getId(EVENT_REF));

        assertFalse(producer.matches(CALENDAR_REF));
        assertFalse(producer.matches("/calendar/event/site1/main"));
        assertFalse(producer.matches((String) null));
    }

    @Test
    public void onlyEventChangesAreIndexed() {
        assertEquals(SearchBuilderItem.ACTION_ADD, producer.getAction(event(CalendarService.EVENT_ADD_CALENDAR, EVENT_REF)));
        assertEquals(SearchBuilderItem.ACTION_ADD, producer.getAction(event(CalendarService.EVENT_MODIFY_CALENDAR, EVENT_REF)));
        assertEquals(SearchBuilderItem.ACTION_DELETE, producer.getAction(event(CalendarService.EVENT_REMOVE_CALENDAR_EVENT, EVENT_REF)));

        // calendar.revise is posted for the calendar itself too
        assertFalse(producer.matches(event(CalendarService.EVENT_MODIFY_CALENDAR, CALENDAR_REF)));
        assertEquals(SearchBuilderItem.ACTION_UNKNOWN, producer.getAction(event(CalendarService.EVENT_MODIFY_CALENDAR, CALENDAR_REF)));
    }

    @Test
    public void contentHoldsTitleDescriptionAndLocation() {
        String content = producer.getContent(EVENT_REF);

        assertEquals("Midterm review", producer.getTitle(EVENT_REF));
        for (String expected : List.of("Midterm review", "Bring photosynthesis notes", "Room 101")) {
            assertTrue(content, content.contains(expected));
        }
    }

    @Test
    public void unreadableEventsHaveNoContent() throws Exception {
        when(calendar.getEvent("event1")).thenThrow(new PermissionException("user", "calendar.read", EVENT_REF));

        assertEquals("", producer.getContent(EVENT_REF));
        assertEquals("", producer.getTitle(EVENT_REF));
    }

    @Test
    public void readAccessIsTheCalendarsOwnGroupAwareCheck() {
        when(calendar.allowGetEvent("event1")).thenReturn(true);
        assertTrue(producer.canRead(EVENT_REF));

        when(calendar.allowGetEvent("event1")).thenReturn(false);
        assertFalse(producer.canRead(EVENT_REF));
        assertFalse(producer.canRead(CALENDAR_REF));
    }

    @Test
    public void recurringEventsAreIndexedOnce() throws Exception {
        List<CalendarEvent> events = new ArrayList<>();
        for (String id : List.of("single", "!r1!1!weekly", "!r1!2!weekly", "!r1!3!weekly")) {
            CalendarEvent occurrence = mock(CalendarEvent.class);
            when(occurrence.getId()).thenReturn(id);
            events.add(occurrence);
        }
        when(calendar.getEvents(null, null)).thenReturn(events);
        when(calendarService.eventReference("site1", "main", "single")).thenReturn("/calendar/event/site1/main/single");
        when(calendarService.eventReference("site1", "main", "weekly")).thenReturn("/calendar/event/site1/main/weekly");

        List<String> references = new ArrayList<>();
        producer.getSiteContentIterator("site1").forEachRemaining(references::add);

        assertEquals(List.of("/calendar/event/site1/main/single", "/calendar/event/site1/main/weekly"), references);
    }

    private static Event event(String name, String resource) {
        Event event = mock(Event.class);
        when(event.getEvent()).thenReturn(name);
        when(event.getResource()).thenReturn(resource);
        return event;
    }
}
