package uk.ac.lancs.e_science.profile2.api;

import java.util.Date;
import java.util.List;



public interface Profile {
	
	
	public String getUserStatus(String userId);
	public String getUserStatusLastUpdated(String userId);
	
	public boolean checkContentTypeForProfileImage(String contentType);

	public byte[] scaleImage (byte[] imageData, int maxSize);
	
	public Date convertStringToDate(String dateStr);
	
	public String convertDateToString(Date date);

	/**
	 * Get a list of uuid's that are friends with a given user
	 *
	 * @param userId		uuid of the user to retrieve the list of friends for
	 * @param confirmed		toggles list between confirmed and pending friends
	 */
	public List getFriendsForUser(String userId, boolean confirmed);
	
}
