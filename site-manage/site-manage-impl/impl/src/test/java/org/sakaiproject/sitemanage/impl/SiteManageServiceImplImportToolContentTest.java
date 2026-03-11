/**
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
package org.sakaiproject.sitemanage.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.sitemanage.api.SiteManageConstants;
import org.sakaiproject.tool.api.Tool;
import org.tsugi.lti13.LTICustomVars;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Focused unit tests for SiteManageServiceImpl#importToolContent site info URL behavior.
 */
public class SiteManageServiceImplImportToolContentTest {

    private SiteManageServiceImpl siteManageService;
    private SiteService siteService;
    private AuthzGroupService authzGroupService;
    private FunctionManager functionManager;
    private ServerConfigurationService serverConfigurationService;
    private EntityManager entityManager;

    @Before
    public void setUp() {
        siteManageService = spy(new SiteManageServiceImpl());
        siteService = mock(SiteService.class);
        authzGroupService = mock(AuthzGroupService.class);
        functionManager = mock(FunctionManager.class);
        serverConfigurationService = mock(ServerConfigurationService.class);
        entityManager = mock(EntityManager.class);

        siteManageService.setSiteService(siteService);
        siteManageService.setAuthzGroupService(authzGroupService);
        siteManageService.setFunctionManager(functionManager);
        siteManageService.setServerConfigurationService(serverConfigurationService);
        siteManageService.setEntityManager(entityManager);

        // Keep this test class focused on site-info URL behavior.
        doReturn(false).when(siteManageService).isAddMissingToolsOnImportEnabled();
        when(entityManager.getEntityProducers()).thenReturn(Collections.emptyList());
    }

    @Test
    public void importToolContentUsesSourceSiteInfoUrl() throws Exception {

        final String oldSiteId = "site-old";
        final String newSiteId = "site-new";
        final String sourceSiteInfoUrl = "https://sakai.example.edu/portal/site/site-old";
        final String destinationSiteInfoUrl = "https://sakai.example.edu/portal/site/should-not-be-used";
        final String transferResultSiteInfoUrl = "https://sakai.example.edu/access/content/group/site-new/site-info?from-transfer=true";
        final String expectedSiteInfoUrl = transferResultSiteInfoUrl;

        Site sourceSite = mock(Site.class);
        Site destinationSite = mock(Site.class);
        mockImportContext(oldSiteId, newSiteId, sourceSiteInfoUrl, destinationSiteInfoUrl, sourceSite, destinationSite);

        doReturn(transferResultSiteInfoUrl).when(siteManageService).transferSiteResource(oldSiteId, newSiteId, sourceSiteInfoUrl);

        siteManageService.importToolContent(oldSiteId, destinationSite, false);

        verify(siteManageService).transferSiteResource(oldSiteId, newSiteId, sourceSiteInfoUrl);
        verify(siteManageService, never()).transferSiteResource(oldSiteId, newSiteId, destinationSiteInfoUrl);
        verify(destinationSite).setInfoUrl(expectedSiteInfoUrl);
        verify(siteService).save(destinationSite);
    }

    @Test
    public void importToolContentFallsBackToSiteIdReplacementWhenUrlIsNotAResource() throws Exception {

        final String oldSiteId = "site-old";
        final String newSiteId = "site-new";
        final String sourceSiteInfoUrl = "https://sakai.example.edu/portal/site/site-old";
        final String destinationSiteInfoUrl = "https://sakai.example.edu/portal/site/site-new";
        final String expectedSiteInfoUrl = "https://sakai.example.edu/portal/site/site-new";

        Site sourceSite = mock(Site.class);
        Site destinationSite = mock(Site.class);
        mockImportContext(oldSiteId, newSiteId, sourceSiteInfoUrl, destinationSiteInfoUrl, sourceSite, destinationSite);

        // Simulate no update from transferSiteResource (non-resource URL case).
        doReturn(sourceSiteInfoUrl).when(siteManageService).transferSiteResource(oldSiteId, newSiteId, sourceSiteInfoUrl);

        siteManageService.importToolContent(oldSiteId, destinationSite, false);

        InOrder inOrder = inOrder(siteManageService, destinationSite, siteService);
        inOrder.verify(siteManageService).transferSiteResource(oldSiteId, newSiteId, sourceSiteInfoUrl);
        inOrder.verify(destinationSite).setInfoUrl(expectedSiteInfoUrl);
        inOrder.verify(siteService).save(destinationSite);
    }

    @Test
    public void importToolContentFallsBackToSiteIdReplacementWhenTransferReturnsBlank() throws Exception {

        final String oldSiteId = "site-old";
        final String newSiteId = "site-new";
        final String sourceSiteInfoUrl = "https://sakai.example.edu/portal/site/site-old";
        final String destinationSiteInfoUrl = "https://sakai.example.edu/portal/site/site-new";
        final String expectedSiteInfoUrl = "https://sakai.example.edu/portal/site/site-new";

        Site sourceSite = mock(Site.class);
        Site destinationSite = mock(Site.class);
        mockImportContext(oldSiteId, newSiteId, sourceSiteInfoUrl, destinationSiteInfoUrl, sourceSite, destinationSite);

        // Simulate transfer failure after resolving a resource.
        doReturn("").when(siteManageService).transferSiteResource(oldSiteId, newSiteId, sourceSiteInfoUrl);

        siteManageService.importToolContent(oldSiteId, destinationSite, false);

        verify(siteManageService).transferSiteResource(oldSiteId, newSiteId, sourceSiteInfoUrl);
        verify(destinationSite).setInfoUrl(expectedSiteInfoUrl);
        verify(destinationSite, never()).setInfoUrl("");
        verify(siteService).save(destinationSite);
    }

    @Test
    public void importToolsIntoSiteUpdatesSiteInfoUrlDuringOverviewImport() throws Exception {

        final String oldSiteId = "site-old";
        final String newSiteId = "site-new";
        final String sourceSiteInfoUrl = "https://sakai.example.edu/portal/site/site-old";
        final String sourceSiteDescription = "Imported site description";
        final String expectedSiteInfoUrl = "https://sakai.example.edu/portal/site/site-new";

        Site sourceSite = mock(Site.class);
        Site destinationSite = mock(Site.class);
        ResourceProperties sourceSiteProperties = mock(ResourceProperties.class);
        ResourcePropertiesEdit destinationSiteProperties = mock(ResourcePropertiesEdit.class);

        when(sourceSite.getDescription()).thenReturn(sourceSiteDescription);
        when(sourceSite.getInfoUrl()).thenReturn(sourceSiteInfoUrl);
        when(sourceSite.getProperties()).thenReturn(sourceSiteProperties);
        when(sourceSiteProperties.getProperty(LTICustomVars.CONTEXT_ID_HISTORY)).thenReturn(null);

        when(destinationSite.getId()).thenReturn(newSiteId);
        when(destinationSite.getPropertiesEdit()).thenReturn(destinationSiteProperties);

        when(siteService.getSite(oldSiteId)).thenReturn(sourceSite);
        when(siteService.getSite(newSiteId)).thenReturn(destinationSite);

        // Simulate no update from transferSiteResource (non-resource URL case).
        doReturn(sourceSiteInfoUrl).when(siteManageService).transferSiteResource(oldSiteId, newSiteId, sourceSiteInfoUrl);

        Map<String, List<String>> importTools = new HashMap<>();
        importTools.put(SiteManageConstants.SITE_INFO_TOOL_ID, List.of(oldSiteId));

        siteManageService.importToolsIntoSite(
            destinationSite,
            new ArrayList<>(List.of(SiteManageConstants.SITE_INFO_TOOL_ID)),
            importTools,
            Collections.emptyMap(),
            Collections.emptyMap(),
            false
        );

        verify(siteManageService).transferSiteResource(oldSiteId, newSiteId, sourceSiteInfoUrl);
        verify(destinationSite).setDescription(sourceSiteDescription);
        verify(destinationSite).setInfoUrl(expectedSiteInfoUrl);
        verify(destinationSite, never()).setInfoUrl("");
        verify(siteService, atLeastOnce()).save(destinationSite);
    }

    @Test
    public void importToolsIntoSiteFallsBackToSiteIdReplacementWhenTransferReturnsBlank() throws Exception {

        final String oldSiteId = "site-old";
        final String newSiteId = "site-new";
        final String sourceSiteInfoUrl = "https://sakai.example.edu/portal/site/site-old";
        final String sourceSiteDescription = "Imported site description";
        final String expectedSiteInfoUrl = "https://sakai.example.edu/portal/site/site-new";

        Site sourceSite = mock(Site.class);
        Site destinationSite = mock(Site.class);
        ResourceProperties sourceSiteProperties = mock(ResourceProperties.class);
        ResourcePropertiesEdit destinationSiteProperties = mock(ResourcePropertiesEdit.class);

        when(sourceSite.getDescription()).thenReturn(sourceSiteDescription);
        when(sourceSite.getInfoUrl()).thenReturn(sourceSiteInfoUrl);
        when(sourceSite.getProperties()).thenReturn(sourceSiteProperties);
        when(sourceSiteProperties.getProperty(LTICustomVars.CONTEXT_ID_HISTORY)).thenReturn(null);

        when(destinationSite.getId()).thenReturn(newSiteId);
        when(destinationSite.getPropertiesEdit()).thenReturn(destinationSiteProperties);

        when(siteService.getSite(oldSiteId)).thenReturn(sourceSite);
        when(siteService.getSite(newSiteId)).thenReturn(destinationSite);

        // Simulate transfer failure after resolving a resource.
        doReturn("").when(siteManageService).transferSiteResource(oldSiteId, newSiteId, sourceSiteInfoUrl);

        Map<String, List<String>> importTools = new HashMap<>();
        importTools.put(SiteManageConstants.SITE_INFO_TOOL_ID, List.of(oldSiteId));

        siteManageService.importToolsIntoSite(
            destinationSite,
            new ArrayList<>(List.of(SiteManageConstants.SITE_INFO_TOOL_ID)),
            importTools,
            Collections.emptyMap(),
            Collections.emptyMap(),
            false
        );

        verify(siteManageService).transferSiteResource(oldSiteId, newSiteId, sourceSiteInfoUrl);
        verify(destinationSite).setDescription(sourceSiteDescription);
        verify(destinationSite).setInfoUrl(expectedSiteInfoUrl);
        verify(destinationSite, never()).setInfoUrl("");
        verify(siteService, atLeastOnce()).save(destinationSite);
    }

    private SitePage mockSiteInfoPage() {
        SitePage page = mock(SitePage.class);
        ToolConfiguration toolConfiguration = mock(ToolConfiguration.class);
        Tool tool = mock(Tool.class);

        when(tool.getId()).thenReturn(SiteManageConstants.SITE_INFO_TOOL_ID);
        when(toolConfiguration.getTool()).thenReturn(tool);
        when(page.getTools()).thenReturn(List.of(toolConfiguration));

        return page;
    }

    private void mockImportContext(String oldSiteId, String newSiteId, String sourceSiteInfoUrl, String destinationSiteInfoUrl, Site sourceSite, Site destinationSite) throws Exception {
        SitePage siteInfoPage = mockSiteInfoPage();
        String destinationSiteRef = "/site/" + newSiteId;

        when(sourceSite.getInfoUrl()).thenReturn(sourceSiteInfoUrl);
        when(sourceSite.getRoles()).thenReturn(Collections.emptySet());

        when(destinationSite.getId()).thenReturn(newSiteId);
        when(destinationSite.getInfoUrl()).thenReturn(destinationSiteInfoUrl);
        when(destinationSite.getPages()).thenReturn(List.of(siteInfoPage));

        when(siteService.getSite(oldSiteId)).thenReturn(sourceSite);
        when(siteService.siteReference(newSiteId)).thenReturn(destinationSiteRef);

        AuthzGroup destinationRealm = mock(AuthzGroup.class);
        when(functionManager.getRegisteredFunctions()).thenReturn(Collections.emptyList());
        when(authzGroupService.getAuthzGroup(destinationSiteRef)).thenReturn(destinationRealm);
    }
}
