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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.ClosingException;
import org.sakaiproject.tool.api.RebuildBreakdownService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class RequestFilterTest {

    private MockedStatic<ComponentManager> componentManagerMock;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private ServerConfigurationService serverConfigurationService;
    @Mock private Session session;
    @Mock private SessionManager sessionManager;
    @Mock private ThreadLocalManager threadLocalManager;
    @Mock private RebuildBreakdownService rebuildBreakdownService;

    private RequestFilter filter;

    @Before
    public void setUp() {
        System.setProperty(RequestFilter.SAKAI_SERVERID, "server1");

        componentManagerMock = Mockito.mockStatic(ComponentManager.class);
        componentManagerMock.when(() -> ComponentManager.get(SessionManager.class)).thenReturn(sessionManager);
        componentManagerMock.when(() -> ComponentManager.get(ThreadLocalManager.class)).thenReturn(threadLocalManager);
        componentManagerMock.when(() -> ComponentManager.get(ServerConfigurationService.class)).thenReturn(serverConfigurationService);
        componentManagerMock.when(() -> ComponentManager.get(RebuildBreakdownService.class)).thenReturn(rebuildBreakdownService);

        filter = new RequestFilter();
    }

    @After
    public void tearDown() {
        if (componentManagerMock != null) {
            componentManagerMock.close();
        }
    }

    @Test
    public void testAssureSession() {
        // Basic simple test.
        Mockito.when(sessionManager.startSession()).thenReturn(session);

        filter.assureSession(request, response);

        Mockito.verify(sessionManager).startSession();
        Mockito.verify(response).addHeader(Mockito.eq("Set-Cookie"), Mockito.anyString());
        Mockito.verify(sessionManager, Mockito.never()).makeSessionId(Mockito.any(HttpServletRequest.class), Mockito.any(Principal.class));
        Mockito.verify(sessionManager).setCurrentSession(session);
    }

    @Test
    public void testAssureSessionCookieLookup() {
        // Check when we have a session that it gets extracted from the cookie ok.
        setupCookieSession();

        filter.assureSession(request, response);

        Mockito.verify(sessionManager, Mockito.never()).startSession();
        Mockito.verify(session).setActive();
    }

    @Test
    public void testAssureSessionAutoParam() {
        // Check that we don't flag as active a session that is an automatic request.
        setupCookieSession();
        Mockito.when(request.getParameter(RequestFilter.PARAM_AUTO)).thenReturn("true");

        filter.assureSession(request, response);

        Mockito.verify(sessionManager).getSession("session1");
        Mockito.verify(session, Mockito.never()).setActive();
    }

    @Test
    public void testAssureSessionPrincipal() {
        setupPrincipal();
        filter.m_checkPrincipal = true;

        filter.assureSession(request, response);

        Mockito.verify(sessionManager).getSession("principalSession");
        // principal authenticated sessions shouldn't set a cookie initially
        Mockito.verify(response, Mockito.never()).addCookie(Mockito.any(Cookie.class));
        Mockito.verify(response, Mockito.never()).addHeader(Mockito.eq("Set-Cookie"), Mockito.anyString());
    }

    @Test
    public void testAssureSessionParameterId() {
        // Check we can pass a session ID parameter
        filter.m_sessionParamAllow = true;
        Mockito.when(request.getParameter(RequestFilter.ATTR_SESSION)).thenReturn("paramSession");
        Mockito.when(sessionManager.getSession("paramSession")).thenReturn(session);
        Mockito.when(session.getUserId()).thenReturn("userId");

        filter.assureSession(request, response);

        Mockito.verify(session).setActive();
    }

    @Test
    public void testAssureSessionParameterIdNoUser() {
        // Check we can pass a session ID parameter, but when the session has no user it gets thrown away.
        filter.m_sessionParamAllow = true;
        Mockito.when(request.getParameter(RequestFilter.ATTR_SESSION)).thenReturn("paramSession");
        Mockito.when(sessionManager.getSession("paramSession")).thenReturn(session);
        Session newSession = Mockito.mock(Session.class);
        Mockito.when(sessionManager.startSession()).thenReturn(newSession);
        Mockito.when(sessionManager.startSession("paramSession")).thenReturn(session);
        Mockito.when(rebuildBreakdownService.rebuildSession(session)).thenReturn(false);

        filter.assureSession(request, response);

        // Existing session couldn't get taken over.
        Mockito.verify(session, Mockito.never()).setActive();
        Mockito.verify(sessionManager).startSession();
    }

    @Test
    public void testAssureSessionShutdownExisting() {
        // When we're in shutdown existing sessions continue to work.
        setupCookieSession();
        filter.assureSession(request, response);
    }

    @Test(expected = ClosingException.class)
    public void testAssureSessionShutdownNew() {
        // When we're in shutdown new sessions are rejected.
        Mockito.when(sessionManager.startSession()).thenThrow(new ClosingException());
        filter.assureSession(request, response);

    }

    @Test
    public void testIsTomcatParseFailure() {
        assertFalse(filter.isTomcatParseFailure(null));
        assertFalse(filter.isTomcatParseFailure(false));
        assertFalse(filter.isTomcatParseFailure("false"));
        assertTrue(filter.isTomcatParseFailure(true));
        assertTrue(filter.isTomcatParseFailure("true"));
        assertTrue(filter.isTomcatParseFailure("TRUE"));
    }

    @Test
    public void testSurfaceTomcatParameterParseFailure() {
        Mockito.when(request.getAttribute("org.apache.catalina.parameter_parse_failed")).thenReturn(true);
        Mockito.when(request.getAttribute("org.apache.catalina.parameter_parse_failed_reason")).thenReturn("TOO_MANY_PARTS");
        Mockito.when(request.getMethod()).thenReturn("POST");
        Mockito.when(request.getRequestURI()).thenReturn("/samigo-app/jsf/delivery/deliverAssessment.faces");
        Mockito.when(request.getQueryString()).thenReturn(null);
        Mockito.when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        filter.surfaceTomcatParameterParseFailure(request);

        Mockito.verify(request).setAttribute(RequestFilter.ATTR_PARAMETER_PARSE_FAILED_REASON, "TOO_MANY_PARTS");
        Mockito.verify(request).setAttribute(RequestFilter.ATTR_PARAMETER_PARSE_FAILED_REPORTED, Boolean.TRUE);
        Mockito.verify(session, Mockito.never()).setAttribute(Mockito.eq("userWarning"), Mockito.any());
    }

    @Test
    public void testDoFilterSurfacesTomcatParameterParseFailureWhenChainThrows() throws Exception {
        RequestFilter testFilter = Mockito.spy(new RequestFilter());
        FilterChain chain = Mockito.mock(FilterChain.class);

        setupDoFilterRequest();
        Mockito.doNothing().when(testFilter).handleCharacterEncoding(request, response);
        Mockito.doReturn(request).when(testFilter).handleFileUpload(Mockito.eq(request), Mockito.eq(response), Mockito.anyList());
        Mockito.doReturn(session).when(testFilter).assureSession(request, response);
        Mockito.doNothing().when(testFilter).surfaceTomcatParameterParseFailure(request);
        Mockito.doReturn(request).when(testFilter).preProcessRequest(session, request);
        Mockito.doReturn(null).when(testFilter).detectToolPlacement(session, request);
        Mockito.doReturn(response).when(testFilter).preProcessResponse(session, request, response);
        Mockito.doThrow(new ServletException("boom")).when(chain).doFilter(request, response);

        testFilter.doFilter(request, response, chain);

        Mockito.verify(testFilter, Mockito.times(2)).surfaceTomcatParameterParseFailure(request);
    }

    @Test
    public void testDoFilterSurfacesTomcatParameterParseFailureWhenTerracottaChainThrows() throws Exception {
        RequestFilter testFilter = Mockito.spy(new RequestFilter());
        FilterChain chain = Mockito.mock(FilterChain.class);
        testFilter.TERRACOTTA_CLUSTER = true;

        setupDoFilterRequest();
        Mockito.doNothing().when(testFilter).handleCharacterEncoding(request, response);
        Mockito.doReturn(request).when(testFilter).handleFileUpload(Mockito.eq(request), Mockito.eq(response), Mockito.anyList());
        Mockito.doReturn(session).when(testFilter).assureSession(request, response);
        Mockito.doNothing().when(testFilter).surfaceTomcatParameterParseFailure(request);
        Mockito.doReturn(request).when(testFilter).preProcessRequest(session, request);
        Mockito.doReturn(null).when(testFilter).detectToolPlacement(session, request);
        Mockito.doReturn(response).when(testFilter).preProcessResponse(session, request, response);
        Mockito.doThrow(new ServletException("boom")).when(chain).doFilter(request, response);

        try {
            testFilter.doFilter(request, response, chain);
        } catch (ServletException e) {
            Mockito.verify(testFilter, Mockito.times(2)).surfaceTomcatParameterParseFailure(request);
            return;
        }

        throw new AssertionError("Expected ServletException");
    }

    private void setupCookieSession() {
        Cookie cookie = Mockito.mock(Cookie.class);
        Mockito.when (cookie.getName()).thenReturn("JSESSIONID");
        Mockito.when (cookie.getValue()).thenReturn("session1.server1");
        Mockito.when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        Mockito.when (sessionManager.getSession("session1")).thenReturn(session);
    }

    private void setupPrincipal() {
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn("name");
        Mockito.when(request.getUserPrincipal()).thenReturn(principal);
        Mockito.when(sessionManager.makeSessionId(request, principal)).thenReturn("principalSession");
        Mockito.when(sessionManager.getSession("principalSession")).thenReturn(session);
    }

    private void setupDoFilterRequest() {
        Mockito.when(request.getMethod()).thenReturn("POST");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/tool"));
        Mockito.when(request.getRequestURI()).thenReturn("/tool");
        Mockito.when(request.getScheme()).thenReturn("http");
        Mockito.when(request.getServerPort()).thenReturn(80);
        Mockito.when(request.getServerName()).thenReturn("localhost");
        Mockito.when(request.isSecure()).thenReturn(false);
    }

}
