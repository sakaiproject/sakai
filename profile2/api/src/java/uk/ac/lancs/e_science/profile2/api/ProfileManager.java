package uk.ac.lancs.e_science.profile2.api;

import uk.ac.lancs.e_science.profile2.api.entity.model.ProfileInfo;

/**
 * This is the public API for Profile2 to be implemented by others.
 * This wraps the profile2 API
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 */
public interface ProfileManager {
	
	public ProfileInfo getUserProfile(String userId);

}
