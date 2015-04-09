/**********************************************************************************
 * Copyright 2009 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.mailsender.logic.impl;

import java.util.List;
import java.util.Set;

import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.mailsender.model.EmailRole;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.user.api.User;

public class CachingComposeLogicImpl extends ComposeLogicImpl
{
	/** Key for cache storage of email groups */
	private static final String EMAIL_GROUPS_CACHE_KEY = "emailGroups";

	/** Key for cache storage of email roles */
	private static final String EMAIL_ROLES_CACHE_KEY = "emailRoles";

	/** Key for cache storage of email sections */
	private static final String EMAIL_SECTIONS_CACHE_KEY = "emailSections";

	/** Key for cache storage of role that is group aware */
	private static final String GROUP_AWARE_ROLE_CACHE_KEY = "groupAwareRole";

	/** Key for cache storage of users within a group */
	private static final String USERS_BY_GROUP_CACHE_KEY = "usersByGroup";

	/** Key for cache storage of users within a role */
	private static final String USERS_BY_ROLE_CACHE_KEY = "usersByRole";

	/** Key for cache storage of user IDs within a group */
	private static final String USER_IDS_BY_GROUP_CACHE_KEY = "userIdsByGroup";

	/** Key for cache storage of user IDs within a role */
	private static final String USER_IDS_BY_ROLE_CACHE_KEY = "userIdsByRole";

	/** Key for cache storage of users within a group */
	private static final String USERS_BY_GROUP_COUNT_CACHE_KEY = "usersByGroup-count";

	/** Key for cache storage of users within a role */
	private static final String USERS_BY_ROLE_COUNT_CACHE_KEY = "usersByRole-count";

	/** Memory service to create cache; injected */
	private MemoryService memoryService;

	/** Cache for storing results; instantiated in init */
	private Cache cache;

	public void init()
	{
		// create the cache needed to store method call results
		// this cache is defined in components.xml with the same name
		cache = memoryService.getCache(this.getClass().getName() + ".groupsCache");
	}

	public void destroy()
	{
		// get rid of any cache elements left
		cache.close();
	}

	public void setMemoryService(MemoryService memoryService)
	{
		this.memoryService = memoryService;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<EmailRole> getEmailGroups() throws IdUnusedException
	{
		String cacheKey = cacheKey(EMAIL_GROUPS_CACHE_KEY);
		List<EmailRole> list = (List<EmailRole>) cache.get(cacheKey);
		if (list == null)
		{
			list = super.getEmailGroups();
			cache.put(cacheKey, list);
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<EmailRole> getEmailRoles() throws GroupNotDefinedException
	{
		String cacheKey = cacheKey(EMAIL_ROLES_CACHE_KEY);
		List<EmailRole> list = (List<EmailRole>) cache.get(cacheKey);
		if (list == null)
		{
			list = super.getEmailRoles();
			cache.put(cacheKey, list);
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<EmailRole> getEmailSections() throws IdUnusedException
	{
		String cacheKey = cacheKey(EMAIL_SECTIONS_CACHE_KEY);
		List<EmailRole> list = (List<EmailRole>) cache.get(cacheKey);
		if (list == null)
		{
			list = super.getEmailSections();
			cache.put(cacheKey, list);
		}
		return list;
	}

	@Override
	public String getGroupAwareRole()
	{
		String cacheKey = cacheKey(GROUP_AWARE_ROLE_CACHE_KEY);
		String role = (String) cache.get(cacheKey);
		if (role == null)
		{
			role = super.getGroupAwareRole();
			cache.put(cacheKey, role);
		}
		return role;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> getUsersByGroup(String groupId) throws IdUnusedException
	{
		String cacheKey = cacheKey(USERS_BY_GROUP_CACHE_KEY, groupId);
		List<User> users = (List<User>) cache.get(cacheKey);
		if (users == null)
		{
			users = super.getUsersByGroup(groupId);
			cache.put(cacheKey, users);
		}
		return users;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> getUsersByRole(String role) throws IdUnusedException
	{
		String cacheKey = cacheKey(USERS_BY_ROLE_CACHE_KEY, role);
		List<User> users = (List<User>) cache.get(cacheKey);
		if (users == null)
		{
			users = super.getUsersByRole(role);
			cache.put(cacheKey, users);
		}
		return users;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.mailsender.logic.impl.ComposeLogicImpl#getUsersByGroupCount(java.lang.String
	 *      )
	 */
	@Override
	public int countUsersByGroup(String groupId)
	{
		int count = -1;
		try
		{
			// check the cache for the count
			String cacheKey = cacheKey(USERS_BY_GROUP_COUNT_CACHE_KEY, groupId);
			Integer counter = (Integer) cache.get(cacheKey);
			if (counter != null)
			{
				count = counter;
			}
			else
			{
				// use local lookup to get extra level of caching
				Set<String> users = getUserIdsByGroup(groupId);
				count = users.size();
				cache.put(cacheKey, count);
			}
		}
		catch (IdUnusedException e)
		{
			// don't care about this
		}
		return count;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.mailsender.logic.impl.ComposeLogicImpl#getUsersByRoleCount(java.lang.String)
	 */
	@Override
	public int countUsersByRole(String role)
	{
		int count = -1;
		try
		{
			// check the cache for the count
			String cacheKey = cacheKey(USERS_BY_ROLE_COUNT_CACHE_KEY, role);
			Integer counter = (Integer) cache.get(cacheKey);
			if (counter != null)
			{
				count = counter;
			}
			else
			{
				// use local lookup to get extra level of caching
				Set<String> users = getUserIdsByRole(role);
				count = users.size();
				cache.put(cacheKey, count);
			}
		}
		catch (IdUnusedException e)
		{
			// don't care about this
		}
		return count;
	}

	/**
	 * @see org.sakaiproject.mailsender.logic.impl.ComposeLogicImpl#getUserIdsByGroup(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Set<String> getUserIdsByGroup(String groupId) throws IdUnusedException
	{
		String cacheKey = cacheKey(USER_IDS_BY_GROUP_CACHE_KEY, groupId);
		Set<String> users = (Set<String>) cache.get(cacheKey);
		if (users == null)
		{
			users = super.getUserIdsByGroup(groupId);
			cache.put(cacheKey, users);
		}
		return users;
	}

	/**
	 * @see org.sakaiproject.mailsender.logic.impl.ComposeLogicImpl#getUserIdsByRole(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Set<String> getUserIdsByRole(String role) throws IdUnusedException
	{
		String cacheKey = cacheKey(USER_IDS_BY_ROLE_CACHE_KEY, role);
		Set<String> users = (Set<String>) cache.get(cacheKey);
		if (users == null)
		{
			users = super.getUserIdsByRole(role);
			cache.put(cacheKey, users);
		}
		return users;
	}

	/**
	 * Construct a unique cache key based on the provided key.
	 * 
	 * @param key
	 *            The key to start with.
	 * @param others
	 *            Other bits to append to the key.
	 * @return The current site id appended by the key and others. Each appendage is separated by a
	 *         colon.
	 * @throws IdUnusedException
	 */
	private String cacheKey(String key, String... others)
	{
		StringBuilder cacheKey = new StringBuilder(externalLogic.getSiteID()).append(":").append(key);
		for (String other : others)
		{
			cacheKey.append(":").append(other);
		}
		return cacheKey.toString();
	}
}
