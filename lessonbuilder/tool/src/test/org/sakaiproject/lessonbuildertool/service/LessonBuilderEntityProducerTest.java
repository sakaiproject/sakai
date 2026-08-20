/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.lessonbuildertool.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageImpl;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageItemImpl;
import org.sakaiproject.lessonbuildertool.api.LessonBuilderConstants;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.util.MergeConfig;
import org.sakaiproject.util.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class LessonBuilderEntityProducerTest {

    private LessonBuilderEntityProducer producer;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private SimplePageToolDao dao;

    @Mock
    private Reference ref;

    @Mock
    private SiteService siteService;

    @Mock
    private RemovedPageService removedPageService;

    @Before
    public void setUp() {
        producer = new LessonBuilderEntityProducer();
        producer.setSimplePageToolDao(dao);
        producer.setSiteService(siteService);
        producer.setRemovedPageService(removedPageService);
    }

    @Test
    public void testParseEntityReferenceNotOurs() {
        assertFalse(producer.parseEntityReference("/else", ref));
        assertFalse(producer.parseEntityReference("/", ref));
        assertFalse(producer.parseEntityReference("", ref));
    }

    @Test
    public void testParseEntityReferenceNoItem() {
        assertFalse(producer.parseEntityReference("/lessonbuilder/item", ref));
    }

    @Test
    public void testParseEntityReferenceNoSite() {
        assertFalse(producer.parseEntityReference("/lessonbuilder/site", ref));
    }

    @Test
    public void testParseEntityReferenceNoPage() {
        assertFalse(producer.parseEntityReference("/lessonbuilder/page", ref));
    }

    @Test
    public void testParseEntityReferencePageNotInt() {
        assertFalse(producer.parseEntityReference("/lessonbuilder/page/notNumber", ref));
    }

    @Test
    public void testParseEntityReferencePageMissingId() {
        assertFalse(producer.parseEntityReference("/lessonbuilder/page/10", ref));
    }

    @Test
    public void testParseEntityReferencePageWithId() {
        SimplePage page = Mockito.mock(SimplePage.class);
        Mockito.when(page.getSiteId()).thenReturn("siteId");
        Mockito.when(dao.getPage(10)).thenReturn(page);
        assertTrue(producer.parseEntityReference("/lessonbuilder/page/10", ref));
        Mockito.verify(ref).set("sakai:lessonbuilder", "page", "/page/10", null, "siteId");
    }

    @Test
    public void testParseEntityReferenceItemWithId() {
        SimplePageItem item = Mockito.mock(SimplePageItem.class);
        Mockito.when(dao.findItem(10)).thenReturn(item);
        Mockito.when(item.getPageId()).thenReturn(11L);
        SimplePage page = Mockito.mock(SimplePage.class);
        Mockito.when(page.getSiteId()).thenReturn("siteId");
        Mockito.when(dao.getPage(11)).thenReturn(page);
        assertTrue(producer.parseEntityReference("/lessonbuilder/item/10", ref));
        Mockito.verify(ref).set("sakai:lessonbuilder", "item", "/item/10", null, "siteId");
    }

    @Test
    public void testParseEntityReferenceSite() {
        assertTrue(producer.parseEntityReference("/lessonbuilder/site/siteId", ref));
        Mockito.verify(ref).set("sakai:lessonbuilder", "site", "/site/siteId", null, "/site/siteId");
    }

    @Test
    public void deleteOrphanPagesReportsServiceResult() {
        when(removedPageService.deleteAllRemovedPages("siteId"))
                .thenReturn(new RemovedPageService.DeleteResult(RemovedPageService.OperationStatus.INVALID, 0));

        assertEquals("status=INVALID, deletedCount=0", producer.deleteOrphanPages("siteId"));
    }

    /**
     * SAK-52251: exercise the archive-only merge workflow with no live source site. A
     * pseudo-orphan with no archived parent is recovered from its regular PAGE reference,
     * an archived parent remains authoritative when another page links to it, and neither
     * next-page nor regular links to a placed top-level page alter either placement.
     */
    @Test
    public void testMergeRestoresPseudoOrphanWithoutReparentingLinkedPages() throws Exception {
        String sourceSiteId = "source-site";
        String destinationSiteId = "destination-site";

        long oldPageA = 100L;
        long oldPageB = 200L;
        long oldPseudoOrphan = 300L;
        long oldSharedPage = 400L;
        long newPageA = 1100L;
        long newPageB = 1200L;
        long newPseudoOrphan = 1300L;
        long newSharedPage = 1400L;

        Map<Long, List<SimplePageItem>> itemsByPage = new HashMap<>();
        Map<Long, SimplePage> pagesById = new HashMap<>();

        when(dao.findItemsOnPage(Mockito.anyLong())).thenAnswer(invocation ->
                itemsByPage.getOrDefault(invocation.getArgument(0), List.of()));
        when(dao.getPage(Mockito.anyLong())).thenAnswer(invocation -> pagesById.get(invocation.getArgument(0)));

        Map<String, Long> importedPageIds = Map.of(
                "Lesson A", newPageA,
                "Lesson B", newPageB,
                "Pseudo orphan", newPseudoOrphan,
                "Shared page", newSharedPage);
        when(dao.makePage(Mockito.eq("0"), Mockito.eq(destinationSiteId), Mockito.anyString(), Mockito.isNull(), Mockito.isNull()))
                .thenAnswer(invocation -> {
                    String title = invocation.getArgument(2);
                    SimplePage page = newPage(importedPageIds.get(title), destinationSiteId, title, "0", null, null);
                    pagesById.put(page.getPageId(), page);
                    return page;
                });

        AtomicLong importedItemId = new AtomicLong(1000L);
        when(dao.makeItem(Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenAnswer(invocation -> {
                    long pageId = invocation.getArgument(0);
                    int sequence = invocation.getArgument(1);
                    int type = invocation.getArgument(2);
                    String sakaiId = invocation.getArgument(3);
                    String name = invocation.getArgument(4);
                    SimplePageItem item = new SimplePageItemImpl();
                    item.setId(importedItemId.incrementAndGet());
                    item.setPageId(pageId);
                    item.setSequence(sequence);
                    item.setType(type);
                    item.setSakaiId(sakaiId);
                    item.setName(name);
                    itemsByPage.computeIfAbsent(pageId, key -> new ArrayList<>()).add(item);
                    return item;
                });
        when(dao.quickSaveItem(Mockito.any())).thenReturn(true);
        when(dao.quickUpdate(Mockito.any())).thenReturn(true);

        Site destinationSite = Mockito.mock(Site.class);
        when(destinationSite.getId()).thenReturn(destinationSiteId);
        when(destinationSite.getTools(Mockito.any(String[].class))).thenReturn(List.of());
        when(siteService.getSite(destinationSiteId)).thenReturn(destinationSite);
        when(siteService.getOptionalSite(sourceSiteId)).thenReturn(Optional.empty());

        SitePage navigationPageA = Mockito.mock(SitePage.class);
        SitePage navigationPageB = Mockito.mock(SitePage.class);
        ToolConfiguration placementA = Mockito.mock(ToolConfiguration.class);
        ToolConfiguration placementB = Mockito.mock(ToolConfiguration.class);
        when(destinationSite.addPage()).thenReturn(navigationPageA, navigationPageB);
        when(navigationPageA.addTool(LessonBuilderConstants.TOOL_ID)).thenReturn(placementA);
        when(navigationPageB.addTool(LessonBuilderConstants.TOOL_ID)).thenReturn(placementB);
        when(navigationPageA.getId()).thenReturn("navigation-a");
        when(navigationPageB.getId()).thenReturn("navigation-b");
        when(placementA.getPageId()).thenReturn("destination-placement-a");
        when(placementB.getPageId()).thenReturn("destination-placement-b");

        Document document = Xml.createDocument();
        Element root = document.createElement("service");
        document.appendChild(root);
        Element lessonBuilder = document.createElement("lessonbuilder");
        root.appendChild(lessonBuilder);

        Element pageA = pageElement(document, oldPageA, sourceSiteId, "Lesson A", null);
        addPageItem(document, pageA, 1L, oldPageA, oldPseudoOrphan, false, "Pseudo orphan");
        addPageItem(document, pageA, 2L, oldPageA, oldPageB, true, "Lesson B");
        lessonBuilder.appendChild(pageA);

        Element pageB = pageElement(document, oldPageB, sourceSiteId, "Lesson B", null);
        addPageItem(document, pageB, 3L, oldPageB, oldSharedPage, false, "Shared page");
        addPageItem(document, pageB, 4L, oldPageB, oldPageA, false, "Lesson A");
        lessonBuilder.appendChild(pageB);
        lessonBuilder.appendChild(pageElement(document, oldPseudoOrphan, sourceSiteId, "Pseudo orphan", null));
        lessonBuilder.appendChild(pageElement(document, oldSharedPage, sourceSiteId, "Shared page", oldPageA));
        lessonBuilder.appendChild(placementElement(document, oldPageA, "Lesson A"));
        lessonBuilder.appendChild(placementElement(document, oldPageB, "Lesson B"));

        String result = producer.mergeInternal(destinationSiteId, root, "", sourceSiteId,
                new MergeConfig(), new HashMap<>(), false);

        assertFalse(result, result.contains("failed"));

        SimplePage importedPageA = pagesById.get(newPageA);
        SimplePage importedPageB = pagesById.get(newPageB);
        SimplePage importedPseudoOrphan = pagesById.get(newPseudoOrphan);
        SimplePage importedSharedPage = pagesById.get(newSharedPage);

        assertNull(importedPageA.getParent());
        assertNull(importedPageA.getTopParent());
        assertEquals("destination-placement-a", importedPageA.getToolId());
        assertNull(importedPageB.getParent());
        assertNull(importedPageB.getTopParent());
        assertEquals("destination-placement-b", importedPageB.getToolId());

        assertEquals(Long.valueOf(newPageA), importedPseudoOrphan.getParent());
        assertEquals(Long.valueOf(newPageA), importedPseudoOrphan.getTopParent());
        assertEquals("destination-placement-a", importedPseudoOrphan.getToolId());

        assertEquals(Long.valueOf(newPageA), importedSharedPage.getParent());
        assertEquals(Long.valueOf(newPageA), importedSharedPage.getTopParent());
        assertEquals("destination-placement-a", importedSharedPage.getToolId());

        verify(dao, never()).getPage(oldPseudoOrphan);
        verify(dao, never()).getPage(oldSharedPage);
    }

    private static Element pageElement(Document document, long pageId, String siteId, String title, Long parentId) {
        Element page = document.createElement("page");
        page.setAttribute("pageid", Long.toString(pageId));
        page.setAttribute("siteid", siteId);
        page.setAttribute("title", title);
        if (parentId != null) {
            page.setAttribute("parent", parentId.toString());
        }
        return page;
    }

    private static void addPageItem(Document document, Element page, long itemId, long pageId,
            long referencedPageId, boolean nextPage, String name) {
        Element item = document.createElement("item");
        item.setAttribute("id", Long.toString(itemId));
        item.setAttribute("pageId", Long.toString(pageId));
        item.setAttribute("sequence", Long.toString(itemId));
        item.setAttribute("type", Integer.toString(SimplePageItem.PAGE));
        item.setAttribute("sakaiid", Long.toString(referencedPageId));
        item.setAttribute("name", name);
        item.setAttribute("nextpage", Boolean.toString(nextPage));
        item.setAttribute("gradebookPoints", "null");
        item.setAttribute("altPoints", "null");
        page.appendChild(item);
    }

    private static Element placementElement(Document document, long pageId, String name) {
        Element placement = document.createElement("lessonbuilder");
        placement.setAttribute("toolid", Long.toString(pageId));
        placement.setAttribute("pageId", Long.toString(pageId));
        placement.setAttribute("name", name);
        return placement;
    }

    private static SimplePage newPage(long pageId, String siteId, String title, String toolId, Long parent, Long topParent) {
        SimplePage page = new SimplePageImpl();
        page.setPageId(pageId);
        page.setSiteId(siteId);
        page.setTitle(title);
        page.setToolId(toolId);
        page.setParent(parent);
        page.setTopParent(topParent);
        return page;
    }
}
