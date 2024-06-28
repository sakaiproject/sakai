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

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.profile2.dao.ProfileDao;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.types.PrivacyType;
import org.sakaiproject.profile2.util.ProfileConstants;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of ProfilePrivacyLogic API
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Setter
@Slf4j
public class ProfilePrivacyLogicImpl implements ProfilePrivacyLogic {

	private ProfileConnectionsLogic connectionsLogic;
	private ProfileDao dao;
	private SakaiProxy sakaiProxy;

	@Override
	public ProfilePrivacy getPrivacyRecordForUser(final String userId) {
		
		if (StringUtils.isBlank(userId)) {
			throw new IllegalArgumentException("Invalid userid " + userId);
		}
		
		// will stay null if we can't get or create one
		ProfilePrivacy privacy = dao.getPrivacyRecord(userId);
		log.debug("Fetching privacy record from dao for [{}]", userId);
		
		// if none, create and persist a default
		if (privacy == null) {
			privacy = dao.addNewPrivacyRecord(getDefaultPrivacyRecord(userId));
		}

		if (privacy == null) {
			log.warn("Couldn't retrieve or create a privacy record for [{}], this should not occur", userId);
		} else {
			log.debug("Created default privacy record for [{}]", userId);
			sakaiProxy.postEvent(ProfileConstants.EVENT_PRIVACY_NEW, "/profile/" + userId, true);
		}

		return privacy;
	}
	
	@Override
	public boolean savePrivacyRecord(ProfilePrivacy privacy) {

		//if changes not allowed
		if(!sakaiProxy.isPrivacyChangeAllowedGlobally()) {
			log.warn("Privacy changes are not permitted as per sakai.properties setting 'profile2.privacy.change.enabled'.");
			return false;
		}
		
		if (dao.updatePrivacyRecord(privacy)) {
			log.debug("Saved privacy record for [{}]", privacy.getUserUuid());
			return true;
		} 
		
		return false;
	}

	@Override
	public boolean isActionAllowed(final String userX, final String userY, final PrivacyType type) {
		
		//if user is requesting own info, they ARE allowed
    	if(StringUtils.equals(userX, userY)) {
    		return true;
    	}
    	
    	//get privacy record for this user
    	ProfilePrivacy profilePrivacy = getPrivacyRecordForUser(userX);
    	if(profilePrivacy == null) {
    		log.warn("Couldn't get a ProfilePrivacy record for userX [{}]", userX);
        	return false;
    	}
    	
    	boolean isConnected = connectionsLogic.isUserXFriendOfUserY(userX, userY);
		
    	boolean result=false;
    	
		log.debug("userX: {}, userY: {}, type: {}", userX, userY, type);

    	switch (type) {
		
	    	case PRIVACY_OPTION_PROFILEIMAGE:
	    		
	    		//if user is friend and friends are allowed
	        	if(isConnected && profilePrivacy.getProfileImage() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
	        		result = true; break;
	        	}
	        	
	        	//if not friend and set to friends only
	        	if(!isConnected && profilePrivacy.getProfileImage() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
	        		result = false; break;
	        	}
	        	
	        	//if everyone is allowed
	        	if(profilePrivacy.getProfileImage() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
	        		result = true; break;
	        	}
	        	
				break;
			case PRIVACY_OPTION_BASICINFO:
	    		
	    		//if user is friend and friends are allowed
	        	if(isConnected && profilePrivacy.getBasicInfo() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
	        		result = true; break;
	        	}
	        	
	        	//if restricted to only self, not allowed
	        	if(profilePrivacy.getBasicInfo() == ProfileConstants.PRIVACY_OPTION_ONLYME) {
	        		result = false; break;
	        	}
	        	
	        	//if not friend and set to friends only
	        	if(!isConnected && profilePrivacy.getBasicInfo() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
	        		result = false; break;
	        	}
	        	
	        	//if everyone is allowed
	        	if(profilePrivacy.getBasicInfo() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
	        		result = true; break;
	        	}
	        	
				break;
			case PRIVACY_OPTION_CONTACTINFO:
	    		
	    		//if user is friend and friends are allowed
	        	if(isConnected && profilePrivacy.getContactInfo() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
	        		result = true; break;
	        	}
	        	
	        	//if restricted to only self, not allowed
	        	if(profilePrivacy.getContactInfo() == ProfileConstants.PRIVACY_OPTION_ONLYME) {
	        		result = false; break;
	        	}
	        	
	        	//if not friend and set to friends only
	        	if(!isConnected && profilePrivacy.getContactInfo() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
	        		result = false; break;
	        	}
	        	
	        	//if everyone is allowed
	        	if(profilePrivacy.getContactInfo() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
	        		result = true; break;
	        	}
	        
				break;
			case PRIVACY_OPTION_STAFFINFO:
	    		
	    		//if restricted to only self, not allowed
	        	if(profilePrivacy.getStaffInfo() == ProfileConstants.PRIVACY_OPTION_ONLYME) {
	        		result = false; break;
	        	}
	    		
	    		//if user is friend and friends are allowed
	        	if(isConnected && profilePrivacy.getStaffInfo() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
	        		result = true; break;
	        	}
	        	
	        	//if not friend and set to friends only
	        	if(!isConnected && profilePrivacy.getStaffInfo() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
	        		result = false; break;
	        	}
	        	
	        	//if everyone is allowed
	        	if(profilePrivacy.getStaffInfo() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
	        		result = true; break;
	        	}
	        
				break;
			case PRIVACY_OPTION_STUDENTINFO:
	    		
	    		//if restricted to only self, not allowed
	        	if(profilePrivacy.getStudentInfo() == ProfileConstants.PRIVACY_OPTION_ONLYME) {
	        		result = false; break;
	        	}
	    		
	    		//if user is friend and friends are allowed
	        	if(isConnected && profilePrivacy.getStudentInfo() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
	        		result = true; break;
	        	}
	        	
	        	//if not friend and set to friends only
	        	if(!isConnected && profilePrivacy.getStudentInfo() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
	        		result = false; break;
	        	}
	        	
	        	//if everyone is allowed
	        	if(profilePrivacy.getStudentInfo() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
	        		result = true; break;
	        	}
	        	
				break;
			case PRIVACY_OPTION_BUSINESSINFO:
	    		
	    		//if restricted to only self, not allowed
	        	if(profilePrivacy.getBusinessInfo() == ProfileConstants.PRIVACY_OPTION_ONLYME) {
	        		result = false; break;
	        	}
	        	
	        	//if user is friend and friends are allowed
	        	if(isConnected && profilePrivacy.getBusinessInfo() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
	        		result = true; break;
	        	}
	        	
	        	//if not friend and set to friends only
	        	if(!isConnected && profilePrivacy.getBusinessInfo() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
	        		result = false; break;
	        	}
	        	
	        	//if everyone is allowed
	        	if(profilePrivacy.getBusinessInfo() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
	        		result = true; break;
	        	}
	        	
				break;
			case PRIVACY_OPTION_SOCIALINFO:
	    		
	    		//if restricted to only self, not allowed
	        	if(profilePrivacy.getSocialNetworkingInfo() == ProfileConstants.PRIVACY_OPTION_ONLYME) {
	        		result = false; break;
	        	}
	        	
	        	//if user is friend and friends are allowed
	        	if(isConnected && profilePrivacy.getSocialNetworkingInfo() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
	        		result = true; break;
	        	}
	        	
	        	//if not friend and set to friends only
	        	if(!isConnected && profilePrivacy.getSocialNetworkingInfo() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
	        		result = false; break;
	        	}
	        	
	        	//if everyone is allowed
	        	if(profilePrivacy.getSocialNetworkingInfo() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
	        		result = true; break;
	        	}
	        	
				break;
			case PRIVACY_OPTION_PERSONALINFO:
	    		
	    		//if restricted to only self, not allowed
	        	if(profilePrivacy.getPersonalInfo() == ProfileConstants.PRIVACY_OPTION_ONLYME) {
	        		result = false; break;
	        	}
	        	
	        	//if user is friend and friends are allowed
	        	if(isConnected && profilePrivacy.getPersonalInfo() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
	        		result = true; break;
	        	}
	        	
	        	//if not friend and set to friends only
	        	if(!isConnected && profilePrivacy.getPersonalInfo() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
	        		result = false; break;
	        	}
	        	
	        	//if everyone is allowed
	        	if(profilePrivacy.getPersonalInfo() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
	        		result = true; break;
	        	}
	        	
				break;
			case PRIVACY_OPTION_MYFRIENDS:
	    		
	    		//if restricted to only self, not allowed
	        	if(profilePrivacy.getMyFriends() == ProfileConstants.PRIVACY_OPTION_ONLYME) {
	        		result = false; break;
	        	}
	        	
	        	//if user is friend and friends are allowed
	        	if(isConnected && profilePrivacy.getMyFriends() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
	        		result = true; break;
	        	}
	        	
	        	//if not friend and set to friends only
	        	if(!isConnected && profilePrivacy.getMyFriends() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
	        		result = false; break;
	        	}
	        	
	        	//if everyone is allowed
	        	if(profilePrivacy.getMyFriends() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
	        		result = true; break;
	        	}
	        	
				break;
			case PRIVACY_OPTION_MYPICTURES:
	    		
	    		//if user is friend and friends are allowed
	        	if (isConnected && profilePrivacy.getMyPictures() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
	        		result = true; break;
	        	}
	        	
	        	//if not friend and set to friends only
	        	if(!isConnected && profilePrivacy.getMyPictures() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
	        		result = false; break;
	        	}
	        	
	        	//if everyone is allowed
	        	if(profilePrivacy.getMyPictures() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
	        		result = true; break;
	        	}
	        	
				break;
			case PRIVACY_OPTION_MYSTATUS:
	    		
	    		//if user is friend and friends are allowed
	        	if(isConnected && profilePrivacy.getMyStatus() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
	        		result = true; break;
	        	}
	        	
	        	//if not friend and set to friends only
	        	if(!isConnected && profilePrivacy.getMyStatus() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
	        		result = false; break;
	        	}
	        	
	        	//if everyone is allowed
	        	if(profilePrivacy.getMyStatus() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
	        		result = true; break;
	        	}
	        	
				break;
			case PRIVACY_OPTION_MYKUDOS:
	    		//if user is friend and friends are allowed
	        	if(isConnected && profilePrivacy.getMyKudos() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
	        		result = true; break;
	        	}
	        	
	        	//if not friend and set to friends only
	        	if(!isConnected && profilePrivacy.getMyKudos() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
	        		result = false; break;
	        	}
	        	
	        	//if everyone is allowed
	        	if(profilePrivacy.getMyKudos() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
	        		result = true; break;
	        	}
	        	
				break;
			case PRIVACY_OPTION_MYWALL:
	    		
	    		//if user is friend and friends are allowed
	        	if(isConnected && profilePrivacy.getMyWall() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
	        		result = true; break;
	        	}
	        	
	        	//if not friend and set to friends only
	        	if(!isConnected && profilePrivacy.getMyWall() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
	        		result = false; break;
	        	}
	        	
	        	//if everyone is allowed
	        	if(profilePrivacy.getMyWall() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
	        		result = true; break;
	        	}
	        	
				break;
			case PRIVACY_OPTION_ONLINESTATUS:
	    		
	    		//if user is friend and friends are allowed
	        	if(isConnected && profilePrivacy.getOnlineStatus() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
	        		result = true; break;
	        	}
	        	
	        	//if not friend and set to friends only
	        	if(!isConnected && profilePrivacy.getOnlineStatus() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
	        		result = false; break;
	        	}
	        	
	        	//if everyone is allowed
	        	if(profilePrivacy.getOnlineStatus() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
	        		result = true; break;
	        	}
	        
				break;
			case PRIVACY_OPTION_MESSAGES:
	    		
	    		//if nobody allowed
	        	if(profilePrivacy.getMessages() == ProfileConstants.PRIVACY_OPTION_NOBODY) {
	        		result = false; break;
	        	}

	        	//if user is friend and friends are allowed
	        	if(isConnected && profilePrivacy.getMessages() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
	        		result = true; break;
	        	}
	        	
	        	//if not friend and set to friends only
	        	if(!isConnected && profilePrivacy.getMessages() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
	        		result = true; break;
	        	}
	    		
			
				break;
			default: 
				//invalid type
		    	log.warn("False for userX: {}, userY: {}, type: {}", userX, userY, type);
				result = false; 
			break;
    	}
	
    	return result;
	}
	
	@Override
	public boolean isBirthYearVisible(String uuid) {
		return getPrivacyRecordForUser(uuid).isShowBirthYear();
	}

	
	
	/**
	 * Create a privacy record according to the defaults. 
	 *
	 * @param userId		uuid of the user to create the record for
	 */
	private ProfilePrivacy getDefaultPrivacyRecord(String userId) {
		
		//get the overridden privacy settings. they'll be defaults if not specified
		HashMap<String, Object> props = sakaiProxy.getOverriddenPrivacySettings();	
		
		//using the props, set them into the ProfilePrivacy object
		ProfilePrivacy privacy = new ProfilePrivacy();
		privacy.setUserUuid(userId);
		privacy.setProfileImage((Integer)props.get("profileImage"));
		privacy.setBasicInfo((Integer)props.get("basicInfo"));
		privacy.setContactInfo((Integer)props.get("contactInfo"));
		privacy.setStaffInfo((Integer)props.get("staffInfo"));
		privacy.setStudentInfo((Integer)props.get("studentInfo"));
		privacy.setPersonalInfo((Integer)props.get("personalInfo"));
		privacy.setShowBirthYear((Boolean)props.get("birthYear"));
		privacy.setMyFriends((Integer)props.get("myFriends"));
		privacy.setMyStatus((Integer)props.get("myStatus"));
		privacy.setMyPictures((Integer)props.get("myPictures"));
		privacy.setMessages((Integer)props.get("messages"));
		privacy.setBusinessInfo((Integer)props.get("businessInfo"));
		privacy.setMyKudos((Integer)props.get("myKudos"));
		privacy.setMyWall((Integer)props.get("myWall"));
		privacy.setSocialNetworkingInfo((Integer)props.get("socialInfo"));
		privacy.setOnlineStatus((Integer)props.get("onlineStatus"));
		
		return privacy;
	}
}
