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

package org.sakaiproject.userauditservice.api.model;

import java.util.Objects;

import lombok.Value;

@Value
public class UserAuditEntry {

	private String siteId;
	private String userId;
	private String roleName;
	private String actionTaken;
	private String source;
	private String actionUserId;

	public static UserAuditEntry of(String siteId, String userId, String roleName,
			String actionTaken, String source, String actionUserId) {

		Objects.requireNonNull(siteId, "siteId must not be null");
		Objects.requireNonNull(userId, "userId must not be null");
		Objects.requireNonNull(roleName, "roleName must not be null");
		Objects.requireNonNull(actionTaken, "actionTaken must not be null");
		return new UserAuditEntry(siteId, userId, roleName, actionTaken, source, actionUserId);
	}
}
