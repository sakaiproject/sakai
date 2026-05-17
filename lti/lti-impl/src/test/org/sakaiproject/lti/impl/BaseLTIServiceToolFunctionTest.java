/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.lti.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.lti.impl.testutil.LtiToolFunctionTestDatabase;

/**
 * Integration-style tests for {@link BaseLTIService} tool API function persistence.
 */
public class BaseLTIServiceToolFunctionTest {

    private static final String ADMIN_SITE = LTIService.ADMIN_SITE;
    private static final String REGULAR_SITE = "site.example.com";

    private LtiToolFunctionTestDatabase database;
    private LTIService ltiService;

    @Before
    public void setUp() throws SQLException {
        database = new LtiToolFunctionTestDatabase();
        ltiService = database.getLtiService();
    }

    @After
    public void tearDown() {
        if (database != null) {
            database.shutdown();
        }
    }

    @Test
    public void setAndGetToolFunctionNames_roundTrip() {
        Long toolId = insertTestTool();
        Set<String> requested = new HashSet<>(Arrays.asList("content.read", "gradebook.write"));

        Object result = ltiService.setToolFunctionNames(toolId, requested, ADMIN_SITE);
        assertEquals(toolId, result);

        List<String> stored = ltiService.getToolFunctionNames(String.valueOf(toolId), ADMIN_SITE);
        assertEquals(2, stored.size());
        assertTrue(stored.contains("content.read"));
        assertTrue(stored.contains("gradebook.write"));
    }

    @Test
    public void setToolFunctionNames_replacesExisting() {
        Long toolId = insertTestTool();

        ltiService.setToolFunctionNames(toolId, new HashSet<>(Arrays.asList("content.read")), ADMIN_SITE);
        ltiService.setToolFunctionNames(toolId, new HashSet<>(Arrays.asList("gradebook.read")), ADMIN_SITE);

        List<String> stored = ltiService.getToolFunctionNames(String.valueOf(toolId), ADMIN_SITE);
        assertEquals(1, stored.size());
        assertEquals("gradebook.read", stored.get(0));
    }

    @Test
    public void setToolFunctionNames_ignoresUnregisteredFunctions() {
        Long toolId = insertTestTool();
        Set<String> requested = new HashSet<>(Arrays.asList("content.read", "not.a.real.function"));

        ltiService.setToolFunctionNames(toolId, requested, ADMIN_SITE);

        List<String> stored = ltiService.getToolFunctionNames(String.valueOf(toolId), ADMIN_SITE);
        assertEquals(1, stored.size());
        assertEquals("content.read", stored.get(0));
    }

    @Test
    public void setToolFunctionNames_emptySetClearsAll() {
        Long toolId = insertTestTool();
        ltiService.setToolFunctionNames(toolId, new HashSet<>(Arrays.asList("content.read", "content.write")), ADMIN_SITE);

        ltiService.setToolFunctionNames(toolId, new HashSet<>(), ADMIN_SITE);

        assertTrue(ltiService.getToolFunctionNames(String.valueOf(toolId), ADMIN_SITE).isEmpty());
    }

    @Test
    public void getToolFunctionNames_nonAdminSiteReturnsEmpty() {
        Long toolId = insertTestTool();
        ltiService.setToolFunctionNames(toolId, new HashSet<>(Arrays.asList("content.read")), ADMIN_SITE);

        assertTrue(ltiService.getToolFunctionNames(String.valueOf(toolId), REGULAR_SITE).isEmpty());
    }

    @Test
    public void setToolFunctionNames_nonAdminSiteNotAuthorized() {
        Long toolId = insertTestTool();

        Object result = ltiService.setToolFunctionNames(toolId, new HashSet<>(Arrays.asList("content.read")), REGULAR_SITE);

        assertEquals("Not authorized", result);
        assertTrue(ltiService.getToolFunctionNames(String.valueOf(toolId), ADMIN_SITE).isEmpty());
    }

    @Test
    public void setToolFunctionNames_unknownToolReturnsError() {
        Object result = ltiService.setToolFunctionNames(99999L, new HashSet<>(Arrays.asList("content.read")), ADMIN_SITE);
        assertEquals("Tool not found", result);
    }

    @Test
    public void deleteToolFunctionsForToolId_removesAllRows() {
        Long toolId = insertTestTool();
        ltiService.setToolFunctionNames(toolId, new HashSet<>(Arrays.asList("content.read", "gradebook.read")), ADMIN_SITE);

        int deleted = ltiService.deleteToolFunctionsForToolIdDao(String.valueOf(toolId));

        assertTrue(deleted >= 2);
        assertTrue(ltiService.getToolFunctionNames(String.valueOf(toolId), ADMIN_SITE).isEmpty());
    }

    @Test
    public void getToolFunctionModel_adminOnly() {
        assertNotNull(ltiService.getToolFunctionModel(ADMIN_SITE));
        assertEquals(null, ltiService.getToolFunctionModel(REGULAR_SITE));
    }

    private Long insertTestTool() {
        Properties toolProps = new Properties();
        toolProps.setProperty(LTIService.LTI_TITLE, "API Function Test Tool");
        toolProps.setProperty(LTIService.LTI_LAUNCH, "https://example.com/lti/launch");
        Object key = ltiService.insertTool(toolProps, ADMIN_SITE);
        assertNotNull(key);
        assertTrue(key instanceof Long);
        return (Long) key;
    }
}
