/*
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.messaging.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.messaging.api.UserMessagingService;
import org.sakaiproject.messaging.api.UserNotificationData;
import org.sakaiproject.messaging.api.UserNotificationHandler;
import org.sakaiproject.messaging.api.model.UserNotification;
import org.sakaiproject.messaging.api.repository.UserNotificationRepository;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.test.SakaiTests;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.AopTestUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Observable;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {UserMessagingServiceTestConfiguration.class})
public class UserMessagingServiceTests extends SakaiTests {

    @Autowired private EntityManager entityManager;
    @Autowired private ServerConfigurationService serverConfigurationService;
    @Autowired private SessionManager sessionManager;
    @Autowired private UserMessagingService userMessagingService;
    @Autowired private UserNotificationRepository userNotificationRepository;
    @Autowired private UserTimeService userTimeService;

    String student = "student";
    User studentUser = null;
    String instructor = "instructor";
    User instructorUser = null;
    UserNotification userNotification1;
    UserNotification userNotification2;

    @Before
    public void setup() {

        super.setup();

        studentUser = mock(User.class);
        when(studentUser.getDisplayName()).thenReturn("Student User");
        try {
            when(userDirectoryService.getUser(student)).thenReturn(studentUser);
        } catch (UserNotDefinedException unde) {}

        instructorUser = mock(User.class);
        when(instructorUser.getDisplayName()).thenReturn("Instructor User");
        try {
            when(userDirectoryService.getUser(instructor)).thenReturn(instructorUser);
        } catch (UserNotDefinedException unde) {}

        when(userTimeService.dateTimeFormat(any(), any(), any())).thenReturn("07 Feb 1971");;

        userNotification1 = new UserNotification();
        userNotification1.setToUser(student);
        userNotification1.setFromUser(instructor);
        userNotification1.setTitle("Notification 1");
        userNotification1.setEvent("notification1.event");
        userNotification1.setEventDate(Instant.now());
        userNotification1.setRef("/notification/one");
        userNotification1.setUrl("/portal/site/bogus/tool/xyz");

        userNotification2 = new UserNotification();
        userNotification2.setToUser(student);
        userNotification2.setFromUser(instructor);
        userNotification2.setTitle("Notification 2");
        userNotification2.setEvent("notification2.event");
        userNotification2.setEventDate(Instant.now());
        userNotification2.setRef("/notification/two");
        userNotification2.setUrl("/portal/site/bogus/tool/xyz");
    }

    @Test
    public void createAndClearNotifications() {

        assertTrue("The alerts list should be empty", userMessagingService.getNotifications().isEmpty());

        switchToStudent();

        assertTrue("The alerts list should be empty", userMessagingService.getNotifications().isEmpty());

        UserNotification updatedUserNotification1 = userNotificationRepository.save(userNotification1);
        assertEquals("There should be 1 alert", 1, userMessagingService.getNotifications().size());

        UserNotification updatedUserNotification2 = userNotificationRepository.save(userNotification2);
        assertEquals("There should be 2 alerts", 2, userMessagingService.getNotifications().size());

        userMessagingService.clearNotification(updatedUserNotification1.getId());
        assertEquals("There should be 1 alert", 1, userMessagingService.getNotifications().size());

        switchToInstructor();
        assertFalse("One user should not be able to delete another's notifications", userMessagingService.clearNotification(updatedUserNotification2.getId()));

        switchToStudent();
        assertEquals("There should be 1 alert", 1, userMessagingService.getNotifications().size());
    }

    @Test
    public void clearAllNotifications() {

        switchToStudent();

        assertTrue("The alerts list should be empty", userMessagingService.getNotifications().isEmpty());

        UserNotification updatedUserNotification1 = userNotificationRepository.save(userNotification1);
        UserNotification updatedUserNotification2 = userNotificationRepository.save(userNotification2);

        assertEquals("There should be 2 alerts", 2, userMessagingService.getNotifications().size());

        userMessagingService.clearAllNotifications();

        assertEquals("There should be 0 alerts", 0, userMessagingService.getNotifications().size());
    }

    @Test
    public void subscribeToPush() {

        switchToInstructor();


        String endpoint = "fake_endpoint";
        String auth = "fake_auth";
        String userKey = "fake_user_key";
        String fingerprint = "fake_fingerprint";
        String tool = "sakai.tool";

        when(entityManager.getTool(any())).thenReturn(Optional.of(tool));
        Site site = mock(Site.class);
        when(site.isPublished()).thenReturn(true);

        try {
            when(siteService.getSite(any())).thenReturn(site);
        } catch (Exception e) {
        }

        userMessagingService.subscribeToPush(endpoint, auth, userKey, fingerprint);

        UserNotificationData und = new UserNotificationData(student, instructor, "site1", "New Assignment", "http://nothing.com", "sakai.tool", false, null);

        Observable noop = new Observable();

        String eventString = "assn.new";

        Event event = mockEvent(eventString);

        UserNotificationHandler handler = mock(UserNotificationHandler.class);
        when(handler.getHandledEvents()).thenReturn(Collections.singletonList(eventString));
        when(handler.handleEvent(event)).thenReturn(Optional.of(Collections.singletonList(und)));
        userMessagingService.registerHandler(handler);

        when(serverConfigurationService.getBoolean("portal.notifications.push.enabled", false)).thenReturn(true);

        ((UserMessagingServiceImpl) AopTestUtils.getTargetObject(userMessagingService)).update(noop, event);
        verify(handler).handleEvent(event);
    }

    private void switchToStudent() {
        when(sessionManager.getCurrentSessionUserId()).thenReturn(student);
    }

    private void switchToInstructor() {
        when(sessionManager.getCurrentSessionUserId()).thenReturn(instructor);
    }

    private Event mockEvent(String eventString) {

        Event event = mock(Event.class);
        when(event.getEvent()).thenReturn(eventString);
        when(event.getResource()).thenReturn("no.resource");
        when(event.getContext()).thenReturn("/no/context");
        when(event.getUserId()).thenReturn(instructor);
        when(event.getEventTime()).thenReturn(new Date());
        return event;
    }
}
