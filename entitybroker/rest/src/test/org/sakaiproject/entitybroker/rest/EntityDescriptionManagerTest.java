/**
 * $Id$
 * $URL$
 * EntityHandlerImplTest.java - entity-broker - Apr 6, 2008 12:08:39 PM - azeckoski
 **************************************************************************
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
 */

package org.sakaiproject.entitybroker.rest;

import junit.framework.TestCase;

import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.sakaiproject.entitybroker.rest.EntityDescriptionManager;

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
      entityDescriptionManager = new ServiceTestManager(td).entityDescriptionManager;
   }

   public void testReplacePrefix() {
      String outgoingTemplate = "/{prefix}/hello";
      String prefix = "myprefix";
      String result = entityDescriptionManager.replacePrefix(outgoingTemplate, prefix);
      assertNotNull(result);
      assertEquals("/myprefix/hello", result);
   }

   public void testDescribeAll() {

      // test describe all entities
      String content = entityDescriptionManager.makeDescribeAll(null, null);
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

      content = entityDescriptionManager.makeDescribeAll(Formats.XML, null);
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
      String content = entityDescriptionManager.makeDescribeEntity(TestData.PREFIX1, "test", null, null);
      assertNotNull(content);
      assertTrue(content.length() > 80);
      assertTrue(content.contains(TestData.PREFIX1));

      // XML
      content = entityDescriptionManager.makeDescribeEntity(TestData.PREFIX1, "test", Formats.XML, null);
      assertNotNull(content);
      assertTrue(content.length() > 80);
      assertTrue(content.contains(TestData.PREFIX1));

      // prefix 4
      content = entityDescriptionManager.makeDescribeEntity(TestData.PREFIX4, "test", null, null);
      assertNotNull(content);
      assertTrue(content.length() > 80);
      assertTrue(content.contains(TestData.PREFIX4));

      content = entityDescriptionManager.makeDescribeEntity(TestData.PREFIX6, "test", null, null);
      assertNotNull(content);
      assertTrue(content.length() > 80);
      assertTrue(content.contains(TestData.PREFIX6));

      // XML
      content = entityDescriptionManager.makeDescribeEntity(TestData.PREFIX6, "test", Formats.XML, null);
      assertNotNull(content);
      assertTrue(content.length() > 80);
      assertTrue(content.contains(TestData.PREFIX6));

      // test an entity which is describeable
      content = entityDescriptionManager.makeDescribeEntity(TestData.PREFIX7, "test", Formats.HTML, null);
      assertNotNull(content);
      assertTrue(content.length() > 80);
      assertTrue(content.contains(TestData.PREFIX7));
      assertTrue(content.contains("describe-prefix test description of an entity"));
      assertTrue(content.contains("This is a test description of Createable"));

      // XML
      content = entityDescriptionManager.makeDescribeEntity(TestData.PREFIX7, "test", Formats.XML, null);
      assertNotNull(content);
      assertTrue(content.length() > 80);
      assertTrue(content.contains(TestData.PREFIX7));
      assertTrue(content.contains("<description>"));
      assertTrue(content.contains("describe-prefix test description of an entity"));
      assertTrue(content.contains("This is a test description of Createable"));

      // test an entity which is DescribePropertiesable
      content = entityDescriptionManager.makeDescribeEntity(TestData.PREFIX8, "test", Formats.HTML, null);
      assertNotNull(content);
      assertTrue(content.length() > 80);
      assertTrue(content.contains(TestData.PREFIX8));
      assertTrue(content.contains("CUSTOM description"));
      assertTrue(content.contains("CUSTOM Deleteable"));

      // XML
      content = entityDescriptionManager.makeDescribeEntity(TestData.PREFIX8, "test", Formats.XML, null);
      assertNotNull(content);
      assertTrue(content.length() > 80);
      assertTrue(content.contains(TestData.PREFIX8));
      assertTrue(content.contains("<description>"));
      assertTrue(content.contains("CUSTOM description"));
      assertTrue(content.contains("CUSTOM Deleteable"));

      // test invalid describe
      try {
         content = entityDescriptionManager.makeDescribeEntity(TestData.PREFIX9, "test", null, null);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }      
      
   }

}
