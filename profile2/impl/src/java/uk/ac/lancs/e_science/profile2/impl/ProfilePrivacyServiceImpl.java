package uk.ac.lancs.e_science.profile2.impl;

import org.apache.log4j.Logger;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.ProfilePrivacyService;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.api.entity.model.UserProfile;
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
	
	public boolean save(ProfilePrivacy obj) {
		// TODO Auto-generated method stub
		return false;
	}
	public boolean create(ProfilePrivacy obj) {
		// TODO Auto-generated method stub
		return false;
	}
	public boolean create(String userId) {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean checkUserExists(String userId) {
		return sakaiProxy.checkForUser(sakaiProxy.getUuidForUserId(userId));
	}
	
	/**
	 * Create a default ProfilePrivacy object for the given user.
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
