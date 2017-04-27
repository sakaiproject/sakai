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

import java.util.List;
import java.util.Map;

import org.sakaiproject.profile2.model.BasicConnection;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.user.api.User;

/**
 * An interface for dealing with connections in Profile2
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 * 
 */
public interface ProfileConnectionsLogic {

	/**
	 * Gets a list of BasicOnlinePersons that are connected to this user
	 * 
	 * @param userUuid		uuid of the user to retrieve the list of connections for
	 * @return
	 */
	public List<BasicConnection> getBasicConnectionsForUser(final String userUuid);
	
	/**
	 * Gets a list of Persons that are connected to this user. incl prefs and privacy
	 * 
	 * @param userUuid		uuid of the user to retrieve the list of connections for
	 * @return
	 */
	public List<Person> getConnectionsForUser(final String userUuid);

	/**
	 * Gets a list of Persons that are connected to this user. Current user, prefs
	 * and privacy are skipped.
	 * 
	 * @param userUuid		uuid of the user to retrieve the list of connections for
	 * @return
	 */
	public List<User> getConnectedUsersForUserInsecurely(final String userUuid);

	
	/**
	 * Gets a count of the number of connections a user has.
	 * @param userId		uuid of the user to retrieve the count for
	 * @return
	 */
	public int getConnectionsForUserCount(final String userId);
	
	/**
	 * Gets a list of Persons's that have unconfirmed connection requests to this person
	 * 
	 * @param userId		uuid of the user to retrieve the list of connections for
	 * @return
	 */
	public List<Person> getConnectionRequestsForUser(final String userId);

	/**
	 * Gets a list of Persons's that have unconfirmed connection requests from this person
	 *
	 * @param userId		the user whose outgoing outgoing connections will be retrieved
	 * @return A list of Persons who have had a request from the supplied user
	 */
	public List<Person> getOutgoingConnectionRequestsForUser(final String userId);
	
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
	 * Gets a subset of the connection list, based on the search string matching the beginning of the displayName,
	 * and based on whether that user is allowed to be messaged.
	 * @param connections	list of connections
	 * @param search		search string to match on
	 * @param forMessaging	if this request is for messaging, we also check if the user has indicated they can receive messages
	 * @return
	 */
	public List<Person> getConnectionsSubsetForSearch(List<Person> connections, String search, boolean forMessaging);
	
	/**
	 * Get the connection status between two users. The user making the query must be userA.
	 * @param userA		user making the query	
	 * @param userB		any other user
	 * @return			int signaling the connection status. See ProfileConstants.
	 */
	public int getConnectionStatus(String userA, String userB);
	
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
	 * Is userY a friend of the userX?
	 * 
	 * @param userX			the uuid of the user we are querying
	 * @param userY			current user uuid
	 * @return boolean
	 */
	public boolean isUserXFriendOfUserY(String userX, String userY);
	
	/**
	 * Get a BasicConnection
	 * @param userUuid
	 * @return
	 */
	public BasicConnection getBasicConnection(String userUuid);
	
	/**
	 * Get a BasicConnection
	 * @param user
	 * @return
	 */
	public BasicConnection getBasicConnection(User user);
	
	/**
	 * Get a List of BasicConnections for the given Users.
	 * @param users
	 * @return
	 */
	public List<BasicConnection> getBasicConnections(List<User> users);
	
	/**
	 * Get the online status for a user
	 * @param userUuid	user to check
	 * @return	int of status, according to ProfileConstants.ONLINE_STATUS_x
	 */
	public int getOnlineStatus(String userUuid);
	
	/**
	 * Get the online status for a list of users
	 * @param userUuid	List of users to check
	 * @return	Map of userUuid to the status, according to ProfileConstants.ONLINE_STATUS_x
	 */
	public Map<String, Integer> getOnlineStatus(List<String> userUuids);
	
}
