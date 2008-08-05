/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright 2006 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
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
	 * @param eid
	 *        The user eid.
	 * @param edit
	 *        The UserEdit matching the eid to be authenticated (may be updated by the provider).
	 * @param password
	 *        The password.
	 * @return true if authenticated, false if not.
	 */
	boolean authenticateUser(String eid, UserEdit edit, String password);

	/**
	 * Whether to check provider or internal data first when authenticating a user
	 * 
	 * @param eid
	 *        The user eid.
	 * @return true if provider data is checked first, false if otherwise
	 */
	boolean authenticateWithProviderFirst(String eid);

	/**
	 * Find a user object who has this email address. Update the object with the information found.
	 * 
	 * @param email
	 *        The email address string.
	 * @return true if the user object was found and information updated, false if not.
	 */
	boolean findUserByEmail(UserEdit edit, String email);

	/**
	 * Access a user object. Update the object with the information found.
	 * 
	 * @param edit
	 *        The user object (eid is set) to fill in.
	 * @return true if the user object was found and information updated, false if not.
	 */
	boolean getUser(UserEdit edit);

	/**
	 * Access a collection of UserEdit objects; if the user is found, update the information, otherwise remove the UserEdit object from the collection.
	 * 
	 * @param users
	 *        The UserEdit objects (with eid set) to fill in or remove.
	 */
	void getUsers(Collection users);
}
