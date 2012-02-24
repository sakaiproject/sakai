/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.user.api;

/**
 * <p>
 * UserFactory is an interface for facility to create User objects consistent with the current UserDirectoryService implementation.
 * This is useful when a provider is wanting to create user objects to be returned from a
 * {@link ExternalUserSearchUDP#searchExternalUsers(String, int, int, UserFactory)} call.
 * </p>
 */
public interface UserFactory
{
	/**
	 * Create a new User (UserEdit) object.
	 * @deprecated {@link #newUser(String)} should be used instead which presets the user ID.
	 * @return a new UserEdit object.
	 */
	UserEdit newUser();
	
	/**
	 * Create a new User (UserEdit) object mapped with the given eid. This userEdit object will also have its ID attribute set.
	 * @param eid	eid of the user to associate this object with
	 * @return a new UserEdit object.
	 */
	UserEdit newUser(String eid);
}
