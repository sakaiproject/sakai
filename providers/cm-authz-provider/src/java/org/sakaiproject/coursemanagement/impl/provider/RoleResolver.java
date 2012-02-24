/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.coursemanagement.impl.provider;

import java.util.Map;

import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.Section;

/**
 * Resolves users roles in CM objects.
 */
public interface RoleResolver {

	/**
	 * Gets users roles in a CM object.  A RoleResolver implementation
	 * will typically use the cmService to look "up" from the section in the CM
	 * hierarchy to find the object it's interested in, then find any membership roles
	 * associated with the user.
	 * 
	 * @param section The section from which to start searching "up" the hierarchy,
	 * if necessary
	 * @param cmService The CM service impl.  We pass this in rather than injecting
	 * it into every RoleResolver
	 * 
	 * @return The user's role, or null if the user has no role in this CM object
	 */
	public Map<String, String> getUserRoles(CourseManagementService cmService, Section section);

	/**
	 * Gets a single user's roles in all sections with which s/he is associated.
	 * 
	 * @param userEid The user's enterprise ID
	 * @param cmService The CM service impl.  We pass this in rather than injecting
	 * it into every RoleResolver
	 * 
	 * @return The user's roles, or null if the user has no role in this CM object
	 */
	public Map<String, String> getGroupRoles(CourseManagementService cmService, String userEid);
}
