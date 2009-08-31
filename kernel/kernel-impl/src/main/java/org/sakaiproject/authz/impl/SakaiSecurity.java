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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.authz.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.memory.api.MultiRefCache;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * <p>
 * SakaiSecurity is a Sakai security service.
 * </p>
 */
public abstract class SakaiSecurity implements SecurityService
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(SakaiSecurity.class);

	/** A cache of calls to the service and the results. */
	protected MultiRefCache m_callCache = null;

	/** ThreadLocalManager key for our SecurityAdvisor Stack. */
	protected final static String ADVISOR_STACK = "SakaiSecurity.advisor.stack";

	/** Session attribute to store roleswap state **/
	protected final static String ROLESWAP_PREFIX = "roleswap";

	/** The update event to post to clear cached security lookups involving the authz group **/
	protected final static String EVENT_ROLESWAP_CLEAR = "realm.clear.cache";

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies, configuration, and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @return the ThreadLocalManager collaborator.
	 */
	protected abstract ThreadLocalManager threadLocalManager();

	/**
	 * @return the AuthzGroupService collaborator.
	 */
	protected abstract AuthzGroupService authzGroupService();

	/**
	 * @return the UserDirectoryService collaborator.
	 */
	protected abstract UserDirectoryService userDirectoryService();

	/**
	 * @return the MemoryService collaborator.
	 */
	protected abstract MemoryService memoryService();

	/**
	 * @return the EntityManager collaborator.
	 */
	protected abstract EntityManager entityManager();
	
	/**
	 * @return the SessionManager collaborator.
	 */
	protected abstract SessionManager sessionManager();
	
	/**
	 * @return the EventTrackingService collaborator.
	 */
	protected abstract EventTrackingService eventTrackingService();

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Configuration
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** The # minutes to cache the security answers. 0 disables the cache. */
	protected int m_cacheMinutes = 3;

	/**
	 * Set the # minutes to cache a security answer.
	 * 
	 * @param time
	 *        The # minutes to cache a security answer (as an integer string).
	 */
	public void setCacheMinutes(String time)
	{
		m_cacheMinutes = Integer.parseInt(time);
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		// <= 0 minutes indicates no caching desired
		if (m_cacheMinutes > 0)
		{
			m_callCache = memoryService().newMultiRefCache(
					"org.sakaiproject.authz.api.SecurityService.cache");
		}
	}

	/**
	 * Final cleanup.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * SecurityService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * {@inheritDoc}
	 */
	public boolean isSuperUser()
	{
		User user = userDirectoryService().getCurrentUser();
		if (user == null) return false;

		return isSuperUser(user.getId());
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSuperUser(String userId)
	{
		// if no user or the no-id user (i.e. the anon user)
		if ((userId == null) || (userId.length() == 0)) return false;

		// check the cache
		String command = "super@" + userId;
		if (m_callCache != null)
		{
			final Boolean value = (Boolean) m_callCache.get(command);
			if(value != null) return value.booleanValue();
		}

		boolean rv = false;

		// these known ids are super
		if (UserDirectoryService.ADMIN_ID.equalsIgnoreCase(userId))
		{
			rv = true;
		}

		else if ("postmaster".equalsIgnoreCase(userId))
		{
			rv = true;
		}

		// if the user has site modification rights in the "!admin" site, welcome aboard!
		else
		{
			// TODO: string constants stolen from site -ggolden
			if (authzGroupService().isAllowed(userId, "site.upd", "/site/!admin"))
			{
				rv = true;
			}
		}

		// cache
		if (m_callCache != null)
		{
			Collection azgIds = new Vector();
			azgIds.add("/site/!admin");
			m_callCache.put(command, Boolean.valueOf(rv), m_cacheMinutes * 60, null, azgIds);
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean unlock(String lock, String resource)
	{
		return unlock(userDirectoryService().getCurrentUser(), lock, resource);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean unlock(User u, String function, String entityRef)
	{
		// pick up the current user if needed
		User user = u;
		if (user == null)
		{
			user = userDirectoryService().getCurrentUser();
		}
		return unlock(user.getId(), function, entityRef);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean unlock(String userId, String function, String entityRef)
	{
		return unlock(userId, function, entityRef, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean unlock(String userId, String function, String entityRef, Collection azgs)
	{
		// make sure we have complete parameters (azgs is optional)
		if (userId == null || function == null || entityRef == null)
		{
			M_log.warn("unlock(): null: " + userId + " " + function + " " + entityRef);
			return false;
		}

		// if super, grant
		if (isSuperUser(userId))
		{
			return true;
		}

		// let the advisors have a crack at it, if we have any
		// Note: this cannot be cached without taking into consideration the exact advisor configuration -ggolden
		if (hasAdvisors())
		{
			SecurityAdvisor.SecurityAdvice advice = adviseIsAllowed(userId, function, entityRef);
			if (advice != SecurityAdvisor.SecurityAdvice.PASS)
			{
				return advice == SecurityAdvisor.SecurityAdvice.ALLOWED;
			}
		}

		// check with the AuthzGroups appropriate for this entity
		return checkAuthzGroups(userId, function, entityRef, azgs);
	}

	/**
	 * Check the appropriate AuthzGroups for the answer - this may be cached
	 * 
	 * @param userId
	 *        The user id.
	 * @param function
	 *        The security function.
	 * @param entityRef
	 *        The entity reference string.
	 * @return true if allowed, false if not.
	 */
	protected boolean checkAuthzGroups(String userId, String function, String entityRef, Collection azgs)
	{
		// check the cache
		String command = "unlock@" + userId + "@" + function + "@" + entityRef;
		
		if (m_callCache != null)
		{
			final Boolean value = (Boolean) m_callCache.get(command);
			if(value != null) return value.booleanValue();
		}

		// get this entity's AuthzGroups if needed
		if (azgs == null)
		{
			// make a reference for the entity
			Reference ref = entityManager().newReference(entityRef);

			azgs = ref.getAuthzGroups(userId);
		}

		boolean rv = authzGroupService().isAllowed(userId, function, azgs);

		// cache
		if (m_callCache != null) m_callCache.put(command, Boolean.valueOf(rv), m_cacheMinutes * 60, entityRef, azgs);

		return rv;
	}

	/**
	 * Access the List the Users who can unlock the lock for use with this resource.
	 * 
	 * @param lock
	 *        The lock id string.
	 * @param reference
	 *        The resource reference string.
	 * @return A List (User) of the users can unlock the lock (may be empty).
	 */
	public List<User> unlockUsers(String lock, String reference)
	{
		if (reference == null)
		{
			M_log.warn("unlockUsers(): null resource: " + lock);
			return new Vector<User>();
		}

		// make a reference for the resource
		Reference ref = entityManager().newReference(reference);

		// get this resource's Realms
		Collection<String> realms = ref.getAuthzGroups();

		// get the users who can unlock in these realms
		List<String> ids = new Vector<String>();
		ids.addAll(authzGroupService().getUsersIsAllowed(lock, realms));

		// convert the set of Users into a sorted list of users
		List<User> users = userDirectoryService().getUsers(ids);
		Collections.sort(users);

		return users;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * SecurityAdvisor Support
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Get the thread-local security advisor stack, possibly creating it
	 * 
	 * @param force
	 *        if true, create if missing
	 */
	protected Stack getAdvisorStack(boolean force)
	{
		Stack advisors = (Stack) threadLocalManager().get(ADVISOR_STACK);
		if ((advisors == null) && force)
		{
			advisors = new Stack();
			threadLocalManager().set(ADVISOR_STACK, advisors);
		}

		return advisors;
	}

	/**
	 * Remove the thread-local security advisor stack
	 */
	protected void dropAdvisorStack()
	{
		threadLocalManager().set(ADVISOR_STACK, null);
	}

	/**
	 * Check the advisor stack - if anyone declares ALLOWED or NOT_ALLOWED, stop and return that, else, while they PASS, keep checking.
	 * 
	 * @param userId
	 *        The user id.
	 * @param function
	 *        The security function.
	 * @param reference
	 *        The Entity reference.
	 * @return ALLOWED or NOT_ALLOWED if an advisor makes a decision, or PASS if there are no advisors or they cannot make a decision.
	 */
	protected SecurityAdvisor.SecurityAdvice adviseIsAllowed(String userId, String function, String reference)
	{
		Stack advisors = getAdvisorStack(false);
		if ((advisors == null) || (advisors.isEmpty())) return SecurityAdvisor.SecurityAdvice.PASS;

		// a Stack grows to the right - process from top to bottom
		for (int i = advisors.size() - 1; i >= 0; i--)
		{
			SecurityAdvisor advisor = (SecurityAdvisor) advisors.elementAt(i);

			SecurityAdvisor.SecurityAdvice advice = advisor.isAllowed(userId, function, reference);
			if (advice != SecurityAdvisor.SecurityAdvice.PASS)
			{
				return advice;
			}
		}

		return SecurityAdvisor.SecurityAdvice.PASS;
	}

	/**
	 * {@inheritDoc}
	 */
	public void pushAdvisor(SecurityAdvisor advisor)
	{
		Stack advisors = getAdvisorStack(true);
		advisors.push(advisor);
	}

	/**
	 * {@inheritDoc}
	 */
	public SecurityAdvisor popAdvisor()
	{
		Stack advisors = getAdvisorStack(false);
		if (advisors == null) return null;

		SecurityAdvisor rv = null;

		if (advisors.size() > 0)
		{
			rv = (SecurityAdvisor) advisors.pop();
		}

		if (advisors.isEmpty())
		{
			dropAdvisorStack();
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasAdvisors()
	{
		Stack advisors = getAdvisorStack(false);
		if (advisors == null) return false;

		return !advisors.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	public void clearAdvisors()
	{
		dropAdvisorStack();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean setUserEffectiveRole(String azGroupId, String role) {
		
		if (!unlock(SiteService.SITE_ROLE_SWAP, azGroupId))
			return false;
		
		// set the session attribute with the roleid
		sessionManager().getCurrentSession().setAttribute(ROLESWAP_PREFIX + azGroupId, role); 
		resetSecurityCache(azGroupId);

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUserEffectiveRole(String azGroupId) {
		
		if (azGroupId == null || "".equals(azGroupId))
			return null;
		
		return (String) sessionManager().getCurrentSession().getAttribute(ROLESWAP_PREFIX + azGroupId);
	}

	/**
	 * {@inheritDoc}
	 */
	public void clearUserEffectiveRole(String azGroupId) {
	
		// remove the attribute from the session
		sessionManager().getCurrentSession().removeAttribute(ROLESWAP_PREFIX + azGroupId);
		resetSecurityCache(azGroupId);
		
		return;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void clearUserEffectiveRoles() {
		
		// get all the roleswaps from the session and clear them
		
		Session session = sessionManager().getCurrentSession();
		
		for (Enumeration<String> e = session.getAttributeNames(); e.hasMoreElements();)
		{
			String name = e.nextElement();
			if (name.startsWith(ROLESWAP_PREFIX)) {
				clearUserEffectiveRole(name.substring(ROLESWAP_PREFIX.length()));
			}
		}
		
		return;
	}
	
	/**
	 * Clear the results of security lookups involving the given authz group from the security lookup cache.
	 * 
	 * @param azGroupId
	 *        The authz group id.
	 */
	protected void resetSecurityCache(String azGroupId) {
		
		// This will clear all cached security lookups involving this realm, thereby forcing the permissions to be rechecked.
	
		// We could turn this into a SessionStateBindingListener so it gets called automatically when
		// the session is cleared.
		
		eventTrackingService().post(eventTrackingService().newEvent(EVENT_ROLESWAP_CLEAR, 
				org.sakaiproject.authz.api.AuthzGroupService.REFERENCE_ROOT + Entity.SEPARATOR + azGroupId, true)); 
		
		return;
	}
}
