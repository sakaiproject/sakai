/**
 * Copyright (c) 2024 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.meetings.impl.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import org.sakaiproject.meetings.api.persistence.MeetingRepository;
import org.sakaiproject.meetings.impl.MeetingsImplTestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MeetingsImplTestConfiguration.class})
public class MeetingRepositoryImplTest {

    @Autowired private MeetingRepository meetingRepository;
    
    private Meeting meeting1;
    
    @Before
    public void setUp() {
        // Meeting "Test 0" - SITE3 - Perms: GROUP1
        Meeting data = new Meeting();
        data.setTitle("Test 0");
        data.setDescription("Test 0");
        data.setSiteId("site3");
        // Attendees
        List<MeetingAttendee> attendees = new ArrayList<MeetingAttendee>();
        // Attendee 1 - GROUP
        MeetingAttendee attendee = new MeetingAttendee();
        attendee.setObjectId("groupId1");
        attendee.setType(AttendeeType.GROUP);
        attendees.add(attendee);
        attendee.setMeeting(data);
        // Create meeting
        data.setAttendees(attendees);
        this.meeting1 = meetingRepository.save(data);
        
        // Meeting "Test 1" - SITE4 - Perms: USER1
        data = new Meeting();
        data.setTitle("Test 1");
        data.setDescription("Test 1");
        data.setSiteId("site4");
        // Attendees
        attendees = new ArrayList<MeetingAttendee>();
        // Attendee 1 - USER
        attendee = new MeetingAttendee();
        attendee.setObjectId("userId");
        attendee.setType(AttendeeType.USER);
        attendees.add(attendee);
        attendee.setMeeting(data);
        // Create meeting
        data.setAttendees(attendees);
        meetingRepository.save(data);
    }
    
    @Test
    public void findById() {
        Optional<Meeting> meeting = meetingRepository.findById(meeting1.getId());
        Assert.assertTrue(meeting.isPresent());
    }
    
    @Test
    public void findMeetingById() {
        Meeting meeting = meetingRepository.findMeetingById(meeting1.getId());
        Assert.assertNotNull(meeting);
    }
    
    @Test
    public void deleteById() {
        Meeting meeting = meetingRepository.findMeetingById(meeting1.getId());
        meetingRepository.delete(meeting);
        meeting = meetingRepository.findMeetingById(meeting1.getId());
        Assert.assertNull(meeting);
    }
    
    @Test
    public void getSiteMeetings() {
        List<Meeting> meetings = meetingRepository.getSiteMeetings("site3");
        Assert.assertTrue(meetings.size() > 0);
    }
    
    @Test
    public void getMeetingsByUser() {
        List<Meeting> meetings = meetingRepository.getMeetings("userId", "site3", null);
        Assert.assertTrue(meetings.size() == 0);
        meetings = meetingRepository.getMeetings("userId", "site4", null);
        Assert.assertTrue(meetings.size() > 0);
    }
    
}
