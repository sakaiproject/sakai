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
package org.sakaiproject.calendar.impl.readers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.calendar.impl.DailyRecurrenceRule;
import org.sakaiproject.calendar.impl.GenericCalendarImporter;
import org.sakaiproject.calendar.impl.GenericImportRowHandler;
import org.sakaiproject.exception.ImportException;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.util.ResourceLoader;

@RunWith(MockitoJUnitRunner.class)
public class IcalendarReaderTest {

	@Mock
	private TimeService timeService;

	@Mock
	private ResourceLoader resourceLoader;

	private IcalendarReader reader;

	@Before
	public void setUp() {
		when(timeService.getLocalTimeZone()).thenReturn(TimeZone.getTimeZone(ZoneId.of("Europe/Madrid")));
		when(resourceLoader.getLocale()).thenReturn(Locale.ENGLISH);
		reader = new IcalendarReader();
		reader.setTimeService(timeService);
		GenericCalendarImporter.setRb(resourceLoader);
	}

	@After
	public void tearDown() {
		GenericCalendarImporter.setRb(new ResourceLoader("calendar"));
	}

	@Test
	public void testImportAllDayWithValueDate_SAK50276() throws ImportException {
		List<Map<String, Object>> rows = importRows("/allday_value_date.ics");

		assertEquals("VALUE=DATE events must be imported", 2, rows.size());

		Map<String, Object> multiDay = findByTitle(rows, "Summer Break");
		assertNotNull(multiDay);
		assertEquals(LocalDate.of(2024, 6, 17), multiDay.get(GenericCalendarImporter.DATE_PROPERTY_NAME));
		assertEquals(DailyRecurrenceRule.FREQ, multiDay.get(GenericCalendarImporter.FREQUENCY_PROPERTY_NAME));
		assertEquals(LocalDate.of(2024, 6, 23), multiDay.get(GenericCalendarImporter.ENDS_PROPERTY_NAME));

		Map<String, Object> singleDay = findByTitle(rows, "Public Holiday");
		assertNotNull(singleDay);
		assertEquals(LocalDate.of(2024, 7, 4), singleDay.get(GenericCalendarImporter.DATE_PROPERTY_NAME));
		assertEquals(Integer.valueOf((int) IcalendarReader.MINUTES_IN_DAY),
				singleDay.get(GenericCalendarImporter.DURATION_PROPERTY_NAME));
		assertNull("Single-day all-day must not use daily recurrence",
				singleDay.get(GenericCalendarImporter.FREQUENCY_PROPERTY_NAME));
	}

	@Test
	public void testImportDateOnlyWithoutValueDate() throws ImportException {
		List<Map<String, Object>> rows = importRows("/allday_date_only_no_value.ics");

		assertEquals("Date-only events without VALUE=DATE must be imported as all-day", 2, rows.size());

		Map<String, Object> singleDay = findByTitle(rows, "Spring Holiday");
		assertNotNull(singleDay);
		assertEquals(LocalDate.of(2026, 5, 1), singleDay.get(GenericCalendarImporter.DATE_PROPERTY_NAME));
		assertEquals(Integer.valueOf((int) IcalendarReader.MINUTES_IN_DAY),
				singleDay.get(GenericCalendarImporter.DURATION_PROPERTY_NAME));
		assertNull("Single-day date-only must not use daily recurrence",
				singleDay.get(GenericCalendarImporter.FREQUENCY_PROPERTY_NAME));

		Map<String, Object> period = findByTitle(rows, "Winter Break");
		assertNotNull(period);
		assertEquals(LocalDate.of(2026, 12, 23), period.get(GenericCalendarImporter.DATE_PROPERTY_NAME));
		assertEquals(DailyRecurrenceRule.FREQ, period.get(GenericCalendarImporter.FREQUENCY_PROPERTY_NAME));
		assertEquals(LocalDate.of(2027, 1, 5), period.get(GenericCalendarImporter.ENDS_PROPERTY_NAME));
	}

	@Test
	public void testImportDateOnlyWithRruleExpandsOccurrences() throws ImportException {
		LocalDate start = LocalDate.now(ZoneId.of("Europe/Madrid")).plusDays(10);
		while (start.getDayOfWeek().getValue() != 1) {
			start = start.plusDays(1);
		}
		LocalDate end = start.plusDays(1);
		String startDate = start.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
		String endDate = end.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
		String ics = "BEGIN:VCALENDAR\nVERSION:2.0\nBEGIN:VEVENT\n"
				+ "UID:date-only-rrule@example.com\n"
				+ "DTSTART:" + startDate + "\n"
				+ "DTEND:" + endDate + "\n"
				+ "RRULE:FREQ=WEEKLY;COUNT=3\n"
				+ "SUMMARY:Weekly Date Only\n"
				+ "END:VEVENT\nEND:VCALENDAR\n";

		GenericImportRowHandler handler = new GenericImportRowHandler(reader.getDefaultColumnMap(), resourceLoader);
		reader.importStreamFromDelimitedFile(
				new ByteArrayInputStream(ics.getBytes(StandardCharsets.UTF_8)), handler);
		List<Map<String, Object>> rows = handler.getRowList();

		assertEquals(3, rows.size());
		LocalDate first = (LocalDate) rows.get(0).get(GenericCalendarImporter.DATE_PROPERTY_NAME);
		LocalDate second = (LocalDate) rows.get(1).get(GenericCalendarImporter.DATE_PROPERTY_NAME);
		LocalDate third = (LocalDate) rows.get(2).get(GenericCalendarImporter.DATE_PROPERTY_NAME);
		assertNotNull(first);
		assertEquals(7, ChronoUnit.DAYS.between(first, second));
		assertEquals(7, ChronoUnit.DAYS.between(second, third));
		for (Map<String, Object> row : rows) {
			assertEquals("Weekly Date Only", row.get("Summary"));
			assertEquals(Integer.valueOf((int) IcalendarReader.MINUTES_IN_DAY),
					row.get(GenericCalendarImporter.DURATION_PROPERTY_NAME));
			assertNull(row.get(GenericCalendarImporter.FREQUENCY_PROPERTY_NAME));
		}
	}

	@Test
	public void testTimedEventStillImports() throws ImportException {
		LocalDate start = LocalDate.now(ZoneId.of("UTC")).plusDays(7);
		String startStamp = start.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE) + "T100000Z";
		String endStamp = start.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE) + "T110000Z";
		String ics = "BEGIN:VCALENDAR\nVERSION:2.0\nBEGIN:VEVENT\n"
				+ "UID:timed@example.com\n"
				+ "DTSTART:" + startStamp + "\n"
				+ "DTEND:" + endStamp + "\n"
				+ "SUMMARY:Timed Meeting\n"
				+ "END:VEVENT\nEND:VCALENDAR\n";

		GenericImportRowHandler handler = new GenericImportRowHandler(reader.getDefaultColumnMap(), resourceLoader);
		reader.importStreamFromDelimitedFile(
				new ByteArrayInputStream(ics.getBytes(StandardCharsets.UTF_8)), handler);
		List<Map<String, Object>> rows = handler.getRowList();

		assertEquals(1, rows.size());
		assertEquals("Timed Meeting", rows.get(0).get("Summary"));
		assertEquals(Integer.valueOf(60), rows.get(0).get(GenericCalendarImporter.DURATION_PROPERTY_NAME));
	}

	@Test
	public void testMidnightDateTimeIsNotAllDayButStillImports() throws ImportException {
		LocalDate start = LocalDate.now(ZoneId.of("UTC")).plusDays(14);
		LocalDate end = start.plusDays(1);
		String startStamp = start.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE) + "T000000Z";
		String endStamp = end.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE) + "T000000Z";
		String ics = "BEGIN:VCALENDAR\nVERSION:2.0\nBEGIN:VEVENT\n"
				+ "UID:midnight@example.com\n"
				+ "DTSTART:" + startStamp + "\n"
				+ "DTEND:" + endStamp + "\n"
				+ "SUMMARY:Midnight Timed Range\n"
				+ "END:VEVENT\nEND:VCALENDAR\n";


		GenericImportRowHandler handler = new GenericImportRowHandler(reader.getDefaultColumnMap(), resourceLoader);
		reader.importStreamFromDelimitedFile(
				new ByteArrayInputStream(ics.getBytes(StandardCharsets.UTF_8)), handler);
		List<Map<String, Object>> rows = handler.getRowList();

		assertEquals(1, rows.size());
		Map<String, Object> row = rows.get(0);
		assertEquals("Midnight Timed Range", row.get("Summary"));
		assertEquals("Exact 1440-minute timed range must keep its duration",
				Integer.valueOf(1440),
				row.get(GenericCalendarImporter.DURATION_PROPERTY_NAME));
		assertNull("Exact one-day timed range must not become daily recurrence",
				row.get(GenericCalendarImporter.FREQUENCY_PROPERTY_NAME));
	}

	@Test
	public void testBadEventDoesNotDiscardFollowingEvent() throws ImportException {
		List<Map<String, Object>> rows = importEventRows(
				"SUMMARY:Bad Event\nDTSTART;VALUE=DATE:20260501\nDTEND:20260502T000000Z\n",
				"SUMMARY:Good Event\nDTSTART;VALUE=DATE:20260503\nDTEND;VALUE=DATE:20260504\n");
		assertEquals(1, rows.size());
		assertEquals("Good Event", rows.get(0).get("Summary"));
		assertEquals(LocalDate.of(2026, 5, 3), rows.get(0).get(GenericCalendarImporter.DATE_PROPERTY_NAME));
	}

	@Test
	public void testDateOnlyWithoutEndDefaultsToOneDay() throws ImportException {
		for (String marker : List.of("", ";VALUE=DATE")) {
			List<Map<String, Object>> rows = importEventRows("SUMMARY:Holiday\nDTSTART" + marker + ":20260501\n");
			assertEquals(1, rows.size());
			assertEquals(LocalDate.of(2026, 5, 1), rows.get(0).get(GenericCalendarImporter.DATE_PROPERTY_NAME));
			assertEquals(Integer.valueOf((int) IcalendarReader.MINUTES_IN_DAY),
					rows.get(0).get(GenericCalendarImporter.DURATION_PROPERTY_NAME));
			assertNull(rows.get(0).get(GenericCalendarImporter.FREQUENCY_PROPERTY_NAME));
		}
	}

	@Test
	public void testDateOnlyDurationDeterminesExclusiveEnd() throws ImportException {
		for (String marker : List.of("", ";VALUE=DATE")) {
			List<Map<String, Object>> rows = importEventRows("SUMMARY:Holiday\nDTSTART" + marker
					+ ":20260501\nDURATION:P3D\n");
			assertEquals(1, rows.size());
			assertEquals(DailyRecurrenceRule.FREQ, rows.get(0).get(GenericCalendarImporter.FREQUENCY_PROPERTY_NAME));
			assertEquals(LocalDate.of(2026, 5, 3), rows.get(0).get(GenericCalendarImporter.ENDS_PROPERTY_NAME));
		}
	}

	@Test
	public void testRecurringDateOnlyWithoutEndExpandsOneDayOccurrences() throws ImportException {
		LocalDate first = LocalDate.now(ZoneId.of("Europe/Madrid")).plusDays(10);
		for (String marker : List.of("", ";VALUE=DATE")) {
			List<Map<String, Object>> rows = importEventRows("SUMMARY:Weekly Holiday\nDTSTART" + marker + ":"
					+ first.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE) + "\nRRULE:FREQ=WEEKLY;COUNT=3\n");
			assertEquals(3, rows.size());
			for (int occurrence = 0; occurrence < rows.size(); occurrence++) {
				assertEquals(first.plusWeeks(occurrence), rows.get(occurrence).get(GenericCalendarImporter.DATE_PROPERTY_NAME));
				assertEquals(Integer.valueOf((int) IcalendarReader.MINUTES_IN_DAY),
						rows.get(occurrence).get(GenericCalendarImporter.DURATION_PROPERTY_NAME));
				assertNull(rows.get(occurrence).get(GenericCalendarImporter.FREQUENCY_PROPERTY_NAME));
			}
		}
	}

	@Test
	public void testTimedDurationImportsWithoutEnd() throws ImportException {
		String date = LocalDate.now(ZoneId.of("UTC")).plusDays(10)
				.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
		List<Map<String, Object>> rows = importEventRows("SUMMARY:Timed Meeting\nDTSTART:" + date
				+ "T100000Z\nDURATION:PT90M\n");
		assertEquals(1, rows.size());
		assertEquals(Integer.valueOf(90), rows.get(0).get(GenericCalendarImporter.DURATION_PROPERTY_NAME));
	}

	private List<Map<String, Object>> importEventRows(String... events) throws ImportException {
		StringBuilder ics = new StringBuilder("BEGIN:VCALENDAR\nVERSION:2.0\nPRODID:-//Sakai//Test//EN\n");
		for (int index = 0; index < events.length; index++) {
			ics.append("BEGIN:VEVENT\nUID:event-").append(index).append("@example.com\n")
					.append("DTSTAMP:20260101T000000Z\n").append(events[index]).append("END:VEVENT\n");
		}
		ics.append("END:VCALENDAR\n");
		GenericImportRowHandler handler = new GenericImportRowHandler(reader.getDefaultColumnMap(), resourceLoader);
		try (InputStream stream = new ByteArrayInputStream(ics.toString().getBytes(StandardCharsets.UTF_8))) {
			reader.importStreamFromDelimitedFile(stream, handler);
		} catch (java.io.IOException e) {
			throw new ImportException(e);
		}
		return handler.getRowList();
	}

	private List<Map<String, Object>> importRows(String resource) throws ImportException {
		GenericImportRowHandler handler = new GenericImportRowHandler(reader.getDefaultColumnMap(), resourceLoader);
		try (InputStream in = getClass().getResourceAsStream(resource)) {
			assertNotNull("Missing test resource " + resource, in);
			reader.importStreamFromDelimitedFile(in, handler);
		} catch (java.io.IOException e) {
			throw new ImportException(e);
		}
		return handler.getRowList();
	}

	private static Map<String, Object> findByTitle(List<Map<String, Object>> rows, String title) {
		return rows.stream()
				.filter(row -> title.equals(row.get("Summary")))
				.findFirst()
				.orElse(null);
	}

}
