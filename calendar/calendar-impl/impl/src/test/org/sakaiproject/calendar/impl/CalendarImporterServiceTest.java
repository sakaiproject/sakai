/**
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
package org.sakaiproject.calendar.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarImporterService;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CalendarImportTestConfiguration.class)
public class CalendarImporterServiceTest {
    private static final ZoneId ZONE = ZoneId.of("Europe/Madrid");
    @Autowired private CalendarImporterService importer;
    @Autowired private org.sakaiproject.time.api.TimeService timeService;

    @Before
    public void setUp() {
        GenericCalendarImporter.setRb(new ResourceLoader("calendar") {
            @Override public Locale getLocale() { return Locale.ENGLISH; }
            @Override protected java.util.ResourceBundle getBundle() {
                return java.util.ResourceBundle.getBundle("calendar", Locale.ENGLISH);
            }
        });
    }

    @After
    public void tearDown() {
        GenericCalendarImporter.setRb(new ResourceLoader("calendar"));
    }

    @Test
    public void importsManusFixturesThroughService() throws Exception {
        for (String fixture : List.of("/allday_date_only_no_value.ics", "/allday_value_date.ics")) {
            try (InputStream stream = getClass().getResourceAsStream(fixture)) {
                assertNotNull(stream);
                List<? extends CalendarEvent> events = importer.doImport(CalendarImporterService.ICALENDAR_IMPORT,
                        stream, null, null, ZONE.getId());
                assertEquals(2, events.size());
                CalendarEvent multiDay = events.stream().filter(e -> e.getRecurrenceRule() != null).findFirst().orElseThrow();
                assertEquals(DailyRecurrenceRule.FREQ, multiDay.getRecurrenceRule().getFrequency());
                LocalDate lastDay = fixture.contains("no_value") ? LocalDate.of(2027, 1, 5) : LocalDate.of(2024, 6, 23);
                assertEquals(lastDay, Instant.ofEpochMilli(multiDay.getRecurrenceRule().getUntil().getTime())
                        .atZone(ZONE).toLocalDate());
                for (CalendarEvent event : events) {
                    assertEquals(0, start(event).atZone(ZONE).getHour());
                    assertEquals(Duration.ofMinutes(1435).toMillis(), event.getRange().duration());
                }
            }
        }
    }

    @Test
    public void expandsBothDateRepresentationsWithExclusions() throws Exception {
        LocalDate first = LocalDate.now(ZONE).plusDays(10);
        for (String marker : List.of("", ";VALUE=DATE")) {
            List<? extends CalendarEvent> events = importProperties("DTSTART" + marker + ":" + stamp(first)
                    + "\nDTEND" + marker + ":" + stamp(first.plusDays(1))
                    + "\nRRULE:FREQ=WEEKLY;COUNT=3\nEXDATE;VALUE=DATE:" + stamp(first.plusDays(7)) + "\n");
            assertEquals(List.of(first, first.plusDays(14)), dates(events));
            assertTrue(events.stream().allMatch(e -> e.getRecurrenceRule() == null));
        }
    }

    @Test
    public void expandsAdditionalDatesWithoutRrule() throws Exception {
        LocalDate first = LocalDate.now(ZONE).plusDays(10);
        List<? extends CalendarEvent> events = importProperties("DTSTART:" + stamp(first)
                + "\nDTEND:" + stamp(first.plusDays(1))
                + "\nRDATE;VALUE=DATE:" + stamp(first.plusDays(7)) + "," + stamp(first.plusDays(14))
                + "\nEXDATE;VALUE=DATE:" + stamp(first.plusDays(7)) + "\n");
        assertEquals(List.of(first, first.plusDays(14)), dates(events));
    }

    @Test
    public void retainsOneDayAcrossDaylightSavingChange() throws Exception {
        for (LocalDate date : List.of(LocalDate.of(2026, 3, 29), LocalDate.of(2026, 10, 25))) {
            List<? extends CalendarEvent> events = importProperties("DTSTART:" + stamp(date)
                    + "\nDTEND:" + stamp(date.plusDays(1)) + "\n");
            assertEquals(List.of(date), dates(events));
            assertNull(events.get(0).getRecurrenceRule());
            assertEquals(Duration.ofMinutes(1435).toMillis(), events.get(0).getRange().duration());
        }
    }

    @Test
    public void preservesLongTimedEventAsOneContinuousRange() throws Exception {
        LocalDate date = LocalDate.now(ZONE).plusDays(10);
        List<? extends CalendarEvent> events = importProperties("DTSTART:" + stamp(date) + "T100000Z"
                + "\nDTEND:" + stamp(date.plusDays(2)) + "T120000Z\n");
        assertEquals(1, events.size());
        assertNull(events.get(0).getRecurrenceRule());
        assertEquals(Duration.ofHours(50).toMillis(), events.get(0).getRange().duration());
    }

    @Test
    public void preservesTimedInstantsInUserTimezone() throws Exception {
        LocalDate date = LocalDate.now(ZONE).plusDays(10);
        List<? extends CalendarEvent> events = importProperties("DTSTART:" + stamp(date) + "T100000Z"
                + "\nDTEND:" + stamp(date) + "T110000Z\n");
        assertEquals(1, events.size());
        assertEquals(date.atTime(10, 0).toInstant(java.time.ZoneOffset.UTC), start(events.get(0)));
        assertEquals(Duration.ofHours(1).toMillis(), events.get(0).getRange().duration());
    }

    @Test
    public void exportsRecurrenceExclusionsWithTheirTimezone() throws Exception {
        LocalDate date = LocalDate.now(ZONE).plusDays(10);
        org.sakaiproject.calendar.api.CalendarEventEdit event = (org.sakaiproject.calendar.api.CalendarEventEdit)
                importProperties("DTSTART:" + stamp(date) + "T100000Z\nDTEND:" + stamp(date) + "T110000Z\n").get(0);
        event.setExclusionRule(new ExclusionRecurrenceRule(List.of(event.getRange())));
        List<net.fortuna.ical4j.model.property.ExDate<java.time.ZonedDateTime>> exclusions =
                org.sakaiproject.calendar.impl.readers.ICalRecurrence.toExDates(event, timeService);
        assertEquals(1, exclusions.size());
        assertEquals(start(event), exclusions.get(0).getDates().get(0).toInstant());
        assertEquals("Europe/Madrid", exclusions.get(0).getParameter("TZID").orElseThrow().getValue());
    }

    @Test
    public void exportsUntilAsUtc() {
        Instant until = Instant.parse("2026-12-25T12:00:00Z");
        net.fortuna.ical4j.model.property.RRule<Instant> rule =
                org.sakaiproject.calendar.impl.readers.ICalRecurrence.toRRule(
                        new WeeklyRecurrenceRule(1, timeService.newTime(until.toEpochMilli())));
        assertEquals(until, rule.getRecur().getUntil());
        assertTrue(rule.getValue().contains("UNTIL=20261225T120000Z"));
    }

    private List<? extends CalendarEvent> importProperties(String properties) throws Exception {
        String ics = "BEGIN:VCALENDAR\nVERSION:2.0\nPRODID:-//Sakai//Test//EN\nBEGIN:VEVENT\n"
                + "UID:import-test@example.com\nDTSTAMP:20260101T000000Z\nSUMMARY:Import Test\n"
                + properties + "END:VEVENT\nEND:VCALENDAR\n";
        try (InputStream stream = new ByteArrayInputStream(ics.getBytes(StandardCharsets.UTF_8))) {
            return importer.doImport(CalendarImporterService.ICALENDAR_IMPORT, stream, null, null, ZONE.getId());
        }
    }

    private static String stamp(LocalDate date) { return date.format(DateTimeFormatter.BASIC_ISO_DATE); }
    private static Instant start(CalendarEvent event) { return Instant.ofEpochMilli(event.getRange().firstTime().getTime()); }
    private static List<LocalDate> dates(List<? extends CalendarEvent> events) {
        return events.stream().map(e -> start(e).atZone(ZONE).toLocalDate()).collect(Collectors.toList());
    }
}
