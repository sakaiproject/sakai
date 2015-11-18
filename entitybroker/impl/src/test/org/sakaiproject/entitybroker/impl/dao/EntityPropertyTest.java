/******************************************************************************
 * EntityPropertyTest.java - created by aaronz on Jul 24, 2007
 * 
 * Copyright (c) 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 *****************************************************************************/

package org.sakaiproject.entitybroker.impl.dao;

import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.entitybroker.dao.EntityProperty;

/**
 * Note really sure this test is needed
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EntityPropertyTest {

   private final Long ID = Long.valueOf(1);
   private final String REF = "ref";
   private final String PREFIX = "prefix";
   private final String NAME = "name";
   private final String VALUE = "value";

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.EntityProperty#EntityProperty()}.
    */
   @Test
   public void testEntityProperty() {
      EntityProperty ep = new EntityProperty();
      Assert.assertNotNull(ep);
      Assert.assertNull(ep.getId());
      // check that none of the setters fail
      ep.setId(ID);
      ep.setEntityRef(REF);
      ep.setEntityPrefix(PREFIX);
      ep.setPropertyName(NAME);
      ep.setPropertyValue(VALUE);
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.EntityProperty#EntityProperty(java.lang.String, java.lang.String, java.lang.String, java.lang.String[])}.
    */
   @Test
   public void testEntityPropertyStringStringStringStringArray() {
      EntityProperty ep = new EntityProperty(REF, PREFIX, NAME, VALUE);
      Assert.assertNotNull(ep);
      // check that none of the getters fail
      Assert.assertNull(ep.getId());
      Assert.assertEquals(REF, ep.getEntityRef());
      Assert.assertEquals(PREFIX, ep.getEntityPrefix());
      Assert.assertEquals(NAME, ep.getPropertyName());
      Assert.assertEquals(VALUE, ep.getPropertyValue());
   }

}
