/**
 * Copyright (c) 2005-2017 The Apereo Foundation
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
package org.sakaiproject.component.app.messageforums;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.type.StringType;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

import org.sakaiproject.api.app.messageforums.AnonymousManager;
import org.sakaiproject.api.app.messageforums.AnonymousMapping;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.app.messageforums.dao.hibernate.AnonymousMappingImpl;

/**
 * @see org.sakaiproject.api.app.messageforums.AnonymousManager
 * @author bbailla2
 */
@Slf4j
public class AnonymousManagerImpl extends HibernateDaoSupport implements AnonymousManager
{

	// Padding used to enforce that anonIDs are always 6 characters
	private final String ANON_ID_PADDING = "000000";
	// Max value of a 6 digit hexadecimal number (ie. 0xFFFFFF)
	private final int MAX_HEX = 16777215;

	// Queries
	private final String QUERY_BY_SITE = "findMappingsBySite";
	private final String QUERY_BY_SITE_AND_USERS = "findMappingsBySiteAndUsers";

	// Oracle doesn't accept more than 1000 items in an 'in' clause
	private final int MAX_IN_CLAUSE_SIZE = 1000;

	private ServerConfigurationService serverConfigurationService;
	
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService)
	{
		this.serverConfigurationService = serverConfigurationService;
	}

	/** {@inheritDoc} */
	public boolean isAnonymousEnabled()
	{
		return serverConfigurationService.getBoolean("msgcntr.forums.anonymous.enable", true);
	}

	/** {@inheritDoc} */
	public boolean isPostAnonymousRevisable()
	{
		return serverConfigurationService.getBoolean("msgcntr.forums.postAnonymous.revisable", false);
	}

	/** {@inheritDoc} */
	public boolean isRevealIDsToRolesRevisable()
	{
		return isPostAnonymousRevisable() || serverConfigurationService.getBoolean("msgcntr.forums.revealIDsToRoles.revisable", false);
	}

	/** {@inheritDoc} */
	public String getAnonId(final String siteId, final String userId)
	{
		// Query for a single user in the site
		List<AnonymousMapping> results = findMappingsBySiteAndUsers(siteId, Collections.singletonList(userId));

		if (CollectionUtils.isEmpty(results))
		{
			return null;
		}

		return results.get(0).getAnonId();
	}

	/** {@inheritDoc} */
	public String getOrCreateAnonId(final String siteId, final String userId)
	{
		return getOrCreateUserIdAnonIdMap(siteId, Collections.singletonList(userId)).get(userId);
	}

	/**
	 * Gets a list of AnonymousMapping objects from the database for the specified users in the specified site.
	 */
	private List<AnonymousMapping> findMappingsBySiteAndUsers(final String siteId, final List<String> userIds)
	{
		if (CollectionUtils.isEmpty(userIds))
		{
			return Collections.emptyList();
		}

		HibernateCallback<List<AnonymousMapping>> hcb = new HibernateCallback<List<AnonymousMapping>>()
		{ 
			public List<AnonymousMapping> doInHibernate(Session session) throws HibernateException
			{
				List<AnonymousMapping> mappings = new ArrayList<>();
				// be mindful of Oracle's 1000 in clause limit
				int minUser = 0;
				int maxUser = Math.min(userIds.size(), MAX_IN_CLAUSE_SIZE);
				while (minUser < userIds.size())
				{
					Query q = session.getNamedQuery(QUERY_BY_SITE_AND_USERS);
					q.setParameter("siteId", siteId, StringType.INSTANCE);
					q.setParameterList("userIds", userIds.subList(minUser, maxUser));
					mappings.addAll(q.list());
					minUser += MAX_IN_CLAUSE_SIZE;
					maxUser = Math.min(userIds.size(), minUser + MAX_IN_CLAUSE_SIZE);
				}
				return mappings;
			}
		};

		return getHibernateTemplate().execute(hcb);
	}

	/** {@inheritDoc} */
	public Map<String, String> getUserIdAnonIdMap(final String siteId)
	{
		Map<String, String> anonIdMap = new HashMap<>();

		HibernateCallback<List<AnonymousMapping>> hcb = new HibernateCallback<List<AnonymousMapping>>()
		{
			public List<AnonymousMapping> doInHibernate(Session session) throws HibernateException
			{
				Query q = session.getNamedQuery(QUERY_BY_SITE);
				q.setParameter("siteId", siteId, StringType.INSTANCE);
				return q.list();
			}
		};

		List<AnonymousMapping> mappings = getHibernateTemplate().execute(hcb);

		for (AnonymousMapping mapping : mappings)
		{
			anonIdMap.put(mapping.getUserId(), mapping.getAnonId());
		}

		return anonIdMap;
	}

	/** {@inheritDoc} */
	public Map<String, String> getUserIdAnonIdMap(final String siteId, final List<String> userIds)
	{
		Map<String, String> anonIdMap = new HashMap<>(userIds.size());

		List<AnonymousMapping> mappings = findMappingsBySiteAndUsers(siteId, userIds);
		if (mappings == null || mappings.isEmpty())
		{
			return anonIdMap;
		}

		for (AnonymousMapping mapping : mappings)
		{
			anonIdMap.put(mapping.getUserId(), mapping.getAnonId());
		}

		return anonIdMap;
	}

	/** {@inheritDoc} */
	public Map<String, String> getOrCreateUserIdAnonIdMap(final String siteId, final List<String> userIds)
	{
		// Get all the userId - anonId mappings for the specified users in this site
		Map<String, String> anonIdMap = getUserIdAnonIdMap(siteId, userIds);

		if (anonIdMap.size() < userIds.size())
		{
			// The map doesn't contain all of the users. We'll have to create their anonIds
			Set<String> notFound = new HashSet<>();
			notFound.addAll(userIds);
			notFound.removeAll(anonIdMap.keySet());

			/*
			 * A race condition could occur:
			 * 1) request1 gets anonIdMap
			 * 2) request2 gets anonIdMap
			 * 3) request1 proceeds to save AnonymousMappings for users in 'notFound'
			 * 4) request2 tries to save AnonymousMappings for users in 'notFound', but they were created by request 1. Gets a constraint violation
			 *
			 * When running on multiple nodes, there is no way around this. 
			 * But in case we have constraint violations (Ie. we are request 2), we'll add all the users who got constraint violations to this list, and we'll query for their newly created anonIds
			 */
			List<String> constraintViolationUsers = new ArrayList<>();
			for (String userId : notFound)
			{
				String anonId = createAnonId();

				// insert to DB, then put in anonIdMap
				try
				{
					saveAnonMapping(new AnonymousMappingImpl(siteId, userId, anonId));
					anonIdMap.put(userId, anonId);
				}
				catch (DataIntegrityViolationException e)
				{
					if (e.getCause() instanceof ConstraintViolationException)
					{
						// Race condition!
						log.info("getOrCreateUserIdAnonIdMap: constraint violation while creating anonId for the following siteId, userId pair: (" + siteId + ", "  + userId + ")");
						// Note the user so we can get their anonId that was created in request 1
						constraintViolationUsers.add(userId);
					}
					else
					{
						// ???
						throw e;
					}
				}
			}

			anonIdMap.putAll(getUserIdAnonIdMap(siteId, constraintViolationUsers));
		}

		return anonIdMap;
	}

	/** {@inheritDoc} */
	public void saveAnonMapping(AnonymousMapping anonMapping)
	{
		getHibernateTemplate().saveOrUpdate(anonMapping);
	}

	/**
	 * Creates the anonymous ID - 6 uppercase hex digits (padded if less than 0x100000)
	 */
	private String createAnonId()
	{
		// 6 hex characters is short enough to be recognizable to follow a conversation.
		String hex = Integer.toHexString( (new Random()).nextInt(MAX_HEX) );
		return (ANON_ID_PADDING.substring(hex.length()) + hex).toUpperCase();
	}
}
