/**
 * EntityReferenceTest.java - 2007 Jul 21, 2007 3:11:31 PM - entity-broker - AZ
 */

package org.sakaiproject.entitybroker;

import junit.framework.TestCase;

import org.sakaiproject.entitybroker.EntityReference;

/**
 * Testing the Entity Reference static methods
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class EntityReferenceTest extends TestCase {

   private final String PREFIX1 = "prefix1";
   private final String REF1 = EntityReference.SEPARATOR + PREFIX1 + EntityReference.SEPARATOR + "111";

   private final String PREFIX2 = "longprefix2";
   private final String REF2 = EntityReference.SEPARATOR + PREFIX2 + EntityReference.SEPARATOR + "222222";

   private final String PREFIX3 = "prefix3";
   private final String REF3 = EntityReference.SEPARATOR + PREFIX3;

   private final String INVALID_REF = "invalid_reference-1";


   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityReference#getOriginalReference()}.
    */
   public void testGetOriginalReference() {
      EntityReference er = null;

      er = new EntityReference("/myprefix/myid/extra");
      assertEquals("myprefix", er.prefix);
      assertEquals("myid", er.id);
      assertEquals("/myprefix/myid", er.toString());
      assertEquals("/myprefix/myid/extra", er.getOriginalReference());
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityReference#setOriginalReference(java.lang.String)}.
    */
   public void testSetOriginalReference() {
      EntityReference er = null;

      er = new EntityReference("/myprefix/myid/extra");
      assertEquals("/myprefix/myid/extra", er.getOriginalReference());
      
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityReference#getReference()}.
    */
   public void testGetReference() {
      //fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityReference#getSpaceReference()}.
    */
   public void testGetSpaceReference() {
      //fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityReference#makeReference(boolean)}.
    */
   public void testMakeReference() {
      //fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityReference#findPrefix(java.lang.String)}.
    */
   public void testFindPrefix() {
      String prefix = null;

      // test 2 part ref
      prefix = EntityReference.findPrefix(REF1);
      assertNotNull(prefix);
      assertEquals(PREFIX1, prefix);

      prefix = EntityReference.findPrefix(REF2);
      assertNotNull(prefix);
      assertEquals(PREFIX2, prefix);

      // test 1 part ref
      prefix = EntityReference.findPrefix(REF3);
      assertNotNull(prefix);
      assertEquals(PREFIX3, prefix);

      // test invalid reference throws exception
      try {
         prefix = EntityReference.findPrefix(INVALID_REF);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityReference#findId(java.lang.String)}.
    */
   public void testFindId() {
      //fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityReference#getSeparatorPos(java.lang.String)}.
    */
   public void testGetSeparatorPos() {
      //fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityReference#checkReference(java.lang.String)}.
    */
   public void testCheckReference() {
      //fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityReference#checkPrefixId(java.lang.String, java.lang.String)}.
    */
   public void testCheckPrefixId() {
      //fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityReference#toString()}.
    */
   public void testToString() {
      EntityReference er = null;

      er = new EntityReference(REF3);
      assertEquals(REF3, er.toString());

      er = new EntityReference(REF1);
      assertEquals(REF1, er.toString());

      // Invalid formed ER will not return a ref string
      er = new EntityReference();
      try {
         er.toString();
         fail("Should have thrown exception");
      } catch (IllegalStateException e) {
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

      er = new EntityReference(REF1);
      assertNotNull(er);
      assertEquals(PREFIX1, er.prefix);
      assertEquals("111", er.id);
      assertEquals(REF1, er.toString());

      er = new EntityReference(REF2);
      assertNotNull(er);
      assertEquals(PREFIX2, er.prefix);
      assertEquals("222222", er.id);
      assertEquals(REF2, er.toString());

      er = new EntityReference(REF3);
      assertNotNull(er);
      assertEquals(PREFIX3, er.prefix);
      assertEquals(null, er.id);
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

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityReference#EntityReference(java.lang.String, java.lang.String)}.
    */
   public void testEntityReferenceStringString() {
      EntityReference er = null;

      er = new EntityReference(PREFIX1, "111");
      assertNotNull(er);
      assertEquals(PREFIX1, er.prefix);
      assertEquals("111", er.id);
      assertEquals(REF1, er.toString());

      er = new EntityReference(PREFIX3, "");
      assertNotNull(er);
      assertEquals(PREFIX3, er.prefix);
      assertEquals(null, er.id);
      assertEquals(REF3, er.toString());

      // test invalid prefix throws exception
      try {
         er = new EntityReference(PREFIX1);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

}
