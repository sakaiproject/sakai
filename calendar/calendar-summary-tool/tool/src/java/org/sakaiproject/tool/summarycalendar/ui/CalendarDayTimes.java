/**********************************************************************************
 *
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.summarycalendar.ui;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Day-boundary helpers for the summary calendar, using {@link java.time}.
 */
final class CalendarDayTimes {

	private CalendarDayTimes() {
	}

	static long startOfDayMillis(Calendar day, TimeZone timeZone) {
		return startOfDay(day, timeZone).toInstant().toEpochMilli();
	}

	static long endOfDayMillis(Calendar day, TimeZone timeZone) {
		ZonedDateTime start = startOfDay(day, timeZone);
		return start.withHour(23).withMinute(59).withSecond(59).withNano(0).toInstant().toEpochMilli();
	}

	static boolean isLocalDate(long epochMillis, TimeZone timeZone, Instant now) {
		ZoneId zone = timeZone.toZoneId();
		LocalDate date = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate();
		LocalDate today = now.atZone(zone).toLocalDate();
		return date.equals(today);
	}

	private static ZonedDateTime startOfDay(Calendar day, TimeZone timeZone) {
		ZoneId zone = timeZone.toZoneId();
		return Instant.ofEpochMilli(day.getTimeInMillis()).atZone(zone).toLocalDate().atStartOfDay(zone);
	}
}
