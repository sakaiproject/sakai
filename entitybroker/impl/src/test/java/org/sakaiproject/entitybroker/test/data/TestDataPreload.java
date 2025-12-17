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

package org.sakaiproject.entitybroker.test.data;

import org.sakaiproject.entitybroker.impl.EntityMetaPropertiesService;
import org.sakaiproject.entitybroker.impl.EntityTaggingService;
import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.sakaiproject.entitybroker.model.EntityProperty;
import org.sakaiproject.entitybroker.model.EntityTagApplication;

/**
 * Contains test data for preloading and test constants
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class TestDataPreload {

   public EntityProperty prop1;
   public EntityProperty prop1B;
   public EntityProperty prop1C;

   public EntityTagApplication tag1_aaronz;
   public EntityTagApplication tag1_test;
   public EntityTagApplication tag2_test;

   public TestDataPreload(EntityMetaPropertiesService emps, EntityTaggingService ets) {
      prop1 = new EntityProperty(TestData.REF5, TestData.PREFIX5, TestData.PROPERTY_NAME5A, TestData.PROPERTY_VALUE5A);
      emps.setPropertyValue(prop1.getEntityRef(), prop1.getPropertyName(), prop1.getPropertyValue());

      prop1B = new EntityProperty(TestData.REF5, TestData.PREFIX5, TestData.PROPERTY_NAME5B, TestData.PROPERTY_VALUE5B);
      emps.setPropertyValue(prop1B.getEntityRef(), prop1B.getPropertyName(), prop1B.getPropertyValue());

      prop1C = new EntityProperty(TestData.REF5_2, TestData.PREFIX5, TestData.PROPERTY_NAME5C, TestData.PROPERTY_VALUE5C);
      emps.setPropertyValue(prop1C.getEntityRef(), prop1C.getPropertyName(), prop1C.getPropertyValue());

      tag1_aaronz = new EntityTagApplication(TestData.REFT1, TestData.PREFIXT1, "test");
      ets.addTagsToEntity(tag1_aaronz.getEntityRef(), new String[]{tag1_aaronz.getTag()});

      tag1_test = new EntityTagApplication(TestData.REFT1, TestData.PREFIXT1, "AZ");
      ets.addTagsToEntity(tag1_test.getEntityRef(), new String[]{tag1_test.getTag()});

      tag2_test = new EntityTagApplication(TestData.REFT1_2, TestData.PREFIXT1, "AZ");
      ets.addTagsToEntity(tag2_test.getEntityRef(), new String[]{tag2_test.getTag()});
   }
}
