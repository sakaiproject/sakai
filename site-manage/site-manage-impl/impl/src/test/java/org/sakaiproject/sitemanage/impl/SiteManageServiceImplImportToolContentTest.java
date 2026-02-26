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

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.sitemanage.api.SiteManageConstants;
import org.sakaiproject.tool.api.Tool;

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

    @Before
    public void setUp() {
        siteManageService = spy(new SiteManageServiceImpl());
        siteService = mock(SiteService.class);
        authzGroupService = mock(AuthzGroupService.class);
        functionManager = mock(FunctionManager.class);

        siteManageService.setSiteService(siteService);
        siteManageService.setAuthzGroupService(authzGroupService);
        siteManageService.setFunctionManager(functionManager);
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
        SitePage siteInfoPage = mockSiteInfoPage();

        when(sourceSite.getInfoUrl()).thenReturn(sourceSiteInfoUrl);
        when(sourceSite.getRoles()).thenReturn(Collections.emptySet());

        when(destinationSite.getId()).thenReturn(newSiteId);
        when(destinationSite.getInfoUrl()).thenReturn(destinationSiteInfoUrl);
        when(destinationSite.getPages()).thenReturn(List.of(siteInfoPage));

        when(siteService.getSite(oldSiteId)).thenReturn(sourceSite);
        when(siteService.siteReference(newSiteId)).thenReturn("/site/" + newSiteId);

        AuthzGroup destinationRealm = mock(AuthzGroup.class);
        when(functionManager.getRegisteredFunctions()).thenReturn(Collections.emptyList());
        when(authzGroupService.getAuthzGroup("/site/" + newSiteId)).thenReturn(destinationRealm);

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
        final String expectedSiteInfoUrl = "https://sakai.example.edu/portal/site/site-new";

        Site sourceSite = mock(Site.class);
        Site destinationSite = mock(Site.class);
        SitePage siteInfoPage = mockSiteInfoPage();

        when(sourceSite.getInfoUrl()).thenReturn(sourceSiteInfoUrl);
        when(sourceSite.getRoles()).thenReturn(Collections.emptySet());

        when(destinationSite.getId()).thenReturn(newSiteId);
        when(destinationSite.getPages()).thenReturn(List.of(siteInfoPage));

        when(siteService.getSite(oldSiteId)).thenReturn(sourceSite);
        when(siteService.siteReference(newSiteId)).thenReturn("/site/" + newSiteId);

        AuthzGroup destinationRealm = mock(AuthzGroup.class);
        when(functionManager.getRegisteredFunctions()).thenReturn(Collections.emptyList());
        when(authzGroupService.getAuthzGroup("/site/" + newSiteId)).thenReturn(destinationRealm);

        // Simulate no update from transferSiteResource (non-resource URL case).
        doReturn(sourceSiteInfoUrl).when(siteManageService).transferSiteResource(oldSiteId, newSiteId, sourceSiteInfoUrl);

        siteManageService.importToolContent(oldSiteId, destinationSite, false);

        InOrder inOrder = inOrder(siteManageService, destinationSite, siteService);
        inOrder.verify(siteManageService).transferSiteResource(oldSiteId, newSiteId, sourceSiteInfoUrl);
        inOrder.verify(destinationSite).setInfoUrl(expectedSiteInfoUrl);
        inOrder.verify(siteService).save(destinationSite);
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
}
