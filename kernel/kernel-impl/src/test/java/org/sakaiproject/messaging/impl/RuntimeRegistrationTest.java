/*
 * Copyright (c) 2003-2022 The Apereo Foundation
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.ignite.EagerIgniteSpringBean;
import org.sakaiproject.messaging.api.UserMessagingService;
import org.sakaiproject.messaging.impl.UserMessagingServiceImpl;
import org.sakaiproject.messaging.api.UserNotificationHandler;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.test.SakaiTests;

import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.AopTestUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {UserMessagingServiceTestConfiguration.class})
public class RuntimeRegistrationTest extends SakaiTests {

    @Autowired private EventTrackingService eventTrackingService;
    @Autowired private EntityManager entityManager;
    @Autowired private EagerIgniteSpringBean ignite;
    @Autowired private ServerConfigurationService serverConfigurationService;
    @Autowired private UserMessagingService userMessagingService;

    @Before
    public void setup() {

        when(serverConfigurationService.getBoolean(eq("portal.bullhorns.enabled"), anyBoolean())).thenReturn(true);
    }

    @Test
    public void givenNoHandlers_whenInitializing_thenItStartsUp() {

        try {
            ((UserMessagingServiceImpl) AopTestUtils.getTargetObject(userMessagingService)).init();
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void givenARegisteredHandler_whenMatchingEventOccurs_thenTheHandlerReceivesIt() {

        // GIVEN
        UserNotificationHandler handler = mock(UserNotificationHandler.class);
        Event event = ATestEvent();
        Observable noop = new Observable();
        when(handler.getHandledEvents()).thenReturn(List.of("test.event"));
        assertNotNull(userMessagingService);
        userMessagingService.registerHandler(handler);
        when(entityManager.getTool(any())).thenReturn(Optional.of("sakai.assignments"));

        Site site = mock(Site.class);
        when(site.isPublished()).thenReturn(true);

        try {
            when(siteService.getSite(event.getContext())).thenReturn(site);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // WHEN
        ((UserMessagingServiceImpl) AopTestUtils.getTargetObject(userMessagingService)).update(noop, event);
        // THEN
        verify(handler).handleEvent(event);
    }

    @Test
    public void givenARegisteredHandler_whenNonMatchingEventOccurs_thenTheHandlerDoesNotReceiveIt() {
        // GIVEN
        ((UserMessagingServiceImpl) AopTestUtils.getTargetObject(userMessagingService)).init();
        UserNotificationHandler handler = mock(UserNotificationHandler.class);
        Event event = ATestEvent();
        Observable noop = new Observable();
        when(handler.getHandledEvents()).thenReturn(List.of("other.event"));
        userMessagingService.registerHandler(handler);
        // WHEN
        ((UserMessagingServiceImpl) AopTestUtils.getTargetObject(userMessagingService)).update(noop, event);
        // THEN
        verify(handler, never()).handleEvent(event);
    }

    @Test
    public void givenAUnregisteredHandler_whenAMatchingEventOccurs_thenTheHandlerDoesNotReceiveIt() {
        // GIVEN
        ((UserMessagingServiceImpl) AopTestUtils.getTargetObject(userMessagingService)).init();
        UserNotificationHandler handler = mock(UserNotificationHandler.class);
        Event event = ATestEvent();
        Observable noop = new Observable();
        when(handler.getHandledEvents()).thenReturn(List.of("test.event"));
        userMessagingService.registerHandler(handler);
        userMessagingService.unregisterHandler(handler);
        // WHEN
        ((UserMessagingServiceImpl) AopTestUtils.getTargetObject(userMessagingService)).update(noop, event);
        // THEN
        verify(handler, never()).handleEvent(event);
    }

    @Test
    public void givenARegisteredHandler_whenWeRegisterForTheSameEvent_thenTheNewHandlerReceivesIt() {
        // GIVEN
        ((UserMessagingServiceImpl) AopTestUtils.getTargetObject(userMessagingService)).init();
        UserNotificationHandler handlerOne = mock(UserNotificationHandler.class);
        UserNotificationHandler handlerTwo = mock(UserNotificationHandler.class);
        Event event = ATestEvent();
        Observable noop = new Observable();
        when(handlerOne.getHandledEvents()).thenReturn(List.of("test.event"));
        when(handlerTwo.getHandledEvents()).thenReturn(List.of("test.event"));
        userMessagingService.registerHandler(handlerOne);
        userMessagingService.registerHandler(handlerTwo);

        Site site = mock(Site.class);
        when(site.isPublished()).thenReturn(true);

        try {
            when(siteService.getSite(event.getContext())).thenReturn(site);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // WHEN
        ((UserMessagingServiceImpl) AopTestUtils.getTargetObject(userMessagingService)).update(noop, event);
        // THEN
        verify(handlerTwo).handleEvent(event);
    }

    // Note that this design is not necessarily intentional. It MAY be sensible for multiple handlers to
    // receive an event. However, the current design is that a single handler may be registered at a time.
    @Test
    public void givenARegisteredHandler_whenWeRegisterForTheSameEvent_thenTheOldHandlerDoesNotReceiveIt() {
        // GIVEN
        ((UserMessagingServiceImpl) AopTestUtils.getTargetObject(userMessagingService)).init();
        UserNotificationHandler handlerOne = mock(UserNotificationHandler.class);
        UserNotificationHandler handlerTwo = mock(UserNotificationHandler.class);
        Event event = ATestEvent();
        Observable noop = new Observable();
        when(handlerOne.getHandledEvents()).thenReturn(List.of("test.event"));
        when(handlerTwo.getHandledEvents()).thenReturn(List.of("test.event"));
        userMessagingService.registerHandler(handlerOne);

        Site site = mock(Site.class);
        when(site.isPublished()).thenReturn(true);

        try {
            when(siteService.getSite(event.getContext())).thenReturn(site);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // WHEN
        userMessagingService.registerHandler(handlerTwo);
        ((UserMessagingServiceImpl) AopTestUtils.getTargetObject(userMessagingService)).update(noop, event);
        // THEN
        verify(handlerOne, never()).handleEvent(event);
    }

    @Test
    public void givenADifferentHandlerIsRegisteredForAnEvent_whenWeUnregisterForThatEvent_thenTheOldHandlerStillReceivesIt() {
        // GIVEN
        ((UserMessagingServiceImpl) AopTestUtils.getTargetObject(userMessagingService)).init();
        UserNotificationHandler handlerOne = mock(UserNotificationHandler.class);
        UserNotificationHandler handlerTwo = mock(UserNotificationHandler.class);
        Event event = ATestEvent();
        Observable noop = new Observable();
        when(handlerOne.getHandledEvents()).thenReturn(List.of("test.event"));
        when(handlerTwo.getHandledEvents()).thenReturn(List.of("test.event"));
        userMessagingService.registerHandler(handlerOne);

        when(entityManager.getTool(any())).thenReturn(Optional.of("sakai.assignments"));
        Site site = mock(Site.class);
        when(site.isPublished()).thenReturn(true);

        try {
            when(siteService.getSite(any())).thenReturn(site);
        } catch (Exception e) {
        }

        // WHEN
        userMessagingService.unregisterHandler(handlerTwo);
        ((UserMessagingServiceImpl) AopTestUtils.getTargetObject(userMessagingService)).update(noop, event);
        // THEN
        verify(handlerOne).handleEvent(event);
    }

    private Event ATestEvent() {
        Event event = mock(Event.class);
        when(event.getEvent()).thenReturn("test.event");
        when(event.getResource()).thenReturn("no.resource");
        when(event.getContext()).thenReturn("/no/context");
        when(event.getUserId()).thenReturn("testuser");
        when(event.getEventTime()).thenReturn(new Date());
        return event;
    }
}
