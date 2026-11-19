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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.api.LessonBuilderConstants;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = LessonBuilderServiceTestConfiguration.class)
@Transactional
public class PlacementPageServiceTest {

    private static final String SITE_ID = "site";
    private static final String TOOL_PAGE_ID = "tool-page";

    @Autowired private PlacementPageService service;
    @Autowired private SimplePageToolDao dao;
    @Autowired private SiteService siteService;
    @Autowired private SecurityService securityService;

    private Site site;

    @Before
    public void setUp() throws Exception {
        reset(siteService, securityService);
        site = mock(Site.class);
        when(siteService.getSite(SITE_ID)).thenReturn(site);
        when(siteService.siteReference(SITE_ID)).thenReturn("/site/" + SITE_ID);
        when(securityService.unlock(any(String.class), any(String.class))).thenReturn(true);
        ToolConfiguration placement = mock(ToolConfiguration.class);
        when(placement.getToolId()).thenReturn(LessonBuilderConstants.TOOL_ID);
        when(placement.getPageId()).thenReturn(TOOL_PAGE_ID);
        when(placement.getTitle()).thenReturn("Lessons");
        when(site.getTools(LessonBuilderConstants.TOOL_ID)).thenReturn(List.of(placement));
    }

    @Test
    public void ensurePlacementPagesExistCreatesMissingRecordsOnce() {
        assertEquals(2, service.ensurePlacementPagesExist(SITE_ID));
        assertEquals(0, service.ensurePlacementPagesExist(SITE_ID));
        dao.flush();
        dao.clear();

        List<SimplePage> pages = dao.getSitePages(SITE_ID);
        assertEquals(1, pages.size());
        assertEquals(TOOL_PAGE_ID, pages.get(0).getToolId());
        assertNotNull(dao.findTopLevelPageItemBySakaiId(Long.toString(pages.get(0).getPageId())));
    }

    @Test
    public void ensurePlacementPagesExistRepairsMissingTopLevelItem() {
        SimplePage page = dao.makePage(TOOL_PAGE_ID, SITE_ID, "Lessons", null, null);
        assertTrue(dao.quickSaveItem(page));
        dao.flush();

        assertEquals(1, service.ensurePlacementPagesExist(SITE_ID));
        assertNotNull(dao.findTopLevelPageItemBySakaiId(Long.toString(page.getPageId())));
    }
}
