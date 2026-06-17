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

package org.sakaiproject.userauditservice.api;

import java.time.Instant;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserAuditLogQuery {

	/** Sentinel value for {@link #limit}: do not cap the number of returned rows. */
	public static final int NO_LIMIT = 0;

	private String siteId;
	private String userId;
	private Instant fromAuditStamp;
	private Instant toAuditStamp;
	@Builder.Default
	private UserAuditSortColumn sortColumn = UserAuditSortColumn.AUDIT_STAMP;
	private boolean sortAscending;
	/** Number of rows to skip before returning results; 0 starts at the first match. */
	private int offset;
	/**
	 * Maximum number of rows to return. Defaults to {@link #NO_LIMIT}, meaning no
	 * max-results cap and all matching rows are returned.
	 */
	@Builder.Default
	private int limit = NO_LIMIT;
}
