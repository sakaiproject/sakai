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

package org.sakaiproject.authz.api;

import java.util.List;

/**
 * <p>
 * FunctionManager is the API for the service that manages security function registrations from the various Sakai applications.
 * </p>
 */
public interface FunctionManager
{
	/**
	 * Register an authz function
	 * 
	 * @param function The function name.
	 */
	void registerFunction(String function);

	/**
	 * Register an authz function
	 * 
	 * @param function
	 *            The function name.
	 * @param userMutable
	 *            If true, this function is intended to be settable by users with update
	 *            rights in the authzgroup, for example through a tool
	 *            permissions widget or web service. Setting this to false indicates that
	 *            tool UIs or web services should not allow this permission
	 *            to be altered by a user, but does not enforce this in Role.allowFunction()
	 *            or Role.disallowFunction().
	 */
	void registerFunction(String function, boolean userMutable);

	/**
	 * Access all the registered functions.
	 * 
	 * @return A List (String) of registered functions.
	 */
	List<String> getRegisteredFunctions();

	/**
	 * Access all the registered functions that begin with the string.
	 * 
	 * @param prefix
	 *        The prefix pattern to find.
	 * @return A List (String) of registered functions that begin with the string.
	 */
	List<String> getRegisteredFunctions(String prefix);
	
	/**
	 * Access all the registered functions intended to be user-settable
	 * 
	 * @return A List (String) of registered functions.
	 */
	List<String> getRegisteredUserMutableFunctions();

	/**
	 * Access all the registered functions intended to be user-settable that begin with the string.
	 * 
	 * @param prefix
	 *        The prefix pattern to find.
	 * @return A List (String) of registered functions that begin with the string.
	 */
	List<String> getRegisteredUserMutableFunctions(String prefix);

}
