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
package org.sakaiproject.sitestats.test;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import org.sakaiproject.serialization.MapperFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.StatsAuthz;
import org.sakaiproject.sitestats.api.view.SiteStatsResourceSearchService;
import org.sakaiproject.sitestats.api.view.SiteStatsResourceSearchService.ResourceOption;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.BaseResourceProperties;
import org.sakaiproject.util.api.LocaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SiteStatsTestConfiguration.class)
@Transactional
public class SiteStatsResourceSearchServiceTest {

    private static final String SITE = "site1";
    private static final String ROOT = "/group/" + SITE + "/";
    private static final String DROPBOX = "/group-user/" + SITE + "/";
    private static final String ATTACHMENTS = "/attachment/" + SITE + "/";

    @Autowired private SiteStatsResourceSearchService service;
    @Autowired private ContentHostingService content;
    @Autowired private SecurityService security;
    @Autowired private SiteService sites;
    @Autowired private ToolManager tools;
    @Autowired private SessionManager sessions;
    @Autowired private LocaleService locales;

    private final Map<String, List<ContentResource>> readable = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        reset(content, security, sites, sessions, locales);
        clearInvocations(tools);
        readable.clear();
        Session session = mock(Session.class);
        when(session.getUserId()).thenReturn("instructor");
        when(sessions.getCurrentSession()).thenReturn(session);
        when(sessions.getCurrentSessionUserId()).thenReturn("instructor");
        when(sites.siteReference(anyString())).thenAnswer(call -> "/site/" + call.getArgument(0));
        allow(SITE, StatsAuthz.PERMISSION_SITESTATS_VIEW);
        allow(SITE, StatsAuthz.PERMISSION_SITESTATS_ALL);
        when(locales.getLocaleForCurrentSiteAndUser()).thenReturn(Locale.US);
        when(locales.getLocaleForSiteAndUser(anyString(), anyString())).thenReturn(Locale.US);
        when(content.getSiteCollection(anyString())).thenAnswer(call -> "/group/" + call.getArgument(0) + "/");
        when(content.getDropboxCollection(anyString())).thenAnswer(call -> "/group-user/" + call.getArgument(0) + "/");
        when(content.getAllResources(anyString())).thenAnswer(call -> readable.getOrDefault(call.getArgument(0), List.of()));
    }

    @Test
    public void loadsReadableMetadataAcrossAllThreeRootsWithoutCollectionsOrBodies() throws Exception {
        folder(ROOT + "week-one/", "Week One");
        folder(ROOT + "week-one/readings/", "Readings");
        ContentResource nested = resource(ROOT, ROOT + "week-one/readings/file.txt", "Reading Notes", ResourceType.TYPE_TEXT);
        resource(DROPBOX, DROPBOX + "upload.txt", "Reading Upload", ResourceType.TYPE_UPLOAD);
        resource(ATTACHMENTS, ATTACHMENTS + "file.url", "Reading Link", ResourceType.TYPE_URL);
        List<ResourceOption> result = service.resources(SITE);
        assertEquals(3, result.size());
        assertEquals("Resources / Week One / Readings", result.get(1).location());
        assertTrue(result.stream().allMatch(item -> !item.id().startsWith("/content")));
        assertTrue(result.stream().noneMatch(ResourceOption::legacyCollection));
        verify(nested, never()).getContent();
        verify(nested, never()).streamContent();
        verify(content).getAllResources(ROOT);
        verify(content).getAllResources(DROPBOX);
        verify(content).getAllResources(ATTACHMENTS);
    }

    @Test
    public void preservesLiteralDisplayNamesAndReadableLocationsForLocalFiltering() throws Exception {
        resource(ROOT, ROOT + "unicode.txt", "Résumé [50%]_星.txt", ResourceType.TYPE_UPLOAD);
        folder(ROOT + "internal-folder/", "ÉTÉ [50%]_星");
        resource(ROOT, ROOT + "internal-folder/reading.txt", "Reading.pdf", ResourceType.TYPE_UPLOAD);
        List<ResourceOption> result = service.resources(SITE);
        assertEquals("Reading.pdf", result.get(0).label());
        assertEquals("Resources / ÉTÉ [50%]_星", result.get(0).location());
        assertEquals("Résumé [50%]_星.txt", result.get(1).label());
        assertFalse(result.get(0).location().contains("internal-folder"));
    }

    @Test
    public void keepsDuplicateNamesDistinctWithReadableLocationsAndStableIdTieBreak() throws Exception {
        folder(ROOT + "a/", "First Week");
        folder(ROOT + "b/", "Second Week");
        resource(ROOT, ROOT + "b/file.txt", "Reading.pdf", ResourceType.TYPE_UPLOAD);
        resource(ROOT, ROOT + "a/file.txt", "Reading.pdf", ResourceType.TYPE_UPLOAD);
        List<ResourceOption> result = service.resources(SITE);
        assertEquals(ROOT + "a/file.txt", result.get(0).id());
        assertEquals("Resources / First Week", result.get(0).location());
        assertEquals("Resources / Second Week", result.get(1).location());
    }

    @Test
    public void returnsAllTwoThousandResourcesSortedForLocalRefinement() throws Exception {
        for (int i = 1999; i >= 0; i--) {
            String name = String.format(Locale.ROOT, "File %04d", i);
            resource(ROOT, ROOT + i + ".txt", name, ResourceType.TYPE_UPLOAD);
        }
        List<ResourceOption> result = service.resources(SITE);
        assertEquals(2000, result.size());
        assertEquals("File 0000", result.get(0).label());
        assertEquals("File 1999", result.get(1999).label());
        assertEquals(ROOT + "1999.txt", result.get(1999).id());
        verify(content).getAllResources(ROOT);
        verify(content).getAllResources(DROPBOX);
        verify(content).getAllResources(ATTACHMENTS);
        verify(content, never()).getResource(anyString());
    }

    @Test
    public void boundsParentReadsForTwoThousandResourcesWithSharedLocations() throws Exception {
        for (int week = 0; week < 100; week++) {
            String folderId = ROOT + "unit-" + week + "/";
            folder(folderId, "Week " + week);
            for (int file = 0; file < 20; file++) {
                resource(ROOT, folderId + file + ".txt", "Reading " + file, ResourceType.TYPE_UPLOAD);
            }
        }
        List<ResourceOption> result = service.resources(SITE);
        assertEquals(2000, result.size());
        assertEquals(20, result.stream().filter(item -> "Resources / Week 99".equals(item.location())).count());
        verify(content, times(100)).getCollection(anyString());
    }

    @Test
    public void keepsPermissionDeniedLocationsNeutralWithoutLeakingHiddenNames() throws Exception {
        folder(ROOT + "private/", "Secret hidden week");
        when(content.getCollection(ROOT + "private/"))
                .thenThrow(new PermissionException("instructor", "content.read", ROOT + "private/"));
        for (int i = 0; i < 20; i++) {
            resource(ROOT, ROOT + "private/" + i + ".txt", "Readable Reading " + i, ResourceType.TYPE_UPLOAD);
        }
        List<ResourceOption> result = service.resources(SITE);
        assertEquals(20, result.size());
        assertTrue(result.stream().allMatch(item -> "Resources / Unavailable location".equals(item.location())));
        verify(content).getCollection(ROOT + "private/");
    }

    @Test
    public void authorizesMetadataAndSelectionLookupsBeforeEnumeratingAnything() throws Exception {
        when(security.unlock(StatsAuthz.PERMISSION_SITESTATS_VIEW, "/site/" + SITE)).thenReturn(false);
        assertThrows(SecurityException.class, () -> service.resources(SITE));
        assertThrows(SecurityException.class, () -> service.selected(SITE, List.of()));
        verifyNoInteractions(content);
    }

    @Test
    public void refusesUnauthorizedSitesUntilTheirReportPermissionsArePresent() throws Exception {
        assertThrows(SecurityException.class, () -> service.resources("site2"));
        verifyNoInteractions(content);
        allow("site2", StatsAuthz.PERMISSION_SITESTATS_VIEW);
        allow("site2", StatsAuthz.PERMISSION_SITESTATS_ALL);
        resource("/group/site2/", "/group/site2/file.txt", "Reading", ResourceType.TYPE_UPLOAD);
        assertEquals("/group/site2/file.txt", service.resources("site2").get(0).id());
        verify(content, never()).getAllResources(ROOT);
    }

    @Test
    public void reflectsKernelReadChangesOnReloadAndWhenResolvingSelections() throws Exception {
        String id = ROOT + "file.txt";
        resource(ROOT, id, "Reading", ResourceType.TYPE_UPLOAD);
        assertEquals(1, service.resources(SITE).size());
        readable.get(ROOT).clear();
        when(content.getResource(id)).thenThrow(new PermissionException("instructor", "content.read", id));
        assertTrue(service.resources(SITE).isEmpty());
        assertEquals("Unavailable resource", service.selected(SITE, List.of(id)).get(0).label());
        assertEquals("", service.selected(SITE, List.of(id)).get(0).location());
    }

    @Test
    public void serializesOnlyResourceMetadataWithoutBodiesOrServerSearchState() throws Exception {
        for (int i = 0; i < 21; i++) {
            resource(ROOT, ROOT + i + ".txt", String.format(Locale.ROOT, "Reading %02d", i), ResourceType.TYPE_UPLOAD);
        }
        String json = MapperFactory.createDefaultJsonMapper().writeValueAsString(service.resources(SITE));
        JsonNode result = MapperFactory.createDefaultJsonMapper().readTree(json);
        assertTrue(result.isArray());
        assertEquals(21, result.size());
        assertEquals(ROOT + "0.txt", result.get(0).get("id").asText());
        assertEquals("Reading 00", result.get(0).get("label").asText());
        assertEquals("Resources", result.get(0).get("location").asText());
        assertEquals(4, result.get(0).size());
        assertFalse(result.get(0).get("legacyCollection").asBoolean());
    }

    @Test
    public void usesTheSitesEffectiveLocaleForLabelsAndOrdering() throws Exception {
        when(locales.getLocaleForSiteAndUser(SITE, "instructor")).thenReturn(Locale.FRANCE);
        resource(ROOT, ROOT + "z.txt", "Zèbre", ResourceType.TYPE_UPLOAD);
        resource(ROOT, ROOT + "a.txt", "Été", ResourceType.TYPE_UPLOAD);
        List<ResourceOption> result = service.resources(SITE);
        assertEquals(List.of("Été", "Zèbre"), result.stream().map(ResourceOption::label).toList());
        verify(locales).getLocaleForSiteAndUser(SITE, "instructor");
    }

    @Test
    public void leavesEmptySelectionsWithoutInventoryReads() {
        assertTrue(service.selected(SITE, List.of()).isEmpty());
        verifyNoInteractions(content);
    }

    @Test
    public void resolvesSavedFilesCollectionsAndGenericRootsAndKeepsUnavailableIdsNeutral() throws Exception {
        resource(ROOT, ROOT + "file.txt", "Reading", ResourceType.TYPE_UPLOAD);
        folder(ROOT + "folder/", "Previous folder selection");
        when(content.getResource(ROOT + "deleted.txt")).thenThrow(new IdUnusedException(ROOT + "deleted.txt"));
        when(content.getResource(ROOT + "private.txt")).thenThrow(new PermissionException("instructor", "content.read", ROOT + "private.txt"));
        List<String> ids = List.of(ROOT + "file.txt", ROOT + "folder/", "/group/", "/group-user/", "/attachment/",
                ROOT + "deleted.txt", ROOT + "private.txt", "/group/site2/leak.txt");
        List<ResourceOption> selected = service.selected(SITE, ids);
        assertEquals(ids, selected.stream().map(ResourceOption::id).toList());
        assertEquals("Reading", selected.get(0).label());
        assertTrue(selected.get(1).legacyCollection());
        for (ResourceOption unavailable : selected.subList(5, 8)) {
            assertEquals("Unavailable resource", unavailable.label());
            assertEquals("", unavailable.location());
        }
        verify(content, never()).getResource("/group/site2/leak.txt");
        verify(content, never()).getAllResources(anyString());
    }

    @Test
    public void keepsSavedIdsWhenTheirParentLocationIsDeniedOrDeletedWithoutLeakingPaths() throws Exception {
        resource(ROOT, ROOT + "private-folder/file.txt", "Readable file", ResourceType.TYPE_UPLOAD);
        resource(ROOT, ROOT + "stale-folder/file.txt", "Other file", ResourceType.TYPE_UPLOAD);
        when(content.getCollection(ROOT + "private-folder/"))
                .thenThrow(new PermissionException("instructor", "content.read", ROOT + "private-folder/"));
        when(content.getCollection(ROOT + "stale-folder/"))
                .thenThrow(new IdUnusedException(ROOT + "stale-folder/"));
        List<String> ids = List.of(ROOT + "private-folder/file.txt", ROOT + "stale-folder/file.txt");
        List<ResourceOption> options = service.selected(SITE, ids);
        assertEquals(ids, options.stream().map(ResourceOption::id).toList());
        for (ResourceOption option : options) {
            assertEquals("Resources / Unavailable location", option.location());
            assertFalse(option.location().contains("private-folder"));
            assertFalse(option.location().contains("stale-folder"));
        }
    }

    private void allow(String site, String permission) {
        when(security.unlock(permission, "/site/" + site)).thenReturn(true);
    }

    private ContentResource resource(String root, String id, String label, String type) throws Exception {
        ContentResource resource = mock(ContentResource.class);
        when(resource.getId()).thenReturn(id);
        when(resource.getResourceType()).thenReturn(type);
        when(resource.getProperties()).thenReturn(properties(label));
        readable.computeIfAbsent(root, ignored -> new ArrayList<>()).add(resource);
        when(content.getResource(id)).thenReturn(resource);
        return resource;
    }

    private void folder(String id, String label) throws Exception {
        ContentCollection collection = mock(ContentCollection.class);
        when(collection.getId()).thenReturn(id);
        when(collection.isCollection()).thenReturn(true);
        when(collection.getProperties()).thenReturn(properties(label));
        when(content.getCollection(id)).thenReturn(collection);
    }

    private ResourceProperties properties(String name) {
        BaseResourceProperties properties = new BaseResourceProperties();
        properties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
        return properties;
    }
}
