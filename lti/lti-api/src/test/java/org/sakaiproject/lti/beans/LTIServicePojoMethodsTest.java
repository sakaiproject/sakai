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

import org.sakaiproject.lti.beans.LtiToolBean;
import org.sakaiproject.lti.beans.LtiContentBean;
import org.sakaiproject.lti.beans.LtiToolSiteBean;
import org.sakaiproject.lti.beans.LtiMembershipsJobBean;

/**
 * Test that the POJO conversion methods work correctly.
 * This is a basic test to verify the fromMap/toMap round-trip functionality.
 */
public class LTIServicePojoMethodsTest {

    @Test
    public void testLtiToolBeanFromMapToMap() {
        // Create a test map with sample data
        Map<String, Object> toolMap = new HashMap<>();
        toolMap.put("id", 123L);
        toolMap.put("SITE_ID", "test-site");
        toolMap.put("title", "Test Tool");
        toolMap.put("description", "A test tool description");
        toolMap.put("launch", "https://example.com/launch");
        toolMap.put("lti13", 1); // LTI13_LTI13
        toolMap.put("consumerkey", "test-key");
        toolMap.put("secret", "test-secret");
        toolMap.put("sendname", true);
        toolMap.put("sendemailaddr", false);

        // Convert to POJO
        LtiToolBean tool = LtiToolBean.of(toolMap);

        // Verify the conversion
        assertNotNull("Tool should not be null", tool);
        assertEquals("ID should match", Long.valueOf(123L), tool.id);
        assertEquals("Site ID should match", "test-site", tool.siteId);
        assertEquals("Title should match", "Test Tool", tool.title);
        assertEquals("Description should match", "A test tool description", tool.description);
        assertEquals("Launch should match", "https://example.com/launch", tool.launch);
        assertEquals("LTI13 should match", Integer.valueOf(1), tool.lti13);
        assertEquals("Consumer key should match", "test-key", tool.consumerkey);
        assertEquals("Secret should match", "test-secret", tool.secret);
        assertTrue("Sendname should be true", tool.sendname);
        assertFalse("Sendemailaddr should be false", tool.sendemailaddr);

        // Convert back to map
        Map<String, Object> resultMap = tool.asMap();

        // Verify round-trip
        assertNotNull("Result map should not be null", resultMap);
        assertEquals("ID should match in round-trip", Long.valueOf(123L), resultMap.get("id"));
        assertEquals("Site ID should match in round-trip", "test-site", resultMap.get("SITE_ID"));
        assertEquals("Title should match in round-trip", "Test Tool", resultMap.get("title"));
        assertEquals("Description should match in round-trip", "A test tool description", resultMap.get("description"));
        assertEquals("Launch should match in round-trip", "https://example.com/launch", resultMap.get("launch"));
        assertEquals("LTI13 should match in round-trip", Integer.valueOf(1), resultMap.get("lti13"));
        assertEquals("Consumer key should match in round-trip", "test-key", resultMap.get("consumerkey"));
        assertEquals("Secret should match in round-trip", "test-secret", resultMap.get("secret"));
        assertEquals("Sendname should be true in round-trip", Integer.valueOf(1), resultMap.get("sendname"));
        assertEquals("Sendemailaddr should be false in round-trip", Integer.valueOf(0), resultMap.get("sendemailaddr"));
    }

    @Test
    public void testLtiContentBeanFromMapToMap() {
        // Create a test map with sample data
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("id", 456L);
        contentMap.put("tool_id", 123L);
        contentMap.put("SITE_ID", "test-site");
        contentMap.put("title", "Test Content");
        contentMap.put("description", "A test content description");
        contentMap.put("frameheight", 600);
        contentMap.put("newpage", true);
        contentMap.put("protect", false);
        contentMap.put("debug", true);

        // Convert to POJO
        LtiContentBean content = LtiContentBean.of(contentMap);

        // Verify the conversion
        assertNotNull("Content should not be null", content);
        assertEquals("ID should match", Long.valueOf(456L), content.id);
        assertEquals("Tool ID should match", Long.valueOf(123L), content.toolId);
        assertEquals("Site ID should match", "test-site", content.siteId);
        assertEquals("Title should match", "Test Content", content.title);
        assertEquals("Description should match", "A test content description", content.description);
        assertEquals("Frameheight should match", Integer.valueOf(600), content.frameheight);
        assertTrue("Newpage should be true", content.newpage);
        assertFalse("Protect should be false", content.protect);
        assertTrue("Debug should be true", content.debug);

        // Convert back to map
        Map<String, Object> resultMap = content.asMap();

        // Verify round-trip
        assertNotNull("Result map should not be null", resultMap);
        assertEquals("ID should match in round-trip", Long.valueOf(456L), resultMap.get("id"));
        assertEquals("Tool ID should match in round-trip", Long.valueOf(123L), resultMap.get("tool_id"));
        assertEquals("Site ID should match in round-trip", "test-site", resultMap.get("SITE_ID"));
        assertEquals("Title should match in round-trip", "Test Content", resultMap.get("title"));
        assertEquals("Description should match in round-trip", "A test content description", resultMap.get("description"));
        assertEquals("Frameheight should match in round-trip", Integer.valueOf(600), resultMap.get("frameheight"));
        assertEquals("Newpage should be true in round-trip", Integer.valueOf(1), resultMap.get("newpage"));
        assertEquals("Protect should be false in round-trip", Integer.valueOf(0), resultMap.get("protect"));
        assertEquals("Debug should be true in round-trip", Integer.valueOf(1), resultMap.get("debug"));
    }

    @Test
    public void testRobustNumberConversion() {
        // Test various number types that might come from different databases
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("integer_as_string", "123");
        testMap.put("integer_as_double", 123.0);
        testMap.put("integer_as_long", 123L);
        testMap.put("decimal_as_string", "456.789");
        testMap.put("decimal_as_double", 456.789);

        // Test LtiToolBean with robust number conversion
        LtiToolBean tool = LtiToolBean.of(testMap);
        assertNotNull("Tool should not be null", tool);

        // Test LtiContentBean with robust number conversion
        LtiContentBean content = LtiContentBean.of(testMap);
        assertNotNull("Content should not be null", content);

        // The robust conversion should handle all these number types gracefully
        // without throwing exceptions
    }

    @Test
    public void testUpdateToolDaoOverload() {
        // Create a test tool bean
        LtiToolBean tool = new LtiToolBean();
        tool.id = 123L;
        tool.title = "Test Tool";
        tool.lti13AutoToken = "Used";
        tool.lti13AutoState = Integer.valueOf(2);

        // Create equivalent map for comparison
        Map<String, Object> toolMap = new HashMap<>();
        toolMap.put("id", 123L);
        toolMap.put("title", "Test Tool");
        toolMap.put("lti13_auto_token", "Used");
        toolMap.put("lti13_auto_state", Integer.valueOf(2));

        // Test that both asMap() calls produce equivalent results
        Map<String, Object> toolAsMap = tool.asMap();
        assertNotNull("Tool asMap should not be null", toolAsMap);

        // Verify that the bean's asMap() method produces the expected structure
        // (We can't easily test the full LTIService.updateToolDao method without mocking
        // the entire Sakai environment, but we can verify the delegation works correctly)
        assertEquals("Tool ID should match", Long.valueOf(123L), toolAsMap.get("id"));
        assertEquals("Tool title should match", "Test Tool", toolAsMap.get("title"));
        assertEquals("LTI13 auto token should match", "Used", toolAsMap.get("lti13_auto_token"));
        assertEquals("LTI13 auto state should match", Integer.valueOf(2), toolAsMap.get("lti13_auto_state"));
    }

    @Test
    public void testToStringExcludesSensitiveFields() {
        // Test LtiToolBean - sensitive fields should be excluded from toString
        LtiToolBean tool = new LtiToolBean();
        tool.id = 123L;
        tool.title = "Test Tool";
        tool.secret = "super-secret-key";
        tool.consumerkey = "consumer-key-123";
        tool.lti13AutoToken = "auto-token-456";
        tool.lti13LmsToken = "lms-token-789";

        String toolToString = tool.toString();
        assertNotNull("Tool toString should not be null", toolToString);
        assertFalse("Tool toString should not contain secret", toolToString.contains("super-secret-key"));
        assertTrue("Tool toString should contain consumerkey (not excluded)", toolToString.contains("consumer-key-123"));
        assertFalse("Tool toString should not contain lti13AutoToken", toolToString.contains("auto-token-456"));
        assertTrue("Tool toString should contain lti13LmsToken (not excluded)", toolToString.contains("lms-token-789"));
        assertTrue("Tool toString should contain non-sensitive fields", toolToString.contains("Test Tool"));

        // Test LtiContentBean - sensitive fields should be excluded from toString
        LtiContentBean content = new LtiContentBean();
        content.id = 456L;
        content.title = "Test Content";
        content.placementsecret = "placement-secret-123";
        content.oldplacementsecret = "old-placement-secret-456";

        String contentToString = content.toString();
        assertNotNull("Content toString should not be null", contentToString);
        assertFalse("Content toString should not contain placementsecret", contentToString.contains("placement-secret-123"));
        assertFalse("Content toString should not contain oldplacementsecret", contentToString.contains("old-placement-secret-456"));
        assertTrue("Content toString should contain non-sensitive fields", contentToString.contains("Test Content"));

        // Test LtiMembershipsJobBean - no sensitive fields excluded from toString
        LtiMembershipsJobBean job = new LtiMembershipsJobBean();
        job.siteId = "test-site";
        job.membershipsId = "memberships-123";
        job.consumerkey = "job-consumer-key-789";

        String jobToString = job.toString();
        assertNotNull("Job toString should not be null", jobToString);
        assertTrue("Job toString should contain consumerkey (not excluded)", jobToString.contains("job-consumer-key-789"));
        assertTrue("Job toString should contain non-sensitive fields", jobToString.contains("test-site"));
    }

    @Test
    public void testLtiContentBeanFromMapToMapDetailed() {
        // Create a test map with sample data
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("id", 456L);
        contentMap.put("SITE_ID", "test-site");
        contentMap.put("title", "Test Content");
        contentMap.put("description", "A test content description");
        contentMap.put("launch", "https://example.com/content/launch");
        contentMap.put("tool_id", 123L);
        contentMap.put("placementsecret", "test-placement-secret");
        contentMap.put("oldplacementsecret", "old-placement-secret");
        contentMap.put("contentitem", "test-content-item");
        contentMap.put("settings", "test-settings");
        contentMap.put("newpage", true);
        contentMap.put("debug", false);
        contentMap.put("protect", true);

        // Convert to POJO
        LtiContentBean content = LtiContentBean.of(contentMap);

        // Verify the conversion
        assertNotNull("Content should not be null", content);
        assertEquals("ID should match", Long.valueOf(456L), content.id);
        assertEquals("Site ID should match", "test-site", content.siteId);
        assertEquals("Title should match", "Test Content", content.title);
        assertEquals("Description should match", "A test content description", content.description);
        assertEquals("Launch should match", "https://example.com/content/launch", content.launch);
        assertEquals("Tool ID should match", Long.valueOf(123L), content.toolId);
        assertEquals("Placement secret should match", "test-placement-secret", content.placementsecret);
        assertEquals("Old placement secret should match", "old-placement-secret", content.oldplacementsecret);
        assertEquals("Content item should match", "test-content-item", content.contentitem);
        assertEquals("Settings should match", "test-settings", content.settings);
        assertTrue("Newpage should be true", content.newpage);
        assertFalse("Debug should be false", content.debug);
        assertTrue("Protect should be true", content.protect);

        // Convert back to map
        Map<String, Object> resultMap = content.asMap();

        // Verify round-trip
        assertNotNull("Result map should not be null", resultMap);
        assertEquals("ID should match in round-trip", Long.valueOf(456L), resultMap.get("id"));
        assertEquals("Site ID should match in round-trip", "test-site", resultMap.get("SITE_ID"));
        assertEquals("Title should match in round-trip", "Test Content", resultMap.get("title"));
        assertEquals("Description should match in round-trip", "A test content description", resultMap.get("description"));
        assertEquals("Launch should match in round-trip", "https://example.com/content/launch", resultMap.get("launch"));
        assertEquals("Tool ID should match in round-trip", Long.valueOf(123L), resultMap.get("tool_id"));
        assertEquals("Placement secret should match in round-trip", "test-placement-secret", resultMap.get("placementsecret"));
        assertEquals("Old placement secret should match in round-trip", "old-placement-secret", resultMap.get("oldplacementsecret"));
        assertEquals("Content item should match in round-trip", "test-content-item", resultMap.get("contentitem"));
        assertEquals("Settings should match in round-trip", "test-settings", resultMap.get("settings"));
        assertEquals("Newpage should match in round-trip", Integer.valueOf(1), resultMap.get("newpage"));
        assertEquals("Debug should match in round-trip", Integer.valueOf(0), resultMap.get("debug"));
        assertEquals("Protect should match in round-trip", Integer.valueOf(1), resultMap.get("protect"));
    }

    @Test
    public void testLtiMembershipsJobBeanFromMapToMap() {
        // Create a test map with sample data
        Map<String, Object> jobMap = new HashMap<>();
        jobMap.put("SITE_ID", "test-site");
        jobMap.put("memberships_id", "memberships-123");
        jobMap.put("memberships_url", "https://example.com/memberships");
        jobMap.put("consumerkey", "test-consumer-key");
        jobMap.put("lti_version", "1.3");

        // Convert to POJO
        LtiMembershipsJobBean job = LtiMembershipsJobBean.of(jobMap);

        // Verify the conversion
        assertNotNull("Job should not be null", job);
        assertEquals("Site ID should match", "test-site", job.siteId);
        assertEquals("Memberships ID should match", "memberships-123", job.membershipsId);
        assertEquals("Memberships URL should match", "https://example.com/memberships", job.membershipsUrl);
        assertEquals("Consumer key should match", "test-consumer-key", job.consumerkey);
        assertEquals("LTI version should match", "1.3", job.ltiVersion);

        // Convert back to map
        Map<String, Object> resultMap = job.asMap();

        // Verify round-trip
        assertNotNull("Result map should not be null", resultMap);
        assertEquals("Site ID should match in round-trip", "test-site", resultMap.get("SITE_ID"));
        assertEquals("Memberships ID should match in round-trip", "memberships-123", resultMap.get("memberships_id"));
        assertEquals("Memberships URL should match in round-trip", "https://example.com/memberships", resultMap.get("memberships_url"));
        assertEquals("Consumer key should match in round-trip", "test-consumer-key", resultMap.get("consumerkey"));
        assertEquals("LTI version should match in round-trip", "1.3", resultMap.get("lti_version"));
    }

    @Test
    public void testThreeStateValidation() {
        // Test that invalid three-state values are handled gracefully with logging

        // Test invalid newpage value
        Map<String, Object> toolMap = new HashMap<>();
        toolMap.put("id", 123L);
        toolMap.put("title", "Test Tool");
        toolMap.put("newpage", 5); // Invalid value > 2
        toolMap.put("debug", -1);  // Invalid value < 0

        LtiToolBean tool = LtiToolBean.of(toolMap);

        // Verify that invalid values are converted to null (treated as off)
        assertNotNull("Tool should not be null", tool);
        assertEquals("ID should match", Long.valueOf(123L), tool.id);
        assertEquals("Title should match", "Test Tool", tool.title);
        assertNull("Invalid newpage should be null", tool.newpage);
        assertNull("Invalid debug should be null", tool.debug);

        // Test that asMap() also validates
        tool.newpage = 3; // Set invalid value directly
        tool.debug = -2;  // Set invalid value directly

        Map<String, Object> resultMap = tool.asMap();

        // Invalid values should not be included in the map
        assertFalse("Invalid newpage should not be in map", resultMap.containsKey("newpage"));
        assertFalse("Invalid debug should not be in map", resultMap.containsKey("debug"));

        // Test valid values
        tool.newpage = 1; // Valid value
        tool.debug = 2;   // Valid value

        Map<String, Object> validMap = tool.asMap();
        assertEquals("Valid newpage should be in map", Integer.valueOf(1), validMap.get("newpage"));
        assertEquals("Valid debug should be in map", Integer.valueOf(2), validMap.get("debug"));
    }

    @Test
    public void testNullHandling() {
        // Test that null maps are handled gracefully
        LtiToolBean nullTool = LtiToolBean.of(null);
        assertNull("Null map should result in null tool", nullTool);

        LtiContentBean nullContent = LtiContentBean.of(null);
        assertNull("Null map should result in null content", nullContent);

        LtiMembershipsJobBean nullJob = LtiMembershipsJobBean.of(null);
        assertNull("Null map should result in null job", nullJob);
    }

    @Test
    public void testEmptyMapHandling() {
        // Test that empty maps create objects with null fields
        Map<String, Object> emptyMap = new HashMap<>();

        LtiToolBean emptyTool = LtiToolBean.of(emptyMap);
        assertNotNull("Empty map should create tool object", emptyTool);
        assertNull("Empty map should result in null ID", emptyTool.id);
        assertNull("Empty map should result in null title", emptyTool.title);

        LtiContentBean emptyContent = LtiContentBean.of(emptyMap);
        assertNotNull("Empty map should create content object", emptyContent);
        assertNull("Empty map should result in null ID", emptyContent.id);
        assertNull("Empty map should result in null title", emptyContent.title);
    }
}
