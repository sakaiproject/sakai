/**
 * Copyright (c) 2008-2010 The Sakai Foundation
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

package org.sakaiproject.profile2.service;

import java.util.List;

import org.sakaiproject.profile2.model.GalleryImage;
import org.sakaiproject.profile2.model.ResourceWrapper;

public interface ProfileImageService {

	/**
	 * Add a profile gallery image for the specified user.
	 * 
	 * @param userId the ID of the user.
	 * @param imageBytes the image bytes.
	 * @param mimeType the MIME type of the image.
	 * @param fileName the filename of the image.
	 * @return <code>true</code> if the gallery image is successfully added,
	 *         <code>false</code> if the gallery image is not added.
	 */
	public boolean addProfileGalleryImage(String userId, byte[] imageBytes,
			String mimeType, String fileName);

	/**
	 * Retrieves all profile gallery images for the specified user.
	 * 
	 * @param userId the ID of the user.
	 * @return all profile gallery images for the specified user.
	 */
	public List<GalleryImage> getProfileGalleryImages(String userId);

	/**
	 * Remove the specified profile gallery image.
	 * 
	 * @param userId the ID of the user.
	 * @param image the image to remove.
	 * @return <code>true</code> if the gallery image is successfully removed,
	 *         <code>false</code> if the gallery image is not removed.
	 */
	public boolean removeProfileGalleryImage(String userId, GalleryImage image);
	
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
