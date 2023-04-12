package org.sakaiproject.meetings.impl.persistence;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.meetings.api.model.Meeting;
import org.sakaiproject.meetings.api.model.MeetingProperty;
import org.sakaiproject.meetings.api.persistence.MeetingPropertyRepository;
import org.sakaiproject.meetings.api.persistence.MeetingRepository;
import org.sakaiproject.meetings.impl.MeetingsImplTestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MeetingsImplTestConfiguration.class})
public class MeetingPropertyRepositoryImplTest {

    @Autowired private MeetingRepository meetingRepository;
    @Autowired private MeetingPropertyRepository meetingPropertyRepository;
    
    private Meeting meeting;
    
    @Before
    public void setUp() {
        Meeting meeting = new Meeting();
        meeting.setTitle("Test 0");
        meeting.setDescription("Test 0");
        meeting.setSiteId("site1");
        meeting = meetingRepository.save(meeting);
        MeetingProperty property = new MeetingProperty();
        property.setName("property");
        property.setValue("value");
        property.setMeeting(meeting);
        meetingPropertyRepository.save(property);
        this.meeting = meeting;
    }
    
    @Test
    public void findFirstByMeetingIdAndName() {
        Optional<MeetingProperty> opt = meetingPropertyRepository.findFirstByMeetingIdAndName(meeting.getId(), "property");
        Assert.assertTrue(opt.isPresent());
        MeetingProperty prop = opt.get();
        Assert.assertEquals(prop.getValue(), "value");
    }
    
    @Test
    public void deletePropertiesByMeetingId() {
       meetingPropertyRepository.deletePropertiesByMeetingId(meeting.getId());
       Optional<MeetingProperty> opt = meetingPropertyRepository.findFirstByMeetingIdAndName(meeting.getId(), "property");
       Assert.assertFalse(opt.isPresent());
    }
    
}
