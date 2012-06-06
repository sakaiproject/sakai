/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.logic;

import java.util.List;

import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.model.ProfileSearchTerm;

/**
 * An interface for dealing with profile searches.
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 * @author Daniel Robinson (d.b.robinson@lancaster.ac.uk)
 */
public interface ProfileSearchLogic {

	/**
	 * Find all users that match the search string in either name or email. 
	 * 
	 * <p>Searches SakaiPerson, UserDirectorySerice internal users as well as external users if your
	 * provider supports SearchExternalUsersUDP.</p>
	 * 
	 * <p>This list is automatically cleaned for non-existent users by way of UserDirectoryService.getUsers.</p>
	 * 
	 * @param search string to search for
	 * @param includeConnections should connections be returned in results
	 * @param worksiteId optional parameter to limit search to a single worksite. Specify <code>null</code> to search all users.
	 * @return List Persons
	 */
	public List<Person> findUsersByNameOrEmail(String search, boolean includeConnections, String worksiteId);
	
	/**
	 * Find all users that match the search string in any of the relevant SakaiPerson fields
	 *
	 * <p>This list is automatically cleaned for non-existent users by way of UserDirectoryService.getUsers.</p>
	 * 
	 * @param search string to search for
	 * @param includeConnections should connections be returned in results
	 * @param worksiteId optional parameter to limit search to a single worksite. Specify <code>null</code> to search all users.
	 * @return List Persons
	 */
	public List<Person> findUsersByInterest(String search, boolean includeConnections, String worksiteId);
		
	/**
	 * Retrieves the last search term made by the user with the given UUID.
	 * 
	 * @param userUuid the UUID of the user to query by.
	 * @return the last search term made by the user with the given
	 *         UUID. Returns <code>null</code> if no search term is found.
	 */
	public ProfileSearchTerm getLastSearchTerm(String userUuid);
	
	/**
	 * Retrieves the search history for the user with the given UUID.
	 * 
	 * @param userUuid the UUID of the user to query by.
	 * @return the search history for the user with the given UUID. Returns
	 *         <code>null</code> if no search history is found.
	 */
	public List<ProfileSearchTerm> getSearchHistory(String userUuid);

	/**
	 * Adds the given profile search term to a user's search history. 
	 * 
	 * @param userUuid the user whose history we're adding to.
	 * @param searchTerm the search term to add.
	 */
	public void addSearchTermToHistory(String userUuid, ProfileSearchTerm searchTerm);
	
	/**
	 * Clears the search history for the user with the given UUID.
	 * 
	 * @param userUuid the UUID of the user to clear history for.
	 */
	public void clearSearchHistory(String userUuid);
}