package uk.ac.lancs.e_science.profile2.api;

import java.util.Date;
import java.util.List;

import org.sakaiproject.api.common.edu.person.SakaiPerson;

import uk.ac.lancs.e_science.profile2.hbm.Friend;
import uk.ac.lancs.e_science.profile2.hbm.ProfileImage;
import uk.ac.lancs.e_science.profile2.hbm.ProfilePrivacy;
import uk.ac.lancs.e_science.profile2.hbm.ProfileStatus;


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
	 * @param userId		uuid of the user to retrieve the list of friends for
	 * @param limit			number of records to return or 0 for unlimited
	 */
	public List<Friend> getFriendsForUser(String userId, int limit);
	
	/**
	 * Get a list of uncorfirmed Friend requests for a given user. Uses a native SQL query
	 * Returns: (all those where userId is the friend_uuid and confirmed=false)
	 *
	 * @param userId		uuid of the user to retrieve the list of friends for
	 * @param limit			number of records to return or 0 for unlimited
	 */
	public List<Friend> getFriendRequestsForUser(String userId);

	/**
	 * Make a request for friendId to be a friend of userId
	 *
	 * @param userId		uuid of the user making the request
	 * @param friendId		uuid of the user that userId wants to be a friend of
	 */
	public boolean requestFriend(String userId, String friendId);
	
	/**
	 * Confirm that userId is a friend of friendId (from a pending friend request)
	 *
	 * @param friendId		uuid of the user that received the friend request
	 * @param userId		uuid of the user that made the original friend request
	 * 
	 * Note that userId will ALWAYS be the one making the friend request, and friendId
	 * will ALWAYS be the one who receives the request.
	 */
	public boolean confirmFriend(String friendId, String userId);
	
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
	 * Get the current ProfileImage record from the database.
	 * There should only ever be one, but if there are more this will return the latest. 
	 * This is called when retrieving the profileImage for a user. When adding a new image, there is a call
	 * to a private method called getCurrentProfileImageRecords() which should invalidate any multiple current images
	 *
	 * @param userId		userId of the user
	 */
	
	public ProfileImage getCurrentProfileImageRecord(final String userId);
	
	/**
	 * Get old ProfileImage records from the database. Used for displaying old the profile pictures album.
	 *
	 * @param userId		userId of the user
	 */
	public List<ProfileImage> getOtherProfileImageRecords(final String userId);

	/**
	 * Find all users that match the search string in either name or email. 
	 * THis first queries Sakai's UserDirectoryProvider for matches, then queries SakaiPerson and combines the lists
	 * This approach is so that we can get attempt to get all users, with or without profiles.
	 * 
	 * Returns only user_uuids for speed
	 * 
	 * Once this list is returned, paginate and lookup sets of SakaiPersons and their associated Privacy and Image records.
	 * 
	 * @param search 	string to search for
	 * @return List 	only userIds (for speed and since the list might be very long).
	 */
	public List<String> findUsersByNameOrEmail(String search);
	

	/**
	 * Find all users that match the search string in any of the relevant SakaiPerson fields
	 * 
	 * Returns only user_uuids for speed
	 * 
	 * Once this list is returned, paginate and lookup sets of SakaiPersons and their associated Privacy and Image records.
	 * 
	 * @param search 	string to search for
	 * @return List 	only userIds (for speed and since the list might be very long).
	 */
	public List<String> findUsersByInterest(String search);
}
