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

import org.sakaiproject.userauditservice.api.UserAuditLogQuery;
import org.sakaiproject.userauditservice.api.UserAuditSortColumn;

final class EventLogQueryBuilder {

	private EventLogQueryBuilder() {
	}

	static UserAuditLogQuery build(String siteId, EventLogFilter filter, String sortColumn, boolean sortAscending,
			int offset, int limit) {

		return UserAuditLogQuery.builder()
				.siteId(siteId)
				.userId(filter.userId)
				.fromAuditStamp(filter.fromAuditStamp)
				.toAuditStamp(filter.toAuditStamp)
				.sortColumn(UserAuditSortColumn.fromColumnName(sortColumn))
				.sortAscending(sortAscending)
				.offset(offset)
				.limit(limit)
				.build();
	}
}
