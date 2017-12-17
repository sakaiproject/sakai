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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.profile2.conversion.ProfileConverter;
import org.sakaiproject.profile2.dao.ProfileDao;
import org.sakaiproject.profile2.model.BasicPerson;
import org.sakaiproject.profile2.model.CompanyProfile;
import org.sakaiproject.profile2.model.Person;
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
@Slf4j
public class ProfileLogicImpl implements ProfileLogic {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserProfile getUserProfile(final String userUuid) {
	    return getUserProfile(userUuid, null);
    }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserProfile getUserProfile(final String userUuid, final String siteId) {
		
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
			
		SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userUuid);
		if (sakaiPerson == null) {
			sakaiPerson = sakaiProxy.createSakaiPerson(userUuid);
			if (sakaiPerson == null) {
				return p;
			}
		}
		
		//transform
		p = transformSakaiPersonToUserProfile(p, sakaiPerson);
		
		//if person requested own profile or superuser, no need for privacy checks
		//add the additional information and return
		if(StringUtils.equals(userUuid, currentUserUuid) || sakaiProxy.isSuperUser()) {
			p.setEmail(u.getEmail());
			p.setStatus(statusLogic.getUserStatus(userUuid));
			p.setSocialInfo(getSocialNetworkingInfo(userUuid));
			p.setCompanyProfiles(getCompanyProfiles(userUuid));
			
			return p;
		}
		
		//REMOVE the birth year if not allowed
		if(!privacyLogic.isBirthYearVisible(userUuid)){
			if(p.getDateOfBirth() != null) {
				p.setDateOfBirth(ProfileUtils.stripYear(p.getDateOfBirth()));
			} else {
				p.setDateOfBirth(null);
			}
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
        } else if(siteId != null && sakaiProxy.isUserAllowedInSite(currentUserUuid, ProfileConstants.ROSTER_VIEW_EMAIL, siteId)) {
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
	@Override
	public boolean saveUserProfile(SakaiPerson sp) {
		
		if(sakaiProxy.updateSakaiPerson(sp)) {
			sendProfileChangeEmailNotification(sp.getAgentUuid());
			
			return true;
		}
		
		return false;
	}
	
	
	
	
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
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
	@Override
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
	@Override
	public List<CompanyProfile> getCompanyProfiles(final String userId) {
		return dao.getCompanyProfiles(userId);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
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
	@Override
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
	@Override
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
	@Override
	public BasicPerson getBasicPerson(String userUuid) {
		return getBasicPerson(sakaiProxy.getUserById(userUuid));
	}

	/**
 	 * {@inheritDoc}
 	 */
	@Override
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
	@Override
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
	@Override
	public Person getPerson(String userUuid) {
		return getPerson(sakaiProxy.getUserById(userUuid));
	}

	/**
 	 * {@inheritDoc}
 	 */
	@Override
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
	@Override
	public List<Person> getPersons(List<User> users) {
		List<Person> list = new ArrayList();
		for (User u : users) {
			list.add(getPerson(u));
		}
		return list;
	}

	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public List<String> getAllSakaiPersonIds() {
		return dao.getAllSakaiPersonIds();
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public int getAllSakaiPersonIdsCount() {
		return dao.getAllSakaiPersonIdsCount();
	}
	
	
	//service init
	public void init() {
		
		log.info("Profile2: init()"); 
		
		//do we need to run the image conversion utility?
		if(sakaiProxy.isProfileConversionEnabled()) {
			//run the profile image converter
			converter.convertProfileImages();
		}
		
		// Should we import profile image URLs to be uploaded profile images?
		if (sakaiProxy.isProfileImageImportEnabled()) {
			if (sakaiProxy.getProfilePictureType() != ProfileConstants.PICTURE_SETTING_UPLOAD) {
				log.warn("I'm set to import images but profile2.picture.type=upload is not set. Not importing.");
			} else {
				converter.importProfileImages();
			}
		}
		
		//do we need to import profiles?
		if(sakaiProxy.isProfileImportEnabled()) {
			
			String csv = sakaiProxy.getProfileImportCsvPath();
			//run the profile importer
			converter.importProfiles(csv);
		}
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
	 * Sends an email notification when a user changes their profile, if enabled.
	 * @param toUuid		the uuid of the user who changed their profile
	 */
	private void sendProfileChangeEmailNotification(final String userUuid) {
		
		//check if option is enabled
		boolean enabled = Boolean.valueOf(sakaiProxy.getServerConfigurationParameter("profile2.profile.change.email.enabled", "false"));
		if(!enabled) {
			return;
		}
		
		//get the user to send to. THis will be translated into an internal ID. Since SakaiProxy.sendEmail takes a userId as a param
		//it was easier to require an eid here (and thus the person needs to have an account) rather than create a new method that takes an email address.
		String eidTo = sakaiProxy.getServerConfigurationParameter("profile2.profile.change.email.eid", null);
		if(StringUtils.isBlank(eidTo)){
			log.error("Profile change email notification is enabled but no user eid to send it to is set. Please set 'profile2.profile.change.email.eid' in sakai.properties");
			return;
		}
		
		//get internal id for this user
		String userUuidTo = sakaiProxy.getUserIdForEid(eidTo);
		if(StringUtils.isBlank(userUuidTo)) {
			log.error("Profile change email notification is setup with an invalid eid. Please adjust 'profile2.profile.change.email.eid' in sakai.properties");
			return;
		}
		
		String emailTemplateKey = ProfileConstants.EMAIL_TEMPLATE_KEY_PROFILE_CHANGE_NOTIFICATION;
			
		//create the map of replacement values for this email template
		Map<String,String> replacementValues = new HashMap<String,String>();
		replacementValues.put("userDisplayName", sakaiProxy.getUserDisplayName(userUuid));
		replacementValues.put("localSakaiName", sakaiProxy.getServiceName());
		replacementValues.put("profileLink", linkLogic.getEntityLinkToProfileHome(userUuid));
		replacementValues.put("localSakaiUrl", sakaiProxy.getPortalUrl());

		sakaiProxy.sendEmail(userUuidTo, emailTemplateKey, replacementValues);
		return;
		
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
	
	@Setter
	private ProfileConverter converter;
	
	@Setter
	private ProfileLinkLogic linkLogic;
	
}
