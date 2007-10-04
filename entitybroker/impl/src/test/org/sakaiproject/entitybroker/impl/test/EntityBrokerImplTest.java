/**
 * EntityBrokerImplTest.java - 2007 Jul 21, 2007 3:04:55 PM - entity-broker - AZ
 */

package org.sakaiproject.entitybroker.impl.test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.easymock.MockControl;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.IdEntityReference;
import org.sakaiproject.entitybroker.dao.EntityBrokerDao;
import org.sakaiproject.entitybroker.impl.EntityBrokerImpl;
import org.sakaiproject.entitybroker.impl.EntityHandler;
import org.sakaiproject.entitybroker.impl.entityprovider.EntityProviderManagerImpl;
import org.sakaiproject.entitybroker.impl.test.data.TestDataPreload;
import org.sakaiproject.entitybroker.impl.test.mocks.FakeEvent;
import org.sakaiproject.entitybroker.impl.test.mocks.FakeServerConfigurationService;
import org.sakaiproject.entitybroker.mocks.data.MyEntity;
import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.sakaiproject.event.api.EventTrackingService;
import org.springframework.test.AbstractTransactionalSpringContextTests;

/**
 * Testing the entitybroker implementation
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class EntityBrokerImplTest extends AbstractTransactionalSpringContextTests {

   protected EntityBrokerImpl entityBroker;

   private EntityBrokerDao dao;
   private TestData td;
   private TestDataPreload tdp;

   private EntityManager entityManager;
   private MockControl entityManagerControl;
   private EventTrackingService eventTrackingService;
   private MockControl eventTrackingServiceControl;

   protected String[] getConfigLocations() {
      // point to the needed spring config files, must be on the classpath
      // (add component/src/webapp/WEB-INF to the build path in Eclipse),
      // they also need to be referenced in the project.xml file
      return new String[] { "hibernate-test.xml", "spring-hibernate.xml" };
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

      // setup the mock objects if needed
      entityManagerControl = MockControl.createControl(EntityManager.class);
      entityManager = (EntityManager) entityManagerControl.getMock();

      eventTrackingServiceControl = MockControl.createControl(EventTrackingService.class);
      eventTrackingService = (EventTrackingService) eventTrackingServiceControl.getMock();

      // setup the defaults for the mock objects (if there are any)
      // sessionManager.getCurrentSessionUserId(); // expect this to be called
      // sessionManagerControl.setDefaultMatcher(MockControl.ALWAYS_MATCHER);
      // sessionManagerControl.setReturnValue(TestDataPreload.USER_ID, MockControl.ZERO_OR_MORE);
      // sessionManagerControl.replay();
      eventTrackingService.newEvent(null, null, false, 0); // expect this to be called
      eventTrackingServiceControl.setDefaultMatcher(MockControl.ALWAYS_MATCHER);
      eventTrackingServiceControl.setReturnValue(new FakeEvent(), MockControl.ZERO_OR_MORE);
      eventTrackingService.post(null); // expect this to be called
      eventTrackingServiceControl.setVoidCallable(MockControl.ZERO_OR_MORE);
      eventTrackingServiceControl.replay();

      // setup fake internal services

      // Fully functional entity provider manager
      EntityProviderManagerImpl entityProviderManagerImpl = new EntityProviderManagerImpl();
      entityProviderManagerImpl.init();
      entityProviderManagerImpl.registerEntityProvider(td.entityProvider1);
      entityProviderManagerImpl.registerEntityProvider(td.entityProvider2);
      entityProviderManagerImpl.registerEntityProvider(td.entityProvider3);
      entityProviderManagerImpl.registerEntityProvider(td.entityProvider4);
      entityProviderManagerImpl.registerEntityProvider(td.entityProvider5);

      // Fully functional entity handler
      EntityHandler entityHandler = new EntityHandler();
      entityHandler.setEntityProviderManager(entityProviderManagerImpl);
      entityHandler.setServerConfigurationService(new FakeServerConfigurationService());

      // create and setup the object to be tested
      entityBroker = new EntityBrokerImpl();
      entityBroker.setDao(dao);
      entityBroker.setEntityHandler(entityHandler);
      entityBroker.setEntityProviderManager(entityProviderManagerImpl);
      entityBroker.setEntityManager(entityManager);
      entityBroker.setEventTrackingService(eventTrackingService);

      // run the init (just like what would normally happen)
      entityBroker.init();

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
    * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#entityExists(java.lang.String)}.
    */
   public void testEntityExists() {
      boolean exists = false;

      exists = entityBroker.entityExists(TestData.REF1);
      assertTrue(exists);

      exists = entityBroker.entityExists(TestData.REF1_1);
      assertTrue(exists);

      exists = entityBroker.entityExists(TestData.REF2);
      assertTrue(exists);

      // test that invalid id with valid prefix does not pass
      exists = entityBroker.entityExists(TestData.REF1_INVALID);
      assertFalse(exists);

      // test that unregistered ref does not pass
      exists = entityBroker.entityExists(TestData.REF9);
      assertFalse(exists);

      // test that invalid ref causes exception
      try {
         exists = entityBroker.entityExists(TestData.INVALID_REF);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#getEntityURL(java.lang.String)}.
    */
   public void testGetEntityURL() {
      String url = null;

      url = entityBroker.getEntityURL(TestData.REF1);
      assertEquals(TestData.URL1, url);

      url = entityBroker.getEntityURL(TestData.REF2);
      assertEquals(TestData.URL2, url);

      url = entityBroker.getEntityURL(TestData.REF1_INVALID);

      try {
         url = entityBroker.getEntityURL(TestData.INVALID_REF);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#getRegisteredPrefixes()}.
    */
   public void testGetRegisteredPrefixes() {
      Set<String> s = entityBroker.getRegisteredPrefixes();
      assertNotNull(s);
      assertTrue(s.contains(TestData.PREFIX1));
      assertTrue(s.contains(TestData.PREFIX2));
      assertTrue(s.contains(TestData.PREFIX3));
      assertFalse(s.contains(TestData.PREFIX9));
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#fireEvent(java.lang.String, java.lang.String)}.
    */
   public void testFireEvent() {
      // we are mostly handing this to a mocked service so we can only check to see if errors occur
      entityBroker.fireEvent(TestData.EVENT1_NAME, TestData.REF1);
      entityBroker.fireEvent(TestData.EVENT1_NAME, TestData.REF2);
      entityBroker.fireEvent(TestData.EVENT2_NAME, TestData.REF1);

      // event with a null name should die
      try {
         entityBroker.fireEvent(null, TestData.REF1);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      try {
         entityBroker.fireEvent("", TestData.REF1);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#parseReference(java.lang.String)}.
    */
   public void testParseReference() {
      EntityReference er = null;

      er = entityBroker.parseReference(TestData.REF1);
      assertNotNull(er);
      assertEquals(TestData.PREFIX1, er.prefix);
      assertTrue(er instanceof IdEntityReference);
      assertEquals(TestData.IDS1[0], ((IdEntityReference) er).id);

      er = entityBroker.parseReference(TestData.REF2);
      assertNotNull(er);
      assertEquals(TestData.PREFIX2, er.prefix);
      assertFalse(er instanceof IdEntityReference);

      // test parsing a defined reference
      er = entityBroker.parseReference(TestData.REF3);
      assertNotNull(er);
      assertEquals(TestData.PREFIX3, er.prefix);
      assertFalse(er instanceof IdEntityReference);

      // parsing of unregistered entity references returns null
      er = entityBroker.parseReference(TestData.REF9);
      assertNull(er);

      // parsing with nonexistent prefix returns null
      er = entityBroker.parseReference("/totallyfake/notreal");
      assertNull(er);
      
      try {
         er = entityBroker.parseReference(TestData.INVALID_REF);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

    

   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#fetchEntity(java.lang.String)}.
    */
   public void testFetchEntity() {
      Object obj = null;

      obj = entityBroker.fetchEntity(TestData.REF4);
      assertNotNull(obj);
      assertTrue(obj instanceof MyEntity);
      MyEntity entity = (MyEntity) obj;
      assertEquals(entity.id, TestData.IDS4[0]);

      obj = entityBroker.fetchEntity(TestData.REF4_two);
      assertNotNull(obj);
      assertTrue(obj instanceof MyEntity);
      MyEntity entity2 = (MyEntity) obj;
      assertEquals(entity2.id, TestData.IDS4[1]);

      // use an unregistered provider to trigger the attempt to do a legacy lookup
      try {
         obj = entityBroker.fetchEntity(TestData.REF9);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      // no object available should cause failure
      try {
         obj = entityBroker.fetchEntity(TestData.REF1);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      try {
         obj = entityBroker.fetchEntity(TestData.REF2);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      // expect invalid reference to fail
      try {
         obj = entityBroker.fetchEntity(TestData.INVALID_REF);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      try {
         obj = entityBroker.fetchEntity(null);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#findEntityRefs(java.lang.String[], java.lang.String[], java.lang.String[], boolean)}.
    */
   public void testFindEntityRefs() {
      List<String> l = null;

      // test search with limit by prefix
      l = entityBroker.findEntityRefs(new String[] { TestData.PREFIX5 }, null, null, true);
      assertNotNull(l);
      assertEquals(2, l.size());
      assertTrue(l.contains(TestData.REF5));
      assertTrue(l.contains(TestData.REF5_2));

      // test search with limit by prefix (check that no results ok)
      l = entityBroker.findEntityRefs(new String[] { TestData.INVALID_REF }, null, null, true);
      assertNotNull(l);
      assertEquals(0, l.size());

      // test searching with multiple prefixes
      l = entityBroker.findEntityRefs(new String[] { TestData.PREFIX5, TestData.PREFIX1 }, null,
            null, true);
      assertNotNull(l);
      assertEquals(2, l.size());
      assertTrue(l.contains(TestData.REF5));
      assertTrue(l.contains(TestData.REF5_2));

      // test searching by names
      l = entityBroker.findEntityRefs(new String[] { TestData.PREFIX5 },
            new String[] { TestData.PROPERTY_NAME5A }, null, true);
      assertNotNull(l);
      assertEquals(1, l.size());
      assertTrue(l.contains(TestData.REF5));

      // test searching by invalid name (return no results)
      l = entityBroker.findEntityRefs(new String[] { TestData.PREFIX5 },
            new String[] { TestData.INVALID_REF }, null, true);
      assertNotNull(l);
      assertEquals(0, l.size());

      // test searching with multiple names
      l = entityBroker.findEntityRefs(new String[] { TestData.PREFIX5 }, new String[] {
            TestData.PROPERTY_NAME5A, TestData.PROPERTY_NAME5C }, null, true);
      assertNotNull(l);
      assertEquals(2, l.size());
      assertTrue(l.contains(TestData.REF5));
      assertTrue(l.contains(TestData.REF5_2));

      // test search limit by values (long exact match)
      l = entityBroker.findEntityRefs(new String[] { TestData.PREFIX5 },
            new String[] { TestData.PROPERTY_NAME5C }, new String[] { TestData.PROPERTY_VALUE5C },
            true);
      assertNotNull(l);
      assertEquals(1, l.size());
      assertTrue(l.contains(TestData.REF5_2));

      // test search limit by values (exact match)
      l = entityBroker.findEntityRefs(new String[] { TestData.PREFIX5 },
            new String[] { TestData.PROPERTY_NAME5B }, new String[] { TestData.PROPERTY_VALUE5B },
            true);
      assertNotNull(l);
      assertEquals(1, l.size());
      assertTrue(l.contains(TestData.REF5));

      // cannot have empty or null prefix
      try {
         l = entityBroker.findEntityRefs(new String[] {},
               new String[] { TestData.PROPERTY_NAME5A }, null, true);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      try {
         l = entityBroker.findEntityRefs(null, new String[] { TestData.PROPERTY_NAME5A }, null,
               true);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      // test search limit by values cannot have name null or empty
      try {
         l = entityBroker.findEntityRefs(new String[] { TestData.PREFIX5 }, new String[] {},
               new String[] { TestData.PROPERTY_VALUE5A }, true);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      // test name and values arrays must match sizes
      try {
         l = entityBroker.findEntityRefs(new String[] { TestData.PREFIX5 }, new String[] {
               TestData.PROPERTY_NAME5A, TestData.PROPERTY_NAME5B },
               new String[] { TestData.PROPERTY_VALUE5A }, true);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      // test search with all empty fields fail
      try {
         l = entityBroker.findEntityRefs(new String[] {}, new String[] {}, new String[] {}, false);
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

      m = entityBroker.getProperties(TestData.REF5);
      assertNotNull(m);
      assertEquals(2, m.size());
      assertTrue(m.containsKey(TestData.PROPERTY_NAME5A));
      assertTrue(m.containsKey(TestData.PROPERTY_NAME5B));

      m = entityBroker.getProperties(TestData.REF5_2);
      assertNotNull(m);
      assertEquals(1, m.size());
      assertTrue(m.containsKey(TestData.PROPERTY_NAME5C));

      // ref with no properties should fetch none
      m = entityBroker.getProperties(TestData.REF1);
      assertNotNull(m);
      assertTrue(m.isEmpty());

      // make sure invalid ref causes failure
      try {
         m = entityBroker.getProperties(TestData.INVALID_REF);
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

      value = entityBroker.getPropertyValue(TestData.REF5, TestData.PROPERTY_NAME5A);
      assertNotNull(value);
      assertEquals(TestData.PROPERTY_VALUE5A, value);

      value = entityBroker.getPropertyValue(TestData.REF5, TestData.PROPERTY_NAME5B);
      assertNotNull(value);
      assertEquals(TestData.PROPERTY_VALUE5B, value);

      // test large value retrieval
      value = entityBroker.getPropertyValue(TestData.REF5_2, TestData.PROPERTY_NAME5C);
      assertNotNull(value);
      assertEquals(TestData.PROPERTY_VALUE5C, value);

      // nonexistent value property get retrieves null
      value = entityBroker.getPropertyValue(TestData.REF5, "XXXXXXXXXXXX");
      assertNull(value);

      // make sure invalid ref causes failure
      try {
         value = entityBroker.getPropertyValue(TestData.INVALID_REF, TestData.PROPERTY_NAME5A);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      // null name causes failure
      try {
         value = entityBroker.getPropertyValue(TestData.REF5, null);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      // empty name causes failure
      try {
         value = entityBroker.getPropertyValue(TestData.REF5, "");
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
      entityBroker.setPropertyValue(TestData.REF5, "newNameAlpha", "newValueAlpha");
      value = entityBroker.getPropertyValue(TestData.REF5, "newNameAlpha");
      assertNotNull(value);
      assertEquals("newValueAlpha", value);

      // check that we can update an existing property on an entity
      entityBroker.setPropertyValue(TestData.REF5, TestData.PROPERTY_NAME5A, "AZnewValue");
      value = entityBroker.getPropertyValue(TestData.REF5, TestData.PROPERTY_NAME5A);
      assertNotNull(value);
      assertNotSame(TestData.PROPERTY_VALUE5A, value);
      assertEquals("AZnewValue", value);

      // check that we can remove a property on an entity
      entityBroker.setPropertyValue(TestData.REF5, "newNameAlpha", null);
      value = entityBroker.getPropertyValue(TestData.REF5, "newNameAlpha");
      assertNull(value);

      // check that we can remove all properties on an entity
      Map<String, String> m = entityBroker.getProperties(TestData.REF5);
      assertEquals(2, m.size());
      entityBroker.setPropertyValue(TestData.REF5, null, null);
      m = entityBroker.getProperties(TestData.REF5);
      assertEquals(0, m.size());

      // make sure invalid ref causes failure
      try {
         entityBroker.setPropertyValue(TestData.INVALID_REF, "newNameAlpha", "newValueAlpha");
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      // make sure invalid params cause failure
      try {
         entityBroker.setPropertyValue(TestData.REF1, null, "XXXXXXXXX");
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

   }

}
