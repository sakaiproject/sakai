package org.sakaiproject.profile2.service;

import java.util.List;

import org.sakaiproject.profile2.entity.model.Connection;
import org.sakaiproject.profile2.entity.model.UserProfile;

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
	 * Get a customised profile for a user. The type must match one of the ProfileConstants.ENTITY_PROFILE_*
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
	 * @param profileType - see ProfileConstants.ENTITY_PROFILE_*
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
	 * Helper method to check whether a user profile ACTUALLY exists, not just if the user exists.
	 * 
	 * @param userId - either internal user id (6ec73d2a-b4d9-41d2-b049-24ea5da03fca) or eid (jsmith26)
	 * @return true if exists, false otherwise
	 */
	public boolean checkUserProfileExists(String userId);
		
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
	 * Get a list of Connections (as Connection objects) for the given user.
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
	public boolean save(UserProfile userProfile);
	
	/**
	 * Create a UserProfile for the given user and persist it to the database.
	 * @param userId - either internal user id (6ec73d2a-b4d9-41d2-b049-24ea5da03fca) or eid (jsmith26)
	 * @return true/false for success
	 */
	public boolean create(String userId);
	
	/**
	 * Persist the given UserProfile object to the database
	 * @param userProfile - UserProfile that you want persisted
	 * @return true/false for success
	 */
	public boolean create(UserProfile userProfile);
	
}
