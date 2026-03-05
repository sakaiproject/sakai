/*
 * $URL$
 * $Id$
 *
 * Copyright (c) 2025 Charles R. Severance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.sakaiproject.lti.beans;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.sakaiproject.lti.beans.LtiToolBean;
import org.sakaiproject.lti.beans.LtiContentBean;
import org.sakaiproject.lti.util.SakaiLTIUtil;

/**
 * Integration tests for Bean usage patterns in assignments and lessonbuilder tools.
 * These tests verify that the Bean methods work correctly in realistic usage scenarios.
 */
public class LTIBeanIntegrationTest {

    @Test
    public void testAssignmentsToolUsagePattern() {
        // Simulate the assignments tool usage pattern:
        // 1. Get content bean from LTIService
        // 2. Access properties directly
        // 3. Update properties and call updateContentDao
        
        // Create a content bean (simulating what would come from ltiService.getContentBean())
        LtiContentBean content = new LtiContentBean();
        content.id = 123L;
        content.siteId = "test-site";
        content.title = "Assignment LTI Tool";
        content.description = "An LTI tool for assignments";
        content.launch = "https://example.com/assignment/launch";
        content.toolId = 456L;
        content.placementsecret = "original-secret";
        content.protect = false;
        content.settings = "{\"available\":\"2023-01-01T00:00:00Z\"}";
        
        // Simulate assignments tool accessing properties (no more casting!)
        String title = content.title;
        String description = content.description;
        Long toolId = content.toolId;
        String placementSecret = content.placementsecret;
        Boolean protect = content.protect;
        String settings = content.settings;
        
        // Verify direct property access works
        assertNotNull("Title should not be null", title);
        assertEquals("Title should match", "Assignment LTI Tool", title);
        assertNotNull("Description should not be null", description);
        assertEquals("Description should match", "An LTI tool for assignments", description);
        assertNotNull("Tool ID should not be null", toolId);
        assertEquals("Tool ID should match", Long.valueOf(456L), toolId);
        assertNotNull("Placement secret should not be null", placementSecret);
        assertEquals("Placement secret should match", "original-secret", placementSecret);
        assertNotNull("Protect should not be null", protect);
        assertFalse("Protect should be false", protect);
        assertNotNull("Settings should not be null", settings);
        assertTrue("Settings should contain available date", settings.contains("2023-01-01"));
        
        // Simulate assignments tool updating properties (no more map.put()!)
        content.placementsecret = "updated-secret";
        content.protect = true;
        content.settings = "{\"available\":\"2023-01-01T00:00:00Z\",\"submission\":\"2023-01-15T23:59:59Z\"}";
        
        // Verify updates work
        assertEquals("Placement secret should be updated", "updated-secret", content.placementsecret);
        assertTrue("Protect should be updated to true", content.protect);
        assertTrue("Settings should contain submission date", content.settings.contains("2023-01-15"));
        
        // Simulate calling updateContentDao with the Bean
        Map<String, Object> contentMap = content.asMap();
        assertNotNull("asMap() should not return null", contentMap);
        assertEquals("asMap() should preserve updated placement secret", "updated-secret", contentMap.get("placementsecret"));
        assertEquals("asMap() should preserve updated protect", Integer.valueOf(1), contentMap.get("protect"));
        assertTrue("asMap() should preserve updated settings", contentMap.get("settings").toString().contains("2023-01-15"));
    }

    @Test
    public void testLessonBuilderToolUsagePattern() {
        // Simulate the lessonbuilder tool usage pattern:
        // 1. Get tools list from LTIService
        // 2. Find best tool match
        // 3. Create new content bean
        // 4. Insert content
        
        // Create a list of tool beans (simulating what would come from ltiService.getToolsAsBeans())
        List<LtiToolBean> tools = new ArrayList<>();
        
        LtiToolBean tool1 = new LtiToolBean();
        tool1.id = 100L;
        tool1.title = "Tool A";
        tool1.launch = "https://example.com/tool-a/launch";
        tool1.newpage = 1; // LTI_TOOL_NEWPAGE_ON
        tool1.frameheight = 800;
        tools.add(tool1);
        
        LtiToolBean tool2 = new LtiToolBean();
        tool2.id = 200L;
        tool2.title = "Tool B";
        tool2.launch = "https://example.com/tool-b/launch";
        tool2.newpage = 0; // LTI_TOOL_NEWPAGE_OFF
        tool2.frameheight = 600;
        tools.add(tool2);
        
        // Simulate lessonbuilder finding best tool match (no more map iteration!)
        LtiToolBean selectedTool = null;
        String targetUrl = "https://example.com/tool-b/launch";
        for (LtiToolBean tool : tools) {
            if (targetUrl.equals(tool.launch)) {
                selectedTool = tool;
                break;
            }
        }
        
        assertNotNull("Selected tool should not be null", selectedTool);
        assertEquals("Selected tool ID should match", Long.valueOf(200L), selectedTool.id);
        assertEquals("Selected tool title should match", "Tool B", selectedTool.title);
        
        // Simulate lessonbuilder creating new content bean (no more Properties!)
        LtiContentBean newContent = new LtiContentBean();
        newContent.toolId = selectedTool.id;
        newContent.title = "Lesson Content";
        newContent.launch = "https://example.com/lesson/launch";
        // Convert tool newpage (Integer) to content newpage (Boolean)
        newContent.newpage = selectedTool.newpage != null && selectedTool.newpage == 1;
        newContent.xmlimport = "test-xml-import";
        newContent.custom = "lesson=custom";
        
        // Verify content creation
        assertNotNull("New content should not be null", newContent);
        assertEquals("Tool ID should be set", Long.valueOf(200L), newContent.toolId);
        assertEquals("Title should be set", "Lesson Content", newContent.title);
        assertEquals("Launch should be set", "https://example.com/lesson/launch", newContent.launch);
        assertEquals("Newpage should inherit from tool", false, newContent.newpage);
        assertEquals("XML import should be set", "test-xml-import", newContent.xmlimport);
        assertEquals("Custom should be set", "lesson=custom", newContent.custom);
        
        // Simulate calling insertContent with the Bean
        Map<String, Object> contentMap = newContent.asMap();
        assertNotNull("asMap() should not return null", contentMap);
        assertEquals("asMap() should preserve tool ID", Long.valueOf(200L), contentMap.get("tool_id"));
        assertEquals("asMap() should preserve title", "Lesson Content", contentMap.get("title"));
        assertEquals("asMap() should preserve launch", "https://example.com/lesson/launch", contentMap.get("launch"));
        assertEquals("asMap() should preserve newpage", Integer.valueOf(0), contentMap.get("newpage"));
        assertEquals("asMap() should preserve xmlimport", "test-xml-import", contentMap.get("xmlimport"));
        assertEquals("asMap() should preserve custom", "lesson=custom", contentMap.get("custom"));
    }

    @Test
    public void testSakaiLTIUtilIntegration() {
        // Test that SakaiLTIUtil Bean overloads work with realistic data
        
        // Create tool and content beans
        LtiToolBean tool = new LtiToolBean();
        tool.newpage = 1; // LTI_TOOL_NEWPAGE_ON
        tool.frameheight = 800;
        
        LtiContentBean content = new LtiContentBean();
        content.newpage = false; // Boolean for content
        content.frameheight = 600;
        content.id = 123L;
        content.placementsecret = "test-secret";
        
        // Test getNewpage with Beans
        boolean newpage = SakaiLTIUtil.getNewpage(tool, content, false);
        assertTrue("Tool newpage=1 should override content newpage=0", newpage);
        
        // Test getFrameHeight with Beans
        String height = SakaiLTIUtil.getFrameHeight(tool, content, "400px");
        assertEquals("Content frameheight=600 should override tool frameheight=800", "600px", height);
        
        // Test getLaunchCodeKey with Bean
        String launchKey = SakaiLTIUtil.getLaunchCodeKey(content);
        assertNotNull("Launch code key should not be null", launchKey);
        assertTrue("Launch code key should contain content ID", launchKey.contains("123"));
        
        // Test getLaunchCode with Bean
        String launchCode = SakaiLTIUtil.getLaunchCode(content);
        assertNotNull("Launch code should not be null", launchCode);
        assertTrue("Launch code should contain content ID", launchCode.contains("123"));
    }

    @Test
    public void testTypeSafetyBenefits() {
        // Demonstrate the type safety benefits of Bean approach
        
        LtiContentBean content = new LtiContentBean();
        content.id = 123L;
        content.title = "Test Content";
        content.toolId = 456L;
        content.protect = true;
        
        // With Beans, we get compile-time type checking
        Long id = content.id;           // Compile-time verified as Long
        String title = content.title;   // Compile-time verified as String
        Long toolId = content.toolId;   // Compile-time verified as Long
        Boolean protect = content.protect; // Compile-time verified as Boolean
        
        // No more casting or runtime type errors!
        assertNotNull("ID should not be null", id);
        assertEquals("ID should be Long", Long.class, id.getClass());
        assertNotNull("Title should not be null", title);
        assertEquals("Title should be String", String.class, title.getClass());
        assertNotNull("Tool ID should not be null", toolId);
        assertEquals("Tool ID should be Long", Long.class, toolId.getClass());
        assertNotNull("Protect should not be null", protect);
        assertEquals("Protect should be Boolean", Boolean.class, protect.getClass());
        
        // Compare with old map-based approach (commented out to show the difference):
        /*
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("id", 123L);
        contentMap.put("title", "Test Content");
        contentMap.put("tool_id", 456L);
        contentMap.put("protect", true);
        
        // Old way - runtime casting, potential ClassCastException
        Long oldId = (Long) contentMap.get("id");
        String oldTitle = (String) contentMap.get("title");
        Long oldToolId = (Long) contentMap.get("tool_id");
        Boolean oldProtect = (Boolean) contentMap.get("protect");
        */
    }

    @Test
    public void testSecurityBenefits() {
        // Demonstrate the security benefits of Bean toString() exclusions
        
        LtiToolBean tool = new LtiToolBean();
        tool.id = 123L;
        tool.title = "Test Tool";
        tool.secret = "super-secret-key";
        tool.lti13AutoToken = "auto-token-123";
        tool.consumerkey = "consumer-key-456";
        
        LtiContentBean content = new LtiContentBean();
        content.id = 456L;
        content.title = "Test Content";
        content.placementsecret = "placement-secret-789";
        content.oldplacementsecret = "old-placement-secret-012";
        
        // Test that sensitive fields are excluded from toString()
        String toolToString = tool.toString();
        String contentToString = content.toString();
        
        // Sensitive fields should be excluded
        assertFalse("Tool toString should not contain secret", toolToString.contains("super-secret-key"));
        assertFalse("Tool toString should not contain lti13AutoToken", toolToString.contains("auto-token-123"));
        assertFalse("Content toString should not contain placementsecret", contentToString.contains("placement-secret-789"));
        assertFalse("Content toString should not contain oldplacementsecret", contentToString.contains("old-placement-secret-012"));
        
        // Non-sensitive fields should be included
        assertTrue("Tool toString should contain title", toolToString.contains("Test Tool"));
        assertTrue("Tool toString should contain consumerkey (not excluded)", toolToString.contains("consumer-key-456"));
        assertTrue("Content toString should contain title", contentToString.contains("Test Content"));
        
        // This prevents accidental logging of sensitive data!
    }
}
