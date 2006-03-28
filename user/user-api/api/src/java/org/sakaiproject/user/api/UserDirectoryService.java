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
	/** This string can be used to find the service in the service manager. */
	static final String SERVICE_NAME = UserDirectoryService.class.getName();

	/** This string starts the references to resources in this service. */
	static final String REFERENCE_ROOT = "/user";

	/** Name for the event of accessing a user. */
	// static final String SECURE_ACCESS_USER = "user.access";
	/** Name for the event of adding a group. */
	static final String SECURE_ADD_USER = "user.add";

	/** Name for the event of removing a user. */
	static final String SECURE_REMOVE_USER = "user.del";

	/** Name for the event of updating one's own user info. */
	static final String SECURE_UPDATE_USER_OWN = "user.upd.own";

	/** Name for the event of updating any user info. */
	static final String SECURE_UPDATE_USER_ANY = "user.upd.any";

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
	 * Access a bunch of user object.
	 * 
	 * @param ids
	 *        The Collection (String) of user ids.
	 * @return A List (User) of user objects for valid ids.
	 */
	List getUsers(Collection ids);

	/**
	 * Access the user object associated with the "current" request.
	 * 
	 * @return The current user (may be anon).
	 */
	User getCurrentUser();

	/**
	 * Find the user objects which have this email address.
	 * 
	 * @param email
	 *        The email address string.
	 * @return A Collection (User) of user objects which have this email address (may be empty).
	 */
	Collection findUsersByEmail(String email);

	/**
	 * check permissions for editUser()
	 * 
	 * @param id
	 *        The user id.
	 * @return true if the user is allowed to update the user, false if not.
	 */
	boolean allowUpdateUser(String id);

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
	 * Commit the changes made to a UserEdit object, and release the lock. The UserEdit is disabled, and not to be used after this call.
	 * 
	 * @param user
	 *        The UserEdit object to commit.
	 * @exception UserAlreadyDefinedException
	 *            if the User eid is already in use by another User object.
	 */
	void commitEdit(UserEdit user) throws UserAlreadyDefinedException;

	/**
	 * Cancel the changes made to a UserEdit object, and release the lock. The UserEdit is disabled, and not to be used after this call.
	 * 
	 * @param user
	 *        The UserEdit object to commit.
	 */
	void cancelEdit(UserEdit user);

	/**
	 * Access the anonymous user object.
	 * 
	 * @return the anonymous user object.
	 */
	User getAnonymousUser();

	/**
	 * Access all user objects - known to us (not from external providers).
	 * 
	 * @return A list of user objects containing each user's information.
	 * @exception IdUnusedException
	 *            if not found.
	 */
	List getUsers();

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
	 * Count all the users.
	 * 
	 * @return The count of all users.
	 */
	int countUsers();

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
	 * Count all the users that match this criteria in id or target, first or last name.
	 * 
	 * @return The count of all users matching the criteria.
	 */
	int countSearchUsers(String criteria);

	/**
	 * check permissions for addUser().
	 * 
	 * @param id
	 *        The group id.
	 * @return true if the user is allowed to addUser(id), false if not.
	 */
	boolean allowAddUser(String id);

	/**
	 * Add a new user to the directory. Must commitEdit() to make official, or cancelEdit() when done!
	 * 
	 * @param id
	 *        The user id.
	 * @return A locked UserEdit object (reserving the id).
	 * @exception UserIdInvalidException
	 *            if the user id is invalid.
	 * @exception UserAlreadyDefinedException
	 *            if the user id is already used.
	 * @exception UserPermissionException
	 *            if the current user does not have permission to add a user.
	 */
	UserEdit addUser(String id) throws UserIdInvalidException, UserAlreadyDefinedException, UserPermissionException;

	/**
	 * Add a new user to the directory, complete in one operation.
	 * 
	 * @param id
	 *        The user id.
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
	 *            if the user id is invalid.
	 * @exception UserAlreadyDefinedException
	 *            if the user id is already used.
	 * @exception UserPermissionException
	 *            if the current user does not have permission to add a user.
	 */
	User addUser(String id, String firstName, String lastName, String email, String pw, String type, ResourceProperties properties)
			throws UserIdInvalidException, UserAlreadyDefinedException, UserPermissionException;

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
	 * check permissions for removeUser().
	 * 
	 * @param id
	 *        The group id.
	 * @return true if the user is allowed to removeUser(id), false if not.
	 */
	boolean allowRemoveUser(String id);

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
	 * Authenticate a user / password.
	 * 
	 * @param id
	 *        The user id.
	 * @param password
	 *        The password.
	 * @return The User object of the authenticated user if successfull, null if not.
	 */
	User authenticate(String id, String password);

	/**
	 * Remove authentication for the current user.
	 */
	void destroyAuthentication();

	/**
	 * Access the internal reference which can be used to access the resource from within the system.
	 * 
	 * @param id
	 *        The user id string.
	 * @return The the internal reference which can be used to access the resource from within the system.
	 */
	String userReference(String id);
}
