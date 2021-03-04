/******************************************************************************
 * Copyright 2015 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.meetings.api.beans;

import java.time.Instant;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.sakaiproject.meetings.api.persistence.Meeting;
import org.sakaiproject.meetings.api.persistence.MeetingParticipant;

public class MeetingTransferBean {

    public boolean deleted;
    public Date endDate;
    public String formattedEndDate;
    public String id;
    public String siteId;
    public String joinUrl;
    public String name;
    public String welcomeMessage;
    public List<MeetingParticipantTransferBean> participants;
    public Map<String, String> properties = new HashMap<>();
    public String ownerId;
    public String ownerDisplayName;
    public Date startDate;
    public String formattedStartDate;
    public String attendeePassword;
    public String moderatorPassword;
    public boolean recording;
    public boolean notStarted;
    public boolean finished;
    public boolean joinable;
    public boolean canEdit;
    public boolean canEnd;
    public boolean canDelete;
    public boolean canJoin;
    public boolean startingToday;

    // Incoming params
    public boolean waitForModerator;
    public boolean multipleSessionsAllowed;
    public String presentation;
    public boolean addToCalendar;
    public String calendarEventId;
    public boolean notifyParticipants;
    public boolean iCalAttached;
    public long iCalAlarmMinutes;

    public MeetingTransferBean() {
    }

    public Meeting toMeeting() {

        Meeting meeting = new Meeting();

        meeting.setDeleted(this.deleted);

        if (this.endDate != null) {
            meeting.setEndDate(this.endDate.toInstant());
        }
        meeting.setId(this.id);
        meeting.setSiteId(this.siteId);
        meeting.setName(this.name);
        meeting.setAttendeePassword(this.attendeePassword);
        meeting.setModeratorPassword(this.moderatorPassword);
        meeting.setWelcomeMessage(this.welcomeMessage);
        meeting.setOwnerId(this.ownerId);
        meeting.setRecording(this.recording);
        meeting.setWaitForModerator(this.waitForModerator);
        meeting.setPresentation(this.presentation);
        meeting.setProperties(this.properties);

        meeting.setParticipants(this.participants.stream().map(p -> {
            MeetingParticipant mp = p.toMeetingParticipant();
            mp.setMeeting(meeting);
            return mp;
        }).collect(Collectors.toList()));

        if (this.startDate != null) {
            meeting.setStartDate(this.startDate.toInstant());
        }
        return meeting;
    }

    public static MeetingTransferBean of(Meeting meeting) {

        MeetingTransferBean mtb = new MeetingTransferBean();

        mtb.deleted = meeting.getDeleted();
        if (meeting.getEndDate() != null) {
            mtb.endDate = Date.from(meeting.getEndDate());
        }
        mtb.id = meeting.getId();
        mtb.siteId = meeting.getSiteId();
        mtb.joinUrl = meeting.getJoinUrl();
        mtb.ownerId = meeting.getOwnerId();
        mtb.name = meeting.getName();
        mtb.welcomeMessage = meeting.getWelcomeMessage();
        mtb.waitForModerator = meeting.getWaitForModerator();
        mtb.attendeePassword = meeting.getAttendeePassword();
        mtb.moderatorPassword = meeting.getModeratorPassword();
        mtb.recording = meeting.getRecording();
        mtb.properties = meeting.getProperties();
        mtb.presentation = meeting.getPresentation();
        mtb.participants = meeting.getParticipants()
            .stream().map(MeetingParticipantTransferBean::new).collect(Collectors.toList());;
        mtb.ownerDisplayName = meeting.getOwnerDisplayName();
        if (meeting.getStartDate() != null) {
            mtb.startDate = Date.from(meeting.getStartDate());
        }

        boolean startOk = meeting.getStartDate() == null || Instant.now().isAfter(meeting.getStartDate());
        boolean endOk = meeting.getEndDate() ==  null || Instant.now().isBefore(meeting.getEndDate());

        mtb.notStarted = !startOk && endOk;
        mtb.finished = startOk && !endOk;
        mtb.joinable = startOk && endOk;

        return mtb;
    }
}
