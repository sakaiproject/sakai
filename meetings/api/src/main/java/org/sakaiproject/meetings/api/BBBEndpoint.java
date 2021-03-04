package org.sakaiproject.meetings.api;

import java.util.Map;

import org.sakaiproject.meetings.api.persistence.Meeting;

public interface BBBEndpoint {

    String getAPIVersion();

    String getBaseUrl();

    String getSalt();

    Meeting createMeeting(Meeting meeting, boolean autoclose, boolean recordingenabled, boolean recordingreadynotification, boolean preuploadpresentation)
            throws MeetingsException;

    boolean isMeetingRunning(String meetingID) throws MeetingsException;

    /**
     * Get detailed live meeting information from BBB server
     **/
    Map<String, Object> getMeetingInfo(String meetingID, String password) throws MeetingsException;

    Map<String, Object> getRecordings(String meetingID) throws MeetingsException;

    boolean endMeeting(String meetingID, String password) throws MeetingsException;

    boolean deleteRecordings(String recordID) throws MeetingsException;

    boolean publishRecordings(String recordID, String publish) throws MeetingsException;

    boolean protectRecordings(String recordID, String protect) throws MeetingsException;

    String getJoinMeetingURL(String meetingID, String userId, String userDisplayName, String password);

    void makeSureMeetingExists(Meeting meeting, boolean autoclose, boolean recordingenabled, boolean recordingreadynotification, boolean preuploadpresentation)
            throws MeetingsException;
}
