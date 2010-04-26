package org.sakaiproject.profile2.logic;

import java.util.List;

import org.sakaiproject.profile2.model.GalleryImage;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.model.ProfileImage;
import org.sakaiproject.profile2.model.ProfilePreferences;
import org.sakaiproject.profile2.model.ProfilePrivacy;

/**
 * An interface for dealing with images in Profile2
 * 
 * <p>Also takes care of image conversion from classic Profile to Profile2</p>
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 * 
 */
public interface ProfileImageLogic {

	/**
	 * Get the profile image for a user. Takes into account all global settings, user preferences and privacy.
	 * 
	 * <p>If making a request for your own image</p>
	 * <ul>
	 * 	<li>'privacy' can be null as it won't be considered.</li>
	 *  <li>You should provide 'prefs' (if available) otherwise it will be looked up.</li>
	 * </ul>
	 * 
	 * <p>If making a request for someone else's image</p>
	 * <ul>
	 * 	<li>You should provide the privacy settings for that user (if available), otherwise it will be looked up.</li>
	 *  <li>You should provide the preferences object for that user (if available), otherwise it will be looked up.</li>
	 *  <li>If privacy is null, a default image will be returned</li>
	 *  <li>If preferences is still null, the global preference will be used, which may not exist and therefore be default.</li>
	 * </ul>
	 * 
	 * <p>The returned ProfileImage object is a wrapper around all of the types of image that can be set. use the getBinarty and getUrl() methods on this object to get the data.
	 * See the docs on ProfileImage for how to use this.
	 *  
	 * @param userUuid
	 * @param prefs
	 * @param privacy
	 * @param size
	 * @return
	 */
	public ProfileImage getProfileImage(String userUuid, ProfilePreferences prefs, ProfilePrivacy privacy, int size);
	
	/**
	 * Get the profile image for a user. See getProfileImage(String, ProfilePreferences, ProfilePrivacy, int);
	 * @param person	Person object that contains all info about a user
	 * @param size		size of image to return.
	 * @return
	 */
	public ProfileImage getProfileImage(Person person, int size);
	
	/**
	 * Update the profile image for a user given the byte[] of the image.
	 * <p>Will work, but not have visible effect if the setting for the image type used in sakai.properties is not upload. ie its using URL instead
	 * 
	 * @param userUuid - uuid for the user
	 * @param imageBytes - byte[] of image
	 * @param mimeType - mimetype of image, ie image/jpeg
	 * @param filename - name of file, used by ContentHosting, optional.
	 * @return
	 */
	public boolean setUploadedProfileImage(String userUuid, byte[] imageBytes, String mimeType, String fileName); 
	
	
	/**
	 * Update the profileImage for a user given the URL to an image
	 * <p>Will work, but not have visible effect if the setting for the image type used in sakai.properties is not url. ie its using an uploaded image instead
	 * 
	 * @param userUuid - uuid for the user
	 * @param url - url for main image
	 * @param thumbnail - thumbnail for main image to be used when thumbnail sizes are requested. 
	 * Leave blank or null for none and when a thumbnail is requested it will return the full image which can be scaled in the markup.
	 * @return
	 */
	public boolean setExternalProfileImage(String userUuid, String url, String thumbnail);
	
	/**
	 * Add a gallery image for the specified user.
	 * 
	 * @param userUuid the ID of the user.
	 * @param imageBytes the image bytes.
	 * @param mimeType the MIME type of the image.
	 * @param fileName the filename of the image.
	 * @return <code>true</code> if the gallery image is successfully added,
	 *         <code>false</code> if the gallery image is not added.
	 */
	public boolean addGalleryImage(String userUuid, byte[] imageBytes,String mimeType, String fileName);

	/**
	 * Retrieves all gallery images for the specified user.
	 * 
	 * @param userUuid the ID of the user.
	 * @return all profile gallery images for the specified user.
	 */
	public List<GalleryImage> getGalleryImages(String userUuid);

	/**
	 * Remove the specified gallery image.
	 * 
	 * @param userUuid the user ID.
	 * @param imageUuid the image ID.
	 * @return <code>true</code> if the gallery image is successfully removed,
	 *         <code>false</code> if the gallery image is not removed.
	 */
	public boolean removeGalleryImage(String userId, long imageId);
	
}
