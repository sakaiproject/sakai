/**
 * $Id$
 * $URL$
 * HttpRESTUtilsTest.java - entity-broker - Dec 18, 2008 5:11:59 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.util;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.sakaiproject.entitybroker.util.http.HttpRESTUtils;


/**
 * Testing the http rest utils
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class HttpRESTUtilsTest {

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.http.HttpRESTUtils#mergeQueryStringWithParams(java.lang.String, java.util.Map)}.
     */
    @Test
    public void testMergeQueryStringWithParams() {
        String queryString = "/direct/prefix/thing?aaron=az&becky=wife";
        Map<String, String> params = new HashMap<String, String>();
        params.put("apple", "fruit");
        params.put("num", "3");
        String merged = HttpRESTUtils.mergeQueryStringWithParams(queryString, params);
        assertNotNull(merged);
        assertTrue(merged.contains("aaron=az"));
        assertTrue(merged.contains("becky=wife"));
        assertTrue(merged.contains("apple=fruit"));
        assertTrue(merged.contains("num=3"));
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.http.HttpRESTUtils#parseURLintoParams(java.lang.String)}.
     */
    @Test
    public void testParseURLintoParams() {
        String queryString = "/direct/prefix/thing?aaron=az&becky=wife";
        Map<String, String> params = HttpRESTUtils.parseURLintoParams(queryString);
        assertNotNull(params);
        assertTrue(params.containsKey("aaron"));
        assertEquals(params.get("aaron"), "az");
        assertTrue(params.containsKey("becky"));
        assertEquals(params.get("becky"), "wife");
    }

}
