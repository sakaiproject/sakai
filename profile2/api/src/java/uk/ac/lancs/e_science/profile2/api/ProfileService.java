package uk.ac.lancs.e_science.profile2.api;

import uk.ac.lancs.e_science.profile2.api.entity.model.UserProfile;

/**
 * <p>This is the outward facing service that should be used by anyone or anything implementing Profile2.</p>
 * <p>It provides a set of public methods for getting at Profile related information and takes care of all necessary privacy checks via its private methods 
 * and calls to the Profile/SakaiProxy API's.</p>
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public interface ProfileService {

	public boolean save(UserProfile userProfile);
	
	/**
	 * Create a blank UserProfile object.
	 * <p>DO NOT USE THIS METHOD.</p>
	 * @return
	 */
	public UserProfile getPrototype();
	
	/**
	 * Create a UserProfile object for the given user. This is the minimum that a UserProfile can be. 
	 * 
	 * @param userId - either internal user id (6ec73d2a-b4d9-41d2-b049-24ea5da03fca) or eid (jsmith26)
	 * @return
	 */
	public UserProfile getPrototype(String userId);
	
	/**
	 * Get a UserProfile for the given user Id
	 * 
	 * <p>All users have profiles, even if they haven't filled it in yet. 
	 * At a very minimum it will contain their name and possibly email address.</p>
	 * 
	 * <p>If they don't have a SakaiPerson object to get the data from, a prototype is used.</p>
	 * <p>If they do, the SakaiPerson object is transformed into a UserProfile object.
	 * 
	 * @param userId - either internal user id (6ec73d2a-b4d9-41d2-b049-24ea5da03fca) or eid (jsmith26)
	 * @param currentUser - user id that is making the request for the profile.
	 * @return
	 */
	public UserProfile getUserProfile(String userId, String currentUser);
	
	
	/**
	 * Checks whether a user profile exists. 
	 * <p>This actually just checks for the existence of a user in the system as every user has a profile, even if it is blank.</p>
	 * 
	 * @param userId - either internal user id (6ec73d2a-b4d9-41d2-b049-24ea5da03fca) or eid (jsmith26)
	 * @return
	 */
	public boolean checkUserProfileExists(String userId);
}
