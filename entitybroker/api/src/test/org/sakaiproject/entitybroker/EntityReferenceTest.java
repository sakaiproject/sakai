/**
 * Copyright (c) 2007-2008 The Apereo Foundation
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
   private static final String ID1 = "111";
   private final String REF1 = EntityReference.SEPARATOR + PREFIX1 + EntityReference.SEPARATOR + ID1;
   private final String INPUT_REF1 = REF1;

   private final String PREFIX2 = "longprefix2";
   private static final String ID2 = "222222";
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

   public void testCheckReference() {
      EntityReference.checkReference("/testing");
      EntityReference.checkReference("/testing/something");
      EntityReference.checkReference("/test/a/long/thing");
      EntityReference.checkReference("/TETETE/SDASDASD/FFFFF/XXXXXXXXXXXXXXXXXXXXX");

      try {
         EntityReference.checkReference(null);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
      try {
         EntityReference.checkReference("");
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
      try {
         EntityReference.checkReference("xxxxxxxxxxxxxxxxxxxx");
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

   public void testCheckPrefixId() {
      EntityReference.checkPrefixId("aaronz", "1111");
      EntityReference.checkPrefixId("AARONZ", "");
      EntityReference.checkPrefixId("ABCdef123-AZ_AZ:AZ;AZ.AZ", "anID");
      EntityReference.checkPrefixId("azeckoski", "ABCdef123-AZ_AZ:AZ;AZ.AZ");

      EntityReference.checkPrefixId("my_crazy-PREFIX", "This.is,a-really_insane:id(very)");

      EntityReference.checkPrefixId("eval-config", "ENABLE_XLS_REPORT_EXPORT:java.lang.Boolean");

      try {
         EntityReference.checkPrefixId(null, null);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
      try {
         EntityReference.checkPrefixId(null, "");
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
      try {
         EntityReference.checkPrefixId("", null);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
      try {
         EntityReference.checkPrefixId("aaronz", null);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
      try {
         EntityReference.checkPrefixId("aaronz", "asd/def");
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

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
      assertEquals("myprefix", er.getPrefix());
      assertEquals("myid", er.getId());
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
    * Test method for {@link org.sakaiproject.entitybroker.EntityReference#makeEntityReference(boolean)}.
    */
   public void testMakeReference() {
      EntityReference er = null;

      er = new EntityReference(INPUT_REF1);
      assertEquals(REF1, er.makeEntityReference(false));

      er = new EntityReference(INPUT_REF2);
      assertEquals(REF2, er.makeEntityReference(false));

      er = new EntityReference(INPUT_REF3);
      assertEquals(REF3, er.makeEntityReference(false));

      er = new EntityReference(INPUT_REF2);
      assertEquals(EntityReference.SEPARATOR + PREFIX2, er.makeEntityReference(true));

      er = new EntityReference(INPUT_REF3);
      assertEquals(REF3, er.makeEntityReference(true));

      
      // Invalid formed ER will not return a ref string
      er = new EntityReference();
      try {
         er.makeEntityReference(false);
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
      assertEquals(PREFIX1, er.getPrefix());
      assertEquals(ID1, er.getId());
      assertEquals(REF1, er.toString());

      er = new EntityReference(INPUT_REF2);
      assertNotNull(er);
      assertEquals(PREFIX2, er.getPrefix());
      assertEquals(ID2, er.getId());
      assertEquals(REF2, er.toString());

      er = new EntityReference(INPUT_REF3);
      assertNotNull(er);
      assertEquals(PREFIX3, er.getPrefix());
      assertEquals(null, er.getId());
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
      assertEquals(PREFIX1, er.getPrefix());
      assertEquals(ID1, er.getId());
      assertEquals(REF1, er.toString());

      er = new EntityReference(PREFIX3, "");
      assertNotNull(er);
      assertEquals(PREFIX3, er.getPrefix());
      assertEquals(null, er.getId());
      assertEquals(REF3, er.toString());

      // test invalid prefix throws exception
      try {
         er = new EntityReference(null, "");
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

   public void testGetIdFromRef() {
       String id = EntityReference.getIdFromRef("/prefix/1234");
       assertNotNull(id);
       assertEquals("1234", id);

       id = EntityReference.getIdFromRef("/prefix/1234/asdf");
       assertNotNull(id);
       assertEquals("1234", id);

       id = EntityReference.getIdFromRef("/prefix");
       assertNull(id);
   }

   public void testGetIdFromRefByKey() {
       String id = EntityReference.getIdFromRefByKey("/prefix/1234", "AAA");
       assertNull(id);

       id = EntityReference.getIdFromRefByKey("/prefix/1234", "prefix");
       assertNotNull(id);
       assertEquals("1234", id);

       id = EntityReference.getIdFromRefByKey("/prefix/1234/site/mysite/group/mygroup", "prefix");
       assertNotNull(id);
       assertEquals("1234", id);

       id = EntityReference.getIdFromRefByKey("/prefix/1234/site/mysite/group/mygroup", "site");
       assertNotNull(id);
       assertEquals("mysite", id);

       id = EntityReference.getIdFromRefByKey("/prefix/1234/site/mysite/group/mygroup", "group");
       assertNotNull(id);
       assertEquals("mygroup", id);

       id = EntityReference.getIdFromRefByKey("/prefix/1234/site/mysite/group/mygroup", "mygroup");
       assertNull(id);
   }

}
