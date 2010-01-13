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

import org.sakaiproject.profile2.model.ProfileStatus;


/**
 * <p>This is the outward facing service that should be used by anyone or anything implementing Profile2 status methods.</p>
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public interface ProfileStatusService {
	
	/**
	 * Create a blank ProfileStatus object.
	 * <p>DO NOT USE THIS METHOD.</p>
	 * @return
	 */
	public ProfileStatus getPrototype();
		
	/**
	 * Get the ProfileStatus object for the given user.
	 * @param userId
	 * @return ProfileStatus record for the user, blank status if they have none, or null if invalid user
	 */
	public ProfileStatus getProfileStatusRecord(String userId);
	
	/**
	 * Save the given ProfileStatus object. Checks currentUser against the ProfileStatus object supplied. 
	 * A user can update only their own, no one elses.
	 * 
	 * @param ProfileStatus object
	 * @return true/false for success
	 */
	public boolean save(ProfileStatus obj);
	
	/**
	 * Create a ProfileStatus object for the given user and message and persist it to the database.
	 * @param userId - either internal user id (6ec73d2a-b4d9-41d2-b049-24ea5da03fca) or eid (jsmith26)
	 * @param message - status message e.g. 'hello'
	 * @return true/false for success
	 */
	public boolean create(String userId, String message);
	
	
	/**
	 * Helper method to check whether a user ProfileStatus object ACTUALLY exists, not just if the user exists.
	 * 
	 * @param userId - either internal user id (6ec73d2a-b4d9-41d2-b049-24ea5da03fca) or eid (jsmith26)
	 * @return true if exists, false otherwise
	 */
	public boolean checkProfileStatusExists(String userId);
}
