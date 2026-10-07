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
import org.sakaiproject.sitestats.api.view.SiteStatsResourceSearchService.ResourceSearchResult;
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
    public void searchesNestedNamesAcrossAllThreeRootsWithoutCollectionsOrBodies() throws Exception {
        folder(ROOT + "week-one/", "Week One");
        folder(ROOT + "week-one/readings/", "Readings");
        ContentResource nested = resource(ROOT, ROOT + "week-one/readings/file.txt", "Reading Notes", ResourceType.TYPE_TEXT);
        resource(DROPBOX, DROPBOX + "upload.txt", "Reading Upload", ResourceType.TYPE_UPLOAD);
        resource(ATTACHMENTS, ATTACHMENTS + "file.url", "Reading Link", ResourceType.TYPE_URL);
        ResourceSearchResult result = service.search(SITE, "reading");
        assertEquals(3, result.items().size());
        assertEquals("Resources / Week One / Readings", result.items().get(1).location());
        assertTrue(result.items().stream().allMatch(item -> !item.id().startsWith("/content")));
        verify(nested, never()).getContent();
        verify(nested, never()).streamContent();
        verify(content).getAllResources(ROOT);
        verify(content).getAllResources(DROPBOX);
        verify(content).getAllResources(ATTACHMENTS);
    }

    @Test
    public void matchesLiteralUnicodeAndPunctuationRatherThanBodyOrWildcards() throws Exception {
        resource(ROOT, ROOT + "unicode.txt", "Résumé [50%]_星.txt", ResourceType.TYPE_UPLOAD);
        resource(ROOT, ROOT + "other.txt", "Unrelated document", ResourceType.TYPE_HTML);
        assertEquals(1, service.search(SITE, "RÉSUMÉ").items().size());
        folder(ROOT + "résumé/", "ÉTÉ [50%]_星");
        resource(ROOT, ROOT + "résumé/reading.txt", "Reading.pdf", ResourceType.TYPE_UPLOAD);
        assertEquals(1, service.search(SITE, "été\u2003[50%]_星 reading").items().size());
        assertEquals(2, service.search(SITE, "[50%]_星").items().size());
        assertEquals(0, service.search(SITE, ".*").items().size());
        assertEquals(0, service.search(SITE, "document body").items().size());
    }

    @Test
    public void keepsDuplicateNamesDistinctWithReadableLocationsAndStableIdTieBreak() throws Exception {
        folder(ROOT + "a/", "First Week");
        folder(ROOT + "b/", "Second Week");
        resource(ROOT, ROOT + "b/file.txt", "Reading.pdf", ResourceType.TYPE_UPLOAD);
        resource(ROOT, ROOT + "a/file.txt", "Reading.pdf", ResourceType.TYPE_UPLOAD);
        ResourceSearchResult result = service.search(SITE, "reading");
        assertEquals(ROOT + "a/file.txt", result.items().get(0).id());
        assertEquals("Resources / First Week", result.items().get(0).location());
        assertEquals("Resources / Second Week", result.items().get(1).location());
    }

    @Test
    public void capsMatchesAndFindsTheLastResourceInTwoThousandByRefinement() throws Exception {
        for (int i = 1999; i >= 0; i--) {
            String name = String.format(Locale.ROOT, "File %04d", i);
            resource(ROOT, ROOT + i + ".txt", name, ResourceType.TYPE_UPLOAD);
        }
        ResourceSearchResult first = service.search(SITE, "file");
        assertEquals(20, first.items().size());
        assertEquals("File 0000", first.items().get(0).label());
        assertTrue(first.truncated());
        ResourceSearchResult refined = service.search(SITE, "file 1999");
        assertEquals(1, refined.items().size());
        assertEquals(ROOT + "1999.txt", refined.items().get(0).id());
        assertFalse(refined.truncated());
    }

    @Test
    public void distinguishesDuplicateNamesByAllLiteralNameAndReadableLocationTerms() throws Exception {
        for (int i = 0; i < 25; i++) {
            String folderId = ROOT + "internal-" + i + "/";
            folder(folderId, "Week " + i);
            resource(ROOT, folderId + "file.pdf", "Reading.pdf", ResourceType.TYPE_UPLOAD);
        }
        assertTrue(service.search(SITE, "reading").truncated());
        verify(content, times(20)).getCollection(anyString());
        clearInvocations(content);
        ResourceSearchResult refined = service.search(SITE, "  WEEK\t5  reading  ");
        assertEquals(2, refined.items().size());
        assertFalse(refined.truncated());
        assertTrue(refined.items().stream().anyMatch(item -> "Resources / Week 5".equals(item.location())));
        verify(content, times(25)).getCollection(anyString());
        assertTrue(service.search(SITE, "week 5 missing").items().isEmpty());
        assertTrue(service.search(SITE, "internal-5").items().isEmpty());
        ResourceSearchResult locationOnly = service.search(SITE, "week 24");
        assertEquals(1, locationOnly.items().size());
        assertEquals(ROOT + "internal-24/file.pdf", locationOnly.items().get(0).id());
        assertEquals("Resources / Week 24", locationOnly.items().get(0).location());
    }

    @Test
    public void refinesTheBrowserFixtureWithTermsAcrossNamesAndLocations() throws Exception {
        String firstWeek = ROOT + "Week 1/";
        String fifthWeek = ROOT + "Week 5/";
        folder(firstWeek, "Week 1");
        folder(fifthWeek, "Week 5");
        resource(ROOT, firstWeek + "Reading.txt", "Reading.txt", ResourceType.TYPE_UPLOAD);
        resource(ROOT, fifthWeek + "Reading.txt", "Reading.txt", ResourceType.TYPE_UPLOAD);
        for (int index = 1; index <= 20; index++) {
            String name = "Reading " + index + ".txt";
            resource(ROOT, firstWeek + name, name, ResourceType.TYPE_UPLOAD);
        }
        ResourceSearchResult broad = service.search(SITE, "reading");
        assertEquals(20, broad.items().size());
        assertTrue(broad.truncated());
        ResourceSearchResult refined = service.search(SITE, "Week 5 Reading");
        assertEquals(List.of(fifthWeek + "Reading.txt", firstWeek + "Reading 15.txt", firstWeek + "Reading 5.txt"),
                refined.items().stream().map(ResourceOption::id).toList());
        assertFalse(refined.truncated());
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
        ResourceSearchResult refined = service.search(SITE, "week 99 reading");
        assertEquals(20, refined.items().size());
        assertFalse(refined.truncated());
        assertTrue(refined.items().stream().allMatch(item -> "Resources / Week 99".equals(item.location())));
        verify(content, times(100)).getCollection(anyString());
    }

    @Test
    public void exactlyTwentyMatchesAreCompleteAndPermissionDeniedLocationsCannotMatchHiddenNames() throws Exception {
        folder(ROOT + "private/", "Secret hidden week");
        when(content.getCollection(ROOT + "private/"))
                .thenThrow(new PermissionException("instructor", "content.read", ROOT + "private/"));
        for (int i = 0; i < 20; i++) {
            resource(ROOT, ROOT + "private/" + i + ".txt", "Readable Reading " + i, ResourceType.TYPE_UPLOAD);
        }
        ResourceSearchResult nameMatches = service.search(SITE, "reading");
        assertEquals(20, nameMatches.items().size());
        assertFalse(nameMatches.truncated());
        assertTrue(nameMatches.items().stream().allMatch(item -> "Resources / Unavailable location".equals(item.location())));
        verify(content).getCollection(ROOT + "private/");
        clearInvocations(content);
        assertTrue(service.search(SITE, "secret reading").items().isEmpty());
        verify(content).getCollection(ROOT + "private/");
    }

    @Test
    public void authorizesEvenShortQueriesAndEnumeratesNothingWhenDenied() throws Exception {
        when(security.unlock(StatsAuthz.PERMISSION_SITESTATS_VIEW, "/site/" + SITE)).thenReturn(false);
        assertThrows(SecurityException.class, () -> service.search(SITE, ""));
        assertThrows(SecurityException.class, () -> service.search(SITE, "a"));
        assertThrows(SecurityException.class, () -> service.selected(SITE, List.of()));
        verifyNoInteractions(content);
        verifyNoInteractions(content);
    }

    @Test
    public void refusesUnauthorizedSitesUntilTheirReportPermissionsArePresent() throws Exception {
        assertThrows(SecurityException.class, () -> service.search("site2", "reading"));
        verifyNoInteractions(content);
        allow("site2", StatsAuthz.PERMISSION_SITESTATS_VIEW);
        allow("site2", StatsAuthz.PERMISSION_SITESTATS_ALL);
        resource("/group/site2/", "/group/site2/file.txt", "Reading", ResourceType.TYPE_UPLOAD);
        assertEquals("/group/site2/file.txt", service.search("site2", "reading").items().get(0).id());
        verify(content, never()).getAllResources(ROOT);
    }

    @Test
    public void reflectsKernelReadChanges() throws Exception {
        ContentResource file = resource(ROOT, ROOT + "file.txt", "Reading", ResourceType.TYPE_UPLOAD);
        assertEquals(1, service.search(SITE, "reading").items().size());
        readable.get(ROOT).remove(file);
        assertTrue(service.search(SITE, "reading").items().isEmpty());
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
    public void serializesCanonicalIdsAndTruncationThroughThePublicResultContract() throws Exception {
        for (int i = 0; i < 21; i++) {
            resource(ROOT, ROOT + i + ".txt", String.format(Locale.ROOT, "Reading %02d", i), ResourceType.TYPE_UPLOAD);
        }
        String json = MapperFactory.createDefaultJsonMapper().writeValueAsString(service.search(SITE, "reading"));
        JsonNode result = MapperFactory.createDefaultJsonMapper().readTree(json);
        assertEquals(20, result.get("items").size());
        assertTrue(result.get("truncated").asBoolean());
        assertEquals(2, result.size());
        assertFalse(result.has("page"));
        assertFalse(result.has("hasNext"));
        assertEquals(ROOT + "0.txt", result.get("items").get(0).get("id").asText());
        assertEquals("Reading 00", result.get("items").get(0).get("label").asText());
        assertEquals("Resources", result.get("items").get(0).get("location").asText());
        assertEquals(4, result.get("items").get(0).size());
        assertFalse(result.get("items").get(0).get("legacyCollection").asBoolean());
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

    @Test
    public void usesTheSitesEffectiveLocaleAndRejectsOversizedQueries() throws Exception {
        when(locales.getLocaleForSiteAndUser(SITE, "instructor")).thenReturn(Locale.FRANCE);
        resource(ROOT, ROOT + "file.txt", "Résumé", ResourceType.TYPE_UPLOAD);
        service.search(SITE, "résumé");
        verify(locales).getLocaleForSiteAndUser(SITE, "instructor");
        assertThrows(SiteStatsResourceSearchService.InvalidQueryException.class,
                () -> service.search(SITE, "a".repeat(257)));
    }

    @Test
    public void leavesAuthorizedBlankQueriesAndEmptySelectionsWithoutInventoryReads() {
        assertTrue(service.search(SITE, "").items().isEmpty());
        assertTrue(service.search(SITE, "a").items().isEmpty());
        assertTrue(service.selected(SITE, List.of()).isEmpty());
        verifyNoInteractions(content);
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
