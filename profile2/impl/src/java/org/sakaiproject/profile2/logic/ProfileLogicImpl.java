package org.sakaiproject.profile2.logic;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.sakaiproject.profile2.model.ProfileFriend;
import org.sakaiproject.profile2.model.ProfileImage;
import org.sakaiproject.profile2.model.ProfileImageExternal;
import org.sakaiproject.profile2.model.ProfilePreferences;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.model.ProfileStatus;
import org.sakaiproject.profile2.model.ResourceWrapper;
import org.sakaiproject.profile2.model.SearchResult;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;
import org.sakaiproject.tinyurl.api.TinyUrlService;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import twitter4j.Twitter;

/**
 * This is the Profile2 API Implementation to be used by the Profile2 tool only. 
 * 
 * DO NOT USE THIS YOURSELF, use the ProfileService instead (todo)
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class ProfileLogicImpl extends HibernateDaoSupport implements ProfileLogic {

	private static final Logger log = Logger.getLogger(ProfileLogicImpl.class);

	// Hibernate query constants
	private static final String QUERY_GET_FRIEND_REQUESTS_FOR_USER = "getFriendRequestsForUser"; //$NON-NLS-1$
	private static final String QUERY_GET_CONFIRMED_FRIEND_USERIDS_FOR_USER = "getConfirmedFriendUserIdsForUser"; //$NON-NLS-1$
	private static final String QUERY_GET_FRIEND_REQUEST = "getFriendRequest"; //$NON-NLS-1$
	private static final String QUERY_GET_FRIEND_RECORD = "getFriendRecord"; //$NON-NLS-1$
	private static final String QUERY_GET_USER_STATUS = "getUserStatus"; //$NON-NLS-1$
	private static final String QUERY_GET_PRIVACY_RECORD = "getPrivacyRecord"; //$NON-NLS-1$
	private static final String QUERY_GET_CURRENT_PROFILE_IMAGE_RECORD = "getCurrentProfileImageRecord"; //$NON-NLS-1$
	private static final String QUERY_OTHER_PROFILE_IMAGE_RECORDS = "getOtherProfileImageRecords"; //$NON-NLS-1$
	private static final String QUERY_FIND_SAKAI_PERSONS_BY_NAME_OR_EMAIL = "findSakaiPersonsByNameOrEmail"; //$NON-NLS-1$
	private static final String QUERY_FIND_SAKAI_PERSONS_BY_INTEREST = "findSakaiPersonsByInterest"; //$NON-NLS-1$
	private static final String QUERY_LIST_ALL_SAKAI_PERSONS = "listAllSakaiPersons"; //$NON-NLS-1$
	private static final String QUERY_GET_PREFERENCES_RECORD = "getPreferencesRecord"; //$NON-NLS-1$
	private static final String QUERY_GET_EXTERNAL_IMAGE_RECORD = "getProfileImageExternalRecord"; //$NON-NLS-1$

	// Hibernate object fields
	private static final String USER_UUID = "userUuid"; //$NON-NLS-1$
	private static final String FRIEND_UUID = "friendUuid"; //$NON-NLS-1$
	private static final String CONFIRMED = "confirmed"; //$NON-NLS-1$
	private static final String OLDEST_STATUS_DATE = "oldestStatusDate"; //$NON-NLS-1$
	private static final String SEARCH = "search"; //$NON-NLS-1$
	
	

	
	/**
 	 * {@inheritDoc}
 	 */
	public List<String> getFriendRequestsForUser(final String userId) {
		
		if(userId == null){
	  		throw new IllegalArgumentException("Null Argument in Profile.getFriendRequestsForUser()"); //$NON-NLS-1$
	  	}
		
		List<String> requests = new ArrayList<String>();
		
		//get friends of this user [and map it automatically to the Friend object]
		//updated: now just returns a List of Strings
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			
	  			Query q = session.getNamedQuery(QUERY_GET_FRIEND_REQUESTS_FOR_USER);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			q.setBoolean("false", Boolean.FALSE); //$NON-NLS-1$
	  			//q.setResultTransformer(Transformers.aliasToBean(Friend.class));
	  			
	  			return q.list();
	  		}
	  	};
	  	
	  	requests = (List<String>) getHibernateTemplate().executeFind(hcb);
	  	
	  	return requests;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<String> getConfirmedFriendUserIdsForUser(final String userId) {
		
		List<String> userUuids = new ArrayList<String>();
		
		//get 
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			
	  		
	  			Query q = session.getNamedQuery(QUERY_GET_CONFIRMED_FRIEND_USERIDS_FOR_USER);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			q.setBoolean("true", Boolean.TRUE); //$NON-NLS-1$
	  			return q.list();
	  		}
	  	};
	  	
	  	userUuids = (List<String>) getHibernateTemplate().executeFind(hcb);
	
	  	return userUuids;
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */	
	public int countConfirmedFriendUserIdsForUser(final String userId) {
		
		//this should operhaps be a count(*) query but since we need to use unions, hmm.
		List<String> userUuids = new ArrayList<String>(getConfirmedFriendUserIdsForUser(userId));
		int count = userUuids.size();
	
	  	return count;
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean requestFriend(String userId, String friendId) {
		if(userId == null || friendId == null){
	  		throw new IllegalArgumentException("Null Argument in Profile.getFriendsForUser"); //$NON-NLS-1$
	  	}
		
		//check values are valid, ie userId, friendId etc
		
		try {
			//make a ProfileFriend object with 'Friend Request' constructor
			ProfileFriend profileFriend = new ProfileFriend(userId, friendId, ProfileConstants.RELATIONSHIP_FRIEND);
			getHibernateTemplate().save(profileFriend);
			log.info("User: " + userId + " requested friend: " + friendId); //$NON-NLS-1$ //$NON-NLS-2$
			return true;
		} catch (Exception e) {
			log.error("Profile.requestFriend() failed. " + e.getClass() + ": " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
	
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean isFriendRequestPending(String fromUser, String toUser) {
		
		ProfileFriend profileFriend = getPendingFriendRequest(fromUser, toUser);

		if(profileFriend == null) {
			log.debug("Profile.isFriendRequestPending: No pending friend request from userId: " + fromUser + " to friendId: " + toUser + " found."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return false;
		}
		return true;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean confirmFriendRequest(final String fromUser, final String toUser) {
		
		if(fromUser == null || toUser == null){
	  		throw new IllegalArgumentException("Null Argument in Profile.confirmFriendRequest"); //$NON-NLS-1$
	  	}
		
		//get pending ProfileFriend object request for the given details
		ProfileFriend profileFriend = getPendingFriendRequest(fromUser, toUser);

		if(profileFriend == null) {
			log.error("Profile.confirmFriendRequest() failed. No pending friend request from userId: " + fromUser + " to friendId: " + toUser + " found."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return false;
		}
		
	  	//make necessary changes to the ProfileFriend object.
	  	profileFriend.setConfirmed(true);
	  	profileFriend.setConfirmedDate(new Date());
		
		//save
		try {
			getHibernateTemplate().update(profileFriend);
			log.info("User: " + fromUser + " confirmed friend request from: " + toUser); //$NON-NLS-1$ //$NON-NLS-2$
			return true;
		} catch (Exception e) {
			log.error("Profile.confirmFriendRequest() failed. " + e.getClass() + ": " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
	
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean ignoreFriendRequest(final String fromUser, final String toUser) {
		
		if(fromUser == null || toUser == null){
	  		throw new IllegalArgumentException("Null Argument in Profile.ignoreFriendRequest"); //$NON-NLS-1$
	  	}
		
		//get pending ProfileFriend object request for the given details
		ProfileFriend profileFriend = getPendingFriendRequest(fromUser, toUser);

		if(profileFriend == null) {
			log.error("Profile.ignoreFriendRequest() failed. No pending friend request from userId: " + fromUser + " to friendId: " + toUser + " found."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return false;
		}
		
	  	
		//delete
		try {
			getHibernateTemplate().delete(profileFriend);
			log.info("User: " + toUser + " ignored friend request from: " + fromUser); //$NON-NLS-1$ //$NON-NLS-2$
			return true;
		} catch (Exception e) {
			log.error("Profile.ignoreFriendRequest() failed. " + e.getClass() + ": " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
	
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean removeFriend(String userId, String friendId) {
		
		if(userId == null || friendId == null){
	  		throw new IllegalArgumentException("Null Argument in Profile.removeFriend"); //$NON-NLS-1$
	  	}
		
		//get the friend object for this connection pair (could be any way around)
		ProfileFriend profileFriend = getFriendRecord(userId, friendId);
		
		if(profileFriend == null){
			log.error("ProfileFriend record does not exist for userId: " + userId + ", friendId: " + friendId); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
				
		//if ok, delete it
		try {
			getHibernateTemplate().delete(profileFriend);
			log.info("User: " + userId + " removed friend: " + friendId); //$NON-NLS-1$ //$NON-NLS-2$
			return true;
		} catch (Exception e) {
			log.error("Profile.removeFriend() failed. " + e.getClass() + ": " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
		
		
	}
	
	//only gets a pending request
	private ProfileFriend getPendingFriendRequest(final String userId, final String friendId) {
		
		if(userId == null || friendId == null){
	  		throw new IllegalArgumentException("Null Argument in Profile.getPendingFriendRequest"); //$NON-NLS-1$
	  	}
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			Query q = session.getNamedQuery(QUERY_GET_FRIEND_REQUEST);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			q.setParameter(FRIEND_UUID, friendId, Hibernate.STRING);
	  			q.setParameter(CONFIRMED, false, Hibernate.BOOLEAN);
	  			q.setMaxResults(1);
	  			return q.uniqueResult();
			}
		};
	
		return (ProfileFriend) getHibernateTemplate().execute(hcb);
	
	}
	
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public int getUnreadMessagesCount(String userId) {
		int unreadMessages = 0;
		return unreadMessages;
		
	}
	
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public ProfileStatus getUserStatus(final String userId) {
		
		if(userId == null){
	  		throw new IllegalArgumentException("Null Argument in Profile.getUserStatus"); //$NON-NLS-1$
	  	}
		
		// compute oldest date for status 
		Calendar cal = Calendar.getInstance(); 
		cal.add(Calendar.DAY_OF_YEAR, -7); 
		final Date oldestStatusDate = cal.getTime(); 
				
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			Query q = session.getNamedQuery(QUERY_GET_USER_STATUS);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			q.setParameter(OLDEST_STATUS_DATE, oldestStatusDate, Hibernate.DATE);
	  			q.setMaxResults(1);
	  			return q.uniqueResult();
			}
		};
	
		return (ProfileStatus) getHibernateTemplate().execute(hcb);
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
	 * Set user status
	 *
	 * @param profileStatus		ProfileStatus object for the user
	 */
	public boolean setUserStatus(ProfileStatus profileStatus) {
		
		try {
			//only allowing oen status object per user, hence saveOrUpdate
			getHibernateTemplate().saveOrUpdate(profileStatus);
			log.info("Updated status for user: " + profileStatus.getUserUuid()); 
			return true;
		} catch (Exception e) {
			log.error("Profile.setUserStatus() failed. " + e.getClass() + ": " + e.getMessage()); 
			return false;
		}
		
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean clearUserStatus(String userId) {
		
		//validate userId here - TODO
		
		ProfileStatus profileStatus = getUserStatus(userId);
		
		if(profileStatus == null){
			log.error("ProfileStatus null for userId: " + userId); //$NON-NLS-1$
			return false;
		}
				
		//if ok, delete it
		try {
			getHibernateTemplate().delete(profileStatus);
			log.info("User: " + userId + " cleared status"); //$NON-NLS-1$ //$NON-NLS-2$
			return true;
		} catch (Exception e) {
			log.error("Profile.clearUserStatus() failed. " + e.getClass() + ": " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
		
	}

	
	

	
	
	/**
 	 * {@inheritDoc}
 	 */
	public ProfilePrivacy createDefaultPrivacyRecord(String userId) {
		
		//see ProfilePrivacy for this constructor and what it all means
		ProfilePrivacy profilePrivacy = getDefaultPrivacyRecord(userId);
		
		//save
		try {
			getHibernateTemplate().save(profilePrivacy);
			log.info("Created default privacy record for user: " + userId); //$NON-NLS-1$
			return profilePrivacy;
		} catch (Exception e) {
			log.error("Profile.createDefaultPrivacyRecord() failed. " + e.getClass() + ": " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public ProfilePrivacy getDefaultPrivacyRecord(String userId) {
		
		//get the overriden privacy settings. they'll be defaults if not specified
		HashMap<String, Object> props = sakaiProxy.getOverriddenPrivacySettings();	
		
		//using the props, set them into the ProfilePrivacy object
		ProfilePrivacy profilePrivacy = new ProfilePrivacy(
				userId,
				(Integer)props.get("profileImage"),
				(Integer)props.get("basicInfo"),
				(Integer)props.get("contactInfo"),
				(Integer)props.get("academicInfo"),
				(Integer)props.get("personalInfo"),
				(Boolean)props.get("birthYear"),
				(Integer)props.get("search"),
				(Integer)props.get("myFriends"),
				(Integer)props.get("myStatus")
		);
		
		return profilePrivacy;
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public ProfilePrivacy getPrivacyRecordForUser(final String userId) {
		
		if(userId == null){
	  		throw new IllegalArgumentException("Null Argument in Profile.getPrivacyRecordForUser"); //$NON-NLS-1$
	  	}
		
		ProfilePrivacy privacy = null;
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			Query q = session.getNamedQuery(QUERY_GET_PRIVACY_RECORD);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			q.setMaxResults(1);
	  			return q.uniqueResult();
			}
		};
	
		privacy = (ProfilePrivacy) getHibernateTemplate().execute(hcb);
		
		//if none, get a default, which can be overridden by sakai.properties
		if(privacy == null) {
			privacy = this.getDefaultPrivacyRecord(userId);
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
		
		try {
			getHibernateTemplate().saveOrUpdate(profilePrivacy);
			log.info("Saved privacy record for user: " + profilePrivacy.getUserUuid()); //$NON-NLS-1$
			return true;
		} catch (Exception e) {
			log.error("Profile.savePrivacyRecordForUser() failed. " + e.getClass() + ": " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
		
	}
	

	/**
 	 * {@inheritDoc}
 	 */
	public boolean addNewProfileImage(final String userId, final String mainResource, final String thumbnailResource) {
		
		Boolean success = (Boolean) getHibernateTemplate().execute(new HibernateCallback() {			
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					try {
						//first get the current ProfileImage records for this user
						List<ProfileImage> currentImages = new ArrayList<ProfileImage>(getCurrentProfileImageRecords(userId));
            
						for(Iterator<ProfileImage> i = currentImages.iterator(); i.hasNext();){
							ProfileImage currentImage = (ProfileImage)i.next();
              
							//invalidate each
							currentImage.setCurrent(false);
              
							//save
							session.update(currentImage);
						}
						//now create a new ProfileImage object with the new data - this is our new current ProfileImage
						ProfileImage newProfileImage = new ProfileImage(userId, mainResource, thumbnailResource, true);
              
						//save the new ProfileImage to the db
						session.save(newProfileImage);
						
						// flush session
			            session.flush();
            
					} catch(Exception e) {
						log.error("Profile.saveProfileImageRecord() failed. " + e.getClass() + ": " + e.getMessage()); 
						return Boolean.FALSE;
					}
					return Boolean.TRUE;
				}			
		});
		return success.booleanValue();
	}


	/**
 	 * {@inheritDoc}
 	 */
	public List<SearchResult> findUsersByNameOrEmail(String search, String userId) {
		
		//perform search (uses private method to wrap the two searches into one)
		List<String> userUuids = new ArrayList<String>(findUsersByNameOrEmail(search));

		//restrict to only return the max number. UI will print message
		int maxResults = ProfileConstants.MAX_SEARCH_RESULTS;
		if(userUuids.size() >= maxResults) {
			userUuids = userUuids.subList(0, maxResults);
		}
		
		//format into SearchResult records (based on friend status, privacy status etc)
		List<SearchResult> results = new ArrayList<SearchResult>(createSearchResultRecordsFromSearch(userUuids, userId));
		
		return results;
		
	}
	
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<SearchResult> findUsersByInterest(String search, String userId) {
		
		//perform search (uses private method to wrap the search)
		List<String> userUuids = new ArrayList<String>(findSakaiPersonsByInterest(search));
		
		//restrict to only return the max number. UI will print message
		int maxResults = ProfileConstants.MAX_SEARCH_RESULTS;
		if(userUuids.size() >= maxResults) {
			userUuids = userUuids.subList(0, maxResults);
		}
		
		//format into SearchResult records (based on friend status, privacy status etc)
		List<SearchResult> results = new ArrayList<SearchResult>(createSearchResultRecordsFromSearch(userUuids, userId));
		
		return results;
		
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public List<String> listAllSakaiPersons() {
		
		List<String> userUuids = new ArrayList<String>();
		
		//get 
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			
	  			Query q = session.getNamedQuery(QUERY_LIST_ALL_SAKAI_PERSONS);
	  			return q.list();
	  		}
	  	};
	  	
	  	userUuids = (List<String>) getHibernateTemplate().executeFind(hcb);
	
	  	return userUuids;
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
		List<String> friendUuids = new ArrayList<String>(getConfirmedFriendUserIdsForUser(userY));
		
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
    	log.error("Profile.isUserXVisibleInSearchesByUserY. Uncaught rule. userX: " + userX + ", userY: " + userY + ", friend: " + friend); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
    	log.error("Profile.isUserProfileImageVisibleByCurrentUser. Uncaught rule. userX: " + userX + ", userY: " + userY + ", friend: " + friend); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
    	log.error("Profile.isUserXBasicInfoVisibleByUserY. Uncaught rule. userX: " + userX + ", userY: " + userY + ", friend: " + friend); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
    	log.error("Profile.isUserXContactInfoVisibleByUserY. Uncaught rule. userX: " + userX + ", userY: " + userY + ", friend: " + friend); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    	return false;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean isUserXAcademicInfoVisibleByUserY(String userX, String userY, boolean friend) {
		
		//if user is requesting own info, they ARE allowed
    	if(userY.equals(userX)) {
    		return true;
    	}
		
		//get privacy record for this user
    	ProfilePrivacy profilePrivacy = getPrivacyRecordForUser(userX);
    	
    	//pass to main
    	return isUserXAcademicInfoVisibleByUserY(userX, profilePrivacy, userY, friend);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean isUserXAcademicInfoVisibleByUserY(String userX, ProfilePrivacy profilePrivacy, String userY, boolean friend) {
		
		//if user is requesting own info, they ARE allowed
    	if(userY.equals(userX)) {
    		return true;
    	}
		
		//if no privacy record, return whatever the flag is set as by default
    	/* deprecated by PRFL-86, privacy object will never be null now it will always be default or overridden default
    	if(profilePrivacy == null) {
    		return ProfileConstants.DEFAULT_ACADEMICINFO_VISIBILITY;
    	}
    	*/
    	
    	//if restricted to only self, not allowed
    	if(profilePrivacy.getAcademicInfo() == ProfileConstants.PRIVACY_OPTION_ONLYME) {
    		return false;
    	}
    	
    	//if user is friend and friends are allowed
    	if(friend && profilePrivacy.getAcademicInfo() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
    		return true;
    	}
    	
    	//if not friend and set to friends only
    	if(!friend && profilePrivacy.getAcademicInfo() == ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) {
    		return false;
    	}
    	
    	//if everyone is allowed
    	if(profilePrivacy.getAcademicInfo() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
    		return true;
    	}
    	
    	//uncaught rule, return false
    	log.error("Profile.isUserXAcademicInfoVisibleByUserY. Uncaught rule. userX: " + userX + ", userY: " + userY + ", friend: " + friend); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

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
    	log.error("Profile.isUserXPersonalInfoVisibleByUserY. Uncaught rule. userX: " + userX + ", userY: " + userY + ", friend: " + friend); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
    	log.error("Profile.isUserXFriendsListVisibleByUserY. Uncaught rule. userX: " + userX + ", userY: " + userY + ", friend: " + friend); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	return false;
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
    	log.error("Profile.isUserXStatusVisibleByUserY. Uncaught rule. userX: " + userX + ", userY: " + userY + ", friend: " + friend); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
		ProfileImage profileImage = getCurrentProfileImageRecord(userId);
		
		if(profileImage == null) {
			log.debug("Profile.getCurrentProfileImageForUser() null for userId: " + userId);
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
		ProfileImage profileImage = getCurrentProfileImageRecord(userId);
		
		if(profileImage == null) {
			log.debug("Profile.getCurrentProfileImageForUserWrapped() null for userId: " + userId);
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
		ProfileImage record = getCurrentProfileImageRecord(userId);
		
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
		ProfileImageExternal record = getExternalImageRecordForUser(userId);
		
		if(record == null) {
			return false;
		}
		return true;
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public ProfilePreferences createDefaultPreferencesRecord(final String userId) {
		
		ProfilePreferences prefs = getDefaultPreferencesRecord(userId);
		
		//save
		try {
			getHibernateTemplate().save(prefs);
			log.info("Created default preferences record for user: " + userId); //$NON-NLS-1$
			return prefs;
		} catch (Exception e) {
			log.error("Profile.createDefaultPreferencesRecord() failed. " + e.getClass() + ": " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public ProfilePreferences getDefaultPreferencesRecord(final String userId) {
		
		ProfilePreferences prefs = new ProfilePreferences(
				userId,
				ProfileConstants.DEFAULT_EMAIL_REQUEST_SETTING,
				ProfileConstants.DEFAULT_EMAIL_CONFIRM_SETTING,
				ProfileConstants.DEFAULT_TWITTER_SETTING);
		
			return prefs;
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public ProfilePreferences getPreferencesRecordForUser(final String userId) {
		
		if(userId == null){
	  		throw new IllegalArgumentException("Null Argument in Profile.getPreferencesRecordForUser"); //$NON-NLS-1$
	  	}
		
		ProfilePreferences prefs = null;
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			Query q = session.getNamedQuery(QUERY_GET_PREFERENCES_RECORD);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			q.setMaxResults(1);
	  			return q.uniqueResult();
			}
		};
	
		prefs = (ProfilePreferences) getHibernateTemplate().execute(hcb);
		
		if(prefs == null) {
			return null;
		}
		
		//decrypt password and set into field
		prefs.setTwitterPasswordDecrypted(ProfileUtils.decrypt(prefs.getTwitterPasswordEncrypted()));
		
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
		
		try {
			getHibernateTemplate().saveOrUpdate(prefs);
			log.info("Updated preferences record for user: " + prefs.getUserUuid()); //$NON-NLS-1$
			return true;
		} catch (Exception e) {
			log.error("Profile.savePreferencesRecord() failed. " + e.getClass() + ": " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
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
				
				runner = new Thread(this,"Profile2 TwitterUpdater thread"); //$NON-NLS-1$
				runner.start();
			}
			

			//do it!
			public synchronized void run() {
				
				Twitter twitter = new Twitter(username, password);
				
				try {
					twitter.setSource(sakaiProxy.getTwitterSource());
					twitter.update(message);
					log.info("Twitter status updated for: " + userId); //$NON-NLS-1$
				}
				catch (Exception e) {
					log.error("Profile.sendMessageToTwitter() failed. " + e.getClass() + ": " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
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
	public String generateTinyUrl(final String url) {
		return tinyUrlService.generateTinyUrl(url);
	}
	
	
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
    	
    	//add more cases here as need progresses
    	
    	//no notification for this message type, return false 	
    	log.debug("Profile.isEmailEnabledForThisMessageType. False for userId: " + userId + ", messageType: " + messageType); //$NON-NLS-1$ //$NON-NLS-2$

    	return false;
		
	}

	
	/**
 	 * {@inheritDoc}
 	 */
	public ProfileImageExternal getExternalImageRecordForUser(final String userId) {
		
		if(userId == null){
	  		throw new IllegalArgumentException("Null Argument in Profile.getExternalImageRecordForUser"); //$NON-NLS-1$
	  	}
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			Query q = session.getNamedQuery(QUERY_GET_EXTERNAL_IMAGE_RECORD);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			q.setMaxResults(1);
	  			return q.uniqueResult();
			}
		};
	
		return (ProfileImageExternal) getHibernateTemplate().execute(hcb);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public String getExternalImageUrl(final String userId, final int imageType) {
		
		//get external image record for this user
		ProfileImageExternal externalImage = getExternalImageRecordForUser(userId);
		
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
    	
    	//no notification for this message type, return false 	
    	log.error("Profile.getExternalImageUrl. No URL for userId: " + userId + ", imageType: " + imageType); //$NON-NLS-1$ //$NON-NLS-2$

    	return null;
		
	}

	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean saveExternalImage(final String userId, final String mainUrl, final String thumbnailUrl) {
		
		//make an object out of the params
		ProfileImageExternal ext = new ProfileImageExternal(userId, mainUrl, thumbnailUrl);
		
		try {
			getHibernateTemplate().saveOrUpdate(ext);
			log.info("Updated external image record for user: " + ext.getUserUuid()); //$NON-NLS-1$
			return true;
		} catch (Exception e) {
			log.error("Profile.setExternalImage() failed. " + e.getClass() + ": " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
	}

	
		
	/**
 	 * {@inheritDoc}
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

	
	
	// helper method to check if all required twitter fields are set properly
	private boolean checkTwitterFields(ProfilePreferences prefs) {
		return (prefs.isTwitterEnabled() &&
				StringUtils.isNotBlank(prefs.getTwitterUsername()) &&
				StringUtils.isNotBlank(prefs.getTwitterPasswordDecrypted()));
	}
	
	
	
	//private method to query SakaiPerson for matches
	//this should go in the profile ProfilePersistence API
	private List<String> findSakaiPersonsByNameOrEmail(final String search) {
		
		List<String> userUuids = new ArrayList<String>();
		
		//get 
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			
	  			Query q = session.getNamedQuery(QUERY_FIND_SAKAI_PERSONS_BY_NAME_OR_EMAIL);
	  			q.setParameter(SEARCH, '%' + search + '%', Hibernate.STRING);
	  			return q.list();
	  		}
	  	};
	  	
	  	userUuids = (List<String>) getHibernateTemplate().executeFind(hcb);
	
	  	return userUuids;
	}
	
	
	//private method to query SakaiPerson for matches
	//this should go in the profile ProfilePersistence API
	private List<String> findSakaiPersonsByInterest(final String search) {
		
		List<String> userUuids = new ArrayList<String>();
		
		//get 
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			
	  			Query q = session.getNamedQuery(QUERY_FIND_SAKAI_PERSONS_BY_INTEREST);
	  			q.setParameter(SEARCH, '%' + search + '%', Hibernate.STRING);
	  			return q.list();
	  		}
	  	};
	  	
	  	userUuids = (List<String>) getHibernateTemplate().executeFind(hcb);
	
	  	return userUuids;
	}


	
	
	
	/**
	 * Get the current ProfileImage records from the database.
	 * There should only ever be one, but in case things get out of sync this returns all.
	 * This method is only used when we are adding a new image as we need to invalidate all of the others
	 * If you are just wanting to retrieve the latest image, see getCurrentProfileImageRecord()
	 *
	 * @param userId		userId of the user
	 */
	private List<ProfileImage> getCurrentProfileImageRecords(final String userId) {
		
		List<ProfileImage> images = new ArrayList<ProfileImage>();
		
		//get 
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			
	  			Query q = session.getNamedQuery(QUERY_GET_CURRENT_PROFILE_IMAGE_RECORD);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			return q.list();
	  		}
	  	};
	  	
	  	images = (List<ProfileImage>) getHibernateTemplate().executeFind(hcb);
	  	
	  	return images;
	}


	/**
	 * Get the current ProfileImage record from the database.
	 * There should only ever be one, but if there are more this will return the latest. 
	 * This is called when retrieving a profile image for a user. When adding a new image, there is a call
	 * to a private method called getCurrentProfileImageRecords() which should invalidate any multiple current images
	 *
	 * @param userId		userId of the user
	 */
	private ProfileImage getCurrentProfileImageRecord(final String userId) {
		
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			Query q = session.getNamedQuery(QUERY_GET_CURRENT_PROFILE_IMAGE_RECORD);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			q.setMaxResults(1);
	  			return q.uniqueResult();
			}
		};
	
		return (ProfileImage) getHibernateTemplate().execute(hcb);
	}
	
	
	/**
	 * Get old ProfileImage records from the database. 
	 * TODO: Used for displaying old the profile pictures album
	 *
	 * @param userId		userId of the user
	 */
	private List<ProfileImage> getOtherProfileImageRecords(final String userId) {
		
		List<ProfileImage> images = new ArrayList<ProfileImage>();
		
		//get 
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			
	  			Query q = session.getNamedQuery(QUERY_OTHER_PROFILE_IMAGE_RECORDS);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			return q.list();
	  		}
	  	};
	  	
	  	images = (List<ProfileImage>) getHibernateTemplate().executeFind(hcb);
	  	
	  	return images;
	}


	//gets a friend record and tries both column arrangements
	private ProfileFriend getFriendRecord(final String userId, final String friendId) {
		
		if(userId == null || friendId == null){
	  		throw new IllegalArgumentException("Null Argument in Profile.getFriendRecord"); //$NON-NLS-1$
	  	}
		
		ProfileFriend profileFriend = null;
		
		//this particular query checks for records when userId/friendId is in either column
		HibernateCallback hcb = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	  			Query q = session.getNamedQuery(QUERY_GET_FRIEND_RECORD);
	  			q.setParameter(USER_UUID, userId, Hibernate.STRING);
	  			q.setParameter(FRIEND_UUID, friendId, Hibernate.STRING);
	  			q.setMaxResults(1);
	  			return q.uniqueResult();
			}
		};
	
		profileFriend = (ProfileFriend) getHibernateTemplate().execute(hcb);
	
		return profileFriend;
	}
	
	
	//private method to back the search methods. returns only uuids which then need to be checked for privacy settings etc.
	private List<String> findUsersByNameOrEmail(String search) {
		
		//get users from SakaiPerson
		List<String> userUuids = new ArrayList<String>(findSakaiPersonsByNameOrEmail(search));

		//get users from UserDirectoryService
		List<String> usersUuidsFromUserDirectoryService = new ArrayList<String>(sakaiProxy.searchUsers(search));
		
		//combine with no duplicates
		userUuids.removeAll(usersUuidsFromUserDirectoryService);
		userUuids.addAll(usersUuidsFromUserDirectoryService);
		
		return userUuids;
	
	}
	
	
	//private utility method used by findUsersByNameOrEmail() and findUsersByInterest() to format results from
	//the supplied userUuids to SearchResult records based on friend or friendRequest status and the privacy settings for each user
	//that was in the initial search results
	private List<SearchResult> createSearchResultRecordsFromSearch(List<String> userUuids, String userId) {

		List<SearchResult> results = new ArrayList<SearchResult>();
				
		//for each userUuid, is userId a friend?
		//also, get privacy record for the userUuid. if searches not allowed for this user pair, skip to next
		//otherwise create SearchResult record and add to list
		for(Iterator<String> i = userUuids.iterator(); i.hasNext();){
			String userUuid = (String)i.next();
				
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
			
			//is this user visible in searches by this user? if not, skip
			if(!isUserXVisibleInSearchesByUserY(userUuid, privacy, userId, friend)) {
				continue; 
			}
			
			//is profile photo visible to this user
			boolean profileImageAllowed = isUserXProfileImageVisibleByUserY(userUuid, privacy, userId, friend);
			
			//is status visible to this user
			boolean statusVisible = isUserXStatusVisibleByUserY(userUuid, privacy, userId, friend);
			
			//is friends list visible to this user
			boolean friendsListVisible = isUserXFriendsListVisibleByUserY(userUuid, privacy, userId, friend);
			
			
			//make object
			SearchResult searchResult = new SearchResult(
					userUuid,
					friend,
					profileImageAllowed,
					statusVisible,
					friendsListVisible,
					friendRequestToThisPerson,
					friendRequestFromThisPerson
					);
			
			results.add(searchResult);
		}
		
		return results;
	}
	
	
	//init method called when Tomcat starts up
	public void init() {
		
		log.info("Profile2: init()"); //$NON-NLS-1$
		
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
		List<String> allUsers = new ArrayList<String>(listAllSakaiPersons());
		
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

	
	//setup TinyUrlService API
	private TinyUrlService tinyUrlService;
	public void setTinyUrlService(TinyUrlService tinyUrlService) {
		this.tinyUrlService = tinyUrlService;
	}
	
	
	
}
