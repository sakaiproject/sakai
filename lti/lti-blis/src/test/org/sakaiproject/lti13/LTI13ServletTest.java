/**
 * Copyright (c) 2026 The Apereo Foundation
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
package org.sakaiproject.lti13;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.net.httpserver.HttpServer;
import jakarta.servlet.Servlet;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class LTI13ServletTest {

    private static final String CANARY = "LTI-PROBE-CANARY-123";

    private HttpServer target;
    private final AtomicInteger targetRequests = new AtomicInteger();
    private final AtomicInteger redirectRequests = new AtomicInteger();
    private String targetUrl;

    @Before
    public void setUp() throws Exception {
        target = HttpServer.create(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 0), 0);
        targetUrl = "http://127.0.0.1:" + target.getAddress().getPort();
        target.createContext("/probe.json", exchange -> {
            targetRequests.incrementAndGet();
            byte[] body = ("{\"secret\":\"" + CANARY + "\"}").getBytes(StandardCharsets.UTF_8);
            try (exchange) {
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
            }
        });
        target.createContext("/redirect", exchange -> {
            redirectRequests.incrementAndGet();
            try (exchange) {
                exchange.getResponseHeaders().set("Location", targetUrl + "/probe.json");
                exchange.sendResponseHeaders(302, -1);
            }
        });
        target.start();
    }

    @After
    public void tearDown() {
        if (target != null) {
            target.stop(0);
        }
    }

    @Test
    public void proxyRejectsAuthenticatedRequestsWithoutFetchingTarget() throws Exception {
        assertProxyRejected("GET", "/probe.json", studentSession());
    }

    @Test
    public void proxyRejectsRedirectsWithoutFetchingEitherTarget() throws Exception {
        assertProxyRejected("GET", "/redirect", studentSession());
    }

    @Test
    public void proxyRejectsHeadRequestsWithoutFetchingTarget() throws Exception {
        assertProxyRejected("HEAD", "/probe.json", studentSession());
    }

    @Test
    public void proxyRejectsUnauthenticatedRequestsWithoutFetchingTarget() throws Exception {
        assertProxyRejected("GET", "/probe.json", null);
    }

    private Session studentSession() {
        Session session = mock(Session.class);
        when(session.getUserId()).thenReturn("student");
        return session;
    }

    private void assertProxyRejected(String method, String path, Session session) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, "/imsblis/lti13/proxy");
        request.addParameter("proxyUrl", targetUrl + path);
        MockHttpServletResponse response = new MockHttpServletResponse();
        Servlet servlet = new LTI13Servlet();

        // Mock kernel boundaries while exercising the real servlet entry point.
        try (MockedStatic<SessionManager> sessions = mockStatic(SessionManager.class);
                MockedStatic<ServerConfigurationService> configuration = mockStatic(ServerConfigurationService.class)) {
            sessions.when(SessionManager::getCurrentSession).thenReturn(session);
            servlet.service(request, response);
        }

        assertEquals("The proxy must never contact the supplied URL", 0, targetRequests.get());
        assertEquals("The proxy must never contact a redirect URL", 0, redirectRequests.get());
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        assertFalse(response.getContentAsString().contains(CANARY));
    }
}
