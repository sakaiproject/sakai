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
import org.sakaiproject.messaging.api.BullhornHandler;

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

    MessagingServiceImpl messagingService;

    @Before
    public void setup() {
        messagingService = new MessagingServiceImpl();
        messagingService.eventTrackingService = eventTrackingService;
        messagingService.serverConfigurationService = serverConfigurationService;
        messagingService.ignite = ignite;
        when(serverConfigurationService.getBoolean(eq("portal.bullhorns.enabled"), anyBoolean())).thenReturn(true);
        when(ignite.cluster()).thenReturn(igniteCluster);
        when(igniteCluster.forLocal()).thenReturn(clusterGroup);
        when(ignite.message(any())).thenReturn(messaging);
    }

    @Test
    public void givenNoHandlers_whenInitializing_thenItStartsUp() {
        assertThatNoException().isThrownBy(() -> {
            messagingService.init();
        });
    }

    @Test
    public void givenARegisteredHandler_whenMatchingEventOccurs_thenTheHandlerReceivesIt() {
        // GIVEN
        messagingService.init();
        BullhornHandler handler = mock(BullhornHandler.class);
        Event event = ATestEvent();
        Observable noop = new Observable();
        when(handler.getHandledEvents()).thenReturn(List.of("test.event"));
        messagingService.registerHandler(handler);
        // WHEN
        messagingService.update(noop, event);
        // THEN
        verify(handler).handleEvent(event);
    }

    @Test
    public void givenARegisteredHandler_whenNonMatchingEventOccurs_thenTheHandlerDoesNotReceiveIt() {
        // GIVEN
        messagingService.init();
        BullhornHandler handler = mock(BullhornHandler.class);
        Event event = ATestEvent();
        Observable noop = new Observable();
        when(handler.getHandledEvents()).thenReturn(List.of("other.event"));
        messagingService.registerHandler(handler);
        // WHEN
        messagingService.update(noop, event);
        // THEN
        verify(handler, never()).handleEvent(event);
    }

    @Test
    public void givenAUnregisteredHandler_whenAMatchingEventOccurs_thenTheHandlerDoesNotReceiveIt() {
        // GIVEN
        messagingService.init();
        BullhornHandler handler = mock(BullhornHandler.class);
        Event event = ATestEvent();
        Observable noop = new Observable();
        when(handler.getHandledEvents()).thenReturn(List.of("test.event"));
        messagingService.registerHandler(handler);
        messagingService.unregisterHandler(handler);
        // WHEN
        messagingService.update(noop, event);
        // THEN
        verify(handler, never()).handleEvent(event);
    }

    @Test
    public void givenARegisteredHandler_whenWeRegisterForTheSameEvent_thenTheNewHandlerReceivesIt() {
        // GIVEN
        messagingService.init();
        BullhornHandler handlerOne = mock(BullhornHandler.class);
        BullhornHandler handlerTwo = mock(BullhornHandler.class);
        Event event = ATestEvent();
        Observable noop = new Observable();
        when(handlerOne.getHandledEvents()).thenReturn(List.of("test.event"));
        when(handlerTwo.getHandledEvents()).thenReturn(List.of("test.event"));
        messagingService.registerHandler(handlerOne);
        messagingService.registerHandler(handlerTwo);
        // WHEN
        messagingService.update(noop, event);
        // THEN
        verify(handlerTwo).handleEvent(event);
    }

    // Note that this design is not necessarily intentional. It MAY be sensible for multiple handlers to
    // receive an event. However, the current design is that a single handler may be registered at a time.
    @Test
    public void givenARegisteredHandler_whenWeRegisterForTheSameEvent_thenTheOldHandlerDoesNotReceiveIt() {
        // GIVEN
        messagingService.init();
        BullhornHandler handlerOne = mock(BullhornHandler.class);
        BullhornHandler handlerTwo = mock(BullhornHandler.class);
        Event event = ATestEvent();
        Observable noop = new Observable();
        when(handlerOne.getHandledEvents()).thenReturn(List.of("test.event"));
        when(handlerTwo.getHandledEvents()).thenReturn(List.of("test.event"));
        messagingService.registerHandler(handlerOne);
        // WHEN
        messagingService.registerHandler(handlerTwo);
        messagingService.update(noop, event);
        // THEN
        verify(handlerOne, never()).handleEvent(event);
    }

    @Test
    public void givenADifferentHandlerIsRegisteredForAnEvent_whenWeUnregisterForThatEvent_thenTheOldHandlerStillReceivesIt() {
        // GIVEN
        messagingService.init();
        BullhornHandler handlerOne = mock(BullhornHandler.class);
        BullhornHandler handlerTwo = mock(BullhornHandler.class);
        Event event = ATestEvent();
        Observable noop = new Observable();
        when(handlerOne.getHandledEvents()).thenReturn(List.of("test.event"));
        when(handlerTwo.getHandledEvents()).thenReturn(List.of("test.event"));
        messagingService.registerHandler(handlerOne);
        // WHEN
        messagingService.unregisterHandler(handlerTwo);
        messagingService.update(noop, event);
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
