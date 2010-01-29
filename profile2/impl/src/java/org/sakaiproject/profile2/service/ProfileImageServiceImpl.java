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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.sakaiproject.id.cover.IdManager;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.GalleryImage;
import org.sakaiproject.profile2.model.ResourceWrapper;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;

public class ProfileImageServiceImpl implements ProfileImageService {

	private static final Logger log = Logger.getLogger(ProfileImageServiceImpl.class);
		
	/**
	 * {@inheritDoc}
	 */
	public boolean addProfileGalleryImage(String userId, byte[] imageBytes,
			String mimeType, String fileName) {

		// check auth and get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if (currentUserUuid == null) {
			throw new SecurityException(
					"You must be logged in to add a gallery image.");
		}

		// convert userId into uuid
		String userUuid = sakaiProxy.getUuidForUserId(userId);
		if (userUuid == null) {
			log.error("Invalid userId: " + userId);
			return false;
		}

		// check admin, or the currentUser and object uuid match
		if (!sakaiProxy.isSuperUser()
				&& !StringUtils.equals(currentUserUuid, userUuid)) {
			throw new SecurityException(
					"You are not allowed to add a gallery image.");
		}

		String imageId = IdManager.createUuid();

		// create resource ID
		String mainResourcePath = sakaiProxy.getProfileGalleryImagePath(userUuid,
				imageId);

		byte[] scaledImageBytes = ProfileUtils.scaleImage(
				imageBytes, ProfileConstants.MAX_GALLERY_IMAGE_XY);
		
		// save image
		if (!sakaiProxy.saveFile(mainResourcePath, userId, fileName, mimeType,
				scaledImageBytes)) {

			log.error("Couldn't add gallery image to CHS. Aborting.");
			return false;
		}

		// create thumbnail
		byte[] thumbnailBytes = ProfileUtils.scaleImage(imageBytes,
				ProfileConstants.MAX_GALLERY_THUMBNAIL_IMAGE_XY);

		String thumbnailResourcePath = sakaiProxy.getProfileGalleryThumbnailPath(userId,
				imageId);

		sakaiProxy.saveFile(thumbnailResourcePath, userId, fileName, mimeType,
				thumbnailBytes);

		if (profileLogic.addNewGalleryImage(new GalleryImage(userUuid,
				mainResourcePath, thumbnailResourcePath, fileName))) {
			return true;
		}
		
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<GalleryImage> getProfileGalleryImages(String userId) {

		// check auth and get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if (currentUserUuid == null) {
			throw new SecurityException(
					"You must be logged in to make a request for a user's gallery images.");
		}

		// convert userId into uuid
		String userUuid = sakaiProxy.getUuidForUserId(userId);
		if (userUuid == null) {
			log.error("Invalid userId: " + userId);
			return null;
		}

		return profileLogic.getGalleryImages(userId);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean removeProfileGalleryImage(String userId, GalleryImage image) {

		// check auth and get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if (currentUserUuid == null) {
			throw new SecurityException(
					"You must be logged in to add a gallery image.");
		}

		// convert userId into uuid
		String userUuid = sakaiProxy.getUuidForUserId(userId);
		if (userUuid == null) {
			log.error("Invalid userId: " + userId);
			return false;
		}

		// check admin, or the currentUser and object uuid match
		if (!sakaiProxy.isSuperUser()
				&& !StringUtils.equals(currentUserUuid, userUuid)) {
			throw new SecurityException(
					"You are not allowed to add a gallery image.");
		}

		if (!sakaiProxy.removeResource(image.getMainResource())) {
			log.error("Gallery image not removed: " + image.getMainResource());
		}
		
		if (!sakaiProxy.removeResource(image.getThumbnailResource())) {
			log.error("Gallery thumbnail not removed: " + image.getThumbnailResource());
		}
		
		if (profileLogic.removeGalleryImage(userId, image.getId())) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ResourceWrapper getProfileImage(String userId, int imageType) {
		
		ResourceWrapper resource = new ResourceWrapper();
		
		//check auth and get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if(currentUserUuid == null) {
			throw new SecurityException("You must be logged in to make a request for a user's profile image.");
		}
		
		//convert userId into uuid
		String userUuid = sakaiProxy.getUuidForUserId(userId);
		if(userUuid == null) {
			log.error("Invalid userId: " + userId);
			return null;
		}
		
		//check friend status
		boolean friend = profileLogic.isUserXFriendOfUserY(userUuid, currentUserUuid);
		
		//check if photo is allowed, if not return default
		if(!profileLogic.isUserXProfileImageVisibleByUserY(userUuid, currentUserUuid, friend)) {
			return getDefaultImage();
		}
		
		//check environment configuration (will be url or upload) and get image accordingly
		//fall back by default. there is no real use case for not doing it.
		if(sakaiProxy.getProfilePictureType() == ProfileConstants.PICTURE_SETTING_URL) {
			String url = profileLogic.getExternalImageUrl(userUuid, imageType);
			if(url == null) {
				return getDefaultImage();
			} else {
				return profileLogic.getURLResourceAsBytes(url);
			}
		} else {
			resource = profileLogic.getCurrentProfileImageForUserWrapped(userUuid, imageType);
			if(resource == null || resource.getBytes() == null) {
				return getDefaultImage();
			} else {
				return resource;
			}
		} 
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean setProfileImage(String userId, byte[] imageBytes, String mimeType, String fileName) {
		
		//check auth and get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if(currentUserUuid == null) {
			throw new SecurityException("You must be logged in to update a user's profile image.");
		}
		
		//convert userId into uuid
		String userUuid = sakaiProxy.getUuidForUserId(userId);
		if(userUuid == null) {
			log.error("Invalid userId: " + userId);
			return false;
		}
		
		//check admin, or the currentUser and object uuid match
		if(!sakaiProxy.isSuperUser() && !StringUtils.equals(currentUserUuid, userUuid)) {
			throw new SecurityException("Not allowed to save.");
		}
		
		//check image is actually allowed to be changed
		if(!sakaiProxy.isProfilePictureChangeEnabled()) {
			log.warn("Profile image changes are not permitted as per sakai.properties setting 'profile2.picture.change.enabled'.");
			return false;
		}
		
		/*
		 * MAIN PROFILE IMAGE
		 */
		//scale image
		imageBytes = ProfileUtils.scaleImage(imageBytes, ProfileConstants.MAX_IMAGE_XY);
		 
		//create resource ID
		String mainResourceId = sakaiProxy.getProfileImageResourcePath(userUuid, ProfileConstants.PROFILE_IMAGE_MAIN);
		
		//save, if error, log and return.
		if(!sakaiProxy.saveFile(mainResourceId, userId, fileName, mimeType, imageBytes)) {
			log.error("Couldn't add main image to CHS. Aborting.");
			return false;
		}

		/*
		 * THUMBNAIL PROFILE IMAGE
		 */
		//scale image
		imageBytes = ProfileUtils.scaleImage(imageBytes, ProfileConstants.MAX_THUMBNAIL_IMAGE_XY);
		 
		//create resource ID
		String thumbnailResourceId = sakaiProxy.getProfileImageResourcePath(userId, ProfileConstants.PROFILE_IMAGE_THUMBNAIL);
		log.debug("Profile.ChangeProfilePicture.onSubmit thumbnailResourceId: " + thumbnailResourceId);
		
		//save, if error, warn, erase thumbnail reference, and continue (we really only need the main image)
		if(!sakaiProxy.saveFile(thumbnailResourceId, userId, fileName, mimeType, imageBytes)) {
			log.warn("Couldn't add thumbnail image to CHS. Main image will be used instead.");
			thumbnailResourceId = null;
		}
		
		/*
		 * SAVE IMAGE RESOURCE IDS
		 */
		//save
		if(profileLogic.addNewProfileImage(userId, mainResourceId, thumbnailResourceId)) {
			return true;
		}
		return false;		
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean setProfileImage(String userId, String url, String thumbnail) {
		
		//check auth and get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if(currentUserUuid == null) {
			throw new SecurityException("You must be logged in to update a user's profile image.");
		}
		
		//convert userId into uuid
		String userUuid = sakaiProxy.getUuidForUserId(userId);
		if(userUuid == null) {
			log.error("Invalid userId: " + userId);
			return false;
		}
		
		//check admin, or the currentUser and object uuid match
		if(!sakaiProxy.isSuperUser() && !StringUtils.equals(currentUserUuid, userUuid)) {
			throw new SecurityException("Not allowed to save.");
		}
		
		//check image is actually allowed to be changed
		if(!sakaiProxy.isProfilePictureChangeEnabled()) {
			log.warn("Profile image changes are not permitted as per sakai.properties setting 'profile2.picture.change.enabled'.");
			return false;
		}
		
		//save it
		return profileLogic.saveExternalImage(userId, url, thumbnail);
	}
	
	/**
	 * This is a helper method to take care of getting the default unavailable image and returning it, along with some metadata about it
	 * @return
	 */
	private ResourceWrapper getDefaultImage() {
		return profileLogic.getURLResourceAsBytes(profileLogic.getUnavailableImageURL());
	}
	
	private SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}
	
	private ProfileLogic profileLogic;
	public void setProfileLogic(ProfileLogic profileLogic) {
		this.profileLogic = profileLogic;
	}

}
