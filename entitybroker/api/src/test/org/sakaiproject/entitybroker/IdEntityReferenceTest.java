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
 * IdEntityReferenceTest.java - 2007 Jul 21, 2007 3:15:08 PM - entity-broker - AZ
 */

package org.sakaiproject.entitybroker;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.IdEntityReference;

import junit.framework.TestCase;

/**
 * Testing the ID Entity Reference static methods
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
@SuppressWarnings("deprecation")
public class IdEntityReferenceTest extends TestCase {

   private final String PREFIX1 = "prefix1";
   private final String ID1 = "111";
   private final String REF1 = EntityReference.SEPARATOR + PREFIX1 + EntityReference.SEPARATOR + ID1;

   private final String PREFIX2 = "longprefix2";
   private final String ID2 = "22222";
   private final String REF2 = EntityReference.SEPARATOR + PREFIX2 + EntityReference.SEPARATOR + ID2;

   private final String INVALID_REF = "invalid_reference-1";

   /**
    * Test method for {@link org.sakaiproject.entitybroker.IdEntityReference#toString()}.
    */
   public void testToString() {
      IdEntityReference ider = null;

      ider = new IdEntityReference(REF1);
      assertEquals(REF1, ider.toString());

      ider = new IdEntityReference(PREFIX2, ID2);
      assertEquals(REF2, ider.toString());
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.IdEntityReference#getID(java.lang.String)}.
    */
   public void testGetID() {
      String id = null;

      id = IdEntityReference.getID(REF1);
      assertNotNull(id);
      assertEquals(ID1, id);

      id = IdEntityReference.getID(REF2);
      assertNotNull(id);
      assertEquals(ID2, id);

      // test invalid reference throws exception
      try {
         id = IdEntityReference.getID(INVALID_REF);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.IdEntityReference#IdEntityReference(java.lang.String)}.
    */
   public void testIdEntityReferenceString() {
      IdEntityReference ider = null;

      ider = new IdEntityReference(REF1);
      assertNotNull(ider);

      ider = new IdEntityReference(REF2);
      assertNotNull(ider);

      // test invalid reference throws exception
      try {
         ider = new IdEntityReference(INVALID_REF);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      try {
         ider = new IdEntityReference("");
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      try {
         ider = new IdEntityReference(null);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.IdEntityReference#IdEntityReference(java.lang.String, java.lang.String)}.
    */
   public void testIdEntityReferenceStringString() {
      IdEntityReference ider = null;

      ider = new IdEntityReference(PREFIX1, ID1);
      assertNotNull(ider);
      assertEquals(REF1, ider.toString());

      ider = new IdEntityReference(PREFIX2, ID2);
      assertNotNull(ider);
      assertEquals(REF2, ider.toString());

      // test passing in prefix or id with / causes failure
      try {
         ider = new IdEntityReference(REF1, ID2);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      try {
         ider = new IdEntityReference(PREFIX1, REF2);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      // test invalid argument throws exception
      try {
         ider = new IdEntityReference(null, ID1);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      try {
         ider = new IdEntityReference(PREFIX1, null);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      try {
         ider = new IdEntityReference("", "");
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      try {
         ider = new IdEntityReference(null, null);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

}
