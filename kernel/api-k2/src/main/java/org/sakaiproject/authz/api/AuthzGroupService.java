/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.authz.api;

import java.util.List;
import java.util.Map;

import org.sakaiproject.authz.api.exception.AuthzPermissionException;
import org.sakaiproject.authz.api.exception.GroupAlreadyDefinedException;
import org.sakaiproject.authz.api.exception.GroupFullException;
import org.sakaiproject.authz.api.exception.GroupIdInvalidException;
import org.sakaiproject.authz.api.exception.GroupNotDefinedException;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.javax.PagingPosition;

/**
 * <p>
 * AuthzGroupService manages authorization grops.
 * </p>
 */
public interface AuthzGroupService extends EntityProducer
{
	
	public static enum Permission implements Entity.Permission {
		/** Name for the event of adding an AuthzGroup. */
		SECURE_ADD_AUTHZ_GROUP("realm.add"),
		/** Name for the event of removing an AuthzGroup. */
		SECURE_REMOVE_AUTHZ_GROUP("realm.del"),
		/** Name for the event of updating an AuthzGroup. */
		SECURE_UPDATE_AUTHZ_GROUP("realm.upd"),
		/** Name for the event of updating ones own relationship in an AuthzGroup. */
		SECURE_UPDATE_OWN_AUTHZ_GROUP("realm.upd.own"),
		SECURE_JOIN_GROUP("realm.join"),
		SECURE_UNJOIN_GROUP("real.unjoin");
		
		private final String permission;

		private Permission(String permission) {
			this.permission = permission;
		}
		
		@Override
		public String toString() {
			return permission;
		}
		
	}
	
	public static enum GlobalRole {
		/** Standard role name for the anon. role. */
		ANON_ROLE(".anon"),
		/** Standard role name for the auth. role. */
		AUTH_ROLE(".auth");
		private final String role;

		private GlobalRole(String role) {
			this.role = role;
		}
		
		@Override
		public String toString() {
			return role;
		}
		
	}
	
	/** This string can be used to find the service in the service manager. */
	public static final String SERVICE_NAME = AuthzGroupService.class.getName();

	/** The type string for this application: should not change over time as it may be stored in various parts of persistent entities. */
	static final String APPLICATION_ID = "sakai:authzGroup";

	/** This string starts the references to resources in this service. */
	static final String REFERENCE_ROOT = "/realm";



	
	

	/**
	 * Access a list of AuthzGroups that meet specified criteria, naturally sorted.
	 * 
	 * @param criteria
	 *        Selection criteria: AuthzGroups returned will match this string somewhere in their id, or provider group id.
	 * @param page
	 *        The PagePosition subset of items to return.
	 * @return The List (AuthzGroup) that meet specified criteria.
	 */
	List getAuthzGroups(String criteria, PagingPosition page);

	/**
	 * Count the AuthzGroups that meet specified criteria.
	 * 
	 * @param criteria
	 *        Selection criteria: AuthzGroups returned will match this string somewhere in their id, or provider group id.
	 * @return The count of AuthzGroups that meet specified criteria.
	 */
	int countAuthzGroups(String criteria);

	/**
	 * Access an AuthzGroup.
	 * 
	 * @param id
	 *        The id string.
	 * @return The AuthzGroup.
	 * @exception GroupNotDefinedException
	 *            if not found.
	 */
	AuthzGroup getAuthzGroup(String id) throws GroupNotDefinedException;

	/**
	 * Save the changes made to the AuthzGroup. The AuthzGroup must already exist, and the user must have permission to update.
	 * 
	 * @param azGroup
	 *        The AuthzGroup to save.
	 * @exception GroupNotDefinedException
	 *            if the AuthzGroup id is not defined.
	 * @exception AuthzPermissionException
	 *            if the current user does not have permission to update the AuthzGroup.
	 */
	void save(AuthzGroup azGroup) throws GroupNotDefinedException, AuthzPermissionException;


	/**
	 * Add a new AuthzGroup
	 * 
	 * @param id
	 *        The AuthzGroup id.
	 * @return The new AuthzGroup.
	 * @exception GroupIdInvalidException
	 *            if the id is invalid.
	 * @exception GroupAlreadyDefinedException
	 *            if the id is already used.
	 * @exception AuthzPermissionException
	 *            if the current user does not have permission to add the AuthzGroup.
	 */
	AuthzGroup addAuthzGroup(String id) throws GroupIdInvalidException, GroupAlreadyDefinedException, AuthzPermissionException;

	/**
	 * Add a new AuthzGroup, as a copy of another AuthzGroup (except for id), and give a user "maintain" access based on the other's definition of "maintain".
	 * 
	 * @param id
	 *        The id.
	 * @param other
	 *        The AuthzGroup to copy into this new AuthzGroup.
	 * @param maintainUserId
	 *        Optional user id to get "maintain" access, or null if none.
	 * @return The new AuthzGroup object.
	 * @exception GroupIdInvalidException
	 *            if the id is invalid.
	 * @exception GroupAlreadyDefinedException
	 *            if the id is already used.
	 * @exception AuthzPermissionException
	 *            if the current user does not have permission to add the AuthzGroup.
	 */
	AuthzGroup addAuthzGroup(String id, AuthzGroup other, String maintainUserId) throws GroupIdInvalidException,
			GroupAlreadyDefinedException, AuthzPermissionException;


	/**
	 * Remove this AuthzGroup.
	 * 
	 * @param azGroup
	 *        The AuthzGroup to remove.
	 * @exception AuthzPermissionException
	 *            if the current user does not have permission to remove this AuthzGroup.
	 */
	void removeAuthzGroup(AuthzGroup azGroup) throws AuthzPermissionException;

	/**
	 * Remove the AuthzGroup with this id, if it exists (fails quietly if not).
	 * 
	 * @param id
	 *        The AuthzGroup id.
	 * @exception AuthzPermissionException
	 *            if the current user does not have permission to remove this AthzGroup.
	 */
	void removeAuthzGroup(String id) throws AuthzPermissionException;

	/**
	 * Access the internal reference which can be used to access the AuthzGroup from within the system.
	 * 
	 * @param id
	 *        The AuthzGroup id.
	 * @return The the internal reference which can be used to access the AuthzGroup from within the system.
	 */
	String authzGroupReference(String id);

	/**
	 * Cause the current user to join the given AuthzGroup with this role, using SECURE_UPDATE_OWN_AUTHZ_GROUP security.
	 * 
	 * @param authzGroupId
	 *        the id of the AuthzGroup.
	 * @param role
	 *        the name of the Role.
	 * @throws GroupNotDefinedException
	 *         if the authzGroupId or role are not defined.
	 * @throws AuthzPermissionException
	 *         if the current user does not have permission to join this AuthzGroup.
	 */
	void joinGroup(String authzGroupId, String role) throws GroupNotDefinedException, AuthzPermissionException;

	/**
	 * Cause the current user to join the given AuthzGroup with this role, using SECURE_UPDATE_OWN_AUTHZ_GROUP security, 
	 * provided that adding this user would not cause the group to exceed the specified size.
	 * 
	 * @param authzGroupId
	 *        the id of the AuthzGroup.
	 * @param role
	 *        the name of the Role.
	 * @param maxSize
	 *        the maximum permitted size of the AuthzGroup.
	 * @throws GroupNotDefinedException
	 *         if the authzGroupId or role are not defined.
	 * @throws AuthzPermissionException
	 *         if the current user does not have permission to join this AuthzGroup.
	 * @throws GroupFullException
	 *         if adding the current user would cause the AuthzGroup to become larger than maxSize.
	 */
	void joinGroup(String authzGroupId, String role, int maxSize) throws GroupNotDefinedException, AuthzPermissionException, GroupFullException;
	
	/**
	 * Cause the current user to unjoin the given AuthzGroup, using SECURE_UPDATE_OWN_AUTHZ_GROUP security.
	 * 
	 * @param authzGroupId
	 *        the id of the AuthzGroup.
	 * @throws GroupNotDefinedException
	 *         if the authzGroupId or role are not defined.
	 * @throws AuthzPermissionException
	 *         if the current user does not have permission to unjoin this site.
	 */
	void unjoinGroup(String authzGroupId) throws GroupNotDefinedException, AuthzPermissionException;

	/**
	 * Test if this user is allowed to perform the function in the named AuthzGroup.
	 * 
	 * @param userId
	 *        The user id.
	 * @param function
	 *        The function to open.
	 * @param azGroupId
	 *        The AuthzGroup id to consult, if it exists.
	 * @return true if this user is allowed to perform the function in the named AuthzGroup, false if not.
	 */
	boolean isAllowed(String userId, Entity.Permission function, String azGroupId);

	/**
	 * Test if this user is allowed to perform the function in the named AuthzGroups.
	 * 
	 * @param userId
	 *        The user id.
	 * @param function
	 *        The function to open.
	 * @param azGroups
	 *        A collection of AuthzGroup ids to consult.
	 * @return true if this user is allowed to perform the function in the named AuthzGroups, false if not.
	 */
	boolean isAllowed(String userId, Entity.Permission function, List azGroups);

	/**
	 * Get the set of user ids of users who are allowed to perform the function in the named AuthzGroups.
	 * 
	 * @param function
	 *        The function to check.
	 * @param azGroups
	 *        A collection of the ids of AuthzGroups to consult.
	 * @return the Set (String) of user ids of users who are allowed to perform the function in the named AuthzGroups.
	 */
	List getUsersIsAllowed(Entity.Permission function, List azGroups);

	/**
	 * Get the set of AuthzGroup ids in which this user is allowed to perform this function.
	 * 
	 * @param userId
	 *        The user id.
	 * @param function
	 *        The function to check.
	 * @param azGroups
	 *        The Collection of AuthzGroup ids to search; if null, search them all.
	 * @return the Set (String) of AuthzGroup ids in which this user is allowed to perform this function.
	 */
	List getAuthzGroupsIsAllowed(String userId, Entity.Permission function, List azGroups);

	/**
	 * Get the set of functions that users with this role in these AuthzGroups are allowed to perform.
	 * 
	 * @param role
	 *        The role name.
	 * @param azGroups
	 *        A collection of AuthzGroup ids to consult.
	 * @return the Set (String) of functions that users with this role in these AuthzGroups are allowed to perform
	 */
	List getAllowedFunctions(String role, List azGroups);

	/**
	 * Get the role name for this user in this AuthzGroup, if the user has membership (the membership gives the user a single role).
	 * 
	 * @param userId
	 *        The user id.
	 * @param function
	 *        The function to open.
	 * @param azGroupId
	 *        The AuthzGroup id to consult, if it exists.
	 * @return the role name for this user in this AuthzGroup, if the user has active membership, or null if not.
	 */
	String getUserRole(String userId, String azGroupId);

	/**
	 * Get the role name for each user in the userIds Collection in this AuthzGroup, for each of these users who have membership (membership gives the user a single role).
	 * 
	 * @param userIds
	 *        The user ids as a Collection of String.
	 * @param function
	 *        The function to open.
	 * @param azGroupId
	 *        The AuthzGroup id to consult, if it exists.
	 * @return A Map (userId (String) -> role name (String)) of role names for each user who have active membership; if the user does not, it will not be in the Map.
	 */
	Map getUsersRole(List userIds, String azGroupId);

	/**
	 * Refresh this user's AuthzGroup external definitions.
	 * 
	 * @param userId
	 *        The user id.
	 */
	void refreshUser(String userId);

	/**
	 * Create a new AuthzGroup, as a copy of another AuthzGroup (except for id), and give a user "maintain" access based on the other's definition of "maintain", but do not store - it can be saved with a save() call
	 * 
	 * @param id
	 *        The id.
	 * @param other
	 *        The AuthzGroup to copy into this new AuthzGroup (or null if none).
	 * @param maintainUserId
	 *        Optional user id to get "maintain" access, or null if none.
	 * @return The new AuthzGroup object.
	 * @exception GroupAlreadyDefinedException
	 *            if the id is already used.
	 */
	AuthzGroup newAuthzGroup(String id, AuthzGroup other, String maintainUserId) throws GroupAlreadyDefinedException;
	
	/**
	 * Gets the IDs of the AuthzGroups with the given provider ID.
	 * 
	 * @return The Set of Strings representing authzGroup IDs (such as /site/1234 or /site/1234/group/5678) for all authzGroups with the given providerId.
	 */
	public List getAuthzGroupIds(String providerId);

	/**
	 * Gets the provider IDs associated with an AuthzGroup.
	 * 
	 * @return The Set of Strings representing external group IDs, as recognized by the GroupProvider implementation, that are associated with the given groupId. These strings
	 * must not be "compound IDs", as defined by the GroupProvider's String[] unpackId(String id) method.
	 */
	public List getProviderIds(String authzGroupId); 
}
