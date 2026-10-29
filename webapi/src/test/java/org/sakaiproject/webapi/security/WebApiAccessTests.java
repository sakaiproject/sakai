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

package org.sakaiproject.webapi.security;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.lti.api.SakaiAccessTokenException;
import org.sakaiproject.lti.api.SakaiAccessTokenService;
import org.sakaiproject.lti13.util.SakaiAccessToken;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.webapi.WebApiAccessConfiguration;
import org.sakaiproject.webapi.exception.GlobalExceptionHandler;
import org.sakaiproject.webapi.lti.LtiBearerTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = WebApiAccessTests.TestConfiguration.class)
public class WebApiAccessTests {

    @Configuration
    @EnableWebMvc
    @Import({ WebApiAccessConfiguration.class, WebApiAccessInterceptor.class,
        LtiBearerTokenInterceptor.class, GlobalExceptionHandler.class, TestController.class,
        org.sakaiproject.webapi.controllers.EntityController.class,
        org.sakaiproject.webapi.controllers.PermissionsController.class })
    public static class TestConfiguration {
        @Bean(name = "org.sakaiproject.tool.api.SessionManager")
        public SessionManager sessionManager() { return mock(SessionManager.class); }
        @Bean(name = "org.sakaiproject.lti.api.SakaiAccessTokenService")
        public SakaiAccessTokenService tokenService() { return mock(SakaiAccessTokenService.class); }
        @Bean
        public SiteService siteService() { return mock(SiteService.class); }
        @Bean public org.sakaiproject.portal.api.PortalService portalService() { return mock(org.sakaiproject.portal.api.PortalService.class); }
        @Bean public org.sakaiproject.entity.api.EntityManager entityManager() { return mock(org.sakaiproject.entity.api.EntityManager.class); }
        @Bean public org.sakaiproject.authz.api.AuthzGroupService authzGroupService() { return mock(org.sakaiproject.authz.api.AuthzGroupService.class); }
        @Bean public org.sakaiproject.authz.api.FunctionManager functionManager() { return mock(org.sakaiproject.authz.api.FunctionManager.class); }
        @Bean public org.sakaiproject.authz.api.SecurityService securityService() { return mock(org.sakaiproject.authz.api.SecurityService.class); }
        @Bean public org.sakaiproject.component.api.ServerConfigurationService serverConfigurationService() { return mock(org.sakaiproject.component.api.ServerConfigurationService.class); }

    }

    @RestController
    public static class TestController {
        @GetMapping("/protected")
        public String protectedHandler() { return "protected"; }
        @PublicApi("A public control for the default policy regression.")
        @GetMapping("/public")
        public String publicHandler() { return "public"; }
        @GetMapping("/sites/{siteId}/protected")
        public String siteHandler() { return "site"; }
        @ServiceSiteAccess("Test operation whose service enforces its own permission.")
        @GetMapping("/sites/{siteId}/service")
        public String serviceHandler() { return "service"; }
        @ServiceSiteAccess("A service permission denial must use the domain exception mapping.")
        @GetMapping("/sites/{siteId}/service-denied")
        public String serviceDeniedHandler() throws org.sakaiproject.conversations.api.ConversationsPermissionsException {
            throw new org.sakaiproject.conversations.api.ConversationsPermissionsException("Current user cannot update posts.");
        }
    }

    @Autowired private WebApplicationContext context;
    @Autowired private SessionManager sessionManager;
    @Autowired private SakaiAccessTokenService tokenService;
    @Autowired private SiteService siteService;
    @Autowired private org.sakaiproject.entity.api.EntityManager entityManager;
    @Autowired private org.sakaiproject.authz.api.AuthzGroupService realms;
    @Autowired private org.sakaiproject.authz.api.FunctionManager functions;
    private MockMvc mvc;
    private AtomicReference<Session> current;
    private Session previous;

    @Before
    public void setup() {
        reset(sessionManager, tokenService, siteService, entityManager, realms, functions);
        previous = mock(Session.class);
        current = new AtomicReference<>(previous);
        when(sessionManager.getCurrentSession()).thenAnswer(call -> current.get());
        doAnswer(call -> { current.set(call.getArgument(0)); return null; })
            .when(sessionManager).setCurrentSession(any(Session.class));
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void newUnannotatedHandlerIsProtectedByDefault() throws Exception {
        mvc.perform(get("/protected")).andExpect(status().isForbidden());
        verifyNoInteractions(siteService);
    }

    @Test
    public void missingSessionIsDenied() throws Exception {
        current.set(null);
        mvc.perform(get("/protected")).andExpect(status().isForbidden());
    }

    @Test
    public void publicHandlerDoesNotRequireSession() throws Exception {
        mvc.perform(get("/public")).andExpect(status().isOk()).andExpect(content().string("public"));
    }

    @Test
    public void authenticatedCallerCanUseProtectedHandler() throws Exception {
        when(previous.getUserId()).thenReturn("member");
        mvc.perform(get("/protected")).andExpect(status().isOk());
    }

    @Test
    public void forbiddenSiteNeverReachesHandler() throws Exception {
        when(previous.getUserId()).thenReturn("nonmember");
        when(siteService.getSiteVisit("private")).thenThrow(new PermissionException("nonmember", "site.visit", "/site/private"));
        mvc.perform(get("/sites/private/protected")).andExpect(status().isForbidden());
    }

    @Test
    public void existingSiteAccessPolicyIsUsed() throws Exception {
        when(previous.getUserId()).thenReturn("member");
        mvc.perform(get("/sites/course/protected")).andExpect(status().isOk());
        verify(siteService).getSiteVisit("course");
    }

    @Test
    public void missingSiteReturnsNotFound() throws Exception {
        when(previous.getUserId()).thenReturn("member");
        when(siteService.getSiteVisit("missing")).thenThrow(new IdUnusedException("missing"));
        mvc.perform(get("/sites/missing/protected")).andExpect(status().isNotFound());
    }

    @Test
    public void serviceSitePolicyStillRequiresLogin() throws Exception {
        mvc.perform(get("/sites/course/service")).andExpect(status().isForbidden());
    }

    @Test
    public void bearerSessionIsEstablishedBeforeAuthorizationAndRestoredAfterDenial() throws Exception {
        Session mini = bearerSession();
        when(siteService.getSiteVisit("private")).thenThrow(new PermissionException("lti", "site.visit", "/site/private"));
        mvc.perform(get("/sites/private/protected").header("Authorization", "Bearer valid"))
            .andExpect(status().isForbidden());
        assertEquals(previous, current.get());
        verify(mini).invalidate();
        mvc.perform(get("/protected")).andExpect(status().isForbidden());
    }

    @Test
    public void validBearerCanReachProtectedHandlerAndIsCleanedUp() throws Exception {
        Session mini = bearerSession();
        mvc.perform(get("/protected").header("Authorization", "Bearer valid"))
            .andExpect(status().isOk());
        assertEquals(previous, current.get());
        verify(mini).invalidate();
    }

    @Test
    public void conversationsServicePermissionDenialReturnsForbiddenAndRestoresBearerSession() throws Exception {
        Session mini = bearerSession();
        mvc.perform(get("/sites/course/service-denied").header("Authorization", "Bearer valid"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Permission denied"))
            .andExpect(jsonPath("$.trace").doesNotExist())
            .andExpect(jsonPath("$.stackTrace").doesNotExist());
        verifyNoInteractions(siteService);
        verify(mini).invalidate();
        assertEquals(previous, current.get());
    }

    @Test
    public void scopedBearerCanReachServicePolicyWithoutSiteVisit() throws Exception {
        Session mini = bearerSession();
        when(siteService.getSiteVisit("private")).thenThrow(new PermissionException("lti", "site.visit", "/site/private"));
        mvc.perform(get("/sites/private/service").header("Authorization", "Bearer valid"))
            .andExpect(status().isOk()).andExpect(content().string("service"));
        verifyNoInteractions(siteService);
        verify(mini).invalidate();
        assertEquals(previous, current.get());
    }

    @Test
    public void invalidBearerIsRejectedBeforeController() throws Exception {
        when(tokenService.isValidBearerHeader("Bearer invalid")).thenReturn(true);
        when(tokenService.isWebApiEnabled()).thenReturn(true);
        when(tokenService.extractBearerToken("Bearer invalid")).thenReturn("invalid");
        when(tokenService.validateToken("invalid")).thenThrow(new SakaiAccessTokenException("invalid", "Rejected"));
        mvc.perform(get("/protected").header("Authorization", "Bearer invalid"))
            .andExpect(status().isUnauthorized());
        assertEquals(previous, current.get());
    }

    @Test
    public void mixedInventoryBatchIsRejectedBeforeAnyProducerIsRead() throws Exception {
        when(previous.getUserId()).thenReturn("editor");
        when(siteService.allowUpdateSite("course")).thenReturn(true);
        mvc.perform(get("/tool-entities/tools/sakai.samigo").param("sites", "course", "foreign"))
            .andExpect(status().isForbidden());
        verifyNoInteractions(entityManager);
    }

    @Test
    public void permittedSourceInventoryBatchReachesRealController() throws Exception {
        when(previous.getUserId()).thenReturn("editor");
        when(siteService.allowUpdateSite("course")).thenReturn(true);
        org.sakaiproject.entity.api.EntityProducer entityProducer = mock(org.sakaiproject.entity.api.EntityProducer.class,
            withSettings().extraInterfaces(org.sakaiproject.entity.api.EntityTransferrer.class));
        org.sakaiproject.entity.api.EntityTransferrer producer = (org.sakaiproject.entity.api.EntityTransferrer) entityProducer;
        when(producer.myToolIds()).thenReturn(new String[] {"sakai.samigo"});
        when(producer.getEntityMap("course")).thenReturn(java.util.List.of(java.util.Map.of("id", "draft-id", "title", "Draft")));
        when(entityManager.getEntityProducers()).thenReturn(java.util.List.of(entityProducer));
        mvc.perform(get("/tool-entities/tools/sakai.samigo").param("sites", "course"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.course[0].title").value("Draft"));
    }

    @Test
    public void permissionEditorCannotReadForeignRealmOrOverride() throws Exception {
        when(previous.getUserId()).thenReturn("editor");
        when(siteService.siteReference("course")).thenReturn("/site/course");
        when(realms.allowUpdate("/site/course")).thenReturn(true);
        mvc.perform(get("/sites/course/permissions/content").param("ref", "/site/foreign"))
            .andExpect(status().isForbidden());
        mvc.perform(get("/sites/course/permissions/content").param("ref", "/site/course")
            .param("overrideRef", "/site/foreign"))
            .andExpect(status().isForbidden());
        verify(realms, never()).getAuthzGroup(anyString());
    }

    @Test
    public void scopedPermissionEditorCanReadTheirRealmWithoutSiteVisit() throws Exception {
        bearerSession();
        when(siteService.siteReference("course")).thenReturn("/site/course");
        when(realms.allowUpdate("/site/course")).thenReturn(true);
        org.sakaiproject.site.api.Site site = mock(org.sakaiproject.site.api.Site.class);
        when(site.getGroups()).thenReturn(java.util.List.of());
        when(siteService.getSite("course")).thenReturn(site);
        org.sakaiproject.authz.api.AuthzGroup realm = mock(org.sakaiproject.authz.api.AuthzGroup.class);
        when(realm.getRoles()).thenReturn(java.util.Set.of());
        when(realms.getAuthzGroup("/site/course")).thenReturn(realm);
        when(functions.getRegisteredFunctions("content.")).thenReturn(java.util.List.of());
        mvc.perform(get("/sites/course/permissions/content").param("ref", "/site/course")
            .header("Authorization", "Bearer valid")).andExpect(status().isOk());
        verify(siteService, never()).getSiteVisit(anyString());
    }

    private Session bearerSession() throws Exception {
        Session mini = mock(Session.class);
        AtomicReference<String> userId = new AtomicReference<>();
        doAnswer(call -> { userId.set(call.getArgument(0)); return null; }).when(mini).setUserId(any(String.class));
        when(mini.getUserId()).thenAnswer(call -> userId.get());
        when(sessionManager.startSession()).thenReturn(mini);
        when(tokenService.isValidBearerHeader("Bearer valid")).thenReturn(true);
        when(tokenService.isWebApiEnabled()).thenReturn(true);
        when(tokenService.extractBearerToken("Bearer valid")).thenReturn("valid");
        SakaiAccessToken token = new SakaiAccessToken();
        token.tool_id = 123L;
        when(tokenService.validateToken("valid")).thenReturn(token);
        return mini;
    }
}
