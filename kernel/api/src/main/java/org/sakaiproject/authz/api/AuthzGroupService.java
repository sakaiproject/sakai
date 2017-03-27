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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.authz.api;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.javax.PagingPosition;

/**
 * <p>
 * AuthzGroupService manages authorization groups.
 * This service allows you to check if a user is allowed to perform a particular function in a
 * context.
 * </p>
 * <p>
 * This service doesn't do any checking that the the user IDs that are return are still valid. As if a user
 * is deleted or no longer exists in a provider the entries in the AuthzGroupService will still exist. If 
 * the calling code needs to make sure a user is valid it should check with the {@see org.sakaiproject.user.api.UserDirectoryService}.
 * Ideally deleted user records should get tidied up, but at the moment that don't.
 * </p>
 */
public interface AuthzGroupService extends EntityProducer
{
	/** The type string for this application: should not change over time as it may be stored in various parts of persistent entities. */
	static final String APPLICATION_ID = "sakai:authzGroup";

	/** This string starts the references to resources in this service. */
	static final String REFERENCE_ROOT = "/realm";

	/** Name for the event of adding an AuthzGroup. */
	static final String SECURE_ADD_AUTHZ_GROUP = "realm.add";

	/** Name for the event of removing an AuthzGroup. */
	static final String SECURE_REMOVE_AUTHZ_GROUP = "realm.del";

	/** Name for the event of updating an AuthzGroup. */
	static final String SECURE_UPDATE_AUTHZ_GROUP = "realm.upd";

	/** Name for the event of joining an AuthzGroup. */
	static final String SECURE_JOIN_AUTHZ_GROUP = "realm.join";

	/** Name for the event of unjoining an AuthzGroup. */
	static final String SECURE_UNJOIN_AUTHZ_GROUP = "realm.unjoin";

	/** Name for the event of viewing all AuthzGroups. */
	static final String SECURE_VIEW_ALL_AUTHZ_GROUPS = "realm.view.all";
	
	/** Name for the event of updating ones own relationship in an AuthzGroup. */
	static final String SECURE_UPDATE_OWN_AUTHZ_GROUP = "realm.upd.own";

	/** Standard role name for the anon. role. */
	static final String ANON_ROLE = ".anon";

	/** Standard role name for the auth. role. */
	static final String AUTH_ROLE = ".auth";

	/**
	 * Get all provider IDs for the realms given.
	 *
	 * @param realmIDs a List of the realms you want the provider IDs for.
	 * @return a Map, where the key is the realm ID, and the value is a List of Strings of provider IDs for that realm
	 */
	public Map<String, List<String>> getProviderIDsForRealms(List<String> realmIDs);

	/**
	 * Access a list of AuthzGroups that meet specified criteria, naturally sorted.
	 * NOTE: The group objects returned will not have the associated roles loaded.
	 * if you need to save the realm retrieve it with {@link #getAuthzGroup(String)}
	 * 
	 * @param criteria
	 *        Selection criteria: AuthzGroups returned will match this string somewhere in their id, or provider group id.
	 * @param page
	 *        The PagePosition subset of items to return.
	 * @return The List (AuthzGroup) that meet specified criteria.
	 */
	List<AuthzGroup> getAuthzGroups(String criteria, PagingPosition page);

	/**
	 * Access a list of AuthzGroups which contain a specified userid
	 * NOTE: This call is backed by a cache.
	 * 
	 * @param authzGroupIds
	 *        AuthzGroup selection criteria (list of AuthzGroup ids)
	 * @param userid
	 *        Return only groups with userid as a member
	 * @return The List (AuthzGroup) that contain the specified userid
	 */
	List<AuthzGroup> getAuthzUserGroupIds(ArrayList<String> authzGroupIds, String userid);

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
	 * Check permissions for updating an AuthzGroup.
	 * 
	 * @param id
	 *        The id.
	 * @return true if the user is allowed to update the AuthzGroup, false if not.
	 */
	boolean allowUpdate(String id);

	/**
	 * Save the changes made to the AuthzGroup. The AuthzGroup must already exist, and the user must have permission to update.
	 *
	 * A side-effect of this call is to refresh current memberships based on
	 * the state of the group's associated provider, if any. (For example, to
	 * update site memberships based on course enrollment records.) Since there
	 * is no other public method that refreshes group memberships, this method
	 * may be useful even to clients who don't edit the AuthzGroup directly.
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
	 * Check permissions for adding an AuthzGroup.
	 * 
	 * @param id
	 *        The authzGroup id.
	 * @return true if the current user is allowed add the AuthzGroup, false if not.
	 */
	boolean allowAdd(String id);

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
	 * Check permissions for removing an AuthzGroup.
	 * 
	 * @param id
	 *        The AuthzGroup id.
	 * @return true if the user is allowed to remove the AuthzGroup, false if not.
	 */
	boolean allowRemove(String id);

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
	 * Check permissions for the current user joining a group.
	 * 
	 * @param id
	 *        The AuthzGroup id.
	 * @return true if the user is allowed to join the group, false if not.
	 */
	boolean allowJoinGroup(String id);

	/**
	 * Check permissions for the current user unjoining a group.
	 * 
	 * @param id
	 *        The AuthzGroup id.
	 * @return true if the user is allowed to unjoin the group, false if not.
	 */
	boolean allowUnjoinGroup(String id);

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
	boolean isAllowed(String userId, String function, String azGroupId);

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
	boolean isAllowed(String userId, String function, Collection<String> azGroups);
	
	/**
	 * Encode the role id to form the dummy user id that will be used to perform role checks.
	 * We check role access by performing an authz check against a dummy so that we don't have to load
	 * all of the roles and iterate through them and also to take advantage of caching.
	 * 
	 * @param roleId the string id of the role to encode
	 * @return a dummy user id which will pass authz checks for this role
	 * @throws IllegalArgumentException if no roleId is provided
	 */
	String encodeDummyUserForRole(String roleId) throws IllegalArgumentException;
	
	/**
	 * Decodes the dummy user id to provide the original roleId as encoded by encodeDummyUserForRole(String)
	 * @see AuthzGroupService#encodeDummyUserForRole(String)
	 * 
	 * @param dummyUserId the string id of the dummy user to decode
	 * @return the decoded role, will return <code>null</code> if it could not be decoded.
	 * @throws IllegalArgumentException if no dummy user id is provided.
	 */
	String decodeRoleFromDummyUser(String dummyUserId) throws IllegalArgumentException;

	/**
	 * Get the set of user ids of users who are allowed to perform the function in the named AuthzGroups.
	 * 
	 * @see AuthzGroupService For details on deleted users.
	 * @param function
	 *        The function to check.
	 * @param azGroups
	 *        A collection of the ids of AuthzGroups to consult.
	 * @return the Set (String) of user ids of users who are allowed to perform the function in the named AuthzGroups.
	 */
	Set<String> getUsersIsAllowed(String function, Collection<String> azGroups);

	/**
	 * Get the set of user ids per group of users who are allowed to perform the function in the named AuthzGroups.
	 * Use this method to get permission-related membership information from a set of groups efficiently, 
	 * rather than iterating through each group.
	 * 
	 * @see AuthzGroupService For details on deleted users.
	 * @param function
	 *        The function to check.
	 * @param azGroups
	 *        A collection of the ids of AuthzGroups to consult; if null, search them all (use with care).
	 * @return A Set of String arrays (userid, realm) with user ids per group who are allowed to perform the function.
	 */
	Set<String[]> getUsersIsAllowedByGroup(String function, Collection<String> azGroups);

	/**
	 * Get the number of users per group who are allowed to perform the function in the given AuthzGroups.
	 * Use this method to get permission-related size information from a set of groups efficiently, 
	 * rather than iterating through each group.
	 * 
	 * @see AuthzGroupService For details on deleted users.
	 * @param function
	 *        The function to check.
	 * @param azGroups
	 *        A collection of the ids of AuthzGroups to search; if null, search them all (use with care).
	 * @return A Map (authzgroupid (String) -> user count (Integer) ) of the number of users who are allowed to perform the function.
	 */
	Map<String,Integer> getUserCountIsAllowed(String function, Collection<String> azGroups);

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
	Set<String> getAuthzGroupsIsAllowed(String userId, String function, Collection<String> azGroups);

	/**
	 * Get the set of functions that users with this role in these AuthzGroups are allowed to perform.
	 * 
	 * @param role
	 *        The role name.
	 * @param azGroups
	 *        A collection of AuthzGroup ids to consult.
	 * @return the Set (String) of functions that users with this role in these AuthzGroups are allowed to perform
	 */
	Set<String> getAllowedFunctions(String role, Collection<String> azGroups);

	/**
	 * Get the role name for this user in this AuthzGroup, if the user has membership (the membership gives the user a single role).
	 * 
	 * @param userId
	 *        The user id.
	 * @param azGroupId
	 *        The AuthzGroup id to consult, if it exists.
	 * @return the role name for this user in this AuthzGroup, if the user has active membership, or null if not.
	 */
	String getUserRole(String userId, String azGroupId);

	/**
	 * Get all role names for a given user in a set of AuthzGroups.
	 *
	 * @param userId
	 *        The user ID of the person to search for.
	 * @param azGroupIds
	 *        A collection of AuthzGroup IDs to narrow the search (may be empty or null to search all).
	 * @return A Map<String, String> (AuthzGroup ID -> role name) for every AuthzGroup where the user is a member, filtered to the set of AuthzGroups in azGroupIds (if non-null and non-empty).
	 *
	 */
	Map<String, String> getUserRoles(String userId, Collection<String> azGroupIds);

	/**
	 * Get the role name for each user in the userIds Collection in this AuthzGroup, for each of these users who have membership (membership gives the user a single role).
	 * 
	 * @param userIds
	 *        The user ids as a Collection of String.
	 * @param azGroupId
	 *        The AuthzGroup id to consult, if it exists.
	 * @return A Map (userId (String) -> role name (String)) of role names for each user who have active membership; if the user does not, it will not be in the Map.
	 */
	Map<String, String> getUsersRole(Collection<String> userIds, String azGroupId);

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
	public Set<String> getAuthzGroupIds(String providerId);

	/**
	 * Gets the provider IDs associated with an AuthzGroup.
	 * 
	 * @return The Set of Strings representing external group IDs, as recognized by the GroupProvider implementation, that are associated with the given groupId. These strings
	 * must not be "compound IDs", as defined by the GroupProvider's String[] unpackId(String id) method.
	 */
	public Set<String> getProviderIds(String authzGroupId); 

    /**
     * Get list of users who are in a set of groups
     * 
     * @param groupIds IDs of authZ groups (AuthzGroup selection criteria)
     * @return list of user IDs who are in a set of groups
     */
    public Collection<String> getAuthzUsersInGroups(Set<String> groupIds);

    /**
     * Registers a AuthzGroupAdvisor with the AuthzGroupService. Each advisor will be
     * called during save(AuthzGroup).
     * 
     * @param advisor The AuthzGroupAdvisor to add 
     */
    public void addAuthzGroupAdvisor(AuthzGroupAdvisor advisor);
    
    /**
     * Removes an AuthzGroupAdvisor
     * 
     * @param advisor The AuthzGroupAdvisor to remove
     * @return Whether a AuthzGroupAdvisor was previously registered and hence removed
     */
    public boolean removeAuthzGroupAdvisor(AuthzGroupAdvisor advisor);
    
    /**
     * List of the current AuthzGroupAdvisors registered with the AuthzGroupService
     * 
     * @return List containing the currently registered AuthzGroupAdvisors
     */
    public List<AuthzGroupAdvisor> getAuthzGroupAdvisors();
    
    /**
     * Gets a set of additional roles that can be added to an authz group. These roles shouldn't be assigned to members but
     * users are part of the role through some other means (eg being a member of staff).
     * @return The set of role IDs that can be used, if no additional roles can be granted it should return an empty set.
     */
    public Set<String> getAdditionalRoles();

    /**
     * Check if the supplied role can be assigned to a user.
     * @param roleId The role ID to check.
     * @return <code>true</code> if the role can be assigned to a user.
     */
    public boolean isRoleAssignable(String roleId);
	
	/**
	 * Get a nice display name for role. 
	 * @param roleId The role ID to check (eg .auth)
	 * @return A display name for the role, if there is no better name the original roleId should be returned.
	 */
	public String getRoleName(String roleId);
	
	/**
	 * Get a nice display name for role group. 
	 * @param roleGroupId The role group ID to check. Empty for generic name.
	 * @return A display name for the role group, if there is no better name the original roleGroupId should be returned.
	 */
	public String getRoleGroupName(String roleGroupId);

    /**
     * Set of all maintain roles
     *
     * @return String Set containing all maintain roles.
     */
    public Set<String> getMaintainRoles();
}
