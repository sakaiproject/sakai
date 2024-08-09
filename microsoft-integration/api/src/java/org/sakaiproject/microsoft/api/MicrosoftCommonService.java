/**
 * Copyright (c) 2024 The Apereo Foundation
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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.sakaiproject.microsoft.api.data.MeetingRecordingData;
import org.sakaiproject.microsoft.api.data.MicrosoftChannel;
import org.sakaiproject.microsoft.api.data.MicrosoftDriveItem;
import org.sakaiproject.microsoft.api.data.MicrosoftDriveItemFilter;
import org.sakaiproject.microsoft.api.data.MicrosoftMembersCollection;
import org.sakaiproject.microsoft.api.data.MicrosoftTeam;
import org.sakaiproject.microsoft.api.data.MicrosoftUser;
import org.sakaiproject.microsoft.api.data.MicrosoftUserIdentifier;
import org.sakaiproject.microsoft.api.data.TeamsMeetingData;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftCredentialsException;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftGenericException;
import org.sakaiproject.microsoft.api.model.SiteSynchronization;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.user.api.User;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MicrosoftCommonService {
	public static final String PERM_VIEW_ALL_CHANNELS = "microsoft.channels.view.all";
	public static final String PERM_CREATE_FILES = "microsoft.documents.create.files";
	public static final String PERM_CREATE_FOLDERS = "microsoft.documents.create.folders";
	public static final String PERM_DELETE_FILES = "microsoft.documents.delete.files";
	public static final String PERM_DELETE_FOLDERS = "microsoft.documents.delete.folders";
	public static final String PERM_UPLOAD_FILES = "microsoft.documents.upload.files";
	public static final int MAX_CHANNELS = 30;
	public static final int MAX_ADD_CHANNELS = 20;

	public static List<User> errorUsers = new ArrayList<>();
	Map<String, Set<User>> groupErrors = new HashMap<>();


	public static enum PermissionRoles { READ, WRITE }
	
	void resetCache();
	void resetTeamsCache();
	void resetDriveItemsCache();
	void resetUserDriveItemsCache(String userId);
	void resetGroupDriveItemsCache(String groupId);
	void resetGraphClient();
	void checkConnection() throws MicrosoftCredentialsException;
	
	// ---------------------------------------- USERS ------------------------------------------------
	List<MicrosoftUser> getUsers() throws MicrosoftCredentialsException;
	List<User> getErrorUsers() throws MicrosoftCredentialsException;
	void addErrorUsers(User user) throws MicrosoftCredentialsException;
	Map<String, Set<User>> getErrorGroupsUsers() throws MicrosoftCredentialsException;
	void addGroupUserErrors(String id, org.sakaiproject.user.api.User user) throws MicrosoftCredentialsException;
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
	Map<String, MicrosoftTeam> getTeamsBySites(List<SiteSynchronization> sites) throws MicrosoftCredentialsException;

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
	List<MicrosoftChannel> createChannels(List<Group> groupsToProcess, String teamId, String ownerEmail) throws MicrosoftCredentialsException, JsonProcessingException;

	String processMicrosoftChannelName(String name);

	String processMicrosoftTeamName(String name);

	boolean deleteChannel(String teamId, String channelId) throws MicrosoftCredentialsException;
	
	MicrosoftMembersCollection getChannelMembers(String teamId, String channelId, MicrosoftUserIdentifier key) throws MicrosoftCredentialsException;
	MicrosoftUser checkUserInChannel(String identifier, String teamId, String channelId, MicrosoftUserIdentifier key) throws MicrosoftCredentialsException;
	
	boolean addMemberToChannel(String userId, String teamId, String channelId) throws MicrosoftCredentialsException;
	boolean addOwnerToChannel(String userId, String teamId, String channelId) throws MicrosoftCredentialsException;
	
	boolean removeMemberFromChannel(String memberId, String teamId, String channelId) throws MicrosoftCredentialsException;
	
	// ---------------------------------------- ONLINE MEETINGS --------------------------------------------------
	TeamsMeetingData createOnlineMeeting(String userEmail, String subject, Instant startDate, Instant endDate) throws MicrosoftCredentialsException;
	void updateOnlineMeeting(String userEmail, String meetingId, String subject, Instant startDate, Instant endDate) throws MicrosoftCredentialsException;
	List<MeetingRecordingData> getOnlineMeetingRecordings(String onlineMeetingId, List<String> teamIdsList, boolean force) throws MicrosoftCredentialsException;
	
	// ---------------------------------------- ONE-DRIVE (APPLICATION) --------------------------------------------------------
	List<MicrosoftDriveItem> getGroupDriveItems(String groupId) throws MicrosoftCredentialsException;
	List<MicrosoftDriveItem> getGroupDriveItems(String groupId, List<String> channelIds) throws MicrosoftCredentialsException;
	List<MicrosoftDriveItem> getGroupDriveItemsByItemId(String groupId, String itemId) throws MicrosoftCredentialsException;
	List<MicrosoftDriveItem> getAllGroupDriveItems(String groupId, List<String> channelIds, MicrosoftDriveItemFilter filter) throws MicrosoftCredentialsException;
	MicrosoftDriveItem getDriveItemFromLink(String link) throws MicrosoftCredentialsException;
	MicrosoftDriveItem getDriveItemFromTeam(String teamId) throws MicrosoftCredentialsException;
	MicrosoftDriveItem getDriveItemFromChannel(String teamId, String channelId) throws MicrosoftCredentialsException;
	boolean grantReadPermissionToTeam(String driveId, String itemId, String teamId) throws MicrosoftCredentialsException;
	String createLinkForTeams(MicrosoftDriveItem item, List<String> teamIds, PermissionRoles role) throws MicrosoftCredentialsException;
	String createLinkForTeams(String driveId, String itemId, List<String> teamIds, PermissionRoles role) throws MicrosoftCredentialsException;
	String getThumbnail(MicrosoftDriveItem item, Integer maxWidth, Integer maxHeight) throws MicrosoftCredentialsException;
	boolean deleteDriveItem(MicrosoftDriveItem item) throws MicrosoftCredentialsException;
	
	// ---------------------------------------- ONE-DRIVE (DELEGATED) --------------------------------------------------------
	List<MicrosoftDriveItem> getMyDriveItems(String userId) throws MicrosoftCredentialsException;
	List<MicrosoftDriveItem> getMyDriveItemsByItemId(String userId, String itemId) throws MicrosoftCredentialsException;
	List<MicrosoftDriveItem> getAllMyDriveItems(String userId, MicrosoftDriveItemFilter filter) throws MicrosoftCredentialsException;
	List<MicrosoftDriveItem> getMySharedDriveItems(String userId) throws MicrosoftCredentialsException;
	List<MicrosoftDriveItem> getAllMySharedDriveItems(String userId, MicrosoftDriveItemFilter filter) throws MicrosoftCredentialsException;
	
	// ---------------------------------------- ONE-DRIVE (MIXED) --------------------------------------------------------
	MicrosoftDriveItem getDriveItem(String driveId, String itemId, String delegatedUserId) throws MicrosoftCredentialsException;
	List<MicrosoftDriveItem> getDriveItems(String driveId, String delegatedUserId) throws MicrosoftCredentialsException;
	List<MicrosoftDriveItem> getDriveItemsByItemId(String driveId, String itemId, String delegatedUserId) throws MicrosoftCredentialsException;
	MicrosoftDriveItem createDriveItem(MicrosoftDriveItem parent, MicrosoftDriveItem.TYPE type, String name, String delegatedUserId) throws MicrosoftCredentialsException;
	MicrosoftDriveItem uploadDriveItem(MicrosoftDriveItem parent, File file, String name, String delegatedUserId) throws MicrosoftCredentialsException;
}
