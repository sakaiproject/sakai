/**
* Copyright (c) 2022 Apereo Foundation
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

package org.sakaiproject.meetings.controller;

import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.text.ParseException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEdit;
import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.meetings.api.MeetingService;
import org.sakaiproject.meetings.api.model.AttendeeType;
import org.sakaiproject.meetings.api.model.Meeting;
import org.sakaiproject.meetings.api.model.MeetingAttendee;
import org.sakaiproject.meetings.controller.data.GroupData;
import org.sakaiproject.meetings.controller.data.MeetingData;
import org.sakaiproject.meetings.controller.data.NotificationType;
import org.sakaiproject.meetings.controller.data.ParticipantData;
import org.sakaiproject.meetings.exceptions.MeetingsException;
import org.sakaiproject.meetings.teams.MicrosoftTeamsService;
import org.sakaiproject.meetings.teams.data.TeamsMeetingData;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


/**
 * MainController
 * 
 * This is the controller used by Spring MVC to handle requests
 * 
 */
@SuppressWarnings("deprecation")
@Slf4j
@RestController
public class MeetingsController {

    /** Resource bundle using current language locale */
    private static ResourceLoader rb = new ResourceLoader("Messages");
    
    @Autowired
    private UserDirectoryService userDirectoryService;
    
    @Autowired
    private SessionManager sessionManager;
    
    @Autowired
    private SiteService siteService;
    
    @Autowired
    private SecurityService securityService;

    @Autowired
    private MeetingService meetingService;
    
    @Autowired
    private CalendarService calendarService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private TimeService timeService;
    
    @Autowired
    private ServerConfigurationService serverConfigurationService;
    
    private MicrosoftTeamsService teamsService = new MicrosoftTeamsService();
    
    private static final String MS_TEAMS = "microsoft_teams";
    private static final String ONLINE_MEETING_ID = "onlineMeetingId";
    private static final String ORGANIZER_USER = "organizerUser";
    private static final String CALENDAR_EVENT_ID = "calendarEventId";
    private static final String MEETING_EVENT_TYPE = "Meeting";
    private static final String NOTIF_SUBJECT = "notification.subject";
    private static final String NOTIF_CONTENT = "notification.content";
    private static final String SMTP_FROM = "smtpFrom@org.sakaiproject.email.api.EmailService";
    private static final String NO_REPLY = "no-reply@";
    
    /**
     * Method to obtain the current user
     * @return
     * @throws MeetingsException 
     */
    private User getCurrentUser() throws MeetingsException {
        String userId = sessionManager.getCurrentSessionUserId();
        try {
            return userDirectoryService.getUser(userId);
        } catch (UserNotDefinedException e) {
            log.error("User {} not found.", userId);
            throw new MeetingsException("Unable to get current user");
        }
    }
    
    /**
     * Check if there's an user logged
     * @return
     * @throws MeetingsException
     */
    private void checkSakaiSession() throws MeetingsException {
        User user = getCurrentUser();
        if (StringUtils.isBlank(user.getId())) {
            throw new MeetingsException("Unable to get current user");
        }
    }
    
    /**
     * Retrieves current user permission to edit meetings
     * @return
     * @throws MeetingsException 
     */
    @GetMapping(value = "/meetings/user/editperms/site/{siteId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean canUpdateSite(@PathVariable String siteId) throws MeetingsException {
        User user = getCurrentUser();
        boolean result = false;
        String userId = user.getId();
        try {
            Site site = siteService.getSite(siteId);
            result = (securityService.unlock(userId, SiteService.SECURE_UPDATE_SITE, site.getReference()) || securityService.isSuperUser(userId));
        } catch (IdUnusedException e) {
            log.error("Error retrieving user permissions", e);
            result = false;
        }
        return result;
    }
    
    /**
     * 'Update site' permissions check
     * @param siteId
     * @return
     * @throws MeetingsException 
     */
    private void checkUpdatePermissions(String siteId) throws MeetingsException {
        boolean result = canUpdateSite(siteId);
        if (!result) {
            throw new MeetingsException("User doesn't have permissions to update this site.");
        }
    }
    
    /**
     * 
     * @param siteId
     * @throws MeetingsException
     */
    private void checkCurrentUserInSite(String siteId) throws MeetingsException {
        User user = getCurrentUser();
        if (!securityService.isSuperUser(user.getId()) && !siteService.isCurrentUserMemberOfSite(siteId)) {
            throw new MeetingsException("Current user has not permissions to see information from site " + siteId);
        }
    }
    
    /**
     * Check current user permission to see a meeting
     * @param meetingId
     * @throws MeetingsException
     */
    private void checkCurrentUserInMeeting(String meetingId) throws MeetingsException {
        try {
            User user = getCurrentUser();
            String userId = user.getId();
            Meeting meeting = meetingService.getMeeting(meetingId);
            Site site = siteService.getSite(meeting.getSiteId());
            String siteId = site.getId();
            List<Group> groups = new ArrayList<>();
            groups.addAll(site.getGroupsWithMember(userId));
            site.getGroupsWithMember(userId);
            List<String> groupIds = groups.stream().map(e->e.getId()).collect(Collectors.toList());
            List<Meeting> meetingList = meetingService.getUserMeetings(userId, siteId, groupIds);
            List<Meeting> result = meetingList.stream().filter(item -> meetingId.equals(item.getId())).collect(Collectors.toList());
            if (result.size() == 0) {
                throw new MeetingsException("Current user does not have permission to see this meeting.");
            }
        } catch (IdUnusedException | MeetingsException e) {
            throw new MeetingsException("Current user does not have permission to see this meeting.");
        }
    }
    
    /**
     * Method to evaluate if there is a calendar tool added to a site
     * @param siteId
     * @return
     * @throws MeetingsException 
     */
    @GetMapping(value = "/meetings/site/{siteId}/existcalendar", produces = MediaType.APPLICATION_JSON_VALUE)
    private boolean isThereAnyCalendarForSite(@PathVariable String siteId) throws MeetingsException {
        checkCurrentUserInSite(siteId);
        boolean result = false;
        String calReference = "/calendar/calendar/" + siteId + "/main";
        try {
            Calendar calendar = calendarService.getCalendar(calReference);
            if (calendar != null) {
                result = true;
            }
        } catch (IdUnusedException | PermissionException e) {
            log.warn("The site {} has no calendars", siteId);
        }
        return result;
    }
    
    /**
     * Retrieves the groups list from a site
     * @param siteId
     * @return
     * @throws MeetingsException
     */
    @GetMapping(value = "/meetings/site/{siteId}/groups", produces = MediaType.APPLICATION_JSON_VALUE)
    public Iterable<GroupData> getSiteGroups(@PathVariable String siteId) throws MeetingsException {
        checkCurrentUserInSite(siteId);
        List<GroupData> siteGroups = new ArrayList<>();
        try {
            Site site = siteService.getSite(siteId);
            Collection<Group> groups = site.getGroups();
            groups.stream().forEach(group -> {
               GroupData data = new GroupData();
               data.setGroupId(group.getId());
               data.setGroupName(group.getTitle());
               siteGroups.add(data);
            });
        } catch (IdUnusedException e) {
            log.error("Error retrieving groups", e);
            throw new MeetingsException(e.getLocalizedMessage());
        }
        return siteGroups;
    }
    
    /**
     * Method to retrieve the list of participants in a meeting
     * @param meetingId
     * @return
     * @throws MeetingsException
     */
    @GetMapping(value = "/meeting/{meetingId}/participants", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ParticipantData> getParticipants(@PathVariable String meetingId) throws MeetingsException {
        checkCurrentUserInMeeting(meetingId);
        final List<ParticipantData> participants = new ArrayList<>();
        Optional<Meeting> optMeeting = meetingService.getMeetingById(meetingId);
        if (optMeeting.isPresent()) {
            Meeting meeting = optMeeting.get();
            checkCurrentUserInSite(meeting.getSiteId());
            try {
                Site site = siteService.getSite(meeting.getSiteId());
                for (MeetingAttendee attendee : meeting.getAttendees()) {
                    switch (attendee.getType()) {
                        case USER:
                            User user = null;
                            try {
                                user = userDirectoryService.getUser(attendee.getObjectId());
                                ParticipantData participant = new ParticipantData();
                                participant.setUserid(user.getId());
                                participants.add(participant);
                            } catch (UserNotDefinedException e1) {
                                log.error("Error retrieving participants", e1);
                            }
                            
                            break;
                        case SITE:
                            site.getMembers().stream().forEach(member -> {
                                ParticipantData siteParticipant = new ParticipantData();
                                User siteUser = null;
                                try {
                                    siteUser = userDirectoryService.getUser(member.getUserId());
                                    siteParticipant.setUserid(siteUser.getId());
                                    participants.add(siteParticipant);
                                } catch (UserNotDefinedException e) {
                                    log.error("Error retrieving participants", e);
                                }
                            });
                            break;
                        case GROUP:
                            site.getMembersInGroups(Collections.singleton(attendee.getObjectId()))
                            .stream().forEach(userId -> {
                                ParticipantData groupParticipant = new ParticipantData();
                                User groupUser = null;
                                try {
                                    groupUser = userDirectoryService.getUser(userId);
                                    groupParticipant.setUserid(groupUser.getId());
                                    participants.add(groupParticipant);
                                } catch (UserNotDefinedException e) {
                                    log.error("Error retrieving participants", e);
                                }
                            });
                            break;
                        default: break;
                    }
                }
                return participants.stream().distinct().collect(Collectors.toList());
            } catch (IdUnusedException e) {
                log.error("Error retrieving participants", e);
                throw new MeetingsException(e.getLocalizedMessage());
            }
        }
        return participants;
    }

    /**
     * Retrieves all current user meetings
     * @return
     * @throws MeetingsException 
     */
    @GetMapping(value = "/meetings/site/{siteId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Iterable<MeetingData> getSiteMeetings(@PathVariable String siteId) throws MeetingsException {
        checkCurrentUserInSite(siteId);
        // Retrieve meetings for which the user has permission 
        String userId = getCurrentUser().getId();
        List<Meeting> meetingList = null;
        if (securityService.isSuperUser()) {
            meetingList = meetingService.getAllMeetingsFromSite(siteId);
        } else {
            try {
                Site site = siteService.getSite(siteId);
                List<Group> groups = new ArrayList<>();
                groups.addAll(site.getGroupsWithMember(userId));
                site.getGroupsWithMember(userId);
                List<String> groupIds = groups.stream().map(e->e.getId()).collect(Collectors.toList());
                meetingList = meetingService.getUserMeetings(userId, siteId, groupIds);   
            } catch (IdUnusedException e) {
                log.error("Error while retrieving group list on Meetings", e);
            }
        }
        // Compose the data to send to the frontend
        List<MeetingData> data = new ArrayList<>();
        meetingList.stream().forEach(meeting -> {
           MeetingData item = new MeetingData();
           BeanUtils.copyProperties(meeting, item);
           item.setStartDate(meeting.getStartDate().toString());
           item.setEndDate(meeting.getEndDate().toString());
           data.add(item);
        });
        
        return data;
    }
    
    @GetMapping(value = "/meeting/{meetingId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public MeetingData getMeeting(@PathVariable String meetingId) throws MeetingsException {
        checkCurrentUserInMeeting(meetingId);
        Optional<Meeting> optMeeting = meetingService.getMeetingById(meetingId);
        if (optMeeting.isPresent()) {
            final MeetingData data = new MeetingData();
            Meeting meeting = optMeeting.get();
            BeanUtils.copyProperties(meeting, data);
            data.setStartDate(meeting.getStartDate().toString());
            data.setEndDate(meeting.getEndDate().toString());
            List<String> meetingGroupIds = new ArrayList<String>();
            meeting.getAttendees().stream().forEach(attendee -> {
                switch (attendee.getType()) {
                    case SITE:
                        data.setParticipantOption(attendee.getType());
                        break;
                    case GROUP:
                        data.setParticipantOption(attendee.getType());
                        meetingGroupIds.add(attendee.getObjectId());
                        break;
                    default: break;
                }
            });
            data.setGroupSelection(meetingGroupIds);
            String calendarEventId = meetingService.getMeetingProperty(meeting, CALENDAR_EVENT_ID);
            data.setSaveToCalendar(StringUtils.isNotBlank(calendarEventId));
            data.setParticipants(getParticipants(meetingId));
            return data;
        }
        return null;
    }
    
    
    /**
     * Method to save a new meeting
     * @param data
     * @return
     * @throws MeetingsException
     */
    @PostMapping(value = "/meeting", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Meeting createMeeting(@RequestBody MeetingData data) throws MeetingsException {
        checkUpdatePermissions(data.getSiteId());
        Meeting meeting = null;
        User user = getCurrentUser();
        try {
            // Meeting info
            meeting = new Meeting();
            BeanUtils.copyProperties(data, meeting);
            meeting.setStartDate(Instant.parse(data.getStartDate()));
            meeting.setEndDate(Instant.parse(data.getEndDate()));
            meeting.setOwnerId(user.getId());
            // Online meeting creation with the selected provider
            String onlineMeetingId = null;
            String onlineMeetingUrl = null;
            if (MS_TEAMS.equals(data.getProvider())) {
            	TeamsMeetingData meetingTeams = teamsService.onlineMeeting(user.getEmail(), meeting.getTitle(), meeting.getStartDate(), meeting.getEndDate());
            	onlineMeetingUrl = meetingTeams.getJoinUrl();
            	onlineMeetingId = meetingTeams.getId();
            }
            meeting.setUrl(onlineMeetingUrl);
            // Participants
            MeetingAttendee attendee = new MeetingAttendee();
            List<MeetingAttendee> meetingAttendees = new ArrayList<MeetingAttendee>();
            attendee.setType(AttendeeType.USER);
            attendee.setObjectId(user.getId());
            meetingAttendees.add(attendee);
            attendee.setMeeting(meeting);
            switch (data.getParticipantOption()) {
                case SITE:
                    attendee = new MeetingAttendee();
                    attendee.setType(AttendeeType.SITE);
                    attendee.setObjectId(data.getSiteId());
                    meetingAttendees.add(attendee);
                    attendee.setMeeting(meeting);
                    break;
                case GROUP:
                    for (String groupId : data.getGroupSelection()) {
                        attendee = new MeetingAttendee();
                        attendee.setType(AttendeeType.GROUP);
                        attendee.setObjectId(groupId);
                        meetingAttendees.add(attendee);
                        attendee.setMeeting(meeting);	
                    }
                    break;
                default:
                    break;
            }
            meeting.setAttendees(meetingAttendees);
            // Meeting creation
            meeting = meetingService.createMeeting(meeting);
            // Properties
            meetingService.setMeetingProperty(meeting, ORGANIZER_USER, user.getEmail());
            meetingService.setMeetingProperty(meeting, ONLINE_MEETING_ID, onlineMeetingId);
            // Calendar events
            if (data.isSaveToCalendar() && isThereAnyCalendarForSite(data.getSiteId())
                    && StringUtils.isNotBlank(data.getStartDate()) && StringUtils.isNotBlank(data.getEndDate())) {
                this.saveToCalendar(meeting);
            }
            // Notifications
            this.sendNotification(meeting, data.getNotificationType());
        } catch (DateTimeParseException e) {
            log.error("Could not parse Meetings start date string '{}' or end time string '{}'", meeting.getStartDate(), meeting.getEndDate());
            throw new MeetingsException(e.getLocalizedMessage());
        } catch (AddressException e) {
            log.error("The system email for notifications is not a valid email address.", e);
            throw new MeetingsException(e.getLocalizedMessage());
        } catch (IdUnusedException e) {
            log.error("Error retrieving site when sending notifications.", e);
            throw new MeetingsException(e.getLocalizedMessage());
        } catch (Exception e) {
            log.error("Error creating meeting", e);
            throw new MeetingsException(e.getLocalizedMessage());
        }
        return meeting;
    }
    
    /**
     * Method to update an existing meeting
     * @param data
     * @param meetingId
     * @return
     * @throws MeetingsException 
     */
    @PutMapping(value = "/meeting/{meetingId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Meeting updateMeeting(@RequestBody MeetingData data, @PathVariable String meetingId) throws MeetingsException {
        checkUpdatePermissions(data.getSiteId());
        checkCurrentUserInMeeting(meetingId);
        Meeting meeting = null;
        try {
            // Remove site and group attendees
            meetingService.removeSiteAndGroupAttendeesByMeetingId(meetingId);
            meeting = meetingService.getMeeting(meetingId);
            // Meeting info
            meeting.setTitle(data.getTitle());
            meeting.setDescription(data.getDescription());
            meeting.setStartDate(Instant.parse(data.getStartDate()));
            meeting.setEndDate(Instant.parse(data.getEndDate()));
            // Participants
            MeetingAttendee attendee = null;
            switch (data.getParticipantOption()) {
                case SITE:
                    attendee = new MeetingAttendee();
                    attendee.setType(AttendeeType.SITE);
                    attendee.setObjectId(data.getSiteId());
                    meeting.getAttendees().add(attendee);
                    attendee.setMeeting(meeting);
                    break;
                case GROUP:
                    for (String groupId : data.getGroupSelection()) {
                        attendee = new MeetingAttendee();
                        attendee.setType(AttendeeType.GROUP);
                        attendee.setObjectId(groupId);
                        meeting.getAttendees().add(attendee);
                        attendee.setMeeting(meeting);
                    }
                    break;
                default:
                    break;
            }
            // Update meeting
            meetingService.updateMeeting(meeting);
            // Calendar events
            if (data.isSaveToCalendar() && isThereAnyCalendarForSite(data.getSiteId())
                    && StringUtils.isNotBlank(data.getStartDate()) && StringUtils.isNotBlank(data.getEndDate())) {
                this.saveToCalendar(meeting);
            }
            // Notifications
            this.sendNotification(meeting, data.getNotificationType());
        } catch (DateTimeParseException e) {
            log.error("Could not parse Meetings start date string '{}' or end time string '{}'", meeting.getStartDate(), meeting.getEndDate());
            throw new MeetingsException(e.getLocalizedMessage());
        } catch (AddressException e) {
            log.error("The system email for notifications is not a valid email address.", e);
            throw new MeetingsException(e.getLocalizedMessage());
        } catch (IdUnusedException e) {
            log.error("Error retrieving site when sending notifications.", e);
            throw new MeetingsException(e.getLocalizedMessage());
        }
        return meeting;
    }
    
    /**
     * Method to remove an existing meeting
     * @param meetingId
     * @throws MeetingsException
     */
    @DeleteMapping(value = "/meeting/{meetingId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteMeeting(@PathVariable String meetingId) throws MeetingsException {
        checkCurrentUserInMeeting(meetingId);
        Meeting meeting = meetingService.getMeeting(meetingId);
        checkUpdatePermissions(meeting.getSiteId());
        try {
            this.removeFromCalendar(meetingId);
            meetingService.deleteMeetingById(meetingId);
        } catch (Exception e) {
            log.error("Error deleting meeting", e);
            throw new MeetingsException(e.getLocalizedMessage());
        }
    }
    
    /**
     * Get i18n bundle
     * @param bundle
     * @param locale
     * @return
     * @throws MeetingsException 
     */
    @GetMapping(value = "/i18n/{locale}/{bundle}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getI18nProperties(@PathVariable String bundle, @PathVariable String locale) throws MeetingsException {
        checkSakaiSession();
        StringBuilder i18n = new StringBuilder();
        if (StringUtils.isNotBlank(bundle) && StringUtils.isNotBlank(locale)) {
            ResourceLoader rbundle = new ResourceLoader(bundle);
            if (rbundle != null) {
                rbundle.setContextLocale(Locale.forLanguageTag(locale));
                rbundle.forEach((k, v) -> i18n.append(k).append("=").append(v).append("\n"));
            }
        }
        return i18n.toString();
    }
    
    /**
     * Returns true if MS Teams is set up in Sakai properties
     * @return
     * @throws MeetingsException
     */
    @GetMapping(value = "/meetings/isteamssetup", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean isMicrosofTeamsConfigured() throws MeetingsException {
        checkSakaiSession();
        return teamsService.isMicrosofTeamsConfigured();
    }
    
    /**
     * Method to send notifications to users about meetings, by level of priority
     * @param meeting
     * @param type
     * @throws IdUnusedException 
     * @throws AddressException 
     */
    private void sendNotification(Meeting meeting, NotificationType type) throws IdUnusedException, AddressException {
        if (type == NotificationType.ALL && meeting != null) {
            Site site = siteService.getSite(meeting.getSiteId());
            InternetAddress from = new InternetAddress(
              serverConfigurationService.getString(SMTP_FROM, NO_REPLY + serverConfigurationService.getServerName())
            );
            String subject = MessageFormat.format(rb.getString(NOTIF_SUBJECT), meeting.getTitle(), site.getTitle());
            String content = MessageFormat.format(rb.getString(NOTIF_CONTENT), meeting.getTitle(), site.getTitle());
            Set<Member> members = site.getMembers();
            InternetAddress[] participantEmails = members.stream().map(member -> {
                InternetAddress address = null;
                try {
                    User user = userDirectoryService.getUser(member.getUserId());
                    address = new InternetAddress(user.getEmail());
                } catch (UserNotDefinedException e) {
                    log.warn("The member does not exist as a user.");
                } catch (AddressException e) {
                    log.warn("The user email is not a valid email address.");
                }  
                return address;
            }).collect(Collectors.toList()).stream().toArray(InternetAddress[]::new);
            emailService.sendMail(from, participantEmails, subject, content, null, null, null, null);
        }
    }
    
    /**
     * Method to save a meeting as an event of the Sakai calendar
     * @param meeting
     * @throws MeetingsException
     */
    private void saveToCalendar(Meeting meeting) {
        String calReference = "/calendar/calendar/" + meeting.getSiteId() + "/main";
        CalendarEdit calendar = null;
        try {
            Site site = siteService.getSite(meeting.getSiteId());
            calendar = calendarService.editCalendar(calReference);
            String calendarEventId = meetingService.getMeetingProperty(meeting, CALENDAR_EVENT_ID);
            if (StringUtils.isNotBlank(calendarEventId)) {
                CalendarEventEdit eventRemove = calendar.getEditEvent(calendarEventId, CalendarService.EVENT_REMOVE_CALENDAR_EVENT);
                calendar.removeEvent(eventRemove);
            }
            CalendarEventEdit cedit = calendar.addEvent();
            long duration = meeting.getEndDate().toEpochMilli() - meeting.getStartDate().toEpochMilli();
            TimeRange timeRange = timeService.newTimeRange(meeting.getStartDate().toEpochMilli(), duration);
            cedit.setRange(timeRange);
            cedit.setDisplayName(meeting.getTitle());
            cedit.setDescription(meeting.getDescription());
            cedit.setType(MEETING_EVENT_TYPE);
            cedit.setDescriptionFormatted(meeting.getDescription());
            // Control group access
            cedit.clearGroupAccess();
            List<Group> groups = new ArrayList<Group>();
            meeting.getAttendees().stream()
                .filter(attendee -> AttendeeType.GROUP.equals(attendee.getType()))
                .forEach(attendee -> {
                    groups.add(site.getGroup(attendee.getObjectId()));
                });
            if (!groups.isEmpty()) {
                cedit.setGroupAccess(groups, false);    
            }
            calendar.commitEvent(cedit);
            calendarService.commitCalendar(calendar);
            meetingService.setMeetingProperty(meeting, CALENDAR_EVENT_ID, cedit.getId());
        } catch (Exception e) {
            calendarService.cancelCalendar(calendar);
            log.error("WS addCalendarEvent(): error " + e.getClass().getName() + " : " + e.getMessage());
        }
    }
    
    
    /**
     * Method to remove a calendar event based on a meeting
     * @param meetingId
     * @throws MeetingsException
     */
    private void removeFromCalendar(String meetingId) throws MeetingsException {
        try {
            Optional<Meeting> optMeeting = meetingService.getMeetingById(meetingId);
            if (optMeeting.isPresent()) {
                Meeting meeting = optMeeting.get();
                String calendarEventId = meetingService.getMeetingProperty(meeting, CALENDAR_EVENT_ID);
                if (StringUtils.isNotBlank(calendarEventId)) {
                    String calendarReference = calendarService.calendarReference(meeting.getSiteId(), SiteService.MAIN_CONTAINER);
                    Calendar calendar = calendarService.getCalendar(calendarReference);
                    CalendarEventEdit eventRemove = calendar.getEditEvent(calendarEventId, CalendarService.EVENT_REMOVE_CALENDAR_EVENT);
                    calendar.removeEvent(eventRemove);
                }
            }
        } catch (PermissionException | InUseException e) {
            log.warn("Error removing meeting from the calendar", e);
            throw new MeetingsException(e.getLocalizedMessage());
        } catch (IdUnusedException e) {
            log.warn("Error removing meeting from the calendar. The calendar event does not exist", e);
        }

    }

}
