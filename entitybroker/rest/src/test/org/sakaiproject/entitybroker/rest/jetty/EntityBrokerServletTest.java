/**
 * $Id$
 * $URL$
 * DSpaceKernelServletFilterTest.java - DSpace2 - Oct 30, 2008 1:59:18 PM - azeckoski
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

package org.sakaiproject.entitybroker.rest.jetty;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.mortbay.jetty.testing.HttpTester;
import org.mortbay.jetty.testing.ServletTester;

import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.sakaiproject.entitybroker.providers.EntityRequestHandler;
import org.sakaiproject.entitybroker.rest.ServiceTestManager;
import org.sakaiproject.entitybroker.util.http.HttpRESTUtils;


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
        fireRequest(uri, null, null);
    }

    /**
     * Fires off a request using Jetty to the given uri (uses GET by default),
     * also resets the request afterward so it can be used again if desired
     * @param uri any uri which you want to test (should be valid for the test)
     * @param method (optional) the method to use, if null then GET
     */
    protected void fireRequest(String uri, String method) {
        fireRequest(uri, null, null);
    }

    /**
     * Fires off a request using Jetty to the given uri (uses GET by default),
     * also resets the request afterward so it can be used again if desired
     * @param uri any uri which you want to test (should be valid for the test)
     * @param method (optional) the method to use, if null then GET
     * @param params (optional) params to append to the end of the uri
     */
    protected void fireRequest(String uri, String method, Map<String, String> params) {
        if (method == null || "".equals(null)) {
            method = "GET";
        }
        this.response = new HttpTester(); // build a new response
        this.request.setMethod( method.toUpperCase() );
        if (params != null && params.size() > 0) {
            uri = HttpRESTUtils.mergeQueryStringWithParams(uri, params);
        }
        this.request.setURI(uri);
        try {
            this.response.parse(this.tester.getResponses(this.request.generate()));
        } catch (Exception e) {
            fail("Could not parse the response to the request ("+uri+"): " + e);
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
        String url = DIRECT_PREFIX + EntityRequestHandler.SLASH_BATCH + ".xml" + "?_refs=" + DIRECT_PREFIX + TestData.REF4;
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
        String url = DIRECT_PREFIX + EntityRequestHandler.SLASH_BATCH + ".json" + "?_refs=" + DIRECT_PREFIX + TestData.REF4 + ".xml";
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

        url = DIRECT_PREFIX + EntityRequestHandler.SLASH_BATCH + ".json" + "?_refs=" + DIRECT_PREFIX + TestData.REF4_3;
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

        url = DIRECT_PREFIX + EntityRequestHandler.SLASH_BATCH + ".json" + "?_refs=" + DIRECT_PREFIX + TestData.REF6_3;
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
        String url = DIRECT_PREFIX + EntityRequestHandler.SLASH_BATCH + ".xml" + "?_refs=" + DIRECT_PREFIX + TestData.REFU1 + ".xml";
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
        url = DIRECT_PREFIX + EntityRequestHandler.SLASH_BATCH + ".xml" + "?_refs=" 
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
                + "?_refs=" + DIRECT_PREFIX + TestData.REF4_two
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
                + "?_refs=" + DIRECT_PREFIX + TestData.REF4_two
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
                + "?_refs=" + DIRECT_PREFIX + TestData.SPACE4
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
                + "?_refs=" + DIRECT_PREFIX + TestData.SPACE4
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
                + "?_refs=" + DIRECT_PREFIX + TestData.SPACE4
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


    @Test
    public void testBatchNew() {
        // now fire the request
        String newURL = DIRECT_PREFIX + "/" + TestData.PREFIX6 + "/new";
        String url = DIRECT_PREFIX + EntityRequestHandler.SLASH_BATCH 
                + "?_refs=" + newURL + "," + newURL + "," + newURL;
        Map<String, String> params = new HashMap<String, String>();
        params.put("ref0.id", "AZ0");
        params.put("ref1.id", "AZ1");
        params.put("ref2.id", "AZ2");
        params.put("ref0.stuff", "Aaron");
        params.put("ref1.stuff", "Becky");
        params.put("ref2.stuff", "Minerva");
        params.put("number", "11");

        assertEquals(4, td.entityProvider6.myEntities.size());

        fireRequest(url, "POST", params);

        try {
            String content = this.response.getContent();
            assertNotNull(content);
            assertTrue(content.contains("ref0"));
            assertTrue(content.contains("ref1"));
            assertTrue(content.contains("ref2"));
            assertTrue(content.contains("\"status\":"));
            assertTrue(content.contains("201"));
            assertTrue(content.contains("\"headers\":"));
            assertTrue(content.contains("\"reference\":"));
        } catch (Exception e) {
            fail("Could not get content: " + e.getMessage());
        }

        assertEquals(7, td.entityProvider6.myEntities.size());
        assertNotNull(td.entityProvider6.myEntities.get("AZ0"));
        assertNotNull(td.entityProvider6.myEntities.get("AZ1"));
        assertNotNull(td.entityProvider6.myEntities.get("AZ2"));
        assertEquals("Aaron", td.entityProvider6.myEntities.get("AZ0").getStuff());
        assertEquals("Becky", td.entityProvider6.myEntities.get("AZ1").getStuff());
        assertEquals("Minerva", td.entityProvider6.myEntities.get("AZ2").getStuff());
        assertEquals(11, td.entityProvider6.myEntities.get("AZ0").getNumber());
        assertEquals(11, td.entityProvider6.myEntities.get("AZ1").getNumber());
        assertEquals(11, td.entityProvider6.myEntities.get("AZ2").getNumber());
    }

    // TODO need to work on a way to actually call the other webapps, it is not actually possible with forward/include

//    @Test
//    public void testBatchAllInvalidJSON() {
//        // now fire the request
//        String url = DIRECT_PREFIX + EntityRequestHandler.SLASH_BATCH + ".json" 
//                + "?_refs=/XXXXX/XXXX/XXX,/YYYYY/YYYY,/ZZZZZ";
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
