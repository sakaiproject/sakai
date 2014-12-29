/**
 * EntityBrokerDaoImplTest.java - created by aaronz on Jul 26, 2007
 */

package org.sakaiproject.entitybroker.impl.dao;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.entitybroker.dao.EntityBrokerDao;
import org.sakaiproject.entitybroker.dao.impl.EntityBrokerDaoImpl;
import org.sakaiproject.entitybroker.impl.data.TestDataPreload;
import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.springframework.test.AbstractTransactionalSpringContextTests;

/**
 * Testing the entity broker dao
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EntityBrokerDaoImplTest extends AbstractTransactionalSpringContextTests {

   private EntityBrokerDao dao;
   private TestDataPreload tdp;

   protected String[] getConfigLocations() {
      // point to the needed spring config files, must be on the classpath
      // (add component/src/webapp/WEB-INF to the build path in Eclipse),
      // they also need to be referenced in the project.xml file
      return new String[] { "database-test.xml", "classpath:org/sakaiproject/entitybroker/spring-jdbc.xml" };
   }

   // run this before each test starts
   protected void onSetUpBeforeTransaction() throws Exception {
   }

   // run this before each test starts and as part of the transaction
   protected void onSetUpInTransaction() {
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
   }

   /**
    * ADD unit tests below here, use testMethod as the name of the unit test, Note that if a method
    * is overloaded you should include the arguments in the test name like so: testMethodClassInt
    * (for method(Class, int);
    */

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.dao.impl.EntityBrokerDaoImpl#getEntityRefsForSearch(java.util.List, java.util.List, java.util.List)}.
    */
   public void testGetEntityRefsForSearch() {
      List<String> properties = new ArrayList<String>();
      List<String> values = new ArrayList<String>();
      List<Integer> comparisons = new ArrayList<Integer>();
      List<String> relations = new ArrayList<String>();
      List<String> results = null;

      // test that a basic fetch works
      properties.add("entityPrefix");
      values.add(TestData.PREFIX5);
      comparisons.add(Restriction.EQUALS);
      relations.add("or");

      results = dao.getEntityRefsForSearch(properties, values, comparisons, relations);
      assertNotNull(results);
      assertFalse(results.isEmpty());

      // test that a basic fetch works with like
      comparisons.clear();
      comparisons.add(Restriction.LIKE);
      results = dao.getEntityRefsForSearch(properties, values, comparisons, relations);
      assertNotNull(results);
      assertFalse(results.isEmpty());

      // test appending to the fetch with or
      properties.add("entityPrefix");
      values.add(TestData.PREFIX5);
      comparisons.add(Restriction.EQUALS);
      relations.add("or");

      results = dao.getEntityRefsForSearch(properties, values, comparisons, relations);
      assertNotNull(results);
      assertFalse(results.isEmpty());

      // test that a more complex fetch works
      properties.add("propertyName");
      values.add(TestData.PROPERTY_NAME5A);
      comparisons.add(Restriction.EQUALS);
      relations.add("and");

      properties.add("propertyValue");
      values.add(TestData.PROPERTY_VALUE5A);
      comparisons.add(Restriction.EQUALS);
      relations.add("and");

      results = dao.getEntityRefsForSearch(properties, values, comparisons, relations);
      assertNotNull(results);
      assertFalse(results.isEmpty());

      // test that all empty lists causes failure
      properties = new ArrayList<String>();
      values = new ArrayList<String>();
      comparisons = new ArrayList<Integer>();
      relations = new ArrayList<String>();

      try {
         dao.getEntityRefsForSearch(properties, values, comparisons, relations);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      // test that null lists causes failure
      try {
         dao.getEntityRefsForSearch(null, null, null, null);
         fail("Should have thrown exception");
      } catch (NullPointerException e) {
         assertNotNull(e);
      }

   }

   /**
    * Test method for {@link EntityBrokerDaoImpl#deleteProperties(String, String)}
    */
   public void testDeleteProperties() {
      // test that we can remove a property
      int removed = dao.deleteProperties(TestData.REF5_2, TestData.PROPERTY_NAME5C);
      assertEquals(1, removed);

      // test removing all properties
      removed = dao.deleteProperties(TestData.REF5, null);
      assertEquals(2, removed);
   }

   public void testDeleteTags() {
      // test we can remove a tag
      int removed = dao.deleteTags(TestData.REFT1, new String[] {"test","AZ"});
      assertEquals(2, removed);

      // test we can remove all tags
      removed = dao.deleteTags(TestData.REFT1_2, null);
      assertEquals(1, removed);
   }
   
}
