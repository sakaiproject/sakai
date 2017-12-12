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

import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.profile2.cache.CacheManager;
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
@Slf4j
public class ProfilePreferencesLogicImpl implements ProfilePreferencesLogic {

	private Cache cache;
	private final String CACHE_NAME = "org.sakaiproject.profile2.cache.preferences";	
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public ProfilePreferences getPreferencesRecordForUser(final String userId) {
		return getPreferencesRecordForUser(userId, true);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public ProfilePreferences getPreferencesRecordForUser(final String userId, final boolean useCache) {
		
		if(userId == null){
	  		throw new IllegalArgumentException("Null argument in ProfileLogic.getPreferencesRecordForUser"); 
	  	}
		
		//will stay null if we can't get or create a record
		ProfilePreferences prefs = null;
		
		//check cache
		if(useCache){
			if(cache.containsKey(userId)){
				log.debug("Fetching preferences record from cache for: " + userId);
				prefs = (ProfilePreferences)cache.get(userId);
				if(prefs != null) {
					return(prefs);
				}
				// This means that the cache has expired. evict the key from the cache
				log.debug("Preferences cache appears to have expired for " + userId);
				this.cacheManager.evictFromCache(this.cache, userId);
			}
		}
		
		prefs = dao.getPreferencesRecordForUser(userId);
		log.debug("Fetching preferences record from dao for: " + userId);
	
		if(prefs == null) {
			prefs = dao.addNewPreferencesRecord(getDefaultPreferencesRecord(userId));
			if(prefs != null) {
				sakaiProxy.postEvent(ProfileConstants.EVENT_PREFERENCES_NEW, "/profile/"+userId, true);
				log.info("Created default preferences record for user: " + userId); 
			}
		}			
		
		//add to cache
		if(prefs != null){
			log.debug("Adding preferences record to cache for: " + userId);
			cache.put(userId, prefs);
		}
		
		//if still null, we can't do much except log an error and wait for an NPE.
		if(prefs == null) {
			log.error("Couldn't retrieve or create a preferences record for user: " + userId + " This is an error and you need to fix your installation.");
		}
		
		return prefs;
	}
	
	
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean savePreferencesRecord(ProfilePreferences prefs) {
		
		if(dao.savePreferencesRecord(prefs)){
			log.info("Updated preferences record for user: " + prefs.getUserUuid()); 
			
			//update cache
			log.debug("Updated preferences record in cache for: " + prefs.getUserUuid());
			cache.put(prefs.getUserUuid(), prefs);
			
			return true;
		} 
		
		return false;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean isPreferenceEnabled(final String userUuid, final PreferenceType type) {
		
		//get preferences record for this user
    	ProfilePreferences prefs = getPreferencesRecordForUser(userUuid);
    	
    	boolean result=false;
    	
    	switch (type) {
    		case EMAIL_NOTIFICATION_REQUEST: result = prefs.isRequestEmailEnabled(); break;
    		case EMAIL_NOTIFICATION_CONFIRM: result = prefs.isConfirmEmailEnabled(); break;
    		case EMAIL_NOTIFICATION_MESSAGE_NEW: result = prefs.isMessageNewEmailEnabled(); break;
    		case EMAIL_NOTIFICATION_MESSAGE_REPLY: result = prefs.isMessageReplyEmailEnabled(); break;
    		case EMAIL_NOTIFICATION_WALL_EVENT_NEW: result = prefs.isWallItemNewEmailEnabled(); break;
    		case EMAIL_NOTIFICATION_WALL_STATUS_NEW: result = prefs.isWallItemNewEmailEnabled(); break;
    		case EMAIL_NOTIFICATION_WALL_POST_MY_NEW: result = prefs.isWallItemNewEmailEnabled(); break;
    		case EMAIL_NOTIFICATION_WALL_POST_CONNECTION_NEW: result = prefs.isWallItemNewEmailEnabled(); break;
    		case EMAIL_NOTIFICATION_WORKSITE_NEW: result = prefs.isWorksiteNewEmailEnabled(); break;
    		default: 
    			//invalid type
    	    	log.debug("ProfileLogic.isPreferenceEnabled. False for userId: " + userUuid + ", type: " + type);  
    			result = false; 
    		break;
    	}
		
    	return result;
	}
	
	
	/**
	 * Create a preferences record according to the defaults. 
	 *
	 * @param userId		uuid of the user to create the record for
	 */
	private ProfilePreferences getDefaultPreferencesRecord(final String userId) {
		
		ProfilePreferences prefs = new ProfilePreferences();
		prefs.setUserUuid(userId);
		prefs.setRequestEmailEnabled(ProfileConstants.DEFAULT_EMAIL_REQUEST_SETTING);
		prefs.setConfirmEmailEnabled(ProfileConstants.DEFAULT_EMAIL_CONFIRM_SETTING);
		prefs.setMessageNewEmailEnabled(ProfileConstants.DEFAULT_EMAIL_MESSAGE_NEW_SETTING);
		prefs.setMessageReplyEmailEnabled(ProfileConstants.DEFAULT_EMAIL_MESSAGE_REPLY_SETTING);
		prefs.setWallItemNewEmailEnabled(ProfileConstants.DEFAULT_EMAIL_MESSAGE_WALL_SETTING);
		prefs.setWorksiteNewEmailEnabled(ProfileConstants.DEFAULT_EMAIL_MESSAGE_WORKSITE_SETTING);
		prefs.setUseOfficialImage(ProfileConstants.DEFAULT_OFFICIAL_IMAGE_SETTING);
		prefs.setShowKudos(ProfileConstants.DEFAULT_SHOW_KUDOS_SETTING);
		prefs.setShowGalleryFeed(ProfileConstants.DEFAULT_SHOW_GALLERY_FEED_SETTING);
		prefs.setUseGravatar(ProfileConstants.DEFAULT_GRAVATAR_SETTING);
		prefs.setShowOnlineStatus(ProfileConstants.DEFAULT_SHOW_ONLINE_STATUS_SETTING);
				
		return prefs;
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
	
}
