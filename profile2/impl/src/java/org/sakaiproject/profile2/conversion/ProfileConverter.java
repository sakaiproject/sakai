package org.sakaiproject.profile2.conversion;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lombok.Setter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.sakaiproject.profile2.dao.ProfileDao;
import org.sakaiproject.profile2.hbm.model.ProfileImageExternal;
import org.sakaiproject.profile2.hbm.model.ProfileImageUploaded;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;

import au.com.bytecode.opencsv.CSVReader;


public class ProfileConverter {

	private static final Logger log = Logger.getLogger(ProfileConverter.class);
	
	@Setter
	private SakaiProxy sakaiProxy;
	
	@Setter
	private ProfileDao dao;
	
	private static final String CSV_MIME_TYPE="text/csv";


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
			
			if(uploadedProfileImage != null) {
				log.info("Profile2 image converter: ProfileImage record exists for " + userUuid + ". Nothing to do here, skipping to next section...");
			} else {
				log.info("Profile2 image converter: No existing ProfileImage record for " + userUuid + ". Processing...");
				
				//get photo from SakaiPerson
				byte[] image = sakaiProxy.getSakaiPersonJpegPhoto(userUuid);
				
				//if none, nothing to do
				if(image == null || image.length == 0) {
					log.info("Profile2 image converter: No image binary to convert for " + userUuid + ". Skipping to next section...");
				} else {
					
					//set some defaults for the image we are adding to ContentHosting
					String fileName = "Profile Image";
					String mimeType = "image/jpeg";
					
					//scale the main image
					byte[] imageMain = ProfileUtils.scaleImage(image, ProfileConstants.MAX_IMAGE_XY, mimeType);
					
					//create resource ID
					String mainResourceId = sakaiProxy.getProfileImageResourcePath(userUuid, ProfileConstants.PROFILE_IMAGE_MAIN);
					log.info("Profile2 image converter: mainResourceId: " + mainResourceId);
					
					//save, if error, log and return.
					if(!sakaiProxy.saveFile(mainResourceId, userUuid, fileName, mimeType, imageMain)) {
						log.error("Profile2 image converter: Saving main profile image failed.");
						continue;
					}
	
					/*
					 * THUMBNAIL PROFILE IMAGE
					 */
					//scale image
					byte[] imageThumbnail = ProfileUtils.scaleImage(image, ProfileConstants.MAX_THUMBNAIL_IMAGE_XY, mimeType);
					 
					//create resource ID
					String thumbnailResourceId = sakaiProxy.getProfileImageResourcePath(userUuid, ProfileConstants.PROFILE_IMAGE_THUMBNAIL);
	
					log.info("Profile2 image converter: thumbnailResourceId:" + thumbnailResourceId);
					
					//save, if error, log and return.
					if(!sakaiProxy.saveFile(thumbnailResourceId, userUuid, fileName, mimeType, imageThumbnail)) {
						log.warn("Profile2 image converter: Saving thumbnail profile image failed. Main image will be used instead.");
						thumbnailResourceId = null;
					}
	
					/*
					 * SAVE IMAGE RESOURCE IDS
					 */
					uploadedProfileImage = new ProfileImageUploaded(userUuid, mainResourceId, thumbnailResourceId, true);
					if(dao.addNewProfileImage(uploadedProfileImage)){
						log.info("Profile2 image converter: Binary image converted and saved for " + userUuid);
					} else {
						log.warn("Profile2 image converter: Binary image conversion failed for " + userUuid);
					}					
					
				}
			} 
			
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
					externalProfileImage = new ProfileImageExternal(userUuid, url, null);
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
	 * Import profiles form the given CSV file
	 * 
	 * Format is:
	 * 
	 * TBA
	 * 
	 * Files must be comma separated and each field surrounded with double quotes
	 * 
	 * @param path	path to CSV file on the server
	 */
	public void importProfiles(String path) {
		
		if(StringUtils.isBlank(path)) {
			log.warn("Profile2 importer: invalid path to CSV file. Aborting.");
			return;
		}
				
		CSVReader reader;
		try {
			reader = new CSVReader(new FileReader(path));
			List<String[]> lines = reader.readAll();
			for(String[] line: lines){
					
				//String eid = line[0]
				//TODO other fields
               
                //TODO create object 
				
				//TODO check if this user already exists. they are skipped if so.
				
				//TODO import this user
			}
			
			//catch some errors that mean we can safely skip, like indexoutofbounds etc
			
		} catch (Exception e) {
			log.error("Profile2 importer: " + e.getClass() + " : " + e.getMessage());
		}
			
		
		
	}
	
	
}
