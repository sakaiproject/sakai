/**
 * $Id$
 * $URL$
 * EntityHandlerImplTest.java - entity-broker - Apr 6, 2008 12:08:39 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.entitybroker.test;

import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entitybroker.EntityBrokerManager;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.mocks.data.MyEntity;
import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ContextConfiguration(classes = {EntityBrokerTestConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class EntityBrokerManagerTest {

    @Autowired private EntityBrokerManager entityBrokerManager;
    @Autowired private EntityProviderManager entityProviderManager;
    @Autowired private ServerConfigurationService serverConfigurationService;

    private TestData td; // loads sample providers

    @Before
    public void onSetUp() throws Exception {
        td = new TestData(entityProviderManager);
        Mockito.when(serverConfigurationService.getServerUrl()).thenReturn("http://localhost:8080");
    }

    @After
    public void tearDown() {
        entityProviderManager.unRegistrarAllProvidersAndListeners();
        td = null;
    }

    @Test
    public void testEntityExists() {
        Assert.assertTrue(entityBrokerManager.entityExists(new EntityReference(TestData.REF1)));
        Assert.assertTrue(entityBrokerManager.entityExists(new EntityReference(TestData.REF1_1)));
        Assert.assertTrue(entityBrokerManager.entityExists(new EntityReference(TestData.REF2)));

        // test that invalid id with valid prefix does not pass
        Assert.assertFalse(entityBrokerManager.entityExists(new EntityReference(TestData.REF1_INVALID)));

        // test that unregistered ref does not pass
        Assert.assertFalse(entityBrokerManager.entityExists(new EntityReference(TestData.REF9)));
    }

    @Test
    public void testGetEntityURL() {
        Assert.assertEquals(TestData.URL1, entityBrokerManager.getEntityURL(TestData.REF1, null, null));
        Assert.assertEquals(TestData.URL2, entityBrokerManager.getEntityURL(TestData.REF2, null, null));
        Assert.assertEquals(TestData.REF1_INVALID_URL, entityBrokerManager.getEntityURL(TestData.REF1_INVALID, null, null));
        Assert.assertThrows(IllegalArgumentException.class, () -> entityBrokerManager.getEntityURL(TestData.INVALID_REF, null, null));
    }

    @Test
    public void testParseReference() {
        EntityReference er = entityBrokerManager.parseReference(TestData.REF1);
        Assert.assertNotNull(er);
        Assert.assertEquals(TestData.PREFIX1, er.getPrefix());
        Assert.assertEquals(TestData.IDS1[0], er.getId());

        er = entityBrokerManager.parseReference(TestData.REF2);
        Assert.assertNotNull(er);
        Assert.assertEquals(TestData.PREFIX2, er.getPrefix());

        // test parsing a defined reference
        er = entityBrokerManager.parseReference(TestData.REF3A);
        Assert.assertNotNull(er);
        Assert.assertEquals(TestData.PREFIX3, er.getPrefix());

        // parsing of unregistered entity references returns null
        Assert.assertNull(entityBrokerManager.parseReference(TestData.REF9));

        // parsing with nonexistent prefix returns null
        Assert.assertNull(entityBrokerManager.parseReference("/totallyfake/notreal"));

        // TODO test handling custom ref objects

        Assert.assertThrows(IllegalArgumentException.class, () -> entityBrokerManager.parseReference(TestData.INVALID_REF));
    }

    @Test
    public void testParseEntityURL() {
        EntityView view = entityBrokerManager.parseEntityURL(TestData.INPUT_URL1);
        Assert.assertNotNull(view);
        Assert.assertEquals(EntityView.VIEW_SHOW, view.getViewKey());
        Assert.assertEquals(TestData.PREFIX1, view.getEntityReference().getPrefix());
        Assert.assertEquals(TestData.IDS1[0], view.getEntityReference().getId());

        // also test some other URLs
        Assert.assertThrows(IllegalArgumentException.class,
            () -> entityBrokerManager.parseEntityURL("/" + TestData.PREFIX1 + "/az.xml?fname=Aaron&lname=Zeckoski"));

        // parsing of URL related to unregistered entity references returns null
        Assert.assertNull(entityBrokerManager.parseEntityURL(TestData.REF9));

        // TODO test custom parse rules

        Assert.assertThrows(IllegalArgumentException.class,
            () -> entityBrokerManager.parseEntityURL(TestData.INVALID_URL));
    }

    @Test
    public void testGetEntityObject() {
        // first for resolveable
        EntityReference ref = entityBrokerManager.parseReference(TestData.REF4);
        Assert.assertNotNull(ref);
        Object entity = entityBrokerManager.fetchEntityObject(ref);
        Assert.assertNotNull(entity);
        Assert.assertEquals(MyEntity.class, entity.getClass());
        Assert.assertEquals(TestData.entity4, entity);

        ref = entityBrokerManager.parseReference(TestData.REF4_two);
        Assert.assertNotNull(ref);
        entity = entityBrokerManager.fetchEntityObject(ref);
        Assert.assertNotNull(entity);
        Assert.assertEquals(MyEntity.class, entity.getClass());
        Assert.assertEquals(TestData.entity4_two, entity);

        // now for non-resolveable
        ref = entityBrokerManager.parseReference(TestData.REF5);
        Assert.assertNotNull(ref);
        Assert.assertNull(entityBrokerManager.fetchEntityObject(ref));
    }

    @Test
    public void testMakeFullURL() {

        String full = entityBrokerManager.makeFullURL(TestData.REF1);
        Assert.assertNotNull(full);
        Assert.assertEquals("http://localhost:8080" + EntityView.DIRECT_PREFIX + TestData.REF1, full);
    }

    @Test
    public void testMakeEntityView() {
        EntityView ev = entityBrokerManager.makeEntityView(new EntityReference("azprefix", "azid"), EntityView.VIEW_SHOW, Formats.XML);
        Assert.assertNotNull(ev);
        Assert.assertEquals("azprefix", ev.getEntityReference().getPrefix());
        Assert.assertEquals("azid", ev.getEntityReference().getId());
        Assert.assertEquals(EntityView.VIEW_SHOW, ev.getViewKey());
        Assert.assertEquals(Formats.XML, ev.getExtension());

        ev = entityBrokerManager.makeEntityView(new EntityReference("azprefix", "azid"), null, null);
        Assert.assertNotNull(ev);
        Assert.assertEquals("azprefix", ev.getEntityReference().getPrefix());
        Assert.assertEquals("azid", ev.getEntityReference().getId());
        Assert.assertEquals(EntityView.VIEW_SHOW, ev.getViewKey());
        Assert.assertNull(ev.getExtension());

        Assert.assertThrows(IllegalArgumentException.class, () -> entityBrokerManager.makeEntityView(null, null, null));
    }

    @Test
    public void testFetchEntity() {
        EntityReference ref = new EntityReference(TestData.REF4);
        Object entity = entityBrokerManager.fetchEntity(ref);
        Assert.assertNotNull(entity);
        Assert.assertEquals(TestData.entity4, entity);

        ref = new EntityReference(TestData.REF1);
        Assert.assertNull(entityBrokerManager.fetchEntity(ref));

        Assert.assertThrows(IllegalArgumentException.class, () -> entityBrokerManager.fetchEntity(null));
    }

    @Test
    public void testGetEntityData() {
        EntityReference ref = new EntityReference(TestData.REF4);
        EntityData entity = entityBrokerManager.getEntityData(ref);
        Assert.assertNotNull(entity);
        Assert.assertEquals(TestData.REF4, entity.getEntityReference());
        Assert.assertEquals(TestData.entity4, entity.getData());
        Assert.assertNotNull(entity.getDisplayTitle());
        Assert.assertNotNull(entity.getEntityURL());

        ref = new EntityReference(TestData.REF1);
        entity = entityBrokerManager.getEntityData(ref);
        Assert.assertNotNull(entity);
        Assert.assertEquals(TestData.REF1, entity.getEntityReference());
        Assert.assertNull(entity.getData());
        Assert.assertNotNull(entity.getDisplayTitle());
        Assert.assertNotNull(entity.getEntityURL());

        Assert.assertThrows(IllegalArgumentException.class, () -> entityBrokerManager.getEntityData(null));
    }

    @Test
    public void testFetchEntities() {
        EntityReference ref = new EntityReference(TestData.SPACE4);
        List<?> l = entityBrokerManager.fetchEntities(ref, null, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());
        Assert.assertEquals(MyEntity.class, l.get(0).getClass());

        ref = new EntityReference(TestData.SPACE4);
        l = entityBrokerManager.fetchEntities(ref, new Search(), new HashMap<>());
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());
        Assert.assertEquals(MyEntity.class, l.get(0).getClass());
        Assert.assertEquals(TestData.entity4, l.get(0));

        ref = new EntityReference(TestData.REF4);
        l = entityBrokerManager.fetchEntities(ref, null, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        Assert.assertEquals(MyEntity.class, l.get(0).getClass());
        Assert.assertEquals(TestData.entity4, l.get(0));

        ref = new EntityReference("/" + TestData.PREFIX1);
        l = entityBrokerManager.fetchEntities(ref, null, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

        Assert.assertThrows(IllegalArgumentException.class, () -> entityBrokerManager.fetchEntities(null, null, null));
    }

    @Test
    public void testGetEntitiesData() {
        EntityReference ref = new EntityReference(TestData.SPACE4);
        List<EntityData> data = entityBrokerManager.getEntitiesData(ref, new Search(), new HashMap<>());
        Assert.assertNotNull(data);
        Assert.assertEquals(3, data.size());
        Assert.assertEquals(EntityData.class, data.get(0).getClass());
        Assert.assertEquals(TestData.entity4, data.get(0).getData());

        ref = new EntityReference(TestData.SPACE4);
        data = entityBrokerManager.getEntitiesData(ref, null, null);
        Assert.assertNotNull(data);
        Assert.assertEquals(3, data.size());
        Assert.assertEquals(EntityData.class, data.get(0).getClass());
        Assert.assertEquals(TestData.entity4, data.get(0).getData());

        ref = new EntityReference("/" + TestData.PREFIX1);
        data = entityBrokerManager.getEntitiesData(ref, null, null);
        Assert.assertNotNull(data);
        Assert.assertEquals(0, data.size());

        Assert.assertThrows(IllegalArgumentException.class, () -> entityBrokerManager.getEntitiesData(null, null, null));
    }

    @Test
    public void testBrowseEntities() {
        List<EntityData> data = entityBrokerManager.browseEntities(TestData.PREFIX4, null, null, null, null, null);
        Assert.assertNotNull(data);
        Assert.assertEquals(0, data.size());

        data = entityBrokerManager.browseEntities(TestData.PREFIXB1, null, null, null, null, null);
        Assert.assertNotNull(data);
        Assert.assertEquals(3, data.size());
        Assert.assertEquals(EntityData.class, data.get(0).getClass());

        data = entityBrokerManager.browseEntities(TestData.PREFIXB2, null, null, null, null, null);
        Assert.assertNotNull(data);
        Assert.assertEquals(3, data.size());
        Assert.assertEquals(EntityData.class, data.get(0).getClass());

        data = entityBrokerManager.browseEntities(TestData.PREFIXB2, null, "/user/aaronz", null, null, null);
        Assert.assertNotNull(data);
        Assert.assertEquals(1, data.size());
        Assert.assertEquals(EntityData.class, data.get(0).getClass());

        data = entityBrokerManager.browseEntities(TestData.PREFIXB2, null, "/user/aaronz", "/site/siteAZ", null, null);
        Assert.assertNotNull(data);
        Assert.assertEquals(2, data.size());
        Assert.assertEquals(EntityData.class, data.get(0).getClass());

        Assert.assertThrows(IllegalArgumentException.class,
            () -> entityBrokerManager.browseEntities(null, null, null, null, null, null));
    }

    @Test
    public void testConvertToEntityDataListOfQEntityReference() {
        // TODO Assert.fail("Not yet implemented");
    }

    @Test
    public void testConvertToEntityDataObjectEntityReference() {
        // TODO Assert.fail("Not yet implemented");
    }

    @Test
    public void testPopulateEntityDataListOfEntityData() {
        // TODO Assert.fail("Not yet implemented");
    }

    @Test
    public void testPopulateEntityDataEntityDataArray() {
        // TODO Assert.fail("Not yet implemented");
    }

}
