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
import org.sakaiproject.profile2.types.PreferenceType;
import org.sakaiproject.profile2.types.PrivacyType;

/**
 * An interface for dealing with ProfilePrivacy in Profile2
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 * 
 */
public interface ProfilePrivacyLogic {

	/**
	 * Retrieve the profile privacy record from the database for this user. If none exists, will
	 * attempt to create one for the user. If that also fails, will return null.
	 * 
	 * <p>Defaults to using the cached version where possible</p>
	 *
	 * @param userId	uuid of the user to retrieve the record for
	 * @return ProfilePrivacy record or null
	 */
	public ProfilePrivacy getPrivacyRecordForUser(String userId);
	
	/**
	 * Retrieve the privacy record from the database for this user but the caller has the option
	 * on whether or not to use the cached version (PRFL-504)
	 *
	 * @param userId		uuid of the user to retrieve the record for
	 * @param useCache		whether or not to use the cache
	 * @return ProfilePrivacy record or null
	 */
	public ProfilePrivacy getPrivacyRecordForUser(final String userId, boolean useCache);
	
	/**
	 * Save the profile privacy record to the database
	 *
	 * @param profilePrivacy	the record for the user
	 */
	public boolean savePrivacyRecord(ProfilePrivacy profilePrivacy);
	
	/**
	 * Has the user allowed the other user to perform a certain action or view a piece of content?
	 * <p>Most profile privacy actions are dealt with here.
	 * 
	 * @param userX			the uuid of the user that will have the action performed on them
	 * @param userY			uuid of user requesting to do the action
	 * @param type			PrivacyType enum
	 * @return	true if allowed, false if not.
	 * @since 1.5
	 *
	 */
	public boolean isActionAllowed(final String userX, final String userY, final PrivacyType type);
	
	/**
	 * Has the user allowed viewing of their birth year in their profile. 
	 * This is either on or off and does not depend on friends etc which is why it is not included above.
	 * 
	 * @param uuid			the uuid of the user we are querying
	 * @return boolean
	 */
	public boolean isBirthYearVisible(String uuid);
}
