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
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.sakaiproject.profile2.dao.ProfileDao;
import org.sakaiproject.profile2.model.CompanyProfile;
import org.sakaiproject.profile2.model.GalleryImage;
import org.sakaiproject.profile2.model.Message;
import org.sakaiproject.profile2.model.MessageParticipant;
import org.sakaiproject.profile2.model.MessageThread;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.model.ProfileFriend;
import org.sakaiproject.profile2.model.ProfileImage;
import org.sakaiproject.profile2.model.ProfileImageExternal;
import org.sakaiproject.profile2.model.ProfileImageOfficial;
import org.sakaiproject.profile2.model.ProfilePreferences;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.model.ProfileStatus;
import org.sakaiproject.profile2.model.ResourceWrapper;
import org.sakaiproject.profile2.model.SearchResult;
import org.sakaiproject.profile2.model.SocialNetworkingInfo;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;
import org.sakaiproject.user.api.User;

import twitter4j.Twitter;

/**
 * This is an internal Profile2 API Implementation to be used by the Profile2 tool only. 
 * 
 * <p>DO NOT USE THIS YOURSELF, use the ProfileServices instead.</p>
 * 
 * <p>If there are methods here that do not have an appropriate exposure in the service APIs, please file a JIRA at <a href="http://jira.sakaiproject.org/browse/PRFL">http://jira.sakaiproject.org/browse/PRFL</a></p>
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class ProfileLogicImpl implements ProfileLogic {

	private static final Logger log = Logger.getLogger(ProfileLogicImpl.class);

	
	/**
 	 * {@inheritDoc}
 	 */
	public List<Person> getConnectionsForUser(String userId) {
		
		List<User> users = new ArrayList<User>();
		List<Person> connections = new ArrayList<Person>();
		users = sakaiProxy.getUsers(dao.getConfirmedConnectionUserIdsForUser(userId));
		
		for(User u: users) {
			Person p = new Person();
			p.setUuid(u.getId());
			p.setDisplayName(u.getDisplayName());
			p.setType(u.getType());
				
			connections.add(p);
		}
		
		return connections;
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */	
	public int getCountConnectionsForUser(final String userId) {
		return getConnectionsForUser(userId).size();
	}
	
	/**
 	 * {@inheritDoc}
 	 */	
	public List<Person> getConnectionRequestsForUser(final String userId) {
		
		List<User> users = new ArrayList<User>();
		List<Person> requests = new ArrayList<Person>();
		users = sakaiProxy.getUsers(dao.getRequestedConnectionUserIdsForUser(userId));
		
		for(User u: users) {
			Person p = new Person();
			p.setUuid(u.getId());
			p.setDisplayName(u.getDisplayName());
			p.setType(u.getType());
				
			requests.add(p);
		}
		
		return requests;
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
	public ProfileStatus getUserStatus(final String userId) {
		
		if(userId == null){
	  		throw new IllegalArgumentException("Null argument in ProfileLogic.getUserStatus"); 
	  	}
		
		// compute oldest date for status 
		Calendar cal = Calendar.getInstance(); 
		cal.add(Calendar.DAY_OF_YEAR, -7); 
		final Date oldestStatusDate = cal.getTime(); 
		
		return dao.getUserStatus(userId, oldestStatusDate);
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
	public boolean addNewGalleryImage(final GalleryImage galleryImage) {
		
		if(dao.addNewGalleryImage(galleryImage)){
			log.info("Added new gallery image for user: " + galleryImage.getUserUuid()); 
			return true;
		} 
		
		return false;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<GalleryImage> getGalleryImages(final String userId) {
		return dao.getGalleryImages(userId);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean removeGalleryImage(String userId, long imageId) {
		if(userId == null || new Long(imageId) == null){
	  		throw new IllegalArgumentException("Null argument in ProfileLogicImpl.removeGalleryImage()"); 
	  	}
		
		GalleryImage galleryImage = dao.getGalleryImageRecord(userId, imageId);
		
		if(galleryImage == null){
			log.error("GalleryImage record does not exist for userId: " + userId + ", imageId: " + imageId);
			return false;
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
	public boolean addNewProfileImage(final String userId, final String mainResource, final String thumbnailResource) {
		
		ProfileImage profileImage = new ProfileImage(userId, mainResource, thumbnailResource, true);
		if(dao.addNewProfileImage(profileImage)){
			log.info("Added a new profile image for user: " + userId);
			return true;
		}
		
		return false;
	}


	/**
 	 * {@inheritDoc}
 	 */
	public List<SearchResult> findUsersByNameOrEmail(String search, String userId) {
		
		List<User> users = new ArrayList<User>();
		List<String> sakaiPersonUuids = new ArrayList<String>();
		
		//add users from SakaiPerson
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
		
		//format into SearchResult records (based on friend status, privacy status etc)
		List<SearchResult> results = new ArrayList<SearchResult>(createSearchResultRecordsFromSearch(users, userId));
		
		return results;
		
	}
	
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<SearchResult> findUsersByInterest(String search, String userId) {
		
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
		
		//format into SearchResult records (based on friend status, privacy status etc)
		List<SearchResult> results = new ArrayList<SearchResult>(createSearchResultRecordsFromSearch(users, userId));
		
		return results;
		
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
	public boolean isUserXVisibleInSearchesByUserY(String userX, String userY, boolean friend) {
				
		//if user is requesting own info, they ARE allowed
    	if(userY.equals(userX)) {
    		return true;
    	}
		
		//get privacy record for userX
    	ProfilePrivacy profilePrivacy = getPrivacyRecordForUser(userX);
    	
    	//pass to main
    	return isUserXVisibleInSearchesByUserY(userX, profilePrivacy, userY, friend);
	}
	
	

	/**
 	 * {@inheritDoc}
 	 */
	public boolean isUserXVisibleInSearchesByUserY(String userX, ProfilePrivacy profilePrivacy, String userY, boolean friend) {
		
		//if user is requesting own info, they ARE allowed
    	if(userY.equals(userX)) {
    		return true;
    	}
		
		//if no privacy record, return whatever the flag is set as by default
    	/* deprecated by PRFL-86, privacy object will never be null now it will always be default or overridden default
    	if(profilePrivacy == null) {
    		return ProfileConstants.SELF_SEARCH_VISIBILITY;
    	}
    	*/
    	
    	//if restricted to only self, not allowed
    	/* DEPRECATED via PRFL-24 when the privacy settings were relaxed
    	if(profilePrivacy.getProfile() == ProfilePrivacyManager.PRIVACY_OPTION_ONLYME) {
    		return false;
    	}
    	*/
		
    	//if friend and set to friends only
    	if(friend && profilePrivacy.getSearch() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
    		return true;
    	}
    	
    	//if not friend and set to friends only
    	if(!friend && profilePrivacy.getSearch() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
    		return false;
    	}
    	
    	//if everyone is allowed
    	if(profilePrivacy.getSearch() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
    		return true;
    	}
    	
    	//uncaught rule, return false
    	log.error("ProfileLogic.isUserXVisibleInSearchesByUserY. Uncaught rule. userX: " + userX + ", userY: " + userY + ", friend: " + friend);   
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
	public byte[] getCurrentProfileImageForUser(String userId, int imageType) {
		
		byte[] image = null;
		
		//get record from db
		ProfileImage profileImage = dao.getCurrentProfileImageRecord(userId);
		
		if(profileImage == null) {
			log.debug("ProfileLogic.getCurrentProfileImageForUser() null for userId: " + userId);
			return null;
		}
		
		//get main image
		if(imageType == ProfileConstants.PROFILE_IMAGE_MAIN) {
			image = sakaiProxy.getResource(profileImage.getMainResource());
		}
		
		//or get thumbnail
		if(imageType == ProfileConstants.PROFILE_IMAGE_THUMBNAIL) {
			image = sakaiProxy.getResource(profileImage.getThumbnailResource());
			if(image == null) {
				image = sakaiProxy.getResource(profileImage.getMainResource());
			}
		}
		
		return image;
	}

	/**
 	 * {@inheritDoc}
 	 */
	public ResourceWrapper getCurrentProfileImageForUserWrapped(String userId, int imageType) {
		
		ResourceWrapper resource = new ResourceWrapper();
		
		//get record from db
		ProfileImage profileImage = dao.getCurrentProfileImageRecord(userId);
		
		if(profileImage == null) {
			log.debug("ProfileLogic.getCurrentProfileImageForUserWrapped() null for userId: " + userId);
			return null;
		}
		
		//get main image
		if(imageType == ProfileConstants.PROFILE_IMAGE_MAIN) {
			resource = sakaiProxy.getResourceWrapped(profileImage.getMainResource());
		}
		
		//or get thumbnail
		if(imageType == ProfileConstants.PROFILE_IMAGE_THUMBNAIL) {
			resource = sakaiProxy.getResourceWrapped(profileImage.getThumbnailResource());
			if(resource == null) {
				resource = sakaiProxy.getResourceWrapped(profileImage.getMainResource());
			}
		}
	
		return resource;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean hasUploadedProfileImage(String userId) {
		
		//get record from db
		ProfileImage record = dao.getCurrentProfileImageRecord(userId);
		
		if(record == null) {
			return false;
		}
		return true;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean hasExternalProfileImage(String userId) {
		
		//get record from db
		ProfileImageExternal record = dao.getExternalImageRecordForUser(userId);
		
		if(record == null) {
			return false;
		}
		return true;
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
	public String getExternalImageUrl(final String userId, final int imageType) {
		
		//get external image record for this user
		ProfileImageExternal externalImage = dao.getExternalImageRecordForUser(userId);
		
		//if none, return null
    	if(externalImage == null) {
    		return null;
    	}
    	
    	//else return the url for the type they requested
    	if(imageType == ProfileConstants.PROFILE_IMAGE_MAIN) {
    		String url = externalImage.getMainUrl();
    		if(StringUtils.isBlank(url)) {
    			return null;
    		}
    		return url;
    	}
    	
    	if(imageType == ProfileConstants.PROFILE_IMAGE_THUMBNAIL) {
    		String url = externalImage.getThumbnailUrl();
    		if(StringUtils.isBlank(url)) {
    			url = externalImage.getMainUrl();
    			if(StringUtils.isBlank(url)) {
    				return null;
    			}
    			return url;
    		}
    		return url;
    	}
    	
    	//no url	
    	log.error("ProfileLogic.getExternalImageUrl. No URL for userId: " + userId + ", imageType: " + imageType);  

    	return null;
		
	}

	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean saveExternalImage(final String userId, final String mainUrl, final String thumbnailUrl) {
		
		ProfileImageExternal externalImage = new ProfileImageExternal(userId, mainUrl, thumbnailUrl);
		
		if(dao.saveExternalImage(externalImage)) {
			log.info("Updated external image record for user: " + userId); 
			return true;
		} 
		
		return false;
	}

	
		
	/**
 	 * {@inheritDoc}
 	 * 
 	 * TODO: GET RID OF THIS. The new ProfileImageRenderer will be able to render URLs as is.
 	 */
	public ResourceWrapper getURLResourceAsBytes(String url) {
		
		ResourceWrapper wrapper = new ResourceWrapper();
		
		try {
			URL u = new URL(url);
			
			URLConnection uc = u.openConnection();
			int contentLength = uc.getContentLength();
			String contentType = uc.getContentType();
			uc.setReadTimeout(5000); //timeout of 5 sec just to be on the safe side.
			
			InputStream in = new BufferedInputStream(uc.getInputStream());
			byte[] data = new byte[contentLength];
			
			int bytes = 0;
			int offset = 0;
		      
			while (offset < contentLength) {
				bytes = in.read(data, offset, data.length - offset);
				if (bytes == -1) {
					break;
				}
				offset += bytes;
			}
			in.close();
			
			if (offset != contentLength) {
				throw new IOException("Only read " + offset + " bytes; Expected " + contentLength + " bytes.");
			}
			
			wrapper.setBytes(data);
			wrapper.setMimeType(contentType);
			wrapper.setLength(contentLength);
			wrapper.setExternal(true);
			wrapper.setResourceID(url);
			
		} catch (Exception e) {
			log.error("Failed to retrieve resource: " + e.getClass() + ": " + e.getMessage());
		} 
		return wrapper;
	}

	/**
 	 * {@inheritDoc}
 	 */
	public String getUnavailableImageURL() {
		StringBuilder path = new StringBuilder();
		path.append(sakaiProxy.getServerUrl());
		path.append(ProfileConstants.UNAVAILABLE_IMAGE_FULL);
		return path.toString();
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
			replacementValues.put("messageLink", getEntityLinkToProfileMessages(directId));
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
			replacementValues.put("messageLink", getEntityLinkToProfileMessages(directId));
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
			replacementValues.put("connectionLink", getEntityLinkToProfileConnections());
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
			replacementValues.put("connectionLink", getEntityLinkToProfileHome(fromUuid));
			replacementValues.put("localSakaiUrl", sakaiProxy.getPortalUrl());
			replacementValues.put("toolName", sakaiProxy.getCurrentToolTitle());

			sakaiProxy.sendEmail(toUuid, emailTemplateKey, replacementValues);
			return;
		}
		
	}


	
	/**
 	 * {@inheritDoc}
 	 */
	public String getEntityLinkToProfileHome(final String userUuid) {
		StringBuilder url = new StringBuilder();
		url.append(getEntityLinkBase());
		url.append(ProfileConstants.LINK_ENTITY_PROFILE);
		if(StringUtils.isNotBlank(userUuid)) {
			url.append("/");
			url.append(userUuid);
		}
		return url.toString();
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public String getEntityLinkToProfileMessages(final String threadId) {
		StringBuilder url = new StringBuilder();
		url.append(getEntityLinkBase());
		url.append(ProfileConstants.LINK_ENTITY_MESSAGES);
		if(StringUtils.isNotBlank(threadId)) {
			url.append("/");
			url.append(threadId);
		}
		return url.toString();
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public String getEntityLinkToProfileConnections() {
		StringBuilder url = new StringBuilder();
		url.append(getEntityLinkBase());
		url.append(ProfileConstants.LINK_ENTITY_CONNECTIONS);
		return url.toString();
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<Person> getListOfFullPersons(final int start, final int count) {
		
		List<Person> persons = new ArrayList<Person>();
		
		//restrict to admin user
		//if(!sakaiProxy.isAdminUser()) {
		//	return persons;
		//}
		
		List<UserProfile> profiles = dao.getUserProfiles(start, count);
		
	  	//foreach UserProfile returned, build our person object
	  	for(UserProfile profile: profiles) {
	  		
	  		//check person really exists
	  		User u = sakaiProxy.getUserQuietly(profile.getUserUuid());
	  		if(u == null) {
	  			continue;
	  		}
	  		
	  		Person p = new Person();
	  		p.setUuid(profile.getUserUuid());
	  		p.setDisplayName(u.getDisplayName());
	  		p.setType(u.getType());
	  		p.setProfile(profile);
	  		
	  		//add privacy record
	  		p.setPrivacy(getPrivacyRecordForUser(profile.getUserUuid()));
	  	
	  		//add preferences record
	  		p.setPreferences(getPreferencesRecordForUser(profile.getUserUuid()));
	  		
	  		persons.add(p);
	  	}
	  	
		return persons;
	}	
		
	/**
 	 * {@inheritDoc}
 	 */
	public String getOfficialImageUrl(final String userUuid) {
		//get external image record for this user
		ProfileImageOfficial official = dao.getOfficialImageRecordForUser(userUuid);
		
		//if none, return null
    	if(official == null) {
    		return null;
    	}
    	
    	if(StringUtils.isBlank(official.getUrl())) {
        	log.error("ProfileLogic.getOfficialImageUrl. No URL for userUuid: " + userUuid);  
			return null;
		}
    	
    	return official.getUrl();
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
	public boolean isOfficialImagePreferred(final String userUuid) {
		
		//get preferences record for this user
    	ProfilePreferences prefs = getPreferencesRecordForUser(userUuid);
    	
    	//if none, return whatever the flag is set as by default
    	if(prefs == null) {
    		return ProfileConstants.DEFAULT_OFFICIAL_IMAGE_SETTING;
    	}
    	
    	//return value
    	return prefs.isUseOfficialImage();
	}
	
	
	
	
	
	
	
	
	
	/**
	 * Helper method to create the link base. We then append more onto it to get the full link.
	 * @return
	 */
	private String getEntityLinkBase() {
		StringBuilder base = new StringBuilder();
		base.append(sakaiProxy.getServerUrl());
		base.append(ProfileConstants.ENTITY_BROKER_PREFIX);
		base.append(ProfileConstants.LINK_ENTITY_PREFIX);
		return base.toString();
	}
		
	
	
	// helper method to check if all required twitter fields are set properly
	private boolean checkTwitterFields(ProfilePreferences prefs) {
		return (prefs.isTwitterEnabled() &&
				StringUtils.isNotBlank(prefs.getTwitterUsername()) &&
				StringUtils.isNotBlank(prefs.getTwitterPasswordDecrypted()));
	}
	
	
	
	
	//private utility method used by findUsersByNameOrEmail() and findUsersByInterest() to format results from
	//the supplied users to SearchResult records based on friend or friendRequest status and the privacy settings for each user
	//that was in the initial search results
	private List<SearchResult> createSearchResultRecordsFromSearch(List<User> users, String userId) {

		List<SearchResult> results = new ArrayList<SearchResult>();
			
		//get type of requesting user so we can check if they are allowed to connect to the users found
		String searchingUserType = sakaiProxy.getUserType(userId);
		
		//for each user, is userId a friend?
		//also, get privacy record for the user. if searches not allowed for this user pair, skip to next
		//otherwise create SearchResult record and add to list
		for(User u : users){
			String userUuid = u.getId();
			
			//if user is in the list of invisible users, skip unless current user is admin
			if(!sakaiProxy.isSuperUser() && sakaiProxy.getInvisibleUsers().contains(userUuid)){
				continue;
			}
			
			//get User details
			String displayName = u.getDisplayName();
			String userType = u.getType();
			
			//friend?
			boolean friend = isUserXFriendOfUserY(userUuid, userId);
			
			//init request flags
			boolean friendRequestToThisPerson = false;
			boolean friendRequestFromThisPerson = false;
			
			//if not friend, has a friend request already been made to this person?
			if(!friend) {
				friendRequestToThisPerson = isFriendRequestPending(userId, userUuid);
			}
			
			//if not friend and no friend request to this person, has a friend request been made from this person to the current user?
			if(!friend && !friendRequestToThisPerson) {
				friendRequestFromThisPerson = isFriendRequestPending(userUuid, userId);
			}
			
			//get privacy record
			ProfilePrivacy privacy = getPrivacyRecordForUser(userUuid);
			
			//is this user visible in searches by this user? if not, skip unless admin user
			if(!sakaiProxy.isSuperUser() && !isUserXVisibleInSearchesByUserY(userUuid, privacy, userId, friend)) {
				continue; 
			}
			
			//is profile photo visible to this user?
			//is status visible to this user?
			//is friends list visible to this user?
			//is connection allowed between these user types?
			//all true if super user, otherwise run the check.
			boolean profileImageAllowed;
			boolean statusVisible;
			boolean friendsListVisible;
			boolean connectionAllowed;
			
			if (sakaiProxy.isSuperUser()) {
				profileImageAllowed = true;
				statusVisible = true;
				friendsListVisible = true;
				connectionAllowed = true;
			} else {
				profileImageAllowed = isUserXProfileImageVisibleByUserY(userUuid, privacy, userId, friend);
				statusVisible = isUserXStatusVisibleByUserY(userUuid, privacy, userId, friend);
				friendsListVisible = isUserXFriendsListVisibleByUserY(userUuid, privacy, userId, friend);
				connectionAllowed = sakaiProxy.isConnectionAllowedBetweenUserTypes(searchingUserType, userType);
			}	
		
			
			//make object
			SearchResult searchResult = new SearchResult(
					userUuid,
					displayName,
					userType,
					friend,
					profileImageAllowed,
					statusVisible,
					friendsListVisible,
					friendRequestToThisPerson,
					friendRequestFromThisPerson,
					connectionAllowed
					);
			
			results.add(searchResult);
		}
		
		return results;
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
		privacy.setSearch((Integer)props.get("search"));
		privacy.setMyFriends((Integer)props.get("myFriends"));
		privacy.setMyStatus((Integer)props.get("myStatus"));
		privacy.setMyPictures((Integer)props.get("myPictures"));
		privacy.setMessages((Integer)props.get("messages"));
		privacy.setBusinessInfo((Integer)props.get("businessInfo"));
		
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
				
		return prefs;
	}
	
	
	
	
	
	
	//init method called when Tomcat starts up
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
			
			//only process uploaded image if doesn't already have a record for this
			if(hasUploadedProfileImage(userUuid)) {
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
					byte[] imageMain = ProfileUtils.scaleImage(image, ProfileConstants.MAX_IMAGE_XY);
					
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
					byte[] imageThumbnail = ProfileUtils.scaleImage(image, ProfileConstants.MAX_THUMBNAIL_IMAGE_XY);
					 
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
					if(addNewProfileImage(userUuid, mainResourceId, thumbnailResourceId)) {
						log.info("Profile2 conversion util: Binary image converted and saved for " + userUuid);
					} else {
						log.warn("Profile2 conversion util: Binary image conversion failed for " + userUuid);
					}
				}
			} 
			
			//process any image URLs, if they don't already have a valid record.
			if(hasExternalProfileImage(userUuid)) {
				log.info("Profile2 conversion util: ProfileImageExternal record exists for " + userUuid + ". Nothing to do here, skipping...");
			} else {
				log.info("Profile2 conversion util: No existing ProfileImageExternal record for " + userUuid + ". Processing...");
				
				String url = sakaiProxy.getSakaiPersonImageUrl(userUuid);
				
				//if none, nothing to do
				if(StringUtils.isBlank(url)) {
					log.info("Profile2 conversion util: No url image to convert for " + userUuid + ". Skipping...");
				} else {
					if(saveExternalImage(userUuid, url, null)) {
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

	
	//setup TinyUrlService API
	/*
	private TinyUrlService tinyUrlService;
	public void setTinyUrlService(TinyUrlService tinyUrlService) {
		this.tinyUrlService = tinyUrlService;
	}
	*/
	
	
	
}
