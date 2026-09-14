/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.tool.mvc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

public class SakaiCsrfTokensTest {

    private static final String TOKEN = "csrf-token";

    private Session session;
    private SessionManager sessionManager;
    private SakaiCsrfTokens csrfTokens;

    @Before
    public void setUp() {
        session = mock(Session.class);
        sessionManager = mock(SessionManager.class);
        when(sessionManager.getCurrentSession()).thenReturn(session);
        when(session.getAttribute(UsageSessionService.SAKAI_CSRF_SESSION_ATTRIBUTE)).thenReturn(TOKEN);
        csrfTokens = new SakaiCsrfTokens(sessionManager);
    }

    @Test
    public void readsAndMatchesTheCurrentSakaiSessionToken() {
        assertTrue(csrfTokens.matches(TOKEN));
    }

    @Test
    public void rejectsMissingAndMismatchedTokens() {
        assertFalse(csrfTokens.matches(null));
        assertFalse(csrfTokens.matches("wrong-token"));
    }

    @Test
    public void hasNoTokenWithoutACurrentSession() {
        when(sessionManager.getCurrentSession()).thenReturn(null);

        assertNull(csrfTokens.currentToken());
        assertFalse(csrfTokens.matches(TOKEN));
    }
}
