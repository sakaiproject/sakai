/*
 *
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

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.lti.beans.LtiToolBean;

/**
 * Unit tests for LtiToolBean Bean conversion methods.
 */
public class LtiToolBeanTest {

    private Map<String, Object> testMap;
    private Date testDate;

    @Before
    public void setUp() {
        testDate = new Date();
        testMap = new HashMap<>();
        
        // Core fields
        testMap.put("id", 123L);
        testMap.put("SITE_ID", "site123");
        testMap.put("title", "Test Tool Title");
        testMap.put("description", "Test tool description");
        testMap.put("status", "enable");
        testMap.put("visible", "visible");
        testMap.put("deployment_id", 789L);
        testMap.put("launch", "https://example.com/tool/launch");
        testMap.put("newpage", Integer.valueOf(1)); // LTI_TOOL_NEWPAGE_ON
        testMap.put("frameheight", Integer.valueOf(800));
        testMap.put("fa_icon", "fa-tool");
        
        // Message Types (pl_ prefix for backwards compatibility)
        // Note: These are stored as Integer (1/0) in the database, not Boolean
        testMap.put("pl_launch", Integer.valueOf(1));
        testMap.put("pl_linkselection", Integer.valueOf(0));
        testMap.put("pl_contextlaunch", Integer.valueOf(1));
        
        // Placements
        testMap.put("pl_lessonsselection", Integer.valueOf(0));
        testMap.put("pl_contenteditor", Integer.valueOf(1));
        testMap.put("pl_assessmentselection", Integer.valueOf(1));
        testMap.put("pl_coursenav", Integer.valueOf(0));
        testMap.put("pl_importitem", Integer.valueOf(1));
        testMap.put("pl_fileitem", Integer.valueOf(0));
        
        // Privacy
        testMap.put("sendname", Integer.valueOf(1));
        testMap.put("sendemailaddr", Integer.valueOf(0));
        testMap.put("pl_privacy", Integer.valueOf(1));
        
        // Services
        testMap.put("allowoutcomes", Integer.valueOf(1));
        testMap.put("allowlineitems", Integer.valueOf(0));
        testMap.put("allowroster", Integer.valueOf(1));
        
        // Configuration
        testMap.put("debug", Integer.valueOf(1)); // LTI_TOOL_DEBUG_ON
        testMap.put("siteinfoconfig", "config");
        testMap.put("splash", "Welcome to the tool!");
        testMap.put("custom", "custom1=value1\ncustom2=value2");
        testMap.put("rolemap", "Instructor=Teacher");
        testMap.put("lti13", Integer.valueOf(1)); // LTI13_LTI13
        
        // LTI 1.3 security values from the tool
        testMap.put("lti13_tool_keyset", "https://tool.com/keyset");
        testMap.put("lti13_oidc_endpoint", "https://tool.com/oidc");
        testMap.put("lti13_oidc_redirect", "https://tool.com/redirect");
        
        // LTI 1.3 security values from the LMS
        testMap.put("lti13_lms_issuer", "https://lms.com");
        testMap.put("lti13_client_id", "client123");
        testMap.put("lti13_lms_deployment_id", "deployment456");
        testMap.put("lti13_lms_keyset", "https://lms.com/keyset");
        testMap.put("lti13_lms_endpoint", "https://lms.com/endpoint");
        testMap.put("lti13_lms_token", "https://lms.com/token");
        
        // LTI 1.1 security arrangement
        testMap.put("consumerkey", "key123");
        testMap.put("secret", "secret456");
        testMap.put("xmlimport", "<tool>xml</tool>");
        testMap.put("lti13_auto_token", "token789");
        testMap.put("lti13_auto_state", Integer.valueOf(1));
        testMap.put("lti13_auto_registration", "{\"auto\":\"reg\"}");
        testMap.put("sakai_tool_checksum", "checksum123");
        
        // Timestamps
        testMap.put("created_at", testDate);
        testMap.put("updated_at", testDate);
        
        // Live attributes - computed fields that may be present in Map data
        testMap.put("lti_content_count", 42L);
        testMap.put("lti_site_count", 7L);
    }

    @Test
    public void testFromMapNullInput() {
        assertNull(LtiToolBean.of(null));
    }

    @Test
    public void testFromMapEmptyMap() {
        Map<String, Object> emptyMap = new HashMap<>();
        LtiToolBean tool = LtiToolBean.of(emptyMap);
        assertNotNull(tool);
        assertNull(tool.getId());
        assertNull(tool.getTitle());
    }

    @Test
    public void testFromMapCompleteData() {
        LtiToolBean tool = LtiToolBean.of(testMap);
        
        assertNotNull(tool);
        
        // Core fields
        assertEquals(Long.valueOf(123L), tool.getId());
        assertEquals("site123", tool.getSiteId());
        assertEquals("Test Tool Title", tool.getTitle());
        assertEquals("Test tool description", tool.getDescription());
        assertEquals("enable", tool.getStatus());
        assertEquals("visible", tool.getVisible());
        assertEquals(Long.valueOf(789L), tool.getDeploymentId());
        assertEquals("https://example.com/tool/launch", tool.getLaunch());
        assertEquals(Integer.valueOf(1), tool.getNewpage()); // LTI_TOOL_NEWPAGE_ON
        assertEquals(Integer.valueOf(800), tool.getFrameheight());
        assertEquals("fa-tool", tool.getFaIcon());
        
        // Message Types
        assertTrue(tool.getPlLaunch());
        assertFalse(tool.getPlLinkselection());
        assertTrue(tool.getPlContextlaunch());
        
        // Placements
        assertFalse(tool.getPlLessonsselection());
        assertTrue(tool.getPlContenteditor());
        assertTrue(tool.getPlAssessmentselection());
        assertFalse(tool.getPlCoursenav());
        assertTrue(tool.getPlImportitem());
        assertFalse(tool.getPlFileitem());
        
        // Privacy
        assertTrue(tool.getSendname());
        assertFalse(tool.getSendemailaddr());
        assertTrue(tool.getPlPrivacy());
        
        // Services
        assertTrue(tool.getAllowoutcomes());
        assertFalse(tool.getAllowlineitems());
        assertTrue(tool.getAllowroster());
        
        // Configuration
        assertEquals(Integer.valueOf(1), tool.getDebug()); // LTI_TOOL_DEBUG_ON
        assertEquals("config", tool.getSiteinfoconfig());
        assertEquals("Welcome to the tool!", tool.getSplash());
        assertEquals("custom1=value1\ncustom2=value2", tool.getCustom());
        assertEquals("Instructor=Teacher", tool.getRolemap());
        assertEquals(Integer.valueOf(1), tool.getLti13()); // LTI13_LTI13
        
        // LTI 1.3 security values from the tool
        assertEquals("https://tool.com/keyset", tool.getLti13ToolKeyset());
        assertEquals("https://tool.com/oidc", tool.getLti13OidcEndpoint());
        assertEquals("https://tool.com/redirect", tool.getLti13OidcRedirect());
        
        // LTI 1.3 security values from the LMS
        assertEquals("https://lms.com", tool.getLti13LmsIssuer());
        assertEquals("client123", tool.getLti13ClientId());
        assertEquals("deployment456", tool.getLti13LmsDeploymentId());
        assertEquals("https://lms.com/keyset", tool.getLti13LmsKeyset());
        assertEquals("https://lms.com/endpoint", tool.getLti13LmsEndpoint());
        assertEquals("https://lms.com/token", tool.getLti13LmsToken());
        
        // LTI 1.1 security arrangement
        assertEquals("key123", tool.getConsumerkey());
        assertEquals("secret456", tool.getSecret());
        assertEquals("<tool>xml</tool>", tool.getXmlimport());
        assertEquals("token789", tool.getLti13AutoToken());
        assertEquals(Integer.valueOf(1), tool.getLti13AutoState());
        assertEquals("{\"auto\":\"reg\"}", tool.getLti13AutoRegistration());
        assertEquals("checksum123", tool.getSakaiToolChecksum());
        
        // Timestamps
        assertEquals(testDate, tool.getCreatedAt());
        assertEquals(testDate, tool.getUpdatedAt());
        
        // Live attributes
        assertEquals(Long.valueOf(42L), tool.getLtiContentCount());
        assertEquals(Long.valueOf(7L), tool.getLtiSiteCount());
    }

    @Test
    public void testToMapCompleteData() {
        LtiToolBean tool = new LtiToolBean();
        
        // Set all fields
        tool.setId(456L);
        tool.setSiteId("site456");
        tool.setTitle("Test ToMap Tool");
        tool.setDescription("Test toMap description");
        tool.setStatus("disable");
        tool.setVisible("stealth");
        tool.setDeploymentId(101112L);
        tool.setLaunch("https://test.com/launch");
        tool.setNewpage(0); // LTI_TOOL_NEWPAGE_OFF
        tool.setFrameheight(1000);
        tool.setFaIcon("fa-test");
        
        tool.setPlLaunch(false);
        tool.setPlLinkselection(true);
        tool.setPlContextlaunch(false);
        
        tool.setPlLessonsselection(true);
        tool.setPlContenteditor(false);
        tool.setPlAssessmentselection(false);
        tool.setPlCoursenav(true);
        tool.setPlImportitem(false);
        tool.setPlFileitem(true);
        
        tool.setSendname(false);
        tool.setSendemailaddr(true);
        tool.setPlPrivacy(false);
        
        tool.setAllowoutcomes(false);
        tool.setAllowlineitems(true);
        tool.setAllowroster(false);
        
        tool.setDebug(0); // LTI_TOOL_DEBUG_OFF
        tool.setSiteinfoconfig("bypass");
        tool.setSplash("Test splash");
        tool.setCustom("custom4=value4");
        tool.setRolemap("Student=Learner");
        tool.setLti13(0); // LTI13_LTI11
        
        tool.setLti13ToolKeyset("https://test.com/keyset");
        tool.setLti13OidcEndpoint("https://test.com/oidc");
        tool.setLti13OidcRedirect("https://test.com/redirect");
        
        tool.setLti13LmsIssuer("https://test.com");
        tool.setLti13ClientId("testclient");
        tool.setLti13LmsDeploymentId("testdeployment");
        tool.setLti13LmsKeyset("https://test.com/keyset");
        tool.setLti13LmsEndpoint("https://test.com/endpoint");
        tool.setLti13LmsToken("https://test.com/token");
        
        tool.setConsumerkey("testkey");
        tool.setSecret("testsecret");
        tool.setXmlimport("<test>xml</test>");
        tool.setLti13AutoToken("testtoken");
        tool.setLti13AutoState(2);
        tool.setLti13AutoRegistration("{\"test\":\"reg\"}");
        tool.setSakaiToolChecksum("testchecksum");
        
        tool.setCreatedAt(testDate);
        tool.setUpdatedAt(testDate);
        
        // Live attributes
        tool.setLtiContentCount(99L);
        tool.setLtiSiteCount(15L);
        
        Map<String, Object> result = tool.asMap();
        
        assertNotNull(result);
        
        // Core fields
        assertEquals(456L, result.get("id"));
        assertEquals("site456", result.get("SITE_ID"));
        assertEquals("Test ToMap Tool", result.get("title"));
        assertEquals("Test toMap description", result.get("description"));
        assertEquals("disable", result.get("status"));
        assertEquals("stealth", result.get("visible"));
        assertEquals(101112L, result.get("deployment_id"));
        assertEquals("https://test.com/launch", result.get("launch"));
        assertEquals(Integer.valueOf(0), result.get("newpage")); // LTI_TOOL_NEWPAGE_OFF
        assertEquals(1000, result.get("frameheight"));
        assertEquals("fa-test", result.get("fa_icon"));
        
        // Message Types
        assertEquals(Integer.valueOf(0), result.get("pl_launch"));
        assertEquals(Integer.valueOf(1), result.get("pl_linkselection"));
        assertEquals(Integer.valueOf(0), result.get("pl_contextlaunch"));
        
        // Placements
        assertEquals(Integer.valueOf(1), result.get("pl_lessonsselection"));
        assertEquals(Integer.valueOf(0), result.get("pl_contenteditor"));
        assertEquals(Integer.valueOf(0), result.get("pl_assessmentselection"));
        assertEquals(Integer.valueOf(1), result.get("pl_coursenav"));
        assertEquals(Integer.valueOf(0), result.get("pl_importitem"));
        assertEquals(Integer.valueOf(1), result.get("pl_fileitem"));
        
        // Privacy
        assertEquals(Integer.valueOf(0), result.get("sendname"));
        assertEquals(Integer.valueOf(1), result.get("sendemailaddr"));
        assertEquals(Integer.valueOf(0), result.get("pl_privacy"));
        
        // Services
        assertEquals(Integer.valueOf(0), result.get("allowoutcomes"));
        assertEquals(Integer.valueOf(1), result.get("allowlineitems"));
        assertEquals(Integer.valueOf(0), result.get("allowroster"));
        
        // Configuration
        assertEquals(Integer.valueOf(0), result.get("debug")); // LTI_TOOL_DEBUG_OFF
        assertEquals("bypass", result.get("siteinfoconfig"));
        assertEquals("Test splash", result.get("splash"));
        assertEquals("custom4=value4", result.get("custom"));
        assertEquals("Student=Learner", result.get("rolemap"));
        assertEquals(Integer.valueOf(0), result.get("lti13")); // LTI13_LTI11
        
        // LTI 1.3 security values from the tool
        assertEquals("https://test.com/keyset", result.get("lti13_tool_keyset"));
        assertEquals("https://test.com/oidc", result.get("lti13_oidc_endpoint"));
        assertEquals("https://test.com/redirect", result.get("lti13_oidc_redirect"));
        
        // LTI 1.3 security values from the LMS
        assertEquals("https://test.com", result.get("lti13_lms_issuer"));
        assertEquals("testclient", result.get("lti13_client_id"));
        assertEquals("testdeployment", result.get("lti13_lms_deployment_id"));
        assertEquals("https://test.com/keyset", result.get("lti13_lms_keyset"));
        assertEquals("https://test.com/endpoint", result.get("lti13_lms_endpoint"));
        assertEquals("https://test.com/token", result.get("lti13_lms_token"));
        
        // LTI 1.1 security arrangement
        assertEquals("testkey", result.get("consumerkey"));
        assertEquals("testsecret", result.get("secret"));
        assertEquals("<test>xml</test>", result.get("xmlimport"));
        assertEquals("testtoken", result.get("lti13_auto_token"));
        assertEquals(2, result.get("lti13_auto_state"));
        assertEquals("{\"test\":\"reg\"}", result.get("lti13_auto_registration"));
        assertEquals("testchecksum", result.get("sakai_tool_checksum"));
        
        // Timestamps
        assertEquals(testDate, result.get("created_at"));
        assertEquals(testDate, result.get("updated_at"));
        
        // Live attributes
        assertEquals(Long.valueOf(99L), result.get("lti_content_count"));
        assertEquals(Long.valueOf(15L), result.get("lti_site_count"));
    }

    @Test
    public void testRoundTripConversion() {
        // Convert Map to Bean
        LtiToolBean originalTool = LtiToolBean.of(testMap);
        assertNotNull(originalTool);
        
        // Convert Bean back to Map
        Map<String, Object> convertedMap = originalTool.asMap();
        assertNotNull(convertedMap);
        
        // Verify all original values are preserved
        // All boolean fields in the map are Integer (1/0), so they should round-trip perfectly
        for (Map.Entry<String, Object> entry : testMap.entrySet()) {
            String key = entry.getKey();
            Object originalValue = entry.getValue();
            Object convertedValue = convertedMap.get(key);
            
            assertEquals("Round-trip conversion failed for key: " + key, originalValue, convertedValue);
        }
        
        // Verify no extra fields were added
        assertEquals("Converted map has different number of fields", testMap.size(), convertedMap.size());
    }

    @Test
    public void testRoundTripWithNullValues() {
        Map<String, Object> mapWithNulls = new HashMap<>();
        mapWithNulls.put("id", 999L);
        mapWithNulls.put("title", "Title Only");
        mapWithNulls.put("description", null);
        mapWithNulls.put("custom", null);
        mapWithNulls.put("frameheight", null);
        mapWithNulls.put("pl_launch", null);
        mapWithNulls.put("created_at", null);
        
        // Convert Map to Bean
        LtiToolBean tool = LtiToolBean.of(mapWithNulls);
        assertNotNull(tool);
        assertEquals(Long.valueOf(999L), tool.getId());
        assertEquals("Title Only", tool.getTitle());
        assertNull(tool.getDescription());
        assertNull(tool.getCustom());
        assertNull(tool.getFrameheight());
        assertNull(tool.getPlLaunch());
        assertNull(tool.getCreatedAt());
        
        // Convert Bean back to Map
        Map<String, Object> convertedMap = tool.asMap();
        assertNotNull(convertedMap);
        
        // Only non-null values should be in the converted map
        assertEquals(Long.valueOf(999L), convertedMap.get("id"));
        assertEquals("Title Only", convertedMap.get("title"));
        assertFalse(convertedMap.containsKey("description"));
        assertFalse(convertedMap.containsKey("custom"));
        assertFalse(convertedMap.containsKey("frameheight"));
        assertFalse(convertedMap.containsKey("pl_launch"));
        assertFalse(convertedMap.containsKey("created_at"));
    }

    @Test
    public void testTypeConversionRobustness() {
        Map<String, Object> mapWithVariousTypes = new HashMap<>();
        
        // Test different number types
        mapWithVariousTypes.put("id", Integer.valueOf(123)); // Integer instead of Long
        mapWithVariousTypes.put("deployment_id", "456"); // String instead of Long
        mapWithVariousTypes.put("frameheight", 600L); // Long instead of Integer
        mapWithVariousTypes.put("lti13_auto_state", "2"); // String instead of Integer
        
        // Test different boolean representations
        mapWithVariousTypes.put("pl_launch", "true"); // String instead of Boolean
        mapWithVariousTypes.put("sendname", Integer.valueOf(1)); // Integer instead of Boolean
        mapWithVariousTypes.put("allowoutcomes", "yes"); // String "yes" instead of Boolean
        
        // Test timestamp as Long
        mapWithVariousTypes.put("created_at", testDate.getTime()); // Long timestamp
        
        LtiToolBean tool = LtiToolBean.of(mapWithVariousTypes);
        assertNotNull(tool);
        
        // Verify type conversions worked
        assertEquals(Long.valueOf(123L), tool.getId());
        assertEquals(Long.valueOf(456L), tool.getDeploymentId());
        assertEquals(Integer.valueOf(600), tool.getFrameheight());
        assertEquals(Integer.valueOf(2), tool.getLti13AutoState());
        assertTrue(tool.getPlLaunch());
        assertTrue(tool.getSendname());
        assertTrue(tool.getAllowoutcomes());
        assertEquals(testDate, tool.getCreatedAt());
    }

    @Test
    public void testDatabaseNumberTypeRobustness() {
        Map<String, Object> mapWithDatabaseTypes = new HashMap<>();
        
        // Test all the weird number types that come from different databases
        mapWithDatabaseTypes.put("id", Double.valueOf(999.0)); // Double from Oracle
        mapWithDatabaseTypes.put("deployment_id", Float.valueOf(888.0f)); // Float from MySQL
        mapWithDatabaseTypes.put("frameheight", Short.valueOf((short)700)); // Short
        mapWithDatabaseTypes.put("lti13_auto_state", Byte.valueOf((byte)2)); // Byte
        
        LtiToolBean tool = LtiToolBean.of(mapWithDatabaseTypes);
        assertNotNull(tool);
        
        // Verify all number types converted correctly
        assertEquals("Double should convert to Long", Long.valueOf(999L), tool.getId());
        assertEquals("Float should convert to Long", Long.valueOf(888L), tool.getDeploymentId());
        assertEquals("Short should convert to Integer", Integer.valueOf(700), tool.getFrameheight());
        assertEquals("Byte should convert to Integer", Integer.valueOf(2), tool.getLti13AutoState());
    }

    @Test
    public void testStringNumberRobustness() {
        Map<String, Object> mapWithStringNumbers = new HashMap<>();
        
        // Test string representations of numbers (common from databases)
        mapWithStringNumbers.put("id", "777"); // String number
        mapWithStringNumbers.put("deployment_id", "-666"); // Negative string number
        mapWithStringNumbers.put("frameheight", "0"); // Zero string
        mapWithStringNumbers.put("lti13_auto_state", "3"); // Single digit string
        
        LtiToolBean tool = LtiToolBean.of(mapWithStringNumbers);
        assertNotNull(tool);
        
        // Verify string numbers converted correctly
        assertEquals("String '777' should convert to Long", Long.valueOf(777L), tool.getId());
        assertEquals("String '-666' should convert to Long", Long.valueOf(-666L), tool.getDeploymentId());
        assertEquals("String '0' should convert to Integer", Integer.valueOf(0), tool.getFrameheight());
        assertEquals("String '3' should convert to Integer", Integer.valueOf(3), tool.getLti13AutoState());
    }

    @Test
    public void testDecimalStringHandling() {
        Map<String, Object> mapWithDecimals = new HashMap<>();
        
        // Test decimal strings (should truncate like LTIUtil)
        mapWithDecimals.put("id", "555.0"); // Decimal string with .0
        mapWithDecimals.put("deployment_id", "444.7"); // Decimal string with decimal part
        mapWithDecimals.put("frameheight", "333.99"); // Decimal string for integer field
        mapWithDecimals.put("lti13_auto_state", "2.5"); // Decimal string for integer field
        
        LtiToolBean tool = LtiToolBean.of(mapWithDecimals);
        assertNotNull(tool);
        
        // Verify decimal strings are truncated correctly
        assertEquals("String '555.0' should truncate to 555", Long.valueOf(555L), tool.getId());
        assertEquals("String '444.7' should truncate to 444", Long.valueOf(444L), tool.getDeploymentId());
        assertEquals("String '333.99' should truncate to 333", Integer.valueOf(333), tool.getFrameheight());
        assertEquals("String '2.5' should truncate to 2", Integer.valueOf(2), tool.getLti13AutoState());
    }

    @Test
    public void testInvalidStringHandling() {
        Map<String, Object> mapWithInvalidStrings = new HashMap<>();
        
        // Test invalid string values (should return null)
        mapWithInvalidStrings.put("id", "invalid"); // Invalid string
        mapWithInvalidStrings.put("deployment_id", ""); // Empty string
        mapWithInvalidStrings.put("frameheight", "not-a-number"); // Non-numeric string
        mapWithInvalidStrings.put("lti13_auto_state", "abc123"); // Mixed string
        
        LtiToolBean tool = LtiToolBean.of(mapWithInvalidStrings);
        assertNotNull(tool);
        
        // Verify invalid strings return null
        assertNull("Invalid string 'invalid' should return null", tool.getId());
        assertNull("Empty string should return null", tool.getDeploymentId());
        assertNull("Non-numeric string should return null", tool.getFrameheight());
        assertNull("Mixed string should return null", tool.getLti13AutoState());
    }

    @Test
    public void testBigDecimalHandling() {
        Map<String, Object> mapWithBigDecimals = new HashMap<>();
        
        // Test BigDecimal (common from databases)
        java.math.BigDecimal bigDecimal111 = new java.math.BigDecimal("111.45");
        java.math.BigDecimal bigDecimal222 = new java.math.BigDecimal("222");
        java.math.BigDecimal bigDecimal333 = new java.math.BigDecimal("333.99");
        
        mapWithBigDecimals.put("id", bigDecimal111);
        mapWithBigDecimals.put("deployment_id", bigDecimal222);
        mapWithBigDecimals.put("frameheight", bigDecimal333);
        
        LtiToolBean tool = LtiToolBean.of(mapWithBigDecimals);
        assertNotNull(tool);
        
        // Verify BigDecimal conversion (should truncate decimals)
        assertEquals("BigDecimal 111.45 should truncate to 111", Long.valueOf(111L), tool.getId());
        assertEquals("BigDecimal 222 should convert to 222", Long.valueOf(222L), tool.getDeploymentId());
        assertEquals("BigDecimal 333.99 should truncate to 333", Integer.valueOf(333), tool.getFrameheight());
    }

    @Test
    public void testLiveAttributesConversion() {
        // Test with live attributes present
        Map<String, Object> mapWithLiveAttributes = new HashMap<>();
        mapWithLiveAttributes.put("id", 123L);
        mapWithLiveAttributes.put("title", "Test Tool");
        mapWithLiveAttributes.put("lti_content_count", 25L);
        mapWithLiveAttributes.put("lti_site_count", 3L);
        
        LtiToolBean tool = LtiToolBean.of(mapWithLiveAttributes);
        assertNotNull(tool);
        assertEquals(Long.valueOf(123L), tool.getId());
        assertEquals("Test Tool", tool.getTitle());
        assertEquals(Long.valueOf(25L), tool.getLtiContentCount());
        assertEquals(Long.valueOf(3L), tool.getLtiSiteCount());
        
        // Convert back to Map
        Map<String, Object> result = tool.asMap();
        assertEquals(Long.valueOf(25L), result.get("lti_content_count"));
        assertEquals(Long.valueOf(3L), result.get("lti_site_count"));
        
        // Test with live attributes absent (should be null)
        Map<String, Object> mapWithoutLiveAttributes = new HashMap<>();
        mapWithoutLiveAttributes.put("id", 456L);
        mapWithoutLiveAttributes.put("title", "Another Tool");
        // No lti_content_count or lti_site_count
        
        LtiToolBean tool2 = LtiToolBean.of(mapWithoutLiveAttributes);
        assertNotNull(tool2);
        assertEquals(Long.valueOf(456L), tool2.getId());
        assertEquals("Another Tool", tool2.getTitle());
        assertNull("ltiContentCount should be null when not present", tool2.getLtiContentCount());
        assertNull("ltiSiteCount should be null when not present", tool2.getLtiSiteCount());
        
        // Convert back to Map - live attributes should not be present
        Map<String, Object> result2 = tool2.asMap();
        assertFalse("lti_content_count should not be in map when null", result2.containsKey("lti_content_count"));
        assertFalse("lti_site_count should not be in map when null", result2.containsKey("lti_site_count"));
    }

    @Test
    public void testLiveAttributesTypeConversion() {
        Map<String, Object> mapWithVariousTypes = new HashMap<>();
        mapWithVariousTypes.put("id", 789L);
        mapWithVariousTypes.put("title", "Type Test Tool");
        
        // Test different number types for live attributes
        mapWithVariousTypes.put("lti_content_count", Integer.valueOf(100)); // Integer instead of Long
        mapWithVariousTypes.put("lti_site_count", "50"); // String instead of Long
        mapWithVariousTypes.put("lti_content_count", Double.valueOf(75.0)); // Double instead of Long
        
        LtiToolBean tool = LtiToolBean.of(mapWithVariousTypes);
        assertNotNull(tool);
        assertEquals(Long.valueOf(789L), tool.getId());
        assertEquals("Type Test Tool", tool.getTitle());
        assertEquals(Long.valueOf(75L), tool.getLtiContentCount()); // Should use the last value (Double)
        assertEquals(Long.valueOf(50L), tool.getLtiSiteCount()); // String should convert to Long
    }

    @Test
    public void testMissingBooleanFieldsStayNull() {
        // Test that missing boolean fields remain null in the bean
        // and do not appear in the map after conversion
        Map<String, Object> mapWithMissingBooleans = new HashMap<>();
        mapWithMissingBooleans.put("id", 999L);
        mapWithMissingBooleans.put("title", "Test Tool with Missing Booleans");
        
        // Deliberately omit all boolean fields to test null handling
        // No pl_launch, pl_linkselection, pl_contextlaunch, etc.
        // No sendname, sendemailaddr, pl_privacy
        // No allowoutcomes, allowlineitems, allowroster
        
        LtiToolBean tool = LtiToolBean.of(mapWithMissingBooleans);
        assertNotNull(tool);
        assertEquals(Long.valueOf(999L), tool.getId());
        assertEquals("Test Tool with Missing Booleans", tool.getTitle());
        
        // Verify all boolean fields are null in the bean
        assertNull("pl_launch should be null", tool.getPlLaunch());
        assertNull("pl_linkselection should be null", tool.getPlLinkselection());
        assertNull("pl_contextlaunch should be null", tool.getPlContextlaunch());
        assertNull("pl_lessonsselection should be null", tool.getPlLessonsselection());
        assertNull("pl_contenteditor should be null", tool.getPlContenteditor());
        assertNull("pl_assessmentselection should be null", tool.getPlAssessmentselection());
        assertNull("pl_coursenav should be null", tool.getPlCoursenav());
        assertNull("pl_importitem should be null", tool.getPlImportitem());
        assertNull("pl_fileitem should be null", tool.getPlFileitem());
        assertNull("sendname should be null", tool.getSendname());
        assertNull("sendemailaddr should be null", tool.getSendemailaddr());
        assertNull("pl_privacy should be null", tool.getPlPrivacy());
        assertNull("allowoutcomes should be null", tool.getAllowoutcomes());
        assertNull("allowlineitems should be null", tool.getAllowlineitems());
        assertNull("allowroster should be null", tool.getAllowroster());
        
        // Convert Bean back to Map
        Map<String, Object> convertedMap = tool.asMap();
        assertNotNull(convertedMap);
        
        // Verify null boolean fields do NOT appear in the converted map
        assertFalse("pl_launch should not be in map", convertedMap.containsKey("pl_launch"));
        assertFalse("pl_linkselection should not be in map", convertedMap.containsKey("pl_linkselection"));
        assertFalse("pl_contextlaunch should not be in map", convertedMap.containsKey("pl_contextlaunch"));
        assertFalse("pl_lessonsselection should not be in map", convertedMap.containsKey("pl_lessonsselection"));
        assertFalse("pl_contenteditor should not be in map", convertedMap.containsKey("pl_contenteditor"));
        assertFalse("pl_assessmentselection should not be in map", convertedMap.containsKey("pl_assessmentselection"));
        assertFalse("pl_coursenav should not be in map", convertedMap.containsKey("pl_coursenav"));
        assertFalse("pl_importitem should not be in map", convertedMap.containsKey("pl_importitem"));
        assertFalse("pl_fileitem should not be in map", convertedMap.containsKey("pl_fileitem"));
        assertFalse("sendname should not be in map", convertedMap.containsKey("sendname"));
        assertFalse("sendemailaddr should not be in map", convertedMap.containsKey("sendemailaddr"));
        assertFalse("pl_privacy should not be in map", convertedMap.containsKey("pl_privacy"));
        assertFalse("allowoutcomes should not be in map", convertedMap.containsKey("allowoutcomes"));
        assertFalse("allowlineitems should not be in map", convertedMap.containsKey("allowlineitems"));
        assertFalse("allowroster should not be in map", convertedMap.containsKey("allowroster"));
        
        // Verify only the fields we set are in the map
        assertEquals("Only id and title should be in map", 2, convertedMap.size());
        assertEquals(Long.valueOf(999L), convertedMap.get("id"));
        assertEquals("Test Tool with Missing Booleans", convertedMap.get("title"));
    }

    @Test
    public void testFromXmlNullInput() throws ParserConfigurationException {
        assertNull(LtiToolBean.fromXml(null));
    }

    @Test
    public void testFromXmlEmptyElement() throws ParserConfigurationException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element el = doc.createElement(LTIService.ARCHIVE_LTI_TOOL_TAG);
        LtiToolBean tool = LtiToolBean.fromXml(el);
        assertNotNull(tool);
        assertNull(tool.getId());
        assertNull(tool.getTitle());
    }

    @Test
    public void testFromXmlPopulatesArchivableFields() throws ParserConfigurationException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element el = doc.createElement(LTIService.ARCHIVE_LTI_TOOL_TAG);
        appendChild(el, "id", "42");
        appendChild(el, "title", "XML Tool Title");
        appendChild(el, "description", "XML description");
        appendChild(el, "launch", "https://xml.example.com/launch");
        appendChild(el, "frameheight", "600");
        appendChild(el, "newpage", "1");
        appendChild(el, "lti13", "1");

        LtiToolBean tool = LtiToolBean.fromXml(el);
        assertNotNull(tool);
        assertEquals(Long.valueOf(42L), tool.getId());
        assertEquals("XML Tool Title", tool.getTitle());
        assertEquals("XML description", tool.getDescription());
        assertEquals("https://xml.example.com/launch", tool.getLaunch());
        assertEquals(Integer.valueOf(600), tool.getFrameheight());
        assertEquals(Integer.valueOf(1), tool.getNewpage());
        assertEquals(Integer.valueOf(1), tool.getLti13());
    }

    @Test
    public void testToXmlAppendsToDocumentWhenStackEmpty() throws ParserConfigurationException {
        LtiToolBean tool = new LtiToolBean();
        tool.setId(99L);
        tool.setTitle("Stack Empty Tool");
        tool.setLaunch("https://empty.example/launch");

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Stack<Element> stack = new Stack<>();
        Element el = tool.toXml(doc, stack);

        assertNotNull(el);
        assertEquals(LTIService.ARCHIVE_LTI_TOOL_TAG, el.getTagName());
        assertNotNull(doc.getFirstChild());
        assertEquals(el, doc.getFirstChild());
        assertTrue(stack.isEmpty());
    }

    @Test
    public void testToXmlAppendsToStackPeekWhenStackNonEmpty() throws ParserConfigurationException {
        LtiToolBean tool = new LtiToolBean();
        tool.setId(88L);
        tool.setTitle("Stack Peek Tool");
        tool.setLaunch("https://peek.example/launch");

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);
        Stack<Element> stack = new Stack<>();
        stack.push(root);

        Element el = tool.toXml(doc, stack);

        assertNotNull(el);
        assertEquals(LTIService.ARCHIVE_LTI_TOOL_TAG, el.getTagName());
        assertEquals(el, root.getFirstChild());
        assertEquals(root, stack.peek());
    }

    @Test
    public void testToXmlWithNullStack() throws ParserConfigurationException {
        LtiToolBean tool = new LtiToolBean();
        tool.setId(77L);
        tool.setTitle("Null Stack Tool");

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element el = tool.toXml(doc, null);

        assertNotNull(el);
        assertEquals(LTIService.ARCHIVE_LTI_TOOL_TAG, el.getTagName());
        assertNotNull(doc.getFirstChild());
    }

    @Test
    public void testToXmlReturnsNullWhenDocumentNull() {
        LtiToolBean tool = new LtiToolBean();
        tool.setTitle("No Doc Tool");
        assertNull(tool.toXml(null, new Stack<>()));
    }

    @Test
    public void testToXmlFromXmlRoundTrip() throws ParserConfigurationException {
        LtiToolBean original = LtiToolBean.of(testMap);
        assertNotNull(original);

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element el = original.toXml(doc, new Stack<>());
        assertNotNull(el);

        LtiToolBean restored = LtiToolBean.fromXml(el);
        assertNotNull(restored);

        // Archivable fields should round-trip (only fields with archive=true)
        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getTitle(), restored.getTitle());
        assertEquals(original.getDescription(), restored.getDescription());
        assertEquals(original.getLaunch(), restored.getLaunch());
        assertEquals(original.getFrameheight(), restored.getFrameheight());
        assertEquals(original.getNewpage(), restored.getNewpage());
        assertEquals(original.getLti13(), restored.getLti13());
    }

    private static void appendChild(Element parent, String tagName, String textContent) {
        Document doc = parent.getOwnerDocument();
        Element child = doc.createElement(tagName);
        child.setTextContent(textContent);
        parent.appendChild(child);
    }
}
