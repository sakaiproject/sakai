package uk.ac.lancs.e_science.profile2.api;

import java.util.Date;
import java.util.List;

import uk.ac.lancs.e_science.profile2.hbm.Friend;
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
	public byte[] scaleImage (byte[] imageData, int maxSize);
	
	
	/**
	 * Get a list of friend uuids that are friends with a given user. Uses a native SQL query so we can use unions
	 *
	 * @param userId		uuid of the user to retrieve the list of friends for
	 * @param limit			number of records to return or 0 for unlimited
	 */
	public List<Friend> getFriendsForUser(String userId, int limit);
	
	
	
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
	 * Get the number of unread messages
	 *
	 * @param userId		uuid of the user to retrieve the count for
	 */
	public int getUnreadMessagesCount(String userId);
	
	
		
	/**
	 * Get the status (message and date) of a user
	 * (this could be private as the other methods call it and its not called externally)
	 *
	 * @param userId		uuid of the user to get their status for
	 */
	public ProfileStatus getUserStatus(String userId);
	
	/**
	 * Get the status message of a user
	 *
	 * @param userId		uuid of the user to get their status message for
	 */
	public String getUserStatusMessage(String userId);
	
	/**
	 * Get the status date of a user
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
	 * Convert a date into a field like (today, yesterday, last week, etc)
	 *
	 * @param data		date to convert
	 * 
	 * convert the date out into a better format:
		 * if today = 'today'
		 * if yesterday = 'yesterday'
		 * if this week = 'on Weekday'
		 * if last week = 'last week'
		 * past a week ago we don't display the status last updated
		
	 * 
	 * 
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
	 * Save the profile privacy record to the database for this user
	 *
	 * @param profilePrivacy	the record for the user
	 */
	public boolean savePrivacyRecordForUser(ProfilePrivacy profilePrivacy);
	
	
}
