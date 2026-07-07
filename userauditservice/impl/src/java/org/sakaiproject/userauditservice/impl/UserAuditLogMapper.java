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

package org.sakaiproject.userauditservice.impl;

import java.time.Instant;

import org.sakaiproject.userauditservice.api.model.UserAuditEntry;
import org.sakaiproject.userauditservice.api.model.UserAuditLog;

final class UserAuditLogMapper {

	private UserAuditLogMapper() {
	}

	static UserAuditLog from(UserAuditEntry entry) {
		UserAuditLog auditLog = new UserAuditLog();
		auditLog.setSiteId(entry.getSiteId());
		auditLog.setUserId(entry.getUserId());
		auditLog.setRoleName(entry.getRoleName());
		auditLog.setActionTaken(entry.getActionTaken());
		auditLog.setAuditStamp(Instant.now());
		auditLog.setSource(entry.getSource());
		auditLog.setActionUserId(entry.getActionUserId());
		return auditLog;
	}
}
