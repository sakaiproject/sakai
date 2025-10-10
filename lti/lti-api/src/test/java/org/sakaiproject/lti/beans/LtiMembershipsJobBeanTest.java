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

import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.lti.beans.LtiMembershipsJobBean;

/**
 * Unit tests for LtiMembershipsJobBean Bean conversion methods.
 */
public class LtiMembershipsJobBeanTest {

    private Map<String, Object> testMap;

    @Before
    public void setUp() {
        testMap = new HashMap<>();
        
        testMap.put("SITE_ID", "site123");
        testMap.put("memberships_id", "membership456");
        testMap.put("memberships_url", "https://example.com/memberships");
        testMap.put("consumerkey", "consumer789");
        testMap.put("lti_version", "LTI-1p0");
    }

    @Test
    public void testFromMapNullInput() {
        assertNull(LtiMembershipsJobBean.of(null));
    }

    @Test
    public void testFromMapEmptyMap() {
        Map<String, Object> emptyMap = new HashMap<>();
        LtiMembershipsJobBean job = LtiMembershipsJobBean.of(emptyMap);
        assertNotNull(job);
        assertNull(job.getSiteId());
        assertNull(job.getMembershipsId());
        assertNull(job.getMembershipsUrl());
        assertNull(job.getConsumerkey());
        assertNull(job.getLtiVersion());
    }

    @Test
    public void testFromMapCompleteData() {
        LtiMembershipsJobBean job = LtiMembershipsJobBean.of(testMap);
        
        assertNotNull(job);
        assertEquals("site123", job.getSiteId());
        assertEquals("membership456", job.getMembershipsId());
        assertEquals("https://example.com/memberships", job.getMembershipsUrl());
        assertEquals("consumer789", job.getConsumerkey());
        assertEquals("LTI-1p0", job.getLtiVersion());
    }

    @Test
    public void testToMapCompleteData() {
        LtiMembershipsJobBean job = new LtiMembershipsJobBean();
        
        job.setSiteId("site456");
        job.setMembershipsId("membership789");
        job.setMembershipsUrl("https://test.com/memberships");
        job.setConsumerkey("consumer123");
        job.setLtiVersion("LTI-1p1");
        
        Map<String, Object> result = job.asMap();
        
        assertNotNull(result);
        assertEquals("site456", result.get("SITE_ID"));
        assertEquals("membership789", result.get("memberships_id"));
        assertEquals("https://test.com/memberships", result.get("memberships_url"));
        assertEquals("consumer123", result.get("consumerkey"));
        assertEquals("LTI-1p1", result.get("lti_version"));
    }

    @Test
    public void testRoundTripConversion() {
        // Convert Map to Bean
        LtiMembershipsJobBean originalJob = LtiMembershipsJobBean.of(testMap);
        assertNotNull(originalJob);
        
        // Convert Bean back to Map
        Map<String, Object> convertedMap = originalJob.asMap();
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
        mapWithNulls.put("SITE_ID", "site999");
        mapWithNulls.put("memberships_id", null);
        mapWithNulls.put("memberships_url", "https://example.com/url");
        mapWithNulls.put("consumerkey", null);
        mapWithNulls.put("lti_version", "LTI-1p0");
        
        // Convert Map to Bean
        LtiMembershipsJobBean job = LtiMembershipsJobBean.of(mapWithNulls);
        assertNotNull(job);
        assertEquals("site999", job.getSiteId());
        assertNull(job.getMembershipsId());
        assertEquals("https://example.com/url", job.getMembershipsUrl());
        assertNull(job.getConsumerkey());
        assertEquals("LTI-1p0", job.getLtiVersion());
        
        // Convert Bean back to Map
        Map<String, Object> convertedMap = job.asMap();
        assertNotNull(convertedMap);
        
        // Only non-null values should be in the converted map
        assertEquals("site999", convertedMap.get("SITE_ID"));
        assertFalse(convertedMap.containsKey("memberships_id"));
        assertEquals("https://example.com/url", convertedMap.get("memberships_url"));
        assertFalse(convertedMap.containsKey("consumerkey"));
        assertEquals("LTI-1p0", convertedMap.get("lti_version"));
    }

    @Test
    public void testMinimalData() {
        Map<String, Object> minimalMap = new HashMap<>();
        minimalMap.put("SITE_ID", "minimal-site");
        minimalMap.put("lti_version", "LTI-1p0");
        
        LtiMembershipsJobBean job = LtiMembershipsJobBean.of(minimalMap);
        assertNotNull(job);
        assertEquals("minimal-site", job.getSiteId());
        assertNull(job.getMembershipsId());
        assertNull(job.getMembershipsUrl());
        assertNull(job.getConsumerkey());
        assertEquals("LTI-1p0", job.getLtiVersion());
        
        Map<String, Object> result = job.asMap();
        assertEquals("minimal-site", result.get("SITE_ID"));
        assertEquals("LTI-1p0", result.get("lti_version"));
        assertFalse(result.containsKey("memberships_id"));
        assertFalse(result.containsKey("memberships_url"));
        assertFalse(result.containsKey("consumerkey"));
    }

    @Test
    public void testAllNullValues() {
        Map<String, Object> allNullMap = new HashMap<>();
        allNullMap.put("SITE_ID", null);
        allNullMap.put("memberships_id", null);
        allNullMap.put("memberships_url", null);
        allNullMap.put("consumerkey", null);
        allNullMap.put("lti_version", null);
        
        LtiMembershipsJobBean job = LtiMembershipsJobBean.of(allNullMap);
        assertNotNull(job);
        assertNull(job.getSiteId());
        assertNull(job.getMembershipsId());
        assertNull(job.getMembershipsUrl());
        assertNull(job.getConsumerkey());
        assertNull(job.getLtiVersion());
        
        Map<String, Object> result = job.asMap();
        assertTrue("Map should be empty when all values are null", result.isEmpty());
    }

    @Test
    public void testLongUrls() {
        Map<String, Object> longUrlMap = new HashMap<>();
        longUrlMap.put("SITE_ID", "site123");
        longUrlMap.put("memberships_url", "https://very-long-domain-name.example.com/path/to/memberships/endpoint/with/many/parameters?param1=value1&param2=value2&param3=value3");
        longUrlMap.put("lti_version", "LTI-1p0");
        
        LtiMembershipsJobBean job = LtiMembershipsJobBean.of(longUrlMap);
        assertNotNull(job);
        assertEquals("site123", job.getSiteId());
        assertEquals("https://very-long-domain-name.example.com/path/to/memberships/endpoint/with/many/parameters?param1=value1&param2=value2&param3=value3", job.getMembershipsUrl());
        assertEquals("LTI-1p0", job.getLtiVersion());
        
        Map<String, Object> result = job.asMap();
        assertEquals("https://very-long-domain-name.example.com/path/to/memberships/endpoint/with/many/parameters?param1=value1&param2=value2&param3=value3", result.get("memberships_url"));
    }

    @Test
    public void testSpecialCharacters() {
        Map<String, Object> specialCharMap = new HashMap<>();
        specialCharMap.put("SITE_ID", "site-with-special-chars_123");
        specialCharMap.put("memberships_id", "membership@domain.com");
        specialCharMap.put("consumerkey", "key-with-dashes_and_underscores");
        specialCharMap.put("lti_version", "LTI-1p1/1.1");
        
        LtiMembershipsJobBean job = LtiMembershipsJobBean.of(specialCharMap);
        assertNotNull(job);
        assertEquals("site-with-special-chars_123", job.getSiteId());
        assertEquals("membership@domain.com", job.getMembershipsId());
        assertEquals("key-with-dashes_and_underscores", job.getConsumerkey());
        assertEquals("LTI-1p1/1.1", job.getLtiVersion());
        
        Map<String, Object> result = job.asMap();
        assertEquals("site-with-special-chars_123", result.get("SITE_ID"));
        assertEquals("membership@domain.com", result.get("memberships_id"));
        assertEquals("key-with-dashes_and_underscores", result.get("consumerkey"));
        assertEquals("LTI-1p1/1.1", result.get("lti_version"));
    }
}
