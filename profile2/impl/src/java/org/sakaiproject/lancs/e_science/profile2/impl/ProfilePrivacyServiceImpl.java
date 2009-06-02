package uk.ac.lancs.e_science.profile2.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.ProfilePrivacyService;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.api.model.ProfilePrivacy;

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
		
		//convert userId into uuid
		String userUuid = sakaiProxy.getUuidForUserId(userId);
		if(userUuid == null) {
			log.error("Invalid userId: " + userId);
			return null;
		}
		
		//get record or default if none.
		ProfilePrivacy privacy = profile.getPrivacyRecordForUser(userUuid);
		if(privacy == null) {
			return getPrototype(userUuid);
		}
		
		return privacy;
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
		
		String userUuid = obj.getUserUuid();
		if(StringUtils.isBlank(userUuid)) {
			return false;
		}
		
		//does this user already have a persisted privacy record?
		if(checkProfilePrivacyExists(userUuid)) {
			log.error("userUuid: " + userUuid + " already has a ProfilePrivacy record. Cannot create another.");
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
		
		//check currentUser and object uuid match
		if(!currentUserUuid.equals(userUuid)) {
			throw new SecurityException("Not allowed to save.");
		}
		
		//does this user already have a persisted profile?
		if(checkProfilePrivacyExists(userUuid)) {
			log.error("userUuid: " + userUuid + " already has a ProfilePrivacy record. Cannot create another.");
			return false;
		}
			
		//no existing privacy record, setup a prototype
		ProfilePrivacy privacy = getPrototype(userUuid);
		
		//save and return response
		return persistProfilePrivacy(privacy);
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
	public boolean checkProfilePrivacyExists(String userId) {
		
		//convert userId into uuid
		String userUuid = sakaiProxy.getUuidForUserId(userId);
		if(userUuid == null) {
			log.error("Invalid userId: " + userId);
			return false;
		}
		
		//check if we have a persisted object already
		if(profile.getPrivacyRecordForUser(userUuid) == null) {
			return false;
		}
		return true;
	}
	
	
	
	
	
	
	
	/**
	 * Helper method to take care of persisting a ProfilePrivacy object to the database.
	 * 
	 * @param ProfilePrivacy object
	 * @return true/false for success
	 */
	private boolean persistProfilePrivacy(ProfilePrivacy obj) {

		if(profile.savePrivacyRecord(obj)) {
			return true;
		} 
		return false;
	}
	
	/**
	 * Helper method to create a default ProfilePrivacy object for the given user.
	 * 
	 * @param userId - either internal user id (6ec73d2a-b4d9-41d2-b049-24ea5da03fca) or eid (jsmith26)
	 * @return a ProfilePrivacy object filled with the default fields
	 */
	private ProfilePrivacy getPrototype(String userId) {
		String userUuid = sakaiProxy.getUuidForUserId(userId);
		return profile.getDefaultPrivacyRecord(userUuid);
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
