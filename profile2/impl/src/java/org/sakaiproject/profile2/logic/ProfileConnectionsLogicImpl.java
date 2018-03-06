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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.profile2.cache.CacheManager;
import org.sakaiproject.profile2.dao.ProfileDao;
import org.sakaiproject.profile2.hbm.model.ProfileFriend;
import org.sakaiproject.profile2.model.BasicConnection;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.types.EmailType;
import org.sakaiproject.profile2.types.PrivacyType;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.user.api.User;

/**
 * Implementation of ProfileConnectionsLogic for Profile2.
 * 
 * @author Steve Swinsburg (s.swinsburg@gmail.com)
 *
 */
@Slf4j
public class ProfileConnectionsLogicImpl implements ProfileConnectionsLogic {

	private Cache cache;
	private final String CACHE_NAME = "org.sakaiproject.profile2.cache.connections";
	
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public List<BasicConnection> getBasicConnectionsForUser(final String userUuid) {
		List<User> users = getConnectedUsers(userUuid);
		return getBasicConnections(users);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public List<Person> getConnectionsForUser(final String userUuid) {
		List<User> users = getConnectedUsers(userUuid);
		return profileLogic.getPersons(users);
	}

	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public List<User> getConnectedUsersForUserInsecurely(final String userUuid) {
		return getConnectedUsersInsecurely(userUuid);
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */	
	@Override
	public int getConnectionsForUserCount(final String userId) {
		return getConnectionsForUser(userId).size();
	}
	
	/**
 	 * {@inheritDoc}
 	 */	
	@Override
	public List<Person> getConnectionRequestsForUser(final String userId) {
		List<User> users = sakaiProxy.getUsers(dao.getRequestedConnectionUserIdsForUser(userId));
		return profileLogic.getPersons(users);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Person> getOutgoingConnectionRequestsForUser(final String userId) {

		List<User> users = sakaiProxy.getUsers(dao.getOutgoingConnectionUserIdsForUser(userId));
		return profileLogic.getPersons(users);
	}
	
	/**
 	 * {@inheritDoc}
 	 */	
	@Override
	public int getConnectionRequestsForUserCount(final String userId) {
		return getConnectionRequestsForUser(userId).size();
	}

	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean isUserXFriendOfUserY(String userX, String userY) {
		
		//if same then friends.
		//added this check so we don't need to do it everywhere else and can call isFriend for all user pairs.
		if(StringUtils.equals(userX, userY)) {
			return true;
		}
		
		//get friends of current user
		//TODO change this to be a single lookup rather than iterating over a list
		List<String> friendUuids = getConfirmedConnectionUserIdsForUser(userY);
		
		//if list of confirmed friends contains this user, they are a friend
		if(friendUuids.contains(userX)) {
			return true;
		}
		
		return false;
	}
	

	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public List<Person> getConnectionsSubsetForSearch(List<Person> connections, String search) {
		return getConnectionsSubsetForSearch( connections, search, false);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public List<Person> getConnectionsSubsetForSearch(List<Person> connections, String search, boolean forMessaging) {
		
		List<Person> subList = new ArrayList<Person>();
		
		//check auth and get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if(currentUserUuid == null) {
			throw new SecurityException("You must be logged in to get a connection list subset.");
		}
		
		for(Person p : connections){
			//check for match by name
			if(StringUtils.startsWithIgnoreCase(p.getDisplayName(), search)) {
				
				//if reached max size
				if(subList.size() == ProfileConstants.MAX_CONNECTIONS_PER_SEARCH) {
					break;
				}
				
				//if we need to check messaging privacy setting
				if(forMessaging){
					//if not allowed to be messaged by this user
					if(!privacyLogic.isActionAllowed(p.getUuid(), currentUserUuid, PrivacyType.PRIVACY_OPTION_MESSAGES)){
						continue;
					}
				} 
				
				//all ok, add them to the list
				subList.add(p);
			}
		}
		return subList;
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public int getConnectionStatus(String userA, String userB) {
		
		//current user must be the user making the request
		String currentUserId = sakaiProxy.getCurrentUserId();
		if(!StringUtils.equals(currentUserId, userA)) {
			log.error("User: " + currentUserId + " attempted to get the connection status with " + userB + " on behalf of " + userA);
			throw new SecurityException("You are not authorised to perform that action.");
		}
		
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
	@Override
	public boolean requestFriend(String userId, String friendId) {
		
		if(userId == null || friendId == null){
	  		throw new IllegalArgumentException("Null argument in ProfileLogic.getFriendsForUser"); 
	  	}
		
		//current user must be the user making the request
		String currentUserId = sakaiProxy.getCurrentUserId();
		if(!StringUtils.equals(currentUserId, userId)) {
			log.error("User: " + currentUserId + " attempted to make connection request to " + friendId + " on behalf of " + userId);
			throw new SecurityException("You are not authorised to perform that action.");
		}
		
		//make a ProfileFriend object with 'Friend Request' constructor
		ProfileFriend profileFriend = new ProfileFriend(userId, friendId, ProfileConstants.RELATIONSHIP_FRIEND);
		
		//make the request
		if (dao.addNewConnection(profileFriend)) {
			
			log.info("User: " + userId + " requested friend: " + friendId);  

			sakaiProxy.postEvent(ProfileConstants.EVENT_FRIEND_REQUEST, "/profile/" + friendId, true);

			//send email notification
			sendConnectionEmailNotification(friendId, userId, EmailType.EMAIL_NOTIFICATION_REQUEST);
			
			return true;
		}
		return false;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
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
	@Override
	public boolean confirmFriendRequest(final String fromUser, final String toUser) {
		
		if(fromUser == null || toUser == null){
	  		throw new IllegalArgumentException("Null argument in ProfileLogic.confirmFriendRequest"); 
	  	}
		
		//current user must be the user making the request
		String currentUserId = sakaiProxy.getCurrentUserId();
		if(!StringUtils.equals(currentUserId, toUser)) {
			log.error("User: " + currentUserId + " attempted to confirm connection request from " + fromUser + " on behalf of " + toUser);
			throw new SecurityException("You are not authorised to perform that action.");
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
			sendConnectionEmailNotification(fromUser, toUser, EmailType.EMAIL_NOTIFICATION_CONFIRM);

			//post event
			sakaiProxy.postEvent(ProfileConstants.EVENT_FRIEND_CONFIRM, "/profile/"+fromUser, true);
			
			//invalidate the confirmed connection caches for each user as they are now stale
			this.cacheManager.evictFromCache(this.cache, fromUser);
			this.cacheManager.evictFromCache(this.cache, toUser);
			
			return true;
		} 
		
		return false;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean ignoreFriendRequest(final String fromUser, final String toUser) {
		
		if(fromUser == null || toUser == null){
	  		throw new IllegalArgumentException("Null argument in ProfileLogic.ignoreFriendRequest"); 
	  	}
		
		//current user must be the user making the request or the user receiving it.
		String currentUserId = sakaiProxy.getCurrentUserId();
		if(!(StringUtils.equals(currentUserId, toUser) || StringUtils.equals(currentUserId, fromUser))) {
			log.error("User: " + currentUserId + " attempted to ignore connection request from " + fromUser + " on behalf of " + toUser);
			throw new SecurityException("You are not authorised to perform that action.");
		}

		//get pending ProfileFriend object request for the given details
		ProfileFriend profileFriend = dao.getPendingConnection(fromUser, toUser);

		if(profileFriend == null) {
		    profileFriend = dao.getPendingConnection(toUser, fromUser);
		    if (profileFriend == null) {
				log.error("ProfileLogic.ignoreFriendRequest() failed. No pending friend request from userId: " + fromUser + " to friendId: " + toUser + " found.");
				return false;
			}
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
	@Override
	public boolean removeFriend(String userId, String friendId) {
		
		if(userId == null || friendId == null){
	  		throw new IllegalArgumentException("Null argument in ProfileLogic.removeFriend"); 
	  	}
		
		//current user must be the user making the request
		String currentUserId = sakaiProxy.getCurrentUserId();
		if(!StringUtils.equals(currentUserId, userId)) {
			log.error("User: " + currentUserId + " attempted to remove connection with " + friendId + " on behalf of " + userId);
			throw new SecurityException("You are not authorised to perform that action.");
		}
		
		//get the friend object for this connection pair (could be any way around)
		ProfileFriend profileFriend = dao.getConnectionRecord(userId, friendId);
		
		if(profileFriend == null){
			log.error("ProfileFriend record does not exist for userId: " + userId + ", friendId: " + friendId);  
			return false;
		}
				
		//delete
		if(dao.removeConnection(profileFriend)) {
			log.info("User: " + userId + " removed friend: " + friendId);  
			
			//invalidate the confirmed connection caches for each user as they are now stale
			this.cacheManager.evictFromCache(this.cache, userId);
			this.cacheManager.evictFromCache(this.cache, friendId);
			
			return true;
		}
		return false;
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public BasicConnection getBasicConnection(String userUuid) {
		return getBasicConnection(sakaiProxy.getUserById(userUuid));
	}

	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public BasicConnection getBasicConnection(User user) {
		BasicConnection p = new BasicConnection();
		p.setUuid(user.getId());
		p.setDisplayName(user.getDisplayName());
		p.setType(user.getType());
		p.setOnlineStatus(getOnlineStatus(user.getId()));
		return p;
	}

	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public List<BasicConnection> getBasicConnections(List<User> users) {

		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if (currentUserUuid == null) {
			throw new SecurityException("You must be logged in to get a connection list.");
		}
		
		List<BasicConnection> list = new ArrayList<>();
		
		//get online status
		Map<String,Integer> onlineStatus = getOnlineStatus(sakaiProxy.getUuids(users));
		
		//this is created manually so that we can use the bulk retrieval of the online status method.
		for(User u:users){
			BasicConnection bc = new BasicConnection();
			bc.setUuid(u.getId());
			bc.setDisplayName(u.getDisplayName());
			bc.setEmail(u.getEmail());
			bc.setProfileUrl(linkLogic.getInternalDirectUrlToUserProfile(u.getId()));
			bc.setType(u.getType());
			bc.setOnlineStatus(onlineStatus.get(u.getId()));
			if (privacyLogic.isActionAllowed(u.getId(), currentUserUuid, PrivacyType.PRIVACY_OPTION_SOCIALINFO)) {
				bc.setSocialNetworkingInfo(profileLogic.getSocialNetworkingInfo(u.getId()));
			}
			list.add(bc);
		}
		return list;
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public int getOnlineStatus(String userUuid) {
		
		//TODO check prefs and privacy for the user. has the user allowed it?
		
		
		//check if user has an active session
		boolean active = sakaiProxy.isUserActive(userUuid);
		if(!active) {
			return ProfileConstants.ONLINE_STATUS_OFFLINE;
		}
		
		//if active, when was their last event
		Long lastEventTime = sakaiProxy.getLastEventTimeForUser(userUuid);
		if(lastEventTime == null) {
			return ProfileConstants.ONLINE_STATUS_OFFLINE;
		}
		
		//if time between now and last event is less than the interval, they are online.
		long timeNow = new Date().getTime();
		
		if((timeNow - lastEventTime.longValue()) < ProfileConstants.ONLINE_INACTIVITY_INTERVAL) {
			return ProfileConstants.ONLINE_STATUS_ONLINE;
		}
		
		//user is online but inactive
		return ProfileConstants.ONLINE_STATUS_AWAY;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public Map<String, Integer> getOnlineStatus(List<String> userUuids) {
		
		//get the list of users that have active sessions
		List<String> activeUuids = sakaiProxy.getActiveUsers(userUuids);
		
		//get last event times for the new list
		Map<String, Long> lastEventTimes = sakaiProxy.getLastEventTimeForUsers(activeUuids);

		long timeNow = new Date().getTime();
		
		//iterate over original list, create the map
		Map<String, Integer> map = new HashMap<String, Integer>();
		for(String uuid: userUuids) {
			if(lastEventTimes.containsKey(uuid)) {
				
				//calc time, if less than interval, online, otherwise away
				Long lastEventTime = lastEventTimes.get(uuid);
				if(lastEventTime != null && ((timeNow - lastEventTime.longValue()) < ProfileConstants.ONLINE_INACTIVITY_INTERVAL)) {
					map.put(uuid, ProfileConstants.ONLINE_STATUS_ONLINE);
				} else {
					map.put(uuid, ProfileConstants.ONLINE_STATUS_AWAY);
				}
			} else {
				//no session/no last event time
				map.put(uuid, ProfileConstants.ONLINE_STATUS_OFFLINE);
			}
		}
		return map;
	}
	
	
	
	
	
	/**
	 * Check auth, privacy and get the list of users that are connected to this user.
	 * @param userUuid
	 * @return List<User>, will be empty if none or not allowed.
	 */
	private List<User> getConnectedUsers(final String userUuid) {
		//check auth and get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if(currentUserUuid == null) {
			throw new SecurityException("You must be logged in to get a connection list.");
		}
		
		List<User> users = new ArrayList<>();
		
		//check privacy
		if(!privacyLogic.isActionAllowed(userUuid, currentUserUuid, PrivacyType.PRIVACY_OPTION_MYFRIENDS)) {
			return users;
		}

		users = sakaiProxy.getUsers(getConfirmedConnectionUserIdsForUser(userUuid));
		return users;
	}

	/**
	 * Check auth, privacy and get the list of users that are connected to this user.
	 * @param userUuid
	 * @return List<User>, will be empty if none or not allowed.
	 */
	private List<User> getConnectedUsersInsecurely(final String userUuid) {
		return sakaiProxy.getUsers(getConfirmedConnectionUserIdsForUser(userUuid));
	}
	

	/**
	 * Helper method to get the list of confirmed connections for a user as a List<String> of uuids.
	 * 
	 * <p>First checks the cache and then goes to the dao if necessary.</p>
	 * 
	 * @param userUuid
	 * @return List<String> of uuids, empty if none.
	 */
	private List<String> getConfirmedConnectionUserIdsForUser(final String userUuid) {

		List<String> userUuids = null;
		
		if(cache.containsKey(userUuid)){
			log.debug("Fetching connections from cache for: " + userUuid);
			userUuids = (List<String>)cache.get(userUuid);
			if(userUuids == null) {
				// This means that the cache has expired. evict the key from the cache
				log.debug("Connections cache appears to have expired for " + userUuid);
				this.cacheManager.evictFromCache(this.cache, userUuid);
			}
		}
		if(userUuids == null) {
			userUuids = dao.getConfirmedConnectionUserIdsForUser(userUuid);
			if(userUuids != null){
				log.debug("Adding connections to cache for: " + userUuid);
				cache.put(userUuid, userUuids);
			} else {
				//if it is null we dont want to return it like that
				userUuids = new ArrayList<String>();
			}
		}
		return userUuids;
	}
	
	
	
	
	/**
	 * Sends an email notification to the users. Used for connections. This formats the data and calls {@link SakaiProxy.sendEmail(String userId, String emailTemplateKey, Map<String,String> replacementValues)}
	 * @param toUuid		user to send the message to - this will be formatted depending on their email preferences for this message type so it is safe to pass any users you need
	 * @param fromUuid		uuid from
	 * @param messageType	the message type to send from ProfileConstants. Retrieves the emailTemplateKey based on this value
	 */
	private void sendConnectionEmailNotification(String toUuid, final String fromUuid, final EmailType messageType) {
		//check if email preference enabled
		if(!preferencesLogic.isPreferenceEnabled(toUuid, messageType.toPreference())) {
			return;
		}
		
		//request
		if(messageType == EmailType.EMAIL_NOTIFICATION_REQUEST) {
			
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
		if(messageType == EmailType.EMAIL_NOTIFICATION_CONFIRM) {
			
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
	
	public void init() {
		cache = cacheManager.createCache(CACHE_NAME);
	}
	
	@Setter
	private SakaiProxy sakaiProxy;
	
	@Setter
	private ProfileDao dao;
	
	@Setter
	private ProfileLogic profileLogic;
	
	@Setter
	private ProfileLinkLogic linkLogic;
	
	@Setter
	private ProfilePrivacyLogic privacyLogic;
	
	@Setter
	private ProfilePreferencesLogic preferencesLogic;
	
	@Setter
	private CacheManager cacheManager;

}
