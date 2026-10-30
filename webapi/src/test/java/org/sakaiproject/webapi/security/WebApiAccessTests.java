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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.persistence.EntityNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.conversations.api.ConversationsPermissionsException;
import org.sakaiproject.conversations.api.ConversationsService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.lti.api.SakaiAccessTokenException;
import org.sakaiproject.lti.api.SakaiAccessTokenService;
import org.sakaiproject.lti13.util.SakaiAccessToken;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.facade.ExtendedTimeFacade;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.webapi.WebApiAccessConfiguration;
import org.sakaiproject.webapi.beans.SiteEntityRestBean;
import org.sakaiproject.webapi.beans.SiteEntityRestBean.SiteEntityType;
import org.sakaiproject.webapi.controllers.ConversationsController;
import org.sakaiproject.webapi.controllers.EntityController;
import org.sakaiproject.webapi.controllers.PermissionsController;
import org.sakaiproject.webapi.controllers.SiteEntityController;
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
        EntityController.class,
        ConversationsController.class,
        PermissionsController.class })
    public static class TestConfiguration {
        @Bean(name = "org.sakaiproject.tool.api.SessionManager")
        public SessionManager sessionManager() { return mock(SessionManager.class); }
        @Bean(name = "org.sakaiproject.lti.api.SakaiAccessTokenService")
        public SakaiAccessTokenService tokenService() { return mock(SakaiAccessTokenService.class); }
        @Bean
        public SiteService siteService() { return mock(SiteService.class); }
        @Bean public PortalService portalService() { return mock(PortalService.class); }
        @Bean public EntityManager entityManager() { return mock(EntityManager.class); }
        @Bean public AuthzGroupService authzGroupService() { return mock(AuthzGroupService.class); }
        @Bean public FunctionManager functionManager() { return mock(FunctionManager.class); }
        @Bean public SecurityService securityService() { return mock(SecurityService.class); }
        @Bean(name = "org.sakaiproject.component.api.ServerConfigurationService")
        public ServerConfigurationService serverConfigurationService() { return mock(ServerConfigurationService.class); }

        @Bean public ConversationsService conversationsService() { return mock(ConversationsService.class); }
        @Bean public UserDirectoryService userDirectoryService() { return mock(UserDirectoryService.class); }
        @Bean public SearchService searchService() { return mock(SearchService.class); }
        @Bean public AssignmentService assignmentService() { return mock(AssignmentService.class); }
        @Bean public ContentHostingService contentHostingService() { return mock(ContentHostingService.class); }
        @Bean public DiscussionForumManager discussionForumManager() { return mock(DiscussionForumManager.class); }
        @Bean public PersistenceService persistenceService() { return mock(PersistenceService.class); }
        @Bean public PublishedAssessmentService publishedAssessmentService() { return mock(PublishedAssessmentService.class); }
        @Bean public SiteEntityController siteEntityController(PublishedAssessmentService service) {
            SiteEntityController controller = new SiteEntityController();
            controller.setPublishedAssessmentService(service);
            return controller;
        }
    }

    @RestController
    public static class TestController {
        @GetMapping("/invalid-request")
        public String invalidRequest() { throw new IllegalArgumentException("Select at least one group"); }
        @GetMapping("/missing-resource")
        public String missingResource() { throw new EntityNotFoundException("Missing topic"); }
        @GetMapping("/protected")
        public String protectedHandler() { return "protected"; }
        @PublicApi("A public control for the default policy regression.")
        @GetMapping("/public")
        public String publicHandler() { return "public"; }
        @GetMapping("/sites/{siteId}/protected")
        public String siteHandler() { return "site"; }
        @GetMapping("/sites/{siteId}/service")
        public String serviceHandler() { return "service"; }
        @GetMapping("/sites/{siteId}/service-denied")
        public String serviceDeniedHandler() throws ConversationsPermissionsException {
            throw new ConversationsPermissionsException("Current user cannot update posts.");
        }
    }

    @Autowired private WebApplicationContext context;
    @Autowired private SessionManager sessionManager;
    @Autowired private SakaiAccessTokenService tokenService;
    @Autowired private SiteService siteService;
    @Autowired private ConversationsService conversationsService;
    @Autowired private DiscussionForumManager forumManager;
    @Autowired private PublishedAssessmentService assessmentService;
    @Autowired private PersistenceService persistenceService;
    @Autowired private EntityManager entityManager;
    @Autowired private AuthzGroupService realms;
    @Autowired private FunctionManager functions;
    private MockMvc mvc;
    private AtomicReference<Session> current;
    private Session previous;

    @Before
    public void setup() {
        reset(sessionManager, tokenService, siteService, entityManager, realms, functions, conversationsService, forumManager, assessmentService, persistenceService);
        previous = mock(Session.class);
        current = new AtomicReference<>(previous);
        when(sessionManager.getCurrentSession()).thenAnswer(call -> current.get());
        doAnswer(call -> { current.set(call.getArgument(0)); return null; })
            .when(sessionManager).setCurrentSession(any(Session.class));
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void invalidForumDateRangeRejectsWholeBatchBeforeSaving() throws Exception {
        when(previous.getUserId()).thenReturn("editor");
        Site site = mock(Site.class);
        when(site.getId()).thenReturn("course");
        when(siteService.getOptionalSite("course")).thenReturn(Optional.of(site));
        when(forumManager.getForumForUpdate("course", 1L)).thenReturn(mock(DiscussionForum.class));
        // Set iteration order is unspecified in JSON binding; use an ordered batch to exercise a valid first item.
        Set<SiteEntityRestBean> batch = new LinkedHashSet<>();
        batch.add(SiteEntityRestBean.builder().id("1").type(SiteEntityType.FORUM).dateRestricted(false).build());
        batch.add(SiteEntityRestBean.builder().id("2").type(SiteEntityType.FORUM).dateRestricted(true)
            .openDate(Instant.parse("2026-01-02T00:00:00Z")).closeDate(Instant.parse("2026-01-01T00:00:00Z")).build());
        assertEquals(400, context.getBean(SiteEntityController.class).updateSiteEntities("course", batch).getStatusCode().value());
        verify(forumManager).getForumForUpdate("course", 1L);
        verify(forumManager, never()).updateForumAvailability(any(), anyLong(), anyBoolean(), any(), any());
    }

    @Test
    public void assessmentGetRoutePreservesSelectedEmptyAndNullGroups() throws Exception {
        when(previous.getUserId()).thenReturn("editor");
        when(siteService.getOptionalSite("course")).thenReturn(Optional.of(mock(Site.class)));
        PublishedAssessmentFacade selected = new PublishedAssessmentFacade(1L, "Selected", null,
            null, null, Map.of("selected-group", "Selected group"));
        PublishedAssessmentFacade empty = new PublishedAssessmentFacade(2L, "Empty", null,
            null, null, Map.of());
        PublishedAssessmentFacade unset = new PublishedAssessmentFacade(3L, "Unset", null,
            null, null, (Map<String, String>) null);
        when(assessmentService.getPublishedAssessmentsForAuthor("course")).thenReturn(List.of(selected, empty, unset));
        ExtendedTimeFacade times = mock(ExtendedTimeFacade.class);
        when(persistenceService.getExtendedTimeFacade()).thenReturn(times);
        when(times.getEntriesForPub(any())).thenReturn(List.of());
        mvc.perform(get("/sites/course/entities/assessments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.id == '1')].groupRefs[0]").value("/site/course/group/selected-group"))
            .andExpect(jsonPath("$[?(@.id == '2')].groupRefs[0]").doesNotExist())
            .andExpect(jsonPath("$[?(@.id == '3')].groupRefs[0]").doesNotExist());
        verify(assessmentService).getPublishedAssessmentsForAuthor("course");
    }

    @Test
    public void assessmentResponseRetainsGroupReleaseReferences() {
        PublishedAssessmentFacade assessment = new PublishedAssessmentFacade(1L, "Group assessment", null,
            null, null, Map.of("selected-group", "Selected group"));
        SiteEntityRestBean response = SiteEntityRestBean.of(assessment, Set.of(), "course");
        assertEquals(Set.of("/site/course/group/selected-group"), response.getGroupRefs());
    }

    @Test
    public void omittedForumAvailabilityPreservesExistingDatesWithoutSaving() throws Exception {
        when(previous.getUserId()).thenReturn("editor");
        when(siteService.getOptionalSite("course")).thenReturn(Optional.of(mock(Site.class)));
        DiscussionForum forum = mock(DiscussionForum.class);
        when(forum.getId()).thenReturn(1L);
        when(forum.getTitle()).thenReturn("Scheduled forum");
        when(forum.getAvailabilityRestricted()).thenReturn(true);
        when(forum.getOpenDate()).thenReturn(Date.from(Instant.parse("2026-01-01T00:00:00Z")));
        when(forum.getCloseDate()).thenReturn(Date.from(Instant.parse("2026-01-02T00:00:00Z")));
        when(forumManager.getForumForUpdate("course", 1L)).thenReturn(forum);
        mvc.perform(patch("/sites/course/entities").contentType("application/json")
                .content("[{\"id\":\"1\",\"type\":\"FORUM\"}]"))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].dateRestricted").value(true))
            .andExpect(jsonPath("$[0].openDate").exists()).andExpect(jsonPath("$[0].closeDate").exists());
        verify(forumManager, never()).updateForumAvailability(anyString(), anyLong(), anyBoolean(), any(), any());
        verify(forum, never()).setAvailabilityRestricted(anyBoolean());
    }

    @Test
    public void missingForumHasAClientResponse() throws Exception {
        when(previous.getUserId()).thenReturn("editor");
        when(siteService.getOptionalSite("course")).thenReturn(Optional.of(mock(Site.class)));
        when(forumManager.getForumForUpdate("course", 99L)).thenThrow(new IllegalArgumentException("Forum not found"));
        mvc.perform(patch("/sites/course/entities").contentType("application/json")
                .content("[{\"id\":\"99\",\"type\":\"FORUM\"}]"))
            .andExpect(status().isBadRequest());
        verify(forumManager).getForumForUpdate("course", 99L);
        verify(forumManager, never()).updateForumAvailability(anyString(), anyLong(), anyBoolean(), any(), any());
    }

    @Test
    public void unpublishedSiteRejectsTopicPostAndCommentWritesBeforeServiceInvocation() throws Exception {
        when(previous.getUserId()).thenReturn("student");
        when(siteService.getSiteVisit("unpublished")).thenThrow(
            new PermissionException("student", SiteService.SITE_VISIT_UNPUBLISHED, "/site/unpublished"));
        for (String path : List.of("/sites/unpublished/topics",
                "/sites/unpublished/topics/topic/posts", "/sites/unpublished/topics/topic/posts/post/comments")) {
            mvc.perform(post(path).contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
        }
        verifyNoInteractions(conversationsService);
    }

    @Test
    public void invalidArgumentsAndMissingEntitiesHaveDeliberateClientResponses() throws Exception {
        when(previous.getUserId()).thenReturn("member");
        mvc.perform(get("/invalid-request")).andExpect(status().isBadRequest());
        mvc.perform(get("/missing-resource")).andExpect(status().isNotFound());
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
    public void serviceHandlerStillRequiresLogin() throws Exception {
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
        verify(siteService).getSiteVisit("course");
        verify(mini).invalidate();
        assertEquals(previous, current.get());
    }

    @Test
    public void scopedBearerWithoutSiteVisitCannotReachServiceHandler() throws Exception {
        Session mini = bearerSession();
        when(siteService.getSiteVisit("private")).thenThrow(new PermissionException("lti", "site.visit", "/site/private"));
        mvc.perform(get("/sites/private/service").header("Authorization", "Bearer valid"))
            .andExpect(status().isForbidden());
        verify(siteService).getSiteVisit("private");
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
        EntityProducer entityProducer = mock(EntityProducer.class,
            withSettings().extraInterfaces(EntityTransferrer.class));
        EntityTransferrer producer = (EntityTransferrer) entityProducer;
        when(producer.myToolIds()).thenReturn(new String[] {"sakai.samigo"});
        when(producer.getEntityMap("course")).thenReturn(List.of(Map.of("id", "draft-id", "title", "Draft")));
        when(entityManager.getEntityProducers()).thenReturn(List.of(entityProducer));
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
    public void bearerPermissionEditorCanReadTheirRealmWithSiteVisit() throws Exception {
        bearerSession();
        when(siteService.siteReference("course")).thenReturn("/site/course");
        when(realms.allowUpdate("/site/course")).thenReturn(true);
        Site site = mock(Site.class);
        when(site.getGroups()).thenReturn(List.of());
        when(siteService.getSite("course")).thenReturn(site);
        AuthzGroup realm = mock(AuthzGroup.class);
        when(realm.getRoles()).thenReturn(Set.of());
        when(realms.getAuthzGroup("/site/course")).thenReturn(realm);
        when(functions.getRegisteredFunctions("content.")).thenReturn(List.of());
        mvc.perform(get("/sites/course/permissions/content").param("ref", "/site/course")
            .header("Authorization", "Bearer valid")).andExpect(status().isOk());
        verify(siteService).getSiteVisit("course");
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
