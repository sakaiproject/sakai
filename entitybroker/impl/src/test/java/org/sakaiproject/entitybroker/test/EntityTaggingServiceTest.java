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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.impl.EntityMetaPropertiesService;
import org.sakaiproject.entitybroker.impl.EntityTaggingService;
import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.sakaiproject.entitybroker.test.data.TestDataPreload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import lombok.extern.slf4j.Slf4j;

/**
 * Testing the entitybroker implementation of the tagging service
 *
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
@Slf4j
@ContextConfiguration(classes = {EntityBrokerTestConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class EntityTaggingServiceTest {

    @Autowired private EntityTaggingService entityTaggingService;
    @Autowired private EntityProviderManager entityProviderManager;
    @Autowired private ServerConfigurationService serverConfigurationService;
    @Autowired private EntityMetaPropertiesService entityMetaPropertiesService;

    private TestData td; // loads sample providers
    private TestDataPreload tdp; // loads sample data in db

    @Before
    public void onSetUp() {
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
     * is overloaded, you should include the arguments in the test name like so: testMethodClassInt
     * (for method(Class, int);
     */

    @Test
    public void testValidTestData() {
        // ensure the test data is set up the way we think
        Assert.assertNotNull(td);
        Assert.assertNotNull(tdp);

        // Assert.assertEquals(new Long(1), tdp.pNode1.getId());
    }

    @Test
    public void testGetTags() {
        List<String> tags;

        tags = entityTaggingService.getTagsForEntity(TestData.REF1);
        Assert.assertNotNull(tags);
        Assert.assertEquals(2, tags.size());
        Assert.assertTrue(tags.contains("test"));
        Assert.assertTrue(tags.contains("aaronz"));

        tags = entityTaggingService.getTagsForEntity(TestData.REF1_1);
        Assert.assertNotNull(tags);
        Assert.assertEquals(0, tags.size());

        // check that we cannot get tags for those which do not support it
        Assert.assertThrows(UnsupportedOperationException.class, () -> entityTaggingService.getTagsForEntity(TestData.REF2));
    }

    @Test
    public void testSetTags() {
        // test adding new tags
        entityTaggingService.setTagsForEntity(TestData.REF1_1, new String[]{"test"});
        Assert.assertEquals(1, entityTaggingService.getTagsForEntity(TestData.REF1_1).size());

        // test clearing tags
        entityTaggingService.setTagsForEntity(TestData.REF1, new String[]{});
        Assert.assertEquals(0, entityTaggingService.getTagsForEntity(TestData.REF1).size());

        // test cannot add tags to refs that do not support it
        Assert.assertThrows(UnsupportedOperationException.class, () -> entityTaggingService.setTagsForEntity(TestData.REF2, new String[]{"test"}));
    }

    @Test
    public void testFindEntityRefsByTags() {
        List<EntityData> refs;

        refs = entityTaggingService.findEntitesByTags(new String[]{"aaronz"}, null, false, null);
        Assert.assertNotNull(refs);
        Assert.assertEquals(1, refs.size());
        Assert.assertEquals(TestData.REF1, refs.get(0).getEntityReference());

        refs = entityTaggingService.findEntitesByTags(new String[]{"AZ"}, null, false, null);
        Assert.assertNotNull(refs);
        Assert.assertEquals(2, refs.size());
        Assert.assertEquals(TestData.REFT1, refs.get(0).getEntityReference());
        Assert.assertEquals(TestData.REFT1_2, refs.get(1).getEntityReference());

        refs = entityTaggingService.findEntitesByTags(new String[]{"test"}, null, false, null);
        Assert.assertNotNull(refs);
        Assert.assertEquals(2, refs.size());
        Assert.assertEquals(TestData.REFT1, refs.get(0).getEntityReference());
        Assert.assertEquals(TestData.REF1, refs.get(1).getEntityReference());

        entityTaggingService.setTagsForEntity(TestData.REF1_1, new String[]{"test"});

        refs = entityTaggingService.findEntitesByTags(new String[]{"test"}, null, false, null);
        Assert.assertNotNull(refs);
        Assert.assertEquals(3, refs.size());
        Assert.assertEquals(TestData.REFT1, refs.get(0).getEntityReference());
        Assert.assertEquals(TestData.REF1, refs.get(1).getEntityReference());
        Assert.assertEquals(TestData.REF1_1, refs.get(2).getEntityReference());

        refs = entityTaggingService.findEntitesByTags(new String[]{"test"}, new String[]{TestData.PREFIX1}, false, null);
        Assert.assertNotNull(refs);
        Assert.assertEquals(2, refs.size());
        Assert.assertEquals(TestData.REF1, refs.get(0).getEntityReference());
        Assert.assertEquals(TestData.REF1_1, refs.get(1).getEntityReference());

        refs = entityTaggingService.findEntitesByTags(new String[]{"test"}, new String[]{TestData.PREFIXT1}, false, null);
        Assert.assertNotNull(refs);
        Assert.assertEquals(1, refs.size());
        Assert.assertEquals(TestData.REFT1, refs.get(0).getEntityReference());

        refs = entityTaggingService.findEntitesByTags(new String[]{"test", "AZ"}, null, false, null);
        Assert.assertNotNull(refs);
        Assert.assertEquals(4, refs.size());
        Assert.assertEquals(TestData.REFT1, refs.get(0).getEntityReference());
        Assert.assertEquals(TestData.REFT1_2, refs.get(1).getEntityReference());
        Assert.assertEquals(TestData.REF1, refs.get(2).getEntityReference());
        Assert.assertEquals(TestData.REF1_1, refs.get(3).getEntityReference());

        refs = entityTaggingService.findEntitesByTags(new String[]{"test", "AZ"}, null, true, null);
        Assert.assertNotNull(refs);
        Assert.assertEquals(1, refs.size());
        Assert.assertEquals(TestData.REFT1, refs.get(0).getEntityReference());

        refs = entityTaggingService.findEntitesByTags(new String[]{"ZZZZZZZZZ"}, null, false, null);
        Assert.assertNotNull(refs);
        Assert.assertEquals(0, refs.size());
    }

}
