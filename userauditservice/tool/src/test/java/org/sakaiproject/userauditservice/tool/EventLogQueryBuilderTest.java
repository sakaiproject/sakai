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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.TimeZone;

import org.junit.Test;
import org.sakaiproject.userauditservice.api.UserAuditLogQuery;
import org.sakaiproject.userauditservice.api.UserAuditSortColumn;

public class EventLogQueryBuilderTest {

	@Test
	public void buildUsesSiteOnlyWhenFiltersAreEmpty() {
		EventLogFilter filter = EventLogFilter.empty();
		UserAuditLogQuery query = EventLogQueryBuilder.build("site-a", filter, "auditStamp", false, 0, 0);

		assertEquals("site-a", query.getSiteId());
		assertNull(query.getUserId());
		assertNull(query.getFromAuditStamp());
		assertNull(query.getToAuditStamp());
		assertEquals(UserAuditSortColumn.AUDIT_STAMP, query.getSortColumn());
		assertFalse(query.isSortAscending());
		assertEquals(0, query.getOffset());
		assertEquals(0, query.getLimit());
	}

	@Test
	public void buildAddsExactUserIdPredicate() {
		EventLogFilter filter = EventLogFilter.of("user-1", null, null, TimeZone.getTimeZone("UTC"));
		UserAuditLogQuery query = EventLogQueryBuilder.build("site-a", filter, "userId", true, 0, 0);

		assertEquals("user-1", query.getUserId());
		assertEquals(UserAuditSortColumn.USER_ID, query.getSortColumn());
		assertTrue(query.isSortAscending());
	}

	@Test
	public void buildAddsDatePredicates() {
		TimeZone timeZone = TimeZone.getTimeZone("UTC");
		EventLogFilter filter = EventLogFilter.of(null, "2026-06-01", "2026-06-30", timeZone);
		UserAuditLogQuery query = EventLogQueryBuilder.build("site-a", filter, "auditStamp", false, 0, 0);

		assertEquals(Timestamp.from(LocalDate.of(2026, 6, 1).atStartOfDay(timeZone.toZoneId()).toInstant()),
				query.getFromAuditStamp());
		assertEquals(Timestamp.from(LocalDate.of(2026, 7, 1).atStartOfDay(timeZone.toZoneId()).toInstant()),
				query.getToAuditStamp());
	}

	@Test
	public void buildCombinesUserDateSortAndPaging() {
		TimeZone timeZone = TimeZone.getTimeZone("UTC");
		EventLogFilter filter = EventLogFilter.of("user-1", "2026-06-01", "2026-06-30", timeZone);
		UserAuditLogQuery query = EventLogQueryBuilder.build("site-a", filter, "actionText", false, 200, 200);

		assertEquals("user-1", query.getUserId());
		assertEquals(UserAuditSortColumn.ACTION_TEXT, query.getSortColumn());
		assertEquals(200, query.getOffset());
		assertEquals(200, query.getLimit());
	}
}
