package org.sakaiproject.messagebundle.impl.test;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import junit.extensions.TestSetup;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.messagebundle.api.MessageBundleProperty;
import org.sakaiproject.messagebundle.api.MessageBundleService;
import org.sakaiproject.test.SakaiKernelTestBase;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Sep 7, 2010
 * Time: 12:45:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class MessageBundleTest extends SakaiKernelTestBase {
	private static Log log = LogFactory.getLog(MessageBundleTest.class);

	private static ServerConfigurationService serverConfigurationService;
    private static MessageBundleService messageBundleService;

    private static String CONFIG = "../kernel-component/src/main/webapp/WEB-INF/components.xml";

    static ResourceBundle resourceBundleEN;
    static ResourceBundle resourceBundleFr;

    static Locale localeEn;
    static Locale localeFr;

    static String baseName;
    static String moduleName;


	public static Test suite() {
		TestSetup setup = new TestSetup(new TestSuite(MessageBundleTest.class)) {
			protected void setUp() throws Exception {
				try {
					oneTimeSetup("messagebundle", CONFIG);
                    oneTimeSetupAfter();
				} catch (Exception e) {
					log.warn(e);
				}
			}
			protected void tearDown() throws Exception {
				oneTimeTearDown();
			}
		};
		return setup;
	}


	static protected  void oneTimeSetupAfter() throws Exception {

         localeEn = new Locale("en");
         localeFr = new Locale("fr");

         baseName = "basename";
         moduleName = "modulename";

		serverConfigurationService = (ServerConfigurationService)getService(ServerConfigurationService.class.getName());
        messageBundleService = (MessageBundleService)getService(MessageBundleService.class.getName());
        Assert.assertNotNull(messageBundleService);
        resourceBundleEN = ResourceBundle.getBundle("org/sakaiproject/messagebundle/impl/test/bundle", localeEn);
        resourceBundleFr = ResourceBundle.getBundle("org/sakaiproject/messagebundle/impl/test/bundle", localeFr);

        messageBundleService.saveOrUpdate(baseName, moduleName, resourceBundleEN, localeEn, false);
        messageBundleService.saveOrUpdate(baseName, moduleName, resourceBundleFr, localeFr, false);
        
	}

    public void testSearch(){
    }

    public void testGetMessageBundleProperty() {
    }

    public void testUpdateMessageBundleProperty(){
        List<MessageBundleProperty> list = messageBundleService.getAllProperties(null, null);
        MessageBundleProperty prop = (MessageBundleProperty)list.get(0);
        prop.setValue("newvalue");
        messageBundleService.updateMessageBundleProperty(prop);

        MessageBundleProperty loadedProp = messageBundleService.getMessageBundleProperty(prop.getId());
        Assert.assertEquals(loadedProp.getValue() , "newvalue");
        
    }

    public void testGetModifiedProperties() {
    }

    public void testGetLocales(){

    }

    public void getModifiedPropertiesCount(){

    }

    public void testGetAllProperties(){
        List<MessageBundleProperty> props = messageBundleService.getAllProperties(localeEn.toString(), moduleName);
        for (Iterator i=props.iterator();i.hasNext();) {
            MessageBundleProperty mbp = (MessageBundleProperty) i.next();
        }
        props = messageBundleService.getAllProperties(localeFr.toString(), moduleName);
        for (Iterator i=props.iterator();i.hasNext();) {
            MessageBundleProperty mbp = (MessageBundleProperty) i.next();
        }

    }

    public void testRevertAll(String locale){

    }

    public void testImportProperties(){

    }

    public void testGetAllModuleNames(){
        List moduleNames = messageBundleService.getAllModuleNames();
        Assert.assertNotNull(moduleNames);
        Assert.assertTrue(moduleNames.size() == 1);
        Assert.assertEquals((String) moduleNames.get(0), moduleName);
    }

    public void testGetAllBaseNames(){
        List baseNames = messageBundleService.getAllBaseNames();
        Assert.assertNotNull(baseNames);
        Assert.assertTrue(baseNames.size() == 1);
        Assert.assertEquals((String) baseNames.get(0), baseName);
    }

    public void testRevert(){
        List<MessageBundleProperty> list = messageBundleService.getAllProperties(localeEn.toString(), moduleName);
        MessageBundleProperty prop = (MessageBundleProperty)list.get(0);
        prop.setValue("newvalue");
        messageBundleService.updateMessageBundleProperty(prop);
        messageBundleService.revert(prop);

        MessageBundleProperty loadedProp = messageBundleService.getMessageBundleProperty(prop.getId());
        Assert.assertTrue(loadedProp.getValue() == null);


    }

    public void testGetSearchCount(){

    }

    public void testGetBundle(){
        // data gets loaded in setup(), this just validates the data is correct upon loading it

        Map enLoadedData = messageBundleService.getBundle(baseName, moduleName, localeEn);
        Map frLoadedData = messageBundleService.getBundle(baseName, moduleName, localeFr);

        for (Iterator i=enLoadedData.entrySet().iterator();i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            Assert.assertEquals(resourceBundleEN.getString((String)entry.getKey()), (String)entry.getValue());
            int key = Integer.valueOf((String) entry.getKey());
            int value = Integer.valueOf((String) entry.getValue());
            // en values are equal
            Assert.assertEquals(key, value);
        }
        for (Iterator i=frLoadedData.entrySet().iterator();i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            Assert.assertEquals(resourceBundleFr.getString((String)entry.getKey()), (String)entry.getValue());
            int key = Integer.valueOf((String) entry.getKey());
            int value = Integer.valueOf((String) entry.getValue());
            // fr values value is 1 greater than key
            Assert.assertEquals(key+1, value);
        }


    }

   public void testGetBundleNotFound(){
        Map map = messageBundleService.getBundle("asdf", "asdf", localeEn);
        Assert.assertNotNull(map);
        Assert.assertTrue(map.values().size() == 0);               
    }




}
