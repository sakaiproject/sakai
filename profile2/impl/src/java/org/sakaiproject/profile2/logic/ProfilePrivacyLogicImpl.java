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

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.profile2.cache.CacheManager;
import org.sakaiproject.profile2.dao.ProfileDao;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.types.PrivacyType;
import org.sakaiproject.profile2.util.ProfileConstants;

/**
 * Implementation of ProfilePrivacyLogic API
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Slf4j
public class ProfilePrivacyLogicImpl implements ProfilePrivacyLogic {

	private Cache cache;
	private final String CACHE_NAME = "org.sakaiproject.profile2.cache.privacy";

	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public ProfilePrivacy getPrivacyRecordForUser(final String userId) {
		return getPrivacyRecordForUser(userId, true);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public ProfilePrivacy getPrivacyRecordForUser(final String userId, final boolean useCache) {
		
		if(userId == null){
			throw new IllegalArgumentException("Null argument in ProfileLogic.getPrivacyRecordForUser"); 
		}
		
		//will stay null if we can't get or create one
		ProfilePrivacy privacy = null;
		
		//check cache
		if(useCache) {
			if(cache.containsKey(userId)){
				log.debug("Fetching privacy record from cache for: " + userId);
				privacy = (ProfilePrivacy)cache.get(userId);
				if(privacy != null) {
				  return(privacy);
				}
				// This means that the cache has expired. evict the key from the cache
				log.debug("Privacy cache appears to have expired for " + userId);
				this.cacheManager.evictFromCache(this.cache, userId);
			}
		}
		
		privacy = dao.getPrivacyRecord(userId);
		log.debug("Fetching privacy record from dao for: " + userId);
		
		//if none, create and persist a default
		if(privacy == null) {
			privacy = dao.addNewPrivacyRecord(getDefaultPrivacyRecord(userId));
			if(privacy != null) {
				sakaiProxy.postEvent(ProfileConstants.EVENT_PRIVACY_NEW, "/profile/"+userId, true);
				log.info("Created default privacy record for user: " + userId); 
			}
		}
		
		//add to cache
		if(privacy != null) {
			log.debug("Adding privacy record to cache for: " + userId);
			cache.put(userId, privacy);
		}
		
		//if still null, we can't do much except log an error and wait for an NPE.
		if(privacy == null) {
			log.error("Couldn't retrieve or create a privacy record for user: " + userId + " This is an error and you need to fix your installation.");
		}
		
		return privacy;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean savePrivacyRecord(ProfilePrivacy privacy) {

		//if changes not allowed
		if(!sakaiProxy.isPrivacyChangeAllowedGlobally()) {
			log.warn("Privacy changes are not permitted as per sakai.properties setting 'profile2.privacy.change.enabled'.");
			return false;
		}
		
		//save
		if(dao.updatePrivacyRecord(privacy)) {
			log.info("Saved privacy record for user: " + privacy.getUserUuid()); 
			
			//update cache
			log.debug("Updated privacy record in cache for: " + privacy.getUserUuid());
			cache.put(privacy.getUserUuid(), privacy);
			
			return true;
		} 
		
		return false;
	}
	
	
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean isActionAllowed(final String userX, final String userY, final PrivacyType type) {
		
		//if user is requesting own info, they ARE allowed
    	if(StringUtils.equals(userX, userY)) {
    		return true;
    	}
    	
    	//get privacy record for this user
    	ProfilePrivacy profilePrivacy = getPrivacyRecordForUser(userX);
    	if(profilePrivacy == null) {
    		log.error("ProfilePrivacyLogic.isActionAllowed. Couldn't get a ProfilePrivacy record for userX: " + userX);   
        	return false;
    	}
    	
    	boolean isConnected = connectionsLogic.isUserXFriendOfUserY(userX, userY);
		
    	boolean result=false;
    	
    	if(log.isDebugEnabled()){
	    	log.debug("ProfilePrivacyLogic.isActionAllowed. userX: " + userX + ", userY: " + userY + ", type: " + type);  
    	}
    	
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
		    	log.error("ProfilePrivacyLogic.isActionAllowed. False for userX: " + userX + ", userY: " + userY + ", type: " + type);  
				result = false; 
			break;
    	}
	
    	return result;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
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
		
		//get the overriden privacy settings. they'll be defaults if not specified
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
	

	public void init() {
		cache = cacheManager.createCache(CACHE_NAME);
	}

	@Setter
	private SakaiProxy sakaiProxy;
	
	@Setter
	private ProfileDao dao;
	
	@Setter
	private CacheManager cacheManager;
	
	@Setter
	private ProfileConnectionsLogic connectionsLogic;
	
}
