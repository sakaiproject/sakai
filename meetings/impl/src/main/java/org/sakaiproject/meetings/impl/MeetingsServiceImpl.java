/**
 * Copyright (c) 2010 onwards - The Sakai Foundation
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

package org.sakaiproject.meetings.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.FormatStyle;

import javax.annotation.Resource;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.CalScale;
import java.time.Duration;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Url;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.RandomUidGenerator;
import net.fortuna.ical4j.util.UidGenerator;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.meetings.api.MeetingsException;
import org.sakaiproject.meetings.api.MeetingRole;
import org.sakaiproject.meetings.api.MeetingsService;
import org.sakaiproject.meetings.api.Participant;
import org.sakaiproject.meetings.api.SelectionType;
import org.sakaiproject.meetings.api.beans.MeetingTransferBean;
import org.sakaiproject.meetings.api.persistence.Meeting;
import org.sakaiproject.meetings.api.persistence.MeetingParticipant;
import org.sakaiproject.meetings.api.persistence.MeetingRepository;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.email.api.Attachment;
import org.sakaiproject.email.api.ContentType;
import org.sakaiproject.email.api.EmailAddress;
import org.sakaiproject.email.api.EmailMessage;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.email.api.EmailAddress.RecipientType;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;

import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MeetingsServiceImpl implements MeetingsService {

    @Resource private MeetingRepository meetingRepository;
    @Resource private BBBEndpointHandler handler;
    @Resource private UserDirectoryService userDirectoryService;
    @Resource private SiteService siteService;
    @Resource private EmailService emailService;
    @Resource private EventTrackingService eventTrackingService;
    @Resource(name = "org.sakaiproject.authz.api.SecurityService")
    private SecurityService securityService;
    @Resource private AuthzGroupService authzGroupService;
    @Resource private SessionManager sessionManager;
    @Resource private ToolManager toolManager;
    @Resource private FunctionManager functionManager;
    @Resource private ServerConfigurationService serverConfigurationService;
    @Resource private PreferencesService preferencesService;
    @Resource private TimeService timeService;
    @Resource(name = "org.sakaiproject.time.api.UserTimeService")
    private UserTimeService userTimeService;
    @Resource private IdManager idManager;

    // -----------------------------------------------------------------------
    // --- Initialization/Spring related methods -----------------------------
    // -----------------------------------------------------------------------
    public void init() {

        log.info("init()");

        FUNCTIONS.forEach(f -> functionManager.registerFunction(f, true));
    }

    public Optional<MeetingTransferBean> getMeeting(String meetingId, String siteId) throws SecurityException, Exception {

        String currentUserId = sessionManager.getCurrentSessionUserId();

        if (!securityService.isSuperUser() && !securityService.unlock(currentUserId, FN_PARTICIPATE, "/site/" + siteId)) {
            throw new SecurityException("Current user is not allowed to participate in meetings");
        }

        Optional<Meeting> optMeeting = meetingRepository.findById(meetingId);
        if (optMeeting.isPresent()) {
            return Optional.of(MeetingTransferBean.of(processMeeting(optMeeting.get())));
        } else {
            return Optional.<MeetingTransferBean>empty();
        }
    }

    public List<MeetingTransferBean> getSiteMeetings(String siteId) {

        log.debug("getSiteMeetings({})", siteId);

        try {
            final Site site = siteService.getSite(siteId);
            return meetingRepository.findBySiteId(siteId, NOT_INCLUDE_DELETED_MEETINGS).stream()
                .map(m -> {
                    try {
                        return decorateMeetingBean(MeetingTransferBean.of(processMeeting(m)), site);
                    } catch (Exception e) {
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());
        } catch (IdUnusedException e) {
            log.warn("No site for id {}", siteId);
            return Collections.<MeetingTransferBean>emptyList();
        }
    }

    @Transactional
    public MeetingTransferBean saveMeeting(MeetingTransferBean meetingBean, boolean meetingOnly) throws Exception {

        String currentUserId = userDirectoryService.getCurrentUser().getId();

        if (!securityService.unlock(currentUserId, FN_CREATE, "/site/" + meetingBean.siteId)) {
            throw new SecurityException("You are not allowed to create meetings in this site");
        }

        Meeting meeting = meetingBean.toMeeting();

        if (StringUtils.isBlank(meetingBean.id)) {

            // generate differentiated passwords
            meeting.setAttendeePassword(generatePassword());
            do {
                meeting.setModeratorPassword(generatePassword());
            } while (meeting.getAttendeePassword().equals(meeting.getModeratorPassword()));

            meeting.setOwnerId(currentUserId);

            // send email notifications to participants
            if (meetingBean.notifyParticipants) {
                notifyParticipants(meeting, true, false, 0L, false);
            }

            // add start date to Calendar
            if (meetingBean.addToCalendar && meetingBean.startDate != null) {
                addEditCalendarEvent(meeting);
            }

            meeting.setVoiceBridge(70000 + new Random().nextInt(10000));

            // log event
            logEvent(EVENT_MEETING_CREATE, meeting);
        }

        // store locally, in DB
        try {
            //if meetingOnly is true, only update meeting properties
            if (!meetingOnly) {
                // send email notifications to participants
                if (meetingBean.notifyParticipants) {
                    notifyParticipants(meeting, false, meetingBean.iCalAttached, meetingBean.iCalAlarmMinutes, false);
                }
                if (meetingBean.addToCalendar && meeting.getStartDate() != null) {
                    addEditCalendarEvent(meeting);
                } else if (meetingBean.calendarEventId != null && !meetingBean.addToCalendar) {
                    removeCalendarEvent(meeting);
                }
            }

            meeting = meetingRepository.save(meeting);

            // set meeting join url (for moderator, which is current user)
            User user = userDirectoryService.getCurrentUser();
            meeting.setJoinUrl(handler.getJoinMeetingURL(meeting, user, true));

            // log event
            logEvent(EVENT_MEETING_EDIT, meeting);

            return decorateMeetingBean(MeetingTransferBean.of(processMeeting(meeting)), siteService.getSite(meeting.getSiteId()));
        } catch (Exception e) {
            log.error("Unable to update meeting in Sakai");
            return null;
        }
    }

    public boolean isMeetingRunning(String meetingId) throws MeetingsException {
        return handler.isMeetingRunning(meetingId);
    }

    public Map<String, Object> getMeetingInfo(String meetingId) throws Exception {

        log.debug("getMeetingInfo({})", meetingId);

        Optional<Meeting> optMeeting = meetingRepository.findById(meetingId);

        if (optMeeting.isPresent()) {
            return handler.getMeetingInfo(optMeeting.get().getId(), optMeeting.get().getModeratorPassword());
        } else {
            throw new IdUnusedException("No meeting for id " + meetingId);
        }
    }

    public MeetingTransferBean getMeetingTemplate(String siteId) {

        MeetingParticipant all = new MeetingParticipant();
        all.setSelectionType(SelectionType.ALL);
        all.setSelectionId(siteId);
        all.setRole(MeetingRole.ATTENDEE);

        MeetingParticipant owner = new MeetingParticipant();
        owner.setSelectionType(SelectionType.USER);
        owner.setSelectionId(userDirectoryService.getCurrentUser().getId());
        owner.setRole(MeetingRole.MODERATOR);

        Meeting meeting = new Meeting();
        List<MeetingParticipant> participants = new ArrayList<>();
        participants.add(all);
        participants.add(owner);
        meeting.setParticipants(participants);
        meeting.setSiteId(siteId);

        meeting.getProperties().put("enablePublicChat", Boolean.TRUE.toString());
        meeting.getProperties().put("enablePrivateChat", Boolean.TRUE.toString());

        return MeetingTransferBean.of(meeting);
    }

    public Map<String, Object> getRecordings(String meetingId, String siteId) throws Exception {

        log.debug("getRecordings({}, {})", meetingId, siteId);

        Optional<Meeting> optMeeting = meetingRepository.findById(meetingId);

        if (!optMeeting.isPresent()) {
            throw new IdUnusedException("No meeting for id " + meetingId);
        }

        Meeting meeting = optMeeting.get();

        Map<String, Object> recordings = new HashMap<>();
        if (!meeting.getRecording()) {
            log.debug("Meeging {} is not set to record, setting empty recordings object ...", meetingId);
            //Mimic empty recordings object
            recordings.put("recordings", "");
            return recordings;
        }

        Map<String, String> ownerIds = new HashMap<>();
        ownerIds.put(meetingId, meeting.getOwnerId());
        return postProcessRecordings(handler.getRecordings(meetingId), siteId, ownerIds);
    }

    private Map<String, Object> postProcessRecordings(Map<String, Object> recordings, String siteId, Map<String, String> ownerIds) {

        List<Map<String, Object>> recordingList = (List<Map<String, Object>>) recordings.get("recordings");
        if ("SUCCESS".equals(recordings.get("returncode")) && recordingList != null) {
            log.debug("{} recordings retrieved from BBB server", recordingList.size());
            boolean recordingFilterEnabled = isRecordingFormatFilterEnabled();
            User user = userDirectoryService.getCurrentUser();
            String userId = user.getId();
            for (Map<String, Object> recordingItem : recordingList) {
                // Add meeting ownerId to the recording
                recordingItem.put("ownerId", ownerIds.get((String) recordingItem.get("meetingId")));
                // Filter formats that are not allowed to be shown, only if filter is enabled.
                if (recordingFilterEnabled) {
                    recordingsFilterFormats(recordingItem, siteId, userId);
                }
            }
        }
        return recordings;
    }

    private List<String> getActiveGroupMembers(AuthzGroup group) {

        return group.getMembers().stream().filter(Member::isActive)
            .map(Member::getUserId).collect(Collectors.toList());
    }

    public Map<String, Object> getSiteRecordings(String siteId) throws SecurityException, Exception {

        log.debug("getSiteRecordings({})", siteId);

        Map<String, Object> recordings;
        List<Meeting> meetings = meetingRepository.findBySiteId(siteId, INCLUDE_DELETED_MEETINGS);
        if (meetings.size() == 0 || !handler.isRecordingEnabled()) {
            log.debug("Either no meetings, or recording is not enabled. Returning noRecordings ...");
            // Set an empty List of recordings and a SUCCESS key as default response values.
            recordings = new HashMap<>();
            recordings.put("recordings", new ArrayList<Object>());
            recordings.put("returncode", "SUCCESS");
            recordings.put("messageKey", "noRecordings");
            return recordings;
        }

        Site site = null;
        try {
            site = siteService.getSite(siteId);
        } catch (IdUnusedException e) {
            log.error("No site for id {}", siteId);
        }

        Map<String, String> ownerIds = new HashMap<>();
        String meetingId;
        String ownerId;
        String meetingIds = "";
        for (Meeting meeting : meetings) {
            if (!meeting.getRecording()) {
                log.debug("Meeting {} is not set to be recorded. Skipping ...", meeting.getId());
                // Meeting is not set to be recorded
                continue;
            }

            List<String> permittedUserIds = getMeetingUsers(meeting, site).stream()
                .map(User::getId).collect(Collectors.toList());

            if (!securityService.isSuperUser()
                && !permittedUserIds.contains(userDirectoryService.getCurrentUser().getId())) {
                log.debug("Not super user and not a permitted user. Skipping {} ...", meeting.getId());
                continue;
            }

            meetingId = meeting.getId();
            ownerId = meeting.getOwnerId();
            ownerIds.put(meetingId, ownerId);
            if (!meetingIds.equals("")) {
                meetingIds += ",";
            }
            meetingIds += meetingId;
        }
        // Safety for BBB-148. Make sure meetingIds is not empty.
        if (meetingIds.equals("")) {
            log.debug("meetingIds is empty. Returning noRecordings ...");
            // Set an empty List of recordings and a SUCCESS key as default response values.
            recordings = new HashMap<>();
            recordings.put("recordings", new ArrayList<Object>());
            recordings.put("returncode", "SUCCESS");
            recordings.put("messageKey", "noRecordings");
            return recordings;
        }

        return postProcessRecordings(handler.getRecordings(meetingIds), siteId, ownerIds);
    }

    private void recordingsFilterFormats(Map<String, Object> recordingItem, String siteId, String userId) {

        String ownerId = (String) recordingItem.get("ownerId");
        ((List<Map<String, Object>>) recordingItem.get("playback"))
            .removeIf(f -> recordingsFilterFormatRemovable((String) f.get("type"), siteId, ownerId, userId));
    }

    private boolean recordingsFilterFormatRemovable(String type, String siteId, String ownerId, String userId) {

        // Validate if type is whitelisted.
        List<String> whiltelist = Arrays.asList(this.getRecordingFormatFilterWhitelist().split(","));
        if (whiltelist.contains(type)) {
            // It is whitelisted, don't remove.
            return false;
        }
        // Validate if user is allowed to view extended formats
        if (ownerId.equals(userId)) {
            if (isUserAllowedInLocation(userId, FN_RECORDING_EXTENDEDFORMATS_OWN, siteId)) {
                // User allowed, don't remove.
                return false;
            }
        }
        if (isUserAllowedInLocation(userId, FN_RECORDING_EXTENDEDFORMATS_ANY, siteId)) {
            // User allowed, don't remove.
            return false;
        }
        // Remove it
        return true;
    }

    public void logMeetingJoin(String meetingId) throws Exception {

        Optional<Meeting> optMeeting = meetingRepository.findById(meetingId);
        if (optMeeting.isPresent()) {
            logEvent(EVENT_MEETING_JOIN, optMeeting.get());
        } else {
            throw new IdUnusedException("No meeting for id " + meetingId);
        }
    }

    public boolean endMeeting(String meetingId) throws Exception {

        Optional<Meeting> optMeeting = meetingRepository.findById(meetingId);
        if (!optMeeting.isPresent()) {
            throw new IdUnusedException("No meeting for id " + meetingId);
        }

        Meeting meeting = optMeeting.get();

        if (!getCanEdit(meeting.getSiteId(), meeting)) {
            throw new SecurityException("You are not allowed to end this meeting");
        }

        // end meeting on server, if running
        handler.endMeeting(meetingId, meeting.getModeratorPassword());

        // log event
        logEvent(EVENT_MEETING_END, meeting);

        return true;
    }

    public boolean deleteMeeting(String meetingId) throws Exception {

        Optional<Meeting> optMeeting = meetingRepository.findById(meetingId);
        if (!optMeeting.isPresent()) {
            throw new IdUnusedException("No meeting for id " + meetingId);
        }

        Meeting meeting = optMeeting.get();

        if (!getCanDelete(meeting.getSiteId(), meeting)) {
            throw new SecurityException("You are not allow to end this meeting");
        }

        // end meeting on server, if running
        try{
            if(handler.isMeetingRunning(meetingId))
                handler.endMeeting(meetingId, meeting.getModeratorPassword());
        } catch( Exception e) {

        }

        // log event
        logEvent(EVENT_MEETING_END, meeting);

        // remove event from Calendar
        removeCalendarEvent(meeting);

        // remove from DB, if no exceptions were thrown
        meeting.setDeleted(true);
        meetingRepository.save(meeting);
        return true;
    }

    public int deleteMeetingsBySiteId(String siteId) throws SecurityException, MeetingsException {

        String currentUserId = userDirectoryService.getCurrentUser().getId();

        if (!securityService.unlock(currentUserId, FN_DELETE_ANY, "/site/" + siteId)) {
            throw new SecurityException("Current user cannot delete the meetings in site " + siteId);
        }

        return meetingRepository.deleteBySiteId(siteId);
    }

    public boolean deleteRecordings(String recordID)
            throws SecurityException, MeetingsException {
        return handler.deleteRecordings(recordID);
    }

    public boolean publishRecordings(String recordID, String publish)
            throws SecurityException, MeetingsException {
        return handler.publishRecordings(recordID, publish);
    }

    public boolean protectRecordings(String recordID, String protect)
            throws SecurityException, MeetingsException {
        return handler.protectRecordings(recordID, protect);
    }

    public void checkJoinMeetingPreConditions(Meeting meeting)
            throws MeetingsException {
        // check if meeting is within dates

        Site meetingSite = null;
        try{
            meetingSite = siteService.getSite(meeting.getSiteId() );
        } catch( Exception e) {
            log.warn("There is an error with the site in this meeting {}: ", meeting.getSiteId(), e.getMessage(), e);
        }

        Instant now = Instant.ofEpochMilli(Long.parseLong((String) getServerTimeInDefaultTimezone().get("timestamp")));

        boolean startOk = meeting.getStartDate() == null || meeting.getStartDate().isBefore(now);
        boolean endOk = meeting.getEndDate() == null || meeting.getEndDate().isAfter(now);

        if (!startOk)
            throw new MeetingsException(MeetingsException.MESSAGEKEY_NOTSTARTED, "Meeting has not started yet.");
        if (!endOk)
            throw new MeetingsException(MeetingsException.MESSAGEKEY_ALREADYENDED, "Meeting has already ended.");

        // Add the metadata to be used in case of create
        Map<String, String> tmpMeta = meeting.getMeta();
        if( !tmpMeta.containsKey("origin")) tmpMeta.put("origin", "Sakai");
        if( !tmpMeta.containsKey("originVersion")) tmpMeta.put("originVersion", serverConfigurationService.getString("version.sakai", ""));
        ResourceLoader toolParameters = new ResourceLoader("meetings");
        if( !tmpMeta.containsKey("originServerCommonName")) tmpMeta.put("originServerCommonName", serverConfigurationService.getServerName());
        if( !tmpMeta.containsKey("originServerUrl")) tmpMeta.put("originServerUrl", serverConfigurationService.getServerUrl().toString());
        if( !tmpMeta.containsKey("originTag")) tmpMeta.put("originTag", "Sakai[" + serverConfigurationService.getString("version.sakai", "") + "]");
        if( !tmpMeta.containsKey("context")) tmpMeta.put("context", siteService.getSiteDisplay(meeting.getSiteId()) );
        if( !tmpMeta.containsKey("contextId")) tmpMeta.put("contextId", meeting.getSiteId() );
        if( !tmpMeta.containsKey("contextActivity")) tmpMeta.put("contextActivity", meeting.getName() );

        /*
         * //////////////////////////////////////////////////////////////////////////////////////////////////
         * //This implementation will work only for a small number of users enrolled (teachers or students)
         * //this is beacuse the long a GET call can be is limited by the configuration of the Webserver
         * //////////////////////////////////////////////////////////////////////////////////////////////////
         *

        Map<String, User> attendees = new HashMap<String, User>();
        Map<String, User> moderators = new HashMap<String, User>();
        List<Participant> participants = meeting.getParticipants();
        if( participants != null ){
            for( int i=0; i < participants.size(); i++){
                Participant participant = participants.get(i);
                if( (Participant.MODERATOR).equals(participant.getRole()) ){
                    moderators.putAll(getUsersParticipating(participant.getSelectionType(), participant.getSelectionId(), meetingSite));
                } else {
                    attendees.putAll(getUsersParticipating(participant.getSelectionType(), participant.getSelectionId(), meetingSite));
                }
            }
        }

        if( !tmpMeta.containsKey("meetingModerators")){
            String meetingModerator = "";
            for( Map.Entry<String, User> e: moderators.entrySet()){
                if( meetingModerator.length() > 0 ) meetingModerator += ", ";
                meetingModerator += e.getValue().getFirstName() + " " + e.getValue().getLastName() + " <" + e.getValue().getEmail() + ">";

            }
            tmpMeta.put("meetingModerator", meetingModerator);

        }
        if( !tmpMeta.containsKey("meetingAttendees")){
            String meetingAttendee = "";
            for( Map.Entry<String, User> e: attendees.entrySet()){
                if( meetingAttendee.length() > 0 ) meetingAttendee += ", ";
                meetingAttendee += e.getValue().getFirstName() + " " + e.getValue().getLastName() + " <" + e.getValue().getEmail() + ">";

            }
            tmpMeta.put("meetingAttendee", meetingAttendee);

        }
        */
        // Metadata ends

        // check if is running, (re)create it if not
        handler.makeSureMeetingExists(meeting);
    }



    // -----------------------------------------------------------------------
    // --- BBB Security related methods --------------------------------------
    // -----------------------------------------------------------------------
    public boolean isMeetingParticipant(Meeting meeting) {
        User currentUser = userDirectoryService.getCurrentUser();
        return getParticipantFromMeeting(meeting, currentUser.getId()) != null;
    }

    public boolean getCanCreate(String siteId) {
        return isUserAllowedInLocation(userDirectoryService.getCurrentUser().getId(), FN_CREATE, siteId);
    }

    public boolean getCanEdit(String siteId, Meeting meeting) {
        String currentUserId = userDirectoryService.getCurrentUser().getId();

        if (meeting != null) {
            // check if owns meeting
            if (currentUserId.equals(meeting.getOwnerId())) {
                if (isUserAllowedInLocation(currentUserId, FN_EDIT_OWN, siteId))
                    return true;
            }
        }

        // otherwise, must be able to edit any meeting
        return isUserAllowedInLocation(currentUserId, FN_EDIT_ANY, siteId);
    }

    public boolean getCanDelete(String siteId, Meeting meeting) {
        String currentUserId = userDirectoryService.getCurrentUser().getId();

        if (meeting != null) {
            // check if owns meeting
            if (currentUserId.equals(meeting.getOwnerId())) {
                if (isUserAllowedInLocation(currentUserId, FN_DELETE_OWN, siteId))
                    return true;
            }
        }

        // otherwise, must be able to delete any meeting
        return isUserAllowedInLocation(currentUserId, FN_DELETE_ANY, siteId);
    }

    public boolean getCanParticipate(String siteId) {
        return isUserAllowedInLocation(userDirectoryService.getCurrentUser().getId(), FN_PARTICIPATE, siteId);
    }

    public boolean getCanViewSiteRecordings(String siteId) {
        return isUserAllowedInLocation(userDirectoryService.getCurrentUser().getId(), FN_RECORDING_VIEW, siteId);
    }

    public void checkPermissions(String siteId) {
        try {
            // get site roles & tool permisions
            Set<Role> roles = null;
            boolean noPermsDefined = true;
            // get site
            // final Site site = siteService.getSite(siteId);
            final AuthzGroup siteAuthz = authzGroupService.getAuthzGroup(siteService.siteReference(siteId));
            // get roles
            roles = siteAuthz.getRoles();
            if (roles == null || roles.size() == 0)
                throw new Exception("No roles defined in site!");
            // check if need to apply defaults (no perms defined)
            for (Role r : roles) {
                Set<String> functions = r.getAllowedFunctions();
                if (functions != null && functions.size() > 0) {
                    for (String fn : functions) {
                        if (fn.startsWith(FN_PREFIX)) {
                            noPermsDefined = false;
                            break;
                        }
                    }
                }
            }

            // no perms defined => apply defaults, if any
            boolean permsSet = false;
            if (noPermsDefined) {
                for (Role r : roles) {
                    String roleId = r.getId();
                    String fns_ = serverConfigurationService.getString(CFG_DEFAULT_PERMS_PRFX + roleId, null);
                    if (fns_ != null) {
                        String[] fns = fns_.split(",");
                        for (int i = 0; i < fns.length; i++) {
                            r.allowFunction(fns[i].trim());
                        }
                        permsSet = true;
                    }
                }
            }

            // apply (new) permissions, if defined
            if (permsSet) {
                AdminExecution exec = new AdminExecution() {
                    @Override
                    public Object execution() throws Exception {
                        authzGroupService.save(siteAuthz);
                        // siteService.save(site);
                        return null;
                    }
                };
                try {
                    exec.execute();
                } catch (Exception e) {
                    log.warn("Unable to apply default BBB permissions to site {}: ", siteId, e.getMessage(), e);
                }
            }

        } catch (Exception e) {
            log.warn("Unable to check tool permissions on site: {}", e.getMessage(), e);
            return;
        }
    }

    // -----------------------------------------------------------------------
    // --- Public utility methods --------------------------------------------
    // -----------------------------------------------------------------------
    public Map<String, Object> getServerTimeInUserTimezone() {

        Map<String, Object> responseMap = new HashMap<>();

        Preferences prefs = preferencesService.getPreferences(userDirectoryService.getCurrentUser().getId());
        TimeZone timeZone = TimeZone.getDefault();
        if (prefs != null) {
            ResourceProperties props = prefs.getProperties(TimeService.APPLICATION_ID);
            String timeZoneStr = props.getProperty(TimeService.TIMEZONE_KEY);
            if( timeZoneStr != null )
                timeZone = TimeZone.getTimeZone(timeZoneStr);
        }

        long timeMs = getTimeInTimezone(new Date(System.currentTimeMillis()), timeZone).getTime();

        responseMap.put("timestamp", "" + timeMs);
        responseMap.put("timezone", "" + timeZone.getDisplayName() );
        responseMap.put("timezoneID", "" + timeZone.getID() );
        responseMap.put("timezoneOffset", "" + timeZone.getOffset(timeMs));
        responseMap.put("defaultOffset", "" + TimeZone.getDefault().getOffset(timeMs));

        return responseMap;
    }

    public Map<String, Object> getServerTimeInDefaultTimezone() {

        Map<String, Object> responseMap = new HashMap<>();

        TimeZone timeZone = TimeZone.getDefault();

        long timeMs = getTimeInTimezone(new Date(System.currentTimeMillis()), timeZone).getTime();

        responseMap.put("timestamp", "" + timeMs);
        responseMap.put("timezone", "" + timeZone.getDisplayName() );
        responseMap.put("timezoneID", "" + timeZone.getID() );
        responseMap.put("timezoneOffset", "" + timeZone.getOffset(timeMs));
        responseMap.put("defaultOffset", "" + TimeZone.getDefault().getOffset(timeMs));

        return responseMap;
    }

    private Date getTimeInUserTimezone(Date time, String userId) {

        Preferences prefs = preferencesService.getPreferences(userId);
        TimeZone timeZone = TimeZone.getDefault();
        if (prefs != null) {
            ResourceProperties props = prefs.getProperties(TimeService.APPLICATION_ID);
            String timeZoneStr = props.getProperty(TimeService.TIMEZONE_KEY);
            if( timeZoneStr != null )
                timeZone = TimeZone.getTimeZone(timeZoneStr);
        }

        return getTimeInTimezone(time, timeZone);
    }

    private Date getTimeInDefaultTimezone(Date time) {

        TimeZone timeZone = TimeZone.getDefault();

        return getTimeInTimezone(time, timeZone);
    }

    private Date getTimeInTimezone(Date time, TimeZone timeZone) {

        long timeMs = time.getTime();
        timeMs =  timeMs                                   // server time in millis
                + timeZone.getOffset(timeMs)               // user timezone offset
                - TimeZone.getDefault().getOffset(timeMs); // server timezone offset

        return new Date(timeMs);
    }

    public String getAutorefreshForMeetings() {
        return "" + handler.getAutoRefreshMeetings();
    }

    public String getAutorefreshForRecordings() {
        return "" + handler.getAutoRefreshRecordings();
    }

    public String isRecordingEnabled(){
        return "" + handler.isRecordingEnabled();
    }

    public String isRecordingEditable(){
        return "" + handler.isRecordingEditable();
    }

    public String getRecordingDefault(){
        return "" + handler.isRecordingDefault();
    }

    public String isDurationEnabled(){
        return "" + handler.isDurationEnabled();
    }

    public String getDurationDefault(){
        return "" + handler.getDurationDefault();
    }

    public String isWaitModeratorEnabled(){
        return "" + handler.isWaitModeratorEnabled();
    }

    public String isWaitModeratorEditable(){
        return "" + handler.isWaitModeratorEditable();
    }

    public String getWaitModeratorDefault(){
        return "" + handler.isWaitModeratorDefault();
    }

    public String isMultipleSessionsAllowedEnabled(){
        return "" + handler.isMultipleSessionsAllowedEnabled();
    }

    public String isMultipleSessionsAllowedEditable(){
        return "" + handler.isMultipleSessionsAllowedEditable();
    }

    public String getMultipleSessionsAllowedDefault(){
        return "" + handler.isMultipleSessionsAllowedDefault();
    }

    public String isGroupSessionsEnabled(){
        return "" + handler.isGroupSessionsEnabled();
    }

    public String isGroupSessionsEditable(){
        return "" + handler.isGroupSessionsEditable();
    }

    public String getGroupSessionsDefault(){
        return "" + handler.isGroupSessionsDefault();
    }

    public String isPreuploadPresentationEnabled(){
        return "" + handler.isPreuploadPresentationEnabled();
    }

    public String getMaxLengthForDescription(){
        return "" + handler.getDescriptionMaxLength();
    }

    public String getTextBoxTypeForDescription(){
        return "" + handler.getDescriptionType();
    }

    public boolean databaseStoreMeeting(MeetingTransferBean meetingBean) {

        try {
            meetingRepository.save(meetingBean.toMeeting());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean databaseDeleteMeeting(Meeting meeting) {
        meetingRepository.delete(meeting);
        return false;
    }

    public String getNoticeText() {
        String bbbNoticeText = serverConfigurationService.getString(CFG_NOTICE_TEXT, null);
        if (bbbNoticeText != null && "".equals(bbbNoticeText.trim())) {
            bbbNoticeText = null;
        }
        return bbbNoticeText;
    }

    public String getNoticeLevel() {
        return serverConfigurationService.getString(CFG_NOTICE_LEVEL, "info").trim().toLowerCase();
    }

    public String getJoinUrl(Meeting meeting, User user) throws SecurityException, Exception {

        if (meeting == null) return null;

        MeetingParticipant p = getParticipantFromMeeting(meeting, userDirectoryService.getCurrentUser().getId());

        // Case #1: is participant
        if (getCanParticipate(meeting.getSiteId()) && p != null) {
            // build join url
            String joinURL = handler.getJoinMeetingURL(meeting, user, MeetingRole.MODERATOR == p.getRole());
            return joinURL;
        }

        return null;
    }

    public boolean recordingReady(String meetingId) {

        if (StringUtils.isBlank(meetingId)) {
            log.info("Blank meetingId supplied to recordingReady. Returning false ...");
            return false;
        }

        Optional<Meeting> optMeeting = meetingRepository.findById(meetingId);
        if (!optMeeting.isPresent()) return false;
        notifyParticipants(optMeeting.get(), false, false, 0L, true);
        return true;
    }

    // -----------------------------------------------------------------------
    // --- BBB Private methods -----------------------------------------------
    // -----------------------------------------------------------------------
    private Meeting processMeeting(Meeting meeting) throws SecurityException, Exception {

        if (meeting == null) return null;

        // determine owner name
        if (meeting.getOwnerId() != null) {
            try {
                String ownerDisplayName = userDirectoryService.getUser(meeting.getOwnerId()).getDisplayName();
                meeting.setOwnerDisplayName(ownerDisplayName);
            } catch (UserNotDefinedException e) {
                meeting.setOwnerDisplayName(meeting.getOwnerId());
            }
        }

        // If MultipleSessionsAllowed is not enabled and the Default is set to true, override the meeting value with true.
        // This will enable all the meetings to allow any number of sessions per user
        if(!handler.isMultipleSessionsAllowedEnabled() && handler.isMultipleSessionsAllowedDefault()) {
            meeting.setMultipleSessionsAllowed(Boolean.valueOf(true));
        }

        MeetingParticipant p = getParticipantFromMeeting(meeting, userDirectoryService.getCurrentUser().getId());

        if (getCanParticipate(meeting.getSiteId()) && p != null) {
            // Case #1: is participant
            //meeting.setJoinUrl(null);
            meeting.setJoinUrl(handler.getJoinMeetingURL(meeting, userDirectoryService.getCurrentUser(), true));
            return meeting;
        } else if (getCanEdit(meeting.getSiteId(), meeting) || getCanDelete(meeting.getSiteId(), meeting)) {
            // Case #2: is not a participant but can manage tool
            // reset join url
            meeting.setJoinUrl(null);

            return meeting;
        }

        return null;
    }

    private List<User> getMeetingUsers(Meeting meeting, Site site) {

        List<User> meetingUsers = new ArrayList<>();

        for (MeetingParticipant p : meeting.getParticipants()) {
            switch (p.getSelectionType()) {
                case ALL:
                    for (String userId : site.getUsers()) {
                        try {
                            meetingUsers.add(userDirectoryService.getUser(userId));
                        } catch (UserNotDefinedException e) {
                            log.warn("No user for id {}", userId);
                        }
                    }
                    break;
                case GROUP:
                    Group group = site.getGroup(p.getSelectionId());
                    if (group == null) {
                        log.warn("The group {} was null. Maybe it's been deleted.", p.getSelectionId());
                    } else {
                        for (String userId : group.getUsers()) {
                            try {
                                meetingUsers.add(userDirectoryService.getUser(userId));
                            } catch (UserNotDefinedException e) {
                                log.warn("No user for id {}", userId);
                            }
                        }
                    }
                    break;
                case ROLE:
                    for (String userId : site.getUsersHasRole(p.getSelectionId())) {
                        try {
                            meetingUsers.add(userDirectoryService.getUser(userId));
                        } catch (UserNotDefinedException e) {
                            log.warn("No user for id {}", userId);
                        }
                    }
                    break;
                case USER:
                    String userId = p.getSelectionId();
                    try {
                        meetingUsers.add(userDirectoryService.getUser(userId));
                    } catch (UserNotDefinedException e) {
                        log.warn("No user for id {}", userId);
                    }
                    break;
                default:
                    break;
            }
        }

        return meetingUsers;
    }

    private void notifyParticipants(Meeting meeting, boolean isNewMeeting, boolean iCalAttached, long iCalAlarmMinutes, boolean recordingReady) {
        // Site title, url and directtool (universal) url for joining meeting
        Site site;
        try {
            site = siteService.getSite(meeting.getSiteId());
        } catch (IdUnusedException e) {
            log.error("Unable to send notifications for '{}' meeting participants", meeting.getName());
            return;
        }
        String siteTitle = site.getTitle();
        String directToolJoinUrl = getDirectToolJoinUrl(meeting);
        // Meeting participants
        List<User> meetingUsers = getMeetingUsers(meeting, site);

        ResourceLoader msgs = null;
        // iterate over all user locales found
        log.debug("Sending notifications to {} users", meetingUsers.size());
        for( User user : meetingUsers){
            String userId = user.getId();
            log.debug("User: {}", userId);
            String userLocale = getUserLocale(userId);

            String prefix = "email";

            if (true == isNewMeeting) {
                msgs = new ResourceLoader(userId, "meetings");
            } else if (true == recordingReady){
                msgs = new ResourceLoader(userId, "meetings");
                prefix = "email.recordingready";
            } else {
                msgs = new ResourceLoader(userId, "meetings");
            }

            // Email message
            final String emailTitle = msgs.getFormattedMessage(prefix + ".title", new Object[] { siteTitle, meeting.getName() });
            StringBuilder msg = new StringBuilder();
            msg.append(msgs.getFormattedMessage(prefix + ".header", new Object[] {}));
            msg.append(msgs.getFormattedMessage(prefix + ".body", new Object[] {
                    siteTitle,
                    serverConfigurationService.getString("ui.institution"),
                    directToolJoinUrl, meeting.getName() }));
            String meetingOwnerEid = null;
            try {
                meetingOwnerEid = userDirectoryService.getUserEid(meeting.getOwnerId());
            } catch (UserNotDefinedException e1) {
                meetingOwnerEid = meeting.getOwnerId();
            }

            msg.append(msgs.getFormattedMessage(prefix + ".body.meeting_details",
                new Object[] {
                    meeting.getName(),
                    meeting.getWelcomeMessage(),
                    meeting.getStartDate() == null ? "-" : getTimeInUserTimezone(Date.from(meeting.getStartDate()), userId),
                    meeting.getEndDate() == null ? "-" : getTimeInUserTimezone(Date.from(meeting.getEndDate()), userId),
                    meeting.getOwnerDisplayName() + " (" + meetingOwnerEid + ")" }));

            msg.append(msgs.getFormattedMessage(prefix + ".footer", new Object[] {
                serverConfigurationService.getString("ui.institution"),
                serverConfigurationService.getServerUrl() + "/portal",
                siteTitle }));

            // Generate an ical to attach to email (if, at least, start date is defined)
            String icalFilename = iCalAttached? generateIcalFromMeetingInUserTimezone(meeting, iCalAlarmMinutes, userId): null;
            final File icalFile = icalFilename != null? new File(icalFilename): null;
            if (icalFile != null)
                icalFile.deleteOnExit();

            // Send (a single) email (per userId)!
            final String emailMessage = msg.toString();
            final User emailRecipients = user;
            try {
                new Thread(() -> {
                    if (emailRecipients.getEmail() != null && !emailRecipients.getEmail().trim().equals("")) {
                        EmailMessage email = new EmailMessage();
                        email.setFrom(new EmailAddress("no-reply@"+ serverConfigurationService.getServerName(), serverConfigurationService.getString("ui.institution")));
                        email.setRecipients(RecipientType.TO, Arrays.asList(new EmailAddress(emailRecipients.getEmail(), emailRecipients.getDisplayName())));
                        email.addHeader("Content-Type", "text/html; charset=ISO-8859-1");
                        email.setContentType(ContentType.TEXT_HTML);
                        email.setSubject(emailTitle);
                        email.setBody(emailMessage);
                        //if (icalFile != null && icalFile.canRead()) {
                        //    email.addAttachment(new Attachment(icalFile, "Calendar_Event.ics"));
                        //}
                        try {
                            emailService.send(email);
                        } catch (Exception e) {
                            log.warn("Unable to send email notification to {} about new BBB meeting", emailRecipients.getEmail(), e);
                        }
                    }
                }).start();
            } catch (Exception e) {
                log.error("Unable to send {} notifications for '{}' meeting participants", userLocale, meeting.getName(), e);
            }
        }
    }

    private String getUserLocale(String userId) {

        Preferences prefs = preferencesService.getPreferences(userId);
        ResourceProperties locProps = prefs.getProperties(ResourceLoader.APPLICATION_ID);
        String localeString = locProps.getProperty(ResourceLoader.LOCALE_KEY);
        if (localeString == null) {
            localeString = Locale.getDefault().toString();
        }
        log.debug("Locale for user {} is {}", userId, localeString);
        return localeString;
    }

    private String getDirectToolJoinUrl(Meeting meeting) {
        try {
            Site site = siteService.getSite(meeting.getSiteId());
            StringBuilder url = new StringBuilder();
            url.append(serverConfigurationService.getServerUrl());
            url.append("/portal");
            url.append("/directtool/");
            url.append(site.getToolForCommonId(TOOL_ID).getId());
            url.append("?state=joinMeeting&meetingId=");
            url.append(meeting.getId());
            return url.toString();
        } catch (IdUnusedException e) {
            log.warn("Unable to determine siteId from meeting with id: {}", meeting.getId(), e);
            StringBuilder url = new StringBuilder();
            url.append(serverConfigurationService.getServerUrl());
            url.append("/portal");
            return url.toString();
        }
    }

    private String getMeetingDescription(Meeting meeting) {
        return meeting.getWelcomeMessage().replaceAll("\\<.*?>","");
    }

    @SuppressWarnings("deprecation")
    private boolean addEditCalendarEvent(Meeting meeting) {
        log.debug("addEditCalendarEvent");
        String eventId = meeting.getProperties().get("calendarEventId");
        boolean newEvent = eventId == null;
        try {
            // get CalendarService
            Object calendarService = ComponentManager.get("org.sakaiproject.calendar.api.CalendarService");

            // get site calendar
            String calendarRef = (String) calendarService.getClass().getMethod(
                    "calendarReference",
                    new Class[] { String.class, String.class }).invoke(
                    calendarService,
                    new Object[] { meeting.getSiteId(),
                            SiteService.MAIN_CONTAINER });
            Object calendar = calendarService.getClass().getMethod(
                    "getCalendar", new Class[] { String.class }).invoke(
                    calendarService, new Object[] { calendarRef });

            // build time range (with dates on user timezone - calendar does conversion)

            Time startTime = timeService.newTime(meeting.getStartDate().toEpochMilli());
            TimeRange range = null;
            if (meeting.getEndDate() != null) {
                Time endTime = timeService.newTime(meeting.getEndDate().toEpochMilli());
                range = timeService.newTimeRange(startTime, endTime);
            } else {
                range = timeService.newTimeRange(startTime);
            }

            // add or edit event
            Object event = null;
            if (newEvent) {
                event = calendar.getClass().getMethod("addEvent", new Class[] {}).invoke(calendar, new Object[] {});
                event.getClass().getMethod("setCreator", new Class[] {}).invoke(event, new Object[] {});
                eventId = (String) event.getClass().getMethod("getId", new Class[] {}).invoke(event, new Object[] {});
            } else {
                // EVENT_MODIFY_CALENDAR = "calendar.revise"
                String eventModify = (String) calendarService.getClass().getField("EVENT_MODIFY_CALENDAR").get(null);
                event = calendar.getClass().getMethod("getEditEvent", new Class[] { String.class, String.class }).invoke( calendar, new Object[] { eventId, eventModify });
            }

            // set event fields
            event.getClass().getMethod("setDisplayName", new Class[] { String.class }).invoke(event, new Object[] { meeting.getName() });
            event.getClass().getMethod("setDescription", new Class[] { String.class }).invoke(event, new Object[] { "Meeting '" + meeting.getName() + "' scheduled."/* meeting.getWelcome() */});
            event.getClass().getMethod("setType", new Class[] { String.class }).invoke(event, new Object[] { "Meeting" });
            event.getClass().getMethod("setRange", new Class[] { TimeRange.class }).invoke(event, new Object[] { range });
            event.getClass().getMethod("setModifiedBy", new Class[] {}).invoke(event, new Object[] {});
            event.getClass().getMethod("clearGroupAccess", new Class[] {}).invoke(event, new Object[] {});
            event.getClass().getMethod("setField", new Class[] { String.class, String.class }).invoke(event, new Object[] { "meetingId", meeting.getId() });

            // commit event
            calendar.getClass().getMethod( "commitEvent",
                    new Class[] { Class.forName("org.sakaiproject.calendar.api.CalendarEventEdit") }).invoke(calendar,
                    new Object[] { event });

            // update calendar eventId locally, in DB
            if (newEvent && eventId != null) {
                meeting.getProperties().put("calendarEventId", eventId);
                meetingRepository.save(meeting);
            }
        } catch (Exception e) {
            log.warn("Unable to add event to Calendar (no permissions or site has no Calendar tool).");
            return false;
        }
        return true;
    }

    private boolean removeCalendarEvent(Meeting meeting) {
        log.debug("removeCalendarEvent");
        String eventId = meeting.getProperties().get("calendarEventId");
        if (eventId != null) {
            try {
                // get CalendarService
                Object calendarService = ComponentManager.get("org.sakaiproject.calendar.api.CalendarService");

                // get site calendar
                String calendarRef = (String) calendarService.getClass()
                        .getMethod("calendarReference",
                                new Class[] { String.class, String.class })
                        .invoke(
                                calendarService,
                                new Object[] { meeting.getSiteId(),
                                        SiteService.MAIN_CONTAINER });
                Object calendar = calendarService.getClass().getMethod(
                        "getCalendar", new Class[] { String.class }).invoke(
                        calendarService, new Object[] { calendarRef });

                // remove event
                // EVENT_REMOVE_CALENDAR = "calendar.delete"
                String eventRemove = (String) calendarService.getClass()
                        .getField("EVENT_REMOVE_CALENDAR").get(null);
                Object event = calendar.getClass().getMethod("getEditEvent",
                        new Class[] { String.class, String.class }).invoke(
                        calendar, new Object[] { eventId, eventRemove });

                // commit event
                calendar.getClass().getMethod("removeEvent",
                        new Class[] { Class.forName("org.sakaiproject.calendar.api.CalendarEventEdit") }).invoke(calendar,
                        new Object[] { event });

                meeting.getProperties().remove("calendarEventId");
            } catch (Exception e) {
                log.warn("Unable to remove event from Calendar (no permissions or site has no Calendar tool).", e);
                return false;
            }
        }
        return true;
    }

    private void logEvent(String event, Meeting meeting) {
        eventTrackingService.post(eventTrackingService.newEvent(event, meeting.getId(), meeting.getSiteId(), true, NotificationService.NOTI_OPTIONAL));
    }

    public MeetingParticipant getParticipantFromMeeting(Meeting meeting, String userId) {
        // 1. we want to first check individual user selection as it may
        // override all/group/role selection...
        List<MeetingParticipant> unprocessed1 = new ArrayList<>();
        for (MeetingParticipant p : meeting.getParticipants()) {
            if (SelectionType.USER == p.getSelectionType()) {
                if (userId.equals(p.getSelectionId())) {
                    return p;
                }
            } else {
                unprocessed1.add(p);
            }
        }

        // 2. ... then with group/role selection types...
        String userRole = getUserRoleInSite(userId, meeting.getSiteId());
        List<String> userGroups = getUserGroupIdsInSite(userId, meeting.getSiteId());
        List<MeetingParticipant> unprocessed2 = new ArrayList<>();
        for (MeetingParticipant p : unprocessed1) {
            if (SelectionType.ROLE == p.getSelectionType()) {
                if (userRole != null && userRole.equals(p.getSelectionId())) {
                    return p;
                }
            } else if (SelectionType.GROUP == p.getSelectionType()) {
                if (userGroups.contains(p.getSelectionId())) {
                    return p;
                }
            } else {
                unprocessed2.add(p);
            }
        }

        // 3. ... then go with 'all' selection type
        for (MeetingParticipant p : unprocessed2) {
            if (SelectionType.ALL == p.getSelectionType()) {
                return p;
            }
        }

        // 4. If not found, just check if is superuser
        if (securityService.isSuperUser()) {
            //return new Participant(Participant.SELECTION_USER, "admin", Participant.MODERATOR);
            MeetingParticipant p = new MeetingParticipant();
            p.setSelectionType(SelectionType.USER);
            p.setSelectionId("admin");
            p.setRole(MeetingRole.MODERATOR);
            return p;
        }

        return null;
    }

    private Date convertDateFromCurrentUserTimezone(Date date) {
        if (date == null)
            return null;

        Preferences prefs = preferencesService.getPreferences(userDirectoryService.getCurrentUser().getId());
        TimeZone timeZone = null;
        if (prefs != null) {
            ResourceProperties props = prefs.getProperties(TimeService.APPLICATION_ID);
            String timeZoneStr = props.getProperty(TimeService.TIMEZONE_KEY);
            timeZone = timeZoneStr != null ? TimeZone.getTimeZone(timeZoneStr): TimeZone.getDefault();
        } else {
            timeZone = TimeZone.getDefault();
        }
        long timeMs = date.getTime();
        Date tzDate = new Date(timeMs - timeZone.getOffset(timeMs));

        return tzDate;
    }

    private Date convertDateFromServerTimezone(Date date) {
        if (date == null)
            return null;

        TimeZone timeZone = TimeZone.getDefault();

        long timeMs = date.getTime();
        Date tzDate = new Date(timeMs - timeZone.getOffset(timeMs));

        return tzDate;
    }

    private Date convertDateToUserTimezone(Date date, String userid) {
        if (date == null)
            return null;

        Preferences prefs = preferencesService.getPreferences(userid);
        TimeZone timeZone = null;
        if (prefs != null) {
            ResourceProperties props = prefs.getProperties(TimeService.APPLICATION_ID);
            String timeZoneStr = props.getProperty(TimeService.TIMEZONE_KEY);
            timeZone = timeZoneStr != null ? TimeZone.getTimeZone(timeZoneStr): TimeZone.getDefault();
        } else {
            timeZone = TimeZone.getDefault();
        }
        long timeMs = date.getTime();
        Date tzDate = new Date(timeMs + timeZone.getOffset(timeMs));

        return tzDate;
    }

    private Date convertDateToCurrentUserTimezone(Date date) {
        if (date == null)
            return null;

        Preferences prefs = preferencesService.getPreferences(userDirectoryService.getCurrentUser().getId());
        TimeZone timeZone = null;
        if (prefs != null) {
            ResourceProperties props = prefs.getProperties(TimeService.APPLICATION_ID);
            String timeZoneStr = props.getProperty(TimeService.TIMEZONE_KEY);
            timeZone = timeZoneStr != null ? TimeZone.getTimeZone(timeZoneStr): TimeZone.getDefault();
        } else {
            timeZone = TimeZone.getDefault();
        }
        long timeMs = date.getTime();
        Date tzDate = new Date(timeMs + timeZone.getOffset(timeMs));

        return tzDate;
    }

    private Date convertDateToServerTimezone(Date date) {
        if (date == null)
            return null;

        TimeZone timeZone = TimeZone.getDefault();

        long timeMs = date.getTime();
        Date tzDate = new Date(timeMs + timeZone.getOffset(timeMs));

        return tzDate;
    }

    private Date convertDateToTimezone(Date date, TimeZone timeZone) {
        if (date == null) return null;
        if (timeZone == null)
            timeZone = TimeZone.getDefault();
        long timeMs = date.getTime();
        Date tzDate = new Date(timeMs + timeZone.getOffset(timeMs));

        return tzDate;
    }

    public boolean isUserAllowedInLocation(String userId, String permission, String locationId) {
        if (securityService.isSuperUser()) {
            return true;
        }
        if (locationId != null && !locationId.startsWith(SiteService.REFERENCE_ROOT)) {
            locationId = SiteService.REFERENCE_ROOT + Entity.SEPARATOR + locationId;
        }
        if (securityService.unlock(userId, permission, locationId)) {
            return true;
        }
        return false;
    }

    public String getUserRoleInSite(String userId, String siteId) {
        String userRoleInSite = null;
        if (siteId != null) {
            try {
                Site site = siteService.getSite(siteId);
                if (!securityService.isSuperUser()) {
                    userRoleInSite = site.getUserRole(userId).getId();
                } else {
                    userRoleInSite = site.getMaintainRole();
                }
            } catch (IdUnusedException e) {
                log.error("No such site while resolving user role in site: {}", siteId);
            } catch (Exception e) {
                log.error("Unknown error while resolving user role in site: {}", siteId);
            }
        }
        return userRoleInSite;
    }

    public List<String> getUserGroupIdsInSite(String userId, String siteId) {

        List<String> groupIds = new ArrayList<>();
        if (siteId != null) {
            try {
                Site site = siteService.getSite(siteId);
                Collection<Group> userGroups = site.getGroupsWithMember(userId);
                for (Group g : userGroups)
                    groupIds.add(g.getId());
            } catch (IdUnusedException e) {
                log.error("No such site while resolving user role in site: {}", siteId);
            } catch (Exception e) {
                log.error("Unknown error while resolving user role in site: {}", siteId);
            }
        }
        return groupIds;
    }

    public boolean isRecordingFormatFilterEnabled() {
        return handler.isRecordingFormatFilterEnabled();
    }

    public String getRecordingFormatFilterWhitelist() {
        return "" + handler.getRecordingFormatFilterWhitelist();
    }

    /**
     * Generate an iCal file in tmp dir, an return the file path on the
     * filesystem
     */
    private String generateIcalFromMeeting(Meeting meeting) {
        TimeZone defaultTimezone = TimeZone.getDefault();
        Long iCalAlarmMinutesuserId = 30L;
        return generateIcalFromMeetingInTimeZone(meeting, iCalAlarmMinutesuserId, defaultTimezone);
    }

    private String generateIcalFromMeetingInUserTimezone(Meeting meeting, Long iCalAlarmMinutesuserId, String userId) {

        Preferences prefs = preferencesService.getPreferences(userId);
        TimeZone timeZone = TimeZone.getDefault();
        if (prefs != null) {
            ResourceProperties props = prefs.getProperties(TimeService.APPLICATION_ID);
            String timeZoneStr = props.getProperty(TimeService.TIMEZONE_KEY);
            if( timeZoneStr != null )
                timeZone = TimeZone.getTimeZone(timeZoneStr);
        }

        return generateIcalFromMeetingInTimeZone(meeting, iCalAlarmMinutesuserId, timeZone);
    }

    private String generateIcalFromMeetingInTimeZone(Meeting meeting, Long iCalAlarmMinutesuserId, TimeZone timeZone) {

        Instant startDate = meeting.getStartDate();
        if (startDate == null) {
            return null;
        }
        Instant endDate = meeting.getEndDate();

        String eventName = meeting.getName();

        // Create a TimeZone
        TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
        net.fortuna.ical4j.model.TimeZone timezone = registry.getTimeZone(timeZone.getID());
        VTimeZone tz = timezone.getVTimeZone();

        // Create a reminder
        int minutes = (iCalAlarmMinutesuserId.intValue() % 60);
        int hours = (iCalAlarmMinutesuserId.intValue() / 60 % 24);
        int days = (iCalAlarmMinutesuserId.intValue() / 60 / 24);
        VAlarm vAlarm = new VAlarm(Duration.ofDays(days > 0 ? days * -1 : 0)
                                        .ofHours(hours > 0 ? hours * -1 : 0)
                                        .ofMinutes(minutes > 0 ? minutes * -1 : 0));

        // display a message..
        vAlarm.getProperties().add(Action.DISPLAY);
        vAlarm.getProperties().add(new Description(eventName));

        // Create the event
        VEvent vEvent = null;
        if (endDate != null) {
            DateTime start = new DateTime(startDate.toEpochMilli());
            DateTime end = new DateTime(endDate.toEpochMilli());
            vEvent = new VEvent(start, end, eventName);
        } else {
            DateTime start = new DateTime(startDate.toEpochMilli());
            vEvent = new VEvent(start, eventName);
        }

        // add description & url
        String meetingDescription = getMeetingDescription(meeting);
        String meetingUrl = getDirectToolJoinUrl(meeting);
        try {
            vEvent.getProperties().add(new Description(meetingDescription));
        } catch (Exception e1) {
            // ignore - no harm
        }
        try {
            vEvent.getProperties().add(new Url(new URI(meetingUrl)));
        } catch (Exception e1) {
            // ignore - no harm
        }

        // add timezone info..
        vEvent.getProperties().add(tz.getTimeZoneId());

        // generate unique identifier..
        UidGenerator ug = new RandomUidGenerator();
        Uid uid = ug.generateUid();
        vEvent.getProperties().add(uid);

        // add the reminder
        vEvent.getAlarms().add(vAlarm);

        // create a calendar
        net.fortuna.ical4j.model.Calendar icsCalendar = new net.fortuna.ical4j.model.Calendar();
        icsCalendar.getProperties().add( new ProdId("-//Sakai Calendar//iCal4j 1.0//EN"));
        icsCalendar.getProperties().add(Version.VERSION_2_0);
        icsCalendar.getProperties().add(CalScale.GREGORIAN);

        // add the event
        icsCalendar.getComponents().add(vEvent);

        // log ical, if debug
        log.debug(icsCalendar.toString());

        // output to temp file
        String filename = System.getProperty("java.io.tmpdir")
                + File.separatorChar + meeting.getId() + ".ics";
        try {
            FileOutputStream fout = new FileOutputStream(filename);
            CalendarOutputter outputter = new CalendarOutputter();
            outputter.output(icsCalendar, fout);
        } catch (Exception e) {
            log.warn("Unable to write iCal to file: {}", filename, e);
        }

        return filename;
    }

    //Get participant permissions converted to a User Map
    private Map<String, User> getUsersParticipating(String selectionType, String selectionId, Site site) {
        Map<String, User> response = new HashMap<>();

        if( selectionType.equals(Participant.SELECTION_USER)){
            try{
                User user = userDirectoryService.getUser(selectionId);

                response.put(selectionId, user);

            } catch (Exception e) {
                log.error("Failed on getUser() for {}", selectionId, e);
            }

        } else if( selectionType.equals(Participant.SELECTION_ROLE)){

            Set users = getSiteUsersInRole(site, selectionId);
            Iterator<String> usersIter = users.iterator();

            while ( usersIter.hasNext() ){
                String userId = usersIter.next();
                try{
                    User user = userDirectoryService.getUser(userId);
                    response.put(userId, user);

                } catch (Exception e) {
                    log.error("Failed on getUser() for {}", selectionId, e);
                }
            }

        }

        return response;

    }

    //Get users in a role for an specific site
    private Set getSiteUsersInRole(Site site, String role) {
        Set users = null;

        try{
            String siteReference = site.getReference();
            AuthzGroup authzGroup = authzGroupService.getAuthzGroup(siteReference);
            users = site.getUsersHasRole(role);

        } catch (Exception e) {
            log.error("Failed on getAuthzGroup() for {}", role, e);

        }

        return users;
    }


    /** Inner class to execute code as Sakai Administrator */
    abstract class AdminExecution {
        public AdminExecution() {};

        public abstract Object execution() throws Exception;

        public Object execute() throws Exception {
            Object returnObject = null;
            Session sakaiSession = sessionManager.getCurrentSession();
            String currentUserId = sakaiSession.getUserId();
            String currentUserEid = sakaiSession.getUserEid();
            if (!"admin".equals(currentUserId)) {
                // current user not admin
                try {
                    sakaiSession.setUserId("admin");
                    sakaiSession.setUserEid("admin");
                    authzGroupService.refreshUser("admin");

                    returnObject = execution();
                } catch (Exception e) {
                    throw e;
                } finally {
                    sakaiSession.setUserId(currentUserId);
                    sakaiSession.setUserEid(currentUserEid);
                    authzGroupService.refreshUser(currentUserId);
                }

            } else {
                // current user is admin
                try {
                    returnObject = execution();
                } catch (Exception e) {
                    throw e;
                }
            }
            return returnObject;
        }
    }

    private String generatePassword() {
        return Long.toHexString(new Random(System.currentTimeMillis()).nextLong());
    }

    private MeetingTransferBean decorateMeetingBean(MeetingTransferBean meetingBean, Site site) {

        String currentUserId = sessionManager.getCurrentSessionUserId();

        String siteReference = "/site/" + meetingBean.siteId;

        if (currentUserId.equals(meetingBean.ownerId)) {
            meetingBean.canEdit = securityService.unlock(MeetingsService.FN_EDIT_OWN, siteReference)
                    || securityService.unlock(MeetingsService.FN_EDIT_ANY, siteReference);
            meetingBean.canEnd = (securityService.unlock(MeetingsService.FN_EDIT_OWN, siteReference)
                    || securityService.unlock(MeetingsService.FN_EDIT_ANY, siteReference))
                        && (securityService.unlock(MeetingsService.FN_DELETE_OWN, siteReference)
                            || securityService.unlock(MeetingsService.FN_DELETE_ANY, siteReference));
            meetingBean.canDelete = securityService.unlock(MeetingsService.FN_DELETE_OWN, siteReference)
                            || securityService.unlock(MeetingsService.FN_DELETE_ANY, siteReference);
        } else {
            meetingBean.canEdit = securityService.unlock(MeetingsService.FN_EDIT_ANY, siteReference);
            meetingBean.canEnd = securityService.unlock(MeetingsService.FN_EDIT_ANY, siteReference) && securityService.unlock(MeetingsService.FN_DELETE_ANY, siteReference);
            meetingBean.canDelete = securityService.unlock(MeetingsService.FN_DELETE_ANY, siteReference);
        }

        meetingBean.canJoin = securityService.unlock(MeetingsService.FN_PARTICIPATE, siteReference);

        if (meetingBean.startDate != null) {
            meetingBean.formattedStartDate = userTimeService.dateTimeFormat(meetingBean.startDate.toInstant(), FormatStyle.MEDIUM, FormatStyle.SHORT);
        }

        if (meetingBean.endDate != null) {
            meetingBean.formattedEndDate = userTimeService.dateTimeFormat(meetingBean.endDate.toInstant(), FormatStyle.MEDIUM, FormatStyle.SHORT);
        }

        if (meetingBean.startDate != null) {
            LocalDate today = LocalDate.now();
            LocalDateTime start = today.atStartOfDay();
            LocalDateTime end = today.plusDays(1).atStartOfDay();
            LocalDateTime localStartTime = LocalDateTime.ofInstant(meetingBean.startDate.toInstant(), ZoneId.of(userTimeService.getLocalTimeZone().getID()));
            meetingBean.startingToday = localStartTime.isAfter(start) && localStartTime.isBefore(end);
        }

        meetingBean.participants.forEach(mp -> {

            switch (mp.selectionType) {
                case USER:
                    try {
                        User user = userDirectoryService.getUser(mp.selectionId);
                        mp.displayString = user.getDisplayName() + " (" + user.getEid() + ")";
                    } catch (UserNotDefinedException unde) {
                    }
                    break;
                case GROUP:
                    Group group = site.getGroup(mp.selectionId);
                    mp.displayString = group.getTitle();
                    break;
                case ROLE:
                    mp.displayString = mp.selectionId;
                    break;
                default:

            }
        });

        return meetingBean;
    }
}
