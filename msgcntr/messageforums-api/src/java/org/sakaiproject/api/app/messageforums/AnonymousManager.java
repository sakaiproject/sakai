/**
 * Copyright (c) 2003-2015 The Apereo Foundation
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
package org.sakaiproject.api.app.messageforums;

import java.util.List;
import java.util.Map;

/**
 * Manages anonymous Ids within forums
 * @author bbailla2
 */
public interface AnonymousManager
{
	/**
	 * Determines whether the anonymous feature is enabled in sakai.properties
	 */
	public boolean isAnonymousEnabled();

	/**
	 * Determines whether maintainers can revise the postAnonymous setting on existing topics.
	 * When false (default), you can create a new topic and set the postAnonymous setting, but you can't go back and change it.
	 * When true, you can change it at will.
	 */
	public boolean isPostAnonymousRevisable();

	/**
	 * Determines whether maintainers can revise the revealIDsToRoles setting on existing topics.
	 * When false (default), you can create a new topic and set the revealIDsToRoles setting, but you can't go back and change it.
	 * When true, you can change it at will.
	 * Note: this will be considered true if isPostAnonymousRevisable().
	 * Rationale: if maintainers can flip the anonymity for the entire site, there is no reason to prevent them from flipping anonymity only for themselves.
	 */
	public boolean isRevealIDsToRolesRevisable();

	/**
	 * Gets a single anonymous ID for the given siteId and the userId
	 * NB: Don't use this in a loop; use getUserIdAnonIdMap up front
	 * @param siteId the site within which we are looking up an anonymous ID
	 * @param userId the user for whom we're looking for an anonymous ID
	 * @return the anonymous ID associated with this (siteId, userId) pair; null if there is no entry
	 */
	public String getAnonId(final String siteId, final String userId);

	/**
	 * Similar to getAnonId(String siteId, String userId), except if an entry is not found, a new anonymous ID is created for this (siteId, userId) pair, and then it is returned
	 * NB: Don't use this in a loop; use getOrCreateUserIdAnonIdMap up front
	 */
	public String getOrCreateAnonId(final String siteId, final String userId);

	/**
	 * Gets a map of userIds to anonIds within the specified site
	 * NB: this is only aware of the mapping table - if the site has memberships that haven't yet made it into the mapping table, they will not be returned in this map.
	 * It's advisable to have the site list handy, and then for all missing userIds in this map, put(userId, getOrCreateAnonId(siteId, userId))
	 * @param siteId the site for which we want a mapping of userIds to anonIds
	 * @return a map of all userIds to anonIds for the current site
	 */
	public Map<String, String> getUserIdAnonIdMap(final String siteId);

	/**
	 * Gets a map of userIds to anonIds within the specified site, but only for the specified userIds
	 * @param siteId the site for which we want a mapping of userIds to anonIds
	 * @param userIds a collection of userIds to query for. Any users in this list who do not have anonIds in this site will have anonIds created for them
	 * @return a map of userIds to anonIds for all the specified users within this site
	 */
	public Map<String, String> getUserIdAnonIdMap(final String siteId, final List<String> userIds);

	/**
	 * Similar to getUserIdAnonIdMap(String siteId, List<String> userIds), but creates anon Ids for userIds that don't have associated entries
	 */
	public Map<String, String> getOrCreateUserIdAnonIdMap(final String siteId, final List<String> userIds);

	/**
	 * Persists the specified anonMapping in the db
	 */
	public void saveAnonMapping(AnonymousMapping anonMapping);
}
