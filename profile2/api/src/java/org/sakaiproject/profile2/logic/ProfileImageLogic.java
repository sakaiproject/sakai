/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
     * Get the blank profile image, the one a user sees if there is
     * no other image available.
     */
	public ProfileImage getBlankProfileImage();

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
	 * Gets the official profile image for a user.
	 * @param userUuid
	 * @param siteId siteId to check that the requesting user has roster.viewofficialphoto permission
	 * @return The ProfileImage object, populated with either a url or binary data.
	 */
	public ProfileImage getOfficialProfileImage(String userUuid, String siteId);
	
	/**
	 * Get the profile image for a user. Takes into account all global settings, user preferences, privacy and permissions in the given site.
	 * See getProfileImage(String, ProfilePreferences, ProfilePrivacy, int);
	 * @param userUuid
	 * @param prefs
	 * @param privacy
	 * @param size
	 * @param siteId - optional siteid to check if the current user has permissions in this site to see the target user's image (PRFL-411)
	 * @return
	 */
	public ProfileImage getProfileImage(String userUuid, ProfilePreferences prefs, ProfilePrivacy privacy, int size, String siteId);

	
	/**
	 * Get the profile image for a user. See getProfileImage(String, ProfilePreferences, ProfilePrivacy, int);
	 * @param person	Person object that contains all info about a user
	 * @param size		size of image to return.
	 * @return
	 */
	public ProfileImage getProfileImage(Person person, int size);
	
	/**
	 * Get the profile image for a user. See getProfileImage(String, ProfilePreferences, ProfilePrivacy, int);
	 * @param person	Person object that contains all info about a user
	 * @param size		size of image to return.
	 * @param siteId - optional siteid to check if the current user has permissions in this site to see the target user's image (PRFL-411)
	 * @return
	 */
	public ProfileImage getProfileImage(Person person, int size, String siteId);
	
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
	 * @param fullSizeUrl - url for main image
	 * @param thumbnailUrl - thumbnail for main image to be used when thumbnail sizes are requested. 
	 * Leave blank or null for none and when a thumbnail is requested it will return the full image which can be scaled in the markup.
	 * @param avatar - avatar for main image to be used when avatar sizes are requested. Can be blank and it will fallback as per thumbnail
	 * @return
	 */
	public boolean setExternalProfileImage(String userUuid, String fullSizeUrl, String thumbnailUrl,  String avatar);
	
	/**
	 * Get the full URL to the default unavailable image defined in ProfileConstants
	 * @return
	 */
	public String getUnavailableImageURL();
	
	/**
	 * Get the full URL to the default unavailable image thumbnail defined in ProfileConstants
	 * @return
	 */
	public String getUnavailableImageThumbnailURL();
	
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
	 * Retrieves all gallery images in randomized order for the specified user.
	 * 
	 * @param userUuid the ID of the user.
	 * @return all profile gallery images in randomized order for the specified
	 *         user.
	 */
	public List<GalleryImage> getGalleryImagesRandomized(String userUuid);
	
	/**
	 * Remove the specified gallery image.
	 * 
	 * @param userUuid the user ID.
	 * @param imageUuid the image ID.
	 * @return <code>true</code> if the gallery image is successfully removed,
	 *         <code>false</code> if the gallery image is not removed.
	 */
	public boolean removeGalleryImage(String userId, long imageId);
	
	/**
	 * Save the official image url that institutions can set.
	 * @param userUuid		uuid of the user
	 * @param url			url to image
	 * @return
	 */
	public boolean saveOfficialImageUrl(final String userUuid, final String url);
	
	/**
	 * Get the entity url to a profile image for a user.
	 *  
	 * It can be added to any profile without checks as the retrieval of the image does the checks, and a default image
	 * is used if not allowed or none available.
	 * 
	 * @param userUuid	uuid for the user
	 * @param size		size of image, from ProfileConstants
	 */
	public String getProfileImageEntityUrl(String userUuid, int size);
	
	/**
	 * Get a count of the number of gallery images for a user
	 * @param userUuid	uuid for the user
	 * @return
	 */
	public int getGalleryImagesCount(final String userUuid);
	
	/**
	 * Generate a gravatar URL for a user
	 * 
	 * <p>URLs are of the form http://www.gravatar.com/avatar/HASH?s=200 where HASH is an MD5 hash of the user's email address and s is the size.
	 * We always use the larger size (200) and then scale it in the markup where required, to take advantage of caching.</p>
	 * <p>If no email address for the user, returns null.</p>
	 * @param userUuid	uuid for the user
	 * @return gravatar URL or null
	 */
	public String getGravatarUrl(final String userUuid);
	
	/**
	 * Reset the profile image for a user
	 * 
	 * @param userUuid uuid for the user
	 * @return
	 */
	public boolean resetProfileImage(final String userUuid);
	
	/**
	 * Does this use have a default profile image?
	 * 
	 * @param userUuid uuid for the user
	 * @return
	 */
	public boolean profileImageIsDefault(final String userUuid);
	
}
