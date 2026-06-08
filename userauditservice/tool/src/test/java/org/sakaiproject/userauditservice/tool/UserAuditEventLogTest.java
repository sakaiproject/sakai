/**********************************************************************************
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
 **********************************************************************************/

package org.sakaiproject.userauditservice.tool;

import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.component.cover.ComponentManager;

public class UserAuditEventLogTest {

	private UserAuditEventLog eventLog;

	@Before
	public void setUp() {
		ComponentManager.testingMode = true;
		eventLog = new UserAuditEventLog();
	}

	@After
	public void tearDown() {
		ComponentManager.shutdown();
		ComponentManager.testingMode = false;
	}

	@Test
	public void getRowsNumberUsesPositivePageSize() {
		eventLog.setTotalItems(500);
		eventLog.setFirstItem(0);
		eventLog.setPageSize(200);
		assertEquals(200, eventLog.getRowsNumber());
	}

	@Test
	public void getRowsNumberReturnsRemainingOnLastPage() {
		eventLog.setTotalItems(250);
		eventLog.setFirstItem(200);
		eventLog.setPageSize(200);
		assertEquals(50, eventLog.getRowsNumber());
	}

	@Test
	public void getRowsNumberReturnsTotalWhenSmallerThanPageSize() {
		eventLog.setTotalItems(50);
		eventLog.setFirstItem(0);
		eventLog.setPageSize(200);
		assertEquals(50, eventLog.getRowsNumber());
	}

	@Test
	public void getRowsNumberShowsAllWhenPageSizeIsZero() {
		eventLog.setTotalItems(500);
		eventLog.setFirstItem(0);
		eventLog.setPageSize(0);
		assertEquals(500, eventLog.getRowsNumber());
	}

	@Test
	public void getRowsNumberShowsAllWhenPageSizeIsNegative() {
		eventLog.setTotalItems(12);
		eventLog.setFirstItem(0);
		eventLog.setPageSize(-1);
		assertEquals(12, eventLog.getRowsNumber());
	}

	@Test
	public void parseDateRangeUsesInclusiveStartAndExclusiveNextDay() {
		TimeZone timeZone = TimeZone.getTimeZone("America/New_York");
		EventLogFilter filter = EventLogFilter.of(null, "2026-06-08", "2026-06-08", timeZone);

		assertEquals(Timestamp.from(LocalDate.of(2026, 6, 8).atStartOfDay(timeZone.toZoneId()).toInstant()), filter.fromAuditStamp);
		assertEquals(Timestamp.from(LocalDate.of(2026, 6, 9).atStartOfDay(timeZone.toZoneId()).toInstant()), filter.toAuditStamp);
	}

	@Test(expected = DateTimeParseException.class)
	public void parseDateRangeRejectsInvalidDates() {
		EventLogFilter.of(null, "06/08/2026", null, TimeZone.getTimeZone("UTC"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseDateRangeRejectsToDateBeforeFromDate() {
		EventLogFilter.of(null, "2026-06-09", "2026-06-08", TimeZone.getTimeZone("UTC"));
	}
}
