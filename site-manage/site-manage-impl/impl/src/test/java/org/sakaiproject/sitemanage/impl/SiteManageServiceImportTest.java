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
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.sitemanage.api.SiteManageConstants;
import org.sakaiproject.sitemanage.api.SiteManageService;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.tsugi.lti13.LTICustomVars;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Tests the public import service using its production Spring wiring.
 */
public class SiteManageServiceImportTest {

    private static final String ANNOUNCEMENTS_TOOL_ID = "sakai.announcements";

    private SiteManageService siteManageService;
    private SiteManageTestConfiguration configuration;
    private SiteService siteService;
    private AuthzGroupService authzGroupService;
    private FunctionManager functionManager;
    private ServerConfigurationService serverConfigurationService;
    private EntityManager entityManager;

    @Before
    public void setUp() {
        configuration = new SiteManageTestConfiguration();
        siteManageService = configuration.getBean(SiteManageService.class);
        siteService = configuration.getBean(SiteService.class);
        authzGroupService = configuration.getBean(AuthzGroupService.class);
        functionManager = configuration.getBean(FunctionManager.class);
        serverConfigurationService = configuration.getBean(ServerConfigurationService.class);
        entityManager = configuration.getBean(EntityManager.class);
        when(entityManager.getEntityProducers()).thenReturn(Collections.emptyList());
    }

    @After
    public void tearDown() {
        configuration.close();
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

        mockSiteInfoResourceTransfer(transferResultSiteInfoUrl);

        siteManageService.importToolContent(oldSiteId, destinationSite, false);

        verify(destinationSite).setInfoUrl(expectedSiteInfoUrl);
        verify(siteService, atLeastOnce()).save(destinationSite);
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


        siteManageService.importToolContent(oldSiteId, destinationSite, false);

        InOrder inOrder = inOrder(destinationSite, siteService);
        inOrder.verify(destinationSite).setInfoUrl(expectedSiteInfoUrl);
        inOrder.verify(siteService, atLeastOnce()).save(destinationSite);
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
        mockSiteInfoResourceTransfer("");

        siteManageService.importToolContent(oldSiteId, destinationSite, false);

        verify(destinationSite).setInfoUrl(expectedSiteInfoUrl);
        verify(destinationSite, never()).setInfoUrl("");
        verify(siteService, atLeastOnce()).save(destinationSite);
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
        mockSiteInfoResourceTransfer("");

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

        verify(destinationSite).setDescription(sourceSiteDescription);
        verify(destinationSite).setInfoUrl(expectedSiteInfoUrl);
        verify(destinationSite, never()).setInfoUrl("");
        verify(siteService, atLeastOnce()).save(destinationSite);
    }

    @Test
    public void importToolsIntoSiteImportsAnnouncementsBeforeAssignments() throws Exception {

        final String oldSiteId = "site-old";
        final String newSiteId = "site-new";

        Site sourceSite = mock(Site.class);
        Site destinationSite = mock(Site.class);
        ResourceProperties sourceSiteProperties = mock(ResourceProperties.class);
        ResourcePropertiesEdit destinationSiteProperties = mock(ResourcePropertiesEdit.class);

        when(sourceSite.getProperties()).thenReturn(sourceSiteProperties);
        when(sourceSiteProperties.getProperty(LTICustomVars.CONTEXT_ID_HISTORY)).thenReturn(null);
        when(destinationSite.getId()).thenReturn(newSiteId);
        when(destinationSite.getPropertiesEdit()).thenReturn(destinationSiteProperties);
        when(siteService.getSite(oldSiteId)).thenReturn(sourceSite);
        when(siteService.getSite(newSiteId)).thenReturn(destinationSite);

        EntityProducer assignmentProducer = mock(EntityProducer.class, withSettings().extraInterfaces(EntityTransferrer.class));
        EntityProducer announcementProducer = mock(EntityProducer.class, withSettings().extraInterfaces(EntityTransferrer.class));
        EntityTransferrer assignmentTransferrer = (EntityTransferrer) assignmentProducer;
        EntityTransferrer announcementTransferrer = (EntityTransferrer) announcementProducer;

        when(assignmentTransferrer.myToolIds()).thenReturn(new String[] { "sakai.assignment.grades" });
        when(announcementTransferrer.myToolIds()).thenReturn(new String[] { ANNOUNCEMENTS_TOOL_ID });
        when(assignmentTransferrer.transferCopyEntities(oldSiteId, newSiteId, null, null, true)).thenReturn(Collections.emptyMap());
        when(announcementTransferrer.transferCopyEntities(oldSiteId, newSiteId, null, null, true)).thenReturn(Collections.emptyMap());
        when(entityManager.getEntityProducers()).thenReturn(List.of(assignmentProducer, announcementProducer));

        Map<String, List<String>> importTools = new HashMap<>();
        importTools.put("sakai.assignment.grades", List.of(oldSiteId));
        importTools.put(ANNOUNCEMENTS_TOOL_ID, List.of(oldSiteId));

        siteManageService.importToolsIntoSite(
            destinationSite,
            new ArrayList<>(List.of("sakai.assignment.grades", ANNOUNCEMENTS_TOOL_ID)),
            importTools,
            Collections.emptyMap(),
            Collections.emptyMap(),
            true
        );

        InOrder inOrder = inOrder(announcementTransferrer, assignmentTransferrer);
        inOrder.verify(announcementTransferrer).transferCopyEntities(oldSiteId, newSiteId, null, null, true);
        inOrder.verify(assignmentTransferrer).transferCopyEntities(oldSiteId, newSiteId, null, null, true);
        verify(announcementTransferrer).transferCopyEntities(oldSiteId, newSiteId, null, null, true);
        verify(assignmentTransferrer).transferCopyEntities(oldSiteId, newSiteId, null, null, true);
    }

    @Test
    public void importToolsIntoSiteImportsAnnouncementItemsBeforeAssignments() throws Exception {

        final String oldSiteId = "site-old";
        final String newSiteId = "site-new";
        final List<String> announcementItems = List.of("announcement-1");

        Site sourceSite = mock(Site.class);
        Site destinationSite = mock(Site.class);
        ResourceProperties sourceSiteProperties = mock(ResourceProperties.class);
        ResourcePropertiesEdit destinationSiteProperties = mock(ResourcePropertiesEdit.class);

        when(sourceSite.getProperties()).thenReturn(sourceSiteProperties);
        when(sourceSiteProperties.getProperty(LTICustomVars.CONTEXT_ID_HISTORY)).thenReturn(null);
        when(destinationSite.getId()).thenReturn(newSiteId);
        when(destinationSite.getPropertiesEdit()).thenReturn(destinationSiteProperties);
        when(siteService.getSite(oldSiteId)).thenReturn(sourceSite);
        when(siteService.getSite(newSiteId)).thenReturn(destinationSite);

        EntityProducer assignmentProducer = mock(EntityProducer.class, withSettings().extraInterfaces(EntityTransferrer.class));
        EntityProducer announcementProducer = mock(EntityProducer.class, withSettings().extraInterfaces(EntityTransferrer.class));
        EntityTransferrer assignmentTransferrer = (EntityTransferrer) assignmentProducer;
        EntityTransferrer announcementTransferrer = (EntityTransferrer) announcementProducer;

        when(assignmentTransferrer.myToolIds()).thenReturn(new String[] { "sakai.assignment.grades" });
        when(announcementTransferrer.myToolIds()).thenReturn(new String[] { ANNOUNCEMENTS_TOOL_ID });
        when(assignmentTransferrer.transferCopyEntities(oldSiteId, newSiteId, null, null, true)).thenReturn(Collections.emptyMap());
        when(announcementTransferrer.transferCopyEntities(oldSiteId, newSiteId, announcementItems, null, true)).thenReturn(Collections.emptyMap());
        when(entityManager.getEntityProducers()).thenReturn(List.of(assignmentProducer, announcementProducer));

        Map<String, List<String>> importTools = new HashMap<>();
        importTools.put("sakai.assignment.grades", List.of(oldSiteId));

        Map<String, Map<String, List<String>>> toolItemMap = new HashMap<>();
        toolItemMap.put(ANNOUNCEMENTS_TOOL_ID, Map.of(oldSiteId, announcementItems));

        siteManageService.importToolsIntoSite(
            destinationSite,
            new ArrayList<>(List.of("sakai.assignment.grades", ANNOUNCEMENTS_TOOL_ID)),
            importTools,
            toolItemMap,
            Collections.emptyMap(),
            true
        );

        InOrder inOrder = inOrder(announcementTransferrer, assignmentTransferrer);
        inOrder.verify(announcementTransferrer).transferCopyEntities(oldSiteId, newSiteId, announcementItems, null, true);
        inOrder.verify(assignmentTransferrer).transferCopyEntities(oldSiteId, newSiteId, null, null, true);
        verify(announcementTransferrer).transferCopyEntities(oldSiteId, newSiteId, announcementItems, null, true);
        verify(assignmentTransferrer).transferCopyEntities(oldSiteId, newSiteId, null, null, true);
    }

    @Test
    public void fullImportSelectsAllOptionsAndOrdersToolsAcrossEveryPlacement() throws Exception {
        String sourceId = "site-old";
        String destinationId = "site-new";
        Site source = mock(Site.class);
        Site destination = mock(Site.class);
        mockImportContext(sourceId, destinationId, null, null, source, destination);
        String assignments = "sakai.assignment.grades";
        String gradebook = SiteManageConstants.GRADEBOOK_TOOL_ID;
        String resources = SiteManageConstants.RESOURCES_TOOL_ID;
        // Deliberately reverse the dependency order and repeat a tool on another page.
        SitePage firstPage = mock(SitePage.class);
        SitePage secondPage = mock(SitePage.class);
        List<ToolConfiguration> firstTools = List.of(placement(assignments), placement(ANNOUNCEMENTS_TOOL_ID));
        List<ToolConfiguration> secondTools = List.of(placement(gradebook), placement(resources), placement(assignments));
        when(firstPage.getTools()).thenReturn(firstTools);
        when(secondPage.getTools()).thenReturn(secondTools);
        when(destination.getPages()).thenReturn(List.of(firstPage, secondPage));
        ContentHostingService content = configuration.getBean(ContentHostingService.class);
        when(content.getSiteCollection(sourceId)).thenReturn("/group/site-old/");
        when(content.getSiteCollection(destinationId)).thenReturn("/group/site-new/");

        EntityTransferrer assignment = transferrer(assignments, List.of(EntityTransferrer.COPY_PERMISSIONS_OPTION, EntityTransferrer.PUBLISH_OPTION));
        EntityTransferrer announcement = transferrer(ANNOUNCEMENTS_TOOL_ID, List.of(EntityTransferrer.COPY_PERMISSIONS_OPTION));
        EntityTransferrer grades = transferrer(gradebook, List.of(EntityTransferrer.COPY_SETTINGS_OPTION));
        EntityTransferrer resource = transferrer(resources, List.of());
        when(entityManager.getEntityProducers()).thenReturn(List.of(
            (EntityProducer) assignment, (EntityProducer) announcement, (EntityProducer) grades, (EntityProducer) resource));
        Map<String, String> resourceLinks = Map.of("/group/site-old/file", "/group/site-new/file");
        when(resource.transferCopyEntities("/group/site-old/", "/group/site-new/", null, List.of(), true)).thenReturn(resourceLinks);

        Role sourceRole = mock(Role.class);
        Role destinationRole = mock(Role.class);
        when(source.getRoles()).thenReturn(Set.of(sourceRole));
        when(sourceRole.getId()).thenReturn("access");
        when(sourceRole.isAllowed("asn.submit")).thenReturn(true);
        when(assignment.getToolPermissionsPrefix()).thenReturn("asn.");
        when(assignment.supportsTransferOption(EntityTransferrer.COPY_PERMISSIONS_OPTION)).thenReturn(true);
        when(functionManager.getRegisteredFunctions("asn.")).thenReturn(List.of("asn.submit", "asn.new"));
        AuthzGroup realm = authzGroupService.getAuthzGroup("/site/" + destinationId);
        when(realm.getRole("access")).thenReturn(destinationRole);
        ToolConfiguration sourcePlacement = placement(assignments);
        ToolConfiguration destinationPlacement = placement(assignments);
        when(sourcePlacement.getId()).thenReturn("assignment-old");
        when(destinationPlacement.getId()).thenReturn("assignment-new");
        when(serverConfigurationService.getPortalUrl()).thenReturn("https://sakai.example.edu/portal");
        Properties sourceConfig = new Properties();
        Properties destinationConfig = new Properties();
        sourceConfig.setProperty(ToolManager.PORTAL_VISIBLE, "false");
        when(sourcePlacement.getPlacementConfig()).thenReturn(sourceConfig);
        when(destinationPlacement.getPlacementConfig()).thenReturn(destinationConfig);
        when(source.getTools(assignments)).thenReturn(List.of(sourcePlacement));
        when(destination.getTools(assignments)).thenReturn(List.of(destinationPlacement));

        // Enabling missing-tool import must not change the selection or duplicate transfers.
        when(serverConfigurationService.getBoolean("site.setup.import.addmissingtools", true)).thenReturn(true);
        siteManageService.importToolContent(sourceId, destination, false);

        InOrder order = inOrder(resource, grades, announcement, assignment);
        order.verify(resource).transferCopyEntities("/group/site-old/", "/group/site-new/", null, List.of(), true);
        order.verify(grades).transferCopyEntities(sourceId, destinationId, null, List.of(EntityTransferrer.COPY_SETTINGS_OPTION), true);
        order.verify(announcement).transferCopyEntities(sourceId, destinationId, null, List.of(EntityTransferrer.COPY_PERMISSIONS_OPTION), true);
        order.verify(assignment).transferCopyEntities(sourceId, destinationId, null,
            List.of(EntityTransferrer.COPY_PERMISSIONS_OPTION, EntityTransferrer.PUBLISH_OPTION), true);
        verify(assignment).transferCopyEntities(sourceId, destinationId, null,
            List.of(EntityTransferrer.COPY_PERMISSIONS_OPTION, EntityTransferrer.PUBLISH_OPTION), true);
        Map<String, String> expectedLinks = new HashMap<>(resourceLinks);
        expectedLinks.put("https://sakai.example.edu/portal/directtool/assignment-old/",
            "https://sakai.example.edu/portal/directtool/assignment-new/");
        verify(assignment).updateEntityReferences(destinationId, expectedLinks);
        verify(resource).updateEntityReferences(destinationId, expectedLinks);
        verify(destinationRole).allowFunction("asn.submit");
        verify(destinationRole).disallowFunction("asn.new");
        assertEquals("false", destinationConfig.getProperty(ToolManager.PORTAL_VISIBLE));
        verify(destinationPlacement).save();
        verify(destination.getPropertiesEdit()).addProperty(LTICustomVars.CONTEXT_ID_HISTORY, sourceId);
    }

    @Test
    public void fullImportIncludesSiteInformationWithoutAddingAnOverviewPlacement() throws Exception {
        Site source = mock(Site.class);
        Site destination = mock(Site.class);
        mockImportContext("site-old", "site-new", "https://sakai.example.edu/portal/site/site-old", null, source, destination);
        when(source.getDescription()).thenReturn("Source description");
        when(destination.getPages()).thenReturn(List.of());
        when(serverConfigurationService.getBoolean("site-manage.importoption.siteinfo", true)).thenReturn(true);
        when(serverConfigurationService.getBoolean("site.setup.import.addmissingtools", true)).thenReturn(true);

        siteManageService.importToolContent("site-old", destination, false);

        verify(destination).setDescription("Source description");
        verify(destination).setInfoUrl("https://sakai.example.edu/portal/site/site-new");
        verify(destination, never()).addPage();
    }

    @Test
    public void templateImportAlwaysRemovesItsSecurityAdvisor() throws Exception {
        Site destination = mock(Site.class);
        when(destination.getId()).thenReturn("site-new");
        when(destination.getPages()).thenThrow(new IllegalStateException("Cannot read placements"));
        SecurityService security = configuration.getBean(SecurityService.class);

        siteManageService.importToolContent("site-old", destination, true);

        ArgumentCaptor<SecurityAdvisor> advisor =
            ArgumentCaptor.forClass(SecurityAdvisor.class);
        verify(security).pushAdvisor(advisor.capture());
        verify(security).popAdvisor(advisor.getValue());
    }

    private ToolConfiguration placement(String toolId) {
        ToolConfiguration placement = mock(ToolConfiguration.class);
        when(placement.getToolId()).thenReturn(toolId);
        return placement;
    }

    private EntityTransferrer transferrer(String toolId, List<String> options) {
        EntityProducer producer = mock(EntityProducer.class, withSettings().extraInterfaces(EntityTransferrer.class));
        EntityTransferrer transferrer = (EntityTransferrer) producer;
        when(transferrer.myToolIds()).thenReturn(new String[] { toolId });
        when(transferrer.getTransferOptions()).thenReturn(Optional.of(options));
        return transferrer;
    }

    private void mockSiteInfoResourceTransfer(String transferredUrl) throws Exception {
        ContentHostingService content = configuration.getBean(ContentHostingService.class);
        when(serverConfigurationService.getAccessUrl()).thenReturn("https://sakai.example.edu/access");
        Reference reference = mock(Reference.class);
        when(entityManager.newReference(any(String.class))).thenReturn(reference);
        when(reference.getId()).thenReturn("/group/site-old/site-info");
        ContentResource source = mock(ContentResource.class);
        ContentResource destination = mock(ContentResource.class);
        when(content.getResource("/group/site-old/site-info")).thenReturn(source);
        when(source.getId()).thenReturn("/group/site-old/site-info");
        when(content.getResource("/group/site-new/site-info")).thenReturn(destination);
        when(destination.getUrl(false)).thenReturn(transferredUrl);
    }

    private SitePage mockSiteInfoPage() {
        SitePage page = mock(SitePage.class);
        ToolConfiguration toolConfiguration = mock(ToolConfiguration.class);
        Tool tool = mock(Tool.class);

        when(tool.getId()).thenReturn(SiteManageConstants.SITE_INFO_TOOL_ID);
        when(toolConfiguration.getTool()).thenReturn(tool);
        when(toolConfiguration.getToolId()).thenReturn(SiteManageConstants.SITE_INFO_TOOL_ID);
        when(page.getTools()).thenReturn(List.of(toolConfiguration));

        return page;
    }

    private void mockImportContext(String oldSiteId, String newSiteId, String sourceSiteInfoUrl, String destinationSiteInfoUrl, Site sourceSite, Site destinationSite) throws Exception {
        SitePage siteInfoPage = mockSiteInfoPage();
        String destinationSiteRef = "/site/" + newSiteId;

        when(sourceSite.getInfoUrl()).thenReturn(sourceSiteInfoUrl);
        when(sourceSite.getRoles()).thenReturn(Collections.emptySet());
        when(sourceSite.getProperties()).thenReturn(mock(ResourceProperties.class));
        when(destinationSite.getPropertiesEdit()).thenReturn(mock(ResourcePropertiesEdit.class));
        when(siteService.getSite(newSiteId)).thenReturn(destinationSite);

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
