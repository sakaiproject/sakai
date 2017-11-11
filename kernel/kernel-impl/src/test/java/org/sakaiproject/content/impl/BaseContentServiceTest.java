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
package org.sakaiproject.content.impl;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Simple checks for BaseContentService
 */
public class BaseContentServiceTest {

    private BaseContentService baseContentService;

    @Before
    public void setUp() {
        baseContentService = new DbContentService();
    }

    @Test
    public void  testIsSiteLevelDropbox() {
        assertFalse(baseContentService.isSiteLevelDropbox(""));
        assertFalse(baseContentService.isSiteLevelDropbox("/"));
        assertFalse(baseContentService.isSiteLevelDropbox("/group"));
        assertFalse(baseContentService.isSiteLevelDropbox("/group/siteId"));
        assertFalse(baseContentService.isSiteLevelDropbox("/group-user"));
        assertFalse(baseContentService.isSiteLevelDropbox("/group-user/"));
        assertFalse(baseContentService.isSiteLevelDropbox("/group-user//"));
        assertFalse(baseContentService.isSiteLevelDropbox("/group-user//other"));
        assertFalse(baseContentService.isSiteLevelDropbox("/group-user/siteId/other/"));

        assertTrue(baseContentService.isSiteLevelDropbox("/group-user/siteId"));
        assertTrue(baseContentService.isSiteLevelDropbox("/group-user/siteId/"));
	}

    @Test
    public void  testIsSiteLevelCollection() {
        assertFalse(baseContentService.isSiteLevelCollection(""));
        assertFalse(baseContentService.isSiteLevelCollection("/"));
        assertFalse(baseContentService.isSiteLevelCollection("/group"));
        assertFalse(baseContentService.isSiteLevelCollection("/group/"));
        assertFalse(baseContentService.isSiteLevelCollection("/group//"));
        assertFalse(baseContentService.isSiteLevelCollection("/group///"));
        assertFalse(baseContentService.isSiteLevelCollection("/group//other/"));
        assertFalse(baseContentService.isSiteLevelCollection("/group/siteId/other/"));

        assertTrue(baseContentService.isSiteLevelCollection("/group/siteId"));
        assertTrue(baseContentService.isSiteLevelCollection("/group/siteId/"));
    }
}
