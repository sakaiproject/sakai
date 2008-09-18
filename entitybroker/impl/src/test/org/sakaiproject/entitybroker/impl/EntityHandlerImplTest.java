/**
 * $Id$
 * $URL$
 * EntityHandlerImplTest.java - entity-broker - Apr 6, 2008 12:08:39 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.entitybroker.impl;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityRequestHandler;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.mocks.ActionsEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.MockEBHttpServletRequest;
import org.sakaiproject.entitybroker.mocks.data.MyEntity;
import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Testing the central logic of the entity handler
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EntityHandlerImplTest extends TestCase {

   protected EntityHandlerImpl entityHandler;
   private TestData td;

   @Override
   protected void setUp() throws Exception {
      super.setUp();
      // setup things
      td = new TestData();

      entityHandler = new ServiceTestManager(td).entityRequestHandler;
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.EntityHandlerImpl#handleEntityAccess(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String)}.
    */
   public void testHandleEntityAccess() {
      MockEBHttpServletRequest req = null;
      MockHttpServletResponse res = null;

      // test valid entity (with access provider)
      req = new MockEBHttpServletRequest("GET", TestData.REF8);
      res = new MockHttpServletResponse();

      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_OK, res.getStatus());

      // test invalid prefix
      req = new MockEBHttpServletRequest("GET", "/fake/thing");
      res = new MockHttpServletResponse();

      try {
         entityHandler.handleEntityAccess(req, res, null);
         fail("Should have thrown exception");
      } catch (EntityException e) {
         assertNotNull(e.getMessage());
         assertEquals(HttpServletResponse.SC_NOT_IMPLEMENTED, e.responseCode);
      }

      // test invalid id
      req = new MockEBHttpServletRequest("GET", TestData.REF1_INVALID);
      res = new MockHttpServletResponse();

      try {
         entityHandler.handleEntityAccess(req, res, null);
         fail("Should have thrown exception");
      } catch (EntityException e) {
         assertNotNull(e.getMessage());
         assertEquals(TestData.REF1_INVALID, e.entityReference);
         assertEquals(HttpServletResponse.SC_NOT_FOUND, e.responseCode);
      }

      // test invalid path format
      req = new MockEBHttpServletRequest("GET", "xxxxxxxxxxxxxxxx");
      res = new MockHttpServletResponse();

      try {
         entityHandler.handleEntityAccess(req, res, null);
         fail("Should have thrown exception");
      } catch (EntityException e) {
         assertNotNull(e.getMessage());
         assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.responseCode);
      }

      // XML test valid resolveable entity
      req = new MockEBHttpServletRequest("GET", TestData.REF4 + "." + Formats.XML);
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_OK, res.getStatus());
      assertNotNull(res.getOutputStream());
      try {
         String xml = res.getContentAsString();
         assertNotNull(xml);
         assertTrue(xml.length() > 20);
         assertTrue(xml.contains(TestData.PREFIX4));
         assertTrue(xml.contains("<id>4-one</id>"));
         assertTrue(xml.contains(EntityEncodingManager.ENTITY_REFERENCE));
      } catch (UnsupportedEncodingException e) {
         fail("failure trying to get string content");
      }

      // JSON test valid resolveable entity
      req = new MockEBHttpServletRequest("GET", TestData.REF4 + "." + Formats.JSON);
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_OK, res.getStatus());
      assertNotNull(res.getOutputStream());
      try {
         String json = res.getContentAsString();
         assertNotNull(json);
         assertTrue(json.length() > 20);
         assertTrue(json.contains(TestData.PREFIX4));
         assertTrue(json.contains("\"id\":\"4-one\","));
         assertTrue(json.contains(EntityEncodingManager.ENTITY_REFERENCE));
      } catch (UnsupportedEncodingException e) {
         fail("failure trying to get string content");
      }

      // HTML test valid resolveable entity
      req = new MockEBHttpServletRequest("GET", TestData.REF4 + "." + Formats.HTML);
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_OK, res.getStatus());
      assertNotNull(res.getOutputStream());
      try {
         String html = res.getContentAsString();
         assertNotNull(html);
         assertTrue(html.length() > 20);
         assertTrue(html.contains(TestData.PREFIX4));
         assertTrue(html.contains(TestData.REF4));
      } catch (UnsupportedEncodingException e) {
         fail("failure trying to get string content");
      }

      // types that cannot handle the return requested
      req = new MockEBHttpServletRequest("GET", TestData.REF4 + ".xxxx");
      res = new MockHttpServletResponse();
      try {
         entityHandler.handleEntityAccess(req, res, null);
         fail("Should have thrown exception");
      } catch (EntityException e) {
         assertNotNull(e.getMessage());
         assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.responseCode);
      }
   }

   public void testAccessEntitySpace() {
      MockEBHttpServletRequest req = null;
      MockHttpServletResponse res = null;

      // test the REST and CRUD methods
      // XML testing

      // test get an entity space
      req = new MockEBHttpServletRequest("GET", TestData.SPACE6 + "." + Formats.XML);
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_OK, res.getStatus());
      assertNotNull(res.getOutputStream());
      try {
         String html = res.getContentAsString();
         assertNotNull(html);
         assertTrue(html.length() > 20);
         assertTrue(html.contains(TestData.PREFIX6));
         assertTrue(html.contains(TestData.PREFIX6 + "_collection"));
         assertTrue(html.contains(TestData.IDS6[0]));
         assertTrue(html.contains(TestData.IDS6[1]));
         assertTrue(html.contains(TestData.IDS6[2]));
         assertTrue(html.contains(TestData.IDS6[3]));
      } catch (UnsupportedEncodingException e) {
         fail("failure trying to get string content");
      }

      req = new MockEBHttpServletRequest("GET", TestData.SPACE6 + "." + Formats.JSON);
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_OK, res.getStatus());
      assertNotNull(res.getOutputStream());
      try {
         String html = res.getContentAsString();
         assertNotNull(html);
         assertTrue(html.length() > 20);
         assertTrue(html.contains(TestData.PREFIX6));
         assertTrue(html.contains(TestData.PREFIX6 + "_collection"));
         assertTrue(html.contains(TestData.IDS6[0]));
         assertTrue(html.contains(TestData.IDS6[1]));
         assertTrue(html.contains(TestData.IDS6[2]));
         assertTrue(html.contains(TestData.IDS6[3]));
      } catch (UnsupportedEncodingException e) {
         fail("failure trying to get string content");
      }
   }

   public void testHandleEntityAccessInputHTML() {
      MockEBHttpServletRequest req = null;
      MockHttpServletResponse res = null;

      // test the REST and CRUD methods
      // HTML testing

      // test creating an entity
      // invalid space is invalid
      req = new MockEBHttpServletRequest("POST", "/xxxxxxxxxxxxxx/new");
      res = new MockHttpServletResponse();
      try {
         entityHandler.handleEntityAccess(req, res, null);
         fail("Should have thrown exception");
      } catch (EntityException e) {
         assertNotNull(e.getMessage());
         assertEquals(HttpServletResponse.SC_NOT_IMPLEMENTED, e.responseCode);
      }

      // create without data is invalid
      req = new MockEBHttpServletRequest("POST", TestData.SPACE6 + "/new");
      res = new MockHttpServletResponse();
      try {
         entityHandler.handleEntityAccess(req, res, null);
         fail("Should have thrown exception");
      } catch (EntityException e) {
         assertNotNull(e.getMessage());
         assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.responseCode);
      }

      req = new MockEBHttpServletRequest("POST", TestData.SPACE6 + "/new");
      req.addParameter("stuff", "TEST"); // now fill in the fields to create the entity in the request (html)
      req.addParameter("number", "5");
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_CREATED, res.getStatus());
      assertNotNull(res.getHeader(EntityRequestHandler.HEADER_ENTITY_ID));
      assertNotNull(res.getHeader(EntityRequestHandler.HEADER_ENTITY_REFERENCE));
      assertNotNull(res.getHeader(EntityRequestHandler.HEADER_ENTITY_URL));
      String entityId = (String) res.getHeader(EntityRequestHandler.HEADER_ENTITY_ID);
      assertTrue( td.entityProvider6.myEntities.containsKey(entityId) );
      MyEntity me = td.entityProvider6.myEntities.get(entityId);
      assertNotNull(me);
      assertEquals("TEST", me.getStuff());
      assertEquals(5, me.getNumber());
      // test that the entityId is being returned in the response as requested by Nico
      assertNotNull(res.getOutputStream());
      try {
         String content = res.getContentAsString();
         assertNotNull(content);
         assertTrue(content.contains(entityId));
      } catch (UnsupportedEncodingException e) {
         fail("failure trying to get string content");
      }

      // make sure the .html works
      req = new MockEBHttpServletRequest("POST", TestData.SPACE6 + "/new" + "." + Formats.HTML);
      req.addParameter("stuff", "TEST2"); // now fill in the fields to create the entity in the request (html)
      req.addParameter("number", "6");
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_CREATED, res.getStatus());
      assertNotNull(res.getHeader(EntityRequestHandler.HEADER_ENTITY_ID));
      assertNotNull(res.getHeader(EntityRequestHandler.HEADER_ENTITY_REFERENCE));
      assertNotNull(res.getHeader(EntityRequestHandler.HEADER_ENTITY_URL));
      String entityId2 = (String) res.getHeader(EntityRequestHandler.HEADER_ENTITY_ID);
      assertTrue( td.entityProvider6.myEntities.containsKey(entityId2) );
      MyEntity me2 = td.entityProvider6.myEntities.get(entityId2);
      assertNotNull(me2);
      assertEquals("TEST2", me2.getStuff());
      assertEquals(6, me2.getNumber());

      // test modifying an entity
      // modify without data is invalid
      req = new MockEBHttpServletRequest("PUT", TestData.SPACE6 + "/" + entityId);
      res = new MockHttpServletResponse();
      try {
         entityHandler.handleEntityAccess(req, res, null);
         fail("Should have thrown exception");
      } catch (EntityException e) {
         assertNotNull(e.getMessage());
         assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.responseCode);
      }

      // invalid id is invalid
      req = new MockEBHttpServletRequest("PUT", TestData.SPACE6 + "/xxxxxxx");
      res = new MockHttpServletResponse();
      try {
         entityHandler.handleEntityAccess(req, res, null);
         fail("Should have thrown exception");
      } catch (EntityException e) {
         assertNotNull(e.getMessage());
         assertEquals(HttpServletResponse.SC_NOT_FOUND, e.responseCode);
      }

      req = new MockEBHttpServletRequest("PUT", TestData.SPACE6 + "/" + entityId);
      req.addParameter("stuff", "TEST-PUT"); // now fill in the fields to create the entity in the request (html)
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_NO_CONTENT, res.getStatus());
      assertNull(res.getHeader(EntityRequestHandler.HEADER_ENTITY_ID));
      assertNotNull(res.getHeader(EntityRequestHandler.HEADER_ENTITY_REFERENCE));
      assertNotNull(res.getHeader(EntityRequestHandler.HEADER_ENTITY_URL));
      assertTrue( td.entityProvider6.myEntities.containsKey(entityId) );
      me = td.entityProvider6.myEntities.get(entityId);
      assertNotNull(me);
      assertEquals("TEST-PUT", me.getStuff());
      assertEquals(5, me.getNumber());

      // make sure the .html works
      req = new MockEBHttpServletRequest("PUT", TestData.SPACE6 + "/" + entityId2 + "." + Formats.HTML);
      req.addParameter("stuff", "TEST-PUT2"); // now fill in the fields to create the entity in the request (html)
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_NO_CONTENT, res.getStatus());
      assertNull(res.getHeader(EntityRequestHandler.HEADER_ENTITY_ID));
      assertNotNull(res.getHeader(EntityRequestHandler.HEADER_ENTITY_REFERENCE));
      assertNotNull(res.getHeader(EntityRequestHandler.HEADER_ENTITY_URL));
      assertTrue( td.entityProvider6.myEntities.containsKey(entityId2) );
      me2 = td.entityProvider6.myEntities.get(entityId2);
      assertNotNull(me2);
      assertEquals("TEST-PUT2", me2.getStuff());
      assertEquals(6, me2.getNumber());

      // test deleting an entity

      // space delete is invalid
      req = new MockEBHttpServletRequest("DELETE", TestData.SPACE6);
      res = new MockHttpServletResponse();
      try {
         entityHandler.handleEntityAccess(req, res, null);
         fail("Should have thrown exception");
      } catch (EntityException e) {
         assertNotNull(e.getMessage());
         assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.responseCode);
      }

      // invalid id is invalid
      req = new MockEBHttpServletRequest("DELETE", TestData.SPACE6 + "/xxxxxxx");
      res = new MockHttpServletResponse();
      try {
         entityHandler.handleEntityAccess(req, res, null);
         fail("Should have thrown exception");
      } catch (EntityException e) {
         assertNotNull(e.getMessage());
         assertEquals(HttpServletResponse.SC_NOT_FOUND, e.responseCode);
      }

      // entity delete is allowed
      assertTrue( td.entityProvider6.myEntities.containsKey(TestData.IDS6[3]) );
      req = new MockEBHttpServletRequest("DELETE", TestData.REF6_4);
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_NO_CONTENT, res.getStatus());
      assertFalse( td.entityProvider6.myEntities.containsKey(TestData.IDS6[3]) );

      assertTrue( td.entityProvider6.myEntities.containsKey(entityId2) );
      req = new MockEBHttpServletRequest("DELETE", TestData.SPACE6 + "/" + entityId2 + "." + Formats.HTML);
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_NO_CONTENT, res.getStatus());
      assertFalse( td.entityProvider6.myEntities.containsKey(entityId2) );

   }

   public void testHandleEntityAccessRESTXML() {
      MockEBHttpServletRequest req = null;
      MockHttpServletResponse res = null;

      // test the REST and CRUD methods
      // XML testing

      // test creating an entity
      req = new MockEBHttpServletRequest("POST", TestData.SPACE6 + "/new" + "." + Formats.XML);
      req.setContent(
            makeUTF8Bytes("<"+TestData.PREFIX6+"><stuff>TEST</stuff><number>5</number></"+TestData.PREFIX6+">")
         );
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_CREATED, res.getStatus());
      assertNotNull(res.getHeader(EntityRequestHandler.HEADER_ENTITY_ID));
      assertNotNull(res.getHeader(EntityRequestHandler.HEADER_ENTITY_REFERENCE));
      assertNotNull(res.getHeader(EntityRequestHandler.HEADER_ENTITY_URL));
      String entityId = (String) res.getHeader(EntityRequestHandler.HEADER_ENTITY_ID);
      assertTrue( td.entityProvider6.myEntities.containsKey(entityId) );
      MyEntity me = td.entityProvider6.myEntities.get(entityId);
      assertNotNull(me);
      assertEquals("TEST", me.getStuff());
      assertEquals(5, me.getNumber());

      // test modifying an entity
      req = new MockEBHttpServletRequest("PUT", TestData.SPACE6 + "/" + entityId + "." + Formats.XML);
      req.setContent(
            makeUTF8Bytes("<"+TestData.PREFIX6+"><stuff>TEST-PUT</stuff></"+TestData.PREFIX6+">")
         );
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_NO_CONTENT, res.getStatus());
      assertNull(res.getHeader(EntityRequestHandler.HEADER_ENTITY_ID));
      assertNotNull(res.getHeader(EntityRequestHandler.HEADER_ENTITY_REFERENCE));
      assertNotNull(res.getHeader(EntityRequestHandler.HEADER_ENTITY_URL));
      assertTrue( td.entityProvider6.myEntities.containsKey(entityId) );
      me = td.entityProvider6.myEntities.get(entityId);
      assertNotNull(me);
      assertEquals("TEST-PUT", me.getStuff());
      assertEquals(5, me.getNumber());

      // test deleting an entity (extension should have no effect)
      assertTrue( td.entityProvider6.myEntities.containsKey(entityId) );
      req = new MockEBHttpServletRequest("DELETE", TestData.SPACE6 + "/" + entityId + "." + Formats.XML);
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_NO_CONTENT, res.getStatus());
      assertFalse( td.entityProvider6.myEntities.containsKey(entityId) );

   }

   public void testHandleEntityAccessRESTJSON() {
      MockEBHttpServletRequest req = null;
      MockHttpServletResponse res = null;

      // test the REST and CRUD methods
      // JSON testing

      // test creating an entity
      req = new MockEBHttpServletRequest("POST", TestData.SPACE6 + "/new" + "." + Formats.JSON);
      req.setContent(
            makeUTF8Bytes("{\""+TestData.PREFIX6+"\" : { \"stuff\" : \"TEST\", \"number\" : 5 }}")
         );
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_CREATED, res.getStatus());
      assertNotNull(res.getHeader(EntityRequestHandler.HEADER_ENTITY_ID));
      assertNotNull(res.getHeader(EntityRequestHandler.HEADER_ENTITY_REFERENCE));
      assertNotNull(res.getHeader(EntityRequestHandler.HEADER_ENTITY_URL));
      String entityId = (String) res.getHeader(EntityRequestHandler.HEADER_ENTITY_ID);
      assertTrue( td.entityProvider6.myEntities.containsKey(entityId) );
      MyEntity me = td.entityProvider6.myEntities.get(entityId);
      assertNotNull(me);
      assertEquals("TEST", me.getStuff());
      assertEquals(5, me.getNumber());

      // test modifying an entity
      req = new MockEBHttpServletRequest("PUT", TestData.SPACE6 + "/" + entityId + "." + Formats.JSON);
      req.setContent(
            makeUTF8Bytes("{\""+TestData.PREFIX6+"\" : { \"stuff\" : \"TEST-PUT\" }}")
         );
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_NO_CONTENT, res.getStatus());
      assertNull(res.getHeader(EntityRequestHandler.HEADER_ENTITY_ID));
      assertNotNull(res.getHeader(EntityRequestHandler.HEADER_ENTITY_REFERENCE));
      assertNotNull(res.getHeader(EntityRequestHandler.HEADER_ENTITY_URL));
      assertTrue( td.entityProvider6.myEntities.containsKey(entityId) );
      me = td.entityProvider6.myEntities.get(entityId);
      assertNotNull(me);
      assertEquals("TEST-PUT", me.getStuff());
      assertEquals(5, me.getNumber());

      // test deleting an entity (extension should have no effect)
      assertTrue( td.entityProvider6.myEntities.containsKey(entityId) );
      req = new MockEBHttpServletRequest("DELETE", TestData.SPACE6 + "/" + entityId + "." + Formats.JSON);
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_NO_CONTENT, res.getStatus());
      assertFalse( td.entityProvider6.myEntities.containsKey(entityId) );

   }

   public void testHandleEntityAccessDescribe() {
      MockEBHttpServletRequest req = null;
      MockHttpServletResponse res = null;

      // test describe all entities
      req = new MockEBHttpServletRequest("GET", "/" + EntityRequestHandler.DESCRIBE);
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_OK, res.getStatus());
      assertNotNull(res.getOutputStream());
      try {
         String content = res.getContentAsString();
         assertNotNull(content);
         assertTrue(content.length() > 80);
         assertTrue(content.contains(TestData.PREFIX1));
         assertTrue(content.contains(TestData.PREFIX2));
         assertTrue(content.contains(TestData.PREFIX3));
         assertTrue(content.contains(TestData.PREFIX4));
         assertTrue(content.contains(TestData.PREFIX5));
         assertTrue(content.contains(TestData.PREFIX6));
         assertTrue(content.contains(TestData.PREFIX7));
         assertTrue(content.contains(TestData.PREFIX8));
      } catch (UnsupportedEncodingException e) {
         fail("failure trying to get string content");
      }

      req = new MockEBHttpServletRequest("GET", "/" + EntityRequestHandler.DESCRIBE + "." + Formats.XML);
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_OK, res.getStatus());
      assertNotNull(res.getOutputStream());
      try {
         String content = res.getContentAsString();
         assertNotNull(content);
         assertTrue(content.length() > 80);
         assertTrue(content.contains(TestData.PREFIX1));
         assertTrue(content.contains(TestData.PREFIX2));
         assertTrue(content.contains(TestData.PREFIX3));
         assertTrue(content.contains(TestData.PREFIX4));
         assertTrue(content.contains(TestData.PREFIX5));
         assertTrue(content.contains(TestData.PREFIX6));
         assertTrue(content.contains(TestData.PREFIX7));
         assertTrue(content.contains(TestData.PREFIX8));
      } catch (UnsupportedEncodingException e) {
         fail("failure trying to get string content");
      }

      // test describe single entity space
      req = new MockEBHttpServletRequest("GET", "/" + TestData.PREFIX1 + "/" + EntityRequestHandler.DESCRIBE);
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_OK, res.getStatus());
      assertNotNull(res.getOutputStream());
      try {
         String content = res.getContentAsString();
         assertNotNull(content);
         assertTrue(content.length() > 80);
         assertTrue(content.contains(TestData.PREFIX1));
      } catch (UnsupportedEncodingException e) {
         fail("failure trying to get string content");
      }

      // XML
      req = new MockEBHttpServletRequest("GET", "/" + TestData.PREFIX1 + "/" + EntityRequestHandler.DESCRIBE + "." + Formats.XML);
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_OK, res.getStatus());
      assertNotNull(res.getOutputStream());
      try {
         String content = res.getContentAsString();
         assertNotNull(content);
         assertTrue(content.length() > 80);
         assertTrue(content.contains(TestData.PREFIX1));
      } catch (UnsupportedEncodingException e) {
         fail("failure trying to get string content");
      }

      req = new MockEBHttpServletRequest("GET", "/" + TestData.PREFIX4 + "/" + EntityRequestHandler.DESCRIBE);
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_OK, res.getStatus());
      assertNotNull(res.getOutputStream());
      try {
         String content = res.getContentAsString();
         assertNotNull(content);
         assertTrue(content.length() > 80);
         assertTrue(content.contains(TestData.PREFIX4));
      } catch (UnsupportedEncodingException e) {
         fail("failure trying to get string content");
      }

      req = new MockEBHttpServletRequest("GET", "/" + TestData.PREFIX6 + "/" + EntityRequestHandler.DESCRIBE);
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_OK, res.getStatus());
      assertNotNull(res.getOutputStream());
      try {
         String content = res.getContentAsString();
         assertNotNull(content);
         assertTrue(content.length() > 80);
         assertTrue(content.contains(TestData.PREFIX6));
      } catch (UnsupportedEncodingException e) {
         fail("failure trying to get string content");
      }

      // XML
      req = new MockEBHttpServletRequest("GET", "/" + TestData.PREFIX6 + "/" + EntityRequestHandler.DESCRIBE + "." + Formats.XML);
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_OK, res.getStatus());
      assertNotNull(res.getOutputStream());
      try {
         String content = res.getContentAsString();
         assertNotNull(content);
         assertTrue(content.length() > 80);
         assertTrue(content.contains(TestData.PREFIX6));
      } catch (UnsupportedEncodingException e) {
         fail("failure trying to get string content");
      }

      // test an entity which is describeable
      req = new MockEBHttpServletRequest("GET", "/" + TestData.PREFIX7 + "/" + EntityRequestHandler.DESCRIBE);
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_OK, res.getStatus());
      assertNotNull(res.getOutputStream());
      try {
         String content = res.getContentAsString();
         assertNotNull(content);
         assertTrue(content.length() > 80);
         assertTrue(content.contains(TestData.PREFIX7));
         assertTrue(content.contains("describe-prefix test description of an entity"));
         assertTrue(content.contains("This is a test description of Createable"));
      } catch (UnsupportedEncodingException e) {
         fail("failure trying to get string content");
      }

      // XML
      req = new MockEBHttpServletRequest("GET", "/" + TestData.PREFIX7 + "/" + EntityRequestHandler.DESCRIBE + "." + Formats.XML);
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_OK, res.getStatus());
      assertNotNull(res.getOutputStream());
      try {
         String content = res.getContentAsString();
         assertNotNull(content);
         assertTrue(content.length() > 80);
         assertTrue(content.contains(TestData.PREFIX7));
         assertTrue(content.contains("<description>"));
         assertTrue(content.contains("describe-prefix test description of an entity"));
         assertTrue(content.contains("This is a test description of Createable"));
      } catch (UnsupportedEncodingException e) {
         fail("failure trying to get string content");
      }

      // test an entity which is DescribePropertiesable
      req = new MockEBHttpServletRequest("GET", "/" + TestData.PREFIX8 + "/" + EntityRequestHandler.DESCRIBE);
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_OK, res.getStatus());
      assertNotNull(res.getOutputStream());
      try {
         String content = res.getContentAsString();
         assertNotNull(content);
         assertTrue(content.length() > 80);
         assertTrue(content.contains(TestData.PREFIX8));
         assertTrue(content.contains("CUSTOM description"));
         assertTrue(content.contains("CUSTOM Deleteable"));
      } catch (UnsupportedEncodingException e) {
         fail("failure trying to get string content");
      }

      // XML
      req = new MockEBHttpServletRequest("GET", "/" + TestData.PREFIX8 + "/" + EntityRequestHandler.DESCRIBE + "." + Formats.XML);
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_OK, res.getStatus());
      assertNotNull(res.getOutputStream());
      try {
         String content = res.getContentAsString();
         assertNotNull(content);
         assertTrue(content.length() > 80);
         assertTrue(content.contains(TestData.PREFIX8));
         assertTrue(content.contains("<description>"));
         assertTrue(content.contains("CUSTOM description"));
         assertTrue(content.contains("CUSTOM Deleteable"));
      } catch (UnsupportedEncodingException e) {
         fail("failure trying to get string content");
      }

      // test an entity which is DescribePropertiesable
      req = new MockEBHttpServletRequest("GET", "/" + TestData.PREFIXA1 + "/" + EntityRequestHandler.DESCRIBE);
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_OK, res.getStatus());
      assertNotNull(res.getOutputStream());
      try {
         String content = res.getContentAsString();
         assertNotNull(content);
         assertTrue(content.length() > 80);
         assertTrue(content.contains(TestData.PREFIXA1));
         assertTrue(content.contains("double action"));
         assertTrue(content.contains("xxx action"));
      } catch (UnsupportedEncodingException e) {
         fail("failure trying to get string content");
      }

      // XML
      req = new MockEBHttpServletRequest("GET", "/" + TestData.PREFIXA1 + "/" + EntityRequestHandler.DESCRIBE + "." + Formats.XML);
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_OK, res.getStatus());
      assertNotNull(res.getOutputStream());
      try {
         String content = res.getContentAsString();
         assertNotNull(content);
         assertTrue(content.length() > 80);
         assertTrue(content.contains(TestData.PREFIXA1));
         assertTrue(content.contains("<description>"));
         assertTrue(content.contains("double action"));
         assertTrue(content.contains("xxx action"));
      } catch (UnsupportedEncodingException e) {
         fail("failure trying to get string content");
      }

      // test invalid describe
      req = new MockEBHttpServletRequest("GET", "/" + TestData.PREFIX9 + "/" + EntityRequestHandler.DESCRIBE);
      res = new MockHttpServletResponse();
      try {
         entityHandler.handleEntityAccess(req, res, null);
         fail("Should have thrown exception");
      } catch (EntityException e) {
         assertNotNull(e.getMessage());
         assertEquals(HttpServletResponse.SC_NOT_IMPLEMENTED, e.responseCode);
      }      
      
   }


   public void testRequestStorageAware() {
      MockEBHttpServletRequest req = null;
      MockHttpServletResponse res = null;

      // test doing a request to see what comes back
      // currently this is just making sure the stuff gets set and does not die
      req = new MockEBHttpServletRequest("GET", TestData.REFA + ".xml");
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_OK, res.getStatus());
      assertNotNull(res.getOutputStream());
      try {
         String content = res.getContentAsString();
         assertNotNull(content);
         assertTrue(content.length() > 80);
         assertTrue(content.contains(TestData.PREFIXA));
      } catch (UnsupportedEncodingException e) {
         fail("failure trying to get string content");
      }

      // TODO we need a better way to test this stuff
   }

   public void testCustomActions() {
      ActionsEntityProviderMock actionProvider = td.entityProviderA1;
      MockEBHttpServletRequest req = null;
      MockHttpServletResponse res = null;
      String action = null;

      // double
      MyEntity me = (MyEntity) actionProvider.getEntity( new EntityReference(TestData.REFA1_2) );
      int num = me.getNumber();
      action = "double";
      req = new MockEBHttpServletRequest("GET", TestData.REFA1_2 + "/" + action + "." + Formats.XML);
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_OK, res.getStatus());
      assertNotNull(res.getOutputStream());
      try {
         String content = res.getContentAsString();
         assertNotNull(content);
         assertTrue(content.length() > 80);
         assertTrue(content.contains(TestData.PREFIXA1));
         assertTrue(content.contains((num*2)+"</number>"));
      } catch (UnsupportedEncodingException e) {
         fail("failure trying to get string content");
      }

      // xxx
      MyEntity me1 = (MyEntity) actionProvider.getEntity( new EntityReference(TestData.REFA1_2) );
      assertFalse("xxx".equals(me1.extra));
      assertFalse("xxx".equals(me1.getStuff()));

      action = "xxx";
      req = new MockEBHttpServletRequest("POST", TestData.REFA1_2 + "/" + action + "." + Formats.XML);
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_OK, res.getStatus());
      assertNotNull(res.getOutputStream());
      try {
         String content = res.getContentAsString();
         assertNotNull(content);
         assertTrue(content.length() == 0);
      } catch (UnsupportedEncodingException e) {
         fail("failure trying to get string content");
      }

      MyEntity xxxMe = (MyEntity) actionProvider.getEntity( new EntityReference(TestData.REFA1_2) );
      assertEquals(me1.getId(), xxxMe.getId());
      assertTrue("xxx".equals(xxxMe.extra));
      assertTrue("xxx".equals(xxxMe.getStuff()));

      // invalid method (GET), should be post
      req = new MockEBHttpServletRequest("GET", TestData.REFA1_2 + "/" + "xxx" + "." + Formats.XML);
      res = new MockHttpServletResponse();
      try {
         entityHandler.handleEntityAccess(req, res, null);
         fail("should have thrown exeception");
      } catch (EntityException e) {
         assertNotNull(e.getMessage());
      }

      // clear
      assertEquals(2, actionProvider.myEntities.size());

      action = "clear";
      req = new MockEBHttpServletRequest("POST", TestData.SPACEA1 + "/" + action);
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_OK, res.getStatus());
      assertNotNull(res.getOutputStream());
      try {
         String content = res.getContentAsString();
         assertNotNull(content);
         assertTrue(content.length() == 0);
      } catch (UnsupportedEncodingException e) {
         fail("failure trying to get string content");
      }

      assertEquals(0, actionProvider.myEntities.size());

      // exceptions

      // invalid action
      req = new MockEBHttpServletRequest("GET", TestData.REFA1_2 + "/" + "INVALID");
      res = new MockHttpServletResponse();
      try {
         entityHandler.handleEntityAccess(req, res, null);
         fail("should have thrown exeception");
      } catch (EntityException e) {
         assertNotNull(e.getMessage());
      }

      // invalid method (GET), should be post
      req = new MockEBHttpServletRequest("GET", TestData.SPACEA1 + "/" + "clear");
      res = new MockHttpServletResponse();
      try {
         entityHandler.handleEntityAccess(req, res, null);
         fail("should have thrown exeception");
      } catch (EntityException e) {
         assertNotNull(e.getMessage());
      }

      req = new MockEBHttpServletRequest("PUT", TestData.SPACEA1 + "/" + "clear");
      res = new MockHttpServletResponse();
      try {
         entityHandler.handleEntityAccess(req, res, null);
         fail("should have thrown exeception");
      } catch (EntityException e) {
         assertNotNull(e.getMessage());
      }

      req = new MockEBHttpServletRequest("DELETE", TestData.SPACEA1 + "/" + "clear");
      res = new MockHttpServletResponse();
      try {
         entityHandler.handleEntityAccess(req, res, null);
         fail("should have thrown exeception");
      } catch (EntityException e) {
         assertNotNull(e.getMessage());
      }
   }

   public void testURLRedirects() {
      MockEBHttpServletRequest req = null;
      MockHttpServletResponse res = null;
      String redirectURL = null;
      String forwardURL = null;

      // testing a few requests to make sure they work right
      req = new MockEBHttpServletRequest("GET", TestData.SPACEU1 + "/123/AZ/go");
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, res.getStatus()); // redirect
      assertNotNull(res.getOutputStream());
      redirectURL = res.getRedirectedUrl();
      forwardURL = res.getForwardedUrl();
      assertNotNull(redirectURL);
      assertNull(forwardURL);
      assertEquals("http://caret.cam.ac.uk/?prefix=" + TestData.PREFIXU1 + "&thing=AZ", redirectURL);

      req = new MockEBHttpServletRequest("GET", TestData.SPACEU1 + "/xml/123");
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_OK, res.getStatus()); // forward
      assertNotNull(res.getOutputStream());
      redirectURL = res.getRedirectedUrl();
      forwardURL = res.getForwardedUrl();
      assertNull(redirectURL);
      assertNotNull(forwardURL);
      assertEquals(TemplateParseUtil.DIRECT_PREFIX + TestData.SPACEU1+"/123.xml", forwardURL);

      // test the special ones
      req = new MockEBHttpServletRequest("GET", TestData.SPACEU1 + "/going/nowhere");
      res = new MockHttpServletResponse();
      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_OK, res.getStatus()); // forward
      assertNotNull(res.getOutputStream());
      redirectURL = res.getRedirectedUrl();
      forwardURL = res.getForwardedUrl();
      assertNull(redirectURL);
      assertNull(forwardURL);
      assertNotNull(res.getOutputStream());
      try {
         String content = res.getContentAsString();
         assertNotNull(content);
         assertTrue(content.length() == 0);
      } catch (UnsupportedEncodingException e) {
         fail("failure trying to get string content");
      }

      req = new MockEBHttpServletRequest("GET", TestData.SPACEU1 + "/keep/moving");
      res = new MockHttpServletResponse();
      try {
         entityHandler.handleEntityAccess(req, res, null);
         fail("should have died");
      } catch (EntityException e) {
         assertNotNull(e.getMessage());
      }

   }


   /**
    * Convenience method for making byte content encoded into UTF-8
    */
   private byte[] makeUTF8Bytes(String string) {
      byte[] bytes;
      try {
         bytes = string.getBytes(Formats.UTF_8);
      } catch (UnsupportedEncodingException e) {
         bytes = string.getBytes();
      }
      return bytes;
   }
}
