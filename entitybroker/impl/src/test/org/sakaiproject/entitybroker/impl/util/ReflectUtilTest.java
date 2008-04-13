/**
 * $Id$
 * $URL$
 * ReflectUtilTest.java - entity-broker - Apr 13, 2008 8:26:39 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.impl.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CRUDable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Createable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Deleteable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Saveable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Updateable;

import junit.framework.TestCase;


/**
 * Testing the utils
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class ReflectUtilTest extends TestCase {

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.util.ReflectUtil#getSuperclasses(java.lang.Class)}.
    */
   public void testGetSuperclasses() {
      class TestNone { }
      class TestProvider implements EntityProvider {
         public String getEntityPrefix() {
            return "provider";
         }
      }
      class TestCrud implements CRUDable {
         public String getEntityPrefix() {
            return "crud";
         }
         public String createEntity(EntityReference ref, Object entity) {
            return "1";
         }
         public Object getSampleEntity() {
            return new String();
         }
         public void updateEntity(EntityReference ref, Object entity) {
         }
         public Object getEntity(EntityReference ref) {
            return "one";
         }
         public void deleteEntity(EntityReference ref) {
         }         
      }
      List<Class<?>> superClasses = null;

      superClasses = ReflectUtil.getSuperclasses(TestNone.class);
      assertNotNull(superClasses);
      assertEquals(1, superClasses.size());
      assertEquals(TestNone.class, superClasses.get(0));

      superClasses = ReflectUtil.getSuperclasses(TestProvider.class);
      assertNotNull(superClasses);
      assertEquals(2, superClasses.size());
      assertEquals(TestProvider.class, superClasses.get(0));
      assertEquals(EntityProvider.class, superClasses.get(1));

      superClasses = ReflectUtil.getSuperclasses(TestCrud.class);
      assertNotNull(superClasses);
      assertTrue(superClasses.size() > 8);
      assertTrue( superClasses.contains(TestCrud.class) );
      assertTrue( superClasses.contains(EntityProvider.class) );
      assertTrue( superClasses.contains(CRUDable.class) );
      assertTrue( superClasses.contains(Createable.class) );
      assertTrue( superClasses.contains(Resolvable.class) );
      assertTrue( superClasses.contains(Updateable.class) );
      assertTrue( superClasses.contains(Deleteable.class) );
      assertTrue( superClasses.contains(Saveable.class) );
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.util.ReflectUtil#getClassFromCollection(java.util.Collection)}.
    */
   @SuppressWarnings("unchecked")
   public void testGetClassFromCollection() {
      Class<?> result = null;

      // null returns object class
      result = ReflectUtil.getClassFromCollection(null);
      assertNotNull(result);
      assertEquals(Object.class, result);

      // empty collection is always object
      result = ReflectUtil.getClassFromCollection( new ArrayList<String>() );
      assertNotNull(result);
      assertEquals(Object.class, result);

      // NOTE: Cannot get real type from empty collections

      // try with collections that have things in them
      List<Object> l = new ArrayList<Object>();
      l.add(new String("testing"));
      result = ReflectUtil.getClassFromCollection(l);
      assertNotNull(result);
      assertEquals(String.class, result);

      HashSet<Object> s = new HashSet<Object>();
      s.add(new Double(22.0));
      result = ReflectUtil.getClassFromCollection(s);
      assertNotNull(result);
      assertEquals(Double.class, result);

      List v = new Vector<Object>();
      v.add( new Integer(30) );
      result = ReflectUtil.getClassFromCollection(v);
      assertNotNull(result);
      assertEquals(Integer.class, result);
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.util.ReflectUtil#contains(T[], java.lang.Object)}.
    */
   public void testContains() {
      assertFalse( ReflectUtil.contains(new String[] {}, "stuff") );
      assertFalse( ReflectUtil.contains(new String[] {"apple"}, "stuff") );
      assertTrue( ReflectUtil.contains(new String[] {"stuff"}, "stuff") );
      assertTrue( ReflectUtil.contains(new String[] {"stuff","other","apple"}, "stuff") );
   }

}
