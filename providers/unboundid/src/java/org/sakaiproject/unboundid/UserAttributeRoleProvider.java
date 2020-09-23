/**
 * Copyright (c) 2003-2018 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.unboundid;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.RoleProvider;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;

/**
 * This just uses attributes set on the user by the LDAP provider to determine role
 * membership.
 * @author buckett
 *
 */
@Slf4j
public class UserAttributeRoleProvider implements RoleProvider {
	private String statusAttribute;
	
	private UserDirectoryService userDirectoryService;
	
	private Map<String,Set<String>> statusRoles;
	
	private Set<String> allRoles;
	
	private static final ResourceLoader rb = new ResourceLoader("UserAttributeRoleProvider");

	public void init() {
		if (statusRoles != null) {
			allRoles = new HashSet<String>();
			for (String key: statusRoles.keySet()) {
				Set<String> roles = statusRoles.get(key);
				allRoles.addAll(roles);
				statusRoles.put(key, Collections.unmodifiableSet(roles));
			}
			allRoles = Collections.unmodifiableSet(allRoles);
		} else {
			throw new IllegalStateException("statusRoles must be set");
		}
	}

	public Set<String> getAdditionalRoles(String userId) {
		if (userId != null) {
			try {
				User user = userDirectoryService.getUser(userId);
				String status = (String) user.getProperties().get(statusAttribute);
				if (status != null && status.length() > 0) {
					Set<String> roles = statusRoles.get(status);
					if (roles != null) {
						return roles;
					}
				}
			} catch (UserNotDefinedException e) {
				// This really shouldn't happen as this should only be called for known users
				log.warn("User couldn't be loaded to find additional roles: "+ userId, e);
			}
		}
		return Collections.emptySet();
	}

	public String getDisplayName(String role) {
		return rb.getString(role, null);
	}

	public Collection<String> getAllAdditionalRoles() {
		return allRoles;
	}

	public void setStatusAttribute(String statusAttribute) {
		this.statusAttribute = statusAttribute;
	}

	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	public void setStatusRoles(Map<String, Set<String>> statusRoles) {
		this.statusRoles = statusRoles;
	}

}
