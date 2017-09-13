/**
 * Copyright (c) 2007-2009 The Apereo Foundation
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
 * TestDataPreload.java - created by Sakai App Builder -AZ
 */

package org.sakaiproject.entitybroker.impl.data;

import org.junit.Ignore;
import org.sakaiproject.entitybroker.dao.EntityProperty;
import org.sakaiproject.entitybroker.dao.EntityTagApplication;
import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.sakaiproject.genericdao.api.GenericDao;

/**
 * Contains test data for preloading and test constants
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
@Ignore // does not contain tests but we have to tell junit this
public class TestDataPreload {

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

   public EntityTagApplication tag1_aaronz = new EntityTagApplication(TestData.REFT1, TestData.PREFIXT1, "test");
   public EntityTagApplication tag1_test = new EntityTagApplication(TestData.REFT1, TestData.PREFIXT1, "AZ");
   public EntityTagApplication tag2_test = new EntityTagApplication(TestData.REFT1_2, TestData.PREFIXT1, "AZ");
   // no tags on the third one

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

      dao.save(tag1_aaronz);
      dao.save(tag1_test);
      dao.save(tag2_test);

      preloaded = true;
   }

}
