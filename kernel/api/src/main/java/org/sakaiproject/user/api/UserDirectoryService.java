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

package org.sakaiproject.user.api;

import java.util.Collection;
import java.util.List;

import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.ResourceProperties;
import org.w3c.dom.Element;

/**
 * <p>
 * UserDirectoryService manages the end-user modeling for Sakai.
 * </p>
 */
public interface UserDirectoryService extends EntityProducer
{
	/** The type string for this application: should not change over time as it may be stored in various parts of persistent entities. */
	static final String APPLICATION_ID = "sakai:user";

	/** This string starts the references to resources in this service. */
	static final String REFERENCE_ROOT = "/user";

	/** Name for the event of adding a group. */
	static final String SECURE_ADD_USER = "user.add";

	/** Name for the event of removing a user. */
	static final String SECURE_REMOVE_USER = "user.del";

	/** Name for the event of updating any user info. */
	static final String SECURE_UPDATE_USER_ANY = "user.upd.any";

	/** Name for the event of updating one's own user info. */
	static final String SECURE_UPDATE_USER_OWN = "user.upd.own";
	
	/** Name for the ability for updating one's own name. */
	static final String SECURE_UPDATE_USER_OWN_NAME = "user.upd.own.name";
	
	/** Name for the ability for updating one's own email. */
	static final String SECURE_UPDATE_USER_OWN_EMAIL = "user.upd.own.email";

	/** Name for the ability for updating one's own password. */
	static final String SECURE_UPDATE_USER_OWN_PASSWORD = "user.upd.own.passwd";

	/** Name for the ability for updating one's own type. */
	static final String SECURE_UPDATE_USER_OWN_TYPE = "user.upd.own.type";

	
	
	/** User id for the admin user. */
	static final String ADMIN_ID = "admin";

	/** User eid for the admin user. */
	static final String ADMIN_EID = "admin";

	/**
	 * Add a new user to the directory. Must commitEdit() to make official, or cancelEdit() when done! Id is auto-generated.
	 * 
	 * @param id
	 *        The user uuid string. Leave null for auto-assignment.
	 * @param eid
	 *        The user eid.
	 * @return A locked UserEdit object (reserving the id).
	 * @exception UserIdInvalidException
	 *            if the user eid is invalid.
	 * @exception UserAlreadyDefinedException
	 *            if the user id or eid is already used.
	 * @exception UserPermissionException
	 *            if the current user does not have permission to add a user.
	 */
	UserEdit addUser(String id, String eid) throws UserIdInvalidException, UserAlreadyDefinedException, UserPermissionException;

	/**
	 * Add a new user to the directory, complete in one operation. Id is auto-generated.
	 * 
	 * @param id
	 *        The user uuid string. Leave null for auto-assignment.
	 * @param eid
	 *        The user eid.
	 * @param firstName
	 *        The user first name.
	 * @param lastName
	 *        The user last name.
	 * @param email
	 *        The user email.
	 * @param pw
	 *        The user password.
	 * @param type
	 *        The user type.
	 * @param properties
	 *        Other user properties.
	 * @return The User object created.
	 * @exception UserIdInvalidException
	 *            if the user eid is invalid.
	 * @exception UserAlreadyDefinedException
	 *            if the user eid is already used.
	 * @exception UserPermissionException
	 *            if the current user does not have permission to add a user.
	 */
	User addUser(String id, String eid, String firstName, String lastName, String email, String pw, String type,
			ResourceProperties properties) throws UserIdInvalidException, UserAlreadyDefinedException, UserPermissionException;

	/**
	 * check permissions for addUser().
	 * 
	 * @return true if the user is allowed to add a user, false if not.
	 */
	boolean allowAddUser();

	/**
	 * check permissions for removeUser().
	 * 
	 * @param id
	 *        The group id.
	 * @return true if the user is allowed to removeUser(id), false if not.
	 */
	boolean allowRemoveUser(String id);

	/**
	 * check permissions for editUser()
	 * 
	 * @param id
	 *        The user id.
	 * @return true if the user is allowed to update the user, false if not.
	 */
	boolean allowUpdateUser(String id);

	/**
	 * check permissions for editUser()
	 * 
	 * @param id
	 *        The user id.
	 * @return true if the user is allowed to update their own first and last names, false if not.
	 */
	public boolean allowUpdateUserName(String id);
	
	/**
	 * check permissions for editUser()
	 * 
	 * @param id
	 *        The user id.
	 * @return true if the user is allowed to update their own email address, false if not.
	 */
	public boolean allowUpdateUserEmail(String id);
	
	/**
	 * check permissions for editUser()
	 * 
	 * @param id
	 *        The user id.
	 * @return true if the user is allowed to update their own password, false if not.
	 */
	public boolean allowUpdateUserPassword(String id);
	
	/**
	 * check permissions for editUser()
	 * 
	 * @param id
	 *        The user id.
	 * @return true if the user is allowed to update their own type, false if not.
	 */
	public boolean allowUpdateUserType(String id);

	/**
	 * Authenticate a user / password.
	 * 
	 * @param loginId
	 *        The string identifying the user to the authentication system.
	 *        If authenticated by basic Sakai services, this will be the user
	 *        record's EID.
	 *        If authenticated by a provider, it may or may not be equal to
	 *        the EID.
	 * @param password
	 *        The password.
	 * @return The User object of the authenticated user if successfull, null if not.
	 */
	User authenticate(String loginId, String password);

	/**
	 * Cancel the changes made to a UserEdit object, and release the lock. The UserEdit is disabled, and not to be used after this call.
	 * 
	 * @param user
	 *        The UserEdit object to commit.
	 */
	void cancelEdit(UserEdit user);

	/**
	 * Commit the changes made to a UserEdit object, and release the lock. The UserEdit is disabled, and not to be used after this call.
	 * 
	 * @param user
	 *        The UserEdit object to commit.
	 * @exception UserAlreadyDefinedException
	 *            if the User eid is already in use by another User object.
	 */
	void commitEdit(UserEdit user) throws UserAlreadyDefinedException;

	/**
	 * Count all the users that match this criteria in id or target, first or last name.
	 * 
	 * @return The count of all users matching the criteria.
	 */
	int countSearchUsers(String criteria);

	/**
	 * Count all the users.
	 * 
	 * @return The count of all users.
	 */
	int countUsers();

	/**
	 * Remove authentication for the current user.
	 * 
	 * @deprecated Unused; will likely be removed from the interface
	 */
	void destroyAuthentication();

	/**
	 * Get a locked user object for editing. Must commitEdit() to make official, or cancelEdit() when done!
	 * 
	 * @param id
	 *        The user id string.
	 * @return A UserEdit object for editing.
	 * @exception UserNotDefinedException
	 *            if not found, or if not an UserEdit object
	 * @exception UserPermissionException
	 *            if the current user does not have permission to mess with this user.
	 * @exception UserLockedException
	 *            if the User object is locked by someone else.
	 */
	UserEdit editUser(String id) throws UserNotDefinedException, UserPermissionException, UserLockedException;

	/**
	 * Find the user objects which have this email address.
	 * 
	 * @param email
	 *        The email address string.
	 * @return A Collection (User) of user objects which have this email address (may be empty).
	 */
	Collection findUsersByEmail(String email);

	/**
	 * Access the anonymous user object.
	 * 
	 * @return the anonymous user object.
	 */
	User getAnonymousUser();

	/**
	 * Access the user object associated with the "current" request.
	 * 
	 * @return The current user (may be anon).
	 */
	User getCurrentUser();

	/**
	 * Access a user object.
	 * 
	 * @param id
	 *        The user id string.
	 * @return A user object containing the user information
	 * @exception UserNotDefinedException
	 *            if not found
	 */
	User getUser(String id) throws UserNotDefinedException;

	/**
	 * Access a user object, given an enterprise id.
	 * 
	 * @param eid
	 *        The user eid string.
	 * @return A user object containing the user information
	 * @exception UserNotDefinedException
	 *            if not found
	 */
	User getUserByEid(String eid) throws UserNotDefinedException;

	/**
	 * Find the user eid from a user id.
	 * 
	 * @param id
	 *        The user id.
	 * @return The eid for the user with this id.
	 * @exception UserNotDefinedException
	 *            if we don't know anything about the user with this id.
	 */
	String getUserEid(String id) throws UserNotDefinedException;

	/**
	 * Find the user id from a user eid.
	 * 
	 * @param eid
	 *        The user eid.
	 * @return The id for the user with this eid.
	 * @exception UserNotDefinedException
	 *            if we don't know anything about the user with this eid.
	 */
	String getUserId(String eid) throws UserNotDefinedException;

	/**
	 * Access all user objects - known to us (not from external providers).
	 * 
	 * @return A list of user objects containing each user's information.
	 */
	List getUsers();

	/**
	 * Access a bunch of user object.
	 * 
	 * @param ids
	 *        The Collection (String) of user ids.
	 * @return A List (User) of user objects for valid ids.
	 */
	List getUsers(Collection ids);

	/**
	 * Find all the users within the record range given (sorted by sort name).
	 * 
	 * @param first
	 *        The first record position to return.
	 * @param last
	 *        The last record position to return.
	 * @return A list (User) of all the users within the record range given (sorted by sort name).
	 */
	List getUsers(int first, int last);
	
	/**
	 * Find all the users matching the given EID strings.
	 * 
	 * @param eids
	 * @return A list of user objects corresponding to the valid EIDs
	 */
	List<User> getUsersByEids(Collection<String> eids);

	/**
	 * Add a new user to the directory, from a definition in XML. Must commitEdit() to make official, or cancelEdit() when done!
	 * 
	 * @param el
	 *        The XML DOM Element defining the user.
	 * @return A locked UserEdit object (reserving the id).
	 * @exception UserIdInvalidException
	 *            if the user id is invalid.
	 * @exception UserAlreadyDefinedException
	 *            if the user id is already used.
	 * @exception UserPermissionException
	 *            if the current user does not have permission to add a user.
	 */
	UserEdit mergeUser(Element el) throws UserIdInvalidException, UserAlreadyDefinedException, UserPermissionException;

	/**
	 * Remove this user's information from the directory - it must be a user with a lock from editUser(). The UserEdit is disabled, and not to be used after this call.
	 * 
	 * @param user
	 *        The locked user object to remove.
	 * @exception UserPermissionException
	 *            if the current user does not have permission to remove this user.
	 */
	void removeUser(UserEdit user) throws UserPermissionException;

	/**
	 * Search all the users that match this criteria in id or email, first or last name, returning a subset of records within the record range given (sorted by sort name).
	 * 
	 * @param criteria
	 *        The search criteria.
	 * @param first
	 *        The first record position to return.
	 * @param last
	 *        The last record position to return.
	 * @return A list (User) of all the aliases matching the criteria, within the record range given (sorted by sort name).
	 */
	List searchUsers(String criteria, int first, int last);

	/**
	 * Access the internal reference which can be used to access the resource from within the system.
	 * 
	 * @param id
	 *        The user id string.
	 * @return The the internal reference which can be used to access the resource from within the system.
	 */
	String userReference(String id);
}
