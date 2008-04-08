/**
 * $Id$
 * $URL$
 * EntityHandlerImplTest.java - entity-broker - Apr 6, 2008 12:08:39 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.impl.test;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.IdEntityReference;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.OutputHTMLable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.OutputJSONable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.OutputXMLable;
import org.sakaiproject.entitybroker.entityprovider.extension.BasicEntity;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.impl.EntityHandlerImpl;
import org.sakaiproject.entitybroker.impl.entityprovider.EntityProviderManagerImpl;
import org.sakaiproject.entitybroker.impl.test.mocks.FakeServerConfigurationService;
import org.sakaiproject.entitybroker.mocks.HttpServletAccessProviderManagerMock;
import org.sakaiproject.entitybroker.mocks.MockHttpServletRequest;
import org.sakaiproject.entitybroker.mocks.data.MyEntity;
import org.sakaiproject.entitybroker.mocks.data.TestData;
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

      EntityProviderManagerImpl epm = new EntityProviderManagerImplTest().makeEntityProviderManager(td);

      entityHandler = new EntityHandlerImpl();
      entityHandler.setEntityProviderManager( epm );
      entityHandler.setAccessProviderManager( new HttpServletAccessProviderManagerMock() );
      entityHandler.setRequestGetter( epm.getRequestGetter() );
      entityHandler.setServerConfigurationService( new FakeServerConfigurationService() );

   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.EntityHandlerImpl#entityExists(java.lang.String)}.
    */
   public void testEntityExists() {
      boolean exists = false;

      exists = entityHandler.entityExists(TestData.REF1);
      assertTrue(exists);

      exists = entityHandler.entityExists(TestData.REF1_1);
      assertTrue(exists);

      exists = entityHandler.entityExists(TestData.REF2);
      assertTrue(exists);

      // test that invalid id with valid prefix does not pass
      exists = entityHandler.entityExists(TestData.REF1_INVALID);
      assertFalse(exists);

      // test that unregistered ref does not pass
      exists = entityHandler.entityExists(TestData.REF9);
      assertFalse(exists);

      // test that invalid ref causes exception
      try {
         exists = entityHandler.entityExists(TestData.INVALID_REF);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.EntityHandlerImpl#getEntityURL(java.lang.String)}.
    */
   public void testGetEntityURL() {
      String url = null;

      url = entityHandler.getEntityURL(TestData.REF1);
      assertEquals(TestData.URL1, url);

      url = entityHandler.getEntityURL(TestData.REF2);
      assertEquals(TestData.URL2, url);

      url = entityHandler.getEntityURL(TestData.REF1_INVALID);

      try {
         url = entityHandler.getEntityURL(TestData.INVALID_REF);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.EntityHandlerImpl#getProvider(java.lang.String)}.
    */
   public void testGetProvider() {
      EntityProvider ep = null;

      // get valid providers
      ep = entityHandler.getProvider(TestData.REF1);
      assertNotNull(ep);
      assertEquals(td.entityProvider1, ep);

      ep = entityHandler.getProvider(TestData.REF3);
      assertNotNull(ep);
      assertEquals(td.entityProvider3, ep);

      // test unregistered provider returns null
      ep = entityHandler.getProvider(TestData.REF9);
      assertNull(ep);

      // ensure invalid prefix dies
      try {
         ep = entityHandler.getProvider(TestData.INVALID_REF);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.EntityHandlerImpl#parseReference(java.lang.String)}.
    */
   public void testParseReference() {
      EntityReference er = null;

      er = entityHandler.parseReference(TestData.REF1);
      assertNotNull(er);
      assertEquals(TestData.PREFIX1, er.prefix);
      assertTrue(er instanceof IdEntityReference);
      assertEquals(TestData.IDS1[0], ((IdEntityReference) er).id);

      er = entityHandler.parseReference(TestData.REF2);
      assertNotNull(er);
      assertEquals(TestData.PREFIX2, er.prefix);
      assertFalse(er instanceof IdEntityReference);

      // test parsing a defined reference
      er = entityHandler.parseReference(TestData.REF3);
      assertNotNull(er);
      assertEquals(TestData.PREFIX3, er.prefix);
      assertFalse(er instanceof IdEntityReference);

      // parsing of unregistered entity references returns null
      er = entityHandler.parseReference(TestData.REF9);
      assertNull(er);

      // parsing with nonexistent prefix returns null
      er = entityHandler.parseReference("/totallyfake/notreal");
      assertNull(er);

      try {
         er = entityHandler.parseReference(TestData.INVALID_REF);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

   public void testGetExtension() {
      assertEquals("xml", entityHandler.getExtension("/blah/yadda.xml"));
      assertEquals("json", entityHandler.getExtension("/blah/blah/yadda.json"));

      assertNull( entityHandler.getExtension("/blah/blah") );
      assertNull( entityHandler.getExtension("/blah/blah.") );
   }


   @SuppressWarnings("unchecked")
   public void testGetEntityObject() {
      Object entity = null;
      EntityReference ref = null;

      // first for resolveable
      ref = entityHandler.parseReference(TestData.REF4);
      assertNotNull(ref);
      entity = entityHandler.getEntityObject(ref);
      assertNotNull(entity);
      assertEquals(MyEntity.class, entity.getClass());
      assertEquals(TestData.entity4, entity);

      ref = entityHandler.parseReference(TestData.REF4_two);
      assertNotNull(ref);
      entity = entityHandler.getEntityObject(ref);
      assertNotNull(entity);
      assertEquals(MyEntity.class, entity.getClass());
      assertEquals(TestData.entity4_two, entity);

      // now for non-resolveable
      ref = entityHandler.parseReference(TestData.REF5);
      assertNotNull(ref);
      entity = entityHandler.getEntityObject(ref);
      assertNotNull(entity);
      assertEquals(BasicEntity.class, entity.getClass());
      assertEquals(TestData.REF5, ((BasicEntity)entity).getReference());
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.EntityHandlerImpl#getClassFromCollection(java.util.Collection)}.
    */
   @SuppressWarnings("unchecked")
   public void testGetClassFromCollection() {
      Class<?> result = null;

      // null returns object class
      result = entityHandler.getClassFromCollection(null);
      assertNotNull(result);
      assertEquals(Object.class, result);

      // empty collection is always object
      result = entityHandler.getClassFromCollection( new ArrayList<String>() );
      assertNotNull(result);
      assertEquals(Object.class, result);

      // NOTE: Cannot get real type from empty collections

      // try with collections that have things in them
      List<Object> l = new ArrayList<Object>();
      l.add(new String("testing"));
      result = entityHandler.getClassFromCollection(l);
      assertNotNull(result);
      assertEquals(String.class, result);

      HashSet<Object> s = new HashSet<Object>();
      s.add(new Double(22.0));
      result = entityHandler.getClassFromCollection(s);
      assertNotNull(result);
      assertEquals(Double.class, result);

      List v = new Vector<Object>();
      v.add( new Integer(30) );
      result = entityHandler.getClassFromCollection(v);
      assertNotNull(result);
      assertEquals(Integer.class, result);
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.EntityHandlerImpl#encodeToResponse(java.lang.Object, java.lang.String, java.lang.String, javax.servlet.http.HttpServletResponse)}.
    */
   public void testEncodeToResponse() {

      EntityReference ref = null;
      MockHttpServletRequest req = null;
      MockHttpServletResponse res = null;

      // JSON test valid resolveable entity
      req = new MockHttpServletRequest("GET", TestData.REF4);
      res = new MockHttpServletResponse();
      ref = entityHandler.parseReference(TestData.REF4);
      assertNotNull(ref);
      entityHandler.encodeToResponse(req, res, ref, OutputJSONable.EXTENSION);
      assertNotNull(res.getOutputStream());
      try {
         String json = res.getContentAsString();
         assertNotNull(json);
         assertTrue(json.length() > 20);
         assertTrue(json.contains(TestData.PREFIX4));
      } catch (UnsupportedEncodingException e) {
         fail("failure trying to get string content");
      }
      assertEquals(58, res.getContentLength());

      // XML test valid resolveable entity
      req = new MockHttpServletRequest("GET", TestData.REF4);
      res = new MockHttpServletResponse();
      ref = entityHandler.parseReference(TestData.REF4);
      assertNotNull(ref);
      entityHandler.encodeToResponse(req, res, ref, OutputXMLable.EXTENSION);
      assertNotNull(res.getOutputStream());
      try {
         String xml = res.getContentAsString();
         assertNotNull(xml);
         assertTrue(xml.length() > 20);
         assertTrue(xml.contains(TestData.PREFIX4));
      } catch (UnsupportedEncodingException e) {
         fail("failure trying to get string content");
      }
      assertEquals(68, res.getContentLength());

      // HTML test valid resolveable entity
      req = new MockHttpServletRequest("GET", TestData.REF4);
      res = new MockHttpServletResponse();
      ref = entityHandler.parseReference(TestData.REF4);
      assertNotNull(ref);
      entityHandler.encodeToResponse(req, res, ref, OutputHTMLable.EXTENSION);
      assertNotNull(res.getOutputStream());
      try {
         String html = res.getContentAsString();
         assertNotNull(html);
         assertTrue(html.length() > 20);
         assertTrue(html.contains(TestData.PREFIX4));
      } catch (UnsupportedEncodingException e) {
         fail("failure trying to get string content");
      }
      assertEquals(43, res.getContentLength());

      // test for unresolvable entities

      // JSON test valid resolveable entity
      req = new MockHttpServletRequest("GET", TestData.REF1);
      res = new MockHttpServletResponse();
      ref = entityHandler.parseReference(TestData.REF1);
      assertNotNull(ref);
      entityHandler.encodeToResponse(req, res, ref, OutputJSONable.EXTENSION);
      assertNotNull(res.getOutputStream());
      try {
         String json = res.getContentAsString();
         assertNotNull(json);
         assertTrue(json.length() > 20);
         assertTrue(json.contains(TestData.PREFIX1));
      } catch (UnsupportedEncodingException e) {
         fail("failure trying to get string content");
      }
      assertEquals(167, res.getContentLength());

      // XML test valid resolveable entity
      req = new MockHttpServletRequest("GET", TestData.REF1);
      res = new MockHttpServletResponse();
      ref = entityHandler.parseReference(TestData.REF1);
      assertNotNull(ref);
      entityHandler.encodeToResponse(req, res, ref, OutputXMLable.EXTENSION);
      assertNotNull(res.getOutputStream());
      try {
         String xml = res.getContentAsString();
         assertNotNull(xml);
         assertTrue(xml.length() > 20);
         assertTrue(xml.contains(TestData.PREFIX1));
      } catch (UnsupportedEncodingException e) {
         fail("failure trying to get string content");
      }
      assertEquals(195, res.getContentLength());

      // HTML test valid resolveable entity
      req = new MockHttpServletRequest("GET", TestData.REF1);
      res = new MockHttpServletResponse();
      ref = entityHandler.parseReference(TestData.REF1);
      assertNotNull(ref);
      entityHandler.encodeToResponse(req, res, ref, OutputHTMLable.EXTENSION);
      assertNotNull(res.getOutputStream());
      try {
         String html = res.getContentAsString();
         assertNotNull(html);
         assertTrue(html.length() > 20);
         assertTrue(html.contains(TestData.PREFIX1));
      } catch (UnsupportedEncodingException e) {
         fail("failure trying to get string content");
      }
      assertEquals(100, res.getContentLength());

      // test resolveable collections

      // test for invalid refs

   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.EntityHandlerImpl#handleEntityAccess(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String)}.
    */
   public void testHandleEntityAccess() {
      MockHttpServletRequest req = null;
      MockHttpServletResponse res = null;

      // test valid entity
      req = new MockHttpServletRequest("GET", TestData.REF1);
      res = new MockHttpServletResponse();

      entityHandler.handleEntityAccess(req, res, null);
      assertEquals(HttpServletResponse.SC_OK, res.getStatus());

      // test invalid prefix
      req = new MockHttpServletRequest("GET", "/fake/thing");
      res = new MockHttpServletResponse();

      try {
         entityHandler.handleEntityAccess(req, res, null);
         fail("Should have thrown exception");
      } catch (EntityException e) {
         assertNotNull(e.getMessage());
         assertEquals(HttpServletResponse.SC_NOT_IMPLEMENTED, e.responseCode);
      }

      // test invalid id
      req = new MockHttpServletRequest("GET", TestData.REF1_INVALID);
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
      req = new MockHttpServletRequest("GET", "xxxxxxxxxxxxxxxx");
      res = new MockHttpServletResponse();

      try {
         entityHandler.handleEntityAccess(req, res, null);
         fail("Should have thrown exception");
      } catch (EntityException e) {
         assertNotNull(e.getMessage());
         assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.responseCode);
      }

      // TODO test JSON data return

      // test XML data return

      // types that cannot handle the return requested

   }

}
