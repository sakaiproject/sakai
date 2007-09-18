/**
 * EntityReferenceTest.java - 2007 Jul 21, 2007 3:11:31 PM - entity-broker - AZ
 */

package org.sakaiproject.entitybroker.test;

import junit.framework.TestCase;

import org.sakaiproject.entitybroker.EntityReference;

/**
 * Testing the Entity Reference static methods
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class EntityReferenceTest extends TestCase {

   private final String PREFIX1 = "prefix1";
   private final int SEP_POS1 = PREFIX1.length() + 1;
   private final String REF1 = EntityReference.SEPARATOR + PREFIX1 + EntityReference.SEPARATOR
         + "111";

   private final String PREFIX2 = "longprefix2";
   private final int SEP_POS2 = PREFIX2.length() + 1;
   private final String REF2 = EntityReference.SEPARATOR + PREFIX2 + EntityReference.SEPARATOR
         + "222222";

   private final String PREFIX3 = "prefix3";
   private final int SEP_POS3 = -1;
   private final String REF3 = EntityReference.SEPARATOR + PREFIX3;

   private final String INVALID_REF = "invalid_reference-1";

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.EntityReference#getSeparatorPos(java.lang.String)}.
    */
   public void testGetSeparatorPos() {
      int pos = 0;

      // test getting position from 2 part reference
      pos = EntityReference.getSeparatorPos(REF1);
      assertEquals(SEP_POS1, pos);

      pos = EntityReference.getSeparatorPos(REF2);
      assertEquals(SEP_POS2, pos);

      // test getting position from 1 part reference (should be -1)
      pos = EntityReference.getSeparatorPos(REF3);
      assertEquals(SEP_POS3, pos);

      // test invalid reference throws exception
      try {
         pos = EntityReference.getSeparatorPos(INVALID_REF);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.EntityReference#getPrefix(java.lang.String)}.
    */
   public void testGetPrefix() {
      String prefix = null;

      // test 2 part ref
      prefix = EntityReference.getPrefix(REF1);
      assertNotNull(prefix);
      assertEquals(PREFIX1, prefix);

      prefix = EntityReference.getPrefix(REF2);
      assertNotNull(prefix);
      assertEquals(PREFIX2, prefix);

      // test 1 part ref
      prefix = EntityReference.getPrefix(REF3);
      assertNotNull(prefix);
      assertEquals(PREFIX3, prefix);

      // test invalid reference throws exception
      try {
         prefix = EntityReference.getPrefix(INVALID_REF);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityReference#toString()}.
    */
   public void testToString() {
      EntityReference er = null;

      er = new EntityReference(PREFIX3);
      assertEquals(REF3, er.toString());

      // Invalid formed ER will not return a ref string
      er = new EntityReference();
      try {
         er.toString();
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityReference#EntityReference()}.
    */
   public void testEntityReference() {
      EntityReference er = null;

      // make sure this does not die
      er = new EntityReference();
      assertNotNull(er);
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.EntityReference#EntityReference(java.lang.String)}.
    */
   public void testEntityReferenceString() {
      EntityReference er = null;

      er = new EntityReference(PREFIX3);
      assertNotNull(er);
      assertEquals(REF3, er.toString());

      // test invalid prefix throws exception
      try {
         er = new EntityReference(REF1);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      try {
         er = new EntityReference("");
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      try {
         er = new EntityReference(null);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

}
