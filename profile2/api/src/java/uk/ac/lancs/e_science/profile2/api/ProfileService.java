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
	 * <p>You must be logged-in in order to make requests to this method as the content returned will be tailored
	 * to be visible for the currently logged in user.</p>
	 * 
	 * <p>If they don't have a SakaiPerson object to get the data from, a prototype is used.</p>
	 * <p>If they do, the SakaiPerson object is transformed into a UserProfile object.
	 * 
	 * @param userId - either internal user id (6ec73d2a-b4d9-41d2-b049-24ea5da03fca) or eid (jsmith26)
	 * @return UserProfile for the user, that is visible to the requesting user
	 */
	public UserProfile getFullUserProfile(String userId);
	
	/**
	 * Get a minimal UserProfile for a user. Contains name, uuid and status msg/date only. Useful for lists of users.
	 * 
	 * <p>You must be logged-in in order to make requests to this method as the content returned will be tailored
	 * to be visible for the currently logged in user.</p>
	 * 
	 * @param userId - either internal user id (6ec73d2a-b4d9-41d2-b049-24ea5da03fca) or eid (jsmith26)
	 * @return UserProfile for the user, that is visible to the requesting user
	 */
	public UserProfile getMinimalUserProfile(String userId);

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
	 * 
	 * <p>You must be logged-in in order to make requests to this method.</p>
	 * 
	 * @param userId - either internal user id (6ec73d2a-b4d9-41d2-b049-24ea5da03fca) or eid (jsmith26)
	 * @param imageType - type of image, main or thumbnail, mapped via ProfileImageManager
	 * @return byte[] or null if not allowed or none
	 */
	public byte[] getProfileImage(String userId, int imageType);
	
	
	/**
	 * Get a list of connections (as userIds) for the given user.
	 * 
	 * <p>You must be logged-in in order to make requests to this method.</p>
	 * 
	 * @param userId - either internal user id (6ec73d2a-b4d9-41d2-b049-24ea5da03fca) or eid (jsmith26)
	 * @return List of uuids or null if error
	 */
	public List<String> getConnectionIdsForUser(String userId);
	
	/**
	 * Get a list of Connections (as Collection objects) for the given user.
	 * 
	 * <p>You must be logged-in in order to make requests to this method.</p>
	 * 
	 * @param userId - either internal user id (6ec73d2a-b4d9-41d2-b049-24ea5da03fca) or eid (jsmith26)
	 * @return List of Connections or null if error
	 */
	public List<Connection> getConnectionsForUser(String userId);
	
	/**
	 * Get the external image url for a user
	 * 
	 * If a thumbnail is requested and none is found, it will by default, fallback to the main image url.
	 * 
	 * @param userId - either internal user id (6ec73d2a-b4d9-41d2-b049-24ea5da03fca) or eid (jsmith26)
	 * @param imageType - type of image, main or thumbnail, mapped via ProfileImageManager
	 * @param fallback - if a thumbnail is requested but it does not exist, should the mainURL be returned instead? 
	 * 					This should generally always be used and the full sized image can just be scaled in the markup.
	 * 					If used with the main type of image, it has no effect.
	 * @return String url or null if error or none
	 */
	public String getExternalProfileImageUrl(String userId, int imageType, boolean fallback);

}
