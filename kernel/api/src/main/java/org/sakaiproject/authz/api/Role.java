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
import java.util.Collection;
import java.util.Set;

/**
 * <p>
 * Role is part of an AuthzGroup, to which users can be assingned, and which is given permissions to various functions.
 * </p>
 */
public interface Role extends Comparable, Serializable
{
	/**
	 * Access the Role id.
	 * 
	 * @return The role id.
	 */
	String getId();

	/**
	 * Access the Role description.
	 * 
	 * @return The role description.
	 */
	String getDescription();

	/**
	 * Whether this Role is assignable only by a provider (true) or if the role can
	 * be manipulated by users (false).
	 * 
	 * @return Whether the role is assignable only by a provider.
	 */
	boolean isProviderOnly();
	
	/**
	 * Test if users with this role are allowed to perform this named function.
	 * 
	 * @param function
	 *        The function name.
	 * @return true if users with this role are allowed to perform this named function, false if not.
	 */
	boolean isAllowed(String function);

	/**
	 * Access the set of functions that users with this role are allowed to perform.
	 * 
	 * @return The Set of function names (String) that users with this role are allowed to perform.
	 */
	Set<String> getAllowedFunctions();

	/**
	 * Set the role description.
	 * 
	 * @param description
	 *        The role description.
	 */
	void setDescription(String description);

	/**
	 * Sets the provider only flag.
	 * 
	 * @param providerOnly
	 */
	void setProviderOnly(boolean providerOnly);

	/**
	 * Add this function to the set of functions that users with this role are allowed to perform.
	 * 
	 * @param lock
	 *        The function name to add to the allowed set.
	 */
	void allowFunction(String lock);

	/**
	 * Add these functions to the set of functions that users with this role are allowed to perform.
	 * 
	 * @param functions
	 *        The Collection (String) of function names to add to the allowed set.
	 */
	void allowFunctions(Collection<String> functions);

	/**
	 * Remove this function from the set of functions that users with this role are allowed to perform.
	 * 
	 * @param lock
	 *        The function name to disallow.
	 */
	void disallowFunction(String lock);

	/**
	 * Remove these functions from the set of functions that users with this role are allowed to perform.
	 * 
	 * @param functions
	 *        The Collection (String) of function names to remove from the allowed set.
	 */
	void disallowFunctions(Collection<String> functions);

	/**
	 * Remove all functions from the set of functions that users with this role are allowed to perform.
	 */
	void disallowAll();

	/**
	 * Check if the Role has no functons in the set of functions that users with this role are allowed to perform.
	 * 
	 * @return true if the role has no allowed functions, false if it does.
	 */
	boolean allowsNoFunctions();
}
