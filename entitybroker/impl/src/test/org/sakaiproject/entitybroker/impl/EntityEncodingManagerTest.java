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

package org.sakaiproject.entitybroker.impl;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.impl.util.EntityXStream;
import org.sakaiproject.entitybroker.mocks.data.MyEntity;
import org.sakaiproject.entitybroker.mocks.data.TestData;

/**
 * Testing the central logic of the entity handler
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EntityEncodingManagerTest extends TestCase {

   protected EntityEncodingManager entityEncodingManager;
   protected EntityBrokerManager entityBrokerManager;
   private TestData td;

   @Override
   protected void setUp() throws Exception {
      super.setUp();
      // setup things
      td = new TestData();
      ServiceTestManager tm = new ServiceTestManager(td);
      entityEncodingManager = tm.entityEncodingManager;
      entityBrokerManager = tm.entityBrokerManager;
   }


   /**
    * Test method for {@link EntityHandlerImpl#internalOutputFormatter(EntityView, javax.servlet.http.HttpServletRequest, HttpServletResponse)}
    **/
   public void testInternalOutputFormatter() {

      String fo = null;
      EntityView view = null;
      OutputStream output = null;

      // XML test valid resolveable entity
      output = new ByteArrayOutputStream();
      view = entityBrokerManager.parseEntityURL(TestData.REF4 + "." + Formats.XML);
      assertNotNull(view);
      entityEncodingManager.internalOutputFormatter(view.getEntityReference(), view.getExtension(), null, output, view, null);
      fo = output.toString();
      assertNotNull(fo);
      assertTrue(fo.length() > 20);
      assertTrue(fo.contains(TestData.PREFIX4));
      assertTrue(fo.contains("<id>4-one</id>"));
      assertTrue(fo.contains(EntityXStream.SAKAI_ENTITY));

      // test null view
      output = new ByteArrayOutputStream();
      entityEncodingManager.internalOutputFormatter(new EntityReference(TestData.REF4), Formats.XML, null, output, null, null);
      fo = output.toString();
      assertNotNull(fo);
      assertTrue(fo.length() > 20);
      assertTrue(fo.contains(TestData.PREFIX4));
      assertTrue(fo.contains("<id>4-one</id>"));
      assertTrue(fo.contains(EntityXStream.SAKAI_ENTITY));
      
      // test list of entities
      ArrayList<MyEntity> testEntities = new ArrayList<MyEntity>();
      testEntities.add(TestData.entity4);
      testEntities.add(TestData.entity4_two);
      output = new ByteArrayOutputStream();
      entityEncodingManager.internalOutputFormatter(new EntityReference(TestData.PREFIX4, ""), Formats.XML, testEntities, output, null, null);
      fo = output.toString();
      assertNotNull(fo);
      assertTrue(fo.length() > 20);
      assertTrue(fo.contains(TestData.PREFIX4));
      assertTrue(fo.contains("<id>4-one</id>"));
      assertTrue(fo.contains("<id>4-two</id>"));
      assertFalse(fo.contains("<id>4-three</id>"));
      assertTrue(fo.contains(EntityXStream.SAKAI_ENTITY));

      // test single entity
      testEntities.clear();
      testEntities.add(TestData.entity4_3);
      output = new ByteArrayOutputStream();
      entityEncodingManager.internalOutputFormatter(new EntityReference(TestData.REF4_3), Formats.XML, testEntities, output, null, null);
      fo = output.toString();
      assertNotNull(fo);
      assertTrue(fo.length() > 20);
      assertTrue(fo.contains(TestData.PREFIX4));
      assertTrue(fo.contains("<id>4-three</id>"));
      assertTrue(fo.contains(EntityXStream.SAKAI_ENTITY));


      // JSON test valid resolveable entity
      output = new ByteArrayOutputStream();
      view = entityBrokerManager.parseEntityURL(TestData.REF4 + "." + Formats.JSON);
      assertNotNull(view);
      entityEncodingManager.internalOutputFormatter(view.getEntityReference(), view.getExtension(), null, output, view, null);
      fo = output.toString();
      assertNotNull(fo);
      assertTrue(fo.length() > 20);
      assertTrue(fo.contains(TestData.PREFIX4));
      assertTrue(fo.contains("\"id\":\"4-one\","));
      assertTrue(fo.contains(EntityXStream.SAKAI_ENTITY));

      // HTML test valid resolveable entity
      output = new ByteArrayOutputStream();
      view = entityBrokerManager.parseEntityURL(TestData.REF4 + "." + Formats.HTML);
      assertNotNull(view);
      entityEncodingManager.internalOutputFormatter(view.getEntityReference(), view.getExtension(), null, output, view, null);
      fo = output.toString();
      assertNotNull(fo);
      assertTrue(fo.length() > 20);
      assertTrue(fo.contains(TestData.PREFIX4));

      // test for unresolvable entities

      // JSON test valid unresolvable entity
      output = new ByteArrayOutputStream();
      view = entityBrokerManager.parseEntityURL(TestData.REF1 + "." + Formats.JSON);
      assertNotNull(view);
      try {
         entityEncodingManager.internalOutputFormatter(view.getEntityReference(), view.getExtension(), null, output, view, null);
         fail("Should have thrown exception");
      } catch (EntityException e) {
         assertNotNull(e.getMessage());
         assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, e.responseCode);
      }

      // HTML test valid unresolvable entity
      output = new ByteArrayOutputStream();
      view = entityBrokerManager.parseEntityURL(TestData.REF1); // blank
      assertNotNull(view);
      try {
         entityEncodingManager.internalOutputFormatter(view.getEntityReference(), view.getExtension(), null, output, view, null);
         fail("Should have thrown exception");
      } catch (EntityException e) {
         assertNotNull(e.getMessage());
         assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, e.responseCode);
      }

      // test resolveable collections
      // XML
      output = new ByteArrayOutputStream();
      view = entityBrokerManager.parseEntityURL(TestData.SPACE4 + "." + Formats.XML);
      assertNotNull(view);
      entityEncodingManager.internalOutputFormatter(view.getEntityReference(), view.getExtension(), null, output, view, null);
      fo = output.toString();
      assertNotNull(fo);
      assertTrue(fo.length() > 20);
      assertTrue(fo.contains(TestData.PREFIX4));
      assertTrue(fo.contains(TestData.IDS4[0]));
      assertTrue(fo.contains(TestData.IDS4[1]));
      assertTrue(fo.contains(TestData.IDS4[2]));
      assertTrue(fo.contains(EntityXStream.SAKAI_ENTITY));

      // JSON
      output = new ByteArrayOutputStream();
      view = entityBrokerManager.parseEntityURL(TestData.SPACE4 + "." + Formats.JSON);
      assertNotNull(view);
      entityEncodingManager.internalOutputFormatter(view.getEntityReference(), view.getExtension(), null, output, view, null);
      fo = output.toString();
      assertNotNull(fo);
      assertTrue(fo.length() > 20);
      assertTrue(fo.contains(TestData.PREFIX4));
      assertTrue(fo.contains(TestData.IDS4[0]));
      assertTrue(fo.contains(TestData.IDS4[1]));
      assertTrue(fo.contains(TestData.IDS4[2]));
      assertTrue(fo.contains(EntityXStream.SAKAI_ENTITY));

      // test for invalid refs
      try {
         entityEncodingManager.internalOutputFormatter( new EntityReference("/fakey/fake"), null, null, output, null, null);
         fail("Should have thrown exception");
      } catch (EntityException e) {
         assertNotNull(e.getMessage());
         assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, e.responseCode);
      }

   }

}
