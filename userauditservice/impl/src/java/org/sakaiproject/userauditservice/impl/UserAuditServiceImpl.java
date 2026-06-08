/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2013 The Sakai Foundation
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

package org.sakaiproject.userauditservice.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sakaiproject.userauditservice.api.UserAuditLogQuery;
import org.sakaiproject.userauditservice.api.UserAuditRegistration;
import org.sakaiproject.userauditservice.api.UserAuditService;
import org.sakaiproject.userauditservice.api.model.UserAuditEntry;
import org.sakaiproject.userauditservice.api.model.UserAuditLog;
import org.sakaiproject.userauditservice.api.repository.UserAuditLogRepository;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAuditServiceImpl implements UserAuditService {

	private List<UserAuditRegistration> registeredItems = new ArrayList<>();
	private List<String> keys = new ArrayList<>();
	private UserAuditLogRepository userAuditLogRepository;

	@Override
	public void register(UserAuditRegistration userAuditRegistration) {
		registeredItems.add(userAuditRegistration);
		keys.add(userAuditRegistration.getDatabaseSourceKey());
	}

	@Override
	public void addToUserAuditing(List<UserAuditEntry> userAuditList) {
		if (userAuditList == null || userAuditList.isEmpty()) {
			return;
		}

		List<UserAuditLog> auditLogs = new ArrayList<UserAuditLog>();
		for (UserAuditEntry entry : userAuditList) {
			if (entry != null) {
				auditLogs.add(UserAuditLogMapper.from(entry));
			}
		}
		if (!auditLogs.isEmpty()) {
			userAuditLogRepository.saveAll(auditLogs);
		}
	}

	@Override
	public void deleteUserAuditingFromSite(String siteId) {
		if (siteId == null) {
			return;
		}
		userAuditLogRepository.deleteBySiteId(siteId);
	}

	@Override
	public long countUserAuditLogs(UserAuditLogQuery query) {
		if (query == null || query.getSiteId() == null) {
			return 0;
		}
		return userAuditLogRepository.count(query);
	}

	@Override
	public List<UserAuditLog> getUserAuditLogs(UserAuditLogQuery query) {
		if (query == null || query.getSiteId() == null) {
			return Collections.emptyList();
		}
		return userAuditLogRepository.find(query);
	}
}
