package org.sakaiproject.meetings.api.persistence;

import org.sakaiproject.meetings.api.model.MeetingAttendee;
import org.sakaiproject.serialization.SerializableRepository;

public interface MeetingAttendeeRepository extends SerializableRepository<MeetingAttendee, Long> {

    public void removeAttendeesByMeetingId(String meetingId);
    public void removeSiteAndGroupAttendeesByMeetingId (String meetingId);
    
}
