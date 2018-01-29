/**
 * Copyright (c) 2006-2016 The Apereo Foundation
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

package org.sakaiproject.coursemanagement.impl.provider;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseRoleResolver implements RoleResolver {

	/** Map of CM section roles to Sakai roles */
	protected Map<String, String> roleMap;
	
	/** Map to support external service configuration */
	protected Map<String, Object> configuration;

	/**
	 * Converts a CM role to a Sakai role.
	 * 
	 * @param cmRole The role according to CM
	 * @return The role to use in a Sakai site or group, or null if the CM role should
	 * not be expressed as a role in a Sakai site or group.
	 */
	protected String convertRole(String cmRole) {
		if (cmRole == null) {
			log.warn("Can not convert CM role 'null' to a sakai role.");
			return null;
		}
		String sakaiRole = (String)roleMap.get(cmRole);
		if(sakaiRole== null) {
			log.warn("Unable to find sakai role for CM role " + cmRole);
			return null;
		} else {
			return sakaiRole;
		}
	}

	public void setRoleMap(Map<String, String> roleMap) {
		this.roleMap = roleMap;
	}

	/**
	 * Support external configuration of the service.
	 * 
	 * @param configuration
	 */
	public void setConfiguration(Map<String, Object> configuration) {
		this.configuration = configuration;
	}

}
