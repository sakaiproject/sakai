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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.profile2.dao.ProfileDao;
import org.sakaiproject.profile2.hbm.model.ProfileImageExternal;
import org.sakaiproject.profile2.hbm.model.ProfileImageOfficial;
import org.sakaiproject.profile2.hbm.model.ProfileImageUploaded;
import org.sakaiproject.profile2.model.GalleryImage;
import org.sakaiproject.profile2.model.MimeTypeByteArray;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.model.ProfileImage;
import org.sakaiproject.profile2.model.ProfilePreferences;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.types.PrivacyType;
import org.sakaiproject.profile2.util.Messages;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;
import org.sakaiproject.user.api.User;

/**
 * Implementation of ProfileImageLogic API
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Slf4j
public class ProfileImageLogicImpl implements ProfileImageLogic {

	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public ProfileImage getBlankProfileImage() {

		ProfileImage profileImage = new ProfileImage();
        profileImage.setExternalImageUrl(getUnavailableImageURL());
        profileImage.setDefault(true);
        return profileImage;
    }
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public ProfileImage getProfileImage(String userUuid, ProfilePreferences prefs, ProfilePrivacy privacy, int size) {
		return getProfileImage(userUuid, prefs, privacy, size, null);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public ProfileImage getOfficialProfileImage(String userUuid, String siteId) {
		
		ProfileImage profileImage = new ProfileImage();
		profileImage.setDefault(false); //will be overridden if required
		
		String currentUserId = sakaiProxy.getCurrentUserId();
		String defaultImageUrl = getUnavailableImageURL();
		
		//check permissions. if not allowed, set default and return
		if(!sakaiProxy.isUserMyWorkspace(siteId)) {
			log.debug("checking if user: " + currentUserId + " has permissions in site: " + siteId);
			if(!sakaiProxy.isUserAllowedInSite(currentUserId, ProfileConstants.ROSTER_VIEW_PHOTO, siteId)) {
				profileImage.setExternalImageUrl(defaultImageUrl);
				profileImage.setDefault(true);
				return profileImage;
			}
		}
		
		//otherwise get official image
		return getOfficialImage(userUuid, profileImage, defaultImageUrl, StringUtils.equals(userUuid,currentUserId));
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public ProfileImage getProfileImage(String userUuid, ProfilePreferences prefs, ProfilePrivacy privacy, int size, String siteId) {
		
		ProfileImage image = new ProfileImage();
		boolean allowed = false;
		boolean isSameUser = false;
		
		image.setDefault(false); //will be overridden if it is actually a default image
		
		String defaultImageUrl;
		if (ProfileConstants.PROFILE_IMAGE_THUMBNAIL == size) {
			defaultImageUrl = getUnavailableImageThumbnailURL();
		} else {
			defaultImageUrl = getUnavailableImageURL();
		}
		
		//get current user
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if(StringUtils.equals(userUuid, currentUserUuid)){
			isSameUser = true;
		}
		
		//if no current user we are not logged in (could be from entity provider)
		if(StringUtils.isBlank(currentUserUuid)){
			// this is where the logic for handling public profile images will go
			//right now we throw a security exception.
			throw new SecurityException("Must be logged in to request a profile image.");
		}
		
		//check prefs supplied was valid, if given
		if(prefs != null && !StringUtils.equals(userUuid, prefs.getUserUuid())) {
			log.error("ProfilePreferences data supplied was not for user: " + userUuid);
			image.setExternalImageUrl(defaultImageUrl);
			image.setAltText(getAltText(userUuid, isSameUser, false));
			image.setDefault(true);
			return image;
		}
		
		//check privacy supplied was valid, if given
		if(privacy != null && !StringUtils.equals(userUuid, privacy.getUserUuid())) {
			log.error("ProfilePrivacy data supplied was not for user: " + userUuid);
			image.setExternalImageUrl(defaultImageUrl);
			image.setAltText(getAltText(userUuid, isSameUser, false));
			image.setDefault(true);
			return image;
		}
		
		//check if same user
		if(isSameUser){
			allowed = true;
		}
		
		//if we have a siteId and it's not a my workspace site, check if the current user has permissions to view the image
		if(StringUtils.isNotBlank(siteId)){
			if(!sakaiProxy.isUserMyWorkspace(siteId)) {
				log.debug("checking if user: " + currentUserUuid + " has permissions in site: " + siteId);
				allowed = sakaiProxy.isUserAllowedInSite(currentUserUuid, ProfileConstants.ROSTER_VIEW_PHOTO, siteId);
			}
		}
		
		//if not allowed yet, check we have a privacy record, if not, get one
		if(!allowed && privacy == null) {
			privacy = privacyLogic.getPrivacyRecordForUser(userUuid);
			//if still null, default image
			if(privacy == null) {
				log.error("Couldn't retrieve ProfilePrivacy data for user: " + userUuid + ". Using default image.");
				image.setExternalImageUrl(defaultImageUrl);
				image.setAltText(getAltText(userUuid, isSameUser, false));
				image.setDefault(true);
				return image;
			} 
		}
		
		//if not allowed, check privacy record
		if(!allowed) {
			allowed = privacyLogic.isActionAllowed(userUuid, currentUserUuid, PrivacyType.PRIVACY_OPTION_PROFILEIMAGE);
		}
		
		//default if still not allowed
		if(!allowed){
			image.setExternalImageUrl(defaultImageUrl);
			image.setAltText(getAltText(userUuid, isSameUser, false));
			image.setDefault(true);
			return image;
		}
		
		//check if we have an image for this user type (if enabled)
		//if we have one, return it. otherwise continue to the rest of the checks.
		String userTypeImageUrl = getUserTypeImageUrl(userUuid);
		if(StringUtils.isNotBlank(userTypeImageUrl)){
			image.setExternalImageUrl(userTypeImageUrl);
			image.setAltText(getAltText(userUuid, isSameUser, true));
			return image;
		}
		
		//lookup global image setting, this will be used if no preferences were supplied.
		int imageType = sakaiProxy.getProfilePictureType();
		
		//if we have no prefs, try to get one, it won't be considered if it is still null.
		if(prefs == null){
			prefs = preferencesLogic.getPreferencesRecordForUser(userUuid);
		}
		
		//if we have prefs and the conditions are set for a user to be able to make a choice, get the pref.
		if(prefs != null && sakaiProxy.isUsingOfficialImageButAlternateSelectionEnabled()) {
			if(prefs.isUseOfficialImage()){
				imageType = ProfileConstants.PICTURE_SETTING_OFFICIAL;
			}
		}
		
		//prefs for gravatar, only if enabled
		if(prefs != null && sakaiProxy.isGravatarImageEnabledGlobally()) {
			if(prefs.isUseGravatar()){
				imageType = ProfileConstants.PICTURE_SETTING_GRAVATAR;
			}
		}
		
		if(log.isDebugEnabled()){
			log.debug("image type: " + imageType);
			log.debug("size requested: " + size);
		}
		
		//get the image based on the global type/preference
		switch (imageType) {
			case ProfileConstants.PICTURE_SETTING_UPLOAD:
				MimeTypeByteArray mtba = getUploadedProfileImage(userUuid, size);
				
				//if no uploaded image, use the default image url
				if(mtba == null || mtba.getBytes() == null) {
					image.setExternalImageUrl(defaultImageUrl);
					image.setDefault(true);
				} else {
					image.setUploadedImage(mtba.getBytes());
					image.setMimeType(mtba.getMimeType());
				}
				image.setAltText(getAltText(userUuid, isSameUser, true));
			break;
			
			case ProfileConstants.PICTURE_SETTING_URL: 
				image.setExternalImageUrl(getExternalProfileImageUrl(userUuid, size));
				if(StringUtils.equals(image.getExternalImageUrl(), defaultImageUrl)){
					image.setDefault(true);
				}
				image.setAltText(getAltText(userUuid, isSameUser, true));
			break;
			
			case ProfileConstants.PICTURE_SETTING_OFFICIAL: 
				image = getOfficialImage(userUuid,image,defaultImageUrl,isSameUser);
			break;
			
			case ProfileConstants.PICTURE_SETTING_GRAVATAR:
				String gravatarUrl = getGravatarUrl(userUuid);
				if(StringUtils.isBlank(gravatarUrl)) {
					image.setExternalImageUrl(defaultImageUrl);
					image.setAltText(getAltText(userUuid, isSameUser, false));
					image.setDefault(true);
				} else {
					image.setExternalImageUrl(gravatarUrl);
					image.setAltText(getAltText(userUuid, isSameUser, true));
				}
			break;
			
			default:
				image.setExternalImageUrl(defaultImageUrl);
				image.setAltText(getAltText(userUuid, isSameUser, false));
				image.setDefault(true);
			break;
		}
		
		return image;
	}
	
	/**
	 * Gets the official image from url, ldap or filesystem, depending on what is specified in props. Filesystem photos
	 * are looked up by appending the first letter of a user's eid, then a slash, then the second letter of the eid
	 * followed by a slash and finally the eid suffixed by '.jpg'.
	 * 
	 * Like this:
	 * /official-photos/a/d/adrian.jpg
	 */
	private ProfileImage getOfficialImage(String userUuid, ProfileImage image,String defaultImageUrl, boolean isSameUser) {
		
		String officialImageSource = sakaiProxy.getOfficialImageSource();
		
		log.debug("Fetching official image. userUuid: " + userUuid + ", officialImageSource: " + officialImageSource);
				
		//check source and get appropriate value
		if(StringUtils.equals(officialImageSource, ProfileConstants.OFFICIAL_IMAGE_SETTING_URL)){
			image.setOfficialImageUrl(getOfficialImageUrl(userUuid));
			
			//PRFL-790 if URL security is required, get and set bytes and remove url
			boolean urlSecurityEnabled = Boolean.valueOf(sakaiProxy.getServerConfigurationParameter("profile2.official.image.url.secure", "false"));
			if(urlSecurityEnabled && StringUtils.isNotBlank(image.getOfficialImageUrl())) {
				
				log.debug("URL Security is active");
				byte[] imageUrlBytes = this.getUrlAsBytes(image.getOfficialImageUrl());
				image.setUploadedImage(imageUrlBytes);
				image.setOfficialImageUrl(null);
			}
						
		} else if(StringUtils.equals(officialImageSource, ProfileConstants.OFFICIAL_IMAGE_SETTING_PROVIDER)){
			String data = getOfficialImageEncoded(userUuid);
			if(StringUtils.isBlank(data)) {
				image.setExternalImageUrl(defaultImageUrl);
				image.setDefault(true);
			} else {
				image.setOfficialImageEncoded(data);
			}
		} else if(StringUtils.equals(officialImageSource, ProfileConstants.OFFICIAL_IMAGE_SETTING_FILESYSTEM)){
			
			//get the path based on the config from sakai.properties, basedir, pattern etc
			String filename = getOfficialImageFileSystemPath(userUuid);

			File file = new File(filename);

			try {
				byte[] data = getBytesFromFile(file);
				if(data != null) {
					image.setUploadedImage(data);
				} else {
					image.setExternalImageUrl(defaultImageUrl);
					image.setDefault(true);
				}
			}
			catch (IOException e) {
				log.error("Could not find/read official profile image file: " + filename + ". The default profile image will be used instead.");
				image.setExternalImageUrl(defaultImageUrl);
				image.setDefault(true);
			}
		}
		image.setAltText(getAltText(userUuid, isSameUser, true));
				
		return image;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public ProfileImage getProfileImage(Person person, int size) {
		return getProfileImage(person.getUuid(), person.getPreferences(), person.getPrivacy(), size, null);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public ProfileImage getProfileImage(Person person, int size, String siteId) {
		return getProfileImage(person.getUuid(), person.getPreferences(), person.getPrivacy(), size, siteId);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean setUploadedProfileImage(String userUuid, byte[] imageBytes, String mimeType, String fileName) {
		
		//check auth and get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if(currentUserUuid == null) {
			throw new SecurityException("You must be logged in to update a user's profile image.");
		}
		
		//check admin, or the currentUser and given uuid match
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
		byte[] mainImageBytes = ProfileUtils.scaleImage(imageBytes, ProfileConstants.MAX_IMAGE_XY, mimeType);
		 
		//create resource ID
		String mainResourceId = sakaiProxy.getProfileImageResourcePath(userUuid, ProfileConstants.PROFILE_IMAGE_MAIN);
		
		//save, if error, log and return.
		if(!sakaiProxy.saveFile(mainResourceId, userUuid, fileName, mimeType, mainImageBytes)) {
			log.error("Couldn't add main image to CHS. Aborting.");
			return false;
		}

		/*
		 * THUMBNAIL PROFILE IMAGE
		 */
		//scale image
		byte[] thumbnailImageBytes = ProfileUtils.scaleImage(imageBytes, ProfileConstants.MAX_THUMBNAIL_IMAGE_XY, mimeType);
		 
		//create resource ID
		String thumbnailResourceId = sakaiProxy.getProfileImageResourcePath(userUuid, ProfileConstants.PROFILE_IMAGE_THUMBNAIL);
		log.debug("Profile.ChangeProfilePicture.onSubmit thumbnailResourceId: " + thumbnailResourceId);
		
		//save, if error, warn, erase thumbnail reference, and continue (we really only need the main image)
		if(!sakaiProxy.saveFile(thumbnailResourceId, userUuid, fileName, mimeType, thumbnailImageBytes)) {
			log.warn("Couldn't add thumbnail image to CHS. Main image will be used instead.");
			thumbnailResourceId = null;
		}
		
		/*
		 * AVATAR PROFILE IMAGE
		 */
		//scale image
		byte[] avatarImageBytes = ProfileUtils.createAvatar(imageBytes, mimeType);
		 
		//create resource ID
		String avatarResourceId = sakaiProxy.getProfileImageResourcePath(userUuid, ProfileConstants.PROFILE_IMAGE_AVATAR);
		log.debug("Profile.ChangeProfilePicture.onSubmit avatarResourceId: " + avatarResourceId);
		
		//save, if error, warn, erase avatar reference, and continue (we really only need the main image)
		if(!sakaiProxy.saveFile(avatarResourceId, userUuid, fileName, mimeType, avatarImageBytes)) {
			log.warn("Couldn't add avatar image to CHS. Main image will be used instead.");
			avatarResourceId = null;
		}
		
		/*
		 * SAVE IMAGE RESOURCE IDS
		 */
		//save
		ProfileImageUploaded profileImage = new ProfileImageUploaded(userUuid, mainResourceId, thumbnailResourceId, avatarResourceId, true);
		if(dao.addNewProfileImage(profileImage)){
			log.info("Added a new profile image for user: " + userUuid);
			return true;
		}
		
		return false;
				
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean setExternalProfileImage(String userUuid, String fullSizeUrl, String thumbnailUrl, String avatar) {
		
		//check auth and get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if(currentUserUuid == null) {
			throw new SecurityException("You must be logged in to update a user's profile image.");
		}
		
		//check admin, or the currentUser and given uuid match
		if(!sakaiProxy.isSuperUser() && !StringUtils.equals(currentUserUuid, userUuid)) {
			throw new SecurityException("Not allowed to save.");
		}
		
		//check image is actually allowed to be changed
		if(!sakaiProxy.isProfilePictureChangeEnabled()) {
			log.warn("Profile image changes are not permitted as per sakai.properties setting 'profile2.picture.change.enabled'.");
			return false;
		}
		
		//save
		ProfileImageExternal externalImage = new ProfileImageExternal(userUuid, fullSizeUrl, thumbnailUrl, avatar);
		if(dao.saveExternalImage(externalImage)) {
			log.info("Updated external image record for user: " + userUuid); 
			return true;
		} 
		
		return false;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean saveOfficialImageUrl(final String userUuid, final String url) {
		
		ProfileImageOfficial officialImage = new ProfileImageOfficial(userUuid, url);
		
		if(dao.saveOfficialImageUrl(officialImage)) {
			log.info("Updated official image record for user: " + userUuid); 
			return true;
		} 
		
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean addGalleryImage(String userUuid, byte[] imageBytes, String mimeType, String fileName) {

		// check auth and get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if (currentUserUuid == null) {
			throw new SecurityException("You must be logged in to add a gallery image.");
		}

		// check admin, or the currentUser and given uuid match
		if (!sakaiProxy.isSuperUser() && !StringUtils.equals(currentUserUuid, userUuid)) {
			throw new SecurityException("You are not allowed to add a gallery image.");
		}

		String imageId = sakaiProxy.createUuid();

		// create resource ID
		String mainResourcePath = sakaiProxy.getProfileGalleryImagePath(userUuid, imageId);

		byte[] scaledImageBytes = ProfileUtils.scaleImage(imageBytes, ProfileConstants.MAX_GALLERY_IMAGE_XY, mimeType);
		
		// save image
		if (!sakaiProxy.saveFile(mainResourcePath, userUuid, fileName, mimeType,scaledImageBytes)) {
			log.error("Couldn't add gallery image to CHS. Aborting.");
			return false;
		}

		// create thumbnail
		byte[] thumbnailBytes = ProfileUtils.scaleImage(imageBytes, ProfileConstants.MAX_GALLERY_THUMBNAIL_IMAGE_XY, mimeType);
		String thumbnailResourcePath = sakaiProxy.getProfileGalleryThumbnailPath(userUuid, imageId);
		sakaiProxy.saveFile(thumbnailResourcePath, userUuid, fileName, mimeType,thumbnailBytes);
		
		//save
		GalleryImage galleryImage = new GalleryImage(userUuid,mainResourcePath, thumbnailResourcePath, fileName);
		if(dao.addNewGalleryImage(galleryImage)){
			log.info("Added new gallery image for user: " + galleryImage.getUserUuid()); 
			return true;
		} 
			
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<GalleryImage> getGalleryImages(String userUuid) {

		// check auth and get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if (currentUserUuid == null) {
			throw new SecurityException("You must be logged in to make a request for a user's gallery images.");
		}

		return dao.getGalleryImages(userUuid);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<GalleryImage> getGalleryImagesRandomized(String userUuid) {
		
		List<GalleryImage> images = getGalleryImages(userUuid);
		Collections.shuffle(images);
		return images;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeGalleryImage(String userId, long imageId) {
		if(userId == null || Long.valueOf(imageId) == null){
	  		throw new IllegalArgumentException("Null argument in ProfileLogicImpl.removeGalleryImage()"); 
	  	}
		
		// check auth and get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if (currentUserUuid == null) {
			throw new SecurityException("You must be logged in to remove a gallery image.");
		}
		
		// check admin, or the currentUser and given uuid match
		if (!sakaiProxy.isSuperUser() && !StringUtils.equals(currentUserUuid, userId)) {
			throw new SecurityException("You are not allowed to remove this gallery image.");
		}
		
		GalleryImage galleryImage = dao.getGalleryImageRecord(userId, imageId);
		
		if(galleryImage == null){
			log.error("GalleryImage record does not exist for userId: " + userId + ", imageId: " + imageId);
			return false;
		}
		
		//delete main image
		if (!sakaiProxy.removeResource(galleryImage.getMainResource())) {
			log.error("Gallery image not removed: " + galleryImage.getMainResource());
		}
		
		//delete thumbnail
		if (!sakaiProxy.removeResource(galleryImage.getThumbnailResource())) {
			log.error("Gallery thumbnail not removed: " + galleryImage.getThumbnailResource());
		}
		
		if(dao.removeGalleryImage(galleryImage)){
			log.info("User: " + userId + " removed gallery image: " + imageId);
			return true;
		} 
		
		return false;
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public String getGravatarUrl(final String userUuid) {
		
		String email = sakaiProxy.getUserEmail(userUuid);
		if(StringUtils.isBlank(email)){
			return null;
		}
				
		return ProfileConstants.GRAVATAR_BASE_URL + ProfileUtils.calculateMD5(email) + "?s=200";
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean resetProfileImage(final String userUuid) {
		if(dao.invalidateCurrentProfileImage(userUuid)) {
			log.info("Invalidated profile image for user: " + userUuid);
			return true;
		}
		return false;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean profileImageIsDefault(final String userUuid) {
		ProfileImage image = getProfileImage(userUuid, null, null, ProfileConstants.PROFILE_IMAGE_MAIN);
		return image.isDefault();
	}


	/**
	 * Generate the full URL to the default image (either full or thumbnail)
	 * @param imagePath
	 * @return
	 */
	private String getUnavailableImageURL(String imagePath) {
		StringBuilder path = new StringBuilder();
		path.append(sakaiProxy.getServerUrl());
		path.append(imagePath);
		return path.toString();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUnavailableImageURL() {
		return getUnavailableImageURL(ProfileConstants.UNAVAILABLE_IMAGE_FULL);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUnavailableImageThumbnailURL() {
		return getUnavailableImageURL(ProfileConstants.UNAVAILABLE_IMAGE_THUMBNAIL);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getProfileImageEntityUrl(String userUuid, int size) {
	
		StringBuilder sb = new StringBuilder();
		sb.append(sakaiProxy.getServerUrl());
		sb.append("/direct/profile/");
		sb.append(userUuid);
		sb.append("/image/");
		if(size == ProfileConstants.PROFILE_IMAGE_THUMBNAIL){
			sb.append("thumb/");
		}
		return sb.toString();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getGalleryImagesCount(final String userUuid) {
		return dao.getGalleryImagesCount(userUuid);
	}
	
	
	/**
	 * Get the profile image for the given user, allowing fallback if no thumbnail exists.
	 * 
	 * @param userUuid 		the uuid of the user we are querying
	 * @param size			comes from ProfileConstants, main or thumbnail, also maps to a directory in ContentHosting
	 * @return MimeTypeByteArray or null
	 * 
	 * <p>Note: if thumbnail is requested and none exists, the main image will be returned instead. It can be scaled in the markup.</p>
	 *
	 */
	private MimeTypeByteArray getUploadedProfileImage(String userUuid, int size) {
		
		MimeTypeByteArray mtba = new MimeTypeByteArray();
		
		//get record from db
		ProfileImageUploaded profileImage = dao.getCurrentProfileImageRecord(userUuid);
		
		if(profileImage == null) {
			log.debug("ProfileLogic.getUploadedProfileImage() null for userUuid: " + userUuid);
			return null;
		}
		
		//get main image
		if(size == ProfileConstants.PROFILE_IMAGE_MAIN) {
			mtba = sakaiProxy.getResource(profileImage.getMainResource());
		}
		
		//or get thumbnail
		if(size == ProfileConstants.PROFILE_IMAGE_THUMBNAIL) {
			mtba = sakaiProxy.getResource(profileImage.getThumbnailResource());
			//PRFL-706, if the file is deleted, catch any possible NPE
			if(mtba == null || mtba.getBytes() == null) {
				mtba = sakaiProxy.getResource(profileImage.getMainResource());
			}
		}
		
		if(size == ProfileConstants.PROFILE_IMAGE_AVATAR) {
			mtba = sakaiProxy.getResource(profileImage.getAvatarResource());
			//PRFL-706, if the file is deleted, catch any possible NPE
			if(mtba == null || mtba.getBytes() == null) {
				mtba = sakaiProxy.getResource(profileImage.getMainResource());
			}
		}
		
		return mtba;
	}
	
	/**
	 * Get the URL to an image that a user has specified as their profile image
	 * @param userId		uuid of user
	 * @param size			comes from ProfileConstants. main or thumbnail.
	 *
	 * <p>Note: if thumbnail is requested and none exists, the main image will be returned instead. It can be scaled in the markup.</p>
	 * 
	 * @return url to the image, or a default image if none.
	 */
	private String getExternalProfileImageUrl(final String userUuid, final int size) {
	
		//get external image record for this user
		ProfileImageExternal externalImage = dao.getExternalImageRecordForUser(userUuid);
		
		//setup default
		String defaultImageUrl = getUnavailableImageURL();
		
		//if none, return null
    	if(externalImage == null) {
    		return defaultImageUrl;
    	}
    	
    	//otherwise get the url for the type they requested
    	String url = null;
    	if(size == ProfileConstants.PROFILE_IMAGE_MAIN) {
    		if(log.isDebugEnabled()) {
    			log.debug("Returning main image url for userId: " + userUuid);
    		}
    		url = externalImage.getMainUrl();
    	}
    	
    	if(size == ProfileConstants.PROFILE_IMAGE_THUMBNAIL) {
    		if(log.isDebugEnabled()) {
    			log.debug("Returning thumb image url for userId: " + userUuid);
    		}
    		url = externalImage.getThumbnailUrl();
    	}
    	
    	if(size == ProfileConstants.PROFILE_IMAGE_AVATAR) {
    		if(log.isDebugEnabled()) {
    			log.debug("Returning avatar image url for userId: " + userUuid);
    		}
    		url = externalImage.getAvatarUrl();
    	}
    	
    	//if we have a url, return it,
    	if(StringUtils.isNotBlank(url)){
    		return url;
    	} else {
    		//otherwise fallback
    		url = externalImage.getMainUrl();
			if(StringUtils.isBlank(url)) {
				url = defaultImageUrl;
			}
    	}
    	    	
    	//no url	
    	log.info("ProfileLogic.getExternalProfileImageUrl. No URL for userId: " + userUuid + ", imageType: " + size + ". Returning default.");  
    	return defaultImageUrl;
	}

	
	
	/**
	 * Get the URL to a user's official profile image
	 * @param userUuid		uuid of user
	 * 
	 * @return url or a default image if none
	 */
	private String getOfficialImageUrl(final String userUuid) {
		
		//get external image record for this user
		ProfileImageOfficial official = dao.getOfficialImageRecordForUser(userUuid);
		
		//setup default
		String defaultImageUrl = getUnavailableImageURL();
		
		//if none, return null
    	if(official == null) {
    		return defaultImageUrl;
    	}
    	
    	if(StringUtils.isBlank(official.getUrl())) {
        	log.info("ProfileLogic.getOfficialImageUrl. No URL for userUuid: " + userUuid + ". Returning default.");  
			return defaultImageUrl;
		}
    	
    	return official.getUrl();
	}
	
	/**
	 * Get the official image data from the user properties, encoded in BASE64
	 * @param userUuid	uuid of user
	 * @return base64 encoded data, or null.
	 */
	private String getOfficialImageEncoded(final String userUuid) {
		User u = sakaiProxy.getUserById(userUuid);
		return u.getProperties().getProperty(sakaiProxy.getOfficialImageAttribute());
	}
	
	/**
	 * Helper to get the altText to be used for the image
	 * @param userUuid
	 * @param isOwner
	 * @param hasImage
	 * @return
	 */
	private String getAltText(String userUuid, boolean isOwner, boolean hasImage) {
		
		//owner and has an image
		if(isOwner && hasImage){
			return Messages.getString("profile.image.my.alt");
		}
		
		//owner and doesnt have an image
		if(isOwner && !hasImage){
			return Messages.getString("profile.image.my.none.alt");
		}
		
		//not owner so get name
		if(!isOwner) {
			String displayName = sakaiProxy.getUserDisplayName(userUuid);
			return Messages.getString("profile.image.other.alt", new Object[] { displayName });
		}
		
		return null;
	}
	
	/**
	 * Gets the url to the user type image (PRFL-691)
	 * 
	 *<p>This first checks if the option is enabled (profile2.user.type.image.enabled=true). 
	 *<p>If so, it attempts to get the value for profile.user.type.image.&lt;usertype&gt; which should be an absolute URL.
	 *<p>If there is one, it is returned, if not, return null. Also returns null if userType is undefined.
	 * @param userUuid	uuid of the user to get the image for
	 * @return url or null.
	 */
	private String getUserTypeImageUrl(String userUuid) {
		
		boolean enabled = Boolean.valueOf(sakaiProxy.getServerConfigurationParameter("profile2.user.type.image.enabled", "false"));
		
		String imageUrl = null;
		
		if(enabled) {
			String userType = sakaiProxy.getUserType(userUuid);
			if(StringUtils.isNotBlank(userType)) {
				imageUrl = sakaiProxy.getServerConfigurationParameter("profile.user.type.image."+userType, null);
			}
		}
		return imageUrl;
	}
	
	/**
	 * Read a file to a byte array.
	 * 
	 * @param file
	 * @return byte[] if file ok, or null if its too big
	 * @throws IOException
	 */
	private byte[] getBytesFromFile(File file) throws IOException {
    
		// Get the size of the file
		long length = file.length();
		
		if (length > (ProfileConstants.MAX_IMAGE_UPLOAD_SIZE * FileUtils.ONE_MB)) {
			log.error("File too large: " + file.getCanonicalPath());  
			return null;
		}
		
		// return file contents
		return FileUtils.readFileToByteArray(file);
   }
	
	/**
	 * Helper to get the path to the official image on the filesystem. This could be in one of several patterns.
	 * @param userUuid
	 * @return
	 */
	private String getOfficialImageFileSystemPath(String userUuid) {
		
		//get basepath, common to all
		String basepath = sakaiProxy.getOfficialImagesDirectory();
		
		//get the pattern
		String pattern = sakaiProxy.getOfficialImagesFileSystemPattern();
		
		//get user, common for all
		User user = sakaiProxy.getUserById(userUuid);
		String userEid = user.getEid();
		
		String filename = null;
		
		//create the path based on the basedir and pattern
		if(StringUtils.equals(pattern, "ALL_IN_ONE")) {
			filename = 	basepath + File.separator + userEid + ".jpg";
		}
		else if(StringUtils.equals(pattern, "ONE_DEEP")) {
			String firstLetter = userEid.substring(0,1);
			filename = basepath + File.separator + firstLetter + File.separator + userEid + ".jpg";
		}
		//insert more patterns here as required. Dont forget to update SakaiProxy and confluence with details.
		else {
			//TWO_DEEP is default
			String firstLetter = userEid.substring(0,1);
			String secondLetter = userEid.substring(1,2);
			filename = basepath + File.separator + firstLetter + File.separator + secondLetter + File.separator + userEid + ".jpg";
		}
		
		if(log.isDebugEnabled()){
			log.debug("Path to official image on filesystem is: " + filename);
		}
		
		return filename;
	}
	
	/**
	 * Get a URL resource as a byte[]
	 * @param url URL to fetch
	 * @return
	 */
	private byte[] getUrlAsBytes(String url) {
		byte[] data = null;
		try {
			URL u = new URL(url);
			URLConnection uc = u.openConnection();
			uc.setReadTimeout(5000); //5 sec timeout
			InputStream inputStream = uc.getInputStream();
			
			data = IOUtils.toByteArray(inputStream);

		} catch (Exception e) {
			log.error("Failed to retrieve url bytes: " + e.getClass() + ": " + e.getMessage());
		} 
		return data;
	}
	
	
	@Setter
	private SakaiProxy sakaiProxy;
	
	@Setter
	private ProfilePrivacyLogic privacyLogic;
	
	@Setter
	private ProfileConnectionsLogic connectionsLogic;
	
	@Setter
	private ProfilePreferencesLogic preferencesLogic;
	
	@Setter
	private ProfileDao dao;
	
}
