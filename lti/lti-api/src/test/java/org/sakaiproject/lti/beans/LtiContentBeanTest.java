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
import org.sakaiproject.lti.beans.LtiContentBean;

/**
 * Unit tests for LtiContentBean Bean conversion methods.
 */
public class LtiContentBeanTest {

    private Map<String, Object> testMap;
    private Date testDate;

    @Before
    public void setUp() {
        testDate = new Date();
        testMap = new HashMap<>();
        
        // Core fields
        testMap.put("id", 123L);
        testMap.put("tool_id", 456L);
        testMap.put("SITE_ID", "site123");
        testMap.put("title", "Test Content Title");
        testMap.put("description", "Test content description");
        testMap.put("frameheight", 600);
        testMap.put("newpage", true);
        testMap.put("protect", false);
        testMap.put("debug", true);
        
        // LTI fields
        testMap.put("custom", "custom1=value1\ncustom2=value2");
        testMap.put("launch", "https://example.com/launch");
        testMap.put("xmlimport", "<xml>test</xml>");
        testMap.put("settings", "{\"setting1\":\"value1\"}");
        testMap.put("contentitem", "{\"lineitem\":\"data\"}");
        testMap.put("placement", "placement123");
        testMap.put("placementsecret", "secret123");
        testMap.put("oldplacementsecret", "oldsecret123");
        
        // LTI 1.3 fields
        
        // Timestamps
        testMap.put("created_at", testDate);
        testMap.put("updated_at", testDate);
        
        // Extra fields from joins
        testMap.put("SITE_TITLE", "Test Site");
        testMap.put("SITE_CONTACT_NAME", "John Doe");
        testMap.put("SITE_CONTACT_EMAIL", "john@example.com");
        testMap.put("ATTRIBUTION", "Department of Testing");
        testMap.put("URL", "https://example.com/tool");
        testMap.put("searchURL", "https://example.com/search");
    }

    @Test
    public void testFromMapNullInput() {
        assertNull(LtiContentBean.of(null));
    }

    @Test
    public void testFromMapEmptyMap() {
        Map<String, Object> emptyMap = new HashMap<>();
        LtiContentBean content = LtiContentBean.of(emptyMap);
        assertNotNull(content);
        assertNull(content.getId());
        assertNull(content.getTitle());
    }

    @Test
    public void testFromMapCompleteData() {
        LtiContentBean content = LtiContentBean.of(testMap);
        
        assertNotNull(content);
        
        // Core fields
        assertEquals(Long.valueOf(123L), content.getId());
        assertEquals(Long.valueOf(456L), content.getToolId());
        assertEquals("site123", content.getSiteId());
        assertEquals("Test Content Title", content.getTitle());
        assertEquals("Test content description", content.getDescription());
        assertEquals(Integer.valueOf(600), content.getFrameheight());
        assertTrue(content.getNewpage());
        assertFalse(content.getProtect());
        assertTrue(content.getDebug());
        
        // LTI fields
        assertEquals("custom1=value1\ncustom2=value2", content.getCustom());
        assertEquals("https://example.com/launch", content.getLaunch());
        assertEquals("<xml>test</xml>", content.getXmlimport());
        assertEquals("{\"setting1\":\"value1\"}", content.getSettings());
        assertEquals("{\"lineitem\":\"data\"}", content.getContentitem());
        assertEquals("placement123", content.getPlacement());
        assertEquals("secret123", content.getPlacementsecret());
        assertEquals("oldsecret123", content.getOldplacementsecret());
        
        // LTI 1.3 fields
        
        // Timestamps
        assertEquals(testDate, content.getCreatedAt());
        assertEquals(testDate, content.getUpdatedAt());
        
        // Extra fields
        assertEquals("Test Site", content.getSiteTitle());
        assertEquals("John Doe", content.getSiteContactName());
        assertEquals("john@example.com", content.getSiteContactEmail());
        assertEquals("Department of Testing", content.getAttribution());
        assertEquals("https://example.com/tool", content.getUrl());
        assertEquals("https://example.com/search", content.getSearchUrl());
    }

    @Test
    public void testToMapCompleteData() {
        LtiContentBean content = new LtiContentBean();
        
        // Set all fields
        content.setId(789L);
        content.setToolId(101112L);
        content.setSiteId("site456");
        content.setTitle("Test ToMap Title");
        content.setDescription("Test toMap description");
        content.setFrameheight(800);
        content.setNewpage(false);
        content.setProtect(true);
        content.setDebug(false);
        
        content.setCustom("custom3=value3");
        content.setLaunch("https://test.com/launch");
        content.setXmlimport("<xml>test2</xml>");
        content.setSettings("{\"setting2\":\"value2\"}");
        content.setContentitem("{\"lineitem2\":\"data2\"}");
        content.setPlacement("placement456");
        content.setPlacementsecret("secret456");
        content.setOldplacementsecret("oldsecret456");
        
        
        content.setCreatedAt(testDate);
        content.setUpdatedAt(testDate);
        
        content.setSiteTitle("Test Site 2");
        content.setSiteContactName("Jane Smith");
        content.setSiteContactEmail("jane@test.com");
        content.setAttribution("Department of Testing 2");
        content.setUrl("https://test.com/tool");
        content.setSearchUrl("https://test.com/search");
        
        Map<String, Object> result = content.asMap();
        
        assertNotNull(result);
        
        // Core fields
        assertEquals(789L, result.get("id"));
        assertEquals(101112L, result.get("tool_id"));
        assertEquals("site456", result.get("SITE_ID"));
        assertEquals("Test ToMap Title", result.get("title"));
        assertEquals("Test toMap description", result.get("description"));
        assertEquals(800, result.get("frameheight"));
        assertEquals(Integer.valueOf(0), result.get("newpage"));
        assertEquals(Integer.valueOf(1), result.get("protect"));
        assertEquals(Integer.valueOf(0), result.get("debug"));
        
        // LTI fields
        assertEquals("custom3=value3", result.get("custom"));
        assertEquals("https://test.com/launch", result.get("launch"));
        assertEquals("<xml>test2</xml>", result.get("xmlimport"));
        assertEquals("{\"setting2\":\"value2\"}", result.get("settings"));
        assertEquals("{\"lineitem2\":\"data2\"}", result.get("contentitem"));
        assertEquals("placement456", result.get("placement"));
        assertEquals("secret456", result.get("placementsecret"));
        assertEquals("oldsecret456", result.get("oldplacementsecret"));
        
        // LTI 1.3 fields
        
        // Timestamps
        assertEquals(testDate, result.get("created_at"));
        assertEquals(testDate, result.get("updated_at"));
        
        // Extra fields
        assertEquals("Test Site 2", result.get("SITE_TITLE"));
        assertEquals("Jane Smith", result.get("SITE_CONTACT_NAME"));
        assertEquals("jane@test.com", result.get("SITE_CONTACT_EMAIL"));
        assertEquals("Department of Testing 2", result.get("ATTRIBUTION"));
        assertEquals("https://test.com/tool", result.get("URL"));
        assertEquals("https://test.com/search", result.get("searchURL"));
    }

    @Test
    public void testRoundTripConversion() {
        // Convert Map to Bean
        LtiContentBean originalContent = LtiContentBean.of(testMap);
        assertNotNull(originalContent);
        
        // Convert Bean back to Map
        Map<String, Object> convertedMap = originalContent.asMap();
        assertNotNull(convertedMap);
        
        // Verify all original values are preserved (with Boolean-to-Integer conversion)
        for (Map.Entry<String, Object> entry : testMap.entrySet()) {
            String key = entry.getKey();
            Object originalValue = entry.getValue();
            Object convertedValue = convertedMap.get(key);
            
            // Handle Boolean-to-Integer conversion for Boolean fields
            if (key.equals("newpage") || key.equals("protect") || key.equals("debug")) {
                if (originalValue instanceof Boolean) {
                    Boolean boolValue = (Boolean) originalValue;
                    Integer expectedValue = boolValue ? 1 : 0;
                    assertEquals("Round-trip conversion failed for Boolean key: " + key, expectedValue, convertedValue);
                } else {
                    assertEquals("Round-trip conversion failed for key: " + key, originalValue, convertedValue);
                }
            } else {
                assertEquals("Round-trip conversion failed for key: " + key, originalValue, convertedValue);
            }
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
        mapWithNulls.put("newpage", null);
        mapWithNulls.put("created_at", null);
        
        // Convert Map to Bean
        LtiContentBean content = LtiContentBean.of(mapWithNulls);
        assertNotNull(content);
        assertEquals(Long.valueOf(999L), content.getId());
        assertEquals("Title Only", content.getTitle());
        assertNull(content.getDescription());
        assertNull(content.getCustom());
        assertNull(content.getFrameheight());
        assertNull(content.getNewpage());
        assertNull(content.getCreatedAt());
        
        // Convert Bean back to Map
        Map<String, Object> convertedMap = content.asMap();
        assertNotNull(convertedMap);
        
        // Only non-null values should be in the converted map
        assertEquals(Long.valueOf(999L), convertedMap.get("id"));
        assertEquals("Title Only", convertedMap.get("title"));
        assertFalse(convertedMap.containsKey("description"));
        assertFalse(convertedMap.containsKey("custom"));
        assertFalse(convertedMap.containsKey("frameheight"));
        assertFalse(convertedMap.containsKey("newpage"));
        assertFalse(convertedMap.containsKey("created_at"));
    }

    @Test
    public void testTypeConversionRobustness() {
        Map<String, Object> mapWithVariousTypes = new HashMap<>();
        
        // Test different number types
        mapWithVariousTypes.put("id", 123); // Integer instead of Long
        mapWithVariousTypes.put("tool_id", "456"); // String instead of Long
        mapWithVariousTypes.put("frameheight", 600L); // Long instead of Integer
        
        // Test different boolean representations
        mapWithVariousTypes.put("newpage", "true"); // String instead of Boolean
        mapWithVariousTypes.put("protect", 1); // Integer instead of Boolean
        mapWithVariousTypes.put("debug", "yes"); // String "yes" instead of Boolean
        
        // Test timestamp as Long
        mapWithVariousTypes.put("created_at", testDate.getTime()); // Long timestamp
        
        LtiContentBean content = LtiContentBean.of(mapWithVariousTypes);
        assertNotNull(content);
        
        // Verify type conversions worked
        assertEquals(Long.valueOf(123L), content.getId());
        assertEquals(Long.valueOf(456L), content.getToolId());
        assertEquals(Integer.valueOf(600), content.getFrameheight());
        assertTrue(content.getNewpage());
        assertTrue(content.getProtect());
        assertTrue(content.getDebug());
        assertEquals(testDate, content.getCreatedAt());
    }

    @Test
    public void testDatabaseNumberTypeRobustness() {
        Map<String, Object> mapWithDatabaseTypes = new HashMap<>();
        
        // Test all the weird number types that come from different databases
        mapWithDatabaseTypes.put("id", Double.valueOf(123.0)); // Double from Oracle
        mapWithDatabaseTypes.put("tool_id", Float.valueOf(456.0f)); // Float from MySQL
        mapWithDatabaseTypes.put("frameheight", Short.valueOf((short)800)); // Short
        mapWithDatabaseTypes.put("created_at", testDate);
        
        LtiContentBean content = LtiContentBean.of(mapWithDatabaseTypes);
        assertNotNull(content);
        
        // Verify all number types converted correctly
        assertEquals("Double should convert to Long", Long.valueOf(123L), content.getId());
        assertEquals("Float should convert to Long", Long.valueOf(456L), content.getToolId());
        assertEquals("Short should convert to Integer", Integer.valueOf(800), content.getFrameheight());
        assertEquals("Date should remain Date", testDate, content.getCreatedAt());
    }

    @Test
    public void testStringNumberRobustness() {
        Map<String, Object> mapWithStringNumbers = new HashMap<>();
        
        // Test string representations of numbers (common from databases)
        mapWithStringNumbers.put("id", "123"); // String number
        mapWithStringNumbers.put("tool_id", "-456"); // Negative string number
        mapWithStringNumbers.put("frameheight", "0"); // Zero string
        
        LtiContentBean content = LtiContentBean.of(mapWithStringNumbers);
        assertNotNull(content);
        
        // Verify string numbers converted correctly
        assertEquals("String '123' should convert to Long", Long.valueOf(123L), content.getId());
        assertEquals("String '-456' should convert to Long", Long.valueOf(-456L), content.getToolId());
        assertEquals("String '0' should convert to Integer", Integer.valueOf(0), content.getFrameheight());
    }

    @Test
    public void testDecimalStringHandling() {
        Map<String, Object> mapWithDecimals = new HashMap<>();
        
        // Test decimal strings (should truncate like LTIUtil)
        mapWithDecimals.put("id", "123.0"); // Decimal string with .0
        mapWithDecimals.put("tool_id", "456.7"); // Decimal string with decimal part
        mapWithDecimals.put("frameheight", "800.99"); // Decimal string for integer field
        
        LtiContentBean content = LtiContentBean.of(mapWithDecimals);
        assertNotNull(content);
        
        // Verify decimal strings are truncated correctly
        assertEquals("String '123.0' should truncate to 123", Long.valueOf(123L), content.getId());
        assertEquals("String '456.7' should truncate to 456", Long.valueOf(456L), content.getToolId());
        assertEquals("String '800.99' should truncate to 800", Integer.valueOf(800), content.getFrameheight());
    }

    @Test
    public void testInvalidStringHandling() {
        Map<String, Object> mapWithInvalidStrings = new HashMap<>();
        
        // Test invalid string values (should return null)
        mapWithInvalidStrings.put("id", "invalid"); // Invalid string
        mapWithInvalidStrings.put("tool_id", ""); // Empty string
        mapWithInvalidStrings.put("frameheight", "not-a-number"); // Non-numeric string
        
        LtiContentBean content = LtiContentBean.of(mapWithInvalidStrings);
        assertNotNull(content);
        
        // Verify invalid strings return null
        assertNull("Invalid string 'invalid' should return null", content.getId());
        assertNull("Empty string should return null", content.getToolId());
        assertNull("Non-numeric string should return null", content.getFrameheight());
    }

    @Test
    public void testNullAndEmptyHandling() {
        Map<String, Object> mapWithNulls = new HashMap<>();
        
        // Test null values
        mapWithNulls.put("id", null);
        mapWithNulls.put("tool_id", null);
        mapWithNulls.put("frameheight", null);
        
        LtiContentBean content = LtiContentBean.of(mapWithNulls);
        assertNotNull(content);
        
        // Verify null values return null
        assertNull("Null id should return null", content.getId());
        assertNull("Null tool_id should return null", content.getToolId());
        assertNull("Null frameheight should return null", content.getFrameheight());
    }

    @Test
    public void testBigDecimalHandling() {
        Map<String, Object> mapWithBigDecimals = new HashMap<>();
        
        // Test BigDecimal (common from databases)
        java.math.BigDecimal bigDecimal123 = new java.math.BigDecimal("123.45");
        java.math.BigDecimal bigDecimal456 = new java.math.BigDecimal("456");
        java.math.BigDecimal bigDecimal800 = new java.math.BigDecimal("800.99");
        
        mapWithBigDecimals.put("id", bigDecimal123);
        mapWithBigDecimals.put("tool_id", bigDecimal456);
        mapWithBigDecimals.put("frameheight", bigDecimal800);
        
        LtiContentBean content = LtiContentBean.of(mapWithBigDecimals);
        assertNotNull(content);
        
        // Verify BigDecimal conversion (should truncate decimals)
        assertEquals("BigDecimal 123.45 should truncate to 123", Long.valueOf(123L), content.getId());
        assertEquals("BigDecimal 456 should convert to 456", Long.valueOf(456L), content.getToolId());
        assertEquals("BigDecimal 800.99 should truncate to 800", Integer.valueOf(800), content.getFrameheight());
    }

    @Test
    public void testLargeNumberHandling() {
        Map<String, Object> mapWithLargeNumbers = new HashMap<>();
        
        // Test large numbers that might come from databases
        mapWithLargeNumbers.put("id", Long.MAX_VALUE); // Maximum long value
        mapWithLargeNumbers.put("tool_id", Integer.MAX_VALUE); // Maximum integer value
        mapWithLargeNumbers.put("frameheight", Short.MAX_VALUE); // Maximum short value
        
        LtiContentBean content = LtiContentBean.of(mapWithLargeNumbers);
        assertNotNull(content);
        
        // Verify large numbers handled correctly
        assertEquals("Long.MAX_VALUE should remain Long.MAX_VALUE", Long.valueOf(Long.MAX_VALUE), content.getId());
        assertEquals("Integer.MAX_VALUE should convert to Long", Long.valueOf(Integer.MAX_VALUE), content.getToolId());
        assertEquals("Short.MAX_VALUE should convert to Integer", Integer.valueOf(Short.MAX_VALUE), content.getFrameheight());
    }

    @Test
    public void testFromXmlNullInput() throws ParserConfigurationException {
        assertNull(LtiContentBean.fromXml(null));
    }

    @Test
    public void testFromXmlEmptyElement() throws ParserConfigurationException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element el = doc.createElement(LTIService.ARCHIVE_LTI_CONTENT_TAG);
        LtiContentBean content = LtiContentBean.fromXml(el);
        assertNotNull(content);
        assertNull(content.getId());
        assertNull(content.getTitle());
    }

    @Test
    public void testFromXmlPopulatesArchivableFields() throws ParserConfigurationException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element el = doc.createElement(LTIService.ARCHIVE_LTI_CONTENT_TAG);
        appendChild(el, "id", "43");
        appendChild(el, "title", "XML Content Title");
        appendChild(el, "description", "XML content description");
        appendChild(el, "launch", "https://xml.example.com/content/launch");
        appendChild(el, "frameheight", "500");
        appendChild(el, "custom", "key=value");
        appendChild(el, "newpage", "1");

        LtiContentBean content = LtiContentBean.fromXml(el);
        assertNotNull(content);
        assertEquals(Long.valueOf(43L), content.getId());
        assertEquals("XML Content Title", content.getTitle());
        assertEquals("XML content description", content.getDescription());
        assertEquals("https://xml.example.com/content/launch", content.getLaunch());
        assertEquals(Integer.valueOf(500), content.getFrameheight());
        assertEquals("key=value", content.getCustom());
        assertTrue(content.getNewpage());
    }

    @Test
    public void testToXmlAppendsToDocumentWhenStackEmpty() throws ParserConfigurationException {
        LtiContentBean content = new LtiContentBean();
        content.setId(199L);
        content.setTitle("Stack Empty Content");
        content.setLaunch("https://empty.example/content");

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Stack<Element> stack = new Stack<>();
        Element el = content.toXml(doc, stack);

        assertNotNull(el);
        assertEquals(LTIService.ARCHIVE_LTI_CONTENT_TAG, el.getTagName());
        assertNotNull(doc.getFirstChild());
        assertEquals(el, doc.getFirstChild());
        assertTrue(stack.isEmpty());
    }

    @Test
    public void testToXmlAppendsToStackPeekWhenStackNonEmpty() throws ParserConfigurationException {
        LtiContentBean content = new LtiContentBean();
        content.setId(188L);
        content.setTitle("Stack Peek Content");
        content.setLaunch("https://peek.example/content");

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);
        Stack<Element> stack = new Stack<>();
        stack.push(root);

        Element el = content.toXml(doc, stack);

        assertNotNull(el);
        assertEquals(LTIService.ARCHIVE_LTI_CONTENT_TAG, el.getTagName());
        assertEquals(el, root.getFirstChild());
        assertEquals(root, stack.peek());
    }

    @Test
    public void testToXmlWithNullStack() throws ParserConfigurationException {
        LtiContentBean content = new LtiContentBean();
        content.setId(177L);
        content.setTitle("Null Stack Content");

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element el = content.toXml(doc, null);

        assertNotNull(el);
        assertEquals(LTIService.ARCHIVE_LTI_CONTENT_TAG, el.getTagName());
        assertNotNull(doc.getFirstChild());
    }

    @Test
    public void testToXmlReturnsNullWhenDocumentNull() {
        LtiContentBean content = new LtiContentBean();
        content.setTitle("No Doc Content");
        assertNull(content.toXml(null, new Stack<>()));
    }

    @Test
    public void testToXmlFromXmlRoundTrip() throws ParserConfigurationException {
        LtiContentBean original = LtiContentBean.of(testMap);
        assertNotNull(original);

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element el = original.toXml(doc, new Stack<>());
        assertNotNull(el);

        LtiContentBean restored = LtiContentBean.fromXml(el);
        assertNotNull(restored);

        // Archivable fields should round-trip (only fields with archive=true)
        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getTitle(), restored.getTitle());
        assertEquals(original.getDescription(), restored.getDescription());
        assertEquals(original.getLaunch(), restored.getLaunch());
        assertEquals(original.getFrameheight(), restored.getFrameheight());
        assertEquals(original.getCustom(), restored.getCustom());
        assertEquals(original.getNewpage(), restored.getNewpage());
    }

    private static void appendChild(Element parent, String tagName, String textContent) {
        Document doc = parent.getOwnerDocument();
        Element child = doc.createElement(tagName);
        child.setTextContent(textContent);
        parent.appendChild(child);
    }
}
