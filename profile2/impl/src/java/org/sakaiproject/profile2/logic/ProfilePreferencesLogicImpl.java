package org.sakaiproject.profile2.logic;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.sakaiproject.profile2.dao.ProfileDao;
import org.sakaiproject.profile2.model.ProfilePreferences;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;

import twitter4j.Twitter;

/**
 * Implementation of ProfilePreferencesLogic API
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class ProfilePreferencesLogicImpl implements ProfilePreferencesLogic {

	private static final Logger log = Logger.getLogger(ProfilePrivacyLogicImpl.class);

	
	/**
 	 * {@inheritDoc}
 	 */
	public ProfilePreferences getPreferencesRecordForUser(final String userId) {
		
		if(userId == null){
	  		throw new IllegalArgumentException("Null argument in ProfileLogic.getPreferencesRecordForUser"); 
	  	}
		
		//will stay null if we can't get or create a record
		ProfilePreferences prefs = null;
		prefs = dao.getPreferencesRecordForUser(userId);
		
		if(prefs == null) {
			prefs = dao.addNewPreferencesRecord(getDefaultPreferencesRecord(userId));
			if(prefs != null) {
				sakaiProxy.postEvent(ProfileConstants.EVENT_PREFERENCES_NEW, "/profile/"+userId, true);
				log.info("Created default preferences record for user: " + userId); 
			}
		}
		
		if(prefs != null) {
			//decrypt password and set into field
			prefs.setTwitterPasswordDecrypted(ProfileUtils.decrypt(prefs.getTwitterPasswordEncrypted()));

			//if owner, decrypt the password, otherwise, remove it entirely
			String currentUserUuid = sakaiProxy.getCurrentUserId();
			if(StringUtils.equals(userId, currentUserUuid)){
				prefs.setTwitterPasswordDecrypted(ProfileUtils.decrypt(prefs.getTwitterPasswordEncrypted()));
			} else {
				prefs.setTwitterPasswordEncrypted(null);
				prefs.setTwitterPasswordDecrypted(null);
			}
		}
		
		return prefs;
	}
	
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean savePreferencesRecord(ProfilePreferences prefs) {
		
		//validate fields are set and encrypt password if necessary, else clear them all
		if(checkTwitterFields(prefs)) {
			prefs.setTwitterPasswordEncrypted(ProfileUtils.encrypt(prefs.getTwitterPasswordDecrypted()));
		} else {
			prefs.setTwitterEnabled(false);
			prefs.setTwitterUsername(null);
			prefs.setTwitterPasswordDecrypted(null);
			prefs.setTwitterPasswordEncrypted(null);
		}
		
		if(dao.savePreferencesRecord(prefs)){
			log.info("Updated preferences record for user: " + prefs.getUserUuid()); 
			return true;
		} 
		
		return false;
	}
	
	
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean isTwitterIntegrationEnabledForUser(final String userId) {
		
		//check global settings
		if(!sakaiProxy.isTwitterIntegrationEnabledGlobally()) {
			return false;
		}
		
		//check own preferences
		ProfilePreferences profilePreferences = getPreferencesRecordForUser(userId);
		if(profilePreferences == null) {
			return false;
		}
		
		if(profilePreferences.isTwitterEnabled()) {
			return true;
		}
		
		return false;
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean isTwitterIntegrationEnabledForUser(ProfilePreferences prefs) {
		
		//check global settings
		if(!sakaiProxy.isTwitterIntegrationEnabledGlobally()) {
			return false;
		}
		
		//check own prefs
		if(prefs == null) {
			return false;
		}
		
		return prefs.isTwitterEnabled();
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
    	
    	//add more cases here as need progresses
    	
    	//no notification for this message type, return false 	
    	log.debug("ProfileLogic.isEmailEnabledForThisMessageType. False for userId: " + userId + ", messageType: " + messageType);  

    	return false;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean validateTwitterCredentials(final String twitterUsername, final String twitterPassword) {
		
		if(StringUtils.isNotBlank(twitterUsername) && StringUtils.isNotBlank(twitterPassword)) {
			Twitter twitter = new Twitter(twitterUsername, twitterPassword);
			if(twitter.verifyCredentials()) {
				return true;
			}
		}
		return false;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean validateTwitterCredentials(ProfilePreferences prefs) {
		
		String twitterUsername = prefs.getTwitterUsername();
		String twitterPassword = prefs.getTwitterPasswordDecrypted();
		return validateTwitterCredentials(twitterUsername, twitterPassword);
	}

	
	

	
	// helper method to check if all required twitter fields are set properly
	private boolean checkTwitterFields(ProfilePreferences prefs) {
		return (prefs.isTwitterEnabled() &&
				StringUtils.isNotBlank(prefs.getTwitterUsername()) &&
				StringUtils.isNotBlank(prefs.getTwitterPasswordDecrypted()));
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
		prefs.setTwitterEnabled(ProfileConstants.DEFAULT_TWITTER_SETTING);
		prefs.setUseOfficialImage(ProfileConstants.DEFAULT_OFFICIAL_IMAGE_SETTING);
		prefs.setShowKudos(ProfileConstants.DEFAULT_SHOW_KUDOS_SETTING);
				
		return prefs;
	}
	
	
	
	
	private SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}
	
	private ProfileDao dao;
	public void setDao(ProfileDao dao) {
		this.dao = dao;
	}
}
