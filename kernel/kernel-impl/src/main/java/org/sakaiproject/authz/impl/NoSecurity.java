/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.authz.impl;

import java.util.List;
import java.util.Vector;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * <p>
 * NoSecurity is an example implementation of the Sakai SecurityService.
 * </p>
 */
@Slf4j
public abstract class NoSecurity implements SecurityService
{
	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @return the UserDirectoryService collaborator.
	 */
	protected abstract UserDirectoryService userDirectoryService();

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		log.info("init()");
	}

	/**
	 * Final cleanup.
	 */
	public void destroy()
	{
		log.info("destroy()");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * SecurityService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Get the authenticated session user
	 * 
	 * @param user
	 *        If not null, use this user, else use the session one.
	 * @return the User object authenticated to the current request's session.
	 */
	protected User getUser(User user)
	{
		if (user != null) return user;

		return userDirectoryService().getCurrentUser();
	}

	/**
	 * Is this a super special super (admin) user?
	 * 
	 * @return true, if the user is a cut above the rest, false if a mere mortal.
	 */
	public boolean isSuperUser()
	{
		if (log.isDebugEnabled()) log.debug("isSuperUser() true user: " + getUserId(null));
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSuperUser(String userId)
	{
		if (log.isDebugEnabled()) log.debug("isSuperUser(userId) true user: " + userId);
		return true;
	}

	/**
	 * Can the user in the security context unlock the lock for use with this resource?
	 * 
	 * @param lock
	 *        The lock id string.
	 * @param resource
	 *        The resource id string, or null if no resource is involved.
	 * @return true, if the user can unlock the lock, false otherwise.
	 */
	public boolean unlock(String lock, String resource)
	{
		if (log.isDebugEnabled())
			log.debug("unlock() true user: " + getUserId(null) + " lock: " + lock + " resource: " + resource);
		return true;
	}

	/**
	 * Can the user in the security context unlock the lock for use with this resource?
	 * 
	 * @param lock
	 *        The lock id string.
	 * @param resource
	 *        The resource id string, or null if no resource is involved.
	 * @return true, if the user can unlock the lock, false otherwise.
	 */
	public boolean unlock(User user, String lock, String resource)
	{
		if (log.isDebugEnabled())
			log.debug("unlock() true user: " + getUserId(user) + " lock: " + lock + " resource: " + resource);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean unlock(String userId, String lock, String resource)
	{
		if (log.isDebugEnabled())
			log.debug("unlock() true user: " + userId+ " lock: " + lock + " resource: " + resource);
		return true;
	}

	/**
	 * Access the List of Users who can unlock the lock for use with this resource.
	 * 
	 * @param lock
	 *        The lock id string.
	 * @param reference
	 *        The resource reference string.
	 * @return A List (User) of the users can unlock the lock (may be empty).
	 */
	public List<User> unlockUsers(String lock, String reference)
	{
		return new Vector<User>();
	}

	protected String getUserId(User u)
	{
		User user = getUser(u);
		if (user == null) return "";
		String id = user.getId();
		if (id == null) return "";
		return id;
	}

	public void pushAdvisor(SecurityAdvisor advisor)
	{
	}

	public SecurityAdvisor popAdvisor()
	{
		return null;
	}

	public boolean hasAdvisors()
	{
		return false;
	}

	public void clearAdvisors()
	{
	}
}
