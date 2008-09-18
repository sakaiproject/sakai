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

import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.mocks.data.MyEntity;
import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.sakaiproject.genericdao.util.ReflectUtils;

/**
 * Testing the central logic of the entity handler
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EntityBrokerManagerTest extends TestCase {

   protected EntityBrokerManager entityBrokerManager;
   private TestData td;

   @Override
   protected void setUp() throws Exception {
      super.setUp();
      // setup things
      td = new TestData();

      entityBrokerManager = new ServiceTestManager(td).entityBrokerManager;
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.EntityHandlerImpl#entityExists(java.lang.String)}.
    */
   public void testEntityExists() {
      EntityReference ref = null;
      boolean exists = false;

      ref = new EntityReference(TestData.REF1);
      exists = entityBrokerManager.entityExists(ref);
      assertTrue(exists);

      ref = new EntityReference(TestData.REF1_1);
      exists = entityBrokerManager.entityExists(ref);
      assertTrue(exists);

      ref = new EntityReference(TestData.REF2);
      exists = entityBrokerManager.entityExists(ref);
      assertTrue(exists);

      // test that invalid id with valid prefix does not pass
      ref = new EntityReference(TestData.REF1_INVALID);
      exists = entityBrokerManager.entityExists(ref);
      assertFalse(exists);

      // test that unregistered ref does not pass
      ref = new EntityReference(TestData.REF9);
      exists = entityBrokerManager.entityExists(ref);
      assertFalse(exists);
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.EntityHandlerImpl#getEntityURL(java.lang.String)}.
    */
   public void testGetEntityURL() {
      String url = null;

      url = entityBrokerManager.getEntityURL(TestData.REF1, null, null);
      assertEquals(TestData.URL1, url);

      url = entityBrokerManager.getEntityURL(TestData.REF2, null, null);
      assertEquals(TestData.URL2, url);

      url = entityBrokerManager.getEntityURL(TestData.REF1_INVALID, null, null);

      try {
         url = entityBrokerManager.getEntityURL(TestData.INVALID_REF, null, null);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.EntityHandlerImpl#parseReference(java.lang.String)}.
    */
   public void testParseReference() {
      EntityReference er = null;

      er = entityBrokerManager.parseReference(TestData.REF1);
      assertNotNull(er);
      assertEquals(TestData.PREFIX1, er.getPrefix());
      assertEquals(TestData.IDS1[0], er.getId());

      er = entityBrokerManager.parseReference(TestData.REF2);
      assertNotNull(er);
      assertEquals(TestData.PREFIX2, er.getPrefix());

      // test parsing a defined reference
      er = entityBrokerManager.parseReference(TestData.REF3A);
      assertNotNull(er);
      assertEquals(TestData.PREFIX3, er.getPrefix());

      // parsing of unregistered entity references returns null
      er = entityBrokerManager.parseReference(TestData.REF9);
      assertNull(er);

      // parsing with nonexistent prefix returns null
      er = entityBrokerManager.parseReference("/totallyfake/notreal");
      assertNull(er);

      // TODO test handling custom ref objects

      try {
         er = entityBrokerManager.parseReference(TestData.INVALID_REF);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

   /**
    * Test method for {@link EntityHandlerImpl#parseEntityURL(String)}
    */
   public void testParseEntityURL() {
      EntityView view = null;

      view = entityBrokerManager.parseEntityURL(TestData.INPUT_URL1);
      assertNotNull(view);
      assertEquals(EntityView.VIEW_SHOW, view.getViewKey());
      assertEquals(TestData.PREFIX1, view.getEntityReference().getPrefix());
      assertEquals(TestData.IDS1[0], view.getEntityReference().getId());

      // TODO add more tests

      // parsing of URL related to unregistered entity references returns null
      view = entityBrokerManager.parseEntityURL(TestData.REF9);
      assertNull(view);

      // TODO test custom parse rules

      try {
         view = entityBrokerManager.parseEntityURL(TestData.INVALID_URL);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

   public void testGetEntityObject() {
      Object entity = null;
      EntityReference ref = null;

      // first for resolveable
      ref = entityBrokerManager.parseReference(TestData.REF4);
      assertNotNull(ref);
      entity = entityBrokerManager.fetchEntityObject(ref);
      assertNotNull(entity);
      assertEquals(MyEntity.class, entity.getClass());
      assertEquals(TestData.entity4, entity);

      ref = entityBrokerManager.parseReference(TestData.REF4_two);
      assertNotNull(ref);
      entity = entityBrokerManager.fetchEntityObject(ref);
      assertNotNull(entity);
      assertEquals(MyEntity.class, entity.getClass());
      assertEquals(TestData.entity4_two, entity);

      // now for non-resolveable
      ref = entityBrokerManager.parseReference(TestData.REF5);
      assertNotNull(ref);
      entity = entityBrokerManager.fetchEntityObject(ref);
      assertNull(entity);
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.EntityBrokerManager#getReflectUtil()}.
    */
   public void testGetReflectUtil() {
      ReflectUtils ru = entityBrokerManager.getReflectUtil();
      assertNotNull(ru);
      ReflectUtils ru2 = null;
      for (int i = 0; i < 1000; i++) {
         ru2 = entityBrokerManager.getReflectUtil();
         assertNotNull(ru2);
      }
      assertNotNull(ru2);
      assertEquals(ru2, ru);
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.EntityBrokerManager#makeFullURL(java.lang.String)}.
    */
   public void testMakeFullURL() {
      String full = entityBrokerManager.makeFullURL(TestData.REF1);
      assertNotNull(full);
      assertEquals("http://localhost:8001/portal/direct" + TestData.REF1, full);
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.EntityBrokerManager#makeEntityView(org.sakaiproject.entitybroker.EntityReference, java.lang.String, java.lang.String)}.
    */
   public void testMakeEntityView() {
      EntityView ev = entityBrokerManager.makeEntityView(new EntityReference("azprefix", "azid"), EntityView.VIEW_SHOW, Formats.XML);
      assertNotNull(ev);
      assertEquals("azprefix", ev.getEntityReference().getPrefix());
      assertEquals("azid", ev.getEntityReference().getId());
      assertEquals(EntityView.VIEW_SHOW, ev.getViewKey());
      assertEquals(Formats.XML, ev.getExtension());

      ev = entityBrokerManager.makeEntityView(new EntityReference("azprefix", "azid"), null, null);
      assertNotNull(ev);
      assertEquals("azprefix", ev.getEntityReference().getPrefix());
      assertEquals("azid", ev.getEntityReference().getId());
      assertEquals(EntityView.VIEW_SHOW, ev.getViewKey());
      assertEquals(null, ev.getExtension());

      try {
         ev = entityBrokerManager.makeEntityView(null, null, null);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.EntityBrokerManager#fetchEntity(org.sakaiproject.entitybroker.EntityReference)}.
    */
   public void testFetchEntity() {
      EntityReference ref = new EntityReference(TestData.REF4);
      Object entity = entityBrokerManager.fetchEntity(ref);
      assertNotNull(entity);
      assertEquals(TestData.entity4, entity);

      ref = new EntityReference(TestData.REF1);
      entity = entityBrokerManager.fetchEntity(ref);
      assertNull(entity);

      try {
         entity = entityBrokerManager.fetchEntity(null);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.EntityBrokerManager#getEntityData(org.sakaiproject.entitybroker.EntityReference)}.
    */
   public void testGetEntityData() {
      EntityReference ref = new EntityReference(TestData.REF4);
      EntityData entity = entityBrokerManager.getEntityData(ref);
      assertNotNull(entity);
      assertEquals(TestData.REF4, entity.getEntityReference());
      assertEquals(TestData.entity4, entity.getData());
      assertNotNull(entity.getDisplayTitle());
      assertNotNull(entity.getEntityURL());

      ref = new EntityReference(TestData.REF1);
      entity = entityBrokerManager.getEntityData(ref);
      assertNotNull(entity);
      assertEquals(TestData.REF1, entity.getEntityReference());
      assertNull(entity.getData());
      assertNotNull(entity.getDisplayTitle());
      assertNotNull(entity.getEntityURL());

      try {
         entity = entityBrokerManager.getEntityData(null);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.EntityBrokerManager#fetchEntities(org.sakaiproject.entitybroker.EntityReference, org.sakaiproject.entitybroker.entityprovider.search.Search, java.util.Map)}.
    */
   public void testFetchEntities() {
      EntityReference ref = new EntityReference(TestData.SPACE4);
      List<?> l = entityBrokerManager.fetchEntities(ref, null, null);
      assertNotNull(l);
      assertEquals(3, l.size());
      assertEquals(MyEntity.class, l.get(0).getClass());

      ref = new EntityReference(TestData.SPACE4);
      l = entityBrokerManager.fetchEntities(ref, new Search(), new HashMap<String, Object>());
      assertNotNull(l);
      assertEquals(3, l.size());
      assertEquals(MyEntity.class, l.get(0).getClass());
      assertEquals(TestData.entity4, l.get(0));

      ref = new EntityReference(TestData.REF4);
      l = entityBrokerManager.fetchEntities(ref, null, null);
      assertNotNull(l);
      assertEquals(1, l.size());
      assertEquals(MyEntity.class, l.get(0).getClass());
      assertEquals(TestData.entity4, l.get(0));

      ref = new EntityReference("/" + TestData.PREFIX1);
      l = entityBrokerManager.fetchEntities(ref, null, null);
      assertNotNull(l);
      assertEquals(0, l.size());

      try {
         entityBrokerManager.fetchEntities(null, null, null);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.EntityBrokerManager#getEntitiesData(org.sakaiproject.entitybroker.EntityReference, org.sakaiproject.entitybroker.entityprovider.search.Search, java.util.Map)}.
    */
   public void testGetEntitiesData() {
      EntityReference ref = new EntityReference(TestData.SPACE4);
      List<EntityData> data = entityBrokerManager.getEntitiesData(ref, new Search(), new HashMap<String, Object>());
      assertNotNull(data);
      assertEquals(3, data.size());
      assertEquals(EntityData.class, data.get(0).getClass());
      assertEquals(TestData.entity4, data.get(0).getData());

      ref = new EntityReference(TestData.SPACE4);
      data = entityBrokerManager.getEntitiesData(ref, null, null);
      assertNotNull(data);
      assertEquals(3, data.size());
      assertEquals(EntityData.class, data.get(0).getClass());
      assertEquals(TestData.entity4, data.get(0).getData());
      
      ref = new EntityReference("/" + TestData.PREFIX1);
      data = entityBrokerManager.getEntitiesData(ref, null, null);
      assertNotNull(data);
      assertEquals(0, data.size());

      try {
         entityBrokerManager.getEntitiesData(null, null, null);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.EntityBrokerManager#browseEntities(java.lang.String, org.sakaiproject.entitybroker.entityprovider.search.Search, java.lang.String, java.lang.String, java.util.Map)}.
    */
   public void testBrowseEntities() {
      List<EntityData> data = entityBrokerManager.browseEntities(TestData.PREFIX4, null, null, null, null);
      assertNotNull(data);
      assertEquals(0, data.size());

      data = entityBrokerManager.browseEntities(TestData.PREFIXB1, null, null, null, null);
      assertNotNull(data);
      assertEquals(3, data.size());
      assertEquals(EntityData.class, data.get(0).getClass());

      data = entityBrokerManager.browseEntities(TestData.PREFIXB2, null, null, null, null);
      assertNotNull(data);
      assertEquals(3, data.size());
      assertEquals(EntityData.class, data.get(0).getClass());

      data = entityBrokerManager.browseEntities(TestData.PREFIXB2, null, "/user/aaronz", null, null);
      assertNotNull(data);
      assertEquals(1, data.size());
      assertEquals(EntityData.class, data.get(0).getClass());

      data = entityBrokerManager.browseEntities(TestData.PREFIXB2, null, "/user/aaronz", "/site/siteAZ", null);
      assertNotNull(data);
      assertEquals(2, data.size());
      assertEquals(EntityData.class, data.get(0).getClass());

      try {
         data = entityBrokerManager.browseEntities(null, null, null, null, null);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.EntityBrokerManager#convertToEntityData(java.util.List, org.sakaiproject.entitybroker.EntityReference)}.
    */
   public void testConvertToEntityDataListOfQEntityReference() {
      // TODO fail("Not yet implemented");
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.EntityBrokerManager#convertToEntityData(java.lang.Object, org.sakaiproject.entitybroker.EntityReference)}.
    */
   public void testConvertToEntityDataObjectEntityReference() {
   // TODO fail("Not yet implemented");
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.EntityBrokerManager#populateEntityData(java.util.List)}.
    */
   public void testPopulateEntityDataListOfEntityData() {
   // TODO fail("Not yet implemented");
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.EntityBrokerManager#populateEntityData(org.sakaiproject.entitybroker.entityprovider.extension.EntityData[])}.
    */
   public void testPopulateEntityDataEntityDataArray() {
   // TODO fail("Not yet implemented");
   }

}
