package org.sakaiproject.meetings.impl.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.meetings.api.model.AttendeeType;
import org.sakaiproject.meetings.api.model.Meeting;
import org.sakaiproject.meetings.api.model.MeetingAttendee;
import org.sakaiproject.meetings.api.model.MeetingProperty;
import org.sakaiproject.meetings.api.persistence.MeetingAttendeeRepository;
import org.sakaiproject.meetings.api.persistence.MeetingRepository;
import org.sakaiproject.meetings.impl.MeetingsImplTestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MeetingsImplTestConfiguration.class})
public class MeetingAttendeeRepositoryImplTest {

    @Autowired private MeetingRepository meetingRepository;
    @Autowired private MeetingAttendeeRepository meetingAttendeeRepository;
    
    private Meeting meeting;
    
    @Before
    public void setUp() {
        Meeting data = new Meeting();
        data.setTitle("Test 0");
        data.setDescription("Test 0");
        data.setSiteId("site");
        List<MeetingAttendee> attendees = new ArrayList<MeetingAttendee>();
        MeetingAttendee attendee = new MeetingAttendee();
        attendee.setObjectId("userId");
        attendee.setType(AttendeeType.USER);
        attendees.add(attendee);
        attendee.setMeeting(data);
        attendee = new MeetingAttendee();
        attendee.setObjectId("groupId");
        attendee.setType(AttendeeType.GROUP);
        attendees.add(attendee);
        attendee.setMeeting(data);
        data.setAttendees(attendees);
        this.meeting = meetingRepository.save(data);
    }
    
    @Test
    public void removeAttendeesByMeetingId() {
        meetingAttendeeRepository.removeAttendeesByMeetingId(meeting.getId());
        Meeting meeting = meetingRepository.findMeetingById(this.meeting.getId());
        Assert.assertEquals(meeting.getAttendees().size(), 0);
    }
    
    @Test
    public void removeSiteAndGroupAttendeesByMeetingId () {
        meetingAttendeeRepository.removeSiteAndGroupAttendeesByMeetingId(meeting.getId());
        Meeting meeting = meetingRepository.findMeetingById(this.meeting.getId());
        Assert.assertEquals(meeting.getAttendees().size(), 1);
    }
    
}
