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
 * EntityBrokerDaoImplTest.java - created by aaronz on Jul 26, 2007
 */

package org.sakaiproject.entitybroker.impl.dao;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.entitybroker.dao.EntityBrokerDao;
import org.sakaiproject.entitybroker.dao.impl.EntityBrokerDaoImpl;
import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

/**
 * Testing the entity broker dao
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
@ContextConfiguration(locations={
		"/database-test.xml",
		"classpath:org/sakaiproject/entitybroker/spring-jdbc.xml" })
public class EntityBrokerDaoImplTest extends AbstractTransactionalJUnit4SpringContextTests {

   @Autowired
   @Qualifier("org.sakaiproject.entitybroker.dao.EntityBrokerDao")
   private EntityBrokerDao dao;

   /**
    * ADD unit tests below here, use testMethod as the name of the unit test, Note that if a method
    * is overloaded you should include the arguments in the test name like so: testMethodClassInt
    * (for method(Class, int);
    */
   
   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.dao.impl.EntityBrokerDaoImpl#getEntityRefsForSearch(java.util.List, java.util.List, java.util.List)}.
    */
   @Test
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
      Assert.assertNotNull(results);
      Assert.assertFalse(results.isEmpty());

      // test that a basic fetch works with like
      comparisons.clear();
      comparisons.add(Restriction.LIKE);
      results = dao.getEntityRefsForSearch(properties, values, comparisons, relations);
      Assert.assertNotNull(results);
      Assert.assertFalse(results.isEmpty());

      // test appending to the fetch with or
      properties.add("entityPrefix");
      values.add(TestData.PREFIX5);
      comparisons.add(Restriction.EQUALS);
      relations.add("or");

      results = dao.getEntityRefsForSearch(properties, values, comparisons, relations);
      Assert.assertNotNull(results);
      Assert.assertFalse(results.isEmpty());

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
      Assert.assertNotNull(results);
      Assert.assertFalse(results.isEmpty());

      // test that all empty lists causes failure
      properties = new ArrayList<String>();
      values = new ArrayList<String>();
      comparisons = new ArrayList<Integer>();
      relations = new ArrayList<String>();

      try {
         dao.getEntityRefsForSearch(properties, values, comparisons, relations);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e.getMessage());
      }

      // test that null lists causes failure
      try {
         dao.getEntityRefsForSearch(null, null, null, null);
         Assert.fail("Should have thrown exception");
      } catch (NullPointerException e) {
         Assert.assertNotNull(e);
      }

   }

   /**
    * Test method for {@link EntityBrokerDaoImpl#deleteProperties(String, String)}
    */
   @Test
   public void testDeleteProperties() {
      // test that we can remove a property
      int removed = dao.deleteProperties(TestData.REF5_2, TestData.PROPERTY_NAME5C);
      Assert.assertEquals(1, removed);

      // test removing all properties
      removed = dao.deleteProperties(TestData.REF5, null);
      Assert.assertEquals(2, removed);
   }

   @Test
   public void testDeleteTags() {
      // test we can remove a tag, need to research why
      int removed = dao.deleteTags(TestData.REFT1, new String[] {"test","AZ"});
      Assert.assertEquals(2, removed);

      // test we can remove all tags
      removed = dao.deleteTags(TestData.REFT1_2, null);
      Assert.assertEquals(1, removed);
   }
   
}
