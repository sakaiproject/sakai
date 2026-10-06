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
package org.sakaiproject.sitestats.tool.mvc;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
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
import org.sakaiproject.sitestats.tool.mvc.SiteStatsResourceSelectionService.ResourceSearchPage;
import org.sakaiproject.sitestats.tool.mvc.SiteStatsResourceSelectionService.SelectedResourceOption;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.BaseResourceProperties;
import org.sakaiproject.util.api.LocaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SiteStatsResourceSelectionTestConfiguration.class)
@WebAppConfiguration("src/webapp")
@Transactional
public class SiteStatsResourceSelectionServiceTest {

    private static final String SITE = "site1";
    private static final String ROOT = "/group/" + SITE + "/";
    private static final String DROPBOX = "/group-user/" + SITE + "/";
    private static final String ATTACHMENTS = "/attachment/" + SITE + "/";

    @Autowired private SiteStatsResourceSelectionService service;
    @Autowired private SiteStatsToolAuthorizationService authorizationService;
    @Autowired private ContentHostingService content;
    @Autowired private SecurityService security;
    @Autowired private SiteService sites;
    @Autowired private ToolManager tools;
    @Autowired private SessionManager sessions;
    @Autowired private LocaleService locales;
    @Autowired private WebApplicationContext context;

    private final Map<String, List<ContentResource>> readable = new HashMap<>();
    private MockMvc mvc;

    @Before
    public void setUp() throws Exception {
        reset(content, security, sites, tools, sessions, locales);
        readable.clear();
        Placement placement = mock(Placement.class);
        when(placement.getContext()).thenReturn(SITE);
        when(tools.getCurrentPlacement()).thenReturn(placement);
        Tool tool = mock(Tool.class);
        when(tool.getId()).thenReturn("sakai.sitestats");
        when(tools.getCurrentTool()).thenReturn(tool);
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
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void usesRealInternalAuthorizationAndSearchBeans() {
        assertFalse(mockingDetails(service).isMock());
        assertFalse(mockingDetails(authorizationService).isMock());
        assertEquals(SITE, authorizationService.reportSite(SITE));
    }

    @Test
    public void searchesNestedNamesAcrossAllThreeRootsWithoutCollectionsOrBodies() throws Exception {
        folder(ROOT + "week-one/", "Week One");
        folder(ROOT + "week-one/readings/", "Readings");
        ContentResource nested = resource(ROOT, ROOT + "week-one/readings/file.txt", "Reading Notes", ResourceType.TYPE_TEXT);
        resource(DROPBOX, DROPBOX + "upload.txt", "Reading Upload", ResourceType.TYPE_UPLOAD);
        resource(ATTACHMENTS, ATTACHMENTS + "file.url", "Reading Link", ResourceType.TYPE_URL);
        ResourceSearchPage page = service.search(SITE, "reading", 0);
        assertEquals(3, page.getItems().size());
        assertEquals("Resources / Week One / Readings", page.getItems().get(1).getLocation());
        assertTrue(page.getItems().stream().allMatch(item -> !item.getId().startsWith("/content")));
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
        assertEquals(1, service.search(SITE, "RÉSUMÉ", 0).getItems().size());
        assertEquals(1, service.search(SITE, "[50%]_星", 0).getItems().size());
        assertEquals(0, service.search(SITE, ".*", 0).getItems().size());
        assertEquals(0, service.search(SITE, "document body", 0).getItems().size());
    }

    @Test
    public void keepsDuplicateNamesDistinctWithReadableLocationsAndStableIdTieBreak() throws Exception {
        folder(ROOT + "a/", "First Week");
        folder(ROOT + "b/", "Second Week");
        resource(ROOT, ROOT + "b/file.txt", "Reading.pdf", ResourceType.TYPE_UPLOAD);
        resource(ROOT, ROOT + "a/file.txt", "Reading.pdf", ResourceType.TYPE_UPLOAD);
        ResourceSearchPage page = service.search(SITE, "reading", 0);
        assertEquals(ROOT + "a/file.txt", page.getItems().get(0).getId());
        assertEquals("Resources / First Week", page.getItems().get(0).getLocation());
        assertEquals("Resources / Second Week", page.getItems().get(1).getLocation());
    }

    @Test
    public void capsPagesAndFindsTheLastResourceInTwoThousand() throws Exception {
        for (int i = 1999; i >= 0; i--) {
            String name = String.format(Locale.ROOT, "File %04d", i);
            resource(ROOT, ROOT + i + ".txt", name, ResourceType.TYPE_UPLOAD);
        }
        ResourceSearchPage first = service.search(SITE, "file", 0);
        assertEquals(20, first.getItems().size());
        assertEquals("File 0000", first.getItems().get(0).getLabel());
        assertTrue(first.isHasNext());
        ResourceSearchPage last = service.search(SITE, "file", 99);
        assertEquals(20, last.getItems().size());
        assertEquals("File 1999", last.getItems().get(19).getLabel());
        assertFalse(last.isHasNext());
        assertTrue(service.search(SITE, "file", Integer.MAX_VALUE).getItems().isEmpty());
        assertEquals(ROOT + "1999.txt", service.search(SITE, "1999", 0).getItems().get(0).getId());
    }

    @Test
    public void authorizesEvenShortQueriesAndEnumeratesNothingWhenDenied() throws Exception {
        when(security.unlock(StatsAuthz.PERMISSION_SITESTATS_VIEW, "/site/" + SITE)).thenReturn(false);
        assertThrows(SecurityException.class, () -> service.search(SITE, "", 0));
        assertThrows(SecurityException.class, () -> service.search(SITE, "a", 0));
        assertThrows(SecurityException.class, () -> service.selected(SITE, List.of()));
        verifyNoInteractions(content);
        mvc.perform(get("/reports/resources/search").param("siteId", SITE).param("q", "reading"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(content);
    }

    @Test
    public void refusesCrossSiteRequestsUntilActualAdminPermissionIsPresent() throws Exception {
        assertThrows(SecurityException.class, () -> service.search("site2", "reading", 0));
        verifyNoInteractions(content);
        allow(SITE, StatsAuthz.PERMISSION_SITESTATS_ADMIN_VIEW);
        allow("site2", StatsAuthz.PERMISSION_SITESTATS_VIEW);
        allow("site2", StatsAuthz.PERMISSION_SITESTATS_ALL);
        resource("/group/site2/", "/group/site2/file.txt", "Reading", ResourceType.TYPE_UPLOAD);
        assertEquals("/group/site2/file.txt", service.search("site2", "reading", 0).getItems().get(0).getId());
        verify(content, never()).getAllResources(ROOT);
    }

    @Test
    public void ignoresOutOfSiteCandidatesAndReflectsKernelReadChanges() throws Exception {
        ContentResource file = resource(ROOT, ROOT + "file.txt", "Reading", ResourceType.TYPE_UPLOAD);
        resource(ROOT, "/group/site2/leak.txt", "Reading Secret", ResourceType.TYPE_UPLOAD);
        assertEquals(1, service.search(SITE, "reading", 0).getItems().size());
        readable.get(ROOT).remove(file);
        assertTrue(service.search(SITE, "reading", 0).getItems().isEmpty());
    }

    @Test
    public void resolvesSavedFilesCollectionsAndGenericRootsAndKeepsUnavailableIdsNeutral() throws Exception {
        resource(ROOT, ROOT + "file.txt", "Reading", ResourceType.TYPE_UPLOAD);
        folder(ROOT + "folder/", "Previous folder selection");
        when(content.getResource(ROOT + "deleted.txt")).thenThrow(new IdUnusedException(ROOT + "deleted.txt"));
        when(content.getResource(ROOT + "private.txt")).thenThrow(new PermissionException("instructor", "content.read", ROOT + "private.txt"));
        List<String> ids = List.of(ROOT + "file.txt", ROOT + "folder/", "/group/", "/group-user/", "/attachment/",
                ROOT + "deleted.txt", ROOT + "private.txt", "/group/site2/leak.txt");
        List<SelectedResourceOption> selected = service.selected(SITE, ids);
        assertEquals(ids, selected.stream().map(SelectedResourceOption::getId).toList());
        assertEquals("Reading", selected.get(0).getLabel());
        assertTrue(selected.get(1).isLegacyCollection());
        for (SelectedResourceOption unavailable : selected.subList(5, 8)) {
            assertTrue(unavailable.isUnavailable());
            assertEquals("Unavailable resource", unavailable.getLabel());
            assertEquals("", unavailable.getLocation());
        }
        verify(content, never()).getResource("/group/site2/leak.txt");
        verify(content, never()).getAllResources(anyString());
    }

    @Test
    public void returnsCanonicalIdsAndBoundedPageMetadataThroughTheRealJsonRoute() throws Exception {
        for (int i = 0; i < 21; i++) {
            resource(ROOT, ROOT + i + ".txt", String.format(Locale.ROOT, "Reading %02d", i), ResourceType.TYPE_UPLOAD);
        }
        String json = mvc.perform(get("/reports/resources/search").param("siteId", SITE).param("q", "reading"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode result = MapperFactory.createDefaultJsonMapper().readTree(json);
        assertEquals(20, result.get("items").size());
        assertEquals(0, result.get("page").asInt());
        assertTrue(result.get("hasNext").asBoolean());
        assertEquals(ROOT + "0.txt", result.get("items").get(0).get("id").asText());
        assertEquals("Reading 00", result.get("items").get(0).get("label").asText());
        assertEquals("Resources", result.get("items").get(0).get("location").asText());
        assertEquals(3, result.get("items").get(0).size());
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
        List<SelectedResourceOption> options = service.selected(SITE, ids);
        assertEquals(ids, options.stream().map(SelectedResourceOption::getId).toList());
        for (SelectedResourceOption option : options) {
            assertEquals("Resources / Unavailable location", option.getLocation());
            assertFalse(option.isUnavailable());
            assertFalse(option.getLocation().contains("private-folder"));
            assertFalse(option.getLocation().contains("stale-folder"));
        }
    }

    @Test
    public void usesTheSitesEffectiveLocaleAndReturnsBadRequestsForInvalidParameters() throws Exception {
        when(locales.getLocaleForSiteAndUser(SITE, "instructor")).thenReturn(Locale.FRANCE);
        resource(ROOT, ROOT + "file.txt", "Résumé", ResourceType.TYPE_UPLOAD);
        service.search(SITE, "résumé", 0);
        verify(locales).getLocaleForSiteAndUser(SITE, "instructor");
        mvc.perform(get("/reports/resources/search").param("siteId", SITE).param("q", "reading").param("page", "-1"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/reports/resources/search").param("siteId", SITE).param("q", "a".repeat(257)))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/reports/resources/search").param("siteId", SITE).param("q", "reading").param("page", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void rendersTheEditorsEffectiveLocaleTagsAndEscapesInitialSelectionJson() throws Exception {
        String hostile = "</sakai-sitestats-resource-search><script>alert(1)</script>";
        resource(ROOT, ROOT + "file.txt", hostile, ResourceType.TYPE_UPLOAD);
        for (Locale locale : List.of(Locale.US, Locale.FRANCE)) {
            SiteStatsReportForm form = SiteStatsReportForm.create(Clock.systemUTC());
            form.setWhat("what-resources");
            form.setWhatResourceIds(ROOT + "file.txt");
            Map<String, Object> model = new HashMap<>();
            model.put("locale", locale);
            model.put("siteId", SITE);
            model.put("reportForm", form);
            model.put("editorOptions", new SiteStatsToolService.ReportEditorOptions(List.of(), List.of(), List.of(),
                    List.of(), List.of(), List.of("what-resources"), new ReportEditorRulesView(), false, false, true, false));
            model.put("toolMenuAvailable", false);
            model.put("adminTool", false);
            model.put("userActivityAvailable", false);
            model.put("cdnQuery", "");
            model.put("selectedResourcesJson", MapperFactory.createDefaultJsonMapper()
                    .writeValueAsString(service.selected(SITE, List.of(ROOT + "file.txt"))));
            MockHttpServletRequest request = new MockHttpServletRequest(context.getServletContext());
            MockHttpServletResponse response = new MockHttpServletResponse();
            View view = context.getBean("viewResolver", ViewResolver.class).resolveViewName("reports/edit", locale);
            assertNotNull(view);
            view.render(model, request, response);
            String rendered = response.getContentAsString();
            assertTrue(rendered.contains("lang=\"" + locale.toLanguageTag() + "\""));
            assertTrue(rendered.contains("&lt;script&gt;alert(1)&lt;/script&gt;"));
            assertFalse(rendered.contains("<script>alert(1)</script>"));
            assertTrue(rendered.contains("initial-selection=\"[{&quot;"));
        }
    }

    @Test
    public void leavesAuthorizedBlankQueriesAndEmptySelectionsWithoutInventoryReads() {
        assertTrue(service.search(SITE, "", 0).getItems().isEmpty());
        assertTrue(service.search(SITE, "a", 0).getItems().isEmpty());
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
