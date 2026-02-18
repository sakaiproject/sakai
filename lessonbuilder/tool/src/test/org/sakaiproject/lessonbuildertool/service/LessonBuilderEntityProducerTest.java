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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageImpl;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageItemImpl;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;

public class LessonBuilderEntityProducerTest {

    private LessonBuilderEntityProducer producer;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private SimplePageToolDao dao;

    @Mock
    private Reference ref;

    @Before
    public void setUp() {
        producer = new LessonBuilderEntityProducer();
        producer.setSimplePageToolDao(dao);
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

    /**
     * Test that findReferencedPagesByItems correctly identifies parent-child relationships
     */
    @Test
    public void testFindReferencedPagesByItems() {
        String siteId = "test-site-1";

        // Parent page (id=100) with two subpage items
        SimplePage parentPage = new SimplePageImpl();
        parentPage.setPageId(100L);
        parentPage.setSiteId(siteId);
        parentPage.setTitle("Parent Page");

        // Subpage 1 (id=101)
        SimplePage subpage1 = new SimplePageImpl();
        subpage1.setPageId(101L);
        subpage1.setSiteId(siteId);
        subpage1.setTitle("Subpage 1");

        // Subpage 2 (id=102)
        SimplePage subpage2 = new SimplePageImpl();
        subpage2.setPageId(102L);
        subpage2.setSiteId(siteId);
        subpage2.setTitle("Subpage 2");

        // Create items that reference the subpages
        SimplePageItem item1 = new SimplePageItemImpl();
        item1.setId(1L);
        item1.setPageId(100L);
        item1.setType(SimplePageItem.PAGE);
        item1.setSakaiId("101"); // References subpage1

        SimplePageItem item2 = new SimplePageItemImpl();
        item2.setId(2L);
        item2.setPageId(100L);
        item2.setType(SimplePageItem.PAGE);
        item2.setSakaiId("102"); // References subpage2

        // Also add a non-PAGE item (should be ignored)
        SimplePageItem textItem = new SimplePageItemImpl();
        textItem.setId(3L);
        textItem.setPageId(100L);
        textItem.setType(SimplePageItem.TEXT);
        textItem.setHtml("Some text");

        List<SimplePageItem> allItems = new ArrayList<>();
        allItems.add(item1);
        allItems.add(item2);
        allItems.add(textItem);

        List<SimplePage> allPages = new ArrayList<>();
        allPages.add(parentPage);
        allPages.add(subpage1);
        allPages.add(subpage2);

        // Mock dao responses
        when(dao.getSitePages(siteId)).thenReturn(allPages);
        when(dao.findItemsOnPage(100L)).thenReturn(allItems);
        when(dao.getPage(101L)).thenReturn(subpage1);
        when(dao.getPage(102L)).thenReturn(subpage2);

        Map<Long, List<Long>> result = producer.findReferencedPagesByItems(siteId);

        // Verify results
        assertNotNull("Result should not be null", result);
        assertTrue("Result should contain parent page", result.containsKey(100L));

        List<Long> referencedPages = result.get(100L);
        assertNotNull("Referenced pages list should not be null", referencedPages);
        assertEquals("Should have 2 referenced pages", 2, referencedPages.size());
        assertTrue("Should reference subpage 101", referencedPages.contains(101L));
        assertTrue("Should reference subpage 102", referencedPages.contains(102L));
    }

    /**
     * Test that findReferencedPagesByItems SKIPS orphaned parent pages
     */
    @Test
    public void testFindReferencedPagesByItemsSkipsOrphans() {
        String siteId = "test-site-2";

        // Parent page that is orphaned
        SimplePage orphanParent = new SimplePageImpl();
        orphanParent.setPageId(200L);
        orphanParent.setSiteId(siteId);
        orphanParent.setToolId("0"); // ORPHANED
        orphanParent.setParent(0L);
        orphanParent.setTopParent(0L);

        SimplePage subpage = new SimplePageImpl();
        subpage.setPageId(201L);
        subpage.setSiteId(siteId);

        SimplePageItem item = new SimplePageItemImpl();
        item.setPageId(200L);
        item.setType(SimplePageItem.PAGE);
        item.setSakaiId("201");

        List<SimplePageItem> items = new ArrayList<>();
        items.add(item);

        List<SimplePage> pages = new ArrayList<>();
        pages.add(orphanParent);
        pages.add(subpage);

        when(dao.getSitePages(siteId)).thenReturn(pages);
        when(dao.findItemsOnPage(200L)).thenReturn(items);
        when(dao.getPage(201L)).thenReturn(subpage);

        // The method should skip orphaned parents
        Map<Long, List<Long>> result = producer.findReferencedPagesByItems(siteId);

        // Verify the orphan parent is not included in the result
        assertNotNull("Result should not be null", result);
        assertFalse("Orphan parent page 200L should not be in result", result.containsKey(200L));
    }

    /**
     * Verify hierarchy calculation logic
     *
     * Tests that parent/topparent relationships are calculated correctly
     * from subpage references, even when original values are 0/null.
     */
    @Test
    public void testHierarchyCalculationFromReferences() {
        // Setup: Page 100 has subpages 101, 102
        //        Page 101 has subpage 103
        Map<Long, List<Long>> subpageRefs = new HashMap<>();
        subpageRefs.put(100L, List.of(101L, 102L));
        subpageRefs.put(101L, List.of(103L));

        // Simulate pageMap (all pages are being imported)
        Map<Long, Long> pageMap = new HashMap<>();
        pageMap.put(100L, 100L);
        pageMap.put(101L, 101L);
        pageMap.put(102L, 102L);
        pageMap.put(103L, 103L);

        Map<Long, Long> calculatedParentMap = new HashMap<>();
        Map<Long, Long> calculatedTopParentMap = new HashMap<>();

        producer.buildParentMapFromReferences(subpageRefs, pageMap, calculatedParentMap);
        producer.calculateTopParentMap(calculatedParentMap, calculatedTopParentMap);

        // Verify results
        assertEquals("Page 101 parent should be 100", Long.valueOf(100L), calculatedParentMap.get(101L));
        assertEquals("Page 102 parent should be 100", Long.valueOf(100L), calculatedParentMap.get(102L));
        assertEquals("Page 103 parent should be 101", Long.valueOf(101L), calculatedParentMap.get(103L));

        assertEquals("Page 101 topparent should be 100", Long.valueOf(100L), calculatedTopParentMap.get(101L));
        assertEquals("Page 102 topparent should be 100", Long.valueOf(100L), calculatedTopParentMap.get(102L));
        assertEquals("Page 103 topparent should be 100", Long.valueOf(100L), calculatedTopParentMap.get(103L));
    }

    /**
     * Selective import should not break subpage hierarchy
     *
     * When pageMap only contains SOME pages (selective import), the hierarchy
     * calculation should only process pages that are in the pageMap.
     */
    @Test
    public void testHierarchyCalculationWithSelectiveImport() {
        Map<Long, Long> pageMap = new HashMap<>(); // old -> new mapping
        Map<Long, List<Long>> subpageRefs = new HashMap<>();

        // Source site has: Page 100, 101, 102, 200
        // User imports ONLY pages 100 and 101 (selective)
        pageMap.put(100L, 5000L); // old 100 -> new 5000
        pageMap.put(101L, 5001L); // old 101 -> new 5001
        // Pages 102 and 200 NOT imported

        // References: 100 -> [101, 102], 200 -> [103]
        subpageRefs.put(100L, List.of(101L, 102L));
        subpageRefs.put(200L, List.of(103L));

        Map<Long, Long> calculatedParentMap = new HashMap<>();

        producer.buildParentMapFromReferences(subpageRefs, pageMap, calculatedParentMap);

        // Verify: only page 101 should have parent (102 not imported)
        assertEquals("Page 101 should have parent 100", Long.valueOf(100L), calculatedParentMap.get(101L));
        assertFalse("Page 102 should not be in map (not imported)", calculatedParentMap.containsKey(102L));
        assertFalse("Page 103 should not be in map (parent not imported)", calculatedParentMap.containsKey(103L));
    }

}
