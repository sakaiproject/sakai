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
package org.sakaiproject.microsoft.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.messaging.api.MicrosoftMessage;
import org.sakaiproject.messaging.api.MicrosoftMessage.MicrosoftMessageBuilder;
import org.sakaiproject.messaging.api.MicrosoftMessagingService;
import org.sakaiproject.microsoft.api.MicrosoftAuthorizationService;
import org.sakaiproject.microsoft.api.MicrosoftCommonService;
import org.sakaiproject.microsoft.api.SakaiProxy;
import org.sakaiproject.microsoft.api.data.AttendanceInterval;
import org.sakaiproject.microsoft.api.data.AttendanceRecord;
import org.sakaiproject.microsoft.api.data.MeetingRecordingData;
import org.sakaiproject.microsoft.api.data.MicrosoftChannel;
import org.sakaiproject.microsoft.api.data.MicrosoftCredentials;
import org.sakaiproject.microsoft.api.data.MicrosoftDriveItem;
import org.sakaiproject.microsoft.api.data.MicrosoftDriveItemFilter;
import org.sakaiproject.microsoft.api.data.MicrosoftMembersCollection;
import org.sakaiproject.microsoft.api.data.MicrosoftTeam;
import org.sakaiproject.microsoft.api.data.MicrosoftUser;
import org.sakaiproject.microsoft.api.data.MicrosoftUserIdentifier;
import org.sakaiproject.microsoft.api.data.SynchronizationStatus;
import org.sakaiproject.microsoft.api.data.TeamsMeetingData;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftCredentialsException;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftGenericException;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftInvalidCredentialsException;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftInvalidInvitationException;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftNoCredentialsException;
import org.sakaiproject.microsoft.api.model.GroupSynchronization;
import org.sakaiproject.microsoft.api.model.MicrosoftLog;
import org.sakaiproject.microsoft.api.model.SiteSynchronization;
import org.sakaiproject.microsoft.api.persistence.MicrosoftConfigRepository;
import org.sakaiproject.microsoft.api.persistence.MicrosoftLoggingRepository;
import org.sakaiproject.microsoft.provider.AdminAuthProvider;
import org.sakaiproject.util.api.FormattedText;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.annotation.Transactional;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.core.content.BatchRequestContent;
import com.microsoft.graph.core.content.BatchRequestContentCollection;
import com.microsoft.graph.core.content.BatchResponseContentCollection;
import com.microsoft.graph.core.models.UploadResult;
import com.microsoft.graph.core.tasks.LargeFileUploadTask;
import com.microsoft.graph.drives.item.items.item.children.ChildrenRequestBuilder;
import com.microsoft.graph.drives.item.items.item.createlink.CreateLinkPostRequestBody;
import com.microsoft.graph.drives.item.items.item.createuploadsession.CreateUploadSessionPostRequestBody;
import com.microsoft.graph.drives.item.items.item.invite.InvitePostRequestBody;
import com.microsoft.graph.models.AadUserConversationMember;
import com.microsoft.graph.models.AttendanceRecordCollectionResponse;
import com.microsoft.graph.models.CallRecordingEventMessageDetail;
import com.microsoft.graph.models.CallRecordingStatus;
import com.microsoft.graph.models.Channel;
import com.microsoft.graph.models.ChannelCollectionResponse;
import com.microsoft.graph.models.ChannelMembershipType;
import com.microsoft.graph.models.ChatMessage;
import com.microsoft.graph.models.ChatMessageCollectionResponse;
import com.microsoft.graph.models.ConversationMember;
import com.microsoft.graph.models.ConversationMemberCollectionResponse;
import com.microsoft.graph.models.DirectoryObject;
import com.microsoft.graph.models.DirectoryObjectCollectionResponse;
import com.microsoft.graph.models.Drive;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.models.DriveItemCollectionResponse;
import com.microsoft.graph.models.DriveItemUploadableProperties;
import com.microsoft.graph.models.DriveRecipient;
import com.microsoft.graph.models.Folder;
import com.microsoft.graph.models.Group;
import com.microsoft.graph.models.GroupCollectionResponse;
import com.microsoft.graph.models.Identity;
import com.microsoft.graph.models.IdentitySet;
import com.microsoft.graph.models.Invitation;
import com.microsoft.graph.models.LobbyBypassScope;
import com.microsoft.graph.models.LobbyBypassSettings;
import com.microsoft.graph.models.MeetingAttendanceReportCollectionResponse;
import com.microsoft.graph.models.MeetingChatMode;
import com.microsoft.graph.models.MeetingParticipantInfo;
import com.microsoft.graph.models.MeetingParticipants;
import com.microsoft.graph.models.OnlineMeeting;
import com.microsoft.graph.models.OnlineMeetingPresenters;
import com.microsoft.graph.models.OnlineMeetingRole;
import com.microsoft.graph.models.Permission;
import com.microsoft.graph.models.ReferenceCreate;
import com.microsoft.graph.models.Site;
import com.microsoft.graph.models.Team;
import com.microsoft.graph.models.TeamVisibilityType;
import com.microsoft.graph.models.ThumbnailSet;
import com.microsoft.graph.models.UploadSession;
import com.microsoft.graph.models.User;
import com.microsoft.graph.models.UserCollectionResponse;
import com.microsoft.graph.models.odataerrors.ODataError;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.graph.shares.item.permission.grant.GrantPostRequestBody;
import com.microsoft.graph.teams.item.archive.ArchivePostRequestBody;
import com.microsoft.kiota.ApiException;
import com.microsoft.kiota.HttpMethod;
import com.microsoft.kiota.RequestInformation;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;

@Slf4j
@Transactional
public class MicrosoftCommonServiceImpl implements MicrosoftCommonService {

	private static final String CACHE_NAME = MicrosoftCommonServiceImpl.class.getName() + "_cache";
	private static final String CACHE_TEAMS = "key::teams";
	private static final String CACHE_CHANNELS = "key::channels::";
	private static final String CACHE_RECORDINGS = "key::recordings::";
	private static final String CACHE_DRIVE_ITEMS = "key::driveitems::";
	private static final String CACHE_DRIVE_ITEMS_USER = "key::driveitems-user::";
	private static final String CACHE_DRIVE_ITEMS_GROUP = "key::driveitems-group::";
	private static final String CACHE_MEMBERS = "key::members::";
	private static final String CACHE_USERS = "key::users::";
	private static final String CACHE_INVITATION_USER = "key::invitations::";
	private static final String PERMISSION_READ = "read";
	private static final String PERMISSION_WRITE = "write";
	private static final String LINK_TYPE_EDIT = "edit";
	private static final String LINK_TYPE_VIEW = "view";
	private static final String LINK_SCOPE_USERS = "users";
	public static int COLUMN_SIZE = 7;
	private static final int TEAM_CHARACTER_LIMIT = 256;// 256 is the maximum length for a Team name in the UI, no limits specified on the API docs
	private static final int CHANNEL_CHARACTER_LIMIT = 50;// this is an official restriction
	private final int MAX_RETRY = 2;
	private final int MAX_PER_REQUEST = 20;
	private final int MAX_LENGTH = 20;

	@Setter private CacheManager cacheManager;
	@Setter private FunctionManager functionManager;
	@Setter private MicrosoftAuthorizationService microsoftAuthorizationService;
	@Setter private MicrosoftConfigRepository microsoftConfigRepository;
	@Setter private MicrosoftLoggingRepository microsoftLoggingRepository;
	@Setter private MicrosoftMessagingService microsoftMessagingService;
	@Setter private SakaiProxy sakaiProxy;
	@Setter private FormattedText formattedText;

	private Cache cache = null;
	private GraphServiceClient graphClient = null;
	final private Object lock = new Object();

	public void init() {
		// register functions
		functionManager.registerFunction(PERM_VIEW_ALL_CHANNELS, true);
		functionManager.registerFunction(PERM_CREATE_FILES, true);
		functionManager.registerFunction(PERM_CREATE_FOLDERS, true);
		functionManager.registerFunction(PERM_DELETE_FILES, true);
		functionManager.registerFunction(PERM_DELETE_FOLDERS, true);
		functionManager.registerFunction(PERM_UPLOAD_FILES, true);
	}
	
	private Cache getCache() {
		if(cache == null) {
			cache = cacheManager.getCache(CACHE_NAME);
		}
		return cache;
	}

	private GraphServiceClient getGraphClient() throws MicrosoftCredentialsException {
		if(this.graphClient == null) {
			MicrosoftCredentials microsoftCredentials = microsoftConfigRepository.getCredentials();
			if(microsoftCredentials.hasValue()) {
				try {
					log.debug(MicrosoftCredentials.KEY_CLIENT_ID+"="+microsoftCredentials.getClientId());
					log.debug(MicrosoftCredentials.KEY_SECRET+"="+microsoftCredentials.getSecret());
					log.debug(MicrosoftCredentials.KEY_SCOPE+"="+microsoftCredentials.getScope());
					log.debug(MicrosoftCredentials.KEY_AUTHORITY+"="+microsoftCredentials.getAuthority());

					TokenCredential credential = new ClientSecretCredentialBuilder()
							.authorityHost(microsoftCredentials.getAuthority())
							.tenantId(microsoftCredentials.getTenantId())
							.clientId(microsoftCredentials.getClientId())
							.clientSecret(microsoftCredentials.getSecret())
							.build();
					this.graphClient = new GraphServiceClient(credential, microsoftCredentials.getScope());

					//validate credentials (we don't care about the result)
					this.graphClient.organization().get();
				} catch(IllegalArgumentException e) {
					this.graphClient = null;
					throw new MicrosoftInvalidCredentialsException();
				} catch(Exception e) {
					log.error("Unexpected error: {}", e.getMessage());
				}
			} else {
				throw new MicrosoftNoCredentialsException();
			}
		}
		return this.graphClient;
	}
	
	@Override
	public void checkConnection() throws MicrosoftCredentialsException {
		getGraphClient();
	}
	
	@Override
	public void resetCache() {
		getCache().invalidate();
	}
	
	@Override
	public void resetTeamsCache() {
		getCache().evictIfPresent(CACHE_TEAMS);
	}
	
	@Override
	public void resetDriveItemsCache() {
		getCache().evictIfPresent(CACHE_DRIVE_ITEMS);
	}
	
	@Override
	public void resetUserDriveItemsCache(String userId) {
		getCache().evictIfPresent(CACHE_DRIVE_ITEMS_USER + userId);
	}
	
	@Override
	public void resetGroupDriveItemsCache(String groupId) {
		getCache().evictIfPresent(CACHE_DRIVE_ITEMS_GROUP + groupId);
	}

	@Override
	public void resetGraphClient() {
		this.graphClient = null;
		resetCache();
	}

	// ---------------------------------------- USERS ------------------------------------------------
	@Override
	public List<MicrosoftUser> getUsers() throws MicrosoftCredentialsException {
		List<MicrosoftUser> userList = new ArrayList<>();

		UserCollectionResponse page = getGraphClient().users()
				.get(requestConfig -> {
					requestConfig.queryParameters.select = new String[]{"id","displayName","mail","userType"};
				});

		while (page != null && page.getValue() != null) {
			page.getValue().stream()
					.map(u -> MicrosoftUser.builder()
							.id(u.getId())
							.name(u.getDisplayName())
							.email(u.getMail())
							.guest(MicrosoftUser.GUEST.equalsIgnoreCase(u.getUserType()))
							.build())
					.forEach(u -> {
						userList.add(u);
						log.debug(u.toString());
					});

			String nextLink = page.getOdataNextLink();
			if (nextLink == null) break;

			page = getGraphClient().users()
					.withUrl(nextLink)
					.get();
		}

		HashMap<String, MicrosoftUser> userMap = (HashMap<String, MicrosoftUser>) userList.stream()
				.collect(Collectors.toMap(MicrosoftUser::getId, u -> u));
		getCache().put(CACHE_USERS, userMap);

		return userList;
	}

	@Override
	public Map<String, Set<org.sakaiproject.user.api.User>> getErrorUsers() {
		return errorUsers;
	}

	@Override
	public void addErrorUsers(String id, org.sakaiproject.user.api.User user) {
		if (id != null && user != null) {
			Set<org.sakaiproject.user.api.User> existingUsers = errorUsers.computeIfAbsent(id, k -> new HashSet<>());
			existingUsers.add(user);
		}
	}

	@Override
	public Map<String, Set<org.sakaiproject.user.api.User>> getErrorGroupsUsers() {
		return groupErrors;
	}

	@Override
	public void addGroupUserErrors(String id, org.sakaiproject.user.api.User user) {
		if (id != null && user != null) {
			Set<org.sakaiproject.user.api.User> existingUsers = groupErrors.computeIfAbsent(id, k -> new HashSet<>());
			existingUsers.add(user);
		}
	}

	@Override
	public void clearErrorUsers(String id) {
		errorUsers.remove(id);
	}

	@Override
	public void clearErrorGroupsUsers(String id) {
		groupErrors.remove(id);
	}

	@Override
	public boolean checkUser(String identifier, MicrosoftUserIdentifier key) throws MicrosoftCredentialsException {
		return getUser(identifier, key) != null;
	}
	
	@Override
	public MicrosoftUser getUser(String identifier, MicrosoftUserIdentifier key) throws MicrosoftCredentialsException {
		switch(key) {
			case USER_ID:
				return getUserById(identifier);

			case EMAIL:
				return getUserByEmail(identifier);

			default:
				return null;
		}
	}
	
	@Override
	public MicrosoftUser getUserByEmail(String email) throws MicrosoftCredentialsException {
		try {
			Map<String, MicrosoftUser> usersMap;
			Cache.ValueWrapper cachedValue = getCache().get(CACHE_USERS);
			if (cachedValue != null) {
				usersMap = (Map<String, MicrosoftUser>) cachedValue.get();
				for (MicrosoftUser user : usersMap.values()) {
					if (user.getEmail().toLowerCase().equals(email)) {
						return user;
					}
				}
			} else {
				usersMap = new HashMap<>();
			}
			UserCollectionResponse pageCollection = getGraphClient().users()
					.get(requestConfig -> {
						requestConfig.queryParameters.filter = "mail eq '" + email + "'";
						requestConfig.queryParameters.select = new String[]{"id", "displayName", "mail", "userType"};
					});
			if (pageCollection != null && pageCollection.getValue().size() > 0) {
				User u = pageCollection.getValue().get(0);
				MicrosoftUser microsoftUser = MicrosoftUser.builder()
						.id(u.getId())
						.name(u.getDisplayName())
						.email(u.getMail())
						.guest(MicrosoftUser.GUEST.equalsIgnoreCase(u.getUserType()))
						.build();

				usersMap.put(microsoftUser.getId(), microsoftUser);
				getCache().put(CACHE_USERS, usersMap);
				return microsoftUser;
			}
		}catch(MicrosoftCredentialsException e) {
			throw e;
		}catch(Exception e) {
			log.debug("Microsoft User not found with email={}", email);
		}
		return null;
	}

	@Override
	public List<MicrosoftUser> getUsersById(Set<String> userIds) throws MicrosoftCredentialsException {
		List<MicrosoftUser> users = new ArrayList<>();

		//get from cache
		Map<String, MicrosoftUser> usersMap = new HashMap<>();
		Cache.ValueWrapper cachedValue = getCache().get(CACHE_USERS);
		if (cachedValue != null) {
			usersMap = (Map<String, MicrosoftUser>) cachedValue.get();
			for (MicrosoftUser user : usersMap.values()) {
				if (userIds.contains(user.getId().toLowerCase())) {
					users.add(user);
				}
			}
		}

		int pointer = 0;
		GraphServiceClient graph = getGraphClient();
		Set<String> pendingUsers = userIds.stream().filter(id -> !id.startsWith("EMPTY_") && users.stream().noneMatch(u -> u.getId().equalsIgnoreCase(id))).collect(Collectors.toSet());
		List<String> usersToProcess;
		//sometimes microsoft fails creating -> loop for retry failed ones
		while (!pendingUsers.isEmpty()) {
			usersToProcess = pendingUsers.stream().skip(pointer).limit(MAX_LENGTH).collect(Collectors.toList());

			BatchRequestContentCollection batchRequestContent = new BatchRequestContentCollection(getGraphClient());

			usersToProcess.forEach(id -> {
				RequestInformation getUsers = graph.users()
						.toGetRequestInformation(requestConfig -> {
							requestConfig.queryParameters.filter = "userId eq '" + id + "'";
							requestConfig.queryParameters.select = new String[]{"id,displayName,mail,userType"};
							requestConfig.headers.add("ConsistencyLevel", "eventual");
						});

				batchRequestContent.addBatchRequestStep(getUsers);
			});

			BatchResponseContentCollection responseContent = null;
			try {
				responseContent = getGraphClient()
						.getBatchRequestBuilder()
						.post(batchRequestContent, null);
			} catch (ApiException | IOException e) {
				log.debug("Error getting users ", e);
				pendingUsers.removeAll(usersToProcess);
			}

			HashMap<String, ?> teamsResponse = parseBatchResponse(responseContent, usersToProcess);

			users.addAll((List<MicrosoftUser>) teamsResponse.get("success"));
			pendingUsers.removeAll(usersToProcess);
		}

		//store in cache
		usersMap.putAll(users.stream().collect(Collectors.toMap(MicrosoftUser::getId, u -> u)));
		getCache().put(CACHE_USERS, usersMap);

		return users;
	}

	@Override
	public List<MicrosoftUser> getUsers(Set<String> ids, MicrosoftUserIdentifier mappedMicrosoftUserId) throws MicrosoftCredentialsException {
		switch (mappedMicrosoftUserId) {
			case USER_ID:
				return getUsersById(ids);
			case EMAIL:
				return getUsersByEmail(ids);
			default:
				return null;
		}
	}

	@Override
	public List<MicrosoftUser> getUsersByEmail(Set<String> userEmails) throws MicrosoftCredentialsException {
		List<MicrosoftUser> users = new ArrayList<>();

		//get from cache
		Map<String, MicrosoftUser> usersMap = new HashMap<>();
		Cache.ValueWrapper cachedValue = getCache().get(CACHE_USERS);
		if (cachedValue != null) {
			usersMap = (Map<String, MicrosoftUser>) cachedValue.get();
			for (MicrosoftUser user : usersMap.values()) {
				if (userEmails.contains(user.getEmail().toLowerCase())) {
					users.add(user);
				}
			}
		}

		int pointer = 0;
		GraphServiceClient graph = getGraphClient();
		Set<String> pendingUsers = userEmails.stream().filter(email -> !email.startsWith("EMPTY_") && users.stream().noneMatch(u -> u.getEmail().equalsIgnoreCase(email))).collect(Collectors.toSet());
		List<String> usersToProcess;
		//sometimes microsoft fails creating -> loop for retry failed ones
		while (!pendingUsers.isEmpty()) {
			usersToProcess = pendingUsers.stream().skip(pointer).limit(MAX_LENGTH).collect(Collectors.toList());

			BatchRequestContentCollection batchRequestContent = new BatchRequestContentCollection(getGraphClient());

			usersToProcess.forEach(email -> {
				RequestInformation getUsers = graph.users()
						.toGetRequestInformation(requestConfig -> {
							requestConfig.queryParameters.filter = "mail eq '" + email + "'";
							requestConfig.queryParameters.select = new String[]{"id,displayName,mail,userType"};
							requestConfig.headers.add("ConsistencyLevel", "eventual");
						});

				batchRequestContent.addBatchRequestStep(getUsers);
			});

			try {
				BatchResponseContentCollection responseContent = getGraphClient()
						.getBatchRequestBuilder()
						.post(batchRequestContent, null);

				HashMap<String, ?> teamsResponse = parseBatchResponse(responseContent, usersToProcess);
				users.addAll((List<MicrosoftUser>) teamsResponse.get("success"));
				pendingUsers.removeAll(usersToProcess);
			} catch (ApiException | IOException e) {
				log.debug("Error getting users by email ", e);
				pendingUsers.removeAll(usersToProcess);
			}
		}

		//store in cache
		usersMap.putAll(users.stream().collect(Collectors.toMap(MicrosoftUser::getId, u -> u)));
		getCache().put(CACHE_USERS, usersMap);

		return users;
	}

	@Override
	public MicrosoftUser getUserById(String id) throws MicrosoftCredentialsException {
		try {
			Map<String, MicrosoftUser> usersMap;
			Cache.ValueWrapper cachedValue = getCache().get(CACHE_USERS);
			if (cachedValue != null) {
				usersMap = (Map<String, MicrosoftUser>) cachedValue.get();
				if (usersMap.containsKey(id)) {
					return usersMap.get(id);
				}
			} else {
				usersMap = new HashMap<>();
			}

			User u = getGraphClient().users().byUserId(id)
					.get(requestConfig -> {
						requestConfig.queryParameters.select = new String[]{"id", "displayName", "mail", "userType"};
					});
			MicrosoftUser microsoftUser = MicrosoftUser.builder()
					.id(u.getId())
					.name(u.getDisplayName())
					.email(u.getMail())
					.guest(MicrosoftUser.GUEST.equalsIgnoreCase(u.getUserType()))
					.build();

			usersMap.put(id, microsoftUser);
			getCache().put(CACHE_USERS, usersMap);

			return microsoftUser;
		} catch (MicrosoftCredentialsException e) {
			throw e;
		}catch(Exception e) {
			log.debug("Microsoft User not found with id={}", id);
		}
		return null;

	}
	
	// ---------------------------------------- INVITATIONS ------------------------------------------------
	@Override
	public MicrosoftUser createInvitation(String email) throws MicrosoftGenericException {
		return createInvitation(email, "https://teams.microsoft.com");
	}
	
	@Override
	public MicrosoftUser createInvitation(String email, String redirectURL) throws MicrosoftGenericException {
		try {
			Map<String, MicrosoftUser> userInvitationMap;
			Cache.ValueWrapper cachedValue = getCache().get(CACHE_INVITATION_USER);
			if (cachedValue != null) {
				userInvitationMap = (Map<String, MicrosoftUser>) cachedValue.get();
				for (MicrosoftUser user : userInvitationMap.values()) {
					if (user.getEmail().toLowerCase().equals(email)) {
						return user;
					}
				}
			} else {
				userInvitationMap = new HashMap<>();
			}
			log.debug("CREATE INVITATION: email={}, redirectURL={}", email, redirectURL);
			if ("true".equals(microsoftConfigRepository.getConfigItemValueByKey("DEBUG"))) {
				return MicrosoftUser.builder()
						.id("NEW-ID")
						.email(email)
						.guest(true)
						.build();
			}

			Invitation invitation = new Invitation();
			invitation.setInvitedUserEmailAddress(email);
			invitation.setInviteRedirectUrl(redirectURL);

			Invitation response = getGraphClient().invitations()
					.post(invitation);

			log.debug("INVITATION RESPONSE: id={}, userId={}, email={}, status={}", response.getId(), response.getInvitedUser().getId(), response.getInvitedUserEmailAddress(), response.getStatus());

			MicrosoftUser microsoftUser = MicrosoftUser.builder()
					.id(response.getInvitedUser().getId())
					.email(response.getInvitedUserEmailAddress())
					.guest(true)
					.build();

			userInvitationMap.put(microsoftUser.getId(), microsoftUser);
			userInvitationMap.put(CACHE_INVITATION_USER, microsoftUser);
			return microsoftUser;
		} catch (MicrosoftCredentialsException e) {
			throw e;
		}catch(Exception e) {
			log.error("Microsoft guest invitation failed for email={}", email);
			throw new MicrosoftInvalidInvitationException();
		}
	}

	
	// ---------------------------------------- TEAMS / GROUPS ------------------------------------------------
	@Override
	public Map<String, MicrosoftTeam> retrieveCacheTeams() throws MicrosoftCredentialsException {
		Cache.ValueWrapper cachedValue = getCache().get(CACHE_TEAMS);
		if (cachedValue != null) {
			return (Map<String, MicrosoftTeam>) cachedValue.get();
		}
		return new HashMap<>();
	}

	@Override
	public MicrosoftTeam getGroup(String id) throws MicrosoftCredentialsException {
		try {
			Group group = getGraphClient()
					.groups()
					.byGroupId(id)
					.get(requestConfig -> {
						requestConfig.queryParameters.select = new String[]{"id", "displayName", "description"};
					});
			return MicrosoftTeam.builder()
					.id(group.getId())
					.name(group.getDisplayName())
					.description(group.getDescription())
					.build();
		}catch(MicrosoftCredentialsException e) {
			throw e;
		}catch(Exception e) {
			log.debug("Microsoft Group not found with id={}", id);
		}
		return null;
	}
	
	@Override
	public MicrosoftTeam getTeam(String id) throws MicrosoftCredentialsException {
		return getTeam(id, false);
	}
	
	@Override
	public MicrosoftTeam getTeam(String id, boolean force) throws MicrosoftCredentialsException {
		Cache.ValueWrapper cachedValue = getCache().get(CACHE_TEAMS);

		try {
			//get from cache (if not force)
			if (!force) {
				if (cachedValue != null) {
					Map<String, MicrosoftTeam> map = (Map<String, MicrosoftTeam>) cachedValue.get();
					if (map.containsKey(id)) {
						return map.get(id);
					}
				}
			}
			//get from Microsoft
			Team team = getGraphClient()
						.groups()
						.byGroupId(id)
						.team()
						.get(requestConfig -> {
							requestConfig.queryParameters.select = new String[]{"id", "displayName", "description"};
						});
			MicrosoftTeam mt = MicrosoftTeam.builder()
					.id(team.getId())
					.name(team.getDisplayName())
					.description(team.getDescription())
					.build();
			
			//update cache
			Map<String, MicrosoftTeam> teamsMap = cachedValue != null ? (Map<String, MicrosoftTeam>) cachedValue.get() : new HashMap<>();
			teamsMap.put(id, mt);
			getCache().put(CACHE_TEAMS, teamsMap);
			return mt;
		}catch(MicrosoftCredentialsException e) {
			throw e;
		} catch (ApiException e) {
			Map<String, MicrosoftTeam> teamsMap = cachedValue != null ? (Map<String, MicrosoftTeam>) cachedValue.get() : new HashMap<>();
			teamsMap.remove(id);
			getCache().put(CACHE_TEAMS, teamsMap);
		} catch (Exception e) {
			log.debug("Microsoft Team not found with id={}", id);
		}
		return null;
	}
	
	@Override
	public Map<String, MicrosoftTeam> getTeams() throws MicrosoftCredentialsException {
		return getTeams(false);
	}
	
	@Override
	public Map<String, MicrosoftTeam> getTeams(boolean force) throws MicrosoftCredentialsException {
		//get from cache (if not force)
		if(!force) {
			Cache.ValueWrapper cachedValue = getCache().get(CACHE_TEAMS);
			if(cachedValue != null) {
				return (Map<String, MicrosoftTeam>)cachedValue.get();
			}
		}
		Map<String, MicrosoftTeam> teamsMap = new HashMap<>();

		//Get all groups linked with a team
		GroupCollectionResponse page = getGraphClient().groups()
				.get(requestConfig -> {
					requestConfig.headers.add("ConsistencyLevel", "eventual");
					requestConfig.queryParameters.filter = "resourceProvisioningOptions/Any(x:x eq 'Team')";
					requestConfig.queryParameters.select = new String[]{"id", "displayName", "description"};
				});
		while (page != null) {
			for(Group group : page.getValue()) {
				//as we only want id and name, with group info is enough. Otherwise we would need to do a request to get the Team info
				teamsMap.put(group.getId(), MicrosoftTeam.builder()
						.id(group.getId())
						.name(group.getDisplayName())
						.description(group.getDescription())
						.build());
			}
			String nextLink = page.getOdataNextLink();
			if (nextLink == null) break;

			page = getGraphClient().groups()
					.withUrl(nextLink)
					.get(requestConfig -> {
						requestConfig.headers.add("ConsistencyLevel", "eventual");
					});
		}
		
		//store in cache
		getCache().put(CACHE_TEAMS, teamsMap);

		return teamsMap;
	}

	@Override
	public Map<String, MicrosoftTeam> getTeamsBySites(List<SiteSynchronization> sites) throws MicrosoftCredentialsException {
		Map<String, MicrosoftTeam> teamsMap = new HashMap<>();
		List<SiteSynchronization> pendingSites = new ArrayList<>();

		//get from cache
		Cache.ValueWrapper cachedValue = getCache().get(CACHE_TEAMS);
		if (cachedValue != null) {
			Map<String, MicrosoftTeam> teams = (Map<String, MicrosoftTeam>) cachedValue.get();
			sites.forEach(s -> {
				if (teams.containsKey(s.getTeamId())) {
					teamsMap.put(s.getTeamId(), teams.get(s.getTeamId()));
				} else {
					pendingSites.add(s);
				}
			});
		} else {
			pendingSites.addAll(sites);
		}

		if (pendingSites.isEmpty()) {
			return teamsMap;
		}

		int pointer = 0;
		GraphServiceClient graph = getGraphClient();
		List<SiteSynchronization> sitesToProcess;

		//sometimes microsoft fails creating -> loop for retry failed ones
		while (!pendingSites.isEmpty()) {
			int endIndex = Math.min(pointer + MAX_LENGTH, pendingSites.size());
			sitesToProcess = pendingSites.subList(pointer, endIndex);

			BatchRequestContentCollection batchRequestContent = new BatchRequestContentCollection(getGraphClient());

			sitesToProcess.forEach(group -> {
				RequestInformation getGroups = graph.groups().byGroupId(group.getTeamId())
						.toGetRequestInformation(requestConfig -> {
							requestConfig.queryParameters.select = new String[]{"id,displayName,description"};
							requestConfig.headers.add("ConsistencyLevel", "eventual");
						});

				batchRequestContent.addBatchRequestStep(getGroups);
			});

			try {
				BatchResponseContentCollection responseContent = getGraphClient()
						.getBatchRequestBuilder()
						.post(batchRequestContent, null);

				HashMap<String, ?> teamsResponse = parseBatchResponse(responseContent, sitesToProcess);
				teamsMap.putAll((HashMap<String, MicrosoftTeam>) teamsResponse.get("success"));
				pendingSites.removeAll(sitesToProcess);
				pendingSites.addAll((List<SiteSynchronization>) teamsResponse.get("pending"));
			} catch (ApiException | IOException e) {
				log.debug("Error getting teams by sites ", e);
				pendingSites.removeAll(sitesToProcess);
			}
		}

		//store in cache
		getCache().putIfAbsent(CACHE_TEAMS, teamsMap);

		return teamsMap;
	}

	public String createTeam_old(String name, String ownerEmail) throws MicrosoftCredentialsException {
		String groupId = createGroup(name, ownerEmail);
		if(groupId != null) {
			//get Teams cache
			Cache.ValueWrapper cachedValue = getCache().get(CACHE_TEAMS);
			if(cachedValue != null) {
				//add newly created group to the cached map
				Map<String, MicrosoftTeam> teamsMap = (Map<String, MicrosoftTeam>)cachedValue.get();
				teamsMap.put(groupId, MicrosoftTeam.builder()
						.id(groupId)
						.name(name)
						.description(name)
						.fullyCreated(false) //set as partially created
						.build());
				
				getCache().put(CACHE_TEAMS, teamsMap);
			}
	
			createTeamFromGroupAsync(groupId);
		}

		return groupId;
	}
	
	@Override
	public String createTeam(String name, String ownerEmail) throws MicrosoftCredentialsException {
		try {
			String truncatedName = processMicrosoftTeamName(name);
			
			// 1. Check if a Team with the same name already exists (to avoid creating duplicates when the method is called several times with the same name in a short period of time, before the cache is updated with the new created Team)
			Cache.ValueWrapper cachedValue = getCache().get(CACHE_TEAMS);
			if (cachedValue != null) {
				Map<String, MicrosoftTeam> teamsMap = (Map<String, MicrosoftTeam>) cachedValue.get();
				if (teamsMap != null) {
					Optional<MicrosoftTeam> existing = teamsMap.values().stream()
							.filter(t -> t.getName().equalsIgnoreCase(truncatedName))
							.findFirst();
					if (existing.isPresent()) {
						log.debug("Team already exists in cache, returning existing id={}", existing.get().getId());
						return existing.get().getId();
					}
				}
			}
			
			// 2. Create the group (returns ID immediately)
			String groupId = createGroup(truncatedName, ownerEmail);
			if (groupId == null) {
				log.debug("Error creating Microsoft Team: could not create group for name={}", name);
				return null;
			}
			
			// 3. Convert the group into a Team (synchronous, returns Team directly)
			Team team = new Team();
			team.setVisibility(TeamVisibilityType.Private);
			
			int attempts = 0;
			boolean teamCreated = false;
			while (attempts < MAX_RETRY && !teamCreated) {
				try {
					getGraphClient().groups().byGroupId(groupId).team().put(team);
					teamCreated = true;
				} catch (ODataError e) {
					attempts++;
					log.debug("Group not yet provisioned for groupId={}, attempt {}/{} - code: {}",
							groupId, attempts, MAX_RETRY, e.getError().getCode());
					if (attempts < MAX_RETRY) {
						try {
							Thread.sleep(5000L * attempts);
						} catch (InterruptedException ie) {
							Thread.currentThread().interrupt();
							break;
						}
					}
				}
			}
			
			if (!teamCreated) {
				log.error("Could not convert group to Team after {} attempts, groupId={}", MAX_RETRY, groupId);
				return null;
			}
			
			// 4. Update cache
			if (cachedValue != null) {
				Map<String, MicrosoftTeam> teamsMap = (Map<String, MicrosoftTeam>) cachedValue.get();
				teamsMap.put(groupId, MicrosoftTeam.builder()
						.id(groupId)
						.name(name)
						.description(name)
						.build());
				getCache().put(CACHE_TEAMS, teamsMap);
			}
			
			return groupId;
		} catch(MicrosoftCredentialsException e) {
			throw e;
		} catch(Exception ex){
			log.error("Error creating Microsoft Team: name={}", name);
		}
		return null;
	}
	
	@Override
	public String createGroup(String name, String ownerEmail) throws MicrosoftCredentialsException {
		try {
			Group group = new Group();
			group.setDisplayName(name);
			group.setDescription(name);
			LinkedList<String> groupTypesList = new LinkedList<String>();
			groupTypesList.add("Unified");
			group.setGroupTypes(groupTypesList);
			group.setMailEnabled(true);
			group.setMailNickname(RandomStringUtils.randomAlphanumeric(64));
			group.setSecurityEnabled(false);
			
			User userOwner = getGraphClient().users().byUserId(ownerEmail).get();
			List<String> ids = Arrays.asList("https://graph.microsoft.com/v1.0/users('" + userOwner.getId() + "')");
			group.getAdditionalData().put("owners@odata.bind", ids);

			Group newGroup = getGraphClient().groups()
				.post(group);

			return newGroup.getId();
		}catch(MicrosoftCredentialsException e) {
			throw e;
		} catch(Exception ex){
			log.debug("Error creating Microsoft group: name={}", name);
		}
		return null;
	}
	
	@Override
	public void createTeamFromGroup(String groupId) throws MicrosoftCredentialsException {
		Team team = new Team();
		
		Map<String, Object> additionalData = new HashMap<>();
		additionalData.put("template@odata.bind", "https://graph.microsoft.com/v1.0/teamsTemplates('standard')");
		additionalData.put("group@odata.bind", "https://graph.microsoft.com/v1.0/groups('" + groupId + "')");
		team.setAdditionalData(additionalData);
		
		getGraphClient().teams()
				.post(team);
	}
	
	@Override
	public void createTeamFromGroupAsync(String groupId) throws MicrosoftCredentialsException {
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		
		AtomicInteger counter = new AtomicInteger(1);
		final int MAX_RETRY = 5;
		
		MicrosoftMessageBuilder builder = MicrosoftMessage.builder();
		builder.action(MicrosoftMessage.Action.CREATE)
			.type(MicrosoftMessage.Type.TEAM)
			.reference(groupId);
		
		Runnable task = new Runnable() {
			@Override
			public void run () {
				synchronized(lock){
					try {
						log.debug("Attempt number: {}", counter.get());
						
						//check if Team does not exist (always ask microsoft, do not use the cache)
						if(getTeam(groupId, true) == null) {
							//create it
							createTeamFromGroup(groupId);
						}
						
						//send message to (ignite) MicrosoftMessagingService (wait 30 sec before sending the message)
						Thread.sleep(30000);
						microsoftMessagingService.send(MicrosoftMessage.Topic.TEAM_CREATION, builder.status(1).build());
						
						executor.shutdown();
					} catch(MicrosoftCredentialsException e) {
						log.error("Error creating Team (credentials): " + e.getMessage());
						//send message to (ignite) MicrosoftMessagingService
						microsoftMessagingService.send(MicrosoftMessage.Topic.TEAM_CREATION, builder.status(0).build());
						
						executor.shutdown();
					} catch (Exception e) {
						if (counter.get() < MAX_RETRY) {
							//IMPORTANT: do not remove this debug message
							log.debug("Attempt number: {} failed", counter.getAndIncrement());
							executor.schedule(this, 10, TimeUnit.SECONDS);
						}
						else {
							log.error("Error creating Team: " + e.getMessage());
							//send message to (ignite) MicrosoftMessagingService
							microsoftMessagingService.send(MicrosoftMessage.Topic.TEAM_CREATION, builder.status(0).build());
							
							executor.shutdown();
						}
					}
				}
			}
		};
		//wait 30 sec before first try
		executor.schedule(task, 30, TimeUnit.SECONDS);
	}
	
	@Override
	public boolean deleteTeam(String teamId) throws MicrosoftCredentialsException {
		try {
			getGraphClient().groups().byGroupId(teamId)
				.delete();
			return true;
		}catch(MicrosoftCredentialsException e) {
			throw e;
		} catch(Exception ex){
			log.debug("Error deleting Microsoft group: id={}", teamId);
		}
		return false;
	}

	public boolean archiveTeam(String teamId) throws MicrosoftCredentialsException {
		try {
			// 1. Archive team without shouldSetSpoSiteReadOnlyForMembers (no supported in app-only)
			ArchivePostRequestBody requestBody = new ArchivePostRequestBody();
			requestBody.setShouldSetSpoSiteReadOnlyForMembers(false);

			getGraphClient().teams().byTeamId(teamId)
				.archive()
				.post(requestBody);

			// 2. Obtain the associated SharePoint site to ensure the team is fully archived before setting it to read-only
			Site site = getGraphClient().groups().byGroupId(teamId)
				.sites()
				.bySiteId("root")
				.get();

			if (site == null || site.getId() == null) {
				log.error("Could not retrieve SharePoint site for team: {}, site will not be set to read-only", teamId);
				return false;
			}

			// 3. Set SharePoint site to read-only (as a backup in case the archive operation did not set it correctly)
			Site siteUpdate = new Site();
			Map<String, Object> additionalData = new HashMap<>();
			additionalData.put("lockState", "readOnly");
			siteUpdate.setAdditionalData(additionalData);

			getGraphClient().sites().bySiteId(site.getId()).patch(siteUpdate);

			log.info("Team archived and SharePoint site set to read-only: teamId={}", teamId);

			return true;
		} catch(MicrosoftCredentialsException e) {
			throw e;
		} catch(Exception ex){
			log.error("Error archiving Microsoft team: id={}", teamId, ex);
		}
		return false;
	}

	public boolean unarchiveTeam(String teamId) throws MicrosoftCredentialsException {
		try {
			//1. Unarchive team
			getGraphClient().teams().byTeamId(teamId)
				.unarchive()
				.post();

			// 2. Obtain the associated SharePoint site to ensure the team is fully unarchived before returning
			Site site = getGraphClient().groups().byGroupId(teamId)
				.sites()
				.bySiteId("root")
				.get();

			if (site == null || site.getId() == null) {
				log.error("Could not retrieve SharePoint site for team: {}, site will remain read-only", teamId);
				return false;
			}

			//3. Set SharePoint site to unlocked (as a backup in case the unarchive operation did not set it correctly)
			Site siteUpdate = new Site();
			HashMap<String, Object> additionalData = new HashMap<>();
			additionalData.put("lockState", "unlocked");
			siteUpdate.setAdditionalData(additionalData);

			getGraphClient().sites().bySiteId(site.getId()).patch(siteUpdate);

			log.info("Team unarchived and SharePoint site unlocked: teamId={}", teamId);

			return true;
		} catch (MicrosoftCredentialsException e) {
			throw e;
		} catch (Exception ex) {
			log.error("Error unarchiving Microsoft team: id={}", teamId, ex);
		}
		return false;
	}

	@Override
	public MicrosoftMembersCollection getTeamMembers(String id, MicrosoftUserIdentifier key) throws MicrosoftCredentialsException {
		MicrosoftMembersCollection ret = new MicrosoftMembersCollection();
		try {
			MicrosoftCredentials credentials = microsoftConfigRepository.getCredentials();
			
			ConversationMemberCollectionResponse page = getGraphClient()
					.teams()
					.byTeamId(id)
					.members()
					.get();
			while (page != null) {
				for(ConversationMember m : page.getValue()) {
					AadUserConversationMember member = (AadUserConversationMember)m;
	
					String identifier = getMemberKeyValue(member, key);
					//avoid insert admin user
					if(StringUtils.isNotBlank(identifier) && (!credentials.getEmail().equalsIgnoreCase(member.getEmail()) && !credentials.getEmail().equalsIgnoreCase(member.getUserId()))) {
						log.debug(">>MEMBER: ({}) --> displayName={}, roles={}, userId={}, id={}", identifier, member.getDisplayName(), member.getRoles().stream().collect(Collectors.joining(", ")), member.getUser(), member.getId());
	
						MicrosoftUser mu = MicrosoftUser.builder()
								.id(member.getUserId())
								.name(member.getDisplayName())
								.email(member.getEmail())
								.memberId(member.getId())
								.owner(member.getRoles() != null && member.getRoles().stream().anyMatch(role -> role.equalsIgnoreCase(MicrosoftUser.OWNER)))
								.guest(member.getRoles() != null && member.getRoles().stream().anyMatch(role -> role.equalsIgnoreCase(MicrosoftUser.GUEST)))
								.build();
						
						if(member.getRoles() != null && member.getRoles().stream().anyMatch(role -> role.equalsIgnoreCase(MicrosoftUser.GUEST))) {
							ret.addGuest(identifier, mu);
						} else if(member.getRoles() != null && member.getRoles().stream().anyMatch(role -> role.equalsIgnoreCase(MicrosoftUser.OWNER))) {
							ret.addOwner(identifier, mu);
						} else {
							ret.addMember(identifier, mu);
						}
					}
				}

				String nextLink = page.getOdataNextLink();
				if (nextLink == null) break;

				page = getGraphClient()
						.teams()
						.byTeamId(id)
						.members()
						.withUrl(nextLink)
						.get();
			}
		} catch (MicrosoftCredentialsException e) {
			throw e;
		} catch(Exception ex){
			log.debug("Microsoft Team not found with id={}", id);
		}

		return ret;
	}

	@Override
	public MicrosoftUser checkUserInTeam(String identifier, String teamId, MicrosoftUserIdentifier key) throws MicrosoftCredentialsException {
		MicrosoftUser ret = null;
		
		String filter;
		switch(key) {
			case USER_ID:
				filter = "(microsoft.graph.aadUserConversationMember/userId eq '"+identifier+"')";
				break;
			case EMAIL:
				filter = "(microsoft.graph.aadUserConversationMember/email eq '"+identifier+"')";
				break;
			default:
				return null;
		}
		if(filter != null) {
			try {
				ConversationMemberCollectionResponse page = getGraphClient().teams().byTeamId(teamId).members()
						.get(requestConfig -> {
							requestConfig.queryParameters.filter = filter;
						});
				while (page != null) {
					for(ConversationMember m : page.getValue()) {
						AadUserConversationMember member = (AadUserConversationMember)m;
					
						if(ret == null) {
							ret = MicrosoftUser.builder()
									.id(member.getUserId())
									.name(member.getDisplayName())
									.email(member.getEmail())
									.memberId(member.getId())
									.owner(member.getRoles() != null && member.getRoles().stream().anyMatch(role -> role.equalsIgnoreCase(MicrosoftUser.OWNER)))
									.guest(member.getRoles() != null && member.getRoles().stream().anyMatch(role -> role.equalsIgnoreCase(MicrosoftUser.GUEST)))
									.build();
						}
					}

					String nextLink = page.getOdataNextLink();
					if (nextLink == null) break;

					page = getGraphClient()
							.teams()
							.byTeamId(teamId)
							.members()
							.withUrl(nextLink)
							.get();
				}
			}catch(MicrosoftCredentialsException e) {
				throw e;
			} catch(Exception ex){
				log.debug("Microsoft Team not found with id={}", teamId);
			}
		}
		return ret;
	}
	
	@Override
	public boolean addMemberToGroup(String userId, String groupId) throws MicrosoftCredentialsException {
		try {
			ReferenceCreate referenceCreate = new ReferenceCreate();
			referenceCreate.setOdataId("https://graph.microsoft.com/v1.0/directoryObjects/" + userId);
			
			getGraphClient().groups().byGroupId(groupId).members().ref()
					.post(referenceCreate);
		}catch(MicrosoftCredentialsException e) {
			throw e;
		}catch (Exception e) {
			log.debug("Error adding member userId={} to groupId={}", userId, groupId);
			return false;
		}
		return true;
	}
	
	@Override
	public boolean addOwnerToGroup(String userId, String groupId) throws MicrosoftCredentialsException {
		try {
			ReferenceCreate referenceCreate = new ReferenceCreate();
			referenceCreate.setOdataId("https://graph.microsoft.com/v1.0/directoryObjects/" + userId);
			
			getGraphClient().groups().byGroupId(groupId).owners().ref()
					.post(referenceCreate);
		}catch(MicrosoftCredentialsException e) {
			throw e;
		}catch (Exception e) {
			log.debug("Error adding owner userId={} to groupId={}", userId, groupId);
			return false;
		}
		return true;
	}
	
	@Override
	public boolean addMemberToTeam(String userId, String teamId) throws MicrosoftCredentialsException {
		try {
			AadUserConversationMember conversationMember = new AadUserConversationMember();
			conversationMember.setOdataType("#microsoft.graph.aadUserConversationMember");
			conversationMember.setRoles(new LinkedList<String>());
			
			Map<String, Object> additionalData = new HashMap<>();
			additionalData.put("user@odata.bind", "https://graph.microsoft.com/v1.0/users('" + userId + "')");
			conversationMember.setAdditionalData(additionalData);
			
			getGraphClient().teams().byTeamId(teamId).members().post(conversationMember);
		}catch(MicrosoftCredentialsException e) {
			throw e;
		} catch (Exception e) {
			log.debug("Error adding member userId={} to teamId={}", userId, teamId);
			return false;
		}
		return true;
	}
	
	@Override
	public boolean addOwnerToTeam(String userId, String teamId) throws MicrosoftCredentialsException {
		try {
			LinkedList<String> rolesList = new LinkedList<String>();
			rolesList.add(MicrosoftUser.OWNER);
			
			AadUserConversationMember conversationMember = new AadUserConversationMember();
			conversationMember.setOdataType("#microsoft.graph.aadUserConversationMember");
			conversationMember.setRoles(rolesList);
			
			Map<String, Object> additionalData = new HashMap<>();
			additionalData.put("user@odata.bind", "https://graph.microsoft.com/v1.0/users('" + userId + "')");
			conversationMember.setAdditionalData(additionalData);
			
			getGraphClient().teams().byTeamId(teamId).members().post(conversationMember);
		}catch(MicrosoftCredentialsException e) {
			throw e;
		}catch (Exception e) {
			log.debug("Error adding owner userId={} to teamId={}", userId, teamId);
			return false;
		}
		return true;
	}
	
	@Override
	public SynchronizationStatus addUsersToTeamOrGroup(SiteSynchronization ss, List<MicrosoftUser> members, SynchronizationStatus status, LinkedList<String> roles) throws MicrosoftCredentialsException {
		String teamId = ss.getTeamId();
		String dataKey = roles.contains(MicrosoftUser.OWNER) ? "ownerId" : "memberId";
		boolean generalError = false;

		ConversationMemberCollectionResponse postMembers = graphClient.teams().byTeamId(teamId).members()
				.get();

		int maxRequests = members.size() / MAX_PER_REQUEST;

		for (int i = 0; i <= maxRequests; i++) {
			List<MicrosoftUser> pendingMembers = members.subList(i * MAX_PER_REQUEST, Math.min(MAX_PER_REQUEST * (i +1 ), members.size()));
			List<MicrosoftUser> successMembers = new LinkedList<>();
			generalError = false;

			int retryCount = 0;
			while (!pendingMembers.isEmpty() && retryCount < MAX_RETRY) {
				BatchRequestContentCollection batchRequestContent = new BatchRequestContentCollection(getGraphClient());

				pendingMembers.forEach(member -> {
					ConversationMember memberToAdd = new ConversationMember();

					memberToAdd.setOdataType("#microsoft.graph.aadUserConversationMember");
					memberToAdd.setRoles(roles);
					Map<String, Object> additionalData = new HashMap<>();
					additionalData.put("user@odata.bind", "https://graph.microsoft.com/v1.0/users('" + member.getId() + "')");
					memberToAdd.setAdditionalData(additionalData);

					try {
						RequestInformation requestInfo = getGraphClient()
								.teams()
								.byTeamId(teamId)
								.members()
								.toPostRequestInformation(memberToAdd);
						batchRequestContent.addBatchRequestStep(requestInfo);
					} catch (Exception e) {
						log.debug("Error building batch request for member userId={}", member.getId());
					}
				});

				BatchResponseContentCollection responseContent;

				try {
					responseContent = getGraphClient()
							.getBatchRequestBuilder()
							.post(batchRequestContent, null);

					HashMap<String, ?> membersResponse = parseBatchResponse(responseContent, pendingMembers);
					successMembers.addAll((List<MicrosoftUser>) membersResponse.get("success"));
					pendingMembers = (List<MicrosoftUser>) membersResponse.get("failed");
					List<Map<String, ?>> errors = (List<Map<String, ?>>) membersResponse.get("errors");
					handleMicrosoftExceptions(errors);
				} catch (ApiException | IOException e) {
					log.debug("Microsoft General error adding members ", e);
					generalError = true;
					break;
				} finally {
					retryCount++;
				}
			}

			if(generalError)
				continue;

			for (MicrosoftUser pendingMember : pendingMembers) {
				if (status != SynchronizationStatus.ERROR) {
					//once ERROR status is set, do not check it again
					status = (pendingMember != null && pendingMember.isGuest()) ? SynchronizationStatus.ERROR_GUEST : SynchronizationStatus.ERROR;
				}

				log.debug("Error adding {} userId={} to teamId={}", (roles.contains(MicrosoftUser.OWNER) && !Objects.requireNonNull(pendingMember).isGuest()) ? "owner" : "member", pendingMember.getId(), teamId);

				// save log add member
				microsoftLoggingRepository.save(MicrosoftLog.builder()
						.event(MicrosoftLog.EVENT_ADD_MEMBER)
						.status((pendingMember != null && pendingMember.isGuest()) ? MicrosoftLog.Status.OK : MicrosoftLog.Status.KO)
						.addData("origin", sakaiProxy.getActionOrigin())
						.addData("teamId", teamId)
						.addData("siteId", ss.getSiteId())
						.addData(dataKey, pendingMember != null ? pendingMember.getId() : "null")
						.build());

			}

			successMembers.forEach(member -> {
				// save log add member
				microsoftLoggingRepository.save(MicrosoftLog.builder()
						.event(MicrosoftLog.EVENT_ADD_MEMBER)
						.status(MicrosoftLog.Status.OK)
						.addData("origin", sakaiProxy.getActionOrigin())
						.addData("teamId", teamId)
						.addData("siteId", ss.getSiteId())
						.addData(dataKey, member.getId())
						.build());
			});

		}

		return status;
	}

	@Override
	public boolean removeUserFromGroup(String userId, String groupId) throws MicrosoftCredentialsException {
		try {
			getGraphClient().groups().byGroupId(groupId).members().byDirectoryObjectId(userId).ref()
				.delete();
		}catch(MicrosoftCredentialsException e) {
			throw e;
		}catch (Exception e) {
			log.debug("Error removing member userId={} from groupId={}", userId, groupId);
			return false;
		}
		return true;
	}
	
	@Override
	public boolean removeMemberFromTeam(String memberId, String teamId) throws MicrosoftCredentialsException {
		try {
			getGraphClient().teams().byTeamId(teamId).members().byConversationMemberId(memberId)
				.delete();
		}catch(MicrosoftCredentialsException e) {
			throw e;
		}catch (Exception e) {
			log.debug("Error removing member memberId={} from teamId={}", memberId, teamId);
			return false;
		}
		return true;
	}
	
	@Override
	public boolean removeAllMembersFromTeam(String teamId) throws MicrosoftCredentialsException {
		try {
			MicrosoftCredentials credentials = microsoftConfigRepository.getCredentials();
			String adminEmail = credentials.getEmail();
			
			//get all members in team
			Map<String, String> teamMemberMap = new HashMap<>();
			ConversationMemberCollectionResponse page = getGraphClient()
					.teams()
					.byTeamId(teamId)
					.members()
					.get();
			while (page != null) {
				for(ConversationMember m : page.getValue()) {
					AadUserConversationMember member = (AadUserConversationMember)m;
	
					//avoid insert admin user
					if(!adminEmail.equalsIgnoreCase(member.getEmail())) {
						log.debug(">>TEAM MEMBER: displayName={}, email={}, roles={}, userId={}, id={}", member.getDisplayName(), member.getEmail(), member.getRoles().stream().collect(Collectors.joining(", ")), member.getUserId(), member.getId());
	
						teamMemberMap.put(member.getUserId(), member.getId());
					}
				}
				String nextLink = page.getOdataNextLink();
				if (nextLink == null) break;

				page = getGraphClient()
						.teams()
						.byTeamId(teamId)
						.members()
						.withUrl(nextLink)
						.get();
			}
			
			//get all members in group (sometimes, specially with guest users, there are members in group that have not been moved to the team)
			Set<String> groupMemberIds = new HashSet<>();
			DirectoryObjectCollectionResponse page2 = getGraphClient().groups()
					.byGroupId(teamId)
					.members()
					.get(requestConfig -> {
						requestConfig.queryParameters.select = new String[]{"id", "mail", "displayName"};
					});
			while (page2 != null) {
				for(DirectoryObject o : page2.getValue()) {
					if("#microsoft.graph.user".equals(o.getOdataType())) {
						User member = (User)o;
						log.debug(">>GROUP MEMBER: displayName={}, mail={}, id={}", member.getDisplayName(), member.getMail(), member.getId());
						//avoid insert admin user
						if(!adminEmail.equalsIgnoreCase(member.getMail())) {
							groupMemberIds.add(member.getId());
						}
					}
				}
				String nextLink2 = page2.getOdataNextLink();
				if (nextLink2 == null) break;

				page2 = getGraphClient()
						.groups()
						.byGroupId(teamId)
						.members()
						.withUrl(nextLink2)
						.get();
			}
			
			//we only want group users that are not team members
			groupMemberIds.removeAll(teamMemberMap.keySet());
			
			//remove all team members
			boolean ret = true;
			for(String memberId : teamMemberMap.values()) {
				ret = ret && removeMemberFromTeam(memberId, teamId);
			}
			
			//remove all group users
			for(String userId : groupMemberIds) {
				ret = ret && removeUserFromGroup(userId, teamId);
			}
			return ret;
		}catch(MicrosoftCredentialsException e) {
			throw e;
		}catch (Exception e) {
			log.debug("Error removing all members from teamId={}", teamId);
			return false;
		}
	}

	public boolean existsTeamWithName(String name) throws MicrosoftCredentialsException {
		String truncatedName = processMicrosoftTeamName(name);
		Map<String, MicrosoftTeam> teams = getTeams();
		return teams.values().stream()
				.anyMatch(t -> t.getName().equalsIgnoreCase(truncatedName));
	}

	// ------------------------------------------ CHANNELS ----------------------------------------------------
	@Override
	public MicrosoftChannel getChannel(String teamId, String channelId) throws MicrosoftCredentialsException {
		return getChannel(teamId, channelId, false);
	}
	
	@Override
	public MicrosoftChannel getChannel(String teamId, String channelId, boolean force) throws MicrosoftCredentialsException {
		try {
			//get from cache (if not force)
			if(!force) {
				Cache.ValueWrapper cachedValue = getCache().get(CACHE_CHANNELS+teamId);
				if(cachedValue != null) {
					Map<String, MicrosoftChannel> channelsMap = (Map<String, MicrosoftChannel>)cachedValue.get();
					if(channelsMap.containsKey(channelId)) {
						return channelsMap.get(channelId);
					}
				}
			}
			//get from Microsoft
			Channel channel = getGraphClient()
						.teams()
						.byTeamId(teamId)
						.channels()
						.byChannelId(channelId)
						.get();
			MicrosoftChannel mc = MicrosoftChannel.builder()
					.id(channel.getId())
					.name(channel.getDisplayName())
					.description(channel.getDescription())
					.build();
			
			//update cache (if map exists)
			Cache.ValueWrapper cachedValue = getCache().get(CACHE_CHANNELS+teamId);
			if(cachedValue != null) {
				Map<String, MicrosoftChannel> channelsMap = (Map<String, MicrosoftChannel>)cachedValue.get();
				channelsMap.put(channelId, mc);
				
				getCache().put(CACHE_CHANNELS+teamId, channelsMap);
			}
			return mc;
		}catch(MicrosoftCredentialsException e) {
			throw e;
		}catch(Exception e) {
			log.debug("Microsoft Team or Channel not found with teamId={}, channelId={}", teamId, channelId);
		}
		return null;
	}
	
	@Override
	public Map<String, MicrosoftChannel> getTeamPrivateChannels(String teamId) throws MicrosoftCredentialsException {
		return getTeamPrivateChannels(teamId, false);
	}
	
	@Override
	public Map<String, MicrosoftChannel> getTeamPrivateChannels(String teamId, boolean force) throws MicrosoftCredentialsException {
		Map<String, MicrosoftChannel> channelsMap = new HashMap<>();

		ChannelCollectionResponse page = getGraphClient().teams().byTeamId(teamId)
				.channels()
				.get(requestConfig -> {
					requestConfig.queryParameters.filter = "membershipType eq 'private'";
					requestConfig.queryParameters.select = new String[]{"id", "displayName", "description"};
				});

		if (!force) {
			// get microsoft channels
			List<Channel> microsoftChannelsList = page.getValue();

			// Create a set of channel IDs obtained from Microsoft
			Set<String> microsoftChannelIds = new HashSet<>();
			for (Channel channel : microsoftChannelsList) {
				microsoftChannelIds.add(channel.getId());
			}
			//get from cache
			Cache.ValueWrapper cachedValue = getCache().get(CACHE_CHANNELS + teamId);
			if (cachedValue != null) {
				Map<String, MicrosoftChannel> cachedChannels = (Map<String, MicrosoftChannel>) cachedValue.get();
				cachedChannels.keySet().removeIf(channelId -> !microsoftChannelIds.contains(channelId));
				//return or update cache by ttl (?)
				if (cachedChannels.values().size() == microsoftChannelIds.size()) {
					return cachedChannels;
				} else {
					microsoftChannelsList.forEach(c -> channelsMap.put(c.getId(), MicrosoftChannel.builder()
							.id(c.getId())
							.name(c.getDisplayName())
							.description(c.getDescription())
							.build()));
					getCache().put(CACHE_CHANNELS + teamId, channelsMap);
					return channelsMap;
				}
			}
		}

		try {
			while (page != null) {
				for (Channel channel : page.getValue()) {
					channelsMap.put(channel.getId(), MicrosoftChannel.builder()
							.id(channel.getId())
							.name(channel.getDisplayName())
							.description(channel.getDescription())
							.build());
				}

				String nextLink = page.getOdataNextLink();
				if (nextLink == null) break;

				page = getGraphClient().teams().byTeamId(teamId)
						.channels()
						.withUrl(nextLink)
						.get();
			}
			
			//store in cache
			getCache().put(CACHE_CHANNELS + teamId, channelsMap);
		} catch (Exception e) {
			log.debug("Error getting private channels for teamId={}", teamId);
		}
		return channelsMap;
	}
	
	@Override
	public String createChannel(String teamId, String name, String ownerEmail) throws MicrosoftCredentialsException {
		try {
			String truncatedName = processMicrosoftChannelName(name);

			Channel channel = new Channel();
			channel.setMembershipType(ChannelMembershipType.Private);
			channel.setDisplayName(truncatedName);
			channel.setDescription(truncatedName);

			channel.setMembers(initializeChannelMembers(ownerEmail));

			Channel newChannel = getGraphClient().teams().byTeamId(teamId).channels()
					.post(channel);

			//add new channel to cache
			Cache.ValueWrapper cachedValue = getCache().get(CACHE_CHANNELS + teamId);
			if (cachedValue != null) {
				Map<String, MicrosoftChannel> channelsMap = (Map<String, MicrosoftChannel>) cachedValue.get();
				channelsMap.put(newChannel.getId(), MicrosoftChannel.builder()
						.id(newChannel.getId())
						.name(newChannel.getDisplayName())
						.description(newChannel.getDescription())
						.build());
				getCache().put(CACHE_CHANNELS+teamId, channelsMap);
			}
	
			return newChannel.getId();
		}catch(MicrosoftCredentialsException e) {
			throw e;
		}catch(Exception e) {
			log.debug("Error creating private channel ({}) in teamId={}", name, teamId);
		}
		return null;
	}

	@Override
	public List<MicrosoftChannel> createChannels(List<org.sakaiproject.site.api.Group> groupsToProcess, String teamId, String ownerEmail) throws MicrosoftCredentialsException {
		List<MicrosoftChannel> channels = new ArrayList<>();
		List<ConversationMember> members = initializeChannelMembers(ownerEmail);
		List<org.sakaiproject.site.api.Group> pendingChannels = groupsToProcess;

		final int MAX_RETRY = 2;
		int retryCount = 0;

		GraphServiceClient graph = getGraphClient();

		//sometimes microsoft fails creating -> loop for retry failed ones
		while (!pendingChannels.isEmpty() && retryCount < MAX_RETRY) {
			BatchRequestContentCollection batchRequestContent = new BatchRequestContentCollection(graph);

			groupsToProcess.forEach(group -> {
				Channel channel = new Channel();
				channel.setMembershipType(ChannelMembershipType.Private);
				channel.setDisplayName(processMicrosoftChannelName(group.getTitle()));
				channel.setDescription(group.getTitle());
				channel.setMembers(members);

				RequestInformation requestInfo = graph
						.teams().byTeamId(teamId)
						.channels()
						.toPostRequestInformation(channel);
				batchRequestContent.addBatchRequestStep(requestInfo);
			});

			try {
				BatchResponseContentCollection responseContent = graph
						.getBatchRequestBuilder()
						.post(batchRequestContent, null);

				HashMap<String, ?> channelsResponse = parseBatchResponse(responseContent, groupsToProcess);
				channels.addAll((List<MicrosoftChannel>) channelsResponse.get("success"));
				pendingChannels = (List<org.sakaiproject.site.api.Group>) channelsResponse.get("failed");
			} catch (ApiException | IOException e) {
				log.debug("Error creating channels for teamId={}", teamId, e);
				break;
			} finally {
				retryCount++;
			}
		}

		//update cache
		Cache.ValueWrapper cachedValue = getCache().get(CACHE_CHANNELS + teamId);
		if (cachedValue != null) {
			Map<String, MicrosoftChannel> channelsMap = (Map<String, MicrosoftChannel>) cachedValue.get();
			channelsMap.keySet().removeIf(channelId -> !channels.stream().map(c -> c.getId()).collect(Collectors.toSet()).contains(channelId));
			channels.stream().filter(c -> !channelsMap.containsKey(c.getId())).forEach(channel -> channelsMap.put(channel.getId(), channel));
			getCache().put(CACHE_CHANNELS + teamId, channelsMap);
		}

		return channels;
	}

	private HashMap<String, ?> parseBatchResponse(BatchResponseContentCollection responseContent, List<?> listToProcess) {
		HashMap<String, ?> resultMap = new HashMap<>();

		if (Objects.isNull(responseContent)) {
			return resultMap;
		}

		switch(listToProcess.get(0).getClass().getSimpleName()) {
			case "MicrosoftUser":
				resultMap = parseBatchResponseToMicrosoftUser(responseContent,(List<MicrosoftUser>) listToProcess);
				break;
			case "BaseGroup":
				resultMap = parseBatchResponseToMicrosoftChannel(responseContent, listToProcess);
				break;
			case "SiteSynchronization":
				resultMap = parseBatchResponseToMicrosoftTeam(responseContent, listToProcess);
				break;
			case "String":
				resultMap = parseBatchResponseToMicrosoftUserFromStringList(responseContent,(List<String>) listToProcess);
				break;
		}

		return resultMap;
	}

	private HashMap<String,?> parseBatchResponseToMicrosoftUser(BatchResponseContentCollection responseContent, List<MicrosoftUser> listToProcess) {
		HashMap<String, Object> responseMap = new HashMap<>();
		Map<String, Integer> statusCodes = responseContent.getResponsesStatusCodes();

		Map<String, MicrosoftUser> successRequests =
				statusCodes.entrySet().stream().filter(r -> r.getValue() <= 299).collect(Collectors.toList())
						.stream().map(r -> {
							ConversationMember member = responseContent.getResponseById(r.getKey(), ConversationMember::createFromDiscriminatorValue);
							String userId = ((AadUserConversationMember) member).getUserId();
							Map.Entry<String, MicrosoftUser> entry = new AbstractMap.SimpleEntry<>(
									userId,
									listToProcess.stream().filter(user -> user.getId().equals(userId)).findFirst().orElse(null)
							);
							return entry;
						}).collect(Collectors.toMap(
								Map.Entry::getKey,
								Map.Entry::getValue
						));

		List<MicrosoftUser> pendingRequests = listToProcess.stream()
				.filter(user -> !successRequests.containsKey(user.getId()))
				.collect(Collectors.toList());

		List<Map<String, ?>> errors = statusCodes.entrySet().stream()
				.filter(r -> r.getValue() > 299)
				.map(r -> {
					String code;
					String innerError;
					try {
						ODataError error = responseContent.getResponseById(r.getKey(), ODataError::createFromDiscriminatorValue);
						code = error.getError().getCode();
						innerError = error.getError().getInnerError().getAdditionalData().get("code").toString();
					} catch (Exception e) {
						code = "Failure";
						innerError = "Failure";
					}
					Response rawResponse = responseContent.getResponseById(r.getKey());
					Map<String, Object> errorMap = new HashMap<>();
					errorMap.put("status", r.getValue());
					errorMap.put("retryAfter", rawResponse != null && rawResponse.header("Retry-After") != null ? rawResponse.header("Retry-After") : 5);
					errorMap.put("code", code);
					errorMap.put("innerError", innerError);
					return errorMap;
				})
				.collect(Collectors.toList());


		responseMap.put("success", new ArrayList<>(successRequests.values()));
		responseMap.put("failed", pendingRequests);
		responseMap.put("errors", errors);

		return responseMap;
	}

	private HashMap<String,?> parseBatchResponseToMicrosoftUserFromStringList(BatchResponseContentCollection responseContent, List<String> listToProcess) {
		HashMap<String, Object> responseMap = new HashMap<>();

		Map<String, Integer> statusCodes = responseContent.getResponsesStatusCodes();

		List<String> nonEmptyResponseKeys = statusCodes.entrySet().stream()
				.filter(r -> r.getValue() <= 299)
				.map(Map.Entry::getKey)
				.filter(key -> {
					UserCollectionResponse ucr = responseContent.getResponseById(key, UserCollectionResponse::createFromDiscriminatorValue);
					return ucr != null && ucr.getValue() != null && !ucr.getValue().isEmpty();
				})
				.collect(Collectors.toList());

		Map<String, MicrosoftUser> successRequests =
				nonEmptyResponseKeys.stream().map(key -> {
					UserCollectionResponse ucr = responseContent.getResponseById(key, UserCollectionResponse::createFromDiscriminatorValue);
					User user = ucr.getValue().get(0);
					return new AbstractMap.SimpleEntry<>(
							user.getId(),
							MicrosoftUser.builder()
								.id(user.getId())
								.email(user.getMail())
								.build()
					);
				}).collect(Collectors.toMap(
						Map.Entry::getKey,
						Map.Entry::getValue
				));

		List<String> notFoundUsers =
				listToProcess.stream()
						.filter(email -> successRequests.entrySet().stream().noneMatch(r -> r.getValue().getEmail().equalsIgnoreCase(email)))
						.collect(Collectors.toList());

		List<Map<String, ?>> errors = statusCodes.entrySet().stream()
				.filter(r -> r.getValue() > 299)
				.map(r -> {
					String code;
					String innerError;
					try {
						ODataError error = responseContent.getResponseById(r.getKey(), ODataError::createFromDiscriminatorValue);
						code = error.getError().getCode();
						innerError = error.getError().getInnerError().getAdditionalData().get("code").toString();
					} catch (Exception e) {
						code = "Failure";
						innerError = "Failure";
					}
					Response rawResponse = responseContent.getResponseById(r.getKey());
					return Map.of(
							"status", r.getValue(),
							"retryAfter", rawResponse != null && rawResponse.header("Retry-After") != null ? rawResponse.header("Retry-After") : 5,
									"code", code,
									"innerError", innerError);
				})
				.collect(Collectors.toList());


		responseMap.put("success", new ArrayList<>(successRequests.values()));
		responseMap.put("failed", notFoundUsers);
		responseMap.put("errors", errors);

		return responseMap;
	}

	private HashMap<String, ?> parseBatchResponseToMicrosoftTeam(BatchResponseContentCollection responseContent, List<?> listToProcess) {
		HashMap<String, Object> responseMap = new HashMap<>();

		Map<String, MicrosoftTeam> successRequests =
				responseContent.getResponsesStatusCodes().entrySet().stream()
					.filter(r -> r.getValue() <= 299)
					.collect(Collectors.toList())
					.stream().map(r -> {
						Team team = responseContent.getResponseById(r.getKey(), Team::createFromDiscriminatorValue);
						Map.Entry<String, MicrosoftTeam> entry = new AbstractMap.SimpleEntry<>(
								team.getId(),
								MicrosoftTeam.builder()
									.id(team.getId())
									.name(team.getDisplayName())
									.description(team.getDescription())
									.build()
						);
						return entry;
					}).collect(Collectors.toMap(
							entry -> entry.getKey(),
							entry -> entry.getValue()
					));

		List<String> nonExistingSites = responseContent.getResponsesStatusCodes().entrySet().stream()
				.filter(r -> r.getValue() == 404)
				.collect(Collectors.toList())
				.stream().map(r -> {
					ODataError error = responseContent.getResponseById(r.getKey(), ODataError::createFromDiscriminatorValue);
					return error.getError().getMessage();
				}).collect(Collectors.toList());

		List<SiteSynchronization> pendingSites =
				(List<SiteSynchronization>) listToProcess.stream()
						.filter(i ->
								successRequests.values().stream().noneMatch(c ->
										c.getId().equals(((SiteSynchronization) i).getTeamId()))
										&& nonExistingSites.stream().noneMatch(nes -> nes.contains(((SiteSynchronization) i).getTeamId())))
						.collect(Collectors.toList());

		responseMap.put("pending", pendingSites);
		responseMap.put("failed", nonExistingSites);
		responseMap.put("success", successRequests);

		return responseMap;
	}

	private HashMap<String, ?> parseBatchResponseToMicrosoftChannel(BatchResponseContentCollection responseContent, List<?> listToProcess) {
		HashMap<String, List<?>> responseMap = new HashMap<>();
		List<MicrosoftChannel> successRequests =
				responseContent.getResponsesStatusCodes().entrySet().stream()
				.filter(r -> r.getValue() <= 299)
				.collect(Collectors.toList())
				.stream().map(r -> {
					Channel channel = responseContent.getResponseById(r.getKey(), Channel::createFromDiscriminatorValue);
					return MicrosoftChannel.builder()
							.id(channel.getId())
							.name(channel.getDisplayName())
							.description(channel.getDescription())
							.build();
				}).collect(Collectors.toList());
		
		List<org.sakaiproject.site.api.Group> pendingGroups =
				(List<org.sakaiproject.site.api.Group>) listToProcess.stream()
						.filter(g ->
								successRequests.stream().noneMatch(c ->
										c.getName()
												.equalsIgnoreCase(processMicrosoftChannelName(((org.sakaiproject.site.api.Group) g).getTitle()))))
						.collect(Collectors.toList());

		responseMap.put("failed", pendingGroups);
		responseMap.put("success", successRequests);

		return responseMap;
	}

	private List<ConversationMember> initializeChannelMembers(String ownerEmail) throws MicrosoftCredentialsException {
		User userOwner = getGraphClient().users().byUserId(ownerEmail).get();
		AadUserConversationMember conversationMember = new AadUserConversationMember();
		conversationMember.setOdataType("#microsoft.graph.aadUserConversationMember");
		conversationMember.setRoles(Arrays.asList(MicrosoftUser.OWNER));
		Map<String, Object> additionalData = new HashMap<>();
		additionalData.put("user@odata.bind", "https://graph.microsoft.com/v1.0/users('" + userOwner.getId() + "')");
		conversationMember.setAdditionalData(additionalData);

		LinkedList<ConversationMember> membersList = new LinkedList<ConversationMember>();
		membersList.add(conversationMember);

		return membersList;
	}

	@Override
	public String processMicrosoftChannelName(String name) {
		return formatMicrosoftChannelString(
			formattedText.makeShortenedText(name, CHANNEL_CHARACTER_LIMIT, null, null)
		);
	}


	@Override
	public String processMicrosoftTeamName(String name) {
		return formattedText.makeShortenedText(name, TEAM_CHARACTER_LIMIT, null, null);
	}

	@Override
	public boolean deleteChannel(String teamId, String channelId) throws MicrosoftCredentialsException {
		try {
			getGraphClient().teams().byTeamId(teamId).channels().byChannelId(channelId)
				.delete();
			return true;
		}catch(MicrosoftCredentialsException e) {
			throw e;
		} catch(Exception ex){
			log.debug("Error deleting Microsoft channel: teamId={}, channelId={}", teamId, channelId);
		}
		return false;
	}
	
	@Override
	public MicrosoftMembersCollection getChannelMembers(String teamId, String channelId, MicrosoftUserIdentifier key) throws MicrosoftCredentialsException {
		MicrosoftMembersCollection ret = new MicrosoftMembersCollection();      
		try {
			MicrosoftCredentials credentials = microsoftConfigRepository.getCredentials();
			
			ConversationMemberCollectionResponse page = getGraphClient()
					.teams()
					.byTeamId(teamId)
					.channels()
					.byChannelId(channelId)
					.members()
					.get();

			while (page != null) {
				for(ConversationMember m : page.getValue()) {
					AadUserConversationMember member = (AadUserConversationMember)m;
	
					String identifier = getMemberKeyValue(member, key);
					//avoid insert admin user
					if(StringUtils.isNotBlank(identifier) && !credentials.getEmail().equalsIgnoreCase(member.getEmail())) {
						log.debug(">>MEMBER: ({}) --> displayName={}, roles={}, userId={}, id={}", identifier, member.getDisplayName(), member.getRoles().stream().collect(Collectors.joining(", ")), member.getUserId(), member.getId());
	
						MicrosoftUser mu = MicrosoftUser.builder()
								.id(member.getUserId())
								.name(member.getDisplayName())
								.email(member.getEmail())
								.memberId(member.getId())
								.owner(member.getRoles() != null && member.getRoles().stream().anyMatch(role -> role.equalsIgnoreCase(MicrosoftUser.OWNER)))
								.guest(member.getRoles() != null && member.getRoles().stream().anyMatch(role -> role.equalsIgnoreCase(MicrosoftUser.GUEST)))
								.build();

						if(member.getRoles() != null && member.getRoles().stream().anyMatch(role -> role.equalsIgnoreCase(MicrosoftUser.GUEST))) {
							ret.addGuest(identifier, mu);
						} else if(member.getRoles() != null && member.getRoles().stream().anyMatch(role -> role.equalsIgnoreCase(MicrosoftUser.OWNER))) {
							ret.addOwner(identifier, mu);
						} else {
							ret.addMember(identifier, mu);
						}
					}
				}
				String nextLink = page.getOdataNextLink();
				if (nextLink == null) break;

				page = getGraphClient()
						.teams()
						.byTeamId(teamId)
						.channels()
						.byChannelId(channelId)
						.withUrl(nextLink)
						.members()
						.get();
			}
		}catch(MicrosoftCredentialsException e) {
			throw e;
		} catch(Exception ex){
			log.debug("Microsoft Team or Channel not found with teamId={}, channelId={}", teamId, channelId);
		}

		return ret;
	}
	
	@Override
	public MicrosoftUser checkUserInChannel(String identifier, String teamId, String channelId, MicrosoftUserIdentifier key) throws MicrosoftCredentialsException {
		MicrosoftUser ret = null;
		
		String filter;
		switch(key) {
			case USER_ID:
				filter = "(microsoft.graph.aadUserConversationMember/userId eq '"+identifier+"')";
				break;
			case EMAIL:
				filter = "(microsoft.graph.aadUserConversationMember/email eq '"+identifier+"')";
				break;
			default:
				return null;
		}
		if(filter != null) {
			try {
				ConversationMemberCollectionResponse page = getGraphClient().teams().byTeamId(teamId).channels().byChannelId(channelId).members()
						.get(requestConfig -> {
							requestConfig.queryParameters.filter = filter;
						});
				while (page != null) {
					for(ConversationMember m : page.getValue()) {
						AadUserConversationMember member = (AadUserConversationMember)m;
					
						if(ret == null) {
							ret = MicrosoftUser.builder()
									.id(member.getUserId())
									.name(member.getDisplayName())
									.email(member.getEmail())
									.memberId(member.getId())
									.owner(member.getRoles() != null && member.getRoles().stream().anyMatch(role -> role.equalsIgnoreCase(MicrosoftUser.OWNER)))
									.guest(member.getRoles() != null && member.getRoles().stream().anyMatch(role -> role.equalsIgnoreCase(MicrosoftUser.GUEST)))
									.build();
						}
					}

					String nextLink = page.getOdataNextLink();
					if (nextLink == null) break;

					page = getGraphClient()
							.teams()
							.byTeamId(teamId)
							.channels()
							.byChannelId(channelId)
							.members()
							.withUrl(nextLink)
							.get();
				}
			}catch(MicrosoftCredentialsException e) {
				throw e;
			} catch(Exception ex){
				log.debug("Microsoft Team or Channel not found with teamId={} and channelId={}", teamId, channelId);
			}
		}

		return ret;
	}

	@Override
	public boolean addMemberToChannel(String userId, String teamId, String channelId) throws MicrosoftCredentialsException {
		try {
			AadUserConversationMember conversationMember = new AadUserConversationMember();
			conversationMember.setOdataType("#microsoft.graph.aadUserConversationMember");
			conversationMember.setRoles(new LinkedList<String>());
			Map<String, Object> additionalData = new HashMap<>();
			additionalData.put("user@odata.bind", "https://graph.microsoft.com/v1.0/users('" + userId + "')");
			conversationMember.setAdditionalData(additionalData);
			
			getGraphClient().teams().byTeamId(teamId).channels().byChannelId(channelId).members().post(conversationMember);
		}catch(MicrosoftCredentialsException e) {
			throw e;
		}catch (Exception e) {
			log.debug("Error adding member userId={} to teamId={} + channelId={}", userId, teamId, channelId);
			return false;
		}
		return true;
	}
	
	@Override
	public boolean addOwnerToChannel(String userId, String teamId, String channelId) throws MicrosoftCredentialsException {
		try {
			LinkedList<String> rolesList = new LinkedList<String>();
			rolesList.add(MicrosoftUser.OWNER);
			
			AadUserConversationMember conversationMember = new AadUserConversationMember();
			conversationMember.setOdataType("#microsoft.graph.aadUserConversationMember");
			conversationMember.setRoles(rolesList);
			Map<String, Object> additionalData = new HashMap<>();
			additionalData.put("user@odata.bind", "https://graph.microsoft.com/v1.0/users('" + userId + "')");
			conversationMember.setAdditionalData(additionalData);
			
			getGraphClient().teams().byTeamId(teamId).channels().byChannelId(channelId).members().post(conversationMember);
		}catch(MicrosoftCredentialsException e) {
			throw e;
		}catch (Exception e) {
			log.debug("Error adding owner userId={} to teamId={} + channelId={}", userId, teamId, channelId);
			return false;
		}
		return true;
	}

	public SynchronizationStatus addUsersToChannel(SiteSynchronization ss, GroupSynchronization gs, List<MicrosoftUser> members, SynchronizationStatus status, LinkedList<String> roles) throws MicrosoftCredentialsException {
		String teamId = ss.getTeamId();
		String channelId = gs.getChannelId();
		boolean generalError = false;

		GraphServiceClient graph = getGraphClient();

		final int maxRequests = members.size() / MAX_PER_REQUEST;

		for (int i = 0; i <= maxRequests; i++) {
			List<MicrosoftUser> pendingMembers = members.subList(i * MAX_PER_REQUEST, Math.min(MAX_PER_REQUEST * (i +1 ), members.size()));
			List<MicrosoftUser> successMembers = new LinkedList<>();
			generalError = false;
			int retryCount = 0;
			while (!pendingMembers.isEmpty() && retryCount < MAX_RETRY) {
				BatchRequestContentCollection batchRequestContent = new BatchRequestContentCollection(graph);

				pendingMembers.forEach(member -> {
					AadUserConversationMember memberToAdd = new AadUserConversationMember();

					memberToAdd.setOdataType("#microsoft.graph.aadUserConversationMember");
					memberToAdd.setRoles(roles);
					Map<String, Object> additionalData = new HashMap<>();
					additionalData.put("user@odata.bind", "https://graph.microsoft.com/v1.0/users('" + member.getId() + "')");
					memberToAdd.setAdditionalData(additionalData);

					RequestInformation reqInfo = graph
							.teams()
							.byTeamId(teamId)
							.channels()
							.byChannelId(channelId)
							.members()
							.toPostRequestInformation(memberToAdd);

					batchRequestContent.addBatchRequestStep(reqInfo);
				});
				BatchResponseContentCollection responseContent;

				try {
					responseContent = graph
							.getBatchRequestBuilder()
							.post(batchRequestContent, null);

					HashMap<String, ?> membersResponse = parseBatchResponse(responseContent, pendingMembers);

					successMembers.addAll((List<MicrosoftUser>) membersResponse.get("success"));
					pendingMembers = (List<MicrosoftUser>) membersResponse.get("failed");
					List<Map<String, ?>> errors = (List<Map<String, ?>>) membersResponse.get("errors");
					handleMicrosoftExceptions(errors);
				} catch (ApiException | IOException e) {
					log.debug("Microsoft General error adding members ", e);
					generalError = true;
					break;
				} finally {
					retryCount++;
				}
			}

			if(generalError)
				continue;

			for (MicrosoftUser pendingMember : pendingMembers) {
				if (status != SynchronizationStatus.ERROR) {
					//once ERROR status is set, do not check it again
					status = (pendingMember != null && pendingMember.isGuest()) ? SynchronizationStatus.ERROR_GUEST : SynchronizationStatus.ERROR;
				}

				log.debug("Error adding {} userId={} to teamId={} + channelId={}", (roles.contains(MicrosoftUser.OWNER) && !Objects.requireNonNull(pendingMember).isGuest()) ? "owner" : "member", pendingMember.getId(), teamId, channelId);

				//save log
				microsoftLoggingRepository.save(MicrosoftLog.builder()
						.event(MicrosoftLog.EVENT_USER_ADDED_TO_CHANNEL)
						.status(MicrosoftLog.Status.KO)
						.addData("origin", sakaiProxy.getActionOrigin())
						.addData("email", pendingMember.getEmail())
						.addData("microsoftUserId", pendingMember.getId())
						.addData("siteId", ss.getSiteId())
						.addData("teamId", ss.getTeamId())
						.addData("groupId", gs.getGroupId())
						.addData("channelId", gs.getChannelId())
						.addData("owner", Boolean.toString(roles.contains(MicrosoftUser.OWNER) && !pendingMember.isGuest()))
						.addData("guest", Boolean.toString(pendingMember.isGuest()))
						.build());

			}

			successMembers.forEach(member -> {
				//save log
				microsoftLoggingRepository.save(MicrosoftLog.builder()
						.event(MicrosoftLog.EVENT_USER_ADDED_TO_CHANNEL)
						.status(MicrosoftLog.Status.OK)
						.addData("origin", sakaiProxy.getActionOrigin())
						.addData("email", member.getEmail())
						.addData("microsoftUserId", member.getId())
						.addData("siteId", ss.getSiteId())
						.addData("teamId", ss.getTeamId())
						.addData("groupId", gs.getGroupId())
						.addData("channelId", gs.getChannelId())
						.addData("owner", Boolean.toString(roles.contains(MicrosoftUser.OWNER) && !member.isGuest()))
						.addData("guest", Boolean.toString(member.isGuest()))
						.build());
			});

		}

		return status;
	}

	private void handleMicrosoftExceptions(List<Map<String,?>> errors) {
		if(!errors.isEmpty()) {
			if(errors.stream().anyMatch(e -> e.containsValue(429))) {
				Map<String, ?> error = errors.stream().filter(e -> e.containsValue(429)).findFirst().get();
				microsoftLoggingRepository.save(MicrosoftLog.builder()
						.event(MicrosoftLog.EVENT_TOO_MANY_REQUESTS)
						.addData("origin", sakaiProxy.getActionOrigin())
						.addData("Status", error.get("status").toString())
						.addData("Code", error.get("code").toString())
						.addData("RetryAfter", error.get("retryAfter").toString())
						.addData("InnerError", error.get("innerError").toString())
						.build());
				int retryAfter = Integer.parseInt(error.get("retryAfter").toString());

				try {
					Thread.sleep(retryAfter * 1000L);
				} catch (InterruptedException ignored) {
				}
			} else if (errors.stream().anyMatch(e -> e.containsValue(404))) {
				Map<String, ?> error = errors.stream().filter(e -> e.containsValue(404)).findFirst().get();
				microsoftLoggingRepository.save(MicrosoftLog.builder()
						.event(MicrosoftLog.EVENT_USER_NOT_FOUND_ON_TEAM)
						.addData("origin", sakaiProxy.getActionOrigin())
						.addData("Status", error.get("status").toString())
						.addData("Code", error.get("code").toString())
						.addData("RetryAfter", error.get("retryAfter").toString())
						.addData("InnerError", error.get("innerError").toString())
						.build());
				int retryAfter = Integer.parseInt(error.get("retryAfter").toString());
				try {
					Thread.sleep(retryAfter * 1000L);
				} catch (InterruptedException ignored) {
				}
			}
		}
	}

	@Override
	public boolean removeMemberFromChannel(String memberId, String teamId, String channelId) throws MicrosoftCredentialsException {
		try {
			getGraphClient().teams().byTeamId(teamId).channels().byChannelId(channelId).members().byConversationMemberId(memberId)
				.delete();
		}catch(MicrosoftCredentialsException e) {
			throw e;
		}catch (Exception e) {
			log.debug("Error removing member memberId={} from teamId={} + channelId={}", memberId, teamId, channelId);
			return false;
		}
		return true;
	}
	
	// ---------------------------------------- ONLINE MEETINGS --------------------------------------------------
	/**
	 * Create online meeting
	 * @param userEmail
	 * @param subject
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public TeamsMeetingData createOnlineMeeting(String userEmail, String subject, Instant startDate, Instant endDate, List<String> coorganizerEmails) throws MicrosoftCredentialsException {
		TeamsMeetingData result = null;
		
		// Get organizer user
		MicrosoftUser organizerUser = getUserByEmail(userEmail);
		
		if(organizerUser != null) {
			// Organizer
			MeetingParticipantInfo organizer = new MeetingParticipantInfo();
			IdentitySet organizerIdentity = new IdentitySet();
			Identity iden = new Identity();
			iden.setId(organizerUser.getId());
			iden.setDisplayName(organizerUser.getName());
			organizerIdentity.setUser(iden); 
			organizer.setIdentity( organizerIdentity);
			organizer.setRole(OnlineMeetingRole.Presenter);
			
			// Participants
			MeetingParticipants participants = new MeetingParticipants();
			participants.setOrganizer(organizer);

			// Coorganizers
			List<MeetingParticipantInfo> attendees = new ArrayList<>();
			if (coorganizerEmails != null) {
				for (String coorganizerEmail : coorganizerEmails) {
					if (!coorganizerEmail.equals(organizerUser.getEmail())) {
						MicrosoftUser coorganizerUser = getUserByEmail(coorganizerEmail);
						if (coorganizerUser != null) {
							MeetingParticipantInfo coorganizer = new MeetingParticipantInfo();
							IdentitySet coorganizerIdentity = new IdentitySet();
							Identity coorganizerIden = new Identity();
							coorganizerIden.setId(coorganizerUser.getId());
							coorganizerIden.setDisplayName(coorganizerUser.getName());
							coorganizerIdentity.setUser(coorganizerIden);
							coorganizer.setIdentity(coorganizerIdentity);
							coorganizer.setRole(OnlineMeetingRole.Coorganizer);
							coorganizer.setUpn(coorganizerUser.getEmail());
							attendees.add(coorganizer);
						}
					}
				}
			}
			participants.setAttendees(attendees);


			// Lobby Settings
			LobbyBypassSettings lobbySettings = new LobbyBypassSettings();
			lobbySettings.setScope(LobbyBypassScope.Organization);

			// Online Meeting
			OnlineMeeting onlineMeeting = new OnlineMeeting();
			if (startDate != null) { onlineMeeting.setStartDateTime(OffsetDateTime.ofInstant(startDate, ZoneId.systemDefault())); }
			if (endDate != null) { onlineMeeting.setEndDateTime(OffsetDateTime.ofInstant(endDate, ZoneId.systemDefault())); }
			onlineMeeting.setParticipants(participants);
			onlineMeeting.setSubject(subject);
			onlineMeeting.setLobbyBypassSettings(lobbySettings);
			onlineMeeting.setAllowedPresenters(OnlineMeetingPresenters.Organizer);
			onlineMeeting.setAllowMeetingChat(MeetingChatMode.Enabled);

			OnlineMeeting meeting = getGraphClient().users().byUserId(organizerUser.getId()).onlineMeetings()
				.post(onlineMeeting);
			
			result = new TeamsMeetingData();
			result.setId(meeting.getId());
			result.setJoinUrl(meeting.getJoinWebUrl());
		}
		return result;
	}
	
	@Override
	public void updateOnlineMeeting(String userEmail, String meetingId, String subject, Instant startDate, Instant endDate, List<String> coorganizerEmails) throws MicrosoftCredentialsException {
		// Get organizer user
		MicrosoftUser organizerUser = getUserByEmail(userEmail);
		
		if(organizerUser != null) {
			// Online Meeting
			OnlineMeeting onlineMeeting = new OnlineMeeting();
			onlineMeeting.setStartDateTime(OffsetDateTime.ofInstant(startDate, ZoneId.systemDefault()));
			onlineMeeting.setEndDateTime(OffsetDateTime.ofInstant(endDate, ZoneId.systemDefault()));
			onlineMeeting.setSubject(subject);

			MeetingParticipants participants = new MeetingParticipants();

			// Coorganizers
			List<MeetingParticipantInfo> attendees = new ArrayList<>();
			if (coorganizerEmails != null) {
				for (String coorganizerEmail : coorganizerEmails) {
					if (!coorganizerEmail.equals(organizerUser.getEmail())) {
						MicrosoftUser coorganizerUser = getUserByEmail(coorganizerEmail);
						if (coorganizerUser != null) {
							MeetingParticipantInfo coorganizer = new MeetingParticipantInfo();
							IdentitySet coorganizerIdentity = new IdentitySet();
							Identity coorganizerIden = new Identity();
							coorganizerIden.setId(coorganizerUser.getId());
							coorganizerIden.setDisplayName(coorganizerUser.getName());
							coorganizerIdentity.setUser(coorganizerIden);
							coorganizer.setIdentity(coorganizerIdentity);
							coorganizer.setRole(OnlineMeetingRole.Coorganizer);
							coorganizer.setUpn(coorganizerUser.getEmail());
							attendees.add(coorganizer);
						}
					}
				}
				participants.setAttendees(attendees);
			}
			onlineMeeting.setParticipants(participants);
			getGraphClient().users().byUserId(organizerUser.getId()).onlineMeetings().byOnlineMeetingId(meetingId)
					.patch(onlineMeeting);
		}
	}
	
	@Override
	public List<MeetingRecordingData> getOnlineMeetingRecordings(String onlineMeetingId, List<String> teamIdsList, boolean force) throws MicrosoftCredentialsException{
		List<MeetingRecordingData> ret = new ArrayList<>();
		
		//get from cache (if not forced)
		if(!force) {
			Cache.ValueWrapper cachedValue = getCache().get(CACHE_RECORDINGS+onlineMeetingId);
			if(cachedValue != null) {
				return (List<MeetingRecordingData>)cachedValue.get();
			}
		}
		ChatMessageCollectionResponse page = getGraphClient().chats().byChatId(onlineMeetingId).messages()
				.get();
		
		while (page != null) {
			//explore chat messages from given meeting
			for(ChatMessage message : page.getValue()) {
				//we only want "success" recording messages
				if(message.getEventDetail() != null && message.getEventDetail() instanceof CallRecordingEventMessageDetail) {
					CallRecordingEventMessageDetail details = (CallRecordingEventMessageDetail)message.getEventDetail();
					if(details.getCallRecordingStatus() == CallRecordingStatus.Success) {
						try {
							MeetingRecordingData.MeetingRecordingDataBuilder builder = MeetingRecordingData.builder()
								.id(details.getCallId())
								.name(details.getCallRecordingDisplayName())
								.url(details.getCallRecordingUrl())
								.organizerId(details.getInitiator().getUser().getId());
							
							//get driveItem (file in one-drive) from given webURL
							//we will use this call to check if link is still valid
							MicrosoftDriveItem driveItem = getDriveItemFromLink(details.getCallRecordingUrl());
							if(driveItem != null) {
								if(teamIdsList != null) {
									for(String teamId : teamIdsList) {
										//add permissions for sharing (we want all Team users to access the recording, not only the assistant ones)
										if(grantReadPermissionToTeam(driveItem.getDriveId(), driveItem.getId(), teamId)) {
											//granted permission to Team -> we replace shared URL (from chat) with basic URL
											builder.url(driveItem.getUrl());
										}
									}
								}
								ret.add(builder.build());
							}
						}catch(Exception e) {
							log.debug("Error getting chat message chatId={}, messageId={}", onlineMeetingId, message.getId());
						}
					}
				}
			}
			String nextLink = page.getOdataNextLink();
			if (nextLink == null) break;
			page = getGraphClient().chats().byChatId(onlineMeetingId).messages().withUrl(nextLink)
					.get();
		}
		//update cache
		getCache().put(CACHE_RECORDINGS+onlineMeetingId, ret);

		return ret;
	}

	@Override
	public List<AttendanceRecord> getMeetingAttendanceReport(String onlineMeetingId, String userEmail) throws MicrosoftCredentialsException {

		List<AttendanceRecord> attendanceRecordsResponse = new ArrayList<>();

		MicrosoftUser organizerUser = getUserByEmail(userEmail);

		MeetingAttendanceReportCollectionResponse attendanceReports = getGraphClient()
				.users()
				.byUserId(organizerUser.getId())
				.onlineMeetings()
				.byOnlineMeetingId(onlineMeetingId)
				.attendanceReports()
				.get();

		if (attendanceReports != null && attendanceReports.getValue() != null) {
			for (com.microsoft.graph.models.MeetingAttendanceReport report : attendanceReports.getValue()) {
				String reportId = report.getId();

				AttendanceRecordCollectionResponse attendanceRecords = getGraphClient()
						.users()
						.byUserId(organizerUser.getId())
						.onlineMeetings()
						.byOnlineMeetingId(onlineMeetingId)
						.attendanceReports()
						.byMeetingAttendanceReportId(reportId)
						.attendanceRecords()
						.get();

				if (attendanceRecords != null && attendanceRecords.getValue() != null) {
					for (com.microsoft.graph.models.AttendanceRecord record : attendanceRecords.getValue()) {
						AttendanceRecord response = new AttendanceRecord();

						if (record.getEmailAddress() != null) response.setEmail(record.getEmailAddress());
						if (record.getId() != null) response.setId(record.getId());
						if (record.getIdentity().getDisplayName() != null) response.setDisplayName(record.getIdentity().getDisplayName());
						if (record.getRole() != null) response.setRole(record.getRole());
						if (record.getTotalAttendanceInSeconds() != null) response.setTotalAttendanceInSeconds(record.getTotalAttendanceInSeconds());

						List<AttendanceInterval> intervals = new ArrayList<>();
						if (record.getAttendanceIntervals() != null) {
							for (com.microsoft.graph.models.AttendanceInterval interval : record.getAttendanceIntervals()) {
								AttendanceInterval intervalResponse = new AttendanceInterval();
								if (interval.getJoinDateTime() != null) intervalResponse.setJoinDateTime(interval.getJoinDateTime().toString());
								if (interval.getLeaveDateTime() != null) intervalResponse.setLeaveDateTime(interval.getLeaveDateTime().toString());
								if (interval.getDurationInSeconds() != null) intervalResponse.setDurationInSeconds(interval.getDurationInSeconds());
								intervals.add(intervalResponse);
							}
						}
						response.setAttendanceIntervals(intervals);


						attendanceRecordsResponse.add(response);

					}
				}

			}


		}
		return attendanceRecordsResponse;
	}


	@Transactional(readOnly = true)
	public byte[] createAttendanceReportCsv(List<AttendanceRecord> attendanceRecords, List<String> columnNames) {
		if (columnNames == null || columnNames.size() != COLUMN_SIZE) {
			throw new IllegalArgumentException("Expected exactly 7 column names");
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), CSVFormat.DEFAULT
				.withHeader(columnNames.toArray(new String[0])))) {

			for (AttendanceRecord record : attendanceRecords) {
				for (AttendanceInterval interval : record.getAttendanceIntervals()) {
					csvPrinter.printRecord(
							record.getDisplayName(),
							record.getEmail(),
							record.getRole(),
							record.getTotalAttendanceInSeconds(),
							AttendanceInterval.formatDateTime(interval.getJoinDateTime()),
							AttendanceInterval.formatDateTime(interval.getLeaveDateTime()),
							interval.getDurationInSeconds()
					);
				}
			}
		} catch (IOException e) {
			log.error("Error creating CSV file", e);
		}

		return out.toByteArray();
	}

	// ---------------------------------------- ONE-DRIVE (APPLICATION) --------------------------------------------------------
	@Override
	public List<MicrosoftDriveItem> getGroupDriveItems(String groupId) throws MicrosoftCredentialsException {
		return getGroupDriveItems(groupId, null);
	}
	
	@Override
	public List<MicrosoftDriveItem> getGroupDriveItems(String groupId, List<String> channelIds) throws MicrosoftCredentialsException {
		List<MicrosoftDriveItem> ret = getGroupDriveItemsByItemId(groupId, null);
		//at this point we only have root files and folders, excluding private channels folders.
		//we need to get all private channels from Team (bypassing the cache), and the DriveItem related to it
		for(String channelId : (channelIds == null) ? getTeamPrivateChannels(groupId, true).keySet() : channelIds) {
			MicrosoftDriveItem item = getDriveItemFromChannel(groupId, channelId);
			if(item != null) {
				ret.add(item);
			}
		}
		return ret;
	}
	
	@Override
	public List<MicrosoftDriveItem> getGroupDriveItemsByItemId(String groupId, String itemId) throws MicrosoftCredentialsException {
		
		String cacheItemKey = itemId;
		if(cacheItemKey == null) {
			cacheItemKey = "ROOT";
		}
		
		Map<String, List<MicrosoftDriveItem>> driveItemsMap = null;
		Cache.ValueWrapper cachedValue = getCache().get(CACHE_DRIVE_ITEMS_GROUP + groupId);
		if(cachedValue != null) {
			driveItemsMap = (Map<String, List<MicrosoftDriveItem>>)cachedValue.get();
			if(driveItemsMap.containsKey(cacheItemKey) && driveItemsMap.get(cacheItemKey) != null) {
				return driveItemsMap.get(cacheItemKey);
			}
		}
		
		List<MicrosoftDriveItem> ret = new ArrayList<>();
		try {
			GraphServiceClient client = getGraphClient();
			Drive drive = client.me().drive().get();
			String driveId = drive.getId();
			
			DriveItemCollectionResponse itemPage = null;
			if(itemId != null) {
				itemPage = client.drives().byDriveId(client.me().drive().get().getId()).items().byDriveItemId(itemId).children().get();
			} else {
				itemPage = client.drives().byDriveId(driveId).items().byDriveItemId("root").children().get();
			}
			while (itemPage != null) {
				ret.addAll(itemPage.getValue().stream().map(item -> {
					Map<String, Object> additionalData = item.getAdditionalData();
					String downloadUrl = (additionalData != null && additionalData.containsKey("@microsoft.graph.downloadUrl"))
							? additionalData.get("@microsoft.graph.downloadUrl").toString() : null;
					return MicrosoftDriveItem.builder()
						.id(item.getId())
						.name(item.getName())
						.url(item.getWebUrl())
						.createdAt((item.getCreatedDateTime() != null) ? item.getCreatedDateTime().atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
						.modifiedAt((item.getLastModifiedDateTime() != null) ? item.getLastModifiedDateTime().atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
						.modifiedBy((item.getLastModifiedBy() != null && item.getLastModifiedBy().getUser() != null) ? item.getLastModifiedBy().getUser().getDisplayName() : null)
						.driveId((item.getParentReference() != null) ? item.getParentReference().getDriveId() : null)
						.downloadURL(downloadUrl)
						.path((item.getParentReference() != null) ? item.getParentReference().getPath() : null)
						.folder(item.getFolder() != null)
						.childCount((item.getFolder() != null) ? item.getFolder().getChildCount() : 0)
						.size(item.getSize())
						.mimeType((item.getFile() != null) ? item.getFile().getMimeType() : null)
						.build();
					}).collect(Collectors.toList())
				);
				String nextLink = itemPage.getOdataNextLink();
				if (nextLink == null) break;

				itemPage = new ChildrenRequestBuilder(nextLink, client.getRequestAdapter()).get();
			}
			
			if(driveItemsMap == null) {
				driveItemsMap = new HashMap<>();
			}
			driveItemsMap.put(cacheItemKey, ret);
			getCache().put(CACHE_DRIVE_ITEMS_GROUP + groupId, driveItemsMap);
		}catch(MicrosoftCredentialsException e) {
			throw e;
		} catch(Exception e) {
			log.debug("Error getting Drive Items for group={} and itemId={}", groupId, itemId);
		}
		return ret;
	}
	
	@Override
	public List<MicrosoftDriveItem> getAllGroupDriveItems(String groupId, List<String> channelIds, MicrosoftDriveItemFilter filter) throws MicrosoftCredentialsException {
		List<MicrosoftDriveItem> ret = new ArrayList<>();
		try {
			ret.addAll(getGroupDriveItems(groupId, channelIds)
					.stream()
					.filter(i -> (filter != null) ? filter.matches(i) : true)
					.collect(Collectors.toList())
			);
			
			for(MicrosoftDriveItem baseItem : ret) {
				if(baseItem.isFolder() && baseItem.hasChildren()) {
					exploreDriveItem(baseItem, filter, null);
				}
			}
		}catch(MicrosoftCredentialsException e) {
			throw e;
		} catch(Exception e) {
			log.debug("Error getting All Drive Items for groupId={}", groupId);
		}
		return ret;
	}
	
	@Override
	public MicrosoftDriveItem getDriveItemFromLink(String link) throws MicrosoftCredentialsException {
		MicrosoftDriveItem ret = null;
		try {
			DriveItem item = getGraphClient().shares().bySharedDriveItemId(encodeWebURL(link)).driveItem().get();
			ret = MicrosoftDriveItem.builder()
					.id(item.getId())
					.name(item.getName())
					.url(item.getWebUrl())
					.driveId((item.getParentReference() != null) ? item.getParentReference().getDriveId() : null)
					.build();
		}catch(MicrosoftCredentialsException e) {
			throw e;
		}catch (Exception e) {
			log.debug("Error getting driveItem from link={}", link);
		}
		return ret;
	}
	
	@Override
	public MicrosoftDriveItem getDriveItemFromTeam(String teamId) throws MicrosoftCredentialsException {
		MicrosoftDriveItem ret = null;
		try {
			Drive drive = getGraphClient()
					.groups()
					.byGroupId(teamId)
					.drive()
					.get();
			String driveId = drive.getId();

			DriveItem item = getGraphClient()
					.drives()
					.byDriveId(driveId)
					.items()
					.byDriveItemId("root")
					.get();

			ret = MicrosoftDriveItem.builder()
					.id(item.getId())
					.name(item.getName())
					.url(item.getWebUrl())
					.createdAt((item.getCreatedDateTime() != null) ? item.getCreatedDateTime().atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
					.modifiedAt((item.getLastModifiedDateTime() != null) ? item.getLastModifiedDateTime().atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
					.modifiedBy((item.getLastModifiedBy() != null && item.getLastModifiedBy().getUser() != null) ? item.getLastModifiedBy().getUser().getDisplayName() : null)
					.driveId((item.getParentReference() != null) ? item.getParentReference().getDriveId() : null)
					.depth(0)
					.folder(item.getFolder() != null)
					.childCount((item.getFolder() != null) ? item.getFolder().getChildCount() : 0)
					.size(item.getSize())
					.build();
		}catch(MicrosoftCredentialsException e) {
			throw e;
		}catch (Exception e) {
			log.debug("Error getting driveItem from team={}", teamId);
		}
		return ret;
	}
	
	@Override
	public MicrosoftDriveItem getDriveItemFromChannel(String teamId, String channelId) throws MicrosoftCredentialsException {
		MicrosoftDriveItem ret = null;
		try {
			DriveItem item = getGraphClient()
					.teams()
					.byTeamId(teamId)
					.channels()
					.byChannelId(channelId)
					.filesFolder()
					.get();
			ret = MicrosoftDriveItem.builder()
					.id(item.getId())
					.name(item.getName())
					.url(item.getWebUrl())
					.createdAt((item.getCreatedDateTime() != null) ? item.getCreatedDateTime().atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
					.modifiedAt((item.getLastModifiedDateTime() != null) ? item.getLastModifiedDateTime().atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
					.modifiedBy((item.getLastModifiedBy() != null && item.getLastModifiedBy().getUser() != null) ? item.getLastModifiedBy().getUser().getDisplayName() : null)
					.driveId((item.getParentReference() != null) ? item.getParentReference().getDriveId() : null)
					.depth(0)
					.folder(item.getFolder() != null)
					.childCount((item.getFolder() != null) ? item.getFolder().getChildCount() : 0)
					.size(item.getSize())
					.build();
		}catch(MicrosoftCredentialsException e) {
			throw e;
		}catch (Exception e) {
			log.debug("Error getting driveItem from team={} and channel={}", teamId, channelId);
		}
		return ret;
	}
	
	@Override
	public boolean grantReadPermissionToTeam(String driveId, String itemId, String teamId) throws MicrosoftCredentialsException {
		try {
			DriveRecipient recipient = new DriveRecipient();
			recipient.setObjectId(teamId);
			
			LinkedList<DriveRecipient> recipientsList = new LinkedList<DriveRecipient>();
			recipientsList.add(recipient);
			
			LinkedList<String> rolesList = new LinkedList<String>();
			rolesList.add(PERMISSION_READ);
			
			InvitePostRequestBody inviteBody = new InvitePostRequestBody();
			inviteBody.setRequireSignIn(true);
			inviteBody.setSendInvitation(false);
			inviteBody.setRoles(rolesList);
			inviteBody.setRecipients(recipientsList);
			
			getGraphClient()
					.drives()
					.byDriveId(driveId)
					.items()
					.byDriveItemId(itemId)
					.invite()
					.post(inviteBody);
			return true;
		}catch(MicrosoftCredentialsException e) {
			throw e;
		}catch (Exception e) {
			log.debug("Error adding permissions for itemId={} from drive={} to teamId={}", itemId, driveId, teamId);
		}
		return false;
	}
	
	@Override
	public String createLinkForTeams(MicrosoftDriveItem item, List<String> teamIds, PermissionRoles role) throws MicrosoftCredentialsException {
		String ret = null;
		if(item != null) {
			ret = createLinkForTeams(item.getDriveId(), item.getId(), teamIds, role);
			item.setLinkURL(ret);
		}
		return ret;
	}
	
	@Override
	public String createLinkForTeams(String driveId, String itemId, List<String> teamIds, PermissionRoles role) throws MicrosoftCredentialsException {
		CreateLinkPostRequestBody createLinkBody = new CreateLinkPostRequestBody();
		createLinkBody.setType((role == PermissionRoles.WRITE) ? LINK_TYPE_EDIT : LINK_TYPE_VIEW);
		createLinkBody.setScope(LINK_SCOPE_USERS);
		try {
			Permission p = getGraphClient()
					.drives()
					.byDriveId(driveId)
					.items()
					.byDriveItemId(itemId)
					.createLink()
					.post(createLinkBody);
			
			if(p != null) {				
				List<DriveRecipient> recipientsList = teamIds.stream()
					.map(id -> { 
						DriveRecipient r = new DriveRecipient();
						r.setObjectId(id);
						return r; 
					})
					.collect(Collectors.toList());
				
				LinkedList<String> rolesList = new LinkedList<String>();
				rolesList.add((role == PermissionRoles.WRITE) ? PERMISSION_WRITE : PERMISSION_READ);
				
				GrantPostRequestBody grantBody = new GrantPostRequestBody();
				grantBody.setRoles(rolesList);
				grantBody.setRecipients(recipientsList);
				
				getGraphClient()
						.shares()
						.bySharedDriveItemId(p.getShareId())
						.permission()
						.grant()
						.post(grantBody);
				
				return p.getLink().getWebUrl();
			}
		}catch(MicrosoftCredentialsException e) {
			throw e;
		}catch (Exception e) {
			log.debug("Error creating link for itemId={} from drive={} to teamIds={}", itemId, driveId, teamIds);
		}
		return null;
	}
	
	@Override
	public String getThumbnail(MicrosoftDriveItem item, Integer maxWidth, Integer maxHeight) throws MicrosoftCredentialsException {
		String ret = null;
		try {
			ThumbnailSet thumbnailSet = getGraphClient()
				.drives()
				.byDriveId(item.getDriveId())
				.items()
				.byDriveItemId(item.getId())
				.thumbnails()
				.byThumbnailSetId("0") //is always zero?
				.get();
			
			if(maxWidth != null && maxHeight != null && maxWidth > 0 && maxHeight > 0) {
				if(thumbnailSet.getLarge().getWidth() <= maxWidth && thumbnailSet.getLarge().getHeight() <= maxHeight) {
					ret = thumbnailSet.getLarge().getUrl();
				} else if(thumbnailSet.getMedium().getWidth() <= maxWidth && thumbnailSet.getMedium().getHeight() <= maxHeight) {
					ret = thumbnailSet.getMedium().getUrl();
				} else {
					ret = thumbnailSet.getSmall().getUrl();
				}
			} else {
				ret = thumbnailSet.getLarge().getUrl();
			}
			
			item.setThumbnail(ret);
		}catch(MicrosoftCredentialsException e) {
			throw e;
		}catch (Exception e) {
			log.debug("Error gettting Thumbnail for itemId={} from drive={}", item.getId(), item.getDriveId());
		}
		return ret;
	}
	
	@Override
	public boolean deleteDriveItem(MicrosoftDriveItem item) throws MicrosoftCredentialsException {
		try {
			getGraphClient().drives().byDriveId(item.getDriveId()).items().byDriveItemId(item.getId())
				.delete();
			return true;
		}catch(MicrosoftCredentialsException e) {
			throw e;
		}catch (Exception e) {
			log.warn("Error deleting DriveItem drive={}, id={}", item.getDriveId(), item.getId());
		}
		return false;
	}
	
	// ---------------------------------------- ONE-DRIVE (DELEGATED) --------------------------------------------------------
	@Override
	public List<MicrosoftDriveItem> getMyDriveItems(String userId) throws MicrosoftCredentialsException {
		return getMyDriveItemsByItemId(userId, null);
	}
	
	@Override
	public List<MicrosoftDriveItem> getMyDriveItemsByItemId(String userId, String itemId) throws MicrosoftCredentialsException {
		
		String cacheItemKey = itemId;
		if(cacheItemKey == null) {
			cacheItemKey = "ROOT";
		}
		
		Map<String, List<MicrosoftDriveItem>> driveItemsMap = null;
		Cache.ValueWrapper cachedValue = getCache().get(CACHE_DRIVE_ITEMS_USER + userId);
		if(cachedValue != null) {
			driveItemsMap = (Map<String, List<MicrosoftDriveItem>>)cachedValue.get();
			if(driveItemsMap.containsKey(cacheItemKey) && driveItemsMap.get(cacheItemKey) != null) {
				return driveItemsMap.get(cacheItemKey);
			}
		}
		
		List<MicrosoftDriveItem> ret = new ArrayList<>();
		try {
			GraphServiceClient client = (GraphServiceClient)microsoftAuthorizationService.getDelegatedGraphClient(userId);
			Drive drive = client.me().drive().get();
			String driveId = drive.getId();
			
			DriveItemCollectionResponse itemPage = null;
			if(itemId != null) {
				itemPage = client.drives().byDriveId(client.me().drive().get().getId()).items().byDriveItemId(itemId).children().get();
			} else {
				itemPage = client.drives().byDriveId(driveId).items().byDriveItemId("root").children().get();
			}
			while (itemPage != null) {
				ret.addAll(itemPage.getValue().stream().map(item -> {
					Map<String, Object> additionalData = item.getAdditionalData();
					String downloadUrl = (additionalData != null && additionalData.containsKey("@microsoft.graph.downloadUrl"))
							? additionalData.get("@microsoft.graph.downloadUrl").toString() : null;
					return MicrosoftDriveItem.builder()
						.id(item.getId())
						.name(item.getName())
						.url(item.getWebUrl())
						.createdAt((item.getCreatedDateTime() != null) ? item.getCreatedDateTime().atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
						.modifiedAt((item.getLastModifiedDateTime() != null) ? item.getLastModifiedDateTime().atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
						.modifiedBy((item.getLastModifiedBy() != null && item.getLastModifiedBy().getUser() != null) ? item.getLastModifiedBy().getUser().getDisplayName() : null)
						.driveId((item.getParentReference() != null) ? item.getParentReference().getDriveId() : null)
						.downloadURL(downloadUrl)
						.path((item.getParentReference() != null) ? item.getParentReference().getPath() : null)
						.folder(item.getFolder() != null)
						.childCount((item.getFolder() != null) ? item.getFolder().getChildCount() : 0)
						.size(item.getSize())
						.mimeType((item.getFile() != null) ? item.getFile().getMimeType() : null)
						.build();
					}).collect(Collectors.toList())
				);
				String nextLink = itemPage.getOdataNextLink();
				if (nextLink == null) break;

				itemPage = new ChildrenRequestBuilder(nextLink, client.getRequestAdapter()).get();
			}
			
			if(driveItemsMap == null) {
				driveItemsMap = new HashMap<>();
			}
			driveItemsMap.put(cacheItemKey, ret);
			getCache().put(CACHE_DRIVE_ITEMS_USER + userId, driveItemsMap);
		}catch(MicrosoftCredentialsException e) {
			throw e;
		} catch(Exception e) {
			log.debug("Error getting (delegated) Drive Items for user={} and itemId={}", userId, itemId);
		}
		return ret;
	}
	
	@Override
	public List<MicrosoftDriveItem> getAllMyDriveItems(String userId, MicrosoftDriveItemFilter filter) throws MicrosoftCredentialsException {
		List<MicrosoftDriveItem> ret = new ArrayList<>();
		try {
			ret.addAll(getMyDriveItems(userId)
					.stream()
					.filter(i -> (filter != null) ? filter.matches(i) : true)
					.collect(Collectors.toList())
			);
			
			for(MicrosoftDriveItem baseItem : ret) {
				if(baseItem.isFolder() && baseItem.hasChildren()) {
					exploreDriveItem(baseItem, filter, userId);
				}
			}
		}catch(MicrosoftCredentialsException e) {
			throw e;
		} catch(Exception e) {
			log.debug("Error getting All My Drive Items for userId={}", userId);
		}
		return ret;
	}
	
	@Override
	public List<MicrosoftDriveItem> getMySharedDriveItems(String userId) throws MicrosoftCredentialsException {
		String cacheItemKey = "SHARED";
		
		Map<String, List<MicrosoftDriveItem>> driveItemsMap = null;
		Cache.ValueWrapper cachedValue = getCache().get(CACHE_DRIVE_ITEMS_USER + userId);
		if(cachedValue != null) {
			driveItemsMap = (Map<String, List<MicrosoftDriveItem>>)cachedValue.get();
			if(driveItemsMap.containsKey(cacheItemKey) && driveItemsMap.get(cacheItemKey) != null) {
				return driveItemsMap.get(cacheItemKey);
			}
		}
		
		List<MicrosoftDriveItem> ret = new ArrayList<>();
		try {
			GraphServiceClient client = (GraphServiceClient)microsoftAuthorizationService.getDelegatedGraphClient(userId);

			RequestInformation requestInfo = new RequestInformation();
			requestInfo.httpMethod = HttpMethod.GET;
			requestInfo.urlTemplate = "https://graph.microsoft.com/v1.0/me/drive/sharedWithMe";

			DriveItemCollectionResponse itemPage = graphClient
					.getRequestAdapter()
					.send(requestInfo, null, DriveItemCollectionResponse::createFromDiscriminatorValue);

			while (itemPage != null) {
				ret.addAll(itemPage.getValue().stream().map(item -> {
					return MicrosoftDriveItem.builder()
						.id(item.getId())
						.name(item.getName())
						.url(item.getWebUrl())
						.createdAt((item.getCreatedDateTime() != null) ? item.getCreatedDateTime().atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
						.modifiedAt((item.getLastModifiedDateTime() != null) ? item.getLastModifiedDateTime().atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
						.modifiedBy((item.getLastModifiedBy() != null && item.getLastModifiedBy().getUser() != null) ? item.getLastModifiedBy().getUser().getDisplayName() : null)
						.driveId((item.getRemoteItem() != null && item.getRemoteItem().getParentReference() != null) ? item.getRemoteItem().getParentReference().getDriveId() : null)
						.shared(true) //IMPORTANT: identify these items as shared (they have uncompleted data)
						.downloadURL(null)
						.depth(0)
						.folder(item.getFolder() != null)
						.childCount(1) //we don't trust this value from Microsoft. Set to "1", so it will be expandable (in case of a folder)
						.size(item.getSize())
						.mimeType((item.getFile() != null) ? item.getFile().getMimeType() : null)
						.build();
					}).collect(Collectors.toList())
				);
				String nextLink = itemPage.getOdataNextLink();
				if (nextLink == null) break;

				RequestInformation nextRequestInfo = new RequestInformation();
				nextRequestInfo.httpMethod = HttpMethod.GET;
				nextRequestInfo.urlTemplate = nextLink;

				itemPage = client
						.getRequestAdapter()
						.send(nextRequestInfo, null, DriveItemCollectionResponse::createFromDiscriminatorValue);
			}
			
			if(driveItemsMap == null) {
				driveItemsMap = new HashMap<>();
			}
			driveItemsMap.put(cacheItemKey, ret);
			getCache().put(CACHE_DRIVE_ITEMS_USER + userId, driveItemsMap);
		}catch(MicrosoftCredentialsException e) {
			throw e;
		} catch(Exception e) {
			log.debug("Error getting (delegated) Shared Drive Items for user={}", userId);
		}
		return ret;
	}
	
	@Override
	public List<MicrosoftDriveItem> getAllMySharedDriveItems(String userId, MicrosoftDriveItemFilter filter) throws MicrosoftCredentialsException {
		List<MicrosoftDriveItem> ret = new ArrayList<>();
		try {
			ret.addAll(getMySharedDriveItems(userId)
					.stream()
					.filter(i -> (filter != null) ? filter.matches(i) : true)
					.collect(Collectors.toList())
			);
			
			for(MicrosoftDriveItem baseItem : ret) {
				if(baseItem.isFolder() && baseItem.hasChildren()) {
					exploreDriveItem(baseItem, filter, userId);
				}
			}
		}catch(MicrosoftCredentialsException e) {
			throw e;
		} catch(Exception e) {
			log.debug("Error getting All My Shared Drive Items for userId={}", userId);
		}
		return ret;
	}
	
	// ---------------------------------------- ONE-DRIVE (MIXED) --------------------------------------------------------
	@Override
	public MicrosoftDriveItem getDriveItem(String driveId, String itemId, String delegatedUserId) throws MicrosoftCredentialsException{
		String cacheItemKey = itemId;
		
		Map<String, Map<String, Object>> driveItemsMap = null;
		Cache.ValueWrapper cachedValue = getCache().get(CACHE_DRIVE_ITEMS);
		if(cachedValue != null) {
			driveItemsMap = (Map<String, Map<String, Object>>)cachedValue.get();
			if(driveItemsMap != null && driveItemsMap.get(driveId) != null && driveItemsMap.get(driveId).get(cacheItemKey) != null) {
				return (MicrosoftDriveItem)driveItemsMap.get(driveId).get(cacheItemKey);
			}
		}
		
		MicrosoftDriveItem ret = null;
		try {
			DriveItem item = null;
			if(StringUtils.isNotBlank(delegatedUserId)) {
				GraphServiceClient client = (GraphServiceClient)microsoftAuthorizationService.getDelegatedGraphClient(delegatedUserId);
				item = client.drives().byDriveId(driveId).items().byDriveItemId(itemId).get();
			} else {
				item = getGraphClient().drives().byDriveId(driveId).items().byDriveItemId(itemId).get();
			}
			
			if (item != null) {
				Map<String, Object> additionalData = item.getAdditionalData();
				String downloadUrl = (additionalData != null && additionalData.containsKey("@microsoft.graph.downloadUrl"))
						? additionalData.get("@microsoft.graph.downloadUrl").toString() : null;
				ret = MicrosoftDriveItem.builder()
						.id(item.getId())
						.name(item.getName())
						.url(item.getWebUrl())
						.createdAt((item.getCreatedDateTime() != null) ? item.getCreatedDateTime().atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
						.modifiedAt((item.getLastModifiedDateTime() != null) ? item.getLastModifiedDateTime().atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
						.modifiedBy((item.getLastModifiedBy() != null && item.getLastModifiedBy().getUser() != null) ? item.getLastModifiedBy().getUser().getDisplayName() : null)
						.driveId((item.getParentReference() != null) ? item.getParentReference().getDriveId() : null)
						.downloadURL(downloadUrl)
						.path((item.getParentReference() != null) ? item.getParentReference().getPath() : null)
						.folder(item.getFolder() != null)
						.childCount((item.getFolder() != null) ? item.getFolder().getChildCount() : 0)
						.size(item.getSize())
						.mimeType((item.getFile() != null) ? item.getFile().getMimeType() : null)
						.build();
			}
			
			if(driveItemsMap == null) {
				driveItemsMap = new HashMap<>();
			}
			driveItemsMap.computeIfAbsent(driveId, k -> new HashMap<>()).put(cacheItemKey, ret);
			getCache().put(CACHE_DRIVE_ITEMS, driveItemsMap);
		}catch(MicrosoftCredentialsException e) {
			throw e;
		} catch(Exception e) {
			log.debug("Error getting Drive Items for driveId={} and itemId={}", driveId, itemId);
		}
		return ret;
	}
	
	@Override
	public List<MicrosoftDriveItem> getDriveItems(String driveId, String delegatedUserId) throws MicrosoftCredentialsException {
		return getDriveItemsByItemId(driveId, null, delegatedUserId);
	}
	
	@Override
	public List<MicrosoftDriveItem> getDriveItemsByItemId(String driveId, String itemId, String delegatedUserId) throws MicrosoftCredentialsException {
		
		String cacheItemKey = itemId;
		if(cacheItemKey == null) {
			cacheItemKey = "ROOT";
		}
		
		Map<String, Map<String, Object>> driveItemsMap = null;
		Cache.ValueWrapper cachedValue = getCache().get(CACHE_DRIVE_ITEMS);
		if(cachedValue != null) {
			driveItemsMap = (Map<String, Map<String, Object>>)cachedValue.get();
			if(driveItemsMap != null && driveItemsMap.get(driveId) != null && driveItemsMap.get(driveId).get(cacheItemKey) != null) {
				return (List<MicrosoftDriveItem>)driveItemsMap.get(driveId).get(cacheItemKey);
			}
		}
		
		List<MicrosoftDriveItem> ret = new ArrayList<>();
		try {
			DriveItemCollectionResponse itemPage = null;
			if(StringUtils.isNotBlank(delegatedUserId)) {
				GraphServiceClient client = (GraphServiceClient)microsoftAuthorizationService.getDelegatedGraphClient(delegatedUserId);
				if(itemId != null) {
					itemPage = client.drives().byDriveId(driveId).items().byDriveItemId(itemId).children().get();
				} else {
					itemPage = client.drives().byDriveId(driveId).items().byDriveItemId("root").children().get();
				}
			} else {
				if(itemId != null) {
					itemPage = getGraphClient().drives().byDriveId(driveId).items().byDriveItemId(itemId).children().get();
				} else {
					itemPage = getGraphClient().drives().byDriveId(driveId).items().byDriveItemId("root").children().get();
				}
			}
			
			while (itemPage != null) {
				ret.addAll(itemPage.getValue().stream().map(item -> {
					Map<String, Object> additionalData = item.getAdditionalData();
					String downloadUrl = (additionalData != null && additionalData.containsKey("@microsoft.graph.downloadUrl"))
							? additionalData.get("@microsoft.graph.downloadUrl").toString() : null;
					return MicrosoftDriveItem.builder()
						.id(item.getId())
						.name(item.getName())
						.url(item.getWebUrl())
						.createdAt((item.getCreatedDateTime() != null) ? item.getCreatedDateTime().atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
						.modifiedAt((item.getLastModifiedDateTime() != null) ? item.getLastModifiedDateTime().atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
						.modifiedBy((item.getLastModifiedBy() != null && item.getLastModifiedBy().getUser() != null) ? item.getLastModifiedBy().getUser().getDisplayName() : null)
						.driveId((item.getParentReference() != null) ? item.getParentReference().getDriveId() : null)
						.downloadURL(downloadUrl)
						.path((item.getParentReference() != null) ? item.getParentReference().getPath() : null)
						.folder(item.getFolder() != null)
						.childCount((item.getFolder() != null) ? item.getFolder().getChildCount() : 0)
						.size(item.getSize())
						.mimeType((item.getFile() != null) ? item.getFile().getMimeType() : null)
						.build();
					}).collect(Collectors.toList())
				);
				String nextLink = itemPage.getOdataNextLink();
				if (nextLink == null) break;

				if (StringUtils.isNotBlank(delegatedUserId)) {
					GraphServiceClient client = (GraphServiceClient) microsoftAuthorizationService.getDelegatedGraphClient(delegatedUserId);
					itemPage = client.drives().byDriveId(driveId).items().byDriveItemId(itemId != null ? itemId : "root").children().withUrl(nextLink).get();
				} else {
					itemPage = getGraphClient().drives().byDriveId(driveId).items().byDriveItemId(itemId != null ? itemId : "root").children().withUrl(nextLink).get();
				}
			}
			
			if(driveItemsMap == null) {
				driveItemsMap = new HashMap<>();
			}
			driveItemsMap.computeIfAbsent(driveId, k -> new HashMap<>()).put(cacheItemKey, ret);
			getCache().put(CACHE_DRIVE_ITEMS, driveItemsMap);
		}catch(MicrosoftCredentialsException e) {
			throw e;
		} catch(Exception e) {
			log.debug("Error getting Drive Items for driveId={} and itemId={}", driveId, itemId);
		}
		return ret;
	}
	
	@Override
	public MicrosoftDriveItem createDriveItem(MicrosoftDriveItem parent, MicrosoftDriveItem.TYPE type, String name, String delegatedUserId) throws MicrosoftCredentialsException {
		MicrosoftDriveItem ret = null;
		try {
			GraphServiceClient client = null;
			if(StringUtils.isNotBlank(delegatedUserId)) {
				client = (GraphServiceClient)microsoftAuthorizationService.getDelegatedGraphClient(delegatedUserId);
			} else {
				client = getGraphClient();
			}
			
			DriveItem newItem = new DriveItem();
			newItem.setName(name);
			if(type == MicrosoftDriveItem.TYPE.FOLDER) {
				newItem.setFolder(new Folder());
			} else {
				if(!name.toLowerCase().endsWith(type.getExt())) {
					newItem.setName(name + type.getExt());
				}
				newItem.setFile(new com.microsoft.graph.models.File());
			}
			Map<String, Object> additionalData = new HashMap<>();
			additionalData.put("@microsoft.graph.conflictBehavior", "rename");
			newItem.setAdditionalData(additionalData);
			
			
			DriveItem item = client.drives().byDriveId(parent.getDriveId()).items().byDriveItemId(parent.getId()).children().post(newItem);
			ret = MicrosoftDriveItem.builder()
					.id(item.getId())
					.name(item.getName())
					.url(item.getWebUrl())
					.createdAt((item.getCreatedDateTime() != null) ? item.getCreatedDateTime().atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
					.modifiedAt((item.getLastModifiedDateTime() != null) ? item.getLastModifiedDateTime().atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
					.modifiedBy((item.getLastModifiedBy() != null && item.getLastModifiedBy().getUser() != null) ? item.getLastModifiedBy().getUser().getDisplayName() : null)
					.driveId(parent.getDriveId())
					.path((item.getParentReference() != null) ? item.getParentReference().getPath() : null)
					.folder(item.getFolder() != null)
					.childCount(0)
					.size(item.getSize())
					.mimeType((item.getFile() != null) ? item.getFile().getMimeType() : null)
					.build();
		}catch(MicrosoftCredentialsException e) {
			throw e;
		}catch (Exception e) {
			log.warn("Error creating DriveItem name={}, type={} in drive={} and parent={}", name, type, parent.getDriveId(), parent.getId());
		}
		return ret;
	}
	
	@Override
	public MicrosoftDriveItem uploadDriveItem(MicrosoftDriveItem parent, File file, String name, String delegatedUserId) throws MicrosoftCredentialsException {
		MicrosoftDriveItem ret = null;
		try {
			GraphServiceClient client = null;
			if(StringUtils.isNotBlank(delegatedUserId)) {
				client = (GraphServiceClient)microsoftAuthorizationService.getDelegatedGraphClient(delegatedUserId);
			} else {
				client = getGraphClient();
			}
			
			// Get an input stream for the file
			InputStream fileStream = new FileInputStream(file);
			long streamSize = file.length();
	
			final DriveItemUploadableProperties upProps = new DriveItemUploadableProperties();
			Map<String, Object> additionalData = new HashMap<>();
			additionalData.put("@microsoft.graph.conflictBehavior", "rename");
			upProps.setAdditionalData(additionalData);
			
			CreateUploadSessionPostRequestBody uploadParams = new CreateUploadSessionPostRequestBody();
			uploadParams.setItem(upProps);
			
			// Create an upload session
			final UploadSession uploadSession = client
					.drives()
					.byDriveId(parent.getDriveId())
					.items()
					.byDriveItemId(parent.getId() + ":/" + name + ":")
					.createUploadSession()
					.post(uploadParams);
	
			if (null == uploadSession) {
				fileStream.close();
				log.warn("Error creating upload session in drive={} and parent={}", parent.getDriveId(), parent.getId());
				return null;
			}
	
			LargeFileUploadTask<DriveItem> largeFileUploadTask = new LargeFileUploadTask<>(
					client.getRequestAdapter(),
					uploadSession,
					fileStream,
					streamSize,
					DriveItem::createFromDiscriminatorValue);

			// Do the upload
			UploadResult<DriveItem> result = largeFileUploadTask.upload();
			
			DriveItem item = result.itemResponse;
			ret = MicrosoftDriveItem.builder()
				.id(item.getId())
				.name(item.getName())
				.url(item.getWebUrl())
				.createdAt((item.getCreatedDateTime() != null) ? item.getCreatedDateTime().atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
				.modifiedAt((item.getLastModifiedDateTime() != null) ? item.getLastModifiedDateTime().atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
				.modifiedBy((item.getLastModifiedBy() != null && item.getLastModifiedBy().getUser() != null) ? item.getLastModifiedBy().getUser().getDisplayName() : null)
				.driveId(parent.getDriveId())
				.path((item.getParentReference() != null) ? item.getParentReference().getPath() : null)
				.folder(item.getFolder() != null)
				.childCount(0)
				.size(item.getSize())
				.mimeType((item.getFile() != null) ? item.getFile().getMimeType() : null)
				.build();
		}catch(MicrosoftCredentialsException e) {
			throw e;
		}catch(Exception e) {
			log.warn("Error uploading DriveItem name={} to drive={} and parent={}", name, parent.getDriveId(), parent.getId());
		}
		return ret;
	}
	
	private void exploreDriveItem(MicrosoftDriveItem driveItem, MicrosoftDriveItemFilter filter, String delegatedUserId) throws MicrosoftCredentialsException {
		try {
			List<MicrosoftDriveItem> children = getDriveItemsByItemId(driveItem.getDriveId(), driveItem.getId(), delegatedUserId)
					.stream()
					.filter(i -> (filter != null) ? filter.matches(i) : true)
					.collect(Collectors.toList());
			for(MicrosoftDriveItem item : children) {
				if(item.isFolder() && item.hasChildren()) {
					exploreDriveItem(item, filter, delegatedUserId);
				}
			}
			driveItem.setChildren(children);
		}catch(MicrosoftCredentialsException e) {
			throw e;
		} catch(Exception e) {
			log.debug("Error exploring Drive Item with driveId={} and itemId={}", driveItem.getDriveId(), driveItem.getId());
		}
	}
	
	// ---------------------------------------- PRIVATE FUNCTIONS ------------------------------------------------

	private String getMemberKeyValue(AadUserConversationMember member, MicrosoftUserIdentifier key) {
		String ret = null;
		//we can use reflection instead, but it's more inefficient
		//String ret = (String)AadUserConversationMember.class.getField(key).get(member);
		switch(key) {
			case USER_ID:
				ret = member.getUserId();
				break;
	
			case EMAIL:
				ret = (member.getEmail() != null) ? member.getEmail().toLowerCase() : null;
				break;

			default:
				ret = null;
				break;
		}
		return ret;
	}
	
	//https://learn.microsoft.com/en-us/graph/known-issues#create-channel-can-return-an-error-response
	private String formatMicrosoftChannelString(String str) {
		String[] charsToReplace = {"\\~", "#", "%", "&", "\\*", "\\{", "\\}", "\\+", "/", "\\\\", ":", "<", ">", "\\?", "\\|", "‘", "'", "`", "´", "”", "\""};
		for (String c : charsToReplace) {
		    str = str.replaceAll(c, "");
		}
		str = str.replaceAll("^_+", "");
		str = str.replaceAll("^\\.\\.\\.", "");
		str = str.replaceAll("\\.\\.\\.$", "");
		return str;
	}

	private String encodeWebURL(String link) {
		try {
			String base64Link = Base64.getUrlEncoder().encodeToString(link.getBytes());
			return "u!"+base64Link.replaceAll("=+$", "").replace('/', '_').replace('+','-');
		}catch(Exception e) {
			log.error("Error encoding link={}", link);
		}
		return null;
	}
}
