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
package org.sakaiproject.profile2.conversion;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.bean.CsvToBean;
import au.com.bytecode.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.profile2.dao.ProfileDao;
import org.sakaiproject.profile2.exception.ProfileNotDefinedException;
import org.sakaiproject.profile2.hbm.model.ProfileImageExternal;
import org.sakaiproject.profile2.hbm.model.ProfileImageUploaded;
import org.sakaiproject.profile2.logic.ProfileImageLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.ImportableUserProfile;
import org.sakaiproject.profile2.model.MimeTypeByteArray;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;

/**
 * Handles the conversion and import of profiles and images. This is not part of the public API.
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Slf4j
public class ProfileConverter {

	@Setter
	private SakaiProxy sakaiProxy;
	
	@Setter
	private ProfileDao dao;
	
	@Setter
	private SecurityService securityService;
	
	@Setter
	private ProfileImageLogic imageLogic;
	
	ConvertedImage ci = null;
	
	private final static String DEFAULT_FILE_NAME = "Profile Image";
	private final static String DEFAULT_MIME_TYPE = "image/jpeg";
	
	public void init() {
		log.info("Profile2: ==============================="); 
		log.info("Profile2: Conversion utility starting up."); 
		log.info("Profile2: ==============================="); 
	}
	
	/**
	 * Convert profile images
	 */
	public void convertProfileImages() {
		
		//get list of users
		List<String> allUsers = new ArrayList<String>(dao.getAllSakaiPersonIds());
		
		if(allUsers.isEmpty()){
			log.warn("Profile2 image converter: No SakaiPersons to process. Nothing to do!");
			return;
		}
		//for each, do they have a profile image record. if so, skip (perhaps null the SakaiPerson JPEG_PHOTO bytes?)
		for(Iterator<String> i = allUsers.iterator(); i.hasNext();) {
			String userUuid = (String)i.next();
			
			//get image record from dao directly, we don't need privacy/prefs here
			ProfileImageUploaded uploadedProfileImage = dao.getCurrentProfileImageRecord(userUuid);
			
			ci = new ConvertedImage();
			ci.setUserUuid(userUuid);
			
			//if no record, we need to run all conversions
			if(uploadedProfileImage == null) {
				//main
				convertSakaiPersonImage();
				
				if(StringUtils.isNotBlank(ci.getMainResourceId())) {
					//thumbnail
					generateAndPersistThumbnail();
					//avatar
					generateAndPersistAvatar();
				}
			} else {
				
				//get any existing values and set into object so we know if we need to generate or save anything
				ci.setMainResourceId(uploadedProfileImage.getMainResource());
				ci.setThumbnailResourceId(uploadedProfileImage.getThumbnailResource());
				ci.setAvatarResourceId(uploadedProfileImage.getAvatarResource());
				
				//get the existing profile image
				MimeTypeByteArray mtba = sakaiProxy.getResource(ci.getMainResourceId());
				
				ci.setImage(mtba.getBytes());
				ci.setMimeType(mtba.getMimeType());

				//if we need thumb or avatar, create as necessary
				if(ci.needsThumb()){
					generateAndPersistThumbnail();
				}
				if(ci.needsAvatar()){
					generateAndPersistAvatar();
				}
			}
		
			//save image resource IDs
			if(ci.isNeedsSaving()){
				ProfileImageUploaded convertedProfileImage = new ProfileImageUploaded(userUuid, ci.getMainResourceId(), ci.getThumbnailResourceId(), ci.getAvatarResourceId(), true);
			
				if(dao.addNewProfileImage(convertedProfileImage)){
					log.info("Profile2 image converter: Binary image converted and saved for " + userUuid);
				} else {
					log.warn("Profile2 image converter: Binary image conversion failed for " + userUuid);
				}	
			}
			
			//
			// Process external image urls
			//
			
			//process any image URLs, if they don't already have a valid record.
			ProfileImageExternal externalProfileImage = dao.getExternalImageRecordForUser(userUuid);
			if(externalProfileImage != null) {
				log.info("Profile2 image converter: ProfileImageExternal record exists for " + userUuid + ". Nothing to do here, skipping...");
			} else {
				log.info("Profile2 image converter: No existing ProfileImageExternal record for " + userUuid + ". Processing...");
				
				String url = sakaiProxy.getSakaiPersonImageUrl(userUuid);
				
				//if none, nothing to do
				if(StringUtils.isBlank(url)) {
					log.info("Profile2 image converter: No url image to convert for " + userUuid + ". Skipping...");
				} else {
					externalProfileImage = new ProfileImageExternal(userUuid, url, null, null);
					if(dao.saveExternalImage(externalProfileImage)) {
						log.info("Profile2 image converter: Url image converted and saved for " + userUuid);
					} else {
						log.warn("Profile2 image converter: Url image conversion failed for " + userUuid);
					}
				}
				
			}
			
			log.info("Profile2 image converter: Finished converting user profile for: " + userUuid);
			//go to next user
		}
		
		return;
	}
	
	/**
	 * This imports URL profile images into upload profile images.
	 */
	public void importProfileImages() {
		
		//get list of users
		List<String> allUsers = new ArrayList<String>(dao.getAllSakaiPersonIds());
		
		if(allUsers.isEmpty()){
			log.warn("Profile2 image converter: No SakaiPersons to process. Nothing to do!");
			return;
		}
		
		//for each, do they have a profile image record. if so, skip (perhaps null the SakaiPerson JPEG_PHOTO bytes?)
		for(Iterator<String> i = allUsers.iterator(); i.hasNext();) {
			String userUuid = i.next();
			
			//get image record from dao directly, we don't need privacy/prefs here
			ProfileImageUploaded uploadedProfileImage = dao.getCurrentProfileImageRecord(userUuid);
			
			ci = new ConvertedImage();
			ci.setUserUuid(userUuid);
			
			//if no record, we need to run all conversions
			if(uploadedProfileImage == null) {
				
				//main
				ProfileImageExternal externalProfileImage = dao.getExternalImageRecordForUser(userUuid);
				if (externalProfileImage == null) {
					log.info("No existing external profile images for "+ userUuid);
				} else {
					String mainUrl = externalProfileImage.getMainUrl();
					if (StringUtils.isNotBlank(mainUrl)) {
						retrieveMainImage(userUuid, mainUrl);
					} else {
						log.info("No URL set for "+ userUuid);
					}
				}
	
				if(StringUtils.isNotBlank(ci.getMainResourceId())) {
					//thumbnail
					generateAndPersistThumbnail();
					//avatar
					generateAndPersistAvatar();
				}
			}
		
			//save image resource IDs
			if(ci.isNeedsSaving()){
				ProfileImageUploaded convertedProfileImage = new ProfileImageUploaded(userUuid, ci.getMainResourceId(), ci.getThumbnailResourceId(), ci.getAvatarResourceId(), true);
			
				if(dao.addNewProfileImage(convertedProfileImage)){
					log.info("Profile2 image converter: Binary image converted and saved for " + userUuid);
				} else {
					log.warn("Profile2 image converter: Binary image conversion failed for " + userUuid);
				}	
			}
		}
	}

	private void retrieveMainImage(String userUuid, String mainUrl) {
		InputStream inputStream = null;
		try {
			URL url = new URL(mainUrl);
			HttpURLConnection openConnection = (HttpURLConnection) url.openConnection();
			openConnection.setReadTimeout(5000);
			openConnection.setConnectTimeout(5000);
			openConnection.setInstanceFollowRedirects(true);
			openConnection.connect();
			int responseCode = openConnection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {

				String mimeType = openConnection.getContentType();
				inputStream = openConnection.getInputStream();
				// Convert the image.
				byte[] imageMain = ProfileUtils.scaleImage(inputStream, ProfileConstants.MAX_IMAGE_XY, mimeType);

				//create resource ID
				String mainResourceId = sakaiProxy.getProfileImageResourcePath(userUuid, ProfileConstants.PROFILE_IMAGE_MAIN);
				log.info("Profile2 image converter: mainResourceId: " + mainResourceId);

				//save, if error, log and return.
				if (!sakaiProxy.saveFile(mainResourceId, userUuid, DEFAULT_FILE_NAME, mimeType, imageMain)) {
					log.error("Profile2 image importer: Saving main profile image failed.");
				} else {
					ci.setImage(imageMain);
					ci.setMimeType(mimeType);
					ci.setFileName(DEFAULT_FILE_NAME);
					ci.setMainResourceId(mainResourceId);
					ci.setNeedsSaving(true);
				}
			} else {
				log.warn("Failed to get good response for user "+ userUuid+ " for "+ mainUrl+ " got "+ responseCode);
			}
		} catch (MalformedURLException e) {
			log.info ("Invalid URL for user: "+ userUuid+ " of: "+ mainUrl);
		} catch (IOException e) {
			log.warn("Failed to download image for: "+ userUuid+ " from: "+ mainUrl+ " error of: "+ e.getMessage());
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException ioe) {
					log.info("Failed to close input stream for request to: "+ mainUrl);
				}
			}
		}
	}
	
	/**
	 * Import profiles from the given CSV file
	 * 
	 * <p>The CSV file may contain any of the following headings, in any order:
	 *  
	 *  <ul>
	 *  <li>eid</li>
	 *  <li>nickname</li>
	 *  <li>position</li>
	 *  <li>department</li>
	 *  <li>school</li>
	 *  <li>room</li>
	 *  <li>web site</li>
	 *  <li>work phone</li>
	 *  <li>home phone</li>
	 *  <li>mobile phone</li>
	 *  <li>fax</li>
	 *  <li>books</li>
	 *  <li>tv</li>
	 *  <li>movies</li>
	 *  <li>quotes</li>
	 *  <li>summary</li>
	 *  <li>course</li>
	 *  <li>subjects</li>
	 *  <li>staff profile</li>
	 *  <li>uni profile url</li>
	 *  <li>academic profile url</li>
	 *  <li>publications</li>
	 *  <li>official image url</li>
	 *  </ul>
	 * 
	 * <p>Column headings must match EXACTLY the list above. They do not need to be in the same order, or even all present.
	 * 
	 * <p>Fields must be comma separated and each field surrounded with double quotes. There must be no spaces between fields.
	 * 
	 * <p>Only users that do not currently have a profile will be imported.
	 * 
	 * @param path	path to CSV file on the server
	 */
	public void importProfiles(String path) {
		
		if(StringUtils.isBlank(path)) {
			log.warn("Profile2 importer: invalid path to CSV file. Aborting.");
			return;
		}
		
        HeaderColumnNameTranslateMappingStrategy<ImportableUserProfile> strat = new HeaderColumnNameTranslateMappingStrategy<ImportableUserProfile>();
        strat.setType(ImportableUserProfile.class);
        
        //map the column headers to the field names in the UserProfile class
        //this mapping is not exhaustive and can be added to at any time since we are mapping
        //on column name not position
        Map<String, String> map = new HashMap<String, String>();
        map.put("eid", "eid");
        map.put("nickname", "nickname");
        map.put("position", "position");
        map.put("department", "department");
        map.put("school", "school");
        map.put("room", "room");
        map.put("web site", "homepage");
        map.put("work phone", "workphone");
        map.put("home phone", "homephone");
        map.put("mobile phone", "mobilephone");
        map.put("fax", "facsimile");
        map.put("books", "favouriteBooks");
        map.put("tv", "favouriteTvShows");
        map.put("movies", "favouriteMovies");
        map.put("quotes", "favouriteQuotes");
        map.put("summary", "personalSummary");
        map.put("course", "course");
        map.put("subjects", "subjects");
        map.put("staff profile", "staffProfile");
        map.put("uni profile url", "universityProfileUrl");
        map.put("academic profile url", "academicProfileUrl");
        map.put("publications", "publications");
        map.put("official image url", "officialImageUrl");
        
        strat.setColumnMapping(map);

        CsvToBean<ImportableUserProfile> csv = new CsvToBean<ImportableUserProfile>();
        List<ImportableUserProfile> list = new ArrayList<ImportableUserProfile>();
        try {
			list = csv.parse(strat, new CSVReader(new FileReader(path)));
		} catch (FileNotFoundException fnfe) {
			log.error("Profile2 importer: Couldn't find file: " + fnfe.getClass() + " : " + fnfe.getMessage());
		}
        
		//setup a security advisor so we can save profiles
		SecurityAdvisor securityAdvisor = new SecurityAdvisor(){
			public SecurityAdvice isAllowed(String userId, String function, String reference){
				  return SecurityAdvice.ALLOWED;
			}
		};
		enableSecurityAdvisor(securityAdvisor);
		
        //process each
        for(ImportableUserProfile profile: list) {

        	log.info("Processing user: " + profile.getEid());
        	
        	//get uuid
        	String uuid = sakaiProxy.getUserIdForEid(profile.getEid());
        	if(StringUtils.isBlank(uuid)) {
        		log.error("Invalid user: " + profile.getEid() + ". Skipping...");
        		continue;
        	}
        	
        	profile.setUserUuid(uuid);
        	        	
        	//check if user already has a profile. Skip if so.
        	if(hasPersistentProfile(uuid)) {
        		log.warn("User: " + profile.getEid() + " already has a profile. Skipping...");
        		continue;
        	}
        	
        	//persist user profile
        	try {
        		SakaiPerson sp = transformUserProfileToSakaiPerson(profile);
        		
        		if(sp == null){
        			//already logged
        			continue;
        		}
        		
        		if(sakaiProxy.updateSakaiPerson(sp)) {
        			log.info("Profile saved for user: " + profile.getEid());
        		} else {
        			log.error("Couldn't save profile for user: " + profile.getEid());
        			continue;
        		}
        	} catch (ProfileNotDefinedException pnde) {
        		//already logged
        		continue;
        	}
        	
        	//add/update official image, if supplied in the CSV
        	if(StringUtils.isNotBlank(profile.getOfficialImageUrl())) {
        		if(imageLogic.saveOfficialImageUrl(uuid, profile.getOfficialImageUrl())) {
        			log.info("Official image saved for user: " + profile.getEid());
        		} else {
        			log.error("Couldn't save official image for user: " + profile.getEid());
        		}
        	}
        }
        disableSecurityAdvisor(securityAdvisor);
		
	}
	
	/**
	 * Does the given user already have a <b>persistent</b> user profile?
	 * 
	 * @param userUuid	uuid of the user
	 * @return
	 */
	private boolean hasPersistentProfile(String userUuid) {
				
		SakaiPerson sp = sakaiProxy.getSakaiPerson(userUuid);
		if(sp != null){
			return true;
		} 
		return false;
	}
	
	
	/**
	 * Convenience method to map a UserProfile object onto a SakaiPerson object for persisting
	 * 
	 * @param up 		input UserProfile
	 * @return			returns a SakaiPerson representation of the UserProfile object which can be persisted
	 */
	private SakaiPerson transformUserProfileToSakaiPerson(UserProfile up) {
	
		log.info("Transforming: " + up.toString());
		
		String userUuid = up.getUserUuid();
		
		if(StringUtils.isBlank(userUuid)) {
			log.error("Profile was invalid (missing uuid), cannot transform.");
			return null;
		}
		
		//get SakaiPerson
		SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userUuid);
		
		//if null, create one 
		if(sakaiPerson == null) {
			sakaiPerson = sakaiProxy.createSakaiPerson(userUuid);
			//if its still null, throw exception
			if(sakaiPerson == null) {
				throw new ProfileNotDefinedException("Couldn't create a SakaiPerson for " + userUuid);
			}
		} 
		
		//map fields from UserProfile to SakaiPerson
		
		//basic info
		sakaiPerson.setNickname(up.getNickname());
		sakaiPerson.setDateOfBirth(up.getDateOfBirth());
		
		//contact info
		sakaiPerson.setLabeledURI(up.getHomepage());
		sakaiPerson.setTelephoneNumber(up.getWorkphone());
		sakaiPerson.setHomePhone(up.getHomephone());
		sakaiPerson.setMobile(up.getMobilephone());
		sakaiPerson.setFacsimileTelephoneNumber(up.getFacsimile());
		
		//staff info
		sakaiPerson.setOrganizationalUnit(up.getDepartment());
		sakaiPerson.setTitle(up.getPosition());
		sakaiPerson.setCampus(up.getSchool());
		sakaiPerson.setRoomNumber(up.getRoom());
		sakaiPerson.setStaffProfile(up.getStaffProfile());
		sakaiPerson.setUniversityProfileUrl(up.getUniversityProfileUrl());
		sakaiPerson.setAcademicProfileUrl(up.getAcademicProfileUrl());
		sakaiPerson.setPublications(up.getPublications());
		
		// student info
		sakaiPerson.setEducationCourse(up.getCourse());
		sakaiPerson.setEducationSubjects(up.getSubjects());
				
		//personal info
		sakaiPerson.setFavouriteBooks(up.getFavouriteBooks());
		sakaiPerson.setFavouriteTvShows(up.getFavouriteTvShows());
		sakaiPerson.setFavouriteMovies(up.getFavouriteMovies());
		sakaiPerson.setFavouriteQuotes(up.getFavouriteQuotes());
		sakaiPerson.setNotes(up.getPersonalSummary());
		
		return sakaiPerson;
	}
	
	/**
	 * Add the supplied security advisor to the stack for this transaction
	 */
	private void enableSecurityAdvisor(SecurityAdvisor securityAdvisor) {
		securityService.pushAdvisor(securityAdvisor);
	}

	/**
	 * Remove security advisor from the stack
	 */
	private void disableSecurityAdvisor(SecurityAdvisor advisor){
		securityService.popAdvisor(advisor);
	}
	
	/**
	 * Helper to convert an image stored in SakaiPerson into a main image
	 * @return 
	 */
	private void convertSakaiPersonImage(){
		
		String userUuid = ci.getUserUuid();
		
		//get photo from SakaiPerson
		byte[] image = sakaiProxy.getSakaiPersonJpegPhoto(userUuid);
		
		//if none, nothing to do
		if(image == null || image.length == 0) {
			log.info("Profile2 image converter: No image binary to convert for " + userUuid + ". Skipping user...");
		} else {
			
			//scale the main image
			byte[] imageMain = ProfileUtils.scaleImage(image, ProfileConstants.MAX_IMAGE_XY, DEFAULT_MIME_TYPE);
			
			//create resource ID
			String mainResourceId = sakaiProxy.getProfileImageResourcePath(userUuid, ProfileConstants.PROFILE_IMAGE_MAIN);
			log.info("Profile2 image converter: mainResourceId: " + mainResourceId);
			
			//save, if error, log and return.
			if(!sakaiProxy.saveFile(mainResourceId, userUuid, DEFAULT_FILE_NAME, DEFAULT_MIME_TYPE, imageMain)) {
				log.error("Profile2 image converter: Saving main profile image failed.");
			} else {
				ci.setImage(imageMain);
				ci.setMimeType(DEFAULT_MIME_TYPE);
				ci.setFileName(DEFAULT_FILE_NAME);
				ci.setMainResourceId(mainResourceId);
				ci.setNeedsSaving(true);
			}
		}
	}
	
	/**
	 * Helper to convert an image into a thumbnail
	 * @return 
	 */
	private void generateAndPersistThumbnail() {
		
		String userUuid = ci.getUserUuid();
		
		byte[] imageThumbnail = ProfileUtils.scaleImage(ci.getImage(), ProfileConstants.MAX_THUMBNAIL_IMAGE_XY, ci.getMimeType());
		 
		//create resource ID
		String thumbnailResourceId = sakaiProxy.getProfileImageResourcePath(userUuid, ProfileConstants.PROFILE_IMAGE_THUMBNAIL);
		log.info("Profile2 image converter: thumbnailResourceId:" + thumbnailResourceId);
		
		//save, if error, log and return.
		if(!sakaiProxy.saveFile(thumbnailResourceId, userUuid, DEFAULT_FILE_NAME, ci.getMimeType(), imageThumbnail)) {
			log.warn("Profile2 image converter: Saving thumbnail profile image failed. Main image will be used instead.");
		} else {
			ci.setThumbnailResourceId(thumbnailResourceId);
			ci.setNeedsSaving(true);
		}
	}
	
	/**
	 * Helper to convert an image into a thumbnail
	 * @return 
	 */
	private void generateAndPersistAvatar() {

		String userUuid = ci.getUserUuid();
		
		byte[] imageAvatar = ProfileUtils.createAvatar(ci.getImage(), ci.getMimeType());
		 
		//create resource ID
		String avatarResourceId = sakaiProxy.getProfileImageResourcePath(userUuid, ProfileConstants.PROFILE_IMAGE_THUMBNAIL);
		log.info("Profile2 image converter: avatarResourceId:" + avatarResourceId);
		
		//save, if error, log and return.
		if(!sakaiProxy.saveFile(avatarResourceId, userUuid, DEFAULT_FILE_NAME, ci.getMimeType(), imageAvatar)) {
			log.warn("Profile2 image converter: Saving avatar profile image failed. Main image will be used instead.");
		} else {
			ci.setAvatarResourceId(avatarResourceId);
			ci.setNeedsSaving(true);
		}
	}
	
	
	/**
	 * Private class to store some info while we perform the conversions
	 * 
	 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
	 *
	 */
	
	class ConvertedImage {
		@Getter @Setter private String mainResourceId;
		@Getter @Setter private String thumbnailResourceId;
		@Getter @Setter private String avatarResourceId;
		@Getter @Setter private byte[] image;
		@Getter @Setter private String mimeType;
		@Getter @Setter private String userUuid;
		@Getter @Setter private String fileName;
		//only want to save if we have to, otherwise we will be duplicating records
		@Getter @Setter private boolean needsSaving = false;
		
		public boolean needsThumb() {
			return (validBytes() && StringUtils.isBlank(thumbnailResourceId));
		}
		
		public boolean needsAvatar() {
			return (validBytes() && StringUtils.isBlank(avatarResourceId));
		}
		
		public boolean validBytes() {
			return (image != null && image.length > 0);
		}
		
	}
	
}
