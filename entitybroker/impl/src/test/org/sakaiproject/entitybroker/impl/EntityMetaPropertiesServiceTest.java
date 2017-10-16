/**
 * Copyright (c) 2007-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * EntityBrokerImplTest.java - 2007 Jul 21, 2007 3:04:55 PM - entity-broker - AZ
 */

package org.sakaiproject.entitybroker.impl;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.entitybroker.dao.EntityBrokerDao;
import org.sakaiproject.entitybroker.impl.data.TestDataPreload;
import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

/**
 * Testing the entitybroker implementation
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
@ContextConfiguration(locations={
		"/database-test.xml",
		"classpath:org/sakaiproject/entitybroker/spring-jdbc.xml" })
public class EntityMetaPropertiesServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

   protected EntityMetaPropertiesService propertiesService;

   @Autowired
   @Qualifier("org.sakaiproject.entitybroker.dao.EntityBrokerDao")
   private EntityBrokerDao dao;
   private TestData td;
   @Autowired
   private TestDataPreload tdp;

   @Before
   public void onSetUp() throws Exception {
      // init the test data
      td = new TestData();

      // setup fake internal services
      ServiceTestManager tm = new ServiceTestManager(td, dao);

      // create and setup the object to be tested
      propertiesService = tm.entityMetaPropertiesService;
   }

   /**
    * ADD unit tests below here, use testMethod as the name of the unit test, Note that if a method
    * is overloaded you should include the arguments in the test name like so: testMethodClassInt
    * (for method(Class, int);
    */

   @Test
   public void testValidTestData() {
      // ensure the test data is setup the way we think
      Assert.assertNotNull(td);
      Assert.assertNotNull(tdp);

      // Assert.assertEquals(new Long(1), tdp.pNode1.getId());
   }



   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#findEntityRefs(java.lang.String[], java.lang.String[], java.lang.String[], boolean)}.
    */
   @Test
   public void testFindEntityRefs() {
      List<String> l = null;

      // test search with limit by prefix
      l = propertiesService.findEntityRefs(new String[] { TestData.PREFIX5 }, null, null, true);
      Assert.assertNotNull(l);
      Assert.assertEquals(2, l.size());
      Assert.assertTrue(l.contains(TestData.REF5));
      Assert.assertTrue(l.contains(TestData.REF5_2));

      // test search with limit by prefix (check that no results ok)
      l = propertiesService.findEntityRefs(new String[] { TestData.INVALID_REF }, null, null, true);
      Assert.assertNotNull(l);
      Assert.assertEquals(0, l.size());

      // test searching with multiple prefixes
      l = propertiesService.findEntityRefs(new String[] { TestData.PREFIX5, TestData.PREFIX1 }, null,
            null, true);
      Assert.assertNotNull(l);
      Assert.assertEquals(2, l.size());
      Assert.assertTrue(l.contains(TestData.REF5));
      Assert.assertTrue(l.contains(TestData.REF5_2));

      // test searching by names
      l = propertiesService.findEntityRefs(new String[] { TestData.PREFIX5 },
            new String[] { TestData.PROPERTY_NAME5A }, null, true);
      Assert.assertNotNull(l);
      Assert.assertEquals(1, l.size());
      Assert.assertTrue(l.contains(TestData.REF5));

      // test searching by invalid name (return no results)
      l = propertiesService.findEntityRefs(new String[] { TestData.PREFIX5 },
            new String[] { TestData.INVALID_REF }, null, true);
      Assert.assertNotNull(l);
      Assert.assertEquals(0, l.size());

      // test searching with multiple names
      l = propertiesService.findEntityRefs(new String[] { TestData.PREFIX5 }, new String[] {
            TestData.PROPERTY_NAME5A, TestData.PROPERTY_NAME5C }, null, true);
      Assert.assertNotNull(l);
      Assert.assertEquals(2, l.size());
      Assert.assertTrue(l.contains(TestData.REF5));
      Assert.assertTrue(l.contains(TestData.REF5_2));

      // test search limit by values (long exact match)
      l = propertiesService.findEntityRefs(new String[] { TestData.PREFIX5 },
            new String[] { TestData.PROPERTY_NAME5C }, new String[] { TestData.PROPERTY_VALUE5C },
            true);
      Assert.assertNotNull(l);
      Assert.assertEquals(1, l.size());
      Assert.assertTrue(l.contains(TestData.REF5_2));

      // test search limit by values (exact match)
      l = propertiesService.findEntityRefs(new String[] { TestData.PREFIX5 },
            new String[] { TestData.PROPERTY_NAME5B }, new String[] { TestData.PROPERTY_VALUE5B },
            true);
      Assert.assertNotNull(l);
      Assert.assertEquals(1, l.size());
      Assert.assertTrue(l.contains(TestData.REF5));

      // cannot have empty or null prefix
      try {
         l = propertiesService.findEntityRefs(new String[] {},
               new String[] { TestData.PROPERTY_NAME5A }, null, true);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e.getMessage());
      }

      try {
         l = propertiesService.findEntityRefs(null, new String[] { TestData.PROPERTY_NAME5A }, null,
               true);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e.getMessage());
      }

      // test search limit by values cannot have name null or empty
      try {
         l = propertiesService.findEntityRefs(new String[] { TestData.PREFIX5 }, new String[] {},
               new String[] { TestData.PROPERTY_VALUE5A }, true);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e.getMessage());
      }

      // test name and values arrays must match sizes
      try {
         l = propertiesService.findEntityRefs(new String[] { TestData.PREFIX5 }, new String[] {
               TestData.PROPERTY_NAME5A, TestData.PROPERTY_NAME5B },
               new String[] { TestData.PROPERTY_VALUE5A }, true);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e.getMessage());
      }

      // test search with all empty fields fail
      try {
         l = propertiesService.findEntityRefs(new String[] {}, new String[] {}, new String[] {}, false);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e.getMessage());
      }

   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#getProperties(java.lang.String)}.
    */
   @Test
   public void testGetProperties() {
      Map<String, String> m = null;

      m = propertiesService.getProperties(TestData.REF5);
      Assert.assertNotNull(m);
      Assert.assertEquals(2, m.size());
      Assert.assertTrue(m.containsKey(TestData.PROPERTY_NAME5A));
      Assert.assertTrue(m.containsKey(TestData.PROPERTY_NAME5B));

      m = propertiesService.getProperties(TestData.REF5_2);
      Assert.assertNotNull(m);
      Assert.assertEquals(1, m.size());
      Assert.assertTrue(m.containsKey(TestData.PROPERTY_NAME5C));

      // ref with no properties should fetch none
      m = propertiesService.getProperties(TestData.REF1);
      Assert.assertNotNull(m);
      Assert.assertTrue(m.isEmpty());

      // make sure invalid ref causes failure
      try {
         m = propertiesService.getProperties(TestData.INVALID_REF);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e.getMessage());
      }

   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#getPropertyValue(java.lang.String, java.lang.String)}.
    */
   @Test
   public void testGetPropertyValue() {
      String value = null;

      value = propertiesService.getPropertyValue(TestData.REF5, TestData.PROPERTY_NAME5A);
      Assert.assertNotNull(value);
      Assert.assertEquals(TestData.PROPERTY_VALUE5A, value);

      value = propertiesService.getPropertyValue(TestData.REF5, TestData.PROPERTY_NAME5B);
      Assert.assertNotNull(value);
      Assert.assertEquals(TestData.PROPERTY_VALUE5B, value);

      // test large value retrieval
      value = propertiesService.getPropertyValue(TestData.REF5_2, TestData.PROPERTY_NAME5C);
      Assert.assertNotNull(value);
      Assert.assertEquals(TestData.PROPERTY_VALUE5C, value);

      // nonexistent value property get retrieves null
      value = propertiesService.getPropertyValue(TestData.REF5, "XXXXXXXXXXXX");
      Assert.assertNull(value);

      // make sure invalid ref causes failure
      try {
         value = propertiesService.getPropertyValue(TestData.INVALID_REF, TestData.PROPERTY_NAME5A);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e.getMessage());
      }

      // null name causes failure
      try {
         value = propertiesService.getPropertyValue(TestData.REF5, null);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e.getMessage());
      }

      // empty name causes failure
      try {
         value = propertiesService.getPropertyValue(TestData.REF5, "");
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e.getMessage());
      }

   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#setPropertyValue(java.lang.String, java.lang.String, java.lang.String[])}.
    */
   @Test
   public void testSetPropertyValue() {
      String value = null;

      // check that we can save a new property on an entity
      propertiesService.setPropertyValue(TestData.REF5, "newNameAlpha", "newValueAlpha");
      value = propertiesService.getPropertyValue(TestData.REF5, "newNameAlpha");
      Assert.assertNotNull(value);
      Assert.assertEquals("newValueAlpha", value);

      // check that we can update an existing property on an entity
      propertiesService.setPropertyValue(TestData.REF5, TestData.PROPERTY_NAME5A, "AZnewValue");
      value = propertiesService.getPropertyValue(TestData.REF5, TestData.PROPERTY_NAME5A);
      Assert.assertNotNull(value);
      Assert.assertNotSame(TestData.PROPERTY_VALUE5A, value);
      Assert.assertEquals("AZnewValue", value);

      // check that we can remove a property on an entity
      propertiesService.setPropertyValue(TestData.REF5, "newNameAlpha", null);
      value = propertiesService.getPropertyValue(TestData.REF5, "newNameAlpha");
      Assert.assertNull(value);

      // check that we can remove all properties on an entity
      Map<String, String> m = propertiesService.getProperties(TestData.REF5);
      Assert.assertEquals(2, m.size());
      propertiesService.setPropertyValue(TestData.REF5, null, null);
      m = propertiesService.getProperties(TestData.REF5);
      Assert.assertEquals(0, m.size());

      // make sure invalid ref causes failure
      try {
         propertiesService.setPropertyValue(TestData.INVALID_REF, "newNameAlpha", "newValueAlpha");
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e.getMessage());
      }

      // make sure invalid params cause failure
      try {
         propertiesService.setPropertyValue(TestData.REF1, null, "XXXXXXXXX");
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e.getMessage());
      }

   }
   
}
