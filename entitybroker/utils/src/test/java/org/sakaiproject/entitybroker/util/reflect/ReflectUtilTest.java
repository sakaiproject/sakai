/**
 * $Id$
 * $URL$
 * ReflectUtilTest.java - entity-broker - Apr 13, 2008 8:26:39 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 The Sakai Foundation
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
 */

package org.sakaiproject.entitybroker.util.reflect;

import java.io.IOException;
import java.io.Serializable;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.azeckoski.reflectutils.ReflectUtils;
import org.azeckoski.reflectutils.exceptions.FieldnameNotFoundException;

import junit.framework.TestCase;

/**
 * Testing the reflection utils
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class ReflectUtilTest extends TestCase {

    class TestNone { }
    class TestImplOne implements TestInterfaceOne {
        public String id = "identity";
        public Long nullVal = null;
    }
    class TestImplFour implements TestInterfaceFour {
        private String thing;
        public void setThing(String thing) {
            this.thing = thing;
        }
        public String getThing() {
            return thing;
        }
        public void run() {
            // nothing
        }
        public int read(CharBuffer cb) throws IOException {
            return 0;
        }
    }

    /**
     * (f) String id = "id" <br/>
     * (f) String entityId = "EID" (EntityId) <br/>
     */
    public class TestPea {
        public String id = "id";
        @TestAnnote
        public String entityId = "EID";
        protected String prot = "prot";
        @SuppressWarnings("unused")
        private String priv = "priv";
    }
    /**
     * int myInt = 0 (EntityId) <br/>
     * String myString = "woot" <br/>
     */
    public class TestBean {
        private int myInt = 0;
        private String myString = "woot";
        @TestAnnote
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
     * Long id = 3 <br/>
     * String entityId = "33" (EntityId) <br/>
     * String extra = null <br/>
     * String[] sArray = {"1","2"} <br/>
     */
    public class TestEntity {
        private Long id = new Long(3);
        private String entityId = "33";
        private String extra = null;
        private Boolean bool = null;
        private String[] sArray = {"1","2"};
        public String getPrefix() {
            return "crud";
        }
        public String createEntity(Object entity) {
            return "1";
        }
        @TestAnnote
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
        public Boolean getBool() {
            return bool;
        }
        public void setBool(Boolean bool) {
            this.bool = bool;
        }
    }
    /**
     * int id = 5 <br/>
     * String title = "55" <br/>
     * String extra = null <br/>
     * List<String> sList = [A,B] <br/>
     * Map<String, String> sMap = [A1=ONE, B2=TWO] <br/>
     * TestBean testBean = null <br/>
     * - int myInt = 0 <br/>
     * - String myString = "woot" <br/>
     * TestEntity testEntity <br/>
     * - Long id = 3 <br/>
     * - String entityId = "33" (EntityId) <br/>
     * - String extra = null <br/>
     * - String[] sArray = {"1","2"} <br/>
     */
    public class TestNesting {
        private int id = 5;
        private String title = "55";
        private String extra = null;
        private List<String> sList = new ArrayList<String>();
        private Map<String, String> sMap = new HashMap<String, String>();
        private TestBean testBean = null;
        private TestEntity testEntity;
        public TestNesting() {
            testEntity = new TestEntity();
            sMap.put("A1", "ONE");
            sMap.put("B2", "TWO");
            sList.add("A");
            sList.add("B");
        }
        public TestNesting(int id, String title, String[] list) {
            this();
            this.id = id;
            this.title = title;
            sList = Arrays.asList(list);
        }
        public int getId() {
            return id;
        }
        public void setId(int id) {
            this.id = id;
        }
        public String getTitle() {
            return title;
        }
        public void setTitle(String title) {
            this.title = title;
        }
        public String getExtra() {
            return extra;
        }
        public void setExtra(String extra) {
            this.extra = extra;
        }
        public List<String> getSList() {
            return sList;
        }
        public void setSList(List<String> list) {
            sList = list;
        }
        public TestBean getTestBean() {
            return testBean;
        }
        public void setTestBean(TestBean testBean) {
            this.testBean = testBean;
        }
        public TestEntity getTestEntity() {
            return testEntity;
        }
        public void setTestEntity(TestEntity testEntity) {
            this.testEntity = testEntity;
        }
        public Map<String, String> getSMap() {
            return sMap;
        }
        public void setSMap(Map<String, String> map) {
            sMap = map;
        }
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.reflect.ReflectUtil#getFieldValue(java.lang.Object, java.lang.String)}.
     */
    @SuppressWarnings("unchecked")
    public void testGetFieldValueObjectString() {
        ReflectUtils reflectUtil = ReflectUtils.getInstance();
        Object value = null;
        Object thing = null;

        thing = new TestEntity();
        value = reflectUtil.getFieldValue( thing, "entityId");
        assertNotNull(value);
        assertEquals("33", value);

        value = reflectUtil.getFieldValue( thing, "id");
        assertNotNull(value);
        assertEquals(new Long(3), value);

        TestNesting nested = new TestNesting(10, "100", new String[] {"A", "B"});
        value = reflectUtil.getFieldValue( nested, "id");
        assertNotNull(value);
        assertEquals(10, value);

        value = reflectUtil.getFieldValue( nested, "testEntity.id");
        assertNotNull(value);
        assertEquals(new Long(3), value);

        // get list value
        value = reflectUtil.getFieldValue( nested, "sList[0]");
        assertNotNull(value);
        assertEquals("A", value);

        value = reflectUtil.getFieldValue( nested, "sList[1]");
        assertNotNull(value);
        assertEquals("B", value);

        value = reflectUtil.getFieldValue( nested, "sList");
        assertNotNull(value);
        assertEquals("A", ((List)value).get(0));

        // get map value
        value = reflectUtil.getFieldValue( nested, "sMap(A1)");
        assertNotNull(value);
        assertEquals("ONE", value);

        value = reflectUtil.getFieldValue( nested, "sMap(B2)");
        assertNotNull(value);
        assertEquals("TWO", value);

        value = reflectUtil.getFieldValue( thing, "extra");
        assertNull(value);

        value = reflectUtil.getFieldValue( thing, "sArray");
        assertNotNull(value);
        assertTrue(value.getClass().isArray());
        assertEquals("1", ((String[])value)[0]);
        assertEquals("2", ((String[])value)[1]);

        // basic pea support
        thing = new TestPea();
        value = reflectUtil.getFieldValue( thing, "id");
        assertNotNull(value);
        assertEquals("id", value);

        thing = new TestBean();
        try {
            value = reflectUtil.getFieldValue(thing, "id");
            fail("Should have thrown exception");
        } catch (FieldnameNotFoundException e) {
            assertNotNull(e.getMessage());
        }
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.reflect.ReflectUtil#setFieldValue(java.lang.Object, java.lang.String, java.lang.Object)}.
     */
    public void testSetFieldValue() {
        ReflectUtils reflectUtil = ReflectUtils.getInstance();
        Object thing = null;

        thing = new TestBean();
        reflectUtil.setFieldValue(thing, "myString", "TEST");
        assertEquals("TEST", ((TestBean)thing).getMyString());

        thing = new TestBean();
        reflectUtil.setFieldValue(thing, "myInt", 5);
        assertEquals(5, ((TestBean)thing).getMyInt());

        thing = new TestEntity();
        reflectUtil.setFieldValue(thing, "sArray", new String[] {"A", "B", "C"});
        assertEquals(3, ((TestEntity)thing).getSArray().length);
        assertEquals("A", ((TestEntity)thing).getSArray()[0]);
        assertEquals("B", ((TestEntity)thing).getSArray()[1]);
        assertEquals("C", ((TestEntity)thing).getSArray()[2]);

        thing = new TestBean();
        try {
            reflectUtil.setFieldValue(thing, "id", "uhohes");
            fail("Should have thrown exception");
        } catch (FieldnameNotFoundException e) {
            assertNotNull(e.getMessage());
        }
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.reflect.ReflectUtil#setFieldStringValue(java.lang.Object, java.lang.String, java.lang.String)}.
     */
    public void testSetFieldStringValue() {
        ReflectUtils reflectUtil = ReflectUtils.getInstance();
        Object thing = null;

        thing = new TestBean();
        reflectUtil.setFieldValue(thing, "myString", "TEST");
        assertEquals("TEST", ((TestBean)thing).getMyString());

        thing = new TestBean();
        reflectUtil.setFieldValue(thing, "myInt", "10");
        assertEquals(10, ((TestBean)thing).getMyInt());

        thing = new TestEntity();
        reflectUtil.setFieldValue(thing, "id", "6");
        assertEquals(new Long(6), ((TestEntity)thing).getId());

        thing = new TestEntity();
        reflectUtil.setFieldValue(thing, "sArray", "A, B, C");
        assertEquals(3, ((TestEntity)thing).getSArray().length);
        assertEquals("A", ((TestEntity)thing).getSArray()[0]);
        assertEquals("B", ((TestEntity)thing).getSArray()[1]);
        assertEquals("C", ((TestEntity)thing).getSArray()[2]);
    }

    public void testSetFieldValueWithConversion() {
        ReflectUtils reflectUtil = ReflectUtils.getInstance();
        Object thing = null;

        thing = new TestEntity();
        reflectUtil.setFieldValue(thing, "id", "10");
        assertEquals(new Long(10), ((TestEntity)thing).getId());

        thing = new TestEntity();
        reflectUtil.setFieldValue(thing, "id", 10);
        assertEquals(new Long(10), ((TestEntity)thing).getId());

        thing = new TestEntity();
        reflectUtil.setFieldValue(thing, "id", new String[] {"10"});
        assertEquals(new Long(10), ((TestEntity)thing).getId());

        thing = new TestEntity();
        reflectUtil.setFieldValue(thing, "bool", true);
        assertEquals(Boolean.TRUE, ((TestEntity)thing).getBool());

        thing = new TestEntity();
        reflectUtil.setFieldValue(thing, "bool", "true");
        assertEquals(Boolean.TRUE, ((TestEntity)thing).getBool());

        thing = new TestEntity();
        reflectUtil.setFieldValue(thing, "bool", "false");
        assertEquals(Boolean.FALSE, ((TestEntity)thing).getBool());

        thing = new TestEntity();
        reflectUtil.setFieldValue(thing, "bool", "");
        assertEquals(Boolean.FALSE, ((TestEntity)thing).getBool());

        thing = new TestEntity();
        reflectUtil.setFieldValue(thing, "extra", "stuff");
        assertEquals("stuff", ((TestEntity)thing).getExtra());

        thing = new TestEntity();
        reflectUtil.setFieldValue(thing, "extra", 100);
        assertEquals("100", ((TestEntity)thing).getExtra());

        thing = new TestEntity();
        reflectUtil.setFieldValue(thing, "extra", new String[] {"stuff"});
        assertEquals("stuff", ((TestEntity)thing).getExtra());

        thing = new TestEntity();
        reflectUtil.setFieldValue(thing, "extra", new String[] {"stuff", "plus"});
        assertEquals("stuff,plus", ((TestEntity)thing).getExtra());
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.reflect.ReflectUtil#getFieldTypes(java.lang.Class)}.
     */
    public void testGetFieldTypes() {
        ReflectUtils reflectUtil = ReflectUtils.getInstance();
        Map<String, Class<?>> types;

        types = reflectUtil.getFieldTypes(TestBean.class);
        assertNotNull(types);
        assertEquals(2, types.size());
        assertEquals(String.class, types.get("myString"));
        assertEquals(int.class, types.get("myInt"));
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.reflect.ReflectUtil#getFieldType(java.lang.Class, java.lang.String)}.
     */
    public void testGetFieldType() {
        ReflectUtils reflectUtil = ReflectUtils.getInstance();
        Class<?> type;

        type = reflectUtil.getFieldType(TestBean.class, "myString");
        assertNotNull(type);
        assertEquals(String.class, type);

        type = reflectUtil.getFieldType(TestBean.class, "myInt");
        assertNotNull(type);
        assertEquals(int.class, type);

        type = reflectUtil.getFieldType(TestPea.class, "id");
        assertNotNull(type);
        assertEquals(String.class, type);

        type = reflectUtil.getFieldType(TestNesting.class, "sList");
        assertNotNull(type);
        assertEquals(List.class, type);

        type = reflectUtil.getFieldType(TestNesting.class, "sMap");
        assertNotNull(type);
        assertEquals(Map.class, type);
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.reflect.ReflectUtil#getObjectValues(java.lang.Object)}.
     */
    public void testGetObjectValues() {
        Map<String, Object> m = null;
        ReflectUtils reflectUtil = ReflectUtils.getInstance();

        m = reflectUtil.getObjectValues( new TestNone() );
        assertNotNull(m);
        assertEquals(0, m.size());

        m = reflectUtil.getObjectValues( new TestPea() );
        assertNotNull(m);
        assertEquals(2, m.size());
        assertTrue(m.containsKey("id"));
        assertEquals("id", m.get("id"));
        assertTrue(m.containsKey("entityId"));
        assertEquals("EID", m.get("entityId"));

        m = reflectUtil.getObjectValues( new TestBean() );
        assertNotNull(m);
        assertEquals(2, m.size());
        assertTrue(m.containsKey("myInt"));
        assertTrue(m.containsKey("myString"));
        assertEquals(0, m.get("myInt"));
        assertEquals("woot", m.get("myString"));

        m = reflectUtil.getObjectValues( new TestEntity() );
        assertNotNull(m);
        assertEquals(6, m.size());
        assertTrue(m.containsKey("id"));
        assertTrue(m.containsKey("entityId"));
        assertTrue(m.containsKey("extra"));
        assertTrue(m.containsKey("sArray"));
        assertTrue(m.containsKey("bool"));
        assertTrue(m.containsKey("prefix"));
        assertEquals(new Long(3), m.get("id"));
        assertEquals("33", m.get("entityId"));
        assertEquals(null, m.get("extra"));
        assertEquals("crud", m.get("prefix"));
        assertTrue(m.get("sArray").getClass().isArray());
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.reflect.ReflectUtil#getFieldNameWithAnnotation(java.lang.Class, java.lang.Class)}.
     */
    public void testGetFieldNameWithAnnotation() {
        ReflectUtils reflectUtil = ReflectUtils.getInstance();
        String fieldName = null;

        fieldName = reflectUtil.getFieldNameWithAnnotation(TestBean.class, TestAnnote.class);
        assertEquals("myInt", fieldName);

        fieldName = reflectUtil.getFieldNameWithAnnotation(TestEntity.class, TestAnnote.class);
        assertEquals("entityId", fieldName);
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.reflect.ReflectUtil#getFieldValueAsString(java.lang.Object, java.lang.String, java.lang.Class)}.
     */
    public void testGetFieldValueObjectStringClassOfQextendsAnnotation() {
        String value = null;
        ReflectUtils reflectUtil = ReflectUtils.getInstance();

        try {
            value = reflectUtil.getFieldValueAsString( new TestBean(), "id", null);
            fail("Should have thrown exception");
        } catch (FieldnameNotFoundException e) {
            assertNotNull(e.getMessage());
        }

        value = reflectUtil.getFieldValueAsString( new TestEntity(), "extra", null);
        assertNull(value);

        value = reflectUtil.getFieldValueAsString( new TestPea(), "id", null);
        assertNotNull(value);
        assertEquals("id", value);

        value = reflectUtil.getFieldValueAsString( new TestEntity(), "id", null);
        assertNotNull(value);
        assertEquals("3", value);

        value = reflectUtil.getFieldValueAsString( new TestPea(), "id", TestAnnote.class);
        assertNotNull(value);
        assertEquals("EID", value);

        value = reflectUtil.getFieldValueAsString( new TestEntity(), "id", TestAnnote.class);
        assertNotNull(value);
        assertEquals("33", value);

        try {
            value = reflectUtil.getFieldValueAsString( new TestNone(), "id", TestAnnote.class);
            fail("Should have thrown exception");
        } catch (FieldnameNotFoundException e) {
            assertNotNull(e.getMessage());
        }

    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.reflect.ReflectUtil#clone(java.lang.Object, int, java.lang.String[])}.
     */
    public void testClone() {
        ReflectUtils reflectUtil = ReflectUtils.getInstance();

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

    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.reflect.ReflectUtil#copy(java.lang.Object, java.lang.Object, int, java.lang.String[], boolean)}.
     */
    public void testCopy() {
        ReflectUtils reflectUtil = ReflectUtils.getInstance();

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
    }

    public void testPopulate() {
        ReflectUtils reflectUtil = ReflectUtils.getInstance();
        List<String> results = null;
        Map<String, Object> properties = new HashMap<String, Object>();

        // empty should be ok and should not change anything
        TestBean target = new TestBean();
        results = reflectUtil.populate(target, properties);
        assertNotNull(results);
        assertEquals(0, results.size());
        assertNotNull(target);
        assertEquals(0, target.getMyInt());
        assertEquals("woot", target.getMyString());

        // non matching fields should be ok
        properties.put("xxxxxxx", "xxxxxx");
        properties.put("yyyyyyy", 1000000);
        results = reflectUtil.populate(target, properties);
        assertNotNull(results);
        assertEquals(0, results.size());
        assertNotNull(target);
        assertEquals(0, target.getMyInt());
        assertEquals("woot", target.getMyString());

        // strings should be ok
        properties.put("myInt", "100");
        properties.put("myString", "NEW");
        results = reflectUtil.populate(target, properties);
        assertNotNull(results);
        assertEquals(2, results.size());
        assertNotNull(target);
        assertEquals(100, target.getMyInt());
        assertEquals("NEW", target.getMyString());

        // string arrays should be ok also
        properties.put("myInt", new String[] {"1000"});
        properties.put("myString", new String[] {"OLD","BLUE"});
        results = reflectUtil.populate(target, properties);
        assertNotNull(results);
        assertEquals(2, results.size());
        assertNotNull(target);
        assertEquals(1000, target.getMyInt());
        assertEquals("OLD,BLUE", target.getMyString());

        // objects
        properties.put("myInt", new Long(222));
        properties.put("myString", 55555);
        results = reflectUtil.populate(target, properties);
        assertNotNull(results);
        assertEquals(2, results.size());
        assertNotNull(target);
        assertEquals(222, target.getMyInt());
        assertEquals("55555", target.getMyString());
    }

    public void testPopulateFromParams() {
        ReflectUtils reflectUtil = ReflectUtils.getInstance();
        List<String> results = null;
        Map<String, String[]> properties = new HashMap<String, String[]>();

        TestEntity target = new TestEntity();

        properties.put("id", new String[] {"1000"});
        properties.put("extra", new String[] {"OLD"});
        properties.put("sArray", new String[] {"AA","BB","CC"});
        results = reflectUtil.populateFromParams(target, properties);
        assertNotNull(results);
        assertEquals(3, results.size());
        assertNotNull(target);
        assertEquals(new Long(1000), target.getId());
        assertEquals("OLD", target.getExtra());
        assertEquals("33", target.getEntityId());
        assertEquals(null, target.getBool());
        assertEquals(3, target.getSArray().length);

    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.reflect.ReflectUtil#makeFieldNameFromMethod(java.lang.String)}.
     */
    public void testMakeFieldNameFromMethod() {
        String name = null;

        name = ReflectUtils.makeFieldNameFromMethod("getStuff");
        assertEquals("stuff", name);

        name = ReflectUtils.makeFieldNameFromMethod("getSomeStuff");
        assertEquals("someStuff", name);

        name = ReflectUtils.makeFieldNameFromMethod("setStuff");
        assertEquals("stuff", name);

        name = ReflectUtils.makeFieldNameFromMethod("isStuff");
        assertEquals("stuff", name);

        name = ReflectUtils.makeFieldNameFromMethod("stuff");
        assertEquals("stuff", name);
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.reflect.ReflectUtil#getSuperclasses(java.lang.Class)}.
     */
    public void testGetSuperclasses() {
        List<Class<?>> superClasses = null;

        superClasses = ReflectUtils.getSuperclasses(TestNone.class);
        assertNotNull(superClasses);
        assertEquals(1, superClasses.size());
        assertEquals(TestNone.class, superClasses.get(0));

        superClasses = ReflectUtils.getSuperclasses(TestImplOne.class);
        assertNotNull(superClasses);
        assertEquals(3, superClasses.size());
        assertTrue( superClasses.contains(TestImplOne.class) );
        assertTrue( superClasses.contains(TestInterfaceOne.class) );
        assertTrue( superClasses.contains(Serializable.class) );

        superClasses = ReflectUtils.getSuperclasses(TestImplFour.class);
        assertNotNull(superClasses);
        assertTrue(superClasses.size() >= 7);
        assertTrue( superClasses.contains(TestImplFour.class) );
        assertTrue( superClasses.contains(TestInterfaceOne.class) );
        assertTrue( superClasses.contains(TestInterfaceFour.class) );
        assertTrue( superClasses.contains(Serializable.class) );
        assertTrue( superClasses.contains(Runnable.class) );
        assertTrue( superClasses.contains(Cloneable.class) );
        assertTrue( superClasses.contains(Readable.class) );
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.reflect.ReflectUtil#getClassFromCollection(java.util.Collection)}.
     */
    @SuppressWarnings("unchecked")
    public void testGetClassFromCollection() {
        Class<?> result = null;

        // null returns object class
        result = ReflectUtils.getClassFromCollection(null);
        assertNotNull(result);
        assertEquals(Object.class, result);

        // empty collection is always object
        result = ReflectUtils.getClassFromCollection( new ArrayList<String>() );
        assertNotNull(result);
        assertEquals(Object.class, result);

        // NOTE: Cannot get real type from empty collections

        // try with collections that have things in them
        List<Object> l = new ArrayList<Object>();
        l.add(new String("testing"));
        result = ReflectUtils.getClassFromCollection(l);
        assertNotNull(result);
        assertEquals(String.class, result);

        HashSet<Object> s = new HashSet<Object>();
        s.add(new Double(22.0));
        result = ReflectUtils.getClassFromCollection(s);
        assertNotNull(result);
        assertEquals(Double.class, result);

        List v = new Vector<Object>();
        v.add( new Integer(30) );
        result = ReflectUtils.getClassFromCollection(v);
        assertNotNull(result);
        assertEquals(Integer.class, result);
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.reflect.ReflectUtil#contains(T[], java.lang.Object)}.
     */
    public void testContains() {
        assertFalse( ReflectUtils.contains(new String[] {}, "stuff") );
        assertFalse( ReflectUtils.contains(new String[] {"apple"}, "stuff") );
        assertTrue( ReflectUtils.contains(new String[] {"stuff"}, "stuff") );
        assertTrue( ReflectUtils.contains(new String[] {"stuff","other","apple"}, "stuff") );
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.reflect.ReflectUtil#capitalize(java.lang.String)}.
     */
    public void testCapitalize() {
        assertTrue( ReflectUtils.capitalize("lower").equals("Lower") );
        assertTrue( ReflectUtils.capitalize("UPPER").equals("UPPER") );
        assertTrue( ReflectUtils.capitalize("myStuff").equals("MyStuff") );
        assertTrue( ReflectUtils.capitalize("MyStuff").equals("MyStuff") );
        assertTrue( ReflectUtils.capitalize("").equals("") );
        assertTrue( ReflectUtils.capitalize("m").equals("M") );
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.reflect.ReflectUtil#unCapitalize(java.lang.String)}.
     */
    public void testUnCapitalize() {
        assertTrue( ReflectUtils.unCapitalize("lower").equals("lower") );
        assertTrue( ReflectUtils.unCapitalize("UPPER").equals("uPPER") );
        assertTrue( ReflectUtils.unCapitalize("MyStuff").equals("myStuff") );
        assertTrue( ReflectUtils.unCapitalize("myStuff").equals("myStuff") );
        assertTrue( ReflectUtils.unCapitalize("").equals("") );
        assertTrue( ReflectUtils.unCapitalize("M").equals("m") );
    }

}
