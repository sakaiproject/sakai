/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * AuthzGroup is a authorization group; a group of users, each with a role, and a set of permissions of functions made to each role.
 * </p>
 * <p>
 * AuthzGroups can related to Entities in Sakai; The entity reference forms the AuthzGroup id.
 * </p>
 * <p>
 * Special AuthzGroups not related to an entity have ids that begin with a "!".
 * </p>
 */
public interface AuthzGroup extends Edit, Comparable, Serializable
{
	/**
	 * Add a member to the AuthzGroup, or, if the user is currently a member, update
     * the membership with the supplied details.
	 * 
	 * @param userId
	 *        The user.
	 * @param roleId
	 *        The role name. If no role with this name exists in the group, an IllegalArgumentException will be thrown.
	 * @param active
	 *        The active flag.
	 * @param provided
	 *        If true, from an external provider.
	 */
	void addMember(String userId, String roleId, boolean active, boolean provided);

	/**
	 * Create a new Role within this AuthzGroup.
	 * 
	 * @param id
	 *        The role id.
	 * @return the new Role.
	 * @exception IdUsedException
	 *            if the id is already a Role in this AuthzGroup.
	 */
	Role addRole(String id) throws RoleAlreadyDefinedException;

	/**
	 * Create a new Role within this AuthzGroup, as a copy of this other role
	 * 
	 * @param id
	 *        The role id.
	 * @param other
	 *        The role to copy.
	 * @return the new Role.
	 * @exception IdUsedException
	 *            if the id is already a Role in this AuthzGroup.
	 */
	Role addRole(String id, Role other) throws RoleAlreadyDefinedException;

	/**
	 * @return the user who created this.
	 * 
	 */
	User getCreatedBy();

	/**
	 * @return the time created.
	 * @deprecated use {#link {@link #getCreatedDate()}
	 */
	Time getCreatedTime();
	
	/**
	 * Get the date created
	 * @return
	 */
	Date getCreatedDate();

	/**
	 * @return a description of the item this realm applies to.
	 */
	String getDescription();

	/**
	 * Access the name of the role to use for giving a user membership with "maintain" access.
	 * 
	 * @return The name of the "maintain" role.
	 */
	public String getMaintainRole();

	/**
	 * Access the user's membership record for this AuthzGroup; the role, and status flags.
	 * 
	 * @param userId
	 *        The user id.
	 * @return The Membership record for the user in this AuthzGroup, or null if the use is not a member.
	 */
	public Member getMember(String userId);

	/**
	 * Access all Membership records defined for this AuthzGroup.
	 * 
	 * @return The set of Membership records (Membership) defined for this AuthzGroup.
	 */
	public Set<Member> getMembers();

	/**
	 * @return the user who last modified this.
	 */
	User getModifiedBy();

	/**
	 * @return the time last modified.
	 * @deprecated see {@link #getModifiedDate()}
	 */
	Time getModifiedTime();
	
	/**
	 * Get date last modified
	 * @return
	 */
	Date getModifiedDate();

	/**
	 * Access the group id for the GroupProvider for this AuthzGroup.
	 * 
	 * @return The the group id for the GroupProvider for this AuthzGroup, or null if none defined.
	 */
	public String getProviderGroupId();

	/**
	 * Access a Role defined in this AuthzGroup.
	 * 
	 * @param id
	 *        The role id.
	 * @return The Role, if found, or null, if not.
	 */
	public Role getRole(String id);

	/**
	 * Access all Roles defined for this AuthzGroup.
	 * 
	 * @return The set of roles (Role) defined for this AuthzGroup.
	 */
	public Set<Role> getRoles();

	/**
	 * Access all roles that have been granted permission to this function.
	 * 
	 * @param function
	 *        The function to check.
	 * @return The Set of role names (String) that have been granted permission to this function.
	 */
	public Set<String> getRolesIsAllowed(String function);

	/**
	 * Access the active role for this user's membership.
	 * 
	 * @param userId
	 *        The user id.
	 * @return The Role for this user's membership, or null if the user has no active membership.
	 */
	public Role getUserRole(String userId);

	/**
	 * Access all users who have active role membership in the AuthzGroup.
	 * 
	 * @return The Set of users ids (String) who have active role membership in the AuthzGroup.
	 */
	public Set<String> getUsers();

	/**
	 * Access all users who have an active role membership with this role.
	 * 
	 * @return The Set of user ids (String) who have an active role membership with this role.
	 */
	public Set<String> getUsersHasRole(String role);

	/**
	 * Access all users who have an active role membership to a role that is allowed this function.
	 * 
	 * @param function
	 *        The function to check.
	 * @return The Set of user ids (String) who have an active role membership to a role that is allowed this function.
	 */
	public Set<String> getUsersIsAllowed(String function);

	/**
	 * Test if this user has a membership in this AuthzGroup that has this role and is active.
	 * 
	 * @param userId
	 *        The user id.
	 * @param role
	 *        The role name.
	 * @return true if the User has has a membership in this AuthzGroup that has this role and is active.
	 */
	boolean hasRole(String userId, String role);

	/**
	 * Test if this user is allowed to perform the function in this AuthzGroup.
	 * 
	 * @param userId
	 *        The user id.
	 * @param function
	 *        The function to open.
	 * @return true if this user is allowed to perform the function in this AuthzGroup, false if not.
	 */
	boolean isAllowed(String userId, String function);

	/**
	 * Is this AuthzGroup empty of any roles or membership?
	 * 
	 * @return true if the AuthzGroup is empty, false if not.
	 */
	public boolean isEmpty();

	/**
	 * Remove membership for for this user from the AuthzGroup.
	 * 
	 * @param userId
	 *        The user.
	 */
	void removeMember(String userId);

	/**
	 * Remove all membership from this AuthzGroup.
	 */
	void removeMembers();

	/**
	 * Remove this Role from this AuthzGroup. Any grants of this Role in the AuthzGroup are also removed.
	 * 
	 * @param role
	 *        The role name.
	 */
	void removeRole(String role);

	/**
	 * Remove all Roles from this AuthzGroup.
	 */
	void removeRoles();

	/**
	 * Set the role name to use for "maintain" access.
	 * 
	 * @param role
	 *        The name of the "maintain" role.
	 */
	void setMaintainRole(String role);

	/**
	 * Set the external group id for the GroupProvider for this AuthzGroup (set to null to have none).
	 * 
	 * @param id
	 *        The external group id for the GroupProvider, or null if there is to be none.
	 */
	void setProviderGroupId(String id);

	/**
	 * Adjust membership so that active members are all active in other, and inactive members are all defined in other
	 * 
	 * @param other
	 *        The other azg to adjust to.
	 * @return true if any changes were made, false if not.
	 */
	boolean keepIntersection(AuthzGroup other);
}
