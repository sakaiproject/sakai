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
package org.sakaiproject.profile2.dao;

import java.util.Date;
import java.util.List;

import org.sakaiproject.profile2.hbm.model.ProfileFriend;
import org.sakaiproject.profile2.hbm.model.ProfileImageExternal;
import org.sakaiproject.profile2.hbm.model.ProfileImageOfficial;
import org.sakaiproject.profile2.hbm.model.ProfileImageUploaded;
import org.sakaiproject.profile2.hbm.model.ProfileKudos;
import org.sakaiproject.profile2.model.CompanyProfile;
import org.sakaiproject.profile2.model.ExternalIntegrationInfo;
import org.sakaiproject.profile2.model.GalleryImage;
import org.sakaiproject.profile2.model.Message;
import org.sakaiproject.profile2.model.MessageParticipant;
import org.sakaiproject.profile2.model.MessageThread;
import org.sakaiproject.profile2.model.ProfilePreferences;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.model.ProfileStatus;
import org.sakaiproject.profile2.model.SocialNetworkingInfo;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.model.WallItem;
import org.sakaiproject.profile2.model.WallItemComment;

/**
 * Internal DAO Interface for Profile2.
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public interface ProfileDao {

	/**
	 * Get a list of unconfirmed Friend requests for a given user. Uses a native SQL query
	 * Returns: (all those where userId is the friend_uuid and confirmed=false)
	 *
	 * @param userId		uuid of the user to retrieve the list of friends for
	 */
	public List<String> getRequestedConnectionUserIdsForUser(final String userId);

	/**
	 * Get a list of unconfirmed outgoing Friend requests for a given user.
	 * Returns: A list of userids that userId has sent requests to
	 *
	 * @param userId		uuid of the user to retrieve the list of friends for
	 */
	public List<String> getOutgoingConnectionUserIdsForUser(final String userId);
	
	/**
	 * Get a list of confirmed connections for a given user. Uses a native SQL query so we can use unions
	 * Returns: (all those where userId is the user_uuid and confirmed=true) & (all those where user is friend_uuid and confirmed=true)
	 *
	 * This only returns userIds. If you want a list of Person objects, see getConnectionsForUser()
	 * 
	 * @param userId		uuid of the user to retrieve the list of friends for
	 */
	public List<String> getConfirmedConnectionUserIdsForUser(final String userId);
	
	/**
	 * Get a list of all userIds that match the search criteria in name or email
	 * 
	 * @param search	string to search on
	 * @return
	 */
	public List<String> findSakaiPersonsByNameOrEmail(final String search);
	
	/**
	 * Get a list of all userIds that match the search criteria in the interest fields.
	 * 
	 * @param search	string to search on
	 * @param includeBusinessBio <code>true</code> if the business biography should also be searched.
	 * @return
	 */
	public List<String> findSakaiPersonsByInterest(final String search, boolean includeBusinessBio);
	
	/**
	 * Get the current ProfileImage records from the database.
	 * There should only ever be one, but in case things get out of sync this returns all.
	 * This method is only used when we are adding a new image as we need to invalidate all of the others
	 * If you are just wanting to retrieve the latest image, see getCurrentProfileImageRecord()
	 *
	 * @param userId		userId of the user
	 */
	public List<ProfileImageUploaded> getCurrentProfileImageRecords(final String userId);
	
	/**
	 * Get the current ProfileImage record from the database.
	 * There should only ever be one, but if there are more this will return the latest. 
	 * This is called when retrieving a profile image for a user. When adding a new image, there is a call
	 * to a private method called getCurrentProfileImageRecords() which should invalidate any multiple current images
	 *
	 * @param userId		userId of the user
	 */
	public ProfileImageUploaded getCurrentProfileImageRecord(final String userId);
	
	/**
	 * Get old ProfileImage records from the database. 
	 * TODO: Used for displaying old the profile pictures album
	 *
	 * @param userId		userId of the user
	 */
	public List<ProfileImageUploaded> getOtherProfileImageRecords(final String userId);

	/**
	 * Get the ProfileImageOfficial record from the database for the given user
	 * @param userUuid		uuid of the user
	 * @return
	 */
	public ProfileImageOfficial getOfficialImageRecordForUser(final String userUuid);
	
	/**
	 * Save the ProfileImageOfficial record the database
	 * @param officialImage		ProfileImageOfficial object
	 * @return
	 */
	public boolean saveOfficialImageUrl(ProfileImageOfficial officialImage);
	
	/**
	 * Get a connection record for a user/friend pair
	 * <p>This tries both column arrangements, ie user/friend and friend/user</p>
	 * @param userId		uuid of the user
	 * @param friendId		uuid of the other user
	 * @return
	 */
	public ProfileFriend getConnectionRecord(final String userId, final String friendId);
	
	/**
	 * Save a new connection record
	 * @param profileFriend		ProfileFriend record
	 * @return
	 */
	public boolean addNewConnection(ProfileFriend profileFriend);
	
	/**
	 * Update a connection record
	 * @param profileFriend		ProfileFriend record
	 * @return
	 */
	public boolean updateConnection(ProfileFriend profileFriend);

	/**
	 * Remove a connection record
	 * @param profileFriend		ProfileFriend record
	 * @return
	 */
	public boolean removeConnection(ProfileFriend profileFriend);

	/**
	 * Get a connection record that is pending
	 * @param userId			uuid of the user
	 * @param friendId			uuid of the friend
	 * @return
	 */
	public ProfileFriend getPendingConnection(final String userId, final String friendId);

	/**
	 * Get a ProfileStatus record for a user, but only if the date of the record is within the given time
	 * @param userId				uuid of the user
	 * @param oldestStatusDate		oldest date to search until
	 * @return
	 */
	public ProfileStatus getUserStatus(final String userId, final Date oldestStatusDate);
	
	/**
	 * Set the status for a user
	 * @param profileStatus		ProfileStatus object
	 * @return	
	 */
	public boolean setUserStatus(ProfileStatus profileStatus);
	
	/**
	 * Remove the ProfileStatus record for a user
	 * @param profileStatus		ProfileStatus object
	 * @return	
	 */
	public boolean clearUserStatus(ProfileStatus profileStatus);
	
	/**
	 * Get a count of all status updates for a user
	 * @param userUuid			uuid of the user
	 * @return
	 */
	public int getStatusUpdatesCount(final String userUuid);
	
	/**
	 * Add a new ProfilePrivacy record
	 * @param privacy		ProfilePrivacy object
	 * @return
	 */
	public ProfilePrivacy addNewPrivacyRecord(ProfilePrivacy privacy);
	
	/**
	 * Get the ProfilePrivacy record for the user
	 * @param userId		uuid of the user
	 * @return
	 */
	public ProfilePrivacy getPrivacyRecord(final String userId);
	
	/**
	 * Update the ProfilePrivacy record
	 * @param privacy		ProfilePrivacy object
	 * @return
	 */
	public boolean updatePrivacyRecord(final ProfilePrivacy privacy);

	/**
	 * Save a new CompanyProfile record
	 * @param companyProfile	CompanyProfile record
	 * @return
	 */
	public boolean addNewCompanyProfile(final CompanyProfile companyProfile);
	
	/**
	 * Update a CompanyProfile record
	 * @param companyProfile	CompanyProfile record
	 * @return
	 */
	public boolean updateCompanyProfile(final CompanyProfile companyProfile);
	
	/**
	 * Get the CompanyProfile record for a user and company ID
	 * @param userId				uuid of the user
	 * @param companyProfileId		id of the company
	 * @return
	 */
	public CompanyProfile getCompanyProfile(final String userId, final long companyProfileId);

	/**
	 * Get all CompanyProfile records for a user
	 * @param userId		uuid of the user
	 * @return
	 */
	public List<CompanyProfile> getCompanyProfiles(final String userId);
	
	/**
	 * Remove a CompanyProfile record
	 * @param companyProfile	CompanyProfile record
	 * @return
	 */
	public boolean removeCompanyProfile(final CompanyProfile companyProfile);
	
	/**
	 * Add a new GalleryImage record
	 * 
	 * @param galleryImage		GalleryImage record
	 * @return
	 */
	public boolean addNewGalleryImage(final GalleryImage galleryImage);
	
	/**
	 * Get the GalleryImage record for a user and image ID
	 * @param userId		uuid of the user
	 * @param imageId		id of the image
	 * @return
	 */
	public GalleryImage getGalleryImageRecord(final String userId, final long imageId);

	/**
	 * Get all GalleryImage records for a user
	 * @param userId		uuid of the user
	 * @return
	 */
	public List<GalleryImage> getGalleryImages(final String userId);
	
	/**
	 * Remove a GalleryImage record
	 * 
	 * @param galleryImage		GalleryImage record
	 * @return
	 */
	public boolean removeGalleryImage(final GalleryImage galleryImage);
	
	/**
	 * Get a count of all gallery images that a user has
	 * @param userUuid		uuid of the user
	 * @return
	 */
	public int getGalleryImagesCount(final String userUuid);

	
	/**
	 * Get a SocialNetworkingInfo record for a user
	 * @param userId		uuid of the user
	 * @return
	 */
	public SocialNetworkingInfo getSocialNetworkingInfo(final String userId);
	
	/**
	 * Save a SocialNetworkingInfo record
	 * @param socialNetworkingInfo	SocialNetworkingInfo object
	 * @return
	 */
	public boolean saveSocialNetworkingInfo(final SocialNetworkingInfo socialNetworkingInfo);
	
	/**
	 * Add a new profile image record to the database. Invalidates others before it adds itself.
	 *
	 * @param profileImage	ProfileImageUploaded obj	
	 */
	public boolean addNewProfileImage(final ProfileImageUploaded profileImage);
		
	/**
	 * Get a list of uuids for all users that have a SakaiPerson record
	 * @return list of uuids
	 */
	public List<String> getAllSakaiPersonIds();
	
	/**
	 * Get a total count of all users with SakaiPerson records
	 * @return count
	 */
	public int getAllSakaiPersonIdsCount();
	
	/**
	 * Get a ProfileImageExternal record for a user
	 * @param userId uuid of the user
	 * @return
	 */
	public ProfileImageExternal getExternalImageRecordForUser(final String userId);
	
	/**
	 * Save a ProfileImageExternal record
	 * @param externalImage		ProfileImageExternal record
	 * @return
	 */
	public boolean saveExternalImage(final ProfileImageExternal externalImage);
	
	/**
	 * Persist a new ProfilePreferences record and return it.
	 *
	 * @param prefs		complete ProfilePreferences record
	 */
	public ProfilePreferences addNewPreferencesRecord(ProfilePreferences prefs);

	/**
	 * Get a ProfilePreferences record for the user
	 * @param userId	uuid for the user
	 * @return
	 */
	public ProfilePreferences getPreferencesRecordForUser(final String userId);
	
	/**
	 * Save a ProfilePreferences record
	 * @param prefs		ProfilePreferences record
	 * @return
	 */
	public boolean savePreferencesRecord(ProfilePreferences prefs);
	
	/**
	 * Get a count of all unread messages for a user
	 * @param userId		uuid of the user
	 * @return
	 */
	public int getAllUnreadMessagesCount(final String userId);
	
	/**
	 * Get a count of all threads with unread messages for a user
	 * @param userId		uuid of the user
	 * @return
	 */
	public int getThreadsWithUnreadMessagesCount(final String userId);
	
	/**
	 * Get a count of all sent messages for a user
	 * @param userId		uuid of the user
	 * @return
	 */
	public int getSentMessagesCount(final String userId);

	/**
	 * Get a list of MessageThreads for a user
	 * @param userId		uuid of the user
	 * @return
	 */
	public List<MessageThread> getMessageThreads(final String userId);
	
	/**
	 * Get a count of all message threads for a user
	 * @param userId		uuid of the user
	 * @return
	 */
	public int getMessageThreadsCount(final String userId);
	
	/**
	 * Get a list of all Messages in a given thread
	 * @param threadId		id of the thread
	 * @return
	 */
	public List<Message> getMessagesInThread(final String threadId);
	
	/**
	 * Get a count of all Messages in a given thread
	 * @param threadId		id of the thread
	 * @return
	 */
	public int getMessagesInThreadCount(final String threadId);
	
	/**
	 * Get a Message record
	 * @param id		uuid of the Message
	 * @return
	 */
	public Message getMessage(final String id);
	
	/**
	 * Get a MessageThread record
	 * @param threadId		id of the thread
	 * @return
	 */
	public MessageThread getMessageThread(final String threadId);
	
	/**
	 * Get the latest Message in a MessageThread
	 * @param threadId		id of the thread
	 * @return
	 */
	public Message getLatestMessageInThread(final String threadId);
	
	/**
	 * Toggle a Message as being read by the given participant
	 * @param participant		MessageParticipant
	 * @param status			true/false for read/unread	
	 * @return
	 */
	public boolean toggleMessageRead(MessageParticipant participant, final boolean status);
	
	/**
	 * Get a MessageParticipant record for the given message and user id
	 * @param messageId			uuid of the message
	 * @param userUuid			uuid of the user
	 * @return
	 */
	public MessageParticipant getMessageParticipant(final String messageId, final String userUuid);
	
	/**
	 * Get a list of uuids of all perticipants in a thread
	 * @param threadId			id of the thread
	 * @return
	 */
	public List<String> getThreadParticipants(final String threadId);
	
	/**
	 * Save a MessageThread record
	 * @param thread	MessageThread object
	 */
	public void saveNewThread(MessageThread thread);
	
	/**
	 * Save a Message record
	 * @param thread	Message object
	 */
	public void saveNewMessage(Message message);
	
	/**
	 * Save a MessageParticipant record
	 * @param thread	MessageParticipant object
	 */
	public void saveNewMessageParticipant(MessageParticipant participant);
	
	/**
	 * Save a list of MessageParticipants
	 * @param participants	List of MessageParticipant objects
	 */
	public void saveNewMessageParticipants(List<MessageParticipant> participants);
	
	
	/**
	 * Get a list of UserProfiles withing the given pageing parameters
	 * @param start		first record
	 * @param count		total number of records
	 * @return
	 */
	public List<UserProfile> getUserProfiles(final int start, final int count);
	
	/**
	 * Get the kudos record for a user
	 * @param userUuid
	 * @return	ProfileKudos record, or null
	 */
	public ProfileKudos getKudos(String userUuid);
	
	/**
	 * Update a user's kudos record
	 * @param kudos	ProfileKudos for the user
	 * @return
	 */
	public boolean updateKudos(ProfileKudos kudos);
	
	/**
	 * Get the ExternalIntegrationInfo record for a user
	 * @param userUuid
	 * @return
	 */
	public ExternalIntegrationInfo getExternalIntegrationInfo(final String userUuid);
	
	/**
	 * Update a user's ExternalIntegrationInfo record
	 * @param info	ExternalIntegrationInfo for the user
	 * @return
	 */
	public boolean updateExternalIntegrationInfo(ExternalIntegrationInfo info);
	
	/**
	 * Adds a wall item for the specified user.
	 * 
	 * @param userUuid the user ID.
	 * @param item the wall item to add.
	 * @return <code>true</code> on success, <code>false</code> on failure.
	 */
	public boolean addNewWallItemForUser(final String userUuid, final WallItem item);
	
	/**
	 * Removes a wall item.
	 * 
	 * @param item the wall item to remove.
	 * @return <code>true</code> on success, <code>false</code> on failure.
	 */
	public boolean removeWallItemFromWall(final WallItem item);

	/**
	 * Returns the specified wall item.
	 *
	 * @param wallItemId the wall item to return.
	 * @return the wall item for the specified id.
	 */
	public WallItem getWallItem(final long wallItemId);

	/**
	 * Returns the specified wall item comment.
	 *
	 * @param wallItemCommentId the wall item comment to return.
	 * @return the wall item comment for the specified id.
	 */
	public WallItemComment getWallItemComment(final long wallItemCommentId);
	
	/**
	 * Retrieves all wall items for the specified user.
	 * 
	 * @param userUuid the user ID.
	 * @return the wall items for the specified user.
	 */
	public List<WallItem> getWallItemsForUser(final String userUuid);
			
	/**
	 * Adds a new wall item comment.
	 *  
	 * @param wallItemComment the wall item comment to add.
	 * @return <code>true</code> if the add is successful and
	 *         <code>false</code> if the add fails.
	 */
	public boolean addNewCommentToWallItem(WallItemComment wallItemComment);
	
	/**
	 * Invalidate the current profile image for a user.
	 *
	 * @param userUuid	the uuid for the user
	 */
	public boolean invalidateCurrentProfileImage(final String userUuid);
	
	// Hibernate query constants
	final String QUERY_GET_COMPANY_PROFILE = "getCompanyProfile";
	final String QUERY_GET_COMPANY_PROFILES = "getCompanyProfiles";
	
	final String QUERY_GET_FRIEND_REQUESTS_FOR_USER = "getFriendRequestsForUser"; 
	final String QUERY_GET_OUTGOING_FRIEND_REQUESTS_FOR_USER = "getOutgoingFriendRequestsForUser"; 
	final String QUERY_GET_CONFIRMED_FRIEND_USERIDS_FOR_USER = "getConfirmedFriendUserIdsForUser"; 
	final String QUERY_GET_FRIEND_REQUEST = "getFriendRequest"; 
	final String QUERY_GET_FRIEND_RECORD = "getFriendRecord"; 
	final String QUERY_GET_USER_STATUS = "getUserStatus"; 
	final String QUERY_GET_PRIVACY_RECORD = "getPrivacyRecord"; 
	final String QUERY_GET_CURRENT_PROFILE_IMAGE_RECORD = "getCurrentProfileImageRecord"; 
	final String QUERY_OTHER_PROFILE_IMAGE_RECORDS = "getOtherProfileImageRecords"; 
	final String QUERY_GET_STATUS_UPDATES_COUNT = "getStatusUpdatesCount";

	//GalleryImage
	final String QUERY_GET_GALLERY_IMAGE_RECORDS = "getGalleryImageRecords";
	final String QUERY_GET_GALLERY_RECORD = "getGalleryRecord";
	final String QUERY_GET_GALLERY_IMAGE_RECORDS_COUNT = "getGalleryImageRecordsCount";

	
	final String QUERY_GET_PREFERENCES_RECORD = "getPreferencesRecord";
	final String QUERY_GET_SOCIAL_NETWORKING_INFO = "getSocialNetworkingInfo";
	final String QUERY_GET_EXTERNAL_IMAGE_RECORD = "getProfileImageExternalRecord";

	//SakaiPersonMeta
	final String QUERY_FIND_SAKAI_PERSONS_BY_NAME_OR_EMAIL = "findSakaiPersonsByNameOrEmail"; 
	final String QUERY_FIND_SAKAI_PERSONS_BY_INTEREST = "findSakaiPersonsByInterest";
	final String QUERY_FIND_SAKAI_PERSONS_BY_INTEREST_AND_BUSINESS_BIO = "findSakaiPersonsByInterestAndBusinessBio";
	final String QUERY_GET_SAKAI_PERSON = "getSakaiPerson";
	final String QUERY_GET_ALL_SAKAI_PERSON_IDS = "getAllSakaiPersonIds"; 
	final String QUERY_GET_ALL_SAKAI_PERSON_IDS_COUNT = "getAllSakaiPersonIdsCount";

	
	//ProfileImageOfficial
	final String QUERY_GET_OFFICIAL_IMAGE_RECORD = "getProfileImageOfficialRecord"; 
	
	// from Message.hbm.xml
	final String QUERY_GET_ALL_UNREAD_MESSAGES_COUNT = "getAllUnreadMessagesCount";
	final String QUERY_GET_THREADS_WITH_UNREAD_MESSAGES_COUNT = "getThreadsWithUnreadMessagesCount";
	final String QUERY_GET_MESSAGES_IN_THREAD="getMessagesInThread";
	final String QUERY_GET_MESSAGES_IN_THREAD_COUNT="getMessagesInThreadCount";
	final String QUERY_GET_MESSAGE="getMessage";
	final String QUERY_GET_LATEST_MESSAGE_IN_THREAD = "getLatestMessageInThread";
	final String QUERY_GET_MESSAGE_THREADS="getMessageThreads";
	final String QUERY_GET_MESSAGE_THREADS_COUNT="getMessageThreadsCount";
	final String QUERY_GET_SENT_MESSAGES_COUNT="getSentMessagesCount";

	
	//from MessageThread.hbm.xml
	final String QUERY_GET_MESSAGE_THREAD="getMessageThread";

	//from MessageRecipient.hbm.xml
	final String QUERY_GET_MESSAGE_PARTICIPANT_FOR_MESSAGE_AND_UUID="getMessageParticipantForMessageAndUuid";
	final String QUERY_GET_THREAD_PARTICIPANTS="getThreadParticipants";

	//from ProfileKudos.hbm.xml
	final String QUERY_GET_KUDOS_RECORD="getKudosRecord";
	
	//from ExternalIntegrationInfo.hbm.xml
	final String QUERY_GET_EXTERNAL_INTEGRATION_INFO="getExternalIntegrationInfo";
	
	//from WallItem.hbm.xml
	final String QUERY_GET_WALL_ITEMS = "getWallItemRecords";

	//from WallItem.hbm.xml
	final String QUERY_GET_WALL_ITEM = "getWallItemRecord";
	
	//from WallItemComment.hbm.xml
	final String QUERY_GET_WALL_ITEM_COMMENT = "getWallItemCommentRecord";
	
	//final String QUERY_GET_WALL_ITEMS_COUNT = "getWallItemsCount";
	
	// Hibernate object fields
	final String USER_UUID = "userUuid";
	final String FRIEND_UUID = "friendUuid";
	final String CONFIRMED = "confirmed";
	final String OLDEST_STATUS_DATE = "oldestStatusDate";
	final String SEARCH = "search";
	final String UUID = "uuid";
	final String ID = "id";
	final String THREAD = "thread";
	final String MESSAGE_ID = "messageId";
	
	
}
