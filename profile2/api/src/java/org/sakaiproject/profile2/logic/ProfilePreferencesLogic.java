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

import org.sakaiproject.profile2.model.ProfilePreferences;
import org.sakaiproject.profile2.types.PreferenceType;

/**
 * An interface for dealing with ProfilePreferences in Profile2
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 * 
 */
public interface ProfilePreferencesLogic {

	/**
	 * Retrieve the preferences record from the database for this user. If none exists, will
	 * attempt to create one for the user. If that also fails, will return null.
	 * 
	 * <p>Defaults to using the cached version where possible</p>
	 *
	 * @param userId	uuid of the user to retrieve the record for
	 * @return ProfilePreferences record or null
	 */
	public ProfilePreferences getPreferencesRecordForUser(final String userId);
	
	/**
	 * Retrieve the preferences record from the database for this user but the caller has the option
	 * on whether or not to use the cached version (PRFL-504)
	 *
	 * @param userId		uuid of the user to retrieve the record for
	 * @param useCache		whether or not to use the cache
	 * @return ProfilePreferences record or null
	 */
	public ProfilePreferences getPreferencesRecordForUser(final String userId, boolean useCache);
	
	/**
	 * Save the preferences record to the database
	 *
	 * @param profilePreferences	the record for the user
	 */
	public boolean savePreferencesRecord(ProfilePreferences profilePreferences);
	
	/**
	 * Does this user have the specific preference enabled? used for querying all of the preferences
	 * @param userUuid	uuid of the user
	 * @param type		PreferenceType enum
	 * @return true if enabled, false if not
	 * @since 1.5
	 */
	public boolean isPreferenceEnabled(final String userUuid, final PreferenceType type);

}
