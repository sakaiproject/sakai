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
import java.util.Map;
import java.util.Vector;

import junit.framework.TestCase;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityId;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CRUDable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Createable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Deleteable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Saveable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Updateable;

/**
 * Testing the reflection utils
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class ReflectUtilTest extends TestCase {

   class TestNone { }
   class TestProvider implements EntityProvider {
      public String id = "identity";
      public Long nullVal = null;
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

   /**
    * String id = "id"
    * String entityId = "EID"; @EntityId
    */
   public class TestPea {
      public String id = "id";
      @EntityId
      public String entityId = "EID";
      protected String prot = "prot";
      @SuppressWarnings("unused")
      private String priv = "priv";
   }
   /**
    * (m) int myInt = 0
    * (m) String myString = "woot"
    */
   public class TestBean {
      private int myInt = 0;
      private String myString = "woot";
      @EntityId
      public int getMyInt() {
         return myInt;
      }
      public void setMyInt(int myInt) {
         this.myInt = myInt;
      }
      public String getMyString() {
         return myString;
      }
      public void setMyString(String myString) {
         this.myString = myString;
      }
   }
   /**
    * (m) Long id = 3
    * (m) String entityId = "33" @EntityId
    * (m) String extra = null
    * (m) String[] sArray = {"1","2"}
    */
   public class TestEntity {
      private Long id = new Long(3);
      private String entityId = "33";
      private String extra = null;
      private String[] sArray = {"1","2"};
      public String getPrefix() {
         return "crud";
      }
      public String createEntity(Object entity) {
         return "1";
      }
      @EntityId
      public String getEntityId() {
         return entityId;
      }
      public void setEntityId(String entityId) {
         this.entityId = entityId;
      }
      public Long getId() {
         return id;
      }
      public void setId(Long id) {
         this.id = id;
      }
      public String getExtra() {
         return extra;
      }
      public void setExtra(String extra) {
         this.extra = extra;
      }
      public String[] getSArray() {
         return sArray;
      }
      public void setSArray(String[] array) {
         sArray = array;
      }
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.util.ReflectUtil#getFieldValue(java.lang.Object, java.lang.String)}.
    */
   public void testGetFieldValueObjectString() {
      ReflectUtil reflectUtil = new ReflectUtil();
      Object value = null;
      Object thing = null;

      thing = new TestEntity();
      value = reflectUtil.getFieldValue( thing, "entityId");
      assertNotNull(value);
      assertEquals("33", value);

      value = reflectUtil.getFieldValue( thing, "id");
      assertNotNull(value);
      assertEquals(new Long(3), value);

      value = reflectUtil.getFieldValue( thing, "extra");
      assertNull(value);

      value = reflectUtil.getFieldValue( thing, "SArray");
      assertNotNull(value);
      assertTrue(value.getClass().isArray());
      assertEquals("1", ((String[])value)[0]);
      assertEquals("2", ((String[])value)[1]);

      // TODO pea support?
//    thing = new TestPea();
//    value = reflectUtil.getFieldValue( thing, "id");
//    assertNotNull(value);
//    assertEquals("id", value);

      thing = new TestBean();
      try {
         value = reflectUtil.getFieldValue(thing, "id");
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.util.ReflectUtil#setFieldValue(java.lang.Object, java.lang.String, java.lang.Object)}.
    */
   public void testSetFieldValue() {
      ReflectUtil reflectUtil = new ReflectUtil();
      Object thing = null;

      thing = new TestBean();
      reflectUtil.setFieldValue(thing, "myString", "TEST");
      assertEquals("TEST", ((TestBean)thing).getMyString());

      thing = new TestBean();
      reflectUtil.setFieldValue(thing, "myInt", 5);
      assertEquals(5, ((TestBean)thing).getMyInt());

      thing = new TestEntity();
      reflectUtil.setFieldValue(thing, "SArray", new String[] {"A", "B", "C"});
      assertEquals(3, ((TestEntity)thing).getSArray().length);
      assertEquals("A", ((TestEntity)thing).getSArray()[0]);
      assertEquals("B", ((TestEntity)thing).getSArray()[1]);
      assertEquals("C", ((TestEntity)thing).getSArray()[2]);

      thing = new TestBean();
      try {
         reflectUtil.setFieldValue(thing, "id", "uhohes");
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.util.ReflectUtil#setFieldStringValue(java.lang.Object, java.lang.String, java.lang.String)}.
    */
   public void testSetFieldStringValue() {
      ReflectUtil reflectUtil = new ReflectUtil();
      Object thing = null;

      thing = new TestBean();
      reflectUtil.setFieldStringValue(thing, "myString", "TEST");
      assertEquals("TEST", ((TestBean)thing).getMyString());

      thing = new TestBean();
      reflectUtil.setFieldStringValue(thing, "myInt", "10");
      assertEquals(10, ((TestBean)thing).getMyInt());

      thing = new TestEntity();
      reflectUtil.setFieldStringValue(thing, "id", "6");
      assertEquals(new Long(6), ((TestEntity)thing).getId());

      thing = new TestEntity();
      reflectUtil.setFieldStringValue(thing, "SArray", "A, B, C");
      assertEquals(3, ((TestEntity)thing).getSArray().length);
      assertEquals("A", ((TestEntity)thing).getSArray()[0]);
      assertEquals("B", ((TestEntity)thing).getSArray()[1]);
      assertEquals("C", ((TestEntity)thing).getSArray()[2]);
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.util.ReflectUtil#getFieldTypes(java.lang.Class)}.
    */
   public void testGetFieldTypes() {
      ReflectUtil reflectUtil = new ReflectUtil();
      Map<String, Class<?>> types;

      types = reflectUtil.getFieldTypes(TestBean.class);
      assertNotNull(types);
      assertEquals(2, types.size());
      assertEquals(String.class, types.get("myString"));
      assertEquals(int.class, types.get("myInt"));
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.util.ReflectUtil#getFieldType(java.lang.Class, java.lang.String)}.
    */
   public void testGetFieldType() {
      ReflectUtil reflectUtil = new ReflectUtil();
      Class<?> type;

      type = reflectUtil.getFieldType(TestBean.class, "myString");
      assertNotNull(type);
      assertEquals(String.class, type);

      type = reflectUtil.getFieldType(TestBean.class, "myInt");
      assertNotNull(type);
      assertEquals(int.class, type);
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.util.ReflectUtil#getObjectValues(java.lang.Object)}.
    */
   public void testGetObjectValues() {
      Map<String, Object> m = null;
      ReflectUtil reflectUtil = new ReflectUtil();

      m = reflectUtil.getObjectValues( new TestNone() );
      assertNotNull(m);
      assertEquals(0, m.size());

//      m = reflectUtil.getObjectValues( new TestPea() );
//      assertNotNull(m);
//      assertEquals(2, m.size());
//      assertTrue(m.containsKey("id"));
//      assertEquals("id", m.get("id"));
//      assertTrue(m.containsKey("entityId"));
//      assertEquals("EID", m.get("entityId"));

      m = reflectUtil.getObjectValues( new TestBean() );
      assertNotNull(m);
      assertEquals(2, m.size());
      assertTrue(m.containsKey("myInt"));
      assertTrue(m.containsKey("myString"));
      assertEquals(0, m.get("myInt"));
      assertEquals("woot", m.get("myString"));

      m = reflectUtil.getObjectValues( new TestEntity() );
      assertNotNull(m);
      assertEquals(5, m.size());
      assertTrue(m.containsKey("id"));
      assertTrue(m.containsKey("entityId"));
      assertTrue(m.containsKey("extra"));
      assertTrue(m.containsKey("SArray"));
      assertTrue(m.containsKey("prefix"));
      assertEquals(new Long(3), m.get("id"));
      assertEquals("33", m.get("entityId"));
      assertEquals(null, m.get("extra"));
      assertEquals("crud", m.get("prefix"));
      assertTrue(m.get("SArray").getClass().isArray());
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.util.ReflectUtil#getFieldNameWithAnnotation(java.lang.Class, java.lang.Class)}.
    */
   public void testGetFieldNameWithAnnotation() {
      ReflectUtil reflectUtil = new ReflectUtil();
      String fieldName = null;

      fieldName = reflectUtil.getFieldNameWithAnnotation(TestBean.class, EntityId.class);
      assertEquals("myInt", fieldName);

      fieldName = reflectUtil.getFieldNameWithAnnotation(TestEntity.class, EntityId.class);
      assertEquals("entityId", fieldName);
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.util.ReflectUtil#getFieldValueAsString(java.lang.Object, java.lang.String, java.lang.Class)}.
    */
   public void testGetFieldValueObjectStringClassOfQextendsAnnotation() {
      String value = null;
      ReflectUtil reflectUtil = new ReflectUtil();

      try {
         value = reflectUtil.getFieldValueAsString( new TestBean(), "id", null);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      value = reflectUtil.getFieldValueAsString( new TestEntity(), "extra", null);
      assertNull(value);

//    value = reflectUtil.getFieldValueAsString( new TestPea(), "id", null);
//    assertNotNull(value);
//    assertEquals("id", value);

      value = reflectUtil.getFieldValueAsString( new TestEntity(), "id", null);
      assertNotNull(value);
      assertEquals("3", value);

//    value = reflectUtil.getFieldValueAsString( new TestPea(), "id", EntityId.class);
//    assertNotNull(value);
//    assertEquals("EID", value);

      value = reflectUtil.getFieldValueAsString( new TestEntity(), "id", EntityId.class);
      assertNotNull(value);
      assertEquals("33", value);

      try {
         value = reflectUtil.getFieldValueAsString( new TestNone(), "id", EntityId.class);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.util.ReflectUtil#clone(java.lang.Object, int, java.lang.String[])}.
    */
   public void testClone() {
      ReflectUtil reflectUtil = new ReflectUtil();

      TestBean tb = new TestBean();
      tb.setMyInt(100);
      tb.setMyString("1000");
      TestBean tbClone = reflectUtil.clone(tb, 0, null);
      assertNotNull(tbClone);
      assertEquals(tb.getMyInt(), tbClone.getMyInt());
      assertEquals(tb.getMyString(), tbClone.getMyString());

      // test skipping values
      tbClone = reflectUtil.clone(tb, 0, new String[] {"myInt"});
      assertNotNull(tbClone);
      assertTrue(tb.getMyInt() != tbClone.getMyInt());
      assertEquals(tb.getMyString(), tbClone.getMyString());

      tbClone = reflectUtil.clone(tb, 5, null);
      assertNotNull(tbClone);
      assertEquals(tb.getMyInt(), tbClone.getMyInt());
      assertEquals(tb.getMyString(), tbClone.getMyString());

      // TODO test cloning maps

      // TODO test cloning nested objects

      // TODO test cloning collections

   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.util.ReflectUtil#copy(java.lang.Object, java.lang.Object, int, java.lang.String[], boolean)}.
    */
   public void testCopy() {
      ReflectUtil reflectUtil = new ReflectUtil();

      TestBean orig = new TestBean();
      orig.setMyInt(100);
      orig.setMyString("1000");
      TestBean dest = new TestBean();
      assertNotSame(orig.getMyInt(), dest.getMyInt());
      assertNotSame(orig.getMyString(), dest.getMyString());
      reflectUtil.copy(orig, dest, 0, null, false);
      assertNotNull(dest);
      assertEquals(orig.getMyInt(), dest.getMyInt());
      assertEquals(orig.getMyString(), dest.getMyString());

      dest = new TestBean();
      reflectUtil.copy(orig, dest, 0, new String[] {"myInt"}, false);
      assertNotNull(dest);
      assertNotSame(orig.getMyInt(), dest.getMyInt());
      assertEquals(orig.getMyString(), dest.getMyString());

      dest = new TestBean();
      reflectUtil.copy(orig, dest, 5, null, true);
      assertNotNull(dest);
      assertEquals(orig.getMyInt(), dest.getMyInt());
      assertEquals(orig.getMyString(), dest.getMyString());

      //FIXME TODO fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.util.ReflectUtil#makeFieldNameFromMethod(java.lang.String)}.
    */
   public void testMakeFieldNameFromMethod() {
      ReflectUtil reflectUtil = new ReflectUtil();
      String name = null;
      
      name = reflectUtil.makeFieldNameFromMethod("getStuff");
      assertEquals("stuff", name);

      name = reflectUtil.makeFieldNameFromMethod("getSomeStuff");
      assertEquals("someStuff", name);

      name = reflectUtil.makeFieldNameFromMethod("setStuff");
      assertEquals("stuff", name);

      name = reflectUtil.makeFieldNameFromMethod("isStuff");
      assertEquals("stuff", name);

      name = reflectUtil.makeFieldNameFromMethod("stuff");
      assertEquals("stuff", name);
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.util.ReflectUtil#getSuperclasses(java.lang.Class)}.
    */
   public void testGetSuperclasses() {
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

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.util.ReflectUtil#capitalize(java.lang.String)}.
    */
   public void testCapitalize() {
      assertTrue( ReflectUtil.capitalize("lower").equals("Lower") );
      assertTrue( ReflectUtil.capitalize("UPPER").equals("UPPER") );
      assertTrue( ReflectUtil.capitalize("myStuff").equals("MyStuff") );
      assertTrue( ReflectUtil.capitalize("MyStuff").equals("MyStuff") );
      assertTrue( ReflectUtil.capitalize("").equals("") );
      assertTrue( ReflectUtil.capitalize("m").equals("M") );
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.util.ReflectUtil#unCapitalize(java.lang.String)}.
    */
   public void testUnCapitalize() {
      assertTrue( ReflectUtil.unCapitalize("lower").equals("lower") );
      assertTrue( ReflectUtil.unCapitalize("UPPER").equals("uPPER") );
      assertTrue( ReflectUtil.unCapitalize("MyStuff").equals("myStuff") );
      assertTrue( ReflectUtil.unCapitalize("myStuff").equals("myStuff") );
      assertTrue( ReflectUtil.unCapitalize("").equals("") );
      assertTrue( ReflectUtil.unCapitalize("M").equals("m") );
   }

}
