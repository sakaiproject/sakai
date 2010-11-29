package org.sakaiproject.profile2.logic;

import org.sakaiproject.profile2.model.ProfilePreferences;

/**
 * An interface for dealing with ProfilePreferences in Profile2
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 * 
 */
public interface ProfilePreferencesLogic {

	/**
	 * Retrieve the preferences record from the database for this user. If none exists, will
	 * attempt to create one for the user. If that also fails, will return null.
	 * 
	 * <p>Defaults to using the cached version where possible</p>
	 *
	 * @param userId	uuid of the user to retrieve the record for
	 * @return ProfilePreferences record or null
	 */
	public ProfilePreferences getPreferencesRecordForUser(final String userId);
	
	/**
	 * Retrieve the preferences record from the database for this user but the caller has the option
	 * on whether or not to use the cached version (PRFL-504)
	 *
	 * @param userId		uuid of the user to retrieve the record for
	 * @param useCache		whether or not to use the cache
	 * @return ProfilePreferences record or null
	 */
	public ProfilePreferences getPreferencesRecordForUser(final String userId, boolean useCache);
	
	/**
	 * Save the preferences record to the database
	 *
	 * @param profilePreferences	the record for the user
	 */
	public boolean savePreferencesRecord(ProfilePreferences profilePreferences);
	
	/**
	 * Is this type of notification to be sent as an email to the given user?
	 * 
	 * @param userId 	uuid of user
	 * @param messageType type of message
	 * @return
	 */
	public boolean isEmailEnabledForThisMessageType(final String userId, final int messageType);

}
