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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = LessonBuilderServiceTestConfiguration.class)
@Transactional
public class LessonItemServiceTests {
    @Autowired private LessonItemService service;
    @Autowired private SimplePageToolDao dao;
    @Autowired private SecurityService security;
    @Autowired private SiteService sites;
    @Autowired private SessionManager sessions;
    @Autowired private SessionFactory sessionFactory;
    private SimplePage ownPage;
    private SimplePage otherPage;
    private SimplePageItem lesson;

    @Before
    public void setup() {
        reset(security, sites, sessions);
        when(sessions.getCurrentSessionUserId()).thenReturn("editor");
        when(sites.siteReference("site-a")).thenReturn("/site/site-a");
        when(security.unlock("editor", SimplePage.PERMISSION_LESSONBUILDER_UPDATE, "/site/site-a")).thenReturn(true);
        ownPage = dao.makePage("tool-a", "site-a", "Own page", null, null);
        otherPage = dao.makePage("tool-b", "site-b", "Other page", null, null);
        assertTrue(dao.quickSaveItem(ownPage));
        assertTrue(dao.quickSaveItem(otherPage));
        lesson = dao.makeItem(0, 1, SimplePageItem.PAGE, Long.toString(ownPage.getPageId()), "Lesson");
        assertTrue(dao.quickSaveItem(lesson));
    }

    @Test
    public void editorCanCreateItemsOnAuthorizedEmptyPage() {
        SimplePageItem item = dao.makeItem(ownPage.getPageId(), 1, SimplePageItem.TEXT, "", "Text");
        assertEquals(1, service.createItems("site-a", lesson.getId(), List.of(item)).size());
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
        assertEquals(1, dao.findPageItemsByPageId(ownPage.getPageId()).size());
    }

    @Test
    public void foreignPagePayloadIsRejectedBeforeAnyInsert() {
        SimplePageItem own = dao.makeItem(ownPage.getPageId(), 1, SimplePageItem.TEXT, "", "Own");
        SimplePageItem foreign = dao.makeItem(otherPage.getPageId(), 2, SimplePageItem.TEXT, "", "Foreign");
        assertThrows(SecurityException.class, () -> service.createItems("site-a", lesson.getId(), List.of(own, foreign)));
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
        assertTrue(dao.findPageItemsByPageId(ownPage.getPageId()).isEmpty());
        assertTrue(dao.findPageItemsByPageId(otherPage.getPageId()).isEmpty());
    }

    @Test
    public void unrelatedSiteAndUnauthorizedCallerAreRejected() {
        SimplePageItem item = dao.makeItem(ownPage.getPageId(), 1, SimplePageItem.TEXT, "", "Own");
        assertThrows(SecurityException.class, () -> service.createItems("site-b", lesson.getId(), List.of(item)));
        when(sessions.getCurrentSessionUserId()).thenReturn("reader");
        assertThrows(SecurityException.class, () -> service.createItems("site-a", lesson.getId(), List.of(item)));
        assertTrue(dao.findPageItemsByPageId(ownPage.getPageId()).isEmpty());
    }
}
