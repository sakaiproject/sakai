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

import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.profile2.model.BasicPerson;
import org.sakaiproject.profile2.model.CompanyProfile;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.model.SocialNetworkingInfo;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.user.api.User;

/**
 * An interface for working with profiles in Profile2.
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */

public interface ProfileLogic {

	/**
	 * Get a UserProfile for the given userUuid
	 * 
	 * <p>All users have profiles, even if they haven't filled it in yet. 
	 * At a very minimum it will contain their name. Privacy checks will determine visibility of other fields</p>
	 * 
	 * <p>You must be logged-in in order to make requests to this method as the content returned will be tailored
	 * to be visible for the currently logged in user.</p>
	 * 
	 * 
	 * @param userUuid		uuid of the user to retrieve the profile for
	 * @return UserProfile 	for the user, that is visible to the requesting user, or null if the user does not exist.
	 */
	public UserProfile getUserProfile(String userUuid);

	/**
	 * Get a UserProfile for the given userUuid
	 * 
	 * <p>All users have profiles, even if they haven't filled it in yet. 
	 * At a very minimum it will contain their name. Privacy checks will determine visibility of other fields</p>
	 * 
	 * <p>You must be logged-in in order to make requests to this method as the content returned will be tailored
	 * to be visible for the currently logged in user.</p>
	 * 
	 * 
	 * @param userUuid		uuid of the user to retrieve the profile for
	 * @param siteId		a site id to check permissions against. Occasionally, site persmissions like roster.viewemail
     *                      need to override profile2 permissions.
	 * @return UserProfile 	for the user, that is visible to the requesting user, or null if the user does not exist.
	 */
	public UserProfile getUserProfile(String userUuid, String siteId);
	
	/**
	 * Persist a SakaiPerson object and send an email notification, if required.
	 * 
	 * <p>Note that this may eventually change to UserProfile, when SakaiPerson is reimplemented.
	 * 
	 * @param sp	SakaiPerson obj
	 * @return	
	 */
	public boolean saveUserProfile(SakaiPerson sp);
	
	/**
	 * Adds a new company profile to the database.
	 * 
	 * @param companyProfile the company profile to add.
	 * @return the success of the operation.
	 */
	public boolean addNewCompanyProfile(CompanyProfile companyProfile);
	
	/**
	 * Retrieves the company profiles from the database for the specified user.
	 * 
	 * @param userId the ID of the user to query by.
	 */
	public List<CompanyProfile> getCompanyProfiles(String userId);
	
	/**
	 * Removes the specified company profile for the specified user.
	 * 
	 * @param userId the ID of the user to query by.
	 * @param companyProfile the ID of the company profile to remove.
	 */
	public boolean removeCompanyProfile(String userId, long companyProfile);
	
	/**
	 * Saves an existing company profile in the database. New company profiles
	 * should be added using the <code>addNewCompanyProfile</code> method.
	 * 
	 * @param companyProfile the existing company profile to be saved.
	 * @return the success of the operation.
	 */
	public boolean updateCompanyProfile(CompanyProfile companyProfile);
	
	/**
	 * Retrieves the social networking information for the specified user from
	 * the database.
	 * 
	 * @param userId the user to query by.
	 * @return the social networking information for the specified user.
	 */
	public SocialNetworkingInfo getSocialNetworkingInfo(String userId);
	
	/**
	 * Saves the social networking information to the database.
	 * 
	 * @param socialNetworkingInfo
	 * @return
	 */
	public boolean saveSocialNetworkingInfo(SocialNetworkingInfo socialNetworkingInfo);
		
	/**
	 * Get a BasicPerson
	 * @param userUuid
	 * @return
	 */
	public BasicPerson getBasicPerson(String userUuid);
	
	/**
	 * Get a BasicPerson
	 * @param user
	 * @return
	 */
	public BasicPerson getBasicPerson(User user);
	
	/**
	 * Get a List of BasicPersons for the given Users.
	 * @param users
	 * @return
	 */
	public List<BasicPerson> getBasicPersons(List<User> users);
	
	/**
	 * Get a Person
	 * @param userUuid The user to lookup
	 * @return The found person or <code>null</code> if the person can't be found.
	 */
	public Person getPerson(String userUuid);
	
	/**
	 * Get a Person
	 * @param user
	 * @return
	 */
	public Person getPerson(User user);
		
	/**
	 * Get a List of Persons for the given Users.
	 * @param users
	 * @return
	 */
	public List<Person> getPersons(List<User> users);
	
	/**
	 * Get a list of all SakaiPerson's userIds (ie list of all people with profile records)
	 *
	 * @return	List of Sakai userId's 
	 */
	public List<String> getAllSakaiPersonIds();
	
	/**
	 * Get a count of all users with SakaiPerson records
	 *
	 * @return count
	 */
	public int getAllSakaiPersonIdsCount();
	
	
}
