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
