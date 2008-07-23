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

import junit.framework.TestCase;

import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.mocks.data.TestData;

/**
 * Testing the central logic of the entity handler
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EntityDescriptionManagerTest extends TestCase {

   protected EntityDescriptionManager entityDescriptionManager;
   private TestData td;

   @Override
   protected void setUp() throws Exception {
      super.setUp();
      // setup things
      td = new TestData();
      entityDescriptionManager = new TestManager(td).entityDescriptionManager;
   }

   public void testDescribeAll() {

      // test describe all entities
      String content = entityDescriptionManager.makeDescribeAll(null);
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

      content = entityDescriptionManager.makeDescribeAll(Formats.XML);
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
   }

   public void testDescribeEntity() {

      // test describe single entity space
      String content = entityDescriptionManager.makeDescribeEntity(TestData.PREFIX1, "test", null);
      assertNotNull(content);
      assertTrue(content.length() > 80);
      assertTrue(content.contains(TestData.PREFIX1));

      // XML
      content = entityDescriptionManager.makeDescribeEntity(TestData.PREFIX1, "test", Formats.XML);
      assertNotNull(content);
      assertTrue(content.length() > 80);
      assertTrue(content.contains(TestData.PREFIX1));

      // prefix 4
      content = entityDescriptionManager.makeDescribeEntity(TestData.PREFIX4, "test", null);
      assertNotNull(content);
      assertTrue(content.length() > 80);
      assertTrue(content.contains(TestData.PREFIX4));

      content = entityDescriptionManager.makeDescribeEntity(TestData.PREFIX6, "test", null);
      assertNotNull(content);
      assertTrue(content.length() > 80);
      assertTrue(content.contains(TestData.PREFIX6));

      // XML
      content = entityDescriptionManager.makeDescribeEntity(TestData.PREFIX6, "test", Formats.XML);
      assertNotNull(content);
      assertTrue(content.length() > 80);
      assertTrue(content.contains(TestData.PREFIX6));

      // test an entity which is describeable
      content = entityDescriptionManager.makeDescribeEntity(TestData.PREFIX7, "test", Formats.HTML);
      assertNotNull(content);
      assertTrue(content.length() > 80);
      assertTrue(content.contains(TestData.PREFIX7));
      assertTrue(content.contains("describe-prefix test description of an entity"));
      assertTrue(content.contains("This is a test description of Createable"));

      // XML
      content = entityDescriptionManager.makeDescribeEntity(TestData.PREFIX7, "test", Formats.XML);
      assertNotNull(content);
      assertTrue(content.length() > 80);
      assertTrue(content.contains(TestData.PREFIX7));
      assertTrue(content.contains("<description>"));
      assertTrue(content.contains("describe-prefix test description of an entity"));
      assertTrue(content.contains("This is a test description of Createable"));

      // test an entity which is DescribePropertiesable
      content = entityDescriptionManager.makeDescribeEntity(TestData.PREFIX8, "test", Formats.HTML);
      assertNotNull(content);
      assertTrue(content.length() > 80);
      assertTrue(content.contains(TestData.PREFIX8));
      assertTrue(content.contains("CUSTOM description"));
      assertTrue(content.contains("CUSTOM Deleteable"));

      // XML
      content = entityDescriptionManager.makeDescribeEntity(TestData.PREFIX8, "test", Formats.XML);
      assertNotNull(content);
      assertTrue(content.length() > 80);
      assertTrue(content.contains(TestData.PREFIX8));
      assertTrue(content.contains("<description>"));
      assertTrue(content.contains("CUSTOM description"));
      assertTrue(content.contains("CUSTOM Deleteable"));

      // test invalid describe
      try {
         content = entityDescriptionManager.makeDescribeEntity(TestData.PREFIX9, "test", null);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }      
      
   }

}
