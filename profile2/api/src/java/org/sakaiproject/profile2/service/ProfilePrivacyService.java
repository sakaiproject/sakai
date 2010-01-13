/**
 * Copyright (c) 2008-2010 The Sakai Foundation
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

package org.sakaiproject.profile2.service;

import org.sakaiproject.profile2.model.ProfilePrivacy;


/**
 * This is the outward facing service that should be used by anyone or anything implementing Profile2 privacy methods.
 * <p>
 * Note that you must be logged in and can request anyone's privacy record. This is required so
 * that we can render information retrieved appropriately.
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public interface ProfilePrivacyService {
	
	/**
	 * Create a blank ProfilePrivacy object.
	 * <p>DO NOT USE THIS METHOD.</p>
	 * @return
	 */
	public ProfilePrivacy getPrototype();
		
	/**
	 * Get the ProfilePrivacy object for the given user.
	 * @param userId
	 * @return ProfilePrivacy record for the user, default if they haven't got one, or null if invalid user
	 */
	public ProfilePrivacy getProfilePrivacyRecord(String userId);
	
	/**
	 * Save the given ProfilePrivacy object. Checks currentUser against the ProfilePrivacy object supplied. 
	 * A user can update only their own, no one elses.
	 * 
	 * @param ProfilePrivacy object
	 * @return true/false for success
	 */
	public boolean save(ProfilePrivacy obj);
	
	/**
	 * Create a ProfilePrivacy object for the given user and persist it to the database.
	 * @param userId - either internal user id (6ec73d2a-b4d9-41d2-b049-24ea5da03fca) or eid (jsmith26)
	 * @return true/false for success
	 */
	public boolean create(String userId);
	
	/**
	 * Persist the given ProfilePrivacy object to the database
	 * @param obj - ProfilePrivacy obj that you want persisted
	 * @return true/false for success
	 */
	public boolean create(ProfilePrivacy obj);
	
	/**
	 * Checks whether a user exists. 
	 * <p>This actually just checks for the existence of a user in the system as every user has a ProfilePrivacy object, even if its a default one.</p>
	 * 
	 * @param userId - either internal user id (6ec73d2a-b4d9-41d2-b049-24ea5da03fca) or eid (jsmith26)
	 * @return true if exists, false otherwise
	 */
	public boolean checkUserExists(String userId);
	
	/**
	 * Helper method to check whether a user ProfilePrivacy object ACTUALLY exists, not just if the user exists.
	 * 
	 * @param userId - either internal user id (6ec73d2a-b4d9-41d2-b049-24ea5da03fca) or eid (jsmith26)
	 * @return true if exists, false otherwise
	 */
	public boolean checkProfilePrivacyExists(String userId);
}
