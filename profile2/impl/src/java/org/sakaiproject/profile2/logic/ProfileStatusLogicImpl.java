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

import java.util.Calendar;
import java.util.Date;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.profile2.dao.ProfileDao;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.model.ProfileStatus;
import org.sakaiproject.profile2.types.PrivacyType;
import org.sakaiproject.profile2.util.ProfileUtils;

/**
 * Implementation of ProfileStatusLogic API
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Slf4j
public class ProfileStatusLogicImpl implements ProfileStatusLogic {

	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public ProfileStatus getUserStatus(final String userUuid, ProfilePrivacy privacy) {
		
		//check privacy
		if(privacy == null){
			return null;
		}
		
		String currentUserUuid = sakaiProxy.getCurrentUserId();

		//if not same, check privacy
        if(!StringUtils.equals(userUuid, currentUserUuid)) {
		
        	//check allowed
        	if(!privacyLogic.isActionAllowed(userUuid, currentUserUuid, PrivacyType.PRIVACY_OPTION_MYSTATUS)){
        		return null;
        	}
        }
		
		// compute oldest date for status 
		Calendar cal = Calendar.getInstance(); 
		cal.add(Calendar.DAY_OF_YEAR, -7); 
		final Date oldestStatusDate = cal.getTime(); 
		
		//get data
		ProfileStatus status = dao.getUserStatus(userUuid, oldestStatusDate);
		if(status == null){
			return null;
		}
		
		//format the date field
		if(status.getDateAdded() != null){
			status.setDateFormatted(ProfileUtils.convertDateForStatus(status.getDateAdded()));
		}
		
		return status;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public ProfileStatus getUserStatus(final String userUuid) {
		return getUserStatus(userUuid, privacyLogic.getPrivacyRecordForUser(userUuid));
	}
	
	
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean setUserStatus(String userId, String status) {
		
		//create object
		ProfileStatus profileStatus = new ProfileStatus(userId,status,new Date());
		
		return setUserStatus(profileStatus);
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean setUserStatus(ProfileStatus profileStatus) {
		
		//current user must be the user making the request
		if(!StringUtils.equals(sakaiProxy.getCurrentUserId(), profileStatus.getUserUuid())) {
			throw new SecurityException("You are not authorised to perform that action.");
		}
		
		//PRFL-588 ensure size limit. Column size is 255.
		String tMessage = ProfileUtils.truncate(profileStatus.getMessage(), 255, false);
		profileStatus.setMessage(tMessage);
		
		if(dao.setUserStatus(profileStatus)){
			log.info("Updated status for user: " + profileStatus.getUserUuid()); 
			return true;
		} 
		
		return false;
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean clearUserStatus(String userId) {
		
		ProfileStatus profileStatus = getUserStatus(userId);
		
		if(profileStatus == null){
			log.error("ProfileStatus null for userId: " + userId); 
			return false;
		}
		
		//current user must be the user making the request
		if(!StringUtils.equals(sakaiProxy.getCurrentUserId(), profileStatus.getUserUuid())) {
			throw new SecurityException("You are not authorised to perform that action.");
		}
				
		if(dao.clearUserStatus(profileStatus)) {
			log.info("User: " + userId + " cleared status");  
			return true;
		}
		
		return false;
	}

	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public int getStatusUpdatesCount(final String userUuid) {
		return dao.getStatusUpdatesCount(userUuid);
	}
	
	
	
	@Setter
	private SakaiProxy sakaiProxy;
	
	@Setter
	private ProfilePrivacyLogic privacyLogic;
	
	@Setter
	private ProfileConnectionsLogic connectionsLogic;
	
	@Setter
	private ProfileDao dao;
	
}
