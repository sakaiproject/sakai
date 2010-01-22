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

import java.util.List;
import java.util.Map;

import org.sakaiproject.profile2.model.GalleryImage;
import org.sakaiproject.profile2.model.Message;
import org.sakaiproject.profile2.model.MessageParticipant;
import org.sakaiproject.profile2.model.MessageThread;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.model.ProfileImageExternal;
import org.sakaiproject.profile2.model.ProfilePreferences;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.model.ProfileStatus;
import org.sakaiproject.profile2.model.ResourceWrapper;
import org.sakaiproject.profile2.model.SearchResult;

/**
 * This is the internal API to be used by the Profile2 tool and entities only. 
 * 
 * DO NOT IMPLEMENT THIS YOURSELF, use the {@link org.sakaiproject.profile2.service.ProfileService} instead
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */

public interface ProfileLogic {

	/**
	 * Gets a list of Persons's that are connected to this user
	 * 
	 * @param userId		uuid of the user to retrieve the list of connections for
	 * @return
	 */
	public List<Person> getConnectionsForUser(final String userId);
	
	/**
	 * Gets a count of the number of connections a user has.
	 * @param userId		uuid of the user to retrieve the count for
	 * @return
	 */
	public int getCountConnectionsForUser(final String userId);
	
	/**
	 * Gets a list of Persons's that have unconfirmed connection requests to this person
	 * 
	 * @param userId		uuid of the user to retrieve the list of connections for
	 * @return
	 */
	public List<Person> getConnectionRequestsForUser(final String userId);
	
	/**
	 * Gets a count of the number of unconfirmed incoming connection requests
	 * 
	 * @param userId		uuid of the user to retrieve the list of connections for
	 * @return
	 */
	public int getConnectionRequestsForUserCount(final String userId);
	
	/**
	 * Gets a subset of the connection list, based on the search string matching the beginning of the displayName
	 * @param connections	list of connections
	 * @param search		search string to match on
	 * @return
	 */
	public List<Person> getConnectionsSubsetForSearch(List<Person> connections, String search);
	
	
	/**
	 * Make a request for friendId to be a friend of userId
	 *
	 * @param userId		uuid of the user making the request
	 * @param friendId		uuid of the user that userId wants to be a friend of
	 */
	public boolean requestFriend(String userId, String friendId);
	
	/**
	 * Check if there is a pending request from fromUser to toUser
	 *
	 * @param fromUser		uuid of the user that made the friend request
	 * @param toUser		uuid of the user that userId made the request to
	 */
	public boolean isFriendRequestPending(String fromUser, String toUser);
	
	/**
	 * Confirm friend request from fromUser to toUser
	 *
	 * @param fromUser		uuid of the user that made the original friend request
	 * @param toUser		uuid of the user that received the friend request
	 * 
	 * Note that fromUser will ALWAYS be the one making the friend request, 
	 * and toUser will ALWAYS be the one who receives the request.
	 */
	public boolean confirmFriendRequest(String fromUser, String toUser);
	
	/**
	 * Ignore a friend request from fromUser to toUser
	 *
	 * @param fromUser		uuid of the user that made the original friend request
	 * @param toUser		uuid of the user that received the friend request and wants to ignore it
	 * 
	 * Note that fromUser will ALWAYS be the one that made the friend request, 
	 * and toUser will ALWAYS be the one who receives the request.
	 */
	public boolean ignoreFriendRequest(String fromUser, String toUser);
	
	/**
	 * Remove a friend connection
	 *
	 * @param userId		uuid of one user
	 * @param userId		uuid of the other user
	 * 
	 * Note that they could be in either column
	 */
	public boolean removeFriend(String userId, String friendId);
	
	/**
	 * Get the status (message and date) of a user
	 * 
	 * Only returns a status object for those that are up to and including one week old. This could be configurable
	 *
	 * @param userId		uuid of the user to get their status for
	 */
	public ProfileStatus getUserStatus(String userId);
	
	/**
	 * Set user status
	 *
	 * @param userId		uuid of the user 
	 * @param status		status to be set
	 */
	public boolean setUserStatus(String userId, String status);
	
	/**
	 * Set user status
	 *
	 * @param profileStatus		ProfileStatus object for the user
	 */
	public boolean setUserStatus(ProfileStatus profileStatus);
	
	
	/**
	 * Clear user status
	 *
	 * @param userId		uuid of the user 
	 */
	public boolean clearUserStatus(String userId);
		
	/**
	 * Create a persistent default privacy record according to the defaults in ProfileConstants.
	 *
	 * @param userId		uuid of the user to create the record for
	 */
	public ProfilePrivacy createDefaultPrivacyRecord(String userId);
	
	/**
	 * Create a default privacy record according to the defaults in ProfileConstants. Not persisted.
	 *
	 * @param userId		uuid of the user to create the record for
	 */
	public ProfilePrivacy getDefaultPrivacyRecord(String userId);
	
	/**
	 * Retrieve the profile privacy record from the database for this user
	 *
	 * @param userId	uuid of the user to retrieve the record for or null if they don't have one.
	 */
	public ProfilePrivacy getPrivacyRecordForUser(String userId);
	
	/**
	 * Save the profile privacy record to the database
	 *
	 * @param profilePrivacy	the record for the user
	 */
	public boolean savePrivacyRecord(ProfilePrivacy profilePrivacy);
	
	/**
	 * Adds a new gallery image to the database.
	 * 
	 * @param galleryImage the gallery image to add.
	 */
	public boolean addNewGalleryImage(GalleryImage galleryImage);
	
	/**
	 * Retrieves the gallery images from the database for the specified user.
	 * 
	 * @param userId the ID of the user to query by.
	 */
	public List<GalleryImage> getGalleryImages(String userId);
	
	/**
	 * Removes the specified gallery image for the specified user.
	 * 
	 * @param userId the ID of the user to query by.
	 * @param imageId the ID of the gallery image to remove.
	 */
	public boolean removeGalleryImage(String userId, long imageId);
	
	/**
	 * Add a new profile image record to the database. Invalidates others before it adds itself.
	 *
	 * @param userId		userId of the user
	 * @param mainResource	the resourceId of the main profile image
	 * @param resourceId	the ContentHosting resource id
	 */
	public boolean addNewProfileImage(String userId, String mainResource, String thumbnailResource);
	
	
	/**
	 * Find all users that match the search string in either name or email. 
	 * This first queries Sakai's UserDirectoryProvider for matches, then queries SakaiPerson and combines the lists
	 * This approach is so that we can get attempt to get all users, with or without profiles.
	 * 
	 * We then check to see if the returned user is a friend of the person performing the search
	 * We then check to see if this person has their privacy settings restricted such that this user should not be
	 * able to see them. We gather some other privacy information, create a SearchResult item and return the List of these
	 * [The above is performed in a private method]
	 *
	 * Once this list is returned, can lookup more info based on SearchResult.getUserUuid()
	 * 
	 * @param search 	string to search for
	 * @param userId 	uuid of user performing the search
	 * @return List 	of SearchResult objects containing a few other pieces of information.
	 */
	public List<SearchResult> findUsersByNameOrEmail(String search, String userId);

	/**
	 * Find all users that match the search string in any of the relevant SakaiPerson fields
	 * 
	 * We then check to see if the returned user is a friend of the person performing the search
	 * We then check to see if this person has their privacy settings restricted such that this user should not be
	 * able to see them. We gather some other privacy information, create a SearchResult item and return the List of these
	 * [The above is performed in a private method]
	 * 
	 * Once this list is returned, can lookup more info based on SearchResult.getUserUuid()
	 * 
	 * @param search 	string to search for
	 * @param userId 	uuid of user performing the search
	 * @return List 	only userIds (for speed and since the list might be very long).
	 */
	public List<SearchResult> findUsersByInterest(String search, String userId);
	
	
	/**
	 * Get a list of all SakaiPerson's userIds (ie list of all people with profile records)
	 *
	 * @return	List of Sakai userId's 
	 */
	public List<String> getAllSakaiPersonIds();
	
	
	/**
	 * Is userY a friend of the userX?
	 * 
	 * @param userX			the uuid of the user we are querying
	 * @param userY			current user uuid
	 * @return boolean
	 */
	public boolean isUserXFriendOfUserY(String userX, String userY);
	

	
	/**
	 * Should this user show up in searches by the given user?
	 * 
	 * @param userX			the uuid of the user we are querying
	 * @param userY			current user uuid
	 * @param friend 		if the current user is a friend of the user we are querying
	 * @return boolean
	 * 
	 * NOTE: userY is currently not used because the friend status between userX and userY has already
	 * been determined, but it is in now in case later we allow blocking/opening up of info to specific users.
	 * 
	 */
	public boolean isUserXVisibleInSearchesByUserY(String userX, String userY, boolean friend);
	
	/**
	 * Should this user show up in searches by the given user?
	 * 
	 * @param userX			the uuid of the user we are querying
	 * @param profilePrivacy	the privacy record of userX
	 * @param userY			current user uuid
	 * @param friend 		if the current user is a friend of the user we are querying
	 * @return boolean
	 * 
	 * NOTE: userY is currently not used because the friend status between userX and userY has already
	 * been determined, but it is in now in case later we allow blocking/opening up of info to specific users.
	 * 
	 */
	public boolean isUserXVisibleInSearchesByUserY(String userX, ProfilePrivacy profilePrivacy, String userY, boolean friend);
	

	/**
	 * Has the user allowed viewing of their profile image by the given user?
	 * ie have they restricted it to only friends? Or can everyone see it.
	 * 
	 * @param userX			the uuid of the user we are querying
	 * @param userY			current user uuid
	 * @param friend 		if the current user is a friend of the user we are querying
	 * @return boolean
	 *
	 * NOTE: userY is currently not used because the friend status between userX and userY has already
	 * been determined, but it is in now in case later we allow blocking/opening up of info to specific users.
	 * 
	 */
	public boolean isUserXProfileImageVisibleByUserY(String userX, String userY, boolean friend);
	
	/**
	 * Has the user allowed viewing of their profile image by the given user?
	 * ie have they restricted it to only friends? Or can everyone see it.
	 * 
	 * @param userX			the uuid of the user we are querying
	 * @param profilePrivacy	the privacy record of userX
	 * @param userY			current user uuid
	 * @param friend 		if the current user is a friend of the user we are querying
	 * @return boolean
	 *
	 * NOTE: userY is currently not used because the friend status between userX and userY has already
	 * been determined, but it is in now in case later we allow blocking/opening up of info to specific users.
	 * 
	 */
	public boolean isUserXProfileImageVisibleByUserY(String userX, ProfilePrivacy profilePrivacy, String userY, boolean friend);
	
	
	/**
	 * Has the user allowed viewing of their basic info by the given user?
	 * 
	 * @param userX			the uuid of the user we are querying
	 * @param userY			current user uuid
	 * @param friend 		if the current user is a friend of the user we are querying
	 * @return boolean
	 *
	 * NOTE: userY is currently not used because the friend status between userX and userY has already
	 * been determined, but it is in now in case later we allow blocking/opening up of info to specific users.
	 * 
	 */
	public boolean isUserXBasicInfoVisibleByUserY(String userX, String userY, boolean friend);
	
	/**
	 * Has the user allowed viewing of their basic info by the given user?
	 * 
	 * <p>This constructor should be used if you already have the ProfilePrivacy record for userX as will minimise DB lookups</p>
	 * 
	 * @param userX				the uuid of the user we are querying
	 * @param profilePrivacy	the privacy record of userX
	 * @param userY				current user uuid
	 * @param friend			if the current user is a friend of the user we are querying	
	 * @return boolean
	 * 
	 * NOTE: userY is currently not used because the friend status between userX and userY has already
	 * been determined, but it is in now in case later we allow blocking/opening up of info to specific users.
	 */
	public boolean isUserXBasicInfoVisibleByUserY(String userX, ProfilePrivacy profilePrivacy, String userY, boolean friend);
	
	
	/**
	 * Has the user allowed viewing of their contact info by the given user?
	 * 
	 * @param userX			the uuid of the user we are querying
	 * @param userY			current user uuid
	 * @param friend 		if the current user is a friend of the user we are querying
	 * @return boolean
	 *
	 * NOTE: userY is currently not used because the friend status between userX and userY has already
	 * been determined, but it is in now in case later we allow blocking/opening up of info to specific users.
	 * 
	 */
	public boolean isUserXContactInfoVisibleByUserY(String userX, String userY, boolean friend);
	
	/**
	 * Has the user allowed viewing of their contact info by the given user?
	 * 
	 * <p>This constructor should be used if you already have the ProfilePrivacy record for userX as will minimise DB lookups</p>
	 * 
	 * @param userX				the uuid of the user we are querying
	 * @param profilePrivacy	the privacy record of userX
	 * @param userY				current user uuid
	 * @param friend			if the current user is a friend of the user we are querying	
	 * @return boolean
	 * 
	 * NOTE: userY is currently not used because the friend status between userX and userY has already
	 * been determined, but it is in now in case later we allow blocking/opening up of info to specific users.
	 */
	public boolean isUserXContactInfoVisibleByUserY(String userX, ProfilePrivacy profilePrivacy, String userY, boolean friend);
	
	/**
	 * Has the user allowed viewing of their academic info by the given user?
	 * 
	 * @param userX			the uuid of the user we are querying
	 * @param userY			current user uuid
	 * @param friend 		if the current user is a friend of the user we are querying
	 * @return boolean
	 *
	 * NOTE: userY is currently not used because the friend status between userX and userY has already
	 * been determined, but it is in now in case later we allow blocking/opening up of info to specific users.
	 * 
	 */
	public boolean isUserXAcademicInfoVisibleByUserY(String userX, String userY, boolean friend);
	
	/**
	 * Has the user allowed viewing of their academic info by the given user?
	 * 
	 * <p>This constructor should be used if you already have the ProfilePrivacy record for userX as will minimise DB lookups</p>
	 * 
	 * @param userX				the uuid of the user we are querying
	 * @param profilePrivacy	the privacy record of userX
	 * @param userY				current user uuid
	 * @param friend			if the current user is a friend of the user we are querying	
	 * @return boolean
	 * 
	 * NOTE: userY is currently not used because the friend status between userX and userY has already
	 * been determined, but it is in now in case later we allow blocking/opening up of info to specific users.
	 */
	public boolean isUserXAcademicInfoVisibleByUserY(String userX, ProfilePrivacy profilePrivacy, String userY, boolean friend);
	
	/**
	 * Has the user allowed viewing of their business info by the given user?
	 * 
	 * <p>This constructor should be used if you already have the ProfilePrivacy record for userX as will minimise DB lookups</p>
	 * 
	 * @param userX				the uuid of the user we are querying
	 * @param profilePrivacy	the privacy record of userX
	 * @param userY				current user uuid
	 * @param friend			if the current user is a friend of the user we are querying	
	 * @return boolean
	 * 
	 * NOTE: userY is currently not used because the friend status between userX and userY has already
	 * been determined, but it is in now in case later we allow blocking/opening up of info to specific users.
	 */
	public boolean isUserXBusinessInfoVisibleByUserY(String userX, ProfilePrivacy profilePrivacy, String userY, boolean friend);
	
	/**
	 * Has the user allowed viewing of their personal info by the given user?
	 * 
	 * @param userX			the uuid of the user we are querying
	 * @param userY			current user uuid
	 * @param friend 		if the current user is a friend of the user we are querying
	 * @return boolean
	 *
	 * NOTE: userY is currently not used because the friend status between userX and userY has already
	 * been determined, but it is in now in case later we allow blocking/opening up of info to specific users.
	 * 
	 */
	public boolean isUserXPersonalInfoVisibleByUserY(String userX, String userY, boolean friend);
	
	/**
	 * Has the user allowed viewing of their personal info by the given user?
	 * 
	 * <p>This constructor should be used if you already have the ProfilePrivacy record for userX as will minimise DB lookups</p>
	 * 
	 * @param userX				the uuid of the user we are querying
	 * @param profilePrivacy	the privacy record of userX
	 * @param userY				current user uuid
	 * @param friend			if the current user is a friend of the user we are querying	
	 * @return boolean
	 * 
	 * NOTE: userY is currently not used because the friend status between userX and userY has already
	 * been determined, but it is in now in case later we allow blocking/opening up of info to specific users.
	 */
	public boolean isUserXPersonalInfoVisibleByUserY(String userX, ProfilePrivacy profilePrivacy, String userY, boolean friend);
	
	
	/**
	 * Has the user allowed viewing of their friends list (which in turn has its own privacy associated for each record)
	 * by the given user? ie have they restricted it to only me or friends etc
	 * 
	 * @param userX			the uuid of the user we are querying
	 * @param userY			current user uuid
	 * @param friend 		if the current user is a friend of the user we are querying
	 * @return boolean
	 *
	 * NOTE: userY is currently not used because the friend status between userX and userY has already
	 * been determined, but it is in now in case later we allow blocking/opening up of info to specific users.
	 * 
	 */
	public boolean isUserXFriendsListVisibleByUserY(String userX, String userY, boolean friend);
	
	/**
	 * Has the user allowed viewing of their friends list (which in turn has its own privacy associated for each record)
	 * by the given user? ie have they restricted it to only me or friends etc
	 * 
	 * @param userX			the uuid of the user we are querying
	 * @param profilePrivacy	the privacy record of userX
	 * @param userY			current user uuid
	 * @param friend 		if the current user is a friend of the user we are querying
	 * @return boolean
	 *
	 * NOTE: userY is currently not used because the friend status between userX and userY has already
	 * been determined, but it is in now in case later we allow blocking/opening up of info to specific users.
	 * 
	 */
	public boolean isUserXFriendsListVisibleByUserY(String userX, ProfilePrivacy profilePrivacy, String userY, boolean friend);
	
	/**
	 * Has the user allowed viewing of their gallery pictures in their profile. 
	 * 
	 * @param userX the uuid of the user we are querying
	 * @param profilePrivacy
	 * @param userY the current user.
	 * @param friend
	 * @return <code>true</code> if the has user allowed viewing of their
	 * gallery pictures in their profile, otherwise returns <code>false</code>.
	 */
	public boolean isUserXGalleryVisibleByUser(String userX, ProfilePrivacy profilePrivacy, String userY, boolean friend);
	
	/**
	 * Has the user allowed messaging from the given user?
	 * 
	 * @param userX			the uuid of the user we are querying
	 * @param profilePrivacy	the privacy record of userX
	 * @param userY			current user uuid
	 * @param friend 		if the current user is a friend of the user we are querying
	 * @return boolean
	 *
	 * NOTE: userY is currently not used because the friend status between userX and userY has already
	 * been determined, but it is in now in case later we allow blocking/opening up of info to specific users.
	 * 
	 */
	public boolean isUserXMessagingEnabledForUserY(String userX, ProfilePrivacy profilePrivacy, String userY, boolean friend);
	
	/**
	 * Has the user allowed viewing of their status by the given user?
	 * 
	 * @param userX			the uuid of the user we are querying
	 * @param userY			current user uuid
	 * @param friend 		if the current user is a friend of the user we are querying
	 * @return boolean
	 *
	 * NOTE: userY is currently not used because the friend status between userX and userY has already
	 * been determined, but it is in now in case later we allow blocking/opening up of info to specific users.
	 * 
	 */
	public boolean isUserXStatusVisibleByUserY(String userX, String userY, boolean friend);
	
	/**
	 * Has the user allowed viewing of their status by the given user?
	 * 
	 * @param userX			the uuid of the user we are querying
	 * @param profilePrivacy	the privacy record of userX
	 * @param userY			current user uuid
	 * @param friend 		if the current user is a friend of the user we are querying
	 * @return boolean
	 *
	 * NOTE: userY is currently not used because the friend status between userX and userY has already
	 * been determined, but it is in now in case later we allow blocking/opening up of info to specific users.
	 * 
	 */
	public boolean isUserXStatusVisibleByUserY(String userX, ProfilePrivacy profilePrivacy, String userY, boolean friend);
	
	
	/**
	 * Has the user allowed viewing of their birth year in their profile. 
	 * This is either on or off and does not depend on friends etc
	 * 
	 * @param userId			the uuid of the user we are querying
	 * @return boolean
	 */
	public boolean isBirthYearVisible(String userId);
	
	/**
	 * Has the user allowed viewing of their birth year in their profile. 
	 * This is either on or off and does not depend on friends etc
	 * 
	 * @param profilePrivacy	the privacy record for the user. 
	 * 							Used if we already have this info to save a lookup by the above method. The above method calls this for it's checks anyway.
	 * @return boolean
	 */
	public boolean isBirthYearVisible(ProfilePrivacy profilePrivacy);
	
	/**
	 * Get the profile image for the given user, allowing fallback if no thumbnail exists.
	 * 
	 * @param userId 		the uuid of the user we are querying
	 * @param imageType		comes from ProfileConstants and maps to a directory in ContentHosting
	 * @return image as bytes
	 * 
	 * <p>Note: if thumbnail is requested and none exists, the main image will be returned instead. It can be scaled in the markup.</p>
	 *
	 */
	public byte[] getCurrentProfileImageForUser(String userId, int imageType);
	
	/**
	 * Get the profile image for the given user, allowing fallback if no thumbnail exists and wrapping it in a ResourceWrapper
	 * 
	 * @param userId 		the uuid of the user we are querying
	 * @param imageType		comes from ProfileConstants and maps to a directory in ContentHosting
	 * @return image as bytes
	 * 
	 * <p>Note: if thumbnail is requested and none exists, the main image will be returned instead. It can be scaled in the markup.</p>
	 * 
	 */
	public ResourceWrapper getCurrentProfileImageForUserWrapped(String userId, int imageType);
	
	
	/**
	 * Does this user have an uplaoded profile image?
	 * Calls getCurrentProfileImageRecord to see if a record exists.
	 * 
	 * This is mainly used by the convertProfile() method, but could have another use.
	 * 
	 * @param userId 		the uuid of the user we are querying
	 * @return boolean		true if it exists/false if not
	 */
	public boolean hasUploadedProfileImage(String userId);
	
	/**
	 * Does this user have an external profile image?
	 * 
	 * @param userId 		the uuid of the user we are querying
	 * @return boolean		true if it exists/false if not
	 */
	public boolean hasExternalProfileImage(String userId);
	
	
	/**
	 * Create a persistent default preferences record for the user according to the defaults.
	 *
	 * @param userId		uuid of the user to create the record for
	 */
	public ProfilePreferences createDefaultPreferencesRecord(final String userId);
	
	
	/**
	 * Create a default preferences record according to the defaults. Not persisted.
	 *
	 * @param userId		uuid of the user to create the record for
	 */
	public ProfilePreferences getDefaultPreferencesRecord(final String userId);
	
	
	/**
	 * Retrieve the preferences record from the database for this user
	 *
	 * @param userId	uuid of the user to retrieve the record for
	 */
	public ProfilePreferences getPreferencesRecordForUser(final String userId);
	
	/**
	 * Save the preferences record to the database
	 *
	 * @param profilePreferences	the record for the user
	 */
	public boolean savePreferencesRecord(ProfilePreferences profilePreferences);
	
	/**
	 * Check if twitter integration is enabled globally and for a user
	 *
	 * @param userId	uuid of the user
	 */
	public boolean isTwitterIntegrationEnabledForUser(final String userId);
	
	/**
	 * Check if twitter integration is enabled globally and for a user
	 * @param prefs	ProfilePreferences object for the user
	 *
	 */
	public boolean isTwitterIntegrationEnabledForUser(ProfilePreferences prefs);
	
	/**
	 * Send a message to twitter ( runs in a separate thread)
	 * Should only be called if twitter integration is enabled globally (ie via sakai.properties) and for the user.
	 * 
	 * TODO could call validateTwitterCredentials() first perhaps?
	 *
	 * @param userId	uuid of the user
	 * @param message	the message
	 */
	public void sendMessageToTwitter(final String userId, final String message);
	
	/**
	 * Validate the Twitter username and password supplied (does NOT run in a separate thread)
	 *
	 * @param twitterUsername	twitter username
	 * @param twitterPassword	twitter password
	 */
	public boolean validateTwitterCredentials(final String twitterUsername, final String twitterPassword);
	
	/**
	 * Validate the Twitter username and password supplied via the object (does NOT run in a separate thread)
	 *
	 * @param prefs	ProfilePreferences object
	 */
	public boolean validateTwitterCredentials(ProfilePreferences prefs);
	
	/**
	 * Generate a tiny URL for the supplied URL
	 * 
	 * @param url
	 * @return
	 */
	//public String generateTinyUrl(final String url);
	
	/**
	 * Is this type of notification to be sent as an email to the given user?
	 * 
	 * @param userId 	uuid of user
	 * @param messageType type of message
	 * @return
	 */
	public boolean isEmailEnabledForThisMessageType(final String userId, final int messageType);
	
	/**
	 * Get a ProfileImageExternal record for a user
	 * @param userId uuid of the user
	 * @return
	 */
	public ProfileImageExternal getExternalImageRecordForUser(final String userId);
	
	/**
	 * Get the URL to an image that a user has specified as their profile image
	 * @param userId		uuid of user
	 * @param imageType		comes from ProfileConstants. main or thumbnail.
	 *
	 * <p>Note: if thumbnail is requested and none exists, the main image will be returned instead. It can be scaled in the markup.</p>
	 * 
	 * @return
	 */
	public String getExternalImageUrl(final String userId, final int imageType);
	
	
	/**
	 * Save the external image url that users can set.
	 * @param userId		uuid of the user
	 * @param mainUrl		url to main profile pic
	 * @param thumbnailUrl	optional url to a thumbnail. If not set the main will be used and will be scaled in the markup
	 * @return
	 */
	public boolean saveExternalImage(final String userId, final String mainUrl, final String thumbnailUrl);
	
	/**
	 * Gets a URL resource, reads it and returns the byte[] wrapped in ResourceWrapper along with metadata. 
	 * Useful for displaying remote resources where you only have a URL.
	 * 
	 * @param url 	String url of the remote resource
	 * @return
	 */
	public ResourceWrapper getURLResourceAsBytes(final String url);
	
	/**
	 * Get the full URL to the default unavailable image defined in ProfileConstants
	 * @return
	 */
	public String getUnavailableImageURL();
	
	
	/**
	 * Get the number of all unread messages for this user, across all all message threads.
	 *
	 * @param userId		uuid of the user to retrieve the count for
	 */
	public int getAllUnreadMessagesCount(final String userId);
	
	/**
	 * Get the number of threads with unread messages.
	 * <p>For instance, if a user has two message threads, each with one unread message in each thread, this will return 2, as expected.
	 * <br />However, if a user has two message threads, each with 5 unread messages in each thread, this will return 2, not 10.
	 * <br />This is because we are interested in the number of threads with unread messages not the total unread messages. See {@link ProfileLogic#getAllUnreadMessagesCount(String)} if you want that instead.</p>
	 * @param userId		uuid of the user to retrieve the count for
	 * @return
	 */
	public int getThreadsWithUnreadMessagesCount(final String userId);
	
	/**
	 * Gets a MessageThread, first gets the item, then injects the latest Message into it before returning
	 * TODO This needs to be optimised to get the latest message property in the same query.
	 * @param id	id of the thread
	 * @return
	 */
	public MessageThread getMessageThread(final String threadId);
	
	/**
	 * Get the latest Message in a MessageThread
	 * @param threadId 	id of the thread
	 * @return
	 */
	public Message getLatestMessageInThread(final String threadId);
		
	/**
	 * Gets a list of MessageThreads with messages to a given user, each containing the most recent messages in each thread
	 * TODO This needs to be optimised to get the latest message property in the same query.
	 * @param userId	user to get the list of messages for
	 * @return
	 */
	public List<MessageThread> getMessageThreads(final String userId);
	
	/**
	 * Gets the count of the message threads for a user
	 * @param userId	user to get the count of message threads for
	 * @return
	 */
	public int getMessageThreadsCount(final String userId);
	
	/**
	 * Gets a list of the messages contained in this thread, sorted by date posted.
	 * @param threadId	id of the thread to get the messages for
	 * @return
	 */
	public List<Message> getMessagesInThread(final String threadId);
	
	/**
	 * Gets the count of the messages in a thread
	 * @param threadId	thread to get the count for
	 * @return
	 */
	public int getMessagesInThreadCount(final String threadId);
	
	/**
	 * Gets a Message from the database
	 * @param id	id of the message
	 * @return
	 */
	public Message getMessage(final String id);
	
	/**
	 * Send a message
	 * <p>TODO this should be optimised for foreign key constraints</p>
	 * @param uuidTo		uuid of recipient
	 * @param uuidFrom		uuid of sender
	 * @param threadId		threadId, a uuid that should be generated via {@link ProfileUtils.generateUuid()}
	 * @param subject		message subject
	 * @param messageStr	message body
	 * @return
	 */
	public boolean sendNewMessage(final String uuidTo, final String uuidFrom, final String threadId, final String subject, final String messageStr);
	
	/**
	 * Sends a reply to a thread, returns the Message just sent
	 * @param threadId		id of the thread
	 * @param reply			the message
	 * @param userId		uuid of user who is sending the message
	 * @return
	 */
	public Message replyToThread(final String threadId, final String reply, final String userId);
	
	/**
	 * Toggle a single message as read/unread
	 * @param participant	the MessageParticipant record as this is the item that stores read/unread status
	 * @param read		boolean if to be toggled as read/unread
	 * @return
	 */
	public boolean toggleMessageRead(MessageParticipant participant, final boolean read);
	
	/**
	 * Get a MessageParticipant record
	 * @param messageId		message id to get the record for
	 * @param userUuid		uuid to get the record for
	 * @return
	 */
	public MessageParticipant getMessageParticipant(final String messageId, final String userUuid);
	
	/**
	 * Create a new/default MessageParticipant record. If a user is viewing a series of messages and doesn't have a recipient record, one will be created.
	 * <p>For instance, if an existing thread adds a user to the list, they will not have a record for the previous messages. They need one though, so they can mark them as unread/delete them.
	 * <p>This is a persistent object</p>
	 * @param messageId		message id to get the record for
	 * @param userUuid		uuid to get the record for
	 * @return
	 */
	public MessageParticipant createDefaultMessageParticipantRecord(final String messageId, final String userUuid);

	/**
	 * Create a default MessageParticipant object for a message and user. Not persisted.
	 * @see {@link createDefaultMessageParticipantRecord(long, String)}
	 * @param messageId
	 * @param userUuid
	 * @return
	 */
	public MessageParticipant getDefaultMessageParticipantRecord(final String messageId, final String userUuid);

	
	/**
	 * Get a list of all participants in a thread
	 * @param threadId		id of the thread
	 * @return
	 */
	public List<String> getThreadParticipants(final String threadId);
	
	/**
	 * Is the user a participant in this thread?
	 * @param threadId		id of the thread
	 * @param userId		id of the user
	 * @return
	 */
	public boolean isThreadParticipant(final String threadId, final String userId);
	
	/**
	 * Sends an email notification to the users. Used for messages. This formats the data and calls {@link SakaiProxy.sendEmail(List<String> userIds, String emailTemplateKey, Map<String,String> replacementValues)}
	 * @param toUuids		list of users to send the message to - this will be formatted depending on their email preferences for this message type so it is safe to pass all users you need
	 * @param fromUuid		uuid from
	 * @param directId		the id of the item, used for direct links back to this item, if required.
	 * @param subject		subject of message
	 * @param messageStr	body of message
	 * @param messageType	the message type to send from ProfileConstants. Retrieves the emailTemplateKey based on this value
	 */
	public void sendMessageEmailNotification(final List<String> toUuids, final String fromUuid, final String directId, final String subject, final String messageStr, final int messageType);
	
	/**
	 * Sends an email notification to the users. Used for connections. This formats the data and calls {@link SakaiProxy.sendEmail(String userId, String emailTemplateKey, Map<String,String> replacementValues)}
	 * @param toUuid		user to send the message to - this will be formatted depending on their email preferences for this message type so it is safe to pass any users you need
	 * @param fromUuid		uuid from
	 * @param messageType	the message type to send from ProfileConstants. Retrieves the emailTemplateKey based on this value
	 */
	public void sendConnectionEmailNotification(String toUuid, final String fromUuid, final int messageType);
	
	
	/**
	 * Creates a RESTful link to the Profile2 home page for either the currently logged in user (if null param) or the given user.
	 * When followed, will pass through the ProfileLinkEntityProvider and be resolved into the real link
	 * 
	 * <p>This is used for url shortening and also to avoid generating the personalised link for each user for when sending out multiple emails.

	 * @param userUuid	optional if you want to link to the profile view of another person
	 * @return
	 */
	public String getEntityLinkToProfileHome(final String userUuid);

	/**
	 * Creates a RESTful link to the Profile2 message page (and optionally directly to a thread) for any currently logged in user.
	 * When followed, will pass through the ProfileLinkEntityProvider and be resolved into the real link.
	 * 
	 * Note: If that person is not a thread participant, this will be handled in the tool and just put to their message list page.
	 * 
	 * @param threadId	optionally, add the threadId to the URL
	 * @return
	 */
	public String getEntityLinkToProfileMessages(final String threadId);
	
	/**
	 * Creates a RESTful link to the Profile2 conenctions page for any currently logged in user.
	 * When followed, will pass through the ProfileLinkEntityProvider and be resolved into the real link.
	 * 
	 * @return
	 */
	public String getEntityLinkToProfileConnections();
	
	/**
	 * Gets a list of all Person records. It will attempt to set all optional fields, ie Privacy and Preference records as well.
	 * Due to the volume of data that is returned, this method allows for paging.
	 * <p>This is used by the KudosJob calculator. There shouldn't be a real need for other applications to use it.
	 * <p>In addition, due to the sensitive nature of this data, it is restricted to the admin user only.
	 * @param start
	 * @param count
	 * @return
	 */
	public List<Person> getListOfFullPersons(int start, int count);
	
	/**
	 * Get the URL to a user's official profile image
	 * @param userUuid		uuid of user
	 * 
	 * @return url or null if none
	 */
	public String getOfficialImageUrl(final String userUuid);
	
	/**
	 * Save the official image url that users can set.
	 * @param userUuid		uuid of the user
	 * @param url			url to image
	 * @return
	 */
	public boolean saveOfficialImageUrl(final String userUuid, final String url);
	
	/**
	 * Has this user set the official image as their preferred image?
	 * @param userUuid		uuid of the user
	 * @return
	 */
	public boolean isOfficialImagePreferred(final String userUuid);

	
}
