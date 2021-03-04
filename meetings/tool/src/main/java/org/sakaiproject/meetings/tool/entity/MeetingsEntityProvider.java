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

package org.sakaiproject.meetings.tool.entity;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import java.time.Instant;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.codec.binary.Base64;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.meetings.api.MeetingsException;
import org.sakaiproject.meetings.api.MeetingRole;
import org.sakaiproject.meetings.api.MeetingsService;
import org.sakaiproject.meetings.api.Participant;
import org.sakaiproject.meetings.api.SelectionType;
import org.sakaiproject.meetings.api.beans.MeetingTransferBean;
import org.sakaiproject.meetings.api.persistence.Meeting;
import org.sakaiproject.meetings.api.persistence.MeetingParticipant;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Createable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Deleteable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Statisticable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Updateable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * MeetingsEntityProvider is the EntityProvider class that implements several
 * EntityBroker capabilities.
 *
 * @author Adrian Fish, Nuno Fernandes
 */
@Slf4j
@Setter
public class MeetingsEntityProvider extends AbstractEntityProvider implements
        CoreEntityProvider, AutoRegisterEntityProvider, Inputable, Outputable,
        Createable, Updateable, Describeable, Deleteable,
        ActionsExecutable, Statisticable {

    private MeetingsService meetingManager;
    private UserDirectoryService userDirectoryService;
    private SiteService siteService;
    private IdManager idManager;
    private ServerConfigurationService serverConfigurationService;
    private ContentHostingService contentHostingService;
    private SecurityService securityService;

    public String[] getHandledOutputFormats() {
        return new String[] { Formats.HTML, Formats.JSON, Formats.TXT };
    }

    public String[] getHandledInputFormats() {
        return new String[] { Formats.HTML, Formats.JSON, Formats.FORM };
    }

    public String getEntityPrefix() {
        return MeetingsService.ENTITY_PREFIX;
    }

    public Object getEntity(EntityReference reference) {
        return null;
    }

    public boolean entityExists(String id) {

        log.debug("entityExists(" + id + ")");

        if (id == null || "".equals(id))
            return false;

        try {
            return (meetingManager.getMeeting(id, null) != null);
        } catch (SecurityException se) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // --- CRUDable
    // ------------------------------------------------------------------
    public Object getSampleEntity() {
        return new Meeting();
    }

    public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {

        log.debug("createMeeting");

        log.debug("EntityReference:" + ref.toString() + ", Entity:" + entity.toString() + ", params:" + params.toString());

        Meeting meeting = (Meeting) entity;

        // generate uuid
        meeting.setId(idManager.createUuid());

        // owner
        meeting.setOwnerId(userDirectoryService.getCurrentUser().getId());
        meeting.setOwnerDisplayName(userDirectoryService.getCurrentUser().getDisplayName());

        // recording flag
        String recordingStr = (String) params.get("recording");
        boolean recording = (recordingStr != null &&
                (recordingStr.toLowerCase().equals("on") || recordingStr.toLowerCase().equals("true")));
        meeting.setRecording(recording ? Boolean.TRUE : Boolean.FALSE);

        // waitForModerator flag
        String waitForModeratorStr = (String) params.get("waitForModerator");
        boolean waitForModerator = (waitForModeratorStr != null &&
                (waitForModeratorStr.toLowerCase().equals("on") || waitForModeratorStr.toLowerCase().equals("true")));
        meeting.setWaitForModerator(Boolean.valueOf(waitForModerator));

        // multipleSessionsAllowed flag
        String multipleSessionsAllowedStr = (String) params.get("multipleSessionsAllowed");
        boolean multipleSessionsAllowed = (multipleSessionsAllowedStr != null &&
                (multipleSessionsAllowedStr.toLowerCase().equals("on") || multipleSessionsAllowedStr.toLowerCase().equals("true")));
        meeting.setMultipleSessionsAllowed(Boolean.valueOf(multipleSessionsAllowed));

        //preuploaded presentation
        String presentationUrl = (String) params.get("presentation");
        meeting.setPresentation(presentationUrl);

        // participants
        String meetingOwnerId = meeting.getOwnerId();
        List<MeetingParticipant> participants = extractParticipants(params, meetingOwnerId);
        participants.forEach(p -> p.setMeeting(meeting));
        meeting.setParticipants(participants);

        // store meeting
        String addToCalendarStr = (String) params.get("addToCalendar");
        String notifyParticipantsStr = (String) params.get("notifyParticipants");
        String iCalAttachedStr = (String) params.get("iCalAttached");
        String iCalAlarmMinutesStr = (String) params.get("iCalAlarmMinutes");
        boolean addToCalendar = addToCalendarStr != null
                && (addToCalendarStr.toLowerCase().equals("on") || addToCalendarStr.toLowerCase().equals("true"));
        boolean notifyParticipants = notifyParticipantsStr != null
                && (notifyParticipantsStr.toLowerCase().equals("on") || notifyParticipantsStr.toLowerCase().equals("true"));
        boolean iCalAttached = iCalAttachedStr != null
                && (iCalAttachedStr.toLowerCase().equals("on") || iCalAttachedStr.toLowerCase().equals("true"));
        Long iCalAlarmMinutes = iCalAlarmMinutesStr != null? Long.valueOf(iCalAlarmMinutesStr): 0L;

        // generate differentiated passwords
        meeting.setAttendeePassword(generatePassword());
        do {
            meeting.setModeratorPassword(generatePassword());
        } while (meeting.getAttendeePassword().equals(
                meeting.getModeratorPassword()));

        // generate voiceBidge
        String voiceBridgeStr = (String) params.get("voiceBridge");
        log.debug("voiceBridgeStr:" + voiceBridgeStr);
        if (voiceBridgeStr == null || voiceBridgeStr.equals("")
                || Integer.parseInt(voiceBridgeStr) == 0) {
            Integer voiceBridge = 70000 + new Random().nextInt(10000);
            meeting.setVoiceBridge(voiceBridge);
        } else {
            meeting.setVoiceBridge(Integer.valueOf(voiceBridgeStr));
        }

        try {
            return meetingManager.saveMeeting(MeetingTransferBean.of(meeting), false).id;
        } catch (Exception e) {
            throw new EntityException(e.getMessage(), meeting.getId(), 400);
        }
    }

    public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {

        log.debug("updateMeeting");

        Meeting newMeeting = (Meeting) entity;

        try {
            Optional<MeetingTransferBean> optMeeting = meetingManager.getMeeting(ref.getId(), null);
            if (!optMeeting.isPresent()) {
                throw new IllegalArgumentException("Could not locate meeting to update");
            }
            Meeting meeting = optMeeting.get().toMeeting();
            // update name
            String nameStr = (String) params.get("name");
            nameStr = StringEscapeUtils.escapeHtml(nameStr);
            if (nameStr != null) {
                meeting.setName(nameStr);
            }

            // update description
            String welcomeMessageStr = (String) params.get("properties.welcomeMessage");
            if (welcomeMessageStr != null) {
                meeting.getProperties().put("welcomeMessage", welcomeMessageStr);
            }

            // update recording flag
            String recordingStr = (String) params.get("recording");
            boolean recording = (recordingStr != null &&
                    (recordingStr.toLowerCase().equals("on") || recordingStr.toLowerCase().equals("true")));
            meeting.setRecording(Boolean.valueOf(recording));

            // update recordingDuration
            String recordingDurationStr = (String) params.get("recordingDuration");
            if (recordingDurationStr != null)
                meeting.setRecordingDuration(Long.valueOf(recordingDurationStr));
            else
                meeting.setRecordingDuration(0L);

            // update voiceBridge only if the voiceBridge parameter is sent from
            // the view to the controller
            String voiceBridgeStr = (String) params.get("voiceBridge");
            if (voiceBridgeStr != null) {
                if (voiceBridgeStr.equals("")
                        || Integer.parseInt(voiceBridgeStr) == 0) {
                    Integer voiceBridge = 70000 + new Random().nextInt(10000);
                    meeting.setVoiceBridge(voiceBridge);
                } else {
                    meeting.setVoiceBridge(Integer.valueOf(voiceBridgeStr));
                }
            }

            // update waitForModerator flag
            String waitForModeratorStr = (String) params.get("waitForModerator");
            boolean waitForModerator = (waitForModeratorStr != null &&
                    (waitForModeratorStr.toLowerCase().equals("on") || waitForModeratorStr.toLowerCase().equals("true")));
            meeting.setWaitForModerator(Boolean.valueOf(waitForModerator));

            // update multipleSessionsAllowed flag
            String multipleSessionsAllowedStr = (String) params.get("multipleSessionsAllowed");
            boolean multipleSessionsAllowed = (multipleSessionsAllowedStr != null &&
                    (multipleSessionsAllowedStr.toLowerCase().equals("on") || multipleSessionsAllowedStr.toLowerCase().equals("true")));
            meeting.setMultipleSessionsAllowed(Boolean.valueOf(multipleSessionsAllowed));

            // update default presentation if preuploadPresentation flag is true
            String presentationUrl = (String) params.get("presentation");
            if (presentationUrl != null && presentationUrl != "") {
                meeting.setPresentation(presentationUrl);
            } else {
                meeting.setPresentation("");
            }

            // update dates
            if (params.get("startDate") != null)
                meeting.setStartDate(newMeeting.getStartDate());
            else
                meeting.setStartDate(null);
            if (params.get("endDate") != null)
                meeting.setEndDate(newMeeting.getEndDate());
            else
                meeting.setEndDate(null);

            // update participants
            String meetingOwnerId = meeting.getOwnerId();
            List<MeetingParticipant> participants = extractParticipants(params, meetingOwnerId);
            meeting.setParticipants(participants);

            // store meeting
            String addToCalendarStr = (String) params.get("addToCalendar");
            String notifyParticipantsStr = (String) params.get("notifyParticipants");
            String iCalAttachedStr = (String) params.get("iCalAttached");
            String iCalAlarmMinutesStr = (String) params.get("iCalAlarmMinutes");
            boolean addToCalendar = addToCalendarStr != null
                    && (addToCalendarStr.toLowerCase().equals("on") || addToCalendarStr.toLowerCase().equals("true"));
            boolean notifyParticipants = notifyParticipantsStr != null
                    && (notifyParticipantsStr.toLowerCase().equals("on") || notifyParticipantsStr.toLowerCase().equals("true"));
            boolean iCalAttached = iCalAttachedStr != null
                    && (iCalAttachedStr.toLowerCase().equals("on") || iCalAttachedStr.toLowerCase().equals("true"));
            Long iCalAlarmMinutes = iCalAlarmMinutesStr != null? Long.valueOf(iCalAlarmMinutesStr): 0L;

            try {
                meetingManager.saveMeeting(MeetingTransferBean.of(meeting), false);
            } catch (MeetingsException e) {
                throw new EntityException(e.getPrettyMessage(), meeting.getId(), 400);
            }
        } catch (SecurityException se) {
            throw new EntityException(se.getMessage(), ref.getReference(), 400);
        } catch (Exception e) {
            throw new EntityException(e.getMessage(), ref.getReference(), 400);
        }
    }

    public void deleteEntity(EntityReference ref, Map<String, Object> params) {

        log.debug("deleteEntity");

        if (ref == null) {
            throw new EntityNotFoundException("Meeting not found", null);
        }

        try {
            meetingManager.deleteMeeting(ref.getId());
        } catch (Exception e) {
            throw new EntityException(e.getMessage(), ref.getReference(), 400);
        }
    }

    private Map<String, Object>getCurrentUser(User user) {

        Map<String, Object> currentUser = new LinkedHashMap<>();
        currentUser.put("id", user.getId());
        currentUser.put("displayId", user.getDisplayId());
        currentUser.put("displayName", user.getDisplayName());
        currentUser.put("eid", user.getEid());
        currentUser.put("email", user.getEmail());
        return currentUser;
    }

    private List<String> getUserPermissionsInSite(String userId, String siteId) {

        List<String> permissions = new ArrayList<>();
        if(meetingManager.isUserAllowedInLocation(userId, "site.viewRoster", siteId)) {
            permissions.add("site.viewRoster");
        }
        if(meetingManager.isUserAllowedInLocation(userId, "site.upd", siteId)) {
            permissions.add("site.upd");
            permissions.add(meetingManager.FN_ADMIN);
        }
        if( meetingManager.isUserAllowedInLocation(userId, meetingManager.FN_CREATE, siteId) )
            permissions.add(meetingManager.FN_CREATE);
        if( meetingManager.isUserAllowedInLocation(userId, meetingManager.FN_EDIT_OWN, siteId) )
            permissions.add(meetingManager.FN_EDIT_OWN);
        if( meetingManager.isUserAllowedInLocation(userId, meetingManager.FN_EDIT_ANY, siteId) )
            permissions.add(meetingManager.FN_EDIT_ANY);
        if( meetingManager.isUserAllowedInLocation(userId, meetingManager.FN_DELETE_OWN, siteId) )
            permissions.add(meetingManager.FN_DELETE_OWN);
        if( meetingManager.isUserAllowedInLocation(userId, meetingManager.FN_DELETE_ANY, siteId) )
            permissions.add(meetingManager.FN_DELETE_ANY);
        if( meetingManager.isUserAllowedInLocation(userId, meetingManager.FN_PARTICIPATE, siteId) )
            permissions.add(meetingManager.FN_PARTICIPATE);
        if( meetingManager.isUserAllowedInLocation(userId, meetingManager.FN_RECORDING_VIEW, siteId) )
            permissions.add(meetingManager.FN_RECORDING_VIEW);
        if( meetingManager.isUserAllowedInLocation(userId, meetingManager.FN_RECORDING_EDIT_OWN, siteId) )
            permissions.add(meetingManager.FN_RECORDING_EDIT_OWN);
        if( meetingManager.isUserAllowedInLocation(userId, meetingManager.FN_RECORDING_EDIT_ANY, siteId) )
            permissions.add(meetingManager.FN_RECORDING_EDIT_ANY);
        if( meetingManager.isUserAllowedInLocation(userId, meetingManager.FN_RECORDING_DELETE_OWN, siteId) )
            permissions.add(meetingManager.FN_RECORDING_DELETE_OWN);
        if( meetingManager.isUserAllowedInLocation(userId, meetingManager.FN_RECORDING_DELETE_ANY, siteId) )
            permissions.add(meetingManager.FN_RECORDING_DELETE_ANY);
        if( meetingManager.isUserAllowedInLocation(userId, meetingManager.FN_RECORDING_EXTENDEDFORMATS_OWN, siteId) )
            permissions.add(meetingManager.FN_RECORDING_EXTENDEDFORMATS_OWN);
        if( meetingManager.isUserAllowedInLocation(userId, meetingManager.FN_RECORDING_EXTENDEDFORMATS_ANY, siteId) )
            permissions.add(meetingManager.FN_RECORDING_EXTENDEDFORMATS_ANY);
        if( meetingManager.isUserAllowedInLocation(userId, "calendar.new", siteId) )
            permissions.add("calendar.new");
        if( meetingManager.isUserAllowedInLocation(userId, "calendar.revise.own", siteId) )
            permissions.add("calendar.revise.own");
        if( meetingManager.isUserAllowedInLocation(userId, "calendar.revise.any", siteId) )
            permissions.add("calendar.revise.any");
        if( meetingManager.isUserAllowedInLocation(userId, "calendar.delete.own", siteId) )
            permissions.add("calendar.delete.own");
        if( meetingManager.isUserAllowedInLocation(userId, "calendar.delete.any", siteId) )
            permissions.add("calendar.delete.any");
        return permissions;
    }

    private Map<String, String> getAutorefreshInterval() {

        Map<String, String> interval = new LinkedHashMap<>();
        String autorefreshMeetings = meetingManager.getAutorefreshForMeetings();
        if (autorefreshMeetings != null) {
            interval.put("meetings", autorefreshMeetings);
        }
        String autorefreshRecordings = meetingManager.getAutorefreshForRecordings();
        if (autorefreshRecordings != null) {
            interval.put("recordings", autorefreshRecordings);
        }
        return interval;
    }

    @EntityCustomAction(viewKey = EntityView.VIEW_LIST)
    public String isMeetingRunning(Map<String, Object> params) {

        log.debug("isMeetingRunning");

        String meetingID = (String) params.get("meetingID");
        if (meetingID == null) {
            throw new IllegalArgumentException("Missing required parameters meetingId");
        }

        try {
            return Boolean.toString(meetingManager.isMeetingRunning(meetingID));
        } catch (MeetingsException e) {
            String ref = Entity.SEPARATOR + MeetingsService.ENTITY_PREFIX + Entity.SEPARATOR + meetingID;
            throw new EntityException(e.getPrettyMessage(), ref, 400);
        }
    }

    @EntityCustomAction(viewKey = EntityView.VIEW_SHOW)
    public ActionReturn getRecordings(EntityReference ref, Map<String, Object> params) {

        log.debug("getRecordings");

        if (ref == null) {
            throw new EntityNotFoundException("Meeting not found", null);
        }

        String groupId = (String) params.get("groupId");
        String siteId = (String) params.get("siteId");
        try {
            Map<String, Object> recordingsResponse = meetingManager.getRecordings(ref.getId(), siteId);
            return new ActionReturn(recordingsResponse);
        } catch (Exception e) {
            return new ActionReturn(new HashMap<String, String>());
        }
    }

    @EntityCustomAction(viewKey = EntityView.VIEW_LIST)
    public ActionReturn getSiteRecordings(Map<String, Object> params) {

        log.debug("getSiteRecordings");

        String siteId = (String) params.get("siteId");

        if (!meetingManager.getCanViewSiteRecordings(siteId)) {
            throw new SecurityException("You are not allowed to view recordings");
        }

        try {
            Map<String, Object> recordingsResponse = meetingManager.getSiteRecordings(siteId);
            return new ActionReturn(recordingsResponse);
        } catch (Exception e) {
            log.error("Failed to retrieve site recordings", e);
            return new ActionReturn(new HashMap<String, String>());
        }
    }

    @EntityCustomAction(viewKey = EntityView.VIEW_LIST)
    public String publishRecordings(Map<String, Object> params) {

        log.debug("publishRecordings");

        String recordID = (String) params.get("recordID");
        if (recordID == null) {
            throw new IllegalArgumentException("Missing required parameter [recordID]");
        }
        String publish = (String) params.get("publish");
        if (publish == null) {
            throw new IllegalArgumentException("Missing required parameter [publish]");
        }

        try {
            return Boolean.toString(meetingManager.publishRecordings(recordID, publish));
        } catch (MeetingsException e) {
            String ref = Entity.SEPARATOR + MeetingsService.ENTITY_PREFIX + Entity.SEPARATOR + recordID;
            throw new EntityException(e.getPrettyMessage(), ref, 400);
        }
    }

    @EntityCustomAction(viewKey = EntityView.VIEW_LIST)
    public String protectRecordings(Map<String, Object> params) {

        log.debug("protectRecordings");

        String recordID = (String) params.get("recordID");
        if (recordID == null) {
            throw new IllegalArgumentException("Missing required parameter [recordID]");
        }
        String protect = (String) params.get("protect");
        if (protect == null) {
            throw new IllegalArgumentException("Missing required parameter [protect]");
        }

        try {
            return Boolean.toString(meetingManager.protectRecordings(recordID, protect));
        } catch (MeetingsException e) {
            String ref = Entity.SEPARATOR + MeetingsService.ENTITY_PREFIX
                    + Entity.SEPARATOR + recordID;
            throw new EntityException(e.getPrettyMessage(), ref, 400);
        }
    }

    @EntityCustomAction(viewKey = EntityView.VIEW_LIST)
    public String deleteRecordings(Map<String, Object> params) {

        log.debug("deleteRecordings");

        String recordID = (String) params.get("recordID");
        if (recordID == null) {
            throw new IllegalArgumentException("Missing required parameter [recordID]");
        }

        try {
            return Boolean.toString(meetingManager.deleteRecordings(recordID));
        } catch (MeetingsException e) {
            String ref = Entity.SEPARATOR + MeetingsService.ENTITY_PREFIX
                    + Entity.SEPARATOR + recordID;
            throw new EntityException(e.getPrettyMessage(), ref, 400);
        }
    }

    @EntityCustomAction(viewKey = EntityView.VIEW_LIST)
    public ActionReturn getGroups(Map<String, Object> params) {

        log.debug("getGroups");

        String meetingID = (String) params.get("meetingID");
        if (meetingID == null) {
            throw new IllegalArgumentException("Missing required parameter [meetingID]");
        }

        Meeting meeting = null;
        try {
            Optional<MeetingTransferBean> optMeeting = meetingManager.getMeeting(meetingID, null);
            if (!optMeeting.isPresent()) {
                return null;
            }
            meeting = optMeeting.get().toMeeting();
        } catch (Exception e) {
            log.error("Failed to get meeting for id {}", meetingID, e);
        }

        Site site;
        try {
            site = siteService.getSite(meeting.getSiteId());
        } catch (IdUnusedException e) {
            log.error("Unable to get groups in '" + meeting.getName() + "'.", e);
            return null;
        }

        //Get user's group ids
        List<String> groupIds = new ArrayList<String>();
        if (meetingManager.getCanEdit(meeting.getSiteId(), meeting)) {
            for(Group g : site.getGroups())
                groupIds.add(g.getId());
        } else {
            groupIds = meetingManager.getUserGroupIdsInSite(userDirectoryService.getCurrentUser().getId(), meeting.getSiteId());
        }

        Map<String, Object> groups = new HashMap<String, Object>();

        for(int i = 0; i < groupIds.size(); i++){
            Map<String, String> groupInfo = new HashMap<String, String>();
            Group group = site.getGroup(groupIds.get(i));

            groupInfo.put("groupId", groupIds.get(i));
            groupInfo.put("groupTitle", group.getTitle());
            groups.put("group" + i, groupInfo);
        }

        return new ActionReturn(groups);
    }

    @EntityCustomAction(viewKey = EntityView.VIEW_LIST)
    public ActionReturn getUserSelectionOptions(Map<String, Object> params) {

        log.debug("getUserSelectionOptions");

        String siteId = (String) params.get("siteId");
        if (siteId == null) {
            throw new IllegalArgumentException("Missing required parameter siteId");
        }

        try {
            Map<String, Object> map = new HashMap<String, Object>();
            Site site = siteService.getSite(siteId);

            // groups
            List<Map<String, String>> groups = new ArrayList<Map<String, String>>();
            for (Group g : site.getGroups()) {
                Map<String, String> m = new HashMap<String, String>();
                m.put("id", g.getId());
                m.put("title", g.getTitle());
                groups.add(m);
            }
            map.put("groups", groups);

            // roles
            List<Map<String, String>> roles = new ArrayList<Map<String, String>>();
            for (Role r : site.getRoles()) {
                Map<String, String> m = new HashMap<String, String>();
                m.put("id", r.getId());
                m.put("title", r.getId());
                roles.add(m);
            }
            map.put("roles", roles);

            // users
            List<Map<String, String>> users = new ArrayList<Map<String, String>>();
            for (Member u : site.getMembers()) {
                String displayName = null;
                try {
                    displayName = userDirectoryService.getUser(u.getUserId())
                            .getDisplayName();
                } catch (UserNotDefinedException e1) {
                    log.warn("Could not retrieve displayName for userId: " + u.getUserId());
                }

                if (displayName != null) {
                    Map<String, String> m = new HashMap<String, String>();
                    m.put("id", u.getUserId());
                    m.put("title", displayName + " (" + u.getUserDisplayId() + ")");
                    users.add(m);
                }
            }
            map.put("users", users);

            // defaults
            Map<String, String> dlfts = new HashMap<String, String>();
            dlfts.put(MeetingsService.CFG_DEFAULT_ALLUSERS,
                    serverConfigurationService.getString(MeetingsService.CFG_DEFAULT_ALLUSERS, "true").toLowerCase());
            dlfts.put(MeetingsService.CFG_DEFAULT_OWNER,
                    serverConfigurationService.getString(MeetingsService.CFG_DEFAULT_OWNER, "moderator").toLowerCase());
            map.put("defaults", dlfts);

            return new ActionReturn(map);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @EntityCustomAction(viewKey = EntityView.VIEW_LIST)
    public ActionReturn getNoticeText(Map<String, Object> params) {

        log.debug("getNoticeText");

        Map<String, String> map = new HashMap<String, String>();
        String noticeText = meetingManager.getNoticeText();
        if (noticeText != null) {
            map.put("text", noticeText);
            map.put("level", meetingManager.getNoticeLevel());
        }
        return new ActionReturn(map);
    }

    @EntityCustomAction(viewKey = EntityView.VIEW_NEW)
    public ActionReturn recordingReady(Map<String, Object> params) {
        String bbbSaltString = serverConfigurationService.getString(MeetingsService.CFG_SALT);
        String bbbSalt = new String(Base64.encodeBase64(bbbSaltString.getBytes()));
        Claims claims = Jwts.parser().setSigningKey(bbbSalt).parseClaimsJws(params.get("signed_parameters").toString()).getBody();
        String meeting_id = claims.get("meeting_id").toString();

        boolean notified = meetingManager.recordingReady(meeting_id);

        ActionReturn response = null;
        if (notified) {
            response = new ActionReturn("OK");
            response.setResponseCode(200);
        } else {
            response = new ActionReturn("Gone");
            response.setResponseCode(410);
        }
        return response;
    }

    @EntityCustomAction(viewKey = EntityView.VIEW_NEW)
    public String doUpload(Map<String, Object> params) {

        log.debug("Uploading File");

        String url = "";
        String siteId = (String) params.get("siteId");
        FileItem file = (FileItem) params.get("file");

        try {
            String filename = Validator.getFileName(file.getName());
            String contentType = file.getContentType();

            InputStream fileContentStream = file.getInputStream();
            InputStreamReader reader = new InputStreamReader(fileContentStream);

            if (fileContentStream != null) {
                String name = Validator.getFileName(filename);
                String resourceId = Validator.escapeResourceName(name);

                ResourcePropertiesEdit props = contentHostingService.newResourceProperties();
                props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
                props.addProperty(ResourceProperties.PROP_DESCRIPTION, filename);
                SecurityAdvisor sa = uploadFileSecurityAdvisor();
                try {
                    if (siteId != null) {
                        ContentResource attachment = contentHostingService.addAttachmentResource(resourceId, siteId, "Meetings", contentType, fileContentStream, props);
                        securityService.pushAdvisor(sa);
                        // Make sure the resource is closed to public.
                        contentHostingService.setPubView(attachment.getId(), false);
                        url = attachment.getUrl();
                    } else {
                        log.debug("Upload failed; Site not found");
                    }
                } catch(IdInvalidException e) {
                    log.debug(e.getMessage());
                } catch(InconsistentException e) {
                    log.debug(e.getMessage());
                } catch(IdUsedException e) {
                    log.debug(e.getMessage());
                } catch(PermissionException e) {
                    log.debug(e.getMessage());
                } catch(OverQuotaException e) {
                    log.debug(e.getMessage());
                } catch(ServerOverloadException e) {
                    log.debug(e.getMessage());
                }
            }
        } catch(IOException e) {
            log.debug("Failed to upload file");
            log.debug(e.getMessage());
        }
        return url;
    }

    private SecurityAdvisor uploadFileSecurityAdvisor() {
        return (userId, function, reference) -> {
            //Needed to be able to add or modify their own
            if (function.equals(contentHostingService.AUTH_RESOURCE_ADD) ||
              function.equals(contentHostingService.AUTH_RESOURCE_WRITE_OWN) ||
              function.equals(contentHostingService.AUTH_RESOURCE_HIDDEN) ) {
                return SecurityAdvisor.SecurityAdvice.ALLOWED;
            } else if (function.equals(contentHostingService.AUTH_RESOURCE_WRITE_ANY)) {
                log.info(userId + " requested ability to write to any content on " + reference +
                    " which we didn't expect, this should be investigated");
                return SecurityAdvisor.SecurityAdvice.ALLOWED;
            }
                  return SecurityAdvisor.SecurityAdvice.PASS;
        };
    }

    @EntityCustomAction(viewKey = EntityView.VIEW_LIST)
    public String removeUpload(Map<String, Object> params) {

        log.debug("Removing File");

        String resourceId = (String) params.get("url");
        String meetingId = (String) params.get("meetingId");

        SecurityAdvisor sa = removeUploadSecurityAdvisor();
        try {
            securityService.pushAdvisor(sa);
            contentHostingService.removeResource(resourceId);
        } catch (PermissionException e) {
            log.debug(e.getMessage());
            return Boolean.toString(false);
        } catch (IdUnusedException e) {
            log.debug(e.getMessage());
            return Boolean.toString(false);
        } catch (TypeException e) {
            log.debug(e.getMessage());
            return Boolean.toString(false);
        } catch (InUseException e) {
            log.debug(e.getMessage());
            return Boolean.toString(false);
        }

        if (meetingId != null && meetingId != "") {
            try {
                Optional<MeetingTransferBean> optMeeting = meetingManager.getMeeting(meetingId, null);
                if (optMeeting.isPresent()) {
                    Meeting meeting = optMeeting.get().toMeeting();

                    meeting.setPresentation("");
                    try {
                        //Update meeting presentation value
                        meetingManager.saveMeeting(MeetingTransferBean.of(meeting), true);
                    } catch (MeetingsException e) {
                        throw new EntityException(e.getPrettyMessage(), meeting.getId(), 400);
                    }
                }
            } catch (SecurityException se) {
                log.debug(se.getMessage());
                return Boolean.toString(false);
            } catch (Exception e) {
                log.debug(e.getMessage());
                return Boolean.toString(false);
            }
        }
        return Boolean.toString(true);
    }

    private SecurityAdvisor removeUploadSecurityAdvisor() {
        return (userId, function, reference) -> {
            if (function.equals(contentHostingService.AUTH_RESOURCE_REMOVE_OWN) ||
              function.equals(contentHostingService.AUTH_RESOURCE_HIDDEN) ) {
                return SecurityAdvisor.SecurityAdvice.ALLOWED;
            } else if (function.equals(contentHostingService.AUTH_RESOURCE_REMOVE_ANY)) {
                log.info(userId + " requested ability to remove any content on " + reference +
                    " which we didn't expect, this should be investigated");
                return SecurityAdvisor.SecurityAdvice.ALLOWED;
            }
            return SecurityAdvisor.SecurityAdvice.PASS;
        };
    }

    // --- Statisticable
    // -------------------------------------------------------------
    public String getAssociatedToolId() {
        return MeetingsService.TOOL_ID;
    }

    public String[] getEventKeys() {
        return MeetingsService.EVENT_KEYS;
    }

    public Map<String, String> getEventNames(Locale locale) {
        Map<String, String> localeEventNames = new HashMap<String, String>();
        ResourceLoader msgs = new ResourceLoader("meetings");
        msgs.setContextLocale(locale);
        for (int i = 0; i < MeetingsService.EVENT_KEYS.length; i++) {
            localeEventNames.put(MeetingsService.EVENT_KEYS[i], msgs.getString(MeetingsService.EVENT_KEYS[i]));
        }
        return localeEventNames;
    }

    // --- UTILITY METHODS
    // -----------------------------------------------------------
    private List<MeetingParticipant> extractParticipants(Map<String, Object> params,
            String meetingOwnerId) {

        List<MeetingParticipant> participants = new ArrayList<>();
        /*
        for (String key : params.keySet()) {
            SelectionType selectionType = null;
            String selectionId = null;
            String role = null;

            if (key.startsWith("all_")) {
                selectionType = SelectionType.ALL;
                selectionId = SelectionType.ALL.name();
                role = (String) params.get("all-role_" + params.get(key));

            } else if (key.startsWith("group_")) {
                selectionType = SelectionType.GROUP;
                selectionId = (String) params.get(key);
                role = (String) params.get("group-role_" + selectionId);

            } else if (key.startsWith("role_")) {
                selectionType = SelectionType.ROLE;
                selectionId = (String) params.get(key);
                role = (String) params.get("role-role_" + selectionId);

            } else if (key.startsWith("user_")) {
                selectionType = SelectionType.USER;
                selectionId = (String) params.get(key);
                role = (String) params.get("user-role_" + selectionId);
            }

            if (selectionType != null && selectionId != null && role != null) {
                MeetingParticipant p = new MeetingParticipant();
                p.setSelectionType(selectionType);
                p.setSelectionId(selectionId);
                p.setRole(role);
                participants.add(p);
                //participants.add(new Participant(selectionType, selectionId,role));
            }
        }
        */
        return participants;
    }

    /** Generate a random password */
    private String generatePassword() {
        return Long.toHexString(new Random(System.currentTimeMillis()).nextLong());
    }

    public class MeetingAdapterBean {

        public boolean deleted;
        public Instant endDate;
        public String id;
        public String joinUrl;
        public String name;
        public List<ParticipantAdapterBean> participants;
        public Map<String, String> properties;
        public String ownerId;
        public String ownerDisplayName;
        public Instant startDate;

        public MeetingAdapterBean(Meeting meeting) {

            this.deleted = meeting.getDeleted();
            this.endDate = meeting.getEndDate();
            this.id = meeting.getId();
            this.joinUrl = meeting.getJoinUrl();
            this.ownerId = meeting.getOwnerId();
            this.name = meeting.getName();
            this.properties = meeting.getProperties();
            this.participants = meeting.getParticipants().stream().map(ParticipantAdapterBean::new).collect(Collectors.toList());;
            this.ownerDisplayName = meeting.getOwnerDisplayName();
            this.startDate = meeting.getStartDate();
        }
    }

    public class ParticipantAdapterBean {

        public Long id;
        public MeetingRole role;
        public String selectionId;
        public SelectionType selectionType;
        
        public ParticipantAdapterBean(MeetingParticipant participant) {

            this.id = participant.getId();
            this.role = participant.getRole();
            this.selectionId = participant.getSelectionId();
            this.selectionType = participant.getSelectionType();
        }
    }
}
