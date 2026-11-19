/*
 * Copyright (c) 2026 The Apereo Foundation
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://opensource.org/licenses/ECL-2.0
 */
package org.sakaiproject.presence.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.api.SimpleEvent;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.impl.ClusterEventTracking;
import org.sakaiproject.presence.api.PresenceService;
import org.sakaiproject.tool.api.SessionBindingListener;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = PresenceServiceTestConfiguration.class)
public class PresenceServiceTest {
    @Autowired private PresenceService presenceService;
    @Autowired private EventTrackingService eventTrackingService;
    @Autowired private SessionManager sessionManager;
    @Autowired private ToolSession toolSession;
    @Autowired private UsageSession usageSession;
    @Autowired private ClusterEventTracking eventFactory;

    @Before
    public void clearBoundaryCalls() {
        clearInvocations(eventTrackingService, toolSession);
    }

    @Test
    public void realEventCanBeCopiedBeforePostingAndReceivesTimeWhenPosted() {
        when(sessionManager.getCurrentSessionUserId()).thenReturn("real-user");
        Event event = eventFactory.newEvent(PresenceService.EVENT_ABSENCE, "/presence/site-presence",
                null, true, NotificationService.NOTI_OPTIONAL, true);
        assertNull(event.getEventTime());
        SimpleEvent copy = new SimpleEvent(event, "test-server");
        assertNull(copy.getEventTime());
        assertEquals("real-user", copy.getUserId());

        eventFactory.post(event, usageSession);
        assertNotNull(event.getEventTime());
        assertEquals(usageSession.getId(), event.getSessionId());
        event.getEventTime().setTime(0);
        assertNotEquals(0L, event.getEventTime().getTime());
    }

    @Test
    public void cleanupWithoutCurrentUserRetainsBeginUser() {
        assertCleanupUser("real-user", null, "real-user");
    }

    @Test
    public void cleanupRetainsImpersonatedUserInsteadOfSessionOwner() {
        assertCleanupUser("impersonated-user", "real-user", "impersonated-user");
    }

    @Test
    public void anonymousPresenceDoesNotBecomeCurrentUserDuringCleanup() {
        assertCleanupUser(null, "real-user", EventTrackingService.UNKNOWN_USER);
    }

    private void assertCleanupUser(String beginUser, String cleanupUser, String expectedUser) {
        when(sessionManager.getCurrentSessionUserId()).thenReturn(beginUser);
        presenceService.setPresence("site-presence");

        ArgumentCaptor<Object> binding = ArgumentCaptor.forClass(Object.class);
        verify(toolSession).setAttribute(eq("site-presence"), binding.capture());
        when(sessionManager.getCurrentSessionUserId()).thenReturn(cleanupUser);
        ((SessionBindingListener) binding.getValue()).valueUnbound(null);

        ArgumentCaptor<Event> events = ArgumentCaptor.forClass(Event.class);
        verify(eventTrackingService, times(2)).post(events.capture(), eq(usageSession));
        Event end = events.getAllValues().get(1);
        assertEquals(PresenceService.EVENT_ABSENCE, end.getEvent());
        assertEquals(presenceService.presenceReference("site-presence"), end.getResource());
        assertEquals(expectedUser, end.getUserId());
        assertEquals("real-user", usageSession.getUserId());
    }
}
