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

package org.sakaiproject.userauditservice.api;

import java.util.List;

import org.sakaiproject.userauditservice.api.model.UserAuditEntry;
import org.sakaiproject.userauditservice.api.model.UserAuditLog;

/**
 * Service interface that allows tools to register their own database key and associated text for User Auditing
 *
 */
public interface UserAuditService {
	
	/** Database action value for logging a user was added to a site */
	static final String USER_AUDIT_ACTION_ADD = "A";
	
	/** Database action value for logging a user was removed from a site */
	static final String USER_AUDIT_ACTION_REMOVE = "D";
	
	/** Database action value for logging a user was updated in a site, typically in a different role */
	static final String USER_AUDIT_ACTION_UPDATE = "U";
	
	/**
	 * Method to register a UserAuditRegistration object
	 * @param ua
	 */
	public void register(UserAuditRegistration uar);
	
	/**
	 * Gets all UserAuditRegistration objects that have been registered
	 * @return
	 */
	public List<UserAuditRegistration> getRegisteredItems();
	
	/**
	 * Get the keys for all UserAuditRegistration objects that have been registered
	 * @return
	 */
	public List<String> getKeys();

	/**
	 * Processes audit entries and writes them to persistence.
	 *
	 * @param userAuditList audit entries to write
	 */
	public void addToUserAuditing(List<UserAuditEntry> userAuditList);

	/**
	 * Delete all user audit log entries for a site.
	 *
	 * @param siteId site id to delete audit logs for
	 */
	public void deleteUserAuditingFromSite(String siteId);

	/**
	 * Count persisted user audit log entries matching a query.
	 *
	 * @param query audit log query
	 * @return matching row count
	 */
	public long countUserAuditLogs(UserAuditLogQuery query);

	/**
	 * Fetch persisted user audit log entries matching a query.
	 *
	 * @param query audit log query
	 * @return matching audit log rows
	 */
	public List<UserAuditLog> getUserAuditLogs(UserAuditLogQuery query);
}
