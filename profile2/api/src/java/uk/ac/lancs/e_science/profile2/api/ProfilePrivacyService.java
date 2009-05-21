package uk.ac.lancs.e_science.profile2.api;

import uk.ac.lancs.e_science.profile2.api.model.ProfilePrivacy;


/**
 * <p>This is the outward facing service that should be used by anyone or anything implementing Profile2 privacy methods.</p>
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public interface ProfilePrivacyService {
	
	/**
	 * Create a blank ProfilePrivacy object.
	 * <p>DO NOT USE THIS METHOD.</p>
	 * @return
	 */
	public ProfilePrivacy getPrototype();
		
	/**
	 * Get the ProfilePrivacy object for the given user.
	 * @param userId
	 * @return ProfilePrivacy record for the user, default if they haven't got one, or null if invalid user
	 */
	public ProfilePrivacy getProfilePrivacyRecord(String userId);
	
	public boolean save(ProfilePrivacy obj);
	
	public boolean create(ProfilePrivacy obj);
	
	public boolean create(String userId);
	
	public boolean checkUserExists(String userId);
}
