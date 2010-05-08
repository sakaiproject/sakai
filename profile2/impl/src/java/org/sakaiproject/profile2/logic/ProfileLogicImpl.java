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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.profile2.dao.ProfileDao;
import org.sakaiproject.profile2.exception.ProfileNotDefinedException;
import org.sakaiproject.profile2.hbm.model.ProfileImageExternal;
import org.sakaiproject.profile2.hbm.model.ProfileImageOfficial;
import org.sakaiproject.profile2.hbm.model.ProfileImageUploaded;
import org.sakaiproject.profile2.hbm.model.ProfileKudos;
import org.sakaiproject.profile2.model.BasicPerson;
import org.sakaiproject.profile2.model.CompanyProfile;
import org.sakaiproject.profile2.model.Message;
import org.sakaiproject.profile2.model.MessageParticipant;
import org.sakaiproject.profile2.model.MessageThread;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.model.ProfileFriend;
import org.sakaiproject.profile2.model.ProfilePreferences;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.model.ProfileStatus;
import org.sakaiproject.profile2.model.SocialNetworkingInfo;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;
import org.sakaiproject.user.api.User;

import twitter4j.Twitter;

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
		p.setImageUrl(getProfileImageEntityUrl(userUuid, ProfileConstants.PROFILE_IMAGE_MAIN));
		p.setImageThumbUrl(getProfileImageEntityUrl(userUuid, ProfileConstants.PROFILE_IMAGE_THUMBNAIL));
			
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
			p.setStatus(getUserStatus(userUuid));
			p.setSocialInfo(getSocialNetworkingInfo(userUuid));
			p.setCompanyProfiles(getCompanyProfiles(userUuid));
			
			return p;
		}
		
		//get privacy record
		ProfilePrivacy privacy = getPrivacyRecordForUser(userUuid);
		
		//check friend status
		boolean friend = isUserXFriendOfUserY(userUuid, currentUserUuid);
		
		//REMOVE basic info if not allowed
		if(!isUserXBasicInfoVisibleByUserY(userUuid, privacy, currentUserUuid, friend)) {
			p.setNickname(null);
			p.setDateOfBirth(null);
			p.setPersonalSummary(null);
		}
		
		//ADD email if allowed, REMOVE contact info if not
		if(isUserXContactInfoVisibleByUserY(userUuid, privacy, currentUserUuid, friend)) {
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
		if(!isUserXStaffInfoVisibleByUserY(userUuid, privacy, currentUserUuid, friend)) {
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
		if(!isUserXStudentInfoVisibleByUserY(userUuid, privacy, currentUserUuid, friend)) {
			p.setCourse(null);
			p.setSubjects(null);
		}
		
		//REMOVE personal info if not allowed
		if(!isUserXPersonalInfoVisibleByUserY(userUuid, privacy, currentUserUuid, friend)) {
			p.setFavouriteBooks(null);
			p.setFavouriteTvShows(null);
			p.setFavouriteMovies(null);
			p.setFavouriteQuotes(null);
		}
		
		//ADD social networking info if allowed
		if(isUserXSocialNetworkingInfoVisibleByUserY(userUuid, privacy, currentUserUuid, friend)) {
			p.setSocialInfo(getSocialNetworkingInfo(userUuid));
		}
		
		//ADD company info if activated and allowed, REMOVE business bio if not
		if(sakaiProxy.isBusinessProfileEnabled()) {
			if(isUserXBusinessInfoVisibleByUserY(userUuid, privacy, currentUserUuid, friend)) {
				p.setCompanyProfiles(getCompanyProfiles(userUuid));
			} else {
				p.setBusinessBiography(null);
			}
		} else {
			p.setBusinessBiography(null);
		}
		
		//ADD profile status if allowed
		if(isUserXStatusVisibleByUserY(userUuid, privacy, currentUserUuid, friend)) {
			p.setStatus(getUserStatus(userUuid));
		}
		
		return p;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean saveUserProfile(UserProfile p) {
		
		/*
		
		SakaiPerson sp = transformUserProfileToSakaiPerson(p);
		
		//update SakaiPerson obj
		
		if(sakaiProxy.updateSakaiPerson(sp)) {
			
			//TODO the fields that can update the Account need to be done as well, if allowed.
			//TODO if profile is locked,should not update, but will need to get the existing record if exists, then check that.
			
			return true;
		} 
		*/
		return false;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<BasicPerson> getBasicConnectionsForUser(final String userUuid) {
		
		//check auth and get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if(currentUserUuid == null) {
			throw new SecurityException("You must be logged in to get a connection list.");
		}
		
		List<User> users = new ArrayList<User>();
		
		//check privacy
		boolean friend = isUserXFriendOfUserY(userUuid, currentUserUuid);
		if(!isUserXFriendsListVisibleByUserY(userUuid, currentUserUuid, friend)) {
			return new ArrayList<BasicPerson>();
		}
		
		users = sakaiProxy.getUsers(dao.getConfirmedConnectionUserIdsForUser(userUuid));
		
		return getBasicPersons(users);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<Person> getConnectionsForUser(final String userUuid) {
		
		//check auth and get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if(currentUserUuid == null) {
			throw new SecurityException("You must be logged in to get a connection list.");
		}
		
		List<User> users = new ArrayList<User>();
		
		//check privacy
		boolean friend = isUserXFriendOfUserY(userUuid, currentUserUuid);
		if(!isUserXFriendsListVisibleByUserY(userUuid, currentUserUuid, friend)) {
			return new ArrayList<Person>();
		}
		
		users = sakaiProxy.getUsers(dao.getConfirmedConnectionUserIdsForUser(userUuid));
		
		return getPersons(users);
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */	
	public int getConnectionsForUserCount(final String userId) {
		return getConnectionsForUser(userId).size();
	}
	
	/**
 	 * {@inheritDoc}
 	 */	
	public List<Person> getConnectionRequestsForUser(final String userId) {
		
		List<User> users = new ArrayList<User>();
		users = sakaiProxy.getUsers(dao.getRequestedConnectionUserIdsForUser(userId));
		
		return getPersons(users);
	}
	
	/**
 	 * {@inheritDoc}
 	 */	
	public int getConnectionRequestsForUserCount(final String userId) {
		return dao.getConnectionRequestsForUserCount(userId);
	}


	
	/**
 	 * {@inheritDoc}
 	 */
	public List<Person> getConnectionsSubsetForSearch(List<Person> connections, String search) {
		
		List<Person> subList = new ArrayList<Person>();
		
		for(Person p : connections){
			if(StringUtils.startsWithIgnoreCase(p.getDisplayName(), search)) {
				if(subList.size() == ProfileConstants.MAX_CONNECTIONS_PER_SEARCH) {
					break;
				}
				subList.add(p);
			}
		}
		return subList;
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public int getConnectionStatus(String userA, String userB) {
		ProfileFriend record = dao.getConnectionRecord(userA, userB);
		
		//no connection
		if(record == null) {
			return ProfileConstants.CONNECTION_NONE;
		}
		
		//confirmed
		if(record.isConfirmed()) {
			return ProfileConstants.CONNECTION_CONFIRMED;
		}
		
		//requested
		if(StringUtils.equals(userA, record.getUserUuid()) && !record.isConfirmed()) {
			return ProfileConstants.CONNECTION_REQUESTED;
		}
		
		//incoming
		if(StringUtils.equals(userA, record.getFriendUuid()) && !record.isConfirmed()) {
			return ProfileConstants.CONNECTION_INCOMING;
		}
		
		return ProfileConstants.CONNECTION_NONE;
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean requestFriend(String userId, String friendId) {
		
		if(userId == null || friendId == null){
	  		throw new IllegalArgumentException("Null argument in ProfileLogic.getFriendsForUser"); 
	  	}
		
		//TODO check values are valid, ie userId, friendId etc
		
		//make a ProfileFriend object with 'Friend Request' constructor
		ProfileFriend profileFriend = new ProfileFriend(userId, friendId, ProfileConstants.RELATIONSHIP_FRIEND);
		
		//make the request
		if(dao.addNewConnection(profileFriend)) {
			
			log.info("User: " + userId + " requested friend: " + friendId);  

			//send email notification
			sendConnectionEmailNotification(friendId, userId, ProfileConstants.EMAIL_NOTIFICATION_REQUEST);
			return true;
		}
		return false;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean isFriendRequestPending(String fromUser, String toUser) {
		
		ProfileFriend profileFriend = dao.getPendingConnection(fromUser, toUser);

		if(profileFriend == null) {
			log.debug("ProfileLogic.isFriendRequestPending: No pending friend request from userId: " + fromUser + " to friendId: " + toUser + " found.");   
			return false;
		}
		
		return true;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean confirmFriendRequest(final String fromUser, final String toUser) {
		
		if(fromUser == null || toUser == null){
	  		throw new IllegalArgumentException("Null argument in ProfileLogic.confirmFriendRequest"); 
	  	}
		
		//get pending ProfileFriend object request for the given details
		ProfileFriend profileFriend = dao.getPendingConnection(fromUser, toUser);

		if(profileFriend == null) {
			log.error("ProfileLogic.confirmFriendRequest() failed. No pending friend request from userId: " + fromUser + " to friendId: " + toUser + " found.");   
			return false;
		}
		
	  	//make necessary changes to the ProfileFriend object.
	  	profileFriend.setConfirmed(true);
	  	profileFriend.setConfirmedDate(new Date());
		
		if(dao.updateConnection(profileFriend)) {
			
			log.info("User: " + fromUser + " confirmed friend request from: " + toUser); 
			//send email notification
			sendConnectionEmailNotification(fromUser, toUser, ProfileConstants.EMAIL_NOTIFICATION_CONFIRM);
			
			return true;
		} 
		
		return false;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean ignoreFriendRequest(final String fromUser, final String toUser) {
		
		if(fromUser == null || toUser == null){
	  		throw new IllegalArgumentException("Null argument in ProfileLogic.ignoreFriendRequest"); 
	  	}
		
		//get pending ProfileFriend object request for the given details
		ProfileFriend profileFriend = dao.getPendingConnection(fromUser, toUser);

		if(profileFriend == null) {
			log.error("ProfileLogic.ignoreFriendRequest() failed. No pending friend request from userId: " + fromUser + " to friendId: " + toUser + " found.");   
			return false;
		}
	  	
		//delete
		if(dao.removeConnection(profileFriend)) {
			log.info("User: " + toUser + " ignored friend request from: " + fromUser);  
			return true;
		}
		
		return false;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean removeFriend(String userId, String friendId) {
		
		if(userId == null || friendId == null){
	  		throw new IllegalArgumentException("Null argument in ProfileLogic.removeFriend"); 
	  	}
		
		//get the friend object for this connection pair (could be any way around)
		ProfileFriend profileFriend = dao.getConnectionRecord(userId, friendId);
		
		if(profileFriend == null){
			log.error("ProfileFriend record does not exist for userId: " + userId + ", friendId: " + friendId);  
			return false;
		}
				
		//delete
		if(dao.removeConnection(profileFriend)) {
			log.info("User: " + userId + " remove friend: " + friendId);  
			return true;
		}
		return false;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public ProfileStatus getUserStatus(final String userUuid, ProfilePrivacy privacy) {
		
		//check privacy
		if(privacy == null){
			return null;
		}
		
		String currentUserUuid = sakaiProxy.getCurrentUserId();

		//if not same, check privacy
        if(!StringUtils.equals(userUuid, currentUserUuid)) {
		
        	//friend?
        	boolean friend = isUserXFriendOfUserY(userUuid, currentUserUuid);
		
        	//check allowed
        	if(!isUserXStatusVisibleByUserY(userUuid, privacy, currentUserUuid, friend)){
        		return null;
        	}
        }
		
		// compute oldest date for status 
		Calendar cal = Calendar.getInstance(); 
		cal.add(Calendar.DAY_OF_YEAR, -7); 
		final Date oldestStatusDate = cal.getTime(); 
		
		//get data
		ProfileStatus status = dao.getUserStatus(userUuid, oldestStatusDate);
		if(status == null){
			return null;
		}
		
		//format the date field
		if(status.getDateAdded() != null){
			status.setDateFormatted(ProfileUtils.convertDateForStatus(status.getDateAdded()));
		}
		
		return status;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public ProfileStatus getUserStatus(final String userUuid) {
		return getUserStatus(userUuid, getPrivacyRecordForUser(userUuid));
	}
	
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean setUserStatus(String userId, String status) {
		
		//create object
		ProfileStatus profileStatus = new ProfileStatus(userId,status,new Date());
		
		return setUserStatus(profileStatus);
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean setUserStatus(ProfileStatus profileStatus) {
		
		if(dao.setUserStatus(profileStatus)){
			log.info("Updated status for user: " + profileStatus.getUserUuid()); 
			return true;
		} 
		
		return false;
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean clearUserStatus(String userId) {
		
		ProfileStatus profileStatus = getUserStatus(userId);
		
		if(profileStatus == null){
			log.error("ProfileStatus null for userId: " + userId); 
			return false;
		}
				
		if(dao.clearUserStatus(profileStatus)) {
			log.info("User: " + userId + " cleared status");  
			return true;
		}
		
		return false;
	}

	
	

	
	
	
	
	
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public ProfilePrivacy getPrivacyRecordForUser(final String userId) {
		
		if(userId == null){
	  		throw new IllegalArgumentException("Null argument in ProfileLogic.getPrivacyRecordForUser"); 
	  	}
		
		//will stay null if we can't get or create one
		ProfilePrivacy privacy = null;
		
		privacy = dao.getPrivacyRecord(userId);
		
		//if none, create and persist a default
		if(privacy == null) {
			privacy = dao.addNewPrivacyRecord(getDefaultPrivacyRecord(userId));
			if(privacy != null) {
				sakaiProxy.postEvent(ProfileConstants.EVENT_PRIVACY_NEW, "/profile/"+userId, true);
				log.info("Created default privacy record for user: " + userId); 
			}
		}
		
		return privacy;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean savePrivacyRecord(ProfilePrivacy profilePrivacy) {

		//if changes not allowed
		if(!sakaiProxy.isPrivacyChangeAllowedGlobally()) {
			log.warn("Privacy changes are not permitted as per sakai.properties setting 'profile2.privacy.change.enabled'.");
			return false;
		}
		
		if(dao.updatePrivacyRecord(profilePrivacy)) {
			log.info("Saved privacy record for user: " + profilePrivacy.getUserUuid()); 
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
	public boolean isUserXFriendOfUserY(String userX, String userY) {
		
		//if same then friends.
		//added this check so we don't need to do it everywhere else and can call isFriend for all user pairs.
		if(userY.equals(userX)) {
			return true;
		}
		
		//get friends of current user
		//TODO change this to be a single lookup rather than iterating over a list
		List<String> friendUuids = new ArrayList<String>(dao.getConfirmedConnectionUserIdsForUser(userY));
		
		//if list of confirmed friends contains this user, they are a friend
		if(friendUuids.contains(userX)) {
			return true;
		}
		
		return false;
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean isUserXProfileImageVisibleByUserY(String userX, String userY, boolean friend) {
		
		//if user is requesting own info, they ARE allowed
    	if(userY.equals(userX)) {
    		return true;
    	}
		
		//get privacy record for userX
    	ProfilePrivacy profilePrivacy = getPrivacyRecordForUser(userX);
    	
    	//pass to main
    	return isUserXProfileImageVisibleByUserY(userX, profilePrivacy, userY, friend);
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean isUserXProfileImageVisibleByUserY(String userX, ProfilePrivacy profilePrivacy, String userY, boolean friend) {
		
		//if user is requesting own info, they ARE allowed
    	if(userY.equals(userX)) {
    		return true;
    	}
		
		//if no privacy record, return whatever the flag is set as by default
    	/* deprecated by PRFL-86, privacy object will never be null now it will always be default or overridden default
    	if(profilePrivacy == null) {
    		return ProfileConstants.DEFAULT_PROFILEIMAGE_VISIBILITY;
    	}
    	*/
    	
    	//if restricted to only self, not allowed
    	/* DEPRECATED via PRFL-24 when the privacy settings were relaxed
    	if(profilePrivacy.getProfile() == ProfilePrivacyManager.PRIVACY_OPTION_ONLYME) {
    		return false;
    	}
    	*/
    	
    	//if user is friend and friends are allowed
    	if(friend && profilePrivacy.getProfileImage() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
    		return true;
    	}
    	
    	//if not friend and set to friends only
    	if(!friend && profilePrivacy.getProfileImage() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
    		return false;
    	}
    	
    	//if everyone is allowed
    	if(profilePrivacy.getProfileImage() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
    		return true;
    	}
    	
    	//uncaught rule, return false
    	log.error("ProfileLogic.isUserProfileImageVisibleByCurrentUser. Uncaught rule. userX: " + userX + ", userY: " + userY + ", friend: " + friend);   
    	return false;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean isUserXBasicInfoVisibleByUserY(String userX, String userY, boolean friend) {
		
		//if user is requesting own info, they ARE allowed
    	if(userY.equals(userX)) {
    		return true;
    	}
		
		//get privacy record for userX
    	ProfilePrivacy profilePrivacy = getPrivacyRecordForUser(userX);
    	
    	//pass to main
    	return isUserXBasicInfoVisibleByUserY(userX, profilePrivacy, userY, friend);
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean isUserXBasicInfoVisibleByUserY(String userX, ProfilePrivacy profilePrivacy, String userY, boolean friend) {
		
		//if user is requesting own info, they ARE allowed
    	if(userY.equals(userX)) {
    		return true;
    	}
		
		//if no privacy record, return whatever the flag is set as by default
    	/* deprecated by PRFL-86, privacy object will never be null now it will always be default or overridden default
    	if(profilePrivacy == null) {
    		return ProfileConstants.DEFAULT_BASICINFO_VISIBILITY;
    	}
    	*/
    	
    	//if restricted to only self, not allowed
    	if(profilePrivacy.getBasicInfo() == ProfileConstants.PRIVACY_OPTION_ONLYME) {
    		return false;
    	}
    	
    	//if user is friend and friends are allowed
    	if(friend && profilePrivacy.getBasicInfo() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
    		return true;
    	}
    	
    	//if not friend and set to friends only
    	if(!friend && profilePrivacy.getBasicInfo() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
    		return false;
    	}
    	
    	//if everyone is allowed
    	if(profilePrivacy.getBasicInfo() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
    		return true;
    	}
    	
    	//uncaught rule, return false
    	log.error("ProfileLogic.isUserXBasicInfoVisibleByUserY. Uncaught rule. userX: " + userX + ", userY: " + userY + ", friend: " + friend);   
    	return false;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean isUserXContactInfoVisibleByUserY(String userX, String userY, boolean friend) {
		
		//if user is requesting own info, they ARE allowed
    	if(userY.equals(userX)) {
    		return true;
    	}
		
		//get privacy record for this user
    	ProfilePrivacy profilePrivacy = getPrivacyRecordForUser(userX);
    	
    	//pass to main
    	return isUserXContactInfoVisibleByUserY(userX, profilePrivacy, userY, friend);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean isUserXContactInfoVisibleByUserY(String userX, ProfilePrivacy profilePrivacy, String userY, boolean friend) {
		
		//if user is requesting own info, they ARE allowed
    	if(userY.equals(userX)) {
    		return true;
    	}
		
		//if no privacy record, return whatever the flag is set as by default
    	/* deprecated by PRFL-86, privacy object will never be null now it will always be default or overridden default
    	if(profilePrivacy == null) {
    		return ProfileConstants.DEFAULT_CONTACTINFO_VISIBILITY;
    	}
    	*/
    	
    	//if restricted to only self, not allowed
    	if(profilePrivacy.getContactInfo() == ProfileConstants.PRIVACY_OPTION_ONLYME) {
    		return false;
    	}
    	
    	//if user is friend and friends are allowed
    	if(friend && profilePrivacy.getContactInfo() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
    		return true;
    	}
    	
    	//if not friend and set to friends only
    	if(!friend && profilePrivacy.getContactInfo() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
    		return false;
    	}
    	
    	//if everyone is allowed
    	if(profilePrivacy.getContactInfo() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
    		return true;
    	}
    	
    	//uncaught rule, return false
    	log.error("ProfileLogic.isUserXContactInfoVisibleByUserY. Uncaught rule. userX: " + userX + ", userY: " + userY + ", friend: " + friend);   

    	return false;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean isUserXStaffInfoVisibleByUserY(String userX, String userY, boolean friend) {
		
		//if user is requesting own info, they ARE allowed
    	if(userY.equals(userX)) {
    		return true;
    	}
    	
		//get privacy record for this user
    	ProfilePrivacy profilePrivacy = getPrivacyRecordForUser(userX);
    	
    	//pass to main
    	return isUserXStaffInfoVisibleByUserY(userX, profilePrivacy, userY, friend);
	}

	/**
 	 * {@inheritDoc}
 	 */
	public boolean isUserXStaffInfoVisibleByUserY(String userX, ProfilePrivacy profilePrivacy, String userY, boolean friend) {
		
		//if user is requesting own info, they ARE allowed
    	if(userY.equals(userX)) {
    		return true;
    	}
		    	
    	//if restricted to only self, not allowed
    	if(profilePrivacy.getStaffInfo() == ProfileConstants.PRIVACY_OPTION_ONLYME) {
    		return false;
    	}
    	
    	//if user is friend and friends are allowed
    	if(friend && profilePrivacy.getStaffInfo() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
    		return true;
    	}
    	
    	//if not friend and set to friends only
    	if(!friend && profilePrivacy.getStaffInfo() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
    		return false;
    	}
    	
    	//if everyone is allowed
    	if(profilePrivacy.getStaffInfo() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
    		return true;
    	}
    	
    	//uncaught rule, return false
    	log.error("ProfileLogic.isUserXStaffInfoVisibleByUserY. Uncaught rule. userX: " + userX + ", userY: " + userY + ", friend: " + friend);   

    	return false;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean isUserXStudentInfoVisibleByUserY(String userX, String userY, boolean friend) {
		
		//if user is requesting own info, they ARE allowed
    	if(userY.equals(userX)) {
    		return true;
    	}
		
		//get privacy record for this user
    	ProfilePrivacy profilePrivacy = getPrivacyRecordForUser(userX);
    	
    	//pass to main
    	return isUserXStudentInfoVisibleByUserY(userX, profilePrivacy, userY, friend);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean isUserXStudentInfoVisibleByUserY(String userX, ProfilePrivacy profilePrivacy, String userY, boolean friend) {
		
		//if user is requesting own info, they ARE allowed
    	if(userY.equals(userX)) {
    		return true;
    	}
    	
    	//if restricted to only self, not allowed
    	if(profilePrivacy.getStudentInfo() == ProfileConstants.PRIVACY_OPTION_ONLYME) {
    		return false;
    	}
    	
    	//if user is friend and friends are allowed
    	if(friend && profilePrivacy.getStudentInfo() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
    		return true;
    	}
    	
    	//if not friend and set to friends only
    	if(!friend && profilePrivacy.getStudentInfo() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
    		return false;
    	}
    	
    	//if everyone is allowed
    	if(profilePrivacy.getStudentInfo() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
    		return true;
    	}
    	
    	//uncaught rule, return false
    	log.error("ProfileLogic.isUserXStudentInfoVisibleByUserY. Uncaught rule. userX: " + userX + ", userY: " + userY + ", friend: " + friend);   

    	return false;
	}

	/**
 	 * {@inheritDoc}
 	 */
	public boolean isUserXBusinessInfoVisibleByUserY(String userX, ProfilePrivacy profilePrivacy, String userY, boolean friend) {
		
		//if user is requesting own info, they ARE allowed
    	if(userY.equals(userX)) {
    		return true;
    	}
    	
    	//if restricted to only self, not allowed
    	if(profilePrivacy.getBusinessInfo() == ProfileConstants.PRIVACY_OPTION_ONLYME) {
    		return false;
    	}
    	
    	//if user is friend and friends are allowed
    	if(friend && profilePrivacy.getBusinessInfo() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
    		return true;
    	}
    	
    	//if not friend and set to friends only
    	if(!friend && profilePrivacy.getBusinessInfo() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
    		return false;
    	}
    	
    	//if everyone is allowed
    	if(profilePrivacy.getBusinessInfo() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
    		return true;
    	}
    	
    	//uncaught rule, return false
    	log.error("ProfileLogic.isUserXBusinessInfoVisibleByUserY. Uncaught rule. userX: " + userX + ", userY: " + userY + ", friend: " + friend);  
    	
		return false;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean isUserXSocialNetworkingInfoVisibleByUserY(String userX,
			ProfilePrivacy profilePrivacy, String userY, boolean friend) {
		
		//if user is requesting own info, they ARE allowed
    	if(userY.equals(userX)) {
    		return true;
    	}
    	
    	//if restricted to only self, not allowed
    	if(profilePrivacy.getSocialNetworkingInfo() == ProfileConstants.PRIVACY_OPTION_ONLYME) {
    		return false;
    	}
    	
    	//if user is friend and friends are allowed
    	if(friend && profilePrivacy.getSocialNetworkingInfo() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
    		return true;
    	}
    	
    	//if not friend and set to friends only
    	if(!friend && profilePrivacy.getSocialNetworkingInfo() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
    		return false;
    	}
    	
    	//if everyone is allowed
    	if(profilePrivacy.getSocialNetworkingInfo() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
    		return true;
    	}
    	
    	//uncaught rule, return false
    	log.error("ProfileLogic.isUserXSocialNetworkingInfoVisibleByUserY. Uncaught rule. userX: " + userX + ", userY: " + userY + ", friend: " + friend);  
    	
		return false;
	}

	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean isUserXPersonalInfoVisibleByUserY(String userX, String userY, boolean friend) {
		
		//if user is requesting own info, they ARE allowed
    	if(userY.equals(userX)) {
    		return true;
    	}
		
		//get privacy record for this user
    	ProfilePrivacy profilePrivacy = getPrivacyRecordForUser(userX);
    	
    	//pass to main
    	return isUserXPersonalInfoVisibleByUserY(userX, profilePrivacy, userY, friend);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean isUserXPersonalInfoVisibleByUserY(String userX, ProfilePrivacy profilePrivacy, String userY, boolean friend) {
		
		//if user is requesting own info, they ARE allowed
    	if(userY.equals(userX)) {
    		return true;
    	}
		
		//if no privacy record, return whatever the flag is set as by default
    	/* deprecated by PRFL-86, privacy object will never be null now it will always be default or overridden default
    	if(profilePrivacy == null) {
    		return ProfileConstants.DEFAULT_PERSONALINFO_VISIBILITY;
    	}
    	*/
    	
    	//if restricted to only self, not allowed
    	if(profilePrivacy.getPersonalInfo() == ProfileConstants.PRIVACY_OPTION_ONLYME) {
    		return false;
    	}
    	
    	//if user is friend and friends are allowed
    	if(friend && profilePrivacy.getPersonalInfo() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
    		return true;
    	}
    	
    	//if not friend and set to friends only
    	if(!friend && profilePrivacy.getPersonalInfo() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
    		return false;
    	}
    	
    	//if everyone is allowed
    	if(profilePrivacy.getPersonalInfo() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
    		return true;
    	}
    	    	
    	//uncaught rule, return false
    	log.error("ProfileLogic.isUserXPersonalInfoVisibleByUserY. Uncaught rule. userX: " + userX + ", userY: " + userY + ", friend: " + friend);   
    	return false;
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean isUserXFriendsListVisibleByUserY(String userX, String userY, boolean friend) {
		
		//if user is requesting own info, they ARE allowed
    	if(userY.equals(userX)) {
    		return true;
    	}
		
		//get privacy record for this user
    	ProfilePrivacy profilePrivacy = getPrivacyRecordForUser(userX);
    	
    	//pass to main
    	return isUserXFriendsListVisibleByUserY(userX, profilePrivacy, userY, friend);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean isUserXFriendsListVisibleByUserY(String userX, ProfilePrivacy profilePrivacy, String userY, boolean friend) {
		
		//if user is requesting own info, they ARE allowed
    	if(userY.equals(userX)) {
    		return true;
    	}
	
		//if no privacy record, return whatever the flag is set as by default
    	/* deprecated by PRFL-86, privacy object will never be null now it will always be default or overridden default
    	if(profilePrivacy == null) {
    		return ProfileConstants.DEFAULT_MYFRIENDS_VISIBILITY;
    	}
    	*/
    	
    	//if restricted to only self, not allowed
    	if(profilePrivacy.getMyFriends() == ProfileConstants.PRIVACY_OPTION_ONLYME) {
    		return false;
    	}
    	
    	//if user is friend and friends are allowed
    	if(friend && profilePrivacy.getMyFriends() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
    		return true;
    	}
    	
    	//if not friend and set to friends only
    	if(!friend && profilePrivacy.getMyFriends() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
    		return false;
    	}
    	
    	//if everyone is allowed
    	if(profilePrivacy.getMyFriends() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
    		return true;
    	}
    	    	
    	//uncaught rule, return false
    	log.error("ProfileLogic.isUserXFriendsListVisibleByUserY. Uncaught rule. userX: " + userX + ", userY: " + userY + ", friend: " + friend);   
    	return false;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean isUserXGalleryVisibleByUser(String userX, ProfilePrivacy profilePrivacy, String userY, boolean friend) {
		
		// current user
    	if(userY.equals(userX)) {
    		return true;
    	}
    	
    	// friend and friends allowed
    	if (friend && profilePrivacy.getMyPictures() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
    		return true;
    	}
    	
    	// everyone else
    	if(profilePrivacy.getMyPictures() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
    		return true;
    	} else {
    		return false;
    	}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean isUserXMessagingEnabledForUserY(String userX, ProfilePrivacy profilePrivacy, String userY, boolean friend) {
		
		// current user
    	if(userY.equals(userX)) {
    		return true;
    	}
    	
    	// friend and friends allowed
    	if (friend && profilePrivacy.getMessages() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
    		return true;
    	}
    	
    	// everyone else
    	if(profilePrivacy.getMessages() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
    		return true;
    	} else {
    		return false;
    	}
	}

	
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean isUserXStatusVisibleByUserY(String userX, String userY, boolean friend) {
		
		//if user is requesting own info, they ARE allowed
    	if(userY.equals(userX)) {
    		return true;
    	}
		
		//get privacy record for this user
    	ProfilePrivacy profilePrivacy = getPrivacyRecordForUser(userX);
    	
    	//pass to main
    	return isUserXStatusVisibleByUserY(userX, profilePrivacy, userY, friend);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean isUserXStatusVisibleByUserY(String userX, ProfilePrivacy profilePrivacy, String userY, boolean friend) {
		
		//if user is requesting own info, they ARE allowed
    	if(userY.equals(userX)) {
    		return true;
    	}
    	
		//if no privacy record, return whatever the flag is set as by default
    	/* deprecated by PRFL-86, privacy object will never be null now it will always be default or overridden default
    	if(profilePrivacy == null) {
    		return ProfileConstants.DEFAULT_MYSTATUS_VISIBILITY;
    	}
    	*/
    	
    	//if restricted to only self, not allowed
    	/* DEPRECATED via PRFL-24 when the privacy settings were relaxed
    	if(profilePrivacy.getMyStatus() == ProfilePrivacyManager.PRIVACY_OPTION_ONLYME) {
    		return false;
    	}
    	*/
    	
    	//if user is friend and friends are allowed
    	if(friend && profilePrivacy.getMyStatus() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
    		return true;
    	}
    	
    	//if not friend and set to friends only
    	if(!friend && profilePrivacy.getMyStatus() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
    		return false;
    	}
    	
    	//if everyone is allowed
    	if(profilePrivacy.getMyStatus() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
    		return true;
    	}
    	    	
    	//uncaught rule, return false
    	log.error("ProfileLogic.isUserXStatusVisibleByUserY. Uncaught rule. userX: " + userX + ", userY: " + userY + ", friend: " + friend);   
    	return false;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean isUserXKudosVisibleByUserY(String userX, String userY, boolean friend) {
		
		//if user is requesting own info, they ARE allowed
    	if(userY.equals(userX)) {
    		return true;
    	}
		
		//get privacy record for this user
    	ProfilePrivacy profilePrivacy = getPrivacyRecordForUser(userX);
    	
    	//pass to main
    	return isUserXKudosVisibleByUserY(userX, profilePrivacy, userY, friend);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean isUserXKudosVisibleByUserY(String userX, ProfilePrivacy profilePrivacy, String userY, boolean friend) {
		
		//if user is requesting own info, they ARE allowed
    	if(userY.equals(userX)) {
    		return true;
    	}
    	
    	//if user is friend and friends are allowed
    	if(friend && profilePrivacy.getMyKudos() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
    		return true;
    	}
    	
    	//if not friend and set to friends only
    	if(!friend && profilePrivacy.getMyKudos() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
    		return false;
    	}
    	
    	//if everyone is allowed
    	if(profilePrivacy.getMyKudos() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
    		return true;
    	}
    	    	
    	//uncaught rule, return false
    	log.error("ProfileLogic.isUserXKudosVisibleByUserY. Uncaught rule. userX: " + userX + ", userY: " + userY + ", friend: " + friend);   
    	return false;
	}
	

	/**
 	 * {@inheritDoc}
 	 */
	public boolean isBirthYearVisible(String userId) {
		
		//get privacy record for this user
		ProfilePrivacy profilePrivacy = getPrivacyRecordForUser(userId);
		
		return isBirthYearVisible(profilePrivacy);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean isBirthYearVisible(ProfilePrivacy profilePrivacy) {
		
		//return value or whatever the flag is set as by default
		/* deprecated by PRFL-86, privacy object will never be null now it will always be default or overridden default
    	if(profilePrivacy == null) {
    		return ProfileConstants.DEFAULT_BIRTHYEAR_VISIBILITY;
    	} else {
    	}
    	*/
    	return profilePrivacy.isShowBirthYear();
	}

	
	
	/**
 	 * {@inheritDoc}
 	 */
	public ProfilePreferences getPreferencesRecordForUser(final String userId) {
		
		if(userId == null){
	  		throw new IllegalArgumentException("Null argument in ProfileLogic.getPreferencesRecordForUser"); 
	  	}
		
		//will stay null if we can't get or create a record
		ProfilePreferences prefs = null;
		prefs = dao.getPreferencesRecordForUser(userId);
		
		if(prefs == null) {
			prefs = dao.addNewPreferencesRecord(getDefaultPreferencesRecord(userId));
			if(prefs != null) {
				sakaiProxy.postEvent(ProfileConstants.EVENT_PREFERENCES_NEW, "/profile/"+userId, true);
				log.info("Created default preferences record for user: " + userId); 
			}
		}
		
		if(prefs != null) {
			//decrypt password and set into field
			prefs.setTwitterPasswordDecrypted(ProfileUtils.decrypt(prefs.getTwitterPasswordEncrypted()));

			//if owner, decrypt the password, otherwise, remove it entirely
			String currentUserUuid = sakaiProxy.getCurrentUserId();
			if(StringUtils.equals(userId, currentUserUuid)){
				prefs.setTwitterPasswordDecrypted(ProfileUtils.decrypt(prefs.getTwitterPasswordEncrypted()));
			} else {
				prefs.setTwitterPasswordEncrypted(null);
				prefs.setTwitterPasswordDecrypted(null);
			}
		}
		
		return prefs;
	}
	
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean savePreferencesRecord(ProfilePreferences prefs) {
		
		//validate fields are set and encrypt password if necessary, else clear them all
		if(checkTwitterFields(prefs)) {
			prefs.setTwitterPasswordEncrypted(ProfileUtils.encrypt(prefs.getTwitterPasswordDecrypted()));
		} else {
			prefs.setTwitterEnabled(false);
			prefs.setTwitterUsername(null);
			prefs.setTwitterPasswordDecrypted(null);
			prefs.setTwitterPasswordEncrypted(null);
		}
		
		if(dao.savePreferencesRecord(prefs)){
			log.info("Updated preferences record for user: " + prefs.getUserUuid()); 
			return true;
		} 
		
		return false;
	}
	
	
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean isTwitterIntegrationEnabledForUser(final String userId) {
		
		//check global settings
		if(!sakaiProxy.isTwitterIntegrationEnabledGlobally()) {
			return false;
		}
		
		//check own preferences
		ProfilePreferences profilePreferences = getPreferencesRecordForUser(userId);
		if(profilePreferences == null) {
			return false;
		}
		
		if(profilePreferences.isTwitterEnabled()) {
			return true;
		}
		
		return false;
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean isTwitterIntegrationEnabledForUser(ProfilePreferences prefs) {
		
		//check global settings
		if(!sakaiProxy.isTwitterIntegrationEnabledGlobally()) {
			return false;
		}
		
		//check own prefs
		if(prefs == null) {
			return false;
		}
		
		return prefs.isTwitterEnabled();
	}
	
	
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public void sendMessageToTwitter(final String userId, final String message){
		//setup class thread to call later
		class TwitterUpdater implements Runnable{
			private Thread runner;
			private String username;
			private String password;
			private String message;

			public TwitterUpdater(String username, String password, String message) {
				this.username=username;
				this.password=password;
				this.message=message;
				
				runner = new Thread(this,"Profile2 TwitterUpdater thread"); 
				runner.start();
			}
			

			//do it!
			public synchronized void run() {
				
				Twitter twitter = new Twitter(username, password);
				
				try {
					twitter.setSource(sakaiProxy.getTwitterSource());
					twitter.update(message);
					log.info("Twitter status updated for: " + userId); 
				}
				catch (Exception e) {
					log.error("ProfileLogic.sendMessageToTwitter() failed. " + e.getClass() + ": " + e.getMessage());  
				}
			}
		}
		
		//get preferences for this user
		ProfilePreferences profilePreferences = getPreferencesRecordForUser(userId);
		
		if(profilePreferences == null) {
			return;
		}
		//get details
		String username = profilePreferences.getTwitterUsername();
		String password = profilePreferences.getTwitterPasswordDecrypted();
		
		//instantiate class to send the data
		new TwitterUpdater(username, password, message);
		
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean validateTwitterCredentials(final String twitterUsername, final String twitterPassword) {
		
		if(StringUtils.isNotBlank(twitterUsername) && StringUtils.isNotBlank(twitterPassword)) {
			Twitter twitter = new Twitter(twitterUsername, twitterPassword);
			if(twitter.verifyCredentials()) {
				return true;
			}
		}
		return false;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean validateTwitterCredentials(ProfilePreferences prefs) {
		
		String twitterUsername = prefs.getTwitterUsername();
		String twitterPassword = prefs.getTwitterPasswordDecrypted();
		return validateTwitterCredentials(twitterUsername, twitterPassword);
	}

	
		
	/**
 	 * {@inheritDoc}
 	 */
	/*
	public String generateTinyUrl(final String url) {
		return tinyUrlService.generateTinyUrl(url);
	}
	*/
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean isEmailEnabledForThisMessageType(final String userId, final int messageType) {
		
		//get preferences record for this user
    	ProfilePreferences profilePreferences = getPreferencesRecordForUser(userId);
    	
    	//if none, return whatever the flag is set as by default
    	if(profilePreferences == null) {
    		return ProfileConstants.DEFAULT_EMAIL_NOTIFICATION_SETTING;
    	}
    	
    	//if its a request and requests enabled, true
    	if(messageType == ProfileConstants.EMAIL_NOTIFICATION_REQUEST && profilePreferences.isRequestEmailEnabled()) {
    		return true;
    	}
    	
    	//if its a confirm and confirms enabled, true
    	if(messageType == ProfileConstants.EMAIL_NOTIFICATION_CONFIRM && profilePreferences.isConfirmEmailEnabled()) {
    		return true;
    	}
    	
    	//if its a new message and new messages enabled, true
    	if(messageType == ProfileConstants.EMAIL_NOTIFICATION_MESSAGE_NEW && profilePreferences.isMessageNewEmailEnabled()) {
    		return true;
    	}
    	
    	//if its a reply to a message message and replies enabled, true
    	if(messageType == ProfileConstants.EMAIL_NOTIFICATION_MESSAGE_REPLY && profilePreferences.isMessageReplyEmailEnabled()) {
    		return true;
    	}
    	
    	//add more cases here as need progresses
    	
    	//no notification for this message type, return false 	
    	log.debug("ProfileLogic.isEmailEnabledForThisMessageType. False for userId: " + userId + ", messageType: " + messageType);  

    	return false;
		
	}

	
	
	
	
	
	
	
		
	
	

	/**
 	 * {@inheritDoc}
 	 */
	public int getAllUnreadMessagesCount(final String userId) {
		return dao.getAllUnreadMessagesCount(userId);
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public int getThreadsWithUnreadMessagesCount(final String userId) {
		return dao.getThreadsWithUnreadMessagesCount(userId);
	}
	
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean sendNewMessage(final String uuidTo, final String uuidFrom, final String threadId, final String subject, final String messageStr) {
		
		//setup thread
		MessageThread thread = new MessageThread();
		thread.setId(threadId);
		
		if(StringUtils.isBlank(subject)) {
			thread.setSubject(ProfileConstants.DEFAULT_PRIVATE_MESSAGE_SUBJECT);
		} else {
			thread.setSubject(subject);
		}
		
		//setup message
		Message message = new Message();
		message.setId(ProfileUtils.generateUuid());
		message.setFrom(uuidFrom);
		message.setMessage(messageStr);
		message.setDatePosted(new Date());
		message.setThread(thread.getId());
		//saveNewMessage(message);
		
		
		//setup participants
		//at present we have one for the receipient and one for sender.
		//in future we may have multiple recipients and will need to check the existing list of thread participants 
		List<String> threadParticipants = new ArrayList<String>();
		threadParticipants.add(uuidTo);
		threadParticipants.add(uuidFrom);

		List<MessageParticipant> participants = new ArrayList<MessageParticipant>();
		for(String threadParticipant : threadParticipants){
			MessageParticipant p = new MessageParticipant();
			p.setMessageId(message.getId());
			p.setUuid(threadParticipant);
			if(StringUtils.equals(threadParticipant, message.getFrom())) {
				p.setRead(true); //sender 
			} else {
				p.setRead(false);
			}
			p.setDeleted(false);
			
			participants.add(p);
		}
		
		if(saveAllNewMessageParts(thread, message, participants)) {
			sendMessageEmailNotification(threadParticipants, uuidFrom, threadId, subject, messageStr, ProfileConstants.EMAIL_NOTIFICATION_MESSAGE_NEW);
			
			return true;
		}
		return false;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public Message replyToThread(final String threadId, final String reply, final String uuidFrom) {
		
		try {
			
			//create the message and save it
			Message message = new Message();
			message.setId(ProfileUtils.generateUuid());
			message.setFrom(uuidFrom);
			message.setMessage(reply);
			message.setDatePosted(new Date());
			message.setThread(threadId);
			dao.saveNewMessage(message);
			
			//get the thread subject
			String subject = getMessageThread(threadId).getSubject();
			
			//get a unique list of participants in this thread, and save a record for each participant for this new message
			List<String> uuids = getThreadParticipants(threadId);
			for(String uuidTo : uuids) {
				MessageParticipant participant = getDefaultMessageParticipantRecord(message.getId(), uuidTo);
				if(StringUtils.equals(uuidFrom, uuidTo)) {
					participant.setRead(true); //sender 
				} 
				
				dao.saveNewMessageParticipant(participant);
			}
			
			//send email notifications
			sendMessageEmailNotification(uuids, uuidFrom, threadId, subject, reply, ProfileConstants.EMAIL_NOTIFICATION_MESSAGE_REPLY);
			
			return message;
		} catch (Exception e) {
			log.error("ProfileLogic.replyToThread(): Couldn't send reply: " + e.getClass() + " : " + e.getMessage());
		}
		
		return null;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<MessageThread> getMessageThreads(final String userId) {
		
		List<MessageThread> threads = dao.getMessageThreads(userId);
	  	
	  	//get latest message for each thread
	  	for(MessageThread thread : threads) {
	  		thread.setMostRecentMessage(dao.getLatestMessageInThread(thread.getId()));
	  	}
	  	
	  	return threads;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public int getMessageThreadsCount(final String userId) {
		return dao.getMessageThreadsCount(userId);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<Message> getMessagesInThread(final String threadId) {
		return dao.getMessagesInThread(threadId);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public int getMessagesInThreadCount(final String threadId) {
		return dao.getMessagesInThreadCount(threadId);
	}
	
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public Message getMessage(final String id) {
		return dao.getMessage(id);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public MessageThread getMessageThread(final String threadId) {
		
		MessageThread thread = dao.getMessageThread(threadId);
		if(thread == null){
			return null;
		}
		
		//add the latest message for this thread
		thread.setMostRecentMessage(dao.getLatestMessageInThread(threadId));
		
		return thread;
	}
	
	


	/**
 	 * {@inheritDoc}
 	 */
	public boolean toggleMessageRead(MessageParticipant participant, final boolean status) {
		return dao.toggleMessageRead(participant, status);
	}
	

	/**
 	 * {@inheritDoc}
 	 */
	/*
	public boolean toggleAllMessagesInThreadAsRead(final String threadId, final String userUuid, final boolean read) {
		// TODO Auto-generated method stub
		return false;
	}
	*/
	
	/**
 	 * {@inheritDoc}
 	 */
	public MessageParticipant getMessageParticipant(final String messageId, final String userUuid) {
		return dao.getMessageParticipant(messageId, userUuid);
	}

	
	
	/**
	 * Create a default MessageParticipant object for a message and user. This is so they can mark messages as unread/delete them. Not persisted until actioned.
	 * @param messageId
	 * @param userUuid
	 * @return
	 */
	private MessageParticipant getDefaultMessageParticipantRecord(final String messageId, final String userUuid) {
		
		MessageParticipant participant = new MessageParticipant();
		participant.setMessageId(messageId);
		participant.setUuid(userUuid);
		participant.setRead(false);
		participant.setDeleted(false);
		
		return participant;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<String> getThreadParticipants(final String threadId) {
		return dao.getThreadParticipants(threadId);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean isThreadParticipant(final String threadId, final String userId) {
		return getThreadParticipants(threadId).contains(userId);
	}

	
	/**
 	 * {@inheritDoc}
 	 */
	public void sendMessageEmailNotification(final List<String> toUuids, final String fromUuid, final String directId, final String subject, final String messageStr, final int messageType) {
		
		//is email notification enabled for this message type? Reformat the recipient list
		for(Iterator<String> it = toUuids.iterator(); it.hasNext();) {
			if(!isEmailEnabledForThisMessageType(it.next(), messageType)) {
				it.remove();
			}
		}
		
		//the sender is a message participant but we don't want email confirmations for them, so remove
		toUuids.remove(fromUuid);
		
		//new message
		if(messageType == ProfileConstants.EMAIL_NOTIFICATION_MESSAGE_NEW) {
			
			String emailTemplateKey = ProfileConstants.EMAIL_TEMPLATE_KEY_MESSAGE_NEW;
			
			//create the map of replacement values for this email template
			Map<String,String> replacementValues = new HashMap<String,String>();
			replacementValues.put("senderDisplayName", sakaiProxy.getUserDisplayName(fromUuid));
			replacementValues.put("localSakaiName", sakaiProxy.getServiceName());
			replacementValues.put("messageSubject", subject);
			replacementValues.put("messageBody", messageStr);
			replacementValues.put("messageLink", linkLogic.getEntityLinkToProfileMessages(directId));
			replacementValues.put("localSakaiUrl", sakaiProxy.getPortalUrl());
			replacementValues.put("toolName", sakaiProxy.getCurrentToolTitle());

			sakaiProxy.sendEmail(toUuids, emailTemplateKey, replacementValues);
			return;
		} 
		
		//reply
		if (messageType == ProfileConstants.EMAIL_NOTIFICATION_MESSAGE_REPLY) {
				
			String emailTemplateKey = ProfileConstants.EMAIL_TEMPLATE_KEY_MESSAGE_REPLY;
			
			//create the map of replacement values for this email template
			Map<String,String> replacementValues = new HashMap<String,String>();
			replacementValues.put("senderDisplayName", sakaiProxy.getUserDisplayName(fromUuid));
			replacementValues.put("localSakaiName", sakaiProxy.getServiceName());
			replacementValues.put("messageSubject", subject);
			replacementValues.put("messageBody", messageStr);
			replacementValues.put("messageLink", linkLogic.getEntityLinkToProfileMessages(directId));
			replacementValues.put("localSakaiUrl", sakaiProxy.getPortalUrl());
			replacementValues.put("toolName", sakaiProxy.getCurrentToolTitle());

			sakaiProxy.sendEmail(toUuids, emailTemplateKey, replacementValues);
			return;
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public void sendConnectionEmailNotification(String toUuid, final String fromUuid, final int messageType) {
		//check if email preference enabled
		if(!isEmailEnabledForThisMessageType(toUuid, messageType)) {
			return;
		}
		
		//request
		if(messageType == ProfileConstants.EMAIL_NOTIFICATION_REQUEST) {
			
			String emailTemplateKey = ProfileConstants.EMAIL_TEMPLATE_KEY_CONNECTION_REQUEST;
			
			//create the map of replacement values for this email template
			Map<String,String> replacementValues = new HashMap<String,String>();
			replacementValues.put("senderDisplayName", sakaiProxy.getUserDisplayName(fromUuid));
			replacementValues.put("localSakaiName", sakaiProxy.getServiceName());
			replacementValues.put("connectionLink", linkLogic.getEntityLinkToProfileConnections());
			replacementValues.put("localSakaiUrl", sakaiProxy.getPortalUrl());
			replacementValues.put("toolName", sakaiProxy.getCurrentToolTitle());

			sakaiProxy.sendEmail(toUuid, emailTemplateKey, replacementValues);
			return;
		}
		
		//confirm
		if(messageType == ProfileConstants.EMAIL_NOTIFICATION_CONFIRM) {
			
			String emailTemplateKey = ProfileConstants.EMAIL_TEMPLATE_KEY_CONNECTION_CONFIRM;
			
			//create the map of replacement values for this email template
			Map<String,String> replacementValues = new HashMap<String,String>();
			replacementValues.put("senderDisplayName", sakaiProxy.getUserDisplayName(fromUuid));
			replacementValues.put("localSakaiName", sakaiProxy.getServiceName());
			replacementValues.put("connectionLink", linkLogic.getEntityLinkToProfileHome(fromUuid));
			replacementValues.put("localSakaiUrl", sakaiProxy.getPortalUrl());
			replacementValues.put("toolName", sakaiProxy.getCurrentToolTitle());

			sakaiProxy.sendEmail(toUuid, emailTemplateKey, replacementValues);
			return;
		}
		
	}


	
	
	
	
	
		
	
	
	/**
 	 * {@inheritDoc}
 	 */
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
		p.setPreferences(getPreferencesRecordForUser(userUuid));
		p.setPrivacy(getPrivacyRecordForUser(userUuid));
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
 	 * {@inheritDoc}
 	 */
	public BigDecimal getKudos(String userUuid){
		ProfileKudos k = dao.getKudos(userUuid);
		if(k == null){
			return null;
		}
		return k.getKudos();
	}

	/**
 	 * {@inheritDoc}
 	 */
	public boolean updateKudos(String userUuid, BigDecimal score) {
		ProfileKudos k = new ProfileKudos();
		k.setUserUuid(userUuid);
		k.setKudos(score);
		k.setDateAdded(new Date());
		
		return dao.updateKudos(k);
	}
	
	
	// helper method to check if all required twitter fields are set properly
	private boolean checkTwitterFields(ProfilePreferences prefs) {
		return (prefs.isTwitterEnabled() &&
				StringUtils.isNotBlank(prefs.getTwitterUsername()) &&
				StringUtils.isNotBlank(prefs.getTwitterPasswordDecrypted()));
	}
	
	
	
	/*
	 * helper method to save a message once all parts have been created. takes care of rollbacks incase of failure (TODO)
	 */
	private boolean saveAllNewMessageParts(MessageThread thread, Message message, List<MessageParticipant> participants) {
		dao.saveNewThread(thread);
		dao.saveNewMessage(message);
		dao.saveNewMessageParticipants(participants);

		return true;
	}
	
	
	/**
	 * Create a privacy record according to the defaults. 
	 *
	 * @param userId		uuid of the user to create the record for
	 */
	private ProfilePrivacy getDefaultPrivacyRecord(String userId) {
		
		//get the overriden privacy settings. they'll be defaults if not specified
		HashMap<String, Object> props = sakaiProxy.getOverriddenPrivacySettings();	
		
		//using the props, set them into the ProfilePrivacy object
		ProfilePrivacy privacy = new ProfilePrivacy();
		privacy.setUserUuid(userId);
		privacy.setProfileImage((Integer)props.get("profileImage"));
		privacy.setBasicInfo((Integer)props.get("basicInfo"));
		privacy.setContactInfo((Integer)props.get("contactInfo"));
		privacy.setStaffInfo((Integer)props.get("staffInfo"));
		privacy.setStudentInfo((Integer)props.get("studentInfo"));
		privacy.setPersonalInfo((Integer)props.get("personalInfo"));
		privacy.setShowBirthYear((Boolean)props.get("birthYear"));
		privacy.setMyFriends((Integer)props.get("myFriends"));
		privacy.setMyStatus((Integer)props.get("myStatus"));
		privacy.setMyPictures((Integer)props.get("myPictures"));
		privacy.setMessages((Integer)props.get("messages"));
		privacy.setBusinessInfo((Integer)props.get("businessInfo"));
		privacy.setMyKudos((Integer)props.get("myKudos"));
		
		return privacy;
	}
	
	/**
	 * Create a preferences record according to the defaults. 
	 *
	 * @param userId		uuid of the user to create the record for
	 */
	private ProfilePreferences getDefaultPreferencesRecord(final String userId) {
		
		ProfilePreferences prefs = new ProfilePreferences();
		prefs.setUserUuid(userId);
		prefs.setRequestEmailEnabled(ProfileConstants.DEFAULT_EMAIL_REQUEST_SETTING);
		prefs.setConfirmEmailEnabled(ProfileConstants.DEFAULT_EMAIL_CONFIRM_SETTING);
		prefs.setMessageNewEmailEnabled(ProfileConstants.DEFAULT_EMAIL_MESSAGE_NEW_SETTING);
		prefs.setMessageReplyEmailEnabled(ProfileConstants.DEFAULT_EMAIL_MESSAGE_REPLY_SETTING);
		prefs.setTwitterEnabled(ProfileConstants.DEFAULT_TWITTER_SETTING);
		prefs.setUseOfficialImage(ProfileConstants.DEFAULT_OFFICIAL_IMAGE_SETTING);
		prefs.setShowKudos(ProfileConstants.DEFAULT_SHOW_KUDOS_SETTING);
				
		return prefs;
	}
	
	/**
	 * Does this user have an uplaoded profile image?
	 * Calls getCurrentProfileImageRecord to see if a record exists.
	 * 
	 * This is mainly used by the convertProfile() method, but could have another use.
	 * 
	 * @param userId 		the uuid of the user we are querying
	 * @return boolean		true if it exists/false if not
	 */
	private boolean hasUploadedProfileImage(String userId) {
		
		//get record from db
		ProfileImageUploaded record = dao.getCurrentProfileImageRecord(userId);
		
		if(record == null) {
			return false;
		}
		return true;
	}
	
	/**
	 * Does this user have an external profile image?
	 * 
	 * @param userId 		the uuid of the user we are querying
	 * @return boolean		true if it exists/false if not
	 */
	private boolean hasExternalProfileImage(String userId) {
		
		//get record from db
		ProfileImageExternal record = dao.getExternalImageRecordForUser(userId);
		
		if(record == null) {
			return false;
		}
		return true;
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
	

	/**
	 * Get the entity url to a profile image for a user.
	 *  
	 * It can be added to any profile without checks as the retrieval of the image does the checks, and a default image
	 * is used if not allowed or none available.
	 * 
	 * @param userUuid	uuid for the user
	 * @param size		size of image, from ProfileConstants
	 */
	private String getProfileImageEntityUrl(String userUuid, int size) {
	
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
	
	
	//setup SakaiProxy API
	private SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}
	
	//setup DAO
	private ProfileDao dao;
	public void setDao(ProfileDao dao) {
		this.dao = dao;
	}
	
	//setup LinkLogic API
	private ProfileLinkLogic linkLogic;
	public void setLinkLogic(ProfileLinkLogic linkLogic) {
		this.linkLogic = linkLogic;
	}
	



	
	//setup TinyUrlService API
	/*
	private TinyUrlService tinyUrlService;
	public void setTinyUrlService(TinyUrlService tinyUrlService) {
		this.tinyUrlService = tinyUrlService;
	}
	*/
	
	
	
}
