/**
 * Copyright (c) 2007-2017 The Apereo Foundation
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

package org.sakaiproject.entitybroker.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.dao.EntityBrokerDao;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.impl.data.TestDataPreload;
import org.sakaiproject.entitybroker.mocks.data.MyEntity;
import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

/**
 * Testing the entitybroker implementation
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
@ContextConfiguration(locations={
		"/database-test.xml",
		"classpath:org/sakaiproject/entitybroker/spring-jdbc.xml" })
public class EntityBrokerTransactionalTest extends AbstractTransactionalJUnit4SpringContextTests {

    protected EntityBrokerImpl entityBroker;

    @Autowired
    @Qualifier("org.sakaiproject.entitybroker.dao.EntityBrokerDao")
    private EntityBrokerDao dao;
    private TestData td;
    @Autowired
    private TestDataPreload tdp;

    // run this before each test starts
    @Before
    public void onSetUp() throws Exception {
        // init the test data
        td = new TestData();

        // setup fake internal services
        ServiceTestManager tm = new ServiceTestManager(td, dao);

        // create and setup the object to be tested
        entityBroker = new EntityBrokerImpl();
        entityBroker.setEntityBrokerManager( tm.entityBrokerManager );
        entityBroker.setEntityProviderManager( tm.entityProviderManager );
        entityBroker.setTagSearchService( tm.entityTaggingService );
        entityBroker.setPropertiesProvider( tm.entityMetaPropertiesService );
        entityBroker.setRequestStorage( tm.requestStorage );
        // NOTE: no external integration provider set
        // NOTE: no REST provider set
    }

    /**
     * ADD unit tests below here, use testMethod as the name of the unit test, Note that if a method
     * is overloaded you should include the arguments in the test name like so: testMethodClassInt
     * (for method(Class, int);
     */

    @Test
    public void testValidTestData() {
        // ensure the test data is setup the way we think
        Assert.assertNotNull(td);
        Assert.assertNotNull(tdp);

        // Assert.assertEquals(new Long(1), tdp.pNode1.getId());
    }

    /**
     * Test method for
     * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#entityExists(java.lang.String)}.
     */
    @Test
    public void testEntityExists() {
        boolean exists = false;

        exists = entityBroker.entityExists(TestData.REF1);
        Assert.assertTrue(exists);

        exists = entityBroker.entityExists(TestData.REF1_1);
        Assert.assertTrue(exists);

        exists = entityBroker.entityExists(TestData.REF2);
        Assert.assertTrue(exists);

        // test that invalid id with valid prefix does not pass
        exists = entityBroker.entityExists(TestData.REF1_INVALID);
        Assert.assertFalse(exists);

        // test that unregistered ref does not pass
        exists = entityBroker.entityExists(TestData.REF9);
        Assert.assertFalse(exists);

        // test that invalid ref causes exception
        try {
            exists = entityBroker.entityExists(TestData.INVALID_REF);
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e.getMessage());
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
        Assert.assertEquals(TestData.URL1, url);

        url = entityBroker.getEntityURL(TestData.REF2);
        Assert.assertEquals(TestData.URL2, url);

        url = entityBroker.getEntityURL(TestData.REF1_INVALID);

        try {
            url = entityBroker.getEntityURL(TestData.INVALID_REF);
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e.getMessage());
        }

    }

    @Test
    public void testGetEntityURLStringStringString() {
        String url = null;

        url = entityBroker.getEntityURL(TestData.REF1, null, null);
        Assert.assertEquals(TestData.URL1, url);

        url = entityBroker.getEntityURL(TestData.REF2, null, null);
        Assert.assertEquals(TestData.URL2, url);

        // test adding viewkey
        url = entityBroker.getEntityURL(TestData.REF1, EntityView.VIEW_SHOW, null);
        Assert.assertEquals(TestData.URL1, url);

        url = entityBroker.getEntityURL(TestData.REF1, EntityView.VIEW_LIST, null);
        Assert.assertEquals(TestData.SPACE_URL1, url);

        url = entityBroker.getEntityURL(TestData.REF1, EntityView.VIEW_NEW, null);
        Assert.assertEquals(TestData.SPACE_URL1 + "/new", url);

        url = entityBroker.getEntityURL(TestData.REF1, EntityView.VIEW_EDIT, null);
        Assert.assertEquals(TestData.URL1 + "/edit", url);

        url = entityBroker.getEntityURL(TestData.REF1, EntityView.VIEW_DELETE, null);
        Assert.assertEquals(TestData.URL1 + "/delete", url);

        // test extension
        url = entityBroker.getEntityURL(TestData.REF1, null, "xml");
        Assert.assertEquals(TestData.URL1 + ".xml", url);

        url = entityBroker.getEntityURL(TestData.REF2, null, "json");
        Assert.assertEquals(TestData.URL2 + ".json", url);

        // test both
        url = entityBroker.getEntityURL(TestData.REF1, EntityView.VIEW_EDIT, "xml");
        Assert.assertEquals(TestData.URL1 + "/edit.xml", url);
    }

    @Test
    public void testIsPrefixRegistered() {
        Assert.assertTrue( entityBroker.isPrefixRegistered(TestData.PREFIX1) );
        Assert.assertTrue( entityBroker.isPrefixRegistered(TestData.PREFIX2) );
        Assert.assertTrue( entityBroker.isPrefixRegistered(TestData.PREFIX3) );
        Assert.assertTrue( entityBroker.isPrefixRegistered(TestData.PREFIX4) );
        Assert.assertTrue( entityBroker.isPrefixRegistered(TestData.PREFIX5) );

        Assert.assertFalse( entityBroker.isPrefixRegistered(TestData.PREFIX9) );
        Assert.assertFalse( entityBroker.isPrefixRegistered("XXXXXX") );
    }

    /**
     * Test method for
     * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#getRegisteredPrefixes()}.
     */
    @Test
    public void testGetRegisteredPrefixes() {
        Set<String> s = entityBroker.getRegisteredPrefixes();
        Assert.assertNotNull(s);
        Assert.assertTrue(s.contains(TestData.PREFIX1));
        Assert.assertTrue(s.contains(TestData.PREFIX2));
        Assert.assertTrue(s.contains(TestData.PREFIX3));
        Assert.assertFalse(s.contains(TestData.PREFIX9));
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
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e.getMessage());
        }

        try {
            entityBroker.fireEvent("", TestData.REF1);
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e.getMessage());
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
        Assert.assertNotNull(er);
        Assert.assertEquals(TestData.PREFIX1, er.getPrefix());
        Assert.assertEquals(TestData.IDS1[0], er.getId());

        er = entityBroker.parseReference(TestData.REF2);
        Assert.assertNotNull(er);
        Assert.assertEquals(TestData.PREFIX2, er.getPrefix());

        // test parsing a defined reference
        er = entityBroker.parseReference(TestData.REF3A);
        Assert.assertNotNull(er);
        Assert.assertEquals(TestData.PREFIX3, er.getPrefix());

        // parsing of unregistered entity references returns null
        er = entityBroker.parseReference(TestData.REF9);
        Assert.assertNull(er);

        // parsing with nonexistent prefix returns null
        er = entityBroker.parseReference("/totallyfake/notreal");
        Assert.assertNull(er);

        try {
            er = entityBroker.parseReference(TestData.INVALID_REF);
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e.getMessage());
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
        Assert.assertNotNull(obj);
        Assert.assertTrue(obj instanceof MyEntity);
        MyEntity entity = (MyEntity) obj;
        Assert.assertEquals(entity.getId(), TestData.IDS4[0]);

        obj = entityBroker.fetchEntity(TestData.REF4_two);
        Assert.assertNotNull(obj);
        Assert.assertTrue(obj instanceof MyEntity);
        MyEntity entity2 = (MyEntity) obj;
        Assert.assertEquals(entity2.getId(), TestData.IDS4[1]);

        // no object available should cause Assert.failure
        obj = entityBroker.fetchEntity(TestData.REF1);
        Assert.assertNull(obj);

        obj = entityBroker.fetchEntity(TestData.REF2);
        Assert.assertNull(obj);

        // use an unregistered provider to trigger the attempt to do a legacy lookup which will Assert.fail and return null
        obj = entityBroker.fetchEntity(TestData.REF9);
        Assert.assertNull(obj);

        // expect invalid reference to Assert.fail
        try {
            obj = entityBroker.fetchEntity(TestData.INVALID_REF);
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e.getMessage());
        }

        try {
            obj = entityBroker.fetchEntity(null);
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e.getMessage());
        }

    }

    @Test
    public void testFormatAndOutputEntity() {
        // test no provider
        String reference = TestData.REF4;
        OutputStream output = new ByteArrayOutputStream();
        String format = Formats.XML;

        try {
            entityBroker.formatAndOutputEntity(reference, format, null, output, null);
            Assert.fail("Should have died");
        } catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e.getMessage());
        }
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
        // test no provider
        String reference = TestData.SPACE6;
        String format = Formats.XML;
        InputStream input = new ByteArrayInputStream( makeUTF8Bytes("<"+TestData.PREFIX6+"><stuff>TEST</stuff><number>5</number></"+TestData.PREFIX6+">") );
        try {
            entityBroker.translateInputToEntity(reference, format, input, null);
            Assert.fail("Should have died");
        } catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testExecuteCustomAction() {
        // test no provider
        try {
            entityBroker.executeCustomAction(TestData.REFA1, "double", null, null);
            Assert.fail("Should have died");
        } catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    /**
     * Test method for
     * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#findEntityRefs(java.lang.String[], java.lang.String[], java.lang.String[], boolean)}.
     */
    @Test
    public void testFindEntityRefs() {
        List<String> l = null;

        // test search with limit by prefix
        l = entityBroker.findEntityRefs(new String[] { TestData.PREFIX5 }, null, null, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());
        Assert.assertTrue(l.contains(TestData.REF5));
        Assert.assertTrue(l.contains(TestData.REF5_2));

        // test search with limit by prefix (check that no results ok)
        l = entityBroker.findEntityRefs(new String[] { TestData.INVALID_REF }, null, null, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

        // test searching with multiple prefixes
        l = entityBroker.findEntityRefs(new String[] { TestData.PREFIX5, TestData.PREFIX1 }, null,
                null, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());
        Assert.assertTrue(l.contains(TestData.REF5));
        Assert.assertTrue(l.contains(TestData.REF5_2));

        // test searching by names
        l = entityBroker.findEntityRefs(new String[] { TestData.PREFIX5 },
                new String[] { TestData.PROPERTY_NAME5A }, null, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        Assert.assertTrue(l.contains(TestData.REF5));

        // test searching by invalid name (return no results)
        l = entityBroker.findEntityRefs(new String[] { TestData.PREFIX5 },
                new String[] { TestData.INVALID_REF }, null, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

        // test searching with multiple names
        l = entityBroker.findEntityRefs(new String[] { TestData.PREFIX5 }, new String[] {
                TestData.PROPERTY_NAME5A, TestData.PROPERTY_NAME5C }, null, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());
        Assert.assertTrue(l.contains(TestData.REF5));
        Assert.assertTrue(l.contains(TestData.REF5_2));

        // test search limit by values (long exact match)
        l = entityBroker.findEntityRefs(new String[] { TestData.PREFIX5 },
                new String[] { TestData.PROPERTY_NAME5C }, new String[] { TestData.PROPERTY_VALUE5C },
                true);
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        Assert.assertTrue(l.contains(TestData.REF5_2));

        // test search limit by values (exact match)
        l = entityBroker.findEntityRefs(new String[] { TestData.PREFIX5 },
                new String[] { TestData.PROPERTY_NAME5B }, new String[] { TestData.PROPERTY_VALUE5B },
                true);
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        Assert.assertTrue(l.contains(TestData.REF5));

        // cannot have empty or null prefix
        try {
            l = entityBroker.findEntityRefs(new String[] {},
                    new String[] { TestData.PROPERTY_NAME5A }, null, true);
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e.getMessage());
        }

        try {
            l = entityBroker.findEntityRefs(null, new String[] { TestData.PROPERTY_NAME5A }, null,
                    true);
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e.getMessage());
        }

        // test search limit by values cannot have name null or empty
        try {
            l = entityBroker.findEntityRefs(new String[] { TestData.PREFIX5 }, new String[] {},
                    new String[] { TestData.PROPERTY_VALUE5A }, true);
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e.getMessage());
        }

        // test name and values arrays must match sizes
        try {
            l = entityBroker.findEntityRefs(new String[] { TestData.PREFIX5 }, new String[] {
                    TestData.PROPERTY_NAME5A, TestData.PROPERTY_NAME5B },
                    new String[] { TestData.PROPERTY_VALUE5A }, true);
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e.getMessage());
        }

        // test search with all empty fields Assert.fail
        try {
            l = entityBroker.findEntityRefs(new String[] {}, new String[] {}, new String[] {}, false);
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e.getMessage());
        }

    }

    /**
     * Test method for
     * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#getProperties(java.lang.String)}.
     */
    @Test
    public void testGetProperties() {
        Map<String, String> m = null;

        m = entityBroker.getProperties(TestData.REF5);
        Assert.assertNotNull(m);
        Assert.assertEquals(2, m.size());
        Assert.assertTrue(m.containsKey(TestData.PROPERTY_NAME5A));
        Assert.assertTrue(m.containsKey(TestData.PROPERTY_NAME5B));

        m = entityBroker.getProperties(TestData.REF5_2);
        Assert.assertNotNull(m);
        Assert.assertEquals(1, m.size());
        Assert.assertTrue(m.containsKey(TestData.PROPERTY_NAME5C));

        // ref with no properties should fetch none
        m = entityBroker.getProperties(TestData.REF1);
        Assert.assertNotNull(m);
        Assert.assertTrue(m.isEmpty());

        // make sure invalid ref causes Assert.failure
        try {
            m = entityBroker.getProperties(TestData.INVALID_REF);
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e.getMessage());
        }

    }

    /**
     * Test method for
     * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#getPropertyValue(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testGetPropertyValue() {
        String value = null;

        value = entityBroker.getPropertyValue(TestData.REF5, TestData.PROPERTY_NAME5A);
        Assert.assertNotNull(value);
        Assert.assertEquals(TestData.PROPERTY_VALUE5A, value);

        value = entityBroker.getPropertyValue(TestData.REF5, TestData.PROPERTY_NAME5B);
        Assert.assertNotNull(value);
        Assert.assertEquals(TestData.PROPERTY_VALUE5B, value);

        // test large value retrieval
        value = entityBroker.getPropertyValue(TestData.REF5_2, TestData.PROPERTY_NAME5C);
        Assert.assertNotNull(value);
        Assert.assertEquals(TestData.PROPERTY_VALUE5C, value);

        // nonexistent value property get retrieves null
        value = entityBroker.getPropertyValue(TestData.REF5, "XXXXXXXXXXXX");
        Assert.assertNull(value);

        // make sure invalid ref causes Assert.failure
        try {
            value = entityBroker.getPropertyValue(TestData.INVALID_REF, TestData.PROPERTY_NAME5A);
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e.getMessage());
        }

        // null name causes Assert.failure
        try {
            value = entityBroker.getPropertyValue(TestData.REF5, null);
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e.getMessage());
        }

        // empty name causes Assert.failure
        try {
            value = entityBroker.getPropertyValue(TestData.REF5, "");
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e.getMessage());
        }

    }

    /**
     * Test method for
     * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#setPropertyValue(java.lang.String, java.lang.String, java.lang.String[])}.
     */
    @Test
    public void testSetPropertyValue() {
        String value = null;

        // check that we can save a new property on an entity
        entityBroker.setPropertyValue(TestData.REF5, "newNameAlpha", "newValueAlpha");
        value = entityBroker.getPropertyValue(TestData.REF5, "newNameAlpha");
        Assert.assertNotNull(value);
        Assert.assertEquals("newValueAlpha", value);

        // check that we can update an existing property on an entity
        entityBroker.setPropertyValue(TestData.REF5, TestData.PROPERTY_NAME5A, "AZnewValue");
        value = entityBroker.getPropertyValue(TestData.REF5, TestData.PROPERTY_NAME5A);
        Assert.assertNotNull(value);
        Assert.assertNotSame(TestData.PROPERTY_VALUE5A, value);
        Assert.assertEquals("AZnewValue", value);

        // check that we can remove a property on an entity
        entityBroker.setPropertyValue(TestData.REF5, "newNameAlpha", null);
        value = entityBroker.getPropertyValue(TestData.REF5, "newNameAlpha");
        Assert.assertNull(value);

        // check that we can remove all properties on an entity
        Map<String, String> m = entityBroker.getProperties(TestData.REF5);
        Assert.assertEquals(2, m.size());
        entityBroker.setPropertyValue(TestData.REF5, null, null);
        m = entityBroker.getProperties(TestData.REF5);
        Assert.assertEquals(0, m.size());

        // make sure invalid ref causes Assert.failure
        try {
            entityBroker.setPropertyValue(TestData.INVALID_REF, "newNameAlpha", "newValueAlpha");
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e.getMessage());
        }

        // make sure invalid params cause Assert.failure
        try {
            entityBroker.setPropertyValue(TestData.REF1, null, "XXXXXXXXX");
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e.getMessage());
        }

    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#getTags(java.lang.String)}.
     */
    @Test
    public void testGetTags() {
        Set<String> tags = null;

        tags = entityBroker.getTags(TestData.REF1);
        Assert.assertNotNull(tags);
        Assert.assertEquals(2, tags.size());
        Assert.assertTrue(tags.contains("test"));
        Assert.assertTrue(tags.contains("aaronz"));

        tags = entityBroker.getTags(TestData.REF1_1);
        Assert.assertNotNull(tags);
        Assert.assertEquals(0, tags.size());
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#setTags(java.lang.String, java.util.Set)}.
     */
    @Test
    public void testSetTags() {
        // test adding new tags
        entityBroker.setTags(TestData.REF1_1, new String[] {"test"});
        Assert.assertEquals(1, entityBroker.getTags(TestData.REF1_1).size() );

        // test clearing tags
        entityBroker.setTags(TestData.REF1, new String[] {});
        Assert.assertEquals(0, entityBroker.getTags(TestData.REF1).size() );
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#findEntityRefsByTags(java.lang.String[])}.
     */
    @Test
    public void testFindEntityRefsByTags() {
        List<String> refs = null;

        refs = entityBroker.findEntityRefsByTags( new String[] {"aaronz"} );
        Assert.assertNotNull(refs);
        Assert.assertEquals(1, refs.size());
        Assert.assertEquals(TestData.REF1, refs.get(0));

        refs = entityBroker.findEntityRefsByTags( new String[] {"test"} );
        Assert.assertNotNull(refs);
        Assert.assertEquals(2, refs.size());
        Assert.assertEquals(TestData.REFT1, refs.get(0));
        Assert.assertEquals(TestData.REF1, refs.get(1));

        entityBroker.setTags(TestData.REF1_1, new String[] {"test"});

        refs = entityBroker.findEntityRefsByTags( new String[] {"test"} );
        Assert.assertNotNull(refs);
        Assert.assertEquals(3, refs.size());
    }
    
}
