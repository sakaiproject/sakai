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

package org.sakaiproject.entitybroker.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.impl.EntityMetaPropertiesService;
import org.sakaiproject.entitybroker.impl.EntityTaggingService;
import org.sakaiproject.entitybroker.test.data.TestDataPreload;
import org.sakaiproject.entitybroker.mocks.data.MyEntity;
import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ContextConfiguration(classes = {EntityBrokerTestConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class EntityBrokerTransactionalTest {

    @Autowired private EntityBroker entityBroker;
    @Autowired private EntityProviderManager entityProviderManager;
    @Autowired private EntityMetaPropertiesService entityMetaPropertiesService;
    @Autowired private ServerConfigurationService serverConfigurationService;
    @Autowired private EntityTaggingService entityTaggingService;

    private TestData td; // loads sample providers
    private TestDataPreload tdp; // loads sample data in db

    @Before
    public void onSetUp() throws Exception {
        td = new TestData(entityProviderManager);
        tdp = new TestDataPreload(entityMetaPropertiesService, entityTaggingService);
        Mockito.when(serverConfigurationService.getServerUrl()).thenReturn("http://localhost:8080");
    }

    @After
    public void tearDown() {
        entityProviderManager.unRegistrarAllProvidersAndListeners();
        td = null;
        tdp = null;
    }

    @Test
    public void testValidTestData() {
        // ensure the test data is setup the way we think
        Assert.assertNotNull(td);

        Assert.assertNotNull(tdp);
        Assert.assertNotNull(tdp.prop1);
        Assert.assertNotNull(tdp.prop1B);
        Assert.assertNotNull(tdp.prop1C);
        Assert.assertEquals(tdp.prop1.getPropertyValue(), entityBroker.getPropertyValue(tdp.prop1.getEntityRef(), tdp.prop1.getPropertyName()));
        Assert.assertEquals(tdp.prop1B.getPropertyValue(), entityBroker.getPropertyValue(tdp.prop1B.getEntityRef(), tdp.prop1B.getPropertyName()));
        Assert.assertEquals(tdp.prop1C.getPropertyValue(), entityBroker.getPropertyValue(tdp.prop1C.getEntityRef(), tdp.prop1C.getPropertyName()));
    }

    @Test
    public void testEntityExists() {
        Assert.assertTrue(entityBroker.entityExists(TestData.REF1));
        Assert.assertTrue(entityBroker.entityExists(TestData.REF1_1));
        Assert.assertTrue(entityBroker.entityExists(TestData.REF2));

        // test that invalid id with valid prefix does not pass
        Assert.assertFalse(entityBroker.entityExists(TestData.REF1_INVALID));

        // test that unregistered ref does not pass
        Assert.assertFalse(entityBroker.entityExists(TestData.REF9));

        // test that invalid ref causes exception
        Assert.assertThrows(IllegalArgumentException.class, () -> entityBroker.entityExists(TestData.INVALID_REF));
    }

    @Test
    public void testGetEntityURL() {
        Assert.assertEquals(TestData.URL1, entityBroker.getEntityURL(TestData.REF1));
        Assert.assertEquals(TestData.URL2, entityBroker.getEntityURL(TestData.REF2));
        Assert.assertEquals(TestData.REF1_INVALID_URL, entityBroker.getEntityURL(TestData.REF1_INVALID));
        Assert.assertThrows(IllegalArgumentException.class, () -> entityBroker.getEntityURL(TestData.INVALID_REF));
    }

    @Test
    public void testGetEntityURLStringStringString() {
        Assert.assertEquals(TestData.URL1, entityBroker.getEntityURL(TestData.REF1, null, null));
        Assert.assertEquals(TestData.URL2, entityBroker.getEntityURL(TestData.REF2, null, null));

        // test adding viewkey
        Assert.assertEquals(TestData.URL1, entityBroker.getEntityURL(TestData.REF1, EntityView.VIEW_SHOW, null));
        Assert.assertEquals(TestData.SPACE_URL1, entityBroker.getEntityURL(TestData.REF1, EntityView.VIEW_LIST, null));
        Assert.assertEquals(TestData.SPACE_URL1 + "/new", entityBroker.getEntityURL(TestData.REF1, EntityView.VIEW_NEW, null));
        Assert.assertEquals(TestData.URL1 + "/edit", entityBroker.getEntityURL(TestData.REF1, EntityView.VIEW_EDIT, null));
        Assert.assertEquals(TestData.URL1 + "/delete", entityBroker.getEntityURL(TestData.REF1, EntityView.VIEW_DELETE, null));

        // test extension
        Assert.assertEquals(TestData.URL1 + ".xml", entityBroker.getEntityURL(TestData.REF1, null, "xml"));
        Assert.assertEquals(TestData.URL2 + ".json", entityBroker.getEntityURL(TestData.REF2, null, "json"));

        // test both
        Assert.assertEquals(TestData.URL1 + "/edit.xml", entityBroker.getEntityURL(TestData.REF1, EntityView.VIEW_EDIT, "xml"));
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

    @Test
    public void testGetRegisteredPrefixes() {
        Set<String> s = entityBroker.getRegisteredPrefixes();
        Assert.assertNotNull(s);
        Assert.assertTrue(s.contains(TestData.PREFIX1));
        Assert.assertTrue(s.contains(TestData.PREFIX2));
        Assert.assertTrue(s.contains(TestData.PREFIX3));
        Assert.assertFalse(s.contains(TestData.PREFIX9));
    }

    @Test
    public void testFireEvent() {
        // we are mostly handing this to a mocked service so we can only check to see if errors occur
        entityBroker.fireEvent(TestData.EVENT1_NAME, TestData.REF1);
        entityBroker.fireEvent(TestData.EVENT1_NAME, TestData.REF2);
        entityBroker.fireEvent(TestData.EVENT2_NAME, TestData.REF1);
        entityBroker.fireEvent(TestData.EVENT2_NAME, "XXXXXXXXXXXXXX");

        // event with a null name should die
        Assert.assertThrows(IllegalArgumentException.class, () -> entityBroker.fireEvent(null, TestData.REF1));
        Assert.assertThrows(IllegalArgumentException.class, () -> entityBroker.fireEvent("", TestData.REF1));
    }

    @Test
    public void testParseReference() {
        EntityReference er = entityBroker.parseReference(TestData.REF1);
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
        Assert.assertNull(entityBroker.parseReference(TestData.REF9));

        // parsing with nonexistent prefix returns null
        Assert.assertNull(entityBroker.parseReference("/totallyfake/notreal"));

        Assert.assertThrows(IllegalArgumentException.class, () -> entityBroker.parseReference(TestData.INVALID_REF));
    }

    @Test
    public void testFetchEntity() {
        Object obj = entityBroker.fetchEntity(TestData.REF4);
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
        Assert.assertNull(entityBroker.fetchEntity(TestData.REF1));
        Assert.assertNull(entityBroker.fetchEntity(TestData.REF2));

        // use an unregistered provider to trigger the attempt to do a legacy lookup which will Assert.fail and return null
        Assert.assertNull(entityBroker.fetchEntity(TestData.REF9));

        // expect invalid reference to Assert.fail
        Assert.assertThrows(IllegalArgumentException.class, () -> entityBroker.fetchEntity(TestData.INVALID_REF));
        Assert.assertThrows(IllegalArgumentException.class, () -> entityBroker.fetchEntity(null));
    }

    @Test
    public void testFormatAndOutputEntity() {
        // test no provider
        String reference = TestData.REF4;
        OutputStream output = new ByteArrayOutputStream();
        String format = Formats.XML;

        Assert.assertThrows(UnsupportedOperationException.class,
            () -> entityBroker.formatAndOutputEntity(reference, format, null, output, null));
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
        Assert.assertThrows(UnsupportedOperationException.class,
            () -> entityBroker.translateInputToEntity(reference, format, input, null));
    }

    @Test
    public void testExecuteCustomAction() {
        // test no provider
        Assert.assertThrows(UnsupportedOperationException.class,
            () -> entityBroker.executeCustomAction(TestData.REFA1, "double", null, null));
    }

    @Test
    public void testFindEntityRefs() {
        // test search with limit by prefix
        List<String> l = entityBroker.findEntityRefs(new String[] { TestData.PREFIX5 }, null, null, true);
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
        Assert.assertThrows(IllegalArgumentException.class,
            () -> entityBroker.findEntityRefs(new String[] {},
                    new String[] { TestData.PROPERTY_NAME5A }, null, true));

        Assert.assertThrows(IllegalArgumentException.class,
            () -> entityBroker.findEntityRefs(null, new String[] { TestData.PROPERTY_NAME5A }, null, true));

        // test search limit by values cannot have name null or empty
        Assert.assertThrows(IllegalArgumentException.class,
            () -> entityBroker.findEntityRefs(new String[] { TestData.PREFIX5 }, new String[] {},
                    new String[] { TestData.PROPERTY_VALUE5A }, true));

        // test name and values arrays must match sizes
        Assert.assertThrows(IllegalArgumentException.class,
            () -> entityBroker.findEntityRefs(new String[] { TestData.PREFIX5 }, new String[] {
                    TestData.PROPERTY_NAME5A, TestData.PROPERTY_NAME5B },
                    new String[] { TestData.PROPERTY_VALUE5A }, true));

        // test search with all empty fields Assert.fail
        Assert.assertThrows(IllegalArgumentException.class,
            () -> entityBroker.findEntityRefs(new String[] {}, new String[] {}, new String[] {}, false));
    }

    @Test
    public void testGetProperties() {
        Map<String, String> m = entityBroker.getProperties(TestData.REF5);
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
        Assert.assertThrows(IllegalArgumentException.class, () -> entityBroker.getProperties(TestData.INVALID_REF));
    }

    @Test
    public void testGetPropertyValue() {
        String value = entityBroker.getPropertyValue(TestData.REF5, TestData.PROPERTY_NAME5A);
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
        Assert.assertNull(entityBroker.getPropertyValue(TestData.REF5, "XXXXXXXXXXXX"));

        // make sure invalid ref causes Assert.failure
        Assert.assertThrows(IllegalArgumentException.class,
            () -> entityBroker.getPropertyValue(TestData.INVALID_REF, TestData.PROPERTY_NAME5A));

        // null name causes Assert.failure
        Assert.assertThrows(IllegalArgumentException.class,
            () -> entityBroker.getPropertyValue(TestData.REF5, null));

        // empty name causes Assert.failure
        Assert.assertThrows(IllegalArgumentException.class,
            () -> entityBroker.getPropertyValue(TestData.REF5, ""));
    }

    @Test
    public void testSetPropertyValue() {
        // check that we can save a new property on an entity
        entityBroker.setPropertyValue(TestData.REF5, "newNameAlpha", "newValueAlpha");
        String value = entityBroker.getPropertyValue(TestData.REF5, "newNameAlpha");
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
        Assert.assertNull(entityBroker.getPropertyValue(TestData.REF5, "newNameAlpha"));

        // check that we can remove all properties on an entity
        Map<String, String> m = entityBroker.getProperties(TestData.REF5);
        Assert.assertEquals(2, m.size());
        entityBroker.setPropertyValue(TestData.REF5, null, null);
        m = entityBroker.getProperties(TestData.REF5);
        Assert.assertEquals(0, m.size());

        // make sure invalid ref causes Assert.failure
        Assert.assertThrows(IllegalArgumentException.class,
            () -> entityBroker.setPropertyValue(TestData.INVALID_REF, "newNameAlpha", "newValueAlpha"));

        // make sure invalid params cause Assert.failure
        Assert.assertThrows(IllegalArgumentException.class,
            () -> entityBroker.setPropertyValue(TestData.REF1, null, "XXXXXXXXX"));
    }


    @Test
    public void testFindEntityRefsByTags() {
        List<String> refs = entityBroker.findEntityRefsByTags( new String[] {"aaronz"} );
        Assert.assertNotNull(refs);
        Assert.assertEquals(1, refs.size());
        Assert.assertEquals(TestData.REF1, refs.get(0));

        refs = entityBroker.findEntityRefsByTags( new String[] {"test"} );
        Assert.assertNotNull(refs);
        Assert.assertEquals(2, refs.size());
        Assert.assertEquals(TestData.REFT1, refs.get(0));
        Assert.assertEquals(TestData.REF1, refs.get(1));
    }
    
}
