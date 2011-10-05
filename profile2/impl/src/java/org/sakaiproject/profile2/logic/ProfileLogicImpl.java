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

package org.sakaiproject.profile2.logic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lombok.Setter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.profile2.dao.ProfileDao;
import org.sakaiproject.profile2.exception.ProfileNotDefinedException;
import org.sakaiproject.profile2.hbm.model.ProfileImageExternal;
import org.sakaiproject.profile2.hbm.model.ProfileImageUploaded;
import org.sakaiproject.profile2.model.BasicPerson;
import org.sakaiproject.profile2.model.CompanyProfile;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.model.SocialNetworkingInfo;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.types.PrivacyType;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;
import org.sakaiproject.user.api.User;

/**
 * Implementation of ProfileLogic for Profile2.
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class ProfileLogicImpl implements ProfileLogic {

	private static final Logger log = Logger.getLogger(ProfileLogicImpl.class);

	/**
	 * {@inheritDoc}
	 */
	public UserProfile getUserProfile(final String userUuid) {
		
		//check auth and get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if(currentUserUuid == null) {
			throw new SecurityException("Must be logged in to get a UserProfile.");
		}
		
		//get User
		User u = sakaiProxy.getUserById(userUuid);
		if(u == null) {
			log.error("User " + userUuid + " does not exist.");
			return null;
		}
		
		//setup obj
		UserProfile p = new UserProfile();
		p.setUserUuid(userUuid);
		p.setDisplayName(u.getDisplayName());
		p.setImageUrl(imageLogic.getProfileImageEntityUrl(userUuid, ProfileConstants.PROFILE_IMAGE_MAIN));
		p.setImageThumbUrl(imageLogic.getProfileImageEntityUrl(userUuid, ProfileConstants.PROFILE_IMAGE_THUMBNAIL));
			
		//get SakaiPerson
		SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userUuid);
		if(sakaiPerson == null) {
			//no profile, return basic info only.
			return p;
		}
		
		//transform
		p = transformSakaiPersonToUserProfile(p, sakaiPerson);
		
		//if person requested own profile or superuser, no need for privacy checks
		//add the additional information and return
		if(userUuid.equals(currentUserUuid) || sakaiProxy.isSuperUser()) {
			p.setEmail(u.getEmail());
			p.setStatus(statusLogic.getUserStatus(userUuid));
			p.setSocialInfo(getSocialNetworkingInfo(userUuid));
			p.setCompanyProfiles(getCompanyProfiles(userUuid));
			
			return p;
		}
		
		//REMOVE the birth year if not allowed
		if(!privacyLogic.isBirthYearVisible(userUuid)){
			p.setDateOfBirth(ProfileUtils.stripYear(p.getDateOfBirth()));
		}
		
		//REMOVE basic info if not allowed
		if(!privacyLogic.isActionAllowed(userUuid,currentUserUuid, PrivacyType.PRIVACY_OPTION_BASICINFO)) {
			p.setNickname(null);
			p.setDateOfBirth(null);
			p.setPersonalSummary(null);
		}
		
		//ADD email if allowed, REMOVE contact info if not
		if(privacyLogic.isActionAllowed(userUuid, currentUserUuid, PrivacyType.PRIVACY_OPTION_CONTACTINFO)) {
			p.setEmail(u.getEmail());
		} else {
			p.setEmail(null);
			p.setHomepage(null);
			p.setHomephone(null);
			p.setWorkphone(null);
			p.setMobilephone(null);
			p.setFacsimile(null);
		}
		
		//REMOVE staff info if not allowed
		if(!privacyLogic.isActionAllowed(userUuid, currentUserUuid, PrivacyType.PRIVACY_OPTION_STAFFINFO)) {
			p.setDepartment(null);
			p.setPosition(null);
			p.setSchool(null);
			p.setRoom(null);
			p.setStaffProfile(null);
			p.setAcademicProfileUrl(null);
			p.setUniversityProfileUrl(null);
			p.setPublications(null);
		}
		
		//REMOVE student info if not allowed
		if(!privacyLogic.isActionAllowed(userUuid, currentUserUuid, PrivacyType.PRIVACY_OPTION_STUDENTINFO)) {
			p.setCourse(null);
			p.setSubjects(null);
		}
		
		//REMOVE personal info if not allowed
		if(!privacyLogic.isActionAllowed(userUuid, currentUserUuid, PrivacyType.PRIVACY_OPTION_PERSONALINFO)) {
			p.setFavouriteBooks(null);
			p.setFavouriteTvShows(null);
			p.setFavouriteMovies(null);
			p.setFavouriteQuotes(null);
		}
		
		//ADD social networking info if allowed
		if(privacyLogic.isActionAllowed(userUuid, currentUserUuid, PrivacyType.PRIVACY_OPTION_SOCIALINFO)) {
			p.setSocialInfo(getSocialNetworkingInfo(userUuid));
		}
		
		//ADD company info if activated and allowed, REMOVE business bio if not
		if(sakaiProxy.isBusinessProfileEnabled()) {
			if(privacyLogic.isActionAllowed(userUuid, currentUserUuid, PrivacyType.PRIVACY_OPTION_BUSINESSINFO)) {
				p.setCompanyProfiles(getCompanyProfiles(userUuid));
			} else {
				p.setBusinessBiography(null);
			}
		} else {
			p.setBusinessBiography(null);
		}
		
		//ADD profile status if allowed
		if(privacyLogic.isActionAllowed(userUuid, currentUserUuid, PrivacyType.PRIVACY_OPTION_MYSTATUS)) {
			p.setStatus(statusLogic.getUserStatus(userUuid));
		}
		
		return p;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean saveUserProfile(UserProfile p) {
		
		SakaiPerson sp = transformUserProfileToSakaiPerson(p);
		
		//update SakaiPerson obj
		
		if(sakaiProxy.updateSakaiPerson(sp)) {
			
			if(p.getImageUrl() != null) {
				imageLogic.saveOfficialImageUrl(p.getUserUuid(), p.getImageUrl());
			}
			
			//TODO the fields that can update the Account need to be done as well, if allowed.
			//TODO if profile is locked,should not update, but will need to get the existing record if exists, then check that.
			
			return true;
		} 
		
		return false;
	}
	
	
	
	
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean addNewCompanyProfile(final CompanyProfile companyProfile) {
		
		if(dao.addNewCompanyProfile(companyProfile)){
			log.info("Added new company profile for user: " + companyProfile.getUserUuid()); 
			return true;
		} 
		
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean updateCompanyProfile(final CompanyProfile companyProfile) {

		if(dao.updateCompanyProfile(companyProfile)){
			log.info("Saved company profile for user: "+ companyProfile.getUserUuid());
			return true;
		}
		return false;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<CompanyProfile> getCompanyProfiles(final String userId) {
		return dao.getCompanyProfiles(userId);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean removeCompanyProfile(String userId, long companyProfileId) {
		if (userId == null || Long.valueOf(companyProfileId) == null) {
			throw new IllegalArgumentException("Null argument in ProfileLogicImpl.removeCompanyProfile()");
		}

		CompanyProfile companyProfile = dao.getCompanyProfile(userId, companyProfileId);

		if (companyProfile == null) {
			log.error("CompanyProfile record does not exist for userId: "+ userId + ", companyProfileId: " + companyProfileId);
			return false;
		}

		if(dao.removeCompanyProfile(companyProfile)){
			log.info("User: " + userId + " removed company profile: "+ companyProfileId);
			return true;
		}
		
		return false;
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public SocialNetworkingInfo getSocialNetworkingInfo(final String userId) {
		
		if(userId == null){
	  		throw new IllegalArgumentException("Null argument in ProfileLogic.getSocialNetworkingInfo"); 
	  	}
		
		SocialNetworkingInfo socialNetworkingInfo = dao.getSocialNetworkingInfo(userId);
		if (null == socialNetworkingInfo) {
			socialNetworkingInfo = new SocialNetworkingInfo(userId);
		}
		
		return socialNetworkingInfo;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean saveSocialNetworkingInfo(SocialNetworkingInfo socialNetworkingInfo) {

		if(dao.saveSocialNetworkingInfo(socialNetworkingInfo)) {
			log.info("Updated social networking info for user: " + socialNetworkingInfo.getUserUuid());
			return true;
		} 
		
		return false;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public BasicPerson getBasicPerson(String userUuid) {
		return getBasicPerson(sakaiProxy.getUserById(userUuid));
	}

	/**
 	 * {@inheritDoc}
 	 */
	public BasicPerson getBasicPerson(User user) {
		BasicPerson p = new BasicPerson();
		p.setUuid(user.getId());
		p.setDisplayName(user.getDisplayName());
		p.setType(user.getType());
		return p;
	}

	/**
 	 * {@inheritDoc}
 	 */
	public List<BasicPerson> getBasicPersons(List<User> users) {
		List<BasicPerson> list = new ArrayList<BasicPerson>();
		for(User u:users){
			list.add(getBasicPerson(u));
		}
		return list;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public Person getPerson(String userUuid) {
		return getPerson(sakaiProxy.getUserById(userUuid));
	}

	/**
 	 * {@inheritDoc}
 	 */
	public Person getPerson(User user) {
		//catch for non existent user
		if(user == null){
			return null;
		}
		Person p = new Person();
		String userUuid = user.getId();
		p.setUuid(userUuid);
		p.setDisplayName(user.getDisplayName());
		p.setType(user.getType());
		p.setPreferences(preferencesLogic.getPreferencesRecordForUser(userUuid));
		p.setPrivacy(privacyLogic.getPrivacyRecordForUser(userUuid));
		p.setProfile(getUserProfile(userUuid));
		
		return p;
	}

	/**
 	 * {@inheritDoc}
 	 */
	public List<Person> getPersons(List<User> users) {
		List<Person> list = new ArrayList<Person>();
		for(User u:users){
			list.add(getPerson(u));
		}
		return list;
	}

	/**
 	 * {@inheritDoc}
 	 */
	public List<String> getAllSakaiPersonIds() {
		return dao.getAllSakaiPersonIds();
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public int getAllSakaiPersonIdsCount() {
		return dao.getAllSakaiPersonIdsCount();
	}
	
	
	//service init
	public void init() {
		
		log.info("Profile2: init()"); 
		
		//do we need to run the conversion utility?
		if(sakaiProxy.isProfileConversionEnabled()) {
			convertProfile();
		}
	}
	
	//method to convert profileImages
	private void convertProfile() {
		log.info("Profile2: ==============================="); 
		log.info("Profile2: Conversion utility starting up."); 
		log.info("Profile2: ==============================="); 

		//get list of users
		List<String> allUsers = new ArrayList<String>(getAllSakaiPersonIds());
		
		if(allUsers.isEmpty()){
			log.info("Profile2 conversion util: No SakaiPersons to process.");
			return;
		}
		//for each, do they have a profile image record. if so, skip (perhaps null the SakaiPerson JPEG_PHOTO bytes?)
		for(Iterator<String> i = allUsers.iterator(); i.hasNext();) {
			String userUuid = (String)i.next();
			
			//get image record from dao directly, we don't need privacy/prefs here
			ProfileImageUploaded uploadedProfileImage = dao.getCurrentProfileImageRecord(userUuid);
			
			if(uploadedProfileImage != null) {
				log.info("Profile2 conversion util: ProfileImage record exists for " + userUuid + ". Nothing to do here, skipping to next section...");
			} else {
				log.info("Profile2 conversion util: No existing ProfileImage record for " + userUuid + ". Processing...");
				
				//get photo from SakaiPerson
				byte[] image = sakaiProxy.getSakaiPersonJpegPhoto(userUuid);
				
				//if none, nothing to do
				if(image == null || image.length == 0) {
					log.info("Profile2 conversion util: No image binary to convert for " + userUuid + ". Skipping to next section...");
				} else {
					
					//set some defaults for the image we are adding to ContentHosting
					String fileName = "Profile Image";
					String mimeType = "image/jpeg";
					
					//scale the main image
					byte[] imageMain = ProfileUtils.scaleImage(image, ProfileConstants.MAX_IMAGE_XY, mimeType);
					
					//create resource ID
					String mainResourceId = sakaiProxy.getProfileImageResourcePath(userUuid, ProfileConstants.PROFILE_IMAGE_MAIN);
					log.info("Profile2 conversion util: mainResourceId: " + mainResourceId);
					
					//save, if error, log and return.
					if(!sakaiProxy.saveFile(mainResourceId, userUuid, fileName, mimeType, imageMain)) {
						log.error("Profile2 conversion util: Saving main profile image failed.");
						continue;
					}
	
					/*
					 * THUMBNAIL PROFILE IMAGE
					 */
					//scale image
					byte[] imageThumbnail = ProfileUtils.scaleImage(image, ProfileConstants.MAX_THUMBNAIL_IMAGE_XY, mimeType);
					 
					//create resource ID
					String thumbnailResourceId = sakaiProxy.getProfileImageResourcePath(userUuid, ProfileConstants.PROFILE_IMAGE_THUMBNAIL);
	
					log.info("Profile2 conversion util: thumbnailResourceId:" + thumbnailResourceId);
					
					//save, if error, log and return.
					if(!sakaiProxy.saveFile(thumbnailResourceId, userUuid, fileName, mimeType, imageThumbnail)) {
						log.warn("Profile2 conversion util: Saving thumbnail profile image failed. Main image will be used instead.");
						thumbnailResourceId = null;
					}
	
					/*
					 * SAVE IMAGE RESOURCE IDS
					 */
					uploadedProfileImage = new ProfileImageUploaded(userUuid, mainResourceId, thumbnailResourceId, true);
					if(dao.addNewProfileImage(uploadedProfileImage)){
						log.info("Profile2 conversion util: Binary image converted and saved for " + userUuid);
					} else {
						log.warn("Profile2 conversion util: Binary image conversion failed for " + userUuid);
					}					
					
				}
			} 
			
			//process any image URLs, if they don't already have a valid record.
			ProfileImageExternal externalProfileImage = dao.getExternalImageRecordForUser(userUuid);
			if(externalProfileImage != null) {
				log.info("Profile2 conversion util: ProfileImageExternal record exists for " + userUuid + ". Nothing to do here, skipping...");
			} else {
				log.info("Profile2 conversion util: No existing ProfileImageExternal record for " + userUuid + ". Processing...");
				
				String url = sakaiProxy.getSakaiPersonImageUrl(userUuid);
				
				//if none, nothing to do
				if(StringUtils.isBlank(url)) {
					log.info("Profile2 conversion util: No url image to convert for " + userUuid + ". Skipping...");
				} else {
					externalProfileImage = new ProfileImageExternal(userUuid, url, null);
					if(dao.saveExternalImage(externalProfileImage)) {
						log.info("Profile2 conversion util: Url image converted and saved for " + userUuid);
					} else {
						log.warn("Profile2 conversion util: Url image conversion failed for " + userUuid);
					}
				}
				
			}
			
			log.info("Profile2 conversion util: Finished converting user profile for: " + userUuid);
			//go to next user
		}
		
		return;
	}
	
	/**
	 * Convenience method to map a SakaiPerson object onto a UserProfile object
	 * 
	 * @param sp 		input SakaiPerson
	 * @return			returns a UserProfile representation of the SakaiPerson object
	 */
	private UserProfile transformSakaiPersonToUserProfile(UserProfile p, SakaiPerson sp) {
		
		//map fields from SakaiPerson to UserProfile

		//basic info
		p.setNickname(sp.getNickname());
		p.setDateOfBirth(sp.getDateOfBirth());
		p.setPersonalSummary(sp.getNotes());
		
		//contact info
		p.setHomepage(sp.getLabeledURI());
		p.setWorkphone(sp.getTelephoneNumber());
		p.setHomephone(sp.getHomePhone());
		p.setMobilephone(sp.getMobile());
		p.setFacsimile(sp.getFacsimileTelephoneNumber());
		
		//staff info
		p.setDepartment(sp.getOrganizationalUnit());
		p.setPosition(sp.getTitle());
		p.setSchool(sp.getCampus());
		p.setRoom(sp.getRoomNumber());
		p.setStaffProfile(sp.getStaffProfile());
		p.setAcademicProfileUrl(sp.getAcademicProfileUrl());
		p.setUniversityProfileUrl(sp.getUniversityProfileUrl());
		p.setPublications(sp.getPublications());
		
		//student info
		p.setCourse(sp.getEducationCourse());
		p.setSubjects(sp.getEducationSubjects());
		
		//personal info
		p.setFavouriteBooks(sp.getFavouriteBooks());
		p.setFavouriteTvShows(sp.getFavouriteTvShows());
		p.setFavouriteMovies(sp.getFavouriteMovies());
		p.setFavouriteQuotes(sp.getFavouriteQuotes());
		
		//business info
		p.setBusinessBiography(sp.getBusinessBiography());
		
		return p;
	}
	
	/**
	 * Convenience method to map a UserProfile object onto a SakaiPerson object for persisting
	 * 
	 * @param up 		input SakaiPerson
	 * @return			returns a SakaiPerson representation of the UserProfile object
	 */
	private SakaiPerson transformUserProfileToSakaiPerson(UserProfile up) {
	
		String userUuid = up.getUserUuid();
		
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
		
		//academic info
		sakaiPerson.setOrganizationalUnit(up.getDepartment());
		sakaiPerson.setTitle(up.getPosition());
		sakaiPerson.setCampus(up.getSchool());
		sakaiPerson.setRoomNumber(up.getRoom());
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
	
	@Setter
	private SakaiProxy sakaiProxy;
	
	@Setter
	private ProfileDao dao;
	
	@Setter
	private ProfilePreferencesLogic preferencesLogic;
	
	@Setter
	private ProfileStatusLogic statusLogic;
	
	@Setter
	private ProfilePrivacyLogic privacyLogic;
	
	@Setter
	private ProfileConnectionsLogic connectionsLogic;
	
	@Setter
	private ProfileImageLogic imageLogic;
	
	
}
