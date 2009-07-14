package org.sakaiproject.profile2.service;

import org.sakaiproject.profile2.model.ResourceWrapper;

public interface ProfileImageService {

	
	/**
	 * Get the profile image for a user
	 * 
	 * Checks the configuration settings for Profile2 and returns accordingly. If the file has been uploaded, will return bytes. If the file is a URL, will send a redirect for that resource. 
	 * <p>Will return default image defined in ProfileConstants.UNAVAILABLE_IMAGE_FULL if there is no image or privacy checks mean it is not allowed.
	 * <p>If the userId is invalid, will return null.
	 * <p>If a thumbnail is requested but does not exist, it will fall back to the full sized image and return that, which can just be scaled down in the markup.
	 * <p>You must be logged-in in order to make requests to this method.
	 * 
	 * @param userId - either internal user id (6ec73d2a-b4d9-41d2-b049-24ea5da03fca) or eid (jsmith26)
	 * @param imageType - type of image, main or thumbnail, mapped via ProfileConstants
	 * @return ResourceWrapper of the wrapped bytes or null if not allowed/none
	 */
	public ResourceWrapper getProfileImage(String userId, int imageType);
	
	/**
	 * Update the profile image for a user given the byte[] of the image.
	 * <p>Must be logged in. Will fail if you are not allowed to update the record.
	 * <p>Will work, but not have visible effect if the setting for the image type used in sakai.properties is not upload. ie its using URL instead
	 * 
	 * @param userId - either internal user id (6ec73d2a-b4d9-41d2-b049-24ea5da03fca) or eid (jsmith26)
	 * @param imageBytes - byte[] of image
	 * @param mimeType - mimetype of image, ie image/jpeg
	 * @param filename - name of file, used by ContentHosting, optional.
	 * @return
	 */
	public boolean setProfileImage(String userId, byte[] imageBytes, String mimeType, String fileName); 
	
	/**
	 * Update the profileImage for a user given the URL to an image
	 * <p>Must be logged in. Will fail if you are not allowed to update the record.
	 * <p>Will work, but not have visible effect if the setting for the image type used in sakai.properties is not url. ie its using an uploaded image instead
	 * 
	 * @param userId - either internal user id (6ec73d2a-b4d9-41d2-b049-24ea5da03fca) or eid (jsmith26)
	 * @param url - url for main image
	 * @param thumbnail - thumbnail for main image to be used when thumbnail sizes are requested. 
	 * Leave blank or null for none (ie its nullsafe) and when a thumbnail is requested it will return the full image which will be scaled in the markup.
	 * @return
	 */
	public boolean setProfileImage(String userId, String url, String thumbnail);
}
