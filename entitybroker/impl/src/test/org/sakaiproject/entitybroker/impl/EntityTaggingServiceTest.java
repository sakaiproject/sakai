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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.entitybroker.dao.EntityBrokerDao;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.impl.data.TestDataPreload;
import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

/**
 * Testing the entitybroker implementation of the tagging service
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
@ContextConfiguration(locations={
		"/database-test.xml",
		"classpath:org/sakaiproject/entitybroker/spring-jdbc.xml" })
public class EntityTaggingServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

   protected EntityTaggingService taggingService;

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
      taggingService = tm.entityTaggingService;
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
    * Test method for {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#getTags(java.lang.String)}.
    */
   @Test
   public void testGetTags() {
      List<String> tags = null;

      tags = taggingService.getTagsForEntity(TestData.REF1);
      Assert.assertNotNull(tags);
      Assert.assertEquals(2, tags.size());
      Assert.assertTrue(tags.contains("test"));
      Assert.assertTrue(tags.contains("aaronz"));

      tags = taggingService.getTagsForEntity(TestData.REF1_1);
      Assert.assertNotNull(tags);
      Assert.assertEquals(0, tags.size());

      // check that we cannot get tags for those which do not support it
      try {
         tags = taggingService.getTagsForEntity(TestData.REF2);
         Assert.fail("Should have thrown exception");
      } catch (UnsupportedOperationException e) {
         Assert.assertNotNull(e.getMessage());
      }
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#setTags(java.lang.String, java.util.Set)}.
    */
   @Test
   public void testSetTags() {
      // test adding new tags
      taggingService.setTagsForEntity(TestData.REF1_1, new String[] {"test"});
      Assert.assertEquals(1, taggingService.getTagsForEntity(TestData.REF1_1).size() );

      // test clearing tags
      taggingService.setTagsForEntity(TestData.REF1, new String[] {});
      Assert.assertEquals(0, taggingService.getTagsForEntity(TestData.REF1).size() );

      // test cannot add tags to refs that do not support it
      try {
         taggingService.setTagsForEntity(TestData.REF2, new String[] {"test"});
         Assert.fail("Should have thrown exception");
      } catch (UnsupportedOperationException e) {
         Assert.assertNotNull(e.getMessage());
      }
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#findEntityRefsByTags(java.lang.String[])}.
    */
   @Test
   public void testFindEntityRefsByTags() {
      List<EntityData> refs = null;

      refs = taggingService.findEntitesByTags(new String[] {"aaronz"}, null, false, null);
      Assert.assertNotNull(refs);
      Assert.assertEquals(1, refs.size());
      Assert.assertEquals(TestData.REF1, refs.get(0).getEntityReference());

      refs = taggingService.findEntitesByTags( new String[] {"AZ"}, null, false, null);
      Assert.assertNotNull(refs);
      Assert.assertEquals(2, refs.size());
      Assert.assertEquals(TestData.REFT1, refs.get(0).getEntityReference());
      Assert.assertEquals(TestData.REFT1_2, refs.get(1).getEntityReference());

      refs = taggingService.findEntitesByTags( new String[] {"test"}, null, false, null);
      Assert.assertNotNull(refs);
      Assert.assertEquals(2, refs.size());
      Assert.assertEquals(TestData.REFT1, refs.get(0).getEntityReference());
      Assert.assertEquals(TestData.REF1, refs.get(1).getEntityReference());

      taggingService.setTagsForEntity(TestData.REF1_1, new String[] {"test"});

      refs = taggingService.findEntitesByTags( new String[] {"test"}, null, false, null);
      Assert.assertNotNull(refs);
      Assert.assertEquals(3, refs.size());
      Assert.assertEquals(TestData.REFT1, refs.get(0).getEntityReference());
      Assert.assertEquals(TestData.REF1, refs.get(1).getEntityReference());
      Assert.assertEquals(TestData.REF1_1, refs.get(2).getEntityReference());

      refs = taggingService.findEntitesByTags( new String[] {"test"}, new String[] {TestData.PREFIX1}, false, null);
      Assert.assertNotNull(refs);
      Assert.assertEquals(2, refs.size());
      Assert.assertEquals(TestData.REF1, refs.get(0).getEntityReference());
      Assert.assertEquals(TestData.REF1_1, refs.get(1).getEntityReference());

      refs = taggingService.findEntitesByTags( new String[] {"test"}, new String[] {TestData.PREFIXT1}, false, null);
      Assert.assertNotNull(refs);
      Assert.assertEquals(1, refs.size());
      Assert.assertEquals(TestData.REFT1, refs.get(0).getEntityReference());

      refs = taggingService.findEntitesByTags( new String[] {"test", "AZ"}, null, false, null);
      Assert.assertNotNull(refs);
      Assert.assertEquals(4, refs.size());
      Assert.assertEquals(TestData.REFT1, refs.get(0).getEntityReference());
      Assert.assertEquals(TestData.REFT1_2, refs.get(1).getEntityReference());
      Assert.assertEquals(TestData.REF1, refs.get(2).getEntityReference());
      Assert.assertEquals(TestData.REF1_1, refs.get(3).getEntityReference());

// TODO this test is failing, need to research why      
//      refs = taggingService.findEntitesByTags( new String[] {"test", "AZ"}, null, true, null);
//      Assert.assertNotNull(refs);
//      Assert.assertEquals(1, refs.size());
//      Assert.assertEquals(TestData.REFT1, refs.get(0).getEntityReference());

      refs = taggingService.findEntitesByTags( new String[] {"ZZZZZZZZZ"}, null, false, null);
      Assert.assertNotNull(refs);
      Assert.assertEquals(0, refs.size());
   }

}
