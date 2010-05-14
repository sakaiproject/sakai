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
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.profile2.dao.ProfileDao;
import org.sakaiproject.profile2.exception.ProfileNotDefinedException;
import org.sakaiproject.profile2.model.BasicPerson;
import org.sakaiproject.profile2.model.CompanyProfile;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.model.ProfileFriend;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.model.SocialNetworkingInfo;
import org.sakaiproject.profile2.model.UserProfile;
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
		
		//get privacy record
		ProfilePrivacy privacy = privacyLogic.getPrivacyRecordForUser(userUuid);
		
		//check friend status
		boolean friend = connectionsLogic.isUserXFriendOfUserY(userUuid, currentUserUuid);
		
		//REMOVE basic info if not allowed
		if(!privacyLogic.isUserXBasicInfoVisibleByUserY(userUuid, privacy, currentUserUuid, friend)) {
			p.setNickname(null);
			p.setDateOfBirth(null);
			p.setPersonalSummary(null);
		}
		
		//ADD email if allowed, REMOVE contact info if not
		if(privacyLogic.isUserXContactInfoVisibleByUserY(userUuid, privacy, currentUserUuid, friend)) {
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
		if(!privacyLogic.isUserXStaffInfoVisibleByUserY(userUuid, privacy, currentUserUuid, friend)) {
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
		if(!privacyLogic.isUserXStudentInfoVisibleByUserY(userUuid, privacy, currentUserUuid, friend)) {
			p.setCourse(null);
			p.setSubjects(null);
		}
		
		//REMOVE personal info if not allowed
		if(!privacyLogic.isUserXPersonalInfoVisibleByUserY(userUuid, privacy, currentUserUuid, friend)) {
			p.setFavouriteBooks(null);
			p.setFavouriteTvShows(null);
			p.setFavouriteMovies(null);
			p.setFavouriteQuotes(null);
		}
		
		//ADD social networking info if allowed
		if(privacyLogic.isUserXSocialNetworkingInfoVisibleByUserY(userUuid, privacy, currentUserUuid, friend)) {
			p.setSocialInfo(getSocialNetworkingInfo(userUuid));
		}
		
		//ADD company info if activated and allowed, REMOVE business bio if not
		if(sakaiProxy.isBusinessProfileEnabled()) {
			if(privacyLogic.isUserXBusinessInfoVisibleByUserY(userUuid, privacy, currentUserUuid, friend)) {
				p.setCompanyProfiles(getCompanyProfiles(userUuid));
			} else {
				p.setBusinessBiography(null);
			}
		} else {
			p.setBusinessBiography(null);
		}
		
		//ADD profile status if allowed
		if(privacyLogic.isUserXStatusVisibleByUserY(userUuid, privacy, currentUserUuid, friend)) {
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
		if (userId == null || new Long(companyProfileId) == null) {
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
		
		return dao.getSocialNetworkingInfo(userId);
	}

	/**
 	 * {@inheritDoc}
 	 */
	public SocialNetworkingInfo getDefaultSocialNetworkingInfo(String userId) {
		return new SocialNetworkingInfo(userId);
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
	public List<Person> findUsersByNameOrEmail(String search) {
		
		List<User> users = new ArrayList<User>();
		List<String> sakaiPersonUuids = new ArrayList<String>();
		
		//add users from SakaiPerson (clean list)
		sakaiPersonUuids = dao.findSakaiPersonsByNameOrEmail(search);
		users.addAll(sakaiProxy.getUsers(sakaiPersonUuids));

		//add local users from UserDirectoryService
		users.addAll(sakaiProxy.searchUsers(search));
		
		//add external users from UserDirectoryService
		users.addAll(sakaiProxy.searchExternalUsers(search));
		
		//remove duplicates
		ProfileUtils.removeDuplicates(users);
		
		log.debug("Found " + users.size() + " results for search: " + search);
		
		//restrict to only return the max number. UI will print message
		int maxResults = ProfileConstants.MAX_SEARCH_RESULTS;
		if(users.size() >= maxResults) {
			users = users.subList(0, maxResults);
		}
		
		//remove invisible
		users = removeInvisibleUsers(users);
		
		return getPersons(users);
	}
	
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<Person> findUsersByInterest(String search) {
		
		List<User> users = new ArrayList<User>();
		List<String> sakaiPersonUuids = new ArrayList<String>();
		
		//add users from SakaiPerson		
		sakaiPersonUuids = dao.findSakaiPersonsByInterest(search);
		users.addAll(sakaiProxy.getUsers(sakaiPersonUuids));
		
		//restrict to only return the max number. UI will print message
		int maxResults = ProfileConstants.MAX_SEARCH_RESULTS;
		if(users.size() >= maxResults) {
			users = users.subList(0, maxResults);
		}
		
		//remove invisible
		users = removeInvisibleUsers(users);
		
		return getPersons(users);
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
	
	
	
	
	
	
	
	/**
	 * Remove invisible users from the list
	 * @param users
	 * @return cleaned list
	 */
	private List<User> removeInvisibleUsers(List<User> users){
		
		//if superuser return list unchanged.
		if(sakaiProxy.isSuperUser()){
			return users;
		}
		
		//get list of invisible users as Users
		List<User> invisibleUsers = sakaiProxy.getUsers(sakaiProxy.getInvisibleUsers());
		if(invisibleUsers.isEmpty()) {
			return users;
		}
		
		//remove
		users.removeAll(invisibleUsers);
		
		return users;
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
	

	
	
	
	private SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}
	
	private ProfileDao dao;
	public void setDao(ProfileDao dao) {
		this.dao = dao;
	}
	
	private ProfilePreferencesLogic preferencesLogic;
	public void setPreferencesLogic(ProfilePreferencesLogic preferencesLogic) {
		this.preferencesLogic = preferencesLogic;
	}
	
	private ProfileStatusLogic statusLogic;
	public void setStatusLogic(ProfileStatusLogic statusLogic) {
		this.statusLogic = statusLogic;
	}
	
	private ProfilePrivacyLogic privacyLogic;
	public void setPrivacyLogic(ProfilePrivacyLogic privacyLogic) {
		this.privacyLogic = privacyLogic;
	}
	
	private ProfileConnectionsLogic connectionsLogic;
	public void setConnectionsLogic(ProfileConnectionsLogic connectionsLogic) {
		this.connectionsLogic = connectionsLogic;
	}
	
	private ProfileImageLogic imageLogic;
	public void setImageLogic(ProfileImageLogic imageLogic) {
		this.imageLogic = imageLogic;
	}
	
	
	
}
