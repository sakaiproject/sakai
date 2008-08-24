/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2007, 2008 Sakai Foundation
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

/**
 * Indicates that the provider will provide user data along with authentication,
 * allowing the user's authentication login name to differ from the user's
 * enterprise integration ID (EID).
 * 
 * This interface is tailored for institutions which pick up other provided user
 * data as a side-effect of authentication. Authentication-only services would be
 * better described through an AuthenticationEidProvider interface.
 */
public interface AuthenticatedUserProvider {
	/**
	 * Authenticate the user based on an ID and password, returning user data.
	 * 
	 * @param loginId
	 *            the ID passed to the authentication system, such as a Kerberos
	 *            prinicipal name
	 * @param password
	 * @return user data, or null if the user was not authenticated; the user
	 *         record's "id" field will generally be null so that it can be
	 *         filled in by the Sakai user directory service
	 */
	UserEdit getAuthenticatedUser(String loginId, String password);
}
