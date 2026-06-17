/**
 * $Id$
 * $URL$
 * HttpRESTUtilsTest.java - entity-broker - Dec 18, 2008 5:11:59 PM - azeckoski
 **********************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
 **********************************************************************************/

package org.sakaiproject.entitybroker.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.Cookie;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.Test;
import org.sakaiproject.entitybroker.util.http.HttpClientWrapper;
import org.sakaiproject.entitybroker.util.http.HttpRESTUtils;
import org.sakaiproject.entitybroker.util.http.HttpRESTUtils.HttpRequestException;
import org.sakaiproject.entitybroker.util.http.HttpRESTUtils.Method;
import org.sakaiproject.entitybroker.util.http.HttpResponse;
import org.sakaiproject.entitybroker.util.http.URLData;


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

    @Test
    public void testFireRequestGetWithParamsAndHeaders() throws Exception {
        AtomicReference<String> requestQuery = new AtomicReference<String>();
        AtomicReference<String> requestHeader = new AtomicReference<String>();
        AtomicReference<List<String>> userAgentHeaders = new AtomicReference<List<String>>();
        HttpServer server = startServer(exchange -> {
            requestQuery.set(exchange.getRequestURI().getRawQuery());
            requestHeader.set(exchange.getRequestHeaders().getFirst("X-Test"));
            userAgentHeaders.set(exchange.getRequestHeaders().get("User-Agent"));
            writeResponse(exchange, 200, "ok");
        });
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("apple", "fruit");
            params.put("num", "3");
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("X-Test", "header-value");
            headers.put("User-Agent", "custom-agent");

            HttpResponse response = HttpRESTUtils.fireRequest(serverUrl(server), Method.GET, params, headers, null, false);

            assertEquals(200, response.getResponseCode());
            assertEquals("OK", response.getResponseMessage());
            assertEquals("ok", response.getResponseBody());
            assertTrue(requestQuery.get().contains("apple=fruit"));
            assertTrue(requestQuery.get().contains("num=3"));
            assertEquals("header-value", requestHeader.get());
            assertEquals(1, userAgentHeaders.get().size());
            assertEquals("custom-agent", userAgentHeaders.get().get(0));
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void testFireRequestPostWithFormParams() throws Exception {
        AtomicReference<String> requestBody = new AtomicReference<String>();
        AtomicReference<String> contentType = new AtomicReference<String>();
        HttpServer server = startServer(exchange -> {
            contentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), "UTF-8"));
            writeResponse(exchange, 201, "created");
        });
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("name", "Sakai User");
            params.put("role", "maintainer");

            HttpResponse response = HttpRESTUtils.fireRequest(serverUrl(server), Method.POST, params);

            assertEquals(201, response.getResponseCode());
            assertEquals("Created", response.getResponseMessage());
            assertEquals("created", response.getResponseBody());
            assertTrue(contentType.get().startsWith(HttpRESTUtils.FORM_CONTENT_TYPE_UTF8));
            assertTrue(requestBody.get().contains("name=Sakai+User"));
            assertTrue(requestBody.get().contains("role=maintainer"));
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void testFireRequestRejectsLargeResponse() throws Exception {
        HttpServer server = startServer(exchange -> writeResponse(exchange, 200,
                "x".repeat(HttpRESTUtils.MAX_RESPONSE_SIZE_CHARS + 1)));
        try {
            HttpRESTUtils.fireRequest(serverUrl(server), Method.GET);
            fail("should have rejected oversized response");
        } catch (HttpRequestException e) {
            assertTrue(e.getMessage().contains("exceeded the maximum allowed batch response size"));
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void testReusableClientKeepsCookieState() throws Exception {
        AtomicInteger requestCount = new AtomicInteger();
        AtomicReference<String> firstCookieHeader = new AtomicReference<String>();
        AtomicReference<String> secondCookieHeader = new AtomicReference<String>();
        HttpServer server = startServer(exchange -> {
            int count = requestCount.incrementAndGet();
            if (count == 1) {
                firstCookieHeader.set(exchange.getRequestHeaders().getFirst("Cookie"));
                exchange.getResponseHeaders().add("Set-Cookie", "session=server-value; Path=/");
                writeResponse(exchange, 200, "set");
            } else {
                secondCookieHeader.set(exchange.getRequestHeaders().getFirst("Cookie"));
                writeResponse(exchange, 200, "check");
            }
        });
        try {
            Cookie initialCookie = new Cookie("initial", "request-value");
            HttpClientWrapper wrapper = HttpRESTUtils.makeReusableHttpClient(false, 0, new Cookie[] { initialCookie });

            HttpRESTUtils.fireRequest(wrapper, serverUrl(server), Method.GET, null, null, false);
            HttpRESTUtils.fireRequest(wrapper, serverUrl(server), Method.GET, null, null, false);

            assertNotNull(firstCookieHeader.get());
            assertTrue(firstCookieHeader.get().contains("initial=request-value"));
            assertNotNull(secondCookieHeader.get());
            assertTrue(secondCookieHeader.get().contains("session=server-value"));
        } finally {
            server.stop(0);
        }
    }

    @Test
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
        assertEquals("direct", ud.servletName);

        url = "/direct/prefix";
        ud = HttpRESTUtils.parseURL(url);
        assertNotNull(ud);
        assertEquals("/prefix", ud.pathInfo);
        assertEquals("80", ud.port);
        assertEquals("http", ud.protocol);
        assertEquals("", ud.query);
        assertEquals("localhost", ud.server);
        assertEquals("direct", ud.servletName);

        url = "/direct/prefix/id";
        ud = HttpRESTUtils.parseURL(url);
        assertNotNull(ud);
        assertEquals("/prefix/id", ud.pathInfo);
        assertEquals("80", ud.port);
        assertEquals("http", ud.protocol);
        assertEquals("", ud.query);
        assertEquals("localhost", ud.server);
        assertEquals("direct", ud.servletName);

        url = "direct/prefix/id";
        ud = HttpRESTUtils.parseURL(url);
        assertNotNull(ud);
        assertEquals("/prefix/id", ud.pathInfo);
        assertEquals("80", ud.port);
        assertEquals("http", ud.protocol);
        assertEquals("", ud.query);
        assertEquals("localhost", ud.server);
        assertEquals("direct", ud.servletName);

        url = "/direct/prefix/id?param1=AZ";
        ud = HttpRESTUtils.parseURL(url);
        assertNotNull(ud);
        assertEquals("/prefix/id", ud.pathInfo);
        assertEquals("80", ud.port);
        assertEquals("http", ud.protocol);
        assertEquals("param1=AZ", ud.query);
        assertEquals("localhost", ud.server);
        assertEquals("direct", ud.servletName);

        url = "/direct/prefix/id?param1=AZ&param2=BZ";
        ud = HttpRESTUtils.parseURL(url);
        assertNotNull(ud);
        assertEquals("/prefix/id", ud.pathInfo);
        assertEquals("80", ud.port);
        assertEquals("http", ud.protocol);
        assertEquals("param1=AZ&param2=BZ", ud.query);
        assertEquals("localhost", ud.server);
        assertEquals("direct", ud.servletName);

        url = "/direct/prefix/id/thing.xml?param1=AZ&param2=BZ";
        ud = HttpRESTUtils.parseURL(url);
        assertNotNull(ud);
        assertEquals("/prefix/id/thing.xml", ud.pathInfo);
        assertEquals("80", ud.port);
        assertEquals("http", ud.protocol);
        assertEquals("param1=AZ&param2=BZ", ud.query);
        assertEquals("localhost", ud.server);
        assertEquals("direct", ud.servletName);
        assertEquals("xml", ud.extension);
        assertEquals("/prefix/id/thing", ud.pathInfoNoExtension);

        url = "ftp://server/direct/prefix/id?param1=AZ&param2=BZ";
        ud = HttpRESTUtils.parseURL(url);
        assertNotNull(ud);
        assertEquals("/prefix/id", ud.pathInfo);
        assertEquals("80", ud.port);
        assertEquals("ftp", ud.protocol);
        assertEquals("param1=AZ&param2=BZ", ud.query);
        assertEquals("server", ud.server);
        assertEquals("direct", ud.servletName);
        assertEquals("", ud.extension);
        assertEquals("/prefix/id", ud.pathInfoNoExtension);

        url = "ftp://server:8080/direct/prefix/id?param1=AZ&param2=BZ";
        ud = HttpRESTUtils.parseURL(url);
        assertNotNull(ud);
        assertEquals("/prefix/id", ud.pathInfo);
        assertEquals("8080", ud.port);
        assertEquals("ftp", ud.protocol);
        assertEquals("param1=AZ&param2=BZ", ud.query);
        assertEquals("server", ud.server);
        assertEquals("direct", ud.servletName);

        url = "ftp://server:8080/direct/prefix/id/thing.xml?param1=AZ&param2=BZ";
        ud = HttpRESTUtils.parseURL(url);
        assertNotNull(ud);
        assertEquals("/prefix/id/thing.xml", ud.pathInfo);
        assertEquals("8080", ud.port);
        assertEquals("ftp", ud.protocol);
        assertEquals("param1=AZ&param2=BZ", ud.query);
        assertEquals("server", ud.server);
        assertEquals("direct", ud.servletName);
        assertEquals("xml", ud.extension);
        assertEquals("/prefix/id/thing", ud.pathInfoNoExtension);

        url = "ftp://server:8080/direct";
        ud = HttpRESTUtils.parseURL(url);
        assertNotNull(ud);
        assertEquals("", ud.pathInfo);
        assertEquals("8080", ud.port);
        assertEquals("ftp", ud.protocol);
        assertEquals("", ud.query);
        assertEquals("server", ud.server);
        assertEquals("direct", ud.servletName);

        url = "ftp://server:8080/direct?param1=AZ&param2=BZ";
        ud = HttpRESTUtils.parseURL(url);
        assertNotNull(ud);
        assertEquals("", ud.pathInfo);
        assertEquals("8080", ud.port);
        assertEquals("ftp", ud.protocol);
        assertEquals("param1=AZ&param2=BZ", ud.query);
        assertEquals("server", ud.server);
        assertEquals("direct", ud.servletName);

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

    private interface ExchangeHandler {
        void handle(HttpExchange exchange) throws IOException;
    }

    private HttpServer startServer(ExchangeHandler handler) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/test", exchange -> handler.handle(exchange));
        server.start();
        return server;
    }

    private String serverUrl(HttpServer server) {
        return "http://localhost:" + server.getAddress().getPort() + "/test";
    }

    private void writeResponse(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes("UTF-8");
        exchange.getResponseHeaders().add("X-Response", "present");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}
