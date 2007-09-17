/**
 * TestDataPreload.java - created by Sakai App Builder -AZ
 */

package org.sakaiproject.entitybroker.impl.test.data;

import org.sakaiproject.entitybroker.dao.model.EntityProperty;
import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.sakaiproject.genericdao.api.GenericDao;

/**
 * Contains test data for preloading and test constants
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class TestDataPreload {

   public TestData td = new TestData();

   public GenericDao dao;

   public void setDao(GenericDao dao) {
      this.dao = dao;
   }

   public void init() {
      preloadTestData(dao);
   }

   // testing data objects here
   public EntityProperty prop1 = new EntityProperty(TestData.REF5, TestData.PREFIX5,
         TestData.PROPERTY_NAME5A, TestData.PROPERTY_VALUE5A);
   public EntityProperty prop1B = new EntityProperty(TestData.REF5, TestData.PREFIX5,
         TestData.PROPERTY_NAME5B, TestData.PROPERTY_VALUE5B);
   public EntityProperty prop1C = new EntityProperty(TestData.REF5_2, TestData.PREFIX5,
         TestData.PROPERTY_NAME5C, TestData.PROPERTY_VALUE5C);

   public boolean preloaded = false;

   /**
    * Preload a bunch of test data into the database
    * 
    * @param dao
    */
   public void preloadTestData(GenericDao dao) {
      dao.save(prop1);
      dao.save(prop1B);
      dao.save(prop1C);

      preloaded = true;
   }

}
