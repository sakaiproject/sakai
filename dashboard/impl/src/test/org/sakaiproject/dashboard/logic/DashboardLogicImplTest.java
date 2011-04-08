/******************************************************************************
 * DashboardLogicImplTest.java - created by Sakai App Builder -AZ
 * 
 * Copyright (c) 2008 Sakai Project/Sakai Foundation
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 *****************************************************************************/

package org.sakaiproject.dashboard.logic;

import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.sakaiproject.dashboard.dao.DashboardDao;
import org.sakaiproject.dashboard.logic.DashboardLogicImpl;
import org.sakaiproject.dashboard.logic.stubs.ExternalLogicStub;
import org.sakaiproject.dashboard.model.DashboardItem;
import org.springframework.test.AbstractTransactionalSpringContextTests;


/**
 * Testing the Logic implementation methods
 * @author Sakai App Builder -AZ
 */
public class DashboardLogicImplTest extends AbstractTransactionalSpringContextTests {

   protected DashboardLogicImpl logicImpl;

   private FakeDataPreload tdp;

   protected String[] getConfigLocations() {
      // point to the needed spring config files, must be on the classpath
      // (add component/src/webapp/WEB-INF to the build path in Eclipse),
      // they also need to be referenced in the project.xml file
      return new String[] {"hibernate-test.xml", "spring-hibernate.xml"};
   }

   // run this before each test starts
   protected void onSetUpBeforeTransaction() throws Exception {
   }

   // run this before each test starts and as part of the transaction
   protected void onSetUpInTransaction() {
      // load the spring created dao class bean from the Spring Application Context
      DashboardDao dao = (DashboardDao) applicationContext.
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

      // reload the test objects in this session
      tdp.reloadTestData();
      
      // init the class if needed

      // setup the mock objects

      // create and setup the object to be tested
      logicImpl = new DashboardLogicImpl();
      logicImpl.setDao(dao);
      logicImpl.setExternalLogic( new ExternalLogicStub() );

      // can set up the default mock object returns here if desired
      // Note: Still need to activate them in the test methods though

      // run the init
      logicImpl.init();
   }

   /**
    * Dashboard method for {@link org.sakaiproject.dashboard.logic.impl.DashboardLogicImpl#getItemById(java.lang.Long)}.
    */
   public void testGetItemById() {
      DashboardItem item = logicImpl.getItemById(tdp.item1.getId());
      Assert.assertNotNull(item);
      Assert.assertEquals(item, tdp.item1);

      DashboardItem baditem = logicImpl.getItemById( new Long(-1) );
      Assert.assertNull(baditem);

      try {
         logicImpl.getItemById(null);
         Assert.fail("Should have thrown IllegalArgumentException");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e.getMessage());
      }
   }

   /**
    * Dashboard method for {@link org.sakaiproject.dashboard.logic.impl.DashboardLogicImpl#canWriteItem(org.sakaiproject.dashboard.model.DashboardItem, java.lang.String, java.lang.String)}.
    */
   public void testCanWriteItemDashboardItemStringString() {
      // testing perms as a normal user
      Assert.assertFalse( logicImpl.canWriteItem(tdp.adminitem, FakeDataPreload.LOCATION1_ID, FakeDataPreload.USER_ID) );
      Assert.assertFalse( logicImpl.canWriteItem(tdp.maintitem, FakeDataPreload.LOCATION1_ID, FakeDataPreload.USER_ID) );
      Assert.assertTrue( logicImpl.canWriteItem(tdp.item1, FakeDataPreload.LOCATION1_ID, FakeDataPreload.USER_ID) );

      // testing perms as user with special perms
      Assert.assertTrue( logicImpl.canWriteItem(tdp.adminitem, FakeDataPreload.LOCATION1_ID, FakeDataPreload.MAINT_USER_ID) );
      Assert.assertTrue( logicImpl.canWriteItem(tdp.maintitem, FakeDataPreload.LOCATION1_ID, FakeDataPreload.MAINT_USER_ID) );
      Assert.assertTrue( logicImpl.canWriteItem(tdp.item1, FakeDataPreload.LOCATION1_ID, FakeDataPreload.MAINT_USER_ID) );

      // testing perms as admin user
      Assert.assertTrue( logicImpl.canWriteItem(tdp.adminitem, FakeDataPreload.LOCATION1_ID, FakeDataPreload.ADMIN_USER_ID) );
      Assert.assertTrue( logicImpl.canWriteItem(tdp.maintitem, FakeDataPreload.LOCATION1_ID, FakeDataPreload.ADMIN_USER_ID) );
      Assert.assertTrue( logicImpl.canWriteItem(tdp.item1, FakeDataPreload.LOCATION1_ID, FakeDataPreload.ADMIN_USER_ID) );
   }

//   /**
//    * Dashboard method for {@link org.sakaiproject.dashboard.logic.impl.DashboardLogicImpl#getAllVisibleItems(java.lang.String, java.lang.String)}.
//    */
//   public void testGetAllVisibleItemsStringString() {
//
//      // add 2 items to test if we can see the visible one and not the hidden one
//      DashboardItem itemHidden = new DashboardItem("New item title", 
//            FakeDataPreload.MAINT_USER_ID, FakeDataPreload.LOCATION1_ID, Boolean.TRUE, new Date());
//      logicImpl.saveItem(itemHidden);
//      DashboardItem itemVisible = new DashboardItem("New item title", 
//            FakeDataPreload.MAINT_USER_ID, FakeDataPreload.LOCATION1_ID, Boolean.FALSE, new Date());
//      logicImpl.saveItem(itemVisible);
//
//      List<DashboardItem> l = logicImpl.getAllVisibleItems(FakeDataPreload.LOCATION1_ID, FakeDataPreload.USER_ID); // test normal user
//      Assert.assertNotNull(l);
//      Assert.assertEquals(4, l.size());
//      Assert.assertTrue(l.contains(tdp.item1));
//      Assert.assertTrue(! l.contains(tdp.item2));
//      Assert.assertTrue(l.contains(itemVisible));
//      Assert.assertTrue(! l.contains(itemHidden));
//
//      List<DashboardItem> lmaintain = logicImpl.getAllVisibleItems(FakeDataPreload.LOCATION1_ID, FakeDataPreload.MAINT_USER_ID); // test maintainer
//      Assert.assertNotNull(lmaintain);
//      Assert.assertEquals(5, lmaintain.size());
//      Assert.assertTrue(lmaintain.contains(tdp.item1));
//      Assert.assertTrue(! lmaintain.contains(tdp.item2));
//      Assert.assertTrue(lmaintain.contains(itemVisible));
//      Assert.assertTrue(lmaintain.contains(itemHidden));
//
//      List<DashboardItem> ladmin = logicImpl.getAllVisibleItems(FakeDataPreload.LOCATION1_ID, FakeDataPreload.ADMIN_USER_ID); // test admin
//      Assert.assertNotNull(ladmin);
//      Assert.assertEquals(5, ladmin.size());
//      Assert.assertTrue(ladmin.contains(tdp.item1));
//      Assert.assertTrue(! ladmin.contains(tdp.item2));
//      Assert.assertTrue(ladmin.contains(itemVisible));
//      Assert.assertTrue(ladmin.contains(itemHidden));
//   }

   /**
    * Dashboard method for {@link org.sakaiproject.dashboard.logic.impl.DashboardLogicImpl#removeItem(org.sakaiproject.dashboard.model.DashboardItem)}.
    */
   public void testRemoveItem() {
      try {
         logicImpl.removeItem(tdp.adminitem); // user cannot delete this
         Assert.fail("Should have thrown SecurityException");
      } catch (SecurityException e) {
         Assert.assertNotNull(e.getMessage());
      }

      try {
         logicImpl.removeItem(tdp.adminitem); // permed user cannot delete this
         Assert.fail("Should have thrown SecurityException");
      } catch (SecurityException e) {
         Assert.assertNotNull(e.getMessage());
      }

      logicImpl.removeItem(tdp.item1); // user can delete this
      DashboardItem item = logicImpl.getItemById(tdp.item1.getId());
      Assert.assertNull(item);
   }

   /**
    * Dashboard method for {@link org.sakaiproject.dashboard.logic.impl.DashboardLogicImpl#saveItem(org.sakaiproject.dashboard.model.DashboardItem)}.
    */
   public void testSaveItem() {
	   
      DashboardItem item = new DashboardItem("New item title",  1, "New item description", "x-entityId-x", null, 
    		  "x-accessUrl-x", FakeDataPreload.LOCATION1_ID, FakeDataPreload.LOCATION1_ID, FakeDataPreload.LOCATION1_TITLE, "cool",
    		  new Date(), FakeDataPreload.USER_ID, FakeDataPreload.USER_DISPLAY);
      try {
    	  logicImpl.saveItem(item);
      } catch (Exception e) {
    	  Assert.fail("Should not have been thrown Exception");
      }
	  Long itemId = item.getId();
	  Assert.assertNotNull(itemId);
	  Assert.assertNotNull(item.getCreatedDate());

      item = logicImpl.getItemById(itemId);
      Assert.assertNotNull(item);     
      Assert.assertEquals(item.getCreatorId(), FakeDataPreload.USER_ID);
      Assert.assertEquals(item.getLocationId(), FakeDataPreload.LOCATION1_ID);
      Assert.assertEquals(item.getCreatorName(), FakeDataPreload.USER_DISPLAY);
      Assert.assertEquals(item.getLocationName(), FakeDataPreload.LOCATION1_TITLE);
      
      // more tests for not null?

      // test saving an incomplete item
      DashboardItem incompleteItem = new DashboardItem();
      incompleteItem.setTitle("New incomplete item");

      try {
    	  logicImpl.saveItem(incompleteItem);
    	  Assert.fail("Should have been thrown Exception");
      } catch(Exception e) {
    	  Assert.assertNotNull(e.getStackTrace());
      }

      Long incItemId = incompleteItem.getId();
      Assert.assertNull(incItemId);


      // test saving a null value for failure
      try {
         logicImpl.saveItem(null);
         Assert.fail("Should have thrown NullPointerException");
      } catch (NullPointerException e) {
         Assert.assertNotNull(e.getStackTrace());
      }
   }

}
