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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageImpl;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageItemImpl;
import org.sakaiproject.lessonbuildertool.api.LessonBuilderEvents;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.springframework.test.util.ReflectionTestUtils;

public class LessonsEntityContentProducerTest {

    private static final long PAGE_ID = 20L;
    private static final long SUBPAGE_ID = 30L;
    private static final String SITE_REFERENCE = "/site/site";

    private SimplePageToolDao dao;
    private SecurityService securityService;
    private SiteService siteService;
    private LessonsAccess lessonsAccess;
    private LessonsEntityContentProducer producer;

    @Before
    public void setUp() {
        dao = mock(SimplePageToolDao.class);
        producer = new LessonsEntityContentProducer();
        producer.setSimplePageToolDao(dao);
        securityService = mock(SecurityService.class);
        siteService = mock(SiteService.class);
        lessonsAccess = mock(LessonsAccess.class);
        producer.setSecurityService(securityService);
        producer.setSiteService(siteService);
        producer.setLessonsAccess(lessonsAccess);
        when(siteService.siteReference("site")).thenReturn(SITE_REFERENCE);

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
        when(dao.getPageUrl(SUBPAGE_ID, 5L)).thenReturn("https://sakai/page30");

        assertEquals("https://sakai/page30", producer.getUrl("/lessonbuilder/item/5"));
    }

    @Test
    public void topLevelPagesAndOtherItemsAreNotIndexed() {
        item(6L, 0L, SimplePageItem.PAGE, Long.toString(PAGE_ID), "Week 1", null);
        item(7L, PAGE_ID, SimplePageItem.RESOURCE, "/group/site/file.pdf", "file.pdf", null);

        assertFalse("top-level page", producer.isForIndex("/lessonbuilder/item/6"));
        assertFalse(producer.isForIndex("/lessonbuilder/item/7"));
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

    @Test
    public void memberWhoCanSeeTheItemCanReadIt() {
        item(20L, PAGE_ID, SimplePageItem.TEXT, null, "", "<p>body</p>");
        when(securityService.unlock(SimplePage.PERMISSION_LESSONBUILDER_READ, SITE_REFERENCE)).thenReturn(true);
        when(lessonsAccess.isItemAccessible(20L, "site", "user", null)).thenReturn(true);
        User user = mock(User.class);
        when(user.getId()).thenReturn("user");

        try (MockedStatic<UserDirectoryService> users = mockStatic(UserDirectoryService.class)) {
            users.when(UserDirectoryService::getCurrentUser).thenReturn(user);
            assertTrue(producer.canRead("/lessonbuilder/item/20"));
        }
    }

    @Test
    public void userWhoCannotVisitTheSiteCannotRead() throws Exception {
        item(21L, PAGE_ID, SimplePageItem.TEXT, null, "", "<p>body</p>");
        when(siteService.getSiteVisit("site")).thenThrow(new PermissionException("user", "site.visit", "site"));

        assertFalse(producer.canRead("/lessonbuilder/item/21"));
        verifyNoInteractions(lessonsAccess);
    }

    @Test
    public void userWithoutLessonsReadPermissionCannotRead() {
        item(22L, PAGE_ID, SimplePageItem.TEXT, null, "", "<p>body</p>");

        assertFalse(producer.canRead("/lessonbuilder/item/22"));
        verifyNoInteractions(lessonsAccess);
    }

    @Test
    public void failedAccessCheckDeniesTheResult() {
        item(23L, PAGE_ID, SimplePageItem.TEXT, null, "", "<p>body</p>");
        when(securityService.unlock(SimplePage.PERMISSION_LESSONBUILDER_READ, SITE_REFERENCE)).thenThrow(new IllegalStateException("broken"));

        assertFalse(producer.canRead("/lessonbuilder/item/23"));
    }

    @Test
    public void renamingAPageReindexesOnlyTheTextBoxesThatTakeItsTitle() {
        SearchIndexBuilder searchIndexBuilder = mock(SearchIndexBuilder.class);
        EventTrackingService eventTrackingService = mock(EventTrackingService.class);
        ReflectionTestUtils.setField(producer, "searchIndexBuilder", searchIndexBuilder);
        producer.setEventTrackingService(eventTrackingService);
        Event reindex = mock(Event.class);
        when(eventTrackingService.newEvent(LessonBuilderEvents.ITEM_UPDATE, "/lessonbuilder/item/30", false)).thenReturn(reindex);
        SimplePageItem untitled = new SimplePageItemImpl(PAGE_ID, 1, SimplePageItem.TEXT, null, "");
        untitled.setId(30L);
        SimplePageItem named = new SimplePageItemImpl(PAGE_ID, 2, SimplePageItem.TEXT, null, "Intro");
        named.setId(31L);
        SimplePageItem subpage = new SimplePageItemImpl(PAGE_ID, 3, SimplePageItem.PAGE, "40", "");
        subpage.setId(32L);
        when(dao.findItemsOnPage(PAGE_ID)).thenReturn(List.of(untitled, named, subpage));
        Event pageUpdate = mock(Event.class);
        when(pageUpdate.getEvent()).thenReturn(LessonBuilderEvents.PAGE_UPDATE);
        when(pageUpdate.getResource()).thenReturn("/lessonbuilder/page/" + PAGE_ID);

        assertTrue(producer.matches(pageUpdate));
        assertEquals(SearchBuilderItem.ACTION_UNKNOWN, producer.getAction(pageUpdate));

        verify(searchIndexBuilder).addResource(null, reindex);
        verifyNoMoreInteractions(searchIndexBuilder);
        verify(eventTrackingService, never()).post(any(Event.class));
    }

    private void item(long id, long pageId, int type, String sakaiId, String name, String html) {
        SimplePageItem item = new SimplePageItemImpl(pageId, 1, type, sakaiId, name);
        item.setId(id);
        item.setHtml(html);
        when(dao.findItem(id)).thenReturn(item);
    }
}
