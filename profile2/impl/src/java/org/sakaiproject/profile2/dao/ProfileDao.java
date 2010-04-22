package org.sakaiproject.profile2.dao;

import java.util.Date;
import java.util.List;

import org.sakaiproject.genericdao.api.GeneralGenericDao;
import org.sakaiproject.profile2.hbm.model.ProfileImageUploaded;
import org.sakaiproject.profile2.hbm.model.ProfileImageExternal;
import org.sakaiproject.profile2.hbm.model.ProfileImageOfficial;
import org.sakaiproject.profile2.model.CompanyProfile;
import org.sakaiproject.profile2.model.GalleryImage;
import org.sakaiproject.profile2.model.Message;
import org.sakaiproject.profile2.model.MessageParticipant;
import org.sakaiproject.profile2.model.MessageThread;
import org.sakaiproject.profile2.model.ProfileFriend;
import org.sakaiproject.profile2.model.ProfilePreferences;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.model.ProfileStatus;
import org.sakaiproject.profile2.model.SocialNetworkingInfo;
import org.sakaiproject.profile2.model.UserProfile;

/**
 * Internal DAO Interface for Profile2
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
	 * @return
	 */
	public List<String> findSakaiPersonsByInterest(final String search);
	
	
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

	
	
	public ProfileImageOfficial getOfficialImageRecordForUser(final String userUuid);
	
	public boolean saveOfficialImageUrl(ProfileImageOfficial officialImage);
	
	
	
	public int getConnectionRequestsForUserCount(final String userId);
	
	//gets a friend record and tries both column arrangements
	public ProfileFriend getConnectionRecord(final String userId, final String friendId);
	
	
	public boolean addNewConnection(ProfileFriend profileFriend);
	
	public boolean updateConnection(ProfileFriend profileFriend);

	public boolean removeConnection(ProfileFriend profileFriend);

	public ProfileFriend getPendingConnection(final String userId, final String friendId);

	
	public ProfileStatus getUserStatus(final String userId, final Date oldestStatusDate);
	
	public boolean setUserStatus(ProfileStatus profileStatus);
	
	public boolean clearUserStatus(ProfileStatus profileStatus);
		
	
	
	public ProfilePrivacy addNewPrivacyRecord(ProfilePrivacy privacy);
	
	public ProfilePrivacy getPrivacyRecord(final String userId);
	
	public boolean updatePrivacyRecord(final ProfilePrivacy privacy);

	
	
	
	
	
	
	
	public boolean addNewCompanyProfile(final CompanyProfile companyProfile);
	
	public boolean updateCompanyProfile(final CompanyProfile companyProfile);
	
	public CompanyProfile getCompanyProfile(final String userId, final long companyProfileId);

	public List<CompanyProfile> getCompanyProfiles(final String userId);
	
	public boolean removeCompanyProfile(final CompanyProfile companyProfile);
	
	
	
	public boolean addNewGalleryImage(final GalleryImage galleryImage);
	
	public GalleryImage getGalleryImageRecord(final String userId, final long imageId);

	public List<GalleryImage> getGalleryImages(final String userId);
	
	public boolean removeGalleryImage(final GalleryImage galleryImage);
	
	
	
	public SocialNetworkingInfo getSocialNetworkingInfo(final String userId);
	
	public boolean saveSocialNetworkingInfo(final SocialNetworkingInfo socialNetworkingInfo);
	
	/**
	 * Add a new profile image record to the database. Invalidates others before it adds itself.
	 *
	 * @param profileImage	ProfileImageUploaded obj	
	 */
	public boolean addNewProfileImage(final ProfileImageUploaded profileImage);
		
	
	public List<String> getAllSakaiPersonIds();
	
	/**
	 * Get a ProfileImageExternal record for a user
	 * @param userId uuid of the user
	 * @return
	 */
	public ProfileImageExternal getExternalImageRecordForUser(final String userId);
	
	public boolean saveExternalImage(final ProfileImageExternal externalImage);
	
	/**
	 * Persist a new ProfilePreferences record and return it.
	 *
	 * @param prefs		complete ProfilePreferences record
	 */
	public ProfilePreferences addNewPreferencesRecord(ProfilePreferences prefs);

	public ProfilePreferences getPreferencesRecordForUser(final String userId);
	
	public boolean savePreferencesRecord(ProfilePreferences prefs);
	
	
	
	
	
	
	
	public int getAllUnreadMessagesCount(final String userId);
	
	public int getThreadsWithUnreadMessagesCount(final String userId);
	
	public List<MessageThread> getMessageThreads(final String userId);
	
	public int getMessageThreadsCount(final String userId);
	
	public List<Message> getMessagesInThread(final String threadId);
	
	public int getMessagesInThreadCount(final String threadId);
	
	public Message getMessage(final String id);
	
	public MessageThread getMessageThread(final String threadId);
	
	public Message getLatestMessageInThread(final String threadId);
	
	public boolean toggleMessageRead(MessageParticipant participant, final boolean status);
	
	public MessageParticipant getMessageParticipant(final String messageId, final String userUuid);
	
	public List<String> getThreadParticipants(final String threadId);
	
	
	public void saveNewThread(MessageThread thread);
	
	public void saveNewMessage(Message message);
	
	public void saveNewMessageParticipant(MessageParticipant participant);
	
	public void saveNewMessageParticipants(List<MessageParticipant> participants);
	
	
	
	public List<UserProfile> getUserProfiles(final int start, final int count);
	
	// Hibernate query constants
	final String QUERY_GET_COMPANY_PROFILE = "getCompanyProfile";
	final String QUERY_GET_COMPANY_PROFILES = "getCompanyProfiles";
	final String QUERY_GET_GALLERY_IMAGE_RECORDS = "getGalleryImageRecords";
	final String QUERY_GET_GALLERY_RECORD = "getGalleryRecord";
	final String QUERY_GET_FRIEND_REQUESTS_FOR_USER = "getFriendRequestsForUser"; 
	final String QUERY_GET_FRIEND_REQUESTS_FOR_USER_COUNT = "getFriendRequestsForUserCount"; 
	final String QUERY_GET_CONFIRMED_FRIEND_USERIDS_FOR_USER = "getConfirmedFriendUserIdsForUser"; 
	final String QUERY_GET_FRIEND_REQUEST = "getFriendRequest"; 
	final String QUERY_GET_FRIEND_RECORD = "getFriendRecord"; 
	final String QUERY_GET_USER_STATUS = "getUserStatus"; 
	final String QUERY_GET_PRIVACY_RECORD = "getPrivacyRecord"; 
	final String QUERY_GET_CURRENT_PROFILE_IMAGE_RECORD = "getCurrentProfileImageRecord"; 
	final String QUERY_OTHER_PROFILE_IMAGE_RECORDS = "getOtherProfileImageRecords"; 
	final String QUERY_FIND_SAKAI_PERSONS_BY_NAME_OR_EMAIL = "findSakaiPersonsByNameOrEmail"; 
	final String QUERY_FIND_SAKAI_PERSONS_BY_INTEREST = "findSakaiPersonsByInterest"; 
	final String QUERY_GET_ALL_SAKAI_PERSON_IDS = "getAllSakaiPersonIds"; 
	final String QUERY_GET_PREFERENCES_RECORD = "getPreferencesRecord";
	final String QUERY_GET_SOCIAL_NETWORKING_INFO = "getSocialNetworkingInfo";
	final String QUERY_GET_EXTERNAL_IMAGE_RECORD = "getProfileImageExternalRecord"; 
	final String QUERY_GET_ALL_SAKAI_PERSONS = "getAllSakaiPersons";
	
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
	
	//from MessageThread.hbm.xml
	final String QUERY_GET_MESSAGE_THREAD="getMessageThread";

	//from MessageRecipient.hbm.xml
	final String QUERY_GET_MESSAGE_PARTICIPANT_FOR_MESSAGE_AND_UUID="getMessageParticipantForMessageAndUuid";
	final String QUERY_GET_THREAD_PARTICIPANTS="getThreadParticipants";

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
