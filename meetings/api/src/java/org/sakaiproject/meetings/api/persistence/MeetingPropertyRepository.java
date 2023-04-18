package org.sakaiproject.meetings.api.persistence;

import java.util.Optional;

import org.sakaiproject.meetings.api.model.MeetingProperty;
import org.sakaiproject.serialization.SerializableRepository;

public interface MeetingPropertyRepository  extends SerializableRepository<MeetingProperty, Long> {

    public Optional<MeetingProperty> findFirstByMeetingIdAndName(String meetingId, String name);
    public void deletePropertiesByMeetingId(String meetingId);
    public void deletePropertyByMeetingIdAndName(String meetingId, String propertyName);

}
