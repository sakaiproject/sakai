/******************************************************************************
 * TestDataPreload.java - created by Sakai App Builder -AZ
 * 
 * Copyright (c) 2008 Sakai Project/Sakai Foundation
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 *****************************************************************************/

package org.sakaiproject.dashboard.logic;

import java.lang.reflect.Field;
import java.util.Date;

import org.sakaiproject.dashboard.model.DashboardItem;
import org.sakaiproject.genericdao.api.GenericDao;

/**
 * Contains test data for preloading and test constants
 * @author Sakai App Builder -AZ
 */
public class FakeDataPreload {

   /**
    * current user, access level user in LOCATION_ID1
    */
   public final static String USER_ID = "user-11111111";
   public final static String USER_DISPLAY = "Aaron Zeckoski";
   /**
    * access level user in LOCATION1_ID
    */
   public final static String ACCESS_USER_ID = "access-2222222";
   public final static String ACCESS_USER_DISPLAY = "Regular User";
   /**
    * maintain level user in LOCATION1_ID
    */
   public final static String MAINT_USER_ID = "maint-33333333";
   public final static String MAINT_USER_DISPLAY = "Maint User";
   /**
    * super admin user 
    */
   public final static String ADMIN_USER_ID = "admin";
   public final static String ADMIN_USER_DISPLAY = "Administrator";
   /**
    * Invalid user (also can be used to simulate the anonymous user) 
    */
   public final static String INVALID_USER_ID = "invalid-UUUUUU";

   /**
    * current location
    */
   public final static String LOCATION1_ID = "/site/ref-1111111";
   public final static String LOCATION1_TITLE = "Location 1 title";
   public final static String LOCATION2_ID = "/site/ref-22222222";
   public final static String LOCATION2_TITLE = "Location 2 title";
   public final static String INVALID_LOCATION_ID = "invalid-LLLLLLLL";

   // testing data objects here

   public DashboardItem item1 = new DashboardItem("Item 1", 1,"Description 1", "entityId-1", null, "accessUrl-1", LOCATION1_ID, LOCATION1_ID, LOCATION1_TITLE, "cool", new Date(), USER_ID, USER_DISPLAY);
   public DashboardItem item2 = new DashboardItem("Item 2", 1,"Description 1", "entityId-2", null, "accessUrl-2", LOCATION2_ID, LOCATION2_ID, LOCATION2_TITLE, "cool", new Date(), ACCESS_USER_ID, ACCESS_USER_DISPLAY);
   public DashboardItem accessitem = new DashboardItem("new access item", 1,"Description 1", "entityId-3", null, "accessUrl-3", LOCATION1_ID, LOCATION1_ID, LOCATION1_TITLE, "cool", new Date(), ACCESS_USER_ID, ACCESS_USER_DISPLAY);
   public DashboardItem maintitem = new DashboardItem("New maint item title", 1,"Description 1", "entityId-4", null, "accessUrl-4", LOCATION2_ID, LOCATION2_ID, LOCATION2_TITLE, "cool", new Date(), MAINT_USER_ID, MAINT_USER_DISPLAY);
   public DashboardItem adminitem = new DashboardItem("New admin item title", 1,"Description 1", "entityId-5", null, "accessUrl-5", LOCATION1_ID, LOCATION1_ID, LOCATION1_TITLE, "cool", new Date(), ADMIN_USER_ID, ADMIN_USER_DISPLAY);

   public GenericDao dao;
   public void setDao(GenericDao dao) {
       this.dao = dao;
   }

   public void init() {
       preloadTestData();
   }

   /**
    * Preload a bunch of test data into the database
    */
   public void preloadTestData() {
      /*
       * This iterates over the fields in FakeDataPreload and finds all the ones which are
       * of the type DashboardItem, then it runs the dao save method on them:
       * dao.findById(DashboardItem.class, item1.getId());
       * This does the same thing as writing a bunch of these:
       * dao.save(item1);
       */
      Field[] fields = this.getClass().getDeclaredFields();
      for (Field field : fields) {
         if (field.getType().equals(DashboardItem.class)) {
            try {
               dao.save((DashboardItem)field.get(this));
            } catch (Exception e) {
               throw new RuntimeException(e.getMessage(), e);
            }
         }
      }
   }

   /**
    * Reload the test data back into the current session so they can be tested correctly,
    * if this is not done then the preloaded data is in a separate session and equality tests will not work
    */
   public void reloadTestData() {
      /*
       * This iterates over the fields in FakeDataPreload and finds all the ones which are
       * of the type DashboardItem, then it sets the field equal to the method:
       * dao.findById(DashboardItem.class, item1.getId());
       * This does the same thing as writing a bunch of these:
       * item1 = (DashboardItem) dao.findById(DashboardItem.class, item1.getId());
       */
      Field[] fields = this.getClass().getDeclaredFields();
      for (Field field : fields) {
         if (field.getType().equals(DashboardItem.class)) {
            try {
               field.set(this, dao.findById(DashboardItem.class, ((DashboardItem)field.get(this)).getId()));
            } catch (Exception e) {
               throw new RuntimeException(e.getMessage(), e);
            }
         }
      }
   }

}
