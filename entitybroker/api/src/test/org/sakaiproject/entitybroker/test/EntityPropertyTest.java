/******************************************************************************
 * EntityPropertyTest.java - created by aaronz on Jul 24, 2007
 * 
 * Copyright (c) 2007 Centre for Academic Research in Educational Technologies
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 *****************************************************************************/

package org.sakaiproject.entitybroker.test;

import org.sakaiproject.entitybroker.dao.model.EntityProperty;

import junit.framework.TestCase;

/**
 * Note really sure this test is needed
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EntityPropertyTest extends TestCase {

   private final Long ID = Long.valueOf(1);
   private final String REF = "ref";
   private final String PREFIX = "prefix";
   private final String NAME = "name";
   private final String VALUE = "value";

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.dao.model.EntityProperty#EntityProperty()}.
    */
   public void testEntityProperty() {
      EntityProperty ep = new EntityProperty();
      assertNotNull(ep);
      assertNull(ep.getId());
      // check that none of the setters fail
      ep.setId(ID);
      ep.setEntityRef(REF);
      ep.setEntityPrefix(PREFIX);
      ep.setPropertyName(NAME);
      ep.setPropertyValue(VALUE);
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.dao.model.EntityProperty#EntityProperty(java.lang.String, java.lang.String, java.lang.String, java.lang.String[])}.
    */
   public void testEntityPropertyStringStringStringStringArray() {
      EntityProperty ep = new EntityProperty(REF, PREFIX, NAME, VALUE);
      assertNotNull(ep);
      // check that none of the getters fail
      assertNull(ep.getId());
      assertEquals(REF, ep.getEntityRef());
      assertEquals(PREFIX, ep.getEntityPrefix());
      assertEquals(NAME, ep.getPropertyName());
      assertEquals(VALUE, ep.getPropertyValue());
   }

}
