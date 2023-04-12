package org.sakaiproject.meetings.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.meetings.api.MeetingService;
import org.sakaiproject.meetings.api.model.AttendeeType;
import org.sakaiproject.meetings.api.model.Meeting;
import org.sakaiproject.meetings.api.model.MeetingAttendee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MeetingsImplTestConfiguration.class})
public class MeetingServiceImplTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired private MeetingService meetingService;
    
    @Before
    public void setUp() {
        // Meeting "Test 0" - SITE1 - Perms: GROUP1
        Meeting data = new Meeting();
        data.setTitle("Test 0");
        data.setDescription("Test 0");
        data.setSiteId("site1");
        // Attendees
        List<MeetingAttendee> attendees = new ArrayList<MeetingAttendee>();
        // Attendee 1 - GROUP
        MeetingAttendee attendee = new MeetingAttendee();
        attendee.setObjectId("groupId1");
        attendee.setType(AttendeeType.GROUP);
        attendees.add(attendee);
        attendee.setMeeting(data);
        // Create meeting 1
        data.setAttendees(attendees);
        data = meetingService.createMeeting(data);
        
        // Meeting "Test 1" - SITE1 - Perms: USERID1, GROUP1, GROUP2
        data = new Meeting();
        data.setTitle("Test 1");
        data.setDescription("Test 1");
        data.setSiteId("site1");
        // Attendees
        attendees = new ArrayList<MeetingAttendee>();
        // Attendee 1 - USER
        attendee = new MeetingAttendee();
        attendee.setObjectId("userId1");
        attendee.setType(AttendeeType.USER);
        attendees.add(attendee);
        attendee.setMeeting(data);
        // Attendee 2 - GROUP
        attendee = new MeetingAttendee();
        attendee.setObjectId("groupId1");
        attendee.setType(AttendeeType.GROUP);
        attendees.add(attendee);
        attendee.setMeeting(data);
        // Attendee 3 - GROUP
        attendee = new MeetingAttendee();
        attendee.setObjectId("groupId2");
        attendee.setType(AttendeeType.GROUP);
        attendees.add(attendee);
        attendee.setMeeting(data);
        // Create meeting 1
        data.setAttendees(attendees);
        data = meetingService.createMeeting(data);
        
        // Meeting "Test 2" - SITE2 - Perms: USERID2, SITE2
        data = new Meeting();
        data.setTitle("Test 2");
        data.setDescription("Test 2");
        data.setSiteId("site2");
        // Attendees
        attendees = new ArrayList<MeetingAttendee>();
        // Attendee 1 - USER
        attendee = new MeetingAttendee();
        attendee.setObjectId("userId2");
        attendee.setType(AttendeeType.USER);
        attendees.add(attendee);
        attendee.setMeeting(data);
        // Attendee 2 - SITE
        attendee = new MeetingAttendee();
        attendee.setObjectId("site2");
        attendee.setType(AttendeeType.SITE);
        attendees.add(attendee);
        attendee.setMeeting(data);
        // Create meeting 2
        data.setAttendees(attendees);
        data = meetingService.createMeeting(data);
        
        // Meeting "Test 3" - SITE2 - Perms: USERID2
        data = new Meeting();
        data.setTitle("Test 3");
        data.setDescription("Test 3");
        data.setSiteId("site3");
        // Attendees
        attendees = new ArrayList<MeetingAttendee>();
        // Attendee 1 - USER
        attendee = new MeetingAttendee();
        attendee.setObjectId("userId2");
        attendee.setType(AttendeeType.USER);
        attendees.add(attendee);
        attendee.setMeeting(data);
        // Create meeting 3
        data.setAttendees(attendees);
        data = meetingService.createMeeting(data);
    }
    
    @Test
    public void createAndUpdateMeetingTest() {
        Meeting data = new Meeting();
        data.setTitle("Test");
        data.setDescription("Test");
        data.setSiteId("site");
        data = meetingService.createMeeting(data);
        Assert.assertNotNull(data.getId());
        data.setTitle("Modified");
        meetingService.updateMeeting(data);
        Meeting data2 = meetingService.getMeeting(data.getId());
        Assert.assertTrue("Modified".equals(data2.getTitle()));
    }
    
    @Test
    public void createLongDescriptionMeetingTest() {
        Meeting data = new Meeting();
        data.setTitle("Test Long");
        data.setDescription(RandomStringUtils.randomAlphabetic(4000));
        data.setSiteId("site");
        Meeting ret = meetingService.createMeeting(data);
        Assert.assertNotNull(ret.getId());
        
        Meeting test = meetingService.getMeeting(ret.getId());
        Assert.assertNotNull(test.getId());
        
        Assert.assertThrows(Exception.class,
            ()->{
                Meeting data2 = new Meeting();
                data2.setTitle("Test Long 2");
                data2.setDescription(RandomStringUtils.randomAlphabetic(4001));
                data2.setSiteId("site");
                
                Meeting ret2 = meetingService.createMeeting(data2);
                Assert.assertNotNull(ret.getId());
                
                Meeting test2 = meetingService.getMeeting(ret2.getId());
                //should never reach this point
                Assert.assertNull(test2.getId());
        });
    }
    
    @Test
    public void testGetOptionalMeetingByIdIsNotPresent() {
        Optional<Meeting> optMeeting = meetingService.getMeetingById("nonExistentId");
        Assert.assertFalse(optMeeting.isPresent());
    }
    
    @Test
    public void testGetOptionalMeetingByIdIsPresent() {
        Meeting data = new Meeting();
        data.setTitle("Test");
        data.setDescription("Test");
        data.setSiteId("site");
        data = meetingService.createMeeting(data);
        Optional<Meeting> optMeeting = meetingService.getMeetingById(data.getId());
        Assert.assertTrue(optMeeting.isPresent());
    }
    
    @Test
    public void getAllMeetings() {
        Iterable<Meeting> meetings = meetingService.getAllMeetings();
        List<Meeting> list = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(meetings.iterator(), Spliterator.ORDERED), false)
                .collect(Collectors.toList());
        Assert.assertTrue(list.size() == 4);
    }
    
    @Test
    public void getAllMeetingsFromSite() {
        List<Meeting> list = meetingService.getAllMeetingsFromSite("site2");
        Assert.assertTrue(list.size() == 1);
    }
    
    @Test
    public void getUserMeetingsUserPermission() {
        List<Meeting> list = meetingService.getUserMeetings("userId2", "site3", null);
        Assert.assertTrue(list.size() == 1);
    }
    
    @Test
    public void getUserMeetingsSitePermission() {
        List<Meeting> list = meetingService.getUserMeetings(null, "site2", null);
        Assert.assertTrue(list.size() == 1);
    }
    
    @Test
    public void getUserMeetingsGroupPermission() {
        List<Meeting> list = meetingService.getUserMeetings(null, "site1", Arrays.asList("groupId1"));
        Assert.assertTrue(list.size() == 2);
        list = meetingService.getUserMeetings(null, "site1", Arrays.asList("groupId2"));
        Assert.assertTrue(list.size() == 1);
    }
    
    @Test
    public void deleteMeetingById() {
        List<Meeting> list = meetingService.getAllMeetingsFromSite("site2");
        Meeting meeting = list.get(0);
        String idMeeting = meeting.getId();
        meetingService.deleteMeetingById(idMeeting);
        Optional<Meeting> optMeeting = meetingService.getMeetingById(idMeeting);
        Assert.assertFalse(optMeeting.isPresent());
    }
    
    @Test
    public void setGetAndRemoveMeetingProperty() {
        List<Meeting> list = meetingService.getAllMeetingsFromSite("site2");
        Meeting meeting = list.get(0);
        meetingService.setMeetingProperty(meeting, "property", "value");
        String value = meetingService.getMeetingProperty(meeting, "property");
        Assert.assertTrue("value".equals(value));
        meetingService.removeMeetingProperty(meeting, "property");
        value = meetingService.getMeetingProperty(meeting, "property");
        Assert.assertNull(value);
    }
    
}
