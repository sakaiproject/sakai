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

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.profile2.dao.ProfileDao;
import org.sakaiproject.profile2.model.ProfilePreferences;
import org.sakaiproject.profile2.types.PreferenceType;
import org.sakaiproject.profile2.util.ProfileConstants;

/**
 * Implementation of ProfilePreferencesLogic API
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Setter
@Slf4j
public class ProfilePreferencesLogicImpl implements ProfilePreferencesLogic {

	private ProfileDao dao;
	private SakaiProxy sakaiProxy;

	@Override
	public ProfilePreferences getPreferencesRecordForUser(final String userId) {
		return getPreferencesRecordForUser(userId, true);
	}
	
	@Override
	public ProfilePreferences getPreferencesRecordForUser(final String userId, final boolean useCache) {
		
		if(userId == null){
	  		throw new IllegalArgumentException("Null argument in ProfileLogic.getPreferencesRecordForUser"); 
	  	}
		
		//will stay null if we can't get or create a record
		ProfilePreferences prefs = dao.getPreferencesRecordForUser(userId);
		log.debug("Fetching preferences record from dao for user [{}]", userId);
	
		if(prefs == null) {
			prefs = dao.addNewPreferencesRecord(getDefaultPreferencesRecord(userId));
			if(prefs != null) {
				sakaiProxy.postEvent(ProfileConstants.EVENT_PREFERENCES_NEW, "/profile/"+userId, true);
				log.debug("Created default preferences record for user [{}]", userId);
			}
		}			
		
		//if still null, we can't do much except log an error and wait for an NPE.
		if (prefs == null) {
			log.warn("Couldn't retrieve or create a preferences record for user [{}], this is an error and you need to fix your installation.", userId);
		}
		
		return prefs;
	}
	
	@Override
	public boolean savePreferencesRecord(ProfilePreferences prefs) {

		if (dao.savePreferencesRecord(prefs)) {
			log.debug("Updated preferences record for user: [{}]", prefs.getUserUuid());
			return true;
		} 
		
		return false;
	}
	
	/**
	 * Create a preferences record according to the defaults. 
	 *
	 * @param userId		uuid of the user to create the record for
	 */
	private ProfilePreferences getDefaultPreferencesRecord(final String userId) {
		
		ProfilePreferences prefs = new ProfilePreferences();
		prefs.setUserUuid(userId);
		prefs.setUseOfficialImage(ProfileConstants.DEFAULT_OFFICIAL_IMAGE_SETTING);
		prefs.setUseGravatar(ProfileConstants.DEFAULT_GRAVATAR_SETTING);
				
		return prefs;
	}
}
