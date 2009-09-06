/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.sakaiproject.authz.api;

import java.util.Map;

/**
 * <p>
 * GroupProvider provides user / role membership in a group, from an external system.
 * </p>
 */
public interface GroupProvider
{
	/**
	 * Access the role name for this particular user in the external group.
	 * 
	 * @param id
	 *        The external group id.
	 * @param userId
	 *        The user Id.
	 * @return the role name for this particular user in the external group, or null if none.
	 */
	String getRole(String id, String user);

	/**
	 * Access the user id - role name map for all users in the external group.
	 * 
	 * @param id
	 *        The external group id.
	 * @return the user id - role name map for all users in the external group (may be empty).
	 */
	Map<String, String> getUserRolesForGroup(String id);

	/**
	 * Access the external group id - role name map for this user in all external groups.
	 * 
	 * @param userId
	 *        The user id.
	 * @return the the external group id - role name map for this users in all external groups. (may be empty).
	 */
	Map<String, String> getGroupRolesForUser(String userId);

	/**
	 * Packs any number of simple ids into a compound id.
	 * @param ids
	 * 		The external group ids
	 * @return a compound id
	 */
	public String packId(String[] ids);
	
	/**
	 * Unpack a possibly compound id into it's component ids, returning at least the id unchanged if not compound.
	 * 
	 * @param id
	 *        The external realm id.
	 * @return a String array of one or more ids upacked from this possibly compound id.
	 */
	String[] unpackId(String id);
	
	/**
	 * Return one or the other of these role names - pick the one that if a user has both, is the more powerful one to give the user as their single role.
	 * 
	 * @param one
	 *        A role name. May be null!
	 * @param other
	 *        Another role name. May be null!
	 * @return The better role.
	 */
	String preferredRole(String one, String other);
}
