/**********************************************************************************
 *
 * Copyright (c) 2006, 2008, 2013 Sakai Foundation
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
 *
 **********************************************************************************/

package org.sakaiproject.messagebundle.impl.test;

import static org.junit.runners.MethodSorters.NAME_ASCENDING;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.hibernate.SessionFactory;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.messagebundle.api.MessageBundleProperty;
import org.sakaiproject.messagebundle.api.MessageBundleService;
import org.sakaiproject.messagebundle.impl.MessageBundleServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.util.Assert;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Sep 7, 2010
 * Time: 12:45:12 PM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-hibernate.xml"})
@FixMethodOrder(NAME_ASCENDING)
public class MessageBundleTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    private MessageBundleService messageBundleService;

    static ResourceBundle resourceBundleEN;
    static ResourceBundle resourceBundleFr;

    static Locale localeEn;
    static Locale localeFr;

    static String baseName;
    static String moduleName;

    @BeforeTransaction
    public void beforeTransaction()  {
        localeEn = new Locale("en");
        localeFr = new Locale("fr");

        baseName = "basename";
        moduleName = "modulename";

        Assert.notNull(messageBundleService);
        resourceBundleEN = ResourceBundle.getBundle("org/sakaiproject/messagebundle/impl/test/bundle", localeEn);
        resourceBundleFr = ResourceBundle.getBundle("org/sakaiproject/messagebundle/impl/test/bundle", localeFr);

        messageBundleService.saveOrUpdate(baseName, moduleName, resourceBundleEN, localeEn);
        messageBundleService.saveOrUpdate(baseName, moduleName, resourceBundleFr, localeFr);
    }

    public void testSearch(){
    }

    public void testGetMessageBundleProperty() {
    }

    @Test
    public void testUpdateMessageBundleProperty(){
        List<MessageBundleProperty> list = messageBundleService.getAllProperties(null, null);
        MessageBundleProperty prop = list.get(0);
        prop.setValue("newvalue");
        messageBundleService.updateMessageBundleProperty(prop);

        MessageBundleProperty loadedProp = messageBundleService.getMessageBundleProperty(prop.getId());
        Assert.isTrue("newvalue".equals(loadedProp.getValue()));
    }

    public void testGetModifiedProperties() {
    }

    public void testGetLocales(){
    }

    public void getModifiedPropertiesCount(){
    }

    @Test
    public void testGetAllProperties(){
        List<MessageBundleProperty> props = messageBundleService.getAllProperties(localeEn.toString(), moduleName);
        Assert.isTrue(4 == props.size());
        props = messageBundleService.getAllProperties(localeFr.toString(), moduleName);
        Assert.isTrue(4 == props.size());
    }

    public void testRevertAll(String locale){
    }

    public void testImportProperties(){
    }

    @Test
    public void testGetAllModuleNames(){
        List<String> moduleNames = messageBundleService.getAllModuleNames();
        Assert.notEmpty(moduleNames);
        Assert.isTrue(moduleNames.size() == 1);
        Assert.isTrue(moduleName.equals(moduleNames.get(0)));
    }

    @Test
    public void testGetAllBaseNames(){
        List<String> baseNames = messageBundleService.getAllBaseNames();
        Assert.notEmpty(baseNames);
        Assert.isTrue(baseNames.size() == 1);
        Assert.isTrue(baseName.equals(baseNames.get(0)));
    }

    @Test
    public void testRevert(){
        List<MessageBundleProperty> list = messageBundleService.getAllProperties(localeEn.toString(), moduleName);
        MessageBundleProperty prop = list.get(0);
        prop.setValue("newvalue");
        messageBundleService.updateMessageBundleProperty(prop);
        messageBundleService.revert(prop);

        MessageBundleProperty loadedProp = messageBundleService.getMessageBundleProperty(prop.getId());
        Assert.isNull(loadedProp.getValue());
    }

    public void testGetSearchCount(){
    }

    @Test
    public void testGetBundle(){
        // data gets loaded in setup(), this just validates the data is correct upon loading it

        Map<String, String> enLoadedData = messageBundleService.getBundle(baseName, moduleName, localeEn);
        Map<String, String> frLoadedData = messageBundleService.getBundle(baseName, moduleName, localeFr);

        for (Map.Entry<String, String> entry: enLoadedData.entrySet()) {
            int key = Integer.valueOf(entry.getKey());
            int value = Integer.valueOf(entry.getValue());
            // en values are equal
            Assert.isTrue(key == value);
        }
        for (Map.Entry<String, String> entry : frLoadedData.entrySet()) {
            int key = Integer.valueOf(entry.getKey());
            int value = Integer.valueOf(entry.getValue());
            // fr values value is 1 greater than key
            Assert.isTrue(key + 1 == value);
        }
    }

    @Test
    public void testGetBundleNotFound() {
        Map<String, String> map = messageBundleService.getBundle("asdf", "asdf", localeEn);
        Assert.notNull(map);
        Assert.isTrue(map.values().size() == 0);
    }
}
