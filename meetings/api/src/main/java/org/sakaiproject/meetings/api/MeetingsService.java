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

package org.sakaiproject.meetings.api;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.sakaiproject.meetings.api.MeetingsException;
import org.sakaiproject.meetings.api.beans.MeetingTransferBean;
import org.sakaiproject.meetings.api.persistence.Meeting;
import org.sakaiproject.meetings.api.persistence.MeetingParticipant;
import org.sakaiproject.user.api.User;
/**
 * MeetingsMeetingManager is the API for managing meetings.
 *
 * @author Adrian Fish, Nuno Fernandes
 */
public interface MeetingsService {

    /** Entity prefix */
    public static final String ENTITY_PREFIX = "meetings";

    /** Meetings tool ID */
    public final static String TOOL_ID = "sakai.meetings";

    /** Meetings tool Webapp */
    public final static String TOOL_WEBAPP = "/meetings";

    // Tool Settings in sakai.properties.
    public final static String CFG_URL = "meetings.url";
    public final static String CFG_SALT = "meetings.salt";
    public final static String CFG_AUTOCLOSE_WIN = "meetings.autocloseMeetingWindow";
    public final static String CFG_VERSIONCHECKINTERVAL = "meetings.versionCheckInterval";
    public final static String CFG_NOTICE_TEXT = "meetings.notice.text";
    public final static String CFG_NOTICE_LEVEL = "meetings.notice.level";
    public final static String CFG_DEFAULT_PERMS_PRFX = "meetings.default.permissions.";
    public final static String CFG_DEFAULT_ALLUSERS = "meetings.default.participants.all_users";
    public final static String CFG_DEFAULT_OWNER = "meetings.default.participants.owner";
    public final static String CFG_AUTOREFRESHMEETINGS = "meetings.autorefresh.meetings";
    public final static String CFG_AUTOREFRESHRECORDINGS = "meetings.autorefresh.recordings";
    public final static String CFG_RECORDING_ENABLED = "meetings.recording.enabled";
    public final static String CFG_RECORDING_EDITABLE = "meetings.recording.editable";
    public final static String CFG_RECORDING_DEFAULT = "meetings.recording.default";
    public final static String CFG_RECORDINGREADYNOTIFICATION_ENABLED = "meetings.recordingready.enabled";
    public final static String CFG_DESCRIPTIONMAXLENGTH = "meetings.descriptionmaxlength";
    public final static String CFG_DESCRIPTIONTYPE = "meetings.descriptiontype";
    public final static String CFG_DURATION_ENABLED = "meetings.duration.enabled";
    public final static String CFG_DURATION_DEFAULT = "meetings.duration.default";
    public final static String CFG_WAITMODERATOR_ENABLED = "meetings.waitmoderator.enabled";
    public final static String CFG_WAITMODERATOR_EDITABLE = "meetings.waitmoderator.editable";
    public final static String CFG_WAITMODERATOR_DEFAULT = "meetings.waitmoderator.default";
    public final static String CFG_MULTIPLESESSIONSALLOWED_ENABLED = "meetings.multiplesessionsallowed.enabled";
    public final static String CFG_MULTIPLESESSIONSALLOWED_EDITABLE = "meetings.multiplesessionsallowed.editable";
    public final static String CFG_MULTIPLESESSIONSALLOWED_DEFAULT = "meetings.multiplesessionsallowed.default";
    public final static String CFG_PREUPLOADPRESENTATION_ENABLED = "meetings.preuploadpresentation.enabled";
    public final static String CFG_GROUPSESSIONS_ENABLED = "meetings.groupsessions.enabled";
    public final static String CFG_GROUPSESSIONS_EDITABLE = "meetings.groupsessions.editable";
    public final static String CFG_GROUPSESSIONS_DEFAULT = "meetings.groupsessions.default";
    public final static String CFG_RECORDINGSTATS_ENABLED = "meetings.recordingstats.enabled";
    public final static String CFG_RECORDINGSTATS_USERID = "meetings.recordingstats.userid";
    public final static String CFG_RECORDINGFORMATFILTER_ENABLED = "meetings.recordingformatfilter.enabled";
    public final static String CFG_RECORDINGFORMATFILTER_WHITELIST = "meetings.recordingformatfilter.whitelist";
    public final static String CFG_CHECKICALOPTION = "meetings.checkicaloption";

    // System Settings in sakai.properties.
    public final static String SYSTEM_UPLOAD_MAX = "content.upload.max";

    // Permissions
    public static final String FN_PREFIX = "meetings.";
    public static final String FN_CREATE = "meetings.create";
    public static final String FN_EDIT_OWN = "meetings.edit.own";
    public static final String FN_EDIT_ANY = "meetings.edit.any";
    public static final String FN_DELETE_OWN = "meetings.delete.own";
    public static final String FN_DELETE_ANY = "meetings.delete.any";
    public static final String FN_PARTICIPATE = "meetings.participate";
    public static final String FN_RECORDING_VIEW = "meetings.recordings.view";
    public static final String FN_RECORDING_EDIT_OWN = "meetings.recordings.edit.own";
    public static final String FN_RECORDING_EDIT_ANY = "meetings.recordings.edit.any";
    public static final String FN_RECORDING_DELETE_OWN = "meetings.recordings.delete.own";
    public static final String FN_RECORDING_DELETE_ANY = "meetings.recordings.delete.any";
    public static final String FN_RECORDING_EXTENDEDFORMATS_OWN = "meetings.recordings.extendedformats.own";
    public static final String FN_RECORDING_EXTENDEDFORMATS_ANY = "meetings.recordings.extendedformats.any";
    public static final List<String> FUNCTIONS = Arrays.asList(new String[] { FN_CREATE,
            FN_EDIT_OWN, FN_EDIT_ANY, FN_DELETE_OWN, FN_DELETE_ANY, FN_PARTICIPATE,
            FN_RECORDING_VIEW, FN_RECORDING_EDIT_OWN, FN_RECORDING_EDIT_ANY,
            FN_RECORDING_DELETE_OWN, FN_RECORDING_DELETE_ANY,
            FN_RECORDING_EXTENDEDFORMATS_OWN, FN_RECORDING_EXTENDEDFORMATS_ANY });
    // Extra function used to enable admin interface in the client
    public static final String FN_ADMIN = "meetings.admin";

    // Log Events
    /** A meeting was created on the Meetings server */
    public static final String EVENT_MEETING_CREATE = "meetings.create";
    /** A meeting was edited on the Meetings server */
    public static final String EVENT_MEETING_EDIT = "meetings.edit";
    /** A meeting was ended on the Meetings server */
    public static final String EVENT_MEETING_END = "meetings.end";
    /** An user joined a meeting on the Meetings server */
    public static final String EVENT_MEETING_JOIN = "meetings.join";
    /** A recording was deleted on the Meetings server */
    public static final String EVENT_RECORDING_DELETE = "meetings.recording.delete";
    /** A recording was published on the Meetings server */
    public static final String EVENT_RECORDING_PUBLISH = "meetings.recording.publish";
    /** A recording was unpublished on the Meetings server */
    public static final String EVENT_RECORDING_UNPUBLISH = "meetings.recording.unpublish";

    /** ALL Log Events */
    public static final String[] EVENT_KEYS = new String[] {
            EVENT_MEETING_CREATE, EVENT_MEETING_EDIT, EVENT_MEETING_END,
            EVENT_MEETING_JOIN };


    public static final boolean INCLUDE_DELETED_MEETINGS = true;
    public static final boolean NOT_INCLUDE_DELETED_MEETINGS = false;


    // -----------------------------------------------------------------------
    // --- Meetings Implementation related methods --------------------------------
    // -----------------------------------------------------------------------
    /**
     * Get the meeting identified by the supplied meetingId
     */
    Optional<MeetingTransferBean> getMeeting(String meetingId, String siteId) throws SecurityException, Exception;

    /**
     * Returns the meetings for the supplied site that the current Sakai user
     * can participate in.
     *
     * @param siteId
     *            The site to retrieve meetings for
     * @return A list of Meeting objects
     */
    List<MeetingTransferBean> getSiteMeetings(String siteId);

    /**
     * Saves a meeting using the passed in object. Populates the id, password
     * and token fields of <code>meeting</code> with the data returned from Meetings.
     *
     * @param meeting
     */
    MeetingTransferBean saveMeeting(MeetingTransferBean meetingBean, boolean meetingOnly) throws Exception;

    /**
     * Check the server to see if the meeting is running (i.e.
     * there is someone in the meeting)
     */
    boolean isMeetingRunning(String meetingId) throws MeetingsException;

    /**
     * Get live meeting details from Meetings server.
     */
    Map<String, Object> getMeetingInfo(String meetingId) throws Exception;

    /**
     * Get playback recordings from Meetings server.
     */
    Map<String, Object> getRecordings(String meetingId, String siteId) throws Exception;

    /**
     * Get ALL playback recordings from Meetings server for the current Site.
     */
    Map<String, Object> getSiteRecordings(String siteId) throws SecurityException, Exception;

    /**
     * Log an event indicating that the current user joined the specified
     * meeting
     */
    void logMeetingJoin(String meetingId) throws Exception;

    /**
     * Currently clears up the Sakai records and endMeeting.
     */
    boolean deleteMeeting(String id) throws Exception;

    /**
     * Delete all the meetings in a site.
     */
    int deleteMeetingsBySiteId(String siteId) throws SecurityException, MeetingsException;

    /**
     * Only executes endMeeting.
     */
    boolean endMeeting(String id) throws Exception;

    /**
     * Get a meeting with all the defaults set up
     */
    MeetingTransferBean getMeetingTemplate(String siteId);

    /**
     * Deletes a recording on the Meetings server
     */
    boolean deleteRecordings(String recordID) throws SecurityException, MeetingsException;

    /**
     * Publish and unpublish recordings using the publishRecordings api command
     */
    boolean publishRecordings(String recordID, String publish) throws SecurityException, MeetingsException;

    /**
     * Protect and unprotect recordings using the *_______________* api command
     */
    boolean protectRecordings(String recordID, String protect) throws SecurityException, MeetingsException;

    /**
     * Check if meeting is ready to be joined.
     */
    void checkJoinMeetingPreConditions(Meeting meeting) throws MeetingsException;

    // -----------------------------------------------------------------------
    // --- Meetings Security related methods --------------------------------------
    // -----------------------------------------------------------------------

    /**
     * Returns true if the current user is a participant in the supplied
     * meeting.
     */
    boolean isMeetingParticipant(Meeting meeting);

    /**
     * Returns true if the current user can create meetings in the supplied
     * site.
     */
    boolean getCanCreate(String siteId);

    /**
     * Returns true if the current user can edit the specified meeting in the
     * supplied site.
     */
    boolean getCanEdit(String siteId, Meeting meeting);

    /**
     * Returns true if the current user can delete the specified meeting in the
     * supplied site.
     */
    boolean getCanDelete(String siteId, Meeting meeting);

    /**
     * Returns true if the current user can participate on meetings in the
     * supplied site.
     */
    boolean getCanParticipate(String siteId);

    /**
     * Returns true if the current user can view recordings in the supplied site
     */
    boolean getCanViewSiteRecordings(String siteId);

    /**
     * Checks tool permissions in site, apply defaults if no perms set and
     * defaults set on sakai.properties.
     */
    void checkPermissions(String siteId);

    // -----------------------------------------------------------------------
    // --- Public utility methods --------------------------------------------
    // -----------------------------------------------------------------------

    /**
     * Returns current server time (in milliseconds) in user timezone.
     */
    Map<String, Object> getServerTimeInUserTimezone();

    /**
     * Returns current server time (in milliseconds) in user timezone.
     */
    Map<String, Object> getServerTimeInDefaultTimezone();

    /**
     * Returns the text notice (if any) to be displayed on the first time the
     * tool is accessed by an user.
     */
    String getNoticeText();

    /**
     * Returns the text notice level to be used when displaying the notice text
     * (info | warn | success).
     */
    String getNoticeLevel();

    /**
     * Returns the url for joining to a meeting
     */
    String getJoinUrl(Meeting meeting, User user) throws SecurityException, Exception;

    /**
     * Returns true if participants were notified when recording was ready
     */
    boolean recordingReady(String meetingId);

    /**
     * Returns meetings.autorefresh.meetings parameter set up on sakai.properties or the one set up by default.
     */
    String getAutorefreshForMeetings();

    /**
     * Returns meetings.autorefresh.recordings parameter set up on sakai.properties or the one set up by default.
     */
    String getAutorefreshForRecordings();

    String isRecordingEnabled();

    String isRecordingEditable();

    String getRecordingDefault();

    String isDurationEnabled();

    String getDurationDefault();

    String isWaitModeratorEnabled();

    String isWaitModeratorEditable();

    String getWaitModeratorDefault();

    String isMultipleSessionsAllowedEnabled();

    String isMultipleSessionsAllowedEditable();

    String getMultipleSessionsAllowedDefault();

    String isPreuploadPresentationEnabled();

    String isGroupSessionsEnabled();

    String isGroupSessionsEditable();

    String getGroupSessionsDefault();

    String getMaxLengthForDescription();

    String getTextBoxTypeForDescription();

    boolean databaseStoreMeeting(MeetingTransferBean meetingBean);

    boolean databaseDeleteMeeting(Meeting meeting);

    MeetingParticipant getParticipantFromMeeting(Meeting meeting, String userId);

    boolean isUserAllowedInLocation(String userId, String permission, String locationId);

    String getUserRoleInSite(String userId, String siteId);

    List<String> getUserGroupIdsInSite(String userId, String siteId);

    boolean isRecordingFormatFilterEnabled();

    String getRecordingFormatFilterWhitelist();
}
