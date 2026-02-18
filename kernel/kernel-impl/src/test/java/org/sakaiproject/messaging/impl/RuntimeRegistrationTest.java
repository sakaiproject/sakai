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

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.messaging.api.UserMessagingService;
import org.sakaiproject.messaging.api.UserNotificationHandler;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.test.SakaiTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {UserMessagingServiceTestConfiguration.class})
public class RuntimeRegistrationTest extends SakaiTests {

    @Autowired private EntityManager entityManager;
    @Autowired private UserMessagingService userMessagingService;

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
            Assert.fail("Exception thrown: " + e);
        }

        // WHEN
        ((Observer) userMessagingService).update(noop, event);
        // THEN
        verify(handler).handleEvent(event);
        userMessagingService.unregisterHandler(handler);
    }

    @Test
    public void givenARegisteredHandler_whenNonMatchingEventOccurs_thenTheHandlerDoesNotReceiveIt() {
        // GIVEN
        UserNotificationHandler handler = mock(UserNotificationHandler.class);
        Event event = ATestEvent();
        Observable noop = new Observable();
        when(handler.getHandledEvents()).thenReturn(List.of("other.event"));
        userMessagingService.registerHandler(handler);
        // WHEN
        ((Observer) userMessagingService).update(noop, event);
        // THEN
        verify(handler, never()).handleEvent(event);
        userMessagingService.unregisterHandler(handler);
    }

    @Test
    public void givenAUnregisteredHandler_whenAMatchingEventOccurs_thenTheHandlerDoesNotReceiveIt() {
        // GIVEN
        UserNotificationHandler handler = mock(UserNotificationHandler.class);
        Event event = ATestEvent();
        Observable noop = new Observable();
        when(handler.getHandledEvents()).thenReturn(List.of("test.event"));
        userMessagingService.registerHandler(handler);
        userMessagingService.unregisterHandler(handler);
        // WHEN
        ((Observer) userMessagingService).update(noop, event);
        // THEN
        verify(handler, never()).handleEvent(event);
        userMessagingService.unregisterHandler(handler);
    }

    @Test
    public void givenARegisteredHandler_whenWeRegisterForTheSameEvent_thenTheOriginalHandlerReceivesIt() {
        // GIVEN
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
            Assert.fail("Exception thrown: " + e);
        }

        // WHEN
        ((Observer) userMessagingService).update(noop, event);

        // THEN
        verify(handlerOne).handleEvent(event);
        verify(handlerTwo, never()).handleEvent(event);
        userMessagingService.unregisterHandler(handlerOne);
    }

    @Test
    public void givenTwoHandlersForSameEvents_whenWeUnregisterOne_thenCanRegisterAnother() {
        // GIVEN
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
            Assert.fail("Exception thrown: " + e);
        }

        userMessagingService.unregisterHandler(handlerOne);
        userMessagingService.registerHandler(handlerTwo);

        // WHEN
        ((Observer) userMessagingService).update(noop, event);

        // THEN
        verify(handlerOne, never()).handleEvent(event);
        verify(handlerTwo).handleEvent(event);
        userMessagingService.unregisterHandler(handlerTwo);
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
