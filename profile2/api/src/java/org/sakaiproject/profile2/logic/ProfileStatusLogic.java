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

import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.model.ProfileStatus;

/**
 * An interface for dealing with ProfileStatus in Profile2
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 * 
 */
public interface ProfileStatusLogic {

	/**
	 * Get the status (message and date) for a user
	 * 
	 * <p>Only returns a status object for those that are up to and including one week old.
	 * This could be configurable.</p>
	 * 
	 * <p>The privacy settings will be retrieved, and checked against the
	 *  current requesting user to see if the status is allowed to be shown.</p>
	 *
	 * @param userUuid		uuid of the user to get their status for
	 * @return ProfileStatus or null if not allowed/none
	 */
	public ProfileStatus getUserStatus(String userUuid);
	
	/**
	 * Get the status (message and date) for a user
	 * 
	 * <p>Only returns a status object for those that are up to and including one week old.
	 * This could be configurable.</p>
	 * 
	 * <p>The supplied privacy settings will be checked against the
	 *  current requesting user to see if the status is allowed to be shown.</p>
	 *
	 * @param userUuid		uuid of the user to get their status for
	 * @param privacy		ProfilePrivacy object for the user. 
	 * @return ProfileStatus or null if not allowed/none
	 */
	public ProfileStatus getUserStatus(String userUuid, ProfilePrivacy privacy);
	
	/**
	 * Set user status
	 *
	 * @param userId		uuid of the user 
	 * @param status		status to be set
	 */
	public boolean setUserStatus(String userId, String status);
	
	/**
	 * Set user status
	 *
	 * @param profileStatus		ProfileStatus object for the user
	 */
	public boolean setUserStatus(ProfileStatus profileStatus);
	
	
	/**
	 * Clear user status
	 *
	 * @param userId		uuid of the user 
	 */
	public boolean clearUserStatus(String userId);
	
	/**
	 * Get the number of status updates this user has made. Until PRFL-191 is implemented
	 * which allows multiple status updates, this will only return one.
	 * 
	 * @param userUuid		uuid of the user 
	 * @return
	 */
	public int getStatusUpdatesCount(final String userUuid);
}
