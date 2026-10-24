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
package org.sakaiproject.webapi.controllers.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.lti.api.LtiBearerSessionConstants;
import org.sakaiproject.lti.api.SakaiAccessTokenException;
import org.sakaiproject.lti.api.SakaiAccessTokenService;
import org.sakaiproject.lti13.util.SakaiAccessToken;
import org.sakaiproject.messaging.api.UserMessagingService;
import org.sakaiproject.messaging.api.UserNotificationTransferBean;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.AuthenticationManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.webapi.WebMvcConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Loads the production MVC configuration and its complete component scan. Only services supplied
 * by the kernel and other Sakai modules are mocked; MVC components use their production wiring.
 * The servlet container's Sakai RequestFilter and listeners are outside this test's scope.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {WebMvcConfiguration.class, WebApiTestConfiguration.class})
public class WebMvcConfigurationTests {

    private static final String BEARER_HEADER = "Bearer mvc-test-token";
    private static final String FIXTURE_BODY = "Web API MVC integration fixture.\n";

    @Autowired
    private WebApplicationContext applicationContext;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private SakaiAccessTokenService accessTokenService;

    @Autowired
    private UserMessagingService userMessagingService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PreferencesService preferencesService;

    @Autowired
    @Qualifier("mvcConversionService")
    private FormattingConversionService conversionService;

    private MockMvc mockMvc;
    private Session userSession;
    private Session bearerSession;
    private AtomicReference<Session> currentSession;

    @Before
    public void setup() {
        reset(sessionManager, accessTokenService, userMessagingService, authenticationManager, preferencesService);

        userSession = mock(Session.class);
        when(userSession.getUserId()).thenReturn("user1");
        currentSession = new AtomicReference<>(userSession);
        when(sessionManager.getCurrentSession()).thenAnswer(invocation -> currentSession.get());
        doAnswer(invocation -> {
            currentSession.set(invocation.getArgument(0));
            return null;
        }).when(sessionManager).setCurrentSession(any(Session.class));

        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
    }

    @Test
    public void notificationsUseProductionJsonConverters() throws Exception {
        UserNotificationTransferBean notification = new UserNotificationTransferBean();
        notification.id = 1L;
        notification.to = "user1";
        notification.title = "New announcement";
        notification.eventDate = Instant.parse("2026-01-18T12:00:00Z");
        when(userMessagingService.getNotifications()).thenReturn(List.of(notification));

        mockMvc.perform(get("/api/users/me/notifications").contextPath("/api"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].to").value("user1"))
            .andExpect(jsonPath("$[0].title").value("New announcement"))
            .andExpect(jsonPath("$[0].eventDate").exists());
    }

    @Test
    public void dashboardUsesProductionControllerInitialization() throws Exception {
        Preferences preferences = mock(Preferences.class);
        when(preferencesService.getPreferences("user1")).thenReturn(preferences);

        mockMvc.perform(get("/api/users/user1/dashboard").contextPath("/api"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.widgets[0]").value("courses"))
            .andExpect(jsonPath("$.widgetLayout[0]").value("courses"));
    }

    @Test
    public void missingSessionUsesDiscoveredExceptionHandler() throws Exception {
        when(userSession.getUserId()).thenReturn(null);

        mockMvc.perform(get("/api/users/me/notifications").contextPath("/api"))
            .andExpect(status().isForbidden())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Missing or invalid Sakai session"));

        verifyNoInteractions(userMessagingService);
    }

    @Test
    public void notificationWritesRequireSessionThroughProductionMvc() throws Exception {
        when(userSession.getUserId()).thenReturn(null);

        for (String suffix : List.of("/1/clear", "/clear", "/markViewed", "/test")) {
            mockMvc.perform(post("/api/users/me/notifications" + suffix).contextPath("/api"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Missing or invalid Sakai session"));
        }

        verifyNoInteractions(userMessagingService);
    }

    @Test
    public void notificationFiltersBindThroughProductionMvc() throws Exception {
        mockMvc.perform(post("/api/users/me/notifications/markViewed").contextPath("/api")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("siteId", "site1").param("toolId", "sakai.announcements"))
            .andExpect(status().isOk());

        verify(userMessagingService).markAllNotificationsViewed("site1", "sakai.announcements");
    }

    @Test
    public void disabledBearerAccessRejectsAuthenticatedRequests() throws Exception {
        when(accessTokenService.isValidBearerHeader(BEARER_HEADER)).thenReturn(true);
        when(accessTokenService.isWebApiEnabled()).thenReturn(false);

        mockMvc.perform(get("/api/users/me/notifications").contextPath("/api")
                .header(HttpHeaders.AUTHORIZATION, BEARER_HEADER))
            .andExpect(status().isForbidden());

        assertSame(userSession, currentSession.get());
        verifyNoInteractions(userMessagingService);
    }

    @Test
    public void rejectedBearerTokenDoesNotFallBackToExistingSession() throws Exception {
        when(accessTokenService.isValidBearerHeader(BEARER_HEADER)).thenReturn(true);
        when(accessTokenService.isWebApiEnabled()).thenReturn(true);
        when(accessTokenService.extractBearerToken(BEARER_HEADER)).thenReturn("mvc-test-token");
        when(accessTokenService.validateToken("mvc-test-token"))
            .thenThrow(new SakaiAccessTokenException("signature_error", "Invalid test signature"));

        mockMvc.perform(get("/api/users/me/notifications").contextPath("/api")
                .header(HttpHeaders.AUTHORIZATION, BEARER_HEADER))
            .andExpect(status().isUnauthorized());

        assertSame(userSession, currentSession.get());
        verifyNoInteractions(userMessagingService);
    }

    @Test
    public void acceptedBearerUsesTemporaryPrincipalAndRestoresPreviousSession() throws Exception {
        configureAcceptedBearer();
        when(userMessagingService.getNotifications()).thenAnswer(invocation -> {
            UserNotificationTransferBean notification = new UserNotificationTransferBean();
            notification.to = currentSession.get().getUserId();
            return List.of(notification);
        });

        mockMvc.perform(get("/api/users/me/notifications").contextPath("/api")
                .header(HttpHeaders.AUTHORIZATION, BEARER_HEADER))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].to").value(LtiBearerSessionConstants.LTI_TOOL_USER_ID_PREFIX + "42"));

        assertSame(userSession, currentSession.get());
        verify(bearerSession).invalidate();
    }

    @Test
    public void bearerSessionIsRestoredAfterControllerFailure() throws Exception {
        configureAcceptedBearer();
        when(userMessagingService.getNotifications()).thenThrow(new SecurityException("Test permission denial"));

        mockMvc.perform(get("/api/users/me/notifications").contextPath("/api")
                .header(HttpHeaders.AUTHORIZATION, BEARER_HEADER))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("Access forbidden"));

        assertSame(userSession, currentSession.get());
        verify(bearerSession).invalidate();
    }

    @Test
    public void loginIsExcludedFromBearerInterceptor() throws Exception {
        mockMvc.perform(post("/api/login").contextPath("/api")
                .header(HttpHeaders.AUTHORIZATION, BEARER_HEADER))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("username and password are required"));

        verifyNoInteractions(accessTokenService, authenticationManager);
    }

    @Test
    public void documentationResourcesAreServedWithoutBearerAuthentication() throws Exception {
        mockMvc.perform(get("/api/docs/mvc-integration.txt").contextPath("/api")
                .header(HttpHeaders.AUTHORIZATION, BEARER_HEADER))
            .andExpect(status().isOk())
            .andExpect(content().string(FIXTURE_BODY));

        verifyNoInteractions(accessTokenService);
    }

    @Test
    public void webjarResourcesAreServedWithoutBearerAuthentication() throws Exception {
        mockMvc.perform(get("/api/webjars/mvc-integration.txt").contextPath("/api")
                .header(HttpHeaders.AUTHORIZATION, BEARER_HEADER))
            .andExpect(status().isOk())
            .andExpect(content().string(FIXTURE_BODY));

        verifyNoInteractions(accessTokenService);
    }

    @Test
    public void productionConversionServiceRegistersEpochMillisFormatter() {
        Instant instant = Instant.parse("2026-01-18T12:00:00Z");

        assertEquals(Long.toString(instant.toEpochMilli()), conversionService.convert(instant, String.class));
    }

    private void configureAcceptedBearer() throws Exception {
        SakaiAccessToken token = new SakaiAccessToken();
        token.tool_id = 42L;
        when(accessTokenService.isValidBearerHeader(BEARER_HEADER)).thenReturn(true);
        when(accessTokenService.isWebApiEnabled()).thenReturn(true);
        when(accessTokenService.extractBearerToken(BEARER_HEADER)).thenReturn("mvc-test-token");
        when(accessTokenService.validateToken("mvc-test-token")).thenReturn(token);

        bearerSession = mock(Session.class);
        AtomicReference<String> bearerPrincipal = new AtomicReference<>();
        when(bearerSession.getUserId()).thenAnswer(invocation -> bearerPrincipal.get());
        doAnswer(invocation -> {
            bearerPrincipal.set(invocation.getArgument(0));
            return null;
        }).when(bearerSession).setUserId(any(String.class));
        when(sessionManager.startSession()).thenReturn(bearerSession);
    }
}
