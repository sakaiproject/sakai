/**
 * Copyright (c) 2009-2015 The Sakai Foundation
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

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.sakaiproject.meetings.api.BBBEndpoint;
import org.sakaiproject.meetings.api.MeetingsException;
import org.sakaiproject.meetings.api.persistence.MeetingRepository;
import org.sakaiproject.meetings.api.persistence.Meeting;
import org.sakaiproject.meetings.api.MeetingsService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.user.api.User;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * BBBEndpointHandler is the class responsible to interact with the BigBlueButton
 * API.
 *
 * @author Nuno Fernandes
 */
@Slf4j
@Getter
public class BBBEndpointHandler {

    /** BBB API Version (full) */
    private String version = null;
    /** BBB API Version number */
    private float versionNumber = 0;
    /** BBB API Snapshot Version ? */
    private boolean versionSnapshot = false;

    /** BBB API version check interval (default to 5 min) */
    private long versionCheckInterval = 0;
    /** BBB UX auto close meeting window on exit */
    private boolean autoCloseMeetingWindow = true;
    /** BBB API auto refresh interval for meetings (default to 0 sec means it is not activated) */
    private long autoRefreshMeetings = 0;
    /** BBB API auto refresh interval for recordings(default to 0 sec means it is not activated) */
    private long autoRefreshRecordings = 0;
    /** BBB UX flag to activate/deactivate recording feature for meetings (default to true) */
    private boolean recordingEnabled = true;
    /** BBB UX flag to activate/deactivate recording recording checkbox (default to true) */
    private boolean recordingEditable = true;
    /** BBB default value for 'recording' checkbox (default to false) */
    private boolean recordingDefault = false;
    /** BBB UX flag to activate/deactivate 'recording ready notifications' (default to false) */
    private boolean recordingReadyNotificationEnabled = false;
    /** BBB UX maximum length allowed for meeting description (default 2083) */
    private int descriptionMaxLength = 2048;
    /** BBB UX textBox type for meeting description (default ckeditor) */
    private String descriptionType = "ckeditor";
    /** BBB UX flag to activate/deactivate 'duration' box (default to false) */
    private boolean durationEnabled = false;
    /** BBB default value for 'duration' box (default 120 minutes) */
    private int durationDefault = 120;
    /** BBB UX flag to activate/deactivate 'wait for moderator' feature for meetings (default to true) */
    private boolean waitModeratorEnabled = true;
    /** BBB UX flag to activate/deactivate 'wait for moderator' checkbox (default to true) */
    private boolean waitModeratorEditable = true;
    /** BBB default value for 'wait for moderator' checkbox (default to true) */
    private boolean waitModeratorDefault = true;
    /** BBB UX flag to activate/deactivate 'Users can open multiple sessions' feature for meetings (default to false) */
    private boolean multipleSessionsAllowedEnabled = false;
    /** BBB UX flag to activate/deactivate 'Users can open multiple sessions' checkbox (default to true) */
    private boolean multipleSessionsAllowedEditable = true;
    /** BBB default value for 'Users can open multiple sessions' checkbox (default to false) */
    private boolean multipleSessionsAllowedDefault = false;
    /** BBB UX flag to activate/deactivate 'presentation' file input (default to true) */
    private boolean preuploadPresentationEnabled = true;
    /** BBB UX flag to activate/deactivate 'group sessions' feature for meetings (default to false) */
    private boolean groupSessionsEnabled = true;
    /** BBB UX flag to activate/deactivate 'group sessions' checkbox (default to true) */
    private boolean groupSessionsEditable = true;
    /** BBB default value for 'group sessions' checkbox (default to false) */
    private boolean groupSessionsDefault = false;
    /** BBB flag to activate/deactivate 'recording status' feature for meetings (default to false) */
    private boolean recordingStatsEnabled = false;
    /** Sakai userid used for linking events with users when 'recording status' feature is enabled (default to eid) */
    private String recordingStatsUserId = "eid";
    /** BBB flag to activate/deactivate 'recording format filter' feature for managing permissions on extended formats (default to true) */
    private boolean recordingFormatFilterEnabled = true;
    /** BBB list of formats allowed to be seen whotout applying a permissions filter (default to presentation,video) */
    private String recordingFormatFilterWhitelist = "presentation,video";

    @Resource private BBBEndpoint api;
    @Resource private ServerConfigurationService config;
    @Resource private MeetingRepository storageManager;

    // -----------------------------------------------------------------------
    // --- Initialization related methods ------------------------------------
    // -----------------------------------------------------------------------
    public void init() {

        log.debug("init()");

        autoCloseMeetingWindow = config.getBoolean(MeetingsService.CFG_AUTOCLOSE_WIN, autoCloseMeetingWindow);
        autoRefreshMeetings = (long) config.getInt(MeetingsService.CFG_AUTOREFRESHMEETINGS, (int) autoRefreshMeetings);
        autoRefreshRecordings = (long) config.getInt(MeetingsService.CFG_AUTOREFRESHRECORDINGS, (int) autoRefreshRecordings);
        recordingEnabled = config.getBoolean(MeetingsService.CFG_RECORDING_ENABLED, recordingEnabled);
        recordingEditable = config.getBoolean(MeetingsService.CFG_RECORDING_EDITABLE, recordingEditable);
        recordingDefault = config.getBoolean(MeetingsService.CFG_RECORDING_DEFAULT, recordingDefault);
        recordingReadyNotificationEnabled = config.getBoolean(MeetingsService.CFG_RECORDINGREADYNOTIFICATION_ENABLED, recordingReadyNotificationEnabled);
        descriptionMaxLength = (int) config.getInt(MeetingsService.CFG_DESCRIPTIONMAXLENGTH, descriptionMaxLength);
        descriptionType = config.getString(MeetingsService.CFG_DESCRIPTIONTYPE, descriptionType);
        durationEnabled = (boolean) config.getBoolean(MeetingsService.CFG_DURATION_ENABLED, durationEnabled);
        durationDefault = (int) config.getInt(MeetingsService.CFG_DURATION_DEFAULT, durationDefault);
        waitModeratorEnabled = (boolean) config.getBoolean(MeetingsService.CFG_WAITMODERATOR_ENABLED, waitModeratorEnabled);
        waitModeratorEditable = (boolean) config.getBoolean(MeetingsService.CFG_WAITMODERATOR_EDITABLE, waitModeratorEditable);
        waitModeratorDefault = (boolean) config.getBoolean(MeetingsService.CFG_WAITMODERATOR_DEFAULT, waitModeratorDefault);
        multipleSessionsAllowedEnabled = (boolean) config.getBoolean(MeetingsService.CFG_MULTIPLESESSIONSALLOWED_ENABLED, multipleSessionsAllowedEnabled);
        multipleSessionsAllowedEditable = (boolean) config.getBoolean(MeetingsService.CFG_MULTIPLESESSIONSALLOWED_EDITABLE, multipleSessionsAllowedEditable);
        multipleSessionsAllowedDefault = (boolean) config.getBoolean(MeetingsService.CFG_MULTIPLESESSIONSALLOWED_DEFAULT, multipleSessionsAllowedDefault);
        preuploadPresentationEnabled = (boolean) config.getBoolean(MeetingsService.CFG_PREUPLOADPRESENTATION_ENABLED, preuploadPresentationEnabled);
        groupSessionsEnabled = (boolean) config.getBoolean(MeetingsService.CFG_GROUPSESSIONS_ENABLED, groupSessionsEnabled);
        groupSessionsEditable = (boolean) config.getBoolean(MeetingsService.CFG_GROUPSESSIONS_EDITABLE, groupSessionsEditable);
        groupSessionsDefault = (boolean) config.getBoolean(MeetingsService.CFG_GROUPSESSIONS_DEFAULT, groupSessionsDefault);
        recordingStatsEnabled = (boolean) config.getBoolean(MeetingsService.CFG_RECORDINGSTATS_ENABLED, recordingStatsEnabled);
        recordingFormatFilterEnabled = (boolean) config.getBoolean(MeetingsService.CFG_RECORDINGFORMATFILTER_ENABLED, recordingFormatFilterEnabled);
        recordingFormatFilterWhitelist = config.getString(MeetingsService.CFG_RECORDINGFORMATFILTER_WHITELIST, recordingFormatFilterWhitelist);
    }

    // -----------------------------------------------------------------------
    // --- BBB API wrapper methods -------------------------------------------
    // -----------------------------------------------------------------------
    public Meeting createMeeting(Meeting meeting)
            throws MeetingsException {

        log.debug("createMeeting()");

        meeting.setHostUrl(api.getBaseUrl());
        return api.createMeeting(meeting, autoCloseMeetingWindow, recordingEnabled, recordingReadyNotificationEnabled, isPreuploadPresentationEnabled());
    }

    public boolean isMeetingRunning(String meetingID) throws MeetingsException {

        log.debug("isMeetingRunning({})", meetingID);

        return api.isMeetingRunning(meetingID);
    }

    public Map<String, Object> getMeetingInfo(String meetingID, String password)
            throws MeetingsException {

        log.debug("getMeetingInfo({})", meetingID);

        Map<String, Object> meetingInfoResponse = new HashMap<>();

        try {
            meetingInfoResponse = api.getMeetingInfo(meetingID, password);
        } catch (MeetingsException e) {
            if (MeetingsException.MESSAGEKEY_UNREACHABLE.equals(e.getMessageKey()) ||
                    MeetingsException.MESSAGEKEY_HTTPERROR.equals(e.getMessageKey()) ||
                    MeetingsException.MESSAGEKEY_INVALIDRESPONSE.equals(e.getMessageKey())) {
                meetingInfoResponse = responseError(e.getMessageKey(), e.getMessage());
            }
        } catch (Exception e) {
            meetingInfoResponse = responseError(MeetingsException.MESSAGEKEY_UNREACHABLE, e.getMessage());
        }

        return meetingInfoResponse;
    }

    public String getJoinMeetingURL(Meeting meeting, User user, boolean isModerator)
            throws MeetingsException {

        log.debug("getJoinMeetingURL()");

        String meetingID = meeting.getId();
        String userId = this.getUserId(user);
        String userDisplayName = user.getDisplayName();
        String password = meeting.getAttendeePassword();
        if (isModerator) {
            password = meeting.getModeratorPassword();
        }

        return api.getJoinMeetingURL(meetingID, userId, userDisplayName, password);
    }

    public Map<String, Object> getRecordings(String meetingId) throws MeetingsException {

        log.debug("getRecordings({})", meetingId);

        try {
            return api.getRecordings(meetingId);
        } catch (MeetingsException e) {
            log.debug("getRecordings.MeetingsException: message={}", e.getMessage());
            return responseError(e.getMessageKey(), e.getMessage());
        } catch (Exception e) {
            log.debug("getRecordings.Exception: message={}", e.getMessage());
            return responseError(MeetingsException.MESSAGEKEY_GENERALERROR, e.getMessage());
        }
    }

    public boolean endMeeting(String meetingId, String password) throws MeetingsException {
        return api.endMeeting(meetingId, password);
    }

    public boolean publishRecordings(String recordingId, String publish) throws MeetingsException {
        return api.publishRecordings(recordingId, publish);
    }

    public boolean protectRecordings(String recordingId, String protect) throws MeetingsException {
        return api.protectRecordings(recordingId, protect);
    }

    public boolean deleteRecordings(String recordingID) throws MeetingsException {
        return api.deleteRecordings(recordingID);
    }

    public void makeSureMeetingExists(Meeting meeting) throws MeetingsException {
        api.makeSureMeetingExists(meeting, autoCloseMeetingWindow, recordingEnabled,
            recordingReadyNotificationEnabled, preuploadPresentationEnabled);
    }

    private Map<String, Object> responseError(String messageKey, String message) {

        log.debug("responseError: {}:{}", messageKey, message);

        Map<String, Object> map = new HashMap<>();
        map.put("returncode", "FAILED");
        map.put("messageKey", messageKey);
        map.put("message", message);
        return map;
    }

    private String getUserId(User user) {

        boolean recordingstatsEnabled = isRecordingStatsEnabled();
        if (!recordingstatsEnabled) {
            return null;
        }
        String recordingstatsUserId = getRecordingStatsUserId();
        if ("eid".equals(recordingstatsUserId)) {
            return user.getEid();
        }
        return user.getId();
    }
}
