// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package org.sakaiproject.meetings.teams;

import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.ConversationMemberCollectionPage;
import com.microsoft.graph.requests.ConversationMemberCollectionResponse;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.GroupCollectionPage;
import com.microsoft.graph.requests.GroupCollectionRequestBuilder;
import com.microsoft.graph.requests.UserCollectionPage;
import com.microsoft.graph.requests.UserCollectionRequestBuilder;
import com.nimbusds.oauth2.sdk.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.meetings.teams.data.TeamsMeetingData;

import com.google.gson.JsonPrimitive;
import com.microsoft.graph.models.AadUserConversationMember;
import com.microsoft.graph.models.ConversationMember;
import com.microsoft.graph.models.Group;
import com.microsoft.graph.models.Identity;
import com.microsoft.graph.models.IdentitySet;
import com.microsoft.graph.models.LobbyBypassScope;
import com.microsoft.graph.models.LobbyBypassSettings;
import com.microsoft.graph.models.MeetingParticipantInfo;
import com.microsoft.graph.models.MeetingParticipants;
import com.microsoft.graph.models.OnlineMeeting;
import com.microsoft.graph.models.OnlineMeetingPresenters;
import com.microsoft.graph.models.OnlineMeetingRole;
import com.microsoft.graph.models.Team;

@Slf4j
public class MicrosoftTeamsService  {

    private static final ServerConfigurationService serverConfigurationService = (ServerConfigurationService) ComponentManager.get(ServerConfigurationService.class);
    
    private GraphServiceClient graphClient;
    
    private static final String MSTEAMS_PREFIX = "meetings.msteams.";
    private static final String AUTHORITY = "authority";
    private static final String CLIENT_ID = "clientId";
    private static final String SECRET = "secret";
    private static final String SCOPE = "scope";
    
    public MicrosoftTeamsService() {
        log.info("Initializing Microsoft Teams Service");
        String authority = serverConfigurationService.getString(MSTEAMS_PREFIX + AUTHORITY, null);
        String clientId = serverConfigurationService.getString(MSTEAMS_PREFIX + CLIENT_ID, null);
        String secret = serverConfigurationService.getString(MSTEAMS_PREFIX + SECRET, null);
        String scope = serverConfigurationService.getString(MSTEAMS_PREFIX + SCOPE, null);
        AdminAuthProvider authProvider = new AdminAuthProvider(authority, clientId, secret, scope);
        graphClient = GraphServiceClient
                .builder()
                .authenticationProvider(authProvider)
                .buildClient();
    }
    
    /**
     * Checking that MSTeams has been set up
     * @return
     */
    public boolean isMicrosofTeamsConfigured() {
        boolean result = false;
        String authority = serverConfigurationService.getString(MSTEAMS_PREFIX + AUTHORITY, null);
        String clientId = serverConfigurationService.getString(MSTEAMS_PREFIX + CLIENT_ID, null);
        String secret = serverConfigurationService.getString(MSTEAMS_PREFIX + SECRET, null);
        String scope = serverConfigurationService.getString(MSTEAMS_PREFIX + SCOPE, null);
        if (StringUtils.isNotBlank(authority) && StringUtils.isNotBlank(clientId) && StringUtils.isNotBlank(secret) && StringUtils.isNotBlank(scope)) {
            result = true;
        }
        return result;
    }
    
    /**
     * Get Azure user list
     * @return
     */
    public List<User> getAzureUserList() {
        List<User> userList = new ArrayList<>();
        UserCollectionPage page = graphClient.users().buildRequest().get();
        while (page != null) {
            userList.addAll(page.getCurrentPage());
            UserCollectionRequestBuilder builder = page.getNextPage();
            if(builder == null)break;
            page = builder.buildRequest().get();
        }
        return userList;
    }

    /**
     * Get Azure groups
     * @return
     */
    public List<Group> getGroups() {
        List<Group> groupList = new ArrayList<>();
        GroupCollectionPage page = graphClient.groups().buildRequest().get();
        while (page != null) {
            groupList.addAll(page.getCurrentPage());
            GroupCollectionRequestBuilder builder = page.getNextPage();
            if (builder == null) break;
            page = builder.buildRequest().get();
        }
        return groupList;
    }

    /**
     * Create Team
     * @param name
     * @param description
     * @param microsoftLogin
     * @throws Exception
     */
    public void createTeam(String name, String description, String microsoftLogin) throws Exception {
        try {
            User userOwner = graphClient.users(microsoftLogin).buildRequest().get();
            Team team = new Team();
            team.additionalDataManager().put("template@odata.bind", new JsonPrimitive("https://graph.microsoft.com/v1.0/teamsTemplates('standard')"));
            team.displayName = name;
            team.description = description;
            LinkedList<ConversationMember> membersList = new LinkedList<ConversationMember>();
            AadUserConversationMember members = new AadUserConversationMember();
            LinkedList<String> rolesList = new LinkedList<String>();
            rolesList.add("owner");
            members.roles = rolesList;
            members.oDataType = "#microsoft.graph.aadUserConversationMember";
            members.additionalDataManager().put("user@odata.bind", new JsonPrimitive("https://graph.microsoft.com/v1.0/users('" + userOwner.id + "')"));
            membersList.add(members);
            ConversationMemberCollectionResponse conversationMemberCollectionResponse = new ConversationMemberCollectionResponse();
            conversationMemberCollectionResponse.value = membersList;
            ConversationMemberCollectionPage conversationMemberCollectionPage = new ConversationMemberCollectionPage(conversationMemberCollectionResponse, null);
            team.members = conversationMemberCollectionPage;
            graphClient.teams()
                .buildRequest()
                .post(team);
        } catch(Exception ex){
            log.error("Oops! We have an exception of type - {}", ex.getClass());
            log.error("Exception message - {}", ex.getMessage());
            throw ex;
        }
    }

    /**
     * Get Microsoft user 
     * @param microsoftLogin
     * @return
     */
    public User getUserByMicrosoftLogin(String microsoftLogin) throws Exception {
        User result = graphClient.users(microsoftLogin).buildRequest().get();
        return result;
    }

    /**
     * Create online meeting
     * @param presenter
     * @param subject
     * @param startDate
     * @param endDate
     * @return
     * @throws ParseException
     */
    public TeamsMeetingData onlineMeeting(String presenter, String subject, Instant startDate, Instant endDate) throws Exception {
        // Get presenter user 
        User organizerUser = getUserByMicrosoftLogin(presenter);
        // Organizer
        MeetingParticipantInfo organizer = new MeetingParticipantInfo();
        IdentitySet organizerIdentity = new IdentitySet();
        Identity iden = new Identity();
        iden.id = organizerUser.id;
        iden.displayName = organizerUser.displayName;
        organizerIdentity.application = iden; 
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
        if (startDate != null) onlineMeeting.startDateTime = OffsetDateTime.ofInstant(startDate, ZoneId.systemDefault());
        if (endDate != null) onlineMeeting.endDateTime = OffsetDateTime.ofInstant(endDate, ZoneId.systemDefault());
        onlineMeeting.participants = participants;
        onlineMeeting.subject = subject;
        onlineMeeting.lobbyBypassSettings = lobbySettings;
        onlineMeeting.allowedPresenters = OnlineMeetingPresenters.ROLE_IS_PRESENTER;
        OnlineMeeting meeting = graphClient.users(organizerUser.id).onlineMeetings()
            .buildRequest()
            .post(onlineMeeting);
        TeamsMeetingData result = new TeamsMeetingData();
        result.setId(meeting.id);
        result.setJoinUrl(meeting.joinWebUrl);
        return result;
    }

    /**
     * Delete meeting
     * @param organizerUser
     * @param meetingId
     * @throws Exception 
     */
    public void deleteMeeting(String organizerUser, String meetingId) throws Exception {
        User user = getUserByMicrosoftLogin(organizerUser);
        graphClient.users(user.id).onlineMeetings(meetingId).buildRequest().delete();
    }

}
