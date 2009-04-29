package uk.ac.lancs.e_science.profile2.api;

import java.util.List;

import uk.ac.lancs.e_science.profile2.api.entity.model.Connection;
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
	 * @return the minimum UserProfile for the user, ie name only
	 */
	public UserProfile getPrototype(String userId);
	
	/**
	 * Get a full UserProfile for the given userId
	 * 
	 * <p>All users have profiles, even if they haven't filled it in yet. 
	 * At a very minimum it will contain their name. Privacy checks will determine visibility of other fields</p>
	 * 
	 * <p>If they don't have a SakaiPerson object to get the data from, a prototype is used.</p>
	 * <p>If they do, the SakaiPerson object is transformed into a UserProfile object.
	 * 
	 * @param userId - either internal user id (6ec73d2a-b4d9-41d2-b049-24ea5da03fca) or eid (jsmith26)
	 * @param currentUser - either internal user id or eid for user that is making the request for the profile.
	 * @return UserProfile for the user, that is visible to the requesting user
	 */
	public UserProfile getFullUserProfile(String userId, String currentUser);
	
	/**
	 * Get a minimal UserProfile for a user. Contains name, uuid and status msg/date only. Useful for lists of users.
	 * 
	 * @param userId - either internal user id (6ec73d2a-b4d9-41d2-b049-24ea5da03fca) or eid (jsmith26)
	 * @param currentUser - either internal user id or eid for user that is making the request for the profile.
	 * @return UserProfile for the user, that is visible to the requesting user
	 */
	public UserProfile getMinimalUserProfile(String userId, String currentUser);

	/**
	 * Checks whether a user profile exists. 
	 * <p>This actually just checks for the existence of a user in the system as every user has a profile, even if it is blank.</p>
	 * 
	 * @param userId - either internal user id (6ec73d2a-b4d9-41d2-b049-24ea5da03fca) or eid (jsmith26)
	 * @return true if exists, false otherwise
	 */
	public boolean checkUserProfileExists(String userId);
	
	/**
	 * Get the profile image for a user
	 * @param userId - either internal user id (6ec73d2a-b4d9-41d2-b049-24ea5da03fca) or eid (jsmith26)
	 * @param currentUser - either internal user id or eid for user that is making the request for the image.
	 * @param imageType - type of image, main or thumbnail, mapped via ProfileImageManager
	 * @return byte[] or null if not allowed or none
	 */
	public byte[] getProfileImage(String userId, String currentUser, int imageType);
	
	
	/**
	 * Get a list of connections (as userIds) for the given user.
	  * @param userId - either internal user id (6ec73d2a-b4d9-41d2-b049-24ea5da03fca) or eid (jsmith26)
	 * @param currentUser - either internal user id or eid for user that is making the request for the list.
	 * @return List of uuids or null if error
	 */
	public List<String> getConnectionIdsForUser(String userId, String currentUser);
	
	/**
	 * Get a list of Connections (as Collection objects) for the given user.
	  * @param userId - either internal user id (6ec73d2a-b4d9-41d2-b049-24ea5da03fca) or eid (jsmith26)
	 * @param currentUser - either internal user id or eid for user that is making the request for the list.
	 * @return List of Connections or null if error
	 */
	public List<Connection> getConnectionsForUser(String userId, String currentUser);
	
	
	//public String getExternalProfileImageUrl(String userId, String currentUser);

}
