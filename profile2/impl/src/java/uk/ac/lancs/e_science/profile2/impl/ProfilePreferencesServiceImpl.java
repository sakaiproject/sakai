package uk.ac.lancs.e_science.profile2.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.ProfilePreferencesService;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.api.model.ProfilePreferences;

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
		
		//convert userId into uuid
		String userUuid = sakaiProxy.getUuidForUserId(userId);
		if(userUuid == null) {
			log.error("Invalid userId: " + userId);
			return null;
		}
		
		//get record or default if none.
		ProfilePreferences prefs = profile.getPreferencesRecordForUser(userUuid);
		if(prefs == null) {
			return getPrototype(userUuid);
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
		
		//check currentUser and profile uuid match
		if(!currentUserUuid.equals(obj.getUserUuid())) {
			throw new SecurityException("Not allowed to save.");
		}
		
		//save and return response
		return persistProfilePreferences(obj);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean create(ProfilePreferences obj) {
		
		String userUuid = obj.getUserUuid();
		if(StringUtils.isBlank(userUuid)) {
			return false;
		}
		
		//does this user already have a persisted preferences record?
		if(checkProfilePreferencesExists(userUuid)) {
			log.error("userUuid: " + userUuid + " already has a ProfilePreferences record. Cannot create another.");
			return false;
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
		if(profile.getPreferencesRecordForUser(userUuid) == null) {
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

		if(profile.savePreferencesRecord(obj)) {
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
		return profile.getDefaultPreferencesRecord(userUuid);
	}
	
	
	
	private SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}
	
	private Profile profile;
	public void setProfile(Profile profile) {
		this.profile = profile;
	}
	

	
	

}
