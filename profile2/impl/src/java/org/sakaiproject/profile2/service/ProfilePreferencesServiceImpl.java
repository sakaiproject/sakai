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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.ProfilePreferences;
import org.sakaiproject.profile2.model.ProfilePrivacy;

/**
 * <p>This is the implementation of {@link ProfilePreferencesService}; see that interface for usage details.
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class ProfilePreferencesServiceImpl implements ProfilePreferencesService {

	private static final Logger log = Logger.getLogger(ProfilePreferencesServiceImpl.class);
	
	/**
	 * {@inheritDoc}
	 */
	public ProfilePreferences getPrototype() {
		return new ProfilePreferences();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ProfilePreferences getProfilePreferencesRecord(String userId) {
		
		//check auth and get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if(currentUserUuid == null) {
			throw new SecurityException("Must be logged in.");
		}
		
		//convert userId into uuid
		String userUuid = sakaiProxy.getUuidForUserId(userId);
		if(userUuid == null) {
			log.error("Invalid userId: " + userId);
			return null;
		}
		
		//get record or default if none.
		ProfilePreferences prefs = null;
		prefs = profileLogic.getPreferencesRecordForUser(userUuid);
		
		//if user requested own
		if(userUuid.equals(currentUserUuid)) {
			return prefs;
		}
		
		//not own, clean it up
		if(prefs != null) {
			prefs.setTwitterUsername(null);
			prefs.setTwitterPasswordDecrypted(null);
			prefs.setTwitterPasswordEncrypted(null);
		}
		
		return prefs;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean save(ProfilePreferences obj) {
		
		//check auth and get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if(currentUserUuid == null) {
			throw new SecurityException("Must be logged in.");
		}
		
		//get uuid
		String userUuid = obj.getUserUuid();
		if(StringUtils.isBlank(userUuid)) {
			return false;
		}
		
		//check currentUser and object uuid match
		if(!currentUserUuid.equals(userUuid)) {
			throw new SecurityException("Not allowed to save.");
		}
		
		//validate twitter credentials if enabled globally and in supplied prefs
		if(profileLogic.isTwitterIntegrationEnabledForUser(obj)) {
			if(!profileLogic.validateTwitterCredentials(obj)) {
				log.error("Failed to validate Twitter credentials for userUuid: " + userUuid);
				return false;
			}
		}
		
		//save and return response
		return persistProfilePreferences(obj);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean create(ProfilePreferences obj) {
		return save(obj);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean create(String userId) {
		
		//check auth and get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if(currentUserUuid == null) {
			throw new SecurityException("Must be logged in.");
		}
		
		//convert userId into uuid
		String userUuid = sakaiProxy.getUuidForUserId(userId);
		if(userUuid == null) {
			log.error("Invalid userId: " + userId);
			return false;
		}
		
		//check currentUser and profile uuid match
		if(!currentUserUuid.equals(userUuid)) {
			throw new SecurityException("Not allowed to save.");
		}
		
		//get the current record, a default will be created if none exists
		//unless this is null, it worked
		ProfilePreferences prefs = profileLogic.getPreferencesRecordForUser(userUuid);
		if(prefs == null) {
			log.error("userUuid: " + userUuid + " already has a ProfilePreferences record. Cannot create another.");
			return false;
		}
		
		return true;
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean checkUserExists(String userId) {
		return sakaiProxy.checkForUser(sakaiProxy.getUuidForUserId(userId));
	}

	
	
	/**
	 * Helper method to take care of persisting a ProfilePreferences object to the database.
	 * 
	 * @param ProfilePreferences object
	 * @return true/false for success
	 */
	private boolean persistProfilePreferences(ProfilePreferences obj) {

		if(profileLogic.savePreferencesRecord(obj)) {
			return true;
		} 
		return false;
	}
	
	
	
	private SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}
	
	private ProfileLogic profileLogic;
	public void setProfileLogic(ProfileLogic profileLogic) {
		this.profileLogic = profileLogic;
	}
	

	
	

}
