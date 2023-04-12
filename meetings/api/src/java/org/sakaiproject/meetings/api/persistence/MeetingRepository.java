package org.sakaiproject.meetings.api.persistence;

import java.util.List;
import java.util.Optional;

import org.sakaiproject.meetings.api.model.Meeting;
import org.sakaiproject.serialization.SerializableRepository;

public interface MeetingRepository extends SerializableRepository<Meeting, String> {

    public Optional<Meeting> findById(String id);
    public Meeting findMeetingById(String id);
    public void deleteById(String id);
    public List<Meeting> getSiteMeetings(String siteId);
    public List<Meeting> getMeetings(String userId, String siteId, List<String> groupIds);
    
}
