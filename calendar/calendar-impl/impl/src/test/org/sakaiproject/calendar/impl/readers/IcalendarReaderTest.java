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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;
import org.sakaiproject.calendar.impl.DailyRecurrenceRule;
import org.sakaiproject.calendar.impl.GenericCalendarImporter;
import org.sakaiproject.calendar.impl.GenericImportRowHandler;
import org.sakaiproject.exception.ImportException;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.util.ResourceLoader;

@RunWith(MockitoJUnitRunner.class)
public class IcalendarReaderTest {

	@Rule
	public MockitoRule rule = MockitoJUnit.rule();

	@Mock
	private TimeService timeService;

	@Mock
	private ResourceLoader resourceLoader;

	private IcalendarReader reader;

	@Before
	public void setUp() {
		Locale.setDefault(Locale.ENGLISH);
		when(timeService.getLocalTimeZone()).thenReturn(TimeZone.getTimeZone(ZoneId.of("Europe/Madrid")));
		when(resourceLoader.getLocale()).thenReturn(Locale.ENGLISH);
		reader = new IcalendarReader();
		reader.setTimeService(timeService);
		GenericCalendarImporter.setRb(resourceLoader);
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

		assertEquals("Date-only events without VALUE=DATE must be imported", 2, rows.size());

		Map<String, Object> singleDay = findByTitle(rows, "Spring Holiday");
		assertNotNull(singleDay);
		assertEquals(LocalDate.of(2026, 5, 1), singleDay.get(GenericCalendarImporter.DATE_PROPERTY_NAME));

		Map<String, Object> period = findByTitle(rows, "Winter Break");
		assertNotNull(period);
		assertEquals(LocalDate.of(2026, 12, 23), period.get(GenericCalendarImporter.DATE_PROPERTY_NAME));
		assertEquals(DailyRecurrenceRule.FREQ, period.get(GenericCalendarImporter.FREQUENCY_PROPERTY_NAME));
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

		assertFalse("Explicit midnight DATE-TIME must stay on the timed path", isAllDayFromIcs(ics));

		GenericImportRowHandler handler = new GenericImportRowHandler(reader.getDefaultColumnMap(), resourceLoader);
		reader.importStreamFromDelimitedFile(
				new ByteArrayInputStream(ics.getBytes(StandardCharsets.UTF_8)), handler);
		List<Map<String, Object>> rows = handler.getRowList();

		assertEquals(1, rows.size());
		assertEquals("Midnight Timed Range", rows.get(0).get("Summary"));
	}

	@Test
	public void testIsAllDayDetectsValueDateOnly() {
		String valueDateIcs =
				"BEGIN:VCALENDAR\nVERSION:2.0\nBEGIN:VEVENT\n"
						+ "DTSTART;VALUE=DATE:20240617\nDTEND;VALUE=DATE:20240618\n"
						+ "SUMMARY:X\nEND:VEVENT\nEND:VCALENDAR\n";
		String dateOnlyIcs =
				"BEGIN:VCALENDAR\nVERSION:2.0\nBEGIN:VEVENT\n"
						+ "DTSTART:20251225\nDTEND:20251226\n"
						+ "SUMMARY:Y\nEND:VEVENT\nEND:VCALENDAR\n";
		String midnightDateTimeIcs =
				"BEGIN:VCALENDAR\nVERSION:2.0\nBEGIN:VEVENT\n"
						+ "DTSTART:20251225T000000\nDTEND:20251226T000000\n"
						+ "SUMMARY:M\nEND:VEVENT\nEND:VCALENDAR\n";
		String timedIcs =
				"BEGIN:VCALENDAR\nVERSION:2.0\nBEGIN:VEVENT\n"
						+ "DTSTART:20240617T100000Z\nDTEND:20240617T110000Z\n"
						+ "SUMMARY:Z\nEND:VEVENT\nEND:VCALENDAR\n";

		assertTrue(isAllDayFromIcs(valueDateIcs));
		assertFalse(isAllDayFromIcs(dateOnlyIcs));
		assertFalse(isAllDayFromIcs(midnightDateTimeIcs));
		assertFalse(isAllDayFromIcs(timedIcs));
	}

	private boolean isAllDayFromIcs(String ics) {
		try {
			CompatibilityHintsHolder.enable();
			net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.data.CalendarBuilder()
					.build(new ByteArrayInputStream(ics.getBytes(StandardCharsets.UTF_8)));
			net.fortuna.ical4j.model.Component event = calendar.getComponents(
					net.fortuna.ical4j.model.Component.VEVENT).get(0);
			return IcalendarReader.isAllDay(
					event.getProperty(net.fortuna.ical4j.model.Property.DTSTART),
					event.getProperty(net.fortuna.ical4j.model.Property.DTEND));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private List<Map<String, Object>> importRows(String resource) throws ImportException {
		GenericImportRowHandler handler = new GenericImportRowHandler(reader.getDefaultColumnMap(), resourceLoader);
		InputStream in = getClass().getResourceAsStream(resource);
		assertNotNull("Missing test resource " + resource, in);
		reader.importStreamFromDelimitedFile(in, handler);
		return handler.getRowList();
	}

	private static Map<String, Object> findByTitle(List<Map<String, Object>> rows, String title) {
		return rows.stream()
				.filter(row -> title.equals(row.get("Summary")))
				.findFirst()
				.orElse(null);
	}

	private static final class CompatibilityHintsHolder {
		static void enable() {
			net.fortuna.ical4j.util.CompatibilityHints.setHintEnabled(
					net.fortuna.ical4j.util.CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
			net.fortuna.ical4j.util.CompatibilityHints.setHintEnabled(
					net.fortuna.ical4j.util.CompatibilityHints.KEY_RELAXED_PARSING, true);
			net.fortuna.ical4j.util.CompatibilityHints.setHintEnabled(
					net.fortuna.ical4j.util.CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY, true);
			net.fortuna.ical4j.util.CompatibilityHints.setHintEnabled(
					net.fortuna.ical4j.util.CompatibilityHints.KEY_RELAXED_VALIDATION, true);
		}
	}
}
