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
import org.sakaiproject.messaging.api.Message;
import org.sakaiproject.messaging.api.UserNotificationData;
import org.sakaiproject.messaging.api.UserNotificationHandler;
import org.sakaiproject.messaging.api.model.UserNotification;
import org.sakaiproject.messaging.api.model.PushSubscription;
import org.sakaiproject.messaging.api.repository.PushSubscriptionRepository;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.hibernate.PropertyValueException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.sakaiproject.messaging.api.repository.UserNotificationRepository;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.test.SakaiTests;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.util.BaseResourceProperties;
import org.springframework.test.annotation.DirtiesContext;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
    @Autowired private PreferencesService preferencesService;
    @Autowired private PushSubscriptionRepository pushSubscriptionRepository;
    @Autowired private SessionFactory sessionFactory;
    @Autowired private PlatformTransactionManager transactionManager;

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
    public void countsOnlyUnviewedNotifications() {

        switchToStudent();

        assertEquals("The alerts count should be empty", 0, userMessagingService.getUnviewedNotificationsCount());

        userNotification1.setViewed(false);
        userNotificationRepository.save(userNotification1);

        userNotification2.setViewed(true);
        userNotificationRepository.save(userNotification2);

        UserNotification broadcastNotification = new UserNotification();
        broadcastNotification.setToUser("broadcast");
        broadcastNotification.setFromUser(instructor);
        broadcastNotification.setTitle("Broadcast Notification");
        broadcastNotification.setEvent("notification3.event");
        broadcastNotification.setEventDate(Instant.now());
        broadcastNotification.setEndDate(Instant.now().plusSeconds(3600));
        broadcastNotification.setRef("/notification/three");
        broadcastNotification.setUrl("/portal/site/bogus/tool/xyz");
        broadcastNotification.setBroadcast(true);
        broadcastNotification.setViewed(false);
        userNotificationRepository.save(broadcastNotification);

        assertEquals("There should be 2 unviewed alerts", 2, userMessagingService.getUnviewedNotificationsCount());
    }

    @Test
    public void subscribeToPush() {

        switchToInstructor();


        String endpoint = "https://1.1.1.1/push";
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

    /** Rejected endpoints must not open a database transaction for DNS validation. */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void rejectsPushEndpointBeforeStartingTransaction() {
        switchToInstructor();
        Statistics statistics = sessionFactory.getStatistics();
        boolean enabled = statistics.isStatisticsEnabled();
        statistics.setStatisticsEnabled(true);
        try {
            long transactions = statistics.getTransactionCount();
            assertThrows(IllegalArgumentException.class, () -> userMessagingService.subscribeToPush(
                "https://169.254.169.254/push", "auth", "key", "invalid-endpoint"));
            assertEquals(transactions, statistics.getTransactionCount());
        } finally {
            statistics.setStatisticsEnabled(enabled);
        }
    }

    /** Subscription replacement commits as one operation and rolls back the deletion if saving fails. */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void replacesPushSubscriptionAtomically() {
        switchToInstructor();
        String fingerprint = "atomic-replacement";
        try {
            userMessagingService.subscribeToPush("https://1.1.1.1/push", "auth", "first-key", fingerprint);
            userMessagingService.subscribeToPush("https://8.8.8.8/push", "auth", "replacement-key", fingerprint);
            List<PushSubscription> subscriptions = pushSubscriptionRepository.findByUser(instructor);
            assertEquals(1, subscriptions.size());
            assertEquals("https://8.8.8.8/push", subscriptions.get(0).getEndpoint());
            assertEquals("replacement-key", subscriptions.get(0).getUserKey());

            assertThrows(PropertyValueException.class, () -> userMessagingService.subscribeToPush(
                "https://1.1.1.1/push", null, "invalid-key", fingerprint));
            subscriptions = pushSubscriptionRepository.findByUser(instructor);
            assertEquals(1, subscriptions.size());
            assertEquals("https://8.8.8.8/push", subscriptions.get(0).getEndpoint());
            assertEquals("replacement-key", subscriptions.get(0).getUserKey());
        } finally {
            new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                pushSubscriptionRepository.deleteByFingerprint(fingerprint));
        }
    }

    /** Shutdown waits for work already accepted by the real messaging executor. */
    @Test
    @DirtiesContext
    public void destroyWaitsForQueuedMessages() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch interrupted = new CountDownLatch(1);
        ExecutorService shutdownWorker = Executors.newSingleThreadExecutor();
        try {
            queueBlockingMessage(started, release, interrupted);
            assertTrue(started.await(5, TimeUnit.SECONDS));
            Future<?> shutdown = shutdownWorker.submit(() ->
                ((UserMessagingServiceImpl) AopTestUtils.getTargetObject(userMessagingService)).destroy());
            assertThrows(TimeoutException.class, () -> shutdown.get(100, TimeUnit.MILLISECONDS));
            release.countDown();
            shutdown.get(5, TimeUnit.SECONDS);
            assertEquals("Graceful shutdown should not interrupt the task", 1, interrupted.getCount());
        } finally {
            release.countDown();
            shutdownWorker.shutdownNow();
        }
    }

    /** Interrupted shutdown cancels running work and preserves the caller's interrupt flag. */
    @Test
    @DirtiesContext
    public void interruptedDestroyCancelsMessagesAndPreservesInterrupt() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch interrupted = new CountDownLatch(1);
        try {
            queueBlockingMessage(started, release, interrupted);
            assertTrue(started.await(5, TimeUnit.SECONDS));
            Thread.currentThread().interrupt();
            ((UserMessagingServiceImpl) AopTestUtils.getTargetObject(userMessagingService)).destroy();
            assertTrue(Thread.currentThread().isInterrupted());
            Thread.interrupted();
            assertTrue("Forced shutdown should interrupt the task", interrupted.await(5, TimeUnit.SECONDS));
        } finally {
            Thread.interrupted();
            release.countDown();
        }
    }

    /** Blocks a message at the external preferences boundary without replacing the service or executor. */
    private void queueBlockingMessage(CountDownLatch started, CountDownLatch release, CountDownLatch interrupted) {
        Preferences preferences = mock(Preferences.class);
        when(preferences.getToolNotificationProperties("test")).thenReturn(new BaseResourceProperties());
        when(preferencesService.getPreferences(student)).thenReturn(preferences);
        when(preferencesService.getLocale(student)).thenAnswer(invocation -> {
            started.countDown();
            try {
                if (!release.await(10, TimeUnit.SECONDS)) {
                    throw new AssertionError("Message was not released by the test");
                }
            } catch (InterruptedException e) {
                interrupted.countDown();
                Thread.currentThread().interrupt();
            }
            return Locale.ENGLISH;
        });
        when(studentUser.getId()).thenReturn(student);
        userMessagingService.message(Set.of(studentUser), Message.builder().tool("test").type("test").build(),
            Collections.emptyList(), new HashMap<>(), 0);
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
