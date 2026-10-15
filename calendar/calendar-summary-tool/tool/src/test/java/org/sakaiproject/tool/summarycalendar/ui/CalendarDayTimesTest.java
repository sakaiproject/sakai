/**********************************************************************************
 *
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.summarycalendar.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Test;

public class CalendarDayTimesTest {

	private static final TimeZone TOKYO = TimeZone.getTimeZone("Asia/Tokyo");
	private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

	@Test
	public void startOfDayIsMidnightInZone() {
		ZonedDateTime noonUtc = ZonedDateTime.of(2026, 3, 15, 12, 0, 0, 0, ZoneId.of("UTC"));
		Calendar cal = GregorianCalendar.from(noonUtc);
		long start = CalendarDayTimes.startOfDayMillis(cal, UTC);
		ZonedDateTime expected = LocalDate.of(2026, 3, 15).atStartOfDay(ZoneId.of("UTC"));
		assertEquals(expected.toInstant().toEpochMilli(), start);
	}

	@Test
	public void endOfDayIsLastSecondInZone() {
		ZonedDateTime noonTokyo = ZonedDateTime.of(2026, 3, 15, 12, 0, 0, 0, ZoneId.of("Asia/Tokyo"));
		Calendar cal = GregorianCalendar.from(noonTokyo);
		long end = CalendarDayTimes.endOfDayMillis(cal, TOKYO);
		ZonedDateTime expected = ZonedDateTime.of(LocalDate.of(2026, 3, 15), LocalTime.of(23, 59, 59), ZoneId.of("Asia/Tokyo"));
		assertEquals(expected.toInstant().toEpochMilli(), end);
	}

	@Test
	public void isLocalDateRespectsUserTimeZone() {
		// 2026-03-15 01:00 in Tokyo is still 2026-03-14 in UTC
		Instant tokyoMorning = ZonedDateTime.of(2026, 3, 15, 1, 0, 0, 0, ZoneId.of("Asia/Tokyo")).toInstant();
		Instant now = ZonedDateTime.of(2026, 3, 15, 10, 0, 0, 0, ZoneId.of("Asia/Tokyo")).toInstant();

		assertTrue(CalendarDayTimes.isLocalDate(tokyoMorning.toEpochMilli(), TOKYO, now));
		assertFalse(CalendarDayTimes.isLocalDate(tokyoMorning.toEpochMilli(), UTC, now));
	}
}
