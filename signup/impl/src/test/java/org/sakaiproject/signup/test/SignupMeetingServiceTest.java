package org.sakaiproject.signup.test;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.signup.api.SignupMeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SignupMeetingServiceTestConfiguration.class })
public class SignupMeetingServiceTest {

    @Autowired private SignupMeetingService service;

    @Test
    public void testService() {
        Assert.assertNotNull("SignupMeetingService is null", service);
    }
}
