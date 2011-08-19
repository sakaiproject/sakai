/**
 * EntityBrokerImplTest.java - 2007 Jul 21, 2007 3:04:55 PM - entity-broker - AZ
 */

package org.sakaiproject.entitybroker.impl;

import java.util.List;
import java.util.Map;

import org.sakaiproject.entitybroker.dao.EntityBrokerDao;
import org.sakaiproject.entitybroker.impl.data.TestDataPreload;
import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.springframework.test.AbstractTransactionalSpringContextTests;

/**
 * Testing the entitybroker implementation
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class EntityMetaPropertiesServiceTest extends AbstractTransactionalSpringContextTests {

   protected EntityMetaPropertiesService propertiesService;

   private EntityBrokerDao dao;
   private TestData td;
   private TestDataPreload tdp;

   protected String[] getConfigLocations() {
      // point to the needed spring config files, must be on the classpath
      // (add component/src/webapp/WEB-INF to the build path in Eclipse),
      // they also need to be referenced in the project.xml file
      return new String[] { "database-test.xml", "classpath:org/sakaiproject/entitybroker/spring-jdbc.xml" };
   }

   // run this before each test starts
   protected void onSetUpBeforeTransaction() throws Exception {
      // load the spring created dao class bean from the Spring Application Context
      dao = (EntityBrokerDao) applicationContext
            .getBean("org.sakaiproject.entitybroker.dao.EntityBrokerDao");
      if (dao == null) {
         throw new NullPointerException("Dao could not be retrieved from spring context");
      }

      // load up the test data preloader from spring
      tdp = (TestDataPreload) applicationContext
            .getBean("org.sakaiproject.entitybroker.impl.test.data.TestDataPreload");
      if (tdp == null) {
         throw new NullPointerException(
               "TestDatePreload could not be retrieved from spring context");
      }

      // load up any other needed spring beans

      // init the test data
      td = new TestData();

      // setup fake internal services
      ServiceTestManager tm = new ServiceTestManager(td, dao);

      // create and setup the object to be tested
      propertiesService = tm.entityMetaPropertiesService;

   }

   // run this before each test starts and as part of the transaction
   protected void onSetUpInTransaction() {
      // preload additional data if desired
   }

   /**
    * ADD unit tests below here, use testMethod as the name of the unit test, Note that if a method
    * is overloaded you should include the arguments in the test name like so: testMethodClassInt
    * (for method(Class, int);
    */

   public void testValidTestData() {
      // ensure the test data is setup the way we think
      assertNotNull(td);
      assertNotNull(tdp);

      // assertEquals(new Long(1), tdp.pNode1.getId());
   }



   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#findEntityRefs(java.lang.String[], java.lang.String[], java.lang.String[], boolean)}.
    */
   public void testFindEntityRefs() {
      List<String> l = null;

      // test search with limit by prefix
      l = propertiesService.findEntityRefs(new String[] { TestData.PREFIX5 }, null, null, true);
      assertNotNull(l);
      assertEquals(2, l.size());
      assertTrue(l.contains(TestData.REF5));
      assertTrue(l.contains(TestData.REF5_2));

      // test search with limit by prefix (check that no results ok)
      l = propertiesService.findEntityRefs(new String[] { TestData.INVALID_REF }, null, null, true);
      assertNotNull(l);
      assertEquals(0, l.size());

      // test searching with multiple prefixes
      l = propertiesService.findEntityRefs(new String[] { TestData.PREFIX5, TestData.PREFIX1 }, null,
            null, true);
      assertNotNull(l);
      assertEquals(2, l.size());
      assertTrue(l.contains(TestData.REF5));
      assertTrue(l.contains(TestData.REF5_2));

      // test searching by names
      l = propertiesService.findEntityRefs(new String[] { TestData.PREFIX5 },
            new String[] { TestData.PROPERTY_NAME5A }, null, true);
      assertNotNull(l);
      assertEquals(1, l.size());
      assertTrue(l.contains(TestData.REF5));

      // test searching by invalid name (return no results)
      l = propertiesService.findEntityRefs(new String[] { TestData.PREFIX5 },
            new String[] { TestData.INVALID_REF }, null, true);
      assertNotNull(l);
      assertEquals(0, l.size());

      // test searching with multiple names
      l = propertiesService.findEntityRefs(new String[] { TestData.PREFIX5 }, new String[] {
            TestData.PROPERTY_NAME5A, TestData.PROPERTY_NAME5C }, null, true);
      assertNotNull(l);
      assertEquals(2, l.size());
      assertTrue(l.contains(TestData.REF5));
      assertTrue(l.contains(TestData.REF5_2));

      // test search limit by values (long exact match)
      l = propertiesService.findEntityRefs(new String[] { TestData.PREFIX5 },
            new String[] { TestData.PROPERTY_NAME5C }, new String[] { TestData.PROPERTY_VALUE5C },
            true);
      assertNotNull(l);
      assertEquals(1, l.size());
      assertTrue(l.contains(TestData.REF5_2));

      // test search limit by values (exact match)
      l = propertiesService.findEntityRefs(new String[] { TestData.PREFIX5 },
            new String[] { TestData.PROPERTY_NAME5B }, new String[] { TestData.PROPERTY_VALUE5B },
            true);
      assertNotNull(l);
      assertEquals(1, l.size());
      assertTrue(l.contains(TestData.REF5));

      // cannot have empty or null prefix
      try {
         l = propertiesService.findEntityRefs(new String[] {},
               new String[] { TestData.PROPERTY_NAME5A }, null, true);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      try {
         l = propertiesService.findEntityRefs(null, new String[] { TestData.PROPERTY_NAME5A }, null,
               true);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      // test search limit by values cannot have name null or empty
      try {
         l = propertiesService.findEntityRefs(new String[] { TestData.PREFIX5 }, new String[] {},
               new String[] { TestData.PROPERTY_VALUE5A }, true);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      // test name and values arrays must match sizes
      try {
         l = propertiesService.findEntityRefs(new String[] { TestData.PREFIX5 }, new String[] {
               TestData.PROPERTY_NAME5A, TestData.PROPERTY_NAME5B },
               new String[] { TestData.PROPERTY_VALUE5A }, true);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      // test search with all empty fields fail
      try {
         l = propertiesService.findEntityRefs(new String[] {}, new String[] {}, new String[] {}, false);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#getProperties(java.lang.String)}.
    */
   public void testGetProperties() {
      Map<String, String> m = null;

      m = propertiesService.getProperties(TestData.REF5);
      assertNotNull(m);
      assertEquals(2, m.size());
      assertTrue(m.containsKey(TestData.PROPERTY_NAME5A));
      assertTrue(m.containsKey(TestData.PROPERTY_NAME5B));

      m = propertiesService.getProperties(TestData.REF5_2);
      assertNotNull(m);
      assertEquals(1, m.size());
      assertTrue(m.containsKey(TestData.PROPERTY_NAME5C));

      // ref with no properties should fetch none
      m = propertiesService.getProperties(TestData.REF1);
      assertNotNull(m);
      assertTrue(m.isEmpty());

      // make sure invalid ref causes failure
      try {
         m = propertiesService.getProperties(TestData.INVALID_REF);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#getPropertyValue(java.lang.String, java.lang.String)}.
    */
   public void testGetPropertyValue() {
      String value = null;

      value = propertiesService.getPropertyValue(TestData.REF5, TestData.PROPERTY_NAME5A);
      assertNotNull(value);
      assertEquals(TestData.PROPERTY_VALUE5A, value);

      value = propertiesService.getPropertyValue(TestData.REF5, TestData.PROPERTY_NAME5B);
      assertNotNull(value);
      assertEquals(TestData.PROPERTY_VALUE5B, value);

      // test large value retrieval
      value = propertiesService.getPropertyValue(TestData.REF5_2, TestData.PROPERTY_NAME5C);
      assertNotNull(value);
      assertEquals(TestData.PROPERTY_VALUE5C, value);

      // nonexistent value property get retrieves null
      value = propertiesService.getPropertyValue(TestData.REF5, "XXXXXXXXXXXX");
      assertNull(value);

      // make sure invalid ref causes failure
      try {
         value = propertiesService.getPropertyValue(TestData.INVALID_REF, TestData.PROPERTY_NAME5A);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      // null name causes failure
      try {
         value = propertiesService.getPropertyValue(TestData.REF5, null);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      // empty name causes failure
      try {
         value = propertiesService.getPropertyValue(TestData.REF5, "");
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#setPropertyValue(java.lang.String, java.lang.String, java.lang.String[])}.
    */
   public void testSetPropertyValue() {
      String value = null;

      // check that we can save a new property on an entity
      propertiesService.setPropertyValue(TestData.REF5, "newNameAlpha", "newValueAlpha");
      value = propertiesService.getPropertyValue(TestData.REF5, "newNameAlpha");
      assertNotNull(value);
      assertEquals("newValueAlpha", value);

      // check that we can update an existing property on an entity
      propertiesService.setPropertyValue(TestData.REF5, TestData.PROPERTY_NAME5A, "AZnewValue");
      value = propertiesService.getPropertyValue(TestData.REF5, TestData.PROPERTY_NAME5A);
      assertNotNull(value);
      assertNotSame(TestData.PROPERTY_VALUE5A, value);
      assertEquals("AZnewValue", value);

      // check that we can remove a property on an entity
      propertiesService.setPropertyValue(TestData.REF5, "newNameAlpha", null);
      value = propertiesService.getPropertyValue(TestData.REF5, "newNameAlpha");
      assertNull(value);

      // check that we can remove all properties on an entity
      Map<String, String> m = propertiesService.getProperties(TestData.REF5);
      assertEquals(2, m.size());
      propertiesService.setPropertyValue(TestData.REF5, null, null);
      m = propertiesService.getProperties(TestData.REF5);
      assertEquals(0, m.size());

      // make sure invalid ref causes failure
      try {
         propertiesService.setPropertyValue(TestData.INVALID_REF, "newNameAlpha", "newValueAlpha");
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      // make sure invalid params cause failure
      try {
         propertiesService.setPropertyValue(TestData.REF1, null, "XXXXXXXXX");
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

   }
   
}
