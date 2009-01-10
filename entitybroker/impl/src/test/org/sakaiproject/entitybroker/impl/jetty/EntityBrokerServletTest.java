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

import org.sakaiproject.entitybroker.EntityRequestHandler;
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

        this.response = new HttpTester();
        prepareRequest();
    }

    private void prepareRequest() {
        this.request = new HttpTester();
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

    /**
     * Fires off a request using Jetty to the given uri (uses GET by default),
     * also resets the request afterward so it can be used again if desired
     * @param uri any uri which you want to test (should be valid for the test)
     */
    protected void fireRequest(String uri) {
        this.response = new HttpTester(); // build a new response
        this.request.setMethod("GET");
        this.request.setURI(uri);
        try {
            this.response.parse(tester.getResponses(request.generate()));
        } catch (Exception e) {
            fail("Could not parse the response to the request ("+uri+"): " + e.getMessage());
        }
        prepareRequest(); // reset the request
    }


    // now the tests

    @Test
    public void testSimpleXML() {
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
            fail("Could not get content: " + e.getMessage());
        }
    }

    @Test
    public void testSimpleJSON() {
        // now fire the request
        fireRequest(DIRECT_PREFIX + TestData.ENTITY_URL4_JSON);

        try {
            String content = this.response.getContent();
            assertNotNull(content);
            assertTrue(content.contains("myPrefix4"));
            assertTrue(content.contains("4-one"));
            assertTrue(content.contains("\"id\": \"4-one\""));
            assertTrue(content.contains("\"entityReference\":"));
            assertTrue(content.contains("\\/myPrefix4\\/4-one"));
            assertFalse(content.contains("4-two"));
            assertFalse(content.contains("4-three"));
        } catch (Exception e) {
            fail("Could not get content: " + e.getMessage());
        }
    }

    @Test
    public void testSimpleCollectionXML() {
        // now fire the request
        fireRequest(DIRECT_PREFIX + TestData.COLLECTION_URL4_XML);

        try {
            String content = this.response.getContent();
            assertNotNull(content);
            assertTrue(content.contains("<myPrefix4_collection entityPrefix=\"myPrefix4\">"));
            assertTrue(content.contains("</myPrefix4_collection>"));
            assertTrue(content.contains("4-two"));
            assertTrue(content.contains("4-three"));
            assertTrue(content.contains("myPrefix4"));
            assertTrue(content.contains("4-one"));
            assertTrue(content.contains("<id>4-one</id>"));
            assertTrue(content.contains("<entityId>4-one</entityId>"));
        } catch (Exception e) {
            fail("Could not get content: " + e.getMessage());
        }
    }

    @Test
    public void testSimpleCollectionJSON() {
        // now fire the request
        fireRequest(DIRECT_PREFIX + TestData.COLLECTION_URL4_JSON);

        try {
            String content = this.response.getContent();
            assertNotNull(content);
            assertTrue(content.contains("\"myPrefix4_collection\":"));
            assertTrue(content.contains("myPrefix4"));
            assertTrue(content.contains("4-one"));
            assertTrue(content.contains("\"id\": \"4-one\""));
            assertTrue(content.contains("\"entityReference\":"));
            assertTrue(content.contains("\\/myPrefix4\\/4-one"));
            assertTrue(content.contains("4-two"));
            assertTrue(content.contains("4-three"));
        } catch (Exception e) {
            fail("Could not get content: " + e.getMessage());
        }
    }

    // test describe

    @Test
    public void testDescribe() {
        // now fire the request
        fireRequest(DIRECT_PREFIX + EntityRequestHandler.SLASH_DESCRIBE);

        try {
            String content = this.response.getContent();
            assertNotNull(content);
            // just check for working and valid format
            assertTrue(content.contains("<?xml"));
            assertTrue(content.contains("<!DOCTYPE html"));
            assertTrue(content.contains("<html"));
            assertTrue(content.contains("<head"));
            assertTrue(content.contains("<title"));
            assertTrue(content.contains("</title>"));
            assertTrue(content.contains("<body"));
            assertTrue(content.contains("</body>"));
            assertTrue(content.contains("</html>"));
        } catch (Exception e) {
            fail("Could not get content: " + e.getMessage());
        }
    }

    @Test
    public void testDescribeXML() {
        // now fire the request
        fireRequest(DIRECT_PREFIX + EntityRequestHandler.SLASH_DESCRIBE + ".xml");

        try {
            String content = this.response.getContent();
            assertNotNull(content);
            assertTrue(content.contains("<?xml"));
            assertTrue(content.contains("<describe>"));
            assertTrue(content.contains("<prefixes>"));
            assertTrue(content.contains("<prefix>"));
            assertTrue(content.contains("<capabilities>"));
            assertTrue(content.contains("<describeURL>"));
            assertTrue(content.contains("<capability>"));
            assertTrue(content.contains("</prefixes>"));
            assertTrue(content.contains("</describe>"));
        } catch (Exception e) {
            fail("Could not get content: " + e.getMessage());
        }
    }

    @Test
    public void testDescribeEntity() {
        // now fire the request
        fireRequest(DIRECT_PREFIX + "/" + TestData.PREFIX4 + EntityRequestHandler.SLASH_DESCRIBE);

        try {
            String content = this.response.getContent();
            assertNotNull(content);
            assertTrue(content.contains("<?xml"));
            assertTrue(content.contains("<!DOCTYPE html"));
            assertTrue(content.contains("<html"));
            assertTrue(content.contains("<head"));
            assertTrue(content.contains("<title"));
            assertTrue(content.contains("</title>"));
            assertTrue(content.contains("<body"));
            assertTrue(content.contains("</body>"));
            assertTrue(content.contains("</html>"));
            assertTrue(content.contains(TestData.PREFIX4));
        } catch (Exception e) {
            fail("Could not get content: " + e.getMessage());
        }
    }

    @Test
    public void testDescribeEntityXML() {
        // now fire the request
        fireRequest(DIRECT_PREFIX + "/" + TestData.PREFIX4 + EntityRequestHandler.SLASH_DESCRIBE + ".xml");

        try {
            String content = this.response.getContent();
            assertNotNull(content);
            assertTrue(content.contains("<?xml"));
            assertTrue(content.contains("<prefix>"));
            assertTrue(content.contains("<collectionURL>/"+TestData.PREFIX4+"</collectionURL>"));
            assertTrue(content.contains("<describeURL>"));
            assertTrue(content.contains("<capabilities>"));
            assertTrue(content.contains("<capability>"));
            assertTrue(content.contains(TestData.PREFIX4));
        } catch (Exception e) {
            fail("Could not get content: " + e.getMessage());
        }
    }

    // test batching

    @Test
    public void testBatchGetOneEntityXML() {
        // now fire the request
        String url = DIRECT_PREFIX + EntityRequestHandler.SLASH_BATCH + ".xml" + "?refs=" + DIRECT_PREFIX + TestData.REF4;
        fireRequest(url);

        try {
            String content = this.response.getContent();
            assertNotNull(content);
            assertTrue(content.contains("<refs"));
            assertTrue(content.contains("ref0"));
            assertTrue(content.contains("<headers"));
            assertTrue(content.contains("<status"));
            assertTrue(content.contains("200"));
            assertTrue(content.contains("<data"));
            assertTrue(content.contains("refs"));
            assertTrue(content.contains("refs"));
            assertTrue(content.contains(TestData.PREFIX4));
            assertTrue(content.contains("4-one"));
            assertTrue(content.contains("<id>4-one</id>"));
            assertTrue(content.contains("<entityId>4-one</entityId>"));
            assertTrue(content.contains("/myPrefix4/4-one"));
        } catch (Exception e) {
            fail("Could not get content: " + e.getMessage());
        }
    }

    @Test
    public void testBatchGetOneEntityJSON() {
        // now fire the request
        String url = DIRECT_PREFIX + EntityRequestHandler.SLASH_BATCH + ".json" + "?refs=" + DIRECT_PREFIX + TestData.REF4 + ".xml";
        fireRequest(url);

        try {
            String content = this.response.getContent();
            assertNotNull(content);
            assertTrue(content.contains("ref0"));
            assertTrue(content.contains("\"status\":"));
            assertTrue(content.contains("200"));
            assertTrue(content.contains("\"headers\":"));
            assertTrue(content.contains("\"reference\":"));
            assertTrue(content.contains("\"data\":"));
            assertTrue(content.contains("myPrefix4"));
            assertTrue(content.contains("4-one"));
            assertTrue(content.contains("\"id\": \"4-one\""));
            assertTrue(content.contains("\"entityReference\":"));
            assertTrue(content.contains("\\/myPrefix4\\/4-one"));
            assertTrue(content.contains(TestData.PREFIX4));
        } catch (Exception e) {
            fail("Could not get content: " + e.getMessage());
        }

        url = DIRECT_PREFIX + EntityRequestHandler.SLASH_BATCH + ".json" + "?refs=" + DIRECT_PREFIX + TestData.REF4_3;
        fireRequest(url);

        try {
            String content = this.response.getContent();
            assertNotNull(content);
            assertTrue(content.contains("ref0"));
            assertTrue(content.contains("\"status\":"));
            assertTrue(content.contains("200"));
            assertTrue(content.contains("\"headers\":"));
            assertTrue(content.contains("\"reference\":"));
            assertTrue(content.contains("\"data\":"));
            assertTrue(content.contains("\"entityReference\":"));
            assertTrue(content.contains(TestData.IDS4[2]));
            assertTrue(content.contains(TestData.PREFIX4));
        } catch (Exception e) {
            fail("Could not get content: " + e.getMessage());
        }

        url = DIRECT_PREFIX + EntityRequestHandler.SLASH_BATCH + ".json" + "?refs=" + DIRECT_PREFIX + TestData.REF6_3;
        fireRequest(url);

        try {
            String content = this.response.getContent();
            assertNotNull(content);
            assertTrue(content.contains("ref0"));
            assertTrue(content.contains("\"status\":"));
            assertTrue(content.contains("200"));
            assertTrue(content.contains("\"headers\":"));
            assertTrue(content.contains("\"reference\":"));
            assertTrue(content.contains("\"data\":"));
            assertTrue(content.contains("\"entityReference\":"));
            assertTrue(content.contains(TestData.IDS6[2]));
            assertTrue(content.contains(TestData.PREFIX6));
        } catch (Exception e) {
            fail("Could not get content: " + e.getMessage());
        }
    }

    // test redirect
    @Test
    public void testBatchRedirectEntityXML() {
        // normal
        String url = DIRECT_PREFIX + EntityRequestHandler.SLASH_BATCH + ".xml" + "?refs=" + DIRECT_PREFIX + TestData.REFU1 + ".xml";
        fireRequest(url);

        try {
            String content = this.response.getContent();
            assertNotNull(content);
            assertTrue(content.contains("<refs"));
            assertTrue(content.contains("ref0"));
            assertTrue(content.contains("<headers"));
            assertTrue(content.contains("<status"));
            assertTrue(content.contains("200"));
            assertTrue(content.contains("<data"));
            assertTrue(content.contains("refs"));
            assertTrue(content.contains(TestData.PREFIXU1));
            assertTrue(content.contains("rA"));
            assertTrue(content.contains("<id>rA</id>"));
            assertTrue(content.contains("<entityId>rA</entityId>"));
            assertTrue(content.contains("/redirect1/rA"));
        } catch (Exception e) {
            fail("Could not get content: " + e.getMessage());
        }

        // once again with feeling
        url = DIRECT_PREFIX + EntityRequestHandler.SLASH_BATCH + ".xml" + "?refs=" 
            + DIRECT_PREFIX + "/" + TestData.PREFIXU1 + "/xml/" + TestData.IDSU1[0];
        fireRequest(url);

        try {
            String content = this.response.getContent();
            assertNotNull(content);
            assertTrue(content.contains("<refs"));
            assertTrue(content.contains("ref0"));
            assertTrue(content.contains("<headers"));
            assertTrue(content.contains("<status"));
            assertTrue(content.contains("200"));
            assertTrue(content.contains("<data"));
            assertTrue(content.contains("refs"));
            assertTrue(content.contains(TestData.PREFIXU1));
            assertTrue(content.contains("rA"));
            assertTrue(content.contains("<id>rA</id>"));
            assertTrue(content.contains("<entityId>rA</entityId>"));
            assertTrue(content.contains("/redirect1/rA"));
        } catch (Exception e) {
            fail("Could not get content: " + e.getMessage());
        }

    }

    // test batch multiple get

    @Test
    public void testBatchGetEntitiesXML() {
        // now fire the request
        String url = DIRECT_PREFIX + EntityRequestHandler.SLASH_BATCH + ".xml" 
                + "?refs=" + DIRECT_PREFIX + TestData.REF4_two
                + "," + DIRECT_PREFIX + TestData.REF6_2;
        fireRequest(url);

        try {
            String content = this.response.getContent();
            assertNotNull(content);
            assertTrue(content.contains("<refs"));
            assertTrue(content.contains("ref0"));
            assertTrue(content.contains("<headers"));
            assertTrue(content.contains("<status"));
            assertTrue(content.contains("200"));
            assertTrue(content.contains("<data"));
            assertTrue(content.contains("refs"));
            assertTrue(content.contains("refs"));
            assertTrue(content.contains(TestData.PREFIX4));
            assertTrue(content.contains("4-two"));
            assertTrue(content.contains("<id>4-two</id>"));
            assertTrue(content.contains("<entityId>4-two</entityId>"));
            assertTrue(content.contains("/myPrefix4/4-two"));
            assertTrue(content.contains(TestData.PREFIX6));
            assertTrue(content.contains("6-two"));
            assertTrue(content.contains("<id>6-two</id>"));
            assertTrue(content.contains("<entityId>6-two</entityId>"));
            assertTrue(content.contains("/myPrefix6/6-two"));
        } catch (Exception e) {
            fail("Could not get content: " + e.getMessage());
        }
    }

    @Test
    public void testBatchGetEntitiesJSON() {
        // now fire the request
        String url = DIRECT_PREFIX + EntityRequestHandler.SLASH_BATCH + ".json" 
                + "?refs=" + DIRECT_PREFIX + TestData.REF4_two
                + "," + DIRECT_PREFIX + TestData.REF6_2;
        fireRequest(url);

        try {
            String content = this.response.getContent();
            assertNotNull(content);
            assertTrue(content.contains("ref0"));
            assertTrue(content.contains("\"status\":"));
            assertTrue(content.contains("200"));
            assertTrue(content.contains("\"headers\":"));
            assertTrue(content.contains("\"reference\":"));
            assertTrue(content.contains("\"data\":"));
            assertTrue(content.contains(TestData.PREFIX4));
            assertTrue(content.contains("4-two"));
            assertTrue(content.contains("\"id\": \"4-two\""));
            assertTrue(content.contains("\"entityReference\":"));
            assertTrue(content.contains("\\/myPrefix4\\/4-two"));
            assertTrue(content.contains(TestData.PREFIX6));
            assertTrue(content.contains("6-two"));
            assertTrue(content.contains("\"id\": \"6-two\""));
        } catch (Exception e) {
            fail("Could not get content: " + e.getMessage());
        }
    }


    @Test
    public void testBatchGetCollectionsXML() {
        // now fire the request
        String url = DIRECT_PREFIX + EntityRequestHandler.SLASH_BATCH + ".xml" 
                + "?refs=" + DIRECT_PREFIX + TestData.SPACE4
                + "," + DIRECT_PREFIX + TestData.REF6_2;
        fireRequest(url);

        try {
            String content = this.response.getContent();
            assertNotNull(content);
            assertTrue(content.contains("<refs"));
            assertTrue(content.contains("ref0"));
            assertTrue(content.contains("<headers"));
            assertTrue(content.contains("<status"));
            assertTrue(content.contains("200"));
            assertTrue(content.contains("<data"));
            assertTrue(content.contains("refs"));
            assertTrue(content.contains("refs"));
            assertTrue(content.contains(TestData.PREFIX4));
            assertTrue(content.contains("4-one"));
            assertTrue(content.contains("4-two"));
            assertTrue(content.contains("4-three"));
            assertTrue(content.contains("<id>4-two</id>"));
            assertTrue(content.contains("<entityId>4-two</entityId>"));
            assertTrue(content.contains("/myPrefix4/4-two"));
            assertTrue(content.contains(TestData.PREFIX6));
            assertTrue(content.contains("6-two"));
            assertTrue(content.contains("<id>6-two</id>"));
            assertTrue(content.contains("<entityId>6-two</entityId>"));
            assertTrue(content.contains("/myPrefix6/6-two"));
        } catch (Exception e) {
            fail("Could not get content: " + e.getMessage());
        }
    }

    @Test
    public void testBatchGetCollectionsJSON() {
        // now fire the request
        String url = DIRECT_PREFIX + EntityRequestHandler.SLASH_BATCH + ".json" 
                + "?refs=" + DIRECT_PREFIX + TestData.SPACE4
                + "," + DIRECT_PREFIX + TestData.REF6_2;
        fireRequest(url);

        try {
            String content = this.response.getContent();
            assertNotNull(content);
            assertTrue(content.contains("ref0"));
            assertTrue(content.contains("\"status\":"));
            assertTrue(content.contains("200"));
            assertTrue(content.contains("\"headers\":"));
            assertTrue(content.contains("\"reference\":"));
            assertTrue(content.contains("\"data\":"));
            assertTrue(content.contains(TestData.PREFIX4));
            assertTrue(content.contains("4-one"));
            assertTrue(content.contains("4-two"));
            assertTrue(content.contains("4-three"));
            assertTrue(content.contains("\"id\": \"4-two\""));
            assertTrue(content.contains("\"entityReference\":"));
            assertTrue(content.contains("\\/myPrefix4\\/4-two"));
            assertTrue(content.contains(TestData.PREFIX6));
            assertTrue(content.contains("6-two"));
            assertTrue(content.contains("\"id\": \"6-two\""));
        } catch (Exception e) {
            fail("Could not get content: " + e.getMessage());
        }
    }

    @Test
    public void testBatchGetValidAndInvalidJSON() {
        // now fire the request
        String url = DIRECT_PREFIX + EntityRequestHandler.SLASH_BATCH + ".json" 
                + "?refs=" + DIRECT_PREFIX + TestData.SPACE4
                + "," + DIRECT_PREFIX + TestData.REF6_2
                + "," + DIRECT_PREFIX + TestData.SPACE6 + "/XXXXX";
        fireRequest(url);

        try {
            String content = this.response.getContent();
            assertNotNull(content);
            assertTrue(content.contains("ref0"));
            assertTrue(content.contains("\"status\":"));
            assertTrue(content.contains("200"));
            assertTrue(content.contains("\"headers\":"));
            assertTrue(content.contains("\"reference\":"));
            assertTrue(content.contains("\"data\":"));
            assertTrue(content.contains(TestData.PREFIX4));
            assertTrue(content.contains("4-one"));
            assertTrue(content.contains("4-two"));
            assertTrue(content.contains("4-three"));
            assertTrue(content.contains("\"id\": \"4-two\""));
            assertTrue(content.contains("\"entityReference\":"));
            assertTrue(content.contains("\\/myPrefix4\\/4-two"));
            assertTrue(content.contains(TestData.PREFIX6));
            assertTrue(content.contains("6-two"));
            assertTrue(content.contains("\"id\": \"6-two\""));
        } catch (Exception e) {
            fail("Could not get content: " + e.getMessage());
        }
    }

    // TODO need to work on a way to actually call the other webapps, it is not actually possible with forward/include

//    @Test
//    public void testBatchAllInvalidJSON() {
//        // now fire the request
//        String url = DIRECT_PREFIX + EntityRequestHandler.SLASH_BATCH + ".json" 
//                + "?refs=/XXXXX/XXXX/XXX,/YYYYY/YYYY,/ZZZZZ";
//        fireRequest(url);
//
//        try {
//            String content = this.response.getContent();
//            assertNotNull(content);
//            assertTrue(content.contains("ref0"));
//            assertTrue(content.contains("\"status\":"));
//            assertTrue(content.contains("200"));
//            assertTrue(content.contains("\"headers\":"));
//            assertTrue(content.contains("\"reference\":"));
//            assertTrue(content.contains("\"data\":"));
//            assertTrue(content.contains(TestData.PREFIX4));
//            assertTrue(content.contains("4-one"));
//            assertTrue(content.contains("4-two"));
//            assertTrue(content.contains("4-three"));
//            assertTrue(content.contains("\"id\": \"4-two\""));
//            assertTrue(content.contains("\"entityReference\":"));
//            assertTrue(content.contains("\\/myPrefix4\\/4-two"));
//            assertTrue(content.contains(TestData.PREFIX6));
//            assertTrue(content.contains("6-two"));
//            assertTrue(content.contains("\"id\": \"6-two\""));
//        } catch (Exception e) {
//            fail("Could not get content: " + e.getMessage());
//        }
//    }

    // TODO test batch post/head/put/delete

}
