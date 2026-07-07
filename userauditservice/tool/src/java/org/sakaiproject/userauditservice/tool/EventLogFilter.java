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

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.TimeZone;

final class EventLogFilter {

	final String userId;
	final Timestamp fromAuditStamp;
	final Timestamp toAuditStamp;
	final TimeZone timeZone;

	private EventLogFilter(String userId, Timestamp fromAuditStamp, Timestamp toAuditStamp, TimeZone timeZone) {
		this.userId = userId;
		this.fromAuditStamp = fromAuditStamp;
		this.toAuditStamp = toAuditStamp;
		this.timeZone = timeZone;
	}

	static EventLogFilter empty() {
		return new EventLogFilter(null, null, null, null);
	}

	static EventLogFilter of(String userId, String fromDate, String toDate, TimeZone timeZone) {
		Timestamp fromAuditStamp = parseDate(fromDate, timeZone, 0);
		Timestamp toAuditStamp = parseDate(toDate, timeZone, 1);
		if (fromAuditStamp != null && toAuditStamp != null && !fromAuditStamp.before(toAuditStamp)) {
			throw new IllegalArgumentException("To date must be on or after from date");
		}
		return new EventLogFilter(userId, fromAuditStamp, toAuditStamp, timeZone);
	}

	private static Timestamp parseDate(String date, TimeZone timeZone, int plusDays) {
		if (date == null) {
			return null;
		}
		ZoneId zoneId = timeZone.toZoneId();
		return Timestamp.from(LocalDate.parse(date).plusDays(plusDays).atStartOfDay(zoneId).toInstant());
	}
}
