/*
 * Copyright (c) 2003-2025 The Apereo Foundation
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.messaging.api.UserMessagingService;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.BaseResourceProperties;
import org.sakaiproject.webapi.controllers.SitesController;
import org.sakaiproject.webapi.exception.GlobalExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SitesControllerTests.SitesTestConfiguration.class)
public class SitesControllerTests {

    @Configuration
    @Import({ WebApiTestConfiguration.class, SitesController.class, GlobalExceptionHandler.class })
    public static class SitesTestConfiguration {

        @Bean
        public ContentHostingService contentHostingService() {
            return mock(ContentHostingService.class);
        }

        @Bean
        @Qualifier("org.sakaiproject.coursemanagement.api.CourseManagementService")
        public CourseManagementService courseManagementService() {
            return mock(CourseManagementService.class);
        }

        @Bean
        public ServerConfigurationService serverConfigurationService() {
            return mock(ServerConfigurationService.class);
        }
    }

    @Autowired
    private SitesController controller;

    @Autowired
    private GlobalExceptionHandler exceptionHandler;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private SiteService siteService;

    @Autowired
    private UserMessagingService userMessagingService;

    @Autowired
    private PortalService portalService;

    private MockMvc mockMvc;
    private Session session;

    @Before
    public void setup() {
        reset(sessionManager, siteService, userMessagingService, portalService);
        session = mock(Session.class);
        when(sessionManager.getCurrentSession()).thenReturn(session);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(exceptionHandler).build();
    }

    @Test
    public void getSiteRequiresLoggedInUser() throws Exception {
        mockMvc.perform(get("/sites/private-site"))
            .andExpect(status().isForbidden());

        verifyNoInteractions(siteService, userMessagingService, portalService);
    }

    @Test
    public void getSiteRejectsUserWithoutSiteAccess() throws Exception {
        when(session.getUserId()).thenReturn("nonmember");
        when(siteService.getSiteVisit("private-site"))
            .thenThrow(new PermissionException("nonmember", "site.visit", "/site/private-site"));

        mockMvc.perform(get("/sites/private-site"))
            .andExpect(status().isForbidden());

        verifyNoInteractions(userMessagingService, portalService);
    }

    @Test
    public void getSiteReturnsMetadataForUserWithSiteAccess() throws Exception {
        when(session.getUserId()).thenReturn("member");
        Site site = mock(Site.class);
        when(site.getId()).thenReturn("private-site");
        when(site.getTitle()).thenReturn("Private Site");
        when(site.getShortDescription()).thenReturn("  Course description  ");
        when(site.getProperties()).thenReturn(new BaseResourceProperties());
        when(site.getPages()).thenReturn(List.of());
        when(siteService.getSiteVisit("private-site")).thenReturn(site);
        when(userMessagingService.getNotifications()).thenReturn(List.of());
        when(portalService.getPinnedSites()).thenReturn(List.of());

        mockMvc.perform(get("/sites/private-site"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.siteId").value("private-site"))
            .andExpect(jsonPath("$.title").value("Private Site"))
            .andExpect(jsonPath("$.shortDescription").value("Course description"))
            .andExpect(jsonPath("$.tools").isEmpty());
    }

    @Test
    public void getSiteReturnsNotFoundForMissingSite() throws Exception {
        when(session.getUserId()).thenReturn("member");
        when(siteService.getSiteVisit("missing-site")).thenThrow(new IdUnusedException("missing-site"));

        mockMvc.perform(get("/sites/missing-site"))
            .andExpect(status().isNotFound());

        verifyNoInteractions(userMessagingService, portalService);
    }
}
