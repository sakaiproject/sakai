/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
package org.sakaiproject.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.ClosingException;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.security.Principal;

import static org.mockito.Mockito.*;

/**
 * Created by buckett on 30/09/2014.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ComponentManager.class)
public class RequestFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private ThreadLocalManager threadLocalManager;

    @Mock
    private ServerConfigurationService serverConfigurationService;

    @Mock
    private Session session;

    private RequestFilter filter;

    @Before
    public void setUp() {
        System.setProperty(RequestFilter.SAKAI_SERVERID, "server1");
        PowerMockito.mockStatic(ComponentManager.class);
        when(ComponentManager.get(SessionManager.class)).thenReturn(sessionManager);
        when(ComponentManager.get(ThreadLocalManager.class)).thenReturn(threadLocalManager);
        when(ComponentManager.get(ServerConfigurationService.class)).thenReturn(serverConfigurationService);
        filter = new RequestFilter();
    }

    @Test
    public void testAssureSession() {
        // Basic simple test.
        when(sessionManager.startSession()).thenReturn(session);

        filter.assureSession(request, response);

        verify(sessionManager).startSession();
        verify(response).addHeader(eq("Set-Cookie"), anyString());
        verify(sessionManager, never()).makeSessionId(any(HttpServletRequest.class), any(Principal.class));
        verify(sessionManager).setCurrentSession(session);
    }

    @Test
    public void testAssureSessionCookieLookup() {
        // Check when we have a session that it gets extracted from the cookie ok.
        setupCookieSession();

        filter.assureSession(request, response);

        verify(sessionManager, never()).startSession();
        verify(session).setActive();
    }

    @Test
    public void testAssureSessionAutoParam() {
        // Check that we don't flag as active a session that is an automatic request.
        setupCookieSession();
        when(request.getParameter(RequestFilter.PARAM_AUTO)).thenReturn("true");

        filter.assureSession(request, response);

        verify(sessionManager).getSession("session1");
        verify(session, never()).setActive();
    }

    @Test
    public void testAssureSessionPrincipal() {
        setupPrincipal();
        filter.m_checkPrincipal = true;

        filter.assureSession(request, response);

        verify(sessionManager).getSession("principalSession");
        // principal authenticated sessions shouldn't set a cookie initially
        verify(response, never()).addCookie(any(Cookie.class));
        verify(response, never()).addHeader(eq("Set-Cookie"), anyString());
    }

    @Test
    public void testAssureSessionParameterId() {
        // Check we can pass a session ID parameter
        filter.m_sessionParamAllow = true;
        when(request.getParameter(RequestFilter.ATTR_SESSION)).thenReturn("paramSession");
        when(sessionManager.getSession("paramSession")).thenReturn(session);
        when(session.getUserId()).thenReturn("userId");

        filter.assureSession(request, response);

        verify(session).setActive();
    }

    @Test
    public void testAssureSessionParameterIdNoUser() {
        // Check we can pass a session ID parameter, but when the session has no user it gets thrown away.
        filter.m_sessionParamAllow = true;
        when(request.getParameter(RequestFilter.ATTR_SESSION)).thenReturn("paramSession");
        when(sessionManager.getSession("paramSession")).thenReturn(session);
        Session newSession = mock(Session.class);
        when(sessionManager.startSession()).thenReturn(newSession);

        filter.assureSession(request, response);

        // Existing session couldn't get taken over.
        verify(session, never()).setActive();
        verify(sessionManager).startSession();
    }

    @Test
    public void testAssureSessionShutdownExisting() {
        // When we're in shutdown existing sessions continue to work.
        setupCookieSession();
        when(sessionManager.startSession()).thenThrow(new ClosingException());
        filter.assureSession(request, response);
    }

    @Test(expected = ClosingException.class)
    public void testAssureSessionShutdownNew() {
        // When we're in shutdown new sessions are rejected.
        when(sessionManager.startSession()).thenThrow(new ClosingException());
        filter.assureSession(request, response);

    }

    private void setupCookieSession() {
        Cookie cookie = mock(Cookie.class);
        when (cookie.getName()).thenReturn("JSESSIONID");
        when (cookie.getValue()).thenReturn("session1.server1");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when (sessionManager.getSession("session1")).thenReturn(session);
    }

    private void setupPrincipal() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("name");
        when(request.getUserPrincipal()).thenReturn(principal);
        when(sessionManager.makeSessionId(request, principal)).thenReturn("principalSession");
        when(sessionManager.getSession("principalSession")).thenReturn(session);
    }

}
