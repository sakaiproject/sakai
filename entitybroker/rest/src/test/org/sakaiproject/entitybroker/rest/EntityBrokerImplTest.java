/**
 * Copyright (c) 2007-2010 The Apereo Foundation
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
 * EntityBrokerImplTest.java - 2007 Jul 21, 2007 3:04:55 PM - entity-broker - AZ
 */

package org.sakaiproject.entitybroker.rest;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Set;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.impl.EntityBrokerImpl;
import org.sakaiproject.entitybroker.mocks.ActionsEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.data.MyEntity;
import org.sakaiproject.entitybroker.mocks.data.TestData;

/**
 * Testing the entitybroker implementation
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class EntityBrokerImplTest {

    protected EntityBrokerImpl entityBroker;

    private TestData td;

    @Before
    public void setUp() {
        // init the test data
        td = new TestData();

        // setup fake internal services
        ServiceTestManager tm = new ServiceTestManager(td);

        // create and setup the object to be tested
        entityBroker = tm.entityBroker;
    }

    /**
     * ADD unit tests below here, use testMethod as the name of the unit test, Note that if a method
     * is overloaded you should include the arguments in the test name like so: testMethodClassInt
     * (for method(Class, int);
     */

    /**
     * Test method for
     * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#entityExists(java.lang.String)}.
     */
    @Test
    public void testEntityExists() {
        boolean exists = false;

        exists = entityBroker.entityExists(TestData.REF1);
        assertTrue(exists);

        exists = entityBroker.entityExists(TestData.REF1_1);
        assertTrue(exists);

        exists = entityBroker.entityExists(TestData.REF2);
        assertTrue(exists);

        // test that invalid id with valid prefix does not pass
        exists = entityBroker.entityExists(TestData.REF1_INVALID);
        assertFalse(exists);

        // test that unregistered ref does not pass
        exists = entityBroker.entityExists(TestData.REF9);
        assertFalse(exists);

        // test that invalid ref causes exception
        try {
            exists = entityBroker.entityExists(TestData.INVALID_REF);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    /**
     * Test method for
     * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#getEntityURL(java.lang.String)}.
     */
    @Test
    public void testGetEntityURL() {
        String url = null;

        url = entityBroker.getEntityURL(TestData.REF1);
        assertEquals(TestData.URL1, url);

        url = entityBroker.getEntityURL(TestData.REF2);
        assertEquals(TestData.URL2, url);

        url = entityBroker.getEntityURL(TestData.REF1_INVALID);

        try {
            url = entityBroker.getEntityURL(TestData.INVALID_REF);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

    }

    @Test
    public void testGetEntityURLStringStringString() {
        String url = null;

        url = entityBroker.getEntityURL(TestData.REF1, null, null);
        assertEquals(TestData.URL1, url);

        url = entityBroker.getEntityURL(TestData.REF2, null, null);
        assertEquals(TestData.URL2, url);

        // test adding viewkey
        url = entityBroker.getEntityURL(TestData.REF1, EntityView.VIEW_SHOW, null);
        assertEquals(TestData.URL1, url);

        url = entityBroker.getEntityURL(TestData.REF1, EntityView.VIEW_LIST, null);
        assertEquals(TestData.SPACE_URL1, url);

        url = entityBroker.getEntityURL(TestData.REF1, EntityView.VIEW_NEW, null);
        assertEquals(TestData.SPACE_URL1 + "/new", url);

        url = entityBroker.getEntityURL(TestData.REF1, EntityView.VIEW_EDIT, null);
        assertEquals(TestData.URL1 + "/edit", url);

        url = entityBroker.getEntityURL(TestData.REF1, EntityView.VIEW_DELETE, null);
        assertEquals(TestData.URL1 + "/delete", url);

        // test extension
        url = entityBroker.getEntityURL(TestData.REF1, null, "xml");
        assertEquals(TestData.URL1 + ".xml", url);

        url = entityBroker.getEntityURL(TestData.REF2, null, "json");
        assertEquals(TestData.URL2 + ".json", url);

        // test both
        url = entityBroker.getEntityURL(TestData.REF1, EntityView.VIEW_EDIT, "xml");
        assertEquals(TestData.URL1 + "/edit.xml", url);
    }

    @Test
    public void testIsPrefixRegistered() {
        assertTrue( entityBroker.isPrefixRegistered(TestData.PREFIX1) );
        assertTrue( entityBroker.isPrefixRegistered(TestData.PREFIX2) );
        assertTrue( entityBroker.isPrefixRegistered(TestData.PREFIX3) );
        assertTrue( entityBroker.isPrefixRegistered(TestData.PREFIX4) );
        assertTrue( entityBroker.isPrefixRegistered(TestData.PREFIX5) );

        assertFalse( entityBroker.isPrefixRegistered(TestData.PREFIX9) );
        assertFalse( entityBroker.isPrefixRegistered("XXXXXX") );
    }

    /**
     * Test method for
     * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#getRegisteredPrefixes()}.
     */
    @Test
    public void testGetRegisteredPrefixes() {
        Set<String> s = entityBroker.getRegisteredPrefixes();
        assertNotNull(s);
        assertTrue(s.contains(TestData.PREFIX1));
        assertTrue(s.contains(TestData.PREFIX2));
        assertTrue(s.contains(TestData.PREFIX3));
        assertFalse(s.contains(TestData.PREFIX9));
    }

    /**
     * Test method for
     * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#fireEvent(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testFireEvent() {
        // we are mostly handing this to a mocked service so we can only check to see if errors occur
        entityBroker.fireEvent(TestData.EVENT1_NAME, TestData.REF1);
        entityBroker.fireEvent(TestData.EVENT1_NAME, TestData.REF2);
        entityBroker.fireEvent(TestData.EVENT2_NAME, TestData.REF1);
        entityBroker.fireEvent(TestData.EVENT2_NAME, "XXXXXXXXXXXXXX");

        // event with a null name should die
        try {
            entityBroker.fireEvent(null, TestData.REF1);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            entityBroker.fireEvent("", TestData.REF1);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

    }

    /**
     * Test method for
     * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#parseReference(java.lang.String)}.
     */
    @Test
    public void testParseReference() {
        EntityReference er = null;

        er = entityBroker.parseReference(TestData.REF1);
        assertNotNull(er);
        assertEquals(TestData.PREFIX1, er.getPrefix());
        assertEquals(TestData.IDS1[0], er.getId());

        er = entityBroker.parseReference(TestData.REF2);
        assertNotNull(er);
        assertEquals(TestData.PREFIX2, er.getPrefix());

        // test parsing a defined reference
        er = entityBroker.parseReference(TestData.REF3A);
        assertNotNull(er);
        assertEquals(TestData.PREFIX3, er.getPrefix());

        // parsing of unregistered entity references returns null
        er = entityBroker.parseReference(TestData.REF9);
        assertNull(er);

        // parsing with nonexistent prefix returns null
        er = entityBroker.parseReference("/totallyfake/notreal");
        assertNull(er);

        try {
            er = entityBroker.parseReference(TestData.INVALID_REF);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    /**
     * Test method for
     * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#fetchEntity(java.lang.String)}.
     */
    @Test
    public void testFetchEntity() {
        Object obj = null;

        obj = entityBroker.fetchEntity(TestData.REF4);
        assertNotNull(obj);
        assertTrue(obj instanceof MyEntity);
        MyEntity entity = (MyEntity) obj;
        assertEquals(entity.getId(), TestData.IDS4[0]);

        obj = entityBroker.fetchEntity(TestData.REF4_two);
        assertNotNull(obj);
        assertTrue(obj instanceof MyEntity);
        MyEntity entity2 = (MyEntity) obj;
        assertEquals(entity2.getId(), TestData.IDS4[1]);

        // no object available should cause failure
        obj = entityBroker.fetchEntity(TestData.REF1);
        assertNull(obj);

        obj = entityBroker.fetchEntity(TestData.REF2);
        assertNull(obj);

        // use an unregistered provider to trigger the attempt to do a legacy lookup which will fail and return null
        obj = entityBroker.fetchEntity(TestData.REF9);
        assertNull(obj);

        // expect invalid reference to fail
        try {
            obj = entityBroker.fetchEntity(TestData.INVALID_REF);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            obj = entityBroker.fetchEntity(null);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

    }

    @Test
    public void testFormatAndOutputEntity() {

        String fo = null;
        String reference = null;
        OutputStream output = null;
        String format = Formats.XML;

        // XML test valid resolveable entity
        reference = TestData.REF4;
        output = new ByteArrayOutputStream();
        entityBroker.formatAndOutputEntity(reference, format, null, output, null);
        fo = output.toString();
        assertNotNull(fo);
        assertTrue(fo.length() > 20);
        assertTrue(fo.contains(TestData.PREFIX4));
        assertTrue(fo.contains("<id>4-one</id>"));
        assertTrue(fo.contains(EntityEncodingManager.ENTITY_REFERENCE));

        // test list of entities
        ArrayList<EntityData> testEntities = new ArrayList<EntityData>();
        testEntities.add( new EntityData(TestData.REF4, null, TestData.entity4) );
        testEntities.add( new EntityData(TestData.REF4_two, null, TestData.entity4_two) );
        reference = TestData.SPACE4;
        output = new ByteArrayOutputStream();
        entityBroker.formatAndOutputEntity(reference, format, testEntities, output, null);
        fo = output.toString();
        assertNotNull(fo);
        assertTrue(fo.length() > 20);
        assertTrue(fo.contains(TestData.PREFIX4));
        assertTrue(fo.contains("<id>4-one</id>"));
        assertTrue(fo.contains("<id>4-two</id>"));
        assertFalse(fo.contains("<id>4-three</id>"));
        assertTrue(fo.contains(EntityEncodingManager.ENTITY_REFERENCE));

    }

    /**
     * Convenience method for making byte content encoded into UTF-8
     */
    private byte[] makeUTF8Bytes(String string) {
        byte[] bytes;
        try {
            bytes = string.getBytes(Formats.UTF_8);
        } catch (UnsupportedEncodingException e) {
            bytes = string.getBytes();
        }
        return bytes;
    }

    @Test
    public void testTranslateInputToEntity() {
        InputStream input = null;
        MyEntity me = null;

        // test creating an entity
        String reference = TestData.SPACE6;
        String format = Formats.XML;
        input = new ByteArrayInputStream( makeUTF8Bytes("<"+TestData.PREFIX6+"><stuff>TEST</stuff><number>5</number></"+TestData.PREFIX6+">") );
        me = (MyEntity) entityBroker.translateInputToEntity(reference, format, input, null);
        assertNotNull(me);
        assertNull(me.getId());
        assertEquals("TEST", me.getStuff());
        assertEquals(5, me.getNumber());

        // test modifying an entity
        reference = TestData.REF6_2;
        input = new ByteArrayInputStream( makeUTF8Bytes("<"+TestData.PREFIX6+"><id>"+TestData.IDS6[1]+"</id><stuff>TEST-PUT</stuff><number>8</number></"+TestData.PREFIX6+">") );
        me = (MyEntity) entityBroker.translateInputToEntity(reference, format, input, null);
        assertNotNull(me);
        assertNotNull(me.getId());
        assertEquals(TestData.IDS6[1], me.getId());
        assertEquals("TEST-PUT", me.getStuff());
        assertEquals(8, me.getNumber());
    }

    @Test
    public void testExecuteCustomAction() {
        ActionReturn actionReturn = null;

        // test the double/xxx/clear actions
        ActionsEntityProviderMock actionProvider = td.entityProviderA1;

        // double
        MyEntity me = (MyEntity) actionProvider.getEntity( new EntityReference(TestData.REFA1) );
        int num = me.getNumber();
        actionReturn = entityBroker.executeCustomAction(TestData.REFA1, "double", null, null);
        //ActionReturn actionReturn = entityActionsManager.handleCustomActionExecution(actionProvider, ref, "double", null, null);
        assertNotNull(actionReturn);
        assertNotNull(actionReturn.entityData);
        MyEntity doubleMe = (MyEntity) actionReturn.entityData.getData();
        assertEquals(doubleMe.getNumber(), num * 2);
        assertEquals(me.getId(), doubleMe.getId());

        // xxx
        MyEntity me1 = (MyEntity) actionProvider.getEntity( new EntityReference(TestData.REFA1) );
        assertFalse("xxx".equals(me1.extra));
        assertFalse("xxx".equals(me1.getStuff()));
        actionReturn = entityBroker.executeCustomAction(TestData.REFA1, "xxx", null, null);
        assertNull(actionReturn);
        MyEntity xxxMe = (MyEntity) actionProvider.getEntity( new EntityReference(TestData.REFA1) );
        assertEquals(me1.getId(), xxxMe.getId());
        assertTrue("xxx".equals(xxxMe.extra));
        assertTrue("xxx".equals(xxxMe.getStuff()));

        // clear
        assertEquals(2, actionProvider.myEntities.size());
        actionReturn = entityBroker.executeCustomAction(TestData.SPACEA1, "clear", null, null);
        assertEquals(0, actionProvider.myEntities.size());

        // check exception when try to execute invalid action
        try {
            entityBroker.executeCustomAction(TestData.REF3A, "action", null, null);
            fail("should have thrown exeception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            entityBroker.executeCustomAction(TestData.INVALID_REF, "action", null, null);
            fail("should have thrown exeception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        String ref = TestData.SPACEA1+"/yyyy/zzzzz/123456";
        actionReturn = entityBroker.executeCustomAction(ref, "yyyy", null, null);
        assertNotNull(actionReturn);
        assertEquals(ref, actionReturn.getOutputString());

    }

}
