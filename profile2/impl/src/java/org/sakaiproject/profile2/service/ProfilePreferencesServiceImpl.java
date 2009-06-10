package org.sakaiproject.profile2.service;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.ProfilePreferences;

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
			throw new SecurityException("You must be logged in to make a request for a user's preferences record.");
		}
		
		//convert userId into uuid
		String userUuid = sakaiProxy.getUuidForUserId(userId);
		if(userUuid == null) {
			log.error("Invalid userId: " + userId);
			return null;
		}
		
		//get record or default if none.
		ProfilePreferences prefs = profileLogic.getPreferencesRecordForUser(userUuid);
		if(prefs == null) {
			return getPrototype(userUuid);
		}
		
		//if user requested own
		if(userUuid.equals(currentUserUuid)) {
			return prefs;
		}
		
		//not own, clean it up
		prefs.setTwitterUsername(null);
		prefs.setTwitterPasswordDecrypted(null);
		prefs.setTwitterPasswordEncrypted(null);
		
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
		
		//check currentUser and profile uuid match
		if(!currentUserUuid.equals(userUuid)) {
			throw new SecurityException("Not allowed to save.");
		}
		
		//does this user already have a persisted preferences record?
		if(checkProfilePreferencesExists(userUuid)) {
			log.error("userUuid: " + userUuid + " already has a ProfilePreferences record. Cannot create another.");
			return false;
		}
		
		//validate twitter credentials if enabled for user and globally
		if(profileLogic.isTwitterIntegrationEnabledForUser(userUuid)) {
			if(!profileLogic.validateTwitterCredentials(obj)) {
				log.error("Failed to validate Twitter credentials for userUuid: " + userUuid);
				return false;
			}
		}
		
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
		
		//does this user already have a persisted profile?
		if(checkProfilePreferencesExists(userUuid)) {
			log.error("userUuid: " + userUuid + " already has a ProfilePreferences record. Cannot create another.");
			return false;
		}
			
		//no existing privacy record, setup a prototype
		ProfilePreferences prefs = getPrototype(userUuid);
		
		//save and return response
		return persistProfilePreferences(prefs);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean checkUserExists(String userId) {
		return sakaiProxy.checkForUser(sakaiProxy.getUuidForUserId(userId));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean checkProfilePreferencesExists(String userId) {
		
		//convert userId into uuid
		String userUuid = sakaiProxy.getUuidForUserId(userId);
		if(userUuid == null) {
			log.error("Invalid userId: " + userId);
			return false;
		}
		
		//check if we have a persisted object already
		if(profileLogic.getPreferencesRecordForUser(userUuid) == null) {
			return false;
		}
		return true;
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
	
	/**
	 * Helper method to create a default ProfilePreferences object for the given user.
	 * 
	 * @param userId - either internal user id (6ec73d2a-b4d9-41d2-b049-24ea5da03fca) or eid (jsmith26)
	 * @return a ProfilePreferences object filled with the default fields
	 */
	private ProfilePreferences getPrototype(String userId) {
		String userUuid = sakaiProxy.getUuidForUserId(userId);
		return profileLogic.getDefaultPreferencesRecord(userUuid);
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
