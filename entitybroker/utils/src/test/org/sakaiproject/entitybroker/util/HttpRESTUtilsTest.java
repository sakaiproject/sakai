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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.sakaiproject.entitybroker.util.http.HttpRESTUtils;
import org.sakaiproject.entitybroker.util.http.URLData;


/**
 * Testing the http rest utils
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class HttpRESTUtilsTest extends TestCase {

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.http.HttpRESTUtils#mergeQueryStringWithParams(java.lang.String, java.util.Map)}.
     */
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
    public void testParseURLintoParams() {
        String queryString = "/direct/prefix/thing?aaron=az&becky=wife";
        Map<String, String> params = HttpRESTUtils.parseURLintoParams(queryString);
        assertNotNull(params);
        assertTrue(params.containsKey("aaron"));
        assertEquals(params.get("aaron"), "az");
        assertTrue(params.containsKey("becky"));
        assertEquals(params.get("becky"), "wife");
    }

    public void testParse() {
        String url = null;
        URLData ud = null;

        url = "/direct";
        ud = HttpRESTUtils.parseURL(url);
        assertNotNull(ud);
        assertEquals("", ud.pathInfo);
        assertEquals("80", ud.port);
        assertEquals("http", ud.protocol);
        assertEquals("", ud.query);
        assertEquals("localhost", ud.server);
        assertEquals("direct", ud.servletPath);

        url = "/direct/prefix";
        ud = HttpRESTUtils.parseURL(url);
        assertNotNull(ud);
        assertEquals("/prefix", ud.pathInfo);
        assertEquals("80", ud.port);
        assertEquals("http", ud.protocol);
        assertEquals("", ud.query);
        assertEquals("localhost", ud.server);
        assertEquals("direct", ud.servletPath);

        url = "/direct/prefix/id";
        ud = HttpRESTUtils.parseURL(url);
        assertNotNull(ud);
        assertEquals("/prefix/id", ud.pathInfo);
        assertEquals("80", ud.port);
        assertEquals("http", ud.protocol);
        assertEquals("", ud.query);
        assertEquals("localhost", ud.server);
        assertEquals("direct", ud.servletPath);

        url = "direct/prefix/id";
        ud = HttpRESTUtils.parseURL(url);
        assertNotNull(ud);
        assertEquals("/prefix/id", ud.pathInfo);
        assertEquals("80", ud.port);
        assertEquals("http", ud.protocol);
        assertEquals("", ud.query);
        assertEquals("localhost", ud.server);
        assertEquals("direct", ud.servletPath);

        url = "/direct/prefix/id?param1=AZ";
        ud = HttpRESTUtils.parseURL(url);
        assertNotNull(ud);
        assertEquals("/prefix/id", ud.pathInfo);
        assertEquals("80", ud.port);
        assertEquals("http", ud.protocol);
        assertEquals("param1=AZ", ud.query);
        assertEquals("localhost", ud.server);
        assertEquals("direct", ud.servletPath);

        url = "/direct/prefix/id?param1=AZ&param2=BZ";
        ud = HttpRESTUtils.parseURL(url);
        assertNotNull(ud);
        assertEquals("/prefix/id", ud.pathInfo);
        assertEquals("80", ud.port);
        assertEquals("http", ud.protocol);
        assertEquals("param1=AZ&param2=BZ", ud.query);
        assertEquals("localhost", ud.server);
        assertEquals("direct", ud.servletPath);

        url = "/direct/prefix/id/thing.xml?param1=AZ&param2=BZ";
        ud = HttpRESTUtils.parseURL(url);
        assertNotNull(ud);
        assertEquals("/prefix/id/thing.xml", ud.pathInfo);
        assertEquals("80", ud.port);
        assertEquals("http", ud.protocol);
        assertEquals("param1=AZ&param2=BZ", ud.query);
        assertEquals("localhost", ud.server);
        assertEquals("direct", ud.servletPath);

        url = "ftp://server/direct/prefix/id?param1=AZ&param2=BZ";
        ud = HttpRESTUtils.parseURL(url);
        assertNotNull(ud);
        assertEquals("/prefix/id", ud.pathInfo);
        assertEquals("80", ud.port);
        assertEquals("ftp", ud.protocol);
        assertEquals("param1=AZ&param2=BZ", ud.query);
        assertEquals("server", ud.server);
        assertEquals("direct", ud.servletPath);

        url = "ftp://server:8080/direct/prefix/id?param1=AZ&param2=BZ";
        ud = HttpRESTUtils.parseURL(url);
        assertNotNull(ud);
        assertEquals("/prefix/id", ud.pathInfo);
        assertEquals("8080", ud.port);
        assertEquals("ftp", ud.protocol);
        assertEquals("param1=AZ&param2=BZ", ud.query);
        assertEquals("server", ud.server);
        assertEquals("direct", ud.servletPath);

        url = "ftp://server:8080/direct/prefix/id/thing.xml?param1=AZ&param2=BZ";
        ud = HttpRESTUtils.parseURL(url);
        assertNotNull(ud);
        assertEquals("/prefix/id/thing.xml", ud.pathInfo);
        assertEquals("8080", ud.port);
        assertEquals("ftp", ud.protocol);
        assertEquals("param1=AZ&param2=BZ", ud.query);
        assertEquals("server", ud.server);
        assertEquals("direct", ud.servletPath);

        url = "ftp://server:8080/direct";
        ud = HttpRESTUtils.parseURL(url);
        assertNotNull(ud);
        assertEquals("", ud.pathInfo);
        assertEquals("8080", ud.port);
        assertEquals("ftp", ud.protocol);
        assertEquals("", ud.query);
        assertEquals("server", ud.server);
        assertEquals("direct", ud.servletPath);

        url = "ftp://server:8080/direct?param1=AZ&param2=BZ";
        ud = HttpRESTUtils.parseURL(url);
        assertNotNull(ud);
        assertEquals("", ud.pathInfo);
        assertEquals("8080", ud.port);
        assertEquals("ftp", ud.protocol);
        assertEquals("param1=AZ&param2=BZ", ud.query);
        assertEquals("server", ud.server);
        assertEquals("direct", ud.servletPath);

        try {
            ud = HttpRESTUtils.parseURL(null);
            fail("should have died");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    // Cannot handle partial URLs
//    public void testParseURL() {
//        URL url = null;
//
//        try {
//            url = new URL("/servlet/path1/path2.xml?q1=A&q2=B");
//            assertNotNull(url);
//        } catch (MalformedURLException e) {
//            fail("OOPS: " + e.getMessage());
//        }
//        assertEquals("/servlet/path1/path2", url.getPath());
//        assertEquals("q1=A&q2=B", url.getQuery());
//    }

}
