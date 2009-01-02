/**
 * $Id$
 * $URL$
 * DSpaceKernelServletFilterTest.java - DSpace2 - Oct 30, 2008 1:59:18 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.impl.jetty;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.mortbay.jetty.testing.HttpTester;
import org.mortbay.jetty.testing.ServletTester;

import org.sakaiproject.entitybroker.impl.ServiceTestManager;
import org.sakaiproject.entitybroker.mocks.data.TestData;


/**
 * This starts up a jetty server and tests the processing of the servlets and batching
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EntityBrokerServletTest {

    public static final String DIRECT_PREFIX = "/direct";

    protected ServiceTestManager serviceTestManager;
    protected TestData td;

    private ServletTester tester;
    private HttpTester request;
    private HttpTester response;

    @Before
    public void setup() {
        // setup the test data and the set of services
        td = new TestData();
        serviceTestManager = new ServiceTestManager(td);

        assertNotNull(serviceTestManager);
        assertNotNull(serviceTestManager.entityBrokerManager);
        assertNotNull(serviceTestManager.entityEncodingManager);
        assertNotNull(serviceTestManager.entityBatchHandler);

        this.tester = new ServletTester();
        this.tester.setContextPath(DIRECT_PREFIX);
        this.tester.addServlet(MockDirectServlet.class, "/*");
        try {
            this.tester.start();
        } catch (Exception e) {
            fail("Could not start the jetty server: " + e.getMessage());
        }

        this.request = new HttpTester();
        this.response = new HttpTester();
        this.request.setMethod("GET");
        this.request.setHeader("Host", "tester");
        this.request.setVersion("HTTP/1.0");
    }

    @After
    public void teardown() {
        try {
            this.tester.stop();
        } catch (Exception e) {
            fail("Could not stop the jetty server: " + e.getMessage());
        }
    }

    protected void fireRequest(String uri) {
        this.request.setURI(uri);
        try {
            this.response.parse(tester.getResponses(request.generate()));
        } catch (Exception e) {
            fail("Could not parse the response to the request ("+uri+"): " + e.getMessage());
        }
    }

    // now the tests

    @Test
    public void testSimple() {
        // now fire the request
        fireRequest(DIRECT_PREFIX + TestData.ENTITY_URL4_XML);

        try {
            String content = this.response.getContent();
            assertNotNull(content);
            assertTrue(content.contains("myPrefix4"));
            assertTrue(content.contains("4-one"));
            assertTrue(content.contains("<id>4-one</id>"));
            assertTrue(content.contains("<entityId>4-one</entityId>"));
            assertTrue(content.contains("/myPrefix4/4-one"));
            assertFalse(content.contains("4-two"));
            assertFalse(content.contains("4-three"));
        } catch (Exception e) {
            fail("Could get content: " + e.getMessage());
        }
    }

    /**********
    @Test
    public void testDescribe() {
        assertNotNull(serviceTestManager);
        assertNotNull(serviceTestManager.entityBrokerManager);
        assertNotNull(serviceTestManager.entityEncodingManager);
        assertNotNull(serviceTestManager.entityBatchHandler);

        ServletTester tester = new ServletTester();
        tester.setContextPath("/");
        tester.addServlet(TestDirectServlet.class, "/direct");
        try {
            tester.start();
        } catch (Exception e) {
            fail("Could not start the jetty server: " + e.getMessage());
        }

        // now fire the request
        String jettyRequest = 
            "GET /dspace HTTP/1.1\r\n"+
            "Host: tester\r\n"+
            "\r\n";
        try {
            String content = tester.getResponses(jettyRequest);
            assertNotNull(content);
            assertTrue(content.contains("DSpaceTest"));
            assertFalse(content.contains("session=null"));
            assertFalse(content.contains("request=null"));
        } catch (Exception e) {
            fail("Could not fire request: " + e.getMessage());
        }

        // try a request a different way
        HttpTester request = new HttpTester();
        HttpTester response = new HttpTester();
        request.setMethod("GET");
        request.setHeader("Host","tester");
        request.setVersion("HTTP/1.0");
        request.setURI("/dspace");

        try {
            response.parse( tester.getResponses(request.generate()) );
        } catch (IOException e1) {
            fail("Could not parse response: " + e1.getMessage());
        } catch (Exception e1) {
            fail("Could not parse response: " + e1.getMessage());
        }

        assertTrue(response.getMethod() == null);
        assertEquals(200, response.getStatus());
        String content = response.getContent();
        assertNotNull(content);
        assertTrue(content.contains("DSpaceTest"));
        assertFalse(content.contains("session=null"));
        assertFalse(content.contains("request=null"));

        try {
            tester.stop();
        } catch (Exception e) {
            fail("Could not stop the jetty server: " + e.getMessage());
        }
    }

    @Test
    public void testBatch() {
        assertNotNull(serviceTestManager);
        assertNotNull(serviceTestManager.entityBrokerManager);
        assertNotNull(serviceTestManager.entityEncodingManager);
        assertNotNull(serviceTestManager.entityBatchHandler);

        ServletTester tester = new ServletTester();
        tester.setContextPath("/");
        tester.addServlet(TestDirectServlet.class, "/direct");
        try {
            tester.start();
        } catch (Exception e) {
            fail("Could not start the jetty server: " + e.getMessage());
        }

        // now fire the request
        String jettyRequest = 
            "GET /dspace HTTP/1.1\r\n"+
            "Host: tester\r\n"+
            "\r\n";
        try {
            String content = tester.getResponses(jettyRequest);
            assertNotNull(content);
            assertTrue(content.contains("DSpaceTest"));
            assertFalse(content.contains("session=null"));
            assertFalse(content.contains("request=null"));
        } catch (Exception e) {
            fail("Could not fire request: " + e.getMessage());
        }

        // try a request a different way
        HttpTester request = new HttpTester();
        HttpTester response = new HttpTester();
        request.setMethod("GET");
        request.setHeader("Host","tester");
        request.setVersion("HTTP/1.0");
        request.setURI("/dspace");

        try {
            response.parse( tester.getResponses(request.generate()) );
        } catch (IOException e1) {
            fail("Could not parse response: " + e1.getMessage());
        } catch (Exception e1) {
            fail("Could not parse response: " + e1.getMessage());
        }

        assertTrue(response.getMethod() == null);
        assertEquals(200, response.getStatus());
        String content = response.getContent();
        assertNotNull(content);
        assertTrue(content.contains("DSpaceTest"));
        assertFalse(content.contains("session=null"));
        assertFalse(content.contains("request=null"));

        try {
            tester.stop();
        } catch (Exception e) {
            fail("Could not stop the jetty server: " + e.getMessage());
        }
    }
    *******/

}
