package org.sakaiproject.profile2.logic;

import org.apache.log4j.Logger;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.profile2.cache.CacheManager;
import org.sakaiproject.profile2.dao.ProfileDao;
import org.sakaiproject.profile2.model.ProfilePreferences;
import org.sakaiproject.profile2.util.ProfileConstants;

/**
 * Implementation of ProfilePreferencesLogic API
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class ProfilePreferencesLogicImpl implements ProfilePreferencesLogic {

	private static final Logger log = Logger.getLogger(ProfilePreferencesLogicImpl.class);

	private Cache cache;
	private final String CACHE_NAME = "org.sakaiproject.profile2.cache.preferences";	
	
	/**
 	 * {@inheritDoc}
 	 */
	public ProfilePreferences getPreferencesRecordForUser(final String userId) {
		return getPreferencesRecordForUser(userId, true);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public ProfilePreferences getPreferencesRecordForUser(final String userId, final boolean useCache) {
		
		if(userId == null){
	  		throw new IllegalArgumentException("Null argument in ProfileLogic.getPreferencesRecordForUser"); 
	  	}
		
		//check cache
		if(useCache){
			if(cache.containsKey(userId)){
				log.debug("Fetching preferences record from cache for: " + userId);
				//log.debug((ProfilePreferences)cache.get(userId));
				return (ProfilePreferences)cache.get(userId);
			}
		}
		
		//will stay null if we can't get or create a record
		ProfilePreferences prefs = null;
		
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
		
		return prefs;
	}
	
	
	
	/**
 	 * {@inheritDoc}
 	 */
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
	public boolean isEmailEnabledForThisMessageType(final String userId, final int messageType) {
		
		//get preferences record for this user
    	ProfilePreferences profilePreferences = getPreferencesRecordForUser(userId);
    	
    	//if none, return whatever the flag is set as by default
    	if(profilePreferences == null) {
    		return ProfileConstants.DEFAULT_EMAIL_NOTIFICATION_SETTING;
    	}
    	
    	//if its a request and requests enabled, true
    	if(messageType == ProfileConstants.EMAIL_NOTIFICATION_REQUEST && profilePreferences.isRequestEmailEnabled()) {
    		return true;
    	}
    	
    	//if its a confirm and confirms enabled, true
    	if(messageType == ProfileConstants.EMAIL_NOTIFICATION_CONFIRM && profilePreferences.isConfirmEmailEnabled()) {
    		return true;
    	}
    	
    	//if its a new message and new messages enabled, true
    	if(messageType == ProfileConstants.EMAIL_NOTIFICATION_MESSAGE_NEW && profilePreferences.isMessageNewEmailEnabled()) {
    		return true;
    	}
    	
    	//if its a reply to a message message and replies enabled, true
    	if(messageType == ProfileConstants.EMAIL_NOTIFICATION_MESSAGE_REPLY && profilePreferences.isMessageReplyEmailEnabled()) {
    		return true;
    	}
    	
    	// may split these later, but for now single preference controls wall emails
    	if((messageType == ProfileConstants.EMAIL_NOTIFICATION_WALL_EVENT_NEW || 
    			messageType == ProfileConstants.EMAIL_NOTIFICATION_WALL_POST_MY_NEW ||
    			messageType == ProfileConstants.EMAIL_NOTIFICATION_WALL_POST_CONNECTION_NEW)
    			&& profilePreferences.isWallItemNewEmailEnabled()) {
    		return true;
    	}
    	
    	//add more cases here as need progresses
    	
    	//no notification for this message type, return false 	
    	log.debug("ProfileLogic.isEmailEnabledForThisMessageType. False for userId: " + userId + ", messageType: " + messageType);  

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
		prefs.setRequestEmailEnabled(ProfileConstants.DEFAULT_EMAIL_REQUEST_SETTING);
		prefs.setConfirmEmailEnabled(ProfileConstants.DEFAULT_EMAIL_CONFIRM_SETTING);
		prefs.setMessageNewEmailEnabled(ProfileConstants.DEFAULT_EMAIL_MESSAGE_NEW_SETTING);
		prefs.setMessageReplyEmailEnabled(ProfileConstants.DEFAULT_EMAIL_MESSAGE_REPLY_SETTING);
		prefs.setUseOfficialImage(ProfileConstants.DEFAULT_OFFICIAL_IMAGE_SETTING);
		prefs.setShowKudos(ProfileConstants.DEFAULT_SHOW_KUDOS_SETTING);
		prefs.setShowGalleryFeed(ProfileConstants.DEFAULT_SHOW_GALLERY_FEED_SETTING);
		prefs.setUseGravatar(ProfileConstants.DEFAULT_GRAVATAR_SETTING);
				
		return prefs;
	}
	
	
	public void init() {
		cache = cacheManager.createCache(CACHE_NAME);
	}
	
	private SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}
	
	private ProfileDao dao;
	public void setDao(ProfileDao dao) {
		this.dao = dao;
	}
	
	private CacheManager cacheManager;
	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
}
