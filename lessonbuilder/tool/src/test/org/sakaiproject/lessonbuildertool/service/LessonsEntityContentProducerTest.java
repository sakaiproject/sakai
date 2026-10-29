/**
 * Copyright (c) 2026 The Apereo Foundation
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageImpl;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageItemImpl;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;

public class LessonsEntityContentProducerTest {

    private static final long PAGE_ID = 20L;
    private static final long SUBPAGE_ID = 30L;

    private SimplePageToolDao dao;
    private LessonsEntityContentProducer producer;

    @Before
    public void setUp() {
        dao = mock(SimplePageToolDao.class);
        producer = new LessonsEntityContentProducer();
        producer.setSimplePageToolDao(dao);

        SimplePage page = new SimplePageImpl("toolpage", "site", "Week 1", null, null);
        page.setPageId(PAGE_ID);
        when(dao.getPage(PAGE_ID)).thenReturn(page);
    }

    @Test
    public void textBoxTakesTheTitleOfItsPage() {
        item(1L, PAGE_ID, SimplePageItem.TEXT, null, "", "<p>body</p>");

        assertEquals("Week 1", producer.getTitle("/lessonbuilder/item/1"));
        assertEquals("<p>body</p>", producer.getContent("/lessonbuilder/item/1"));
    }

    @Test
    public void namedItemKeepsItsName() {
        item(2L, PAGE_ID, SimplePageItem.TEXT, null, "Intro", "<p>body</p>");

        assertEquals("Intro", producer.getTitle("/lessonbuilder/item/2"));
    }

    @Test
    public void subpageIsFoundByItsTitle() {
        item(3L, PAGE_ID, SimplePageItem.PAGE, Long.toString(SUBPAGE_ID), "Readings", null);

        assertTrue(producer.isForIndex("/lessonbuilder/item/3"));
        assertEquals("Readings", producer.getContent("/lessonbuilder/item/3"));
        assertEquals("Readings", producer.getTitle("/lessonbuilder/item/3"));
    }

    @Test
    public void resultOpensThePageTheTextIsOn() {
        item(4L, PAGE_ID, SimplePageItem.TEXT, null, "", "<p>body</p>");
        when(dao.getPageUrl(PAGE_ID)).thenReturn("https://sakai/page20");

        assertEquals("https://sakai/page20", producer.getUrl("/lessonbuilder/item/4"));
    }

    @Test
    public void subpageResultOpensTheSubpage() {
        item(5L, PAGE_ID, SimplePageItem.PAGE, Long.toString(SUBPAGE_ID), "Readings", null);
        when(dao.getPageUrl(SUBPAGE_ID)).thenReturn("https://sakai/page30");

        assertEquals("https://sakai/page30", producer.getUrl("/lessonbuilder/item/5"));
    }

    @Test
    public void onlyTextAndSubpagesAreIndexed() {
        item(6L, 0L, SimplePageItem.PAGE, Long.toString(PAGE_ID), "Week 1", null);
        item(7L, PAGE_ID, SimplePageItem.RESOURCE, "/group/site/file.pdf", "file.pdf", null);
        item(8L, PAGE_ID, SimplePageItem.COMMENTS, null, "", null);

        assertFalse("top-level page", producer.isForIndex("/lessonbuilder/item/6"));
        assertFalse(producer.isForIndex("/lessonbuilder/item/7"));
        assertFalse(producer.isForIndex("/lessonbuilder/item/8"));
    }

    @Test
    public void siteRebuildListsTextBoxesAndSubpages() {
        SimplePageItem text = new SimplePageItemImpl();
        text.setId(9L);
        when(dao.findTextItemsInSite("site")).thenReturn(List.of(text));
        when(dao.findSubpageItemIdsInSite("site")).thenReturn(List.of(10L));

        List<Object> references = new ArrayList<>();
        Iterator<?> iterator = producer.getSiteContentIterator("site");
        iterator.forEachRemaining(references::add);

        assertEquals(List.of("/lessonbuilder/item/9", "/lessonbuilder/item/10"), references);
    }

    private void item(long id, long pageId, int type, String sakaiId, String name, String html) {
        SimplePageItem item = new SimplePageItemImpl(pageId, 1, type, sakaiId, name);
        item.setId(id);
        item.setHtml(html);
        when(dao.findItem(id)).thenReturn(item);
    }
}
