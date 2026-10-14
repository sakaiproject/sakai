/*
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
package org.sakaiproject.login.tool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.MockedStatic;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(Parameterized.class)
public class AuthnPortalTest {

    private static final String SERVER_URL = "https://sakai.example";
    private static final String LOGGED_OUT_URL = "https://trusted-logout.example/signed-out";

    @Parameterized.Parameters(name = "{0} {1}: return URLs {2}")
    public static Collection<Object[]> parameters() {
        Collection<Object[]> cases = new ArrayList<>();
        String[][] routes = {{"GET", "/login"}, {"POST", "/login"}, {"GET", "/logout"}, {"GET", "/"}};
        String[] rejected = {
            "https://evil.example/", "http://evil.example/", "HtTP://bestbuy.com", "HtTPs://bestbuy.com",
            "////bestbuy.com", "//bestbuy.com", "//bestbuy。com", "//bestbuy.com/..",
            "x00http://bestbuy.com", "\\\\x20http://bestbuy.com", "http\\\\x3A\\\\x2F\\\\x2Fbestbuy.com",
            "evil.com", "bestbuy.com", "aHR0cDovL3d3dy5obS5jb20", "", " https://sakai.example/portal",
            "https://sakai.example/portal\r\nLocation:https://evil.example/", "javascript:alert(1)",
            "https://sakai.example.evil.example/", "https://sakai.example@evil.example/",
            "https://evil.example@sakai.example/", "https://sakai.example:444/", "http://sakai.example/",
            "https://request-host.invalid/", "/\\evil.example", "https://sakai.example\\@evil.example/",
            "https:///evil.example", "https://sakai.example/%", "https://sakai.example:bad/"
        };
        String[][] accepted = {
            {SERVER_URL + "/samigo-app/servlet/Login?id=assessment&x=1", SERVER_URL + "/samigo-app/servlet/Login?id=assessment&x=1"},
            {"HTTPS://SAKAI.EXAMPLE:443/portal", "HTTPS://SAKAI.EXAMPLE:443/portal"},
            {"/portal", SERVER_URL + "/portal"},
            {"/", SERVER_URL + "/"}
        };
        for (String[] route : routes) {
            for (String url : rejected) {
                cases.add(new Object[] {route[0], route[1], new String[] {url}, null, 400, null});
            }
            for (String[] url : accepted) {
                cases.add(new Object[] {route[0], route[1], new String[] {url[0]}, null, 200, url[1]});
            }
            cases.add(new Object[] {route[0], route[1], new String[] {SERVER_URL + "/portal", "https://evil.example/"}, null, 400, null});
            cases.add(new Object[] {route[0], route[1], new String[] {"https://evil.example/", SERVER_URL + "/portal"}, null, 400, null});
            String defaultUrl = "/logout".equals(route[1]) ? LOGGED_OUT_URL : SERVER_URL + "/portal";
            cases.add(new Object[] {route[0], route[1], null, null, 200, defaultUrl});
            cases.add(new Object[] {route[0], route[1], null, LOGGED_OUT_URL, 200, LOGGED_OUT_URL});
        }
        return cases;
    }

    private final String method;
    private final String path;
    private final String[] urls;
    private final String previousDestination;
    private final int status;
    private final String destination;

    public AuthnPortalTest(String method, String path, String[] urls, String previousDestination, int status, String destination) {
        this.method = method;
        this.path = path;
        this.urls = urls;
        this.previousDestination = previousDestination;
        this.status = status;
        this.destination = destination;
    }

    @Test
    public void validatesRequestReturnUrlsAndPreservesTrustedDestinations() throws Exception {
        // These are kernel boundaries; exercise the real servlet through its public service method.
        Session session = mock(Session.class);
        ActiveTool login = mock(ActiveTool.class);
        Map<String, Object> attributes = new HashMap<>();
        if (previousDestination != null) {
            attributes.put(Tool.HELPER_DONE_URL, previousDestination);
        }
        when(session.getAttribute(anyString())).thenAnswer(invocation -> attributes.get(invocation.getArgument(0)));
        doAnswer(invocation -> {
            attributes.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(session).setAttribute(anyString(), any());

        try (MockedStatic<SessionManager> sessions = mockStatic(SessionManager.class);
                MockedStatic<ActiveToolManager> tools = mockStatic(ActiveToolManager.class);
                MockedStatic<ServerConfigurationService> config = mockStatic(ServerConfigurationService.class)) {
            sessions.when(SessionManager::getCurrentSession).thenReturn(session);
            tools.when(() -> ActiveToolManager.getActiveTool("sakai.login")).thenReturn(login);
            config.when(ServerConfigurationService::getServerUrl).thenReturn("https://request-host.invalid");
            config.when(() -> ServerConfigurationService.getString("serverUrl", "https://request-host.invalid")).thenReturn(SERVER_URL);
            config.when(ServerConfigurationService::getPortalUrl).thenReturn(SERVER_URL + "/portal");
            config.when(ServerConfigurationService::getLoggedOutUrl).thenReturn(LOGGED_OUT_URL);

            MockHttpServletRequest request = new MockHttpServletRequest(method, "/authn" + path);
            request.setContextPath("/authn");
            request.setPathInfo(path);
            if (urls != null) {
                request.addParameter("url", urls);
            }
            MockHttpServletResponse response = new MockHttpServletResponse();
            new AuthnPortal().service(request, response);

            assertEquals(status, response.getStatus());
            assertNull(response.getRedirectedUrl());
            assertEquals(destination, attributes.get(Tool.HELPER_DONE_URL));
            if (status == 400) {
                verifyNoInteractions(login);
            } else {
                String action = "/logout".equals(path) ? "/logout" : "/login";
                verify(login).help(request, response, "/authn" + action, "/logout".equals(path) ? "/logout" : null);
            }
        }
    }
}
