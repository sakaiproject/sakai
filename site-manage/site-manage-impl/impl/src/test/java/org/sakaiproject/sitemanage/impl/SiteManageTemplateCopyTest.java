/*
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitemanage.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.sitemanage.api.SiteManageConstants;
import org.sakaiproject.sitemanage.api.SiteManageService;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.util.api.LinkMigrationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = SiteManageTemplateCopyTest.TestConfiguration.class)
public class SiteManageTemplateCopyTest {

    @Autowired private SiteManageService service;
    @Autowired private SiteService siteService;
    @Autowired private EntityManager entityManager;
    @Autowired private AuthzGroupService authzGroupService;
    @Autowired private LinkMigrationHelper linkMigrationHelper;

    private EntityTransferrer transfer;
    private Site originalDestination;
    private Site importedDestination;
    private AtomicReference<String> importedDescription;
    private Map<String, String> transferredEntities;

    @Before
    public void setUp() throws Exception {
        reset(siteService, entityManager, authzGroupService, linkMigrationHelper);
        transferredEntities = Collections.emptyMap();
        importedDescription = new AtomicReference<>("/portal/site/source");
        Site source = mock(Site.class);
        originalDestination = mock(Site.class);
        importedDestination = mock(Site.class);
        when(source.getRoles()).thenReturn(Collections.emptySet());
        when(originalDestination.getId()).thenReturn("destination");
        when(importedDestination.getId()).thenReturn("destination");
        when(importedDestination.getDescription()).thenAnswer(invocation -> importedDescription.get());
        doAnswer(invocation -> {
            importedDescription.set(invocation.getArgument(0));
            return null;
        }).when(importedDestination).setDescription(anyString());
        when(siteService.getSite("source")).thenReturn(source);
        when(siteService.getSite("destination")).thenReturn(originalDestination);
        when(siteService.siteReference("destination")).thenReturn("/site/destination");
        when(authzGroupService.getAuthzGroup("/site/destination")).thenReturn(mock(AuthzGroup.class));
        SitePage lessonsPage = page("sakai.lessonbuildertool");
        SitePage overviewPage = page(SiteManageConstants.SITE_INFO_TOOL_ID);
        when(originalDestination.getPages()).thenReturn(List.of(lessonsPage, overviewPage));

        // Lessons is a separate tool: its transfer saves a new site edit containing its placements.
        EntityProducer lessons = mock(EntityProducer.class, withSettings().extraInterfaces(EntityTransferrer.class));
        transfer = (EntityTransferrer) lessons;
        when(transfer.myToolIds()).thenReturn(new String[] { "sakai.lessonbuildertool" });
        when(entityManager.getEntityProducers()).thenReturn(List.of(lessons));
        when(transfer.transferCopyEntities("source", "destination", List.of(), List.of(), false))
                .thenAnswer(invocation -> {
                    when(siteService.getSite("destination")).thenReturn(importedDestination);
                    return transferredEntities;
                });
    }

    @Test
    public void overviewAfterLessonsSavesTheSiteWithImportedNavigation() throws Exception {
        service.importToolContent("source", originalDestination, false);

        verify(transfer).transferCopyEntities("source", "destination", List.of(), List.of(), false);
        verify(siteService).save(importedDestination);
        verify(siteService, never()).save(originalDestination);
        verify(importedDestination).setDescription("/portal/site/destination");
    }

    @Test
    public void referenceMigrationUsesTheCurrentDescriptionAfterOverviewSave() throws Exception {
        String staleDescription = "Original /portal/site/source /direct/assignment/old";
        String currentDescription = "Current /portal/site/destination /direct/assignment/old";
        String migratedDescription = "Current /portal/site/destination /direct/assignment/new";
        when(originalDestination.getDescription()).thenReturn(staleDescription);
        importedDescription.set("Current /portal/site/source /direct/assignment/old");
        transferredEntities = Map.of("/direct/assignment/old", "/direct/assignment/new");
        when(linkMigrationHelper.migrateAllLinks(transferredEntities.entrySet(), staleDescription))
                .thenReturn("Original /portal/site/source /direct/assignment/new");
        when(linkMigrationHelper.migrateAllLinks(transferredEntities.entrySet(), currentDescription))
                .thenReturn(migratedDescription);

        service.importToolContent("source", originalDestination, false);

        verify(linkMigrationHelper).migrateAllLinks(transferredEntities.entrySet(), currentDescription);
        assertEquals(migratedDescription, importedDescription.get());
        verify(siteService, times(2)).save(importedDestination);
        verify(siteService, never()).save(originalDestination);
    }

    private SitePage page(String toolId) {
        Tool tool = mock(Tool.class);
        when(tool.getId()).thenReturn(toolId);
        ToolConfiguration configuration = mock(ToolConfiguration.class);
        when(configuration.getTool()).thenReturn(tool);
        SitePage page = mock(SitePage.class);
        when(page.getTools()).thenReturn(List.of(configuration));
        return page;
    }

    @Configuration
    public static class TestConfiguration {
        @Bean public SiteService siteService() { return mock(SiteService.class); }
        @Bean public EntityManager entityManager() { return mock(EntityManager.class); }
        @Bean public AuthzGroupService authzGroupService() { return mock(AuthzGroupService.class); }
        @Bean public LinkMigrationHelper linkMigrationHelper() { return mock(LinkMigrationHelper.class); }
        @Bean public FunctionManager functionManager() { return mock(FunctionManager.class); }
        @Bean public ServerConfigurationService serverConfigurationService() { return mock(ServerConfigurationService.class); }

        @Bean
        public SiteManageService siteManageService(SiteService siteService, EntityManager entityManager,
                AuthzGroupService authzGroupService, FunctionManager functionManager,
                ServerConfigurationService serverConfigurationService, LinkMigrationHelper linkMigrationHelper) {
            SiteManageServiceImpl service = new SiteManageServiceImpl();
            service.setSiteService(siteService);
            service.setEntityManager(entityManager);
            service.setAuthzGroupService(authzGroupService);
            service.setFunctionManager(functionManager);
            service.setServerConfigurationService(serverConfigurationService);
            service.setLinkMigrationHelper(linkMigrationHelper);
            service.setTransactionTemplate(new TransactionTemplate(mock(PlatformTransactionManager.class)));
            return service;
        }
    }
}
