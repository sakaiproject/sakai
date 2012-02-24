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
 * AuthenticationManager provides authentication of end-users.
 * </p>
 */
public interface AuthenticationManager
{
	/**
	 * Attempt to authenticate a user by the given evidence. Success produces the authenticated user id. Failure throws an exception.
	 * 
	 * @param e
	 *        The collected evidence to authenticate.
	 * @return The authentication information if authenticated.
	 * @throws AuthenticationException
	 *         if the evidence is not understood or not valid.
	 */
	Authentication authenticate(Evidence e) throws AuthenticationException;
}
