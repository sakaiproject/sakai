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
   private final String REF1 = EntityReference.SEPARATOR + PREFIX1 + EntityReference.SEPARATOR
         + "111";

   private final String PREFIX2 = "longprefix2";
   private final String REF2 = EntityReference.SEPARATOR + PREFIX2 + EntityReference.SEPARATOR
         + "222222";

   private final String PREFIX3 = "prefix3";
   private final String REF3 = EntityReference.SEPARATOR + PREFIX3;

   private final String INVALID_REF = "invalid_reference-1";


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

      er = new EntityReference(REF3);
      assertEquals(REF3, er.toString());

      // Invalid formed ER will not return a ref string
      er = new EntityReference();
      try {
         er.toString();
         fail("Should have thrown exception");
      } catch (NullPointerException e) {
         assertNotNull(e);
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

      er = new EntityReference(REF3);
      assertNotNull(er);
      assertEquals(REF3, er.toString());

      // test invalid prefix throws exception
      try {
         er = new EntityReference(PREFIX1);
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
