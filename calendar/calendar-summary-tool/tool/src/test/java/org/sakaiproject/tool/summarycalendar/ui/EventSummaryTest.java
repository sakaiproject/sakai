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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;

import org.junit.Test;

public class EventSummaryTest {

	@Test
	public void isTodayRespectsProvidedTimeZone() {
		TimeZone tokyo = TimeZone.getTimeZone("Asia/Tokyo");
		TimeZone utc = TimeZone.getTimeZone("UTC");
		Instant tokyoMorning = ZonedDateTime.of(2026, 3, 15, 1, 0, 0, 0, ZoneId.of("Asia/Tokyo")).toInstant();
		Instant now = ZonedDateTime.of(2026, 3, 15, 10, 0, 0, 0, ZoneId.of("Asia/Tokyo")).toInstant();

		EventSummary summary = new EventSummary();
		assertTrue(summary.isToday(tokyoMorning.toEpochMilli(), tokyo, now));
		assertFalse(summary.isToday(tokyoMorning.toEpochMilli(), utc, now));
	}
}
