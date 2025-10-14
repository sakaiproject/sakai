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

import org.sakaiproject.lti.beans.LtiToolSiteBean;

/**
 * Unit tests for LtiToolSiteBean Bean conversion methods.
 */
public class LtiToolSiteBeanTest {

    private Map<String, Object> testMap;
    private Date testDate;

    @Before
    public void setUp() {
        testDate = new Date();
        testMap = new HashMap<>();
        
        testMap.put("id", 123L);
        testMap.put("tool_id", 456L);
        testMap.put("SITE_ID", "site123");
        testMap.put("notes", "Test notes for this tool site");
        testMap.put("created_at", testDate);
        testMap.put("updated_at", testDate);
    }

    @Test
    public void testFromMapNullInput() {
        assertNull(LtiToolSiteBean.of(null));
    }

    @Test
    public void testFromMapEmptyMap() {
        Map<String, Object> emptyMap = new HashMap<>();
        LtiToolSiteBean toolSite = LtiToolSiteBean.of(emptyMap);
        assertNotNull(toolSite);
        assertNull(toolSite.getId());
        assertNull(toolSite.getToolId());
        assertNull(toolSite.getSiteId());
    }

    @Test
    public void testFromMapCompleteData() {
        LtiToolSiteBean toolSite = LtiToolSiteBean.of(testMap);
        
        assertNotNull(toolSite);
        assertEquals(Long.valueOf(123L), toolSite.getId());
        assertEquals(Long.valueOf(456L), toolSite.getToolId());
        assertEquals("site123", toolSite.getSiteId());
        assertEquals("Test notes for this tool site", toolSite.getNotes());
        assertEquals(testDate, toolSite.getCreatedAt());
        assertEquals(testDate, toolSite.getUpdatedAt());
    }

    @Test
    public void testToMapCompleteData() {
        LtiToolSiteBean toolSite = new LtiToolSiteBean();
        
        toolSite.setId(789L);
        toolSite.setToolId(101112L);
        toolSite.setSiteId("site456");
        toolSite.setNotes("Test toMap notes");
        toolSite.setCreatedAt(testDate);
        toolSite.setUpdatedAt(testDate);
        
        Map<String, Object> result = toolSite.asMap();
        
        assertNotNull(result);
        assertEquals(789L, result.get("id"));
        assertEquals(101112L, result.get("tool_id"));
        assertEquals("site456", result.get("SITE_ID"));
        assertEquals("Test toMap notes", result.get("notes"));
        assertEquals(testDate, result.get("created_at"));
        assertEquals(testDate, result.get("updated_at"));
    }

    @Test
    public void testRoundTripConversion() {
        // Convert Map to Bean
        LtiToolSiteBean originalToolSite = LtiToolSiteBean.of(testMap);
        assertNotNull(originalToolSite);
        
        // Convert Bean back to Map
        Map<String, Object> convertedMap = originalToolSite.asMap();
        assertNotNull(convertedMap);
        
        // Verify all original values are preserved
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
        mapWithNulls.put("tool_id", null);
        mapWithNulls.put("SITE_ID", "site999");
        mapWithNulls.put("notes", null);
        mapWithNulls.put("created_at", null);
        mapWithNulls.put("updated_at", testDate);
        
        // Convert Map to Bean
        LtiToolSiteBean toolSite = LtiToolSiteBean.of(mapWithNulls);
        assertNotNull(toolSite);
        assertEquals(Long.valueOf(999L), toolSite.getId());
        assertNull(toolSite.getToolId());
        assertEquals("site999", toolSite.getSiteId());
        assertNull(toolSite.getNotes());
        assertNull(toolSite.getCreatedAt());
        assertEquals(testDate, toolSite.getUpdatedAt());
        
        // Convert Bean back to Map
        Map<String, Object> convertedMap = toolSite.asMap();
        assertNotNull(convertedMap);
        
        // Only non-null values should be in the converted map
        assertEquals(Long.valueOf(999L), convertedMap.get("id"));
        assertFalse(convertedMap.containsKey("tool_id"));
        assertEquals("site999", convertedMap.get("SITE_ID"));
        assertFalse(convertedMap.containsKey("notes"));
        assertFalse(convertedMap.containsKey("created_at"));
        assertEquals(testDate, convertedMap.get("updated_at"));
    }

    @Test
    public void testTypeConversionRobustness() {
        Map<String, Object> mapWithVariousTypes = new HashMap<>();
        
        // Test different number types
        mapWithVariousTypes.put("id", 123); // Integer instead of Long
        mapWithVariousTypes.put("tool_id", "456"); // String instead of Long
        
        // Test timestamp as Long
        mapWithVariousTypes.put("created_at", testDate.getTime()); // Long timestamp
        
        LtiToolSiteBean toolSite = LtiToolSiteBean.of(mapWithVariousTypes);
        assertNotNull(toolSite);
        
        // Verify type conversions worked
        assertEquals(Long.valueOf(123L), toolSite.getId());
        assertEquals(Long.valueOf(456L), toolSite.getToolId());
        assertEquals(testDate, toolSite.getCreatedAt());
    }

    @Test
    public void testMinimalData() {
        Map<String, Object> minimalMap = new HashMap<>();
        minimalMap.put("id", 1L);
        minimalMap.put("SITE_ID", "minimal-site");
        
        LtiToolSiteBean toolSite = LtiToolSiteBean.of(minimalMap);
        assertNotNull(toolSite);
        assertEquals(Long.valueOf(1L), toolSite.getId());
        assertNull(toolSite.getToolId());
        assertEquals("minimal-site", toolSite.getSiteId());
        assertNull(toolSite.getNotes());
        assertNull(toolSite.getCreatedAt());
        assertNull(toolSite.getUpdatedAt());
        
        Map<String, Object> result = toolSite.asMap();
        assertEquals(Long.valueOf(1L), result.get("id"));
        assertEquals("minimal-site", result.get("SITE_ID"));
        assertFalse(result.containsKey("tool_id"));
        assertFalse(result.containsKey("notes"));
        assertFalse(result.containsKey("created_at"));
        assertFalse(result.containsKey("updated_at"));
    }

    @Test
    public void testDatabaseNumberTypeRobustness() {
        Map<String, Object> mapWithDatabaseTypes = new HashMap<>();
        
        // Test all the weird number types that come from different databases
        mapWithDatabaseTypes.put("id", Double.valueOf(999.0)); // Double from Oracle
        mapWithDatabaseTypes.put("tool_id", Float.valueOf(888.0f)); // Float from MySQL
        mapWithDatabaseTypes.put("created_at", testDate);
        
        LtiToolSiteBean toolSite = LtiToolSiteBean.of(mapWithDatabaseTypes);
        assertNotNull(toolSite);
        
        // Verify all number types converted correctly
        assertEquals("Double should convert to Long", Long.valueOf(999L), toolSite.getId());
        assertEquals("Float should convert to Long", Long.valueOf(888L), toolSite.getToolId());
        assertEquals("Date should remain Date", testDate, toolSite.getCreatedAt());
    }

    @Test
    public void testStringNumberRobustness() {
        Map<String, Object> mapWithStringNumbers = new HashMap<>();
        
        // Test string representations of numbers (common from databases)
        mapWithStringNumbers.put("id", "777"); // String number
        mapWithStringNumbers.put("tool_id", "-666"); // Negative string number
        
        LtiToolSiteBean toolSite = LtiToolSiteBean.of(mapWithStringNumbers);
        assertNotNull(toolSite);
        
        // Verify string numbers converted correctly
        assertEquals("String '777' should convert to Long", Long.valueOf(777L), toolSite.getId());
        assertEquals("String '-666' should convert to Long", Long.valueOf(-666L), toolSite.getToolId());
    }

    @Test
    public void testDecimalStringHandling() {
        Map<String, Object> mapWithDecimals = new HashMap<>();
        
        // Test decimal strings (should truncate like LTIUtil)
        mapWithDecimals.put("id", "555.0"); // Decimal string with .0
        mapWithDecimals.put("tool_id", "444.7"); // Decimal string with decimal part
        
        LtiToolSiteBean toolSite = LtiToolSiteBean.of(mapWithDecimals);
        assertNotNull(toolSite);
        
        // Verify decimal strings are truncated correctly
        assertEquals("String '555.0' should truncate to 555", Long.valueOf(555L), toolSite.getId());
        assertEquals("String '444.7' should truncate to 444", Long.valueOf(444L), toolSite.getToolId());
    }

    @Test
    public void testInvalidStringHandling() {
        Map<String, Object> mapWithInvalidStrings = new HashMap<>();
        
        // Test invalid string values (should return null)
        mapWithInvalidStrings.put("id", "invalid"); // Invalid string
        mapWithInvalidStrings.put("tool_id", ""); // Empty string
        
        LtiToolSiteBean toolSite = LtiToolSiteBean.of(mapWithInvalidStrings);
        assertNotNull(toolSite);
        
        // Verify invalid strings return null
        assertNull("Invalid string 'invalid' should return null", toolSite.getId());
        assertNull("Empty string should return null", toolSite.getToolId());
    }

    @Test
    public void testBigDecimalHandling() {
        Map<String, Object> mapWithBigDecimals = new HashMap<>();
        
        // Test BigDecimal (common from databases)
        java.math.BigDecimal bigDecimal111 = new java.math.BigDecimal("111.45");
        java.math.BigDecimal bigDecimal222 = new java.math.BigDecimal("222");
        
        mapWithBigDecimals.put("id", bigDecimal111);
        mapWithBigDecimals.put("tool_id", bigDecimal222);
        
        LtiToolSiteBean toolSite = LtiToolSiteBean.of(mapWithBigDecimals);
        assertNotNull(toolSite);
        
        // Verify BigDecimal conversion (should truncate decimals)
        assertEquals("BigDecimal 111.45 should truncate to 111", Long.valueOf(111L), toolSite.getId());
        assertEquals("BigDecimal 222 should convert to 222", Long.valueOf(222L), toolSite.getToolId());
    }
}
