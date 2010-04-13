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

import org.apache.log4j.Logger;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.ProfilePrivacy;

/**
 * <p>This is the implementation of {@link ProfilePrivacyService}; see that interface for usage details.
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class ProfilePrivacyServiceImpl implements ProfilePrivacyService {

	private static final Logger log = Logger.getLogger(ProfilePrivacyServiceImpl.class);
	
	/**
	 * {@inheritDoc}
	 */
	public ProfilePrivacy getPrototype() {
		return new ProfilePrivacy();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ProfilePrivacy getProfilePrivacyRecord(String userId) {
		
		//check auth
		if(sakaiProxy.getCurrentUserId() == null) {
			throw new SecurityException("Must be logged in.");
		}
		
		//convert userId into uuid
		String userUuid = sakaiProxy.getUuidForUserId(userId);
		if(userUuid == null) {
			log.error("Invalid userId: " + userId);
			return null;
		}
		
		//get record, default, or null
		return profileLogic.getPrivacyRecordForUser(userUuid);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean save(ProfilePrivacy obj) {
		
		//check auth and get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if(currentUserUuid == null) {
			throw new SecurityException("Must be logged in.");
		}
		
		//check currentUser and object uuid match
		if(!currentUserUuid.equals(obj.getUserUuid())) {
			throw new SecurityException("Not allowed to save.");
		}
		
		//save and return response
		return persistProfilePrivacy(obj);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean create(ProfilePrivacy obj) {
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
		
		//check currentUser and object uuid match
		if(!currentUserUuid.equals(userUuid)) {
			throw new SecurityException("Not allowed to save.");
		}
		
		//get the current record, a default will be created if none exists
		//unless this is null, it worked
		ProfilePrivacy privacy = profileLogic.getPrivacyRecordForUser(userUuid);
		if(privacy == null) {
			log.error("userUuid: " + userUuid + " already has a ProfilePrivacy record. Cannot create another.");
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
	 * Helper method to take care of persisting a ProfilePrivacy object to the database.
	 * 
	 * @param ProfilePrivacy object
	 * @return true/false for success
	 */
	private boolean persistProfilePrivacy(ProfilePrivacy obj) {

		if(profileLogic.savePrivacyRecord(obj)) {
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
