/**
 * Copyright (c) 2003-2015 The Apereo Foundation
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
package org.sakaiproject.authz.api;

import java.util.Collection;
import java.util.Set;

/**
 * This provider allows a user to belong to additional roles other than
 * the standard .auth/.anon supported by Sakai out of the box. These roles
 * can be added to a site to allow large numbers of users to access it.
 * Just like .auth/.anon these people who get access through the roles won't
 * show up as members of the site.
 * 
 * @author buckett
 *
 */
public interface RoleProvider {

	/**
	 * Get a set of additional roles for the user.
	 * @param userId
	 * @return An empty set of a set of additional roles.
	 * It should never return <code>null</code>.
	 */
	public Set<String> getAdditionalRoles(String userId);
	
	/**
	 * Get a nice user visible display name for a role ID.
	 * @param role The role we want a display name for.
	 * @return The nice display name. If it doesn't know about the role then it should return <code>null</code>.
	 */
	public String getDisplayName(String role);
	
	/**
	 * Get all the additional roles available. This is designed to allow
	 * user interfaces to provide lists of available roles.
	 * @return A Collection of role IDs.
	 */
	public Collection<String> getAllAdditionalRoles();
}
