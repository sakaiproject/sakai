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
import java.io.PrintWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
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
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonElement;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.UserCollectionPage;
import com.microsoft.graph.requests.GroupCollectionPage;
import com.microsoft.graph.requests.UserCollectionRequestBuilder;
import com.microsoft.graph.requests.GroupCollectionRequestBuilder;
import com.microsoft.graph.requests.ConversationMemberCollectionResponse;
import com.microsoft.graph.requests.ConversationMemberCollectionPage;
import com.microsoft.graph.requests.ConversationMemberCollectionRequestBuilder;
import com.microsoft.graph.requests.DirectoryObjectCollectionWithReferencesPage;
import com.microsoft.graph.requests.DirectoryObjectCollectionWithReferencesRequestBuilder;
import com.microsoft.graph.requests.ChannelCollectionPage;
import com.microsoft.graph.requests.ChannelCollectionRequestBuilder;
import com.microsoft.graph.requests.ChatMessageCollectionPage;
import com.microsoft.graph.requests.ChatMessageCollectionRequestBuilder;
import com.microsoft.graph.requests.MeetingAttendanceReportCollectionPage;
import com.microsoft.graph.requests.AttendanceRecordCollectionPage;
import com.microsoft.graph.requests.DriveItemCollectionPage;
import com.microsoft.graph.requests.DriveItemCollectionRequestBuilder;
import com.microsoft.graph.requests.DriveSharedWithMeCollectionPage;
import com.microsoft.graph.requests.DriveSharedWithMeCollectionRequestBuilder;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import com.microsoft.graph.content.BatchRequestContent;
import com.microsoft.graph.content.BatchResponseContent;
import com.microsoft.graph.content.BatchResponseStep;
import com.microsoft.graph.http.GraphServiceException;
import com.microsoft.graph.http.HttpMethod;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.ConversationMemberCollectionRequest;
import com.microsoft.graph.requests.GroupRequest;
import com.microsoft.graph.requests.UserCollectionRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.messaging.api.MicrosoftMessage;
import org.sakaiproject.messaging.api.MicrosoftMessage.MicrosoftMessageBuilder;
import org.sakaiproject.messaging.api.MicrosoftMessagingService;
import org.sakaiproject.microsoft.api.MicrosoftAuthorizationService;
import org.sakaiproject.microsoft.api.MicrosoftCommonService;
import org.sakaiproject.microsoft.api.SakaiProxy;
import org.sakaiproject.microsoft.api.data.MicrosoftCredentials;
import org.sakaiproject.microsoft.api.data.MicrosoftUser;
import org.sakaiproject.microsoft.api.data.MicrosoftUserIdentifier;
import org.sakaiproject.microsoft.api.data.MicrosoftTeam;
import org.sakaiproject.microsoft.api.data.MicrosoftMembersCollection;
import org.sakaiproject.microsoft.api.data.MicrosoftChannel;
import org.sakaiproject.microsoft.api.data.SynchronizationStatus;
import org.sakaiproject.microsoft.api.data.TeamsMeetingData;
import org.sakaiproject.microsoft.api.data.MeetingRecordingData;
import org.sakaiproject.microsoft.api.data.AttendanceRecord;
import org.sakaiproject.microsoft.api.data.MicrosoftDriveItem;
import org.sakaiproject.microsoft.api.data.AttendanceInterval;
import org.sakaiproject.microsoft.api.data.MicrosoftDriveItemFilter;
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
import org.sakaiproject.scheduling.api.SchedulingService;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.annotation.Transactional;

import com.microsoft.graph.tasks.LargeFileUploadTask;
import com.microsoft.graph.tasks.LargeFileUploadResult;
import com.microsoft.graph.models.MeetingChatMode;
import com.microsoft.graph.models.AadUserConversationMember;
import com.microsoft.graph.models.CallRecordingEventMessageDetail;
import com.microsoft.graph.models.CallRecordingStatus;
import com.microsoft.graph.models.Channel;
import com.microsoft.graph.models.ChannelMembershipType;
import com.microsoft.graph.models.ChatMessage;
import com.microsoft.graph.models.ConversationMember;
import com.microsoft.graph.models.DirectoryObject;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.models.DriveItemCreateLinkParameterSet;
import com.microsoft.graph.models.DriveItemCreateUploadSessionParameterSet;
import com.microsoft.graph.models.DriveItemInviteParameterSet;
import com.microsoft.graph.models.DriveItemUploadableProperties;
import com.microsoft.graph.models.DriveRecipient;
import com.microsoft.graph.models.Folder;
import com.microsoft.graph.models.Group;
import com.microsoft.graph.models.Identity;
import com.microsoft.graph.models.IdentitySet;
import com.microsoft.graph.models.Invitation;
import com.microsoft.graph.models.LobbyBypassScope;
import com.microsoft.graph.models.LobbyBypassSettings;
import com.microsoft.graph.models.MeetingParticipantInfo;
import com.microsoft.graph.models.MeetingParticipants;
import com.microsoft.graph.models.OnlineMeeting;
import com.microsoft.graph.models.OnlineMeetingPresenters;
import com.microsoft.graph.models.OnlineMeetingRole;
import com.microsoft.graph.models.Permission;
import com.microsoft.graph.models.PermissionGrantParameterSet;
import com.microsoft.graph.models.Team;
import com.microsoft.graph.models.TeamVisibilityType;
import com.microsoft.graph.models.ThumbnailSet;
import com.microsoft.graph.models.UploadSession;
import com.microsoft.graph.models.User;
import com.microsoft.graph.options.HeaderOption;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.requests.ChannelCollectionRequest;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import static org.sakaiproject.microsoft.impl.MicrosoftConfigurationServiceImpl.decrypt;

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
	@Setter private SchedulingService schedulingService;
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

                    String decryptedSecret = decrypt(microsoftCredentials.getSecret());
                    if (decryptedSecret == null) {
                        log.error("Failed to decrypt Microsoft secret. Check your microsoft.encryption.key.");
                        throw new MicrosoftInvalidCredentialsException();
                    }

					AdminAuthProvider authProvider = new AdminAuthProvider(microsoftCredentials.getAuthority(), microsoftCredentials.getClientId(), decryptedSecret, microsoftCredentials.getScope());
					this.graphClient = GraphServiceClient
							.builder()
							.authenticationProvider(authProvider)
							.buildClient();

					//validate credentials (we don't care about the result)
					this.graphClient.organization().buildRequest().get();
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

		UserCollectionPage page = getGraphClient().users()
				.buildRequest()
				.select("id,displayName,mail,userType")
				.get();
		while (page != null) {
			userList.addAll(page.getCurrentPage().stream().map(
					u -> MicrosoftUser.builder()
							.id(u.id)
							.name(u.displayName)
							.email(u.mail)
							.guest(MicrosoftUser.GUEST.equalsIgnoreCase(u.userType))
							.build()).collect(Collectors.toList()));
			userList.forEach(u -> log.debug(u.toString()));
			UserCollectionRequestBuilder builder = page.getNextPage();
			if (builder == null) break;
			page = builder.buildRequest().get();
		}

		HashMap<String, MicrosoftUser> userMap = (HashMap<String, MicrosoftUser>) userList.stream().collect(Collectors.toMap(MicrosoftUser::getId, u -> u));
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
			UserCollectionPage pageCollection = getGraphClient().users()
					.buildRequest()
					.filter("mail eq '" + email + "'")
					.select("id,displayName,mail,userType")
					.get();
			if (pageCollection != null && pageCollection.getCurrentPage().size() > 0) {
				User u = pageCollection.getCurrentPage().get(0);
				MicrosoftUser microsoftUser = MicrosoftUser.builder()
						.id(u.id)
						.name(u.displayName)
						.email(u.mail)
						.guest(MicrosoftUser.GUEST.equalsIgnoreCase(u.userType))
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
		LinkedList<Option> requestOptions = new LinkedList<Option>();
		requestOptions.add(new HeaderOption("ConsistencyLevel", "eventual"));
		requestOptions.add(new QueryOption("$select", "id,displayName,mail,userType"));

		GraphServiceClient graph = getGraphClient();
		Set<String> pendingUsers = userIds.stream().filter(id -> !id.startsWith("EMPTY_") && users.stream().noneMatch(u -> u.getId().equalsIgnoreCase(id))).collect(Collectors.toSet());
		List<String> usersToProcess;
		//sometimes microsoft fails creating -> loop for retry failed ones
		while (!pendingUsers.isEmpty()) {
			usersToProcess = pendingUsers.stream().skip(pointer).limit(MAX_LENGTH).collect(Collectors.toList());

			BatchRequestContent batchRequestContent = new BatchRequestContent();

			usersToProcess.forEach(id -> {
				UserCollectionRequest getUsers = graph.users()
						.buildRequest(requestOptions)
						.filter("userId eq '" + id + "'");

				batchRequestContent.addBatchRequestStep(getUsers, HttpMethod.GET);
			});

			BatchResponseContent responseContent = getGraphClient().batch().buildRequest().post(batchRequestContent);

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
		LinkedList<Option> requestOptions = new LinkedList<Option>();
		requestOptions.add(new HeaderOption("ConsistencyLevel", "eventual"));
		requestOptions.add(new QueryOption("$select", "id,displayName,mail,userType"));

		GraphServiceClient graph = getGraphClient();
		Set<String> pendingUsers = userEmails.stream().filter(email -> !email.startsWith("EMPTY_") && users.stream().noneMatch(u -> u.getEmail().equalsIgnoreCase(email))).collect(Collectors.toSet());
		List<String> usersToProcess;
		//sometimes microsoft fails creating -> loop for retry failed ones
		while (!pendingUsers.isEmpty()) {
			usersToProcess = pendingUsers.stream().skip(pointer).limit(MAX_LENGTH).collect(Collectors.toList());

			BatchRequestContent batchRequestContent = new BatchRequestContent();

			usersToProcess.forEach(email -> {
				UserCollectionRequest getUsers = graph.users()
						.buildRequest(requestOptions)
						.filter("mail eq '" + email + "'");

				batchRequestContent.addBatchRequestStep(getUsers, HttpMethod.GET);
			});

			BatchResponseContent responseContent = getGraphClient().batch().buildRequest().post(batchRequestContent);

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

			User u = getGraphClient().users(id)
					.buildRequest()
					.select("id,displayName,mail,userType")
					.get();
			MicrosoftUser microsoftUser = MicrosoftUser.builder()
					.id(u.id)
					.name(u.displayName)
					.email(u.mail)
					.guest(MicrosoftUser.GUEST.equalsIgnoreCase(u.userType))
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
			invitation.invitedUserEmailAddress = email;
			invitation.inviteRedirectUrl = redirectURL;

			Invitation response = getGraphClient().invitations()
					.buildRequest()
					.post(invitation);

			log.debug("INVITATION RESPONSE: id={}, userId={}, email={}, status={}", response.id, response.invitedUser.id, response.invitedUserEmailAddress, response.status);

			MicrosoftUser microsoftUser = MicrosoftUser.builder()
					.id(response.invitedUser.id)
					.email(response.invitedUserEmailAddress)
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
						.groups(id)
						.buildRequest()
						.select("id,displayName,description")
						.get();
			return MicrosoftTeam.builder()
					.id(group.id)
					.name(group.displayName)
					.description(group.description)
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
						.groups(id)
						.team()
						.buildRequest()
						.select("id,displayName,description")
						.get();
			MicrosoftTeam mt = MicrosoftTeam.builder()
					.id(team.id)
					.name(team.displayName)
					.description(team.description)
					.build();
			
			//update cache
			Map<String, MicrosoftTeam> teamsMap = cachedValue != null ? (Map<String, MicrosoftTeam>) cachedValue.get() : new HashMap<>();
			teamsMap.put(id, mt);
			getCache().put(CACHE_TEAMS, teamsMap);
			return mt;
		}catch(MicrosoftCredentialsException e) {
			throw e;
		} catch (GraphServiceException e) {
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

		LinkedList<Option> requestOptions = new LinkedList<Option>();
		requestOptions.add(new HeaderOption("ConsistencyLevel", "eventual"));

		//Get all groups linked with a team
		GroupCollectionPage page = getGraphClient().groups()
				.buildRequest( requestOptions )
				.filter("resourceProvisioningOptions/Any(x:x eq 'Team')")
				.select("id,displayName,description")
				.get();
		while (page != null) {
			for(Group group : page.getCurrentPage()) {
				//as we only want id and name, with group info is enough. Otherwise we would need to do a request to get the Team info
				teamsMap.put(group.id, MicrosoftTeam.builder()
						.id(group.id)
						.name(group.displayName)
						.description(group.description)
						.build());
			}
			GroupCollectionRequestBuilder builder = page.getNextPage();
			if (builder == null) break;
			page = builder.buildRequest().get();
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
		LinkedList<Option> requestOptions = new LinkedList<Option>();
		requestOptions.add(new HeaderOption("ConsistencyLevel", "eventual"));
		requestOptions.add(new QueryOption("$select", "id,displayName,description"));

		GraphServiceClient graph = getGraphClient();
		List<SiteSynchronization> sitesToProcess;

		//sometimes microsoft fails creating -> loop for retry failed ones
		while (!pendingSites.isEmpty()) {
			int endIndex = Math.min(pointer + MAX_LENGTH, pendingSites.size());
			sitesToProcess = pendingSites.subList(pointer, endIndex);

			BatchRequestContent batchRequestContent = new BatchRequestContent();

			sitesToProcess.forEach(group -> {
				GroupRequest getGroups = graph.groups(group.getTeamId())
						.buildRequest(requestOptions);

				batchRequestContent.addBatchRequestStep(getGroups, HttpMethod.GET);
			});

			BatchResponseContent responseContent = getGraphClient().batch().buildRequest().post(batchRequestContent);

			HashMap<String, ?> teamsResponse = parseBatchResponse(responseContent, sitesToProcess);

			teamsMap.putAll((HashMap<String, MicrosoftTeam>) teamsResponse.get("success"));
			pendingSites.removeAll(sitesToProcess);
			pendingSites.addAll((List<SiteSynchronization>) teamsResponse.get("pending"));
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
			LinkedList<String> rolesList = new LinkedList<String>();
			rolesList.add("owner");

			String truncatedName = processMicrosoftTeamName(name);

			User userOwner = getGraphClient().users(ownerEmail).buildRequest().get();
			AadUserConversationMember conversationMember = new AadUserConversationMember();
			conversationMember.oDataType = "#microsoft.graph.aadUserConversationMember";
			conversationMember.roles = rolesList;
			conversationMember.additionalDataManager().put("user@odata.bind", new JsonPrimitive("https://graph.microsoft.com/v1.0/users('" + userOwner.id + "')"));
			
			LinkedList<ConversationMember> membersList = new LinkedList<ConversationMember>();
			membersList.add(conversationMember);
			
			ConversationMemberCollectionResponse conversationMemberCollectionResponse = new ConversationMemberCollectionResponse();
			conversationMemberCollectionResponse.value = membersList;
			ConversationMemberCollectionPage conversationMemberCollectionPage = new ConversationMemberCollectionPage(conversationMemberCollectionResponse, null);
			
			Team team = new Team();
			team.displayName = truncatedName;
			team.description = truncatedName;
			team.visibility = TeamVisibilityType.PRIVATE;
			team.members = conversationMemberCollectionPage;
			team.additionalDataManager().put("template@odata.bind", new JsonPrimitive("https://graph.microsoft.com/v1.0/teamsTemplates('standard')"));

			Team ret = getGraphClient().teams()
				.buildRequest()
				.post(team);
			
			JsonElement adm = ret.additionalDataManager().get("graphResponseHeaders");
			if(adm != null) {
				String contentLocation = adm.getAsJsonObject().get("content-location").getAsString();
				Pattern teamPattern = Pattern.compile("^/teams\\('([^']+)'\\)$");
				Matcher matcher = teamPattern.matcher(contentLocation);
				if(matcher.find()) {
					String teamId = matcher.group(1);
					//get Teams cache
					Cache.ValueWrapper cachedValue = getCache().get(CACHE_TEAMS);
					if(cachedValue != null) {
						//add newly created team to the cached map
						Map<String, MicrosoftTeam> teamsMap = (Map<String, MicrosoftTeam>)cachedValue.get();
						teamsMap.put(teamId, MicrosoftTeam.builder()
								.id(teamId)
								.name(name)
								.description(name)
								.build());
						
						getCache().put(CACHE_TEAMS, teamsMap);
					}
					
					return teamId;
				}
			}
		}catch(MicrosoftCredentialsException e) {
			throw e;
		} catch(Exception ex){
			log.debug("Error creating Microsoft Team: name={}", name);
		}
		return null;
	}
	
	@Override
	public String createGroup(String name, String ownerEmail) throws MicrosoftCredentialsException {
		try {
			Group group = new Group();
			group.displayName = name;
			group.description = name;
			LinkedList<String> groupTypesList = new LinkedList<String>();
			groupTypesList.add("Unified");
			group.groupTypes = groupTypesList;
			group.mailEnabled = true;
			group.mailNickname = RandomStringUtils.randomAlphanumeric(64);
			group.securityEnabled = false;
			
			User userOwner = getGraphClient().users(ownerEmail).buildRequest().get();
			List<String> ids = Arrays.asList("https://graph.microsoft.com/v1.0/users('" + userOwner.id + "')");
			group.additionalDataManager().put("owners@odata.bind", new Gson().toJsonTree(ids));

			Group newGroup = getGraphClient().groups()
				.buildRequest()
				.post(group);

			return newGroup.id;
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
		team.additionalDataManager().put("template@odata.bind", new JsonPrimitive("https://graph.microsoft.com/v1.0/teamsTemplates('standard')"));
		team.additionalDataManager().put("group@odata.bind", new JsonPrimitive("https://graph.microsoft.com/v1.0/groups('" + groupId + "')"));

		getGraphClient().teams()
				.buildRequest()
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
					} catch(MicrosoftCredentialsException e) {
						log.error("Error creating Team (credentials): " + e.getMessage());
						//send message to (ignite) MicrosoftMessagingService
						microsoftMessagingService.send(MicrosoftMessage.Topic.TEAM_CREATION, builder.status(0).build());
					} catch (Exception e) {
						if (counter.get() < MAX_RETRY) {
							//IMPORTANT: do not remove this debug message
							log.debug("Attempt number: {} failed", counter.getAndIncrement());
							schedulingService.schedule(this, 10, TimeUnit.SECONDS);
						}
						else {
							log.error("Error creating Team: " + e.getMessage());
							//send message to (ignite) MicrosoftMessagingService
							microsoftMessagingService.send(MicrosoftMessage.Topic.TEAM_CREATION, builder.status(0).build());
						}
					}
				}
			}
		};
		//wait 30 sec before first try
		schedulingService.schedule(task, 30, TimeUnit.SECONDS);
	}
	
	@Override
	public boolean deleteTeam(String teamId) throws MicrosoftCredentialsException {
		try {
			getGraphClient().groups(teamId)
				.buildRequest()
				.delete();
			return true;
		}catch(MicrosoftCredentialsException e) {
			throw e;
		} catch(Exception ex){
			log.debug("Error deleting Microsoft group: id={}", teamId);
		}
		return false;
	}

	@Override
	public MicrosoftMembersCollection getTeamMembers(String id, MicrosoftUserIdentifier key) throws MicrosoftCredentialsException {
		MicrosoftMembersCollection ret = new MicrosoftMembersCollection();
		try {
			MicrosoftCredentials credentials = microsoftConfigRepository.getCredentials();
			
			ConversationMemberCollectionPage page = getGraphClient()
					.teams(id)
					.members()
					.buildRequest()
					.get();
			while (page != null) {
				for(ConversationMember m : page.getCurrentPage()) {
					AadUserConversationMember member = (AadUserConversationMember)m;
	
					String identifier = getMemberKeyValue(member, key);
					//avoid insert admin user
					if(StringUtils.isNotBlank(identifier) && !credentials.getEmail().equalsIgnoreCase(member.email)) {
						log.debug(">>MEMBER: ({}) --> displayName={}, roles={}, userId={}, id={}", identifier, member.displayName, member.roles.stream().collect(Collectors.joining(", ")), member.userId, member.id);
	
						MicrosoftUser mu = MicrosoftUser.builder()
								.id(member.userId)
								.name(member.displayName)
								.email(member.email)
								.memberId(member.id)
								.owner(member.roles != null && member.roles.contains(MicrosoftUser.OWNER))
								.guest(member.roles != null && member.roles.contains(MicrosoftUser.GUEST))
								.build();
						
						if(member.roles != null && member.roles.contains(MicrosoftUser.GUEST)) {
							ret.addGuest(identifier, mu);
						} else if(member.roles != null && member.roles.contains(MicrosoftUser.OWNER)) {
							ret.addOwner(identifier, mu);
						} else {
							ret.addMember(identifier, mu);
						}
					}
				}
				ConversationMemberCollectionRequestBuilder builder = page.getNextPage();
				if (builder == null) break;
				page = builder.buildRequest().get();
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
		
		String filter = null;
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
				ConversationMemberCollectionPage page = getGraphClient().teams(teamId).members()
						.buildRequest()
						.filter(filter)
						.get();
				while (page != null) {
					for(ConversationMember m : page.getCurrentPage()) {
						AadUserConversationMember member = (AadUserConversationMember)m;
					
						if(ret == null) {
							ret = MicrosoftUser.builder()
									.id(member.userId)
									.name(member.displayName)
									.email(member.email)
									.memberId(member.id)
									.owner(member.roles != null && member.roles.contains(MicrosoftUser.OWNER))
									.guest(member.roles != null && member.roles.contains(MicrosoftUser.GUEST))
									.build();
						}
					}
					
					ConversationMemberCollectionRequestBuilder builder = page.getNextPage();
					if (builder == null) break;
					page = builder.buildRequest().get();
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
			DirectoryObject directoryObject = new DirectoryObject();
			directoryObject.id = userId;
			
			getGraphClient().groups(groupId).members().references()
					.buildRequest()
					.post(directoryObject);
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
			DirectoryObject directoryObject = new DirectoryObject();
			directoryObject.id = userId;
			
			getGraphClient().groups(groupId).owners().references()
					.buildRequest()
					.post(directoryObject);
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
			conversationMember.oDataType = "#microsoft.graph.aadUserConversationMember";
			conversationMember.roles = new LinkedList<String>();
			
			conversationMember.additionalDataManager().put("user@odata.bind", new JsonPrimitive("https://graph.microsoft.com/v1.0/users('" + userId + "')"));
			
			getGraphClient().teams(teamId).members().buildRequest().post(conversationMember);
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
			conversationMember.oDataType = "#microsoft.graph.aadUserConversationMember";
			conversationMember.roles = rolesList;
			
			conversationMember.additionalDataManager().put("user@odata.bind", new JsonPrimitive("https://graph.microsoft.com/v1.0/users('" + userId + "')"));
			
			getGraphClient().teams(teamId).members().buildRequest().post(conversationMember);
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
		boolean res = false;
		String teamId = ss.getTeamId();
		String dataKey = roles.contains(MicrosoftUser.OWNER) ? "ownerId" : "memberId";
		boolean generalError = false;

		ConversationMemberCollectionRequest postMembers = graphClient.teams(teamId).members()
				.buildRequest();

		int maxRequests = members.size() / MAX_PER_REQUEST;

		for (int i = 0; i <= maxRequests; i++) {
			List<MicrosoftUser> pendingMembers = members.subList(i * MAX_PER_REQUEST, Math.min(MAX_PER_REQUEST * (i +1 ), members.size()));
			List<MicrosoftUser> successMembers = new LinkedList<>();
			generalError = false;

			int retryCount = 0;
			while (!pendingMembers.isEmpty() && retryCount < MAX_RETRY) {
				BatchRequestContent batchRequestContent = new BatchRequestContent();

				pendingMembers.forEach(member -> {
					ConversationMember memberToAdd = new ConversationMember();

					memberToAdd.oDataType = "#microsoft.graph.aadUserConversationMember";
					memberToAdd.roles = roles;
					memberToAdd.additionalDataManager().put("user@odata.bind", new JsonPrimitive("https://graph.microsoft.com/v1.0/users('" + member.getId() + "')"));

					batchRequestContent.addBatchRequestStep(postMembers, HttpMethod.POST, memberToAdd);
				});

				BatchResponseContent responseContent;

				try {
					responseContent = getGraphClient().batch().buildRequest().post(batchRequestContent);
					HashMap<String, ?> membersResponse = parseBatchResponse(responseContent, pendingMembers);

					successMembers.addAll((List<MicrosoftUser>) membersResponse.get("success"));
					pendingMembers = (List<MicrosoftUser>) membersResponse.get("failed");
					List<Map<String, ?>> errors = (List<Map<String, ?>>) membersResponse.get("errors");
					handleMicrosoftExceptions(errors);
				} catch (GraphServiceException e) {
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
				if (!res && status != SynchronizationStatus.ERROR) {
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
			getGraphClient().groups(groupId).members(userId).reference()
				.buildRequest()
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
			getGraphClient().teams(teamId).members(memberId)
				.buildRequest()
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
			ConversationMemberCollectionPage page = getGraphClient()
					.teams(teamId)
					.members()
					.buildRequest()
					.get();
			while (page != null) {
				for(ConversationMember m : page.getCurrentPage()) {
					AadUserConversationMember member = (AadUserConversationMember)m;
	
					//avoid insert admin user
					if(!adminEmail.equalsIgnoreCase(member.email)) {
						log.debug(">>TEAM MEMBER: displayName={}, email={}, roles={}, userId={}, id={}", member.displayName, member.email, member.roles.stream().collect(Collectors.joining(", ")), member.userId, member.id);
	
						teamMemberMap.put(member.userId, member.id);
					}
				}
				ConversationMemberCollectionRequestBuilder builder = page.getNextPage();
				if (builder == null) break;
				page = builder.buildRequest().get();
			}
			
			//get all members in group (sometimes, specially with guest users, there are members in group that have not been moved to the team)
			Set<String> groupMemberIds = new HashSet<>();
			DirectoryObjectCollectionWithReferencesPage page2 = graphClient.groups(teamId)
					.members()
					.buildRequest()
					.select("id,oDataType,mail,displayName")
					.get();
			while (page2 != null) {
				for(DirectoryObject o : page2.getCurrentPage()) {
					if("#microsoft.graph.user".equals(o.oDataType)) {
						User member = (User)o;
						log.debug(">>GROUP MEMBER: displayName={}, mail={}, id={}", member.displayName, member.mail, member.id);
						//avoid insert admin user
						if(!adminEmail.equalsIgnoreCase(member.mail)) {
							groupMemberIds.add(member.id);
						}
					}
				}
				DirectoryObjectCollectionWithReferencesRequestBuilder builder = page2.getNextPage();
				if (builder == null) break;
				page2 = builder.buildRequest().get();
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
						.teams(teamId)
						.channels(channelId)
						.buildRequest()
						.get();
			MicrosoftChannel mc = MicrosoftChannel.builder()
					.id(channel.id)
					.name(channel.displayName)
					.description(channel.description)
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

		ChannelCollectionPage page = getGraphClient().teams(teamId)
				.channels()
				.buildRequest()
				.filter("membershipType eq 'private'")
				.select("id,displayName,description")
				.get();

		if (!force) {
			// get microsoft channels
			List<Channel> microsoftChannelsList = page.getCurrentPage();

			// Create a set of channel IDs obtained from Microsoft
			Set<String> microsoftChannelIds = new HashSet<>();
			for (Channel channel : microsoftChannelsList) {
				microsoftChannelIds.add(channel.id);
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
					microsoftChannelsList.forEach(c -> channelsMap.put(c.id, MicrosoftChannel.builder()
							.id(c.id)
							.name(c.displayName)
							.description(c.description)
							.build()));
					getCache().put(CACHE_CHANNELS + teamId, channelsMap);
					return channelsMap;
				}
			}
		}

		try {
			while (page != null) {
				for (Channel channel : page.getCurrentPage()) {
					channelsMap.put(channel.id, MicrosoftChannel.builder()
							.id(channel.id)
							.name(channel.displayName)
							.description(channel.description)
							.build());
				}
				ChannelCollectionRequestBuilder builder = page.getNextPage();
				if (builder == null) break;
				page = builder.buildRequest().get();
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
			channel.membershipType = ChannelMembershipType.PRIVATE;
			channel.displayName = truncatedName;
			channel.description = truncatedName;

			channel.members = initializeChannelMembers(ownerEmail);

			Channel newChannel = getGraphClient().teams(teamId).channels()
					.buildRequest()
					.post(channel);

			//add new channel to cache
			Cache.ValueWrapper cachedValue = getCache().get(CACHE_CHANNELS + teamId);
			if (cachedValue != null) {
				Map<String, MicrosoftChannel> channelsMap = (Map<String, MicrosoftChannel>) cachedValue.get();
				channelsMap.put(newChannel.id, MicrosoftChannel.builder()
						.id(newChannel.id)
						.name(newChannel.displayName)
						.description(newChannel.description)
						.build());
				getCache().put(CACHE_CHANNELS+teamId, channelsMap);
			}
	
			return newChannel.id;
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

		ChannelCollectionRequest postChannel = graphClient.teams(teamId).channels()
				.buildRequest();

		ConversationMemberCollectionPage members = initializeChannelMembers(ownerEmail);
		List<org.sakaiproject.site.api.Group> pendingChannels = groupsToProcess;

		final int MAX_RETRY = 2;
		int retryCount = 0;

		//sometimes microsoft fails creating -> loop for retry failed ones
		while (!pendingChannels.isEmpty() && retryCount < MAX_RETRY) {
			BatchRequestContent batchRequestContent = new BatchRequestContent();

			groupsToProcess.forEach(group -> {
				Channel channel = new Channel();
				channel.membershipType = ChannelMembershipType.PRIVATE;
				channel.displayName = processMicrosoftChannelName(group.getTitle());
				channel.description = group.getTitle();
				channel.members = members;

				batchRequestContent.addBatchRequestStep(postChannel, HttpMethod.POST, channel);
			});

			BatchResponseContent responseContent = getGraphClient().batch().buildRequest().post(batchRequestContent);

			HashMap<String, ?> channelsResponse = parseBatchResponse(responseContent, groupsToProcess);

			channels.addAll((List<MicrosoftChannel>) channelsResponse.get("success"));
			pendingChannels = (List<org.sakaiproject.site.api.Group>) channelsResponse.get("failed");
			retryCount++;
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

	private HashMap<String, ?> parseBatchResponse(BatchResponseContent responseContent, List<?> listToProcess) {
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

	private HashMap<String,?> parseBatchResponseToMicrosoftUser(BatchResponseContent responseContent, List<MicrosoftUser> listToProcess) {
		HashMap<String, Object> responseMap = new HashMap<>();

		Map<String, MicrosoftUser> successRequests =
				responseContent.responses.stream().filter(r -> r.status <= 299).collect(Collectors.toList())
						.stream().map(r -> {
							Map.Entry<String, MicrosoftUser> entry = new AbstractMap.SimpleEntry<>(
									r.body.getAsJsonObject().get("userId").getAsString(),
									listToProcess.stream().filter(user -> user.getId().equals(r.body.getAsJsonObject().get("userId").getAsString())).findFirst().orElse(null)
							);
							return entry;
						}).collect(Collectors.toMap(
								Map.Entry::getKey,
								Map.Entry::getValue
						));

		List<MicrosoftUser> pendingRequests = listToProcess.stream()
				.filter(user -> !successRequests.containsKey(user.getId()))
				.collect(Collectors.toList());

		List<Map<String, ?>> errors = responseContent.responses.stream()
				.filter(r -> r.status > 299)
				.map(r -> {
					String code, innerError;
					try {
						code = r.body.getAsJsonObject().get("error").getAsJsonObject().get("code").getAsString();
						innerError = r.body.getAsJsonObject().get("error").getAsJsonObject().get("innerError").getAsJsonObject().get("code").getAsString();
					} catch (Exception e) {
						code = "Failure";
						innerError = "Failure";
					}
					return Map.of(
							"status", r.status,
							"retryAfter", r.headers.containsKey("Retry-After") ? r.headers.get("Retry-After") : 5,
							"code", code,
							"innerError", innerError);
				})
				.collect(Collectors.toList());


		responseMap.put("success", new ArrayList<>(successRequests.values()));
		responseMap.put("failed", pendingRequests);
		responseMap.put("errors", errors);

		return responseMap;
	}


	private HashMap<String,?> parseBatchResponseToMicrosoftUserFromStringList(BatchResponseContent responseContent, List<String> listToProcess) {
		HashMap<String, Object> responseMap = new HashMap<>();

		List<BatchResponseStep<JsonElement>> nonEmptyResponses = responseContent.responses.stream()
				.filter(r -> r.status <= 299)
				.collect(Collectors.toList())
				.stream().filter(r -> !r.body.getAsJsonObject().get("value").getAsJsonArray().isEmpty())
				.collect(Collectors.toList());

		Map<String, MicrosoftUser> successRequests =
				nonEmptyResponses
						.stream().map(r -> {
							Map.Entry<String, MicrosoftUser> entry = new AbstractMap.SimpleEntry<>(
									r.body.getAsJsonObject().get("value").getAsJsonArray().get(0).getAsJsonObject().get("id").getAsString(),
									MicrosoftUser.builder()
											.id(r.body.getAsJsonObject().get("value").getAsJsonArray().get(0).getAsJsonObject().get("id").getAsString())
											.email(r.body.getAsJsonObject().get("value").getAsJsonArray().get(0).getAsJsonObject().get("mail").getAsString())
											.build()
							);
							return entry;
						}).collect(Collectors.toMap(
								Map.Entry::getKey,
								Map.Entry::getValue
						));

		List<String> notFoundUsers =
				listToProcess.stream()
						.filter(email -> successRequests.entrySet().stream().noneMatch(r -> r.getValue().getEmail().equalsIgnoreCase(email)))
						.collect(Collectors.toList());

		List<Map<String, ?>> errors = responseContent.responses.stream()
				.filter(r -> r.status > 299)
				.map(r -> {
					String code, innerError;
					try {
						code = r.body.getAsJsonObject().get("error").getAsJsonObject().get("code").getAsString();
						innerError = r.body.getAsJsonObject().get("error").getAsJsonObject().get("innerError").getAsJsonObject().get("code").getAsString();
					} catch (Exception e) {
						code = "Failure";
						innerError = "Failure";
					}
					return Map.of(
							"status", r.status,
							"retryAfter", r.headers.containsKey("Retry-After") ? r.headers.get("Retry-After") : 5,
							"code", code,
							"innerError", innerError);
				})
				.collect(Collectors.toList());


		responseMap.put("success", new ArrayList<>(successRequests.values()));
		responseMap.put("failed", notFoundUsers);
		responseMap.put("errors", errors);

		return responseMap;
	}

	private HashMap<String, ?> parseBatchResponseToMicrosoftTeam(BatchResponseContent responseContent, List<?> listToProcess) {
		HashMap<String, Object> responseMap = new HashMap<>();

		Map<String, MicrosoftTeam> successRequests =
				responseContent.responses.stream().filter(r -> r.status <= 299).collect(Collectors.toList())
						.stream().map(r -> {
							Map.Entry<String, MicrosoftTeam> entry = new AbstractMap.SimpleEntry<>(
									r.body.getAsJsonObject().get("id").getAsString(),
									MicrosoftTeam.builder()
											.id(r.body.getAsJsonObject().get("id").getAsString())
											.name(r.body.getAsJsonObject().get("displayName").getAsString())
											.description(r.body.getAsJsonObject().get("description").getAsString())
											.build()
							);
							return entry;
						}).collect(Collectors.toMap(
								entry -> entry.getKey(),
								entry -> entry.getValue()
						));

		List<String> nonExistingSites = responseContent.responses.stream().filter(r -> r.status == 404).collect(Collectors.toList())
				.stream().map(r -> r.body.getAsJsonObject().get("error").getAsJsonObject().get("message").getAsString()).collect(Collectors.toList());

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

	private HashMap<String, ?> parseBatchResponseToMicrosoftChannel(BatchResponseContent responseContent, List<?> listToProcess) {
		HashMap<String, List<?>> responseMap = new HashMap<>();
		List<MicrosoftChannel> successRequests =
				responseContent.responses.stream().filter(r -> r.status <= 299).collect(Collectors.toList())
						.stream().map(r -> MicrosoftChannel.builder()
								.id(r.body.getAsJsonObject().get("id").getAsString())
								.name(r.body.getAsJsonObject().get("displayName").getAsString())
								.description(r.body.getAsJsonObject().get("description").getAsString())
								.build()).collect(Collectors.toList());

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

	private ConversationMemberCollectionPage initializeChannelMembers(String ownerEmail) throws MicrosoftCredentialsException {
		User userOwner = getGraphClient().users(ownerEmail).buildRequest().get();
		AadUserConversationMember conversationMember = new AadUserConversationMember();
		conversationMember.oDataType = "#microsoft.graph.aadUserConversationMember";
		conversationMember.roles = Arrays.asList(MicrosoftUser.OWNER);
		conversationMember.additionalDataManager().put("user@odata.bind", new JsonPrimitive("https://graph.microsoft.com/v1.0/users('" + userOwner.id + "')"));

		LinkedList<ConversationMember> membersList = new LinkedList<ConversationMember>();
		membersList.add(conversationMember);

		ConversationMemberCollectionResponse conversationMemberCollectionResponse = new ConversationMemberCollectionResponse();
		conversationMemberCollectionResponse.value = membersList;
		return new ConversationMemberCollectionPage(conversationMemberCollectionResponse, null);
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
			getGraphClient().teams(teamId).channels(channelId)
				.buildRequest()
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
			
			ConversationMemberCollectionPage page = getGraphClient()
					.teams(teamId)
					.channels(channelId)
					.members()
					.buildRequest()
					.get();

			while (page != null) {
				for(ConversationMember m : page.getCurrentPage()) {
					AadUserConversationMember member = (AadUserConversationMember)m;
	
					String identifier = getMemberKeyValue(member, key);
					//avoid insert admin user
					if(StringUtils.isNotBlank(identifier) && !credentials.getEmail().equalsIgnoreCase(member.email)) {
						log.debug(">>MEMBER: ({}) --> displayName={}, roles={}, userId={}, id={}", identifier, member.displayName, member.roles.stream().collect(Collectors.joining(", ")), member.userId, member.id);
	
						MicrosoftUser mu = MicrosoftUser.builder()
								.id(member.userId)
								.name(member.displayName)
								.email(member.email)
								.memberId(member.id)
								.owner(member.roles != null && member.roles.contains(MicrosoftUser.OWNER))
								.guest(member.roles != null && member.roles.contains(MicrosoftUser.GUEST))
								.build();

						if(member.roles != null && member.roles.contains(MicrosoftUser.GUEST)) {
							ret.addGuest(identifier, mu);
						} else if(member.roles != null && member.roles.contains(MicrosoftUser.OWNER)) {
							ret.addOwner(identifier, mu);
						} else {
							ret.addMember(identifier, mu);
						}
					}
				}
				ConversationMemberCollectionRequestBuilder builder = page.getNextPage();
				if (builder == null) break;
				page = builder.buildRequest().get();
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
		
		String filter = null;
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
				ConversationMemberCollectionPage page = getGraphClient().teams(teamId).channels(channelId).members()
						.buildRequest()
						.filter(filter)
						.get();
				while (page != null) {
					for(ConversationMember m : page.getCurrentPage()) {
						AadUserConversationMember member = (AadUserConversationMember)m;
					
						if(ret == null) {
							ret = MicrosoftUser.builder()
									.id(member.userId)
									.name(member.displayName)
									.email(member.email)
									.memberId(member.id)
									.owner(member.roles != null && member.roles.contains(MicrosoftUser.OWNER))
									.guest(member.roles != null && member.roles.contains(MicrosoftUser.GUEST))
									.build();
						}
					}
					
					ConversationMemberCollectionRequestBuilder builder = page.getNextPage();
					if (builder == null) break;
					page = builder.buildRequest().get();
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
			conversationMember.oDataType = "#microsoft.graph.aadUserConversationMember";
			conversationMember.roles = new LinkedList<String>();
			
			conversationMember.additionalDataManager().put("user@odata.bind", new JsonPrimitive("https://graph.microsoft.com/v1.0/users('" + userId + "')"));
			
			getGraphClient().teams(teamId).channels(channelId).members().buildRequest().post(conversationMember);
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
			conversationMember.oDataType = "#microsoft.graph.aadUserConversationMember";
			conversationMember.roles = rolesList;
			
			conversationMember.additionalDataManager().put("user@odata.bind", new JsonPrimitive("https://graph.microsoft.com/v1.0/users('" + userId + "')"));
			
			getGraphClient().teams(teamId).channels(channelId).members().buildRequest().post(conversationMember);
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

		ConversationMemberCollectionRequest postMembers = graphClient.teams(teamId).channels(channelId).members()
				.buildRequest();

		final int maxRequests = members.size() / MAX_PER_REQUEST;

		for (int i = 0; i <= maxRequests; i++) {
			List<MicrosoftUser> pendingMembers = members.subList(i * MAX_PER_REQUEST, Math.min(MAX_PER_REQUEST * (i +1 ), members.size()));
			List<MicrosoftUser> successMembers = new LinkedList<>();
			generalError = false;
			int retryCount = 0;
			while (!pendingMembers.isEmpty() && retryCount < MAX_RETRY) {
				BatchRequestContent batchRequestContent = new BatchRequestContent();

				pendingMembers.forEach(member -> {
					ConversationMember memberToAdd = new ConversationMember();

					memberToAdd.oDataType = "#microsoft.graph.aadUserConversationMember";
					memberToAdd.roles = roles;
					memberToAdd.additionalDataManager().put("user@odata.bind", new JsonPrimitive("https://graph.microsoft.com/v1.0/users('" + member.getId() + "')"));

					batchRequestContent.addBatchRequestStep(postMembers, HttpMethod.POST, memberToAdd);
				});
				BatchResponseContent responseContent;

				try {
					responseContent = getGraphClient().batch().buildRequest().post(batchRequestContent);
					HashMap<String, ?> membersResponse = parseBatchResponse(responseContent, pendingMembers);

					successMembers.addAll((List<MicrosoftUser>) membersResponse.get("success"));
					pendingMembers = (List<MicrosoftUser>) membersResponse.get("failed");
					List<Map<String, ?>> errors = (List<Map<String, ?>>) membersResponse.get("errors");
					handleMicrosoftExceptions(errors);
				} catch (GraphServiceException e) {
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
			getGraphClient().teams(teamId).channels(channelId).members(memberId)
				.buildRequest()
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
			iden.id = organizerUser.getId();
			iden.displayName = organizerUser.getName();
			organizerIdentity.user = iden; 
			organizer.identity = organizerIdentity;
			organizer.role = OnlineMeetingRole.PRESENTER;
			
			// Participants
			MeetingParticipants participants = new MeetingParticipants();
			participants.organizer = organizer;

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
							coorganizerIden.id = coorganizerUser.getId();
							coorganizerIden.displayName = coorganizerUser.getName();
							coorganizerIdentity.user = coorganizerIden;
							coorganizer.identity = coorganizerIdentity;
							coorganizer.role = OnlineMeetingRole.COORGANIZER;
							coorganizer.upn = coorganizerUser.getEmail();
							attendees.add(coorganizer);
						}
					}
				}
			}
			participants.attendees = attendees;


			// Lobby Settings
			LobbyBypassSettings lobbySettings = new LobbyBypassSettings();
			lobbySettings.scope = LobbyBypassScope.ORGANIZATION;

			// Online Meeting
			OnlineMeeting onlineMeeting = new OnlineMeeting();
			if (startDate != null) { onlineMeeting.startDateTime = OffsetDateTime.ofInstant(startDate, ZoneId.systemDefault()); }
			if (endDate != null) { onlineMeeting.endDateTime = OffsetDateTime.ofInstant(endDate, ZoneId.systemDefault()); }
			onlineMeeting.participants = participants;
			onlineMeeting.subject = subject;
			onlineMeeting.lobbyBypassSettings = lobbySettings;
			onlineMeeting.allowedPresenters = OnlineMeetingPresenters.ORGANIZER;
			onlineMeeting.allowMeetingChat = MeetingChatMode.ENABLED;

			OnlineMeeting meeting = getGraphClient().users(organizerUser.getId()).onlineMeetings()
				.buildRequest()
				.post(onlineMeeting);
			
			result = new TeamsMeetingData();
			result.setId(meeting.id);
			result.setJoinUrl(meeting.joinWebUrl);
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
			onlineMeeting.startDateTime = OffsetDateTime.ofInstant(startDate, ZoneId.systemDefault());
			onlineMeeting.endDateTime = OffsetDateTime.ofInstant(endDate, ZoneId.systemDefault());
			onlineMeeting.subject = subject;

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
							coorganizerIden.id = coorganizerUser.getId();
							coorganizerIden.displayName = coorganizerUser.getName();
							coorganizerIdentity.user = coorganizerIden;
							coorganizer.identity = coorganizerIdentity;
							coorganizer.role = OnlineMeetingRole.COORGANIZER;
							coorganizer.upn = coorganizerUser.getEmail();
							attendees.add(coorganizer);
						}
					}
				}
				participants.attendees = attendees;
			}
			onlineMeeting.participants = participants;
			getGraphClient().users(organizerUser.getId()).onlineMeetings(meetingId)
					.buildRequest()
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
		ChatMessageCollectionPage page = getGraphClient().chats(onlineMeetingId).messages()
				.buildRequest()
				.get();
		
		while (page != null) {
			//explore chat messages from given meeting
			for(ChatMessage message : page.getCurrentPage()) {
				//we only want "success" recording messages
				if(message.eventDetail != null && message.eventDetail instanceof CallRecordingEventMessageDetail) {
					CallRecordingEventMessageDetail details = (CallRecordingEventMessageDetail)message.eventDetail;
					if(details.callRecordingStatus == CallRecordingStatus.SUCCESS) {
						try {
							MeetingRecordingData.MeetingRecordingDataBuilder builder = MeetingRecordingData.builder()
								.id(details.callId)
								.name(details.callRecordingDisplayName)
								.url(details.callRecordingUrl)
								.organizerId(details.initiator.user.id);
							
							//get driveItem (file in one-drive) from given webURL
							//we will use this call to check if link is still valid
							MicrosoftDriveItem driveItem = getDriveItemFromLink(details.callRecordingUrl);
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
							log.debug("Error getting chat message chatId={}, messageId={}", onlineMeetingId, message.id);
						}
					}
				}
			}
			ChatMessageCollectionRequestBuilder builder = page.getNextPage();
			if (builder == null) break;
			page = builder.buildRequest().get();
		}
		//update cache
		getCache().put(CACHE_RECORDINGS+onlineMeetingId, ret);

		return ret;
	}

	@Override
	public List<AttendanceRecord> getMeetingAttendanceReport(String onlineMeetingId, String userEmail) throws MicrosoftCredentialsException {

		List<AttendanceRecord> attendanceRecordsResponse = new ArrayList<>();

		MicrosoftUser organizerUser = getUserByEmail(userEmail);

		MeetingAttendanceReportCollectionPage attendanceReports = getGraphClient()
				.users(organizerUser.getId())
				.onlineMeetings(onlineMeetingId)
				.attendanceReports()
				.buildRequest()
				.get();

		if (attendanceReports != null && attendanceReports.getCurrentPage() != null) {
			for (com.microsoft.graph.models.MeetingAttendanceReport report : attendanceReports.getCurrentPage()) {
				String reportId = report.id;

				AttendanceRecordCollectionPage attendanceRecords = getGraphClient()
						.users(organizerUser.getId())
						.onlineMeetings()
						.byId(onlineMeetingId)
						.attendanceReports(reportId)
						.attendanceRecords()
						.buildRequest()
						.get();

				if (attendanceRecords != null && attendanceRecords.getCurrentPage() != null) {
					for (com.microsoft.graph.models.AttendanceRecord record : attendanceRecords.getCurrentPage()) {
						AttendanceRecord response = new AttendanceRecord();

						if (record.emailAddress != null) response.setEmail(record.emailAddress);
						if (record.id != null) response.setId(record.id);
						if (record.identity.displayName != null) response.setDisplayName(record.identity.displayName);
						if (record.role != null) response.setRole(record.role);
						if (record.totalAttendanceInSeconds != null) response.setTotalAttendanceInSeconds(record.totalAttendanceInSeconds);

						List<AttendanceInterval> intervals = new ArrayList<>();
						if (record.attendanceIntervals != null) {
							for (com.microsoft.graph.models.AttendanceInterval interval : record.attendanceIntervals) {
								AttendanceInterval intervalResponse = new AttendanceInterval();
								if (interval.joinDateTime != null) intervalResponse.setJoinDateTime(interval.joinDateTime.toString());
								if (interval.leaveDateTime != null) intervalResponse.setLeaveDateTime(interval.leaveDateTime.toString());
								if (interval.durationInSeconds != null) intervalResponse.setDurationInSeconds(interval.durationInSeconds);
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
			DriveItemCollectionPage itemPage = null;
			if(itemId != null) {
				itemPage = getGraphClient().groups(groupId).drive().items(itemId).children().buildRequest().get();
			} else {
				itemPage = getGraphClient().groups(groupId).drive().root().children().buildRequest().get();
			}
			while (itemPage != null) {
				ret.addAll(itemPage.getCurrentPage().stream().map(item -> {
					JsonElement adm = item.additionalDataManager().get("@microsoft.graph.downloadUrl");
					return MicrosoftDriveItem.builder()
						.id(item.id)
						.name(item.name)
						.url(item.webUrl)
						.createdAt((item.createdDateTime != null) ? item.createdDateTime.atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
						.modifiedAt((item.lastModifiedDateTime != null) ? item.lastModifiedDateTime.atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
						.modifiedBy((item.lastModifiedBy != null && item.lastModifiedBy.user != null) ? item.lastModifiedBy.user.displayName : null)
						.driveId((item.parentReference != null) ? item.parentReference.driveId : null)
						.downloadURL((adm!=null) ? adm.getAsString() : null)
						.path((item.parentReference != null) ? item.parentReference.path : null)
						.folder(item.folder != null)
						.childCount((item.folder != null) ? item.folder.childCount : 0)
						.size(item.size)
						.mimeType((item.file != null) ? item.file.mimeType : null)
						.build();
					}).collect(Collectors.toList())
				);
				DriveItemCollectionRequestBuilder itemBuilder = itemPage.getNextPage();
				if (itemBuilder == null) break;
				itemPage = itemBuilder.buildRequest().get();
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
			DriveItem item = getGraphClient().shares(encodeWebURL(link)).driveItem().buildRequest().get();
			ret = MicrosoftDriveItem.builder()
					.id(item.id)
					.name(item.name)
					.url(item.webUrl)
					.driveId((item.parentReference != null) ? item.parentReference.driveId : null)
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
			DriveItem item = getGraphClient()
					.groups(teamId)
					.drive()
					.root()
					.buildRequest()
					.get();
			ret = MicrosoftDriveItem.builder()
					.id(item.id)
					.name(item.name)
					.url(item.webUrl)
					.createdAt((item.createdDateTime != null) ? item.createdDateTime.atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
					.modifiedAt((item.lastModifiedDateTime != null) ? item.lastModifiedDateTime.atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
					.modifiedBy((item.lastModifiedBy != null && item.lastModifiedBy.user != null) ? item.lastModifiedBy.user.displayName : null)
					.driveId((item.parentReference != null) ? item.parentReference.driveId : null)
					.depth(0)
					.folder(item.folder != null)
					.childCount((item.folder != null) ? item.folder.childCount : 0)
					.size(item.size)
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
					.teams(teamId)
					.channels(channelId)
					.filesFolder()
					.buildRequest()
					.get();
			ret = MicrosoftDriveItem.builder()
					.id(item.id)
					.name(item.name)
					.url(item.webUrl)
					.createdAt((item.createdDateTime != null) ? item.createdDateTime.atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
					.modifiedAt((item.lastModifiedDateTime != null) ? item.lastModifiedDateTime.atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
					.modifiedBy((item.lastModifiedBy != null && item.lastModifiedBy.user != null) ? item.lastModifiedBy.user.displayName : null)
					.driveId((item.parentReference != null) ? item.parentReference.driveId : null)
					.depth(0)
					.folder(item.folder != null)
					.childCount((item.folder != null) ? item.folder.childCount : 0)
					.size(item.size)
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
			recipient.objectId = teamId;
			
			LinkedList<DriveRecipient> recipientsList = new LinkedList<DriveRecipient>();
			recipientsList.add(recipient);
			
			LinkedList<String> rolesList = new LinkedList<String>();
			rolesList.add(PERMISSION_READ);
			
			getGraphClient()
					.drives(driveId).items(itemId)
					.invite(DriveItemInviteParameterSet
							.newBuilder()
							.withRequireSignIn(true)
							.withSendInvitation(false)
							.withRoles(rolesList)
							.withRecipients(recipientsList)
							.build())
					.buildRequest()
					.post();
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
		try {
			Permission p = getGraphClient()
					.drives(driveId)
					.items(itemId)
					.createLink(DriveItemCreateLinkParameterSet
							.newBuilder()
							.withType((role == PermissionRoles.WRITE) ? LINK_TYPE_EDIT : LINK_TYPE_VIEW)
							.withScope(LINK_SCOPE_USERS)
							.build())
					.buildRequest()
					.post();
			
			if(p != null) {				
				List<DriveRecipient> recipientsList = teamIds.stream()
					.map(id -> { 
						DriveRecipient r = new DriveRecipient();
						r.objectId = id;
						return r; 
					})
					.collect(Collectors.toList());
				
				LinkedList<String> rolesList = new LinkedList<String>();
				rolesList.add((role == PermissionRoles.WRITE) ? PERMISSION_WRITE : PERMISSION_READ);
				
				getGraphClient()
						.shares(p.shareId)
						.permission()
						.grant(PermissionGrantParameterSet
								.newBuilder()
								.withRoles(rolesList)
								.withRecipients(recipientsList)
								.build())
						.buildRequest()
						.post();
				
				return p.link.webUrl;
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
				.drives(item.getDriveId())
				.items(item.getId())
				.thumbnails("0") //is always zero?
				.buildRequest()
				.get();
			
			if(maxWidth != null && maxHeight != null && maxWidth > 0 && maxHeight > 0) {
				if(thumbnailSet.large.width <= maxWidth && thumbnailSet.large.height <= maxHeight) {
					ret = thumbnailSet.large.url;
				} else if(thumbnailSet.medium.width <= maxWidth && thumbnailSet.medium.height <= maxHeight) {
					ret = thumbnailSet.medium.url;
				} else {
					ret = thumbnailSet.small.url;
				}
			} else {
				ret = thumbnailSet.large.url;
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
			getGraphClient().drives(item.getDriveId()).items(item.getId())
				.buildRequest()
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
			
			DriveItemCollectionPage itemPage = null;
			if(itemId != null) {
				itemPage = client.me().drive().items(itemId).children().buildRequest().get();
			} else {
				itemPage = client.me().drive().root().children().buildRequest().get();
			}
			while (itemPage != null) {
				ret.addAll(itemPage.getCurrentPage().stream().map(item -> {
					JsonElement adm = item.additionalDataManager().get("@microsoft.graph.downloadUrl");
					return MicrosoftDriveItem.builder()
						.id(item.id)
						.name(item.name)
						.url(item.webUrl)
						.createdAt((item.createdDateTime != null) ? item.createdDateTime.atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
						.modifiedAt((item.lastModifiedDateTime != null) ? item.lastModifiedDateTime.atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
						.modifiedBy((item.lastModifiedBy != null && item.lastModifiedBy.user != null) ? item.lastModifiedBy.user.displayName : null)
						.driveId((item.parentReference != null) ? item.parentReference.driveId : null)
						.downloadURL((adm!=null) ? adm.getAsString() : null)
						.path((item.parentReference != null) ? item.parentReference.path : null)
						.folder(item.folder != null)
						.childCount((item.folder != null) ? item.folder.childCount : 0)
						.size(item.size)
						.mimeType((item.file != null) ? item.file.mimeType : null)
						.build();
					}).collect(Collectors.toList())
				);
				DriveItemCollectionRequestBuilder itemBuilder = itemPage.getNextPage();
				if (itemBuilder == null) break;
				itemPage = itemBuilder.buildRequest().get();
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
			DriveSharedWithMeCollectionPage itemPage = client.me().drive().sharedWithMe().buildRequest().get();
			while (itemPage != null) {
				ret.addAll(itemPage.getCurrentPage().stream().map(item -> {
					return MicrosoftDriveItem.builder()
						.id(item.id)
						.name(item.name)
						.url(item.webUrl)
						.createdAt((item.createdDateTime != null) ? item.createdDateTime.atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
						.modifiedAt((item.lastModifiedDateTime != null) ? item.lastModifiedDateTime.atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
						.modifiedBy((item.lastModifiedBy != null && item.lastModifiedBy.user != null) ? item.lastModifiedBy.user.displayName : null)
						.driveId((item.remoteItem != null && item.remoteItem.parentReference != null) ? item.remoteItem.parentReference.driveId : null)
						.shared(true) //IMPORTANT: identify these items as shared (they have uncompleted data)
						.downloadURL(null)
						.depth(0)
						.folder(item.folder != null)
						.childCount(1) //we don't trust this value from Microsoft. Set to "1", so it will be expandable (in case of a folder)
						.size(item.size)
						.mimeType((item.file != null) ? item.file.mimeType : null)
						.build();
					}).collect(Collectors.toList())
				);
				DriveSharedWithMeCollectionRequestBuilder itemBuilder = itemPage.getNextPage();
				if (itemBuilder == null) break;
				itemPage = itemBuilder.buildRequest().get();
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
				item = client.drives(driveId).items(itemId).buildRequest().get();
			} else {
				item = getGraphClient().drives(driveId).items(itemId).buildRequest().get();
			}
			
			if (item != null) {
				JsonElement adm = item.additionalDataManager().get("@microsoft.graph.downloadUrl");
				ret = MicrosoftDriveItem.builder()
						.id(item.id)
						.name(item.name)
						.url(item.webUrl)
						.createdAt((item.createdDateTime != null) ? item.createdDateTime.atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
						.modifiedAt((item.lastModifiedDateTime != null) ? item.lastModifiedDateTime.atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
						.modifiedBy((item.lastModifiedBy != null && item.lastModifiedBy.user != null) ? item.lastModifiedBy.user.displayName : null)
						.driveId((item.parentReference != null) ? item.parentReference.driveId : null)
						.downloadURL((adm!=null) ? adm.getAsString() : null)
						.path((item.parentReference != null) ? item.parentReference.path : null)
						.folder(item.folder != null)
						.childCount((item.folder != null) ? item.folder.childCount : 0)
						.size(item.size)
						.mimeType((item.file != null) ? item.file.mimeType : null)
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
			DriveItemCollectionPage itemPage = null;
			if(StringUtils.isNotBlank(delegatedUserId)) {
				GraphServiceClient client = (GraphServiceClient)microsoftAuthorizationService.getDelegatedGraphClient(delegatedUserId);
				if(itemId != null) {
					itemPage = client.drives(driveId).items(itemId).children().buildRequest().get();
				} else {
					itemPage = client.drives(driveId).root().children().buildRequest().get();
				}
			} else {
				if(itemId != null) {
					itemPage = getGraphClient().drives(driveId).items(itemId).children().buildRequest().get();
				} else {
					itemPage = getGraphClient().drives(driveId).root().children().buildRequest().get();
				}
			}
			
			while (itemPage != null) {
				ret.addAll(itemPage.getCurrentPage().stream().map(item -> {
					JsonElement adm = item.additionalDataManager().get("@microsoft.graph.downloadUrl");
					return MicrosoftDriveItem.builder()
						.id(item.id)
						.name(item.name)
						.url(item.webUrl)
						.createdAt((item.createdDateTime != null) ? item.createdDateTime.atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
						.modifiedAt((item.lastModifiedDateTime != null) ? item.lastModifiedDateTime.atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
						.modifiedBy((item.lastModifiedBy != null && item.lastModifiedBy.user != null) ? item.lastModifiedBy.user.displayName : null)
						.driveId((item.parentReference != null) ? item.parentReference.driveId : null)
						.downloadURL((adm!=null) ? adm.getAsString() : null)
						.path((item.parentReference != null) ? item.parentReference.path : null)
						.folder(item.folder != null)
						.childCount((item.folder != null) ? item.folder.childCount : 0)
						.size(item.size)
						.mimeType((item.file != null) ? item.file.mimeType : null)
						.build();
					}).collect(Collectors.toList())
				);
				DriveItemCollectionRequestBuilder itemBuilder = itemPage.getNextPage();
				if (itemBuilder == null) break;
				itemPage = itemBuilder.buildRequest().get();
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
			newItem.name = name;
			if(type == MicrosoftDriveItem.TYPE.FOLDER) {
				newItem.folder = new Folder();
			} else {
				if(!name.toLowerCase().endsWith(type.getExt())) {
					newItem.name = name + type.getExt();
				}
				newItem.file = new com.microsoft.graph.models.File();
			}
			newItem.additionalDataManager().put("@microsoft.graph.conflictBehavior", new JsonPrimitive("rename"));
			
			DriveItem item = client.drives(parent.getDriveId()).items(parent.getId()).children().buildRequest().post(newItem);
			ret = MicrosoftDriveItem.builder()
					.id(item.id)
					.name(item.name)
					.url(item.webUrl)
					.createdAt((item.createdDateTime != null) ? item.createdDateTime.atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
					.modifiedAt((item.lastModifiedDateTime != null) ? item.lastModifiedDateTime.atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
					.modifiedBy((item.lastModifiedBy != null && item.lastModifiedBy.user != null) ? item.lastModifiedBy.user.displayName : null)
					.driveId(parent.getDriveId())
					.path((item.parentReference != null) ? item.parentReference.path : null)
					.folder(item.folder != null)
					.childCount(0)
					.size(item.size)
					.mimeType((item.file != null) ? item.file.mimeType : null)
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
			upProps.additionalDataManager().put("@microsoft.graph.conflictBehavior", new JsonPrimitive("rename"));
			
			final DriveItemCreateUploadSessionParameterSet uploadParams = DriveItemCreateUploadSessionParameterSet.newBuilder().withItem(upProps).build();
			
			// Create an upload session
			final UploadSession uploadSession = client
					.drives(parent.getDriveId())
					.items(parent.getId())
					.itemWithPath(name)
					.createUploadSession(uploadParams)
					.buildRequest()
					.post();
	
			if (null == uploadSession) {
				fileStream.close();
				log.warn("Error creating upload session in drive={} and parent={}", parent.getDriveId(), parent.getId());
				return null;
			}
	
			LargeFileUploadTask<DriveItem> largeFileUploadTask = new LargeFileUploadTask<DriveItem>(uploadSession, client, fileStream, streamSize, DriveItem.class);
	
			// Do the upload
			LargeFileUploadResult<DriveItem> result = largeFileUploadTask.upload();
			
			DriveItem item = result.responseBody;
			ret = MicrosoftDriveItem.builder()
				.id(item.id)
				.name(item.name)
				.url(item.webUrl)
				.createdAt((item.createdDateTime != null) ? item.createdDateTime.atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
				.modifiedAt((item.lastModifiedDateTime != null) ? item.lastModifiedDateTime.atZoneSameInstant(sakaiProxy.getUserTimeZoneId()) : null)
				.modifiedBy((item.lastModifiedBy != null && item.lastModifiedBy.user != null) ? item.lastModifiedBy.user.displayName : null)
				.driveId(parent.getDriveId())
				.path((item.parentReference != null) ? item.parentReference.path : null)
				.folder(item.folder != null)
				.childCount(0)
				.size(item.size)
				.mimeType((item.file != null) ? item.file.mimeType : null)
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
				ret = member.userId;
				break;
	
			case EMAIL:
				ret = (member.email != null) ? member.email.toLowerCase() : null;
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
