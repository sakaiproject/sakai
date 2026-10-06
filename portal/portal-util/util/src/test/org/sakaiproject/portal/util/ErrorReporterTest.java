/*
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.portal.util;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.api.FormattedText;

@RunWith(Parameterized.class)
public class ErrorReporterTest {

    @Parameterized.Parameters(name = "authenticated={0}, showDetails={1}")
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][] {{false, false}, {false, true}, {true, false}, {true, true}});
    }

    private final boolean authenticated;
    private final boolean showDetails;

    public ErrorReporterTest(boolean authenticated, boolean showDetails) {
        this.authenticated = authenticated;
        this.showDetails = showDetails;
    }

    @Test
    public void reportDoesNotDiscloseCredentialsInPageLogsOrEmail() throws Exception {
        List<String> secrets = List.of("cookie-credential-marker", "authorization-credential-marker",
            "parameter-credential-marker", "attribute-credential-marker", "session-credential-marker",
            "csrf-credential-marker");
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        StringWriter page = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(page));
        when(request.getLocale()).thenReturn(Locale.ENGLISH);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/portal/test-error");
        when(request.getRequestURL()).thenReturn(new StringBuffer("https://sakai.example/portal/test-error"));
        when(request.getQueryString()).thenReturn("sessionId=" + secrets.get(2));
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(List.of("Cookie", "Authorization")));
        when(request.getHeaders("Cookie")).thenReturn(Collections.enumeration(List.of(secrets.get(0))));
        when(request.getHeaders("Authorization")).thenReturn(Collections.enumeration(List.of(secrets.get(1))));
        when(request.getParameterNames()).thenReturn(Collections.enumeration(List.of("sessionId")));
        when(request.getParameterValues("sessionId")).thenReturn(new String[] {secrets.get(2)});
        when(request.getAttributeNames()).thenReturn(Collections.enumeration(List.of("sakai.session")));
        when(request.getAttribute("sakai.session")).thenReturn(secrets.get(3));
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttributeNames()).thenReturn(Collections.enumeration(
            List.of("sakai.locale." + secrets.get(4), "sakai.csrf.token")));
        when(session.getAttribute("sakai.csrf.token")).thenReturn(secrets.get(5));
        when(session.getCreationTime()).thenReturn(1L);
        when(session.getLastAccessedTime()).thenReturn(2L);

        IdManager ids = mock(IdManager.class);
        when(ids.createUuid()).thenReturn("bug-marker");
        UserTimeService time = mock(UserTimeService.class);
        when(time.shortPreciseLocalizedTimestamp(any(Instant.class), eq(Locale.ENGLISH))).thenReturn("report-time");
        UsageSessionService usage = mock(UsageSessionService.class);
        when(usage.getSessionId()).thenReturn(authenticated ? "usage-marker" : null);
        SessionManager sessions = mock(SessionManager.class);
        when(sessions.getCurrentSessionUserId()).thenReturn(authenticated ? "user-marker" : null);
        SecurityService security = mock(SecurityService.class);
        FormattedText formattedText = mock(FormattedText.class);
        when(formattedText.escapeHtml(anyString(), eq(false))).thenAnswer(
            invocation -> invocation.getArgument(0, String.class));
        ToolManager tools = mock(ToolManager.class);
        EmailService email = mock(EmailService.class);
        UserDirectoryService users = mock(UserDirectoryService.class);
        User user = mock(User.class);
        when(users.getUser("user-marker")).thenReturn(user);
        when(user.getEid()).thenReturn("user-marker");

        StringBuilder messages = new StringBuilder();
        Logger logger = (Logger) LogManager.getLogger(ErrorReporter.class);
        Level originalLevel = logger.getLevel();
        AbstractAppender appender = new AbstractAppender("credential-test", null,
                PatternLayout.createDefaultLayout(), false, Property.EMPTY_ARRAY) {
            @Override
            public void append(LogEvent event) {
                messages.append(event.getMessage().getFormattedMessage());
            }
        };
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.WARN);
        try (MockedStatic<ComponentManager> components = mockStatic(ComponentManager.class);
                MockedStatic<ServerConfigurationService> configuration = mockStatic(ServerConfigurationService.class)) {
            components.when(() -> ComponentManager.get(IdManager.class)).thenReturn(ids);
            components.when(() -> ComponentManager.get(UserTimeService.class)).thenReturn(time);
            components.when(() -> ComponentManager.get(UsageSessionService.class)).thenReturn(usage);
            components.when(() -> ComponentManager.get(SessionManager.class)).thenReturn(sessions);
            components.when(() -> ComponentManager.get(SecurityService.class)).thenReturn(security);
            components.when(() -> ComponentManager.get(FormattedText.class)).thenReturn(formattedText);
            components.when(() -> ComponentManager.get(ToolManager.class)).thenReturn(tools);
            components.when(() -> ComponentManager.get(EmailService.class)).thenReturn(email);
            components.when(() -> ComponentManager.get(UserDirectoryService.class)).thenReturn(users);
            configuration.when(() -> ServerConfigurationService.getPortalUrl()).thenReturn("https://sakai.example/portal");
            configuration.when(() -> ServerConfigurationService.getBoolean("portal.error.showdetail", false)).thenReturn(showDetails);
            configuration.when(() -> ServerConfigurationService.getString("portal.error.email")).thenReturn("support@example.invalid");

            new ErrorReporter().report(request, response, new IllegalStateException("problem-marker"));

            verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            assertEquals(authenticated, page.toString().contains("/portal/error-report"));
            assertEquals(authenticated, page.toString().contains("problemRequest"));
            assertTrue(messages.toString().contains("problem-marker"));
            ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
            verify(email).send(any(), eq("support@example.invalid"), anyString(), body.capture(),
                eq("support@example.invalid"), isNull(), isNull());
            assertTrue(body.getValue().contains("/portal/test-error"));
            for (String secret : secrets) {
                assertFalse("Page must not contain credentials", page.toString().contains(secret));
                assertFalse("Logs must not contain credentials", messages.toString().contains(secret));
                assertFalse("Email must not contain credentials", body.getValue().contains(secret));
            }
        } finally {
            logger.removeAppender(appender);
            logger.setLevel(originalLevel);
            appender.stop();
        }
    }
}
