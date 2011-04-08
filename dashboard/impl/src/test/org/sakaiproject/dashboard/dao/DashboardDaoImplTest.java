/******************************************************************************
 * DashboardDaoImplTest.java - created by Sakai App Builder -AZ
 * 
 * Copyright (c) 2008 Sakai Project/Sakai Foundation
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 *****************************************************************************/

package org.sakaiproject.dashboard.dao;

import java.util.Date;

import junit.framework.Assert;

import org.sakaiproject.dashboard.dao.DashboardDao;
import org.sakaiproject.dashboard.logic.FakeDataPreload;
import org.sakaiproject.dashboard.model.DashboardItem;
import org.springframework.test.AbstractTransactionalSpringContextTests;

/**
 * Testing for the specialized DAO methods (do not test the Generic Dao methods)
 * @author Sakai App Builder -AZ
 */
public class DashboardDaoImplTest extends AbstractTransactionalSpringContextTests {

   protected DashboardDao dao;
   private FakeDataPreload tdp;

   private DashboardItem item;

   private final static String ITEM_TITLE = "New Title";
   private final static String ITEM_DESCRIPTION = "Description of New Item";
   private final static String ITEM_ENTITY_ID = "entityId-Z";
   private final static String ITEM_ACCESS_URL = "accessUrl-Z";
   
   private static final String ITEM_LOCATION_NAME = "Location Name";
   private static final String ITEM_STATUS = "my status";
   private static final String ITEM_LOCATION_URL = "Location URL";
   private static final Date ITEM_DUE_DATE = new Date();
   private static final String ITEM_LOCATION_ID = "/site/1234567890";
   private static final String ITEM_CREATOR_NAME = "Ms Item Creator";
   private static final String ITEM_CREATOR_ID = "superman";


   protected String[] getConfigLocations() {
      // point to the needed spring config files, must be on the classpath
      // (add component/src/webapp/WEB-INF to the build path in Eclipse),
      // they also need to be referenced in the project.xml file
      return new String[] {"hibernate-test.xml", "spring-hibernate.xml"};
   }

   // run this before each test starts
   protected void onSetUpBeforeTransaction() throws Exception {
      // create test objects
      item = new DashboardItem(ITEM_TITLE, 1, ITEM_DESCRIPTION, ITEM_ENTITY_ID, null, 
    		  ITEM_ACCESS_URL, ITEM_LOCATION_ID, ITEM_LOCATION_URL, ITEM_LOCATION_NAME, ITEM_STATUS,
    		  ITEM_DUE_DATE, ITEM_CREATOR_ID, ITEM_CREATOR_NAME);
   }

   // run this before each test starts and as part of the transaction
   protected void onSetUpInTransaction() {
      // load the spring created dao class bean from the Spring Application Context
      dao = (DashboardDao) applicationContext.
         getBean("org.sakaiproject.dashboard.dao.DashboardDao");
      if (dao == null) {
         throw new NullPointerException("DAO could not be retrieved from spring context");
      }

      // load up the test data preloader from spring
      tdp = (FakeDataPreload) applicationContext.
         getBean("org.sakaiproject.dashboard.logic.test.FakeDataPreload");
      if (tdp == null) {
         throw new NullPointerException("FakeDataPreload could not be retrieved from spring context");
      }

      // init the class if needed

      // check the preloaded data
      Assert.assertTrue("Error preloading data", dao.countAll(DashboardItem.class) > 0);

      // preload data if desired
      dao.save(item);
   }


   /**
    * ADD unit tests below here, use testMethod as the name of the unit test,
    * Note that if a method is overloaded you should include the arguments in the
    * test name like so: testMethodClassInt (for method(Class, int);
    */


   // THESE ARE SAMPLE UNIT TESTS WHICH SHOULD BE REMOVED LATER -AZ
   /**
    * TODO - Remove this sample unit test
    * Test method for {@link org.sakaiproject.dashboard.dao.impl.GenericHibernateDao#save(java.lang.Object)}.
    */
   public void testSave() {
      DashboardItem item1 = new DashboardItem("New item1", 1, ITEM_DESCRIPTION, ITEM_ENTITY_ID, null, 
    		  ITEM_ACCESS_URL, ITEM_LOCATION_ID, ITEM_LOCATION_URL, ITEM_LOCATION_NAME, ITEM_STATUS,
    		  ITEM_DUE_DATE, ITEM_CREATOR_ID, ITEM_CREATOR_NAME);
      dao.save(item1);
      Long itemId = item1.getId();
      Assert.assertNotNull(itemId);
      Assert.assertEquals(7, dao.countAll(DashboardItem.class));
   }

   /**
    * TODO - Remove this sample unit test
    * Test method for {@link org.sakaiproject.dashboard.dao.impl.GenericHibernateDao#delete(java.lang.Object)}.
    */
   public void testDelete() {
      Assert.assertEquals(dao.countAll(DashboardItem.class), 6);
      dao.delete(item);
      Assert.assertEquals(dao.countAll(DashboardItem.class), 5);
   }

   /**
    * TODO - Remove this sample unit test
    * Test method for {@link org.sakaiproject.dashboard.dao.impl.GenericHibernateDao#findById(java.lang.Class, java.io.Serializable)}.
    */
   public void testFindById() {
      Long id = item.getId();
      Assert.assertNotNull(id);
      DashboardItem item1 = (DashboardItem) dao.findById(DashboardItem.class, id);
      Assert.assertNotNull(item1);
      Assert.assertEquals(item, item1);
   }

   /**
    * Add anything that supports the unit tests below here
    */
}
