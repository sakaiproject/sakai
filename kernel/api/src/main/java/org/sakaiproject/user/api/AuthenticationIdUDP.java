/**
 * Copyright (c) 2003-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.user.api;

/**
 * Interface a provider should implement if the authentication ID doesn't map to the EID.
 *
 * If you are performing authentication in the provider then you may want to look at
 * {@link org.sakaiproject.user.api.AuthenticatedUserProvider}.
 */
public interface AuthenticationIdUDP {

	/**
	 * Find a user by their authentication ID.
	 * @param aid The ID used to find the user by.
	 * @param user A blank user object onto which the details of the user can be loaded.
	 * @return <code>true</code> if a valid user was found and loaded.
	 */
	public boolean getUserbyAid(String aid, UserEdit user);

}
