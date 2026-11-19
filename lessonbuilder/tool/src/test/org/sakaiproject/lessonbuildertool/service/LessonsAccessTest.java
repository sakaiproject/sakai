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
package org.sakaiproject.lessonbuildertool.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = LessonBuilderServiceTestConfiguration.class)
@Transactional
public class LessonsAccessTest {

    private static final String SITE_ID = "site";
    private static final String USER_ID = "owner";

    @Autowired private LessonsAccess service;
    @Autowired private SimplePageToolDao dao;
    @Autowired private SecurityService securityService;
    @Autowired private SiteService siteService;
    @Autowired private UserDirectoryService userDirectoryService;
    @Autowired private User lessonBuilderTestUser;

    private SimplePage page;
    private SimplePageItem item;
    private SimplePageBean pageBean;

    @Before
    public void setUp() {
        reset(securityService, siteService, userDirectoryService, lessonBuilderTestUser);
        when(siteService.siteReference(SITE_ID)).thenReturn("/site/" + SITE_ID);
        when(lessonBuilderTestUser.getId()).thenReturn(USER_ID);
        when(userDirectoryService.getCurrentUser()).thenReturn(lessonBuilderTestUser);
        page = dao.makePage("tool", SITE_ID, "Owned page", null, null);
        page.setOwner(USER_ID);
        page.setHidden(true);
        assertTrue(dao.quickSaveItem(page));
        item = dao.makeItem(page.getPageId(), 1, SimplePageItem.TEXT, "", "Source");
        assertTrue(dao.quickSaveItem(item));

        // RSF creates this request bean outside the Spring application context.
        pageBean = new SimplePageBean();
        pageBean.setSecurityService(securityService);
        pageBean.setUserDirectoryService(userDirectoryService);
        pageBean.setCurrentPage(page);
        pageBean.setCurrentPageId(page.getPageId());
        pageBean.setCurrentSiteId(SITE_ID);
    }

    @Test
    public void pageOwnerCanChooseSourcesEvenWhenThePageIsHiddenFromReaders() {
        assertTrue(pageBean.canEditPage());
        assertEquals(List.of(item.getId()), accessibleItems(pageBean).stream().map(SimplePageItem::getId).toList());
    }

    @Test
    public void editorBeanForAnotherPageDoesNotBypassReaderChecks() {
        pageBean.setCurrentPageId(page.getPageId() + 1);
        assertTrue(accessibleItems(pageBean).isEmpty());
    }

    @Test
    public void editorBeanForAnotherSiteDoesNotBypassReaderChecks() {
        pageBean.setCurrentSiteId("another-site");
        assertTrue(accessibleItems(pageBean).isEmpty());
    }

    @Test
    public void matchingBeanWithoutEditPermissionUsesReaderChecks() {
        when(lessonBuilderTestUser.getId()).thenReturn("reader");
        assertTrue(accessibleItems(pageBean).isEmpty());
    }

    @Test
    public void siteWideEditorDoesNotNeedAPageBean() {
        when(securityService.unlock(USER_ID, SimplePage.PERMISSION_LESSONBUILDER_UPDATE, "/site/" + SITE_ID))
                .thenReturn(true);
        assertEquals(List.of(item.getId()), accessibleItems(null).stream().map(SimplePageItem::getId).toList());
    }

    private List<SimplePageItem> accessibleItems(SimplePageBean bean) {
        return service.getAccessibleItemsOnPage(SITE_ID, page.getPageId(), USER_ID, bean);
    }
}
