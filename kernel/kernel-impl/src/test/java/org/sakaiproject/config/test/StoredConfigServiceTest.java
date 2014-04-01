/******************************************************************************
 * $URL: $
 * $Id: $
 ******************************************************************************
 *
 * Copyright (c) 2003-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *****************************************************************************/

package org.sakaiproject.config.test;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.api.ServerConfigurationService.ConfigItem;
import org.sakaiproject.component.impl.BasicConfigurationService;
import org.sakaiproject.component.impl.ConfigItemImpl;
import org.sakaiproject.config.api.HibernateConfigItem;
import org.sakaiproject.config.impl.StoredConfigService;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.util.BasicConfigItem;

import java.util.Arrays;
import java.util.List;

/**
 * StoredConfigServiceTest
 * KNL-1063
 *
 * @author Earle Nietzel
 *         Created on April 18, 2013
 */
public class StoredConfigServiceTest extends SakaiKernelTestBase {
    private static final Log LOG = LogFactory.getLog(StoredConfigServiceTest.class);

    private static BasicConfigurationService basicConfigurationService;
    private static StoredConfigService storedConfigService;
    private static final String SOURCE = "TEST";

    public static Test suite() {
        TestSetup setup = new TestSetup(new TestSuite(StoredConfigServiceTest.class)) {
            protected void setUp() throws Exception {
                LOG.debug("starting oneTimeSetup");
                oneTimeSetup("storedconfigservice");
                oneTimeSetupAfter();

                LOG.debug("finished oneTimeSetup");
            }

            protected void tearDown() throws Exception {
                LOG.debug("starting tearDown");
                oneTimeTearDown();
                LOG.debug("finished tearDown");
            }
        };
        return setup;
    }

    private static void oneTimeSetupAfter() {
        basicConfigurationService = (BasicConfigurationService) getService(ServerConfigurationService.class.getName());
        storedConfigService = (StoredConfigService) getService(StoredConfigService.class.getName());

        basicConfigurationService.registerConfigItem(new ConfigItemImpl("test1", "test1", SOURCE));
        basicConfigurationService.registerConfigItem(new ConfigItemImpl("test2", "test2", ServerConfigurationService.TYPE_STRING, "this is a test", SOURCE, "test2", 0, 0, null, true, true, true, true));
        basicConfigurationService.registerConfigItem(BasicConfigItem.makeConfigItem("test3", "test3", SOURCE));
        basicConfigurationService.registerConfigItem(BasicConfigItem.makeConfigItem("test4", "test4", SOURCE, true));
    }

    public void testStoredConfigService() {
        LOG.info("Total # of Config Items is " + storedConfigService.countAll());

        // check that test[1-4] has been added
        assertEquals(1, storedConfigService.countByName("test1"));
        assertEquals(1, storedConfigService.countByName("test2"));
        assertEquals(1, storedConfigService.countByName("test3"));
        assertEquals(1, storedConfigService.countByName("test4"));

        // modify test1 for time test
        basicConfigurationService.registerConfigItem(new ConfigItemImpl("test1", "test1-updated", SOURCE));
        HibernateConfigItem test1 = storedConfigService.findByName("test1");
        LOG.info(test1.toString());
        assertEquals("test1-updated", test1.getValue());
        assertFalse(test1.getCreated().equals(test1.getModified()));

        // countAll == findAll.size()
        assertEquals(storedConfigService.countAll(), storedConfigService.findAll().size());

        // create a type string ConfigItem that is secured, dynamic, defaulted, and registered
        ConfigItem test5CI = new ConfigItemImpl("test5", "test5", ServerConfigurationService.TYPE_STRING, "this is a test", SOURCE, "test5", 0, 0, null, true, true, true, true);
        persistAndRetrieve(test5CI);

        // test5 is defaulted
        List<HibernateConfigItem> defaultedItems = storedConfigService.findDefaulted();
        assertTrue(isConfigItemInList(defaultedItems, "test5"));

        // test5 is registered
        List<HibernateConfigItem> registeredItems = storedConfigService.findRegistered();
        assertTrue(isConfigItemInList(registeredItems, "test5"));

        // test5 is dynamic
        List<HibernateConfigItem> dynamicItems = storedConfigService.findDynamic();
        assertTrue(isConfigItemInList(dynamicItems, "test5"));

        // test5 is secured
        List<HibernateConfigItem> securedItems = storedConfigService.findSecured();
        assertTrue(isConfigItemInList(securedItems, "test5"));

        LOG.info("Number of secured: " + securedItems.size() + ", defaulted: " + defaultedItems.size() + ", registered: " + registeredItems.size() + ", dynamic: " + dynamicItems.size());

        // test type boolean
        ConfigItem test6CI = new ConfigItemImpl("test6", Boolean.TRUE, ServerConfigurationService.TYPE_BOOLEAN, SOURCE);
        persistAndRetrieve(test6CI);

        // test type Integer
        ConfigItem test7CI = new ConfigItemImpl("test7", 999, ServerConfigurationService.TYPE_INT, SOURCE);
        persistAndRetrieve(test7CI);

        // test type array
        ConfigItem test8CI = new ConfigItemImpl("test8", new String[]{"test8.1=one", "test8.2=two", "test8.3=three"}, ServerConfigurationService.TYPE_ARRAY, SOURCE);
        persistAndRetrieve(test8CI);

        // test type UNKNOWN should not be saved
        ConfigItem test9CI = new ConfigItemImpl("test9", "test9", "UNKNOWN", SOURCE);
        storedConfigService.saveOrUpdate(storedConfigService.createHibernateConfigItem(test9CI));
        assertNull(storedConfigService.findByName(test9CI.getName()));

        // findRegistered.size == getConfigItems.size
        assertEquals(storedConfigService.findRegistered().size(), storedConfigService.getConfigItems().size());
    }

    private boolean isConfigItemInList(List<HibernateConfigItem> list, String name) {
        for (HibernateConfigItem item : list) {
            if (item.getName().equals(name)) {
                LOG.info(item.toString());
                return true;
            }
        }
        return false;
    }

    private void persistAndRetrieve(ConfigItem item) {

        LOG.info("persistAndRetrieve: " + item.toString());
        HibernateConfigItem origItem = storedConfigService.createHibernateConfigItem(item);

        storedConfigService.saveOrUpdate(origItem);

        HibernateConfigItem savedItem = storedConfigService.findByName(origItem.getName());

        assertTrue(origItem.equals(savedItem));

        ConfigItem persistedItem = storedConfigService.createConfigItem(savedItem);

        assertEquals(item, persistedItem);

        // check the values are still the same
        if (origItem.getType().equals(ServerConfigurationService.TYPE_ARRAY)) {
            assertTrue(Arrays.deepEquals((String[]) item.getValue(), (String[]) persistedItem.getValue()));
        } else {
            assertTrue(item.getValue().equals(persistedItem.getValue()));
        }
    }
}

