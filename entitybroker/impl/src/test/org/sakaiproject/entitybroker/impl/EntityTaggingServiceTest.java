/**
 * EntityBrokerImplTest.java - 2007 Jul 21, 2007 3:04:55 PM - entity-broker - AZ
 */

package org.sakaiproject.entitybroker.impl;

import java.util.List;

import org.sakaiproject.entitybroker.dao.EntityBrokerDao;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.impl.data.TestDataPreload;
import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.springframework.test.AbstractTransactionalSpringContextTests;

/**
 * Testing the entitybroker implementation of the tagging service
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class EntityTaggingServiceTest extends AbstractTransactionalSpringContextTests {

   protected EntityTaggingService taggingService;

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
      taggingService = tm.entityTaggingService;

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
    * Test method for {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#getTags(java.lang.String)}.
    */
   public void testGetTags() {
      List<String> tags = null;

      tags = taggingService.getTagsForEntity(TestData.REF1);
      assertNotNull(tags);
      assertEquals(2, tags.size());
      assertTrue(tags.contains("test"));
      assertTrue(tags.contains("aaronz"));

      tags = taggingService.getTagsForEntity(TestData.REF1_1);
      assertNotNull(tags);
      assertEquals(0, tags.size());

      // check that we cannot get tags for those which do not support it
      try {
         tags = taggingService.getTagsForEntity(TestData.REF2);
         fail("Should have thrown exception");
      } catch (UnsupportedOperationException e) {
         assertNotNull(e.getMessage());
      }
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#setTags(java.lang.String, java.util.Set)}.
    */
   public void testSetTags() {
      // test adding new tags
      taggingService.setTagsForEntity(TestData.REF1_1, new String[] {"test"});
      assertEquals(1, taggingService.getTagsForEntity(TestData.REF1_1).size() );

      // test clearing tags
      taggingService.setTagsForEntity(TestData.REF1, new String[] {});
      assertEquals(0, taggingService.getTagsForEntity(TestData.REF1).size() );

      // test cannot add tags to refs that do not support it
      try {
         taggingService.setTagsForEntity(TestData.REF2, new String[] {"test"});
         fail("Should have thrown exception");
      } catch (UnsupportedOperationException e) {
         assertNotNull(e.getMessage());
      }
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#findEntityRefsByTags(java.lang.String[])}.
    */
   public void testFindEntityRefsByTags() {
      List<EntityData> refs = null;

      refs = taggingService.findEntitesByTags(new String[] {"aaronz"}, null, false, null);
      assertNotNull(refs);
      assertEquals(1, refs.size());
      assertEquals(TestData.REF1, refs.get(0).getEntityReference());

      refs = taggingService.findEntitesByTags( new String[] {"AZ"}, null, false, null);
      assertNotNull(refs);
      assertEquals(2, refs.size());
      assertEquals(TestData.REFT1, refs.get(0).getEntityReference());
      assertEquals(TestData.REFT1_2, refs.get(1).getEntityReference());

      refs = taggingService.findEntitesByTags( new String[] {"test"}, null, false, null);
      assertNotNull(refs);
      assertEquals(2, refs.size());
      assertEquals(TestData.REFT1, refs.get(0).getEntityReference());
      assertEquals(TestData.REF1, refs.get(1).getEntityReference());

      taggingService.setTagsForEntity(TestData.REF1_1, new String[] {"test"});

      refs = taggingService.findEntitesByTags( new String[] {"test"}, null, false, null);
      assertNotNull(refs);
      assertEquals(3, refs.size());
      assertEquals(TestData.REFT1, refs.get(0).getEntityReference());
      assertEquals(TestData.REF1, refs.get(1).getEntityReference());
      assertEquals(TestData.REF1_1, refs.get(2).getEntityReference());

      refs = taggingService.findEntitesByTags( new String[] {"test"}, new String[] {TestData.PREFIX1}, false, null);
      assertNotNull(refs);
      assertEquals(2, refs.size());
      assertEquals(TestData.REF1, refs.get(0).getEntityReference());
      assertEquals(TestData.REF1_1, refs.get(1).getEntityReference());

      refs = taggingService.findEntitesByTags( new String[] {"test"}, new String[] {TestData.PREFIXT1}, false, null);
      assertNotNull(refs);
      assertEquals(1, refs.size());
      assertEquals(TestData.REFT1, refs.get(0).getEntityReference());

      refs = taggingService.findEntitesByTags( new String[] {"test", "AZ"}, null, false, null);
      assertNotNull(refs);
      assertEquals(4, refs.size());
      assertEquals(TestData.REFT1, refs.get(0).getEntityReference());
      assertEquals(TestData.REFT1_2, refs.get(1).getEntityReference());
      assertEquals(TestData.REF1, refs.get(2).getEntityReference());
      assertEquals(TestData.REF1_1, refs.get(3).getEntityReference());

      refs = taggingService.findEntitesByTags( new String[] {"test", "AZ"}, null, true, null);
      assertNotNull(refs);
      assertEquals(1, refs.size());
      assertEquals(TestData.REFT1, refs.get(0).getEntityReference());

      refs = taggingService.findEntitesByTags( new String[] {"ZZZZZZZZZ"}, null, false, null);
      assertNotNull(refs);
      assertEquals(0, refs.size());
   }

}
