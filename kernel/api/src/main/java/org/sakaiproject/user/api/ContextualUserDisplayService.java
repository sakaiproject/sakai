/**
 * Copyright (c) 2003-2012 The Apereo Foundation
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

package org.sakaiproject.user.api;

/**
 * Optional service to override default display IDs and names with context-specific values,
 * particularly site-specific aliases.
 * 
 * This interface will not typically be implemented by user directory providers. The user
 * directory provider connects a user identifier to user data maintained by an external
 * source such as LDAP or SIS. Such implementations tend to be institution specific.
 * In contrast, site-specific and tool-specific aliases and nicknames will typically be
 * enabled and managed by adding generic profile utilities to a site.
 * <p>
 * WARNING: This service will be called on every access of a relevant User object's
 * "getDisplayName()" and "getDisplayId()"! It's very important for implementers to
 * include caching, since the User Directory Service itself won't cache these fields.
 */
public interface ContextualUserDisplayService {
	/**
	 * Based on the current runtime environment, find a context-specific humanly-understandable
	 * disambiguating ID for the specified user.
	 * 
	 * @param user
	 * @return context-specific ID, or null if the feature doesn't apply to this
	 *   user in the current runtime environment
	 */
	String getUserDisplayId(User user);
	
	/**
	 * Based on the current runtime environment, find a context-specific full name
	 * to display for the specified user. 
	 * 
	 * @param user
	 * @return context-specific name, or null if the feature doesn't apply to this
	 *   user in the current runtime environment
	 */
	String getUserDisplayName(User user);

	/**
	 * Return a context-specific humanly-understandable disambiguating ID for the
	 * specified user. 
	 * 
	 * @param user
	 * @param contextReference the entity reference to be used to determine the context;
	 *   the only currently known service implementation expects a Site ID
	 * @return context-specific ID, or null if the feature doesn't apply to this
	 *   user and context
	 */
	String getUserDisplayId(User user, String contextReference);
	
	/**
	 * Return a context-specific full name to display for the specified user. 
	 * 
	 * @param user
	 * @param contextReference the entity reference to be used to determine the context;
	 *   the only currently known service implementation expects a Site ID
	 * @return context-specific name, or null if the feature doesn't apply to this
	 *   user and context
	 */
	String getUserDisplayName(User user, String contextReference);
}
