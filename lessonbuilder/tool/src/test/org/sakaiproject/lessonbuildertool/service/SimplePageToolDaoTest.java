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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = LessonBuilderServiceTestConfiguration.class)
@Transactional
public class SimplePageToolDaoTest {

    @Autowired private SimplePageToolDao dao;

    @Test
    public void findsOnlyRequestedTopLevelPagesWithTheirVisibility() {
        SimplePage visible = dao.makePage("visible", "site", "Visible", null, null);
        SimplePage hidden = dao.makePage("hidden", "site", "Hidden", null, null);
        hidden.setHiddenFromNavigation(true);
        assertTrue(dao.quickSaveItem(visible));
        assertTrue(dao.quickSaveItem(hidden));
        // Subpages share the top-level page's tool ID and must not override its visibility.
        SimplePage subpage = dao.makePage("visible", "site", "Subpage", visible.getPageId(), visible.getPageId());
        subpage.setHiddenFromNavigation(true);
        assertTrue(dao.quickSaveItem(subpage));
        assertTrue(dao.quickSaveItem(dao.makePage("other", "site", "Other", null, null)));
        dao.flush();
        dao.clear();

        Map<String, Boolean> visibility = dao.findSimplePagesBySitePageIds(List.of("visible", "hidden", "missing"))
            .stream().collect(Collectors.toMap(SimplePage::getToolId, SimplePage::isHiddenFromNavigation));
        assertEquals(Map.of("visible", false, "hidden", true), visibility);
        assertTrue(dao.findSimplePagesBySitePageIds(List.of()).isEmpty());
        assertTrue(dao.findSimplePagesBySitePageIds(List.of("missing")).isEmpty());
    }
}
