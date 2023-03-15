/**
* Copyright (c) 2023 Apereo Foundation
* 
* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*             http://opensource.org/licenses/ecl2
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.sakaiproject.microsoft.api;

import java.util.List;
import java.util.Map;

import org.sakaiproject.microsoft.api.data.MicrosoftChannel;
import org.sakaiproject.microsoft.api.data.MicrosoftMembersCollection;
import org.sakaiproject.microsoft.api.data.MicrosoftTeam;
import org.sakaiproject.microsoft.api.data.MicrosoftUser;
import org.sakaiproject.microsoft.api.data.MicrosoftUserIdentifier;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftCredentialsException;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftGenericException;

public interface MicrosoftCommonService {
	void resetCache();
	void resetTeamsCache();
	void resetGraphClient();
	void checkConnection() throws MicrosoftCredentialsException;
	
	// ---------------------------------------- USERS ------------------------------------------------
	List<MicrosoftUser> getUsers() throws MicrosoftCredentialsException;

	MicrosoftUser getUser(String identifier, MicrosoftUserIdentifier key) throws MicrosoftCredentialsException;
	MicrosoftUser getUserById(String id) throws MicrosoftCredentialsException;
	MicrosoftUser getUserByEmail(String email) throws MicrosoftCredentialsException;

	boolean checkUser(String identifier, MicrosoftUserIdentifier key) throws MicrosoftCredentialsException;
	
	// ---------------------------------------- INVITATIONS ------------------------------------------------
	MicrosoftUser createInvitation(String email) throws MicrosoftGenericException;
	MicrosoftUser createInvitation(String email, String redirectURL) throws MicrosoftGenericException;
	
	// ---------------------------------------- TEAMS / GROUPS ------------------------------------------------
	MicrosoftTeam getGroup(String id) throws MicrosoftCredentialsException;
	Map<String, MicrosoftTeam> getTeams() throws MicrosoftCredentialsException;
	Map<String, MicrosoftTeam> getTeams(boolean force) throws MicrosoftCredentialsException;
	MicrosoftTeam getTeam(String id) throws MicrosoftCredentialsException;
	MicrosoftTeam getTeam(String id, boolean force) throws MicrosoftCredentialsException;
	
	String createGroup(String name, String ownerEmail) throws MicrosoftCredentialsException;
	String createTeam(String name, String ownerEmail) throws MicrosoftCredentialsException;
	void createTeamFromGroup(String groupId) throws MicrosoftCredentialsException;
	void createTeamFromGroupAsync(String groupId) throws MicrosoftCredentialsException;
	
	boolean deleteTeam(String teamId) throws MicrosoftCredentialsException;

	MicrosoftMembersCollection getTeamMembers(String id, MicrosoftUserIdentifier key) throws MicrosoftCredentialsException;
	MicrosoftUser checkUserInTeam(String identifier, String teamId, MicrosoftUserIdentifier key) throws MicrosoftCredentialsException;
	
	//boolean addMembersToGroup(List<String> userIds, String groupId) throws MicrosoftCredentialsException;
	boolean addMemberToGroup(String userId, String groupId) throws MicrosoftCredentialsException;
	//Not used by MicrosoftSynchronizationService. 
	//We only add users to a group when they are guest (otherwise they will be added directly to the team).
	//And guest users never can be owners.
	boolean addOwnerToGroup(String userId, String groupId) throws MicrosoftCredentialsException;
	boolean addMemberToTeam(String userId, String teamId) throws MicrosoftCredentialsException;
	boolean addOwnerToTeam(String userId, String teamId) throws MicrosoftCredentialsException;
	
	boolean removeUserFromGroup(String userId, String groupId) throws MicrosoftCredentialsException;
	boolean removeMemberFromTeam(String memberId, String teamId) throws MicrosoftCredentialsException;
	boolean removeAllMembersFromTeam(String teamId) throws MicrosoftCredentialsException;
	
	// ------------------------------------------ CHANNELS ----------------------------------------------------
	MicrosoftChannel getChannel(String teamId, String channelId) throws MicrosoftCredentialsException;
	MicrosoftChannel getChannel(String teamId, String channelId, boolean force) throws MicrosoftCredentialsException;
	Map<String, MicrosoftChannel> getTeamPrivateChannels(String teamId) throws MicrosoftCredentialsException;
	Map<String, MicrosoftChannel> getTeamPrivateChannels(String teamId, boolean force) throws MicrosoftCredentialsException;
	
	String createChannel(String teamId, String name, String ownerEmail) throws MicrosoftCredentialsException;
	
	boolean deleteChannel(String teamId, String channelId) throws MicrosoftCredentialsException;
	
	MicrosoftMembersCollection getChannelMembers(String teamId, String channelId, MicrosoftUserIdentifier key) throws MicrosoftCredentialsException;
	MicrosoftUser checkUserInChannel(String identifier, String teamId, String channelId, MicrosoftUserIdentifier key) throws MicrosoftCredentialsException;
	
	boolean addMemberToChannel(String userId, String teamId, String channelId) throws MicrosoftCredentialsException;
	boolean addOwnerToChannel(String userId, String teamId, String channelId) throws MicrosoftCredentialsException;
	
	boolean removeMemberFromChannel(String memberId, String teamId, String channelId) throws MicrosoftCredentialsException;
}