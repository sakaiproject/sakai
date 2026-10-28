/*
 * Copyright (c) 2026 The Apereo Foundation
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://opensource.org/licenses/ECL-2.0
 */
package org.sakaiproject.presence.impl;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;

import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PresenceServiceTestConfiguration {
    @Bean
    public ClusterPresenceService presenceService() {
        ClusterPresenceService service = new ClusterPresenceService();
        service.setSqlService(sqlService());
        service.setSessionManager(sessionManager());
        service.setUsageSessionService(usageSessionService());
        service.setEventTrackingService(eventTrackingService());
        // Use the real storage without starting the unrelated maintenance thread in init().
        service.m_storage = service.newStorage();
        return service;
    }

    @Bean
    public SqlService sqlService() {
        SqlService service = mock(SqlService.class);
        when(service.dbWrite(anyString(), any(Object[].class))).thenReturn(true);
        return service;
    }

    @Bean
    public SessionManager sessionManager() {
        SessionManager manager = mock(SessionManager.class);
        Session session = mock(Session.class);
        ToolSession toolSession = toolSession();
        when(session.getToolSession(BasePresenceService.SESSION_KEY)).thenReturn(toolSession);
        when(manager.getCurrentSession()).thenReturn(session);
        return manager;
    }

    @Bean
    public ToolSession toolSession() {
        ToolSession session = mock(ToolSession.class);
        when(session.getAttributeNames()).thenAnswer(invocation -> Collections.emptyEnumeration());
        return session;
    }

    @Bean
    public UsageSession usageSession() {
        UsageSession session = mock(UsageSession.class);
        when(session.getId()).thenReturn("usage-session");
        when(session.getServer()).thenReturn("test-server");
        when(session.getUserId()).thenReturn("real-user");
        return session;
    }

    @Bean
    public UsageSessionService usageSessionService() {
        UsageSessionService service = mock(UsageSessionService.class);
        UsageSession session = usageSession();
        when(service.getSession()).thenReturn(session);
        return service;
    }

    @Bean
    public EventTrackingService eventTrackingService() {
        EventTrackingService service = mock(EventTrackingService.class);
        SessionManager manager = sessionManager();
        when(service.newEvent(anyString(), anyString(), eq(true))).thenAnswer(invocation -> {
            Event event = mock(Event.class);
            String userId = manager.getCurrentSessionUserId();
            when(event.getUserId()).thenReturn(userId == null ? EventTrackingService.UNKNOWN_USER : userId);
            when(event.getEvent()).thenReturn(invocation.getArgument(0));
            when(event.getResource()).thenReturn(invocation.getArgument(1));
            when(event.getEventTime()).thenReturn(new Date());
            return event;
        });
        return service;
    }
}
