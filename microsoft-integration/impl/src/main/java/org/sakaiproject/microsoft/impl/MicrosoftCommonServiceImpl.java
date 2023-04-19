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
package org.sakaiproject.microsoft.impl;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
import org.sakaiproject.messaging.api.MicrosoftMessage;
import org.sakaiproject.messaging.api.MicrosoftMessage.MicrosoftMessageBuilder;
import org.sakaiproject.messaging.api.MicrosoftMessagingService;
import org.sakaiproject.microsoft.api.MicrosoftCommonService;
import org.sakaiproject.microsoft.api.data.MeetingRecordingData;
import org.sakaiproject.microsoft.api.data.MicrosoftChannel;
import org.sakaiproject.microsoft.api.data.MicrosoftCredentials;
import org.sakaiproject.microsoft.api.data.MicrosoftMembersCollection;
import org.sakaiproject.microsoft.api.data.MicrosoftTeam;
import org.sakaiproject.microsoft.api.data.MicrosoftUser;
import org.sakaiproject.microsoft.api.data.MicrosoftUserIdentifier;
import org.sakaiproject.microsoft.api.data.TeamsMeetingData;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftCredentialsException;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftGenericException;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftInvalidCredentialsException;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftInvalidInvitationException;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftNoCredentialsException;
import org.sakaiproject.microsoft.api.persistence.MicrosoftConfigRepository;
import org.sakaiproject.microsoft.api.persistence.MicrosoftLoggingRepository;
import org.sakaiproject.microsoft.provider.AdminAuthProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
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
import com.microsoft.graph.models.Group;
import com.microsoft.graph.models.Invitation;
import com.microsoft.graph.models.Team;
import com.microsoft.graph.models.User;
import com.microsoft.graph.options.HeaderOption;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.requests.ChannelCollectionPage;
import com.microsoft.graph.requests.ChannelCollectionRequestBuilder;
import com.microsoft.graph.requests.ChatMessageCollectionPage;
import com.microsoft.graph.requests.ChatMessageCollectionRequestBuilder;
import com.microsoft.graph.requests.ConversationMemberCollectionPage;
import com.microsoft.graph.requests.ConversationMemberCollectionRequestBuilder;
import com.microsoft.graph.requests.ConversationMemberCollectionResponse;
import com.microsoft.graph.requests.DirectoryObjectCollectionWithReferencesPage;
import com.microsoft.graph.requests.DirectoryObjectCollectionWithReferencesRequestBuilder;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.GroupCollectionPage;
import com.microsoft.graph.requests.GroupCollectionRequestBuilder;
import com.microsoft.graph.requests.UserCollectionPage;
import com.microsoft.graph.requests.UserCollectionRequestBuilder;
import com.nimbusds.oauth2.sdk.util.StringUtils;

import com.microsoft.graph.models.Identity;
import com.microsoft.graph.models.IdentitySet;
import com.microsoft.graph.models.LobbyBypassScope;
import com.microsoft.graph.models.LobbyBypassSettings;
import com.microsoft.graph.models.MeetingParticipantInfo;
import com.microsoft.graph.models.MeetingParticipants;
import com.microsoft.graph.models.OnlineMeeting;
import com.microsoft.graph.models.OnlineMeetingPresenters;
import com.microsoft.graph.models.OnlineMeetingRole;
import com.microsoft.graph.models.Permission;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
public class MicrosoftCommonServiceImpl implements MicrosoftCommonService {

	private GraphServiceClient graphClient = null;

	@Setter
	MicrosoftConfigRepository microsoftConfigRepository;
	
	@Setter
	MicrosoftLoggingRepository microsoftLoggingRepository;
	
	@Autowired
	MicrosoftMessagingService microsoftMessagingService;
	
	@Setter
	private CacheManager cacheManager;
	private Cache cache = null;
	
	private static final String CACHE_NAME = MicrosoftCommonServiceImpl.class.getName() + "_cache";
	private static final String CACHE_TEAMS = "key::teams";
	private static final String CACHE_CHANNELS = "key::channels::";
	private static final String CACHE_RECORDINGS = "key::recordings::";
	
	private String lock = "LOCK";

	public void init() {
		log.info("Initializing MicrosoftCommonService Service");
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

					AdminAuthProvider authProvider = new AdminAuthProvider(microsoftCredentials.getAuthority(), microsoftCredentials.getClientId(), microsoftCredentials.getSecret(), microsoftCredentials.getScope());
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
			UserCollectionRequestBuilder builder = page.getNextPage();
			if(builder == null)break;
			page = builder.buildRequest().get();
		}

		return userList;
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
			UserCollectionPage pageCollection = getGraphClient().users()
					.buildRequest()
					.filter("mail eq '" + email + "'")
					.select("id,displayName,mail,userType")
					.get();
			if (pageCollection != null && pageCollection.getCurrentPage().size() > 0) {
				User u = pageCollection.getCurrentPage().get(0);
				return MicrosoftUser.builder()
						.id(u.id)
						.name(u.displayName)
						.email(u.mail)
						.guest(MicrosoftUser.GUEST.equalsIgnoreCase(u.userType))
						.build();
			}
		}catch(MicrosoftCredentialsException e) {
			throw e;
		}catch(Exception e) {
			log.debug("Microsoft User not found with email={}", email);
		}
		return null;
	}
	
	@Override
	public MicrosoftUser getUserById(String id) throws MicrosoftCredentialsException {
		try {
			User u = getGraphClient().users(id)
				.buildRequest()
				.select("id,displayName,mail,userType")
				.get();
			return MicrosoftUser.builder()
					.id(u.id)
					.name(u.displayName)
					.email(u.mail)
					.guest(MicrosoftUser.GUEST.equalsIgnoreCase(u.userType))
					.build();
		}catch(MicrosoftCredentialsException e) {
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
			log.debug("CREATE INVITATION: email={}, redirectURL={}", email, redirectURL);
			if("true".equals(microsoftConfigRepository.getConfigItemValueByKey("DEBUG"))) { return MicrosoftUser.builder()
					.id("NEW-ID")
					.email(email)
					.guest(true)
					.build(); }
			
			Invitation invitation = new Invitation();
			invitation.invitedUserEmailAddress = email;
			invitation.inviteRedirectUrl = redirectURL;

			Invitation response = getGraphClient().invitations()
					.buildRequest()
					.post(invitation);

			log.debug("INVITATION RESPONSE: id={}, userId={}, email={}, status={}", response.id, response.invitedUser.id, response.invitedUserEmailAddress, response.status);
			
			return MicrosoftUser.builder()
					.id(response.invitedUser.id)
					.email(response.invitedUserEmailAddress)
					.guest(true)
					.build();
		}catch(MicrosoftCredentialsException e) {
			throw e;
		}catch(Exception e) {
			log.error("Microsoft guest invitation failed for email={}", email);
			throw new MicrosoftInvalidInvitationException();
		}
	}

	
	// ---------------------------------------- TEAMS / GROUPS ------------------------------------------------
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
		try {
			//get from cache (if not force)
			if(!force) {
				Cache.ValueWrapper cachedValue = getCache().get(CACHE_TEAMS);
				if(cachedValue != null) {
					Map<String, MicrosoftTeam> map = (Map<String, MicrosoftTeam>)cachedValue.get();
					if(map.containsKey(id)) {
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
			Cache.ValueWrapper cachedValue = getCache().get(CACHE_TEAMS);
			if(cachedValue != null) {
				Map<String, MicrosoftTeam> teamsMap = (Map<String, MicrosoftTeam>)cachedValue.get();
				teamsMap.put(id, mt);
				
				getCache().put(CACHE_TEAMS, teamsMap);
			}
			return mt;
		}catch(MicrosoftCredentialsException e) {
			throw e;
		}catch(Exception e) {
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
			team.displayName = name;
			team.description = name;
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
		}catch(MicrosoftCredentialsException e) {
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
		}catch (Exception e) {
			log.debug("Error adding owner userId={} to teamId={}", userId, teamId);
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
		}catch(MicrosoftCredentialsException e) {
			throw e;
		}catch (Exception e) {
			log.debug("Error removing all members from teamId={}", teamId);
			return false;
		}
		return true;
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
		//get from cache
		Cache.ValueWrapper cachedValue = getCache().get(CACHE_CHANNELS+teamId);
		if(cachedValue != null) {
			return (Map<String, MicrosoftChannel>)cachedValue.get();
		}
		
		Map<String, MicrosoftChannel> channelsMap = new HashMap<>();
		
		try {
			ChannelCollectionPage page = getGraphClient().teams(teamId)
					.channels()
					.buildRequest()
					.filter("membershipType eq 'private'")
					.select("id,displayName,description")
					.get();
			while (page != null) {
				for(Channel channel : page.getCurrentPage()) {
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
			getCache().put(CACHE_CHANNELS+teamId, channelsMap);
		}catch(MicrosoftCredentialsException e) {
			throw e;
		}catch(Exception e) {
			log.debug("Error getting private channels for teamId={}", teamId);
		}
		return channelsMap;
	}
	
	@Override
	public String createChannel(String teamId, String name, String ownerEmail) throws MicrosoftCredentialsException {
		try {
			Channel channel = new Channel();
			channel.membershipType = ChannelMembershipType.PRIVATE;
			channel.displayName = formatMicrosoftString(name);
			channel.description = name;
			
			User userOwner = getGraphClient().users(ownerEmail).buildRequest().get();
			AadUserConversationMember conversationMember = new AadUserConversationMember();
			conversationMember.oDataType = "#microsoft.graph.aadUserConversationMember";
			conversationMember.roles = Arrays.asList(MicrosoftUser.OWNER);
			conversationMember.additionalDataManager().put("user@odata.bind", new JsonPrimitive("https://graph.microsoft.com/v1.0/users('" + userOwner.id + "')"));
			
			LinkedList<ConversationMember> membersList = new LinkedList<ConversationMember>();
			membersList.add(conversationMember);
			
			ConversationMemberCollectionResponse conversationMemberCollectionResponse = new ConversationMemberCollectionResponse();
			conversationMemberCollectionResponse.value = membersList;
			ConversationMemberCollectionPage conversationMemberCollectionPage = new ConversationMemberCollectionPage(conversationMemberCollectionResponse, null);
			channel.members = conversationMemberCollectionPage;
	
			Channel newChannel = getGraphClient().teams(teamId).channels()
				.buildRequest()
				.post(channel);
			
			//add new channel to cache
			Cache.ValueWrapper cachedValue = getCache().get(CACHE_CHANNELS+teamId);
			if(cachedValue != null) {
				Map<String, MicrosoftChannel> channelsMap = (Map<String, MicrosoftChannel>)cachedValue.get();
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
	public TeamsMeetingData createOnlineMeeting(String userEmail, String subject, Instant startDate, Instant endDate) throws MicrosoftCredentialsException {
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
			onlineMeeting.allowedPresenters = OnlineMeetingPresenters.ROLE_IS_PRESENTER;
			
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
	public void updateOnlineMeeting(String userEmail, String meetingId, String subject, Instant startDate, Instant endDate) throws MicrosoftCredentialsException {
		// Get organizer user
		MicrosoftUser organizerUser = getUserByEmail(userEmail);
		
		if(organizerUser != null) {
			// Online Meeting
			OnlineMeeting onlineMeeting = new OnlineMeeting();
			onlineMeeting.startDateTime = OffsetDateTime.ofInstant(startDate, ZoneId.systemDefault());
			onlineMeeting.endDateTime = OffsetDateTime.ofInstant(endDate, ZoneId.systemDefault());
			onlineMeeting.subject = subject;
			
			getGraphClient().users(organizerUser.getId()).onlineMeetings(meetingId)
					.buildRequest()
					.patch(onlineMeeting);
		}
	}
	
	@Override
	public List<MeetingRecordingData> getOnlineMeetingRecordings(String onlineMeetingId, boolean force) throws MicrosoftCredentialsException{
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
							String driveItemId = getDriveItemIdFromLink(details.callRecordingUrl);
							if(StringUtils.isNotBlank(driveItemId)) {
								//create a new link for sharing (we want all users to access the recording, not only the assistant ones)
								String newURL = getNewLink(details.initiator.user.id, driveItemId);
								if(newURL != null) {
									builder.url(newURL);
								}
							}
							
							ret.add(builder.build());
							
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
	
	// ---------------------------------------- ONE-DRIVE --------------------------------------------------------
	@Override
	public String getDriveItemIdFromLink(String link) throws MicrosoftCredentialsException {
		String ret = null;
		try {
			String base64Link = Base64.getUrlEncoder().encodeToString(link.getBytes());
			String finalLink = "u!"+base64Link.replaceAll("=+$", "").replace('/', '_').replace('+','-');
			DriveItem item = getGraphClient().shares(finalLink).driveItem().buildRequest().get();
			ret = item.id;
		}catch(MicrosoftCredentialsException e) {
			throw e;
		}catch (Exception e) {
			log.debug("Error getting driveItem from link={}", link);
		}
		return ret;
	}
	
	@Override
	public String getNewLink(String userId, String itemId) throws MicrosoftCredentialsException {
		String ret = null;
		try {
			Permission p = getGraphClient().users(userId).drive().items(itemId)
				.createLink(DriveItemCreateLinkParameterSet
					.newBuilder()
					.withType("view")
					.withScope("organization")
					.build())
				.buildRequest()
				.post();
			ret = p.link.webUrl;
		}catch(MicrosoftCredentialsException e) {
			throw e;
		}catch (Exception e) {
			log.debug("Error creating link for item={} from user={}", itemId, userId);
		}
		return ret;
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
	private String formatMicrosoftString(String str) {
		String[] charsToReplace = {"\\~", "#", "%", "&", "\\*", "\\{", "\\}", "\\+", "/", "\\\\", ":", "<", ">", "\\?", "\\|", "‘", "`", "´", "”", "\""};
		for (String c : charsToReplace) {
		    str = str.replaceAll(c, "");
		}
		str = str.replaceAll("^_+", "");
		str = str.replaceAll("^\\.\\.\\.", "");
		str = str.replaceAll("\\.\\.\\.$", "");
		return str;
	}

}
