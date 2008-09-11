/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright 2008 Sakai Foundation
*
* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*       http://www.osedu.org/licenses/ECL-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.coursemanagement.impl.provider;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class BaseRoleResolver implements RoleResolver {
	static final Log log = LogFactory.getLog(BaseRoleResolver.class);

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