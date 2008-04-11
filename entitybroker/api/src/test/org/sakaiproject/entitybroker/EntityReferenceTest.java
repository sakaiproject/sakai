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

   private static final String ID1 = "111";
   private static final String ID2 = "222222";
   private final String PREFIX1 = "prefix1";
   private final String REF1 = EntityReference.SEPARATOR + PREFIX1 + EntityReference.SEPARATOR + ID1;
   private final String INPUT_REF1 = REF1;

   private final String PREFIX2 = "longprefix2";
   private final String REF2 = EntityReference.SEPARATOR + PREFIX2 + EntityReference.SEPARATOR + ID2;
   private final String INPUT_REF2 = EntityReference.SEPARATOR + PREFIX2 + EntityReference.SEPARATOR + ID2 + EntityReference.SEPARATOR + "extrajunk";

   private final String PREFIX3 = "prefix3";
   private final String REF3 = EntityReference.SEPARATOR + PREFIX3;
   private final String INPUT_REF3 = REF3;

   private final String INVALID_REF = "invalid_reference-1";



   /**
    * Test method for get separator position
    */
   public void testGetSeparatorPos() {
      String reference = "/1234/678/01234/6789/12345";
      assertEquals(0, EntityReference.getSeparatorPos(reference, 0));
      assertEquals(5, EntityReference.getSeparatorPos(reference, 1));
      assertEquals(9, EntityReference.getSeparatorPos(reference, 2));
      assertEquals(15, EntityReference.getSeparatorPos(reference, 3));
      assertEquals(20, EntityReference.getSeparatorPos(reference, 4));
      assertEquals(-1, EntityReference.getSeparatorPos(reference, 5));
      assertEquals(-1, EntityReference.getSeparatorPos(reference, 6));
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityReference#getOriginalReference()}.
    */
   public void testGetOriginalReference() {
      EntityReference er = null;

      er = new EntityReference(INPUT_REF2);
      assertEquals(REF2, er.toString());
      assertEquals(INPUT_REF2, er.getOriginalReference());

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
      er.setOriginalReference("/reset");
      assertEquals("/reset", er.getOriginalReference());

      // test invalid reference throws exception
      try {
         er.setOriginalReference(INVALID_REF);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
      
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityReference#getReference()}.
    */
   public void testGetReference() {
      EntityReference er = null;

      er = new EntityReference(INPUT_REF1);
      assertEquals(REF1, er.getReference());
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityReference#getSpaceReference()}.
    */
   public void testGetSpaceReference() {
      EntityReference er = null;

      er = new EntityReference(INPUT_REF3);
      assertEquals(REF3, er.getSpaceReference());

      er = new EntityReference(INPUT_REF1);
      assertEquals(EntityReference.SEPARATOR + PREFIX1, er.getSpaceReference());
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityReference#makeReference(boolean)}.
    */
   public void testMakeReference() {
      EntityReference er = null;

      er = new EntityReference(INPUT_REF1);
      assertEquals(REF1, er.makeReference(false));

      er = new EntityReference(INPUT_REF2);
      assertEquals(REF2, er.makeReference(false));

      er = new EntityReference(INPUT_REF3);
      assertEquals(REF3, er.makeReference(false));

      er = new EntityReference(INPUT_REF2);
      assertEquals(EntityReference.SEPARATOR + PREFIX2, er.makeReference(true));

      er = new EntityReference(INPUT_REF3);
      assertEquals(REF3, er.makeReference(true));

      
      // Invalid formed ER will not return a ref string
      er = new EntityReference();
      try {
         er.makeReference(false);
         fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         assertNotNull(e);
      }
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityReference#findPrefix(java.lang.String)}.
    */
   public void testFindPrefix() {
      String prefix = null;

      // test 2 part ref
      prefix = EntityReference.findPrefix(INPUT_REF1);
      assertNotNull(prefix);
      assertEquals(PREFIX1, prefix);

      prefix = EntityReference.findPrefix(INPUT_REF2);
      assertNotNull(prefix);
      assertEquals(PREFIX2, prefix);

      // test 1 part ref
      prefix = EntityReference.findPrefix(INPUT_REF3);
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
      String id = null;

      // test 2 part ref
      id = EntityReference.findId(INPUT_REF1);
      assertNotNull(id);
      assertEquals(ID1, id);

      id = EntityReference.findId(INPUT_REF2);
      assertNotNull(id);
      assertEquals(ID2, id);

      id = EntityReference.findId(INPUT_REF3);
      assertNull(id);

      // test invalid reference throws exception
      try {
         id = EntityReference.findId(INVALID_REF);
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

      er = new EntityReference(INPUT_REF1);
      assertEquals(REF1, er.toString());
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

      er = new EntityReference(INPUT_REF1);
      assertNotNull(er);
      assertEquals(PREFIX1, er.prefix);
      assertEquals(ID1, er.id);
      assertEquals(REF1, er.toString());

      er = new EntityReference(INPUT_REF2);
      assertNotNull(er);
      assertEquals(PREFIX2, er.prefix);
      assertEquals(ID2, er.id);
      assertEquals(REF2, er.toString());

      er = new EntityReference(INPUT_REF3);
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

      er = new EntityReference(PREFIX1, ID1);
      assertNotNull(er);
      assertEquals(PREFIX1, er.prefix);
      assertEquals(ID1, er.id);
      assertEquals(REF1, er.toString());

      er = new EntityReference(PREFIX3, "");
      assertNotNull(er);
      assertEquals(PREFIX3, er.prefix);
      assertEquals(null, er.id);
      assertEquals(REF3, er.toString());

      // test invalid prefix throws exception
      try {
         er = new EntityReference(null, "");
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

}
