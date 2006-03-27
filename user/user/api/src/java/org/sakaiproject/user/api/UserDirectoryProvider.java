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

/**
 * <p>
 * UserDirectoryProvider feeds external user information to the UserDirectoryService.
 * </p>
 */
public interface UserDirectoryProvider
{
	/**
	 * Authenticate a user / password. If the user edit exists it may be modified, and will be stored if...
	 * 
	 * @param id
	 *        The user id.
	 * @param edit
	 *        The UserEdit matching the id to be authenticated (and updated) if we have one.
	 * @param password
	 *        The password.
	 * @return true if authenticated, false if not.
	 */
	boolean authenticateUser(String id, UserEdit edit, String password);

	/**
	 * Will this provider update user records on successfull authentication? If so, the UserDirectoryService will cause these updates to be stored.
	 * 
	 * @return true if the user record may be updated after successfull authentication, false if not.
	 */
	boolean updateUserAfterAuthentication();

	/**
	 * Remove any authentication traces for the current user / request
	 */
	void destroyAuthentication();

	/**
	 * See if a user by this id is known to the provider.
	 * 
	 * @param id
	 *        The user id string.
	 * @return true if a user by this id exists, false if not.
	 */
	boolean userExists(String id);

	/**
	 * Access a user object. Update the object with the information found.
	 * 
	 * @param edit
	 *        The user object (id is set) to fill in.
	 * @return true if the user object was found and information updated, false if not.
	 */
	boolean getUser(UserEdit edit);

	/**
	 * Access a collection of UserEdit objects; if the user is found, update the information, otherwise remove the UserEdit object from the collection.
	 * 
	 * @param users
	 *        The UserEdit objects (with id set) to fill in or remove.
	 */
	void getUsers(Collection users);

	/**
	 * Find a user object who has this email address. Update the object with the information found.
	 * 
	 * @param email
	 *        The email address string.
	 * @return true if the user object was found and information updated, false if not.
	 */
	boolean findUserByEmail(UserEdit edit, String email);

	/**
	 * Whether to check provider or internal data first when authenticating a user
	 * 
	 * @return true if provider data is checked first, false if otherwise
	 */
	boolean authenticateWithProviderFirst(String id);

	/**
	 * If user record cannot be found in by UserDirectoryService, can the service create the user record?
	 */
	boolean createUserRecord(String id);
}
