package uk.ac.lancs.e_science.profile2.api;

import java.util.Date;
import java.util.List;

import uk.ac.lancs.e_science.profile2.hbm.Friend;
import uk.ac.lancs.e_science.profile2.hbm.ProfilePreferences;
import uk.ac.lancs.e_science.profile2.hbm.ProfilePrivacy;
import uk.ac.lancs.e_science.profile2.hbm.ProfileStatus;
import uk.ac.lancs.e_science.profile2.hbm.SearchResult;

/**
 * This is the internal API to be used by the Profile2 tool and entities only. 
 * 
 * DO NOT IMPLEMENT THIS YOURSELF, use the ProfileService instead (todo)
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */

public interface Profile {

	
	/**
	 * Check content type against allowed types. only JPEG,GIF and PNG are support at the moment
	 *
	 * @param contentType		string of the content type determined by some image parser
	 */
	public boolean checkContentTypeForProfileImage(String contentType);

	/**
	 * Scale an image so one side is a maximum of maxSize in pixels.
	 *
	 * @param imageData		bytes of the original image
	 * @param maxSize		maximum dimension in px that the image should have on any one side
	 */
	public byte[] scaleImage(byte[] imageData, int maxSize);
	
	/**
	 * Convert a Date into a String according to format
	 *
	 * @param date			date to convert
	 * @param format		format in SimpleDateFormat syntax
	 */
	public String convertDateToString(Date date, String format);
	
	/**
	 * Convert a string into a Date object (reverse of above
	 *
	 * @param dateStr		date string to convert
	 * @param format		format of the input date in SimpleDateFormat syntax
	 */
	public Date convertStringToDate(String dateStr, String format);
	
	/**
	 * Get a list of Friends for a given user. Uses a native SQL query so we can use unions
	 * Returns: (all those where userId is the user_uuid) & (all those where user is friend_uuid and confirmed=true)
	 * This will then get all those that the user has confirmed as well as those that they have requested of others
	 *  but may have not been confirmed yet.
	 *  
	 *  If you need this separate, see getFriendRequestsForUser() and getConfirmedFriendUserIdsForUser()
	 *
	 * @param userId		uuid of the user to retrieve the list of friends for
	 * @param limit			number of records to return or 0 for unlimited
	 * @deprecated
	 */
	public List<Friend> getFriendsForUser(String userId, int limit);
	
	/**
	 * Get a list of unconfirmed Friend requests for a given user. Uses a native SQL query
	 * Returns: (all those where userId is the friend_uuid and confirmed=false)
	 *
	 * @param userId		uuid of the user to retrieve the list of friends for
	 */
	public List<String> getFriendRequestsForUser(final String userId);
	
	/**
	 * Get a list of confirmed friends for a given user. Uses a native SQL query so we can use unions
	 * Returns: (all those where userId is the user_uuid and confirmed=true) & (all those where user is friend_uuid and confirmed=true)
	 *
	 * This only returns userIds, as I havent had a need for getting Friend objects yet (ie more than one param returned)
	 * If required, simply implement this again, with a modified HBM query to add the extra fields
	 * and Transform to Friend object.
	 * 
	 * @param userId		uuid of the user to retrieve the list of friends for
	 */
	public List<String> getConfirmedFriendUserIdsForUser(final String userId);
	
	/**
	 * Similar to getConfirmedFriendUserIdsForUser but returns an int of the count of results
	 * 
	 * @param userId		uuid of the user to retrieve the list of friends for
	 */
	public int countConfirmedFriendUserIdsForUser(final String userId);
	
	
	/**
	 * Make a request for friendId to be a friend of userId
	 *
	 * @param userId		uuid of the user making the request
	 * @param friendId		uuid of the user that userId wants to be a friend of
	 */
	public boolean requestFriend(String userId, String friendId);
	
	/**
	 * Check if there is a pending request from fromUser to toUser
	 *
	 * @param fromUser		uuid of the user that made the friend request
	 * @param toUser		uuid of the user that userId made the request to
	 */
	public boolean isFriendRequestPending(String fromUser, String toUser);
	
	/**
	 * Confirm friend request from fromUser to toUser
	 *
	 * @param fromUser		uuid of the user that made the original friend request
	 * @param toUser		uuid of the user that received the friend request
	 * 
	 * Note that fromUser will ALWAYS be the one making the friend request, 
	 * and toUser will ALWAYS be the one who receives the request.
	 */
	public boolean confirmFriendRequest(String fromUser, String toUser);
	
	/**
	 * Ignore a friend request from fromUser to toUser
	 *
	 * @param fromUser		uuid of the user that made the original friend request
	 * @param toUser		uuid of the user that received the friend request and wants to ignore it
	 * 
	 * Note that fromUser will ALWAYS be the one that made the friend request, 
	 * and toUser will ALWAYS be the one who receives the request.
	 */
	public boolean ignoreFriendRequest(String fromUser, String toUser);
	
	/**
	 * Remove a friend connection
	 *
	 * @param userId		uuid of one user
	 * @param userId		uuid of the other user
	 * 
	 * Note that they could be in either column
	 */
	public boolean removeFriend(String userId, String friendId);
	
	/**
	 * Get the number of unread messages
	 *
	 * @param userId		uuid of the user to retrieve the count for
	 */
	public int getUnreadMessagesCount(String userId);
	
	/**
	 * Get the status (message and date) of a user
	 * (this could be private as the other methods call it and its not called externally)
	 * 
	 * Only returns a status object for those that are up to and including one week old. This could be configurable
	 *
	 * @param userId		uuid of the user to get their status for
	 */
	public ProfileStatus getUserStatus(String userId);
	
	/**
	 * Get the status message of a user
	 * NOTE: This is required so the status display model can be dynamically updated with the latest status
	 *
	 * @param userId		uuid of the user to get their status message for
	 */
	public String getUserStatusMessage(String userId);
	
	/**
	 * Get the status date of a user.
	 * NOTE: This is required so the status display model can be dynamically updated with the latest status date
	 *
	 * @param userId		uuid of the user to get their status date for
	 */
	public Date getUserStatusDate(String userId);
	
	/**
	 * Set user status
	 *
	 * @param userId		uuid of the user 
	 * @param status		status to be set
	 */
	public boolean setUserStatus(String userId, String status);
	
	/**
	 * Clear user status
	 *
	 * @param userId		uuid of the user 
	 */
	public boolean clearUserStatus(String userId);
	
	
	/**
	 * Convert a date into a field like (just then, 2 minutes ago, 4 hours ago, yesterday, on sunday, etc)
	 *
	 * @param data		date to convert
	 */
	public String convertDateForStatus(Date date);
	
	/**
	 * Truncate a string and pad it with ... at the end
	 *
	 * @param string	the string to be manipulated
	 * @param size		size the string should be truncated to (not including the ...)
	 */
	public String truncateAndPadStringToSize(String string, int size);
	
	/**
	 * Create a default privacy record where everything is public
	 *
	 * @param userId		uuid of the user to create the record for
	 */
	public ProfilePrivacy createDefaultPrivacyRecord(String userId);
	
	/**
	 * Retrieve the profile privacy record from the database for this user
	 *
	 * @param userId	uuid of the user to retrieve the record for
	 */
	public ProfilePrivacy getPrivacyRecordForUser(String userId);
	
	/**
	 * Save the profile privacy record to the database
	 *
	 * @param profilePrivacy	the record for the user
	 */
	public boolean savePrivacyRecord(ProfilePrivacy profilePrivacy);
	
	/**
	 * Add a new profile image record to the database. Invalidates others before it adds itself.
	 *
	 * @param userId		userId of the user
	 * @param mainResource	the resourceId of the main profile image
	 * @param resourceId	the ContentHosting resource id
	 */
	public boolean addNewProfileImage(String userId, String mainResource, String thumbnailResource);
	
	
	/**
	 * Find all users that match the search string in either name or email. 
	 * This first queries Sakai's UserDirectoryProvider for matches, then queries SakaiPerson and combines the lists
	 * This approach is so that we can get attempt to get all users, with or without profiles.
	 * 
	 * We then check to see if the returned user is a friend of the person performing the search
	 * We then check to see if this person has their privacy settings restricted such that this user should not be
	 * able to see them. We gather some other privacy information, create a SearchResult item and return the List of these
	 * [The above is performed in a private method]
	 *
	 * Once this list is returned, can lookup more info based on SearchResult.getUserUuid()
	 * 
	 * @param search 	string to search for
	 * @param userId 	uuid of user performing the search
	 * @return List 	of SearchResult objects containing a few other pieces of information.
	 */
	public List<SearchResult> findUsersByNameOrEmail(String search, String userId);

	/**
	 * Find all users that match the search string in any of the relevant SakaiPerson fields
	 * 
	 * We then check to see if the returned user is a friend of the person performing the search
	 * We then check to see if this person has their privacy settings restricted such that this user should not be
	 * able to see them. We gather some other privacy information, create a SearchResult item and return the List of these
	 * [The above is performed in a private method]
	 * 
	 * Once this list is returned, can lookup more info based on SearchResult.getUserUuid()
	 * 
	 * @param search 	string to search for
	 * @param userId 	uuid of user performing the search
	 * @return List 	only userIds (for speed and since the list might be very long).
	 */
	public List<SearchResult> findUsersByInterest(String search, String userId);
	
	
	/**
	 * Get a list of all SakaiPerson's (ie list of all people with profile records)
	 *
	 * @return	List of Sakai userId's 
	 */
	public List<String> listAllSakaiPersons();
	
	
	/**
	 * Is userY a friend of the userX?
	 * 
	 * @param userX			the uuid of the user we are querying
	 * @param userY			current user uuid
	 * @return boolean
	 */
	public boolean isUserXFriendOfUserY(String userX, String userY);
	
	
	/**
	 * Get a list of confirmed friends for userX, that is visible by userY, depending on the individual 
	 * privacy settings of each friend
	 * 
	 * - First Calls getConfirmedFriendUserIdsForUser() for userX first,
	 * - Then determines if each person in that list is visible by userY by getting list of userX's friends
	 * 		via (getConfirmedFriendUserIdsForUser) and checking each person in the first list against
	 * 		the second list. This determines friend status.
	 * - Then checks privacy status of each person. If all ok, add to the final list that is returned.
	 * 
	 * @param userX		uuid of the user to retrieve the list of friends for
	 * @param userY		uuid of the user viewing the list
	 */
	public List<String> getFriendsOfUserXVisibleByUserY(final String userX, final String userY);
	
	/**
	 * Get a list of confirmed friends for a given user, that are visible by that user 
	 * (depending on their privacy settings)
	 * 
	 * - First Calls getConfirmedFriendUserIdsForUser() for userId first,
	 * - Then checks privacy status of each person. If all ok, add to the final list that is returned.
	 * 
	 * @param userId		uuid of the user to retrieve the list of friends for
	 */
	public List<String> getVisibleFriendsOfUser(final String userId);
	
	
	
	
	/**
	 * Should this user show up in searches by the given user?
	 * 
	 * @param userX			the uuid of the user we are querying
	 * @param userY			current user uuid
	 * @param friend 		if the current user is a friend of the user we are querying
	 * @return boolean
	 * 
	 * NOTE: userY is currently not used because the friend status between userX and userY has already
	 * been determined, but it is in now in case later we allow blocking/opening up of info to specific users.
	 * 
	 */
	public boolean isUserXVisibleInSearchesByUserY(String userX, String userY, boolean friend);
	
	
	
	/**
	 * Has the user allowed viewing of their profile (including image) by the given user?
	 * ie have they restricted it to only me or friends etc
	 * 
	 * @param userX			the uuid of the user we are querying
	 * @param userY			current user uuid
	 * @param friend 		if the current user is a friend of the user we are querying
	 * @return boolean
	 *
	 * NOTE: userY is currently not used because the friend status between userX and userY has already
	 * been determined, but it is in now in case later we allow blocking/opening up of info to specific users.
	 * 
	 */
	public boolean isUserXProfileVisibleByUserY(String userX, String userY, boolean friend);
	
	
	/**
	 * Has the user allowed viewing of their basic info by the given user?
	 * ie have they restricted it to only me or friends etc
	 * 
	 * @param userX			the uuid of the user we are querying
	 * @param userY			current user uuid
	 * @param friend 		if the current user is a friend of the user we are querying
	 * @return boolean
	 *
	 * NOTE: userY is currently not used because the friend status between userX and userY has already
	 * been determined, but it is in now in case later we allow blocking/opening up of info to specific users.
	 * 
	 */
	public boolean isUserXBasicInfoVisibleByUserY(String userX, String userY, boolean friend);
	
	/**
	 * Has the user allowed viewing of their contact info by the given user?
	 * ie have they restricted it to only me or friends etc
	 * 
	 * @param userX			the uuid of the user we are querying
	 * @param userY			current user uuid
	 * @param friend 		if the current user is a friend of the user we are querying
	 * @return boolean
	 *
	 * NOTE: userY is currently not used because the friend status between userX and userY has already
	 * been determined, but it is in now in case later we allow blocking/opening up of info to specific users.
	 * 
	 */
	public boolean isUserXContactInfoVisibleByUserY(String userX, String userY, boolean friend);
	
	/**
	 * Has the user allowed viewing of their personal info by the given user?
	 * ie have they restricted it to only me or friends etc
	 * 
	 * @param userX			the uuid of the user we are querying
	 * @param userY			current user uuid
	 * @param friend 		if the current user is a friend of the user we are querying
	 * @return boolean
	 *
	 * NOTE: userY is currently not used because the friend status between userX and userY has already
	 * been determined, but it is in now in case later we allow blocking/opening up of info to specific users.
	 * 
	 */
	public boolean isUserXPersonalInfoVisibleByUserY(String userX, String userY, boolean friend);
	
	/**
	 * Has the user allowed viewing of their friends list (which in turn has its own privacy associated for each record)
	 * by the given user? ie have they restricted it to only me or friends etc
	 * 
	 * @param userX			the uuid of the user we are querying
	 * @param userY			current user uuid
	 * @param friend 		if the current user is a friend of the user we are querying
	 * @return boolean
	 *
	 * NOTE: userY is currently not used because the friend status between userX and userY has already
	 * been determined, but it is in now in case later we allow blocking/opening up of info to specific users.
	 * 
	 */
	public boolean isUserXFriendsListVisibleByUserY(String userX, String userY, boolean friend);
	
	
	/**
	 * Has the user allowed viewing of their birth year in their profile. 
	 * This is either on or off and does not depend on friends etc
	 * 
	 * @param userId			the uuid of the user we are querying
	 * @return boolean
	 */
	public boolean isBirthYearVisible(String userId);
	
	
	
	/**
	 * Get the profile image for the given user
	 * First calls getCurrentProfileImageRecord to get the record, then using the URLs contained within
	 * calls sakaiProxy.getResource(resource_id) to get the bytes.
	 * 
	 * @param userId 		the uuid of the user we are querying
	 * @param imageType		comes from ProfileImageManager and maps to a directory in ContentHosting
	 * @return image as bytes
	 */
	public byte[] getCurrentProfileImageForUser(String userId, int imageType);
	
	
	/**
	 * Does this user have a profile image record?
	 * Calls getCurrentProfileImageRecord to see if a record exists.
	 * 
	 * This is mainly used by the convertProfile() method, but could have another use.
	 * 
	 * @param userId 		the uuid of the user we are querying
	 * @return boolean		true if it exists/false if not
	 */
	public boolean hasProfileImage(String userId);
	
	
	/**
	 * Create a default preferences record for the user
	 *
	 * @param userId		uuid of the user to create the record for
	 */
	public ProfilePreferences createDefaultPreferencesRecord(final String userId);
	
	/**
	 * Retrieve the preferences record from the database for this user
	 *
	 * @param userId	uuid of the user to retrieve the record for
	 */
	public ProfilePreferences getPreferencesRecordForUser(final String userId);
	
	/**
	 * Save the preferences record to the database
	 *
	 * @param profilePreferences	the record for the user
	 */
	public boolean savePreferencesRecord(ProfilePreferences profilePreferences);
	
	/**
	 * Check if twitter integration is enabled for a user
	 *
	 * @param userId	uuid of the user
	 */
	public boolean isTwitterIntegrationEnabled(final String userId);
	
	/**
	 * Send a message to twitter
	 *
	 * @param userId	uuid of the user
	 * @param message	the message
	 */
	public void sendMessageToTwitter(final String userId, final String message);
	
	/**
	 * Generate a tiny URL for the supplied URL
	 * 
	 * @param url
	 * @return
	 */
	public String generateTinyUrl(final String url);
	
	/**
	 * Is this type of notification to be sent as an email to the given user?
	 * 
	 * @param userId 	uuid of user
	 * @param messageType type of message
	 * @return
	 */
	public boolean isEmailEnabledForThisMessageType(final String userId, final int messageType);
}
