package org.sakaiproject.meetings.api;

import java.util.List;
import java.util.Optional;

import org.sakaiproject.meetings.api.model.Meeting;

public interface MeetingService {

	public Iterable<Meeting> getAllMeetings();
	public List<Meeting> getAllMeetingsFromSite(String siteId);
	public List<Meeting> getUserMeetings(String userId, String siteId, List <String> groupIds);
	public Meeting createMeeting(Meeting meetingData);
	public void updateMeeting(Meeting meetingData);
	public void deleteMeetingById(String id);
	public Optional<Meeting> getMeetingById(String id);
	public Meeting getMeeting(String id);
	public void removeSiteAndGroupAttendeesByMeetingId(String id);
	public void setMeetingProperty(Meeting meeting, String property, String value);
	public String getMeetingProperty(Meeting meeting, String property);
	public void removeMeetingProperty(Meeting meeting, String property);
	
}
