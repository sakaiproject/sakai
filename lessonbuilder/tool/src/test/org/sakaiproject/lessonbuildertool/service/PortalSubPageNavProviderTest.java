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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.json.simple.JSONValue;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.portal.api.PortalSubPageNavProvider;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = LessonBuilderServiceTestConfiguration.class)
public class PortalSubPageNavProviderTest {

    @Autowired private SimplePageToolDao dao;
    @Autowired private PortalService portalService;
    @Autowired private SiteService siteService;

    @BeforeClass
    public static void enableKernelTestMode() {
        ComponentManager.testingMode = true;
    }

    @AfterClass
    public static void disableKernelTestMode() {
        ComponentManager.shutdown();
        ComponentManager.testingMode = false;
    }

    @Test
    public void registeredProviderReadsSubPagesWithoutAnExistingTransaction() throws Exception {
        String siteId = "subnav-site";
        String pageId = "subnav-page";
        String userId = "subnav-user";
        String toolId = "subnav-tool";
        Site site = mock(Site.class);
        SitePage sitePage = mock(SitePage.class);
        ToolConfiguration tool = mock(ToolConfiguration.class);
        when(siteService.siteReference(siteId)).thenReturn("/site/" + siteId);
        when(siteService.getSite(siteId)).thenReturn(site);
        when(site.getGroupsWithMember(userId)).thenReturn(List.of());
        when(site.getPage(pageId)).thenReturn(sitePage);
        when(sitePage.getTools()).thenReturn(List.of(tool));
        when(tool.getId()).thenReturn(toolId);

        SimplePage parent = dao.makePage(pageId, siteId, "Lessons", null, null);
        assertTrue(dao.quickSaveItem(parent));
        SimplePage child = dao.makePage(pageId, siteId, "Subpage", parent.getPageId(), parent.getPageId());
        assertTrue(dao.quickSaveItem(child));
        SimplePageItem item = dao.makeItem(parent.getPageId(), 1, SimplePageItem.PAGE,
                Long.toString(child.getPageId()), "Subpage");
        assertTrue(dao.quickSaveItem(item));

        ArgumentCaptor<PortalSubPageNavProvider> providerCaptor = ArgumentCaptor.forClass(PortalSubPageNavProvider.class);
        verify(portalService).registerSubPageNavProvider(providerCaptor.capture());
        PortalSubPageNavProvider provider = providerCaptor.getValue();
        assertEquals("sakai.lessonbuildertool", provider.getName());
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());

        String json = provider.getData(siteId, userId, List.of(pageId));

        assertNotNull(json);
        Map<?, ?> data = (Map<?, ?>) JSONValue.parse(json);
        assertEquals(siteId, data.get("siteId"));
        Map<?, ?> pages = (Map<?, ?>) data.get("pages");
        List<?> subPages = (List<?>) pages.get(toolId);
        assertEquals(1, subPages.size());
        Map<?, ?> subPage = (Map<?, ?>) subPages.get(0);
        assertEquals("Subpage", subPage.get("name"));
        assertEquals(Long.toString(child.getPageId()), subPage.get("sendingPage"));
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
    }
}
