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

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.impl.EntityMetaPropertiesService;
import org.sakaiproject.entitybroker.impl.EntityTaggingService;
import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.sakaiproject.entitybroker.test.data.TestDataPreload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ContextConfiguration(classes = {EntityBrokerTestConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class EntityMetaPropertiesServiceTest {

    @Autowired private EntityBroker entityBroker;
    @Autowired private EntityMetaPropertiesService propertiesService;
    @Autowired private EntityProviderManager entityProviderManager;
    @Autowired private EntityMetaPropertiesService entityMetaPropertiesService;
    @Autowired private EntityTaggingService entityTaggingService;
    @Autowired private ServerConfigurationService serverConfigurationService;

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

        Assert.assertNotNull(tdp.prop1);
        Assert.assertNotNull(tdp.prop1B);
        Assert.assertNotNull(tdp.prop1C);
        Assert.assertEquals(tdp.prop1.getPropertyValue(), entityBroker.getPropertyValue(tdp.prop1.getEntityRef(), tdp.prop1.getPropertyName()));
        Assert.assertEquals(tdp.prop1B.getPropertyValue(), entityBroker.getPropertyValue(tdp.prop1B.getEntityRef(), tdp.prop1B.getPropertyName()));
        Assert.assertEquals(tdp.prop1C.getPropertyValue(), entityBroker.getPropertyValue(tdp.prop1C.getEntityRef(), tdp.prop1C.getPropertyName()));
    }


    @Test
    public void testFindEntityRefs() {
        // test search with limit by prefix
        List<String> l = propertiesService.findEntityRefs(new String[]{TestData.PREFIX5}, null, null, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());
        Assert.assertTrue(l.contains(TestData.REF5));
        Assert.assertTrue(l.contains(TestData.REF5_2));

        // test search with limit by prefix (check that no results ok)
        l = propertiesService.findEntityRefs(new String[]{TestData.INVALID_REF}, null, null, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

        // test searching with multiple prefixes
        l = propertiesService.findEntityRefs(new String[]{TestData.PREFIX5, TestData.PREFIX1}, null,
                null, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());
        Assert.assertTrue(l.contains(TestData.REF5));
        Assert.assertTrue(l.contains(TestData.REF5_2));

        // test searching by names
        l = propertiesService.findEntityRefs(new String[]{TestData.PREFIX5},
                new String[]{TestData.PROPERTY_NAME5A}, null, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        Assert.assertTrue(l.contains(TestData.REF5));

        // test searching by invalid name (return no results)
        l = propertiesService.findEntityRefs(new String[]{TestData.PREFIX5},
                new String[]{TestData.INVALID_REF}, null, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

        // test searching with multiple names
        l = propertiesService.findEntityRefs(new String[]{TestData.PREFIX5}, new String[]{
                TestData.PROPERTY_NAME5A, TestData.PROPERTY_NAME5C}, null, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());
        Assert.assertTrue(l.contains(TestData.REF5));
        Assert.assertTrue(l.contains(TestData.REF5_2));

        // test search limit by values (long exact match)
        l = propertiesService.findEntityRefs(new String[]{TestData.PREFIX5},
                new String[]{TestData.PROPERTY_NAME5C}, new String[]{TestData.PROPERTY_VALUE5C},
                true);
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        Assert.assertTrue(l.contains(TestData.REF5_2));

        // test search limit by values (exact match)
        l = propertiesService.findEntityRefs(new String[]{TestData.PREFIX5},
                new String[]{TestData.PROPERTY_NAME5B}, new String[]{TestData.PROPERTY_VALUE5B},
                true);
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        Assert.assertTrue(l.contains(TestData.REF5));

        // cannot have empty or null prefix
        Assert.assertThrows(IllegalArgumentException.class,
            () -> propertiesService.findEntityRefs(new String[]{},
                    new String[]{TestData.PROPERTY_NAME5A}, null, true));

        Assert.assertThrows(IllegalArgumentException.class,
            () -> propertiesService.findEntityRefs(null, new String[]{TestData.PROPERTY_NAME5A}, null, true));

        // test search limit by values cannot have name null or empty
        Assert.assertThrows(IllegalArgumentException.class,
            () -> propertiesService.findEntityRefs(new String[]{TestData.PREFIX5}, new String[]{},
                    new String[]{TestData.PROPERTY_VALUE5A}, true));

        // test name and values arrays must match sizes
        Assert.assertThrows(IllegalArgumentException.class,
            () -> propertiesService.findEntityRefs(new String[]{TestData.PREFIX5}, new String[]{
                            TestData.PROPERTY_NAME5A, TestData.PROPERTY_NAME5B},
                    new String[]{TestData.PROPERTY_VALUE5A}, true));

        // test search with all empty fields fail
        Assert.assertThrows(IllegalArgumentException.class,
            () -> propertiesService.findEntityRefs(new String[]{}, new String[]{}, new String[]{}, false));
    }

    @Test
    public void testGetProperties() {
        Map<String, String> m = propertiesService.getProperties(TestData.REF5);
        Assert.assertNotNull(m);
        Assert.assertEquals(2, m.size());
        Assert.assertTrue(m.containsKey(TestData.PROPERTY_NAME5A));
        Assert.assertTrue(m.containsKey(TestData.PROPERTY_NAME5B));

        m = propertiesService.getProperties(TestData.REF5_2);
        Assert.assertNotNull(m);
        Assert.assertEquals(1, m.size());
        Assert.assertTrue(m.containsKey(TestData.PROPERTY_NAME5C));

        // ref with no properties should fetch none
        m = propertiesService.getProperties(TestData.REF1);
        Assert.assertNotNull(m);
        Assert.assertTrue(m.isEmpty());

        // make sure invalid ref causes failure
        Assert.assertThrows(IllegalArgumentException.class, () -> propertiesService.getProperties(TestData.INVALID_REF));
    }

    @Test
    public void testGetPropertyValue() {
        String value = propertiesService.getPropertyValue(TestData.REF5, TestData.PROPERTY_NAME5A);
        Assert.assertNotNull(value);
        Assert.assertEquals(TestData.PROPERTY_VALUE5A, value);

        value = propertiesService.getPropertyValue(TestData.REF5, TestData.PROPERTY_NAME5B);
        Assert.assertNotNull(value);
        Assert.assertEquals(TestData.PROPERTY_VALUE5B, value);

        // test large value retrieval
        value = propertiesService.getPropertyValue(TestData.REF5_2, TestData.PROPERTY_NAME5C);
        Assert.assertNotNull(value);
        Assert.assertEquals(TestData.PROPERTY_VALUE5C, value);

        // nonexistent value property get retrieves null
        Assert.assertNull(propertiesService.getPropertyValue(TestData.REF5, "XXXXXXXXXXXX"));

        // make sure invalid ref causes failure
        Assert.assertThrows(IllegalArgumentException.class,
            () -> propertiesService.getPropertyValue(TestData.INVALID_REF, TestData.PROPERTY_NAME5A));

        // null name causes failure
        Assert.assertThrows(IllegalArgumentException.class,
            () -> propertiesService.getPropertyValue(TestData.REF5, null));

        // empty name causes failure
        Assert.assertThrows(IllegalArgumentException.class,
            () -> propertiesService.getPropertyValue(TestData.REF5, ""));
    }

    @Test
    public void testSetPropertyValue() {
        // check that we can save a new property on an entity
        propertiesService.setPropertyValue(TestData.REF5, "newNameAlpha", "newValueAlpha");
        String value = propertiesService.getPropertyValue(TestData.REF5, "newNameAlpha");
        Assert.assertNotNull(value);
        Assert.assertEquals("newValueAlpha", value);

        // check that we can update an existing property on an entity
        propertiesService.setPropertyValue(TestData.REF5, TestData.PROPERTY_NAME5A, "AZnewValue");
        value = propertiesService.getPropertyValue(TestData.REF5, TestData.PROPERTY_NAME5A);
        Assert.assertNotNull(value);
        Assert.assertNotSame(TestData.PROPERTY_VALUE5A, value);
        Assert.assertEquals("AZnewValue", value);

        // check that we can remove a property on an entity
        propertiesService.setPropertyValue(TestData.REF5, "newNameAlpha", null);
        Assert.assertNull(propertiesService.getPropertyValue(TestData.REF5, "newNameAlpha"));

        // check that we can remove all properties on an entity
        Map<String, String> m = propertiesService.getProperties(TestData.REF5);
        Assert.assertEquals(2, m.size());
        propertiesService.setPropertyValue(TestData.REF5, null, null);
        m = propertiesService.getProperties(TestData.REF5);
        Assert.assertEquals(0, m.size());

        // make sure invalid ref causes failure
        Assert.assertThrows(IllegalArgumentException.class,
            () -> propertiesService.setPropertyValue(TestData.INVALID_REF, "newNameAlpha", "newValueAlpha"));

        // make sure invalid params cause failure
        Assert.assertThrows(IllegalArgumentException.class,
            () -> propertiesService.setPropertyValue(TestData.REF1, null, "XXXXXXXXX"));
    }

}
