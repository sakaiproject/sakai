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
	 * Get the academic UserProfile for a user.
	 * 
	 * <p>You must be logged-in in order to make requests to this method as the content returned will be tailored
	 * to be visible for the currently logged in user.</p>
	 * 
	 * @param userId - either internal user id (6ec73d2a-b4d9-41d2-b049-24ea5da03fca) or eid (jsmith26)
	 * @return UserProfile for the user, that is visible to the requesting user
	 */
	public UserProfile getAcademicUserProfile(String userId);
	
	
	/**
	 * Get a customised profile for a user. The type must match one of the ProfileUtilityManager.ENTITY_PROFILE_*
	 * If none is given, defaults to full.
	 * 
	 * <p>The set of fields can be configured in sakai.properties (except for full and minimal)</p>
	 * 
	 * <p>You must be logged-in in order to make requests to this method as the content returned will be tailored
	 * to be visible for the currently logged in user, even if the fields are explicitly specified.</p>
	 * 
	 * <p>Not yet implemented</p>
	 * 
	 * @param userId - either internal user id (6ec73d2a-b4d9-41d2-b049-24ea5da03fca) or eid (jsmith26)
	 * @param profileType - see ProfileUtilityManager.ENTITY_PROFILE_*
	 * @return UserProfile for the user, that is visible to the requesting user
	 */
	public UserProfile getCustomUserProfile(String userId, int profileType);

	/**
	 * Checks whether a user exists. 
	 * <p>This actually just checks for the existence of a user in the system as every user has a profile, even if it is blank.</p>
	 * 
	 * @param userId - either internal user id (6ec73d2a-b4d9-41d2-b049-24ea5da03fca) or eid (jsmith26)
	 * @return true if exists, false otherwise
	 */
	public boolean checkUserExists(String userId);
	
	/**
	 * Checks whether a user profile ACTUALLY exists. 
	 * <p>Sometimes we need to check if the profile really does already exist.</p>
	 * 
	 * @param userId - either internal user id (6ec73d2a-b4d9-41d2-b049-24ea5da03fca) or eid (jsmith26)
	 * @return true if exists, false otherwise
	 */
	public boolean checkUserProfileExists(String userId);
	
	/**
	 * Get the profile image for a user
	 * 
	 * <p>Checks the configuration settings for Profile2 and returns accordingly. If the file has been uploaded, will return bytes. If the file is a URL, will send a redirect for that resource. 
	 * <p>Will return default image defined in ProfileImageManager.UNAVAILABLE_IMAGE_FULL if there is no image or privacy checks mean it is not allowed.</p>
	 * <p>If the userId is invalid, will return null.</p>
	 * <p>If a thumbnail is requested but does not exist, it will fall back to the full sized image and return that, which can just be scaled down in the markup.</p>
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
	 * Convert a UserProfile object into a fragment of HTML.
	 * The HTML is fully abstracted so it can be styled and rearranged as desired.
	 * 
	 * @param userProfile
	 * @return formatted HTML rady for use or for futher styling if required.
	 */
	public String getUserProfileAsHTML(UserProfile userProfile);
	
	/**
	 * Save the given UserProfile. Checks currentUser against the userProfile supplied. 
	 * A user can update only their own profile, no one elses.
	 * 
	 * @param userProfile
	 * @return true/false for success
	 */
	public String save(UserProfile userProfile);
	
	/**
	 * Create a UserProfile for the given user and persist it to the database.
	 * @param userId - either internal user id (6ec73d2a-b4d9-41d2-b049-24ea5da03fca) or eid (jsmith26)
	 * @return String of user's uuid
	 */
	public String create(String userId);
	
	/**
	 * Persist the given UserProfile object to the database
	 * @param userProfile - UserProfile that you want persisted
	 * @return String of user's uuid
	 */
	public String create(UserProfile userProfile);
	
}
