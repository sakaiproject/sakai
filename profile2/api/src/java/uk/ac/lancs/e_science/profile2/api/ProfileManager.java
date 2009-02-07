package uk.ac.lancs.e_science.profile2.api;

import uk.ac.lancs.e_science.profile2.api.entity.model.ProfileInfo;

/**
 * This is the public API for Profile2 to be implemented by others.
 * This wraps the Profile2 API. 
 * 
 * This is the API you should implement
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 */
public interface ProfileManager {
	
	/**
	 * get a ProfileInfo object for userX with parts visible by userY
	 * 
	 * @param 	userX	uuid of the user to get the profile for
	 * @paramr userY	uuid of the user making the request
	 * @return
	 * 
	 */
	public ProfileInfo getProfileForUserXVisibleByUserY(String userX, String userY);

	//get profile of self
	//public ProfileInfo getOwnProfile();
	
	
}
