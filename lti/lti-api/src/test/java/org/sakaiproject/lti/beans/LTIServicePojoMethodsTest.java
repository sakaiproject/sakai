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
        toolMap.put("lti13", "1");
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
        assertEquals("LTI13 should match", "1", tool.lti13);
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
        assertEquals("LTI13 should match in round-trip", "1", resultMap.get("lti13"));
        assertEquals("Consumer key should match in round-trip", "test-key", resultMap.get("consumerkey"));
        assertEquals("Secret should match in round-trip", "test-secret", resultMap.get("secret"));
        assertTrue("Sendname should be true in round-trip", (Boolean) resultMap.get("sendname"));
        assertFalse("Sendemailaddr should be false in round-trip", (Boolean) resultMap.get("sendemailaddr"));
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
        contentMap.put("lti13", 789L);
        
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
        assertEquals("LTI13 should match", Long.valueOf(789L), content.lti13);
        
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
        assertTrue("Newpage should be true in round-trip", (Boolean) resultMap.get("newpage"));
        assertFalse("Protect should be false in round-trip", (Boolean) resultMap.get("protect"));
        assertTrue("Debug should be true in round-trip", (Boolean) resultMap.get("debug"));
        assertEquals("LTI13 should match in round-trip", Long.valueOf(789L), resultMap.get("lti13"));
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
}
