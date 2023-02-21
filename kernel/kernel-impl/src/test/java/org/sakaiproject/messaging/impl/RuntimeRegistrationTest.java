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

import org.apache.ignite.IgniteCluster;
import org.apache.ignite.IgniteMessaging;
import org.apache.ignite.cluster.ClusterGroup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.ignite.EagerIgniteSpringBean;
import org.sakaiproject.messaging.api.UserNotificationHandler;

import java.util.Date;
import java.util.List;
import java.util.Observable;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RuntimeRegistrationTest {
    @Mock public EventTrackingService eventTrackingService;
    @Mock public EagerIgniteSpringBean ignite;
    @Mock public IgniteCluster igniteCluster;
    @Mock public ClusterGroup clusterGroup;
    @Mock public IgniteMessaging messaging;
    @Mock public ServerConfigurationService serverConfigurationService;

    UserMessagingServiceImpl userMessagingService;

    @Before
    public void setup() {
        userMessagingService = new UserMessagingServiceImpl();
        userMessagingService.eventTrackingService = eventTrackingService;
        userMessagingService.serverConfigurationService = serverConfigurationService;
        userMessagingService.ignite = ignite;
        when(serverConfigurationService.getBoolean(eq("portal.bullhorns.enabled"), anyBoolean())).thenReturn(true);
        when(ignite.cluster()).thenReturn(igniteCluster);
        when(igniteCluster.forLocal()).thenReturn(clusterGroup);
        when(ignite.message(any())).thenReturn(messaging);
    }

    @Test
    public void givenNoHandlers_whenInitializing_thenItStartsUp() {
        assertThatNoException().isThrownBy(() -> {
            userMessagingService.init();
        });
    }

    @Test
    public void givenARegisteredHandler_whenMatchingEventOccurs_thenTheHandlerReceivesIt() {
        // GIVEN
        userMessagingService.init();
        UserNotificationHandler handler = mock(UserNotificationHandler.class);
        Event event = ATestEvent();
        Observable noop = new Observable();
        when(handler.getHandledEvents()).thenReturn(List.of("test.event"));
        userMessagingService.registerHandler(handler);
        // WHEN
        userMessagingService.update(noop, event);
        // THEN
        verify(handler).handleEvent(event);
    }

    @Test
    public void givenARegisteredHandler_whenNonMatchingEventOccurs_thenTheHandlerDoesNotReceiveIt() {
        // GIVEN
        userMessagingService.init();
        UserNotificationHandler handler = mock(UserNotificationHandler.class);
        Event event = ATestEvent();
        Observable noop = new Observable();
        when(handler.getHandledEvents()).thenReturn(List.of("other.event"));
        userMessagingService.registerHandler(handler);
        // WHEN
        userMessagingService.update(noop, event);
        // THEN
        verify(handler, never()).handleEvent(event);
    }

    @Test
    public void givenAUnregisteredHandler_whenAMatchingEventOccurs_thenTheHandlerDoesNotReceiveIt() {
        // GIVEN
        userMessagingService.init();
        UserNotificationHandler handler = mock(UserNotificationHandler.class);
        Event event = ATestEvent();
        Observable noop = new Observable();
        when(handler.getHandledEvents()).thenReturn(List.of("test.event"));
        userMessagingService.registerHandler(handler);
        userMessagingService.unregisterHandler(handler);
        // WHEN
        userMessagingService.update(noop, event);
        // THEN
        verify(handler, never()).handleEvent(event);
    }

    @Test
    public void givenARegisteredHandler_whenWeRegisterForTheSameEvent_thenTheNewHandlerReceivesIt() {
        // GIVEN
        userMessagingService.init();
        UserNotificationHandler handlerOne = mock(UserNotificationHandler.class);
        UserNotificationHandler handlerTwo = mock(UserNotificationHandler.class);
        Event event = ATestEvent();
        Observable noop = new Observable();
        when(handlerOne.getHandledEvents()).thenReturn(List.of("test.event"));
        when(handlerTwo.getHandledEvents()).thenReturn(List.of("test.event"));
        userMessagingService.registerHandler(handlerOne);
        userMessagingService.registerHandler(handlerTwo);
        // WHEN
        userMessagingService.update(noop, event);
        // THEN
        verify(handlerTwo).handleEvent(event);
    }

    // Note that this design is not necessarily intentional. It MAY be sensible for multiple handlers to
    // receive an event. However, the current design is that a single handler may be registered at a time.
    @Test
    public void givenARegisteredHandler_whenWeRegisterForTheSameEvent_thenTheOldHandlerDoesNotReceiveIt() {
        // GIVEN
        userMessagingService.init();
        UserNotificationHandler handlerOne = mock(UserNotificationHandler.class);
        UserNotificationHandler handlerTwo = mock(UserNotificationHandler.class);
        Event event = ATestEvent();
        Observable noop = new Observable();
        when(handlerOne.getHandledEvents()).thenReturn(List.of("test.event"));
        when(handlerTwo.getHandledEvents()).thenReturn(List.of("test.event"));
        userMessagingService.registerHandler(handlerOne);
        // WHEN
        userMessagingService.registerHandler(handlerTwo);
        userMessagingService.update(noop, event);
        // THEN
        verify(handlerOne, never()).handleEvent(event);
    }

    @Test
    public void givenADifferentHandlerIsRegisteredForAnEvent_whenWeUnregisterForThatEvent_thenTheOldHandlerStillReceivesIt() {
        // GIVEN
        userMessagingService.init();
        UserNotificationHandler handlerOne = mock(UserNotificationHandler.class);
        UserNotificationHandler handlerTwo = mock(UserNotificationHandler.class);
        Event event = ATestEvent();
        Observable noop = new Observable();
        when(handlerOne.getHandledEvents()).thenReturn(List.of("test.event"));
        when(handlerTwo.getHandledEvents()).thenReturn(List.of("test.event"));
        userMessagingService.registerHandler(handlerOne);
        // WHEN
        userMessagingService.unregisterHandler(handlerTwo);
        userMessagingService.update(noop, event);
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
